/*
 * This file is part of the ZoRa project: http://www.photozora.org.
 * It is an adaptation of the equally named file from the jUploadr project (http://sourceforge.net/projects/juploadr/)
 * (c) 2009 Steve Cohen and others
 * 
 * jUploadr is licensed under the GNU Library or Lesser General Public License (LGPL).
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
 * Modifications (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */
package org.scohen.juploadr.app.tags;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Tag implements Comparable<Object>, Serializable {
    private String tag;

    public Tag(String tag) {
        this.tag = tag.replace('"', ' ').trim();
    }

    
	@Override
	public String toString() {
        if (tag.indexOf(' ') >= 0) {
            return '"' + tag + '"';
        }
		return tag;
    }

    public String toDisplayString() {
        return tag;
    }

    
	public int compareTo(Object o) {
        Tag another = (Tag) o;
        return toDisplayString().compareTo(another.toDisplayString());
    }

    
	@Override
	public boolean equals(Object obj) {
        Tag another = (Tag) obj;
        return this == obj || another.toDisplayString().equals(tag);
    }

    
    @Override
	public int hashCode() {
        return tag.hashCode() + 42;
    }

}
