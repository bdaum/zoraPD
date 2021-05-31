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
 * (c) 2017 Berthold Daum  
 */
package com.bdaum.zoom.report.internal.wizards;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.CategoryStepRenderer;
import org.jfree.chart.swt.ChartComposite;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.Month;
import org.jfree.data.time.Quarter;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.Week;
import org.jfree.data.time.Year;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.Rotation;
import org.jfree.util.TableOrder;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.report.Report;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.report.internal.ReportActivator;
import com.bdaum.zoom.report.internal.jfree.custom.CylinderRenderer;
import com.bdaum.zoom.report.internal.jfree.custom.SparseCategoryAxis;

public class ReportComponent extends Composite {

	private static final String VALUE = Messages.ReportComponent_value;
	private static final long ONEDAY = 86400000L;
	private static final String OTHERS = Messages.ReportComponent_others;
	private static final String EARNINGS = Messages.ReportComponent_earnings;
	private static final String SALES = Messages.ReportComponent_sales;
	private static final String COUNT = Messages.ReportComponent_count;
	// Propertys keys
	private static final String TITLE = "title"; //$NON-NLS-1$
	private static final String TITLEFONT = "titleFont"; //$NON-NLS-1$
	private static final String TITLECOLOR = "titleColor"; //$NON-NLS-1$
	private static final String BGCOLOR = "bgColor"; //$NON-NLS-1$
	private static final String OUTLINEPAINT = "outlinePaint"; //$NON-NLS-1$
	private static final String OUTLINESTROKE = "outlineStroke"; //$NON-NLS-1$
	private static final String ORIENTATION = "orientation"; //$NON-NLS-1$
	private static final String AXISLABEL = "AxisLabel"; //$NON-NLS-1$
	private static final String AXISLABELFONT = "AxisLabelFont"; //$NON-NLS-1$
	private static final String AXISLABELPAINT = "AxisLabelPaint"; //$NON-NLS-1$
	private static final String TICKMARKSVISIBLE = "TickmarksVisble"; //$NON-NLS-1$
	private static final String TICKLABELSVISIBLE = "TicklabelsVisible"; //$NON-NLS-1$
	private static final String TICKLABELFONT = "TicklabelFont"; //$NON-NLS-1$
	private static final String TICKLABELPAINT = "TicklabelPaint"; //$NON-NLS-1$
	private static final String ANTIALIAS = "antialias"; //$NON-NLS-1$
	private static final String CANVASPAINT = "canvasPaint"; //$NON-NLS-1$

	public class Cumulated {

		private int count;
		private int sales;
		private double earnings;

		public Cumulated(int count, int sales, double earnings) {
			this.count = count;
			this.sales = sales;
			this.earnings = earnings;
		}

		public void count() {
			++count;
		}

		public void addSales(int sales) {
			this.sales += sales;
		}

		public void addEarnings(double earnings) {
			this.earnings += earnings;
		}

		public int getCount() {
			return count;
		}

		public int getSales() {
			return sales;
		}

		public double getEarnings() {
			return earnings;
		}

	}

	public class ReportJob extends Job {

		private Report report;
		private ChartComposite target;
		private int preview;
		private int interval;
		private long lower;
		private long upper;
		private long range;
		private QueryField[] qfields;
		private int mode;
		private boolean doCount;
		private boolean doSales;
		private boolean doEarnings;
		private SimpleDateFormat df;

		public ReportJob(Report report, ChartComposite target, int preview) {
			super(Messages.ReportComponent_generator);
			this.report = report;
			this.target = target;
			this.preview = preview;
			mode = report.getMode();
			if ((mode & ReportWizard.DAYTIME) != 0) {
				lower = 0;
				upper = 96;
				interval = report.getDayInterval();
				df = new SimpleDateFormat(interval == 96 ? Messages.ReportComponent_Hmm : Messages.ReportComponent_H);
				df.setTimeZone(TimeZone.getTimeZone("Z")); //$NON-NLS-1$
			} else if ((mode & ReportWizard.TIME) != 0) {
				lower = report.getTimeLower();
				upper = report.getTimeUpper();
				interval = report.getTimeInterval();
			} else {
				lower = report.getValueLower();
				upper = report.getValueUpper();
				interval = report.getValueInterval();
			}
			range = upper - lower;
			qfields = resolveField(report);
			doCount = (mode & ReportWizard.IMAGECOUNT) != 0;
			doSales = (mode & ReportWizard.SALES) != 0;
			doEarnings = (mode & ReportWizard.EARNINGS) != 0;

			setSystem(true);
			setPriority(Job.LONG);
		}

		@Override
		public boolean belongsTo(Object family) {
			return family == ReportComponent.this;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			IProgressMonitor monitorWrapper = new IProgressMonitor() {

				public void worked(int work) {
					monitor.worked(work);
					ReportComponent.this.worked(work);
				}

				public void subTask(String name) {
					monitor.subTask(name);
				}

				public void setTaskName(String name) {
					monitor.setTaskName(name);
				}

				public void setCanceled(boolean value) {
					monitor.setCanceled(value);
				}

				public boolean isCanceled() {
					return monitor.isCanceled();
				}

				public void internalWorked(double work) {
					monitor.internalWorked(work);
				}

				public void done() {
					monitor.done();
					ReportComponent.this.endTask();
				}

				public void beginTask(String name, int totalWork) {
					monitor.beginTask(name, totalWork);
					ReportComponent.this.beginTask(totalWork);
				}
			};
			return runJob(monitorWrapper);
		}

		private IStatus runJob(IProgressMonitor monitor) {
			final Dataset dataset = createDataset(monitor);
			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;
			JFreeChart chart = createChart(dataset);
			if (!target.isDisposed())
				target.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						target.setChart(chart);
						target.forceRedraw();
					}
				});
			monitor.done();
			return Status.OK_STATUS;
		}

		private QueryField[] resolveField(Report report) {
			String field = report.getField();
			if (field == null)
				return null;
			return QueryField.findQuerySubField(field);
		}

		private Dataset createDataset(IProgressMonitor monitor) {
			Dataset result = null;
			if ((mode & ReportWizard.PIE) != 0 && !ReportWizard.isMultiple(mode)) {
				result = new DefaultPieDataset();
				collectDiscreteValues((DefaultPieDataset) result, monitor);
			} else if ((mode & ReportWizard.DISCRETE) != 0) {
				result = new DefaultCategoryDataset();
				collectDiscreteValues((DefaultCategoryDataset) result, monitor);
			} else if ((mode & ReportWizard.DAYTIME) != 0) {
				result = new DefaultCategoryDataset();
				collectDayValues((DefaultCategoryDataset) result, monitor);
			} else if ((mode & ReportWizard.TIME) != 0) {
				result = new TimeSeriesCollection();
				collectTimeValues((TimeSeriesCollection) result, monitor);
			} else {
				result = new DefaultCategoryDataset();
				collectNumericValues((DefaultCategoryDataset) result, monitor);
			}
			return result;
		}

		private void collectDayValues(DefaultCategoryDataset result, IProgressMonitor monitor) {
			List<Asset> set = fetchAssets(report, preview);
			if (set != null) {
				int size = set.size();
				int incr = Math.max(1, size / 100);
				monitor.beginTask(Messages.ReportComponent_collecting, size);
				int[] counts = new int[interval];
				int[] sales = new int[interval];
				double[] earnings = new double[interval];
				int div = 1440 / interval;
				int cnt = 0;
				for (Asset asset : set) {
					Integer minutes = (Integer) QueryField.TIMEOFDAY.obtainFieldValue(asset);
					if (minutes != null) {
						int i = minutes / div;
						if (doCount)
							++counts[i];
						if (doSales)
							sales[i] += asset.getSales();
						if (doEarnings)
							earnings[i] += asset.getEarnings();
						if (++cnt % incr == 0)
							monitor.worked(incr);
						if (cnt > preview)
							break;
					}
				}
				cumulate(counts);
				cumulate(sales);
				cumulate(earnings);
				for (int i = 0; i < earnings.length; i++) {
					String label;
					if (interval == 24)
						label = String.valueOf(i);
					else if (i % 4 == 0)
						label = String.valueOf(i / 4);
					else
						label = "_" + i; //$NON-NLS-1$
					if (doCount)
						result.addValue((Integer) counts[i], COUNT, label);
					if (doSales)
						result.addValue((Integer) sales[i], SALES, label);
					if (doEarnings)
						result.addValue(earnings[i], EARNINGS, label);
				}
			}
		}

		private void collectTimeValues(TimeSeriesCollection result, IProgressMonitor monitor) {
			List<Asset> set = fetchAssets(report, preview);
			if (set != null) {
				int size = set.size();
				int incr = Math.max(1, size / 100);
				monitor.beginTask(Messages.ReportComponent_collecting, size);
				GregorianCalendar lowerCal = new GregorianCalendar();
				lowerCal.setTimeInMillis(lower);
				lowerCal.set(GregorianCalendar.HOUR, 0);
				lowerCal.set(GregorianCalendar.MINUTE, 0);
				lowerCal.set(GregorianCalendar.SECOND, 0);
				lowerCal.set(GregorianCalendar.MILLISECOND, 0);

				GregorianCalendar upperCal = new GregorianCalendar();
				upperCal.setTimeInMillis(upper);
				upperCal.set(GregorianCalendar.HOUR, 0);
				upperCal.set(GregorianCalendar.MINUTE, 0);
				upperCal.set(GregorianCalendar.SECOND, 0);
				upperCal.set(GregorianCalendar.MILLISECOND, 0);

				int startYear = 0;
				int startQuarter = 0;
				int startMonth = 0;
				long lowerday = 0;
				long lowerWeek = 0;
				int dim;
				switch (interval) {
				case ReportWizard.T_YEAR:
					startYear = lowerCal.get(GregorianCalendar.YEAR);
					dim = upperCal.get(GregorianCalendar.YEAR) - startYear + 1;
					lowerCal.set(GregorianCalendar.DAY_OF_YEAR, 1);
					break;
				case ReportWizard.T_QUARTER:
					startQuarter = lowerCal.get(GregorianCalendar.MONTH) / 3 + lowerCal.get(GregorianCalendar.YEAR) * 4;
					int uq = upperCal.get(GregorianCalendar.MONTH) / 3 + upperCal.get(GregorianCalendar.YEAR) * 4;
					dim = uq - startQuarter + 1;
					lowerCal.set(GregorianCalendar.DAY_OF_MONTH, 1);
					int m = lowerCal.get(GregorianCalendar.MONTH);
					lowerCal.add(GregorianCalendar.MONTH, -(m % 3));
					break;
				case ReportWizard.T_MONTH:
					startMonth = lowerCal.get(GregorianCalendar.MONTH) + lowerCal.get(GregorianCalendar.YEAR) * 12;
					int um = upperCal.get(GregorianCalendar.MONTH) + upperCal.get(GregorianCalendar.YEAR) * 12;
					dim = um - startMonth + 1;
					lowerCal.set(GregorianCalendar.DAY_OF_MONTH, 1);
					break;
				case ReportWizard.T_WEEK:
					int firstDay = lowerCal.get(GregorianCalendar.DAY_OF_WEEK);
					int weekStart = lowerCal.getFirstDayOfWeek();
					int adjust = (firstDay - weekStart + 7) % 7;
					lowerCal.add(GregorianCalendar.DAY_OF_YEAR, -adjust);
					lowerWeek = lowerCal.getTimeInMillis();

					firstDay = upperCal.get(GregorianCalendar.DAY_OF_WEEK);
					weekStart = upperCal.getFirstDayOfWeek();
					adjust = (firstDay - weekStart + 7) % 7;
					upperCal.add(GregorianCalendar.DAY_OF_YEAR, -adjust);
					long diff = upperCal.getTimeInMillis() - lowerWeek;
					dim = (int) (diff / (7 * ONEDAY)) + 1;
					break;
				default:
					lowerCal.set(GregorianCalendar.HOUR, 0);
					lowerCal.set(GregorianCalendar.MINUTE, 0);
					lowerCal.set(GregorianCalendar.SECOND, 0);
					lowerCal.set(GregorianCalendar.MILLISECOND, 0);
					lowerday = lowerCal.getTimeInMillis();
					upperCal.set(GregorianCalendar.HOUR, 0);
					upperCal.set(GregorianCalendar.MINUTE, 0);
					upperCal.set(GregorianCalendar.SECOND, 0);
					upperCal.set(GregorianCalendar.MILLISECOND, 0);
					diff = upperCal.getTimeInMillis() - lowerday;
					dim = (int) (diff / ONEDAY) + 1;
					break;
				}
				int vmode = report.getMode() & ReportWizard.ALLVALUES;
				int[] counts = new int[dim];
				int[] sales = new int[dim];
				double[] earnings = new double[dim];
				boolean doCount = (vmode & ReportWizard.IMAGECOUNT) != 0;
				boolean doSales = (vmode & ReportWizard.SALES) != 0;
				boolean doEarnings = (vmode & ReportWizard.EARNINGS) != 0;
				GregorianCalendar cal = new GregorianCalendar();
				int cnt = 0;
				for (Asset asset : set) {
					Date date = (Date) QueryField.DATE.obtainFieldValue(asset);
					if (date != null) {
						cal.setTime(date);
						if (cal.compareTo(lowerCal) >= 0 && cal.compareTo(upperCal) < 0) {
							int i;
							switch (interval) {
							case ReportWizard.T_YEAR:
								i = cal.get(GregorianCalendar.YEAR) - startYear;
								break;
							case ReportWizard.T_QUARTER:
								int uq = cal.get(GregorianCalendar.MONTH) / 3 + cal.get(GregorianCalendar.YEAR) * 4;
								i = uq - startQuarter;
								break;
							case ReportWizard.T_MONTH:
								int um = cal.get(GregorianCalendar.MONTH) + cal.get(GregorianCalendar.YEAR) * 12;
								i = um - startMonth;
								break;
							case ReportWizard.T_WEEK:
								cal.set(GregorianCalendar.HOUR, 0);
								cal.set(GregorianCalendar.MINUTE, 0);
								cal.set(GregorianCalendar.SECOND, 0);
								cal.set(GregorianCalendar.MILLISECOND, 0);
								int firstDay = cal.get(GregorianCalendar.DAY_OF_WEEK);
								int weekStart = cal.getFirstDayOfWeek();
								int adjust = (firstDay - weekStart + 7) % 7;
								cal.add(GregorianCalendar.DAY_OF_YEAR, -adjust);
								long diff = cal.getTimeInMillis() - lowerWeek;
								i = (int) (diff / (7 * ONEDAY));
								break;
							default:
								cal.set(GregorianCalendar.HOUR, 0);
								cal.set(GregorianCalendar.MINUTE, 0);
								cal.set(GregorianCalendar.SECOND, 0);
								cal.set(GregorianCalendar.MILLISECOND, 0);
								diff = cal.getTimeInMillis() - lowerday;
								i = (int) (diff / ONEDAY);
								break;
							}
							if (doCount)
								++counts[i];
							if (doSales)
								sales[i] += asset.getSales();
							if (doEarnings)
								earnings[i] += asset.getEarnings();
						}
					}
					if (++cnt % incr == 0)
						monitor.worked(incr);
					if (cnt > preview)
						break;
				}
				cumulate(counts);
				cumulate(sales);
				cumulate(earnings);
				TimeSeries countSeries = doCount ? new TimeSeries(COUNT) : null;
				TimeSeries salesSeries = doSales ? new TimeSeries(SALES) : null;
				TimeSeries earningsSeries = doEarnings ? new TimeSeries(EARNINGS) : null;
				for (int i = 0; i < earnings.length; i++) {
					Date time = lowerCal.getTime();
					RegularTimePeriod period;
					switch (interval) {
					case ReportWizard.T_YEAR:
						period = new Year(time);
						lowerCal.add(GregorianCalendar.YEAR, 1);
						break;
					case ReportWizard.T_QUARTER:
						period = new Quarter(time);
						lowerCal.add(GregorianCalendar.MONTH, 3);
						break;
					case ReportWizard.T_MONTH:
						period = new Month(time);
						lowerCal.add(GregorianCalendar.MONTH, 1);
						break;
					case ReportWizard.T_WEEK:
						period = new Week(time);
						lowerCal.add(GregorianCalendar.WEEK_OF_YEAR, 1);
						break;
					default:
						period = new Day(time);
						lowerCal.add(GregorianCalendar.DAY_OF_YEAR, 1);
						break;
					}
					if (countSeries != null)
						countSeries.add(period, counts[i]);
					if (salesSeries != null)
						salesSeries.add(period, sales[i]);
					if (earningsSeries != null)
						earningsSeries.add(period, earnings[i]);
				}
				if (countSeries != null)
					result.addSeries(countSeries);
				if (salesSeries != null)
					result.addSeries(salesSeries);
				if (earningsSeries != null)
					result.addSeries(earningsSeries);
			}
		}

		private void collectDiscreteValues(DefaultPieDataset result, IProgressMonitor monitor) {
			if (qfields == null)
				return;
			Map<String, Cumulated> histMap = collectValues(monitor);
			Cumulated others = computeOthers(histMap);
			int vmode = mode & ReportWizard.ALLVALUES;
			String[] enumLabels = computeDiscreteLabels(histMap, others);
			for (String key : enumLabels) {
				Cumulated cumulated = histMap.get(key);
				if (cumulated != null) {
					switch (vmode) {
					case ReportWizard.IMAGECOUNT:
						result.setValue(key, cumulated.getCount());
						break;
					case ReportWizard.SALES:
						result.setValue(key, cumulated.getSales());
						break;
					case ReportWizard.EARNINGS:
						result.setValue(key, cumulated.getEarnings());
						break;
					}
				}
			}
		}

		private void collectNumericValues(DefaultCategoryDataset result, IProgressMonitor monitor) {
			if (qfields == null)
				return;
			List<Asset> set = fetchAssets(report, preview);
			if (set != null) {
				int size = set.size();
				int incr = Math.max(1, size / 100);
				monitor.beginTask(Messages.ReportComponent_collecting, size);
				int[] counts = new int[interval];
				int[] sales = new int[interval];
				double[] earnings = new double[interval];
				boolean day = (mode & ReportWizard.DAYTIME) != 0;
				int i;
				int cnt = 0;
				for (Asset asset : set) {
					Object value = QueryField.obtainFieldValue(asset, qfields[0], qfields[1]);
					if (value instanceof Integer) {
						int v = (Integer) value;
						if (v >= lower && v < upper) {
							i = (int) (((v - lower) * interval) / range);
							if (doCount)
								++counts[i];
							if (doSales)
								sales[i] += asset.getSales();
							if (doEarnings)
								earnings[i] += asset.getEarnings();
						}
					} else if (value instanceof Double) {
						double v = (Double) value;
						if (v >= lower && v < upper) {
							i = (int) (((v - lower) * interval) / range);
							if (doCount)
								++counts[i];
							if (doSales)
								sales[i] += asset.getSales();
							if (doEarnings)
								earnings[i] += asset.getEarnings();
						}
					}
					if (++cnt % incr == 0)
						monitor.worked(incr);
					if (cnt > preview)
						break;

				}
				cumulate(counts);
				cumulate(sales);
				cumulate(earnings);
				for (i = 0; i < earnings.length; i++) {
					String label = day ? df.format(i * ONEDAY / interval)
							: String.valueOf((int) ((i * range + interval / 2) / interval + lower));
					if (doCount)
						result.addValue((Integer) counts[i], COUNT, label);
					if (doSales)
						result.addValue((Integer) sales[i], SALES, label);
					if (doEarnings)
						result.addValue(earnings[i], EARNINGS, label);
				}
			}
		}

		private String[] computeDiscreteLabels(Map<String, Cumulated> histMap, Cumulated others) {
			String[] enumLabels = qfields[1].getEnumLabels();
			boolean nameSort = false;
			if (enumLabels == null) {
				nameSort = true;
				Set<String> keySet = histMap.keySet();
				enumLabels = keySet.toArray(new String[keySet.size()]);
			}
			if (others != null) {
				String[] newLabels = new String[enumLabels.length + 1];
				System.arraycopy(enumLabels, 0, newLabels, 0, enumLabels.length);
				newLabels[enumLabels.length] = OTHERS;
				enumLabels = newLabels;
				histMap.put(OTHERS, others);
			}
			int sortField = report.getSortField();
			boolean descending = report.getDescending();
			if (sortField == 0 && nameSort) {
				sortField = ReportWizard.NAME;
				descending = false;
			}
			if (sortField != 0) {
				final int field = sortField;
				final boolean desc = descending;
				Arrays.sort(enumLabels, new Comparator<String>() {
					@Override
					public int compare(String s1, String s2) {
						if ((field & ReportWizard.NAME) != 0)
							return desc ? s2.compareTo(s1) : s1.compareTo(s2);
						Cumulated c1 = histMap.get(s1);
						Cumulated c2 = histMap.get(s2);
						int result = 0;
						if ((field & ReportWizard.SALES) != 0) {
							int sales1 = c1 == null ? 0 : c1.sales;
							int sales2 = c2 == null ? 0 : c2.sales;
							result = sales1 == sales2 ? 0 : sales1 < sales2 ? -1 : 1;
						} else if ((field & ReportWizard.EARNINGS) != 0) {
							double earnings1 = c1 == null ? 0d : c1.earnings;
							double earnings2 = c2 == null ? 0d : c2.earnings;
							result = earnings1 == earnings2 ? 0 : earnings1 < earnings2 ? -1 : 1;
						} else {
							int count1 = c1 == null ? 0 : c1.count;
							int count2 = c2 == null ? 0 : c2.count;
							result = count1 == count2 ? 0 : count1 < count2 ? -1 : 1;
						}
						return desc ? -result : result;
					}
				});
			}
			return enumLabels;
		}

		private void collectDiscreteValues(DefaultCategoryDataset result, IProgressMonitor monitor) {
			if (qfields == null)
				return;
			Map<String, Cumulated> histMap = collectValues(monitor);
			Cumulated others = computeOthers(histMap);
			String[] enumLabels = computeDiscreteLabels(histMap, others);
			for (String key : enumLabels) {
				Cumulated cumulated = histMap.get(key);
				if (cumulated != null) {
					if ((mode & ReportWizard.IMAGECOUNT) != 0)
						result.setValue(cumulated.getCount(), COUNT, key);
					if ((mode & ReportWizard.SALES) != 0)
						result.setValue(cumulated.getSales(), SALES, key);
					if ((mode & ReportWizard.EARNINGS) != 0)
						result.setValue(cumulated.getEarnings(), EARNINGS, key);
				}
			}
		}

		protected Map<String, Cumulated> collectValues(IProgressMonitor monitor) {
			Map<String, Cumulated> histMap = new HashMap<>(50);
			String[] filter = report.getFilter();
			Set<String> hiddenValues = filter != null && filter.length > 0 ? new HashSet<>(Arrays.asList(filter))
					: null;
			List<Asset> set = fetchAssets(report, preview);
			if (set != null) {
				int size = set.size();
				int incr = Math.max(1, size / 100);
				monitor.beginTask(Messages.ReportComponent_collecting, size);
				int cnt = 0;
				for (Asset asset : set) {
					Object value = QueryField.obtainFieldValue(asset, qfields[0], qfields[1]);
					String text = qfields[1].value2text(value, null);
					if (hiddenValues == null || !hiddenValues.contains(text)) {
						Cumulated cumulated = histMap.get(text);
						if (cumulated == null) {
							cumulated = new Cumulated(1, asset.getSales(), asset.getEarnings());
							histMap.put(text, cumulated);
						} else {
							cumulated.count();
							cumulated.addSales(asset.getSales());
							cumulated.addEarnings(asset.getEarnings());
						}
					}
					if (++cnt % incr == 0)
						monitor.worked(incr);
					if (cnt > preview)
						break;
				}
			}
			return histMap;
		}

		private Cumulated computeOthers(Map<String, Cumulated> histMap) {
			int totalCount = 0;
			int totalSales = 0;
			double totalEarnings = 0d;

			for (Cumulated cumulated : histMap.values()) {
				totalCount += cumulated.getCount();
				totalSales += cumulated.getSales();
				totalEarnings += cumulated.getEarnings();
			}
			double t = report.getThreshold();
			double countThreshold = totalCount * t / 100;
			double salesThreshold = totalSales * t / 100;
			double earningsThreshold = totalEarnings * t / 100;
			List<String> toBeDeleted = new ArrayList<>();
			for (Entry<String, Cumulated> entry : histMap.entrySet()) {
				Cumulated cumulated = entry.getValue();
				if ((totalCount == 0 || cumulated.getCount() < countThreshold)
						&& (totalSales == 0 || cumulated.getSales() < salesThreshold)
						&& (totalEarnings == 0 || cumulated.getEarnings() < earningsThreshold)) {
					toBeDeleted.add(entry.getKey());
				}
			}
			int otherCounts = 0;
			int otherSales = 0;
			double otherEarnings = 0;
			if (toBeDeleted.size() > 1)
				for (String key : toBeDeleted) {
					Cumulated removed = histMap.remove(key);
					otherCounts += removed.getCount();
					otherSales += removed.getSales();
					otherEarnings += removed.getEarnings();
				}
			return otherCounts > 0 || otherSales > 0 || otherEarnings != 0
					? new Cumulated(otherCounts, otherSales, otherEarnings)
					: null;
		}

		protected List<Asset> fetchAssets(Report report, int preview) {
			List<Asset> assets = null;
			IDbManager dbManager = Core.getCore().getDbManager();
			String sourceId = report.getSource();
			if (sourceId != null) {
				SmartCollectionImpl sm = dbManager.obtainById(SmartCollectionImpl.class, sourceId);
				assets = sm != null ? dbManager.createCollectionProcessor(sm).select(false) : null;
			}
			if (assets == null)
				assets = dbManager.obtainObjects(Asset.class);
			if (report.getSkipOrphans()) {
				int cnt = 0;
				List<Asset> nonOrphans = new ArrayList<>(Math.min(1000, preview));
				IVolumeManager volumeManager = Core.getCore().getVolumeManager();
				Iterator<Asset> it = assets.iterator();
				while (it.hasNext()) {
					Asset asset = it.next();
					if (volumeManager.findExistingFile(asset, false) != null
							|| volumeManager.isOffline(asset.getVolume())) {
						nonOrphans.add(asset);
						++cnt;
					}
					if (cnt >= preview)
						break;
				}
				return nonOrphans;
			}
			return assets;
		}

		private void cumulate(double[] values) {
			if ((mode & ReportWizard.CUMULATE) != 0)
				for (int i = 1; i < values.length; i++)
					values[i] += values[i - 1];
		}

		private void cumulate(int[] values) {
			if ((mode & ReportWizard.CUMULATE) != 0)
				for (int i = 1; i < values.length; i++)
					values[i] += values[i - 1];
		}

		@SuppressWarnings("unchecked")
		private JFreeChart createChart(Dataset dataset) {
			boolean multiple = ReportWizard.isMultiple(mode);
			JFreeChart chart = null;
			String name = report.getName();
			String vTitle = computeValueLabel(mode);
			String dTitle = qfields[1].getLabel();
			boolean tooltips = preview == Integer.MAX_VALUE;
			if (dataset instanceof PieDataset) {
				if ((mode & ReportWizard.THREEDIM) != 0)
					chart = ChartFactory.createPieChart3D(name, (PieDataset) dataset, multiple, tooltips, false);
				else
					chart = ChartFactory.createPieChart(name, (PieDataset) dataset, multiple, tooltips, false);
			} else if (dataset instanceof CategoryDataset) {
				if ((mode & ReportWizard.AREA) != 0)
					chart = ChartFactory.createAreaChart(name, dTitle, vTitle, (CategoryDataset) dataset,
							PlotOrientation.VERTICAL, multiple, tooltips, false);
				else if ((mode & ReportWizard.BAR) != 0)
					chart = createBarChart(name, dTitle, vTitle, (CategoryDataset) dataset, multiple, tooltips);
				else if ((mode & (ReportWizard.LINE | ReportWizard.STEP)) != 0)
					chart = createLineChart(name, dTitle, vTitle, (CategoryDataset) dataset, multiple, tooltips);
				else if ((mode & ReportWizard.PIE) != 0)
					chart = ChartFactory.createMultiplePieChart(name, (CategoryDataset) dataset, TableOrder.BY_COLUMN,
							multiple, tooltips, false);
			} else if (dataset instanceof TimeSeriesCollection) {
				dataset = toCategoryDataset((TimeSeriesCollection) dataset);
				if ((mode & ReportWizard.AREA) != 0)
					chart = ChartFactory.createAreaChart(name, dTitle, vTitle, (CategoryDataset) dataset,
							PlotOrientation.VERTICAL, multiple, tooltips, false);
				else if ((mode & ReportWizard.BAR) != 0)
					chart = createBarChart(name, dTitle, vTitle, (CategoryDataset) dataset, multiple, tooltips);
				else if ((mode & (ReportWizard.LINE | ReportWizard.STEP)) != 0)
					chart = createLineChart(name, dTitle, vTitle, (CategoryDataset) dataset, multiple, tooltips);
			}
			customize(chart, dataset);
			applyProperties(chart, (Map<String, Object>) report.getProperties());
			return chart;
		}

		protected JFreeChart createLineChart(String name, String dTitle, String vTitle, CategoryDataset dataset,
				boolean multiple, boolean tooltips) {
			JFreeChart chart;
			if ((report.getMode() & ReportWizard.THREEDIM) != 0)
				chart = ChartFactory.createLineChart3D(name, dTitle, vTitle, dataset, PlotOrientation.VERTICAL,
						multiple, tooltips, false);
			else
				chart = ChartFactory.createLineChart(name, dTitle, vTitle, dataset, PlotOrientation.VERTICAL, multiple,
						tooltips, false);
			return chart;
		}

		protected JFreeChart createBarChart(String name, String dTitle, String vTitle, CategoryDataset dataset,
				boolean multiple, boolean tooltips) {
			JFreeChart chart;
			if ((mode & ReportWizard.THREEDIM) != 0)
				chart = ChartFactory.createBarChart3D(name, dTitle, vTitle, dataset, PlotOrientation.VERTICAL, multiple,
						tooltips, false);
			else
				chart = ChartFactory.createBarChart(name, dTitle, vTitle, dataset, PlotOrientation.VERTICAL, multiple,
						tooltips, false);
			return chart;
		}

		private CategoryDataset toCategoryDataset(TimeSeriesCollection dataset) {
			DefaultCategoryDataset result = new DefaultCategoryDataset();
			// String pattern;
			// switch (interval) {
			// case ReportWizard.T_YEAR:
			// pattern = "yyyy";
			// break;
			// case ReportWizard.T_QUARTER:
			// pattern = "yyyy-MM";
			// break;
			// case ReportWizard.T_MONTH:
			// pattern = "yyyy-MM";
			// break;
			// case ReportWizard.T_WEEK:
			// pattern = "YYYY/ww";
			// break;
			// default:
			// pattern = "yyyy-MM-dd";
			// break;
			// }
			// SimpleDateFormat sdf = new SimpleDateFormat(pattern);
			int n = dataset.getSeriesCount();
			TimeSeries[] series = new TimeSeries[n];
			Comparable<?>[] keys = new Comparable<?>[n];
			for (int i = 0; i < n; i++) {
				series[i] = dataset.getSeries(i);
				keys[i] = series[i].getKey();
			}
			for (int j = 0; j < series[0].getItemCount(); j++) {
				for (int i = 0; i < n; i++) {
					Number v = series[i].getValue(j);
					RegularTimePeriod timePeriod = series[i].getTimePeriod(j);
					// long time = timePeriod.getFirstMillisecond();
					// String label = sdf.format(new Date(time));
					result.addValue(v, keys[i], timePeriod.toString());
				}
			}
			return result;
		}

		private void customize(JFreeChart chart, Dataset dataset) {
			if (chart != null) {
				String name = report.getName();
				if (name != null && !name.isEmpty()) {
					TextTitle t = new TextTitle(name);
					chart.setTitle(t);
				}
				String description = report.getDescription();
				if (description != null && !description.isEmpty()) {
					TextTitle t = new TextTitle(description);
					chart.addSubtitle(t);
				}
				Plot plot = chart.getPlot();
				if ((mode & ReportWizard.BAR) != 0 && (mode & ReportWizard.CYLINDER) != 0) {
					if (plot instanceof CategoryPlot) {
						CylinderRenderer cylinderRenderer = new CylinderRenderer();
						CategoryPlot categoryPlot = (CategoryPlot) plot;
						// BarRenderer3D barRenderer3D =
						// (BarRenderer3D)categoryPlot.getRenderer();

						// set all needed renderer properties here; for
						// instance:
						// cylinderRenderer.setItemMargin(barRenderer3D.getItemMargin());
						// etc
						categoryPlot.setRenderer(cylinderRenderer);
					}
				} else if ((mode & ReportWizard.STEP) != 0) {
					if (plot instanceof CategoryPlot) {
						CategoryStepRenderer stepRenderer = new CategoryStepRenderer();
						CategoryPlot categoryPlot = (CategoryPlot) plot;
						categoryPlot.setRenderer(stepRenderer);
					}
				}
				if ((mode & ReportWizard.PIE) != 0) {
					PiePlot piePlot = (PiePlot) plot;
					piePlot.setStartAngle(290);
					piePlot.setDirection(Rotation.CLOCKWISE);
					piePlot.setForegroundAlpha(0.5f);
					piePlot.setLabelFont(new Font("Arial", Font.PLAIN, 11)); //$NON-NLS-1$
					piePlot.setLabelPadding(new RectangleInsets(0, 4, 5, 1));
				} else {
					if ((mode & ReportWizard.EARNINGS) == 0) {
						if (plot instanceof CategoryPlot) {
							ValueAxis rangeAxis = ((CategoryPlot) plot).getRangeAxis();
							rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
						}
					}
				}
				if (plot instanceof CategoryPlot) {
					SparseCategoryAxis xAxis = null;
					if ((mode & ReportWizard.DAYTIME) != 0 && report.getDayInterval() == 96)
						xAxis = new SparseCategoryAxis(4);
					else if ((mode & ReportWizard.TIME) != 0) {
						int columnCount = ((CategoryDataset) dataset).getColumnCount();
						if (columnCount > 15) {
							switch (interval) {
							case ReportWizard.T_YEAR:
								xAxis = new SparseCategoryAxis(5);
								break;
							case ReportWizard.T_QUARTER:
								xAxis = new SparseCategoryAxis(4);
								break;
							case ReportWizard.T_MONTH:
								xAxis = new SparseCategoryAxis(6);
								break;
							case ReportWizard.T_WEEK:
								xAxis = new SparseCategoryAxis(4);
								break;
							case ReportWizard.T_DAY:
								xAxis = new SparseCategoryAxis(7);
								break;
							}
						}
					}
					if (xAxis != null) {
						Font font = new Font("Arial", Font.PLAIN, 9); //$NON-NLS-1$
						xAxis.setTickLabelFont(font);
						((CategoryPlot) plot).setDomainAxis(xAxis);
						((CategoryPlot) plot).getRangeAxis().setTickLabelFont(font);
					} else {
						Font font = new Font("Arial", Font.PLAIN, 11); //$NON-NLS-1$
						((CategoryPlot) plot).getDomainAxis().setTickLabelFont(font);
						((CategoryPlot) plot).getRangeAxis().setTickLabelFont(font);
					}
				}
				chart.setAntiAlias(true);
				chart.setTextAntiAlias(true);
			}
		}

		protected String computeValueLabel(int mode) {
			if (ReportWizard.isMultiple(mode))
				return VALUE;
			switch (mode & ReportWizard.ALLVALUES) {
			case ReportWizard.SALES:
				return SALES;
			case ReportWizard.EARNINGS:
				return EARNINGS;
			default:
				return COUNT;
			}
		}

	}

	private static final int PROGRESS_THICKNESS = 5;
	private ChartComposite chartComposite;
	private int preview;
	private Composite stack;
	private StackLayout stackLayout;
	private Composite pendingComp;
	private ProgressIndicator progressBar;

	public ReportComponent(Composite parent, int style, int preview) {
		super(parent, style);
		this.preview = preview;
		setLayout(new FillLayout());
		stack = new Composite(this, SWT.NONE);
		stackLayout = new StackLayout();
		stack.setLayout(stackLayout);

		chartComposite = new ChartComposite(stack, SWT.NONE, null, preview == Integer.MAX_VALUE,
				preview == Integer.MAX_VALUE, preview == Integer.MAX_VALUE, preview == Integer.MAX_VALUE,
				preview == Integer.MAX_VALUE);
		chartComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		pendingComp = new Composite(stack, SWT.NONE);
		pendingComp.setLayout(new GridLayout(1, false));
		Label label = new Label(pendingComp, SWT.NONE);
		label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		label.setText(Messages.ReportPage_pending);
		if (preview == Integer.MAX_VALUE) {
			progressBar = new ProgressIndicator(pendingComp, SWT.BORDER);
			GridData data = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
			data.heightHint = PROGRESS_THICKNESS;
			progressBar.setLayoutData(data);
		}
		stackLayout.topControl = pendingComp;
	}

	public void saveAs() {
		try {
			Job.getJobManager().join(this, null);
		} catch (OperationCanceledException | InterruptedException e) {
			// do nothing
		}
		try {
			chartComposite.doSaveAs();
		} catch (IOException e) {
			ReportActivator.getDefault().logError(Messages.ReportComponent_io_error, e);
		}
	}

	public void print() {
		try {
			Job.getJobManager().join(this, null);
		} catch (OperationCanceledException | InterruptedException e) {
			// do nothing
		}
		chartComposite.createChartPrintJob();
	}

	public void setReport(Report report) {
		stackLayout.topControl = pendingComp;
		stack.layout(true, true);
		Job.getJobManager().cancel(this);
		if (report != null)
			new ReportJob(report, chartComposite, preview).schedule();
	}

	protected void endTask() {
		if (progressBar != null) {
			if (!progressBar.isDisposed()) {
				progressBar.getDisplay().asyncExec(() -> {
					if (!progressBar.isDisposed()) {
						progressBar.done();
					}
				});
			}
		}
		if (!stack.isDisposed()) {
			stack.getDisplay().asyncExec(() -> {
				stackLayout.topControl = chartComposite;
				stack.layout(true, true);
			});
		}
	}

	protected void worked(int work) {
		if (progressBar != null && !progressBar.isDisposed()) {
			progressBar.getDisplay().asyncExec(() -> {
				if (!progressBar.isDisposed())
					progressBar.worked(work);
			});
		}
	}

	protected void beginTask(int totalWork) {
		if (progressBar != null && !progressBar.isDisposed()) {
			progressBar.getDisplay().asyncExec(() -> {
				if (!progressBar.isDisposed())
					progressBar.beginTask(totalWork);
			});
		}
	}

	@Override
	public void dispose() {
		Job.getJobManager().cancel(this);
		super.dispose();
	}

	public void saveChartProperties(Report report) {
		JFreeChart chart = chartComposite.getChart();
		if (chart != null) {
			Map<String, Object> properties = new HashMap<>();
			TextTitle title = chart.getTitle();
			if (title != null) {
				properties.put(TITLE, title.getText());
				properties.put(TITLEFONT, title.getFont());
				properties.put(TITLECOLOR, title.getPaint());
			}
			Plot plot = chart.getPlot();
			properties.put(BGCOLOR, plot.getBackgroundPaint());
			properties.put(OUTLINEPAINT, plot.getOutlinePaint());
			properties.put(OUTLINESTROKE, plot.getOutlineStroke());
			Axis domainAxis = null;
			Axis rangeAxis = null;
			if (plot instanceof CategoryPlot) {
				CategoryPlot p = (CategoryPlot) plot;
				domainAxis = p.getDomainAxis();
				rangeAxis = p.getRangeAxis();
				properties.put(ORIENTATION, p.getOrientation());
			} else if (plot instanceof XYPlot) {
				XYPlot p = (XYPlot) plot;
				domainAxis = p.getDomainAxis();
				rangeAxis = p.getRangeAxis();
				properties.put(ORIENTATION, p.getOrientation());
			}
			if (domainAxis != null)
				saveAxisProperties(domainAxis, "x", properties); //$NON-NLS-1$
			if (rangeAxis != null)
				saveAxisProperties(rangeAxis, "y", properties); //$NON-NLS-1$
			properties.put(ANTIALIAS, chart.getAntiAlias());
			properties.put(CANVASPAINT, chart.getBackgroundPaint());
			report.setProperties(properties);
		}
	}

	private static void saveAxisProperties(Axis axis, String prefix, Map<String, Object> properties) {
		properties.put(prefix + AXISLABEL, axis.getLabel());
		properties.put(prefix + AXISLABELFONT, axis.getLabelFont());
		properties.put(prefix + AXISLABELPAINT, axis.getLabelPaint());
		properties.put(prefix + TICKMARKSVISIBLE, axis.isTickMarksVisible());
		properties.put(prefix + TICKLABELSVISIBLE, axis.isTickLabelsVisible());
		properties.put(prefix + TICKLABELFONT, axis.getTickLabelFont());
		properties.put(prefix + TICKLABELPAINT, axis.getLabelPaint());
	}

	private static void applyProperties(JFreeChart chart, Map<String, Object> properties) {
		if (properties != null) {
			String text = (String) properties.get(TITLE);
			if (text != null) {
				TextTitle title = chart.getTitle();
				if (title == null) {
					title = new TextTitle();
					chart.setTitle(title);
				}
				title.setText(text);
				Font titleFont = (Font) properties.get(TITLEFONT);
				if (titleFont != null)
					title.setFont(titleFont);
				Paint paint = (Paint) properties.get(TITLECOLOR);
				if (paint != null)
					title.setPaint(paint);
			} else
				chart.setTitle((TextTitle) null);
			Plot plot = chart.getPlot();
			Paint paint = (Paint) properties.get(BGCOLOR);
			if (paint != null)
				plot.setBackgroundPaint(paint);
			paint = (Color) properties.get(OUTLINEPAINT);
			if (paint != null)
				plot.setOutlinePaint(paint);
			Stroke stroke = (Stroke) properties.get(OUTLINESTROKE);
			if (stroke != null)
				plot.setOutlineStroke(stroke);
			PlotOrientation orientation = (PlotOrientation) properties.get(ORIENTATION);
			Axis domainAxis = null;
			Axis rangeAxis = null;
			if (plot instanceof CategoryPlot) {
				CategoryPlot p = (CategoryPlot) plot;
				domainAxis = p.getDomainAxis();
				rangeAxis = p.getRangeAxis();
				if (orientation != null)
					p.setOrientation(orientation);
			} else if (plot instanceof XYPlot) {
				XYPlot p = (XYPlot) plot;
				domainAxis = p.getDomainAxis();
				rangeAxis = p.getRangeAxis();
				if (orientation != null)
					p.setOrientation(orientation);
			}
			if (domainAxis != null)
				applyAxisProperties(domainAxis, "x", properties); //$NON-NLS-1$
			if (rangeAxis != null)
				applyAxisProperties(rangeAxis, "y", properties); //$NON-NLS-1$
			Boolean anti = (Boolean) properties.get(ANTIALIAS);
			if (anti != null)
				chart.setAntiAlias(anti);
			paint = (Paint) properties.get(CANVASPAINT);
			if (paint != null)
				chart.setBackgroundPaint(paint);
		}

	}

	private static void applyAxisProperties(Axis axis, String prefix, Map<String, Object> properties) {
		axis.setLabel((String) properties.get(prefix + AXISLABEL));
		Font font = (Font) properties.get(prefix + AXISLABELFONT);
		if (font != null)
			axis.setLabelFont(font);
		Paint paint = (Paint) properties.get(prefix + AXISLABELPAINT);
		if (paint != null)
			axis.setLabelPaint(paint);
		Boolean visible = (Boolean) properties.get(prefix + TICKMARKSVISIBLE);
		if (visible != null)
			axis.setTickMarksVisible(visible);
		visible = (Boolean) properties.get(prefix + TICKLABELSVISIBLE);
		if (visible != null)
			axis.setTickLabelsVisible(visible);
		font = (Font) properties.get(prefix + TICKLABELFONT);
		if (font != null)
			axis.setTickLabelFont(font);
		paint = (Paint) properties.get(prefix + TICKLABELPAINT);
		if (paint != null)
			axis.setTickLabelPaint(paint);
	}

}
