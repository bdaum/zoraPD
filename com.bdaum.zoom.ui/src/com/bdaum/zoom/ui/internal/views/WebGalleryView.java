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
 * (c) 2009-2019 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.views;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.event.InputEvent;
import java.awt.geom.Point2D;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.piccolo2d.PNode;
import org.piccolo2d.event.PBasicInputEventHandler;
import org.piccolo2d.event.PInputEvent;
import org.piccolo2d.extras.swt.PSWTCanvas;
import org.piccolo2d.extras.swt.PSWTText;
import org.piccolo2d.util.PBounds;

import com.bdaum.aoModeling.runtime.IIdentifiableObject;
import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.webGallery.Storyboard;
import com.bdaum.zoom.cat.model.group.webGallery.StoryboardImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebExhibit;
import com.bdaum.zoom.cat.model.group.webGallery.WebExhibitImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebGallery;
import com.bdaum.zoom.cat.model.group.webGallery.WebGalleryImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IGalleryGenerator;
import com.bdaum.zoom.core.ISpellCheckingService;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.css.CSSProperties;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.css.internal.CssActivator;
import com.bdaum.zoom.image.ImageUtilities;
import com.bdaum.zoom.net.core.ftp.FtpAccount;
import com.bdaum.zoom.net.core.job.TransferJob;
import com.bdaum.zoom.operations.internal.gen.AbstractGalleryGenerator;
import com.bdaum.zoom.operations.jobs.WebGalleryJob;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.CaptionProcessor.CaptionConfiguration;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.dialogs.EditStoryBoardDialog;
import com.bdaum.zoom.ui.internal.dialogs.EditWebExibitDialog;
import com.bdaum.zoom.ui.internal.dialogs.SaveTemplateDialog;
import com.bdaum.zoom.ui.internal.dialogs.SelectWebExhibitDialog;
import com.bdaum.zoom.ui.internal.dialogs.WebGalleryEditDialog;
import com.bdaum.zoom.ui.internal.hover.IHoverContext;
import com.bdaum.zoom.ui.internal.job.DecorateJob;
import com.bdaum.zoom.ui.internal.operations.ExhibitionPropertiesOperation;
import com.bdaum.zoom.ui.internal.operations.WebGalleryPropertiesOperation;
import com.bdaum.zoom.ui.internal.views.SlideshowView.PSlide;
import com.bdaum.zoom.ui.internal.widgets.GalleryPanEventHandler;
import com.bdaum.zoom.ui.internal.widgets.GreekedPSWTText;
import com.bdaum.zoom.ui.internal.widgets.IAugmentedTextField;
import com.bdaum.zoom.ui.internal.widgets.PPanel;
import com.bdaum.zoom.ui.internal.widgets.PSWTAssetThumbnail;
import com.bdaum.zoom.ui.internal.widgets.PSWTButton;
import com.bdaum.zoom.ui.internal.widgets.TextEventHandler;
import com.bdaum.zoom.ui.internal.widgets.TextField;
import com.bdaum.zoom.ui.internal.widgets.ZPSWTImage;

@SuppressWarnings("restriction")
public class WebGalleryView extends AbstractPresentationView implements IHoverContext {

	public class SelectStoryBoardDialog extends ZTitleAreaDialog {

		private String title, msg;
		private Collection<Storyboard> input;
		private TableViewer viewer;
		private Storyboard selected;

		public SelectStoryBoardDialog(Shell parentShell, String title, String msg, Collection<Storyboard> input) {
			super(parentShell);
			this.title = title;
			this.msg = msg;
			this.input = input;
		}

		@Override
		public void create() {
			super.create();
			setTitle(title);
			setMessage(msg);
			updateButtons();
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite area = (Composite) super.createDialogArea(parent);
			Composite composite = new Composite(area, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			composite.setLayout(new FillLayout());
			viewer = new TableViewer(composite, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
			viewer.setContentProvider(ArrayContentProvider.getInstance());
			viewer.setLabelProvider(new ZColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					if (element instanceof Storyboard)
						return ((Storyboard) element).getTitle();
					return super.getText(element);
				}
			});
			viewer.addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					updateButtons();
				}
			});
			viewer.setInput(input);
			return area;
		}

		private void updateButtons() {
			getButton(OK).setEnabled(!viewer.getSelection().isEmpty());
		}

		@Override
		protected void okPressed() {
			selected = (Storyboard) viewer.getStructuredSelection().getFirstElement();
			super.okPressed();
		}

		public Storyboard getResult() {
			return selected;
		}

	}

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
				storyMap.remove(storyboard.getStringId());
				setPanAndZoomHandlers();
				updateStatusLine();
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
				updateStatusLine();
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
				updateStatusLine();
				return Status.OK_STATUS;
			}
			return Status.CANCEL_STATUS;
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
				storeSafelyAndUpdateIndex(storyboard, show, null);
				Point2D offset = added.getOffset();
				double ydiff = added.getHeight() + 15;
				surface.removeChild(added);
				for (ListIterator<?> it = surface.getChildrenIterator(); it.hasNext();) {
					Object obj = it.next();
					if (obj instanceof PStoryboard)
						if (((PStoryboard) obj).getOffset().getY() > offset.getY())
							((PStoryboard) obj).offset(0, -ydiff);
				}
				storyMap.remove(storyboard.getStringId());
				setPanAndZoomHandlers();
				updateStatusLine();
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
		private final Point2D newOffset;
		private PStoryboard oldStoryboard;
		private int oldSeqno;

		public MoveExhibitOperation(PWebExhibit moved, Point2D newOffset) {
			super(Messages.getString("WebGalleryView.move_exh_undo")); //$NON-NLS-1$
			this.moved = moved;
			this.newOffset = newOffset;
			this.oldSeqno = moved.exhibit.getSequenceNo();
		}

		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			final WebGallery gal = getGallery();
			if (gal != null) {
				PWebExhibit target = (PWebExhibit) findExhibit(newOffset);
				if (target != null) {
					List<Storyboard> storyboards = new ArrayList<>(2);
					String sbid = target.exhibit.getStoryboard_exhibit_parent();
					PStoryboard targetpStoryboard = storyMap.get(sbid);
					int newSeqNo = target == null ? targetpStoryboard.getChildrenCount() + 1
							: target.exhibit.getSequenceNo();
					sbid = moved.exhibit.getStoryboard_exhibit_parent();
					oldStoryboard = storyMap.get(sbid);
					storyboards.add(oldStoryboard.getStoryboard());
					if (oldStoryboard != targetpStoryboard)
						storyboards.add(targetpStoryboard.getStoryboard());
					oldSeqno = moved.exhibit.getSequenceNo();
					if (oldSeqno != newSeqNo || targetpStoryboard != oldStoryboard) {
						if (oldSeqno > newSeqNo || targetpStoryboard != oldStoryboard)
							++newSeqNo;
						removeExhibit(oldStoryboard.getStoryboard(), oldSeqno);
						moved.setSequenceNo(newSeqNo);
						insertExhibit(targetpStoryboard.getStoryboard(), moved);
						for (Storyboard sb : storyboards)
							updateSequenceNumbers(sb);
						newSeqNo = moved.exhibit.getSequenceNo();
						Core.getCore().getDbManager().safeTransaction(null, storyboards);
						updateActions(false);
						return Status.OK_STATUS;
					}
				}
			}
			return Status.CANCEL_STATUS;
		}

		@Override
		public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			return execute(monitor, info);
		}

		@Override
		public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			final WebGallery gal = getGallery();
			if (gal != null) {
				List<Storyboard> storyboards = new ArrayList<>(2);
				String sbid = moved.exhibit.getStoryboard_exhibit_parent();
				PStoryboard sourcepStoryboard = storyMap.get(sbid);
				removeExhibit(sourcepStoryboard.getStoryboard(), moved.exhibit.getSequenceNo());
				moved.setSequenceNo(oldSeqno);
				insertExhibit(oldStoryboard.getStoryboard(), moved);
				storyboards.add(oldStoryboard.getStoryboard());
				if (oldStoryboard != sourcepStoryboard)
					storyboards.add(sourcepStoryboard.getStoryboard());
				for (Storyboard sb : storyboards)
					updateSequenceNumbers(sb);
				Core.getCore().getDbManager().safeTransaction(null, storyboards);
				updateActions(false);
			}
			return Status.OK_STATUS;
		}
	}

	public class DeleteExhibitOperation extends AbstractOperation {

		private List<PWebExhibit> deleted;
		private boolean cut;

		public DeleteExhibitOperation(List<PWebExhibit> deleted, boolean cut) {
			super(Messages.getString("WebGalleryView.delete_exh_undo")); //$NON-NLS-1$
			this.deleted = deleted;
			this.cut = cut;
		}

		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			WebGalleryImpl gal = getGallery();
			if (gal != null) {
				if (cut && !deleted.isEmpty())
					clipboard.clear();
				List<IIdentifiableObject> toBeDeleted = new ArrayList<>(deleted.size());
				Set<IIdentifiableObject> storyboards = new HashSet<>(deleted.size());
				List<String> toBeIndexed = new ArrayList<>(deleted.size());
				List<WebExhibit> clipped = null;
				for (int i = deleted.size() - 1; i >= 0; i--) {
					WebExhibitImpl exhibit = deleted.get(i).exhibit;
					if (cut) {
						if (clipped == null)
							clipped = new ArrayList<>(deleted.size());
						clipped.add(exhibit);
					}
					toBeDeleted.add(exhibit);
					String sbid = exhibit.getStoryboard_exhibit_parent();
					Storyboard storyboard = storyMap.get(sbid).getStoryboard();
					storyboards.add(storyboard);
					toBeIndexed.add(exhibit.getAsset());
					removeExhibit(storyboard, exhibit.getSequenceNo());
				}
				for (IIdentifiableObject storyboard : storyboards)
					updateSequenceNumbers((Storyboard) storyboard);
				updateSelectionMarkers();
				storeSafelyAndUpdateIndex(toBeDeleted, storyboards, toBeIndexed);
				if (clipped != null) {
					clipboard.clear();
					clipboard.addAll(clipped);
				}
				updateActions(false);
				updateStatusLine();
				return Status.OK_STATUS;
			}
			return Status.CANCEL_STATUS;
		}

		@Override
		public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			return execute(monitor, info);
		}

		@Override
		public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			WebGalleryImpl gal = getGallery();
			if (gal != null) {
				List<IIdentifiableObject> toBeStored = new ArrayList<>(deleted.size() + 10);
				Set<Storyboard> storyboards = new HashSet<Storyboard>(15);
				List<String> toBeIndexed = new ArrayList<>(deleted.size());
				for (PWebExhibit pexhibit : deleted) {
					WebExhibit exhibit = pexhibit.exhibit;
					String sbid = exhibit.getStoryboard_exhibit_parent();
					Storyboard storyboard = storyMap.get(sbid).getStoryboard();
					insertExhibit(storyboard, pexhibit);
					toBeStored.add(exhibit);
					toBeIndexed.add(exhibit.getAsset());
					storyboards.add(storyboard);
				}
				for (Storyboard sb : storyboards) {
					updateSequenceNumbers(sb);
					toBeStored.add(sb);
				}
				storeSafelyAndUpdateIndex(null, toBeStored, toBeIndexed);
				updateActions(false);
				updateStatusLine();
			}
			return Status.OK_STATUS;
		}
	}

	public class DropAssetOperation extends AbstractOperation {

		private final AssetSelection selection;
		private final Point2D position;
		private List<PWebExhibit> added;
		private PStoryboard pstoryboard;
		private final boolean replace;
		private PWebExhibit deleted;

		public DropAssetOperation(AssetSelection selection, Point2D position, boolean replace) {
			super(Messages.getString("ExhibitionView.drop_images_undo")); //$NON-NLS-1$
			this.selection = selection;
			this.position = position;
			this.replace = replace;
			added = new ArrayList<PWebExhibit>(selection.size());
			pstoryboard = findStoryboard(position);
		}

		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			WebGallery gal = getGallery();
			if (gal != null && pstoryboard != null) {
				Storyboard storyboard = pstoryboard.getStoryboard();
				PWebExhibit target = (PWebExhibit) findExhibit(position);
				List<String> assetIds = new ArrayList<String>(selection.size());
				int seqNo = target == null ? pstoryboard.getChildrenCount() + 1 : target.exhibit.getSequenceNo();
				WebExhibit replaced = null;
				if (replace && target != null) {
					replaced = target.exhibit;
					assetIds.add(replaced.getAsset());
					deleted = removeExhibit(storyboard, seqNo);
					--seqNo;
				}
				boolean cleared = false;
				Set<IIdentifiableObject> toBeStored = new HashSet<>(selection.size() * 2);
				CaptionConfiguration captionConfig = captionProcessor.computeCaptionConfiguration(selection.getContext());
				for (Asset asset : selection.getAssets()) {
					if (!accepts(asset))
						continue;
					if (!cleared) {
						clearSelection();
						cleared = true;
					}
					String assetId = asset.getStringId();
					int rawIx = computeSequenceNumber(position.getX() - pstoryboard.getOffset().getX());
					int ix = Math.max(1, Math.min(rawIx, storyboard.getExhibit().size() + 1));
					WebExhibitImpl exhibit = replaced != null
							? new WebExhibitImpl(createSlideTitle(captionConfig, asset), ix, "", //$NON-NLS-1$
									replaced.getHtmlDescription(), asset.getName(), replaced.getDownloadable(),
									replaced.getIncludeMetadata(), assetId)
							:
							new WebExhibitImpl(createSlideTitle(captionConfig, asset), ix, "", false, //$NON-NLS-1$
									asset.getName(), true, true, assetId);
					replaced = null;
					insertIntoStoryboard(storyboard, exhibit, ix - 1);
					PWebExhibit pexhibit = makeExhibit(pstoryboard, exhibit);
					pexhibit.setSelected(true);
					lastSelection = recentSelection = pexhibit;
					added.add(pexhibit);
					toBeStored.add(exhibit);
					assetIds.add(assetId);
					ix++;
				}
				toBeStored.add(storyboard);
				updateSequenceNumbers(storyboard);
				storeSafelyAndUpdateIndex(null, toBeStored, assetIds);
				updateActions(false);
				updateStatusLine();
				return Status.OK_STATUS;
			}
			return Status.CANCEL_STATUS;
		}

		@Override
		public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			added.clear();
			return execute(monitor, info);
		}

		@Override
		public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			WebGallery gal = getGallery();
			if (gal != null) {
				List<IIdentifiableObject> toBeDeleted = new ArrayList<>(added.size());
				Set<IIdentifiableObject> toBeStored = new HashSet<>(5);
				List<String> assetIds = new ArrayList<>(added.size());
				for (int i = added.size() - 1; i >= 0; i--) {
					PWebExhibit pexhibit = added.get(i);
					WebExhibitImpl exhibit = pexhibit.exhibit;
					String sbid = exhibit.getStoryboard_exhibit_parent();
					Storyboard storyboard = storyMap.get(sbid).getStoryboard();
					removeExhibit(storyboard, exhibit.getSequenceNo());
					toBeDeleted.add(exhibit);
					toBeStored.add(storyboard);
					assetIds.add(exhibit.getAsset());
				}
				if (deleted != null) {
					String sbid = deleted.exhibit.getStoryboard_exhibit_parent();
					Storyboard storyboard = storyMap.get(sbid).getStoryboard();
					insertExhibit(storyboard, deleted);
				}
				updateSequenceNumbers(pstoryboard.getStoryboard());
				updateSelectionMarkers();
				storeSafelyAndUpdateIndex(toBeDeleted, toBeStored, assetIds);
				updateActions(false);
				updateStatusLine();
			}
			return Status.OK_STATUS;

		}
	}

	public class PasteExhibitsOperation extends AbstractOperation {

		private Point2D position;
		private List<PWebExhibit> added;
		private PStoryboard pstoryboard;

		public PasteExhibitsOperation(Point2D position) {
			super(Messages.getString("WebGalleryView.paste_exhibits")); //$NON-NLS-1$
			this.position = position;
			added = new ArrayList<>(clipboard.size());
			pstoryboard = findStoryboard(position);
		}

		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			WebGallery gal = getGallery();
			if (gal != null && !clipboard.isEmpty() && pstoryboard != null) {
				PWebExhibit target = (PWebExhibit) findExhibit(position);
				List<String> assetIds = new ArrayList<String>(clipboard.size());
				int seqNo = target == null ? pstoryboard.getChildrenCount() + 1 : target.exhibit.getSequenceNo();
				if (!clipboard.isEmpty())
					clearSelection();
				List<IIdentifiableObject> toBeStored = new ArrayList<>(clipboard.size() + 1);
				for (IIdentifiableObject exhibit : clipboard) {
					WebExhibitImpl copy = cloneExhibit((WebExhibitImpl) exhibit);
					insertIntoStoryboard(pstoryboard.getStoryboard(), copy, seqNo);
					PWebExhibit pexhibit = makeExhibit(pstoryboard, copy);
					pexhibit.setSelected(true);
					lastSelection = recentSelection = pexhibit;
					added.add(pexhibit);
					toBeStored.add(copy);
					assetIds.add(copy.getAsset());
					++seqNo;
				}
				updateSequenceNumbers(pstoryboard.getStoryboard());
				toBeStored.add(pstoryboard.getStoryboard());
				storeSafelyAndUpdateIndex(null, toBeStored, assetIds);
				updateActions(false);
				updateStatusLine();
				return Status.OK_STATUS;
			}
			return Status.CANCEL_STATUS;
		}

		@Override
		public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			added.clear();
			return execute(monitor, info);
		}

		@Override
		public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			WebGallery gal = getGallery();
			if (gal != null && pstoryboard != null) {
				List<IIdentifiableObject> toBeDeleted = new ArrayList<>(added.size());
				List<String> assetIds = new ArrayList<String>(added.size());
				Storyboard storyboard = pstoryboard.getStoryboard();
				for (int i = added.size() - 1; i >= 0; i--) {
					PWebExhibit pexhibit = added.get(i);
					WebExhibitImpl exhibit = pexhibit.exhibit;
					removeExhibit(storyboard, exhibit.getSequenceNo());
					toBeDeleted.add(exhibit);
					assetIds.add(exhibit.getAsset());
				}
				updateSequenceNumbers(pstoryboard.getStoryboard());
				updateSelectionMarkers();
				storeSafelyAndUpdateIndex(toBeDeleted, storyboard, assetIds);
				updateActions(false);
				updateStatusLine();
			}
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
			return !storyMap.isEmpty();
		}

		@Override
		protected void doRun(IProgressMonitor monitor) {
			final Display display = canvas.getDisplay();
			IDbManager dbManager = Core.getCore().getDbManager();
			offline.clear();
			for (PStoryboard storyboard : storyMap.values())
				for (Object child : storyboard.getChildrenReference().toArray()) {
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
										penColor = titleForegroundColor;
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
			offlineImages = offline;
		}
	}

	@SuppressWarnings("serial")
	public class PWebExhibit extends PPresentationItem {

		private WebExhibitImpl exhibit;
		private TextField description;
		private PSWTButton propButton;
		private GreekedPSWTText seqNo;

		public PWebExhibit(final PSWTCanvas canvas, PStoryboard pstoryboard, WebExhibitImpl exhibit, int y) {
			this.exhibit = exhibit;
			setPickable(false);
			// Image
			pImage = new PSWTAssetThumbnail(canvas, WebGalleryView.this,
					Core.getCore().getDbManager().obtainAsset(exhibit.getAsset()));
			PBounds bounds = pImage.getBoundsReference();
			addChild(pImage);
			configureImage();
			setBounds(0, 0, CELLSIZE, STORYBOARDHEIGHT);
			// select frame
			pImage.addChild(createSelectionFrame(bounds));
			// prop button
			Image propImage = Icons.smallProperties.getImage();
			propButton = new PSWTButton(canvas, propImage, Messages.getString("WebGalleryView.slide_properties")); //$NON-NLS-1$
			Rectangle propbounds = propImage.getBounds();
			propButton.setOffset(STORYBOARDMARGINS, IMAGESIZE + CSIZE);
			addChild(propButton);
			// Seqno
			Color strokePaint = (Color) pstoryboard.getStrokePaint();
			seqNo = new GreekedPSWTText(createSequenceNo(exhibit), new Font("Arial", //$NON-NLS-1$
					Font.BOLD, 9));
			seqNo.setGreekThreshold(5);
			seqNo.setTransparent(true);
			seqNo.setPenColor(strokePaint);
			int textHorMargins = 2 * STORYBOARDMARGINS + propbounds.width;
			int textYPos = IMAGESIZE + CSIZE;
			seqNo.setOffset(textHorMargins, textYPos);
			seqNo.setPickable(true);
			addChild(seqNo);
			// caption
			caption = createTextLine(this, exhibit.getCaption(), 5,
					(int) (IMAGESIZE - 2 * textHorMargins - seqNo.getWidth()),
					(int) (textHorMargins + seqNo.getWidth()), textYPos, titleForegroundColor, null, CAPTIONFONT,
					ISpellCheckingService.TITLEOPTIONS, SWT.SINGLE);
			// legend
			description = createTextLine(this, getDescription(exhibit), 5, IMAGESIZE - 2 * textHorMargins - 2 * CSIZE,
					textHorMargins + CSIZE, textYPos + 2 * CSIZE, foregroundColor, null, DESCRIPTIONFONT,
					ISpellCheckingService.DESCRIPTIONOPTIONS, SWT.SINGLE);
			// position and bounds
			setOffset(0, y);
			installHandleEventHandlers(pImage, false, false, this);
			addInputEventListener(new PBasicInputEventHandler() {
				@Override
				public void mousePressed(PInputEvent event) {
					setPickedNode(event.getPickedNode());
					if (getAdapter(IPresentationItem.class) instanceof PSlide)
						event.getPickedNode().raiseToTop();
				}

				@Override
				public void mouseReleased(PInputEvent event) {
					PNode pickedNode = getPickedNode();
					if (pickedNode == propButton || pickedNode == seqNo || event.getClickCount() == 1)
						editExhibitProperties();
				}
			});
			pImage.addInputEventListener(new PBasicInputEventHandler() {
				@Override
				public void mousePressed(PInputEvent event) {
					PNode pickedNode = event.getPickedNode();
					setPickedNode(pickedNode);
					if (event.isLeftMouseButton() && !event.isShiftDown() && !event.isAltDown()
							&& (pickedNode instanceof PSWTAssetThumbnail))
						selectExhibit((PWebExhibit) pickedNode.getParent(), event.getModifiers(),
								event.getClickCount());
				}
			});
		}

		@Override
		public void raiseToTop() {
			super.raiseToTop();
			if (exhibitAtFront != null)
				exhibitAtFront.description.setPenColor(foregroundColor);
			exhibitAtFront = this;
			description.setPenColor(selectionForegroundColor);
		}

		@Override
		public void lowerToBottom() {
			super.lowerToBottom();
			if (exhibitAtFront == this)
				exhibitAtFront = null;
			description.setPenColor(foregroundColor);
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
			if (des != null && !des.isEmpty())
				return des;
			return Messages.getString("WebGalleryView.description"); //$NON-NLS-1$
		}

		public void processTextEvent(TextField focus) {
			String text = focus.getText();
			int type = -1;
			if (focus == caption) {
				if (!text.equals(exhibit.getCaption()))
					type = EditTextOperation.CAPTION;
			} else if (focus == description && !text.equals(exhibit.getDescription()))
				type = EditTextOperation.DESCRIPTION;
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
			pImage.lowerToBottom();
			setCaptionAndDescription(exh);
		}

		private void configureImage() {
			pImage.setPickable(true);
			PBounds bounds = pImage.getBoundsReference();
			double width = bounds.getWidth();
			double height = bounds.getHeight();
			double scale = Math.min(IMAGESIZE / width, IMAGESIZE / height);
			pImage.setOffset(STORYBOARDMARGINS + ((IMAGESIZE - width * scale) / 2),
					STORYBOARDMARGINS + ((IMAGESIZE - height * scale) / 2));
			pImage.scale(scale);
		}

		public void setSequenceNo(int no) {
			exhibit.setSequenceNo(no);
			seqNo.setText(createSequenceNo(exhibit));
			Rectangle propbounds = propButton.getImage().getBounds();
			caption.setOffset(2 * STORYBOARDMARGINS + propbounds.width + seqNo.getWidth(), caption.getOffset().getY());
			setOffset((no - 1) * CELLSIZE + 2 * LARGEBUTTONSIZE, 0);
		}

		private String createSequenceNo(WebExhibit exh) {
			return exh.getSequenceNo() + " - "; //$NON-NLS-1$
		}

		public void setPenColor(Color color) {
			seqNo.setPenColor(color);
			description.setPenColor(color);
			description.setSelectedBgColor(selectionBackgroundColor);
		}

		public void setBackgroundColor(Color color) {
			description.setBackgroundColor(color);
		}

		public void setTitleColor(Color color) {
			caption.setPenColor(color);
			caption.setSelectedBgColor(selectionBackgroundColor);
		}

		public String getAssetId() {
			return exhibit.getAsset();
		}

	}

	@SuppressWarnings("serial")
	public class PStoryboard extends PPanel {

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
			createTitle(canvas, storyboard.getTitle(), STORYBOARDMARGINS, STORYBOARDMARGINS, titleForegroundColor,
					(Color) getPaint(), 18);
			setPickable(true);
		}

		public Storyboard getStoryboard() {
			return (Storyboard) data;
		}

		public void setStoryboard(StoryboardImpl storyboard) {
			this.data = storyboard;
		}

		@Override
		public void setPaint(Paint newPaint) {
			super.setPaint(newPaint);
			for (ListIterator<?> it = getChildrenIterator(); it.hasNext();) {
				Object child = it.next();
				if (child instanceof TextField)
					((TextField) child).setBackgroundColor((Color) newPaint);
				else if (child instanceof PWebExhibit)
					((PWebExhibit) child).setBackgroundColor((Color) newPaint);
			}
		}

		@Override
		public void setStrokeColor(Paint strokeColor) {
			super.setStrokeColor(strokeColor);
			for (ListIterator<?> it = getChildrenIterator(); it.hasNext();) {
				Object child = it.next();
				if (child instanceof PSWTText)
					((PSWTText) child).setPenColor((Color) strokeColor);
				else if (child instanceof TextField) {
					((TextField) child).setPenColor((Color) strokeColor);
					((TextField) child).setSelectedBgColor(selectionBackgroundColor);
				} else if (child instanceof PWebExhibit) {
					((PWebExhibit) child).setPenColor(foregroundColor);
					((PWebExhibit) child).setTitleColor(titleForegroundColor);
				}
			}
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
			for (ListIterator<?> it = getChildrenIterator(); it.hasNext();) {
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

	}

	public static final String ID = "com.bdaum.zoom.ui.WebGalleryView"; //$NON-NLS-1$
	public static final String WEBGALLERY_PERSPECTIVE = "com.bdaum.zoom.PresentationPerspective"; //$NON-NLS-1$
	private static final String LAST_GALLERY = "com.bdaum.zoom.lastGallery"; //$NON-NLS-1$
	private static final int STORYBOARDWIDTH = 1500;
	private static final int CELLSIZE = 250;
	private static final int STORYBOARDMARGINS = 5;
	private static final int CSIZE = 12;
	private static final int STORYBOARDHEIGHT = CELLSIZE + 5 * CSIZE;
	private static final int IMAGESIZE = CELLSIZE - 2 * STORYBOARDMARGINS;
	protected static final int STORYBOARDDIST = STORYBOARDHEIGHT + CSIZE;
	private static final int LARGEBUTTONSIZE = 64;
	private Map<String, PStoryboard> storyMap = new HashMap<>(13);
	private Map<String, PWebExhibit> exhibitMap = new HashMap<>(521);
	protected Color wallPaint = new Color(255, 255, 250);
	protected static final Font CAPTIONFONT = new Font("Arial", Font.BOLD, 9); //$NON-NLS-1$
	protected static final Font DESCRIPTIONFONT = new Font("Arial", Font.PLAIN, 9); //$NON-NLS-1$

	private WebGalleryImpl gallery;
	private Action addStoryBoardAction, generateAction, saveAction, deleteStoryboardAction, editStoryboardAction;

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		if (memento != null)
			lastSessionPresentation = memento.getString(LAST_GALLERY);
	}

	public WebExhibitImpl cloneExhibit(WebExhibitImpl original) {
		return new WebExhibitImpl(original.getCaption(), original.getSequenceNo(), original.getDescription(),
				original.getHtmlDescription(), original.getAltText(), original.getDownloadable(),
				original.getDownloadable(), original.getAsset());
	}

	@Override
	public void saveState(IMemento memento) {
		if (memento != null && gallery != null)
			memento.putString(LAST_GALLERY, gallery.getStringId());
		super.saveState(memento);
	}

	private WebGalleryImpl setGallery(IIdentifiableObject presentation) {
		if (presentation instanceof WebGalleryImpl) {
			gallery = (WebGalleryImpl) presentation;
			setPartName(gallery.getName());
			if (gallery.getStoryboard().isEmpty())
				gallery.addStoryboard(
						new StoryboardImpl(Messages.getString("WebGalleryView.storyboard_1"), 1, false, "", //$NON-NLS-1$ //$NON-NLS-2$
								0, false, true, true, true));
		} else if (gallery == null)
			setPartName(Messages.getString("WebGalleryView.web_gallery")); //$NON-NLS-1$
		return this.gallery;
	}

	@Override
	protected String createHoverText(PNode thumbnail) {
		PNode parent = thumbnail.getParent();
		if (parent instanceof PWebExhibit)
			return UiActivator.getDefault().getHoverManager().getHoverText(
					"com.bdaum.zoom.ui.hover.webexhibition.exhibit", ((PWebExhibit) parent).exhibit, this); //$NON-NLS-1$
		return ""; //$NON-NLS-1$
	}

	@Override
	protected String createHoverTitle(PNode thumbnail) {
		PNode parent = thumbnail.getParent();
		if (parent instanceof PWebExhibit)
			return UiActivator.getDefault().getHoverManager().getHoverTitle(
					"com.bdaum.zoom.ui.hover.webexhibition.exhibit", ((PWebExhibit) parent).exhibit, this); //$NON-NLS-1$
		return ""; //$NON-NLS-1$
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		canvas.setData(CSSProperties.ID, CSSProperties.WEBGALLERY);
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

			private boolean dragged;

			@Override
			public void keyPressed(PInputEvent event) {
				if (textEventHandler.hasFocus())
					textEventHandler.keyPressed(event);
				else
					pan(event);
			}

			@Override
			public void mouseMoved(PInputEvent event) {
				currentMousePosition = event.getPosition();
			}

			@Override
			public void mousePressed(PInputEvent event) {
				dragged = false;
				setPickedNode(event.getPickedNode());
			}

			@Override
			public void mouseDragged(PInputEvent event) {
				dragged = true;
				textEventHandler.mouseDragged(event);
			}

			@Override
			public void mouseReleased(PInputEvent event) {
				positionX = null;
				if (!dragged) {
					textEventHandler.mouseReleased(event);
					if (event.getClickCount() == 2)
						toggleTransform(event.getCamera());
					else if (event.isRightMouseButton()) {
						positionX = event.getPosition();
						positionRelativeToCamera = event.getPositionRelativeTo(canvas.getCamera());
					} else if (event.isControlDown()) {
						PNode picked = getPickedNode();
						if (picked == surface)
							propertiesAction.run();
						else if (picked instanceof PStoryboard)
							editStoryboardAction.run();
					}
				}
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
		installListeners();
		hookContextMenu();
		contributeToActionBars();
	}

	@Override
	protected void startup() {
		if (gallery == null)
			setGallery(Core.getCore().getDbManager().obtainById(WebGalleryImpl.class, lastSessionPresentation));
		if (isVisible()) {
			if (gallery != null)
				setInput(gallery);
			addCatalogListener();
			setDecorator(canvas, new GalleryDecorateJob());
			updateActions(false);
		} else if (gallery != null)
			isDirty = true;
	}

	@Override
	protected void fillLocalToolBar(IToolBarManager manager) {
		super.fillLocalToolBar(manager);
		manager.add(addStoryBoardAction);
		manager.add(new Separator());
		manager.add(generateAction);
		manager.add(saveAction);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
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
		updateActions(true);
		boolean writable = !dbIsReadonly();
		PNode pickedNode = getPickedNode();
		if (pickedNode != null && pickedNode.getParent() instanceof PWebExhibit) {
			if (writable) {
				manager.add(cutAction);
				manager.add(new Separator());
			}
			addCommonContextActions(manager);
		} else {
			if (pickedNode != null && pickedNode.getParent() instanceof PStoryboard)
				pickedNode = pickedNode.getParent();
			if (pickedNode instanceof PStoryboard) {
				if (writable)
					manager.add(pasteAction);
				manager.add(new Separator());
				manager.add(editStoryboardAction);
				manager.add(deleteStoryboardAction);
			}
		}
		manager.add(new Separator());
		manager.add(gotoExhibitAction);
		if (gotoLastSelectedAction.isEnabled())
			manager.add(gotoLastSelectedAction);
		super.fillContextMenu(manager);
	}

	@Override
	protected void updatePresentation(Collection<? extends Asset> assets) {
		Map<String, Asset> amap = new HashMap<String, Asset>(assets.size());
		for (Asset a : assets)
			amap.put(a.getStringId(), a);
		for (PStoryboard pstoryboard : storyMap.values())
			for (ListIterator<?> it = pstoryboard.getChildrenIterator(); it.hasNext();) {
				Object next = it.next();
				if (next instanceof PWebExhibit) {
					PWebExhibit pexhibit = (PWebExhibit) next;
					Asset asset = amap.get(pexhibit.getAssetId());
					if (asset != null) {
						pexhibit.update(pexhibit.exhibit, asset);
						break;
					}
				}
			}
	}

	@Override
	public void updateActions(boolean force) {
		if (addStoryBoardAction != null && (isVisible() || force)) {
			boolean hasGallery = gallery != null;
			addStoryBoardAction.setEnabled(hasGallery && !dbIsReadonly());
			propertiesAction.setEnabled(hasGallery);
			saveAction.setEnabled(hasGallery);
			generateAction.setEnabled(hasGallery);
			gotoExhibitAction.setEnabled(hasGallery);
			super.updateActions(force);
		}
	}

	private void setupGallery(IViewSite site, WebGalleryImpl gallery) {
		if (gallery != null) {
			int h = Math.max(2, gallery.getStoryboard().size()) * STORYBOARDDIST;
			surface.setBounds(-3000d, -3000d, (STORYBOARDWIDTH + 6000d), (h + 6000d));
			surface.setScale(site.getShell().getDisplay().getPrimaryMonitor().getBounds().height / (2d * h));
		}
	}

	@Override
	protected void cleanUp() {
		try {
			for (PStoryboard pstoryboard : storyMap.values()) {
				pstoryboard.removeAllChildren();
				PNode parent = pstoryboard.getParent();
				if (parent != null)
					parent.removeChild(pstoryboard);
			}
		} catch (SWTException e) {
			// do nothing
		}
		storyMap.clear();
		super.cleanUp();
	}

	@Override
	protected void setColor(Control control) {
		CssActivator.getDefault().applyExtendedStyle(control, this);
		surface.setPaint(wallPaint = UiUtilities.getAwtBackground(control, null));
		for (PStoryboard pstoryboard : storyMap.values()) {
			pstoryboard.setPaint(wallPaint);
			pstoryboard.setStrokeColor(selectionBackgroundColor);
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
				show = WebGalleryEditDialog.openWebGalleryEditDialog(getSite().getShell(), WebGalleryView.this, null, show, show.getName(),
						false, true, null);
				if (show != null) {
					performOperation(new WebGalleryPropertiesOperation(backup, setGallery(show)));
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
				WebGallery gal = getGallery();
				if (gal != null) {
					List<WebExhibitImpl> exhibits = getAllExhibits(gal);
					SelectWebExhibitDialog dialog = new SelectWebExhibitDialog(getSite().getShell(), exhibits);
					dialog.create();
					org.eclipse.swt.graphics.Point pos = canvas.toDisplay((int) positionRelativeToCamera.getX(),
							(int) positionRelativeToCamera.getY());
					pos.x += 10;
					pos.y += 10;
					dialog.getShell().setLocation(pos);
					if (dialog.open() == Window.OK)
						revealExhibit(dialog.getResult());
				}
			}

			private List<WebExhibitImpl> getAllExhibits(WebGallery gal) {
				List<WebExhibitImpl> exhibits = new ArrayList<WebExhibitImpl>(300);
				for (Storyboard sb : gal.getStoryboard()) {
					for (String id : sb.getExhibit()) {
						PWebExhibit pexhibit = exhibitMap.get(id);
						exhibits.add(pexhibit.exhibit);
					}
				}
				return exhibits;
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
		storyMap.put(storyboard.getStringId(), pstoryboard);
		surface.addChild(pstoryboard);
		pstoryboard.setOffset(0, y);
		int i = 0;
		List<String> ids = new ArrayList<String>(storyboard.getExhibit().size());
		for (String id : storyboard.getExhibit()) {
			WebExhibitImpl exhibit = dbManager.obtainById(WebExhibitImpl.class, id);
			if (exhibit != null) {
				ids.add(id);
				PWebExhibit pexhibit = makeExhibit(pstoryboard, exhibit);
				pexhibit.setSequenceNo(++i);
				if (progrBar != null)
					progrBar.worked(1);
			}
		}
		storyboard.setExhibit(ids);
		setColor(canvas);
		pstoryboard.updateColors();
		setPanAndZoomHandlers();
		return pstoryboard;
	}

	private void setPanAndZoomHandlers() {
		setPanAndZoomHandlers(-13, -17);
	}

	protected PWebExhibit makeExhibit(PStoryboard pstoryboard, WebExhibitImpl exhibit) {
		PWebExhibit pExhibit = new PWebExhibit(canvas, pstoryboard, exhibit, 0);
		PBounds bounds = pstoryboard.getBoundsReference();
		int w = pstoryboard.getChildrenCount() * CELLSIZE;
		if (w > bounds.getWidth())
			pstoryboard.setWidth(w);
		pstoryboard.addChild(pExhibit);
		exhibitMap.put(pExhibit.exhibit.getStringId(), pExhibit);
		return pExhibit;
	}

	private static int computeSequenceNumber(double x) {
		return Math.max(0, (int) ((x - 2 * LARGEBUTTONSIZE) / CELLSIZE + 1));
	}

	protected void moveExhibit(PWebExhibit moved, Point2D newOffset) {
		performOperation(new MoveExhibitOperation(moved, newOffset));
	}

	@Override
	protected void pastePresentationItems() {
		if (getGallery() != null)
			performOperation(new PasteExhibitsOperation(positionX));
	}

	private PStoryboard findStoryboard(Point2D position) {
		RECT1.x = (int) position.getX();
		RECT1.y = (int) position.getY();
		for (PStoryboard w : storyMap.values())
			if (w.fullIntersects(RECT1))
				return w;
		return null;
	}

	@Override
	protected void deletePresentationItem(IPresentationItem picked, boolean cut, boolean multiple) {
		if (getGallery() != null)
			performOperation(new DeleteExhibitOperation(getSelectedExhibits((PWebExhibit) picked, multiple), cut));
	}

	protected void selectExhibit(PWebExhibit pExhibit, int modifiers, int clicks) {
		if ((modifiers & InputEvent.CTRL_MASK) != 0) {
			pExhibit.setSelected(!pExhibit.isSelected());
			if (pExhibit.isSelected())
				recentSelection = pExhibit;
			else
				updateSelectionMarkers();
		} else if ((modifiers & InputEvent.SHIFT_MASK) != 0) {
			PStoryboard pStory = (PStoryboard) pExhibit.getParent();
			List<String> entries = pStory.getStoryboard().getExhibit();
			int index = pExhibit.exhibit.getSequenceNo() - 1;
			if (pExhibit.isSelected()) {
				int lastIndex = -1;
				for (int i = 0; i < entries.size(); i++)
					if (exhibitMap.get(entries.get(i)) == lastSelection) {
						lastIndex = i;
						break;
					}
				if (lastIndex < 0) {
					int lower = 0;
					int upper = entries.size() - 1;
					for (int i = index - 1; i >= 0; i--) {
						if (!exhibitMap.get(entries.get(i)).isSelected())
							break;
						lower = i;
					}
					for (int i = index + 1; i < entries.size(); i++) {
						if (!exhibitMap.get(entries.get(i)).isSelected())
							break;
						upper = i;
					}
					if (index - lower < upper - index)
						lastIndex = lower;
					else
						lastIndex = upper;
				}
				if (lastIndex < index)
					for (int i = index - 1; i >= lastIndex; i--)
						exhibitMap.get(entries.get(i)).setSelected(false);
				else
					for (int i = index + 1; i <= lastIndex; i++)
						exhibitMap.get(entries.get(i)).setSelected(false);
			} else {
				recentSelection = pExhibit;
				for (int i = index - 1; i >= 0; i--)
					if (exhibitMap.get(entries.get(i)).isSelected()) {
						for (int j = i + 1; j <= index; j++)
							exhibitMap.get(entries.get(j)).setSelected(true);
						lastSelection = pExhibit;
						return;
					}
				for (int i = index + 1; i < entries.size(); i++)
					if (exhibitMap.get(entries.get(i)).isSelected()) {
						for (int j = i - 1; j >= index; j--)
							exhibitMap.get(entries.get(j)).setSelected(true);
					}
			}
			lastSelection = pExhibit;
		} else {
			clearSelection();
			recentSelection = pExhibit;
			if (clicks == 2) {
				PStoryboard pStory = (PStoryboard) pExhibit.getParent();
				for (ListIterator<?> iterator = pStory.getChildrenIterator(); iterator.hasNext();) {
					Object next = iterator.next();
					if (next instanceof PWebExhibit)
						((PWebExhibit) next).setSelected(true);
				}
			} else
				pExhibit.setSelected(true);
		}
	}

	private void clearSelection() {
		for (PStoryboard sb : storyMap.values())
			for (ListIterator<?> childrenIterator = sb.getChildrenIterator(); childrenIterator.hasNext();) {
				Object next = childrenIterator.next();
				if (next instanceof PWebExhibit)
					((PWebExhibit) next).setSelected(false);
			}
		lastSelection = recentSelection = null;
	}

	public void updateSelectionMarkers() {
		for (PStoryboard sb : storyMap.values())
			for (ListIterator<?> childrenIterator = sb.getChildrenIterator(); childrenIterator.hasNext();) {
				Object next = childrenIterator.next();
				if (next instanceof PWebExhibit && ((PWebExhibit) next).isSelected())
					return;
			}
		lastSelection = recentSelection = null;
	}

	protected List<PWebExhibit> getSelectedExhibits(PWebExhibit picked, boolean multiple) {
		boolean hit = false;
		List<PWebExhibit> selected = new ArrayList<>();
		if (gallery != null) {
			if (multiple)
				for (Storyboard storyboard : gallery.getStoryboard()) {
					for (String id : storyboard.getExhibit()) {
						PWebExhibit pExhibit = exhibitMap.get(id);
						if (pExhibit.isSelected()) {
							selected.add(pExhibit);
							hit |= picked == pExhibit;
						}
					}
				}
			if (!hit) {
				selected.clear();
				selected.add(picked);
				selectExhibit(picked, 0, 1);
			}
		}
		return selected;
	}

	protected PWebExhibit removeExhibit(Storyboard storyboard, int seqNo) {
		String id = storyboard.getExhibit().remove(seqNo - 1);
		PPresentationItem pexibit = exhibitMap.remove(id);
		PStoryboard pStoryboard = storyMap.get(storyboard.getStringId());
		return (PWebExhibit) pStoryboard.removeChild(pexibit);
	}

	protected void insertExhibit(Storyboard storyboard, PWebExhibit pexhibit) {
		int seqNo = pexhibit.exhibit.getSequenceNo();
		PStoryboard pStoryboard = storyMap.get(storyboard.getStringId());
		pStoryboard.addChild(pexhibit);
		WebExhibitImpl exhibit = pexhibit.exhibit;
		exhibitMap.put(exhibit.getStringId(), pexhibit);
		insertIntoStoryboard(storyboard, exhibit, seqNo - 1);
	}

	private static void insertIntoStoryboard(Storyboard storyboard, WebExhibit exhibit, int index) {
		if (index >= storyboard.getExhibit().size())
			storyboard.addExhibit(exhibit.getStringId());
		else
			storyboard.getExhibit().add(index, exhibit.getStringId());
		exhibit.setStoryboard_exhibit_parent(storyboard.getStringId());
	}

	protected void updateSequenceNumbers(Storyboard storyboard) {
		int i = 1;
		for (String id : storyboard.getExhibit()) {
			PWebExhibit pexhibit = exhibitMap.get(id);
			if (pexhibit.exhibit.getSequenceNo() != i)
				pexhibit.setSequenceNo(i);
			++i;
		}
	}

	protected WebGalleryImpl getGallery() {
		if (gallery == null && !dbIsReadonly())
			setGallery(WebGalleryEditDialog.openWebGalleryEditDialog(getSite().getShell(), null, null, null,
					Messages.getString("WebGalleryView.create_new_web_gallery"), false, false, null));//$NON-NLS-1$
		return gallery;
	}

	@Override
	public void setInput(IdentifiableObject presentation) {
		setGallery(presentation);
		if (started) {
			super.setInput(presentation);
			if (presentation instanceof WebGalleryImpl) {
				setupGallery(getViewSite(), gallery);
				int n = 0;
				List<Storyboard> storyboards = gallery.getStoryboard();
				for (Storyboard sb : storyboards)
					n += sb.getExhibit().size();
				beginTask(n);
				int y = 2 * STORYBOARDMARGINS;
				double w = STORYBOARDWIDTH;
				for (Storyboard sb : storyboards)
					w = Math.max(w,
							addStoryboard(sb, y += STORYBOARDDIST, progressBar).getBoundsReference().getWidth());
				updateSurfaceBounds(w + 2 * STORYBOARDMARGINS, storyboards.size() * (STORYBOARDHEIGHT + STORYBOARDDIST)
						+ 2 * STORYBOARDMARGINS - STORYBOARDDIST);
				setColor(canvas);
				updateActions(false);
				updateStatusLine();
				endTask();
			}
		}
	}

	@Override
	protected void show() {
		if (isDirty && gallery != null)
			setInput(gallery);
		super.show();
	}

	public void generate(final boolean save) {
		WebGalleryImpl show = getGallery();
		if (show != null) {
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
			} else {
				String selectedEngine = show.getSelectedEngine();
				boolean isFtp = show.getIsFtp();
				String outputFolder = (isFtp) ? show.getFtpDir() : show.getOutputFolder();
				while (selectedEngine == null || outputFolder == null || selectedEngine.isEmpty()
						|| outputFolder.isEmpty()) {
					show = WebGalleryEditDialog.openWebGalleryEditDialog(getSite().getShell(), null, null, show,
							show.getName(), true, false, Messages.getString("WebGalleryView.select_web_gallery")); //$NON-NLS-1$
					if (show == null)
						return;
					selectedEngine = show.getSelectedEngine();
					isFtp = show.getIsFtp();
					outputFolder = (isFtp) ? show.getFtpDir() : show.getOutputFolder();
				}
				for (IExtension ext : Platform.getExtensionRegistry()
						.getExtensionPoint(UiActivator.PLUGIN_ID, "galleryGenerator").getExtensions()) //$NON-NLS-1$
					for (IConfigurationElement el : ext.getConfigurationElements())
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
			Storyboard selectedStoryboard = null;
			if (!Boolean.parseBoolean(el.getAttribute("sections")) && show.getStoryboard().size() > 1) { //$NON-NLS-1$
				SelectStoryBoardDialog dialog = new SelectStoryBoardDialog(getSite().getShell(),
						Messages.getString("WebGalleryView.multiple_storyboards"), //$NON-NLS-1$
						NLS.bind(Messages.getString("WebGalleryView.does_not_support_multiple_storyboards"), //$NON-NLS-1$
								el.getAttribute("name")), //$NON-NLS-1$
						show.getStoryboard());
				if (dialog.open() != SelectStoryBoardDialog.OK)
					return true;
				selectedStoryboard = dialog.getResult();
			}
			String maxImages = el.getAttribute("maxImages"); //$NON-NLS-1$
			if (maxImages != null) {
				try {
					int max = Integer.parseInt(maxImages);
					int n = 0;
					for (Storyboard storyboard : show.getStoryboard())
						max -= storyboard.getExhibit().size();
					if (n > max && !AcousticMessageDialog.openQuestion(getSite().getShell(),
							Messages.getString("WebGalleryView.too_many"), //$NON-NLS-1$
							NLS.bind(Messages.getString("WebGalleryView.n_exceeds_max"), //$NON-NLS-1$
									new Object[] { n, max, el.getAttribute("name") }))) //$NON-NLS-1$
						return true;
				} catch (NumberFormatException e) {
					// ignore
				}
			}
			String aspectRatio = el.getAttribute("aspectRatio"); //$NON-NLS-1$
			if (aspectRatio != null) {
				aspectRatio.trim();
				if (!aspectRatio.isEmpty()) {
					double sample = Double.NaN;
					double tolerance = 5;
					int p = aspectRatio.indexOf(' ');
					if (p > 0) {
						try {
							sample = Double.parseDouble(aspectRatio.substring(0, p));
						} catch (NumberFormatException e) {
							// Leave at NaN
						}
						aspectRatio = aspectRatio.substring(p + 1);
					}
					if (aspectRatio.endsWith("%")) { //$NON-NLS-1$
						try {
							tolerance = Double.parseDouble(aspectRatio.substring(0, aspectRatio.length() - 1));
						} catch (NumberFormatException e) {
							// Leave at 5
						}
					}
					List<String> errands = new ArrayList<>();
					IDbManager dbManager = Core.getCore().getDbManager();
					lp: for (Storyboard storyboard : show.getStoryboard()) {
						for (String exhibitId : storyboard.getExhibit()) {
							WebExhibitImpl exhibit = dbManager.obtainById(WebExhibitImpl.class, exhibitId);
							if (exhibit != null) {
								AssetImpl asset = dbManager.obtainAsset(exhibit.getAsset());
								if (asset != null & asset.getHeight() > 0) {
									double prop = (double) asset.getWidth() / asset.getHeight();
									if (Double.isNaN(sample))
										sample = prop;
									else if (Math.abs(sample - prop) > sample * tolerance / 100d) {
										if (p > 0)
											errands.add((asset.getName()));
										else {
											if (!AcousticMessageDialog.openQuestion(getSite().getShell(),
													Messages.getString("WebGalleryView.unequal_proportions"), //$NON-NLS-1$
													NLS.bind(
															Messages.getString(
																	"WebGalleryView.unequal_proportions_msg"), //$NON-NLS-1$
															el.getAttribute("name")))) //$NON-NLS-1$
												return true;
											break lp;
										}
									}
								}
							}
						}
					}
					if (!errands.isEmpty() && !AcousticMessageDialog.openQuestion(getSite().getShell(),
							Messages.getString("WebGalleryView.unequal_proportions"), //$NON-NLS-1$
							NLS.bind(Messages.getString("WebGalleryView.required_aspect_ratio"), //$NON-NLS-1$
									new Object[] { el.getAttribute("name"), sample, Core.toStringList( //$NON-NLS-1$
											errands.toArray(new String[errands.size()]), ", ") }))) //$NON-NLS-1$
						return true;
				}
			}
			final IGalleryGenerator generator = (IGalleryGenerator) el.createExecutableExtension("class"); //$NON-NLS-1$
			if (generator instanceof AbstractGalleryGenerator) {
				((AbstractGalleryGenerator) generator).setConfigurationElement(el);
				((AbstractGalleryGenerator) generator).setSelectedStoryBoard(selectedStoryboard);
			}
			final File file = new File(outputFolder);
			if (!isFtp && file.exists() && file.listFiles().length > 0) {
				AcousticMessageDialog dialog = new AcousticMessageDialog(getSite().getShell(),
						Messages.getString("WebGalleryView.overwrite"), //$NON-NLS-1$
						null, NLS.bind(Messages.getString("WebGalleryView.output_folder_not_empty"), file), //$NON-NLS-1$
						MessageDialog.QUESTION, new String[] { Messages.getString("WebGalleryView.overwrite_button"), //$NON-NLS-1$
								Messages.getString("WebGalleryView.clear_folder"), //$NON-NLS-1$
								IDialogConstants.CANCEL_LABEL },
						0);
				switch (dialog.open()) {
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
								List<WebGalleryImpl> set = dbManager.obtainObjects(WebGalleryImpl.class, false, "name", //$NON-NLS-1$
										template.getName(), QueryField.EQUALS, "template", true, //$NON-NLS-1$
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
		Collection<PStoryboard> values = storyMap.values();
		return values.toArray(new PNode[values.size()]);
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
				updateActions(false);
			}

			@Override
			protected void drop(PInputEvent event, PNode pnode) {
				PWebExhibit pexhibit = (PWebExhibit) pnode.getParent();
				if (event.getCanvasPosition().getY() < 0)
					deletePresentationItem(pexhibit, false, false);
				else {
					Point2D newOffset = event.getPosition();
					Point2D parentOffset = ((PStoryboard) pexhibit.getParent()).getOffset();
					Point2D point = new Point2D.Double(newOffset.getX() + parentOffset.getX(), newOffset.getY());
					surface.parentToLocal(point);
					for (PStoryboard pstoryboard : storyMap.values()) {
						double y = point.getY() - pstoryboard.getOffset().getY();
						PBounds bounds = pstoryboard.getBoundsReference();
						if (bounds.getY() <= y && bounds.getY() + bounds.getHeight() >= y) {
							moveExhibit(pexhibit, newOffset);
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
		for (PStoryboard stb : storyMap.values()) {
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

	protected PNode findExhibit(Point2D position) {
		PStoryboard pstoryboard = null;
		RECT1.x = (int) position.getX();
		RECT1.y = (int) position.getY();
		for (PStoryboard w : storyMap.values())
			if (w.fullIntersects(RECT1)) {
				pstoryboard = w;
				break;
			}
		if (pstoryboard != null) {
			int rawIx = computeSequenceNumber(position.getX() - pstoryboard.getOffset().getX());
			String id = pstoryboard.getStoryboard().getExhibit(rawIx - 1);
			return exhibitMap.get(id);
		}
		return null;
	}

	protected void revealItem(PPresentationItem item) {
		if (item != null) {
			PBounds gfb = item.getGlobalFullBounds();
			double w = gfb.getWidth();
			double h = gfb.getHeight();
			gfb.setRect(gfb.getX() - w * 0.4d, gfb.getY() - h * 0.4d, 1.8d * w, 1.8d * h);
			canvas.getCamera().setViewBounds(gfb);
		}
	}

	private void revealExhibit(WebExhibit selectedExhibit) {
		revealItem(exhibitMap.get(selectedExhibit.getStringId()));
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getValue(String key, Object object) {
		if (object instanceof WebExhibit) {
			if (Constants.HV_TOTAL.equals(key)) {
				int total = 0;
				for (PStoryboard sb : storyMap.values())
					total += sb.getStoryboard().getExhibit().size();
				return String.valueOf(total);
			}
			if (Constants.HV_STORYBOARD.equals(key)) {
				String sbId = ((WebExhibit) object).getStoryboard_exhibit_parent();
				PStoryboard pstoryboard = storyMap.get(sbId);
				return pstoryboard.getStoryboard().getTitle();
			}
		}
		return null;
	}

	@Override
	protected String getStatusMessage() {
		WebGalleryImpl gal = getGallery();
		if (gal != null)
			return NLS.bind(storyMap.size() == 1 ? Messages.getString("WebGalleryView.n_exh_one_storyboard") //$NON-NLS-1$
					: Messages.getString("WebGalleryView.n_exh_m_storyboards"), //$NON-NLS-1$
					exhibitMap.size(), storyMap.size());
		return null;
	}

}
