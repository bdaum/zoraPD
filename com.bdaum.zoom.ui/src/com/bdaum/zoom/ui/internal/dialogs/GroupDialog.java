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
 * (c) 2009-2018 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.dialogs;

import java.util.GregorianCalendar;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.bdaum.zoom.cat.model.group.Group;
import com.bdaum.zoom.cat.model.group.GroupImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.preferences.AutoPreferencePage;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.LabelConfigGroup;
import com.bdaum.zoom.ui.internal.widgets.RadioButtonGroup;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.bdaum.zoom.ui.widgets.CLink;

public class GroupDialog extends ZTitleAreaDialog {

	private static final String TIMELINE = "T"; //$NON-NLS-1$
	private static final String LOCATIONS = "L"; //$NON-NLS-1$
	private static final String IMPORTS = "I"; //$NON-NLS-1$
	private static final String RATING = "R"; //$NON-NLS-1$
	private static final String AUTORULE = "A"; //$NON-NLS-1$
	private Group parent;
	private GroupImpl current;
	private Text nameField;
	private String name;
	private CheckboxButton overwriteButton;
	private AutoRuleComponent ruleComponent;
	private CLink link;
	private CheckboxButton notRatedButton;
	private CheckboxButton oneButton;
	private CheckboxButton twoButton;
	private CheckboxButton threeButton;
	private CheckboxButton fourButton;
	private CheckboxButton fiveButton;
	private RadioButtonGroup importButtons;
	private RadioButtonGroup monthsButtons;
	private RadioButtonGroup weeksButtons;
	private RadioButtonGroup daysButtons;
	private Spinner yearSpinner;
	private RadioButtonGroup locationButtons;
	private String annotations;
	private CTabFolder tabFolder;
	private LabelConfigGroup labelConfigGroup;
	private int showLabel;
	private String labelTemplate;

	public GroupDialog(Shell parentShell, final GroupImpl current, Group parent) {
		super(parentShell, HelpContextIds.GROUP_DIALOG);
		this.current = current;
		this.parent = parent;
	}

	@Override
	public void create() {
		super.create();
		setTitle(parent == null ? Messages.GroupDialog_Group
				: NLS.bind(Messages.GroupDialog_subgroup_of, parent.getName()));
		setMessage(Messages.GroupDialog_edit_group_properties);
		fillValues();
		updateTable();
		updateLink();
		validate();
	}

	private void fillValues() {
		if (current != null) {
			nameField.setText(current.getName());
			String annotations = current.getAnnotations();
			if (isAuto()) {
				if (annotations != null && annotations.startsWith(AUTORULE)) {
					overwriteButton.setSelection(true);
					ruleComponent.fillValues(annotations.substring(1));
				} else {
					overwriteButton.setSelection(false);
					ruleComponent.fillValues(""); //$NON-NLS-1$
				}
			} else if (isRating()) {
				if (annotations != null && annotations.startsWith(RATING)) {
					annotations += "tttttt"; //$NON-NLS-1$
					notRatedButton.setSelection(annotations.charAt(1) == 't');
					oneButton.setSelection(annotations.charAt(2) == 't');
					twoButton.setSelection(annotations.charAt(3) == 't');
					threeButton.setSelection(annotations.charAt(4) == 't');
					fourButton.setSelection(annotations.charAt(5) == 't');
					fiveButton.setSelection(annotations.charAt(6) == 't');
				} else {
					notRatedButton.setSelection(true);
					oneButton.setSelection(true);
					twoButton.setSelection(true);
					threeButton.setSelection(true);
					fourButton.setSelection(true);
					fiveButton.setSelection(true);
				}
			} else if (isImports()) {
				if (annotations != null && annotations.startsWith(IMPORTS)) {
					annotations += "3"; //$NON-NLS-1$
					importButtons.setSelection(annotations.charAt(1) & 3);
				} else
					importButtons.setSelection(3);
			} else if (isLocations()) {
				if (annotations != null && annotations.startsWith(LOCATIONS)) {
					annotations += "2"; //$NON-NLS-1$
					locationButtons.setSelection(annotations.charAt(1) & 3);
				} else
					locationButtons.setSelection(2);
			} else if (isTimeline()) {
				if (annotations != null && annotations.startsWith(TIMELINE)) {
					annotations += "2221900"; //$NON-NLS-1$
					daysButtons.setSelection(annotations.charAt(1) & 3);
					weeksButtons.setSelection(annotations.charAt(2) & 3);
					monthsButtons.setSelection(annotations.charAt(3) & 3);
					try {
						yearSpinner.setSelection(Integer.parseInt(annotations.substring(4, 8)));
					} catch (NumberFormatException e) {
						yearSpinner.setSelection(1900);
					}
				} else {
					daysButtons.setSelection(2);
					weeksButtons.setSelection(2);
					monthsButtons.setSelection(2);
					yearSpinner.setSelection(1900);
				}
			}
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		tabFolder = new CTabFolder(area, SWT.BORDER);
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
		tabFolder.setSimple(false);
		UiUtilities.createTabItem(tabFolder, Messages.GroupDialog_general, null).setControl(createGeneralGroup(tabFolder));
		if (isAuto() || isRating() || isImports() || isLocations() || isTimeline())
			UiUtilities
					.createTabItem(tabFolder,
							isAuto() ? Messages.GroupDialog_rules : Messages.GroupDialog_collectionFilter, null)
					.setControl(createFilterGroup(tabFolder));
		UiUtilities.createTabItem(tabFolder, Messages.GroupDialog_appearance, null)
				.setControl(createAppearanceGroup(tabFolder));
		return area;
	}

	private Control createAppearanceGroup(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout());

		labelConfigGroup = new LabelConfigGroup(composite, true);
		labelConfigGroup.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				validate();
			}
		});
		if (current != null)
			labelConfigGroup.setSelection(current.getShowLabel(), current.getLabelTemplate(), current.getFontSize());
		return composite;
	}

	private Control createFilterGroup(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout());
		GridLayout layout;
		if (isAuto()) {
			Composite autoArea = new Composite(composite, SWT.NONE);
			autoArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
			layout = new GridLayout(2, false);
			layout.marginWidth = 0;
			autoArea.setLayout(layout);
			overwriteButton = WidgetFactory.createCheckButton(autoArea, Messages.GroupDialog_overwrite, null);
			overwriteButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					updateTable();
					updateLink();
				}
			});
			link = new CLink(autoArea, SWT.NONE);
			link.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
			link.setText(Messages.GroupDialog_configure);
			link.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					PreferencesUtil.createPreferenceDialogOn(getShell(), AutoPreferencePage.ID, new String[0],
							AutoPreferencePage.RULES).open();
				}
			});
			ruleComponent = new AutoRuleComponent(autoArea, SWT.SHORT, this);
		} else if (isRating()) {
			CGroup ratingArea = new CGroup(composite, SWT.NONE);
			ratingArea.setText(Messages.GroupDialog_filter);
			ratingArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
			ratingArea.setLayout(new GridLayout());
			new Label(ratingArea, SWT.NONE).setText(Messages.GroupDialog_show_only);
			Composite ratingButtons = new Composite(ratingArea, SWT.NONE);
			GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
			layoutData.horizontalIndent = 15;
			ratingButtons.setLayoutData(layoutData);
			ratingButtons.setLayout(new GridLayout());

			notRatedButton = WidgetFactory.createCheckButton(ratingButtons, Messages.GroupDialog_not_rated, null);
			oneButton = WidgetFactory.createCheckButton(ratingButtons, "*", null); //$NON-NLS-1$
			twoButton = WidgetFactory.createCheckButton(ratingButtons, "**", null); //$NON-NLS-1$
			threeButton = WidgetFactory.createCheckButton(ratingButtons, "***", null); //$NON-NLS-1$
			fourButton = WidgetFactory.createCheckButton(ratingButtons, "****", null); //$NON-NLS-1$
			fiveButton = WidgetFactory.createCheckButton(ratingButtons, "*****", null); //$NON-NLS-1$

		} else if (isImports()) {
			CGroup importsArea = new CGroup(composite, SWT.NONE);
			importsArea.setText(Messages.GroupDialog_filter);
			importsArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
			importsArea.setLayout(new GridLayout(2, false));
			new Label(importsArea, SWT.NONE).setText(Messages.GroupDialog_older_than);
			importButtons = new RadioButtonGroup(importsArea, null, SWT.HORIZONTAL,
					new String[] { Messages.GroupDialog_one_m, Messages.GroupDialog_three_m, Messages.GroupDialog_one_y,
							Messages.GroupDialog_show_all });
		} else if (isLocations()) {
			CGroup locationsArea = new CGroup(composite, SWT.NONE);
			locationsArea.setText(Messages.GroupDialog_filter);
			locationsArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
			locationsArea.setLayout(new GridLayout(2, false));
			new Label(locationsArea, SWT.NONE).setText(Messages.GroupDialog_show);
			locationButtons = new RadioButtonGroup(locationsArea, null, SWT.HORIZONTAL, new String[] {
					Messages.GroupDialog_countries, Messages.GroupDialog_countries_states, Messages.GroupDialog_all });
		} else if (isTimeline()) {
			CGroup timelineArea = new CGroup(composite, SWT.NONE);
			timelineArea.setText(Messages.GroupDialog_filter);
			timelineArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
			timelineArea.setLayout(new GridLayout(1, false));
			daysButtons = new RadioButtonGroup(timelineArea, Messages.GroupDialog_days, SWT.HORIZONTAL, new String[] {
					Messages.GroupDialog_never, Messages.GroupDialog_current_y, Messages.GroupDialog_always });
			weeksButtons = new RadioButtonGroup(timelineArea, Messages.GroupDialog_weeks, SWT.HORIZONTAL, new String[] {
					Messages.GroupDialog_never, Messages.GroupDialog_current_y, Messages.GroupDialog_always });
			monthsButtons = new RadioButtonGroup(timelineArea, Messages.GroupDialog_month, SWT.HORIZONTAL,
					new String[] { Messages.GroupDialog_never, Messages.GroupDialog_current_y,
							Messages.GroupDialog_always });
			Composite yearComp = new Composite(timelineArea, SWT.NONE);
			yearComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			GridLayout glayout = new GridLayout(2, false);
			glayout.marginHeight = glayout.marginWidth = 0;
			yearComp.setLayout(glayout);
			new Label(yearComp, SWT.NONE).setText(Messages.GroupDialog_hide_years);
			yearSpinner = new Spinner(yearComp, SWT.BORDER);
			yearSpinner.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			GregorianCalendar cal = new GregorianCalendar();
			yearSpinner.setMaximum(cal.get(GregorianCalendar.YEAR));
			yearSpinner.setMinimum(1900);
			yearSpinner.setIncrement(1);
		}
		return composite;
	}

	private Control createGeneralGroup(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, false));
		Label label = new Label(composite, SWT.NONE);
		label.setText(Messages.GroupDialog_group_name);
		nameField = new Text(composite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		nameField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		nameField.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});
		return composite;
	}

	protected void updateLink() {
		if (link != null)
			link.setEnabled(!overwriteButton.getSelection());
	}

	protected void updateTable() {
		if (ruleComponent != null)
			ruleComponent.setEnabled(overwriteButton.getSelection());
	}

	private boolean isAuto() {
		return current != null && Constants.GROUP_ID_AUTO.equals(current.getStringId());
	}

	private boolean isRating() {
		return current != null && Constants.GROUP_ID_RATING.equals(current.getStringId());
	}

	private boolean isImports() {
		return current != null && Constants.GROUP_ID_RECENTIMPORTS.equals(current.getStringId());
	}

	private boolean isLocations() {
		return current != null && Constants.GROUP_ID_LOCATIONS.equals(current.getStringId());
	}

	private boolean isTimeline() {
		return current != null && Constants.GROUP_ID_TIMELINE.equals(current.getStringId());
	}

	private void validate() {
		String errorMessage = null;
		String newText = nameField.getText();
		if (newText.isEmpty())
			errorMessage = Messages.GroupDialog_specify_name;
		else
			for (GroupImpl obj : Core.getCore().getDbManager().obtainObjects(GroupImpl.class, "name", newText, //$NON-NLS-1$
					QueryField.EQUALS))
				if (obj != current) {
					errorMessage = Messages.GroupDialog_name_already_exists;
					break;
				}
		if (errorMessage == null)
			errorMessage = labelConfigGroup.validate();
		setErrorMessage(errorMessage);
		getButton(OK).setEnabled(errorMessage == null);
	}

	@Override
	protected void okPressed() {
		name = nameField.getText();
		annotations = computeAnnotations();
		showLabel = labelConfigGroup.getSelection();
		labelTemplate = labelConfigGroup.getTemplate();
		if (isAuto() && overwriteButton.getSelection())
			ruleComponent.accelerate();
		super.okPressed();
	}

	public String getName() {
		return name;
	}

	private String computeAnnotations() {
		if (isAuto()) {
			if (overwriteButton.getSelection())
				return AUTORULE + ruleComponent.getResult();
		} else if (isRating()) {
			StringBuilder sb = new StringBuilder(RATING);
			sb.append(notRatedButton.getSelection() ? 't' : 'f');
			sb.append(oneButton.getSelection() ? 't' : 'f');
			sb.append(twoButton.getSelection() ? 't' : 'f');
			sb.append(threeButton.getSelection() ? 't' : 'f');
			sb.append(fourButton.getSelection() ? 't' : 'f');
			sb.append(fiveButton.getSelection() ? 't' : 'f');
			String anno = sb.toString();
			if (!"Rtttttt".equals(anno)) //$NON-NLS-1$
				return anno;
		} else if (isImports()) {
			int selection = importButtons.getSelection();
			if (selection != 3)
				return IMPORTS + selection;
		} else if (isLocations()) {
			int selection = locationButtons.getSelection();
			if (selection != 2)
				return LOCATIONS + selection;
		} else if (isTimeline()) {
			StringBuilder sb = new StringBuilder(TIMELINE);
			sb.append(daysButtons.getSelection());
			sb.append(weeksButtons.getSelection());
			sb.append(monthsButtons.getSelection());
			sb.append(yearSpinner.getSelection());
			String anno = sb.toString();
			if (!"T2221900".equals(anno)) //$NON-NLS-1$
				return anno;
		}
		return null;
	}

	public String getAnnotations() {
		return annotations;
	}

	public int getShowLabel() {
		return showLabel;
	}

	public String getLabelTemplate() {
		return labelTemplate;
	}

}
