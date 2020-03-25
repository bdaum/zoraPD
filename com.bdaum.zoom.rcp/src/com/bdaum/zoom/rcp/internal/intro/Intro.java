/*******************************************************************************
 * Copyright (c) 2009-2011 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/

package com.bdaum.zoom.rcp.internal.intro;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.ColumnLayoutData;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.help.IWorkbenchHelpSystem;
import org.eclipse.ui.part.IntroPart;
import org.osgi.framework.Version;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.ISpellCheckingService;
import com.bdaum.zoom.css.CSSProperties;
import com.bdaum.zoom.rcp.internal.RcpActivator;
import com.bdaum.zoom.ui.internal.commands.AbstractCommandHandler;
import com.bdaum.zoom.ui.internal.commands.CheckUpdateCommand;
import com.bdaum.zoom.ui.internal.commands.EditMetaCommand;

public class Intro extends IntroPart implements IHyperlinkListener, IExpansionListener, Listener {

	private static final String FILE = "file:"; //$NON-NLS-1$
	private static final String HTTP = "http:"; //$NON-NLS-1$
	private static final String HELP = "help:"; //$NON-NLS-1$
	private static final String ACTION = "action:"; //$NON-NLS-1$
	private static final String PREF = "pref:"; //$NON-NLS-1$
	private static final int START = 0;
	private static final int ECLIPSE = 1;
	private static final int LUCENE = 2;
	protected static final String ECLIPSE_URL = "http://www.eclipse.org"; //$NON-NLS-1$
	protected static final String LUCENE_URL = "http://lucene.apache.org"; //$NON-NLS-1$

	private Form form;
	private List<Section> sections = new ArrayList<Section>();
	private ColumnLayout sectionGroupLayout;
	private Label subtitle;
	private boolean standby;
	private Image titleImage64;
	private Image titleImage32;
	private Image startImage;
	private Canvas buttonCanvas;
	private Image eclipseImage;
	private Image luceneImage;
	protected Rectangle startButtonRect;
	protected Rectangle eclipseImageRect;
	protected Rectangle luceneImageRect;

	@Override
	public void createPartControl(Composite parent) {
		FormColors formColors = new FormColors(parent.getDisplay());
		formColors.createColor(IFormColors.TB_BG, new RGB(255, 255, 255));
		formColors.createColor(IFormColors.TB_FG, new RGB(128, 0, 0));
		formColors.createColor(IFormColors.TB_TOGGLE, new RGB(128, 0, 0));
		formColors.createColor(IFormColors.TB_TOGGLE_HOVER, new RGB(255, 64, 0));
		FormToolkit toolkit = new FormToolkit(formColors);
		titleImage64 = RcpActivator.getImageDescriptor("icons/intro/zora64t.gif") //$NON-NLS-1$
				.createImage();
		titleImage32 = RcpActivator.getImageDescriptor("icons/intro/zora32t.gif") //$NON-NLS-1$
				.createImage();
		startImage = RcpActivator.getImageDescriptor("icons/intro/START.png") //$NON-NLS-1$
				.createImage();
		eclipseImage = RcpActivator.getImageDescriptor("icons/intro/builton_eclipse.png") //$NON-NLS-1$
				.createImage();
		luceneImage = RcpActivator.getImageDescriptor("icons/intro/lucene.png") //$NON-NLS-1$
				.createImage();
		form = toolkit.createForm(parent);
		Version version = Platform.getProduct().getDefiningBundle().getVersion();
		RcpActivator activator = RcpActivator.getDefault();
		boolean isNew = activator.isNew();
		boolean[] expansionState = activator.getIntroExpansionState();
		subtitle = toolkit.createLabel(form.getHead(), NLS.bind(Messages.Intro_version, version));
		Composite formBody = form.getBody();
		formBody.setLayout(new TableWrapLayout());
		// Create sections
		Composite sectionGroup = toolkit.createComposite(formBody);
		sectionGroup.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		sectionGroupLayout = new ColumnLayout();
		sectionGroup.setLayout(sectionGroupLayout);
		createFeatureSection(toolkit, sectionGroup, 0);
		createWhatsnewSection(toolkit, sectionGroup, expansionState != null || isNew ? 0 : Section.EXPANDED, version);
		createUpdateSection(toolkit, sectionGroup, expansionState != null ? 0 : Section.EXPANDED);
		createConfigurationSection(toolkit, sectionGroup, expansionState == null && isNew ? Section.EXPANDED : 0);
		createHelpSection(toolkit, sectionGroup, 0);
		createWebsiteSection(toolkit, sectionGroup, 0);
		if (expansionState != null) {
			int i = 0;
			for (Section section : sections)
				if (i < expansionState.length)
					section.setExpanded(expansionState[i++]);
		}
		// Create image buttons
		buttonCanvas = new Canvas(formBody, SWT.NONE);
		TableWrapData layoutData = new TableWrapData(TableWrapData.FILL_GRAB);
		layoutData.heightHint = startImage.getBounds().height;
		buttonCanvas.setLayoutData(layoutData);
		buttonCanvas.addListener(SWT.Paint, this);
		buttonCanvas.redraw();
		buttonCanvas.addListener(SWT.MouseUp, this);
		buttonCanvas.addListener(SWT.MouseMove, this);
		buttonCanvas.addListener(SWT.KeyUp, this);
	}

	int testButton(int x, int y) {
		if (startButtonRect.contains(x, y))
			return START;
		if (eclipseImageRect.contains(x, y))
			return ECLIPSE;
		if (luceneImageRect.contains(x, y))
			return LUCENE;
		return -1;
	}

	void close() {
		RcpActivator.getDefault().setIntroExpansionState(getExpansionState());
		getIntroSite().getWorkbenchWindow().getWorkbench().getIntroManager().closeIntro(Intro.this);
	}

	private boolean[] getExpansionState() {
		boolean[] state = new boolean[sections.size()];
		int i = 0;
		for (Section section : sections)
			state[i++] = section.isExpanded();
		return state;
	}

	private void createWhatsnewSection(FormToolkit toolkit, Composite parent, int style, Version version) {
		createSection(toolkit, parent, NLS.bind(Messages.Intro_whats_new_title, version),
				NLS.bind(Messages.Intro_whats_new_tooltip, version), NLS.bind(Messages.Intro_whats_new_text,
						Constants.APPLICATION_NAME, Platform.getInstallLocation().getURL().toString()),
				style);
	}

	private void createWebsiteSection(FormToolkit toolkit, Composite parent, int style) {
		createSection(toolkit, parent, Messages.Intro_hompage_title, Messages.Intro_homepage_tooltip,
				NLS.bind(Messages.Intro_homepage_text, System.getProperty("com.bdaum.zoom.homePage"), //$NON-NLS-1$
						Constants.APPLICATION_NAME),
				style);
	}

	private void createHelpSection(FormToolkit toolkit, Composite parent, int style) {
		createSection(toolkit, parent, Messages.Intro_help_title, Messages.Intro_help_tooltip,
				NLS.bind(Messages.Intro_help_text, Constants.APPLICATION_NAME,
						System.getProperty("com.bdaum.zoom.forum")), //$NON-NLS-1$
				style);

	}

	private void createUpdateSection(FormToolkit toolkit, Composite parent, int style) {
		createSection(toolkit, parent, Messages.Intro_update_title,
				NLS.bind(Messages.Intro_update_tooltip, Constants.APPLICATION_NAME),
				NLS.bind(Messages.Intro_update_text, Constants.APPLICATION_NAME), style);
	}

	private void createConfigurationSection(FormToolkit toolkit, Composite parent, int style) {
		File dict = null;
		URL url = Platform.getInstallLocation().getURL();
		if (url != null)
			try {
				File installFolder = new File(url.toURI());
				dict = new File(installFolder, ISpellCheckingService.DICTFOLDER);
				if (!dict.exists())
					dict = new File(installFolder.getParentFile(), ISpellCheckingService.DICTFOLDER);
			} catch (URISyntaxException e) {
				// should never happen
			}
		createSection(toolkit, parent, Messages.Intro_config_title,
				NLS.bind(Messages.Intro_config_tooltip, Constants.APPLICATION_NAME),
				NLS.bind(Messages.Intro_config_text,
						new Object[] { Constants.APPLICATION_NAME, dict == null ? "" : dict.getPath(), //$NON-NLS-1$
								System.getProperty(Messages.Intro_dictionaries_key) }),
				style);
	}

	private void createFeatureSection(FormToolkit toolkit, Composite parent, int style) {
		createSection(toolkit, parent, NLS.bind(Messages.Intro_features_title, Constants.APPLICATION_NAME),
				NLS.bind(Messages.Intro_features_tooltip, Constants.APPLICATION_NAME), Messages.Intro_features_text,
				style);
	}

	private Section createSection(FormToolkit toolkit, Composite parent, String title, String tooltip, String text,
			int style) {
		Section section = toolkit.createSection(parent, Section.TITLE_BAR | Section.TREE_NODE | style);
		for (Control control : section.getChildren())
			if (control instanceof Label)
				control.setData(CSSProperties.ID, CSSProperties.SECTIONTITLE); 
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 400;
		section.setLayoutData(layoutData);
		section.setText(title);
		section.setToolTipText(tooltip);
		FormText formText = toolkit.createFormText(section, true);
		formText.setText(text, true, false);
		section.setClient(formText);
		formText.addHyperlinkListener(this);
		section.addExpansionListener(this);
		formText.addListener(SWT.KeyUp, this);
		section.addListener(SWT.KeyUp, this);
		sections.add(section);
		return section;
	}

	private void showUrl(String url) {
		form.setBusy(true);
		try {
			getIntroSite().getWorkbenchWindow().getWorkbench().getBrowserSupport().getExternalBrowser()
					.openURL(new URL(url));
		} catch (PartInitException e) {
			// should never happen
		} catch (MalformedURLException e) {
			// should never happen
		} finally {
			form.setBusy(false);
		}

	}

	private void displayHelp(String href) {
		form.setBusy(true);
		try {
			IWorkbenchHelpSystem helpSystem = getIntroSite().getWorkbenchWindow().getWorkbench().getHelpSystem();
			if (href.length() <= HELP.length())
				helpSystem.displayHelp();
			else
				helpSystem.displayHelp(href.substring(HELP.length()));
		} finally {
			form.setBusy(false);
		}
	}

	@Override
	public void setFocus() {
		buttonCanvas.setFocus();
	}

	public void standbyStateChanged(boolean sb) {
		this.standby = sb;
		if (sb) {
			sectionGroupLayout.minNumColumns = sectionGroupLayout.maxNumColumns = 1;
			sectionGroupLayout.bottomMargin = sectionGroupLayout.leftMargin = sectionGroupLayout.topMargin = sectionGroupLayout.rightMargin = 3;
			buttonCanvas.setVisible(false);
			boolean firstOpen = false;
			for (Section section : sections) {
				if (section.isExpanded())
					if (firstOpen)
						section.setExpanded(false);
					else
						firstOpen = true;
				((ColumnLayoutData) section.getLayoutData()).widthHint = 250;
				((FormText) section.getClient()).setFont(JFaceResources.getDefaultFont());
			}
			form.setText(Constants.APPLICATION_NAME);
			form.setImage(titleImage32);
			form.setSeparatorVisible(false);
			form.setHeadClient(null);
			((TableWrapLayout) form.getBody().getLayout()).topMargin = 3;
		} else {
			sectionGroupLayout.minNumColumns = 2;
			sectionGroupLayout.maxNumColumns = 4;
			sectionGroupLayout.bottomMargin = sectionGroupLayout.leftMargin = sectionGroupLayout.topMargin = sectionGroupLayout.rightMargin = 10;
			buttonCanvas.setVisible(true);
			for (Section section : sections) {
				((ColumnLayoutData) section.getLayoutData()).widthHint = 400;
				((FormText) section.getClient()).setFont(JFaceResources.getDialogFont());
			}
			form.setText(NLS.bind('\n' + Messages.Intro_welcome, Constants.APPLICATION_NAME));
			form.setImage(titleImage64);
			form.setSeparatorVisible(true);
			form.setHeadClient(subtitle);
			((TableWrapLayout) form.getBody().getLayout()).topMargin = 10;
		}
		form.getBody().layout();
		form.redraw();
	}

	public void linkEntered(HyperlinkEvent e) {
		// do nothing
	}

	public void linkExited(HyperlinkEvent e) {
		// do nothing
	}

	public void linkActivated(HyperlinkEvent e) {
		String href = e.getHref().toString();
		if (href.startsWith(HTTP) || href.startsWith(FILE)) {
			showUrl(href);
			return;
		}
		if (href.startsWith(HELP)) {
			displayHelp(href);
			return;
		}
		if (href.startsWith(PREF)) {
			showPreferences(href);
			return;
		}
		if (href.startsWith(ACTION)) {
			AbstractCommandHandler action = null;
			if (href.indexOf("update") >= 0) //$NON-NLS-1$
				action = new CheckUpdateCommand();
			else if (href.indexOf("catalogSettings") >= 0) //$NON-NLS-1$
				action = new EditMetaCommand();
			if (action != null) {
				action.init(getIntroSite().getWorkbenchWindow());
				action.run();
			}
		}
	}

	private void showPreferences(String href) {
		form.setBusy(true);
		try {
			String prefId = href.substring(PREF.length());
			String data = null;
			int p = prefId.lastIndexOf('?');
			if (p >= 0) {
				data = prefId.substring(p + 1);
				prefId = prefId.substring(0, p);
			}
			PreferencesUtil.createPreferenceDialogOn(getIntroSite().getShell(), prefId, null, data).open();
		} finally {
			form.setBusy(false);
		}
	}

	public void expansionStateChanging(ExpansionEvent e) {
		// do nothing
	}

	public void expansionStateChanged(ExpansionEvent e) {
		if (e.getState() && standby)
			for (Section section : sections)
				if (section != e.getSource())
					section.setExpanded(false);
		form.layout();
		form.redraw();
	}

	@Override
	public void dispose() {
		if (titleImage64 != null)
			titleImage64.dispose();
		if (titleImage32 != null)
			titleImage32.dispose();
		if (startImage != null)
			startImage.dispose();
		if (eclipseImage != null)
			eclipseImage.dispose();
		if (luceneImage != null)
			luceneImage.dispose();
		super.dispose();
	}

	@Override
	public void handleEvent(Event e) {
		switch (e.type) {
		case SWT.KeyUp:
			if (e.keyCode == SWT.ESC)
				close();
			break;
		case SWT.MouseUp:
			switch (testButton(e.x, e.y)) {
			case START:
				close();
				break;
			case ECLIPSE:
				showUrl(ECLIPSE_URL);
				break;
			case LUCENE:
				showUrl(LUCENE_URL);
				break;
			}
			break;
		case SWT.MouseMove:
			buttonCanvas.setCursor(
					e.display.getSystemCursor(testButton(e.x, e.y) >= 0 ? SWT.CURSOR_HAND : SWT.CURSOR_ARROW));
			break;
		case SWT.Paint:
			Rectangle area = buttonCanvas.getClientArea();
			Rectangle ibounds = startImage.getBounds();
			startButtonRect = new Rectangle((area.width - ibounds.width) / 2, (area.height - ibounds.height) / 2,
					ibounds.width, ibounds.height);
			e.gc.drawImage(startImage, startButtonRect.x, startButtonRect.y);
			Rectangle ebounds = eclipseImage.getBounds();
			Rectangle lbounds = luceneImage.getBounds();
			eclipseImageRect = new Rectangle(
					area.width / 2 - ibounds.width - (6 * ebounds.width + 4 * lbounds.width) / 10,
					(area.height - ebounds.height) / 2, ebounds.width, ebounds.height);
			e.gc.drawImage(eclipseImage, eclipseImageRect.x, eclipseImageRect.y);
			luceneImageRect = new Rectangle(area.width / 2 + ibounds.width, (area.height - lbounds.height) / 2,
					lbounds.width, lbounds.height);
			e.gc.drawImage(luceneImage, luceneImageRect.x, luceneImageRect.y);
			break;
		}
		
	}

}
