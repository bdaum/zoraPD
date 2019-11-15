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
package com.bdaum.zoom.core.db;

import com.bdaum.zoom.core.IAssetFilter;

/**
 * Represents a file type filter that is applied to queries in
 * addition to the original search expression
 */
public interface ITypeFilter extends IAssetFilter {

	public static final int RAW = 1;
	public static final int DNG = 2;
	public static final int JPEG = 4;
	public static final int TIFF = 8;
	public static final int OTHER = 16;
	public static final int MEDIA = 32;
	public static final int ALLIMAGEFORMATS = RAW | DNG | JPEG | TIFF | OTHER;
	public static final int ALLFORMATS = ALLIMAGEFORMATS | MEDIA;


	/**
	 * Returns the file formats accepted by the filter
	 *
	 * @return - ORed expression of file format constants
	 */
	public abstract int getFormats();

}