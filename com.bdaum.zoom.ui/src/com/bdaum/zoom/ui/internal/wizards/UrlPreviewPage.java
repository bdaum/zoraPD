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

package com.bdaum.zoom.ui.internal.wizards;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.CloseWindowListener;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.internal.FileNameExtensionFilter;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.mtp.ObjectFilter;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.wizards.ColoredWizardPage;

@SuppressWarnings("restriction")
public class UrlPreviewPage extends ColoredWizardPage {

	private Browser browser;
	private ObjectFilter filter;
	private Label urlLabel;
	private URL url;
	private Button backButton;
	private Button forwardButton;
	private Composite comp;

	public UrlPreviewPage() {
		super("URL-Preview"); //$NON-NLS-1$
		filter = new FileNameExtensionFilter(ImageConstants.getSupportedImageFileExtensionsGroups(true), true);
	}

	@Override
	public IWizardPage getNextPage() {
		return null;
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 1);
		createButtonGroup(composite);
		createBrowserGroup(composite);
		setControl(composite);
		setHelp(HelpContextIds.IMPORTREMOTE);
		setTitle(Messages.UrlPreviewPage_title);
		setMessage(Messages.UrlPreviewPage_please_press_finish);
		super.createControl(parent);
	}

	private void createButtonGroup(Composite composite) {
		comp = new Composite(composite, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout(3, false));
		urlLabel = new Label(comp, SWT.NONE);
		urlLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		backButton = new Button(comp, SWT.PUSH);
		backButton.setImage(Icons.backwards.getImage());
		backButton.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		backButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browser.back();
			}
		});
		forwardButton = new Button(comp, SWT.PUSH);
		forwardButton.setImage(Icons.forwards.getImage());
		forwardButton.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		forwardButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browser.forward();
			}
		});
	}

	private void createBrowserGroup(Composite composite) {

		browser = new Browser(composite, SWT.BORDER);
		browser.addCloseWindowListener(new CloseWindowListener() {
			public void close(WindowEvent event) {
				getWizard().getContainer().getShell().close();
			}
		});
		browser.addLocationListener(new LocationListener() {

			public void changing(LocationEvent event) {
				// do nothing
			}

			public void changed(LocationEvent event) {
				try {
					url = new URL(event.location);
				} catch (MalformedURLException e) {
					// ignore
				}
				urlLabel.setText(url.toString());
				validatePage();
			}
		});
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.widthHint = 400;
		layoutData.heightHint = 400;
		browser.setLayoutData(layoutData);
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			url = ((RemoteImportWizard) getWizard()).getUrl();
			browser.setUrl(url.toString());
			urlLabel.setText(url.toString());
			validatePage();
		}
		super.setVisible(visible);
	}

	@Override
	protected String validate() {
		backButton.setEnabled(browser.isBackEnabled());
		forwardButton.setEnabled(browser.isForwardEnabled());
		try {
			if (url != null && filter.accept(Core.getFileName(url.toURI(), true)))
				return null;
		} catch (URISyntaxException e) {
			// fall through
		}
		return Messages.UrlPreviewPage_does_not_point_to_image;
	}

	public URI[] getURIs() {
		try {
			return new URI[] { url.toURI() };
		} catch (URISyntaxException e) {
			return null;
		}
	}
}
