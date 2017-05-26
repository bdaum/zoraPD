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
 * (c) 2016 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.ui.internal.actions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchWindow;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.ManageKeywordsOperation;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.internal.dialogs.KeywordDialog;

/**
 * @author berth
 *
 */
@SuppressWarnings("restriction")
public class UpdateKeywordsAction extends AbstractMultiMediaAction {

	private IAdaptable adaptable;

	public UpdateKeywordsAction(IWorkbenchWindow window, String label, String tooltip, ImageDescriptor image,
			IAdaptable adaptable) {
		super(window, label, image, QueryField.PHOTO | IMediaSupport.VIDEO);
		this.adaptable = adaptable;
		setToolTipText(tooltip);
	}

	@Override
	public void run() {
		Meta meta = Core.getCore().getDbManager().getMeta(true);
		Set<String> selectableKeywords = new HashSet<String>(meta.getKeywords());
		List<Asset> selectedAssets = adaptable.getAdapter(AssetSelection.class).getLocalAssets();
		KeywordDialog dialog = new KeywordDialog(window.getShell(), Messages.KeywordDefinitionAction_add_keywords,
				QueryField.IPTC_KEYWORDS.getCommonItems(selectedAssets), selectableKeywords, selectedAssets);
		if (dialog.open() == Window.OK) {
			BagChange<String> bagChange = dialog.getResult();
			if (bagChange.hasChanges())
				OperationJob.executeOperation(new ManageKeywordsOperation(bagChange, selectedAssets), adaptable);
		}
	}

}
