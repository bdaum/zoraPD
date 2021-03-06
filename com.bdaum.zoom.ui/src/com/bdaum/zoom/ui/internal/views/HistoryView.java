package com.bdaum.zoom.ui.internal.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.batch.internal.Daemon;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitionImpl;
import com.bdaum.zoom.cat.model.group.slideShow.SlideShowImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebGalleryImpl;
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.CatalogAdapter;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.css.ITitle;
import com.bdaum.zoom.ui.HistoryListener;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiUtilities;

@SuppressWarnings("restriction")
public class HistoryView extends AbstractCatalogView implements HistoryListener, IDoubleClickListener, ISelectionChangedListener {

	private class HistoryAction extends Action {

		private final int type;

		public HistoryAction(int type, String text, ImageDescriptor image, String tooltip) {
			super(text, image);
			setToolTipText(tooltip);
			this.type = type;
		}

		@Override
		public void run() {
			for (Object o : history)
				if (o instanceof HistoryTitle)
					if (((HistoryTitle) o).getType() == type) {
						viewer.reveal(o);
						break;
					}
		}
	}

	private class LimitAction extends Action {

		private final int limit;

		public LimitAction(int limit, String text, String tooltip) {
			super(text, IAction.AS_RADIO_BUTTON);
			setToolTipText(tooltip);
			this.limit = limit;
		}

		@Override
		public void run() {
			historyLimit = limit;
			refresh();
		}
	}

	private class HistoryUpdateJob extends Daemon {

		private final IdentifiableObject delete;
		private final IdentifiableObject add;

		public HistoryUpdateJob(IdentifiableObject delete, IdentifiableObject add) {
			super(Messages.getString("HistoryView.updating_history"), -1); //$NON-NLS-1$
			this.delete = delete;
			this.add = add;
			setRule(rule);
		}

		@Override
		protected void doRun(IProgressMonitor monitor) {
			Control control = viewer.getControl();
			if (!control.isDisposed())
				control.getDisplay().asyncExec(() -> {
					if (!control.isDisposed()) {
						if (delete != null) {
							((TableViewer) viewer).remove(delete);
							history.remove(delete);
						}
						if (add != null) {
							@SuppressWarnings("unchecked")
							List<Object> input = (List<Object>) viewer.getInput();
							if (input != null && input.size() > 1) {
								history.add(1, add);
								((TableViewer) viewer).insert(add, 1);
							}
						}
					}
				});
		}
	}

	public class CleanupJob extends Job {

		public CleanupJob() {
			super(Messages.getString("HistoryView.removing_decayed")); //$NON-NLS-1$
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			List<Object> toBeDeleted = new ArrayList<Object>();
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTimeInMillis(System.currentTimeMillis());
			cal.add(GregorianCalendar.YEAR, -1);
			Date decaylimit = cal.getTime();
			IDbManager dbManager = Core.getCore().getDbManager();
			for (SmartCollectionImpl sm : dbManager.obtainObjects(SmartCollectionImpl.class, "lastAccessDate", //$NON-NLS-1$
					decaylimit, QueryField.SMALLER)) {
				if (sm.getAdhoc() && !sm.getSystem())
					toBeDeleted.add(sm);
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
			}
			if (!toBeDeleted.isEmpty())
				dbManager.safeTransaction(toBeDeleted, null);
			return Status.OK_STATUS;
		}
	}

	public class HistoryJob extends Daemon {

		public HistoryJob() {
			super(Messages.getString("HistoryView.creating_history"), -1); //$NON-NLS-1$
			setRule(rule);
			final Control control = viewer.getControl();
			if (!control.isDisposed())
				control.getDisplay().asyncExec(() -> {
					if (!control.isDisposed())
						control.setCursor(control.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
				});
		}
		
		@Override
		public boolean belongsTo(Object family) {
			return family == HistoryView.this || super.belongsTo(family);
		}

		@Override
		protected void doRun(IProgressMonitor monitor) {
			++settingSelection;
			monitor.beginTask(Messages.getString("HistoryView.creating_history"), 13); //$NON-NLS-1$
			history.clear();
			GregorianCalendar cal = new GregorianCalendar();
			cal.set(GregorianCalendar.HOUR_OF_DAY, 0);
			cal.set(GregorianCalendar.MINUTE, 0);
			cal.set(GregorianCalendar.SECOND, 0);
			cal.set(GregorianCalendar.MILLISECOND, 0);
			cal.add(GregorianCalendar.DAY_OF_MONTH, 1); // tomorrow 0:00
			history.add(new HistoryTitle(Messages.getString("HistoryView.today"), cal.getTime(), TODAY)); //$NON-NLS-1$
			cal.add(GregorianCalendar.DAY_OF_MONTH, -1); // today 0:00
			int dayOfWeek = cal.get(GregorianCalendar.DAY_OF_WEEK);
			int firstDayOfWeek = cal.getFirstDayOfWeek();
			int dayOfMonth = cal.get(GregorianCalendar.DAY_OF_MONTH);
			history.add(new HistoryTitle(Messages.getString("HistoryView.yesterday"), cal.getTime(), YESTERDAY)); //$NON-NLS-1$
			cal.add(GregorianCalendar.DAY_OF_MONTH, -1);
			int covered;
			if (dayOfWeek != firstDayOfWeek) {
				history.add(new HistoryTitle(Messages.getString("HistoryView.start_of_week"), cal.getTime(), //$NON-NLS-1$
						WEEK));
				int diff = (dayOfWeek - firstDayOfWeek + 7) % 7;
				cal.add(GregorianCalendar.DAY_OF_MONTH, -diff); // First day of
																// week
				covered = diff;
				if (diff <= 2) {
					covered += 7;
					history.add(new HistoryTitle(Messages.getString("HistoryView.last_week"), cal.getTime(), WEEK)); //$NON-NLS-1$
					cal.add(GregorianCalendar.DAY_OF_MONTH, -7); // First day of last week
				}
			} else {
				history.add(new HistoryTitle(Messages.getString("HistoryView.last_week"), cal.getTime(), WEEK)); //$NON-NLS-1$
				cal.add(GregorianCalendar.DAY_OF_MONTH, -7); // First day of last week
				covered = 7;
			}
			if (dayOfMonth - 1 <= covered) {
				history.add(new HistoryTitle(Messages.getString("HistoryView.last_month"), cal.getTime(), MONTH)); //$NON-NLS-1$
				cal.add(GregorianCalendar.DAY_OF_MONTH, -1); // Last day of last month
				cal.set(GregorianCalendar.DAY_OF_MONTH, 1); // First day of last month
			} else {
				history.add(new HistoryTitle(Messages.getString("HistoryView.start_of_month"), cal.getTime(), //$NON-NLS-1$
						MONTH));
				cal.set(GregorianCalendar.DAY_OF_MONTH, 1); // First day of this month
				if (dayOfMonth <= 7) {
					history.add(new HistoryTitle(Messages.getString("HistoryView.last_month"), cal.getTime(), MONTH)); //$NON-NLS-1$
					cal.add(GregorianCalendar.MONTH, -1); // First day of last month
				}
			}
			history.add(new HistoryTitle(Messages.getString("HistoryView.older"), cal.getTime(), OLDER)); //$NON-NLS-1$
			cal.setTimeInMillis(System.currentTimeMillis());
			cal.add(GregorianCalendar.YEAR, -1);
			Date limit = cal.getTime();
			switch (historyLimit) {
			case ONEMONTH:
				cal.add(GregorianCalendar.MONTH, 11);
				limit = cal.getTime();
				break;
			case THREEMONTH:
				cal.add(GregorianCalendar.MONTH, 9);
				limit = cal.getTime();
				break;
			case SIXMONTH:
				cal.add(GregorianCalendar.MONTH, 6);
				limit = cal.getTime();
				break;
			}
			IDbManager dbManager = Core.getCore().getDbManager();
			for (SmartCollectionImpl sm : dbManager.obtainObjects(SmartCollectionImpl.class, "lastAccessDate", limit, //$NON-NLS-1$
					QueryField.GREATER)) {
				history.add(sm);
				if (monitor.isCanceled())
					return;
			}
			monitor.worked(10);
			history.addAll(dbManager.obtainObjects(SlideShowImpl.class, "lastAccessDate", limit, //$NON-NLS-1$
					QueryField.GREATER));
			if (monitor.isCanceled())
				return;
			monitor.worked(1);
			history.addAll(dbManager.obtainObjects(ExhibitionImpl.class, "lastAccessDate", limit, //$NON-NLS-1$
					QueryField.GREATER));
			if (monitor.isCanceled())
				return;
			monitor.worked(1);
			history.addAll(dbManager.obtainObjects(WebGalleryImpl.class, "lastAccessDate", limit, //$NON-NLS-1$
					QueryField.GREATER));
			if (monitor.isCanceled())
				return;
			monitor.worked(1);
			Collections.sort(history, new Comparator<Object>() {
				public int compare(Object o1, Object o2) {
					return getDate(o2).compareTo(getDate(o1));
				}

				private Date getDate(Object o) {
					if (o instanceof HistoryTitle)
						return ((HistoryTitle) o).getTitleDate();
					if (o instanceof SmartCollectionImpl)
						return ((SmartCollectionImpl) o).getLastAccessDate();
					if (o instanceof SlideShowImpl)
						return ((SlideShowImpl) o).getLastAccessDate();
					if (o instanceof ExhibitionImpl)
						return ((ExhibitionImpl) o).getLastAccessDate();
					if (o instanceof WebGalleryImpl)
						return ((WebGalleryImpl) o).getLastAccessDate();
					return MINDATE;
				}
			});
			Object previous = null;
			LinkedList<Object> folded = new LinkedList<Object>();
			ListIterator<Object> listIterator = history.listIterator(history.size());
			while (listIterator.hasPrevious()) {
				Object item = listIterator.previous();
				if (item instanceof HistoryTitle && previous instanceof HistoryTitle)
					continue;
				previous = item;
				folded.add(0, item);
			}
			String previousLabel = ""; //$NON-NLS-1$
			Iterator<Object> it = folded.iterator();
			while (it.hasNext()) {
				Object item = it.next();
				if (item instanceof SmartCollection && ((SmartCollection) item).getAdhoc()) {
					String label = composeLabel((SmartCollection) item);
					if (previousLabel.equals(label))
						it.remove();
					else
						previousLabel = label;
				} else
					previousLabel = ""; //$NON-NLS-1$
			}
			history = folded;
			monitor.done();
			final Control control = viewer.getControl();
			if (!control.isDisposed())
				control.getDisplay().asyncExec(() -> {
					if (!control.isDisposed()) {
						viewer.setInput(history);
						control.setCursor(control.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
					}
					--settingSelection;
				});
		}
	}

	private class HistoryTitle implements ITitle {
		private String name;
		private Date titleDate;
		private int type;

		public HistoryTitle(String name, Date titleDate, int type) {
			this.name = name;
			this.titleDate = titleDate;
			this.type = type;
		}

		public Date getTitleDate() {
			return titleDate;
		}

		@Override
		public String toString() {
			return name;
		}

		public int getType() {
			return type;
		}
	}

	private class HistoryLabelProvider extends CatalogLabelProvider {

		HistoryLabelProvider(IAdaptable adaptable) {
			super(adaptable);
		}

		@Override
		public String getText(Object element) {
			if (element instanceof SmartCollection)
				return composeLabel((SmartCollection) element);
			return element.toString();
		}
	}

	public static final String ID = "com.bdaum.zoom.ui.views.HistoryView"; //$NON-NLS-1$
	private static final Date MINDATE = new Date(0L);
	private static final int TODAY = 0;
	private static final int YESTERDAY = 1;
	private static final int WEEK = 2;
	private static final int MONTH = 3;
	private static final int OLDER = 4;
	private static final int ONEMONTH = 0;
	private static final int THREEMONTH = 1;
	private static final int SIXMONTH = 2;
	private static final int ONEYEAR = 3;
	private static final String HISTORYLIMIT = "historyLimit"; //$NON-NLS-1$

	private int settingSelection;
	private LinkedList<Object> history = new LinkedList<Object>();
	private HistoryAction todayAction, yesterdayAction, weekAction, monthAction, olderAction;
	private int historyLimit = ONEYEAR;
	private LimitAction threeMonthAction;
	private LimitAction sixMonthAction;
	private LimitAction oneYearAction;
	private LimitAction oneMonthAction;

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		if (memento != null) {
			Integer integer = memento.getInteger(HISTORYLIMIT);
			if (integer != null)
				historyLimit = integer;
		}
		new CleanupJob().schedule();
	}

	@Override
	public void saveState(IMemento memento) {
		memento.putInteger(HISTORYLIMIT, historyLimit);
		super.saveState(memento);
	}

	@Override
	public void createPartControl(final Composite parent) {
		viewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setLabelProvider(new HistoryLabelProvider(this));
		viewer.setComparer(IdentifiedElementComparer.getInstance());
		ZColumnViewerToolTipSupport.enableFor(viewer);
//		setInput();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), HelpContextIds.HISTORY_VIEW);
		addCtrlKeyListener();
		viewer.addSelectionChangedListener(this);
		addKeyListener();
		addGestureListener(((TableViewer) viewer).getTable());
		makeActions();
		installListeners();
		hookContextMenu(viewer);
		hookDoubleClickAction();
		contributeToActionBars();
		Core.getCore().addCatalogListener(new CatalogAdapter() {
			@Override
			public void assetsModified(BagChange<Asset> changes, QueryField node) {
				if (changes == null || changes.hasChanges())
					forceSelectionUpdate();
			}

			private void forceSelectionUpdate() {
				Shell shell = getSite().getShell();
				if (shell != null && !shell.isDisposed())
					shell.getDisplay().asyncExec(() -> {
						if (!shell.isDisposed()) {
							ISelection selection = viewer.getSelection();
							viewer.setSelection(StructuredSelection.EMPTY);
							viewer.setSelection(selection, true);
						}
					});
			}

			@Override
			public void setCatalogSelection(final ISelection selection, final boolean forceUpdate) {
				Shell shell = getSite().getShell();
				if (shell != null && !shell.isDisposed())
					shell.getDisplay().asyncExec(() -> {
						if (!shell.isDisposed())
							setSelection(selection, forceUpdate);
					});
			}

			@Override
			public void catalogOpened(boolean newDb) {
				cancelJobs(HistoryView.this);
				Shell shell = getSite().getShell();
				if (shell != null && !shell.isDisposed()) {
					Display display = shell.getDisplay();
					display.asyncExec(() -> {
						if (!shell.isDisposed())
							BusyIndicator.showWhile(display, () -> structureModified());
					});
				}
				new HistoryJob().schedule(3000L);
			}
			
			@Override
			public void catalogClosed(int mode) {
				cancelJobs(HistoryView.this);
			}
		});
		updateActions(viewer.getStructuredSelection(), true);
		getNavigationHistory().addHistoryListener(this);
		switch (historyLimit) {
		case ONEMONTH:
			oneMonthAction.setChecked(true);
			break;
		case THREEMONTH:
			threeMonthAction.setChecked(true);
			break;
		case SIXMONTH:
			sixMonthAction.setChecked(true);
			break;
		default:
			oneYearAction.setChecked(true);
			break;
		}
	}
	
	public void selectionChanged(final SelectionChangedEvent event) {
		if (settingSelection <= 0) {
			cancelJobs(HistoryView.this);
			new SelectionJob(viewer, event).schedule();
		}
		if (cntrlDwn && editItemAction.isEnabled()) {
			editItemAction.run();
			cntrlDwn = false;
		}
	}
	
	@Override
	public void dispose() {
		getNavigationHistory().removeHistoryListener(this);
		super.dispose();
	}

	@Override
	protected void fireSelection(SelectionChangedEvent event) {
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		Object obj = selection.getFirstElement();
		String perspId = null;
		if (obj instanceof SlideShowImpl) {
			perspId = ((SlideShowImpl) obj).getPerspective();
			if (perspId == null)
				perspId = "com.bdaum.zoom.SlidesPerspective"; //$NON-NLS-1$
		} else if (obj instanceof ExhibitionImpl) {
			perspId = ((ExhibitionImpl) obj).getPerspective();
			if (perspId == null)
				perspId = "com.bdaum.zoom.ExhibitionPerspective"; //$NON-NLS-1$
		} else if (obj instanceof WebGalleryImpl) {
			perspId = ((WebGalleryImpl) obj).getPerspective();
			if (perspId == null)
				perspId = "com.bdaum.zoom.WebGalleryPerspective"; //$NON-NLS-1$
		} else if (obj instanceof SmartCollectionImpl) {
			perspId = ((SmartCollectionImpl) obj).getPerspective();
			if (perspId == null)
				perspId = "com.bdaum.zoom.rcp.LightboxPerspective"; //$NON-NLS-1$
		}
		if (perspId != null) {
			IWorkbenchPage page = getViewSite().getPage();
			if (!page.getPerspective().getId().equals(perspId)) {
				IPerspectiveDescriptor perspective = PlatformUI.getWorkbench().getPerspectiveRegistry()
						.findPerspectiveWithId(perspId);
				if (perspective != null)
					page.setPerspective(perspective);
			}
		}
		super.fireSelection(event);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(todayAction);
		manager.add(yesterdayAction);
		manager.add(weekAction);
		manager.add(monthAction);
		manager.add(olderAction);
		manager.add(new Separator());
		manager.add(oneMonthAction);
		manager.add(threeMonthAction);
		manager.add(sixMonthAction);
		manager.add(oneYearAction);
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(todayAction);
		manager.add(yesterdayAction);
		manager.add(weekAction);
		manager.add(monthAction);
		manager.add(olderAction);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(this);
	}
	
	public void doubleClick(DoubleClickEvent event) {
		if (!cntrlDwn)
			editItemAction.run();
	}

	@Override
	protected void makeActions() {
		super.makeActions();
		todayAction = new HistoryAction(TODAY, Messages.getString("HistoryView.today"), //$NON-NLS-1$
				Icons.today.getDescriptor(), Messages.getString("HistoryView.today_tooltip")); //$NON-NLS-1$
		yesterdayAction = new HistoryAction(YESTERDAY, Messages.getString("HistoryView.yesterday"), //$NON-NLS-1$
				Icons.yesterday.getDescriptor(), Messages.getString("HistoryView.yesterday_tooltip")); //$NON-NLS-1$
		weekAction = new HistoryAction(WEEK, Messages.getString("HistoryView.week"), //$NON-NLS-1$
				Icons.week.getDescriptor(), Messages.getString("HistoryView.week_tooltip")); //$NON-NLS-1$
		monthAction = new HistoryAction(MONTH, Messages.getString("HistoryView.month"), //$NON-NLS-1$
				Icons.month.getDescriptor(), Messages.getString("HistoryView.month_tooltip")); //$NON-NLS-1$
		olderAction = new HistoryAction(OLDER, Messages.getString("HistoryView.older"), //$NON-NLS-1$
				Icons.history.getDescriptor(), Messages.getString("HistoryView.older_tooltip")); //$NON-NLS-1$
		oneMonthAction = new LimitAction(ONEMONTH, Messages.getString("HistoryView.one_month"), //$NON-NLS-1$
				Messages.getString("HistoryView.one_month_tooltip")); //$NON-NLS-1$
		threeMonthAction = new LimitAction(THREEMONTH, Messages.getString("HistoryView.three_month"), //$NON-NLS-1$
				Messages.getString("HistoryView.three_month_tooltip")); //$NON-NLS-1$
		sixMonthAction = new LimitAction(THREEMONTH, Messages.getString("HistoryView.six_month"), //$NON-NLS-1$
				Messages.getString("HistoryView.six_month_tooltip")); //$NON-NLS-1$
		oneYearAction = new LimitAction(THREEMONTH, Messages.getString("HistoryView.one_year"), //$NON-NLS-1$
				Messages.getString("HistoryView.one_year_tooltip")); //$NON-NLS-1$
	}

	public ISelection getSelection() {
		return viewer.getSelection();
	}

	public void setSelection(ISelection selection) {
		++settingSelection;
		try {
			if (selection instanceof IStructuredSelection) {
				viewer.setSelection(selection, true);
				updateActions((IStructuredSelection) selection, false);
			}
		} finally {
			--settingSelection;
		}
	}

	public Control getControl() {
		return viewer.getControl();
	}

	@Override
	public boolean selectionChanged() {
		return true;
	}

	@Override
	public boolean assetsChanged() {
		return false;
	}

	@Override
	public boolean collectionChanged() {
		return true;
	}

	@Override
	public void refresh() {
		if (isVisible()) {
			cancelJobs(this);
			new HistoryJob().schedule();
		}
	}

	@Override
	public void setFocus() {
		getControl().setFocus();
	}

	@Override
	protected void fillContextMenu(IMenuManager manager) {
		updateActions((IStructuredSelection) getSelection(), true);
		super.fillContextMenu(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	public void historyChanged() {
		// do nothing
	}

	public void queryHistoryChanged(IdentifiableObject obj) {
		new HistoryUpdateJob(obj, obj).schedule();
	}

	public void removeItem(IdentifiableObject obj) {
		new HistoryUpdateJob(obj, null).schedule();
	}

	private static String composeLabel(SmartCollection sm) {
		StringBuilder sb = new StringBuilder();
		do {
			if (sb.length() > 0)
				sb.insert(0, '>');
			sb.insert(0, sm.getAdhoc() ? UiUtilities.composeContentDescription(sm, " - ", true, false) : sm.getName()); //$NON-NLS-1$
			sm = sm.getSmartCollection_subSelection_parent();
		} while (sm != null);
		return sb.toString();
	}

}
