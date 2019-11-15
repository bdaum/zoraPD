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

package com.bdaum.zoom.ui.internal.html;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

public class XMLCodeScanner extends RuleBasedScanner {

	public XMLCodeScanner() {
		Display display = Display.getCurrent();
		setRules(new IRule[] { 
				new MultiLineRule("<!--", "-->", //$NON-NLS-1$ //$NON-NLS-2$
						new Token(new TextAttribute(display.getSystemColor(SWT.COLOR_DARK_GRAY)))),
				new SingleLineRule("<!", ">", //$NON-NLS-1$ //$NON-NLS-2$
				new Token(new TextAttribute(display.getSystemColor(SWT.COLOR_DARK_YELLOW))), '\\'),
				new SingleLineRule("<", ">", //$NON-NLS-1$ //$NON-NLS-2$
						new Token(new TextAttribute(display.getSystemColor(SWT.COLOR_DARK_GREEN))), '\\'),
				new SingleLineRule("{$$", "}", //$NON-NLS-1$ //$NON-NLS-2$
						new Token(new TextAttribute(display.getSystemColor(SWT.COLOR_DARK_BLUE), null, SWT.BOLD)), '\\'),
				new SingleLineRule("{$", "}", //$NON-NLS-1$ //$NON-NLS-2$
						new Token(new TextAttribute(display.getSystemColor(SWT.COLOR_BLUE), null, SWT.BOLD)), '\\')
		});
	}
}
