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
 * (c) 2009 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.dialogs;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.bdaum.zoom.core.Constants;

public class MetadataOptionGroup {

	private static final String METADATAOPTIONS = "metaDataOptions"; //$NON-NLS-1$
	private Button overWriteButtun;
	private Button fillinButton;
	private Button mergeButton;

	public MetadataOptionGroup(Composite composite, boolean withMerge) {
		final Composite buttonBar = new Composite(composite, SWT.NONE);
		buttonBar.setLayout(new GridLayout(withMerge ? 6 : 4, false));
		overWriteButtun = new Button(buttonBar, SWT.RADIO);
		new Label(buttonBar, SWT.NONE).setText(Messages.PasteMetaDialog_overwrite);
		fillinButton = new Button(buttonBar, SWT.RADIO);
		new Label(buttonBar, SWT.NONE).setText(Messages.PasteMetaDialog_fill_in_blanks);
		if (withMerge) {
			mergeButton = new Button(buttonBar, SWT.RADIO);
			new Label(buttonBar, SWT.NONE).setText(Messages.MetadataOptionGroup_combine);
		}
	}

	public void saveSettings(IDialogSettings settings) {
		settings.put(METADATAOPTIONS, getMode());
	}

	public int getMode() {
		if (fillinButton.getSelection())
			return Constants.SKIP;
		if (mergeButton != null && mergeButton.getSelection())
			return Constants.MERGE;
		return Constants.REPLACE;
	}

	public void fillValues(IDialogSettings settings) {
		if (settings != null)
			try {
				int mode = settings.getInt(METADATAOPTIONS);
				switch (mode) {
				case Constants.REPLACE:
					overWriteButtun.setSelection(true);
					break;
				case Constants.MERGE:
					if (mergeButton != null)
						mergeButton.setSelection(true);
					break;
				case Constants.SKIP:
					fillinButton.setSelection(true);
					break;
				}
			} catch (NumberFormatException e) {
				if (mergeButton != null)
					mergeButton.setSelection(true);
				else
					fillinButton.setSelection(true);
			}
	}

}
