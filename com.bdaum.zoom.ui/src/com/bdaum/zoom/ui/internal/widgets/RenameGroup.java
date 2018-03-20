package com.bdaum.zoom.ui.internal.widgets;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.css.internal.CssActivator;
import com.bdaum.zoom.program.BatchConstants;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiConstants;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.dialogs.RenamingTemplate;
import com.bdaum.zoom.ui.internal.dialogs.TemplateEditDialog;
import com.bdaum.zoom.ui.widgets.NumericControl;

@SuppressWarnings("restriction")
public class RenameGroup extends Composite {

	public class TemplateLabelProvider extends ZColumnLabelProvider {

		@Override
		public String getText(Object element) {
			if (element instanceof RenamingTemplate) {
				RenamingTemplate template = (RenamingTemplate) element;
				String cue = cueField.getText().trim();
				int maxLength = BatchConstants.MAXPATHLENGTH;
				String filename = "_1072417.JPG"; //$NON-NLS-1$
				if (asset != null) {
					File file;
					try {
						file = new File(new URI(asset.getUri()));
						filename = file.getName();
						String ext = ""; //$NON-NLS-1$
						int p = filename.lastIndexOf('.');
						if (p >= 0)
							ext = filename.substring(p);
						maxLength -= (file.getParent().length() + 1 + ext.length());
					} catch (URISyntaxException e) {
						// use default
					}
				}
				return template.getLabel() + "  (" //$NON-NLS-1$
						+ Utilities.evaluateTemplate(template.getContent(),
								asset != null ? Constants.TV_RENAME
										: transfer ? Constants.TV_TRANSFER : Constants.TV_ALL,
								filename, new GregorianCalendar(), 1, start, 1,
								!cue.isEmpty() ? cue : Messages.RenameGroup_cue2, asset, "", maxLength, //$NON-NLS-1$
								QueryField.URI == field)
						+ ")"; //$NON-NLS-1$
			}
			return element.toString();
		}

		@Override
		public Image getImage(Object element) {
			if (selectedTemplate == element)
				return Icons.forwards.getImage();
			return null;
		}

		@Override
		protected Rectangle getIconBounds() {
			return Icons.forwards.getImage().getBounds();
		}

	}

	private static final String SELECTEDTEMPLATE = "activeTemplate"; //$NON-NLS-1$
	private static final String TEMPLATELABEL = "name"; //$NON-NLS-1$
	private static final String TEMPLATECONTENT = "value"; //$NON-NLS-1$
	private static final String CUE = "cue"; //$NON-NLS-1$
	private static final String START = "start"; //$NON-NLS-1$
	private static final String FIELD = "field"; //$NON-NLS-1$

	private TableViewer templateViewer;
	private Button addButton;
	private Button editButton;
	private Button removeButton;
	private Combo cueField;
	private RenamingTemplate selectedTemplate;
	private ArrayList<RenamingTemplate> templates;
	private ListenerList<SelectionListener> selectionListeners = new ListenerList<SelectionListener>();
	private ListenerList<ModifyListener> modifyListeners = new ListenerList<ModifyListener>();
	private ListenerList<ISelectionChangedListener> selectionChangedListeners = new ListenerList<ISelectionChangedListener>();
	private final Asset asset;
	protected QueryField field;
	private ComboViewer fieldViewer;
	private final RenamingTemplate[] systemTemplates;
	private boolean transfer;
	private int start = 1;
	private NumericControl startField;
	protected boolean cntrlDwn;

	public RenameGroup(Composite parent, int style, Asset asset, boolean fieldSelection,
			RenamingTemplate[] systemTemplates, boolean transfer) {
		super(parent, style);
		this.asset = asset;
		this.systemTemplates = systemTemplates;
		this.transfer = transfer;
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		setLayout(new GridLayout(2, false));
		Composite labelGroup = new Composite(this, SWT.NONE);
		labelGroup.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));
		labelGroup.setLayout(new GridLayout(7, false));
		if (fieldSelection) {
			Label label = new Label(labelGroup, SWT.NONE);
			label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			label.setText(Messages.RenameGroup_change);
			fieldViewer = new ComboViewer(labelGroup);
			fieldViewer.setContentProvider(ArrayContentProvider.getInstance());
			fieldViewer.setLabelProvider(new ZColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					return QueryField.URI == element ? Messages.RenameGroup_filename_imagename
							: ((QueryField) element).getLabel();
				}
			});
			fieldViewer.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					field = ((QueryField) ((IStructuredSelection) event.getSelection()).getFirstElement());
					templateViewer.refresh(true);
				}
			});
			fieldViewer.setInput(new QueryField[] { QueryField.URI, QueryField.IPTC_TITLE });
		} else {
			final Label fileRenamingLabel = new Label(labelGroup, SWT.NONE);
			fileRenamingLabel.setFont(JFaceResources.getBannerFont());
			fileRenamingLabel.setText(Messages.RenameGroup_file_renaming);
			new Label(labelGroup, SWT.NONE).setText(Messages.RenameGroup_please_select_template);
		}
		new Label(labelGroup, SWT.NONE).setLayoutData(new GridData(100, -1));
		startField = createNumericControl(labelGroup, Messages.RenameGroup_start_at);
		startField.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				start = startField.getSelection();
				updateTemplateViewer();
				fireSelection(e);
			}
		});
		cueField = createHistoryCombo(labelGroup, Messages.RenameGroup_cue);
		cueField.setLayoutData(new GridData(80, -1));
		cueField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateTemplateViewer();
				fireModify(e);
			}
		});
		cueField.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateTemplateViewer();
				fireSelection(e);
			}
		});
		templateViewer = new TableViewer(this, SWT.BORDER | SWT.V_SCROLL | SWT.SINGLE);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
		layoutData.heightHint = 150;
		templateViewer.getControl().setLayoutData(layoutData);
		templateViewer.setContentProvider(ArrayContentProvider.getInstance());
		templateViewer.setLabelProvider(new TemplateLabelProvider());
		templateViewer.getControl().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CTRL)
					cntrlDwn = true;
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.CTRL)
					cntrlDwn = false;
			}
		});
		templateViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateButtons();
				if (cntrlDwn) {
					if (editButton.isEnabled())
						editTemplate();
					cntrlDwn = false;
				}
				updateParameterFields();
				fireSelectionChanged(event);
			}
		});

		final Composite buttonComp = new Composite(this, SWT.NONE);
		buttonComp.setLayout(new GridLayout());

		addButton = WidgetFactory.createPushButton(buttonComp, Messages.RenameGroup_add, SWT.FILL);
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				RenamingTemplate t = openEditDialog(getParent(), null);
				if (t != null)
					templateViewer.add(t);
			}
		});
		editButton = WidgetFactory.createPushButton(buttonComp, Messages.RenameGroup_edit, SWT.FILL);
		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editTemplate();
			}
		});
		removeButton = WidgetFactory.createPushButton(buttonComp, Messages.RenameGroup_remove, SWT.FILL);
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				templateViewer.remove(((IStructuredSelection) templateViewer.getSelection()).getFirstElement());
			}
		});
		templateViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				editTemplate();
			}
		});
	}

	private void editTemplate() {
		RenamingTemplate t = (RenamingTemplate) ((IStructuredSelection) templateViewer.getSelection())
				.getFirstElement();
		t = openEditDialog(getParent(), t);
		if (t != null)
			templateViewer.update(t, null);
	}

	private NumericControl createNumericControl(Composite parent, String lab) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(lab);
		NumericControl startField = new NumericControl(parent, SWT.NONE);
		startField.setData(UiConstants.KEY, START); 
		startField.setData(UiConstants.LABEL, label); 
		startField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		startField.setSelection(start);
		return startField;
	}

	private static Combo createHistoryCombo(Composite parent, String lab) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(lab);
		Combo cueCombo = new Combo(parent, SWT.NONE);
		cueCombo.setData(UiConstants.KEY, CUE);
		cueCombo.setData(UiConstants.LABEL, label); 
		cueCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		return cueCombo;
	}

	private void saveComboHistory(Combo combo, IDialogSettings settings) {
		settings.put(CUE, UiUtilities.updateComboHistory(combo));
		settings.put(START, start);
	}

	private RenamingTemplate openEditDialog(Composite parent, RenamingTemplate t) {
		TemplateEditDialog dialog = new TemplateEditDialog(parent.getShell(), t, asset, field, transfer);
		dialog.create();
		Point location = getShell().getLocation();
		dialog.getShell().setLocation(location.x + 50, location.y + 100);
		return (dialog.open() == Dialog.OK) ? dialog.getResult() : null;
	}

	private void updateParameterFields() {
		RenamingTemplate oldTemplate = selectedTemplate;
		selectedTemplate = ((RenamingTemplate) ((IStructuredSelection) templateViewer.getSelection())
				.getFirstElement());
		if (oldTemplate != null)
			templateViewer.update(oldTemplate, null);
		if (selectedTemplate != null)
			templateViewer.update(selectedTemplate, null);
		boolean visible = selectedTemplate != null && selectedTemplate.getContent().indexOf(Constants.TV_CUE) >= 0;
		cueField.setVisible(visible);
		((Control) cueField.getData(UiConstants.LABEL)).setVisible(visible); 
		if (visible)
			CssActivator.getDefault().setColors(cueField);
		visible = asset != null && selectedTemplate != null
				&& (selectedTemplate.getContent().indexOf(Constants.TV_IMAGE_NO5) >= 0
						|| selectedTemplate.getContent().indexOf(Constants.TV_IMAGE_NO4) >= 0
						|| selectedTemplate.getContent().indexOf(Constants.TV_IMAGE_NO3) >= 0
						|| selectedTemplate.getContent().indexOf(Constants.TV_IMAGE_NO2) >= 0
						|| selectedTemplate.getContent().indexOf(Constants.TV_IMAGE_NO1) >= 0);
		startField.setVisible(visible);
		((Control) startField.getData(UiConstants.LABEL)).setVisible(visible); 
		if (visible)
			CssActivator.getDefault().setColors(startField);
	}

	private void updateButtons() {
		RenamingTemplate sel = ((RenamingTemplate) ((IStructuredSelection) templateViewer.getSelection())
				.getFirstElement());
		editButton.setEnabled(sel != null);
		removeButton.setEnabled(sel != null && !sel.isSystem());
	}

	protected void updateTemplateViewer() {
		for (RenamingTemplate template : templates) {
			String content = template.getContent();
			if (content.indexOf(Constants.TV_CUE) >= 0)
				templateViewer.update(template, null);
		}
	}

	public void fillValues(IDialogSettings dialogSettings, String selTemplate, String cue) {
		templates = new ArrayList<RenamingTemplate>();
		templates.addAll(Arrays.asList(systemTemplates));
		IDialogSettings[] sections = dialogSettings.getSections();
		if (sections != null)
			for (IDialogSettings template : sections) {
				String label = template.get(TEMPLATELABEL);
				String content = template.get(TEMPLATECONTENT);
				if (label != null && content != null)
					templates.add(new RenamingTemplate(label, content, false));
			}
		if (selTemplate == null)
			selTemplate = dialogSettings.get(SELECTEDTEMPLATE);
		templateViewer.setInput(templates);
		for (RenamingTemplate t : templates)
			if (t.getContent().equals(selTemplate) || t.getLabel().equals(selTemplate)) {
				templateViewer.setSelection(new StructuredSelection(t));
				break;
			}
		boolean visible = cueField.getVisible();
		String[] items = dialogSettings.getArray(CUE);
		if (items != null) {
			cueField.setVisible(true);
			cueField.setItems(items);
			cueField.setVisibleItemCount(8);
			cueField.setVisible(visible);
		}
		if (cue != null) {
			cueField.setVisible(true);
			cueField.setText(cue);
			cueField.setVisible(visible);
		}
		try {
			start = dialogSettings.getInt(START);
		} catch (NumberFormatException e) {
			// do nothing
		}
		updateParameterFields();
		if (fieldViewer != null) {
			String id = dialogSettings.get(FIELD);
			fieldViewer.setSelection(new StructuredSelection(field = id != null ? QueryField.findQueryField(id) : QueryField.URI));
		}
	}

	public void saveSettings(IDialogSettings dialogSettings) {
		saveComboHistory(cueField, dialogSettings);
		int i = 0;
		for (RenamingTemplate t : templates)
			if (!t.isSystem()) {
				IDialogSettings section = dialogSettings.addNewSection("template" //$NON-NLS-1$
						+ i++);
				section.put(TEMPLATELABEL, t.getLabel());
				section.put(TEMPLATECONTENT, t.getContent());
			}
		if (selectedTemplate != null)
			dialogSettings.put(SELECTEDTEMPLATE, selectedTemplate.getLabel());
		if (field != null)
			dialogSettings.put(FIELD, field.getId());
	}

	public RenamingTemplate getSelectedTemplate() {
		return selectedTemplate;
	}

	public String validate() {
		if (selectedTemplate == null)
			return Messages.RenameGroup_select_template;
		if (cueField.isVisible() && cueField.getText().trim().isEmpty())
			return Messages.RenameGroup_cue_empty;
		return null;
	}

	public void addSelectionListener(SelectionListener listener) {
		selectionListeners.add(listener);
	}

	public void removeSelectionListener(SelectionListener listener) {
		selectionListeners.remove(listener);
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		selectionChangedListeners.add(listener);
	}

	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		selectionChangedListeners.remove(listener);
	}

	public void addModifyListener(ModifyListener listener) {
		modifyListeners.add(listener);
	}

	public void removeModifyListener(ModifyListener listener) {
		modifyListeners.remove(listener);
	}

	protected void fireSelection(SelectionEvent e) {
		for (SelectionListener listener : selectionListeners)
			listener.widgetSelected(e);
	}

	protected void fireSelectionChanged(SelectionChangedEvent e) {
		for (ISelectionChangedListener listener : selectionChangedListeners)
			listener.selectionChanged(e);
	}

	protected void fireModify(ModifyEvent e) {
		for (ModifyListener listener : modifyListeners)
			listener.modifyText(e);
	}

	public String getCue() {
		return cueField.getText();
	}

	public QueryField getField() {
		return field;
	}

	public int getStart() {
		return start;
	}

}
