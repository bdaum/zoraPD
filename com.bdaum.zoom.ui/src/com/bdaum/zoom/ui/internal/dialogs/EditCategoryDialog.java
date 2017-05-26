package com.bdaum.zoom.ui.internal.dialogs;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.cat.model.meta.Category;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;

public class EditCategoryDialog extends ZTitleAreaDialog implements ModifyListener, VerifyListener {

	private Category category;
	private Text nameField;
	private Text synField;
	private CheckboxButton applyButton;
	private String[] sysnonyms;
	private String label;
	private boolean apply;
	private Map<String, Category> categories;
	private Category parent;
	private Collection<Category> supCategories;
	private String isProposal;
	private boolean preserveName;

	public EditCategoryDialog(Shell shell, Category category, Map<String, Category> categories,
			Collection<Category> supCategories, Category parent, boolean isProposal) {
		super(shell);
		this.category = category;
		this.categories = categories;
		this.supCategories = supCategories;
		this.parent = parent;
		this.preserveName = isProposal;
	}

	@Override
	public void create() {
		super.create();
		getShell().setText(Constants.APPLICATION_NAME);
		setTitle(parent != null ? NLS.bind(Messages.EditCategoryDialog_add_subcat, parent.getLabel())
				: category != null ? Messages.EditCategoryDialog_edit : Messages.EditCategoryDialog_add);
		setMessage(parent == null && category != null ? Messages.EditCategoryDialog_change_name
				: Messages.EditCategoryDialog_define_name);
		fill();
		updateButtons();
	}

	@SuppressWarnings("unused")
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, false));
		new Label(composite, SWT.NONE).setText(Messages.EditCategoryDialog_name);
		nameField = new Text(composite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		nameField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		nameField.addModifyListener(this);
		nameField.addVerifyListener(this);
		if (category != null && !preserveName) {
			new Label(composite, SWT.NONE);
			applyButton = WidgetFactory.createCheckButton(composite, Messages.EditCategoryDialog_apply_changes, null);
		}
		Label synLabel = new Label(composite, SWT.NONE);
		synLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		synLabel.setText(Messages.EditCategoryDialog_synonyms);
		synField = new Text(composite, SWT.MULTI | SWT.LEAD | SWT.BORDER | SWT.V_SCROLL);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.heightHint = 250;
		synField.setLayoutData(layoutData);
		synField.addVerifyListener(this);
		return area;
	}

	@Override
	public void modifyText(ModifyEvent e) {
		if (isProposal != null) {
			String syn = synField.getText();
			if (syn.isEmpty())
				synField.setText(isProposal);
			else {
				int p = syn.indexOf(isProposal);
				if (p < 0 || (p + isProposal.length() < syn.length() && syn.charAt(p + isProposal.length()) != '\n'))
					synField.setText(isProposal + '\n' + syn);
			}
			isProposal = null;
		}
		updateButtons();
	}

	private void updateButtons() {
		String name = nameField.getText();
		String errorMessage = null;
		if (applyButton != null)
			applyButton.setEnabled(!name.isEmpty() && !name.equals(category.getLabel()));
		if (name.isEmpty())
			errorMessage = Messages.EditCategoryDialog_name_empty;
		else if (category == null || !name.equals(category.getLabel())) {
			errorMessage = searchDuplicate(categories, name);
			if (errorMessage == null && supCategories != null)
				for (Category category : supCategories) {
					if (name.equals(category.getLabel())) {
						errorMessage = Messages.EditCategoryDialog_name_exists;
						break;
					}
					errorMessage = checkSynonyms(name, category);
				}
		}
		setErrorMessage(errorMessage);
		getButton(OK).setEnabled(errorMessage == null);
	}

	private static String checkSynonyms(String name, Category cat) {
		String[] synonyms = cat.getSynonyms();
		if (synonyms != null)
			for (String syn : synonyms)
				if (name.equals(syn))
					return NLS.bind(Messages.EditCategoryDialog_name_in_synonyms, cat.getLabel());
		return null;
	}

	private String searchDuplicate(Map<String, Category> categories, String name) {
		if (categories == null)
			return null;
		for (Entry<String, Category> entry : categories.entrySet()) {
			if (entry.getKey().equals(name))
				return Messages.EditCategoryDialog_name_exists;
			Category cat = entry.getValue();
			if (cat != null) {
				String errorMessage = checkSynonyms(name, cat);
				if (errorMessage == null)
					errorMessage = searchDuplicate(cat.getSubCategory(), name);
				if (errorMessage != null)
					return errorMessage;
			}
		}
		return null;
	}

	private void fill() {
		if (category != null) {
			String lab = category.getLabel();
			nameField.setText(lab);
			nameField.setSelection(0, lab.length());
			if (category.getSynonyms() != null)
				synField.setText(Core.toStringList(category.getSynonyms(), "\n")); //$NON-NLS-1$
			if (preserveName) {
				isProposal = lab;
				preserveName = false;
			}
		}
	}

	@Override
	public void verifyText(VerifyEvent e) {
		for (int i = 0; i < e.text.length(); i++) {
			char c = e.text.charAt(i);
			if (c == '.' || c == ',' || c == ';' || c == '/') {
				e.doit = false;
				break;
			}
		}
	}

	@Override
	protected void okPressed() {
		label = nameField.getText();
		List<String> synlist = Core.fromStringList(synField.getText(), "\n"); //$NON-NLS-1$
		sysnonyms = synlist.toArray(new String[synlist.size()]);
		apply = applyButton != null && applyButton.getEnabled() & applyButton.getSelection();
		super.okPressed();
	}

	public String getLabel() {
		return label;
	}

	public String[] getSynonyms() {
		return sysnonyms;
	}

	public boolean getApply() {
		return apply;
	}

}
