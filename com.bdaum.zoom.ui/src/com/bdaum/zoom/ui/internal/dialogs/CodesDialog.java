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
 * (c) 2011-2013 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ui.internal.dialogs;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.fileMonitor.internal.filefilter.WildCardFilter;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.codes.CodeParser;
import com.bdaum.zoom.ui.internal.codes.Topic;
import com.bdaum.zoom.ui.internal.widgets.ExpandCollapseGroup;
import com.bdaum.zoom.ui.internal.widgets.FilterField;

public class CodesDialog extends ZTitleAreaDialog {
	private static final Topic[] EMPTYTOPICS = new Topic[0];

	private final class TopicFilter extends ViewerFilter {
		private final boolean deep;
		private final Set<String> filterExclusions;

		public TopicFilter(boolean deep, Set<String> exclusions) {
			this.deep = deep;
			this.filterExclusions = exclusions;
		}

		@Override
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			if (element instanceof Topic)
				return ((Topic) element).isShown(filterField.getFilter(), deep,
						filterExclusions);
			return false;
		}
	}

	public class TopicContentProvider implements ITreeContentProvider {

		public void dispose() {
			// do nothing
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// do nothing
		}

		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof Topic[])
				return (Topic[]) inputElement;
			return EMPTYTOPICS;
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof Topic) {
				List<Topic> subTopics = ((Topic) parentElement).getSubTopics();
				if (subTopics != null)
					return subTopics.toArray();
			}
			return EMPTYTOPICS;
		}

		public Object getParent(Object element) {
			if (element instanceof Topic) {
				return ((Topic) element).getParent();
			}
			return null;
		}

		public boolean hasChildren(Object element) {
			if (element instanceof Topic)
				return ((Topic) element).getSubTopics() != null;
			return false;
		}

	}

	public class TopicLabelProvider extends ZColumnLabelProvider {

		private final int column;

		public TopicLabelProvider(int column) {
			this.column = column;
		}

		@Override
		public String getText(Object element) {
			if (element instanceof Topic) {
				switch (column) {
				case 0:
					return ((Topic) element).getCode();
				case 2:
					return ((Topic) element).getDescription();
				}
			}
			return element.toString();
		}

		@Override
		public Font getFont(Object element) {
			if (element instanceof Topic) {
				WildCardFilter filter = filterField.getFilter();
				if (filter != null
						&& filter.accept(((Topic) element).getName()))
					return JFaceResources.getBannerFont();
			}
			return super.getFont(element);
		}

	}

	private String result;
	private TreeViewer topicViewer;
	private final String dflt;
	private TableViewer recentViewer;
	private final CodeParser parser;
	private FilterField filterField;
	protected Topic selectedTopic;
	private final Set<String> exclusions;

	private IDoubleClickListener doubleClickListener = new IDoubleClickListener() {

		public void doubleClick(DoubleClickEvent event) {
			okPressed();
		}
	};

	private ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {

		public void selectionChanged(SelectionChangedEvent event) {
			IStructuredSelection selection = (IStructuredSelection) event
					.getSelection();
			if (!selection.isEmpty())
				selectedTopic = ((Topic) selection.getFirstElement());
			updateButtons();
		}
	};

	private ViewerComparator topicComparator = new ViewerComparator() {
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			if (e1 instanceof Topic && e2 instanceof Topic)
				return ((Topic) e1).getName().compareTo(((Topic) e2).getName());
			return super.compare(viewer, e1, e2);
		}
	};

	public CodesDialog(Shell parentShell, CodeParser parser, String dflt,
			String[] exclusions) {
		super(parentShell);
		this.parser = parser;
		this.dflt = dflt;
		this.exclusions = exclusions == null ? null : new HashSet<String>(
				Arrays.asList(exclusions));
	}

	@Override
	public void create() {
		super.create();
		BusyIndicator.showWhile(getShell().getDisplay(), () -> {
			fillValues();
			updateButtons();
			setTitle(parser.getTitle());
			setMessage(parser.getMessage());
		});
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(1, false));
		filterField = new FilterField(composite);
		filterField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateViewers();
			}
		});
		new Label(composite, SWT.NONE)
				.setText(Messages.CodesDialog_recent_topics);
		createRecentViewer(composite);
		new Label(composite, SWT.NONE)
				.setText(Messages.CodesDialog_matching_topics);
		createTopicViewer(composite);
		topicViewer.getControl().setFocus();
		return area;
	}

	protected void updateViewers() {
		ISelection selection = recentViewer.getSelection();
		recentViewer.setInput(recentViewer.getInput());
		recentViewer.setSelection(selection);
		selection = topicViewer.getSelection();
		topicViewer.setInput(topicViewer.getInput());
		if (filterField.getFilter() != null)
			topicViewer.expandAll();
		topicViewer.setSelection(selection);
	}

	private void createRecentViewer(Composite composite) {
		recentViewer = new TableViewer(composite, SWT.SINGLE | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.BORDER);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.heightHint = 150;
		recentViewer.getTable().setLayoutData(layoutData);
		recentViewer.getTable().setLinesVisible(true);
		recentViewer.getTable().setHeaderVisible(true);
		TableViewerColumn col1 = new TableViewerColumn(recentViewer, SWT.NONE);
		col1.getColumn().setWidth(80);
		col1.getColumn().setText(Messages.CodesDialog_code);
		col1.setLabelProvider(new TopicLabelProvider(0));
		TableViewerColumn col2 = new TableViewerColumn(recentViewer, SWT.NONE);
		col2.getColumn().setWidth(150);
		col2.getColumn().setText(Messages.CodesDialog_name);
		col2.setLabelProvider(new TopicLabelProvider(1));
		TableViewerColumn col3 = new TableViewerColumn(recentViewer, SWT.NONE);
		col3.getColumn().setWidth(300);
		col3.getColumn().setText(Messages.CodesDialog_explanation);
		col3.setLabelProvider(new TopicLabelProvider(2));
		recentViewer.setContentProvider(ArrayContentProvider.getInstance());
		recentViewer.setComparator(topicComparator);
		recentViewer.setFilters(new ViewerFilter[] { new TopicFilter(false,
				exclusions) });
		recentViewer.addSelectionChangedListener(selectionChangedListener);
		recentViewer.addDoubleClickListener(doubleClickListener);
	}

	private void createTopicViewer(Composite composite) {
		ExpandCollapseGroup expandCollapseGroup = new ExpandCollapseGroup(composite, SWT.NONE);
		topicViewer = new TreeViewer(composite, SWT.SINGLE | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.BORDER);
		expandCollapseGroup.setViewer(topicViewer);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.heightHint = 300;
		topicViewer.getTree().setLayoutData(layoutData);
		topicViewer.getTree().setLinesVisible(true);
		topicViewer.getTree().setHeaderVisible(true);
		TreeViewerColumn col1 = new TreeViewerColumn(topicViewer, SWT.NONE);
		col1.getColumn().setWidth(120);
		col1.getColumn().setText(Messages.CodesDialog_code);
		col1.setLabelProvider(new TopicLabelProvider(0));
		TreeViewerColumn col2 = new TreeViewerColumn(topicViewer, SWT.NONE);
		col2.getColumn().setWidth(150);
		col2.getColumn().setText(Messages.CodesDialog_name);
		col2.setLabelProvider(new TopicLabelProvider(1));
		TreeViewerColumn col3 = new TreeViewerColumn(topicViewer, SWT.NONE);
		col3.getColumn().setWidth(300);
		col3.getColumn().setText(Messages.CodesDialog_explanation);
		col3.setLabelProvider(new TopicLabelProvider(2));
		topicViewer.setContentProvider(new TopicContentProvider());
		topicViewer.setComparator(topicComparator);
		topicViewer.setFilters(new ViewerFilter[] { new TopicFilter(true,
				exclusions) });
		topicViewer.addSelectionChangedListener(selectionChangedListener);
		topicViewer.addDoubleClickListener(doubleClickListener);
	}

	private void fillValues() {
		topicViewer.setInput(parser.loadCodes());
		if (dflt != null && !dflt.isEmpty()) {
			selectedTopic = parser.findTopic(dflt);
			if (selectedTopic != null)
				topicViewer.setSelection(
						new StructuredSelection(selectedTopic), true);
		}
		recentViewer.setInput(parser.getRecentTopics());
		if (selectedTopic != null)
			recentViewer.setSelection(new StructuredSelection(selectedTopic),
					true);
	}

	@Override
	protected void okPressed() {
		result = selectedTopic == null ? "" : selectedTopic.getCode(); //$NON-NLS-1$
		if (selectedTopic != null)
			parser.addRecentTopic(selectedTopic);
		super.okPressed();
	}

	private void updateButtons() {
		getButton(OK).setEnabled(selectedTopic != null);
	}

	public String getResult() {
		return result;
	}

}
