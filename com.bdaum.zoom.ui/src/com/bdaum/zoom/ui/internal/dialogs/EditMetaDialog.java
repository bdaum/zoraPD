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
 * (c) 2009-2017 Berthold Daum  (berthold.daum@bdaum.de)
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
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
import org.eclipse.jface.viewers.ViewerComparator;
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
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
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
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.LocationConstants;
import com.bdaum.zoom.core.internal.Theme;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.core.internal.lire.Algorithm;
import com.bdaum.zoom.core.internal.lucene.ILuceneService;
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
import com.bdaum.zoom.ui.ILocationDisplay;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.dialogs.ZInputDialog;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiConstants;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.VocabManager;
import com.bdaum.zoom.ui.internal.VocabManager.VocabNode;
import com.bdaum.zoom.ui.internal.commands.LastImportCommand;
import com.bdaum.zoom.ui.internal.operations.ModifyMetaOperation;
import com.bdaum.zoom.ui.internal.preferences.GeneralPreferencePage;
import com.bdaum.zoom.ui.internal.preferences.KeyPreferencePage;
import com.bdaum.zoom.ui.internal.views.AbstractPropertiesView.ViewComparator;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.CheckedText;
import com.bdaum.zoom.ui.internal.widgets.CompressionGroup;
import com.bdaum.zoom.ui.internal.widgets.ExpandCollapseGroup;
import com.bdaum.zoom.ui.internal.widgets.FilterField;
import com.bdaum.zoom.ui.internal.widgets.FlatGroup;
import com.bdaum.zoom.ui.internal.widgets.IInputAdvisor;
import com.bdaum.zoom.ui.internal.widgets.RadioButtonGroup;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.internal.wizards.ImportFileSelectionPage;
import com.bdaum.zoom.ui.internal.wizards.WatchedFolderWizard;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.bdaum.zoom.ui.widgets.CLink;
import com.bdaum.zoom.ui.widgets.NumericControl;

@SuppressWarnings("restriction")
public class EditMetaDialog extends ZTitleAreaDialog {

	public class DetailSelectionAdapter extends SelectionAdapter {

		private final Class<? extends IIdentifiableObject> clazz;
		private final String label;

		public DetailSelectionAdapter(Class<? extends IIdentifiableObject> clazz, String label) {
			this.clazz = clazz;
			this.label = label;
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			Job.getJobManager().cancel(EditMetaDialog.this);
			detailsGroup.setText(label);
			detailsGroup.setVisible(true);
			new DetailsJob(clazz).schedule();
		}

	}

	private static final String LABELKEY = "label"; //$NON-NLS-1$

	public class DetailsJob extends Job {

		private final Class<? extends IIdentifiableObject> clazz;

		public DetailsJob(Class<? extends IIdentifiableObject> clazz) {
			super(Messages.EditMetaDialog_collecting_details);
			this.clazz = clazz;
			setSystem(true);
			setPriority(Job.INTERACTIVE);
		}

		@Override
		public boolean belongsTo(Object family) {
			return family == EditMetaDialog.this;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if (clazz == GroupImpl.class) {
				List<GroupImpl> groups = dbManager.obtainObjects(GroupImpl.class);
				final Shell shell = getShell();
				int work = groups.size();
				int incr = Math.max(1, (work + 15) / 16);
				init(shell, work);
				int main = 0;
				int subgroup = 0;
				int system = 0;
				int user = 0;
				int i = 0;
				for (GroupImpl group : groups) {
					if (group.getGroup_subgroup_parent() == null)
						++main;
					else
						++subgroup;
					if (group.getSystem())
						++system;
					else
						++user;
					if (updateProgressBar(shell, ++i, incr, monitor))
						return Status.CANCEL_STATUS;
				}
				final int main1 = main;
				final int subgroup1 = subgroup;
				final int system1 = system;
				final int user1 = user;
				if (!shell.isDisposed()) {
					shell.getDisplay().asyncExec(new Runnable() {
						public void run() {
							if (!shell.isDisposed()) {
								detailsProgressBar.setVisible(false);
								setDetails(0, Messages.EditMetaDialog_main_groups, main1);
								setDetails(1, Messages.EditMetaDialog_subgroups, subgroup1);
								setDetails(2, Messages.EditMetaDialog_system_groups, system1);
								setDetails(3, Messages.EditMetaDialog_user_groups, user1);
							}
						}
					});

				}
			} else if (clazz == SmartCollectionImpl.class) {
				int main = 0;
				int subCollection = 0;
				int system = 0;
				int user = 0;
				int persons = 0;
				int albums = 0;
				int timeline = 0;
				int locations = 0;
				int imports = 0;
				int directory = 0;
				List<SmartCollectionImpl> collections = dbManager.obtainObjects(SmartCollectionImpl.class);
				final int work = collections.size();
				int incr = Math.max(1, (work + 31) / 32);
				final Shell shell = getShell();
				init(shell, work);
				String uriKey = QueryField.URI.getKey() + '=';
				String volumeKey = QueryField.VOLUME.getKey() + '=';
				int i = 0;
				for (SmartCollectionImpl sm : collections) {
					if (sm.getSmartCollection_subSelection_parent() == null)
						++main;
					else
						++subCollection;
					if (sm.getSystem()) {
						++system;
					} else
						++user;
					if (sm.getAlbum()) {
						if (sm.getSystem())
							++persons;
						else
							++albums;
					} else {
						String id = sm.getStringId();
						if (id.startsWith(uriKey) || id.startsWith(volumeKey))
							++directory;
						else {
							List<Criterion> crits = sm.getCriterion();
							if (!crits.isEmpty()) {
								Criterion criterion = crits.get(0);
								String field = criterion.getField();
								if (sm.getSystem()) {
									if (field.equals(QueryField.IPTC_DATECREATED.getKey()))
										++timeline;
									else if (criterion.getField().equals(QueryField.IPTC_LOCATIONCREATED.getKey()))
										++locations;
								} else if (field.equals(QueryField.IMPORTDATE.getKey()))
									++imports;
							}
						}
					}
					if (updateProgressBar(shell, ++i, incr, monitor))
						return Status.CANCEL_STATUS;
				}
				final int main1 = main;
				final int subCollection1 = subCollection;
				final int system1 = system;
				final int imports1 = imports;
				final int user1 = user - imports + 1;
				final int locations1 = locations;
				final int timeline1 = timeline;
				final int persons1 = persons;
				final int albums1 = albums;
				final int directory1 = directory;
				if (!shell.isDisposed()) {
					shell.getDisplay().asyncExec(new Runnable() {

						public void run() {
							if (!shell.isDisposed()) {
								detailsProgressBar.setVisible(false);
								setDetails(0, Messages.EditMetaDialog_main_collections, main1);
								setDetails(1, Messages.EditMetaDialog_subcollections, subCollection1);
								setDetails(2, Messages.EditMetaDialog_import_folders, imports1);
								setDetails(4, Messages.EditMetaDialog_system_collections, system1);
								setDetails(5, Messages.EditMetaDialog_directories, directory1);
								setDetails(6, Messages.EditMetaDialog_locations_folders, locations1);
								setDetails(7, Messages.EditMetaDialog_timeline_folders, timeline1);
								setDetails(8, Messages.EditMetaDialog_person_folders, persons1);
								setDetails(9, Messages.EditMetaDialog_user_collections, user1);
								setDetails(10, Messages.EditMetaDialog_albums, albums1);
								setDetails(11, Messages.EditMetaDialog_other_collections, user1 - albums1);
							}
						}
					});
				}
			} else if (clazz == LocationImpl.class) {

				Set<String> regions = new HashSet<String>(10);
				Set<String> countries = new HashSet<String>(51);
				Set<String> cities = new HashSet<String>(777);
				List<LocationImpl> collections = dbManager.obtainObjects(LocationImpl.class);
				final int work = collections.size();
				int incr = Math.max(1, (work + 31) / 32);
				final Shell shell = getShell();

				init(shell, work);
				int i = 0;
				for (LocationImpl loc : collections) {
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
					String city = loc.getCity();
					cities.add(city);
					if (updateProgressBar(shell, ++i, incr, monitor))
						return Status.CANCEL_STATUS;
				}
				final int regions1 = regions.size();
				final int countries1 = countries.size();
				final int cities1 = cities.size();
				if (!shell.isDisposed()) {
					shell.getDisplay().asyncExec(new Runnable() {
						public void run() {
							if (!shell.isDisposed()) {
								detailsProgressBar.setVisible(false);
								setDetails(0, Messages.EditMetaDialog_world_regions, regions1);
								setDetails(1, Messages.EditMetaDialog_countries, countries1);
								setDetails(2, Messages.EditMetaDialog_cities, cities1);
							}
						}
					});
				}
			}
			return Status.OK_STATUS;

		}

		private boolean updateProgressBar(final Shell shell, final int i, int incr, IProgressMonitor monitor) {
			if (!monitor.isCanceled() && !shell.isDisposed()) {
				if (i % incr == 0)
					shell.getDisplay().syncExec(new Runnable() {
						public void run() {
							if (!shell.isDisposed())
								detailsProgressBar.setSelection(i);
						}
					});
				return false;
			}
			return true;
		}

		private void init(final Shell shell, final int work) {
			if (!shell.isDisposed()) {
				shell.getDisplay().syncExec(new Runnable() {
					public void run() {
						if (!shell.isDisposed()) {
							setDetails(0, getName(), -1);
							detailsProgressBar.setMaximum(work);
							detailsProgressBar.setVisible(true);
						}
					}
				});
			}
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
			Set<String> availableKeywords = (Set<String>) inputElement;
			String[] available = filterKeywords(availableKeywords);
			if (flatKeywordGroup.isFlat())
				return available;
			if (chapters == null) {
				chapters = new HashMap<Character, List<String>>();
				for (String kw : available) {
					if (kw.length() > 0) {
						Character chapterTitle = Character.toUpperCase(kw.charAt(0));
						List<String> elements = chapters.get(chapterTitle);
						if (elements == null) {
							elements = new ArrayList<String>();
							chapters.put(chapterTitle, elements);
						}
						elements.add(kw);
					}
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
				if (kw.length() > 0) {
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

	public class StructGroup implements DisposeListener {

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
				locations = dbManager.obtainObjects(LocationImpl.class);
				root = new ArrayList<LocationNode>();
				for (LocationImpl location : locations)
					buildLocationTree(root, null, location, LocationNode.CONTINENT, true);
				final Control control = locTreeViewer.getControl();
				if (!control.isDisposed()) {
					control.getDisplay().asyncExec(new Runnable() {
						public void run() {
							if (!control.isDisposed())
								locTreeViewer.setInput(root);
						}
					});
				}
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

		public StructGroup(Composite sComposite, final int type, String item, boolean enabled) {
			this.type = type;
			this.item = item;
			this.enabled = enabled;
			sComposite.addDisposeListener(this);
			radioGroup = new FlatGroup(sComposite, SWT.NONE, settings, HIERARCHICAL_STRUCT + type);
			radioGroup.setLayoutData(new GridData(SWT.END, SWT.BEGINNING, false, false));
			radioGroup.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (type == QueryField.T_LOCATION)
						updateLocationStack();
					else
						flatComponent.update();
				}
			});
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
						radioGroup, 1);
				treeComposite = new Composite(stackComposite, SWT.NONE);
				treeComposite.setLayout(new GridLayout(2, false));
				createLocationTree(treeComposite);
				createButtonBar(viewerAndButtonComposite);
			} else {
				flatComponent = new StructComponent(dbManager, sComposite, null, type, false, structOverlayMap,
						radioGroup, 1);
				createButtonBar(sComposite);
			}
			flatComponent.addDoubleClickListener(new IDoubleClickListener() {
				public void doubleClick(DoubleClickEvent event) {
					editStruct(type);
				}
			});
			flatComponent.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					updateStructButtons();
				}
			});
			updateLocationStack();
		}

		private void createButtonBar(Composite parent) {
			final Composite bar = new Composite(parent, SWT.NONE);
			bar.setLayout(new GridLayout());
			bar.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
			addButton = createPushButton(bar, Messages.EditMetaDialog_add,
					NLS.bind(Messages.EditMetaDialog_add_x, item));
			addButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					EditStructDialog dialog = new EditStructDialog(getShell(), null, type, -1, structOverlayMap,
							Messages.EditMetaDialog_add_n);
					if (dialog.open() == Window.OK) {
						IdentifiableObject result = dialog.getResult();
						newStructMap.put(result.getStringId(), result);
						flatComponent.add(result);
						if (result instanceof LocationImpl) {
							buildLocationTree(root, null, (LocationImpl) result, LocationNode.CONTINENT, true);
							locTreeViewer.setInput(root);
							geographic = null;
						}
					}
				}
			});
			editButton = createPushButton(bar, Messages.EditMetaDialog_edit,
					NLS.bind(Messages.EditMetaDialog_edit_selected_x, item));
			editButton.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					editStruct(type);
				}

			});
			removeButton = createPushButton(bar, Messages.EditMetaDialog_remove,
					NLS.bind(Messages.EditMetaDialog_remove_x, item));
			removeButton.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					removeItem();
					if (type == QueryField.T_LOCATION)
						geographic = null;
				}

			});
			showButton = createPushButton(bar, Messages.EditMetaDialog_show_images,
					NLS.bind(Messages.EditMetaDialog_show_x, item));
			showButton.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					if (workbenchPage != null) {
						UiActivator.getDefault().getNavigationHistory(workbenchPage.getWorkbenchWindow()).postSelection(
								new StructuredSelection(createAdhocQuery(flatComponent.getSelectedElement())));
						close();
					}
				}
			});
			mapButton = createPushButton(bar, Messages.EditMetaDialog_show_in_map,
					NLS.bind(Messages.EditMetaDialog_show_x_in_map, item));
			mapButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					Object selectedElement = getSelectedElement();
					LocationImpl loc = (LocationImpl) getModelElement(selectedElement);
					if (loc == null && selectedElement instanceof LocationNode) {
						loc = new LocationImpl();
						LocationNode node = (LocationNode) selectedElement;
						while (node != null && !node.isUnknown()) {
							switch (node.getLevel()) {
							case LocationNode.COUNTRY:
								loc.setCountryISOCode(node.getKey());
								String name = node.getName();
								int p = name.lastIndexOf('(');
								if (p >= 0)
									name = name.substring(0, p).trim();
								loc.setCountryName(name);
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
						ILocationDisplay display = UiActivator.getDefault().getLocationDisplay();
						if (display != null)
							display.display(loc);
						close();
					}
				}
			});
			if (type != QueryField.T_LOCATION)
				mapButton.setVisible(false);
			emailButton = createPushButton(bar, Messages.EditMetaDialog_send_email,
					NLS.bind(Messages.EditMetaDialog_Email_x, item));
			emailButton.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					ContactImpl contact = (ContactImpl) flatComponent.getSelectedElement();
					String[] email = contact.getEmail();
					if (email != null && email.length > 0)
						UiActivator.getDefault().sendMail(Arrays.asList(email));
				}
			});
			webButton = createPushButton(bar, Messages.EditMetaDialog_visit_web_site,
					NLS.bind(Messages.EditMetaDialog_visit_x, item));
			webButton.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					ContactImpl contact = (ContactImpl) flatComponent.getSelectedElement();
					String[] web = contact.getWebUrl();
					if (web != null && web.length == 1) {
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
			});
			if (type != QueryField.T_CONTACT) {
				emailButton.setVisible(false);
				webButton.setVisible(false);
			}
		}

		protected SmartCollection createAdhocQuery(Object sel) {
			SmartCollectionImpl coll = new SmartCollectionImpl("", true, false, true, false, null, 0, null, 0, null, //$NON-NLS-1$
					null);
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
						QueryField.EQUALS, false));
				coll.addCriterion(new CriterionImpl(QueryField.IPTC_LOCATIONCREATED.getKey(), subfield.getKey(), key,
						QueryField.EQUALS, false));
			} else {
				IdentifiableObject item = getModelElement(sel);
				if (item instanceof LocationImpl) {
					coll.setName(QueryField.LOCATION_TYPE.getLabel() + '='
							+ StructComponent.getStructText(item, structOverlayMap));
					coll.addCriterion(new CriterionImpl(QueryField.IPTC_LOCATIONSHOWN.getKey(), null,
							item.getStringId(), QueryField.EQUALS, false));
					coll.addCriterion(new CriterionImpl(QueryField.IPTC_LOCATIONCREATED.getKey(), null,
							item.getStringId(), QueryField.EQUALS, false));
				} else if (item instanceof ContactImpl) {
					coll.setName(Messages.EditMetaDialog_contactquery
							+ StructComponent.getStructText(item, structOverlayMap));
					coll.addCriterion(new CriterionImpl(QueryField.IPTC_CONTACT.getKey(), null, item.getStringId(),
							QueryField.EQUALS, false));
				} else if (item instanceof ArtworkOrObjectImpl) {
					coll.setName(Messages.EditMetaDialog_artworkquery
							+ StructComponent.getStructText(item, structOverlayMap));
					coll.addCriterion(new CriterionImpl(QueryField.IPTC_ARTWORK.getKey(), null, item.getStringId(),
							QueryField.EQUALS, false));
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
					if (qfield != null) {
						if (!AcousticMessageDialog.openConfirm(getShell(),
								NLS.bind(Messages.EditMetaDialog_n_in_use, qfield.getLabel()),
								NLS.bind(Messages.EditMetaDialog_the_selected_x_is_used
										+ Messages.EditMetaDialog_want_to_delte, qfield.getLabel())))
							return;
					}
				}
				structOverlayMap.put(modelElement.getStringId(), null);
				flatComponent.remove(modelElement);
				if (locTreeViewer != null) {
					buildLocationTree(root, null, (LocationImpl) modelElement, LocationNode.CONTINENT, false);
					locTreeViewer.setInput(root);
				}
				return;
			}
			if (sel instanceof LocationNode) {
				removeChildren((LocationNode) sel, -1);
				root = new ArrayList<LocationNode>();
				for (LocationImpl location : locations)
					buildLocationTree(root, null, location, LocationNode.CONTINENT, true);
				locTreeViewer.setInput(root);
			}

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
			locTreeViewer.setComparator(new CategoryComparator());
			locTreeViewer.setLabelProvider(ZColumnLabelProvider.getDefaultInstance());
			locTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					updateStructButtons();
				}
			});
			locTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
				public void doubleClick(DoubleClickEvent event) {
					LocationNode node = (LocationNode) ((IStructuredSelection) locTreeViewer.getSelection())
							.getFirstElement();
					if (node != null) {
						if (node.getLocation() == null) {
							if (locTreeViewer.getExpandedState(node))
								locTreeViewer.collapseToLevel(node, 1);
							else
								locTreeViewer.expandToLevel(node, 1);
						} else
							editStruct(QueryField.T_LOCATION);
					}
				}
			});
			new LocationTreeJob().schedule(100);
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
								locTreeViewer.setInput(root);
							} else {
								updateElement(selectedElement);
							}
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
						if (fieldMap == null) {
							fieldMap = new HashMap<QueryField, Object>();
							structOverlayMap.put(id, fieldMap);
						}
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
			if (children != null) {
				for (LocationNode child : children) {
					IdentifiableObject modelElement = getModelElement(child);
					if (modelElement != null)
						return modelElement;
					modelElement = getFirstModelElement(child);
					if (modelElement != null)
						return modelElement;
				}
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
			IdentifiableObject model = null;
			if (sel != null && !(sel instanceof Character)) {
				model = getModelElement(sel);
				selected = (model != null);
				editable = selected || (sel instanceof LocationNode) && !((LocationNode) sel).isUnknown()
						&& ((LocationNode) sel).getLevel() >= LocationNode.COUNTRY;
			}
			editButton.setEnabled(editable && enabled);
			showButton.setEnabled(canShow);
			removeButton.setEnabled(editable && enabled);
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
					if (name == null || name.length() == 0) {
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
					if (iso != null && iso.length() > 0)
						sb.append(" (").append(iso) //$NON-NLS-1$
								.append(')').toString();
					if (sb.length() == 0) {
						name = Messages.EditMetaDialog_unknown_country;
						unknown = true;
					} else
						name = sb.toString();
					break;
				case LocationNode.STATE:
					key = getUpdatedValue(location, QueryField.LOCATION_STATE, location.getProvinceOrState());
					name = key;
					if (name == null || name.length() == 0) {
						name = Messages.EditMetaDialog_unknown_state;
						unknown = true;
					}
					break;
				default:
					key = getUpdatedValue(location, QueryField.LOCATION_CITY, location.getCity());
					name = key;
					if (name == null || name.length() == 0) {
						name = Messages.EditMetaDialog_unknown_city;
						unknown = true;
					}
					break;
				}
				if (key == null)
					key = ""; //$NON-NLS-1$
				LocationNode current = null;
				for (LocationNode locationNode : base)
					if (locationNode.getKey().equals(key)) {
						current = locationNode;
						break;
					}
				if (current == null && add) {
					current = new LocationNode(parent, key, name, level, null, unknown);
					base.add(current);
				}
				if (current != null)
					buildLocationTree(current.getChildren(), current, location, level + 1, add);
			} else if (add)
				base.add(new LocationNode(parent, "", null, level, location, false)); //$NON-NLS-1$
			else {
				Iterator<LocationNode> it = base.iterator();
				while (it.hasNext()) {
					LocationNode node = it.next();
					if (node.getLocation().equals(location)) {
						it.remove();
						break;
					}
				}
			}
		}

		public void saveSettings() {
			if (radioGroup != null)
				radioGroup.saveSettings();
		}

		public void widgetDisposed(DisposeEvent e) {
			Job.getJobManager().cancel(this);
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
			updateStructButtons();
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

		public LocationNode(LocationNode parent, String key, String name, int level, LocationImpl location,
				boolean unknown) {
			this.parent = parent;
			this.key = key;
			this.name = name;
			this.level = level;
			this.location = location;
			this.unknown = unknown;
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
				append(sb, Format.latitudeFormatter.toString(latitude));
			Double longitude = location.getLongitude();
			if (longitude != null && !Double.isNaN(longitude))
				append(sb, Format.longitudeFormatter.toString(longitude));
			Double altitude = location.getAltitude();
			if (altitude != null && !Double.isNaN(altitude))
				append(sb, Format.altitudeFormatter.toString(altitude));
			return (sb.length() == 0) ? NO_DETAILS : sb.toString();
		}

		private void append(StringBuilder sb, String s) {
			if (s != null && s.length() > 0) {
				if (sb.length() > 0)
					sb.append("; "); //$NON-NLS-1$
				sb.append(s);
			}
		}

		public boolean addChild(LocationNode e) {
			return children.add(e);
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

		/**
		 * @return unknown
		 */
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

	private static final Object[] EMPTY = new Object[0];

	private static final long HIGHWATERMARK = 2L * 1024L * 1024L * 1024L - 205L * 1024L * 1024L;
	private static final String NO_DETAILS = Messages.EditMetaDialog_no_details;

	private static final String HIERARCHICAL_STRUCT = "hierarchicalStruct"; //$NON-NLS-1$
	private Text yearlySeqNoField;
	private Text seqNoField;
	private Button fromPreviewButton;
	private Text backupField;
	protected static final IInputValidator keywordValidator = new KeywordValidator();
	static final String[] KEYWORDEXTENSIONS = new String[] { "*" //$NON-NLS-1$
			+ Constants.KEYWORDFILEEXTENSION + ";*" //$NON-NLS-1$
			+ Constants.KEYWORDFILEEXTENSION.toUpperCase() };
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
	protected static final SimpleDateFormat df = new SimpleDateFormat(Messages.EditMetaDialog_observation_date_format);
	private static final int PREVIOUS = 99;
	private static final int NEXT = 98;
	protected static final Object EMPTYSTRINGARRAY = new String[0];
	protected final Object[] EMPTYOBJECTARRAY = new Object[0];
	private static final long ONEDAY = 86400000L;
	private static final String SETTINGSID = "com.bdaum.zoom.ui.editMetaDialog"; //$NON-NLS-1$
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
	private boolean[] visited = new boolean[WATCHEDFOLDERS + 1];
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
	private CheckboxTreeViewer textIndexViewer;
	private CheckboxButton noIndexButton;
	private CheckboxButton slideTitleButton;
	private CheckboxButton slideDescrButton;
	private CheckboxButton exhibitionDescrButton;
	private CheckboxButton exhibitionTitleButton;
	private CheckboxButton webgalleryDescrButton;
	private CheckboxButton webgalleryTitleButton;
	private CheckboxButton personsButton;
	private CGroup simGroup;
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
			c.getDisplay().timerExec(300, new Runnable() {
				public void run() {
					if (c.isListening(SWT.Help))
						c.notifyListeners(SWT.Help, new Event());
				}
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
		WATCHEDFOLDERS = pgCnt;
		tabFolder.setSimple(false);
		tabFolder.setSelection(initialPage);
		initHeader();
		initPages(initialPage);
		tabFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tabChanged();
			}
		});
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
		Label label = new Label(composite, SWT.WRAP);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		label.setText(Messages.EditMetaDialog_configure_index_warning);
		noIndexButton = WidgetFactory.createCheckButton(composite, Messages.EditMetaDialog_no_indexing, null);
		noIndexButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateIndexingControls();
			}
		});
		Composite vGroup = new Composite(composite, SWT.NONE);
		vGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		vGroup.setLayout(new GridLayout(2, false));

		simGroup = new CGroup(vGroup, SWT.NONE);
		simGroup.setText(Messages.EditMetaDialog_similarity_algos);
		simGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		simGroup.setLayout(new GridLayout(2, false));
		simViewer = CheckboxTableViewer.newCheckList(simGroup, SWT.FULL_SELECTION | SWT.SINGLE | SWT.V_SCROLL);
		setViewerLayout(simViewer, 200, 1);
		simViewer.setContentProvider(ArrayContentProvider.getInstance());
		ColumnViewerToolTipSupport.enableFor(simViewer, ToolTip.NO_RECREATE);
		simViewer.setComparator(new ViewerComparator());
		TableViewerColumn col1 = new TableViewerColumn(simViewer, SWT.NONE);
		col1.getColumn().setWidth(400);
		col1.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getToolTipText(Object element) {
				if (element instanceof Algorithm)
					return ((Algorithm) element).getDescription();
				return super.getText(element);
			}

			@Override
			public String getText(Object element) {
				return element.toString();
			}
		});
		simViewer.setInput(Core.getCore().getDbFactory().getLireService(true).getSupportedSimilarityAlgorithms());
		textGroup = new CGroup(vGroup, SWT.NONE);
		textGroup.setText(Messages.EditMetaDialog_fields_in_text_search);
		textGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true));
		textGroup.setLayout(new GridLayout());
		textIndexViewer = new CheckboxTreeViewer(textGroup, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		setViewerLayout(textIndexViewer, 120, 1);
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
		textIndexViewer.setComparator(new ViewComparator());
		textIndexViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				UiUtilities.checkHierarchy(textIndexViewer, event.getElement(), event.getChecked(), true, true);
			}
		});
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

	private void updateIndexingControls() {
		boolean enabled = !noIndexButton.getSelection();
		simGroup.setVisible(enabled);
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
				languageCombo.setComparator(new ViewerComparator());
				languageCombo.setInput(supportedLanguages);
			} else {
				CLink link = new CLink(header, SWT.NONE);
				link.setText(Messages.EditMetaDialog_intall_dict);
				link.addSelectionListener(new SelectionAdapter() {

					@Override
					public void widgetSelected(SelectionEvent e) {
						try {
							IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser();
							String language = Locale.getDefault().getLanguage();
							String url = System.getProperty("com.bdaum.zoom.dictionaries." + language); //$NON-NLS-1$
							if (url == null)
								url = System.getProperty("com.bdaum.zoom.dictionaries"); //$NON-NLS-1$
							browser.openURL(new URL(url));
						} catch (PartInitException e1) {
							// do nothing
						} catch (MalformedURLException e1) {
							// should never happen
						}
					}
				});
			}
		} else {

			Label label = new Label(header, SWT.NONE);
			label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 2, 1));
		}
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
		readOnlyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				readonly = readOnlyButton.getSelection();
				updateFolderButtons();
				updateButtons();
				updateFields();
			}
		});
		readOnlyButton.setEnabled(!newDb);

		Label label = new Label(header, SWT.RIGHT);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		label.setText(Messages.EditMetaDialog_last_import);

		lastImport = new Text(header, SWT.READ_ONLY | SWT.BORDER);
		lastImport.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (window != null) {
					LastImportCommand command = new LastImportCommand();
					command.init(window);
					command.run();
				}
			}
		});
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
		backupField = new Text(backupGroup, SWT.BORDER);
		backupField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		CLink link = new CLink(backupGroup, SWT.NONE);
		link.setText(Messages.EditMetaDialog_configure_interval);
		link.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				PreferencesUtil.createPreferenceDialogOn(getShell(), GeneralPreferencePage.ID, new String[0], "backup") //$NON-NLS-1$
						.open();
				updateBackupTooltip();
			}
		});
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
			createTimeLineButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					OperationJob.executeOperation(
							new CreateTimelineOperation(
									(String) ((IStructuredSelection) timelineViewer.getSelection()).getFirstElement()),
							EditMetaDialog.this);
				}
			});
		}
		timelineViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateCreateNowButtons();
			}
		});
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
		createLocationFoldersButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				OperationJob.executeOperation(
						new CreateLocationFolderOperation(
								(String) ((IStructuredSelection) locationViewer.getSelection()).getFirstElement()),
						EditMetaDialog.this);
			}
		});
		locationViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				updateCreateNowButtons();
			}
		});

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
		String timeline = (String) ((IStructuredSelection) timelineViewer.getSelection()).getFirstElement();
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
		String locfolder = (String) ((IStructuredSelection) locationViewer.getSelection()).getFirstElement();
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
			freeSpaceField.setData("id", "errors"); //$NON-NLS-1$ //$NON-NLS-2$
			freeSpaceField.setForeground(freeSpaceField.getDisplay().getSystemColor(SWT.COLOR_RED));
		} else {
			freeSpaceField.setData("id", null); //$NON-NLS-1$
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
		detailFields = new Label[13];
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
		SelectionAdapter defragListener = dbManager.isEmbedded() ? new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (AcousticMessageDialog.openQuestion(getShell(), Messages.EditMetaDialog_cat_maintenance,
						Messages.EditMetaDialog_defrag_continue)) {
					File file = dbManager.getFile();
					BatchActivator.setFastExit(false);
					if (UiActivator.getDefault().preCatClose(CatalogListener.TUNE,
							Messages.EditMetaDialog_closing_for_defrag, Messages.EditMetaDialog_operations_running,
							false)) {
						dbManager = CoreActivator.getDefault().openDatabase(file.getAbsolutePath());
						CoreActivator.getDefault().setCatFile(file);
						CoreActivator.getDefault().fireCatalogOpened(false);
						UiActivator.getDefault().postCatOpen();
						UiActivator.getDefault().postCatInit(false);
						computeStatistics();
					}
					BatchActivator.setFastExit(true);
				}
			}
		} : null;
		freeSegmentsField = createTextField(composite, Messages.EditMetaDialog_free_segments, 100, defragListener,
				Messages.EditMetaDialog_defrag_now);
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
		((Label) textField.getData(LABELKEY)).setText(label);
		textField.setText(value < 0 ? "" : String.valueOf(value)); //$NON-NLS-1$
		for (int j = i + 1; j < detailFields.length; j++) {
			((Label) detailFields[j].getData(LABELKEY)).setText(""); //$NON-NLS-1$
			detailFields[j].setText(""); //$NON-NLS-1$
		}
	}

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
		TableViewerColumn col0 = createColumn(Messages.EditMetaDialog_path, 240);
		col0.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof WatchedFolder) {
					String uri = ((WatchedFolder) element).getUri();
					try {
						File file = new File(new URI(uri));
						String path = file.getPath();
						if (!path.endsWith(File.separator))
							path += File.separator;
						if (!file.exists())
							path += Messages.EditMetaDialog_offline;
						return path;
					} catch (URISyntaxException e) {
						// ignore
					}
					return uri;
				}
				return null;
			}
		});
		TableViewerColumn col1 = createColumn(Messages.EditMetaDialog_volume, 80);
		col1.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof WatchedFolder) {
					String volume = ((WatchedFolder) element).getVolume();
					return volume == null ? "" : volume; //$NON-NLS-1$
				}
				return null;
			}
		});
		TableViewerColumn col2 = createColumn(Messages.EditMetaDialog_type, 60);
		col2.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof WatchedFolder) {
					return ((WatchedFolder) element).getTransfer() ? Messages.EditMetaDialog_transfer
							: Messages.EditMetaDialog_storage;
				}
				return null;
			}
		});
		TableViewerColumn col3 = createColumn(Messages.EditMetaDialog_recursive, 80);
		col3.setLabelProvider(new ZColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof WatchedFolder)
					return ((WatchedFolder) element).getRecursive() ? Messages.EditMetaDialog_c_yes
							: Messages.EditMetaDialog_c_no;
				return null;
			}
		});
		TableViewerColumn col4 = createColumn(Messages.EditMetaDialog_last_observation, 120);
		col4.setLabelProvider(new ZColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof WatchedFolder)
					return df.format(new Date(((WatchedFolder) element).getLastObservation()));
				return null;
			}
		});
		TableViewerColumn col5 = createColumn(Messages.EditMetaDialog_file_filter, 220);
		col5.setLabelProvider(new ZColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof WatchedFolder) {
					WatchedFolder wf = (WatchedFolder) element;
					if (!wf.getTransfer())
						return UiUtilities.getFilters(wf);
					StringBuilder sb = new StringBuilder();
					int skipPolicy = wf.getSkipPolicy();
					if (skipPolicy < 0 && skipPolicy >= ImportFileSelectionPage.SKIPPOLICIES.length)
						skipPolicy = 0;
					sb.append(ImportFileSelectionPage.SKIPPOLICIES[skipPolicy]);
					sb.append(" | ").append(wf.getTargetDir()); //$NON-NLS-1$
					return sb.toString();
				}
				return null;
			}
		});
		watchedFolderViewer.setContentProvider(ArrayContentProvider.getInstance());
		watchedFolderViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateFolderButtons();
			}
		});
		watchedFolderViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				editWatchedFolder();
			}
		});
		Composite buttonComp = new Composite(composite, SWT.NONE);
		buttonComp.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, true));
		buttonComp.setLayout(new GridLayout());
		addFolderButton = new Button(buttonComp, SWT.PUSH);
		addFolderButton.setText(Messages.EditMetaDialog_add);
		addFolderButton.setToolTipText(Messages.EditMetaDialog_add_watched_folder);
		addFolderButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				WatchedFolder folder = new WatchedFolderImpl(null, null, 0L, true, null, false, null, false, 0, null, 2,
						null, null, Constants.FILESOURCE_DIGITAL_CAMERA);
				WatchedFolderWizard wizard = new WatchedFolderWizard();
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
			}
		});
		editFolderButton = new Button(buttonComp, SWT.PUSH);
		editFolderButton.setText(Messages.EditMetaDialog_edit_watched);
		editFolderButton.setToolTipText(Messages.EditMetaDialog_edit_watched_tooltip);
		editFolderButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editWatchedFolder();
			}
		});

		removeFolderButton = new Button(buttonComp, SWT.PUSH);
		removeFolderButton.setText(Messages.EditMetaDialog_remove);
		removeFolderButton.setToolTipText(Messages.EditMetaDialog_remove_watched_folder);
		removeFolderButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				WatchedFolderImpl firstElement = (WatchedFolderImpl) ((IStructuredSelection) watchedFolderViewer
						.getSelection()).getFirstElement();
				watchedFolders.remove(firstElement);
				watchedFolderViewer.remove(firstElement);
			}
		});
		categoryRemoveButton.setEnabled(!readonly);
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
		boolean enabled = !watchedFolderViewer.getSelection().isEmpty() && !readonly;
		removeFolderButton.setEnabled(enabled);
		editFolderButton.setEnabled(enabled);
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
		final Composite pageComp = createTabPage(folder, lab, descr, columns);
		Label header = new Label(pageComp, SWT.NONE);
		header.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, columns - space, 1));
		header.setText(descr);
		return pageComp;
	}

	protected static Composite createTabPage(CTabFolder folder, String lab, String descr, int columns) {
		final CTabItem tabItem = new CTabItem(folder, SWT.NONE);
		tabItem.setText(lab);
		tabItem.setToolTipText(descr);
		final Composite pageComp = new Composite(folder, SWT.NONE);
		pageComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(columns, false);
		layout.verticalSpacing = 10;
		pageComp.setLayout(layout);
		tabItem.setControl(pageComp);
		return pageComp;
	}

	@SuppressWarnings("unused")
	private static Label createTextField(final Composite parent, String lab, int w, SelectionListener listener,
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
			link.addSelectionListener(listener);
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
		textField.setData(LABELKEY, label);
		return textField;
	}

	private void createThumbnailsGroup(final CTabFolder folder) {
		final Composite thComp = createTabPage(folder, Messages.EditMetaDialog_thumbnails,
				Messages.EditMetaDialog_thumbnail_tooltip, 1, 0);

		final CGroup thButtonComp = new CGroup(thComp, SWT.NONE);
		thButtonComp.setText(Messages.EditMetaDialog_thumbnail_resolution);
		thButtonComp.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		final Label highResolutionWillLabel = new Label(thButtonComp, SWT.WRAP);
		highResolutionWillLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		highResolutionWillLabel.setText(Messages.EditMetaDialog_high_res_will_increase);
		thButtonComp.setLayout(new GridLayout());
		thumbSizeGroup = new RadioButtonGroup(thButtonComp, null, SWT.NONE, Messages.EditMetaDialog_low,
				Messages.EditMetaDialog_medium, Messages.EditMetaDialog_high, Messages.EditMetaDialog_very_high);

		// Preview
		final Composite composite = new Composite(thComp, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));

		new Label(composite, SWT.NONE).setText(Messages.EditMetaDialog_from_preview);

		fromPreviewButton = new Button(composite, SWT.CHECK);

		// Sharpening
		final CGroup shButtonComp = new CGroup(thComp, SWT.NONE);
		shButtonComp.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		shButtonComp.setText(Messages.EditMetaDialog_sharpen_during_import);
		shButtonComp.setLayout(new GridLayout(8, false));
		sharpenButtonGroup = new RadioButtonGroup(shButtonComp, null, SWT.HORIZONTAL,
				Messages.EditMetaDialog_dont_sharpen, Messages.EditMetaDialog_light_sharpen,
				Messages.EditMetaDialog_medium_sharpen, Messages.EditMetaDialog_heavy_sharpen);
		final CGroup comprGroup = new CGroup(thComp, SWT.NONE);
		comprGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		comprGroup.setText(Messages.EditMetaDialog_compression);
		comprGroup.setLayout(new GridLayout(2, false));
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
		flatKeywordGroup.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				keywordViewer.setInput(keywords);
				keywordViewer.expandAll();
				keywordExpandCollapseGroup.setVisible(!flatKeywordGroup.isFlat());
			}
		});
		Composite viewerGroup = new Composite(kwGroup, SWT.NONE);
		viewerGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginWidth = 0;
		viewerGroup.setLayout(layout);
		final FilterField filterField = new FilterField(viewerGroup);
		filterField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				keywordViewer.setInput(keywords);
				keywordViewer.expandAll();
			}
		});
		final CheckboxButton excludeButton = WidgetFactory.createCheckButton(viewerGroup,
				Messages.KeywordGroup_exclude_geographic, new GridData(SWT.END, SWT.CENTER, true, false));
		excludeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				excludeGeographic = excludeButton.getSelection();
				Object[] expandedElements = keywordViewer.getExpandedElements();
				keywordViewer.setInput(keywords);
				keywordViewer.setExpandedElements(expandedElements);
			}
		});
		keywordExpandCollapseGroup = new ExpandCollapseGroup(viewerGroup, SWT.NONE,
				new GridData(SWT.END, SWT.BEGINNING, true, false, 2, 1));
		keywordViewer = new TreeViewer(viewerGroup, SWT.V_SCROLL | SWT.BORDER);
		keywordExpandCollapseGroup.setViewer(keywordViewer);
		keywordExpandCollapseGroup.setVisible(!flatKeywordGroup.isFlat());
		setViewerLayout(keywordViewer, 220, 2);
		keywordViewer.setContentProvider(new KeywordContentProvider());
		keywordViewer.setLabelProvider(new KeywordLabelProvider(getVocabManager(), null));
		ColumnViewerToolTipSupport.enableFor(keywordViewer);
		keywordViewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				return (e1 instanceof String && e2 instanceof String)
						? super.compare(viewer, ((String) e1).toUpperCase(), ((String) e2).toUpperCase())
						: super.compare(viewer, e1, e2);
			}
		});
		keywordViewer.setFilters(new ViewerFilter[] { new ViewerFilter() {
			@Override
			public boolean select(Viewer aViewer, Object parentElement, Object element) {
				WildCardFilter filter = filterField.getFilter();
				return filter == null || element instanceof Character || filter.accept((String) element);
			}
		} });
		keywordViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateButtons();
			}
		});
		configureKeywordLink = new CLink(viewerGroup, SWT.NONE);
		configureKeywordLink.setText(Messages.EditMetaDialog_configure_keyword_filter);
		configureKeywordLink.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PreferencesUtil.createPreferenceDialogOn(getShell(), KeyPreferencePage.ID, new String[0], null).open();
			}
		});
		final Composite buttonGroup = new Composite(kwGroup, SWT.NONE);
		buttonGroup.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		buttonGroup.setLayout(new GridLayout());

		keywordAddButton = createPushButton(buttonGroup, Messages.EditMetaDialog_add,
				Messages.EditMetaDialog_add_keyword_tooltip);
		keywordAddButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ZInputDialog dialog = createKeywordDialog(Messages.EditMetaDialog_add_keyword,
						Messages.EditMetaDialog_enter_a_new_keyword, ""); //$NON-NLS-1$
				if (dialog.open() == Window.OK)
					addKeywordToViewer(keywordViewer, dialog.getValue(), true);
			}
		});
		keywordModifyButton = createPushButton(buttonGroup, Messages.EditMetaDialog_edit,
				Messages.EditMetaDialog_modify_keyword_tooltip);
		keywordModifyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editKeyword();
			}
		});
		keywordViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				editKeyword();
			}
		});

		keywordReplaceButton = createPushButton(buttonGroup, Messages.EditMetaDialog_replace,
				Messages.EditMetaDialog_replace_tooltip);
		keywordReplaceButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
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
			}
		});
		keywordDeleteButton = createPushButton(buttonGroup, Messages.EditMetaDialog_delete_keyword,
				Messages.EditMetaDialog_delete_keyword_tooltip);
		keywordDeleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				BusyIndicator.showWhile(keywordDeleteButton.getDisplay(), new Runnable() {
					public void run() {
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
					}
				});
			}
		});
		keywordShowButton = createPushButton(buttonGroup, Messages.EditMetaDialog_show_images,
				Messages.EditMetaDialog_show_keyword_tooltip);
		keywordShowButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) keywordViewer.getSelection();
				String kw = (String) selection.getFirstElement();
				if (kw != null) {
					SmartCollectionImpl sm = new SmartCollectionImpl(kw, false, false, true, false, null, 0, null, 0,
							null, null);
					sm.addCriterion(
							new CriterionImpl(QueryField.IPTC_KEYWORDS.getKey(), null, kw, QueryField.EQUALS, false));
					sm.addSortCriterion(new SortCriterionImpl(QueryField.IPTC_DATECREATED.getKey(), null, true));
					UiActivator.getDefault().getNavigationHistory(workbenchPage.getWorkbenchWindow())
							.postSelection(new StructuredSelection(sm));
					close();
				}
			}
		});
		new Label(buttonGroup, SWT.SEPARATOR | SWT.HORIZONTAL);

		keywordCollectButton = createPushButton(buttonGroup, Messages.EditMetaDialog_collect,
				Messages.EditMetaDialog_collect_keyword_tooltip);
		keywordCollectButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				KeywordCollectDialog dialog = new KeywordCollectDialog(getShell(), keywords);
				if (dialog.open() == Window.OK) {
					int i = 0;
					for (String kw : dialog.getToAdd())
						addKeywordToViewer(keywordViewer, kw, i++ == 0);
					for (String kw : dialog.getToRemove())
						removeKeywordFromViewer(keywordViewer, kw);
				}
			}
		});

		keywordLoadButton = createPushButton(buttonGroup, Messages.EditMetaDialog_load,
				NLS.bind(Messages.EditMetaDialog_load_keyword_tooltip, Constants.KEYWORDFILEEXTENSION));
		keywordLoadButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent ev) {
				FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
				dialog.setFilterExtensions(KEYWORDEXTENSIONS);
				dialog.setFilterNames(new String[] { Constants.APPNAME + Messages.EditMetaDialog_zoom_keyword_file
						+ Constants.KEYWORDFILEEXTENSION + ")" }); //$NON-NLS-1$
				String filename = dialog.open();
				if (filename != null) {
					try (InputStream in = new BufferedInputStream(new FileInputStream(filename))) {
						List<String> list = Utilities.loadKeywords(in);
						keywords.clear();
						keywords.addAll(list);
					} catch (IOException e) {
						// ignore
					}
					keywordViewer.setInput(keywords);
					keywordViewer.expandAll();
				}
			}
		});
		keywordSaveButton = createPushButton(buttonGroup, Messages.EditMetaDialog_save,
				NLS.bind(Messages.EditMetaDialog_save_keyword_tooltip, Constants.KEYWORDFILEEXTENSION));
		keywordSaveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
				dialog.setFilterExtensions(KEYWORDEXTENSIONS);
				dialog.setFilterNames(new String[] { Constants.APPNAME + Messages.EditMetaDialog_zoom_keyword_file
						+ Constants.KEYWORDFILEEXTENSION + ")" }); //$NON-NLS-1$
				dialog.setFileName("*" + Constants.KEYWORDFILEEXTENSION); //$NON-NLS-1$
				dialog.setOverwrite(true);
				String filename = dialog.open();
				if (filename != null)
					Utilities.saveKeywords(keywords, new File(filename));
			}
		});
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
				if (element instanceof String) {
					File file = new File((String) element);
					if (!file.exists())
						return Icons.error.getImage();
				}
				return null;
			}

			@Override
			public String getToolTipText(Object element) {
				if (element instanceof String) {
					File file = new File((String) element);
					if (!file.exists())
						return Messages.EditMetaDialog_file_does_not_exist;
				}
				return null;
			}

			@Override
			public Image getToolTipImage(Object element) {
				return getImage(element);
			}
		});
		vocabViewer.setContentProvider(ArrayContentProvider.getInstance());
		vocabViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateButtons();
			}
		});
		vocabViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				viewVocab();
			}
		});
		ColumnViewerToolTipSupport.enableFor(vocabViewer);
		Composite vocabButtonGroup = new Composite(vocabViewerGroup, SWT.NONE);
		vocabButtonGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		vocabButtonGroup.setLayout(new GridLayout());
		vocabAddButton = createPushButton(vocabButtonGroup, Messages.EditMetaDialog_add,
				Messages.EditMetaDialog_add_vocab);
		vocabAddButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
				dialog.setText(Messages.EditMetaDialog_select_vocab);
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
			}
		});
		vocabRemoveButton = createPushButton(vocabButtonGroup, Messages.EditMetaDialog_remove,
				Messages.EditMetaDialog_remove_vocab);
		vocabRemoveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				@SuppressWarnings("unchecked")
				Iterator<Object> it = ((IStructuredSelection) vocabViewer.getSelection()).iterator();
				while (it.hasNext()) {
					Object file = it.next();
					vocabularies.remove(file);
					vocabViewer.remove(file);
				}
				if (vocabManager != null)
					vocabManager.reset(vocabularies);
				updateKeywordViewer();
			}
		});
		vocabViewButton = createPushButton(vocabButtonGroup, Messages.EditMetaDialog_view_vocab,
				Messages.EditMetaDialog_view_vocab_tooltip);
		vocabViewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				viewVocab();
			}
		});
		new Label(vocabButtonGroup, SWT.SEPARATOR | SWT.HORIZONTAL);
		vocabEnforceButton = createPushButton(vocabButtonGroup, Messages.EditMetaDialog_enforce,
				Messages.EditMetaDialog_enforce_tooltip);
		vocabEnforceButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
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
			}
		});
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
		catTreeViewer.setComparator(new CategoryComparator());
		catTreeViewer.setLabelProvider(new CategoryLabelProvider());

		final Composite buttonGroup = new Composite(catComposite, SWT.NONE);
		buttonGroup.setLayout(new GridLayout());
		buttonGroup.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

		categoryAddButton = createPushButton(buttonGroup, Messages.EditMetaDialog_add,
				Messages.EditMetaDialog_add_category);
		categoryAddButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				EditCategoryDialog inputDialog = new EditCategoryDialog(parent.getShell(), null, categories, null, null,
						false);
				if (inputDialog.open() == Window.OK) {
					String label = inputDialog.getLabel();
					Category category = new CategoryImpl(label);
					category.setSynonyms(inputDialog.getSynonyms());
					categories.put(label, category);
					catTreeViewer.setInput(categories);
				}
			}
		});

		categoryRefineButton = createPushButton(buttonGroup, Messages.EditMetaDialog_refine,
				Messages.EditMetaDialog_refine_category);
		categoryRefineButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				Category firstElement = (Category) ((IStructuredSelection) catTreeViewer.getSelection())
						.getFirstElement();
				EditCategoryDialog inputDialog = new EditCategoryDialog(parent.getShell(), null, categories, null,
						firstElement, false);
				if (inputDialog.open() == Window.OK) {
					Category subCategory = new CategoryImpl(inputDialog.getLabel());
					subCategory.setSynonyms(inputDialog.getSynonyms());
					firstElement.putSubCategory(subCategory);
					catTreeViewer.add(firstElement, subCategory);
					catTreeViewer.expandToLevel(firstElement, 2);
				}
			}
		});

		categoryEditButton = createPushButton(buttonGroup, Messages.EditMetaDialog_edit,
				Messages.EditMetaDialog_edit_selected_category);
		categoryEditButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editCategory(parent);
			}
		});
		catTreeViewer.addDoubleClickListener(new IDoubleClickListener() {

			public void doubleClick(DoubleClickEvent event) {
				editCategory(parent);
			}
		});
		categoryRemoveButton = createPushButton(buttonGroup, Messages.EditMetaDialog_remove,
				Messages.EditMetaDialog_remove_category);
		categoryRemoveButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				CategoryImpl firstElement = (CategoryImpl) ((IStructuredSelection) catTreeViewer.getSelection())
						.getFirstElement();
				Category subCategory_parent = firstElement.getCategory_subCategory_parent();
				if (subCategory_parent != null)
					subCategory_parent.removeSubCategory(firstElement.getLabel());
				else
					categories.remove(firstElement.getLabel());
				catTreeViewer.remove(firstElement);
			}

		});

		new Label(buttonGroup, SWT.HORIZONTAL | SWT.SEPARATOR).setText(Messages.EditMetaDialog_label);
		final String[] catExtensions = new String[] { "*" //$NON-NLS-1$
				+ Constants.CATEGORYFILEEXTENSION + ";*" //$NON-NLS-1$
				+ Constants.CATEGORYFILEEXTENSION.toUpperCase() };
		loadCatButton = createPushButton(buttonGroup, Messages.EditMetaDialog_load,
				NLS.bind(Messages.EditMetaDialog_load_category, Constants.CATEGORYFILEEXTENSION));
		loadCatButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
				dialog.setFilterExtensions(catExtensions);
				dialog.setFilterNames(new String[] { Constants.APPNAME + Messages.EditMetaDialog_zoom_cat_file
						+ Constants.CATEGORYFILEEXTENSION + ")" }); //$NON-NLS-1$
				String filename = dialog.open();
				if (filename != null) {
					try (InputStream in = new BufferedInputStream(new FileInputStream(filename))) {
						Utilities.loadCategories(dbManager, categories, in, null);
					} catch (IOException e1) {
						// ignore
					}
				}
			}
		});

		saveCatButton = createPushButton(buttonGroup, Messages.EditMetaDialog_save,
				NLS.bind(Messages.EditMetaDialog_save_category, Constants.CATEGORYFILEEXTENSION));
		saveCatButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
				dialog.setFilterExtensions(catExtensions);
				dialog.setFilterNames(new String[] { Constants.APPNAME + Messages.EditMetaDialog_zoom_cat_file
						+ Constants.CATEGORYFILEEXTENSION + ")" }); //$NON-NLS-1$
				dialog.setFileName("*" + Constants.CATEGORYFILEEXTENSION); //$NON-NLS-1$
				dialog.setOverwrite(true);
				String filename = dialog.open();
				if (filename != null)
					Utilities.saveCategories(meta, new File(filename));
			}
		});
		catTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				updateButtons();
			}
		});
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
		vocabAddButton.setEnabled(!readonly);
		boolean vocabSel = !vocabViewer.getSelection().isEmpty();
		vocabRemoveButton.setEnabled(vocabSel && !readonly);
		vocabViewButton.setEnabled(vocabSel);
		vocabEnforceButton.setEnabled(vocabularies != null && !vocabularies.isEmpty());
		loadCatButton.setEnabled(!readonly);
		boolean enabled = newDb ? visited[OVERVIEW] && visited[THUMBNAILS] && visited[CATEGORIES] && visited[KEYWORDS]
				&& visited[INDEXING] : true;
		getButton(IDialogConstants.OK_ID).setEnabled(enabled);
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
		creationDate.setText(Constants.DFDT.format(meta.getCreationDate()));
		lastImport.setText(Constants.DFDT.format(meta.getLastImport()));
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
			Set<String> cbirAlgorithms = CoreActivator.getDefault().getCbirAlgorithms();
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
			UiUtilities.checkInitialHierarchy(textIndexViewer, fields);
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
		} else if (index == STATISTICS) {
			tabFolder.getDisplay().timerExec(100, new Runnable() {
				public void run() {
					computeStatistics();
				}
			});
		} else if (index == WATCHEDFOLDERS) {
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
						f.getSelectedTemplate(), f.getCue(), f.getFileSource());
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
		Date lastBackup = meta.getLastBackup();
		if (lastBackup != null) {
			Date nextBackup = new Date(lastBackup.getTime() + CoreActivator.getDefault().getBackupInterval() * ONEDAY);
			backupField.setToolTipText(NLS.bind(Messages.EditMetaDialog_backup_tooltip,
					Constants.DFDT.format(lastBackup), Constants.DFDT.format(nextBackup)));
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
			timelineOption = (String) ((IStructuredSelection) timelineViewer.getSelection()).getFirstElement();
			if (timelineOption == null)
				timelineOption = Meta_type.timeline_no;
			locationOption = (String) ((IStructuredSelection) locationViewer.getSelection()).getFirstElement();
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
		Map<String, Category> cats;
		if (visited[CATEGORIES]) {
			cats = categories;
		} else {
			cats = meta.getCategory();
		}
		boolean addToKeywords;
		Set<String> kWords;
		if (visited[KEYWORDS]) {
			addToKeywords = addToKeywordsButton.getSelection();
			kWords = keywords;
		} else {
			addToKeywords = booleanValue(meta.getPersonsToKeywords());
			kWords = meta.getKeywords();
		}
		Set<String> cbirAlgorithms;
		Set<String> indexedTextFields;
		boolean noIndex;
		if (visited[INDEXING]) {
			noIndex = noIndexButton.getSelection();
			cbirAlgorithms = new HashSet<String>();
			for (Object el : simViewer.getCheckedElements())
				cbirAlgorithms.add(((Algorithm) el).getName());
			cbirAlgorithms.add(""); // indicate that it has been //$NON-NLS-1$
									// set
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
										// been
										// set
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
				: (String) ((IStructuredSelection) languageCombo.getSelection()).getFirstElement();
		Theme theme = (Theme) ((IStructuredSelection) themeField.getSelection()).getFirstElement();
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

	private void editCategory(final Composite parent) {
		if (!readonly) {
			Category firstElement = (Category) ((IStructuredSelection) catTreeViewer.getSelection()).getFirstElement();
			EditCategoryDialog inputDialog = new EditCategoryDialog(parent.getShell(), firstElement, categories, null,
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
		final String[] kws = keywords.toArray(new String[keywords.size()]);
		IInputValidator validator = new IInputValidator() {
			public String isValid(String newText) {
				if (newText.length() == 0)
					return Messages.EditMetaDialog_please_enter_a_keyword;
				if (keywords.contains(newText))
					return Messages.EditMetaDialog_keyword_already_exsists;
				return null;
			}
		};
		IInputAdvisor advisor = new IInputAdvisor() {
			public String getAdvice(String newText) {
				int l = newText.length();
				if (kws.length > 0 && l > 0) {
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
		};
		return new ZInputDialog(getShell(), title, message, dflt, advisor, validator, false);
	}

	public void setInitialPage(int page) {
		this.initialPage = page;
	}

	public void editWatchedFolder() {
		WatchedFolderWizard wizard = new WatchedFolderWizard();
		WizardDialog wizardDialog = new WizardDialog(getShell(), wizard);
		wizard.init(null, (IStructuredSelection) watchedFolderViewer.getSelection());
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
				Theme newTheme = (Theme) ((IStructuredSelection) themeField.getSelection()).getFirstElement();
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
			File cfile = dbManager.getFile();
			List<Object> toBeStored = new ArrayList<Object>();
			Meta newMeta = dbManager.getMeta(true);
			toBeStored.add(newMeta);
			CoreActivator.getDefault().copyMeta(previousMeta, cfile, toBeStored, newMeta);
			dbManager.safeTransaction(null, toBeStored);
		}
	}

	protected void viewVocab() {
		List<String> selected = new ArrayList<>();
		@SuppressWarnings("unchecked")
		Iterator<Object> it = ((IStructuredSelection) vocabViewer.getSelection()).iterator();
		while (it.hasNext())
			selected.add((String) it.next());
		if (!selected.isEmpty()) {
			VocabNode root = new VocabManager(selected, EditMetaDialog.this).getVocabTree();
			ViewVocabDialog dialog = new ViewVocabDialog(getShell(), root,
					selected.size() != 1 ? null : new File(selected.get(0)), false);
			dialog.open();
		}
	}

}
