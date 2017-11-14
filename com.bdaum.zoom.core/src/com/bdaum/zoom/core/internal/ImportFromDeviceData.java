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

package com.bdaum.zoom.core.internal;

import java.io.File;
import java.util.Set;

import com.bdaum.zoom.cat.model.meta.WatchedFolder;


public class ImportFromDeviceData {

	public static final int SUBFOLDERPOLICY_NO = 0;
	public static final int SUBFOLDERPOLICY_YEAR = 1;
	public static final int SUBFOLDERPOLICY_YEARMONTH = 2;
	public static final int SUBFOLDERPOLICY_YEARMONTHDAY = 3;
	private String targetDir;
	private int subfolderPolicy;
	private boolean detectDuplicates;
	private String exifTransferPrefix;
	private boolean removeMedia;
	private String renamingTemplate;
	private FileInput fileInput;
	private String artist;
	private String event;
	private String[] keywords;
	private String cue;
	private final boolean media;
	private int skipPolicy;
	private Set<String> skippedFormats;
	private final File[] dcims;
	private WatchedFolder watchedFolder;
	private int privacy;


	public ImportFromDeviceData(File[] dcims, boolean media, WatchedFolder watchedFolder) {
		this.dcims = dcims;
		this.media = media;
		this.watchedFolder = watchedFolder;
	}

	public void setRemoveMedia(boolean removeMedia) {
		this.removeMedia = removeMedia;
	}

	public void setDetectDuplicates(boolean detectDuplicates) {
		this.detectDuplicates = detectDuplicates;
	}

	public void setSubfolderPolicy(int subfolderPolicy) {
		this.subfolderPolicy = subfolderPolicy;
	}

	public void setTargetDir(String targetDir) {
		this.targetDir = targetDir;
	}

	public String getTargetDir() {
		return targetDir;
	}

	public int getSubfolderPolicy() {
		return subfolderPolicy;
	}

	public boolean isDetectDuplicates() {
		return detectDuplicates;
	}

	public boolean isRemoveMedia() {
		return removeMedia;
	}

	public String getRenamingTemplate() {
		return renamingTemplate;
	}

	public void setRenamingTemplate(String renamingTemplate) {
		this.renamingTemplate = renamingTemplate;
	}

	public void setFileInput(FileInput fileInput) {
		this.fileInput = fileInput;
	}

	public FileInput getFileInput() {
		return fileInput;
	}

	public String getExifTransferPrefix() {
		return exifTransferPrefix;
	}

	public void setExifTransferPrefix(String exifTransferPrefix) {
		this.exifTransferPrefix = exifTransferPrefix;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public String[] getKeywords() {
		return keywords;
	}

	public void setKeywords(String[] keywords) {
		this.keywords = keywords;
	}

	public String getCue() {
		return cue;
	}

	public void setCue(String cue) {
		this.cue = cue;
	}

	/**
	 * @return the media
	 */
	public boolean isMedia() {
		return media;
	}

	public void setSkipPolicy(int skipPolicy) {
		this.skipPolicy = skipPolicy;
	}

	/**
	 * @return the skipPolicy
	 */
	public int getSkipPolicy() {
		return skipPolicy;
	}

	public Set<String> getSkippedFormats() {
		return skippedFormats;
	}

	/**
	 * @param skippedFormats the skippedFormats to set
	 */
	public void setSkippedFormats(Set<String> skippedFormats) {
		this.skippedFormats = skippedFormats;
	}

	/**
	 * @return dcims
	 */
	public File[] getDcims() {
		return dcims;
	}

	public WatchedFolder getWatchedFolder() {
		return watchedFolder;
	}

	public void setPrivacy(int privacy) {
		this.privacy = privacy;
	}

	public int getPrivacy() {
		return privacy;
	}

}
