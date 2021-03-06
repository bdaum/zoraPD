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
 * (c) 2009-2017 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.dialogs;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.bdaum.aoModeling.runtime.IIdentifiableObject;
import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.batch.internal.BatchActivator;
import com.bdaum.zoom.cat.model.Meta_type;
import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObjectImpl;
import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObjectShownImpl;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.creatorsContact.ContactImpl;
import com.bdaum.zoom.cat.model.creatorsContact.CreatorsContactImpl;
import com.bdaum.zoom.cat.model.group.Criterion;
import com.bdaum.zoom.cat.model.group.CriterionImpl;
import com.bdaum.zoom.cat.model.group.GroupImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.SortCriterionImpl;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitionImpl;
import com.bdaum.zoom.cat.model.group.slideShow.SlideShowImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebGalleryImpl;
import com.bdaum.zoom.cat.model.location.LocationImpl;
import com.bdaum.zoom.cat.model.locationCreated.LocationCreatedImpl;
import com.bdaum.zoom.cat.model.locationShown.LocationShownImpl;
import com.bdaum.zoom.cat.model.meta.Category;
import com.bdaum.zoom.cat.model.meta.CategoryImpl;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.cat.model.meta.MetaImpl;
import com.bdaum.zoom.cat.model.meta.WatchedFolder;
import com.bdaum.zoom.cat.model.meta.WatchedFolderImpl;
import com.bdaum.zoom.common.GeoMessages;
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.CatalogListener;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.core.ISpellCheckingService;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.LocationConstants;
import com.bdaum.zoom.core.internal.Theme;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.core.internal.lire.Algorithm;
import com.bdaum.zoom.core.internal.lucene.ILuceneService;
import com.bdaum.zoom.css.CSSProperties;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.fileMonitor.internal.filefilter.WildCardFilter;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.internal.CreateLocationFolderOperation;
import com.bdaum.zoom.operations.internal.CreateTimelineOperation;
import com.bdaum.zoom.operations.internal.ManageKeywordsOperation;
import com.bdaum.zoom.operations.internal.ModifyKeywordOperation;
import com.bdaum.zoom.operations.internal.ReplaceKeywordOperation;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.ui.ILocationDisplay;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.dialogs.WatchedFolderLabelProvider;
import com.bdaum.zoom.ui.dialogs.ZInputDialog;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.SortColumnManager;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiConstants;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.VocabManager;
import com.bdaum.zoom.ui.internal.ZViewerComparator;
import com.bdaum.zoom.ui.internal.commands.LastImportCommand;
import com.bdaum.zoom.ui.internal.operations.ModifyMetaOperation;
import com.bdaum.zoom.ui.internal.preferences.GeneralPreferencePage;
import com.bdaum.zoom.ui.internal.preferences.KeyPreferencePage;
import com.bdaum.zoom.ui.internal.views.ZColumnViewerToolTipSupport;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.CheckedText;
import com.bdaum.zoom.ui.internal.widgets.CompressionGroup;
import com.bdaum.zoom.ui.internal.widgets.ExpandCollapseGroup;
import com.bdaum.zoom.ui.internal.widgets.FilterField;
import com.bdaum.zoom.ui.internal.widgets.FlatGroup;
import com.bdaum.zoom.ui.internal.widgets.IInputAdvisor;
import com.bdaum.zoom.ui.internal.widgets.RadioButtonGroup;
import com.bdaum.zoom.ui.internal.widgets.TextWithVariableGroup;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.internal.wizards.WatchedFolderWizard;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.bdaum.zoom.ui.widgets.CLink;
import com.bdaum.zoom.ui.widgets.NumericControl;

@SuppressWarnings("restriction")
public class EditMetaDialog extends ZTitleAreaDialog implements Listener, ICheckStateListener, IDoubleClickListener,
		IInputValidator, IInputAdvisor, ISelectionChangedListener {

	public class DetailSelectionAdapter implements Listener {

		private final Class<? extends IIdentifiableObject> clazz;
		private final String label;
		private LinkedHashMap<String, Integer> values = new LinkedHashMap<>();

		public DetailSelectionAdapter(Class<? extends IIdentifiableObject> clazz, String label) {
			this.label = label;
			new DetailsJob(this.clazz = clazz, values).schedule();
		}

		public String getLabel() {
			return label;
		}

		@Override
		public void handleEvent(Event e) {
			detailsProgressBar.setData(clazz);
			detailsGroup.setText(label);
			detailsGroup.setVisible(true);
			try {
				Job.getJobManager().join(clazz, null);
			} catch (OperationCanceledException | InterruptedException e1) {
				// should not happen
			}
			Shell shell = getShell();
			if (!shell.isDisposed()) {
				shell.getDisplay().asyncExec(() -> {
					if (!shell.isDisposed()) {
						int i = 0;
						for (Map.Entry<String, Integer> entry : values.entrySet())
							setDetails(i++, entry.getKey(), entry.getValue());
						detailsProgressBar.setVisible(false);
					}
				});
			}
		}
	}

	public class DetailsJob extends Job {

		private final Class<? extends IIdentifiableObject> clazz;
		private LinkedHashMap<String, Integer> values;

		public DetailsJob(Class<? extends IIdentifiableObject> clazz, LinkedHashMap<String, Integer> values) {
			super(Messages.EditMetaDialog_collecting_details);
			this.clazz = clazz;
			this.values = values;
			setSystem(true);
			setPriority(Job.INTERACTIVE);
		}

		@Override
		public boolean belongsTo(Object family) {
			return family == clazz || family == DETAILS;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if (clazz == GroupImpl.class) {
				List<GroupImpl> groups = dbManager.obtainObjects(GroupImpl.class);
				final Shell shell = getShell();
				int work = groups.size();
				int incr = Math.max(1, (work + 15) / 16);
				int main = 0, subgroup = 0, system = 0, user = 0;
				for (int i = 0; i < work;) {
					GroupImpl group = groups.get(i);
					if (group.getGroup_subgroup_parent() == null)
						++main;
					else
						++subgroup;
					if (group.getSystem())
						++system;
					else
						++user;
					values.put(Messages.EditMetaDialog_main_groups, main);
					values.put(Messages.EditMetaDialog_subgroups, subgroup);
					values.put(Messages.EditMetaDialog_system_groups, system);
					values.put(Messages.EditMetaDialog_user_groups, user);
					if (updateProgressBar(shell, ++i, work, incr, monitor))
						return Status.CANCEL_STATUS;
				}
			} else if (clazz == SmartCollectionImpl.class) {
				int main = 0, subCollection = 0, system = 0, user = 0, persons = 0, albums = 0;
				int timeline = 0, locations = 0, imports = 0, directory = 0, offline = 0, local = 0, auto = 0;
				List<SmartCollectionImpl> collections = dbManager.obtainObjects(SmartCollectionImpl.class);
				final int work = collections.size();
				int incr = Math.max(1, (work + 31) / 32);
				final Shell shell = getShell();
				IVolumeManager volumeManager = Core.getCore().getVolumeManager();
				String uriKey = QueryField.URI.getKey() + '=';
				String volumeKey = QueryField.VOLUME.getKey() + '=';
				String dateCreatedKey = QueryField.IPTC_DATECREATED.getKey();
				String locationCreatedKey = QueryField.IPTC_LOCATIONCREATED.getKey();
				String importDateKey = QueryField.IMPORTDATE.getKey();
				for (int i = 0; i < work;) {
					SmartCollectionImpl sm = collections.get(i);
					if (sm.getSmartCollection_subSelection_parent() == null)
						++main;
					else
						++subCollection;
					boolean sys = sm.getSystem();
					if (sys)
						++system;
					else
						++user;
					if (sm.getAlbum()) {
						if (sys)
							++persons;
						else
							++albums;
					} else {
						String id = sm.getStringId();
						if (id.startsWith(uriKey)) {
							++directory;
							if (volumeManager.findExistingFile(id.substring(uriKey.length()), null) == null)
								++offline;
							else
								++local;
						} else if (id.startsWith(volumeKey)) {
							++directory;
							if (volumeManager.isOffline(id.substring(volumeKey.length())))
								++offline;
							else
								++local;
						} else {
							if (id.startsWith(Constants.GROUP_ID_AUTOSUB))
								++auto;
							List<Criterion> crits = sm.getCriterion();
							if (!crits.isEmpty()) {
								String field = crits.get(0).getField();
								if (sys) {
									if (field.equals(dateCreatedKey))
										++timeline;
									else if (field.equals(locationCreatedKey))
										++locations;
								} else if (field.equals(importDateKey))
									++imports;
							}
						}
					}
					values.put(Messages.EditMetaDialog_main_collections, main);
					values.put(Messages.EditMetaDialog_subcollections, subCollection);
					values.put(Messages.EditMetaDialog_import_folders, imports);
					values.put(Messages.EditMetaDialog_system_collections, system);
					values.put(Messages.EditMetaDialog_directories, directory);
					values.put(Messages.EditMetaDialog_local_dir, local);
					values.put(Messages.EditMetaDialog_offline_dir, offline);
					values.put(Messages.EditMetaDialog_locations_folders, locations);
					values.put(Messages.EditMetaDialog_timeline_folders, timeline);
					values.put(Messages.EditMetaDialog_person_folders, persons);
					values.put(Messages.EditMetaDialog_user_collections, user);
					values.put(Messages.EditMetaDialog_albums, albums);
					values.put(Messages.EditMetaDialog_created_by_rule, auto);
					values.put(Messages.EditMetaDialog_other_collections, user - albums - auto);
					if (updateProgressBar(shell, ++i, work, incr, monitor))
						return Status.CANCEL_STATUS;
				}
			} else if (clazz == LocationImpl.class) {
				Set<String> regions = new HashSet<String>(10);
				Set<String> countries = new HashSet<String>(51);
				Set<String> cities = new HashSet<String>(777);
				List<LocationImpl> locations = dbManager.obtainObjects(LocationImpl.class);
				final int work = locations.size();
				int incr = Math.max(1, (work + 31) / 32);
				final Shell shell = getShell();
				for (int i = 0; i < work;) {
					LocationImpl loc = locations.get(i);
					String worldRegionCode = loc.getWorldRegionCode();
					String worldRegion = loc.getWorldRegion();
					if (worldRegionCode == null && worldRegion != null)
						worldRegionCode = LocationConstants.worldRegionToContinent.get(worldRegion);
					regions.add(worldRegionCode);
					String countryISOCode = loc.getCountryISOCode();
					if (countryISOCode != null && countryISOCode.length() < 3)
						countryISOCode = LocationConstants.iso2Toiso3.get(countryISOCode);
					if (countryISOCode == null)
						countryISOCode = loc.getCountryName();
					countries.add(countryISOCode);
					cities.add(loc.getCity());
					values.put(Messages.EditMetaDialog_world_regions, regions.size());
					values.put(Messages.EditMetaDialog_countries, countries.size());
					values.put(Messages.EditMetaDialog_cities, cities.size());
					if (updateProgressBar(shell, ++i, work, incr, monitor))
						return Status.CANCEL_STATUS;
				}
			}
			return Status.OK_STATUS;
		}

		private boolean updateProgressBar(final Shell shell, final int i, int work, int incr,
				IProgressMonitor monitor) {
			if (!monitor.isCanceled() && !shell.isDisposed()) {
				if (i % incr == 0)
					shell.getDisplay().syncExec(() -> {
						if (!detailsProgressBar.isDisposed() && detailsProgressBar.getData() == clazz) {
							int j = 0;
							for (Map.Entry<String, Integer> entry : values.entrySet())
								setDetails(j++, entry.getKey(), entry.getValue());
							detailsProgressBar.setMaximum(work);
							detailsProgressBar.setSelection(i);
							detailsProgressBar.setVisible(true);
						}
					});
				return false;
			}
			return true;
		}
	}

	public class KeywordContentProvider implements ITreeContentProvider {

		private Map<Character, List<String>> chapters;

		public void dispose() {
			chapters = null;
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			chapters = null;
		}

		public Object[] getElements(Object inputElement) {
			@SuppressWarnings("unchecked")
			String[] available = filterKeywords((Set<String>) inputElement);
			if (flatKeywordGroup.isFlat())
				return available;
			if (chapters == null) {
				chapters = new HashMap<Character, List<String>>();
				for (String kw : available)
					if (!kw.isEmpty()) {
						Character chapterTitle = Character.toUpperCase(kw.charAt(0));
						List<String> elements = chapters.get(chapterTitle);
						if (elements == null)
							chapters.put(chapterTitle, elements = new ArrayList<String>());
						elements.add(kw);
					}
			}
			return chapters.keySet().toArray();
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof Character) {
				List<String> elements = chapters.get(parentElement);
				if (elements != null)
					return elements.toArray();
			}
			return EMPTY;
		}

		public Object getParent(Object element) {
			if (!flatKeywordGroup.isFlat()) {
				String kw = (String) element;
				if (!kw.isEmpty()) {
					char firstChar = Character.toUpperCase(kw.charAt(0));
					for (Character title : chapters.keySet())
						if (title.charValue() == firstChar)
							return title;
				}
			}
			return null;
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}

	}

	public class StructGroup implements Listener, IDoubleClickListener, ISelectionChangedListener {

		public class LocationTreeJob extends Job {

			public LocationTreeJob() {
				super(Messages.EditMetaDialog_building_location_tree);
				setSystem(true);
				setPriority(Job.INTERACTIVE);
			}

			@Override
			public boolean belongsTo(Object family) {
				return family == StructGroup.this;
			}

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				final Control control = locTreeViewer.getControl();
				if (!control.isDisposed())
					control.getDisplay().asyncExec(() -> {
						if (!control.isDisposed())
							control.setCursor(control.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
					});
				locations = dbManager.obtainObjects(LocationImpl.class);
				root = new ArrayList<LocationNode>();
				for (LocationImpl location : locations) {
					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;
					buildLocationTree(root, null, location, LocationNode.CONTINENT, true);
				}
				colorLocationTree(root);
				String expansions = settings.get(TREEEXPANSION + type);
				List<String> expList = expansions == null ? null : Core.fromStringList(expansions, ";"); //$NON-NLS-1$
				final List<LocationNode> nodeList = expList == null ? null : new ArrayList<>(expList.size());
				if (expList != null)
					for (String s : expList) {
						for (LocationNode rootNode : root) {
							LocationNode node = rootNode.find(s);
							if (node != null)
								nodeList.add(node);
						}
					}
				if (!control.isDisposed())
					control.getDisplay().asyncExec(() -> {
						if (!control.isDisposed()) {
							locTreeViewer.setInput(root);
							if (nodeList != null)
								locTreeViewer.setExpandedElements(nodeList.toArray());
							else
								locTreeViewer.expandAll();
							control.setCursor(control.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
						}
					});
				return Status.OK_STATUS;
			}
		}

		private TreeViewer locTreeViewer;
		private Composite stackComposite;
		private StackLayout stackLayout;
		private Composite flatComposite;
		private Composite treeComposite;
		private List<LocationNode> root;
		private StructComponent flatComponent;
		private Button addButton;
		private Button editButton;
		private Button removeButton;
		private Button showButton;
		private Button mapButton;
		private Button emailButton;
		private Button webButton;
		private final int type;
		private List<LocationImpl> locations = new ArrayList<LocationImpl>(0);
		private final String item;
		private FlatGroup radioGroup;
		private boolean enabled;
		private Button cleanButton;
		private Set<String> usedObjects;

		public StructGroup(Composite sComposite, final int type, String item, boolean enabled) {
			this.type = type;
			this.item = item;
			this.enabled = enabled;
			usedObjects = new HashSet<>(537);
			switch (type) {
			case QueryField.T_LOCATION:
				for (LocationCreatedImpl rel : dbManager.obtainObjects(LocationCreatedImpl.class))
					usedObjects.add(rel.getLocation());
				for (LocationShownImpl rel : dbManager.obtainObjects(LocationShownImpl.class))
					usedObjects.add(rel.getLocation());
				break;
			case QueryField.T_CONTACT:
				for (CreatorsContactImpl rel : dbManager.obtainObjects(CreatorsContactImpl.class))
					usedObjects.add(rel.getContact());
				break;
			case QueryField.T_OBJECT:
				for (ArtworkOrObjectShownImpl rel : dbManager.obtainObjects(ArtworkOrObjectShownImpl.class))
					usedObjects.add(rel.getArtworkOrObject());
				break;
			}
			sComposite.addListener(SWT.Dispose, this);
			radioGroup = new FlatGroup(sComposite, SWT.NONE, settings, HIERARCHICAL_STRUCT + type);
			radioGroup.setLayoutData(new GridData(SWT.END, SWT.BEGINNING, false, false));
			radioGroup.addListener(SWT.Selection, this);
			if (type == QueryField.T_LOCATION) {
				Composite locComposite = new Composite(sComposite, SWT.NONE);
				locComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
				locComposite.setLayout(new GridLayout(1, false));
				Composite viewerAndButtonComposite = new Composite(locComposite, SWT.NONE);
				viewerAndButtonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				viewerAndButtonComposite.setLayout(new GridLayout(2, false));

				stackComposite = new Composite(viewerAndButtonComposite, SWT.NONE);
				stackComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				stackLayout = new StackLayout();
				stackComposite.setLayout(stackLayout);
				flatComposite = new Composite(stackComposite, SWT.NONE);
				flatComposite.setLayout(new GridLayout(2, false));
				flatComponent = new StructComponent(dbManager, flatComposite, null, type, false, structOverlayMap,
						radioGroup, usedObjects, 1, settings);
				treeComposite = new Composite(stackComposite, SWT.NONE);
				treeComposite.setLayout(new GridLayout(2, false));
				createLocationTree(treeComposite);
				createButtonBar(viewerAndButtonComposite);
				new LocationTreeJob().schedule();
			} else {
				flatComponent = new StructComponent(dbManager, sComposite, null, type, false, structOverlayMap,
						radioGroup, usedObjects, 1, settings);
				createButtonBar(sComposite);
			}
			flatComponent.addDoubleClickListener(this);
			flatComponent.addSelectionChangedListener(this);
			updateLocationStack();
		}

		public void fillValues() {
			flatComponent.fillValues();
		}

		@SuppressWarnings("unused")
		private void createButtonBar(Composite parent) {
			final Composite bar = new Composite(parent, SWT.NONE);
			bar.setLayout(new GridLayout());
			bar.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
			Label label = new Label(bar, SWT.NONE);
			label.setLayoutData(new GridData(50, 15));
			addButton = createPushButton(bar, Messages.EditMetaDialog_add,
					NLS.bind(Messages.EditMetaDialog_add_x, item));
			addButton.addListener(SWT.Selection, this);
			editButton = createPushButton(bar, Messages.EditMetaDialog_edit,
					NLS.bind(Messages.EditMetaDialog_edit_selected_x, item));
			editButton.addListener(SWT.Selection, this);
			removeButton = createPushButton(bar, Messages.EditMetaDialog_remove,
					NLS.bind(Messages.EditMetaDialog_remove_x, item));
			removeButton.addListener(SWT.Selection, this);
			cleanButton = createPushButton(bar, Messages.EditMetaDialog_clean_up,
					Messages.EditMetaDialog_clean_up_tooltip);
			cleanButton.addListener(SWT.Selection, this);
			new Label(bar, SWT.SEPARATOR | SWT.HORIZONTAL);
			showButton = createPushButton(bar, Messages.EditMetaDialog_show_images,
					NLS.bind(Messages.EditMetaDialog_show_x, item));
			showButton.addListener(SWT.Selection, this);
			mapButton = createPushButton(bar, Messages.EditMetaDialog_show_in_map,
					NLS.bind(Messages.EditMetaDialog_show_x_in_map, item));
			mapButton.addListener(SWT.Selection, this);
			if (type != QueryField.T_LOCATION)
				mapButton.setVisible(false);
			emailButton = createPushButton(bar, Messages.EditMetaDialog_send_email,
					NLS.bind(Messages.EditMetaDialog_Email_x, item));
			emailButton.addListener(SWT.Selection, this);
			webButton = createPushButton(bar, Messages.EditMetaDialog_visit_web_site,
					NLS.bind(Messages.EditMetaDialog_visit_x, item));
			webButton.addListener(SWT.Selection, this);
			if (type != QueryField.T_CONTACT) {
				emailButton.setVisible(false);
				webButton.setVisible(false);
			}
		}

		protected void cleanup(int type) {
			List<IIdentifiableObject> objects = new LinkedList<>(flatComponent.getObjects());
			BusyIndicator.showWhile(getShell().getDisplay(), () -> {
				Iterator<IIdentifiableObject> it = objects.iterator();
				while (it.hasNext())
					if (usedObjects.contains(it.next().getStringId()))
						it.remove();
			});
			if (objects.isEmpty())
				AcousticMessageDialog.openInformation(getShell(), Messages.EditMetaDialog_clean_up,
						Messages.EditMetaDialog_clean_up_msg);
			else {
				CleanupDialog dialog = new CleanupDialog(getShell(), objects);
				if (dialog.open() == OK) {
					dbManager.safeTransaction(objects, null);
					flatComponent.removeAll(objects);
					if (type == QueryField.T_LOCATION) {
						Job.getJobManager().cancel(this);
						try {
							Job.getJobManager().join(this, null);
						} catch (OperationCanceledException | InterruptedException e) {
							// ignore
						}
						new LocationTreeJob().schedule();
					}
				}
			}
		}

		protected SmartCollection createAdhocQuery(Object sel) {
			SmartCollectionImpl coll = new SmartCollectionImpl("", true, false, true, false, null, 0, null, 0, null, //$NON-NLS-1$
					Constants.INHERIT_LABEL, null, 0, 1, null);
			if (sel instanceof LocationNode && ((LocationNode) sel).getLocation() == null) {
				LocationNode node = (LocationNode) sel;
				QueryField subfield;
				switch (node.getLevel()) {
				case LocationNode.CONTINENT:
					subfield = QueryField.LOCATION_WORLDREGIONCODE;
					break;
				case LocationNode.COUNTRY:
					subfield = QueryField.LOCATION_COUNTRYCODE;
					break;
				case LocationNode.STATE:
					subfield = QueryField.LOCATION_STATE;
					break;
				default:
					subfield = QueryField.LOCATION_CITY;
					break;
				}
				String key = node.getKey();
				coll.setName(QueryField.LOCATION_TYPE.getLabel() + '.' + subfield.getLabel() + '=' + key);
				coll.addCriterion(new CriterionImpl(QueryField.IPTC_LOCATIONSHOWN.getKey(), subfield.getKey(), key,
						null, QueryField.EQUALS, false));
				coll.addCriterion(new CriterionImpl(QueryField.IPTC_LOCATIONCREATED.getKey(), subfield.getKey(), key,
						null, QueryField.EQUALS, false));
			} else {
				IdentifiableObject item = getModelElement(sel);
				if (item instanceof LocationImpl) {
					coll.setName(QueryField.LOCATION_TYPE.getLabel() + '='
							+ StructComponent.getStructText(item, structOverlayMap));
					coll.addCriterion(new CriterionImpl(QueryField.IPTC_LOCATIONSHOWN.getKey(), null,
							item.getStringId(), null, QueryField.EQUALS, false));
					coll.addCriterion(new CriterionImpl(QueryField.IPTC_LOCATIONCREATED.getKey(), null,
							item.getStringId(), null, QueryField.EQUALS, false));
				} else if (item instanceof ContactImpl) {
					coll.setName(Messages.EditMetaDialog_contactquery
							+ StructComponent.getStructText(item, structOverlayMap));
					coll.addCriterion(new CriterionImpl(QueryField.IPTC_CONTACT.getKey(), null, item.getStringId(),
							null, QueryField.EQUALS, false));
				} else if (item instanceof ArtworkOrObjectImpl) {
					coll.setName(Messages.EditMetaDialog_artworkquery
							+ StructComponent.getStructText(item, structOverlayMap));
					coll.addCriterion(new CriterionImpl(QueryField.IPTC_ARTWORK.getKey(), null, item.getStringId(),
							null, QueryField.EQUALS, false));
				}
			}
			coll.addSortCriterion(new SortCriterionImpl(QueryField.IPTC_DATECREATED.getKey(), null, true));
			return coll;
		}

		private void removeItem() {
			Object sel = getSelectedElement();
			IdentifiableObject modelElement = getModelElement(sel);
			if (modelElement != null) {
				if (isElementInUse(modelElement)) {
					QueryField qfield = QueryField.getStructParent(type);
					if (qfield != null
							&& !AcousticMessageDialog
									.openConfirm(getShell(),
											NLS.bind(Messages.EditMetaDialog_n_in_use, qfield.getLabel()),
											NLS.bind(
													Messages.EditMetaDialog_the_selected_x_is_used
															+ Messages.EditMetaDialog_want_to_delte,
													qfield.getLabel())))
						return;
				}
				structOverlayMap.put(modelElement.getStringId(), null);
				flatComponent.remove(modelElement);
				if (locTreeViewer != null) {
					buildLocationTree(root, null, (LocationImpl) modelElement, LocationNode.CONTINENT, false);
					colorLocationTree(root);
					setTreeInput(root);
				}
				return;
			}
			if (sel instanceof LocationNode) {
				removeChildren((LocationNode) sel, -1);
				root = new ArrayList<LocationNode>();
				for (LocationImpl location : locations)
					buildLocationTree(root, null, location, LocationNode.CONTINENT, true);
				colorLocationTree(root);
				setTreeInput(root);
			}
		}

		private void setTreeInput(List<LocationNode> root) {
			ISelection selection = locTreeViewer.getSelection();
			Object[] expandedElements = locTreeViewer.getExpandedElements();
			locTreeViewer.setInput(root);
			locTreeViewer.setExpandedElements(expandedElements);
			locTreeViewer.setSelection(selection);
		}

		private int removeChildren(LocationNode node, int policy) {
			List<LocationNode> children = node.getChildren();
			if (children != null) {
				for (LocationNode child : children) {
					LocationImpl modelElement = (LocationImpl) getModelElement(child);
					if (modelElement != null) {
						if (isElementInUse(modelElement)) {
							QueryField qfield = QueryField.getStructParent(type);
							if (qfield == null)
								return policy;
							if (policy != 2) {
								AcousticMessageDialog dialog = new AcousticMessageDialog(getShell(),
										NLS.bind(Messages.EditMetaDialog_n_in_use, qfield.getLabel()), null,
										NLS.bind(Messages.EditMetaDialog_the_selected_x_is_used
												+ Messages.EditMetaDialog_want_to_delte, qfield.getLabel()),
										AcousticMessageDialog.QUESTION,
										new String[] { Messages.EditMetaDialog_no, Messages.EditMetaDialog_yes,
												Messages.EditMetaDialog_all, IDialogConstants.CANCEL_LABEL },
										0);
								int ret = dialog.open();
								switch (ret) {
								case 2:
									policy = 2;
									//$FALL-THROUGH$
								case 1:
									break;
								default:
									return ret;
								}
							}
						}
						structOverlayMap.put(modelElement.getStringId(), null);
						flatComponent.remove(modelElement);
					} else {
						int ret = removeChildren(child, policy);
						if (ret == 3)
							return ret;
					}
				}
			}
			return policy;
		}

		private void createLocationTree(Composite parent) {
			ExpandCollapseGroup expandCollapseGroup = new ExpandCollapseGroup(parent, SWT.NONE);
			expandCollapseGroup.setLayoutData(new GridData(SWT.END, SWT.BEGINNING, true, false, 2, 1));
			locTreeViewer = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.SINGLE);
			expandCollapseGroup.setViewer(locTreeViewer);
			GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
			layoutData.heightHint = 200;
			locTreeViewer.getControl().setLayoutData(layoutData);
			locTreeViewer.setContentProvider(new LocTreeContentProvider());
			locTreeViewer.setComparator(ZViewerComparator.INSTANCE);
			locTreeViewer.setLabelProvider(new ZColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					return element.toString();
				}

				@Override
				protected Color getForeground(Object element) {
					if (element instanceof LocationNode && ((LocationNode) element).isUnused())
						return locTreeViewer.getControl().getDisplay().getSystemColor(SWT.COLOR_RED);
					return super.getForeground(element);
				}
			});
			UiUtilities.installDoubleClickExpansion(locTreeViewer);
			addKeyListener(locTreeViewer.getControl());
			locTreeViewer.addSelectionChangedListener(this);
		}

		private Object getSelectedElement() {
			if (stackLayout != null && stackLayout.topControl == treeComposite)
				return ((IStructuredSelection) locTreeViewer.getSelection()).getFirstElement();
			return flatComponent.getSelectedElement();
		}

		private void editStruct(final int aType) {
			if (!readonly) {
				boolean massUpdate = false;
				Object selectedElement = getSelectedElement();
				IdentifiableObject modelElement = getModelElement(selectedElement);
				if (modelElement == null && selectedElement instanceof LocationNode
						&& !((LocationNode) selectedElement).isUnknown()) {
					modelElement = getFirstModelElement((LocationNode) selectedElement);
					massUpdate = true;
				}
				if (modelElement != null) {
					int level = (selectedElement instanceof LocationNode) ? ((LocationNode) selectedElement).getLevel()
							: -1;
					QueryField qField = QueryField.getStructParent(aType);
					if (qField != null) {
						EditStructDialog dialog = new EditStructDialog(getShell(), modelElement, aType, level,
								structOverlayMap, NLS.bind(Messages.EditMetaDialog_edit_x, qField.getLabel()));
						if (dialog.open() == Window.OK && dialog.isUpdated()) {
							IdentifiableObject result = dialog.getResult();
							if (massUpdate) {
								updateChildren((LocationNode) selectedElement, level, result.getStringId());
								root = new ArrayList<LocationNode>();
								for (LocationImpl location : locations)
									buildLocationTree(root, null, location, LocationNode.CONTINENT, true);
								colorLocationTree(root);
								setTreeInput(root);
							} else
								updateElement(selectedElement);
							if (type == QueryField.T_LOCATION)
								geographic = null;
						}
					}
				}
			}
		}

		private void updateChildren(LocationNode node, int level, String protoTypeId) {
			List<LocationNode> children = node.getChildren();
			if (children != null) {
				Map<QueryField, Object> prototypeMap = structOverlayMap.get(protoTypeId);
				for (LocationNode child : children) {
					LocationImpl modelElement = (LocationImpl) getModelElement(child);
					if (modelElement != null) {
						String id = modelElement.getStringId();
						Map<QueryField, Object> fieldMap = structOverlayMap.get(id);
						if (fieldMap == null)
							structOverlayMap.put(id, fieldMap = new HashMap<QueryField, Object>());
						switch (level) {
						case LocationNode.CITY:
							fieldMap.put(QueryField.LOCATION_CITY, prototypeMap.get(QueryField.LOCATION_CITY));
							//$FALL-THROUGH$
						case LocationNode.STATE:
							fieldMap.put(QueryField.LOCATION_STATE, prototypeMap.get(QueryField.LOCATION_STATE));
							//$FALL-THROUGH$
						case LocationNode.COUNTRY:
							fieldMap.put(QueryField.LOCATION_COUNTRYCODE,
									prototypeMap.get(QueryField.LOCATION_COUNTRYCODE));
							fieldMap.put(QueryField.LOCATION_COUNTRYNAME,
									prototypeMap.get(QueryField.LOCATION_COUNTRYNAME));
							fieldMap.put(QueryField.LOCATION_WORLDREGION,
									prototypeMap.get(QueryField.LOCATION_WORLDREGION));
							fieldMap.put(QueryField.LOCATION_WORLDREGIONCODE,
									prototypeMap.get(QueryField.LOCATION_WORLDREGIONCODE));
							break;
						}
					} else
						updateChildren(child, level, protoTypeId);
				}
			}
		}

		private IdentifiableObject getFirstModelElement(LocationNode node) {
			List<LocationNode> children = node.getChildren();
			if (children != null)
				for (LocationNode child : children) {
					IdentifiableObject modelElement = getModelElement(child);
					if (modelElement != null)
						return modelElement;
					modelElement = getFirstModelElement(child);
					if (modelElement != null)
						return modelElement;
				}
			return null;
		}

		private IdentifiableObject getModelElement(Object selectedElement) {
			if (selectedElement instanceof LocationNode)
				return ((LocationNode) selectedElement).getLocation();
			if (selectedElement instanceof IdentifiableObject)
				return (IdentifiableObject) selectedElement;
			return null;
		}

		private void updateElement(Object selectedElement) {
			flatComponent.update(selectedElement, null);
			flatComponent.setSelection(new StructuredSelection(selectedElement));
			if (locTreeViewer != null) {
				locTreeViewer.update(selectedElement, null);
				locTreeViewer.setSelection(new StructuredSelection(selectedElement));
			}
		}

		protected void updateLocationStack() {
			if (stackLayout != null) {
				stackLayout.topControl = radioGroup.isFlat() ? flatComposite : treeComposite;
				stackComposite.layout();
			}
			updateStructButtons();
		}

		private void updateStructButtons() {
			Object sel = getSelectedElement();
			boolean canShow = sel instanceof LocationNode || sel instanceof IdentifiableObject;
			boolean selected = false;
			boolean editable = false;
			boolean removable = false;
			IdentifiableObject model = null;
			if (sel != null && !(sel instanceof Character)) {
				model = getModelElement(sel);
				selected = (model != null);
				boolean isKnownLocation = sel instanceof LocationNode && !((LocationNode) sel).isUnknown();
				editable = selected || isKnownLocation && ((LocationNode) sel).getLevel() >= LocationNode.COUNTRY;
				removable = selected || isKnownLocation
						&& (((LocationNode) sel).getLevel() >= LocationNode.COUNTRY || ((LocationNode) sel).unused);
			}
			editButton.setEnabled(editable && enabled);
			showButton.setEnabled(canShow);
			removeButton.setEnabled(removable && enabled);
			addButton.setEnabled(enabled);
			boolean mapEnabled = editable;
			mapButton.setEnabled(mapEnabled);
			mapButton.setVisible(UiActivator.getDefault().getLocationDisplay() != null);
			boolean emailEnabled = false;
			boolean webEnabled = false;
			if (model != null && type == QueryField.T_CONTACT) {
				String[] email = ((ContactImpl) model).getEmail();
				emailEnabled = email != null && email.length > 0;
				String[] web = ((ContactImpl) model).getWebUrl();
				webEnabled = web != null && web.length == 1;
			}
			webButton.setEnabled(webEnabled);
			emailButton.setEnabled(emailEnabled);
		}

		private void buildLocationTree(List<LocationNode> base, LocationNode parent, LocationImpl location, int level,
				boolean add) {
			if (base == null)
				return;
			String locId = location.getStringId();
			if (structOverlayMap.containsKey(locId) && structOverlayMap.get(locId) == null)
				return;
			if (level < LocationNode.DETAIL) {
				boolean unknown = false;
				String key;
				String name;
				switch (level) {
				case LocationNode.CONTINENT:
					key = getUpdatedValue(location, QueryField.LOCATION_WORLDREGIONCODE, location.getWorldRegionCode());
					name = getUpdatedValue(location, QueryField.LOCATION_WORLDREGION, location.getWorldRegion());
					if (key == null && name != null)
						key = LocationConstants.worldRegionToContinent.get(name);
					if (key != null)
						name = GeoMessages.getString(GeoMessages.PREFIX + key);
					if (name == null || name.isEmpty()) {
						name = Messages.EditMetaDialog_unknown_worldregion;
						unknown = true;
					}
					break;
				case LocationNode.COUNTRY:
					String iso = getUpdatedValue(location, QueryField.LOCATION_COUNTRYCODE,
							location.getCountryISOCode());
					String cname = getUpdatedValue(location, QueryField.LOCATION_COUNTRYNAME,
							location.getCountryName());
					if (iso != null && iso.length() < 3)
						iso = LocationConstants.iso2Toiso3.get(iso);
					key = iso == null ? cname : iso;
					StringBuilder sb = new StringBuilder();
					if (cname != null)
						sb.append(cname);
					if (iso != null && !iso.isEmpty())
						sb.append(" (").append(iso).append(')').toString(); //$NON-NLS-1$
					if (sb.length() == 0) {
						name = Messages.EditMetaDialog_unknown_country;
						unknown = true;
					} else
						name = sb.toString();
					break;
				case LocationNode.STATE:
					key = getUpdatedValue(location, QueryField.LOCATION_STATE, location.getProvinceOrState());
					name = key;
					if (name == null || name.isEmpty()) {
						name = Messages.EditMetaDialog_unknown_state;
						unknown = true;
					}
					break;
				default:
					key = getUpdatedValue(location, QueryField.LOCATION_CITY, location.getCity());
					name = key;
					if (name == null || name.isEmpty()) {
						name = Messages.EditMetaDialog_unknown_city;
						unknown = true;
					}
					break;
				}
				if (key == null)
					key = ""; //$NON-NLS-1$
				LocationNode current = null;
				int size = base.size();
				for (int i = 0; i < size; i++)
					if (base.get(i).getKey().equals(key)) {
						current = base.get(i);
						break;
					}
				if (current == null && add)
					base.add(current = new LocationNode(parent, key, name, level, null, unknown, false));
				if (current != null)
					buildLocationTree(current.getChildren(), current, location, level + 1, add);
			} else if (add)
				base.add(new LocationNode(parent, "", null, level, location, false, !usedObjects.contains(locId))); //$NON-NLS-1$
			else
				for (Iterator<LocationNode> it = base.iterator(); it.hasNext();)
					if (it.next().getLocation().equals(location)) {
						it.remove();
						break;
					}
		}

		private boolean colorLocationTree(List<LocationNode> base) {
			if (base == null || base.isEmpty())
				return false;
			boolean allUnused = true;
			int size = base.size();
			for (int i = 0; i < size; i++) {
				LocationNode node = base.get(i);
				if (colorLocationTree(node.children))
					node.setUnused(true);
				allUnused &= node.isUnused();
			}
			return allUnused;
		}

		public void saveSettings() {
			if (radioGroup != null)
				radioGroup.saveSettings();
			flatComponent.saveSettings();
			if (locTreeViewer != null) {
				Object[] expandedElements = locTreeViewer.getExpandedElements();
				StringBuilder sb = new StringBuilder();
				StringBuilder sbp = new StringBuilder();
				for (int i = 0; i < expandedElements.length; i++) {
					LocationNode node = (LocationNode) expandedElements[i];
					sbp.setLength(0);
					node.buildPath(sbp);
					if (sb.length() > 0)
						sb.append(';');
					sb.append(sbp);
				}
				settings.put(TREEEXPANSION + type, sb.toString());
			}
		}

		public void handleEvent(Event e) {
			if (e.type == SWT.Dispose)
				Job.getJobManager().cancel(this);
			else {
				Widget widget = e.widget;
				if (widget == radioGroup) {
					if (type == QueryField.T_LOCATION)
						updateLocationStack();
					else
						flatComponent.update();
				} else if (widget == addButton)
					addStruct();
				else if (widget == editButton)
					editStruct(type);
				else if (widget == removeButton) {
					removeItem();
					if (type == QueryField.T_LOCATION)
						geographic = null;
				} else if (widget == cleanButton) {
					cleanup(type);
					if (type == QueryField.T_LOCATION)
						geographic = null;
				} else if (widget == showButton) {
					if (workbenchPage != null) {
						Ui.getUi().getNavigationHistory(workbenchPage.getWorkbenchWindow())
								.postSelection(new StructuredSelection(createAdhocQuery(getSelectedElement())));
						close();
					}
				} else if (widget == mapButton)
					showMap();
				else if (widget == emailButton) {
					String[] email = ((ContactImpl) getSelectedElement()).getEmail();
					if (email != null && email.length > 0)
						UiActivator.getDefault().sendMail(Arrays.asList(email));
				} else if (widget == webButton) {
					String[] web = ((ContactImpl) getSelectedElement()).getWebUrl();
					if (web != null && web.length == 1)
						try {
							PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser()
									.openURL(new URL(Core.furnishWebUrl(web[0])));
						} catch (PartInitException ex) {
							UiActivator.getDefault().logError(Messages.EditMetaDialog_cannot_open_web_browser, ex);
						} catch (MalformedURLException ex) {
							UiActivator.getDefault().logError(Messages.EditMetaDialog_invalid_web_url, ex);
						}
				}
			}
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
			updateStructButtons();
		}

		private void showMap() {
			Object selectedElement = getSelectedElement();
			LocationImpl loc = (LocationImpl) getModelElement(selectedElement);
			if (loc == null && selectedElement instanceof LocationNode) {
				LocationNode node = (LocationNode) selectedElement;
				loc = new LocationImpl();
				while (node != null && !node.isUnknown()) {
					switch (node.getLevel()) {
					case LocationNode.COUNTRY:
						loc.setCountryISOCode(node.getKey());
						String name = node.getName();
						int p = name.lastIndexOf('(');
						loc.setCountryName(p >= 0 ? name.substring(0, p).trim() : name);
						break;
					case LocationNode.STATE:
						loc.setProvinceOrState(node.getName());
						break;
					case LocationNode.CITY:
						loc.setCity(node.getName());
						break;
					case LocationNode.DETAIL:
						loc.setDetails(node.getName());
						break;
					}
					node = node.getParent();
				}
			}
			if (loc != null) {
				ILocationDisplay ldis = UiActivator.getDefault().getLocationDisplay();
				if (ldis != null)
					ldis.display(loc);
				close();
			}
		}

		private void addStruct() {
			EditStructDialog dialog = new EditStructDialog(getShell(), null, type, -1, structOverlayMap,
					Messages.EditMetaDialog_add_n);
			if (dialog.open() == Window.OK) {
				IdentifiableObject result = dialog.getResult();
				newStructMap.put(result.getStringId(), result);
				flatComponent.add(result);
				if (result instanceof LocationImpl) {
					buildLocationTree(root, null, (LocationImpl) result, LocationNode.CONTINENT, true);
					colorLocationTree(root);
					setTreeInput(root);
					geographic = null;
				}
			}
		}

		@Override
		public void doubleClick(DoubleClickEvent event) {
			editStruct(type);
		}

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			updateStructButtons();
			if (event.getSource() == locTreeViewer) {
				if (cntrlDwn) {
					if (editButton.isEnabled()) {
						LocationNode node = (LocationNode) ((IStructuredSelection) locTreeViewer.getSelection())
								.getFirstElement();
						if (node != null && node.getLocation() != null)
							editStruct(QueryField.T_LOCATION);
					}
					cntrlDwn = false;
				}
			}
		}
	}

	public class LocTreeContentProvider implements ITreeContentProvider {

		public void dispose() {
			// do nothing
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// do nothing
		}

		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof Collection<?>)
				return ((Collection<?>) inputElement).toArray();
			return EMPTYOBJECTARRAY;
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof LocationNode) {
				List<LocationNode> children = ((LocationNode) parentElement).getChildren();
				if (children != null)
					return children.toArray();
			}
			return EMPTYOBJECTARRAY;
		}

		public Object getParent(Object element) {
			return (element instanceof LocationNode) ? ((LocationNode) element).getParent() : null;
		}

		public boolean hasChildren(Object element) {
			if (element instanceof LocationNode) {
				List<LocationNode> children = ((LocationNode) element).getChildren();
				return (children != null && !children.isEmpty());
			}
			return false;
		}

	}

	public class LocationNode {
		public static final int CONTINENT = 0;
		public static final int COUNTRY = 1;
		public static final int STATE = 2;
		public static final int CITY = 3;
		public static final int DETAIL = 4;

		private String name;
		private String key;
		private List<LocationNode> children;
		private final LocationNode parent;
		private final LocationImpl location;
		private final int level;
		private final boolean unknown;
		private boolean unused;

		public LocationNode(LocationNode parent, String key, String name, int level, LocationImpl location,
				boolean unknown, boolean unused) {
			this.parent = parent;
			this.key = key;
			this.name = name;
			this.level = level;
			this.location = location;
			this.unknown = unknown;
			this.unused = unused;
		}

		public LocationNode find(String s) {
			String start;
			int p = s.indexOf('>');
			if (p >= 0)
				start = s.substring(0, p);
			else
				start = s;
			if (start.equals(name)) {
				if (p < 0)
					return this;
				if (children != null)
					for (LocationNode child : children) {
						LocationNode found = child.find(s.substring(p + 1));
						if (found != null)
							return found;
					}
			}
			return null;
		}

		public void buildPath(StringBuilder sbp) {
			if (sbp.length() > 0)
				sbp.insert(0, '>');
			sbp.insert(0, name);
			if (parent != null)
				parent.buildPath(sbp);
		}

		public void setUnused(boolean unused) {
			this.unused = unused;
		}

		public boolean isUnused() {
			return unused;
		}

		public LocationNode getParent() {
			return parent;
		}

		@Override
		public String toString() {
			if (name != null)
				return name;
			StringBuilder sb = new StringBuilder();
			append(sb, getUpdatedValue(location, QueryField.LOCATION_DETAILS, location.getDetails()));
			String sublocation = location.getSublocation();
			if (sublocation != null && !sublocation.equals(location.getCity()))
				append(sb, sublocation);
			Double latitude = location.getLatitude();
			if (latitude != null && !Double.isNaN(latitude))
				append(sb, Format.latitudeFormatter.format(latitude));
			Double longitude = location.getLongitude();
			if (longitude != null && !Double.isNaN(longitude))
				append(sb, Format.longitudeFormatter.format(longitude));
			Double altitude = location.getAltitude();
			if (altitude != null && !Double.isNaN(altitude))
				append(sb, Format.altitudeFormatter.format(altitude));
			append(sb, location.getPlusCode());
			return (sb.length() == 0) ? NO_DETAILS : sb.toString();
		}

		private void append(StringBuilder sb, String s) {
			if (s != null && !s.isEmpty()) {
				if (sb.length() > 0)
					sb.append("; "); //$NON-NLS-1$
				sb.append(s);
			}
		}

		public boolean addChild(LocationNode e) {
			return getChildren().add(e);
		}

		public String getName() {
			return name;
		}

		public List<LocationNode> getChildren() {
			if (children == null)
				children = new ArrayList<EditMetaDialog.LocationNode>();
			return children;
		}

		public String getKey() {
			return key;
		}

		public int getLevel() {
			return level;
		}

		public LocationImpl getLocation() {
			return location;
		}

		public boolean isUnknown() {
			return unknown;
		}

	}

	public static int WELCOME = -1;
	public static int OVERVIEW = 0;
	public static int THUMBNAILS = 1;
	public static int CATEGORIES = 2;
	public static int KEYWORDS = 3;
	public static int INDEXING = 4;
	public static int LOCATIONS = 5;
	public static int ARTWORK = 6;
	public static int CONTACTS = 7;
	public static int STATISTICS = 8;
	public static int WATCHEDFOLDERS = 9;

	private static final String DETAILS = "details"; //$NON-NLS-1$

	private static final Object[] EMPTY = new Object[0];
	private static final String[] CATEXTENSIONS = new String[] { "*" //$NON-NLS-1$
			+ Constants.CATEGORYFILEEXTENSION + ";*" //$NON-NLS-1$
			+ Constants.CATEGORYFILEEXTENSION.toUpperCase() };
	private static final long HIGHWATERMARK = 2L * 1024L * 1024L * 1024L - 205L * 1024L * 1024L;
	private static final String NO_DETAILS = Messages.EditMetaDialog_no_details;

	private static final String HIERARCHICAL_STRUCT = "hierarchicalStruct"; //$NON-NLS-1$
	private static final String TREEEXPANSION = "treeExpansion"; //$NON-NLS-1$

	private Text yearlySeqNoField;
	private Text seqNoField;
	private Button fromPreviewButton;
	private TextWithVariableGroup backupField;
	protected static final IInputValidator keywordValidator = new KeywordValidator();
	static final String[] KEYWORDEXTENSIONS = new String[] { "*" //$NON-NLS-1$
			+ Constants.KEYWORDFILEEXTENSION + ";*" //$NON-NLS-1$
			+ Constants.KEYWORDFILEEXTENSION.toUpperCase(), Messages.EditMetaDialog_0 };
	private Text fileName;
	private Text creationDate;
	private Text lastImport;
	private CheckedText ownerInformation;
	private CheckedText description;
	private Text userFieldLabel1;
	private Text userFieldLabel2;
	private Meta meta;
	private TreeViewer keywordViewer;
	private TreeViewer catTreeViewer;
	private Button categoryRemoveButton;
	private Button categoryEditButton;
	private Button categoryAddButton;
	private Button categoryRefineButton;
	private Button keywordLoadButton;
	private Button keywordSaveButton;
	private Button saveCatButton;
	private Button loadCatButton;
	private Map<String, Category> categories;
	private ComboViewer timelineViewer;
	private IWorkbenchPage workbenchPage;
	private Label versionLabel;
	private Label catSizeField;
	private Label freeSpaceField;
	private Label freeSegmentsField;
	private Label collectionsField;
	private Label imagesField;
	private static final NumberFormat nf = NumberFormat.getNumberInstance();
	private static final int PREVIOUS = 99;
	private static final int NEXT = 98;
	protected static final Object EMPTYSTRINGARRAY = new String[0];
	protected final Object[] EMPTYOBJECTARRAY = new Object[0];
	private static final long ONEDAY = 86400000L;
	private static final String SETTINGSID = "com.bdaum.zoom.ui.editMetaDialog"; //$NON-NLS-1$
	protected static final String[] VOCABEXTENSIONS = null;
	private Label locationsField;
	private Label contactsField;
	private Label artworksField;
	private Label exhibitionsField;
	private Label slideshowsField;
	private Label webgalleriesField;
	private TableViewer watchedFolderViewer;
	private Button addFolderButton;
	private Button removeFolderButton;
	private List<WatchedFolder> watchedFolders = new ArrayList<>();
	private List<String> vocabularies = new ArrayList<>();
	private ArrayList<WatchedFolder> folderBackup;
	private CheckboxButton pauseButton;
	private CheckboxButton readOnlyButton;
	private boolean newDb = true;
	private CheckboxButton autoWatchButton;
	private Button keywordDeleteButton;
	private Button keywordAddButton;
	private Set<String> keywords;
	protected List<DbOperation> todo = new ArrayList<DbOperation>(3);
	private Button keywordCollectButton;
	private Button keywordShowButton;
	private CTabFolder tabFolder;
	private boolean[] visited;
	private ComboViewer languageCombo;
	private NumericControl latencyField;
	private Button keywordModifyButton;
	private CheckboxButton bgimportField;
	private Label personsField;
	private StructGroup locationGroup;
	private Button createTimeLineButton;
	private ComboViewer locationViewer;
	private Button createLocationFoldersButton;
	private CompressionGroup compressionGroup;
	private CheckboxTableViewer simViewer;
	private ContainerCheckedTreeViewer textIndexViewer;
	private CheckboxButton noIndexButton;
	private CheckboxButton slideTitleButton;
	private CheckboxButton slideDescrButton;
	private CheckboxButton exhibitionDescrButton;
	private CheckboxButton exhibitionTitleButton;
	private CheckboxButton webgalleryDescrButton;
	private CheckboxButton webgalleryTitleButton;
	private CheckboxButton personsButton;
	private CGroup textGroup;
	private CheckboxButton exhibitionCredButton;
	private CheckboxButton webgalleryAltButton;
	private int initialPage;
	private CheckboxButton addToKeywordsButton;
	private Map<String, Map<QueryField, Object>> structOverlayMap = new HashMap<String, Map<QueryField, Object>>();
	private Map<String, IIdentifiableObject> newStructMap = new HashMap<String, IIdentifiableObject>();
	private CheckboxButton fileNameButton;
	private Button keywordReplaceButton;
	private FlatGroup flatKeywordGroup;
	private StructGroup artworkGroup;
	private StructGroup contactsGroup;
	private Label groupField;
	private Label[] detailFields;
	private ProgressBar detailsProgressBar;
	private CGroup detailsGroup;
	private ExpandCollapseGroup keywordExpandCollapseGroup;
	private Button editFolderButton;
	private IDialogSettings settings;
	protected boolean excludeGeographic;
	private Set<String> geographic;
	private ComboViewer themeField;
	private Meta previousMeta;
	private Theme theme;
	private List<String> categoryChanges = new ArrayList<>();
	private CLink configureKeywordLink;
	private RadioButtonGroup initButtonGroup;
	private RadioButtonGroup thumbSizeGroup;
	private RadioButtonGroup sharpenButtonGroup;
	private Button vocabAddButton;
	private Button vocabRemoveButton;
	private Button vocabViewButton;
	private TableViewer vocabViewer;
	private Button vocabEnforceButton;
	private VocabManager vocabManager;
	private boolean essentialAlgos = true;
	private Set<String> cbirAlgorithms = new HashSet<String>(CoreActivator.getDefault().getCbirAlgorithms());
	private Composite simComp;
	private boolean cntrlDwn;
	private CLink backupIntervalLink;
	private Button showLocButton;
	private Button showDestButton;
	private CheckboxButton excludeButton;
	private Button algoButton;
	private CGroup simGroup;
	private CLink istallLink;
	private Button duplicateFolderButton;

	public EditMetaDialog(Shell parentShell, IWorkbenchPage workbenchPage, IDbManager dbManager, boolean newDb,
			Meta previousMeta) {
		super(parentShell, newDb ? HelpContextIds.NEWCAT_PROPERTIES_DIALOG : HelpContextIds.CAT_PROPERTIES_DIALOG);
		this.workbenchPage = workbenchPage;
		this.dbManager = dbManager;
		this.newDb = newDb;
		this.previousMeta = previousMeta;
		this.theme = previousMeta != null ? CoreActivator.getDefault().getCurrentTheme() : null;
		this.settings = getDialogSettings(UiActivator.getDefault(), SETTINGSID);
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.EditMetaDialog_edit_cat_properties);
		setMessage(Messages.EditMetaDialog_configure_this_cat);
		updateButtons();
		if (newDb) {
			final Shell c = getShell();
			c.getDisplay().timerExec(300, () -> {
				if (!c.isDisposed() && c.isListening(SWT.Help))
					c.notifyListeners(SWT.Help, new Event());
			});
		}
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		Composite comp = (Composite) super.createDialogArea(parent);
		comp.setLayout(new GridLayout());
		final Composite editArea = createHeaderGroup(comp);
		tabFolder = new CTabFolder(editArea, SWT.BORDER);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.verticalIndent = 5;
		tabFolder.setLayoutData(layoutData);
		int pgCnt = 0;
		if (newDb) {
			createWelcomeGroup(tabFolder);
			WELCOME = pgCnt++;
		}
		createOverviewGroup(tabFolder);
		OVERVIEW = pgCnt++;
		createThumbnailsGroup(tabFolder);
		THUMBNAILS = pgCnt++;
		createCategoriesGroup(parent, tabFolder);
		CATEGORIES = pgCnt++;
		createKeywordsGroup(tabFolder);
		KEYWORDS = pgCnt++;
		if (Core.getCore().getDbFactory().getLireServiceVersion() >= 0) {
			createIndexingGroup(tabFolder);
			INDEXING = pgCnt++;
		}
		locationGroup = createStructComponent(tabFolder, QueryField.T_LOCATION, Messages.EditMetaDialog_locations,
				Messages.EditMetaDialog_locations_tooltip, Messages.EditMetaDialog_location);
		LOCATIONS = pgCnt++;
		artworkGroup = createStructComponent(tabFolder, QueryField.T_OBJECT, Messages.EditMetaDialog_artwork,
				Messages.EditMetaDialog_artworks_tooltip, Messages.EditMetaDialog_object);
		ARTWORK = pgCnt++;
		contactsGroup = createStructComponent(tabFolder, QueryField.T_CONTACT, Messages.EditMetaDialog_contacts,
				Messages.EditMetaDialog_contacts_tooltip, Messages.EditMetaDialog_contact);
		CONTACTS = pgCnt++;
		createStatisticsGroup(tabFolder);
		STATISTICS = pgCnt++;
		createWatchedFolderGroup(tabFolder);
		WATCHEDFOLDERS = pgCnt++;
		visited = new boolean[pgCnt];
		tabFolder.setSimple(false);
		tabFolder.setSelection(initialPage);
		initHeader();
		initPages(initialPage);
		tabFolder.addListener(SWT.Selection, this);
		updateTabItems();
		if (CATEGORIES == initialPage)
			catTreeViewer.expandAll();
		return comp;
	}

	private void createWelcomeGroup(CTabFolder folder) {
		final Composite composite = createTabPage(folder, Messages.EditMetaDialog_welcome,
				Messages.EditMetaDialog_welcome_tooltip, 1);
		Composite comp = new Composite(composite, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 50;
		layout.marginHeight = 100;
		comp.setLayout(layout);
		Label label = new Label(comp, SWT.WRAP);
		label.setFont(JFaceResources.getHeaderFont());
		label.setAlignment(SWT.CENTER);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		label.setText(Messages.EditMetaDialog_you_have_opted);
		if (previousMeta != null) {
			initButtonGroup = new RadioButtonGroup(comp, Messages.EditMetaDialog_initial_values, SWT.NONE,
					Messages.EditMetaDialog_default_values, Messages.EditMetaDialog_copy_values);
			initButtonGroup.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
			initButtonGroup.setSelection(1);
		}
	}

	@SuppressWarnings("unused")
	private void createIndexingGroup(CTabFolder folder) {

		final Composite ixComp = createTabPage(folder, Messages.EditMetaDialog_indexing,
				Messages.EditMetaDialog_configure_index, 1, 0);
		final Composite composite = new Composite(ixComp, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout());
		noIndexButton = WidgetFactory.createCheckButton(composite, Messages.EditMetaDialog_no_indexing, null);
		noIndexButton.addListener(SWT.Selection, this);
		simComp = new Composite(composite, SWT.NONE);
		simComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		simComp.setLayout(layout);
		Label label = new Label(simComp, SWT.WRAP);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		label.setText(Messages.EditMetaDialog_influences_speed);
		label = new Label(simComp, SWT.WRAP);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		label.setText(Messages.EditMetaDialog_configure_index_warning);
		Composite vGroup = new Composite(simComp, SWT.NONE);
		vGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		vGroup.setLayout(layout);
		simGroup = new CGroup(vGroup, SWT.NONE);
		simGroup.setText(Messages.EditMetaDialog_similarity_algos);
		simGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		simGroup.setLayout(new GridLayout());
		simViewer = CheckboxTableViewer.newCheckList(simGroup, SWT.FULL_SELECTION | SWT.SINGLE | SWT.V_SCROLL);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		layoutData.heightHint = 200;
		layoutData.widthHint = 400;
		simViewer.getControl().setLayoutData(layoutData);
		simViewer.setContentProvider(ArrayContentProvider.getInstance());
		ZColumnViewerToolTipSupport.enableFor(simViewer, ToolTip.NO_RECREATE);
		simViewer.setComparator(ZViewerComparator.INSTANCE);
		simViewer.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getToolTipText(Object element) {
				if (element instanceof Algorithm && UiActivator.getDefault().getShowHover())
					return ((Algorithm) element).getDescription();
				return super.getToolTipText(element);
			}

			@Override
			public String getText(Object element) {
				return element.toString();
			}

			@Override
			public Image getImage(Object element) {
				if (element instanceof Algorithm && ((Algorithm) element).isAi())
					return Icons.neural.getImage();
				return null;
			}
		});
		algoButton = new Button(simGroup, SWT.PUSH);
		algoButton.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
		algoButton.setText(Messages.EditMetaDialog_show_more);
		algoButton.addListener(SWT.Selection, this);
		simViewer.addCheckStateListener(this);
		simViewer.setFilters(new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (essentialAlgos && element instanceof Algorithm)
					return ((Algorithm) element).isEssential()
							|| cbirAlgorithms.contains(((Algorithm) element).getName());
				return true;
			}
		});
		simViewer.setInput(Core.getCore().getDbFactory().getLireService(true).getSupportedSimilarityAlgorithms());
		textGroup = new CGroup(vGroup, SWT.NONE);
		textGroup.setText(Messages.EditMetaDialog_fields_in_text_search);
		textGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		textGroup.setLayout(new GridLayout());
		textIndexViewer = new ContainerCheckedTreeViewer(textGroup, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		setViewerLayout(textIndexViewer, SWT.DEFAULT, 1);
		textIndexViewer.setLabelProvider(new MetadataLabelProvider());
		textIndexViewer.setContentProvider(new MetadataContentProvider());
		textIndexViewer.setFilters(new ViewerFilter[] { new ViewerFilter() {
			@Override
			public boolean select(Viewer aViewer, Object parentElement, Object element) {
				if (element instanceof QueryField && ((QueryField) element).getLabel() != null)
					return isFullTextSearch((QueryField) element);
				return false;
			}

			private boolean isFullTextSearch(QueryField qf) {
				if (qf.hasChildren()) {
					for (QueryField child : qf.getChildren())
						if (isFullTextSearch(child))
							return true;
					return false;
				}
				return qf.isFullTextSearch();
			}
		} });
		textIndexViewer.setComparator(ZViewerComparator.INSTANCE);
		UiUtilities.installDoubleClickExpansion(textIndexViewer);
		textIndexViewer.setInput(QueryField.ALL);
		textIndexViewer.expandToLevel(1);
		textIndexViewer.expandToLevel(QueryField.IMAGE_ALL, 1);
		Composite bGroup = new Composite(textGroup, SWT.NONE);
		bGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		bGroup.setLayout(new GridLayout(4, false));
		fileNameButton = WidgetFactory.createCheckButton(bGroup, Messages.EditMetaDialog_file_name, null);
		personsButton = WidgetFactory.createCheckButton(bGroup, Messages.EditMetaDialog_persons_shown,
				new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 3, 1));
		Label sep = new Label(bGroup, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));

		Label lab = new Label(bGroup, SWT.NONE);
		lab.setFont(JFaceResources.getBannerFont());
		lab.setText(Messages.EditMetaDialog_slide_shows);
		lab.setLayoutData(new GridData(140, SWT.DEFAULT));
		slideTitleButton = WidgetFactory.createCheckButton(bGroup, Messages.EditMetaDialog_titles, null);
		slideDescrButton = WidgetFactory.createCheckButton(bGroup, Messages.EditMetaDialog_descriptions, null);
		new Label(bGroup, SWT.NONE);
		lab = new Label(bGroup, SWT.NONE);
		lab.setFont(JFaceResources.getBannerFont());
		lab.setText(Messages.EditMetaDialog_exhibitions);
		exhibitionTitleButton = WidgetFactory.createCheckButton(bGroup, Messages.EditMetaDialog_titles, null);
		exhibitionDescrButton = WidgetFactory.createCheckButton(bGroup, Messages.EditMetaDialog_descriptions, null);
		exhibitionCredButton = WidgetFactory.createCheckButton(bGroup, Messages.EditMetaDialog_credits, null);
		lab = new Label(bGroup, SWT.NONE);
		lab.setFont(JFaceResources.getBannerFont());
		lab.setText(Messages.EditMetaDialog_webgalleries);
		webgalleryTitleButton = WidgetFactory.createCheckButton(bGroup, Messages.EditMetaDialog_titles, null);
		webgalleryDescrButton = WidgetFactory.createCheckButton(bGroup, Messages.EditMetaDialog_descriptions, null);
		webgalleryAltButton = WidgetFactory.createCheckButton(bGroup, Messages.EditMetaDialog_alt_texts, null);
	}

	protected void validateAlgorithms() {
		Object[] checkedElements = simViewer.getCheckedElements();
		String errorMessage = null;
		if (checkedElements.length == 0)
			errorMessage = Messages.EditMetaDialog_select_at_least_one;
		else {
			for (Object element : checkedElements) {
				if (element instanceof Algorithm) {
					Algorithm aiAlgorithm = (Algorithm) element;
					if (!aiAlgorithm.isEnabled())
						errorMessage = NLS.bind(Messages.EditMetaDialog_ai_enabled, aiAlgorithm.getName());
					else if (!aiAlgorithm.isAccountValid())
						errorMessage = NLS.bind(Messages.EditMetaDialog_account_valid, aiAlgorithm.getName());
					if (errorMessage != null)
						break;
				}
			}
		}

		setErrorMessage(errorMessage);
		getButton(OK).setEnabled(errorMessage == null);
	}

	private void updateIndexingControls() {
		boolean enabled = !noIndexButton.getSelection();
		simComp.setVisible(enabled);
		textGroup.setVisible(enabled);
	}

	protected void updateTabItems() {
		CTabItem selection = tabFolder.getSelection();
		for (CTabItem item : tabFolder.getItems())
			item.setFont(selection == item ? JFaceResources.getBannerFont() : JFaceResources.getDefaultFont());
	}

	private Composite createHeaderGroup(Composite comp) {
		final Composite header = new Composite(comp, SWT.NONE);
		header.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		header.setLayout(new GridLayout(4, false));
		// Line 1
		new Label(header, SWT.NONE).setText(Messages.EditMetaDialog_file_name);
		Composite catGroup = new Composite(header, SWT.NONE);
		catGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = 0;
		catGroup.setLayout(gridLayout);

		fileName = new Text(catGroup, SWT.READ_ONLY | SWT.BORDER);
		fileName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fileName.addListener(SWT.MouseDoubleClick, this);
		versionLabel = new Label(catGroup, SWT.NONE);

		GridData data = new GridData(SWT.END, SWT.CENTER, false, false);
		data.horizontalIndent = 20;
		versionLabel.setLayoutData(data);
		// Line 2
		new Label(header, SWT.NONE).setText(Messages.EditMetaDialog_catalog_theme);
		themeField = new ComboViewer(header, SWT.DROP_DOWN | SWT.READ_ONLY);
		themeField.getControl().setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));
		themeField.setContentProvider(ArrayContentProvider.getInstance());
		themeField.setLabelProvider(new LabelProvider());
		themeField.setInput(CoreActivator.getDefault().getThemes().values());
		final BundleContext bundleContext = UiActivator.getDefault().getBundle().getBundleContext();
		final ServiceReference<?> ref = bundleContext.getServiceReference(ISpellCheckingService.class.getName());
		if (ref != null) {
			Label label = new Label(header, SWT.RIGHT);
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			label.setText(Messages.EditMetaDialog_language);
			final ISpellCheckingService service = (ISpellCheckingService) bundleContext.getService(ref);
			Collection<String> supportedLanguages = service.getSupportedLanguages();
			bundleContext.ungetService(ref);
			if (supportedLanguages != null && !supportedLanguages.isEmpty()) {
				languageCombo = new ComboViewer(header);
				languageCombo.setContentProvider(ArrayContentProvider.getInstance());
				languageCombo.setLabelProvider(new LabelProvider() {
					@Override
					public String getText(Object element) {
						if (element instanceof String) {
							String prefix = (String) element;
							String variant = ""; //$NON-NLS-1$
							int p = prefix.indexOf('_');
							if (p > 0) {
								p = prefix.indexOf('_', p + 1);
								if (p > 0) {
									String v = prefix.substring(p + 1);
									if ("frami".equals(v)) //$NON-NLS-1$
										v = Messages.EditMetaDialog_frami;
									variant = " (" + v + ')'; //$NON-NLS-1$
									prefix = prefix.substring(0, p);
								}
							}
							for (Locale locale : Locale.getAvailableLocales())
								if (locale.toString().equals(prefix))
									return locale.getDisplayName() + variant;
						}
						return super.getText(element);
					}
				});
				languageCombo.setComparator(ZViewerComparator.INSTANCE);
				languageCombo.setInput(supportedLanguages);
			} else {
				istallLink = new CLink(header, SWT.NONE);
				istallLink.setText(Messages.EditMetaDialog_intall_dict);
				istallLink.addListener(SWT.Selection, this);
			}
		} else
			new Label(header, SWT.NONE).setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 2, 1));
		// Line 3
		Composite line3Comp = new Composite(header, SWT.NONE);
		line3Comp.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 2, 1));
		GridLayout layout = new GridLayout(3, false);
		layout.marginHeight = layout.marginWidth = 0;
		line3Comp.setLayout(layout);

		new Label(line3Comp, SWT.NONE).setText(Messages.EditMetaDialog_creation_date);

		creationDate = new Text(line3Comp, SWT.READ_ONLY | SWT.BORDER);
		creationDate.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));

		readOnlyButton = WidgetFactory.createCheckButton(line3Comp, Messages.EditMetaDialog_write_protected,
				new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 1, 1));
		readOnlyButton.addListener(SWT.Selection, this);
		readOnlyButton.setEnabled(!newDb);

		Label label = new Label(header, SWT.RIGHT);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		label.setText(Messages.EditMetaDialog_last_import);

		lastImport = new Text(header, SWT.READ_ONLY | SWT.BORDER);
		lastImport.addListener(SWT.MouseDoubleClick, this);
		// Line 4
		new Label(header, SWT.NONE).setText(Messages.EditMetaDialog_seqno);

		seqNoField = new Text(header, SWT.READ_ONLY | SWT.BORDER);
		seqNoField.setLayoutData(new GridData(80, SWT.DEFAULT));

		label = new Label(header, SWT.RIGHT);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		label.setText(Messages.EditMetaDialog_yearlyseqno);

		yearlySeqNoField = new Text(header, SWT.READ_ONLY | SWT.BORDER);
		yearlySeqNoField.setLayoutData(new GridData(80, SWT.DEFAULT));

		// Line 5
		new Label(header, SWT.NONE).setText(Messages.EditMetaDialog_backup_file);
		Composite backupGroup = new Composite(header, SWT.NONE);
		backupGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 3, 1));
		gridLayout = new GridLayout(3, false);
		gridLayout.marginWidth = 0;
		backupGroup.setLayout(gridLayout);
		backupField = new TextWithVariableGroup(backupGroup, null, 400, Constants.BV_ALL, false, null, null, null); // $NON-NLS-1$
		backupIntervalLink = new CLink(backupGroup, SWT.NONE);
		backupIntervalLink.setText(Messages.EditMetaDialog_configure_interval);
		backupIntervalLink.addListener(SWT.Selection, this);
		final Composite editArea = new Composite(comp, SWT.NONE);
		editArea.setLayout(new GridLayout());
		editArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		return editArea;
	}

	protected void updateFields() {
		ownerInformation.setEnabled(!readonly);
		themeField.getCombo().setEnabled(!readonly);
		description.setEnabled(!readonly);
		userFieldLabel1.setEnabled(!readonly);
		userFieldLabel2.setEnabled(!readonly);
		bgimportField.setEnabled(!readonly);
		timelineViewer.getCombo().setEnabled(!readonly);
		if (createTimeLineButton != null)
			createTimeLineButton.setEnabled(!readonly);
		locationViewer.getCombo().setEnabled(!readonly);
		if (createLocationFoldersButton != null)
			createLocationFoldersButton.setEnabled(!readonly);
		thumbSizeGroup.setEnabled(!readonly);
		fromPreviewButton.setEnabled(!readonly);
		configureKeywordLink.setEnabled(!readonly);
		addToKeywordsButton.setEnabled(!readonly);
		watchedFolderViewer.getTable().setEnabled(!readonly);
		latencyField.setEnabled(!readonly);
		pauseButton.setEnabled(!readonly);
		if (languageCombo != null)
			languageCombo.getControl().setEnabled(!readonly);
		bgimportField.setEnabled(!readonly);
		autoWatchButton.setEnabled(!readonly);
		locationGroup.setEnabled(!readonly);
		locationViewer.getCombo().setEnabled(!readonly);
		compressionGroup.setEnabled(!readonly);
		flatKeywordGroup.setEnabled(!readonly);
		artworkGroup.setEnabled(!readonly);
		contactsGroup.setEnabled(!readonly);
	}

	private void createOverviewGroup(final CTabFolder folder) {
		final Composite composite = createTabPage(folder, Messages.EditMetaDialog_overview,
				Messages.EditMetaDialog_owner_info, 1, 0);
		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		layoutData.heightHint = 40;
		layoutData.widthHint = 400;
		ownerInformation = new CheckedText(composite, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.WRAP);
		ownerInformation.setSpellingOptions(8, ISpellCheckingService.DESCRIPTIONOPTIONS);
		ownerInformation.setLayoutData(layoutData);
		new Label(composite, SWT.NONE).setText(Messages.EditMetaDialog_description);

		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.heightHint = 100;
		layoutData.widthHint = 400;
		description = new CheckedText(composite, SWT.V_SCROLL | SWT.BORDER | SWT.MULTI | SWT.WRAP);
		description.setLayoutData(layoutData);
		final Composite optionsGroup = new Composite(composite, SWT.NONE);
		optionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		optionsGroup.setLayout(new GridLayout(7, false));

		new Label(optionsGroup, SWT.NONE).setText(Messages.EditMetaDialog_create_timeline);

		timelineViewer = new ComboViewer(optionsGroup);
		timelineViewer.setContentProvider(ArrayContentProvider.getInstance());
		timelineViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (Meta_type.timeline_year.equals(element))
					return Messages.EditMetaDialog_by_year;
				if (Meta_type.timeline_month.equals(element))
					return Messages.EditMetaDialog_by_month;
				if (Meta_type.timeline_day.equals(element))
					return Messages.EditMetaDialog_by_day;
				if (Meta_type.timeline_week.equals(element))
					return Messages.EditMetaDialog_by_week;
				if (Meta_type.timeline_weekAndDay.equals(element))
					return Messages.EditMetaDialog_by_week_and_day;
				return Messages.EditMetaDialog_none;
			}
		});
		timelineViewer.setInput(Meta_type.timelineALLVALUES);
		if (!newDb) {
			createTimeLineButton = new Button(optionsGroup, SWT.PUSH);
			createTimeLineButton.setText(Messages.EditMetaDialog_create_now);
			createTimeLineButton.setToolTipText(Messages.EditMetaDialog_recreate_timeline);
			createTimeLineButton.addListener(SWT.Selection, this);
		}
		timelineViewer.addSelectionChangedListener(this);
		new Label(optionsGroup, SWT.NONE).setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		new Label(optionsGroup, SWT.NONE).setText(Messages.EditMetaDialog_create_loc_folders);

		locationViewer = new ComboViewer(optionsGroup);
		locationViewer.setContentProvider(ArrayContentProvider.getInstance());
		locationViewer.setLabelProvider(new ZColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				if (Meta_type.locationFolders_country.equals(element))
					return Messages.EditMetaDialog_byCountry;
				if (Meta_type.locationFolders_state.equals(element))
					return Messages.EditMetaDialog_byState;
				if (Meta_type.locationFolders_city.equals(element))
					return Messages.EditMetaDialog_byCity;
				return Messages.EditMetaDialog_none;
			}
		});
		locationViewer.setInput(Meta_type.locationFoldersALLVALUES);
		createLocationFoldersButton = new Button(optionsGroup, SWT.PUSH);
		createLocationFoldersButton.setText(Messages.EditMetaDialog_create_now);
		createLocationFoldersButton.setToolTipText(Messages.EditMetaDialog_recreate_locations);
		createLocationFoldersButton.addListener(SWT.Selection, this);
		locationViewer.addSelectionChangedListener(this);

		Composite userfieldGroup = new Composite(composite, SWT.NONE);
		userfieldGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		userfieldGroup.setLayout(new GridLayout(2, false));
		new Label(userfieldGroup, SWT.NONE).setText(Messages.EditMetaDialog_user_field_lab_1);

		userFieldLabel1 = new Text(userfieldGroup, SWT.BORDER);
		userFieldLabel1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		new Label(userfieldGroup, SWT.NONE).setText(Messages.EditMetaDialog_user_field_lab_2);

		userFieldLabel2 = new Text(userfieldGroup, SWT.BORDER);
		userFieldLabel2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	}

	protected void updateCreateNowButtons() {
		String timeline = (String) timelineViewer.getStructuredSelection().getFirstElement();
		if (createTimeLineButton != null) {
			if (timeline == null)
				createTimeLineButton.setEnabled(false);
			else {
				createTimeLineButton.setEnabled(true);
				if (Meta_type.timeline_no.equals(timeline))
					createTimeLineButton.setText(Messages.EditMetaDialog_remove_now);
				else
					createTimeLineButton.setText(Messages.EditMetaDialog_create_now);
			}
		}
		String locfolder = (String) locationViewer.getStructuredSelection().getFirstElement();
		if (locfolder == null)
			createLocationFoldersButton.setEnabled(false);
		else {
			createLocationFoldersButton.setEnabled(true);
			if (Meta_type.timeline_no.equals(locfolder))
				createLocationFoldersButton.setText(Messages.EditMetaDialog_remove_now);
			else
				createLocationFoldersButton.setText(Messages.EditMetaDialog_create_now);
		}
	}

	private void computeStatistics() {
		Map<String, Long> statistics = dbManager.getStatistics();
		long totalSize = statistics.get(IDbManager.TOTALSIZE);
		catSizeField.setText(nf.format(totalSize));
		if (totalSize > HIGHWATERMARK) {
			freeSpaceField.setData(CSSProperties.ID, CSSProperties.ERRORS);
			freeSpaceField.setForeground(freeSpaceField.getDisplay().getSystemColor(SWT.COLOR_RED));
		} else {
			freeSpaceField.setData(CSSProperties.ID, null);
			freeSpaceField.setForeground(freeSpaceField.getParent().getForeground());
		}
		freeSpaceField.setText(nf.format(statistics.get(IDbManager.FREESPACE)));
		freeSegmentsField.setText(nf.format(statistics.get(IDbManager.FREESPACEENTRIES)));
		fillCountField(GroupImpl.class, groupField);
		fillCountField(SmartCollectionImpl.class, collectionsField);
		fillCountField(AssetImpl.class, imagesField);
		fillCountField(LocationImpl.class, locationsField);
		fillCountField(ContactImpl.class, contactsField);
		fillCountField(ArtworkOrObjectImpl.class, artworksField);
		fillCountField(ExhibitionImpl.class, exhibitionsField);
		fillCountField(SlideShowImpl.class, slideshowsField);
		fillCountField(WebGalleryImpl.class, webgalleriesField);
		List<SmartCollectionImpl> albums = dbManager.obtainObjects(SmartCollectionImpl.class, false, "system", true, //$NON-NLS-1$
				QueryField.EQUALS, "album", true, QueryField.EQUALS); //$NON-NLS-1$
		personsField.setText(nf.format(albums.size()));
	}

	private void fillCountField(Class<? extends IdentifiableObject> clazz, Label field) {
		field.setText(nf.format(dbManager.obtainObjects(clazz).size()));
	}

	private void createStatisticsGroup(CTabFolder folder) {
		final Composite stComp = createTabPage(folder, Messages.EditMetaDialog_Statistics,
				Messages.EditMetaDialog_statistics_tooltip, 2, 0);
		Composite composite = new Composite(stComp, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));
		detailsGroup = new CGroup(stComp, SWT.NONE);
		detailsGroup.setText(Messages.EditMetaDialog_details);
		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, true);
		layoutData.horizontalIndent = 30;
		detailsGroup.setLayoutData(layoutData);
		detailsGroup.setLayout(new GridLayout(2, false));
		detailFields = new Label[16];
		for (int i = 0; i < detailFields.length; i++)
			detailFields[i] = createDetailsField(detailsGroup, 100);
		detailsProgressBar = new ProgressBar(detailsGroup, SWT.HORIZONTAL);
		GridData data = new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1);
		data.heightHint = 5;
		detailsProgressBar.setLayoutData(data);
		detailsProgressBar.setVisible(false);
		detailsGroup.setVisible(false);

		catSizeField = createTextField(composite, Messages.EditMetaDialog_Cat_file_size, 100, null, null);
		freeSpaceField = createTextField(composite, Messages.EditMetaDialog_free_space, 100, null, null);
		freeSegmentsField = createTextField(composite, Messages.EditMetaDialog_free_segments, 100,
				dbManager.isEmbedded() ? this : null, Messages.EditMetaDialog_defrag_now);
		createSeparator(composite);
		groupField = createTextField(composite, Messages.EditMetaDialog_groups, 100,
				new DetailSelectionAdapter(GroupImpl.class, Messages.EditMetaDialog_group_details), null);
		collectionsField = createTextField(composite, Messages.EditMetaDialog_collections, 100,
				new DetailSelectionAdapter(SmartCollectionImpl.class, Messages.EditMetaDialog_collection_details),
				null);
		imagesField = createTextField(composite, Messages.EditMetaDialog_images, 100, null, null);
		createSeparator(composite);
		locationsField = createTextField(composite, Messages.EditMetaDialog_locations, 100,
				new DetailSelectionAdapter(LocationImpl.class, Messages.EditMetaDialog_location_details), null);
		contactsField = createTextField(composite, Messages.EditMetaDialog_contacts, 100, null, null);
		artworksField = createTextField(composite, Messages.EditMetaDialog_artworks, 100, null, null);
		personsField = createTextField(composite, Messages.EditMetaDialog_persons, 100, null, null);
		createSeparator(composite);
		exhibitionsField = createTextField(composite, Messages.EditMetaDialog_exhibitions, 100, null, null);
		slideshowsField = createTextField(composite, Messages.EditMetaDialog_slideshows, 100, null, null);
		webgalleriesField = createTextField(composite, Messages.EditMetaDialog_webgalleries, 100, null, null);
		composite.pack();
	}

	protected void setDetails(int i, String label, int value) {
		Label textField = detailFields[i];
		((Label) textField.getData(UiConstants.LABEL)).setText(label);
		textField.setText(value < 0 ? "" : String.valueOf(value)); //$NON-NLS-1$
		for (int j = i + 1; j < detailFields.length; j++) {
			((Label) detailFields[j].getData(UiConstants.LABEL)).setText(""); //$NON-NLS-1$
			detailFields[j].setText(""); //$NON-NLS-1$
		}
	}

	@SuppressWarnings("unused")
	private void createWatchedFolderGroup(CTabFolder folder) {
		final Composite wfComp = createTabPage(folder, Messages.EditMetaDialog_watched_folders,
				Messages.EditMetaDialog_watchedFolders_tooltip, 1, 0);
		final Composite composite = new Composite(wfComp, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, false));
		watchedFolderViewer = new TableViewer(composite,
				SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		setViewerLayout(watchedFolderViewer, 200, 1);
		watchedFolderViewer.getTable().setLinesVisible(true);
		watchedFolderViewer.getTable().setHeaderVisible(true);
		createColumn(Messages.EditMetaDialog_path, 240)
				.setLabelProvider(new WatchedFolderLabelProvider(WatchedFolderLabelProvider.PATH));
		createColumn(Messages.EditMetaDialog_volume, 80)
				.setLabelProvider(new WatchedFolderLabelProvider(WatchedFolderLabelProvider.VOLUME));
		createColumn(Messages.EditMetaDialog_type, 60)
				.setLabelProvider(new WatchedFolderLabelProvider(WatchedFolderLabelProvider.TYPE));
		createColumn(Messages.EditMetaDialog_recursive, 80)
				.setLabelProvider(new WatchedFolderLabelProvider(WatchedFolderLabelProvider.RECURSIVE));
		createColumn(Messages.EditMetaDialog_last_observation, 120)
				.setLabelProvider(new WatchedFolderLabelProvider(WatchedFolderLabelProvider.LASTOBSERVATION));
		createColumn(Messages.EditMetaDialog_file_filter, 400)
				.setLabelProvider(new WatchedFolderLabelProvider(WatchedFolderLabelProvider.FILTERS));
		watchedFolderViewer.setContentProvider(ArrayContentProvider.getInstance());
		new SortColumnManager(watchedFolderViewer, new int[] { SWT.UP, SWT.UP, SWT.UP, SWT.NONE, SWT.UP, SWT.NONE }, 0);
		watchedFolderViewer.setComparator(new ZViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				Table table = watchedFolderViewer.getTable();
				TableColumn sortColumn = table.getSortColumn();
				if (sortColumn == table.getColumn(4)) {
					int sortDirection = table.getSortDirection();
					int a = (sortDirection & SWT.DOWN) != 0 ? 1 : -1;
					long t1 = ((WatchedFolder) e1).getLastObservation();
					long t2 = ((WatchedFolder) e2).getLastObservation();
					return t1 == t2 ? 0 : t1 < t2 ? a : -a;
				}
				return super.compare(viewer, e1, e2);
			}
		});
		ZColumnViewerToolTipSupport.enableFor(watchedFolderViewer);
		addKeyListener(watchedFolderViewer.getControl());
		watchedFolderViewer.addSelectionChangedListener(this);
		watchedFolderViewer.addDoubleClickListener(this);
		Composite buttonComp = new Composite(composite, SWT.NONE);
		buttonComp.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, true));
		buttonComp.setLayout(new GridLayout());
		addFolderButton = new Button(buttonComp, SWT.PUSH);
		addFolderButton.setText(Messages.EditMetaDialog_add);
		addFolderButton.setToolTipText(Messages.EditMetaDialog_add_watched_folder);
		addFolderButton.addListener(SWT.Selection, this);
		duplicateFolderButton = new Button(buttonComp, SWT.PUSH);
		duplicateFolderButton.setText(Messages.EditMetaDialog_clone);
		duplicateFolderButton.setToolTipText(Messages.EditMetaDialog_clone_tooltip);
		duplicateFolderButton.addListener(SWT.Selection, this);
		editFolderButton = new Button(buttonComp, SWT.PUSH);
		editFolderButton.setText(Messages.EditMetaDialog_edit_watched);
		editFolderButton.setToolTipText(Messages.EditMetaDialog_edit_watched_tooltip);
		editFolderButton.addListener(SWT.Selection, this);
		removeFolderButton = new Button(buttonComp, SWT.PUSH);
		removeFolderButton.setText(Messages.EditMetaDialog_remove);
		removeFolderButton.setToolTipText(Messages.EditMetaDialog_remove_watched_folder);
		removeFolderButton.addListener(SWT.Selection, this);
		new Label(buttonComp, SWT.SEPARATOR | SWT.HORIZONTAL);
		showLocButton = new Button(buttonComp, SWT.PUSH);
		showLocButton.setText(Messages.EditMetaDialog_show_folder);
		showLocButton.addListener(SWT.Selection, this);
		showDestButton = new Button(buttonComp, SWT.PUSH);
		showDestButton.setText(Messages.EditMetaDialog_show_target);
		showDestButton.addListener(SWT.Selection, this);
		new Label(buttonComp, SWT.SEPARATOR | SWT.HORIZONTAL);
		Composite optionComp = new Composite(buttonComp, SWT.NONE);
		optionComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		optionComp.setLayout(new GridLayout(2, false));
		new Label(optionComp, SWT.NONE).setText(Messages.EditMetaDialog_latency);
		latencyField = new NumericControl(optionComp, SWT.NONE);
		latencyField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		latencyField.setMaximum(60);
		latencyField.setMinimum(1);
		latencyField.setIncrement(1);
		pauseButton = WidgetFactory.createCheckButton(optionComp, Messages.EditMetaDialog_paused,
				new GridData(SWT.BEGINNING, SWT.END, true, false, 2, 1));

		// Footer
		bgimportField = WidgetFactory.createCheckButton(composite, Messages.EditMetaDialog_show_bg_imports,
				new GridData(SWT.BEGINNING, SWT.END, true, false, 2, 1));

		autoWatchButton = WidgetFactory.createCheckButton(composite,
				Messages.EditMetaDialog_automatically_add_imported_folders,
				new GridData(SWT.BEGINNING, SWT.END, true, false, 2, 1));
	}

	private void updateFolderButtons() {
		boolean showFolder = false;
		boolean showDest = false;
		boolean enabled = false;
		WatchedFolder wf = (WatchedFolder) watchedFolderViewer.getStructuredSelection().getFirstElement();
		if (wf != null) {
			enabled = !readonly;
			URI uri = Core.getCore().getVolumeManager().findFile(wf.getUri(), wf.getVolume());
			if (uri != null)
				showFolder = new File(uri).exists();
			if (wf.getTransfer()) {
				String targetDir = wf.getTargetDir();
				showDest = targetDir != null && new File(targetDir).exists();
			}
		}
		showLocButton.setEnabled(showFolder);
		showDestButton.setEnabled(showDest);
		removeFolderButton.setEnabled(enabled);
		editFolderButton.setEnabled(enabled);
		duplicateFolderButton.setEnabled(enabled);
		addFolderButton.setEnabled(!readonly);
	}

	private static void setViewerLayout(ContentViewer viewer, int height, int columns) {
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, columns, 1);
		layoutData.heightHint = height;
		viewer.getControl().setLayoutData(layoutData);
	}

	private TableViewerColumn createColumn(String lab, int w) {
		TableViewerColumn col = new TableViewerColumn(watchedFolderViewer, SWT.NONE);
		col.getColumn().setText(lab);
		col.getColumn().setWidth(w);
		return col;
	}

	private static void createSeparator(Composite composite) {
		new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL)
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
	}

	private static Composite createTabPage(CTabFolder folder, String lab, String descr, int columns, int space) {
		Composite pageComp = createTabPage(folder, lab, descr, columns);
		Label header = new Label(pageComp, SWT.NONE);
		header.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, columns - space, 1));
		header.setText(descr);
		return pageComp;
	}

	protected static Composite createTabPage(CTabFolder folder, String lab, String descr, int columns) {
		Composite pageComp = UiUtilities.createTabPage(folder, lab, descr);
		pageComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(columns, false);
		layout.verticalSpacing = 10;
		pageComp.setLayout(layout);
		return pageComp;
	}

	@SuppressWarnings("unused")
	private static Label createTextField(final Composite parent, String lab, int w, Listener listener,
			String linkLabel) {
		Label label = new Label(parent, SWT.NONE);
		GridData data = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		data.horizontalIndent = 10;
		label.setLayoutData(data);
		label.setText(lab);
		Label textField = new Label(parent, SWT.RIGHT);
		textField.setLayoutData(new GridData(w, SWT.DEFAULT));
		if (listener != null) {
			CLink link = new CLink(parent, SWT.NONE);
			data = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
			data.horizontalIndent = 50;
			link.setLayoutData(data);
			link.setText(linkLabel != null ? linkLabel : Messages.EditMetaDialog_details);
			link.addListener(SWT.Selection, listener);
			if (listener instanceof DetailSelectionAdapter)
				link.setToolTipText(((DetailSelectionAdapter) listener).getLabel());
		} else
			new Label(parent, SWT.NONE);
		return textField;
	}

	private static Label createDetailsField(final Composite parent, int w) {
		Label label = new Label(parent, SWT.NONE);
		GridData data = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		data.horizontalIndent = 10;
		data.widthHint = 250;
		label.setLayoutData(data);
		Label textField = new Label(parent, SWT.RIGHT);
		textField.setLayoutData(new GridData(w, SWT.DEFAULT));
		textField.setData(UiConstants.LABEL, label);
		return textField;
	}

	private void createThumbnailsGroup(final CTabFolder folder) {
		final Composite thComp = createTabPage(folder, Messages.EditMetaDialog_thumbnails,
				Messages.EditMetaDialog_thumbnail_tooltip, 1, 0);

		final CGroup thButtonComp = UiUtilities.createGroup(thComp, 1, Messages.EditMetaDialog_thumbnail_resolution);
		thumbSizeGroup = new RadioButtonGroup(thButtonComp, Messages.EditMetaDialog_high_res_will_increase, 2,
				Messages.EditMetaDialog_low, Messages.EditMetaDialog_medium, Messages.EditMetaDialog_high,
				Messages.EditMetaDialog_very_high);

		// Preview
		final Composite composite = new Composite(thComp, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		new Label(composite, SWT.NONE).setText(Messages.EditMetaDialog_from_preview);
		fromPreviewButton = new Button(composite, SWT.CHECK);

		// Sharpening
		final CGroup shButtonComp = UiUtilities.createGroup(thComp, 8, Messages.EditMetaDialog_sharpen_during_import);
		sharpenButtonGroup = new RadioButtonGroup(shButtonComp, null, SWT.HORIZONTAL,
				Messages.EditMetaDialog_dont_sharpen, Messages.EditMetaDialog_light_sharpen,
				Messages.EditMetaDialog_medium_sharpen, Messages.EditMetaDialog_heavy_sharpen);
		final CGroup comprGroup = CGroup.create(thComp, 1, Messages.EditMetaDialog_compression);
		compressionGroup = new CompressionGroup(comprGroup, true);
	}

	@SuppressWarnings("unused")
	private void createKeywordsGroup(final CTabFolder folder) {
		final Composite kwComp = createTabPage(folder, Messages.EditMetaDialog_keywords,
				Messages.EditMetaDialog_keyword_tooltip, 2, 0);
		CGroup kwGroup = new CGroup(kwComp, SWT.NONE);
		kwGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		kwGroup.setLayout(new GridLayout(2, false));
		kwGroup.setText(Messages.EditMetaDialog_cat_keywords);
		Label catKwLabel = new Label(kwGroup, SWT.NONE);
		catKwLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		catKwLabel.setFont(JFaceResources.getFontRegistry().get(UiConstants.SELECTIONFONT));
		flatKeywordGroup = new FlatGroup(kwGroup, SWT.NONE, settings, "hierarchicalKeywords"); //$NON-NLS-1$
		flatKeywordGroup.setLayoutData(new GridData(SWT.END, SWT.BEGINNING, false, false));
		flatKeywordGroup.addListener(SWT.Selection, this);
		Composite viewerGroup = new Composite(kwGroup, SWT.NONE);
		viewerGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginWidth = 0;
		viewerGroup.setLayout(layout);
		final FilterField filterField = new FilterField(viewerGroup);
		filterField.addListener(SWT.Modify, this);
		excludeButton = WidgetFactory.createCheckButton(viewerGroup, Messages.KeywordGroup_exclude_geographic,
				new GridData(SWT.END, SWT.CENTER, true, false));
		excludeButton.addListener(SWT.Selection, this);
		keywordExpandCollapseGroup = new ExpandCollapseGroup(viewerGroup, SWT.NONE,
				new GridData(SWT.END, SWT.BEGINNING, true, false, 2, 1));
		keywordViewer = new TreeViewer(viewerGroup, SWT.V_SCROLL | SWT.BORDER);
		keywordExpandCollapseGroup.setViewer(keywordViewer);
		keywordExpandCollapseGroup.setVisible(!flatKeywordGroup.isFlat());
		setViewerLayout(keywordViewer, 220, 2);
		keywordViewer.setContentProvider(new KeywordContentProvider());
		keywordViewer.setLabelProvider(new KeywordLabelProvider(getVocabManager(), null));
		ZColumnViewerToolTipSupport.enableFor(keywordViewer);
		keywordViewer.setComparator(ZViewerComparator.INSTANCE);
		UiUtilities.installDoubleClickExpansion(keywordViewer);
		keywordViewer.setFilters(new ViewerFilter[] { new ViewerFilter() {
			@Override
			public boolean select(Viewer aViewer, Object parentElement, Object element) {
				WildCardFilter filter = filterField.getFilter();
				return filter == null || element instanceof Character || filter.accept((String) element);
			}
		} });
		configureKeywordLink = new CLink(viewerGroup, SWT.NONE);
		configureKeywordLink.setText(Messages.EditMetaDialog_configure_keyword_filter);
		configureKeywordLink.addListener(SWT.Selection, this);
		final Composite buttonGroup = new Composite(kwGroup, SWT.NONE);
		buttonGroup.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		buttonGroup.setLayout(new GridLayout());

		keywordAddButton = createPushButton(buttonGroup, Messages.EditMetaDialog_add,
				Messages.EditMetaDialog_add_keyword_tooltip);
		keywordAddButton.addListener(SWT.Selection, this);
		keywordModifyButton = createPushButton(buttonGroup, Messages.EditMetaDialog_edit,
				Messages.EditMetaDialog_modify_keyword_tooltip);
		keywordModifyButton.addListener(SWT.Selection, this);
		addKeyListener(keywordViewer.getControl());
		keywordViewer.addSelectionChangedListener(this);
		keywordReplaceButton = createPushButton(buttonGroup, Messages.EditMetaDialog_replace,
				Messages.EditMetaDialog_replace_tooltip);
		keywordReplaceButton.addListener(SWT.Selection, this);
		keywordDeleteButton = createPushButton(buttonGroup, Messages.EditMetaDialog_delete_keyword,
				Messages.EditMetaDialog_delete_keyword_tooltip);
		keywordDeleteButton.addListener(SWT.Selection, this);
		keywordShowButton = createPushButton(buttonGroup, Messages.EditMetaDialog_show_images,
				Messages.EditMetaDialog_show_keyword_tooltip);
		keywordShowButton.addListener(SWT.Selection, this);
		new Label(buttonGroup, SWT.SEPARATOR | SWT.HORIZONTAL);

		keywordCollectButton = createPushButton(buttonGroup, Messages.EditMetaDialog_collect,
				Messages.EditMetaDialog_collect_keyword_tooltip);
		keywordCollectButton.addListener(SWT.Selection, this);

		keywordLoadButton = createPushButton(buttonGroup, Messages.EditMetaDialog_load,
				NLS.bind(Messages.EditMetaDialog_load_keyword_tooltip, Constants.KEYWORDFILEEXTENSION));
		keywordLoadButton.addListener(SWT.Selection, this);
		keywordSaveButton = createPushButton(buttonGroup, Messages.EditMetaDialog_save,
				NLS.bind(Messages.EditMetaDialog_save_keyword_tooltip, Constants.KEYWORDFILEEXTENSION));
		keywordSaveButton.addListener(SWT.Selection, this);
		addToKeywordsButton = WidgetFactory.createCheckButton(kwGroup, Messages.EditMetaDialog_add_to_keywords,
				new GridData(SWT.BEGINNING, SWT.END, true, false, 2, 1));
		CGroup vocabGroup = new CGroup(kwComp, SWT.NONE);
		vocabGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		vocabGroup.setLayout(new GridLayout(2, false));
		vocabGroup.setText(Messages.EditMetaDialog_controlled_vocabs);
		Composite vocabViewerGroup = new Composite(vocabGroup, SWT.NONE);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.verticalIndent = 20;
		vocabViewerGroup.setLayoutData(layoutData);
		vocabViewerGroup.setLayout(new GridLayout(2, false));

		vocabViewer = new TableViewer(vocabViewerGroup, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		setViewerLayout(vocabViewer, 150, 1);
		TableViewerColumn col1 = new TableViewerColumn(vocabViewer, SWT.NONE);
		col1.getColumn().setWidth(300);
		col1.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return element.toString();
			}

			@Override
			public Image getImage(Object element) {
				if (element instanceof String && !new File((String) element).exists())
					return Icons.error.getImage();
				return null;
			}

			@Override
			public String getToolTipText(Object element) {
				if (element instanceof String && UiActivator.getDefault().getShowHover()) {
					File file = new File((String) element);
					if (!file.exists())
						return Messages.EditMetaDialog_file_does_not_exist;
				}
				return super.getToolTipText(element);
			}

			@Override
			public Image getToolTipImage(Object element) {
				return getImage(element);
			}
		});
		vocabViewer.setContentProvider(ArrayContentProvider.getInstance());
		vocabViewer.addSelectionChangedListener(this);
		vocabViewer.addDoubleClickListener(this);
		ColumnViewerToolTipSupport.enableFor(vocabViewer);
		Composite vocabButtonGroup = new Composite(vocabViewerGroup, SWT.NONE);
		vocabButtonGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		vocabButtonGroup.setLayout(new GridLayout());
		vocabAddButton = createPushButton(vocabButtonGroup, Messages.EditMetaDialog_add,
				Messages.EditMetaDialog_add_vocab);
		vocabAddButton.addListener(SWT.Selection, this);
		vocabRemoveButton = createPushButton(vocabButtonGroup, Messages.EditMetaDialog_remove,
				Messages.EditMetaDialog_remove_vocab);
		vocabRemoveButton.addListener(SWT.Selection, this);
		vocabViewButton = createPushButton(vocabButtonGroup, Messages.EditMetaDialog_view_vocab,
				Messages.EditMetaDialog_view_vocab_tooltip);
		vocabViewButton.addListener(SWT.Selection, this);
		new Label(vocabButtonGroup, SWT.SEPARATOR | SWT.HORIZONTAL);
		vocabEnforceButton = createPushButton(vocabButtonGroup, Messages.EditMetaDialog_enforce,
				Messages.EditMetaDialog_enforce_tooltip);
		vocabEnforceButton.addListener(SWT.Selection, this);
	}

	protected VocabManager getVocabManager() {
		if (vocabManager == null)
			vocabManager = new VocabManager(vocabularies, this);
		return vocabManager;
	}

	protected void updateKeywordViewer() {
		Object[] expandedElements = keywordViewer.getExpandedElements();
		keywordViewer.setInput(keywords);
		keywordViewer.setExpandedElements(expandedElements);
	}

	protected void editKeyword() {
		if (!readonly) {
			IStructuredSelection selection = (IStructuredSelection) keywordViewer.getSelection();
			String kw = (String) selection.getFirstElement();
			if (kw != null) {
				ZInputDialog dialog = createKeywordDialog(Messages.EditMetaDialog_edit_keyword,
						Messages.EditMetaDialog_modify_existing_keyword, kw);
				if (dialog.open() == Window.OK) {
					removeKeywordFromViewer(keywordViewer, kw);
					String newKw = dialog.getValue();
					addKeywordToViewer(keywordViewer, newKw, true);
					todo.add(new ModifyKeywordOperation(kw, newKw));
				}
			}
		}
	}

	private String[] filterKeywords(Collection<String> keywords) {
		if (!excludeGeographic)
			return keywords.toArray(new String[keywords.size()]);
		Set<String> geographic = getGeographicKeywords();
		List<String> filtered = new ArrayList<>(keywords.size());
		for (String kw : keywords)
			if (!geographic.contains(kw))
				filtered.add(kw);
		return filtered.toArray(new String[filtered.size()]);
	}

	public Set<String> getGeographicKeywords() {
		if (geographic == null) {
			geographic = new HashSet<>();
			List<LocationImpl> locations = Core.getCore().getDbManager().obtainObjects(LocationImpl.class);
			for (LocationImpl location : locations)
				Utilities.extractKeywords(location, geographic);
		}
		return geographic;
	}

	private void addKeywordToViewer(TreeViewer viewer, String kw, boolean select) {
		if (keywords.add(kw)) {
			if (flatKeywordGroup.isFlat())
				viewer.add(null, kw);
			else {
				Object[] expandedElements = viewer.getExpandedElements();
				viewer.setInput(keywords);
				viewer.setExpandedElements(expandedElements);
			}
		}
		if (select)
			keywordViewer.setSelection(new StructuredSelection(kw));
	}

	private void removeKeywordFromViewer(TreeViewer viewer, String kw) {
		if (keywords.remove(kw)) {
			if (flatKeywordGroup.isFlat())
				viewer.remove(kw);
			else {
				Object[] expandedElements = viewer.getExpandedElements();
				viewer.setInput(keywords);
				viewer.setExpandedElements(expandedElements);
			}
		}
	}

	private static Button createPushButton(final Composite parent, final String label, String tooltip) {
		Button button = new Button(parent, SWT.PUSH);
		button.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));
		button.setText(label);
		button.setToolTipText(tooltip);
		return button;
	}

	@SuppressWarnings("unused")
	private void createCategoriesGroup(final Composite parent, final CTabFolder folder) {
		final Composite catComposite = createTabPage(folder, Messages.EditMetaDialog_categories,
				Messages.EditMetaDialog_category_tooltip, 2, 0);
		ExpandCollapseGroup expandCollapseGroup = new ExpandCollapseGroup(catComposite, SWT.NONE);
		new Label(catComposite, SWT.NONE);
		catTreeViewer = new TreeViewer(catComposite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.SINGLE);
		expandCollapseGroup.setViewer(catTreeViewer);
		setViewerLayout(catTreeViewer, 200, 1);
		catTreeViewer.setContentProvider(new CategoryContentProvider());
		catTreeViewer.setComparator(ZViewerComparator.INSTANCE);
		catTreeViewer.setLabelProvider(new CategoryLabelProvider());
		UiUtilities.installDoubleClickExpansion(catTreeViewer);
		final Composite buttonGroup = new Composite(catComposite, SWT.NONE);
		buttonGroup.setLayout(new GridLayout());
		buttonGroup.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		categoryAddButton = createPushButton(buttonGroup, Messages.EditMetaDialog_add,
				Messages.EditMetaDialog_add_category);
		categoryAddButton.addListener(SWT.Selection, this);
		categoryRefineButton = createPushButton(buttonGroup, Messages.EditMetaDialog_refine,
				Messages.EditMetaDialog_refine_category);
		categoryRefineButton.addListener(SWT.Selection, this);

		categoryEditButton = createPushButton(buttonGroup, Messages.EditMetaDialog_edit,
				Messages.EditMetaDialog_edit_selected_category);
		categoryEditButton.addListener(SWT.Selection, this);
		addKeyListener(catTreeViewer.getControl());
		catTreeViewer.addSelectionChangedListener(this);
		categoryRemoveButton = createPushButton(buttonGroup, Messages.EditMetaDialog_remove,
				Messages.EditMetaDialog_remove_category);
		categoryRemoveButton.addListener(SWT.Selection, this);

		new Label(buttonGroup, SWT.HORIZONTAL | SWT.SEPARATOR).setText(Messages.EditMetaDialog_label);
		loadCatButton = createPushButton(buttonGroup, Messages.EditMetaDialog_load,
				NLS.bind(Messages.EditMetaDialog_load_category, Constants.CATEGORYFILEEXTENSION));
		loadCatButton.addListener(SWT.Selection, this);
		saveCatButton = createPushButton(buttonGroup, Messages.EditMetaDialog_save,
				NLS.bind(Messages.EditMetaDialog_save_category, Constants.CATEGORYFILEEXTENSION));
		saveCatButton.addListener(SWT.Selection, this);
	}

	private StructGroup createStructComponent(final CTabFolder folder, final int type, String label, String descr,
			String item) {
		return new StructGroup(createTabPage(folder, label, descr, 2, 1), type, item, !readonly);
	}

	protected boolean isElementInUse(IdentifiableObject element) {
		String id = element.getStringId();
		if (element instanceof LocationImpl) {
			if (!dbManager.obtainObjects(LocationCreatedImpl.class, "location", //$NON-NLS-1$
					id, QueryField.EQUALS).isEmpty())
				return true;
			return (!dbManager.obtainObjects(LocationShownImpl.class, "location", id, QueryField.EQUALS) //$NON-NLS-1$
					.isEmpty());
		}
		if (element instanceof ContactImpl)
			return (!dbManager.obtainObjects(CreatorsContactImpl.class, "contact", id, QueryField.EQUALS).isEmpty()); //$NON-NLS-1$
		if (element instanceof ArtworkOrObjectImpl)
			return (!dbManager.obtainObjects(ArtworkOrObjectShownImpl.class, "artworkOrObject", id, QueryField.EQUALS) //$NON-NLS-1$
					.isEmpty());
		return false;
	}

	protected void updateButtons() {
		boolean sel = !catTreeViewer.getSelection().isEmpty();
		categoryRefineButton.setEnabled(sel && !readonly);
		categoryEditButton.setEnabled(sel);
		categoryRemoveButton.setEnabled(sel && !readonly);
		IStructuredSelection kwSelection = (IStructuredSelection) keywordViewer.getSelection();
		boolean kwSel = kwSelection.getFirstElement() instanceof String;
		keywordAddButton.setEnabled(!readonly);
		keywordModifyButton.setEnabled(!readonly && kwSel);
		keywordDeleteButton.setEnabled(!readonly && kwSel);
		keywordReplaceButton.setEnabled(!readonly && kwSelection.size() == 1 && kwSel);
		keywordShowButton.setEnabled(kwSel);
		keywordLoadButton.setEnabled(!readonly);
		categoryAddButton.setEnabled(!readonly);
		categoryEditButton.setEnabled(!readonly);
		categoryRemoveButton.setEnabled(!readonly);
		vocabAddButton.setEnabled(!readonly);
		boolean vocabSel = !vocabViewer.getSelection().isEmpty();
		vocabRemoveButton.setEnabled(vocabSel && !readonly);
		vocabViewButton.setEnabled(vocabSel);
		vocabEnforceButton.setEnabled(vocabularies != null && !vocabularies.isEmpty());
		loadCatButton.setEnabled(!readonly);
		boolean enabled = newDb
				? visited[OVERVIEW] && visited[THUMBNAILS] && visited[CATEGORIES] && visited[KEYWORDS]
						&& visited[INDEXING]
				: true;
		getButton(OK).setEnabled(enabled);
		getShell().setModified(enabled);
		int selectionIndex = tabFolder.getSelectionIndex();
		getButton(PREVIOUS).setEnabled(selectionIndex > WELCOME + 1);
		getButton(NEXT).setEnabled(selectionIndex < tabFolder.getTabList().length - 1);

	}

	private void initHeader() {
		meta = dbManager.getMeta(true);
		readOnlyButton.setSelection(meta.getReadonly());
		fileName.setText(dbManager.getFileName());
		versionLabel.setText(NLS.bind(Messages.EditMetaDialog_catversion, meta.getVersion()));
		SimpleDateFormat sdf = Format.DFDT.get();
		creationDate.setText(sdf.format(meta.getCreationDate()));
		lastImport.setText(sdf.format(meta.getLastImport()));
		seqNoField.setText(String.valueOf(meta.getLastSequenceNo()));
		yearlySeqNoField.setText(String.valueOf(meta.getLastYearSequenceNo()));
		backupField.setText(meta.getBackupLocation());
		updateBackupTooltip();
		String themeId = previousMeta != null ? previousMeta.getThemeID() : meta.getThemeID();
		Theme theme = themeId != null ? CoreActivator.getDefault().getThemes().get(themeId) : null;
		if (theme == null)
			theme = CoreActivator.getDefault().getCurrentTheme();
		themeField.setSelection(new StructuredSelection(theme));
		String language = meta.getLocale();
		if (language != null && languageCombo != null)
			languageCombo.setSelection(new StructuredSelection(language));
	}

	private void initPages(int index) {
		if (index < 0)
			return;
		if (visited[index])
			return;
		visited[index] = true;
		meta = dbManager.getMeta(true);
		if (index == OVERVIEW) {
			ownerInformation.setText(meta.getOwner());
			description.setText(meta.getDescription());
			userFieldLabel1.setText(meta.getUserFieldLabel1());
			userFieldLabel2.setText(meta.getUserFieldLabel2());
			timelineViewer.setSelection(new StructuredSelection(meta.getTimeline()));
			String locationFolders = meta.getLocationFolders();
			locationViewer.setSelection(
					new StructuredSelection(locationFolders == null ? Meta_type.locationFolders_no : locationFolders));
			updateCreateNowButtons();
		} else if (index == THUMBNAILS) {
			fromPreviewButton.setSelection(meta.getThumbnailFromPreview());
			String res = meta.getThumbnailResolution();
			if (res.equals(Meta_type.thumbnailResolution_low))
				thumbSizeGroup.setSelection(0);
			else if (res.equals(Meta_type.thumbnailResolution_medium))
				thumbSizeGroup.setSelection(1);
			else if (res.equals(Meta_type.thumbnailResolution_high))
				thumbSizeGroup.setSelection(2);
			else if (res.equals(Meta_type.thumbnailResolution_veryHigh))
				thumbSizeGroup.setSelection(3);
			switch (meta.getSharpen()) {
			case ImageConstants.SHARPEN_LIGHT:
				sharpenButtonGroup.setSelection(1);
				break;
			case ImageConstants.SHARPEN_MEDIUM:
				sharpenButtonGroup.setSelection(2);
				break;
			case ImageConstants.SHARPEN_HEAVY:
				sharpenButtonGroup.setSelection(3);
				break;
			default:
				sharpenButtonGroup.setSelection(0);
				break;
			}
			compressionGroup.fillValues(meta.getJpegQuality(), meta.getWebpCompression());
		} else if (index == CATEGORIES) {
			if (categories == null)
				categories = Utilities.cloneCategories(meta.getCategory());
			catTreeViewer.setInput(categories);
		} else if (index == KEYWORDS) {
			if (meta.getVocabularies() != null)
				vocabularies.addAll(meta.getVocabularies());
			vocabViewer.setInput(vocabularies);
			keywords = new HashSet<String>(meta.getKeywords());
			keywordViewer.setInput(keywords);
			keywordViewer.expandAll();
			addToKeywordsButton.setSelection(booleanValue(meta.getPersonsToKeywords()));
		} else if (index == INDEXING) {
			noIndexButton.setSelection(meta.getNoIndex());
			for (Algorithm algo : Core.getCore().getDbFactory().getLireService(true).getSupportedSimilarityAlgorithms())
				if (cbirAlgorithms.contains(algo.getName()))
					simViewer.setChecked(algo, true);
			Set<String> indexedTextFields = CoreActivator.getDefault().getIndexedTextFields();
			List<QueryField> fields = new ArrayList<QueryField>(indexedTextFields.size());
			for (String field : indexedTextFields) {
				QueryField qf = QueryField.findQueryField(field);
				if (qf != null) {
					fields.add(qf);
					textIndexViewer.setChecked(qf, true);
				}
			}
			slideTitleButton.setSelection(indexedTextFields.contains(ILuceneService.INDEX_SLIDE_TITLE));
			slideDescrButton.setSelection(indexedTextFields.contains(ILuceneService.INDEX_SLIDE_DESCR));
			exhibitionTitleButton.setSelection(indexedTextFields.contains(ILuceneService.INDEX_EXH_TITLE));
			exhibitionDescrButton.setSelection(indexedTextFields.contains(ILuceneService.INDEX_EXH_DESCR));
			exhibitionCredButton.setSelection(indexedTextFields.contains(ILuceneService.INDEX_EXH_CREDITS));
			webgalleryTitleButton.setSelection(indexedTextFields.contains(ILuceneService.INDEX_WEBGAL_TITLE));
			webgalleryDescrButton.setSelection(indexedTextFields.contains(ILuceneService.INDEX_WEBGAL_DESCR));
			webgalleryAltButton.setSelection(indexedTextFields.contains(ILuceneService.INDEX_WEBGAL_ALT));
			personsButton.setSelection(indexedTextFields.contains(ILuceneService.INDEX_PERSON_SHOWN));
			fileNameButton.setSelection(indexedTextFields.contains(ILuceneService.INDEX_FILENAME));
			updateIndexingControls();
			validateAlgorithms();
		} else if (index == LOCATIONS)
			locationGroup.fillValues();
		else if (index == ARTWORK)
			artworkGroup.fillValues();
		else if (index == CONTACTS)
			contactsGroup.fillValues();
		else if (index == STATISTICS)
			tabFolder.getDisplay().timerExec(100, () -> {
				if (!tabFolder.isDisposed())
					computeStatistics();
			});
		else if (index == WATCHEDFOLDERS) {
			bgimportField.setSelection(meta.getCumulateImports());
			watchedFolders.clear();
			if (meta.getWatchedFolder() != null) {
				CoreActivator activator = CoreActivator.getDefault();
				for (String id : meta.getWatchedFolder()) {
					WatchedFolder folder = activator.getObservedFolder(id);
					if (folder != null)
						watchedFolders.add(folder);
				}
			}
			autoWatchButton.setSelection(meta.getAutoWatch());
			folderBackup = new ArrayList<WatchedFolder>(watchedFolders);
			// make new instances
			watchedFolders.clear();
			for (WatchedFolder f : folderBackup) {
				WatchedFolderImpl newWatchedFolder = new WatchedFolderImpl(f.getUri(), f.getVolume(),
						f.getLastObservation(), f.getRecursive(), f.getFilters(), f.getTransfer(), f.getArtist(),
						f.getSkipDuplicates(), f.getSkipPolicy(), f.getTargetDir(), f.getSubfolderPolicy(),
						f.getSelectedTemplate(), f.getCue(), f.getFileSource(), f.getTethered());
				try {
					newWatchedFolder.setStringId(
							Utilities.computeWatchedFolderId(new File(new URI(f.getUri())), f.getVolume()));
				} catch (URISyntaxException e) {
					// should never happen
				}
				newWatchedFolder.setMeta_parent(((MetaImpl) meta).getStringId());
				watchedFolders.add(newWatchedFolder);
			}
			watchedFolderViewer.setInput(watchedFolders);
			int folderWatchLatency = meta.getFolderWatchLatency();
			if (folderWatchLatency == 0)
				folderWatchLatency = 30;
			latencyField.setSelection(folderWatchLatency);
			pauseButton.setSelection(meta.getPauseFolderWatch());
			updateFolderButtons();
		}
	}

	private static boolean booleanValue(Boolean bool) {
		return bool == null ? false : bool.booleanValue();
	}

	private void updateBackupTooltip() {
		String tooltip;
		if (readOnlyButton.getSelection() && CoreActivator.getDefault().getNoBackup()) {
			tooltip = Messages.EditMetaDialog_backup_disabled;
			backupField.setToolTipText(tooltip);
			backupIntervalLink.setToolTipText(tooltip);
		} else {
			Date lastBackup = meta.getLastBackup();
			if (lastBackup != null) {
				Date nextBackup = new Date(
						lastBackup.getTime() + CoreActivator.getDefault().getBackupInterval() * ONEDAY);
				SimpleDateFormat sdf = Format.DFDT.get();
				tooltip = NLS.bind(Messages.EditMetaDialog_backup_tooltip, sdf.format(lastBackup),
						sdf.format(nextBackup));
				backupField.setToolTipText(tooltip);
				backupIntervalLink.setToolTipText(tooltip);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T getUpdatedValue(IIdentifiableObject obj, QueryField qf, T dflt) {
		Map<QueryField, Object> fieldMap = structOverlayMap.get(obj.getStringId());
		if (fieldMap != null && fieldMap.containsKey(qf))
			return (T) fieldMap.get(qf);
		return dflt;
	}

	@Override
	public boolean close() {
		Job.getJobManager().cancel(this);
		return super.close();
	}

	@Override
	protected void okPressed() {
		locationGroup.saveSettings();
		contactsGroup.saveSettings();
		artworkGroup.saveSettings();
		flatKeywordGroup.saveSettings();
		String ownerInf;
		String descr;
		String uField1;
		String uField2;
		String timelineOption;
		String locationOption;
		if (visited[OVERVIEW]) {
			ownerInf = ownerInformation.getText();
			descr = description.getText();
			uField1 = userFieldLabel1.getText();
			uField2 = userFieldLabel2.getText();
			timelineOption = (String) timelineViewer.getStructuredSelection().getFirstElement();
			if (timelineOption == null)
				timelineOption = Meta_type.timeline_no;
			locationOption = (String) locationViewer.getStructuredSelection().getFirstElement();
			if (locationOption == null)
				locationOption = Meta_type.locationFolders_no;
		} else {
			ownerInf = meta.getOwner();
			descr = meta.getDescription();
			uField1 = meta.getUserFieldLabel1();
			uField2 = meta.getUserFieldLabel2();
			timelineOption = meta.getTimeline();
			locationOption = meta.getLocationFolders();
		}
		String resolution;
		int sharpen;
		int jpegQuality;
		boolean fromPreview;
		boolean webpCompression;
		if (visited[THUMBNAILS]) {
			resolution = Meta_type.thumbnailResolution_medium;
			int selection = thumbSizeGroup.getSelection();
			if (selection == 0)
				resolution = Meta_type.thumbnailResolution_low;
			else if (selection == 2)
				resolution = Meta_type.thumbnailResolution_high;
			else if (selection == 3)
				resolution = Meta_type.thumbnailResolution_veryHigh;
			sharpen = ImageConstants.SHARPEN_NONE;
			selection = sharpenButtonGroup.getSelection();
			if (selection == 1)
				sharpen = ImageConstants.SHARPEN_LIGHT;
			else if (selection == 2)
				sharpen = ImageConstants.SHARPEN_MEDIUM;
			else if (selection == 3)
				sharpen = ImageConstants.SHARPEN_HEAVY;
			jpegQuality = compressionGroup.getJpegQuality();
			webpCompression = compressionGroup.getUseWebp();
			fromPreview = fromPreviewButton.getSelection();
		} else {
			resolution = meta.getThumbnailResolution();
			sharpen = meta.getSharpen();
			jpegQuality = meta.getJpegQuality();
			webpCompression = meta.getWebpCompression();
			fromPreview = meta.getThumbnailFromPreview();
		}
		Map<String, Category> cats = visited[CATEGORIES] ? categories : meta.getCategory();
		boolean addToKeywords;
		Set<String> kWords;
		if (visited[KEYWORDS]) {
			addToKeywords = addToKeywordsButton.getSelection();
			kWords = keywords;
		} else {
			addToKeywords = booleanValue(meta.getPersonsToKeywords());
			kWords = meta.getKeywords();
		}
		Set<String> indexedTextFields;
		boolean noIndex;
		if (visited[INDEXING]) {
			noIndex = noIndexButton.getSelection();
			updateSelectedAlgorithms();
			cbirAlgorithms.add(""); // indicate that it has been set //$NON-NLS-1$
			indexedTextFields = new HashSet<String>();
			for (Object el : textIndexViewer.getCheckedElements()) {
				String key = ((QueryField) el).getKey();
				if (key != null)
					indexedTextFields.add(key);
			}
			if (slideTitleButton.getSelection())
				indexedTextFields.add(ILuceneService.INDEX_SLIDE_TITLE);
			if (slideDescrButton.getSelection())
				indexedTextFields.add(ILuceneService.INDEX_SLIDE_DESCR);
			if (exhibitionTitleButton.getSelection())
				indexedTextFields.add(ILuceneService.INDEX_EXH_TITLE);
			if (exhibitionDescrButton.getSelection())
				indexedTextFields.add(ILuceneService.INDEX_EXH_DESCR);
			if (exhibitionCredButton.getSelection())
				indexedTextFields.add(ILuceneService.INDEX_EXH_CREDITS);
			if (webgalleryTitleButton.getSelection())
				indexedTextFields.add(ILuceneService.INDEX_WEBGAL_TITLE);
			if (webgalleryDescrButton.getSelection())
				indexedTextFields.add(ILuceneService.INDEX_WEBGAL_DESCR);
			if (webgalleryAltButton.getSelection())
				indexedTextFields.add(ILuceneService.INDEX_WEBGAL_ALT);
			if (personsButton.getSelection())
				indexedTextFields.add(ILuceneService.INDEX_PERSON_SHOWN);
			if (fileNameButton.getSelection())
				indexedTextFields.add(ILuceneService.INDEX_FILENAME);
			indexedTextFields.add(""); // indicate that it has //$NON-NLS-1$
										// been set
		} else {
			noIndex = meta.getNoIndex();
			cbirAlgorithms = meta.getCbirAlgorithms();
			indexedTextFields = meta.getIndexedTextFields();
		}
		List<WatchedFolder> wFolders;
		List<WatchedFolder> fBackup;
		int latency;
		boolean autoWatch;
		boolean pause;
		boolean bgimport;
		if (visited[WATCHEDFOLDERS]) {
			fBackup = folderBackup;
			wFolders = watchedFolders;
			latency = latencyField.getSelection();
			pause = pauseButton.getSelection();
			autoWatch = autoWatchButton.getSelection();
			bgimport = bgimportField.getSelection();
		} else {
			wFolders = null;
			fBackup = null;
			latency = meta.getFolderWatchLatency();
			pause = meta.getPauseFolderWatch();
			autoWatch = meta.getAutoWatch();
			bgimport = meta.getCumulateImports();
		}
		String language = languageCombo == null ? null
				: (String) languageCombo.getStructuredSelection().getFirstElement();
		Theme theme = (Theme) themeField.getStructuredSelection().getFirstElement();
		ModifyMetaOperation op = new ModifyMetaOperation(meta, newDb, structOverlayMap, newStructMap,
				backupField.getText(), ownerInf, theme == null ? null : theme.getId(), descr, uField1, uField2,
				bgimport, timelineOption, locationOption, kWords, cats, resolution, fromPreview, fBackup, wFolders,
				latency, pause, readonly, autoWatch, sharpen, webpCompression, jpegQuality, noIndex, language,
				cbirAlgorithms, indexedTextFields, addToKeywords, categoryChanges, vocabularies);
		OperationJob.executeOperation(op, workbenchPage.getActivePart());
		for (DbOperation operation : todo)
			OperationJob.executeOperation(operation, workbenchPage.getActivePart());
		super.okPressed();
	}

	protected void updateSelectedAlgorithms() {
		cbirAlgorithms.clear();
		for (Object el : simViewer.getCheckedElements())
			cbirAlgorithms.add(((Algorithm) el).getName());
	}

	private void editCategory(final Shell shell) {
		if (!readonly) {
			Category firstElement = (Category) ((IStructuredSelection) catTreeViewer.getSelection()).getFirstElement();
			EditCategoryDialog inputDialog = new EditCategoryDialog(shell, firstElement, categories, null,
					firstElement.getCategory_subCategory_parent(), false);
			if (inputDialog.open() == Window.OK) {
				String label = inputDialog.getLabel();
				firstElement.setSynonyms(inputDialog.getSynonyms());
				if (inputDialog.getApply()) {
					categoryChanges.add(firstElement.getLabel());
					categoryChanges.add(label);
				}
				Category subCategory_parent = firstElement.getCategory_subCategory_parent();
				if (subCategory_parent != null) {
					subCategory_parent.removeSubCategory(firstElement.getLabel());
					firstElement.setLabel(label);
					subCategory_parent.putSubCategory(firstElement);
				} else {
					categories.remove(firstElement.getLabel());
					firstElement.setLabel(label);
					categories.put(firstElement.getLabel(), firstElement);
				}
				catTreeViewer.update(firstElement, null);
			}
		}
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, PREVIOUS, Messages.EditMetaDialog_previous, false);
		createButton(parent, NEXT, Messages.EditMetaDialog_next, true);
		super.createButtonsForButtonBar(parent);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
		case PREVIOUS:
			int selectionIndex = tabFolder.getSelectionIndex();
			if (selectionIndex > WELCOME + 1) {
				tabFolder.setSelection(selectionIndex - 1);
				tabChanged();
			}
			return;
		case NEXT:
			selectionIndex = tabFolder.getSelectionIndex();
			tabFolder.setSelection(selectionIndex + 1);
			tabChanged();
			return;
		}
		super.buttonPressed(buttonId);
	}

	private ZInputDialog createKeywordDialog(String title, String message, String dflt) {
		return new ZInputDialog(getShell(), title, message, dflt, this, this, false);
	}

	public String isValid(String newText) {
		if (newText.isEmpty())
			return Messages.EditMetaDialog_please_enter_a_keyword;
		if (keywords.contains(newText))
			return Messages.EditMetaDialog_keyword_already_exsists;
		return null;
	}

	public String getAdvice(String newText) {
		int l = newText.length();
		int size = keywords.size();
		if (l > 0 && size > 0) {
			final String[] kws = keywords.toArray(new String[size]);
			Utilities.sortForSimilarity(kws, newText);
			StringBuilder sb = new StringBuilder();
			int i = 0;
			for (String kw : kws) {
				int ld = Utilities.LD(newText, kw);
				if (2 * ld > l || (3 * ld + 2 > l && l >= 5))
					break;
				if (++i > 3)
					break;
				if (sb.length() > 0)
					sb.append(", "); //$NON-NLS-1$
				sb.append(kw);
			}
			if (sb.length() > 0)
				return NLS.bind(Messages.EditMetaDialog_similar_keywords, sb);
		}
		return null;
	}

	public void setInitialPage(int page) {
		this.initialPage = page;
	}

	public void editWatchedFolder() {
		WatchedFolderWizard wizard = new WatchedFolderWizard(true, true, true);
		WizardDialog wizardDialog = new WizardDialog(getShell(), wizard);
		wizard.init(null, watchedFolderViewer.getStructuredSelection());
		if (wizardDialog.open() == WizardDialog.OK)
			watchedFolderViewer.update(wizard.getResult(), null);
	}

	public Map<String, Category> getCategories() {
		return categories;
	}

	public void setCategories(Map<String, Category> categories) {
		this.categories = categories;
	}

	protected void tabChanged() {
		cntrlDwn = false;
		int tabIndex = tabFolder.getSelectionIndex();
		if (newDb) {
			if (initButtonGroup != null && initButtonGroup.getSelection() == 1 && initButtonGroup.isEnabled(1)) {
				copyMeta();
				categories = meta.getCategory();
				initHeader();
			}
			initPages(tabIndex);
			if (initButtonGroup != null)
				initButtonGroup.setEnabled(false);
			if (tabIndex == CATEGORIES) {
				Theme newTheme = (Theme) themeField.getStructuredSelection().getFirstElement();
				boolean themeChanged = newTheme != theme;
				theme = newTheme;
				if (meta.getCategory() == null || meta.getCategory().isEmpty()) {
					InputStream catin = null;
					try {
						if (!themeChanged) {
							Map<String, Category> oldCategories = previousMeta.getCategory();
							if (oldCategories != null && !oldCategories.isEmpty()) {
								ByteArrayOutputStream out = new ByteArrayOutputStream();
								Utilities.saveCategories(previousMeta, out);
								catin = new ByteArrayInputStream(out.toByteArray());
							}
						}
						if (catin == null)
							catin = Utilities.openPropertyFile(theme.getCategories());
						if (catin != null) {
							categories = new HashMap<>();
							Utilities.loadCategories(dbManager, categories, catin, null);
							catTreeViewer.setInput(categories);
						}
					} finally {
						try {
							if (catin != null)
								catin.close();
						} catch (IOException e1) {
							// do nothing
						}
					}
				}
				if (meta.getKeywords() == null || meta.getKeywords().isEmpty()) {
					InputStream kwin = null;
					try {
						if (!themeChanged) {
							Set<String> oldKeywords = previousMeta.getKeywords();
							if (oldKeywords != null && !oldKeywords.isEmpty()) {
								ByteArrayOutputStream out = new ByteArrayOutputStream();
								Utilities.saveKeywords(oldKeywords, out);
								kwin = new ByteArrayInputStream(out.toByteArray());
							}
						}
						if (kwin == null)
							kwin = Utilities.openPropertyFile(theme.getKeywords());
						if (kwin != null) {
							List<String> loadedKeywords = Utilities.loadKeywords(kwin);
							if (loadedKeywords != null)
								keywords = new HashSet<String>(loadedKeywords);
							keywordViewer.setInput(keywords);
							keywordViewer.expandAll();
						}
					} finally {
						try {
							if (kwin != null)
								kwin.close();
						} catch (IOException e1) {
							// do nothing
						}
					}
				}
			}
		} else
			initPages(tabIndex);
		updateButtons();
		updateTabItems();
	}

	private void copyMeta() {
		if (previousMeta != null) {
			List<Object> toBeStored = new ArrayList<Object>();
			Meta newMeta = dbManager.getMeta(true);
			toBeStored.add(newMeta);
			CoreActivator.getDefault().copyMeta(previousMeta, dbManager.getFile(), toBeStored, newMeta);
			dbManager.safeTransaction(null, toBeStored);
		}
	}

	protected void viewVocab() {
		List<String> selected = new ArrayList<>();
		for (@SuppressWarnings("unchecked")
		Iterator<Object> it = vocabViewer.getStructuredSelection().iterator(); it.hasNext();)
			selected.add((String) it.next());
		if (!selected.isEmpty())
			new ViewVocabDialog(getShell(), new VocabManager(selected, EditMetaDialog.this).getVocabTree(),
					selected.size() != 1 ? null : new File(selected.get(0)), false).open();
	}

	@Override
	public void handleEvent(Event e) {
		switch (e.type) {
		case SWT.KeyDown:
			if (e.keyCode == SWT.CTRL)
				cntrlDwn = true;
			return;
		case SWT.KeyUp:
			if (e.keyCode == SWT.CTRL)
				cntrlDwn = false;
			return;
		case SWT.Modify:
			keywordViewer.setInput(keywords);
			keywordViewer.expandAll();
			return;
		case SWT.Selection:
			Widget widget = e.widget;
			if (widget == noIndexButton)
				updateIndexingControls();
			else if (widget == algoButton) {
				essentialAlgos = !essentialAlgos;
				algoButton.setText(
						essentialAlgos ? Messages.EditMetaDialog_show_more : Messages.EditMetaDialog_show_less);
				updateSelectedAlgorithms();
				Object[] checkedElements = simViewer.getCheckedElements();
				simViewer.setInput(simViewer.getInput());
				simViewer.setCheckedElements(checkedElements);
				simGroup.layout(true, true);
			} else if (widget == addFolderButton || widget == duplicateFolderButton) {
				WatchedFolder folder;
				if (widget == addFolderButton)
					folder = new WatchedFolderImpl(null, null, 0L, true, null, false, null, false, 0, null, 2, null,
							null, Constants.FILESOURCE_DIGITAL_CAMERA, false);
				else {
					WatchedFolder orig = (WatchedFolder) watchedFolderViewer.getStructuredSelection().getFirstElement();
					folder = new WatchedFolderImpl(null, null, 0L, orig.getRecursive(), orig.getFilters(),
							orig.getTransfer(), orig.getArtist(), orig.getSkipDuplicates(), orig.getSkipPolicy(),
							orig.getTargetDir(), orig.getSubfolderPolicy(), orig.getSelectedTemplate(), orig.getCue(),
							orig.getFileSource(), orig.getTethered());
				}
				WatchedFolderWizard wizard = new WatchedFolderWizard(true, true, true);
				WizardDialog wizardDialog = new WizardDialog(getShell(), wizard);
				wizard.init(null, new StructuredSelection(folder));
				if (wizardDialog.open() == WizardDialog.OK) {
					try {
						folder.setStringId(Utilities.computeWatchedFolderId(new File(new URI(folder.getUri())),
								folder.getVolume()));
						watchedFolders.add(folder);
						watchedFolderViewer.setInput(watchedFolders);
					} catch (URISyntaxException e1) {
						// should never happend
					}
				}
			} else if (widget == editFolderButton)
				editWatchedFolder();
			else if (widget == removeFolderButton) {
				WatchedFolderImpl firstElement = (WatchedFolderImpl) watchedFolderViewer.getStructuredSelection()
						.getFirstElement();
				watchedFolders.remove(firstElement);
				watchedFolderViewer.remove(firstElement);
			} else if (widget == showLocButton) {
				WatchedFolder wf = (WatchedFolder) watchedFolderViewer.getStructuredSelection().getFirstElement();
				if (wf != null) {
					URI uri = Core.getCore().getVolumeManager().findFile(wf.getUri(), wf.getVolume());
					if (uri != null) {
						File file = new File(uri);
						if (file.exists())
							BatchUtilities.showInFolder(file, false);
					}
				}
			} else if (widget == showDestButton) {
				WatchedFolder wf = (WatchedFolder) watchedFolderViewer.getStructuredSelection().getFirstElement();
				if (wf != null && wf.getTransfer()) {
					String targetDir = wf.getTargetDir();
					if (targetDir != null) {
						File file = new File(targetDir);
						if (file.exists())
							BatchUtilities.showInFolder(file, false);
					}
				}
			} else if (widget == createTimeLineButton) {
				OperationJob.executeOperation(
						new CreateTimelineOperation((String) timelineViewer.getStructuredSelection().getFirstElement()),
						EditMetaDialog.this);
			} else if (widget == createLocationFoldersButton) {
				OperationJob.executeOperation(
						new CreateLocationFolderOperation(
								(String) locationViewer.getStructuredSelection().getFirstElement()),
						EditMetaDialog.this);
			} else if (widget == tabFolder)
				tabChanged();
			else if (widget == fileName)
				BatchUtilities.showInFolder(new File(fileName.getText()), true);
			else if (widget == istallLink) {
				try {
					String language = Locale.getDefault().getLanguage();
					String url = System.getProperty("com.bdaum.zoom.dictionaries." + language); //$NON-NLS-1$
					if (url == null)
						url = System.getProperty("com.bdaum.zoom.dictionaries"); //$NON-NLS-1$
					PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(url));
				} catch (PartInitException e1) {
					// do nothing
				} catch (MalformedURLException e1) {
					// should never happen
				}
			} else if (widget == readOnlyButton) {
				readonly = readOnlyButton.getSelection();
				updateFolderButtons();
				updateButtons();
				updateFields();
				updateBackupTooltip();
			} else if (widget == lastImport) {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (window != null) {
					LastImportCommand command = new LastImportCommand();
					command.init(window);
					command.run();
				}
			} else if (widget == backupIntervalLink) {
				PreferencesUtil.createPreferenceDialogOn(getShell(), GeneralPreferencePage.ID, new String[0], "backup") //$NON-NLS-1$
						.open();
				updateBackupTooltip();
			} else if (widget == flatKeywordGroup) {
				keywordViewer.setInput(keywords);
				keywordViewer.expandAll();
				keywordExpandCollapseGroup.setVisible(!flatKeywordGroup.isFlat());
			} else if (widget == excludeButton) {
				excludeGeographic = excludeButton.getSelection();
				Object[] expandedElements = keywordViewer.getExpandedElements();
				keywordViewer.setInput(keywords);
				keywordViewer.setExpandedElements(expandedElements);
			} else if (widget == configureKeywordLink)
				PreferencesUtil.createPreferenceDialogOn(getShell(), KeyPreferencePage.ID, new String[0], null).open();
			else if (widget == keywordAddButton) {
				ZInputDialog dialog = createKeywordDialog(Messages.EditMetaDialog_add_keyword,
						Messages.EditMetaDialog_enter_a_new_keyword, ""); //$NON-NLS-1$
				if (dialog.open() == Window.OK)
					addKeywordToViewer(keywordViewer, dialog.getValue(), true);
			} else if (widget == keywordModifyButton)
				editKeyword();
			else if (widget == keywordReplaceButton) {
				String kw = (String) ((IStructuredSelection) keywordViewer.getSelection()).getFirstElement();
				if (kw != null) {
					KeywordDialog dialog = new KeywordDialog(getShell(),
							NLS.bind(Messages.EditMetaDialog_replace_keyword, kw), null, keywords, null);
					if (dialog.open() == Window.OK) {
						BagChange<String> result = dialog.getResult();
						boolean found = false;
						Set<String> added = result.getAdded();
						if (added != null)
							for (String s : added)
								if (kw.equals(s)) {
									found = true;
									break;
								}
						if (!found)
							removeKeywordFromViewer(keywordViewer, kw);
						Set<String> addedKeywords = result.getAdded();
						String[] replacement = addedKeywords.toArray(new String[addedKeywords.size()]);
						int i = 0;
						for (String k : replacement)
							addKeywordToViewer(keywordViewer, k, i++ == 0);
						todo.add(new ReplaceKeywordOperation(kw, replacement));
					}
				}
			} else if (widget == keywordDeleteButton) {
				BusyIndicator.showWhile(keywordDeleteButton.getDisplay(), () -> {
					IStructuredSelection selection = (IStructuredSelection) keywordViewer.getSelection();
					String kw = (String) selection.getFirstElement();
					if (kw != null) {
						List<AssetImpl> set = dbManager.obtainObjects(AssetImpl.class,
								QueryField.IPTC_KEYWORDS.getKey(), kw, QueryField.EQUALS);
						removeKeywordFromViewer(keywordViewer, kw);
						if (!set.isEmpty()) {
							KeywordDeleteDialog dialog = new KeywordDeleteDialog(getShell(), kw, set);
							if (dialog.open() != Window.OK) {
								keywords.add(kw);
								return;
							}
							if (dialog.getPolicy() == KeywordDeleteDialog.REMOVE)
								todo.add(new ManageKeywordsOperation(
										new BagChange<String>(null, null, Collections.singleton(kw), null), set));
						}
					}
				});
			} else if (widget == keywordShowButton) {
				IStructuredSelection selection = (IStructuredSelection) keywordViewer.getSelection();
				String kw = (String) selection.getFirstElement();
				if (kw != null) {
					SmartCollectionImpl sm = new SmartCollectionImpl(kw, false, false, true, false, null, 0, null, 0,
							null, Constants.INHERIT_LABEL, null, 0, 1, null);
					sm.addCriterion(new CriterionImpl(QueryField.IPTC_KEYWORDS.getKey(), null, kw, null,
							QueryField.EQUALS, false));
					sm.addSortCriterion(new SortCriterionImpl(QueryField.IPTC_DATECREATED.getKey(), null, true));
					Ui.getUi().getNavigationHistory(workbenchPage.getWorkbenchWindow())
							.postSelection(new StructuredSelection(sm));
				}
				close();
			} else if (widget == keywordCollectButton) {
				KeywordCollectDialog dialog = new KeywordCollectDialog(getShell(), keywords);
				if (dialog.open() == Window.OK) {
					int i = 0;
					for (String kw : dialog.getToAdd())
						addKeywordToViewer(keywordViewer, kw, i++ == 0);
					for (String kw : dialog.getToRemove())
						removeKeywordFromViewer(keywordViewer, kw);
				}
			} else if (widget == keywordLoadButton) {
				FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
				dialog.setFilterExtensions(KEYWORDEXTENSIONS);
				dialog.setFilterNames(new String[] { Constants.APPNAME + Messages.EditMetaDialog_zoom_keyword_file
						+ Constants.KEYWORDFILEEXTENSION + ')', Messages.EditMetaDialog_all_files });
				String filename = dialog.open();
				if (filename != null) {
					try (InputStream in = new BufferedInputStream(new FileInputStream(filename))) {
						List<String> list = Utilities.loadKeywords(in);
						keywords.clear();
						keywords.addAll(list);
					} catch (IOException ex) {
						// ignore
					}
					keywordViewer.setInput(keywords);
					keywordViewer.expandAll();
				}
			} else if (widget == keywordSaveButton) {
				FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
				dialog.setFilterExtensions(KEYWORDEXTENSIONS);
				dialog.setFilterNames(new String[] {
						Constants.APPNAME + Messages.EditMetaDialog_zoom_keyword_file + Constants.KEYWORDFILEEXTENSION
								+ ")", //$NON-NLS-1$
						Messages.EditMetaDialog_all_files });
				dialog.setFileName("*" + Constants.KEYWORDFILEEXTENSION); //$NON-NLS-1$
				dialog.setOverwrite(true);
				String filename = dialog.open();
				if (filename != null)
					Utilities.saveKeywords(keywords, new File(filename));
			} else if (widget == vocabAddButton) {
				FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
				dialog.setText(Messages.EditMetaDialog_select_vocab);
				dialog.setFilterExtensions(KEYWORDEXTENSIONS);
				dialog.setFilterNames(new String[] { Constants.APPNAME + Messages.EditMetaDialog_zoom_keyword_file
						+ Constants.KEYWORDFILEEXTENSION + ')', Messages.EditMetaDialog_all_files });
				String file = dialog.open();
				if (file != null) {
					boolean found = false;
					for (String s : vocabularies)
						if (s.equals(file)) {
							found = true;
							break;
						}
					if (!found) {
						vocabularies.add(file);
						vocabViewer.add(file);
					}
					vocabViewer.setSelection(new StructuredSelection(file), true);
					if (vocabManager != null)
						vocabManager.reset(vocabularies);
					updateKeywordViewer();
				}
			} else if (widget == vocabRemoveButton) {
				Iterator<?> it = vocabViewer.getStructuredSelection().iterator();
				while (it.hasNext()) {
					Object file = it.next();
					vocabularies.remove(file);
					vocabViewer.remove(file);
				}
				if (vocabManager != null)
					vocabManager.reset(vocabularies);
				updateKeywordViewer();
			} else if (widget == vocabViewButton) {
				viewVocab();
			} else if (widget == vocabEnforceButton) {
				List<String[]> changes = new ArrayList<>();
				VocabManager vManager = getVocabManager();
				for (String kw : keywords) {
					String mapped = vManager.getVocab(kw);
					if (!kw.equals(mapped))
						changes.add(new String[] { kw, mapped });
				}
				VocabEnforceDialog dialog = new VocabEnforceDialog(getShell(), changes);
				if (dialog.open() == VocabEnforceDialog.OK) {
					BusyIndicator.showWhile(getShell().getDisplay(), () -> {
						int policy = dialog.getPolicy();
						for (String[] change : dialog.getChanges()) {
							String kw = change[0];
							keywords.remove(kw);
							if (change[1] != null) {
								keywords.add(change[1]);
								if (policy == KeywordDeleteDialog.REMOVE)
									todo.add(new ReplaceKeywordOperation(kw, new String[] { change[1] }));
							} else if (policy == KeywordDeleteDialog.REMOVE)
								todo.add(new ManageKeywordsOperation(
										new BagChange<String>(null, null, Collections.singleton(kw), change), null));
							updateKeywordViewer();
						}

					});
				}
			} else if (widget == categoryAddButton) {
				EditCategoryDialog inputDialog = new EditCategoryDialog(tabFolder.getShell(), null, categories, null,
						null, false);
				if (inputDialog.open() == Window.OK) {
					String label = inputDialog.getLabel();
					Category category = new CategoryImpl(label);
					category.setSynonyms(inputDialog.getSynonyms());
					categories.put(label, category);
					catTreeViewer.setInput(categories);
				}
			} else if (widget == categoryRefineButton) {
				Category firstElement = (Category) ((IStructuredSelection) catTreeViewer.getSelection())
						.getFirstElement();
				EditCategoryDialog inputDialog = new EditCategoryDialog(tabFolder.getShell(), null, categories, null,
						firstElement, false);
				if (inputDialog.open() == Window.OK) {
					Category subCategory = new CategoryImpl(inputDialog.getLabel());
					subCategory.setSynonyms(inputDialog.getSynonyms());
					firstElement.putSubCategory(subCategory);
					catTreeViewer.add(firstElement, subCategory);
					catTreeViewer.expandToLevel(firstElement, 2);
				}
			} else if (widget == categoryEditButton)
				editCategory(tabFolder.getShell());
			else if (widget == categoryRemoveButton) {
				CategoryImpl firstElement = (CategoryImpl) ((IStructuredSelection) catTreeViewer.getSelection())
						.getFirstElement();
				Category subCategory_parent = firstElement.getCategory_subCategory_parent();
				if (subCategory_parent != null)
					subCategory_parent.removeSubCategory(firstElement.getLabel());
				else
					categories.remove(firstElement.getLabel());
				catTreeViewer.remove(firstElement);
			} else if (widget == loadCatButton) {
				FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
				dialog.setFilterExtensions(CATEXTENSIONS);
				dialog.setFilterNames(new String[] { Constants.APPNAME + Messages.EditMetaDialog_zoom_cat_file
						+ Constants.CATEGORYFILEEXTENSION + ")" }); //$NON-NLS-1$
				String filename = dialog.open();
				if (filename != null)
					try (InputStream in = new BufferedInputStream(new FileInputStream(filename))) {
						Utilities.loadCategories(dbManager, categories, in, null);
					} catch (IOException e1) {
						// ignore
					}
			} else if (widget == saveCatButton) {
				FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
				dialog.setFilterExtensions(CATEXTENSIONS);
				dialog.setFilterNames(new String[] { Constants.APPNAME + Messages.EditMetaDialog_zoom_cat_file
						+ Constants.CATEGORYFILEEXTENSION + ")" }); //$NON-NLS-1$
				dialog.setFileName("*" + Constants.CATEGORYFILEEXTENSION); //$NON-NLS-1$
				dialog.setOverwrite(true);
				String filename = dialog.open();
				if (filename != null)
					Utilities.saveCategories(meta, new File(filename));
			} else {
				boolean enabled = getButton(OK).getEnabled();
				getButton(OK).setEnabled(false);
				getButton(CANCEL).setEnabled(false);
				getButton(PREVIOUS).setEnabled(false);
				getButton(NEXT).setEnabled(false);
				askForDefrag();
				getButton(OK).setEnabled(enabled);
				getButton(CANCEL).setEnabled(true);
				getButton(PREVIOUS).setEnabled(true);
				getButton(NEXT).setEnabled(true);
			}
		}
	}

	private void askForDefrag() {
		if (AcousticMessageDialog.openQuestion(getShell(), Messages.EditMetaDialog_cat_maintenance,
				Messages.EditMetaDialog_defrag_continue)) {
			BusyIndicator.showWhile(freeSegmentsField.getDisplay(), () -> {
				try {
					Job.getJobManager().join(DETAILS, null);
				} catch (OperationCanceledException | InterruptedException e1) {
					// ignore
				}
			});
			File file = dbManager.getFile();
			BatchActivator.setFastExit(false);
			UiActivator ui = UiActivator.getDefault();
			if (ui.preCatClose(CatalogListener.TUNE, Messages.EditMetaDialog_closing_for_defrag,
					Messages.EditMetaDialog_operations_running, false)) {
				CoreActivator core = CoreActivator.getDefault();
				dbManager = core.openDatabase(file.getAbsolutePath());
				core.setCatFile(file);
				core.fireCatalogOpened(false);
				ui.postCatOpen();
				ui.postCatInit(false);
				computeStatistics();
			}
			BatchActivator.setFastExit(true);
		}
	}

	private void addKeyListener(Control control) {
		control.addListener(SWT.KeyDown, this);
		control.addListener(SWT.KeyUp, this);
	}

	@Override
	public void checkStateChanged(CheckStateChangedEvent event) {
		validateAlgorithms();
	}

	@Override
	public void doubleClick(DoubleClickEvent event) {
		if (event.getSource() == vocabViewer)
			viewVocab();
		else if (!cntrlDwn && editFolderButton.isEnabled())
			editWatchedFolder();
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		Object source = event.getSource();
		if (source == catTreeViewer) {
			updateButtons();
			if (cntrlDwn) {
				if (categoryEditButton.isEnabled())
					editCategory(tabFolder.getShell());
				cntrlDwn = false;
			}
		} else if (source == keywordViewer) {
			updateButtons();
			if (cntrlDwn) {
				if (keywordModifyButton.isEnabled())
					editKeyword();
				cntrlDwn = false;
			}
		} else if (source == watchedFolderViewer) {
			updateFolderButtons();
			if (cntrlDwn) {
				if (editFolderButton.isEnabled())
					editWatchedFolder();
				cntrlDwn = false;
			}
		} else if (source == vocabViewer) {
			updateButtons();
		} else
			updateCreateNowButtons();
	}

}
