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
 * (c) 2013 Berthold Daum  
 */
package com.bdaum.zoom.peer.internal.preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;

import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.peer.internal.IPeerListener;
import com.bdaum.zoom.peer.internal.PeerActivator;
import com.bdaum.zoom.peer.internal.model.PeerDefinition;
import com.bdaum.zoom.peer.internal.model.SharedCatalog;
import com.bdaum.zoom.peer.internal.ui.HelpContextIds;
import com.bdaum.zoom.peer.internal.ui.PeerDefinitionDialog;
import com.bdaum.zoom.ui.internal.SortColumnManager;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.ZViewerComparator;
import com.bdaum.zoom.ui.preferences.AbstractPreferencePage;
import com.bdaum.zoom.ui.widgets.CGroup;

@SuppressWarnings("restriction")
public class PeerPreferencePage extends AbstractPreferencePage implements IPeerListener, Listener, ISelectionChangedListener {

	private static final Object[] EMPTY = new Object[0];
	private TreeViewer catViewer;
	private Spinner portField;
	private Text ipLabel;
	private TableViewer peerViewer;
	private TableViewer incomingViewer;
	private Button addCurrentButton;
	private Button removeCatButton;
	private Button addRestrictionButton;
	private Label updateLabel;

	public PeerPreferencePage() {
		setDescription(Messages.PeerPreferencePage_peer_and_shared_cat_definition);
	}

	@Override
	public void init(IWorkbench aWorkbench) {
		this.workbench = aWorkbench;
		setPreferenceStore(PeerActivator.getDefault().getPreferenceStore());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected String doValidate() {
		String errorMessage = null;
		Collection<PeerDefinition> incoming = (Collection<PeerDefinition>) incomingViewer.getInput();
		lp: for (SharedCatalog sharedCatalog : (List<SharedCatalog>) catViewer.getInput()) {
			for (PeerDefinition peer : sharedCatalog.getRestrictions()) {
				File path = sharedCatalog.getPath();
				if (!path.exists()) {
					errorMessage = NLS.bind(Messages.PeerPreferencePage_catalog_does_not_exist, path);
					break lp;
				}
				for (PeerDefinition p : incoming) {
					if (p.isBlocked() && peer.getHost().equals(p.getHost()) && peer.getPort() == p.getPort()) {
						errorMessage = NLS.bind(Messages.PeerPreferencePage_restriction_obsolete, peer.getLocation());
						break lp;
					}
				}
			}
		}
		return errorMessage;
	}

	@Override
	protected void createPageContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);

		createHeaderGroup(composite);
		createTabFolder(composite, "Peer"); //$NON-NLS-1$
		UiUtilities.createTabItem(tabFolder, Messages.PeerPreferencePage_shared_cats,
				Messages.PeerPreferencePage_cats_tooltip).setControl(createSharedGroup(tabFolder));
		UiUtilities.createTabItem(tabFolder, Messages.PeerPreferencePage_peer_nodes,
				Messages.PeerPreferencePage_peer_tooltip).setControl(createPeerGroup(tabFolder));
		UiUtilities.createTabItem(tabFolder, Messages.PeerPreferencePage_incoming_calls,
				Messages.PeerPreferencePage_incoming_tooltip).setControl(createIncomingGroup(tabFolder));
		initTabFolder(0);
		setHelp(HelpContextIds.PEER_PREFERENCE_PAGE);
		fillValues();
		PeerActivator.getDefault().addPeerListener(this);
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		getDefaultsButton().setEnabled(false);
	}

	private void createHeaderGroup(Composite composite) {
		String hostName = PeerActivator.getDefault().getHostName();
		String host = PeerActivator.getDefault().getHost();
		String location = hostName.isEmpty() ? host
				: NLS.bind("{0} ({1})", //$NON-NLS-1$
						hostName, host);
		CGroup locationGroup = new CGroup(composite, SWT.NONE);
		locationGroup.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		locationGroup.setLayout(new GridLayout(6, false));
		locationGroup.setText(Messages.PeerPreferencePage_own_location);
		new Label(locationGroup, SWT.NONE).setText(Messages.PeerPreferencePage_computer_name);
		ipLabel = new Text(locationGroup, SWT.READ_ONLY | SWT.BORDER);
		ipLabel.setText(location);
		Label label = new Label(locationGroup, SWT.NONE);
		GridData gridData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		gridData.horizontalIndent = 20;
		label.setLayoutData(gridData);
		label.setText(Messages.PeerPreferencePage_port);
		portField = new Spinner(locationGroup, SWT.BORDER);
		portField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		portField.setMaximum(65535);
		portField.setIncrement(1);
		updateLabel = new Label(locationGroup, SWT.NONE);
		updateLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		new Label(locationGroup, SWT.NONE).setText(NLS.bind(Messages.PeerPreferencePage_defaultport,
				getPreferenceStore().getDefaultInt(PreferenceConstants.PORT)));
	}

	@SuppressWarnings("unused")
	private Control createIncomingGroup(final CTabFolder parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout(1, false));
		Composite innerComp = new Composite(comp, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 20;
		innerComp.setLayout(layout);
		innerComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Label label = new Label(innerComp, SWT.WRAP);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		label.setText(Messages.PeerPreferencePage_incoming_msg);
		incomingViewer = new TableViewer(innerComp, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
		incomingViewer.getTable().setLinesVisible(true);
		incomingViewer.getTable().setHeaderVisible(true);
		incomingViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		final TableViewerColumn col1 = new TableViewerColumn(incomingViewer, SWT.NONE);
		col1.getColumn().setWidth(250);
		col1.getColumn().setText(Messages.PeerPreferencePage_calling_peer);
		col1.setLabelProvider(ZColumnLabelProvider.getDefaultInstance());

		final TableViewerColumn col2 = new TableViewerColumn(incomingViewer, SWT.NONE);
		col2.getColumn().setWidth(250);
		col2.getColumn().setText(Messages.PeerPreferencePage_last_access);
		col2.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof PeerDefinition)
					return Format.YMD_TIME_FORMAT.get().format(((PeerDefinition) element).getLastAccess());
				return  ""; //$NON-NLS-1$
			}
		});
		TableViewerColumn col3 = new TableViewerColumn(incomingViewer, SWT.NONE);
		col3.getColumn().setWidth(250);
		col3.getColumn().setText(Messages.PeerPreferencePage_last_op);
		col3.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof PeerDefinition)
					return ((PeerDefinition) element).getRightsLabel();
				return  ""; //$NON-NLS-1$
			}

			@Override
			public Color getForeground(Object element) {
				if (element instanceof PeerDefinition)
					return ((PeerDefinition) element).isBlocked() ? parent.getDisplay().getSystemColor(SWT.COLOR_RED)
							: null;
				return super.getForeground(element);
			}
		});
		new SortColumnManager(incomingViewer, new int[] {SWT.UP,  SWT.DOWN, SWT.NONE}, 1);
		incomingViewer.setContentProvider(ArrayContentProvider.getInstance());
		incomingViewer.setComparator(new ZViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (e1 instanceof PeerDefinition && e2 instanceof PeerDefinition) {
					Table table = incomingViewer.getTable();
					int sortDirection = table.getSortDirection();
					TableColumn sortColumn = incomingViewer.getTable().getSortColumn();
					int result;
					if (sortColumn == table.getColumn(0))
						result = UiUtilities.stringComparator.compare(((PeerDefinition) e1).getLocation(),
								((PeerDefinition) e2).getLocation());
					else {
						long t1 = ((PeerDefinition) e1).getLastAccess();
						long t2 = ((PeerDefinition) e2).getLastAccess();
						result = t1 > t2 ? 1 : t1 < t2 ? -1 : 0;
					}
					return sortDirection == SWT.DOWN ? result : -result;
				}
				return super.compare(viewer, e1, e2);
			}

		});
		Composite buttonGroup = new Composite(innerComp, SWT.NONE);
		buttonGroup.setLayoutData(new GridData(SWT.END, SWT.BEGINNING, false, true));
		buttonGroup.setLayout(new GridLayout());
		Button clearButton = new Button(buttonGroup, SWT.PUSH);
		clearButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		clearButton.setText(Messages.PeerPreferencePage_clear);
		clearButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				@SuppressWarnings("unchecked")
				Collection<PeerDefinition> peers = (Collection<PeerDefinition>) incomingViewer.getInput();
				Iterator<PeerDefinition> it = peers.iterator();
				while (it.hasNext()) {
					PeerDefinition peerDefinition = it.next();
					if (!peerDefinition.isBlocked())
						it.remove();
				}
				incomingViewer.setInput(peers);
			}
		});
		final Button blockButton = new Button(buttonGroup, SWT.PUSH);
		blockButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		blockButton.setText(Messages.PeerPreferencePage_toggle);
		blockButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				IStructuredSelection sel = incomingViewer.getStructuredSelection();
				Object firstElement = sel.getFirstElement();
				if (firstElement instanceof PeerDefinition) {
					PeerDefinition peerDefinition = (PeerDefinition) firstElement;
					if (peerDefinition.isBlocked())
						PeerActivator.getDefault().getBlockedNodes().remove(peerDefinition.getHost());
					else
						PeerActivator.getDefault().getBlockedNodes().add(peerDefinition.getHost());
					PeerActivator.getDefault().writeBlockedNodes();
					incomingViewer.setInput(incomingViewer.getInput());
					incomingViewer.setSelection(sel);
				}
			}
		});
		blockButton.setEnabled(false);
		incomingViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				blockButton.setEnabled(!event.getSelection().isEmpty());
			}
		});

		return comp;
	}

	@Override
	public void dispose() {
		PeerActivator.getDefault().removePeerListener(this);
		super.dispose();
	}

	@SuppressWarnings("unused")
	private Control createPeerGroup(CTabFolder parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout(1, false));
		Composite innerComp = new Composite(comp, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 20;
		innerComp.setLayout(layout);
		innerComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Label label = new Label(innerComp, SWT.WRAP);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		label.setText(Messages.PeerPreferencePage_network_geography);
		peerViewer = new TableViewer(innerComp, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
		peerViewer.getTable().setLinesVisible(true);
		peerViewer.getTable().setHeaderVisible(true);
		peerViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		TableViewerColumn col0 = new TableViewerColumn(peerViewer, SWT.NONE);
		col0.getColumn().setWidth(100);
		col0.getColumn().setText(Messages.PeerPreferencePage_nickname);
		col0.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof PeerDefinition) {
					String nick = ((PeerDefinition) element).getNickname();
					return nick == null ? "" : nick; //$NON-NLS-1$
				}
				return  ""; //$NON-NLS-1$
			}
		});
		EditingSupport editingSupport = new EditingSupport(peerViewer) {
			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof PeerDefinition && value instanceof PeerDefinition) {
					((PeerDefinition) element).setHost(((PeerDefinition) value).getHost());
					((PeerDefinition) element).setNickname(((PeerDefinition) value).getNickname());
					((PeerDefinition) element).setPort(((PeerDefinition) value).getPort());
					peerViewer.update(element, null);
				}
			}

			@Override
			protected Object getValue(Object element) {
				if (element instanceof PeerDefinition)
					return element;
				return null;
			}

			@Override
			protected CellEditor getCellEditor(final Object element) {
				if (element instanceof PeerDefinition) {
					return new DialogCellEditor(peerViewer.getTable()) {
						@Override
						protected Object openDialogBox(Control cellEditorWindow) {
							PeerDefinitionDialog dialog = new PeerDefinitionDialog(cellEditorWindow.getShell(),
									(PeerDefinition) element, true, false, false);
							if (dialog.open() == Window.OK)
								return dialog.getResult();
							return null;
						}
					};
				}
				return null;
			}

			@Override
			protected boolean canEdit(Object element) {
				return true;
			}
		};
		col0.setEditingSupport(editingSupport);

		TableViewerColumn col1 = new TableViewerColumn(peerViewer, SWT.NONE);
		col1.getColumn().setWidth(250);
		col1.getColumn().setText(Messages.PeerPreferencePage_peer_location);
		col1.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof PeerDefinition)
					return ((PeerDefinition) element).getLocation();
				return  ""; //$NON-NLS-1$
			}
		});
		col1.setEditingSupport(editingSupport);
		TableViewerColumn col2 = new TableViewerColumn(peerViewer, SWT.NONE);
		col2.getColumn().setWidth(150);
		col2.getColumn().setText(Messages.PeerPreferencePage_status);
		col2.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof PeerDefinition)
					return PeerActivator.getDefault().isOnline((PeerDefinition) element)
							? Messages.PeerPreferencePage_online
							: Messages.PeerPreferencePage_offline;
				return  ""; //$NON-NLS-1$
			}
		});
		peerViewer.setContentProvider(ArrayContentProvider.getInstance());
		new SortColumnManager(peerViewer, new int[] {SWT.UP, SWT.UP, SWT.NONE}, 0);
		peerViewer.setComparator(ZViewerComparator.INSTANCE);
		Composite buttonGroup = new Composite(innerComp, SWT.NONE);
		buttonGroup.setLayoutData(new GridData(SWT.END, SWT.BEGINNING, false, true));
		buttonGroup.setLayout(new GridLayout());
		Button addButton = new Button(buttonGroup, SWT.PUSH);
		addButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		addButton.setText(Messages.PeerPreferencePage_add);
		addButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				PeerDefinitionDialog dialog = new PeerDefinitionDialog(getShell(), null, true, false, false);
				if (dialog.open() == PeerDefinitionDialog.OK) {
					PeerDefinition newPeer = dialog.getResult();
					@SuppressWarnings("unchecked")
					List<PeerDefinition> peers = (List<PeerDefinition>) peerViewer.getInput();
					peers.add(newPeer);
					peerViewer.setInput(peers);
					peerViewer.setSelection(new StructuredSelection(newPeer));
				}
			}
		});
		final Button removeButton = new Button(buttonGroup, SWT.PUSH);
		removeButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		removeButton.setText(Messages.PeerPreferencePage_remove);
		removeButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				IStructuredSelection sel = peerViewer.getStructuredSelection();
				Object firstElement = sel.getFirstElement();
				if (firstElement instanceof PeerDefinition) {
					@SuppressWarnings("unchecked")
					List<PeerDefinition> peers = (List<PeerDefinition>) peerViewer.getInput();
					peers.remove(firstElement);
					peerViewer.setInput(peers);
				}
			}
		});
		removeButton.setEnabled(false);
		peerViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				removeButton.setEnabled(!event.getSelection().isEmpty());
			}
		});

		return comp;
	}

	@SuppressWarnings("unused")
	private Control createSharedGroup(CTabFolder parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout());
		Composite innerComp = new Composite(comp, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 20;
		innerComp.setLayout(layout);
		innerComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Label label = new Label(innerComp, SWT.WRAP);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		label.setText(Messages.PeerPreferencePage_shared_cats_msg);

		catViewer = new TreeViewer(innerComp, SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
		catViewer.getTree().setLinesVisible(true);
		catViewer.getTree().setHeaderVisible(true);
		catViewer.getTree().setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, false, true));
		TreeViewerColumn col1 = new TreeViewerColumn(catViewer, SWT.NONE);
		col1.getColumn().setWidth(400);
		col1.getColumn().setText(Messages.PeerPreferencePage_path);
		col1.setLabelProvider(ZColumnLabelProvider.getDefaultInstance());
		TreeViewerColumn col2 = new TreeViewerColumn(catViewer, SWT.NONE);
		col2.getColumn().setWidth(200);
		col2.getColumn().setText(Messages.PeerPreferencePage_privacy);
		col2.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof SharedCatalog)
					return translatePrivacy(((SharedCatalog) element).getPrivacy());
				if (element instanceof PeerDefinition)
					return ((PeerDefinition) element).getRightsLabel();
				return  ""; //$NON-NLS-1$
			}
		});
		col1.setEditingSupport(new EditingSupport(catViewer) {
			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof PeerDefinition && value instanceof PeerDefinition) {
					((PeerDefinition) element).setHost(((PeerDefinition) value).getHost());
					((PeerDefinition) element).setPort(((PeerDefinition) value).getPort());
					catViewer.update(element, null);
					validate();
				}
			}

			@Override
			protected Object getValue(Object element) {
				if (element instanceof PeerDefinition)
					return element;
				return null;
			}

			@Override
			protected CellEditor getCellEditor(final Object element) {
				if (element instanceof PeerDefinition) {
					return new DialogCellEditor(catViewer.getTree()) {
						@Override
						protected Object openDialogBox(Control cellEditorWindow) {
							PeerDefinitionDialog dialog = new PeerDefinitionDialog(cellEditorWindow.getShell(),
									(PeerDefinition) element, true, true, true);
							if (dialog.open() == Window.OK)
								return dialog.getResult();
							return null;
						}
					};
				}
				return null;
			}

			@Override
			protected boolean canEdit(Object element) {
				return (element instanceof PeerDefinition);
			}
		});
		col2.setEditingSupport(new EditingSupport(catViewer) {
			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof SharedCatalog && value instanceof Integer) {
					((SharedCatalog) element).setPrivacy((Integer) value);
					catViewer.update(element, null);
				} else if (element instanceof PeerDefinition && value instanceof PeerDefinition) {
					((PeerDefinition) element).setRights(((PeerDefinition) value).getRights());
					catViewer.update(element, null);
				}
			}

			@Override
			protected Object getValue(Object element) {
				if (element instanceof SharedCatalog)
					return ((SharedCatalog) element).getPrivacy();
				if (element instanceof PeerDefinition)
					return element;
				return null;
			}

			@Override
			protected CellEditor getCellEditor(final Object element) {
				if (element instanceof SharedCatalog) {
					ComboBoxViewerCellEditor editor = new ComboBoxViewerCellEditor(catViewer.getTree());
					editor.setLabelProvider(new LabelProvider() {
						@Override
						public String getText(Object element) {
							return translatePrivacy(((Integer) element));
						}
					});
					editor.setContentProvider(ArrayContentProvider.getInstance());
					editor.setInput(((SharedCatalog) element).getRestrictions().isEmpty()
							? new Integer[] { QueryField.SAFETY_RESTRICTED, QueryField.SAFETY_MODERATE,
									QueryField.SAFETY_SAFE, QueryField.SAFETY_LOCAL }
							: new Integer[] { QueryField.SAFETY_RESTRICTED, QueryField.SAFETY_MODERATE,
									QueryField.SAFETY_SAFE });
					return editor;
				}
				if (element instanceof PeerDefinition) {
					return new DialogCellEditor(catViewer.getTree()) {
						@Override
						protected Object openDialogBox(Control cellEditorWindow) {
							PeerDefinitionDialog dialog = new PeerDefinitionDialog(cellEditorWindow.getShell(),
									(PeerDefinition) element, false, true, false);
							if (dialog.open() == Window.OK)
								return dialog.getResult();
							return null;
						}

						@Override
						protected void updateContents(Object value) {
							if (value instanceof PeerDefinition)
								super.updateContents(((PeerDefinition) value).getRightsLabel());
						}
					};
				}
				return null;
			}

			@Override
			protected boolean canEdit(Object element) {
				return true;
			}
		});
		catViewer.setContentProvider(new ITreeContentProvider() {

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// do nothing
			}

			public void dispose() {
				// do nothing
			}

			public boolean hasChildren(Object element) {
				if (element instanceof SharedCatalog)
					return !((SharedCatalog) element).getRestrictions().isEmpty();
				return false;
			}

			public Object getParent(Object element) {
				if (element instanceof PeerDefinition)
					return ((PeerDefinition) element).getParent();
				return null;
			}

			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof Collection<?>)
					return ((Collection<?>) inputElement).toArray();
				return EMPTY;
			}

			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof SharedCatalog)
					return ((SharedCatalog) parentElement).getRestrictions().toArray();
				return EMPTY;
			}
		});
		new SortColumnManager(catViewer, new int[] {SWT.UP,  SWT.NONE} , 0);
		catViewer.setComparator(ZViewerComparator.INSTANCE);
		UiUtilities.installDoubleClickExpansion(catViewer);
		Composite buttonGroup = new Composite(innerComp, SWT.NONE);
		buttonGroup.setLayoutData(new GridData(SWT.END, SWT.BEGINNING, false, true));
		buttonGroup.setLayout(new GridLayout());
		addCurrentButton = new Button(buttonGroup, SWT.PUSH);
		addCurrentButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		addCurrentButton.setText(Messages.PeerPreferencePage_add_current);
		addCurrentButton.addListener(SWT.Selection, this);

		final Button addButton = new Button(buttonGroup, SWT.PUSH);
		addButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		addButton.setText(Messages.PeerPreferencePage_add);
		addButton.addListener(SWT.Selection, this);
		addRestrictionButton = new Button(buttonGroup, SWT.PUSH);
		addRestrictionButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		addRestrictionButton.setText(Messages.PeerPreferencePage_add_restriction);
		addRestrictionButton.addListener(SWT.Selection, this);
		removeCatButton = new Button(buttonGroup, SWT.PUSH);
		removeCatButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		removeCatButton.setText(Messages.PeerPreferencePage_remove);
		removeCatButton.addListener(SWT.Selection, this);
		catViewer.addSelectionChangedListener(this);
		return comp;
	}
	
	public void selectionChanged(SelectionChangedEvent event) {
		updateButtons();
	}

	@Override
	protected void doUpdateButtons() {
		ISelection selection = catViewer.getSelection();
		boolean enabled = !selection.isEmpty();
		removeCatButton.setEnabled(enabled);
		if (enabled) {
			Object el = ((IStructuredSelection) selection).getFirstElement();
			addRestrictionButton.setEnabled(
					(el instanceof SharedCatalog && ((SharedCatalog) el).getPrivacy() != QueryField.SAFETY_LOCAL));
		} else
			addRestrictionButton.setEnabled(false);
		boolean currentShared = false;
		File file = Core.getCore().getDbManager().getFile();
		if (file != null) {
			@SuppressWarnings("unchecked")
			List<SharedCatalog> catalogs = (List<SharedCatalog>) catViewer.getInput();
			for (SharedCatalog sharedCatalog : catalogs)
				if (sharedCatalog.getPath().equals(file)) {
					currentShared = true;
					break;
				}
		}
		addCurrentButton.setEnabled(!currentShared);
	}

	private static String translatePrivacy(int privacy) {
		switch (privacy) {
		case QueryField.SAFETY_LOCAL:
			return Messages.PeerPreferencePage_local;
		case QueryField.SAFETY_SAFE:
			return Messages.PeerPreferencePage_public_items;
		case QueryField.SAFETY_MODERATE:
			return Messages.PeerPreferencePage_medium;
		default:
			return Messages.PeerPreferencePage_all;
		}
	}

	@Override
	protected void doFillValues() {
		catViewer.setInput(PeerActivator.getDefault().getSharedCatalogs());
		catViewer.expandAll();
		peerViewer.setInput(PeerActivator.getDefault().getPeers());
		int port = PeerActivator.getDefault().getListeningPort();
		portField.setSelection(port);
		int oldPort = getPreferenceStore().getInt(PreferenceConstants.PORT);
		updateLabel.setText(port != oldPort ? NLS.bind(Messages.PeerPreferencePage_automatic_update, oldPort) : ""); //$NON-NLS-1$
		incomingViewer.setInput(new ArrayList<PeerDefinition>(PeerActivator.getDefault().getIncomingCalls().values()));
	}

	@Override
	protected void doPerformOk() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		StringBuilder sb = new StringBuilder();
		@SuppressWarnings("unchecked")
		List<SharedCatalog> catalogs = (List<SharedCatalog>) catViewer.getInput();
		for (SharedCatalog cat : catalogs) {
			if (sb.length() > 0)
				sb.append('\n');
			sb.append(cat).append('?').append(cat.getPrivacy());
			for (PeerDefinition restriction : cat.getRestrictions())
				sb.append('?').append(restriction.getHost()).append('=').append(restriction.getRights());
		}
		preferenceStore.setValue(PreferenceConstants.SHAREDCATALOGS, sb.toString());
		sb.setLength(0);
		@SuppressWarnings("unchecked")
		List<PeerDefinition> peers = (List<PeerDefinition>) peerViewer.getInput();
		for (PeerDefinition peer : peers) {
			if (sb.length() > 0)
				sb.append('\n');
			sb.append(peer);
		}
		preferenceStore.setValue(PreferenceConstants.PEERS, sb.toString());
		preferenceStore.setValue(PreferenceConstants.PORT, portField.getSelection());
		Map<String, PeerDefinition> incomingCalls = PeerActivator.getDefault().getIncomingCalls();
		incomingCalls.clear();
		@SuppressWarnings("unchecked")
		Collection<PeerDefinition> input = (Collection<PeerDefinition>) incomingViewer.getInput();
		for (PeerDefinition peerDefinition : input)
			incomingCalls.put(peerDefinition.getLocation(), peerDefinition);
		PeerActivator.getDefault().writeIncomingCalls();
	}

	public void statusChanged(final PeerDefinition peer, boolean online) {
		getControl().getDisplay().syncExec(() -> peerViewer.update(peer, null));
	}

	@Override
	public void handleEvent(Event e) {
		if (e.widget == addCurrentButton) {
			String catFile = Core.getCore().getDbManager().getFileName();
			SharedCatalog cat = new SharedCatalog(catFile, QueryField.SAFETY_RESTRICTED);
			@SuppressWarnings("unchecked")
			List<SharedCatalog> catalogs = (List<SharedCatalog>) catViewer.getInput();
			catalogs.add(cat);
			catViewer.setInput(catalogs);
			catViewer.setSelection(new StructuredSelection(cat));
			updateButtons();
		} else if (e.widget == addRestrictionButton) {
			IStructuredSelection selection = (IStructuredSelection) catViewer.getSelection();
			Object firstElement = selection.getFirstElement();
			if (firstElement instanceof SharedCatalog) {
				PeerDefinitionDialog dialog = new PeerDefinitionDialog(getControl().getShell(), null, true, true,
						true);
				if (dialog.open() == PeerDefinitionDialog.OK) {
					PeerDefinition result = dialog.getResult();
					result.setParent((SharedCatalog) firstElement);
					catViewer.setInput(catViewer.getInput());
					catViewer.expandToLevel(firstElement, 1);
					catViewer.setSelection(new StructuredSelection(result));
					validate();
				}
			}
		} else if (e.widget == removeCatButton) {
			IStructuredSelection sel = (IStructuredSelection) catViewer.getSelection();
			Object firstElement = sel.getFirstElement();
			if (firstElement instanceof SharedCatalog) {
				@SuppressWarnings("unchecked")
				List<SharedCatalog> catalogs = (List<SharedCatalog>) catViewer.getInput();
				catalogs.remove(firstElement);
				catViewer.setInput(catalogs);
				validate();
				updateButtons();
			}
		} else {
			FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
			dialog.setFileName(Core.getCore().getDbManager().getFileName());
			String filename = dialog.open();
			if (filename != null) {
				SharedCatalog cat = new SharedCatalog(filename, QueryField.SAFETY_RESTRICTED);
				@SuppressWarnings("unchecked")
				List<SharedCatalog> catalogs = (List<SharedCatalog>) catViewer.getInput();
				catalogs.add(cat);
				catViewer.setInput(catalogs);
				catViewer.setSelection(new StructuredSelection(cat));
			}
		}
		
	}

}
