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

import java.util.List;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import com.bdaum.zoom.cat.model.PageLayout_type;
import com.bdaum.zoom.cat.model.PageLayout_typeImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.dialogs.ZListDialog;
import com.bdaum.zoom.ui.internal.ZViewerComparator;
import com.bdaum.zoom.ui.internal.widgets.SharpeningGroup;
import com.bdaum.zoom.ui.internal.widgets.TextWithVariableGroup;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.bdaum.zoom.ui.widgets.NumericControl;

public class LayoutComponent implements IInputValidator, Listener {

	private class LoadLayoutDialog extends ZListDialog {

		private static final int DELETE = 9999;

		public LoadLayoutDialog(Shell parent, int style) {
			super(parent, style);
		}

		@Override
		protected Control createDialogArea(Composite container) {
			Control area = super.createDialogArea(container);
			getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					updateDialogButtons();
				}
			});
			return area;
		}

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, DELETE, Messages.LayoutComponent_delete, false);
			super.createButtonsForButtonBar(parent);
			updateDialogButtons();
		}

		private void updateDialogButtons() {
			boolean enabled = !getTableViewer().getSelection().isEmpty();
			getButton(OK).setEnabled(enabled);
			getButton(DELETE).setEnabled(enabled);
		}

		@Override
		protected void buttonPressed(int buttonId) {
			if (buttonId == DELETE) {
				TableViewer tableViewer = getTableViewer();
				IStructuredSelection selection = tableViewer.getStructuredSelection();
				Core.getCore().getDbManager().safeTransaction(selection.toList(), null);
				tableViewer.remove(selection.toArray());
				return;
			}
			super.buttonPressed(buttonId);
		}

	}

	public static final int PDF = 0;
	public static final int HTML = 1;
	public static final int PRINT = 2;

	protected static final String[] PAGEVARIABLES = new String[] { Constants.PT_CATALOG, Constants.PT_TODAY,
			Constants.PT_COUNT, Constants.PT_PAGENO, Constants.PT_COLLECTION, Constants.PT_USER, Constants.PT_OWNER };

	// protected static final String[] CAPTIONVARIABLES = new String[] {
	// Constants.PI_TITLE, Constants.PI_NAME,
	// Constants.PI_FORMAT, Constants.PI_CREATIONDATE, Constants.PI_CREATIONYEAR,
	// Constants.PI_SIZE,
	// Constants.PT_COLLECTION, Constants.PI_SEQUENCENO, Constants.PI_PAGEITEM };

	private static final String[] CAPTIONTEMPLATES = new String[] { "- {sequenceNo} -", //$NON-NLS-1$
			"- {pageItem} -", //$NON-NLS-1$
			"{title}", //$NON-NLS-1$
			"{title} - {creationDate}", //$NON-NLS-1$
			"{title} - {creationYear}", //$NON-NLS-1$
			"{title} �{creationYear} {meta=artist}", //$NON-NLS-1$
			Messages.LayoutComponent_template4, Messages.LayoutComponent_template5,
			Messages.LayoutComponent_template6 };

	private TextWithVariableGroup titleField;
	private NumericControl columnsField;
	private NumericControl leftMarginsField;
	private NumericControl rightMarginsField;
	private TextWithVariableGroup caption1Field;
	private TextWithVariableGroup caption2Field;
	private PageLayout_typeImpl layout;
	private NumericControl bottomMarginsField;
	private NumericControl topMarginsField;
	private NumericControl horizontalGapField;
	private NumericControl verticalGapField;
	private TextWithVariableGroup footerField;
	private TextWithVariableGroup subtitleField;
	private NumericControl keylineField;
	private Composite composite;
	private Combo oriField;
	private Combo formatField;
	private Button facingField;
	private SharpeningGroup sharpeningGroup;
	private NumericControl sizeField;
	private TextWithVariableGroup altField;
	private final int type;
	private Button loadButton;
	private char unit = Core.getCore().getDbFactory().getDimUnit();

	@SuppressWarnings("unused")
	public LayoutComponent(Composite parent, final int type, List<Asset> assets, String collection) {
		this.type = type;
		Asset asset = assets.isEmpty() ? null : assets.get(0);
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		if (type == PDF) {
			CGroup pageGroup = createGroup(composite, Messages.LayoutComponent_page_format, 4);
			new Label(pageGroup, SWT.NONE).setText(Messages.LayoutComponent_Format);
			formatField = new Combo(pageGroup, SWT.READ_ONLY);
			formatField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			formatField.setItems(PageLayout_type.formatALLVALUES);
			Label label = new Label(pageGroup, SWT.NONE);
			GridData layoutData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
			layoutData.horizontalIndent = 10;
			label.setLayoutData(layoutData);
			label.setText(Messages.LayoutComponent_Orientation);
			oriField = new Combo(pageGroup, SWT.READ_ONLY);
			oriField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			oriField.setItems(new String[] { Messages.LayoutComponent_Portrait, Messages.LayoutComponent_Landscape });
		}
		CGroup titleGroup = createGroup(composite, Messages.LayoutComponent_titles, 3);
		titleField = new TextWithVariableGroup(titleGroup, Messages.PrintLayoutDialog_page_title, 300, PAGEVARIABLES,
				false, null, asset, collection);
		subtitleField = new TextWithVariableGroup(titleGroup, Messages.PrintLayoutDialog_aubtitle, 300, PAGEVARIABLES,
				false, null, asset, collection);
		footerField = new TextWithVariableGroup(titleGroup, Messages.PrintLayoutDialog_footer, 300, PAGEVARIABLES,
				false, null, asset, collection);
		CGroup layoutGroup = createGroup(composite, Messages.LayoutComponent_layout, 4);
		if (type == HTML) {
			new Label(layoutGroup, SWT.NONE).setText(Messages.LayoutComponent_size);
			sizeField = new NumericControl(layoutGroup, SWT.NONE);
			sizeField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 3, 1));
			sizeField.setMaximum(640);
			sizeField.setMinimum(64);
		}
		new Label(layoutGroup, SWT.NONE).setText(Messages.PrintLayoutDialog_colums);
		columnsField = new NumericControl(layoutGroup, SWT.NONE);
		columnsField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, type != HTML ? 1 : 3, 1));
		columnsField.setMaximum(20);
		columnsField.setMinimum(1);
		if (type != HTML) {
			Label label = new Label(layoutGroup, SWT.NONE);
			label.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
			label.setText(Messages.LayoutComponent_Facing_pages);
			facingField = new Button(layoutGroup, SWT.CHECK);
			facingField.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
			new Label(layoutGroup, SWT.NONE).setText(Messages.PrintLayoutDialog_left_margins + captionUnitcmin());
			leftMarginsField = new NumericControl(layoutGroup, SWT.NONE);
			leftMarginsField.setMinimum(3);
			leftMarginsField.setDigits(1);
			leftMarginsField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			leftMarginsField.setMaximum(unit == 'i' ? 20 : 50);

			label = new Label(layoutGroup, SWT.NONE);
			label.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
			label.setText(Messages.PrintLayoutDialog_right_margins + captionUnitcmin());
			rightMarginsField = new NumericControl(layoutGroup, SWT.NONE);
			rightMarginsField.setMinimum(3);
			rightMarginsField.setDigits(1);
			rightMarginsField.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
			rightMarginsField.setMaximum(unit == 'i' ? 20 : 50);
			new Label(layoutGroup, SWT.NONE).setText(Messages.PrintLayoutDialog_hor_gap + captionUnitcmin());
			horizontalGapField = new NumericControl(layoutGroup, SWT.NONE);
			horizontalGapField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			horizontalGapField.setMinimum(1);
			horizontalGapField.setDigits(1);
			horizontalGapField.setMaximum(unit == 'i' ? 12 : 30);
			label = new Label(layoutGroup, SWT.NONE);
			label.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
			label.setText(Messages.PrintLayoutDialog_vert_gap + captionUnitcmin());
			verticalGapField = new NumericControl(layoutGroup, SWT.NONE);
			verticalGapField.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
			verticalGapField.setMaximum(unit == 'i' ? 12 : 30);
			verticalGapField.setMinimum(1);
			verticalGapField.setDigits(1);
			new Label(layoutGroup, SWT.NONE).setText(Messages.PrintLayoutDialog_top_margins + captionUnitcmin());
			topMarginsField = new NumericControl(layoutGroup, SWT.NONE);
			topMarginsField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			topMarginsField.setMaximum(50);
			topMarginsField.setDigits(1);
			label = new Label(layoutGroup, SWT.NONE);
			label.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
			label.setText(Messages.PrintLayoutDialog_bottom_margins + captionUnitcmin());
			bottomMarginsField = new NumericControl(layoutGroup, SWT.NONE);
			bottomMarginsField.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
			bottomMarginsField.setMaximum(50);
			bottomMarginsField.setDigits(1);
		} else {
			new Label(layoutGroup, SWT.NONE).setText(Messages.LayoutComponent_padding);
			horizontalGapField = new NumericControl(layoutGroup, SWT.NONE);
			horizontalGapField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 3, 1));
			horizontalGapField.setMinimum(0);
			horizontalGapField.setMaximum(100);
		}
		CGroup captionGroup = createGroup(composite, Messages.LayoutComponent_captions, 5);
		caption1Field = new TextWithVariableGroup(captionGroup, Messages.PrintLayoutDialog_capt_line_1, 300,
				Constants.PI_ALL, true, CAPTIONTEMPLATES, asset, collection);
		caption2Field = new TextWithVariableGroup(captionGroup, Messages.PrintLayoutDialog_capt_line_2, 300,
				Constants.PI_ALL, true, CAPTIONTEMPLATES, asset, collection);
		if (type == HTML)
			altField = new TextWithVariableGroup(captionGroup, Messages.LayoutComponent_alt, 300, Constants.PI_ALL,
					true, CAPTIONTEMPLATES, asset, collection);
		else {
			new Label(captionGroup, SWT.NONE).setText(Messages.PrintLayoutDialog_keyline);
			keylineField = new NumericControl(captionGroup, SWT.NONE);
			keylineField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 4, 1));
			keylineField.setMaximum(10);
		}
		if (type == PRINT) {
			CGroup sharpGroup = createGroup(composite, Messages.LayoutComponent_sharpening, 1);
			Composite group = new Composite(sharpGroup, SWT.NONE);
			group.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
			GridLayout gl = new GridLayout();
			gl.marginHeight = gl.marginWidth = 0;
			group.setLayout(gl);
			sharpeningGroup = new SharpeningGroup(group);
		}
		new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL)
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		Composite buttonGroup = new Composite(parent, SWT.NONE);
		buttonGroup.setLayoutData(new GridData(SWT.END, SWT.BEGINNING, true, false));
		buttonGroup.setLayout(new GridLayout(2, false));
		loadButton = new Button(buttonGroup, SWT.PUSH);
		loadButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));
		loadButton.setText(Messages.LayoutComponent_load_layout);
		loadButton.addListener(SWT.Selection, this);
		final Button saveButton = new Button(buttonGroup, SWT.PUSH);
		saveButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));
		saveButton.setText(Messages.LayoutComponent_save_layout);
		saveButton.addListener(SWT.Selection, this);
		new Label(buttonGroup, SWT.NONE);
		updateButtons();
	}

	protected CGroup createGroup(Composite parent, String title, int columns) {
		CGroup pageGroup = new CGroup(parent, SWT.NONE);
		pageGroup.setText(title);
		pageGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		pageGroup.setLayout(new GridLayout(columns, false));
		return pageGroup;
	}

	private void updateButtons() {
		loadButton.setEnabled(!Core.getCore().getDbManager()
				.obtainObjects(PageLayout_typeImpl.class, "type", type, QueryField.EQUALS).isEmpty()); //$NON-NLS-1$
	}

	public PageLayout_typeImpl getResult() {
		saveValues(layout);
		return layout;
	}

	private void saveValues(PageLayout_typeImpl layout) {
		layout.setTitle(titleField.getText().trim());
		layout.setSubtitle(subtitleField.getText().trim());
		layout.setFooter(footerField.getText().trim());
		if (sizeField != null)
			layout.setSize(sizeField.getSelection());
		layout.setColumns(columnsField.getSelection());
		if (leftMarginsField != null)
			layout.setLeftMargin(toMm(leftMarginsField.getSelection()));
		if (rightMarginsField != null)
			layout.setRightMargin(toMm(rightMarginsField.getSelection()));
		layout.setFacingPages(facingField == null ? false : facingField.getSelection());
		layout.setHorizontalGap(
				type == HTML ? horizontalGapField.getSelection() : toMm(horizontalGapField.getSelection()));
		if (topMarginsField != null)
			layout.setTopMargin(toMm(topMarginsField.getSelection()));
		if (bottomMarginsField != null)
			layout.setBottomMargin(toMm(bottomMarginsField.getSelection()));
		if (verticalGapField != null)
			layout.setVerticalGap(toMm(verticalGapField.getSelection()));
		layout.setCaption1(caption1Field.getText().trim());
		layout.setCaption2(caption2Field.getText().trim());
		if (altField != null)
			layout.setAlt(altField.getText());
		if (keylineField != null)
			layout.setKeyLine(keylineField.getSelection());
		if (oriField != null && formatField != null) {
			layout.setLandscape(oriField.getSelectionIndex() == 1);
			layout.setFormat(formatField.getText());
		}
		if (sharpeningGroup != null) {
			layout.setApplySharpening(sharpeningGroup.getApplySharpening());
			layout.setRadius(sharpeningGroup.getRadius());
			layout.setAmount(sharpeningGroup.getAmount());
			layout.setThreshold(sharpeningGroup.getThreshold());
			layout.setJpegQuality(-1);
		}
	}

	public void fillValues(PageLayout_typeImpl aLayout) {
		this.layout = aLayout;
		titleField.setText(aLayout.getTitle());
		subtitleField.setText(aLayout.getSubtitle());
		footerField.setText(aLayout.getFooter());
		if (sizeField != null)
			sizeField.setSelection(aLayout.getSize());
		columnsField.setSelection(aLayout.getColumns());
		if (leftMarginsField != null)
			leftMarginsField.setSelection(fromMm(aLayout.getLeftMargin()));
		if (rightMarginsField != null)
			rightMarginsField.setSelection(fromMm(aLayout.getRightMargin()));
		if (facingField != null)
			facingField.setSelection(aLayout.getFacingPages());
		horizontalGapField.setSelection(type == HTML ? aLayout.getHorizontalGap() : fromMm(aLayout.getHorizontalGap()));
		if (topMarginsField != null)
			topMarginsField.setSelection(fromMm(aLayout.getTopMargin()));
		if (bottomMarginsField != null)
			bottomMarginsField.setSelection(fromMm(aLayout.getBottomMargin()));
		if (verticalGapField != null)
			verticalGapField.setSelection(fromMm(aLayout.getVerticalGap()));
		caption1Field.setText(aLayout.getCaption1());
		caption2Field.setText(aLayout.getCaption2());
		if (altField != null)
			altField.setText(aLayout.getAlt());
		if (keylineField != null)
			keylineField.setSelection(aLayout.getKeyLine());
		if (oriField != null && formatField != null) {
			oriField.select(aLayout.getLandscape() ? 1 : 0);
			String format = aLayout.getFormat();
			if (format == null)
				format = PageLayout_type.format_a4;
			formatField.setText(format);
		}
		if (sharpeningGroup != null)
			sharpeningGroup.fillValues(aLayout.getApplySharpening(), aLayout.getRadius(), aLayout.getAmount(),
					aLayout.getThreshold());
	}

	public Control getControl() {
		return composite;
	}

	public String isValid(String newText) {
		if (newText.isEmpty())
			return Messages.LayoutComponent_empty_name;
		return null;
	}

	private String captionUnitcmin() {
		return unit == 'i' ? " (in)" : " (cm)"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	private int fromMm(int x) {
		return unit == 'i' ? (int) (x / 2.54d + 0.5d) : x;
	}

	private int toMm(int x) {
		return unit == 'i' ? (int) (x * 2.54d + 0.5d) : x;
	}

	@Override
	public void handleEvent(Event event) {
		if (event.widget == loadButton) {
			List<PageLayout_typeImpl> set = Core.getCore().getDbManager().obtainObjects(PageLayout_typeImpl.class,
					"type", type, QueryField.EQUALS); //$NON-NLS-1$
			LoadLayoutDialog dialog = new LoadLayoutDialog(loadButton.getShell(), SWT.BORDER);
			dialog.setTitle(Messages.LayoutComponent_load_layout_title);
			dialog.setMessage(Messages.LayoutComponent_select_layout);
			dialog.setContentProvider(ArrayContentProvider.getInstance());
			dialog.setLabelProvider(new ZColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					if (element instanceof PageLayout_typeImpl)
						return ((PageLayout_typeImpl) element).getName();
					return null;
				}
			});
			dialog.setInput(set);
			dialog.create();
			dialog.setComparator(ZViewerComparator.INSTANCE);
			if (dialog.open() == ListSelectionDialog.OK) {
				Object[] result = dialog.getResult();
				if (result != null && result.length > 0 && result[0] instanceof PageLayout_typeImpl)
					fillValues((PageLayout_typeImpl) result[0]);
			}
		} else {
			InputDialog dialog = new InputDialog(composite.getShell(), Messages.LayoutComponent_layout_name,
					Messages.LayoutComponent_layout_name_msg,
					Format.MDY_FORMAT.get().format(System.currentTimeMillis()), LayoutComponent.this);
			if (dialog.open() == InputDialog.OK) {
				String value = dialog.getValue();
				IDbManager dbManager = Core.getCore().getDbManager();
				if (!dbManager.obtainObjects(PageLayout_typeImpl.class, "name", value, //$NON-NLS-1$
						QueryField.EQUALS).isEmpty()
						&& !AcousticMessageDialog.openQuestion(loadButton.getShell(),
								Messages.LayoutComponent_save_layout_title, Messages.LayoutComponent_layout_exists))
					return;
				PageLayout_typeImpl newLayout = new PageLayout_typeImpl();
				saveValues(newLayout);
				newLayout.setName(value);
				newLayout.setType(type);
				dbManager.storeAndCommit(newLayout);
				updateButtons();
			}
		}

	}

}
