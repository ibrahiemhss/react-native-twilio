package com.twillio.src

import android.os.Bundle
import com.facebook.react.ReactActivity
import com.facebook.react.modules.core.PermissionAwareActivity



class NativeViewActivity : ReactActivity() {

  var view: NativeView? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val permissionAwareActivity =this as PermissionAwareActivity?
    view = NativeView(this.applicationContext, false,this, permissionAwareActivity!!)
    setContentView(view)
  }
}
