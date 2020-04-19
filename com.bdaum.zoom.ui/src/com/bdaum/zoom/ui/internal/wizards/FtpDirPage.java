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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.bdaum.zoom.core.IFTPService;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.mtp.ObjectFilter;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.widgets.ExpandCollapseGroup;
import com.bdaum.zoom.ui.wizards.ColoredWizardPage;

@SuppressWarnings("restriction")
public class FtpDirPage extends ColoredWizardPage {

	private CheckboxTreeViewer viewer;
	private FTPClient ftp;
	protected Map<FTPFile, FTPFile> fileParents = new HashMap<FTPFile, FTPFile>(57);
	protected Map<FTPFile, String> dirPaths = new HashMap<FTPFile, String>(57);
	private ObjectFilter filter;
	private String dir;
	private Label urlLabel;
	private URL url;
	private IFTPService service;
	private ServiceReference<?> serviceReference;
	private Object ticket;

	public FtpDirPage() {
		super("FTP-Import"); //$NON-NLS-1$
		filter = CoreActivator.getDefault().getFilenameExtensionFilter();
		BundleContext bundleContext = UiActivator.getDefault().getBundle().getBundleContext();
		serviceReference = bundleContext.getServiceReference(IFTPService.class.getName());
		if (serviceReference != null) {
			service = (IFTPService) bundleContext.getService(serviceReference);
			try {
				ticket = service.startSession();
			} finally {
				bundleContext.ungetService(serviceReference);
			}
		}
	}

	@Override
	public IWizardPage getNextPage() {
		return null;
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 1);
		viewer = createViewerGroup(composite);
		setControl(composite);
		setHelp(HelpContextIds.IMPORTREMOTE);
		setTitle(Messages.FtpDirPage_title);
		setMessage(Messages.FtpDirPage_select_images);
		super.createControl(parent);
	}

	private ContainerCheckedTreeViewer createViewerGroup(Composite comp) {
		urlLabel = new Label(comp, SWT.NONE);
		urlLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		ExpandCollapseGroup expandCollapseGroup = new ExpandCollapseGroup(comp, SWT.NONE);
		final ContainerCheckedTreeViewer cbViewer = new ContainerCheckedTreeViewer(comp,
				SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		expandCollapseGroup.setViewer(cbViewer);
		final Tree tree = cbViewer.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		cbViewer.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof FTPFile)
					return ((FTPFile) element).getName();
				return element.toString();
			}
		});
		cbViewer.setContentProvider(new ITreeContentProvider() {
			public void inputChanged(Viewer v, Object oldInput, Object newInput) {
				fileParents.clear();
				dirPaths.clear();
			}

			public void dispose() {
				fileParents.clear();
				dirPaths.clear();
			}

			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof FTPClient) {
					FTPClient ftpClient = (FTPClient) inputElement;
					List<FTPFile> files = new ArrayList<FTPFile>();
					try {
						FTPFile[] listFiles = ftpClient.listFiles();
						for (FTPFile ftpFile : listFiles) {
							if (ftpFile.isDirectory()) {
								if (!ftpFile.getName().endsWith(".")) { //$NON-NLS-1$
									files.add(ftpFile);
									dirPaths.put(ftpFile, dir + '/' + ftpFile.getName());
								}
							} else if (filter.accept(ftpFile.getName()))
								files.add(ftpFile);
						}
					} catch (IOException e) {
						// ignore
					}
					return files.toArray();
				}
				return new Object[0];
			}

			public boolean hasChildren(Object element) {
				if (element instanceof FTPFile)
					return ((FTPFile) element).isDirectory();
				return false;
			}

			public Object getParent(Object element) {
				return fileParents.get(element);
			}

			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof FTPFile) {
					FTPFile parent = (FTPFile) parentElement;
					if (parent.isDirectory()) {
						String path = dirPaths.get(parent);
						if (path != null)
							try {
								if (ftp.changeWorkingDirectory(path)) {
									List<FTPFile> files = new ArrayList<FTPFile>();
									for (FTPFile ftpFile : ftp.listFiles()) {
										if (ftpFile.isDirectory()) {
											if (!ftpFile.getName().endsWith(".")) { //$NON-NLS-1$
												files.add(ftpFile);
												dirPaths.put(ftpFile, path + '/' + ftpFile.getName());
											}
										} else if (filter.accept(ftpFile.getName()))
											files.add(ftpFile);
										fileParents.put(ftpFile, parent);
									}
									return files.toArray();
								}
							} catch (IOException e) {
								// ignore
							}
					}
				}
				return new Object[0];
			}
		});
		cbViewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer v, Object e1, Object e2) {
				if (e1 instanceof FTPFile && e2 instanceof FTPFile) {
					int i1 = ((FTPFile) e1).isDirectory() ? 1 : 2;
					int i2 = ((FTPFile) e2).isDirectory() ? 1 : 2;
					if (i1 != i2)
						return i1 - i2;
					return ((FTPFile) e1).getName().compareToIgnoreCase(((FTPFile) e2).getName());
				}
				return super.compare(v, e1, e2);
			}
		});
		cbViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				validatePage();
			}
		});
		UiUtilities.installDoubleClickExpansion(cbViewer);
		return cbViewer;
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			URL u = ((RemoteImportWizard) getWizard()).getUrl();
			urlLabel.setText(stripUserData(u.toString()));
			setInput(u);
		}
		super.setVisible(visible);
	}

	private static String stripUserData(String s) {
		int p = s.indexOf('@');
		if (p > 0) {
			int q = s.indexOf(':', p);
			if (q > 0 && q < p) {
				while (++q < s.length())
					if (s.charAt(q) != '/')
						break;
				s = new StringBuilder().append(s, 0, q).append(s, p + 1, s.length()).toString();
			}
		}
		return s;
	}

	private void setInput(URL url) {
		ftp = null;
		this.url = url;
		if (ticket != null) {
			ftp = (FTPClient) service.getClient(ticket, url);
			if (ftp != null)
				try {
					dir = url.getPath();
					if (!dir.isEmpty() && !ftp.changeWorkingDirectory(dir))
						ftp = null;
				} catch (IOException e) {
					ftp = null;
				}
			if (ftp != null)
				viewer.setInput(ftp);
		}
		validatePage();
	}

	@Override
	protected String validate() {
		if (ftp == null)
			return Messages.FtpDirPage_no_connection;
		Object[] checkedElements = viewer.getCheckedElements();
		if (checkedElements.length == 0)
			return Messages.FtpDirPage_nothing_selected;
		for (Object object : checkedElements)
			if (object instanceof FTPFile) {
				FTPFile file = (FTPFile) object;
				if (file.isFile() && filter.accept(file.getName()))
					return null;
			}
		return Messages.FtpDirPage_no_image_is_selected;
	}

	@Override
	public void dispose() {
		if (ticket != null)
			service.endSession(ticket);
		if (serviceReference != null)
			UiActivator.getDefault().getBundle().getBundleContext().ungetService(serviceReference);
		super.dispose();
	}

	public URI[] getURIs() {
		if (ftp != null) {
			String prefix = "ftp://" + url.getHost(); //$NON-NLS-1$
			int port = url.getPort();
			if (port > 0 && port != ftp.getDefaultPort())
				prefix += ":" + port; //$NON-NLS-1$
			prefix += '/';
			Object[] checkedElements = viewer.getCheckedElements();
			List<URI> list = new ArrayList<URI>();
			for (Object object : checkedElements) {
				if (object instanceof FTPFile) {
					FTPFile file = (FTPFile) object;
					if (file.isFile() && filter.accept(file.getName())) {
						FTPFile parent = fileParents.get(file);
						String dirPath;
						if (parent == null)
							dirPath = stripSlashes(dir);
						else
							dirPath = stripSlashes(dirPaths.get(parent));
						try {
							list.add(new URI(
									prefix + (dirPath.isEmpty() ? file.getName() : dirPath + '/' + file.getName())));
						} catch (URISyntaxException e) {
							// should never happen
						}
					}
				}
			}
			return list.toArray(new URI[list.size()]);
		}
		return null;
	}

	private static String stripSlashes(String s) {
		while (s.startsWith("/")) //$NON-NLS-1$
			s = s.substring(1);
		return s;
	}

}
