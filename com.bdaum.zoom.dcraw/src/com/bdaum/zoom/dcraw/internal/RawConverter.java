/*******************************************************************************
 * Copyright (c) 2009-2015 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/

package com.bdaum.zoom.dcraw.internal;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Bundle;

import com.bdaum.zoom.batch.internal.Options;
import com.bdaum.zoom.core.IRecipeDetector;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.image.IFileHandler;
import com.bdaum.zoom.image.IFocalLengthProvider;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.image.recipe.Recipe;
import com.bdaum.zoom.program.AbstractRawConverter;
import com.bdaum.zoom.program.IConverter;
import com.bdaum.zoom.program.IRawConverter;

@SuppressWarnings("restriction")
public class RawConverter extends AbstractRawConverter {

	private static final String HIGHRES = "highres"; //$NON-NLS-1$
	private static final String wbASSHOT = "asShot"; //$NON-NLS-1$
	private static final String wbAUTO = "auto"; //$NON-NLS-1$

	private static final String RAWITERATIONS = "com.bdaum.zoom.rawiterations"; //$NON-NLS-1$
	private static final String RAWINTERPOLATION = "com.bdaum.zoom.rawinterpolation"; //$NON-NLS-1$
	private static final String RAWHIGHLIGHT = "com.bdaum.zoom.rawhighlight"; //$NON-NLS-1$
	private static final String WB_METHOD = "com.bdaum.zoom.wbMethod"; //$NON-NLS-1$
	private static final String SHOW_INFO = "com.bdaum.zoom.dcraw.property.showInfo"; //$NON-NLS-1$

	private final static NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);

	static {
		nf.setGroupingUsed(false);
	}

	private File dcraw;
	private boolean external = false;
	private int outputProfile = -1;

	public RawConverter() {
		setConverterLocation(null);
	}

	public void setConverterLocation(String location) {
		if (location == null || location.isEmpty()) {
			if (dcraw == null || external)
				dcraw = Activator.getDefault().locateDCRAW();
			external = false;
		} else {
			dcraw = new File(location);
			external = true;
		}
	}

	public File setInput(File file, Options options) {
		IFileHandler fileHandler = ImageConstants.findFileHandler(rawFile = file);
		if (fileHandler != null) {
			rawFile = fileHandler.getImageFile(file);
			if (rawFile == null)
				return null;
		}
		String path = rawFile.getAbsolutePath();
		int p = path.lastIndexOf('.');
		if (p >= 0)
			path = path.substring(0, p);
		return new File(path + ".tiff"); //$NON-NLS-1$
	}

	public String[] getParms(Options options) {
		if (dcraw == null)
			return null;
		List<String> parms = new ArrayList<String>();
		parms.add(dcraw.getAbsolutePath());
		if (options != null && getOption(options, SHOW_INFO))
			parms.add("-v"); //$NON-NLS-1$
		if (options != null && getOption(options, HIGHRES)) {
			Integer q = (Integer) options.get(RAWINTERPOLATION);
			if (q != null) {
				parms.add("-q"); //$NON-NLS-1$
				parms.add(String.valueOf(q.intValue()));
			}
			Integer i = (Integer) options.get(RAWITERATIONS);
			if (i != null) {
				parms.add("-m"); //$NON-NLS-1$
				parms.add(String.valueOf(i));
			}
			Integer h = (Integer) options.get(RAWHIGHLIGHT);
			if (h != null && h.intValue() != 0) {
				parms.add("-H"); //$NON-NLS-1$
				parms.add(String.valueOf(h.intValue()));
			}
			Integer n = (Integer) options.get(DENOISE);
			if (n != null) {
				parms.add("-n"); //$NON-NLS-1$
				parms.add(String.valueOf(n.intValue()));
			}
			Object abb = options.get(ABBERATION);
			if (abb instanceof double[]) {
				parms.add("-C"); //$NON-NLS-1$
				for (double f : (double[]) abb)
					parms.add(nf.format(f));
			}
		} else
			parms.add("-h"); //$NON-NLS-1$
		if (options != null) {
			Float g = (Float) options.get(GAMMA);
			Float e = (Float) options.get(EXPOSURE);
			if (g != null || e != null)
				parms.add("-W"); //$NON-NLS-1$
			if (e != null && e.doubleValue() != 1f) {
				parms.add("-b"); //$NON-NLS-1$
				parms.add(nf.format(Math.min(999, e)));
			}
			if (g != null) {
				parms.add("-g"); //$NON-NLS-1$
				parms.add(nf.format(g));
				parms.add("0.0"); //$NON-NLS-1$
			}
			Object wb = options.get(WB_METHOD);
			if (wbASSHOT.equals(wb))
				parms.add("-w"); //$NON-NLS-1$
			else if (wbAUTO.equals(wb))
				parms.add("-a"); //$NON-NLS-1$
			else if (wb instanceof float[]) {
				parms.add("-r"); //$NON-NLS-1$
				float[] wf = (float[]) wb;
				parms.add(nf.format(wf[0]));
				parms.add(nf.format(wf[1]));
				parms.add(nf.format(wf[2]));
				parms.add(nf.format(wf[wf.length < 4 ? 1 : 3]));
			}
			Integer iccProfile = (Integer) options.get(COLORPROFILE);
			int op = 0;
			if (iccProfile != null) {
				switch (iccProfile) {
				case ImageConstants.SRGB:
					op = 1;
					outputProfile = iccProfile;
					break;
				case ImageConstants.ARGB:
					op = 2;
					outputProfile = iccProfile;
					break;
				}
			}
			parms.add("-o"); //$NON-NLS-1$
			parms.add(String.valueOf(op));
		}
		parms.add("-T"); //$NON-NLS-1$
		parms.add(rawFile.getName());
		return parms.toArray(new String[parms.size()]);
	}

	private static boolean getOption(Options options, String option) {
		return options == null ? false : options.getBoolean(option);
	}

	public File getInputDirectory() {
		return rawFile == null ? null : rawFile.getParentFile();
	}

	public int deriveOptions(Recipe recipe, Options options, int resolution) {
		for (IRawConverter.RawProperty prop : props) {
			if (SHOW_INFO.equals(prop.id))
				options.put(SHOW_INFO, Boolean.parseBoolean(prop.value));
			else if (recipe == null) {
				if (WB_METHOD.equals(prop.id)) {
					try {
						switch (Integer.parseInt(prop.value)) {
						case Recipe.wbASSHOT:
							options.put(WB_METHOD, wbASSHOT);
							break;
						case Recipe.wbAUTO:
							options.put(WB_METHOD, wbAUTO);
							break;
						}
					} catch (NumberFormatException e) {
						// ignore
					}
				} else if (RAWHIGHLIGHT.equals(prop.id)) {
					if (Boolean.parseBoolean(prop.value))
						options.put(RAWHIGHLIGHT, 2);
				}
			}
			if (resolution == HIGH) {
				try {
					if (RAWINTERPOLATION.equals(prop.id))
						options.put(RAWINTERPOLATION, Integer.parseInt(prop.value));
					else if (RAWITERATIONS.equals(prop.id))
						options.put(RAWITERATIONS, Integer.parseInt(prop.value));
				} catch (NumberFormatException e) {
					// do nothing
				}
			}
		}
		if (recipe != null) {
			switch (recipe.whiteBalanceMethod) {
			case Recipe.wbASSHOT:
				options.put(WB_METHOD, wbASSHOT);
				break;
			case Recipe.wbAUTO:
				options.put(WB_METHOD, wbAUTO);
				break;
			case Recipe.wbFACTORS:
				options.put(WB_METHOD, recipe.getWbFactors());
				break;
			}
			if (resolution != THUMB) {
				if (recipe.noiseReduction > 0)
					options.put(IConverter.DENOISE, recipe.noiseReduction);
				if (recipe.chromaticAberrationB != 1d && recipe.chromaticAberrationR != 1d)
					options.put(IConverter.ABBERATION,
							new double[] { recipe.chromaticAberrationR, recipe.chromaticAberrationB });
			}
			if (recipe.highlightRecovery > 0)
				options.put(RAWHIGHLIGHT, recipe.highlightRecovery);
			options.put(IConverter.EXPOSURE, recipe.exposure);
			if (!Float.isNaN(recipe.gamma))
				options.put(IConverter.GAMMA, recipe.gamma);
		}
		if (resolution == HIGH) {
			options.put(HIGHRES, Boolean.TRUE);
			return 1;
		}
		return 2;
	}

	public File findModule(File parentFile) {
		if (parentFile != null) {
			File dcraw = new File(parentFile, Activator.DCRAW);
			if (dcraw.exists())
				return dcraw;
			File[] members = parentFile.listFiles();
			if (members != null)
				for (File member : members)
					if (member.isDirectory()) {
						dcraw = findModule(member);
						if (dcraw != null)
							return dcraw;
					}
		}
		return null;
	}

	@Override
	public String getVersionMessage() {
		Bundle[] fragments = Platform.getFragments(Activator.getDefault().getBundle());
		if (fragments != null && fragments.length > 0)
			return NLS.bind(Messages.getString("RawConverter.internal_version"), //$NON-NLS-1$
					fragments[0].getVersion());
		return null;
	}

	public synchronized long getLastRecipeModification(String uri, long lastMod, String[] detectorIds) {
		List<IRecipeDetector> detectors = CoreActivator.getDefault().getDetectors(detectorIds);
		if (detectors != null)
			for (IRecipeDetector recipeDetector : detectors)
				if (recipeDetector.isRecipeXMPembbedded(uri) < 0)
					lastMod = Math.max(recipeDetector.getRecipeModificationTimestamp(uri), lastMod);
		return lastMod;
	}

	public synchronized Recipe getRecipe(final String uri, boolean highres, IFocalLengthProvider focalLengthProvider,
			Map<String, String> overlayMap, String[] detectorIds) {
		List<IRecipeDetector> detectors = CoreActivator.getDefault().getDetectors(detectorIds);
		if (detectors != null) {
			Collections.sort(detectors, new Comparator<IRecipeDetector>() {
				public int compare(IRecipeDetector d1, IRecipeDetector d2) {
					long t1 = d1.getRecipeModificationTimestamp(uri);
					long t2 = d2.getRecipeModificationTimestamp(uri);
					if (t1 == t2) {
						int p1 = d1.isRecipeXMPembbedded(uri);
						int p2 = d2.isRecipeXMPembbedded(uri);
						if (p1 >= 0 && p2 >= 0)
							return p1 - p2;
						return 0;
					}
					return (int) (t2 - t1);
				}
			});
			for (IRecipeDetector detector : detectors)
				try {
					Recipe recipe = detector.loadRecipeForImage(uri, highres, focalLengthProvider, overlayMap);
					if (recipe != null)
						return recipe;
				} catch (Exception e) {
					Activator.getDefault().logError(NLS.bind(Messages.getString("RawConverter.internal_error"), //$NON-NLS-1$
							detector.getName()), e);
				}
		}
		return null;
	}

	@Override
	public boolean isValid() {
		return dcraw != null ? true : super.isValid();
	}

	@Override
	public int getOutputProfile() {
		return outputProfile;
	}

}
