package com.bdaum.zoom.rawtherapee.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.bdaum.zoom.batch.internal.Options;
import com.bdaum.zoom.core.IRecipeDetector;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.image.IFileHandler;
import com.bdaum.zoom.image.IFocalLengthProvider;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.image.recipe.Recipe;
import com.bdaum.zoom.program.AbstractRawConverter;
import com.bdaum.zoom.program.BatchConstants;
import com.bdaum.zoom.program.IRawConverter;

@SuppressWarnings("restriction")
public class RTRawConverter extends AbstractRawConverter {

	private static final String RT3DETECTORID = "com.bdaum.zoom.recipes.rt3"; //$NON-NLS-1$
	private static final String SIDECAR = "sidecar"; //$NON-NLS-1$
	private static final String HIGHRES = "highres"; //$NON-NLS-1$
	private static final String USE_PP3 = "com.bdaum.zoom.rawtherapee.property.recipes"; //$NON-NLS-1$
	private File rt;
	private File outFile;
	private IRecipeDetector rt3Detector;

	public void setConverterLocation(String location) {
		if (location != null && location.length() > 0)
			rt = new File(location);
	}

	public String[] getParms(Options options) {
		if (rt == null)
			return null;
		List<String> parms = new ArrayList<String>();
		parms.add(rt.getAbsolutePath());
		if (BatchConstants.WIN32)
			parms.add("-w"); //$NON-NLS-1$
		parms.add("-o"); //$NON-NLS-1$
		parms.add(outFile.getAbsolutePath());
		if (options != null) {
			if (options.getBoolean(SIDECAR))
				parms.add("-s"); //$NON-NLS-1$
			if (options.getBoolean(HIGHRES))
				parms.add("-b16"); //$NON-NLS-1$
			else
				parms.add("-b8"); //$NON-NLS-1$
		}
		parms.add("-t"); //$NON-NLS-1$
		parms.add("-Y"); //$NON-NLS-1$
		parms.add("-c"); //$NON-NLS-1$
		parms.add(rawFile.getAbsolutePath());
		return parms.toArray(new String[parms.size()]);
	}

	public File setInput(File file, Options options) {
		rawFile = file;
		IFileHandler fileHandler = ImageConstants.findFileHandler(file);
		if (fileHandler != null) {
			rawFile = fileHandler.getImageFile(file);
			if (rawFile == null)
				return null;
		}
		String name = rawFile.getAbsolutePath();
		int p = name.lastIndexOf('.');
		if (p >= 0)
			name = name.substring(0, p);
		outFile = new File(name + ".tif"); //$NON-NLS-1$
		return outFile;
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
		return null;
	}

	public long getLastRecipeModification(String uri, long lastMod,
			String[] detectorIds) {
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
			List<IRecipeDetector> detectors = CoreActivator.getDefault()
					.getDetectors(detectorIds);
			if (detectors != null)
				for (IRecipeDetector recipeDetector : detectors)
					if (recipeDetector.getId().equals(RT3DETECTORID))
						rt3Detector = recipeDetector;
		}
		return rt3Detector;
	}

	public Recipe getRecipe(String uri, boolean highres,
			IFocalLengthProvider focalLengthProvider,
			Map<String, String> overlayMap, String[] detectorIds) {
		return getLastRecipeModification(uri, detectorIds) > 0 ? Recipe.NULL
				: null;
	}

	@Override
	public boolean isValid() {
		if (rt != null)
			return true;
		return super.isValid();
	}

}
