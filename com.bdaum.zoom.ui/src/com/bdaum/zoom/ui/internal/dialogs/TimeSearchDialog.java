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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.cat.model.group.CriterionImpl;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.SortCriterionImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.Range;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.widgets.RadioButtonGroup;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.bdaum.zoom.ui.widgets.DateInput;

public class TimeSearchDialog extends ZTitleAreaDialog {

	private static final SimpleDateFormat df = new SimpleDateFormat(Messages.TimeSearchDialog_dateFormat);
	private static final String SETTINGSID = "com.bdaum.zoom.timeSearchDialog"; //$NON-NLS-1$

	private static final long ONEHOUR = 3600000L;
	private static final String INTERVAL = "interval"; //$NON-NLS-1$
	private Date date1;
	private Date date2;
	private SmartCollectionImpl coll;
	private IDialogSettings settings;
	private FindWithinGroup findWithinGroup;
	private DateInput fromField;
	private DateInput toField;
	private FindInNetworkGroup findInNetworkGroup;
	private RadioButtonGroup intervalRadioGroup;

	public TimeSearchDialog(Shell parentShell, Date date1, Date date2) {
		super(parentShell, HelpContextIds.TIMESEARCH_DIALOG);
		this.date1 = date1;
		this.date2 = date2;
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.TimeSearchDialog_time_search);
		setMessage(date1.equals(date2) ? NLS.bind(Messages.TimeSearchDialog_search_for_all, df.format(date1))
				: NLS.bind(Messages.TimeSearchDialog_search_for_all_images_between, df.format(date1),
						df.format(date2)));
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);

		final Composite comp = new Composite(area, SWT.NONE);
		final GridLayout gridLayout = new GridLayout();
		comp.setLayout(gridLayout);
		CGroup timeGroup = new CGroup(comp, SWT.NONE);
		timeGroup.setText(Messages.TimeSearchDialog_base_interval);
		GridData layoutData = new GridData(SWT.FILL, SWT.BEGINNING, true, true);
		layoutData.widthHint = 200;
		timeGroup.setLayoutData(layoutData);
		timeGroup.setLayout(new GridLayout(2, false));
		new Label(timeGroup, SWT.NONE).setText(Messages.TimeSearchDialog_from);
		fromField = new DateInput(timeGroup, SWT.DATE | SWT.TIME | SWT.DROP_DOWN | SWT.BORDER);
		fromField.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
		fromField.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Date to = toField.getDate();
				if (fromField.getDate().compareTo(to) > 0)
					fromField.setDate(new Date(to.getTime()));
			}
		});
		new Label(timeGroup, SWT.NONE).setText(Messages.TimeSearchDialog_to);
		toField = new DateInput(timeGroup, SWT.DATE | SWT.TIME | SWT.DROP_DOWN | SWT.BORDER);
		toField.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
		toField.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Date from = fromField.getDate();
				if (from.compareTo(toField.getDate()) > 0)
					toField.setDate(new Date(from.getTime()));
			}
		});
		findWithinGroup = new FindWithinGroup(comp);
		if (Core.getCore().isNetworked())
			findInNetworkGroup = new FindInNetworkGroup(comp);
		final CGroup intervalComp = new CGroup(comp, SWT.NONE);
		layoutData = new GridData(SWT.FILL, SWT.BEGINNING, true, true);
		layoutData.widthHint = 200;
		intervalComp.setLayoutData(layoutData);
		intervalComp.setText(Messages.TimeSearchDialog_derived_interval);
		intervalComp.setLayout(new GridLayout(2, false));
		intervalRadioGroup = new RadioButtonGroup(intervalComp, null, SWT.VERTICAL,
				Messages.TimeSearchDialog_exactly_same_time, Messages.TimeSearchDialog_within_same_hour,
				Messages.TimeSearchDialog_within_4_hours, Messages.TimeSearchDialog_same_day,
				Messages.TimeSearchDialog_same_week, Messages.TimeSearchDialog_same_month,
				Messages.TimeSearchDialog_same_year);
		fillValues();
		return area;
	}

	private void fillValues() {
		settings = getDialogSettings(UiActivator.getDefault(), SETTINGSID);
		try {
			intervalRadioGroup.setSelection(settings.getInt(INTERVAL));
		} catch (NumberFormatException e) {
			intervalRadioGroup.setSelection(3);
		}
		findWithinGroup.fillValues(settings);
		if (findInNetworkGroup != null)
			findInNetworkGroup.fillValues(settings);
		fromField.setDate(date1);
		toField.setDate(date2);
	}

	@Override
	protected void okPressed() {
		date1 = fromField.getDate();
		date2 = toField.getDate();
		boolean network = findInNetworkGroup == null ? false : findInNetworkGroup.getSelection();
		coll = new SmartCollectionImpl(Messages.TimeSearchDialog_time_search, false, false, true, network, null, 0,
				null, 0, null, Constants.INHERIT_LABEL, null, 0, null);
		Date from, to;
		int buttonpressed = intervalRadioGroup.getSelection();
		switch (buttonpressed) {
		case 1:
			from = new Date(date1.getTime() - ONEHOUR);
			to = new Date(date2.getTime() + ONEHOUR);
			break;
		case 2:
			from = new Date(date1.getTime() - 4 * ONEHOUR);
			to = new Date(date2.getTime() + 4 * ONEHOUR);
			break;
		case 3:
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(date1);
			cal.set(Calendar.HOUR, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			from = cal.getTime();
			cal.setTime(date2);
			cal.set(Calendar.HOUR, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			cal.add(Calendar.DAY_OF_MONTH, 1);
			cal.add(Calendar.MILLISECOND, -1);
			to = cal.getTime();
			break;
		case 4:
			cal = new GregorianCalendar();
			cal.setTime(date1);
			cal.set(Calendar.HOUR, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			int firstDayOfWeek = cal.getFirstDayOfWeek();
			int day = cal.get(Calendar.DAY_OF_WEEK);
			int diff = day - firstDayOfWeek;
			if (diff < 0)
				diff += 7;
			cal.add(Calendar.DAY_OF_MONTH, -diff);
			from = cal.getTime();
			cal.setTime(date2);
			cal.set(Calendar.HOUR, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			day = cal.get(Calendar.DAY_OF_WEEK);
			diff = day - firstDayOfWeek;
			if (diff < 0)
				diff += 7;
			cal.add(Calendar.DAY_OF_MONTH, -diff);
			cal.add(Calendar.DAY_OF_MONTH, 7);
			cal.add(Calendar.MILLISECOND, -1);
			to = cal.getTime();
			break;
		case 5:
			cal = new GregorianCalendar();
			cal.setTime(date1);
			cal.set(Calendar.HOUR, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			from = cal.getTime();
			cal.setTime(date2);
			cal.set(Calendar.HOUR, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			cal.add(Calendar.MONTH, 1);
			cal.add(Calendar.MILLISECOND, -1);
			to = cal.getTime();
			break;
		case 6:
			cal = new GregorianCalendar();
			cal.setTime(date1);
			cal.set(Calendar.HOUR, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			cal.set(Calendar.MONTH, Calendar.JANUARY);
			from = cal.getTime();
			cal.setTime(date2);
			cal.set(Calendar.HOUR, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			cal.set(Calendar.MONTH, Calendar.JANUARY);
			cal.add(Calendar.YEAR, 1);
			cal.add(Calendar.MILLISECOND, -1);
			to = cal.getTime();
			break;
		default:
			from = date1;
			to = date2;
		}
		int rel;
		Object value;
		if (from.equals(to)) {
			value = from;
			rel = QueryField.EQUALS;
		} else {
			value = new Range(from, to);
			rel = QueryField.BETWEEN;
		}
		coll.addCriterion(new CriterionImpl(QueryField.IPTC_DATECREATED.getKey(), null, value, rel, false));
		coll.addSortCriterion(new SortCriterionImpl(QueryField.IPTC_DATECREATED.getKey(), null, false));
		coll.setSmartCollection_subSelection_parent(findWithinGroup.getParentCollection());
		settings.put(INTERVAL, buttonpressed);
		findWithinGroup.saveValues(settings);
		if (findInNetworkGroup != null)
			findInNetworkGroup.saveValues(settings);
		super.okPressed();
	}

	public SmartCollectionImpl getResult() {
		return coll;
	}

}
