package com.bdaum.zoom.darktable.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IStatus;

import com.bdaum.zoom.batch.internal.Options;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.internal.ImportState;
import com.bdaum.zoom.image.IFileHandler;
import com.bdaum.zoom.image.IFocalLengthProvider;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.image.recipe.Recipe;
import com.bdaum.zoom.program.AbstractRawConverter;
import com.bdaum.zoom.program.BatchUtilities;

@SuppressWarnings("restriction")
public class DtRawConverter extends AbstractRawConverter {

	private static final String LOCATE = "locate"; //$NON-NLS-1$
	private static final String DARKTABLE = "darktable"; //$NON-NLS-1$
	private static final String[] LOCATEDARKTABLE = new String[] { LOCATE, DARKTABLE };
	private static final String HIGHRES = "highres"; //$NON-NLS-1$
	private File dt;
	private File outFile;

	@Override
	public int deriveOptions(Recipe rawRecipe, Options options, int resolution) {
		options.put(HIGHRES, resolution != THUMB);
		return 1;
	}

	@Override
	public File findModule(File parentFile) {
		if (Constants.WIN32) {
			if (parentFile == null) {
				String getenv = System.getenv("ProgramFiles"); //$NON-NLS-1$
				if (getenv != null)
					parentFile = new File(getenv, "darktable/bin"); //$NON-NLS-1$
			}
			if (parentFile != null) {
				File module = new File(parentFile, "darktable-cli.exe"); //$NON-NLS-1$
				if (module.exists())
					return module;
			}
		} else if (Constants.LINUX) {
			if (parentFile == null) {
				try {
					String result = BatchUtilities.executeCommand(LOCATEDARKTABLE, null,
							Messages.DtRawConverter_locate_darktable, IStatus.OK, IStatus.WARNING, -1, 1000L, "UTF-8", null); //$NON-NLS-1$
					if (result != null) {
						StringTokenizer st = new StringTokenizer(result, "\n"); //$NON-NLS-1$
						while (st.hasMoreTokens()) {
							String line = st.nextToken().trim();
							int p = line.lastIndexOf(DARKTABLE);
							if (p >= 0) {
								int q = line.indexOf('/', p + 10);
								if (q < 0) {
									File module = new File(line, "darktable-cli"); //$NON-NLS-1$
									if (module.exists())
										return module;
								}
							}
						}
					}
				} catch (Exception e) {
					// ignore
				}
			}
		}
		return null;
	}

	@Override
	public void setConverterLocation(String location) {
		if (location != null && !location.isEmpty())
			dt = new File(location);
	}

	// darktable-cli <input file>|<image folder>
	// [<xmp file>]
	// <output file>
	// [--width <max width>]
	// [--height <max height>]
	// [--bpp <bpp>]
	// [--hq <0|1|true|false>]
	// [--verbose]
	// [--core <darktable options>]

	@Override
	public String[] getParms(Options options) {
		if (dt == null)
			return null;
		List<String> parms = new ArrayList<String>();
		parms.add(dt.getAbsolutePath());
		parms.add(rawFile.getAbsolutePath());
		parms.add(outFile.getName());
		if (options != null) {
			if (options.getBoolean(HIGHRES))
				parms.add("--hq true"); //$NON-NLS-1$
			else {
				int size = ImportState
						.computeThumbnailWidth(Core.getCore().getDbManager().getMeta(true).getThumbnailResolution());
				parms.add("--width " + size); //$NON-NLS-1$
				parms.add("--height " + size); //$NON-NLS-1$
				parms.add("--hq false"); //$NON-NLS-1$
			}
		}
		return parms.toArray(new String[parms.size()]);
	}

	@Override
	public File setInput(File file, Options options) {
		IFileHandler fileHandler = ImageConstants.findFileHandler(rawFile = file);
		if (fileHandler != null) {
			rawFile = fileHandler.getImageFile(file);
			if (rawFile == null)
				return null;
		}
		return outFile = new File(rawFile.getAbsolutePath() + ".tif"); //$NON-NLS-1$
	}

	@Override
	public File getInputDirectory() {
		return rawFile == null ? null : rawFile.getParentFile();
	}

	@Override
	public Recipe getRecipe(String uri, boolean highres, IFocalLengthProvider focalLengthProvider,
			Map<String, String> overlayMap, String[] detectorIds) {
		return null;
	}

	@Override
	public long getLastRecipeModification(String uri, long lastMod, String[] detectorIds) {
		return 0L;
	}

}
