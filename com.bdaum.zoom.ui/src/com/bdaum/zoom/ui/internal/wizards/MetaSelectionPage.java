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

package com.bdaum.zoom.ui.internal.wizards;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.ZViewerComparator;
import com.bdaum.zoom.ui.internal.dialogs.MetadataContentProvider;
import com.bdaum.zoom.ui.internal.dialogs.MetadataLabelProvider;
import com.bdaum.zoom.ui.internal.dialogs.MetadataOptionGroup;
import com.bdaum.zoom.ui.internal.widgets.ExpandCollapseGroup;
import com.bdaum.zoom.ui.internal.widgets.JpegMetaGroup;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;
import com.bdaum.zoom.ui.wizards.ColoredWizardPage;

public class MetaSelectionPage extends ColoredWizardPage {

	private static final Object[] EMPTY = new Object[0];
	private static final String JPEGMETA = "jpegmeta"; //$NON-NLS-1$
	private ContainerCheckedTreeViewer viewer;
	private final Object rootElements;
	private MetadataOptionGroup metadataOptionGroup;
	private final boolean options;
	private final boolean jpeg;
	private JpegMetaGroup jpegGroup;
	private ViewerFilter filter;

	public MetaSelectionPage(Object rootElements, boolean options,
			ViewerFilter filter, boolean jpeg) {
		super(Messages.MetaSelectionPage_Field_selection);
		this.rootElements = rootElements;
		this.options = options;
		this.filter = filter;
		this.jpeg = jpeg;
	}

	@SuppressWarnings("unused")
	@Override
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 1);
		createViewer(composite);
		fillViewer();
		if (options) {
			metadataOptionGroup = new MetadataOptionGroup(composite, true);
			metadataOptionGroup.fillValues(getDialogSettings());
		}
		if (jpeg) {
			jpegGroup = new JpegMetaGroup(composite, SWT.NONE);
			IDialogSettings dialogSettings = getDialogSettings();
			String s = dialogSettings.get(JPEGMETA);
			jpegGroup.setSelection(s != null ? Boolean.parseBoolean(s)
					: UiActivator.getDefault().getPreferenceStore()
							.getBoolean(PreferenceConstants.JPEGMETADATA));
		}
		new Label(composite, SWT.NONE);
		setControl(composite);
		setHelp(HelpContextIds.EXPORTFOLDER_WIZARD);
		setTitle(Messages.MetaSelectionPage_metadata);
		setMessage(Messages.MetaSelectionPage_select_the_metadata);
		super.createControl(parent);
	}

	@SuppressWarnings("unused")
	private void createViewer(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout(2, false));
		ExpandCollapseGroup expandCollapseGroup = new ExpandCollapseGroup(comp,
				SWT.NONE);
		new Label(comp, SWT.NONE);
		viewer = new ContainerCheckedTreeViewer(comp, SWT.SINGLE | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.BORDER);
		expandCollapseGroup.setViewer(viewer);
		Tree tree = viewer.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer.setLabelProvider(new MetadataLabelProvider());
		viewer.setContentProvider(new MetadataContentProvider());
		viewer.setFilters(new ViewerFilter[] { filter });
		viewer.setComparator(ZViewerComparator.INSTANCE);
		viewer.setInput(rootElements);
		viewer.expandToLevel(2);
		UiUtilities.installDoubleClickExpansion(viewer);
		Composite buttonBar = new Composite(comp, SWT.NONE);
		buttonBar.setLayoutData(new GridData(SWT.END, SWT.FILL, false, true));
		buttonBar.setLayout(new GridLayout(1, false));
		Button selectAllButton = new Button(buttonBar, SWT.PUSH);
		selectAllButton.setText(Messages.MetaSelectionPage_select_all);
		selectAllButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				viewer.setGrayedElements(EMPTY);
				viewer.setCheckedElements(QueryField.getQueryFields().toArray());
			}
		});
		Button selectNoneButton = new Button(buttonBar, SWT.PUSH);
		selectNoneButton.setText(Messages.MetaSelectionPage_select_none);
		selectNoneButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				viewer.setCheckedElements(EMPTY);
			}
		});
	}

	private void fillViewer() {
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			String ess = settings.get(ExportFolderWizard.SELECTEDFIELDS);
			if (ess == null)
				ess = UiActivator.getDefault().getPreferenceStore()
						.getString(PreferenceConstants.EXPORTMETADATA);
			if (ess != null) {
				List<QueryField> fields = new ArrayList<QueryField>(100);
				StringTokenizer st = new StringTokenizer(ess, "\n"); //$NON-NLS-1$
				while (st.hasMoreTokens()) {
					QueryField qfield = QueryField.findQueryField(st
							.nextToken());
					if (qfield != null)
						fields.add(qfield);
				}
			}
		}
	}

	private void saveSettings(Set<QueryField> filt) {
		StringBuilder sb = new StringBuilder();
		Object[] checkedElements = viewer.getCheckedElements();
		for (Object object : checkedElements)
			if (object instanceof QueryField) {
				QueryField queryField = (QueryField) object;
				String id = queryField.getId();
				if (id != null && queryField.getChildren().length == 0) {
					if (sb.length() > 0)
						sb.append('\n');
					sb.append(id);
					filt.add(queryField);
				}
			}
		IDialogSettings settings = getDialogSettings();
		settings.put(ExportFolderWizard.SELECTEDFIELDS, sb.toString());
		if (metadataOptionGroup != null)
			metadataOptionGroup.saveSettings(settings);
		if (jpegGroup != null)
			settings.put(JPEGMETA, jpegGroup.getSelection());
	}

	public Set<QueryField> getFilter() {
		Set<QueryField> filter = new HashSet<QueryField>();
		saveSettings(filter);
		return filter;
	}

	public int getOptions() {
		return (metadataOptionGroup != null) ? metadataOptionGroup.getMode()
				: Constants.MERGE;
	}

	@Override
	protected String validate() {
		return null;
	}

	public boolean isJpegSet() {
		return jpegGroup == null ? false : jpegGroup.getSelection();
	}
}
