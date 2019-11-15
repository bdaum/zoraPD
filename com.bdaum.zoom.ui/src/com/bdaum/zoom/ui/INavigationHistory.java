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
import com.bdaum.zoom.ui.internal.views.EducatedSelectionListener;

/**
 * Interface describing navigation history instances
 * Navigation history instances are always local to workbench windows
 */
public interface INavigationHistory {

	/**
	 * Register a selection provider
	 * @param provider
	 */
	void registerSelectionProvider(ISelectionProvider provider);

	/**
	 * Deregister a selection provider
	 * @param provider
	 */
	void deregisterSelectionProvicder(ISelectionProvider provider);

	/**
	 * Post a new selection
	 * @param selection
	 */
	void postSelection(ISelection selection);

	/**
	 * Indicate that asset filters have been changed
	 * @param remove - remove filter or null
	 * @param add - added filter or null
	 */
	void updateFilters(IAssetFilter remove, IAssetFilter add);

	/**
	 * Indicate that the custom sort has changed
	 * @param customSort - new custom Sort
	 */
	void sortChanged(SortCriterion customSort);

	/**
	 * Reset the history, e.g when a new catalog is opened
	 */
	void resetHistory();

	/**
	 * Fires a selection event to the given listeners without adding it to the
	 * history log.
	 *
	 * @param part
	 *            the part or <code>null</code> if no active part
	 * @param sel
	 *            the selection or <code>null</code> if no active selection
	 */
	void fireSelection(final IWorkbenchPart part, final ISelection sel);

	/**
	 * Adds the given selection listener. Has no effect if an identical listener is
	 * already registered.
	 * <p>
	 * <b>Note:</b> listeners should be removed when no longer necessary. If not,
	 * they will be removed when the IServiceLocator used to acquire this service is
	 * disposed.
	 * </p>
	 *
	 * @param basicView
	 *            a selection listener
	 * @see #removeSelectionListener(ISelectionListener)
	 */
	void addSelectionListener(EducatedSelectionListener listener);

	/**
	 * Removes the given selection listener. Has no effect if an identical listener
	 * is not registered.
	 *
	 * @param listener
	 *            a selection listener
	 */
	void removeSelectionListener(EducatedSelectionListener listener);

	/**
	 * Traverses the part activation beginning with the specified part by incr steps
	 * @param partId
	 * @param incr
	 */
	void traverse(String partId, int incr);

	/**
	 * @return true if we can go back in history
	 */
	boolean canGoBack();

	/**
	 * Moves back in history
	 */
	void back();

	/**
	 * @return true if we can go forward in history
	 */
	boolean canGoForward();

	/**
	 * Moves forward in history
	 */
	void forward();
	
	/**
	 * @return all previous history items
	 */
	Stack<HistoryItem> getBackwardList();

	/**
	 * Return the the specified history item
	 * @param historyItem
	 */
	void back(HistoryItem historyItem);

	/**
	 * @return all next history items
	 */
	Stack<HistoryItem> getForwardList();

	/**
	 * Go to the specified history item
	 * @param historyItem
	 */
	void forward(HistoryItem historyItem);

	/**
	 * @return the currently selected collection
	 */
	SmartCollectionImpl getSelectedCollection();

	/**
	 * Sets a new selected collection
	 * @param selectedCollection
	 */
	void setSelectedCollection(SmartCollectionImpl selectedCollection);

	/**
	 * @return the currently selected assets
	 */
	AssetSelection getSelectedAssets();

	/**
	 * Sets a new asset selection
	 * @param selectedAssets
	 */
	void setSelectedAssets(AssetSelection selectedAssets);

	/**
	 * @return a selection other than an asset selection or a collection selection
	 */
	IStructuredSelection getOtherSelection();

	/**
	 * Set a selection other than an asset selection or a collection selection
	 * @param otherSelection
	 */
	void setOtherSelection(IStructuredSelection otherSelection);

	/**
	 * Adds an history listener that is informed when the history changes
	 * @param listener
	 */
	void addHistoryListener(HistoryListener listener);

	/**
	 * Removes an history listener
	 * @param listener
	 */
	void removeHistoryListener(HistoryListener listener);

	/**
	 * @return the current asset filters
	 */
	IAssetFilter[] getFilters();

	/**
	 * @return the currently selected item
	 */
	Object getSelectedItem();

	/**
	 * @return the current custom sort
	 */
	SortCriterion getCustomSort();

	/**
	 * Indicates a selection change
	 * @param event
	 */
	void selectionChanged(SelectionChangedEvent event);

	/**
	 * Indicate a cue change
	 * @param object - the base object that has changed
	 */
	void postCueChanged(Object object);


	/**
	 * @return the most recent active presentation view
	 */
	String getLastPresentationView();

	/**
	 * @return a list of currently open part ids
	 */
	List<String> getOpenParts();

}