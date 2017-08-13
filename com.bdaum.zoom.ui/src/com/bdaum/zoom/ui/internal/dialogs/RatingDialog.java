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

package com.bdaum.zoom.ui.internal.dialogs;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.Icons.Icon;
import com.bdaum.zoom.ui.internal.widgets.ZDialog;

public class RatingDialog extends ZDialog implements PaintListener {

	public static final int SELECTABORT = -3;
	public static final int ABORT = -1;
	public static final int DELETE = -2;
	private int rating;
	private Canvas canvas;
	private int width;
	private int height;
	private Icon trash;
	private double scale;
	private int nfields;
	private boolean select;
	private boolean focusWatch = true;

	public RatingDialog(Shell parentShell, int rating, double scale, boolean focusWatch) {
		super(parentShell);
		this.focusWatch = focusWatch;
		setShellStyle(SWT.NO_TRIM);
		this.rating = rating;
		this.scale = scale;
		this.nfields = 7;
		this.select = false;
		trash = rating == DELETE ? Icons.trashrestore : Icons.trash;
	}

	public RatingDialog(Shell parentShell, int rating) {
		super(parentShell);
		setShellStyle(SWT.NO_TRIM);
		this.rating = rating;
		this.scale = 0.5d;
		this.nfields = 8;
		this.select = true;
		trash = Icons.ratingAll;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite comp = (Composite) super.createDialogArea(parent);
		comp.setLayout(new FillLayout());
		canvas = new Canvas(comp, SWT.DOUBLE_BUFFERED | SWT.BORDER);
		canvas.addPaintListener(this);
		if (focusWatch)
			canvas.addFocusListener(new FocusAdapter() {

				@Override
				public void focusLost(FocusEvent e) {
					rating = select ? SELECTABORT : Constants.RATING_UNDEFINED;
					close();
				}
			});
		else
			getShell().addShellListener(new ShellAdapter() {

				@Override
				public void shellDeactivated(ShellEvent e) {
					rating = select ? SELECTABORT : Constants.RATING_UNDEFINED;
					close();
				}
			});
		canvas.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDown(MouseEvent e) {
				int r = e.x / width - 1;
				rating = select ? r - 1 : r < 0 ? DELETE : r;
				canvas.redraw();
				getShell().getDisplay().timerExec(100, () -> {
					if (!canvas.isDisposed())
						close();
				});

			}
		});
		canvas.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				switch (e.character) {
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
					rating = e.character - '0';
					canvas.redraw();
					break;
				default:
					switch (e.keyCode) {
					case SWT.ESC:
						setReturnCode(Window.CANCEL);
						close();
						break;
					case SWT.DEL:
						rating = DELETE;
						canvas.redraw();
					}
					break;
				}

			}

		});
		canvas.redraw();
		Rectangle bounds = Icons.rating61.getImage().getBounds();
		width = (int) (bounds.width * scale);
		height = (int) (bounds.height * scale);
		Shell shell = getShell();
		shell.setSize(nfields * width + 2, height + 2);
		shell.layout();
		canvas.setFocus();
		return comp;
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		return null;
	}

	@Override
	public int open() {
		int open = super.open();
		return (open != Window.CANCEL) ? rating : select ? SELECTABORT : ABORT;
	}

	public void paintControl(PaintEvent e) {
		GC gc = e.gc;
		gc.setBackground(e.display.getSystemColor(SWT.COLOR_WHITE));
		gc.fillRectangle(0, 0, nfields * width, height);
		int zero = nfields - 6;
		for (int i = 0; i < nfields; i++) {
			int r = i - zero;
			if (i == 0) {
				drawCell(e, trash.getImage(), i, DELETE);
			} else if (i == 1 && select) {
				drawCell(e, Icons.rating60.getImage(), i, QueryField.SELECTUNDEF);
			} else if (i == zero) {
				drawCell(e, null, i, r);
			} else if (i > zero) {
				drawCell(e, Icons.rating61.getImage(), i, r);
			}
		}
	}

	private void drawCell(PaintEvent e, Image image, int i, int r) {
		GC gc = e.gc;
		if (r == rating) {
			gc.setBackground(e.display.getSystemColor(SWT.COLOR_DARK_GRAY));
			gc.fillRectangle(width * i, 0, width, height);
		}
		if (image != null) {
			Rectangle bounds = image.getBounds();
			double factor = r < 0 ? 0.8d : (i + 0.6d) / 5.6d;
			int w = (int) (width * factor + 0.5d);
			int h = (int) (height * factor + 0.5d);
			gc.drawImage(image, bounds.x, bounds.y, bounds.width, bounds.height, width * i + (width - w) / 2,
					(height - h) / 2, w, h);
		}
	}
}
