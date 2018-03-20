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
 * (c) 2011 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.dialogs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.ICore;
import com.bdaum.zoom.image.ImageUtilities;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.widgets.ZDialog;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;

public abstract class AbstractSelectExhibitDialog extends ZDialog {

	protected static final int ICONWIDTH = 48;
	protected TableViewer viewer;
	protected boolean advanced;
	protected Object selection;
	protected final List<? extends IdentifiableObject> exhibits;
	protected boolean focusGained;

	public AbstractSelectExhibitDialog(Shell parentShell, List<? extends IdentifiableObject> exhibits) {
		super(parentShell);
		this.exhibits = exhibits;
		setShellStyle(SWT.NO_TRIM);
		advanced = Platform.getPreferencesService().getBoolean(UiActivator.PLUGIN_ID,
				PreferenceConstants.ADVANCEDGRAPHICS, false, null);
	}

	@Override
	public void create() {
		super.create();
		getShell().layout();
		getShell().pack();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		GridLayout layout = (GridLayout) area.getLayout();
		layout.marginWidth = 1;
		layout.marginHeight = 1;
		viewer = new TableViewer(area, SWT.V_SCROLL | SWT.SINGLE | SWT.BORDER | SWT.VIRTUAL);
		viewer.getControl().setLayoutData(new GridData(200, 350));
		viewer.setContentProvider(new ILazyContentProvider() {
			public void inputChanged(Viewer aViewer, Object oldInput, Object newInput) {
				// do nothing
			}

			public void dispose() {
				// do nothing
			}

			public void updateElement(int index) {
				viewer.replace(exhibits.get(index), index);
			}
		});
		viewer.setLabelProvider(new ColumnLabelProvider() {
			private Map<String, Image> thMap = new HashMap<String, Image>();

			@Override
			public String getText(Object element) {
				return AbstractSelectExhibitDialog.this.getText(element);
			}

			@Override
			public Font getFont(Object element) {
				return AbstractSelectExhibitDialog.this.getFont(element);
			}

			@Override
			public Image getImage(Object element) {
				String assetID = AbstractSelectExhibitDialog.this.getAssetId(element);
				if (assetID == null)
					return null;
				Image image = thMap.get(assetID);
				if (image == null) {
					ICore core = Core.getCore();
					AssetImpl asset = core.getDbManager().obtainAsset(assetID);
					if (asset != null) {
						image = core.getImageCache().getImage(asset);
						if (image != null) {
							Rectangle bounds = image.getBounds();
							double scale = ImageUtilities.computeScale(bounds.width, bounds.height, ICONWIDTH,
									ICONWIDTH);
							int newWidth = (int) (bounds.width * scale + 0.5d);
							int newHeight = (int) (bounds.height * scale + 0.5d);
							Image thumbnail = new Image(image.getDevice(), ICONWIDTH, ICONWIDTH);
							GC gc = new GC(thumbnail);
							try {
								if (advanced) {
									gc.setAntialias(SWT.ON);
									gc.setInterpolation(SWT.HIGH);
								}
								gc.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_GRAY));
								gc.fillRectangle(0, 0, ICONWIDTH, ICONWIDTH);
								gc.drawImage(image, 0, 0, bounds.width, bounds.height, (ICONWIDTH - newWidth) / 2,
										(ICONWIDTH - newHeight) / 2, newWidth, newHeight);
								image = thumbnail;
							} finally {
								gc.dispose();
							}
							thMap.put(assetID, image);
						}
					}
				}
				return image;
			}

			@Override
			public void dispose() {
				for (Image image : thMap.values())
					image.dispose();
				thMap.clear();
				super.dispose();
			}
		});
		viewer.setInput(exhibits);
		viewer.setItemCount(exhibits.size());
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				processSelection();
			}
		});
		viewer.getControl().addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.character == '\r')
					processSelection();
			}
		});
		viewer.getControl().addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				if (focusGained)
					cancelPressed();
			}

			@Override
			public void focusGained(FocusEvent e) {
				focusGained = true;
			}
		});
		viewer.getTable().pack();
		return area;
	}

	protected abstract String getAssetId(Object element);

	protected Font getFont(Object element) {
		return null;
	}

	protected String getText(Object element) {
		return element.toString();
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		return null;
	}

	public void setSelection(Object obj) {
		viewer.setSelection(new StructuredSelection(obj), true);
	}

	protected void processSelection() {
		IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
		if (sel.isEmpty())
			cancelPressed();
		else {
			selection = sel.getFirstElement();
			okPressed();
		}
	}

}