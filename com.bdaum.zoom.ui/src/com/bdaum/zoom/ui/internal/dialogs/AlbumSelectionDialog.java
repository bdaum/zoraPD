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
 * (c) 2016 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

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
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.ZViewerComparator;

@SuppressWarnings("restriction")
public class AlbumSelectionDialog extends ZTitleAreaDialog {

	private static final int NEWALBUM = 9999;
	private static final int DELETEREGION = 9998;
	protected static final Object[] EMPTY = new Object[0];
	private List<SmartCollectionImpl> albums;
	private Collection<SmartCollectionImpl> selectedAlbums;
	private CheckboxTreeViewer viewer;
	private boolean small;
	private List<String> assignedAlbums;
	private boolean deleteRegion = false;
	protected boolean cntrlDwn;
	private SmartCollectionImpl[] preselection;

	public AlbumSelectionDialog(Shell parentShell, boolean small, List<String> assignedAlbums, SmartCollectionImpl[] preselection) {
		super(parentShell);
		this.small = small;
		this.assignedAlbums = assignedAlbums;
		this.preselection = preselection;
	}

	@Override
	public void create() {
		super.create();
		getShell().setText(Constants.APPLICATION_NAME);
		setTitle(Messages.AlbumSelectionDialog_addToAlbum);
		setMessage(Messages.AlbumSelectionDialog_addToAlbum_tooltip);
		fillValues(false);
		validate();
	}

	private void validate() {
		String errorMessage = null;
		Object[] checkedElements = viewer.getCheckedElements();
		if (checkedElements.length == 0)
			errorMessage = Messages.AlbumSelectionDialog_select_at_least_one;
		setErrorMessage(errorMessage);
		getButton(OK).setEnabled(errorMessage == null);
	}

	@SuppressWarnings("unused")
	@Override
	protected Control createDialogArea(final Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout());
		composite.setLayout(new GridLayout(2, false));
		viewer = new CheckboxTreeViewer(composite, SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
		TreeViewerColumn col1 = new TreeViewerColumn(viewer, SWT.NONE);
		col1.getColumn().setWidth(200);
		col1.getColumn().setText(Messages.AlbumSelectionDialog_name);
		col1.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof SmartCollectionImpl)
					return ((SmartCollectionImpl) element).getName();
				return element.toString();
			}
		});
		if (!small) {
			TreeViewerColumn col2 = new TreeViewerColumn(viewer, SWT.NONE);
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
			viewer.getTree().setHeaderVisible(true);
		}
		viewer.setContentProvider(new ITreeContentProvider() {
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// do nothing
			}

			public void dispose() {
				// do nothing
			}

			public boolean hasChildren(Object element) {
				if (element instanceof SmartCollection)
					return !((SmartCollection) element).getSubSelection().isEmpty();
				return false;
			}

			public Object getParent(Object element) {
				if (element instanceof SmartCollection)
					return ((SmartCollection) element).getSmartCollection_subSelection_parent();
				return null;
			}

			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof List) {
					List<SmartCollection> rootalbums = new ArrayList<SmartCollection>();
					for (Object obj : (List<?>) inputElement)
						if (obj instanceof SmartCollection) {
							SmartCollection album = (SmartCollection) obj;
							if (album.getSmartCollection_subSelection_parent() == null)
								rootalbums.add(album);
						}
					return rootalbums.toArray();
				}
				return null;
			}

			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof SmartCollection)
					return ((SmartCollection) parentElement).getSubSelection().toArray();
				return EMPTY;
			}
		});
		UiUtilities.installDoubleClickExpansion(viewer);
		viewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				validate();
			}
		});
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.heightHint = small ? 150 : 400;
		viewer.getControl().setLayoutData(layoutData);
		viewer.setComparator(ZViewerComparator.INSTANCE);
		if (assignedAlbums != null && !assignedAlbums.isEmpty())
			viewer.setFilters(new ViewerFilter[] { new ViewerFilter() {
				@Override
				public boolean select(Viewer v, Object parentElement, Object element) {
					return !assignedAlbums.contains(((SmartCollectionImpl) element).getName());
				}
			} });
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (cntrlDwn) {
					SmartCollectionImpl sm = (SmartCollectionImpl) ((IStructuredSelection) viewer.getSelection())
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
		if (!small)
			new AllNoneGroup(composite, new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (e.widget.getData() == AllNoneGroup.ALL) {
						viewer.expandAll();
						viewer.setCheckedElements(albums.toArray());
					} else
						viewer.setCheckedElements(EMPTY);
					validate();
				}
			});
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		if (Core.getCore().getDbManager().obtainById(GroupImpl.class, Constants.GROUP_ID_USER) != null)
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
		if (buttonId == NEWALBUM) {
			CollectionEditDialog dialog = new CollectionEditDialog(getShell(), null,
					Messages.AlbumSelectionDialog_create_album, Messages.AlbumSelectionDialog_specify_name, false, true,
					false, false);
			if (dialog.open() == Window.OK) {
				final SmartCollectionImpl album = dialog.getResult();
				if (album != null) {
					final GroupImpl group = dbManager.obtainById(GroupImpl.class, Constants.GROUP_ID_USER);
					album.addSortCriterion(new SortCriterionImpl(QueryField.IPTC_DATECREATED.getKey(), null, true));
					group.addRootCollection(album.getStringId());
					album.setGroup_rootCollection_parent(group.getStringId());
					Collection<Object> toBeStored = Utilities.storeCollection(album, true, null);
					toBeStored.add(group);
					dbManager.safeTransaction(null, toBeStored);
					fillValues(true);
					viewer.setChecked(album, true);
					validate();
					CoreActivator.getDefault().fireStructureModified();
				}
			}
		} else
			super.buttonPressed(buttonId);
	}

	@Override
	protected void okPressed() {
		Object[] checkedElements = viewer.getCheckedElements();
		selectedAlbums = new ArrayList<SmartCollectionImpl>(checkedElements.length);
		for (Object object : checkedElements)
			selectedAlbums.add((SmartCollectionImpl) object);
		super.okPressed();
	}

	private void fillValues(boolean keep) {
		albums = new ArrayList<SmartCollectionImpl>(
				dbManager.<SmartCollectionImpl>obtainObjects(SmartCollectionImpl.class, false, "album", true, //$NON-NLS-1$
						QueryField.EQUALS, "system", false, //$NON-NLS-1$
						QueryField.EQUALS));
		IAssetProvider assetProvider = Core.getCore().getAssetProvider();
		if (assetProvider != null)
			albums.remove(assetProvider.getCurrentCollection());
		Object[] checkedElements = viewer.getCheckedElements();
		viewer.setInput(albums);
		((AbstractTreeViewer) viewer).expandAll();
		if (keep)
			viewer.setCheckedElements(checkedElements);
		else if (preselection != null)
			viewer.setCheckedElements(preselection);
	}

	public Collection<SmartCollectionImpl> getResult() {
		return selectedAlbums;
	}

	public boolean isDeleteRegion() {
		return deleteRegion;
	}

}
