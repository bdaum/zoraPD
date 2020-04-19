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
 * (c) 2019 Berthold Daum  
 */
package com.bdaum.zoom.webserver.internals.preferences;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;

import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.cat.model.meta.WatchedFolder;
import com.bdaum.zoom.cat.model.meta.WatchedFolderImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.ITypeFilter;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.hover.WatchedFolderLabelProvider;
import com.bdaum.zoom.ui.internal.html.CssCodeScanner;
import com.bdaum.zoom.ui.internal.html.CssSourceViewer;
import com.bdaum.zoom.ui.internal.html.HtmlSourceViewer;
import com.bdaum.zoom.ui.internal.html.XMLCodeScanner;
import com.bdaum.zoom.ui.internal.operations.ModifyMetaOperation;
import com.bdaum.zoom.ui.internal.views.ZColumnViewerToolTipSupport;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.RadioButtonGroup;
import com.bdaum.zoom.ui.internal.widgets.SharpeningGroup;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.internal.wizards.ImportFileSelectionPage;
import com.bdaum.zoom.ui.internal.wizards.WatchedFolderWizard;
import com.bdaum.zoom.ui.preferences.AbstractPreferencePage;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.bdaum.zoom.ui.widgets.NumericControl;
import com.bdaum.zoom.webserver.PreferenceConstants;
import com.bdaum.zoom.webserver.internals.HelpContextIds;
import com.bdaum.zoom.webserver.internals.WebserverActivator;
import com.bdaum.zoom.webserver.internals.WebserverListener;

import io.nayuki.qrcodegen.QrCode;
import io.nayuki.qrcodegen.QrSegment;

@SuppressWarnings("restriction")
public class WebserverPreferencePage extends AbstractPreferencePage implements WebserverListener, Listener {
	public static final String GENERAL = "general"; //$NON-NLS-1$
	private static final String HTMLSELECTION = "htmlSelection"; //$NON-NLS-1$

	private NumericControl portField, numberField;
	private int oldPort;
	private Text imagesPathField;
	private String oldImagesPath;
	private RadioButtonGroup privacyButtonGroup;
	private String oldExhibitionsPath, oldGalleriesPath, oldSLideshowPath;
	private CheckboxButton imagesButton, exhibitionsButton, galleriesButton, infoButton, fullButton, downloadButton,
			geoButton, orphansButton, automaticField, categoriesField, directoriesField, importsField, locationsField,
			mediaGroupField, personsField, ratingField, timelineField, usedDefField, slideshowsButton,
			allowUploadsButton, metadataButton, rawField, tifField, jpgField, dngField, allField, autoField;
	private CTabItem tabItem0, tabItem1, tabItem2, tabItem3, tabItem4;
	private ComboViewer sourceSelectViewer;
	private Map<String, HtmlSourceViewer> viewerMap = new HashMap<String, HtmlSourceViewer>(9);
	private CssSourceViewer cssViewer;
	private Listener selectionListener;
	private TableViewer watchedFolderViewer;
	private List<WatchedFolder> watchedFolders = new ArrayList<>();
	private ArrayList<WatchedFolder> folderBackup;
	private Composite htmlcomp;
	private StackLayout htmlstack;
	private Composite uploadComp;
	private Button addFolderButton, showLocButton, showDestButton, resetHtmlButton, resetCssButton, startButton,
			stopButton;
	private ITextListener textListener;
	private TableViewer ipViewer;
	private Label ipLabel;
	private Canvas qrCanvas;
	private SharpeningGroup sharpeningGroup;
	private Text slideshowsPathField, slideshowsTitleField, passwordField, pageField, imagesTitleField,
			exhibitionsPathField, exhibitionsTitleField, galleriesTitleField, galleriesPathField;

	public WebserverPreferencePage() {
		setDescription(Messages.WebserverPreferencePage_webserver_description);
	}

	@Override
	public void init(IWorkbench aWorkbench) {
		this.workbench = aWorkbench;
		setPreferenceStore(WebserverActivator.getDefault().getPreferenceStore());
	}

	@Override
	public void applyData(Object data) {
		if (GENERAL.equals(data))
			tabFolder.setSelection(0);
	}

	@Override
	protected void createPageContents(Composite composite) {
		selectionListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				updateFields();
			}
		};
		textListener = new ITextListener() {
			@Override
			public void textChanged(TextEvent event) {
				updateButtons();
			}
		};
		createTabFolder(composite, Messages.WebserverPreferencePage_webserver);
		tabItem0 = UiUtilities.createTabItem(tabFolder, Messages.WebserverPreferencePage_settings,
				Messages.WebserverPreferencePage_settings_tooltip);
		tabItem0.setControl(createGeneralGroup(tabFolder));
		tabItem1 = UiUtilities.createTabItem(tabFolder, Messages.WebserverPreferencePage_content,
				Messages.WebserverPreferencePage_rights_content);
		tabItem1.setControl(createContentGroup(tabFolder));
		tabItem2 = UiUtilities.createTabItem(tabFolder, Messages.WebserverPreferencePage_0,
				Messages.WebserverPreferencePage_uploads);
		tabItem2.setControl(createUploadGroup(tabFolder));
		tabItem3 = UiUtilities.createTabItem(tabFolder, Messages.WebserverPreferencePage_html,
				Messages.WebserverPreferencePage_html_tooltip);
		tabItem3.setControl(createHTMLGroup(tabFolder));
		tabItem4 = UiUtilities.createTabItem(tabFolder, Messages.WebserverPreferencePage_css,
				Messages.WebserverPreferencePage_css_tooltip);
		tabItem4.setControl(createCSSGroup(tabFolder));
		initTabFolder(0);
		fillValues();
	}

	@SuppressWarnings("unused")
	private Control createUploadGroup(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout();
		layout.marginTop = 10;
		composite.setLayout(layout);
		allowUploadsButton = WidgetFactory.createCheckButton(composite, Messages.WebserverPreferencePage_allow_uploads,
				null);
		allowUploadsButton.addListener(SWT.Selection, selectionListener);
		uploadComp = new Composite(composite, SWT.NONE);
		uploadComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		uploadComp.setLayout(new GridLayout());
		CGroup accessGroup = CGroup.create(uploadComp, 1, Messages.WebserverPreferencePage_acess_control);
		new Label(accessGroup, SWT.NONE).setText(Messages.WebserverPreferencePage_password);
		passwordField = new Text(accessGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		passwordField.setLayoutData(new GridData(200, SWT.DEFAULT));
		metadataButton = WidgetFactory.createCheckButton(accessGroup, Messages.WebserverPreferencePage_meta_mod,
				new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1),
				Messages.WebserverPreferencePage_meta_mod_tooltip);
		CGroup importGroup = CGroup.create(uploadComp, 1, Messages.WebserverPreferencePage_import_channel);
		((GridLayout) importGroup.getLayout()).numColumns = 3;
		Label label = new Label(importGroup, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		label.setText(Messages.WebserverPreferencePage_transfer_folder);
		watchedFolderViewer = new TableViewer(importGroup,
				SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		GridData layoutData = new GridData(SWT.FILL, SWT.BEGINNING, true, true, 1, 1);
		layoutData.heightHint = 100;
		watchedFolderViewer.getControl().setLayoutData(layoutData);
		watchedFolderViewer.getTable().setLinesVisible(true);
		watchedFolderViewer.getTable().setHeaderVisible(true);
		TableViewerColumn col0 = createColumn(Messages.WebserverPreferencePage_path, 240);
		col0.setLabelProvider(new WatchedFolderLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof WatchedFolder) {
					String uri = ((WatchedFolder) element).getUri();
					try {
						File file = new File(new URI(uri));
						String path = file.getPath();
						if (!path.endsWith(File.separator))
							path += File.separator;
						if (!file.exists())
							path += Messages.WebserverPreferencePage_offline;
						return path;
					} catch (URISyntaxException e) {
						// ignore
					}
					return uri;
				}
				return null;
			}
		});
		TableViewerColumn col1 = createColumn(Messages.WebserverPreferencePage_7, 80);
		col1.setLabelProvider(new WatchedFolderLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof WatchedFolder) {
					String volume = ((WatchedFolder) element).getVolume();
					return volume == null ? "" : volume; //$NON-NLS-1$
				}
				return null;
			}
		});
		TableViewerColumn col2 = createColumn(Messages.WebserverPreferencePage_volume, 400);
		col2.setLabelProvider(new WatchedFolderLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof WatchedFolder) {
					WatchedFolder wf = (WatchedFolder) element;
					StringBuilder sb = new StringBuilder();
					int skipPolicy = wf.getSkipPolicy();
					if (skipPolicy < 0 && skipPolicy >= ImportFileSelectionPage.SKIPPOLICIES.length)
						skipPolicy = 0;
					sb.append(ImportFileSelectionPage.SKIPPOLICIES[skipPolicy]);
					sb.append(" | ").append(wf.getTargetDir()); //$NON-NLS-1$
					return sb.toString();
				}
				return null;
			}
		});
		watchedFolderViewer.setContentProvider(ArrayContentProvider.getInstance());
		watchedFolderViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateButtons();
				validate();
			}
		});
		ZColumnViewerToolTipSupport.enableFor(watchedFolderViewer);
		Composite buttonComp = new Composite(importGroup, SWT.NONE);
		buttonComp.setLayoutData(new GridData(SWT.END, SWT.FILL, false, true));
		buttonComp.setLayout(new GridLayout());
		addFolderButton = new Button(buttonComp, SWT.PUSH);
		addFolderButton.setText(Messages.WebserverPreferencePage_add);
		addFolderButton.setToolTipText(Messages.WebserverPreferencePage_add_tooltip);
		addFolderButton.addListener(SWT.Selection, this);
		new Label(buttonComp, SWT.SEPARATOR | SWT.HORIZONTAL);
		showLocButton = new Button(buttonComp, SWT.PUSH);
		showLocButton.setText(Messages.WebserverPreferencePage_show_folder);
		showLocButton.addListener(SWT.Selection, this);
		showDestButton = new Button(buttonComp, SWT.PUSH);
		showDestButton.setText(Messages.WebserverPreferencePage_show_target);
		showDestButton.addListener(SWT.Selection, this);
		return composite;
	}

	private TableViewerColumn createColumn(String lab, int w) {
		TableViewerColumn col = new TableViewerColumn(watchedFolderViewer, SWT.NONE);
		col.getColumn().setText(lab);
		col.getColumn().setWidth(w);
		return col;
	}

	private Control createCSSGroup(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, false));
		cssViewer = new CssSourceViewer(composite, null,
				SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL, new CssCodeScanner(), null);
		cssViewer.addTextListener(textListener);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.widthHint = 600;
		layoutData.heightHint = 400;
		cssViewer.getControl().setLayoutData(layoutData);
		resetCssButton = new Button(composite, SWT.PUSH);
		resetCssButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		resetCssButton.setText(Messages.WebserverPreferencePage_reset);
		resetCssButton.addListener(SWT.Selection, this);
		return composite;
	}

	private Control createHTMLGroup(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, false));
		sourceSelectViewer = new ComboViewer(composite, SWT.BORDER);
		sourceSelectViewer.getControl().setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false, 2, 1));
		sourceSelectViewer.setContentProvider(ArrayContentProvider.getInstance());
		sourceSelectViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element == PreferenceConstants.INDEX)
					return element + Messages.WebserverPreferencePage_home_page;
				if (element == PreferenceConstants.LIGHTBOX)
					return element + Messages.WebserverPreferencePage_oeverview_page;
				if (element == PreferenceConstants.SLIDESHOW)
					return element + Messages.WebserverPreferencePage_slideshow_page;
				if (element == PreferenceConstants.VIDEO)
					return element + Messages.WebserverPreferencePage_video_player;
				if (element == PreferenceConstants.LOGIN)
					return element + Messages.WebserverPreferencePage_login_page;
				if (element == PreferenceConstants.UPLOADS)
					return element + Messages.WebserverPreferencePage_upload_page;
				if (element == PreferenceConstants.STATUS)
					return element + Messages.WebserverPreferencePage_status_page;
				if (element == PreferenceConstants.ERROR)
					return element + Messages.WebserverPreferencePage_error_page;
				if (element == PreferenceConstants.HELP)
					return element + Messages.WebserverPreferencePage_user_help;
				return element + Messages.WebserverPreferencePage_detail_page;
			}
		});
		sourceSelectViewer.setInput(PreferenceConstants.HTMLTYPES);
		sourceSelectViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateStack(sourceSelectViewer.getStructuredSelection().getFirstElement());
				updateButtons();
			}
		});
		htmlcomp = new Composite(composite, SWT.NONE);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.widthHint = 600;
		layoutData.heightHint = 400;
		htmlcomp.setLayoutData(layoutData);
		htmlcomp.setLayout(htmlstack = new StackLayout());
		for (String type : PreferenceConstants.HTMLTYPES) {
			HtmlSourceViewer sourceViewer = new HtmlSourceViewer(htmlcomp, null,
					SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, new XMLCodeScanner(), null);
			sourceViewer.addTextListener(textListener);
			viewerMap.put(type, sourceViewer);
		}
		resetHtmlButton = new Button(composite, SWT.PUSH);
		resetHtmlButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		resetHtmlButton.setText(Messages.WebserverPreferencePage_reset);
		resetHtmlButton.addListener(SWT.Selection, this);
		return composite;
	}

	protected void updateStack(Object type) {
		htmlstack.topControl = viewerMap.get(type).getControl();
		htmlcomp.layout(true, true);
	}

	private Control createGeneralGroup(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout());
		createStartGroup(composite);
		createHttpGroup(composite);
		createIpGroup(composite);
		return composite;
	}

	private void createIpGroup(Composite comp) {
		CGroup ipGroup = CGroup.create(comp, 1, Messages.WebserverPreferencePage_ip_addresses);
		List<NetworkInterface> interfaces = new ArrayList<NetworkInterface>();
		try {
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			while (networkInterfaces.hasMoreElements()) {
				NetworkInterface networkInterface = networkInterfaces.nextElement();
				if (!networkInterface.isLoopback() && !networkInterface.isVirtual()) {
					Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
					while (inetAddresses.hasMoreElements())
						if (inetAddresses.nextElement().getHostAddress().indexOf(':') < 0) {
							interfaces.add(networkInterface);
							break;
						}
				}
			}
		} catch (SocketException e) {
			// do nothing
		}

		Composite leftGroup = new Composite(ipGroup, SWT.NONE);
		leftGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		leftGroup.setLayout(layout);

		ipViewer = new TableViewer(leftGroup, SWT.FULL_SELECTION | SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
		GridData layoutData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		layoutData.widthHint = 450;
		layoutData.heightHint = 149;
		ipViewer.getControl().setLayoutData(layoutData);
		ipViewer.getTable().setHeaderVisible(true);
		ipViewer.getTable().setLinesVisible(true);
		TableViewerColumn col1 = new TableViewerColumn(ipViewer, SWT.NONE);
		col1.getColumn().setWidth(270);
		col1.getColumn().setText(Messages.IpDialog_name);
		col1.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof NetworkInterface)
					return ((NetworkInterface) element).getDisplayName();
				return super.getText(element);
			}
		});
		TableViewerColumn col2 = new TableViewerColumn(ipViewer, SWT.NONE);
		col2.getColumn().setWidth(100);
		col2.getColumn().setText(Messages.IpDialog_address);
		col2.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Enumeration<InetAddress> inetAddresses = ((NetworkInterface) element).getInetAddresses();
				while (inetAddresses.hasMoreElements()) {
					String hostAddress = inetAddresses.nextElement().getHostAddress();
					if (hostAddress.indexOf(':') < 0)
						return hostAddress;
				}
				return super.getText(element);
			}
		});
		TableViewerColumn col3 = new TableViewerColumn(ipViewer, SWT.NONE);
		col3.getColumn().setWidth(80);
		col3.getColumn().setText(Messages.IpDialog_interface);
		col3.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof NetworkInterface)
					return ((NetworkInterface) element).getName();
				return super.getText(element);
			}
		});
		ipViewer.setContentProvider(ArrayContentProvider.getInstance());
		ipViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateUrlFields();
			}
		});
		ipViewer.setInput(interfaces);
		ipLabel = new Label(leftGroup, SWT.NONE);
		ipLabel.setAlignment(SWT.CENTER);
		ipLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		qrCanvas = new Canvas(ipGroup, SWT.DOUBLE_BUFFERED);
		layoutData = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
		layoutData.heightHint = layoutData.widthHint = 168;
		layoutData.horizontalIndent = 10;
		qrCanvas.setLayoutData(layoutData);
		qrCanvas.addListener(SWT.Paint, this);
		if (!interfaces.isEmpty())
			ipViewer.getTable().select(0);
	}

	@Override
	public void handleEvent(Event e) {
		switch (e.type) {
		case SWT.Paint:
			paintControl(e);
			break;
		case SWT.Selection:
			if (e.widget == resetHtmlButton) {
				String type = (String) sourceSelectViewer.getStructuredSelection().getFirstElement();
				if (type != null) {
					viewerMap.get(type).setDocument(new Document(getPreferenceStore().getDefaultString(type)));
					updateButtons();
				}
			} else if (e.widget == resetCssButton) {
				cssViewer.setDocument(new Document(getPreferenceStore().getDefaultString(PreferenceConstants.CSS)));
				updateButtons();
			} else if (e.widget == showDestButton) {
				WatchedFolder wf = (WatchedFolder) watchedFolderViewer.getStructuredSelection().getFirstElement();
				if (wf != null) {
					String targetDir = wf.getTargetDir();
					if (targetDir != null) {
						File file = new File(targetDir);
						if (file.exists())
							BatchUtilities.showInFolder(file, false);
					}
				}
			} else if (e.widget == showLocButton) {
				WatchedFolder wf = (WatchedFolder) watchedFolderViewer.getStructuredSelection().getFirstElement();
				if (wf != null) {
					URI uri = Core.getCore().getVolumeManager().findFile(wf.getUri(), wf.getVolume());
					if (uri != null) {
						File file = new File(uri);
						if (file.exists())
							BatchUtilities.showInFolder(file, false);
					}
				}
			} else if (e.widget == addFolderButton) {
				WatchedFolder folder = new WatchedFolderImpl(null, null, 0L, false, null, true, null, false, 0, null, 2,
						null, null, Constants.FILESOURCE_UNKNOWN, false);
				WatchedFolderWizard wizard = new WatchedFolderWizard(false, false, false);
				WizardDialog wizardDialog = new WizardDialog(getShell(), wizard);
				wizard.init(null, new StructuredSelection(folder));
				if (wizardDialog.open() == WizardDialog.OK)
					try {
						if (folderBackup == null)
							folderBackup = new ArrayList<WatchedFolder>(watchedFolders);
						folder.setStringId(Utilities.computeWatchedFolderId(new File(new URI(folder.getUri())),
								folder.getVolume()));
						watchedFolders.add(folder);
						watchedFolderViewer.setInput(watchedFolders);
					} catch (URISyntaxException e1) {
						// should never happend
					}
			} else if (e.widget == startButton) {
				startButton.setEnabled(false);
				WebserverActivator.getDefault().startWebserver();
			} else if (e.widget == stopButton) {
				stopButton.setEnabled(false);
				WebserverActivator.getDefault().stopWebserver();
			} else if (e.widget == portField) {
				updateUrlFields();
				updateButtons();
			} else if (e.widget == allField) {
				boolean sel = allField.getSelection();
				rawField.setSelection(sel);
				dngField.setSelection(sel);
				jpgField.setSelection(sel);
				tifField.setSelection(sel);
				validate();
			} else if (e.widget == rawField || e.widget == dngField || e.widget == jpgField || e.widget == tifField) {
				allField.setSelection(rawField.getSelection() && dngField.getSelection() && jpgField.getSelection()
						&& tifField.getSelection());
				validate();
			}
			break;
		case SWT.Modify:
			updateUrlFields();
			validate();
			break;
		}

	}

	private void paintControl(Event e) {
		Rectangle clientArea = qrCanvas.getClientArea();
		GC gc = e.gc;
		gc.setBackground(e.display.getSystemColor(SWT.COLOR_WHITE));
		gc.fillRectangle(clientArea);
		String serverUrl = createServerUrl();
		if (serverUrl != null) {
			List<QrSegment> segs = QrSegment.makeSegments(serverUrl);
			QrCode qr1 = QrCode.encodeSegments(segs, QrCode.Ecc.HIGH, 5, 5, 2, false);
			int w = clientArea.width - 20;
			int size = w / qr1.size;
			int offset = w % qr1.size / 2 + 10;
			gc.setBackground(e.display.getSystemColor(
					WebserverActivator.getDefault().getState() == WebserverActivator.RUNNING ? SWT.COLOR_BLACK
							: SWT.COLOR_GRAY));
			for (int y = 0; y < qr1.size; y++)
				for (int x = 0; x < qr1.size; x++)
					if (qr1.getModule(x, y))
						gc.fillRectangle(offset + x * size, offset + y * size, size, size);
		}
	}

	private String createServerUrl() {
		try {
			NetworkInterface ni = (NetworkInterface) ipViewer.getStructuredSelection().getFirstElement();
			if (ni != null) {
				Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
				while (inetAddresses.hasMoreElements()) {
					String s = inetAddresses.nextElement().getHostAddress();
					if (s.indexOf(':') < 0)
						return NLS.bind("http://{0}:{1}/{2}", //$NON-NLS-1$
								new Object[] { s, portField.getSelection(), pageField.getText() });
				}
			}
		} catch (ClassCastException e) {
			// do nothing
		}
		return null;
	}

	private Control createContentGroup(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout());
		createRightsGroup(composite);
		createPageContentGroup(composite);
		createSharpeningGroup(composite);
		return composite;
	}

	private void createSharpeningGroup(Composite composite) {
		CGroup group = new CGroup(composite, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		group.setText(Messages.WebserverPreferencePage_output_quality);
		group.setLayout(new GridLayout());
		sharpeningGroup = new SharpeningGroup(group);
	}

	private void createRightsGroup(Composite comp) {
		CGroup rightsGroup = CGroup.create(comp, 1, Messages.WebserverPreferencePage_rights);
		new Label(rightsGroup, SWT.WRAP).setText(Messages.WebserverPreferencePage_chapters);
		Composite chapterComp = new Composite(rightsGroup, SWT.NONE);
		chapterComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		GridLayout layout = new GridLayout(4, false);
		layout.marginHeight = layout.marginWidth = 0;
		chapterComp.setLayout(layout);
		imagesButton = WidgetFactory.createCheckButton(chapterComp, Messages.WebserverPreferencePage_images, null,
				Messages.WebserverPreferencePage_images_tooltip);
		imagesButton.addListener(SWT.Selection, selectionListener);
		exhibitionsButton = WidgetFactory.createCheckButton(chapterComp, Messages.WebserverPreferencePage_exhibitions,
				null, Messages.WebserverPreferencePage_exhibitions_tooltip);
		exhibitionsButton.addListener(SWT.Selection, selectionListener);
		galleriesButton = WidgetFactory.createCheckButton(chapterComp, Messages.WebserverPreferencePage_galleries, null,
				Messages.WebserverPreferencePage_galleries_tooltip);
		galleriesButton.addListener(SWT.Selection, selectionListener);
		slideshowsButton = WidgetFactory.createCheckButton(chapterComp, Messages.WebserverPreferencePage_slideshows,
				null, Messages.WebserverPreferencePage_galleries_tooltip);
		slideshowsButton.addListener(SWT.Selection, selectionListener);

		Label label = new Label(rightsGroup, SWT.NONE);
		label.setText(Messages.WebserverPreferencePage_groups);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, true));
		Composite groupsGroup = new Composite(rightsGroup, SWT.NONE);
		groupsGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		layout = new GridLayout(5, false);
		layout.marginHeight = layout.marginWidth = 0;
		groupsGroup.setLayout(layout);
		automaticField = WidgetFactory.createCheckButton(groupsGroup, Messages.WebserverPreferencePage_automatic, null);
		categoriesField = WidgetFactory.createCheckButton(groupsGroup, Messages.WebserverPreferencePage_categories,
				null);
		directoriesField = WidgetFactory.createCheckButton(groupsGroup, Messages.WebserverPreferencePage_directories,
				null);
		importsField = WidgetFactory.createCheckButton(groupsGroup, Messages.WebserverPreferencePage_imports, null);
		locationsField = WidgetFactory.createCheckButton(groupsGroup, Messages.WebserverPreferencePage_locations, null);
		mediaGroupField = WidgetFactory.createCheckButton(groupsGroup, Messages.WebserverPreferencePage_media, null);
		personsField = WidgetFactory.createCheckButton(groupsGroup, Messages.WebserverPreferencePage_persons, null);
		ratingField = WidgetFactory.createCheckButton(groupsGroup, Messages.WebserverPreferencePage_ratings, null);
		timelineField = WidgetFactory.createCheckButton(groupsGroup, Messages.WebserverPreferencePage_timeline, null);
		usedDefField = WidgetFactory.createCheckButton(groupsGroup, Messages.WebserverPreferencePage_userdef, null);
		new Label(rightsGroup, SWT.NONE).setText(Messages.WebserverPreferencePage_actions);
		Composite actionsComp = new Composite(rightsGroup, SWT.NONE);
		actionsComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		layout = new GridLayout(4, true);
		layout.marginHeight = layout.marginWidth = 0;
		actionsComp.setLayout(layout);
		infoButton = WidgetFactory.createCheckButton(actionsComp, Messages.WebserverPreferencePage_metadata, null,
				Messages.WebserverPreferencePage_metadata_tooltip);
		infoButton.addListener(SWT.Selection, selectionListener);
		fullButton = WidgetFactory.createCheckButton(actionsComp, Messages.WebserverPreferencePage_fullsize, null,
				Messages.WebserverPreferencePage_fullsize_tooltip);
		fullButton.addListener(SWT.Selection, selectionListener);
		downloadButton = WidgetFactory.createCheckButton(actionsComp, Messages.WebserverPreferencePage_download, null,
				Messages.WebserverPreferencePage_download_tooltip);
		downloadButton.addListener(SWT.Selection, selectionListener);
		geoButton = WidgetFactory.createCheckButton(actionsComp, Messages.WebserverPreferencePage_geo, null,
				Messages.WebserverPreferencePage_download_tooltip);
		geoButton.addListener(SWT.Selection, selectionListener);
		new Label(rightsGroup, SWT.NONE).setText(Messages.WebserverPreferencePage_privacy);
		privacyButtonGroup = new RadioButtonGroup(rightsGroup, null, SWT.HORIZONTAL,
				Messages.WebserverPreferencePage_public, Messages.WebserverPreferencePage_moderate,
				Messages.WebserverPreferencePage_all);
		privacyButtonGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
	}

	@SuppressWarnings("unused")
	private void createPageContentGroup(Composite comp) {
		CGroup contentGroup = CGroup.create(comp, 1, Messages.WebserverPreferencePage_content);
		new Label(contentGroup, SWT.NONE).setText(Messages.WebserverPreferencePage_no_thumbnails);
		numberField = new NumericControl(contentGroup, SWT.NONE);
		numberField.setMinimum(4);
		numberField.setMaximum(50);
		new Label(contentGroup, SWT.NONE);
		orphansButton = WidgetFactory.createCheckButton(contentGroup, Messages.WebserverPreferencePage_orphans, null,
				Messages.WebserverPreferencePage_orphans_tooltip);
		Label label = new Label(contentGroup, SWT.NONE);
		label.setText(Messages.WebserverPreferencePage_file_types);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, true));
		Composite formatGroup = new Composite(contentGroup, SWT.NONE);
		formatGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		GridLayout layout = new GridLayout(5, false);
		layout.marginHeight = layout.marginWidth = 0;
		formatGroup.setLayout(layout);
		allField = WidgetFactory.createCheckButton(formatGroup, Messages.WebserverPreferencePage_all_types, null);
		allField.addListener(SWT.Selection, this);
		rawField = WidgetFactory.createCheckButton(formatGroup, "RAW", null); //$NON-NLS-1$
		rawField.addListener(SWT.Selection, this);
		dngField = WidgetFactory.createCheckButton(formatGroup, "DNG", null); //$NON-NLS-1$
		dngField.addListener(SWT.Selection, this);
		jpgField = WidgetFactory.createCheckButton(formatGroup, "JPEG", null); //$NON-NLS-1$
		jpgField.addListener(SWT.Selection, this);
		tifField = WidgetFactory.createCheckButton(formatGroup, "TIFF", null); //$NON-NLS-1$
		tifField.addListener(SWT.Selection, this);
		new Label(contentGroup, SWT.NONE).setText(Messages.WebserverPreferencePage_images_title);
		imagesTitleField = new Text(contentGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		imagesTitleField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		imagesTitleField.addListener(SWT.Modify, this);
		new Label(contentGroup, SWT.NONE).setText(Messages.WebserverPreferencePage_exhibitions_title);
		exhibitionsTitleField = new Text(contentGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		exhibitionsTitleField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		exhibitionsTitleField.addListener(SWT.Modify, this);
		new Label(contentGroup, SWT.NONE).setText(Messages.WebserverPreferencePage_web_galleries_title);
		galleriesTitleField = new Text(contentGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		galleriesTitleField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		galleriesTitleField.addListener(SWT.Modify, this);
		new Label(contentGroup, SWT.NONE).setText(Messages.WebserverPreferencePage_slideshows_title);
		slideshowsTitleField = new Text(contentGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		slideshowsTitleField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		slideshowsTitleField.addListener(SWT.Modify, this);
	}

	protected void updateFields() {
		boolean imagesEnabled = imagesButton.getSelection();
		imagesPathField.setEnabled(imagesEnabled);
		imagesTitleField.setEnabled(imagesEnabled);
		orphansButton.setEnabled(imagesEnabled);
		allField.setEnabled(imagesEnabled);
		rawField.setEnabled(imagesEnabled);
		dngField.setEnabled(imagesEnabled);
		jpgField.setEnabled(imagesEnabled);
		tifField.setEnabled(imagesEnabled);
		infoButton.setEnabled(imagesEnabled);
		fullButton.setEnabled(imagesEnabled);
		downloadButton.setEnabled(imagesEnabled);
		geoButton.setEnabled(imagesEnabled);
		automaticField.setEnabled(imagesEnabled);
		categoriesField.setEnabled(imagesEnabled);
		directoriesField.setEnabled(imagesEnabled);
		importsField.setEnabled(imagesEnabled);
		locationsField.setEnabled(imagesEnabled);
		mediaGroupField.setEnabled(imagesEnabled);
		personsField.setEnabled(imagesEnabled);
		ratingField.setEnabled(imagesEnabled);
		timelineField.setEnabled(imagesEnabled);
		usedDefField.setEnabled(imagesEnabled);
		sharpeningGroup.setEnabled(imagesEnabled);
		exhibitionsPathField.setEnabled(exhibitionsButton.getSelection());
		exhibitionsTitleField.setEnabled(exhibitionsButton.getSelection());
		galleriesPathField.setEnabled(galleriesButton.getSelection());
		galleriesTitleField.setEnabled(galleriesButton.getSelection());
		slideshowsPathField.setEnabled(slideshowsButton.getSelection());
		slideshowsTitleField.setEnabled(slideshowsButton.getSelection());
		uploadComp.setVisible(allowUploadsButton.getSelection());
		validate();
	}

	private void createHttpGroup(Composite comp) {
		CGroup httpGroup = CGroup.create(comp, 1, Messages.WebserverPreferencePage_http_settings);
		new Label(httpGroup, SWT.NONE).setText(Messages.WebserverPreferencePage_port);
		portField = new NumericControl(httpGroup, SWT.NONE);
		portField.setMinimum(80);
		portField.setMaximum(49150);
		portField.addListener(SWT.Selection, this);
		new Label(httpGroup, SWT.NONE).setText(Messages.WebserverPreferencePage_startpage);
		pageField = new Text(httpGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		pageField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		pageField.addListener(SWT.Modify, this);
		new Label(httpGroup, SWT.NONE).setText(Messages.WebserverPreferencePage_images_path);
		imagesPathField = new Text(httpGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		imagesPathField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		imagesPathField.addListener(SWT.Modify, this);
		new Label(httpGroup, SWT.NONE).setText(Messages.WebserverPreferencePage_exhibitions_path);
		exhibitionsPathField = new Text(httpGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		exhibitionsPathField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		exhibitionsPathField.addListener(SWT.Modify, this);
		new Label(httpGroup, SWT.NONE).setText(Messages.WebserverPreferencePage_web_galleries_path);
		galleriesPathField = new Text(httpGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		galleriesPathField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		galleriesPathField.addListener(SWT.Modify, this);
		new Label(httpGroup, SWT.NONE).setText(Messages.WebserverPreferencePage_slideshows_path);
		slideshowsPathField = new Text(httpGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		slideshowsPathField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		slideshowsPathField.addListener(SWT.Modify, this);
	}

	private void createStartGroup(Composite comp) {
		WebserverActivator.getDefault().addWebserverListener(this);
		setHelp(HelpContextIds.WEBSERVER_PREFERENCE_PAGE);
		new Label(comp, SWT.WRAP).setText(Messages.WebserverPreferencePage_define_properties);
		CGroup startGroup = new CGroup(comp, SWT.NONE);
		startGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		startGroup.setLayout(new GridLayout(3, false));
		startGroup.setText(Messages.WebserverPreferencePage_start_stop);
		autoField = WidgetFactory.createCheckButton(startGroup, Messages.WebserverPreferencePage_autostart,
				new GridData(SWT.BEGINNING, SWT.CENTER, true, false),
				Messages.WebserverPreferencePage_autostart_tooltip);
		startButton = new Button(startGroup, SWT.PUSH);
		startButton.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
		startButton.setText(Messages.WebserverPreferencePage_start);
		startButton.setEnabled(WebserverActivator.getDefault().getState() == WebserverActivator.STOPPED);
		startButton.addListener(SWT.Selection, this);
		stopButton = new Button(startGroup, SWT.PUSH);
		stopButton.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		stopButton.setText(Messages.WebserverPreferencePage_stop);
		stopButton.setEnabled(WebserverActivator.getDefault().getState() == WebserverActivator.RUNNING);
		stopButton.addListener(SWT.Selection, this);
	}

	public void setPrivacySelection(int rating) {
		switch (rating) {
		case QueryField.SAFETY_SAFE:
			privacyButtonGroup.setSelection(0);
			break;
		case QueryField.SAFETY_MODERATE:
			privacyButtonGroup.setSelection(1);
			break;
		case QueryField.SAFETY_RESTRICTED:
			privacyButtonGroup.setSelection(2);
			break;
		}
	}

	public int getPrivacySelection() {
		switch (privacyButtonGroup.getSelection()) {
		case 0:
			return QueryField.SAFETY_SAFE;
		case 1:
			return QueryField.SAFETY_MODERATE;
		default:
			return QueryField.SAFETY_RESTRICTED;
		}
	}

	@Override
	public void stateChanged(int oldstate, int state) {
		if (!startButton.isDisposed())
			startButton.getDisplay().asyncExec(() -> {
				if (!startButton.isDisposed()) {
					startButton.setEnabled(state == WebserverActivator.STOPPED);
					stopButton.setEnabled(state == WebserverActivator.RUNNING);
				}
			});
	}

	@Override
	public void dispose() {
		WebserverActivator.getDefault().removeWebserverListener(this);
		super.dispose();
	}

	@Override
	protected void doFillValues() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		oldPort = preferenceStore.getInt(PreferenceConstants.PORT);
		portField.setSelection(oldPort);
		pageField.setText(preferenceStore.getString(PreferenceConstants.STARTPAGE));
		imagesPathField.setText(oldImagesPath = preferenceStore.getString(PreferenceConstants.IMAGEPATH));
		exhibitionsPathField
				.setText(oldExhibitionsPath = preferenceStore.getString(PreferenceConstants.EXHIBITIONPATH));
		galleriesPathField.setText(oldGalleriesPath = preferenceStore.getString(PreferenceConstants.GALLERYPATH));
		slideshowsPathField.setText(oldSLideshowPath = preferenceStore.getString(PreferenceConstants.SLIDESHOWSPATH));
		imagesTitleField.setText(preferenceStore.getString(PreferenceConstants.IMAGESTITLE));
		exhibitionsTitleField.setText(preferenceStore.getString(PreferenceConstants.EXHIBITIONSTITLE));
		galleriesTitleField.setText(preferenceStore.getString(PreferenceConstants.GALLERIESTITLE));
		slideshowsTitleField.setText(preferenceStore.getString(PreferenceConstants.SLIDESHOWSTITLE));
		autoField.setSelection(preferenceStore.getBoolean(PreferenceConstants.AUTOSTART));
		imagesButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.IMAGES));
		exhibitionsButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.EXHIBITIONS));
		galleriesButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.GALLERIES));
		slideshowsButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.SLIDESHOWS));

		int groups = preferenceStore.getInt(PreferenceConstants.GROUPS);
		automaticField.setSelection((groups & PreferenceConstants.AUTOMATIC) != 0);
		categoriesField.setSelection((groups & PreferenceConstants.CATEGERIES) != 0);
		directoriesField.setSelection((groups & PreferenceConstants.DIRECTORIES) != 0);
		importsField.setSelection((groups & PreferenceConstants.IMPORTS) != 0);
		locationsField.setSelection((groups & PreferenceConstants.LOCATIONS) != 0);
		mediaGroupField.setSelection((groups & PreferenceConstants.MEDIA) != 0);
		personsField.setSelection((groups & PreferenceConstants.PERSONS) != 0);
		ratingField.setSelection((groups & PreferenceConstants.RATINGS) != 0);
		timelineField.setSelection((groups & PreferenceConstants.TIMELINE) != 0);
		usedDefField.setSelection((groups & PreferenceConstants.USER) != 0);

		sharpeningGroup.fillValues(preferenceStore.getBoolean(PreferenceConstants.APPLY_SHARPENING),
				preferenceStore.getFloat(PreferenceConstants.RADIUS),
				preferenceStore.getFloat(PreferenceConstants.AMOUNT),
				preferenceStore.getInt(PreferenceConstants.THRESHOLD));

		infoButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.METADATA));
		fullButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.FULLSIZE));
		downloadButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.DOWNLOAD));
		geoButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.GEO));
		numberField.setSelection(preferenceStore.getInt(PreferenceConstants.THUMBNAILSPERPAGE));
		setPrivacySelection(preferenceStore.getInt(PreferenceConstants.PRIVACY));

		int formats = preferenceStore.getInt(PreferenceConstants.FORMATS);
		allField.setSelection(formats == ITypeFilter.ALLFORMATS);
		rawField.setSelection((formats & ITypeFilter.RAW) != 0);
		dngField.setSelection((formats & ITypeFilter.DNG) != 0);
		jpgField.setSelection((formats & ITypeFilter.JPEG) != 0);
		tifField.setSelection((formats & ITypeFilter.TIFF) != 0);
		orphansButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.ORPHANS));

		boolean allowUploads = preferenceStore.getBoolean(PreferenceConstants.ALLOWUPLOADS);
		allowUploadsButton.setSelection(allowUploads);
		String password = preferenceStore.getString(PreferenceConstants.PASSWORD);
		if (password != null)
			passwordField.setText(password);
		metadataButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.ALLOWMETADATAMOD));
		WatchedFolder wf = null;
		String tid = preferenceStore.getString(PreferenceConstants.TRANSFERFOLDER);
		watchedFolders.clear();
		Meta meta = Core.getCore().getDbManager().getMeta(true);
		if (meta.getWatchedFolder() != null) {
			CoreActivator activator = CoreActivator.getDefault();
			for (String id : meta.getWatchedFolder()) {
				WatchedFolder folder = activator.getObservedFolder(id);
				if (folder != null && folder.getTransfer()) {
					watchedFolders.add(folder);
					if (id.equals(tid))
						wf = folder;
				}
			}
		}
		watchedFolderViewer.setInput(watchedFolders);
		if (wf != null)
			watchedFolderViewer.setSelection(new StructuredSelection(wf));
		stateChanged(WebserverActivator.STOPPED, WebserverActivator.getDefault().getState());
		for (String type : PreferenceConstants.HTMLTYPES)
			viewerMap.get(type).setDocument(new Document(preferenceStore.getString(type)));
		int index = getPreferenceStore().getInt(HTMLSELECTION);
		sourceSelectViewer.getCombo().select(index);
		updateStack(PreferenceConstants.HTMLTYPES[index]);
		cssViewer.setDocument(new Document(preferenceStore.getString(PreferenceConstants.CSS)));
		updateFields();
		updateUrlFields();
		updateButtons();
	}

	protected void updateUrlFields() {
		ipLabel.setText(createServerUrl());
		qrCanvas.redraw();
	}

	@Override
	protected void doPerformDefaults() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(PreferenceConstants.PORT, preferenceStore.getDefaultInt(PreferenceConstants.PORT));
		preferenceStore.setValue(PreferenceConstants.STARTPAGE,
				preferenceStore.getDefaultString(PreferenceConstants.STARTPAGE));
		preferenceStore.setValue(PreferenceConstants.IMAGEPATH,
				preferenceStore.getDefaultInt(PreferenceConstants.IMAGEPATH));
		preferenceStore.setValue(PreferenceConstants.EXHIBITIONPATH,
				preferenceStore.getDefaultInt(PreferenceConstants.EXHIBITIONPATH));
		preferenceStore.setValue(PreferenceConstants.GALLERYPATH,
				preferenceStore.getDefaultInt(PreferenceConstants.GALLERYPATH));
		preferenceStore.setValue(PreferenceConstants.SLIDESHOWSPATH,
				preferenceStore.getDefaultInt(PreferenceConstants.SLIDESHOWSPATH));
		preferenceStore.setValue(PreferenceConstants.IMAGESTITLE,
				preferenceStore.getDefaultInt(PreferenceConstants.IMAGESTITLE));
		preferenceStore.setValue(PreferenceConstants.EXHIBITIONSTITLE,
				preferenceStore.getDefaultInt(PreferenceConstants.EXHIBITIONSTITLE));
		preferenceStore.setValue(PreferenceConstants.GALLERIESTITLE,
				preferenceStore.getDefaultInt(PreferenceConstants.GALLERIESTITLE));
		preferenceStore.setValue(PreferenceConstants.SLIDESHOWSTITLE,
				preferenceStore.getDefaultInt(PreferenceConstants.SLIDESHOWSTITLE));
		preferenceStore.setValue(PreferenceConstants.AUTOSTART,
				preferenceStore.getDefaultBoolean(PreferenceConstants.AUTOSTART));
		preferenceStore.setValue(PreferenceConstants.THUMBNAILSPERPAGE,
				preferenceStore.getDefaultInt(PreferenceConstants.THUMBNAILSPERPAGE));
		preferenceStore.setValue(PreferenceConstants.PRIVACY,
				preferenceStore.getDefaultInt(PreferenceConstants.PRIVACY));
		preferenceStore.setValue(PreferenceConstants.FORMATS,
				preferenceStore.getDefaultInt(PreferenceConstants.FORMATS));
		preferenceStore.setValue(PreferenceConstants.IMAGES,
				preferenceStore.getDefaultBoolean(PreferenceConstants.IMAGES));
		preferenceStore.setValue(PreferenceConstants.EXHIBITIONS,
				preferenceStore.getDefaultBoolean(PreferenceConstants.EXHIBITIONS));
		preferenceStore.setValue(PreferenceConstants.GALLERIES,
				preferenceStore.getDefaultBoolean(PreferenceConstants.GALLERIES));
		preferenceStore.setValue(PreferenceConstants.SLIDESHOWS,
				preferenceStore.getDefaultBoolean(PreferenceConstants.SLIDESHOWS));
		preferenceStore.setValue(PreferenceConstants.METADATA,
				preferenceStore.getDefaultBoolean(PreferenceConstants.METADATA));
		preferenceStore.setValue(PreferenceConstants.FULLSIZE,
				preferenceStore.getDefaultBoolean(PreferenceConstants.FULLSIZE));
		preferenceStore.setValue(PreferenceConstants.DOWNLOAD,
				preferenceStore.getDefaultBoolean(PreferenceConstants.DOWNLOAD));
		preferenceStore.setValue(PreferenceConstants.GEO, preferenceStore.getDefaultBoolean(PreferenceConstants.GEO));
		preferenceStore.setValue(PreferenceConstants.ORPHANS,
				preferenceStore.getDefaultBoolean(PreferenceConstants.ORPHANS));
		preferenceStore.setValue(PreferenceConstants.ALLOWUPLOADS,
				preferenceStore.getDefaultBoolean(PreferenceConstants.ALLOWUPLOADS));
		preferenceStore.setValue(PreferenceConstants.PASSWORD,
				preferenceStore.getDefaultString(PreferenceConstants.PASSWORD));
		preferenceStore.setValue(PreferenceConstants.ALLOWMETADATAMOD,
				preferenceStore.getDefaultBoolean(PreferenceConstants.ALLOWMETADATAMOD));
		preferenceStore.setValue(PreferenceConstants.TRANSFERFOLDER,
				preferenceStore.getDefaultString(PreferenceConstants.TRANSFERFOLDER));
		preferenceStore.setValue(PreferenceConstants.GROUPS, preferenceStore.getDefaultInt(PreferenceConstants.GROUPS));
		for (String type : PreferenceConstants.HTMLTYPES)
			preferenceStore.setValue(type, preferenceStore.getDefaultString(type));
		preferenceStore.setValue(PreferenceConstants.CSS, preferenceStore.getDefaultString(PreferenceConstants.CSS));
		preferenceStore.setValue(PreferenceConstants.APPLY_SHARPENING,
				preferenceStore.getDefaultBoolean(PreferenceConstants.APPLY_SHARPENING));
		preferenceStore.setValue(PreferenceConstants.RADIUS,
				preferenceStore.getDefaultFloat(PreferenceConstants.RADIUS));
		preferenceStore.setValue(PreferenceConstants.AMOUNT,
				preferenceStore.getDefaultFloat(PreferenceConstants.AMOUNT));
		preferenceStore.setValue(PreferenceConstants.THRESHOLD,
				preferenceStore.getDefaultInt(PreferenceConstants.THRESHOLD));
	}

	@Override
	protected void doPerformOk() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		getPreferenceStore().setValue(HTMLSELECTION, sourceSelectViewer.getCombo().getSelectionIndex());
		int port = portField.getSelection();
		String imagesPath = imagesPathField.getText();
		String exhibitionsPath = exhibitionsPathField.getText();
		String galleriesPath = galleriesPathField.getText();
		String slideshowsPath = slideshowsPathField.getText();
		preferenceStore.setValue(PreferenceConstants.PORT, port);
		preferenceStore.setValue(PreferenceConstants.STARTPAGE, pageField.getText());
		preferenceStore.setValue(PreferenceConstants.IMAGEPATH, imagesPath);
		preferenceStore.setValue(PreferenceConstants.EXHIBITIONPATH, exhibitionsPath);
		preferenceStore.setValue(PreferenceConstants.GALLERYPATH, galleriesPath);
		preferenceStore.setValue(PreferenceConstants.SLIDESHOWSPATH, slideshowsPath);
		preferenceStore.setValue(PreferenceConstants.IMAGESTITLE, imagesTitleField.getText());
		preferenceStore.setValue(PreferenceConstants.EXHIBITIONSTITLE, exhibitionsTitleField.getText());
		preferenceStore.setValue(PreferenceConstants.GALLERIESTITLE, galleriesTitleField.getText());
		preferenceStore.setValue(PreferenceConstants.SLIDESHOWSTITLE, slideshowsTitleField.getText());
		preferenceStore.setValue(PreferenceConstants.AUTOSTART, autoField.getSelection());
		preferenceStore.setValue(PreferenceConstants.THUMBNAILSPERPAGE, numberField.getSelection());
		preferenceStore.setValue(PreferenceConstants.PRIVACY, getPrivacySelection());
		preferenceStore.setValue(PreferenceConstants.IMAGES, imagesButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.EXHIBITIONS, exhibitionsButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.GALLERIES, galleriesButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.SLIDESHOWS, slideshowsButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.METADATA, infoButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.FULLSIZE, fullButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.DOWNLOAD, downloadButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.GEO, geoButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.ORPHANS, orphansButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.APPLY_SHARPENING, sharpeningGroup.getApplySharpening());
		preferenceStore.setValue(PreferenceConstants.RADIUS, sharpeningGroup.getRadius());
		preferenceStore.setValue(PreferenceConstants.AMOUNT, sharpeningGroup.getAmount());
		preferenceStore.setValue(PreferenceConstants.THRESHOLD, sharpeningGroup.getThreshold());

		int formats = 0;
		if (allField.getSelection())
			formats = ITypeFilter.ALLFORMATS;
		else {
			if (rawField.getSelection())
				formats |= ITypeFilter.RAW;
			if (dngField.getSelection())
				formats |= ITypeFilter.DNG;
			if (jpgField.getSelection())
				formats |= ITypeFilter.JPEG;
			if (tifField.getSelection())
				formats |= ITypeFilter.TIFF;
		}
		preferenceStore.setValue(PreferenceConstants.FORMATS, formats);

		int groups = 0;
		if (automaticField.getSelection())
			groups |= PreferenceConstants.AUTOMATIC;
		if (categoriesField.getSelection())
			groups |= PreferenceConstants.CATEGERIES;
		if (directoriesField.getSelection())
			groups |= PreferenceConstants.DIRECTORIES;
		if (importsField.getSelection())
			groups |= PreferenceConstants.IMPORTS;
		if (locationsField.getSelection())
			groups |= PreferenceConstants.LOCATIONS;
		if (mediaGroupField.getSelection())
			groups |= PreferenceConstants.MEDIA;
		if (personsField.getSelection())
			groups |= PreferenceConstants.PERSONS;
		if (ratingField.getSelection())
			groups |= PreferenceConstants.RATINGS;
		if (timelineField.getSelection())
			groups |= PreferenceConstants.TIMELINE;
		if (usedDefField.getSelection())
			groups |= PreferenceConstants.USER;
		preferenceStore.setValue(PreferenceConstants.GROUPS, groups);

		boolean allowUploads = allowUploadsButton.getSelection();
		preferenceStore.setValue(PreferenceConstants.ALLOWUPLOADS, allowUploads);
		if (allowUploads) {
			preferenceStore.setValue(PreferenceConstants.PASSWORD, passwordField.getText());
			preferenceStore.setValue(PreferenceConstants.ALLOWMETADATAMOD, metadataButton.getSelection());
			if (folderBackup != null) {
				Meta meta = Core.getCore().getDbManager().getMeta(true);
				ModifyMetaOperation op = new ModifyMetaOperation(meta, false, null, null, null, null, null, null, null,
						null, meta.getCumulateImports(), null, null, null, null, null, meta.getThumbnailFromPreview(),
						folderBackup, watchedFolders, meta.getFolderWatchLatency(), meta.getPauseFolderWatch(),
						meta.getReadonly(), meta.getAutoWatch(), meta.getSharpen(), meta.getWebpCompression(),
						meta.getJpegQuality(), meta.getNoIndex(), meta.getLocale(), meta.getCbirAlgorithms(),
						meta.getIndexedTextFields(), meta.getPersonsToKeywords(), null, meta.getVocabularies());
				OperationJob.executeOperation(op, this);
			}
			WatchedFolder wf = (WatchedFolder) watchedFolderViewer.getStructuredSelection().getFirstElement();
			preferenceStore.setValue(PreferenceConstants.TRANSFERFOLDER, wf.getStringId());
		}
		for (String type : PreferenceConstants.HTMLTYPES)
			preferenceStore.setValue(type, viewerMap.get(type).getDocument().get());
		preferenceStore.setValue(PreferenceConstants.CSS, cssViewer.getDocument().get());

		if ((port != oldPort || !imagesPath.equals(oldImagesPath) || !exhibitionsPath.equals(oldExhibitionsPath)
				|| !galleriesPath.equals(oldGalleriesPath) || !slideshowsPath.equals(oldSLideshowPath))
				&& WebserverActivator.getDefault().getState() == WebserverActivator.RUNNING) {
			Shell shell = getShell();
			if (AcousticMessageDialog.openQuestion(shell, Messages.WebserverPreferencePage_restart_needed,
					Messages.WebserverPreferencePage_restart_tooltip)) {
				WebserverActivator.getDefault().stopWebserver();
				shell.getDisplay().timerExec(100, () -> WebserverActivator.getDefault().startWebserver());
			}
		}
	}

	@Override
	protected void doUpdateButtons() {
		boolean showFolder = false;
		boolean showDest = false;
		addFolderButton.setEnabled(!Core.getCore().getDbManager().isReadOnly());
		WatchedFolder wf = (WatchedFolder) watchedFolderViewer.getStructuredSelection().getFirstElement();
		if (wf != null) {
			URI uri = Core.getCore().getVolumeManager().findFile(wf.getUri(), wf.getVolume());
			if (uri != null)
				showFolder = new File(uri).exists();
			String targetDir = wf.getTargetDir();
			showDest = targetDir != null && new File(targetDir).exists();
		}
		showLocButton.setEnabled(showFolder);
		showDestButton.setEnabled(showDest);
		String type = (String) sourceSelectViewer.getStructuredSelection().getFirstElement();
		boolean resetHtml = false;
		if (type != null) {
			IDocument document = viewerMap.get(type).getDocument();
			if (document != null)
				resetHtml = !document.get().equals(getPreferenceStore().getDefaultString(type));
		}
		resetHtmlButton.setEnabled(resetHtml);
		boolean resetCss = false;
		IDocument document = cssViewer.getDocument();
		if (document != null)
			resetCss = !document.get().equals(getPreferenceStore().getDefaultString(PreferenceConstants.CSS));
		resetCssButton.setEnabled(resetCss);
		int state = WebserverActivator.getDefault().getState();
		startButton.setEnabled(state == WebserverActivator.STOPPED);
		stopButton.setEnabled(state == WebserverActivator.RUNNING);
		super.doUpdateButtons();
	}

	@Override
	protected String doValidate() {
		if (imagesPathField.getText().isEmpty())
			return Messages.WebserverPreferencePage_image_path_empty;
		if (exhibitionsPathField.getText().isEmpty())
			return Messages.WebserverPreferencePage_exhibitions_path_empty;
		if (galleriesPathField.getText().isEmpty())
			return Messages.WebserverPreferencePage_web_galleries_path_empty;
		if (imagesTitleField.getText().isEmpty())
			return Messages.WebserverPreferencePage_images_title_empty;
		if (exhibitionsTitleField.getText().isEmpty())
			return Messages.WebserverPreferencePage_exhibitions_title_empty;
		if (galleriesTitleField.getText().isEmpty())
			return Messages.WebserverPreferencePage_web_galleries_title_empty;
		if (imagesPathField.getText().equalsIgnoreCase(exhibitionsPathField.getText()))
			return Messages.WebserverPreferencePage_images_exhibitions_clash;
		if (imagesPathField.getText().equalsIgnoreCase(galleriesPathField.getText()))
			return Messages.WebserverPreferencePage_images_web_galleries_clash;
		if (galleriesPathField.getText().equalsIgnoreCase(exhibitionsPathField.getText()))
			return Messages.WebserverPreferencePage_web_galleries_exhibitions_clash;
		if (!allField.getSelection() && !rawField.getSelection() && !dngField.getSelection() && !jpgField.getSelection()
				&& !tifField.getSelection())
			return Messages.WebserverPreferencePage_no_file_format;
		if (allowUploadsButton.getSelection() && watchedFolderViewer.getStructuredSelection().isEmpty())
			return Messages.WebserverPreferencePage_no_transfer_folder;
		return super.doValidate();
	}

}
