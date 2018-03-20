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
 * (c) 2018 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.cat.model.group.Group;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.UiConstants;

@SuppressWarnings("restriction")
public class PurgeEmptyDialog extends ZTitleAreaDialog {

	public class PurgeJob extends Job {

		private boolean undo;

		public PurgeJob(boolean undo) {
			super(Messages.PurgeEmptyDialog_Removing_empty);
			this.undo = undo;
		}

		@Override
		public boolean belongsTo(Object family) {
			return family == PurgeEmptyDialog.this;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			Set<Object> deepSet = new HashSet<>();
			Set<Object> flatSet = new HashSet<>();
			if (!undo)
				for (SmartCollection sm : collections) {
					String id = sm.getStringId();
					if (!allDeleted.contains(id)) {
						updateViewer(pending = sm);
						if (dbManager.pruneSystemCollection(sm)) {
							deepSet.clear();
							flatSet.clear();
							Utilities.deleteCollection(sm, false, flatSet);
							Utilities.deleteCollection(sm, true, deepSet);
							for (Object obj : deepSet)
								if (obj instanceof SmartCollection) {
									SmartCollection sm2 = (SmartCollection) obj;
									allDeleted.add(sm2.getStringId());
									backup.add(sm2);
								}
							deepSet.removeAll(flatSet);
							dbManager.safeTransaction(deepSet, null);
						} else
							kept.add(id);
					}
					updateViewer(sm);
					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;
				}
			else
				for (SmartCollection sm : backup) {
					flatSet.clear();
					Utilities.storeCollection(sm, false, flatSet);
					String groupId = sm.getGroup_rootCollection_parent();
					if (groupId != null) {
						Group group = dbManager.obtainById(Group.class, groupId);
						if (group == null)
							continue;
						if (!group.getRootCollection().contains(sm.getStringId())) {
							group.addRootCollection(sm.getStringId());
							flatSet.add(group);
						}
					} else {
						SmartCollection parent = sm.getSmartCollection_subSelection_parent();
						if (parent == null)
							continue;
						if (!parent.getSubSelection().contains(sm)) {
							parent.addSubSelection(sm);
							flatSet.add(parent);
						}
					}
					dbManager.safeTransaction(null, flatSet);
					allDeleted.remove(sm.getStringId());
					updateViewer(sm);
				}
			pending = null;
			return Status.OK_STATUS;
		}

		private void updateViewer(SmartCollection sm) {
			getShell().getDisplay().asyncExec(() -> {
				if (!viewer.getControl().isDisposed()) {
					viewer.update(sm, null);
					viewer.setSelection(new StructuredSelection(sm), true);
				}
			});
		}
	}

	private static final int UNDO = 999;

	private List<SmartCollection> collections;
	private SmartCollection pending;
	protected Set<String> allDeleted = new HashSet<>();
	protected Set<String> kept = new HashSet<>();
	protected List<SmartCollection> backup = new ArrayList<>();
	private TableViewer viewer;

	public PurgeEmptyDialog(Shell parentShell, Group group) {
		super(parentShell);
		compileCollections(group, collections = new ArrayList<>(50));
	}

	protected void compileCollections(Group group, List<SmartCollection> collections) {
		List<String> rootCollection = group.getRootCollection();
		if (rootCollection != null)
			for (String id : rootCollection) {
				SmartCollection sm = Core.getCore().getDbManager().obtainById(SmartCollection.class, id);
				if (sm != null)
					compileCollections(sm, collections);
			}
		List<Group> subgroup = group.getSubgroup();
		if (subgroup != null)
			for (Group sub : subgroup)
				compileCollections(sub, collections);
		Collections.sort(collections, new Comparator<SmartCollection>() {
			@Override
			public int compare(SmartCollection o1, SmartCollection o2) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		});
	}

	private void compileCollections(SmartCollection sm, List<SmartCollection> collections) {
		collections.add(sm);
		List<SmartCollection> subSelection = sm.getSubSelection();
		if (subSelection != null)
			for (SmartCollection sub : subSelection)
				compileCollections(sub, collections);
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.PurgeEmptyDialog_Removing_empty);
		setMessage(Messages.PurgeEmptyDialog_please_wait);
		run(false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout());
		viewer = new TableViewer(composite, SWT.BORDER | SWT.V_SCROLL);
		viewer.getTable().setLayoutData(new GridData(350, 500));
		viewer.getTable().setLinesVisible(true);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		TableViewerColumn col1 = new TableViewerColumn(viewer, SWT.NONE);
		col1.getColumn().setWidth(250);
		col1.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof SmartCollection)
					return ((SmartCollection) element).getName();
				return super.getText(element);
			}
		});
		TableViewerColumn col2 = new TableViewerColumn(viewer, SWT.NONE);
		col2.getColumn().setWidth(250);
		col2.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof SmartCollection) {
					if (element == pending)
						return Messages.PurgeEmptyDialog_analyzing;
					if (allDeleted.contains(((SmartCollection) element).getStringId()))
						return Messages.PurgeEmptyDialog_deleted;
					if (kept.contains(((SmartCollection) element).getStringId()))
						return Messages.PurgeEmptyDialog_kept;
					return ""; //$NON-NLS-1$
				}
				return super.getText(element);
			}
			
			@Override
			protected Font getFont(Object element) {
				if (allDeleted.contains(((SmartCollection) element).getStringId()))
					return JFaceResources.getFont(UiConstants.SELECTIONFONT);
				return super.getFont(element);
			}
		});
		viewer.setInput(collections);
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, UNDO, Messages.PurgeEmptyDialog_undo, false).setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	protected void run(boolean undo) {
		getButton(CANCEL).setEnabled(!undo);
		PurgeJob purgeJob = new PurgeJob(undo);
		purgeJob.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				Shell shell = getShell();
				if (!shell.isDisposed())
					shell.getDisplay().syncExec(() -> {
						if (!undo && allDeleted.isEmpty())
							okPressed();
						Button cancelButton = getButton(CANCEL);
						cancelButton.setText(Messages.PurgeEmptyDialog_finish);
						cancelButton.setEnabled(true);
						getButton(UNDO).setEnabled(!undo);
					});
			}
		});
		purgeJob.schedule();
	}

	@Override
	protected void cancelPressed() {
		if (getButton(CANCEL).getText().equals(Messages.PurgeEmptyDialog_finish))
			okPressed();
		Job.getJobManager().cancel(this);
	}

	@Override
	protected void okPressed() {
		if (!allDeleted.isEmpty())
			Core.getCore().fireStructureModified();
		super.okPressed();
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == UNDO)
			run(true);
		else
			super.buttonPressed(buttonId);
	}

}
