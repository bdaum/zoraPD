package com.bdaum.zoom.video.internal;

import static org.bytedeco.javacpp.avutil.AV_LOG_PANIC;
import static org.bytedeco.javacpp.avutil.av_log_set_level;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.image.IVideoService.IFrameHandler;
import com.bdaum.zoom.image.IVideoService.IVideoStreamer;

public class VideoStreamer implements IVideoStreamer {

	private String file;
	private int preferredWidth;
	private IFrameHandler frameHandler;

	public VideoStreamer(String file, IFrameHandler frameHandler, int preferredWidth) {
		this.file = file;
		this.frameHandler = frameHandler;
		this.preferredWidth = preferredWidth;
	}

	@Override
	public void start() throws IOException {
		av_log_set_level(AV_LOG_PANIC);
		int frameNo = 0;
		try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(file)) {
			Java2DFrameConverter converter = new Java2DFrameConverter();
			grabber.start();
			if (preferredWidth > 0) {
				int h = grabber.getImageHeight();
				int w = grabber.getImageWidth();
				double f = (double) preferredWidth / Math.max(w, h);
				int newWidth = (int) (w * f);
				int newHeight = (int) (h * f);
				grabber.setImageWidth(newWidth);
				grabber.setImageHeight(newHeight);
			}
			int lengthInFrames = grabber.getLengthInFrames();
//			double frameRate = grabber.getFrameRate();
			for (frameNo = 0; frameNo < lengthInFrames; frameNo++) {
				grabber.setFrameNumber(frameNo);
				Frame frame = grabber.grabImage();
				BufferedImage img = converter.getBufferedImage(frame);
				if (!frameHandler.handleFrame(img))
					break;
			}
		} catch (Exception e) {
			if (frameNo > 0 && e.getMessage().contains("_seek_")) //$NON-NLS-1$
				throw new UnsupportedOperationException(e.getMessage());
			throw new IOException(NLS.bind("Streaming of file {0} failed", file), e); //$NON-NLS-1$
		}
	}

}
