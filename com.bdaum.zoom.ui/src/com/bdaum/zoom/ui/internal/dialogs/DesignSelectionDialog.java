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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.cat.model.group.webGallery.WebGalleryImpl;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.image.ImageUtilities;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;

public class DesignSelectionDialog extends ZTitleAreaDialog implements
		PaintListener {

	private TableViewer viewer;
	private Canvas canvas;
	private Button clearButton;
	private WebGalleryImpl selected;
	private Object[] elements;

	public DesignSelectionDialog(Shell parentShell) {
		super(parentShell);
		elements = dbManager.obtainObjects(WebGalleryImpl.class,
				"template", Boolean.TRUE, //$NON-NLS-1$
				QueryField.EQUALS).toArray();
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.DesignSelectionDialog_web_gallery_design_selection);
		if (elements.length == 0)
			setMessage(Messages.DesignSelectionDialog_no_designs_available);
		else
			setMessage(Messages.DesignSelectionDialog_please_select_a_design);
		updatedButtons();
	}

	private void updatedButtons() {
		boolean empty = viewer.getSelection().isEmpty();
		getButton(IDialogConstants.OK_ID).setEnabled(!empty);
		clearButton.setEnabled(!empty);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite comp = new Composite(area, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout(2, false));
		Composite viewComp = new Composite(comp, SWT.NONE);
		viewComp.setLayout(new GridLayout());
		viewComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer = new TableViewer(viewComp, SWT.V_SCROLL | SWT.SINGLE
				| SWT.BORDER);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.widthHint = 200;
		viewer.getControl().setLayoutData(data);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof WebGalleryImpl)
					return ((WebGalleryImpl) element).getName();
				return element.toString();
			}
		});
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updatedButtons();
				canvas.redraw();
			}
		});
		viewer.setInput(elements);
		clearButton = new Button(viewComp, SWT.PUSH);
		clearButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER,
				false, false));
		clearButton.setText(Messages.DesignSelectionDialog_delete);
		clearButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) viewer
						.getSelection();
				Object first = selection.getFirstElement();
				if (first instanceof WebGalleryImpl) {
					String name = ((WebGalleryImpl) first).getName();
					if (AcousticMessageDialog.openConfirm(
							getShell(),
							Messages.DesignSelectionDialog_delete_design,
							NLS.bind(
									Messages.DesignSelectionDialog_do_you_really_want_to_delete,
									name))) {
						viewer.remove(first);
						dbManager.safeTransaction(first, null);
					}
				}
			}
		});
		Composite previewComp = new Composite(comp, SWT.NONE);
		previewComp.setLayout(new GridLayout());
		canvas = new Canvas(previewComp, SWT.DOUBLE_BUFFERED | SWT.BORDER);
		canvas.setLayoutData(new GridData(300, 300));
		canvas.addPaintListener(this);
		canvas.redraw();
		return area;
	}

	@Override
	protected void okPressed() {
		IStructuredSelection selection = (IStructuredSelection) viewer
				.getSelection();
		Object first = selection.getFirstElement();
		if (first instanceof WebGalleryImpl)
			selected = (WebGalleryImpl) first;
		super.okPressed();
	}

	public void paintControl(PaintEvent e) {
		Rectangle clientArea = canvas.getClientArea();
		GC gc = e.gc;
		gc.setBackground(canvas.getParent().getBackground());
		gc.fillRectangle(clientArea);
		IStructuredSelection selection = (IStructuredSelection) viewer
				.getSelection();
		Object first = selection.getFirstElement();
		if (first instanceof WebGalleryImpl) {
			Image image = ImageUtilities.loadThumbnail(e.display,
					((WebGalleryImpl) first).getPreview(),
					ImageConstants.NOCMS, SWT.IMAGE_JPEG, false);
			if (image != null) {
				Rectangle ibounds = image.getBounds();
				gc.drawImage(image, 0, 0, ibounds.width, ibounds.height,
						(clientArea.width - ibounds.width) / 2,
						(clientArea.height - ibounds.height) / 2,
						clientArea.width, clientArea.height);
			}
		} else {
			gc.setForeground(canvas.getParent().getForeground());
			String msg = Messages.DesignSelectionDialog_no_design_selected;
			Point tx = gc.textExtent(msg);
			gc.drawText(msg, (clientArea.width - tx.x) / 2,
					(clientArea.height - tx.y) / 2);
		}
	}

	public WebGalleryImpl getResult() {
		return selected;
	}

}
