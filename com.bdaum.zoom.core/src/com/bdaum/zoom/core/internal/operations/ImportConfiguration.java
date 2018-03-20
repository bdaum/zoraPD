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
 * (c) 2009 Berthold Daum  
 */

package com.bdaum.zoom.core.internal.operations;

import java.util.List;

import org.eclipse.core.runtime.IAdaptable;

import com.bdaum.zoom.batch.internal.BatchActivator;
import com.bdaum.zoom.core.IRelationDetector;
import com.bdaum.zoom.program.IRawConverter;

@SuppressWarnings("restriction")
public class ImportConfiguration {
	public final String timeline;
	public boolean isSynchronize;
	public boolean isResetStatus;
	public boolean isResetIptc;
	public boolean isResetGps;
	public String rawOptions;
	public final IDngLocator dngLocator;
	public final boolean dngUncompressed;
	public final boolean dngLinear;
	public final String deriveRelations;
	public final String dngFolder;
	public final boolean onPrompt;
	public final boolean onFinish;
	public final boolean inBackground;
	public final boolean autoDerive;
	public final boolean applyXmp;
	public final boolean isResetImage;
	public final boolean isResetExif;
	public final boolean makerNotes;
	public final boolean archiveRecipes;
	public boolean isResetFaceData;
	public final String locations;
	public final boolean useWebP;
	public final int jpegQuality;
	public final boolean processSidecars;
	public int conflictPolicy;
	public final boolean showImported;
	public final IRelationDetector[] relationDetectors;
	public final List<AutoRule> rules;
	public final IAdaptable info;
	public IRawConverter rawConverter;
	public boolean silent;

	public ImportConfiguration(IAdaptable info, String timeline,
			String locations, boolean isSynchronize, int conflictPolicy,
			boolean isResetImage, boolean isResetStatus, boolean isResetExif,
			boolean isResetIptc, boolean isResetGps, boolean isResetFaceData,
			boolean processSidecars, String rawOptions, IDngLocator dngLocator,
			boolean dngUncompressed, boolean dngLinear, String deriveRelations,
			boolean autoDerive, boolean applyXmp, String dngFolder,
			boolean onPrompt, boolean onFinish, boolean inBackground,
			boolean makerNotes, boolean archiveRecipes, boolean useWebP, int jpegQuality,
			boolean showImported, IRelationDetector[] relationDetectors,
			List<AutoRule> rules) {
		this.info = info;
		this.timeline = timeline;
		this.locations = locations;
		this.isSynchronize = isSynchronize;
		this.conflictPolicy = conflictPolicy;
		this.isResetImage = isResetImage;
		this.isResetStatus = isResetStatus;
		this.isResetExif = isResetExif;
		this.isResetIptc = isResetIptc;
		this.isResetGps = isResetGps;
		this.isResetFaceData = isResetFaceData;
		this.processSidecars = processSidecars;
		this.rawOptions = rawOptions;
		this.dngLocator = dngLocator;
		this.dngUncompressed = dngUncompressed;
		this.dngLinear = dngLinear;
		this.deriveRelations = deriveRelations;
		this.autoDerive = autoDerive;
		this.applyXmp = applyXmp;
		this.dngFolder = dngFolder;
		this.onPrompt = onPrompt;
		this.onFinish = onFinish;
		this.inBackground = inBackground;
		this.makerNotes = makerNotes;
		this.archiveRecipes = archiveRecipes;
		this.useWebP = useWebP;
		this.jpegQuality = jpegQuality;
		this.showImported = showImported;
		this.relationDetectors = relationDetectors;
		this.rules = rules;
		this.rawConverter = BatchActivator.getDefault().getCurrentRawConverter(false);
	}
}