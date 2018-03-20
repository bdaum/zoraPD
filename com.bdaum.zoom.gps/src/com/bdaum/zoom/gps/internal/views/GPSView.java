/*******************************************************************************
 * Copyright (c) 2009 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/

package com.bdaum.zoom.gps.internal.views;

import org.eclipse.jface.action.IMenuManager;

import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.ui.internal.views.AbstractPropertiesView;

@SuppressWarnings("restriction")
public class GPSView extends AbstractPropertiesView {

	@Override
	protected Object getFieldParent(QueryField element) {
		QueryField parent = element.getParent();
		return (parent != QueryField.EXIF_ALL) ? parent : null;
	}

	@Override
	public QueryField getRootElement() {
		return QueryField.EXIF_GPS;
	}

	@Override
	protected int getExpandLevel() {
		return 1;
	}

	@Override
	protected void fillLocalPullDown(IMenuManager manager) {
		// do nothing
	}

	@Override
	protected void makeActions() {
		// do nothing
	}

	@Override
	public void updateActions(boolean force) {
		// do nothing
	}

	@Override
	protected int[] getColumnWidths() {
		return new int[] {140, 150, 30};
	}

	@Override
	protected int[] getColumnMaxWidths() {
		return new int[] { 220, Integer.MAX_VALUE, 30 };
	}
}
