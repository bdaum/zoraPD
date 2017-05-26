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

package com.bdaum.zoom.ui.internal.html;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
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

/**
 * @author Berthold Daum
 *
 *         (c) 2002 Berthold Daum
 */
public class HtmlSourceViewer extends SourceViewer implements
		ITextOperationTarget {

	private final IUndoManager undoManager = new TextViewerUndoManager(25);

	public HtmlSourceViewer(Composite parent, IVerticalRuler ruler, int styles,
			RuleBasedScanner codeScanner,
			IContentAssistProcessor contentAssistant) {
		super(parent, ruler, styles);
		configure(new XMLViewerConfiguration(this, codeScanner,
				contentAssistant, undoManager));
		Control styleTextWidget = getControl();
		appendVerifyKeyListener(new VerifyKeyListener() {
			/**
			 * @see org.eclipse.swt.custom.VerifyKeyListener#verifyKey(VerifyEvent)
			 */
			public void verifyKey(VerifyEvent event) {
				if (event.stateMask == SWT.CTRL || event.character == 13) {
					event.doit = false;
				}
			}
		});
		styleTextWidget.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent event) {
				if (event.character == 13) {
					ITextSelection selection = (ITextSelection) getSelection();
					if (selection != null) {
						try {
							getDocument().replace(selection.getOffset(), 0, "<br/>"); //$NON-NLS-1$
							setSelectedRange(selection.getOffset()+5, 0);
						} catch (BadLocationException e) {
							// should never happen
						}
					}
					return;
				}
				if (event.stateMask != SWT.CTRL)
					return;
				int operation = 0;
				if (event.character == ' ') {
					// STRG+Leer: Content Assist
					operation = ISourceViewer.CONTENTASSIST_PROPOSALS;
				} else {
					switch (event.character | '\u0040') {
					case 'Z':
						// STRG+Z: Undo
						operation = ITextOperationTarget.UNDO;
						break;
					case 'Y':
						// STRG+Z: Undo
						operation = ITextOperationTarget.REDO;
						break;
					case 'C':
						// STRG+Z: Undo
						operation = ITextOperationTarget.COPY;
						break;
					case 'V':
						// STRG+Z: Undo
						operation = ITextOperationTarget.PASTE;
						break;
					case 'X':
						// STRG+Z: Undo
						operation = ITextOperationTarget.CUT;
						break;
					case 'A':
						// STRG+Z: Undo
						operation = ITextOperationTarget.SELECT_ALL;
						break;
					case 'B':
					case 'F':
						// STRG+B: Bold
						setTextStyle(SWT.BOLD);
						break;
					case 'I':
					case 'K':
						// STRG+I: Italic
						setTextStyle(SWT.ITALIC);
						break;
					case 'U':
						// STRG+U: Underlined
						setTextStyle(SWT.UNDERLINE_SINGLE);
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

	protected void setTextStyle(int style) {
		ITextSelection selection = (ITextSelection) getSelection();
		if (selection != null) {
			String text = selection.getText();
			String newText;
			switch (style) {
			case SWT.BOLD:
				newText = "<b>"+text+"</b>"; //$NON-NLS-1$ //$NON-NLS-2$
				break;
			case SWT.ITALIC:
				newText = "<i>"+text+"</i>";  //$NON-NLS-1$//$NON-NLS-2$
				break;
			default:
				newText = "<u>"+text+"</u>";  //$NON-NLS-1$//$NON-NLS-2$
				break;
			}
			try {
				getDocument().replace(selection.getOffset(), text.length(), newText);
			} catch (BadLocationException e) {
				// should never happen
			}
		}

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