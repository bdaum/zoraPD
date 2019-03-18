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
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.css.internal.CssActivator;
import com.bdaum.zoom.mtp.StorageObject;
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
				return NLS.bind("{0}  ({1})", template.getLabel(), computePreview(template)); //$NON-NLS-1$
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
	private ListenerList<Listener> listeners = new ListenerList<>();
	private Asset asset;
	private StorageObject file;
	protected QueryField field;
	private ComboViewer fieldViewer;
	private final RenamingTemplate[] systemTemplates;
	private int start = 1;
	private NumericControl startField;
	protected boolean cntrlDwn;
	private Label preLabel;
	private String[] tv;
	private IFileProvider fileprovider;
	private RenamingTemplate presetSelectedTemplate;
	private String presetCue;

	public RenameGroup(Composite parent, int style, Object assetOrFileOrProvider, boolean fieldSelection,
			RenamingTemplate[] systemTemplates, String[] tv) {
		super(parent, style);
		this.tv = tv;
		if (assetOrFileOrProvider instanceof Asset)
			this.asset = (Asset) assetOrFileOrProvider;
		else if (assetOrFileOrProvider instanceof StorageObject)
			this.file = (StorageObject) assetOrFileOrProvider;
		else if (assetOrFileOrProvider instanceof File)
			this.file = new StorageObject((File) assetOrFileOrProvider);
		else if (assetOrFileOrProvider instanceof IFileProvider)
			this.fileprovider = (IFileProvider) assetOrFileOrProvider;
		this.systemTemplates = systemTemplates;
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
		Listener listener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (event.widget == startField)
					start = startField.getSelection();
				updateTemplateViewer();
				fireEvent(event);
			}
		};
		startField.addListener(listener);
		cueField = createHistoryCombo(labelGroup, Messages.RenameGroup_cue);
		cueField.setLayoutData(new GridData(80, -1));
		cueField.addListener(SWT.Modify, listener);
		cueField.addListener(SWT.Selection, listener);
		templateViewer = new TableViewer(this, SWT.BORDER | SWT.V_SCROLL | SWT.SINGLE);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
		layoutData.heightHint = 150;
		templateViewer.getControl().setLayoutData(layoutData);
		templateViewer.setContentProvider(ArrayContentProvider.getInstance());
		templateViewer.setLabelProvider(new TemplateLabelProvider());
		Listener tableListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (event.type == SWT.Selection) {
					updateButtons();
					if (cntrlDwn) {
						if (editButton.isEnabled())
							editTemplate();
						cntrlDwn = false;
					}
					updateParameterFields();
					updatePreview();
					fireEvent(event);
				} else if (event.keyCode == SWT.CTRL)
					cntrlDwn = event.type == SWT.KeyDown;
			}
		};
		templateViewer.getTable().addListener(SWT.KeyDown, tableListener);
		templateViewer.getTable().addListener(SWT.KeyUp, tableListener);
		templateViewer.getTable().addListener(SWT.Selection, tableListener);

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
				templateViewer.remove(templateViewer.getStructuredSelection().getFirstElement());
			}
		});
		templateViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				editTemplate();
			}
		});
		Label sep = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		Label label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		label.setFont(JFaceResources.getBannerFont());
		label.setText(Messages.RenameGroup_preview);
		preLabel = new Label(this, SWT.NONE);
		layoutData = new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 2, 1);
		layoutData.horizontalIndent = 15;
		layoutData.widthHint = 500;
		preLabel.setLayoutData(layoutData);
	}

	protected void updatePreview() {
		RenamingTemplate template = (RenamingTemplate) templateViewer.getStructuredSelection().getFirstElement();
		if (template != null)
			preLabel.setText(computePreview(template));
	}

	private void editTemplate() {
		RenamingTemplate t = (RenamingTemplate) templateViewer.getStructuredSelection().getFirstElement();
		t = openEditDialog(getParent(), t);
		if (t != null)
			templateViewer.update(t, null);
	}

	private NumericControl createNumericControl(Composite parent, String lab) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(lab);
		NumericControl field = new NumericControl(parent, SWT.NONE);
		field.setData(UiConstants.KEY, START);
		field.setData(UiConstants.LABEL, label);
		field.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		field.setSelection(start);
		return field;
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
		TemplateEditDialog dialog = new TemplateEditDialog(parent.getShell(), t, asset != null ? asset : file, field,
				tv);
		dialog.create();
		Point location = getShell().getLocation();
		dialog.getShell().setLocation(location.x + 50, location.y + 100);
		return (dialog.open() == Dialog.OK) ? dialog.getResult() : null;
	}

	private void updateParameterFields() {
		RenamingTemplate oldTemplate = selectedTemplate;
		selectedTemplate = ((RenamingTemplate) templateViewer.getStructuredSelection().getFirstElement());
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
		RenamingTemplate sel = ((RenamingTemplate) templateViewer.getStructuredSelection().getFirstElement());
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

	protected String computePreview(RenamingTemplate template) {
		String cue = cueField.getText().trim();
		int maxLength = BatchConstants.MAXPATHLENGTH;
		String filename = "_1072417.JPG"; //$NON-NLS-1$
		if (asset != null) {
			try {
				file = new StorageObject(new File(new URI(asset.getUri())));
			} catch (URISyntaxException e) {
				// use default
			}
		} else if (fileprovider != null)
			file = fileprovider.getFile();
		if (file != null) {
			filename = file.getName();
			int p = filename.lastIndexOf('.');
			String ext = (p >= 0) ? filename.substring(p) : ""; //$NON-NLS-1$
			maxLength -= (file.getAbsolutePath().length() - filename.length() + ext.length());
		}
		Meta meta = Core.getCore().getDbManager().getMeta(true);
		return Utilities.evaluateTemplate(template.getContent(), tv, filename, new GregorianCalendar(), 1,
				startField.isVisible() ? start : meta.getLastSequenceNo() + 1, meta.getLastYearSequenceNo() + 1,
				!cue.isEmpty() ? cue : Messages.RenameGroup_cue2, asset, "", //$NON-NLS-1$
				maxLength, QueryField.URI == field, false);
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
		setSelectedTemplate(selTemplate);
		boolean visible = cueField.getVisible();
		String[] items = dialogSettings.getArray(CUE);
		if (items != null) {
			cueField.setVisible(true);
			cueField.setItems(items);
			cueField.setVisibleItemCount(8);
			cueField.setVisible(visible);
		}
		if (cue != null)
			setCue(cue);
		try {
			start = dialogSettings.getInt(START);
		} catch (NumberFormatException e) {
			// do nothing
		}
		updateParameterFields();
		if (fieldViewer != null) {
			String id = dialogSettings.get(FIELD);
			fieldViewer.setSelection(
					new StructuredSelection(field = id != null ? QueryField.findQueryField(id) : QueryField.URI));
		}
		updateButtons();
	}

	public void update() {
		updateTemplateViewer();
		updatePreview();
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

	public void addListener(Listener listener) {
		listeners.add(listener);
	}

	public void removeListener(Listener listener) {
		listeners.remove(listener);
	}

	protected void fireEvent(Event e) {
		for (Listener listener : listeners)
			listener.handleEvent(e);
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

	private void setSelectedTemplate(String tt) {
		for (RenamingTemplate t : templates)
			if (t.getContent().equals(tt) || t.getLabel().equals(tt)) {
				templateViewer.setSelection(new StructuredSelection(presetSelectedTemplate = selectedTemplate = t));
				break;
			}
	}

	private void setCue(String cue) {
		boolean visible = cueField.getVisible();
		cueField.setVisible(true);
		cueField.setText(presetCue = cue);
		cueField.setVisible(visible);
	}

	public void updateValues(String tt, String cue) {
		Object firstElement = templateViewer.getStructuredSelection().getFirstElement();
		if (firstElement == null || firstElement.equals(presetSelectedTemplate) && tt != null && !tt.isEmpty())
			setSelectedTemplate(tt);
		if (cue != null && !cue.isEmpty()) {
			boolean visible = cueField.getVisible();
			cueField.setVisible(true);
			if (cueField.getText().equals(presetCue))
				setCue(cue);
			cueField.setVisible(visible);
		}
	}

}
