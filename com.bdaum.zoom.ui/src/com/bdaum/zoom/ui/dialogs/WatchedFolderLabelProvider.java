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
 * (c) 2019 Berthold Daum  
 */
package com.bdaum.zoom.ui.dialogs;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

import com.bdaum.zoom.cat.model.meta.WatchedFolder;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.wizards.ImportFileSelectionPage;

public class WatchedFolderLabelProvider extends ZColumnLabelProvider {

	public static final int PATH = 0;

	public static final int VOLUME = 1;

	public static final int TYPE = 2;

	public static final int RECURSIVE = 3;

	public static final int LASTOBSERVATION = 4;

	public static final int FILTERS = 5;

	public static final int POLICIES = 6;

	private int colNo;

	public WatchedFolderLabelProvider(int colNo) {
		this.colNo = colNo;
	}

	@Override
	public String getToolTipText(Object element) {
		if (element instanceof WatchedFolder && UiActivator.getDefault().getShowHover())
			return UiActivator.getDefault().getHoverManager().getHoverText("com.bdaum.zoom.ui.hover.watchedFolder", //$NON-NLS-1$
					element, null);
		return ""; //$NON-NLS-1$
	}

	@Override
	protected Color getForeground(Object element) {
		if (element instanceof WatchedFolder) {
			String uri = ((WatchedFolder) element).getUri();
			try {
				File file = new File(new URI(uri));
				if (!file.exists())
					return getViewerControl().getDisplay().getSystemColor(SWT.COLOR_DARK_RED);
			} catch (URISyntaxException e) {
				// ignore
			}
		}
		return super.getForeground(element);
	}

	@Override
	public String getText(Object element) {
		switch (colNo) {
		case PATH:
			if (element instanceof WatchedFolder) {
				String uri = ((WatchedFolder) element).getUri();
				try {
					String path = new File(new URI(uri)).getPath();
					if (!path.endsWith(File.separator))
						path += File.separator;
					return path;
				} catch (URISyntaxException e) {
					// ignore
				}
				return uri;
			}
			return null;
		case VOLUME:
			if (element instanceof WatchedFolder) {
				String volume = ((WatchedFolder) element).getVolume();
				if (volume == null || volume.isEmpty()) {
					if (Constants.WIN32)
						try {
							String path = new File(new URI(((WatchedFolder) element).getUri())).getPath();
							int p = path.indexOf(':');
							if (p >= 0)
								return path.substring(0, p + 1);
						} catch (URISyntaxException e) {
							// ignore
						}
					return ""; //$NON-NLS-1$
				}
				return volume;
			}
			return null;
		case TYPE:
			if (element instanceof WatchedFolder)
				return ((WatchedFolder) element).getTransfer() ? Messages.WatchedFolderLabelProvider_transfer
						: Messages.WatchedFolderLabelProvider_storage;
			return null;
		case RECURSIVE:
			if (element instanceof WatchedFolder)
				return ((WatchedFolder) element).getRecursive() ? Messages.WatchedFolderLabelProvider_yes
						: Messages.WatchedFolderLabelProvider_no;
			return null;
		case LASTOBSERVATION:
			if (element instanceof WatchedFolder) {
				long last = ((WatchedFolder) element).getLastObservation();
				return last <= 0 ? "   -" //$NON-NLS-1$
						: Format.MDY_TIME_SHORT_FORMAT.get().format(last);
			}
			return null;
		case FILTERS:
			if (element instanceof WatchedFolder) {
				WatchedFolder wf = (WatchedFolder) element;
				if (!wf.getTransfer())
					return UiUtilities.getFilters(wf);
				int skipPolicy = wf.getSkipPolicy();
				if (skipPolicy < 0 && skipPolicy >= ImportFileSelectionPage.SKIPPOLICIES.length)
					skipPolicy = 0;
				return new StringBuilder().append(ImportFileSelectionPage.SKIPPOLICIES[skipPolicy]).append(" | ") //$NON-NLS-1$
						.append(wf.getTargetDir()).toString();
			}
			return null;
		case POLICIES:
			if (element instanceof WatchedFolder) {
				WatchedFolder wf = (WatchedFolder) element;
				int skipPolicy = wf.getSkipPolicy();
				if (skipPolicy < 0 && skipPolicy >= ImportFileSelectionPage.SKIPPOLICIES.length)
					skipPolicy = 0;
				return new StringBuilder().append(ImportFileSelectionPage.SKIPPOLICIES[skipPolicy]).append(" | ") //$NON-NLS-1$
						.append(wf.getTargetDir()).toString();
			}
		}
		return null;
	}

}
