/**
 * Component for Twilio Video participant views.
 *
 *
 * Authors:
 * Jonathan Chang <slycoder></slycoder>@gmail.com>
 */
package com.twilio.views

import android.util.Log
import com.facebook.react.uimanager.ThemedReactContext
import com.twilio.views.CustomTwilioVideoView.Companion.registerPrimaryVideoView

class TwilioRemotePreview(context: ThemedReactContext?, trackSid: String?) :
    RNVideoViewGroup(context) {
    init {
        Log.i("CustomTwilioVideoView", "Remote Prview Construct")
        Log.i("CustomTwilioVideoView", trackSid!!)
        registerPrimaryVideoView(surfaceViewRenderer, trackSid)
    }

    fun applyZOrder(applyZOrder: Boolean) {
        surfaceViewRenderer!!.applyZOrder(applyZOrder)
    }

    companion object {
        private const val TAG = "TwilioRemotePreview"
    }
}
