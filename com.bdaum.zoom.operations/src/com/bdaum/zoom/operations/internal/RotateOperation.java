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

package com.bdaum.zoom.operations.internal;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.internal.db.AssetEnsemble;
import com.bdaum.zoom.image.ImageUtilities;
import com.bdaum.zoom.image.ZImage;
import com.bdaum.zoom.image.internal.ImageActivator;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;

@SuppressWarnings("restriction")
public class RotateOperation extends DbOperation {

	private int degrees;
	private List<Asset> assets;
	private List<String> ids;
	private Map<String, byte[]> thumbnails;
	private int n;

	public RotateOperation(List<Asset> assets, int degrees) {
		super(NLS.bind(Messages.getString("RotateOperation.Rotate"), //$NON-NLS-1$
				(degrees == 90) ? Messages.getString("RotateOperation.clockwise") //$NON-NLS-1$
						: Messages.getString("RotateOperation.antiClockwise"))); //$NON-NLS-1$
		this.assets = assets;
		this.degrees = degrees;
		n = assets != null ? assets.size() : 0;
		ids = new ArrayList<String>(n);
	}

	@Override
	public IStatus execute(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		return rotate(aMonitor, info, degrees);
	}

	private IStatus rotate(IProgressMonitor aMonitor, IAdaptable info, int d) {
		boolean rotated = false;
		init(aMonitor, ids.size());
		if (assets != null)
			for (Asset asset : assets) {
				if (asset.getFileState() != IVolumeManager.PEER) {
					String id = asset.getStringId();
					byte[] byteArray;
					if (thumbnails != null && thumbnails.containsKey(id))
						byteArray = thumbnails.remove(id);
					else {
						byte[] original = asset.getJpegThumbnail();
						if (ImageUtilities.testOnJpeg(original))
							byteArray = ImageUtilities.lljTran(d, original);
						else {
							int quality = Core.getCore().getDbManager().getMeta(true).getJpegQuality();
							byteArray = rotateViaImageIO(d, original, quality);
							if (byteArray != null) {
								if (thumbnails == null)
									thumbnails = new HashMap<>(n * 3 / 2);
								thumbnails.put(id, original);
							}
						}
					}
					if (byteArray != null) {
						ids.add(id);
						asset.setJpegThumbnail(byteArray);
						asset.setRotation((asset.getRotation() + d) % 360);
						AssetEnsemble.setOrientation(asset);
						if (!storeSafely(null, 1, asset))
							break;
						rotated = true;
					}
				}
			}
		if (rotated) {
			fireAssetsModified(new BagChange<>(null, assets, null, null), null);
			if (!dbManager.getMeta(true).getNoIndex()) {
				Job job = Core.getCore().getDbFactory().getLireService(true).createIndexingJob(assets, true, -1, 0,
						true);
				if (job != null)
					job.schedule(20L);
			}
		}
		assets = null; // drop assets to save heap space
		return close(info);
	}

	private static byte[] rotateViaImageIO(int angle, byte[] data, int quality) {
		try (ByteArrayInputStream in = new ByteArrayInputStream(data)) {
			BufferedImage image = ImageIO.read(in);
			if (image != null) {
				BufferedImage rotatedImage = ImageUtilities.rotateImage(image, angle, 1f, 1f);
				image.flush();
				ByteArrayOutputStream out = new ByteArrayOutputStream(20000);
				ImageUtilities.saveBufferedImageToStream(rotatedImage, out, ZImage.IMAGE_WEBP, quality);
				rotatedImage.flush();
				return out.toByteArray();
			}
		} catch (IOException e) {
			ImageActivator.getDefault().logError(Messages.getString("RotateOperation.error_roation_webp"), e); //$NON-NLS-1$
		}
		return null;
	}

	@Override
	public IStatus redo(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		restoreAssets();
		return rotate(aMonitor, info, degrees);
	}

	private void restoreAssets() {
		assets = dbManager.obtainByIds(Asset.class, ids);
		ids.clear();
	}

	@Override
	public IStatus undo(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		restoreAssets();
		return rotate(aMonitor, info, 360 - degrees);
	}

	public int getExecuteProfile() {
		return IProfiledOperation.CONTENT;
	}

	public int getUndoProfile() {
		return IProfiledOperation.CONTENT;
	}

	@Override
	public int getPriority() {
		return ids.size() > 3 ? Job.LONG : Job.SHORT;
	}

}
