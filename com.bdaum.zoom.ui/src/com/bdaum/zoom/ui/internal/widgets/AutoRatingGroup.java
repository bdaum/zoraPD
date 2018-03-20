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
 * (c) 2018 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.widgets;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Widget;

import com.bdaum.zoom.core.internal.ai.IAiService;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.bdaum.zoom.ui.widgets.NumericControl;

public class AutoRatingGroup extends Composite {

	private static final String PROVIDER = "provider"; //$NON-NLS-1$
	private static final String MODEL = "model"; //$NON-NLS-1$
	private static final String MAXRATING = "maxRating"; //$NON-NLS-1$
	private static final String OVERWRITE = "overwrite"; //$NON-NLS-1$
	private static final String ENABLEAUTORATING = "enableAutoRating"; //$NON-NLS-1$

	private String[] ratingProviderIds;
	private CheckboxButton enableButton;
	private ComboViewer providerViewer;
	protected String selectedProvider;
	private ComboViewer modelViewer;
	private NumericControl maxField;
	private CheckboxButton overwriteButton;
	private IAiService aiService;
	private String[] modelIds;
	private String[] modelLabels;
	private IDialogSettings dialogSettings;
	private ListenerList<ModifyListener> listeners = new ListenerList<>();

	public AutoRatingGroup(Composite parent, IAiService aiService, IDialogSettings dialogSettings) {
		super(parent, SWT.NONE);
		setLayout(new FillLayout());
		this.aiService = aiService;
		ratingProviderIds = aiService.getRatingProviderIds();
		String[] ratingProviderNames = aiService.getRatingProviderNames();
		this.dialogSettings = dialogSettings;
		CGroup group = new CGroup(this, SWT.NONE);
		group.setText(Messages.AutoRatingGroup_auto_rating);
		group.setLayout(new GridLayout(3, false));
		enableButton = WidgetFactory.createCheckButton(group, Messages.AutoRatingGroup_enable,
				new GridData(SWT.FILL, SWT.BEGINNING, true, false, 3, 1));
		enableButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateControls();
				fireModified(enableButton);
			}
		});
		Label label = new Label(group, SWT.NONE);
		label.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
		label.setText(Messages.AutoRatingGroup_provider);
		if (ratingProviderIds.length > 1) {
			providerViewer = new ComboViewer(group, SWT.NONE);
			GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
			layoutData.widthHint = 100;
			providerViewer.getControl().setLayoutData(layoutData);
			providerViewer.setContentProvider(ArrayContentProvider.getInstance());
			providerViewer.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(Object element) {
					for (int i = 0; i < ratingProviderIds.length; i++)
						if (ratingProviderIds[i].equals(element))
							return ratingProviderNames[i];
					return super.getText(element);
				}
			});
			providerViewer.addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					selectedProvider = (String) ((StructuredSelection) providerViewer.getSelection()).getFirstElement();
					updateModelViewer();
					fireModified(providerViewer.getControl());
				}
			});
		} else {
			label = new Label(group, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			label.setText(ratingProviderNames[0]);
			selectedProvider = ratingProviderIds[0];
		}
		Composite modelcomp = new Composite(group, SWT.NONE);
		modelcomp.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginWidth = 0;
		modelcomp.setLayout(layout);
		label = new Label(modelcomp, SWT.NONE);
		label.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
		label.setText(Messages.AutoRatingGroup_theme);
		modelViewer = new ComboViewer(modelcomp, SWT.NONE);
		GridData layoutData = new GridData(SWT.BEGINNING, SWT.CENTER, true, false);
		layoutData.widthHint = 80;
		modelViewer.getControl().setLayoutData(layoutData);
		modelViewer.setContentProvider(ArrayContentProvider.getInstance());
		modelViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				for (int i = 0; i < modelIds.length; i++)
					if (modelIds[i].equals(element))
						return modelLabels[i];
				return super.getText(element);
			}
		});
		modelViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				fireModified(modelViewer.getControl());
			}
		});
		new Label(group, SWT.NONE).setText(Messages.AutoRatingGroup_rating);
		maxField = new NumericControl(group, SWT.NONE);
		maxField.setMinimum(1);
		maxField.setMaximum(5);
		overwriteButton = WidgetFactory.createCheckButton(group, Messages.AutoRatingGroup_overwrite,
				new GridData(SWT.END, SWT.CENTER, false, false));
	}

	public void addModifyListener(ModifyListener listener) {
		listeners.add(listener);
	}

	public void removeModifyListener(ModifyListener listener) {
		listeners.remove(listener);
	}

	protected void fireModified(Widget control) {
		Event ev = new Event();
		ev.item = control;
		ev.display = this.getDisplay();
		ev.doit = true;
		ev.widget = this;
		ModifyEvent e = new ModifyEvent(ev);
		for (ModifyListener modifyListener : listeners)
			modifyListener.modifyText(e);

	}

	protected void updateControls() {
		boolean enabled = enableButton.getSelection();
		if (providerViewer != null)
			providerViewer.getControl().setEnabled(enabled);
		modelViewer.getControl().setEnabled(enabled);
		maxField.setEnabled(enabled);
		overwriteButton.setEnabled(enabled);
	}

	private void updateModelViewer() {
		if (selectedProvider != null) {
			String selectedModel = (String) ((StructuredSelection) modelViewer.getSelection()).getFirstElement();
			if (selectedModel == null)
				selectedModel = dialogSettings.get(MODEL);
			modelLabels = aiService.getRatingModelLabels(selectedProvider);
			modelViewer.setInput(modelIds = aiService.getRatingModelIds(selectedProvider));
			if (selectedModel != null)
				modelViewer.setSelection(new StructuredSelection(selectedModel));
		}
	}

	public String validate() {
		String errorMessage = null;
		if (enableButton != null && enableButton.getSelection()) {
			if (providerViewer != null && providerViewer.getSelection().isEmpty())
				errorMessage = Messages.AutoRatingGroup_select_provider;
			if (errorMessage == null && modelViewer != null && modelViewer.getSelection().isEmpty())
				errorMessage = Messages.AutoRatingGroup_select_theme;
		}
		return errorMessage;
	}

	public void fillValues() {
		if (providerViewer != null) {
			providerViewer.setInput(ratingProviderIds);
			selectedProvider = dialogSettings.get(PROVIDER);
			if (selectedProvider != null)
				providerViewer.setSelection(new StructuredSelection(selectedProvider));
		}
		updateModelViewer();
		try {
			maxField.setSelection(dialogSettings.getInt(MAXRATING));
		} catch (NumberFormatException e) {
			maxField.setSelection(3);
		}
		overwriteButton.setSelection(dialogSettings.getBoolean(OVERWRITE));
		enableButton.setSelection(dialogSettings.getBoolean(ENABLEAUTORATING));
		updateControls();
	}

	public void saveValues() {
		String selectedModel = (String) ((StructuredSelection) modelViewer.getSelection()).getFirstElement();
		boolean enabled = enableButton.getSelection();
		dialogSettings.put(ENABLEAUTORATING, enabled);
		if (enabled && selectedProvider != null && selectedModel != null) {
			dialogSettings.put(PROVIDER, selectedProvider);
			dialogSettings.put(MODEL, selectedModel);
			dialogSettings.put(MAXRATING, maxField.getSelection());
			dialogSettings.put(OVERWRITE, overwriteButton.getSelection());
		}
	}

	public String getProviderId() {
		return selectedProvider;
	}

	public String getModelId() {
		return (String) ((StructuredSelection) modelViewer.getSelection()).getFirstElement();
	}

	public boolean getOverwrite() {
		return overwriteButton.getSelection();
	}

	public int getMaxRating() {
		return maxField.getSelection();
	}

}
