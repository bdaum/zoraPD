/*******************************************************************************
 * Copyright (c) 2009-2018 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/

package com.bdaum.zoom.rcp.internal;

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.operations.IWorkbenchOperationSupport;

import com.bdaum.zoom.batch.internal.BatchActivator;
import com.bdaum.zoom.cat.model.group.Criterion;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.common.CommonUtilities;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IPostProcessor2;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbFactory;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.css.internal.CssActivator;
import com.bdaum.zoom.fileMonitor.internal.filefilter.FilterChain;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.image.internal.ImageActivator;
import com.bdaum.zoom.mtp.DeviceInsertionListener;
import com.bdaum.zoom.rcp.internal.perspective.DataEntryPerspective;
import com.bdaum.zoom.rcp.internal.perspective.ExhibitionPerspective;
import com.bdaum.zoom.rcp.internal.perspective.LightboxPerspective;
import com.bdaum.zoom.rcp.internal.perspective.PresentationPerspective;
import com.bdaum.zoom.rcp.internal.perspective.SleevesPerspective;
import com.bdaum.zoom.rcp.internal.perspective.SlidesPerspective;
import com.bdaum.zoom.rcp.internal.perspective.TablePerspective;
import com.bdaum.zoom.rcp.internal.perspective.WebGalleryPerspective;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.commands.AbstractCommandHandler;
import com.bdaum.zoom.ui.internal.commands.ImportDeviceCommand;
import com.bdaum.zoom.ui.internal.commands.TetheredShootingCommand;
import com.bdaum.zoom.ui.internal.preferences.PreferenceInitializer;
import com.bdaum.zoom.ui.internal.views.DataEntryView;
import com.bdaum.zoom.ui.internal.views.DuplicatesView;
import com.bdaum.zoom.ui.internal.views.ExhibitionView;
import com.bdaum.zoom.ui.internal.views.LightboxView;
import com.bdaum.zoom.ui.internal.views.SlideshowView;
import com.bdaum.zoom.ui.internal.views.TableView;
import com.bdaum.zoom.ui.internal.views.WebGalleryView;
import com.bdaum.zoom.ui.internal.views.ZuiView;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;

@SuppressWarnings("restriction")
public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor implements DeviceInsertionListener {

	private final static String UI_NAMESPACE = UiActivator.PLUGIN_ID;
	private ApplicationWorkbenchAdvisor applicationWorkbenchAdvisor;

	public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer,
			ApplicationWorkbenchAdvisor applicationWorkbenchAdvisor) {
		super(configurer);
		this.applicationWorkbenchAdvisor = applicationWorkbenchAdvisor;
	}

	@Override
	public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
		return new ApplicationActionBarAdvisor(configurer);
	}

	@Override
	public void openIntro() {
		RcpActivator activator = RcpActivator.getDefault();
		String v = activator.getPreferenceStore()
				.getString(com.bdaum.zoom.rcp.internal.PreferenceConstants.PREVIOUSVERSION);
		activator.setNew(v == null || v.isEmpty());
		super.openIntro();
	}

	@Override
	public void postWindowCreate() {
		getWindowConfigurer().getWindow().getShell().setMinimized(Platform.getPreferencesService()
				.getBoolean(UiActivator.PLUGIN_ID, PreferenceConstants.TRAY_MODE, false, null));
	}

	@Override
	public void preWindowOpen() {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		Rectangle clientArea = Display.getCurrent().getMonitors()[0].getClientArea();
		configurer.setInitialSize(new Point(clientArea.width * 4 / 5, clientArea.height * 4 / 5));
		configurer.setShowCoolBar(true);
		configurer.setShowStatusLine(true);
		configurer.setShowProgressIndicator(true);
		configurer.setShowPerspectiveBar(true);
		configurer.setTitle(Constants.APPLICATION_NAME);
		configureInfrastructure(configurer);
	}

	@Override
	public boolean preWindowShellClose() {
		if (Platform.getPreferencesService().getBoolean(UiActivator.PLUGIN_ID, PreferenceConstants.TRAY_MODE, false,
				null)) {
			getWindowConfigurer().getWindow().getShell().setVisible(false);
			applicationWorkbenchAdvisor.changeTrayState(true);
			return false;
		}
		BatchActivator.setFastExit(false);
		return true;
	}

	private void configureInfrastructure(final IWorkbenchWindowConfigurer configurer) {
		final IWorkbenchWindow window = configurer.getWindow();
		final IPreferencesService preferencesService = Platform.getPreferencesService();
		int dt = preferencesService.getInt(UI_NAMESPACE, PreferenceConstants.HOVERDELAY, 200, null);
		int bt = preferencesService.getInt(UI_NAMESPACE, PreferenceConstants.HOVERBASETIME, 1000, null);
		int ct = preferencesService.getInt(UI_NAMESPACE, PreferenceConstants.HOVERCHARTIME, 25, null);
		CommonUtilities.setHoverTiming(dt, bt, ct);
		String customProfile = preferencesService.getString(UI_NAMESPACE, PreferenceConstants.CUSTOMPROFILE, null,
				null);
		ImageActivator.getDefault().resetCustomProfile(customProfile);
		if (ImageConstants.AVAILABLE_PROCESSORS > 1)
			ImageConstants.setNoProcessors(
					preferencesService.getInt(UI_NAMESPACE, PreferenceConstants.NOPROCESSORS, 2, null));
		setTheme(preferencesService.getString(UI_NAMESPACE, PreferenceConstants.BACKGROUNDCOLOR,
				PreferenceConstants.BACKGROUNDCOLOR_DARKGREY, null));
		final CoreActivator activator = CoreActivator.getDefault();
		activator.setBackupInterval(
				preferencesService.getInt(UI_NAMESPACE, PreferenceConstants.BACKUPINTERVAL, 7, null));
		activator.setBackupGenerations(preferencesService.getInt(UI_NAMESPACE, PreferenceConstants.BACKUPGENERATIONS,
				Integer.MAX_VALUE, null));
		activator.setNoProgress(
				preferencesService.getBoolean(UI_NAMESPACE, PreferenceConstants.NOPROGRESS, false, null));
		final IDbFactory dbFactory = CoreActivator.getDefault().getDbFactory();
		dbFactory.setTolerances(
				preferencesService.getString(UI_NAMESPACE, PreferenceConstants.METADATATOLERANCES, "", null)); //$NON-NLS-1$
		dbFactory.setMaxImports(preferencesService.getInt(UI_NAMESPACE, PreferenceConstants.MAXIMPORTS, 99, null));
		dbFactory.setAutoColoringProcessors(createAutoColoringPostProcessors());
		dbFactory.setIndexedFields(
				preferencesService.getString(UI_NAMESPACE, PreferenceConstants.METADATATUNING, "", null)); //$NON-NLS-1$
		dbFactory.setDistanceUnit(
				preferencesService.getString(UI_NAMESPACE, PreferenceConstants.DISTANCEUNIT, "k", null)); //$NON-NLS-1$
		dbFactory.setDimUnit(preferencesService.getString(UI_NAMESPACE, PreferenceConstants.DIMUNIT, "c", null)); //$NON-NLS-1$
		configureUndoLevels(window, preferencesService);
		configureKeywordExclusions(preferencesService);
		configureDeviceInsertionListeners(preferencesService);
		configureRecipeDetectors(preferencesService);
		InstanceScope.INSTANCE.getNode(UI_NAMESPACE)
				.addPreferenceChangeListener(new IEclipsePreferences.IPreferenceChangeListener() {
					public void preferenceChange(PreferenceChangeEvent event) {
						String key = event.getKey().intern();
						if (key == PreferenceConstants.HOVERBASETIME || key == PreferenceConstants.HOVERCHARTIME) {
							int dt = preferencesService.getInt(UI_NAMESPACE, PreferenceConstants.HOVERDELAY, 200, null);
							int bt = preferencesService.getInt(UI_NAMESPACE, PreferenceConstants.HOVERBASETIME, 1000,
									null);
							int ct = preferencesService.getInt(UI_NAMESPACE, PreferenceConstants.HOVERCHARTIME, 25,
									null);
							CommonUtilities.setHoverTiming(dt, bt, ct);
						} else if (key == PreferenceConstants.CUSTOMPROFILE) {
							String customProfile = preferencesService.getString(UI_NAMESPACE,
									PreferenceConstants.CUSTOMPROFILE, null, null);
							if (customProfile != null)
								ImageActivator.getDefault().resetCustomProfile(customProfile);
						} else if (key == PreferenceConstants.NOPROCESSORS) {
							ImageConstants.setNoProcessors(
									preferencesService.getInt(UI_NAMESPACE, PreferenceConstants.NOPROCESSORS, 2, null));
						} else if (key == PreferenceConstants.BACKGROUNDCOLOR) {
							String bgColor = preferencesService.getString(UI_NAMESPACE,
									PreferenceConstants.BACKGROUNDCOLOR, PreferenceConstants.BACKGROUNDCOLOR_DARKGREY,
									null);
							if (PreferenceConstants.BACKGROUNDCOLOR_PLATFORM.equals(bgColor)) {
								UiActivator.getDefault().restart();
								return;
							}
							setTheme(bgColor);
						} else if (key == PreferenceConstants.FILEASSOCIATION)
							UiActivator.getDefault().resetFileEditorMappings();
						else if (key == PreferenceConstants.METADATATOLERANCES)
							dbFactory.setTolerances(preferencesService.getString(UI_NAMESPACE,
									PreferenceConstants.METADATATOLERANCES, "", null)); //$NON-NLS-1$
						else if (key == PreferenceConstants.MAXIMPORTS)
							dbFactory.setMaxImports(
									preferencesService.getInt(UI_NAMESPACE, PreferenceConstants.MAXIMPORTS, 99, null));
						else if (key == PreferenceConstants.AUTOCOLORCODECRIT
								|| key == PreferenceConstants.SHOWCOLORCODE)
							dbFactory.setAutoColoringProcessors(createAutoColoringPostProcessors());
						else if (key == PreferenceConstants.METADATATUNING)
							dbFactory.setIndexedFields(preferencesService.getString(UI_NAMESPACE,
									PreferenceConstants.METADATATUNING, "", null)); //$NON-NLS-1$
						else if (key == PreferenceConstants.DISTANCEUNIT)
							dbFactory.setDistanceUnit(preferencesService.getString(UI_NAMESPACE,
									PreferenceConstants.DISTANCEUNIT, "k", null)); //$NON-NLS-1$
						else if (key == PreferenceConstants.DIMUNIT)
							dbFactory.setDimUnit(
									preferencesService.getString(UI_NAMESPACE, PreferenceConstants.DIMUNIT, "c", null)); //$NON-NLS-1$
						else if (key == PreferenceConstants.NOPROGRESS)
							activator.setNoProgress(preferencesService.getBoolean(UI_NAMESPACE,
									PreferenceConstants.NOPROGRESS, false, null));
						else if (key == PreferenceConstants.UNDOLEVELS)
							configureUndoLevels(window, preferencesService);
						else if (key == PreferenceConstants.KEYWORDFILTER)
							configureKeywordExclusions(preferencesService);
						else if (key == PreferenceConstants.DEVICEWATCH)
							configureDeviceInsertionListeners(preferencesService);
						else if (key == PreferenceConstants.RECIPEDETECTORS
								|| key == PreferenceConstants.PROCESSRECIPES) {
							configureRecipeDetectors(preferencesService);
							activator.getFileWatchManager().updateWatchedMetaFolders();
						} else if (key == PreferenceConstants.BACKUPINTERVAL)
							activator.setBackupInterval(preferencesService.getInt(UI_NAMESPACE,
									PreferenceConstants.BACKUPINTERVAL, 7, null));
						else if (key == PreferenceConstants.BACKUPGENERATIONS)
							activator.setBackupGenerations(preferencesService.getInt(UI_NAMESPACE,
									PreferenceConstants.BACKUPGENERATIONS, Integer.MAX_VALUE, null));
					}
				});
	}

	private static IPostProcessor2[] createAutoColoringPostProcessors() {
		IPreferencesService preferencesService = Platform.getPreferencesService();
		if (!preferencesService
				.getString(UI_NAMESPACE, PreferenceConstants.SHOWCOLORCODE, PreferenceConstants.COLORCODE_NO, null)
				.equals(PreferenceConstants.COLORCODE_AUTO))
			return null;
		IPostProcessor2[] processors = new IPostProcessor2[QueryField.COLORCODELABELS.length - 1];
		String s = preferencesService.getString(UI_NAMESPACE, PreferenceConstants.AUTOCOLORCODECRIT, null, null);
		if (s != null) {
			int off = 0;
			for (int i = 0; i < processors.length; i++) {
				int p = s.indexOf('\n', off);
				if (p < 0)
					break;
				Criterion crit = UiActivator.decodeAutoColoringCriterion(s.substring(off, p));
				off = p + 1;
				if (crit != null) {
					SmartCollectionImpl sm = new SmartCollectionImpl("autoColors", true, false, false, false, "", 0, //$NON-NLS-1$//$NON-NLS-2$
							null, 0, null, Constants.INHERIT_LABEL, null, 0, 1, null);
					sm.addCriterion(crit);
					processors[i] = Core.getCore().getDbFactory().createQueryPostProcessor(sm);
				}
			}
		}
		return processors;
	}

	void configureDeviceInsertionListeners(final IPreferencesService preferencesService) {
		IVolumeManager volumeManager = Core.getCore().getVolumeManager();
		String watch = preferencesService.getString(UI_NAMESPACE, PreferenceConstants.DEVICEWATCH, "false", null); //$NON-NLS-1$
		if ("false".equalsIgnoreCase(watch)) //$NON-NLS-1$
			volumeManager.removeDeviceInsertionListener(ApplicationWorkbenchWindowAdvisor.this);
		else
			volumeManager.addDeviceInsertionListener(ApplicationWorkbenchWindowAdvisor.this);
	}

	void configureKeywordExclusions(final IPreferencesService preferencesService) {
		QueryField.setKeywordFilter(
				new FilterChain(preferencesService.getString(UI_NAMESPACE, PreferenceConstants.KEYWORDFILTER,
						PreferenceInitializer.DEFAULTKEYWORDFILTER, null), ">", "\n", false)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	void configureRecipeDetectors(IPreferencesService preferencesService) {
		boolean processRecipes = preferencesService.getBoolean(UI_NAMESPACE, PreferenceConstants.PROCESSRECIPES, false,
				null);
		List<String> detectorIds = processRecipes ? Core.fromStringList(
				preferencesService.getString(UI_NAMESPACE, PreferenceConstants.RECIPEDETECTORS, null, null), "\n") //$NON-NLS-1$
				: null;
		CoreActivator.getDefault().configureActiveRecipeDetectors(
				detectorIds == null ? null : detectorIds.toArray(new String[detectorIds.size()]), processRecipes);
	}

	private static void setTheme(String theme) {
		CssActivator.getDefault().setTheme(theme);
	}

	@Override
	public void postWindowRestore() throws WorkbenchException {
		IWorkbenchPage activePage = getWindowConfigurer().getWindow().getActivePage();
		if (activePage != null) {
			IViewReference view = activePage.findViewReference(DuplicatesView.ID);
			if (view != null)
				activePage.hideView(view);
		}
	}

	@Override
	public void postWindowOpen() {
		final IWorkbenchWindow window = getWindowConfigurer().getWindow();
		final Shell shell = window.getShell();
		shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellActivated(ShellEvent e) {
				CoreActivator.getDefault().getFileWatchManager().setActiveWindows(true);
				CssActivator.getDefault().setColors(shell);
			}

			@Override
			public void shellDeactivated(ShellEvent e) {
				CoreActivator.getDefault().getFileWatchManager().setActiveWindows(false);
			}
		});
		window.addPerspectiveListener(new IPerspectiveListener() {
			@Override
			public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
				// do nothing
			}

			@Override
			public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
				String id = perspective.getId().intern();
				try {
					if (id == LightboxPerspective.ID)
						page.showView(LightboxView.ID);
					else if (id == SleevesPerspective.ID)
						page.showView(ZuiView.ID);
					else if (id == TablePerspective.ID)
						page.showView(TableView.ID);
					else if (id == DataEntryPerspective.ID)
						page.showView(DataEntryView.ID);
					else if (id == ExhibitionPerspective.ID)
						page.showView(ExhibitionView.ID);
					else if (id == WebGalleryPerspective.ID)
						page.showView(WebGalleryView.ID);
					else if (id == SlidesPerspective.ID)
						page.showView(SlideshowView.ID);
					else if (id == PresentationPerspective.ID) {
						String viewId = Ui.getUi().getNavigationHistory(window).getLastPresentationView();
						if (viewId != null)
							page.showView(viewId);
					}
				} catch (PartInitException e) {
					// should never happen
				}
			}
		});
	}

	public void deviceInserted() {
		String watch = Platform.getPreferencesService().getString(UI_NAMESPACE, PreferenceConstants.DEVICEWATCH,
				"false", null); //$NON-NLS-1$
		final AbstractCommandHandler command = PreferenceConstants.TETHERED.equals(watch)
				? new TetheredShootingCommand()
				: new ImportDeviceCommand();
		IWorkbenchWindow window = getWindowConfigurer().getWindow();
		final Shell shell = window.getShell();
		if (!shell.isDisposed())
			shell.getDisplay().asyncExec(() -> {
				if (!shell.isDisposed())
					try {
						command.init(window);
						command.execute(null);
					} catch (ExecutionException e) {
						// Tdo nothing
					}
			});
	}

	@Override
	public void deviceEjected() {
		if (UiActivator.getDefault().isTetheredShootingActive()) {
			try {
				new TetheredShootingCommand().execute(null);
			} catch (ExecutionException e) {
				// ignore
			}
		}
	}

	private static void configureUndoLevels(final IWorkbenchWindow window,
			final IPreferencesService preferencesService) {
		IWorkbenchOperationSupport operationSupport = window.getWorkbench().getOperationSupport();
		operationSupport.getOperationHistory().setLimit(operationSupport.getUndoContext(),
				preferencesService.getInt(UI_NAMESPACE, PreferenceConstants.UNDOLEVELS, 9, null));
	}

}
