/**
 * Wrapper component for the Twilio Video View to facilitate easier layout.
 *
 *
 * Author:
 * Jonathan Chang <slycoder></slycoder>@gmail.com>
 */
package com.twilio.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import tvi.webrtc.RendererCommon.ScalingType
import tvi.webrtc.RendererCommon
import com.facebook.react.uimanager.events.RCTEventEmitter
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.ThemedReactContext
import com.twilio.video.VideoScaleType
import tvi.webrtc.RendererCommon.RendererEvents
import com.facebook.react.bridge.WritableNativeMap
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

open class RNVideoViewGroup : ViewGroup {
    constructor(themedReactContext: ThemedReactContext?) : super(themedReactContext) {
      eventEmitter = themedReactContext!!.getJSModule(RCTEventEmitter::class.java)
      surfaceViewRenderer = PatchedVideoView(themedReactContext)
      surfaceViewRenderer!!.videoScaleType = VideoScaleType.ASPECT_FILL
      addView(surfaceViewRenderer)
      surfaceViewRenderer!!.setListener(
        object : RendererEvents {
          override fun onFirstFrameRendered() {}
          override fun onFrameResolutionChanged(vw: Int, vh: Int, rotation: Int) {
            synchronized(layoutSync) {
              if (rotation == 90 || rotation == 270) {
                videoHeight = vw
                videoWidth = vh
              } else {
                videoHeight = vh
                videoWidth = vw
              }
              forceLayout()
              val event: WritableMap = WritableNativeMap()
              event.putInt("height", vh)
              event.putInt("width", vw)
              event.putInt("rotation", rotation)
              pushEvent(this@RNVideoViewGroup, Events.ON_FRAME_DIMENSIONS_CHANGED, event)
            }
          }
        }
      )

    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}

    var surfaceViewRenderer: PatchedVideoView? = null
        private set
    private var videoWidth = 0
    private var videoHeight = 0
    private val layoutSync = Any()
    private var scalingType = ScalingType.SCALE_ASPECT_FILL
    private var eventEmitter: RCTEventEmitter? = null

    @Retention(RetentionPolicy.SOURCE)
    annotation class Events {
        companion object {
            var ON_FRAME_DIMENSIONS_CHANGED = "onFrameDimensionsChanged"
        }
    }

    fun pushEvent(view: View, name: String?, data: WritableMap?) {
        eventEmitter!!.receiveEvent(view.id, name, data)
    }

    fun setScalingType(scalingType: ScalingType) {
        this.scalingType = scalingType
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var l = l
        var t = t
        var r = r
        var b = b
        val height = b - t
        val width = r - l
        if (height == 0 || width == 0) {
            b = 0
            r = b
            t = r
            l = t
        } else {
            var videoHeight: Int
            var videoWidth: Int
            synchronized(layoutSync) {
                videoHeight = this.videoHeight
                videoWidth = this.videoWidth
            }
            if (videoHeight == 0 || videoWidth == 0) {
                // These are Twilio defaults.
                videoHeight = 480
                videoWidth = 640
            }
            val displaySize = RendererCommon.getDisplaySize(
                scalingType,
                videoWidth / videoHeight.toFloat(),
                width,
                height
            )
            l = (width - displaySize.x) / 2
            t = (height - displaySize.y) / 2
            r = l + displaySize.x
            b = t + displaySize.y
        }
        surfaceViewRenderer!!.layout(l, t, r, b)
    }
}
