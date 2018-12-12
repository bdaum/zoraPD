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
 * (c) 2009 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.SortCriterion;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitionImpl;
import com.bdaum.zoom.cat.model.group.slideShow.SlideShowImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebGalleryImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IAssetFilter;
import com.bdaum.zoom.core.db.ICollectionProcessor;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.HistoryListener;
import com.bdaum.zoom.ui.INavigationHistory;
import com.bdaum.zoom.ui.internal.views.AbstractGalleryView;
import com.bdaum.zoom.ui.internal.views.AbstractPresentationView;
import com.bdaum.zoom.ui.internal.views.BasicView;
import com.bdaum.zoom.ui.internal.views.EducatedSelectionListener;
import com.bdaum.zoom.ui.internal.views.SelectionActionClusterProvider;

@SuppressWarnings("restriction")
public class NavigationHistory implements IPerspectiveListener, ISelectionChangedListener, INavigationHistory {

	private static final String PRESENTATIONPERSPECTIVEID = "com.bdaum.zoom.PresentationPerspective"; //$NON-NLS-1$

	public static class HistoryItem {
		private Object query; // String id if in database
		private String perspectiveId;
		private String[] selectedAssets;
		private String partId, secondaryId;
		private IAssetFilter[] filters;
		private SortCriterion sort;
		private final int generation;

		public HistoryItem(Object query, int generation, String perspectiveId, IAssetFilter[] filters,
				SortCriterion sort) {
			this.query = query;
			this.generation = generation;
			this.perspectiveId = perspectiveId;
			this.filters = filters;
			this.sort = sort;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!(obj instanceof HistoryItem))
				return false;
			HistoryItem other = (HistoryItem) obj;
			if (generation != other.generation)
				return false;
			if (perspectiveId != other.perspectiveId) {
				if (perspectiveId == null)
					return false;
				if (!perspectiveId.equals(other.perspectiveId))
					return false;
			}
			if (secondaryId != other.secondaryId) {
				if (secondaryId == null)
					return false;
				if (!secondaryId.equals(other.secondaryId))
					return false;
			}
			if (query != other.query) {
				if (query == null)
					return false;
				if (!query.equals(other.query))
					return false;
			}
			if (sort != other.sort) {
				if (sort == null)
					return false;
				if (!sort.equals(other.sort))
					return false;
			}
			return Arrays.equals(filters, other.filters);
		}

		@Override
		public int hashCode() {
			int hashCode = super.hashCode() * 31 + generation;
			hashCode = 31 * hashCode + (perspectiveId == null ? 0 : perspectiveId.hashCode());
			hashCode = 31 * hashCode + (secondaryId == null ? 0 : secondaryId.hashCode());
			hashCode = 31 * hashCode + (query == null ? 0 : query.hashCode());
			hashCode = 31 * hashCode + (sort == null ? 0 : sort.hashCode());
			return 31 * hashCode + (filters == null ? 0 : Arrays.hashCode(filters));
		}

		public Object getQuery() {
			return query;
		}

		public String getPerspectiveId() {
			return perspectiveId;
		}

		public String[] getSelectedAssets() {
			return selectedAssets;
		}

		public void setSelectedAssets(List<Asset> assets) {
			if (assets != null) {
				selectedAssets = new String[assets.size()];
				int i = 0;
				for (Asset asset : assets)
					selectedAssets[i++] = asset.getStringId();
			} else
				selectedAssets = null;
		}

		public void setActivePart(String partId) {
			this.partId = partId;
		}

		public String getActivePart() {
			return partId;
		}

		public void setSecondaryId(String secondaryId) {
			this.secondaryId = secondaryId;
		}

		public String getSecondaryId() {
			return secondaryId;
		}

		public boolean isPicked() {
			return selectedAssets != null;
		}

		public IAssetFilter[] getFilters() {
			return filters;
		}

		public SortCriterion getSort() {
			return sort;
		}

		public int getGeneration() {
			return generation;
		}
	}

	private Stack<HistoryItem> previousStack = new Stack<HistoryItem>();
	private Stack<HistoryItem> nextStack = new Stack<HistoryItem>();
	private IWorkbenchWindow window;
	private boolean restoring;
	private ListenerList<EducatedSelectionListener> selectionListeners = new ListenerList<EducatedSelectionListener>();
	private ListenerList<HistoryListener> historyListeners = new ListenerList<HistoryListener>();
	public IWorkbenchPart activePart;
	private SmartCollectionImpl selectedCollection;
	private AssetSelection selectedAssets = AssetSelection.EMPTY;
	private IStructuredSelection otherSelection = StructuredSelection.EMPTY;
	private IAssetFilter[] filters;
	private Object selectedItem;
	private SortCriterion customSort;
	protected List<String> openParts = new LinkedList<String>();
	private boolean forceSelection;
	private ISelection lastSelection;
	private IWorkbenchPart lastPart;
	private String lastPresentationView;

	public NavigationHistory(IWorkbenchWindow window) {
		this.window = window;
		register(window);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.INavigationHistory#registerSelectionProvider(org.
	 * eclipse .jface.viewers.ISelectionProvider)
	 */

	public void registerSelectionProvider(ISelectionProvider provider) {
		provider.addSelectionChangedListener(this);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.INavigationHistory#deregisterSelectionProvicder(org
	 * .eclipse.jface.viewers.ISelectionProvider)
	 */

	public void deregisterSelectionProvicder(ISelectionProvider provider) {
		provider.removeSelectionChangedListener(this);
	}

	public void selectionChanged(SelectionChangedEvent event) {
		if (updateHistory(activePart, event.getSelection()))
			fireSelection(activePart, event.getSelection());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.INavigationHistory#postSelection(org.eclipse.jface.
	 * viewers.ISelection)
	 */

	public void postSelection(ISelection selection) {
		if (updateHistory(activePart, selection) || forceSelection)
			fireSelection(null, selection);
		forceSelection = true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.INavigationHistory#filterChanged(com.bdaum.zoom.core
	 * .IAssetFilter)
	 */

	public void updateFilters(IAssetFilter remove, IAssetFilter add) {
		boolean changed = false;
		if (filters != null && remove != null)
			for (int i = 0; i < filters.length; i++) {
				if (filters[i].getClass().equals(remove.getClass())) {
					if (filters.length == 1)
						filters = null;
					else {
						IAssetFilter[] newFilters = new IAssetFilter[filters.length - 1];
						System.arraycopy(filters, 0, newFilters, 0, i);
						System.arraycopy(filters, i + 1, newFilters, i, newFilters.length - i);
						filters = newFilters;
					}
					changed = true;
					break;
				}
			}
		if (add != null) {
			if (filters == null) {
				filters = new IAssetFilter[] { add };
				changed = true;
			} else {
				boolean found = false;
				for (int i = 0; i < filters.length; i++)
					if (filters[i].getClass().equals(add.getClass())) {
						found = true;
						if (!filters[i].equals(add)) {
							filters[i] = add;
							changed = true;
						}
						break;
					}
				if (!found) {
					IAssetFilter[] newFilters = new IAssetFilter[filters.length + 1];
					System.arraycopy(filters, 0, newFilters, 0, filters.length);
					newFilters[filters.length] = add;
					filters = newFilters;
					changed = true;
				}
			}
		}
		updateHistory(filters);
		if (changed)
			for (EducatedSelectionListener listener : selectionListeners)
				try {
					listener.filterChanged();
				} catch (Exception e) {
					UiActivator.getDefault().logError(Messages.NavigationHistory_internal_error, e);
				}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.ui.INavigationHistory#sortChanged(com.bdaum.zoom.cat.model
	 * .group.SortCriterion)
	 */

	public void sortChanged(SortCriterion sortCrit) {
		boolean changed = (sortCrit == null && this.customSort != null)
				|| (sortCrit != null && !sortCrit.equals(this.customSort));
		this.customSort = sortCrit;
		updateHistory(sortCrit);
		if (changed)
			for (EducatedSelectionListener listener : selectionListeners)
				try {
					listener.sortChanged();
				} catch (Exception e) {
					UiActivator.getDefault().logError(Messages.NavigationHistory_internal_error, e);
				}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.INavigationHistory#resetHistory()
	 */

	public void resetHistory() {
		previousStack.clear();
		nextStack.clear();
		selectedAssets = AssetSelection.EMPTY;
		otherSelection = StructuredSelection.EMPTY;
		fireSelection(null, new StructuredSelection(ICollectionProcessor.EMPTYCOLLECTION));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.INavigationHistory#fireSelection(org.eclipse.ui.
	 * IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */

	public void fireSelection(final IWorkbenchPart part, final ISelection sel) {
		boolean assetsChanged = false;
		boolean collectionChanged = false;
		boolean selectionChanged = false;
		if (sel instanceof AssetSelection) {
			lastSelection = sel;
			lastPart = part;
			AssetSelection assetSelection = (AssetSelection) sel;
			assetsChanged = !assetSelection.equals(selectedAssets);
			if (assetsChanged)
				selectedAssets = assetSelection;
		} else if (sel instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) sel;
			Object first = ss.getFirstElement();
			if (first instanceof SmartCollection) {
				lastSelection = sel;
				lastPart = part;
				if (first != selectedCollection) {
					selectedCollection = (SmartCollectionImpl) first;
					assetsChanged = selectedAssets != AssetSelection.EMPTY;
					selectedAssets = AssetSelection.EMPTY;
					collectionChanged = true;
				}
			} else {
				selectedItem = first;
				if (!ss.equals(otherSelection)) {
					otherSelection = ss;
					selectionChanged = true;
				}
			}
		}
		for (EducatedSelectionListener l : selectionListeners) {
			try {
				if (collectionChanged)
					l.collectionChanged(part, (IStructuredSelection) sel);
				if (selectionChanged)
					l.selectionChanged(part, sel);
				if (assetsChanged)
					l.assetsChanged(part, selectedAssets);
			} catch (Exception e) {
				UiActivator.getDefault().logError(Messages.NavigationHistory_internal_error, e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.INavigationHistory#addSelectionListener(com.bdaum.zoom
	 * .ui.internal.views.EducatedSelectionListener)
	 */

	public void addSelectionListener(EducatedSelectionListener listener) {
		selectionListeners.add(listener);
		if (lastSelection instanceof AssetSelection)
			listener.assetsChanged(lastPart, (AssetSelection) lastSelection);
		else if (lastSelection instanceof IStructuredSelection)
			listener.collectionChanged(lastPart, (IStructuredSelection) lastSelection);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.INavigationHistory#removeSelectionListener(com.bdaum
	 * .zoom.ui.internal.views.EducatedSelectionListener)
	 */

	public void removeSelectionListener(EducatedSelectionListener listener) {
		selectionListeners.remove(listener);
	}

	private void register(IWorkbenchWindow aWindow) {
		aWindow.getPartService().addPartListener(new IPartListener() {

			public void partOpened(IWorkbenchPart part) {
				openParts.add(part.getSite().getId());
			}

			public void partDeactivated(IWorkbenchPart part) {
				if (part == activePart)
					activePart = null;
			}

			public void partClosed(IWorkbenchPart part) {
				openParts.remove(part.getSite().getId());
			}

			public void partBroughtToTop(IWorkbenchPart part) {
				// do nothing
			}

			public void partActivated(IWorkbenchPart part) {
				activePart = part;
				if (part instanceof AbstractPresentationView && !previousStack.isEmpty()
						&& PRESENTATIONPERSPECTIVEID.equals(previousStack.peek().getPerspectiveId())) // $NON-NLS-1$
					lastPresentationView = ((AbstractPresentationView) part).getId();
			}
		});
		IWorkbenchPage activePage = aWindow.getActivePage();
		if (activePage != null)
			activePart = activePage.getActivePart();
		aWindow.addPerspectiveListener(this);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.INavigationHistory#traverse(java.lang.String, int)
	 */

	public void traverse(String partId, int incr) {
		if (openParts.size() > 1) {
			String newId = null;
			int i = 0;
			for (String id : openParts) {
				if (id.equals(partId)) {
					int j = i + incr;
					if (j < 0)
						j = openParts.size() - 1;
					if (j >= openParts.size())
						j = 0;
					newId = openParts.get(j);
					break;
				}
				i++;
			}
			if (newId != null) {
				IWorkbenchPage activePage = window.getActivePage();
				if (activePage != null) {
					IViewPart view = activePage.findView(newId);
					if (view != null)
						activePage.activate(view);
				}
			}
		}
	}

	public List<AbstractGalleryView> getOpenGalleries() {
		List<AbstractGalleryView> galleries = new ArrayList<AbstractGalleryView>(3);
		IWorkbenchPage activePage = window.getActivePage();
		if (activePage != null)
			for (String id : openParts) {
				IViewPart view = activePage.findView(id);
				if (view instanceof AbstractGalleryView)
					galleries.add((AbstractGalleryView) view);
			}
		return galleries;
	}

	public boolean updateHistory(IWorkbenchPart part, ISelection selection) {
		if (restoring)
			return false;
		if (selection instanceof AssetSelection) {
			if (!selection.isEmpty() && !previousStack.isEmpty()) {
				AssetSelection assetSelection = (AssetSelection) selection;
				HistoryItem item = previousStack.peek();
				if (assetSelection.isPicked())
					item.setSelectedAssets(assetSelection.getAssets());
				else
					item.setSelectedAssets(null);
				if (part instanceof AbstractGalleryView) {
					item.setActivePart(((BasicView) part).getViewSite().getId());
					item.setSecondaryId(((BasicView) part).getViewSite().getSecondaryId());
				}
			}
			return true;
		}
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection) selection;
			if (sel.size() == 1 && sel.getFirstElement() instanceof IdentifiableObject) {
				String perspectiveId = part == null ? null : part.getSite().getPage().getPerspective().getId();
				IdentifiableObject obj = (IdentifiableObject) sel.getFirstElement();
				IDbManager dbManager = Core.getCore().getDbManager();
				Object o = null;
				Date now = new Date();
				if (obj instanceof SmartCollectionImpl) {
					((SmartCollectionImpl) obj).setLastAccessDate(now);
					((SmartCollectionImpl) obj).setPerspective(perspectiveId);
					o = obj.getStringId();
				} else if (obj instanceof SlideShowImpl) {
					((SlideShowImpl) obj).setLastAccessDate(now);
					((SlideShowImpl) obj).setPerspective(perspectiveId);
					o = obj.getStringId();
				} else if (obj instanceof ExhibitionImpl) {
					((ExhibitionImpl) obj).setLastAccessDate(now);
					((ExhibitionImpl) obj).setPerspective(perspectiveId);
					o = obj.getStringId();
				} else if (obj instanceof WebGalleryImpl) {
					((WebGalleryImpl) obj).setLastAccessDate(now);
					((WebGalleryImpl) obj).setPerspective(perspectiveId);
					o = obj.getStringId();
				}
				if (o != null) {
					boolean readOnly = dbManager.isReadOnly();
					dbManager.setReadOnly(false);
					if (obj instanceof SmartCollectionImpl && ((SmartCollectionImpl) obj).getAdhoc())
						dbManager.safeTransaction(null, Utilities.storeCollection((SmartCollection) obj, false, null));
					else
						dbManager.storeAndCommit(obj);
					dbManager.setReadOnly(readOnly);
					fireQueryHistoryChanged(obj);
				} else
					o = (dbManager.obtainById(IdentifiableObject.class, obj.getStringId()) != null) ? obj.getStringId()
							: obj;
				IAssetFilter[] oldfilters = previousStack.isEmpty() ? null : previousStack.peek().getFilters();
				SortCriterion oldSort = previousStack.isEmpty() ? null : previousStack.peek().getSort();
				return push(
						new HistoryItem(o, obj instanceof SmartCollection ? ((SmartCollection) obj).getGeneration() : 0,
								perspectiveId, oldfilters, oldSort));
			}
		}
		return false;
	}

	private void updateHistory(IAssetFilter[] assetFilters) {
		if (!previousStack.isEmpty()) {
			HistoryItem currentItem = previousStack.peek();
			push(new HistoryItem(currentItem.getQuery(), currentItem.getGeneration(), currentItem.getPerspectiveId(),
					assetFilters, currentItem.getSort()));
		}
	}

	private boolean push(HistoryItem item) {
		if (previousStack.isEmpty() || !previousStack.peek().equals(item)) {
			previousStack.push(item);
			nextStack.clear();
			fireHistoryChanged();
			return true;
		}
		return false;
	}

	private void updateHistory(SortCriterion sort) {
		if (!previousStack.isEmpty()) {
			HistoryItem currentItem = previousStack.peek();
			push(new HistoryItem(currentItem.getQuery(), currentItem.getGeneration(), currentItem.getPerspectiveId(),
					currentItem.getFilters(), sort));
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.INavigationHistory#canGoBack()
	 */

	public boolean canGoBack() {
		return (previousStack.size() >= 2);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.INavigationHistory#back()
	 */

	public void back() {
		if (canGoBack()) {
			HistoryItem pop = previousStack.pop();
			nextStack.push(pop);
			HistoryItem current = previousStack.peek();
			restoreState(current);
			fireHistoryChanged();
		}
	}

	public void back(HistoryItem historyItem) {
		while (canGoBack()) {
			HistoryItem pop = previousStack.pop();
			nextStack.push(pop);
			HistoryItem current = previousStack.peek();
			if (current == historyItem) {
				restoreState(current);
				fireHistoryChanged();
				break;
			}
		}
	}

	private void fireHistoryChanged() {
		for (HistoryListener listener : historyListeners)
			listener.historyChanged();
	}

	private void fireQueryHistoryChanged(IdentifiableObject obj) {
		for (HistoryListener listener : historyListeners)
			listener.queryHistoryChanged(obj);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.INavigationHistory#canGoForward()
	 */

	public boolean canGoForward() {
		return (!nextStack.isEmpty());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.INavigationHistory#forward()
	 */

	public void forward() {
		if (canGoForward()) {
			HistoryItem current = nextStack.pop();
			previousStack.push(current);
			restoreState(current);
			fireHistoryChanged();
		}
	}

	public void forward(HistoryItem historyItem) {
		while (canGoForward()) {
			HistoryItem current = nextStack.pop();
			previousStack.push(current);
			if (current == historyItem) {
				restoreState(current);
				fireHistoryChanged();
				break;
			}
		}

	}

	private void restoreState(HistoryItem current) {
		if (restoring)
			return;
		restoring = true;
		try {
			IDbManager dbManager = Core.getCore().getDbManager();
			this.filters = current.getFilters();
			this.customSort = current.getSort();
			IdentifiableObject collection = null;
			if (current.getQuery() instanceof String) {
				collection = dbManager.obtainById(IdentifiableObject.class, (String) current.query);
			} else if (current.getQuery() instanceof IdentifiableObject) {
				collection = (IdentifiableObject) current.getQuery();
			}
			if (collection != null) {
				IWorkbenchPage activePage = window.getActivePage();
				if (activePage != null) {
					String perspectiveId = current.getPerspectiveId();
					if (perspectiveId != null) {
						IPerspectiveDescriptor perspective = PlatformUI.getWorkbench().getPerspectiveRegistry()
								.findPerspectiveWithId(perspectiveId);
						if (perspective != null)
							activePage.setPerspective(perspective);
					}
					postSelection(new StructuredSelection(collection));
					String partId = current.getActivePart();
					IViewPart view = null;
					if (partId != null) {
						try {
							String secondaryId = current.getSecondaryId();
							view = secondaryId != null
									? activePage.showView(partId, secondaryId, IWorkbenchPage.VIEW_ACTIVATE)
									: activePage.showView(partId);
						} catch (PartInitException e) {
							// ignore
						}
					}
					if (current.isPicked()) {
						String[] selectedIds = current.getSelectedAssets();
						List<Asset> assets = new ArrayList<Asset>(selectedIds.length);
						for (String id : selectedIds) {
							Asset a = dbManager.obtainAsset(id);
							if (a != null)
								assets.add(a);
						}
						postSelection(new AssetSelection(assets));
					} else if (view instanceof SelectionActionClusterProvider)
						((SelectionActionClusterProvider) view).selectAll();
				}
			}
		} finally {
			restoring = false;
		}
	}

	public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		if (!previousStack.isEmpty()) {
			HistoryItem current = previousStack.peek();
			push(new HistoryItem(current.getQuery(), current.getGeneration(), perspective.getId(), current.getFilters(),
					current.getSort()));
		}
	}

	public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.INavigationHistory#getSelectedCollection()
	 */

	public SmartCollectionImpl getSelectedCollection() {
		return selectedCollection;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.INavigationHistory#setSelectedCollection(com.bdaum.
	 * zoom.cat.model.group.SmartCollectionImpl)
	 */

	public void setSelectedCollection(SmartCollectionImpl selectedCollection) {
		this.selectedCollection = selectedCollection;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.INavigationHistory#getSelectedAssets()
	 */

	public AssetSelection getSelectedAssets() {
		return selectedAssets;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.INavigationHistory#setSelectedAssets(com.bdaum.zoom
	 * .ui.AssetSelection)
	 */

	public void setSelectedAssets(AssetSelection selectedAssets) {
		this.selectedAssets = selectedAssets;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.INavigationHistory#getOtherSelection()
	 */

	public IStructuredSelection getOtherSelection() {
		return otherSelection;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.INavigationHistory#setOtherSelection(org.eclipse.jface
	 * .viewers.IStructuredSelection)
	 */

	public void setOtherSelection(IStructuredSelection otherSelection) {
		this.otherSelection = otherSelection;
	}

	public static AssetSelection getEMPTYASSETS() {
		return AssetSelection.EMPTY;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.INavigationHistory#addHistoryListener(com.bdaum.zoom
	 * .ui.HistoryListener)
	 */
	public void addHistoryListener(HistoryListener listener) {
		historyListeners.add(listener);
	}
	

	/* (nicht-Javadoc)
	 * @see com.bdaum.zoom.ui.INavigationHistory#removeHistoryListener(com.bdaum.zoom.ui.HistoryListener)
	 */
	@Override
	public void removeHistoryListener(HistoryListener listener) {
		historyListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.INavigationHistory#getFilter()
	 */
	public IAssetFilter[] getFilters() {
		return filters;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.INavigationHistory#getSelectedItem()
	 */

	public Object getSelectedItem() {
		return selectedItem;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.INavigationHistory#getCustomSort()
	 */

	public SortCriterion getCustomSort() {
		return customSort;
	}

	public void postCueChanged(Object object) {
		for (Object listener : selectionListeners.getListeners())
			((EducatedSelectionListener) listener).cueChanged(object);
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.INavigationHistory#getBackwardList()
	 */
	public Stack<HistoryItem> getBackwardList() {
		return previousStack;
	}

	public Stack<HistoryItem> getForwardList() {
		return nextStack;
	}

	@Override
	public String getLastPresentationView() {
		return lastPresentationView;
	}

}
