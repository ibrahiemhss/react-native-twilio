/**
 * Component to orchestrate the Twilio Video connection and the various video
 * views.
 *
 *
 * Authors:
 * Ralph Pina <ralph.pina></ralph.pina>@gmail.com>
 * Jonathan Chang <slycoder></slycoder>@gmail.com>
 */
package com.twilio.views

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.facebook.react.bridge.*
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.events.RCTEventEmitter
import com.twilio.utils.Events.Companion.ON_CAMERA_SWITCHED
import com.twilio.views.utils.isH264Supported
import com.twilio.utils.Events
import com.twilio.video.*
import tvi.webrtc.Camera1Enumerator
import tvi.webrtc.voiceengine.WebRtcAudioManager
import java.nio.ByteBuffer

class CustomTwilioVideoView : View, LifecycleEventListener, OnAudioFocusChangeListener {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}

    private var enableRemoteAudio = false
    private var enableNetworkQualityReporting = false
    private var isVideoEnabled = false
    private var dominantSpeakerEnabled = false
    private var maintainVideoTrackInBackground = false
    private var cameraType: String? = ""
    private var enableH264Codec = false

    private var themedReactContext: ThemedReactContext? = null
    private var eventEmitter: RCTEventEmitter? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var playbackAttributes: AudioAttributes? = null
    private var roomName: String? = null
    private var accessToken: String? = null
    private var localParticipant: LocalParticipant? = null
    private var localAudioTrack: LocalAudioTrack? = null
    private var audioManager: AudioManager? = null
    private var previousAudioMode = 0
    private var disconnectedFromOnDestroy = false
    private var intentFilter: IntentFilter? = null
    private var myNoisyAudioStreamReceiver: BecomingNoisyReceiver? = null

    // Dedicated thread and handler for messages received from a RemoteDataTrack
    private val dataTrackMessageThread = HandlerThread(DATA_TRACK_MESSAGE_THREAD_NAME)
    private var dataTrackMessageThreadHandler: Handler? = null
    private var localDataTrack: LocalDataTrack? = null

    // Map used to map remote data tracks to remote participants
    private val dataTrackRemoteParticipantMap: MutableMap<RemoteDataTrack?, RemoteParticipant> =
        HashMap()

    constructor(context: ThemedReactContext?) : super(context) {
        themedReactContext = context
        eventEmitter = themedReactContext!!.getJSModule(RCTEventEmitter::class.java)

        // add lifecycle for onResume and on onPause
        themedReactContext!!.addLifecycleEventListener(this)

        /*
         * Needed for setting/abandoning audio focus during call
         */audioManager =
            themedReactContext!!.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        myNoisyAudioStreamReceiver = BecomingNoisyReceiver()
        intentFilter = IntentFilter(Intent.ACTION_HEADSET_PLUG)

        // Create the local data track
        // localDataTrack = LocalDataTrack.create(this);
        localDataTrack = LocalDataTrack.create(getContext())

        // Start the thread where data messages are received
        dataTrackMessageThread.start()
        dataTrackMessageThreadHandler = Handler(dataTrackMessageThread.looper)
    }

    // ===== SETUP =================================================================================
    private fun buildVideoFormat(): VideoFormat {
        return VideoFormat(VideoDimensions.CIF_VIDEO_DIMENSIONS, 15)
    }

    private fun createCameraCaputer(context: Context, cameraId: String?): CameraCapturer? {
        var newCameraCapturer: CameraCapturer? = null
        return try {
            newCameraCapturer = CameraCapturer(
                context,
                cameraId!!,
                object : CameraCapturer.Listener {
                    override fun onFirstFrameAvailable() {}
                    override fun onCameraSwitched(newCameraId: String) {
                        setThumbnailMirror()
                        val event: WritableMap = WritableNativeMap()
                        event.putBoolean("isBackCamera", isCurrentCameraSourceBackFacing)
                        pushEvent(this@CustomTwilioVideoView, ON_CAMERA_SWITCHED, event)
                    }

                    override fun onError(i: Int) {
                        Log.i("CustomTwilioVideoView", "Error getting camera")
                    }
                }
            )
            newCameraCapturer
        } catch (e: Exception) {
            null
        }
    }

    private fun buildDeviceInfo() {
        val enumerator = Camera1Enumerator()
        val deviceNames = enumerator.deviceNames
        backFacingDevice = null
        frontFacingDevice = null
        for (deviceName in deviceNames) {
            if (enumerator.isBackFacing(deviceName) && enumerator.getSupportedFormats(deviceName).size > 0) {
                backFacingDevice = deviceName
            } else if (enumerator.isFrontFacing(deviceName) && enumerator.getSupportedFormats(
                    deviceName
                ).size > 0
            ) {
                frontFacingDevice = deviceName
            }
        }
    }

    private fun createLocalVideo(enableVideo: Boolean, cameraType: String): Boolean {
        isVideoEnabled = enableVideo

        // Share your camera
        buildDeviceInfo()
        if (cameraType == FRONT_CAMERA_TYPE) {
            if (frontFacingDevice != null) {
                cameraCapturer = createCameraCaputer(context, frontFacingDevice)
            } else {
                // IF the camera is unavailable try the other camera
                cameraCapturer = createCameraCaputer(context, backFacingDevice)
            }
        } else {
            if (backFacingDevice != null) {
                cameraCapturer = createCameraCaputer(context, backFacingDevice)
            } else {
                // IF the camera is unavailable try the other camera
                cameraCapturer = createCameraCaputer(context, frontFacingDevice)
            }
        }

        // If no camera is available let the caller know
        if (cameraCapturer == null) {
            val event: WritableMap = WritableNativeMap()
            event.putString("error", "No camera is supported on this device")
            pushEvent(this@CustomTwilioVideoView, Events.ON_CONNECT_FAILURE, event)
            return false
        }
        localVideoTrack =
            LocalVideoTrack.create(context, enableVideo, cameraCapturer!!, buildVideoFormat())
        if (thumbnailVideoView != null && localVideoTrack != null) {
            localVideoTrack!!.addSink(thumbnailVideoView!!)
        }
        setThumbnailMirror()
        return true
    }

    // ===== LIFECYCLE EVENTS ======================================================================
    override fun onHostResume() {
        /*
         * In case it wasn't set.
         */
        if (themedReactContext!!.currentActivity != null) {
            /*
             * If the local video track was released when the app was put in the background, recreate.
             */
            if (cameraCapturer != null && localVideoTrack == null) {
                localVideoTrack = LocalVideoTrack.create(
                    context,
                    isVideoEnabled,
                    cameraCapturer!!,
                    buildVideoFormat()
                )
            }
            if (localVideoTrack != null) {
                if (thumbnailVideoView != null) {
                    localVideoTrack!!.addSink(thumbnailVideoView!!)
                }

                /*
                 * If connected to a Room then share the local video track.
                 */if (localParticipant != null) {
                    localParticipant!!.publishTrack(localVideoTrack!!)
                }
            }
            if (room != null) {
                themedReactContext!!.currentActivity!!.volumeControlStream =
                    AudioManager.STREAM_VOICE_CALL
            }
        }
    }

    override fun onHostPause() {
        /*
         * Release the local video track before going in the background. This ensures that the
         * camera can be used by other applications while this app is in the background.
         */
        if (localVideoTrack != null && !maintainVideoTrackInBackground) {
            /*
             * If this local video track is being shared in a Room, remove from local
             * participant before releasing the video track. Participants will be notified that
             * the track has been removed.
             */
            if (localParticipant != null) {
                localParticipant!!.unpublishTrack(localVideoTrack!!)
            }
            localVideoTrack!!.release()
            localVideoTrack = null
        }
    }

    override fun onHostDestroy() {
        /*
         * Remove stream voice control
         */
        if (themedReactContext!!.currentActivity != null) {
            themedReactContext!!.currentActivity!!.volumeControlStream =
                AudioManager.USE_DEFAULT_STREAM_TYPE
        }
        /*
         * Always disconnect from the room before leaving the Activity to
         * ensure any memory allocated to the Room resource is freed.
         */if (room != null && room!!.state != Room.State.DISCONNECTED) {
            room!!.disconnect()
            disconnectedFromOnDestroy = true
        }

        /*
         * Release the local media ensuring any memory allocated to audio or video is freed.
         */if (localVideoTrack != null) {
            localVideoTrack!!.release()
            localVideoTrack = null
        }
        if (localAudioTrack != null) {
            localAudioTrack!!.release()
            audioManager!!.stopBluetoothSco()
            localAudioTrack = null
        }

        // Quit the data track message thread
        dataTrackMessageThread.quit()
    }

    fun releaseResource() {
        themedReactContext!!.removeLifecycleEventListener(this)
        room = null
        localVideoTrack = null
        thumbnailVideoView = null
        cameraCapturer = null
    }

    // ====== CONNECTING ===========================================================================
    fun connectToRoomWrapper(
        roomName: String?,
        accessToken: String?,
        enableAudio: Boolean,
        enableVideo: Boolean,
        enableRemoteAudio: Boolean,
        enableNetworkQualityReporting: Boolean,
        dominantSpeakerEnabled: Boolean,
        maintainVideoTrackInBackground: Boolean,
        cameraType: String,
        enableH264Codec: Boolean
    ) {
        this.roomName = roomName
        this.accessToken = accessToken
        this.enableRemoteAudio = enableRemoteAudio
        this.enableNetworkQualityReporting = enableNetworkQualityReporting
        this.dominantSpeakerEnabled = dominantSpeakerEnabled
        this.maintainVideoTrackInBackground = maintainVideoTrackInBackground
        this.cameraType = cameraType
        this.enableH264Codec = enableH264Codec

        // Share your microphone
        localAudioTrack = LocalAudioTrack.create(context, enableAudio)
        if (cameraCapturer == null && enableVideo) {
            val createVideoStatus = createLocalVideo(enableVideo, cameraType)
            if (!createVideoStatus) {
                Log.d("RNTwilioVideo", "Failed to create local video")
                // No need to connect to room if video creation failed
                return
            }
        } else {
            isVideoEnabled = false
        }
        setAudioFocus(enableAudio)
        connectToRoom()
    }

    fun connectToRoom() {
        /*
         * Create a VideoClient allowing you to connect to a Room
         */
        val connectOptionsBuilder = ConnectOptions.Builder(accessToken!!)
        if (roomName != null) {
            connectOptionsBuilder.roomName(roomName!!)
        }
        if (localAudioTrack != null) {
            connectOptionsBuilder.audioTracks(listOf(localAudioTrack!!))
        }
        if (localVideoTrack != null) {
            connectOptionsBuilder.videoTracks(listOf(localVideoTrack))
        }

        //LocalDataTrack localDataTrack = LocalDataTrack.create(getContext());
        if (localDataTrack != null) {
            connectOptionsBuilder.dataTracks(listOf(localDataTrack!!))
        }

        // H264 Codec Support Detection: https://www.twilio.com/docs/video/managing-codecs
        Log.d("RNTwilioVideo", "H264 supported by hardware: " + isH264Supported())
        val supportedCodecs: WritableArray = WritableNativeArray()
        var videoCodec: VideoCodec = Vp8Codec()
        // VP8 is supported on all android devices by default
        supportedCodecs.pushString(videoCodec.toString())
        if (isH264Supported() && enableH264Codec) {
            videoCodec = H264Codec()
            supportedCodecs.pushString(videoCodec.toString())
        }
        val event: WritableMap = WritableNativeMap()
        event.putArray("supportedCodecs", supportedCodecs)
        pushEvent(this@CustomTwilioVideoView, Events.ON_LOCAL_PARTICIPANT_SUPPORTED_CODECS, event)
        connectOptionsBuilder.preferVideoCodecs(listOf(videoCodec))
        connectOptionsBuilder.enableDominantSpeaker(dominantSpeakerEnabled)
        if (enableNetworkQualityReporting) {
            connectOptionsBuilder.enableNetworkQuality(true)
            connectOptionsBuilder.networkQualityConfiguration(
                NetworkQualityConfiguration(
                    NetworkQualityVerbosity.NETWORK_QUALITY_VERBOSITY_MINIMAL,
                    NetworkQualityVerbosity.NETWORK_QUALITY_VERBOSITY_MINIMAL
                )
            )
        }
        room = Video.connect(context, connectOptionsBuilder.build(), roomListener())
    }

    fun setAudioType() {
        var devicesInfo = arrayOfNulls<AudioDeviceInfo>(0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            devicesInfo = audioManager!!.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        }
        var hasNonSpeakerphoneDevice = false
        for (i in devicesInfo.indices) {
            var deviceType = 0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                deviceType = devicesInfo[i]!!.type
            }
            if (deviceType == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                deviceType == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
            ) {
                hasNonSpeakerphoneDevice = true
            }
            if (deviceType == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                deviceType == AudioDeviceInfo.TYPE_BLUETOOTH_SCO
            ) {
                audioManager!!.startBluetoothSco()
                audioManager!!.isBluetoothScoOn = true
                hasNonSpeakerphoneDevice = true
            }
        }
        audioManager!!.isSpeakerphoneOn = !hasNonSpeakerphoneDevice
    }

    private fun setAudioFocus(focus: Boolean) {
        if (focus) {
            previousAudioMode = audioManager!!.mode
            // Request audio focus before making any device switch.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                audioManager!!.requestAudioFocus(
                    this,
                    AudioManager.STREAM_VOICE_CALL,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
                )
            } else {
                playbackAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
                audioFocusRequest =
                    AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                        .setAudioAttributes(playbackAttributes!!)
                        .setAcceptsDelayedFocusGain(true)
                        .setOnAudioFocusChangeListener(this, handler)
                        .build()
                audioManager!!.requestAudioFocus(audioFocusRequest!!)
            }
            /*
             * Use MODE_IN_COMMUNICATION as the default audio mode. It is required
             * to be in this mode when playout and/or recording starts for the best
             * possible VoIP performance. Some devices have difficulties with
             * speaker mode if this is not set.
             */audioManager!!.mode = AudioManager.MODE_IN_COMMUNICATION
            setAudioType()
            context.registerReceiver(myNoisyAudioStreamReceiver, intentFilter)
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                audioManager!!.abandonAudioFocus(this)
            } else if (audioFocusRequest != null) {
                audioManager!!.abandonAudioFocusRequest(audioFocusRequest!!)
            }
            audioManager!!.isSpeakerphoneOn = false
            audioManager!!.mode = previousAudioMode
            try {
                if (myNoisyAudioStreamReceiver != null) {
                    context.unregisterReceiver(myNoisyAudioStreamReceiver)
                }
                myNoisyAudioStreamReceiver = null
            } catch (e: Exception) {
                // already registered
                e.printStackTrace()
            }
        }
    }

    private inner class BecomingNoisyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
//            audioManager.setSpeakerphoneOn(true);
            if (Intent.ACTION_HEADSET_PLUG == intent.action) {
                setAudioType()
            }
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        Log.e(TAG, "onAudioFocusChange: focuschange: $focusChange")
    }

    // ====== DISCONNECTING ========================================================================
    fun disconnect() {
        if (room != null) {
            room!!.disconnect()
        }
        if (localAudioTrack != null) {
            localAudioTrack!!.release()
            localAudioTrack = null
            audioManager!!.stopBluetoothSco()
        }
        if (localVideoTrack != null) {
            localVideoTrack!!.release()
            localVideoTrack = null
            audioManager!!.stopBluetoothSco()
        }
        setAudioFocus(false)
        if (cameraCapturer != null) {
            cameraCapturer!!.stopCapture()
            cameraCapturer = null
        }
    }

    // ===== SEND STRING ON DATA TRACK ======================================================================
    fun sendString(message: String?) {
        if (localDataTrack != null) {
            localDataTrack!!.send(message!!)
        }
    }

    fun switchCamera() {
        if (cameraCapturer != null) {
            val isBackCamera = isCurrentCameraSourceBackFacing
            cameraType =
                if (frontFacingDevice != null && (isBackCamera || backFacingDevice == null)) {
                    cameraCapturer!!.switchCamera(frontFacingDevice!!)
                    FRONT_CAMERA_TYPE
                } else {
                    cameraCapturer!!.switchCamera(backFacingDevice!!)
                    BACK_CAMERA_TYPE
                }
        }
    }

    fun toggleVideo(enabled: Boolean) {
        isVideoEnabled = enabled
        if (cameraCapturer == null && enabled) {
            val fallbackCameraType = if (cameraType == null) FRONT_CAMERA_TYPE else cameraType!!
            val createVideoStatus = createLocalVideo(true, fallbackCameraType)
            if (!createVideoStatus) {
                Log.d("RNTwilioVideo", "Failed to create local video")
                return
            }
        }
        if (localVideoTrack != null) {
            localVideoTrack!!.enable(enabled)
            publishLocalVideo(enabled)
            val event: WritableMap = WritableNativeMap()
            event.putBoolean("videoEnabled", enabled)
            pushEvent(this@CustomTwilioVideoView, Events.ON_VIDEO_CHANGED, event)
        }
    }

    fun toggleSoundSetup(speaker: Boolean) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (speaker) {
            audioManager.isSpeakerphoneOn = true
        } else {
            audioManager.isSpeakerphoneOn = false
        }
    }

    fun toggleAudio(enabled: Boolean) {
        if (localAudioTrack != null) {
            localAudioTrack!!.enable(enabled)
            val event: WritableMap = WritableNativeMap()
            event.putBoolean("audioEnabled", enabled)
            pushEvent(this@CustomTwilioVideoView, Events.ON_AUDIO_CHANGED, event)
        }
    }

    fun toggleBluetoothHeadset(enabled: Boolean) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (enabled) {
            audioManager.startBluetoothSco()
            audioManager.isSpeakerphoneOn = false
        } else {
            audioManager.stopBluetoothSco()
            audioManager.isSpeakerphoneOn = true
        }
    }

    fun toggleRemoteAudio(enabled: Boolean) {
        if (room != null) {
            for (rp in room!!.remoteParticipants) {
                for (at in rp.audioTracks) {
                    if (at.audioTrack != null) {
                        (at.audioTrack as RemoteAudioTrack?)!!.enablePlayback(enabled)
                    }
                }
            }
        }
    }

    fun publishLocalVideo(enabled: Boolean) {
        if (localParticipant != null && localVideoTrack != null) {
            if (enabled) {
                localParticipant!!.publishTrack(localVideoTrack!!)
            } else {
                localParticipant!!.unpublishTrack(localVideoTrack!!)
            }
        }
    }

    fun publishLocalAudio(enabled: Boolean) {
        if (localParticipant != null && localAudioTrack != null) {
            if (enabled) {
                localParticipant!!.publishTrack(localAudioTrack!!)
            } else {
                localParticipant!!.unpublishTrack(localAudioTrack!!)
            }
        }
    }

    private fun convertBaseTrackStats(bs: BaseTrackStats, result: WritableMap) {
        result.putString("codec", bs.codec)
        result.putInt("packetsLost", bs.packetsLost)
        result.putString("ssrc", bs.ssrc)
        result.putDouble("timestamp", bs.timestamp)
        result.putString("trackSid", bs.trackSid)
    }

    private fun convertLocalTrackStats(ts: LocalTrackStats, result: WritableMap) {
        result.putDouble("bytesSent", ts.bytesSent.toDouble())
        result.putInt("packetsSent", ts.packetsSent)
        result.putDouble("roundTripTime", ts.roundTripTime.toDouble())
    }

    private fun convertRemoteTrackStats(ts: RemoteTrackStats, result: WritableMap) {
        result.putDouble("bytesReceived", ts.bytesReceived.toDouble())
        result.putInt("packetsReceived", ts.packetsReceived)
    }

    private fun convertAudioTrackStats(`as`: RemoteAudioTrackStats): WritableMap {
        val result: WritableMap = WritableNativeMap()
        result.putInt("audioLevel", `as`.audioLevel)
        result.putInt("jitter", `as`.jitter)
        convertBaseTrackStats(`as`, result)
        convertRemoteTrackStats(`as`, result)
        return result
    }

    private fun convertLocalAudioTrackStats(`as`: LocalAudioTrackStats): WritableMap {
        val result: WritableMap = WritableNativeMap()
        result.putInt("audioLevel", `as`.audioLevel)
        result.putInt("jitter", `as`.jitter)
        convertBaseTrackStats(`as`, result)
        convertLocalTrackStats(`as`, result)
        return result
    }

    private fun convertVideoTrackStats(vs: RemoteVideoTrackStats): WritableMap {
        val result: WritableMap = WritableNativeMap()
        val dimensions: WritableMap = WritableNativeMap()
        dimensions.putInt("height", vs.dimensions.height)
        dimensions.putInt("width", vs.dimensions.width)
        result.putMap("dimensions", dimensions)
        result.putInt("frameRate", vs.frameRate)
        convertBaseTrackStats(vs, result)
        convertRemoteTrackStats(vs, result)
        return result
    }

    private fun convertLocalVideoTrackStats(vs: LocalVideoTrackStats): WritableMap {
        val result: WritableMap = WritableNativeMap()
        val dimensions: WritableMap = WritableNativeMap()
        dimensions.putInt("height", vs.dimensions.height)
        dimensions.putInt("width", vs.dimensions.width)
        result.putMap("dimensions", dimensions)
        result.putInt("frameRate", vs.frameRate)
        convertBaseTrackStats(vs, result)
        convertLocalTrackStats(vs, result)
        return result
    }

    val stats: Unit
        get() {
            if (room != null) {
                room!!.getStats { statsReports ->
                    val event: WritableMap = WritableNativeMap()
                    for (sr in statsReports) {
                        val connectionStats: WritableMap = WritableNativeMap()
                        val `as`: WritableArray = WritableNativeArray()
                        for (s in sr.remoteAudioTrackStats) {
                            `as`.pushMap(convertAudioTrackStats(s))
                        }
                        connectionStats.putArray("remoteAudioTrackStats", `as`)
                        val vs: WritableArray = WritableNativeArray()
                        for (s in sr.remoteVideoTrackStats) {
                            vs.pushMap(convertVideoTrackStats(s))
                        }
                        connectionStats.putArray("remoteVideoTrackStats", vs)
                        val las: WritableArray = WritableNativeArray()
                        for (s in sr.localAudioTrackStats) {
                            las.pushMap(convertLocalAudioTrackStats(s))
                        }
                        connectionStats.putArray("localAudioTrackStats", las)
                        val lvs: WritableArray = WritableNativeArray()
                        for (s in sr.localVideoTrackStats) {
                            lvs.pushMap(convertLocalVideoTrackStats(s))
                        }
                        connectionStats.putArray("localVideoTrackStats", lvs)
                        event.putMap(sr.peerConnectionId, connectionStats)
                    }
                    pushEvent(this@CustomTwilioVideoView, Events.ON_STATS_RECEIVED, event)
                }
            }
        }

    fun disableOpenSLES() {
        WebRtcAudioManager.setBlacklistDeviceForOpenSLESUsage(true)
    }

    // ====== ROOM LISTENER ========================================================================
    /*
     * Room events listener
     */
    private fun roomListener(): Room.Listener {
        return object : Room.Listener {
            override fun onConnected(room: Room) {
                /*
                 * Enable changing the volume using the up/down keys during a conversation
                 */
                if (themedReactContext!!.currentActivity != null) {
                    themedReactContext!!.currentActivity!!.volumeControlStream =
                        AudioManager.STREAM_VOICE_CALL
                }
                localParticipant = room.localParticipant
                localParticipant!!.setListener(localListener())
                val event: WritableMap = WritableNativeMap()
                event.putString("roomName", room.name)
                event.putString("roomSid", room.sid)
                val participants = room.remoteParticipants
                val participantsArray: WritableArray = WritableNativeArray()
                for (participant in participants) {
                    participantsArray.pushMap(buildParticipant(participant))
                }
                participantsArray.pushMap(buildParticipant(localParticipant))
                event.putArray("participants", participantsArray)
                event.putMap("localParticipant", buildParticipant(localParticipant))
                pushEvent(this@CustomTwilioVideoView, Events.ON_CONNECTED, event)


                //There is not .publish it's publishTrack
                localParticipant!!.publishTrack(localDataTrack!!)
                for (participant in participants) {
                    addParticipant(room, participant)
                }
            }

            override fun onConnectFailure(room: Room, e: TwilioException) {
                val event: WritableMap = WritableNativeMap()
                event.putString("roomName", room.name)
                event.putString("roomSid", room.sid)
                event.putString("error", e.message)
                pushEvent(this@CustomTwilioVideoView, Events.ON_CONNECT_FAILURE, event)
            }

            override fun onReconnecting(room: Room, twilioException: TwilioException) {}
            override fun onReconnected(room: Room) {}
            override fun onDisconnected(room: Room, e: TwilioException?) {
                val event: WritableMap = WritableNativeMap()

                /*
                 * Remove stream voice control
                 */if (themedReactContext!!.currentActivity != null) {
                    themedReactContext!!.currentActivity!!.volumeControlStream =
                        AudioManager.USE_DEFAULT_STREAM_TYPE
                }
                if (localParticipant != null) {
                    event.putString("participant", localParticipant!!.identity)
                }
                event.putString("roomName", room.name)
                event.putString("roomSid", room.sid)
                if (e != null) {
                    event.putString("error", e.message)
                }
                pushEvent(this@CustomTwilioVideoView, Events.ON_DISCONNECTED, event)
                localParticipant = null
                roomName = null
                accessToken = null
                Companion.room = null
                // Only reinitialize the UI if disconnect was not called from onDestroy()
                if (!disconnectedFromOnDestroy) {
                    setAudioFocus(false)
                }
            }

            override fun onParticipantConnected(room: Room, participant: RemoteParticipant) {
                addParticipant(room, participant)
            }

            override fun onParticipantDisconnected(room: Room, participant: RemoteParticipant) {
                removeParticipant(room, participant)
            }

            override fun onRecordingStarted(room: Room) {}
            override fun onRecordingStopped(room: Room) {}
            override fun onDominantSpeakerChanged(
                room: Room,
                remoteParticipant: RemoteParticipant?
            ) {
                val event: WritableMap = WritableNativeMap()
                event.putString("roomName", room.name)
                event.putString("roomSid", room.sid)
                if (remoteParticipant == null) {
                    event.putString("participant", "")
                } else {
                    event.putMap("participant", buildParticipant(remoteParticipant))
                }
                pushEvent(this@CustomTwilioVideoView, Events.ON_DOMINANT_SPEAKER_CHANGED, event)
            }
        }
    }

    /*
     * Called when participant joins the room
     */
    private fun addParticipant(room: Room, remoteParticipant: RemoteParticipant) {
        val event: WritableMap = WritableNativeMap()
        event.putString("roomName", room.name)
        event.putString("roomSid", room.sid)
        event.putMap("participant", buildParticipant(remoteParticipant))
        pushEvent(this, Events.ON_PARTICIPANT_CONNECTED, event)

        /*
         * Start listening for participant media events
         */remoteParticipant.setListener(mediaListener())
        for (remoteDataTrackPublication in remoteParticipant.remoteDataTracks) {
            /*
             * Data track messages are received on the thread that calls setListener. Post the
             * invocation of setting the listener onto our dedicated data track message thread.
             */
            if (remoteDataTrackPublication.isTrackSubscribed) {
                dataTrackMessageThreadHandler!!.post {
                    addRemoteDataTrack(
                        remoteParticipant,
                        remoteDataTrackPublication.remoteDataTrack
                    )
                }
            }
        }
    }

    /*
     * Called when participant leaves the room
     */
    private fun removeParticipant(room: Room, participant: RemoteParticipant) {
        val event: WritableMap = WritableNativeMap()
        event.putString("roomName", room.name)
        event.putString("roomSid", room.sid)
        event.putMap("participant", buildParticipant(participant))
        pushEvent(this, Events.ON_PARTICIPANT_DISCONNECTED, event)
        //something about this breaking.
        //participant.setListener(null);
    }

    private fun addRemoteDataTrack(
        remoteParticipant: RemoteParticipant,
        remoteDataTrack: RemoteDataTrack?
    ) {
        dataTrackRemoteParticipantMap[remoteDataTrack] = remoteParticipant
        remoteDataTrack!!.setListener(remoteDataTrackListener())
    }

    // ====== MEDIA LISTENER =======================================================================
    private fun mediaListener(): RemoteParticipant.Listener {
        return object : RemoteParticipant.Listener {
            override fun onAudioTrackSubscribed(
                participant: RemoteParticipant,
                publication: RemoteAudioTrackPublication,
                audioTrack: RemoteAudioTrack
            ) {
                audioTrack.enablePlayback(enableRemoteAudio)
                val event = buildParticipantVideoEvent(participant, publication)
                pushEvent(
                    this@CustomTwilioVideoView,
                    Events.ON_PARTICIPANT_ADDED_AUDIO_TRACK,
                    event
                )
            }

            override fun onAudioTrackUnsubscribed(
                participant: RemoteParticipant,
                publication: RemoteAudioTrackPublication,
                audioTrack: RemoteAudioTrack
            ) {
                val event = buildParticipantVideoEvent(participant, publication)
                pushEvent(
                    this@CustomTwilioVideoView,
                    Events.ON_PARTICIPANT_REMOVED_AUDIO_TRACK,
                    event
                )
            }

            override fun onAudioTrackSubscriptionFailed(
                participant: RemoteParticipant,
                publication: RemoteAudioTrackPublication,
                twilioException: TwilioException
            ) {
            }

            override fun onAudioTrackPublished(
                participant: RemoteParticipant,
                publication: RemoteAudioTrackPublication
            ) {
            }

            override fun onAudioTrackUnpublished(
                participant: RemoteParticipant,
                publication: RemoteAudioTrackPublication
            ) {
            }

            override fun onDataTrackSubscribed(
                remoteParticipant: RemoteParticipant,
                remoteDataTrackPublication: RemoteDataTrackPublication,
                remoteDataTrack: RemoteDataTrack
            ) {
                val event = buildParticipantDataEvent(remoteParticipant, remoteDataTrackPublication)
                pushEvent(this@CustomTwilioVideoView, Events.ON_PARTICIPANT_ADDED_DATA_TRACK, event)
                dataTrackMessageThreadHandler!!.post {
                    addRemoteDataTrack(
                        remoteParticipant,
                        remoteDataTrack
                    )
                }
            }

            override fun onDataTrackUnsubscribed(
                remoteParticipant: RemoteParticipant,
                remoteDataTrackPublication: RemoteDataTrackPublication,
                remoteDataTrack: RemoteDataTrack
            ) {
                val event = buildParticipantDataEvent(remoteParticipant, remoteDataTrackPublication)
                pushEvent(
                    this@CustomTwilioVideoView,
                    Events.ON_PARTICIPANT_REMOVED_DATA_TRACK,
                    event
                )
            }

            override fun onDataTrackSubscriptionFailed(
                participant: RemoteParticipant,
                publication: RemoteDataTrackPublication,
                twilioException: TwilioException
            ) {
            }

            override fun onDataTrackPublished(
                participant: RemoteParticipant,
                publication: RemoteDataTrackPublication
            ) {
            }

            override fun onDataTrackUnpublished(
                participant: RemoteParticipant,
                publication: RemoteDataTrackPublication
            ) {
            }

            override fun onVideoTrackSubscribed(
                participant: RemoteParticipant,
                publication: RemoteVideoTrackPublication,
                videoTrack: RemoteVideoTrack
            ) {
                addParticipantVideo(participant, publication)
            }

            override fun onVideoTrackUnsubscribed(
                participant: RemoteParticipant,
                publication: RemoteVideoTrackPublication,
                videoTrack: RemoteVideoTrack
            ) {
                removeParticipantVideo(participant, publication)
            }

            override fun onVideoTrackSubscriptionFailed(
                participant: RemoteParticipant,
                publication: RemoteVideoTrackPublication,
                twilioException: TwilioException
            ) {
            }

            override fun onVideoTrackPublished(
                participant: RemoteParticipant,
                publication: RemoteVideoTrackPublication
            ) {
            }

            override fun onVideoTrackUnpublished(
                participant: RemoteParticipant,
                publication: RemoteVideoTrackPublication
            ) {
            }

            override fun onAudioTrackEnabled(
                participant: RemoteParticipant,
                publication: RemoteAudioTrackPublication
            ) { //                Log.i(TAG, "onAudioTrackEnabled");
//                publication.getRemoteAudioTrack().enablePlayback(false);
                val event = buildParticipantVideoEvent(participant, publication)
                pushEvent(
                    this@CustomTwilioVideoView,
                    Events.ON_PARTICIPANT_ENABLED_AUDIO_TRACK,
                    event
                )
            }

            override fun onAudioTrackDisabled(
                participant: RemoteParticipant,
                publication: RemoteAudioTrackPublication
            ) {
                val event = buildParticipantVideoEvent(participant, publication)
                pushEvent(
                    this@CustomTwilioVideoView,
                    Events.ON_PARTICIPANT_DISABLED_AUDIO_TRACK,
                    event
                )
            }

            override fun onVideoTrackEnabled(
                participant: RemoteParticipant,
                publication: RemoteVideoTrackPublication
            ) {
                val event = buildParticipantVideoEvent(participant, publication)
                pushEvent(
                    this@CustomTwilioVideoView,
                    Events.ON_PARTICIPANT_ENABLED_VIDEO_TRACK,
                    event
                )
            }

            override fun onVideoTrackDisabled(
                participant: RemoteParticipant,
                publication: RemoteVideoTrackPublication
            ) {
                val event = buildParticipantVideoEvent(participant, publication)
                pushEvent(
                    this@CustomTwilioVideoView,
                    Events.ON_PARTICIPANT_DISABLED_VIDEO_TRACK,
                    event
                )
            }

            override fun onNetworkQualityLevelChanged(
                remoteParticipant: RemoteParticipant,
                networkQualityLevel: NetworkQualityLevel
            ) {
                val event: WritableMap = WritableNativeMap()
                event.putMap("participant", buildParticipant(remoteParticipant))
                event.putBoolean("isLocalUser", false)

                // Twilio SDK defines Enum 0 as UNKNOWN and 1 as Quality ZERO, so we subtract one to get the correct quality level as an integer
                event.putInt("quality", networkQualityLevel.ordinal - 1)
                pushEvent(
                    this@CustomTwilioVideoView,
                    Events.ON_NETWORK_QUALITY_LEVELS_CHANGED,
                    event
                )
            }
        }
    }

    // ====== LOCAL LISTENER =======================================================================
    private fun localListener(): LocalParticipant.Listener {
        return object : LocalParticipant.Listener {
            override fun onAudioTrackPublished(
                localParticipant: LocalParticipant,
                localAudioTrackPublication: LocalAudioTrackPublication
            ) {
            }

            override fun onAudioTrackPublicationFailed(
                localParticipant: LocalParticipant,
                localAudioTrack: LocalAudioTrack,
                twilioException: TwilioException
            ) {
            }

            override fun onVideoTrackPublished(
                localParticipant: LocalParticipant,
                localVideoTrackPublication: LocalVideoTrackPublication
            ) {
            }

            override fun onVideoTrackPublicationFailed(
                localParticipant: LocalParticipant,
                localVideoTrack: LocalVideoTrack,
                twilioException: TwilioException
            ) {
            }

            override fun onDataTrackPublished(
                localParticipant: LocalParticipant,
                localDataTrackPublication: LocalDataTrackPublication
            ) {
            }

            override fun onDataTrackPublicationFailed(
                localParticipant: LocalParticipant,
                localDataTrack: LocalDataTrack,
                twilioException: TwilioException
            ) {
            }

            override fun onNetworkQualityLevelChanged(
                localParticipant: LocalParticipant,
                networkQualityLevel: NetworkQualityLevel
            ) {
                val event: WritableMap = WritableNativeMap()
                event.putMap("participant", buildParticipant(localParticipant))
                event.putBoolean("isLocalUser", true)

                // Twilio SDK defines Enum 0 as UNKNOWN and 1 as Quality ZERO, so we subtract one to get the correct quality level as an integer
                event.putInt("quality", networkQualityLevel.ordinal - 1)
                pushEvent(
                    this@CustomTwilioVideoView,
                    Events.ON_NETWORK_QUALITY_LEVELS_CHANGED,
                    event
                )
            }
        }
    }

    private fun buildParticipant(participant: Participant?): WritableMap {
        val participantMap: WritableMap = WritableNativeMap()
        participantMap.putString("identity", participant!!.identity)
        participantMap.putString("sid", participant.sid)
        return participantMap
    }

    private fun buildTrack(publication: TrackPublication): WritableMap {
        val trackMap: WritableMap = WritableNativeMap()
        trackMap.putString("trackSid", publication.trackSid)
        trackMap.putString("trackName", publication.trackName)
        trackMap.putBoolean("enabled", publication.isTrackEnabled)
        return trackMap
    }

    private fun buildParticipantDataEvent(
        participant: Participant,
        publication: TrackPublication
    ): WritableMap {
        val participantMap = buildParticipant(participant)
        val trackMap = buildTrack(publication)
        val event: WritableMap = WritableNativeMap()
        event.putMap("participant", participantMap)
        event.putMap("track", trackMap)
        return event
    }

    private fun buildParticipantVideoEvent(
        participant: Participant,
        publication: TrackPublication
    ): WritableMap {
        val participantMap = buildParticipant(participant)
        val trackMap = buildTrack(publication)
        val event: WritableMap = WritableNativeMap()
        event.putMap("participant", participantMap)
        event.putMap("track", trackMap)
        return event
    }

    private fun buildDataTrackEvent(
        remoteDataTrack: RemoteDataTrack,
        message: String
    ): WritableMap {
        val event: WritableMap = WritableNativeMap()
        event.putString("message", message)
        event.putString("trackSid", remoteDataTrack.sid)
        return event
    }

    private fun addParticipantVideo(
        participant: Participant,
        publication: RemoteVideoTrackPublication
    ) {
        val event = buildParticipantVideoEvent(participant, publication)
        pushEvent(this@CustomTwilioVideoView, Events.ON_PARTICIPANT_ADDED_VIDEO_TRACK, event)
    }

    private fun removeParticipantVideo(
        participant: Participant,
        deleteVideoTrack: RemoteVideoTrackPublication
    ) {
        val event = buildParticipantVideoEvent(participant, deleteVideoTrack)
        pushEvent(this@CustomTwilioVideoView, Events.ON_PARTICIPANT_REMOVED_VIDEO_TRACK, event)
    }

    // ===== EVENTS TO RN ==========================================================================
    fun pushEvent(view: View, name: String?, data: WritableMap?) {
        eventEmitter!!.receiveEvent(view.id, name, data)
    }

    private fun remoteDataTrackListener(): RemoteDataTrack.Listener {
        return object : RemoteDataTrack.Listener {
            override fun onMessage(remoteDataTrack: RemoteDataTrack, byteBuffer: ByteBuffer) {}
            override fun onMessage(remoteDataTrack: RemoteDataTrack, message: String) {
                val event = buildDataTrackEvent(remoteDataTrack, message)
                pushEvent(this@CustomTwilioVideoView, Events.ON_DATATRACK_MESSAGE_RECEIVED, event)
            }
        }
    }

    companion object {
        private const val TAG = "CustomTwilioVideoView"
        private const val DATA_TRACK_MESSAGE_THREAD_NAME = "DataTrackMessages"
        private const val FRONT_CAMERA_TYPE = "front"
        private const val BACK_CAMERA_TYPE = "back"
        private var frontFacingDevice: String? = null
        private var backFacingDevice: String? = null

        /*
     * A Room represents communication between the client and one or more participants.
     */
        private var room: Room? = null

        /*
     * A VideoView receives frames from a local or remote video track and renders them
     * to an associated view.
     */
        private var thumbnailVideoView: PatchedVideoView? = null
        private var localVideoTrack: LocalVideoTrack? = null
        private var cameraCapturer: CameraCapturer? = null
        private val isCurrentCameraSourceBackFacing: Boolean
            private get() = cameraCapturer != null && cameraCapturer!!.cameraId === backFacingDevice

        // ===== BUTTON LISTENERS ======================================================================
        private fun setThumbnailMirror() {
            if (cameraCapturer != null) {
                val isBackCamera = isCurrentCameraSourceBackFacing
                if (thumbnailVideoView != null && thumbnailVideoView!!.visibility == VISIBLE) {
                    thumbnailVideoView!!.mirror = !isBackCamera
                }
            }
        }

        @JvmStatic
        fun registerPrimaryVideoView(v: PatchedVideoView?, trackSid: String) {
            if (room != null) {
                for (participant in room!!.remoteParticipants) {
                    for (publication in participant.remoteVideoTracks) {
                        val track = publication.remoteVideoTrack ?: continue
                        if (publication.trackSid == trackSid) {
                            track.addSink(v!!)
                        } else {
                            track.removeSink(v!!)
                        }
                    }
                }
            }
        }

        @JvmStatic
        fun registerThumbnailVideoView(v: PatchedVideoView?) {
            thumbnailVideoView = v
            if (localVideoTrack != null) {
                localVideoTrack!!.addSink(v!!)
            }
            setThumbnailMirror()
        }
    }
}
