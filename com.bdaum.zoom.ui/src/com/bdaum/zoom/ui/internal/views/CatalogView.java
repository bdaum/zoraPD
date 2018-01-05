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
 * (c) 2009-2018 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.ui.internal.views;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.commands.operations.UndoContext;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.operations.UndoRedoActionGroup;
import org.eclipse.ui.part.DrillDownAdapter;

import com.bdaum.aoModeling.runtime.AomList;
import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.asset.Region;
import com.bdaum.zoom.cat.model.asset.RegionImpl;
import com.bdaum.zoom.cat.model.group.Criterion;
import com.bdaum.zoom.cat.model.group.CriterionImpl;
import com.bdaum.zoom.cat.model.group.Group;
import com.bdaum.zoom.cat.model.group.GroupImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.SortCriterion;
import com.bdaum.zoom.cat.model.group.SortCriterionImpl;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitImpl;
import com.bdaum.zoom.cat.model.group.exhibition.Exhibition;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitionImpl;
import com.bdaum.zoom.cat.model.group.exhibition.Wall;
import com.bdaum.zoom.cat.model.group.slideShow.SlideImpl;
import com.bdaum.zoom.cat.model.group.slideShow.SlideShow;
import com.bdaum.zoom.cat.model.group.slideShow.SlideShowImpl;
import com.bdaum.zoom.cat.model.group.webGallery.Storyboard;
import com.bdaum.zoom.cat.model.group.webGallery.WebExhibitImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebGallery;
import com.bdaum.zoom.cat.model.group.webGallery.WebGalleryImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebParameter;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.CatalogAdapter;
import com.bdaum.zoom.core.CatalogListener;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.Range;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.FileNameExtensionFilter;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.IProfiledOperation;
import com.bdaum.zoom.operations.internal.AddAlbumOperation;
import com.bdaum.zoom.operations.internal.AutoRuleOperation;
import com.bdaum.zoom.operations.internal.MoveOperation;
import com.bdaum.zoom.operations.internal.MultiModifyAssetOperation;
import com.bdaum.zoom.operations.internal.RateOperation;
import com.bdaum.zoom.operations.internal.RemoveAlbumOperation;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.IDropinHandler;
import com.bdaum.zoom.ui.IZoomCommandIds;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.Icons.Icon;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.dialogs.CollectionEditDialog;
import com.bdaum.zoom.ui.internal.dialogs.EditMetaDialog;
import com.bdaum.zoom.ui.internal.dialogs.ExhibitionEditDialog;
import com.bdaum.zoom.ui.internal.dialogs.FilterDialog;
import com.bdaum.zoom.ui.internal.dialogs.GroupDialog;
import com.bdaum.zoom.ui.internal.dialogs.SlideshowEditDialog;
import com.bdaum.zoom.ui.internal.dialogs.WebGalleryEditDialog;
import com.bdaum.zoom.ui.internal.hover.IGalleryHover;
import com.bdaum.zoom.ui.internal.operations.SlideshowPropertiesOperation;
import com.bdaum.zoom.ui.internal.wizards.MergeCatWizard;

@SuppressWarnings("restriction")
public class CatalogView extends AbstractCatalogView implements IPerspectiveListener {

	public final class CatalogDragSourceListener implements DragSourceListener {
		public void dragStart(DragSourceEvent event) {
			ISelection selection = viewer.getSelection();
			Object firstElement = ((IStructuredSelection) selection).getFirstElement();
			if (firstElement != null) {
				boolean canDrag = false;
				IDbManager dbManager = Core.getCore().getDbManager();
				if (firstElement instanceof SmartCollection) {
					String groupId = ((SmartCollection) firstElement).getGroup_rootCollection_parent();
					if (groupId != null) {
						Group group = dbManager.obtainById(GroupImpl.class, groupId);
						canDrag = (group != null && !group.getSystem());
					}
				} else if (firstElement instanceof Group)
					canDrag = (((Group) firstElement).getGroup_subgroup_parent() != null);
				else if (firstElement instanceof Exhibition)
					canDrag = dbManager.exists(GroupImpl.class,
							((Exhibition) firstElement).getGroup_exhibition_parent());
				else if (firstElement instanceof SlideShow)
					canDrag = dbManager.exists(GroupImpl.class, ((SlideShow) firstElement).getGroup_slideshow_parent());
				else if (firstElement instanceof WebGallery)
					canDrag = dbManager.exists(GroupImpl.class,
							((WebGallery) firstElement).getGroup_webGallery_parent());
				if (canDrag) {
					event.doit = true;
					selectionTransfer.setSelection(selection);
					event.detail = DND.DROP_MOVE;
					return;
				}
			}
			event.doit = false;
		}

		public void dragSetData(DragSourceEvent event) {
			// do nothing
		}

		public void dragFinished(DragSourceEvent event) {
			selectionTransfer.setSelection(null);
		}
	}

	public final class CatalogDropTargetListener extends EffectDropTargetListener {
		public CatalogDropTargetListener(Control control) {
			super(control);
		}

		@Override
		public void dragEnter(DropTargetEvent event) {
			int detail = event.detail;
			event.detail = DND.DROP_NONE;
			if (!dbIsReadonly()) {
				boolean match = false;
				for (int i = 0; i < event.dataTypes.length; i++) {
					if (selectionTransfer.isSupportedType(event.dataTypes[i])) {
						event.currentDataType = event.dataTypes[i];
						ISelection selection = selectionTransfer.getSelection();
						if (selection instanceof TreeSelection) {
							if ((detail & DND.DROP_MOVE) != 0)
								event.detail = DND.DROP_MOVE;
							super.dragEnter(event);
							return;
						}
						AssetSelection selectedAssets = getNavigationHistory().getSelectedAssets();
						if (!selectedAssets.isEmpty()) {
							selectionTransfer.setSelection(selectedAssets);
							match = true;
							break;
						}
					}
				}
				if (!match)
					for (int i = 0; i < event.dataTypes.length; i++)
						if (fileTransfer.isSupportedType(event.dataTypes[i])) {
							event.currentDataType = event.dataTypes[i];
							match = true;
							break;
						}
				if (match && (detail & OPERATIONS) != 0)
					event.detail = DND.DROP_COPY;
				super.dragEnter(event);
			}
		}

		@Override
		public void dragOver(DropTargetEvent event) {
			int detail = event.detail;
			event.detail = DND.DROP_NONE;
			if (!dbIsReadonly()) {
				event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
				ISelection selection = selectionTransfer.getSelection();
				if (selection instanceof TreeSelection) {
					if (canBeDroppedInGroup(event, (TreeSelection) selection))
						event.detail = DND.DROP_MOVE;
					return;
				}
				if (selectionTransfer.isSupportedType(event.currentDataType))
					if (canBeDropped(event))
						if ((detail & OPERATIONS) != 0 || detail == DND.DROP_NONE)
							event.detail = DND.DROP_COPY;
						else if (fileTransfer.isSupportedType(event.currentDataType) && gpxCanBeDropped(event))
							if ((detail & OPERATIONS) != 0 || detail == DND.DROP_NONE)
								event.detail = DND.DROP_COPY;
			}
		}

		private boolean canBeDroppedInGroup(DropTargetEvent event, TreeSelection selection) {
			Widget item = event.item;
			boolean canDrop = false;
			if (item != null) {
				Object data = item.getData();
				if (data instanceof Group) {
					Group target = ((Group) data);
					canDrop = !target.getSystem();
					if (canDrop) {
						Object firstElement = selection.getFirstElement();
						if (firstElement instanceof Group) {
							Group group = (Group) firstElement;
							if (group == target)
								canDrop = false;
							else if (group.getGroup_subgroup_parent() == target)
								canDrop = false;
						} else {
							boolean exhibition = !target.getExhibition().isEmpty();
							boolean slideshow = !target.getSlideshow().isEmpty();
							boolean webgallery = target.getWebGallery() != null && !target.getWebGallery().isEmpty();
							boolean collection = !target.getRootCollection().isEmpty();
							String groupId = target.getStringId();
							if (!exhibition && !slideshow && !webgallery && !collection) {
								exhibition = Constants.GROUP_ID_EXHIBITION.equals(groupId);
								slideshow = Constants.GROUP_ID_SLIDESHOW.equals(groupId);
								webgallery = Constants.GROUP_ID_WEBGALLERY.equals(groupId);
								if (!exhibition && !slideshow && !webgallery)
									collection = exhibition = slideshow = webgallery = true;
							}
							if (firstElement instanceof Exhibition)
								canDrop &= exhibition;
							if (firstElement instanceof SlideShow)
								canDrop &= slideshow;
							if (firstElement instanceof WebGallery)
								canDrop &= webgallery;
							if (firstElement instanceof SmartCollection)
								canDrop &= collection;
						}
					}
				}
			}
			return canDrop;
		}

		private boolean gpxCanBeDropped(DropTargetEvent event) {
			Widget item = event.item;
			return (item != null && (item.getData() instanceof SmartCollectionImpl));
		}

		private boolean canBeDropped(DropTargetEvent event) {
			Widget item = event.item;
			boolean canDrop = false;
			if (item != null) {
				Object data = item.getData();
				if (data instanceof SmartCollectionImpl) {
					SmartCollectionImpl coll = ((SmartCollectionImpl) data);
					if (coll.getAlbum())
						canDrop = true;
					else {
						String collId = coll.getStringId();
						int p = collId.indexOf('=');
						if (p > 0) {
							String key = collId.substring(0, p);
							canDrop = key.equals(QueryField.RATING.getKey())
									|| (key.equals(QueryField.URI.getKey())
											&& coll.getSmartCollection_subSelection_parent() != null)
									|| key.equals(QueryField.IPTC_CATEGORY.getKey());
						}
					}
				}
			}
			return canDrop;
		}

		public void dragOperationChanged(DropTargetEvent event) {
			if (!dbIsReadonly()) {
				if (selectionTransfer.isSupportedType(event.currentDataType)) {
					if ((event.detail & OPERATIONS) == 0)
						event.detail = DND.DROP_NONE;
					return;
				}
				if (fileTransfer.isSupportedType(event.currentDataType)) {
					event.detail = ((event.detail & OPERATIONS) == 0) ? DND.DROP_NONE : DND.DROP_COPY;
					return;
				}
			}
			event.detail = DND.DROP_NONE;
		}

		public void drop(DropTargetEvent event) {
			if (dbIsReadonly())
				return;
			ISelection element = selectionTransfer.getSelection();
			if (element instanceof TreeSelection) {
				if (canBeDroppedInGroup(event, (TreeSelection) element)) {
					IDbManager dbManager = Core.getCore().getDbManager();
					List<Object> toBeStored = new ArrayList<Object>(3);
					Object movedElement = ((TreeSelection) element).getFirstElement();
					Widget item = event.item;
					Object data = item.getData();
					Group target = ((Group) data);
					toBeStored.add(movedElement);
					toBeStored.add(target);
					if (movedElement instanceof Group) {
						Group group = (Group) movedElement;
						Group parent = group.getGroup_subgroup_parent();
						if (parent != null && parent.getSubgroup() != null) {
							parent.removeSubgroup(group);
							toBeStored.add(parent);
						}
						group.setGroup_subgroup_parent(target);
						if (target.getSubgroup() == null)
							target.setSubgroup(Collections.singletonList(group));
						else
							target.addSubgroup(group);
					} else if (movedElement instanceof SmartCollection) {
						SmartCollection sm = (SmartCollection) movedElement;
						String parentId = sm.getGroup_rootCollection_parent();
						if (parentId != null) {
							Group parent = dbManager.obtainById(Group.class, parentId);
							if (parent != null) {
								parent.removeRootCollection(sm.getStringId());
								toBeStored.add(parent);
							}
							sm.setGroup_rootCollection_parent(target.getStringId());
							target.addRootCollection(sm.getStringId());
						}
					} else if (movedElement instanceof Exhibition) {
						Exhibition exhibition = (Exhibition) movedElement;
						String parentId = exhibition.getGroup_exhibition_parent();
						if (parentId != null) {
							Group parent = dbManager.obtainById(Group.class, parentId);
							if (parent != null) {
								parent.removeExhibition(exhibition.getStringId());
								toBeStored.add(parent);
							}
							exhibition.setGroup_exhibition_parent(target.getStringId());
							target.addExhibition(exhibition.getStringId());
						}
					} else if (movedElement instanceof SlideShow) {
						SlideShow slideshow = (SlideShow) movedElement;
						String parentId = slideshow.getGroup_slideshow_parent();
						if (parentId != null) {
							Group parent = dbManager.obtainById(Group.class, parentId);
							if (parent != null) {
								parent.removeSlideshow(slideshow.getStringId());
								toBeStored.add(parent);
							}
							slideshow.setGroup_slideshow_parent(target.getStringId());
							target.addSlideshow(slideshow.getStringId());
						}
					} else if (movedElement instanceof WebGallery) {
						WebGallery gallery = (WebGallery) movedElement;
						String parentId = gallery.getGroup_webGallery_parent();
						if (parentId != null) {
							Group parent = dbManager.obtainById(Group.class, parentId);
							if (parent != null) {
								parent.removeWebGallery(gallery.getStringId());
								toBeStored.add(parent);
							}
							gallery.setGroup_webGallery_parent(target.getStringId());
							target.addWebGallery(gallery.getStringId());
						}
					}
					dbManager.safeTransaction(null, toBeStored);
					setInput();
					viewer.setSelection(new StructuredSelection(movedElement), true);
				}
			}
			if (selectionTransfer.isSupportedType(event.currentDataType)) {
				if (canBeDropped(event)) {
					Region region = null;
					List<Asset> assets = new ArrayList<>(1);
					if (element instanceof AssetSelection)
						assets = ((AssetSelection) element).getAssets();
					else if (element instanceof IStructuredSelection) {
						Object firstElement = ((IStructuredSelection) element).getFirstElement();
						if (firstElement instanceof ImageRegion) {
							ImageRegion imageRegion = (ImageRegion) firstElement;
							if (imageRegion != null) {
								Asset owner = imageRegion.owner;
								assets.add(owner);
								List<RegionImpl> set = Core.getCore().getDbManager().obtainObjects(RegionImpl.class,
										false, "asset_person_parent", owner.getStringId(), QueryField.EQUALS, //$NON-NLS-1$
										Constants.OID, imageRegion.regionId, QueryField.EQUALS);
								if (!set.isEmpty())
									region = set.get(0);
							}
						}
					}
					SmartCollectionImpl coll = (SmartCollectionImpl) event.item.getData();
					Criterion criterion = coll.getCriterion(0);
					Object value = criterion.getValue();
					IProfiledOperation op = null;
					if (coll.getAlbum()) {
						if (!assets.isEmpty())
							op = new AddAlbumOperation(coll, assets, region);
					} else {
						String key = criterion.getField();
						if (QueryField.RATING.getKey().equals(key))
							op = new RateOperation(assets, ((Integer) value));
						else if (QueryField.URI.getKey().equals(key))
							try {
								File folder = new File(new URI((String) value));
								folder.mkdirs(); // Just in case
								op = new MoveOperation(assets, folder);
							} catch (URISyntaxException e) {
								Core.getCore().logError(Messages.getString("CatalogView.bad_uri_for_target_folder"), e); //$NON-NLS-1$
							}
						else if (QueryField.IPTC_CATEGORY.getKey().equals(key))
							op = new MultiModifyAssetOperation(QueryField.IPTC_CATEGORY, (value), null, assets);
					}
					if (op != null)
						OperationJob.executeOperation(op, CatalogView.this);
				}
			} else if (fileTransfer.isSupportedType(event.currentDataType)) {
				String[] filenames = (String[]) event.data;
				if (filenames.length == 1 && filenames[0].endsWith(Constants.CATALOGEXTENSION)) {
					MergeCatWizard wizard = new MergeCatWizard();
					WizardDialog wizardDialog = new WizardDialog(getSite().getShell(), wizard);
					wizard.init(getSite().getWorkbenchWindow().getWorkbench(), new StructuredSelection(filenames[0]));
					wizardDialog.open();
					return;
				}
				FileNameExtensionFilter filter = UiActivator.getDefault().createGpsFileFormatFilter();
				List<File> gpx = new ArrayList<File>();
				for (int j = 0; j < filenames.length; j++) {
					File file = new File(filenames[j]);
					if (filter.accept(file))
						gpx.add(file);
				}
				if (!gpx.isEmpty() && gpxCanBeDropped(event)) {
					SmartCollectionImpl collection = (SmartCollectionImpl) event.item.getData();
					List<String> ids = new ArrayList<String>(3000);
					for (Asset asset : Core.getCore().getDbManager().createCollectionProcessor(collection)
							.select(false))
						ids.add(asset.getStringId());
					IDropinHandler handler = UiActivator.getDefault().getDropinHandler("gps"); //$NON-NLS-1$
					if (handler != null)
						handler.handleDropin(gpx.toArray(new File[gpx.size()]), ids.toArray(new String[ids.size()]),
								CatalogView.this);
				}
			}
		}
	}

	private static final int OPERATIONS = DND.DROP_MOVE | DND.DROP_COPY;

	public static class CatalogComparator extends ViewerComparator {
		@Override
		@SuppressWarnings("unchecked")
		public int compare(Viewer viewer, Object e1, Object e2) {
			if (e1 instanceof SmartCollection && e2 instanceof SmartCollection) {
				SmartCollection c1 = (SmartCollection) e1;
				SmartCollection c2 = (SmartCollection) e2;
				if (c1.getSystem() && c2.getSystem() && !c1.getAlbum() && !c2.getAlbum() && !c1.getCriterion().isEmpty()
						&& !c2.getCriterion().isEmpty()) {
					Criterion crit1 = c1.getCriterion(0);
					Object value1 = crit1 == null ? null : crit1.getValue();
					if (value1 instanceof Range)
						value1 = ((Range) value1).getFrom();
					Criterion crit2 = c2.getCriterion(0);
					Object value2 = crit2 == null ? null : crit2.getValue();
					if (value2 instanceof Range)
						value2 = ((Range) value2).getFrom();
					if (value1 != null && value2 != null && value1.getClass().equals(value2.getClass()))
						return ((Comparable<Object>) value1).compareTo(value2);
				} else
					return (UiUtilities.isImport(c1)) ? c2.getName().compareTo(c1.getName())
							: c1.getName().compareToIgnoreCase(c2.getName());
			}
			return super.compare(viewer, e1, e2);
		}
	}

	public static final String ID = "com.bdaum.zoom.ui.views.CatalogView"; //$NON-NLS-1$
	private static final FileTransfer fileTransfer = FileTransfer.getInstance();
	private static final LocalSelectionTransfer selectionTransfer = LocalSelectionTransfer.getTransfer();
	private static final Transfer[] allTypes = new Transfer[] { fileTransfer, selectionTransfer };
	private static final Transfer[] selectionTypes = new Transfer[] { selectionTransfer };
	private static final String CATFILTERS = "catFilters"; //$NON-NLS-1$
	private static final String PERSONALBUMS = "persons"; //$NON-NLS-1$
	private static final String EMPTYPERSONALBUMS = "emptyPersons"; //$NON-NLS-1$
	private static final String RATINGS = "ratings"; //$NON-NLS-1$
	private static final String CATEGORIES = "categories"; //$NON-NLS-1$
	private static final String DIRECTORIES = "directories"; //$NON-NLS-1$
	private static final String TIMELINE = "timeline"; //$NON-NLS-1$
	private static final String LOCATIONS = "locations"; //$NON-NLS-1$
	private static final String PASTIMPORTS = "pastImports"; //$NON-NLS-1$

	private static final String[] ALLCATITEMS = new String[] { EMPTYPERSONALBUMS, PERSONALBUMS, CATEGORIES, RATINGS,
			DIRECTORIES, TIMELINE, LOCATIONS, PASTIMPORTS };

	private static final String[] CATITEMLABELS = new String[] { Messages.getString("CatalogView.empty_person_albums"), //$NON-NLS-1$
			Messages.getString("CatalogView.person_albums"), //$NON-NLS-1$
			Messages.getString("CatalogView.categories"), Messages.getString("CatalogView.ratings"), //$NON-NLS-1$ //$NON-NLS-2$
			Messages.getString("CatalogView.directories"), Messages.getString("CatalogView.timeline"), //$NON-NLS-1$ //$NON-NLS-2$
			Messages.getString("CatalogView.locations"), Messages.getString("CatalogView.past_imports"), }; //$NON-NLS-1$ //$NON-NLS-2$

	private DrillDownAdapter drillDownAdapter;
	private Action createCollectionAction, addSubCollectionAction, deleteAction, cutCollection, copyCollection,
			pasteCollection, createGroup, createSlideShowAction, collapseAction, propertiesAction,
			createExhibitionAction, createAlbumAction, createWebGalleryAction, filterAction, addSubAlbumAction,
			addSubGroupAction, expandAction;
	private Set<String> hiddenCatItems;
	private Clipboard clipboard;
	private boolean restoring;
	private Object[] expansions;
	private boolean settingSelection;
	private ISelection initialSelection;
	private boolean initialized;
	private int colorCode = -1;

	private ViewerFilter userFilter = new ViewerFilter() {

		@Override
		public boolean select(Viewer aViewer, Object parentElement, Object element) {
			if (element instanceof GroupImpl) {
				String groupId = ((GroupImpl) element).getStringId();
				return !(Constants.GROUP_ID_CATEGORIES.equals(groupId) && hiddenCatItems.contains(CATEGORIES)
						|| Constants.GROUP_ID_FOLDERSTRUCTURE.equals(groupId) && hiddenCatItems.contains(DIRECTORIES)
						|| Constants.GROUP_ID_PERSONS.equals(groupId) && hiddenCatItems.contains(PERSONALBUMS)
						|| Constants.GROUP_ID_RATING.equals(groupId) && hiddenCatItems.contains(RATINGS)
						|| Constants.GROUP_ID_TIMELINE.equals(groupId) && hiddenCatItems.contains(TIMELINE)
						|| Constants.GROUP_ID_LOCATIONS.equals(groupId) && hiddenCatItems.contains(LOCATIONS)
						|| Constants.GROUP_ID_RECENTIMPORTS.equals(groupId) && hiddenCatItems.contains(PASTIMPORTS));
			} else if (element instanceof SmartCollectionImpl) {
				SmartCollectionImpl coll = (SmartCollectionImpl) element;
				return !(hiddenCatItems.contains(EMPTYPERSONALBUMS) && coll.getAlbum() && coll.getSystem()
						&& coll.getAsset().isEmpty()
						|| hiddenCatItems.contains(PASTIMPORTS) && coll.getStringId().startsWith(IDbManager.IMPORTKEY));
			}
			return true;
		}
	};

	private ViewerFilter colorCodeFilter = new ViewerFilter() {

		@Override
		public boolean select(Viewer aViewer, Object parentElement, Object element) {
			if (colorCode <= Constants.COLOR_UNDEFINED)
				return true;
			if (element instanceof Group)
				return true;
			if (element instanceof SmartCollection) {
				if (((SmartCollection) element).getColorCode() - 1 == colorCode)
					return true;
				for (SmartCollection sub : ((SmartCollection) element).getSubSelection())
					if (select(aViewer, element, sub))
						return true;
				return false;
			}
			return true;
		}
	};

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		String textData = memento == null ? null : memento.getString(CATFILTERS);
		hiddenCatItems = new HashSet<String>();
		if (textData != null) {
			StringTokenizer st = new StringTokenizer(textData);
			while (st.hasMoreTokens())
				hiddenCatItems.add(st.nextToken());
		}
	}

	@Override
	public void saveState(IMemento memento) {
		memento.putString(CATFILTERS, Core.toStringList(hiddenCatItems, ' '));
		super.saveState(memento);
	}

	@Override
	public void createPartControl(final Composite parent) {
		undoContext = new UndoContext() {
			@Override
			public String getLabel() {
				return Messages.getString("CatalogView.catalog_undo_context"); //$NON-NLS-1$
			}
		};

		viewer = new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		drillDownAdapter = new DrillDownAdapter((TreeViewer) viewer);
		viewer.setContentProvider(new CatalogContentProvider());
		viewer.setLabelProvider(new CatalogLabelProvider(this));
		viewer.setFilters(new ViewerFilter[] { colorCodeFilter, userFilter });
		viewer.setComparator(new CatalogComparator());
		viewer.setComparer(IdentifiedElementComparer.getInstance());
		UiUtilities.installDoubleClickExpansion((TreeViewer) viewer);
		ColumnViewerToolTipSupport.enableFor(viewer);
		setInput();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), HelpContextIds.CATALOG_VIEW);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				if (!settingSelection) {
					Job.getJobManager().cancel(CatalogView.this);
					new SelectionJob(viewer, event).schedule();
				}
				if (cntrlDwn && editItemAction.isEnabled()) {
					editItemAction.run();
					cntrlDwn = false;
				}
			}
		});
		addCtrlKeyListener();
		viewer.addDropSupport(OPERATIONS, allTypes, new CatalogDropTargetListener(viewer.getControl()));
		viewer.addDragSupport(DND.DROP_MOVE, new Transfer[] { selectionTransfer }, new CatalogDragSourceListener());
		addKeyListener();
		Tree tree = ((TreeViewer) viewer).getTree();
		addGestureListener(tree);
		makeActions();
		installListeners(parent);
		hookContextMenu(viewer);
		contributeToActionBars();
		addDragDropClipboard(tree);
		getSite().getWorkbenchWindow().addPerspectiveListener(this);
		Core.getCore().addCatalogListener(new CatalogAdapter() {
			@Override
			public void structureModified() {
				Shell shell = getSite().getShell();
				if (shell != null && !shell.isDisposed())
					shell.getDisplay().asyncExec(() -> {
						if (!shell.isDisposed())
							refresh();
					});
			}

			@Override
			public void assetsModified(BagChange<Asset> changes, QueryField node) {
				forceSelectionUpdate();
			}

			@Override
			public void applyRules(Collection<? extends Asset> assets, QueryField node) {
				OperationJob.executeSlaveOperation(
						new AutoRuleOperation(UiActivator.getDefault().obtainAutoRules(), assets, node),
						CatalogView.this);
			}

			private void forceSelectionUpdate() {
				Shell shell = getSite().getShell();
				if (shell != null && !shell.isDisposed())
					shell.getDisplay().asyncExec(() -> {
						if (!shell.isDisposed()) {
							ISelection selection = viewer.getSelection();
							viewer.setSelection(StructuredSelection.EMPTY);
							settingSelection = true;
							viewer.setSelection(selection, true);
							settingSelection = false;
							fireSelection(new SelectionChangedEvent(CatalogView.this, selection));
						}
					});
			}

			@Override
			public void setCatalogSelection(final ISelection selection, final boolean forceUpdate) {
				try {
					Job.getJobManager().join(CatalogView.this, null);
				} catch (Exception e) {
					// do nothing
				}
				Shell shell = getSite().getShell();
				if (shell != null && !shell.isDisposed())
					shell.getDisplay().asyncExec(() -> {
						if (!shell.isDisposed())
							setSelection(selection, forceUpdate);
					});
			}

			@Override
			public void catalogOpened(boolean newDb) {
				Display display = getSite().getShell().getDisplay();
				display.syncExec(() -> BusyIndicator.showWhile(display, () -> {
					structureModified();
					restoreLastSelection();
				}));
			}

			@Override
			public void catalogClosed(int mode) {
				if (mode != CatalogListener.EMERGENCY)
					saveLastSelection();
			}
		});
		updateActions((IStructuredSelection) viewer.getSelection());
	}

	public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
		// do nothing
	}

	public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		if (!selection.isEmpty()) {
			Object selected = selection.getFirstElement();
			Object newSelection = null;
			for (IViewReference ref : page.getViewReferences()) {
				IViewPart view = ref.getView(false);
				if (view instanceof BasicView) {
					Object content = ((BasicView) view).getContent();
					if (selected == content)
						return;
					if (content instanceof SmartCollection)
						newSelection = content;
				}
			}
			if (newSelection != null)
				viewer.setSelection(new StructuredSelection(newSelection));
		}
	}

	private void saveLastSelection() {
		final IDbManager dbManager = Core.getCore().getDbManager();
		if (!viewer.getControl().isDisposed()) {
			final Meta meta = dbManager.getMeta(true);
			if (meta != null) {
				if (meta.getLastExpansion() == null)
					meta.setLastExpansion(new ArrayList<String>());
				else
					meta.clearLastExpansion();
				Object[] expandedElements = ((TreeViewer) viewer).getExpandedElements();
				for (Object element : expandedElements)
					if (element instanceof IdentifiableObject)
						meta.addLastExpansion(element.toString());
				Object firstElement = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
				SmartCollectionImpl selectedCollection = null;
				String lastSelection = null;
				if (firstElement instanceof IdentifiableObject) {
					if (firstElement instanceof SmartCollection) {
						for (IViewReference ref : getSite().getPage().getViewReferences()) {
							IViewPart view = ref.getView(false);
							if (view instanceof AbstractPresentationView
									&& ((AbstractPresentationView) view).isVisible()) {
								String currentPresentationId = ((AbstractPresentationView) view)
										.getCurrentPresentation();
								if (currentPresentationId != null) {
									selectedCollection = (SmartCollectionImpl) firstElement;
									lastSelection = currentPresentationId;
									break;
								}
							}
						}
						if (selectedCollection == null)
							lastSelection = firstElement.toString();
					} else {
						lastSelection = firstElement.toString();
						selectedCollection = getNavigationHistory().getSelectedCollection();
					}
					meta.setLastCollection(selectedCollection == null ? null : selectedCollection.getStringId());
					meta.setLastSelection(lastSelection);
				}
				dbManager.safeTransaction(null, meta);
			}
		}
	}

	private void restoreLastSelection() {
		// long time = System.currentTimeMillis();
		IDbManager dbManager = Core.getCore().getDbManager();
		Meta meta = dbManager.getMeta(true);
		if (meta != null) {
			String lastCollection = meta.getLastCollection();
			final SmartCollectionImpl sc = lastCollection != null
					? dbManager.obtainById(SmartCollectionImpl.class, lastCollection)
					: null;
			if (sc != null) {
				restoring = true;
				getNavigationHistory().fireSelection(null, new StructuredSelection(sc));
				restoring = false;
			}
			String lastSelection = meta.getLastSelection();
			final IdentifiableObject sm = lastSelection != null
					? dbManager.obtainById(IdentifiableObject.class, lastSelection)
					: null;
			List<String> lastExpansion = meta.getLastExpansion();
			if (sm != null || lastExpansion != null) {
				if (sm != null) {
					restoring = true;
					try {
						getNavigationHistory().postSelection(initialSelection = new StructuredSelection(sm));
						if (initialized)
							super.selectionChanged(null, initialSelection);
					} finally {
						restoring = false;
					}
				}
				if (lastExpansion != null)
					expansions = lastExpansion.toArray();
			}
		}
		// System.out.println("restore: " + (System.currentTimeMillis() -
		// time));
	}

	@Override
	protected void updateActions(IStructuredSelection selection) {
		if (deleteAction == null)
			return;
		super.updateActions(selection);
		boolean writable = !dbIsReadonly();
		createGroup.setEnabled(writable);
		if (selection.isEmpty()) {
			if (viewActive) {
				deleteAction.setEnabled(false);
				cutCollection.setEnabled(false);
				copyCollection.setEnabled(false);
			}
			createSlideShowAction.setEnabled(false);
			createWebGalleryAction.setEnabled(false);
			createExhibitionAction.setEnabled(false);
			createCollectionAction.setEnabled(false);
			createAlbumAction.setEnabled(false);
			addSubAlbumAction.setEnabled(false);
			addSubCollectionAction.setEnabled(false);
			addSubGroupAction.setEnabled(false);
			playSlideshowAction.setEnabled(false);
		} else {
			Object obj = selection.getFirstElement();
			if ((obj instanceof GroupImpl)) {
				GroupImpl group = (GroupImpl) obj;
				boolean exhibition = !group.getExhibition().isEmpty();
				boolean slideshow = !group.getSlideshow().isEmpty();
				boolean webgallery = group.getWebGallery() != null && !group.getWebGallery().isEmpty();
				boolean collection = !group.getRootCollection().isEmpty();
				String groupId = group.getStringId();
				if (!exhibition && !slideshow && !webgallery && !collection) {
					exhibition = Constants.GROUP_ID_EXHIBITION.equals(groupId);
					slideshow = Constants.GROUP_ID_SLIDESHOW.equals(groupId);
					webgallery = Constants.GROUP_ID_WEBGALLERY.equals(groupId);
					if (!exhibition && !slideshow && !webgallery)
						collection = exhibition = slideshow = webgallery = true;
				}
				addSubGroupAction.setEnabled(!group.getSystem());
				createWebGalleryAction.setEnabled(webgallery && writable);
				createSlideShowAction.setEnabled(slideshow && writable);
				createExhibitionAction.setEnabled(exhibition && writable);
				createCollectionAction.setEnabled(collection && writable);
				createAlbumAction.setEnabled(collection && writable);
				if (viewActive) {
					deleteAction.setEnabled(group.getRootCollection().isEmpty() && !group.getSystem() && writable);
					cutCollection.setEnabled(false);
					copyCollection.setEnabled(false);
				}
				addSubCollectionAction.setEnabled(false);
				addSubAlbumAction.setEnabled(false);
			} else {
				addSubGroupAction.setEnabled(false);
				createSlideShowAction.setEnabled(false);
				createExhibitionAction.setEnabled(false);
				createWebGalleryAction.setEnabled(false);
				createCollectionAction.setEnabled(false);
				createAlbumAction.setEnabled(false);
				if (obj instanceof SmartCollection) {
					SmartCollection coll = (SmartCollection) obj;
					if (viewActive) {
						boolean enabled = !coll.getSystem() && writable;
						boolean deleteEnabled = enabled;
						if (!enabled) {
							String collId = coll.getStringId();
							String key = QueryField.URI.getKey() + '=';
							if (collId.startsWith(key))
								try {
									deleteEnabled = !new File(new URI(collId.substring(key.length()))).exists();
								} catch (URISyntaxException e) {
									deleteEnabled = true;
								}
						}
						deleteAction.setEnabled(deleteEnabled);
						cutCollection.setEnabled(enabled);
						copyCollection.setEnabled(enabled);
					}
					boolean isFinal = (coll.getCriterion().size() == 1
							&& coll.getCriterion(0).getField().startsWith("*")); //$NON-NLS-1$
					addSubCollectionAction.setEnabled(!isFinal && writable);
					addSubAlbumAction.setEnabled(!coll.getSystem() && coll.getAlbum() && writable);
				} else {
					if (viewActive) {
						deleteAction.setEnabled(writable);
						cutCollection.setEnabled(writable);
						copyCollection.setEnabled(writable);
					}
					addSubCollectionAction.setEnabled(false);
				}
			}
		}
		Object contents = clipboard.getContents(LocalSelectionTransfer.getTransfer());
		pasteCollection.setEnabled((contents instanceof IStructuredSelection
				&& ((IStructuredSelection) contents).getFirstElement() instanceof SmartCollectionImpl) && writable);
	}

	@Override
	protected void setInput() {
		Shell shell = getSite().getWorkbenchWindow().getShell();
		String shellTitle = shell.getText();
		int p = shellTitle.indexOf(" - "); //$NON-NLS-1$
		if (p >= 0)
			shellTitle = shellTitle.substring(0, p);
		shellTitle += " - " + Core.getCore().getDbManager().getFileName(); //$NON-NLS-1$
		shell.setText(shellTitle);
		super.setInput();
	}

	@Override
	public void refresh() {
		if (isVisible()) {
			// long time = System.currentTimeMillis();
			Object[] expandedElements = expansions != null ? expansions : ((TreeViewer) viewer).getExpandedElements();
			expansions = null;
			viewer.setInput(GroupImpl.class);
			if (expandedElements != null && !restoring) {
				((TreeViewer) viewer).setExpandedElements(expandedElements);
				setSelection(initialSelection == null ? viewer.getSelection() : initialSelection);
				initialSelection = null;
			}
			initialized = true;
			// System.out.println("cat:" +
			// (System.currentTimeMillis()-time));
		}
	}

	@Override
	protected void setVisible(boolean visible) {
		if (isVisible() != visible) {
			super.setVisible(visible);
			if (visible && !initialized)
				refresh();
		}

	}

	@Override
	public void dispose() {
		if (clipboard != null)
			clipboard.dispose();
		super.dispose();
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
		new UndoRedoActionGroup(getViewSite(), PlatformUI.getWorkbench().getOperationSupport().getUndoContext(), true)
				.fillActionBars(bars);
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(createGroup);
		manager.add(new Separator());
		manager.add(editItemAction);
		manager.add(deleteAction);
		manager.add(new Separator());
		manager.add(propertiesAction);
		manager.add(new Separator());
		manager.add(filterAction);
		manager.add(expandAction);
		manager.add(collapseAction);
	}

	@Override
	protected void fillContextMenu(IMenuManager manager) {
		updateActions((IStructuredSelection) getSelection());
		boolean readOnly = dbIsReadonly();
		if (!readOnly) {
			addEnabled(manager, addSubGroupAction);
			addEnabled(manager, createCollectionAction);
			addEnabled(manager, addSubCollectionAction);
			addEnabled(manager, createAlbumAction);
			addEnabled(manager, addSubAlbumAction);
			if (createExhibitionAction.isEnabled() && createSlideShowAction.isEnabled()
					&& createWebGalleryAction.isEnabled()) {
				MenuManager subMenu = new MenuManager(Messages.getString("CatalogView.create_new_presentation"), //$NON-NLS-1$
						"new_presentation"); //$NON-NLS-1$
				manager.add(subMenu);
				subMenu.add(createExhibitionAction);
				subMenu.add(createSlideShowAction);
				subMenu.add(createWebGalleryAction);
			} else {
				addEnabled(manager, createExhibitionAction);
				addEnabled(manager, createSlideShowAction);
				addEnabled(manager, createWebGalleryAction);
			}
		}
		manager.add(new Separator());
		addEnabled(manager, playSlideshowAction);
		boolean selectall = false;
		IViewReference[] viewReferences = getSite().getPage().getViewReferences();
		for (IViewReference ref : viewReferences) {
			IWorkbenchPart part = ref.getPart(false);
			if (part instanceof SelectAllActionProvider) {
				IAction action = ((SelectAllActionProvider) part).getSelectAllAction();
				if (action != null && action.isEnabled()) {
					selectall = true;
					break;
				}
			}
		}
		if (selectall)
			manager.add(selectAllAction);
		addEnabled(manager, editItemAction);
		if (!readOnly) {
			manager.add(new Separator());
			addEnabled(manager, cutCollection);
			addEnabled(manager, copyCollection);
			addEnabled(manager, pasteCollection);
			addEnabled(manager, deleteAction);
		}
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(propertiesAction);
		manager.add(new Separator());
		manager.add(createGroup);
		manager.add(editItemAction);
		manager.add(deleteAction);
		manager.add(new Separator());
		manager.add(expandAction);
		manager.add(collapseAction);
		manager.add(filterAction);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}

	@Override
	protected void makeActions() {
		super.makeActions();

		createGroup = new Action(Messages.getString("CatalogView.create_new_group"), Icons.newGroup.getDescriptor()) { //$NON-NLS-1$
			@Override
			public void run() {
				GroupDialog dialog = new GroupDialog(getSite().getShell(), null, null);
				if (dialog.open() == Window.OK) {
					GroupImpl group = new GroupImpl(dialog.getName(), false, Constants.INHERIT_LABEL, null, 0, null);
					group.setAnnotations(dialog.getAnnotations());
					group.setShowLabel(dialog.getShowLabel());
					group.setLabelTemplate(dialog.getLabelTemplate());
					Core.getCore().getDbManager().safeTransaction(null, group);
					setInput();
					viewer.setSelection(new StructuredSelection(group), true);
				}
			}
		};
		createGroup.setToolTipText(Messages.getString("CatalogView.creates_a_group_for_collections")); //$NON-NLS-1$
		createSlideShowAction = new Action(Messages.getString("CatalogView.create_new_slide_show"), //$NON-NLS-1$
				Icons.newSlideshow.getDescriptor()) {
			@Override
			public void run() {
				Object obj = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
				if (obj instanceof GroupImpl) {
					SlideshowEditDialog dialog = new SlideshowEditDialog(getSite().getShell(), (GroupImpl) obj, null,
							Messages.getString("CatalogView.create_slideshow"), //$NON-NLS-1$
							false, false);
					if (dialog.open() == Window.OK) {
						SlideShowImpl result = dialog.getResult();
						((TreeViewer) viewer).add(obj, result);
						viewer.setSelection(new StructuredSelection(result), true);
						openSlideShowEditor(result, true);
					}
				}
			}
		};
		createSlideShowAction.setToolTipText(Messages.getString("CatalogView.creates_a_new_slideshow")); //$NON-NLS-1$

		createExhibitionAction = new Action(Messages.getString("CatalogView.create_new_exhibition"), //$NON-NLS-1$
				Icons.exhibition.getDescriptor()) {
			@Override
			public void run() {
				Object obj = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
				if (obj instanceof GroupImpl) {
					ExhibitionImpl show = ExhibitionEditDialog.open(getSite().getShell(), (GroupImpl) obj, null,
							Messages.getString("CatalogView.create_exhibition"), false, null); //$NON-NLS-1$
					if (show != null) {
						ExhibitionImpl result = show;
						((TreeViewer) viewer).add(obj, result);
						viewer.setSelection(new StructuredSelection(result), true);
						openExhibitionEditor(result, true);
					}
				}
			}
		};
		createExhibitionAction.setToolTipText(Messages.getString("CatalogView.creates_a_new_exhibition")); //$NON-NLS-1$
		createWebGalleryAction = new Action(Messages.getString("CatalogView.create_web_gallery"), //$NON-NLS-1$
				Icons.webGallery.getDescriptor()) {
			@Override
			public void run() {
				Object obj = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
				if (obj instanceof GroupImpl) {
					WebGalleryImpl result = WebGalleryEditDialog.openWebGalleryEditDialog(getSite().getShell(),
							(GroupImpl) obj, null, Messages.getString("CatalogView.web_gallery"), false, false, null); //$NON-NLS-1$
					if (result != null) {
						((TreeViewer) viewer).add(obj, result);
						viewer.setSelection(new StructuredSelection(result), true);
						openWebGalleryEditor(result, true);
					}
				}
			}
		};
		createWebGalleryAction.setToolTipText(Messages.getString("CatalogView.creates_a_new_web_gallery")); //$NON-NLS-1$

		createAlbumAction = new Action(Messages.getString("CatalogView.create_new_album"), //$NON-NLS-1$
				Icons.addAlbum.getDescriptor()) {
			@Override
			public void run() {
				Object obj = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
				if (obj instanceof GroupImpl) {
					CollectionEditDialog dialog = new CollectionEditDialog(getSite().getShell(), null,
							Messages.getString("CatalogView.create_album"), //$NON-NLS-1$
							Messages.getString("CatalogView.albums_msg"), //$NON-NLS-1$
							false, true, false, false);
					if (dialog.open() == Window.OK)
						insertCollection(dialog.getResult(), (GroupImpl) obj);
				}
			}
		};
		createAlbumAction.setToolTipText(Messages.getString("CatalogView.create_album_tooltip")); //$NON-NLS-1$

		addSubGroupAction = new Action(Messages.getString("CatalogView.add_subroup"), //$NON-NLS-1$
				Icons.newGroup.getDescriptor()) {
			@Override
			public void run() {
				Object obj = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
				if (obj instanceof Group && (!((Group) obj).getSystem())) {
					Group parent = (Group) obj;
					GroupDialog dialog = new GroupDialog(getSite().getShell(), null, parent);
					if (dialog.open() == Window.OK) {
						Group group = new GroupImpl(dialog.getName(), false, Constants.INHERIT_LABEL, null, 0, null);
						group.setAnnotations(dialog.getAnnotations());
						group.setShowLabel(dialog.getShowLabel());
						group.setLabelTemplate(dialog.getLabelTemplate());
						group.setGroup_subgroup_parent(parent);
						List<Group> subgroups = parent.getSubgroup();
						if (subgroups == null)
							parent.setSubgroup(Collections.singletonList(group));
						else
							parent.addSubgroup(group);
						List<Object> toBeStored = new ArrayList<Object>(2);
						toBeStored.add(group);
						toBeStored.add(parent);
						Core.getCore().getDbManager().safeTransaction(null, toBeStored);
						setInput();
						viewer.setSelection(new StructuredSelection(group), true);
					}
				}
			}
		};
		addSubGroupAction.setToolTipText(Messages.getString("CatalogView.add_subgroup_tooltip"));  //$NON-NLS-1$

		addSubAlbumAction = new Action(Messages.getString("CatalogView.add_sub_album"), //$NON-NLS-1$
				Icons.addAlbum.getDescriptor()) {
			@Override
			public void run() {
				Object obj = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
				if (obj instanceof SmartCollection) {
					SmartCollection sm = (SmartCollection) obj;
					SmartCollection parent = sm.getSmartCollection_subSelection_parent();
					if (sm.getAlbum() && !sm.getSystem()) {
						CollectionEditDialog dialog = new CollectionEditDialog(getSite().getShell(), null,
								NLS.bind(Messages.getString("CatalogView.add_sub_album"), parent.getName()), //$NON-NLS-1$
								Messages.getString("CatalogView.albums_msg"), //$NON-NLS-1$
								false, true, false, false);
						if (dialog.open() == Window.OK)
							addSubSelection((SmartCollection) obj, dialog.getResult());
					}
				}
			}
		};
		addSubAlbumAction.setToolTipText(Messages.getString("CatalogView.add_sub_album_tooltip")); //$NON-NLS-1$

		createCollectionAction = new Action(Messages.getString("CatalogView.create_new_collection"), //$NON-NLS-1$
				Icons.folder_add.getDescriptor()) {
			@Override
			public void run() {
				Object obj = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
				if (obj instanceof GroupImpl) {
					CollectionEditDialog dialog = new CollectionEditDialog(getSite().getShell(), null,
							Messages.getString("CatalogView.create_collection"), //$NON-NLS-1$
							Messages.getString("CatalogView.collection_msg"), //$NON-NLS-1$
							false, false, false, Core.getCore().isNetworked());
					if (dialog.open() == Window.OK)
						insertCollection(dialog.getResult(), (GroupImpl) obj);
				}
			}
		};
		createCollectionAction.setToolTipText(Messages.getString("CatalogView.creates_a_new_root_collection")); //$NON-NLS-1$

		addSubCollectionAction = new Action(Messages.getString("CatalogView.add_subselection"), //$NON-NLS-1$
				Icons.addSubselection.getDescriptor()) {
			@Override
			public void run() {
				Object obj = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
				if (obj instanceof SmartCollection) {
					SmartCollection parent = (SmartCollection) obj;
					CollectionEditDialog dialog = new CollectionEditDialog(getSite().getShell(), null,
							NLS.bind(Messages.getString("CatalogView.add_subselection"), parent.getName()), //$NON-NLS-1$
							Messages.getString("CatalogView.collection_msg"), //$NON-NLS-1$
							false, false, false, Core.getCore().isNetworked());
					if (dialog.open() == Window.OK)
						addSubSelection(parent, dialog.getResult());
				}
			}
		};
		addSubCollectionAction.setToolTipText(Messages.getString("CatalogView.narrow_down_selection")); //$NON-NLS-1$

		deleteAction = new Action(Messages.getString("CatalogView.delete"), Icons.folder_delete.getDescriptor()) { //$NON-NLS-1$
			@Override
			public void run() {
				Object obj = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
				if (obj instanceof SmartCollectionImpl) {
					SmartCollectionImpl current = (SmartCollectionImpl) obj;
					if (!current.getSystem()) {
						if (AcousticMessageDialog.openQuestion(getSite().getShell(),
								current.getAlbum() ? Messages.getString("CatalogView.delete_album") //$NON-NLS-1$
										: Messages.getString("CatalogView.delete_collection"), //$NON-NLS-1$
								current.getAlbum() ? Messages.getString("CatalogView.really_delete_album") //$NON-NLS-1$
										: Messages.getString("CatalogView.really_delete_collection"))) //$NON-NLS-1$
							deleteCollection(current);
					} else if (AcousticMessageDialog.openQuestion(getSite().getShell(),
							Messages.getString("CatalogView.delete_system_coll"), //$NON-NLS-1$
							Messages.getString("CatalogView.delete_system_coll_msg")))  //$NON-NLS-1$
						deleteCollection(current);
				} else if (obj instanceof SlideShowImpl) {
					SlideShowImpl current = (SlideShowImpl) obj;
					if (AcousticMessageDialog.openQuestion(getSite().getShell(),
							Messages.getString("CatalogView.delete_slideshow"), //$NON-NLS-1$
							Messages.getString("CatalogView.really_delete_slideshow"))) //$NON-NLS-1$
						deleteSlideshow(current);
				} else if (obj instanceof ExhibitionImpl) {
					ExhibitionImpl current = (ExhibitionImpl) obj;
					if (AcousticMessageDialog.openQuestion(getSite().getShell(),
							Messages.getString("CatalogView.delete_exhibition"), //$NON-NLS-1$
							Messages.getString("CatalogView.really_delete_exhibition"))) //$NON-NLS-1$
						deleteExhibition(current);
				} else if (obj instanceof WebGalleryImpl) {
					WebGalleryImpl current = (WebGalleryImpl) obj;
					if (AcousticMessageDialog.openQuestion(getSite().getShell(),
							Messages.getString("CatalogView.delete_web_gallery"), //$NON-NLS-1$
							Messages.getString("CatalogView.do_you_really_want_to_delete"))) //$NON-NLS-1$
						deleteWebGallery(current);
				} else if (obj instanceof GroupImpl) {
					final GroupImpl current = (GroupImpl) obj;
					if (!current.getSystem()) {
						boolean hasChildren = current.getSubgroup() != null && !current.getSubgroup().isEmpty()
								|| current.getExhibition() != null && !current.getExhibition().isEmpty()
								|| current.getSlideshow() != null && !current.getSlideshow().isEmpty()
								|| current.getWebGallery() != null && !current.getWebGallery().isEmpty()
								|| current.getRootCollection() != null && !current.getRootCollection().isEmpty();
						if (AcousticMessageDialog.openQuestion(getSite().getShell(),
								Messages.getString("CatalogView.delete_group"), //$NON-NLS-1$
								hasChildren ? Messages.getString("CatalogView.delete_group_with_contents") //$NON-NLS-1$
										: Messages.getString("CatalogView.really_delete_group"))) //$NON-NLS-1$
							deleteGroup(current);
					}
				}
			}
		};
		deleteAction.setToolTipText(Messages.getString("CatalogView.delete_selected_item")); //$NON-NLS-1$

		cutCollection = new Action(Messages.getString("CatalogView.cut"), Icons.cut.getDescriptor()) { //$NON-NLS-1$
			@Override
			public void run() {
				Object obj = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
				if (obj instanceof SmartCollectionImpl) {
					copyCollection.run();
					deleteCollection((SmartCollectionImpl) obj);
				}
			}
		};
		cutCollection.setToolTipText(Messages.getString("CatalogView.cut_selected_item")); //$NON-NLS-1$

		copyCollection = new Action(Messages.getString("CatalogView.copy"), Icons.copy.getDescriptor()) { //$NON-NLS-1$
			@Override
			public void run() {
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				Object obj = selection.getFirstElement();
				if (obj instanceof SmartCollection) {
					selectionTransfer.setSelection(selection);
					clipboard.setContents(new Object[] { obj }, selectionTypes, DND.CLIPBOARD);
					pasteCollection.setEnabled(true);
				}
			}
		};
		copyCollection.setToolTipText(Messages.getString("CatalogView.copy_selected_item")); //$NON-NLS-1$

		pasteCollection = new Action(Messages.getString("CatalogView.paste"), Icons.paste.getDescriptor()) { //$NON-NLS-1$

			@Override
			public void run() {
				Object obj = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
				if (obj instanceof GroupImpl) {
					Object contents = clipboard.getContents(selectionTransfer);
					if (contents instanceof IStructuredSelection) {
						Object o = ((IStructuredSelection) contents).getFirstElement();
						if (o instanceof SmartCollectionImpl)
							insertCollection(cloneCollection((SmartCollectionImpl) o), (GroupImpl) obj);
						else if (o instanceof SlideShowImpl) {
							SlideShowImpl newSm = SlideshowPropertiesOperation.cloneSlideshow((SlideShowImpl) o);
							final GroupImpl group = (GroupImpl) obj;
							group.addSlideshow(newSm.getStringId());
							newSm.setGroup_slideshow_parent(group.getStringId());
							Core.getCore().getDbManager().safeTransaction(null, group);
							((TreeViewer) viewer).add(group, newSm);
							viewer.setSelection(new StructuredSelection(newSm), true);
							openSlideShowEditor(newSm, true);
						}
					}
				} else if (obj instanceof SmartCollection) {
					SmartCollectionImpl parent = (SmartCollectionImpl) obj;
					Object contents = clipboard.getContents(selectionTransfer);
					if (contents instanceof IStructuredSelection) {
						Object o = ((IStructuredSelection) contents).getFirstElement();
						if (o instanceof SmartCollectionImpl)
							addSubSelection(parent, cloneCollection((SmartCollectionImpl) o));
					}
				}
			}

			private SmartCollectionImpl cloneCollection(SmartCollectionImpl oldSm) {
				SmartCollectionImpl newSm = new SmartCollectionImpl(oldSm.getName(), false, oldSm.getAlbum(),
						oldSm.getAdhoc(), oldSm.getNetwork(), null, oldSm.getColorCode(), oldSm.getLastAccessDate(), 0,
						oldSm.getPerspective(), oldSm.getShowLabel(), oldSm.getLabelTemplate(), oldSm.getFontSize(),
						null);
				for (Criterion oldCrit : oldSm.getCriterion())
					newSm.addCriterion(new CriterionImpl(oldCrit.getField(), oldCrit.getSubfield(), oldCrit.getValue(),
							oldCrit.getRelation(), oldCrit.getAnd()));
				for (SortCriterion oldCrit : oldSm.getSortCriterion())
					newSm.addSortCriterion(
							new SortCriterionImpl(oldCrit.getField(), oldCrit.getSubfield(), oldCrit.getDescending()));
				return newSm;
			}
		};
		pasteCollection.setToolTipText(Messages.getString("CatalogView.paste_selected_item")); //$NON-NLS-1$

		expandAction = new Action(Messages.getString("CatalogView.expand_all"), Icons.expandAll.getDescriptor()) { //$NON-NLS-1$
			@Override
			public void run() {
				((TreeViewer) viewer).expandAll();
			}
		};
		expandAction.setToolTipText(Messages.getString("CatalogView.expand_all_tooltip")); //$NON-NLS-1$
		collapseAction = new Action(Messages.getString("CatalogView.collapse_all"), Icons.collapseAll.getDescriptor()) { //$NON-NLS-1$
			@Override
			public void run() {
				((TreeViewer) viewer).collapseAll();
			}
		};
		collapseAction.setToolTipText(Messages.getString("CatalogView.collapse_all_tree_items")); //$NON-NLS-1$
		propertiesAction = new Action(Messages.getString("CatalogView.properties"), Icons.properties.getDescriptor()) { //$NON-NLS-1$

			@Override
			public void run() {
				IDbManager dbManager = Core.getCore().getDbManager();
				if (dbManager.getFile() != null)
					new EditMetaDialog(getSite().getShell(), getSite().getPage(), dbManager, false, null).open();
			}
		};
		propertiesAction.setToolTipText(Messages.getString("CatalogView.edit_catalog_properties")); //$NON-NLS-1$

		filterAction = new Action(Messages.getString("CatalogView.filter"), Icons.catFilter.getDescriptor()) { //$NON-NLS-1$
			@Override
			public void run() {
				FilterDialog filterDialog = new FilterDialog(getSite().getShell(), colorCode, ALLCATITEMS,
						hiddenCatItems, CATITEMLABELS);
				if (filterDialog.open() == Dialog.OK) {
					colorCode = filterDialog.getColorCode();
					Icon icon = Icons.catFilter;
					Image image = (colorCode <= Constants.COLOR_UNDEFINED) ? icon.getImage()
							: icon.getColorOverlay(colorCode);
					filterAction.setImageDescriptor(ImageDescriptor.createFromImage(image));
					viewer.refresh();
				}
			}
		};
	}

	@Override
	protected void registerCommands() {
		registerCommand(propertiesAction, IZoomCommandIds.PropertiesCommand);
		registerCommand(pasteCollection, IWorkbenchCommandConstants.EDIT_PASTE);
		registerCommand(copyCollection, IWorkbenchCommandConstants.EDIT_COPY);
		registerCommand(cutCollection, IWorkbenchCommandConstants.EDIT_CUT);
		registerCommand(deleteAction, IZoomCommandIds.DeleteCommand);
		super.registerCommands();
	}

	protected void deleteGroup(Group current) {
		Set<Object> toBeStored = new HashSet<Object>();
		Set<Object> toBeDeleted = new HashSet<Object>();
		IDbManager dbManager = Core.getCore().getDbManager();
		doDeleteGroup(current, toBeStored, toBeDeleted, dbManager);
		dbManager.safeTransaction(toBeDeleted, toBeStored);
		((TreeViewer) viewer).remove(current);
	}

	private void doDeleteGroup(Group current, Set<Object> toBeStored, Set<Object> toBeDeleted, IDbManager dbManager) {
		List<String> exhibitions = current.getExhibition();
		if (exhibitions != null)
			for (String id : exhibitions) {
				ExhibitionImpl exhibition = dbManager.obtainById(ExhibitionImpl.class, id);
				if (exhibition != null)
					doDeleteExhibition(exhibition, toBeStored, toBeDeleted, dbManager);
			}
		List<String> slideshows = current.getSlideshow();
		if (slideshows != null)
			for (String id : slideshows) {
				SlideShowImpl slideshow = dbManager.obtainById(SlideShowImpl.class, id);
				if (slideshow != null)
					doDeleteSlideshow(slideshow, toBeStored, toBeDeleted, dbManager);
			}
		List<String> galleries = current.getWebGallery();
		if (galleries != null)
			for (String id : galleries) {
				WebGalleryImpl gallery = dbManager.obtainById(WebGalleryImpl.class, id);
				if (gallery != null)
					doDeleteWebGallery(gallery, toBeStored, toBeDeleted, dbManager);
			}
		List<String> collections = current.getRootCollection();
		if (collections != null)
			for (String id : collections) {
				SmartCollectionImpl sm = dbManager.obtainById(SmartCollectionImpl.class, id);
				if (sm != null)
					doDeleteCollection(sm, toBeStored, toBeDeleted, dbManager);
			}
		List<Group> subgroups = current.getSubgroup();
		if (subgroups != null)
			for (Group subgroup : subgroups)
				doDeleteGroup(subgroup, toBeStored, toBeDeleted, dbManager);
		Group parent = current.getGroup_subgroup_parent();
		if (parent != null && parent.getSubgroup() != null)
			parent.removeSubgroup(current);
		toBeDeleted.add(current);
		toBeStored.add(parent);
	}

	private void deleteWebGallery(final WebGalleryImpl gallery) {
		Set<Object> toBeStored = new HashSet<Object>();
		Set<Object> toBeDeleted = new HashSet<Object>();
		final IDbManager dbManager = Core.getCore().getDbManager();
		Object sibling = doDeleteWebGallery(gallery, toBeStored, toBeDeleted, dbManager);
		dbManager.safeTransaction(toBeDeleted, toBeStored);
		refresh();
		if (sibling != null)
			viewer.setSelection(new StructuredSelection(sibling), true);
		tellHistoryView(gallery);
	}

	private Object doDeleteWebGallery(final WebGalleryImpl gallery, Set<Object> toBeStored, Set<Object> toBeDeleted,
			final IDbManager dbManager) {
		final Object parent = getParent(gallery);
		Object sibling = getSibling(parent, gallery);
		for (Storyboard storyboard : gallery.getStoryboard()) {
			toBeDeleted.addAll(dbManager.obtainByIds(WebExhibitImpl.class, storyboard.getExhibit()));
			toBeDeleted.add(storyboard);
		}
		for (WebParameter parameter : gallery.getParameter().values())
			toBeDeleted.add(parameter);
		dbManager.delete(gallery);
		if (parent instanceof GroupImpl) {
			((GroupImpl) parent).removeWebGallery(gallery.getStringId());
			toBeStored.add(parent);
		}
		return sibling;
	}

	protected void addDragDropClipboard(Control control) {
		clipboard = new Clipboard(control.getDisplay());
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */

	@Override
	public void setFocus() {
		getControl().setFocus();
	}

	public ISelection getSelection() {
		return viewer.getSelection();
	}

	public void setSelection(ISelection selection) {
		settingSelection = true;
		try {
			if (selection instanceof IStructuredSelection) {
				Object firstElement = ((IStructuredSelection) selection).getFirstElement();
				if ((firstElement instanceof SlideShow && ((SlideShow) firstElement).getAdhoc())
						|| (firstElement instanceof SmartCollection && ((SmartCollection) firstElement).getAdhoc()))
					selection = StructuredSelection.EMPTY;
				viewer.setSelection(selection, true);
				updateActions((IStructuredSelection) selection);
			}
		} finally {
			settingSelection = false;
		}
	}

	private void addSubSelection(final SmartCollection parent, SmartCollection subSelection) {
		parent.addSubSelection(subSelection);
		Core.getCore().getDbManager().safeTransaction(null, parent);
		((TreeViewer) viewer).add(parent, subSelection);
		((TreeViewer) viewer).expandToLevel(subSelection, 1);
		viewer.setSelection(new StructuredSelection(subSelection), true);
	}

	private void deleteCollection(final SmartCollectionImpl collection) {
		Set<Object> toBeStored = new HashSet<Object>();
		Set<Object> toBeDeleted = new HashSet<Object>();
		final IDbManager dbManager = Core.getCore().getDbManager();
		Object sibling = doDeleteCollection(collection, toBeDeleted, toBeStored, dbManager);
		dbManager.safeTransaction(toBeDeleted, toBeStored);
		refresh();
		if (sibling != null)
			viewer.setSelection(new StructuredSelection(sibling), true);
		tellHistoryView(collection);
	}

	private void tellHistoryView(IdentifiableObject obj) {
		HistoryView historyView = (HistoryView) getSite().getPage().findView(HistoryView.ID);
		if (historyView != null)
			historyView.removeItem(obj);
	}

	private Object doDeleteCollection(final SmartCollectionImpl collection, Set<Object> toBeDeleted,
			Set<Object> toBeStored, final IDbManager dbManager) {
		if (collection.getAlbum()) {
			if (!collection.getCriterion().isEmpty()) {
				AomList<String> assetIds = collection.getAsset();
				if (assetIds != null) {
					List<AssetImpl> set = dbManager.obtainByIds(AssetImpl.class, assetIds);
					if (!set.isEmpty())
						OperationJob.executeOperation(new RemoveAlbumOperation(collection, set), this);
				}
			}
		}
		final Object parent = getParent(collection);
		Object sibling = getSibling(parent, collection);
		Utilities.deleteCollection(collection, true, toBeDeleted);
		if (parent instanceof SmartCollection) {
			((SmartCollection) parent).removeSubSelection(collection);
			toBeStored.add(parent);
		} else if (parent instanceof GroupImpl) {
			((GroupImpl) parent).removeRootCollection(collection.getStringId());
			toBeStored.add(parent);
		}
		return sibling;
	}

	private Object getParent(Object obj) {
		return ((ITreeContentProvider) viewer.getContentProvider()).getParent(obj);
	}

	private Object getSibling(Object parent, Object obj) {
		if (parent != null) {
			Object[] children = ((ITreeContentProvider) viewer.getContentProvider()).getChildren(parent);
			for (int i = 0; i < children.length; i++) {
				if (children[i] == obj) {
					if (i < children.length - 1)
						return children[i + 1];
					if (i > 0)
						return children[i - 1];
				}
			}
		}
		return parent;
	}

	private void deleteSlideshow(final SlideShowImpl slideshow) {
		Set<Object> toBeStored = new HashSet<Object>();
		Set<Object> toBeDeleted = new HashSet<Object>();
		final IDbManager dbManager = Core.getCore().getDbManager();
		Object sibling = doDeleteSlideshow(slideshow, toBeDeleted, toBeStored, dbManager);
		dbManager.safeTransaction(toBeDeleted, toBeStored);
		refresh();
		if (sibling != null)
			viewer.setSelection(new StructuredSelection(sibling), true);
		tellHistoryView(slideshow);
	}

	private Object doDeleteSlideshow(final SlideShowImpl slideshow, Set<Object> toBeDeleted, Set<Object> toBeStored,
			final IDbManager dbManager) {
		final Object parent = getParent(slideshow);
		Object sibling = getSibling(parent, slideshow);
		toBeDeleted.addAll(dbManager.obtainByIds(SlideImpl.class, slideshow.getEntry()));
		toBeDeleted.add(slideshow);
		if (parent instanceof GroupImpl) {
			((GroupImpl) parent).removeSlideshow(slideshow.getStringId());
			toBeStored.add(parent);
		}
		return sibling;
	}

	private void deleteExhibition(final ExhibitionImpl exhibition) {
		Set<Object> toBeStored = new HashSet<Object>();
		Set<Object> toBeDeleted = new HashSet<Object>();
		final IDbManager dbManager = Core.getCore().getDbManager();
		Object sibling = doDeleteExhibition(exhibition, toBeStored, toBeDeleted, dbManager);
		dbManager.safeTransaction(toBeDeleted, toBeStored);
		refresh();
		if (sibling != null)
			viewer.setSelection(new StructuredSelection(sibling), true);
		tellHistoryView(exhibition);
	}

	private Object doDeleteExhibition(final ExhibitionImpl exhibition, Set<Object> toBeStored, Set<Object> toBeDeleted,
			final IDbManager dbManager) {
		final Object parent = getParent(exhibition);
		Object sibling = getSibling(parent, exhibition);
		for (Wall wall : exhibition.getWall()) {
			toBeDeleted.addAll(dbManager.obtainByIds(ExhibitImpl.class, wall.getExhibit()));
			toBeDeleted.add(wall);
		}
		toBeDeleted.add(exhibition);
		if (parent instanceof GroupImpl) {
			((GroupImpl) parent).removeExhibition(exhibition.getStringId());
			toBeStored.add(parent);
		}
		return sibling;
	}

	private void insertCollection(final SmartCollectionImpl result, final GroupImpl group) {
		final IDbManager dbManager = Core.getCore().getDbManager();
		group.addRootCollection(result.getStringId());
		result.setGroup_rootCollection_parent(group.getStringId());
		dbManager.safeTransaction(() -> {
			dbManager.store(result);
			dbManager.store(group);
		});
		((TreeViewer) viewer).add(group, result);
		viewer.setSelection(new StructuredSelection(result), true);
	}

	@Override
	public Object findObject(MouseEvent event) {
		ViewerCell cell = viewer.getCell(new Point(event.x, event.y));
		return cell == null ? null : cell.getElement();
	}

	@Override
	public IGalleryHover getGalleryHover(MouseEvent event) {
		// return new GalleryHover();
		return null;
	}

	public Control getControl() {
		return viewer.getControl();
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
	public boolean selectionChanged() {
		return true;
	}

	@Override
	public void collectionChanged(IWorkbenchPart part, IStructuredSelection selection) {
		if (part != this && selection != null && !selection.isEmpty())
			viewer.setSelection(selection, true);
	}

}