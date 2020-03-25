package com.bdaum.zoom.image;

import java.awt.image.BufferedImage;
import java.io.IOException;

public interface IVideoService {
	
	public interface IFrameHandler {
		
		boolean handleFrame(BufferedImage frame);
		
	}

	public interface IVideoStreamer {
		
		void start() throws IOException;

	}

	IVideoStreamer createVideoStreamer(String file, IFrameHandler frameHandler, int preferreWidth);

}
