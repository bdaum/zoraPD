package com.bdaum.zoom.ui.internal.preferences;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.cat.model.group.Criterion;
import com.bdaum.zoom.cat.model.group.CriterionImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.Range;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.net.core.internal.Base64;
import com.bdaum.zoom.ui.internal.FieldDescriptor;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiConstants;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.codes.CodeParser;
import com.bdaum.zoom.ui.internal.widgets.CodeGroup;
import com.bdaum.zoom.ui.internal.widgets.CriterionGroup.IdLabelTuple;
import com.bdaum.zoom.ui.internal.widgets.GroupComboCatFilter;
import com.bdaum.zoom.ui.internal.widgets.GroupComboLabelProvider;
import com.bdaum.zoom.ui.internal.widgets.ProposalListener;
import com.bdaum.zoom.ui.widgets.DateInput;

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
 * (c) 2012 Berthold Daum  
 */
public class ColorCodeGroup {

	private static final String[] NOITEMS = new String[0];

	private ComboViewer critGroupCombo;

	private List<FieldDescriptor> fieldDescriptors = new ArrayList<FieldDescriptor>(150);

	private FieldDescriptor fieldDescriptor;

	private int[] relationKeys = new int[QueryField.ALLRELATIONS.length];

	private Object enumKeys;

	private DateInput dateValueField, dateFromField, dateToField;

	private Text textFromField, textToField, textValueField;

	private CodeGroup codeValueField, codeFromField, codeToField;

	private Criterion crit;

	private Composite valueComp, codeGroup, textGroup, enumGroup, dateGroup, undefinedGroup, enumRangeGroup,
			dateRangeGroup, codeRangeGroup, textRangeGroup;

	private Combo enumValueCombo, enumFromField, enumToField, critRelationCombo, critFieldCombo;

	private StackLayout valueLayout;

	private final PreferencePage page;

	private List<Text> fieldsToValidate = new ArrayList<Text>(2);

	@SuppressWarnings("unused")
	public ColorCodeGroup(Composite autoGroup, int index, PreferencePage page) {
		this.page = page;
		new Label(autoGroup, SWT.BORDER).setImage(Icons.toSwtColors(index));
		new Label(autoGroup, SWT.NONE);
		critGroupCombo = new ComboViewer(autoGroup, SWT.READ_ONLY | SWT.BORDER);
		Combo comboControl = critGroupCombo.getCombo();
		comboControl.setVisibleItemCount(10);
		critGroupCombo.setContentProvider(ArrayContentProvider.getInstance());
		critGroupCombo.setFilters(new ViewerFilter[] { new GroupComboCatFilter() });
		critGroupCombo.setLabelProvider(new GroupComboLabelProvider());
		critGroupCombo.setInput(QueryField.getCategoriesAndSubgroups());
		critGroupCombo.setSelection(new StructuredSelection(QueryField.CATEGORY_ALL));
		comboControl.setLayoutData(new GridData(80, SWT.DEFAULT));
		critFieldCombo = new Combo(autoGroup, SWT.READ_ONLY | SWT.BORDER);
		critFieldCombo.setLayoutData(new GridData(200, SWT.DEFAULT));
		critFieldCombo.setVisibleItemCount(10);
		critRelationCombo = new Combo(autoGroup, SWT.READ_ONLY | SWT.BORDER);
		critRelationCombo.setLayoutData(new GridData(80, SWT.DEFAULT));
		valueComp = new Composite(autoGroup, SWT.NONE);
		valueComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		valueLayout = new StackLayout();
		valueComp.setLayout(valueLayout);
		enumGroup = createStackGroup(valueComp, 1);
		enumValueCombo = new Combo(enumGroup, SWT.READ_ONLY | SWT.BORDER);
		GridData data = new GridData(SWT.LEFT, SWT.CENTER, false, true);
		data.widthHint = 150;
		enumValueCombo.setLayoutData(data);
		textGroup = createStackGroup(valueComp, 1);
		textValueField = new Text(textGroup, SWT.SINGLE | SWT.BORDER);
		data = new GridData(SWT.LEFT, SWT.CENTER, false, true);
		data.widthHint = 320;
		textValueField.setLayoutData(data);
		codeGroup = createStackGroup(valueComp, 1);
		codeValueField = new CodeGroup(codeGroup, SWT.NONE, null);
		data = new GridData(SWT.LEFT, SWT.CENTER, false, true);
		data.widthHint = 150;
		codeValueField.setLayoutData(data);
		dateGroup = createStackGroup(valueComp, 1);
		dateValueField = new DateInput(dateGroup, SWT.DATE | SWT.TIME | SWT.DROP_DOWN | SWT.BORDER);
		dateValueField.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true));
		enumRangeGroup = createStackGroup(valueComp, 2);
		enumFromField = new Combo(enumRangeGroup, SWT.READ_ONLY | SWT.BORDER);
		data = new GridData(SWT.LEFT, SWT.CENTER, false, true);
		data.widthHint = 150;
		enumFromField.setLayoutData(data);
		enumToField = new Combo(enumRangeGroup, SWT.READ_ONLY | SWT.BORDER);
		data = new GridData(SWT.LEFT, SWT.CENTER, false, true);
		data.widthHint = 150;
		enumToField.setLayoutData(data);
		codeRangeGroup = createStackGroup(valueComp, 2);
		codeFromField = new CodeGroup(codeRangeGroup, SWT.NONE, null);
		data = new GridData(SWT.LEFT, SWT.CENTER, false, true);
		data.widthHint = 150;
		codeFromField.setLayoutData(data);
		codeToField = new CodeGroup(codeRangeGroup, SWT.NONE, null);
		data = new GridData(SWT.LEFT, SWT.CENTER, false, true);
		data.widthHint = 150;
		codeToField.setLayoutData(data);
		textRangeGroup = createStackGroup(valueComp, 2);
		textFromField = new Text(textRangeGroup, SWT.SINGLE | SWT.BORDER);
		textFromField.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true));
		textToField = new Text(textRangeGroup, SWT.SINGLE | SWT.BORDER);
		textToField.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true));
		dateRangeGroup = createStackGroup(valueComp, 2);
		dateFromField = new DateInput(dateRangeGroup, SWT.DATE | SWT.TIME | SWT.DROP_DOWN | SWT.BORDER);
		dateFromField.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true));
		dateToField = new DateInput(dateRangeGroup, SWT.DATE | SWT.TIME | SWT.DROP_DOWN | SWT.BORDER);
		dateToField.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true));
		undefinedGroup = createStackGroup(valueComp, 1);
		Button clearButton = new Button(autoGroup, SWT.PUSH);
		clearButton.setImage(Icons.delete.getImage());
		clearButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				crit = null;
				switchColor();
			}
		});
		critGroupCombo.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				fillFieldCombo();
				resetValues();
			}
		});
		critFieldCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fillRelationCombo();
				resetValues();
				validate();
			}
		});
		critRelationCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateValueFields(crit);
				validate();
			}
		});
		fillFieldCombo();
	}

	private static Composite createStackGroup(Composite parent, int columns) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(columns, false));
		return composite;
	}

	private void resetValues() {
		if (valueLayout.topControl == enumGroup)
			enumValueCombo.select(0);
		else if (valueLayout.topControl == dateGroup)
			dateValueField.setDate(new Date());
		else if (valueLayout.topControl == textGroup)
			textValueField.setText(""); //$NON-NLS-1$
		else if (valueLayout.topControl == codeGroup)
			codeValueField.setText(""); //$NON-NLS-1$
		else if (valueLayout.topControl == enumRangeGroup) {
			enumFromField.select(0);
			enumToField.select(0);
		} else if (valueLayout.topControl == dateRangeGroup) {
			dateFromField.setDate(new Date());
			dateToField.setDate(new Date());
		} else if (valueLayout.topControl == textRangeGroup) {
			textFromField.setText(""); //$NON-NLS-1$
			textToField.setText(""); //$NON-NLS-1$
		} else if (valueLayout.topControl == codeRangeGroup) {
			codeFromField.setText(""); //$NON-NLS-1$
			codeToField.setText(""); //$NON-NLS-1$
		}
	}

	private void fillRelationCombo() {
		int i = critFieldCombo.getSelectionIndex();
		if (i >= 0) {
			fieldDescriptor = fieldDescriptors.get(i);
			int relation = fieldDescriptor.getDetailQueryField().getRelations();
			List<String> relationLabels = new ArrayList<String>();
			int j = 0;
			for (int k = 0; k < QueryField.ALLRELATIONS.length; k++) {
				int rel = QueryField.ALLRELATIONS[k];
				if ((rel & relation) != 0) {
					relationKeys[j++] = rel;
					relationLabels.add(QueryField.ALLRELATIONLABELS[k]);
				}
			}
			critRelationCombo.setItems(relationLabels.toArray(new String[relationLabels.size()]));
			int index = -1;
			if (critRelationCombo.getItemCount() >= 1)
				index = 0;
			if (crit != null) {
				int rel = crit.getRelation();
				for (int k = 0; k < relationLabels.size(); k++)
					if (relationKeys[k] == rel) {
						index = k;
						break;
					}
			}
			if (index >= 0)
				critRelationCombo.select(index);
		} else
			critRelationCombo.setItems(NOITEMS);
		updateValueFields(crit);
	}

	private void updateValueFields(Criterion criterion) {
		if (proposalListener != null) {
			proposalListener.removeFrom(textValueField);
			proposalListener = null;
		}
		int i = critFieldCombo.getSelectionIndex();
		if (i >= 0) {
			int index = critRelationCombo.getSelectionIndex();
			if (index >= 0) {
				fieldDescriptor = fieldDescriptors.get(i);
				int rel = relationKeys[index];
				String[] enumLabels = fieldDescriptor.getDetailQueryField().getEnumLabels();
				int type = fieldDescriptor.getDetailQueryField().getType();
				IDbManager dbManager = Core.getCore().getDbManager();
				String[] valueProposals = getValueProposals(dbManager, fieldDescriptor.qfield,
						fieldDescriptor.subfield);
				if (enumLabels != null || type == QueryField.T_BOOLEAN || valueProposals != null) {
					updateValueGroup(true, valueProposals, rel == QueryField.BETWEEN || rel == QueryField.NOTBETWEEN,
							fieldDescriptor, rel);
					enumKeys = fieldDescriptor.getDetailQueryField().getEnumeration();
					if (valueProposals != null) {
						proposalListener = new ProposalListener(valueProposals);
						proposalListener.addTo(textValueField);
					} else {
						if (enumLabels != null) {
							enumValueCombo.setItems(enumLabels);
							enumFromField.setItems(enumLabels);
							enumToField.setItems(enumLabels);
						} else if (type == QueryField.T_BOOLEAN)
							enumValueCombo.setItems(UiConstants.BOOLEANLABELS);
						int minItems = Math.min(10, enumValueCombo.getItemCount());
						enumValueCombo.setVisibleItemCount(minItems);
						enumFromField.setVisibleItemCount(minItems);
						enumToField.setVisibleItemCount(minItems);
						if (criterion != null) {
							Object value = criterion.getValue();
							if (enumKeys instanceof int[]) {
								int[] intKeys = (int[]) enumKeys;
								if (value instanceof Range) {
									Range range = (Range) value;
									int v = ((Integer) range.getFrom()).intValue();
									for (int j = 0; j < intKeys.length; j++)
										if (intKeys[j] == v) {
											enumFromField.select(j);
											break;
										}
									v = ((Integer) range.getTo()).intValue();
									for (int j = 0; j < intKeys.length; j++)
										if (intKeys[j] == v) {
											enumToField.select(j);
											break;
										}
								} else if (value != null) {
									int v = ((Integer) value).intValue();
									for (int j = 0; j < intKeys.length; j++)
										if (intKeys[j] == v) {
											enumValueCombo.select(j);
											break;
										}
								}
							} else if (enumKeys instanceof String[]) {
								String[] sKeys = (String[]) enumKeys;
								for (int j = 0; j < sKeys.length; j++)
									if (sKeys[j].equals(value)) {
										enumValueCombo.select(j);
										break;
									}
							} else {
								String[] sKeys = enumValueCombo.getItems();
								for (int j = 0; j < sKeys.length; j++)
									if (sKeys[j].equals(value)) {
										enumValueCombo.select(j);
										break;
									}
							}
						}
					}
				} else {
					enumKeys = null;
					if (rel == QueryField.BETWEEN || rel == QueryField.NOTBETWEEN) {
						updateValueGroup(false, null, true, fieldDescriptor, rel);
						if (criterion != null) {
							Range range = (Range) criterion.getValue();
							Object from = range.getFrom();
							Object to = range.getTo();
							if (valueLayout.topControl == enumRangeGroup) {
								enumFromField.setText(UiUtilities.computeFieldStringValue(fieldDescriptor, from));
								enumToField.setText(UiUtilities.computeFieldStringValue(fieldDescriptor, to));
							} else if (valueLayout.topControl == textRangeGroup) {
								textFromField.setText(UiUtilities.computeFieldStringValue(fieldDescriptor, from));
								textToField.setText(UiUtilities.computeFieldStringValue(fieldDescriptor, to));
							} else if (valueLayout.topControl == codeRangeGroup) {
								codeFromField.setText(UiUtilities.computeFieldStringValue(fieldDescriptor, from));
								codeToField.setText(UiUtilities.computeFieldStringValue(fieldDescriptor, to));
							} else {
								dateFromField.setDate((Date) from);
								dateToField.setDate((Date) to);
							}
						}
					} else {
						updateValueGroup(false, null, false, fieldDescriptor, rel);
						if (criterion != null && rel != QueryField.UNDEFINED) {
							Object from = criterion.getValue();
							if (valueLayout.topControl == enumGroup)
								enumValueCombo.setText(UiUtilities.computeFieldStringValue(fieldDescriptor, from));
							else if (valueLayout.topControl == textGroup)
								textValueField.setText(UiUtilities.computeFieldStringValue(fieldDescriptor, from));
							else if (valueLayout.topControl == codeGroup)
								codeValueField.setText(UiUtilities.computeFieldStringValue(fieldDescriptor, from));
							else
								dateValueField.setDate((Date) from);
						}
					}
				}
			}
		}
	}

	private static String[] getValueProposals(IDbManager dbManager, QueryField qfield, QueryField subfield) {
		Set<String> valueProposals = UiUtilities.getValueProposals(dbManager, qfield.getKey(),
				subfield == null ? null : subfield.getKey());
		if (valueProposals == null)
			return null;
		String[] array = valueProposals.toArray(new String[valueProposals.size()]);
		Arrays.sort(array);
		return array;
	}

	private ModifyListener modifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			validate();
		}
	};

	private ProposalListener proposalListener;

	private void switchColor() {
		resetValues();
		if (crit == null)
			crit = new CriterionImpl();
		if (crit.getField() == null || crit.getField().isEmpty()) {
			fieldDescriptor = null;
			critGroupCombo.setSelection(new StructuredSelection(QueryField.CATEGORY_ALL));
		} else {
			fieldDescriptor = new FieldDescriptor(crit);
			critGroupCombo.setSelection(new StructuredSelection(fieldDescriptor.qfield.getCategory()));
		}
		fillFieldCombo();
	}

	protected void validate() {
		int index = critRelationCombo.getSelectionIndex();
		int rel = index >= 0 ? relationKeys[index] : -1;
		String errorMessage = null;
		for (Text field : fieldsToValidate) {
			if (!field.isEnabled() || !field.isVisible())
				continue;
			errorMessage = UiUtilities.verifyText(fieldDescriptor, field.getText(), rel);
			if (errorMessage == null && proposalListener != null)
				errorMessage = proposalListener.validate(field.getText());
			if (errorMessage != null)
				break;
		}
		page.setErrorMessage(errorMessage);
		page.setValid(errorMessage == null);
	}

	private void updateValueGroup(boolean enumeration, String[] valueProposals, boolean range, FieldDescriptor fd,
			int rel) {
		for (Text field : fieldsToValidate)
			field.removeModifyListener(modifyListener);
		fieldsToValidate.clear();
		if (rel == QueryField.UNDEFINED)
			valueLayout.topControl = undefinedGroup;
		else {
			QueryField detailQueryField = fd.getDetailQueryField();
			if (range) {
				if (enumeration)
					valueLayout.topControl = enumRangeGroup;
				else if (fd != null && detailQueryField.getType() == QueryField.T_DATE)
					valueLayout.topControl = dateRangeGroup;
				else {
					CodeParser parser = null;
					if (fd != null && detailQueryField.getEnumeration() instanceof Integer)
						parser = UiActivator.getDefault().getCodeParser((Integer) detailQueryField.getEnumeration());
					if (parser != null && parser.canParse()) {
						valueLayout.topControl = codeRangeGroup;
						codeFromField.setQueryField(detailQueryField);
						monitorField(codeFromField.getTextControl());
						codeToField.setQueryField(detailQueryField);
						monitorField(codeToField.getTextControl());
					} else {
						valueLayout.topControl = textRangeGroup;
						monitorField(textFromField);
						monitorField(textToField);
					}
				}
			} else {
				if (valueProposals != null)
					valueLayout.topControl = textGroup;
				else if (enumeration)
					valueLayout.topControl = enumGroup;
				else if (fd != null && detailQueryField.getType() == QueryField.T_DATE)
					valueLayout.topControl = dateGroup;
				else {
					CodeParser parser = null;
					if (fd != null && detailQueryField.getEnumeration() instanceof Integer)
						parser = UiActivator.getDefault().getCodeParser((Integer) detailQueryField.getEnumeration());
					if (parser != null && parser.canParse()) {
						valueLayout.topControl = codeGroup;
						codeValueField.setQueryField(detailQueryField);
						monitorField(codeValueField.getTextControl());
					} else {
						valueLayout.topControl = textGroup;
						monitorField(textValueField);
					}
				}
			}
		}
		valueComp.layout();
	}

	private void monitorField(Text field) {
		field.addModifyListener(modifyListener);
		fieldsToValidate.add(field);
	}

	private void fillFieldCombo() {
		List<FieldDescriptor> fields = new ArrayList<FieldDescriptor>();
		fieldDescriptors.clear();
		Object g = critGroupCombo.getStructuredSelection().getFirstElement();
		for (String id : QueryField.getQueryFieldKeys()) {
			QueryField mainField = QueryField.findQueryField(id);
			if (mainField.belongsTo(g) && mainField.hasLabel() && mainField.isQuery() && !mainField.isStruct())
				fields.add(new FieldDescriptor(mainField, null));
		}
		Collections.sort(fields, new Comparator<FieldDescriptor>() {
			public int compare(FieldDescriptor o1, FieldDescriptor o2) {
				return o1.label.compareTo(o2.label);
			}
		});
		List<String> fieldLabels = new ArrayList<String>();
		for (FieldDescriptor des : fields) {
			fieldDescriptors.add(des);
			fieldLabels.add(des.label);
		}
		critFieldCombo.setItems(fieldLabels.toArray(new String[fieldLabels.size()]));
		if (crit != null && crit.getField() != null && crit.getField().length() != 0)
			critFieldCombo.select(fieldDescriptors.indexOf(new FieldDescriptor(crit)));
		fillRelationCombo();
	}

	public Criterion getCriterion() {
		int field = critFieldCombo.getSelectionIndex();
		int relation = critRelationCombo.getSelectionIndex();
		if (critGroupCombo.getSelection().isEmpty() || field < 0 || relation < 0)
			return null;
		if (valueLayout.topControl == enumGroup && enumValueCombo.getSelectionIndex() < 0)
			return null;
		if (valueLayout.topControl == enumRangeGroup
				&& (enumFromField.getSelectionIndex() < 0 || enumToField.getSelectionIndex() < 0))
			return null;
		if (valueLayout.topControl == codeGroup && codeValueField.getText().isEmpty())
			return null;
		if (valueLayout.topControl == codeRangeGroup
				&& (codeFromField.getText().isEmpty() || codeToField.getText().isEmpty()))
			return null;
		if (valueLayout.topControl == textGroup && textValueField.getText().isEmpty())
			return null;
		if (valueLayout.topControl == textRangeGroup
				&& (textFromField.getText().isEmpty() || textToField.getText().isEmpty()))
			return null;
		Criterion criterion = new CriterionImpl();
		FieldDescriptor des = fieldDescriptors.get(field);
		criterion.setField(des.qfield.getKey());
		criterion.setSubfield(des.subfield == null ? null : des.subfield.getKey());
		criterion.setRelation(relationKeys[relation]);
		if (valueLayout.topControl == enumGroup)
			criterion.setValue(getEnumValue(enumValueCombo, des));
		else if (valueLayout.topControl == enumRangeGroup)
			criterion.setValue(new Range(getEnumValue(enumFromField, des), getEnumValue(enumToField, des)));
		else if (valueLayout.topControl == dateGroup)
			criterion.setValue(dateValueField.getDate());
		else if (valueLayout.topControl == dateRangeGroup)
			criterion.setValue(new Range(dateFromField.getDate(), dateToField.getDate()));
		else if (valueLayout.topControl == codeGroup)
			criterion.setValue(UiUtilities.computeCookedValue(des, codeValueField.getText()));
		else if (valueLayout.topControl == dateRangeGroup)
			criterion.setValue(new Range(UiUtilities.computeCookedValue(des, codeFromField.getText()),
					UiUtilities.computeCookedValue(des, codeToField.getText())));
		else if (valueLayout.topControl == textGroup)
			criterion.setValue(UiUtilities.computeCookedValue(des, textValueField.getText()));
		else
			criterion.setValue(new Range(UiUtilities.computeCookedValue(des, textFromField.getText()),
					UiUtilities.computeCookedValue(des, textToField.getText())));
		return criterion;
	}

	private Object getEnumValue(Combo combo, FieldDescriptor des) {
		int e = combo.getSelectionIndex();
		if (enumKeys instanceof int[])
			return ((int[]) enumKeys)[e];
		if (enumKeys instanceof String[])
			return ((String[]) enumKeys)[e];
		if (enumKeys instanceof IdLabelTuple[])
			return ((IdLabelTuple[]) enumKeys)[e].id;
		QueryField qField = des.qfield;
		if (qField.getType() == QueryField.T_BOOLEAN)
			return (e == 0);
		return combo.getItem(e);
	}

	public void fillValues(String token) {
		crit = token != null ? UiActivator.decodeAutoColoringCriterion(token) : new CriterionImpl();
		switchColor();
	}

	public String encodeCriterion() {
		crit = getCriterion();
		if (crit == null)
			return ""; //$NON-NLS-1$
		StringBuilder sb = new StringBuilder();
		sb.append(crit.getField()).append('\t').append(crit.getRelation()).append('\t');
		try (ObjectOutput out = new ObjectOutputStream(new ByteArrayOutputStream())) {
			out.writeObject(crit.getValue());
			sb.append(Base64.encodeBytes(new ByteArrayOutputStream().toByteArray()));
			return sb.toString();
		} catch (IOException e) {
			// should never happen
		}
		return null;
	}

}
