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
 * (c) 2011-2021 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.dialogs;

import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.ui.internal.codes.Topic;

public class TopicLabelProvider extends ZColumnLabelProvider {
	private final int column;

	public TopicLabelProvider(int column) {
		this.column = column;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof Topic) {
			switch (column) {
			case 0:
				return ((Topic) element).getCode().trim();
			case 2:
				return ((Topic) element).getDescription().trim();
			}
		}
		return element.toString().trim();
	}

}