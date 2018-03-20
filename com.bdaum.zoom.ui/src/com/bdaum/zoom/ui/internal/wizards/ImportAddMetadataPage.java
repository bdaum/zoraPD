package com.bdaum.zoom.ui.internal.wizards;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.meta.LastDeviceImport;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.ImportFromDeviceData;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.core.internal.ai.IAiService;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiConstants;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.dialogs.KeywordDialog;
import com.bdaum.zoom.ui.internal.widgets.AutoRatingGroup;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.RadioButtonGroup;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.bdaum.zoom.ui.wizards.ColoredWizardPage;

@SuppressWarnings("restriction")
public class ImportAddMetadataPage extends ColoredWizardPage {

	private static final String[] EMPTYSTRINGS = new String[0];
	private static final String ARTISTS = "artists"; //$NON-NLS-1$
	private static final String LAST_ARTIST = "lastArtist"; //$NON-NLS-1$
	private static final String EVENTS = "events"; //$NON-NLS-1$
	private static final String KEYWORDS = "keywords"; //$NON-NLS-1$
	private static final String PREFIX = "exifPrefix"; //$NON-NLS-1$

	private Combo artistField;
	private Combo eventField;
	private Text keywordField;
	private String[] currentKeywords;
	private Text prefixField;
	private CGroup prefixComp;
	private final boolean media;
	private String presetAuthor;
	private RadioButtonGroup privacyGroup;
	private IAiService aiService;
	private IDialogSettings dialogSettings;
	private SmartCollectionImpl collection;
	private CheckboxButton albumButton;
	private boolean newStruct;
	private AutoRatingGroup autoGroup;

	public ImportAddMetadataPage(String pageName, boolean media, SmartCollectionImpl collection, boolean newStruct) {
		super(pageName);
		this.media = media;
		this.collection = collection;
		this.newStruct = newStruct;
		aiService = CoreActivator.getDefault().getAiService();
	}

	@SuppressWarnings("unused")
	@Override
	public void createControl(Composite parent) {
		dialogSettings = getWizard().getDialogSettings();
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout());
		CGroup metaComp = CGroup.create(comp, 1, Messages.ImportAddMetadataPage_add_metadata);
		((GridLayout) metaComp.getLayout()).numColumns = 4;
		artistField = createHistoryCombo(metaComp, QueryField.EXIF_ARTIST.getLabel(), dialogSettings, ARTISTS);
		String lastArtist = dialogSettings.get(LAST_ARTIST);
		if (lastArtist != null)
			artistField.setText(lastArtist);
		presetAuthor = artistField.getText();
		eventField = createHistoryCombo(metaComp, QueryField.IPTC_EVENT.getLabel(), dialogSettings, EVENTS);
		new Label(metaComp, SWT.NONE).setText(QueryField.IPTC_KEYWORDS.getLabel());
		keywordField = new Text(metaComp, SWT.READ_ONLY | SWT.BORDER | SWT.V_SCROLL);
		GridData layoutData = new GridData(SWT.FILL, SWT.BEGINNING, true, false, 3, 1);
		layoutData.heightHint = 50;
		keywordField.setLayoutData(layoutData);
		keywordField.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				openKeywordDialog();
			}
		});
		new Label(metaComp, SWT.NONE).setText(QueryField.SAFETY.getLabel());
		privacyGroup = new RadioButtonGroup(metaComp, null, SWT.HORIZONTAL, QueryField.SAFETY.getEnumLabels());
		privacyGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 3, 1));
		if (collection != null && collection.getAlbum() && !collection.getSystem()) {
			albumButton = WidgetFactory.createCheckButton(metaComp,
					NLS.bind(Messages.ImportAddMetadataPage_add_to_album, collection.getName()),
					new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 4, 1));
			albumButton.setSelection(true);
		}
		// Auto rating
		if (aiService != null && aiService.isEnabled() && aiService.getRatingProviderIds().length > 0) {
			autoGroup = new AutoRatingGroup(comp, aiService, dialogSettings);
			autoGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
			autoGroup.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					validatePage();
				}
			});
		}
		// CHDK
		if (media) {
			prefixComp = CGroup.create(comp, 1, Messages.ImportAddMetadataPage_advanced_options);
			((GridLayout) prefixComp.getLayout()).numColumns = 4;
			Label msg = new Label(prefixComp, SWT.NONE);
			msg.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 4, 1));
			msg.setText(Messages.ImportAddMetadataPage_metadata_options_chdk);

			Label prefixLabel = new Label(prefixComp, SWT.NONE);
			prefixLabel.setText(Messages.ImportFromDeviceWizard_enabling_prefix);
			prefixField = new Text(prefixComp, SWT.BORDER);
			GridData data = new GridData(GridData.BEGINNING, GridData.CENTER, false, false);
			data.widthHint = 50;
			prefixField.setLayoutData(data);
			prefixField.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseDoubleClick(MouseEvent e) {
					if (prefixField.getText().isEmpty())
						prefixField.setText("IMG_"); //$NON-NLS-1$
				}
			});
			Label explanation = new Label(prefixComp, SWT.WRAP);
			data = new GridData();
			data.widthHint = 500;
			data.horizontalIndent = 15;
			data.horizontalSpan = 3;
			explanation.setLayoutData(data);
			explanation.setText(Messages.ImportFromDeviceWizard_exif_data_are_transferred);
			new Label(prefixComp, SWT.NONE);
		}
		setControl(comp);
		setHelp(media ? HelpContextIds.IMPORT_FROM_DEVICE_WIZARD_METADATA
				: newStruct ? HelpContextIds.IMPORT_NEW_STRUCTURE_WIZARD_METADATA
						: HelpContextIds.IMPORT_FOLDER_WIZARD_METADATA);

		setTitle(Messages.ImportAddMetadataPage_add_metadata);
		setMessage(Messages.ImportAddMetadataPage_specify_metadata);
		super.createControl(parent);
		fillValues();
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible && prefixComp != null)
			prefixComp.setVisible(((ImportFromDeviceWizard) getWizard()).needsAdvancedOptions());
	}

	private void openKeywordDialog() {
		Meta meta = Core.getCore().getDbManager().getMeta(true);
		Set<String> selectableKeywords = new HashSet<String>(meta.getKeywords());
		KeywordDialog dialog = new KeywordDialog(getShell(), Messages.ImportFromDeviceWizard_Add_keywords_for_import,
				currentKeywords, selectableKeywords, null);
		if (dialog.open() == Dialog.OK) {
			Arrays.sort(currentKeywords = dialog.getResult().getDisplay(), Utilities.KEYWORDCOMPARATOR);
			keywordField.setText(Core.toStringList(currentKeywords, "\n")); //$NON-NLS-1$
		}
	}

	private static Combo createHistoryCombo(Composite parent, String lab, IDialogSettings settings, String key) {
		new Label(parent, SWT.NONE).setText(lab);
		Combo combo = new Combo(parent, SWT.NONE);
		String[] items = settings.getArray(key);
		if (items != null) {
			combo.setItems(items);
			combo.setVisibleItemCount(8);
		} else
			combo.setItems(EMPTYSTRINGS);
		combo.setData(UiConstants.KEY, key); 
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		return combo;
	}

	private static void saveComboHistory(Combo combo, IDialogSettings settings) {
		settings.put((String) combo.getData(UiConstants.KEY), UiUtilities.updateComboHistory(combo)); 
	}

	private void fillValues() {
		String[] keywords = dialogSettings.getArray(KEYWORDS);
		if (keywords != null)
			keywordField.setText(Core.toStringList(currentKeywords = keywords, "\n")); //$NON-NLS-1$
		if (prefixField != null) {
			String prefix = dialogSettings.get(PREFIX);
			if (prefix != null)
				prefixField.setText(prefix);
		}
		updateAuthor();
		privacyGroup.setSelection(0);
		if (autoGroup != null)
			autoGroup.fillValues();
		validatePage();
	}

	public void updateAuthor() {
		if (artistField.getText().equals(presetAuthor)) {
			String volume = ((ImportFromDeviceWizard) getWizard()).getVolume();
			if (volume != null) {
				LastDeviceImport lastImport = Core.getCore().getDbManager().getMeta(true).getLastDeviceImport(volume);
				if (lastImport != null) {
					String owner = lastImport.getOwner();
					if (owner != null && !owner.isEmpty())
						artistField.setText(presetAuthor = owner);
				}
			}
		}
	}


	@Override
	protected void validatePage() {
		String errorMessage = autoGroup != null ? autoGroup.validate() : null;
		setErrorMessage(errorMessage);
		setPageComplete(errorMessage == null);
	}

	public void performFinish(ImportFromDeviceData importData) {
		saveComboHistory(artistField, dialogSettings);
		String artist = artistField.getText();
		importData.setArtist(artist);
		dialogSettings.put(LAST_ARTIST, artist);
		saveComboHistory(eventField, dialogSettings);
		importData.setEvent(eventField.getText());
		dialogSettings.put(KEYWORDS, currentKeywords);
		importData.setKeywords(currentKeywords);
		importData.setPrivacy(privacyGroup.getSelection() * 3);
		if (prefixField != null) {
			String prefix = prefixField.getText();
			importData.setExifTransferPrefix(prefix);
			dialogSettings.put(PREFIX, prefix);
		}
		if (autoGroup != null)
			autoGroup.saveValues();
	}

	public boolean addToAlbum() {
		return albumButton != null && albumButton.getSelection();
	}

	public String getProviderId() {
		return autoGroup != null  ? autoGroup.getProviderId() : null;
	}

	public String getModelId() {
		return autoGroup != null  ? autoGroup.getModelId() : null;
	}

	public boolean getOverwrite() {
		return autoGroup != null  ? autoGroup.getOverwrite() : false;
	}

	public int getMaxRating() {
		return autoGroup != null  ? autoGroup.getMaxRating() : 3;
	}

}
