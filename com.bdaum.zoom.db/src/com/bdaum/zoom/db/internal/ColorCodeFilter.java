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
package com.bdaum.zoom.db.internal;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IPostProcessor2;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IColorCodeFilter;
import com.db4o.query.Constraint;
import com.db4o.query.Query;

public class ColorCodeFilter extends AssetFilter implements IColorCodeFilter {

	int colorCode;
	private IPostProcessor2 processor;

	public ColorCodeFilter(int colorCode) {
		this.colorCode = colorCode;
		if (colorCode != Constants.COLOR_UNDEFINED) {
			IPostProcessor2[] autoColoringProcessors = Core.getCore().getDbFactory()
					.getAutoColoringProcessors();
			processor = autoColoringProcessors == null ? null : autoColoringProcessors[colorCode];
		}
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.db.internal.AssetFilter#accept(com.bdaum.zoom.cat.model
	 * .asset.Asset)
	 */
	@Override
	public boolean accept(Asset asset) {
		if (colorCode == QueryField.SELECTALL)
			return true;
		int code = asset.getColorCode();
		if (colorCode == Constants.COLOR_UNDEFINED)
			return code < 0;
		if (code < 0 && processor != null && processor.accept(asset))
			return true;
		return code == colorCode;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.db.internal.AssetFilter#getConstraint(com.bdaum.zoom.db
	 * .internal.DbManager, com.db4o.query.Query)
	 */
	@Override
	public Constraint getConstraint(DbManager dbManager, Query query) {
		if (colorCode == QueryField.SELECTALL)
			return null;
		if (colorCode == Constants.COLOR_UNDEFINED)
			return query.descend(QueryField.COLORCODE.getKey()).constrain(0)
					.smaller();
		Constraint con3 = query.descend(QueryField.COLORCODE.getKey())
				.constrain(colorCode);
		if (processor instanceof QueryPostProcessor) {
			Constraint con2 = ((QueryPostProcessor) processor).getConstraint(
					dbManager, query);
			if (con2 != null) {
				Constraint con1 = query.descend(QueryField.COLORCODE.getKey())
						.constrain(0).not();
				con1.and(con2);
				con1.or(con3);
				return con1;
			}
		}
		return con3;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.db.IRatingFilter#getRating()
	 */
	public int getColorCode() {
		return colorCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IColorCodeFilter)
			return colorCode == ((IColorCodeFilter) obj).getColorCode();
		return false;
	}

	@Override
	public int hashCode() {
		return colorCode;
	}
}
