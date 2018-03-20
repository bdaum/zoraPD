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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.osgi.framework.Bundle;

import com.bdaum.aoModeling.runtime.IIdentifiableObject;
import com.bdaum.zoom.cat.model.Rgb_type;
import com.bdaum.zoom.cat.model.Rgb_typeImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.GroupImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitImpl;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitionImpl;
import com.bdaum.zoom.cat.model.group.exhibition.Wall;
import com.bdaum.zoom.cat.model.group.slideShow.SlideImpl;
import com.bdaum.zoom.cat.model.group.slideShow.SlideShowImpl;
import com.bdaum.zoom.cat.model.group.webGallery.Storyboard;
import com.bdaum.zoom.cat.model.group.webGallery.StoryboardImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebExhibitImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebGalleryImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebParameter;
import com.bdaum.zoom.cat.model.group.webGallery.WebParameterImpl;
import com.bdaum.zoom.common.internal.FileLocator;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.net.core.ftp.FtpAccount;
import com.bdaum.zoom.operations.internal.gen.AbstractGalleryGenerator;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.ExportXmpViewerFilter;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiConstants;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.ZViewerComparator;
import com.bdaum.zoom.ui.internal.html.HtmlContentAssistant;
import com.bdaum.zoom.ui.internal.html.HtmlSourceViewer;
import com.bdaum.zoom.ui.internal.html.XMLCodeScanner;
import com.bdaum.zoom.ui.internal.views.WebGalleryView;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.DescriptionGroup;
import com.bdaum.zoom.ui.internal.widgets.ExpandCollapseGroup;
import com.bdaum.zoom.ui.internal.widgets.FileEditor;
import com.bdaum.zoom.ui.internal.widgets.QualityGroup;
import com.bdaum.zoom.ui.internal.widgets.RadioButtonGroup;
import com.bdaum.zoom.ui.internal.widgets.WebColorGroup;
import com.bdaum.zoom.ui.internal.widgets.WebFontGroup;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.bdaum.zoom.ui.widgets.NumericControl;

@SuppressWarnings("restriction")
public class WebGalleryEditDialog extends ZTitleAreaDialog {

	private static final String HIDE_DOWNLOAD = "hideDownload"; //$NON-NLS-1$
	private static final String HIDE_HEADER = "hideHeader"; //$NON-NLS-1$
	private static final String HIDE_FOOTER = "hideFooter"; //$NON-NLS-1$
	private static final String WEB_PARAMETERS = "webParameters"; //$NON-NLS-1$
	private static final String WEB_ENGINE = "webEngine"; //$NON-NLS-1$
	private static final String XMP_FILTER = "xmpFilter"; //$NON-NLS-1$
	private static final String SHOW_META = "showMeta"; //$NON-NLS-1$
	private static final String FOOTER_FONT = "footerFont"; //$NON-NLS-1$
	private static final String NAVIGATION_FONT = "navigationFont"; //$NON-NLS-1$
	private static final String BODY_FONT = "bodyFont"; //$NON-NLS-1$
	private static final String CAPTION_FONT = "captionFont"; //$NON-NLS-1$
	private static final String TITLE_FONT = "titleFont"; //$NON-NLS-1$
	private static final String SECTION_FONT = "sectionFont"; //$NON-NLS-1$
	private static final String PADDING = "padding"; //$NON-NLS-1$
	private static final String MARGINRIGHT = "marginRight"; //$NON-NLS-1$
	private static final String MARGINTOP = "marginTop"; //$NON-NLS-1$
	private static final String MARGINBOTTOM = "marginBottom"; //$NON-NLS-1$
	private static final String MARGINLEFT = "marginLeft"; //$NON-NLS-1$
	private static final String MARGINS = "margins"; //$NON-NLS-1$

	private static final String THUMBSIZE2 = "thumbsize"; //$NON-NLS-1$
	private static final String OPACITY = "opacity"; //$NON-NLS-1$
	private static final String LINK_COLOR = "linkColor"; //$NON-NLS-1$
	private static final String SHADE_COLOR = "shadeColor"; //$NON-NLS-1$
	private static final String BORDER_COLOR = "borderColor"; //$NON-NLS-1$
	private static final String BG_COLOR = "bgColor"; //$NON-NLS-1$
	private static final String WATER_MARK = "waterMark"; //$NON-NLS-1$
	private static final String WEBURL = "weburl"; //$NON-NLS-1$
	private static final String EMAIL = "email"; //$NON-NLS-1$
	private static final String CONTACT = "contact"; //$NON-NLS-1$
	private static final String COPYRIGHT = "copyright"; //$NON-NLS-1$
	private static final String REPEAT_BG = "repeatBg"; //$NON-NLS-1$
	private static final String BG_IMAGE = "bgImage"; //$NON-NLS-1$
	private static final String LOGO = "logo"; //$NON-NLS-1$
	private static final String DOWNLOAD_TEXT = "downloadText"; //$NON-NLS-1$
	private static final String LINKPAGE = "linkpage"; //$NON-NLS-1$
	private static final String POWEREDBY_TEXT = "powered_by"; //$NON-NLS-1$
	private static final String TOPHTML = "headerHtml"; //$NON-NLS-1$
	private static final String FOOTERHTML = "footerHtml"; //$NON-NLS-1$
	private static final String HEADHTML = "headHtml"; //$NON-NLS-1$

	private static final String[] TITLEFAMILY = new String[] { "Lucida Sans Unicode", "Lucida Grande", "sans-serif" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String[] BODYFAMILY = new String[] { "Trebuchet MS1", //$NON-NLS-1$
			"Helvetica", "sans-serif" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] THUMBSIZES = new String[] { Messages.WebGalleryEditDialog_medium,
			Messages.WebGalleryEditDialog_large, Messages.WebGalleryEditDialog_very_large,
			Messages.WebGalleryEditDialog_small, Messages.WebGalleryEditDialog_very_small,
			Messages.WebGalleryEditDialog_medium_square, Messages.WebGalleryEditDialog_large_square,
			Messages.WebGalleryEditDialog_very_large_square, Messages.WebGalleryEditDialog_small_square,
			Messages.WebGalleryEditDialog_very_small_square };
	private static final int LOAD = 9999;
	private static final String SETTINGSID = "com.bdaum.zoom.webgalleryProperties"; //$NON-NLS-1$
	private static final int PREVIOUS = 99;
	private static final int NEXT = 98;
	private static final String TRUE = "true"; //$NON-NLS-1$
	private static final String FALSE = "false"; //$NON-NLS-1$
	private final WebGalleryImpl current;
	private final String title;
	private Text nameField;
	private Map<String, Control> unsupportedFieldMap = new HashMap<String, Control>();
	private boolean initialised = false;

	private final ModifyListener modifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			if (initialised) {
				updateWebParameterEnablement();
				updateButtons();
			}
		}
	};

	private final SelectionAdapter selectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (initialised) {
				updateWebParameterEnablement();
				updateButtons();
			}
		}
	};

	private final boolean canUndo;
	private WebGalleryImpl result;
	private FileEditor logoField;
	private Text copyrightField, contactField, emailField, weburlField, keywordField, poweredByField, linkField,
			downloadField;
	private CheckboxButton watermarkButton, repeatBgButton, showMetaButton, hideHeaderButton, hideFooterButton,
			downloadButton;
	private Combo thumbsizeField;
	private WebColorGroup borderButton, shadeButton, linkButton, bgButton;
	private NumericControl marginsTopField, marginsBottomField, marginsRightField, marginsLeftField, paddingField;
	private WebFontGroup titleButton, sectionButton, captionButton, descriptionButton, footerButton, navigationButton;
	private Scale opacityField;
	private GroupImpl group;
	private ComboViewer engineViewer;
	private IConfigurationElement selectedEngine;
	private final boolean promptForEngine;
	private List<IConfigurationElement> generators;
	private Map<String, Composite> compMap = new HashMap<String, Composite>();
	private Map<String, Object> controlMap = new HashMap<String, Object>();
	private Composite emptyComp, parmComp;
	private CheckboxTreeViewer metaViewer;
	private FileEditor imageField;
	private Browser browser;
	private CTabFolder tabFolder;
	private ImportGalleryGroup importGroup;
	private OutputTargetGroup outputTargetGroup;
	private IDialogSettings settings;
	private Rgb_type bgColor, borderColor, shadeColor, linkColor;
	private HtmlSourceViewer topViewer, headViewer, footerViewer;
	private Map<IConfigurationElement, String> linkMap = new HashMap<IConfigurationElement, String>();
	private DescriptionGroup descriptionGroup;
	private QualityGroup qualityGroup;

	public WebGalleryEditDialog(Shell parentShell, GroupImpl group, WebGalleryImpl current, String title,
			boolean promptForEngine, boolean canUndo) {
		super(parentShell, HelpContextIds.WEBGALLERY_DIALOG);
		this.group = group;
		this.current = current;
		this.title = title;
		this.promptForEngine = promptForEngine;
		this.canUndo = canUndo;
		generators = new ArrayList<IConfigurationElement>();
		for (IExtension ext : Platform.getExtensionRegistry()
				.getExtensionPoint(UiActivator.PLUGIN_ID, "galleryGenerator").getExtensions()) //$NON-NLS-1$
			generators.addAll(Arrays.asList(ext.getConfigurationElements()));
		settings = getDialogSettings(UiActivator.getDefault(), SETTINGSID);
	}

	public static WebGalleryImpl openWebGalleryEditDialog(final Shell shell, final GroupImpl group,
			final WebGalleryImpl gal, final String title, final boolean promptForEngine, final boolean canUndo,
			final String errorMsg) {
		final WebGalleryImpl[] box = new WebGalleryImpl[1];
		BusyIndicator.showWhile(shell.getDisplay(), () -> {
			WebGalleryEditDialog dialog = new WebGalleryEditDialog(shell, group, gal, title, promptForEngine, canUndo);
			if (errorMsg != null) {
				dialog.create();
				dialog.selectOutputPage(errorMsg);
			}
			if (dialog.open() == Window.OK)
				box[0] = dialog.getResult();
		});
		return box[0];
	}

	@Override
	public void create() {
		super.create();
		setTitle(title);
		setMessage(Messages.WebGalleryEditDialog_specify_the_web_gallery_properties);
		fillValues(current, false);
		initialised = true;
		updateParms();
		updateButtons();
		updateDownloadField();
		updateHeaderFields();
		updateFooterFields();
		updateWebParameterEnablement();
		fillPreview();
		tabFolder.setSelection(promptForEngine ? 3 : 0);
		nameField.setFocus();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);

		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, false));
		if (current == null) {
			importGroup = new ImportGalleryGroup(composite, new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1),
					Messages.WebGalleryEditDialog_web_gallery);
			importGroup.addChangeListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					IIdentifiableObject fromItem = importGroup.getFromItem();
					if (fromItem instanceof WebGalleryImpl)
						fillValues((WebGalleryImpl) fromItem, true);
					descriptionGroup.setText(importGroup.getDescription(), false);
				}
			});
		}

		tabFolder = new CTabFolder(composite, SWT.BORDER);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		createOverview(UiUtilities.createTabPage(tabFolder, Messages.WebGalleryEditDialog_overview, null));
		createAppearanceGroup(UiUtilities.createTabPage(tabFolder, Messages.WebGalleryEditDialog_appearance, null));
		createHtmlGroup(UiUtilities.createTabPage(tabFolder, "&HTML", null));//$NON-NLS-1$
		createMetaGroup(UiUtilities.createTabPage(tabFolder, Messages.WebGalleryEditDialog_metadata, null));
		createEngineGroup(UiUtilities.createTabPage(tabFolder, Messages.WebGalleryEditDialog_web_engine, null));
		createOutputGroup(UiUtilities.createTabPage(tabFolder, Messages.WebGalleryEditDialog_output, null));

		Composite previewComp = new Composite(composite, SWT.NONE);
		GridData layoutData = new GridData(GridData.FILL_VERTICAL);
		layoutData.widthHint = 360;
		layoutData.heightHint = 550;
		previewComp.setLayoutData(layoutData);
		previewComp.setLayout(new GridLayout());
		new Label(previewComp, SWT.NONE).setText(Messages.WebGalleryEditDialog_selected_web_engine_characteristics);
		browser = new Browser(previewComp, SWT.NONE);
		browser.setLayoutData(new GridData(GridData.FILL_BOTH));
		tabFolder.setSimple(false);
		tabFolder.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtons();
				updateTabItems();
			}

		});
		tabFolder.setSelection(0);
		updateTabItems();
		return area;
	}

	protected void updateTabItems() {
		CTabItem selection = tabFolder.getSelection();
		for (CTabItem item : tabFolder.getItems())
			item.setFont(selection == item ? JFaceResources.getBannerFont() : JFaceResources.getDefaultFont());
	}

	private void fillPreview() {
		if (selectedEngine == null) {
			showMessage(Messages.WebGalleryEditDialog_no_engine_selected);
		} else {
			String des = selectedEngine.getAttribute("description"); //$NON-NLS-1$
			if (des == null)
				showMessage(Messages.WebGalleryEditDialog_selected_engine_without_description);
			else {
				String id = selectedEngine.getNamespaceIdentifier();
				for (Bundle bundle : UiActivator.getDefault().getBundle().getBundleContext().getBundles())
					if (bundle.getSymbolicName().equals(id)) {
						try {
							URL u = FileLocator.findFileURL(bundle, "/$nl$/" //$NON-NLS-1$
									+ des, true);
							if (u != null)
								browser.setUrl(u.toString());
						} catch (IOException e) {
							// ignore
						}
						break;
					}

			}
		}
	}

	private void showMessage(String msg) {
		RGB bg = getShell().getBackground().getRGB();
		String bgcolor = AbstractGalleryGenerator.toHtmlColors(bg.red, bg.green, bg.blue);
		RGB fg = getShell().getForeground().getRGB();
		String color = AbstractGalleryGenerator.toHtmlColors(fg.red, fg.green, fg.blue);
		browser.setText("<html><body style=\"background-color:" + bgcolor //$NON-NLS-1$
				+ "; color:" + color + ";\">" + "<p align=\"center\">" + msg //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ "</p></body></html>"); //$NON-NLS-1$
	}

	private String[] initValues() {
		outputTargetGroup.initValues(settings);
		String link = settings.get(LINKPAGE);
		if (link == null || link.isEmpty())
			link = "index.html"; //$NON-NLS-1$
		setCondText(linkField, link);
		String download = settings.get(DOWNLOAD_TEXT);
		if (download == null || download.isEmpty())
			download = Messages.WebGalleryEditDialog_download_original;
		setCondText(downloadField, download);
		downloadButton.setSelection(settings.getBoolean(HIDE_DOWNLOAD));
		hideHeaderButton.setSelection(settings.getBoolean(HIDE_HEADER));
		hideFooterButton.setSelection(settings.getBoolean(HIDE_FOOTER));
		setCondText(logoField, settings.get(LOGO));
		setCondText(imageField, settings.get(BG_IMAGE));
		repeatBgButton.setSelection(settings.getBoolean(REPEAT_BG));
		String copyright = settings.get(COPYRIGHT);
		if (copyright == null || copyright.isEmpty())
			copyright = new GregorianCalendar().get(Calendar.YEAR) + " "; //$NON-NLS-1$
		setCondText(copyrightField, copyright);
		setCondText(contactField, settings.get(CONTACT));
		setCondText(emailField, settings.get(EMAIL));
		setCondText(weburlField, settings.get(WEBURL));
		String poweredBy = settings.get(POWEREDBY_TEXT);
		if (poweredBy == null || poweredBy.isEmpty())
			poweredBy = Messages.WebGalleryEditDialog_powered_by;
		setCondText(poweredByField, poweredBy);
		watermarkButton.setSelection(settings.getBoolean(WATER_MARK));
		qualityGroup.fillValues(settings);
		showMetaButton.setSelection(settings.getBoolean(SHOW_META));
		bgColor = bgButton.fillValues(settings, BG_COLOR, 240, 240, 240);
		borderColor = borderButton.fillValues(settings, BORDER_COLOR, 128, 128, 255);
		shadeColor = shadeButton.fillValues(settings, SHADE_COLOR, 224, 224, 224);
		linkColor = linkButton.fillValues(settings, LINK_COLOR, 32, 112, 208);
		// Colors
		try {
			opacityField.setSelection(settings.getInt(OPACITY));
		} catch (NumberFormatException e) {
			opacityField.setSelection(67);
		}
		try {
			int thumbSize = Math.max(0, Math.min(thumbsizeField.getItemCount() - 1, settings.getInt(THUMBSIZE2)));
			thumbsizeField.select(thumbSize);
			thumbsizeField.setText(thumbsizeField.getItem(thumbSize));
		} catch (NumberFormatException e) {
			thumbsizeField.select(0);
			thumbsizeField.setText(thumbsizeField.getItem(0));
		}
		initNumericControl(paddingField, PADDING, 5);
		initNumericControl(marginsTopField, MARGINTOP, 5);
		initNumericControl(marginsRightField, MARGINRIGHT, 10);
		initNumericControl(marginsBottomField, MARGINBOTTOM, 5);
		initNumericControl(marginsLeftField, MARGINLEFT, 0);
		titleButton.fillValues(settings, TITLE_FONT, TITLEFAMILY, 250, 0, 1, 0, new Rgb_typeImpl(48, 48, 48));
		sectionButton.fillValues(settings, SECTION_FONT, TITLEFAMILY, 200, 0, 1, 0, new Rgb_typeImpl(64, 64, 64));
		captionButton.fillValues(settings, CAPTION_FONT, BODYFAMILY, 75, 0, 0, 0, new Rgb_typeImpl(64, 64, 64));
		descriptionButton.fillValues(settings, BODY_FONT, BODYFAMILY, 75, 0, 0, 0, new Rgb_typeImpl(64, 64, 64));
		footerButton.fillValues(settings, FOOTER_FONT, BODYFAMILY, 85, 0, 0, 0, new Rgb_typeImpl(112, 112, 112));
		navigationButton.fillValues(settings, NAVIGATION_FONT, BODYFAMILY, 85, 0, 0, 0, new Rgb_typeImpl(64, 64, 64));
		String headHtml = settings.get(HEADHTML);
		if (headHtml != null)
			headViewer.setDocument(new Document(headHtml));
		String topHtml = settings.get(TOPHTML);
		if (topHtml != null)
			topViewer.setDocument(new Document(topHtml));
		String footerHtml = settings.get(FOOTERHTML);
		if (footerHtml != null)
			footerViewer.setDocument(new Document(footerHtml));
		setSelectedWebEngine(settings.get(WEB_ENGINE));
		String[] array = settings.getArray(WEB_PARAMETERS);
		if (array != null) {
			Map<String, WebParameter> parameters = new HashMap<String, WebParameter>(array.length * 3 / 2);
			for (String s : array) {
				int p = s.indexOf('=');
				if (p >= 0) {
					String key = s.substring(0, p);
					parameters.put(key, new WebParameterImpl(key, s.substring(p + 1), false, null));
				}
			}
			setWebParameters(parameters);
		}
		return settings.getArray(XMP_FILTER);
	}

	private void initNumericControl(NumericControl control, String key, int dflt) {
		try {
			control.setSelection(settings.getInt(key));
		} catch (NumberFormatException e) {
			control.setSelection(dflt);
		}
	}

	protected void saveSettings(WebGalleryImpl gallery) {
		outputTargetGroup.saveValues(settings);
		settings.put(LINKPAGE, gallery.getPageName());
		settings.put(DOWNLOAD_TEXT, gallery.getDownloadText());
		settings.put(HIDE_DOWNLOAD, downloadButton.getSelection());
		settings.put(HIDE_HEADER, hideHeaderButton.getSelection());
		settings.put(HIDE_FOOTER, hideFooterButton.getSelection());
		settings.put(POWEREDBY_TEXT, gallery.getPoweredByText());
		settings.put(LOGO, gallery.getLogo());
		settings.put(BG_IMAGE, gallery.getBgImage());
		settings.put(REPEAT_BG, gallery.getBgRepeat());
		settings.put(COPYRIGHT, gallery.getCopyright());
		settings.put(CONTACT, gallery.getContactName());
		settings.put(EMAIL, gallery.getEmail());
		settings.put(WEBURL, gallery.getWebUrl());
		settings.put(WATER_MARK, gallery.getAddWatermark());
		settings.put(SHOW_META, gallery.getShowMeta());
		bgButton.saveSettings(settings, BG_COLOR);
		borderButton.saveSettings(settings, BORDER_COLOR);
		shadeButton.saveSettings(settings, SHADE_COLOR);
		linkButton.saveSettings(settings, LINK_COLOR);
		settings.put(OPACITY, gallery.getOpacity());
		settings.put(THUMBSIZE2, gallery.getThumbSize());
		settings.put(PADDING, gallery.getPadding());
		int[] margins = gallery.getMargins();
		if (margins != null && margins.length >= 4) {
			settings.put(MARGINTOP, margins[0]);
			settings.put(MARGINRIGHT, margins[1]);
			settings.put(MARGINBOTTOM, margins[2]);
			settings.put(MARGINLEFT, margins[3]);
		}
		titleButton.saveSettings(settings, TITLE_FONT);
		sectionButton.saveSettings(settings, SECTION_FONT);
		captionButton.saveSettings(settings, CAPTION_FONT);
		descriptionButton.saveSettings(settings, BODY_FONT);
		footerButton.saveSettings(settings, FOOTER_FONT);
		navigationButton.saveSettings(settings, NAVIGATION_FONT);
		settings.put(TOPHTML, topViewer.getDocument().get());
		settings.put(HEADHTML, headViewer.getDocument().get());
		settings.put(FOOTERHTML, footerViewer.getDocument().get());
		settings.put(XMP_FILTER, gallery.getXmpFilter());
		settings.put(WEB_ENGINE, gallery.getSelectedEngine());
		Map<String, WebParameter> parameters = gallery.getParameter();
		List<String> parmlist = new ArrayList<String>(parameters.size());
		for (WebParameter parm : parameters.values())
			parmlist.add(parm.toString());
		settings.put(WEB_PARAMETERS, parmlist.toArray(new String[parmlist.size()]));
		qualityGroup.saveSettings(settings);
		logoField.saveValues();
		imageField.saveValues();
	}

	private void fillValues(WebGalleryImpl gallery, boolean template) {
		String[] xmpFilter = initValues();
		engineViewer.setInput(generators);
		if (gallery != null) {
			if (!template) {
				setText(nameField, gallery.getName());
				descriptionGroup.setText(gallery.getDescription(), gallery.getHtmlDescription());
				setText(linkField, gallery.getPageName());
				String[] keywords = gallery.getKeyword();
				if (keywords != null) {
					StringBuilder sb = new StringBuilder();
					for (String kw : keywords) {
						if (sb.length() > 0)
							sb.append('\n');
						sb.append(kw);
					}
					keywordField.setText(sb.toString());
				}
				xmpFilter = gallery.getXmpFilter();
			}
			setText(downloadField, gallery.getDownloadText());
			downloadButton.setSelection(gallery.getHideDownload());
			hideHeaderButton.setSelection(gallery.getHideHeader());
			hideFooterButton.setSelection(gallery.getHideFooter());
			setCondText(logoField, gallery.getLogo());
			setCondText(imageField, gallery.getBgImage());
			repeatBgButton.setSelection(gallery.getBgRepeat());
			setCondText(copyrightField, gallery.getCopyright());
			setCondText(contactField, gallery.getContactName());
			setCondText(emailField, gallery.getEmail());
			setCondText(weburlField, gallery.getWebUrl());
			setCondText(poweredByField, gallery.getPoweredByText());
			watermarkButton.setSelection(gallery.getAddWatermark());
			bgColor = gallery.getBgColor();
			shadeColor = gallery.getShadeColor();
			borderColor = gallery.getBorderColor();
			linkColor = gallery.getLinkColor();
			titleButton.setFont(gallery.getTitleFont());
			sectionButton.setFont(gallery.getSectionFont());
			captionButton.setFont(gallery.getCaptionFont());
			navigationButton.setFont(gallery.getControlsFont());
			descriptionButton.setFont(gallery.getDescriptionFont());
			footerButton.setFont(gallery.getFooterFont());
			String headHtml = gallery.getHeadHtml();
			if (headHtml != null)
				headViewer.setDocument(new Document(headHtml));
			String topHtml = gallery.getTopHtml();
			if (topHtml != null)
				topViewer.setDocument(new Document(topHtml));
			String footerHtml = gallery.getFooterHtml();
			if (footerHtml != null)
				footerViewer.setDocument(new Document(footerHtml));
			opacityField.setSelection(gallery.getOpacity());
			int thumbSize = Math.max(0, Math.min(thumbsizeField.getItemCount() - 1, gallery.getThumbSize()));
			thumbsizeField.select(thumbSize);
			thumbsizeField.setText(thumbsizeField.getItem(thumbSize));
			int padding = gallery.getPadding();
			paddingField.setSelection(padding);
			int[] margins = gallery.getMargins();
			if (margins == null || margins.length < 4) {
				marginsTopField.setSelection(padding);
				marginsRightField.setSelection(padding * 2);
				marginsBottomField.setSelection(padding);
				marginsLeftField.setSelection(0);
			} else {
				marginsTopField.setSelection(margins[0]);
				marginsRightField.setSelection(margins[1]);
				marginsBottomField.setSelection(margins[2]);
				marginsLeftField.setSelection(margins[3]);
			}
			if (!template) {
				outputTargetGroup.setLocalFolder(gallery.getOutputFolder());
				outputTargetGroup.setFtpDir(gallery.getFtpDir());
				outputTargetGroup.setTarget(gallery.getIsFtp() ? Constants.FTP : Constants.FILE);
			}
			showMetaButton.setSelection(gallery.getShowMeta());
			qualityGroup.fillValues(gallery.getApplySharpening(), gallery.getRadius(), gallery.getAmount(),
					gallery.getThreshold(), gallery.getJpegQuality(), gallery.getScalingMethod());
			setWebParameters(gallery.getParameter());
			setSelectedWebEngine(gallery.getSelectedEngine());
			constructLinks(gallery);
		}
		if (xmpFilter != null) {
			List<QueryField> fields = new ArrayList<QueryField>(xmpFilter.length);
			for (String id : xmpFilter) {
				QueryField qfield = QueryField.findQueryField(id);
				if (qfield != null)
					fields.add(qfield);
			}
		}
		if (bgColor == null)
			bgColor = new Rgb_typeImpl(240, 240, 240);
		if (shadeColor == null)
			shadeColor = new Rgb_typeImpl(224, 224, 224);
		if (borderColor == null)
			borderColor = new Rgb_typeImpl(128, 128, 255);
		if (linkColor == null)
			linkColor = new Rgb_typeImpl(32, 112, 208);
		bgButton.setRGB(bgColor);
		shadeButton.setRGB(shadeColor);
		borderButton.setRGB(borderColor);
		linkButton.setRGB(linkColor);
	}

	private void constructLinks(final WebGalleryImpl gallery) {
		for (Map.Entry<IConfigurationElement, String> entry : linkMap.entrySet()) {
			final IConfigurationElement fromConf = entry.getKey();
			final Object target = controlMap.get(entry.getValue());
			if (target instanceof Combo) {
				@SuppressWarnings("unchecked")
				String v = ((Map<String, String>) ((Combo) target).getData("map")).get(((Combo) target).getText()); //$NON-NLS-1$
				Object control = fillParmValue(fromConf, v, gallery.getParameter());
				if (control != null)
					((Combo) target).addSelectionListener(new SelectionAdapter() {
						@SuppressWarnings("unchecked")
						@Override
						public void widgetSelected(SelectionEvent e) {
							saveParameterValue(fromConf, gallery.getParameter());
							fillParmValue(fromConf, ((Map<String, String>) ((Combo) target).getData("map")) //$NON-NLS-1$
									.get(((Combo) target).getText()), gallery.getParameter());
						}
					});
			} else if (target instanceof RadioButtonGroup) {
				int selection = ((RadioButtonGroup) target).getSelection();
				if (selection >= 0) {
					Object control = fillParmValue(fromConf,
							((String[]) ((RadioButtonGroup) target).getData("map"))[selection], gallery.getParameter()); //$NON-NLS-1$
					if (control != null)
						((RadioButtonGroup) target).addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								saveParameterValue(fromConf, gallery.getParameter());
								fillParmValue(fromConf,
										((String[]) ((RadioButtonGroup) target)
												.getData("map"))[((RadioButtonGroup) target) //$NON-NLS-1$
														.getSelection()],
										gallery.getParameter());
							}
						});
				}
			}
		}
	}

	private void setWebParameters(Map<String, WebParameter> parameters) {
		for (IConfigurationElement element : generators)
			for (IConfigurationElement child : element.getChildren()) {
				if (!"group".equals(child.getName())) //$NON-NLS-1$
					fillParmValue(child, null, parameters);
			}
	}

	private void updateWebParameterEnablement() {
		for (IConfigurationElement element : generators) {
			String ns = element.getNamespaceIdentifier();
			for (IConfigurationElement child : element.getChildren()) {
				if ("group".equals(child.getName())) //$NON-NLS-1$
					continue;
				String enabledIf = child.getAttribute("enabledIf"); //$NON-NLS-1$
				if (enabledIf != null && !enabledIf.isEmpty()) {
					boolean enabled = processEnablement(enabledIf, ns);
					String id = child.getAttribute("id"); //$NON-NLS-1$
					Object control = controlMap.get(computeKey(ns, id));
					if (control instanceof Control)
						((Control) control).setEnabled(enabled);
					else if (control instanceof WebColorGroup)
						((WebColorGroup) control).setEnabled(enabled);
				}
			}
		}
	}

	private boolean processEnablement(String enabledIf, String ns) {
		StringTokenizer st = new StringTokenizer(enabledIf, "!&| ", true); //$NON-NLS-1$
		Stack<Object> stack = new Stack<>();
		while (st.hasMoreTokens()) {
			String token = st.nextToken().trim();
			if (token.isEmpty())
				continue;
			if ("!".equals(token) && !stack.isEmpty()) //$NON-NLS-1$
				stack.push(popBoolean(stack) ? FALSE : TRUE);
			else if ("&".equals(token) && stack.size() > 1) //$NON-NLS-1$
				stack.push(popBoolean(stack) & popBoolean(stack) ? TRUE : FALSE);
			else if ("|".equals(token) && stack.size() > 1) //$NON-NLS-1$
				stack.push(popBoolean(stack) | popBoolean(stack) ? TRUE : FALSE);
			else if (token.startsWith("\"") && token.endsWith("\"")) //$NON-NLS-1$ //$NON-NLS-2$
				stack.push(token.substring(1, token.length() - 1).equals(popText(stack)) ? TRUE : FALSE);
			else {
				Object control = controlMap.get(computeKey(ns, token));
				if (control != null)
					stack.push(control);
				else
					stack.push(FALSE);
			}
		}
		return stack.isEmpty() ? true : popBoolean(stack);
	}

	@SuppressWarnings("unchecked")
	private static Object popText(Stack<Object> stack) {
		Object pop = stack.pop();
		if (pop instanceof Text)
			return ((Text) pop).getText();
		else if (pop instanceof Combo) {
			String text = ((Combo) pop).getText().trim();
			if (!text.isEmpty())
				return ((Map<String, String>) ((Combo) pop).getData("map")).get(text); //$NON-NLS-1$
		} else if (pop instanceof RadioButtonGroup) {
			int selection = ((RadioButtonGroup) pop).getSelection();
			if (selection >= 0)
				return ((String[]) ((RadioButtonGroup) pop).getData("map"))[selection]; //$NON-NLS-1$
		} else if (pop instanceof NumericControl)
			return String.valueOf(((NumericControl) pop).getSelection());
		return FALSE;
	}

	private static boolean popBoolean(Stack<Object> stack) {
		Object pop = stack.pop();
		if (pop == TRUE)
			return true;
		if (pop == FALSE)
			return false;
		if (pop instanceof CheckboxButton)
			return ((CheckboxButton) pop).getSelection();
		return false;
	}

	private void setSelectedWebEngine(String engineId) {
		if (engineId != null && !engineId.isEmpty())
			for (IConfigurationElement generator : generators)
				if (engineId.equals(generator.getAttribute("id"))) { //$NON-NLS-1$
					engineViewer.setSelection(new StructuredSelection(generator));
					break;
				}
	}

	@SuppressWarnings("unused")
	private void createOverview(final Composite parent) {
		parent.setLayout(new GridLayout(3, false));
		new Label(parent, SWT.NONE).setText(Messages.WebGalleryEditDialog_name);
		nameField = new Text(parent, SWT.BORDER);
		nameField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		nameField.addModifyListener(modifyListener);
		hideHeaderButton = WidgetFactory.createCheckButton(parent, Messages.WebGalleryEditDialog_hide, null);
		hideHeaderButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateHeaderFields();
				if (nameField.getEnabled())
					nameField.setFocus();
			}
		});
		descriptionGroup = new DescriptionGroup(parent, SWT.NONE);
		// Link
		new Label(parent, SWT.NONE).setText(Messages.WebGalleryEditDialog_web_link);
		linkField = new Text(parent, SWT.BORDER);
		linkField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		linkField.addModifyListener(modifyListener);
		// Keywords
		final Label keywordLabel = new Label(parent, SWT.NONE);
		keywordLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		keywordLabel.setText(Messages.WebGalleryEditDialog_keywords);
		keywordField = new Text(parent, SWT.WRAP | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER);
		final GridData gd_keywordField = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd_keywordField.widthHint = 150;
		gd_keywordField.heightHint = 40;
		keywordField.setLayoutData(gd_keywordField);
		Button keywordButton = new Button(parent, SWT.PUSH);
		keywordButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		keywordButton.setText(Messages.WebGalleryEditDialog_add_image_keywords);
		keywordButton.setEnabled(hasImages());
		keywordButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addImageKeywords();
			}
		});
		// Download
		final Label downloadLabel = new Label(parent, SWT.NONE);
		downloadLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		downloadLabel.setText(Messages.WebGalleryEditDialog_download_link);
		unsupportedFieldMap.put("download", downloadLabel); //$NON-NLS-1$
		downloadField = new Text(parent, SWT.BORDER);
		downloadLabel.setData("control", downloadField); //$NON-NLS-1$
		final GridData gd_downloadField = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd_downloadField.widthHint = 250;
		downloadField.setLayoutData(gd_downloadField);
		downloadField.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				if (!downloadButton.getSelection())
					downloadField.setText(
							downloadField.getText().isEmpty() ? Messages.WebGalleryEditDialog_download_original : ""); //$NON-NLS-1$
			}
		});
		downloadButton = WidgetFactory.createCheckButton(parent, Messages.WebGalleryEditDialog_hide, null);
		downloadField.setData("button", downloadButton); //$NON-NLS-1$
		downloadButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateDownloadField();
				if (downloadField.getEnabled())
					downloadField.setFocus();
			}
		});
		// Nameplate and Watermark
		new Label(parent, SWT.NONE).setText(Messages.WebGalleryEditDialog_name_plate);
		logoField = createImageGroup(parent, Messages.WebGalleryEditDialog_select_nameplate_image);
		logoField.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
		new Label(parent, SWT.NONE);
		watermarkButton = WidgetFactory.createCheckButton(parent, Messages.WebGalleryEditDialog_create_watermarks,
				new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1));
		// Footer
		// Copyright
		final Label copyrightLabel = new Label(parent, SWT.NONE);
		copyrightLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		copyrightLabel.setText(Messages.WebGalleryEditDialog_copyright);

		copyrightField = new Text(parent, SWT.BORDER);
		final GridData gd_copyrightField = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd_copyrightField.widthHint = 250;
		copyrightField.setLayoutData(gd_copyrightField);
		hideFooterButton = WidgetFactory.createCheckButton(parent, Messages.WebGalleryEditDialog_hide, null);
		hideFooterButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateFooterFields();
				if (contactField.getEnabled())
					contactField.setFocus();
			}
		});
		final Label contactLabel = new Label(parent, SWT.NONE);
		contactLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		contactLabel.setText(Messages.WebGalleryEditDialog_contact);
		contactField = new Text(parent, SWT.BORDER);
		final GridData gd_contactField = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd_contactField.widthHint = 250;
		contactField.setLayoutData(gd_contactField);
		new Label(parent, SWT.NONE);
		final Label emailLabel = new Label(parent, SWT.NONE);
		emailLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		emailLabel.setText(Messages.WebGalleryEditDialog_email);
		emailField = new Text(parent, SWT.BORDER);
		final GridData gd_emailField = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd_emailField.widthHint = 250;
		emailField.setLayoutData(gd_emailField);
		new Label(parent, SWT.NONE);
		final Label weburlLabel = new Label(parent, SWT.NONE);
		weburlLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		weburlLabel.setText(Messages.WebGalleryEditDialog_web_url);
		weburlField = new Text(parent, SWT.BORDER);
		final GridData gd_weburlField = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd_weburlField.widthHint = 250;
		weburlField.setLayoutData(gd_weburlField);
		new Label(parent, SWT.NONE);
		final Label poweredByLabel = new Label(parent, SWT.NONE);
		poweredByLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		poweredByLabel.setText(Messages.WebGalleryEditDialog_engine_reference);
		poweredByField = new Text(parent, SWT.BORDER);
		final GridData gd_poweredByField = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd_poweredByField.widthHint = 250;
		poweredByField.setLayoutData(gd_poweredByField);
		poweredByField.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				poweredByField
						.setText(poweredByField.getText().isEmpty() ? Messages.WebGalleryEditDialog_powered_by : ""); //$NON-NLS-1$
			}
		});
	}

	protected void updateDownloadField() {
		downloadField.setEnabled(!downloadButton.getSelection());
	}

	protected void updateHeaderFields() {
		boolean enabled = !hideHeaderButton.getSelection();
		nameField.setEnabled(enabled);
		descriptionGroup.setEnabled(enabled);
	}

	protected void updateFooterFields() {
		boolean enabled = !hideFooterButton.getSelection();
		contactField.setEnabled(enabled);
		emailField.setEnabled(enabled);
		weburlField.setEnabled(enabled);
		poweredByField.setEnabled(enabled);
		copyrightField.setEnabled(enabled);
	}

	protected void addImageKeywords() {
		if (current == null)
			return;
		final List<Storyboard> storyboards = current.getStoryboard();
		if (storyboards == null)
			return;
		BusyIndicator.showWhile(getShell().getDisplay(), () -> {
			Set<String> keywords = new HashSet<String>();
			StringTokenizer st = new StringTokenizer(keywordField.getText().trim(), "\n"); //$NON-NLS-1$
			while (st.hasMoreTokens())
				keywords.add(st.nextToken());
			for (Storyboard storyboard : storyboards) {
				List<String> exhibit = storyboard.getExhibit();
				if (exhibit != null) {
					for (String exhibitId : exhibit) {
						WebExhibitImpl obj = dbManager.obtainById(WebExhibitImpl.class, exhibitId);
						if (obj != null) {
							String assetId = obj.getAsset();
							AssetImpl asset = dbManager.obtainAsset(assetId);
							if (asset != null) {
								String[] kw = asset.getKeyword();
								if (kw != null)
									for (String k : kw)
										keywords.add(k);
							}
						}
					}
				}
			}
			String[] kws = keywords.toArray(new String[keywords.size()]);
			Arrays.sort(kws, Utilities.KEYWORDCOMPARATOR);
			keywordField.setText(Core.toStringList(kws, "\n")); //$NON-NLS-1$
		});
	}

	private boolean hasImages() {
		if (current != null) {
			List<Storyboard> storyboards = current.getStoryboard();
			if (storyboards != null)
				for (Storyboard storyboard : storyboards) {
					List<String> exhibit = storyboard.getExhibit();
					if (exhibit != null && !exhibit.isEmpty())
						return true;
				}
		}
		return false;
	}

	private void createMetaGroup(Composite comp) {
		comp.setLayout(new GridLayout(2, false));
		metaViewer = createViewerGroup(comp);
		showMetaButton = WidgetFactory.createCheckButton(comp, Messages.WebGalleryEditDialog_show_metadata_in_caption,
				null);
		unsupportedFieldMap.put("showmetadata", showMetaButton); //$NON-NLS-1$
	}

	private static ContainerCheckedTreeViewer createViewerGroup(Composite comp) {
		ExpandCollapseGroup expandCollapseGroup = new ExpandCollapseGroup(comp, SWT.NONE);
		expandCollapseGroup.setLayoutData(new GridData(SWT.END, SWT.BEGINNING, true, false, 2, 1));
		final ContainerCheckedTreeViewer viewer = new ContainerCheckedTreeViewer(comp,
				SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		expandCollapseGroup.setViewer(viewer);
		viewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		viewer.setLabelProvider(new MetadataLabelProvider());
		viewer.setContentProvider(new MetadataContentProvider());
		viewer.setFilters(new ViewerFilter[] { ExportXmpViewerFilter.INSTANCE });
		viewer.setComparator(ZViewerComparator.INSTANCE);
		viewer.setInput(new QueryField[] { QueryField.EXIF_ALL, QueryField.IPTC_ALL });
		viewer.expandToLevel(2);
		UiUtilities.installDoubleClickExpansion(viewer);
		return viewer;
	}

	private void createHtmlGroup(Composite comp) {
		comp.setLayout(new GridLayout());
		Label label = new Label(comp, SWT.WRAP);
		GridData layoutData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		layoutData.verticalIndent = 15;
		label.setLayoutData(layoutData);
		label.setText(Messages.WebGalleryEditDialog_insert_your_own_html);
		headViewer = createHtmlViewer(comp, Messages.WebGalleryEditDialog_in_head_element, 50, 500);
		topViewer = createHtmlViewer(comp, Messages.WebGalleryEditDialog_header_html, 150, 500);
		footerViewer = createHtmlViewer(comp, Messages.WebGalleryEditDialog_footer_html, 150, 500);
	}

	private static HtmlSourceViewer createHtmlViewer(Composite comp, String lab, int h, int w) {
		Label label = new Label(comp, SWT.NONE);
		GridData layoutData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		layoutData.verticalIndent = 10;
		label.setLayoutData(layoutData);
		label.setText(lab);
		HtmlSourceViewer viewer = new HtmlSourceViewer(comp, null,
				SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL, new XMLCodeScanner(),
				new HtmlContentAssistant());
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = h;
		data.widthHint = w;
		viewer.getControl().setLayoutData(data);
		viewer.setDocument(new Document());
		return viewer;
	}

	@SuppressWarnings("unused")
	private void createAppearanceGroup(Composite comp) {
		comp.setLayout(new GridLayout(2, false));
		final Label imageLabel = new Label(comp, SWT.NONE);
		imageLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		imageLabel.setText(Messages.WebGalleryEditDialog_background_image);
		imageField = createImageGroup(comp, Messages.WebGalleryEditDialog_select_background_image);
		new Label(comp, SWT.NONE);
		repeatBgButton = WidgetFactory.createCheckButton(comp, Messages.WebGalleryEditDialog_repeat, null);
		CGroup colorGroup = CGroup.create(comp, 2, Messages.WebGalleryEditDialog_Colors);
		bgButton = new WebColorGroup(colorGroup, Messages.WebGalleryEditDialog_background_color);
		shadeButton = new WebColorGroup(colorGroup, Messages.WebGalleryEditDialog_shaded_area_color);
		borderButton = new WebColorGroup(colorGroup, Messages.WebGalleryEditDialog_border_color);
		linkButton = new WebColorGroup(colorGroup, Messages.WebGalleryEditDialog_link_color);
		final Label thumbsizeLabel = new Label(comp, SWT.NONE);
		thumbsizeLabel.setText(Messages.WebGalleryEditDialog_thumbnail_size);
		unsupportedFieldMap.put("thumbnailsize", thumbsizeLabel); //$NON-NLS-1$
		thumbsizeField = new Combo(comp, SWT.READ_ONLY);
		thumbsizeLabel.setData("control", thumbsizeField); //$NON-NLS-1$
		thumbsizeField.setItems(THUMBSIZES);
		final Label opacityLabel = new Label(comp, SWT.NONE);
		opacityLabel.setText(Messages.WebGalleryEditDialog_opacity);
		unsupportedFieldMap.put(OPACITY, opacityLabel);
		opacityField = new Scale(comp, SWT.HORIZONTAL);
		opacityLabel.setData("control", opacityField); //$NON-NLS-1$
		opacityField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		opacityField.setMaximum(100);
		opacityField.setIncrement(5);
		final Label paddingLabel = new Label(comp, SWT.NONE);
		paddingLabel.setText(Messages.WebGalleryEditDialog_padding);
		unsupportedFieldMap.put(PADDING, paddingLabel);
		paddingField = new NumericControl(comp, SWT.NONE);
		paddingLabel.setData("control", paddingField); //$NON-NLS-1$
		paddingField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		paddingField.setMaximum(20);
		paddingField.setIncrement(1);
		final Label marginsLabel = new Label(comp, SWT.NONE);
		marginsLabel.setText(Messages.WebGalleryEditDialog_margins);
		unsupportedFieldMap.put(MARGINS, marginsLabel);
		Composite marginsGroup = new Composite(comp, SWT.NONE);
		marginsLabel.setData("control", marginsGroup); //$NON-NLS-1$
		GridLayout layout = new GridLayout(8, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		marginsGroup.setLayout(layout);
		marginsTopField = createMarginsField(marginsGroup, Messages.WebGalleryEditDialog_top);
		marginsRightField = createMarginsField(marginsGroup, Messages.WebGalleryEditDialog_right);
		marginsBottomField = createMarginsField(marginsGroup, Messages.WebGalleryEditDialog_bottom);
		marginsLeftField = createMarginsField(marginsGroup, Messages.WebGalleryEditDialog_left);
		CGroup fontGroup = CGroup.create(comp, 2, Messages.WebGalleryEditDialog_Fonts);
		titleButton = new WebFontGroup(fontGroup, Messages.WebGalleryEditDialog_title_font);
		sectionButton = new WebFontGroup(fontGroup, Messages.WebGalleryEditDialog_section_font);
		Label sectionLabel = sectionButton.getLabel();
		sectionLabel.setData("control", sectionButton); //$NON-NLS-1$
		unsupportedFieldMap.put("sectionfont", sectionLabel); //$NON-NLS-1$
		captionButton = new WebFontGroup(fontGroup, Messages.WebGalleryEditDialog_caption_font);
		Label captionLabel = captionButton.getLabel();
		captionLabel.setData("control", captionButton); //$NON-NLS-1$
		unsupportedFieldMap.put("captionfont", captionLabel); //$NON-NLS-1$
		navigationButton = new WebFontGroup(fontGroup, Messages.WebGalleryEditDialog_navigation_controls);
		Label navigationLabel = navigationButton.getLabel();
		navigationLabel.setData("control", navigationButton); //$NON-NLS-1$
		unsupportedFieldMap.put("navfont", navigationLabel); //$NON-NLS-1$
		descriptionButton = new WebFontGroup(fontGroup, Messages.WebGalleryEditDialog_description_font);
		Label descriptionLabel = descriptionButton.getLabel();
		descriptionLabel.setData("control", descriptionButton); //$NON-NLS-1$
		unsupportedFieldMap.put("descriptionfont", descriptionLabel); //$NON-NLS-1$
		footerButton = new WebFontGroup(fontGroup, Messages.WebGalleryEditDialog_footer_font);
	}

	private static NumericControl createMarginsField(Composite marginsGroup, String lab) {
		new Label(marginsGroup, SWT.NONE).setText(lab);
		NumericControl marginTopField = new NumericControl(marginsGroup, SWT.NONE);
		marginTopField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		marginTopField.setMaximum(200);
		marginTopField.setIncrement(1);
		return marginTopField;
	}

	private FileEditor createImageGroup(Composite comp, final String dialogTitle) {
		FileEditor imageEditor = new FileEditor(comp, SWT.OPEN, dialogTitle, false,
				new String[] { "*.gif;*.GIF;*.jpg;*.JPG;*.png;*.PNG" }, //$NON-NLS-1$
				new String[] { Messages.WebGalleryEditDialog_valid_image_files }, null, null, false, settings);
		imageEditor.addModifyListener(modifyListener);
		return imageEditor;
	}

	private void createOutputGroup(Composite parent) {
		parent.setLayout(new GridLayout());
		outputTargetGroup = new OutputTargetGroup(parent, new GridData(GridData.FILL, GridData.BEGINNING, true, false),
				modifyListener, false, true);
		qualityGroup = new QualityGroup(parent, false);
	}

	private void createEngineGroup(Composite comp) {
		comp.setLayout(new GridLayout(2, false));
		CGroup group = new CGroup(comp, SWT.NONE);
		group.setText(Messages.WebGalleryEditDialog_engine);
		group.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
		group.setLayout(new GridLayout());
		engineViewer = new ComboViewer(group, SWT.READ_ONLY);
		engineViewer.getControl().setLayoutData(new GridData(150, SWT.DEFAULT));
		engineViewer.setContentProvider(ArrayContentProvider.getInstance());
		engineViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IConfigurationElement)
					return ((IConfigurationElement) element).getAttribute("name"); //$NON-NLS-1$
				return super.getText(element);
			}
		});
		engineViewer.setComparator(ZViewerComparator.INSTANCE);
		engineViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateParms();
				fillPreview();
			}
		});
		parmComp = new Composite(comp, SWT.NONE);
		parmComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		parmComp.setLayout(new StackLayout());
		emptyComp = new Composite(parmComp, SWT.NONE);
		for (IConfigurationElement element : generators) {
			Composite genComp = new Composite(parmComp, SWT.NONE);
			genComp.setLayout(new GridLayout(2, false));
			String ns = element.getNamespaceIdentifier();
			compMap.put(element.getAttribute("id"), genComp); //$NON-NLS-1$
			Composite lastParent = genComp;
			for (IConfigurationElement child : element.getChildren()) {
				if ("group".equals(child.getName())) { //$NON-NLS-1$
					group = new CGroup(genComp, SWT.NONE);
					group.setText(child.getAttribute("name")); //$NON-NLS-1$
					group.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
					group.setLayout(new GridLayout(2, false));
					lastParent = group;
				} else
					createParmControl(lastParent, ns, child);
			}
		}
	}

	protected Object fillParmValue(IConfigurationElement conf, String selector, Map<String, WebParameter> parameters) {
		String key = computeKey(conf.getNamespaceIdentifier(), conf.getAttribute("id")); //$NON-NLS-1$
		Object control = controlMap.get(key);
		if (control instanceof Control)
			((Control) control).setData("selector", selector); //$NON-NLS-1$
		WebParameter parm = parameters.get(key);
		boolean encodeForHtml = Boolean.parseBoolean(conf.getAttribute("encodeForHtml")); //$NON-NLS-1$
		if (parm == null) {
			String dflt = conf.getAttribute("default"); //$NON-NLS-1$
			if (dflt != null)
				parm = new WebParameterImpl(key, dflt, encodeForHtml, conf.getAttribute("linkTo")); //$NON-NLS-1$ );
		}
		if (parm != null) {
			parm.setEncodeHtml(encodeForHtml);
			Object value = parm.getValue();
			if (selector != null) {
				String prefix = selector + ':';
				StringTokenizer st = new StringTokenizer(value.toString(), "\f"); //$NON-NLS-1$
				value = ""; //$NON-NLS-1$
				while (st.hasMoreTokens()) {
					String token = st.nextToken();
					if (token.startsWith(prefix)) {
						value = token.substring(prefix.length());
						break;
					}
				}
			}
			if (control instanceof CheckboxButton) {
				if (value instanceof String)
					value = Boolean.parseBoolean(value.toString());
				((CheckboxButton) control).setSelection((Boolean) value);
			} else if (control instanceof Text)
				((Text) control).setText((String) value);
			else if (control instanceof RadioButtonGroup) {
				RadioButtonGroup radioGroup = (RadioButtonGroup) control;
				String[] ids = (String[]) radioGroup.getData("map"); //$NON-NLS-1$
				for (int i = 0; i < ids.length; i++) {
					if (ids[i].equals(value)) {
						radioGroup.setSelection(i);
						break;
					}
				}
			} else if (control instanceof Combo) {
				Combo combo = (Combo) control;
				@SuppressWarnings("unchecked")
				Map<String, String> comboMap = (Map<String, String>) combo.getData("map"); //$NON-NLS-1$
				for (int i = 0; i < combo.getItemCount(); i++) {
					String id = comboMap.get(combo.getItem(i));
					if (id.equals(value)) {
						combo.select(i);
						break;
					}
				}
			} else if (control instanceof NumericControl) {
				NumericControl spinner = (NumericControl) control;
				if (value instanceof Integer)
					spinner.setSelection((Integer) value);
				else if (value instanceof Double)
					spinner.setSelection((int) (((Double) value) * Math.pow(10, spinner.getDigits()) + 0.5d));
			} else if (control instanceof WebColorGroup && value instanceof Rgb_type)
				((WebColorGroup) control).setRGB((Rgb_type) value);
		}
		return control;
	}

	@SuppressWarnings("unchecked")
	protected WebParameter saveParameterValue(IConfigurationElement conf, Map<String, WebParameter> parameters) {
		String key = computeKey(conf.getNamespaceIdentifier(), conf.getAttribute("id")); //$NON-NLS-1$
		boolean encodeHtml = Boolean.parseBoolean(conf.getAttribute("encodeForHtml")); //$NON-NLS-1$
		Object control = controlMap.get(key);
		Object newValue = null;
		WebParameter newParm = null;
		if (control instanceof CheckboxButton)
			newValue = ((CheckboxButton) control).getSelection();
		else if (control instanceof Text) {
			String text = ((Text) control).getText().trim();
			if (!text.isEmpty())
				newValue = text;
		} else if (control instanceof RadioButtonGroup) {
			int selection = ((RadioButtonGroup) control).getSelection();
			if (selection >= 0)
				newValue = ((String[]) ((RadioButtonGroup) control).getData("map"))[selection]; //$NON-NLS-1$
		} else if (control instanceof Combo) {
			String text = ((Combo) control).getText().trim();
			if (!text.isEmpty())
				newValue = ((Map<String, String>) ((Combo) control).getData("map")).get(text); //$NON-NLS-1$
		} else if (control instanceof NumericControl) {
			NumericControl spinner = (NumericControl) control;
			if ("int".equals(conf.getAttribute("type"))) //$NON-NLS-1$ //$NON-NLS-2$
				newValue = spinner.getSelection();
			else
				newValue = spinner.getSelection() * Math.pow(10, -spinner.getDigits());
		} else if (control instanceof WebColorGroup)
			newValue = ((WebColorGroup) control).getRGB();
		if (newValue != null) {
			String selector = control instanceof Control ? (String) ((Control) control).getData("selector") : null; //$NON-NLS-1$
			if (selector != null) {
				String prefix = selector + ':';
				WebParameter oldParm = parameters.get(key);
				Object oldValue = (oldParm != null) ? oldParm.getValue() : ""; //$NON-NLS-1$
				StringBuilder sb = new StringBuilder();
				StringTokenizer st = new StringTokenizer(oldValue.toString(), "\f"); //$NON-NLS-1$
				boolean replaced = false;
				while (st.hasMoreTokens()) {
					if (sb.length() > 0)
						sb.append('\f');
					String token = st.nextToken();
					if (token.startsWith(prefix)) {
						sb.append(prefix).append(newValue);
						replaced = true;
					} else
						sb.append(token);
				}
				if (!replaced) {
					if (sb.length() > 0)
						sb.append('\f');
					sb.append(prefix).append(newValue);
				}
				newValue = sb.toString();
			}
			parameters.put(key, newParm = new WebParameterImpl(key, newValue, encodeHtml, conf.getAttribute("linkTo"))); //$NON-NLS-1$
		}
		return newParm;
	}

	protected void updateParms() {
		StackLayout layout = (StackLayout) parmComp.getLayout();
		IStructuredSelection selection = (IStructuredSelection) engineViewer.getSelection();
		Object first = selection.getFirstElement();
		if (first instanceof IConfigurationElement) {
			selectedEngine = (IConfigurationElement) first;
			Composite composite = compMap.get(selectedEngine.getAttribute("id")); //$NON-NLS-1$
			layout.topControl = (composite != null) ? composite : emptyComp;
		} else
			layout.topControl = emptyComp;
		parmComp.layout();
		updateLabels();
	}

	private void updateLabels() {
		for (Control control : unsupportedFieldMap.values()) {
			control.setVisible(true);
			Object c = control.getData("control"); //$NON-NLS-1$
			if (c instanceof Control) {
				((Control) c).setVisible(true);
				Object button = ((Control) c).getData("button"); //$NON-NLS-1$
				if (button instanceof Button) {
					((Button) button).setVisible(true);
					Object buttonLabel = ((Button) button).getData(UiConstants.LABEL); 
					if (buttonLabel instanceof Label)
						((Label) buttonLabel).setVisible(true);
				}
			} else if (c instanceof WebFontGroup)
				((WebFontGroup) c).setVisible(true);
		}
		if (selectedEngine != null) {
			String unsupported = selectedEngine.getAttribute("unsupported"); //$NON-NLS-1$
			if (unsupported != null) {
				StringTokenizer st = new StringTokenizer(unsupported);
				while (st.hasMoreTokens()) {
					String name = st.nextToken();
					Control control = unsupportedFieldMap.get(name);
					if (control != null) {
						control.setVisible(false);
						Object c = control.getData("control"); //$NON-NLS-1$
						if (c instanceof Control) {
							((Control) c).setVisible(false);
							Object button = ((Control) c).getData("button"); //$NON-NLS-1$
							if (button instanceof Button) {
								((Button) button).setVisible(false);
								Object buttonLabel = ((Button) button).getData(UiConstants.LABEL);
								if (buttonLabel instanceof Label)
									((Label) buttonLabel).setVisible(false);
							}
						} else if (c instanceof WebFontGroup)
							((WebFontGroup) c).setVisible(false);
					}
				}
			}
		}
	}

	private void createParmControl(Composite genComp, String ns, IConfigurationElement parmConf) {
		String label = parmConf.getAttribute("label"); //$NON-NLS-1$
		String id = parmConf.getAttribute("id"); //$NON-NLS-1$
		String type = parmConf.getAttribute("type"); //$NON-NLS-1$
		String dflt = parmConf.getAttribute("default"); //$NON-NLS-1$
		String max = parmConf.getAttribute("max"); //$NON-NLS-1$
		String min = parmConf.getAttribute("min"); //$NON-NLS-1$
		String description = parmConf.getAttribute("description"); //$NON-NLS-1$
		String linkTo = parmConf.getAttribute("linkTo"); //$NON-NLS-1$
		Object control = null;
		Label lab = null;
		if ("boolean".equals(type)) { //$NON-NLS-1$
			CheckboxButton button = WidgetFactory.createCheckButton(genComp, label,
					new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			button.setSelection("true".equals(dflt)); //$NON-NLS-1$
			button.addSelectionListener(selectionListener);
			control = button;
		} else if ("color".equals(type)) { //$NON-NLS-1$
			WebColorGroup colorGroup = new WebColorGroup(genComp, label);
			if (dflt != null && !dflt.isEmpty()) {
				StringTokenizer st = new StringTokenizer(dflt);
				int i = 0;
				int[] rgb = new int[3];
				while (st.hasMoreTokens() && i <= 2) {
					try {
						rgb[i++] = Integer.parseInt(st.nextToken());
					} catch (NumberFormatException e) {
						++i;
					}
				}
				colorGroup.setRGB(new Rgb_typeImpl(rgb[0], rgb[1], rgb[2]));
			}
			if (description != null)
				colorGroup.setToolTipText(description);
			colorGroup.addSelectionListener(selectionListener);
			control = colorGroup;
		} else {
			lab = new Label(genComp, SWT.NONE);
			lab.setText(label);
			if (description != null)
				lab.setToolTipText(description);
			if ("string".equals(type)) { //$NON-NLS-1$
				IConfigurationElement[] enums = parmConf.getChildren("enumeration"); //$NON-NLS-1$
				if (enums == null || enums.length == 0) {
					Text textField = new Text(genComp, SWT.SINGLE | SWT.BORDER);
					textField.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
					if (dflt != null)
						textField.setText(dflt);
					textField.addModifyListener(modifyListener);
					control = textField;
				} else if (enums.length <= 3) {
					String[] labels = new String[enums.length];
					String[] ids = new String[enums.length];
					int selection = -1;
					for (int i = 0; i < enums.length; i++) {
						IConfigurationElement en = enums[i];
						labels[i] = en.getAttribute("label"); //$NON-NLS-1$
						ids[i] = en.getAttribute("id"); //$NON-NLS-1$
						if (ids[i].equals(dflt))
							selection = i;
					}
					RadioButtonGroup radioGroup = new RadioButtonGroup(genComp, null, SWT.HORIZONTAL, labels);
					radioGroup.setData("map", ids); //$NON-NLS-1$
					if (selection >= 0)
						radioGroup.setSelection(selection);
					radioGroup.addSelectionListener(selectionListener);
					control = radioGroup;
				} else {
					Combo combo = new Combo(genComp, SWT.READ_ONLY);
					Map<String, String> comboMap = new HashMap<String, String>();
					int index = -1;
					int i = 0;
					for (IConfigurationElement en : enums) {
						String vlab = en.getAttribute("label"); //$NON-NLS-1$
						combo.add(vlab);
						String idv = en.getAttribute("id"); //$NON-NLS-1$
						if (idv.equals(dflt))
							index = i;
						comboMap.put(vlab, idv);
						++i;
					}
					combo.setData("map", comboMap); //$NON-NLS-1$
					combo.setVisibleItemCount(8);
					if (index >= 0)
						combo.select(index);
					combo.addModifyListener(modifyListener);
					combo.addSelectionListener(selectionListener);
					control = combo;
				}
			} else if ("text".equals(type)) { //$NON-NLS-1$
				Text textField = new Text(genComp, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
				GridData layoutData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
				layoutData.heightHint = 80;
				textField.setLayoutData(layoutData);
				if (dflt != null)
					textField.setText(dflt);
				textField.addModifyListener(modifyListener);
				control = textField;
			} else if ("int".equals(type)) { //$NON-NLS-1$
				NumericControl spinner = new NumericControl(genComp, SWT.NONE);
				spinner.setLayoutData(new GridData(50, SWT.DEFAULT));
				spinner.setMaximum(Integer.MAX_VALUE);
				if (max != null) {
					try {
						spinner.setMaximum(Integer.parseInt(max));
					} catch (NumberFormatException e) {
						// do nothing
					}
				}
				if (min != null) {
					try {
						spinner.setMinimum(Integer.parseInt(min));
					} catch (NumberFormatException e) {
						// do nothing
					}
				}
				if (dflt != null) {
					try {
						spinner.setSelection(Integer.parseInt(dflt));
					} catch (NumberFormatException e) {
						// do nothing
					}
				}
				spinner.addSelectionListener(selectionListener);
				control = spinner;
			} else if ("double".equals(type)) { //$NON-NLS-1$
				NumericControl spinner = new NumericControl(genComp, SWT.NONE);
				spinner.setLayoutData(new GridData(50, SWT.DEFAULT));
				int digits = 1;
				if (dflt != null) {
					try {
						int p = dflt.indexOf('.');
						if (p < 0)
							p = dflt.indexOf(',');
						if (p >= 0)
							digits = dflt.length() - p - 1;
						spinner.setSelection((int) (Double.parseDouble(dflt) * Math.pow(10, digits)));
					} catch (NumberFormatException e) {
						// do nothing
					}
				}
				spinner.setMaximum(Integer.MAX_VALUE);
				if (max != null) {
					try {
						spinner.setMaximum((int) (Integer.parseInt(max) * Math.pow(10, digits)));
					} catch (NumberFormatException e) {
						// do nothing
					}
				}
				if (min != null) {
					try {
						spinner.setMinimum((int) (Integer.parseInt(min) * Math.pow(10, digits)));
					} catch (NumberFormatException e) {
						// do nothing
					}
				}
				spinner.setDigits(digits);
				spinner.addSelectionListener(selectionListener);
				control = spinner;
			}
		}
		if (control != null) {
			if (description != null) {
				if (control instanceof Control)
					((Control) control).setToolTipText(description);
				if (lab != null)
					lab.setToolTipText(description);
			}
			controlMap.put(computeKey(ns, id), control);
			if (linkTo != null)
				linkMap.put(parmConf, computeKey(ns, linkTo));
		}
	}

	private static String computeKey(String ns, String id) {
		return ns + '.' + id;
	}

	private void updateButtons() {
		repeatBgButton.setEnabled(!imageField.getText().isEmpty());
		Button okButton = getButton(IDialogConstants.OK_ID);
		if (okButton != null) {
			boolean enabled = validate() && !readonly;
			getShell().setModified(enabled);
			okButton.setEnabled(enabled);
		}
		int selectionIndex = tabFolder.getSelectionIndex();
		getButton(PREVIOUS).setEnabled(selectionIndex > 0);
		getButton(NEXT).setEnabled(selectionIndex < tabFolder.getTabList().length - 1);
	}

	private boolean validate() {
		String name = nameField.getText();
		if (name.isEmpty()) {
			setErrorMessage(Messages.WebGalleryEditDialog_please_specify_web_gallery_name);
			return false;
		}
		List<WebGalleryImpl> set = dbManager.obtainObjects(WebGalleryImpl.class, "name", name, QueryField.EQUALS); //$NON-NLS-1$
		for (WebGalleryImpl obj : set)
			if (obj != current) {
				setErrorMessage(Messages.WebGalleryEditDialog_name_already_exists);
				return false;
			}
		if (promptForEngine) {
			if (selectedEngine == null) {
				setErrorMessage(Messages.WebGalleryEditDialog_please_specify_web_engine);
				return false;
			}
			String msg = outputTargetGroup.validate();
			if (msg != null) {
				setErrorMessage(msg);
				return false;
			}
		}
		if (!logoField.getText().isEmpty()) {
			File logoFile = new File(logoField.getText());
			if (!logoFile.exists()) {
				setErrorMessage(Messages.WebGalleryEditDialog_nameplate_image_does_not_exist);
				return false;
			}
		}
		if (!imageField.getText().isEmpty()) {
			File imageFile = new File(imageField.getText());
			if (!imageFile.exists()) {
				setErrorMessage(Messages.WebGalleryEditDialog_background_image_does_not_exist);
				return false;
			}
		}
		if (selectedEngine != null) {
			String ns = selectedEngine.getNamespaceIdentifier();
			for (IConfigurationElement child : selectedEngine.getChildren())
				if ("true".equals(child.getAttribute("required"))) { //$NON-NLS-1$ //$NON-NLS-2$
					Object control = controlMap.get(computeKey(ns, child.getAttribute("id"))); //$NON-NLS-1$
					String label = child.getAttribute("label"); //$NON-NLS-1$
					if (control instanceof Text) {
						if (((Text) control).getText().trim().isEmpty()) {
							setErrorMessage(NLS.bind(Messages.WebGalleryEditDialog_please_specify_value, label));
							return false;
						}
					} else if (control instanceof Combo && ((Combo) control).getText().trim().isEmpty()) {
						setErrorMessage(NLS.bind(Messages.WebGalleryEditDialog_please_specify_value, label));
						return false;
					}
				}
		}
		setErrorMessage(null);
		return true;
	}

	@Override
	protected void okPressed() {
		BusyIndicator.showWhile(getShell().getDisplay(), () -> {
			// Must create a new instance if operation cannot be undone
			List<Object> toBeStored = new ArrayList<>(50);
			List<Object> toBeDeleted = new ArrayList<>(50);
			if (canUndo)
				result = current;
			else {
				result = new WebGalleryImpl();
				if (current != null) {
					result.setStoryboard(current.getStoryboard());
					for (Storyboard storyboard : result.getStoryboard())
						storyboard.setWebGallery_storyboard_parent(result);
					result.setGroup_webGallery_parent(current.getGroup_webGallery_parent());
					for (WebParameter parm : current.getParameter().values())
						toBeDeleted.add(parm);
					toBeDeleted.add(current);
				}
			}
			result.setName(nameField.getText().trim());
			result.setHtmlDescription(descriptionGroup.isHtml());
			result.setDescription(descriptionGroup.getText());
			String webLink = linkField.getText().trim();
			if (webLink.isEmpty())
				webLink = "index.html"; //$NON-NLS-1$
			result.setPageName(webLink);
			StringTokenizer st = new StringTokenizer(keywordField.getText().trim(), "\n"); //$NON-NLS-1$
			List<String> keywords = new ArrayList<String>();
			while (st.hasMoreTokens())
				keywords.add(st.nextToken());
			result.setKeyword(keywords.toArray(new String[keywords.size()]));
			result.setDownloadText(downloadField.getText().trim());
			result.setHideDownload(downloadButton.getSelection());
			result.setHideHeader(hideHeaderButton.getSelection());
			result.setHideFooter(hideFooterButton.getSelection());
			result.setPoweredByText(poweredByField.getText().trim());
			String logo = logoField.getText().trim();
			result.setLogo(logo.isEmpty() ? null : logo);
			String image = imageField.getText().trim();
			result.setBgImage(image.isEmpty() ? null : image);
			result.setBgRepeat(repeatBgButton.getSelection());
			result.setCopyright(copyrightField.getText().trim());
			result.setContactName(contactField.getText().trim());
			result.setEmail(emailField.getText().trim());
			result.setWebUrl(weburlField.getText().trim());
			result.setAddWatermark(watermarkButton.getSelection());
			result.setXmpFilter(getXmpFilter());
			result.setBgColor(bgButton.getRGB());
			result.setShadeColor(shadeButton.getRGB());
			result.setBorderColor(borderButton.getRGB());
			result.setLinkColor(linkButton.getRGB());
			result.setOpacity(opacityField.getSelection());
			result.setThumbSize(thumbsizeField.getSelectionIndex());
			result.setPadding(paddingField.getSelection());
			result.setMargins(new int[] { marginsTopField.getSelection(), marginsRightField.getSelection(),
					marginsBottomField.getSelection(), marginsLeftField.getSelection() });
			result.setTitleFont(titleButton.getFont());
			result.setSectionFont(sectionButton.getFont());
			result.setCaptionFont(captionButton.getFont());
			result.setControlsFont(navigationButton.getFont());
			result.setDescriptionFont(descriptionButton.getFont());
			result.setFooterFont(footerButton.getFont());
			result.setTopHtml(topViewer.getDocument().get());
			result.setHeadHtml(headViewer.getDocument().get());
			result.setFooterHtml(footerViewer.getDocument().get());
			result.setShowMeta(showMetaButton.getSelection());
			result.setApplySharpening(qualityGroup.getApplySharpening());
			result.setRadius(qualityGroup.getRadius());
			result.setAmount(qualityGroup.getAmount());
			result.setThreshold(qualityGroup.getThreshold());
			result.setOutputFolder(outputTargetGroup.getLocalFolder());
			FtpAccount ftpDir = outputTargetGroup.getFtpDir();
			result.setFtpDir(ftpDir != null ? ftpDir.getName() : null);
			result.setIsFtp(outputTargetGroup.getTarget() == Constants.FTP);
			if (selectedEngine != null)
				result.setSelectedEngine(selectedEngine.getAttribute("id")); //$NON-NLS-1$
			Map<String, WebParameter> parameters = new HashMap<String, WebParameter>();
			for (IConfigurationElement element : generators)
				for (IConfigurationElement child : element.getChildren()) {
					if ("group".equals(child.getName())) //$NON-NLS-1$
						continue;
					WebParameter newParm = saveParameterValue(child, parameters);
					if (newParm != null)
						toBeStored.add(newParm);
				}
			result.setParameter(parameters);
			if (!canUndo) {
				if (importGroup != null)
					importIntoGallery(result, importGroup.getFromItem());
				if (group == null) {
					String groupId = (current != null) ? current.getGroup_webGallery_parent()
							: Constants.GROUP_ID_WEBGALLERY;
					if (groupId == null)
						groupId = Constants.GROUP_ID_WEBGALLERY;
					group = dbManager.obtainById(GroupImpl.class, groupId);
				}
				if (group == null) {
					group = new GroupImpl(Messages.WebGalleryEditDialog_web_galleries, false, Constants.INHERIT_LABEL,
							null, 0, null);
					group.setStringId(Constants.GROUP_ID_WEBGALLERY);
				}
				if (current != null)
					group.removeWebGallery(current.getStringId());
				group.addWebGallery(result.getStringId());
				result.setGroup_webGallery_parent(group.getStringId());
				toBeStored.add(group);
				toBeStored.addAll(result.getStoryboard());
				toBeStored.add(result);
			}
			dbManager.safeTransaction(toBeDeleted, toBeStored);
			saveSettings(result);
		});
		super.okPressed();
	}

	private void importIntoGallery(WebGalleryImpl gallery, IIdentifiableObject obj) {
		if (obj instanceof SlideShowImpl) {
			boolean downloadable = gallery.getDownloadText() != null && !gallery.getDownloadText().isEmpty();
			boolean includeMetadata = gallery.getXmpFilter() != null && gallery.getXmpFilter().length > 0;
			int seqNo = 0;
			int index = 0;
			StoryboardImpl newStoryboard = null;
			for (String slideId : ((SlideShowImpl) obj).getEntry()) {
				SlideImpl slide = dbManager.obtainById(SlideImpl.class, slideId);
				if (slide != null) {
					String assetId = slide.getAsset();
					if (assetId == null) {
						if (newStoryboard != null) {
							dbManager.store(newStoryboard);
							gallery.addStoryboard(newStoryboard);
						}
						newStoryboard = new StoryboardImpl("", ++seqNo, false, null, //$NON-NLS-1$
								0, false, true, true, true);
						newStoryboard.setTitle(slide.getCaption());
						newStoryboard.setDescription(slide.getDescription());
						newStoryboard.setSequenceNo(seqNo);
						index = 0;
					} else {
						if (newStoryboard == null)
							newStoryboard = new StoryboardImpl("", ++seqNo, //$NON-NLS-1$
									false, null, 0, false, true, true, true);
						AssetImpl asset = dbManager.obtainAsset(assetId);
						if (!WebGalleryView.accepts(asset))
							continue;
						WebExhibitImpl newExhibit = new WebExhibitImpl(slide.getCaption(), ++index,
								slide.getDescription(), false, asset != null ? asset.getName() : null, downloadable,
								includeMetadata, assetId);
						dbManager.store(newExhibit);
						newStoryboard.addExhibit(newExhibit.getStringId());
					}
				}
			}
			if (newStoryboard != null) {
				dbManager.store(newStoryboard);
				gallery.addStoryboard(newStoryboard);
			}
		} else if (obj instanceof ExhibitionImpl) {
			List<Wall> walls = ((ExhibitionImpl) obj).getWall();
			int seqNo = 1;
			boolean downloadable = gallery.getDownloadText() != null && !gallery.getDownloadText().isEmpty();
			boolean includeMetadata = gallery.getXmpFilter() != null && gallery.getXmpFilter().length > 0;
			int sequenceNo = 0;
			for (Wall wall : walls) {
				Storyboard newStoryboard = new StoryboardImpl(wall.getLocation(), seqNo++, false, null, 0, false, true,
						true, true);
				for (String exhibitId : wall.getExhibit()) {
					ExhibitImpl exhibit = dbManager.obtainById(ExhibitImpl.class, exhibitId);
					if (exhibit != null) {
						String assetId = exhibit.getAsset();
						AssetImpl asset = dbManager.obtainAsset(assetId);
						if (!WebGalleryView.accepts(asset))
							continue;
						WebExhibitImpl newExhibit = new WebExhibitImpl(exhibit.getTitle(), ++sequenceNo,
								exhibit.getDescription(), false, asset != null ? asset.getName() : null, downloadable,
								includeMetadata, assetId);
						dbManager.store(newExhibit);
						newStoryboard.addExhibit(newExhibit.getStringId());
					}
				}
				dbManager.store(newStoryboard);
				gallery.addStoryboard(newStoryboard);
			}
		} else if (obj instanceof WebGalleryImpl) {
			for (Storyboard storyboard : ((WebGalleryImpl) obj).getStoryboard()) {
				Storyboard newStoryboard = new StoryboardImpl(storyboard.getTitle(), storyboard.getSequenceNo(),
						storyboard.getHtmlDescription(), storyboard.getDescription(), storyboard.getImageSize(),
						storyboard.getEnlargeSmall(), storyboard.getShowCaptions(), storyboard.getShowDescriptions(),
						storyboard.getShowExif());
				for (String exhibitId : storyboard.getExhibit()) {
					WebExhibitImpl exhibit = dbManager.obtainById(WebExhibitImpl.class, exhibitId);
					if (exhibit != null) {
						WebExhibitImpl newExhibit = new WebExhibitImpl(exhibit.getCaption(), exhibit.getSequenceNo(),
								exhibit.getDescription(), exhibit.getHtmlDescription(), exhibit.getAltText(),
								exhibit.getDownloadable(), exhibit.getIncludeMetadata(), exhibit.getAsset());
						dbManager.store(newExhibit);
						newStoryboard.addExhibit(newExhibit.getStringId());
					}
				}
				dbManager.store(newStoryboard);
				gallery.addStoryboard(newStoryboard);
			}
		} else if (obj instanceof SmartCollectionImpl) {
			int seqNo = 1;
			SmartCollection sm = (SmartCollection) obj;
			List<Asset> assets = dbManager.createCollectionProcessor(sm).select(true);
			StoryboardImpl newStoryboard = new StoryboardImpl("", 1, false, null, //$NON-NLS-1$
					0, false, true, true, true);
			newStoryboard.setTitle(sm.getName());
			newStoryboard.setDescription(sm.getDescription());
			for (Asset asset : assets) {
				if (!WebGalleryView.accepts(asset))
					continue;
				WebExhibitImpl newExhibit = new WebExhibitImpl(UiUtilities.createSlideTitle(asset), ++seqNo, "", //$NON-NLS-1$
						false, asset.getName(), true, true, asset.getStringId());
				dbManager.store(newExhibit);
				newStoryboard.addExhibit(newExhibit.getStringId());
			}
			dbManager.store(newStoryboard);
			gallery.addStoryboard(newStoryboard);
		}
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, PREVIOUS, Messages.EditMetaDialog_previous, false);
		createButton(parent, NEXT, Messages.EditMetaDialog_next, true);
		createButton(parent, LOAD, Messages.WebGalleryEditDialog_load_design, false);
		super.createButtonsForButtonBar(parent);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
		case PREVIOUS:
			int selectionIndex = tabFolder.getSelectionIndex();
			tabFolder.setSelection(selectionIndex - 1);
			updateTabItems();
			updateButtons();
			break;
		case NEXT:
			selectionIndex = tabFolder.getSelectionIndex();
			tabFolder.setSelection(selectionIndex + 1);
			updateTabItems();
			updateButtons();
			break;
		case LOAD:
			loadDesign();
			break;
		default:
			super.buttonPressed(buttonId);
		}
	}

	private void loadDesign() {
		DesignSelectionDialog dialog = new DesignSelectionDialog(getShell());
		if (dialog.open() == Window.OK) {
			WebGalleryImpl template = dialog.getResult();
			if (template != null) {
				fillValues(template, true);
				updateButtons();
				updateParms();
			}
		}
	}

	private String[] getXmpFilter() {
		List<String> filter = new ArrayList<String>();
		for (Object object : metaViewer.getCheckedElements())
			if (object instanceof QueryField) {
				QueryField queryField = (QueryField) object;
				String id = queryField.getId();
				if (id != null && !queryField.hasChildren())
					filter.add(id);
			}
		return filter.toArray(new String[filter.size()]);
	}

	public WebGalleryImpl getResult() {
		return result;
	}

	private void selectOutputPage(String msg) {
		setErrorMessage(msg);
		tabFolder.setSelection(4);
	}

}
