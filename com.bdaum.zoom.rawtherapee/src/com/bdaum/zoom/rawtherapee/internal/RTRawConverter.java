package com.bdaum.zoom.rawtherapee.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IStatus;

import com.bdaum.zoom.batch.internal.Options;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.IRecipeDetector;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.image.IFileHandler;
import com.bdaum.zoom.image.IFocalLengthProvider;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.image.recipe.Recipe;
import com.bdaum.zoom.program.AbstractRawConverter;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.program.IRawConverter;

@SuppressWarnings("restriction")
public class RTRawConverter extends AbstractRawConverter {

	private static final String RT3DETECTORID = "com.bdaum.zoom.recipes.rt3"; //$NON-NLS-1$
	private static final String SIDECAR = "sidecar"; //$NON-NLS-1$
	private static final String HIGHRES = "highres"; //$NON-NLS-1$
	private static final String USE_PP3 = "com.bdaum.zoom.rawtherapee.property.recipes"; //$NON-NLS-1$
	private static final String LOCATE = "locate"; //$NON-NLS-1$
	private static final String RAW_THERAPEE = "RawTherapee"; //$NON-NLS-1$
	private static final String[] LOCATERAWTHERAPEE = new String[] { LOCATE, RAW_THERAPEE };
	private File rt;
	private File outFile;
	private IRecipeDetector rt3Detector;

	public void setConverterLocation(String location) {
		if (location != null && !location.isEmpty())
			rt = new File(location);
	}

	public String[] getParms(Options options) {
		if (rt == null)
			return null;
		List<String> parms = new ArrayList<String>();
		parms.add(rt.getAbsolutePath());
//		if (BatchConstants.WIN32)
//			parms.add("-w"); //$NON-NLS-1$
		parms.add("-o"); //$NON-NLS-1$
		parms.add(outFile.getAbsolutePath());
		if (options != null) {
			if (options.getBoolean(SIDECAR))
				parms.add("-s"); //$NON-NLS-1$
			parms.add(options.getBoolean(HIGHRES) ? "-b16" : "-b8"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		parms.add("-t"); //$NON-NLS-1$
		parms.add("-Y"); //$NON-NLS-1$
		parms.add("-c"); //$NON-NLS-1$
		parms.add(rawFile.getAbsolutePath());
		return parms.toArray(new String[parms.size()]);
	}

	public File setInput(File file, Options options) {
		IFileHandler fileHandler = ImageConstants.findFileHandler(rawFile = file);
		if (fileHandler != null) {
			rawFile = fileHandler.getImageFile(file);
			if (rawFile == null)
				return null;
		}
		return outFile = new File(rawFile.getAbsolutePath() + ".tif"); //$NON-NLS-1$
	}

	public File getInputDirectory() {
		return rawFile == null ? null : rawFile.getParentFile();
	}

	public int deriveOptions(Recipe rawRecipe, Options options, int resolution) {
		for (IRawConverter.RawProperty prop : props)
			if (USE_PP3.equals(prop.id) && Boolean.parseBoolean(prop.value))
				options.put(SIDECAR, Boolean.TRUE);
		options.put(HIGHRES, resolution != THUMB);
		return 1;
	}

	public File findModule(File parentFile) {
		if (Constants.WIN32) {
			if (parentFile == null) {
				String getenv = System.getenv("ProgramFiles"); //$NON-NLS-1$
				if (getenv != null)
					parentFile = new File(getenv, "RawTherapee"); //$NON-NLS-1$
			}
			if (parentFile != null) {
				File[] members = parentFile.listFiles();
				File newest = null;
				if (members != null)
					for (File file : members)
						if (newest == null || file.getName().compareTo(newest.getName()) > 0)
							newest = file;
				if (newest != null) {
					File module = new File(newest, "rawtherapee-cli.exe"); //$NON-NLS-1$
					if (module.exists())
						return module;
					module = new File(newest, "rawtherapee.exe"); //$NON-NLS-1$
					if (module.exists())
						return module;
				}
			}
		} else if (Constants.LINUX) {
			if (parentFile == null) {
				try {
					String result = BatchUtilities.executeCommand(LOCATERAWTHERAPEE, null,
							Messages.RTRawConverter_locate_rawtherapee, IStatus.OK, IStatus.WARNING, -1, 1000L,
							"UTF-8"); //$NON-NLS-1$
					if (result != null) {
						StringTokenizer st = new StringTokenizer(result, "\n"); //$NON-NLS-1$
						while (st.hasMoreTokens()) {
							String line = st.nextToken().trim();
							int p = line.lastIndexOf(RAW_THERAPEE);
							if (p >= 0) {
								int q = line.indexOf('/', p + 10);
								if (q < 0) {
									File module = new File(line, "rawtherapee-cli"); //$NON-NLS-1$
									if (module.exists())
										return module;
									module = new File(line, "rawtherapee"); //$NON-NLS-1$
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

	public long getLastRecipeModification(String uri, long lastMod, String[] detectorIds) {
		return Math.max(lastMod, getLastRecipeModification(uri, detectorIds));
	}

	private long getLastRecipeModification(String uri, String[] detectorIds) {
		for (IRawConverter.RawProperty prop : props)
			if (USE_PP3.equals(prop.id) && Boolean.parseBoolean(prop.value)) {
				IRecipeDetector detector = getDetector(detectorIds);
				if (detector != null)
					return detector.getRecipeModificationTimestamp(uri);
			}
		return 0L;
	}

	protected IRecipeDetector getDetector(String[] detectorIds) {
		if (rt3Detector == null) {
			List<IRecipeDetector> detectors = CoreActivator.getDefault().getDetectors(detectorIds);
			if (detectors != null)
				for (IRecipeDetector recipeDetector : detectors)
					if (recipeDetector.getId().equals(RT3DETECTORID))
						return rt3Detector = recipeDetector;
		}
		return rt3Detector;
	}

	public Recipe getRecipe(String uri, boolean highres, IFocalLengthProvider focalLengthProvider,
			Map<String, String> overlayMap, String[] detectorIds) {
		return getLastRecipeModification(uri, detectorIds) > 0 ? Recipe.NULL : null;
	}

	@Override
	public boolean isValid() {
		return rt != null ? true : super.isValid();
	}

	@Override
	public String getVersionMessage() {
		return com.bdaum.zoom.rawtherapee.internal.Messages.RTRawConverter_use_cli;
	}

	@Override
	public String getPath() {
		String path = super.getPath();
		if (path != null && !path.contains("-cli")) { //$NON-NLS-1$
			File file = new File(path);
			File parentFile = file.getParentFile();
			file = path.endsWith(".exe") ? new File(parentFile, "rawtherapee-cli.exe") //$NON-NLS-1$//$NON-NLS-2$
					: new File(parentFile, "rawtherapee-cli"); //$NON-NLS-1$
			if (file.exists())
				return file.getAbsolutePath();
		}
		return path;
	}

}
