/***
 * Based on code from https://www.assembla.com/wiki/show/rcpmediaplayer
 */

package com.bdaum.zoom.video.internal.views;

import org.eclipse.swt.graphics.ImageData;

public interface ThreadUINotifier {

    void applyPause();

    void applyStop();

    void updateFrame(ImageData data, long position, long seek);

	void showError(String msg);

}