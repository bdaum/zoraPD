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
 * (c) 2014 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.operations;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.bdaum.zoom.cat.model.group.exhibition.ExhibitionImpl;
import com.bdaum.zoom.core.Core;

public class ExhibitionPropertiesOperation extends AbstractOperation {

	private ExhibitionImpl backup;
	private ExhibitionImpl exhibition;
	private ExhibitionImpl redo;

	public ExhibitionPropertiesOperation(ExhibitionImpl backup,
			ExhibitionImpl exhibition) {
		super(Messages.ExhibitionPropertiesOperation_set_exhibition_properties);
		this.backup = backup;
		this.exhibition = exhibition;
	}

	public static ExhibitionImpl cloneExhibition(ExhibitionImpl exhibition) {
		return new ExhibitionImpl(exhibition.getName(),
				exhibition.getDescription(), exhibition.getInfo(),
				exhibition.getDefaultViewingHeight(), exhibition.getVariance(),
				exhibition.getGridSize(), exhibition.getShowGrid(),
				exhibition.getSnapToGrid(), exhibition.getDefaultDescription(),
				exhibition.getLabelFontFamily(), exhibition.getLabelFontSize(),
				exhibition.getLabelSequence(), exhibition.getHideLabel(),
				exhibition.getLabelAlignment(), exhibition.getLabelDistance(),
				exhibition.getLabelIndent(), exhibition.getStartX(),
				exhibition.getStartY(), exhibition.getMatWidth(),
				exhibition.getMatColor(), exhibition.getFrameWidth(),
				exhibition.getFrameColor(), exhibition.getGroundColor(),
				exhibition.getHorizonColor(), exhibition.getCeilingColor(),
				exhibition.getAudio(), exhibition.getOutputFolder(),
				exhibition.getFtpDir(), exhibition.getIsFtp(),
				exhibition.getPageName(), exhibition.getApplySharpening(),
				exhibition.getRadius(), exhibition.getAmount(),
				exhibition.getThreshold(), exhibition.getAddWatermark(),
				exhibition.getContactName(), exhibition.getEmail(),
				exhibition.getWebUrl(), exhibition.getCopyright(),
				exhibition.getLogo(), exhibition.getInfoPlatePosition(),
				exhibition.getHideCredits(), exhibition.getJpegQuality(),
				exhibition.getScalingMethod(), exhibition.getLastAccessDate(),
				exhibition.getPerspective());
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		Core.getCore().getDbManager().safeTransaction(null, exhibition);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		transferValues(redo, exhibition);
		return execute(monitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		redo = cloneExhibition(exhibition);
		transferValues(backup, exhibition);
		return execute(monitor, info);
	}

	public static void transferValues(ExhibitionImpl from, ExhibitionImpl to) {
		to.setAddWatermark(from.getAddWatermark());
		to.setAmount(from.getAmount());
		to.setApplySharpening(from.getApplySharpening());
		to.setAudio(from.getAudio());
		to.setCeilingColor(from.getCeilingColor());
		to.setContactName(from.getContactName());
		to.setCopyright(from.getCopyright());
		to.setDefaultDescription(from.getDefaultDescription());
		to.setDefaultViewingHeight(from.getDefaultViewingHeight());
		to.setDescription(from.getDescription());
		to.setEmail(from.getEmail());
		to.setFrameColor(from.getFrameColor());
		to.setFrameWidth(from.getFrameWidth());
		to.setFtpDir(from.getFtpDir());
		to.setGridSize(from.getGridSize());
		to.setGroundColor(from.getGroundColor());
		to.setHideCredits(from.getHideCredits());
		to.setHideLabel(from.getHideLabel());
		to.setHorizonColor(from.getHorizonColor());
		to.setInfo(from.getInfo());
		to.setInfoPlatePosition(from.getInfoPlatePosition());
		to.setIsFtp(from.getIsFtp());
		to.setJpegQuality(from.getJpegQuality());
		to.setKeyword(from.getKeyword());
		to.setLabelFontFamily(from.getLabelFontFamily());
		to.setLabelFontSize(from.getLabelFontSize());
		to.setLabelSequence(from.getLabelSequence());
		to.setLogo(from.getLogo());
		to.setMatColor(from.getMatColor());
		to.setMatWidth(from.getMatWidth());
		to.setName(from.getName());
		to.setOutputFolder(from.getOutputFolder());
		to.setPageName(from.getPageName());
		to.setRadius(from.getRadius());
		to.setScalingMethod(from.getScalingMethod());
		to.setShowGrid(from.getShowGrid());
		to.setSnapToGrid(from.getSnapToGrid());
		to.setStartX(from.getStartX());
		to.setStartY(from.getStartY());
		to.setThreshold(from.getThreshold());
		to.setVariance(from.getVariance());
		to.setWebUrl(from.getWebUrl());
	}

}