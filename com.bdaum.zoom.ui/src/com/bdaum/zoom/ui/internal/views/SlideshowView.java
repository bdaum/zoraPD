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
 * (c) 2009-2017 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.views;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.UndoContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.piccolo2d.PCamera;
import org.piccolo2d.PNode;
import org.piccolo2d.event.PBasicInputEventHandler;
import org.piccolo2d.event.PInputEvent;
import org.piccolo2d.extras.swt.PSWTCanvas;
import org.piccolo2d.extras.swt.PSWTHandle;
import org.piccolo2d.extras.swt.PSWTImage;
import org.piccolo2d.extras.swt.PSWTPath;
import org.piccolo2d.extras.swt.PSWTText;
import org.piccolo2d.extras.util.PLocator;
import org.piccolo2d.util.PBounds;
import org.piccolo2d.util.PDimension;

import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.slideShow.Slide;
import com.bdaum.zoom.cat.model.group.slideShow.SlideImpl;
import com.bdaum.zoom.cat.model.group.slideShow.SlideShowImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.ISpellCheckingService;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.css.CSSProperties;
import com.bdaum.zoom.css.internal.CssActivator;
import com.bdaum.zoom.image.ImageUtilities;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.dialogs.EditSlideDialog;
import com.bdaum.zoom.ui.internal.dialogs.SectionBreakDialog;
import com.bdaum.zoom.ui.internal.dialogs.SelectSlideDialog;
import com.bdaum.zoom.ui.internal.dialogs.SlideshowEditDialog;
import com.bdaum.zoom.ui.internal.job.DecorateJob;
import com.bdaum.zoom.ui.internal.operations.SlideshowPropertiesOperation;
import com.bdaum.zoom.ui.internal.widgets.AbstractHandle;
import com.bdaum.zoom.ui.internal.widgets.GalleryPanEventHandler;
import com.bdaum.zoom.ui.internal.widgets.GreekedPSWTText;
import com.bdaum.zoom.ui.internal.widgets.IAugmentedTextField;
import com.bdaum.zoom.ui.internal.widgets.PSWTAssetThumbnail;
import com.bdaum.zoom.ui.internal.widgets.PSWTSectionBreak;
import com.bdaum.zoom.ui.internal.widgets.PTextHandler;
import com.bdaum.zoom.ui.internal.widgets.TextEventHandler;
import com.bdaum.zoom.ui.internal.widgets.TextField;
import com.bdaum.zoom.ui.internal.widgets.ZPSWTImage;

@SuppressWarnings("restriction")
public class SlideshowView extends AbstractPresentationView {

	private static final Point DRAGTOLERANCE = new Point(25, 25);

	public class EditCaptionOperation extends AbstractOperation {

		private final SlideImpl slide;
		private final String text;
		private final IAugmentedTextField textField;
		private String oldCaption;

		public EditCaptionOperation(SlideImpl slide, String text, IAugmentedTextField textField) {
			super(Messages.getString("SlideshowView.edit_caption_undo")); //$NON-NLS-1$
			this.slide = slide;
			this.text = text;
			this.textField = textField;
		}

		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			oldCaption = slide.getCaption();
			doSetCaption(text);
			return Status.OK_STATUS;
		}

		private void doSetCaption(String t) {
			slide.setCaption(t);
			storeSafelyAndUpdateIndex(null, slide, slide.getAsset());
		}

		@Override
		public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			doSetCaption(text);
			doSetTextField(text);
			return Status.OK_STATUS;
		}

		@Override
		public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			doSetCaption(oldCaption);
			doSetTextField(oldCaption);
			return Status.OK_STATUS;
		}

		private void doSetTextField(String t) {
			textField.setText(t);
		}

	}

	public class EditSlideOperation extends AbstractOperation {

		public final static int FADEIN = 0;
		public final static int DURATION = 1;
		public final static int FADEOUT = 2;
		public final static int DELAY = 3;
		public static final int ZOOM = 4;
		public static final int ZOOMPOS = 5;
		public static final int EDIT_SLIDE = 100;
		public static final int EDIT_SECTION_BREAK = 101;
		private final PSlide pslide;
		private final SlideImpl slide;
		private SlideImpl backup;
		private SlideImpl redo;
		private final int mode;
		private boolean indexChange;

		public EditSlideOperation(PSlide pslide, SlideImpl slide, SlideImpl backup, int mode) {
			super(Messages.getString("SlideshowView.change_slide_layout_undo")); //$NON-NLS-1$
			switch (mode) {
			case FADEIN:
				setLabel(Messages.getString("SlideshowView.change_fadein")); //$NON-NLS-1$
				break;
			case DURATION:
				setLabel(Messages.getString("SlideshowView.change_dur")); //$NON-NLS-1$
				break;
			case FADEOUT:
				setLabel(Messages.getString("SlideshowView.change_fadeout")); //$NON-NLS-1$
				break;
			case DELAY:
				setLabel(Messages.getString("SlideshowView.change_delay")); //$NON-NLS-1$
				break;
			case ZOOM:
				setLabel(Messages.getString("SlideshowView.change_zoom")); //$NON-NLS-1$
				break;
			case ZOOMPOS:
				setLabel(Messages.getString("SlideshowView.change_zoomdir")); //$NON-NLS-1$
				break;
			case EDIT_SLIDE:
				setLabel(Messages.getString("SlideshowView.edit_slide_undo")); //$NON-NLS-1$
				break;
			case EDIT_SECTION_BREAK:
				setLabel(Messages.getString("SlideshowView.edit_section_break_undo")); //$NON-NLS-1$
				break;
			}
			this.pslide = pslide;
			this.slide = slide;
			this.backup = backup;
			this.mode = mode;
			indexChange = (backup.getCaption() == null && slide.getCaption() != null && !slide.getCaption().isEmpty())
					|| (backup.getCaption() != null && !backup.getCaption().equals(slide.getCaption()))
					|| (backup.getDescription() == null && slide.getDescription() != null
							&& !slide.getDescription().isEmpty())
					|| (backup.getDescription() != null && !backup.getDescription().equals(slide.getDescription()));
		}

		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			String assetId = slide.getAsset();
			storeSafelyAndUpdateIndex(null, slide, indexChange ? assetId : null);
			switch (mode) {
			case FADEIN:
			case DURATION:
			case FADEOUT:
			case DELAY:
			case ZOOM:
			case ZOOMPOS:
				pslide.layout();
				break;
			default:
				pslide.update(slide, Core.getCore().getDbManager().obtainAsset(assetId));
				break;
			}
			return Status.OK_STATUS;
		}

		@Override
		public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			slide.setCaption(redo.getCaption());
			slide.setSequenceNo(redo.getSequenceNo());
			slide.setDescription(redo.getDescription());
			slide.setLayout(redo.getLayout());
			slide.setDelay(redo.getDelay());
			slide.setFadeIn(redo.getFadeIn());
			slide.setDuration(redo.getDuration());
			slide.setFadeOut(redo.getFadeOut());
			slide.setEffect(redo.getEffect());
			slide.setZoom(redo.getZoom());
			slide.setZoomX(redo.getZoomX());
			slide.setZoomY(redo.getZoomY());
			slide.setAsset(redo.getAsset());
			return execute(monitor, info);
		}

		@Override
		public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			redo = cloneSlide(slide);
			slide.setCaption(backup.getCaption());
			slide.setSequenceNo(backup.getSequenceNo());
			slide.setDescription(backup.getDescription());
			slide.setLayout(backup.getLayout());
			slide.setDelay(backup.getDelay());
			slide.setFadeIn(backup.getFadeIn());
			slide.setDuration(backup.getDuration());
			slide.setFadeOut(backup.getFadeOut());
			slide.setEffect(backup.getEffect());
			slide.setZoom(backup.getZoom());
			slide.setZoomX(backup.getZoomX());
			slide.setZoomY(backup.getZoomY());
			slide.setAsset(backup.getAsset());
			return execute(monitor, info);
		}

	}

	public class CreateBreakOperation extends AbstractOperation {

		private final SlideImpl sectionSlide;
		private PSlide added;
		private final Point2D at;

		public CreateBreakOperation(SlideImpl sectionSlide, Point2D at) {
			super(Messages.getString("SlideshowView.create_section_break_undo")); //$NON-NLS-1$
			this.sectionSlide = sectionSlide;
			this.at = at;
		}

		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			int pos = 0;
			int index = 0;
			List<PSlide> trail = new ArrayList<PSlide>(slides.size());
			ListIterator<?> it = slideBar.getChildrenIterator();
			double x = at.getX();
			while (it.hasNext()) {
				PSlide pslide = (PSlide) it.next();
				if (pslide.getOffset().getX() >= x)
					trail.add(pslide);
				else {
					pos += pslide.slide.getDelay() + pslide.slide.getDuration();
					++index;
				}
			}
			int oldPos = pos;
			int oldIndex = index;
			SlideShowImpl show = getSlideShow();
			if (show != null) {
				++index;
				sectionSlide.setFadeIn(slideshow.getFading());
				sectionSlide.setFadeOut(slideshow.getFading());
				sectionSlide.setDelay(slideshow.getFading());
				sectionSlide.setDuration(slideshow.getDuration());
				int effect = show.getEffect();
				if (effect == Constants.SLIDE_TRANSITION_RANDOM)
					effect = (int) (Math.random() * Constants.SLIDE_TRANSITION_N);
				sectionSlide.setEffect(effect);
				sectionSlide.setZoom(0);
				sectionSlide.setSequenceNo(index);
				if (index >= slides.size()) {
					slides.add(sectionSlide);
					show.addEntry(sectionSlide.getStringId());
				} else {
					slides.add(index, sectionSlide);
					show.getEntry().add(index, sectionSlide.getStringId());
				}
				sectionSlide.setSlideShow_entry_parent(show.getStringId());
				pos += sectionSlide.getDelay();
				added = makeSlide(pos, sectionSlide);
				pos += sectionSlide.getDuration();
				List<Object> toBeStored = new ArrayList<Object>(2);
				toBeStored.add(sectionSlide);
				toBeStored.add(show);
				storeSafelyAndUpdateIndex(null, toBeStored, null);
			}
			double iWidth = (pos - oldPos) * factor;
			int diff = index - oldIndex;
			if (diff != 0)
				for (PSlide pslide : trail) {
					pslide.offset(iWidth, 0);
					pslide.slide.setSequenceNo(pslide.slide.getSequenceNo() + diff);
				}
			updateActions(false);
			return Status.OK_STATUS;
		}

		@Override
		public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			added = null;
			return execute(monitor, info);
		}

		@Override
		public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			if (added != null) {
				SlideShowImpl show = getSlideShow();
				if (show != null) {
					doRemoveSlide(show, added);
					storeSafelyAndUpdateIndex(added.slide, show, added.slide.getAsset());
					updateActions(false);
				}
			}
			return Status.OK_STATUS;
		}

	}

	public class SetTimeCursorOperation extends AbstractOperation {

		private final Point2D to;
		private Point2D from;

		public SetTimeCursorOperation(Point2D to) {
			super(Messages.getString("SlideshowView.set_time_cursor_undo")); //$NON-NLS-1$
			this.to = to;
		}

		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			from = timeCursor.getOffset();
			timeCursor.setOffset(to.getX(), 0);
			return Status.OK_STATUS;
		}

		@Override
		public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			timeCursor.setOffset(to.getX(), 0);
			return Status.OK_STATUS;
		}

		@Override
		public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			timeCursor.setOffset(from.getX(), 0);
			return Status.OK_STATUS;
		}

	}

	public class MoveSlideOperation extends AbstractOperation {

		private final PSlide moved;
		private final Point2D oldOffset;
		private final Point2D newOffset;
		private Point2D newUndoOffset;
		private Point2D oldUndoOffset;

		public MoveSlideOperation(PSlide moved, Point2D oldOffset, Point2D newOffset) {
			super(Messages.getString("SlideshowView.move_slide_undo")); //$NON-NLS-1$
			this.moved = moved;
			this.oldOffset = oldOffset;
			this.newOffset = newOffset;
		}

		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			newUndoOffset = oldOffset;
			newUndoOffset.setLocation(
					newUndoOffset.getX() + (moved.slide.getDelay() + moved.slide.getDuration()) * factor,
					newUndoOffset.getY());
			doMove(oldOffset, newOffset);
			oldUndoOffset = moved.getOffset();
			return Status.OK_STATUS;
		}

		private void doMove(Point2D from, Point2D to) {
			final SlideShowImpl show = getSlideShow();
			if (show != null) {
				double w = doRemoveSlide(show, moved);
				double x = to.getX();
				if (from.getX() < x)
					x -= w;
				int pos = 0;
				int index = 0;
				List<PSlide> trail = new ArrayList<PSlide>(slides.size());
				ListIterator<?> it = slideBar.getChildrenIterator();
				while (it.hasNext()) {
					PSlide pslide = (PSlide) it.next();
					if (pslide.getOffset().getX() >= x)
						trail.add(pslide);
					else {
						pos += pslide.slide.getDelay() + pslide.slide.getDuration();
						++index;
					}
				}
				int oldPos = pos;
				int oldIndex = index;
				SlideImpl slide = moved.slide;
				moved.setSequenceNo(++index);
				if (index >= slides.size()) {
					slides.add(slide);
					show.addEntry(slide.getStringId());
				} else {
					slides.add(index - 1, slide);
					show.getEntry().add(index - 1, slide.getStringId());
				}
				pos += slide.getDelay();
				slideBar.addChild(moved);
				moved.setOffset(pos * factor, from.getY());
				pos += slide.getDuration();
				double iWidth = (pos - oldPos) * factor;
				int diff = index - oldIndex;
				for (PSlide pslide : trail) {
					pslide.setOffset(pslide.getOffset().getX() + iWidth, 0);
					pslide.setSequenceNo(pslide.slide.getSequenceNo() + diff);
				}
				Core.getCore().getDbManager().safeTransaction(null, show);
				updateActions(false);
			}
		}

		@Override
		public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			doMove(oldOffset, newOffset);
			return Status.OK_STATUS;
		}

		@Override
		public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			doMove(oldUndoOffset, newUndoOffset);
			return Status.OK_STATUS;
		}

	}

	public class DeleteSlideOperation extends AbstractOperation {

		private PSlide deleted;
		private Point2D offset;
		private SlideImpl slide;

		public DeleteSlideOperation(PSlide deleted, Point2D oldOffset) {
			super(Messages.getString("SlideshowView.delete_slide_undo")); //$NON-NLS-1$
			this.deleted = deleted;
			offset = oldOffset;
		}

		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			slide = deleted.slide;
			SlideShowImpl show = getSlideShow();
			if (show != null) {
				doRemoveSlide(show, deleted);
				storeSafelyAndUpdateIndex(slide, show, slide.getAsset());
				updateActions(false);
			}
			return Status.OK_STATUS;
		}

		@Override
		public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			return execute(monitor, info);
		}

		@Override
		public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			double x = offset.getX();
			int pos = 0;
			int index = 0;
			List<PSlide> trail = new ArrayList<PSlide>(slides.size());
			ListIterator<?> it = slideBar.getChildrenIterator();
			while (it.hasNext()) {
				PSlide pslide = (PSlide) it.next();
				if (pslide.getOffset().getX() >= x)
					trail.add(pslide);
				else {
					pos += pslide.slide.getDelay() + pslide.slide.getDuration();
					++index;
				}
			}
			int oldPos = pos;
			SlideShowImpl show = getSlideShow();
			if (show != null) {
				index++;
				if (index >= slides.size()) {
					slides.add(slide);
					show.addEntry(slide.getStringId());
				} else {
					slides.add(index, slide);
					show.getEntry().add(index, slide.getStringId());
				}
				slide.setSlideShow_entry_parent(show.getStringId());
				pos += slide.getDelay();
				deleted = makeSlide(pos, slide);
				pos += slide.getDuration();
				List<Object> toBeStored = new ArrayList<Object>(2);
				toBeStored.add(slide);
				toBeStored.add(show);
				storeSafelyAndUpdateIndex(null, toBeStored, slide.getAsset());
				double iWidth = (pos - oldPos) * factor;
				for (PSlide pslide : trail) {
					pslide.offset(iWidth, 0);
					pslide.setSequenceNo(pslide.slide.getSequenceNo() + 1);
				}
				updateActions(false);
			}
			return Status.OK_STATUS;
		}
	}

	public class DropAssetOperation extends AbstractOperation {

		private final AssetSelection selection;
		private final Point2D position;
		private List<PSlide> added;
		private final boolean replace;
		private DeleteSlideOperation deleteOp;

		public DropAssetOperation(AssetSelection selection, Point2D position, boolean replace) {
			super(Messages.getString("SlideshowView.drop_images_undo")); //$NON-NLS-1$
			this.selection = selection;
			this.position = position;
			this.replace = replace;
			added = new ArrayList<PSlide>(selection.size());
		}

		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			double x = position.getX();
			List<PSlide> trail = new ArrayList<PSlide>(slides.size());
			PSlide cand = null;
			int pos = 0;
			int index = 0;
			lp: while (true) {
				ListIterator<?> it = slideBar.getChildrenIterator();
				while (it.hasNext()) {
					PSlide pslide = (PSlide) it.next();
					Point2D offset = pslide.getOffset();
					double p = offset.getX();
					if (p >= x)
						trail.add(pslide);
					else {
						if (replace && cand == null && p + pslide.getFullBoundsReference().width >= x) {
							cand = pslide;
							pos = 0;
							index = 0;
							trail.clear();
							deleteOp = new DeleteSlideOperation(cand, cand.getOffset());
							deleteOp.execute(monitor, info);
							x -= cand.getFullBoundsReference().width;
							continue lp;
						}
						++index;
						pos += pslide.slide.getDelay() + pslide.slide.getDuration();
					}
				}
				break;
			}
			int oldPos = pos;
			int oldIndex = index;
			SlideShowImpl show = null;
			List<Object> toBeStored = new ArrayList<Object>(selection.size() + 1);
			List<String> assetIds = new ArrayList<String>(selection.size());
			for (Asset asset : selection) {
				if (!accepts(asset))
					continue;
				show = getSlideShow();
				if (show == null)
					break;
				++index;
				int effect = show.getEffect();
				if (effect == Constants.SLIDE_TRANSITION_RANDOM)
					effect = (int) (Math.random() * Constants.SLIDE_TRANSITION_N);
				String assetId = asset.getStringId();
				Slide replaced = cand == null ? null : cand.slide;
				SlideImpl slide = replaced != null
						? new SlideImpl(UiUtilities.createSlideTitle(asset), index, null, replaced.getLayout(),
								replaced.getDelay(), replaced.getFadeIn(), replaced.getDuration(),
								replaced.getFadeOut(), replaced.getEffect(), replaced.getZoom(), replaced.getZoomX(),
								replaced.getZoomY(), replaced.getNoVoice(), asset.getSafety(), assetId)
						: new SlideImpl(UiUtilities.createSlideTitle(asset), index, null, Constants.SLIDE_NO_THUMBNAILS,
								show.getFading(), show.getFading(), show.getDuration(), show.getFading(), effect,
								show.getZoom(), 0, 0, false, asset.getSafety(), assetId);
				cand = null;
				if (index >= slides.size()) {
					slides.add(slide);
					show.addEntry(slide.getStringId());
				} else {
					slides.add(index, slide);
					show.getEntry().add(index, slide.getStringId());
				}
				slide.setSlideShow_entry_parent(show.getStringId());
				pos += slide.getDelay();
				PSlide pslide = makeSlide(pos, slide);
				pos += slide.getDuration();
				toBeStored.add(slide);
				assetIds.add(assetId);
				added.add(pslide);
			}
			double iWidth = (pos - oldPos) * factor;
			int diff = index - oldIndex;
			double w = pos;
			for (PSlide pslide : trail) {
				pslide.offset(iWidth, 0);
				pslide.setSequenceNo(pslide.slide.getSequenceNo() + diff);
				double x2 = pslide.getOffset().getX() + pslide.getBoundsReference().getWidth();
				if (x2 > w)
					w = x2;
			}
			updateSlidebarBounds(w);
			if (show != null) {
				toBeStored.add(show);
				storeSafelyAndUpdateIndex(null, toBeStored, assetIds);
			}
			updateActions(false);
			return Status.OK_STATUS;
		}

		@Override
		public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			added.clear();
			return execute(monitor, info);
		}

		@Override
		public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			SlideShowImpl show = getSlideShow();
			if (show != null) {
				List<Object> toBeDeleted = new ArrayList<Object>(selection.size());
				List<String> assetIds = new ArrayList<String>(selection.size());
				for (PSlide pslide : added) {
					doRemoveSlide(show, pslide);
					toBeDeleted.add(pslide.slide);
					assetIds.add(pslide.slide.getAsset());
				}
				if (deleteOp != null)
					deleteOp.undo(monitor, info);
				storeSafelyAndUpdateIndex(toBeDeleted, show, assetIds);
				updateActions(false);
			}
			return Status.OK_STATUS;
		}
	}

	public class SlideShowDecorateJob extends DecorateJob {

		private Color penColor;

		public SlideShowDecorateJob() {
			super(Messages.getString("SlideshowView.decorate_slideshow")); //$NON-NLS-1$
		}

		@Override
		protected boolean mayRun() {
			return slideBar != null && slideBar.getChildrenCount() > 0;
		}

		@Override
		protected void doRun(IProgressMonitor monitor) {
			final Display display = canvas.getDisplay();
			IDbManager dbManager = Core.getCore().getDbManager();
			for (Object child : slideBar.getChildrenReference().toArray()) {
				if (child instanceof PSlide && isVisible() && mayRun()) {
					final PSlide pslide = (PSlide) child;
					SlideImpl slide = pslide.slide;
					try {
						String assetId = slide.getAsset();
						if (assetId != null) {
							AssetImpl asset = dbManager.obtainAsset(assetId);
							if (asset != null && !display.isDisposed()
									&& !pslide.caption.getPenColor().equals(penColor)) {
								switch (volumeManager.determineFileState(asset)) {
								case IVolumeManager.REMOTE:
									if (remoteColor != null)
										penColor = remoteColor;
									break;
								case IVolumeManager.OFFLINE:
									if (offlineColor != null)
										penColor = offlineColor;
									break;
								default:
									penColor = titleForegroundColor;
									break;
								}
								display.asyncExec(() -> {
									if (!canvas.isDisposed())
										pslide.caption.setPenColor(penColor);
								});
							}
						}
					} catch (Exception e) {
						// just ignore
					}
				}
				if (monitor.isCanceled())
					break;
			}
		}
	}

	class PSlide extends PNode implements PTextHandler, IPresentationItem {

		/**
		 *
		 */
		private static final long serialVersionUID = -8619376689449783925L;

		private final class SlideHandle extends AbstractHandle {

			private static final long serialVersionUID = -2915474593495856476L;

			class SlideLocator extends PLocator {

				private static final long serialVersionUID = 1228742743568586994L;

				@Override
				public double locateX() {
					return SlideHandle.this.locateX();
				}

				@Override
				public double locateY() {
					return SlideHandle.this.locateY();
				}
			}

			private int style;
			private double oldScale;
			private double shiftX;
			private double shiftY;
			private int size;
			private SlideImpl backup;

			private SlideHandle(int style, int size) {
				super(style == EditSlideOperation.ZOOMPOS ? SWT.CURSOR_SIZEALL : SWT.CURSOR_SIZEWE, null);
				setLocator(new SlideLocator());
				this.style = style;
				this.size = size;
				switch (style) {
				case EditSlideOperation.FADEIN:
					setTooltip(Messages.getString("SlideshowView.fade_in_tooltip")); //$NON-NLS-1$
					setPathToPolyline(new float[] { 0f, size, size / 2 }, new float[] { 0f, 0f, -size });
					setPaint(new Color(0, 224, 0));
					break;
				case EditSlideOperation.DELAY:
					setTooltip(Messages.getString("SlideshowView.delay_tooltip")); //$NON-NLS-1$
					setPathToPolyline(new float[] { size, 0, size }, new float[] { -size / 2, 0f, size / 2 });
					setPaint(new Color(224, 224, 224));
					break;
				case EditSlideOperation.DURATION:
					setTooltip(Messages.getString("SlideshowView.duration_tooltip")); //$NON-NLS-1$
					setPathToPolyline(new float[] { 0f, size, 0 }, new float[] { -size / 2, 0f, size / 2 });
					setPaint(new Color(224, 224, 0));
					break;
				case EditSlideOperation.ZOOM:
					setTooltip(Messages.getString("SlideshowView.zoom_amount")); //$NON-NLS-1$
					setPathToPolyline(new float[] { 0f, size, size / 2 },
							new float[] { size / 2f, size / 2f, size + size / 2f });
					setPaint(new Color(0, 0, 224));
					break;
				case EditSlideOperation.ZOOMPOS:
					setTooltip(Messages.getString("SlideshowView.zoom_dir")); //$NON-NLS-1$
					setPathToEllipse(size / 2, size / 2, size, size);
					setPaint(new Color(0, 0, 224));
					break;
				default:
					setTooltip(Messages.getString("SlideshowView.fade_out_tooltip")); //$NON-NLS-1$
					setPathToPolyline(new float[] { 0f, size, size / 2 },
							new float[] { size / 2f, size / 2f, size + size / 2f });
					setPaint(new Color(224, 0, 0));
					break;
				}
				addInputEventListener(new PBasicInputEventHandler() {
					@Override
					public void mouseEntered(PInputEvent event) {
						PSlide.this.raiseToTop();
					}
				});
			}

			@Override
			public void startHandleDrag(Point2D localPoint, PInputEvent event) {
				backup = cloneSlide(slide);
				oldScale = pImage.getScale();
				shiftX = 0;
				shiftY = 0;
				PSlide.this.raiseToTop();
			}

			@Override
			public void dragHandle(PDimension localDimension, PInputEvent event) {
				double width = localDimension.getWidth();
				double height = localDimension.getHeight();
				shiftX += width;
				shiftY += height;
				double shift = width / factor;
				switch (style) {
				case EditSlideOperation.FADEIN:
					int fadein = (int) (slide.getFadeIn() - shift);
					if (fadein >= 0) {
						slide.setFadeIn(fadein);
						startLine.setPathToPolyline(new float[] { (float) (-fadein * factor), 0f },
								new float[] { slideSize, 0f });
					}
					break;
				case EditSlideOperation.DELAY:
					int delay = (int) (slide.getDelay() + shift);
					if (delay >= 0) {
						slide.setDelay(delay);
						PSlide.this.offset(width, 0);
					}
					break;
				case EditSlideOperation.DURATION:
					int duration = (int) (slide.getDuration() + shift);
					if (duration >= 0) {
						slide.setDuration(duration);
						double x = pImage.getOffset().getX();
						pImage.setScale(oldScale * duration / backup.getDuration());
						pImage.setOffset(x, slideSize - pImage.getFullBoundsReference().getHeight());
					}
					break;
				case EditSlideOperation.ZOOM:
					slide.setZoom(
							Math.max(0, Math.min(100, (int) (slide.getZoom() + width * 100d / pImage.getWidth()))));
					break;
				case EditSlideOperation.ZOOMPOS:
					slide.setZoomX(
							Math.max(-100, Math.min(100, (int) (slide.getZoomX() + width * 200d / pImage.getWidth()))));
					slide.setZoomY(Math.max(-100,
							Math.min(100, (int) (slide.getZoomY() + height * 200d / pImage.getHeight()))));
					break;
				default:
					int fadeout = (int) (slide.getFadeOut() + shift);
					if (fadeout >= 0) {
						slide.setFadeOut(fadeout);
						endLine.setPathToPolyline(new float[] { 0f, (float) (fadeout * factor) },
								new float[] { 0f, slideSize });
					}
					break;
				}
				legend.setText(createLegendText(slide));
				invalidatePaint();
				relocateHandle();
				if (style == EditSlideOperation.ZOOM || style == EditSlideOperation.ZOOMPOS)
					connectZoomPoints();
			}

			@Override
			public void endHandleDrag(Point2D localPoint, PInputEvent event) {
				double shift = shiftX / factor;
				switch (style) {
				case EditSlideOperation.FADEIN:
					int fadein = (int) (backup.getFadeIn() - shift);
					if (fadein >= 0) {
						int maxFadein = slide.getDelay();
						int index = slide.getSequenceNo();
						if (index > 0)
							maxFadein += slides.get(index - 1).getDuration();
						slide.setFadeIn(Math.min(fadein, maxFadein));
					}
					break;
				case EditSlideOperation.DELAY:
					int delay = (int) (backup.getDelay() + shift);
					if (delay >= 0) {
						slide.setDelay(delay);
						PSlide.this.offset(shiftX, 0);
					}
					break;
				case EditSlideOperation.DURATION:
					int duration = (int) (backup.getDuration() + shift);
					if (duration >= 0)
						slide.setDuration(duration);
					break;
				case EditSlideOperation.ZOOM:
					slide.setZoom(Math.max(0, Math.min(100,
							(int) (backup.getZoom() + shiftX * 100d / pImage.getGlobalBounds().getWidth()))));
					break;
				case EditSlideOperation.ZOOMPOS:
					slide.setZoomX(Math.max(-100, Math.min(100,
							(int) (backup.getZoomX() + shiftX * 100d / pImage.getGlobalBounds().getWidth()))));
					slide.setZoomY(Math.max(-100, Math.min(100,
							(int) (backup.getZoomY() + shiftY * 100d / pImage.getGlobalBounds().getHeight()))));
					break;
				default:
					int maxFadeout = 60000;
					int index = slide.getSequenceNo() + 1;
					if (index < slides.size()) {
						SlideImpl next = slides.get(index);
						maxFadeout = next.getDelay();
						maxFadeout += Math.min(maxFadeout / 3, next.getDuration() / 2);
					}
					int fadeout = (int) (backup.getFadeOut() + shift);
					if (fadeout >= 0)
						slide.setFadeOut(Math.min(fadeout, maxFadeout));
					break;
				}
				performOperation(new EditSlideOperation(PSlide.this, slide, backup, style));
				setPickedNode(this);
				updateActions(false);
				updateHandles();
			}

			public double locateX() {
				switch (style) {
				case EditSlideOperation.FADEIN:
					return -slide.getFadeIn() * factor;
				case EditSlideOperation.DELAY:
					return -size / 2d;
				case EditSlideOperation.DURATION:
					return pImage.getFullBoundsReference().getWidth() + size / 2d;
				case EditSlideOperation.ZOOM:
					return slide.getZoom() / 100d * pImage.getWidth();
				case EditSlideOperation.ZOOMPOS:
					return (100 + slide.getZoomX()) / 200d * pImage.getWidth();
				default:
					return pImage.getFullBoundsReference().getWidth() + slide.getFadeOut() * factor;
				}

			}

			public double locateY() {
				switch (style) {
				case EditSlideOperation.DELAY:
				case EditSlideOperation.DURATION:
					return (pImage.getOffset().getY() + slideSize) / 2;
				case EditSlideOperation.ZOOM:
					return pImage.getY() - size;
				case EditSlideOperation.ZOOMPOS:
					return (100 + slide.getZoomY()) / 200d * pImage.getHeight();
				default:
					return slideSize;
				}
			}
		}

		private SlideImpl slide;
		private PSWTImage pImage;
		private PSWTHandle startPoint;
		private PSWTHandle endPoint;
		private PSWTPath startLine;
		private PSWTPath endLine;
		private PSWTHandle midPoint;
		private PSWTText legend;
		private PSWTText seqNo;
		private TextField caption;
		private double totalWidth;
		private SlideHandle delayPoint;
		private final int csize = 12;
		private SlideHandle zoomPoint;
		private SlideHandle zoomDirPoint;
		private PSWTPath zoomLine;

		public PSlide(final PSWTCanvas canvas, SlideImpl slide, int pos) {
			this.slide = slide;
			final IDbManager dbManager = Core.getCore().getDbManager();
			totalWidth = (slide.getDuration() + slide.getFadeIn() + slide.getFadeOut()) * factor;
			double fadein = slide.getFadeIn() * factor;
			double fadeout = slide.getFadeOut() * factor;
			setPickable(false);
			// Image
			String assetId = slide.getAsset();
			pImage = assetId == null ? new PSWTSectionBreak(canvas, slide)
					: new PSWTAssetThumbnail(canvas, SlideshowView.this, dbManager.obtainAsset(assetId));
			PBounds bounds = pImage.getBoundsReference();
			double scale = setSizeAndPos(slide, bounds);
			addChild(pImage);
			pImage.setPickable(true);
			setBounds(-fadein, 0, totalWidth, slideSize);
			// fadein
			startPoint = new SlideHandle(EditSlideOperation.FADEIN, csize);
			startPoint.setPickable(true);
			addChild(startPoint);
			startLine = new PSWTPath();
			startLine.setPathToPolyline(new float[] { (float) -fadein, 0f }, new float[] { slideSize, 0f });
			startLine.setStrokeColor(new Color(0, 192, 0));
			startLine.setPickable(false);
			addChild(startLine);
			// delay
			delayPoint = new SlideHandle(EditSlideOperation.DELAY, csize);
			delayPoint.setPickable(true);
			addChild(delayPoint);
			// duration
			midPoint = new SlideHandle(EditSlideOperation.DURATION, csize);
			midPoint.setPickable(true);
			addChild(midPoint);
			// zoom
			if (assetId != null) {
				zoomPoint = new SlideHandle(EditSlideOperation.ZOOM, csize);
				zoomPoint.setPickable(true);
				pImage.addChild(zoomPoint);
				zoomDirPoint = new SlideHandle(EditSlideOperation.ZOOMPOS, csize);
				zoomDirPoint.setTransparency(0.5f);
				zoomDirPoint.setPickable(true);
				pImage.addChild(zoomDirPoint);
				zoomLine = new PSWTPath();
				zoomLine.setTransparency(0.5f);
				connectZoomPoints();
				zoomLine.setStrokeColor(new Color(0, 0, 224));
				zoomLine.setPickable(false);
				pImage.addChild(zoomLine);
			}
			// fadeout
			endPoint = new SlideHandle(EditSlideOperation.FADEOUT, csize);
			endPoint.setPickable(true);
			addChild(endPoint);
			endLine = new PSWTPath();
			endLine.setPathToPolyline(new float[] { 0f, (float) fadeout }, new float[] { 0f, slideSize });
			double w = bounds.getWidth() * scale;
			endLine.setOffset(w, 0);
			endLine.setStrokeColor(new Color(192, 0, 0));
			endLine.setPickable(false);
			addChild(endLine);
			// caption
			RGB rgb = canvas.getForeground().getRGB();
			Color penColor = new Color(rgb.red, rgb.green, rgb.blue);
			seqNo = new GreekedPSWTText(createSequenceNo(slide), new Font("Arial", //$NON-NLS-1$
					Font.BOLD, 9));
			seqNo.setGreekThreshold(5);
			seqNo.setTransparent(true);
			seqNo.setPenColor(penColor);
			seqNo.setOffset(csize, slideSize + csize);
			seqNo.setPickable(true);
			addChild(seqNo);
			caption = createTextLine(this, slide.getCaption(), 5, (int) (w - 2 * csize - seqNo.getWidth()),
					(int) (seqNo.getWidth() + csize), slideSize + csize, titleForegroundColor, null, "Arial", Font.BOLD, //$NON-NLS-1$
					9, ISpellCheckingService.TITLEOPTIONS, SWT.SINGLE);
			// legend
			legend = new GreekedPSWTText(createLegendText(slide), new Font("Arial", //$NON-NLS-1$
					Font.PLAIN, 9));
			legend.setGreekThreshold(5);
			legend.setTransparent(true);
			legend.setPenColor(penColor);
			legend.setOffset(csize, slideSize + 3 * csize);
			legend.setPickable(true);
			addChild(legend);
			// position
			setOffset(pos * factor, 0);
			installHandleEventHandlers(pImage, false, false, this);
			addInputEventListener(new PBasicInputEventHandler() {
				@Override
				public void mousePressed(PInputEvent event) {
					setPickedNode(event.getPickedNode());
					if (getAdapter(IPresentationItem.class) instanceof PSlide)
						event.getPickedNode().raiseToTop();
				}
			});
			pImage.addInputEventListener(new PBasicInputEventHandler() {
				public void mousePressed(PInputEvent event) {
					setPickedNode(event.getPickedNode());
				}

				@Override
				public void mouseReleased(PInputEvent event) {
					if (event.getClickCount() == 2 && editLegend(PSlide.this))
						canvas.repaint();
				}
			});
			updateHandles();
		}

		public void updateHandles() {
			boolean visible = slide.getZoom() > 0;
			if (zoomDirPoint != null)
				zoomDirPoint.setVisible(visible);
			if (zoomLine != null)
				zoomLine.setVisible(visible);
		}

		protected void connectZoomPoints() {
			PBounds bounds = zoomPoint.getBoundsReference();
			double x1 = bounds.getCenterX();
			double y1 = bounds.getCenterY();
			bounds = zoomDirPoint.getBoundsReference();
			double x2 = bounds.getCenterX();
			double y2 = bounds.getCenterY();
			zoomLine.setPathToPolyline(new float[] { (float) x1, (float) x2 }, new float[] { (float) y1, (float) y2 });
		}

		public void update(SlideImpl slideImpl, Asset asset) {
			// Point2D offset = pImage.getOffset();
			PNode removedImage = removeChild(pImage);
			pImage.getImage().dispose();
			images.remove(pImage.getImage());
			Display display = canvas.getDisplay();
			if (slideImpl.getAsset() != null) {
				pImage = new ZPSWTImage(canvas,
						ImageUtilities.loadThumbnail(display, asset == null ? null : asset.getJpegThumbnail(),
								Ui.getUi().getDisplayCMS(), SWT.IMAGE_JPEG, true));
				while (removedImage.getChildrenCount() > 0)
					pImage.addChild(removedImage.removeChild(0));
			} else
				pImage = new PSWTSectionBreak(canvas, slideImpl);
			images.add(pImage.getImage());
			PBounds bounds = pImage.getBoundsReference();
			setSizeAndPos(slideImpl, bounds);
			addChild(0, pImage);
			pImage.setPickable(true);
			pImage.lowerToBottom();
			caption.setText(slideImpl.getCaption());
		}

		private double setSizeAndPos(SlideImpl slide, PBounds bounds) {
			double scale = (slideSize / bounds.getWidth()) * slide.getDuration() / slideshow.getDuration();
			pImage.scale(scale);
			pImage.setOffset(0, slideSize - bounds.getHeight() * scale);
			return scale;
		}

		private void layout() {
			legend.setText(createLegendText(slide));
			totalWidth = (slide.getDuration() + slide.getFadeIn() + slide.getFadeOut()) * factor;
			PBounds bounds = pImage.getGlobalBounds();
			setSizeAndPos(slide, bounds);
			startLine.setPathToPolyline(new float[] { (float) -(slide.getFadeIn() * factor), 0f },
					new float[] { slideSize, 0 });
			endLine.setPathToPolyline(new float[] { 0, (float) (slide.getFadeOut() * factor) },
					new float[] { 0, slideSize });
			endLine.setOffset(bounds.width, 0);
			connectZoomPoints();
			caption.setOffset(seqNo.getWidth() + csize, slideSize + csize);
			legend.setOffset(csize, slideSize + 3 * csize);
			realignSlides(slide.getSequenceNo());
			invalidatePaint();
			relocateAllHandles();
		}

		private String createLegendText(SlideImpl slide) {
			af.setMaximumFractionDigits(1);
			StringBuilder sb = new StringBuilder();
			sb.append(Messages.getString("SlideshowView.delay")).append(af.format(slide.getDelay() / 1000d)) //$NON-NLS-1$
					.append(" sec").append('\n'); //$NON-NLS-1$
			sb.append(Messages.getString("SlideshowView.duration")) //$NON-NLS-1$
					.append(af.format(slide.getDuration() / 1000d)).append(" sec") //$NON-NLS-1$
					.append('\n');
			sb.append(Messages.getString("SlideshowView.fade_in")).append(af.format(slide.getFadeIn() / 1000d)) //$NON-NLS-1$
					.append(" sec").append('\n'); //$NON-NLS-1$
			sb.append(Messages.getString("SlideshowView.fade_out")).append( //$NON-NLS-1$
					af.format(slide.getFadeOut() / 1000d)).append(" sec").append('\n'); //$NON-NLS-1$
			sb.append(Messages.getString("SlideshowView.effect")) //$NON-NLS-1$
					.append(SlideshowEditDialog.EFFECTS[slide.getEffect()]).append('\n');
			sb.append(slide.getZoom() > 0 ? NLS.bind(Messages.getString("SlideshowView.zoom_legend"), //$NON-NLS-1$
					new Object[] { slide.getZoom(), slide.getZoomX(), slide.getZoomY() })
					: Messages.getString("SlideshowView.no_zoom")); //$NON-NLS-1$
			return sb.toString();
		}

		void relocateAllHandles() {
			startPoint.relocateHandle();
			delayPoint.relocateHandle();
			midPoint.relocateHandle();
			zoomPoint.relocateHandle();
			zoomDirPoint.relocateHandle();
			endPoint.relocateHandle();
			connectZoomPoints();
		}

		public void processTextEvent(TextField focus) {
			String text = focus.getText();
			String capt = slide.getCaption();
			if (capt == null || !capt.equals(text))
				performOperation(new EditCaptionOperation(slide, text, focus));
		}

		public void setSequenceNo(int i) {
			slide.setSequenceNo(i);
			seqNo.setText(createSequenceNo(slide));
			caption.setOffset(seqNo.getWidth() + csize, caption.getOffset().getY());
		}

		private String createSequenceNo(Slide s) {
			return s.getSequenceNo() + " - "; //$NON-NLS-1$
		}

		public void setPenColor(Color color) {
			seqNo.setPenColor(color);
			legend.setPenColor(color);
		}

		public void setTitleColor(Color color) {
			caption.setPenColor(color);
			caption.setSelectedBgColor(selectionBackgroundColor);
		}

		public void setBackgroundColor(Color color) {
			// do nothing
		}

		public void updateColors(Color selectedPaint) {
			// do nothing
		}

		public String getAssetId() {
			return slide.getAsset();
		}

	}

	public static final String SLIDES_PERSPECTIVE = "com.bdaum.zoom.PresentationPerspective"; //$NON-NLS-1$
	public static final String ID = "com.bdaum.zoom.ui.SlideshowView"; //$NON-NLS-1$
	private static final String LAST_SLIDESHOW = "com.bdaum.zoom.lastSlideshow"; //$NON-NLS-1$

	final static NumberFormat af = (NumberFormat.getNumberInstance());

	private SlideShowImpl slideshow;
	private List<SlideImpl> slides = new ArrayList<SlideImpl>(200);
	private int slideSize = 200;
	private int defaultDuration = 7000;
	private double factor = (double) slideSize / defaultDuration;
	private PSWTPath slideBar;
	public Image shadowImage;
	private Action playAction;
	private PSWTPath timeBar;
	private PSWTPath timeCursor;
	protected Point2D positionX;

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		if (memento != null) {
			String lastSelection = memento.getString(LAST_SLIDESHOW);
			SlideShowImpl obj = Core.getCore().getDbManager().obtainById(SlideShowImpl.class, lastSelection);
			if (obj != null) {
				defaultDuration = obj.getDuration();
				factor = ((double) slideSize) / defaultDuration;
			}
		}
	}

	@Override
	public void saveState(IMemento memento) {
		if (memento != null && slideshow != null)
			memento.putString(LAST_SLIDESHOW, slideshow.getStringId());
		super.saveState(memento);
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		canvas.setData(CSSProperties.ID, CSSProperties.SLIDESHOW);
		undoContext = new UndoContext() {
			@Override
			public String getLabel() {
				return Messages.getString("SlideshowView.slideshow_undo_context"); //$NON-NLS-1$
			}
		};
		slideBar = PSWTPath.createRectangle(0, 0, 1000 * slideSize, slideSize);
		slideBar.setOffset(0d, slideSize / 2d);
		surface.addChild(slideBar);
		timeBar = PSWTPath.createRectangle(0, 0, 1000 * slideSize, 25);
		timeBar.setOffset(0d, 2.2d * slideSize);
		timeBar.setPickable(true);
		double tensec = slideSize * 10000d / defaultDuration;
		double off = tensec;
		double limit = 1000 * slideSize;
		int sec = 10;
		while (off < limit) {
			int minute = sec / 60;
			String secs = String.valueOf((sec % 60));
			if (secs.length() < 2)
				secs = "0" + secs; //$NON-NLS-1$
			String text = minute + "'" + secs + "\""; //$NON-NLS-1$ //$NON-NLS-2$
			PSWTText legend = new PSWTText(text, new Font("Arial", Font.PLAIN, //$NON-NLS-1$
					9));
			legend.setGreekThreshold(5);
			legend.setTransparent(true);
			legend.setOffset(off, 5);
			legend.setPickable(false);
			timeBar.addChild(legend);
			sec += 10;
			off += tensec;
		}
		timeCursor = PSWTPath.createRectangle(0, 1, 3, 23);
		timeCursor.setPickable(false);
		Color red = new Color(255, 0, 0);
		timeCursor.setStrokeColor(red);
		timeCursor.setPaint(red);
		timeBar.addChild(timeCursor);
		surface.addChild(timeBar);
		setColor(canvas);
		addWheelListener(0.2d, 3d);
		textEventHandler = new TextEventHandler(selectionBackgroundColor);
		PBasicInputEventHandler eventHandler = new PBasicInputEventHandler() {

			private Point2D mousePos;

			@Override
			public void keyPressed(PInputEvent event) {
				if (textEventHandler.hasFocus())
					textEventHandler.keyPressed(event);
				else
					pan(event);
			}

			@Override
			public void mousePressed(PInputEvent event) {
				mousePos = event.getPosition();
				setPickedNode(event.getPickedNode());
			}

			@Override
			public void mouseDragged(PInputEvent event) {
				textEventHandler.mouseDragged(event);
			}

			@Override
			public void mouseReleased(PInputEvent event) {
				positionX = null;
				textEventHandler.mouseReleased(event);
				PNode pickedNode = getPickedNode();
				if (event.getClickCount() == 2) {
					PNode par = pickedNode.getParent();
					if (par instanceof PSlide) {
						if (editLegend((PSlide) par))
							canvas.repaint();
					} else {
						PCamera camera = event.getCamera();
						if (oldTransform == null) {
							oldTransform = camera.getViewTransform();
							camera.setViewTransform(new AffineTransform());
						} else {
							camera.setViewTransform(oldTransform);
							resetTransform();
						}
					}
				} else if (event.getClickCount() == 1) {
					if (event.isLeftMouseButton()) {
						if (mousePos != null && pickedNode == timeBar) {
							double x = event.getPosition().getX();
							if (Math.abs(x - mousePos.getX()) < 5)
								timeCursor.setOffset(x, 0);
						} else if (pickedNode instanceof GreekedPSWTText && editLegend((PSlide) pickedNode.getParent()))
							canvas.repaint();
					}
				}
				if (event.isRightMouseButton()) {
					positionX = event.getPosition();
					positionRelativeToCamera = event.getPositionRelativeTo(canvas.getCamera());
				}
				mousePos = null;
			}

		};
		canvas.getRoot().getDefaultInputManager().setKeyboardFocus(eventHandler);
		canvas.addInputEventListener(eventHandler);
		setPanAndZoomHandlers();
		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(canvas, HelpContextIds.SLIDESHOW_VIEW);
		// Drop-Unterstützung
		addDropListener(canvas);
		// Hover
		installHoveringController();
		// Actions
		makeActions();
		installListeners();
		hookContextMenu();
		contributeToActionBars();
		if (slideshow != null)
			setInput(slideshow);
		addCatalogListener();
		setDecorator(canvas, new SlideShowDecorateJob());
		updateActions(false);
	}

	private void setPanAndZoomHandlers() {
		setPanAndZoomHandlers(-4, -14);
	}

	protected boolean editLegend(PSlide pslide) {
		if (pslide != null) {
			SlideImpl slide = pslide.slide;
			SlideImpl backup = cloneSlide(slide);
			EditSlideDialog dialog = new EditSlideDialog(getSite().getShell(), slide);
			if (dialog.open() == Window.OK
					&& performOperation(new EditSlideOperation(pslide, slide, backup, EditSlideOperation.EDIT_SLIDE))
							.isOK()) {
				pslide.invalidatePaint();
				pslide.relocateAllHandles();
				pslide.updateHandles();
				return true;
			}
		}
		return false;
	}

	private Action setTimerCursorAction;
	private Action createBreakAction;
	private Action editBreakAction;
	private Map<Slide, PSlide> slideMap = new HashMap<Slide, SlideshowView.PSlide>(300);

	@Override
	protected void updatePresentation(Collection<? extends Asset> assets) {
		Map<String, Asset> amap = new HashMap<String, Asset>(assets.size());
		for (Asset a : assets)
			amap.put(a.getStringId(), a);
		ListIterator<?> it = slideBar.getChildrenIterator();
		while (it.hasNext()) {
			PSlide pslide = (PSlide) it.next();
			Asset asset = amap.get(pslide.slide.getAsset());
			if (asset != null) {
				pslide.update(pslide.slide, asset);
				break;
			}
		}
	}

	protected PSlide makeSlide(int pos, SlideImpl slide) {
		PSlide pslide = new PSlide(canvas, slide, pos);
		pslide.setPenColor(foregroundColor);
		pslide.setTitleColor(titleForegroundColor);
		slideBar.addChild(pslide);
		slideMap.put(slide, pslide);
		return pslide;
	}

	protected void deleteSlide(PSlide moved, Point2D oldOffset) {
		if (getSlideShow() != null)
			performOperation(new DeleteSlideOperation(moved, oldOffset));
	}

	protected double doRemoveSlide(SlideShowImpl show, PSlide moved) {
		slideBar.removeChild(moved);
		SlideImpl slide = moved.slide;
		int sequenceNo = slide.getSequenceNo();
		double width = (slide.getDuration() + slide.getDelay()) * factor;
		ListIterator<?> it = slideBar.getChildrenIterator();
		while (it.hasNext()) {
			PSlide pslide = (PSlide) it.next();
			int q = pslide.slide.getSequenceNo();
			if (q >= sequenceNo) {
				pslide.offset(-width, 0);
				pslide.setSequenceNo(q - 1);
			}
		}
		slides.remove(sequenceNo - 1);
		show.removeEntry(slide.getStringId());
		return width;
	}

	protected void moveSlide(PSlide moved, Point2D oldOffset, Point2D newOffset) {
		performOperation(new MoveSlideOperation(moved, oldOffset, newOffset));
	}

	private void realignSlides(int from) {
		int pos = 0;
		int i = 0;
		int[] positions = new int[slides.size()];
		for (SlideImpl slide : slides) {
			pos += slide.getDelay();
			positions[i++] = pos;
			pos += slide.getDuration();
		}
		ListIterator<?> iterator = slideBar.getChildrenIterator();
		while (iterator.hasNext()) {
			PSlide pslide = (PSlide) iterator.next();
			int sequenceNo = pslide.slide.getSequenceNo();
			if (sequenceNo >= from)
				pslide.setOffset(positions[sequenceNo - 1] * factor, pslide.getOffset().getY());
		}
	}

	protected SlideShowImpl getSlideShow() {
		if (slideshow == null && !dbIsReadonly()) {
			SlideshowEditDialog dialog = new SlideshowEditDialog(getSite().getShell(), null, null,
					Messages.getString("SlideshowView.create_slideshow"), false, false); //$NON-NLS-1$
			if (dialog.open() == Window.OK)
				slideshow = dialog.getResult();
		}
		return slideshow;
	}

	@Override
	public void updateActions(boolean force) {
		if (playAction != null && (isVisible() || force)) {
			super.updateActions(force);
			playAction.setEnabled(!slides.isEmpty());
		}
	}

	@Override
	protected void fillLocalToolBar(IToolBarManager manager) {
		super.fillLocalToolBar(manager);
		manager.add(playAction);
	}

	@Override
	protected void fillLocalPullDown(IMenuManager manager) {
		manager.add(playAction);
		super.fillLocalPullDown(manager);
	}

	@Override
	protected void fillContextMenu(IMenuManager manager) {
		updateActions(true);
		boolean writable = !dbIsReadonly();
		manager.add(setTimerCursorAction);
		manager.add(new Separator());
		manager.add(gotoExhibitAction);
		PSlide pslide = null;
		Object item = getAdapter(IPresentationItem.class);
		if (item instanceof PSlide) {
			pslide = (PSlide) item;
			addCommonContextActions(manager);
		}
		if (writable) {
			manager.add(new Separator());
			if (pslide != null && pslide.slide.getAsset() == null)
				manager.add(editBreakAction);
			manager.add(createBreakAction);
		}
		super.fillContextMenu(manager);
	}

	@Override
	protected void makeActions() {
		super.makeActions();
		playAction = new Action(Messages.getString("SlideshowView.play"), Icons.play.getDescriptor()) { //$NON-NLS-1$
			@Override
			public void run() {
				SlideShowPlayer player = new SlideShowPlayer();
				player.init(getSite().getWorkbenchWindow(), slideshow, slides, false);
				player.open((int) (timeCursor.getOffset().getX() / slideSize * defaultDuration));
			}
		};
		playAction.setToolTipText(Messages.getString("SlideshowView.play_slideshow")); //$NON-NLS-1$
		propertiesAction = new Action(Messages.getString("SlideshowView.properties"), Icons.slideshow.getDescriptor()) { //$NON-NLS-1$
			@Override
			public void run() {
				SlideShowImpl backup = SlideshowPropertiesOperation.cloneSlideshow(slideshow);
				SlideshowEditDialog dialog = new SlideshowEditDialog(getSite().getShell(), null, slideshow,
						slideshow.getName(), false, true);
				if (dialog.open() == Window.OK) {
					slideshow = dialog.getResult();
					performOperation(new SlideshowPropertiesOperation(backup, slideshow));
					setInput(slideshow);
				}
			}
		};
		propertiesAction.setToolTipText(Messages.getString("SlideshowView.edit_slideshow_properties")); //$NON-NLS-1$
		setTimerCursorAction = new Action(Messages.getString("SlideshowView.set_time_cursor")) { //$NON-NLS-1$
			@Override
			public void run() {
				performOperation(new SetTimeCursorOperation(positionX));
			}
		};
		setTimerCursorAction.setToolTipText(Messages.getString("SlideshowView.set_time_cursor_tooltip")); //$NON-NLS-1$
		gotoExhibitAction = new Action(Messages.getString("SlideshowView.goto_slide")) { //$NON-NLS-1$
			@Override
			public void run() {
				SelectSlideDialog dialog = new SelectSlideDialog(getSite().getShell(), slides);
				dialog.create();
				org.eclipse.swt.graphics.Point pos = canvas.toDisplay((int) positionRelativeToCamera.getX(),
						(int) positionRelativeToCamera.getY());
				pos.x += 10;
				pos.y += 10;
				dialog.getShell().setLocation(pos);
				if (dialog.open() == Window.OK) {
					SlideImpl selectedSlide = dialog.getResult();
					ListIterator<?> it = slideBar.getChildrenIterator();
					PSlide previous = null;
					while (it.hasNext()) {
						PSlide pslide = (PSlide) it.next();
						if (pslide.slide == selectedSlide) {
							double x = pslide.getOffset().getX();
							if (previous != null)
								x = (previous.getOffset().getX() + x) / 2;
							PCamera camera = canvas.getCamera();
							camera.translateView(camera.getViewBounds().getX() - x, 0);
							return;
						}
						previous = pslide;
					}
				}
			}
		};
		gotoExhibitAction.setToolTipText(Messages.getString("SlideshowView.goto_slide_tooltip")); //$NON-NLS-1$
		createBreakAction = new Action(Messages.getString("SlideshowView.create_section_break")) { //$NON-NLS-1$
			@Override
			public void run() {
				SectionBreakDialog dialog = new SectionBreakDialog(getSite().getShell(), null);
				if (dialog.open() == Window.OK)
					performOperation(new CreateBreakOperation(dialog.getResult(), positionX));
			}
		};
		createBreakAction.setToolTipText(Messages.getString("SlideshowView.create_section_break_tooltip")); //$NON-NLS-1$
		editBreakAction = new Action(Messages.getString("SlideshowView.edit_section_break")) { //$NON-NLS-1$
			@Override
			public void run() {
				PSlide pslide = (PSlide) getPickedNode().getParent();
				SlideImpl slide = pslide.slide;
				SlideImpl backup = cloneSlide(slide);
				SectionBreakDialog dialog = new SectionBreakDialog(getSite().getShell(), slide);
				if (dialog.open() == Window.OK)
					performOperation(
							new EditSlideOperation(pslide, slide, backup, EditSlideOperation.EDIT_SECTION_BREAK));
			}
		};
		editBreakAction.setToolTipText(Messages.getString("SlideshowView.edit_section_break_tooltip")); //$NON-NLS-1$
		exhibitPropertiesAction = new Action(Messages.getString("SlideshowView.slide_properties")) { //$NON-NLS-1$
			@Override
			public void run() {
				editLegend((PSlide) getAdapter(IPresentationItem.class));
			}
		};
		exhibitPropertiesAction.setToolTipText(Messages.getString("SlideshowView.properties_tooltip")); //$NON-NLS-1$

	}

	public static SlideImpl cloneSlide(SlideImpl slide) {
		return new SlideImpl(slide.getCaption(), slide.getSequenceNo(), slide.getDescription(), slide.getLayout(),
				slide.getDelay(), slide.getFadeIn(), slide.getDuration(), slide.getFadeOut(), slide.getEffect(),
				slide.getZoom(), slide.getZoomX(), slide.getZoomY(), slide.getNoVoice(), slide.getSafety(),
				slide.getAsset());
	}

	@Override
	protected void setColor(Control control) {
		CssActivator.getDefault().applyExtendedStyle(control, this);
		Color newPaint = UiUtilities.getAwtBackground(control, null);
		surface.setPaint(newPaint);
		slideBar.setPaint(newPaint);
		timeBar.setPaint(newPaint);
		slideBar.setStrokeColor(selectionBackgroundColor);
		for (ListIterator<?> it = slideBar.getChildrenIterator(); it.hasNext();) {
			Object child = it.next();
			if (child instanceof PSWTText)
				((PSWTText) child).setPenColor(selectionBackgroundColor);
			else if (child instanceof TextField)
				((TextField) child).setPenColor(selectionBackgroundColor);
			else if (child instanceof PSlide) {
				((PSlide) child).setPenColor(foregroundColor);
				((PSlide) child).setTitleColor(titleForegroundColor);
			}
		}
		timeBar.setStrokeColor(selectionBackgroundColor);
		for (ListIterator<?> it = timeBar.getChildrenIterator(); it.hasNext();) {
			Object child = it.next();
			if (child instanceof PSWTText)
				((PSWTText) child).setPenColor(selectionBackgroundColor);
		}
	}

	@Override
	public boolean selectionChanged() {
		return true;
	}

	@Override
	public void setInput(IdentifiableObject presentation) {
		super.setInput(presentation);
		if (presentation instanceof SlideShowImpl) {
			this.slideshow = (SlideShowImpl) presentation;
			setPartName(slideshow.getName());
			beginTask(slideshow.getEntry().size());
			defaultDuration = slideshow.getDuration();
			factor = ((double) slideSize) / defaultDuration;
			double x = 0;
			int pos = 0;
			for (SlideImpl slide : Core.getCore().getDbManager().obtainByIds(SlideImpl.class, slideshow.getEntry())) {
				slides.add(slide);
				slide.setSequenceNo(slides.size());
				pos += slide.getDelay();
				PSlide pSlide = makeSlide(pos, slide);
				pos += slide.getDuration();
				x = pSlide.getOffset().getX() + pSlide.getBoundsReference().width;
				progressBar.worked(1);
			}
			updateSlidebarBounds(x);
			updateActions(false);
			endTask();
		} else
			setPartName(Messages.getString("SlideshowView.slide_show")); //$NON-NLS-1$
	}

	private void updateSlidebarBounds(double w) {
		w += 2 * slideSize;
		if (w > slideBar.getBoundsReference().getWidth())
			slideBar.setWidth(w);
		updateSurfaceBounds(w, -1);
	}

	@Override
	protected void cleanUp() {
		try {
			slideBar.removeAllChildren();
		} catch (SWTException e) {
			// do nothing
		}
		slides.clear();
		super.cleanUp();
	}

	@Override
	protected void dropAssets(ISelection selection, org.eclipse.swt.graphics.Point point, Point2D position,
			boolean replace) {
		performOperation(new DropAssetOperation((AssetSelection) selection, position, replace));
		getSite().getPage().activate(this);
	}

	@Override
	protected PNode[] getWorkArea() {
		return new PNode[] { slideBar };
	}

	@Override
	protected int getPanDirection() {
		return GalleryPanEventHandler.HOR;
	}

	@Override
	protected int getHoriztontalMargins() {
		return slideSize;
	}

	public void removeSlides(List<SlideImpl> slideList) {
		for (SlideImpl slide : slideList) {
			PSlide pSlide = slideMap.get(slide);
			deleteSlide(pSlide, pSlide.getOffset());
		}
	}

	@Override
	public Object getContent() {
		return slideshow;
	}

	@Override
	protected AbstractHandleDragSequenceEventHandler createDragSequenceEventHandler(PNode node, boolean relative,
			boolean restricted, PNode presentationObject) {
		return new AbstractPresentationView.AbstractHandleDragSequenceEventHandler(node, relative, restricted,
				presentationObject) {

			@Override
			protected void moveToFront(PNode object) {
				updateActions(false);
			}

			@Override
			protected void drop(PInputEvent event, PNode pnode) {
				PSlide pslide = (PSlide) pnode.getParent();
				Point2D newOffset = pslide.getOffset();
				PBounds ibounds = pnode.getFullBoundsReference();
				Point2D point = new Point2D.Double(newOffset.getX() + ibounds.x + ibounds.width / 2,
						newOffset.getY() + ibounds.y + ibounds.height / 2);
				double x = point.getX();
				double y = point.getY();
				pnode.localToGlobal(point);
				Point2D canvasPosition = event.getCanvasPosition();
				if (canvasPosition.getY() < 0)
					deleteSlide(pslide, oldOffset);
				else {
					surface.parentToLocal(point);
					if (slideBar.getBoundsReference().contains(x, y))
						moveSlide(pslide, oldOffset, newOffset);
					else
						pslide.setOffset(oldOffset);
				}
			}

			@Override
			public Point getDragTolerance() {
				return DRAGTOLERANCE;
			}
		};
	}

	@Override
	protected PBounds getClientAreaReference() {
		return surface.getBoundsReference();
	}

	public static boolean accepts(Asset asset) {
		if (asset == null)
			return false;
		IMediaSupport mediaSupport = CoreActivator.getDefault().getMediaSupport(asset.getFormat());
		return mediaSupport == null || mediaSupport.testProperty(IMediaSupport.SLIDESHOW);
	}

	@Override
	protected void refreshAfterHistoryEvent(IUndoableOperation operation) {
		if (operation instanceof SlideshowPropertiesOperation)
			setInput(slideshow);
	}

	@Override
	protected PNode findExhibit(Point point, Point2D position) {
		double x = position.getX();
		ListIterator<?> it = slideBar.getChildrenIterator();
		while (it.hasNext()) {
			PSlide pslide = (PSlide) it.next();
			double p = pslide.getOffset().getX();
			if (p < x) {
				p += pslide.getFullBoundsReference().width;
				if (p >= x)
					return pslide;
			}
		}
		return null;
	}

	@Override
	public String getId() {
		return ID;
	}

}
