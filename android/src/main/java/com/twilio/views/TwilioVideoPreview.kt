/**
 * Component for Twilio Video local views.
 *
 *
 * Authors:
 * Jonathan Chang <slycoder></slycoder>@gmail.com>
 */
package com.twilio.views

import com.facebook.react.uimanager.ThemedReactContext
import com.twilio.views.CustomTwilioVideoView.Companion.registerThumbnailVideoView

class TwilioVideoPreview(themedReactContext: ThemedReactContext?) :
    RNVideoViewGroup(themedReactContext) {
    init {
        registerThumbnailVideoView(surfaceViewRenderer)
    }

    fun applyZOrder(applyZOrder: Boolean) {
        surfaceViewRenderer!!.applyZOrder(applyZOrder)
    }

    companion object {
        private const val TAG = "TwilioVideoPreview"
    }
}
