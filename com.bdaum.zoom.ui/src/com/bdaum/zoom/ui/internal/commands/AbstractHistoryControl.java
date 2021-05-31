package com.bdaum.zoom.ui.internal.commands;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.ui.HistoryListener;
import com.bdaum.zoom.ui.INavigationHistory;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.internal.Icons.Icon;
import com.bdaum.zoom.ui.internal.NavigationHistory.HistoryItem;
import com.bdaum.zoom.ui.internal.UiUtilities;

public abstract class AbstractHistoryControl extends WorkbenchWindowControlContribution
		implements HistoryListener, Listener {

	private IWorkbenchWindow window;
	protected INavigationHistory navigationHistory;
	protected Label label;
	private Menu menu;

	public AbstractHistoryControl() {
		super();
	}

	public AbstractHistoryControl(String id) {
		super(id);
	}

	private static String composeName(SmartCollection sm) {
		String name = sm.getAdhoc() ? UiUtilities.composeContentDescription(sm, " - ", true, false) //$NON-NLS-1$
				: sm.getName();
		while (true) {
			SmartCollection parent = sm.getSmartCollection_subSelection_parent();
			if (parent == null)
				break;
			String pname = parent.getName();
			if (name.length() + pname.length() > 62) {
				name = "…>" + name; //$NON-NLS-1$
				break;
			}
			name = pname + ">" + name; //$NON-NLS-1$
			sm = parent;
		}
		if (name.length() > 64)
			return name.substring(0, 64) + "…"; //$NON-NLS-1$
		return name;
	}

	@Override
	protected Control createControl(Composite parent) {
		window = getWorkbenchWindow();
		navigationHistory = Ui.getUi().getNavigationHistory(window);
		navigationHistory.addHistoryListener(this);
		if (parent != null && !parent.isDisposed()) {
			label = new Label(parent, SWT.NONE);
			label.setImage(getIcon().getImage());
			label.addListener(SWT.MouseDown, this);
			label.addListener(SWT.MouseHover, this);
		}
		return label;
	}

	protected abstract Icon getIcon();

	private void createDropDownMenu() {
		disposeMenu();
		List<HistoryItem> historyItems = getHistoryItems();
		if (historyItems.isEmpty())
			return;
		menu = new Menu(window.getShell(), SWT.POP_UP);
		IDbManager dbManager = Core.getCore().getDbManager();
		int i = 0;
		for (final HistoryItem historyItem : historyItems) {
			if (++i > 16)
				break;
			Object query = historyItem.getQuery();
			if (query instanceof String)
				query = dbManager.obtainById(IdentifiableObject.class, (String) query);
			if (query instanceof SmartCollection) {
				SmartCollection sm = (SmartCollection) query;
				MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
				menuItem.setText(composeName(sm));
				Icon icon = UiUtilities.getSmartCollectionIcon(sm);
				if (icon != null)
					menuItem.setImage(icon.getImage());
				menuItem.addListener(SWT.Selection, new Listener() {
					@Override
					public void handleEvent(Event e) {
						BusyIndicator.showWhile(window.getShell().getDisplay(), () -> goTo(historyItem));
					}
				});
			}
		}
		menu.setVisible(true);
	}

	protected abstract List<HistoryItem> getHistoryItems();

	protected abstract void goTo(HistoryItem item);

	protected abstract void goTo();

	@Override
	public void queryHistoryChanged(IdentifiableObject obj) {
		// do nothing
	}

	@Override
	public void dispose() {
		navigationHistory.removeHistoryListener(this);
		disposeMenu();
		super.dispose();
	}

	protected void disposeMenu() {
		if (menu != null) {
			dispose();
			menu = null;
		}
	}

	@Override
	public void handleEvent(Event e) {
		switch (e.type) {
		case SWT.MouseHover:
			createDropDownMenu();
			break;
		case SWT.MouseDown:
			if (e.button <= 1)
				BusyIndicator.showWhile(window.getShell().getDisplay(), () -> goTo());
			else
				createDropDownMenu();
		}
	}

}