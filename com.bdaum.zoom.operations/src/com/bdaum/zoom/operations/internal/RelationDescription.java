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

import java.util.Date;

public class RelationDescription {
	public String kind;
	public String recipe;
	public String tool;
	public String parameterFile;
	public Date createdAt;
	public byte[] archivedRecipe;

	public RelationDescription(String kind, String recipe, String tool,
			String parameterFile, Date createdAt, byte[] archivedRecipe) {
		super();
		this.kind = kind;
		this.recipe = recipe;
		this.tool = tool;
		this.parameterFile = parameterFile;
		this.createdAt = createdAt;
		this.archivedRecipe = archivedRecipe;
	}
}