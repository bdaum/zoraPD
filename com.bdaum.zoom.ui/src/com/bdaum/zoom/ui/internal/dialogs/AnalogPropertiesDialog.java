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
 * (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.ui.internal.dialogs;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.operations.AnalogProperties;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.widgets.CheckedText;

@SuppressWarnings("restriction")
public class AnalogPropertiesDialog extends ZTitleAreaDialog {

	private static final String[] EMULSIONS = new String[] {
			"Agfacolor", "Agfa Optima", //$NON-NLS-1$ //$NON-NLS-2$
			"Fuji Reala", "Fuji Superia", "Fuji Sensia", "Fuji Velvia", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"Fuji Provia", "Fuji Astia", "Ilford Pan/F", "Ilford Pan/F Plus", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"Ilford FP4", "Ilford FP4 Plus", "Ilford HP5", "Ilford HP5 Plus", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"Ilford Delta", "Ilford XP1", "Ilford XP2", "Ilford SFX", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"Ilford Ortho Plus", "Kodak Ektachrome", "Kodachrome", "Kodacolor", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"Kodak Pro", "Kodak Vericolor", "Kodak Portra", "Kodak T-Max", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"Polaroid Type 100", "Polaroid SX70" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String SETTINGSID = "com.bdaum.zoom.analogProperties"; //$NON-NLS-1$
	private static final String CAMERA = "camera"; //$NON-NLS-1$
	private static final String LENS = "lens"; //$NON-NLS-1$
	private static final String ARTIST = "artist"; //$NON-NLS-1$
	private static final String COPYRIGHT = "copyright"; //$NON-NLS-1$
	private static final String LIGHTSOURCE = "lightsource"; //$NON-NLS-1$
	private static final String TYPE = "type"; //$NON-NLS-1$
	private static final String EMULSION = "emulsion"; //$NON-NLS-1$
	private static final String DATE = "date"; //$NON-NLS-1$
	private static final SimpleDateFormat sf = new SimpleDateFormat(
			"yyyy-DDD HH:mm:ss"); //$NON-NLS-1$

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
	private Combo emulsionField;
	private Combo formatField;
	private Combo typeField;
	private Spinner isoField;
	private CheckedText notesField;
	private Combo makeField;
	private Combo modelField;
	private Combo serialField;
	private Combo lensField;
	private Combo lensserialField;
	private Spinner focalLengthField;
	private Spinner factorField;
	private Spinner focalLength35Field;
	private DateTime dateField;
	private DateTime timeField;
	private Combo lightSourceField;
	private Text lvField;
	private Text exposureTimeField;
	private Text fnumberTimeField;
	private Combo artistField;
	private Combo copyrightField;
	private List<String> formats;
	private List<String> focals;
	private AnalogProperties result;
	private IDialogSettings settings;

	public AnalogPropertiesDialog(Shell parentShell) {
		super(parentShell, HelpContextIds.ANALOG_IMPORT_DIALOG);
//		titleSize = 3;
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.AnalogPropertiesDialog_import_analog_images);
		setMessage(Messages.AnalogPropertiesDialog_specify_metadata);
		fillFields();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, false));
		makeField = createCombo(composite, QueryField.EXIF_MAKE.getLabel(),
				200, SWT.NONE, null);
		SelectionAdapter cameraSelectionListener = new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				int i = ((Combo) e.widget).getSelectionIndex();
				if (i >= 0) {
					makeField.select(i);
					modelField.select(i);
					serialField.select(i);
					try {
						int fo = Integer.parseInt(formats.get(i));
						formatField.select(fo);
						setFactor();
					} catch (NumberFormatException e1) {
						// do nothing
					}
				}
			}
		};
		makeField.addSelectionListener(cameraSelectionListener);
		modelField = createCombo(composite, QueryField.EXIF_MODEL.getLabel(),
				200, SWT.NONE, null);
		modelField.addSelectionListener(cameraSelectionListener);
		serialField = createCombo(composite, QueryField.EXIF_SERIAL.getLabel(),
				200, SWT.NONE, null);
		serialField.addSelectionListener(cameraSelectionListener);
		createSeparator(composite);
		typeField = createCombo(composite, QueryField.ANALOGTYPE.getLabel(),
				100, SWT.READ_ONLY, QueryField.ANALOGTYPE.getEnumLabels());
		emulsionField = createCombo(composite, QueryField.EMULSION.getLabel(),
				200, SWT.NONE, EMULSIONS);
		formatField = createCombo(composite,
				QueryField.ANALOGFORMAT.getLabel(), 100, SWT.READ_ONLY,
				QueryField.ANALOGFORMAT.getEnumLabels());
		formatField.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				setFactor();
			}
		});
		isoField = createSpinner(composite, "ISO", 50, 10, 0, 50000, 0); //$NON-NLS-1$
		notesField = createTextarea(composite,
				QueryField.ANALOGPROCESSING.getLabel(), 200, 40);
		createSeparator(composite);
		SelectionAdapter lensSelectionListener = new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				int i = ((Combo) e.widget).getSelectionIndex();
				if (i >= 0) {
					lensField.select(i);
					lensserialField.select(i);
					try {
						int f = Integer.parseInt(focals.get(i));
						focalLengthField.setSelection(f);
						setF35();
					} catch (NumberFormatException e1) {
						// do nothing
					}
				}
			}
		};
		lensField = createCombo(composite, QueryField.EXIF_LENS.getLabel(),
				200, SWT.NONE, null);
		lensField.addSelectionListener(lensSelectionListener);
		lensserialField = createCombo(composite,
				QueryField.EXIF_LENSSERIAL.getLabel(), 200, SWT.NONE, null);
		lensserialField.addSelectionListener(lensSelectionListener);
		createLabel(composite, QueryField.EXIF_FOCALLENGTH.getLabel());
		Composite focalGroup = new Composite(composite, SWT.NONE);
		focalGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(6, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		focalGroup.setLayout(layout);
		focalLengthField = createSpinner(focalGroup, null, 25, 10, 0, 100000, 1);
		SelectionAdapter flListener = new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				setF35();
			}
		};
		focalLengthField.addSelectionListener(flListener);
		factorField = createSpinner(focalGroup, "x", 25, 10, 1, 300, 2); //$NON-NLS-1$
		factorField.addSelectionListener(flListener);
		focalLength35Field = createSpinner(focalGroup, "=", 25, 1, 0, 50000, 0); //$NON-NLS-1$
		focalLength35Field.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				int fac = factorField.getSelection();
				int f35 = focalLength35Field.getSelection();
				int f = (1000 * f35 + fac / 2) / fac;
				focalLengthField.setSelection(f);
			}
		});
		Label label = new Label(focalGroup, SWT.NONE);
		label.setText("@35mm"); //$NON-NLS-1$
		createSeparator(composite);
		dateField = createDateInput(composite,
				QueryField.EXIF_DATETIMEORIGINAL.getLabel(), SWT.DATE
						| SWT.DROP_DOWN);
		timeField = createDateInput(composite,
				Messages.AnalogPropertiesDialog_creation_time, SWT.TIME);
		lightSourceField = createCombo(composite,
				QueryField.EXIF_LIGHTSOURCE.getLabel(), 200, SWT.READ_ONLY,
				QueryField.EXIF_LIGHTSOURCE.getEnumLabels());
		lvField = createText(composite, QueryField.EXIF_LV.getLabel(), 15);
		ModifyListener mlistener = new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				updateButtons();
			}
		};
		lvField.addModifyListener(mlistener);
		exposureTimeField = createText(composite,
				QueryField.EXIF_EXPOSURETIME.getLabel(), 25);
		exposureTimeField.addModifyListener(mlistener);
		fnumberTimeField = createText(composite,
				QueryField.EXIF_FNUMBER.getLabel(), 15);
		fnumberTimeField.addModifyListener(mlistener);
		createSeparator(composite);
		artistField = createCombo(composite, QueryField.IPTC_BYLINE.getLabel(),
				200, SWT.NONE, null);
		copyrightField = createCombo(composite,
				QueryField.EXIF_COPYRIGHT.getLabel(), 200, SWT.NONE, null);
		return area;
	}

	private void setF35() {
		int f = focalLengthField.getSelection();
		int fac = factorField.getSelection();
		int f35 = (f * fac + 500) / 1000;
		focalLength35Field.setSelection(f35);
	}

	private static void createSeparator(Composite composite) {
		Label label = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		layoutData.horizontalSpan = 2;
		label.setLayoutData(layoutData);
	}

	protected void updateButtons() {
		Button okbutton = getButton(IDialogConstants.OK_ID);
		if (okbutton != null) {
			String errorMessage = null;
			String fn = fnumberTimeField.getText().trim();
			if (!fn.isEmpty()) {
				try {
					QueryField.EXIF_FNUMBER.getFormatter().fromString(fn);
				} catch (ParseException e) {
					errorMessage = e.getMessage();
				}
			}
			String exp = exposureTimeField.getText().trim();
			if (!exp.isEmpty()) {
				try {
					QueryField.EXIF_EXPOSURETIME.getFormatter().fromString(exp);
				} catch (ParseException e) {
					errorMessage = e.getMessage();
				}
			}
			String lv = lvField.getText();
			if (!lv.isEmpty()) {
				try {
					double v = Double.parseDouble(lv);
					if (v < -20 || v > 25)
						errorMessage = Messages.AnalogPropertiesDialog_lv_range_warning;
				} catch (NumberFormatException e) {
					errorMessage = Messages.AnalogPropertiesDialog_lv_must_be_a_number;
				}
			}
			setErrorMessage(errorMessage);
			boolean enabled = errorMessage == null;
			getShell().setModified(enabled);
			okbutton.setEnabled(enabled);
		}
	}

	private static DateTime createDateInput(Composite composite, String lab, int style) {
		createLabel(composite, lab);
		DateTime dateTime = new DateTime(composite, style);
		return dateTime;
	}

	private void fillFields() {
		settings = getDialogSettings(UiActivator.getDefault(),SETTINGSID);
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
			lensserialField
					.setItems(serials.toArray(new String[serials.size()]));
			if (!lenses.isEmpty())
				lensField.setText(lenses.get(0));
			if (!serials.isEmpty())
				lensserialField.setText(serials.get(0));
			if (!focals.isEmpty())
				try {
					int f = Integer.parseInt(focals.get(0));
					focalLengthField.setSelection(f);
					setF35();
				} catch (NumberFormatException e) {
					// do nothing
				}
		}
		String s = settings.get(DATE);
		if (s != null) {
			try {
				Date d = sf.parse(s);
				GregorianCalendar cal = new GregorianCalendar();
				cal.setTime(d);
				dateField
						.setDate(cal.get(Calendar.YEAR),
								cal.get(Calendar.MONTH),
								cal.get(Calendar.DAY_OF_MONTH));
				timeField.setTime(cal.get(Calendar.HOUR_OF_DAY),
						cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
			} catch (ParseException e) {
				// do nothing
			}
		}
		String artist = settings.get(ARTIST);
		if (artist != null) {
			List<String> artists = Core.fromStringList(artist, "\n"); //$NON-NLS-1$
			artistField.setItems(artists.toArray(new String[artists.size()]));
			if (!artists.isEmpty())
				artistField.setText(artists.get(0));
		}
		String copyright = settings.get(COPYRIGHT);
		if (copyright != null) {
			List<String> rights = Core.fromStringList(copyright, "\n"); //$NON-NLS-1$
			copyrightField.setItems(rights.toArray(new String[rights.size()]));
			if (!rights.isEmpty())
				copyrightField.setText(rights.get(0));
		}
		try {
			int si = settings.getInt(LIGHTSOURCE);
			lightSourceField.select(si);
		} catch (NumberFormatException e) {
			// do nothing
		}
		try {
			int si = settings.getInt(TYPE);
			typeField.select(si);
		} catch (NumberFormatException e) {
			// do nothing
		}
		String em = settings.get(EMULSION);
		if (em != null)
			emulsionField.setText(em);
		else {
			emulsionField.setText(""); //$NON-NLS-1$
		}
	}

	@Override
	protected void okPressed() {
		result = createResult();
		StringBuilder sb = new StringBuilder();
		if (!makeField.getText().trim().isEmpty()
				&& !modelField.getText().trim().isEmpty()) {
			sb.append(encode(makeField.getText())).append('\n')
					.append(encode(modelField.getText())).append('\n')
					.append(encode(serialField.getText())).append('\n')
					.append(formatField.getSelectionIndex());
			String newChoice = sb.toString();
			String[] makes = makeField.getItems();
			String[] models = modelField.getItems();
			String[] serials = serialField.getItems();
			int n = 1;
			if (formats == null)
				formats = new ArrayList<String>(0);
			int l = Math.min(
					makes.length,
					Math.min(models.length,
							Math.min(serials.length, formats.size())));
			for (int i = 0; i < l; i++) {
				StringBuilder buf = new StringBuilder();
				buf.append(encode(makes[i])).append('\n')
						.append(encode(models[i])).append('\n')
						.append(encode(serials[i])).append('\n')
						.append(formats.get(i));
				String hist = buf.toString();
				if (!hist.equals(newChoice) && n < 8) {
					sb.append('\n').append(hist);
					++n;
				}
			}
			settings.put(CAMERA, sb.toString());
			sb.setLength(0);
		}
		if (!lensField.getText().trim().isEmpty()) {
			sb.append(encode(lensField.getText())).append('\n')
					.append(encode(lensserialField.getText())).append('\n')
					.append(focalLengthField.getSelection());
			String newChoice = sb.toString();
			String[] lenses = lensField.getItems();
			String[] serials = lensserialField.getItems();
			int n = 1;
			int l = Math.min(lenses.length,
					Math.min(serials.length, focals.size()));
			for (int i = 0; i < l; i++) {
				StringBuilder buf = new StringBuilder();
				buf.append(encode(lenses[i])).append('\n')
						.append(encode(serials[i])).append('\n')
						.append(focals.get(i));
				String hist = buf.toString();
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
			for (int i = 0; i < artists.length; i++) {
				if (!artists[i].equals(newChoice) && n < 8) {
					sb.append('\n').append(artists[i]);
					++n;
				}
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
			for (int i = 0; i < rights.length; i++) {
				if (!rights[i].equals(newChoice) && n < 8) {
					sb.append('\n').append(rights[i]);
					++n;
				}
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
		settings.put(DATE, sf.format(result.creationDate));
		super.okPressed();
	}

	private static Object encode(String text) {
		text = text.trim();
		if (!text.isEmpty())
			return text;
		return "-"; //$NON-NLS-1$
	}

	private static Text createText(Composite composite, String lab, int w) {
		createLabel(composite, lab);
		Text text = new Text(composite, SWT.SINGLE | SWT.BORDER);
		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		layoutData.widthHint = w;
		text.setLayoutData(layoutData);
		return text;
	}

	private static CheckedText createTextarea(Composite composite, String lab, int w,
			int h) {
		createLabel(composite, lab);
		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		layoutData.widthHint = w;
		layoutData.heightHint = h;
		CheckedText checkedText = new CheckedText(composite, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL
				| SWT.BORDER);
		checkedText.setLayoutData(layoutData);
		return checkedText;

	}

	private static Spinner createSpinner(Composite composite, String lab, int w,
			int incr, int min, int max, int digits) {
		createLabel(composite, lab);
		Spinner spinner = new Spinner(composite, SWT.BORDER);
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
			label.setLayoutData(new GridData(GridData.END, GridData.CENTER,
					true, false));
			label.setText(lab);
		}
	}

	private static Combo createCombo(Composite composite, String lab, int w,
			int style, String[] items) {
		createLabel(composite, lab);
		Combo combo = new Combo(composite, SWT.DROP_DOWN | style);
		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		layoutData.widthHint = w;
		combo.setLayoutData(layoutData);
		if (items != null) {
			combo.setItems(items);
			if ((style & SWT.READ_ONLY) != 0)
				combo.setText(items[0]);
		}
		return combo;
	}

	public AnalogProperties getResult() {
		return result;
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
		GregorianCalendar cal = new GregorianCalendar(dateField.getYear(),
				dateField.getMonth(), dateField.getDay(), timeField.getHours(),
				timeField.getMinutes(), timeField.getSeconds());
		properties.creationDate = cal.getTime();
		i = lightSourceField.getSelectionIndex();
		properties.lightSource = ((int[]) QueryField.EXIF_LIGHTSOURCE
				.getEnumeration())[i];
		String lv = lvField.getText().trim();
		if (!lv.isEmpty()) {
			try {
				properties.lv = Double.parseDouble(lv);
			} catch (NumberFormatException e) {
				// do nothing
			}
		}
		String exp = exposureTimeField.getText().trim();
		if (!exp.isEmpty()) {
			try {
				properties.exposureTime = (Double) QueryField.EXIF_EXPOSURETIME
						.getFormatter().fromString(exp);
			} catch (ParseException e) {
				properties.exposureTime = Double.NaN;
			}
		}
		String fn = fnumberTimeField.getText().trim();
		if (!fn.isEmpty()) {
			try {
				properties.fNumber = (Double) QueryField.EXIF_FNUMBER
						.getFormatter().fromString(fn);
			} catch (ParseException e) {
				properties.fNumber = Double.NaN;
			}
		}
		s = artistField.getText().trim();
		if (!s.isEmpty())
			properties.artist = s;
		s = copyrightField.getText().trim();
		if (!s.isEmpty())
			properties.copyright = s;
		return properties;
	}

	private void setFactor() {
		int i = formatField.getSelectionIndex();
		int fac = FACTORS[i];
		factorField.setSelection(fac);
		setF35();
	}

}
