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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import com.bdaum.zoom.cat.model.PageLayout_type;
import com.bdaum.zoom.cat.model.PageLayout_typeImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.dialogs.ZListDialog;
import com.bdaum.zoom.ui.internal.widgets.SharpeningGroup;
import com.bdaum.zoom.ui.internal.widgets.TextWithVariableGroup;
import com.bdaum.zoom.ui.widgets.NumericControl;

public class LayoutComponent implements IInputValidator {

	private class LoadLayoutDialog extends ZListDialog {

		private static final int DELETE = 9999;

		public LoadLayoutDialog(Shell parent, int style) {
			super(parent, style);
		}

		@Override
		protected Control createDialogArea(Composite container) {
			Control area = super.createDialogArea(container);
			getTableViewer().addSelectionChangedListener(
					new ISelectionChangedListener() {
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
				IStructuredSelection selection = (IStructuredSelection) tableViewer
						.getSelection();
				Core.getCore().getDbManager()
						.safeTransaction(selection.toList(), null);
				tableViewer.remove(selection.toArray());
				return;
			}
			super.buttonPressed(buttonId);
		}

	}

	public static final int PDF = 0;
	public static final int HTML = 1;
	public static final int PRINT = 2;

	protected static final String[] PAGEVARIABLES = new String[] {
			Constants.PT_CATALOG, Constants.PT_TODAY, Constants.PT_COUNT,
			Constants.PT_PAGENO, Constants.PT_COLLECTION, Constants.PT_USER,
			Constants.PT_OWNER };

	protected static final String[] CAPTIONVARIABLES = new String[] {
			Constants.PI_TITLE, Constants.PI_NAME, Constants.PI_FORMAT,
			Constants.PI_CREATIONDATE, Constants.PI_SIZE,
			Constants.PT_COLLECTION, Constants.PI_SEQUENCENO,
			Constants.PI_PAGEITEM };

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

	@SuppressWarnings("unused")
	public LayoutComponent(Composite parent, final int type) {
		this.type = type;
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(4, false));
		if (type == PDF) {
			final Label formatLabel = new Label(composite, SWT.NONE);
			formatLabel.setText(Messages.LayoutComponent_Format);
			formatField = new Combo(composite, SWT.READ_ONLY);
			formatField.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
					false, 3, 1));
			formatField.setItems(PageLayout_type.formatALLVALUES);
			final Label oriLabel = new Label(composite, SWT.NONE);
			oriLabel.setText(Messages.LayoutComponent_Orientation);
			oriField = new Combo(composite, SWT.READ_ONLY);
			oriField.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
					false, 3, 1));
			oriField.setItems(new String[] { Messages.LayoutComponent_Portrait,
					Messages.LayoutComponent_Landscape });
		}
		titleField = new TextWithVariableGroup(composite,
				Messages.PrintLayoutDialog_page_title,
				Messages.LayoutComponent_page__vars, PAGEVARIABLES, false);
		new Label(composite, SWT.NONE);

		subtitleField = new TextWithVariableGroup(composite,
				Messages.PrintLayoutDialog_aubtitle,
				Messages.LayoutComponent_page__vars, PAGEVARIABLES, false);
		new Label(composite, SWT.NONE);
		footerField = new TextWithVariableGroup(composite,
				Messages.PrintLayoutDialog_footer,
				Messages.LayoutComponent_page__vars, PAGEVARIABLES, false);
		new Label(composite, SWT.NONE);
		if (type == HTML) {
			new Label(composite, SWT.NONE)
					.setText(Messages.LayoutComponent_size);
			sizeField = new NumericControl(composite, SWT.NONE);
			sizeField.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
					false, 3, 1));
			sizeField.setMaximum(640);
			sizeField.setMinimum(64);
		}
		new Label(composite, SWT.NONE)
				.setText(Messages.PrintLayoutDialog_colums);
		columnsField = new NumericControl(composite, SWT.NONE);
		columnsField.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 3, 1));
		columnsField.setMaximum(20);
		columnsField.setMinimum(1);
		if (type != HTML) {
			new Label(composite, SWT.NONE)
					.setText(Messages.PrintLayoutDialog_left_margins);
			leftMarginsField = new NumericControl(composite, SWT.NONE);
			leftMarginsField.setMinimum(3);
			leftMarginsField.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
					false, false, 3, 1));
			leftMarginsField.setMaximum(50);

			new Label(composite, SWT.NONE)
					.setText(Messages.PrintLayoutDialog_right_margins);
			rightMarginsField = new NumericControl(composite, SWT.NONE);
			rightMarginsField.setMinimum(3);
			rightMarginsField.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
					false, false, 3, 1));
			rightMarginsField.setMaximum(50);
			final Label facingLabel = new Label(composite, SWT.NONE);
			facingLabel.setText(Messages.LayoutComponent_Facing_pages);
			facingField = new Button(composite, SWT.CHECK);
			facingField.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
					false, 3, 1));
			new Label(composite, SWT.NONE)
					.setText(Messages.PrintLayoutDialog_hor_gap);
			horizontalGapField = new NumericControl(composite, SWT.NONE);
			horizontalGapField.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
					false, false, 3, 1));
			horizontalGapField.setMinimum(1);
			horizontalGapField.setMaximum(30);
			final Label topMarginsLabel = new Label(composite, SWT.NONE);
			topMarginsLabel.setText(Messages.PrintLayoutDialog_top_margins);

			topMarginsField = new NumericControl(composite, SWT.NONE);
			topMarginsField.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
					false, false, 3, 1));
			topMarginsField.setMaximum(50);

			new Label(composite, SWT.NONE)
					.setText(Messages.PrintLayoutDialog_bottom_margins);
			bottomMarginsField = new NumericControl(composite, SWT.NONE);
			bottomMarginsField.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
					false, false, 3, 1));
			bottomMarginsField.setMaximum(50);

			new Label(composite, SWT.NONE)
					.setText(Messages.PrintLayoutDialog_vert_gap);
			verticalGapField = new NumericControl(composite, SWT.NONE);
			verticalGapField.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
					false, false, 3, 1));
			verticalGapField.setMaximum(30);
			verticalGapField.setMinimum(1);
		} else {
			new Label(composite, SWT.NONE)
					.setText(Messages.LayoutComponent_padding);
			horizontalGapField = new NumericControl(composite, SWT.NONE);
			horizontalGapField.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
					false, false, 3, 1));
			horizontalGapField.setMinimum(0);
			horizontalGapField.setMaximum(100);
		}
		caption1Field = new TextWithVariableGroup(composite,
				Messages.PrintLayoutDialog_capt_line_1,
				Messages.LayoutComponent_caption_vars, CAPTIONVARIABLES, true);
		caption2Field = new TextWithVariableGroup(composite,
				Messages.PrintLayoutDialog_capt_line_2,
				Messages.LayoutComponent_caption_vars, CAPTIONVARIABLES, true);
		if (type == HTML)
			altField = new TextWithVariableGroup(composite,
					Messages.LayoutComponent_alt,
					Messages.LayoutComponent_caption_vars, CAPTIONVARIABLES,
					true);
		else {
			new Label(composite, SWT.NONE)
					.setText(Messages.PrintLayoutDialog_keyline);
			keylineField = new NumericControl(composite, SWT.NONE);
			keylineField.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
					false, false, 3, 1));
			keylineField.setMaximum(10);
		}
		if (type == PRINT) {
			new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL)
					.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
							false, 4, 1));
			Label sharpeningLabel = new Label(composite, SWT.NONE);
			sharpeningLabel.setText(Messages.LayoutComponent_sharpening);
			sharpeningLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING,
					false, false));
			Composite group = new Composite(composite, SWT.NONE);
			group.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true,
					false, 3, 1));
			GridLayout gl = new GridLayout();
			gl.marginHeight = gl.marginWidth = 0;
			group.setLayout(gl);
			sharpeningGroup = new SharpeningGroup(group);
		}
		// new Label(composite, SWT.NONE);
		Label sep = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		Composite buttonGroup = new Composite(parent, SWT.NONE);
		buttonGroup.setLayoutData(new GridData(SWT.END, SWT.BEGINNING, true,
				false, 4, 1));
		buttonGroup.setLayout(new GridLayout(2, false));
		loadButton = new Button(buttonGroup, SWT.PUSH);
		loadButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true,
				false));
		loadButton.setText(Messages.LayoutComponent_load_layout);
		loadButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				List<PageLayout_typeImpl> set = Core
						.getCore()
						.getDbManager()
						.obtainObjects(PageLayout_typeImpl.class,
								"type", type, QueryField.EQUALS); //$NON-NLS-1$
				LoadLayoutDialog dialog = new LoadLayoutDialog(loadButton
						.getShell(), SWT.BORDER);
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
				dialog.setComparator(new ViewerComparator());
				if (dialog.open() == ListSelectionDialog.OK) {
					Object[] result = dialog.getResult();
					if (result != null && result.length > 0
							&& result[0] instanceof PageLayout_typeImpl)
						fillValues((PageLayout_typeImpl) result[0]);
				}
			}
		});
		final Button saveButton = new Button(buttonGroup, SWT.PUSH);
		saveButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true,
				false));
		saveButton.setText(Messages.LayoutComponent_save_layout);
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Date now = new Date();
				SimpleDateFormat df = new SimpleDateFormat(
						Messages.LayoutComponent_date_format);
				InputDialog dialog = new InputDialog(saveButton.getShell(),
						Messages.LayoutComponent_layout_name,
						Messages.LayoutComponent_layout_name_msg, df
								.format(now), LayoutComponent.this);
				if (dialog.open() == InputDialog.OK) {
					String value = dialog.getValue();
					IDbManager dbManager = Core.getCore().getDbManager();
					if (!dbManager.obtainObjects(PageLayout_typeImpl.class,
							"name", value, //$NON-NLS-1$
							QueryField.EQUALS).isEmpty()
							&& !AcousticMessageDialog.openQuestion(
									loadButton.getShell(),
									Messages.LayoutComponent_save_layout_title,
									Messages.LayoutComponent_layout_exists))
						return;
					PageLayout_typeImpl newLayout = new PageLayout_typeImpl();
					saveValues(newLayout);
					newLayout.setName(value);
					newLayout.setType(type);
					dbManager.storeAndCommit(newLayout);
					updateButtons();
				}
			}
		});
		new Label(buttonGroup, SWT.NONE);
		new Label(buttonGroup, SWT.NONE);
		updateButtons();
	}

	private void updateButtons() {
		loadButton.setEnabled(!Core
				.getCore()
				.getDbManager()
				.obtainObjects(PageLayout_typeImpl.class,
						"type", type, QueryField.EQUALS).isEmpty()); //$NON-NLS-1$
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
			layout.setLeftMargin(leftMarginsField.getSelection());
		if (rightMarginsField != null)
			layout.setRightMargin(rightMarginsField.getSelection());
		layout.setFacingPages(facingField == null ? false : facingField
				.getSelection());
		layout.setHorizontalGap(horizontalGapField.getSelection());
		if (topMarginsField != null)
			layout.setTopMargin(topMarginsField.getSelection());
		if (bottomMarginsField != null)
			layout.setBottomMargin(bottomMarginsField.getSelection());
		if (verticalGapField != null)
			layout.setVerticalGap(verticalGapField.getSelection());
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
			leftMarginsField.setSelection(aLayout.getLeftMargin());
		if (rightMarginsField != null)
			rightMarginsField.setSelection(aLayout.getRightMargin());
		if (facingField != null)
			facingField.setSelection(aLayout.getFacingPages());
		horizontalGapField.setSelection(aLayout.getHorizontalGap());
		if (topMarginsField != null)
			topMarginsField.setSelection(aLayout.getTopMargin());
		if (bottomMarginsField != null)
			bottomMarginsField.setSelection(aLayout.getBottomMargin());
		if (verticalGapField != null)
			verticalGapField.setSelection(aLayout.getVerticalGap());
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
			sharpeningGroup.fillValues(aLayout.getApplySharpening(),
					aLayout.getRadius(), aLayout.getAmount(),
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

}
