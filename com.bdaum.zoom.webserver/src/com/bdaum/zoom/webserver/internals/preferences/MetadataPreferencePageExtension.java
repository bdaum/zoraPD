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
 * (c) 2019 Berthold Daum  
 */
package com.bdaum.zoom.webserver.internals.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;

import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.ui.internal.dialogs.MetadataLabelProvider;
import com.bdaum.zoom.ui.internal.preferences.MetadataPreferencePage;
import com.bdaum.zoom.ui.preferences.AbstractPreferencePage;
import com.bdaum.zoom.ui.preferences.AbstractPreferencePagePart;
import com.bdaum.zoom.webserver.PreferenceConstants;
import com.bdaum.zoom.webserver.internals.WebserverActivator;

@SuppressWarnings("restriction")
public class MetadataPreferencePageExtension extends AbstractPreferencePagePart {

	private AbstractPreferencePage parentPage;
	private ContainerCheckedTreeViewer webMetatadataViewer;
	private Composite composite;
	private IPreferenceStore preferenceStore;

	@Override
	public Control createPageContents(Composite parent, AbstractPreferencePage parentPage) {
		this.parentPage = parentPage;
		preferenceStore = WebserverActivator.getDefault().getPreferenceStore();
		composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, false));
		ViewerFilter viewerFilter = new ViewerFilter() {
			@Override
			public boolean select(Viewer aViewer, Object parentElement, Object element) {
				if (element instanceof QueryField) {
					QueryField qfield = (QueryField) element;
					return qfield != QueryField.TRACK && (qfield.hasChildren() || qfield.isUiField());
				}
				return false;
			}
		};
		Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
		label.setText(Messages.MetadataPreferencePageExtension_made_visible);
		webMetatadataViewer = MetadataPreferencePage.createViewerGroup(composite, viewerFilter, new MetadataLabelProvider(), null);
		return composite;
	}

	@Override
	public void fillValues() {
		CTabItem tabitem = parentPage.getTabFolder().getSelection();
		if (tabitem.getControl() == composite) {
			String s = preferenceStore.getString(PreferenceConstants.WEBMETADATA);
			MetadataPreferencePage.fillViewer(webMetatadataViewer, s, null, 2, false);
		}
	}

	@Override
	public void performDefaults() {
		MetadataPreferencePage.setDefaults(preferenceStore, webMetatadataViewer, PreferenceConstants.WEBMETADATA);
	}

	@Override
	public void performOk() {
		MetadataPreferencePage.saveValues(preferenceStore, webMetatadataViewer, PreferenceConstants.WEBMETADATA, null);
	}

	@Override
	public void performCancel() {
		// do nothing
	}
	
	@Override
	public String getLabel() {
		return Messages.MetadataPreferencePageExtension_web;
	}
	
	@Override
	public String getTooltip() {
		return Messages.MetadataPreferencePageExtension_web_tooltip;
	}

}
