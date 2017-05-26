/*******************************************************************************
 * Copyright (c) 2009-2011 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *     stayopen feature is based on the ExifTool class from The Buzz Media, LLC
 *            (http://www.thebuzzmedia.com/software/exiftool-enhanced-java-integration-for-exiftool/)
 *******************************************************************************/

package com.bdaum.zoom.batch.internal;

import java.awt.color.ICC_Profile;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;

import com.bdaum.zoom.image.IExifLoader;
import com.bdaum.zoom.image.ZImage;
import com.bdaum.zoom.image.internal.swt.ImageLoader;
import com.bdaum.zoom.program.BatchUtilities;

public class ExifTool implements IExifLoader {

	private static class IOStream {
		OutputStreamWriter writer;
		InputStream inputStream;
		BufferedReader inputReader;

		public IOStream(InputStream inputStream, OutputStreamWriter writer) {
			this.inputStream = inputStream;
			inputReader = new BufferedReader(new InputStreamReader(inputStream));
			this.writer = writer;
		}

		public void close() {
			try {
				inputReader.close();
			} catch (Exception e) {
				// ignore
			}
			try {
				inputStream.close();
			} catch (Exception e) {
				// ignore
			}
			try {
				writer.close();
			} catch (Exception e) {
				// ignore
			}
			inputReader = null;
			inputStream = null;
			writer = null;
		}
	}

	private static final String[] B_ICCPROFILE = new String[] {
			"-b", "-icc_profile" //$NON-NLS-1$ //$NON-NLS-2$
	};
	private static final String[] B_PREVIEWIMAGE_FAST = new String[] { "-b", //$NON-NLS-1$
			"-previewimage", "-fast" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] E_S_N_FAST_STRUCT = new String[] {
			"-E", "-S", "-n", "-fast", "-g0", "-struct" }; //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
	private static final String[] E_S_N_STRUCT = new String[] {
			"-E", "-S", "-n", "-g0", "-struct" }; //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	private static final String[] STAYOPEN = new String[] { "-stay_open", //$NON-NLS-1$
			"True", "-@", "-" }; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
	private File file;
	private String[] toolLocation;
	private Map<String, String> metadata;
	private Set<String> makerNotes;
	private boolean isMakerNote;

	private static final float[][] SWTORIENTATION = new float[][] {
			new float[] { 0f, 1f, 1f }, new float[] { 0f, -1f, 1f },
			new float[] { 180f, 1f, 1f }, new float[] { 0f, 1f, -1f },
			new float[] { 90f, -1f, 1f }, new float[] { 90f, 1f, 1f },
			new float[] { 270f, -1f, 1f }, new float[] { 270f, 1f, 1f }, };
	private static final long CLEANUP_DELAY = 60 * 1000;
	private final boolean once;
	private IOStream streams;
	private final StringBuffer data = new StringBuffer();
	private byte[] buffer;
	private boolean abort;
	private boolean fast;
	private Thread shutdownHook = new Thread() {
		@Override
		public void run() {
			ExifTool.this.close();
		}
	};
	private Daemon cleanupJob;
	private static Process proc;

	public ExifTool(File file, boolean once) {
		this.file = file;
		this.once = once;
		toolLocation = BatchActivator.getDefault().locateExifTool();
		if (!once) {
			try {
				Runtime.getRuntime().addShutdownHook(shutdownHook);
			} catch (Exception e) {
				// ignore
			}
			cleanupJob = new Daemon(Messages.ExifTool_exiftool_cleanup,
					CLEANUP_DELAY) {
				@Override
				protected void doRun(IProgressMonitor monitor) {
					ExifTool.this.close();
				}
			};
			resetCleanupTask();
		}
	}

	public void reset(File f) {
		this.file = f;
		metadata = null;
		makerNotes = null;
		isMakerNote = false;
		resetCleanupTask();
	}

	private void resetCleanupTask() {
		// no-op if the timer was never created.
		if (cleanupJob == null)
			return;

		// Cancel the current cleanup task if necessary.
		Job.getJobManager().cancel(cleanupJob);

		// Schedule a new cleanup task.
		cleanupJob.schedule(CLEANUP_DELAY);
	}

	public Map<String, String> getMetadata() {
		if (metadata == null)
			compileMetaData();
		return metadata;
	}

	private void compileMetaData() {
		metadata = new HashMap<String, String>(300);
		makerNotes = new HashSet<String>(150);
		if (toolLocation != null) {
			String[] parms = fast ? E_S_N_FAST_STRUCT : E_S_N_STRUCT;
			try {
				String result = (String) connectToExifTool(parms, false);
				if (result != null) {
					StringTokenizer st = new StringTokenizer(result, "\n"); //$NON-NLS-1$
					while (st.hasMoreTokens()) {
						String line = st.nextToken().trim();
						if (line.startsWith("-")) //$NON-NLS-1$
							isMakerNote = line.indexOf("MakerNotes") > 0; //$NON-NLS-1$
						else {
							int p = line.indexOf(':');
							if (p >= 0) {
								String key = line.substring(0, p);
								StringBuilder sb = new StringBuilder();
								BatchUtilities.decodeHTML(line.substring(p + 1)
										.trim(), sb);
								metadata.put(key, sb.toString());
								if (isMakerNote)
									makerNotes.add(key);
							}
						}
					}
				}
			} catch (ConversionException e) {
				BatchActivator
						.getDefault()
						.logError(
								NLS.bind(
										Messages.ExifTool_Interna_error_fetching_metadata,
										file), e);
			}
		}
	}

	private Object connectToExifTool(String[] parms, boolean binary)
			throws ConversionException {
		if (toolLocation == null)
			return null;
		abort = false;
		if (once) {
			List<String> args = concatParms(parms);
			args.add(file.getAbsolutePath());
			streams = startExifToolProcess(args);
		} else {
			resetCleanupTask();
			if (streams == null)
				streams = startExifToolProcess(concatParms(STAYOPEN));
			try {
				for (int i = 0; i < parms.length; i++) {
					streams.writer.write(parms[i]);
					streams.writer.write('\n');
				}
				streams.writer.write(file.getAbsolutePath());
				streams.writer.write("\n"); //$NON-NLS-1$
				streams.writer.write("-execute\n"); //$NON-NLS-1$
				streams.writer.flush();
			} catch (IOException e) {
				throw new ConversionException(
						Messages.ExifTool_error_sending_commands, e);
			}
		}
		String line;
		if (binary) {
			InputStream inputStream = streams.inputStream;
			try {
				byte[] bdata = null;
				if (buffer == null)
					buffer = new byte[4096];
				while (!abort) {
					int read = inputStream.read(buffer);
					if (read < 0)
						break;
					if (read >= 7) {
						int end = read - 1;
						while (end >= 0 && buffer[end] == 13
								|| buffer[end] == 10)
							--end;
						if (end >= 6 && buffer[end] == '}'
								&& buffer[end - 1] == 'y'
								&& buffer[end - 2] == 'd'
								&& buffer[end - 3] == 'a'
								&& buffer[end - 4] == 'e'
								&& buffer[end - 5] == 'r'
								&& buffer[end - 6] == '{') {
							read = end - 6;
							abort = true;
						}
					}
					if (bdata == null) {
						bdata = new byte[read];
						System.arraycopy(buffer, 0, bdata, 0, read);
					} else {
						byte[] newData = new byte[bdata.length + read];
						System.arraycopy(bdata, 0, newData, 0, bdata.length);
						System.arraycopy(buffer, 0, newData, bdata.length, read);
						bdata = newData;
					}
					if (read < buffer.length)
						break;
				}
				return bdata;
			} catch (IOException e) {
				throw new ConversionException(
						Messages.ExifTool_error_fetching_binary_data, e);
			}
		}
		BufferedReader reader = streams.inputReader;
		data.setLength(0);
		try {
			while (!abort && (line = reader.readLine()) != null) {
				if (!once && line.equals("{ready}")) //$NON-NLS-1$
					break;
				data.append(line).append('\n');
			}
			return data.toString();
		} catch (IOException e) {
			throw new ConversionException(
					Messages.ExifTool_error_fetching_metadata, e);
		}
	}

	protected static IOStream startExifToolProcess(List<String> args)
			throws ConversionException {
		IOStream streams = null;
		try {
			proc = new ProcessBuilder(args).start();
		} catch (Exception e) {
			throw new ConversionException(
					Messages.ExifTool_error_launching_error_tool, e);
		}
		streams = new IOStream(proc.getInputStream(), new OutputStreamWriter(
				proc.getOutputStream()));
		return streams;
	}

	public void dispose() {
		if (!once) {
			if (cleanupJob != null)
				cleanupJob.cancel();
			close();
			if (proc != null)
				proc.destroy();
		}
	}

	public Set<String> getMakerNotes() {
		if (makerNotes == null)
			compileMetaData();
		return makerNotes;
	}

	public double get35mm() {
		String flen = getMetadata().get("FocalLength35efl"); //$NON-NLS-1$
		if (flen != null) {
			try {
				return Double.parseDouble(flen);
			} catch (NumberFormatException e) {
				// ignore
			}
		}
		return Double.NaN;
	}

	private List<String> concatParms(String[] parms) {
		List<String> args = new ArrayList<String>(parms.length
				+ toolLocation.length + 1);
		for (String s : toolLocation)
			args.add(s);
		for (String s : parms)
			args.add(s);
		return args;
	}

	public byte[] getBinaryData(String tag, boolean check) {
		String pvi = check ? getMetadata().get(tag) : ""; //$NON-NLS-1$
		if (pvi != null) {
			String[] parms = new String[] { "-b", "-" + tag.toLowerCase() }; //$NON-NLS-1$//$NON-NLS-2$
			try {
				return (byte[]) connectToExifTool(parms, true);
			} catch (ConversionException e) {
				BatchActivator.getDefault().logError(
						NLS.bind(Messages.ExifTool_errort_fetching_binary_data,
								tag, file), e);
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.batch.IExifTool#getPreviewImage(boolean)
	 */

	public ZImage getPreviewImage(boolean check) {
		String pvi = check ? getMetadata().get("PreviewImage") : ""; //$NON-NLS-1$ //$NON-NLS-2$
		if (pvi != null) {
			try {
				byte[] result = (byte[]) connectToExifTool(B_PREVIEWIMAGE_FAST,
						true);
				if (result != null && result.length > 0) {
					ImageData[] image = new ImageLoader().load(
							new ByteArrayInputStream(result), SWT.DEFAULT);
					if (image != null && image.length > 0)
						return new ZImage(image[0], file.getAbsolutePath());
				}
			} catch (ConversionException e) {
				BatchActivator
						.getDefault()
						.logError(
								NLS.bind(
										Messages.ExifTool_Internal_error_fetching_preview,
										file), e);
			}
		}
		return null;
	}

	public static void fixOrientation(ZImage image, int ori, int rotation) {
		if (ori >= 1 && ori <= 8) {
			float[] parms = SWTORIENTATION[ori - 1];
			int rot = (int) parms[0] + rotation;
			if (rot < 0)
				rot += 360;
			if (rot >= 360)
				rot -= 360;
			image.setOrientation(rot, parms[1], parms[2]);
		} else
			image.setOrientation(rotation, 1.0f, 1.0f);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.batch.IExifTool#getICCProfile()
	 */
	public ICC_Profile getICCProfile() {
		if (toolLocation != null) {
			try {
				byte[] result = (byte[]) connectToExifTool(B_ICCPROFILE, true);
				if (result != null) {
					try {
						return ICC_Profile.getInstance(result);
					} catch (Exception e) {
						BatchActivator.getDefault().logWarning(
								NLS.bind(Messages.ExifTool_Bad_ICC_profile,
										file), null);
					}
				}
			} catch (ConversionException e) {
				BatchActivator.getDefault().logError(
						NLS.bind(Messages.ExifTool_Internal_error_fetching_ICC,
								file), e);
			}
		}
		return null;
	}

	protected void close() {
		if (streams == null)
			return;
		abort = true;
		try {
			Runtime.getRuntime().removeShutdownHook(shutdownHook);
		} catch (Exception e1) {
			// ignore
		}
		try {
			streams.writer.write("-stay_open\nFalse\n"); //$NON-NLS-1$
			streams.writer.flush();
		} catch (IOException e) {
			// ignore
		} finally {
			streams.close();
		}
		streams = null;
	}

	public void setFast(boolean fast) {
		this.fast = fast;
	}

}
