/*
 * This file is part of the ZoRa project: http://www.photozora.org.
 *
 * ZoRa is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ZoRa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZoRa; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.ui.internal.audio;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.TargetDataLine;

import com.bdaum.zoom.ui.internal.UiActivator;

public class AudioCapture extends Thread {
	private TargetDataLine dataLine;
	private AudioFileFormat.Type audioFileType;
	private AudioInputStream incomingStream;
	private File generatedFile;

	public AudioCapture(TargetDataLine line, AudioFileFormat.Type requiredFileType, File file) {
		dataLine = line;
		incomingStream = new AudioInputStream(line);
		audioFileType = requiredFileType;
		generatedFile = file;
	}

	public void startCapture() {
		dataLine.start();
		super.start();
	}

	public void stopCapture() {
		if (dataLine.isRunning())
			dataLine.stop();
		if (dataLine.isOpen())
			dataLine.close();
	}

	@Override
	public void run() {
		try {
			AudioSystem.write(incomingStream, audioFileType, generatedFile);
		} catch (IOException e) {
			UiActivator.getDefault().logError(Messages.getString("AudioCapture.io_error"), e); //$NON-NLS-1$
		}
	}
}
