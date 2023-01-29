
import Foundation
import TwilioVideo
import AVFoundation

class TwilioViewController: UIViewController {
    
    
    
    // Video SDK components
    public static var room: Room?
    public static var roomName: String?
    public static var camera: CameraSource?
    public static var localVideoTrack: LocalVideoTrack?
    public static var localAudioTrack: LocalAudioTrack?
    public static var remoteParticipant: RemoteParticipant?
    
    // all static
    public static var textLabel = UILabel(frame: CGRect.zero)
    public static var accessToken : String?
    public static var isLocal: Bool = true
    public static var isStopedCamera: Bool = false
    public static var imgUriPlaceHolder : String?
    public static var imageViewPlaceHolder:UIImageView = UIImageView.init()
    public static var textPlaceHolder : String?
    public static var localTextPlaceHolder : String?
    public static var isCameraClosed : Bool?
    public static var placeHolderContainer: CustomView?
    public static var localPlaceHolderContainer: CustomView?
    public static var placeHolderLabel:UILabel = UILabel.init()
    public static var localPlaceHolderLabel:UILabel = UILabel.init()
    
    
    var remoteView: VideoView?
    var previewView:VideoView = VideoView.init()
    static var viewRect = CGRectMake(0, 0, 48, 48)
    
    // ------------------------------------------------------------------------------------------------------
    func setDataSrc( data :NSDictionary,rect :CGRect){
        TwilioViewController.viewRect=rect
        TwilioViewController.localPlaceHolderContainer = CustomView.init(rect: rect)
        TwilioViewController.placeHolderContainer = CustomView.init(rect: rect)
        
        self.previewView.frame = rect
        self.previewView.contentMode = .scaleAspectFill;
        self.view.addSubview(self.previewView)
        self.view.addSubview(TwilioViewController.localPlaceHolderContainer!)
        
        let dic = NSDictionary(dictionary:data)
        guard let _token = dic.object(forKey: "token") as? String else {
            return
        }
        guard let _roomName = dic.object(forKey: "roomName") as? String else {
            return
        }
        
        let _imgUriPlaceHolder = dic.object(forKey: "imgUriPlaceHolder")
        let _textPlaceHolder = dic.object(forKey: "textPlaceHolder")
        let _localTextPlaceHolder = dic.object(forKey: "localTextPlaceHolder")
        
        TwilioViewController.accessToken = _token
        TwilioViewController.roomName = _roomName
        TwilioViewController.imgUriPlaceHolder = _imgUriPlaceHolder as? String
        TwilioViewController.localTextPlaceHolder = _localTextPlaceHolder as? String
        TwilioViewController.textPlaceHolder = _textPlaceHolder as? String
        
        
        self.connectToARoom()
    }
    
    // ------------------------------------------------------------------------------------------------------
    func switchCamera() {
        let params =
        [TwilioEmitter.ON_CAMERA_SWITCHED:TwilioEmitter.ON_CAMERA_SWITCHED,
        ] as [String : Any]
        TwilioEmitter.emitter.sendEvent(withName: TwilioEmitter.ON_CAMERA_SWITCHED, body:params);
        
        print (" ==== switchCamera")
        flipCamera()
    }
    
    // ------------------------------------------------------------------------------------------------------
    func mute() {
        
        print (" ==== mute")
        
        if (TwilioViewController.localAudioTrack != nil) {
            TwilioEmitter.emitter.sendEvent(withName: TwilioEmitter.ON_MUTE, body: [TwilioEmitter.ON_MUTE:TwilioViewController.localAudioTrack?.isEnabled]);
            
            TwilioViewController.localAudioTrack?.isEnabled = !(TwilioViewController.localAudioTrack?.isEnabled)!
        }
    }
    
    // ------------------------------------------------------------------------------------------------------
    func closeCamera() {
        
        TwilioViewController.localVideoTrack!.isEnabled = !TwilioViewController.localVideoTrack!.isEnabled;
        self.setupRemoteVideoView()
        
        print (" ==== closeCamera")
    }
    
    // ------------------------------------------------------------------------------------------------------
    func endCall() {
        
        if(TwilioViewController.room !== nil){
            TwilioViewController.room!.disconnect()
            print (" ==== endCall")
            TwilioEmitter.emitter.sendEvent(withName: TwilioEmitter.END_CALL, body: [TwilioEmitter.END_CALL:TwilioEmitter.END_CALL]);
            TwilioViewController.isLocal=true
            self.setupRemoteVideoView()
        }
    }
    
    /* deinit {
     // We are done with camera
     if let camera = self.camera {
     camera.stopCapture()
     self.camera = nil
     }
     }
     */
    
    // ------------------------------------------------------------------------------------------------------
    //native twilio view ======================================================================
    override func viewDidAppear(_ animated: Bool) {
        print (" ==== viewDidAppear")
    }
    
    // ------------------------------------------------------------------------------------------------------
    override func viewDidLoad() {
        self.view.backgroundColor=UIColor.white
        if(TwilioViewController.camera == nil){
            self.startPreview()
        }
        print (" ==== viewDidLoad")
    }
    
    // ------------------------------------------------------------------------------------------------------
    override var prefersHomeIndicatorAutoHidden: Bool {
        print (" ==== prefersHomeIndicatorAutoHidden")
        return TwilioViewController.room != nil
    }
    
    // ------------------------------------------------------------------------------------------------------
    func setupRemoteVideoView() {
        
        if(TwilioViewController.isLocal){
            if(self.remoteView !== nil){
                if(isVisible(view: self.remoteView!)){
                    self.remoteView?.isHidden=true
                    self.remoteView!.removeFromSuperview()
                    
                }
            }
            
            
            TwilioViewController.localPlaceHolderContainer?.frame = TwilioViewController.viewRect
            TwilioViewController.localPlaceHolderContainer!.backgroundColor = UIColor.white
            TwilioViewController.localPlaceHolderContainer!.layer.cornerRadius = 15.0
            TwilioViewController.localPlaceHolderLabel.translatesAutoresizingMaskIntoConstraints = false
            TwilioViewController.localPlaceHolderLabel.text = TwilioViewController.localTextPlaceHolder
            TwilioViewController.localPlaceHolderLabel.textColor = UIColor.black
            TwilioViewController.localPlaceHolderLabel.textAlignment = .center
            TwilioViewController.localPlaceHolderLabel.numberOfLines = 0
            TwilioViewController.localPlaceHolderContainer!.addSubview(TwilioViewController.localPlaceHolderLabel)
            TwilioViewController.localPlaceHolderLabel.centerXAnchor.constraint(equalTo: TwilioViewController.localPlaceHolderContainer!.centerXAnchor).isActive = true
            TwilioViewController.localPlaceHolderLabel.centerYAnchor.constraint(equalTo: TwilioViewController.localPlaceHolderContainer!.centerYAnchor).isActive = true
            TwilioViewController.localPlaceHolderLabel.center=TwilioViewController.localPlaceHolderContainer!.center
            self.previewView.frame = TwilioViewController.viewRect
            TwilioViewController.placeHolderContainer?.isHidden=true
            TwilioViewController.imageViewPlaceHolder.isHidden=true
            if(TwilioViewController.localVideoTrack!.isEnabled){
                self.previewView.isHidden=false
                TwilioViewController.localPlaceHolderContainer?.isHidden=true
                
            }else{
                
                TwilioViewController.localPlaceHolderContainer?.isHidden=false
                self.previewView.isHidden=true
                
            }
            
        }else{
            
            if(self.remoteView !== nil){
                if(isVisible(view: self.remoteView!)){
                    self.remoteView?.isHidden=false
                    self.remoteView!.removeFromSuperview()
                    
                }
            }
            
            
            self.remoteView = VideoView(frame: CGRect.zero)
            self.remoteView!.tag = 100
            self.remoteView!.contentMode = .scaleAspectFill;
            
            self.remoteView!.frame = TwilioViewController.viewRect
            self.view.insertSubview(self.remoteView!, at: 0)
            
            self.remoteView?.sendSubviewToBack(self.previewView)
            
            //IMAGE remote placholders
            let image = UIImage(named: "default")
            TwilioViewController.imageViewPlaceHolder.image = image
            TwilioViewController.imageViewPlaceHolder.frame=TwilioViewController.viewRect
            TwilioViewController.imageViewPlaceHolder.contentMode = .scaleAspectFit;
            
            TwilioViewController.imageViewPlaceHolder.bounds = TwilioViewController.viewRect.insetBy(dx: 16.0, dy: 16.0);
            
            
            //TEXT remote placeholder ===========================================================================
            TwilioViewController.placeHolderLabel.numberOfLines = 0
            TwilioViewController.placeHolderLabel.lineBreakMode = NSLineBreakMode.byWordWrapping
            TwilioViewController.placeHolderLabel.text = TwilioViewController.textPlaceHolder
            TwilioViewController.placeHolderLabel.sizeToFit()
            TwilioViewController.placeHolderLabel.center=TwilioViewController.placeHolderContainer!.center
            TwilioViewController.placeHolderContainer!.backgroundColor = UIColor.gray
            TwilioViewController.placeHolderContainer!.addSubview(TwilioViewController.placeHolderLabel)
            TwilioViewController.placeHolderContainer!.tag = 200
            TwilioViewController.placeHolderContainer!.sendSubviewToBack(self.previewView)
            TwilioViewController.placeHolderContainer?.frame = TwilioViewController.viewRect
            TwilioViewController.placeHolderLabel = UILabel(frame: CGRectMake(0, 0,  TwilioViewController.viewRect.size.width, CGFloat.greatestFiniteMagnitude))
            
            
            //  local main breview===============================================================================
            let localFrame = CGRect(x: TwilioViewController.viewRect.width/1.62, y:TwilioViewController.viewRect.height/15, width: TwilioViewController.viewRect.height/6.5, height:TwilioViewController.viewRect.height/5.4)
            self.previewView.frame = localFrame
            self.previewView.layer.cornerRadius = 15.0
            
            //  local placeholders===============================================================================
            TwilioViewController.localPlaceHolderContainer!.frame = localFrame
            TwilioViewController.localPlaceHolderContainer!.backgroundColor = UIColor.gray
            TwilioViewController.localPlaceHolderContainer!.layer.cornerRadius = 15.0
            TwilioViewController.localPlaceHolderLabel.translatesAutoresizingMaskIntoConstraints = false
            TwilioViewController.localPlaceHolderLabel.text = TwilioViewController.localTextPlaceHolder
            TwilioViewController.localPlaceHolderLabel.textColor = UIColor.black
            TwilioViewController.localPlaceHolderLabel.textAlignment = .center
            TwilioViewController.localPlaceHolderLabel.numberOfLines = 0
            TwilioViewController.localPlaceHolderContainer!.addSubview(TwilioViewController.localPlaceHolderLabel)
            TwilioViewController.localPlaceHolderLabel.centerXAnchor.constraint(equalTo: TwilioViewController.localPlaceHolderContainer!.centerXAnchor).isActive = true
            TwilioViewController.localPlaceHolderLabel.centerYAnchor.constraint(equalTo: TwilioViewController.localPlaceHolderContainer!.centerYAnchor).isActive = true
            TwilioViewController.localPlaceHolderLabel.center=TwilioViewController.localPlaceHolderContainer!.center
            TwilioViewController.placeHolderContainer!.sendSubviewToBack(TwilioViewController.localPlaceHolderContainer!)
            self.remoteView?.sendSubviewToBack(TwilioViewController.localPlaceHolderContainer!)
            
            // change with actions
            if(TwilioViewController.localVideoTrack!.isEnabled){
                self.previewView.isHidden=false
                TwilioViewController.localPlaceHolderContainer?.isHidden=true
                
            }else{
                
                TwilioViewController.localPlaceHolderContainer?.isHidden=false
                self.previewView.isHidden=true
                
            }
            self.remoteView!.isHidden=false
            TwilioViewController.placeHolderContainer?.isHidden=true
            TwilioViewController.imageViewPlaceHolder.isHidden=true
        }
        
    }
    
    
    // ------------------------------------------------------------------------------------------------------
    func isVisible(view: UIView) -> Bool {
        func isVisible(view: UIView, inView: UIView?) -> Bool {
            guard let inView = inView else { return true }
            let viewFrame = inView.convert(view.bounds, from: view)
            if viewFrame.intersects(inView.bounds) {
                return isVisible(view: view, inView: inView.superview)
            }
            return false
        }
        return isVisible(view: view, inView: view.superview)
    }
    
    // ------------------------------------------------------------------------------------------------------
    func connectToARoom() {
        
        self.prepareLocalMedia()
        
        let connectOptions = ConnectOptions(token: TwilioViewController.accessToken!) { (builder) in
            builder.preferredAudioCodecs = [OpusCodec()]
            builder.preferredVideoCodecs =  [Vp8Codec()]
            
            builder.audioTracks = TwilioViewController.localAudioTrack != nil ? [TwilioViewController.localAudioTrack!] : [LocalAudioTrack]()
            builder.videoTracks = TwilioViewController.localVideoTrack != nil ? [TwilioViewController.localVideoTrack!] : [LocalVideoTrack]()
            builder.encodingParameters = EncodingParameters(audioBitrate:16, videoBitrate:0)
            
        }
        
        TwilioViewController.room = TwilioVideoSDK.connect(options: connectOptions, delegate: self)
    }
    
    // ------------------------------------------------------------------------------------------------------
    func disconnect(sender: AnyObject) {
        TwilioViewController.room!.disconnect()
        logMessage(messageText: "Attempting to disconnect from room \(TwilioViewController.room!.name)")
    }
    
    // ------------------------------------------------------------------------------------------------------
    func startPreview() {
        let frontCamera = CameraSource.captureDevice(position: .front)
        let backCamera = CameraSource.captureDevice(position: .back)
        
        if (frontCamera != nil || backCamera != nil) {
            
            let options = CameraSourceOptions { (builder) in
                if #available(iOS 13.0, *) {
                    // Track UIWindowScene events for the key window's scene.
                    //  disables multi-window support in the .plist (see UIApplicationSceneManifestKey).
                    builder.orientationTracker = UserInterfaceTracker(scene: UIApplication.shared.keyWindow!.windowScene!)
                }
            }
            // Preview our local camera track in the local video preview view.
            TwilioViewController.camera = CameraSource(options: options, delegate: self)
            TwilioViewController.localVideoTrack = LocalVideoTrack(source: TwilioViewController.camera!, enabled: true, name: "Camera")
            
            // Add renderer to video track for local preview
            TwilioViewController.localVideoTrack!.addRenderer(self.previewView)
            logMessage(messageText: "Video track created")
            
            let supportedFormats = CameraSource.supportedFormats(captureDevice: backCamera!)
            // var formatFound: VideoFormat?
            for format in supportedFormats {
                if let formatCasted = format as? VideoFormat {
                    if formatCasted.dimensions.height == 480 && formatCasted.dimensions.width == 640 {
                        formatCasted.frameRate = 24
                        TwilioViewController.camera!.startCapture(device: frontCamera != nil ? frontCamera! : backCamera!, format: formatCasted, completion: { (captureDevice, videoFormat, error) in
                            if let error = error {
                                self.logMessage(messageText: "Capture failed with error.\ncode = \((error as NSError).code) error = \(error.localizedDescription)")
                            } else {
                                self.previewView.shouldMirror = (captureDevice.position == .front)
                            }
                        });
                    }
                }
            }
            
        }
        else {
            self.logMessage(messageText:"No front or back capture device found!")
        }
    }
    
    // ------------------------------------------------------------------------------------------------------
    @objc func flipCamera() {
        var newDevice: AVCaptureDevice?
        
        if let camera = TwilioViewController.camera, let captureDevice = camera.device {
            if captureDevice.position == .front {
                newDevice = CameraSource.captureDevice(position: .back)
            } else {
                newDevice = CameraSource.captureDevice(position: .front)
            }
            
            if let newDevice = newDevice {
                camera.selectCaptureDevice(newDevice) { (captureDevice, videoFormat, error) in
                    if let error = error {
                        self.logMessage(messageText: "Error selecting capture device.\ncode = \((error as NSError).code) error = \(error.localizedDescription)")
                    } else {
                        self.previewView.shouldMirror = (captureDevice.position == .front)
                    }
                }
            }
        }
    }
    
    // ------------------------------------------------------------------------------------------------------
    func stopCamera() {
        if  TwilioViewController.isCameraClosed == true{
            if let source = AppScreenSource(), let track = LocalVideoTrack(source: source) {
                TwilioViewController.room?.localParticipant?.unpublishVideoTrack(track)
                print("Stop")
                TwilioViewController.isCameraClosed = false
                TwilioViewController.camera!.stopCapture()
            }
        } else {
            if let source = AppScreenSource(), let track = LocalVideoTrack(source: source) {
                TwilioViewController.room?.localParticipant?.publishVideoTrack(track)
                print("Start")
                TwilioViewController.isCameraClosed = true
                self.startPreview()
            }
        }
    }
    
    // ------------------------------------------------------------------------------------------------------
    func prepareLocalMedia() {
        
        // We will share local audio and video when we connect to the Room.
        
        // Create an audio track.
        if (TwilioViewController.localAudioTrack == nil) {
            TwilioViewController.localAudioTrack = LocalAudioTrack(options: nil, enabled: true, name: "Microphone")
            
            if (TwilioViewController.localAudioTrack == nil) {
                logMessage(messageText: "Failed to create audio track")
            }
        }
        
        // Create a video track which captures from the camera.
        if (TwilioViewController.localVideoTrack == nil) {
            self.startPreview()
        }
    }
    
    // ------------------------------------------------------------------------------------------------------
    func renderRemoteParticipant(participant : RemoteParticipant) -> Bool {
        // This example renders the first subscribed RemoteVideoTrack from the RemoteParticipant.
        let videoPublications = participant.remoteVideoTracks
        for publication in videoPublications {
            if let subscribedVideoTrack = publication.remoteTrack,
               publication.isTrackSubscribed {
                TwilioViewController.isLocal=false
                self.setupRemoteVideoView()
                subscribedVideoTrack.addRenderer(self.remoteView!)
                TwilioViewController.remoteParticipant = participant
                return true
            }
        }
        return false
    }
    
    // ------------------------------------------------------------------------------------------------------
    func logMessage(messageText: String) {
        NSLog(messageText)
    }
    
    // ------------------------------------------------------------------------------------------------------
    func renderRemoteParticipants(participants : Array<RemoteParticipant>) {
        for participant in participants {
            // Find the first renderable track.
            if participant.remoteVideoTracks.count > 0,
               renderRemoteParticipant(participant: participant) {
                break
            }
        }
    }
    
    // ------------------------------------------------------------------------------------------------------
    func cleanupRemoteParticipant() {
        if TwilioViewController.remoteParticipant != nil {
            self.remoteView?.removeFromSuperview()
            self.remoteView = nil
            TwilioViewController.remoteParticipant = nil
        }
    }
    
    // ------------------------------------------------------------------------------------------------------
    func buildParticipant(participant: Participant) -> AnyObject {
        return [TwilioEmitter.IDENTITY:participant.identity,TwilioEmitter.SID:participant.sid] as AnyObject;
    }
    
    // ------------------------------------------------------------------------------------------------------
    func buildTrack(publication: TrackPublication)-> AnyObject {
        let params =
        [TwilioEmitter.TRACK_SID:publication.trackSid,
         TwilioEmitter.TRACK_NAME: publication.trackName,
         TwilioEmitter.ENABLED: publication.isTrackEnabled,
        ] as AnyObject
        
        return params
    }
    
    // ------------------------------------------------------------------------------------------------------
    func buildParticipantTrack(participant: Participant,publication: TrackPublication)-> AnyObject {
        let participantMap = buildParticipant(participant: participant)
        let trackMap = buildTrack(publication: publication)
        
        let params =
        [TwilioEmitter.PARTICIPANT:participantMap,
         TwilioEmitter.TRACK:trackMap,
        ] as AnyObject
        
        return params
    }
}

// **********************************************************************************************************
// MARK:- RoomDelegate
extension TwilioViewController : RoomDelegate {
    func roomDidConnect(room: Room) {
        
        logMessage(messageText: "Connected to room \(room.name) as \(room.localParticipant?.identity ?? "")")
        
        let participants = room.remoteParticipants
        let localParticipant = room.localParticipant
        var participantsArray = [AnyObject]()
        
        for remoteParticipant in room.remoteParticipants {
            remoteParticipant.delegate = self
        }
        for participant in participants {
            participantsArray.append(buildParticipant(participant: participant))
        }
        participantsArray.append(buildParticipant(participant: localParticipant!))
        let params =
        [TwilioEmitter.ROOM_NAME:room.name,
         TwilioEmitter.ROOM_SID:room.sid,
         TwilioEmitter.PARTICIPANTS:participantsArray,
         TwilioEmitter.LOCAL_PARTICIPANT:buildParticipant(participant: localParticipant!),
        ] as [String : Any]
        
        TwilioEmitter.emitter.sendEvent(withName: "onRoomDidConnect", body:params);
    }
    
    func roomDidDisconnect(room: Room, error: Error?) {
        logMessage(messageText: "Disconnected from room \(room.name), error = \(String(describing: error))")
        
        let params =
        [TwilioEmitter.ROOM_NAME:room.name,
         TwilioEmitter.ROOM_SID:room.sid,
        ] as [String : Any]
        TwilioEmitter.emitter.sendEvent(withName: TwilioEmitter.ON_DISCONNECTED, body:params);
        
        self.cleanupRemoteParticipant()
        TwilioViewController.room = nil
        TwilioViewController.isLocal=true
        self.setupRemoteVideoView()
    }
    
    func roomDidFailToConnect(room: Room, error: Error) {
        logMessage(messageText: "Failed to connect to room with error = \(String(describing: error))")
        TwilioViewController.room = nil
        
        let params =
        [
            TwilioEmitter.ERROR:error.localizedDescription,
            TwilioEmitter.ROOM_NAME:room.name,
            TwilioEmitter.ROOM_SID:room.sid,
        ] as [String : Any]
        TwilioEmitter.emitter.sendEvent(withName: TwilioEmitter.ON_CONNECT_FAILURE, body:params);
    }
    
    func roomIsReconnecting(room: Room, error: Error) {
        logMessage(messageText: "Reconnecting to room \(room.name), error = \(String(describing: error))")
        
    }
    
    func roomDidReconnect(room: Room) {
        logMessage(messageText: "Reconnected to room \(room.name)")
        let params =
        [TwilioEmitter.ROOM_NAME:room.name,
         TwilioEmitter.ROOM_SID:room.sid,
        ] as [String : Any]
        TwilioEmitter.emitter.sendEvent(withName: TwilioEmitter.ON_RE_CONNECTED, body:params);
        
    }
    
    func participantDidConnect(room: Room, participant: RemoteParticipant) {
        // Listen for events from all Participants to decide which RemoteVideoTrack to render.
        participant.delegate = self
        let params =
        [TwilioEmitter.ROOM_NAME:room.name,
         TwilioEmitter.ROOM_SID:room.sid,
         TwilioEmitter.PARTICIPANT_SID:participant.sid as Any,
        ] as [String : Any]
        TwilioEmitter.emitter.sendEvent(withName: TwilioEmitter.ON_PARTICIPANT_CONNECTED, body:params);
        
        
        logMessage(messageText: "Participant \(participant.identity) connected with \(participant.remoteAudioTracks.count) audio and \(participant.remoteVideoTracks.count) video tracks")
    }
    
    func participantDidDisconnect(room: Room, participant: RemoteParticipant) {
        logMessage(messageText: "Room \(room.name), Participant \(participant.identity) disconnected")
        
        let params =
        [TwilioEmitter.ROOM_NAME:room.name,
         TwilioEmitter.ROOM_SID:room.sid,
         TwilioEmitter.PARTICIPANT_SID:participant.sid as Any,
        ] as [String : Any]
        TwilioEmitter.emitter.sendEvent(withName: TwilioEmitter.ON_PARTICIPANT_DISCONNECTED, body:params);
        TwilioViewController.isLocal=true
        self.setupRemoteVideoView()
        
    }
}


// **********************************************************************************************************
// MARK:- RemoteParticipantDelegate
extension TwilioViewController : RemoteParticipantDelegate {
    
    func remoteParticipantDidPublishVideoTrack(participant: RemoteParticipant, publication: RemoteVideoTrackPublication) {
        // Remote Participant has offered to share the video Track.
        
        logMessage(messageText: "Participant \(participant.identity) published \(publication.trackName) video track")
    }
    
    func remoteParticipantDidUnpublishVideoTrack(participant: RemoteParticipant, publication: RemoteVideoTrackPublication) {
        // Remote Participant has stopped sharing the video Track.
        
        logMessage(messageText: "Participant \(participant.identity) unpublished \(publication.trackName) video track")
    }
    
    func remoteParticipantDidPublishAudioTrack(participant: RemoteParticipant, publication: RemoteAudioTrackPublication) {
        // Remote Participant has offered to share the audio Track.
        
        logMessage(messageText: "Participant \(participant.identity) published \(publication.trackName) audio track")
    }
    
    func remoteParticipantDidUnpublishAudioTrack(participant: RemoteParticipant, publication: RemoteAudioTrackPublication) {
        // Remote Participant has stopped sharing the audio Track.
        
        logMessage(messageText: "Participant \(participant.identity) unpublished \(publication.trackName) audio track")
    }
    
    func didSubscribeToDataTrack(dataTrack: RemoteDataTrack, publication: RemoteDataTrackPublication, participant: RemoteParticipant) {
        
        let params = buildParticipantTrack(participant: participant, publication: publication)
        
        TwilioEmitter.emitter.sendEvent(withName: TwilioEmitter.ON_PARTICIPANT_ADDED_DATA_TRACK, body:params);
        
    }
    
    func didUnsubscribeFromDataTrack(dataTrack: RemoteDataTrack, publication: RemoteDataTrackPublication, participant: RemoteParticipant) {
        
        let params = buildParticipantTrack(participant: participant, publication: publication)
        
        TwilioEmitter.emitter.sendEvent(withName: TwilioEmitter.ON_PARTICIPANT_REMOVED_DATA_TRACK, body:params);
        
    }
    func didSubscribeToVideoTrack(videoTrack: RemoteVideoTrack, publication: RemoteVideoTrackPublication, participant: RemoteParticipant) {
        // The LocalParticipant is subscribed to the RemoteParticipant's video Track. Frames will begin to arrive now.
        let params = buildParticipantTrack(participant: participant, publication: publication)
        
        TwilioEmitter.emitter.sendEvent(withName: TwilioEmitter.ON_PARTICIPANT_ADDED_VIDEO_TRACK, body:params);
        
        
        logMessage(messageText: "Subscribed to \(publication.trackName) video track for Participant \(participant.identity)")
        
        if (TwilioViewController.remoteParticipant == nil) {
            _ = renderRemoteParticipant(participant: participant)
        }
    }
    
    func didUnsubscribeFromVideoTrack(videoTrack: RemoteVideoTrack, publication: RemoteVideoTrackPublication, participant: RemoteParticipant) {
        // We are unsubscribed from the remote Participant's video Track. We will no longer receive the
        // remote Participant's video.
        
        let params = buildParticipantTrack(participant: participant, publication: publication)
        
        TwilioEmitter.emitter.sendEvent(withName: TwilioEmitter.ON_PARTICIPANT_REMOVED_VIDEO_TRACK, body:params);
        
        logMessage(messageText: "Unsubscribed from \(publication.trackName) video track for Participant \(participant.identity)")
        
        if TwilioViewController.remoteParticipant == participant {
            cleanupRemoteParticipant()
            
            // Find another Participant video to render, if possible.
            if var remainingParticipants = TwilioViewController.room?.remoteParticipants,
               let index = remainingParticipants.firstIndex(of: participant) {
                remainingParticipants.remove(at: index)
                renderRemoteParticipants(participants: remainingParticipants)
            }
        }
    }
    
    func didSubscribeToAudioTrack(audioTrack: RemoteAudioTrack, publication: RemoteAudioTrackPublication, participant: RemoteParticipant) {
        // We are subscribed to the remote Participant's audio Track. We will start receiving the
        // remote Participant's audio now.
        let params = buildParticipantTrack(participant: participant, publication: publication)
        
        TwilioEmitter.emitter.sendEvent(withName: TwilioEmitter.ON_PARTICIPANT_ADDED_AUDIO_TRACK, body:params);
        
        logMessage(messageText: "Subscribed to \(publication.trackName) audio track for Participant \(participant.identity)")
    }
    
    func didUnsubscribeFromAudioTrack(audioTrack: RemoteAudioTrack, publication: RemoteAudioTrackPublication, participant: RemoteParticipant) {
        // We are unsubscribed from the remote Participant's audio Track. We will no longer receive the
        // remote Participant's audio.
        let params = buildParticipantTrack(participant: participant, publication: publication)
        TwilioEmitter.emitter.sendEvent(withName: TwilioEmitter.ON_PARTICIPANT_REMOVED_AUDIO_TRACK, body:params);
        
        logMessage(messageText: "Unsubscribed from \(publication.trackName) audio track for Participant \(participant.identity)")
    }
    
    func remoteParticipantDidEnableVideoTrack(participant: RemoteParticipant, publication: RemoteVideoTrackPublication) {
        let params = buildParticipantTrack(participant: participant, publication: publication)
        TwilioEmitter.emitter.sendEvent(withName: TwilioEmitter.ON_PARTICIPANT_ENABLED_VIDEO_TRACK, body:params);
        
        
        self.remoteView?.isHidden=false
        TwilioViewController.placeHolderContainer?.isHidden=true
        TwilioViewController.imageViewPlaceHolder.isHidden=true
        
        if(isVisible(view: TwilioViewController.placeHolderContainer!)){
            TwilioViewController.placeHolderContainer?.removeFromSuperview()
        }
        if(isVisible(view: TwilioViewController.imageViewPlaceHolder)){
            TwilioViewController.imageViewPlaceHolder.removeFromSuperview()
        }
        self.view.insertSubview(self.remoteView!, at: 0)
        
        
        logMessage(messageText: "Participant \(participant.identity) enabled \(publication.trackName) video track")
    }
    
    func remoteParticipantDidDisableVideoTrack(participant: RemoteParticipant, publication: RemoteVideoTrackPublication) {
        let params = buildParticipantTrack(participant: participant, publication: publication)
        TwilioEmitter.emitter.sendEvent(withName: TwilioEmitter.ON_PARTICIPANT_DISABLED_VIDEO_TRACK, body:params);
        self.remoteView?.isHidden=true
        if(TwilioViewController.imgUriPlaceHolder != nil){
            TwilioViewController.placeHolderContainer?.isHidden=true
            TwilioViewController.imageViewPlaceHolder.isHidden=false
            TwilioViewController.imageViewPlaceHolder.load(url: URL(string:TwilioViewController.imgUriPlaceHolder! )!)
            self.remoteView?.removeFromSuperview()
            self.view.insertSubview(TwilioViewController.imageViewPlaceHolder, at: 0)
            TwilioViewController.placeHolderContainer!.removeFromSuperview()
            
        }else{
            TwilioViewController.placeHolderContainer?.isHidden=false
            TwilioViewController.imageViewPlaceHolder.isHidden=true
            self.view.insertSubview(TwilioViewController.placeHolderContainer!, at: 0)
            TwilioViewController.imageViewPlaceHolder.removeFromSuperview()
            
            self.remoteView?.removeFromSuperview()
        }
        
        
        logMessage(messageText: "Participant \(participant.identity) disabled \(publication.trackName) video track")
    }
    
    func remoteParticipantDidEnableAudioTrack(participant: RemoteParticipant, publication: RemoteAudioTrackPublication) {
        
        let params = buildParticipantTrack(participant: participant, publication: publication)
        TwilioEmitter.emitter.sendEvent(withName: TwilioEmitter.ON_PARTICIPANT_ENABLED_AUDIO_TRACK, body:params);
        
        logMessage(messageText: "Participant \(participant.identity) enabled \(publication.trackName) audio track")
    }
    
    func remoteParticipantDidDisableAudioTrack(participant: RemoteParticipant, publication: RemoteAudioTrackPublication) {
        
        let params = buildParticipantTrack(participant: participant, publication: publication)
        TwilioEmitter.emitter.sendEvent(withName: TwilioEmitter.ON_PARTICIPANT_DISABLED_AUDIO_TRACK, body:params);
        
        logMessage(messageText: "Participant \(participant.identity) disabled \(publication.trackName) audio track")
    }
    
    func didFailToSubscribeToAudioTrack(publication: RemoteAudioTrackPublication, error: Error, participant: RemoteParticipant) {
        logMessage(messageText: "FailedToSubscribe \(publication.trackName) audio track, error = \(String(describing: error))")
    }
    
    func didFailToSubscribeToVideoTrack(publication: RemoteVideoTrackPublication, error: Error, participant: RemoteParticipant) {
        logMessage(messageText: "FailedToSubscribe \(publication.trackName) video track, error = \(String(describing: error))")
    }
    
    func remoteParticipantNetworkQualityLevelDidChange(participant: RemoteParticipant, networkQualityLevel: NetworkQualityLevel) {
        let params = buildParticipant(participant: participant)
        let params2 =
        [TwilioEmitter.IS_LOCAL_USER:true,
         TwilioEmitter.QUALITY:networkQualityLevel.rawValue - 1,
        ] as [String : Any]
        params.add(params2)
        
        TwilioEmitter.emitter.sendEvent(withName: TwilioEmitter.ON_NETWORK_QUALITY_LEVELS_CHANGED, body:params);
        
    }
}

// **********************************************************************************************************
// MARK:- CameraSourceDelegate
extension TwilioViewController : CameraSourceDelegate {
    func cameraSourceDidFail(source: CameraSource, error: Error) {
        logMessage(messageText: "Camera source failed with error: \(error.localizedDescription)")
    }
}

// **********************************************************************************************************
// MARK:- UIImageView
extension UIImageView {
    func load(url: URL) {
        DispatchQueue.global().async { [weak self] in
            if let data = try? Data(contentsOf: url) {
                if let image = UIImage(data: data) {
                    DispatchQueue.main.async {
                        self?.image = image
                    }
                }
            }
        }
    }
    var intrinsicScaledContentSize: CGSize? {
        switch contentMode {
        case .scaleAspectFit:
            // aspect fit
            if let image = self.image {
                let imageWidth = image.size.width
                let imageHeight = image.size.height
                let viewWidth = self.frame.size.width
                
                let ratio = viewWidth/imageWidth
                let scaledHeight = imageHeight * ratio
                
                return CGSize(width: viewWidth, height: scaledHeight)
            }
        case .scaleAspectFill:
            // aspect fill
            if let image = self.image {
                let imageWidth = image.size.width
                let imageHeight = image.size.height
                let viewHeight = self.frame.size.width
                
                let ratio = viewHeight/imageHeight
                let scaledWidth = imageWidth * ratio
                
                return CGSize(width: scaledWidth, height: imageHeight)
            }
            
        default: return self.bounds.size
        }
        return nil
        
    }
}

// **********************************************************************************************************
// CustomView
class CustomView : UIView {
    var s: String?
    var i: Int?
    init(rect: CGRect) {
        super.init(frame: rect)
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
}
