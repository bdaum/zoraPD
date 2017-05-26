/***
 * Based on code from https://www.assembla.com/wiki/show/rcpmediaplayer
 */

package com.bdaum.zoom.video.internal.views;

import java.io.File;
import java.net.URI;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;

import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallback;

import com.bdaum.zoom.core.Constants;
import com.sun.jna.Memory;

public class PlayingThread extends Thread {
	public class TestRenderCallback implements RenderCallback {
		private int[] rgbBuffer;
		private ImageData data;

		public void display(DirectMediaPlayer mediaPlayer,
				Memory[] nativeBuffer, BufferFormat bufferFormat) {

			int w = bufferFormat.getWidth();
			int h = bufferFormat.getHeight();
			if (rgbBuffer == null)
				rgbBuffer = new int[w * h];
			nativeBuffer[0].getByteBuffer(0L, nativeBuffer[0].size())
					.asIntBuffer().get(rgbBuffer, 0, h * w);
			if (data == null)
				data = new ImageData(w, h, 24, new PaletteData(0xFF0000,
						0xFF00, 0xFF));
			data.setPixels(0, 0, w * h, rgbBuffer, 0);
			long length = mediaPlayer.getLength();
			float position = mediaPlayer.getPosition();
			currentPosition = (long) ((double) position * length);
			if (position >= 1f) {
				pause = true;
				notifier.applyPause();
			} else
				notifier.updateFrame(data, currentPosition, 0);
		}

	}

	public class TestBufferFormatCallback implements BufferFormatCallback {

		public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
			return new BufferFormat("RV32", sourceWidth, sourceHeight, //$NON-NLS-1$
					new int[] { sourceWidth * 4 }, new int[] { sourceHeight });
		}

	}


	private final ThreadUINotifier notifier;

	private volatile boolean pause = false;
	private volatile long currentPosition;

	private final URI uri;

	private MediaPlayerFactory factory;

	private DirectMediaPlayer mediaPlayer;

	public PlayingThread(URI uri, MediaPlayerFactory factory, ThreadUINotifier notifier) {
		this.uri = uri;
		this.factory = factory;
		this.notifier = notifier;
	}

	public void stopPlaying() {
		closeResources();
	}

	public boolean isPaused() {
		return pause;
	}

	public void pause() {
		if (!this.pause) {
			this.pause = true;
			mediaPlayer.setPause(true);
		}
	}

	public void resumePlaying() {
		if (pause) {
			pause = false;
			mediaPlayer.setPause(false);
		}
	}

	public void seek(long milli) {
		long length = mediaPlayer.getLength();
		double pos = (double) milli / length;
		mediaPlayer.setPosition((float) pos);
	}

	public long getDuration() {
		return mediaPlayer.getLength();
	}

	public long getCurrentPosition() {
		return currentPosition;
	}

	public boolean hasAudio() {
		return mediaPlayer.getAudioTrackCount() > 0;
	}

	public boolean hasVideo() {
		return mediaPlayer.getVideoTrackCount() > 0;
	}

	public synchronized boolean startVideoThread() {
		if (!init())
			return false;
		start();
		return true;
	}

	private boolean init() {
		try {
			if (uri == null)
				return false;
			mediaPlayer = factory.newDirectMediaPlayer(
					new TestBufferFormatCallback(), new TestRenderCallback());
			return true;
		} catch (RuntimeException ex) {
			notifier.showError(NLS.bind(
					Messages.VlcPlayingThread_error_initializing, uri, ex));
		}
		return false;
	}

	@Override
	public void run() {
		try {
			if (Constants.FILESCHEME.equals(uri.getScheme()))
				mediaPlayer.playMedia(new File(uri).getAbsolutePath());
			else
				mediaPlayer.playMedia(uri.toString());
		} catch (Exception ex) {
			notifier.showError(NLS.bind(
					Messages.VlcPlayingThread_error_playing, uri, ex));
		}
	}

	private void closeResources() {
		mediaPlayer.release();
	}

	public void setVolume(int loudness) {
		mediaPlayer.setVolume(loudness);
	}

	public int getVolume() {
		return mediaPlayer.getVolume();
	}

}