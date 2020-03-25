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
 * (c) 2018 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.views;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.Criterion;
import com.bdaum.zoom.cat.model.group.CriterionImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.SortCriterionImpl;
import com.bdaum.zoom.core.CatalogAdapter;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.hover.IGalleryHover;
import com.bdaum.zoom.ui.internal.hover.IHoverInfo;

@SuppressWarnings("restriction")
public class CalendarView extends BasicView {

	public class CalendarHover implements IGalleryHover {

		@Override
		public IHoverInfo getHoverInfo(IHoverSubject subject, Event event) {
			if (event.widget instanceof Canvas) {
				Rectangle clientArea = ((Canvas) event.widget).getClientArea();
				if (event.x >= clientArea.x && event.x < clientArea.x + clientArea.width && event.y > clientArea.y
						&& event.y < clientArea.y + clientArea.height) {
					int i = (event.x - wxo) / size + 7 * ((event.y - yoff) / size);
					if (i >= 0 && i < daysInMonth && calendarAssets[i] != null) {
						workingCal.setTime(currentCal.getTime());
						workingCal.add(GregorianCalendar.DAY_OF_MONTH, i);
						Date from = workingCal.getTime();
						workingCal.add(GregorianCalendar.DAY_OF_MONTH, 1);
						List<AssetImpl> set = Core.getCore().getDbManager().obtainObjects(AssetImpl.class, false,
								QueryField.IPTC_DATECREATED.getKey(), from, QueryField.NOTSMALLER,
								QueryField.IPTC_DATECREATED.getKey(), workingCal.getTime(), QueryField.SMALLER);
						final String hovertext = NLS.bind(Messages.getString("CalendarView.n_items"), set.size()); //$NON-NLS-1$
						return new IHoverInfo() {
							@Override
							public String getTitle() {
								return null;
							}

							@Override
							public String getText() {
								return hovertext;
							}

							@Override
							public ImageRegion[] getRegions() {
								return null;
							}

							@Override
							public Object getObject() {
								return this;
							}
						};
					}
				}
			}
			return null;
		}
	}

	public class FindLimitsJob extends Job {

		public FindLimitsJob() {
			super(Messages.getString("CalendarView.find_limits")); //$NON-NLS-1$
			setSystem(true);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			Date min = null;
			Date max = null;
			Date dmin = null;
			Date dmax = null;
			for (SmartCollectionImpl sm : Core.getCore().getDbManager().obtainObjects(SmartCollectionImpl.class, false,
					"system", true, QueryField.EQUALS, "album", false, QueryField.EQUALS, Constants.OID, //$NON-NLS-1$ //$NON-NLS-2$
					IDbManager.DATETIMEKEY, QueryField.STARTSWITH)) {
				List<Criterion> criteria = sm.getCriterion();
				if (criteria != null && !criteria.isEmpty()) {
					Criterion crit = criteria.get(0);
					Date from = (Date) crit.getValue();
					Date to = (Date) crit.getTo();
					if (to.getTime() - from.getTime() < D100) {
						if (dmin == null || dmin.after(from))
							dmin = from;
						if (dmax == null || dmax.before(to))
							dmax = to;
					} else {
						if (min == null || min.after(from))
							min = from;
						if (max == null || max.before(to))
							max = to;
					}
				}
			}
			if (dmin != null)
				min = dmin;
			if (dmax != null)
				max = dmax;
			minDate = new GregorianCalendar(1826, 0, 1);
			if (min != null) {
				minDate.setTime(min);
				alignCal(minDate);
			}
			maxDate = new GregorianCalendar();
			if (max != null) {
				maxDate.setTime(max);
				alignCal(maxDate);
				maxDate.add(GregorianCalendar.MONTH, 1);
				maxDate.add(GregorianCalendar.MILLISECOND, -1);
			}
			return Status.OK_STATUS;
		}
	}

	private class CalendarAction extends Action {
		private int month;

		public CalendarAction(String label, ImageDescriptor icon, int month) {
			super(label, icon);
			setToolTipText(label);
			this.month = month;
		}

		@Override
		public void run() {
			currentCal.add(GregorianCalendar.MONTH, month);
			if (currentCal.before(minDate) || currentCal.after(maxDate))
				currentCal.add(GregorianCalendar.MONTH, -month);
			else
				refresh();
		}
	}

	public static final String ID = "com.bdaum.zoom.ui.views.CalendarView"; //$NON-NLS-1$
	private static final long D100 = 8640000000L;

	private Canvas canvas;
	private final GregorianCalendar currentCal = new GregorianCalendar();
	private final GregorianCalendar workingCal = new GregorianCalendar();
	private GregorianCalendar minDate = new GregorianCalendar(1826, 0, 1);
	private GregorianCalendar maxDate = new GregorianCalendar();
	private final Asset[] calendarAssets = new Asset[31];
	private int daysInMonth;
	private CalendarAction nextMonthAction, previousMonthAction, nextYearAction, previousYearAction;
	private Rectangle titleRect = new Rectangle(0, 0, 0, 0);
	private Rectangle dayRect = new Rectangle(0, 0, 0, 0);
	private Rectangle weekRect = new Rectangle(0, 0, 0, 0);
	private int size, yoff, wxo, coff, pending = -1;
	private Timer timer = new Timer();
	private Point mouseDown;
	private TimerTask task;

	@Override
	public ISelection getSelection() {
		return AssetSelection.EMPTY;
	}

	@Override
	public void setSelection(ISelection selection) {
		// do nothing
	}

	@Override
	public Control getControl() {
		return canvas;
	}

	@Override
	public boolean selectionChanged() {
		return false;
	}

	@Override
	public void assetsChanged(IWorkbenchPart part, AssetSelection selectedAssets) {
		for (Asset asset : selectedAssets.getAssets()) {
			Date dateCreated = asset.getDateCreated();
			if (dateCreated != null) {
				setCurrentCal(dateCreated);
				break;
			}
		}
	}

	private void setCurrentCal(Date dateCreated) {
		int year = currentCal.get(GregorianCalendar.YEAR);
		int month = currentCal.get(GregorianCalendar.MONTH);
		currentCal.setTime(dateCreated);
		alignCal(currentCal);
		if (year != currentCal.get(GregorianCalendar.YEAR) || month != currentCal.get(GregorianCalendar.MONTH)) {
			if (isVisible())
				refreshBusy();
			else
				isDirty = true;
		}
	}

	private static void alignCal(GregorianCalendar cal) {
		cal.set(GregorianCalendar.DAY_OF_MONTH, 1);
		cal.set(GregorianCalendar.HOUR_OF_DAY, 0);
		cal.set(GregorianCalendar.MINUTE, 0);
		cal.set(GregorianCalendar.SECOND, 0);
		cal.set(GregorianCalendar.MILLISECOND, 0);
	}

	@Override
	public boolean assetsChanged() {
		return false;
	}

	@Override
	public void collectionChanged(IWorkbenchPart part, IStructuredSelection sel) {
		if (sel.size() == 1) {
			Object firstElement = sel.getFirstElement();
			if (firstElement instanceof SmartCollection) {
				SmartCollectionImpl currentCollection = (SmartCollectionImpl) firstElement;
				if (currentCollection.getStringId().startsWith(IDbManager.DATETIMEKEY))
					setCurrentCal((Date) currentCollection.getCriterion(0).getValue());
			}
		}
	}

	@Override
	public boolean collectionChanged() {
		return false;
	}

	@Override
	public void refresh() {
		IDbManager dbManager = Core.getCore().getDbManager();
		workingCal.setTime(currentCal.getTime());
		workingCal.add(GregorianCalendar.MONTH, 1);
		workingCal.add(GregorianCalendar.DAY_OF_MONTH, -1);
		daysInMonth = workingCal.get(GregorianCalendar.DAY_OF_MONTH);
		workingCal.setTime(currentCal.getTime());
		for (int i = 0; i < daysInMonth; i++) {
			Date from = workingCal.getTime();
			workingCal.add(GregorianCalendar.DAY_OF_MONTH, 1);
			Iterator<AssetImpl> it = dbManager.obtainObjects(AssetImpl.class, false,
					QueryField.IPTC_DATECREATED.getKey(), from, QueryField.NOTSMALLER,
					QueryField.IPTC_DATECREATED.getKey(), workingCal.getTime(), QueryField.SMALLER).iterator();
			calendarAssets[i] = it.hasNext() ? it.next() : null;
		}
		canvas.redraw();
		updateActions(true);
	}

	@Override
	public void createPartControl(Composite parent) {
		canvas = new Canvas(parent, SWT.DOUBLE_BUFFERED);
		canvas.addListener(SWT.Paint, this);
		installListeners();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(canvas, HelpContextIds.CALENDAR_VIEW);
		makeActions();
		contributeToActionBars();
		updateActions(true);
		CoreActivator.getDefault().addCatalogListener(new CatalogAdapter() {
			@Override
			public void catalogOpened(boolean newDb) {
				new FindLimitsJob().schedule();
			}
		});
		installHoveringController();
	}

	public Control[] getControls() {
		return new Control[] { canvas };
	}

	@Override
	public IGalleryHover getGalleryHover(Event event) {
		return new CalendarHover();
	}

	@Override
	protected void installListeners() {
		super.installListeners();
		canvas.addListener(SWT.MouseDown, this);
		canvas.addListener(SWT.MouseUp, this);
		canvas.addListener(SWT.MouseMove, this);
		canvas.addListener(SWT.KeyUp, this);
		Ui.getUi().getNavigationHistory(getSite().getWorkbenchWindow()).addSelectionListener(this);
	}

	@Override
	protected void uninstallListeners() {
		super.uninstallListeners();
		if (!canvas.isDisposed()) {
			canvas.removeListener(SWT.MouseDown, this);
			canvas.removeListener(SWT.MouseUp, this);
			canvas.removeListener(SWT.MouseMove, this);
			canvas.removeListener(SWT.KeyUp, this);
		}
		Ui.getUi().getNavigationHistory(getSite().getWorkbenchWindow()).removeSelectionListener(this);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(nextMonthAction);
		manager.add(previousMonthAction);
		manager.add(nextYearAction);
		manager.add(previousYearAction);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalPullDown(IMenuManager menuManager) {
		menuManager.add(nextMonthAction);
		menuManager.add(previousMonthAction);
		menuManager.add(nextYearAction);
		menuManager.add(previousYearAction);
	}

	@Override
	protected void makeActions() {
		nextMonthAction = new CalendarAction(Messages.getString("CalendarView.next_month"), //$NON-NLS-1$
				Icons.forwards.getDescriptor(), 1);
		previousMonthAction = new CalendarAction(Messages.getString("CalendarView.previous_month"), //$NON-NLS-1$
				Icons.backwards.getDescriptor(), -1);
		nextYearAction = new CalendarAction(Messages.getString("CalendarView.next_year"), Icons.down.getDescriptor(), //$NON-NLS-1$
				12);
		previousYearAction = new CalendarAction(Messages.getString("CalendarView.previous_year"), //$NON-NLS-1$
				Icons.up.getDescriptor(), -12);
	}

	@Override
	public void setFocus() {
		canvas.setFocus();
	}

	@Override
	public void updateActions(boolean force) {
		workingCal.setTime(currentCal.getTime());
		workingCal.add(GregorianCalendar.MONTH, -1);
		previousMonthAction.setEnabled(!workingCal.before(minDate));
		workingCal.add(GregorianCalendar.MONTH, -11);
		previousYearAction.setEnabled(!workingCal.before(minDate));
		workingCal.setTime(currentCal.getTime());
		workingCal.add(GregorianCalendar.MONTH, 1);
		nextMonthAction.setEnabled(workingCal.before(maxDate));
		workingCal.add(GregorianCalendar.MONTH, 11);
		nextYearAction.setEnabled(workingCal.before(maxDate));
	}

	private void paintControl(Event e) {
		Rectangle clientArea = canvas.getClientArea();
		int h = clientArea.height / 6;
		int ww = h / 5;
		int w = (clientArea.width - ww) / 7;
		size = Math.min(w, h);
		GC gc = e.gc;
		gc.setTextAntialias(SWT.ON);
		h = size * 6;
		w = size * 7;
		ww = size / 5;
		yoff = (clientArea.height - h) / 2;
		int xoff = (clientArea.width - w) / 2;
		dayRect.x = wxo = xoff + ww;
		dayRect.y = yoff;
		dayRect.width = w;
		dayRect.height = h;
		weekRect.x = xoff;
		weekRect.y = yoff;
		weekRect.width = ww;
		weekRect.height = h;
		workingCal.setTime(currentCal.getTime());
		coff = (workingCal.get(GregorianCalendar.DAY_OF_WEEK) - workingCal.getFirstDayOfWeek() + 7) % 7;
		gc.setBackground(canvas.getForeground());
		gc.setAlpha(64);
		gc.fillRectangle(xoff, yoff, ww, size * 6);
		gc.setAlpha(128);
		if (coff > 0)
			gc.fillRectangle(xoff + ww, yoff, coff * size, size);
		int obsolete = 42 - coff - daysInMonth;
		if (obsolete > 7) {
			gc.fillRectangle(wxo + (14 - obsolete) * size, yoff + 4 * size, (obsolete % 7) * size, size);
			gc.fillRectangle(wxo, yoff + 5 * size, 7 * size, size);
		} else
			gc.fillRectangle(wxo + (7 - obsolete) * size, yoff + 5 * size, obsolete * size, size);
		gc.setBackground(canvas.getBackground());
		gc.setAlpha(255);
		gc.drawLine(xoff, yoff, xoff, yoff + h);
		for (int i = 0; i <= 7; i++)
			gc.drawLine(wxo + i * size, yoff, wxo + i * size, yoff + h);
		for (int i = 0; i <= 6; i++)
			gc.drawLine(xoff, yoff + i * size, wxo + w, yoff + i * size);
		for (int i = 0; i < daysInMonth; i++) {
			Asset asset = calendarAssets[i];
			if (asset != null) {
				Image image = getImage(asset);
				Rectangle bounds = image.getBounds();
				double fac = (double) (size - 2) / Math.max(bounds.width, bounds.height);
				int iw = (int) (bounds.width * fac);
				int ih = (int) (bounds.height * fac);
				int x = (i + coff) % 7;
				int y = (i + coff) / 7;
				gc.drawImage(image, 0, 0, bounds.width, bounds.height, wxo + x * size + (size - iw) / 2,
						yoff + y * size + (size - ih) / 2, iw, ih);
			}
		}
		workingCal.add(GregorianCalendar.DAY_OF_MONTH, -coff);
		FontData fontData = e.display.getSystemFont().getFontData()[0];
		Font dayFont = new Font(e.display, fontData.getName(), size / 6, SWT.BOLD);
		gc.setAlpha(128);
		for (int i = 0; i < 6; i++) {
			gc.setFont(e.display.getSystemFont());
			String s = String.valueOf(workingCal.get(GregorianCalendar.WEEK_OF_YEAR));
			Point tx = gc.textExtent(s);
			int y = yoff + i * size;
			gc.drawText(s, xoff + (ww - tx.x) / 2, y, true);
			gc.setFont(dayFont);
			y += size;
			for (int j = 1; j <= 7; j++) {
				s = String.valueOf(workingCal.get(GregorianCalendar.DAY_OF_MONTH));
				tx = gc.textExtent(s);
				gc.drawText(s, wxo + j * size - tx.x, y - tx.y, true);
				workingCal.add(GregorianCalendar.DAY_OF_MONTH, 1);
			}
		}
		dayFont.dispose();
		Date date = currentCal.getTime();
		int fontsize;
		SimpleDateFormat df;
		if (xoff > yoff * 3) {
			df = Format.LRY_FORMAT.get();
			fontsize = size / 2;
			titleRect.x = 0;
			titleRect.y = yoff;
		} else {
			df = Format.LY_SHORT_FORMAT.get();
			fontsize = size / 4;
			titleRect.x = xoff;
			titleRect.y = 0;
		}
		String s = df.format(date);
		Font monthFont = new Font(e.display, fontData.getName(), fontsize, SWT.BOLD);
		gc.setFont(monthFont);
		gc.drawText(s, titleRect.x, titleRect.y, true);
		Point tx = gc.textExtent(s);
		titleRect.width = tx.x;
		titleRect.height = tx.y;
		monthFont.dispose();
	}

	@Override
	public void handleEvent(Event e) {
		switch (e.type) {
		case SWT.MouseDown:
			mouseDown = new Point(e.x, e.y);
			break;
		case SWT.MouseUp:
			if (mouseDown != null) {
				boolean gesture = processGesture(e.x - mouseDown.x, e.y - mouseDown.y);
				mouseDown = null;
				if (gesture)
					return;
			}
			if (titleRect.contains(e.x, e.y))
				showMonth();
			else if (weekRect.contains(e.x, e.y))
				showWeek(((e.y - yoff) / size));
			else if (dayRect.contains(e.x, e.y))
				showDay(((e.y - yoff) / size) * 7 + (e.x - wxo) / size - coff);

			break;
		case SWT.MouseMove:
			if (titleRect.contains(e.x, e.y)) {
				for (int day = 0; day < daysInMonth; day++)
					if (calendarAssets[day] != null) {
						canvas.setCursor(e.display.getSystemCursor(SWT.CURSOR_HAND));
						return;
					}
			} else if (dayRect.contains(e.x, e.y)) {
				int day = ((e.y - yoff) / size) * 7 + (e.x - wxo) / size - coff;
				if (day >= 0 && day < daysInMonth && calendarAssets[day] != null) {
					canvas.setCursor(e.display.getSystemCursor(SWT.CURSOR_HAND));
					return;
				}
			} else if (weekRect.contains(e.x, e.y)) {
				int week = ((e.y - yoff) / size);
				for (int i = 0; i < 7; i++) {
					int day = week * 7 + i - coff;
					if (day >= 0 && day < daysInMonth && calendarAssets[day] != null) {
						canvas.setCursor(e.display.getSystemCursor(SWT.CURSOR_HAND));
						return;
					}
				}
			}
			canvas.setCursor(e.display.getSystemCursor(SWT.CURSOR_ARROW));
			break;
		case SWT.Paint:
			paintControl(e);
			break;
		}
		super.handleEvent(e);
	}

	private boolean processGesture(int difx, int dify) {
		Rectangle area = canvas.getClientArea();
		if (Math.abs(difx) > Math.abs(dify)) {
			if (Math.abs(difx) > area.width / 4) {
				(difx > 0 ? previousMonthAction : nextMonthAction).run();
				return true;
			}
		} else if (Math.abs(dify) > area.height / 4) {
			(dify > 0 ? previousYearAction : nextYearAction).run();
			return true;
		}
		return false;
	}

	private void showWeek(int week) {
		pending = -1;
		for (int i = 0; i < 7; i++) {
			int day = i + week * 7 - coff;
			if (day >= 0 && day < daysInMonth && calendarAssets[day] != null) {
				StringBuilder ksb = new StringBuilder(IDbManager.DATETIMEKEY);
				ksb.append(currentCal.get(GregorianCalendar.YEAR)).append("-W") //$NON-NLS-1$
						.append(week + currentCal.get(GregorianCalendar.WEEK_OF_YEAR));
				SmartCollectionImpl sm = Core.getCore().getDbManager().obtainById(SmartCollectionImpl.class,
						ksb.toString());
				if (sm != null) {
					showCollection(sm);
					return;
				}
				workingCal.setTime(currentCal.getTime());
				workingCal.add(GregorianCalendar.DAY_OF_MONTH, week * 7 - coff);
				Date from = workingCal.getTime();
				workingCal.add(GregorianCalendar.DAY_OF_MONTH, 7);
				workingCal.add(GregorianCalendar.MILLISECOND, -1);
				postQuery(from, workingCal.getTime(), Format.WEEK_WY_FORMAT.get().format(from));
				break;
			}
		}
	}

	private void showDay(final int day) {
		pending = -1;
		if (day >= 0 && day < daysInMonth) {
			if (calendarAssets[day] != null) {
				IDbManager dbManager = Core.getCore().getDbManager();
				workingCal.setTime(currentCal.getTime());
				workingCal.add(GregorianCalendar.DAY_OF_MONTH, day);
				StringBuilder ksb = new StringBuilder(IDbManager.DATETIMEKEY);
				ksb.append(workingCal.get(GregorianCalendar.YEAR)).append('-')
						.append(workingCal.get(GregorianCalendar.MONTH)).append('-')
						.append(workingCal.get(GregorianCalendar.DAY_OF_MONTH));
				SmartCollectionImpl sm = dbManager.obtainById(SmartCollectionImpl.class, ksb.toString());
				if (sm != null) {
					showCollection(sm);
					return;
				}
				ksb.setLength(IDbManager.DATETIMEKEY.length());
				ksb.append(workingCal.get(GregorianCalendar.YEAR)).append("W-") //$NON-NLS-1$
						.append(workingCal.get(GregorianCalendar.WEEK_OF_YEAR)).append('-')
						.append(workingCal.get(GregorianCalendar.DAY_OF_WEEK));
				sm = dbManager.obtainById(SmartCollectionImpl.class, ksb.toString());
				if (sm != null) {
					showCollection(sm);
					return;
				}
				Date from = workingCal.getTime();
				workingCal.add(GregorianCalendar.DAY_OF_MONTH, 1);
				workingCal.add(GregorianCalendar.MILLISECOND, -1);
				postQuery(from, workingCal.getTime(), Format.LDY_FORMAT.get().format(from));
			}
		}
	}

	private void showMonth() {
		pending = -1;
		StringBuilder ksb = new StringBuilder(IDbManager.DATETIMEKEY);
		ksb.append(currentCal.get(GregorianCalendar.YEAR)).append('-').append(currentCal.get(GregorianCalendar.MONTH));
		SmartCollectionImpl sm = Core.getCore().getDbManager().obtainById(SmartCollectionImpl.class, ksb.toString());
		if (sm != null) {
			showCollection(sm);
			return;
		}
		Date from = currentCal.getTime();
		workingCal.setTime(from);
		workingCal.add(GregorianCalendar.MONTH, 1);
		workingCal.add(GregorianCalendar.MILLISECOND, -1);
		postQuery(from, workingCal.getTime(), Format.LY_FORMAT.get().format(from));
	}

	@Override
	protected void onKeyUp(Event e) {
		switch (e.keyCode) {
		case SWT.ARROW_RIGHT:
			nextMonthAction.run();
			break;
		case SWT.ARROW_LEFT:
			previousMonthAction.run();
			break;
		case SWT.ARROW_UP:
			previousYearAction.run();
			break;
		case SWT.ARROW_DOWN:
			nextYearAction.run();
			break;
		case SWT.HOME:
			currentCal.set(GregorianCalendar.MONTH, 0);
			if (currentCal.before(minDate))
				currentCal.setTime(minDate.getTime());
			refresh();
			break;
		case SWT.END:
			currentCal.set(GregorianCalendar.MONTH, 11);
			if (currentCal.after(maxDate))
				currentCal.setTime(maxDate.getTime());
			refresh();
			break;
		case SWT.PAGE_UP:
			currentCal.setTime(minDate.getTime());
			refresh();
			break;
		case SWT.PAGE_DOWN:
			currentCal.setTime(maxDate.getTime());
			alignCal(currentCal);
			refresh();
			break;
		default:
			char c = e.character;
			if (c == '*')
				showMonth();
			else if (c >= '0' && c <= '9') {
				if (pending < 0) {
					if (c >= '1' && c <= '3') {
						pending = c - '0';
						if (task != null)
							task.cancel();
						task = new TimerTask() {
							@Override
							public void run() {
								if (!canvas.isDisposed())
									e.display.asyncExec(() -> {
										if (!canvas.isDisposed())
											showDay(pending);
									});
								timer = null;
							}
						};
						timer.schedule(task, 800L);
					} else if (c != '0')
						showDay(c - '0');
				} else
					showDay(pending * 10 + c - '0');
			}
			break;
		}

	}

	private void postQuery(Date from, Date to, String s) {
		SmartCollectionImpl sm = new SmartCollectionImpl(s, false, false, false, true, null, 0, null, 0, null,
				Constants.INHERIT_LABEL, null, 0, 1, null);
		sm.addCriterion(
				new CriterionImpl(QueryField.IPTC_DATECREATED.getKey(), null, from, to, QueryField.BETWEEN, true));
		sm.addSortCriterion(new SortCriterionImpl(QueryField.IPTC_DATECREATED.getKey(), null, false));
		showCollection(sm);
	}

	private void showCollection(SmartCollectionImpl sm) {
		BusyIndicator.showWhile(canvas.getDisplay(), () -> {
			bringGalleryToTop();
			Ui.getUi().getNavigationHistory(getSite().getWorkbenchWindow()).postSelection(new StructuredSelection(sm));
		});
	}

	private void bringGalleryToTop() {
		String perspId = getSite().getPage().getPerspective().getId();
		String viewId = UiActivator.getDefault().getPerspectiveGallery(perspId);
		if (viewId != null)
			try {
				getSite().getPage().showView(viewId);
			} catch (PartInitException e) {
				// ignore
			}
	}

}
