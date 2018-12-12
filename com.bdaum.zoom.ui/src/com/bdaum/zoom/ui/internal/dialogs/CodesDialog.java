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
 * (c) 2011-2017 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.dialogs;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnViewer;
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
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.SortColumnManager;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.ZViewerComparator;
import com.bdaum.zoom.ui.internal.codes.CodeParser;
import com.bdaum.zoom.ui.internal.codes.Topic;
import com.bdaum.zoom.ui.internal.views.ZColumnViewerToolTipSupport;
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
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (element instanceof Topic)
				return ((Topic) element).isShown(filterField.getFilter(), deep, filterExclusions);
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
			if (element instanceof Topic)
				return ((Topic) element).getParent();
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

	}

	private String result;
	private ColumnViewer topicViewer;
	private final String dflt;
	private TableViewer recentViewer;
	private final CodeParser parser;
	private FilterField filterField;
	protected Topic selectedTopic;
	private final Set<String> exclusions;
	private Combo customField;

	private IDoubleClickListener doubleClickListener = new IDoubleClickListener() {
		public void doubleClick(DoubleClickEvent event) {
			okPressed();
		}
	};

	private ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent event) {
			IStructuredSelection selection = (IStructuredSelection) event.getSelection();
			if (!selection.isEmpty())
				selectedTopic = ((Topic) selection.getFirstElement());
			updateFields();
			updateButtons();
		}
	};

	public CodesDialog(Shell parentShell, QueryField qfield, String dflt, String[] exclusions) {
		super(parentShell);
		this.parser = UiActivator.getDefault().getCodeParser((Integer) qfield.getEnumeration());
		this.dflt = dflt;
		this.exclusions = exclusions == null ? null : new HashSet<String>(Arrays.asList(exclusions));
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
		composite.setLayout(new GridLayout(2, false));
		filterField = new FilterField(composite);
		filterField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		filterField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateViewers();
			}
		});
		if (parser.hasRecentTopics()) {
			Label recentLabel = new Label(composite, SWT.NONE);
			recentLabel.setText(Messages.CodesDialog_recent_topics);
			recentLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
			createRecentViewer(composite);
		}
		new Label(composite, SWT.NONE).setText(Messages.CodesDialog_matching_topics);
		createTopicViewer(composite);
		if (parser.isAllowCustomCode()) {
			Label label = new Label(composite, SWT.NONE);
			label.setText(Messages.CodesDialog_custom_code);
			customField = new Combo(composite, SWT.BORDER);
			customField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			customField.setItems(parser.getCustomTopics());
			customField.setText(""); //$NON-NLS-1$
			customField.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					updateButtons();
				}
			});
		}
		topicViewer.getControl().setFocus();
		return area;
	}

	protected void updateViewers() {
		if (recentViewer != null) {
			ISelection selection = recentViewer.getSelection();
			recentViewer.setInput(recentViewer.getInput());
			recentViewer.setSelection(selection);
		}
		ISelection selection = topicViewer.getSelection();
		topicViewer.setInput(topicViewer.getInput());
		if (topicViewer instanceof TreeViewer && filterField.getFilter() != null)
			((TreeViewer) topicViewer).expandAll();
		topicViewer.setSelection(selection);
	}

	@SuppressWarnings("unused")
	private void createRecentViewer(Composite composite) {
		recentViewer = new TableViewer(composite, SWT.SINGLE | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		layoutData.heightHint = 100;
		recentViewer.getTable().setLayoutData(layoutData);
		recentViewer.getTable().setLinesVisible(true);
		recentViewer.getTable().setHeaderVisible(true);
		if (!parser.isByName())
			createTableColumn(recentViewer, 120, Messages.CodesDialog_code, 0);
		createTableColumn(recentViewer, 150, Messages.CodesDialog_name, 1);
		createTableColumn(recentViewer, 300, Messages.CodesDialog_explanation, 2);
		recentViewer.setContentProvider(ArrayContentProvider.getInstance());
		if (parser.isByName())
			new SortColumnManager(recentViewer, new int[] { SWT.UP, SWT.UP }, 0);
		else
			new SortColumnManager(recentViewer, new int[] { SWT.UP, SWT.UP, SWT.UP }, 1);
		recentViewer.setComparator(ZViewerComparator.INSTANCE);
		recentViewer.setFilters(new ViewerFilter[] { new TopicFilter(false, exclusions) });
		recentViewer.addSelectionChangedListener(selectionChangedListener);
		recentViewer.addDoubleClickListener(doubleClickListener);
		ZColumnViewerToolTipSupport.enableFor(recentViewer);
	}

	@SuppressWarnings("unused")
	private void createTopicViewer(Composite composite) {
		ExpandCollapseGroup expandCollapseGroup = null;
		if (parser.hasSubtopics()) {
			expandCollapseGroup = new ExpandCollapseGroup(composite, SWT.NONE);
			TreeViewer treeViewer = new TreeViewer(composite,
					SWT.SINGLE | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
			expandCollapseGroup.setViewer((TreeViewer) topicViewer);
			treeViewer.getTree().setLinesVisible(true);
			treeViewer.getTree().setHeaderVisible(true);
			if (!parser.isByName())
				createTreeColumn(treeViewer, 120, Messages.CodesDialog_code, 0);
			createTreeColumn(treeViewer, 150, Messages.CodesDialog_name, 1);
			createTreeColumn(treeViewer, 300, Messages.CodesDialog_explanation, 2);
			UiUtilities.installDoubleClickExpansion(treeViewer);
			topicViewer = treeViewer;
		} else {
			new Label(composite, SWT.NONE);
			TableViewer tableViewer = new TableViewer(composite,
					SWT.SINGLE | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
			tableViewer.getTable().setLinesVisible(true);
			tableViewer.getTable().setHeaderVisible(true);
			if (!parser.isByName())
				createTableColumn(tableViewer, 120, Messages.CodesDialog_code, 0);
			createTableColumn(tableViewer, 150, Messages.CodesDialog_name, 1);
			createTableColumn(tableViewer, 300, Messages.CodesDialog_explanation, 2);
			tableViewer.addDoubleClickListener(doubleClickListener);
			topicViewer = tableViewer;
		}
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		layoutData.heightHint = 300;
		topicViewer.getControl().setLayoutData(layoutData);
		topicViewer.setContentProvider(new TopicContentProvider());
		if (parser.isByName())
			new SortColumnManager(recentViewer, new int[] { SWT.UP, SWT.UP }, 0);
		else
			new SortColumnManager(recentViewer, new int[] { SWT.UP, SWT.UP, SWT.UP }, 1);
		topicViewer.setComparator(ZViewerComparator.INSTANCE);
		topicViewer.setFilters(new ViewerFilter[] { new TopicFilter(true, exclusions) });
		topicViewer.addSelectionChangedListener(selectionChangedListener);
		ZColumnViewerToolTipSupport.enableFor(topicViewer);
	}

	private void createTreeColumn(TreeViewer treeViewer, int width, String text, int index) {
		TreeViewerColumn col = new TreeViewerColumn(treeViewer, SWT.NONE);
		col.getColumn().setWidth(width);
		col.getColumn().setText(text);
		col.setLabelProvider(new TopicLabelProvider(index));
	}

	private void createTableColumn(TableViewer tableViewer, int width, String text, int index) {
		TableViewerColumn col = new TableViewerColumn(tableViewer, SWT.NONE);
		col.getColumn().setWidth(width);
		col.getColumn().setText(text);
		col.setLabelProvider(new TopicLabelProvider(index));
	}

	private void fillValues() {
		topicViewer.setInput(parser.loadCodes());
		if (dflt != null && !dflt.isEmpty()) {
			selectedTopic = parser.findTopic(dflt);
			if (selectedTopic != null)
				topicViewer.setSelection(new StructuredSelection(selectedTopic), true);
			else
				customField.setText(dflt);
		}
		if (recentViewer != null) {
			recentViewer.setInput(parser.getRecentTopics());
			if (selectedTopic != null)
				recentViewer.setSelection(new StructuredSelection(selectedTopic), true);
		}
	}

	@Override
	protected void okPressed() {
		if (customField != null)
			result = customField.getText();
		if ((result == null || result.isEmpty()) && selectedTopic != null)
			result = selectedTopic.getCode();
		if (result == null)
			result = ""; //$NON-NLS-1$
		if (!result.isEmpty() && parser.findTopic(result) == null)
			parser.addCustomTopic(result);
		super.okPressed();
	}

	private void updateButtons() {
		getButton(OK).setEnabled(selectedTopic != null || customField != null && !customField.getText().isEmpty());
	}

	private void updateFields() {
		if (selectedTopic != null && customField != null)
			customField.setText(selectedTopic.getCode());
	}

	public String getResult() {
		return result;
	}

}
