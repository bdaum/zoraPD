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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.PasteMetadataOperation;
import com.bdaum.zoom.operations.internal.xmp.XMPField;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.ZViewerComparator;
import com.bdaum.zoom.ui.internal.widgets.ExpandCollapseGroup;

@SuppressWarnings("restriction")
public class PasteMetaDialog extends ZTitleAreaDialog {

	private static final String SETTINGSID = "com.bdaum.zoom.pasteMetaDialog"; //$NON-NLS-1$
	private static final String SELECTION = "selection"; //$NON-NLS-1$
	private CheckboxTreeViewer viewer;
	private final List<XMPField> values;
	private HashSet<QueryField> filter;
	private final IAdaptable info;
	private final List<Asset> selectedAssets;
	private IDialogSettings settings;
	private Set<String> selectionToKeep = new HashSet<String>();
	private MetadataOptionGroup metadataOptionGroup;

	public PasteMetaDialog(Shell parentShell, List<Asset> selectedAssets,
			List<XMPField> values, IAdaptable info) {
		super(parentShell, HelpContextIds.PASTEMETA_DIALOG);
		this.selectedAssets = selectedAssets;
		this.values = values;
		this.info = info;
		filter = new HashSet<QueryField>();
		for (XMPField field : values) {
			QueryField qfield = field.translateFields();
			while (qfield != null) {
				filter.add(qfield);
				qfield = qfield.getParent();
			}
		}
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.PasteMetaDialog_paste_metadata);
		setMessage(Messages.PasteMetaDialog_select_the_metadata_to_be_pasted);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout());
		viewer = createViewerGroup(composite);
		metadataOptionGroup = new MetadataOptionGroup(composite, false);
		viewer.expandAll();
		fillValues();
		return area;
	}

	private ContainerCheckedTreeViewer createViewerGroup(Composite comp) {
		ExpandCollapseGroup expandCollapseGroup = new ExpandCollapseGroup(comp,
				SWT.NONE);
		final ContainerCheckedTreeViewer treeViewer = new ContainerCheckedTreeViewer(comp,
				SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		expandCollapseGroup.setViewer(treeViewer);
		treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		treeViewer.setLabelProvider(new MetadataLabelProvider());
		treeViewer.setContentProvider(new MetadataContentProvider());
		treeViewer.setComparator(ZViewerComparator.INSTANCE);
		treeViewer.setFilters(new ViewerFilter[] { new ViewerFilter() {
			@Override
			public boolean select(Viewer aViewer, Object parentElement,
					Object element) {
				if (element instanceof QueryField)
					return filter.contains(element);
				return true;
			}
		} });
		treeViewer.setInput(QueryField.ALL);
		UiUtilities.installDoubleClickExpansion(treeViewer);
		return treeViewer;
	}

	@Override
	protected void okPressed() {
		Set<XMPField> selectedFields = new HashSet<XMPField>(values.size());
		for (XMPField field : values) {
			QueryField qfield = field.translateIdFields();
			if (viewer.getChecked(qfield)) {
				selectedFields.add(field);
				selectionToKeep.add(qfield.getKey());
			} else {
				QueryField parent = field.translateFields();
				if (viewer.getChecked(parent)) {
					selectedFields.add(field);
					selectionToKeep.add(parent.getKey());
				}
			}
		}
		PasteMetadataOperation op = new PasteMetadataOperation(selectedAssets,
				selectedFields, metadataOptionGroup.getMode());
		OperationJob.executeOperation(op, info);
		metadataOptionGroup.saveSettings(settings);
		settings.put(SELECTION, Core.toStringList(selectionToKeep, '\n'));
		super.okPressed();
	}

	private void fillValues() {
		settings = getDialogSettings(UiActivator.getDefault(), SETTINGSID);
		metadataOptionGroup.fillValues(settings);
		String selection = settings.get(SELECTION);
		if (selection != null) {
			StringTokenizer st = new StringTokenizer(selection, "\n"); //$NON-NLS-1$
			while (st.hasMoreTokens()) {
				String id = st.nextToken();
				QueryField qfield = QueryField.findQueryField(id);
				if (filter.contains(qfield))
					viewer.setChecked(qfield, true);
				else
					selectionToKeep.add(id);
			}
		} else
			viewer.setCheckedElements(filter.toArray());
	}
}
