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

package com.bdaum.zoom.core;

/**
 * Represents an entry in a multi value field of a structured asset component
 * 
 * 
 */
public class IndexedMember {

	private QueryField qfield;
	private int index;
	private Object value;

	/**
	 * Constructor
	 * 
	 * @param qfield
	 *            - field node identifier
	 * @param value
	 *            - node value
	 * @param index
	 *            - node index
	 */
	public IndexedMember(QueryField qfield, Object value, int index) {
		this.qfield = qfield;
		this.value = value;
		this.index = index;
	}

	/**
	 * Returns node identifier
	 * 
	 * @return node identifier
	 */
	public QueryField getQfield() {
		return qfield;
	}

	/**
	 * Returns node index
	 * 
	 * @return node index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Returns node value
	 * 
	 * @return node value
	 */
	public Object getValue() {
		return value;
	}

	
	@Override
	public String toString() {
		return "[" + (index + 1) + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @param index the index to set
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}

}