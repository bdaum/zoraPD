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

package com.bdaum.zoom.ui.internal.preferences;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.Scheme;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeySequenceText;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.keys.IBindingService;

import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.SortColumnManager;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiConstants;
import com.bdaum.zoom.ui.internal.ZViewerComparator;
import com.bdaum.zoom.ui.preferences.AbstractPreferencePage;

public class KeyPreferencePage extends AbstractPreferencePage {
	public static final String ID = "com.bdaum.zoom.ui.preferences.KeywordPreferencePage"; //$NON-NLS-1$

	private static Set<String> hidden;

	private TableViewer bindingViewer;
	private TableViewerColumn commandColumn;
	private TableViewerColumn keyColumn;
	private TableViewerColumn catColumn;
	private ZColumnLabelProvider commandLabelProvider;
	private ZColumnLabelProvider keyLabelProvider;
	private ZColumnLabelProvider catLabelProvider;
	private Text nameField;
	private Text keyField;
	private Text descriptionField;
	private KeySequenceText keySequenceField;
	private Command selectedCommand;
	private TableViewer conflictViewer;
	private String activeSchemeId;
	private Map<TriggerSequence, Binding> systemMap = new HashMap<TriggerSequence, Binding>();
	private Map<TriggerSequence, Binding> userMap = new HashMap<TriggerSequence, Binding>();
	private Map<String, Binding> commandMap = new HashMap<String, Binding>();
	private Command[] definedCommands;
	private Label userLabel;

	static {
		hidden = new HashSet<String>(Arrays.asList("org.eclipse.ui.help.dynamicHelp", //$NON-NLS-1$
				"org.eclipse.ui.help.quickStartAction", //$NON-NLS-1$
				"org.eclipse.ui.help.tipsAndTricksAction", //$NON-NLS-1$
				"org.eclipse.ui.help.installationDialog", //$NON-NLS-1$
				"org.eclipse.ui.newWizard", "org.eclipse.ui.file.close", //$NON-NLS-1$ //$NON-NLS-2$
				"org.eclipse.ui.file.closeAll", "org.eclipse.ui.file.import", //$NON-NLS-1$ //$NON-NLS-2$
				"org.eclipse.ui.file.save", "org.eclipse.ui.file.saveAs", //$NON-NLS-1$ //$NON-NLS-2$
				"org.eclipse.ui.file.saveAll", "org.eclipse.ui.file.revert", //$NON-NLS-1$ //$NON-NLS-2$
				"org.eclipse.ui.file.refresh", "org.eclipse.ui.file.properties", //$NON-NLS-1$ //$NON-NLS-2$
				"org.eclipse.ui.window.maximizePart", "org.eclipse.ui.window.minimizePart", //$NON-NLS-1$ //$NON-NLS-2$
				"org.eclipse.ui.file.restartWorkbench", //$NON-NLS-1$
				"org.eclipse.ui.edit.cut", //$NON-NLS-1$
				"org.eclipse.ui.edit.delete", //$NON-NLS-1$
				"org.eclipse.ui.edit.text.contentAssist.proposals", //$NON-NLS-1$
				"org.eclipse.ui.edit.text.contentAssist.contextInformation", //$NON-NLS-1$
				"org.eclipse.ui.edit.move", "org.eclipse.ui.edit.rename", //$NON-NLS-1$ //$NON-NLS-2$
				"org.eclipse.ui.edit.findReplace", //$NON-NLS-1$
				"org.eclipse.ui.navigate.goInto", "org.eclipse.ui.navigate.up", //$NON-NLS-1$ //$NON-NLS-2$
				"org.eclipse.ui.navigate.next", //$NON-NLS-1$
				"org.eclipse.ui.navigate.backwardHistory", //$NON-NLS-1$
				"org.eclipse.ui.navigate.forwardHistory", //$NON-NLS-1$
				"org.eclipse.ui.navigate.previous", //$NON-NLS-1$
				"org.eclipse.ui.navigate.linkWithEditor", //$NON-NLS-1$
				"org.eclipse.ui.window.newWindow", //$NON-NLS-1$
				"org.eclipse.ui.window.newEditor", //$NON-NLS-1$
				"org.eclipse.ui.window.openEditorDropDown", //$NON-NLS-1$
				"org.eclipse.ui.window.quickAccess", //$NON-NLS-1$
				"org.eclipse.ui.window.switchToEditor", //$NON-NLS-1$
				"org.eclipse.ui.window.showSystemMenu", //$NON-NLS-1$
				"org.eclipse.ui.window.activateEditor", //$NON-NLS-1$
				"org.eclipse.ui.window.nextEditor", //$NON-NLS-1$
				"org.eclipse.ui.window.previousEditor", //$NON-NLS-1$
				"org.eclipse.ui.window.nextView", //$NON-NLS-1$
				"org.eclipse.ui.window.previousView", //$NON-NLS-1$
				"org.eclipse.ui.window.nextPerspective", //$NON-NLS-1$
				"org.eclipse.ui.window.previousPerspective", //$NON-NLS-1$
				"org.eclipse.ui.file.closePart", //$NON-NLS-1$
				"org.eclipse.ui.window.hideShowEditors", //$NON-NLS-1$
				"org.eclipse.ui.window.lockToolBar", //$NON-NLS-1$
				"org.eclipse.ui.window.pinEditor", //$NON-NLS-1$
				"org.eclipse.ui.file.closeOthers", "org.eclipse.ui.part.nextPage", //$NON-NLS-1$ //$NON-NLS-2$
				"org.eclipse.ui.part.previousPage", //$NON-NLS-1$
				"org.eclipse.ui.navigate.nextSubTab", //$NON-NLS-1$
				"org.eclipse.ui.navigate.previousSubTab", //$NON-NLS-1$
				"org.eclipse.ui.navigate.nextTab", //$NON-NLS-1$
				"org.eclipse.ui.navigate.previousTab", //$NON-NLS-1$
				"org.eclipse.ui.navigate.collapseAll", //$NON-NLS-1$
				"org.eclipse.ui.navigate.back", //$NON-NLS-1$
				"org.eclipse.ui.navigate.forward", //$NON-NLS-1$
				"org.eclipse.ui.navigate.showIn", //$NON-NLS-1$
				"org.eclipse.ui.browser.openBundleResource", //$NON-NLS-1$
				"org.eclipse.ui.dialogs.openMessageDialog", //$NON-NLS-1$
				"org.eclipse.help.ui.indexcommand", //$NON-NLS-1$
				"org.eclipse.ui.navigate.expandAll", //$NON-NLS-1$
				"org.eclipse.ui.activeContextInfo", //$NON-NLS-1$
				"org.eclipse.ui.window.fullscreenmode", //$NON-NLS-1$
				"org.eclipse.help.ui.ignoreMissingPlaceholders", //$NON-NLS-1$
				"org.eclipse.ui.window.hidetrimbars", //$NON-NLS-1$
				"org.eclipse.ui.window.togglestatusbar", //$NON-NLS-1$
				"org.eclipse.ui.cheatsheets.openCheatSheetURL", //$NON-NLS-1$
				"org.eclipse.ui.file.closeAllSaved", //$NON-NLS-1$
				"org.eclipse.help.ui.closeTray", //$NON-NLS-1$
				"org.eclipse.ui.window.spy", //$NON-NLS-1$
				"org.eclipse.ui.dialogs.openInputDialog", //$NON-NLS-1$
				"org.eclipse.ui.window.showContextMenu", //$NON-NLS-1$
				"org.eclipse.ui.ToggleCoolbarAction", //$NON-NLS-1$
				"org.eclipse.ui.window.splitEditor", //$NON-NLS-1$
				"org.eclipse.ui.window.showViewMenu", //$NON-NLS-1$
				"org.eclipse.ui.help.displayHelp", //$NON-NLS-1$
				"org.eclipse.ui.cheatsheets.openCheatSheet", //$NON-NLS-1$
				"com.bdaum.zoom.ui.command3")); //$NON-NLS-1$
	}

	public KeyPreferencePage() {
		setDescription(Messages.getString("KeyPreferencePage.key_descr")); //$NON-NLS-1$
	}

	@Override
	public void init(IWorkbench aWorkbench) {
		super.init(aWorkbench);
		ICommandService commandService = workbench.getService(ICommandService.class);
		Command[] cmds = commandService.getDefinedCommands();
		List<Command> list = new ArrayList<Command>(cmds.length);
		for (Command command : cmds)
			if (!hidden.contains(command.getId()))
				list.add(command);
		definedCommands = list.toArray(new Command[list.size()]);
	}

	@Override
	protected void createPageContents(Composite parent) {
		setHelp(HelpContextIds.KEY_PREFERENCE_PAGE);
		createBindingTable(parent);
		createButtonBar(parent);
		createDetailsArea(parent);
		fillValues();
	}

	@SuppressWarnings("unused")
	private void createBindingTable(Composite composite) {
		bindingViewer = new TableViewer(composite, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.FULL_SELECTION);
		Table table = bindingViewer.getTable();
		table.setLayoutData(new GridData(550, 300));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		commandColumn = createColumn(bindingViewer, Messages.getString("KeyPreferencePage.command"), 200); //$NON-NLS-1$
		commandLabelProvider = new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Command) {
					try {
						return ((Command) element).getName();
					} catch (NotDefinedException e) {
						return Messages.getString("KeyPreferencePage.undefined"); //$NON-NLS-1$
					}
				}
				return element.toString();
			}

			@Override
			public Font getFont(Object element) {
				if (element instanceof Command) {
					Binding binding = commandMap.get(((Command) element).getId());
					if (binding != null && binding.getType() == Binding.USER)
						return JFaceResources.getFont(UiConstants.ITALICFONT);
				}
				return super.getFont(element);
			}
			
		};
		commandColumn.setLabelProvider(commandLabelProvider);
		keyColumn = createColumn(bindingViewer, Messages.getString("KeyPreferencePage.keys"), 150); //$NON-NLS-1$
		keyLabelProvider = new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Command) {
					Binding binding = commandMap.get(((Command) element).getId());
					if (binding != null)
						return binding.getTriggerSequence().format();
					return null;
				}
				return element.toString();
			}
		};
		keyColumn.setLabelProvider(keyLabelProvider);
		catColumn = createColumn(bindingViewer, Messages.getString("KeyPreferencePage.category"), 150); //$NON-NLS-1$
		catLabelProvider = new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Command)
					try {
						return ((Command) element).getCategory().getName();
					} catch (NotDefinedException e) {
						return Messages.getString("KeyPreferencePage.undefined"); //$NON-NLS-1$
					}
				return element.toString();
			}
		};
		catColumn.setLabelProvider(catLabelProvider);
		bindingViewer.setContentProvider(ArrayContentProvider.getInstance());
		new SortColumnManager(bindingViewer, new int[] { SWT.UP, SWT.UP, SWT.UP }, 0);
		bindingViewer.setComparator(ZViewerComparator.INSTANCE);
		bindingViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateDetails();
			}
		});
	}

	private static TableViewerColumn createColumn(final TableViewer viewer, String lab, int w) {
		final TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setText(lab);
		column.getColumn().setWidth(w);
		return column;
	}

	private final Control createButtonBar(final Composite parent) {
		final Composite buttonBar = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		buttonBar.setLayout(layout);
		buttonBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final Button removeBindingButton = new Button(buttonBar, SWT.PUSH);
		removeBindingButton.setText(Messages.getString("KeyPreferencePage.remove_shortcuts")); //$NON-NLS-1$
		removeBindingButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public final void widgetSelected(final SelectionEvent event) {
				if (selectedCommand != null) {
					Binding b = commandMap.get(selectedCommand.getId());
					if (b != null) {
						Binding userBinding = new KeyBinding((KeySequence) b.getTriggerSequence(), null, activeSchemeId,
								b.getContextId(), null, null, null, Binding.USER);
						userMap.put(userBinding.getTriggerSequence(), userBinding);
						commandMap.remove(selectedCommand.getId());
					}
					refreshViewer();
					doValidate();
				}
			}
		});

		final Button restore = new Button(buttonBar, SWT.PUSH);
		restore.setText(Messages.getString("KeyPreferencePage.restore_command")); //$NON-NLS-1$
		restore.addSelectionListener(new SelectionAdapter() {
			@Override
			public final void widgetSelected(final SelectionEvent event) {
				if (selectedCommand != null) {
					String id = selectedCommand.getId();
					Binding b = commandMap.get(id);
					userMap.remove(b.getTriggerSequence());
					for (Binding sb : systemMap.values()) {
						if (sb.getParameterizedCommand() != null && sb.getParameterizedCommand().getId().equals(id)) {
							commandMap.put(sb.getParameterizedCommand().getId(), sb);
							break;
						}
					}
					refreshViewer();
					doValidate();
				}
			}
		});
		return buttonBar;
	}

	protected void refreshViewer() {
		ISelection selection = bindingViewer.getSelection();
		bindingViewer.setInput(definedCommands);
		bindingViewer.setSelection(selection);
	}

	private void createDetailsArea(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout(2, false));
		createDefinitionArea(comp);
		createConflictsArea(comp);
	}

	@SuppressWarnings("unused")
	private void createDefinitionArea(Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, false));
		new Label(composite, SWT.NONE).setText(Messages.getString("KeyPreferencePage.command2")); //$NON-NLS-1$
		nameField = new Text(composite, SWT.SINGLE | SWT.LEAD | SWT.BORDER | SWT.READ_ONLY);
		nameField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		new Label(composite, SWT.NONE).setText(Messages.getString("KeyPreferencePage.description")); //$NON-NLS-1$
		descriptionField = new Text(composite, SWT.MULTI | SWT.LEAD | SWT.BORDER | SWT.READ_ONLY | SWT.WRAP);
		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		layoutData.heightHint = 50;
		descriptionField.setLayoutData(layoutData);
		new Label(composite, SWT.NONE).setText(Messages.getString("KeyPreferencePage.key_sequence")); //$NON-NLS-1$
		Composite keyGroup = new Composite(composite, SWT.NONE);
		keyGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		keyGroup.setLayout(layout);
		keyField = new Text(keyGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		keyField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		keySequenceField = new KeySequenceText(keyField);
		keySequenceField.setKeyStrokeLimit(2);
		keySequenceField.addPropertyChangeListener(new IPropertyChangeListener() {
			public final void propertyChange(final PropertyChangeEvent event) {
				if (!event.getOldValue().equals(event.getNewValue())) {
					final KeySequence keySequence = keySequenceField.getKeySequence();
					if (selectedCommand == null || !keySequence.isComplete())
						return;
					boolean empty = keySequence.isEmpty();
					Binding newBinding;
					Binding b = commandMap.get(selectedCommand.getId());
					if (b != null) {
						if (!keySequence.equals(b.getTriggerSequence())) {
							newBinding = new KeyBinding(keySequence, empty ? null : b.getParameterizedCommand(),
									activeSchemeId, b.getContextId(), null, null, null, Binding.USER);
							userMap.remove(b.getTriggerSequence());
							userMap.put(keySequence, newBinding);
							if (empty)
								commandMap.remove(selectedCommand.getId());
							else
								commandMap.put(selectedCommand.getId(), newBinding);
						}
					} else if (!empty) {
						ParameterizedCommand pc = new ParameterizedCommand(selectedCommand, null);
						newBinding = new KeyBinding(keySequence, pc, activeSchemeId, "org.eclipse.ui.contexts.window", //$NON-NLS-1$
								null, null, null, Binding.USER);
						userMap.put(keySequence, newBinding);
						commandMap.put(selectedCommand.getId(), newBinding);
					}
					refreshViewer();
					doValidate();
					keyField.setSelection(keyField.getTextLimit());
				}
			}
		});
		final Button helpButton = new Button(keyGroup, SWT.ARROW | SWT.LEFT);
		helpButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		helpButton.setToolTipText(Messages.getString("KeyPreferencePage.add_a_special_key")); //$NON-NLS-1$
		// Arrow buttons aren't normally added to the tab list. Let's fix that.
		final Control[] tabStops = keyGroup.getTabList();
		final ArrayList<Control> newTabStops = new ArrayList<Control>();
		for (int i = 0; i < tabStops.length; i++) {
			Control tabStop = tabStops[i];
			newTabStops.add(tabStop);
			if (keyField.equals(tabStop))
				newTabStops.add(helpButton);
		}
		keyGroup.setTabList(newTabStops.toArray(new Control[newTabStops.size()]));

		// Construct the menu to attach to the above button.
		final Menu addKeyMenu = new Menu(helpButton);
		final Iterator<?> trappedKeyItr = KeySequenceText.TRAPPED_KEYS.iterator();
		while (trappedKeyItr.hasNext()) {
			final KeyStroke trappedKey = (KeyStroke) trappedKeyItr.next();
			final MenuItem menuItem = new MenuItem(addKeyMenu, SWT.PUSH);
			menuItem.setText(trappedKey.format());
			menuItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					keySequenceField.insert(trappedKey);
					keyField.setFocus();
					keyField.setSelection(keyField.getTextLimit());
				}
			});
		}
		helpButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				Point buttonLocation = helpButton.getLocation();
				buttonLocation = composite.toDisplay(buttonLocation.x, buttonLocation.y);
				Point buttonSize = helpButton.getSize();
				addKeyMenu.setLocation(buttonLocation.x, buttonLocation.y + buttonSize.y);
				addKeyMenu.setVisible(true);
			}
		});
		new Label(composite, SWT.NONE);
		userLabel = new Label(composite, SWT.NONE);
		userLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	}

	private void createConflictsArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(1, false));
		new Label(composite, SWT.NONE).setText(Messages.getString("KeyPreferencePage.conflicts")); //$NON-NLS-1$
		conflictViewer = new TableViewer(composite,
				SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		Table table = conflictViewer.getTable();
		TableViewerColumn bindingNameColumn = new TableViewerColumn(conflictViewer, SWT.LEAD);
		bindingNameColumn.getColumn().setWidth(250);
		table.setLayoutData(new GridData(250, 80));
		bindingNameColumn.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Binding[]) {
					StringBuilder sb = new StringBuilder();
					for (Binding binding : ((Binding[]) element)) {
						ParameterizedCommand parameterizedCommand = binding.getParameterizedCommand();
						String s = Messages.getString("KeyPreferencePage.undefined"); //$NON-NLS-1$
						if (parameterizedCommand != null)
							try {
								s = parameterizedCommand.getName();
							} catch (NotDefinedException e) {
								// do nothing
							}
						if (sb.length() > 0)
							sb.append("; "); //$NON-NLS-1$
						sb.append(s);
					}
					return sb.toString();
				}
				return element.toString();
			}
		});
		conflictViewer.setContentProvider(ArrayContentProvider.getInstance());
		conflictViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (!selection.isEmpty()) {
					Binding[] conflict = (Binding[]) selection.getFirstElement();
					bindingViewer.setSelection(new StructuredSelection(conflict[0]));
				}
			}
		});
	}

	@Override
	protected void fillValues() {
		IBindingService bindingService = PlatformUI.getWorkbench().getService(IBindingService.class);
		userMap.clear();
		systemMap.clear();
		commandMap.clear();
		activeSchemeId = bindingService.getActiveScheme().getId();
		Binding[] bindings = bindingService.getBindings();
		for (Binding binding : bindings) {
			if (activeSchemeId.equals(binding.getSchemeId())) {
				TriggerSequence triggerSequence = binding.getTriggerSequence();
				if (binding.getType() == Binding.SYSTEM)
					systemMap.put(triggerSequence, binding);
				else
					userMap.put(triggerSequence, binding);
			}
		}
		updateViewer();
		ISelection selection = bindingViewer.getSelection();
		if (selection.isEmpty())
			bindingViewer.setSelection(new StructuredSelection(bindingViewer.getElementAt(0)), true);
	}

	private void updateViewer() {
		for (Binding binding : systemMap.values()) {
			ParameterizedCommand parameterizedCommand = binding.getParameterizedCommand();
			if (parameterizedCommand != null)
				commandMap.put(parameterizedCommand.getId(), binding);
		}
		for (Binding binding : userMap.values()) {
			ParameterizedCommand parameterizedCommand = binding.getParameterizedCommand();
			if (parameterizedCommand != null)
				commandMap.put(parameterizedCommand.getId(), binding);
			else {
				Binding systemBinding = systemMap.get(binding.getTriggerSequence());
				if (systemBinding != null) {
					parameterizedCommand = systemBinding.getParameterizedCommand();
					if (parameterizedCommand != null)
						commandMap.remove(parameterizedCommand.getId());
				}
			}
		}
		refreshViewer();
	}

	@Override
	protected String doValidate() {
		Map<TriggerSequence, List<Binding>> conflictMap = new HashMap<>();
		for (Map.Entry<TriggerSequence, Binding> entry : systemMap.entrySet()) {
			TriggerSequence key = entry.getKey();
			Binding binding = userMap.get(key);
			if (binding == null)
				binding = entry.getValue();
			if (binding.getParameterizedCommand() != null && binding.getSchemeId().equals(activeSchemeId)) {
				TriggerSequence triggerSequence = binding.getTriggerSequence();
				List<Binding> list = conflictMap.get(triggerSequence);
				if (list == null) {
					list = new ArrayList<Binding>();
					conflictMap.put(triggerSequence, list);
				}
				list.add(binding);
			}
		}
		for (Map.Entry<TriggerSequence, Binding> entry : userMap.entrySet()) {
			TriggerSequence key = entry.getKey();
			Binding userBinding = entry.getValue();
			Binding binding = systemMap.get(key);
			if (binding != null && binding.getParameterizedCommand() != null
					&& userBinding.getParameterizedCommand() != null
					&& !binding.getParameterizedCommand().getId().equals(userBinding.getParameterizedCommand().getId())
					&& binding.getSchemeId().equals(activeSchemeId)) {
				TriggerSequence triggerSequence = binding.getTriggerSequence();
				List<Binding> list = conflictMap.get(triggerSequence);
				if (list == null) {
					list = new ArrayList<Binding>();
					conflictMap.put(triggerSequence, list);
				}
				if (!list.contains(binding))
					list.add(binding);
			}
		}
		List<Binding[]> conflicts = new ArrayList<Binding[]>();
		for (List<Binding> conflict : conflictMap.values())
			if (conflict.size() > 1)
				conflicts.add(conflict.toArray(new Binding[conflict.size()]));
		conflictViewer.setInput(conflicts);
		return conflicts.isEmpty() ? null : Messages.getString("KeyPreferencePage.there_are_conflicts"); //$NON-NLS-1$
	}

	@Override
	protected void doPerformOk() {
		List<Binding> allBindings = new ArrayList<Binding>(systemMap.size() + userMap.size());
		allBindings.addAll(systemMap.values());
		allBindings.addAll(userMap.values());
		saveConfiguration(allBindings);
	}

	@Override
	protected void doPerformDefaults() {
		userMap.clear();
		saveConfiguration(systemMap.values());
		updateViewer();
		validate();
	}

	private static void saveConfiguration(Collection<Binding> bindings) {
		IBindingService bindingService = PlatformUI.getWorkbench().getService(IBindingService.class);
		Scheme activeScheme = bindingService.getActiveScheme();
		try {
			bindingService.savePreferences(activeScheme, bindings.toArray(new Binding[bindings.size()]));
		} catch (IOException e) {
			UiActivator.getDefault().logError(Messages.getString("KeyPreferencePage.io_error"), e); //$NON-NLS-1$
		}
	}

	private void updateDetails() {
		selectedCommand = (Command) bindingViewer.getStructuredSelection().getFirstElement();
		if (selectedCommand != null) {
			try {
				String name = selectedCommand.getName();
				nameField.setText(name == null ? "" : name); //$NON-NLS-1$
				String description = selectedCommand.getDescription();
				descriptionField.setText(description == null ? "" //$NON-NLS-1$
						: description);
				Binding binding = commandMap.get(selectedCommand.getId());
				if (binding != null) {
					TriggerSequence triggerSequence = binding.getTriggerSequence();
					if (triggerSequence instanceof KeySequence)
						keySequenceField.setKeySequence((KeySequence) triggerSequence);
					else
						keySequenceField.setKeySequence(null);
					userLabel.setText(binding.getType() == Binding.USER ? Messages.getString("KeyPreferencePage.user_defined") : ""); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					keySequenceField.setKeySequence(null);
					userLabel.setText(""); //$NON-NLS-1$
				}
			} catch (NotDefinedException e) {
				// ignore
			}
		}
	}

}
