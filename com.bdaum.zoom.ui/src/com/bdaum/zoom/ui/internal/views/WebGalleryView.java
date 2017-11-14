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
package com.bdaum.zoom.ui.internal.views;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.UndoContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;

import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.webGallery.Storyboard;
import com.bdaum.zoom.cat.model.group.webGallery.StoryboardImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebExhibit;
import com.bdaum.zoom.cat.model.group.webGallery.WebExhibitImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebGalleryImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IGalleryGenerator;
import com.bdaum.zoom.core.ISpellCheckingService;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.css.internal.CssActivator;
import com.bdaum.zoom.image.ImageUtilities;
import com.bdaum.zoom.net.core.ftp.FtpAccount;
import com.bdaum.zoom.net.core.job.TransferJob;
import com.bdaum.zoom.operations.internal.gen.AbstractGalleryGenerator;
import com.bdaum.zoom.operations.jobs.WebGalleryJob;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.dialogs.EditStoryBoardDialog;
import com.bdaum.zoom.ui.internal.dialogs.EditWebExibitDialog;
import com.bdaum.zoom.ui.internal.dialogs.SaveTemplateDialog;
import com.bdaum.zoom.ui.internal.dialogs.SelectWebExhibitDialog;
import com.bdaum.zoom.ui.internal.dialogs.WebGalleryEditDialog;
import com.bdaum.zoom.ui.internal.job.DecorateJob;
import com.bdaum.zoom.ui.internal.operations.ExhibitionPropertiesOperation;
import com.bdaum.zoom.ui.internal.operations.WebGalleryPropertiesOperation;
import com.bdaum.zoom.ui.internal.widgets.GalleryPanEventHandler;
import com.bdaum.zoom.ui.internal.widgets.GreekedPSWTText;
import com.bdaum.zoom.ui.internal.widgets.IAugmentedTextField;
import com.bdaum.zoom.ui.internal.widgets.PPanel;
import com.bdaum.zoom.ui.internal.widgets.PSWTAssetThumbnail;
import com.bdaum.zoom.ui.internal.widgets.PSWTButton;
import com.bdaum.zoom.ui.internal.widgets.PTextHandler;
import com.bdaum.zoom.ui.internal.widgets.TextEventHandler;
import com.bdaum.zoom.ui.internal.widgets.TextField;
import com.bdaum.zoom.ui.internal.widgets.ZPSWTImage;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.swt.PSWTCanvas;
import edu.umd.cs.piccolox.swt.PSWTImage;
import edu.umd.cs.piccolox.swt.PSWTText;

@SuppressWarnings("restriction")
public class WebGalleryView extends AbstractPresentationView {

	private static final Point DRAGTOLERANCE = new Point(25, 25);
	private static final java.awt.Rectangle RECT1 = new java.awt.Rectangle(0, 0, 1, 1);

	public class DeleteStoryboardOperation extends AbstractOperation {

		private PStoryboard pStoryboard;
		private Storyboard storyboard;
		private double y;
		private ArrayList<WebExhibitImpl> deletedExhibits;

		public DeleteStoryboardOperation(PStoryboard pStoryboard) {
			super(Messages.getString("WebGalleryView.delete_storyboard_undo")); //$NON-NLS-1$
			this.pStoryboard = pStoryboard;
			this.y = pStoryboard.getOffset().getY();
			this.storyboard = pStoryboard.getStoryboard();
		}

		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			WebGalleryImpl show = getGallery();
			if (show != null) {
				show.removeStoryboard(storyboard);
				List<Object> toBeDeleted = new ArrayList<Object>(storyboard.getExhibit().size() + 1);
				List<String> assetIds = new ArrayList<String>(storyboard.getExhibit().size());
				deletedExhibits = new ArrayList<WebExhibitImpl>(storyboard.getExhibit().size());
				for (WebExhibitImpl obj : Core.getCore().getDbManager().obtainByIds(WebExhibitImpl.class,
						storyboard.getExhibit())) {
					toBeDeleted.add(obj);
					deletedExhibits.add(obj);
					assetIds.add(obj.getAsset());
				}
				toBeDeleted.add(storyboard);
				storeSafelyAndUpdateIndex(toBeDeleted, show, assetIds);
				Point2D offset = pStoryboard.getOffset();
				double ydiff = pStoryboard.getHeight() + 15;
				surface.removeChild(pStoryboard);
				ListIterator<?> it = surface.getChildrenIterator();
				while (it.hasNext()) {
					Object obj = it.next();
					if (obj instanceof PStoryboard) {
						PStoryboard w = (PStoryboard) obj;
						if (w.getOffset().getY() > offset.getY())
							w.offset(0, -ydiff);
					}
				}
				storyboards.remove(pStoryboard);
				setPanAndZoomHandlers();
			}
			return Status.OK_STATUS;
		}

		@Override
		public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			return execute(monitor, info);
		}

		@Override
		public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			WebGalleryImpl show = getGallery();
			if (show != null) {
				show.addStoryboard(storyboard);
				pStoryboard = addStoryboard(storyboard, y, null);
				setColor(canvas);
				List<Object> tobeStored = new ArrayList<Object>(deletedExhibits.size() + 2);
				List<String> assetIds = new ArrayList<String>(deletedExhibits.size());
				for (WebExhibitImpl exhibit : deletedExhibits) {
					tobeStored.add(exhibit);
					assetIds.add(exhibit.getAsset());
				}
				tobeStored.add(storyboard);
				tobeStored.add(show);
				storeSafelyAndUpdateIndex(null, tobeStored, assetIds);
			}
			return Status.OK_STATUS;
		}

	}

	public class CreateStoryboardOperation extends AbstractOperation {

		private final Storyboard storyboard;
		private final int y;
		private PStoryboard added;

		public CreateStoryboardOperation(Storyboard storyboard, int y) {
			super(Messages.getString("WebGalleryView.create_storyboard_undo")); //$NON-NLS-1$
			this.storyboard = storyboard;
			this.y = y;
		}

		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			WebGalleryImpl show = getGallery();
			if (show != null) {
				show.addStoryboard(storyboard);
				added = addStoryboard(storyboard, y, null);
				setColor(canvas);
				List<Object> toBeStored = new ArrayList<Object>(2);
				toBeStored.add(storyboard);
				toBeStored.add(show);
				storeSafelyAndUpdateIndex(null, toBeStored, null);
			}
			return Status.OK_STATUS;
		}

		@Override
		public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			return execute(monitor, info);
		}

		@Override
		public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			WebGalleryImpl show = getGallery();
			if (show != null) {
				show.removeStoryboard(storyboard);
				List<Object> toBeDeleted = new ArrayList<Object>(storyboard.getExhibit().size() + 1);
				List<String> assetIds = new ArrayList<String>(storyboard.getExhibit().size());
				for (WebExhibitImpl obj : Core.getCore().getDbManager().obtainByIds(WebExhibitImpl.class,
						storyboard.getExhibit())) {
					toBeDeleted.add(obj);
					assetIds.add(obj.getAsset());
				}
				toBeDeleted.add(storyboard);
				storeSafelyAndUpdateIndex(toBeDeleted, show, assetIds);
				Point2D offset = added.getOffset();
				double ydiff = added.getHeight() + 15;
				surface.removeChild(added);
				ListIterator<?> it = surface.getChildrenIterator();
				while (it.hasNext()) {
					Object obj = it.next();
					if (obj instanceof PStoryboard) {
						PStoryboard w = (PStoryboard) obj;
						if (w.getOffset().getY() > offset.getY())
							w.offset(0, -ydiff);
					}
				}
				storyboards.remove(added);
				setPanAndZoomHandlers();
			}
			return Status.OK_STATUS;
		}
	}

	public static class EditStoryboardOperation extends AbstractOperation {

		private final StoryboardImpl storyboard;
		private final StoryboardImpl backup;
		private StoryboardImpl redo;
		private final PStoryboard pstoryboard;

		public EditStoryboardOperation(StoryboardImpl storyboard, StoryboardImpl backup, PStoryboard pstoryboard) {
			super(Messages.getString("WebGalleryView.edit_storyboard_undo")); //$NON-NLS-1$
			this.storyboard = storyboard;
			this.backup = backup;
			this.pstoryboard = pstoryboard;
		}

		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			Core.getCore().getDbManager().safeTransaction(null, storyboard);
			pstoryboard.setTitle(storyboard.getTitle());
			return Status.OK_STATUS;
		}

		@Override
		public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			storyboard.setTitle(redo.getTitle());
			storyboard.setSequenceNo(redo.getSequenceNo());
			storyboard.setDescription(redo.getDescription());
			storyboard.setImageSize(redo.getImageSize());
			storyboard.setEnlargeSmall(redo.getEnlargeSmall());
			storyboard.setShowCaptions(redo.getShowCaptions());
			storyboard.setShowDescriptions(redo.getShowDescriptions());
			storyboard.setShowExif(redo.getShowExif());
			return execute(monitor, info);
		}

		@Override
		public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			redo = new StoryboardImpl(storyboard.getTitle(), storyboard.getSequenceNo(),
					storyboard.getHtmlDescription(), storyboard.getDescription(), storyboard.getImageSize(),
					storyboard.getEnlargeSmall(), storyboard.getShowCaptions(), storyboard.getShowDescriptions(),
					storyboard.getShowExif());
			storyboard.setTitle(backup.getTitle());
			storyboard.setSequenceNo(backup.getSequenceNo());
			storyboard.setDescription(backup.getDescription());
			storyboard.setImageSize(backup.getImageSize());
			storyboard.setEnlargeSmall(backup.getEnlargeSmall());
			storyboard.setShowCaptions(backup.getShowCaptions());
			storyboard.setShowDescriptions(backup.getShowDescriptions());
			storyboard.setShowExif(backup.getShowExif());
			return execute(monitor, info);

		}
	}

	public static class EditStoryboardTextOperation extends AbstractOperation {

		public static final int CAPTION = 0;
		public static final int DESCRIPTION = 1;
		public static final int CREDITS = 2;
		public static final int DATE = 3;
		private final Storyboard storyboard;
		private final String text;
		private String oldtext;
		private final IAugmentedTextField textField;

		public EditStoryboardTextOperation(Storyboard storyboard, TextField textField) {
			super(Messages.getString("WebGalleryView.edit_storyboard_title_undo")); //$NON-NLS-1$
			this.storyboard = storyboard;
			this.text = textField.getText();
			this.textField = textField;
		}

		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			oldtext = storyboard.getTitle();
			doSetText(text);
			return Status.OK_STATUS;
		}

		@Override
		public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			doSetText(text);
			textField.setText(text);
			return Status.OK_STATUS;
		}

		@Override
		public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			doSetText(oldtext);
			textField.setText(oldtext);
			return Status.OK_STATUS;
		}

		private void doSetText(String t) {
			storyboard.setTitle(t);
			final IDbManager dbManager = Core.getCore().getDbManager();
			dbManager.safeTransaction(() -> dbManager.store(storyboard));
		}
	}

	public class EditTextOperation extends AbstractOperation {

		public static final int CAPTION = 0;
		public static final int DESCRIPTION = 1;
		private final WebExhibitImpl exhibit;
		private final String text;
		private String oldtext;
		private final IAugmentedTextField textField;
		private final int type;

		public EditTextOperation(WebExhibitImpl exhibit, String text, IAugmentedTextField textField, int type) {
			super(Messages.getString("WebGalleryView.edit_exh_undo")); //$NON-NLS-1$
			this.exhibit = exhibit;
			this.text = text;
			this.textField = textField;
			this.type = type;
		}

		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			switch (type) {
			case CAPTION:
				oldtext = exhibit.getCaption();
				break;
			case DESCRIPTION:
				oldtext = exhibit.getDescription();
				break;
			}
			doSetText(text);
			return Status.OK_STATUS;
		}

		private void doSetText(String t) {
			boolean changed = false;
			switch (type) {
			case CAPTION:
				changed = exhibit.getCaption() == null || !exhibit.getCaption().equals(t);
				exhibit.setCaption(t);
				break;
			case DESCRIPTION:
				changed = exhibit.getDescription() == null || !exhibit.getDescription().equals(t);
				exhibit.setDescription(t);
				break;
			}
			if (changed)
				storeSafelyAndUpdateIndex(null, exhibit, exhibit.getAsset());
		}

		@Override
		public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			doSetText(text);
			doSetTextField(text);
			return Status.OK_STATUS;
		}

		@Override
		public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			doSetText(oldtext);
			doSetTextField(oldtext);
			return Status.OK_STATUS;
		}

		private void doSetTextField(String t) {
			textField.setText(t);
		}

	}

	public class EditExhibitOperation extends AbstractOperation {

		private final PWebExhibit pexhibit;
		private final WebExhibitImpl exhibit;
		private WebExhibitImpl redo;
		private WebExhibitImpl backup;
		private boolean indexChange;

		public EditExhibitOperation(PWebExhibit pexhibit, WebExhibitImpl backup, WebExhibitImpl exhibit) {
			super(Messages.getString("ExhibitionView.change_exh_size_pos_undo")); //$NON-NLS-1$
			this.pexhibit = pexhibit;
			this.backup = backup;
			this.exhibit = exhibit;
			String backupDescription = Utilities.getPlainDescription(backup);
			indexChange = (backup.getCaption() == null || !backup.getCaption().equals(exhibit.getCaption())
					|| backupDescription == null || !backupDescription.equals(Utilities.getPlainDescription(exhibit))
					|| backup.getAltText() == null || !backup.getAltText().equals(exhibit.getAltText()));
		}

		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			pexhibit.invalidatePaint();
			storeSafelyAndUpdateIndex(null, exhibit, indexChange ? exhibit.getAsset() : null);
			return Status.OK_STATUS;
		}

		@Override
		public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			exhibit.setCaption(redo.getCaption());
			exhibit.setSequenceNo(redo.getSequenceNo());
			exhibit.setDescription(redo.getDescription());
			exhibit.setAltText(redo.getAltText());
			exhibit.setDownloadable(redo.getDownloadable());
			exhibit.setIncludeMetadata(redo.getIncludeMetadata());
			exhibit.setAsset(redo.getAsset());
			return execute(monitor, info);
		}

		@Override
		public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			redo = new WebExhibitImpl(exhibit.getCaption(), exhibit.getSequenceNo(), exhibit.getDescription(),
					exhibit.getHtmlDescription(), exhibit.getAltText(), exhibit.getDownloadable(),
					exhibit.getIncludeMetadata(), exhibit.getAsset());
			exhibit.setCaption(backup.getCaption());
			exhibit.setSequenceNo(backup.getSequenceNo());
			exhibit.setDescription(backup.getDescription());
			exhibit.setAltText(backup.getAltText());
			exhibit.setDownloadable(backup.getDownloadable());
			exhibit.setIncludeMetadata(backup.getIncludeMetadata());
			exhibit.setAsset(backup.getAsset());
			return Status.OK_STATUS;
		}
	}

	public class MoveExhibitOperation extends AbstractOperation {

		private final PWebExhibit moved;
		private final Point2D oldOffset;
		private final Point2D newOffset;
		private PStoryboard oldStoryboard;
		private final PStoryboard target;
		private boolean toFront;
		private int oldSeqno;

		public MoveExhibitOperation(PWebExhibit moved, Point2D oldOffset, PStoryboard target, Point2D newOffset) {
			super(Messages.getString("WebGalleryView.move_exh_undo")); //$NON-NLS-1$
			toFront = (target != moved.getParent() || newOffset.getX() < oldOffset.getX());
			this.moved = moved;
			this.oldOffset = oldOffset;
			this.target = target;
			this.newOffset = newOffset;
			this.oldSeqno = moved.exhibit.getSequenceNo();
		}

		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			oldStoryboard = doMove(newOffset, target, -1);
			return (oldStoryboard == null) ? Status.CANCEL_STATUS : Status.OK_STATUS;
		}

		private PStoryboard doMove(Point2D to, PStoryboard toStoryboard, int ix) {
			PStoryboard fromStoryboard = doRemoveExhibit(moved);
			if (fromStoryboard == null)
				return null;
			Storyboard targetStoryboard = toStoryboard.getStoryboard();
			Storyboard sourceStoryboard = fromStoryboard.getStoryboard();
			if (ix < 0) {
				ix = computeSequenceNumber(to.getX());
				if (toFront)
					ix += 1;
				else
					ix = (Math.max(1, ix));
			}
			WebExhibitImpl exhibit = moved.exhibit;
			String exhibitId = exhibit.getStringId();
			List<String> exhibits = targetStoryboard.getExhibit();
			if (ix > exhibits.size()) {
				exhibits.add(exhibitId);
				moved.setSequenceNo(exhibits.size());
			} else {
				exhibits.add(ix - 1, exhibitId);
				moved.setSequenceNo(ix);
			}
			moved.setOffset(computePos(exhibit), 0);
			ListIterator<?> it = toStoryboard.getChildrenIterator();
			while (it.hasNext()) {
				Object next = it.next();
				if (next instanceof PWebExhibit) {
					PWebExhibit pExhibit = (PWebExhibit) next;
					int sequenceNo = pExhibit.exhibit.getSequenceNo();
					if (sequenceNo >= ix) {
						pExhibit.setSequenceNo(sequenceNo + 1);
						pExhibit.setOffset(computePos(pExhibit.exhibit), pExhibit.getOffset().getY());
					}
				}
			}
			toStoryboard.addChild(moved);
			PBounds bounds = toStoryboard.getBoundsReference();
			int w = toStoryboard.getChildrenCount() * CELLSIZE;
			if (w > bounds.getWidth())
				toStoryboard.setWidth(w);
			List<Object> tobeStored = new ArrayList<Object>(3);
			tobeStored.add(exhibit);
			tobeStored.add(targetStoryboard);
			tobeStored.add(sourceStoryboard);
			storeSafelyAndUpdateIndex(null, tobeStored, null);
			updateActions();
			return fromStoryboard;
		}

		@Override
		public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			return (doMove(newOffset, target, -1) == null) ? Status.CANCEL_STATUS : Status.OK_STATUS;
		}

		@Override
		public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			return (doMove(oldOffset, oldStoryboard, oldSeqno) == null) ? Status.CANCEL_STATUS : Status.OK_STATUS;
		}
	}

	public class DeleteExhibitOperation extends AbstractOperation {

		private PWebExhibit deleted;
		private PStoryboard pstoryboard;
		private Point2D offset;
		private WebExhibitImpl exhibit;

		public DeleteExhibitOperation(PWebExhibit deleted, Point2D oldOffset) {
			super(Messages.getString("WebGalleryView.delete_exh_undo")); //$NON-NLS-1$
			this.deleted = deleted;
			pstoryboard = (PStoryboard) deleted.getParent();
			offset = oldOffset;
		}

		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			exhibit = deleted.exhibit;
			doRemoveExhibit(deleted);
			storeSafelyAndUpdateIndex(exhibit, pstoryboard.getStoryboard(), exhibit.getAsset());
			updateActions();
			return Status.OK_STATUS;
		}

		@Override
		public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			return execute(monitor, info);
		}

		@Override
		public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			StoryboardImpl storyboard = (StoryboardImpl) pstoryboard.getStoryboard();
			double x = offset.getX() - pstoryboard.getOffset().getX();
			double pos = 0;
			int index = 0;
			List<PWebExhibit> trail = new ArrayList<PWebExhibit>(storyboard.getExhibit().size());
			ListIterator<?> it = pstoryboard.getChildrenIterator();
			while (it.hasNext()) {
				Object next = it.next();
				if (next instanceof PWebExhibit) {
					PWebExhibit pExhibit = (PWebExhibit) next;
					PBounds eBounds = pExhibit.getFullBoundsReference();
					double p = eBounds.getX();
					if (p >= x)
						trail.add(pExhibit);
					else {
						p += eBounds.getWidth();
						if (p > pos)
							pos = p;
						index++;
					}
				}
			}
			double oldPos = pos;
			int oldIndex = index;
			if (index >= storyboard.getExhibit().size())
				storyboard.addExhibit(exhibit.getStringId());
			else
				storyboard.getExhibit().add(index, exhibit.getStringId());
			exhibit.setStoryboard_exhibit_parent(storyboard.getStringId());
			deleted = makeExhibit(pstoryboard, exhibit);
			PBounds eBounds = deleted.getFullBoundsReference();
			pos = eBounds.getX() + eBounds.getWidth();
			double iWidth = (pos - oldPos);
			int diff = index - oldIndex;
			for (PWebExhibit pExhibit : trail) {
				pExhibit.offset(iWidth, 0);
				pExhibit.setSequenceNo(pExhibit.exhibit.getSequenceNo() + diff);
			}
			List<Object> toBeStored = new ArrayList<Object>(2);
			toBeStored.add(exhibit);
			toBeStored.add(storyboard);
			storeSafelyAndUpdateIndex(null, toBeStored, exhibit.getAsset());
			updateActions();
			return Status.OK_STATUS;
		}
	}

	public class DropAssetOperation extends AbstractOperation {

		private final AssetSelection selection;
		private final Point2D position;
		private List<PWebExhibit> added;
		private PStoryboard pstoryboard;
		private final boolean replace;
		private com.bdaum.zoom.ui.internal.views.WebGalleryView.DeleteExhibitOperation deleteOp;

		public DropAssetOperation(AssetSelection selection, Point2D position, boolean replace) {
			super(Messages.getString("ExhibitionView.drop_images_undo")); //$NON-NLS-1$
			this.selection = selection;
			this.position = position;
			this.replace = replace;
			added = new ArrayList<PWebExhibit>(selection.size());
			RECT1.x = (int) position.getX();
			RECT1.y = (int) position.getY();
			for (PStoryboard w : storyboards)
				if (w.fullIntersects(RECT1)) {
					pstoryboard = w;
					break;
				}
		}

		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			if (pstoryboard != null) {
				StoryboardImpl storyboard = (StoryboardImpl) pstoryboard.getStoryboard();
				int rawIx = computeSequenceNumber(position.getX() - pstoryboard.getOffset().getX());
				int ix = Math.max(1, Math.min(rawIx, storyboard.getExhibit().size() + 1));
				int oldIndex = ix;
				PWebExhibit cand = null;
				List<PWebExhibit> trail = new ArrayList<PWebExhibit>(storyboard.getExhibit().size());
				ListIterator<?> it = pstoryboard.getChildrenIterator();
				while (it.hasNext()) {
					Object next = it.next();
					if (next instanceof PWebExhibit) {
						PWebExhibit pExhibit = (PWebExhibit) next;
						int sequenceNo = pExhibit.exhibit.getSequenceNo();
						if (replace && sequenceNo == rawIx)
							cand = pExhibit;
						else if (sequenceNo >= oldIndex)
							trail.add(pExhibit);
					}
				}
				WebGalleryImpl show = null;
				List<Object> toBeStored = new ArrayList<Object>(selection.size());
				List<String> assetIds = new ArrayList<String>(selection.size());
				for (Asset asset : selection) {
					if (!accepts(asset))
						continue;
					show = getGallery();
					if (show == null)
						break;
					Date dateCreated = asset.getDateTimeOriginal();
					if (dateCreated == null)
						dateCreated = asset.getDateTime();
					String assetId = asset.getStringId();
					WebExhibitImpl exhibit = new WebExhibitImpl(UiUtilities.createSlideTitle(asset), ix, "", false, //$NON-NLS-1$
							asset.getName(), true, true, assetId);
					if (ix > storyboard.getExhibit().size())
						storyboard.addExhibit(exhibit.getStringId());
					else
						storyboard.getExhibit().add(ix - 1, exhibit.getStringId());
					exhibit.setStoryboard_exhibit_parent(storyboard.getStringId());
					toBeStored.add(exhibit);
					assetIds.add(assetId);
					if (cand != null) {
						deleteOp = new DeleteExhibitOperation(cand, cand.getOffset());
						deleteOp.execute(monitor, info);
						cand = null;
					}
					added.add(makeExhibit(pstoryboard, exhibit));
					ix++;
				}
				int diff = ix - oldIndex;
				for (PWebExhibit pExhibit : trail) {
					pExhibit.setSequenceNo(pExhibit.exhibit.getSequenceNo() + diff);
					pExhibit.setOffset(computePos(pExhibit.exhibit), pExhibit.getOffset().getY());
				}
				if (show != null) {
					toBeStored.add(storyboard);
					storeSafelyAndUpdateIndex(null, toBeStored, assetIds);
				}
				updateActions();
			}
			return Status.OK_STATUS;
		}

		@Override
		public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			added.clear();
			return execute(monitor, info);
		}

		@Override
		public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			List<Object> toBeDeleted = new ArrayList<Object>(selection.size());
			List<String> assetIds = new ArrayList<String>(selection.size());
			for (PWebExhibit exhibit : added) {
				doRemoveExhibit(exhibit);
				toBeDeleted.add(exhibit.exhibit);
				assetIds.add(exhibit.exhibit.getAsset());
			}
			if (deleteOp != null)
				deleteOp.undo(monitor, info);
			storeSafelyAndUpdateIndex(toBeDeleted, pstoryboard.getStoryboard(), assetIds);
			updateActions();
			return Status.OK_STATUS;
		}
	}

	public Set<String> offlineImages = null;
	public PWebExhibit exhibitAtFront;

	private class GalleryDecorateJob extends DecorateJob {

		private Set<String> offline = new HashSet<String>();
		private Color penColor;

		public GalleryDecorateJob() {
			super(Messages.getString("WebGalleryView.decorate_storyboards")); //$NON-NLS-1$
		}

		@Override
		protected boolean mayRun() {
			return storyboards != null && !storyboards.isEmpty();
		}

		@Override
		protected void doRun(IProgressMonitor monitor) {
			final Display display = canvas.getDisplay();
			IDbManager dbManager = Core.getCore().getDbManager();
			offline.clear();
			for (PStoryboard storyboard : storyboards) {
				Object[] children = storyboard.getChildrenReference().toArray();
				for (Object child : children) {
					if (child instanceof PWebExhibit && isVisible() && mayRun()) {
						final PWebExhibit pexhibit = (PWebExhibit) child;
						WebExhibit exhibit = pexhibit.exhibit;
						try {
							String assetId = exhibit.getAsset();
							if (assetId != null) {
								AssetImpl asset = dbManager.obtainAsset(assetId);
								if (asset != null) {
									switch (volumeManager.determineFileState(asset)) {
									case IVolumeManager.PEER:
									case IVolumeManager.REMOTE:
										if (remoteColor != null)
											penColor = remoteColor;
										break;
									case IVolumeManager.OFFLINE:
										if (offlineColor != null)
											penColor = offlineColor;
										String volume = asset.getVolume();
										offline.add((volume == null) ? "" : volume); //$NON-NLS-1$
										break;
									default:
										penColor = titleColor;
										break;
									}
									if (penColor != null && !display.isDisposed()
											&& !((PWebExhibit) child).caption.getPenColor().equals(penColor))
										display.asyncExec(() -> {
											if (!canvas.isDisposed())
												pexhibit.caption.setPenColor(penColor);
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
			offlineImages = offline;
		}
	}

	@SuppressWarnings("serial")
	public class PWebExhibit extends PNode implements PTextHandler, IPresentationItem {

		private WebExhibitImpl exhibit;
		private PSWTImage pImage;
		private TextField caption;
		private TextField description;
		private int imageSize = CELLSIZE - 2 * STORYBOARDMARGINS;
		private PSWTButton propButton;
		private GreekedPSWTText seqNo;
		private final PStoryboard pstoryboard;

		public PWebExhibit(final PSWTCanvas canvas, PStoryboard pstoryboard, WebExhibitImpl exhibit, int pos, int y) {
			this.pstoryboard = pstoryboard;
			this.exhibit = exhibit;
			setPickable(false);
			// Image
			pImage = new PSWTAssetThumbnail(canvas, WebGalleryView.this,
					Core.getCore().getDbManager().obtainAsset(exhibit.getAsset()));
			addChild(pImage);
			configureImage();
			setBounds(0, 0, CELLSIZE, STORYBOARDHEIGHT);
			// prop button
			Image propImage = Icons.smallProperties.getImage();
			propButton = new PSWTButton(canvas, propImage, Messages.getString("WebGalleryView.slide_properties")); //$NON-NLS-1$
			Rectangle propbounds = propImage.getBounds();
			propButton.setOffset(STORYBOARDMARGINS, imageSize + CSIZE);
			addChild(propButton);
			// Seqno
			Color strokePaint = (Color) pstoryboard.getStrokePaint();
			seqNo = new GreekedPSWTText(createSequenceNo(exhibit), new Font("Arial", //$NON-NLS-1$
					Font.BOLD, 9));
			seqNo.setGreekThreshold(5);
			seqNo.setTransparent(true);
			seqNo.setPenColor(strokePaint);
			int textHorMargins = 2 * STORYBOARDMARGINS + propbounds.width;
			int textYPos = imageSize + CSIZE;
			seqNo.setOffset(textHorMargins, textYPos);
			seqNo.setPickable(false);
			addChild(seqNo);
			// caption
			String text = exhibit.getCaption();
			caption = new TextField(text, (int) (imageSize - 2 * textHorMargins - seqNo.getWidth()),
					new Font("Arial", Font.BOLD, 9), strokePaint, null, true, SWT.SINGLE); //$NON-NLS-1$
			caption.setSpellingOptions(10, ISpellCheckingService.TITLEOPTIONS);
			caption.setGreekThreshold(5);
			caption.setOffset(textHorMargins + seqNo.getWidth(), textYPos);
			caption.setPickable(true);
			addChild(caption);
			// legend
			description = new TextField(getDescription(exhibit), imageSize - 2 * textHorMargins - 2 * CSIZE,
					new Font("Arial", //$NON-NLS-1$
							Font.PLAIN, 9),
					strokePaint, null, true, SWT.SINGLE);
			description.setSpellingOptions(10, ISpellCheckingService.DESCRIPTIONOPTIONS);
			description.setGreekThreshold(5);
			description.setOffset(textHorMargins + CSIZE, textYPos + 2 * CSIZE);
			description.setPickable(true);
			addChild(description);
			// position and bounds
			setOffset(pos, y);
			installHandleEventHandlers(pImage, false, false, this);
			addInputEventListener(new PBasicInputEventHandler() {
				@Override
				public void mouseReleased(PInputEvent event) {
					setPickedNode(event.getPickedNode());
					if (getPickedNode() == propButton)
						editExhibitProperties();
				}
			});
			pImage.addInputEventListener(new PBasicInputEventHandler() {
				@Override
				public void mouseReleased(PInputEvent event) {
					setPickedNode(event.getPickedNode());
					if (event.getClickCount() == 2)
						editExhibitProperties();
				}
			});
		}

		@Override
		public void moveToFront() {
			super.moveToFront();
			if (exhibitAtFront != null)
				exhibitAtFront.description.setPenColor((Color) pstoryboard.getStrokePaint());
			exhibitAtFront = this;
			description.setPenColor(pstoryboard.getHighlightColor());
		}

		@Override
		public void moveToBack() {
			super.moveToBack();
			if (exhibitAtFront == this)
				exhibitAtFront = null;
			description.setPenColor((Color) pstoryboard.getStrokePaint());
		}

		private void editExhibitProperties() {
			doEditExhibitProperties(PWebExhibit.this);
		}

		private void setCaptionAndDescription(WebExhibit exh) {
			caption.setText(exh.getCaption());
			description.setText(getDescription(exh));
			description.setSpellingOptions(10, exh.getHtmlDescription() ? ISpellCheckingService.NOSPELLING
					: ISpellCheckingService.DESCRIPTIONOPTIONS);
		}

		private String getDescription(WebExhibit exh) {
			String des = exh.getDescription();
			if (des == null || des.isEmpty())
				des = Messages.getString("WebGalleryView.description"); //$NON-NLS-1$
			return des;
		}

		public void processTextEvent(TextField focus) {
			String text = focus.getText();
			int type = -1;
			if (focus == caption) {
				if (!text.equals(exhibit.getCaption()))
					type = EditTextOperation.CAPTION;
			} else if (focus == description) {
				if (!text.equals(exhibit.getDescription()))
					type = EditTextOperation.DESCRIPTION;
			}
			if (type >= 0)
				performOperation(new EditTextOperation(exhibit, text, focus, type));
		}

		public void update(WebExhibit exh, Asset asset) {
			removeChild(pImage);
			pImage.getImage().dispose();
			Image image = ImageUtilities.loadThumbnail(canvas.getDisplay(), asset.getJpegThumbnail(),
					Ui.getUi().getDisplayCMS(), SWT.IMAGE_JPEG, true);
			images.add(image);
			pImage = new ZPSWTImage(canvas, image);
			addChild(0, pImage);
			configureImage();
			pImage.moveToBack();
			setCaptionAndDescription(exh);
		}

		private void configureImage() {
			pImage.setPickable(true);
			PBounds bounds = pImage.getBoundsReference();
			double width = bounds.getWidth();
			double height = bounds.getHeight();
			double scale = Math.min(imageSize / width, imageSize / height);
			double xx = (imageSize - width * scale) / 2;
			double yy = (imageSize - height * scale) / 2;
			pImage.setOffset(STORYBOARDMARGINS + xx, STORYBOARDMARGINS + yy);
			pImage.scale(scale);
		}

		public void updateColors(Color selectedPaint) {
			ListIterator<?> it = getChildrenIterator();
			while (it.hasNext()) {
				PNode child = (PNode) it.next();
				if (child instanceof TextField)
					((TextField) child).setSelectedPenColor(selectedPaint);
			}
		}

		public void setSequenceNo(int i) {
			exhibit.setSequenceNo(i);
			seqNo.setText(createSequenceNo(exhibit));
			Rectangle propbounds = propButton.getImage().getBounds();
			caption.setOffset(2 * STORYBOARDMARGINS + propbounds.width + seqNo.getWidth(), caption.getOffset().getY());
		}

		private String createSequenceNo(WebExhibit exh) {
			return exh.getSequenceNo() + " - "; //$NON-NLS-1$
		}

		public void setPenColor(Color color) {
			caption.setPenColor(color);
			description.setPenColor(color);
		}

		public void setBackgroundColor(Color color) {
			description.setBackgroundColor(color);
		}

		public void setTitleColor(Color color) {
			caption.setPenColor(color);
		}

		public String getAssetId() {
			return exhibit.getAsset();
		}

	}

	@SuppressWarnings("serial")
	public class PStoryboard extends PPanel {

		private Color highlightColor;

		public PStoryboard(Storyboard storyboard) {
			super(storyboard, STORYBOARDWIDTH, STORYBOARDHEIGHT);
			// buttons
			deleteButton = createButton(canvas, Icons.largeDelete,
					Messages.getString("WebGalleryView.delete_storyboard")); //$NON-NLS-1$
			Rectangle bounds = deleteButton.getImage().getBounds();
			deleteButton.setOffset(STORYBOARDMARGINS, STORYBOARDHEIGHT - (bounds.height / 2 + STORYBOARDMARGINS));
			propButton = createButton(canvas, Icons.largeProperties,
					Messages.getString("WebGalleryView.storyboard_properties")); //$NON-NLS-1$
			bounds = propButton.getImage().getBounds();
			propButton.setOffset(STORYBOARDMARGINS, STORYBOARDHEIGHT - 2 * (bounds.height / 2 + STORYBOARDMARGINS));
			// Title
			createTitle(canvas, storyboard.getTitle(), STORYBOARDMARGINS, STORYBOARDMARGINS, new Color(132, 132, 132),
					(Color) getPaint(), 18);
			highlightColor = new Color(0, 0, 0);
		}

		public Storyboard getStoryboard() {
			return (Storyboard) data;
		}

		public void setStoryboard(StoryboardImpl storyboard) {
			this.data = storyboard;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.bdaum.zoom.ui.views.PTextHandler#processTextEvent(com.bdaum.zoom
		 * .ui.widgets.TextField)
		 */

		public void processTextEvent(TextField focus) {
			Storyboard storyboard = getStoryboard();
			if (!focus.getText().equals(storyboard.getTitle()))
				performOperation(new EditStoryboardTextOperation(storyboard, focus));
		}

		public void updateColors() {
			Color color = (Color) getPaint();
			ListIterator<?> it = getChildrenIterator();
			while (it.hasNext()) {
				PNode child = (PNode) it.next();
				if (child instanceof PWebExhibit)
					((PWebExhibit) child).updateColors(color);
			}
		}

		@Override
		protected void deletePressed() {
			setPickedNode(PStoryboard.this);
			deleteStoryboardAction.run();
		}

		@Override
		protected void propertiesPressed() {
			setPickedNode(PStoryboard.this);
			editStoryboardAction.run();
		}

		/**
		 * @return the highlightColor
		 */
		public Color getHighlightColor() {
			return highlightColor;
		}
	}

	public static final String ID = "com.bdaum.zoom.ui.WebGalleryView"; //$NON-NLS-1$
	public static final String WEBGALLERY_PERSPECTIVE = "com.bdaum.zoom.PresentationPerspective"; //$NON-NLS-1$
	private static final String LAST_GALLERY = "com.bdaum.zoom.lastGallery"; //$NON-NLS-1$
	private static final int STORYBOARDWIDTH = 1500;
	private static final int CELLSIZE = 250;
	private static final int STORYBOARDMARGINS = 5;
	private static final int CSIZE = 12;
	private static final int STORYBOARDHEIGHT = CELLSIZE + 5 * CSIZE;
	protected static final int STORYBOARDDIST = STORYBOARDHEIGHT + CSIZE;
	private static final int LARGEBUTTONSIZE = 64;
	private Color wallPaint = new Color(255, 255, 250);
	private WebGalleryImpl gallery;
	private Action addStoryBoardAction;
	private Action generateAction;
	private Action saveAction;
	private List<PStoryboard> storyboards = new ArrayList<PStoryboard>();
	private Action deleteStoryboardAction;
	private Action editStoryboardAction;

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		if (memento != null) {
			String lastSelection = memento.getString(LAST_GALLERY);
			WebGalleryImpl obj = Core.getCore().getDbManager().obtainById(WebGalleryImpl.class, lastSelection);
			if (obj != null)
				setGallery(obj);
		}
	}

	@Override
	public void saveState(IMemento memento) {
		if (memento != null && gallery != null)
			memento.putString(LAST_GALLERY, gallery.getStringId());
		super.saveState(memento);
	}

	private void setGallery(WebGalleryImpl gallery) {
		this.gallery = gallery;
		if (gallery.getStoryboard().isEmpty())
			gallery.addStoryboard(new StoryboardImpl(Messages.getString("WebGalleryView.storyboard_1"), 1, false, "", //$NON-NLS-1$ //$NON-NLS-2$
					0, false, true, true, true));
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		canvas.setData("id", "webgallery"); //$NON-NLS-1$ //$NON-NLS-2$
		undoContext = new UndoContext() {
			@Override
			public String getLabel() {
				return Messages.getString("WebGalleryView.webgallery_undo_context"); //$NON-NLS-1$
			}
		};
		// Setup
		setupGallery(getViewSite(), gallery);
		setColor(canvas);
		addWheelListener(0.1d, 10d);
		textEventHandler = new TextEventHandler(selectionBackgroundColor);
		PBasicInputEventHandler eventHandler = new PBasicInputEventHandler() {

			@Override
			public void keyPressed(PInputEvent event) {
				if (textEventHandler.hasFocus())
					textEventHandler.keyPressed(event);
				else
					pan(event);
			}

			@Override
			public void mousePressed(PInputEvent event) {
				setPickedNode(event.getPickedNode());
			}

			@Override
			public void mouseDragged(PInputEvent event) {
				textEventHandler.mouseDragged(event);
			}

			@Override
			public void mouseReleased(PInputEvent event) {
				textEventHandler.mouseReleased(event);
				if (event.getClickCount() == 2) {
					PCamera camera = event.getCamera();
					if (oldTransform == null) {
						oldTransform = camera.getViewTransform();
						camera.setViewTransform(new AffineTransform());
					} else {
						camera.setViewTransform(oldTransform);
						resetTransform();
					}
				}
				if (event.isRightMouseButton())
					positionRelativeToCamera = event.getPositionRelativeTo(canvas.getCamera());
			}
		};
		canvas.getRoot().getDefaultInputManager().setKeyboardFocus(eventHandler);
		canvas.addInputEventListener(eventHandler);
		setPanAndZoomHandlers();
		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(canvas, HelpContextIds.WEBGALLERY_VIEW);
		// Drop support
		addDropListener(canvas);
		// Hover
		installHoveringController();
		// Actions
		makeActions();
		installListeners(parent);
		hookContextMenu();
		contributeToActionBars();
		if (gallery != null)
			setInput(gallery);
		addCatalogListener();
		setDecorator(canvas, new GalleryDecorateJob());
		updateActions();
	}

	@Override
	protected void fillLocalToolBar(IToolBarManager manager) {
		super.fillLocalToolBar(manager);
		manager.add(addStoryBoardAction);
		manager.add(new Separator());
		manager.add(generateAction);
		manager.add(saveAction);
	}

	@Override
	protected void fillLocalPullDown(IMenuManager manager) {
		manager.add(addStoryBoardAction);
		manager.add(new Separator());
		manager.add(generateAction);
		manager.add(saveAction);
		super.fillLocalPullDown(manager);
	}

	@Override
	protected void fillContextMenu(IMenuManager manager) {
		manager.add(gotoExhibitAction);
		PNode pickedNode = getPickedNode();
		if (pickedNode != null && pickedNode.getParent() instanceof PWebExhibit)
			addCommonContextActions(manager);
		else {
			if (pickedNode != null && pickedNode.getParent() instanceof PStoryboard)
				pickedNode = pickedNode.getParent();
			if (pickedNode instanceof PStoryboard) {
				manager.add(new Separator());
				manager.add(editStoryboardAction);
				manager.add(deleteStoryboardAction);
			}
		}
		super.fillContextMenu(manager);
	}

	@Override
	protected void updatePresentation(Collection<? extends Asset> assets) {
		Map<String, Asset> amap = new HashMap<String, Asset>(assets.size());
		for (Asset a : assets)
			amap.put(a.getStringId(), a);
		for (PStoryboard pstoryboard : storyboards) {
			ListIterator<?> it = pstoryboard.getChildrenIterator();
			while (it.hasNext()) {
				Object next = it.next();
				if (next instanceof PWebExhibit) {
					PWebExhibit pexhibit = (PWebExhibit) next;
					Asset asset = amap.get(pexhibit.exhibit.getAsset());
					if (asset != null) {
						pexhibit.update(pexhibit.exhibit, asset);
						break;
					}
				}
			}
		}
	}

	@Override
	public void updateActions() {
		if (addStoryBoardAction == null)
			return;
		boolean hasGallery = gallery != null;
		addStoryBoardAction.setEnabled(hasGallery && !dbIsReadonly());
		propertiesAction.setEnabled(hasGallery);
		saveAction.setEnabled(hasGallery);
		generateAction.setEnabled(hasGallery);
		gotoExhibitAction.setEnabled(hasGallery);
		super.updateActions();
	}

	private void setupGallery(IViewSite site, WebGalleryImpl gallery) {
		if (gallery != null) {
			int h = Math.max(2, gallery.getStoryboard().size()) * STORYBOARDDIST;
			double factor = site.getShell().getDisplay().getPrimaryMonitor().getBounds().height / (2d * h);
			surface.setBounds(-3000d, -3000d, (STORYBOARDWIDTH + 6000d), (h + 6000d));
			surface.setScale(factor);
		}
	}

	@Override
	protected void cleanUp() {
		try {
			for (PStoryboard pstoryboard : storyboards) {
				pstoryboard.removeAllChildren();
				PNode parent = pstoryboard.getParent();
				if (parent != null)
					parent.removeChild(pstoryboard);
			}
		} catch (SWTException e) {
			// do nothing
		}
		storyboards.clear();
		super.cleanUp();
	}

	@Override
	protected void setColor(Control control) {
		Color newPaint = UiUtilities.getAwtBackground(control, null);
		surface.setPaint(newPaint);
		selectionBackgroundColor = UiUtilities.getAwtForeground(control, null);
		CssActivator.getDefault().applyExtendedStyle(control, this);
		wallPaint = UiUtilities.getAwtForeground(control, null);
		for (PStoryboard pstoryboard : storyboards) {
			pstoryboard.setPaint(wallPaint);
			pstoryboard.setStrokeColor(selectionBackgroundColor);
		}
		for (PStoryboard pstoryboard : storyboards) {
			pstoryboard.setStrokeColor(selectionBackgroundColor);
			ListIterator<?> it = pstoryboard.getChildrenIterator();
			while (it.hasNext()) {
				Object child = it.next();
				if (child instanceof PSWTText)
					((PSWTText) child).setPenColor(newPaint);
				else if (child instanceof TextField) {
					((TextField) child).setPenColor(newPaint);
					((TextField) child).setBackgroundColor(wallPaint);
				} else if (child instanceof PWebExhibit) {
					((PWebExhibit) child).setPenColor(foregroundColor);
					((PWebExhibit) child).setTitleColor(titleForegroundColor);
					((PWebExhibit) child).setBackgroundColor(wallPaint);
				}
			}
		}
	}

	@Override
	protected void makeActions() {
		super.makeActions();
		propertiesAction = new Action(Messages.getString("WebGalleryView.gallery_properties"), //$NON-NLS-1$
				Icons.webGallery.getDescriptor()) {
			@Override
			public void run() {
				WebGalleryImpl show = getGallery();
				if (show == null)
					return;
				WebGalleryImpl backup = WebGalleryPropertiesOperation.cloneGallery(show);
				show = WebGalleryEditDialog.openWebGalleryEditDialog(getSite().getShell(), null, show, show.getName(),
						false, true, null);
				if (show != null) {
					gallery = show;
					performOperation(new WebGalleryPropertiesOperation(backup, gallery));
					setInput(gallery);
				}
			}
		};
		propertiesAction.setToolTipText(Messages.getString("WebGalleryView.gallery_properties_tooltip")); //$NON-NLS-1$
		addStoryBoardAction = new Action(Messages.getString("WebGalleryView.add_storyboard"), //$NON-NLS-1$
				Icons.add.getDescriptor()) {

			@Override
			public void run() {
				WebGalleryImpl show = getGallery();
				if (show == null)
					return;
				int l = show.getStoryboard().size();
				EditStoryBoardDialog dialog = new EditStoryBoardDialog(getSite().getShell(), show, null,
						NLS.bind("Storyboard {0}", l + 1)); //$NON-NLS-1$
				if (dialog.open() == Window.OK)
					performOperation(new CreateStoryboardOperation(dialog.getResult(), 10 + l * STORYBOARDDIST));
			}
		};
		addStoryBoardAction.setToolTipText(Messages.getString("WebGalleryView.add_storyboard_tooltip")); //$NON-NLS-1$
		generateAction = new Action(Messages.getString("WebGalleryView.generate"), Icons.play.getDescriptor()) { //$NON-NLS-1$

			@Override
			public void run() {
				generate(false);
			}
		};
		generateAction.setToolTipText(Messages.getString("WebGalleryView.generate_tooltip")); //$NON-NLS-1$
		saveAction = new Action(Messages.getString("WebGalleryView.save_design"), Icons.save.getDescriptor()) { //$NON-NLS-1$

			@Override
			public void run() {
				generate(true);
			}
		};
		saveAction.setToolTipText(Messages.getString("WebGalleryView.generate_web_gallery")); //$NON-NLS-1$
		gotoExhibitAction = new Action(Messages.getString("WebGalleryView.goto_exhibit")) { //$NON-NLS-1$

			@Override
			public void run() {
				List<WebExhibitImpl> exhibits = new ArrayList<WebExhibitImpl>(300);
				for (PStoryboard sb : storyboards) {
					ListIterator<?> it = sb.getChildrenIterator();
					while (it.hasNext()) {
						Object next = it.next();
						if (next instanceof PWebExhibit && ((PWebExhibit) next).exhibit != null)
							exhibits.add(((PWebExhibit) next).exhibit);
					}
				}
				SelectWebExhibitDialog dialog = new SelectWebExhibitDialog(getSite().getShell(), exhibits);
				dialog.create();
				org.eclipse.swt.graphics.Point pos = canvas.toDisplay((int) positionRelativeToCamera.getX(),
						(int) positionRelativeToCamera.getY());
				pos.x += 10;
				pos.y += 10;
				dialog.getShell().setLocation(pos);
				if (dialog.open() == Window.OK) {
					WebExhibitImpl selectedExhibit = dialog.getResult();
					for (PStoryboard sb : storyboards) {
						ListIterator<?> it = sb.getChildrenIterator();
						while (it.hasNext()) {
							Object next = it.next();
							if (next instanceof PWebExhibit) {
								PWebExhibit pexhibit = (PWebExhibit) next;
								if (pexhibit.exhibit == selectedExhibit) {
									Point2D offset = pexhibit.getOffset();
									Point2D storyBoardOffset = sb.getOffset();
									PCamera camera = canvas.getCamera();
									double x = offset.getX() + storyBoardOffset.getX() - CELLSIZE / 2;
									double y = offset.getY() + storyBoardOffset.getY() - CELLSIZE / 2;
									double scale = surface.getScale();
									PBounds viewBounds = camera.getViewBounds();
									camera.translateView(viewBounds.getX() - x * scale, viewBounds.getY() - y * scale);
									return;
								}
							}
						}
					}
				}
			}
		};
		gotoExhibitAction.setToolTipText(Messages.getString("WebGalleryView.goto_exhibit_tooltip")); //$NON-NLS-1$

		deleteStoryboardAction = new Action(Messages.getString("WebGalleryView.delete_storyboard"), //$NON-NLS-1$
				Icons.delete.getDescriptor()) {

			@Override
			public void run() {
				if (getPickedNode() instanceof PStoryboard && AcousticMessageDialog.openQuestion(getSite().getShell(),
						Messages.getString("WebGalleryView.remove_storyboard"), //$NON-NLS-1$
						Messages.getString("WebGalleryView.do_you_really_want_to_remove"))) //$NON-NLS-1$
					performOperation(new DeleteStoryboardOperation((PStoryboard) getPickedNode()));
			}
		};
		deleteStoryboardAction.setToolTipText(Messages.getString("WebGalleryView.delete_storyboard_tooltip")); //$NON-NLS-1$

		editStoryboardAction = new Action(Messages.getString("WebGalleryView.edit_storyboard"), //$NON-NLS-1$
				Icons.properties_blue.getDescriptor()) {

			@Override
			public void run() {
				if (getPickedNode() instanceof PStoryboard) {
					textEventHandler.commit();
					editStoryboardLegend((PStoryboard) getPickedNode());
				}
			}
		};
		editStoryboardAction.setToolTipText(Messages.getString("WebGalleryView.edit_storyboard_tooltip")); //$NON-NLS-1$
		exhibitPropertiesAction = new Action(Messages.getString("WebGalleryView.properties")) { //$NON-NLS-1$
			@Override
			public void run() {
				doEditExhibitProperties((PWebExhibit) getAdapter(IPresentationItem.class));
			}
		};
		exhibitPropertiesAction.setToolTipText(Messages.getString("WebGalleryView.properties_tooltip")); //$NON-NLS-1$
	}

	protected void doEditExhibitProperties(PWebExhibit pWebExhibit) {
		if (pWebExhibit != null) {
			textEventHandler.commit();
			WebExhibitImpl exh = pWebExhibit.exhibit;
			WebExhibitImpl backup = new WebExhibitImpl(exh.getCaption(), exh.getSequenceNo(), exh.getDescription(),
					exh.getHtmlDescription(), exh.getAltText(), exh.getDownloadable(), exh.getIncludeMetadata(),
					exh.getAsset());
			EditWebExibitDialog dialog = new EditWebExibitDialog(getSite().getShell(), exh, exh.getCaption(), gallery);
			if (dialog.open() == Window.OK) {
				performOperation(new EditExhibitOperation(pWebExhibit, backup, exh));
				pWebExhibit.setCaptionAndDescription(exh);
			}
		}
	}

	protected PStoryboard addStoryboard(Storyboard storyboard, double y, ProgressIndicator progrBar) {
		IDbManager dbManager = Core.getCore().getDbManager();
		PStoryboard pstoryboard = new PStoryboard(storyboard);
		surface.addChild(pstoryboard);
		storyboards.add(pstoryboard);
		pstoryboard.setOffset(0, y);
		int i = 0;
		for (WebExhibitImpl exhibit : dbManager.obtainByIds(WebExhibitImpl.class, storyboard.getExhibit())) {
			exhibit.setSequenceNo(++i);
			makeExhibit(pstoryboard, exhibit);
			if (progrBar != null)
				progrBar.worked(1);
		}
		setColor(canvas);
		pstoryboard.updateColors();
		setPanAndZoomHandlers();
		return pstoryboard;
	}

	private void setPanAndZoomHandlers() {
		setPanAndZoomHandlers(-13, -17);
	}

	protected PWebExhibit makeExhibit(PStoryboard pstoryboard, WebExhibitImpl exhibit) {
		PWebExhibit pExhibit = new PWebExhibit(canvas, pstoryboard, exhibit, computePos(exhibit), 0);
		PBounds bounds = pstoryboard.getBoundsReference();
		int w = pstoryboard.getChildrenCount() * CELLSIZE;
		if (w > bounds.getWidth())
			pstoryboard.setWidth(w);
		pstoryboard.addChild(pExhibit);
		return pExhibit;
	}

	private static int computePos(WebExhibitImpl exhibit) {
		return (exhibit.getSequenceNo() - 1) * CELLSIZE + 2 * LARGEBUTTONSIZE;
	}

	private static int computeSequenceNumber(double x) {
		return Math.max(0, (int) ((x - 2 * LARGEBUTTONSIZE) / CELLSIZE + 1));
	}

	protected void moveExhibit(PWebExhibit moved, PStoryboard target, Point2D oldOffset, Point2D newOffset) {
		performOperation(new MoveExhibitOperation(moved, oldOffset, target, newOffset));
	}

	protected void deleteExhibit(PWebExhibit moved, Point2D oldOffset) {
		if (getGallery() != null)
			performOperation(new DeleteExhibitOperation(moved, oldOffset));
	}

	protected PStoryboard doRemoveExhibit(PWebExhibit moved) {
		PNode node = moved.getParent();
		if (node instanceof PStoryboard) {
			PStoryboard pstoryboard = (PStoryboard) node;
			pstoryboard.removeChild(moved);
			WebExhibitImpl exhibit = moved.exhibit;
			int sequenceNo = exhibit.getSequenceNo();
			ListIterator<?> it = pstoryboard.getChildrenIterator();
			while (it.hasNext()) {
				Object next = it.next();
				if (next instanceof PWebExhibit) {
					PWebExhibit pExhibit = (PWebExhibit) next;
					WebExhibitImpl exh = pExhibit.exhibit;
					int q = exh.getSequenceNo();
					if (q >= sequenceNo) {
						pExhibit.setSequenceNo(q - 1);
						pExhibit.setOffset(computePos(exh), pExhibit.getOffset().getY());
					}
				}
			}
			Storyboard storyboard = pstoryboard.getStoryboard();
			storyboard.removeExhibit(exhibit.getStringId());
			return pstoryboard;
		}
		return null;
	}

	protected WebGalleryImpl getGallery() {
		if (gallery == null && !dbIsReadonly())
			gallery = WebGalleryEditDialog.openWebGalleryEditDialog(getSite().getShell(), null, null,
					Messages.getString("WebGalleryView.create_new_web_gallery"), false, false, null);//$NON-NLS-1$
		return gallery;
	}

	@Override
	public void setInput(IdentifiableObject presentation) {
		super.setInput(presentation);
		if (presentation instanceof WebGalleryImpl) {
			WebGalleryImpl gal = (WebGalleryImpl) presentation;
			setGallery(gal);
			setupGallery(getViewSite(), gal);
			setPartName(gal.getName());
			int n = 0;
			for (Storyboard sb : gal.getStoryboard())
				n += sb.getExhibit().size();
			beginTask(n);
			int y = 2 * STORYBOARDMARGINS;
			double w = STORYBOARDWIDTH;
			for (Storyboard sb : gal.getStoryboard()) {
				w = Math.max(w, addStoryboard(sb, y, progressBar).getBoundsReference().getWidth());
				y += STORYBOARDDIST;
			}
			double h = gal.getStoryboard().size() * (STORYBOARDHEIGHT + STORYBOARDDIST) + 2 * STORYBOARDMARGINS
					- STORYBOARDDIST;
			w += 2 * STORYBOARDMARGINS;
			updateSurfaceBounds(w, h);
			setColor(canvas);
			updateActions();
			endTask();
		} else
			setPartName(Messages.getString("WebGalleryView.web_gallery")); //$NON-NLS-1$
	}

	private void generate(final boolean save) {
		WebGalleryImpl show = getGallery();
		if (show == null)
			return;
		if (offlineImages != null && !offlineImages.isEmpty()) {
			String[] volumes = offlineImages.toArray(new String[offlineImages.size()]);
			Arrays.sort(volumes);
			AcousticMessageDialog.openInformation(getSite().getShell(),
					Messages.getString("WebGalleryView.images_are_offline"), //$NON-NLS-1$
					volumes.length > 1
							? NLS.bind(Messages.getString("WebGalleryView.some_image_on_volumes_are_offline"), //$NON-NLS-1$
									Core.toStringList(volumes, ", ")) //$NON-NLS-1$
							: NLS.bind(Messages.getString("WebGalleryView.some_images_on_volume_n_are_offline"), //$NON-NLS-1$
									volumes[0]));
			return;
		}
		String selectedEngine = show.getSelectedEngine();
		boolean isFtp = show.getIsFtp();
		String outputFolder = (isFtp) ? show.getFtpDir() : show.getOutputFolder();
		while (selectedEngine == null || outputFolder == null || selectedEngine.isEmpty() || outputFolder.isEmpty()) {
			show = WebGalleryEditDialog.openWebGalleryEditDialog(getSite().getShell(), null, show, show.getName(), true,
					false, Messages.getString("WebGalleryView.select_web_gallery")); //$NON-NLS-1$
			if (show == null)
				return;
			selectedEngine = show.getSelectedEngine();
			isFtp = show.getIsFtp();
			outputFolder = (isFtp) ? show.getFtpDir() : show.getOutputFolder();
		}
		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint(UiActivator.PLUGIN_ID, "galleryGenerator"); //$NON-NLS-1$
		IExtension[] extensions = extensionPoint.getExtensions();
		for (IExtension ext : extensions) {
			IConfigurationElement[] configurationElements = ext.getConfigurationElements();
			for (IConfigurationElement el : configurationElements) {
				if (el.getAttribute("id").equals(selectedEngine)) { //$NON-NLS-1$
					if (createGenerator(show, el, outputFolder, isFtp, save))
						return;
					break;
				}
			}
		}
	}

	private boolean createGenerator(WebGalleryImpl show, IConfigurationElement el, String outputFolder, boolean isFtp,
			final boolean save) {
		try {
			String sections = el.getAttribute("sections"); //$NON-NLS-1$
			if (!Boolean.parseBoolean(sections) && show.getStoryboard().size() > 1) {
				AcousticMessageDialog dialog = new AcousticMessageDialog(getSite().getShell(),
						Messages.getString("WebGalleryView.multiple_storyboards"), //$NON-NLS-1$
						null, NLS.bind(Messages.getString("WebGalleryView.does_not_support_multiple_storyboards"), //$NON-NLS-1$
								el.getAttribute("name")), //$NON-NLS-1$
						MessageDialog.WARNING, new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL },
						0);
				if (dialog.open() != 0)
					return true;
			}
			String maxImages = el.getAttribute("maxImages"); //$NON-NLS-1$
			if (maxImages != null) {
				try {
					int max = Integer.parseInt(maxImages);
					int n = 0;
					for (Storyboard storyboard : show.getStoryboard())
						max -= storyboard.getExhibit().size();
					if (n > max) {
						AcousticMessageDialog dialog = new AcousticMessageDialog(getSite().getShell(),
								Messages.getString("WebGalleryView.too_many"), //$NON-NLS-1$
								null, NLS.bind(Messages.getString("WebGalleryView.n_exceeds_max"), //$NON-NLS-1$
										new Object[] { n, max, el.getAttribute("name") }), //$NON-NLS-1$
								MessageDialog.WARNING,
								new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 0);
						if (dialog.open() != 0)
							return true;
					}
				} catch (NumberFormatException e) {
					// ignore
				}
			}
			final IGalleryGenerator generator = (IGalleryGenerator) el.createExecutableExtension("class"); //$NON-NLS-1$
			if (generator instanceof AbstractGalleryGenerator)
				((AbstractGalleryGenerator) generator).setConfigurationElement(el);
			final File file = new File(outputFolder);
			if (!isFtp && file.exists() && file.listFiles().length > 0) {
				AcousticMessageDialog dialog = new AcousticMessageDialog(getSite().getShell(),
						Messages.getString("WebGalleryView.overwrite"), //$NON-NLS-1$
						null, NLS.bind(Messages.getString("WebGalleryView.output_folder_not_empty"), file), //$NON-NLS-1$
						MessageDialog.QUESTION, new String[] { Messages.getString("WebGalleryView.overwrite_button"), //$NON-NLS-1$
								Messages.getString("WebGalleryView.clear_folder"), //$NON-NLS-1$
								IDialogConstants.CANCEL_LABEL },
						0);
				int ret = dialog.open();
				switch (ret) {
				case 2:
					return true;
				case 1:
					Core.deleteFolderExcluding(file, "themes"); //$NON-NLS-1$
					break;
				}
			}
			String page = show.getPageName();
			final boolean ftp = isFtp;
			final File start = new File(file, (page == null || page.isEmpty()) ? "index.html" //$NON-NLS-1$
					: page);
			final WebGalleryImpl gal = show;
			final WebGalleryJob job = new WebGalleryJob(gal, generator, WebGalleryView.this);
			final FtpAccount account;
			if (ftp) {
				account = FtpAccount.findAccount(outputFolder);
				if (account == null) {
					AcousticMessageDialog.openError(getSite().getShell(),
							Messages.getString("WebGalleryView.account_does_not_exist"), //$NON-NLS-1$
							NLS.bind(Messages.getString("WebGalleryView.ftp_account_not_defined"), //$NON-NLS-1$
									outputFolder));
					return true;
				}
			} else
				account = null;
			job.addJobChangeListener(new JobChangeAdapter() {
				@Override
				public void done(IJobChangeEvent event) {
					if (!job.wasAborted()) {
						if (ftp)
							new TransferJob(generator.getTargetFolder().listFiles(), account, true).schedule(0);
						else
							showWebGallery(save, start, gal);
					}
				}
			});
			job.schedule();
		} catch (CoreException e) {
			UiActivator.getDefault()
					.logError(NLS.bind(Messages.getString("WebGalleryView.error_when_instantiating_web_generator"), //$NON-NLS-1$
							el.getAttribute("name")), e); //$NON-NLS-1$
		}
		return false;
	}

	private void showWebGallery(final boolean save, final File start, final WebGalleryImpl show) {
		final Display display = getSite().getShell().getDisplay();
		display.asyncExec(() -> {
			if (!display.isDisposed())
				BusyIndicator.showWhile(display, () -> {
					try {
						URL url = start.toURI().toURL();
						if (save) {
							SaveTemplateDialog dialog = new SaveTemplateDialog(getSite().getShell(), start, show);
							if (dialog.open() == Window.OK) {
								final WebGalleryImpl template = dialog.getResult();
								final IDbManager dbManager = Core.getCore().getDbManager();
								List<IdentifiableObject> set = dbManager.obtainObjects(WebGalleryImpl.class, false,
										"name", template.getName(), QueryField.EQUALS, "template", true, //$NON-NLS-1$//$NON-NLS-2$
										QueryField.EQUALS);
								dbManager.safeTransaction(set, template);
							}
						} else {
							IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser();
							if (browser != null)
								browser.openURL(url);
						}
					} catch (MalformedURLException e1) {
						// should not happen
					} catch (PartInitException e2) {
						// ignore
					}
				});
		});
	}

	@Override
	protected void dropAssets(ISelection selection, org.eclipse.swt.graphics.Point point, Point2D position,
			boolean replace) {
		performOperation(new DropAssetOperation((AssetSelection) selection, position, replace));
		getSite().getPage().activate(this);
	}

	private void editStoryboardLegend(PStoryboard pStoryboard) {
		StoryboardImpl w = (StoryboardImpl) pStoryboard.getStoryboard();
		StoryboardImpl backup = new StoryboardImpl(w.getTitle(), w.getSequenceNo(), w.getHtmlDescription(),
				w.getDescription(), w.getImageSize(), w.getEnlargeSmall(), w.getShowCaptions(), w.getShowDescriptions(),
				w.getShowExif());
		EditStoryBoardDialog dialog = new EditStoryBoardDialog(getSite().getShell(),
				w.getWebGallery_storyboard_parent(), w, w.getTitle());
		if (dialog.open() == Window.OK)
			performOperation(new EditStoryboardOperation(w, backup, pStoryboard));
	}

	@Override
	protected PNode[] getWorkArea() {
		return storyboards.toArray(new PNode[storyboards.size()]);
	}

	@Override
	protected int getPanDirection() {
		return GalleryPanEventHandler.BOTH;
	}

	@Override
	protected int getHoriztontalMargins() {
		return STORYBOARDMARGINS;
	}

	@Override
	public Object getContent() {
		return gallery;
	}

	@Override
	protected AbstractHandleDragSequenceEventHandler createDragSequenceEventHandler(PNode node, boolean relative,
			boolean restricted, PNode presentationObject) {
		return new AbstractPresentationView.AbstractHandleDragSequenceEventHandler(node, relative, restricted,
				presentationObject) {

			@Override
			protected void moveToFront(PNode object) {
				updateActions();
			}

			@Override
			protected void drop(PInputEvent event, PNode pnode) {
				PWebExhibit pexhibit = (PWebExhibit) pnode.getParent();
				if (event.getCanvasPosition().getY() < 0)
					deleteExhibit(pexhibit, oldOffset);
				else {
					Point2D newOffset = pexhibit.getOffset();
					Point2D parentOffset = ((PStoryboard) pexhibit.getParent()).getOffset();
					Point2D point = new Point2D.Double(newOffset.getX() + parentOffset.getX(),
							event.getPosition().getY());
					surface.parentToLocal(point);
					for (PStoryboard pstoryboard : storyboards) {
						double y = point.getY() - pstoryboard.getOffset().getY();
						PBounds bounds = pstoryboard.getBoundsReference();
						if (bounds.getY() <= y && bounds.getY() + bounds.getHeight() >= y) {
							moveExhibit(pexhibit, pstoryboard, oldOffset, newOffset);
							return;
						}
					}
					pexhibit.setOffset(oldOffset);
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
		PBounds bounds = new PBounds(0, 0, 0, 0);
		for (PStoryboard stb : storyboards) {
			PBounds b1 = stb.getBoundsReference();
			if (b1.getX() < bounds.x)
				bounds.x = b1.getX();
			if (b1.getY() < bounds.y)
				bounds.y = b1.getY();
			if (b1.getWidth() + b1.getX() > bounds.width + bounds.x)
				bounds.width = b1.getWidth() + b1.getX() - bounds.x;
			if (b1.getHeight() + b1.getY() > bounds.height + bounds.y)
				bounds.height = b1.getHeight() + b1.getY() - bounds.y;
		}
		return bounds;
	}

	public static boolean accepts(Asset asset) {
		if (asset == null)
			return false;
		IMediaSupport mediaSupport = CoreActivator.getDefault().getMediaSupport(asset.getFormat());
		return mediaSupport == null || mediaSupport.testProperty(IMediaSupport.WEBGALLERY);
	}

	@Override
	protected void refreshAfterHistoryEvent(IUndoableOperation operation) {
		if (operation instanceof ExhibitionPropertiesOperation)
			setInput(gallery);
	}

	@Override
	protected PNode findExhibit(Point point, Point2D position) {
		PStoryboard pstoryboard = null;
		RECT1.x = (int) position.getX();
		RECT1.y = (int) position.getY();
		for (PStoryboard w : storyboards)
			if (w.fullIntersects(RECT1)) {
				pstoryboard = w;
				break;
			}
		if (pstoryboard != null) {
			int rawIx = computeSequenceNumber(position.getX() - pstoryboard.getOffset().getX());
			if (rawIx >= 1 && rawIx <= pstoryboard.getStoryboard().getExhibit().size()) {
				ListIterator<?> it = pstoryboard.getChildrenIterator();
				while (it.hasNext()) {
					Object next = it.next();
					if (next instanceof PWebExhibit) {
						PWebExhibit pExhibit = (PWebExhibit) next;
						int sequenceNo = pExhibit.exhibit.getSequenceNo();
						if (sequenceNo == rawIx)
							return pExhibit;
					}
				}
			}
		}
		return null;
	}

}
