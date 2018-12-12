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
package com.bdaum.zoom.gps.internal.dialogs;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.gps.geonames.IGeocodingService;
import com.bdaum.zoom.gps.internal.GpsActivator;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.widgets.CGroup;

public class SearchDetailDialog extends ZTitleAreaDialog {

	private Combo combo;
	private String[] serviceNames;
	private int dflt = -1;
	private String result;
	private String current;
	private StackLayout stackLayout;
	private Map<String, Control> stackMap = new HashMap<>(5);
	private CGroup parmGroup;

	public SearchDetailDialog(Shell parentShell, String current) {
		super(parentShell);
		this.current = current;
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.SearchDetailDialog_loc_search_config);
		setMessage(Messages.SearchDetailDialog_select_service_provider);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, false));
		new Label(composite, SWT.NONE).setText(Messages.SearchDetailDialog_service);
		combo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		IGeocodingService[] namingServices = GpsActivator.getDefault().getNamingServices();
		serviceNames = new String[namingServices.length];
		int select = -1;
		for (int i = 0; i < namingServices.length; i++) {
			serviceNames[i] = namingServices[i].getName();
			if (namingServices[i].isDefault())
				dflt = i;
			if (serviceNames[i].equals(current))
				select = i;
		}
		if (select < 0)
			select = dflt;
		combo.setItems(serviceNames);
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateStack(combo.getSelectionIndex());
			}
		});
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		data.widthHint = 500;
		data.heightHint = 250;
		parmGroup = new CGroup(composite, SWT.NONE);
		parmGroup.setLayoutData(data);
		parmGroup.setText(Messages.SearchDetailDialog_parameters);
		stackLayout = new StackLayout();
		parmGroup.setLayout(stackLayout);
		for (int i = 0; i < namingServices.length; i++)
			stackMap.put(namingServices[i].getName(), namingServices[i].createParameterGroup(parmGroup));
		combo.select(select);
		updateStack(select);
		return area;
	}

	public void updateStack(int index) {
		stackLayout.topControl = stackMap.get(serviceNames[index]);
		parmGroup.layout(true, true);
	}

	@Override
	protected void okPressed() {
		IGeocodingService[] namingServices = GpsActivator.getDefault().getNamingServices();
		for (int i = 0; i < namingServices.length; i++)
			namingServices[i].saveSearchParameters();
		result = combo.getText();
		super.okPressed();
	}

	public String getResult() {
		return result;
	}

}