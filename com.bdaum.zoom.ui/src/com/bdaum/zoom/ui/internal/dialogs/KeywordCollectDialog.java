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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.fileMonitor.internal.filefilter.FilterChain;
import com.bdaum.zoom.ui.internal.ZViewerComparator;

public class KeywordCollectDialog extends ZProgressDialog implements ICheckStateListener, Listener {

	private class CollectJob extends Job {

		public CollectJob() {
			super(Messages.KeywordCollectDialog_collecting);
			setPriority(Job.INTERACTIVE);
			setSystem(true);
		}

		@Override
		public boolean belongsTo(Object family) {
			return family == KeywordCollectDialog.this;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			final Shell shell = getShell();
			FilterChain keywordFilter = QueryField.getKeywordFilter();
			candidates = new HashSet<String>();
			final List<AssetImpl> set = dbManager.obtainObjects(AssetImpl.class);
			if (!shell.isDisposed())
				shell.getDisplay().syncExec(() -> setMinMax(0, Math.max(1, set.size())));
			int i = 0;
			for (AssetImpl asset : set) {
				String[] list = asset.getKeyword();
				if (list != null)
					for (String kw : keywordFilter.filter(list)) {
						if (!keywords.contains(kw))
							candidates.add(kw);
						else
							unused.remove(kw);
					}
				if (++i % 10 == 0) {
					if (!shell.isDisposed()) {
						final int p = i;
						shell.getDisplay().syncExec(() -> progressBar.setSelection(p));
					}
				}
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
			}
			if (!shell.isDisposed()) {
				shell.getDisplay().syncExec(() -> {
					candLabel.setText(NLS.bind(Messages.KeywordCollectDialog_candidates_x, candidates.size()));
					unusedLabel.setText(NLS.bind(Messages.KeywordCollectDialog_unused_x, unused.size()));
					viewer1.setInput(candidates);
					viewer2.setInput(unused);
					setMessage(Messages.KeywordCollectDialog_please_select_keywords);
					validate();
					progressBar.setVisible(false);
					shell.pack();
					Point size = shell.getSize();
					shell.setSize(size.x, Math.min(size.y, 700));
					shell.layout(true, true);
				});
			}
			return Status.OK_STATUS;
		}

	}

	private final Set<String> keywords;
	private CheckboxTableViewer viewer1;
	private String[] toAdd;
	private Set<String> candidates;
	private HashSet<String> unused;
	private CheckboxTableViewer viewer2;
	private String[] toRemove;
	private Label candLabel;
	private Label unusedLabel;

	public KeywordCollectDialog(Shell parentShell, Set<String> keywords) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.keywords = keywords;
		this.unused = new HashSet<>(keywords);
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.KeywordCollectDialog_collected_keywords);
		setMessage(Messages.KeywordCollectDialog_collecting_keywords);
		BusyIndicator.showWhile(getShell().getDisplay(), () -> new CollectJob().schedule());
	}

	private void validate() {
		String errorMessage = null;
		if (unused.isEmpty() && candidates.isEmpty())
			errorMessage = Messages.KeywordCollectDialog_nothing_to_select_from;
		else if (viewer1.getCheckedElements().length == 0 && viewer2.getCheckedElements().length == 0)
			errorMessage = Messages.KeywordCollectDialog_nothing_selected;
		setErrorMessage(errorMessage);
		boolean enabled = errorMessage == null;
		getShell().setModified(enabled);
		getButton(IDialogConstants.OK_ID).setEnabled(enabled);
	}

	@SuppressWarnings("unused")
	@Override
	protected void createCustomArea(Composite area) {
		area.setLayout(new GridLayout(1, true));
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, true));
		candLabel = new Label(composite, SWT.NONE);
		candLabel.setText(Messages.KeywordCollectDialog_candidates);
		unusedLabel = new Label(composite, SWT.NONE);
		unusedLabel.setText(Messages.KeywordCollectDialog_unused);
		Composite viewerComp1 = new Composite(composite, SWT.NONE);
		viewerComp1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginWidth = 0;
		viewerComp1.setLayout(layout);
		viewer1 = CheckboxTableViewer.newCheckList(viewerComp1, SWT.BORDER | SWT.V_SCROLL);
		viewer1.getTable().setLayoutData(new GridData(500, 300));
		viewer1.setContentProvider(ArrayContentProvider.getInstance());
		viewer1.setLabelProvider(ZColumnLabelProvider.getDefaultInstance());
		viewer1.setComparator(ZViewerComparator.INSTANCE);
		viewer1.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer1.addCheckStateListener(this);
		new AllNoneGroup(viewerComp1, this);
		Composite viewerComp2 = new Composite(composite, SWT.NONE);
		viewerComp2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginWidth = 0;
		viewerComp2.setLayout(layout);
		viewer2 = CheckboxTableViewer.newCheckList(viewerComp2, SWT.BORDER | SWT.V_SCROLL);
		viewer2.getTable().setLayoutData(new GridData(500, 300));
		viewer2.setContentProvider(ArrayContentProvider.getInstance());
		viewer2.setLabelProvider(ZColumnLabelProvider.getDefaultInstance());
		viewer2.setComparator(ZViewerComparator.INSTANCE);
		viewer2.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer2.addCheckStateListener(this);
		new AllNoneGroup(viewerComp2, new Listener() {
			@Override
			public void handleEvent(Event e) {
				viewer2.setAllChecked(e.widget.getData() == AllNoneGroup.ALL);
				validate();
			}
		});
	}
	
	@Override
	public void handleEvent(Event e) {
		viewer1.setAllChecked(e.widget.getData() == AllNoneGroup.ALL);
		validate();
	}

	@Override
	protected void buttonPressed(int buttonId) {
		Job.getJobManager().cancel(this);
		super.buttonPressed(buttonId);
	}

	@Override
	protected void okPressed() {
		toAdd = (String[]) viewer1.getCheckedElements();
		toRemove = (String[]) viewer2.getCheckedElements();
		super.okPressed();
	}

	public String[] getToAdd() {
		return toAdd;
	}

	public String[] getToRemove() {
		return toRemove;
	}

	@Override
	public void checkStateChanged(CheckStateChangedEvent event) {
		validate();
	}

}
