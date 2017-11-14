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

package com.bdaum.zoom.ui.internal.widgets;

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.UndoContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.ISpellCheckingService;
import com.bdaum.zoom.core.ISpellCheckingService.ISpellIncident;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.job.SpellCheckingJob;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;

import jdk.nashorn.tools.Shell;

public class CheckedText extends Composite
		implements ISpellCheckingTarget, PaintListener, IAugmentedTextField, IAdaptable {
	private static final int[] NOREDSEA = new int[0];
	private StyledText control;
	private int maxSuggestions = 10;
	private int spellingOptions = ISpellCheckingService.KEYWORDOPTIONS;
	private ISpellIncident[] incidents;
	private FocusListener focusListener;
	private String hint = ""; //$NON-NLS-1$
	protected boolean hintShown;
	protected Color originalTextColor;
	private ListenerList<ModifyListener> modifyListeners;
	private ListenerList<VerifyListener> verifyListeners;
	private IOperationHistory history;
	private IUndoContext context;
	private char lastChar;
	private char previousChar;
	private TextOperation currentOperation;
	private long timeStamp;
	private ModifyListener afterListener;
	protected MenuDetectListener menuListener;
	protected Menu previousMenu;
	private VerifyListener beforeListener;
	protected boolean lastWasDel;
	private int style;

	public CheckedText(Composite parent, int style) {
		this(parent, style, ISpellCheckingService.DESCRIPTIONOPTIONS);
	}

	public CheckedText(Composite parent, int style, int spellingOptions) {
		super(parent, SWT.NONE);
		this.style = style;
		this.spellingOptions = spellingOptions;
		control = new StyledText(this, style);
		setLayout(new FillLayout());
		installUndoSupport();
		if (spellingOptions != ISpellCheckingService.NOSPELLING)
			addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					if (!control.getText().isEmpty())
						checkSpelling(true);
				}
			});
		control.addPaintListener(this);
		installMenu(style);
	}

	protected void installUndoSupport() {
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				int modifiers = event.stateMask;
				if ((modifiers & SWT.CTRL) != 0) {
					int keyCode = event.keyCode;
					switch (keyCode) {
					case 'a':
						selectAll();
						break;
					// c,v,x handled by StyledText
					case 'z':
						// Ctrl+Z
						if ((style & SWT.READ_ONLY) == 0)
							undo();
						break;
					case 'y':
						// Ctrl+Y
						if ((style & SWT.READ_ONLY) == 0)
							redo();
						break;
					}
					return;
				}
				switch (event.keyCode) {
				case SWT.ESC:
				case SWT.ARROW_LEFT:
				case SWT.ARROW_RIGHT:
				case SWT.ARROW_UP:
				case SWT.ARROW_DOWN:
				case SWT.HOME:
				case SWT.END:
				case SWT.PAGE_DOWN:
				case SWT.PAGE_UP:
					createUndoPoint();
					break;
				}
			}
		});
		if ((style & SWT.READ_ONLY) == 0) {
			beforeListener = new VerifyListener() {
				@Override
				public void verifyText(VerifyEvent e) {
					String text = e.text;
					if (text.length() == 0 || e.start != e.end) {
						if (!lastWasDel) {
							createUndoPoint();
							lastWasDel = true;
						}
					} else
						lastWasDel = false;
					if (text.length() > 1)
						createUndoPoint();
					else {
						char keyChar = e.character;
						if (keyChar != 0) {
							long time = System.currentTimeMillis();
							if ((timeStamp != 0 && time - timeStamp > 1000)
									|| (!UiUtilities.isInterPunction(previousChar)
											&& UiUtilities.isInterPunction(lastChar)
											&& Character.isWhitespace(keyChar))) {
								createUndoPoint();
							} else {
								previousChar = lastChar;
								lastChar = keyChar;
								timeStamp = time;
							}
						}
					}
				}
			};
			addVerifyListener(beforeListener);
			afterListener = new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					if (currentOperation != null) {
						currentOperation.setReplacement(getText(), getSelection());
						currentOperation.addToHistory(getHistory());
					}
				}
			};
			addModifyListener(afterListener);
		}
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				createUndoPoint();
			}
		});
	}

	@Override
	public void dispose() {
		if (history != null) {
			history.dispose(context, true, true, true);
			history = null;
			context = null;
		}
		super.dispose();
	}

	protected void installMenu(int style) {
		addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				if (previousMenu != null) {
					control.setMenu(previousMenu);
					previousMenu = null;
				}
				if (menuListener != null)
					control.removeMenuDetectListener(menuListener);
			}

			@Override
			public void focusGained(FocusEvent e) {
				Menu menu = control.getMenu();
				if (menu != null) {
					if (previousMenu != null)
						previousMenu.dispose();
					previousMenu = menu;
					control.setMenu(null);
				}
				if (menuListener == null)
					menuListener = createMenuListener(style);
				control.addMenuDetectListener(menuListener);
			}
		});
	}

	protected MenuDetectListener createMenuListener(int style) {
		MenuDetectListener listener = new TextMenuListener(this, style);
		control.addMenuDetectListener(listener);
		return listener;
	}

	public void redo() {
		if (history != null)
			try {
				removeVerifyListener(beforeListener);
				removeModifyListener(afterListener);
				history.redo(context, null, this);
			} catch (ExecutionException e) {
				// should never happen
			} finally {
				addModifyListener(afterListener);
				addVerifyListener(beforeListener);
			}
	}

	public void undo() {
		if (history != null)
			try {
				removeVerifyListener(beforeListener);
				removeModifyListener(afterListener);
				history.undo(context, null, this);
			} catch (ExecutionException e) {
				// should never happen
			} finally {
				addModifyListener(afterListener);
				addVerifyListener(beforeListener);
			}
	}

	private IOperationHistory getHistory() {
		if (history == null) {
			history = PlatformUI.getWorkbench().getOperationSupport().getOperationHistory();
			context = new UndoContext();
			history.setLimit(context,
					UiActivator.getDefault().getPreferenceStore().getInt(PreferenceConstants.UNDOLEVELS));
		}
		return history;
	}

	private void createUndoPoint() {
		if ((style & SWT.READ_ONLY) == 0) {
			IOperationHistory hist = getHistory();
			if (currentOperation != null)
				currentOperation.addToHistory(hist);
			if (currentOperation == null || !currentOperation.isEmpty())
				currentOperation = new TextOperation(this, context, getText(), getSelection());
			timeStamp = 0;
			previousChar = 0;
			lastChar = 0;
		}
	}

	public void setHint(String hint) {
		if (hint == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		this.hint = hint;
		if (!hint.isEmpty()) {
			focusListener = new FocusListener() {
				@Override
				public void focusLost(FocusEvent e) {
					showHint();
				}

				@Override
				public void focusGained(FocusEvent e) {
					removeHint();
				}
			};
			showHint();
			control.addFocusListener(focusListener);
		} else if (focusListener != null) {
			control.removeFocusListener(focusListener);
			removeHint();
		}
	}

	protected void showHint() {
		if (!hintShown) {
			String text = control.getText();
			if (text.isEmpty()) {
				removeAllListeners();
				originalTextColor = control.getForeground();
				control.setForeground(control.getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
				control.setText(hint);
				hintShown = true;
			}
		}
	}

	protected void removeHint() {
		if (hintShown) {
			control.setForeground(originalTextColor);
			control.setText(""); //$NON-NLS-1$
			hintShown = false;
			addAllListeners();
		}
	}

	protected void addAllListeners() {
		if (modifyListeners != null)
			for (ModifyListener modifyListener : modifyListeners)
				control.addModifyListener(modifyListener);
		if (verifyListeners != null)
			for (VerifyListener verifyListener : verifyListeners)
				control.addVerifyListener(verifyListener);
	}

	protected void removeAllListeners() {
		if (modifyListeners != null)
			for (ModifyListener modifyListener : modifyListeners)
				control.removeModifyListener(modifyListener);
		if (verifyListeners != null)
			for (VerifyListener verifyListener : verifyListeners)
				control.removeVerifyListener(verifyListener);
	}

	public void checkSpelling(boolean cancelSpellchecking) {
		if (cancelSpellchecking)
			Job.getJobManager().cancel(Constants.SPELLING);
		new SpellCheckingJob(this, getText(), spellingOptions, maxSuggestions).schedule(10);
	}

	/**
	 * @return the control
	 */
	public StyledText getControl() {
		return control;
	}

	public String getText() {
		return control.getText();
	}

	public void setText(String text) {
		if (beforeListener != null)
			removeVerifyListener(beforeListener);
		if (afterListener != null)
			removeModifyListener(afterListener);
		control.setText(text == null ? "" : text); //$NON-NLS-1$
		if (beforeListener != null)
			addVerifyListener(beforeListener);
		if (afterListener != null)
			addModifyListener(afterListener);
	}

	public void addVerifyListener(VerifyListener listener) {
		if (verifyListeners == null)
			verifyListeners = new ListenerList<>();
		verifyListeners.add(listener);
		control.addVerifyListener(listener);
	}

	public void removeVerifyListener(VerifyListener listener) {
		if (verifyListeners != null) {
			verifyListeners.remove(listener);
			control.removeVerifyListener(listener);
		}
	}

	@Override
	public boolean setFocus() {
		return control.setFocus();
	}

	public void setSelection(Point point) {
		control.setSelection(point);
	}

	@Override
	public void setEnabled(boolean enabled) {
		control.setEnabled(enabled);
	}

	/**
	 * @param listener
	 * @see org.eclipse.swt.widgets.Control#addMouseListener(org.eclipse.swt.events.MouseListener)
	 */
	@Override
	public void addMouseListener(MouseListener listener) {
		control.addMouseListener(listener);
	}

	/**
	 * @param listener
	 * @see org.eclipse.swt.widgets.Control#removeMouseListener(org.eclipse.swt.events.MouseListener)
	 */
	@Override
	public void removeMouseListener(MouseListener listener) {
		control.removeMouseListener(listener);
	}

	public void setSpellingOptions(int maxSuggestions, int spellingOptions) {
		this.maxSuggestions = maxSuggestions;
		if (this.spellingOptions != spellingOptions) {
			if (spellingOptions == ISpellCheckingService.NOSPELLING)
				control.removePaintListener(this);
			else if (this.spellingOptions == ISpellCheckingService.NOSPELLING)
				control.addPaintListener(this);
		}
	}

	public void setSpellIncidents(List<ISpellIncident> incidents) {
		if (!control.isDisposed()) {
			this.incidents = incidents.toArray(new ISpellIncident[incidents.size()]);
			control.getDisplay().asyncExec(() -> {
				if (!control.isDisposed())
					control.redraw();
			});
		}
	}

	public void paintControl(PaintEvent e) {
		if (control.isDisposed())
			return;
		if (incidents != null && incidents.length > 0) {
			e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_RED));
			int charHeight = e.gc.getFontMetrics().getHeight();
			for (ISpellIncident incident : incidents) {
				int offset = incident.getOffset();
				// Convert to coordinates
				try {
					int charCount = control.getCharCount();
					if (offset >= 0 && offset < charCount) {
						Point off1 = control.getLocationAtOffset(offset);
						if (off1 != null) {
							Point off2 = control.getLocationAtOffset(offset + incident.getWrongWord().length());
							if (off2 != null)
								e.gc.drawPolyline(computePolyline(off1, off2, charHeight));
						}
					}
				} catch (Exception ex) {
					// do nothing
				}
			}
		}
	}

	public static int[] computePolyline(Point left, Point right, int height) {

		final int WIDTH = 3;
		final int HEIGHT = 2;

		int w2 = 2 * WIDTH;
		int peeks = (right.x - left.x) / w2;

		int leftX = left.x;

		// compute (number of points) * 2
		int length = 4 * peeks + 2;
		if (length <= 0)
			return NOREDSEA;

		int[] coordinates = new int[length];

		// compute top and bottom of peeks
		int bottom = left.y + height;
		int top = bottom - HEIGHT;

		// populate array with peek coordinates
		int index = 0;
		for (int i = 0; i < peeks; i++) {
			coordinates[index++] = leftX + (w2 * i);
			coordinates[index++] = bottom;
			coordinates[index++] = coordinates[index - 3] + WIDTH;
			coordinates[index++] = top;
		}
		// add the last down flank
		coordinates[length - 2] = left.x + (w2 * peeks - WIDTH / 2);
		coordinates[length - 1] = (top + bottom + 1) / 2;
		return coordinates;
	}

	/**
	 * @param listener
	 * @see org.eclipse.swt.widgets.Control#addFocusListener(org.eclipse.swt.events.FocusListener)
	 */
	@Override
	public void addFocusListener(FocusListener listener) {
		control.addFocusListener(listener);
	}

	/**
	 * @param listener
	 * @see org.eclipse.swt.widgets.Control#addKeyListener(org.eclipse.swt.events.KeyListener)
	 */
	@Override
	public void addKeyListener(KeyListener listener) {
		control.addKeyListener(listener);
	}

	/**
	 * @param listener
	 * @see org.eclipse.swt.widgets.Control#removeFocusListener(org.eclipse.swt.events.FocusListener)
	 */
	@Override
	public void removeFocusListener(FocusListener listener) {
		control.removeFocusListener(listener);
	}

	/**
	 * @param listener
	 * @see org.eclipse.swt.widgets.Control#removeKeyListener(org.eclipse.swt.events.KeyListener)
	 */
	@Override
	public void removeKeyListener(KeyListener listener) {
		control.removeKeyListener(listener);
	}

	@Override
	public void setFont(Font font) {
		control.setFont(font);
	}

	/**
	 *
	 * @see org.eclipse.swt.custom.StyledText#selectAll()
	 */
	public void selectAll() {
		createUndoPoint();
		control.selectAll();
	}

	/**
	 * @return
	 * @see org.eclipse.swt.widgets.Control#getBorderWidth()
	 */
	@Override
	public int getBorderWidth() {
		return control != null ? control.getBorderWidth() : super.getBorderWidth();
	}

	/**
	 * @param color
	 * @see org.eclipse.swt.custom.StyledText#setBackground(org.eclipse.swt.graphics.Color)
	 */
	@Override
	public void setBackground(Color color) {
		control.setBackground(color);
	}

	/**
	 * @param color
	 * @see org.eclipse.swt.custom.StyledText#setForeground(org.eclipse.swt.graphics.Color)
	 */
	@Override
	public void setForeground(Color color) {
		control.setForeground(color);
	}

	@Override
	public Color getForeground() {
		return control.getForeground();
	}

	public Point getSelection() {
		return control.getSelection();
	}

	public void addModifyListener(ModifyListener listener) {
		if (modifyListeners == null)
			modifyListeners = new ListenerList<>();
		modifyListeners.add(listener);
		control.addModifyListener(listener);
	}

	public void removeModifyListener(ModifyListener listener) {
		if (modifyListeners != null) {
			modifyListeners.remove(listener);
			control.removeModifyListener(listener);
		}
	}

	/**
	 * @param listener
	 * @see org.eclipse.swt.widgets.Control#addTraverseListener(org.eclipse.swt.events.TraverseListener)
	 */
	@Override
	public void addTraverseListener(TraverseListener listener) {
		control.addTraverseListener(listener);
	}

	/**
	 * @param listener
	 * @see org.eclipse.swt.widgets.Control#removeTraverseListener(org.eclipse.swt.events.TraverseListener)
	 */
	@Override
	public void removeTraverseListener(TraverseListener listener) {
		control.removeTraverseListener(listener);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == Shell.class)
			return control.getShell();
		return null;
	}

	@Override
	public void cut() {
		if ((style & SWT.READ_ONLY) == 0) {
			createUndoPoint();
			control.cut();
		}
	}

	@Override
	public void copy() {
		createUndoPoint();
		control.copy();
	}

	@Override
	public void paste() {
		if ((style & SWT.READ_ONLY) == 0) {
			createUndoPoint();
			control.paste();
		}
	}

	@Override
	public ISpellIncident findSpellIncident(MenuDetectEvent e) {
		if (incidents != null) {
			try {
				Point loc = control.toControl(e.x, e.y);
				int offset = control.getOffsetAtLocation(loc);
				for (ISpellIncident inc : incidents) {
					if (inc.happensAt(offset)) {
						return inc;
					}
				}
			} catch (IllegalArgumentException e1) {
				// nothing found
			}
		}
		return null;
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
	public void applyCorrection(ISpellIncident incident, String newWord) {
		String oldText = control.getText();
		int woff = incident.getOffset();
		control.setText(
				oldText.substring(0, woff) + newWord + oldText.substring(woff + incident.getWrongWord().length()));
	}

}
