package com.bdaum.zoom.ui.internal.wizards;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.core.internal.operations.AnalogProperties;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.dialogs.KeywordDialog;
import com.bdaum.zoom.ui.internal.widgets.AutoRatingGroup;
import com.bdaum.zoom.ui.internal.widgets.CheckedText;
import com.bdaum.zoom.ui.internal.widgets.RadioButtonGroup;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.bdaum.zoom.ui.widgets.NumericControl;
import com.bdaum.zoom.ui.wizards.ColoredWizardPage;

@SuppressWarnings("restriction")
public class ImportAnalogPropertiesPage extends ColoredWizardPage implements Listener {

	private static final String[] EMULSIONS = new String[] { "Agfacolor", "Agfa Optima", //$NON-NLS-1$ //$NON-NLS-2$
			"Fuji Reala", "Fuji Superia", "Fuji Sensia", "Fuji Velvia", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"Fuji Provia", "Fuji Astia", "Ilford Pan/F", "Ilford Pan/F Plus", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"Ilford FP4", "Ilford FP4 Plus", "Ilford HP5", "Ilford HP5 Plus", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"Ilford Delta", "Ilford XP1", "Ilford XP2", "Ilford SFX", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"Ilford Ortho Plus", "Kodak Ektachrome", "Kodachrome", "Kodacolor", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"Kodak Pro", "Kodak Vericolor", "Kodak Portra", "Kodak T-Max", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"Polaroid Type 100", "Polaroid SX70" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String CAMERA = "camera"; //$NON-NLS-1$
	private static final String LENS = "lens"; //$NON-NLS-1$
	private static final String ARTIST = "artist"; //$NON-NLS-1$
	private static final String COPYRIGHT = "copyright"; //$NON-NLS-1$
	private static final String LIGHTSOURCE = "lightsource"; //$NON-NLS-1$
	private static final String TYPE = "type"; //$NON-NLS-1$
	private static final String EMULSION = "emulsion"; //$NON-NLS-1$
	private static final String DATE = "date"; //$NON-NLS-1$
	private static final String EVENTS = "events"; //$NON-NLS-1$
	private static final String KEYWORDS = "keywords"; //$NON-NLS-1$

	private static final String[] EMPTYSTRINGS = new String[0];

	private static final int[] FACTORS = new int[] { 100, 100, 318, // Messages.QueryField_kodak_disc,
			308, // "8x11mm",
			196, // Messages.QueryField_pocket,
			140, // "18x24mm",
			124, // "24x24mm",
			139, // Messages.QueryField_APS_classic,
			132, // Messages.QueryField_APS_panorama,
			122, // Messages.QueryField_APS_full,
			106, // Messages.QueryField_Instamatic,
			100, 67, // Messages.QueryField_35_panorama,
			84, // "3x4cm",
			74, // "4x4cm",
			55, // Messages.QueryField_4x6_5,
			56, // Messages.QueryField_4_5x6,
			47, // Messages.QueryField_5X7_5,
			50, // "6x6cm",
			49, // "5x7cm",
			45, // "5x8cm",
			39, // "6x9cm",
			34, // "6x11cm",
			26, // "4x5\"",
			15, // "4x10\"",
			19, // "5x7\"",
			13, // "8x10\"",
			8, // "8x20\"",
			9, // "11x14\"",
			6, // "16x20\"",
			5, // "20x24\"",
			52, // Messages.QueryField_ninth_plate,
			39, // Messages.QueryField_sixth_plate,
			31, // Messages.QueryField_quarter_plate,
			23, // Messages.QueryField_half_plate,
			15, // Messages.QueryField_full_plate,
			100 };
//	private IAiService aiService;
	private IDialogSettings settings;
	private Combo makeField;
	private Combo modelField;
	private Combo serialField;
	private Combo typeField;
	private Combo emulsionField;
	private Combo formatField;
	private NumericControl isoField;
	private Combo lensField;
	private Combo lensserialField;
	private NumericControl focalLengthField;
	private NumericControl factorField;
	private NumericControl focalLength35Field;
	private CheckedText notesField;
	private DateTime dateField;
	private DateTime timeField;
	private Combo lightSourceField;
	private Text lvField;
	private Text exposureTimeField;
	private Text fnumberTimeField;
	private Combo artistField;
	private Combo copyrightField;
	private Combo eventField;
	private Text keywordField;
	private RadioButtonGroup privacyGroup;
	private AutoRatingGroup autoGroup;
	private AnalogProperties result;
	private ArrayList<String> focals;
	private ArrayList<String> formats;
	private String[] currentKeywords;

	private static Object encode(String text) {
		text = text.trim();
		if (!text.isEmpty())
			return text;
		return "-"; //$NON-NLS-1$
	}

	private static DateTime createDateInput(Composite composite, String lab, int style) {
		createLabel(composite, lab);
		return new DateTime(composite, style);
	}

	private static Text createText(Composite composite, String lab, int w) {
		createLabel(composite, lab);
		Text text = new Text(composite, SWT.SINGLE | SWT.BORDER);
		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		layoutData.widthHint = w;
		text.setLayoutData(layoutData);
		return text;
	}

	private static CheckedText createTextarea(Composite composite, String lab, int w, int h) {
		createLabel(composite, lab);
		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		layoutData.widthHint = w;
		layoutData.heightHint = h;
		CheckedText checkedText = new CheckedText(composite, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
		checkedText.setLayoutData(layoutData);
		return checkedText;

	}

	private static NumericControl createSpinner(Composite composite, String lab, int w, int incr, int min, int max,
			int digits, boolean logarithmic) {
		createLabel(composite, lab);
		NumericControl spinner = new NumericControl(composite, SWT.NONE);
		spinner.setLogrithmic(logarithmic);
		GridData layoutData = new GridData();
		layoutData.widthHint = w;
		spinner.setLayoutData(layoutData);
		spinner.setMinimum(min);
		spinner.setMaximum(max);
		spinner.setIncrement(incr);
		spinner.setDigits(digits);
		return spinner;
	}

	private static void createLabel(Composite composite, String lab) {
		if (lab != null) {
			Label label = new Label(composite, SWT.RIGHT);
			label.setLayoutData(new GridData(GridData.END, GridData.CENTER, true, false));
			label.setText(lab);
		}
	}

	private static Combo createCombo(Composite composite, String lab, int w, int style, String[] items) {
		createLabel(composite, lab);
		Combo combo = new Combo(composite, SWT.DROP_DOWN | style);
		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		layoutData.widthHint = w;
		combo.setLayoutData(layoutData);
		if (items != null) {
			combo.setItems(items);
			if ((style & SWT.READ_ONLY) != 0)
				combo.setText(items[0]);
			combo.setVisibleItemCount(8);
		} else
			combo.setItems(EMPTYSTRINGS);
		return combo;
	}

	public ImportAnalogPropertiesPage(String pageName) {
		super(pageName);
//		aiService = CoreActivator.getDefault().getAiService();
	}

	public AnalogProperties getResult() {
		return result;
	}

	@Override
	public void createControl(Composite parent) {
		settings = getWizard().getDialogSettings();
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, false));
		Composite left = new Composite(composite, SWT.NONE);
		left.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		left.setLayout(new GridLayout(1, false));
		Composite right = new Composite(composite, SWT.NONE);
		right.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		right.setLayout(new GridLayout(1, false));
		CGroup cameraGroup = CGroup.create(left, 1, Messages.ImportAnalogPropertiesPage_camera);
		makeField = createCombo(cameraGroup, QueryField.EXIF_MAKE.getLabel(), 200, SWT.NONE, null);
		makeField.addListener(SWT.Selection, this);
		modelField = createCombo(cameraGroup, QueryField.EXIF_MODEL.getLabel(), 200, SWT.NONE, null);
		modelField.addListener(SWT.Selection, this);
		serialField = createCombo(cameraGroup, QueryField.EXIF_SERIAL.getLabel(), 200, SWT.NONE, null);
		serialField.addListener(SWT.Selection, this);
		CGroup mediumGroup = CGroup.create(right, 1, Messages.ImportAnalogPropertiesPage_medium);
		typeField = createCombo(mediumGroup, QueryField.ANALOGTYPE.getLabel(), 100, SWT.READ_ONLY,
				QueryField.ANALOGTYPE.getEnumLabels());
		emulsionField = createCombo(mediumGroup, QueryField.EMULSION.getLabel(), 200, SWT.NONE, EMULSIONS);
		formatField = createCombo(mediumGroup, QueryField.ANALOGFORMAT.getLabel(), 100, SWT.READ_ONLY,
				QueryField.ANALOGFORMAT.getEnumLabels());
		formatField.addListener(SWT.Selection, this);
		isoField = createSpinner(mediumGroup, "ISO", 60, 10, 0, 50000, 0, true); //$NON-NLS-1$
		notesField = createTextarea(mediumGroup, QueryField.ANALOGPROCESSING.getLabel(), 200, 40);
		CGroup lensGroup = CGroup.create(left, 1, Messages.ImportAnalogPropertiesPage_lens);
		lensField = createCombo(lensGroup, QueryField.EXIF_LENS.getLabel(), 200, SWT.NONE, null);
		lensField.addListener(SWT.Selection, this);
		lensserialField = createCombo(lensGroup, QueryField.EXIF_LENSSERIAL.getLabel(), 200, SWT.NONE, null);
		lensserialField.addListener(SWT.Selection, this);
		createLabel(lensGroup, QueryField.EXIF_FOCALLENGTH.getLabel());
		Composite focalGroup = new Composite(lensGroup, SWT.NONE);
		focalGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(6, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		focalGroup.setLayout(layout);
		focalLengthField = createSpinner(focalGroup, null, 70, 10, 0, 100000, 1, true);
		focalLengthField.addListener(SWT.Selection, this);
		factorField = createSpinner(focalGroup, "x", 60, 10, 1, 1000, 2, false); //$NON-NLS-1$
		factorField.addListener(SWT.Selection, this);
		focalLength35Field = createSpinner(focalGroup, "=", 80, 1, 3, 10000, 0, true); //$NON-NLS-1$
		focalLength35Field.addListener(SWT.Selection, this);
		new Label(focalGroup, SWT.NONE).setText("@35mm"); //$NON-NLS-1$
		CGroup exposureGroup = CGroup.create(right, 1, Messages.ImportAnalogPropertiesPage_exposure);
		dateField = createDateInput(exposureGroup, QueryField.EXIF_DATETIMEORIGINAL.getLabel(),
				SWT.DATE | SWT.DROP_DOWN);
		timeField = createDateInput(exposureGroup, QueryField.IPTC_DATECREATED.getLabel(), SWT.TIME);
		lightSourceField = createCombo(exposureGroup, QueryField.EXIF_LIGHTSOURCE.getLabel(), 200, SWT.READ_ONLY,
				QueryField.EXIF_LIGHTSOURCE.getEnumLabels());
		lvField = createText(exposureGroup, QueryField.EXIF_LV.getLabel(), 15);
		lvField.addListener(SWT.Modify, this);
		exposureTimeField = createText(exposureGroup, QueryField.EXIF_EXPOSURETIME.getLabel(), 25);
		exposureTimeField.addListener(SWT.Modify, this);
		fnumberTimeField = createText(exposureGroup, QueryField.EXIF_FNUMBER.getLabel(), 15);
		fnumberTimeField.addListener(SWT.Modify, this);
		CGroup iptcGroup = CGroup.create(left, 1, Messages.ImportAnalogPropertiesPage_rights);
		artistField = createCombo(iptcGroup, QueryField.IPTC_BYLINE.getLabel(), 200, SWT.NONE, null);
		copyrightField = createCombo(iptcGroup, QueryField.EXIF_COPYRIGHT.getLabel(), 200, SWT.NONE, null);
		eventField = createCombo(iptcGroup, QueryField.IPTC_EVENT.getLabel(), 200, SWT.NONE, null);
		createLabel(iptcGroup, QueryField.IPTC_KEYWORDS.getLabel());
		keywordField = new Text(iptcGroup, SWT.READ_ONLY | SWT.BORDER | SWT.V_SCROLL);
		GridData layoutData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		layoutData.heightHint = 50;
		keywordField.setLayoutData(layoutData);
		keywordField.addListener(SWT.MouseUp, this);
		createLabel(iptcGroup, QueryField.SAFETY.getLabel());
		privacyGroup = new RadioButtonGroup(iptcGroup, null, SWT.HORIZONTAL, QueryField.SAFETY.getEnumLabels());
//		if (aiService != null && aiService.isEnabled() && aiService.getRatingProviderIds().length > 0) {
//			autoGroup = new AutoRatingGroup(right, aiService, settings);
//			autoGroup.addListener(new Listener() {
//				@Override
//				public void handleEvent(Event event) {
//					validatePage();
//				}
//			});
//		}
		setControl(composite);
		setHelp(HelpContextIds.IMPORT_FROM_DEVICE_WIZARD_ANALOG);
		setTitle(Messages.ImportAnalogPropertiesPage_analog_props);
		setMessage(Messages.ImportAddMetadataPage_specify_metadata);
		super.createControl(parent);
		fillValues();
	}
	
	@Override
	public void handleEvent(Event e) {
		switch (e.type) {
		case SWT.MouseUp:
			openKeywordDialog();
			break;
		case SWT.Modify:
			validatePage();
			break;
		case SWT.Selection:
			if (e.widget == focalLength35Field) {
				int fac = factorField.getSelection();
				focalLengthField.setSelection((1000 * focalLength35Field.getSelection() + fac / 2) / fac);
			} else if (e.widget == formatField)
				setFactor();
			else if (e.widget == makeField || e.widget == modelField || e.widget == serialField) {
				int i = ((Combo) e.widget).getSelectionIndex();
				if (i >= 0) {
					makeField.select(i);
					modelField.select(i);
					serialField.select(i);
					try {
						formatField.select(Integer.parseInt(formats.get(i)));
						setFactor();
					} catch (NumberFormatException e1) {
						// do nothing
					}
				}
			} else if (e.widget == lensField || e.widget == lensserialField) {
				int i = ((Combo) e.widget).getSelectionIndex();
				if (i >= 0) {
					lensField.select(i);
					lensserialField.select(i);
					try {
						focalLengthField.setSelection(Integer.parseInt(focals.get(i)));
						setF35();
					} catch (NumberFormatException ex) {
						// do nothing
					}
				}
			}
			else
				setF35();
			break;
		}
	}

	private void openKeywordDialog() {
		Set<String> selectableKeywords = new HashSet<String>(Core.getCore().getDbManager().getMeta(true).getKeywords());
		KeywordDialog dialog = new KeywordDialog(getShell(), Messages.ImportFromDeviceWizard_Add_keywords_for_import,
				currentKeywords, selectableKeywords, null);
		if (dialog.open() == Dialog.OK) {
			Arrays.parallelSort(currentKeywords = dialog.getResult().getDisplay(), Utilities.KEYWORDCOMPARATOR);
			keywordField.setText(Core.toStringList(currentKeywords, "\n")); //$NON-NLS-1$
		}
	}

	private void fillValues() {
		factorField.setSelection(100);
		isoField.setSelection(100);
		String camera = settings.get(CAMERA);
		if (camera != null) {
			List<String> makes = new ArrayList<String>();
			List<String> models = new ArrayList<String>();
			List<String> serials = new ArrayList<String>();
			formats = new ArrayList<String>();
			StringTokenizer st = new StringTokenizer(camera, "\n"); //$NON-NLS-1$
			int i = 0;
			while (st.hasMoreTokens()) {
				String tok = st.nextToken();
				if ("-".equals(tok)) //$NON-NLS-1$
					tok = ""; //$NON-NLS-1$
				switch (i) {
				case 0:
					makes.add(tok);
					break;
				case 1:
					models.add(tok);
					break;
				case 2:
					serials.add(tok);
					break;
				case 3:
					formats.add(tok);
					break;
				}
				i = (++i) % 4;
			}
			makeField.setItems(makes.toArray(new String[makes.size()]));
			modelField.setItems(models.toArray(new String[models.size()]));
			serialField.setItems(serials.toArray(new String[serials.size()]));
			if (!makes.isEmpty())
				makeField.setText(makes.get(0));
			if (!models.isEmpty())
				modelField.setText(models.get(0));
			if (!serials.isEmpty())
				serialField.setText(serials.get(0));
			if (!formats.isEmpty())
				try {
					int fo = Integer.parseInt(formats.get(0));
					if (fo < formatField.getItemCount()) {
						formatField.select(fo);
						setFactor();
					}
				} catch (NumberFormatException e) {
					// do nothing
				}
		}
		String lens = settings.get(LENS);
		if (lens != null) {
			List<String> lenses = new ArrayList<String>();
			List<String> serials = new ArrayList<String>();
			focals = new ArrayList<String>();
			StringTokenizer st = new StringTokenizer(lens, "\n"); //$NON-NLS-1$
			int i = 0;
			while (st.hasMoreTokens()) {
				String tok = st.nextToken();
				if ("-".equals(tok)) //$NON-NLS-1$
					tok = ""; //$NON-NLS-1$
				switch (i) {
				case 0:
					lenses.add(tok);
					break;
				case 1:
					serials.add(tok);
					break;
				case 2:
					focals.add(tok);
					break;
				}
				i = (++i) % 3;
			}
			lensField.setItems(lenses.toArray(new String[lenses.size()]));
			lensserialField.setItems(serials.toArray(new String[serials.size()]));
			if (!lenses.isEmpty())
				lensField.setText(lenses.get(0));
			if (!serials.isEmpty())
				lensserialField.setText(serials.get(0));
			if (!focals.isEmpty())
				try {
					focalLengthField.setSelection(Integer.parseInt(focals.get(0)));
					setF35();
				} catch (NumberFormatException e) {
					// do nothing
				}
		}
		String s = settings.get(DATE);
		if (s != null)
			try {
				GregorianCalendar cal = new GregorianCalendar();
				cal.setTime(Format.YEAR_DAY_TIME_FORMAT.get().parse(s));
				dateField.setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
				timeField.setTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
			} catch (ParseException e) {
				// do nothing
			}
		try {
			lightSourceField.select(settings.getInt(LIGHTSOURCE));
		} catch (NumberFormatException e) {
			// do nothing
		}
		try {
			typeField.select(settings.getInt(TYPE));
		} catch (NumberFormatException e) {
			// do nothing
		}
		String em = settings.get(EMULSION);
		emulsionField.setText(em != null ? em : ""); //$NON-NLS-1$
		fillCombo(artistField, ARTIST);
		fillCombo(copyrightField, COPYRIGHT);
		fillCombo(eventField, EVENTS);
		String[] keywords = settings.getArray(KEYWORDS);
		if (keywords != null)
			keywordField.setText(Core.toStringList(currentKeywords = keywords, "\n")); //$NON-NLS-1$
		if (autoGroup != null)
			autoGroup.fillValues();
		validatePage();
	}

	protected void validatePage() {
		String errorMessage = null;
		String fn = fnumberTimeField.getText().trim();
		if (!fn.isEmpty())
			try {
				QueryField.EXIF_FNUMBER.getFormatter().fromString(fn);
			} catch (ParseException e) {
				errorMessage = e.getMessage();
			}
		String exp = exposureTimeField.getText().trim();
		if (!exp.isEmpty())
			try {
				QueryField.EXIF_EXPOSURETIME.getFormatter().fromString(exp);
			} catch (ParseException e) {
				errorMessage = e.getMessage();
			}
		String lv = lvField.getText();
		if (!lv.isEmpty())
			try {
				double v = Double.parseDouble(lv);
				if (v < -20 || v > 25)
					errorMessage = Messages.ImportAnalogPropertiesPage_lv_range;
			} catch (NumberFormatException e) {
				errorMessage = Messages.ImportAnalogPropertiesPage_lv_format;
			}
		if (errorMessage == null && autoGroup != null)
			errorMessage = autoGroup.validate();
		setErrorMessage(errorMessage);
		setPageComplete(errorMessage == null);
	}

	private void fillCombo(Combo combo, String key) {
		String items = settings.get(key);
		if (items != null) {
			List<String> list = Core.fromStringList(items, "\n"); //$NON-NLS-1$
			combo.setItems(list.toArray(new String[list.size()]));
			if (!list.isEmpty())
				combo.setText(list.get(0));
		}
	}

	public void performFinish() {
		result = createResult();
		StringBuilder sb = new StringBuilder();
		if (!makeField.getText().trim().isEmpty() && !modelField.getText().trim().isEmpty()) {
			sb.append(encode(makeField.getText())).append('\n').append(encode(modelField.getText())).append('\n')
					.append(encode(serialField.getText())).append('\n').append(formatField.getSelectionIndex());
			String newChoice = sb.toString();
			String[] makes = makeField.getItems();
			String[] models = modelField.getItems();
			String[] serials = serialField.getItems();
			int n = 1;
			if (formats == null)
				formats = new ArrayList<String>(0);
			int l = Math.min(makes.length, Math.min(models.length, Math.min(serials.length, formats.size())));
			for (int i = 0; i < l; i++) {
				String hist = new StringBuilder().append(encode(makes[i])).append('\n').append(encode(models[i]))
						.append('\n').append(encode(serials[i])).append('\n').append(formats.get(i)).toString();
				if (!hist.equals(newChoice) && n < 8) {
					sb.append('\n').append(hist);
					++n;
				}
			}
			settings.put(CAMERA, sb.toString());
			sb.setLength(0);
		}
		if (!lensField.getText().trim().isEmpty()) {
			sb.append(encode(lensField.getText())).append('\n').append(encode(lensserialField.getText())).append('\n')
					.append(focalLengthField.getSelection());
			String newChoice = sb.toString();
			String[] lenses = lensField.getItems();
			String[] serials = lensserialField.getItems();
			int n = 1;
			int l = Math.min(lenses.length, Math.min(serials.length, focals.size()));
			for (int i = 0; i < l; i++) {
				String hist = new StringBuilder().append(encode(lenses[i])).append('\n').append(encode(serials[i]))
						.append('\n').append(focals.get(i)).toString();
				if (!hist.equals(newChoice) && n < 8) {
					sb.append('\n').append(hist);
					++n;
				}
			}
			settings.put(LENS, sb.toString());
			sb.setLength(0);
		}
		String a = artistField.getText().trim();
		if (!a.isEmpty()) {
			int n = 1;
			sb.append(a);
			String newChoice = sb.toString();
			String[] artists = artistField.getItems();
			for (int i = 0; i < artists.length; i++)
				if (!artists[i].equals(newChoice) && n < 8) {
					sb.append('\n').append(artists[i]);
					++n;
				}
			settings.put(ARTIST, sb.toString());
			sb.setLength(0);
		}
		String c = copyrightField.getText().trim();
		if (!c.isEmpty()) {
			int n = 1;
			sb.append(c);
			String newChoice = sb.toString();
			String[] rights = copyrightField.getItems();
			for (int i = 0; i < rights.length; i++)
				if (!rights[i].equals(newChoice) && n < 8) {
					sb.append('\n').append(rights[i]);
					++n;
				}
			settings.put(COPYRIGHT, sb.toString());
		}
		int si = lightSourceField.getSelectionIndex();
		if (si >= 0)
			settings.put(LIGHTSOURCE, si);
		si = typeField.getSelectionIndex();
		if (si >= 0)
			settings.put(TYPE, si);
		String em = emulsionField.getText().trim();
		if (!em.isEmpty())
			settings.put(EMULSION, em);
		settings.put(DATE, Format.YEAR_DAY_TIME_FORMAT.get().format(result.creationDate));
		settings.put(KEYWORDS, currentKeywords);
		if (autoGroup != null)
			autoGroup.saveValues();
	}

	private AnalogProperties createResult() {
		AnalogProperties properties = new AnalogProperties();
		int i = typeField.getSelectionIndex();
		properties.type = ((int[]) QueryField.ANALOGTYPE.getEnumeration())[i];
		String s = emulsionField.getText().trim();
		if (!s.isEmpty())
			properties.emulsion = s;
		i = formatField.getSelectionIndex();
		properties.format = ((int[]) QueryField.ANALOGFORMAT.getEnumeration())[i];
		properties.scalarSpeedRatings = isoField.getSelection();
		s = notesField.getText().trim();
		if (!s.isEmpty())
			properties.processingNotes = s;
		s = makeField.getText().trim();
		if (!s.isEmpty())
			properties.make = s;
		s = modelField.getText().trim();
		if (!s.isEmpty())
			properties.model = s;
		s = serialField.getText().trim();
		if (!s.isEmpty())
			properties.serial = s;
		s = lensField.getText().trim();
		if (!s.isEmpty())
			properties.lens = s;
		s = lensserialField.getText().trim();
		if (!s.isEmpty())
			properties.lensSerial = s;
		properties.focalLength = (double) focalLengthField.getSelection() / 10;
		properties.focalLengthFactor = (double) factorField.getSelection() / 100;
		properties.focalLengthIn35MmFilm = focalLength35Field.getSelection();
		properties.creationDate = new GregorianCalendar(dateField.getYear(), dateField.getMonth(), dateField.getDay(),
				timeField.getHours(), timeField.getMinutes(), timeField.getSeconds()).getTime();
		properties.lightSource = ((int[]) QueryField.EXIF_LIGHTSOURCE.getEnumeration())[lightSourceField
				.getSelectionIndex()];
		String lv = lvField.getText().trim();
		if (!lv.isEmpty())
			try {
				properties.lv = Double.parseDouble(lv);
			} catch (NumberFormatException e) {
				// do nothing
			}
		String exp = exposureTimeField.getText().trim();
		if (!exp.isEmpty())
			try {
				properties.exposureTime = (Double) QueryField.EXIF_EXPOSURETIME.getFormatter().fromString(exp);
			} catch (ParseException e) {
				properties.exposureTime = Double.NaN;
			}
		String fn = fnumberTimeField.getText().trim();
		if (!fn.isEmpty())
			try {
				properties.fNumber = (Double) QueryField.EXIF_FNUMBER.getFormatter().fromString(fn);
			} catch (ParseException e) {
				properties.fNumber = Double.NaN;
			}
		s = artistField.getText().trim();
		if (!s.isEmpty())
			properties.artist = s;
		s = copyrightField.getText().trim();
		if (!s.isEmpty())
			properties.copyright = s;
		s = eventField.getText().trim();
		if (!s.isEmpty())
			properties.event = s;
		s = keywordField.getText().trim();
		if (!s.isEmpty())
			properties.keywords = Core.fromStringList(s, "\n"); //$NON-NLS-1$
		properties.safety = privacyGroup.getSelection();
		if (autoGroup != null) {
			String providerId = autoGroup.getProviderId();
			if (providerId != null) {
				properties.providerId = providerId;
				properties.modelId = autoGroup.getModelId();
				properties.maxRating = autoGroup.getMaxRating();
				properties.overwriteRating = autoGroup.getOverwrite();
			}
		}
		return properties;
	}

	private void setFactor() {
		factorField.setSelection(FACTORS[formatField.getSelectionIndex()]);
		setF35();
	}

	private void setF35() {
		focalLength35Field.setSelection((focalLengthField.getSelection() * factorField.getSelection() + 500) / 1000);
	}

}
