/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jacek Pospychala <jacek.pospychala@pl.ibm.com> - bugs 202583, 202584, 207344
 *     													bugs 207323, 207931, 207101
 *     													bugs 172658, 216341, 216657
 *     Benjamin Cabe <benjamin.cabe@anyware-tech.com> - bug 218648
 *     Tuukka Lehtonen <tuukka.lehtonen@semantum.fi>  - bug 247907
 *
 *     bdaum - simplified and adapted to ZoRaPD
 *******************************************************************************/

package org.eclipse.ui.internal.views.log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;

@SuppressWarnings("restriction")
public class LogView extends ViewPart implements ILogListener {
	public static final String P_LOG_WARNING = "warning"; //$NON-NLS-1$
	public static final String P_LOG_ERROR = "error"; //$NON-NLS-1$
	public static final String P_LOG_INFO = "info"; //$NON-NLS-1$
	public static final String P_LOG_OK = "ok"; //$NON-NLS-1$
	public static final String P_LOG_LIMIT = "limit"; //$NON-NLS-1$
	public static final String P_USE_LIMIT = "useLimit"; //$NON-NLS-1$
	public static final String P_SHOW_ALL_SESSIONS = "allSessions"; //$NON-NLS-1$
	public static final String P_ACTIVATE = "activate"; //$NON-NLS-1$

	private int DATE_ORDER = DESCENDING;

	public final static byte DATE = 0x2;
	public static int ASCENDING = 1;
	public static int DESCENDING = -1;

	public static final int GROUP_BY_SESSION = 1;

	private List elements;
	private Map groups;
	private LogSession currentSession;

	private List batchedEntries;
	private boolean batchEntries;

	private Clipboard fClipboard;

	private IMemento fMemento;
	private File fInputFile;
	private String fDirectory;

	private Comparator fComparator;

	// hover text
	private boolean fCanOpenTextShell;
	private Text fTextLabel;
	private Shell fTextShell;

	private boolean fFirstEvent = true;

	private Action fDeleteLogAction;
	private Action fCopyAction;
	private Action fActivateViewAction;
	private Action fExportLogAction;
	private Action fExportLogEntryAction;

	private TreeViewer viewer;
	private DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
	private DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
	private SashForm sashForm;
	private Text stackTraceText;
	private Text sessionDataText;
	private Label dateLabel;
	private Label severityImageLabel;
	private Label severityLabel;
	private Text msgText;
	private AbstractEntry entry;

	private final class MessageLabelProvider extends ZColumnLabelProvider {
		private Image infoImage;
		private Image okImage;
		private Image errorImage;
		private Image warningImage;
		private Image errorWithStackImage;
		private Image hierarchicalImage;

		public MessageLabelProvider() {
			errorImage = SharedImages.getImage(SharedImages.DESC_ERROR_ST_OBJ);
			warningImage = SharedImages.getImage(SharedImages.DESC_WARNING_ST_OBJ);
			infoImage = SharedImages.getImage(SharedImages.DESC_INFO_ST_OBJ);
			okImage = SharedImages.getImage(SharedImages.DESC_OK_ST_OBJ);
			errorWithStackImage = SharedImages.getImage(SharedImages.DESC_ERROR_STACK_OBJ);
			hierarchicalImage = SharedImages.getImage(SharedImages.DESC_HIERARCHICAL_LAYOUT_OBJ);
		}

		@Override
		public String getText(Object element) {

			if ((element instanceof LogSession)) {
				LogSession session = (LogSession) element;
				if (session.getDate() == null)
					return element.toString();
				return NLS.bind(Messages.LogView_starting_at, element.toString(), dateFormat.format(session.getDate()));
			}
			if (element instanceof LogEntry) {
				LogEntry entry = (LogEntry) element;
				if (entry.getMessage() != null) {
					String message = entry.getMessage();
					String time = timeFormat.format(entry.getDate());
					return NLS.bind("{0} {1}", time, message); //$NON-NLS-1$
				}
			}
			return ""; //$NON-NLS-1$
		}

		public Image getImage(Object element) {
			if (element instanceof Group)
				return hierarchicalImage;

			LogEntry entry = (LogEntry) element;
			switch (entry.getSeverity()) {
			case IStatus.INFO:
				return infoImage;
			case IStatus.OK:
				return okImage;
			case IStatus.WARNING:
				return warningImage;
			default:
				return (entry.getStack() == null ? errorImage : errorWithStackImage);
			}
		}
	}

	/**
	 * Constructor
	 */
	public LogView() {
		elements = new ArrayList();
		groups = new HashMap();
		batchedEntries = new ArrayList();
		fInputFile = Platform.getLogFileLocation().toFile();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.
	 * widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		readLogFile();
		createViewer(composite);
		createDetailsArea(parent);
		createActions();
		fClipboard = new Clipboard(parent.getDisplay());
		viewer.getTree().setToolTipText(""); //$NON-NLS-1$
		makeHoverShell();

		Platform.addLogListener(this);
	}

	private void createDetailsArea(Composite parent) {
		CGroup container = new CGroup(parent, SWT.NONE);
		container.setLayout(new GridLayout());
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		createDetailsSection(container);
		createSashForm(container);
		createStackSection(getSashForm());
		createSessionSection(getSashForm());
		container.setText(Messages.LogView_details);
		updateProperties();
	}

	private void createDetailsSection(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = layout.marginHeight = 0;
		container.setLayout(layout);
		container.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		createTextSection(container);
	}

	public void updateProperties() {
		if (entry instanceof LogEntry) {
			LogEntry logEntry = (LogEntry) entry;

			String strDate = dateFormat.format(logEntry.getDate());
			dateLabel.setText(strDate);
			severityImageLabel.setImage(((MessageLabelProvider) viewer.getLabelProvider(0)).getImage(entry));
			severityLabel.setText(logEntry.getSeverityText());
			msgText.setText(logEntry.getMessage() != null ? logEntry.getMessage() : ""); //$NON-NLS-1$
			String stack = logEntry.getStack();

			if (stack != null) {
				stackTraceText.setText(stack);
			} else {
				stackTraceText.setText(Messages.EventDetailsDialog_noStack);
			}

			if (logEntry.getSession() != null) {
				String session = logEntry.getSession().getSessionData();
				if (session != null) {
					sessionDataText.setText(session);
				}
			}

		} else {
			dateLabel.setText(""); //$NON-NLS-1$
			severityImageLabel.setImage(null);
			severityLabel.setText(""); //$NON-NLS-1$
			msgText.setText(""); //$NON-NLS-1$
			stackTraceText.setText(""); //$NON-NLS-1$
			sessionDataText.setText(""); //$NON-NLS-1$
		}
	}

	public SashForm getSashForm() {
		return sashForm;
	}

	private void createSashForm(Composite parent) {
		sashForm = new SashForm(parent, SWT.VERTICAL);
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		sashForm.setLayout(layout);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	private void createTextSection(Composite parent) {
		Composite textContainer = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		layout.marginHeight = layout.marginWidth = 0;
		textContainer.setLayout(layout);
		textContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label label = new Label(textContainer, SWT.NONE);
		label.setText(Messages.EventDetailsDialog_date);
		dateLabel = new Label(textContainer, SWT.NULL);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		dateLabel.setLayoutData(gd);

		label = new Label(textContainer, SWT.NONE);
		label.setText(Messages.EventDetailsDialog_severity);
		severityImageLabel = new Label(textContainer, SWT.NULL);
		severityLabel = new Label(textContainer, SWT.NULL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		severityLabel.setLayoutData(gd);

		label = new Label(textContainer, SWT.NONE);
		label.setText(Messages.EventDetailsDialog_message);
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		label.setLayoutData(gd);
		msgText = new Text(textContainer, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP | SWT.BORDER);
		msgText.setEditable(false);
		gd = new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_BEGINNING | GridData.GRAB_VERTICAL);
		gd.horizontalSpan = 2;
		gd.heightHint = 44;
		gd.grabExcessVerticalSpace = true;
		msgText.setLayoutData(gd);
	}

	private void createStackSection(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 6;
		container.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 100;
		container.setLayoutData(gd);

		Label label = new Label(container, SWT.NONE);
		label.setText(Messages.EventDetailsDialog_exception);
		gd = new GridData();
		gd.verticalAlignment = SWT.BOTTOM;
		label.setLayoutData(gd);

		stackTraceText = new Text(container, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 2;
		stackTraceText.setLayoutData(gd);
		stackTraceText.setEditable(false);
	}

	private void createSessionSection(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 6;
		container.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 100;
		container.setLayoutData(gd);

		Label line = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.widthHint = 1;
		line.setLayoutData(gd);

		Label label = new Label(container, SWT.NONE);
		label.setText(Messages.EventDetailsDialog_session);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		label.setLayoutData(gd);
		sessionDataText = new Text(container, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		sessionDataText.setLayoutData(gd);
		sessionDataText.setEditable(false);
	}

	/**
	 * Creates the actions for the viewsite action bars
	 */
	private void createActions() {
		IActionBars bars = getViewSite().getActionBars();

		fCopyAction = createCopyAction();
		bars.setGlobalActionHandler(ActionFactory.COPY.getId(), fCopyAction);

		IToolBarManager toolBarManager = bars.getToolBarManager();

		fExportLogAction = createExportLogAction();
		toolBarManager.add(fExportLogAction);

		fExportLogEntryAction = createExportLogEntryAction();

		toolBarManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

		fDeleteLogAction = createDeleteLogAction();
		toolBarManager.add(fDeleteLogAction);

		toolBarManager.add(new Separator());

		IMenuManager mgr = bars.getMenuManager();

		mgr.add(createFilterAction());
		mgr.add(new Separator());

		fActivateViewAction = createActivateViewAction();
		mgr.add(fActivateViewAction);

		MenuManager popupMenuManager = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		IMenuListener listener = new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				manager.add(fCopyAction);
				manager.add(new Separator());
				manager.add(fDeleteLogAction);
				manager.add(new Separator());
				manager.add(fExportLogAction);
				manager.add(new Separator());
				manager.add(fExportLogEntryAction);
				manager.add(new Separator());
				manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
			}
		};
		popupMenuManager.addMenuListener(listener);
		popupMenuManager.setRemoveAllWhenShown(true);
		getSite().registerContextMenu(popupMenuManager, getSite().getSelectionProvider());
		Tree fTree = viewer.getTree();
		Menu menu = popupMenuManager.createContextMenu(fTree);
		fTree.setMenu(menu);
	}

	private Action createActivateViewAction() {
		Action action = new Action(Messages.LogView_activate) { //
			@Override
			public void run() {
				fMemento.putString(P_ACTIVATE, isChecked() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		};
		action.setChecked(fMemento.getString(P_ACTIVATE).equals("true")); //$NON-NLS-1$
		return action;
	}

	private Action createCopyAction() {
		Action action = new Action(Messages.LogView_copy) {
			@Override
			public void run() {
				copyToClipboard(viewer.getSelection());
			}
		};
		action.setImageDescriptor(
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		return action;
	}

	private Action createDeleteLogAction() {
		Action action = new Action(Messages.LogView_delete) {
			@Override
			public void run() {
				doDeleteLog();
			}
		};
		action.setToolTipText(Messages.LogView_delete_tooltip);
		action.setImageDescriptor(SharedImages.getImageDescriptor(SharedImages.DESC_REMOVE_LOG));
		action.setDisabledImageDescriptor(SharedImages.getImageDescriptor(SharedImages.DESC_REMOVE_LOG_DISABLED));
		action.setEnabled(fInputFile.exists() && fInputFile.equals(Platform.getLogFileLocation().toFile()));
		return action;
	}

	private Action createExportLogAction() {
		Action action = new Action(Messages.LogView_export) {
			@Override
			public void run() {
				handleExport(true);
			}
		};
		action.setToolTipText(Messages.LogView_export_tooltip);
		action.setImageDescriptor(SharedImages.getImageDescriptor(SharedImages.DESC_EXPORT));
		action.setDisabledImageDescriptor(SharedImages.getImageDescriptor(SharedImages.DESC_EXPORT_DISABLED));
		action.setEnabled(fInputFile.exists());
		return action;
	}

	private Action createExportLogEntryAction() {
		Action action = new Action(Messages.LogView_exportEntry) {
			@Override
			public void run() {
				handleExport(false);
			}
		};
		action.setToolTipText(Messages.LogView_exportEntry_tooltip);
		action.setImageDescriptor(SharedImages.getImageDescriptor(SharedImages.DESC_EXPORT));
		action.setDisabledImageDescriptor(SharedImages.getImageDescriptor(SharedImages.DESC_EXPORT_DISABLED));
		action.setEnabled(!viewer.getSelection().isEmpty());
		return action;
	}

	private Action createFilterAction() {
		Action action = new Action(Messages.LogView_filter) {
			@Override
			public void run() {
				handleFilter();
			}
		};
		action.setToolTipText(Messages.LogView_filter);
		action.setImageDescriptor(SharedImages.getImageDescriptor(SharedImages.DESC_FILTER));
		action.setDisabledImageDescriptor(SharedImages.getImageDescriptor(SharedImages.DESC_FILTER_DISABLED));
		return action;
	}

	/**
	 * Creates the Show Text Filter view menu action
	 * 
	 * @return the new action for the Show Text Filter
	 */

	/**
	 * Shows/hides the filter text control from the filtered tree. This method
	 * also sets the P_SHOW_FILTER_TEXT preference to the visible state
	 *
	 * @param visible
	 *            if the filter text control should be shown or not
	 */

	private void createViewer(Composite parent) {
		viewer = new TreeViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.widthHint = 500;
		viewer.getControl().setLayoutData(layoutData);
		viewer.getTree().setLinesVisible(true);
		viewer.getTree().setHeaderVisible(true);
		TreeViewerColumn col1 = new TreeViewerColumn(viewer, SWT.NONE);
		col1.getColumn().setText(Messages.LogView_column_message);
		col1.getColumn().setWidth(300);
		col1.setLabelProvider(new MessageLabelProvider());
		viewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
		viewer.setContentProvider(new LogViewContentProvider(this));
		ViewerComparator comparator = getViewerComparator(DATE);
		viewer.setComparator(comparator);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent e) {
				handleSelectionChanged(e.getSelection());
			}
		});
		viewer.setInput(this);
		addMouseListeners();
		addDragSource();
	}

	@Override
	public void dispose() {
		writeSettings();
		Platform.removeLogListener(this);
		if (fClipboard != null)
			fClipboard.dispose();
		if (fTextShell != null)
			fTextShell.dispose();
		super.dispose();
	}

	/**
	 * Import log from file selected in FileDialog.
	 */
	void handleImport() {
		FileDialog dialog = new FileDialog(getViewSite().getShell());
		dialog.setFilterExtensions(new String[] { "*.log" }); //$NON-NLS-1$
		if (fDirectory != null)
			dialog.setFilterPath(fDirectory);
		String path = dialog.open();
		if (path == null) { // cancel
			return;
		}

		File file = new Path(path).toFile();
		if (file.exists()) {
			handleImportPath(path);
		} else {
			String msg = NLS.bind(Messages.LogView_FileCouldNotBeFound, file.getName());
			MessageDialog.openError(getViewSite().getShell(), Messages.LogView_OpenFile, msg);
		}
	}

	/**
	 * Import log from given file path. Do nothing if file not exists.
	 * 
	 * @param path
	 *            path to log file.
	 */
	public void handleImportPath(String path) {
		File file = new File(path);
		if (path != null && file.exists()) {
			setLogFile(file);
		}
	}

	/**
	 * Import log from given file path.
	 * 
	 * @param path
	 *            path to log file.
	 */
	protected void setLogFile(File path) {
		fInputFile = path;
		fDirectory = fInputFile.getParent();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				monitor.beginTask(Messages.LogView_operation_importing, IProgressMonitor.UNKNOWN);
				readLogFile();
			}
		};
		ProgressMonitorDialog pmd = new ProgressMonitorDialog(getViewSite().getShell());
		try {
			pmd.run(true, true, op);
		} catch (InvocationTargetException e) { // do nothing
		} catch (InterruptedException e) { // do nothing
		} finally {
			asyncRefresh(false);
		}
	}

	@SuppressWarnings({ "null", "unused" })
	private void handleExport(boolean exportWholeLog) {
		FileDialog dialog = new FileDialog(getViewSite().getShell(), SWT.SAVE);
		dialog.setFilterExtensions(new String[] { "*.log" }); //$NON-NLS-1$
		if (fDirectory != null)
			dialog.setFilterPath(fDirectory);
		String path = dialog.open();
		if (path != null) {
			if (path.indexOf('.') == -1 && !path.endsWith(".log")) //$NON-NLS-1$
				path += ".log"; //$NON-NLS-1$
			File outputFile = new Path(path).toFile();
			fDirectory = outputFile.getParent();
			if (outputFile.exists()) {
				String message = NLS.bind(Messages.LogView_confirmOverwrite_message, outputFile.toString());
				if (!MessageDialog.openQuestion(getViewSite().getShell(),
						(exportWholeLog ? Messages.LogView_exportLog : Messages.LogView_exportLogEntry), message))
					return;
			}

			Reader in = null;
			Writer out = null;
			try {
				out = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"); //$NON-NLS-1$
				if (exportWholeLog)
					in = new InputStreamReader(new FileInputStream(fInputFile), "UTF-8"); //$NON-NLS-1$
				else {
					String selectedEntryAsString = selectionToString(viewer.getSelection());
					in = new StringReader(selectedEntryAsString);
				}
				copy(in, out);
			} catch (IOException ex) {
				try {
					if (in != null)
						in.close();
					if (out != null)
						out.close();
				} catch (IOException e1) { // do nothing
				}
			}
		}
	}

	private static void copy(Reader input, Writer output) {
		BufferedReader reader = null;
		BufferedWriter writer = null;
		try {
			reader = new BufferedReader(input);
			writer = new BufferedWriter(output);
			String line;
			while (reader.ready() && ((line = reader.readLine()) != null)) {
				writer.write(line);
				writer.newLine();
			}
		} catch (IOException e) { // do nothing
		} finally {
			try {
				if (reader != null)
					reader.close();
				if (writer != null)
					writer.close();
			} catch (IOException e1) {
				// do nothing
			}
		}
	}

	private void handleFilter() {
		FilterDialog dialog = new FilterDialog(
				Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(), fMemento);
		dialog.create();
		dialog.getShell().setText(Messages.LogView_FilterDialog_title);
		if (dialog.open() == Window.OK)
			reloadLog();
	}

	private void doDeleteLog() {
		String title = Messages.LogView_confirmDelete_title;
		String message = Messages.LogView_confirmDelete_message;
		if (!MessageDialog.openConfirm(getSite().getShell(), title, message))
			return;
		if (fInputFile.delete() || elements.size() > 0) {
			handleClear();
		}
	}

	public void fillContextMenu(IMenuManager manager) { // nothing
	}

	public AbstractEntry[] getElements() {
		return (AbstractEntry[]) elements.toArray(new AbstractEntry[elements.size()]);
	}

	protected void handleClear() {
		BusyIndicator.showWhile(getSite().getShell().getDisplay(), new Runnable() {
			public void run() {
				elements.clear();
				groups.clear();
				if (currentSession != null) {
					currentSession.removeAllChildren();
				}
				asyncRefresh(false);
			}
		});
	}

	/**
	 * Reloads the log
	 */
	protected void reloadLog() {
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				monitor.beginTask(Messages.LogView_operation_reloading, IProgressMonitor.UNKNOWN);
				readLogFile();
			}
		};
		ProgressMonitorDialog pmd = new ProgressMonitorDialog(getViewSite().getShell());
		try {
			pmd.run(true, true, op);
		} catch (InvocationTargetException e) { // do nothing
		} catch (InterruptedException e) { // do nothing
		} finally {
			asyncRefresh(false);
		}
	}

	/**
	 * Reads the chosen backing log file
	 */
	void readLogFile() {
		elements.clear();
		groups.clear();

		List result = new ArrayList();
		LogSession lastLogSession = LogReader.parseLogFile(fInputFile, result, fMemento);
		if ((lastLogSession != null) && isEclipseStartTime(lastLogSession.getDate())) {
			currentSession = lastLogSession;
		} else {
			currentSession = null;
		}

		group(result);
		limitEntriesCount();
	}

	private static boolean isEclipseStartTime(Date date) {
		String ts = System.getProperty("eclipse.startTime"); //$NON-NLS-1$
		try {
			return (ts != null && date != null && date.getTime() == Long.parseLong(ts));
		} catch (NumberFormatException e) {
			// empty
		}
		return false;
	}

	/**
	 * Add new entries to correct groups in the view.
	 * 
	 * @param entries
	 *            new entries to show up in groups in the view.
	 */
	private void group(List entries) {
		for (Iterator i = entries.iterator(); i.hasNext();) {
			LogEntry entry = (LogEntry) i.next();
			Group group = getGroup(entry);
			group.addChild(entry);
		}
	}

	/**
	 * Limits the number of entries according to the max entries limit set in
	 * memento.
	 */
	private void limitEntriesCount() {
		int limit = Integer.MAX_VALUE;
		if (fMemento.getString(LogView.P_USE_LIMIT).equals("true")) {//$NON-NLS-1$
			limit = fMemento.getInteger(LogView.P_LOG_LIMIT).intValue();
		}

		int entriesCount = getEntriesCount();

		if (entriesCount <= limit) {
			return;
		}
		Comparator dateComparator = new Comparator() {
			public int compare(Object o1, Object o2) {
				Date l1 = ((LogEntry) o1).getDate();
				Date l2 = ((LogEntry) o2).getDate();
				if ((l1 != null) && (l2 != null)) {
					return l1.before(l2) ? -1 : 1;
				} else if ((l1 == null) && (l2 == null)) {
					return 0;
				} else
					return (l1 == null) ? -1 : 1;
			}
		};

		List copy = new ArrayList(entriesCount);
		for (Iterator i = elements.iterator(); i.hasNext();) {
			AbstractEntry group = (AbstractEntry) i.next();
			copy.addAll(Arrays.asList(group.getChildren(group)));
		}

		Collections.sort(copy, dateComparator);
		List toRemove = copy.subList(0, copy.size() - limit);

		for (Iterator i = elements.iterator(); i.hasNext();) {
			AbstractEntry group = (AbstractEntry) i.next();
			group.removeChildren(toRemove);
		}
	}

	private int getEntriesCount() {
		int size = 0;
		for (Iterator i = elements.iterator(); i.hasNext();) {
			AbstractEntry group = (AbstractEntry) i.next();
			size += group.size();
		}
		return size;
	}

	/**
	 * Returns group appropriate for the entry. Group depends on P_GROUP_BY
	 * preference, or is null if grouping is disabled (GROUP_BY_NONE), or group
	 * could not be determined. May create group if it haven't existed before.
	 *
	 * @param entry
	 *            entry to be grouped
	 * @return group or null if grouping is disabled
	 */
	protected Group getGroup(LogEntry entry) {
		Object elementGroupId = null;
		elementGroupId = entry.getSession();
		if (elementGroupId == null) { // could not determine group
			return null;
		}
		Group group = (Group) groups.get(elementGroupId);
		if (group == null) {
			group = entry.getSession();
			groups.put(elementGroupId, group);
			elements.add(group);
		}
		return group;
	}

	public void logging(IStatus status, String plugin) {
		if (!isPlatformLogOpen())
			return;

		if (batchEntries) {
			// create LogEntry immediately to don't loose IStatus creation date.
			LogEntry entry = createLogEntry(status);
			batchedEntries.add(entry);
			return;
		}

		if (fFirstEvent || (currentSession == null)) {
			readLogFile();
			asyncRefresh(true);
			fFirstEvent = false;
		} else {
			LogEntry entry = createLogEntry(status);

			if (!batchedEntries.isEmpty()) {
				// batch new entry as well, to have only one asyncRefresh()
				batchedEntries.add(entry);
				pushBatchedEntries();
			} else {
				pushEntry(entry);
				// filteredAsyncRefresh(entry);
			}
		}
	}

	/**
	 * Push batched entries to log view.
	 */
	private void pushBatchedEntries() {
		Job job = new Job(Messages.LogView_AddingBatchedEvents) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				for (int i = 0; i < batchedEntries.size(); i++) {
					if (!monitor.isCanceled()) {
						LogEntry entry = (LogEntry) batchedEntries.get(i);
						pushEntry(entry);
						batchedEntries.remove(i);
					}
				}
				asyncRefresh(true);
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	private LogEntry createLogEntry(IStatus status) {
		LogEntry entry = new LogEntry(status);
		entry.setSession(currentSession);
		return entry;
	}

	private synchronized void pushEntry(LogEntry entry) {
		if (LogReader.isLogged(entry, fMemento)) {
			group(Collections.singletonList(entry));
			limitEntriesCount();
		}
		asyncRefresh(true);
	}

	private void asyncRefresh(final boolean activate) {
		Tree fTree = viewer.getTree();
		if (fTree.isDisposed())
			return;
		Display display = fTree.getDisplay();
		final ViewPart view = this;
		if (display != null) {
			display.asyncExec(new Runnable() {
				public void run() {
					if (!fTree.isDisposed()) {
						// TreeViewer viewer = fFilteredTree.getViewer();
						viewer.refresh();
						viewer.expandAll();
						fDeleteLogAction.setEnabled(
								fInputFile.exists() && fInputFile.equals(Platform.getLogFileLocation().toFile()));
						// fOpenLogAction.setEnabled(fInputFile.exists());
						fExportLogAction.setEnabled(fInputFile.exists());
						fExportLogEntryAction.setEnabled(!viewer.getSelection().isEmpty());
						if (activate && fActivateViewAction.isChecked()) {
							IWorkbenchPage page = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow()
									.getActivePage();
							if (page != null)
								page.bringToTop(view);
						}
					}
				}
			});
		}
	}

	@Override
	public void setFocus() {
		Control control = viewer.getControl();
		if (!control.isDisposed())
			control.setFocus();
	}

	private void handleSelectionChanged(ISelection selection) {
		updateStatus(selection);
		Object firstElement = ((IStructuredSelection) selection).getFirstElement();
		fCopyAction.setEnabled(firstElement != null);
		fExportLogEntryAction.setEnabled(!selection.isEmpty());
		this.entry = (AbstractEntry) firstElement;
		updateProperties();
	}

	private void updateStatus(ISelection selection) {
		IStatusLineManager status = getViewSite().getActionBars().getStatusLineManager();
		if (selection.isEmpty())
			UiActivator.getDefault().setMesssage(status, null, false);
		else {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			UiActivator.getDefault().setMesssage(status,
					((MessageLabelProvider) viewer.getLabelProvider(0)).getText(element), false);
		}
	}

	/**
	 * Converts selected log view element to string.
	 * 
	 * @return textual log entry representation or null if selection doesn't
	 *         contain log entry
	 */
	private static String selectionToString(ISelection selection) {
		try (StringWriter writer = new StringWriter(); PrintWriter pwriter = new PrintWriter(writer)) {
			if (selection.isEmpty())
				return null;
			AbstractEntry entry = (AbstractEntry) ((IStructuredSelection) selection).getFirstElement();
			entry.write(pwriter);
			pwriter.flush();
			return writer.toString();
		} catch (IOException e) {
			// ignore
		}
		return null;
	}

	/**
	 * Copies selected element to clipboard.
	 */
	private void copyToClipboard(ISelection selection) {
		String textVersion = selectionToString(selection);
		if ((textVersion != null) && (textVersion.trim().length() > 0)) {
			// set the clipboard contents
			fClipboard.setContents(new Object[] { textVersion }, new Transfer[] { TextTransfer.getInstance() });
		}
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		if (memento == null)
			this.fMemento = XMLMemento.createWriteRoot("LOGVIEW"); //$NON-NLS-1$
		else
			this.fMemento = memento;
		readSettings();
		setComparator(DATE);
	}

	private void initializeMemento() {
		if (fMemento.getString(P_USE_LIMIT) == null) {
			fMemento.putString(P_USE_LIMIT, "true"); //$NON-NLS-1$
		}
		if (fMemento.getInteger(P_LOG_LIMIT) == null) {
			fMemento.putInteger(P_LOG_LIMIT, 50);
		}
		if (fMemento.getString(P_LOG_INFO) == null) {
			fMemento.putString(P_LOG_INFO, "true"); //$NON-NLS-1$
		}
		if (fMemento.getString(P_LOG_OK) == null) {
			fMemento.putString(P_LOG_OK, "true"); //$NON-NLS-1$
		}
		if (fMemento.getString(P_LOG_WARNING) == null) {
			fMemento.putString(P_LOG_WARNING, "true"); //$NON-NLS-1$
		}
		if (fMemento.getString(P_LOG_ERROR) == null) {
			fMemento.putString(P_LOG_ERROR, "true"); //$NON-NLS-1$
		}
		if (fMemento.getString(P_SHOW_ALL_SESSIONS) == null) {
			fMemento.putString(P_SHOW_ALL_SESSIONS, "true"); //$NON-NLS-1$
		}
	}

	@Override
	public void saveState(IMemento memento) {
		if (this.fMemento == null || memento == null)
			return;
		this.fMemento.putString(P_ACTIVATE, fActivateViewAction.isChecked() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
		memento.putMemento(this.fMemento);
		writeSettings();
	}

	private void addMouseListeners() {
		Listener tableListener = new Listener() {
			public void handleEvent(Event e) {
				switch (e.type) {
				case SWT.MouseExit:
				case SWT.MouseMove:
					onMouseMove(e);
					break;
				case SWT.MouseHover:
					onMouseHover(e);
					break;
				case SWT.MouseDown:
					onMouseDown(e);
					break;
				}
			}
		};
		int[] tableEvents = new int[] { SWT.MouseDown, SWT.MouseMove, SWT.MouseHover, SWT.MouseExit };
		for (int i = 0; i < tableEvents.length; i++) {
			viewer.getTree().addListener(tableEvents[i], tableListener);
		}
	}

	/**
	 * Adds drag source support to error log tree.
	 */
	private void addDragSource() {
		DragSource source = new DragSource(viewer.getTree(), DND.DROP_COPY);
		Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
		source.setTransfer(types);

		source.addDragListener(new DragSourceAdapter() {

			@Override
			public void dragStart(DragSourceEvent event) {
				ISelection selection = viewer.getSelection();
				if (selection.isEmpty()) {
					event.doit = false;
					return;
				}

				AbstractEntry entry = (AbstractEntry) ((TreeSelection) selection).getFirstElement();
				if (!(entry instanceof LogEntry)) {
					event.doit = false;
					return;
				}
			}

			@Override
			public void dragSetData(DragSourceEvent event) {
				if (!TextTransfer.getInstance().isSupportedType(event.dataType)) {
					return;
				}
				ISelection selection = viewer.getSelection();
				String textVersion = selectionToString(selection);
				event.data = textVersion;
			}
		});
	}

	private void makeHoverShell() {
		fTextShell = new Shell(getSite().getShell(), SWT.NO_FOCUS | SWT.ON_TOP | SWT.TOOL);
		Display display = fTextShell.getDisplay();
		fTextShell.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		GridLayout layout = new GridLayout(1, false);
		int border = ((getSite().getShell().getStyle() & SWT.NO_TRIM) == 0) ? 0 : 1;
		layout.marginHeight = border;
		layout.marginWidth = border;
		fTextShell.setLayout(layout);
		fTextShell.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Composite shellComposite = new Composite(fTextShell, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		shellComposite.setLayout(layout);
		shellComposite.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_BEGINNING));
		fTextLabel = new Text(shellComposite, SWT.WRAP | SWT.MULTI | SWT.READ_ONLY);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 100;
		gd.grabExcessHorizontalSpace = true;
		fTextLabel.setLayoutData(gd);
		Color c = fTextShell.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
		fTextLabel.setBackground(c);
		c = fTextShell.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND);
		fTextLabel.setForeground(c);
		fTextLabel.setEditable(false);
		fTextShell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				onTextShellDispose(e);
			}
		});
	}

	void onTextShellDispose(DisposeEvent e) {
		fCanOpenTextShell = true;
		setFocus();
	}

	void onMouseDown(Event e) {
		if (fTextShell != null && !fTextShell.isDisposed() && !fTextShell.isFocusControl()) {
			fTextShell.setVisible(false);
			fCanOpenTextShell = true;
		}
	}

	void onMouseHover(Event e) {
		if (!fCanOpenTextShell || fTextShell == null || fTextShell.isDisposed())
			return;
		Tree fTree = viewer.getTree();
		fCanOpenTextShell = false;
		Point point = new Point(e.x, e.y);
		TreeItem item = fTree.getItem(point);
		if (item == null)
			return;

		String message = null;
		if (item.getData() instanceof LogEntry) {
			message = ((LogEntry) item.getData()).getStack();
		} else if (item.getData() instanceof LogSession) {
			LogSession session = ((LogSession) item.getData());
			message = Messages.LogView_SessionStarted;
			if (session.getDate() != null) {
				DateFormat formatter = new SimpleDateFormat(LogEntry.F_DATE_FORMAT);
				message += ' ' + formatter.format(session.getDate());
			}
		}

		if (message == null)
			return;

		fTextLabel.setText(message);
		Rectangle bounds = fTree.getDisplay().getPrimaryMonitor().getBounds();
		Point cursorPoint = fTree.getDisplay().getCursorLocation();
		int x = point.x;
		int y = point.y + 25;
		int width = fTree.getColumn(0).getWidth();
		int height = 125;
		if (cursorPoint.x + width > bounds.width)
			x -= width;
		if (cursorPoint.y + height + 25 > bounds.height)
			y -= height + 27;

		fTextShell.setLocation(fTree.toDisplay(x, y));
		fTextShell.setSize(width, height);
		fTextShell.setVisible(true);
	}

	void onMouseMove(Event e) {
		if (fTextShell != null && !fTextShell.isDisposed() && fTextShell.isVisible())
			fTextShell.setVisible(false);

		Point point = new Point(e.x, e.y);
		TreeItem item = viewer.getTree().getItem(point);
		if (item == null)
			return;
		Image image = item.getImage();
		Object data = item.getData();
		if (data instanceof LogEntry) {
			LogEntry entry = (LogEntry) data;
			int parentCount = getNumberOfParents(entry);
			int startRange = 20 + Math.max(image.getBounds().width + 2, 7 + 2) * parentCount;
			int endRange = startRange + 16;
			fCanOpenTextShell = e.x >= startRange && e.x <= endRange;
		}
	}

	private int getNumberOfParents(AbstractEntry entry) {
		AbstractEntry parent = (AbstractEntry) entry.getParent(entry);
		if (parent == null)
			return 0;
		return 1 + getNumberOfParents(parent);
	}

	public Comparator getComparator() {
		return fComparator;
	}

	private void setComparator(byte sortType) {
		if (sortType == DATE) {
			fComparator = new Comparator() {
				public int compare(Object e1, Object e2) {
					long date1 = 0;
					long date2 = 0;
					if ((e1 instanceof LogEntry) && (e2 instanceof LogEntry)) {
						date1 = ((LogEntry) e1).getDate().getTime();
						date2 = ((LogEntry) e2).getDate().getTime();
					} else if ((e1 instanceof LogSession) && (e2 instanceof LogSession)) {
						date1 = ((LogSession) e1).getDate() == null ? 0 : ((LogSession) e1).getDate().getTime();
						date2 = ((LogSession) e2).getDate() == null ? 0 : ((LogSession) e2).getDate().getTime();
					}
					if (date1 == date2) {
						int result = elements.indexOf(e2) - elements.indexOf(e1);
						if (DATE_ORDER == DESCENDING)
							result *= DESCENDING;
						return result;
					}
					if (DATE_ORDER == DESCENDING)
						return date1 > date2 ? DESCENDING : ASCENDING;
					return date1 < date2 ? DESCENDING : ASCENDING;
				}
			};
		}
	}

	private ViewerComparator getViewerComparator(byte sortType) {
		return new ViewerComparator() {
			private int indexOf(Object[] array, Object o) {
				if (o == null)
					return -1;
				for (int i = 0; i < array.length; ++i)
					if (o.equals(array[i]))
						return i;
				return -1;
			}

			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				long date1 = 0;
				long date2 = 0;
				if ((e1 instanceof LogEntry) && (e2 instanceof LogEntry)) {
					date1 = ((LogEntry) e1).getDate().getTime();
					date2 = ((LogEntry) e2).getDate().getTime();
				} else if ((e1 instanceof LogSession) && (e2 instanceof LogSession)) {
					date1 = ((LogSession) e1).getDate() == null ? 0 : ((LogSession) e1).getDate().getTime();
					date2 = ((LogSession) e2).getDate() == null ? 0 : ((LogSession) e2).getDate().getTime();
				}

				if (date1 == date2) {
					// Everything that appears in LogView should be an
					// AbstractEntry.
					AbstractEntry parent = (AbstractEntry) ((AbstractEntry) e1).getParent(null);
					Object[] children = null;
					if (parent != null)
						children = parent.getChildren(parent);

					int result = 0;
					if (children != null) {
						// The elements in children seem to be in reverse
						// order,
						// i.e. latest log message first, therefore
						// index(e2)-index(e1)
						result = indexOf(children, e2) - indexOf(children, e1);
					} else {
						result = elements.indexOf(e1) - elements.indexOf(e2);
					}
					if (DATE_ORDER == DESCENDING)
						result *= DESCENDING;
					return result;
				}
				if (DATE_ORDER == DESCENDING)
					return date1 > date2 ? DESCENDING : ASCENDING;
				return date1 < date2 ? DESCENDING : ASCENDING;
			}
		};
		// }
	}


	/**
	 * Returns the filter dialog settings object used to maintain state between
	 * filter dialogs
	 * 
	 * @return the dialog settings to be used
	 */
	private IDialogSettings getLogSettings() {
		IDialogSettings settings = Activator.getDefault().getDialogSettings();
		return settings.getSection(getClass().getName());
	}

	/**
	 * Returns the plugin preferences used to maintain state of log view
	 * 
	 * @return the plugin preferences
	 */
	private static Preferences getLogPreferences() {
		return InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
	}

	/**
	 * Loads any saved {@link IDialogSettings} into the backing view memento
	 */
	private void readSettings() {
		IDialogSettings s = getLogSettings();
		if (s == null) {
			initializeMemento();
		} else {
			fMemento.putString(P_USE_LIMIT, s.getBoolean(P_USE_LIMIT) ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
			fMemento.putString(P_LOG_INFO, s.getBoolean(P_LOG_INFO) ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
			fMemento.putString(P_LOG_OK, s.getBoolean(P_LOG_OK) ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
			fMemento.putString(P_LOG_WARNING, s.getBoolean(P_LOG_WARNING) ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
			fMemento.putString(P_LOG_ERROR, s.getBoolean(P_LOG_ERROR) ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
			fMemento.putString(P_SHOW_ALL_SESSIONS, s.getBoolean(P_SHOW_ALL_SESSIONS) ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
			try {
				fMemento.putInteger(P_LOG_LIMIT, s.getInt(P_LOG_LIMIT));
			} catch (NumberFormatException e) {
				fMemento.putInteger(P_LOG_LIMIT, 50);
			}
		}
		Preferences p = getLogPreferences(); // never returns null
		fMemento.putBoolean(P_ACTIVATE, p.getBoolean(P_ACTIVATE, false));
	}

	/**
	 * Returns the width to use for the column represented by the given key. The
	 * default width is returned iff:
	 * <ul>
	 * <li>There is no preference for the given key</li>
	 * <li>The returned preference value is too small, making the columns
	 * invisible by width.</li>
	 * </ul>
	 * 
	 * @param preferences
	 * @param key
	 * @param defaultwidth
	 * @return the stored width for the a column described by the given key or
	 *         the default width
	 *
	 * @since 3.6
	 */
	int getColumnWidthPreference(Preferences preferences, String key, int defaultwidth) {
		int width = preferences.getInt(key, defaultwidth);
		return width < 1 ? defaultwidth : width;
	}

	private void writeSettings() {
		writeViewSettings();
		writeFilterSettings();
	}

	private void writeFilterSettings() {
		IDialogSettings settings = getLogSettings();
		if (settings == null)
			settings = Activator.getDefault().getDialogSettings().addNewSection(getClass().getName());
		settings.put(P_USE_LIMIT, fMemento.getString(P_USE_LIMIT).equals("true")); //$NON-NLS-1$
		settings.put(P_LOG_LIMIT, fMemento.getInteger(P_LOG_LIMIT).intValue());
		settings.put(P_LOG_INFO, fMemento.getString(P_LOG_INFO).equals("true")); //$NON-NLS-1$
		settings.put(P_LOG_OK, fMemento.getString(P_LOG_OK).equals("true")); //$NON-NLS-1$
		settings.put(P_LOG_WARNING, fMemento.getString(P_LOG_WARNING).equals("true")); //$NON-NLS-1$
		settings.put(P_LOG_ERROR, fMemento.getString(P_LOG_ERROR).equals("true")); //$NON-NLS-1$
		settings.put(P_SHOW_ALL_SESSIONS, fMemento.getString(P_SHOW_ALL_SESSIONS).equals("true")); //$NON-NLS-1$
	}

	private void writeViewSettings() {
		Preferences preferences = getLogPreferences();
		preferences.putBoolean(P_ACTIVATE, fMemento.getBoolean(P_ACTIVATE).booleanValue());
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			// empty
		}
	}

	protected Job getOpenLogFileJob() {
		return new Job(Messages.OpenLogDialog_message) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				boolean failed = false;
				if (fInputFile.length() <= LogReader.MAX_FILE_LENGTH) {
					failed = !Program.launch(fInputFile.getAbsolutePath());
					if (failed) {
						Program p = Program.findProgram(".txt"); //$NON-NLS-1$
						if (p != null) {
							p.execute(fInputFile.getAbsolutePath());
							return Status.OK_STATUS;
						}
					}
				}
				return Status.OK_STATUS;
			}
		};
	}

	protected File getLogFile() {
		return fInputFile;
	}

	/**
	 * Returns whether given session equals to currently displayed in LogView.
	 * 
	 * @param session
	 *            LogSession
	 * @return true if given session equals to currently displayed in LogView
	 */
	public boolean isCurrentLogSession(LogSession session) {
		return isPlatformLogOpen() && (currentSession != null) && (currentSession.equals(session));
	}

	/**
	 * Returns whether currently open log is platform log or imported file.
	 * 
	 * @return true if currently open log is platform log, false otherwise
	 */
	public boolean isPlatformLogOpen() {
		return (fInputFile.equals(Platform.getLogFileLocation().toFile()));
	}

	/**
	 *
	 */
	public void setPlatformLog() {
		setLogFile(Platform.getLogFileLocation().toFile());
	}
}
