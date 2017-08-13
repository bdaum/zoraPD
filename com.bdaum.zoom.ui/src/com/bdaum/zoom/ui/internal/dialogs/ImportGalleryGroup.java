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

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitionImpl;
import com.bdaum.zoom.cat.model.group.slideShow.SlideShowImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebGalleryImpl;

public class ImportGalleryGroup {

	private Text fromField;
	private IdentifiableObject selectedItem;
	private ListenerList<ISelectionChangedListener> listeners = new ListenerList<ISelectionChangedListener>();
	private Button clearButton;

	public ImportGalleryGroup(final Composite parent, GridData gridData,
			final String targetType) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayoutData(gridData);
		GridLayout layout = new GridLayout(4, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		comp.setLayout(layout);
		Label label = new Label(comp, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
				false));
		label.setText(Messages.ImportGalleryGroup_import_from);
		fromField = new Text(comp, SWT.SINGLE | SWT.LEAD | SWT.BORDER
				| SWT.READ_ONLY);
		fromField.setLayoutData(new GridData(200, -1));
		clearButton = new Button(comp, SWT.PUSH);
		clearButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER,
				false, false));
		clearButton.setText(Messages.ImportGalleryGroup_clear);
		clearButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				fromField.setText(""); //$NON-NLS-1$
				selectedItem = null;
				updateButtons();
			}
		});
		Button browseButton = new Button(comp, SWT.PUSH);
		browseButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER,
				false, false));
		browseButton.setText(Messages.ImportGalleryGroup_browse);
		browseButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				PresentationSelectDialog dialog = new PresentationSelectDialog(
						parent.getShell(), targetType);
				if (dialog.open() == Window.OK) {
					selectedItem = dialog.getResult();
					fromField.setText(getName());
					updateButtons();
					fireSelectionChanged();
				}
			}
		});
		updateButtons();
	}

	protected void updateButtons() {
		clearButton.setEnabled(!fromField.getText().isEmpty());
	}

	protected void fireSelectionChanged() {
		for (Object l : listeners.getListeners()) {
			((ISelectionChangedListener) l).selectionChanged(null);
		}
	}

	protected String getName() {
		if (selectedItem instanceof SlideShowImpl)
			return NLS.bind(Messages.ImportGalleryGroup_slideshow_n,
					((SlideShowImpl) selectedItem).getName());
		if (selectedItem instanceof ExhibitionImpl)
			return NLS.bind(Messages.ImportGalleryGroup_exhibition_n,
					((ExhibitionImpl) selectedItem).getName());
		if (selectedItem instanceof WebGalleryImpl)
			return NLS.bind(Messages.ImportGalleryGroup_web_gallery_n,
					((WebGalleryImpl) selectedItem).getName());
		if (selectedItem instanceof SmartCollectionImpl) {
			SmartCollectionImpl sm = (SmartCollectionImpl) selectedItem;
			return NLS.bind(
					sm.getAlbum() ? sm.getSystem() ? Messages.ImportGalleryGroup_persons
							: Messages.ImportGalleryGroup_album : Messages.ImportGalleryGroup_user_defined, sm
							.getName());
		}
		return ""; //$NON-NLS-1$
	}

	public void addChangeListener(ISelectionChangedListener listener) {
		listeners.add(listener);
	}

	public String getDescription() {
		if (selectedItem instanceof SlideShowImpl)
			return ((SlideShowImpl) selectedItem).getDescription();
		if (selectedItem instanceof ExhibitionImpl)
			return ((ExhibitionImpl) selectedItem).getDescription();
		if (selectedItem instanceof WebGalleryImpl)
			return ((WebGalleryImpl) selectedItem).getDescription();
		if (selectedItem instanceof SmartCollectionImpl)
			return ((SmartCollectionImpl) selectedItem).getDescription();
		return null;
	}

	public IdentifiableObject getFromItem() {
		return selectedItem;
	}

}
