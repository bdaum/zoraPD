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

package com.bdaum.zoom.ui;

import java.util.List;
import java.util.Stack;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.SortCriterion;
import com.bdaum.zoom.core.IAssetFilter;
import com.bdaum.zoom.ui.internal.NavigationHistory.HistoryItem;
import com.bdaum.zoom.ui.internal.views.AbstractGalleryView;
import com.bdaum.zoom.ui.internal.views.EducatedSelectionListener;

/**
 * Interface describing navigation history instances
 *
 */
public interface INavigationHistory {

	void registerSelectionProvider(ISelectionProvider provider);

	void deregisterSelectionProvicder(
			ISelectionProvider provider);

	void postSelection(ISelection selection);

	void updateFilters(IAssetFilter remove, IAssetFilter add);

	void sortChanged(SortCriterion customSort);

	void resetHistory();

	/**
	 * Fires a selection event to the given listeners without adding it to the history log.
	 *
	 * @param part
	 *            the part or <code>null</code> if no active part
	 * @param sel
	 *            the selection or <code>null</code> if no active selection
	 */
	void fireSelection(final IWorkbenchPart part,
			final ISelection sel);

	/**
	 * Adds the given selection listener. Has no effect if an identical listener
	 * is already registered.
	 * <p>
	 * <b>Note:</b> listeners should be removed when no longer necessary. If
	 * not, they will be removed when the IServiceLocator used to acquire this
	 * service is disposed.
	 * </p>
	 *
	 * @param basicView
	 *            a selection listener
	 * @see #removeSelectionListener(ISelectionListener)
	 */
	void addSelectionListener(EducatedSelectionListener listener);

	/**
	 * Removes the given selection listener. Has no effect if an identical
	 * listener is not registered.
	 *
	 * @param listener
	 *            a selection listener
	 */
	void removeSelectionListener(
			EducatedSelectionListener listener);

	void traverse(String partId, int incr);

	boolean canGoBack();

	void back();

	boolean canGoForward();

	void forward();

	SmartCollectionImpl getSelectedCollection();

	void setSelectedCollection(
			SmartCollectionImpl selectedCollection);

	AssetSelection getSelectedAssets();

	void setSelectedAssets(AssetSelection selectedAssets);

	IStructuredSelection getOtherSelection();

	void setOtherSelection(IStructuredSelection otherSelection);

	void addHistoryListener(HistoryListener listener);

	IAssetFilter[] getFilters();

	Object getSelectedItem();

	SortCriterion getCustomSort();

	void selectionChanged(SelectionChangedEvent event);

	List<AbstractGalleryView> getOpenGalleries();

	void postCueChanged(Object object);

	Stack<HistoryItem> getBackwardList();

	void back(HistoryItem historyItem);

	Stack<HistoryItem> getForwardList();

	void forward(HistoryItem historyItem);

	String getLastPresentationView();

}