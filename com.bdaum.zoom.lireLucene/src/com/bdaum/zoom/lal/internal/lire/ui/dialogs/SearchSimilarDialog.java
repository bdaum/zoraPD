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

package com.bdaum.zoom.lal.internal.lire.ui.dialogs;

import java.awt.image.ColorConvertOp;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.cat.model.SimilarityOptions_typeImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.Criterion;
import com.bdaum.zoom.cat.model.group.CriterionImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.ICore;
import com.bdaum.zoom.core.ISpellCheckingService;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.ICollectionProcessor;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.lire.Algorithm;
import com.bdaum.zoom.core.internal.peer.IPeerService;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.image.ImageUtilities;
import com.bdaum.zoom.image.ZImage;
import com.bdaum.zoom.image.internal.ImageActivator;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.dialogs.FindInNetworkGroup;
import com.bdaum.zoom.ui.internal.dialogs.FindWithinGroup;
import com.bdaum.zoom.ui.internal.dialogs.KeywordVerifyListener;
import com.bdaum.zoom.ui.internal.widgets.CheckedText;
import com.bdaum.zoom.ui.internal.widgets.SearchResultGroup;
import com.bdaum.zoom.ui.paint.PaintExample;
import com.bdaum.zoom.ui.paint.ToolSettings;
import com.bdaum.zoom.ui.widgets.CGroup;

@SuppressWarnings("restriction")
public class SearchSimilarDialog extends ZTitleAreaDialog {

	private static final String SETTINGSID = "com.bdaum.zoom.similaritySearchDialog"; //$NON-NLS-1$
	private SmartCollectionImpl collection;
	private PaintExample paintExample;
	private Image image;
	private SimilarityOptions_typeImpl options;
	private final Asset asset;
	private int profile;
	private FindWithinGroup findWithinGroup;
	private boolean disposeImage = false;
	private boolean adhoc = true;
	private byte[] pngImage;
	private FindInNetworkGroup findInNetworkGroup;
	private final SmartCollection currentCollection;
	private SearchResultGroup searchResultGroup;
	private IDialogSettings settings;
	private CheckedText keywordField;
	private Scale scale;

	public SearchSimilarDialog(Shell parentShell, Asset asset, SmartCollection currentCollection) {
		super(parentShell, HelpContextIds.SEARCHSIMILAR_DIALOG);
		this.asset = asset;
		this.currentCollection = currentCollection;
		settings = getDialogSettings(UiActivator.getDefault(), SETTINGSID);
		ICore core = Core.getCore();
		if (asset != null)
			image = core.getImageCache().getImage(asset);
		else if (currentCollection != null) {
			adhoc = currentCollection.getAdhoc();
			Criterion criterion = currentCollection.getCriterion(0);
			if (criterion != null && criterion.getValue() instanceof SimilarityOptions_typeImpl)
				options = (SimilarityOptions_typeImpl) criterion.getValue();

		} else {
			ImageData lastReferenceImage = UiActivator.getDefault().getLastReferenceImage();
			if (lastReferenceImage != null) {
				image = new Image(parentShell.getDisplay(), lastReferenceImage);
				disposeImage = true;
			}
		}
		if (options == null) {
			options = dbManager.obtainById(SimilarityOptions_typeImpl.class, Constants.SIMILARITYOPTIONS_ID);
			if (options == null) {
				options = new SimilarityOptions_typeImpl(CoreActivator.getDefault().getDefaultCbirAlgorithm().getId(),
						50, 0.12f, 0, 10, 30, 50, null, 0);
				options.setStringId(Constants.SIMILARITYOPTIONS_ID);
				options.setPngImage(null);
			}
		}
		if (image == null) {
			image = ImageUtilities.loadThumbnail(parentShell.getDisplay(), options.getPngImage(),
					Ui.getUi().getDisplayCMS(), SWT.IMAGE_PNG, false);
			if (image != null)
				disposeImage = true;
		}
		if (image == null && options.getAssetId() != null) {
			AssetImpl a = dbManager.obtainAsset(options.getAssetId());
			if (a != null)
				image = core.getImageCache().getImage(a);
		}
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.SearchSimilarDialog_similarity_search);
		setMessage(Messages.SearchSimilarDialog_similarity_search_message);
		fillValues();
		searchResultGroup.updateControls();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		final Composite comp = new Composite(area, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout(2, false));
		createPaintGroup(comp);
		createKeywordGroup(comp);
		createAlgoGroup(comp);
		createOptionsGroup(comp);
		return area;
	}

	private void createKeywordGroup(Composite composite) {
		CGroup group = new CGroup(composite, SWT.NONE);
		group.setText(Messages.SearchSimilarDialog_additional_keywords);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		group.setLayout(new GridLayout());
		keywordField = new CheckedText(group, SWT.MULTI | SWT.LEAD | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		keywordField.setSpellingOptions(8, ISpellCheckingService.KEYWORDOPTIONS);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.widthHint = 120;
		keywordField.setLayoutData(layoutData);
		keywordField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateScale();
			}
		});
		KeywordVerifyListener keywordVerifyListener = new KeywordVerifyListener();
		Set<String> keywords = dbManager.getMeta(true).getKeywords();
		keywordVerifyListener.setKeywords(keywords.toArray(new String[keywords.size()]));
		keywordField.addVerifyListener(keywordVerifyListener);
		Composite sliderGroup = new Composite(group, SWT.NONE);
		sliderGroup.setLayoutData(new GridData(SWT.FILL, SWT.END, true, false));
		sliderGroup.setLayout(new GridLayout(3, false));
		new Label(sliderGroup, SWT.NONE).setText(Messages.SearchSimilarDialog_visual);
		scale = new Scale(sliderGroup, SWT.HORIZONTAL);
		scale.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		scale.setMaximum(100);
		scale.setIncrement(5);
		scale.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				scale.setToolTipText(String.valueOf(scale.getSelection()));
				updateKeywordField();
			}
		});
		new Label(sliderGroup, SWT.NONE).setText(Messages.SearchSimilarDialog_keywords);
	}

	private void createAlgoGroup(Composite composite) {
		searchResultGroup = new SearchResultGroup(composite, SWT.NONE, true, true, false, getButton(OK),
				new GridData(SWT.FILL, SWT.FILL, true, true));
		if (Core.getCore().isNetworked())
			searchResultGroup.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					validateAlgo();
				}
			});
	}

	private void fillValues() {
		if (findWithinGroup != null) {
			if (currentCollection != null)
				findWithinGroup.setSelection(currentCollection.getSmartCollection_subSelection_parent() != null);
			else
				findWithinGroup.fillValues(settings);
		}
		ToolSettings toolSettings = paintExample.getToolSettings();
		toolSettings.pencilRadius = options.getPencilRadius();
		toolSettings.airbrushRadius = options.getAirbrushRadius();
		toolSettings.airbrushIntensity = options.getAirbrushIntensity();
		scale.setSelection(options.getKeywordWeight());
		fillKeywords(asset == null ? null : asset.getKeyword());
		searchResultGroup.fillValues((int) (100 * options.getMinScore() + 0.5f), options.getMaxResults(),
				options.getMethod(), 0);
		if (findInNetworkGroup != null && currentCollection != null) {
			findInNetworkGroup.setSelection(currentCollection.getNetwork());
			validateAlgo();
		}
		paintExample.setPaintTool(options.getLastTool());
		if (disposeImage)
			paintExample.setDirty(true);
	}

	private void fillKeywords(String[] keywords) {
		if (keywords != null)
			keywordField.setText(Core.toStringList(QueryField.getKeywordFilter().filter(keywords), '\n'));
		updateScale();
		updateKeywordField();
	}

	private void updateScale() {
		scale.setEnabled(!keywordField.getText().isEmpty());
	}

	private void createOptionsGroup(Composite comp) {
		final Composite optionsGroup = new Composite(comp, SWT.NONE);
		final GridData data = new GridData(SWT.LEFT, SWT.TOP, false, false);
		data.verticalIndent = 10;
		optionsGroup.setLayoutData(data);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		optionsGroup.setLayout(gridLayout);
		if (adhoc)
			findWithinGroup = new FindWithinGroup(optionsGroup);
		if (Core.getCore().isNetworked()) {
			findInNetworkGroup = new FindInNetworkGroup(optionsGroup);
			findInNetworkGroup.addListener(new Listener() {
				@Override
				public void handleEvent(Event event) {
					validateAlgo();
				}
			});
			if (currentCollection != null)
				findInNetworkGroup.setSelection(currentCollection.getNetwork());
		}
	}

	protected void validateAlgo() {
		if (findInNetworkGroup != null) {
			boolean networked = findInNetworkGroup.getSelection();
			Algorithm algo = searchResultGroup.getSelectedAlgorithm();
			if (networked && algo != null) {
				IPeerService peerService = Core.getCore().getPeerService();
				String[] unsupported = peerService.findWeakSimilarityPeers(algo.getName());
				if (unsupported != null)
					setErrorMessage(NLS.bind(Messages.SearchSimilarDialog_no_peer_support_for_algo,
							Core.toStringList(unsupported, ", "))); //$NON-NLS-1$
				return;
			}
			setErrorMessage(null);
		}
	}

	private void createPaintGroup(final Composite comp) {
		int w = 320;
		int h = 320;
		if (image != null) {
			Rectangle bounds = image.getBounds();
			if (bounds.width > bounds.height)
				h = bounds.height * 320 / bounds.width;
			else
				w = bounds.width * 320 / bounds.height;
		}
		final CGroup paintGroup = CGroup.create(comp, 1, Messages.SearchSimilarDialog_reference_image);
		paintExample = new PaintExample(paintGroup);
		paintExample.createGUI(paintGroup, w, h);
		paintExample.setDefaults();
		if (image != null)
			paintExample.setBackgroundImage(image, w, h);
	}

	@Override
	protected void okPressed() {
		Algorithm selectedAlgorithm = searchResultGroup.getSelectedAlgorithm();
		int method = selectedAlgorithm == null ? CoreActivator.getDefault().getDefaultCbirAlgorithm().getId()
				: selectedAlgorithm.getId();
		Image hardcopy = paintExample.getHardcopy();
		ColorConvertOp op = (profile == ImageConstants.ARGB) ? ImageActivator.getDefault().getCOLORCONVERTOP_ARGB2SRGB()
				: null;
		try {
			ImageData imageData = hardcopy.getImageData();
			ZImage zimage = new ZImage(imageData, null);
			zimage.setOutputColorConvert(op);
			ByteArrayOutputStream out = new ByteArrayOutputStream(50000);
			zimage.saveToStream(null, true, ZImage.UNCROPPED, SWT.DEFAULT, SWT.DEFAULT, out, SWT.IMAGE_PNG);
			zimage.dispose();
			pngImage = out.toByteArray();
			if (currentCollection == null && paintExample.isDirty())
				UiActivator.getDefault().setLastRefererenceImage(imageData);
		} catch (IOException e) {
			// ignore
		}
		String assetId = (asset == null || paintExample.isDirty()) ? null : asset.getStringId();
		String title = (assetId == null)
				? NLS.bind(Messages.SearchSimilarDialog_similarit_search_coll_title,
						Messages.SearchSimilarDialog_drawing)
				: NLS.bind(Messages.SearchSimilarDialog_similarit_search_coll_title,
						UiUtilities.createSlideTitle(asset));
		ToolSettings toolSettings = paintExample.getToolSettings();
		SimilarityOptions_typeImpl newOptions = new SimilarityOptions_typeImpl(method, searchResultGroup.getMaxNumber(),
				searchResultGroup.getScore() / 100f, paintExample.getCurrentTool(), toolSettings.pencilRadius,
				toolSettings.airbrushRadius, toolSettings.airbrushIntensity, assetId, 0);
		newOptions.setPngImage(pngImage);
		String text = keywordField.getText();
		if (!text.isEmpty()) {
			List<String> keywords = Core.fromStringList(text, "\n\r"); //$NON-NLS-1$
			newOptions.setKeywords(keywords.toArray(new String[keywords.size()]));
			newOptions.setKeywordWeight(scale.getSelection());
		}
		boolean network = findInNetworkGroup == null ? false : findInNetworkGroup.getSelection();
		collection = new SmartCollectionImpl(title, false, false, adhoc, network, null, 0, null, 0, null,
				Constants.INHERIT_LABEL, null, 0, null);
		collection.addCriterion(new CriterionImpl(ICollectionProcessor.SIMILARITY, null, newOptions,
				searchResultGroup.getScore(), false));
		if (findWithinGroup != null) {
			collection.setSmartCollection_subSelection_parent(findWithinGroup.getParentCollection());
			findWithinGroup.saveValues(settings);
		}
		options.setMaxResults(searchResultGroup.getMaxNumber());
		options.setMinScore(searchResultGroup.getScore() / 100f);
		options.setMethod(method);
		options.setPencilRadius(toolSettings.pencilRadius);
		options.setAirbrushRadius(toolSettings.airbrushRadius);
		options.setAirbrushIntensity(toolSettings.airbrushIntensity);
		options.setLastTool(paintExample.getCurrentTool());
		options.setPngImage((asset == null) ? pngImage : null);
		options.setKeywords(null);
		options.setKeywordWeight(scale.getSelection());
		Core.getCore().getDbManager().safeTransaction(null, options);
		super.okPressed();
	}

	public SmartCollectionImpl getResult() {
		return collection;
	}

	@Override
	public boolean close() {
		if (disposeImage)
			image.dispose();
		return super.close();
	}

	private void updateKeywordField() {
		keywordField.setEnabled(scale.getSelection() > 0 || !scale.isEnabled());
	}

}
