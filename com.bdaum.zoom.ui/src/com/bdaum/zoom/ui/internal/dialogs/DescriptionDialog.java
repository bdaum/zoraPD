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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.osgi.util.NLS;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.bdaum.aoModeling.runtime.AomObject;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.composedTo.ComposedToImpl;
import com.bdaum.zoom.cat.model.derivedBy.DerivedByImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.ICore;
import com.bdaum.zoom.core.IRecipeDetector;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.css.internal.CssActivator;
import com.bdaum.zoom.image.IFocalLengthProvider;
import com.bdaum.zoom.image.recipe.Recipe;
import com.bdaum.zoom.operations.internal.RelationDescription;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.widgets.CheckedText;
import com.bdaum.zoom.ui.widgets.DateInput;

@SuppressWarnings("restriction")
public class DescriptionDialog extends ZTitleAreaDialog {

	private AomObject description;
	private RelationDescription result;
	private Combo kindCombo;
	private Text toolField;
	private CheckedText recipeField;
	private Text parmFileField;
	private Button restoreButton;
	private DateInput createField;
	private byte[] archivedRecipe;
	private final Asset asset;
	private Text adjustmentField;

	public DescriptionDialog(Shell parentShell, AomObject rel, Asset asset) {
		super(parentShell, HelpContextIds.DESCRIPTION_DIALOG);
		this.description = rel;
		this.asset = asset;
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.DescriptionDialog_Relationship_description);
		if (description instanceof ComposedToImpl)
			setMessage(Messages.DescriptionDialog_how_the_composite_was_composed);
		else
			setMessage(Messages.DescriptionDialog_how_the_derivative_was_derived);
		updateButtons(validateParmFile());
	}

	@SuppressWarnings("unused")
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite comp = new Composite(area, SWT.NONE);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		comp.setLayout(gridLayout);
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		final Label dateLabel = new Label(comp, SWT.NONE);
		dateLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));
		dateLabel.setText(Messages.DescriptionDialog_created_at);
		createField = new DateInput(comp, SWT.DATE | SWT.TIME | SWT.DROP_DOWN | SWT.BORDER);
		createField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));
		if (description instanceof ComposedToImpl) {
			final Label kindLabel = new Label(comp, SWT.NONE);
			kindLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
			kindLabel.setText(Messages.DescriptionDialog_kind);

			kindCombo = new Combo(comp, SWT.NONE);
			kindCombo.setItems(new String[] { Messages.DescriptionDialog_panorama, Messages.DescriptionDialog_hdr,
					Messages.DescriptionDialog_focus_stacking, Messages.DescriptionDialog_noise_reduction,
					Messages.DescriptionDialog_super_res, Messages.DescriptionDialog_montage });
			final GridData gd_kindCombo = new GridData(SWT.FILL, SWT.CENTER, true, false);
			kindCombo.setLayoutData(gd_kindCombo);
		}
		final Label toolLabel = new Label(comp, SWT.NONE);
		toolLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));
		toolLabel.setText(Messages.DescriptionDialog_tool);

		toolField = new Text(comp, SWT.BORDER);
		toolField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final Label parmLabel = new Label(comp, SWT.NONE);
		parmLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));
		parmLabel.setText(Messages.DescriptionDialog_parameter_file);
		Composite parmComposite = new Composite(comp, SWT.NONE);
		parmComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		GridLayout layout = new GridLayout((description instanceof DerivedByImpl) ? 4 : 3, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		parmComposite.setLayout(layout);
		parmFileField = new Text(parmComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		parmFileField.setLayoutData(new GridData(350, SWT.DEFAULT));
		parmFileField.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				updateButtons(validateParmFile());
			}
		});
		final Button browseButton = new Button(parmComposite, SWT.PUSH);
		browseButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		browseButton.setText(Messages.DescriptionDialog_browse);
		browseButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
				dialog.setText(Messages.DescriptionDialog_select_parameter_file);
				String id = description instanceof ComposedToImpl ? ((ComposedToImpl) description).getComposite()
						: ((DerivedByImpl) description).getDerivative();
				ICore core = Core.getCore();
				AssetImpl a = core.getDbManager().obtainAsset(id);
				if (a != null) {
					URI uri = core.getVolumeManager().findExistingFile(a, true);
					if (uri != null)
						dialog.setFilterPath((new File(uri)).getParent());
				}
				String file = dialog.open();
				if (file != null) {
					File f = new File(file);
					parmFileField.setText(f.toURI().toString());
					updateButtons(validateParmFile());
				}
			}
		});
		if (description instanceof DerivedByImpl) {
			restoreButton = new Button(parmComposite, SWT.PUSH);
			restoreButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			restoreButton.setText(Messages.DescriptionDialog_restore);
			restoreButton.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					restoreRecipe();
				}
			});
			if (getParmFile(((DerivedByImpl) description).getParameterFile()) != null) {
				new Label(comp, SWT.NONE);
				adjustmentField = new Text(comp,
						SWT.MULTI | SWT.LEAD | SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
				adjustmentField.setLayoutData(new GridData(450, 75));
			}
		}
		final Label recipeLabel = new Label(comp, SWT.NONE);
		recipeLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));
		recipeLabel.setText(Messages.DescriptionDialog_recipe);

		final GridData gd_recipeField = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd_recipeField.heightHint = 60;
		gd_recipeField.widthHint = 300;
		recipeField = new CheckedText(comp, SWT.MULTI | SWT.BORDER);
		recipeField.setLayoutData(gd_recipeField);
		fillValues();
		return area;
	}

	protected void restoreRecipe() {
		String filename = ((DerivedByImpl) description).getParameterFile();
		URI uri;
		try {
			uri = new URI(filename);
		} catch (URISyntaxException e2) {
			setErrorMessage(Messages.DescriptionDialog_illegal_uri_for_recipe);
			restoreButton.setEnabled(false);
			return;
		}
		File outputFile = new File(uri);
		ByteArrayInputStream in = new ByteArrayInputStream(((DerivedByImpl) description).getArchivedRecipe());
		ZipEntry entry;
		try (ZipInputStream zis = new ZipInputStream(in)) {
			while ((entry = zis.getNextEntry()) != null) {
				String name = entry.getName();
				if (outputFile.getName().equals(name)) {
					if (outputFile.exists()) {
						if (!AcousticMessageDialog.openConfirm(getShell(),
								Messages.DescriptionDialog_restore_raw_recipe,
								NLS.bind(Messages.DescriptionDialog_recipe_file_exists, outputFile.getName())))
							return;
						outputFile.delete();
					}
					try (FileOutputStream fos = new FileOutputStream(outputFile)) {
						int l;
						byte[] buffer = new byte[256];
						while ((l = zis.read(buffer, 0, buffer.length)) > 0)
							fos.write(buffer, 0, l);
						setMessage(Messages.DescriptionDialog_recipe_file_restored);
						setErrorMessage(null);
						restoreButton.setEnabled(false);
					}
					break;
				}
			}
		} catch (IOException e) {
			setErrorMessage(Messages.DescriptionDialog_io_error_when_restoring);
		}
	}

	private void fillValues() {
		if (description instanceof ComposedToImpl) {
			ComposedToImpl rel = (ComposedToImpl) description;
			if (rel.getType() != null)
				kindCombo.setText(rel.getType());
			if (rel.getTool() != null)
				toolField.setText(rel.getTool());
			recipeField.setText(rel.getRecipe());
			if (rel.getParameterFile() != null)
				parmFileField.setText(rel.getParameterFile());
			if (rel.getDate() != null)
				createField.setDate(rel.getDate());
		} else if (description instanceof DerivedByImpl) {
			DerivedByImpl rel = (DerivedByImpl) description;
			if (rel.getTool() != null)
				toolField.setText(rel.getTool());
			recipeField.setText(rel.getRecipe());
			if (rel.getParameterFile() != null) {
				parmFileField.setText(rel.getParameterFile());
				if (adjustmentField != null) {
					String recipeContent = getRecipeContent(rel.getParameterFile());
					if (recipeContent != null)
						adjustmentField.setText(recipeContent);
				}
			}
			if (rel.getDate() != null)
				createField.setDate(rel.getDate());
			archivedRecipe = rel.getArchivedRecipe();
		}
	}

	private String getRecipeContent(String parameterFile) {
		List<IRecipeDetector> recipeDetectors = CoreActivator.getDefault().getRecipeDetectors();
		for (IRecipeDetector detector : recipeDetectors) {
			Recipe recipe = detector.loadRecipe(parameterFile, false, new IFocalLengthProvider() {
				public double get35mm() {
					return asset.getFocalLengthIn35MmFilm();
				}
			}, null);
			if (recipe != null)
				return recipe.toString();
		}
		return null;
	}

	protected void updateButtons(boolean valid) {
		getShell().setModified(!readonly);
		if (restoreButton != null)
			restoreButton
					.setEnabled(parmFileField.getText().length() > 0 && valid && description instanceof DerivedByImpl
							&& ((DerivedByImpl) description).getArchivedRecipe() != null);
		Button okButton = getButton(IDialogConstants.OK_ID);
		if (okButton != null)
			okButton.setEnabled(!readonly);
	}

	protected boolean validateParmFile() {
		File file = getParmFile(parmFileField.getText());
		if (file == null) {
			parmFileField.setData("id", "errors"); //$NON-NLS-1$ //$NON-NLS-2$
			CssActivator.getDefault().setColors(parmFileField);
			return false;
		}
		parmFileField.setData("id", null); //$NON-NLS-1$
		CssActivator.getDefault().setColors(parmFileField);
		return parmFileField.getText().length() > 0;
	}

	private static File getParmFile(String path) {
		if (path.length() > 0) {
			File file;
			try {
				file = new File(new URI(path));
			} catch (URISyntaxException e) {
				file = new File(path);
			}
			return file.exists() ? file : null;
		}
		return null;
	}

	@Override
	protected void okPressed() {
		result = new RelationDescription((kindCombo != null) ? kindCombo.getText() : null, recipeField.getText(),
				toolField.getText(), parmFileField.getText(), createField.getDate(), archivedRecipe);
		super.okPressed();
	}

	public RelationDescription getResult() {
		return result;
	}
}
