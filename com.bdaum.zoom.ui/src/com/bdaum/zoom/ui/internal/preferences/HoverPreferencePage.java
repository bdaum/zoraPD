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
 * (c) 2019 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
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
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.FieldDescriptor;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.TemplateProcessor;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.dialogs.AddVariablesDialog;
import com.bdaum.zoom.ui.internal.dialogs.TemplateFieldSelectionDialog;
import com.bdaum.zoom.ui.internal.hover.HoverTestAsset;
import com.bdaum.zoom.ui.internal.hover.IHoverContribution;
import com.bdaum.zoom.ui.internal.views.ZColumnViewerToolTipSupport;
import com.bdaum.zoom.ui.preferences.AbstractPreferencePage;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;
import com.bdaum.zoom.ui.widgets.CGroup;

public class HoverPreferencePage extends AbstractPreferencePage
		implements IDoubleClickListener, Listener, ISelectionChangedListener {

	public class HoverNode {

		private IHoverContribution contrib;
		private Category parent;
		private String titleTemplate;
		private String template;

		public HoverNode(IHoverContribution contrib, Category parent) {
			this.contrib = contrib;
			this.parent = parent;
		}

		public Object getParent() {
			return parent;
		}

		@Override
		public String toString() {
			return contrib.toString();
		}

		public IHoverContribution getContribution() {
			return contrib;
		}

		public void setTitleTemplate(String template) {
			this.titleTemplate = template;
		}

		public void setTemplate(String template) {
			this.template = template;
		}

		public void save() {
			contrib.save(titleTemplate, template);
		}

		public String getTitleTemplate() {
			return titleTemplate;
		}

		public String getTemplate() {
			return template;
		}

	}

	public class HoverEditGroup extends CGroup implements Listener, VerifyListener {

		private Text templateField;
		private Text previewField;
		private IHoverContribution contrib;
		private Button addVariableButton;
		private Button addMetadataButon;
		private String[] variables;
		private boolean isTitle;

		@SuppressWarnings("unused")
		public HoverEditGroup(Composite parent, String label, IHoverContribution contrib, String[] variables,
				String[] vlabels, boolean isTitle) {
			super(parent, SWT.NONE);
			this.variables = variables;
			this.isTitle = isTitle;
			boolean metadata = contrib.getTarget(contrib.getTestObject()) instanceof HoverTestAsset;
			this.contrib = contrib;
			setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			setLayout(new GridLayout(3, false));
			setText(label);
			Label lab = new Label(this, SWT.NONE);
			lab.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
			lab.setText(Messages.getString("HoverPreferencePage.template")); //$NON-NLS-1$
			templateField = new Text(this, SWT.MULTI | SWT.LEAD | SWT.BORDER);
			if (isTitle)
				templateField.addVerifyListener(this);
			GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
			layoutData.heightHint = 100;
			layoutData.widthHint = 300;
			templateField.setLayoutData(layoutData);
			templateField.addListener(SWT.Modify, this);
			new Label(this, SWT.NONE);
			if (variables != null && variables.length > 0) {
				addVariableButton = new Button(this, SWT.PUSH);
				addVariableButton.setText(Messages.getString("HoverPreferencePage.add_variable")); //$NON-NLS-1$
				addVariableButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						AddVariablesDialog dialog = new AddVariablesDialog(getShell(),
								Messages.getString("HoverPreferencePage.add_variable"), variables, vlabels); //$NON-NLS-1$
						Point loc = templateField.toDisplay(20, 10);
						dialog.create();
						dialog.getShell().setLocation(loc);
						if (dialog.open() == Window.OK)
							templateField.insert(dialog.getResult());
					}
				});
			} else
				new Label(this, SWT.NONE);
			if (metadata) {
				addMetadataButon = new Button(this, SWT.PUSH);
				addMetadataButon.setText(Messages.getString("HoverPreferencePage.add_metadata")); //$NON-NLS-1$
				addMetadataButon.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						TemplateFieldSelectionDialog dialog = new TemplateFieldSelectionDialog(getShell());
						Point loc = templateField.toDisplay(20, 10);
						dialog.create();
						dialog.getShell().setLocation(loc);
						if (dialog.open() == TemplateFieldSelectionDialog.OK) {
							FieldDescriptor fd = dialog.getResult();
							String qname = fd.subfield == null ? fd.qfield.getId()
									: fd.qfield.getId() + '&' + fd.subfield.getId();
							templateField.insert(Constants.TV_META + qname + '}');
						}
					}
				});
			} else
				new Label(this, SWT.NONE);
			lab = new Label(this, SWT.NONE);
			lab.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
			lab.setText(Messages.getString("HoverPreferencePage.preview")); //$NON-NLS-1$
			previewField = new Text(this, SWT.MULTI | SWT.LEAD | SWT.BORDER | SWT.READ_ONLY);
			layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
			layoutData.heightHint = 100;
			layoutData.widthHint = 300;
			previewField.setLayoutData(layoutData);
		}

		@Override
		public void handleEvent(Event event) {
			computePreview();
			updateButtons();
		}

		public void setTemplate(String template) {
			templateField.setText(template);
			updateButtons();
			computePreview();
		}

		private void computePreview() {
			previewField
					.setText(templateProcessor.processTemplate(templateField.getText(), contrib, variables, isTitle));
		}

		public String getTemplate() {
			return templateField.getText();
		}

		@Override
		public void verifyText(VerifyEvent e) {
			if (e.text.indexOf('\n') >= 0)
				e.doit = false;
		}

	}

	public class HoverEditDialog extends ZTitleAreaDialog {

		private static final int DEFAULTBUTTON = 9999;
		private HoverNode node;
		private HoverEditGroup titleGroup;
		private HoverEditGroup textGroup;
		private IHoverContribution contrib;

		public HoverEditDialog(Shell shell, HoverNode node) {
			super(shell, HelpContextIds.HOVER_PREFERENCE_PAGE);
			this.node = node;
			this.contrib = node.getContribution();
		}

		@Override
		public void create() {
			super.create();
			fillValues();
			updateButtons();
			setTitle(NLS.bind(Messages.getString("HoverPreferencePage.edit_template"), node.getParent().toString(), //$NON-NLS-1$
					node.toString()));
			setMessage(Messages.getString("HoverPreferencePage.modify")); //$NON-NLS-1$
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite area = (Composite) super.createDialogArea(parent);
			Composite composite = new Composite(area, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			composite.setLayout(new GridLayout());
			if (contrib.supportsTitle())
				titleGroup = new HoverEditGroup(composite, Messages.getString("HoverPreferencePage.title"), contrib, //$NON-NLS-1$
						contrib.getTitleItemKeys(), contrib.getTitleItemLabels(), true);
			textGroup = new HoverEditGroup(composite, Messages.getString("HoverPreferencePage.text"), contrib, //$NON-NLS-1$
					contrib.getItemKeys(), contrib.getItemLabels(), false);
			return area;
		}

		private void fillValues() {
			String template;
			if (titleGroup != null) {
				template = node.getTemplate();
				if (template == null || template.isEmpty())
					template = contrib.getTitleTemplate();
				if (template == null || template.isEmpty())
					template = contrib.getDefaultTitleTemplate();
				if (template == null)
					template = ""; //$NON-NLS-1$
				titleGroup.setTemplate(template);
			}
			template = node.getTemplate();
			if (template == null || template.isEmpty())
				template = contrib.getTemplate();
			if (template == null || template.isEmpty())
				template = contrib.getDefaultTemplate();
			if (template == null)
				template = ""; //$NON-NLS-1$
			textGroup.setTemplate(template);
		}

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, DEFAULTBUTTON, Messages.getString("HoverPreferencePage.restore"), false); //$NON-NLS-1$
			super.createButtonsForButtonBar(parent);
		}

		private void updateButtons() {
			boolean enabled = textGroup.getTemplate().equals(contrib.getDefaultTemplate());
			if (titleGroup != null)
				enabled |= titleGroup.getTemplate().equals(contrib.getDefaultTemplate());
			getButton(DEFAULTBUTTON).setEnabled(enabled);
		}

		@Override
		protected void buttonPressed(int buttonId) {
			if (buttonId == DEFAULTBUTTON) {
				if (titleGroup != null)
					titleGroup.setTemplate(contrib.getDefaultTitleTemplate());
				textGroup.setTemplate(contrib.getDefaultTemplate());
				return;
			}
			super.buttonPressed(buttonId);
		}

		@Override
		protected void okPressed() {
			if (titleGroup != null)
				node.setTitleTemplate(titleGroup.getTemplate());
			node.setTemplate(textGroup.getTemplate());
			viewer.update(node, null);
			doUpdateButtons();
			super.okPressed();
		}

	}

	public class Category {

		private String label;
		private List<HoverNode> children = new ArrayList<>(5);

		public Category(String label) {
			this.label = label;
		}

		public void add(HoverNode contrib) {
			children.add(contrib);
		}

		public List<HoverNode> getChildren() {
			return children;
		}

		@Override
		public String toString() {
			return label;
		}

	}

	private TreeViewer viewer;
	private Map<String, Category> rootMap = new HashMap<>(11);
	private Button editButton;
	private TemplateProcessor templateProcessor = new TemplateProcessor();
	private Button resetButton;
	private CTabItem tabItem0;
	private CTabItem tabItem1;
	private Spinner baseTimeField;
	private Spinner charTimeField;
	private Spinner delayTimeField;

	public HoverPreferencePage() {
		super();
	}

	public HoverPreferencePage(String title) {
		super(title);
	}

	public HoverPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	@Override
	protected void createPageContents(Composite comp) {
		setHelp(HelpContextIds.HOVER_PREFERENCE_PAGE);
		createTabFolder(comp, "Templates"); //$NON-NLS-1$
		tabItem0 = UiUtilities.createTabItem(tabFolder, Messages.getString("HoverPreferencePage.templates"), //$NON-NLS-1$
				Messages.getString("HoverPreferencePage.templates_tooltip")); //$NON-NLS-1$
		Composite comp0 = createTemplatesPage(tabFolder);
		tabItem0.setControl(comp0);
		tabItem1 = UiUtilities.createTabItem(tabFolder, Messages.getString("HoverPreferencePage.timing"), Messages.getString("HoverPreferencePage.timing_tooltip")); //$NON-NLS-1$ //$NON-NLS-2$
		Composite comp1 = createTimingPage(tabFolder);
		tabItem1.setControl(comp1);
		initTabFolder(0);
		fillValues();
	}

	@SuppressWarnings("unused")
	private Composite createTimingPage(CTabFolder tabFolder) {
		Composite comp = new Composite(tabFolder, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout());
		new Label(comp, SWT.WRAP).setText(Messages.getString("HoverPreferencePage.timing_expl"));  //$NON-NLS-1$
		new Label(comp, SWT.NONE);
		Composite composite = new Composite(comp, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, false));
		new Label(composite, SWT.NONE).setText(Messages.getString("HoverPreferencePage.delay")); //$NON-NLS-1$
		delayTimeField = new Spinner(composite, SWT.BORDER);
		delayTimeField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		delayTimeField.setMaximum(2000);
		delayTimeField.setIncrement(10);
		delayTimeField.setPageIncrement(100);
		new Label(composite, SWT.NONE).setText(Messages.getString("HoverPreferencePage.base_time")); //$NON-NLS-1$
		baseTimeField = new Spinner(composite, SWT.BORDER);
		baseTimeField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		baseTimeField.setMaximum(10000);
		baseTimeField.setIncrement(50);
		baseTimeField.setPageIncrement(500);
		new Label(composite, SWT.NONE).setText(Messages.getString("HoverPreferencePage.time_per_char")); //$NON-NLS-1$
		charTimeField = new Spinner(composite, SWT.BORDER);
		charTimeField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		charTimeField.setMaximum(500);
		charTimeField.setMinimum(1);
		charTimeField.setIncrement(1);
		charTimeField.setPageIncrement(1);

		return comp;
	}

	@SuppressWarnings("unused")
	private Composite createTemplatesPage(CTabFolder tabFolder) {
		Composite comp = new Composite(tabFolder, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout());
		new Label(comp, SWT.WRAP).setText(Messages.getString("HoverPreferencePage.define")); //$NON-NLS-1$
		new Label(comp, SWT.NONE);
		Composite composite = new Composite(comp, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, false));
		viewer = new TreeViewer(composite, SWT.BORDER | SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.heightHint = 250;
		viewer.getTree().setLayoutData(layoutData);
		viewer.setContentProvider(new ITreeContentProvider() {
			@Override
			public boolean hasChildren(Object element) {
				return element instanceof Category;
			}

			@Override
			public Object getParent(Object element) {
				if (element instanceof HoverNode)
					return ((HoverNode) element).getParent();
				return null;
			}

			@Override
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof Object[])
					return (Object[]) inputElement;
				return null;
			}

			@Override
			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof Category)
					return ((Category) parentElement).getChildren().toArray();
				return null;
			}
		});
		viewer.setComparator(new ViewerComparator());
		ZColumnViewerToolTipSupport.enableFor(viewer);
		TreeViewerColumn col1 = new TreeViewerColumn(viewer, SWT.NONE);
		col1.getColumn().setWidth(280);
		col1.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return element.toString();
			}

			@Override
			public String getToolTipText(Object element) {
				if (element instanceof HoverNode && UiActivator.getDefault().getShowHover())
					return ((HoverNode) element).getContribution().getDescription();
				return super.getToolTipText(element);
			}
		});
		viewer.addDoubleClickListener(this);
		viewer.addSelectionChangedListener(this);
		TreeViewerColumn col2 = new TreeViewerColumn(viewer, SWT.NONE);
		col2.getColumn().setWidth(80);
		col2.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof HoverNode)
					return testForUnModified((HoverNode) element) ? Messages.getString("HoverPreferencePage.default") //$NON-NLS-1$
							: Messages.getString("HoverPreferencePage.modified"); //$NON-NLS-1$
				return ""; //$NON-NLS-1$
			}
		});
		viewer.addDoubleClickListener(this);
		viewer.addSelectionChangedListener(this);
		Composite buttonArea = new Composite(composite, SWT.NONE);
		buttonArea.setLayoutData(new GridData(SWT.END, SWT.FILL, false, true));
		buttonArea.setLayout(new GridLayout());
		editButton = new Button(buttonArea, SWT.PUSH);
		editButton.setText(Messages.getString("HoverPreferencePage.edit")); //$NON-NLS-1$
		editButton.addListener(SWT.Selection, this);
		resetButton = new Button(buttonArea, SWT.PUSH);
		resetButton.setText(Messages.getString("HoverPreferencePage.reset")); //$NON-NLS-1$
		resetButton.addListener(SWT.Selection, this);
		return comp;
	}

	@Override
	public void init(IWorkbench wb) {
		super.init(wb);
		setTitle(Messages.getString("HoverPreferencePage.hover")); //$NON-NLS-1$
		setMessage(Messages.getString("HoverPreferencePage.hover"));  //$NON-NLS-1$
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return UiActivator.getDefault().getPreferenceStore();
	}

	@Override
	protected void doFillValues() {
		Map<String, IHoverContribution> hoverContributions = UiActivator.getDefault().getHoverManager()
				.getHoverContributions();
		for (IHoverContribution contrib : hoverContributions.values()) {
			String cat = contrib.getCategory();
			Category root = rootMap.get(cat);
			if (root == null)
				rootMap.put(cat, root = new Category(cat));
			root.add(new HoverNode(contrib, root));
		}
		viewer.setInput(rootMap.values().toArray());
		viewer.expandAll();
		IPreferenceStore preferenceStore = getPreferenceStore();
		delayTimeField.setSelection(preferenceStore.getInt(PreferenceConstants.HOVERDELAY));
		baseTimeField.setSelection(preferenceStore.getInt(PreferenceConstants.HOVERBASETIME));
		charTimeField.setSelection(preferenceStore.getInt(PreferenceConstants.HOVERCHARTIME));
		updateButtons();
		super.doFillValues();
	}

	@Override
	protected void doPerformOk() {
		for (Category cat : rootMap.values())
			for (HoverNode node : cat.getChildren())
				node.save();
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(PreferenceConstants.HOVERDELAY,delayTimeField.getSelection());
		preferenceStore.setValue(PreferenceConstants.HOVERBASETIME,baseTimeField.getSelection());
		preferenceStore.setValue(PreferenceConstants.HOVERCHARTIME,charTimeField.getSelection());
	}

	@Override
	public void doubleClick(DoubleClickEvent event) {
		openEditDialog();
	}

	@Override
	public void handleEvent(Event event) {
		if (event.widget == editButton)
			openEditDialog();
		else if (event.widget == resetButton) {
			Object el = viewer.getStructuredSelection().getFirstElement();
			if (el instanceof HoverNode)
				resetNode(el);
			else if (el instanceof Category)
				for (HoverNode node : ((Category) el).getChildren())
					resetNode(node);
		}
	}

	private void resetNode(Object el) {
		HoverNode node = (HoverNode) el;
		IHoverContribution contribution = node.getContribution();
		node.setTemplate(contribution.getDefaultTemplate());
		if (contribution.supportsTitle())
			node.setTitleTemplate(contribution.getDefaultTitleTemplate());
		viewer.update(node, null);
	}

	private void openEditDialog() {
		Object el = viewer.getStructuredSelection().getFirstElement();
		if (el instanceof HoverNode) {
			HoverNode node = (HoverNode) el;
			HoverEditDialog dialog = new HoverEditDialog(getShell(), node);
			if (dialog.open() == HoverEditDialog.OK)
				viewer.update(node, null);
		}
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		updateButtons();
	}

	@Override
	protected void doPerformDefaults() {
		for (Category cat : rootMap.values())
			for (HoverNode node : cat.getChildren())
				resetNode(node);
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(PreferenceConstants.HOVERDELAY,
				preferenceStore.getDefaultInt(PreferenceConstants.HOVERDELAY));
		preferenceStore.setValue(PreferenceConstants.HOVERBASETIME,
				preferenceStore.getDefaultInt(PreferenceConstants.HOVERBASETIME));
		preferenceStore.setValue(PreferenceConstants.HOVERCHARTIME,
				preferenceStore.getDefaultInt(PreferenceConstants.HOVERCHARTIME));
	}

	@Override
	protected void doUpdateButtons() {
		Object el = viewer.getStructuredSelection().getFirstElement();
		editButton.setEnabled(el instanceof HoverNode);
		if (el instanceof HoverNode)
			resetButton.setEnabled(!testForUnModified((HoverNode) el));
		else if (el instanceof Category) {
			boolean unmodified = true;
			for (HoverNode node : ((Category) el).getChildren())
				unmodified &= testForUnModified(node);
			resetButton.setEnabled(!unmodified);
		}
		super.doUpdateButtons();
	}

	private static boolean testForUnModified(HoverNode node) {
		IHoverContribution contrib = node.getContribution();
		String template = node.getTemplate();
		if (template == null)
			template = contrib.getTemplate();
		boolean unmodified = template == null || template.isEmpty() || template.equals(contrib.getDefaultTemplate());
		if (contrib.supportsTitle()) {
			String titleTemplate = node.getTitleTemplate();
			if (titleTemplate == null)
				titleTemplate = contrib.getTitleTemplate();
			unmodified &= titleTemplate == null || titleTemplate.isEmpty()
					|| titleTemplate.equals(contrib.getDefaultTitleTemplate());
		}
		return unmodified;
	}

}
