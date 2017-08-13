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

import java.text.ParseException;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.cat.model.group.Criterion;
import com.bdaum.zoom.cat.model.group.CriterionImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.SortCriterionImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.widgets.NumericControl;

public class ProximityEditDialog extends ZTitleAreaDialog {

	private final SmartCollection coll;
	private NumericControl distanceField;
	private Text latField;
	private Text lonField;
	private ModifyListener modifyListener = new ModifyListener() {

		public void modifyText(ModifyEvent e) {
			updateButtons();
		}
	};
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
			Format.latitudeFormatter.fromString(latField.getText());
		} catch (ParseException e) {
			setErrorMessage(e.getMessage());
			return false;
		}
		try {
			Format.longitudeFormatter.fromString(lonField.getText());
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
		final Label latLabel = new Label(comp, SWT.NONE);
		latLabel.setText(Messages.ProximityEditDialog_latitude);
		latField = new Text(comp, SWT.BORDER);
		latField.setLayoutData(new GridData(80, SWT.DEFAULT));
		latField.addModifyListener(modifyListener);
		final Label lonLabel = new Label(comp, SWT.NONE);
		lonLabel.setText(Messages.ProximityEditDialog_longitude);
		lonField = new Text(comp, SWT.BORDER);
		lonField.setLayoutData(new GridData(80, SWT.DEFAULT));
		lonField.addModifyListener(modifyListener);
		final Label distancekmLabel = new Label(comp, SWT.NONE);
		distancekmLabel.setText(Messages.ProximityEditDialog_distance);
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
		String lat = Format.latitudeFormatter.toString(values[0]);
		latField.setText(lat);
		String lon = Format.longitudeFormatter.toString(values[0]);
		lonField.setText(lon);
	}

	@Override
	protected void okPressed() {
		Double lat;
		try {
			lat = (Double) Format.latitudeFormatter.fromString(latField
					.getText());
		} catch (ParseException e) {
			lat = Double.NaN;
		}
		Double lon;
		try {
			lon = (Double) Format.longitudeFormatter.fromString(lonField
					.getText());
		} catch (ParseException e) {
			lon = Double.NaN;
		}
		Double[] values = new Double[] { lat, lon,
				distanceField.getSelection() / 1000d };
		boolean network = findInNetworkGroup == null ? false
				: findInNetworkGroup.getSelection();
		collection = new SmartCollectionImpl(coll.getName(), false, false,
				coll.getAdhoc(), network, null, 0, null, 0, null, null);
		CriterionImpl crit = new CriterionImpl(
				QueryField.EXIF_GPSLOCATIONDISTANCE.getKey(), null, values,
				QueryField.NOTGREATER, false);
		collection.addCriterion(crit);
		SortCriterionImpl sort = new SortCriterionImpl(
				QueryField.EXIF_GPSLOCATIONDISTANCE.getKey(),  null, false);
		collection.addSortCriterion(sort);
		super.okPressed();
	}

	public SmartCollectionImpl getResult() {
		return collection;
	}

}
