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
 * (c) 2009-2021 Berthold Daum  
 */

package com.bdaum.zoom.core;

import java.text.ParseException;
import java.util.Date;


public interface IFormatter {

	/**
	 * Returns a formatted string representation of an object or primitive
	 * @param obj/v/b/d the object
	 * @return the string representation
	 */
	String format(int v);
	String format(double v);
	String format(long v);
	String format(boolean b);
	String format(Date d);
	String format(Object obj);

	/**
	 * Converts a string representation into an object
	 * @param s - string
	 * @return - object
	 * @throws ParseException
	 */
	Object parse(String s) throws ParseException;
}
