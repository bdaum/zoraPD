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
package com.bdaum.zoom.net.communities.ui;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.scohen.juploadr.app.PhotoSet;
import org.scohen.juploadr.app.tags.Tag;
import org.scohen.juploadr.uploadapi.AuthException;
import org.scohen.juploadr.uploadapi.CommunicationException;
import org.scohen.juploadr.uploadapi.IErrorHandler;
import org.scohen.juploadr.uploadapi.Session;

import com.bdaum.aoModeling.runtime.AomMap;
import com.bdaum.zoom.cat.model.group.Criterion;
import com.bdaum.zoom.cat.model.group.CriterionImpl;
import com.bdaum.zoom.cat.model.group.GroupImpl;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.meta.Category;
import com.bdaum.zoom.cat.model.meta.CategoryImpl;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.net.communities.CommunitiesActivator;
import com.bdaum.zoom.net.communities.CommunityAccount;
import com.bdaum.zoom.net.communities.CommunityApi;
import com.bdaum.zoom.net.communities.HelpContextIds;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.UiConstants;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.ZViewerComparator;
import com.bdaum.zoom.ui.internal.dialogs.KeywordGroup;
import com.bdaum.zoom.ui.internal.operations.ModifyMetaOperation;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.ExpandCollapseGroup;
import com.bdaum.zoom.ui.internal.widgets.RadioButtonGroup;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.bdaum.zoom.ui.widgets.NumericControl;

@SuppressWarnings("restriction")
public class EditCommunityAccountDialog extends ZTitleAreaDialog implements IErrorHandler {

	private static final Object[] EMPTY = new Object[0];
	private static final String SETTINGSID = "editCommunitiesAccountDialog"; //$NON-NLS-1$
	private CommunityAccount account;
	private Text nameField;
	private CheckboxButton albumButton;
	private KeywordGroup keywordGroup;
	private CheckboxButton keyWordButton;
	private String accessType;
	private Set<String> accessDetails;
	private boolean useDefaultDetails;
	private CheckboxButton limitedButton;
	private NumericControl bandwidthField;
	private Text usedBandwidthField;
	private NumberFormat nf = NumberFormat.getNumberInstance();
	private Text trafficLimitField;
	private Text filesizeLimitField;
	private Text typeField;
	private CheckboxButton trackField;
	private Text visitField;
	private CGroup restrictionGroup;
	private IConfigurationElement configuration;
	private Text albumLimitField;
	private Text usedAlbumsField;
	private String pw;
	private Session session;
	private Map<String, PhotoSet> externalAlbums;
	private CheckboxTableViewer allCatViewer;
	private Button updateAllCatButton;
	private CheckboxTreeViewer usedCatViewer;
	private Button updateUsedCatButton;
	private Set<String> externalCategories;
	private Set<Category> externalUsedCat;
	private boolean offline;
	private final CommunityApi api;
	private IDialogSettings settings;

	public EditCommunityAccountDialog(Shell parentShell, CommunityAccount account, CommunityApi api) {
		super(parentShell, HelpContextIds.COMMUNITYACCOUNT_DIALOG);
		this.account = account;
		this.api = api;
		if (api != null)
			api.setErrorHandler(this);
		settings = getDialogSettings(CommunitiesActivator.getDefault(), SETTINGSID);
	}

	@Override
	public void create() {
		if (api != null)
			try {
				this.session = api.getSession(account);
			} catch (AuthException e) {
				// do nothing
			} catch (CommunicationException e) {
				offline = true;
			}
		configuration = account.getConfiguration();
		super.create();
		String name = ((IConfigurationElement) configuration.getParent()).getAttribute("name"); //$NON-NLS-1$
		setTitle(NLS.bind(Messages.EditCommunityAccountDialog_account_details, name));
		fillValues();
		setMessage(Messages.EditCommunityAccountDialog_please_fill_in_details);
		updateButtons();
	}

	@Override
	public boolean close() {
		if (session != null)
			session.close();
		return super.close();
	}

	private void updateButtons() {
		Button okbutton = getButton(IDialogConstants.OK_ID);
		if (okbutton != null) {
			boolean valid = validate();
			okbutton.setEnabled(valid);
			getShell().setModified(valid);
			if (valid)
				setErrorMessage(null);
		}
	}

	private boolean validate() {
		String msg = null;
		if (offline) {
			String communityName = ((IConfigurationElement) account.getConfiguration().getParent())
					.getAttribute("name"); //$NON-NLS-1$
			msg = NLS.bind(Messages.EditCommunityAccountDialog_connection_failed, account.getName(), communityName);
		} else if (nameField != null && nameField.getText().isEmpty())
			msg = Messages.EditCommunityAccountDialog_please_specify_account_name;
		if (msg != null)
			setErrorMessage(msg);
		return msg == null;
	}

	private void fillValues() {
		setText(nameField, account.getName());
		pw = session == null ? null : session.getPassword();
		if (pw == null) {
			int len = account.getPasswordLength();
			pw = "________________________________________________".substring(0, len); //$NON-NLS-1$
		}
		if (passwordField != null)
			setText(passwordField, pw);
		String type = account.getAccountType();
		if (type == null)
			typeField.setText(Messages.EditCommunityAccountDialog_unknown);
		else {
			IConfigurationElement[] children = configuration.getChildren("accountType"); //$NON-NLS-1$
			for (IConfigurationElement child : children)
				if (type.equals(child.getAttribute("key"))) { //$NON-NLS-1$
					typeField.setText(child.getAttribute("label")); //$NON-NLS-1$
					break;
				}
		}
		trackField.setSelection(account.isTrackExport());
		if (keyWordButton != null)
			keyWordButton.setSelection(account.isKeywordsAsTags());
		setText(visitField, account.getVisit());
		if (albumButton != null)
			albumButton.setSelection(account.isAlbumsAsSets());
		if (catButton != null)
			catButton.setSelection(account.isPropagateCategories());
		limitedButton.setSelection(account.isBandwidthLimited());
		bandwidthField.setSelection(account.getBandwidth());
		nf.setMaximumFractionDigits(1);
		trafficLimitField.setText(formatBandwidth(account.getTrafficLimit()));
		usedBandwidthField.setText(formatBandwidth(account.getCurrentUploadUsed()));
		trafficLimitField.setEnabled(!account.isUnlimited());
		usedBandwidthField.setEnabled(!account.isUnlimited());
		filesizeLimitField.setText(formatBandwidth(account.getMaxFilesize()));
		filesizeLimitField.setEnabled(account.getMaxFilesize() == Integer.MAX_VALUE);
		if (account.getAvailableAlbums() < 0) {
			if (albumLimitField != null)
				albumLimitField.setText(Messages.EditCommunityAccountDialog_unknown);
			if (usedAlbumsField != null)
				usedAlbumsField.setText("-"); //$NON-NLS-1$
		} else if (account.getAvailableAlbums() == Integer.MAX_VALUE) {
			if (albumLimitField != null)
				albumLimitField.setText(Messages.EditCommunityAccountDialog_unlimited);
			if (usedAlbumsField != null)
				usedAlbumsField.setText("-"); //$NON-NLS-1$
		} else {
			if (albumLimitField != null)
				albumLimitField.setText(String.valueOf(account.getAvailableAlbums()));
			if (usedAlbumsField != null)
				usedAlbumsField.setText(String.valueOf(account.getUsedAlbums()));
		}
		updateTestButton();
	}

	private String formatBandwidth(long v) {
		if (v < 0)
			return Messages.EditCommunityAccountDialog_unknown;
		if (v == Long.MAX_VALUE)
			return Messages.EditCommunityAccountDialog_unlimited;
		return nf.format(v / (1024d * 1024d));
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(1, false));
		CTabFolder tabFolder = new CTabFolder(composite, SWT.TOP);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createOverviewGroup(UiUtilities.createTabPage(tabFolder, Messages.EditCommunityAccountDialog_general, null));
		if (api != null) {
			if (api.isSupportsPhotosets())
				createAlbumGroup(UiUtilities.createTabPage(tabFolder,
						Messages.EditCommunityAccountDialog_photosets_albums, null));
			if (api.isSupportsTagging())
				createTagGroup(UiUtilities.createTabPage(tabFolder, Messages.EditCommunityAccountDialog_tags, null));
			if (api.isSupportsCategories())
				createCategoryGroup(
						UiUtilities.createTabPage(tabFolder, Messages.EditCommunityAccountDialog_categories, null));
		}
		createCommunicationGroup(
				UiUtilities.createTabPage(tabFolder, Messages.EditCommunityAccountDialog_Capacity, null));
		return area;
	}

	private void createCommunicationGroup(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		CGroup group1 = UiUtilities.createGroup(parent, 2, Messages.EditCommunityAccountDialog_bandwidth);
		limitedButton = WidgetFactory.createCheckButton(group1, Messages.EditCommunityAccountDialog_limit_bandwidth,
				new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		limitedButton.addListener(new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				bandwidthField.setEnabled(limitedButton.getSelection());
			}
		});
		bandwidthField = new NumericControl(group1, NumericControl.LOGARITHMIC);
		bandwidthField.setMinimum(10);
		bandwidthField.setMaximum(1000000);
		bandwidthField.setIncrement(10);
		restrictionGroup = UiUtilities.createGroup(parent, 2, Messages.EditCommunityAccountDialog_restrictions);
		trafficLimitField = createTextField(restrictionGroup, Messages.EditCommunityAccountDialog_available_bandwidth,
				60, SWT.READ_ONLY, 1);
		usedBandwidthField = createTextField(restrictionGroup, Messages.EditCommunityAccountDialog_used_bandwidth, 60,
				SWT.READ_ONLY, 1);
		filesizeLimitField = createTextField(restrictionGroup, Messages.EditCommunityAccountDialog_maximum_file_size,
				60, SWT.READ_ONLY, 1);
		if (api.isSupportsPhotosets()) {
			albumLimitField = createTextField(restrictionGroup, Messages.EditCommunityAccountDialog_available_albums,
					60, SWT.READ_ONLY, 1);
			usedAlbumsField = createTextField(restrictionGroup, Messages.EditCommunityAccountDialog_used_albums, 60,
					SWT.READ_ONLY, 1);
		}
	}

	private Listener accessTypeSelectionListener = new Listener() {
		@Override
		public void handleEvent(Event e) {
			int index = accessButtonGroup.getSelection();
			accessType = (String) accessButtonGroup.getData(index, "data"); //$NON-NLS-1$
			for (int i = 0; i < accessButtonGroup.size(); i++) {
				@SuppressWarnings("unchecked")
				List<CheckboxButton> childButtons = (List<CheckboxButton>) accessButtonGroup.getData(i, "children"); //$NON-NLS-1$
				if (childButtons != null)
					for (CheckboxButton childButton : childButtons)
						childButton.setEnabled(i == index);
			}
		}
	};

	private Listener accessDetailSelectionListener = new Listener() {
		@Override
		public void handleEvent(Event e) {
			String detail = (String) e.widget.getData(UiConstants.DATA);
			if (((Button) e.widget).getSelection())
				accessDetails.add(detail);
			else
				accessDetails.remove(detail);
		}
	};
	private Button testUrlButton;
	private CheckboxButton catButton;
	private Text passwordField;
	private CheckboxTableViewer albumViewer;
	private Button importAlbumsButton;
	private RadioButtonGroup accessButtonGroup;

	private void createOverviewGroup(Composite parent) {
		accessType = account.getAccessType();
		accessDetails = account.getAccessDetails();
		if (accessDetails == null) {
			accessDetails = new HashSet<String>();
			useDefaultDetails = true;
		}
		parent.setLayout(new GridLayout(1, false));
		CGroup group1 = UiUtilities.createGroup(parent, 3, Messages.EditCommunityAccountDialog_account);
		nameField = createTextField(group1, Messages.EditCommunityAccountDialog_name, 150, SWT.NONE, 2);
		if ("password".equals(configuration.getAttribute("authentification"))) //$NON-NLS-1$ //$NON-NLS-2$
			passwordField = createTextField(group1, Messages.EditCommunityAccountDialog_password, 150, SWT.PASSWORD, 2);
		typeField = createTextField(group1, Messages.EditCommunityAccountDialog_account_type, 150, SWT.READ_ONLY, 2);
		visitField = createTextField(group1, Messages.EditCommunityAccountDialog_web_url, 250, SWT.NONE, 1);
		testUrlButton = new Button(group1, SWT.PUSH);
		testUrlButton.setText(Messages.EditCommunityAccountDialog_test);
		testUrlButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				testUrl();
			}
		});
		visitField.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				updateTestButton();
			}
		});
		trackField = WidgetFactory.createCheckButton(group1, Messages.EditCommunityAccountDialog_track_exports,
				new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 3, 1));
		IConfigurationElement[] children = configuration.getChildren("accessType"); //$NON-NLS-1$
		if (children.length > 0) {
			CGroup group2 = UiUtilities.createGroup(parent, 1, Messages.EditCommunityAccountDialog_privacy);
			accessButtonGroup = new RadioButtonGroup(group2, null, SWT.NONE);
			accessButtonGroup.addListener(accessTypeSelectionListener);
			int k = 0;
			for (IConfigurationElement child : children) {
				accessButtonGroup.addButton(child.getAttribute("label")); //$NON-NLS-1$
				String key = child.getAttribute("key"); //$NON-NLS-1$
				accessButtonGroup.setData(k, "data", key); //$NON-NLS-1$
				boolean selected = false;
				if (accessType != null)
					selected = accessType.equals(key);
				else {
					String isdeflt = child.getAttribute("isDefault"); //$NON-NLS-1$
					if (isdeflt != null && Boolean.parseBoolean(isdeflt)) {
						selected = true;
						accessType = key;
					}
				}
				if (selected)
					accessButtonGroup.setSelection(k);
				IConfigurationElement[] grandchildren = child.getChildren("accessTypeDetail"); //$NON-NLS-1$
				if (grandchildren.length > 0) {
					List<CheckboxButton> childButtons = new ArrayList<>();
					for (IConfigurationElement grandchild : grandchildren) {
						GridData layoutData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1);
						layoutData.horizontalIndent = 10;
						CheckboxButton dbutton = WidgetFactory.createCheckButton(accessButtonGroup,
								grandchild.getAttribute("label"), //$NON-NLS-1$
								layoutData);
						dbutton.setEnabled(selected);
						String key2 = grandchild.getAttribute("key"); //$NON-NLS-1$
						dbutton.setData(UiConstants.DATA, key2);
						if (useDefaultDetails) {
							String init = child.getAttribute("initValue"); //$NON-NLS-1$
							if (init != null && Boolean.parseBoolean(init)) {
								accessButtonGroup.setSelection(k);
								accessDetails.add(key2);
							}
						} else
							dbutton.setSelection(accessDetails.contains(key2));
						dbutton.addListener(accessDetailSelectionListener);
						childButtons.add(dbutton);
					}
					accessButtonGroup.setData(k, "children", childButtons); //$NON-NLS-1$
				}
				++k;
			}
		}
		if (api != null && (api.isSupportsTagging() || api.isSupportsPhotosets()
				|| api.isSupportsCategories() && !api.isSetCategories())) {
			CGroup group3 = UiUtilities.createGroup(parent, 2, Messages.EditCommunityAccountDialog_metadata);
			if (api.isSupportsTagging())
				keyWordButton = WidgetFactory.createCheckButton(group3,
						Messages.EditCommunityAccountDialog_assign_keywords_as_tags,
						new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
			if (api.isSupportsPhotosets())
				albumButton = WidgetFactory.createCheckButton(group3,
						Messages.EditCommunityAccountDialog_create_photo_sets_for_albums,
						new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
			if (api.isSupportsCategories() && !api.isSetCategories())
				catButton = WidgetFactory.createCheckButton(group3,
						Messages.EditCommunityAccountDialog_propagate_categories,
						new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		}
	}

	private void createTagGroup(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		Set<String> kw = new HashSet<String>();
		for (Tag tag : account.getDefaultTags())
			kw.add(tag.toDisplayString());
		String[] selectedKeywords = kw.toArray(new String[kw.size()]);
		Set<String> userKeywords = new HashSet<String>();
		for (Tag tag : account.getUserTags())
			userKeywords.add(tag.toDisplayString());
		List<String> recentKeywords = new ArrayList<String>();
		for (Tag tag : account.getRecentTags())
			recentKeywords.add(tag.toDisplayString());
		keywordGroup = new KeywordGroup(parent, selectedKeywords, userKeywords, recentKeywords, true, settings);
	}

	private void createAlbumGroup(Composite parent) {
		IDbManager db = Core.getCore().getDbManager();
		List<SmartCollectionImpl> albums = db.obtainObjects(SmartCollectionImpl.class, "album", true, //$NON-NLS-1$
				QueryField.EQUALS);
		List<PhotoSet> photosets = account.getPhotosets();
		Map<String, PhotoSet> remoteAlbums = new HashMap<String, PhotoSet>(photosets.size() * 5 / 4 + 1);
		for (PhotoSet photoset : photosets)
			remoteAlbums.put(photoset.getTitle(), photoset);
		final Map<String, SmartCollectionImpl> localAlbums = new HashMap<String, SmartCollectionImpl>(
				albums.size() * 5 / 4 + 1);
		externalAlbums = new HashMap<String, PhotoSet>(remoteAlbums);
		for (SmartCollectionImpl album : albums) {
			String name = Utilities.getExternalAlbumName(album);
			externalAlbums.remove(name);
			localAlbums.put(name, album);
		}
		parent.setLayout(new GridLayout());
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout());

		albumViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER | SWT.V_SCROLL);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.heightHint = 400;
		albumViewer.getControl().setLayoutData(layoutData);
		albumViewer.setContentProvider(ArrayContentProvider.getInstance());
		albumViewer.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof PhotoSet)
					return ((PhotoSet) element).getTitle();
				return element.toString();
			}

			@Override
			public Font getFont(Object element) {
				if (element instanceof PhotoSet && externalAlbums.containsKey(((PhotoSet) element).getTitle()))
					return JFaceResources.getBannerFont();
				return super.getFont(element);
			}

		});
		albumViewer.setComparator(ZViewerComparator.INSTANCE);
		albumViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				updateAlbumsButton();
			}
		});
		albumViewer.setInput(photosets);
		importAlbumsButton = new Button(composite, SWT.PUSH);
		importAlbumsButton.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		importAlbumsButton.setText(Messages.EditCommunityAccountDialog_import_into_catalog);
		importAlbumsButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				BusyIndicator.showWhile(e.display, () -> importAlbums());
			}

			private void importAlbums() {
				final IDbManager dbm = Core.getCore().getDbManager();
				GroupImpl importedAlbums = dbm.obtainById(GroupImpl.class, Constants.GROUP_ID_IMPORTED_ALBUMS);
				if (importedAlbums == null) {
					importedAlbums = new GroupImpl(Messages.EditCommunityAccountDialog_imported_albums, true,
							Constants.INHERIT_LABEL, null, 0, null);
					importedAlbums.setStringId(Constants.GROUP_ID_IMPORTED_ALBUMS);
				}
				Object[] checkedElements = albumViewer.getCheckedElements();
				final List<Object> collections = new ArrayList<Object>(checkedElements.length + 1);
				for (Object obj : checkedElements) {
					PhotoSet photoSet = (PhotoSet) obj;
					String title = photoSet.getTitle();
					SmartCollectionImpl sm = localAlbums.get(title);
					if (sm == null) {
						sm = new SmartCollectionImpl(title, false, true, false, false, photoSet.getDescription(), 0,
								null, 0, null, Constants.INHERIT_LABEL, null, 0, null);
						sm.setGroup_rootCollection_parent(Constants.GROUP_ID_IMPORTED_ALBUMS);
						List<Criterion> criteria = new ArrayList<Criterion>(1);
						criteria.add(new CriterionImpl(Constants.OID, null, sm.getStringId(), QueryField.XREF, false));
						sm.setCriterion(criteria);
						importedAlbums.addRootCollection(sm.getStringId());
					} else
						sm.setDescription(photoSet.getDescription());
					collections.add(sm);
					externalAlbums.remove(title);
					albumViewer.setChecked(obj, false);
				}
				collections.add(importedAlbums);
				dbm.safeTransaction(null, collections);
				Core.getCore().fireStructureModified();
				updateAlbumsButton();
			}
		});
		updateAlbumsButton();
	}

	private void updateAlbumsButton() {
		importAlbumsButton.setEnabled(albumViewer.getCheckedElements().length > 0);
	}

	private ModifyListener modifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			updateButtons();
		}
	};

	private void createCategoryGroup(Composite parent) {
		List<? extends org.scohen.juploadr.app.Category> categories = account.getCategories();
		IDbManager db = Core.getCore().getDbManager();
		final Meta meta = db.getMeta(true);
		Map<String, Category> localCategories = meta.getCategory();
		externalCategories = new HashSet<String>(categories.size() * 5 / 4 + 1);
		for (org.scohen.juploadr.app.Category cat : categories)
			externalCategories.add(cat.getTitle());
		externalCategories.removeAll(localCategories.keySet());
		parent.setLayout(new GridLayout());
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout());
		Label allCatLabel = new Label(composite, SWT.NONE);
		allCatLabel.setText(Messages.EditCommunityAccountDialog_all_categories);
		allCatViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER | SWT.V_SCROLL);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.heightHint = 150;
		allCatViewer.getControl().setLayoutData(layoutData);
		allCatViewer.setContentProvider(ArrayContentProvider.getInstance());
		allCatViewer.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof org.scohen.juploadr.app.Category)
					return ((org.scohen.juploadr.app.Category) element).getTitle();
				return element.toString();
			}

			@Override
			public Font getFont(Object element) {
				if (element instanceof org.scohen.juploadr.app.Category
						&& externalCategories.contains(((org.scohen.juploadr.app.Category) element).getTitle()))
					return JFaceResources.getBannerFont();
				return super.getFont(element);
			}

		});
		allCatViewer.setComparator(ZViewerComparator.INSTANCE);
		allCatViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				Object element = event.getElement();
				if (!externalCategories.contains(((org.scohen.juploadr.app.Category) element).getTitle()))
					allCatViewer.setChecked(element, false);
				updateCatButtons();
			}
		});
		allCatViewer.setInput(categories);
		updateAllCatButton = new Button(composite, SWT.PUSH);
		updateAllCatButton.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		updateAllCatButton.setText(Messages.EditCommunityAccountDialog_import_into_catalog);
		updateAllCatButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				BusyIndicator.showWhile(e.display, () -> importAllCats());
			}

			private void importAllCats() {
				Object[] checkedElements = allCatViewer.getCheckedElements();
				Map<String, Category> newCats = new HashMap<String, Category>(meta.getCategory());
				for (Object element : checkedElements) {
					org.scohen.juploadr.app.Category cat = (org.scohen.juploadr.app.Category) element;
					String label = cat.getTitle();
					CategoryImpl newCat = new CategoryImpl(label);
					newCats.put(label, newCat);
					allCatViewer.setChecked(element, false);
					externalCategories.remove(label);
				}
				updateMeta(meta, newCats);
				updateCatButtons();
			}

		});
		// Part 2
		Map<String, Category> usedCategories = new HashMap<String, Category>();
		externalUsedCat = new HashSet<Category>();
		List<PhotoSet> photosets = account.getPhotosets();
		for (PhotoSet photoSet : photosets) {
			org.scohen.juploadr.app.Category category = photoSet.getCategory();
			if (category != null) {
				String title = category.getTitle();
				Category usedCat = usedCategories.get(title);
				if (usedCat == null) {
					usedCat = new CategoryImpl(title);
					usedCategories.put(title, usedCat);
				}
				Category localCat = localCategories.get(title);
				if (localCat == null)
					externalUsedCat.add(usedCat);
				org.scohen.juploadr.app.Category subcategory = photoSet.getSubcategory();
				if (subcategory != null) {
					String stitle = subcategory.getTitle();
					Category usedSub = usedCat.getSubCategory(stitle);
					if (usedSub == null) {
						usedSub = new CategoryImpl(stitle);
						usedCat.putSubCategory(usedSub);
					}
					if (localCat == null || localCat.getSubCategory() == null
							|| !localCat.getSubCategory().containsKey(stitle))
						externalUsedCat.add(usedSub);
				}
			}
		}
		Label usedCatLabel = new Label(composite, SWT.NONE);
		usedCatLabel.setText(Messages.EditCommunityAccountDialog_used_categories);
		ExpandCollapseGroup expandCollapseGroup = new ExpandCollapseGroup(composite, SWT.NONE);
		usedCatViewer = new CheckboxTreeViewer(composite, SWT.BORDER | SWT.V_SCROLL);
		expandCollapseGroup.setViewer(usedCatViewer);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.heightHint = 100;
		usedCatViewer.getControl().setLayoutData(layoutData);
		usedCatViewer.setContentProvider(new ITreeContentProvider() {
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// do nothing
			}

			public void dispose() {
				// do nothing
			}

			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof Map<?, ?>) {
					return ((Map<?, ?>) inputElement).values().toArray();
				}
				return EMPTY;
			}

			public boolean hasChildren(Object element) {
				return getChildren(element).length > 0;
			}

			public Object getParent(Object element) {
				if (element instanceof Category)
					return ((Category) element).getCategory_subCategory_parent();
				return null;
			}

			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof Category) {
					AomMap<String, Category> subCategories = ((Category) parentElement).getSubCategory();
					if (subCategories != null) {
						List<Category> children = new ArrayList<>(subCategories.size());
						for (Category cat : subCategories.values())
							if (cat != null)
								children.add(cat);
						return children.toArray();
					}
				}
				return EMPTY;
			}
		});
		usedCatViewer.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Category)
					return ((Category) element).getLabel();
				return element.toString();
			}

			@Override
			public Font getFont(Object element) {
				if (element instanceof Category && externalUsedCat.contains(element))
					return JFaceResources.getBannerFont();
				return super.getFont(element);
			}

		});
		usedCatViewer.setComparator(ZViewerComparator.INSTANCE);
		usedCatViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				Object element = event.getElement();
				if (element instanceof Category && event.getChecked()) {
					if (!externalUsedCat.contains(element))
						usedCatViewer.setChecked(element, false);
					else {
						Category pcat = ((Category) element).getCategory_subCategory_parent();
						if (pcat != null && externalUsedCat.contains(pcat))
							usedCatViewer.setChecked(pcat, true);
					}
				}
				updateCatButtons();
			}
		});
		UiUtilities.installDoubleClickExpansion(usedCatViewer);
		usedCatViewer.setInput(usedCategories);
		usedCatViewer.expandAll();
		updateUsedCatButton = new Button(composite, SWT.PUSH);
		updateUsedCatButton.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		updateUsedCatButton.setText(Messages.EditCommunityAccountDialog_import_into_catalog);
		updateUsedCatButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				BusyIndicator.showWhile(e.display, () -> importAllUsedCats());
			}

			private void importAllUsedCats() {
				Object[] checkedElements = usedCatViewer.getCheckedElements();
				Map<String, Category> newCats = new HashMap<String, Category>(meta.getCategory());
				for (Object element : checkedElements) {
					Category cat = (Category) element;
					Category pcat = cat.getCategory_subCategory_parent();
					if (pcat != null) {
						Category p2 = newCats.get(pcat.getLabel());
						if (p2 != null) {
							pcat.removeSubCategory(cat.getLabel());
							p2.putSubCategory(cat);
						} else
							newCats.put(pcat.getLabel(), pcat);
					} else if (newCats.get(cat.getLabel()) == null)
						newCats.put(cat.getLabel(), cat);
					externalUsedCat.remove(cat);
					usedCatViewer.setChecked(cat, false);
					if (pcat != null) {
						externalUsedCat.remove(pcat);
						usedCatViewer.setChecked(pcat, false);
					}
				}
				updateMeta(meta, newCats);
				updateCatButtons();
			}
		});
		updateCatButtons();
	}

	protected void updateMeta(final Meta meta, Map<String, Category> newCats) {
		ModifyMetaOperation op = new ModifyMetaOperation(meta, false, null, null, null, null, null, null, null, null,
				meta.getCumulateImports(), null, null, null, newCats, null, meta.getThumbnailFromPreview(), null, null,
				meta.getFolderWatchLatency(), meta.getPauseFolderWatch(), meta.getReadonly(), meta.getAutoWatch(),
				meta.getSharpen(), meta.getWebpCompression(), meta.getJpegQuality(), meta.getNoIndex(),
				meta.getLocale(), meta.getCbirAlgorithms(), meta.getIndexedTextFields(), meta.getPersonsToKeywords(),
				null, meta.getVocabularies());
		OperationJob.executeOperation(op, EditCommunityAccountDialog.this);
	}

	protected void updateCatButtons() {
		boolean readOnly = Core.getCore().getDbManager().isReadOnly();
		updateAllCatButton.setEnabled(!readOnly && allCatViewer.getCheckedElements().length > 0);
		updateUsedCatButton.setEnabled(!readOnly && usedCatViewer.getCheckedElements().length > 0);
	}

	private Text createTextField(Composite g, String text, int width, int style, int span) {
		Label label = new Label(g, SWT.NONE);
		label.setText(text);
		Text textField = new Text(g, SWT.SINGLE | SWT.LEAD | SWT.BORDER | style);
		GridData data = (width <= 0) ? new GridData(SWT.FILL, SWT.CENTER, true, false) : new GridData(width, -1);
		data.horizontalSpan = span;
		textField.setLayoutData(data);
		textField.addModifyListener(modifyListener);
		return textField;
	}

	@Override
	protected void okPressed() {
		if (keywordGroup != null)
			keywordGroup.commit();
		if (passwordField != null) {
			String text = passwordField.getText();
			account.setPasswordLength(text.length());
			if (!text.equals(pw))
				account.setPasswordHash(api.encodePassword(text));
		}
		if (nameField != null)
			account.setName(nameField.getText());
		if (albumButton != null)
			account.setAlbumsAsSets(albumButton.getSelection());
		if (catButton != null)
			account.setPropagateCategories(catButton.getSelection());
		if (keyWordButton != null)
			account.setKeywordsAsTags(keyWordButton.getSelection());
		if (keywordGroup != null) {
			account.setDefaultTags(keywordToTag(keywordGroup.getResult().getDisplay()));
			List<String> recentKeywords = keywordGroup.getRecentKeywords();
			account.setRecentTags(keywordToTag(recentKeywords.toArray(new String[recentKeywords.size()])));
		}
		account.setAccessDetails(accessDetails);
		account.setAccessType(accessType);
		account.setBandwidth(bandwidthField.getSelection());
		account.setBandwidthLimited(limitedButton.getSelection());
		account.setTrackExport(trackField.getSelection());
		account.setVisit(visitField.getText());
		super.okPressed();
	}

	private static List<Tag> keywordToTag(String[] keywords) {
		List<Tag> tags = new ArrayList<Tag>();
		for (String kw : keywords)
			tags.add(new Tag(kw));
		return tags;
	}

	public CommunityAccount getResult() {
		return account;
	}

	protected void testUrl() {
		String backup = account.getVisit();
		account.setVisit(visitField.getText());
		setErrorMessage(account.testVisit());
		account.setVisit(backup);
	}

	private void updateTestButton() {
		testUrlButton.setEnabled(!visitField.getText().isEmpty());
	}

	public void handleError(Object source, Exception e) {
		offline = true;
		updateButtons();
	}

}
