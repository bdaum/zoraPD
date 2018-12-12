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

package com.bdaum.zoom.ui.internal.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.ai.IAiService;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.AutoRatingOperation;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.bdaum.zoom.ui.widgets.NumericControl;

@SuppressWarnings("restriction")
public class AutomatedRatingDialog extends ZTitleAreaDialog {

	private static final String SETTINGSID = "autoRating"; //$NON-NLS-1$
	private static final String PROVIDER = "provider"; //$NON-NLS-1$
	private static final String MODEL = "model"; //$NON-NLS-1$
	private static final String MAXRATING = "maxRating"; //$NON-NLS-1$
	private static final String OVERWRITE = "overwrite"; //$NON-NLS-1$
	private ComboViewer providerViewer;
	private ComboViewer modelViewer;
	private String selectedProvider;
	private IDialogSettings settings;
	private IAiService aiService;
	private String[] modelIds;
	private String[] modelLabels;
	private String[] ratingProviderIds;
	private String[] ratingProviderNames;
	private NumericControl maxField;
	private CheckboxButton overwriteButton;
	private List<Asset> assets;

	public AutomatedRatingDialog(Shell parentShell, List<Asset> assets) {
		super(parentShell, HelpContextIds.AUTORATING_DIALOG);
		this.assets = assets;
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.AutomatedRatingDialog_use_ai_rating);
		setMessage(Messages.AutomatedRatingDialog_ai_rating_msg);
		fillValues();
		validate();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout());
		aiService = CoreActivator.getDefault().getAiService();
		ratingProviderIds = aiService.getRatingProviderIds();
		ratingProviderNames = aiService.getRatingProviderNames();
		CGroup providerGroup = CGroup.create(composite, 1, Messages.AutomatedRatingDialog_service);
		new Label(providerGroup, SWT.NONE).setText(Messages.AutomatedRatingDialog_provider);
		if (ratingProviderIds.length > 1) {
			providerViewer = new ComboViewer(providerGroup, SWT.NONE);
			providerViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
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
					selectedProvider = (String) providerViewer.getStructuredSelection().getFirstElement();
					updateModelViewer();
					validate();
				}
			});
		} else {
			new Label(providerGroup, SWT.NONE).setText(ratingProviderNames[0]);
			selectedProvider = ratingProviderIds[0];
		}
		new Label(providerGroup, SWT.NONE).setText(Messages.AutomatedRatingDialog_sujet);
		modelViewer = new ComboViewer(providerGroup, SWT.NONE);
		modelViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
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
				validate();
			}
		});

		CGroup optionsGroup = CGroup.create(composite, 1, Messages.AutomatedRatingDialog_options);
		new Label(optionsGroup, SWT.NONE).setText(Messages.AutomatedRatingDialog_max_rating);
		maxField = new NumericControl(optionsGroup, SWT.NONE);
		maxField.setMinimum(1);
		maxField.setMaximum(5);
		overwriteButton = WidgetFactory.createCheckButton(optionsGroup, Messages.AutomatedRatingDialog_overwrite, null);
		return area;
	}

	private void fillValues() {
		settings = getDialogSettings(UiActivator.getDefault(), SETTINGSID);
		if (providerViewer != null) {
			providerViewer.setInput(ratingProviderIds);
			selectedProvider = settings.get(PROVIDER);
			if (selectedProvider != null)
				providerViewer.setSelection(new StructuredSelection(selectedProvider));
		}
		updateModelViewer();
		try {
			maxField.setSelection(settings.getInt(MAXRATING));
		} catch (NumberFormatException e) {
			maxField.setSelection(3);
		}
		overwriteButton.setSelection(settings.getBoolean(OVERWRITE));
	}

	private void updateModelViewer() {
		if (selectedProvider != null) {
			String selectedModel = (String) modelViewer.getStructuredSelection().getFirstElement();
			if (selectedModel == null)
				selectedModel = settings.get(MODEL);
			modelIds = aiService.getRatingModelIds(selectedProvider);
			modelLabels = aiService.getRatingModelLabels(selectedProvider);
			modelViewer.setInput(modelIds);
			if (selectedModel != null)
				modelViewer.setSelection(new StructuredSelection(selectedModel));
		}
	}

	@Override
	protected void okPressed() {
		saveValues();
		OperationJob.executeOperation(new AutoRatingOperation(assets, selectedProvider,
				(String) modelViewer.getStructuredSelection().getFirstElement(),
				overwriteButton.getSelection(), maxField.getSelection()), this);
		super.okPressed();
	}

	private void saveValues() {
		String selectedModel = (String) modelViewer.getStructuredSelection().getFirstElement();
		if (selectedProvider != null && selectedModel != null) {
			settings.put(PROVIDER, selectedProvider);
			settings.put(MODEL, selectedModel);
			settings.put(MAXRATING, maxField.getSelection());
			settings.put(OVERWRITE, overwriteButton.getSelection());
		}
	}

	private void validate() {
		String errorMessage = null;
		if (providerViewer != null && providerViewer.getSelection().isEmpty())
			errorMessage = Messages.AutomatedRatingDialog_select_provider;
		if (errorMessage == null && modelViewer.getSelection().isEmpty())
			errorMessage = Messages.AutomatedRatingDialog_select_sujet;
		setErrorMessage(errorMessage);
		getButton(OK).setEnabled(errorMessage == null);
	}

}
