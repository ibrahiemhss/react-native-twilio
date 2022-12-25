/**
 * Component for Twilio Video local views.
 * <p>
 * Authors:
 * Jonathan Chang <slycoder@gmail.com>
 */

package com.twillio.src2;
import com.facebook.react.uimanager.ThemedReactContext;

public class TwilioVideoPreview extends RNVideoViewGroup {

    private static final String TAG = "TwilioVideoPreview";

    public TwilioVideoPreview(ThemedReactContext themedReactContext) {
        super(themedReactContext);
        CustomTwilioVideoView.registerThumbnailVideoView(this.getSurfaceViewRenderer());
    }

    public void applyZOrder(boolean applyZOrder) {
        this.getSurfaceViewRenderer().applyZOrder(applyZOrder);
    }
}
