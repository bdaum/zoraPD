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
package com.bdaum.zoom.ui.internal.wizards;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
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
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
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
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.meta.LastDeviceImport;
import com.bdaum.zoom.cat.model.meta.LastDeviceImportImpl;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.FileInput;
import com.bdaum.zoom.core.internal.FileNameExtensionFilter;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.core.internal.ImportFromDeviceData;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.mtp.ObjectFilter;
import com.bdaum.zoom.mtp.StorageObject;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.dialogs.ZProgressMonitorDialog;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.dialogs.AllNoneGroup;
import com.bdaum.zoom.ui.internal.dialogs.MediaDialog;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.ExpandCollapseGroup;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.wizards.ColoredWizardPage;

@SuppressWarnings("restriction")
public class ImportFileSelectionPage extends ColoredWizardPage {

	public class Digest {

		private String originalFileName;
		private Date dateTimeOriginal;

		public Digest(String originalFileName, Date dateTimeOriginal) {
			this.originalFileName = originalFileName;
			this.dateTimeOriginal = dateTimeOriginal;
		}

		@Override
		public int hashCode() {
			if (dateTimeOriginal == null)
				return originalFileName.hashCode();
			return 31 * originalFileName.hashCode() * dateTimeOriginal.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Digest) {
				if (dateTimeOriginal == null)
					return originalFileName.equals(((Digest) obj).originalFileName);
				return originalFileName.equals(((Digest) obj).originalFileName)
						&& dateTimeOriginal.equals(((Digest) obj).dateTimeOriginal);
			}
			return false;
		}

		public String getOriginalFileName() {
			return originalFileName;
		}

		public void setOriginalFileName(String originalFileName) {
			this.originalFileName = originalFileName;
		}

		public Date getDateTimeOriginal() {
			return dateTimeOriginal;
		}

		public void setDateTimeOriginal(Date dateTimeOriginal) {
			this.dateTimeOriginal = dateTimeOriginal;
		}

	}

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
			getButton(OK).setEnabled(validate());
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
			return element.toString();
		}

		@Override
		public Image getImage(Object element) {
			if ((element instanceof ImportNode))
				switch (((ImportNode) element).type) {
				case Calendar.YEAR:
				case Calendar.MONTH:
				case Calendar.DAY_OF_MONTH:
				case FILE:
					return null;
				case NODATES:
					return Icons.images.getImage();
				default:
					return Icons.folder.getImage();
				}
			return null;
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

	private class ImportNode {
		String plural;
		String singular;
		List<ImportNode> children = new ArrayList<ImportNode>();
		ImportNode parent;
		String label;
		int type;
		int value;
		private int count;
		long minTime;
		long maxTime;
		List<StorageObject> missing;
		StorageObject[] memberFiles;

		public ImportNode(ImportNode parent, String label, int type, int value, int count, String plural,
				String singular) {
			this.parent = parent;
			this.label = label;
			this.type = type;
			this.value = value;
			this.count = count;
			this.plural = plural;
			this.singular = singular;
			++nodeCount;
		}

		public String toString() {
			if (type == FILE)
				return label;
			return label + NLS.bind(Messages.ImportFromDeviceWizard_n_images, count, count == 1 ? singular : plural);
		}

		public void add(ImportNode child) {
			children.add(child);
		}

		public StorageObject[] getMissing() {
			return missing == null ? EMPTYFILES : missing.toArray(new StorageObject[missing.size()]);
		}

		public String getPath() {
			if (memberFiles == null || memberFiles.length == 0)
				return ""; //$NON-NLS-1$
			return memberFiles[0].getAbsolutePath();
		}

	}

	private static final Object[] EMPTY = new Object[0];
	private static final StorageObject[] EMPTYFILES = new StorageObject[0];
	private static final String DETECTDUPLICATES = "detectDuplicates"; //$NON-NLS-1$
	private static final String REMOVEMEDIA = "removeMedia"; //$NON-NLS-1$
	private static final String SKIPPOLICY = "skipPolicy"; //$NON-NLS-1$
	private static final String SKIPPEDFORMATS = "skippedFormats"; //$NON-NLS-1$
	private static final SimpleDateFormat sdf = new SimpleDateFormat(Messages.ImportFileSelectionPage_import_date_mask);
	private static final int NODATES = 1000;
	private static final int FILE = 999;
	private CheckboxTreeViewer importViewer;
	private GregorianCalendar calendar = new GregorianCalendar();
	private long lastImportTimestamp = -1L;
	private String lastImportPath = ""; //$NON-NLS-1$
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
	private Button diffButton;
	private ProgressBar progressbar;
	private int nodeCount = 0;
	private ImportNode[] rootElements;
	private int progress;
	protected IProgressMonitor monitor;
	private List<StorageObject> selectedFiles;
	private Label groupedByLabel;
	private boolean byDate = false;
	private boolean byFilename = false;
	private Boolean presetDuplicates;
	private Boolean presetRemoveMedia;
	private Integer presetSkipPolicy;
	private String presetFormats;

	public ImportFileSelectionPage(String pageName, boolean media, boolean eject) {
		super(pageName);
		this.media = media;
		this.eject = eject;
	}

	@SuppressWarnings("unused")
	@Override
	public void createControl(Composite parent) {
		Composite comp = createComposite(parent, 1);
		final Composite viewerComp = new Composite(comp, SWT.NONE);
		viewerComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		viewerComp.setLayout(new GridLayout());
		if (media) {
			volumeLabel = new Label(viewerComp, SWT.NONE);
			volumeLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			volumeLabel.setFont(JFaceResources.getBannerFont());
		}
		groupedByLabel = new Label(viewerComp, SWT.NONE);
		groupedByLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
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
				selectedFiles = null;
				checkHierarchy(event.getElement(), event.getChecked(), false);
				validatePage();
				if (lastImportButton != null)
					lastImportButton.setEnabled(lastImportTimestamp >= 0 || !lastImportPath.isEmpty());
			}
		});
		UiUtilities.installDoubleClickExpansion(importViewer);
		AllNoneGroup selectComp = new AllNoneGroup(innerComp, new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.widget.getData() == AllNoneGroup.ALL) {
					ImportNode[] rootElements = (ImportNode[]) importViewer.getInput();
					importViewer.setCheckedElements(rootElements);
					for (ImportNode node : rootElements)
						checkHierarchy(node, true, false);
				} else
					importViewer.setCheckedElements(new ImportNode[0]);
				validatePage();
				if (lastImportButton != null)
					lastImportButton.setEnabled(lastImportTimestamp >= 0 || !lastImportPath.isEmpty());
			}
		});
		if (media) {
			StorageObject[] dcims = ((ImportFromDeviceWizard) getWizard()).getDcims();
			lastImportButton = WidgetFactory.createPushButton(selectComp,
					Messages.ImportFromDeviceWizard_all_since_last_import, SWT.BEGINNING);
			diffButton = WidgetFactory.createPushButton(selectComp, Messages.ImportFileSelectionPage_Difference,
					SWT.BEGINNING);
			diffButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					progressbar.setVisible(true);
					calculateDiff();
					progressbar.setVisible(false);
				}
			});
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
				key = dcims[0].getMedium();
				lastImport = meta.getLastDeviceImport(key);
				if (lastImport != null) {
					lastImportTimestamp = lastImport.getTimestamp();
					if (lastImport.getPath() != null)
						lastImportPath = lastImport.getPath();
				} else if (!key.isEmpty())
					editMedia(meta, key, false);
				else
					AcousticMessageDialog.openInformation(getShell(),
							Messages.ImportFileSelectionPage_import_from_device,
							Messages.ImportFileSelectionPage_assign_volume_name);
				updateVolumeLabel(key);
			}
			if (lastImportTimestamp >= 0 || !lastImportPath.isEmpty())
				lastImportButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (lastImportTimestamp >= 0 || !lastImportPath.isEmpty()) {
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
						editMedia(meta, k, true);
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
		if (media) {
			progressbar = new ProgressBar(comp, SWT.HORIZONTAL);
			progressbar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			progressbar.setVisible(false);
		}

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
			if (e1.getMessage() != null && !e1.getMessage().isEmpty())
				parent.getDisplay().syncExec(() -> AcousticMessageDialog.openError(getShell(),
						Messages.ImportFileSelectionPage_import_from_device, e1.getMessage()));
			((WizardDialog) getWizard().getContainer()).close();
		}
	}

	protected void editMedia(Meta meta, String key, boolean all) {
		List<LastDeviceImport> mediaList = new ArrayList<LastDeviceImport>();
		if (all && meta.getLastDeviceImport() != null)
			for (LastDeviceImport imp : meta.getLastDeviceImport().values()) {
				LastDeviceImportImpl last = new LastDeviceImportImpl(imp.getVolume(), imp.getTimestamp(),
						imp.getDescription(), imp.getOwner(), imp.getPath(), imp.getDetectDuplicates(),
						imp.getRemoveMedia(), imp.getSkipPolicy(), imp.getTargetDir(), imp.getSubfolders(),
						imp.getDeepSubfolders(), imp.getSelectedTemplate(), imp.getCue(), imp.getPrefix(),
						imp.getPrivacy());
				last.setSkippedFormats(imp.getSkippedFormats());
				last.setKeywords(imp.getKeywords());
				mediaList.add(last);
			}
		if (key != null && (meta.getLastDeviceImport() == null || !meta.getLastDeviceImport().containsKey(key)))
			mediaList.add(new LastDeviceImportImpl(key, 0L, null, null, null, null, null, null, null, null, null, null,
					null, null, null));
		MediaDialog dialog = new MediaDialog(getContainer().getShell(), mediaList, key, !all);
		if (dialog.open() == MediaDialog.OK) {
			updatePage();
			updateVolumeLabel(key);
			((ImportFromDeviceWizard) getWizard()).updateValues();
		}
	}

	protected void calculateDiff() {
		IDbManager dbManager = Core.getCore().getDbManager();
		List<AssetImpl> assets = dbManager.obtainAssets();
		int size = assets.size();
		int increment = nodeCount == 0 ? 1 : size / nodeCount;
		int work = 3 * nodeCount;
		progress = 0;
		progressbar.setMaximum(work);
		Set<Digest> cataloged = new HashSet<>(size * 3 / 2);
		int k = 0;
		for (Asset asset : assets) {
			if (asset.getOriginalFileName() != null && asset.getDateTime() != null)
				cataloged.add(new Digest(asset.getOriginalFileName(), asset.getDateTime()));
			if (++k % increment == 0)
				progressbar.setSelection(++progress);
		}
		Digest cand = new Digest(null, null);
		calculateDiff(rootElements, cand, cataloged);
		visualizeDiff(rootElements, importViewer);
	}

	private void visualizeDiff(ImportNode[] nodes, CheckboxTreeViewer viewer) {
		for (ImportNode node : nodes) {
			if (node.type == Calendar.DAY_OF_MONTH) {
				StorageObject[] memberFiles = node.memberFiles;
				if (memberFiles != null) {
					int mCount = node.missing == null ? 0 : node.missing.size();
					if (mCount == 0)
						viewer.setGrayChecked(node, false);
					else if (mCount != memberFiles.length)
						viewer.setGrayChecked(node, true);
					else {
						viewer.setGrayed(node, false);
						viewer.setChecked(node, true);
					}
				}
			} else if (node.type == FILE) {
				int mCount = node.missing == null ? 0 : node.missing.size();
				viewer.setChecked(node, mCount != 1);
			} else if (node.children != null && !node.children.isEmpty()) {
				ImportNode[] children = node.children.toArray(new ImportNode[node.children.size()]);
				visualizeDiff(children, viewer);
				int checked = 0;
				int grayed = 0;
				for (ImportNode child : children)
					if (viewer.getChecked(child)) {
						++checked;
						if (viewer.getGrayed(child))
							++grayed;
					}
				if (checked == 0)
					viewer.setGrayChecked(node, false);
				else if (checked < children.length || grayed != 0)
					viewer.setGrayChecked(node, true);
				else {
					viewer.setChecked(node, true);
					viewer.setGrayed(node, false);
				}
			}
			progressbar.setSelection(++progress);
		}
	}

	private void calculateDiff(ImportNode[] nodes, Digest cand, Set<Digest> cataloged) {
		for (ImportNode node : nodes) {
			if (node.type == Calendar.DAY_OF_MONTH) {
				StorageObject[] memberFiles = node.memberFiles;
				if (memberFiles != null) {
					if (node.missing != null)
						node.missing.clear();
					for (StorageObject file : memberFiles) {
						cand.setOriginalFileName(computeRelativePath(file));
						cand.setDateTimeOriginal(new Date(file.lastModified()));
						if (!cataloged.contains(cand)) {
							if (node.missing == null)
								node.missing = new ArrayList<>();
							node.missing.add(file);
						}
					}
				}
			} else if (node.type == FILE) {
				if (node.missing != null)
					node.missing.clear();
				StorageObject file = node.memberFiles[0];
				cand.setOriginalFileName(computeRelativePath(file));
				cand.setDateTimeOriginal(null);
				if (!cataloged.contains(cand)) {
					if (node.missing == null)
						node.missing = new ArrayList<>();
					node.missing.add(file);
				}
			} else if (node.children != null && !node.children.isEmpty())
				calculateDiff(node.children.toArray(new ImportNode[node.children.size()]), cand, cataloged);
			progressbar.setSelection(++progress);
		}
	}

	private static String computeRelativePath(StorageObject file) {
		String path = file.getAbsolutePath();
		int p = path.indexOf("DCIM/"); //$NON-NLS-1$
		if (p >= 0)
			return path.substring(p + 5);
		return path;
	}

	private void updateVolumeLabel(String key) {
		if (key != null && volumeLabel != null) {
			String descr = null;
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
				descr = imp.getDescription();
			}
			volumeLabel.setText(sb.toString());
			volumeLabel.setToolTipText(descr == null ? "" : descr); //$NON-NLS-1$
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
		for (ImportNode node : selectedElements) {
			if (firstNode == null)
				firstNode = node;
			checkHierarchy(node, true, false);
		}
		if (firstNode != null)
			importViewer.reveal(firstNode);
		lastImportButton.setEnabled(false);
	}

	private ImportNode enableAfterTimestamp(ImportNode node, CheckboxTreeViewer viewer,
			List<ImportNode> selectedElements) {
		switch (node.type) {
		case Calendar.YEAR:
			if (lastImportTimestamp >= 0 && lastImportTimestamp < node.minTime) {
				selectedElements.add(node);
				break;
			}
			if (lastImportTimestamp >= 0 && lastImportTimestamp <= node.maxTime)
				return enableChildren(node, viewer, selectedElements);
			break;
		case Calendar.MONTH:
			if (lastImportTimestamp >= 0 && lastImportTimestamp < node.minTime) {
				selectedElements.add(node);
				break;
			}
			if (lastImportTimestamp >= 0 && lastImportTimestamp <= node.maxTime)
				return enableChildren(node, viewer, selectedElements);
			break;
		case Calendar.DAY_OF_MONTH:
			if (lastImportTimestamp >= 0 && lastImportTimestamp <= node.maxTime) {
				selectedElements.add(node);
				return node;
			}
			break;
		case NODATES:
			return enableChildren(node, viewer, selectedElements);
		case FILE:
			if (!lastImportPath.isEmpty() && lastImportPath.compareTo(node.getPath()) < 0) {
				selectedElements.add(node);
				return node;
			}
			break;
		default:
			enableChildren(node, viewer, selectedElements);
			break;
		}
		return null;
	}

	private ImportNode enableChildren(ImportNode node, CheckboxTreeViewer viewer, List<ImportNode> selectedElements) {
		ImportNode firstNode = null;
		for (Object child : ((ITreeContentProvider) viewer.getContentProvider()).getChildren(node)) {
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

	void checkHierarchy(Object element, boolean checked, boolean gray) {
		BusyIndicator.showWhile(importViewer.getControl().getDisplay(), () -> {
			doCheckHierarchy(element, checked, gray, true);
		});
	}

	private void doCheckHierarchy(Object element, boolean checked, boolean gray, boolean force) {
		boolean changed = importViewer.getChecked(element) != checked || importViewer.getGrayed(element) != gray;
		importViewer.setGrayed(element, gray);
		importViewer.setChecked(element, checked);
		if (element instanceof ImportNode) {
			ImportNode node = (ImportNode) element;
			if (!gray && changed || force) {
				node.missing = null;
				for (ImportNode child : node.children)
					doCheckHierarchy(child, checked, gray, false);
			}
			ImportNode parent = node.parent;
			while (parent != null) {
				boolean allChecked = true;
				boolean someChecked = false;
				for (ImportNode member : parent.children)
					if (importViewer.getGrayed(member)) {
						allChecked = false;
						someChecked = true;
					} else {
						boolean childChecked = importViewer.getChecked(member);
						allChecked &= childChecked;
						someChecked |= childChecked;
					}
				if (allChecked) {
					importViewer.setGrayed(parent, false);
					importViewer.setChecked(parent, true);
				} else
					importViewer.setGrayChecked(parent, someChecked);
				parent = parent.parent;
			}
		}
	}

	private void fillValues() throws InvocationTargetException, InterruptedException {
		ImportFromDeviceWizard wizard = (ImportFromDeviceWizard) getWizard();
		IDialogSettings dialogSettings = wizard.getDialogSettings();
		rootElements = createInput(wizard.getFiles(), wizard.getDcims());
		importViewer.setInput(rootElements);
		importViewer.expandAll();
		if (lastImportButton != null && lastImportButton.isEnabled()
				&& (lastImportTimestamp >= 0 || !lastImportPath.isEmpty()))
			markSinceLastImport();
		else
			for (ImportNode node : rootElements)
				importViewer.setSubtreeChecked(node, true);
		duplicatesButton.setSelection(presetDuplicates = dialogSettings.getBoolean(DETECTDUPLICATES));
		removeMedia = dialogSettings.getBoolean(REMOVEMEDIA);
		if (removeMediaButton != null)
			removeMediaButton.setSelection(presetRemoveMedia = removeMedia);
		if (skipCombo != null)
			try {
				skipCombo.select(presetSkipPolicy = dialogSettings.getInt(SKIPPOLICY));
			} catch (NumberFormatException e) {
				// do nothing
			}
		if (skipFormatsField != null) {
			String[] formats = dialogSettings.getArray(SKIPPEDFORMATS);
			if (formats == null)
				skippedFormats = new HashSet<String>(3);
			else {
				skipFormatsField.setText(presetFormats = Core.toStringList(formats, ";")); //$NON-NLS-1$
				skippedFormats = new HashSet<String>(Arrays.asList(formats));
			}
		}
		updateValues(wizard.getCurrentDevice());
	}

	public void updateValues(LastDeviceImport current) {
		if (current != null) {
			Boolean dd = current.getDetectDuplicates();
			if (duplicatesButton != null && duplicatesButton.getSelection() == presetDuplicates && dd != null)
				duplicatesButton.setSelection(presetDuplicates = dd);
			Boolean rr = current.getRemoveMedia();
			if (rr != null && removeMediaButton != null && removeMediaButton.getSelection() == presetRemoveMedia)
				removeMediaButton.setSelection(presetRemoveMedia = rr);
			Integer sp = current.getSkipPolicy();
			if (sp != null && skipCombo != null && skipCombo.getSelectionIndex() == presetSkipPolicy)
				skipCombo.select(presetSkipPolicy = sp);
			String[] sf = current.getSkippedFormats();
			if (sf != null && skipFormatsField != null) {
				String formats = Core.toStringList(sf, ";"); //$NON-NLS-1$
				if (skipFormatsField.getText().equals(presetFormats)) {
					skipFormatsField.setText(presetFormats = formats);
					skippedFormats = new HashSet<String>(Arrays.asList(sf));
				}
			}
		}
	}

	private ImportNode[] createInput(final StorageObject[] files, final StorageObject[] dcims)
			throws InvocationTargetException, InterruptedException {
		nodeCount = 0;
		final ObjectFilter imageFilter = new FileNameExtensionFilter(
				ImageConstants.getSupportedImageFileExtensionsGroups(true), true);
		final List<ImportNode> nodes = new ArrayList<ImportNode>();
		final ZProgressMonitorDialog dialog = new ZProgressMonitorDialog(getShell());
		dialog.create();
		dialog.getShell()
				.setText(Constants.APPLICATION_NAME + " - " + Messages.ImportFileSelectionPage_import_preparation); //$NON-NLS-1$
		dialog.run(true, true, new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				ImportFileSelectionPage.this.monitor = monitor;
				try {
					monitor.beginTask(Messages.ImportFileSelectionPage_analyzing_folder_contents,
							IProgressMonitor.UNKNOWN);
					for (int i = 0; i < dcims.length; i++) {
						StorageObject file = dcims[i];
						ImportNode node = new ImportNode(null, file.getAbsolutePath(), -1, 0, 0,
								Messages.ImportFileSelectionPage_photos, Messages.ImportFileSelectionPage_photo);
						nodes.add(node);
						node.count = addChildren(node, file, null, imageFilter, monitor);
						if (monitor.isCanceled())
							throw new InterruptedException();
					}
					if (files != null && files.length > 0) {
						ImportNode node = new ImportNode(null, Messages.ImportFileSelectionPage_dropped_files, -1, 0, 0,
								Messages.ImportFileSelectionPage_photos, Messages.ImportFileSelectionPage_photo);
						nodes.add(node);
						node.count = addChildren(node, null, files, imageFilter, monitor);
						if (monitor.isCanceled())
							throw new InterruptedException();
					}
					for (IMediaSupport ms : CoreActivator.getDefault().getMediaSupport()) {
						ObjectFilter foreignFilter = new FileNameExtensionFilter(ms.getFileExtensions());
						for (int i = 0; i < dcims.length; i++) {
							StorageObject file = media ? ms.getMediaFolder(dcims[i]) : dcims[i];
							if (file == null)
								continue;
							ImportNode node = new ImportNode(null, file.getAbsolutePath(), -1, 0, 0, ms.getPlural(),
									ms.getName());
							nodes.add(node);
							node.count = addChildren(node, file, null, foreignFilter, monitor);
							if (monitor.isCanceled())
								throw new InterruptedException();
						}
						if (files != null && files.length > 0) {
							ImportNode node = new ImportNode(null, Messages.ImportFileSelectionPage_dropped_files, -1,
									0, 0, ms.getPlural(), ms.getName());
							nodes.add(node);
							node.count = addChildren(node, null, files, foreignFilter, monitor);
							if (monitor.isCanceled())
								throw new InterruptedException();
						}
					}
					monitor.done();
				} catch (IOException e) {
					throw new InterruptedException(Messages.ImportFileSelectionPage_io_error_while_scanning);
				} finally {
					ImportFileSelectionPage.this.monitor = null;
				}
			}
		});
		groupedByLabel.setText(byDate
				? byFilename ? Messages.ImportFileSelectionPage_grouped_by_date_or_filename
						: Messages.ImportFromDeviceWizard_modification_dates
				: Messages.ImportFileSelectionPage_grouped_by_filename);
		return nodes.toArray(new ImportNode[nodes.size()]);
	}

	private int addChildren(ImportNode node, StorageObject folder, StorageObject[] files, ObjectFilter imageFilter,
			IProgressMonitor monitor) throws IOException {
		List<StorageObject> noDates = null;
		int[] years = new int[130];
		long[] mins = new long[130];
		for (int i = 0; i < mins.length; i++)
			mins[i] = Long.MAX_VALUE;
		long[] maxs = new long[130];
		StorageObject[] members = folder != null ? filterObjects(folder.listChildren(), imageFilter, true)
				: filterObjects(files, imageFilter, false);
		int n = 0;
		if (members != null) {
			for (StorageObject child : members) {
				if (monitor.isCanceled())
					break;
				if (child.isDirectory()) {
					monitor.subTask(child.getName());
					ImportNode childNode = new ImportNode(node, child.getName(), -1, 0, 0, node.plural, node.singular);
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
					if (lastModified > 0) {
						calendar.setTimeInMillis(lastModified);
						int y = calendar.get(Calendar.YEAR) - 1970;
						++years[y];
						mins[y] = Math.min(mins[y], lastModified);
						maxs[y] = Math.max(maxs[y], lastModified);
						byDate = true;
					} else {
						if (noDates == null)
							noDates = new ArrayList<StorageObject>();
						noDates.add(child);
						byFilename = true;
					}
					++n;
				}
			}
			for (int i = 0; i < years.length; i++)
				if (years[i] > 0) {
					ImportNode childNode = new ImportNode(node, String.valueOf(i + 1970), Calendar.YEAR, i + 1970,
							years[i], node.plural, node.singular);
					childNode.minTime = mins[i];
					childNode.maxTime = maxs[i];
					node.add(childNode);
					addChildrenInPeriod(childNode, folder, members, files, imageFilter, monitor);
					if (monitor.isCanceled())
						break;
				}
			if (noDates != null) {
				Collections.sort(noDates, new Comparator<StorageObject>() {
					@Override
					public int compare(StorageObject o1, StorageObject o2) {
						return o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
					}
				});
				int size = noDates.size();
				ImportNode currentNode = null;
				for (int i = 0; i < size; i++) {
					StorageObject file = noDates.get(i);
					if (i % 36 == 0)
						currentNode = new ImportNode(node, extractFileName(file.getAbsolutePath()), NODATES, 0, 0,
								node.plural, node.singular);
					if (i == size - 1 || i % 36 == 35) {
						currentNode.label += " - " + extractFileName(file.getAbsolutePath()); //$NON-NLS-1$
						currentNode.count = i % 36 + 1;
						node.add(currentNode);
					}
					ImportNode fileNode = new ImportNode(node, file.getName(), FILE, 0, 1, node.plural, node.singular);
					fileNode.memberFiles = new StorageObject[] { file };
					currentNode.add(fileNode);
				}
			}
		} else
			node.label += " - " + Messages.ImportFromDeviceWizard_bad_connection; //$NON-NLS-1$
		return n;
	}

	private static String extractFileName(String path) {
		return path.substring(path.lastIndexOf('/') + 1);
	}

	private static StorageObject[] filterObjects(StorageObject[] files, ObjectFilter imageFilter, boolean skipHidden) {
		if (files == null)
			return null;
		List<StorageObject> filtered = new ArrayList<>(files.length);
		for (StorageObject file : files)
			if ((!skipHidden || !file.isHidden()) && imageFilter.accept(file))
				filtered.add(file);
		return filtered.toArray(new StorageObject[filtered.size()]);
	}

	private void addChildrenInPeriod(ImportNode parent, StorageObject folder, StorageObject[] members,
			StorageObject[] files, ObjectFilter imageFilter, IProgressMonitor monitor) {
		int[] dates = new int[32];
		long[] mins = new long[32];
		for (int i = 0; i < mins.length; i++)
			mins[i] = Long.MAX_VALUE;
		long[] maxs = new long[32];
		for (StorageObject child : members) {
			if (monitor.isCanceled())
				break;
			long lastModified = child.lastModified();
			calendar.setTimeInMillis(lastModified);
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH);
			switch (parent.type) {
			case Calendar.YEAR:
				if (year == parent.value) {
					++dates[month];
					mins[month] = Math.min(mins[month], lastModified);
					maxs[month] = Math.max(maxs[month], lastModified);
				}
				break;
			case Calendar.MONTH:
				if (month == parent.value && year == parent.parent.value) {
					int day = calendar.get(Calendar.DAY_OF_MONTH);
					++dates[day];
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
				if (type == Calendar.DAY_OF_MONTH)
					label = Constants.DATEFORMATS
							.getWeekdays()[new GregorianCalendar(parent.parent.value, parent.value, dm)
									.get(Calendar.DAY_OF_WEEK)].substring(0, 3)
							+ ", " + label; //$NON-NLS-1$
				ImportNode childNode = new ImportNode(parent, label, type, dm, dates[dm], parent.plural,
						parent.singular);
				childNode.minTime = mins[dm];
				childNode.maxTime = maxs[dm];
				parent.add(childNode);
				if ((type == Calendar.MONTH))
					addChildrenInPeriod(childNode, folder, members, files, imageFilter, monitor);
				else
					childNode.memberFiles = members;
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
			for (Object importNode : checkedElements)
				if (((ImportNode) importNode).count > 0) {
					setErrorMessage(null);
					setPageComplete(true);
					return;
				}
			setErrorMessage(Messages.ImportFileSelectionPage_no_images_to_import);
			setPageComplete(false);
		}
	}

	public String performFinish(ImportFromDeviceData importData) {
		ImportFromDeviceWizard wizard = (ImportFromDeviceWizard) getWizard();
		IDialogSettings dialogSettings = wizard.getDialogSettings();
		LastDeviceImport newDevice = wizard.getNewDevice();
		boolean detectDuplicates = duplicatesButton.getSelection();
		importData.setDetectDuplicates(detectDuplicates);
		dialogSettings.put(DETECTDUPLICATES, detectDuplicates);
		newDevice.setDetectDuplicates(detectDuplicates);
		importData.setRemoveMedia(removeMedia = removeMediaButton != null ? removeMediaButton.getSelection() : false);
		dialogSettings.put(REMOVEMEDIA, removeMedia);
		newDevice.setRemoveMedia(removeMedia);
		int policy = 0;
		if (skipCombo != null)
			dialogSettings.put(SKIPPOLICY, policy = Math.max(0, skipCombo.getSelectionIndex()));
		importData.setSkipPolicy(policy);
		newDevice.setSkipPolicy(policy);
		if (skipFormatsField != null) {
			String[] formats = skippedFormats.toArray(new String[skippedFormats.size()]);
			Arrays.sort(formats);
			dialogSettings.put(SKIPPEDFORMATS, formats);
			newDevice.setSkippedFormats(formats);
		}
		List<StorageObject> selectedFiles = getSelectedFiles();
		if (selectedFiles == null)
			return Messages.ImportFromDeviceWizard_device_seems_to_be_offline;
		importData.setFileInput(new FileInput(selectedFiles, false));
		return null;
	}

	public List<StorageObject> getSelectedFiles() {
		if (selectedFiles == null) {
			selectedFiles = new ArrayList<>(100);
			for (Object checked : importViewer.getCheckedElements())
				if (checked instanceof ImportNode) {
					ImportNode node = ((ImportNode) checked);
					if (node.type == Calendar.DAY_OF_MONTH) {
						int day = node.value;
						int month = node.parent.value;
						int year = node.parent.parent.value;
						boolean grayed = importViewer.getGrayed(checked);
						StorageObject[] listFiles = grayed ? node.getMissing() : node.memberFiles;
						if (listFiles != null)
							for (StorageObject file : listFiles) {
								calendar.setTimeInMillis(file.lastModified());
								if (calendar.get(Calendar.DAY_OF_MONTH) == day && calendar.get(Calendar.MONTH) == month
										&& calendar.get(Calendar.YEAR) == year)
									selectedFiles.add(file);
							}
					} else if (node.type == FILE)
						selectedFiles.add(node.memberFiles[0]);
				}
		}
		return selectedFiles;
	}

	public StorageObject getFirstSelectedFile() {
		for (Object checked : importViewer.getCheckedElements())
			if (checked instanceof ImportNode) {
				ImportNode node = ((ImportNode) checked);
				if (node.type == Calendar.DAY_OF_MONTH) {
					int day = node.value;
					int month = node.parent.value;
					int year = node.parent.parent.value;
					boolean grayed = importViewer.getGrayed(checked);
					StorageObject[] listFiles = grayed ? node.getMissing() : node.memberFiles;
					if (listFiles != null)
						for (StorageObject file : listFiles) {
							calendar.setTimeInMillis(file.lastModified());
							if (calendar.get(Calendar.DAY_OF_MONTH) == day && calendar.get(Calendar.MONTH) == month
									&& calendar.get(Calendar.YEAR) == year)
								return file;
						}
				} else if (node.type == FILE)
					return node.memberFiles[0];
			}
		return null;
	}

	public void cancel() {
		if (monitor != null) {
			monitor.setCanceled(true);
			getWizard().performCancel();
		}
	}

}
