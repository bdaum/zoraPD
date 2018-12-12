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
 * (c) 2017 Berthold Daum  
 */
package com.bdaum.zoom.report.internal.wizards;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.cat.model.group.Group;
import com.bdaum.zoom.cat.model.group.GroupImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.report.Report;
import com.bdaum.zoom.cat.model.report.ReportImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.ISpellCheckingService;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.report.internal.HelpContextIds;
import com.bdaum.zoom.ui.dialogs.ZListDialog;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.ZViewerComparator;
import com.bdaum.zoom.ui.internal.views.CatalogLabelProvider;
import com.bdaum.zoom.ui.internal.views.CatalogView.CatalogComparator;
import com.bdaum.zoom.ui.internal.views.IdentifiedElementComparer;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.CheckedText;
import com.bdaum.zoom.ui.internal.widgets.RadioButtonGroup;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.wizards.ColoredWizardPage;

@SuppressWarnings("restriction")
public class SourcePage extends ColoredWizardPage implements Listener {

	public class ReportSelectionDialog extends ZListDialog {

		private static final int DELETE = 9999;

		public ReportSelectionDialog(Shell parent, int style) {
			super(parent, style);
		}
		
		@Override
		public void create() {
			setContentProvider(ArrayContentProvider.getInstance());
			setLabelProvider(new ZColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					if (element instanceof Report)
						return ((Report)element).getName();
					return element.toString();
				}
			});
			super.create();
			setComparator(ZViewerComparator.INSTANCE);
			getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					 updateButtons();
				}
			});
			updateButtons();
		}
		
		private void updateButtons() {
			boolean enabled = !getTableViewer().getSelection().isEmpty();
			getButton(DELETE).setEnabled(enabled);
			getButton(OK).setEnabled(enabled);
		}

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, DELETE, Messages.SourcePage_delete, false);
			super.createButtonsForButtonBar(parent);
		}
		
		@Override
		protected void buttonPressed(int buttonId) {
			if (buttonId == DELETE) {
				Object firstElement = getTableViewer().getStructuredSelection().getFirstElement();
				if (firstElement != null) {
					Core.getCore().getDbManager().safeTransaction(firstElement, null);
					getTableViewer().remove(firstElement);
					updateButtons();
				}
				return;
			}
			super.buttonPressed(buttonId);
		}

	}

	private TreeViewer collViewer;
	private static final ViewerFilter collectionFilter = new ViewerFilter() {
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			return element instanceof Group || element instanceof SmartCollection;
		}
	};
	private Text nameField;
	private CheckedText descriptionField;
	private Report report;
	private RadioButtonGroup sourceButtonGroup;
	private List<ReportImpl> reports;
	private Button browseButton;
	private CheckboxButton skipOrphansButton;

	public SourcePage(String id, String title, String msg, ImageDescriptor imageDescriptor) {
		super(id, title, imageDescriptor);
		setMessage(msg);
	}

	@SuppressWarnings({ "unused" })
	@Override
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 3);
		setControl(composite);
		setHelp(HelpContextIds.REPORT_WIZARD);
		new Label(composite, SWT.NONE).setText(Messages.SourcePage_name);
		nameField = new Text(composite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		nameField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		nameField.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				report.setName(nameField.getText());
			}
		});
		browseButton = new Button(composite, SWT.PUSH);
		browseButton.setText(Messages.SourcePage_browse);
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ReportSelectionDialog dialog = new ReportSelectionDialog(getShell(), SWT.SINGLE);
				dialog.setInput(reports);
				if (dialog.open() == ReportSelectionDialog.OK) {
					Object[] result = dialog.getResult();
					if (result != null && result.length > 0) {
						((ReportWizard)getWizard()).setReport((Report) result[0]);
						fillValues();
						checkExistingReports();
					}
				}
			}
		});
		
		new Label(composite, SWT.NONE).setText(Messages.SourcePage_description);
		descriptionField = new CheckedText(composite, SWT.MULTI | SWT.LEAD | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL,
				ISpellCheckingService.DESCRIPTIONOPTIONS);
		descriptionField.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				report.setDescription(descriptionField.getText());
			}
		});
		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		layoutData.heightHint = 100;
		descriptionField.setLayoutData(layoutData);
		Composite sourceComp = new Composite(composite, SWT.NONE);
		sourceComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		sourceComp.setLayout(new GridLayout(1, false));
		sourceButtonGroup = new RadioButtonGroup(sourceComp, null, SWT.NONE, Messages.SourcePage_all, Messages.SourcePage_collection);
		sourceButtonGroup.addListener(this);
		sourceButtonGroup.setSelection(1);
		new Label(sourceComp, SWT.NONE);
		collViewer = new TreeViewer(sourceComp, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		collViewer.setContentProvider(new CatalogContentProvider());
		collViewer.setLabelProvider(new CatalogLabelProvider(this));
		collViewer.setFilters(new ViewerFilter[] { collectionFilter });
		collViewer.setComparator(new CatalogComparator());
		UiUtilities.installDoubleClickExpansion(collViewer);
		setComparer();
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.widthHint = 300;
		layoutData.heightHint = 400;
		collViewer.getControl().setLayoutData(layoutData);
		collViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				validatePage();
			}
		});
		skipOrphansButton = WidgetFactory.createCheckButton(sourceComp, Messages.SourcePage_skip_orphans, null);
		setInput();
		updateFields();
		checkExistingReports();
		super.createControl(parent);
	}

	protected void checkExistingReports() {
		reports = Core.getCore().getDbManager().obtainObjects(ReportImpl.class);
		browseButton.setEnabled(!reports.isEmpty());
	}

	private void setInput() {
		collViewer.setInput(GroupImpl.class);
	}

	protected void setComparer() {
		collViewer.setComparer(IdentifiedElementComparer.getInstance());
	}

	private void updateFields() {
		collViewer.getControl().setVisible(sourceButtonGroup.getSelection() == 1);
	}

	@Override
	protected void validatePage() {
		if (sourceButtonGroup.getSelection() == 1) {
			Object firstElement = ((IStructuredSelection) collViewer.getSelection()).getFirstElement();
			if (firstElement == null) {
				setErrorMessage(Messages.SourcePage_please_select);
				setPageComplete(false);
			} else if (!(firstElement instanceof SmartCollection)) {
				setErrorMessage(Messages.SourcePage_not_a_group);
				setPageComplete(false);
			} else {
				report.setSource(((SmartCollection)firstElement).getStringId());
				report.setSkipOrphans(skipOrphansButton.getSelection());
				setErrorMessage(null);
				setPageComplete(true);
			}
		} else  {
			report.setSource(null);
			report.setSkipOrphans(skipOrphansButton.getSelection());
			setErrorMessage(null);
			setPageComplete(true);
		}
	}
	
	@Override
	public void setVisible(boolean visible) {
		if (visible)
			fillValues();
		super.setVisible(visible);
	}

	protected void fillValues() {
		report = ((ReportWizard)getWizard()).getReport();
		nameField.setText(report.getName());
		String descr = report.getDescription();
		descriptionField.setText(descr == null ? "" : descr); //$NON-NLS-1$
		String id = report.getSource();
		SmartCollection sm = null;
		if (id != null)
			sm = Core.getCore().getDbManager().obtainById(SmartCollectionImpl.class, id);
		if (sm != null) {
			sourceButtonGroup.setSelection(1);
			collViewer.setSelection(new StructuredSelection(sm));
		} else
			sourceButtonGroup.setSelection(0);
		skipOrphansButton.setSelection(report.getSkipOrphans());
		updateFields();
		validatePage();
	}

	public void handleEvent(Event e) {
		updateFields();
	}
	

}
