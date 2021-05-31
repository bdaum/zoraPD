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

import com.bdaum.zoom.cat.model.group.slideShow.SlideShowImpl;
import com.bdaum.zoom.core.Core;

public class SlideshowPropertiesOperation extends AbstractOperation {

	private final SlideShowImpl backup, slideshow;
	private SlideShowImpl redo;

	public SlideshowPropertiesOperation(SlideShowImpl backup, SlideShowImpl slideshow) {
		super(Messages.SlideshowPropertiesOperation_set_slideshow_properties);
		this.backup = backup;
		this.slideshow = slideshow;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		Core.getCore().getDbManager().safeTransaction(null, slideshow);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		transferValues(redo, slideshow);
		return execute(monitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		redo = cloneSlideshow(slideshow);
		transferValues(backup, slideshow);
		return execute(monitor, info);
	}

	public static void transferValues(SlideShowImpl from, SlideShowImpl to) {
		to.setName(from.getName());
		to.setDescription(from.getDescription());
		to.setFromPreview(from.getFromPreview());
		to.setDuration(from.getDuration());
		to.setEffect(from.getEffect());
		to.setFading(from.getFading());
		to.setTitleDisplay(from.getTitleDisplay());
		to.setTitleContent(from.getTitleContent());
		to.setAdhoc(from.getAdhoc());
		to.setSkipDublettes(from.getSkipDublettes());
		to.setZoom(from.getZoom());
	}

	public static SlideShowImpl cloneSlideshow(SlideShowImpl show) {
		return new SlideShowImpl(show.getName(), show.getDescription(), show.getFromPreview(), show.getDuration(),
				show.getEffect(), show.getFading(), show.getZoom(), show.getTitleDisplay(), show.getTitleContent(),
				show.getTitleScheme(), show.getTitleTransparency(), show.getColorScheme(), show.getAdhoc(), show.getSkipDublettes(),
				show.getVoiceNotes(), show.getLastAccessDate(), show.getPerspective());
	}

}
