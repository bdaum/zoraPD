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
 * (c) 2015 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ui.internal.actions;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.BulkRenameOperation;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.internal.dialogs.StackDialog;

@SuppressWarnings("restriction")
public class StackAction extends Action {

	private IAdaptable adaptable;

	public StackAction(String label, String tooltip, ImageDescriptor image, IAdaptable adaptable) {
		super(label, image);
		this.adaptable = adaptable;
		setToolTipText(tooltip);
	}

	@Override
	public void run() {
		AssetSelection selection = adaptable.getAdapter(AssetSelection.class);
		List<Asset> selectedAssets = selection.getAssets();
		if (selectedAssets.size() > 1) {
			Set<String> names = new HashSet<String>();
			for (Asset asset : selectedAssets) {
				String uri = asset.getUri();
				try {
					File file = new File(new URI(uri));
					String name = file.getName();
					if (name.startsWith(Constants.STACK)) {
						int p = name.indexOf('!', Constants.STACK.length());
						if (p >= 0)
							names.add(name.substring(Constants.STACK.length(), p));
					}
				} catch (URISyntaxException e) {
					// do nothing
				}
			}
			StackDialog dialog = new StackDialog(adaptable.getAdapter(Shell.class),
					names.toArray(new String[names.size()]));
			if (dialog.open() == Window.OK) {
				String template = Constants.STACK + dialog.getName() + '!' + Constants.TV_FILENAME;
				OperationJob.executeOperation(new BulkRenameOperation(selectedAssets, template, null, 1, null),
						adaptable);
			}
		}

	}

}
