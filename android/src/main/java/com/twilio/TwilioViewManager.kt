package com.twilio

import android.util.Log
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.PermissionAwareActivity
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.twilio.src.NativeView


class TwilioViewManager (reactApplicationContext: ReactApplicationContext) : SimpleViewManager<NativeView>() {
  override fun getName() = "TwilioView"
  private var mReactApplicationContext: ReactApplicationContext? = null

  var TAG="NativeViewTwilio"
  init {
    mReactApplicationContext=reactApplicationContext
  }
  override fun createViewInstance(reactContext: ThemedReactContext): NativeView {
    val permissionAwareActivity = reactContext.currentActivity as PermissionAwareActivity?
    return NativeView(reactContext, mReactApplicationContext!!, reactContext.currentActivity!!, permissionAwareActivity!!)

  }

  override fun receiveCommand(root: NativeView, commandId: String?, args: ReadableArray?) {
    if(commandId != null && args != null){
      Log.d("NativeViewTwilio", "receiveCommand commandId ${commandId}")
      when(commandId){
        "connect" -> {
          val argCount = args.size() ?: 0
          if (argCount > 0) {
            if (args.getType(args.size() - 1) === ReadableType.Map) {
              val map = args.getMap(args.size() - 1)
              Log.d("NativeViewTwilio", "connectToRoom map=${map}")
              if (map.hasKey("roomName")) {
                root.connectToRoom(map);
              }
            }
          }
        }
        "switchCamera" -> root.switchCamera();
        "mute" -> root.mute();
        "closeCamera" -> root.enableVideo();
        "endCall" -> root.disconnect();
        else -> {
          println("default");
        }
      }
    }
  }
}
