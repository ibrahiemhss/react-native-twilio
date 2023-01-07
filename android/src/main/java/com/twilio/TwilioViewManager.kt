package com.twilio

import android.graphics.Color
import android.util.Log
import android.view.View
import com.facebook.react.modules.core.PermissionAwareActivity
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.twilio.src.NativeView

class TwilioViewManager : SimpleViewManager<View>() {
  override fun getName() = "TwilioView"

  var myTrackSid: String? = ""
  var mRoomName: String? = null

  override fun createViewInstance(reactContext: ThemedReactContext): NativeView {
    val permissionAwareActivity = reactContext.currentActivity as PermissionAwareActivity?
    return reactContext.currentActivity?.let {
      NativeView(reactContext, true,
        it,permissionAwareActivity!!)
    }!!

  }

  @ReactProp(name = "accessToken")
  fun setAccessToken(view: NativeView,accessToken: String?) {
    if(view!=null){
      if(accessToken!=null){
        if(accessToken.isNotEmpty()){
          view.setAccessToken(accessToken)
          //TODO ======= test connect
          if(mRoomName!=null){
            if(mRoomName!!.isNotEmpty()){
              view.connectToRoom(mRoomName!!)
            }
          }
        }
      }
    }
  }
  /* @ReactProp(name = "disconnect")
   fun disconnect(view: NativeView) {
     view.setDisconnectAction()
   }
 */
  @ReactProp(name = "roomName")
  fun connectRom(view: NativeView,roomName: String?) {
    if(roomName!=null){
      if(roomName.isNotEmpty()){
        view.connectToRoom(roomName)
      }
    }
  }
}
