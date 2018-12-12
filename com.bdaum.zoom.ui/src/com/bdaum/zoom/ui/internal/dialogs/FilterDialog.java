package com.bdaum.zoom.ui.internal.dialogs;

import java.util.Set;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.widgets.CGroup;

public class FilterDialog extends ZTitleAreaDialog {

	private CheckboxTableViewer typeViewer;
	private final String[] allcatitems;
	private final Set<String> hiddenCatItems;
	private final String[] catitemlabels;
	private int colorCode;

	public FilterDialog(Shell parentShell, int colorCode, String[] allcatitems, Set<String> hiddenCatItems,
			String[] catitemlabels) {
		super(parentShell, HelpContextIds.FILTERDIALOG);
		this.allcatitems = allcatitems;
		this.hiddenCatItems = hiddenCatItems;
		this.catitemlabels = catitemlabels;
		this.colorCode = colorCode;
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.FilterDialog_apply_filters);
		setMessage(Messages.FilterDialog_apply_filters_msg);
		getShell().pack();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(1, false));
		final CGroup colorGroup = new CGroup(composite, SWT.NONE);
		colorGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		colorGroup.setText(Messages.FilterDialog_filter_by_code);
		colorGroup.setLayout(new GridLayout());
		final ColorCodeGroup colorCodeGroup = new ColorCodeGroup(colorGroup, SWT.NONE, colorCode);
		final Label tooltip = new Label(colorGroup, SWT.NONE);
		tooltip.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		colorCodeGroup.addListener(new Listener() {
			@Override
			public void handleEvent(Event event) {
				setTooltip(tooltip, colorCode = colorCodeGroup.getCode());
			}
		});
		setTooltip(tooltip, colorCode);
		CGroup typeGroup = new CGroup(composite, SWT.NONE);
		typeGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		typeGroup.setText(Messages.FilterDialog_hide_by_type);
		typeGroup.setLayout(new GridLayout());
		typeViewer = CheckboxTableViewer.newCheckList(typeGroup, SWT.NONE);
		typeViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		typeViewer.setContentProvider(ArrayContentProvider.getInstance());
		typeViewer.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				for (int i = 0; i < allcatitems.length; i++)
					if (allcatitems[i].equals(element))
						return catitemlabels[i];
				return null;
			}
		});
		typeViewer.setInput(allcatitems);
		typeViewer.setCheckedElements(hiddenCatItems.toArray());
		return area;
	}

	@Override
	protected void okPressed() {
		hiddenCatItems.clear();
		for (Object item : typeViewer.getCheckedElements())
			hiddenCatItems.add((String) item);
		super.okPressed();
	}

	/**
	 * @return colorCode
	 */
	public int getColorCode() {
		return colorCode;
	}

	private void setTooltip(final Label label, int newColorCode) {
		label.setText(Icons.toColorIcon(newColorCode) == Icons.dashed ? Messages.FilterDialog_color_not_set
				: colorCode <= 0 ? null
						: NLS.bind(Messages.FilterDialog_show_only_with_color_code,
								QueryField.COLORCODELABELS[newColorCode + 1]));
	}
}
