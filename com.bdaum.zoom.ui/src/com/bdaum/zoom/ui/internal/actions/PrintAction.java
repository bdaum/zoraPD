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
 * (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.ui.internal.actions;

import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

import com.bdaum.zoom.cat.model.PageLayout_typeImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IAssetProvider;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.image.internal.ImageActivator;
import com.bdaum.zoom.image.recipe.UnsharpMask;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.dialogs.PrintLayoutDialog;
import com.bdaum.zoom.ui.internal.job.PrintJob;

@SuppressWarnings("restriction")
public class PrintAction extends AbstractSelectionAction implements IAdaptable {

	private IWorkbenchWindow window;

	public PrintAction(IWorkbenchWindow window) {
		super(window, Messages.PrintAction_print, UiActivator
				.getImageDescriptor("/icons/print_edit.gif")); //$NON-NLS-1$
		this.window = window;
	}

	@Override
	public void run() {
		PrinterData data = null;
		PageLayout_typeImpl layout = null;
		UnsharpMask imageOp = null;
		List<Asset> assets = null;
		while (true) {
			PrintDialog dialog = new PrintDialog(window.getShell());
			if (data == null) {
				data = new PrinterData();
				if (selection != null && !selection.isEmpty())
					data.scope = PrinterData.SELECTION;
			}
			dialog.setPrinterData(data);
			data = dialog.open();
			if (data == null)
				break;
			if (data.scope == PrinterData.SELECTION && selection != null
					&& !selection.isEmpty())
				assets = selection.getAssets();
			if (assets == null || assets.isEmpty()) {
				IAssetProvider provider = Core.getCore().getAssetProvider();
				if (provider != null)
					assets = provider.getAssets();
			}
			if (assets == null || assets.isEmpty()) {
				AcousticMessageDialog.openInformation(window.getShell(),
						Messages.PrintAction_Printing,
						Messages.PrintAction_Nothing_to_print);
				return;
			}
			PrintLayoutDialog layoutDialog = new PrintLayoutDialog(
					window.getShell(), data, assets);
			int retCode = layoutDialog.open();
			if (retCode == PrintLayoutDialog.CANCEL)
				return;
			if (retCode == Window.OK) {
				layout = layoutDialog.getResult();
				imageOp = layout.getApplySharpening() ? ImageActivator
						.getDefault().computeUnsharpMask(layout.getRadius(),
								layout.getAmount(), layout.getThreshold())
						: null;
				break;
			}
		}
		if (data != null && layout != null)
			new PrintJob(data, layout, assets, imageOp, this).schedule();
	}

	@Override
	public void assetsChanged(IWorkbenchPart part, AssetSelection selectedAssets) {
		selection = selectedAssets;
		List<Asset> assets = null;
		if (selection.isEmpty()) {
			IAssetProvider provider = Core.getCore().getAssetProvider();
			if (provider != null)
				assets = provider.getAssets();
		} else
			assets = selection.getAssets();
		CoreActivator activator = CoreActivator.getDefault();
		if (assets != null)
			for (Asset asset : assets) {
				IMediaSupport mediaSupport = activator.getMediaSupport(asset
						.getFormat());
				if (mediaSupport != null) {
					if (mediaSupport.testProperty(QueryField.PHOTO)) {
						setEnabled(true);
						return;
					}
				} else {
					setEnabled(true);
					return;
				}
			}
		setEnabled(false);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getAdapter(Class adapter) {
		if (adapter.equals(Shell.class))
			return window.getShell();
		return null;
	}

}
