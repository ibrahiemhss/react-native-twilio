package com.twillio

import android.graphics.Color
import android.view.View
import com.facebook.react.modules.core.PermissionAwareActivity
import com.facebook.react.modules.core.PermissionListener
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.twillio.src.CommonNativeView

class TwillioViewManager : SimpleViewManager<View>() {
  override fun getName() = "TwillioView"

  override fun createViewInstance(reactContext: ThemedReactContext): View {
      val permissionAwareActivity = reactContext.currentActivity as PermissionAwareActivity?
      return CommonNativeView(reactContext, true,reactContext.currentActivity!!,permissionAwareActivity!!)

  }
  @ReactProp(name = "color")
  fun setColor(view: View, color: String) {
    view.setBackgroundColor(Color.parseColor(color))
  }
}
