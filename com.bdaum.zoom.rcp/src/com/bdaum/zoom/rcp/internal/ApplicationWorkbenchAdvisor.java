/*******************************************************************************
 * Copyright (c) 2009-2015 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/

package com.bdaum.zoom.rcp.internal;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.bdaum.zoom.batch.internal.BatchActivator;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.common.CommonConstants;
import com.bdaum.zoom.common.internal.FileLocator;
import com.bdaum.zoom.core.CatalogListener;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.db.IDbErrorHandler;
import com.bdaum.zoom.core.db.IValidator;
import com.bdaum.zoom.core.internal.CatLocation;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.operations.ImportConfiguration;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.ImportOperation;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.program.IRawConverter;
import com.bdaum.zoom.rcp.internal.perspective.LightboxPerspective;
import com.bdaum.zoom.ui.IUi;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.dialogs.TimedMessageDialog;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.Icons.Icon;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiConstants;
import com.bdaum.zoom.ui.internal.actions.Startup;
import com.bdaum.zoom.ui.internal.commands.AbstractCatCommandHandler;
import com.bdaum.zoom.ui.internal.commands.OpenCatalogCommand;
import com.bdaum.zoom.ui.internal.dialogs.ConflictDialog;
import com.bdaum.zoom.ui.internal.dialogs.DNGConverterDialog;
import com.bdaum.zoom.ui.internal.dialogs.InvalidFileDialog;
import com.bdaum.zoom.ui.internal.preferences.ImportPreferencePage;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;

@SuppressWarnings("restriction")
public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor implements IAdaptable {

	private final class ErrorHandler implements IDbErrorHandler {
		private final IWorkbenchConfigurer configurer;

		public ErrorHandler(IWorkbenchConfigurer configurer) {
			this.configurer = configurer;
		}

		public void fatalError(final String title, final String msg, IAdaptable adaptable) {
			final Shell shell = getShell(adaptable);
			syncExec(shell, () -> {
				if (!shell.isDisposed()) {
					AcousticMessageDialog.openError(shell, title, msg);
					shell.getDisplay().dispose();
				}
			});
			System.exit(4);
		}

		public void invalidFile(final String title, final String message, final URI uri, final IProgressMonitor monitor,
				final IAdaptable adaptable) {
			final Shell shell = getShell(adaptable);
			final InvalidFileDialog dialog = new InvalidFileDialog(shell, title, message, uri, monitor);
			syncExec(shell, () -> {
				if (!shell.isDisposed()) {
					dialog.open();
				}
			});
		}

		public void connectionLostWarning(final String title, final String message, final IAdaptable adaptable) {
			final Shell shell = getShell(adaptable);
			syncExec(shell, () -> {
				if (!shell.isDisposed() && !AcousticMessageDialog.openQuestion(shell, title, message))
					return;
				configurer.emergencyClose();
			});
		}

		public void promptForReconnect(final String title, final String message, final IInputValidator validator,
				final IAdaptable adaptable) {
			final Shell shell = getShell(adaptable);
			syncExec(shell, () -> {
				if (!shell.isDisposed()
						&& new TimedMessageDialog(shell, validator, title, null, message, MessageDialog.WARNING,
								new String[] { IDialogConstants.RETRY_LABEL, IDialogConstants.ABORT_LABEL }, 0, 0,
								1000L).open() != 0) {
					shell.getDisplay().dispose();
					System.exit(4);
				}
			});
		}

		public boolean question(String title, String message, IAdaptable adaptable) {
			final Shell shell = getShell(adaptable);
			final AcousticMessageDialog dialog = new AcousticMessageDialog(shell, title, null, message,
					MessageDialog.QUESTION, new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 0);
			syncExec(shell, () -> dialog.open());
			return dialog.getReturnCode() == 0;
		}

		public void showError(final String title, final String message, IAdaptable adaptable) {
			final Shell shell = getShell(adaptable);
			asyncExec(shell, () -> {
				if (!shell.isDisposed())
					AcousticMessageDialog.openError(shell, title, message);
			});
		}

		public void showWarning(final String title, final String message, IAdaptable adaptable) {
			final Shell shell = getShell(adaptable);
			asyncExec(shell, () -> {
				if (!shell.isDisposed())
					AcousticMessageDialog.openWarning(shell, title, message);
			});
		}

		public void showInformation(final String title, final String message, IAdaptable adaptable) {
			showInformation(title, message, adaptable, null);
		}

		public void showInformation(final String title, final String message, IAdaptable adaptable,
				final IValidator validator) {
			final Shell shell = getShell(adaptable);
			asyncExec(shell, () -> {
				if (!shell.isDisposed())
					AcousticMessageDialog.openInformation(shell, title, message, validator);
			});
		}

		public int showMessageDialog(String dialogTitle, Image dialogTitleImage, String dialogMessage,
				int dialogImageType, String[] dialogButtonLabels, int defaultIndex, IAdaptable adaptable) {
			final Shell shell = getShell(adaptable);
			final AcousticMessageDialog dialog = new AcousticMessageDialog(shell, dialogTitle, dialogTitleImage,
					dialogMessage, dialogImageType, dialogButtonLabels, defaultIndex);
			syncExec(shell, () -> dialog.open());
			return dialog.getReturnCode();
		}

		public ImportConfiguration showConflictDialog(String title, String message, Asset asset,
				ImportConfiguration currentConfig, boolean multi, IAdaptable adaptable) {
			final Shell shell = getShell(adaptable);
			final ConflictDialog dialog = new ConflictDialog(shell, title, message, asset, currentConfig, multi);
			syncExec(shell, () -> dialog.open());
			return dialog.getCurrentConfig();
		}

		public void alarmOnPrompt(String sound) {
			IUi ui = Ui.getUi();
			if (ui != null)
				ui.playSound(sound, PreferenceConstants.ALARMONPROMPT);
		}

		public void signalEOJ(String sound) {
			IUi ui = Ui.getUi();
			if (ui != null)
				ui.playSound(sound, PreferenceConstants.ALARMONFINISH);
		}

		private void syncExec(Shell shell, Runnable runnable) {
			if (shell != null && !shell.isDisposed())
				shell.getDisplay().syncExec(runnable);
		}

		private void asyncExec(Shell shell, Runnable runnable) {
			if (shell != null && !shell.isDisposed())
				shell.getDisplay().asyncExec(runnable);
		}

		public Shell getShell(IAdaptable adaptable) {
			Shell shell = adaptable == null ? null : (Shell) adaptable.getAdapter(Shell.class);
			if (shell == null) {
				final Display display = Display.getDefault();
				if (!display.isDisposed()) {
					display.syncExec(() -> {
						auxShell = display.getActiveShell();
						if (auxShell == null)
							auxShell = new Shell(display);
					});
					shell = auxShell;
					auxShell = null;
				}
			}
			return shell;
		}

		public File showDngDialog(File dngLocation, IAdaptable adaptable) {
			Shell shell = getShell(adaptable);
			final DNGConverterDialog dialog = new DNGConverterDialog(shell, dngLocation);
			syncExec(shell, () -> dialog.open());
			return dialog.getResult();
		}

		public IRawConverter showRawDialog(IAdaptable adaptable) {
			final Shell shell = getShell(adaptable);
			boolean result = question(Constants.APPLICATION_NAME,
					Messages.getString("ApplicationWorkbenchAdvisor.no_raw_converter"), adaptable); //$NON-NLS-1$
			if (result) {
				final int ret[] = new int[1];
				syncExec(shell, () -> ret[0] = PreferencesUtil.createPreferenceDialogOn(shell, ImportPreferencePage.ID, new String[0],
						ImportPreferencePage.RAW).open());
				if (ret[0] == Dialog.OK)
					return BatchActivator.getDefault().getCurrentRawConverter(true);
			}
			return null;
		}
	}

	private static final String CURRENT_CATALOG = "com.bdaum.zoom.currentCatalog"; //$NON-NLS-1$
	private static final String CURRENT_VOLUME = "com.bdaum.zoom.currentVolume"; //$NON-NLS-1$
	private static final String RECENTCATS = "com.bdaum.zoom.recentCats"; //$NON-NLS-1$

	private final static int RESTORE = 0;

	private final static int QUIT = 1;

	private final static String[] trayMenuItems = new String[] {
			Messages.getString("ApplicationWorkbenchAdvisor.restore"), //$NON-NLS-1$
			Messages.getString("ApplicationWorkbenchAdvisor.quit") }; //$NON-NLS-1$
	private static final int APPSTARTING = 0;
	private static final int RUNNING = 1;
	private static final int MAINTENANCE = 2;

	private MenuItem[] menuItems = new MenuItem[trayMenuItems.length];
	private TrayItem trayItem;
	private Shell auxShell;
	private long sendToTray = 0L;
	private boolean initialized;
	private WorkbenchHelper workbenchHelper;

	@Override
	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		return new ApplicationWorkbenchWindowAdvisor(configurer, this);
	}

	@Override
	public String getInitialWindowPerspectiveId() {
		return LightboxPerspective.ID;
	}

	@Override
	public void initialize(final IWorkbenchConfigurer configurer) {
		Core.getCore().getDbFactory().setErrorHandler(new ErrorHandler(configurer));
		configurer.setSaveAndRestore(true);
		workbenchHelper = new WorkbenchHelper(this, true);
		workbenchHelper.restoreWorkbenchState();
	}

	@Override
	public void postStartup() {
		super.postStartup();
		final IWorkbench workbench = getWorkbenchConfigurer().getWorkbench();
		workbench.getPreferenceManager().remove("org.eclipse.help.ui.browsersPreferencePage"); //$NON-NLS-1$
		System.setProperty("zoom.version", Platform.getProduct() //$NON-NLS-1$
				.getDefiningBundle().getVersion().toString());
		System.setProperty("zoom.year", String.valueOf(new GregorianCalendar().get(GregorianCalendar.YEAR))); //$NON-NLS-1$
		FontRegistry fontRegistry = JFaceResources.getFontRegistry();
		FontData fontData = fontRegistry.defaultFontDescriptor().getFontData()[0];
		fontRegistry.put(UiConstants.MESSAGEFONT,
				new FontData[] { new FontData(fontData.getName(), fontData.getHeight() + 4, fontData.getStyle()) });
		fontRegistry.put(UiConstants.SELECTIONFONT,
				new FontData[] { new FontData(fontData.getName(), fontData.getHeight(), SWT.BOLD) });
		fontData = fontRegistry.getDescriptor(JFaceResources.HEADER_FONT).getFontData()[0];
		fontRegistry.put(UiConstants.MESSAGETITLEFONT,
				new FontData[] { new FontData(fontData.getName(), fontData.getHeight() + 4, fontData.getStyle()) });
		// tray
		IWorkbenchWindow[] workbenchWindows = workbench.getWorkbenchWindows();
		if (workbenchWindows.length > 0) {
			IWorkbenchWindow workbenchWindow = workbenchWindows[0];
			final Shell shell = workbenchWindow.getShell();
			final Tray tray = workbench.getDisplay().getSystemTray();
			if (tray != null) {
				trayItem = new TrayItem(tray, SWT.NONE);
				trayItem.setData("id", RcpActivator.PLUGIN_ID + "_" + System.currentTimeMillis()); //$NON-NLS-1$ //$NON-NLS-2$
				setTrayVisible(true, APPSTARTING);
				trayItem.addListener(SWT.Show, new Listener() {
					public void handleEvent(Event event) {
						// do nothing
					}
				});
				trayItem.addListener(SWT.Hide, new Listener() {
					public void handleEvent(Event event) {
						// do nothing
					}
				});
				trayItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						restoreHideWindows(workbench);
					}
				});
				if (!shell.isDisposed()) {
					final Menu menu = new Menu(shell, SWT.POP_UP);
					for (int i = 0; i < trayMenuItems.length; i++) {
						menuItems[i] = new MenuItem(menu, SWT.PUSH);
						menuItems[i].setText(trayMenuItems[i]);
						final int cmd = i;
						menuItems[i].addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								switch (cmd) {
								case QUIT:
									BatchActivator.setFastExit(false);
									workbench.close();
									break;
								case RESTORE:
									restoreHideWindows(workbench);
									break;
								}
							}
						});
					}
					trayItem.addListener(SWT.MenuDetect, new Listener() {
						public void handleEvent(Event event) {
							if (!menu.isDisposed()) {
								menuItems[RESTORE].setText(!shell.isDisposed() && shell.isVisible()
										? Messages.getString("ApplicationWorkbenchAdvisor.hide_window") //$NON-NLS-1$
										: Messages.getString("ApplicationWorkbenchAdvisor.restore_window")); //$NON-NLS-1$
								menu.setVisible(true);
							}
						}
					});
				}
				setTrayMode();
				IUi ui = Ui.getUi();
				if (ui != null)
					ui.addPreferenceChangeListener(new IEclipsePreferences.IPreferenceChangeListener() {
						public void preferenceChange(PreferenceChangeEvent event) {
							if (event.getKey().equals(PreferenceConstants.TRAY_MODE))
								setTrayMode();
						}
					});
			}
		}
		OperationJob.addOperationJobListener(new JobChangeAdapter() {
			private int imports;
			private Timer importTimer;
			private boolean blinkState;
			private Runnable blinkRunnable;
			private Runnable doneRunnable;

			@Override
			public void running(IJobChangeEvent event) {
				if (event.getJob() instanceof OperationJob
						&& ((OperationJob) event.getJob()).getOperation() instanceof ImportOperation) {
					if (imports == 0) {
						if (trayItem != null && !trayItem.isDisposed()) {
							blinkState = false;
							if (blinkRunnable == null)
								blinkRunnable = new Runnable() {
									public void run() {
										if (!trayItem.isDisposed()) {
											blinkState = !blinkState;
											trayItem.setToolTipText(
													Messages.getString("ApplicationWorkbenchAdvisor.importing_images")); //$NON-NLS-1$
											trayItem.setImage(
													blinkState ? Icons.appIcon24.getImage() : Icons.appIcon.getImage());
										}
									}
								};
							importTimer = new Timer();
							importTimer.schedule(new TimerTask() {
								@Override
								public void run() {
									trayItem.getDisplay().asyncExec(blinkRunnable);
								}
							}, 50L, 400L);
						}
					}
					++imports;
				}
			}

			@Override
			public void done(IJobChangeEvent event) {
				if (event.getJob() instanceof OperationJob
						&& ((OperationJob) event.getJob()).getOperation() instanceof ImportOperation) {
					--imports;
					if (imports == 0) {
						if (importTimer != null) {
							importTimer.cancel();
							importTimer = null;
						}
						if (trayItem != null && !trayItem.isDisposed()) {
							if (doneRunnable == null)
								doneRunnable = new Runnable() {
									public void run() {
										if (!trayItem.isDisposed()) {
											trayItem.setImage(Icons.appIcon.getImage());
											trayItem.setToolTipText(Constants.APPLICATION_NAME);
										}
									}
								};
							trayItem.getDisplay().asyncExec(doneRunnable);
						}
					}
				}
			}
		});
		System.gc();
	}

	private void setTrayMode() {
		trayItem.setVisible(Platform.getPreferencesService().getBoolean(UiActivator.PLUGIN_ID,
				PreferenceConstants.TRAY_MODE, false, null));
	}

	private void restoreHideWindows(IWorkbench workbench) {
		boolean wasVisible = false;
		IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
		if (windows.length > 0) {
			Shell shell = windows[0].getShell();
			wasVisible = !shell.isDisposed() && shell.isVisible();
			for (int i = 0; i < windows.length; i++) {
				shell = windows[i].getShell();
				shell.setVisible(!wasVisible);
				if (!wasVisible) {
					shell.setMinimized(false);
					shell.forceActive();
					shell.layout();
				} else
					System.gc();
			}
		}
		changeTrayState(wasVisible);
	}

	public void changeTrayState(boolean visible) {
		setTrayVisible(visible, visible ? MAINTENANCE : RUNNING);
		if (visible) {
			if (System.currentTimeMillis() - sendToTray > 1000L) {
				setTrayVisible(true, MAINTENANCE);
				UiActivator.getDefault().performClosingTasks(CatalogListener.TASKBAR);
				workbenchHelper.flushWorkbenchState();
				System.gc();
			}
			setTrayVisible(true, RUNNING);
		} else
			sendToTray = System.currentTimeMillis();
	}

	@Override
	public boolean preShutdown() {
		boolean emergency = isEmergency();
		if (!emergency) {
			setTrayVisible(true, MAINTENANCE);
			workbenchHelper.flushWorkbenchState();
		}
		return UiActivator.getDefault().preCatClose(emergency ? CatalogListener.EMERGENCY : CatalogListener.SHUTDOWN,
				Messages.getString("ApplicationWorkbenchAdvisor.shutdown"), //$NON-NLS-1$
				Messages.getString("ApplicationWorkbenchAdvisor.pending_operations"), true); //$NON-NLS-1$
	}

	private boolean isEmergency() {
		return getWorkbenchConfigurer().emergencyClosing() || Core.getCore().getDbManager().getFile() == null;
	}

	@Override
	public void postShutdown() {
		Core.waitOnJobCanceled(Constants.INDEXING); // Just to be safe
		if (!CommonConstants.DEVELOPMENTMODE && !isEmergency())
			maintainInstallation();
	}

	private static void maintainInstallation() {
		Location installLocation = Platform.getInstallLocation();
		URL url = installLocation == null ? null : installLocation.getURL();
		if (url != null) {
			try {
				File installPath = new File(new URI(BatchUtilities.encodeBlanks(url.toString())));
				UiActivator uiActivator = UiActivator.getDefault();
				String[] updaterCommand = uiActivator.getUpdaterCommand();
				if (updaterCommand != null)
					hookUpdater(updaterCommand, installPath);
				else {
					long lastScan = uiActivator.getPreferenceStore().getLong(PreferenceConstants.LASTPLUGINSCAN);
					File features = new File(installPath, "features"); //$NON-NLS-1$
					File plugins = new File(installPath, "plugins"); //$NON-NLS-1$
					if (features.lastModified() > lastScan || plugins.lastModified() > lastScan)
						hookUpdater(null, installPath);
				}
			} catch (URISyntaxException e) {
				// should never happen
			}
		}
	}

	private static void hookUpdater(String[] updaterCommand, File installPath) {
		UiActivator.getDefault().getPreferenceStore().setValue(PreferenceConstants.LASTPLUGINSCAN,
				System.currentTimeMillis());
		try {
			File file = FileLocator.findFile(RcpActivator.getDefault().getBundle(), "/"); //$NON-NLS-1$
			Runtime.getRuntime().addShutdownHook(new Updater(file, updaterCommand, installPath));
			if (updaterCommand != null)
				RcpActivator.getDefault()
						.logInfo(Messages.getString("ApplicationWorkbenchAdvisor.program_update_started")); //$NON-NLS-1$
		} catch (Exception e) {
			RcpActivator.getDefault()
					.logError(Messages.getString("ApplicationWorkbenchAdvisor.scheduling_update_failed"), e); //$NON-NLS-1$
		}
	}

	@Override
	public IStatus restoreState(IMemento memento) {
		File catFile = extractCatFile(Platform.getApplicationArgs());
		CoreActivator coreActivator = CoreActivator.getDefault();
		if (memento != null) {
			if (catFile == null) {
				String uri = memento.getString(CURRENT_CATALOG);
				if (uri != null) {
					UiActivator.getDefault().setPreviousCatUri(uri);
					String volume = memento.getString(CURRENT_VOLUME);
					if (volume != null)
						catFile = coreActivator.getVolumeManager().findExistingFile(uri, volume);
				}
			}
			String recentCats = memento.getString(RECENTCATS);
			if (recentCats != null && !recentCats.isEmpty()) {
				LinkedList<CatLocation> recentList = new LinkedList<CatLocation>();
				StringTokenizer st = new StringTokenizer(recentCats, "\n"); //$NON-NLS-1$
				while (st.hasMoreTokens())
					recentList.add(new CatLocation(st.nextToken()));
				coreActivator.setRecentCats(recentList);
			}
		}
		coreActivator.setCatFile(catFile);
		return Status.OK_STATUS;
	}

	private static File extractCatFile(String[] commandLineArgs) {
		File catFile = null;
		for (int i = 0; i < commandLineArgs.length; i++) {
			String command = commandLineArgs[i];
			if (command.startsWith("-")) //$NON-NLS-1$
				i++;
			else {
				if (command.endsWith(Constants.CATALOGEXTENSION)) {
					File file = new File(command);
					if (file.exists())
						catFile = file;
				} else if (command.endsWith(Constants.INDEXEXTENSION)) {
					File file = new File(command.substring(0, command.length() - Constants.INDEXEXTENSION.length())
							+ Constants.CATALOGEXTENSION);
					if (file.exists())
						catFile = file;
				}
				break;
			}
		}
		return catFile;
	}

	@Override
	public IStatus saveState(IMemento memento) {
		CoreActivator activator = CoreActivator.getDefault();
		File file = activator.getCatFile();
		if (file != null && file.exists()) {
			memento.putString(CURRENT_CATALOG, file.toURI().toString());
			String volumeLabel = activator.getVolumeManager().getVolumeForFile(file);
			memento.putString(CURRENT_VOLUME, volumeLabel != null ? volumeLabel : ""); //$NON-NLS-1$
		}
		LinkedList<CatLocation> recentList = activator.getRecentCats();
		if (recentList != null && !recentList.isEmpty())
			memento.putString(RECENTCATS, Core.toStringList(recentList, '\n'));
		return super.saveState(memento);
	}

	public void setTrayVisible(boolean visible, int state) {
		if (trayItem != null) {
			Icon icon;
			String tooltip;
			switch (state) {
			case MAINTENANCE:
				tooltip = NLS.bind(Messages.getString("ApplicationWorkbenchAdvisor.is_performing_maintenance"), //$NON-NLS-1$
						Constants.APPLICATION_NAME);
				icon = Icons.appIconShutDown;
				break;
			case APPSTARTING:
				tooltip = NLS.bind(Messages.getString("ApplicationWorkbenchAdvisor.app_starting"), //$NON-NLS-1$
						Constants.APPLICATION_NAME);
				icon = Icons.appStart;
				break;
			default:
				tooltip = Constants.APPLICATION_NAME;
				icon = Icons.appIcon;
				break;
			}
			trayItem.setImage(icon.getImage());
			trayItem.setToolTipText(tooltip);
			trayItem.setVisible(visible);
		}
	}

	@Override
	public void eventLoopIdle(Display display) {
		UiActivator ui = UiActivator.getDefault();
		if (ui.hasStarted()) {
			if (!initialized) {
				initialized = true;
				setTrayVisible(trayItem.getVisible(), RUNNING);
				ui.fireStartListeners();
			}
			IWorkbenchWindow activeWorkbenchWindow = getWorkbenchConfigurer().getWorkbench().getActiveWorkbenchWindow();
			if (activeWorkbenchWindow != null) {
				Shell shell = activeWorkbenchWindow.getShell();
				if (!shell.isDisposed()) {
					String[] parms = CoreActivator.getDefault().testOnShow();
					if (parms != null) {
						if (parms.length > 0) {
							File catFile = extractCatFile(parms);
							if (catFile != null) {
								AbstractCatCommandHandler command = new OpenCatalogCommand();
								command.setCatFile(catFile);
								command.init(activeWorkbenchWindow);
								command.run();
							}
							new Startup().processDroppedImages(parms);
						}
						shell.setVisible(true);
						shell.setMinimized(false);
						shell.forceActive();
						setTrayVisible(false, -1);
					}
				}
			}
		}
		super.eventLoopIdle(display);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getAdapter(Class adapter) {
		if (Shell.class.equals(adapter)) {
			IWorkbenchWindow activeWorkbenchWindow = getWorkbenchConfigurer().getWorkbench().getActiveWorkbenchWindow();
			if (activeWorkbenchWindow != null)
				return activeWorkbenchWindow.getShell();
		}
		return null;
	}

}
