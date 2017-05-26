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

package com.bdaum.zoom.ui.internal.preferences;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.widgets.ScaleFieldEditor;
import com.bdaum.zoom.ui.preferences.AbstractFieldEditorPreferencePage;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;

public class MousePreferencePage extends AbstractFieldEditorPreferencePage {

	private static String[][] zoomOptions = new String[][] {
			new String[] {
					Messages.getString("MousePreferencePage.alt"), String.valueOf(PreferenceConstants.ZOOMALT) }, //$NON-NLS-1$
			new String[] { Messages.getString("MousePreferencePage.shift"), //$NON-NLS-1$
					String.valueOf(PreferenceConstants.ZOOMSHIFT) },
			new String[] {
					Messages.getString("MousePreferencePage.right_mouse_button"), //$NON-NLS-1$
					String.valueOf(PreferenceConstants.ZOOMRIGHT) },
			new String[] { Messages.getString("MousePreferencePage.no_zoom"), //$NON-NLS-1$
					String.valueOf(PreferenceConstants.NOZOOM) } };
	private static String[][] wheelOptions = new String[][] {
			new String[] {
					Messages.getString("MousePreferencePage.shiftscrolls"), //$NON-NLS-1$
					String.valueOf(PreferenceConstants.WHEELSHIFTPANS) },
			new String[] {
					Messages.getString("MousePreferencePage.altscrolls"), //$NON-NLS-1$
					String.valueOf(PreferenceConstants.WHEELALTPANS) },
			new String[] {
					Messages.getString("MousePreferencePage.shiftzooms"), //$NON-NLS-1$
					String.valueOf(PreferenceConstants.WHEELSHIFTZOOMS) },
			new String[] { Messages.getString("MousePreferencePage.altzooms"), //$NON-NLS-1$
					String.valueOf(PreferenceConstants.WHEELALTZOOMS) },
			new String[] { Messages.getString("MousePreferencePage.zoom_only"), //$NON-NLS-1$
					String.valueOf(PreferenceConstants.WHEELZOOMONLY) },
			new String[] {
					Messages.getString("MousePreferencePage.scroll_only"), //$NON-NLS-1$
					String.valueOf(PreferenceConstants.WHEELSCROLLONLY) } };
	private ComboFieldEditor zoomkeyEditor;
	private ComboFieldEditor wheelkeyEditor;

	public MousePreferencePage() {
		super(GRID);
	}

	@Override
	protected void createFieldEditors() {
		setHelp(HelpContextIds.MOUSE_PREFERENCE_PAGE);
		addField(new ExplanationFieldEditor("", Messages.getString("MousePreferencePage.mouse_descr"),getFieldEditorParent())); //$NON-NLS-1$ //$NON-NLS-2$
		addField(new FieldComment(getFieldEditorParent(), "", 0)); //$NON-NLS-1$
		ScaleFieldEditor editor = new ScaleFieldEditor(
				PreferenceConstants.MOUSE_SPEED,
				Messages.getString("MousePreferencePage.mouse_speed"), 20, getFieldEditorParent()); //$NON-NLS-1$
		editor.setValidRange(0, 20);
		addField(editor);
		zoomkeyEditor = new ComboFieldEditor(PreferenceConstants.ZOOMKEY,
				Messages.getString("MousePreferencePage.zoom_key"), //$NON-NLS-1$
				zoomOptions, getFieldEditorParent());
		addField(zoomkeyEditor);
		addField(new FieldComment(getFieldEditorParent(), "", 0)); //$NON-NLS-1$
		addField(new FieldSeparator(getFieldEditorParent()));
		addField(new FieldComment(getFieldEditorParent(),
				Messages.getString("MousePreferencePage.mouse_wheel_descr"), 0)); //$NON-NLS-1$
		addField(new FieldComment(getFieldEditorParent(), "", 0)); //$NON-NLS-1$
		wheelkeyEditor = new ComboFieldEditor(PreferenceConstants.WHEELKEY,
				Messages.getString("MousePreferencePage.mouse_wheel_behavior"), //$NON-NLS-1$
				wheelOptions, getFieldEditorParent());
		addField(wheelkeyEditor);
		editor = new ScaleFieldEditor(
				PreferenceConstants.WHEELSOFTNESS,
				Messages.getString("MousePreferencePage.soft_acceleration"), 20, getFieldEditorParent()); //$NON-NLS-1$
		editor.setValidRange(0, 100);
		addField(editor);
	}

	@Override
	protected void contributeButtons(Composite parent) {
		super.contributeButtons(parent);
		if (!getShell().getDisplay().getTouchEnabled()) {
			Button touchButton = new Button(parent, SWT.PUSH);
			((GridLayout) parent.getLayout()).numColumns++;
			touchButton.setText(Messages
					.getString("MousePreferencePage.touch_settings")); //$NON-NLS-1$
			touchButton.setToolTipText(Messages
					.getString("MousePreferencePage.touch_settings_tooltip")); //$NON-NLS-1$
			touchButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					IPreferenceStore store = getPreferenceStore();
					store.setValue(PreferenceConstants.WHEELKEY,
							PreferenceConstants.WHEELZOOMONLY);
					store.setValue(PreferenceConstants.ZOOMKEY,
							PreferenceConstants.NOZOOM);
					wheelkeyEditor.load();
					zoomkeyEditor.load();
				}
			});
		}
	}

}
