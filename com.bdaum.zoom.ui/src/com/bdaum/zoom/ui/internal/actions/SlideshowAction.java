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

package com.bdaum.zoom.ui.internal.actions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IWorkbenchWindow;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.group.slideShow.SlideImpl;
import com.bdaum.zoom.cat.model.group.slideShow.SlideShowImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IAssetProvider;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.FileNameExtensionFilter;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.dialogs.SlideshowEditDialog;
import com.bdaum.zoom.ui.internal.views.SlideShowPlayer;

@SuppressWarnings("restriction")
public class SlideshowAction extends AbstractMultiMediaAction {

	private IAdaptable adaptable;

	public SlideshowAction(IWorkbenchWindow window, String label, String tooltip, ImageDescriptor image,
			IAdaptable adaptable) {
		super(window, label, image, QueryField.PHOTO);
		this.adaptable = adaptable;
		setToolTipText(tooltip);
	}

	@Override
	public void run() {
		BusyIndicator.showWhile(window.getShell().getDisplay(), () -> doRun());
	}

	private void doRun() {
		IAssetProvider assetProvider = Core.getCore().getAssetProvider();
		if (assetProvider == null)
			return;
		List<Asset> selectedAssets = adaptable.getAdapter(AssetSelection.class).getAssets();
		if (selectedAssets == null || selectedAssets.isEmpty())
			selectedAssets = assetProvider.getAssets();
		if (!selectedAssets.isEmpty()) {
			IDialogSettings settings = UiActivator.getDefault().getDialogSettings(SlideshowEditDialog.SETTINGSID);
			int duration = 7000;
			int effect = Constants.SLIDE_TRANSITION_RANDOM;
			int zoom = 0;
			int fading = 1000;
			int titleDisplay = 1500;
			int titleContent = Constants.SLIDE_TITLEONLY;
			boolean fromPreview = false;
			try {
				duration = settings.getInt(SlideshowEditDialog.DELAY);
			} catch (NumberFormatException e) {
				// ignore
			}
			try {
				effect = settings.getInt(SlideshowEditDialog.EFFECT);
			} catch (NumberFormatException e) {
				// ignore
			}
			try {
				zoom = settings.getInt(SlideshowEditDialog.ZOOM);
			} catch (NumberFormatException e) {
				// ignore
			}
			try {
				fading = settings.getInt(SlideshowEditDialog.FADING);
			} catch (NumberFormatException e) {
				// ignore
			}
			try {
				titleDisplay = settings.getInt(SlideshowEditDialog.TITLEDISPLAY);
			} catch (NumberFormatException e) {
				// ignore
			}
			try {
				titleContent = settings.getInt(SlideshowEditDialog.TITLECONTENT);
			} catch (NumberFormatException e) {
				// ignore
			}
			try {
				fromPreview = settings.getBoolean(SlideshowEditDialog.FROMPREVIEW);
			} catch (Exception e) {
				// ignore
			}

			SlideShowImpl show = new SlideShowImpl("", "", fromPreview, duration, //$NON-NLS-1$ //$NON-NLS-2$
					effect, fading, zoom, titleDisplay, titleContent, true, true, false, new Date(), null);
			SlideshowEditDialog dialog = new SlideshowEditDialog(window.getShell(), null, show,
					Messages.SlideshowActionDelegate_adhoc_slideshow, true, false);
			List<SlideImpl> slides = createSlides(selectedAssets, show);
			SlideShowPlayer player = new SlideShowPlayer(window, show, slides, true);
			player.create();
			player.prepare(0);
			if (dialog.open() == Window.OK) {
				if (updateSlides(slides, show, dialog.getPrivacy())) {
					if (slides.isEmpty()) {
						player.close();
						return;
					}
					player.prepare(0);
				}
				player.open(0);
			} else
				player.close();
		}
	}

	private static boolean updateSlides(List<SlideImpl> slides, SlideShowImpl show, int privacy) {
		boolean filtered = false;
		int fading = show.getFading();
		int duration = show.getDuration();
		int lag = Math.min(fading / 3, duration / 2);
		int effect = show.getEffect();
		int zoom = show.getZoom();
		Iterator<SlideImpl> it = slides.iterator();
		while (it.hasNext()) {
			SlideImpl slide = it.next();
			int safety = slide.getSafety();
			if (safety > privacy) {
				it.remove();
				filtered = true;
			} else {
				slide.setDelay(fading);
				slide.setFadeIn(fading);
				slide.setDuration(duration);
				slide.setFadeOut(fading + lag);
				slide.setEffect(effect);
				slide.setZoom(zoom);
			}
		}
		return filtered;
	}

	private static List<SlideImpl> createSlides(List<Asset> selectedAssets, SlideShowImpl show) {
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				ImageConstants.getSupportedImageFileExtensionsGroups(true), true);
		List<SlideImpl> slides = new ArrayList<SlideImpl>(selectedAssets.size());
		int index = 0;
		String id = show.getStringId();
		Set<String> done = new HashSet<String>(slides.size() * 3 / 2);
		for (Asset asset : selectedAssets) {
			String fileName = Core.getFileName(asset.getUri(), true);
			if (filter.accept(fileName)) {
				String tit = asset.getTitle();
				if (tit == null || tit.isEmpty()) {
					if (show.getSkipDublettes()) {
						String name = asset.getName();
						if (done.contains(name))
							continue;
						done.add(name);
					}
					tit = fileName;
				}
				index++;
				int fading = show.getFading();
				int duration = show.getDuration();
				int lag = Math.min(fading / 3, duration / 2);
				int effect = show.getEffect();
				int zoom = show.getZoom();
				if (effect == Constants.SLIDE_TRANSITION_RANDOM)
					effect = (int) (Math.random() * Constants.SLIDE_TRANSITION_N);
				SlideImpl slide = new SlideImpl(tit, index, null, Constants.SLIDE_NO_THUMBNAILS, fading, fading,
						duration, fading + lag, effect, zoom, 0, 0, false, asset.getSafety(), asset.getStringId());
				slides.add(slide);
				show.addEntry(slide.getStringId());
				slide.setSlideShow_entry_parent(id);
			}
		}
		return slides;
	}
}
