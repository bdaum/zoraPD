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
 * (c) 2014 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ui.internal.operations;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.bdaum.zoom.cat.model.group.webGallery.WebGalleryImpl;
import com.bdaum.zoom.core.Core;

public class WebGalleryPropertiesOperation extends AbstractOperation {

	private final WebGalleryImpl backup;
	private final WebGalleryImpl gallery;
	private WebGalleryImpl redo;

	public WebGalleryPropertiesOperation(WebGalleryImpl backup,
			WebGalleryImpl gallery) {
		super(Messages.WebGalleryPropertiesOperation_set_web_gallery_properties);
		this.backup = backup;
		this.gallery = gallery;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		Core.getCore().getDbManager().safeTransaction(null, gallery);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		transferValues(redo, gallery);
		return execute(monitor, info);
	}

	public static void transferValues(WebGalleryImpl from, WebGalleryImpl to) {
		to.setTemplate(from.getTemplate());
		to.setName(from.getName());
		to.setLogo(from.getLogo());
		to.setHtmlDescription(from.getHtmlDescription());
		to.setDescription(from.getDescription());
		to.setHideHeader(from.getHideHeader());
		to.setOpacity(from.getOpacity());
		to.setPadding(from.getPadding());
		to.setThumbSize(from.getThumbSize());
		to.setDownloadText(from.getDownloadText());
		to.setHideDownload(from.getHideDownload());
		to.setCopyright(from.getCopyright());
		to.setAddWatermark(from.getAddWatermark());
		to.setShowMeta(from.getShowMeta());
		to.setContactName(from.getContactName());
		to.setEmail(from.getEmail());
		to.setWebUrl(from.getWebUrl());
		to.setHideFooter(from.getHideFooter());
		to.setBgImage(from.getBgImage());
		to.setBgRepeat(from.getBgRepeat());
		to.setBgColor(from.getBgColor());
		to.setShadeColor(from.getShadeColor());
		to.setBorderColor(from.getBorderColor());
		to.setLinkColor(from.getLinkColor());
		to.setTitleFont(from.getTitleFont());
		to.setSectionFont(from.getSectionFont());
		to.setCaptionFont(from.getCaptionFont());
		to.setDescriptionFont(from.getDescriptionFont());
		to.setFooterFont(from.getFooterFont());
		to.setControlsFont(from.getControlsFont());
		to.setSelectedEngine(from.getSelectedEngine());
		to.setOutputFolder(from.getOutputFolder());
		to.setFtpDir(from.getFtpDir());
		to.setIsFtp(from.getIsFtp());
		to.setPageName(from.getPageName());
		to.setPoweredByText(from.getPoweredByText());
		to.setApplySharpening(from.getApplySharpening());
		to.setRadius(from.getRadius());
		to.setAmount(from.getAmount());
		to.setThreshold(from.getThreshold());
		to.setHeadHtml(from.getHeadHtml());
		to.setTopHtml(from.getTopHtml());
		to.setFooterHtml(from.getFooterHtml());
		to.setJpegQuality(from.getJpegQuality());
		to.setScalingMethod(from.getScalingMethod());

	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		redo = cloneGallery(gallery);
		transferValues(backup, gallery);
		return execute(monitor, info);
	}

	public static WebGalleryImpl cloneGallery(WebGalleryImpl show) {
		return new WebGalleryImpl(show.getTemplate(), show.getName(),
				show.getLogo(), show.getHtmlDescription(),
				show.getDescription(), show.getHideHeader(), show.getOpacity(),
				show.getPadding(), show.getThumbSize(), show.getDownloadText(),
				show.getHideDownload(), show.getCopyright(),
				show.getAddWatermark(), show.getShowMeta(),
				show.getContactName(), show.getEmail(), show.getWebUrl(),
				show.getHideFooter(), show.getBgImage(), show.getBgRepeat(),
				show.getBgColor(), show.getShadeColor(), show.getBorderColor(),
				show.getLinkColor(), show.getTitleFont(),
				show.getSectionFont(), show.getCaptionFont(),
				show.getDescriptionFont(), show.getFooterFont(),
				show.getControlsFont(), show.getSelectedEngine(),
				show.getOutputFolder(), show.getFtpDir(), show.getIsFtp(),
				show.getPageName(), show.getPoweredByText(),
				show.getApplySharpening(), show.getRadius(), show.getAmount(),
				show.getThreshold(), show.getHeadHtml(), show.getTopHtml(),
				show.getFooterHtml(), show.getJpegQuality(),
				show.getScalingMethod(), show.getLastAccessDate(), show.getPerspective());
	}

}
