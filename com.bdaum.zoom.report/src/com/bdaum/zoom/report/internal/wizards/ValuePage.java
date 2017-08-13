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
 * (c) 2017 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.report.internal.wizards;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;

import com.bdaum.zoom.cat.model.report.Report;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.report.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.dialogs.AllNoneGroup;
import com.bdaum.zoom.ui.internal.views.AbstractPropertiesView.ViewComparator;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.RadioButtonGroup;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.widgets.NumericControl;
import com.bdaum.zoom.ui.widgets.RangeControl;
import com.bdaum.zoom.ui.wizards.ColoredWizardPage;

@SuppressWarnings("restriction")
public class ValuePage extends ColoredWizardPage implements SelectionListener {

	private class ViewerComp extends Composite {

		private class MetadataContentProvider implements ITreeContentProvider {

			public void dispose() {
				// do nothing
			}

			public void inputChanged(Viewer aViewer, Object oldInput, Object newInput) {
				// do nothing
			}

			public Object[] getChildren(Object parentElement) {
				QueryField qfield = getDetailField(parentElement);
				if (qfield != null) {
					boolean composite = false;
					QueryField[] children;
					switch (qfield.getType()) {
					case QueryField.T_CONTACT:
						composite = true;
						children = QueryField.CONTACT_TYPE.getChildren();
						break;
					case QueryField.T_LOCATION:
						composite = true;
						children = QueryField.LOCATION_TYPE.getChildren();
						break;
					case QueryField.T_OBJECT:
						composite = true;
						children = QueryField.ARTWORKOROBJECT_TYPE.getChildren();
						break;
					default:
						children = qfield.getChildren();
						break;
					}
					List<QueryField> fields = new ArrayList<QueryField>(children.length);
					for (QueryField field : children)
						if (hasChildren(field) || field.isReportField() && (!essential || field.isEssential()))
							fields.add(field);
					if (composite) {
						QueryField[][] result = new QueryField[fields.size()][];
						int i = 0;
						for (QueryField field : fields)
							result[i++] = new QueryField[] { qfield, field };
						return result;
					}
					return fields.toArray();
				}
				return EMPTYOBJECTS;
			}

			public Object getParent(Object element) {
				if (element instanceof QueryField) {
					QueryField parent = ((QueryField) element).getParent();
					if (parent != QueryField.ALL)
						return parent;
				} else if (element instanceof QueryField[])
					return ((QueryField[]) element)[0];
				return null;
			}

			public boolean hasChildren(Object element) {
				return ViewerComp.this.hasChildren(element);
			}

			public Object[] getElements(Object inputElement) {
				return (inputElement instanceof QueryField) ? ((QueryField) inputElement).getChildren() : EMPTYOBJECTS;
			}
		}

		private boolean essential = false;
		private TreeViewer viewer;
		private ViewerFilter viewerFilter;

		public ViewerComp(Composite parent) {
			super(parent, SWT.NONE);
			setLayout(new GridLayout(1, false));
			CheckboxButton essentialButton = WidgetFactory.createCheckButton(this, Messages.ValuePage_only_essential,
					new GridData(SWT.END, SWT.CENTER, true, true));
			essentialButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					essential = essentialButton.getSelection();
					setInput();
				}
			});
			viewer = new TreeViewer(this, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
			GridData layoutData = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, true);
			layoutData.widthHint = 500;
			layoutData.heightHint = 250;
			viewer.getControl().setLayoutData(layoutData);
			TreeViewerColumn col1 = new TreeViewerColumn(viewer, SWT.NONE);
			col1.getColumn().setWidth(400);
			col1.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					QueryField qfield = getDetailField(element);
					if (qfield != null)
						return qfield.getLabel();
					return super.getText(element);
				}
			});
			viewer.setContentProvider(new MetadataContentProvider());
			viewer.setComparator(new ViewComparator());
			viewerFilter = new ViewerFilter() {
				@Override
				public boolean select(Viewer viewer, Object parentElement, Object element) {
					QueryField qfield = getDetailField(element);
					if (qfield != null) {
						if (hasChildren(qfield))
							return true;
						int type = qfield.getType();
						if ((mode & ReportWizard.NUMERIC) != 0) {
							if (qfield.getEnumLabels() != null || (qfield.getEnumeration()) != null)
								return false;
							if ((mode & ReportWizard.SALES) != 0 && qfield == QueryField.SALES)
								return false;
							if ((mode & ReportWizard.EARNINGS) != 0 && qfield == QueryField.EARNINGS)
								return false;
							return type == QueryField.T_CURRENCY || type == QueryField.T_FLOAT
									|| type == QueryField.T_FLOATB || type == QueryField.T_INTEGER
									|| type == QueryField.T_POSITIVEFLOAT || type == QueryField.T_POSITIVEINTEGER;
						}
						return type == QueryField.T_STRING || type == QueryField.T_BOOLEAN
								|| qfield.getEnumeration() != null || qfield.getEnumLabels() != null;
					}
					return false;
				}
			};
			viewer.setFilters(viewerFilter);
		}

		public void addSelectionChangedListener(ISelectionChangedListener selectionChangedListener) {
			viewer.addSelectionChangedListener(selectionChangedListener);

		}

		public boolean hasChildren(Object element) {
			QueryField qfield = getDetailField(element);
			if (qfield != null) {
				QueryField[] children;
				switch (qfield.getType()) {
				case QueryField.T_CONTACT:
					children = QueryField.CONTACT_TYPE.getChildren();
					break;
				case QueryField.T_LOCATION:
					children = QueryField.LOCATION_TYPE.getChildren();
					break;
				case QueryField.T_OBJECT:
					children = QueryField.ARTWORKOROBJECT_TYPE.getChildren();
					break;
				default:
					children = qfield.getChildren();
					break;
				}
				if (children != null)
					for (QueryField field : children)
						if (hasChildren(field) || field.isReportField() && (!essential || field.isEssential())
								&& viewerFilter.select(viewer, element, field))
							return true;
			}
			return false;
		}

		public void setInput() {
			Object[] expandedElements = viewer.getExpandedElements();
			viewer.setInput(QueryField.ALL);
			viewer.setExpandedElements(expandedElements);
		}

		public String checkSelection() {
			QueryField qfield = getDetailField(((IStructuredSelection) viewer.getSelection()).getFirstElement());
			if (qfield == null)
				return Messages.ValuePage_select_field;
			if (qfield.isReportField())
				return null;
			return Messages.ValuePage_not_for_this_field;
		}

		protected QueryField getDetailField(Object element) {
			QueryField qfield = null;
			if (element instanceof QueryField)
				qfield = (QueryField) element;
			else if (element instanceof QueryField[])
				qfield = ((QueryField[]) element)[1];
			return qfield;
		}

		public QueryField getSelection() {
			Object element = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
			QueryField qfield = getDetailField(element);
			return qfield != null && qfield.isReportField() ? qfield : null;
		}

		public QueryField getDetailParent() {
			Object element = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
			if (element instanceof QueryField[])
				return ((QueryField[]) element)[0];
			return null;
		}

		public void setSelection(String field) {
			QueryField[] qpath = QueryField.findQuerySubField(field);
			if (qpath != null)
				viewer.setSelection(new StructuredSelection(qpath));
		}

	}

	private static final long HALFYEAR = 4386 * 3600000L;
	private static final long TWOYEARS = HALFYEAR * 4;
	private static final long TENYEARS = HALFYEAR * 20;
	private static final Object[] EMPTYOBJECTS = new Object[0];
	private int mode;
	private Composite daytimeComposite;
	private StackLayout stackLayout;
	private Composite timeComposite;
	private DateTime timeFromField;
	private DateTime timeToField;
	private Composite numericComposite;
	private ViewerComp numericViewer;
	private Composite discreteComposite;
	private ViewerComp discreteViewer;
	private Composite stack;
	private RangeControl rangeSlider;
	private NumericControl intervalField;
	private Label rangeLabel;
	private Report report;
	private CheckboxTableViewer filterViewer;
	private Label hiddenLabel;
	private AllNoneGroup allNoneGroup;
	private NumericControl thresholdField;
	private Label thresholdLabel;
	private RadioButtonGroup dayTimeButtonGroup;
	private RadioButtonGroup timeButtonGroup;

	public ValuePage(String id, String title, String msg, ImageDescriptor imageDescriptor) {
		super(id, title, imageDescriptor);
		setMessage(msg);
	}

	@Override
	public void createControl(Composite parent) {
		stack = new Composite(parent, SWT.NONE);
		stack.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		stackLayout = new StackLayout();
		stack.setLayout(stackLayout);
		setControl(stack);
		setHelp(HelpContextIds.REPORT_WIZARD);
		daytimeComposite = new Composite(stack, SWT.NONE);
		daytimeComposite.setLayout(new GridLayout());
		Label label = new Label(daytimeComposite, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		label.setFont(JFaceResources.getBannerFont());
		label.setText(Messages.ValuePage_time_of_day);
		dayTimeButtonGroup = new RadioButtonGroup(daytimeComposite, Messages.ValuePage_intervals, SWT.NONE, Messages.ValuePage_hours, Messages.ValuePage_fifteen_min);
		GridData data = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		data.verticalIndent = 20;
		dayTimeButtonGroup.setLayoutData(data);
		dayTimeButtonGroup.addSelectionListener(this);
		dayTimeButtonGroup.setSelection(0);
		timeComposite = new Composite(stack, SWT.NONE);
		timeComposite.setLayout(new GridLayout(4, false));
		label = new Label(timeComposite, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 4, 1));
		label.setFont(JFaceResources.getBannerFont());
		label.setText(Messages.ValuePage_calTime);
		label = new Label(timeComposite, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		label.setText(Messages.ValuePage_from);
		timeFromField = new DateTime(timeComposite, SWT.CALENDAR | SWT.SHORT);
		timeFromField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		label = new Label(timeComposite, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		label.setText(Messages.ValuePage_to);
		timeToField = new DateTime(timeComposite, SWT.CALENDAR | SWT.SHORT);
		timeToField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		timeFromField.setDate(timeToField.getYear() - 4, 0, 1);
		timeFromField.addSelectionListener(this);
		timeToField.addSelectionListener(this);
		GregorianCalendar cal = new GregorianCalendar();
		timeToField.setDate(cal.get(GregorianCalendar.YEAR), cal.get(GregorianCalendar.MONTH) + 1,
				cal.get(GregorianCalendar.DAY_OF_MONTH));
		timeButtonGroup = new RadioButtonGroup(timeComposite, Messages.ValuePage_interval, SWT.NONE, Messages.ValuePage_by_year, Messages.ValuePage_by_quarter, Messages.ValuePage_by_month,
				Messages.ValuePage_by_week, Messages.ValuePage_by_day);
		timeButtonGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		timeButtonGroup.addSelectionListener(this);
		timeButtonGroup.setSelection(0);
		numericComposite = new Composite(stack, SWT.NONE);
		numericComposite.setLayout(new GridLayout(1, false));
		label = new Label(numericComposite, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		label.setFont(JFaceResources.getBannerFont());
		label.setText(Messages.ValuePage_numeric);
		numericViewer = createFieldViewer(numericComposite);
		Composite rangeComp = new Composite(numericComposite, SWT.NONE);
		rangeComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		rangeComp.setLayout(new GridLayout(3, false));
		rangeLabel = new Label(rangeComp, SWT.NONE);
		rangeLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

		label = new Label(rangeComp, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		label.setText(Messages.ValuePage_range);
		rangeSlider = new RangeControl(rangeComp, SWT.BORDER);
		rangeSlider.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 2, 1));
		rangeSlider.setSelection(new Point(25, 75));
		rangeSlider.addSelectionListener(this);
		label = new Label(rangeComp, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		label.setText(Messages.ValuePage_intervals);
		intervalField = new NumericControl(rangeComp, SWT.NONE);
		intervalField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, true, 2, 1));
		intervalField.setMinimum(2);
		intervalField.setMaximum(150);
		intervalField.setSelection(50);
		intervalField.addSelectionListener(this);
		discreteComposite = new Composite(stack, SWT.NONE);
		discreteComposite.setLayout(new GridLayout(1, false));
		label = new Label(discreteComposite, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		label.setFont(JFaceResources.getBannerFont());
		label.setText(Messages.ValuePage_discrete);
		discreteViewer = createFieldViewer(discreteComposite);
		Composite spinnerComp = new Composite(discreteComposite, SWT.NONE);
		spinnerComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		spinnerComp.setLayout(new GridLayout(2, false));
		thresholdLabel = new Label(spinnerComp, SWT.NONE);
		thresholdLabel.setText(Messages.ValuePage_threshold);

		thresholdField = new NumericControl(spinnerComp, SWT.BORDER);
		thresholdField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		thresholdField.setMaximum(50);
		thresholdField.setIncrement(1);
		thresholdField.setDigits(1);
		thresholdField.setSelection(10);
		thresholdField.addSelectionListener(this);

		hiddenLabel = new Label(discreteComposite, SWT.NONE);
		hiddenLabel.setText(Messages.ValuePage_hidden);
		Composite filterComp = new Composite(discreteComposite, SWT.NONE);
		filterComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		filterComp.setLayout(new GridLayout(2, false));

		filterViewer = CheckboxTableViewer.newCheckList(filterComp, SWT.BORDER);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.heightHint = 200;
		layoutData.widthHint = 200;
		filterViewer.getControl().setLayoutData(layoutData);
		filterViewer.setContentProvider(ArrayContentProvider.getInstance());
		filterViewer.setLabelProvider(ZColumnLabelProvider.getDefaultInstance());
		allNoneGroup = new AllNoneGroup(filterComp, new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				filterViewer.setAllChecked(e.widget.getData() == AllNoneGroup.ALL);
			}
		});
		setInput();
		super.createControl(parent);
	}

	protected void setInput() {
		numericViewer.setInput();
		discreteViewer.setInput();
	}

	protected ViewerComp createFieldViewer(Composite parent) {
		ViewerComp viewer = new ViewerComp(parent);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateFields();
				validatePage();
			}
		});
		return viewer;
	}

	protected void updateFields() {
		if (stackLayout.topControl == daytimeComposite) {
			report.setField(QueryField.TIMEOFDAY.getKey());
		} else if (stackLayout.topControl == timeComposite) {
			GregorianCalendar toCal = new GregorianCalendar(timeToField.getYear(), timeToField.getMonth(),
					timeToField.getDay());
			GregorianCalendar fromCal = new GregorianCalendar(timeFromField.getYear(), timeFromField.getMonth(),
					timeFromField.getDay());
			long interval = toCal.getTimeInMillis() - fromCal.getTimeInMillis();
			if (interval > TENYEARS) {
				timeButtonGroup.setEnabled(false);
				timeButtonGroup.setEnabled(0, true);
				timeButtonGroup.setEnabled(1, true);
				if (timeButtonGroup.getSelection() != 1)
					timeButtonGroup.setSelection(0);
			} else if (interval > TWOYEARS) {
				timeButtonGroup.setEnabled(true);
				timeButtonGroup.setEnabled(3, false);
				timeButtonGroup.setEnabled(4, false);
				int selection = timeButtonGroup.getSelection();
				if (selection != 2 && selection != 0)
					timeButtonGroup.setSelection(1);
			} else if (interval > HALFYEAR) {
				timeButtonGroup.setEnabled(true);
				timeButtonGroup.setEnabled(0, false);
				timeButtonGroup.setEnabled(4, false);
				if (timeButtonGroup.getSelection() == 4)
					timeButtonGroup.setSelection(2);
			} else {
				timeButtonGroup.setEnabled(false);
				timeButtonGroup.setEnabled(3, true);
				timeButtonGroup.setEnabled(4, true);
				int selection = timeButtonGroup.getSelection();
				if (selection == 0)
					timeButtonGroup.setSelection(3);
			}
			report.setField(QueryField.DATE.getKey());
		} else if (stackLayout.topControl == numericComposite) {
			QueryField qfield = numericViewer.getSelection();
			rangeSlider.setEnabled(qfield != null);
			if (qfield != null) {
				float maxValue = qfield.getMaxValue();
				if (!Float.isNaN(maxValue)) {
					boolean log = (qfield.getAutoPolicy() & QueryField.AUTO_LOG) != 0 || Float.isInfinite(maxValue);
					rangeLabel.setText(NLS.bind(log ? Messages.ValuePage_logar : Messages.ValuePage_linear, qfield.getLabel()));
					rangeSlider.setLogrithmic(log);
					int intMaxVlaue = Float.isFinite(maxValue) ? (int) maxValue : 1000000;
					int type = qfield.getType();
					if (type == QueryField.T_CURRENCY) {
						rangeSlider.setDigits(0);
						rangeSlider.setMinimum(0);
						rangeSlider.setMaximum(100000);
					} else if (type == QueryField.T_FLOAT || type == QueryField.T_FLOATB
							|| type == QueryField.T_POSITIVEFLOAT) {
						int digits = Math.max(0, qfield.getMaxlength() - 2);
						int factor = (int) Math.pow(10d, digits);
						rangeSlider.setDigits(digits);
						rangeSlider.setMinimum(type == QueryField.T_POSITIVEFLOAT ? 0 : -intMaxVlaue * factor);
						rangeSlider.setMaximum(intMaxVlaue * factor);
					} else if (type == QueryField.T_INTEGER) {
						rangeSlider.setMaximum(intMaxVlaue);
						rangeSlider.setMinimum(-intMaxVlaue);
						rangeSlider.setDigits(0);
					} else if (type == QueryField.T_POSITIVEINTEGER) {
						rangeSlider.setMaximum(intMaxVlaue);
						rangeSlider.setMinimum(0);
						rangeSlider.setDigits(0);
					}
					numericViewer.getParent().layout(true, true);
					updateInterval();
					QueryField detailParent = numericViewer.getDetailParent();
					report.setField(
							detailParent == null ? qfield.getKey() : detailParent.getKey() + '&' + qfield.getKey());
				}
			} else
				rangeLabel.setText(""); //$NON-NLS-1$
		} else if (stackLayout.topControl == discreteComposite) {
			QueryField qfield = discreteViewer.getSelection();
			if (qfield != null) {
				QueryField detailParent = discreteViewer.getDetailParent();
				report.setField(detailParent == null ? qfield.getKey() : detailParent.getKey() + '&' + qfield.getKey());
				thresholdLabel.setVisible(true);
				thresholdField.setVisible(true);
				String[] enumLabels = qfield.getEnumLabels();
				if (enumLabels == null) {
					Object enumeration = qfield.getEnumeration();
					if (enumeration instanceof String[])
						enumLabels = (String[]) enumeration;
				}
				if (enumLabels != null) {
					filterViewer.setInput(enumLabels);
					hiddenLabel.setVisible(true);
					filterViewer.getControl().setVisible(true);
					allNoneGroup.setVisible(true);
				} else {
					hiddenLabel.setVisible(false);
					filterViewer.getControl().setVisible(false);
					allNoneGroup.setVisible(false);
				}
			} else {
				hiddenLabel.setVisible(false);
				filterViewer.getControl().setVisible(false);
				thresholdLabel.setVisible(false);
				thresholdField.setVisible(false);
				allNoneGroup.setVisible(false);
			}
		}
		updateInterval();
	}

	@Override
	protected void validatePage() {
		String errorMessage = null;
		if (stackLayout.topControl == numericComposite)
			errorMessage = numericViewer.checkSelection();
		else if (stackLayout.topControl == discreteComposite)
			errorMessage = discreteViewer.checkSelection();
		setErrorMessage(errorMessage);
		setPageComplete(errorMessage == null);
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			report = ((ReportWizard)getWizard()).getReport();
			mode = report.getMode();
			if ((mode & ReportWizard.DAYTIME) != 0) {
				stackLayout.topControl = daytimeComposite;
				if (report.getDayInterval() == 96) {
					dayTimeButtonGroup.setSelection(1);
				} else {
					dayTimeButtonGroup.setSelection(0);
					report.setDayInterval(24);
				}
			} else if ((mode & ReportWizard.TIME) != 0) {
				stackLayout.topControl = timeComposite;
				if (report.getTimeLower() != report.getTimeUpper()) {
					GregorianCalendar cal = new GregorianCalendar();
					cal.setTimeInMillis(report.getTimeLower());
					timeFromField.setDate(cal.get(GregorianCalendar.YEAR), cal.get(GregorianCalendar.MONTH) + 1,
							cal.get(GregorianCalendar.DAY_OF_MONTH));
					cal.setTimeInMillis(report.getTimeUpper());
					timeToField.setDate(cal.get(GregorianCalendar.YEAR), cal.get(GregorianCalendar.MONTH) + 1,
							cal.get(GregorianCalendar.DAY_OF_MONTH));
				}
				int interval = report.getTimeInterval();
				if (interval > 0)
					timeButtonGroup.setSelection(interval == ReportWizard.T_YEAR ? 0
							: interval == ReportWizard.T_QUARTER ? 1
									: interval == ReportWizard.T_MONTH ? 2 : interval == ReportWizard.T_WEEK ? 3 : 4);
			} else if ((mode & ReportWizard.NUMERIC) != 0) {
				stackLayout.topControl = numericComposite;
				if (report.getValueLower() != report.getValueUpper())
					rangeSlider.setSelection(new Point((int) report.getValueLower(), (int) report.getValueUpper()));
				if (report.getValueInterval() > 0)
					intervalField.setSelection(report.getValueInterval());
				if (report.getField() != null)
					numericViewer.setSelection(report.getField());
			} else if ((mode & ReportWizard.DISCRETE) != 0) {
				stackLayout.topControl = discreteComposite;
				String[] filter = report.getFilter();
				filterViewer.setCheckedElements(filter);
				thresholdField.setSelection((int) (report.getThreshold() * 10 + 0.5f));
				if (report.getField() != null)
					discreteViewer.setSelection(report.getField());
			}
			updateFields();
			setInput();
			stack.layout(true, true);
			validatePage();
		}
		super.setVisible(visible);
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		updateFields();
	}

	private void updateInterval() {
		if (stackLayout.topControl == daytimeComposite) {
			report.setDayInterval(dayTimeButtonGroup.getSelection() == 0 ? 24 : 96);
		} else if (stackLayout.topControl == timeComposite) {
			GregorianCalendar toCal = new GregorianCalendar(timeToField.getYear(), timeToField.getMonth(),
					timeToField.getDay());
			GregorianCalendar fromCal = new GregorianCalendar(timeFromField.getYear(), timeFromField.getMonth(),
					timeFromField.getDay());
			report.setTimeLower(fromCal.getTimeInMillis());
			report.setTimeUpper(toCal.getTimeInMillis());
			int selection = timeButtonGroup.getSelection();
			report.setTimeInterval(selection == 0 ? ReportWizard.T_YEAR
					: selection == 1 ? ReportWizard.T_QUARTER
							: selection == 2 ? ReportWizard.T_MONTH
									: selection == 3 ? ReportWizard.T_WEEK : ReportWizard.T_DAY);
		} else if (stackLayout.topControl == numericComposite) {
			Point selection = rangeSlider.getSelection();
			report.setValueLower(selection.x);
			report.setValueUpper(selection.y);
			report.setValueInterval(intervalField.getSelection());
		} else if (stackLayout.topControl == discreteComposite) {
			int mode = report.getMode();
			if (filterViewer.getControl().isVisible()) {
				Object[] checkedElements = filterViewer.getCheckedElements();
				String[] filter = new String[checkedElements.length];
				for (int i = 0; i < checkedElements.length; i++)
					filter[i] = (String) checkedElements[i];
				report.setFilter(filter);
			}
			report.setThreshold(thresholdField.getSelection() * 0.1f);
			report.setMode(mode);
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// do nothing
	}

}
