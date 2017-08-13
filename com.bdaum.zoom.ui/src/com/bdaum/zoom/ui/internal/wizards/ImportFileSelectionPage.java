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
 * (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ui.internal.wizards;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.cat.model.meta.LastDeviceImport;
import com.bdaum.zoom.cat.model.meta.LastDeviceImportImpl;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.FileInput;
import com.bdaum.zoom.core.internal.FileNameExtensionFilter;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.core.internal.ImportFromDeviceData;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.dialogs.ZProgressMonitorDialog;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.dialogs.AllNoneGroup;
import com.bdaum.zoom.ui.internal.dialogs.MediaDialog;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.ExpandCollapseGroup;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.wizards.ColoredWizardPage;

@SuppressWarnings("restriction")
public class ImportFileSelectionPage extends ColoredWizardPage {

	public static final String[] SKIPPOLICIES = new String[] { Messages.ImportFileSelectionPage_none,
			Messages.ImportFileSelectionPage_all_raw, Messages.ImportFileSelectionPage_raw_if_jpeg,
			Messages.ImportFileSelectionPage_all_jpeg, Messages.ImportFileSelectionPage_jpeg_if_raw };

	public static class SkippedFormatsDialog extends ZTitleAreaDialog implements SelectionListener {

		private static final int SELECTALL = 9999;
		private static final int SELECTNONE = 9998;
		private final Set<String> skippedFormats;
		private HashSet<String> result;
		private Map<String, Button> buttonMap = new HashMap<String, Button>(90);
		private int skipCount;

		public SkippedFormatsDialog(Shell parentShell, Set<String> skippedFormats) {
			super(parentShell);
			this.skippedFormats = skippedFormats;
			skipCount = skippedFormats.size();
		}

		@Override
		public void create() {
			super.create();
			setTitle(Messages.ImportFileSelectionPage_skipped_files);
			setMessage();
			updateButtons();
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite area = (Composite) super.createDialogArea(parent);
			Composite composite = new Composite(area, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			composite.setLayout(new GridLayout(6, true));
			Set<String> formats = ImageConstants.getAllFormats();
			String[] allFormats = formats.toArray(new String[formats.size()]);
			Arrays.sort(allFormats);
			for (String format : allFormats) {
				Composite checkGroup = new Composite(composite, SWT.NONE);
				GridLayout layout = new GridLayout(2, false);
				layout.marginWidth = 2;
				layout.marginHeight = 2;
				checkGroup.setLayout(layout);
				Button button = new Button(checkGroup, SWT.CHECK);
				button.addSelectionListener(this);
				button.setSelection(skippedFormats.contains(format));
				buttonMap.put(format, button);
				new Label(checkGroup, SWT.NONE)
						.setText(format.isEmpty() ? Messages.ImportFileSelectionPage_no_ext : format);
			}
			return area;
		}

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, SELECTALL, Messages.ImportFileSelectionPage_select_all, false);
			createButton(parent, SELECTNONE, Messages.ImportFileSelectionPage_select_none, false);
			super.createButtonsForButtonBar(parent);
		}

		@Override
		protected void buttonPressed(int buttonId) {
			switch (buttonId) {
			case SELECTALL:
				for (Button button : buttonMap.values())
					button.setSelection(true);
				updateButtons();
				break;
			case SELECTNONE:
				for (Button button : buttonMap.values())
					button.setSelection(false);
				updateButtons();
				break;
			}
			super.buttonPressed(buttonId);
		}

		protected void updateButtons() {
			boolean valid = validate();
			getButton(OK).setEnabled(valid);
		}

		private boolean validate() {
			skipCount = 0;
			for (Button button : buttonMap.values())
				if (button.getSelection())
					++skipCount;
			if (skipCount == buttonMap.size()) {
				setErrorMessage(Messages.ImportFileSelectionPage_nothing_to_import);
				getShell().pack();
				return false;
			}
			setErrorMessage(null);
			setMessage();
			getShell().pack();
			return true;
		}

		private void setMessage() {
			setMessage(Messages.ImportFileSelectionPage_Mark_extensions + "\n" //$NON-NLS-1$
					+ (skipCount == 0 ? Messages.ImportFileSelectionPage_no_format_selected
							: skipCount == 1 ? Messages.ImportFileSelectionPage_one_format_selected
									: NLS.bind(Messages.ImportFileSelectionPage_n_formats_selected, skipCount)));
		}

		@Override
		protected void okPressed() {
			result = new HashSet<String>();
			for (Map.Entry<String, Button> entry : buttonMap.entrySet())
				if (entry.getValue().getSelection())
					result.add(entry.getKey());
			super.okPressed();
		}

		/**
		 * @return the result
		 */
		public HashSet<String> getResult() {
			return result;
		}

		public void widgetSelected(SelectionEvent e) {
			updateButtons();
		}

		public void widgetDefaultSelected(SelectionEvent e) {
			// do nothing
		}

	}

	private static final class DcimLabelProvider extends ZColumnLabelProvider {

		@Override
		public String getText(Object element) {
			if (element instanceof ImportNode) {
				ImportNode importNode = (ImportNode) element;
				int count = importNode.count;
				return importNode.label + NLS.bind(Messages.ImportFromDeviceWizard_n_images, count,
						(count == 1) ? importNode.singular : importNode.plural);
			}
			return element.toString();
		}

		@Override
		public Image getImage(Object element) {
			return ((element instanceof ImportNode) && ((ImportNode) element).type < 0) ? Icons.folder.getImage()
					: null;
		}
	}

	private final static class DcimContentProvider implements ITreeContentProvider {

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// do nothing
		}

		public void dispose() {
			// do nothing
		}

		public Object[] getElements(Object inputElement) {
			return (inputElement instanceof ImportNode[]) ? (ImportNode[]) inputElement : EMPTY;
		}

		public boolean hasChildren(Object element) {
			return (element instanceof ImportNode) && !((ImportNode) element).children.isEmpty();
		}

		public Object getParent(Object element) {
			return (element instanceof ImportNode) ? ((ImportNode) element).parent : null;
		}

		public Object[] getChildren(Object parentElement) {
			return (parentElement instanceof ImportNode) ? ((ImportNode) parentElement).children.toArray() : EMPTY;
		}
	}

	private static class ImportNode {
		String plural;
		String singular;
		List<ImportNode> children = new ArrayList<ImportNode>();
		ImportNode parent;
		String label;
		int type;
		int value;
		private File folder;
		private int count;
		long minTime;
		long maxTime;
		private final File[] files;

		public ImportNode(ImportNode parent, String label, int type, int value, File folder, File[] files, int count,
				String plural, String singular) {
			this.parent = parent;
			this.label = label;
			this.type = type;
			this.value = value;
			this.folder = folder;
			this.files = files;
			this.count = count;
			this.plural = plural;
			this.singular = singular;
		}

		public void add(ImportNode child) {
			children.add(child);
		}

		public File[] getMemberFiles() {
			return folder != null ? folder.listFiles() : files;
		}

	}

	private static final Object[] EMPTY = new Object[0];
	private static final String DETECTDUPLICATES = "detectDuplicates"; //$NON-NLS-1$
	private static final String REMOVEMEDIA = "removeMedia"; //$NON-NLS-1$
	private static final String SKIPPOLICY = "skipPolicy"; //$NON-NLS-1$
	private static final String SKIPPEDFORMATS = "skippedFormats"; //$NON-NLS-1$
	private static final SimpleDateFormat sdf = new SimpleDateFormat(Messages.ImportFileSelectionPage_import_date_mask);
	private CheckboxTreeViewer importViewer;
	private GregorianCalendar calendar = new GregorianCalendar();
	private long lastImportTimestamp = -1L;
	private boolean removeMedia;
	private CheckboxButton duplicatesButton;
	private CheckboxButton removeMediaButton;
	private final boolean media;
	private Combo skipCombo;
	private Text skipFormatsField;
	private Set<String> skippedFormats;
	private Button lastImportButton;
	private Button manageMediaButton;
	private Label volumeLabel;
	private final boolean eject;

	public ImportFileSelectionPage(String pageName, boolean media, boolean eject) {
		super(pageName);
		this.media = media;
		this.eject = eject;
	}

	@SuppressWarnings("unused")
	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		// comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout(1, false));
		final Composite viewerComp = new Composite(comp, SWT.NONE);
		viewerComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		viewerComp.setLayout(new GridLayout());
		if (media) {
			volumeLabel = new Label(viewerComp, SWT.NONE);
			volumeLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		}
		new Label(viewerComp, SWT.NONE).setText(Messages.ImportFromDeviceWizard_modification_dates);
		Composite innerComp = new Composite(viewerComp, SWT.NONE);
		innerComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		innerComp.setLayout(new GridLayout(2, false));
		ExpandCollapseGroup expandCollapseGroup = new ExpandCollapseGroup(innerComp, SWT.NONE);
		new Label(innerComp, SWT.NONE);
		importViewer = new CheckboxTreeViewer(innerComp, SWT.BORDER | SWT.V_SCROLL);
		expandCollapseGroup.setViewer(importViewer);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.heightHint = 200;
		importViewer.getControl().setLayoutData(layoutData);
		importViewer.setContentProvider(new DcimContentProvider());
		importViewer.setLabelProvider(new DcimLabelProvider());
		importViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				checkHierarchy(event.getElement(), event.getChecked());
				validatePage();
				if (lastImportButton != null)
					lastImportButton.setEnabled(lastImportTimestamp >= 0);
			}
		});
		AllNoneGroup selectComp = new AllNoneGroup(innerComp, new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.widget.getData() == AllNoneGroup.ALL) {
					ImportNode[] rootElements = (ImportNode[]) importViewer.getInput();
					importViewer.setCheckedElements(rootElements);
					for (ImportNode node : rootElements)
						checkHierarchy(node, true);
				} else
					importViewer.setCheckedElements(new ImportNode[0]);
				validatePage();
				if (lastImportButton != null)
					lastImportButton.setEnabled(lastImportTimestamp >= 0);
			}
		});
		if (media) {
			File[] dcims = ((ImportFromDeviceWizard) getWizard()).getDcims();
			lastImportButton = WidgetFactory.createPushButton(selectComp,
					Messages.ImportFromDeviceWizard_all_since_last_import, SWT.BEGINNING);
			Label label = new Label(selectComp, SWT.SEPARATOR | SWT.HORIZONTAL);
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			manageMediaButton = WidgetFactory.createPushButton(selectComp,
					Messages.ImportFileSelectionPage_manage_media, SWT.BEGINNING);
			manageMediaButton.setToolTipText(Messages.ImportFileSelectionPage_manage_media_tooltip);
			final Meta meta = Core.getCore().getDbManager().getMeta(true);
			String key = null;
			LastDeviceImport lastImport = null;
			if (dcims.length > 0) {
				lastImportButton.setToolTipText(Messages.ImportFileSelectionPage_name_each_volume_differently);
				key = Core.getCore().getVolumeManager().getVolumeForFile(dcims[0]);
				lastImport = meta.getLastDeviceImport(key);
				if (lastImport != null)
					lastImportTimestamp = lastImport.getTimestamp();
				updateVolumeLabel(key);
			}
			if (lastImportTimestamp >= 0)
				lastImportButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (lastImportTimestamp >= 0) {
							markSinceLastImport();
							validatePage();
						}
					}
				});
			else
				lastImportButton.setEnabled(false);
			if (meta.getLastDeviceImport() != null && !meta.getLastDeviceImport().isEmpty() || key != null) {
				final String k = key;
				manageMediaButton.addSelectionListener(new SelectionAdapter() {

					@Override
					public void widgetSelected(SelectionEvent e) {
						List<LastDeviceImport> mediaList = new ArrayList<LastDeviceImport>();
						if (meta.getLastDeviceImport() != null)
							for (LastDeviceImport imp : meta.getLastDeviceImport().values())
								mediaList.add(new LastDeviceImportImpl(imp.getVolume(), imp.getTimestamp(),
										imp.getDescription(), imp.getOwner()));
						if (k != null
								&& (meta.getLastDeviceImport() == null || !meta.getLastDeviceImport().containsKey(k)))
							mediaList.add(new LastDeviceImportImpl(k, 0L, null, null));
						MediaDialog dialog = new MediaDialog(getContainer().getShell(), mediaList);
						if (dialog.open() == MediaDialog.OK) {
							updatePage();
							updateVolumeLabel(k);
							((ImportFromDeviceWizard) getWizard()).updateAuthor();
						}
					}
				});
			} else
				manageMediaButton.setEnabled(false);
		}
		duplicatesButton = WidgetFactory.createCheckButton(innerComp, Messages.ImportFromDeviceWizard_dont_import_dupes,
				new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 2, 1));
		Composite skipComp = new Composite(comp, SWT.NONE);
		skipComp.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		Label label = new Label(skipComp, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		label.setText(Messages.ImportFileSelectionPage_skipped_file_types);
		if (media) {
			skipComp.setLayout(new GridLayout(2, false));
			skipCombo = new Combo(skipComp, SWT.READ_ONLY);
			skipCombo.setItems(SKIPPOLICIES);
			skipCombo.setVisibleItemCount(5);
		} else {
			skipComp.setLayout(new GridLayout(3, false));
			skipFormatsField = new Text(skipComp, SWT.BORDER | SWT.READ_ONLY | SWT.SINGLE);
			skipFormatsField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			Button skipFormatButton = new Button(skipComp, SWT.PUSH);
			skipFormatButton.setText("..."); //$NON-NLS-1$
			skipFormatButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					SkippedFormatsDialog dialog = new SkippedFormatsDialog(getShell(), skippedFormats);
					if (dialog.open() == Dialog.OK) {
						skippedFormats = dialog.getResult();
						skipFormatsField.setText(Core.toStringList(skippedFormats.toArray(), ";")); //$NON-NLS-1$
					}
					super.widgetSelected(e);
				}
			});
		}
		if (eject)
			removeMediaButton = WidgetFactory.createCheckButton(comp, Messages.ImportFromDeviceWizard_remove_media,
					null);
		setControl(comp);
		setHelp(media ? HelpContextIds.IMPORT_FROM_DEVICE_WIZARD_FILE_SELECTION
				: HelpContextIds.IMPORT_NEW_STRUCTURE_WIZARD_FILE_SELECTION);
		setTitle(Messages.ImportFileSelectionPage_file_selection);
		setMessage(Messages.ImportFileSelectionPage_select_the_image_files);
		super.createControl(parent);
		try {
			fillValues();
			validatePage();
		} catch (InvocationTargetException e1) {
			UiActivator.getDefault().logError(Messages.ImportFileSelectionPage_internal_error, e1);
		} catch (InterruptedException e1) {
			getWizard().getContainer().getShell().close();
		}
	}

	private void updateVolumeLabel(String key) {
		if (key != null && volumeLabel != null) {
			StringBuilder sb = new StringBuilder();
			sb.append(Messages.ImportFileSelectionPage_medium).append(key);
			LastDeviceImport imp = Core.getCore().getDbManager().getMeta(true).getLastDeviceImport(key);
			if (imp != null) {
				String owner = imp.getOwner();
				if (owner != null && !owner.isEmpty())
					sb.append(", ").append( //$NON-NLS-1$
							NLS.bind(Messages.ImportFileSelectionPage_owner, owner));
				long timestamp = imp.getTimestamp();
				if (timestamp > 0)
					sb.append(", ") //$NON-NLS-1$
							.append(NLS.bind(Messages.ImportFileSelectionPage_last_import,
									sdf.format(new Date(timestamp))));
			}
			volumeLabel.setText(sb.toString());
			volumeLabel.setAlignment(SWT.CENTER);
		}

	}

	protected void updatePage() {
		if (lastImportButton != null)
			lastImportButton.setEnabled(true);
	}

	private void markSinceLastImport() {
		ImportNode firstNode = null;
		Object[] checkedElements = importViewer.getCheckedElements();
		for (Object element : checkedElements)
			importViewer.setChecked(element, false);
		List<ImportNode> selectedElements = new ArrayList<ImportNode>(100);
		for (ImportNode rootNode : (ImportNode[]) importViewer.getInput()) {
			ImportNode cand = enableAfterTimestamp(rootNode, importViewer, selectedElements);
			if (cand != null && firstNode == null)
				firstNode = cand;
		}
		importViewer.setCheckedElements(selectedElements.toArray());
		for (ImportNode node : selectedElements)
			checkHierarchy(node, true);
		if (firstNode != null)
			importViewer.reveal(firstNode);
		lastImportButton.setEnabled(false);
	}

	private ImportNode enableAfterTimestamp(ImportNode node, CheckboxTreeViewer viewer,
			List<ImportNode> selectedElements) {
		ImportNode firstNode = null;
		switch (node.type) {
		case Calendar.YEAR:
			if (lastImportTimestamp < node.minTime)
				selectedElements.add(node);
			else if (lastImportTimestamp <= node.maxTime) {
				ImportNode cand = enableChildren(node, viewer, selectedElements);
				if (cand != null)
					firstNode = cand;
			}
			break;
		case Calendar.MONTH:
			if (lastImportTimestamp < node.minTime)
				selectedElements.add(node);
			else if (lastImportTimestamp <= node.maxTime) {
				ImportNode cand = enableChildren(node, viewer, selectedElements);
				if (cand != null)
					firstNode = cand;
			}
			break;
		case Calendar.DAY_OF_MONTH:
			if (lastImportTimestamp <= node.maxTime) {
				selectedElements.add(node);
				firstNode = node;
			}
			break;
		default:
			enableChildren(node, viewer, selectedElements);
			break;
		}
		return firstNode;
	}

	private ImportNode enableChildren(ImportNode node, CheckboxTreeViewer viewer, List<ImportNode> selectedElements) {
		ImportNode firstNode = null;
		ITreeContentProvider contentProvider = (ITreeContentProvider) viewer.getContentProvider();
		for (Object child : contentProvider.getChildren(node)) {
			ImportNode cand = enableAfterTimestamp((ImportNode) child, viewer, selectedElements);
			if (cand != null && firstNode == null)
				firstNode = cand;
		}
		return firstNode;
	}

	protected boolean needsAdvancedOptions() {
		if (media) {
			int index = skipCombo.getSelectionIndex();
			return index != 1 && index != 2;
		}
		return false;
	}

	void checkHierarchy(Object element, boolean checked) {
		importViewer.setGrayChecked(element, false);
		importViewer.setChecked(element, checked);
		if (element instanceof ImportNode) {
			List<ImportNode> children = ((ImportNode) element).children;
			for (ImportNode child : children)
				checkHierarchy(child, checked);
			ImportNode parent = ((ImportNode) element).parent;
			while (parent != null) {
				children = parent.children;
				boolean allChecked = true;
				boolean someChecked = false;
				for (ImportNode child : children) {
					if (importViewer.getGrayed(child)) {
						allChecked = false;
						someChecked = true;
					} else {
						boolean childChecked = importViewer.getChecked(child);
						allChecked &= childChecked;
						someChecked |= childChecked;
					}
				}
				if (allChecked) {
					importViewer.setGrayChecked(parent, false);
					importViewer.setChecked(parent, true);
				} else if (someChecked) {
					importViewer.setGrayChecked(parent, true);
				} else {
					importViewer.setGrayChecked(parent, false);
					importViewer.setChecked(parent, false);
				}
				parent = parent.parent;
			}
		}
	}

	private void fillValues() throws InvocationTargetException, InterruptedException {
		ImportFromDeviceWizard wizard = (ImportFromDeviceWizard) getWizard();
		IDialogSettings dialogSettings = wizard.getDialogSettings();
		ImportNode[] rootElements = createInput(wizard.getFiles(), wizard.getDcims());
		importViewer.setInput(rootElements);
		importViewer.expandAll();
		if (lastImportButton != null && lastImportButton.isEnabled() && lastImportTimestamp >= 0)
			markSinceLastImport();
		else
			for (ImportNode node : rootElements)
				importViewer.setSubtreeChecked(node, true);
		duplicatesButton.setSelection(dialogSettings.getBoolean(DETECTDUPLICATES));
		removeMedia = dialogSettings.getBoolean(REMOVEMEDIA);
		if (removeMediaButton != null)
			removeMediaButton.setSelection(removeMedia);
		if (skipCombo != null) {
			try {
				skipCombo.select(dialogSettings.getInt(SKIPPOLICY));
			} catch (NumberFormatException e) {
				// do nothing
			}
		}
		if (skipFormatsField != null) {
			String[] formats = dialogSettings.getArray(SKIPPEDFORMATS);
			if (formats == null)
				skippedFormats = new HashSet<String>(3);
			else {
				skipFormatsField.setText(Core.toStringList(formats, ";")); //$NON-NLS-1$
				skippedFormats = new HashSet<String>(Arrays.asList(formats));
			}
		}
	}

	private ImportNode[] createInput(final File[] files, final File[] dcims)
			throws InvocationTargetException, InterruptedException {
		final FileNameExtensionFilter imageFilter = new FileNameExtensionFilter(
				ImageConstants.getSupportedImageFileExtensionsGroups(true), true);
		final List<ImportNode> nodes = new ArrayList<ImportNode>();
		final ZProgressMonitorDialog dialog = new ZProgressMonitorDialog(getShell());
		dialog.create();
		dialog.getShell()
				.setText(Constants.APPLICATION_NAME + " - " + Messages.ImportFileSelectionPage_import_preparation); //$NON-NLS-1$
		dialog.run(true, true, new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				monitor.beginTask(Messages.ImportFileSelectionPage_analyzing_folder_contents, IProgressMonitor.UNKNOWN);
				for (int i = 0; i < dcims.length; i++) {
					File file = dcims[i];
					ImportNode node = new ImportNode(null, file.getAbsolutePath(), -1, 0, file, null, 0,
							Messages.ImportFileSelectionPage_photos, Messages.ImportFileSelectionPage_photo);
					nodes.add(node);
					node.count = addChildren(node, file, null, imageFilter, monitor);
					if (monitor.isCanceled())
						throw new InterruptedException();
				}
				if (files != null && files.length > 0) {
					ImportNode node = new ImportNode(null, Messages.ImportFileSelectionPage_dropped_files, -1, 0, null,
							files, 0, Messages.ImportFileSelectionPage_photos, Messages.ImportFileSelectionPage_photo);
					nodes.add(node);
					node.count = addChildren(node, null, files, imageFilter, monitor);
					if (monitor.isCanceled())
						throw new InterruptedException();
				}
				for (IMediaSupport ms : CoreActivator.getDefault().getMediaSupport()) {
					FileNameExtensionFilter foreignFilter = new FileNameExtensionFilter(ms.getFileExtensions());
					for (int i = 0; i < dcims.length; i++) {
						File file = media ? ms.getMediaFolder(dcims[i]) : dcims[i];
						ImportNode node = new ImportNode(null, file.getAbsolutePath(), -1, 0, file, null, 0,
								ms.getPlural(), ms.getName());
						nodes.add(node);
						node.count = addChildren(node, file, null, foreignFilter, monitor);
						if (monitor.isCanceled())
							throw new InterruptedException();
					}
					if (files != null && files.length > 0) {
						ImportNode node = new ImportNode(null, Messages.ImportFileSelectionPage_dropped_files, -1, 0,
								null, files, 0, ms.getPlural(), ms.getName());
						nodes.add(node);
						node.count = addChildren(node, null, files, foreignFilter, monitor);
						if (monitor.isCanceled())
							throw new InterruptedException();
					}
				}
				monitor.done();
			}
		});
		return nodes.toArray(new ImportNode[nodes.size()]);
	}

	private int addChildren(ImportNode node, File folder, File[] files, FileFilter imageFilter,
			IProgressMonitor monitor) {
		int[] years = new int[130];
		long[] mins = new long[130];
		for (int i = 0; i < mins.length; i++)
			mins[i] = Long.MAX_VALUE;
		long[] maxs = new long[130];
		File[] listFiles;
		if (folder != null)
			listFiles = folder.listFiles(imageFilter);
		else {
			List<File> filtered = new ArrayList<File>(files.length);
			for (File file : files)
				if (imageFilter.accept(file))
					filtered.add(file);
			listFiles = filtered.toArray(new File[filtered.size()]);
		}
		int n = 0;
		if (listFiles != null) {
			for (File child : listFiles) {
				if (monitor.isCanceled())
					break;
				if (child.isDirectory()) {
					ImportNode childNode = new ImportNode(node, child.getName(), -1, 0, child, null, 0, node.plural,
							node.singular);
					int m = addChildren(childNode, child, null, imageFilter, monitor);
					if (monitor.isCanceled())
						break;
					if (m > 0) {
						childNode.count = m;
						n += m;
						node.add(childNode);
					}
				} else {
					long lastModified = child.lastModified();
					calendar.setTimeInMillis(lastModified);
					int y = calendar.get(Calendar.YEAR) - 1970;
					years[y] += 1;
					mins[y] = Math.min(mins[y], lastModified);
					maxs[y] = Math.max(maxs[y], lastModified);
					n += 1;
				}
			}
			for (int i = 0; i < years.length; i++) {
				if (years[i] > 0) {
					ImportNode childNode = new ImportNode(node, String.valueOf(i + 1970), Calendar.YEAR, i + 1970,
							folder, files, years[i], node.plural, node.singular);
					childNode.minTime = mins[i];
					childNode.maxTime = maxs[i];
					node.add(childNode);
					addChildrenInPeriod(childNode, folder, files, imageFilter, monitor);
					if (monitor.isCanceled())
						break;
				}
			}
		} else
			node.label += " - " //$NON-NLS-1$
					+ Messages.ImportFromDeviceWizard_bad_connection;
		return n;
	}

	private void addChildrenInPeriod(ImportNode parent, File folder, File[] files, FileFilter imageFilter,
			IProgressMonitor monitor) {
		int[] dates = new int[32];
		long[] mins = new long[32];
		for (int i = 0; i < mins.length; i++)
			mins[i] = Long.MAX_VALUE;
		long[] maxs = new long[32];
		for (File child : folder != null ? folder.listFiles(imageFilter) : files) {
			if (monitor.isCanceled())
				break;
			long lastModified = child.lastModified();
			calendar.setTimeInMillis(lastModified);
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH);
			switch (parent.type) {
			case Calendar.YEAR:
				if (year == parent.value) {
					dates[month] += 1;
					mins[month] = Math.min(mins[month], lastModified);
					maxs[month] = Math.max(maxs[month], lastModified);
				}
				break;
			case Calendar.MONTH:
				if (month == parent.value && year == parent.parent.value) {
					int day = calendar.get(Calendar.DAY_OF_MONTH);
					dates[day] += 1;
					mins[day] = Math.min(mins[day], lastModified);
					maxs[day] = Math.max(maxs[day], lastModified);
				}
				break;
			}
		}
		int type = (parent.type == Calendar.YEAR) ? Calendar.MONTH : Calendar.DAY_OF_MONTH;
		for (int dm = 0; dm < dates.length; dm++) {
			if (monitor.isCanceled())
				break;
			if (dates[dm] > 0) {
				String label = (type == Calendar.MONTH) ? Constants.DATEFORMATS.getMonths()[dm] : String.valueOf(dm);
				if (type == Calendar.DAY_OF_MONTH) {
					int m = parent.value;
					int y = parent.parent.value;
					label = Constants.DATEFORMATS.getWeekdays()[new GregorianCalendar(y, m, dm)
							.get(Calendar.DAY_OF_WEEK)].substring(0, 3) + ", " + label; //$NON-NLS-1$
				}
				ImportNode childNode = new ImportNode(parent, label, type, dm, folder, files, dates[dm], parent.plural,
						parent.singular);
				childNode.minTime = mins[dm];
				childNode.maxTime = maxs[dm];
				parent.add(childNode);
				if ((parent.type == Calendar.YEAR))
					addChildrenInPeriod(childNode, folder, files, imageFilter, monitor);
			}
		}
	}

	@Override
	protected void validatePage() {
		Object[] checkedElements = importViewer.getCheckedElements();
		if (checkedElements.length == 0) {
			setErrorMessage(Messages.ImportFromDeviceWizard_please_select_files);
			setPageComplete(false);
		} else {
			for (Object importNode : checkedElements) {
				if (((ImportNode) importNode).count > 0) {
					setErrorMessage(null);
					setPageComplete(true);
					return;
				}
			}
			setErrorMessage(Messages.ImportFileSelectionPage_no_images_to_import);
			setPageComplete(false);
		}
	}

	public void performFinish(ImportFromDeviceData importData) {
		IDialogSettings dialogSettings = getWizard().getDialogSettings();
		boolean detectDuplicates = duplicatesButton.getSelection();
		importData.setDetectDuplicates(detectDuplicates);
		dialogSettings.put(DETECTDUPLICATES, detectDuplicates);
		if (removeMediaButton != null) {
			removeMedia = removeMediaButton.getSelection();
			importData.setRemoveMedia(removeMedia);
		} else
			importData.setRemoveMedia(false);
		dialogSettings.put(REMOVEMEDIA, removeMedia);
		int policy = 0;
		if (skipCombo != null) {
			policy = Math.max(0, skipCombo.getSelectionIndex());
			dialogSettings.put(SKIPPOLICY, policy);
		}
		importData.setSkipPolicy(policy);
		if (skipFormatsField != null) {
			String[] formats = skippedFormats.toArray(new String[skippedFormats.size()]);
			Arrays.sort(formats);
			dialogSettings.put(SKIPPEDFORMATS, formats);
		}
		List<File> selectedFiles = getSelectedFiles();
		if (selectedFiles != null)
			importData.setFileInput(new FileInput(selectedFiles, false));
		else
			AcousticMessageDialog.openError(getShell(), Messages.ImportFromDeviceWizard_Import_error,
					Messages.ImportFromDeviceWizard_device_seems_to_be_offline);
	}

	public List<File> getSelectedFiles() {
		List<File> selectedFiles = new ArrayList<File>(100);
		Object[] checkedElements = importViewer.getCheckedElements();
		for (Object checked : checkedElements)
			if (checked instanceof ImportNode) {
				ImportNode node = ((ImportNode) checked);
				if (node.type == Calendar.DAY_OF_MONTH) {
					int day = node.value;
					int month = node.parent.value;
					int year = node.parent.parent.value;
					File[] listFiles = node.getMemberFiles();
					if (listFiles != null)
						for (File file : listFiles) {
							long lastModified = file.lastModified();
							calendar.setTimeInMillis(lastModified);
							if (calendar.get(Calendar.DAY_OF_MONTH) == day && calendar.get(Calendar.MONTH) == month
									&& calendar.get(Calendar.YEAR) == year)
								selectedFiles.add(file);
						}
					else
						return null;
				}
			}
		return selectedFiles;
	}
}
