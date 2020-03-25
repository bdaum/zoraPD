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
 * (c) 2009-2019 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.widgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObjectImpl;
import com.bdaum.zoom.cat.model.creatorsContact.ContactImpl;
import com.bdaum.zoom.cat.model.group.Criterion;
import com.bdaum.zoom.cat.model.group.CriterionImpl;
import com.bdaum.zoom.cat.model.location.LocationImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.ui.internal.FieldDescriptor;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiConstants;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.codes.CodeParser;
import com.bdaum.zoom.ui.internal.dialogs.Messages;
import com.bdaum.zoom.ui.widgets.DateInput;
import com.bdaum.zoom.ui.widgets.NumericControl;

public class CriterionGroup extends AbstractCriterionGroup {

	public static class IdLabelTuple {

		public String id;
		public String label;

		public IdLabelTuple(String id, String label) {
			this.id = id;
			this.label = label;
		}
	}

	public class ValueStack extends Composite {
		private StackLayout layout;
		private Composite enumComposite;
		private Combo enumCombo;
		private Composite textComposite;
		private Text textField;
		private Composite dateComposite;
		private DateInput dateField;
		private Composite valueComposite;
		private Combo valueCombo;
		private Composite codeComposite;
		private CodeGroup codeField;
		private Object enumKeys;
		private Composite intComposite;
		private NumericControl intField;
		private ProposalListener proposalListener;
		private Label readOnlyField;
		private boolean readOnly;
		private Object value;
		private String[] items;

		public ValueStack(Composite parent, int style) {
			super(parent, SWT.NONE);
			this.readOnly = (style & SWT.READ_ONLY) != 0;
			boolean single = (style & SWT.SINGLE) != 0;
			style &= ~(SWT.READ_ONLY | SWT.SINGLE);
			GridData layoutData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
			layoutData.widthHint = !Constants.WIN32 && !readOnly || single ? 300 : 180;
			setLayoutData(layoutData);
			layout = new StackLayout();
			setLayout(layout);
			if (readOnly) {
				textComposite = createLayerComposite();
				readOnlyField = new Label(textComposite, style);
				readOnlyField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
			} else {
				textComposite = createLayerComposite();
				textField = new Text(textComposite, SWT.SINGLE | style);
				textField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
				enumComposite = createLayerComposite();
				enumCombo = new Combo(enumComposite, SWT.READ_ONLY | style);
				enumCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
				enumCombo.addListener(SWT.Selection, CriterionGroup.this);
				dateComposite = createLayerComposite();
				dateField = new DateInput(dateComposite, SWT.DATE | SWT.TIME | SWT.DROP_DOWN | style);
				dateField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, true));
				intComposite = createLayerComposite();
				intField = new NumericControl(intComposite, style);
				intField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, true));
				intField.setMaximum(100000000);
				valueComposite = createLayerComposite();
				valueCombo = new Combo(valueComposite, style);
				valueCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
				codeComposite = createLayerComposite();
				codeField = new CodeGroup(codeComposite, style, null);
				codeField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
			}
		}

		public void addFieldListeners(Listener modifyListener, Listener selectionListener) {
			if (!readOnly) {
				if (modifyListener != null) {
					textField.addListener(SWT.Modify, modifyListener);
					codeField.addListener(SWT.Modify, modifyListener);
					valueCombo.addListener(SWT.Modify, modifyListener);
				}
				if (selectionListener != null) {
					valueCombo.addListener(SWT.Selection, selectionListener);
					enumCombo.addListener(SWT.Selection, selectionListener);
					dateField.addListener(SWT.Selection, selectionListener);
					intField.addListener(SWT.Selection, selectionListener);
				}
			}
		}

		public void removeFieldListeners(Listener modifyListener, Listener selectionListener) {
			if (!readOnly) {
				if (modifyListener != null) {
					textField.removeListener(SWT.Modify, modifyListener);
					codeField.removeListener(SWT.Modify, modifyListener);
					valueCombo.removeListener(SWT.Modify, modifyListener);
				}
				if (selectionListener != null) {
					valueCombo.removeListener(SWT.Selection, selectionListener);
					enumCombo.removeListener(SWT.Selection, selectionListener);
					dateField.removeListener(SWT.Selection, selectionListener);
					intField.removeListener(SWT.Selection, selectionListener);
				}
			}
		}

		private Composite createLayerComposite() {
			Composite comp = new Composite(this, SWT.NONE);
			GridLayout gridLayout = new GridLayout();
			gridLayout.marginHeight = gridLayout.marginWidth = 0;
			comp.setLayout(gridLayout);
			return comp;
		}

		public void update(String[] valueProposals, boolean enumeration, FieldDescriptor fd, int rel) {
			if (readOnly)
				layout.topControl = textComposite;
			else {
				if (proposalListener != null) {
					proposalListener.removeFrom(textField);
					proposalListener = null;
				}
				if (valueProposals != null)
					layout.topControl = textComposite;
				else if (enumeration)
					layout.topControl = enumComposite;
				else {
					QueryField detailQfield = fd == null ? null : fd.getDetailQueryField();
					if (detailQfield != null && detailQfield.getType() == QueryField.T_DATE)
						layout.topControl = dateComposite;
					else if (detailQfield != null && detailQfield.getCard() == 1
							&& (detailQfield.getType() == QueryField.T_POSITIVEINTEGER
									|| detailQfield.getType() == QueryField.T_INTEGER)) {
						layout.topControl = intComposite;
						configureNumericField(intField, detailQfield);
					} else if (fd != null && (rel == QueryField.EQUALS || rel == QueryField.NOTEQUAL)
							&& collectionEditGroup.hasPreparedValues(fd.qfield)) {
						layout.topControl = valueComposite;
						collectionEditGroup.fillPreparedValues(valueCombo, fd.qfield);
					} else if (detailQfield != null && detailQfield.getEnumeration() instanceof Integer) {
						CodeParser parser = UiActivator.getDefault()
								.getCodeParser((Integer) fd.getDetailQueryField().getEnumeration());
						if (parser.canParse()) {
							layout.topControl = codeComposite;
							codeField.setQueryField(detailQfield);
						} else
							layout.topControl = textComposite;
					} else
						layout.topControl = textComposite;
				}
			}
			layout(true, true);
		}

		public String validate(FieldDescriptor fieldDescriptor, int rel) {
			if (!readOnly) {
				if (layout.topControl == textComposite) {
					if (textField.isEnabled()) {
						String errorMessage = UiUtilities.verifyText(fieldDescriptor, textField.getText(), rel);
						if (errorMessage == null && proposalListener != null)
							errorMessage = proposalListener.validate(textField.getText());
						return errorMessage;
					}
				} else if (layout.topControl == codeComposite && codeField.isEnabled())
					return UiUtilities.verifyText(fieldDescriptor, codeField.getText(), rel);
			}
			return null;
		}

		public boolean setValue(FieldDescriptor des, Object value) {
			if (readOnly) {
				this.value = value;
				if (value == null)
					readOnlyField.setText(""); //$NON-NLS-1$
				else if (fieldDescriptor == null)
					readOnlyField.setText(String.valueOf(value));
				else
					readOnlyField.setText(fieldDescriptor.qfield.formatScalarValue(value, true, true, null));
				return true;
			}
			if (value != null)
				try {
					if (layout.topControl == textComposite) {
						textField.setText((String) (fieldDescriptor == null ? value
								: UiUtilities.computeFieldStringValue(fieldDescriptor, value)));
						return true;
					}
					if (layout.topControl == valueComposite) {
						valueCombo.setText((String) (fieldDescriptor == null ? value
								: UiUtilities.computeFieldStringValue(fieldDescriptor, value)));
						return true;
					}
					if (layout.topControl == dateComposite) {
						dateField.setDate((Date) value);
						return true;
					}
					if (layout.topControl == intComposite) {
						if (des != null)
							configureNumericField(intField, des.getDetailQueryField());
						intField.setSelection((Integer) value);
						return true;
					}
					if (layout.topControl == codeComposite) {
						codeField.setText((String) (fieldDescriptor == null ? value
								: UiUtilities.computeFieldStringValue(fieldDescriptor, value)));
						return true;
					}

				} catch (Exception e) {
					// fall through
				}
			return false;
		}

		protected void configureNumericField(NumericControl field, QueryField qfield) {
			float maxValue = qfield.getMaxValue();
			if (Float.isInfinite(maxValue) || Float.isNaN(maxValue))
				maxValue = (float) (Math.pow(10, qfield.getMaxlength()) - 1);
			int v = (int) (maxValue + 0.5f);
			field.setMaximum(v);
			field.setMinimum(qfield.getType() == QueryField.T_INTEGER ? -v : 0);
			field.setLogrithmic(Float.isInfinite(maxValue) || (qfield.getAutoPolicy() & QueryField.AUTO_LOG) != 0);
		}

		public Object getValue(FieldDescriptor des) {
			if (readOnly)
				return value;
			if (des != null) {
				if (layout.topControl == enumComposite) {
					if (!enumCombo.isEnabled() || enumCombo.getSelectionIndex() < 0)
						return null;
					int e = enumCombo.getSelectionIndex();
					if (enumKeys instanceof int[])
						return ((int[]) enumKeys)[e];
					if (enumKeys instanceof String[])
						return ((String[]) enumKeys)[e];
					if (enumKeys instanceof IdLabelTuple[])
						return ((IdLabelTuple[]) enumKeys)[e].id;
					if (des.qfield.getType() == QueryField.T_BOOLEAN)
						return (e == 0);
					return enumCombo.getItem(e);
				}
				if (layout.topControl == codeComposite) {
					if (!codeField.isEnabled() || codeField.getText().isEmpty())
						return null;
					return UiUtilities.computeCookedValue(des, codeField.getText());
				}
				if (layout.topControl == textComposite) {
					if (!textField.isEnabled() || textField.getText().isEmpty())
						return null;
					return UiUtilities.computeCookedValue(des, textField.getText());
				}
				if (layout.topControl == dateComposite) {
					if (!dateField.isEnabled() || dateField.getDate() == null)
						return null;
					return dateField.getDate();
				}
				if (layout.topControl == intComposite) {
					if (!intField.isEnabled())
						return null;
					return intField.getSelection();
				}
				if (layout.topControl == valueComposite) {
					if (!valueCombo.isEnabled() || valueCombo.getText().isEmpty())
						return null;
					return UiUtilities.computeCookedValue(des, valueCombo.getText());
				}
			}
			return null;
		}

		public void setEnumerationData(Object enumKeys, String[] items, Object value, int maxVisible) {

			if (readOnly) {
				this.items = items;
				setValue(null, value);
			} else {
				this.enumKeys = enumKeys;
				if (items != null)
					enumCombo.setItems(items);
				if (enumCombo.getItemCount() == 1)
					enumCombo.select(0);
				else if (value != null) {
					int i = 0;
					for (String item : enumCombo.getItems()) {
						if (item.equals(value)) {
							enumCombo.select(i);
							break;
						}
						++i;
					}
				}
				enumCombo.setVisibleItemCount(Math.min(maxVisible, items.length));
			}
		}

		public void select(Object value) {
			try {
				if (enumKeys instanceof int[]) {
					int[] intKeys = (int[]) enumKeys;
					int v = ((Integer) value).intValue();
					for (int j = 0; j < intKeys.length; j++)
						if (intKeys[j] == v) {
							if (readOnly)
								setValue(null, items[j]);
							else
								enumCombo.select(j);
							break;
						}
				} else if (enumKeys instanceof String[])
					select(value, (String[]) enumKeys);
				else
					select(value, enumCombo.getItems());
			} catch (Exception e) {
				// Don't select on bad data
			}
		}

		private void select(Object value, String[] sKeys) {
			for (int j = 0; j < sKeys.length; j++)
				if (sKeys[j].equals(value)) {
					if (readOnly)
						setValue(null, items[j]);
					else
						enumCombo.select(j);
					break;
				}
		}

		private void resetValues() {
			if (readOnly)
				readOnlyField.setText(""); //$NON-NLS-1$
			else {
				enumCombo.select(0);
				dateField.setDate(new Date());
				intField.setSelection(0);
				textField.setText(""); //$NON-NLS-1$
				valueCombo.setText(""); //$NON-NLS-1$
				codeField.setText(""); //$NON-NLS-1$
			}
		}

		public void addProposalListener(ProposalListener proposalListener) {
			if (!readOnly) {
				this.proposalListener = proposalListener;
				proposalListener.addTo(textField);
			}
		}
	}

	private static final String NODETAILS = Messages.CriterionGroup_no_details;

	private Button orButton;

	private Object enumKeys;

	private boolean networked;

	private final Criterion crit;

	private String errorMessage;

	private ValueStack fromStack;

	private ValueStack toStack;

	public CriterionGroup(final Composite parent, int groupNo, CollectionEditGroup collectionEditGroup,
			final Criterion crit, boolean enabled, boolean and, boolean networked) {
		super(parent, collectionEditGroup, groupNo, enabled, and);
		this.crit = crit;
		this.networked = networked;
		relationKeys = new int[QueryField.ALLRELATIONS.length];
		int rel = -1;
		if (crit != null) {
			fieldDescriptor = new FieldDescriptor(crit);
			initGroup(fieldDescriptor.qfield);
			rel = crit.getRelation();
		} else
			initGroup(null);
		if (groupCombo != null)
			groupCombo.getCombo().addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					fillFieldCombo(crit);
					fromStack.resetValues();
					if (toStack != null)
						toStack.resetValues();
					validate();
					signalModification(event);
				}
			});
		if (relationCombo != null)
			relationCombo.setVisibleItemCount(8);
		boolean ranged = enabled && (rel == QueryField.BETWEEN || rel == QueryField.NOTBETWEEN);
		addChild(fromStack = new ValueStack(parent,
				enabled ? borderStyle : borderStyle | SWT.READ_ONLY | (ranged ? SWT.DEFAULT : SWT.SINGLE)));
		fromStack.addFieldListeners(modifyListener, this);
		if (ranged) {
			betweenLabel = new Label(parent, SWT.NONE);
			betweenLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			betweenLabel.setText(Messages.CriterionGroup_and_between);
			addChild(toStack = new ValueStack(parent, enabled ? borderStyle : borderStyle | SWT.READ_ONLY));
			toStack.addFieldListeners(modifyListener, this);
		}
		updateStacks(false, null, false, null, 0);
		createButtons(parent);
		if (fieldCombo != null)
			fieldCombo.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					fillRelationCombo(crit);
					fromStack.resetValues();
					if (toStack != null)
						toStack.resetValues();
					validate();
					signalModification(e);
				}
			});
		if (relationCombo != null)
			relationCombo.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					updateValueFields(crit, true);
					validate();
					signalModification(e);
				}
			});
		fillFieldCombo(crit);
		signalModification(null);
	}

	private void updateStacks(boolean enumeration, String[] valueProposals, boolean range, FieldDescriptor fd,
			int rel) {
		if (rel == QueryField.UNDEFINED) {
			fromStack.setVisible(false);
			if (toStack != null)
				toStack.setVisible(false);
		} else {
			fromStack.setVisible(true);
			fromStack.update(valueProposals, enumeration, fd, rel);
			if (toStack != null) {
				if (range)
					toStack.update(valueProposals, enumeration, fd, rel);
				toStack.setVisible(range);
				betweenLabel.setVisible(range);
			}
		}
	}

	protected void setNetworked(boolean networked) {
		this.networked = networked;
		updateValueFields(crit, true);
	}

	private void createButtons(final Composite parent) {
		orButton = createButton(parent, Messages.CriterionGroup_OR);
		orButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				CriterionGroup.this.collectionEditGroup.addGroup(parent, CriterionGroup.this, null, false);
				signalModification(e);
			}
		});
		andButton = createButton(parent, Messages.CriterionGroup_AND);
		andButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				CriterionGroup.this.collectionEditGroup.addGroup(parent, CriterionGroup.this, null, true);
				signalModification(e);
			}
		});
		clearButton = createButton(parent, Icons.delete.getImage());
		clearButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				CriterionGroup.this.collectionEditGroup.removeGroup(CriterionGroup.this);
				signalModification(e);
			}
		});
		clearButton.setVisible(enabled && groupNo > 0);
	}

	private Listener modifyListener = new Listener() {
		public void handleEvent(Event e) {
			validate();
			signalModification(e);
		}
	};

	private Label betweenLabel;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void validate() {
		errorMessage = null;
		int rel = getRelationValue();
		if (rel != QueryField.UNDEFINED) {
			errorMessage = fromStack.validate(fieldDescriptor, rel);
			if (errorMessage == null && toStack != null && toStack.isVisible()) {
				errorMessage = toStack.validate(fieldDescriptor, rel);
				Object fromValue = fromStack.getValue(fieldDescriptor);
				Object toValue = toStack.getValue(fieldDescriptor);
				if (fromValue instanceof Comparable && toValue instanceof Comparable) {
					try {
						if (((Comparable) fromValue).compareTo(toValue) >= 0)
							errorMessage = Messages.CriterionGroup_from_must_be_smaller;
					} catch (Exception e) {
						// can't compare - no message
					}
				}
			}
		}
	}

	private void updateValueFields(Criterion crit, boolean update) {
		FieldDescriptor des = getFieldValue();
		if (des != null) {
			Object oldFrom = null;
			Object oldTo = null;
			fieldDescriptor = des;
			if (update) {
				oldFrom = fromStack.getValue(fieldDescriptor);
				if (toStack != null && toStack.isVisible())
					oldTo = toStack.getValue(fieldDescriptor);
			}
			String[] enumLabels = fieldDescriptor.getDetailQueryField().getEnumLabels();
			int type = fieldDescriptor.getDetailQueryField().getType();
			IDbManager dbManager = Core.getCore().getDbManager();
			if (fieldDescriptor.isStruct()) {
				updateStacks(true, null, false, fieldDescriptor, crit == null ? 0 : crit.getRelation());
				List<IdLabelTuple> labels = new ArrayList<IdLabelTuple>();
				Class<? extends IdentifiableObject> clazz = null;
				switch (fieldDescriptor.getDetailQueryField().getType()) {
				case QueryField.T_LOCATION:
					clazz = LocationImpl.class;
					break;
				case QueryField.T_OBJECT:
					clazz = ArtworkOrObjectImpl.class;
					break;
				case QueryField.T_CONTACT:
					clazz = ContactImpl.class;
					break;
				}
				if (clazz != null) {
					List<? extends IdentifiableObject> set = dbManager.obtainObjects(clazz);
					for (IdentifiableObject obj : set)
						labels.add(new IdLabelTuple(obj.getStringId(), QueryField.serializeStruct(obj, NODETAILS)));
					Collections.sort(labels, new Comparator<IdLabelTuple>() {
						public int compare(IdLabelTuple t1, IdLabelTuple t2) {
							return t1.label.compareTo(t2.label);
						}
					});
				}
				enumKeys = labels.toArray(new IdLabelTuple[labels.size()]);
				String[] array = new String[labels.size()];
				String id = (crit != null && (crit.getValue() instanceof String)) ? (String) crit.getValue() : null;
				int k = 0;
				for (IdLabelTuple t : labels)
					array[k++] = t.label;
				fromStack.setEnumerationData(enumKeys, array, id, 10);
			} else {
				enumKeys = null;
				int rel = getRelationValue();
				if (rel != 0) {
					boolean range = (rel == QueryField.BETWEEN || rel == QueryField.NOTBETWEEN);
					String[] valueProposals = enabled && (rel == QueryField.EQUALS || rel == QueryField.NOTEQUAL)
							? UiUtilities.getValueProposals(dbManager, fieldDescriptor.qfield, fieldDescriptor.subfield,
									networked)
							: null;
					if (enumLabels != null || type == QueryField.T_BOOLEAN || valueProposals != null) {
						updateStacks(true, valueProposals, range, fieldDescriptor,
								crit == null ? 0 : crit.getRelation());
						enumKeys = fieldDescriptor.getDetailQueryField().getEnumeration();
						if (valueProposals != null) {
							boolean set = oldFrom != null && fromStack.setValue(null, oldFrom);
							if (!set && crit != null)
								fromStack.setValue(null, crit.getValue().toString());
							fromStack.addProposalListener(new ProposalListener(valueProposals));
						} else if (enumLabels != null) {
							if (toStack != null && toStack.isVisible())
								toStack.setEnumerationData(enumKeys, enumLabels, null, 10);
							fromStack.setEnumerationData(enumKeys, enumLabels, crit != null ? crit.getValue() : null,
									10);
						} else if (type == QueryField.T_BOOLEAN)
							fromStack.setEnumerationData(enumKeys, UiConstants.BOOLEANLABELS, null, 10);
						if (crit != null) {
							fromStack.select(crit.getValue());
							if (range && toStack != null)
								toStack.select(crit.getTo()); 
						}
					} else if (range) {
						updateStacks(false, null, true, fieldDescriptor, rel);
						boolean set = oldFrom != null && fromStack.setValue(fieldDescriptor, oldFrom);
						set &= oldTo != null && toStack != null && toStack.setValue(fieldDescriptor, oldTo);
						if (!set && crit != null) {
							fromStack.setValue(fieldDescriptor, crit.getValue());
							Object vto = crit.getTo();
							if (vto != null && toStack != null)
								toStack.setValue(fieldDescriptor, vto);
						}
					} else {
						updateStacks(false, null, false, fieldDescriptor, rel);
						if (rel != QueryField.UNDEFINED
								&& (oldFrom == null || !fromStack.setValue(fieldDescriptor, oldFrom)) && crit != null)
							fromStack.setValue(fieldDescriptor, crit.getValue());
					}
				}
			}
		}

	}

	private void fillRelationCombo(Criterion crit) {
		FieldDescriptor des = getFieldValue();
		if (des != null) {
			fieldDescriptor = des;
			int relation = fieldDescriptor.getDetailQueryField().getRelations();
			List<String> relationLabels = new ArrayList<String>();
			for (int k = 0, j = 0; k < QueryField.ALLRELATIONS.length; k++) {
				int rel = QueryField.ALLRELATIONS[k];
				if ((rel & relation) != 0) {
					relationKeys[j++] = rel;
					relationLabels.add(QueryField.ALLRELATIONLABELS[k]);
				}
			}
			setRelationValue(crit == null ? 0 : crit.getRelation(), relationKeys,
					relationLabels.toArray(new String[relationLabels.size()]));
		}
		updateValueFields(crit, false);
	}

	private void fillFieldCombo(Criterion crit) {
		fillFieldCombo((FieldDescriptor) null);
		if (crit != null)
			setFieldValue(new FieldDescriptor(crit));
		fillRelationCombo(crit);
	}

	public Criterion getCriterion() {
		FieldDescriptor des = getFieldValue();
		int relation = getRelationValue();
		if (getGroupValue() == null || des == null || relation == 0)
			return null;
		Object fromValue = fromStack.getValue(des);
		if (fromValue == null)
			return null;
		Object toValue = null;
		if (toStack != null && toStack.isVisible()) {
			toValue = toStack.getValue(des);
			if (toValue == null)
				return null;
		}
		Criterion crit = new CriterionImpl();
		crit.setField(des.qfield.getKey());
		crit.setSubfield(des.subfield == null ? null : des.subfield.getKey());
		crit.setRelation(relation);
		crit.setAnd(and);
		crit.setValue(fromValue);
		crit.setTo(toValue);
		return crit;
	}

	@Override
	protected boolean acceptField(QueryField mainField) {
		return true;
	}

	@Override
	protected boolean acceptRootStruct(QueryField mainField) {
		return true;
	}

	@Override
	protected ViewerFilter getExtensionFilter() {
		return new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				return true;
			}
		};
	}

	public String getErrorMessage() {
		return errorMessage;
	}

}
