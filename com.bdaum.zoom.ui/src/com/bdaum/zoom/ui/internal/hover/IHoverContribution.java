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
 * (c) 2019 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.hover;

public interface IHoverContribution {

	/**
	 * @return true if this type of hover supports a title line
	 */
	boolean supportsTitle();
	
	/**
	 * @return category name
	 */
	String getCategory();
	
	/**
	 * @return name of contribution
	 */
	String getName();
	
	/**
	 * @return description of contribution
	 */
	String getDescription();
	
	/**
	 * @return the template for the hover text or null
	 */
	String getTemplate();
	
	/**
	 * @return the default template for the hover text
	 */
	String getDefaultTemplate();
	
	/**
	 * @return the template for the hover title or null
	 */
	String getTitleTemplate();
	
	/**
	 * @return the default template for the hover title or null
	 */
	String getDefaultTitleTemplate();
	
	/**
	 * @return the keys for the variables supported for the hover text of this contribution
	 */
	String[] getItemKeys();
	
	/**
	 * @param itemkey
	 * @return the variable handler for the specified key
	 */
	IHoverItem getHoverItem(String itemkey);

	/**
	 * Sets the id of this hover contribution. Used internally.
	 * @param id
	 */
	void setId(String id);

	/**
	 * Supplies the target object
	 * e.g. for a bookmark the target object is the marked asset
	 * @param object - hover object
	 * @return - target object
	 */
	Object getTarget(Object object);

	/**
	 * Supplies the media flags (PHOTO, VIDEO) if the specified object is an asset
	 * @param object - hover object
	 * @return media flags
	 */
	int getMediaFlags(Object object);

	/**
	 * Supplies a test object used to generate a preview of the template
	 * @return an object of type HoverTestObject or HoverTestAsset
	 */
	Object getTestObject();

	/**
	 * Saves the templates to the preference store
	 * @param titleTemplate
	 * @param template
	 */
	void save(String titleTemplate, String template);

	/**
	 * @return the keys for the variables supported for the hover title of this contribution
	 */
	String[] getTitleItemKeys();

	/**
	 * @return the labels for the variables supported for the hover title of this contribution
	 */
	String[] getTitleItemLabels();

	/**
	 * @return the labels for the variables supported for the hover text of this contribution
	 */
	String[] getItemLabels();
	
}
