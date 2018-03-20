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

package com.bdaum.zoom.core;

import java.io.Serializable;


/**
 * Object expressing a range of values
 *
 */
public class Range implements Serializable {

	private static final long serialVersionUID = -4394428736040399205L;
	private Object from;
	private Object to;

	/**
	 * Default constructor
	 */
	public Range() {
	}

	/**
	 * Constructor
	 * @param from - lower value
	 * @param to - upper value
	 */
	public Range(Object from, Object to) {
		this.from = from;
		this.to = to;
	}

	/**
	 * Returns lower value
	 * @return lower value
	 */
	public Object getFrom() {
		return from;
	}

	/**
	 * Sets lower value
	 * @param from - lower value
	 */
	public void setFrom(Object from) {
		this.from = from;
	}

	/**
	 * Returns upper value
	 * @return upper value
	 */
	public Object getTo() {
		return to;
	}

	/**
	 * Sets lower value
	 * @param from - upper value
	 */
	public void setTo(Object to) {
		this.to = to;
	}


	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof Range)
			return from.equals(((Range) obj).getFrom()) && to.equals(((Range) obj).getTo());
		return false;
	}


	@Override
	public int hashCode() {
		return from.hashCode() * 31 + to.hashCode();
	}

	@Override
	public String toString() {
		return from + " - " + to; //$NON-NLS-1$
	}

}
