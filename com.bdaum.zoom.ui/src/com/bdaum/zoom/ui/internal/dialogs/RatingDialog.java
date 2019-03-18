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
 * (c) 2009-2018 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.dialogs;

import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.ai.IAiService;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.Icons.Icon;
import com.bdaum.zoom.ui.internal.widgets.ZDialog;

@SuppressWarnings("restriction")
public class RatingDialog extends ZDialog implements PaintListener, MouseMoveListener, MouseListener, KeyListener {

	public static final int BYSERVICE = -5;
	public static final int SELECTABORT = -4;
	public static final int ABORT = -2;
	public static final int DELETE = -3;
	public static final int UNDEF = -1;
	private static final int nfields = 8;
	private static final int zero = nfields - 6;
	private int rating;
	private Canvas canvas;
	private int width;
	private int height;
	private int buttonHeight = 0;
	private Icon trash;
	private double scale;
	private boolean select;
	private boolean focusWatch = true;
	private boolean ai;
	private int timeout = 0;

	public RatingDialog(Shell parentShell, int rating, double scale, boolean focusWatch, boolean ai) {
		super(parentShell);
		this.focusWatch = focusWatch;
		this.ai = ai;
		setShellStyle(SWT.NO_TRIM);
		this.rating = rating;
		this.scale = scale;
		this.select = false;
		trash = rating == DELETE ? Icons.trashrestore : Icons.trash;
	}

	public RatingDialog(Shell parentShell, int rating) {
		super(parentShell);
		setShellStyle(SWT.NO_TRIM);
		this.rating = rating;
		this.scale = 0.5d;
		this.select = true;
		trash = Icons.rating6a;
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
		canvas.addMouseListener(this);
		canvas.addMouseMoveListener(this);
		canvas.addKeyListener(this);
		Rectangle bounds = Icons.rating61.getImage().getBounds();
		width = (int) (bounds.width * scale);
		height = (int) (bounds.height * scale);
		if (ai) {
			IAiService aiService = CoreActivator.getDefault().getAiService();
			if (aiService != null && aiService.isEnabled() && aiService.getRatingProviderIds().length > 0)
				buttonHeight = height;
		}
		Shell shell = getShell();
		shell.setSize(nfields * width + 8, height + 2 + buttonHeight);
		shell.layout();
		canvas.redraw();
		canvas.setFocus();
		return comp;
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		return null;
	}

	@Override
	public int open() {
		if (timeout > 0) {
			Shell shell = getShell();
			shell.getDisplay().timerExec(timeout, new Runnable() {
				@Override
				public void run() {
					if (!shell.isDisposed())
						close();
				}
			});
		}
		return (super.open() != Window.CANCEL) ? rating : select ? SELECTABORT : ABORT;
	}

	public void paintControl(PaintEvent e) {
		GC gc = e.gc;
		gc.setBackground(e.display.getSystemColor(SWT.COLOR_WHITE));
		int w = nfields * width + 6;
		gc.fillRectangle(0, 0, w, height);
		for (int i = 0; i < nfields; i++) {
			int r = i - zero;
			if (i == 0)
				drawCell(e, trash.getImage(), 0, select ? QueryField.SELECTALL : DELETE);
			else if (i == 1)
				drawCell(e, (select ? Icons.rating6u : Icons.rating6r).getImage(), 1,
						select ? QueryField.SELECTUNDEF : UNDEF);
			else if (i == zero)
				drawCell(e, Icons.rating60.getImage(), i, r);
			else if (i > zero)
				drawCell(e, Icons.rating61.getImage(), i, r);
		}
		if (buttonHeight > 0) {
			String text = Messages.RatingDialog_use_rating_service;
			Point tx = gc.textExtent(text);
			gc.drawText(text, (w - tx.x) / 2, height + (height - tx.y) / 2, true);
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

	@Override
	public void mouseMove(MouseEvent e) {
		if (buttonHeight > 0 && e.y > height) {
			canvas.setToolTipText(Messages.RatingDialog_start_automated);
			return;
		}
		int r = e.x / width;
		switch (r) {
		case 0:
			canvas.setToolTipText(select ? Messages.RatingDialog_all : Messages.RatingDialog_delete);
			break;
		case 1:
			canvas.setToolTipText(select ? Messages.RatingDialog_unrated : Messages.RatingDialog_remove);
			break;
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
		case 7:
			canvas.setToolTipText(NLS.bind("{0} ({1})", QueryField.RATING.getEnumLabels()[r - 1], r - 2)); //$NON-NLS-1$
			break;
		default:
			canvas.setToolTipText(""); //$NON-NLS-1$
			break;
		}

	}

	@Override
	public void mouseDown(MouseEvent e) {
		if (buttonHeight > 0 && e.y > height) {
			rating = BYSERVICE;
			close();
			return;
		}
		int r = e.x / width;
		switch (r) {
		case 0:
			rating = select ? QueryField.SELECTALL : DELETE;
			break;
		case 1:
			rating = select ? QueryField.SELECTUNDEF : UNDEF;
			break;
		default:
			rating = r - 2;
		}
		canvas.redraw();
		getShell().getDisplay().timerExec(100, () -> {
			if (!canvas.isDisposed())
				close();
		});
	}

	@Override
	public void mouseDoubleClick(MouseEvent e) {
		// do nothing
	}

	@Override
	public void mouseUp(MouseEvent e) {
		// do nothing
	}

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
		case 'a':
			if (select) {
				rating = QueryField.SELECTALL;
				canvas.redraw();
			}
			break;
		case '-':
			rating = select ? QueryField.SELECTUNDEF : UNDEF;
			canvas.redraw();
			break;
		case '*':
			if (buttonHeight > 0) {
				rating = BYSERVICE;
				close();
			}
			break;
		default:
			switch (e.keyCode) {
			case SWT.ESC:
				setReturnCode(Window.CANCEL);
				close();
				break;
			case SWT.DEL:
				if (!select) {
					rating = DELETE;
					canvas.redraw();
				}
			}
			break;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// do nothing
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

}
