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

package com.bdaum.zoom.ui.internal.dialogs;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.widgets.NumericControl;

public class ProximityDialog extends ZTitleAreaDialog {
	private static final String SETTINGSID = "com.bdaum.zoom.proximityProperties"; //$NON-NLS-1$

	private static final String DISTANCE = "distance"; //$NON-NLS-1$

	private NumericControl distanceField;
	private double distance = 1d;

	private FindWithinGroup findWithinGroup;

	private IDialogSettings settings;

	private SmartCollection parentCollection;

	private FindInNetworkGroup findInNetworkGroup;

	private boolean networked;

	public ProximityDialog(Shell parentShell) {
		super(parentShell);
	}

	public double getResult() {
		return distance;
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.ProximityDialog_proximity_search);
		setMessage(Messages.ProximityDialog_search_images_taken_in_vincinity);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);

		final Composite comp = new Composite(area, SWT.NONE);
		comp.setLayoutData(new GridData(250, SWT.DEFAULT));
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		comp.setLayout(gridLayout);
		findWithinGroup = new FindWithinGroup(comp);
		if (Core.getCore().isNetworked())
			findInNetworkGroup = new FindInNetworkGroup(comp);
		final Label distancekmLabel = new Label(comp, SWT.NONE);
		distancekmLabel.setText(Messages.ProximityDialog_distance);

		distanceField = new NumericControl(comp, SWT.NONE);
		distanceField.setDigits(3);
		distanceField.setIncrement(100);
		distanceField.setPageIncrement(1000);
		distanceField.setMinimum(1);
		distanceField.setMaximum(20000000);
		distanceField.setLogrithmic(true);
		fillValues();
		distanceField.setSelection((int) (distance * 1000));
		return area;
	}

	@Override
	protected void okPressed() {
		distance = distanceField.getSelection() / 1000d;
		settings.put(DISTANCE, distance);
		findWithinGroup.saveValues(settings);
		if (findInNetworkGroup != null) {
			networked = findInNetworkGroup.getSelection();
			findInNetworkGroup.saveValues(settings);
		} else
			networked = false;
		parentCollection = findWithinGroup.getParentCollection();
		super.okPressed();
	}

	private void fillValues() {
		settings = UiActivator.getDefault().getDialogSettings(SETTINGSID);
		try {
			distance = settings.getDouble(DISTANCE);
		} catch (NumberFormatException e) {
			// ignore
		}
		findWithinGroup.fillValues(settings);
		if (findInNetworkGroup != null)
			findInNetworkGroup.fillValues(settings);
	}

	public SmartCollection getParentCollection() {
		return parentCollection;
	}

	/**
	 * @return the networked
	 */
	public boolean isNetworked() {
		return networked;
	}
}
