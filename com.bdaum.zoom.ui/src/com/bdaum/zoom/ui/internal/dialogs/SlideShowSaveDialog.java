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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.cat.model.group.Group;
import com.bdaum.zoom.cat.model.group.GroupImpl;
import com.bdaum.zoom.cat.model.group.slideShow.SlideShowImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;

public class SlideShowSaveDialog extends ZTitleAreaDialog implements Listener {

	private Map<String, GroupImpl> groupMap = new HashMap<String, GroupImpl>(50);
	private Combo groupField;
	private Text nameField;
	private Group selectedGroup;
	private String name;
	private CheckboxButton openButton;
	private boolean open;

	public SlideShowSaveDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	public void create() {
		super.create();
		setMessage(Messages.SlideShowSaveDialog_you_may_want_to_save);
		setTitle(Messages.SlideShowSaveDialog_save_slideshow);
		updateButtons();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, false));
		new Label(composite, SWT.NONE).setText(Messages.SlideShowSaveDialog_name);
		nameField = new Text(composite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		layoutData.verticalIndent = 10;
		nameField.setLayoutData(layoutData);
		nameField.setText(Messages.SlideShowSaveDialog_slideshow + Format.DFDT.get().format(System.currentTimeMillis()));
		nameField.addListener(SWT.Modify, this);
		new Label(composite, SWT.NONE).setText(Messages.SlideShowSaveDialog_group);
		groupField = new Combo(composite, SWT.DROP_DOWN);
		groupField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		List<GroupImpl> set = Core.getCore().getDbManager().obtainObjects(GroupImpl.class);
		int slideshowGroup = -1;
		int i = 0;
		List<String> items = new ArrayList<String>(set.size());
		for (GroupImpl group : set) {
			boolean exhibition = !group.getExhibition().isEmpty();
			boolean slideshow = !group.getSlideshow().isEmpty();
			boolean webgallery = group.getWebGallery() != null && !group.getWebGallery().isEmpty();
			boolean collection = !group.getRootCollection().isEmpty();
			if (!exhibition && !slideshow && !webgallery && !collection) {
				String groupId = group.getStringId();
				exhibition = Constants.GROUP_ID_EXHIBITION.equals(groupId);
				slideshow = Constants.GROUP_ID_SLIDESHOW.equals(groupId);
				if (slideshow)
					slideshowGroup = i;
				webgallery = Constants.GROUP_ID_WEBGALLERY.equals(groupId);
				if (!exhibition && !slideshow && !webgallery)
					slideshow = true;
			}
			if (slideshow) {
				String n = group.getName();
				groupMap.put(n, group);
				items.add(n);
				++i;
			}
		}
		String[] array = items.toArray(new String[items.size()]);
		Arrays.sort(array);
		groupField.setItems(array);
		if (slideshowGroup >= 0)
			groupField.select(slideshowGroup);
		if (array.length > 0)
			groupField.setText(array[slideshowGroup >= 0 ? slideshowGroup : 0]);
		groupField.addListener(SWT.Modify, this);
		groupField.addListener(SWT.Selection, this);
		openButton = WidgetFactory.createCheckButton(composite, Messages.SlideShowSaveDialog_open_editor,
				new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		nameField.setFocus();
		return area;
	}

	@Override
	protected void okPressed() {
		name = nameField.getText().trim();
		String groupName = groupField.getText().trim();
		selectedGroup = groupMap.get(groupName);
		if (selectedGroup == null)
			selectedGroup = new GroupImpl(groupName, false, Constants.INHERIT_LABEL, null, 0, 1, null);
		open = openButton.getSelection();
		super.okPressed();
	}

	public String getName() {
		return name;
	}

	public Group getGroup() {
		return selectedGroup;
	}

	public boolean getOpen() {
		return open;
	}

	private void updateButtons() {
		boolean enabled = validate();
		getShell().setModified(enabled);
		getButton(IDialogConstants.OK_ID).setEnabled(enabled);
	}

	private boolean validate() {
		String n = nameField.getText().trim();
		if (n.isEmpty()) {
			setErrorMessage(Messages.SlideShowSaveDialog_please_enter_slideshow_name);
			return false;
		}
		IDbManager db = Core.getCore().getDbManager();
		List<SlideShowImpl> set = db.obtainObjects(SlideShowImpl.class, "name", n, //$NON-NLS-1$
				QueryField.EQUALS);
		if (!set.isEmpty()) {
			setErrorMessage(Messages.SlideShowSaveDialog_a_slideshow_with_that_name_exists);
			return false;
		}
		String groupName = groupField.getText().trim();
		if (groupName.isEmpty()) {
			setErrorMessage(Messages.SlideShowSaveDialog_please_enter_group_name);
			return false;
		}
		if (!groupMap.containsKey(groupName)) {
			List<GroupImpl> groups = db.obtainObjects(GroupImpl.class, "name", groupName, //$NON-NLS-1$
					QueryField.EQUALS);
			if (!groups.isEmpty()) {
				setErrorMessage(Messages.SlideShowSaveDialog_group_already_exists);
				return false;
			}
		}
		setErrorMessage(null);
		return true;
	}

	@Override
	public void handleEvent(Event e) {
		updateButtons();
	}

}
