package com.bdaum.zoom.ui.internal.wizards;

import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.ImportFromDeviceData;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.ZViewerComparator;
import com.bdaum.zoom.ui.internal.widgets.RadioButtonGroup;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.wizards.ColoredWizardPage;

@SuppressWarnings("restriction")
public class ImportTargetPage extends ColoredWizardPage {

	public class PreviewJob extends Job {

		private final Date date = new Date();
		private List<File> files;
		private File rootFolder;
		private int policy;
		private TreeViewer viewer;
		private boolean deep;

		public PreviewJob(List<File> list, File rootFolder, int policy, boolean deep, TreeViewer viewer) {
			super(Messages.ImportTargetPage_generate_preview);
			setSystem(true);
			this.files = list;
			this.rootFolder = rootFolder;
			this.policy = policy;
			this.deep = deep;
			this.viewer = viewer;
		}

		@Override
		public boolean belongsTo(Object family) {
			return family == ImportTargetPage.this;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			String t = null;
			switch (policy) {
			case ImportFromDeviceData.SUBFOLDERPOLICY_YEAR:
				t = "yyyy"; //$NON-NLS-1$
				break;
			case ImportFromDeviceData.SUBFOLDERPOLICY_YEARMONTH:
				t = "yyyy-MM"; //$NON-NLS-1$
				break;
			case ImportFromDeviceData.SUBFOLDERPOLICY_YEARMONTHDAY:
				t = "yyyy-MM-dd"; //$NON-NLS-1$
				break;
			case ImportFromDeviceData.SUBFOLDERPOLICY_YEARWEEK:
				t = "YYYY-'W'ww"; //$NON-NLS-1$
				break;
			case ImportFromDeviceData.SUBFOLDERPOLICY_YEARWEEKDAY:
				t = "YYYY-'W'ww-uu"; //$NON-NLS-1$
				break;
			}
			if (t != null) {
				FolderNode root = new FolderNode(null, rootFolder.getName());
				for (File file : files) {
					date.setTime(file.lastModified());
					String name = new SimpleDateFormat(t).format(date);
					FolderNode parentNode = root;
					int from = 0;
					int depth = 0;
					while (from >= 0) {
						String newName;
						int p = name.indexOf('-', from);
						if ((depth++ == 0 || deep) && p > 0) {
							newName = name.substring(0, p);
							from = p + 1;
						} else {
							newName = name;
							from = -1;
						}
						FolderNode currentNode = parentNode.find(newName);
						if (currentNode == null)
							currentNode = new FolderNode(parentNode, newName);
						parentNode = currentNode;
					}
				}
				if (monitor.isCanceled() || viewer.getControl().isDisposed())
					return Status.CANCEL_STATUS;
				Control control = viewer.getControl();
				control.getDisplay().asyncExec(() -> {
					if (!control.isDisposed()) {
						viewer.setInput(new FolderNode[] { root });
						viewer.expandAll();
					}
				});
			}
			return Status.OK_STATUS;
		}

	}

	private static class FolderNode implements Comparable<FolderNode> {
		List<FolderNode> children = new ArrayList<>();
		FolderNode parent;
		String label;

		public FolderNode(FolderNode parent, String label) {
			this.parent = parent;
			this.label = label;
			if (parent != null)
				parent.add(this);
		}

		public FolderNode find(String name) {
			for (FolderNode c : children)
				if (c.label.equals(name))
					return c;
			return null;
		}

		public void add(FolderNode child) {
			children.add(child);
		}

		@Override
		public int compareTo(FolderNode o) {
			return this.label.compareTo(o.label);
		}

	}

	private static final String TARGETDIR = "targetDir"; //$NON-NLS-1$
	private static final String SUBFOLDERS = "subfolder"; //$NON-NLS-1$
	private static final Object[] EMPTY = new Object[0];
	private static final String DEEPSUBFOLDERS = "deepSubfolders"; //$NON-NLS-1$

	private Text targetDirField;
	private Combo subfolderCombo;
	private final boolean media;
	private Label copyLabel;
	private TreeViewer treeviewer;
	private Label previewLabel;
	private RadioButtonGroup depthGroup;

	public ImportTargetPage(String pageName, boolean media) {
		super(pageName);
		this.media = media;
	}

	@Override
	public void createControl(final Composite parent) {
		Composite targetComp = createComposite(parent, 3);
		copyLabel = new Label(targetComp, SWT.NONE);
		copyLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		Label transferToLabel = new Label(targetComp, SWT.NONE);
		transferToLabel.setText(Messages.ImportFromDeviceWizard_transfer_to);

		targetDirField = new Text(targetComp, SWT.READ_ONLY | SWT.BORDER);
		final GridData gd_text = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd_text.widthHint = 200;
		targetDirField.setLayoutData(gd_text);
		WidgetFactory.createPushButton(targetComp, Messages.ImportFromDeviceWizard_browse, SWT.BEGINNING)
				.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						DirectoryDialog dialog = new DirectoryDialog(parent.getShell());
						dialog.setText(Messages.ImportTargetPage_target_folder);
						dialog.setMessage(Messages.ImportTargetPage_select_folder);
						String lastTargetDir = targetDirField.getText();
						dialog.setFilterPath(lastTargetDir.isEmpty() ? null : lastTargetDir);
						String dir = dialog.open();
						if (dir != null) {
							if (!dir.endsWith(File.separator))
								dir += File.separator;
							targetDirField.setText(dir);
							updateSpaceLabel();
							validatePage();
							startPreviewJob();
						}
					}
				});

		new Label(targetComp, SWT.NONE).setText(Messages.ImportFromDeviceWizard_create_subfolder);

		subfolderCombo = new Combo(targetComp, SWT.READ_ONLY);
		subfolderCombo.setItems(new String[] { Messages.ImportFromDeviceWizard_no,
				Messages.ImportFromDeviceWizard_by_year, Messages.ImportFromDeviceWizard_by_year_month,
				Messages.ImportFromDeviceWizard_by_year_month_day, Messages.ImportTargetPage_by_year_week,
				Messages.ImportTargetPage_by_year_wek_day });
		subfolderCombo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		subfolderCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateDepthGroup();
				startPreviewJob();
			}
		});
		depthGroup = new RadioButtonGroup(targetComp, null, SWT.HORIZONTAL, Messages.ImportTargetPage_two_levels,
				Messages.ImportTargetPage_three_levels);
		depthGroup.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
		depthGroup.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				startPreviewJob();
			}
		});
		Label sep = new Label(targetComp, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		previewLabel = new Label(targetComp, SWT.NONE);
		previewLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 3, 1));
		previewLabel.setFont(JFaceResources.getBannerFont());
		previewLabel.setText(Messages.ImportTargetPage_preview);
		treeviewer = new TreeViewer(targetComp, SWT.V_SCROLL);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
		layoutData.horizontalIndent = 15;
		treeviewer.getControl().setLayoutData(layoutData);
		treeviewer.setContentProvider(new ITreeContentProvider() {
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// do nothing
			}

			public void dispose() {
				// do nothing
			}

			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof FolderNode[])
					return (FolderNode[]) inputElement;
				return EMPTY;
			}

			public boolean hasChildren(Object element) {
				return (element instanceof FolderNode) && !((FolderNode) element).children.isEmpty();
			}

			public Object getParent(Object element) {
				return (element instanceof FolderNode) ? ((FolderNode) element).parent : null;
			}

			public Object[] getChildren(Object parentElement) {
				return (parentElement instanceof FolderNode) ? ((FolderNode) parentElement).children.toArray() : EMPTY;
			}
		});
		treeviewer.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof FolderNode)
					return ((FolderNode) element).label;
				return element.toString();
			}

			@Override
			public Image getImage(Object element) {
				return Icons.folder.getImage();
			}

		});
		treeviewer.setComparator(ZViewerComparator.INSTANCE);
		setControl(targetComp);
		setHelp(media ? HelpContextIds.IMPORT_FROM_DEVICE_WIZARD_TARGET_SELECTION
				: HelpContextIds.IMPORT_NEW_STRUCTURE_WIZARD_TARGET_SELECTION);
		setTitle(Messages.ImportTargetPage_target_selection);
		setMessage(Messages.ImportTargetPage_select_the_target_folder);
		super.createControl(parent);
		fillValues();
		validatePage();
	}

	private void fillValues() {
		IDialogSettings dialogSettings = ((ImportFromDeviceWizard) getWizard()).getDialogSettings();
		String targetDir = dialogSettings.get(TARGETDIR);
		targetDirField.setText(targetDir == null ? "" : targetDir); //$NON-NLS-1$
		try {
			subfolderCombo.select(dialogSettings.getInt(SUBFOLDERS));
		} catch (NumberFormatException e) {
			subfolderCombo.select(2);
		}
		depthGroup.setSelection(dialogSettings.getBoolean(DEEPSUBFOLDERS) ? 1 : 0);
		updateSpaceLabel();
		updateDepthGroup();
		startPreviewJob();
	}

	protected void startPreviewJob() {
		boolean visible = false;
		Job.getJobManager().cancel(this);
		String targetFileName = targetDirField.getText();
		if (!targetFileName.isEmpty()) {
			int policy = subfolderCombo.getSelectionIndex();
			if (policy != ImportFromDeviceData.SUBFOLDERPOLICY_NO) {
				new PreviewJob(((ImportFromDeviceWizard) getWizard()).getSelectedFiles(), new File(targetFileName),
						policy, depthGroup.getSelection() == 1, treeviewer).schedule();
				visible = true;
			}
		}
		previewLabel.setVisible(visible);
		treeviewer.getControl().setVisible(visible);
	}

	private void updateSpaceLabel() {
		List<File> selectedFiles = ((ImportFromDeviceWizard) getWizard()).getSelectedFiles();
		if (selectedFiles == null) {
			copyLabel.setText(Messages.ImportTargetPage_medium_offline);
			copyLabel.setData("id", "errors"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			copyLabel.setData("id", null); //$NON-NLS-1$
			long requiredSpace = 0;
			int n = selectedFiles.size();
			long freespace = 0;
			String target = targetDirField.getText();
			if (!target.isEmpty()) {
				File targetFile = new File(target);
				if (targetFile.exists())
					freespace = CoreActivator.getDefault().getFreeSpace(targetFile);
			}
			if (freespace <= 0) {
				copyLabel.setText(""); //$NON-NLS-1$
				return;
			}
			for (File file : selectedFiles)
				requiredSpace += file.length();
			NumberFormat nf = NumberFormat.getNumberInstance();
			nf.setMaximumFractionDigits(1);
			switch (n) {
			case 0:
				copyLabel.setText(Messages.ImportTargetPage_no_files);
				break;
			case 1:
				copyLabel.setText(NLS.bind(Messages.ImportTargetPage_one_image, nf.format(requiredSpace / 1048576.0),
						nf.format(freespace / 1048576.0)));
				break;
			default:
				copyLabel.setText(NLS.bind(Messages.ImportTargetPage_n_images,
						new Object[] { n, nf.format(requiredSpace / 1048576.0), nf.format(freespace / 1048576.0) }));
				break;
			}
		}
	}

	@Override
	protected void validatePage() {
		String target = targetDirField.getText();
		if (target.isEmpty()) {
			setErrorMessage(Messages.ImportFromDeviceWizard_specify_output_dir);
			setPageComplete(false);
		} else if (!new File(target).exists()) {
			setErrorMessage(Messages.ImportFromDeviceWizard_target_dir_does_not_exist);
			setPageComplete(false);
		} else {
			setErrorMessage(null);
			setPageComplete(true);
		}
	}

	public void performFinish(ImportFromDeviceData importData) {
		IDialogSettings dialogSettings = getWizard().getDialogSettings();
		String targetDir = targetDirField.getText();
		importData.setTargetDir(targetDir);
		dialogSettings.put(TARGETDIR, targetDir);
		int subfolderPolicy = subfolderCombo.getSelectionIndex();
		importData.setSubfolderPolicy(subfolderPolicy);
		dialogSettings.put(SUBFOLDERS, subfolderPolicy);
		boolean deep = depthGroup.isEnabled() && depthGroup.getSelection() == 1;
		importData.setDeepSubfolders(deep);
		dialogSettings.put(DEEPSUBFOLDERS, deep);
	}

	protected void updateDepthGroup() {
		int policy = subfolderCombo.getSelectionIndex();
		depthGroup.setEnabled(policy == ImportFromDeviceData.SUBFOLDERPOLICY_YEARMONTHDAY
				|| policy == ImportFromDeviceData.SUBFOLDERPOLICY_YEARWEEKDAY);
	}

}
