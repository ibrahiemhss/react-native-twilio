package com.twilio

import android.graphics.Color
import android.view.View
import com.facebook.react.modules.core.PermissionAwareActivity
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.twilio.src.NativeView

class TwilioViewManager : SimpleViewManager<View>() {
  override fun getName() = "TwilioView"

  override fun createViewInstance(reactContext: ThemedReactContext): View {
    val permissionAwareActivity = reactContext.currentActivity as PermissionAwareActivity?
    return NativeView(reactContext, true,reactContext.currentActivity!!,permissionAwareActivity!!)

  }
  @ReactProp(name = "color")
  fun setColor(view: View, color: String) {
    view.setBackgroundColor(Color.parseColor(color))
  }
}
