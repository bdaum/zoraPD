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

package com.bdaum.zoom.ui.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.css.internal.CssActivator;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.ZViewerComparator;
import com.bdaum.zoom.ui.widgets.CLink;

public abstract class AbstractPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, IAdaptable {

	private static final String LAST_TAB_SELECTION = "lastTabSelection#"; //$NON-NLS-1$
	protected IWorkbench workbench;
	private boolean hasHelp;

	private List<IPreferencePageExtension> addOns;
	private int lastSelectedItem = -1;
	protected CTabFolder tabFolder;
	private String tabName;

	/**
	 * Default constructor
	 */
	public AbstractPreferencePage() {
	}

	/**
	 * Constructor
	 *
	 * @param title
	 *            - Page title
	 */
	public AbstractPreferencePage(String title) {
		super(title);
	}

	/**
	 * Constructor
	 *
	 * @param title
	 *            - Page title
	 * @param image
	 *            - Page image
	 */
	public AbstractPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */

	public void init(IWorkbench aWorkbench) {
		this.workbench = aWorkbench;
		setPreferenceStore(UiActivator.getDefault().getPreferenceStore());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse
	 * .swt.widgets.Composite)
	 */

	@Override
	protected Control createContents(Composite parent) {
		CssActivator.getDefault().setColors(parent.getShell());
		Composite composite = createComposite(parent);
		createPageContents(composite);
		applyDialogFont(composite);
		composite.layout();
		validate();
		return composite;
	}

	/**
	 * Creates the page contents
	 *
	 * @param composite
	 *            - parent container
	 */
	protected abstract void createPageContents(Composite composite);

	/**
	 * Creates the composite which will contain all the preference controls for
	 * this page.
	 *
	 * @param parent
	 *            the parent composite
	 * @return the composite for this page
	 */
	protected Composite createComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		composite.setFont(parent.getFont());
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 10;
		composite.setLayout(layout);
		return composite;
	}

	/**
	 * Sets the context help id
	 *
	 * @param id
	 *            - help id
	 */
	protected void setHelp(String id) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), id);
		hasHelp = id != null;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see
	 * org.eclipse.jface.preference.PreferencePage#contributeButtons(org.eclipse
	 * .swt.widgets.Composite)
	 */
	@Override
	protected void contributeButtons(Composite parent) {
		if (hasHelp) {
			createHelpControl(parent);
			((GridLayout) parent.getLayout()).numColumns++;
		}
		super.contributeButtons(parent);
	}

	/**
	 * Creates the help control button or link
	 *
	 * @param parent
	 *            - parent composite
	 * @return - help button or link
	 */
	protected static Control createHelpControl(Composite parent) {
		Image helpImage = JFaceResources.getImage(Dialog.DLG_IMG_HELP);
		return (helpImage != null) ? createHelpImageButton(parent, helpImage) : createHelpLink(parent);
	}

	/*
	 * Creates a button with a help image. This is only used if there is an
	 * image available.
	 */
	private static ToolBar createHelpImageButton(final Composite parent, Image image) {
		ToolBar toolBar = new ToolBar(parent, SWT.FLAT | SWT.NO_FOCUS);
		((GridLayout) parent.getLayout()).numColumns++;
		toolBar.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		final Cursor cursor = new Cursor(parent.getDisplay(), SWT.CURSOR_HAND);
		toolBar.setCursor(cursor);
		toolBar.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				cursor.dispose();
			}
		});
		ToolItem item = new ToolItem(toolBar, SWT.NONE);
		item.setImage(image);
		item.setToolTipText(JFaceResources.getString("helpToolTip")); //$NON-NLS-1$
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				helpPressed(parent);
			}
		});
		return toolBar;
	}

	/*
	 * Creates a help link. This is used when there is no help image available.
	 */
	private static CLink createHelpLink(final Composite parent) {
		CLink link = new CLink(parent, SWT.WRAP | SWT.NO_FOCUS);
		((GridLayout) parent.getLayout()).numColumns++;
		link.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		link.setText(IDialogConstants.HELP_LABEL);
		link.setToolTipText(IDialogConstants.HELP_LABEL);
		link.addListener(new Listener() {
			@Override
			public void handleEvent(Event event) {
				helpPressed(parent);
			}
		});
		return link;
	}

	/*
	 * Called when the help control is invoked. This emulates the keyboard
	 * context help behavior (e.g. F1 on Windows). It traverses the widget tree
	 * upward until it finds a widget that has a help listener on it, then
	 * invokes a help event on that widget.
	 */
	private static void helpPressed(Control c) {
		while (c != null) {
			if (c.isListening(SWT.Help)) {
				c.notifyListeners(SWT.Help, new Event());
				break;
			}
			c = c.getParent();
		}
	}

	/**
	 * Creates a combo viewer
	 *
	 * @param parent
	 *            - parent container
	 * @param lab
	 *            - label
	 * @param options
	 *            - combo items
	 * @param labelling
	 *            - either a String[] object with corresponding labels or a
	 *            LabelProvider
	 * @param sort
	 *            - true if items are to be sorted alphabetically
	 * @return combo viewer
	 */
	public static ComboViewer createComboViewer(Composite parent, String lab, final String[] options,
			final Object labelling, boolean sort) {
		if (lab != null)
			new Label(parent, SWT.NONE).setText(lab);
		ComboViewer viewer = new ComboViewer(parent);
		viewer.getControl().setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		if (labelling instanceof LabelProvider)
			viewer.setLabelProvider((LabelProvider) labelling);
		else
			viewer.setLabelProvider(new LabelProvider() {

				@Override
				public String getText(Object element) {
					if (labelling instanceof String[])
						for (int i = 0; i < options.length; i++)
							if (options[i].equals(element))
								return ((String[]) labelling)[i];
					return super.getText(element);
				}
			});
		if (sort)
			viewer.setComparator(ZViewerComparator.INSTANCE);
		viewer.setInput(options);
		return viewer;
	}

	protected CTabFolder createTabFolder(Composite parent, String tabName) {
		this.tabName = tabName;
		tabFolder = new CTabFolder(parent, SWT.BORDER);
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
		tabFolder.setSimple(false);
		tabFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				lastSelectedItem = tabFolder.getSelectionIndex();
			}
		});
		lastSelectedItem = getPreferenceStore().getInt(LAST_TAB_SELECTION + tabName) - 1;
		return tabFolder;
	}

	protected void initTabFolder(int dflt) {
		if (lastSelectedItem < 0)
			lastSelectedItem = dflt;
		tabFolder.setSelection(lastSelectedItem);
	}


	/**
	 * Extents the preference page with registered IPreferencePageExtensions
	 *
	 * @param parent
	 *            - parent composite. If this is a CTabFolder the extension are
	 *            created as CTabItems, as CGroups otherwise
	 * @param pageId
	 *            - ID of target preference page
	 */
	protected void createExtensions(Composite parent, String pageId) {
		addOns = new ArrayList<IPreferencePageExtension>(3);
		IExtension[] extensions = Platform.getExtensionRegistry()
				.getExtensionPoint(UiActivator.PLUGIN_ID, "preferencePageExtension").getExtensions(); //$NON-NLS-1$
		for (IExtension extension : extensions)
			for (IConfigurationElement conf : extension.getConfigurationElements())
				if (conf.getAttribute("pageId").equals(pageId)) //$NON-NLS-1$
					try {
						IPreferencePageExtension ext = (IPreferencePageExtension) conf
								.createExecutableExtension("class"); //$NON-NLS-1$
						if (parent instanceof CTabFolder)
							UiUtilities.createTabItem((CTabFolder) parent, ext.getLabel(), ext.getTooltip())
									.setControl(ext.createPageContents(parent, this));
						else
							ext.createPageContents(UiUtilities.createGroup(parent, 1, ext.getLabel()), this);
						addOns.add(ext);
					} catch (CoreException e) {
						UiActivator.getDefault().logError(
								com.bdaum.zoom.ui.preferences.Messages.AbstractPreferencePage_cannot_create_page_part,
								e);
					}
	}

	/**
	 * Fill all values into the controls of the preference page
	 */
	protected void fillValues() {
		doFillValues();
		if (addOns != null)
			for (IPreferencePageExtension item : addOns)
				item.fillValues();
		validate();
		updateButtons();
	}
	
	/**
	 * Extensible pages should overwrite this method instead of fillValues().
	 */
	protected void doFillValues() {
		// default implementation does nothing
	}
	
	protected void setEnabled(boolean enabled) {
		if (addOns != null)
			for (IPreferencePageExtension item : addOns)
				item.setEnabled(enabled);
	}

	/**
	 * Updates any buttons after filling the page
	 */
	protected void updateButtons() {
		doUpdateButtons();
		if (addOns != null)
			for (IPreferencePageExtension item : addOns)
				item.updateButtons();
	}

	/**
	 * Extensible pages should overwrite this method instead of updateButtons().
	 */
	protected void doUpdateButtons() {
		// default implementation does nothing
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	public void performDefaults() {
		doPerformDefaults();
		if (addOns != null)
			for (IPreferencePageExtension item : addOns)
				item.performDefaults();
		fillValues();
	}

	/**
	 * Extensible pages should overwrite this method instead of
	 * performDefaults().
	 */
	protected void doPerformDefaults() {
		// default implementation does nothing
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		if (tabFolder != null)
			getPreferenceStore().setValue(LAST_TAB_SELECTION + tabName, lastSelectedItem + 1);
		doPerformOk();
		if (addOns != null)
			for (IPreferencePageExtension item : addOns)
				item.performOk();
		return super.performOk();
	}

	/**
	 * Extensible pages should overwrite this method instead of performOk().
	 */
	protected void doPerformOk() {
		// default implementation does nothing
	}
	
	@Override
	public boolean performCancel() {
		doPerformCancel();
		if (addOns != null)
			for (IPreferencePageExtension item : addOns)
				item.performCancel();
		return super.performCancel();
	}
	
	/**
	 * Extensible pages should overwrite this method instead of performOk().
	 */
	protected void doPerformCancel() {
		// default implementation does nothing
	}


	/**
	 * Validates the page
	 */
	public void validate() {
		String errorMessage = doValidate();
		if (errorMessage != null) {
			setErrorMessage(errorMessage);
			setValid(false);
			return;
		}
		if (addOns != null) {
			if (tabFolder != null) {
				int selectionIndex = tabFolder.getSelectionIndex();
				String[] errorMessages = new String[addOns.size()];
				int i = 0;
				for (IPreferencePageExtension item : addOns) {
					errorMessages[i] = item.validate();
					if (errorMessages[i] != null && i == selectionIndex) {
						setErrorMessage(errorMessages[i]);
						setValid(false);
						return;
					}
					++i;
				}
				for (int j = 0; j < errorMessages.length; j++) {
					if (errorMessages[j] != null) {
						tabFolder.setSelection(j);
						setErrorMessage(errorMessages[j]);
						setValid(false);
						return;
					}
				}
			} else
				for (IPreferencePageExtension item : addOns) {
					errorMessage = item.validate();
					if (errorMessage != null) {
						setErrorMessage(errorMessage);
						setValid(false);
						return;
					}
				}
		}
		setErrorMessage(null);
		setValid(true);
	}

	/**
	 * Extensible pages should overwrite this method instead of validate().
	 *
	 * @return errormessage or null
	 */
	protected String doValidate() {
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getAdapter(Class adapter) {
		if (Shell.class.equals(adapter)) {
			Shell shell = getControl().getShell();
			while (shell.getParent() instanceof Shell)
				shell = (Shell) shell.getParent();
			return shell;
		}
		return null;
	}

}