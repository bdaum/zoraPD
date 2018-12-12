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

package com.bdaum.zoom.ui.internal.widgets;

import java.awt.Color;
import java.awt.Font;

import org.eclipse.swt.SWT;
import org.piccolo2d.event.PBasicInputEventHandler;
import org.piccolo2d.event.PInputEvent;
import org.piccolo2d.extras.swt.PSWTCanvas;
import org.piccolo2d.extras.swt.PSWTPath;

import com.bdaum.zoom.core.ISpellCheckingService;
import com.bdaum.zoom.ui.internal.Icons.Icon;

public abstract class PPanel extends PSWTPath implements PTextHandler {

	private static final long serialVersionUID = -1896974701502837715L;
	protected TextField title;
	protected PSWTButton deleteButton;
	protected PSWTButton propButton;
	protected Object data;

	public PPanel(Object data, int width, int height) {
		this.data = data;
		setPathToRectangle(0, 0, width, height);
		addInputEventListener(new PBasicInputEventHandler() {
			@Override
			public void mouseReleased(PInputEvent event) {
				if (event.getPickedNode() == deleteButton)
					deletePressed();
				else if (event.getPickedNode() == propButton)
					propertiesPressed();
			}
		});
	}

	protected abstract void propertiesPressed();

	protected abstract void deletePressed();

	protected PSWTButton createButton(PSWTCanvas canvas, Icon icon, String tooltip) {
		PSWTButton button = new PSWTButton(canvas, icon.getImage(), tooltip);
		addChild(button);
		return button;
	}

	protected void createTitle(PSWTCanvas canvas, String text, int x, int y, Color penColor, Color background,
			int size) {
		title = new TextField(text, SWT.DEFAULT, new Font("Arial", //$NON-NLS-1$
				Font.PLAIN, size), penColor, background, true, SWT.SINGLE);
		title.setSpellingOptions(10, ISpellCheckingService.TITLEOPTIONS);
		title.setGreekThreshold(3);
		title.setOffset(x, y);
		title.setPickable(true);
		addChild(title);
	}

	public void setTitle(String text) {
		title.setText(text);
	}

}
