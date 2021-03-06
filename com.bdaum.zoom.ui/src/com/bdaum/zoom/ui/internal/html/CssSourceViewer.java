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

import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.TextViewerUndoManager;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class CssSourceViewer extends SourceViewer implements
		ITextOperationTarget {

	private final IUndoManager undoManager = new TextViewerUndoManager(25);

	public CssSourceViewer(Composite parent, IVerticalRuler ruler, int styles,
			RuleBasedScanner codeScanner,
			IContentAssistProcessor contentAssistant) {
		super(parent, ruler, styles);
		configure(new SimpleViewerConfiguration(this, codeScanner,
				contentAssistant, undoManager));
		Control styleTextWidget = getControl();
		appendVerifyKeyListener(new VerifyKeyListener() {
			public void verifyKey(VerifyEvent event) {
				if (event.stateMask == SWT.CTRL)
					event.doit = false;
			}
		});
		styleTextWidget.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent event) {
				if (event.stateMask != SWT.CTRL)
					return;
				int operation = 0;
				if (contentAssistant != null && event.character == ' ') {
					// STRG+Leer: Content Assist
					operation = ISourceViewer.CONTENTASSIST_PROPOSALS;
				} else {
					switch (event.character | '\u0040') {
					case 'Z':
						operation = ITextOperationTarget.UNDO;
						break;
					case 'Y':
						operation = ITextOperationTarget.REDO;
						break;
					case 'C':
						operation = ITextOperationTarget.COPY;
						break;
					case 'V':
						operation = ITextOperationTarget.PASTE;
						break;
					case 'X':
						operation = ITextOperationTarget.CUT;
						break;
					case 'A':
						operation = ITextOperationTarget.SELECT_ALL;
						break;
					}
				}
				if (operation != 0 && canDoOperation(operation))
					doOperation(operation);
			}

			public void keyReleased(KeyEvent e) {
				// do nothing
			}
		});
	}


	@Override
	public void doOperation(int operation) {
		if (canDoOperation(operation))
			super.doOperation(operation);
	}

	public void resetUndoManager() {
		undoManager.reset();
	}

	public boolean hasFocus() {
		return getControl().isFocusControl();
	}

	/**
	 * @param sel
	 */
	public void selectAndReveal(ISelection sel) {
		setSelection(sel, true);
	}

	public void setFocus() {
		getControl().setFocus();
	}
}