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
 * (c) 2018 Berthold Daum  
 */
package com.bdaum.zoom.core;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.internal.CoreActivator;

/**
 * A Assetbox represents an asset related FTP session. It can download files via asset specification. 
 * Tempfiles are cleaned up before each download and at the end of the session.
 * Assetbox can either collect errands into a MultiStatus object or use the Ticketbox errand management 
 * 
 * Typical use:
 *    try (Assetbox box = new Assetbox(assets, status, false)) {
 *    	for (File file : box) {
 *    		Asset asset = box.getAsset();
 *    		URI uri = box.getUri();
 *    		int index = box.getIndex();
 *      }
 *    }
 *    
 * Alternate use:
 *    try (Assetbox box = new Assetbox(null, status, false)) {
 *    	File file = box.obtainFile(asset);
 * 		URI uri = box.getUri();
 *    }
 *    
 */
public class Assetbox extends Ticketbox implements Iterable<File>, Iterator<File> {

	private static File dummy = new File("dummy");  //$NON-NLS-1$
	private Iterator<Asset> assetIterator;
	private MultiStatus status;
	private IVolumeManager vm = Core.getCore().getVolumeManager();
	private Asset asset;
	private int index = -1;
	private URI uri;
	private boolean testOnly;

	/**
	 * Constructor
	 * 
	 * @param assets
	 *            - assets to process via iterator. If null is specified use
	 *            obtainFile(Asset)
	 * @param status
	 *            - a status object receiving error messages. If null is specified
	 *            errors are collected locally and a summary error message can be
	 *            requested via getErrorMessage()
	 * @param testOnly
	 *            true if no download is required
	 */
	public Assetbox(Iterable<Asset> assets, MultiStatus status, boolean testOnly) {
		this.testOnly = testOnly;
		this.assetIterator = assets == null ? null : assets.iterator();
		this.status = status;
	}

	@Override
	public Iterator<File> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		return assetIterator == null ? false : assetIterator.hasNext();
	}

	@Override
	public File next() {
		++index;
		return obtainFile(asset = assetIterator.next());
	}

	public File obtainFile(Asset a) {
		File file = null;
		uri = vm.findExistingFile(a, false);
		if (uri != null) {
			if (testOnly)
				return dummy;
			try {
				file = obtainFile(uri);
			} catch (IOException e) {
				if (status != null)
					status.add(new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID,
							NLS.bind(Messages.Assetbox_download_failed, uri), e));
			}
		}
		if (file == null && status == null)
			addErrand(a.getUri(), a.getVolume());
		return file;
	}

	public Asset getAsset() {
		return asset;
	}

	public int getIndex() {
		return index;
	}

	public URI getUri() {
		return uri;
	}

	public MultiStatus getStatus() {
		return status;
	}

	public void setStatus(MultiStatus status) {
		this.status = status;
	}

}
