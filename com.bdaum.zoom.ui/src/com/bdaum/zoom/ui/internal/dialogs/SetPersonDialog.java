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
 * (c) 2018 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.deferred.DeferredContentProvider;
import org.eclipse.jface.viewers.deferred.SetModel;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.cat.model.asset.RegionImpl;
import com.bdaum.zoom.cat.model.group.Group;
import com.bdaum.zoom.cat.model.group.GroupImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.SortCriterionImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IAssetProvider;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiUtilities;

@SuppressWarnings("restriction")
public class SetPersonDialog extends ZTitleAreaDialog {

	private static final int NEWALBUM = 9999;
	private static final int DELETEREGION = 9998;
	private static final int DELETEALLREGIONS = 9997;
	protected static final Object[] EMPTY = new Object[0];
	private List<SmartCollectionImpl> albums;
	private Collection<SmartCollectionImpl> selectedAlbums;
	private TableViewer viewer;
	private String assignedAlbum;
	private Map<String, Image> faces = new HashMap<>();
	private boolean deleteRegion = false;
	protected boolean cntrlDwn;
	private boolean deleteAllRegions;
	private int regionCount;

	public SetPersonDialog(Shell parentShell, String assignedAlbum, String assetId) {
		super(parentShell);
		this.assignedAlbum = assignedAlbum;
		regionCount = Core.getCore().getDbManager().obtainObjects(RegionImpl.class, "asset_person_parent", //$NON-NLS-1$
				assetId, QueryField.EQUALS).size();
	}

	@Override
	public void create() {
		super.create();
		getShell().setText(Constants.APPLICATION_NAME);
		setTitle(Messages.AlbumSelectionDialog_assign_person);
		setMessage(Messages.AlbumSelectionDialog_select_person);
		fillValues(false);
		validate();
	}

	private void validate() {
		String errorMessage = null;
		if (viewer.getSelection().isEmpty())
			errorMessage = Messages.AlbumSelectionDialog_select_one;
		setErrorMessage(errorMessage);
		getButton(OK).setEnabled(errorMessage == null);
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout());
		composite.setLayout(new GridLayout(2, false));
		viewer = new TableViewer(composite, SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.SINGLE | SWT.VIRTUAL);
		TableViewerColumn col1 = new TableViewerColumn(viewer, SWT.NONE);
		col1.getColumn().setWidth(200);
		col1.getColumn().setText(Messages.AlbumSelectionDialog_name);
		col1.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof SmartCollectionImpl)
					return ((SmartCollectionImpl) element).getName();
				return element.toString();
			}

			public Image getImage(Object element) {
				if (element instanceof SmartCollectionImpl) {
					String id = ((SmartCollectionImpl) element).getStringId();
					Image face = faces.get(id);
					if (face == null)
						faces.put(id, face = UiUtilities.getFace(getShell().getDisplay(), (SmartCollectionImpl) element,
								24, 4, parent.getBackground()));
					return face;
				}
				return null;
			}

			@Override
			protected Rectangle getIconBounds() {
				return Icons.person64.getImage().getBounds();
			}
		});
		TableViewerColumn col2 = new TableViewerColumn(viewer, SWT.NONE);
		col2.getColumn().setWidth(250);
		col2.getColumn().setText(Messages.AlbumSelectionDialog_description);
		col2.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof SmartCollectionImpl)
					return ((SmartCollectionImpl) element).getDescription();
				return element.toString();
			}
		});
		viewer.setContentProvider(new DeferredContentProvider(new Comparator<SmartCollection>() {
			@Override
			public int compare(SmartCollection e1, SmartCollection e2) {
				return e1.getName().compareTo(e2.getName());
			}
		}));
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.heightHint = 500;
		viewer.getControl().setLayoutData(layoutData);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (cntrlDwn) {
					SmartCollectionImpl sm = (SmartCollectionImpl) viewer.getStructuredSelection()
							.getFirstElement();
					if (sm != null) {
						CollectionEditDialog dialog = new CollectionEditDialog(getShell(), sm,
								Messages.AlbumSelectionDialog_edit_person,
								Messages.AlbumSelectionDialog_person_album_msg, false, true, false, false);
						if (dialog.open() == Window.OK) {
							final SmartCollectionImpl album = dialog.getResult();
							if (album != null) {
								Set<Object> toBeDeleted = new HashSet<Object>();
								List<Object> toBeStored = new ArrayList<Object>();
								Utilities.updateCollection(dbManager, sm, album, toBeDeleted, toBeStored);
								dbManager.safeTransaction(toBeDeleted, toBeStored);
							}
							viewer.update(sm, null);
						}
					}
					cntrlDwn = false;
				}
				validate();
			}
		});
		viewer.getControl().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CTRL)
					cntrlDwn = true;
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.CTRL)
					cntrlDwn = false;
			}
		});
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, DELETEREGION, Messages.AlbumSelectionDialog_delete_region, false);
		if (regionCount > 1)
			createButton(parent, DELETEALLREGIONS, Messages.SetPersonDialog_delete_all, false);
		if (Core.getCore().getDbManager().exists(GroupImpl.class, Constants.GROUP_ID_PERSONS))
			createButton(parent, NEWALBUM, Messages.AlbumSelectionDialog_new_album, false);
		super.createButtonsForButtonBar(parent);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == DELETEREGION) {
			deleteRegion = true;
			okPressed();
			return;
		}
		if (buttonId == DELETEALLREGIONS) {
			if (AcousticMessageDialog.openQuestion(getShell(), Messages.SetPersonDialog_delete_all_regions,
					Messages.SetPersonDialog_this_will_remove)) {
				deleteAllRegions = true;
				okPressed();
			}
			return;
		}
		if (buttonId == NEWALBUM) {
			CollectionEditDialog dialog = new CollectionEditDialog(getShell(), null,
					Messages.AlbumSelectionDialog_create_person, Messages.AlbumSelectionDialog_specify_person_name,
					false, true, true, false);
			if (dialog.open() == Window.OK) {
				final SmartCollectionImpl album = dialog.getResult();
				if (album != null) {
					Group group = dbManager.obtainById(GroupImpl.class, Constants.GROUP_ID_PERSONS);
					album.addSortCriterion(new SortCriterionImpl(QueryField.IPTC_DATECREATED.getKey(), null, true));
					group.addRootCollection(album.getStringId());
					album.setGroup_rootCollection_parent(Constants.GROUP_ID_PERSONS);
					Collection<Object> toBeStored = Utilities.storeCollection(album, true, null);
					toBeStored.add(group);
					dbManager.safeTransaction(null, toBeStored);
					fillValues(true);
					viewer.setSelection(new StructuredSelection(album), true);
					validate();
					CoreActivator.getDefault().fireStructureModified();
				}
			}
			return;
		}
		super.buttonPressed(buttonId);
	}

	@Override
	protected void okPressed() {
		SmartCollectionImpl sm = (SmartCollectionImpl) viewer.getStructuredSelection().getFirstElement();
		if (sm != null)
			selectedAlbums = Collections.singletonList(sm);
		super.okPressed();
	}

	private void fillValues(boolean keep) {
		albums = new ArrayList<SmartCollectionImpl>(
				dbManager.<SmartCollectionImpl>obtainObjects(SmartCollectionImpl.class, false, "album", true, //$NON-NLS-1$
						QueryField.EQUALS, "system", true, //$NON-NLS-1$
						QueryField.EQUALS));
		IAssetProvider assetProvider = Core.getCore().getAssetProvider();
		if (assetProvider != null)
			albums.remove(assetProvider.getCurrentCollection());
		IStructuredSelection selection = viewer.getStructuredSelection();
		SetModel model = new SetModel();
		model.addAll(albums);
		viewer.setInput(model);
		viewer.getControl().getDisplay().timerExec(500, new Runnable() {
			@Override
			public void run() {
				if (!viewer.getControl().isDisposed()) {
					if (keep)
						viewer.setSelection(selection);
					else if (assignedAlbum != null)
						for (SmartCollectionImpl sm : albums)
							if (sm.getStringId().equals(assignedAlbum)) {
								viewer.setSelection(new StructuredSelection(sm), true);
								break;
							}
				}

			}
		});
	}

	public Collection<SmartCollectionImpl> getResult() {
		return selectedAlbums;
	}

	@Override
	public boolean close() {
		for (Image face : faces.values())
			face.dispose();
		faces.clear();
		return super.close();
	}

	public boolean isDeleteRegion() {
		return deleteRegion;
	}

	public boolean isDeleteAllRegions() {
		return deleteAllRegions;
	}

}
