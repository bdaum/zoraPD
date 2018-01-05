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
 * (c) 2009-2017 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.ui.internal.widgets;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.InputEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.UndoContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.operations.IWorkbenchOperationSupport;
import org.piccolo2d.PNode;
import org.piccolo2d.event.PInputEvent;
import org.piccolo2d.extras.swt.PSWTCanvas;
import org.piccolo2d.extras.swt.PSWTPath;
import org.piccolo2d.util.PBounds;
import org.piccolo2d.util.PPickPath;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.ISpellCheckingService;
import com.bdaum.zoom.core.ISpellCheckingService.ISpellIncident;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.job.SpellCheckingJob;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;

import jdk.nashorn.tools.Shell;

public class TextField extends PNode implements ISpellCheckingTarget, IAdaptable, IAugmentedTextField {

	private static final int MINDELAYAFTERDOUBLECLICK = 200;
	private static final long serialVersionUID = 1686165715967343718L;
	private GreekedPSWTText textfield;
	private ZPSWTPath caret;
	private Point selection = new Point(-1, -1);
	private ZPSWTPath highlight;
	private Color penColor;
	private Color selectedPenColor;
	private int lead;
	private boolean dirleft;
	private PSWTPath textCanvas;
	private int maxSuggestions = -1;
	private int spellingOptions = ISpellCheckingService.NOSPELLING;
	private ISpellIncident[] incidents;
	private List<PSWTPath> currentSegments;
	private MenuDetectListener menuListener;
	private PSWTCanvas control;
	private Menu previousMenu;
	private IInputValidator validator;
	private ListenerList<VerifyListener> listeners = new ListenerList<VerifyListener>();
	private final int style;
	private boolean valid = true;
	private String text;
	private boolean hasFocus;
	private long when;
	private int wordX = -1;
	private int wordY;
	private IOperationHistory history;
	private IUndoContext context;
	private char lastChar;
	private char previousChar;
	private TextOperation currentOperation;
	private long timeStamp;

	public TextField(String str, int textWidth, Font font, Color penColor, Color backgroundColor, boolean transparent,
			int style) {
		this.style = style;
		control = PSWTCanvas.CURRENT_CANVAS;
		lead = font.getSize() * 5 / 3;
		setPickable(true);
		textCanvas = new PSWTPath();
		textCanvas.setPathToRectangle(0, 0, 30, lead);
		textCanvas.setStrokeColor(null);
		textCanvas.setPickable(false);
		addChild(textCanvas);
		textfield = new GreekedPSWTText("", font); //$NON-NLS-1$
		textfield.setBackgroundColor(backgroundColor);
		textfield.setTransparent(transparent);
		textfield.setPickable(true);
		textfield.setTextWidth(textWidth);
		textfield.setAlignment(style & (SWT.LEFT | SWT.RIGHT | SWT.CENTER));
		addChild(textfield);
		setPenColor(penColor);
		setText(str);
		control.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (history != null) {
					history.dispose(context, true, true, true);
					history = null;
					context = null;
				}
			}
		});
	}

	private IOperationHistory getHistory() {
		if (history == null) {
			IWorkbenchOperationSupport operationSupport = PlatformUI.getWorkbench().getOperationSupport();
			history = operationSupport.getOperationHistory();
			context = new UndoContext();
			history.setLimit(context,
					UiActivator.getDefault().getPreferenceStore().getInt(PreferenceConstants.UNDOLEVELS));
		}
		return history;
	}

	private void setHighlight(boolean b) {
		textfield.setPenColor(penColor);
		if (highlight == null) {
			if (!b)
				return;
			createCaretAndHighlight();
		}
		highlight.setVisible(b);
	}

	public void setGreekThreshold(double threshold) {
		textfield.setGreekThreshold(threshold);
	}

	public boolean isGreek() {
		return textfield.isGreek();
	}

	public void setPenColor(Color color) {
		penColor = color;
		textfield.setPenColor(color);
	}

	public Color getPenColor() {
		return penColor;
	}

	public void setSelectedBgColor(Color selectedBgColor) {
		if (highlight == null)
			createCaretAndHighlight();
		highlight.setPaint(selectedBgColor);
	}

	public void setBackgroundColor(Color color) {
		textCanvas.setPaint(color);
	}

	/*
	 * (nicht-Javadoc)
	 * 
	 * @see com.bdaum.zoom.ui.internal.widgets.ITextField#setText(java.lang.String)
	 */
	@Override
	public void setText(String str) {
		if (!str.equals(text))
			doSetText(text = str, true);
	}

	private void doSetText(String str, boolean cancelSpellchecking) {
		wordX = -1;
		textfield.setText(processRawText(str));
		setCanvasSize();
		if (!str.isEmpty() && spellingOptions != ISpellCheckingService.NOSPELLING)
			checkSpelling(cancelSpellchecking);
	}

	private String processRawText(String str) {
		if (textfield.getTextWidth() != SWT.DEFAULT) {
			if ((style & SWT.WRAP) != 0) {
				StringBuilder sb = new StringBuilder();
				int lastLineBreak = 0;
				int lastWordBreak = 0;
				for (int i = 0; i < str.length(); i++) {
					char c = str.charAt(i);
					sb.append(c);
					String recentLine = sb.substring(lastLineBreak);
					if (textfield.textExtent(recentLine).x > textfield.getTextWidth()) {
						if (lastWordBreak > lastLineBreak) {
							sb.replace(lastWordBreak, lastWordBreak + 1, "\n"); //$NON-NLS-1$
							lastLineBreak = lastWordBreak;
						} else if (c == ' ') {
							sb.insert(sb.length() - 2, '\n');
							lastLineBreak = sb.length() - 2;
						}
					}
					switch (c) {
					case '\n':
						lastLineBreak = sb.length() - 1;
						break;
					case ' ':
					case '\t':
						lastWordBreak = sb.length() - 1;
						break;
					}
				}
				return sb.toString();
			}
			if (!hasFocus) {
				StringBuilder sb = new StringBuilder();
				int lastLineBreak = 0;
				boolean showChar = true;
				for (int i = 0; i < str.length(); i++) {
					char c = str.charAt(i);
					if (c == '\n') {
						sb.append(c);
						lastLineBreak = sb.length();
						showChar = true;
						continue;
					}
					if (showChar) {
						sb.append(c);
						String recentLine = sb.substring(lastLineBreak);
						if (textfield.textExtent(recentLine).x > textfield.getTextWidth()) {
							sb.append('…');
							showChar = false;
						}
					}
				}
				return sb.toString();
			}
		}
		return str;
	}

	public void checkSpelling(boolean cancelSpellchecking) {
		if (cancelSpellchecking)
			Job.getJobManager().cancel(Constants.SPELLING);
		if (spellingOptions != ISpellCheckingService.NOSPELLING)
			new SpellCheckingJob(this, text, spellingOptions, maxSuggestions).schedule();
	}

	private void setCanvasSize() {
		PBounds newBounds = textfield.getBoundsReference();
		textCanvas.setPathToRectangle((float) newBounds.x, (float) newBounds.y, (float) newBounds.width,
				(float) newBounds.height);
	}

	public void setFocus(boolean hasFocus) {
		this.hasFocus = hasFocus;
		if (control == null || control.isDisposed())
			return;
		boolean setText = textfield.getTextWidth() != SWT.DEFAULT && (style & SWT.WRAP) == 0;
		if (hasFocus) {
			if ((style & SWT.READ_ONLY) == 0) {
				selection.x = 0;
				selection.y = setText ? text.length() : textfield.getText().length();
				wordX = -1;
				textCanvas.setStrokeColor(penColor);
			}
			Menu menu = control.getMenu();
			if (menu != null) {
				if (previousMenu != null)
					previousMenu.dispose();
				previousMenu = menu;
				control.setMenu(null);
			}
			if (menuListener == null)
				menuListener = new TextMenuListener(this, style);
			control.addMenuDetectListener(menuListener);
		} else {
			selection.x = selection.y = -1;
			textCanvas.setStrokeColor(null);
			if (incidents != null)
				processIncidents(null);
			if (previousMenu != null) {
				control.setMenu(previousMenu);
				previousMenu = null;
			}
			if (menuListener != null)
				control.removeMenuDetectListener(menuListener);
		}
		if (setText)
			doSetText(text, false);
		setCaretAndHighlight();
	}

	private void setCaretAndHighlight() {
		if (selection.x < 0) {
			setHighlight(false);
			if (caret != null)
				caret.setVisible(false);
			return;
		}
		if (highlight == null)
			createCaretAndHighlight();
		String txt = textfield.getText();
		int sx1 = 0;
		int sx2 = 0;
		int sy1 = 0;
		int sy2 = 0;
		int h = lead;
		List<String> lines2 = null;
		if (selection.x <= txt.length()) {
			String start = txt.substring(0, selection.x);
			while (true) {
				int p = start.indexOf('\n');
				if (p < 0)
					break;
				++sy1;
				start = start.substring(p + 1);
			}
			org.eclipse.swt.graphics.Point tx = textfield.textExtent(start);
			sx1 = tx.x;
			sx2 = sx1;
			sy2 = sy1;
			h = tx.y;
			if (selection.y > 0) {
				int e = selection.x + selection.y;
				if (e <= txt.length()) {
					start = txt.substring(0, e);
					lines2 = new ArrayList<String>();
					while (true) {
						int p = start.indexOf('\n');
						if (p < 0) {
							lines2.add(start);
							break;
						}
						lines2.add(start.substring(0, p));
						start = start.substring(p + 1);
					}
					sx2 = textfield.textExtent(lines2.get(lines2.size() - 1)).x;
					sy2 = lines2.size() - 1;
				}
			}
		}
		float base = textfield.getFont().getSize2D() / 4f;
		int lx = textfield.getLineOffsetAt(sy1);
		if (sx1 == sx2 && sy1 == sy2) {
			caret.setOffset(sx1 + 1f + lx, sy1 * h + base);
			caret.setVisible(true);
			setHighlight(false);
		} else {
			if (sy1 == sy2)
				highlight.setPathToRectangle(sx1 + lx, sy1 * h + base, sx2 - sx1, h);
			else if (lines2 != null) {
				float[] xp = new float[(sy2 - sy1) * 2 + 6];
				float[] yp = new float[(sy2 - sy1) * 2 + 6];
				int lx2 = textfield.getLineOffsetAt(sy2);
				xp[0] = lx2;
				yp[0] = h * (sy1 + 1) + base;
				xp[1] = sx1 + lx;
				yp[1] = yp[0];
				xp[2] = sx1 + lx;
				yp[2] = h * sy1 + base;
				int j = 3;
				for (int i = sy1; i < sy2; i++) {
					int lxi = textfield.getLineOffsetAt(i);
					int x = textfield.textExtent(lines2.get(i)).x;
					xp[j] = x + lxi;
					yp[j++] = h * i + base;
					xp[j] = x + lxi;
					yp[j++] = h * (i + 1) + base;
				}
				xp[j] = sx2 + lx2;
				yp[j++] = h * sy2 + base;
				xp[j] = sx2 + lx2;
				yp[j++] = h * (sy2 + 1) + base;
				xp[j] = lx2;
				yp[j++] = h * (sy2 + 1) + base;
				highlight.setPathToPolyline(xp, yp);
			}
			setHighlight(true);
			caret.setVisible(false);
		}
	}

	private void createCaretAndHighlight() {
		highlight = new ZPSWTPath();
		highlight.setPathToRectangle(0, 0, 30, lead);
		highlight.setStrokeColor(null);
		highlight.setLineWidth(0);
		highlight.setPickable(false);
		highlight.setVisible(false);
		addChild(highlight);
		highlight.lowerBelow(textfield);
		caret = new ZPSWTPath();
		if ((style & SWT.READ_ONLY) == 0) {
			float d = lead / 5f;
			caret.setPathToPolyline(
					new float[] { 0f, 1f - d, 1f - d, d + 1f, d + 1f, 1.5f, 1.5f, d + 1f, d + 1f, 1f - d, 1f - d, 0f,
							0f },
					new float[] { 1f, 1f, 0f, 0f, 1f, 1f, lead - 1f, lead - 1f, lead, lead, lead - 1f, lead - 1f, 1f });
			caret.setPaint(Color.cyan);
		} else {
			caret.setPathToPolyline(new float[] { 1f, 1f, -1f, -1f }, new float[] { 1f, lead - 1f, lead - 1f, 1f });
			caret.setPaint(Color.red);
		}
		caret.setStrokeColor(null);
		caret.setTransparency(0.66f);
		caret.setLineWidth(0f);
		caret.setPickable(false);
		caret.setVisible(false);
		addChild(caret);
	}

	public String getText() {
		return text;
	}

	@SuppressWarnings("fallthrough")
	public void keyPressed(PInputEvent event) {
		String oldText = textfield.getText();
		String newText = text;
		boolean editable = (style & SWT.READ_ONLY) == 0;
		int modifiers = event.getModifiers();
		if ((modifiers & InputEvent.CTRL_MASK) != 0) {
			createUndoPoint();
			int keyCode = event.getKeyCode();
			switch (keyCode) {
			case 'a':
				// Ctrl+A
				selectAll();
				break;
			case 'c':
				// Ctrl+C
				copy();
				break;
			case 'v':
				// Ctrl+V
				if (editable)
					paste();
				break;
			case 'x':
				// Ctrl+X
				if (editable)
					cut();
				break;
			case 'z':
				// Ctrl+Z
				if (editable)
					undo();
				break;
			case 'y':
				// Ctrl+Y
				if (editable)
					redo();
				break;
			}
			setCaretAndHighlight();
			return;
		}
		long time = System.currentTimeMillis();
		StringBuilder sb = new StringBuilder(oldText);
		switch (event.getKeyCode()) {
		case '\b':
			if (lastChar != '\b')
				createUndoPoint();
			lastChar = '\b';
			if (editable) {
				if (selection.y > 0) {
					newText = replace(sb, selection.x, selection.x + selection.y, ""); //$NON-NLS-1$
					selection.y = 0;
				} else if (selection.x > 0) {
					newText = replace(sb, selection.x - 1, selection.x, ""); //$NON-NLS-1$
					selection.x -= 1;
				}
			}
			break;
		case SWT.ESC:
			createUndoPoint();
			selection.y = 0;
			break;
		case SWT.DEL:
			if (lastChar != SWT.DEL)
				createUndoPoint();
			lastChar = SWT.DEL;
			if (editable) {
				if (selection.y > 0) {
					newText = replace(sb, selection.x, selection.x + selection.y, ""); //$NON-NLS-1$
					selection.y = 0;
				} else if (selection.x < sb.length())
					newText = replace(sb, selection.x, selection.x + 1, ""); //$NON-NLS-1$
			}
			break;
		case SWT.ARROW_LEFT:
			createUndoPoint();
			if (event.isShiftDown()) {
				if (selection.y == 0)
					dirleft = true;
				if (dirleft) {
					if (selection.x > 0) {
						selection.y++;
						selection.x--;
					}
				} else {
					selection.y--;
				}
			} else {
				if (selection.y > 0)
					selection.y = 0;
				else if (selection.x > 0)
					selection.x -= 1;
			}
			break;
		case SWT.ARROW_RIGHT:
			createUndoPoint();
			if (event.isShiftDown()) {
				if (selection.y == 0)
					dirleft = false;
				if (!dirleft) {
					if (selection.x + selection.y < sb.length())
						selection.y++;
				} else {
					selection.y--;
					selection.x++;
				}
			} else {
				if (selection.y > 0) {
					selection.x += selection.y;
					if (selection.x > sb.length())
						selection.x = sb.length();
					selection.y = 0;
				} else if (selection.x < sb.length())
					selection.x += 1;
			}
			break;
		case SWT.HOME:
			createUndoPoint();
			if (event.isShiftDown()) {
				if (selection.y == 0)
					dirleft = true;
				if (dirleft) {
					if (selection.x > 0) {
						selection.y += selection.x;
						selection.x = 0;
					}
				} else
					selection.y = 0;
			} else {
				selection.y = 0;
				selection.x = 0;
			}
			break;
		case SWT.END:
			createUndoPoint();
			if (event.isShiftDown()) {
				if (selection.y == 0)
					dirleft = false;
				if (!dirleft) {
					if (selection.x + selection.y < sb.length())
						selection.y = sb.length() - selection.x;
				} else
					selection.y = 0;
			} else {
				selection.y = 0;
				selection.x = sb.length();
			}
			break;
		case SWT.ARROW_UP:
		case SWT.ARROW_DOWN:
		case SWT.PAGE_DOWN:
		case SWT.PAGE_UP:
			createUndoPoint();
			break;
		default:
			if (editable) {
				char keyChar = event.getKeyChar();
				if (keyChar == 0)
					break;
				if ((timeStamp != 0 && time - timeStamp > 1000) || (!UiUtilities.isInterPunction(previousChar)
						&& UiUtilities.isInterPunction(lastChar) && Character.isWhitespace(keyChar))) {
					createUndoPoint();
				} else {
					previousChar = lastChar;
					lastChar = keyChar;
					timeStamp = time;
				}
				switch (keyChar) {
				case '\n':
				case '\r':
					if ((style & SWT.SINGLE) != 0)
						return;
					keyChar = '\n';
					// FALL THROUGH
				default:
					String str = new String(new char[] { keyChar });
					newText = replace(sb, selection.x, selection.x + selection.y, str);
					selection.y = 0;
					selection.x += 1;
					break;
				}
			}
			break;
		}
		if (!newText.equals(text)) {
			setText(newText);
			setCaretAndHighlight();
			if (validator != null) {
				String errorMessage = validator.isValid(text);
				valid = errorMessage == null;
				textfield.setPenColor(errorMessage == null ? penColor : Color.red);
				if (!listeners.isEmpty()) {
					Event e = new Event();
					e.widget = PSWTCanvas.CURRENT_CANVAS;
					e.type = SWT.Verify;
					e.text = errorMessage;
					e.data = text;
					VerifyEvent ev = new VerifyEvent(e);
					for (Object l : listeners.getListeners())
						((VerifyListener) l).verifyText(ev);
				}
			}
		} else
			setCaretAndHighlight();
	}

	public void selectAll() {
		createUndoPoint();
		selection.x = 0;
		selection.y = textfield.getText().length();
	}

	public void undo() {
		if (history != null)
			try {
				history.undo(context, null, this);
			} catch (ExecutionException e) {
				// should never happen
			}
	}

	public void redo() {
		if (history != null)
			try {
				history.redo(context, null, this);
			} catch (ExecutionException e) {
				// should never happen
			}
	}

	private void createUndoPoint() {
		if ((style & SWT.READ_ONLY) == 0) {
			IOperationHistory hist = getHistory();
			if (currentOperation != null)
				currentOperation.addToHistory(hist);
			if (currentOperation == null || !currentOperation.isEmpty())
				currentOperation = new TextOperation(this, context, text, selection);
			timeStamp = 0;
			previousChar = 0;
			lastChar = 0;
		}
	}

	public void copy() {
		createUndoPoint();
		doCopy();
	}

	private Point doCopy() {
		Point sel = selection;
		if (sel.y > 0 && sel.x >= 0) {
			Clipboard clipboard = UiActivator.getDefault().getClipboard(control.getDisplay());
			if (textfield.getTextWidth() != SWT.DEFAULT && (style & SWT.WRAP) != 0)
				sel = computeTrueSelection(selection);
			clipboard.setContents(new Object[] { text.substring(sel.x, sel.x + sel.y) },
					new Transfer[] { TextTransfer.getInstance() });
		}
		return sel;
	}

	private Point computeTrueSelection(Point sel) {
		String internal = textfield.getText();
		Point result = new Point(0, 0);
		boolean xset = false;
		boolean yset = false;
		for (int i = 0, j = 0; i < internal.length() && j < text.length(); i++) {
			char ci = internal.charAt(i);
			char ct = text.charAt(j);
			if (ci == ct || Character.isWhitespace(ci) && Character.isWhitespace(ct)) {
				if (i == sel.x) {
					result.x = j;
					xset = true;
				}
				if (xset && i == sel.x + sel.y) {
					result.y = j - result.x;
					yset = true;
				}
				++j;
			}
		}
		if (!xset)
			result.x = text.length();
		else if (!yset)
			result.y = text.length() - result.x;
		return result;
	}

	public void paste() {
		if ((style & SWT.READ_ONLY) == 0) {
			createUndoPoint();
			Clipboard clipboard = UiActivator.getDefault().getClipboard(control.getDisplay());
			Object contents = clipboard.getContents(TextTransfer.getInstance());
			if (contents instanceof String) {
				StringBuilder sb = new StringBuilder(text);
				Point sel = (textfield.getTextWidth() != SWT.DEFAULT && (style & SWT.WRAP) != 0)
						? computeTrueSelection(selection)
						: selection;
				String t = replace(sb, sel.x, sel.x + sel.y, (String) contents);
				setText(t);
				setSelection(sel.x, sel.x + ((String) contents).length());
			}
		}
	}

	public void setSelection(int start, int end) {
		selection = computeRawSelection(start, end);
	}

	private Point computeRawSelection(int start, int end) {
		Point sel = new Point(0, 0);
		String internal = textfield.getText();
		boolean xset = false;
		boolean yset = false;
		for (int i = 0, j = 0; i < internal.length() && j < text.length(); i++) {
			char ci = internal.charAt(i);
			char ct = text.charAt(j);
			if (ci == ct || Character.isWhitespace(ci) && Character.isWhitespace(ct)) {
				if (j == start) {
					sel.x = i;
					xset = true;
				}
				if (xset && j == end) {
					sel.y = i - sel.x;
					yset = true;
				}
				++j;
			}
		}
		if (!xset)
			sel.x = internal.length();
		else if (!yset)
			sel.y = internal.length() - sel.x;
		return sel;
	}

	public void cut() {
		if ((style & SWT.READ_ONLY) == 0) {
			createUndoPoint();
			Point sel = doCopy();
			if (sel.y > 0 && sel.x >= 0) {
				setText(replace(new StringBuilder(text), sel.x, sel.x + sel.y, "")); //$NON-NLS-1$
				selection.y = 0;
			}
		}
	}

	private String replace(StringBuilder sb, int start, int end, String str) {
		if ((style & SWT.WRAP) != 0 && textfield.getTextWidth() != SWT.DEFAULT) {
			StringBuilder result = new StringBuilder();
			int j = 0;
			for (int i = 0; i < start; i++) {
				char c = sb.charAt(i);
				char c2 = j < text.length() ? text.charAt(j) : 0;
				if (c == c2 || (c == '\n' && (c2 == ' ' || c2 == '\t'))) {
					result.append(c2);
					++j;
				}
			}
			for (int i = start; i < end; i++) {
				char c = sb.charAt(i);
				char c2 = j < text.length() ? text.charAt(j) : 0;
				if (c == c2 || (c == '\n' && (c2 == ' ' || c2 == '\t')))
					++j;
			}
			result.append(str);
			for (int i = end; i < sb.length(); i++) {
				char c = sb.charAt(i);
				char c2 = j < text.length() ? text.charAt(j) : 0;
				if (c == c2 || (c == '\n' && (c2 == ' ' || c2 == '\t'))) {
					result.append(c2);
					++j;
				}
			}
			sb = result;
		} else
			sb.replace(start, end, str);
		String t = sb.toString();
		if (currentOperation != null) {
			currentOperation.setReplacement(t, selection);
			currentOperation.addToHistory(getHistory());
		}
		return t;
	}

	/**
	 * Handles Drag events. Note that also mouse release events after a drag are
	 * passed as a Drag event
	 *
	 * @param startX
	 * @param startY
	 * @param positionRelativeTo
	 */
	public void mouseDragged(double startX, double startY, Point2D to) {
		int sx = Math.max(0, textfield.getOffsetAtLocation(startX, startY));
		int sy = Math.max(0, textfield.getOffsetAtLocation(to.getX(), to.getY()));
		selection.x = Math.min(sx, sy);
		selection.y = Math.abs(sy - sx);
		setCaretAndHighlight();
	}

	public void mouseReleased(PInputEvent event) {
		createUndoPoint();
		long w = event.getWhen();
		if (w - when < MINDELAYAFTERDOUBLECLICK)
			return;
		Point2D pos = event.getPositionRelativeTo(this);
		int sx = textfield.getOffsetAtLocation(pos.getX(), pos.getY());
		if (event.getClickCount() == 2) {
			when = w;
			if (wordX >= 0 && sx >= wordX && sx < wordY + wordX) {
				selection.x = 0;
				selection.y = textfield.getText().length();
				setCaretAndHighlight();
				wordX = -1;
				return;
			}
			selectWord(sx);
			setCaretAndHighlight();
			wordX = selection.x;
			wordY = selection.y;
			return;
		}
		if (sx >= 0) {
			if (event.isShiftDown() && selection.x >= 0) {
				int end = selection.x + selection.y;
				if (selection.y == 0) {
					if (sx < selection.x) {
						selection.y = selection.x - sx;
						selection.x = sx;
						dirleft = true;
					} else {
						selection.y = sx - selection.x;
						dirleft = false;
					}
				} else if (dirleft) {
					if (sx <= end) {
						selection.y += selection.x - sx;
						selection.x = sx;
					} else {
						selection.y = sx - end;
						selection.x = end;
						dirleft = false;
					}
				} else {
					if (sx >= selection.x)
						selection.y += sx - end;
					else {
						selection.y = selection.x - sx;
						selection.x = sx;
						dirleft = true;
					}
				}
			} else {
				selection.x = sx;
				selection.y = 0;
			}
			setCaretAndHighlight();
		}
	}

	private void selectWord(int offset) {
		Point sel = computeTrueSelection(new Point(offset, 0));
		int x = sel.x;
		while (x > 0) {
			char c = text.charAt(x--);
			if (!Character.isLetterOrDigit(c) && c != '_' && c != '-' && c != '\'') {
				x += 2;
				break;
			}
		}
		int y = sel.x;
		while (y < text.length()) {
			char c = text.charAt(y++);
			if (!Character.isLetterOrDigit(c) && c != '_' && c != '-' && c != '\'') {
				--y;
				break;
			}
		}
		setSelection(Math.min(x, text.length() - 1), Math.max(0, y));
	}

	public void setFont(Font font) {
		textfield.setFont(font);
	}

	public Color getSelectedPenColor() {
		return selectedPenColor;
	}

	public void setSelectedPenColor(Color selectedPenColor) {
		this.selectedPenColor = selectedPenColor;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.ui.internal.widgets.ISpellCheckingTarget#setSpellingOptions
	 * (int, int)
	 */
	public void setSpellingOptions(int maxSuggestions, int spellingOptions) {
		this.maxSuggestions = maxSuggestions;
		this.spellingOptions = spellingOptions;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.ui.internal.widgets.ISpellCheckingTarget#setSpellIncidents
	 * (java.util.List)
	 */
	public void setSpellIncidents(final List<ISpellIncident> incidents) {
		if (control != null && !control.isDisposed())
			control.getDisplay().asyncExec(() -> {
				if (!control.isDisposed())
					processIncidents(incidents.toArray(new ISpellIncident[incidents.size()]));
			});
	}

	private void processIncidents(final ISpellIncident[] incid) {
		this.incidents = incid;
		if (currentSegments != null) {
			for (PSWTPath segment : currentSegments)
				removeChild(segment);
			currentSegments.clear();
		}
		if (incid != null && incid.length > 0) {
			if (currentSegments == null)
				currentSegments = new ArrayList<PSWTPath>(incid.length);
			int charHeight = lead;
			int textWidth = textfield.getTextWidth();
			for (ISpellIncident incident : incid) {
				int offset = incident.getOffset();
				Point wordBounds = computeRawSelection(offset, offset + incident.getWrongWord().length());
				// Convert to coordinates
				org.eclipse.swt.graphics.Point off1 = textfield.getLocationAtOffset(wordBounds.x);
				if (off1 != null) {
					off1.y -= charHeight / 9;
					org.eclipse.swt.graphics.Point off2 = textfield.getLocationAtOffset(wordBounds.x + wordBounds.y);
					if (off2 != null) {
						off2.y -= charHeight / 9;
						if (off2.y > off1.y) {
							currentSegments.add(createRedSeaSegment(computePolyline(off1,
									new org.eclipse.swt.graphics.Point(textWidth, off1.y), charHeight)));
							off1.x = 0;
							off1.y = off2.y;
						}
						currentSegments.add(createRedSeaSegment(computePolyline(off1, off2, charHeight)));
					}
				}
			}
		}
	}

	private PSWTPath createRedSeaSegment(Point2D[] pl) {
		PSWTPath segment = new PSWTPath();
		segment.setStrokeColor(Color.red);
		segment.setTransparency(0.66f);
		segment.setVisible(true);
		segment.setPickable(false);
		segment.setPathToPolyline(pl);
		addChild(segment);
		return segment;
	}

	public static Point2D[] computePolyline(org.eclipse.swt.graphics.Point left, org.eclipse.swt.graphics.Point right,
			int height) {

		final int WIDTH = 3;
		final int HEIGHT = 2;

		int w2 = 2 * WIDTH;
		int peeks = (right.x - left.x) / w2;

		int leftX = left.x;

		// compute (number of points) * 2
		int length = 2 * peeks + 1;
		if (length <= 0)
			return new Point2D[0];

		Point2D[] coordinates = new Point2D[length];

		// compute top and bottom of peeks
		int bottom = left.y + height;
		int top = bottom - HEIGHT;

		// populate array with peek coordinates
		int index = 0;
		for (int i = 0; i < peeks; i++) {
			int x = leftX + (w2 * i);
			coordinates[index++] = new java.awt.Point(x, bottom);
			coordinates[index++] = new java.awt.Point(x + WIDTH, top);
		}
		// add the last down flank
		coordinates[length - 1] = new java.awt.Point(left.x + (w2 * peeks), bottom + 1);
		return coordinates;
	}

	public boolean hasSpellingErrors() {
		return incidents != null && incidents.length > 0;
	}

	public void setValidator(IInputValidator validator) {
		this.validator = validator;
	}

	public void addErrorListener(VerifyListener listener) {
		listeners.add(listener);
	}

	public void removeErrorListener(VerifyListener listener) {
		listeners.remove(listener);
	}

	public boolean isValid() {
		return valid;
	}

	public int getStyle() {
		return style;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == Shell.class)
			return control.getShell();
		return null;
	}

	@Override
	public void setSelection(Point selection) {
		this.selection.x = selection.x;
		this.selection.y = selection.y;
	}

	public ISpellIncident findSpellIncident(MenuDetectEvent e) {
		if (currentSegments != null && !currentSegments.isEmpty() && incidents != null) {
			Point cp = control.toControl(e.x, e.y);
			Point2D loc = new java.awt.Point(cp.x, cp.y);
			PPickPath pickPath = PPickPath.CURRENT_PICK_PATH;
			Point2D canvasToLocal = pickPath.canvasToLocal(loc, textfield);
			localToGlobal(canvasToLocal);
			try {
				for (ISpellIncident inc : incidents) {
					// Convert to coordinates
					Point wordBounds = computeRawSelection(inc.getOffset(),
							inc.getOffset() + inc.getWrongWord().length());
					org.eclipse.swt.graphics.Point startTop = textfield.getLocationAtOffset(wordBounds.x);
					org.eclipse.swt.graphics.Point endTop = textfield.getLocationAtOffset(wordBounds.x + wordBounds.y);
					if (startTop != null && endTop != null) {
						Point2D st = new java.awt.Point(startTop.x, startTop.y);
						localToGlobal(st);
						Point2D et = new java.awt.Point(endTop.x, endTop.y);
						localToGlobal(et);
						Point2D sb = new java.awt.Point(startTop.x, startTop.y + lead);
						localToGlobal(sb);
						if (st.getY() == et.getY()) {
							if (st.getY() <= loc.getY() && st.getX() <= loc.getX() && sb.getY() >= loc.getY()
									&& et.getX() >= loc.getX()) {
								return inc;
							}
						} else {
							Point2D eb = new java.awt.Point(endTop.x, endTop.y + lead);
							localToGlobal(eb);
							if (loc.getY() > sb.getY() && loc.getY() < et.getY()
									|| loc.getY() >= st.getY() && loc.getY() <= sb.getY() && loc.getX() >= st.getX()
									|| loc.getY() >= et.getY() && loc.getY() <= eb.getY() && loc.getX() <= et.getX()) {
								return inc;
							}
						}
					}
				}
			} catch (IllegalArgumentException e1) {
				// nothing found
			}
		}
		return null;
	}

	@Override
	public Control getControl() {
		return control;
	}

	@Override
	public boolean canUndo() {
		return getHistory().canUndo(context);
	}

	@Override
	public boolean canRedo() {
		return getHistory().canRedo(context);
	}

	@Override
	public Point getSelection() {
		return selection;
	}

	@Override
	public void applyCorrection(ISpellIncident incident, String newWord) {
		createUndoPoint();
		int woff = incident.getOffset();
		setText(replace(new StringBuilder(text), woff, woff + incident.getWrongWord().length(), newWord));
	}

}
