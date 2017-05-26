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
 * (c) 2017 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ui.internal.dialogs;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;

import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.VocabManager;

public class KeywordLabelProvider extends ZColumnLabelProvider {
	private VocabManager vManager;
	private String[] noImg;

	public KeywordLabelProvider(VocabManager vManager, String[] noImg) {
		this.vManager = vManager;
		this.noImg = noImg;
	}

	@Override
	public String getText(Object element) {
		return element.toString();
	}

	@Override
	public Image getImage(Object element) {
		if (vManager != null && element instanceof String) {
			if (isNoImg(element))
				return null;
			String mapped = vManager.getVocab((String) element);
			if (mapped == null)
				return Icons.warning.getImage();
			if (!mapped.equals(element))
				return Icons.info.getImage();
		}
		return null;
	}

	private boolean isNoImg(Object element) {
		if (noImg != null)
			for (String n : noImg)
				if (n == element)
					return true;
		return false;
	}

	@Override
	public String getToolTipText(Object element) {
		if (vManager != null && element instanceof String) {
			if (isNoImg(element))
				return null;
			String mapped = vManager.getVocab((String) element);
			if (mapped == null)
				return Messages.EditMetaDialog_does_not_belong_to_vocab;
			if (!mapped.equals(element))
				return NLS.bind(Messages.EditMetaDialog_better_use_x, mapped, element);
		}
		return null;
	}

	@Override
	public Image getToolTipImage(Object element) {
		return getImage(element);
	}
}