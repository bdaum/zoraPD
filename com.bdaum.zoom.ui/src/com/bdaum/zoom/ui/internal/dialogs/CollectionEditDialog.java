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
 * (c) 2009-2017 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.ui.internal.dialogs;

import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.widgets.CheckedText;
import com.bdaum.zoom.ui.internal.widgets.CollectionEditGroup;
import com.bdaum.zoom.ui.internal.widgets.ISizeHandler;

public class CollectionEditDialog extends ZTitleAreaDialog implements ISizeHandler {

	private SmartCollectionImpl result;

	private SmartCollection current;

	private Composite comp;

	private Control nameField;

	private String title;

	private Label errorLabel;

	private boolean adhoc;

	private final boolean album;

	private FindWithinGroup findWithinGroup;

	private Control descriptionField;

	private FindInNetworkGroup findInNetworkGroup;

	private final boolean networkPossible;

	private boolean isSystem;

	private CollectionEditGroup collectionEditGroup;

	private Button colorCodeButton;

	private int colorCode = -1;

	private boolean person;

	private Image face;

	private String message;

	public CollectionEditDialog(Shell parentShell, SmartCollection current, String title, String message, boolean adhoc,
			boolean album, boolean person, boolean networkPossible) {
		super(parentShell, HelpContextIds.COLLECTION_EDIT_DIALOG);
		this.current = current;
		this.message = message;
		this.person = person;
		this.isSystem = current != null && current.getSystem();
		this.title = title;
		this.adhoc = adhoc;
		this.album = album;
		this.networkPossible = networkPossible;
		readonly &= !adhoc;
	}

	@Override
	public void create() {
		super.create();
		setTitle(title);
		if (current != null && current.getAlbum() && current.getSystem())
			setTitleImage(face = UiUtilities.getFace(getShell().getDisplay(), current, 64, 0, null));
		else if (current == null && album && person)
			setTitleImage(Icons.person64.getImage());
		else
			setTitleImage(Icons.folder64.getImage());
		setMessage(message);
		updateButtons();
		getShell().layout();
		getShell().pack();
		collectionEditGroup.prepare();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		comp = new Composite(area, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		int numColumns = 2;
		if (!adhoc) {
			numColumns += 2;
			if (current == null || !isSystem || current.getAlbum())
				numColumns += 2;
		}
		if (networkPossible)
			++numColumns;
		comp.setLayout(new GridLayout(numColumns, false));
		createHeaderGroup();
		Composite critComp = new Composite(comp, SWT.NONE);
		critComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, numColumns, 1));
		critComp.setLayout(new GridLayout(1, false));

		collectionEditGroup = new CollectionEditGroup(critComp, current, album, readonly,
				findInNetworkGroup != null && findInNetworkGroup.getSelection(), this);
		collectionEditGroup.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateButtons();
			}
		});
		Shell shell = getShell();
		Point size = shell.getSize();
		shell.setSize(size.x, size.y + 12);
		shell.layout();
		return area;
	}

	private void createHeaderGroup() {
		if (!adhoc) {
			if (isSystem) {
				nameField = new Label(comp, SWT.NONE);
				nameField.setFont(JFaceResources.getHeaderFont());
				// nameField.setBounds(x, 13, 150, 15);
				GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
				data.widthHint = 150;
				nameField.setLayoutData(data);
				((Label) nameField).setText(current.getName());
				nameField.pack();
			} else {
				Label nameLabel = new Label(comp, SWT.NONE);
				nameLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
				nameLabel.setText(Messages.CollectionEditDialog_name);
				nameField = new Text(comp, SWT.BORDER);
				GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
				data.widthHint = 150;
				nameField.setLayoutData(data);
				if (current != null)
					((Text) nameField).setText(current.getName());
				((Text) nameField).addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						updateButtons();
					}
				});
				nameField.setEnabled(!readonly);
			}
			if (current == null || !isSystem || current.getAlbum()) {
				if (isSystem) {
					if (album) {
						descriptionField = new StyledText(comp, SWT.READ_ONLY | SWT.MULTI | SWT.WRAP);
						((StyledText) descriptionField).setText(current.getDescription());
						descriptionField.addMouseListener(new MouseAdapter() {
							@Override
							public void mouseDoubleClick(MouseEvent e) {
								detectAndHandleHyperlink();
							}
						});
					} else {
						descriptionField = new Label(comp, SWT.WRAP);
						((Label) descriptionField).setText(current.getDescription());
						descriptionField.pack();
					}
					GridData data = new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1);
					data.widthHint = 240;
					descriptionField.setLayoutData(data);
				} else {
					Label descriptionLabel = new Label(comp, SWT.NONE);
					descriptionLabel.setText(Messages.CollectionEditDialog_description);
					descriptionLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
					if (album) {
						descriptionLabel.pack();
						Rectangle bounds = descriptionLabel.getBounds();
						descriptionField = new CheckedText(comp, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
						GridData data = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
						data.widthHint = 240;
						data.heightHint = bounds.height * 3;
						data.verticalIndent = bounds.height;
						descriptionField.setLayoutData(data);
					} else {
						descriptionField = new CheckedText(comp, SWT.BORDER);
						GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
						data.widthHint = 240;
						descriptionField.setLayoutData(data);
					}
					if (current != null)
						((CheckedText) descriptionField).setText(current.getDescription());
					descriptionField.setEnabled(!readonly);
				}
			}
			Label colorCodeLabel = new Label(comp, SWT.NONE);
			colorCodeLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			colorCodeLabel.setText(Messages.CollectionEditDialog_color_code);
			colorCodeLabel.setAlignment(SWT.RIGHT);
			colorCodeButton = new Button(comp, SWT.PUSH);
			colorCodeButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			if (current != null)
				colorCode = current.getColorCode() - 1;
			colorCodeButton.setToolTipText(Messages.CollectionEditDialog_color_code);
			colorCodeButton.setImage(Icons.toSwtColors(colorCode));
			colorCodeButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					ColorCodeDialog dialog = new ColorCodeDialog(getShell(), colorCode);
					dialog.create();
					dialog.getShell().setLocation(colorCodeButton.toDisplay(0, 0));
					int code = dialog.open();
					if (code >= Constants.COLOR_UNDEFINED)
						colorCodeButton.setImage(Icons.toSwtColors(colorCode = code));
				}
			});
		} else {
			Label label = new Label(comp, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			Composite findComp = new Composite(comp, SWT.NONE);
			findComp.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
			findComp.setLayout(new GridLayout(1, false));
			findWithinGroup = new FindWithinGroup(findComp);
		}
		if (networkPossible) {
			Composite netComp = new Composite(comp, SWT.NONE);
			netComp.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
			netComp.setLayout(new GridLayout(1, false));
			findInNetworkGroup = new FindInNetworkGroup(netComp);
			if (current != null)
				findInNetworkGroup.setSelection(current.getNetwork());
			findInNetworkGroup.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					collectionEditGroup.setNetworked(findInNetworkGroup.getSelection());
				}
			});
		}
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		composite.setFont(parent.getFont());
		errorLabel = new Label(composite, SWT.NONE);
		errorLabel.setData("id", "errors"); //$NON-NLS-1$ //$NON-NLS-2$
		errorLabel.setForeground(errorLabel.getDisplay().getSystemColor(SWT.COLOR_RED));
		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		layoutData.horizontalIndent = 10;
		errorLabel.setLayoutData(layoutData);
		return super.createButtonBar(composite);
	}

	@Override
	public void setErrorMessage(String msg) {
		if (errorLabel != null)
			errorLabel.setText(msg == null ? "" : msg); //$NON-NLS-1$
	}

	@Override
	public boolean close() {
		if (face != null)
			face.dispose();
		collectionEditGroup.dispose();
		return super.close();
	}

	public SmartCollectionImpl getResult() {
		return result;
	}

	public void updateButtons() {
		Button button = getButton(IDialogConstants.OK_ID);
		if (button != null) {
			boolean valid = validate();
			getShell().setModified(valid);
			button.setEnabled(valid);
		}
	}

	private boolean validate() {
		if (readonly)
			return false;
		String errorMessage = null;
		if (nameField instanceof Text) {
			String text = ((Text) nameField).getText();
			if (text.isEmpty())
				errorMessage = Messages.CollectionEditDialog_specify_name;
			else if (text.indexOf(':') >= 0
					&& (current == null || !current.getStringId().startsWith(IDbManager.IMPORTKEY)))
				errorMessage = Messages.CollectionEditDialog_illegal_colon;
			else if (person) {
				List<SmartCollectionImpl> sm = dbManager.obtainObjects(SmartCollectionImpl.class, false, "album", true, //$NON-NLS-1$
						QueryField.EQUALS, "system", //$NON-NLS-1$
						true, QueryField.EQUALS, "name", text, QueryField.EQUALS); //$NON-NLS-1$
				if (!sm.isEmpty())
					errorMessage = Messages.CollectionEditDialog_person_already_exists;
			}
		}
		if (errorMessage == null)
			errorMessage = collectionEditGroup.validate();
		if (errorMessage == null || !errorMessage.isEmpty())
			setErrorMessage(errorMessage);
		else
			setErrorMessage(null);
		return errorMessage == null;
	}

	@Override
	protected void okPressed() {
		// Must create a new instance
		String name = (nameField != null)
				? (nameField instanceof Text) ? ((Text) nameField).getText() : ((Label) nameField).getText()
				: Messages.CollectionEditDialog_adhoc_query;
		boolean networked = findInNetworkGroup == null ? false : findInNetworkGroup.getSelection();
		result = new SmartCollectionImpl(name, isSystem || person, album, adhoc, networked,
				descriptionField != null
						? (descriptionField instanceof CheckedText) ? ((CheckedText) descriptionField).getText()
								: ((Label) descriptionField).getText()
						: null,
				colorCode + 1, current != null ? current.getLastAccessDate() : null,
				current != null ? current.getGeneration() + 1 : 0, current != null ? current.getPerspective() : null,
				null);
		if (isSystem || UiUtilities.isImport(current))
			result.setStringId(current.getStringId());
		collectionEditGroup.applyCriteria(result, name);
		if (findWithinGroup != null)
			result.setSmartCollection_subSelection_parent(findWithinGroup.getParentCollection());
		super.okPressed();
	}

	private void detectAndHandleHyperlink() {
		String text = ((StyledText) descriptionField).getText();
		int p = text.indexOf('\n');
		if (p > 0) {
			int q = text.indexOf(": ", p); //$NON-NLS-1$
			if (q >= 0) {
				Point selection = ((StyledText) descriptionField).getSelection();
				int offset = q + 2;
				StringTokenizer st = new StringTokenizer(text.substring(offset), ";", true); //$NON-NLS-1$
				while (st.hasMoreTokens()) {
					String token = st.nextToken();
					if (!";".equals(token)) { //$NON-NLS-1$
						if (offset <= selection.x && offset + token.length() > selection.y) {
							BusyIndicator.showWhile(getShell().getDisplay(),
									() -> UiActivator.getDefault().sendMail(Collections.singletonList(token.trim())));
							break;
						}
					}
					offset += token.length();
				}
			}
		}
	}

	public void sizeChanged() {
		Shell shell = comp.getShell();
		shell.pack();
		shell.layout();
	}

}
