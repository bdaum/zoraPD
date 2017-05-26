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
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
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
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;

import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.Rgb_type;
import com.bdaum.zoom.cat.model.Rgb_typeImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.exhibition.Exhibit;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitImpl;
import com.bdaum.zoom.cat.model.group.exhibition.Exhibition;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitionImpl;
import com.bdaum.zoom.cat.model.group.exhibition.Wall;
import com.bdaum.zoom.cat.model.group.exhibition.WallImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.ISpellCheckingService;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.css.internal.CssActivator;
import com.bdaum.zoom.net.core.ftp.FtpAccount;
import com.bdaum.zoom.net.core.job.TransferJob;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.dialogs.EditWallDialog;
import com.bdaum.zoom.ui.internal.dialogs.ExhibitLayoutDialog;
import com.bdaum.zoom.ui.internal.dialogs.ExhibitionEditDialog;
import com.bdaum.zoom.ui.internal.dialogs.SelectExhibitDialog;
import com.bdaum.zoom.ui.internal.operations.ExhibitionPropertiesOperation;
import com.bdaum.zoom.ui.internal.widgets.AbstractHandle;
import com.bdaum.zoom.ui.internal.widgets.GalleryPanEventHandler;
import com.bdaum.zoom.ui.internal.widgets.GreekedPSWTText;
import com.bdaum.zoom.ui.internal.widgets.PPanel;
import com.bdaum.zoom.ui.internal.widgets.PSWTAssetThumbnail;
import com.bdaum.zoom.ui.internal.widgets.PTextHandler;
import com.bdaum.zoom.ui.internal.widgets.TextEventHandler;
import com.bdaum.zoom.ui.internal.widgets.TextField;
import com.bdaum.zoom.vr.internal.ExhibitionJob;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PDimension;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.swt.PSWTCanvas;
import edu.umd.cs.piccolox.swt.PSWTHandle;
import edu.umd.cs.piccolox.swt.PSWTImage;
import edu.umd.cs.piccolox.swt.PSWTPath;
import edu.umd.cs.piccolox.swt.PSWTText;
import edu.umd.cs.piccolox.util.PLocator;

@SuppressWarnings("restriction")
public class ExhibitionView extends AbstractPresentationView {

	private static final Point DRAGTOLERANCE = new Point(0, 0);
	private static final java.awt.Rectangle RECT1 = new java.awt.Rectangle(0, 0, 1, 1);

	public class LayoutOperation extends AbstractOperation {

		private final ExhibitImpl backup;
		private final ExhibitImpl exhibit;
		private ExhibitImpl redo;

		public LayoutOperation(String title, ExhibitImpl backup, ExhibitImpl exhibit) {
			super(title);
			this.backup = backup;
			this.exhibit = exhibit;
		}

		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			storeSafelyAndUpdateIndex(null, exhibit, null);
			setInput(exhibition);
			return Status.OK_STATUS;
		}

		@Override
		public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			transferValues(redo, exhibit);
			return execute(monitor, info);
		}

		@Override
		public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			redo = cloneExhibit(exhibit);
			transferValues(backup, exhibit);
			return execute(monitor, info);
		}

		private void transferValues(ExhibitImpl from, ExhibitImpl to) {
			to.setWidth(from.getWidth());
			to.setHeight(from.getHeight());
			to.setMatWidth(from.getMatWidth());
			to.setMatColor(from.getMatColor());
			to.setFrameWidth(from.getFrameWidth());
			to.setFrameColor(from.getFrameColor());
			to.setHideLabel(from.getHideLabel());
		}
	}

	public class MarkSoldOperation extends AbstractOperation {

		private final PExhibit pexhibit;

		public MarkSoldOperation(PExhibit pexhibit) {
			super(pexhibit.exhibit.getSold() ? Messages.getString("ExhibitionView.mark_as_sold") //$NON-NLS-1$
					: Messages.getString("ExhibitionView.remove_sold_mark")); //$NON-NLS-1$
			this.pexhibit = pexhibit;
		}

		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			pexhibit.exhibit.setSold(!pexhibit.exhibit.getSold());
			pexhibit.updateSoldMark();
			storeSafelyAndUpdateIndex(null, pexhibit.exhibit, null);
			return Status.OK_STATUS;
		}

		@Override
		public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			return execute(monitor, info);
		}

		@Override
		public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			return execute(monitor, info);
		}
	}

	private static final int LEFTINDENT = 30;
	private static final int TOPINDENT = 10;
	private static final int V_WALLMARGINS = 15;
	private static final int gridTolerance = 8;

	public class DeleteWallOperation extends AbstractOperation {

		private PWall pWall;
		private Wall wall;
		private double y;
		private List<ExhibitImpl> deletedExhibits;
		private int index;

		public DeleteWallOperation(PWall pWall) {
			super(Messages.getString("ExhibitionView.delete_wall_undo")); //$NON-NLS-1$
			this.pWall = pWall;
			this.y = pWall.getOffset().getY();
			this.wall = pWall.getWall();
		}

		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			index = exhibition.getWall().indexOf(wall);
			if (index < 0)
				return Status.CANCEL_STATUS;
			exhibition.removeWall(wall);
			int size = wall.getExhibit().size();
			List<Object> toBeDeleted = new ArrayList<Object>(size + 1);
			List<String> assetIds = new ArrayList<String>(size);
			deletedExhibits = new ArrayList<ExhibitImpl>(size);
			for (ExhibitImpl obj : Core.getCore().getDbManager().obtainByIds(ExhibitImpl.class, wall.getExhibit())) {
				toBeDeleted.add(obj);
				deletedExhibits.add(obj);
				assetIds.add(obj.getAsset());
			}
			toBeDeleted.add(wall);
			storeSafelyAndUpdateIndex(toBeDeleted, exhibition, assetIds);
			Point2D offset = pWall.getOffset();
			double ydiff = pWall.getHeight() + 15;
			surface.removeChild(pWall);
			ListIterator<?> it = surface.getChildrenIterator();
			while (it.hasNext()) {
				Object obj = it.next();
				if (obj instanceof PWall) {
					PWall w = (PWall) obj;
					if (w.getOffset().getY() > offset.getY())
						w.offset(0, -ydiff);
				}
			}
			walls.remove(pWall);
			setPanAndZoomHandlers();
			return Status.OK_STATUS;
		}

		@Override
		public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			return execute(monitor, info);
		}

		@Override
		public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			ExhibitionImpl show = getExhibition();
			if (show != null) {
				List<Wall> allWalls = show.getWall();
				if (index < allWalls.size())
					allWalls.add(index, wall);
				else
					allWalls.add(wall);
				double ydiff = pWall.getHeight() + 15;
				ListIterator<?> it = surface.getChildrenIterator();
				while (it.hasNext()) {
					Object obj = it.next();
					if (obj instanceof PWall) {
						PWall w = (PWall) obj;
						if (w.getOffset().getY() + w.getHeight() >= y)
							w.offset(0, ydiff);
					}
				}
				surface.addChild(pWall);
				pWall.setOffset(0, y);
				setColor(canvas);
				List<Object> tobeStored = new ArrayList<Object>(deletedExhibits);
				List<String> assetIds = new ArrayList<String>(deletedExhibits.size());
				for (ExhibitImpl exhibit : deletedExhibits)
					assetIds.add(exhibit.getAsset());
				tobeStored.add(wall);
				tobeStored.add(show);
				storeSafelyAndUpdateIndex(null, tobeStored, assetIds);
			}
			return Status.OK_STATUS;
		}

	}

	public class CreateWallOperation extends AbstractOperation {

		private final Wall wall;
		private final int y;
		private PWall added;

		public CreateWallOperation(Wall wall, int y) {
			super(Messages.getString("ExhibitionView.create_wall_undo")); //$NON-NLS-1$
			this.wall = wall;
			this.y = y;
		}

		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			ExhibitionImpl show = getExhibition();
			if (show != null) {
				show.addWall(wall);
				added = addWall(wall, y, true, null);
				setColor(canvas);
				List<Object> toBeStored = new ArrayList<Object>(2);
				toBeStored.add(wall);
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
			ExhibitionImpl show = getExhibition();
			if (show != null) {
				show.removeWall(wall);
				List<Object> toBeDeleted = new ArrayList<Object>(wall.getExhibit().size() + 1);
				List<String> assetIds = new ArrayList<String>(wall.getExhibit().size());
				for (ExhibitImpl obj : Core.getCore().getDbManager().obtainByIds(ExhibitImpl.class,
						wall.getExhibit())) {
					toBeDeleted.add(obj);
					assetIds.add(obj.getAsset());
				}
				toBeDeleted.add(wall);
				storeSafelyAndUpdateIndex(toBeDeleted, exhibition, assetIds);
				Point2D offset = added.getOffset();
				double ydiff = added.getHeight() + 15;
				surface.removeChild(added);
				ListIterator<?> it = surface.getChildrenIterator();
				while (it.hasNext()) {
					Object obj = it.next();
					if (obj instanceof PWall) {
						PWall w = (PWall) obj;
						if (w.getOffset().getY() > offset.getY()) {
							w.offset(0, -ydiff);
						}
					}
				}
				walls.remove(wall);
				setPanAndZoomHandlers();
			}
			return Status.OK_STATUS;
		}
	}

	public class EditWallOperation extends AbstractOperation {

		private final WallImpl wall;
		private final WallImpl backup;
		private WallImpl redo;
		private final PWall pwall;

		public EditWallOperation(WallImpl wall, WallImpl backup, PWall pwall) {
			super(Messages.getString("ExhibitionView.edit_wall_undo")); //$NON-NLS-1$
			this.wall = wall;
			this.backup = backup;
			this.pwall = pwall;
		}

		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			Core.getCore().getDbManager().safeTransaction(null, wall);
			pwall.setTitle(wall.getLocation());
			pwall.resize();
			setColor(canvas);
			return Status.OK_STATUS;
		}

		@Override
		public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			wall.setLocation(redo.getLocation());
			wall.setX(redo.getX());
			wall.setY(redo.getY());
			wall.setWidth(redo.getWidth());
			wall.setHeight(redo.getHeight());
			wall.setColor(redo.getColor());
			return execute(monitor, info);
		}

		@Override
		public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			redo = new WallImpl(wall.getLocation(), wall.getX(), wall.getY(), wall.getWidth(), wall.getHeight(),
					wall.getGX(), wall.getGX(), wall.getGAngle(), wall.getColor());
			wall.setLocation(backup.getLocation());
			wall.setX(backup.getX());
			wall.setY(backup.getY());
			wall.setWidth(backup.getWidth());
			wall.setHeight(backup.getHeight());
			wall.setColor(backup.getColor());
			return execute(monitor, info);
		}
	}

	public static class EditWallTextOperation extends AbstractOperation {

		public static final int CAPTION = 0;
		public static final int DESCRIPTION = 1;
		public static final int CREDITS = 2;
		public static final int DATE = 3;
		private final Wall wall;
		private final String text;
		private String oldtext;
		private final TextField textField;

		public EditWallTextOperation(Wall wall, String text, TextField textField) {
			super(Messages.getString("ExhibitionView.edit_wall_loc_undo")); //$NON-NLS-1$
			this.wall = wall;
			this.text = text;
			this.textField = textField;
		}

		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			oldtext = wall.getLocation();
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
			wall.setLocation(t);
			Core.getCore().getDbManager().safeTransaction(null, wall);
		}
	}

	public static class EditInfoTextOperation extends AbstractOperation {

		private final Exhibition exhibition;
		private final String text;
		private String oldtext;
		private final TextField textField;

		public EditInfoTextOperation(Exhibition exhibition, String text, TextField textField) {
			super(Messages.getString("ExhibitionView.edit_info_text")); //$NON-NLS-1$
			this.exhibition = exhibition;
			this.text = text;
			this.textField = textField;
		}

		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			oldtext = exhibition.getInfo();
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
			exhibition.setInfo(t);
			Core.getCore().getDbManager().safeTransaction(null, exhibition);
		}
	}

	public class EditTextOperation extends AbstractOperation {

		public static final int CAPTION = 0;
		public static final int DESCRIPTION = 1;
		public static final int CREDITS = 2;
		public static final int DATE = 3;
		private final ExhibitImpl exhibit;
		private final String text;
		private String oldtext;
		private final TextField textField;
		private final int type;

		public EditTextOperation(ExhibitImpl exhibit, String text, TextField textField, int type) {
			super(Messages.getString("ExhibitionView.edit_exh_text_undo")); //$NON-NLS-1$
			this.exhibit = exhibit;
			this.text = text;
			this.textField = textField;
			this.type = type;
		}

		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			switch (type) {
			case CAPTION:
				oldtext = exhibit.getTitle();
				break;
			case DESCRIPTION:
				oldtext = exhibit.getDescription();
				break;
			case CREDITS:
				oldtext = exhibit.getCredits();
				break;
			case DATE:
				oldtext = exhibit.getDate();
				break;
			}
			doSetText(text);
			return Status.OK_STATUS;
		}

		private void doSetText(String t) {
			boolean indexed = false;
			boolean changed = false;
			switch (type) {
			case CAPTION:
				changed = exhibit.getTitle() == null || !exhibit.getTitle().equals(t);
				indexed = true;
				exhibit.setTitle(t);
				break;
			case DESCRIPTION:
				changed = exhibit.getDescription() == null || !exhibit.getDescription().equals(t);
				indexed = true;
				exhibit.setDescription(t);
				break;
			case CREDITS:
				changed = exhibit.getCredits() == null || !exhibit.getCredits().equals(t);
				indexed = true;
				exhibit.setCredits(t);
				break;
			case DATE:
				exhibit.setDate(t);
				break;
			}
			if (changed)
				storeSafelyAndUpdateIndex(null, exhibit, indexed ? exhibit.getAsset() : null);
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

	}

	public class EditExhibitOperation extends AbstractOperation {

		private final PExhibit pexhibit;
		private final ExhibitImpl exhibit;
		private ExhibitImpl redo;
		private ExhibitImpl backup;
		private boolean indexChange;

		public EditExhibitOperation(PExhibit pexhibit, ExhibitImpl backup, ExhibitImpl exhibit) {
			super(Messages.getString("ExhibitionView.change_exh_size_pos_undo")); //$NON-NLS-1$
			this.pexhibit = pexhibit;
			this.backup = backup;
			this.exhibit = exhibit;
			indexChange = (backup.getTitle() == null || !backup.getTitle().equals(exhibit.getTitle())
					|| backup.getDescription() == null || !backup.getDescription().equals(exhibit.getDescription())
					|| backup.getCredits() == null || !backup.getCredits().equals(exhibit.getCredits()));
		}

		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			pexhibit.setImageWidth(exhibit.getWidth());
			pexhibit.setImageHeight(exhibit.getHeight());
			pexhibit.invalidatePaint();
			pexhibit.relocateAllHandles();
			String id = exhibit.getAsset();
			storeSafelyAndUpdateIndex(null, exhibit, indexChange ? id : null);
			AssetImpl asset = Core.getCore().getDbManager().obtainAsset(id);
			if (asset != null)
				pexhibit.computeResolutionWarning(asset);
			return Status.OK_STATUS;
		}

		@Override
		public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			exhibit.setTitle(redo.getTitle());
			exhibit.setDescription(redo.getDescription());
			exhibit.setCredits(redo.getCredits());
			exhibit.setDate(redo.getDate());
			exhibit.setX(redo.getX());
			exhibit.setY(redo.getY());
			exhibit.setWidth(redo.getWidth());
			exhibit.setHeight(redo.getHeight());
			exhibit.setAsset(redo.getAsset());
			pexhibit.rescale();
			return execute(monitor, info);
		}

		@Override
		public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			redo = cloneExhibit(exhibit);
			exhibit.setTitle(backup.getTitle());
			exhibit.setDescription(backup.getDescription());
			exhibit.setCredits(backup.getCredits());
			exhibit.setDate(backup.getDate());
			exhibit.setX(backup.getX());
			exhibit.setY(backup.getY());
			exhibit.setWidth(backup.getWidth());
			exhibit.setHeight(backup.getHeight());
			exhibit.setAsset(backup.getAsset());
			pexhibit.rescale();
			return execute(monitor, info);
		}

	}

	public class MoveExhibitOperation extends AbstractOperation {

		private final PExhibit moved;
		private final Point2D oldOffset;
		private final Point2D newOffset;
		private PWall oldWall;
		private final PWall target;

		public MoveExhibitOperation(PExhibit moved, Point2D oldOffset, PWall target, Point2D newOffset) {
			super(Messages.getString("ExhibitionView.move_exh_undo")); //$NON-NLS-1$
			this.moved = moved;
			this.oldOffset = oldOffset;
			this.target = target;
			this.newOffset = newOffset;
		}

		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			oldWall = doMove(newOffset, target, false);
			return (oldWall == null) ? Status.CANCEL_STATUS : Status.OK_STATUS;
		}

		private PWall doMove(Point2D to, PWall toWall, boolean undo) {
			PWall fromPWall = doRemoveExhibit(moved);
			if (fromPWall == null)
				return null;
			Point2D sourceOffset = fromPWall.getOffset();
			Point2D targetOffset = toWall.getOffset();
			double xdif = targetOffset.getX() - sourceOffset.getX();
			double ydif = targetOffset.getY() - sourceOffset.getY();
			double x = to.getX();
			double y = to.getY();
			int index = 0;
			ListIterator<?> it = toWall.getChildrenIterator();
			while (it.hasNext()) {
				Object next = it.next();
				if (next instanceof PExhibit) {
					PExhibit pExhibit = (PExhibit) next;
					Point2D offset = pExhibit.getOffset();
					if (offset.getX() < x)
						index++;
				}
			}
			int globalIndex = index;
			ExhibitImpl exhibit = moved.exhibit;
			Wall targetWall = toWall.getWall();
			for (Wall w : exhibition.getWall()) {
				if (w == targetWall)
					break;
				globalIndex += w.getExhibit().size();
			}
			if (++globalIndex >= exhibits.size())
				exhibits.add(exhibit);
			else
				exhibits.add(globalIndex, exhibit);
			if (++index >= targetWall.getExhibit().size())
				targetWall.addExhibit(exhibit.getStringId());
			else
				targetWall.getExhibit().add(index, exhibit.getStringId());
			toWall.addChild(moved);
			int newX = (int) (undo ? x : (x - xdif));
			int newY = (int) (undo ? y : (y - ydif));
			// Exhibition exhibition = targetWall.getExhibition_wall_parent();
			if (!undo && exhibition.getShowGrid() && exhibition.getSnapToGrid()) {
				org.eclipse.swt.graphics.Point pnt = UiUtilities.snapToGrid(newX, newY, exhibit.getWidth(),
						exhibit.getHeight(), targetWall.getHeight(), exhibition.getGridSize(), gridTolerance);
				newX = pnt.x;
				newY = pnt.y;
			}
			moved.setOffset(newX, newY);
			exhibit.setY((targetWall.getHeight() - newY));
			exhibit.setX((newX));
			List<Object> tobeStored = new ArrayList<Object>(3);
			tobeStored.add(exhibit);
			tobeStored.add(targetWall);
			Wall fromWall = fromPWall.getWall();
			if (fromWall != targetWall)
				tobeStored.add(fromWall);
			storeSafelyAndUpdateIndex(null, tobeStored, null);
			updateActions();
			return fromPWall;
		}

		@Override
		public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			return execute(monitor, info);
		}

		@Override
		public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			return (doMove(oldOffset, oldWall, true) == null) ? Status.CANCEL_STATUS : Status.OK_STATUS;
		}
	}

	public class DeleteExhibitOperation extends AbstractOperation {

		private PExhibit deleted;
		private PWall wallPanel;
		private Point2D offset;
		private ExhibitImpl exhibit;

		public DeleteExhibitOperation(PExhibit deleted, Point2D oldOffset) {
			super(Messages.getString("ExhibitionView.delete_exh_undo")); //$NON-NLS-1$
			this.deleted = deleted;
			offset = oldOffset;
		}

		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			exhibit = deleted.exhibit;
			wallPanel = doRemoveExhibit(deleted);
			if (wallPanel == null)
				return Status.CANCEL_STATUS;
			storeSafelyAndUpdateIndex(exhibit, wallPanel.getWall(), exhibit.getAsset());
			updateActions();
			return Status.OK_STATUS;
		}

		@Override
		public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			return execute(monitor, info);
		}

		@Override
		public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			WallImpl wall = (WallImpl) wallPanel.getWall();
			double x = offset.getX() - wallPanel.getOffset().getX();
			double pos = 0;
			int index = 0;
			// List<PExhibit> trail = new ArrayList<PExhibit>(exhibits.size());
			ListIterator<?> it = wallPanel.getChildrenIterator();
			while (it.hasNext()) {
				Object next = it.next();
				if (next instanceof PExhibit) {
					PExhibit pExhibit = (PExhibit) next;
					PBounds eBounds = pExhibit.getFullBoundsReference();
					double p = eBounds.getX();
					if (p < x) {
						// trail.add(pExhibit);
						// } else {
						p += eBounds.getWidth();
						if (p > pos)
							pos = p;
						index++;
					}
				}
			}
			// double oldPos = pos;
			int globalIndex = index;
			for (Wall w : exhibition.getWall()) {
				if (w == wall)
					break;
				globalIndex += w.getExhibit().size();
			}
			if (globalIndex >= exhibits.size())
				exhibits.add(exhibit);
			else
				exhibits.add(globalIndex, exhibit);
			if (index >= wall.getExhibit().size())
				wall.addExhibit(exhibit.getStringId());
			else
				wall.getExhibit().add(index, exhibit.getStringId());
			// exhibit.setWall_exhibit_parent(wall.getStringId());
			// pos += horizontalGap;
			// deleted = makeExhibit(wallPanel, (int) pos, exhibit);
			// PBounds eBounds = deleted.getFullBoundsReference();
			// pos = eBounds.getX() + eBounds.getWidth();
			// double iWidth = (pos - oldPos);
			// for (PExhibit element : trail)
			// element.offset(iWidth, 0);
			wallPanel.addChild(deleted);
			deleted.setOffset(offset);
			List<Object> toBeStored = new ArrayList<Object>(2);
			toBeStored.add(exhibit);
			toBeStored.add(wall);
			storeSafelyAndUpdateIndex(null, toBeStored, exhibit.getAsset());
			updateActions();
			return Status.OK_STATUS;
		}
	}

	public class DropAssetOperation extends AbstractOperation {

		private final AssetSelection selection;
		private final Point2D position;
		private List<PExhibit> added;
		private PWall wallPanel;
		private final boolean replace;
		private DeleteExhibitOperation deleteOp;

		public DropAssetOperation(AssetSelection selection, Point2D position, boolean replace) {
			super(Messages.getString("ExhibitionView.drop_images_undo")); //$NON-NLS-1$
			this.selection = selection;
			this.position = position;
			this.replace = replace;
			added = new ArrayList<PExhibit>(selection.size());
			RECT1.x = (int) position.getX();
			RECT1.y = (int) position.getY();
			for (PWall w : walls)
				if (w.fullIntersects(RECT1)) {
					wallPanel = w;
					break;
				}
		}

		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			if (wallPanel != null) {
				ExhibitionImpl show = null;
				if (!selection.isEmpty())
					show = getExhibition();
				if (show == null)
					return Status.OK_STATUS;
				WallImpl wall = (WallImpl) wallPanel.getWall();
				double x = position.getX() - wallPanel.getOffset().getX();
				double pos = 0;
				int index = 0;
				PExhibit cand = null;
				List<PExhibit> trail = new ArrayList<PExhibit>(exhibits.size());
				ListIterator<?> it = wallPanel.getChildrenIterator();
				while (it.hasNext()) {
					Object next = it.next();
					if (next instanceof PExhibit) {
						PExhibit pExhibit = (PExhibit) next;
						PBounds eBounds = pExhibit.getFullBoundsReference();
						double p = eBounds.getX();
						if (p >= x)
							trail.add(pExhibit);
						else {
							p += eBounds.getWidth();
							if (replace && p >= x)
								cand = pExhibit;
							if (p > pos)
								pos = p;
							index++;
						}
					}
				}
				double oldPos = pos;
				int globalIndex = index;
				for (Wall w1 : exhibition.getWall()) {
					if (w1 == wall)
						break;
					globalIndex += w1.getExhibit().size();
				}
				Exhibit sibling = null;
				if (cand != null)
					sibling = cand.exhibit;
				if (globalIndex > 0)
					sibling = exhibits.get(globalIndex - 1);
				else if (globalIndex < exhibits.size() - 1)
					sibling = exhibits.get(globalIndex + 1);
				double siblingSize = sibling == null ? Double.NaN
						: Math.sqrt(
								sibling.getWidth() * sibling.getWidth() + sibling.getHeight() * sibling.getHeight());
				double resfac = 25.4d / 300d;
				List<Object> toBeStored = new ArrayList<Object>(selection.size() + 1);
				List<String> assetIds = new ArrayList<String>(selection.size());
				int defaultViewingHeight = show.getDefaultViewingHeight();
				int variance = show.getVariance();
				String defaultDescription = exhibition.getDefaultDescription();
				for (Asset asset : selection) {
					if (!accepts(asset))
						continue;
					++index;
					++globalIndex;
					Date dateCreated = asset.getDateTimeOriginal();
					if (dateCreated == null)
						dateCreated = asset.getDateTime();
					double fac = Double.isNaN(siblingSize) ? resfac
							: siblingSize / Math.max(320, Math
									.sqrt(asset.getHeight() * asset.getHeight() + asset.getWidth() * asset.getWidth()));
					int h = (int) (asset.getHeight() * fac);
					int w = (int) (asset.getWidth() * fac);
					if ((asset.getRotation() + Utilities.orientationDegrees(asset)) % 180 == 90) {
						int u = h;
						h = w;
						w = u;
					}
					double r = Math.random();
					int v = (int) (r * variance + 0.5d) - variance / 2;
					int ys = defaultViewingHeight + v + h / 2;
					ys = Math.max(0, Math.min(ys, wall.getHeight()));
					int xs = (int) (pos + 0.5d);
					if (variance > 0 && exhibition.getShowGrid() && exhibition.getSnapToGrid()) {
						org.eclipse.swt.graphics.Point pnt = UiUtilities.snapToGrid(xs, ys, w, h, wall.getHeight(),
								exhibition.getGridSize(), exhibition.getGridSize());
						xs = pnt.x;
						ys = pnt.y;
					}
					if (cand != null) {
						xs = cand.exhibit.getX();
						ys = cand.exhibit.getY();
					}
					String assetId = asset.getStringId();
					ExhibitImpl exhibit = new ExhibitImpl(UiUtilities.createSlideTitle(asset),
							defaultDescription == null ? Messages.getString("ExhibitionView.inkjet_print") //$NON-NLS-1$
									: defaultDescription,
							Core.toStringList(asset.getArtist(), " "), //$NON-NLS-1$
							(dateCreated == null) ? "" //$NON-NLS-1$
									: EDF.format(dateCreated),
							xs, ys, w, h, null, null, null, null, false, null, null, null, null, assetId);
					if (globalIndex >= exhibits.size())
						exhibits.add(exhibit);
					else
						exhibits.add(globalIndex, exhibit);
					if (index >= wall.getExhibit().size())
						wall.addExhibit(exhibit.getStringId());
					else
						wall.getExhibit().add(index, exhibit.getStringId());
					exhibit.setWall_exhibit_parent(wall.getStringId());
					pos += horizontalGap;
					if (cand != null) {
						pos = cand.getFullBoundsReference().getX();
						deleteOp = new DeleteExhibitOperation(cand, cand.getOffset());
						deleteOp.execute(monitor, info);
						cand = null;
					}
					PExhibit pExhibit = makeExhibit(wallPanel, (int) pos, exhibit);
					PBounds eBounds = pExhibit.getFullBoundsReference();
					pos = eBounds.getX() + eBounds.getWidth();
					toBeStored.add(exhibit);
					assetIds.add(assetId);
					added.add(pExhibit);
				}
				setArtists();
				double iWidth = (pos - oldPos);
				for (PExhibit pExhibit : trail)
					pExhibit.offset(iWidth, 0);
				toBeStored.add(wall);
				storeSafelyAndUpdateIndex(null, toBeStored, assetIds);
			}
			updateActions();
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
			for (PExhibit exhibit : added) {
				doRemoveExhibit(exhibit);
				toBeDeleted.add(exhibit.exhibit);
				assetIds.add(exhibit.exhibit.getAsset());
			}
			if (deleteOp != null)
				deleteOp.undo(monitor, info);
			storeSafelyAndUpdateIndex(toBeDeleted, wallPanel.getWall(), assetIds);
			updateActions();
			return Status.OK_STATUS;
		}
	}

	private static final int labelWidth = 140;
	private static final int labelHeight = 70;
	private static final int labelMargins = 10;
	private static final int soldMarkDiameter = labelMargins;
	private static final int labelIndent = 10;
	private static final int handleSize = 10;
	protected static final double horizontalGap = 80;
	protected static final Color GridColor = new Color(224, 224, 224);
	protected static final Color DoorColor = new Color(0, 255, 255);
	protected static final Color GuideColor = new Color(64, 192, 192);

	private static class GridLine extends PSWTPath {
		private static final long serialVersionUID = 8838795470028525193L;
		private static final double MINRENDEREDDISTANCE = 4d;
		private final int lineDist;

		public GridLine(float[] xp, float[] yp, int lineDist) {
			this.lineDist = lineDist;
			setPathToPolyline(xp, yp);
			setStrokeColor(GridColor);
		}

		@Override
		protected void paint(PPaintContext ppc) {
			final double renderedLineDist = lineDist * ppc.getScale();
			if (renderedLineDist > MINRENDEREDDISTANCE)
				super.paint(ppc);
		}
	}

	private static class GuideLine extends PSWTPath {

		private static final long serialVersionUID = 5682799369373994468L;

		public GuideLine(float[] xp, float[] yp) {
			setPathToPolyline(xp, yp);
			setStrokeColor(GuideColor);
		}
	}

	private class PWall extends PPanel {

		private static final long serialVersionUID = 2123557338158980177L;
		private TextField info;
		private PSWTPath pInfoPlate;
		private PSWTPath pDoor;
		private int height;
		private TextField artistsField;

		public PWall(Wall wall) {
			super(wall, wall.getWidth(), wall.getHeight());
			height = wall.getHeight();
			// Grid
			addGrid(this);
			// Viewing height guide
			addGuide(this);
			// Door and info plate
			int sx = exhibition.getStartX();
			int sy = exhibition.getStartY();
			double angle = wall.getGAngle();
			double cos = Math.cos(Math.toRadians(angle));
			double sin = Math.sin(Math.toRadians(angle));
			int wallX1 = wall.getGX();
			int wallY1 = wall.getGY();
			int wallW = wall.getWidth();
			double wallX2 = wallX1 + cos * wallW;
			double wallY2 = wallY1 + sin * wallW;
			boolean door = false;
			if (sx >= Math.min(wallX1, wallX2) - 50 && sx <= Math.max(wallX1, wallX2) + 50
					&& sy >= Math.min(wallY1, wallY2) - 50 && sy <= Math.max(wallY1, wallY2) + 50) {
				if (Math.abs(cos) > 0.01d)
					door = Math.abs(wallY1 - sy + (sx - wallX1) * sin / cos) < 50;
				else
					door = true;
			}
			if (door) {
				float doorX1 = (float) (sin * (sy - wallY1) + cos * (sx - wallX1)) - DOORWIDTH / 2;
				float doorX2 = doorX1 + DOORWIDTH;
				int infoPlatePosition = exhibition.getInfoPlatePosition();
				float xinfoShift = 0;
				float infoX1 = doorX2 + INFOGAP;
				if (infoPlatePosition == INFOLEFT) {
					xinfoShift = INFOWIDTH + INFOGAP;
					infoX1 = doorX1;
				}
				pDoor = new PSWTPath();
				pDoor.setPathToPolyline(new float[] { 0, 0, DOORWIDTH, DOORWIDTH },
						new float[] { DOORHEIGHT, 0, 0, DOORHEIGHT });
				pDoor.setOffset(doorX1 + xinfoShift, height - DOORHEIGHT);
				pDoor.setStrokeColor(DoorColor);
				addChild(0, pDoor);
				if (infoPlatePosition != INFONONE) {
					pInfoPlate = new PSWTPath();
					pInfoPlate.setPathToPolyline(new float[] { 0, 0, INFOWIDTH, INFOWIDTH, 0 },
							new float[] { INFOHEIGHT, 0, 0, INFOHEIGHT, INFOHEIGHT });
					pInfoPlate.setStrokeColor(DoorColor);
					pInfoPlate.setPickable(true);
					addChild(pInfoPlate);
					pInfoPlate.setOffset(infoX1, height - DOORHEIGHT);
					String fontFamily = "Arial"; //$NON-NLS-1$
					int fontsize = 22;
					// caption
					double ypos = INFOMARGINS;
					Color penColor = new Color(32, 32, 32);
					Color selectedPenColor = new Color(92, 92, 92);
					Color background = (Color) pInfoPlate.getPaint();
					GreekedPSWTText caption = new GreekedPSWTText(exhibition.getName(),
							new Font(fontFamily, Font.BOLD, fontsize + 6));
					caption.setPenColor(penColor);
					caption.setBackgroundColor(background);
					caption.setOffset(INFOMARGINS / 2, ypos);
					caption.setTransparent(true);
					caption.setTextWidth(INFOWIDTH - INFOMARGINS);
					caption.setAlignment(SWT.CENTER);
					pInfoPlate.addChild(caption);
					ypos += caption.getFullBoundsReference().getHeight() + INFOMARGINS;
					if (!exhibition.getHideCredits()) {
						artistsField = createTextLine(pInfoPlate, getArtists(), INFOWIDTH - INFOMARGINS,
								INFOMARGINS / 2, ypos, penColor, background, selectedPenColor, fontFamily, Font.PLAIN,
								fontsize, ISpellCheckingService.NOSPELLING,
								SWT.MULTI | SWT.WRAP | SWT.READ_ONLY | SWT.CENTER);
						ypos += artistsField.getFullBoundsReference().getHeight() + INFOMARGINS;
					}
					// info
					info = createTextLine(pInfoPlate, exhibition.getInfo(), INFOWIDTH - INFOMARGINS, INFOMARGINS / 2,
							ypos, penColor, background, selectedPenColor, fontFamily, Font.PLAIN, fontsize,
							ISpellCheckingService.DESCRIPTIONOPTIONS, SWT.MULTI | SWT.WRAP | SWT.CENTER);
					ypos += info.getFullBoundsReference().getHeight();
				}
			}
			// buttons
			deleteButton = createButton(canvas, Icons.largeDelete, Messages.getString("ExhibitionView.delete_wall")); //$NON-NLS-1$
			propButton = createButton(canvas, Icons.largeProperties,
					Messages.getString("ExhibitionView.wall_properties")); //$NON-NLS-1$
			Rectangle bounds = positionButtons(height);
			// Location
			createTitle(canvas, wall.getLocation(), 11 * bounds.width / 4, height - bounds.height,
					new Color(128, 128, 128), (Color) getPaint(), 18);
		}

		public void setArtists() {
			if (artistsField != null)
				artistsField.setText(getArtists());
		}

		public void resize() {
			int h = ((Wall) data).getHeight();
			double diff = h - height;
			height = h;
			setPathToRectangle(0, 0, (((Wall) data).getWidth()), height);
			// Grid
			addGrid(this);
			// Viewing height guide
			addGuide(this);
			if (pDoor != null)
				pDoor.setOffset(pDoor.getOffset().getX(), height - DOORHEIGHT);
			if (pInfoPlate != null)
				pInfoPlate.setOffset(pInfoPlate.getOffset().getX(), height - DOORHEIGHT);
			positionButtons(height);
			if (diff != 0) {
				positionImages(0, diff);
				positionSubsequentWalls(diff);
			}
		}

		private void positionSubsequentWalls(double ydiff) {
			ListIterator<?> it = surface.getChildrenIterator();
			boolean passed = false;
			while (it.hasNext()) {
				Object w = it.next();
				if (w == this)
					passed = true;
				else if (passed)
					((PWall) w).offset(0, ydiff);
			}
		}

		private void positionImages(double xdiff, double ydiff) {
			ListIterator<?> it = getChildrenIterator();
			while (it.hasNext()) {
				Object obj = it.next();
				if (obj instanceof PExhibit) {
					PExhibit pexhibit = (PExhibit) obj;
					pexhibit.offset(xdiff, ydiff);
				}
			}

		}

		private Rectangle positionButtons(int h) {
			Rectangle bounds = deleteButton.getImage().getBounds();
			int margins = bounds.width / 4;
			deleteButton.setOffset(margins, h - bounds.height - margins);
			bounds = propButton.getImage().getBounds();
			margins = bounds.width / 4;
			propButton.setOffset(bounds.width + 2 * margins, h - bounds.height - margins);
			return bounds;
		}

		public Wall getWall() {
			return (Wall) data;
		}

		public void processTextEvent(TextField focus) {
			if (focus == info)
				performOperation(new EditInfoTextOperation(exhibition, focus.getText(), focus));
			else if (focus == title)
				performOperation(new EditWallTextOperation(getWall(), focus.getText(), focus));
		}

		@Override
		protected void deletePressed() {
			setPickedNode(PWall.this);
			deleteWallAction.run();
		}

		@Override
		protected void propertiesPressed() {
			textEventHandler.commit();
			editWallLegend(PWall.this);
		}

		/**
		 * @return the pInfoPlate
		 */
		public PSWTPath getpInfoPlate() {
			return pInfoPlate;
		}
	}

	class PExhibit extends PNode implements PTextHandler, IPresentationItem {

		private static final long serialVersionUID = -8619376689449783925L;

		private final class ExhibitHandle extends AbstractHandle {
			private static final long serialVersionUID = -2915474593495856476L;

			class ExhibitLocator extends PLocator {
				private static final long serialVersionUID = 1228742743568586994L;

				@Override
				public double locateX() {
					return ExhibitHandle.this.locateX() + size / 4d;
				}

				@Override
				public double locateY() {
					return ExhibitHandle.this.locateY() - size / 4d;
				}
			}

			private double shiftX;
			private double shiftY;
			private int size;
			private double oldScale;
			private ExhibitImpl backup;

			private ExhibitHandle(int size) {
				super(SWT.CURSOR_SIZENESW, Messages.getString("ExhibitionView.size_tooltip")); //$NON-NLS-1$
				setLocator(new ExhibitLocator());
				this.size = size;
				setPaint(new Color(0, 224, 0));
				setStrokeColor(null);
			}

			@Override
			public void startHandleDrag(Point2D localPoint, PInputEvent event) {
				backup = cloneExhibit(exhibit);
				shiftX = 0;
				shiftY = 0;
				oldScale = pImage.getScale();
				PExhibit.this.moveToFront();
			}

			@Override
			public void dragHandle(PDimension localDimension, PInputEvent event) {
				double width = localDimension.getWidth();
				double height = localDimension.getHeight();
				shiftX += width;
				shiftY -= height;
				int w = (int) (backup.getWidth() + shiftX);
				int h = (int) (backup.getHeight() + shiftY);
				if (w >= 0 && h >= 0) {
					Integer i = exhibit.getMatWidth();
					int matWidth = (i == null) ? exhibition.getMatWidth() : i;
					i = exhibit.getFrameWidth();
					int frameWidth = (i == null) ? exhibition.getFrameWidth() : i;
					double f = (((double) w / backup.getWidth()) + ((double) h / backup.getHeight())) / 2;
					int iWidth = (int) (backup.getWidth() * f);
					exhibit.setWidth(iWidth);
					int iHeight = (int) (backup.getHeight() * f);
					exhibit.setHeight(iHeight);
					pImage.setScale(oldScale * f);
					if (pFrame != null)
						pFrame.setPathToRectangle(0f, 0f, iWidth + 2 * (frameWidth + matWidth),
								iHeight + 2 * (frameWidth + matWidth));
					if (pMat != null)
						pMat.setPathToRectangle(frameWidth, frameWidth, iWidth + 2 * matWidth, iHeight + 2 * matWidth);
					setLabelOffset(pImage.getFullBoundsReference(), exhibit, exhibition);
				}
				invalidatePaint();
				relocateHandle();
				createCue(exhibit);
			}

			private void createCue(ExhibitImpl exhibit) {
				Display display = canvas.getDisplay();
				Image image = new Image(display, 48, 48);
				Rectangle bounds = image.getBounds();
				GC gc = new GC(image);
				gc.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
				gc.fillRectangle(bounds);
				gc.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
				gc.drawRectangle(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1);
				dimFormat.setMaximumFractionDigits(1);
				dimFormat.setMinimumFractionDigits(1);
				String text = new StringBuilder().append(dimFormat.format(exhibit.getWidth() / 10d)).append(" x ") //$NON-NLS-1$
						.append(dimFormat.format(exhibit.getHeight() / 10d)).append(" cm").toString(); //$NON-NLS-1$
				TextLayout layout = new TextLayout(display);
				layout.setAlignment(SWT.CENTER);
				layout.setWidth(bounds.width - 6);
				layout.setText(text);
				Rectangle tbounds = layout.getBounds();
				layout.draw(gc, (bounds.width - tbounds.width) / 2, (bounds.height - tbounds.height) / 2);
				layout.dispose();
				gc.dispose();
				Cursor cue = new Cursor(display, image.getImageData(), 0, 0);
				image.dispose();
				setCue(cue);
			}

			@Override
			public void endHandleDrag(Point2D localPoint, PInputEvent event) {
				setCue(null);
				performOperation(new EditExhibitOperation(PExhibit.this, backup, exhibit));
			}

			public double locateX() {
				return pImage.getFullBoundsReference().getWidth() + pImage.getOffset().getX();
			}

			public double locateY() {
				return pImage.getOffset().getY();
			}
		}

		private ExhibitImpl exhibit;
		private PSWTImage pImage;
		private PSWTHandle midPoint;
		private TextField caption;
		private double imageWidth;
		private double imageHeight;
		private PSWTPath imageLabel;
		private TextField description;
		private TextField credits;
		private TextField creationDate;
		private PSWTText resolutionWarning;
		private PPresentationPanel pFrame;
		private PPresentationPanel pMat;
		private final PSWTCanvas canvas;
		private final PWall pwall;
		private PSWTPath soldMark;

		public PExhibit(PSWTCanvas canvas, PWall pwall, final ExhibitImpl exhibit, int pos, int y) {
			this.canvas = canvas;
			this.pwall = pwall;
			this.exhibit = exhibit;
			setPickable(false);
			createContent(canvas, pwall, exhibit, pos, y);
		}

		private void createContent(PSWTCanvas canvas, PWall pwall, final ExhibitImpl exhibit, double pos, double y) {
			String fontFamily = "Arial"; //$NON-NLS-1$
			int fontsize = 11;
			if (exhibition.getLabelFontFamily() != null && exhibition.getLabelFontFamily().length() > 0) {
				fontFamily = exhibition.getLabelFontFamily();
				fontsize = Math.max(5, exhibition.getLabelFontSize());
			}
			imageWidth = exhibit.getWidth();
			imageHeight = exhibit.getHeight();
			Integer i = exhibit.getMatWidth();
			int matWidth = (i == null) ? exhibition.getMatWidth() : i;
			Rgb_type matColor = exhibit.getMatColor();
			if (matColor == null)
				matColor = exhibition.getMatColor();
			if (matColor == null)
				matColor = new Rgb_typeImpl(255, 255, 252);
			i = exhibit.getFrameWidth();
			int frameWidth = (i == null) ? exhibition.getFrameWidth() : i;
			Rgb_type frameColor = exhibit.getFrameColor();
			if (frameColor == null)
				frameColor = exhibition.getFrameColor();
			if (frameColor == null)
				frameColor = new Rgb_typeImpl(8, 8, 8);
			PBasicInputEventHandler inputEventHandler = new PBasicInputEventHandler() {
				@Override
				public void mouseReleased(PInputEvent event) {
					setPickedNode(event.getPickedNode());
				}
			};
			// Frame and Mat
			if (frameWidth != 0) {
				pFrame = new PPresentationPanel(0, 0, (float) (imageWidth + 2 * (frameWidth + matWidth)),
						(float) (imageHeight + 2 * (frameWidth + matWidth)),
						new Color(frameColor.getR(), frameColor.getG(), frameColor.getB()), null, inputEventHandler);
				addChild(pFrame);
			}
			if (matWidth != 0) {
				pMat = new PPresentationPanel(frameWidth, frameWidth, (float) (imageWidth + 2 * matWidth),
						(float) (imageHeight + 2 * matWidth),
						new Color(matColor.getR(), matColor.getG(), matColor.getB()), null, inputEventHandler);
				addChild(pMat);
			}
			// Image
			AssetImpl asset = Core.getCore().getDbManager().obtainAsset(exhibit.getAsset());
			pImage = new PSWTAssetThumbnail(canvas, ExhibitionView.this, asset);
			PBounds bounds = pImage.getBoundsReference();
			double width = bounds.getWidth();
			double scale = imageWidth / width;
			addChild(pImage);
			pImage.setPickable(true);
			setBounds(0, 0, imageWidth, imageHeight);
			pImage.scale(scale);
			pImage.setOffset(frameWidth + matWidth, frameWidth + matWidth);
			// Label
			Boolean b = exhibit.getHideLabel();
			boolean hideLabel = b == null ? exhibition.getHideLabel() : b.booleanValue();
			if (!hideLabel) {
				PBounds iBounds = pImage.getFullBoundsReference();
				imageLabel = new PPresentationPanel(0f, 0f, labelWidth, labelHeight, new Color(255, 255, 255),
						new Color(192, 192, 192), inputEventHandler);
				addChild(imageLabel);
				double ypos = labelMargins;
				Color penColor = new Color(92, 92, 92);
				Color selectedPenColor = new Color(92, 92, 92);
				Color background = (Color) pwall.getPaint();
				switch (exhibition.getLabelSequence()) {
				case Constants.EXHLABEL_TIT_CRED_DES:
					ypos += createCaption(exhibit, fontFamily, fontsize, 0, ypos, penColor, selectedPenColor,
							background);
					ypos += createCredits(exhibit, fontFamily, fontsize, 0, ypos, penColor, selectedPenColor,
							background);
					ypos += createDescription(exhibit, fontFamily, fontsize, labelIndent, ypos, penColor,
							selectedPenColor, background);
					break;
				case Constants.EXHLABEL_CRED_TIT_DES:
					ypos += createCredits(exhibit, fontFamily, fontsize, 0, ypos, penColor, selectedPenColor,
							background);
					ypos += createCaption(exhibit, fontFamily, fontsize, 0, ypos, penColor, selectedPenColor,
							background);
					ypos += createDescription(exhibit, fontFamily, fontsize, labelIndent, ypos, penColor,
							selectedPenColor, background);
					break;
				default:
					ypos += createCaption(exhibit, fontFamily, fontsize, 0, ypos, penColor, selectedPenColor,
							background);
					ypos += createDescription(exhibit, fontFamily, fontsize, labelIndent, ypos, penColor,
							selectedPenColor, background);
					ypos += createCredits(exhibit, fontFamily, fontsize, labelIndent, ypos, penColor, selectedPenColor,
							background);
					break;
				}
				// date
				creationDate = createTextLine(imageLabel, exhibit.getDate(), labelWidth - labelMargins * 2,
						labelMargins + labelIndent, ypos, penColor, background, selectedPenColor, fontFamily,
						Font.PLAIN, fontsize, ISpellCheckingService.NOSPELLING, SWT.SINGLE | SWT.WRAP);
				ypos += creationDate.getFullBoundsReference().getHeight();
				// warning
				resolutionWarning = new PSWTText("", new Font(fontFamily, //$NON-NLS-1$
						Font.ITALIC, Math.max(fontsize - 2, 4)));
				resolutionWarning.setPenColor(Color.RED);
				resolutionWarning.setOffset(labelMargins, ypos);
				resolutionWarning.setPickable(false);
				imageLabel.addChild(resolutionWarning);
				resolutionWarning.setGreekThreshold(1);
				computeResolutionWarning(asset);
				// position and bounds
				setBounds();
				setLabelOffset(iBounds, exhibit, exhibition);
				// sold mark
				updateSoldMark();
			}
			setOffset(pos - frameWidth - matWidth, y - frameWidth - matWidth);
			installHandleEventHandlers(pImage, false, false, this);
			if (pMat != null)
				installHandleEventHandlers(pMat, false, false, this);
			if (pFrame != null)
				installHandleEventHandlers(pFrame, false, false, this);
			if (imageLabel != null)
				installHandleEventHandlers(imageLabel, true, true, this);
			pImage.addInputEventListener(inputEventHandler);

			// size handle
			midPoint = new ExhibitHandle(handleSize);
			addChild(midPoint);
		}

		public void updateSoldMark() {
			if (exhibit.getSold()) {
				soldMark = new PSWTPath();
				soldMark.setPathToEllipse((int) imageLabel.getBoundsReference().getWidth() - 3 * soldMarkDiameter / 2,
						soldMarkDiameter / 2, soldMarkDiameter, soldMarkDiameter);
				soldMark.setPaint(Color.RED);
				soldMark.setStrokeColor(null);
				imageLabel.addChild(soldMark);
			} else if (soldMark != null) {
				imageLabel.removeChild(soldMark);
				soldMark = null;
			}
		}

		public void rescale() {
			imageWidth = exhibit.getWidth();
			imageHeight = exhibit.getHeight();
			PBounds bounds = pImage.getBoundsReference();
			double width = bounds.getWidth();
			double scale = imageWidth / width;
			pImage.setScale(scale);
			Integer i = exhibit.getMatWidth();
			int matWidth = (i == null) ? exhibition.getMatWidth() : i;
			i = exhibit.getFrameWidth();
			int frameWidth = (i == null) ? exhibition.getFrameWidth() : i;
			if (pFrame != null)
				pFrame.setPathToRectangle(0f, 0f, (float) (imageWidth + 2 * (frameWidth + matWidth)),
						(float) (imageHeight + 2 * (frameWidth + matWidth)));
			if (pMat != null)
				pMat.setPathToRectangle(frameWidth, frameWidth, (float) (imageWidth + 2 * matWidth),
						(float) (imageHeight + 2 * matWidth));
			setLabelOffset(pImage.getFullBoundsReference(), exhibit, exhibition);
		}

		private double createCredits(final ExhibitImpl exhibit, String fontFamily, int fontsize, int indent,
				double ypos, Color penColor, Color selectedPenColor, Color background) {
			credits = createTextLine(imageLabel, exhibit.getCredits(), labelWidth - labelMargins * 2,
					labelMargins + indent, ypos, penColor, background, selectedPenColor, fontFamily, Font.PLAIN,
					fontsize, ISpellCheckingService.NOSPELLING, SWT.SINGLE | SWT.WRAP);
			return credits.getFullBoundsReference().getHeight();
		}

		private double createDescription(final ExhibitImpl exhibit, String fontFamily, int fontsize, int indent,
				double ypos, Color penColor, Color selectedPenColor, Color background) {
			description = createTextLine(imageLabel, exhibit.getDescription(), labelWidth - labelMargins * 2,
					labelMargins + indent, ypos, penColor, background, selectedPenColor, fontFamily, Font.PLAIN,
					fontsize, ISpellCheckingService.DESCRIPTIONOPTIONS, SWT.MULTI | SWT.WRAP);
			return description.getFullBoundsReference().getHeight();
		}

		private double createCaption(final ExhibitImpl exhibit, String fontFamily, int fontsize, int indent,
				double ypos, Color penColor, Color selectedPenColor, Color background) {
			caption = createTextLine(imageLabel, exhibit.getTitle(), labelWidth - labelMargins * 2,
					labelMargins + indent, ypos, penColor, background, selectedPenColor, fontFamily, Font.BOLD,
					fontsize + 1, ISpellCheckingService.TITLEOPTIONS, SWT.SINGLE | SWT.WRAP);
			return caption.getFullBoundsReference().getHeight();
		}

		public void setImageHeight(int height) {
			imageHeight = height;
		}

		public void setImageWidth(int width) {
			imageWidth = width;
		}

		private void computeResolutionWarning(AssetImpl asset) {
			int pixels = asset.getWidth();
			int w = exhibit.getWidth();
			int h = exhibit.getWidth();
			double d = Math.sqrt((double) w * w + (double) h * h);
			double c = Math.sqrt(d) * 20;
			double res = Math.max(180, 100000 / c);
			double dpi = pixels * 25.4d / w;
			resolutionWarning.setText(NLS.bind(Messages.getString("ExhibitionView.image_res_problematic"), (int) dpi)); //$NON-NLS-1$
			resolutionWarning.setVisible(dpi < res);
		}

		private void setLabelOffset(PBounds iBounds, ExhibitImpl exhibit, ExhibitionImpl exhibition) {
			if (imageLabel != null) {
				PBounds lBounds = imageLabel.getBoundsReference();
				Integer i = exhibit.getMatWidth();
				int matWidth = (i == null) ? exhibition.getMatWidth() : i;
				i = exhibit.getFrameWidth();
				int frameWidth = (i == null) ? exhibition.getFrameWidth() : i;
				int tara = matWidth + frameWidth;
				double iWidth = iBounds.getWidth() + 2 * tara;
				double iHeight = iBounds.getHeight() + 2 * tara;
				double lx = 0;
				double ly = 0;
				Integer a = exhibit.getLabelAlignment();
				if (a == null)
					a = exhibition.getLabelAlignment();
				int alignment = a == null ? Constants.DEFAULTLABELALIGNMENT : a.intValue();
				Integer d = exhibit.getLabelDistance();
				if (d == null)
					d = exhibition.getLabelDistance();
				int distance = d == null ? Constants.DEFAULTLABELDISTANCE : d.intValue();
				i = exhibit.getLabelIndent();
				if (i == null)
					i = exhibition.getLabelIndent();
				int indent = i == null ? Constants.DEFAULTLABELINDENT : i.intValue();
				switch (alignment / 9) {
				case 0:
					lx += iWidth + distance;
					ly -= indent - align(iHeight, alignment, lBounds.getHeight(), false);
					break;
				case 1:
					lx += align(iWidth, alignment, lBounds.getWidth(), true) + indent;
					ly += iHeight + distance;
					break;
				case 2:
					lx -= distance + lBounds.getWidth();
					ly -= indent - align(iHeight, alignment, lBounds.getHeight(), true);
					break;
				default:
					lx += align(iWidth, alignment, lBounds.getWidth(), false) + indent;
					ly -= distance + lBounds.getHeight();
					break;
				}
				imageLabel.setOffset(lx, ly);
			}
		}

		private double align(double iWidth, int alignment, double lWidth, boolean reverse) {
			double labelAlign;
			switch (alignment % 3) {
			case 0:
				labelAlign = 0;
				break;
			case 1:
				labelAlign = lWidth / 2;
				break;
			default:
				labelAlign = lWidth;
				break;
			}
			switch (reverse ? 2 - (alignment / 3 % 3) : alignment / 3 % 3) {
			case 0:
				return -labelAlign;
			case 1:
				return iWidth / 2 - labelAlign;
			default:
				return iWidth - labelAlign;
			}
		}

		public void update(Asset asset) {
			double pos = getOffset().getX() + pImage.getOffset().getX();
			double y = getOffset().getY() + pImage.getOffset().getY();
			removeAllChildren();
			pImage.getImage().dispose();
			pImage = null;
			pFrame = null;
			pMat = null;
			createContent(canvas, pwall, exhibit, pos, y);
		}

		void relocateAllHandles() {
			midPoint.relocateHandle();
		}

		public void processTextEvent(TextField focus) {
			int type = -1;
			if (focus == caption)
				type = EditTextOperation.CAPTION;
			else if (focus == description)
				type = EditTextOperation.DESCRIPTION;
			else if (focus == credits)
				type = EditTextOperation.CREDITS;
			else if (focus == creationDate)
				type = EditTextOperation.DATE;
			if (type >= 0)
				performOperation(new EditTextOperation(exhibit, focus.getText(), focus, type));
		}

		public void updateFont(String fontFamily, int fontsize) {
			updateFont(caption, fontFamily, Font.BOLD, fontsize + 1);
			updateFont(description, fontFamily, Font.BOLD, fontsize);
			updateFont(credits, fontFamily, Font.BOLD, fontsize);
			updateFont(creationDate, fontFamily, Font.BOLD, fontsize);
			updateFont(resolutionWarning, fontFamily, Font.BOLD, Math.max(4, fontsize));
			setBounds();
		}

		private void setBounds() {
			if (imageLabel != null) {
				PBounds childrenBounds = imageLabel.getUnionOfChildrenBounds(null);
				childrenBounds.x = 0;
				childrenBounds.y = 0;
				childrenBounds.width += 2 * labelMargins;
				childrenBounds.height += 2 * labelMargins;
				imageLabel.setBounds(childrenBounds);
			}
			PBounds bounds = getUnionOfChildrenBounds(null);
			bounds.x = 0;
			bounds.y = 0;
			setBounds(bounds);
		}

		private void updateFont(PSWTText field, String fontFamily, int style, int size) {
			field.setFont(new Font(fontFamily, style, size));
		}

		private void updateFont(TextField field, String fontFamily, int style, int size) {
			field.setFont(new Font(fontFamily, style, size));
		}

		public void setTitleColor(Color color) {
			// ignore
		}

		public void setBackgroundColor(Color color) {
			// ignore
		}

		public void setPenColor(Color color) {
			// ignore
		}

		public void setSequenceNo(int i) {
			// ignore
		}

		public void updateColors(Color selectedPaint) {
			// ignore
		}

		public String getAssetId() {
			return exhibit.getAsset();
		}

	}

	public static final String ID = "com.bdaum.zoom.ui.ExhibitionView"; //$NON-NLS-1$
	private static final String LAST_EXHIBITION = "com.bdaum.zoom.lastExhibition"; //$NON-NLS-1$
	protected static final SimpleDateFormat EDF = new SimpleDateFormat("yyyy"); //$NON-NLS-1$
	public static final String EXHIBITION_PERSPECTIVE = "com.bdaum.zoom.ExhibitionPerspective"; //$NON-NLS-1$
	private static final float DOORWIDTH = 1000;
	private static final int DOORHEIGHT = 2000;
	private static final int INFOLEFT = 1;
	private static final int INFONONE = -1;
	private static final int INFOMARGINS = 40;
	private static final int INFOWIDTH = 950;
	private static final int INFOGAP = 50;
	private static final int INFOHEIGHT = 1333;
	private static NumberFormat dimFormat = NumberFormat.getNumberInstance();

	private ExhibitionImpl exhibition;
	private List<PWall> walls = new ArrayList<PWall>(5);
	private List<ExhibitImpl> exhibits = new ArrayList<ExhibitImpl>(50);
	private Action addWallAction;
	private Action layoutAction;
	private Action markSoldAction;
	private Action generateAction;
	private Set<String> offlineImages = null;
	private Action deleteWallAction;
	private Action editWallAction;

	private static TextField createTextLine(PNode parent, String text, int textWidth, int indent, double ypos,
			Color penColor, Color bgColor, Color selectedPenColor, String fontFamily, int fontStyle, int fontSize,
			int spellingOptions, int style) {
		TextField field = new TextField(text, textWidth, new Font(fontFamily, fontStyle, fontSize), penColor, bgColor,
				true, style);
		field.setSpellingOptions(10, spellingOptions);
		field.setOffset(indent, ypos);
		field.setSelectedPenColor(selectedPenColor);
		field.setPickable(true);
		parent.addChild(field);
		field.setGreekThreshold(3);
		return field;
	}

	public String getArtists() {
		Set<String> artists = new HashSet<String>();
		for (Exhibit exhibit : exhibits)
			artists.addAll(Core.fromStringList(exhibit.getCredits(), ",")); //$NON-NLS-1$
		String[] artistArray = artists.toArray(new String[artists.size()]);
		Arrays.sort(artistArray);
		return Core.toStringList(artistArray, ", "); //$NON-NLS-1$
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		if (memento != null) {
			String lastSelection = memento.getString(LAST_EXHIBITION);
			ExhibitionImpl obj = Core.getCore().getDbManager().obtainById(ExhibitionImpl.class, lastSelection);
			if (obj != null)
				setExhibition(obj);
		}
	}

	public void addGrid(PWall pwall) {
		List<PNode> gridLines = new ArrayList<PNode>();
		ListIterator<?> it = pwall.getChildrenIterator();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj instanceof GridLine)
				gridLines.add((PNode) obj);
		}
		pwall.removeChildren(gridLines);
		Wall wall = pwall.getWall();
		if (exhibition.getShowGrid()) {
			int gridSize = exhibition.getGridSize();
			int width = wall.getWidth();
			int height = wall.getHeight();
			float[] xdim = new float[] { 0f, width };
			float[] ydim = new float[] { 0f, 0f };
			for (float y = height - gridSize; y > 0; y -= gridSize) {
				ydim[0] = ydim[1] = y;
				pwall.addChild(0, new GridLine(xdim, ydim, gridSize));
			}
			ydim[0] = 0;
			ydim[1] = height;
			for (float x = gridSize; x < width; x += gridSize) {
				xdim[0] = xdim[1] = x;
				pwall.addChild(0, new GridLine(xdim, ydim, gridSize));
			}
		}
	}

	public void addGuide(PWall pwall) {
		List<PNode> guideLines = new ArrayList<PNode>();
		ListIterator<?> it = pwall.getChildrenIterator();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj instanceof GuideLine)
				guideLines.add((PNode) obj);
		}
		pwall.removeChildren(guideLines);
		Wall wall = pwall.getWall();
		int h = wall.getHeight() - wall.getExhibition_wall_parent().getDefaultViewingHeight();
		pwall.addChild(0, new GuideLine(new float[] { 0f, wall.getWidth() }, new float[] { h, h }));
	}

	@Override
	public void saveState(IMemento memento) {
		if (memento != null) {
			if (exhibition != null)
				memento.putString(LAST_EXHIBITION, exhibition.getStringId());
		}
		super.saveState(memento);
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		canvas.setData("id", "exhibition"); //$NON-NLS-1$ //$NON-NLS-2$
		undoContext = new UndoContext() {

			@Override
			public String getLabel() {
				return Messages.getString("ExhibitionView.exh_undo_context"); //$NON-NLS-1$
			}
		};
		setColor(canvas);
		addWheelListener(0.1d, 100d);
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
				PNode picked = getPickedNode();
				textEventHandler.mouseReleased(event);
				if (event.getClickCount() == 2) {
					PCamera camera = event.getCamera();
					if (oldTransform == null) {
						oldTransform = camera.getViewTransform();
						if (picked instanceof PExhibit) {
							camera.scaleViewAboutPoint(15d / camera.getViewScale(), event.getPosition().getX(),
									event.getPosition().getY());
						} else {
							if (picked instanceof PWall) {
								PWall pwall = (PWall) picked;
								PSWTPath infoPlate = pwall.getpInfoPlate();
								if (infoPlate != null) {
									Point2D offset = infoPlate.getOffset();
									Point2D position = event.getPositionRelativeTo(pwall);
									if (position.getX() >= offset.getX() && position.getX() <= offset.getX() + INFOWIDTH
											&& position.getY() >= offset.getY()
											&& position.getY() <= offset.getY() + INFOHEIGHT) {
										camera.scaleViewAboutPoint(7.5d / camera.getViewScale(),
												event.getPosition().getX(), event.getPosition().getY());
										return;
									}
								}
							}
							if (!(picked instanceof PSWTText))
								event.getCamera().setViewTransform(new AffineTransform());
						}
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
		// Setup
		setupExhibition(getViewSite(), exhibition, 0);
		// Drop-Unterstützung
		addDropListener(canvas);
		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(canvas, HelpContextIds.EXHIBITION_VIEW);
		// Hover
		installHoveringController();
		// Actions
		makeActions();
		installListeners(parent);
		hookContextMenu();
		contributeToActionBars();
		if (exhibition != null)
			setInput(exhibition);
		addCatalogListener();
		updateActions();
	}

	@Override
	protected void makeActions() {
		super.makeActions();
		propertiesAction = new Action(Messages.getString("ExhibitionView.properties"), //$NON-NLS-1$
				Icons.exhibition.getDescriptor()) {

			@Override
			public void run() {
				ExhibitionImpl show = getExhibition();
				if (show == null)
					return;
				ExhibitionImpl backup = ExhibitionPropertiesOperation.cloneExhibition(show);
				show = ExhibitionEditDialog.open(getSite().getShell(), null, show, show.getName(), true, null);
				if (show != null) {
					exhibition = show;
					performOperation(new ExhibitionPropertiesOperation(backup, exhibition));
					setInput(exhibition);
				}
			}
		};
		propertiesAction.setToolTipText(Messages.getString("ExhibitionView.edit_exhibition_properties")); //$NON-NLS-1$
		addWallAction = new Action(Messages.getString("ExhibitionView.add_wall"), Icons.add_obj.getDescriptor()) { //$NON-NLS-1$

			@Override
			public void run() {
				ExhibitionImpl show = getExhibition();
				if (show == null)
					return;
				int l = show.getWall().size();
				int y = 10;
				for (Wall w : show.getWall())
					y += w.getHeight() + 15;
				EditWallDialog dialog = new EditWallDialog(getSite().getShell(), null,
						NLS.bind(Messages.getString("ExhibitionView.wall_n"), l + 1)); //$NON-NLS-1$
				if (dialog.open() == Window.OK)
					performOperation(new CreateWallOperation(dialog.getResult(), y));
			}
		};
		addWallAction.setToolTipText(Messages.getString("ExhibitionView.add_a_new_wall")); //$NON-NLS-1$
		deleteWallAction = new Action(Messages.getString("ExhibitionView.delete_wall_action"), //$NON-NLS-1$
				Icons.delete.getDescriptor()) {

			@Override
			public void run() {
				PNode node = getPickedNode();
				while (node != null && !(node instanceof PWall))
					node = node.getParent();
				if (node != null) {
					if (AcousticMessageDialog.openQuestion(getSite().getShell(),
							Messages.getString("ExhibitionView.remove_wall"), //$NON-NLS-1$
							Messages.getString("ExhibitionView.do_you_really_want_to_remove"))) //$NON-NLS-1$
						performOperation(new DeleteWallOperation((PWall) node));
				}
			}
		};
		deleteWallAction.setToolTipText(Messages.getString("ExhibitionView.delete_wall_tooltip")); //$NON-NLS-1$
		editWallAction = new Action(Messages.getString("ExhibitionView.edit_wall_action"), //$NON-NLS-1$
				Icons.properties_blue.getDescriptor()) {

			@Override
			public void run() {
				if (getPickedNode() instanceof PWall)
					editWallLegend((PWall) getPickedNode());
			}
		};
		editWallAction.setToolTipText(Messages.getString("ExhibitionView.edit_wall_tooltip")); //$NON-NLS-1$
		generateAction = new Action(Messages.getString("ExhibitionView.generate"), Icons.play.getDescriptor()) { //$NON-NLS-1$

			@Override
			public void run() {
				generate();
			}
		};
		generateAction.setToolTipText(Messages.getString("ExhibitionView.generate_exhibition")); //$NON-NLS-1$

		gotoExhibitAction = new Action(Messages.getString("ExhibitionView.goto_exhibit")) { //$NON-NLS-1$

			@Override
			public void run() {
				List<ExhibitImpl> list = new ArrayList<ExhibitImpl>(300);
				for (PWall w : walls) {
					ListIterator<?> it = w.getChildrenIterator();
					while (it.hasNext()) {
						Object next = it.next();
						if (next instanceof PExhibit && ((PExhibit) next).exhibit != null)
							list.add(((PExhibit) next).exhibit);
					}
				}
				SelectExhibitDialog dialog = new SelectExhibitDialog(getSite().getShell(), list);
				dialog.create();
				org.eclipse.swt.graphics.Point pos = canvas.toDisplay((int) positionRelativeToCamera.getX(),
						(int) positionRelativeToCamera.getY());
				pos.x += 10;
				pos.y += 10;
				dialog.getShell().setLocation(pos);
				if (dialog.open() == Window.OK) {
					ExhibitImpl selectedExhibit = dialog.getResult();
					for (PWall w : walls) {
						ListIterator<?> it = w.getChildrenIterator();
						while (it.hasNext()) {
							Object next = it.next();
							if (next instanceof PExhibit) {
								PExhibit pexhibit = (PExhibit) next;
								if (pexhibit.exhibit == selectedExhibit) {
									Point2D offset = pexhibit.getOffset();
									PBounds bounds = pexhibit.getBounds();
									w.localToParent(offset);
									w.localToParent(bounds);
									surface.localToParent(offset);
									surface.localToParent(bounds);
									PCamera camera = canvas.getCamera();
									double x = offset.getX();
									double y = offset.getY();
									camera.setViewTransform(new AffineTransform());
									camera.translateView(bounds.width / 3 - x, bounds.height / 2 - y);
									return;
								}
							}
						}
					}
				}
			}
		};
		gotoExhibitAction.setToolTipText(Messages.getString("ExhibitionView.goto_exhibit_tooltip")); //$NON-NLS-1$

		layoutAction = new Action(Messages.getString("ExhibitionView.layout")) { //$NON-NLS-1$
			@Override
			public void run() {
				PExhibit pExhibit = getPickedExhibit();
				if (pExhibit != null) {
					ExhibitImpl exhibit = pExhibit.exhibit;
					ExhibitImpl backup = cloneExhibit(exhibit);
					ExhibitLayoutDialog dialog = new ExhibitLayoutDialog(getSite().getShell(), exhibition, exhibit);
					if (dialog.open() == Dialog.OK)
						performOperation(new LayoutOperation(Messages.getString("ExhibitionView.set_layout"), //$NON-NLS-1$
								backup, exhibit));
				}
			}
		};
		layoutAction.setToolTipText(Messages.getString("ExhibitionView.layout_tooltip")); //$NON-NLS-1$
		markSoldAction = new Action() {
			@Override
			public void run() {
				PExhibit pExhibit = getPickedExhibit();
				if (pExhibit != null)
					performOperation(new MarkSoldOperation(pExhibit));
			}
		};
	}

	private PExhibit getPickedExhibit() {
		PNode pickedNode = getPickedNode();
		if (pickedNode != null) {
			if (pickedNode instanceof PExhibit)
				return (PExhibit) pickedNode;
			else if (pickedNode.getParent() instanceof PExhibit)
				return (PExhibit) pickedNode.getParent();
		}
		return null;
	}

	@Override
	public void updateActions() {
		if (addWallAction == null)
			return;
		super.updateActions();
		addWallAction.setEnabled(!dbIsReadonly());
	}

	@Override
	protected void fillLocalToolBar(IToolBarManager manager) {
		super.fillLocalToolBar(manager);
		manager.add(addWallAction);
		manager.add(new Separator());
		manager.add(generateAction);
	}

	@Override
	protected void fillLocalPullDown(IMenuManager manager) {
		manager.add(addWallAction);
		manager.add(synchronizeAction);
		manager.add(new Separator());
		manager.add(generateAction);
		super.fillLocalPullDown(manager);
	}

	@Override
	protected void fillContextMenu(IMenuManager manager) {
		manager.add(gotoExhibitAction);
		PNode pickedNode = getPickedNode();
		if (pickedNode instanceof PExhibit) {
			addSoldAction(manager, (PExhibit) pickedNode);
			manager.add(layoutAction);
		} else if (pickedNode != null && pickedNode.getParent() instanceof PExhibit) {
			addCommonContextActions(manager);
			manager.add(new Separator());
			addSoldAction(manager, (PExhibit) pickedNode.getParent());
			manager.add(layoutAction);
		} else {
			if (pickedNode != null && pickedNode.getParent() instanceof PWall)
				pickedNode = pickedNode.getParent();
			if (pickedNode instanceof PWall) {
				manager.add(new Separator());
				manager.add(editWallAction);
				manager.add(deleteWallAction);
			}
		}
		super.fillContextMenu(manager);
	}

	private void addSoldAction(IMenuManager manager, PExhibit exhibit) {
		boolean sold = exhibit.exhibit.getSold();
		markSoldAction.setText(sold ? Messages.getString("ExhibitionView.remove_sold_mark") //$NON-NLS-1$
				: Messages.getString("ExhibitionView.mark_as_sold")); //$NON-NLS-1$
		markSoldAction.setToolTipText(sold ? Messages.getString("ExhibitionView.remove_sold_tooltip") //$NON-NLS-1$
				: Messages.getString("ExhibitionView.mark_sold_tooltip")); //$NON-NLS-1$
		manager.add(markSoldAction);
	}

	@Override
	protected void updatePresentation(Collection<? extends Asset> assets) {
		Map<String, Asset> amap = new HashMap<String, Asset>(assets.size());
		for (Asset a : assets)
			amap.put(a.getStringId(), a);
		for (PNode wall : walls) {
			ListIterator<?> it = wall.getChildrenIterator();
			while (it.hasNext()) {
				Object next = it.next();
				if (next instanceof PExhibit) {
					PExhibit pexhibit = (PExhibit) next;
					Asset asset = amap.get(pexhibit.exhibit.getAsset());
					if (asset != null) {
						pexhibit.update(asset);
						break;
					}
				}
			}
		}
	}

	protected PExhibit makeExhibit(PWall pwall, int pos, ExhibitImpl exhibit) {
		PExhibit pExhibit = new PExhibit(canvas, pwall, exhibit, pos, pwall.getWall().getHeight() - exhibit.getY());
		pwall.addChild(pExhibit);
		return pExhibit;
	}

	protected void deleteExhibit(PExhibit moved, Point2D oldOffset) {
		if (getExhibition() != null)
			performOperation(new DeleteExhibitOperation(moved, oldOffset));

	}

	protected PWall doRemoveExhibit(PExhibit moved) {
		PNode node = moved.getParent();
		if (node instanceof PWall) {
			PWall pwall = (PWall) node;
			pwall.removeChild(moved);
			ExhibitImpl exhibit = moved.exhibit;
			exhibits.remove(exhibit);
			Wall wall = pwall.getWall();
			wall.removeExhibit(exhibit.getStringId());
			return pwall;
		}
		return null;
	}

	protected void moveExhibit(PExhibit moved, PWall target, Point2D oldOffset, Point2D newOffset) {
		performOperation(new MoveExhibitOperation(moved, oldOffset, target, newOffset));
	}

	protected ExhibitionImpl getExhibition() {
		if (exhibition == null && !dbIsReadonly())
			exhibition = ExhibitionEditDialog.open(getSite().getShell(), null, null,
					Messages.getString("ExhibitionView.create_exhibition"), false, null); //$NON-NLS-1$
		return exhibition;
	}

	@Override
	protected void setColor(Control control) {
		Color newPaint = UiUtilities.getAwtBackground(control, null);
		selectionBackgroundColor = new Color(224, 224, 124); // .getAwtForeground(control,
																// null);
		CssActivator.getDefault().applyExtendedStyle(control, this);
		surface.setPaint(newPaint);
		for (PWall pwall : walls) {
			setColor(pwall, pwall.getWall());
			pwall.setStrokeColor(selectionBackgroundColor);
		}
	}

	@Override
	public void setSelectionBackgroundColor(org.eclipse.swt.graphics.Color color) {
		RGB rgb = color != null ? color.getRGB() : new RGB(224, 224, 124);
		selectionBackgroundColor = new Color(rgb.red, rgb.green, rgb.blue);
	}

	protected void setColor(PSWTPath path, Wall wall) {
		Rgb_type bgColor = wall.getColor();
		Color wallPaint = (bgColor == null) ? new Color(255, 255, 250)
				: new Color(bgColor.getR(), bgColor.getG(), bgColor.getB());
		path.setPaint(wallPaint);
	}

	@Override
	public void setInput(IdentifiableObject presentation) {
		super.setInput(presentation);
		if (presentation instanceof ExhibitionImpl) {
			ExhibitionImpl exh = (ExhibitionImpl) presentation;
			setExhibition(exh);
			setupExhibition(getViewSite(), exh, 0);
			setPartName(exh.getName());
			int n = 0;
			for (Wall wall : exh.getWall())
				n += wall.getExhibit().size();
			beginTask(n);
			int y = TOPINDENT;
			for (Wall wall : exh.getWall()) {
				addWall(wall, y, false, progressBar);
				y += wall.getHeight() + V_WALLMARGINS;
			}
			setArtists();
			setColor(canvas);
			updateActions();
			endTask();
		} else
			setPartName(Messages.getString("ExhibitionView.exhibition")); //$NON-NLS-1$
	}

	private PWall addWall(Wall wall, double y, boolean reposition, ProgressIndicator bar) {
		PWall pwall = new PWall(wall);
		surface.addChild(pwall);
		walls.add(pwall);
		pwall.setOffset(0, y);
		int pos = 0;
		for (ExhibitImpl exhibit : Core.getCore().getDbManager().obtainByIds(ExhibitImpl.class, wall.getExhibit())) {
			exhibits.add(exhibit);
			if (reposition)
				pos += exhibit.getWidth() / 2;
			else
				pos = exhibit.getX();
			makeExhibit(pwall, pos, exhibit);
			pos += exhibit.getWidth();
		}
		if (bar != null)
			bar.worked(1);
		setupExhibition(getViewSite(), exhibition, pos);
		setPanAndZoomHandlers();
		return pwall;
	}

	private void setPanAndZoomHandlers() {
		setPanAndZoomHandlers(-11, -14);
	}

	private void setExhibition(ExhibitionImpl exhibition) {
		this.exhibition = exhibition;
		if (exhibition.getWall().isEmpty()) {
			Wall wall = new WallImpl();
			wall.setLocation(Messages.getString("ExhibitionView.wall_1")); //$NON-NLS-1$
			wall.setWidth(5000);
			wall.setHeight(2500);
			wall.setColor(new Rgb_typeImpl(255, 255, 250));
			exhibition.addWall(wall);
			List<Object> toBeStored = new ArrayList<Object>(2);
			toBeStored.add(wall);
			toBeStored.add(exhibition);
			storeSafelyAndUpdateIndex(null, toBeStored, null);
		}
	}

	private void setupExhibition(IViewSite site, ExhibitionImpl exhibition, int maxwidth) {
		if (exhibition != null) {
			int h = 10;
			int w = 0;
			for (Wall wall : exhibition.getWall()) {
				h += wall.getHeight();
				if (w > 0)
					h += wall.getHeight() / 2;
				if (wall.getWidth() > w)
					w = wall.getWidth();
			}
			surface.setBounds(-3000d, -3000d,
					Math.max(Math.max(maxwidth, w) + 6000d, surface.getBoundsReference().width), h + 6000d);
			surface.setScale((double) site.getShell().getDisplay().getPrimaryMonitor().getBounds().height / (2 * h));
		}
	}

	@Override
	protected void cleanUp() {
		try {
			for (PWall wall : walls) {
				wall.removeAllChildren();
				PNode parent = wall.getParent();
				if (parent != null)
					parent.removeChild(wall);
			}
		} catch (SWTException e) {
			// ignore
		}
		exhibits.clear();
		walls.clear();
		super.cleanUp();
	}

	@Override
	protected void dropAssets(ISelection selection, org.eclipse.swt.graphics.Point point, Point2D position,
			boolean replace) {
		performOperation(new DropAssetOperation((AssetSelection) selection, position, replace));
		getSite().getPage().activate(this);
	}

	@Override
	protected PNode findExhibit(Point point, Point2D position) {
		RECT1.x = (int) position.getX();
		RECT1.y = (int) position.getY();
		PWall wallPanel = null;
		for (PWall w : walls)
			if (w.fullIntersects(RECT1)) {
				wallPanel = w;
				break;
			}
		if (wallPanel != null) {
			double x = position.getX() - wallPanel.getOffset().getX();
			ListIterator<?> it = wallPanel.getChildrenIterator();
			while (it.hasNext()) {
				Object next = it.next();
				if (next instanceof PExhibit) {
					PExhibit pExhibit = (PExhibit) next;
					PBounds eBounds = pExhibit.getFullBoundsReference();
					double p = eBounds.getX();
					if (p < x) {
						p += eBounds.getWidth();
						if (p >= x)
							return pExhibit;
					}
				}
			}
		}
		return null;
	}

	private void editWallLegend(PWall pwall) {
		WallImpl w = (WallImpl) pwall.getWall();
		WallImpl backup = new WallImpl(w.getLocation(), w.getX(), w.getY(), w.getWidth(), w.getHeight(), w.getGX(),
				w.getGY(), w.getGAngle(), w.getColor());
		EditWallDialog dialog = new EditWallDialog(getSite().getShell(), w, w.getLocation());
		if (dialog.open() == Window.OK)
			performOperation(new EditWallOperation(w, backup, pwall));
	}

	@Override
	protected PNode[] getWorkArea() {
		return walls.toArray(new PNode[walls.size()]);
	}

	@Override
	protected int getPanDirection() {
		return GalleryPanEventHandler.BOTH;
	}

	@Override
	protected int getHoriztontalMargins() {
		return LEFTINDENT;
	}

	@Override
	public Object getContent() {
		return exhibition;
	}

	@Override
	protected AbstractHandleDragSequenceEventHandler createDragSequenceEventHandler(PNode node, boolean relative,
			boolean restricted, PNode presentationObject) {
		return new AbstractPresentationView.AbstractHandleDragSequenceEventHandler(node, relative, restricted,
				presentationObject) {

			@Override
			protected void drop(PInputEvent event, PNode pnode) {
				PExhibit pExhibit = (PExhibit) pnode.getParent();
				Point2D canvasPosition = event.getCanvasPosition();
				if (canvasPosition.getY() < 0) {
					if (!relative)
						deleteExhibit(pExhibit, oldOffset);
				} else if (relative) {
					Point2D newOffset = pnode.getOffset();
					moveLabel(pnode, oldOffset, newOffset);
				} else {
					Point2D newOffset = pExhibit.getOffset();
					Point2D parentOffset = ((PWall) pExhibit.getParent()).getOffset();
					Point2D point = new Point2D.Double(newOffset.getX() + parentOffset.getX(),
							event.getPosition().getY());
					surface.parentToLocal(point);
					for (PWall pwall : walls) {
						Point2D offset = pwall.getOffset();
						double y = point.getY() - offset.getY();
						PBounds bounds = pwall.getBoundsReference();
						if (bounds.getY() <= y && bounds.getY() + bounds.getHeight() >= y) {
							moveExhibit(pExhibit, pwall, oldOffset, newOffset);
							return;
						}
					}
					pExhibit.setOffset(oldOffset);
				}
			}

			@Override
			public Point getDragTolerance() {
				return DRAGTOLERANCE;
			}
		};

	}

	protected void moveLabel(PNode pnode, Point2D oldOffset, Point2D newOffset) {
		PExhibit pExhibit = (PExhibit) pnode.getParent();
		PNode pFrame = pExhibit.pFrame;
		if (pFrame == null)
			pFrame = pExhibit.pMat;
		if (pFrame == null)
			pFrame = pExhibit.pImage;
		PBounds iBounds = pFrame.getGlobalBounds();
		double iMidX = iBounds.width / 2 + iBounds.x;
		double iMidY = iBounds.height / 2 + iBounds.y;
		double aspect = iBounds.width / iBounds.height;
		PBounds lBounds = pnode.getGlobalBounds();
		double lMidX = lBounds.width / 2 + lBounds.x;
		double lMidY = lBounds.height / 2 + lBounds.y;
		double degrees;
		if (lMidX == iMidX) {
			if (lMidY > iMidY)
				degrees = 0;
			else
				degrees = 180;
		} else {
			double angle = Math.atan((lMidY - iMidY) * aspect / (lMidX - iMidX));
			degrees = 90 - Math.toDegrees(angle);
			if (lMidX < iMidX)
				degrees += 180;
		}
		int segment = (int) ((degrees + 15) / 30) % 12;
		double distance = 0;
		double indent = 0;
		int labPoint = 1;
		switch (segment) {
		case 0:
			labPoint = getClosestHorizontal(iBounds.width / 2 + iBounds.x, iBounds.height + iBounds.y, lBounds.y,
					lBounds);
			distance = lBounds.y - (iBounds.height + iBounds.y);
			indent = computeIndent(labPoint, iBounds.width / 2 + iBounds.x, lBounds.x, lBounds.width);
			break;
		case 1:
			labPoint = getClosestHorizontal(iBounds.width + iBounds.x, iBounds.height + iBounds.y, lBounds.y, lBounds);
			distance = lBounds.y - (iBounds.height + iBounds.y);
			indent = computeIndent(labPoint, iBounds.width + iBounds.x, lBounds.x, lBounds.width);
			break;
		case 2:
			labPoint = getClosestVertical(iBounds.width + iBounds.x, iBounds.height + iBounds.y, lBounds.x, lBounds);
			distance = lBounds.x - (iBounds.width + iBounds.x);
			indent = -computeIndent(labPoint, iBounds.height + iBounds.y, lBounds.y, lBounds.height);
			break;
		case 3:
			labPoint = getClosestVertical(iBounds.width + iBounds.x, iBounds.height / 2 + iBounds.y, lBounds.x,
					lBounds);
			distance = lBounds.x - (iBounds.width + iBounds.x);
			indent = -computeIndent(labPoint, iBounds.height / 2 + iBounds.y, lBounds.y, lBounds.height);
			break;
		case 4:
			labPoint = getClosestVertical(iBounds.width + iBounds.x, iBounds.y, lBounds.x, lBounds);
			distance = lBounds.x - (iBounds.width + iBounds.x);
			indent = -computeIndent(labPoint, iBounds.y, lBounds.y, lBounds.height);
			break;
		case 5:
			labPoint = getClosestHorizontal(iBounds.width + iBounds.x, iBounds.y, lBounds.y + lBounds.height, lBounds);
			distance = iBounds.y - (lBounds.height + lBounds.y);
			indent = computeIndent(labPoint, iBounds.width + iBounds.x, lBounds.x, lBounds.width);
			break;
		case 6:
			labPoint = getClosestHorizontal(iBounds.width / 2 + iBounds.x, iBounds.y, lBounds.y + lBounds.height,
					lBounds);
			distance = iBounds.y - (lBounds.height + lBounds.y);
			indent = computeIndent(labPoint, iBounds.width / 2 + iBounds.x, lBounds.x, lBounds.width);
			break;
		case 7:
			labPoint = getClosestHorizontal(iBounds.x, iBounds.y, lBounds.y + lBounds.height, lBounds);
			distance = iBounds.y - (lBounds.height + lBounds.y);
			indent = computeIndent(labPoint, iBounds.x, lBounds.x, lBounds.width);
			break;
		case 8:
			labPoint = getClosestVertical(iBounds.x, iBounds.y, lBounds.x + lBounds.width, lBounds);
			distance = iBounds.x - (lBounds.width + lBounds.x);
			indent = -computeIndent(labPoint, iBounds.y, lBounds.y, lBounds.height);
			break;
		case 9:
			labPoint = getClosestVertical(iBounds.x, iBounds.height / 2 + iBounds.y, lBounds.x + lBounds.width,
					lBounds);
			distance = iBounds.x - (lBounds.width + lBounds.x);
			indent = -computeIndent(labPoint, iBounds.height / 2 + iBounds.y, lBounds.y, lBounds.height);
			break;
		case 10:
			labPoint = getClosestVertical(iBounds.x, iBounds.height + iBounds.y, lBounds.x + lBounds.width, lBounds);
			distance = iBounds.x - (lBounds.width + lBounds.x);
			indent = -computeIndent(labPoint, iBounds.height + iBounds.y, lBounds.y, lBounds.height);
			break;
		case 11:
			labPoint = getClosestHorizontal(iBounds.x, iBounds.height + iBounds.y, lBounds.y, lBounds);
			distance = lBounds.y - (iBounds.height + iBounds.y);
			indent = computeIndent(labPoint, iBounds.x, lBounds.x, lBounds.width);
			break;
		}
		int alignment = (16 - segment) % 12 * 3 + labPoint;
		double globalScale = pFrame.getGlobalScale();
		ExhibitImpl exhibit = pExhibit.exhibit;
		ExhibitImpl backup = cloneExhibit(exhibit);
		exhibit.setLabelAlignment(alignment);
		exhibit.setLabelDistance((int) (distance / globalScale + 0.5d));
		exhibit.setLabelIndent((int) (indent / globalScale + 0.5d));
		performOperation(new LayoutOperation(Messages.getString("ExhibitionView.move_label"), backup, exhibit)); //$NON-NLS-1$
	}

	private static double computeIndent(int point, double ix, double lx, double lwidth) {
		switch (point) {
		case 0:
			return lx - ix;
		case 1:
			return lx + lwidth / 2 - ix;
		default:
			return lx + lwidth - ix;
		}
	}

	private static int getClosestVertical(double ix, double iy, double lx, PBounds lBounds) {
		double dx2 = (lx - ix) * (lx - ix);
		double d0 = (lBounds.y - iy) * (lBounds.y - iy) + dx2;
		double d1 = (lBounds.y + lBounds.height / 2 - iy) * (lBounds.y + lBounds.height / 2 - iy) + dx2;
		double d2 = (lBounds.y + lBounds.height - iy) * (lBounds.y + lBounds.height - iy) + dx2;
		double min = Math.min(d0, Math.min(d1, d2));
		if (d1 == min)
			return 1;
		if (d0 == min)
			return 0;
		return 2;
	}

	private static int getClosestHorizontal(double ix, double iy, double ly, PBounds lBounds) {
		double dy2 = (ly - iy) * (ly - iy);
		double d0 = (lBounds.x - ix) * (lBounds.x - ix) + dy2;
		double d1 = (lBounds.x + lBounds.width / 2 - ix) * (lBounds.x + lBounds.width / 2 - ix) + dy2;
		double d2 = (lBounds.x + lBounds.width - ix) * (lBounds.x + lBounds.width - ix) + dy2;
		double min = Math.min(d0, Math.min(d1, d2));
		if (d1 == min)
			return 1;
		if (d0 == min)
			return 0;
		return 2;

	}

	private static ExhibitImpl cloneExhibit(Exhibit exhibit) {
		return new ExhibitImpl(exhibit.getTitle(), exhibit.getDescription(), exhibit.getCredits(), exhibit.getDate(),
				exhibit.getX(), exhibit.getY(), exhibit.getWidth(), exhibit.getHeight(), exhibit.getMatWidth(),
				exhibit.getMatColor(), exhibit.getFrameWidth(), exhibit.getFrameColor(), exhibit.getSold(),
				exhibit.getHideLabel(), exhibit.getLabelAlignment(), exhibit.getLabelDistance(),
				exhibit.getLabelIndent(), exhibit.getAsset());
	}

	private void generate() {
		ExhibitionImpl show = getExhibition();
		if (show == null)
			return;
		if (offlineImages != null && !offlineImages.isEmpty()) {
			String[] volumes = offlineImages.toArray(new String[offlineImages.size()]);
			Arrays.sort(volumes);
			AcousticMessageDialog.openInformation(getSite().getShell(),
					Messages.getString("ExhibitionView.images_offline"), //$NON-NLS-1$
					volumes.length > 1
							? NLS.bind(Messages.getString("ExhibitionView.volumes_offline"), //$NON-NLS-1$
									Core.toStringList(volumes, ", ")) //$NON-NLS-1$
							: NLS.bind(Messages.getString("ExhibitionView.volume_offline"), //$NON-NLS-1$
									volumes[0]));
			return;
		}
		String s = exhibition.getStringId();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (Character.isLetterOrDigit(c))
				sb.append(c);
		}
		String exhibitionId = sb.toString();
		boolean isFtp = show.getIsFtp();
		String outputFolder = (isFtp) ? show.getFtpDir() : show.getOutputFolder();
		while (outputFolder == null || outputFolder.length() == 0) {
			show = ExhibitionEditDialog.open(getSite().getShell(), null, show, show.getName(), false,
					Messages.getString("ExhibitionView.please_specify_target")); //$NON-NLS-1$
			if (show == null)
				return;
			isFtp = show.getIsFtp();
			outputFolder = (isFtp) ? show.getFtpDir() : show.getOutputFolder();
		}
		boolean makeDefault = false;
		boolean askForDefault = false;
		final File file = new File(outputFolder);
		if (!isFtp && file.exists() && file.listFiles().length > 0) {
			AcousticMessageDialog dialog;
			File resFile = new File(file, "res"); //$NON-NLS-1$
			File[] rooms = resFile.listFiles(new FileFilter() {
				public boolean accept(File f) {
					return f.getName().startsWith("room_"); //$NON-NLS-1$
				}
			});
			if (rooms.length > 0) {
				File roomFile = new File(resFile, "room_" //$NON-NLS-1$
						+ exhibitionId);
				if (roomFile.exists()) {
					askForDefault = rooms.length > 1;
					dialog = new AcousticMessageDialog(getSite().getShell(),
							Messages.getString("ExhibitionView.overwriteTitle"), //$NON-NLS-1$
							null, NLS.bind(Messages.getString("ExhibitionView.exhibition_exists"), file), //$NON-NLS-1$
							MessageDialog.QUESTION,
							new String[] { Messages.getString("ExhibitionView.overwrite"), //$NON-NLS-1$
									Messages.getString("ExhibitionView.clear_folder"), //$NON-NLS-1$
									IDialogConstants.CANCEL_LABEL },
							0);
				} else {
					askForDefault = true;
					dialog = new AcousticMessageDialog(getSite().getShell(), Messages.getString("ExhibitionView.add"), //$NON-NLS-1$
							null, NLS.bind(Messages.getString("ExhibitionView.other_exhibitions"), file), //$NON-NLS-1$
							MessageDialog.QUESTION,
							new String[] { Messages.getString("ExhibitionView.add_room"), //$NON-NLS-1$
									Messages.getString("ExhibitionView.clear_folder"), //$NON-NLS-1$
									IDialogConstants.CANCEL_LABEL },
							0);
				}
			} else {
				dialog = new AcousticMessageDialog(getSite().getShell(),
						Messages.getString("ExhibitionView.overwriteTitle"), //$NON-NLS-1$
						null, NLS.bind(Messages.getString("ExhibitionView.not_empty"), file), //$NON-NLS-1$
						MessageDialog.QUESTION,
						new String[] { Messages.getString("ExhibitionView.overwrite"), //$NON-NLS-1$
								Messages.getString("ExhibitionView.clear_folder"), //$NON-NLS-1$
								IDialogConstants.CANCEL_LABEL },
						0);
			}
			int ret = dialog.open();
			switch (ret) {
			case 2:
				return;
			case 1:
				Core.deleteFileOrFolder(file);
				break;
			}
		}
		if (askForDefault)
			makeDefault = AcousticMessageDialog.openQuestion(getSite().getShell(),
					Messages.getString("ExhibitionView.exhibition_start"), //$NON-NLS-1$
					Messages.getString("ExhibitionView.start_viewing")); //$NON-NLS-1$
		String page = show.getPageName();
		final boolean ftp = isFtp;
		final File start = new File(file, (page == null || page.length() == 0) ? "index.html" //$NON-NLS-1$
				: page);
		final ExhibitionJob job = new ExhibitionJob(show, exhibitionId, ExhibitionView.this, makeDefault);
		final FtpAccount account;
		if (ftp) {
			account = FtpAccount.findAccount(outputFolder);
			if (account == null) {
				AcousticMessageDialog.openError(getSite().getShell(),
						Messages.getString("ExhibitionView.account_not_existing"), //$NON-NLS-1$
						NLS.bind(Messages.getString("ExhibitionView.account_not_defined"), //$NON-NLS-1$
								outputFolder));
				return;
			}
		} else
			account = null;
		job.addJobChangeListener(new JobChangeAdapter() {

			@Override
			public void done(IJobChangeEvent event) {
				if (!job.wasAborted()) {
					if (ftp)
						new TransferJob(job.getTargetFolder().listFiles(), account, true).schedule(0);
					else
						showExhibition(start);
				}
			}
		});
		job.schedule();
	}

	// private ExhibitionImpl openExhibitionEditDialog(final Shell shell,
	// final GroupImpl group, final ExhibitionImpl gallery,
	// final String title, final boolean canUndo) {
	// final ExhibitionImpl[] box = new ExhibitionImpl[1];
	// BusyIndicator.showWhile(shell.getDisplay(), new Runnable() {
	// public void run() {
	// ExhibitionEditDialog dialog = new ExhibitionEditDialog(shell,
	// group, gallery, title, canUndo);
	// dialog.create();
	// dialog.selectOutputPage(Messages
	// .getString("ExhibitionView.please_specify_target")); //$NON-NLS-1$
	// if (dialog.open() == Window.OK)
	// box[0] = dialog.getResult();
	// }
	// });
	// return box[0];
	// }

	private void showExhibition(final File start) {
		final Display display = getSite().getShell().getDisplay();
		display.asyncExec(new Runnable() {
			public void run() {
				BusyIndicator.showWhile(display, new Runnable() {
					public void run() {
						try {
							URL url = start.toURI().toURL();
							IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser();
							if (browser != null)
								browser.openURL(url);
						} catch (MalformedURLException e) {
							// should not happen
						} catch (PartInitException e) {
							// ignore
						}
					}
				});
			}
		});
	}

	@Override
	protected PBounds getClientAreaReference() {
		PBounds bounds = new PBounds(0, 0, 0, 0);
		for (PWall pwall : walls) {
			PBounds b1 = pwall.getBoundsReference();
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

	private void setArtists() {
		ListIterator<?> it1 = surface.getChildrenIterator();
		while (it1.hasNext()) {
			Object obj = it1.next();
			if (obj instanceof PWall)
				((PWall) obj).setArtists();
		}
	}

	public static boolean accepts(Asset asset) {
		if (asset == null)
			return false;
		IMediaSupport mediaSupport = CoreActivator.getDefault().getMediaSupport(asset.getFormat());
		return mediaSupport == null || mediaSupport.testProperty(IMediaSupport.EXHIBITION);
	}

	@Override
	protected void refreshAfterHistoryEvent(IUndoableOperation operation) {
		if (operation instanceof ExhibitionPropertiesOperation)
			setInput(exhibition);
	}

}
