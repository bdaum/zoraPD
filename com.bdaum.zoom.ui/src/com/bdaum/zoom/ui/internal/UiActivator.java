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

package com.bdaum.zoom.ui.internal;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.bdaum.jna.ext.ShellFunctions;
import com.bdaum.zoom.batch.internal.BatchActivator;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.Criterion;
import com.bdaum.zoom.cat.model.group.CriterionImpl;
import com.bdaum.zoom.cat.model.group.GroupImpl;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.exhibition.Exhibit;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitImpl;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitionImpl;
import com.bdaum.zoom.cat.model.group.exhibition.Wall;
import com.bdaum.zoom.cat.model.group.slideShow.Slide;
import com.bdaum.zoom.cat.model.group.slideShow.SlideImpl;
import com.bdaum.zoom.cat.model.group.slideShow.SlideShowImpl;
import com.bdaum.zoom.cat.model.group.webGallery.Storyboard;
import com.bdaum.zoom.cat.model.group.webGallery.WebExhibit;
import com.bdaum.zoom.cat.model.group.webGallery.WebExhibitImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebGalleryImpl;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.common.internal.FileLocator;
import com.bdaum.zoom.core.CatalogAdapter;
import com.bdaum.zoom.core.CatalogListener;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IEmailService;
import com.bdaum.zoom.core.IRelationDetector;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.LifeCycleListener;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.FileNameExtensionFilter;
import com.bdaum.zoom.core.internal.ImportState;
import com.bdaum.zoom.core.internal.QueryOptions;
import com.bdaum.zoom.core.internal.db.AssetEnsemble;
import com.bdaum.zoom.core.internal.lire.ILireService;
import com.bdaum.zoom.core.internal.operations.AutoRule;
import com.bdaum.zoom.core.internal.operations.IDngLocator;
import com.bdaum.zoom.core.internal.operations.ImportConfiguration;
import com.bdaum.zoom.core.internal.peer.AssetOrigin;
import com.bdaum.zoom.core.internal.peer.ConnectionLostException;
import com.bdaum.zoom.core.internal.peer.FileInfo;
import com.bdaum.zoom.core.internal.peer.IPeerService;
import com.bdaum.zoom.css.internal.CssActivator;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.mtp.ObjectFilter;
import com.bdaum.zoom.mtp.StorageObject;
import com.bdaum.zoom.net.core.internal.Base64;
import com.bdaum.zoom.program.DiskFullException;
import com.bdaum.zoom.ui.IDropinHandler;
import com.bdaum.zoom.ui.IFrameManager;
import com.bdaum.zoom.ui.ILocationDisplay;
import com.bdaum.zoom.ui.INavigationHistory;
import com.bdaum.zoom.ui.IUi;
import com.bdaum.zoom.ui.actions.IViewAction;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.dialogs.TimedMessageDialog;
import com.bdaum.zoom.ui.gps.IGpsParser;
import com.bdaum.zoom.ui.gps.IWaypointCollector;
import com.bdaum.zoom.ui.internal.actions.ViewImageAction;
import com.bdaum.zoom.ui.internal.codes.CodeParser;
import com.bdaum.zoom.ui.internal.commands.LastImportCommand;
import com.bdaum.zoom.ui.internal.dialogs.TetheredDialog;
import com.bdaum.zoom.ui.internal.job.FolderWatchJob;
import com.bdaum.zoom.ui.internal.job.SyncPicasaJob;
import com.bdaum.zoom.ui.internal.job.TetheredJob;
import com.bdaum.zoom.ui.internal.preferences.FileAssociationsPreferencePage;
import com.bdaum.zoom.ui.internal.preferences.FileEditorMapping;
import com.bdaum.zoom.ui.internal.views.AssetDropTargetEffect;
import com.bdaum.zoom.ui.internal.views.IDragHost;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;
import com.bdaum.zoom.ui.views.IMediaViewer;

@SuppressWarnings("restriction")
public class UiActivator extends ZUiPlugin implements IUi, IDngLocator {

	public static final String PLUGIN_ID = "com.bdaum.zoom.ui"; //$NON-NLS-1$

	private static final String CONTACTFILE = "contacts/contacts.xml"; //$NON-NLS-1$

	private static final long ONEDAY = 86400000L;

	private static final String DB3_SCANLIST = "db3/scanlist.txt"; //$NON-NLS-1$

	private static final String TMPFILE = "tmp"; //$NON-NLS-1$

	private static final String DB3FILE = "db3"; //$NON-NLS-1$

	private static UiActivator plugin;

	private HashMap<String, IMediaViewer> viewerMap;

	private String inputFolderpath;

	private String filepath;

	private int filterIndex = 0;

	private static final String[] CatFileExtensions = new String[] { "*" //$NON-NLS-1$
			+ Constants.CATALOGEXTENSION };

	private static final String[] CatFileNames = new String[] {
			Constants.APPNAME + Messages.UiActivator_ZoomCatalog + Constants.CATALOGEXTENSION + ")", }; //$NON-NLS-1$

	private static final List<Object> EMPTYOBJECTS = new ArrayList<Object>(0);

	private Map<String, FileEditorMapping> fileEditorMappings;

	private SmartCollectionImpl lastAdhocQuery;

	private Clipboard clipboard;

	private boolean slideShowRunning;

	private boolean showHover = true;

	private Map<IWorkbenchWindow, NavigationHistory> navMap = new HashMap<IWorkbenchWindow, NavigationHistory>(5);

	private QueryField[] hoverNodes;

	private Map<String, Cursor> cursorShapes = new HashMap<String, Cursor>(7);

	private String defaultWatchFilters;

	private boolean closing;

	private String[] updaterCommand;

	private CodeParser[] codeParsers = new CodeParser[3];

	private boolean repairCat;

	private QueryOptions queryOptions;

	private File contactsFile;

	private ImageData lastRefererenceImage;

	private Map<String, IRelationDetector> relationDetectors;

	private ListenerList<LifeCycleListener> lifeCycleListeners = new ListenerList<LifeCycleListener>();

	private AssetDropTargetEffect dropTargetEffect;

	private static ScheduledExecutorService scheduledExecutorService;

	private IDragHost dragHost;

	private ListenerList<StartListener> startListeners = new ListenerList<StartListener>();

	private String previousCatUri;

	private boolean hasStarted;

	private File picasaDb3;

	private File picasaTmp;

	private File picasaScanList;

	private Clip clip;

	private boolean started;

	private ILocationDisplay locationDisplay;

	private boolean locationDisplayIntialized;

	private List<String> galleryperspectiveIds = new ArrayList<>(4);

	private IFrameManager frameManager;

	private Map<String, String> perspectiveGalleries;

	private Map<Rectangle, IKiosk> viewerMonitorMap = new HashMap<>(5);

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
	 * BundleContext )
	 */

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		try {
			for (File file : Platform.getLogFileLocation().toFile().getParentFile().listFiles()) {
				String path = file.getPath();
				if (path.indexOf(".log") >= 0 && path.indexOf(".bak") >= 0) //$NON-NLS-1$ //$NON-NLS-2$
					file.delete();
			}
		} catch (RuntimeException e) {
			// exit gracefully
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
	 * BundleContext )
	 */

	@Override
	public void stop(BundleContext context) throws Exception {
		for (Cursor cursor : cursorShapes.values())
			cursor.dispose();
		plugin = null;
		super.stop(context);
	}

	public static UiActivator getDefault() {
		return plugin;
	}

	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public String getInputFolderLocation() {
		return inputFolderpath;
	}

	public void setInputFolderLocation(String path) {
		inputFolderpath = path;
	}

	public String getFileLocation() {
		return filepath;
	}

	public void setFileLocation(String path) {
		filepath = path;
	}

	public void logInfo(String message) {
		getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message));
	}

	public void logWarning(String message, Throwable e) {
		getLog().log(new Status(IStatus.WARNING, PLUGIN_ID, message, e));
	}

	public void logError(String message, Throwable e) {
		getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message, e));
	}

	public int getFilterIndex() {
		return filterIndex;
	}

	public void setFilterIndex(int filterIndex) {
		this.filterIndex = filterIndex;
	}

	public String[] getCatFileExtensions() {
		return CatFileExtensions;
	}

	public String[] getSupportedCatFileNames() {
		return CatFileNames;
	}

	public FileEditorMapping getFileEditorMapping(String extension) {
		if (fileEditorMappings == null) {
			fileEditorMappings = new HashMap<String, FileEditorMapping>();
			for (FileEditorMapping mapping : FileAssociationsPreferencePage.loadFileEditorMappings())
				for (String ext : mapping.getExtensions())
					fileEditorMappings.put(ext.toLowerCase(), mapping);
			addPreferenceChangeListener(new IPreferenceChangeListener() {
				public void preferenceChange(PreferenceChangeEvent event) {
					if (PreferenceConstants.FILEASSOCIATION.equals(event.getKey()))
						fileEditorMappings = null;
				}
			});

		}
		return fileEditorMappings.get(extension.toLowerCase());
	}

	public static Criterion decodeAutoColoringCriterion(String s) {
		if (s != null) {
			String field = null;
			int relation = 0;
			int p = s.indexOf('\t');
			if (p >= 0) {
				field = s.substring(0, p);
				int off = p + 1;
				p = s.indexOf('\t', off);
				if (p >= 0) {
					relation = Integer.parseInt(s.substring(off, p));
					try (ObjectInputStream in = new ObjectInputStream(
							new ByteArrayInputStream(Base64.decode(s.substring(p + 1))))) {
						return new CriterionImpl(field, null, in.readObject(), relation, true);
					} catch (ClassNotFoundException e) {
						// do nothing
					} catch (IOException e) {
						// should never happen
					}
				}
			}
		}
		return null;
	}

	public ImportConfiguration createImportConfiguration(IAdaptable adaptable) {
		return createImportConfiguration(adaptable, false, true, true, true, true, true, true, true, false);
	}

	public ImportConfiguration createImportConfiguration(IAdaptable adaptable, boolean isSynchronize,
			boolean isResetImage, boolean isResetStatus, boolean isResetExif, boolean isResetIptc, boolean isResetGps,
			boolean isResetFaceData, boolean processSidecars, boolean inBackground) {
		IPreferencesService preferencesService = Platform.getPreferencesService();
		String rawimport = preferencesService.getString(PLUGIN_ID, PreferenceConstants.RAWIMPORT,
				Constants.RAWIMPORT_BOTH, null);
		boolean dnguncompressed = preferencesService.getBoolean(PLUGIN_ID, PreferenceConstants.DNGUNCOMPRESSED, false,
				null);
		boolean dnglinear = preferencesService.getBoolean(PLUGIN_ID, PreferenceConstants.DNGLINEAR, false, null);
		String deriverelations = preferencesService.getString(PLUGIN_ID, PreferenceConstants.DERIVERELATIONS,
				Constants.DERIVE_ALL, null);
		String dngfolder = preferencesService.getString(PLUGIN_ID, PreferenceConstants.DNGFOLDER, "dng", null); //$NON-NLS-1$
		boolean alarmonprompt = preferencesService.getBoolean(PLUGIN_ID, PreferenceConstants.ALARMONPROMPT, true, null);
		boolean alarmonfinish = preferencesService.getBoolean(PLUGIN_ID, PreferenceConstants.ALARMONFINISH, true, null);
		boolean autoDerive = preferencesService.getBoolean(PLUGIN_ID, PreferenceConstants.AUTODERIVE, true, null);
		boolean applyXmp = preferencesService.getBoolean(PLUGIN_ID, PreferenceConstants.APPLYXMPTODERIVATES, true,
				null);
		boolean makerNotes = preferencesService.getBoolean(PLUGIN_ID, PreferenceConstants.IMPORTMAKERNOTES, true, null);
		boolean faceData = preferencesService.getBoolean(PLUGIN_ID, PreferenceConstants.IMPORTFACEDATA, true, null);
		boolean archiveRecipes = preferencesService.getBoolean(PLUGIN_ID, PreferenceConstants.ARCHIVERECIPES, false,
				null);
		boolean showImported = preferencesService.getBoolean(PLUGIN_ID, PreferenceConstants.SHOWIMPORTED, false, null);

		Meta meta = CoreActivator.getDefault().getDbManager().getMeta(true);
		return new ImportConfiguration(adaptable, meta.getTimeline(), meta.getLocationFolders(), isSynchronize,
				ImportState.ASK, isResetImage, isResetStatus, isResetExif, isResetIptc, isResetGps, isResetFaceData,
				processSidecars, rawimport, this, dnguncompressed, dnglinear, deriverelations, autoDerive, applyXmp,
				dngfolder.trim(), alarmonprompt, alarmonfinish, inBackground, makerNotes, faceData, archiveRecipes,
				meta.getWebpCompression(), meta.getJpegQuality(), showImported, getEnabledRelationDetectors(),
				obtainAutoRules());
	}

	public List<AutoRule> obtainAutoRules() {
		String raw = Platform.getPreferencesService().getString(PLUGIN_ID, PreferenceConstants.AUTORULES, "", null); //$NON-NLS-1$
		GroupImpl group = Core.getCore().getDbManager().obtainById(GroupImpl.class, Constants.GROUP_ID_AUTO);
		if (group != null) {
			String annotations = group.getAnnotations();
			if (annotations != null && annotations.startsWith("A")) //$NON-NLS-1$
				raw = annotations.substring(1);
		}
		return AutoRule.constructRules(raw);
	}

	public Collection<IRelationDetector> getRelationDetectors() {
		if (relationDetectors == null) {
			relationDetectors = new HashMap<String, IRelationDetector>(5);
			for (IExtension ext : Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID, "relationDetector") //$NON-NLS-1$
					.getExtensions())
				for (IConfigurationElement config : ext.getConfigurationElements())
					try {
						IRelationDetector detector = (IRelationDetector) config.createExecutableExtension("class"); //$NON-NLS-1$
						String id = config.getAttribute("id"); //$NON-NLS-1$
						detector.setId(id);
						detector.setName(config.getAttribute("name")); //$NON-NLS-1$
						detector.setDescription(config.getAttribute("description")); //$NON-NLS-1$
						relationDetectors.put(id, detector);
					} catch (CoreException e) {
						logError(NLS.bind(Messages.UiActivator_cannot_create_relation_detector,
								config.getAttribute("name")), e); //$NON-NLS-1$
					}
		}
		return relationDetectors.values();
	}

	public IRelationDetector[] getEnabledRelationDetectors() {
		getRelationDetectors();
		List<IRelationDetector> enabledDetectors = new ArrayList<IRelationDetector>(3);
		for (StringTokenizer st = new StringTokenizer(
				getPreferenceStore().getString(PreferenceConstants.RELATIONDETECTORS)); st.hasMoreTokens();) {
			IRelationDetector detector = relationDetectors.get(st.nextToken());
			if (detector != null)
				enabledDetectors.add(detector);
		}
		return enabledDetectors.toArray(new IRelationDetector[enabledDetectors.size()]);
	}

	public IDropinHandler getDropinHandler(String ext) {
		for (IExtension extension : Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID, "dropinHandler") //$NON-NLS-1$
				.getExtensions())
			for (IConfigurationElement conf : extension.getConfigurationElements())
				if (ext.equals(conf.getAttribute("type"))) //$NON-NLS-1$
					try {
						return (IDropinHandler) conf.createExecutableExtension("class"); //$NON-NLS-1$
					} catch (CoreException e) {
						logError(Messages.UiActivator_InternalErrorDropinHandler, e);
					}
		return null;
	}

	public ILocationDisplay getLocationDisplay() {
		if (locationDisplay == null && !locationDisplayIntialized) {
			for (IExtension ext : Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID, "locationDisplay") //$NON-NLS-1$
					.getExtensions())
				for (IConfigurationElement config : ext.getConfigurationElements())
					try {
						ILocationDisplay display = (ILocationDisplay) config.createExecutableExtension("class"); //$NON-NLS-1$
						if (display != null) {
							locationDisplay = display;
							locationDisplayIntialized = true;
						}
					} catch (CoreException e) {
						// do nothing
					}
		}
		return locationDisplay;
	}

	public Cursor getCursor(Display display, String key) {
		if (key == null)
			return display.getSystemCursor(SWT.CURSOR_ARROW);
		Cursor cursor = cursorShapes.get(key);
		if (cursor == null) {
			ImageData cursorData = UiActivator.getImageDescriptor("icons/cursors/" + key + ".BMP").getImageData(100); //$NON-NLS-1$//$NON-NLS-2$
			cursorData.transparentPixel = 1;
			cursorShapes.put(key,
					cursor = new Cursor(display, cursorData, cursorData.width / 2, cursorData.height / 2));
		}
		return cursor;
	}

	public SmartCollectionImpl getLastAdhocQuery() {
		return lastAdhocQuery;
	}

	public void setLastAdhocQuery(SmartCollectionImpl collection) {
		lastAdhocQuery = collection;
	}

	public Clipboard getClipboard(Display display) {
		if (clipboard == null) {
			clipboard = new Clipboard(display);
			display.disposeExec(() -> {
				if (clipboard != null)
					clipboard.dispose();
			});
		}
		return clipboard;
	}

	public void setSlideShowRunning(boolean running) {
		slideShowRunning = running;
	}

	public boolean isSlideShowRunning() {
		return slideShowRunning;
	}

	public boolean getShowHover() {
		return showHover;
	}

	public void setShowHover(boolean showHover) {
		this.showHover = showHover;
	}

	public QueryField[] getHoverNodes() {
		if (hoverNodes == null) {
			List<QueryField> list = new ArrayList<QueryField>(100);
			StringTokenizer st = new StringTokenizer(
					Platform.getPreferencesService().getString(PLUGIN_ID, PreferenceConstants.HOVERMETADATA, "", null), //$NON-NLS-1$
					"\n"); //$NON-NLS-1$
			while (st.hasMoreTokens()) {
				QueryField qfield = QueryField.findQueryField(st.nextToken());
				if (qfield != null && qfield.hasLabel() && qfield.getType() != QueryField.T_NONE)
					list.add(qfield);
			}
			hoverNodes = list.toArray(new QueryField[list.size()]);
			Arrays.sort(hoverNodes, new Comparator<QueryField>() {
				public int compare(QueryField q1, QueryField q2) {
					return q1.getLabel().compareTo(q2.getLabel());
				}
			});
			addPreferenceChangeListener(new IPreferenceChangeListener() {
				public void preferenceChange(PreferenceChangeEvent event) {
					if (PreferenceConstants.HOVERMETADATA.equals(event.getKey()))
						hoverNodes = null;
				}
			});
		}
		return hoverNodes;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.internal.Ui#getNavigationHistory(org.eclipse.ui.
	 * IWorkbenchWindow)
	 */
	public INavigationHistory getNavigationHistory(IWorkbenchWindow window) {
		if (window == null)
			window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			IWorkbenchWindow[] workbenchWindows = PlatformUI.getWorkbench().getWorkbenchWindows();
			if (workbenchWindows.length > 0)
				window = workbenchWindows[0];
		}
		if (window == null)
			return null;
		NavigationHistory history = navMap.get(window);
		if (history == null)
			navMap.put(window, history = new NavigationHistory(window));
		return history;
	}

	public void resetFileEditorMappings() {
		fileEditorMappings = null;
	}

	public boolean preCatClose(final int mode, String title, String msg, boolean hideShell) {
		closing = true;
		Job.getJobManager().cancel(Constants.FOLDERWATCH);
		Job.getJobManager().cancel(Constants.SYNCPICASA);
		Job.getJobManager().cancel(Constants.UPDATING);
		Core.waitOnJobCanceled(Constants.FOLDERWATCH, Constants.SYNCPICASA, Constants.UPDATING);
		if (Core.getCore().getDbManager().getFile() == null)
			return true;
		IWorkbench workbench = PlatformUI.getWorkbench();
		Display display = workbench.getDisplay();
		if (mode != CatalogListener.EMERGENCY) {
			IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
			if (criticalJobsRunning()) {
				UiUtilities.showView("org.eclipse.ui.views.ProgressView"); //$NON-NLS-1$
				IInputValidator validator = new IInputValidator() {
					public String isValid(String newText) {
						return criticalJobsRunning() ? "Jobs running" : null; //$NON-NLS-1$
					}
				};
				final TimedMessageDialog dialog = new TimedMessageDialog(null, validator, title, null,
						msg + Messages.UiActivator_please_wait, MessageDialog.WARNING, new String[] {
								Messages.UiActivator_return_to_workbench, Messages.UiActivator_cancel_all_operations },
						0, 1, 200L);
				if (!display.isDisposed())
					display.syncExec(() -> dialog.open());
				int returnCode = dialog.getReturnCode();
				if (hideShell && returnCode == 0 && activeWorkbenchWindow != null)
					activeWorkbenchWindow.getShell().setVisible(true);
				return returnCode == 1;
			}
			if (hideShell && activeWorkbenchWindow != null)
				activeWorkbenchWindow.getShell().setVisible(false);
		}
		performClosingTasks(mode);
		return true;
	}

	public void addLifeCycleListener(LifeCycleListener listener) {
		lifeCycleListeners.add(listener);
	}

	public void performClosingTasks(final int mode) {
		Runnable runnable = new Runnable() {
			public void run() {
				for (LifeCycleListener listener : lifeCycleListeners)
					listener.sessionClosed(mode);
				IJobManager jobManager = Job.getJobManager();
				if (mode != CatalogListener.TASKBAR) {
					jobManager.cancel(Constants.SLIDESHOW);
					jobManager.cancel(Constants.OPERATIONJOBFAMILY);
					jobManager.cancel(Constants.DAEMONS);
					Core.waitOnJobCanceled(Constants.OPERATIONJOBFAMILY);
					jobManager.cancel(Constants.INDEXING);
				}
				CoreActivator activator = CoreActivator.getDefault();
				if (mode == CatalogListener.EMERGENCY) {
					jobManager.cancel(Constants.UPDATING);
					jobManager.cancel(Constants.FILETRANSFER);
					Core.waitOnJobCanceled(Constants.INDEXING);
				} else {
					final IDbManager dbManager = activator.getDbManager();
					if (dbManager.getFile() != null) {
						Meta meta = dbManager.getMeta(false);
						if (meta != null) {
							meta.setLastSessionEnd(new Date());
							dbManager.safeTransaction(null, meta);
						}
						if (mode != CatalogListener.TASKBAR) {
							ILireService lireService = Core.getCore().getDbFactory().getLireService(false);
							if (lireService != null) {
								Set<String> postponed = lireService.postponeIndexing();
								Core.waitOnJobCanceled(Constants.FILETRANSFER);
								if (!postponed.isEmpty() && meta != null) {
									meta.setPostponed(postponed);
									dbManager.safeTransaction(null, meta);
								}
							}
							if (repairCat) {
								repairCat = false;
								dbManager.repairCatalog();
							}
						}
						if (!BatchActivator.isFastExit())
							dbManager.pruneEmptySystemCollections();
						if (mode != CatalogListener.TASKBAR) {
							activator.saveFolderStates();
							if (mode != CatalogListener.TUNE)
								activator.deleteTrashCan();
						} else if (getPreferenceStore().getBoolean(PreferenceConstants.FORCEDELETETRASH))
							activator.deleteTrashCan();
						if (!BatchActivator.isFastExit() && dbManager.isEmbedded()) {
							if (mode == CatalogListener.TUNE)
								dbManager.checkDbSanity(true);
							else if (meta != null) {
								if (meta.getBackupLocation() == null || meta.getBackupLocation().isEmpty()) {
									if (mode != CatalogListener.TASKBAR)
										dbManager.checkDbSanity(false);
								} else {
									long interval = activator.getBackupInterval() * ONEDAY;
									Date lastSessionEnd = meta.getLastSessionEnd();
									long lastBackup = meta.getLastBackup().getTime();
									long lastSession = lastSessionEnd == null ? Long.MAX_VALUE
											: lastSessionEnd.getTime();
									if (interval < 0 || System.currentTimeMillis() - lastBackup > interval
											&& lastSession > lastBackup) {
										if (mode != CatalogListener.TASKBAR)
											dbManager.checkDbSanity(false);
										dbManager.performBackup(0L, interval, mode != CatalogListener.TASKBAR,
												activator.getBackupGenerations());
									}
								}
							}
						}
					}
				}
				if (mode != CatalogListener.TASKBAR) {
					Core.waitOnJobCanceled(Constants.UPDATING);
					activator.fireCatalogClosed(mode);
				}
			}

		};
		if (mode != CatalogListener.NORMAL && mode != CatalogListener.TUNE)
			runnable.run();
		else
			BusyIndicator.showWhile(PlatformUI.getWorkbench().getDisplay(), runnable);
	}

	private static boolean criticalJobsRunning() {
		for (Job job : Job.getJobManager().find(Constants.CRITICAL))
			return (job instanceof OperationJob) ? !((OperationJob) job).isSilent() : true;
		return false;
	}

	public List<String> getGpsFileExtensions() {
		List<String> extList = new ArrayList<String>(5);
		for (IExtension extension : Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID, "gpsParser") //$NON-NLS-1$
				.getExtensions())
			for (IConfigurationElement conf : extension.getConfigurationElements()) {
				StringTokenizer st = new StringTokenizer(conf.getAttribute("extensions")); //$NON-NLS-1$
				while (st.hasMoreTokens())
					extList.add(st.nextToken());
			}
		return extList;
	}

	public List<String> getGpsFileTypes() {
		List<String> extList = new ArrayList<String>(5);
		for (IExtension extension : Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID, "gpsParser") //$NON-NLS-1$
				.getExtensions())
			for (IConfigurationElement conf : extension.getConfigurationElements())
				for (IConfigurationElement sub : conf.getChildren("type")) //$NON-NLS-1$
					extList.add(sub.getAttribute("name")); //$NON-NLS-1$
		return extList;
	}

	public ObjectFilter createGpsFileFormatFilter() {
		List<String> extList = new ArrayList<String>(5);
		for (IExtension extension : Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID, "gpsParser") //$NON-NLS-1$
				.getExtensions())
			for (IConfigurationElement conf : extension.getConfigurationElements()) {
				StringTokenizer st = new StringTokenizer(conf.getAttribute("extensions"), " ;"); //$NON-NLS-1$ //$NON-NLS-2$
				while (st.hasMoreTokens())
					extList.add(st.nextToken());
			}
		return new FileNameExtensionFilter(extList.toArray(new String[extList.size()]));
	}

	public IGpsParser getGpsParser(File file) {
		return (IGpsParser) getGpsExtension(file, "class", Messages.UiActivator_cannot_instantiate_parser, true); //$NON-NLS-1$
	}

	private Object getGpsExtension(File file, String att, String msg, boolean required) {
		String filename = file.getName();
		int p = filename.lastIndexOf('.');
		String ext = (p >= 0) ? filename.substring(p + 1) : ""; //$NON-NLS-1$
		for (IExtension extension : Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID, "gpsParser") //$NON-NLS-1$
				.getExtensions())
			for (IConfigurationElement conf : extension.getConfigurationElements()) {
				StringTokenizer st = new StringTokenizer(conf.getAttribute("extensions")); //$NON-NLS-1$
				while (st.hasMoreTokens())
					if (ext.equalsIgnoreCase(st.nextToken())) {
						if (!required && conf.getAttribute(att) == null)
							return null;
						try {
							return conf.createExecutableExtension(att);
						} catch (CoreException e) {
							logError(NLS.bind(msg, conf.getAttribute("name")), e); //$NON-NLS-1$
							return null;
						}
					}
			}
		return null;
	}

	public IWaypointCollector getWaypointCollector(File file) {
		return (IWaypointCollector) getGpsExtension(file, "waypoints", //$NON-NLS-1$
				Messages.UiActivator_cannot_instantiate_waypoint_collector, false);
	}

	public void playVoicenote(final Asset asset) {
		if (asset != null) {
			if (asset.getFileState() == IVolumeManager.PEER) {
				final IPeerService peerService = Core.getCore().getPeerService();
				if (peerService != null) {
					final AssetOrigin assetOrigin = peerService.getAssetOrigin(asset.getStringId());
					if (assetOrigin != null) {
						BusyIndicator.showWhile(null, () -> playRemoteVoiceNote(asset, peerService, assetOrigin));
						return;
					}
				}
			}
			URI voiceURI = Core.getCore().getVolumeManager().findVoiceFile(asset);
			try {
				playSoundfile(voiceURI.toURL(), null);
			} catch (MalformedURLException e) {
				// should not happen
			}
		}
	}

	private void playRemoteVoiceNote(Asset asset, IPeerService peerService, AssetOrigin assetOrigin) {
		try {
			if (peerService.checkCredentials(IPeerService.VOICE, asset.getSafety(), assetOrigin))
				try {
					URI voiceOrigURI = null;
					String voiceFileURI = AssetEnsemble.extractVoiceNote(asset);
					if (".".equals(voiceFileURI)) { //$NON-NLS-1$
						FileInfo fileInfo = peerService.getFileInfo(assetOrigin, asset.getUri(), asset.getVolume());
						voiceOrigURI = Core.getVoicefileURI(fileInfo.getFile());
					}
					if (voiceOrigURI != null || voiceFileURI != null) {
						File tempFile = Core.createTempFile("peerfile", Core.getFileExtension(voiceFileURI)); //$NON-NLS-1$
						tempFile.deleteOnExit();
						FileInfo voiceInfo = peerService.getFileInfo(assetOrigin,
								voiceOrigURI != null ? voiceOrigURI.toString() : voiceFileURI, asset.getVolume());
						if (peerService.transferRemoteFile(new NullProgressMonitor(), assetOrigin, voiceInfo, tempFile))
							playSoundfile(tempFile.toURI().toURL(), null);
					}
				} catch (IOException e) {
					logError(Messages.UiActivator_io_error_playing_remote, e);
				} catch (DiskFullException e) {
					logError(Messages.UiActivator_disk_full_playing_remote, e);
				}
			else
				AcousticMessageDialog.openWarning(null, Messages.UiActivator_access_restriction,
						Messages.UiActivator_unsufficient_right_voice_note);
		} catch (ConnectionLostException e) {
			AcousticMessageDialog.openError(null, Messages.UiActivator_connection_lost, e.getLocalizedMessage());
		}
	}

	public void stopAudio() {
		if (clip != null && clip.isRunning())
			clip.stop();
	}

	public IMediaViewer[] getImageViewers() {
		Map<String, IMediaViewer> imageViewerMap = getImageViewerMap();
		Set<IMediaViewer> viewers = new HashSet<IMediaViewer>(imageViewerMap.values());
		IMediaViewer[] a = viewers.toArray(new IMediaViewer[viewers.size()]);
		Arrays.sort(a, new Comparator<IMediaViewer>() {
			public int compare(IMediaViewer o1, IMediaViewer o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		return a;
	}

	public IMediaViewer getMediaViewer(Asset asset) {
		return getImageViewerMap().get(asset.getFormat());
	}

	private Map<String, IMediaViewer> getImageViewerMap() {
		if (viewerMap == null) {
			viewerMap = new HashMap<String, IMediaViewer>();
			for (IExtension extension : Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID, "imageViewer") //$NON-NLS-1$
					.getExtensions())
				for (IConfigurationElement conf : extension.getConfigurationElements()) {
					String name = conf.getAttribute("name"); //$NON-NLS-1$
					try {
						IMediaViewer viewer = (IMediaViewer) conf.createExecutableExtension("class"); //$NON-NLS-1$
						viewer.setName(name);
						viewer.setId(conf.getAttribute("id")); //$NON-NLS-1$
						StringTokenizer st = new StringTokenizer(conf.getAttribute("formats")); //$NON-NLS-1$
						while (st.hasMoreTokens())
							viewerMap.put(st.nextToken(), viewer);
					} catch (CoreException e) {
						getDefault().logError(NLS.bind(Messages.UiActivator_cannot_instantiate_image_viewer, name), e);
					}
				}
		}
		return viewerMap;
	}

	public void sendMail(final List<String> to) {
		BusyIndicator.showWhile(null, () -> {
			BundleContext context = getBundle().getBundleContext();
			ServiceReference<?> ref = context.getServiceReference(IEmailService.class.getName());
			if (ref != null) {
				IStatus status = ((IEmailService) context.getService(ref)).sendMail(to, null, null, null, null, null);
				context.ungetService(ref);
				if (!status.isOK())
					getLog().log(status);
			}
		});
	}

	public void playSound(String sound, String prefKey) {
		if (prefKey != null && !getPreferenceStore().getBoolean(prefKey))
			return;
		try {
			for (String ext : ImageConstants.VOICEEXT) {
				URL url = FileLocator.findFileURL(getBundle(), "/sound/" + sound + ext, true); //$NON-NLS-1$
				if (url != null) {
					playSoundfile(url, null);
					break;
				}
			}
		} catch (IOException e) {
			// do nothing
		}
	}

	public long getSoundfileLengthInMicroseconds(URL clipURL) {
		try (AudioInputStream ais = AudioSystem.getAudioInputStream(clipURL)) {
			Clip clip = (Clip) AudioSystem.getLine(new DataLine.Info(Clip.class, ais.getFormat()));
			return clip.getMicrosecondLength();
		} catch (Exception e) {
			return 0L;
		}
	}

	public void playSoundfile(URL clipURL, final Runnable onStop) {
		stopAudio();
		try {
			final AudioInputStream ais = AudioSystem.getAudioInputStream(clipURL);
			clip = (Clip) AudioSystem.getLine(new DataLine.Info(Clip.class, ais.getFormat()));
			try {
				if (clip.isOpen())
					clip.close();
				clip.open(ais);
			} catch (IllegalStateException e) {
				// line already open
			}
			clip.addLineListener(new LineListener() {
				public void update(LineEvent event) {
					if (event.getType() == LineEvent.Type.STOP) {
						if (clip.isOpen())
							clip.close();
						clip = null;
						try {
							ais.close();
							if (onStop != null)
								onStop.run();
						} catch (IOException e) {
							// ignore
						}
					}
				}
			});
			clip.start();
		} catch (IOException e) {
			// do nothing
		} catch (LineUnavailableException e) {
			// do nothing
		} catch (UnsupportedAudioFileException e) {
			// should never happen
		} catch (NullPointerException e) {
			// do nothing
		}
	}

	public int getDisplayCMS() {
		return getPreferenceStore().getInt(PreferenceConstants.COLORPROFILE);
	}

	public void addPreferenceChangeListener(IPreferenceChangeListener preferenceListener) {
		InstanceScope.INSTANCE.getNode(UiActivator.PLUGIN_ID).addPreferenceChangeListener(preferenceListener);
	}

	public void removePreferenceChangeListener(IPreferenceChangeListener preferenceListener) {
		InstanceScope.INSTANCE.getNode(UiActivator.PLUGIN_ID).removePreferenceChangeListener(preferenceListener);
	}

	public String getDefaultWatchFilters() {
		if (defaultWatchFilters == null)
			defaultWatchFilters = getPreferenceStore().getString(PreferenceConstants.WATCHFILTER);
		return defaultWatchFilters;
	}

	public CodeParser getCodeParser(int type) {
		if (codeParsers[type] == null)
			codeParsers[type] = new CodeParser(type);
		return codeParsers[type];
	}

	public void setMesssage(IStatusLineManager statusLineManager, String text, boolean error) {
		if (error)
			statusLineManager.setErrorMessage(text);
		else
			statusLineManager.setMessage(text);
		if (text != null)
			CssActivator.getDefault().updateStatusLine(statusLineManager);
	}

	/**
	 * @return the closing
	 */
	public boolean isClosing() {
		return closing;
	}

	public void setUpdaterCommand(String[] updaterCommand) {
		this.updaterCommand = updaterCommand;
	}

	/**
	 * @return the updaterCommand
	 */
	public String[] getUpdaterCommand() {
		return updaterCommand;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.IUi#getPresentationItems()
	 */
	public List<Object> getPresentationItems() {
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWorkbenchWindow != null) {
			IDbManager dbManager = Core.getCore().getDbManager();
			IStructuredSelection sel = getNavigationHistory(activeWorkbenchWindow).getOtherSelection();
			if (sel != null) {
				Object first = sel.getFirstElement();
				if (first instanceof SlideShowImpl) {
					List<String> entries = ((SlideShowImpl) first).getEntry();
					List<Object> list = new ArrayList<>(entries.size());
					for (String slideId : entries) {
						SlideImpl slide = dbManager.obtainById(SlideImpl.class, slideId);
						if (slide != null)
							list.add(slide);
					}
					return list;
				} else if (first instanceof ExhibitionImpl) {
					List<Object> list = new ArrayList<>();
					for (Wall wall : ((ExhibitionImpl) first).getWall())
						for (String exhibitId : wall.getExhibit()) {
							ExhibitImpl exhibit = dbManager.obtainById(ExhibitImpl.class, exhibitId);
							if (exhibit != null)
								list.add(exhibit);
						}
					return list;
				} else if (first instanceof WebGalleryImpl) {
					List<Object> list = new ArrayList<>();
					for (Storyboard storyboard : ((WebGalleryImpl) first).getStoryboard())
						for (String exhibitId : storyboard.getExhibit()) {
							WebExhibitImpl exhibit = dbManager.obtainById(WebExhibitImpl.class, exhibitId);
							if (exhibit != null)
								list.add(exhibit);
						}
					return list;
				}
			}
		}
		return EMPTYOBJECTS;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.IUi#getAssetsFromPresentation()
	 */
	public List<Asset> getAssetsFromPresentation() {
		return getAssetsFromPresentationItems(getPresentationItems(), false);
	}

	/**
	 * @param presentationItems
	 * @param pruneNonAssets
	 * @return
	 */
	public List<Asset> getAssetsFromPresentationItems(Collection<Object> presentationItems, boolean pruneNonAssets) {
		IDbManager dbManager = Core.getCore().getDbManager();
		List<Asset> assets = new ArrayList<Asset>(presentationItems.size());
		Iterator<Object> it = presentationItems.iterator();
		while (it.hasNext()) {
			Object item = it.next();
			if (item instanceof Slide) {
				String assetId = ((Slide) item).getAsset();
				if (assetId != null) {
					AssetImpl asset = dbManager.obtainAsset(assetId);
					if (asset != null) {
						assets.add(asset);
						continue;
					}
				}
			} else if (item instanceof Exhibit) {
				String assetId = ((Exhibit) item).getAsset();
				if (assetId != null) {
					AssetImpl asset = dbManager.obtainAsset(assetId);
					if (asset != null) {
						assets.add(asset);
						continue;
					}
				}
			} else if (item instanceof WebExhibit) {
				String assetId = ((WebExhibit) item).getAsset();
				if (assetId != null) {
					AssetImpl asset = dbManager.obtainAsset(assetId);
					if (asset != null) {
						assets.add(asset);
						continue;
					}
				}
			}
			if (pruneNonAssets)
				it.remove();
		}
		return assets;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.IUi#getPresentationName()
	 */
	public String getPresentationName() {
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWorkbenchWindow != null) {
			IStructuredSelection sel = getNavigationHistory(activeWorkbenchWindow).getOtherSelection();
			if (sel != null) {
				Object first = sel.getFirstElement();
				if (first instanceof SlideShowImpl)
					return ((SlideShowImpl) first).getName();
				if (first instanceof ExhibitionImpl)
					return ((ExhibitionImpl) first).getName();
				if (first instanceof WebGalleryImpl)
					return ((WebGalleryImpl) first).getName();
			}
		}
		return null;
	}

	public Set<QueryField> getExportFilter() {
		String ess = UiActivator.getDefault().getPreferenceStore().getString(PreferenceConstants.EXPORTMETADATA);
		if (ess != null) {
			Set<QueryField> filter = new HashSet<QueryField>();
			StringTokenizer st = new StringTokenizer(ess, "\n"); //$NON-NLS-1$
			while (st.hasMoreTokens()) {
				QueryField qfield = QueryField.findQueryField(st.nextToken());
				if (qfield != null)
					filter.add(qfield);
			}
			return filter;
		}
		return null;
	}

	public void setRepairCat(boolean repairCat) {
		this.repairCat = repairCat;
	}

	public String getDngLocation() {
		return Platform.getPreferencesService().getString(PLUGIN_ID, PreferenceConstants.DNGCONVERTERPATH, "", null); //$NON-NLS-1$
	}

	public QueryOptions getQueryOptions() {
		if (queryOptions == null)
			queryOptions = new QueryOptions();
		return queryOptions;
	}

	public void setQueryOptions(QueryOptions queryOptions) {
		this.queryOptions = queryOptions;
	}

	public File findPicasaContactsFile() {
		if (contactsFile == null) {
			File localAppPath = ShellFunctions.getPicasaAppData();
			if (localAppPath != null) {
				contactsFile = new File(localAppPath, CONTACTFILE);
				picasaDb3 = new File(localAppPath, DB3FILE);
				picasaTmp = new File(localAppPath, TMPFILE);
				picasaScanList = new File(localAppPath, DB3_SCANLIST);
			}
		}
		return contactsFile;
	}

	public long getLastPicasaUpdate() {
		long lastUpdate = 0L;
		if (picasaDb3 != null)
			lastUpdate = Math.max(lastUpdate, picasaDb3.lastModified());
		if (picasaTmp != null)
			lastUpdate = Math.max(lastUpdate, picasaTmp.lastModified());
		if (picasaScanList != null)
			lastUpdate = Math.max(lastUpdate, picasaScanList.lastModified());
		return lastUpdate;
	}

	public void restart() {
		final Display display = PlatformUI.getWorkbench().getDisplay();
		if (!display.isDisposed()) {
			display.timerExec(300, () -> {
				if (!display.isDisposed()) {
					AcousticMessageDialog dialog = new AcousticMessageDialog(null,
							NLS.bind(Messages.UiActivator_restart_required, Constants.APPLICATION_NAME), null,
							Messages.UiActivator_restart_msg, MessageDialog.QUESTION,
							new String[] { Messages.UiActivator_restart_now, Messages.UiActivator_restart_later }, 0);
					if (dialog.open() == 0 && preCatClose(CatalogListener.SHUTDOWN, Messages.UiActivator_restart,
							Messages.UiActivator_restart_question, false))
						PlatformUI.getWorkbench().restart();
				}
			});
		}
	}

	public static ScheduledExecutorService getScheduledExecutorService() {
		if (scheduledExecutorService == null)
			scheduledExecutorService = Executors.newScheduledThreadPool(3);
		return scheduledExecutorService;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.IUi#isVisibleAndNotMinimized()
	 */
	public boolean isWorkbenchActive() {
		return true;
	}

	public ImageData getLastReferenceImage() {
		return lastRefererenceImage;
	}

	public void setLastRefererenceImage(ImageData lastRefererenceImage) {
		this.lastRefererenceImage = lastRefererenceImage;
	}

	/* Drag and Drop */

	public final void dragStart(IDragHost dragHost) {
		this.dragHost = dragHost;
		disposeDropTargetEffect();
		dragHost.setDragging(true);
	}

	private void disposeDropTargetEffect() {
		if (dropTargetEffect != null) {
			dropTargetEffect.dispose();
			dropTargetEffect = null;
		}
	}

	public void setDropTargetEffect(DropTargetEvent event, Control control) {
		if (dragHost != null && event.detail != DND.DROP_NONE) {
			dropTargetEffect = new AssetDropTargetEffect(dragHost, control);
			((DropTarget) event.widget).setDropTargetEffect(dropTargetEffect);
		}
	}

	public void hideDropTargetEffect() {
		if (dropTargetEffect != null)
			dropTargetEffect.setMode(DND.DROP_NONE);
	}

	public final void dragFinished() {
		if (dragHost != null) {
			dragHost.setDragging(false);
			dragHost = null;
		}
		disposeDropTargetEffect();
	}

	public IDragHost getDragHost() {
		return dragHost;
	}

	public void fireStartListeners() {
		hasStarted = true;
		for (StartListener listener : startListeners)
			listener.hasStarted();
		startListeners.clear();
	}

	public boolean addStartListener(StartListener listener) {
		if (!hasStarted) {
			startListeners.add(listener);
			return true;
		}
		return false;
	}

	public String getPreviousCatUri() {
		return previousCatUri;
	}

	public void setPreviousCatUri(String previousCatUri) {
		this.previousCatUri = previousCatUri;
	}

	public void postCatOpen() {
		closing = false;
	}

	public void postCatInit(boolean startup) {
		boolean traymode = startup && getPreferenceStore().getBoolean(PreferenceConstants.TRAY_MODE);
		Meta meta = Core.getCore().getDbManager().getMeta(true);
		if (!meta.getPauseFolderWatch() && meta.getWatchedFolder() != null && !meta.getWatchedFolder().isEmpty())
			new FolderWatchJob(null).schedule(traymode ? 120000L : 6100L);
		Job.getJobManager().cancel(Constants.SYNCPICASA);
		new SyncPicasaJob(this).schedule(traymode ? 180000L : 30000L);
	}

	public List<IViewAction> getExtraViewActions(String id) {
		List<IViewAction> actions = new ArrayList<>(5);
		for (IExtension extension : Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID, "viewAction") //$NON-NLS-1$
				.getExtensions())
			for (IConfigurationElement conf : extension.getConfigurationElements()) {
				String appliesTo = conf.getAttribute("appliesTo"); //$NON-NLS-1$
				if (appliesTo == null || appliesTo.isEmpty() || findId(id, appliesTo)) {
					String label = conf.getAttribute("label"); //$NON-NLS-1$
					try {
						IViewAction action = (IViewAction) conf.createExecutableExtension("class"); //$NON-NLS-1$
						action.setText(label);
						action.setId(conf.getAttribute("id")); //$NON-NLS-1$
						String tooltip = conf.getAttribute("tooltip"); //$NON-NLS-1$
						if (tooltip != null && !tooltip.isEmpty())
							action.setToolTipText(tooltip);
						String definitionId = conf.getAttribute("definitionId"); //$NON-NLS-1$
						if (definitionId != null && !definitionId.isEmpty())
							action.setActionDefinitionId(definitionId);
						actions.add(action);
					} catch (InvalidRegistryObjectException e) {
						// should never happen
					} catch (CoreException e) {
						logError(NLS.bind(Messages.UiActivator_error_instantiating_action, label), e);
					}
				}
			}
		return actions;
	}

	private static boolean findId(String id, String idlist) {
		int p = 0;
		int l2 = idlist.length();
		int l1 = id.length();
		while (p + l1 <= l2) {
			boolean found = true;
			for (int i = l1 - 1; i >= 0; i--)
				if (id.charAt(i) != idlist.charAt(p + i)) {
					found = false;
					break;
				}
			if (found)
				return true;
			int q = idlist.indexOf(' ', p);
			if (q < 0)
				return false;
			p = q + 1;
			while (p < l2 && idlist.charAt(p) == ' ')
				++p;
		}
		return false;
	}

	public void setStarted() {
		started = true;
	}

	public boolean hasStarted() {
		return started;
	}

	public boolean isPresentationPerspective(String id) {
		for (String perspectivId : galleryperspectiveIds)
			if (perspectivId.equals(id))
				return true;
		return false;
	}

	public String getPerspectiveGallery(String perspId) {
		if (perspectiveGalleries == null) {
			perspectiveGalleries = new HashMap<>();
			IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID,
					"perspectiveGallery"); //$NON-NLS-1$
			for (IExtension ext : extensionPoint.getExtensions())
				for (IConfigurationElement config : ext.getConfigurationElements()) {
					String pId = config.getAttribute("perspectiveId"); //$NON-NLS-1$
					perspectiveGalleries.put(pId, config.getAttribute("galleryId")); //$NON-NLS-1$
					if (Boolean.parseBoolean(config.getAttribute("isGalleryPerspective"))) //$NON-NLS-1$
						galleryperspectiveIds.add(pId);
				}
		}
		return perspectiveGalleries.get(perspId);
	}

	/* Tethered Shooting */

	private CatalogListener shootingListener = new CatalogAdapter() {
		public void assetsModified(com.bdaum.zoom.core.BagChange<Asset> changes, QueryField node) {
			if (changes != null && !changes.getAdded().isEmpty()) {
				LastImportCommand command = new LastImportCommand();
				command.init((IWorkbenchWindow) getAdapter(IWorkbenchWindow.class));
				command.run();
				int tetheredShow = getPreferenceStore().getInt(PreferenceConstants.TETHEREDSHOW);
				if (tetheredShow != PreferenceConstants.TETHEREDSHOW_NO)
					for (Asset asset : changes.getAdded()) {
						ViewImageAction action = new ViewImageAction("", null, null, UiActivator.this); //$NON-NLS-1$
						Event event = new Event();
						if (tetheredShow == PreferenceConstants.TETHEREDSHOW_INTERN)
							event.stateMask = SWT.SHIFT;
						event.data = asset;
						Shell shell = (Shell) getAdapter(Shell.class);
						shell.getDisplay().asyncExec(() -> {
							if (!shell.isDisposed())
								action.runWithEvent(event);
						});
					}
			}
		}
	};

	public boolean startTetheredShooting(StorageObject[] dcims, IJobChangeListener listener) {
		if (!isTetheredShootingActive()) {
			TetheredDialog dialog = new TetheredDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell());
			if (dialog.open() == TetheredDialog.OK) {
				Job.getJobManager().cancel(Constants.TETHEREDJOB);
				CoreActivator act = CoreActivator.getDefault();
				act.setTetheredShooting(true);
				act.addCatalogListener(shootingListener);
				TetheredJob tetheredJob = new TetheredJob(dcims, dialog.getFolder());
				tetheredJob.addJobChangeListener(listener);
				tetheredJob.schedule();
			}
		}
		return isTetheredShootingActive();
	}

	public void endTetheredShooting() {
		if (isTetheredShootingActive()) {
			Job.getJobManager().cancel(Constants.TETHEREDJOB);
			CoreActivator act = CoreActivator.getDefault();
			act.setTetheredShooting(false);
			act.removeCatalogListener(shootingListener);
		}
	}

	public boolean isTetheredShootingActive() {
		return Core.getCore().isTetheredShootingActive();
	}

	/** Kiosks **/

	public void registerKiosk(IKiosk viewer, Rectangle mbounds) {
		if (viewer == null)
			viewerMonitorMap.remove(mbounds);
		else {
			IKiosk v = viewerMonitorMap.put(mbounds, viewer);
			if (v != null)
				v.close();
		}
	}

	public Date occupiedSince(Rectangle mbounds) {
		IKiosk v = viewerMonitorMap.get(mbounds);
		return v == null ? null : v.getCreationDate();
	}

	public Rectangle getSecondaryMonitorBounds(Shell parentShell) {
		Monitor primaryMonitor = parentShell.getDisplay().getPrimaryMonitor();
		Rectangle mbounds = primaryMonitor.getBounds();
		Monitor[] monitors = parentShell.getDisplay().getMonitors();
		String mode = Platform.getPreferencesService().getString(UiActivator.PLUGIN_ID,
				PreferenceConstants.SECONDARYMONITOR, "false", null); //$NON-NLS-1$
		boolean alternate = PreferenceConstants.MON_ALTERNATE.equals(mode);
		if (monitors.length > 1 && (alternate || Boolean.parseBoolean(mode))) {
			Rectangle r = parentShell.getBounds();
			if (mbounds.contains(r.x + r.width / 2, r.y + r.height / 2)) {
				int max = 0;
				long minTime = Long.MAX_VALUE;
				Rectangle oldest = null;
				Rectangle bestFree = null;
				for (Monitor monitor : monitors)
					if (!monitor.equals(primaryMonitor)) {
						r = monitor.getBounds();
						int d = r.width * r.width + r.height * r.height;
						Date creationDate = occupiedSince(r);
						if (creationDate == null || !alternate) {
							if (d > max) {
								max = d;
								bestFree = r;
							}
						} else if (creationDate.getTime() < minTime) {
							minTime = creationDate.getTime();
							oldest = r;
						}
					}
				if (bestFree != null)
					return bestFree;
				if (alternate) {
					Date creationDate = occupiedSince(mbounds);
					if (oldest != null && creationDate != null && creationDate.getTime() >= minTime)
						return oldest;
				}
			}
		}
		return mbounds;
	}

	@Override
	public IFrameManager getFrameManager() {
		if (frameManager == null)
			frameManager = new FrameManager();
		return frameManager;
	}

}
