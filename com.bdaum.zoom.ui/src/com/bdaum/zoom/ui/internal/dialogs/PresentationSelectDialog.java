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
 * (c) 2009 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.aoModeling.runtime.IIdentifiableObject;
import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitionImpl;
import com.bdaum.zoom.cat.model.group.slideShow.SlideShowImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebGalleryImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.ZViewerComparator;
import com.bdaum.zoom.ui.internal.widgets.ExpandCollapseGroup;

public class PresentationSelectDialog extends ZTitleAreaDialog {

	private static final String SLIDESHOWS = Messages.PresentationSelectDialog_slideshows;
	private static final String EXHIBITIONS = Messages.PresentationSelectDialog_exhibitions;
	private static final String WEBGALLERIES = Messages.PresentationSelectDialog_webgalleries;
	private static final String ALBUMS = Messages.PresentationSelectDialog_albums;
	private static final String PERSONS = Messages.PresentationSelectDialog_persons;
	private static final String USERDEFINED = Messages.PresentationSelectDialog_user_defined;
	private static final String[] ALL = new String[] { SLIDESHOWS, EXHIBITIONS, WEBGALLERIES, ALBUMS, PERSONS,
			USERDEFINED };
	protected static final List<IdentifiableObject> EMPTYOBJECTS = new ArrayList<IdentifiableObject>(0);
	private String targetType;
	private TreeViewer viewer;
	private IdentifiableObject selectedItem;

	public PresentationSelectDialog(Shell parentShell, String targetType) {
		super(parentShell);
		this.targetType = targetType;
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.PresentationSelectDialog_presentation_import);
		setMessage(NLS.bind(Messages.PresentationSelectDialog_please_select_a_presentation, targetType));
		updateButtons();
	}

	private void updateButtons() {
		getButton(IDialogConstants.OK_ID).setEnabled(selectedItem != null);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite comp = new Composite(area, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout());
		ExpandCollapseGroup expandCollapseGroup = new ExpandCollapseGroup(comp, SWT.NONE);
		viewer = new TreeViewer(comp, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		expandCollapseGroup.setViewer(viewer);
		GridData layoutData = new GridData(SWT.FILL, SWT.BEGINNING, true, true);
		layoutData.widthHint = 500;
		viewer.getControl().setLayoutData(layoutData);
		viewer.setContentProvider(new ITreeContentProvider() {

			public void inputChanged(Viewer aViewer, Object oldInput, Object newInput) {
				// do nothing
			}

			public void dispose() {
				// do nothing
			}

			public Object[] getElements(Object inputElement) {
				return (Object[]) inputElement;
			}

			public boolean hasChildren(Object element) {
				if (element instanceof String)
					return !obtainChildren(element).isEmpty();
				return false;
			}

			public Object getParent(Object element) {
				if (element instanceof SlideShowImpl)
					return SLIDESHOWS;
				if (element instanceof ExhibitionImpl)
					return EXHIBITIONS;
				if (element instanceof WebGalleryImpl)
					return WEBGALLERIES;
				if (element instanceof SmartCollectionImpl) {
					if (((SmartCollectionImpl) element).getAlbum())
						return ((SmartCollectionImpl) element).getSystem() ? PERSONS : ALBUMS;
					if (((SmartCollectionImpl) element).getSystem())
						return USERDEFINED;
				}
				return null;
			}

			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof String)
					return obtainChildren(parentElement).toArray();
				return null;
			}

			private List<IIdentifiableObject> obtainChildren(Object parentElement) {
				List<IIdentifiableObject> children = new ArrayList<IIdentifiableObject>();
				if (parentElement == SLIDESHOWS)
					children.addAll(dbManager.obtainObjects(SlideShowImpl.class));
				else if (parentElement == EXHIBITIONS)
					children.addAll(dbManager.obtainObjects(ExhibitionImpl.class));
				else if (parentElement == WEBGALLERIES)
					children.addAll(
							dbManager.obtainObjects(WebGalleryImpl.class, "template", false, QueryField.EQUALS)); //$NON-NLS-1$
				else if (parentElement == ALBUMS)
					children.addAll(dbManager.obtainObjects(SmartCollectionImpl.class, false, "album", true, //$NON-NLS-1$
							QueryField.EQUALS, "system", false, //$NON-NLS-1$
							QueryField.EQUALS));
				else if (parentElement == PERSONS)
					children.addAll(dbManager.obtainObjects(SmartCollectionImpl.class, false, "album", true, //$NON-NLS-1$
							QueryField.EQUALS, "system", true, //$NON-NLS-1$
							QueryField.EQUALS));
				else if (parentElement == USERDEFINED) {
					children.addAll(dbManager.obtainObjects(SmartCollectionImpl.class, false, "album", false, //$NON-NLS-1$
							QueryField.EQUALS, "system", false, //$NON-NLS-1$
							QueryField.EQUALS, "group_rootCollection_parent", //$NON-NLS-1$
							Constants.GROUP_ID_IMPORTS, QueryField.NOTEQUAL));
				}
				return children;
			}
		});
		viewer.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof SlideShowImpl)
					return ((SlideShowImpl) element).getName();
				if (element instanceof ExhibitionImpl)
					return ((ExhibitionImpl) element).getName();
				if (element instanceof WebGalleryImpl)
					return ((WebGalleryImpl) element).getName();
				if (element instanceof SmartCollectionImpl)
					return ((SmartCollectionImpl) element).getName();
				return element.toString();
			}
		});
		viewer.setComparator(ZViewerComparator.INSTANCE);
		UiUtilities.installDoubleClickExpansion(viewer);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				Object firstElement = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
				if (firstElement instanceof IdentifiableObject)
					selectedItem = (IdentifiableObject) firstElement;
				updateButtons();
			}
		});
		viewer.setInput(ALL);
		return area;
	}

	public IdentifiableObject getResult() {
		return selectedItem;
	}

}
