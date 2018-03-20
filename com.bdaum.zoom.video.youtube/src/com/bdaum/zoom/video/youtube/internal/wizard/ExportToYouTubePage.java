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
package com.bdaum.zoom.video.youtube.internal.wizard;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.scohen.juploadr.uploadapi.CommunicationException;
import org.scohen.juploadr.uploadapi.ProtocolException;
import org.scohen.juploadr.uploadapi.Session;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.net.communities.CommunityAccount;
import com.bdaum.zoom.net.communities.ui.AbstractCommunityExportWizard;
import com.bdaum.zoom.net.communities.ui.AbstractExportToCommunityPage;
import com.bdaum.zoom.ui.internal.ZViewerComparator;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.bdaum.zoom.video.youtube.internal.HelpContextIds;
import com.bdaum.zoom.video.youtube.internal.YouTubeUploadClient;
import com.bdaum.zoom.video.youtube.internal.job.ExportToYouTubeJob;

@SuppressWarnings("restriction")
public class ExportToYouTubePage extends AbstractExportToCommunityPage {

	private static final String SELECTED_CAT = "selectedCat"; //$NON-NLS-1$

	private static final String HIDE_KEYWORDS = "hideKeywords"; //$NON-NLS-1$

	private static final String HIDE_GEO = "hideGeo"; //$NON-NLS-1$

	private String msg;
	private ComboViewer catViewer;

	private CheckboxButton keywordsButton;

	private CheckboxButton geoButton;

	private Map<String, String> categories;

	public ExportToYouTubePage(IConfigurationElement configElement,
			List<Asset> assets, String id, String title,
			ImageDescriptor titleImage) {
		super(configElement, assets, id, title, titleImage);
		int size = assets.size();
		msg = size == 0 ? Messages.ExportToYouTubePage_no_video_to_export
				: (size == 1) ? Messages.ExportToYouTubePage_exporting_one_video : NLS.bind(
						Messages.ExportToYouTubePage_exporting_n_video, size);
	}

	@SuppressWarnings("unused")
	@Override
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 1);
		new Label(composite, SWT.NONE);
		createAccountGroup(composite);
		CGroup group = CGroup.create(composite, 1, Messages.ExportToYouTubePage_settings);
		new Label(group, SWT.NONE).setText(Messages.ExportToYouTubePage_youtube_category);
		catViewer = new ComboViewer(group);
		catViewer.setContentProvider(new IStructuredContentProvider() {

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
				// do nothing
			}

			public void dispose() {
				// do nothing
			}

			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof Map) {
					return ((Map<?, ?>) inputElement).keySet().toArray();
				}
				return new Object[0];
			}
		});
		catViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				String label = categories.get(element);
				return (label != null) ? label : super.getText(element);
			}
		});
		catViewer.setComparator(ZViewerComparator.INSTANCE);
		YouTubeUploadClient api = (YouTubeUploadClient) ((AbstractCommunityExportWizard) getWizard())
				.getApi();
		try {
			categories = api.getCategories();
		} catch (IOException e) {
			setErrorMessage(NLS.bind(Messages.ExportToYouTubePage_error_fetching_category,
					api.getSiteName(), e));
		}
		if (categories != null)
			catViewer.setInput(categories);
		keywordsButton = WidgetFactory.createCheckButton(group,
				Messages.ExportToYouTubePage_include_keywords, new GridData(SWT.BEGINNING, SWT.CENTER,
						false, false, 2, 1));
		keywordsButton.setSelection(true);
		geoButton = WidgetFactory.createCheckButton(group,
				Messages.ExportToYouTubePage_include_geo, new GridData(SWT.BEGINNING,
						SWT.CENTER, false, false, 2, 1));
		geoButton.setSelection(true);
		setTitle(Messages.ExportToYouTubePage_exporting);
		setMessage(msg);
		fillValues();
		setControl(composite);
		setHelp(HelpContextIds.EXPORT_WIZARD);
		super.createControl(parent);
	}

	@Override
	protected void fillValues() {
		super.fillValues();
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			String selectedCat = settings.get(SELECTED_CAT);
			if (selectedCat != null)
				catViewer.setSelection(new StructuredSelection(selectedCat));
			keywordsButton.setSelection(!settings.getBoolean(HIDE_KEYWORDS));
			geoButton.setSelection(!settings.getBoolean(HIDE_GEO));
		}
	}

	@Override
	protected void saveSettings() {
		super.saveSettings();
		IDialogSettings settings = getDialogSettings();
		String firstElement = (String) ((IStructuredSelection) catViewer
				.getSelection()).getFirstElement();
		if (firstElement != null)
			settings.put(SELECTED_CAT, firstElement);
		settings.put(HIDE_KEYWORDS, !keywordsButton.getSelection());
		settings.put(HIDE_GEO, !geoButton.getSelection());
	}

	@Override
	protected void validatePage() {
		String message = assets.isEmpty() ? Messages.ExportToYouTubePage_no_video_selected
				: checkAccount();
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	@Override
	protected boolean doFinish(CommunityAccount acc) {
		YouTubeUploadClient api = (YouTubeUploadClient) ((AbstractCommunityExportWizard) getWizard())
				.getApi();
		try {
			Session session = new Session(api, acc);
			try {
				session.init();
				if (api.authenticate(session)) {
					String category = (String) ((IStructuredSelection) catViewer
							.getSelection()).getFirstElement();
					ExportToYouTubeJob job = new ExportToYouTubeJob(
							configElement, assets, session, category,
							keywordsButton.getSelection(),
							geoButton.getSelection(), this);
					job.schedule();
					return true;
				}
				setErrorMessage(Messages.ExportToYouTubePage_user_auth_failed);
			} catch (CommunicationException e) {
				setErrorMessage(Messages.ExportToYouTubePage_communication_failed);
			} catch (ProtocolException e) {
				setErrorMessage(NLS.bind(Messages.ExportToYouTubePage_wrong_protocol, Constants.APPNAME));
			}
		} catch (Exception e1) {
			setErrorMessage(Messages.ExportToYouTubePage_check_account);
		}
		return false;
	}

}
