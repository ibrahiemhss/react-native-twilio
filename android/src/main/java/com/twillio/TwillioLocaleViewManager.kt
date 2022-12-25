package com.twillio


import android.view.View
import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.twillio.src2.RNVideoViewGroup
import com.twillio.src2.TwilioVideoPreview
import tvi.webrtc.RendererCommon

class TwillioLocaleViewManager : SimpleViewManager<View>() {

  override fun createViewInstance(reactContext: ThemedReactContext): View {
    return TwilioVideoPreview(reactContext)

  }

  val REACT_CLASS = "TwillioLocaleView"

  override fun getName(): String {
    return REACT_CLASS
  }

  @ReactProp(name = "scaleType")
  fun setScaleType(view: TwilioVideoPreview, scaleType: String?) {
    if (scaleType == "fit") {
      view.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
    } else {
      view.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
    }
  }

  @ReactProp(name = "applyZOrder", defaultBoolean = true)
  fun setApplyZOrder(view: TwilioVideoPreview, applyZOrder: Boolean) {
    view.applyZOrder(applyZOrder)
  }

  override fun getExportedCustomDirectEventTypeConstants(): MutableMap<String, Any>? {
    return MapBuilder.of(
      RNVideoViewGroup.Events.ON_FRAME_DIMENSIONS_CHANGED,
      MapBuilder.of("registrationName", RNVideoViewGroup.Events.ON_FRAME_DIMENSIONS_CHANGED)
    )
  }

  protected fun createViewInstance(reactContext: ThemedReactContext?): TwilioVideoPreview? {
    return TwilioVideoPreview(reactContext)
  }
}
