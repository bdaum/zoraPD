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
 * (c) 2017 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.bdaum.zoom.core.ISpellCheckingService.ISpellIncident;
import com.bdaum.zoom.ui.internal.UiActivator;

final class TextMenuListener implements MenuDetectListener {

	private final IAugmentedTextField textField;
	private int style;

	TextMenuListener(IAugmentedTextField textField, int style) {
		this.textField = textField;
		this.style = style;
	}

	public void menuDetected(MenuDetectEvent e) {
		final Control control = textField.getControl();
		Menu menu = control.getMenu();
		if (menu != null) {
			control.setMenu(null);
			menu.dispose();
		}
		showMenu(control, textField.findSpellIncident(e));
	}

	@SuppressWarnings("unused") void showMenu(final Control control, final ISpellIncident incident) {
		Point selection = textField.getSelection();
		Menu menu = new Menu(control.getShell(), SWT.POP_UP);
		MenuItem item = new MenuItem(menu, SWT.PUSH);
		item.setText(Messages.TextMenuListener_selectall);
		item.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				textField.selectAll();
			}
		});
		new MenuItem(menu, SWT.SEPARATOR);
		if ((style & SWT.READ_ONLY) == 0) {
			item = new MenuItem(menu, SWT.PUSH);
			item.setText(Messages.TextMenuListener_cut);
			item.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					textField.cut();
				}
			});
			item.setEnabled(selection.y > 0);
		}
		item = new MenuItem(menu, SWT.PUSH);
		item.setText(Messages.TextMenuListener_copy);
		item.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				textField.copy();
			}
		});
		item.setEnabled(selection.y > 0);
		if ((style & SWT.READ_ONLY) == 0) {
			item = new MenuItem(menu, SWT.PUSH);
			item.setText(Messages.TextMenuListener_paste);
			item.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					textField.paste();
				}
			});
			Object contents = UiActivator.getDefault().getClipboard(control.getDisplay())
					.getContents(TextTransfer.getInstance());
			item.setEnabled(contents instanceof String && !((String) contents).isEmpty());
			new MenuItem(menu, SWT.SEPARATOR);
			item = new MenuItem(menu, SWT.PUSH);
			item.setText(Messages.TextMenuListener_undo);
			item.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					textField.undo();
				}
			});
			item.setEnabled(textField.canUndo());
			item = new MenuItem(menu, SWT.PUSH);
			item.setText(Messages.TextMenuListener_redo);
			item.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					textField.redo();
				}
			});
			item.setEnabled(textField.canRedo());
		}
		if (incident != null) {
			new MenuItem(menu, SWT.SEPARATOR);
			String[] suggestions = incident.getSuggestions();
			if (suggestions != null && suggestions.length > 0) {
				for (String proposal : suggestions) {
					item = new MenuItem(menu, SWT.PUSH);
					item.setText(proposal);
					item.addListener(SWT.Selection, new Listener() {
						@Override
						public void handleEvent(Event e) {
							textField.applyCorrection(incident, proposal);
						}
					});
				}
				new MenuItem(menu, SWT.SEPARATOR);
			}
			item = new MenuItem(menu, SWT.PUSH);
			item.setText(Messages.TextMenuListener_add_to_dict);
			item.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					incident.addWord();
					textField.checkSpelling(true);
				}
			});
		}
		control.setMenu(menu);
	}

}