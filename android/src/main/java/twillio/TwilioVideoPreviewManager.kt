/**
 * Component for Twilio Video local views.
 *
 *
 * Authors:
 * Jonathan Chang <slycoder></slycoder>@gmail.com>
 */
package com.twilio

import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.twilio.views.RNVideoViewGroup
import com.twilio.views.TwilioVideoPreview
import tvi.webrtc.RendererCommon

class TwilioVideoPreviewManager : SimpleViewManager<TwilioVideoPreview>() {
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

    override fun getExportedCustomDirectEventTypeConstants(): MutableMap<String, Map<String, String>>? {
        return MapBuilder.of<String, Map<String, String>>(
          RNVideoViewGroup.Events.ON_FRAME_DIMENSIONS_CHANGED,
            MapBuilder.of(
                "registrationName",
              RNVideoViewGroup.Events.ON_FRAME_DIMENSIONS_CHANGED
            )
        )
    }

    override fun createViewInstance(reactContext: ThemedReactContext): TwilioVideoPreview {
        return TwilioVideoPreview(reactContext)
    }

    companion object {
        const val REACT_CLASS = "RCTTWLocalVideoView"
    }
}
