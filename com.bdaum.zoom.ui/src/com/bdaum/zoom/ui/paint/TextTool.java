/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.bdaum.zoom.ui.paint;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A text drawing tool.
 */
public class TextTool extends BasicPaintSession implements PaintTool {
	private ToolSettings settings;
	private String drawText = PaintExample.getResourceString("tool.Text.settings.defaulttext"); //$NON-NLS-1$

	/**
	 * Constructs a PaintTool.
	 * 
	 * @param toolSettings
	 *            the new tool settings
	 * @param paintExample
	 */
	public TextTool(ToolSettings toolSettings, PaintExample paintExample) {
		super(paintExample);
		set(toolSettings);
	}

	/**
	 * Sets the tool's settings.
	 * 
	 * @param toolSettings
	 *            the new tool settings
	 */

	public void set(ToolSettings toolSettings) {
		settings = toolSettings;
	}

	/**
	 * Returns name associated with this tool.
	 * 
	 * @return the localized name of this tool
	 */

	public String getDisplayName() {
		return PaintExample.getResourceString("tool.Text.label"); //$NON-NLS-1$
	}

	/**
	 * Activates the tool.
	 */

	public void beginSession() {
		super.beginSession();
		getPaintSurface().setStatusMessage(PaintExample.getResourceString("session.Text.message")); //$NON-NLS-1$
	}

	/**
	 * Deactivates the tool.
	 */

	public void endSession() {
		getPaintSurface().clearRubberbandSelection();
		super.endSession();
	}

	/**
	 * Aborts the current operation.
	 */

	public void resetSession() {
		getPaintSurface().clearRubberbandSelection();
	}
	
	@Override
	public void handleEvent(Event e) {
		switch (e.type) {
		case SWT.MouseDown:
			if (e.button == 1)
				// draw with left mouse button
				getPaintSurface().commitRubberbandSelection();
			else {
				// set text with right mouse button
				getPaintSurface().clearRubberbandSelection();
				Shell shell = getPaintSurface().getShell();
				final Shell dialog = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
				dialog.setText(PaintExample.getResourceString("tool.Text.dialog.title")); //$NON-NLS-1$
				dialog.setLayout(new GridLayout());
				Label label = new Label(dialog, SWT.NONE);
				label.setText(PaintExample.getResourceString("tool.Text.dialog.message")); //$NON-NLS-1$
				label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
				final Text field = new Text(dialog, SWT.SINGLE | SWT.BORDER);
				field.setText(drawText);
				field.selectAll();
				field.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				Composite buttons = new Composite(dialog, SWT.NONE);
				GridLayout layout = new GridLayout(2, true);
				layout.marginWidth = 0;
				buttons.setLayout(layout);
				buttons.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
				Button ok = new Button(buttons, SWT.PUSH);
				ok.setText(PaintExample.getResourceString("OK")); //$NON-NLS-1$
				ok.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
				Listener listener = new Listener() {
					@Override
					public void handleEvent(Event e) {
						if (e.widget == ok)
							drawText = field.getText();
						dialog.dispose();
					}
				};
				ok.addListener(SWT.Selection, listener);
				Button cancel = new Button(buttons, SWT.PUSH);
				cancel.setText(PaintExample.getResourceString("Cancel")); //$NON-NLS-1$
				cancel.addListener(SWT.Selection, listener);
				dialog.setDefaultButton(ok);
				dialog.pack();
				dialog.open();
				Display display = dialog.getDisplay();
				while (!shell.isDisposed() && !dialog.isDisposed())
					if (!display.readAndDispatch())
						display.sleep();
			}
			break;
		case SWT.MouseMove:
			final PaintSurface ps = getPaintSurface();
			ps.setStatusCoord(ps.getCurrentPosition());
			ps.clearRubberbandSelection();
			ps.addRubberbandSelection(
					new TextFigure(settings.commonForegroundColor, settings.commonFont, drawText, e.x, e.y));
			break;
		}
	}

}
