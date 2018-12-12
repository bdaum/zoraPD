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
 * (c) 2009-2017 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.dialogs;

import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.css.CSSProperties;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.widgets.CheckedText;
import com.bdaum.zoom.ui.internal.widgets.CollectionEditGroup;
import com.bdaum.zoom.ui.internal.widgets.ISizeHandler;
import com.bdaum.zoom.ui.internal.widgets.LabelConfigGroup;

public class CollectionEditDialog extends ZTitleAreaDialog implements ISizeHandler {

	private static final String SETTINGSID = "collectionEditDialog"; //$NON-NLS-1$

	private static final String ACTIVETAB = "activeTab"; //$NON-NLS-1$

	private SmartCollectionImpl result;

	private SmartCollection current;

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

	private CTabFolder tabFolder;

	private LabelConfigGroup labelConfigGroup;

	private IDialogSettings settings;

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
		settings = getDialogSettings(UiActivator.getDefault(), SETTINGSID);
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
		if (nameField instanceof Text && nameField.isEnabled())
			nameField.setFocus();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		tabFolder = new CTabFolder(area, SWT.BORDER);
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
		tabFolder.setSimple(false);
		UiUtilities.createTabItem(tabFolder, Messages.CollectionEditDialog_general, null)
				.setControl(createGeneralGroup(tabFolder));
		UiUtilities.createTabItem(tabFolder, Messages.CollectionEditDialog_query, null)
				.setControl(createQueryGroup(tabFolder));
		UiUtilities.createTabItem(tabFolder, Messages.CollectionEditDialog_appearance, null)
				.setControl(createApperanceGroup(tabFolder));
		try {
			tabFolder.setSelection(settings.getInt(ACTIVETAB));
		} catch (NumberFormatException e) {
			// do nothing
		}
		Shell shell = getShell();
		Point size = shell.getSize();
		shell.setSize(size.x, size.y + 12);
		shell.layout();
		return area;
	}

	private Control createGeneralGroup(Composite parent) {
		Composite generalComp = new Composite(parent, SWT.NONE);
		generalComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		if (!adhoc) {
			generalComp.setLayout(new GridLayout(2, false));
			if (isSystem) {
				nameField = new Label(generalComp, SWT.NONE);
				nameField.setFont(JFaceResources.getHeaderFont());
				GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
				data.widthHint = 150;
				nameField.setLayoutData(data);
				((Label) nameField).setText(current.getName());
			} else {
				new Label(generalComp, SWT.NONE).setText(Messages.CollectionEditDialog_name);
				nameField = new Text(generalComp, SWT.BORDER);
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
			if (current == null || !isSystem || current.getAlbum()
					|| current.getStringId().equals(Constants.LAST_IMPORT_ID)
					|| current.getStringId().startsWith(IDbManager.IMPORTKEY)) {
				boolean showDescription = current == null
						|| current.getDescription() != null && !current.getDescription().isEmpty();
				if (isSystem) {
					if (showDescription) {
						if (album) {
							descriptionField = new StyledText(generalComp, SWT.READ_ONLY | SWT.MULTI | SWT.WRAP);
							((StyledText) descriptionField).setText(current.getDescription());
							descriptionField.addMouseListener(new MouseAdapter() {
								@Override
								public void mouseDoubleClick(MouseEvent e) {
									detectAndHandleHyperlink();
								}
							});
						} else
							((Label) (descriptionField = new Label(generalComp, SWT.WRAP)))
									.setText(current.getDescription());
					}
				} else {
					if (!readonly || showDescription) {
						new Label(generalComp, SWT.NONE).setText(Messages.CollectionEditDialog_description);
						descriptionField = new CheckedText(generalComp,
								SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
						if (current != null)
							((CheckedText) descriptionField).setText(current.getDescription());
						descriptionField.setEnabled(!readonly);
					}
				}
				if (descriptionField != null) {
					GridData data = new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1);
					data.widthHint = 240;
					data.heightHint = 50;
					descriptionField.setLayoutData(data);
				}
			}
			new Label(generalComp, SWT.NONE).setText(Messages.CollectionEditDialog_color_code);
			colorCodeButton = new Button(generalComp, SWT.PUSH);
			if (current != null)
				colorCode = current.getColorCode() - 1;
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
			generalComp.setLayout(new GridLayout());
			new Label(generalComp, SWT.NONE).setText(Messages.CollectionEditDialog_adhoc_hint);
			Composite findComp = new Composite(generalComp, SWT.NONE);
			findComp.setLayout(new GridLayout());
			findWithinGroup = new FindWithinGroup(findComp);
		}
		if (networkPossible) {
			Composite netComp = new Composite(generalComp, SWT.NONE);
			netComp.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
			netComp.setLayout(new GridLayout());
			findInNetworkGroup = new FindInNetworkGroup(netComp);
			if (current != null)
				findInNetworkGroup.setSelection(current.getNetwork());
			findInNetworkGroup.addListener(new Listener() {
				@Override
				public void handleEvent(Event event) {
					collectionEditGroup.setNetworked(findInNetworkGroup.getSelection());
				}
			});
		}
		return generalComp;
	}

	private Control createApperanceGroup(Composite parent) {
		Composite apperanceComp = new Composite(parent, SWT.NONE);
		apperanceComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		apperanceComp.setLayout(new GridLayout(1, false));
		labelConfigGroup = new LabelConfigGroup(apperanceComp, true);
		labelConfigGroup.addListener(new Listener() {
			@Override
			public void handleEvent(Event event) {
				validate();
			}
		});
		if (current != null)
			labelConfigGroup.setSelection(current.getShowLabel(), current.getLabelTemplate(), current.getFontSize());
		return apperanceComp;
	}

	private Control createQueryGroup(Composite parent) {
		Composite queryComp = new Composite(parent, SWT.NONE);
		queryComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		queryComp.setLayout(new GridLayout());
		Composite critComp = new Composite(queryComp, SWT.NONE);
		critComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		critComp.setLayout(new GridLayout(1, false));

		collectionEditGroup = new CollectionEditGroup(critComp, current, album, readonly,
				findInNetworkGroup != null && findInNetworkGroup.getSelection(), this);
		collectionEditGroup.addListener(new Listener() {
			public void handleEvent(Event e) {
				updateButtons();
			}
		});
		return queryComp;
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
		errorLabel.setData(CSSProperties.ID, CSSProperties.ERRORS);
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
		if (errorMessage == null)
			errorMessage = labelConfigGroup.validate();
		if (errorMessage == null || !errorMessage.isEmpty())
			setErrorMessage(errorMessage);
		else
			setErrorMessage(null);
		return errorMessage == null;
	}

	@Override
	protected void okPressed() {
		settings.put(ACTIVETAB, tabFolder.getSelectionIndex());
		// Must create a new instance
		String name = (nameField != null)
				? (nameField instanceof Text) ? ((Text) nameField).getText() : ((Label) nameField).getText()
				: Messages.CollectionEditDialog_adhoc_query;
		result = new SmartCollectionImpl(name, isSystem || person, album, adhoc,
				findInNetworkGroup == null ? false : findInNetworkGroup.getSelection(),
				descriptionField == null ? null
						: (descriptionField instanceof CheckedText) ? ((CheckedText) descriptionField).getText()
								: (descriptionField instanceof StyledText) ? ((StyledText) descriptionField).getText()
										: ((Label) descriptionField).getText(),
				colorCode + 1, current != null ? current.getLastAccessDate() : null,
				current != null ? current.getGeneration() + 1 : 0, current != null ? current.getPerspective() : null,
				labelConfigGroup.getSelection(), labelConfigGroup.getTemplate(), labelConfigGroup.getFontSize(), null);
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
					if (!";".equals(token) && offset <= selection.x && offset + token.length() > selection.y) { //$NON-NLS-1$
						BusyIndicator.showWhile(getShell().getDisplay(),
								() -> UiActivator.getDefault().sendMail(Collections.singletonList(token.trim())));
						break;
					}
					offset += token.length();
				}
			}
		}
	}

	public void sizeChanged() {
		Shell shell = getShell();
		shell.pack();
		shell.layout();
	}

}
