package com.twillio

import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.facebook.react.modules.core.PermissionAwareActivity
import com.facebook.react.modules.core.PermissionListener
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.twilio.video.Room
import com.twillio.src.NativeView
import tvi.webrtc.RendererCommon

class TwillioViewManager : SimpleViewManager<NativeView>() {
  override fun getName() = "TwillioView"
  var myTrackSid: String? = ""
  var myAccessToken: String? = ""
  override fun createViewInstance(reactContext: ThemedReactContext): NativeView {
      val permissionAwareActivity = reactContext.currentActivity as PermissionAwareActivity?
      return NativeView(reactContext, myAccessToken!!,true,reactContext.currentActivity!!,permissionAwareActivity!!)

  }

  @ReactProp(name = "accessToken")
  fun setAccessToken(view: View, accessToken: String) {
    Log.i("myAccessToken", accessToken)
    myAccessToken = accessToken
  }
  @ReactProp(name = "color")
  fun setColor(view: View, color: String) {
    view.setBackgroundColor(Color.parseColor(color))
  }

  @ReactProp(name = "disconnect")
  fun disconnect(view: NativeView) {
    view.setDisconnectAction()
  }

  @ReactProp(name = "roomName")
  fun connectRom(view: NativeView,roomName: String) {
    view.connectRom(roomName)
  }

  @ReactProp(name = "trackSid")
  fun setTrackId(view: NativeView,trackSid: String) {
    Log.i("CustomTwilioVideoView", "Initialize Twilio REMOTE")
    Log.i("CustomTwilioVideoView", trackSid)
    myTrackSid = trackSid
    view.registerTrackIdVideoView(trackSid)
  }
}
