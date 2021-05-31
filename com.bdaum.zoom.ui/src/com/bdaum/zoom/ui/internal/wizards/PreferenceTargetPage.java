package com.bdaum.zoom.ui.internal.wizards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.preferences.IPreferenceFilter;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.PreferenceFilterEntry;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.bdaum.zoom.common.PreferenceRegistry;
import com.bdaum.zoom.common.PreferenceRegistry.IPreferenceConstants;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.program.BatchConstants;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.dialogs.AllNoneGroup;
import com.bdaum.zoom.ui.internal.widgets.FileEditor;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.bdaum.zoom.ui.wizards.ColoredWizardPage;

public class PreferenceTargetPage extends ColoredWizardPage implements Listener, ICheckStateListener {

	private static final String PATH = "path"; //$NON-NLS-1$
	protected static final Object[] EMPTY = new Object[0];
	private FileEditor fileEditor;
	protected String path;
	private IDialogSettings dialogSettings;
	private CheckboxTreeViewer viewer;
	private Object[] checkedElements;

	public PreferenceTargetPage() {
		super("main", Messages.PreferenceTargetPage_export_preferences_to_file, null); //$NON-NLS-1$
	}

	@SuppressWarnings("unused")
	@Override
	public void createControl(Composite parent) {
		dialogSettings = getWizard().getDialogSettings();
		path = dialogSettings.get(PATH);
		Composite composite = createComposite(parent, 1);
		CGroup targetGroup = new CGroup(composite, SWT.NONE);
		targetGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		targetGroup.setLayout(new GridLayout(3, false));
		targetGroup.setText(Messages.PreferenceTargetPage_target_file);
		fileEditor = new FileEditor(targetGroup, SWT.SAVE, Messages.PreferenceTargetPage_path, true,
				new String[] { "*.zpf" }, //$NON-NLS-1$
				new String[] { Messages.PreferenceTargetPage_user_preferences }, path,
				path == null ? BatchConstants.APP_PREFERENCES : path, false, dialogSettings);
		fileEditor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		fileEditor.addListener(SWT.Modify, this);
		CGroup filterGroup = new CGroup(composite, SWT.NONE);
		filterGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		filterGroup.setLayout(new GridLayout(2, false));
		filterGroup.setText(Messages.PreferenceTargetPage_filter);
		viewer = new CheckboxTreeViewer(filterGroup, SWT.NO_SCROLL);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.heightHint = 500;
		viewer.getControl().setLayoutData(layoutData);
		viewer.setLabelProvider(ZColumnLabelProvider.getDefaultInstance());
		viewer.setContentProvider(new ITreeContentProvider() {

			@Override
			public boolean hasChildren(Object element) {
				return getChildren(element).length > 0;
			}

			@Override
			public Object getParent(Object element) {
				return PreferenceRegistry.getDefault().getParent(element);
			}

			@Override
			public Object[] getElements(Object inputElement) {
				return PreferenceRegistry.getDefault().getRootElements();
			}

			@Override
			public Object[] getChildren(Object parentElement) {
				Object[] children = PreferenceRegistry.getDefault().getChildren(parentElement);
				List<Object> result = new ArrayList<Object>(children.length);
				for (Object child : children)
					if (PreferenceRegistry.getDefault().getChildren(child).length > 0)
						result.add(child);
				return result.toArray();
			}
		});
		viewer.setComparator(new ViewerComparator());
		viewer.addCheckStateListener(this);
		Composite buttonBar = new Composite(filterGroup, SWT.NONE);
		buttonBar.setLayoutData(new GridData(SWT.END, SWT.BEGINNING, false, false));
		buttonBar.setLayout(new GridLayout(1, false));
		new AllNoneGroup(buttonBar, new Listener() {
			@Override
			public void handleEvent(Event e) {
				UiUtilities.checkAllInTrre(viewer, e.widget.getData() == AllNoneGroup.ALL);
				viewer.setGrayedElements(EMPTY);
				validatePage();
			}
		});
		setControl(composite);
		fillValues();
		super.createControl(parent);
	}

	@Override
	public void handleEvent(Event event) {
		path = getTargetFile();
		validatePage();
	}

	public String getTargetFile() {
		return fileEditor.getText().trim();
	}

	@Override
	protected String validate() {
		checkedElements = viewer.getCheckedElements();
		if (path == null || path.isEmpty() || (path.indexOf('/') < 0 && path.indexOf('\\') < 0))
			return Messages.PreferenceTargetPage_specify_target_file;
		return checkedElements.length > 0 ? null : Messages.PreferenceFilterPage_at_least_one;
	}

	public String getPath() {
		return path;
	}

	private void fillValues() {
		viewer.setInput(this);
		viewer.expandAll();
		UiUtilities.checkAllInTrre(viewer, true);
	}

	@Override
	public void checkStateChanged(CheckStateChangedEvent event) {
		UiUtilities.updateCheckboxTree(event);
		validatePage();
	}

	public IPreferenceFilter getFilter() {
		Set<Object> checked = new HashSet<>(Arrays.asList(checkedElements));
		Map<String, PreferenceFilterEntry[]> map = new HashMap<>();
		List<PreferenceFilterEntry> entries = new ArrayList<PreferenceFilterEntry>();
		PreferenceRegistry registry = PreferenceRegistry.getDefault();
		Object[] rootElements = registry.getRootElements();
		List<Object> subGroups = new ArrayList<Object>();
		for (Object root : rootElements)
			for (Object child : registry.getChildren(root))
				if (checked.contains(child))
					subGroups.add(child);
		for (IPreferenceConstants constant : registry.getConstants()) {
			entries.clear();
			for (Object subGroup : subGroups)
				for (Object child : constant.getChildren(subGroup))
					if (child instanceof String)
						entries.add(new PreferenceFilterEntry(((String) child)));
			if (!entries.isEmpty())
				map.put(constant.getNode().name(), entries.toArray(new PreferenceFilterEntry[entries.size()]));
		}
		return new IPreferenceFilter() {
			public String[] getScopes() {
				return new String[] { InstanceScope.SCOPE };
			}

			@SuppressWarnings({ "rawtypes", "unchecked" })
			public Map getMapping(String scope) {
				return map;
			}
		};

	}

	public boolean finish() {
		if (path != null) {
			dialogSettings.put(PATH, path);
			if (fileEditor.testSave()) {
				fileEditor.saveValues();
				return true;
			}
		}
		return false;
	}

}
