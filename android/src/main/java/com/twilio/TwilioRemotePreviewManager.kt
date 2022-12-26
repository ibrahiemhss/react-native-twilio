/**
 * Component for Twilio Video participant views.
 *
 *
 * Authors:
 * Jonathan Chang <slycoder></slycoder>@gmail.com>
 */
package com.twilio

import android.util.Log
import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.annotations.ReactProp
import tvi.webrtc.RendererCommon
import com.facebook.react.uimanager.ThemedReactContext
import com.twilio.views.CustomTwilioVideoView
import com.twilio.views.RNVideoViewGroup
import com.twilio.views.TwilioRemotePreview

class TwilioRemotePreviewManager : SimpleViewManager<TwilioRemotePreview>() {
    var myTrackSid: String? = ""
    override fun getName(): String {
        return REACT_CLASS
    }

    @ReactProp(name = "scaleType")
    fun setScaleType(view: TwilioRemotePreview, scaleType: String?) {
        if (scaleType == "fit") {
            view.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
        } else {
            view.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
        }
    }

    @ReactProp(name = "trackSid")
    fun setTrackId(view: TwilioRemotePreview, trackSid: String?) {
        Log.i("CustomTwilioVideoView", "Initialize Twilio REMOTE")
        Log.i("CustomTwilioVideoView", trackSid!!)
        myTrackSid = trackSid
      CustomTwilioVideoView.registerPrimaryVideoView(view.surfaceViewRenderer, trackSid)
    }

    @ReactProp(name = "applyZOrder", defaultBoolean = false)
    fun setApplyZOrder(view: TwilioRemotePreview, applyZOrder: Boolean) {
        view.applyZOrder(applyZOrder)
    }

    override fun createViewInstance(reactContext: ThemedReactContext): TwilioRemotePreview {
        return TwilioRemotePreview(reactContext, myTrackSid)
    }

    override fun getExportedCustomBubblingEventTypeConstants(): MutableMap<String, Any>? {
        return MapBuilder.builder<String, Any>()
            .put(
              RNVideoViewGroup.Events.ON_FRAME_DIMENSIONS_CHANGED,
                MapBuilder.of(
                    "phasedRegistrationNames",
                    MapBuilder.of("bubbled", RNVideoViewGroup.Events.ON_FRAME_DIMENSIONS_CHANGED)
                )
            )
            .build()
    }

    companion object {
        const val REACT_CLASS = "RNTwilioRemotePreview"
    }
}
