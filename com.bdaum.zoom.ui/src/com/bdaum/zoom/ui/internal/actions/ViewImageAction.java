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

package com.bdaum.zoom.ui.internal.actions;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IFTPService;
import com.bdaum.zoom.image.ZImage;
import com.bdaum.zoom.operations.internal.ExportMetadataOperation;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.IKiosk;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.views.AssetContainer;
import com.bdaum.zoom.ui.internal.views.ImageViewer;
import com.bdaum.zoom.ui.internal.views.MultiViewer;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;
import com.bdaum.zoom.ui.views.IMediaViewer;

@SuppressWarnings("restriction")
public class ViewImageAction extends Action {

	private final IAdaptable adaptable;

	public ViewImageAction(String label, String tooltip, ImageDescriptor image, IAdaptable adaptable) {
		super(label, image);
		this.adaptable = adaptable;
		setToolTipText(tooltip);
	}

	@Override
	public void runWithEvent(Event event) {
		Boolean bwopt = adaptable.getAdapter(Boolean.class);
		final boolean shift = (event.stateMask & SWT.SHIFT) != 0;
		final boolean ctrl = (event.stateMask & SWT.CTRL) != 0;
		boolean bw = bwopt != null ? bwopt : false;
		if ((event.stateMask & SWT.ALT) != 0)
			bw = !bw;
		Asset asset1 = null;
		Asset asset2 = null;
		if (event.data instanceof AssetImpl)
			asset1 = ((AssetImpl) event.data);
		else if (event.data instanceof AssetContainer)
			asset1 = ((AssetContainer) event.data).getAsset();
		if (asset1 == null) {
			AssetSelection selection = adaptable.getAdapter(AssetSelection.class);
			if (!selection.isEmpty()) {
				asset1 = selection.get(0);
				if (selection.size() > 1)
					asset2 = selection.get(1);
			}
		}
		if (asset1 != null) {
			final Asset a1 = asset1;
			final Asset a2 = asset2;
			final boolean b = bw;
			BusyIndicator.showWhile(Display.getCurrent(),
					() -> launchViewer(a1, a2, shift, b, ctrl, adaptable.getAdapter(IWorkbenchWindow.class)));
		}
	}

	protected void launchViewer(Asset asset1, Asset asset2, boolean shift, boolean alt, boolean ctrl,
			final IWorkbenchWindow window) {
		IMediaViewer imageViewer = UiActivator.getDefault().getMediaViewer(asset1);
		URI uri = Core.getCore().getVolumeManager().findExistingFile(asset1, false);
		boolean isFile = uri == null || Constants.FILESCHEME.equals(uri.getScheme());
		boolean isFtp = uri == null || IFTPService.FTPSCHEME.equals(uri.getScheme());
		try {
			if (uri != null && !isFile && !isFtp && (imageViewer == null || !imageViewer.canHandleRemote())) {
				PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(uri.toURL());
				return;
			}
		} catch (Exception e) {
			Core.getCore().logError(NLS.bind(Messages.ViewImageAction_error_viewing_remote_image, uri), e);
		}
		IPreferencesService preferencesService = Platform.getPreferencesService();
		RGB filter = StringConverter.asRGB(
				preferencesService.getString(UiActivator.PLUGIN_ID, PreferenceConstants.BWFILTER, null, null),
				new RGB(64, 128, 64));
		boolean extrnRqrd = asset2 == null && !shift && !alt && !ctrl
				&& (isFile || imageViewer == null || !imageViewer.canHandleRemote());
		boolean isDummy = imageViewer != null && imageViewer.isDummy();
		if (extrnRqrd || isDummy) {
			String viewerPath = preferencesService.getString(UiActivator.PLUGIN_ID,
					imageViewer == null ? PreferenceConstants.EXTERNALVIEWER
							: PreferenceConstants.EXTERNALMEDIAVIEWER + imageViewer.getId(),
					"", null); //$NON-NLS-1$
			if (viewerPath != null && !viewerPath.isEmpty()) {
				File viewer = new File(viewerPath);
				if (viewer.exists()) {
					try {
						if (uri == null) {
							UiUtilities.showFileIsOffline(window.getShell(), asset1);
							return;
						}
						File tempFile = null;
						File file;
						if (isFile)
							file = new File(uri);
						else
							file = tempFile = Core.download(uri, null);
						boolean autoExport = preferencesService.getBoolean(UiActivator.PLUGIN_ID,
								PreferenceConstants.AUTOEXPORT, true, null);
						if (autoExport && tempFile == null) {
							ExportMetadataOperation op = new ExportMetadataOperation(Collections.singletonList(asset1),
									UiActivator.getDefault().getExportFilter(), UiActivator.getDefault()
											.getPreferenceStore().getBoolean(PreferenceConstants.JPEGMETADATA),
									true, false);
							try {
								op.execute(new NullProgressMonitor(), new IAdaptable() {
									@SuppressWarnings({ "unchecked", "rawtypes" })
									public Object getAdapter(Class adapter) {
										if (Shell.class.equals(adapter))
											return window.getShell();
										return null;
									}
								});
							} catch (ExecutionException e) {
								// should not happen
							}
						}
						BatchUtilities.executeCommand(new String[] { viewerPath, file.getAbsolutePath() }, null, viewer.getParent(),
								Messages.ViewImageAction_run_viewer, IStatus.OK, IStatus.ERROR, -1, 2500L, "UTF-8", null); //$NON-NLS-1$
						if (tempFile != null)
							tempFile.delete();
						return;
					} catch (Exception e) {
						Core.getCore().logError(NLS.bind(Messages.ViewImageAction_error_launching, viewerPath), e);
					}
					return;
				}
				if (isDummy) {
					AcousticMessageDialog.openError(window.getShell(), Messages.ViewImageAction_viewer_missing, NLS
							.bind(Messages.ViewImageAction_reconfigure, viewerPath));
					return;
				}
				AcousticMessageDialog.openError(window.getShell(), Messages.ViewImageAction_viewer_missing,
						NLS.bind(Messages.ViewImageAction_configured_external_viewer, viewerPath));
			} else if (isDummy) {
				AcousticMessageDialog.openError(window.getShell(), Messages.ViewImageAction_viewer_missing,
						Messages.ViewImageAction_configure);
				return;
			}
		}
		if (imageViewer == null)
			imageViewer = new ImageViewer();
		if (asset2 != null) {
			IMediaViewer rightViewer = UiActivator.getDefault().getMediaViewer(asset2);
			if (rightViewer == null)
				rightViewer = new ImageViewer();
			imageViewer = new MultiViewer(imageViewer, rightViewer);
		}
		imageViewer.init(window, IKiosk.PRIMARY, alt ? filter : null,
				ctrl ? (shift ? ZImage.ORIGINAL : ZImage.CROPPED) : ZImage.CROPMASK);
		try {
			imageViewer.open(new Asset[] { asset1, asset2 });
		} catch (IOException e) {
			Core.getCore().logError(Messages.ViewImageAction_error_launching_internal_viewer, e);
		}
	}
}