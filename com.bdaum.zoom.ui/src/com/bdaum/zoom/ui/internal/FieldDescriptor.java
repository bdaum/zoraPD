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
 * (c) 2009-2012 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal;

import com.bdaum.zoom.cat.model.group.Criterion;
import com.bdaum.zoom.cat.model.group.SortCriterion;
import com.bdaum.zoom.core.QueryField;

public class FieldDescriptor {
	public QueryField qfield;
	public QueryField subfield;
	public String label;

	private FieldDescriptor(QueryField qfield, String subfield) {
		this(qfield, findSubfield(qfield, subfield));
	}

	public FieldDescriptor(QueryField qfield, QueryField subfield) {
		this.qfield = qfield;
		this.subfield = subfield;
		this.label = qfield.getLabel();
		if (subfield != null)
			this.label += ' ' + subfield.getLabel();
	}

	public FieldDescriptor(String label) {
		this.label = label;
	}

	public FieldDescriptor(Criterion crit) {
		this(QueryField.findQueryField(crit.getField()), crit.getSubfield());
	}

	public FieldDescriptor(SortCriterion crit) {
		this(QueryField.findQueryField(crit.getField()), crit.getSubfield());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof FieldDescriptor) || qfield != ((FieldDescriptor) obj).qfield)
			return false;
		if (qfield == null)
			return label.equals(((FieldDescriptor) obj).label);
		return (((FieldDescriptor) obj).subfield == subfield);
	}

	private static QueryField findSubfield(QueryField qfield, String subfield) {
		if (subfield != null && qfield != null) {
			QueryField parent = QueryField.getStructParent(qfield.getType());
			if (parent != null && parent.getChildren() != null)
				for (QueryField q : parent.getChildren())
					if (subfield.equals(q.getKey()))
						return q;
		}
		return null;
	}

	@Override
	public int hashCode() {
		int hashCode = qfield == null ? label.hashCode() : qfield.hashCode();
		return (subfield != null) ? 31 * hashCode + subfield.hashCode() : hashCode;
	}

	public QueryField getDetailQueryField() {
		return (subfield != null) ? subfield : qfield;
	}

	public boolean isStruct() {
		return qfield != null && qfield.isStruct() && subfield == null;
	}

	public boolean isUiField() {
		return qfield != null && qfield.isUiField() && (subfield == null || subfield.isUiField());
	}

	@Override
	public String toString() {
		return "FieldDescriptor: " + label; //$NON-NLS-1$
	}
}