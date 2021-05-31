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
 * (c) 2021 Berthold Daum  
 */
package com.bdaum.zoom.email.internal;

import java.util.List;
import java.util.Set;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.image.recipe.UnsharpMask;

public class EmailData {

	private List<Asset> assets;
	private int mode, sizing, maxSize, cropMode, jpegQuality, privacy;
	private double scalingFactor;
	private UnsharpMask unsharpMask;
	private Set<QueryField> filter;
	private boolean watermark, exportTrack;
	private String copyright, to, cc, bcc, subject, message;

	public EmailData(List<Asset> assets, int mode, int sizing, double scalingFactor, int maxSize, int cropMode,
			UnsharpMask unsharpMask, int jpegQuality, Set<QueryField> filter, boolean watermark, String copyright, int privacy,
			boolean exportTrack) {
				this.assets = assets;
				this.mode = mode;
				this.sizing = sizing;
				this.scalingFactor = scalingFactor;
				this.maxSize = maxSize;
				this.cropMode = cropMode;
				this.unsharpMask = unsharpMask;
				this.jpegQuality = jpegQuality;
				this.filter = filter;
				this.watermark = watermark;
				this.copyright = copyright;
				this.privacy = privacy;
				this.exportTrack = exportTrack;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public void setCc(String cc) {
		this.cc = cc;
	}

	public void setBcc(String bcc) {
		this.bcc = bcc;
	}

	public List<Asset> getAssets() {
		return assets;
	}

	public int getMode() {
		return mode;
	}

	public int getSizing() {
		return sizing;
	}

	public double getScalingFactor() {
		return scalingFactor;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public int getCropMode() {
		return cropMode;
	}

	public UnsharpMask getUnsharpMask() {
		return unsharpMask;
	}

	public int getJpegQuality() {
		return jpegQuality;
	}

	public Set<QueryField> getFilter() {
		return filter;
	}

	public boolean isWatermark() {
		return watermark;
	}

	public String getCopyright() {
		return copyright;
	}

	public int getPrivacy() {
		return privacy;
	}

	public boolean isExportTrack() {
		return exportTrack;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getSubject() {
		return subject;
	}

	public String getMessage() {
		return message;
	}

	public List<String> getTo() {
		return Core.fromStringList(to, ";"); //$NON-NLS-1$
	}

	public List<String> getCc() {
		return Core.fromStringList(cc, ";"); //$NON-NLS-1$
	}

	public List<String> getBcc() {
		return Core.fromStringList(bcc, ";"); //$NON-NLS-1$
	}

}
