package com.bdaum.zoom.ui.internal.wizards;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.cat.model.meta.LastDeviceImport;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.internal.ImportFromDeviceData;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.dialogs.KeywordDialog;
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

	public ImportAddMetadataPage(String pageName, boolean media) {
		super(pageName);
		this.media = media;
	}

	@SuppressWarnings("unused")
	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout());

		CGroup metaComp = new CGroup(comp, SWT.NONE);
		metaComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		metaComp.setLayout(new GridLayout(4, false));
		metaComp.setText(Messages.ImportAddMetadataPage_add_metadata);

		ImportFromDeviceWizard wizard = (ImportFromDeviceWizard) getWizard();
		IDialogSettings dialogSettings = wizard.getDialogSettings();

		artistField = createHistoryCombo(metaComp,
				Messages.ImportFromDeviceWizard_Artist, dialogSettings, ARTISTS);
		String lastArtist = dialogSettings.get(LAST_ARTIST);
		if (lastArtist != null)
			artistField.setText(lastArtist);
		presetAuthor = artistField.getText();
		eventField = createHistoryCombo(metaComp,
				Messages.ImportFromDeviceWizard_Event, dialogSettings, EVENTS);
		Label label = new Label(metaComp, SWT.NONE);
		label.setText(Messages.ImportFromDeviceWizard_Keywords);
		keywordField = new Text(metaComp, SWT.READ_ONLY | SWT.BORDER
				| SWT.V_SCROLL);
		GridData layoutData = new GridData(SWT.FILL, SWT.BEGINNING, true,
				false, 3, 1);
		layoutData.heightHint = 50;
		keywordField.setLayoutData(layoutData);
		keywordField.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				openKeywordDialog();
			}
		});
		if (media) {
			prefixComp = new CGroup(comp, SWT.NONE);
			prefixComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
					false));
			prefixComp.setLayout(new GridLayout(4, false));
			prefixComp.setText(Messages.ImportAddMetadataPage_advanced_options);
			Label msg = new Label(prefixComp, SWT.NONE);
			msg.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
					false, 4, 1));
			msg.setText(Messages.ImportAddMetadataPage_metadata_options_chdk);

			Label prefixLabel = new Label(prefixComp, SWT.NONE);
			prefixLabel
					.setText(Messages.ImportFromDeviceWizard_enabling_prefix);
			prefixField = new Text(prefixComp, SWT.BORDER);
			GridData data = new GridData(GridData.BEGINNING, GridData.CENTER,
					false, false);
			data.widthHint = 50;
			prefixField.setLayoutData(data);
			prefixField.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseDoubleClick(MouseEvent e) {
					if (prefixField.getText().length() == 0)
						prefixField.setText("IMG_"); //$NON-NLS-1$
				}
			});
			Label explanation = new Label(prefixComp, SWT.WRAP);
			data = new GridData();
			data.widthHint = 500;
			data.horizontalIndent = 15;
			data.horizontalSpan = 3;
			explanation.setLayoutData(data);
			explanation
					.setText(Messages.ImportFromDeviceWizard_exif_data_are_transferred);
			new Label(prefixComp, SWT.NONE);
		}
		setControl(comp);
		setHelp(HelpContextIds.IMPORT_FROM_DEVICE_WIZARD_METADATA);
		setTitle(Messages.ImportAddMetadataPage_add_metadata);
		setMessage(Messages.ImportAddMetadataPage_specify_metadata);
		super.createControl(parent);
		fillValues();
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible && prefixComp != null)
			prefixComp.setVisible(((ImportFromDeviceWizard) getWizard())
					.needsAdvancedOptions());
	}

	private void openKeywordDialog() {
		Meta meta = Core.getCore().getDbManager().getMeta(true);
		Set<String> selectableKeywords = new HashSet<String>(meta.getKeywords());
		KeywordDialog dialog = new KeywordDialog(getShell(),
				Messages.ImportFromDeviceWizard_Add_keywords_for_import,
				currentKeywords, selectableKeywords, null);
		if (dialog.open() == Dialog.OK) {
			Arrays.sort(currentKeywords = dialog.getResult().getDisplay(),Utilities.KEYWORDCOMPARATOR);
			keywordField.setText(Core.toStringList(currentKeywords, "\n")); //$NON-NLS-1$
		}
	}

	private static Combo createHistoryCombo(Composite parent, String lab,
			IDialogSettings settings, String key) {
		new Label(parent, SWT.NONE).setText(lab);
		Combo combo = new Combo(parent, SWT.NONE);
		String[] items = settings.getArray(key);
		if (items != null) {
			combo.setItems(items);
			combo.setVisibleItemCount(8);
		} else
			combo.setItems(EMPTYSTRINGS);
		combo.setData("key", key); //$NON-NLS-1$
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		return combo;
	}

	private static void saveComboHistory(Combo combo, IDialogSettings settings2) {
		settings2
				.put((String) combo.getData("key"), UiUtilities.updateComboHistory(combo)); //$NON-NLS-1$
	}

	private void fillValues() {
		IDialogSettings dialogSettings = getWizard().getDialogSettings();
		String[] keywords = dialogSettings.getArray(KEYWORDS);
		if (keywords != null)
			keywordField.setText(Core.toStringList(currentKeywords = keywords,
					"\n")); //$NON-NLS-1$
		if (prefixField != null) {
			String prefix = dialogSettings.get(PREFIX);
			if (prefix != null)
				prefixField.setText(prefix);
		}
		updateAuthor();
	}

	public void updateAuthor() {
		if (artistField.getText().equals(presetAuthor)) {
			File[] dcims = ((ImportFromDeviceWizard) getWizard()).getDcims();
			if (dcims.length > 0) {
				String key = Core.getCore().getVolumeManager()
						.getVolumeForFile(dcims[0]);
				LastDeviceImport lastImport = Core.getCore().getDbManager()
						.getMeta(true).getLastDeviceImport(key);
				if (lastImport != null) {
					String owner = lastImport.getOwner();
					if (owner != null && owner.length() > 0) {
						artistField.setText(owner);
						presetAuthor = owner;
					}
				}
			}
		}
	}

	@Override
	protected void validatePage() {
		// do nothing
	}

	public void performFinish(ImportFromDeviceData importData) {
		IDialogSettings dialogSettings = getWizard().getDialogSettings();
		saveComboHistory(artistField, dialogSettings);
		String artist = artistField.getText();
		importData.setArtist(artist);
		dialogSettings.put(LAST_ARTIST, artist);
		saveComboHistory(eventField, dialogSettings);
		importData.setEvent(eventField.getText());
		dialogSettings.put(KEYWORDS, currentKeywords);
		importData.setKeywords(currentKeywords);
		if (prefixField != null) {
			String prefix = prefixField.getText();
			importData.setExifTransferPrefix(prefix);
			dialogSettings.put(PREFIX, prefix);
		}
	}

}
