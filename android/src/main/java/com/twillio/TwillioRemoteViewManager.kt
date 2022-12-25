package com.twillio


import android.util.Log
import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.twillio.src2.CustomTwilioVideoView
import com.twillio.src2.RNVideoViewGroup
import com.twillio.src2.TwilioRemotePreview
import tvi.webrtc.RendererCommon

class TwillioRemoteViewManager : SimpleViewManager<TwilioRemotePreview>() {

  val REACT_CLASS = "TwillioRemoteView"
  var myTrackSid: String? = ""

  override fun getName(): String {
    return REACT_CLASS
  }
  override fun createViewInstance(reactContext: ThemedReactContext): TwilioRemotePreview {
    return   TwilioRemotePreview(reactContext, myTrackSid)
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
}
