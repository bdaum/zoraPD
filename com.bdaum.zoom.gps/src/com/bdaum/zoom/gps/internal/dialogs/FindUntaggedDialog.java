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

package com.bdaum.zoom.gps.internal.dialogs;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.cat.model.group.CriterionImpl;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.SortCriterionImpl;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.dialogs.FindWithinGroup;

@SuppressWarnings("restriction")
public class FindUntaggedDialog extends ZTitleAreaDialog {

	private static final String SETTINGSID = "com.bdaum.zoom.findUntaggedDialog"; //$NON-NLS-1$
	private FindWithinGroup findWithinGroup;
	private IDialogSettings settings;
	private SmartCollectionImpl coll;

	public FindUntaggedDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.FindUntaggedDialog_find_untagged);
		setMessage(Messages.FindUntaggedDialog_find_untagged_message);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		final Composite comp = new Composite(area, SWT.NONE);
		final GridLayout gridLayout = new GridLayout();
		comp.setLayout(gridLayout);
		findWithinGroup = new FindWithinGroup(comp);
		fillValues();
		return area;
	}

	private void fillValues() {
		settings = Ui.getUi().getDialogSettings(SETTINGSID);
		findWithinGroup.fillValues(settings);
	}

	@Override
	protected void okPressed() {
		coll = new SmartCollectionImpl(
				Messages.FindUntaggedDialog_untagged_images, false, false,
				false, true, null, 0, null, 0, null, null);
		coll.addCriterion(new CriterionImpl(QueryField.EXIF_GPSLATITUDE
				.getKey(), null, Double.NaN, QueryField.UNDEFINED, false));
		coll.addCriterion(new CriterionImpl(QueryField.EXIF_GPSLONGITUDE
				.getKey(), null, Double.NaN, QueryField.UNDEFINED, false));
		coll.addSortCriterion(new SortCriterionImpl(
				QueryField.EXIF_DATETIMEORIGINAL.getKey(),  null, false));
		coll.setSmartCollection_subSelection_parent(findWithinGroup
				.getParentCollection());
		findWithinGroup.saveValues(settings);
		super.okPressed();
	}

	public SmartCollectionImpl getResult() {
		return coll;
	}

}
