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

package com.bdaum.zoom.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.cat.model.Font_type;
import com.bdaum.zoom.cat.model.Font_typeImpl;
import com.bdaum.zoom.cat.model.Rgb_type;
import com.bdaum.zoom.cat.model.Rgb_typeImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.UiConstants;

public class WebFontDialog extends ZTitleAreaDialog {

	private static final String[] FAMILIES = new String[] {
			"Arial, Helvetica, sans-serif", "Arial Black, Gadget, sans-serif", //$NON-NLS-1$ //$NON-NLS-2$
			"Comic Sans MS, Comic Sans MS5, cursive", //$NON-NLS-1$
			"Courier New, Courier6, monospace", "Georgia1, Georgia, serif", //$NON-NLS-1$ //$NON-NLS-2$
			"Impact, Impact5, Charcoal6, sans-serif", //$NON-NLS-1$
			"Lucida Console, Monaco5, monospace", //$NON-NLS-1$
			"Lucida Sans Unicode, Lucida Grande, sans-serif", //$NON-NLS-1$
			"Palatino Linotype, Book Antiqua3, Palatino6, serif", //$NON-NLS-1$
			"Tahoma, Geneva, sans-serif", "Times New Roman, Times, serif", //$NON-NLS-1$ //$NON-NLS-2$
			"Trebuchet MS1, Helvetica, sans-serif", //$NON-NLS-1$
			"Verdana, Geneva, sans-serif", //$NON-NLS-1$
			"MS Sans Serif4, Geneva, sans-serif", "MS Serif4, New York6, serif" }; //$NON-NLS-1$ //$NON-NLS-2$
	public static final String[] STYLES = new String[] { Messages.WebFontDialog_normal, Messages.WebFontDialog_italic,
			Messages.WebFontDialog_oblique };
	public static final String[] WEIGHT = new String[] {  Messages.WebFontDialog_normal, Messages.WebFontDialog_bold,
			Messages.WebFontDialog_bolder, Messages.WebFontDialog_lighter };
	public static final String[] VARIANT = new String[] {  Messages.WebFontDialog_normal, Messages.WebFontDialog_smallCaps };
	private Font_type current;
	private final String text;
	private Text familyField;
	private Spinner heightField;
	private Combo styleField;
	private Button colorButton;
	private Combo variantField;
	private Combo weightField;
	protected Rgb_type currentColor;

	public WebFontDialog(Shell parentShell, String text, Font_type current) {
		super(parentShell);
		this.text = text;
		this.current = current;
	}

	@Override
	public void create() {
		super.create();
		getShell().setText(text);
		fillValues();
		updateButtons();
		setMessage(Messages.WebFontDialog_select_font_properties);
		getShell().pack();
	}

	private void fillValues() {
		if (current != null) {
			if (current.getFamily() != null)
				familyField.setText(Core.toStringList(current.getFamily(), "\n")); //$NON-NLS-1$
			heightField.setSelection(current.getSize());
			int style = current.getStyle();
			style = Math.max(0, Math.min(style, styleField.getItemCount() - 1));
			styleField.select(style);
			int weight = current.getWeight();
			weight = Math.max(0, Math.min(weight,
					weightField.getItemCount() - 1));
			weightField.select(weight);
			int variant = current.getVariant();
			variant = Math.max(0, Math.min(variant,
					variantField.getItemCount() - 1));
			variantField.select(variant);
			currentColor = current.getColor();
		} else {
			heightField.setSelection(100);
			styleField.select(0);
			weightField.select(0);
			variantField.select(0);
			currentColor = new Rgb_typeImpl(48, 48, 48);
		}
		if (currentColor != null)
			paintButton(colorButton, currentColor);
	}

	private void updateButtons() {
		boolean enabled = isValid();
		getShell().setModified(enabled);
		getButton(IDialogConstants.OK_ID).setEnabled(enabled);
	}

	@SuppressWarnings("static-method")
	private boolean isValid() {
		return true;
	}

	@SuppressWarnings("unused")
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite comp = new Composite(area, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout(2, false));
		Label familyLabel = new Label(comp, SWT.NONE);
		familyLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER,
				false, false));
		familyLabel.setText(Messages.WebFontDialog_font_families);
		familyField = new Text(comp, SWT.MULTI | SWT.LEAD | SWT.BORDER
				| SWT.V_SCROLL);
		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		layoutData.heightHint = 70;
		layoutData.widthHint = 200;
		familyField.setLayoutData(layoutData);
		new Label(comp, SWT.NONE);
		Composite addComp = new Composite(comp, SWT.NONE);
		addComp.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false,
				false));
		addComp.setLayout(new GridLayout(2, false));

		Button addButton = new Button(addComp, SWT.PUSH);
		addButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
				false));
		addButton.setText(Messages.WebFontDialog_set);
		final Combo addCombo = new Combo(addComp, SWT.DROP_DOWN);
		addCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		addCombo.setItems(FAMILIES);
		addCombo.setVisibleItemCount(8);
		addCombo.select(0);
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StringBuilder sb = new StringBuilder();
				StringTokenizer st = new StringTokenizer(addCombo.getText(),
						","); //$NON-NLS-1$
				while (st.hasMoreTokens()) {
					if (sb.length() > 0)
						sb.append('\n');
					sb.append(st.nextToken().trim());
				}
				familyField.setText(sb.toString());
			}
		});
		new Label(comp, SWT.NONE).setText(Messages.WebFontDialog_height);
		heightField = new Spinner(comp, SWT.BORDER);
		heightField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER,
				false, false));
		heightField.setMaximum(500);
		heightField.setMinimum(10);
		heightField.setIncrement(5);
		new Label(comp, SWT.NONE).setText(Messages.WebFontDialog_style);
		styleField = new Combo(comp, SWT.DROP_DOWN | SWT.READ_ONLY);
		styleField
				.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));
		styleField.setItems(STYLES);
		new Label(comp, SWT.NONE).setText(Messages.WebFontDialog_weight);
		weightField = new Combo(comp, SWT.DROP_DOWN | SWT.READ_ONLY);
		weightField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true,
				false));
		weightField.setItems(WEIGHT);
		new Label(comp, SWT.NONE).setText(Messages.WebFontDialog_variant);
		variantField = new Combo(comp, SWT.DROP_DOWN | SWT.READ_ONLY);
		variantField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true,
				false));
		variantField.setItems(VARIANT);
		colorButton = createColorButton(comp, Messages.WebFontDialog_color);
		return area;
	}

	private Button createColorButton(Composite comp, final String s) {
		if (s != null)
			new Label(comp, SWT.NONE).setText(s);
		final Button button = new Button(comp, SWT.PUSH | SWT.BORDER);
		final GridData gd_bgButton = new GridData(SWT.BEGINNING, SWT.CENTER,
				false, false);
		gd_bgButton.widthHint = 20;
		gd_bgButton.heightHint = 20;
		button.setLayoutData(gd_bgButton);
		final Image image = new Image(getShell().getDisplay(), 20, 20);
		button.setImage(image);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ColorDialog dialog = new ColorDialog(getShell());
				dialog.setText(s);
				if (currentColor != null)
					dialog.setRGB(new RGB(currentColor.getR(), currentColor
							.getG(), currentColor.getB()));
				RGB rgb = dialog.open();
				if (rgb != null)
					paintButton(button, currentColor = new Rgb_typeImpl(rgb.red, rgb.green,
							rgb.blue));
			}
		});
		button.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				image.dispose();
			}
		});
		return button;
	}

	protected void paintButton(Button button, Rgb_type rgb) {
		Image image = button.getImage();
		Rectangle bounds = image.getBounds();
		GC gc = new GC(image);
		Color c = new Color(getShell().getDisplay(), rgb.getR(), rgb.getG(),
				rgb.getB());
		gc.setBackground(c);
		gc.fillRectangle(bounds);
		c.dispose();
		gc.dispose();
		button.setImage(image);
		button.setData(UiConstants.RGB, rgb);
	}


	@Override
	protected void okPressed() {
		List<String> fonts = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(familyField.getText(), "\n"); //$NON-NLS-1$
		while (st.hasMoreTokens())
			fonts.add(st.nextToken().trim());
		if (fonts.isEmpty())
			current = null;
		else {
			if (current == null)
				current = new Font_typeImpl();
			current.setFamily(fonts.toArray(new String[fonts.size()]));
			current.setSize(heightField.getSelection());
			current.setStyle(styleField.getSelectionIndex());
			current.setWeight(weightField.getSelectionIndex());
			current.setVariant(variantField.getSelectionIndex());
			current.setColor(currentColor);
		}
		super.okPressed();
	}

	public Font_type getResult() {
		return current;
	}

}
