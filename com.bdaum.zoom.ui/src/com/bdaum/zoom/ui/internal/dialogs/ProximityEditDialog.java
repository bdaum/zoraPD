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

import java.text.ParseException;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.cat.model.group.Criterion;
import com.bdaum.zoom.cat.model.group.CriterionImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.SortCriterionImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.widgets.NumericControl;

public class ProximityEditDialog extends ZTitleAreaDialog implements Listener {

	private final SmartCollection coll;
	private NumericControl distanceField;
	private Text latField;
	private Text lonField;
	private SmartCollectionImpl collection;
	private FindInNetworkGroup findInNetworkGroup;

	public ProximityEditDialog(Shell parentShell, SmartCollection coll) {
		super(parentShell);
		this.coll = coll;
	}

	protected void updateButtons() {
		boolean enabled = validate();
		getShell().setModified(enabled);
		getButton(IDialogConstants.OK_ID).setEnabled(enabled);
	}

	private boolean validate() {
		try {
			Format.latitudeFormatter.parse(latField.getText());
		} catch (ParseException e) {
			setErrorMessage(e.getMessage());
			return false;
		}
		try {
			Format.longitudeFormatter.parse(lonField.getText());
		} catch (ParseException e) {
			setErrorMessage(e.getMessage());
			return false;
		}
		setErrorMessage(null);
		return true;
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.ProximityEditDialog_edit_proximity_search);
		setMessage(Messages.ProximityEditDialog_search_images);
		fillValues();
		updateButtons();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);

		final Composite comp = new Composite(area, SWT.NONE);
		comp.setLayoutData(new GridData(250, SWT.DEFAULT));
		comp.setLayout(new GridLayout(2, false));
		if (Core.getCore().isNetworked()) {
			findInNetworkGroup = new FindInNetworkGroup(comp);
			if (coll != null)
				findInNetworkGroup.setSelection(coll.getNetwork());
		}
		new Label(comp, SWT.NONE).setText(Messages.ProximityEditDialog_latitude);
		latField = new Text(comp, SWT.BORDER);
		latField.setLayoutData(new GridData(80, SWT.DEFAULT));
		latField.addListener(SWT.Modify, this);
		new Label(comp, SWT.NONE).setText(Messages.ProximityEditDialog_longitude);
		lonField = new Text(comp, SWT.BORDER);
		lonField.setLayoutData(new GridData(80, SWT.DEFAULT));
		lonField.addListener(SWT.Modify, this);
		new Label(comp, SWT.NONE).setText(Messages.ProximityEditDialog_distance);
		distanceField = new NumericControl(comp, SWT.NONE);
		distanceField.setDigits(3);
		distanceField.setIncrement(100);
		distanceField.setPageIncrement(1000);
		distanceField.setMinimum(1);
		distanceField.setMaximum(20000000);
		return area;
	}

	private void fillValues() {
		Criterion criterion = coll.getCriterion(0);
		Double[] values = (Double[]) criterion.getValue();
		distanceField.setSelection((int) (values[2] * 1000));
		String lat = Format.latitudeFormatter.format(values[0]);
		latField.setText(lat);
		String lon = Format.longitudeFormatter.format(values[0]);
		lonField.setText(lon);
	}

	@Override
	protected void okPressed() {
		Double lat;
		try {
			lat = (Double) Format.latitudeFormatter.parse(latField.getText());
		} catch (ParseException e) {
			lat = Double.NaN;
		}
		Double lon;
		try {
			lon = (Double) Format.longitudeFormatter.parse(lonField.getText());
		} catch (ParseException e) {
			lon = Double.NaN;
		}
		Double[] values = new Double[] { lat, lon, distanceField.getSelection() / 1000d };
		boolean network = findInNetworkGroup == null ? false : findInNetworkGroup.getSelection();
		collection = new SmartCollectionImpl(coll.getName(), false, false, coll.getAdhoc(), network, null, 0, null, 0,
				null, Constants.INHERIT_LABEL, null, 0, 1, null);
		collection.addCriterion(new CriterionImpl(QueryField.EXIF_GPSLOCATIONDISTANCE.getKey(), null, values, null,
				QueryField.NOTGREATER, false));
		collection.addSortCriterion(new SortCriterionImpl(QueryField.EXIF_GPSLOCATIONDISTANCE.getKey(), null, false));
		super.okPressed();
	}

	public SmartCollectionImpl getResult() {
		return collection;
	}

	@Override
	public void handleEvent(Event event) {
		updateButtons();
	}

}
