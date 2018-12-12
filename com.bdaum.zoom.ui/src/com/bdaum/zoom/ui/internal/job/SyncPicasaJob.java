package com.bdaum.zoom.ui.internal.job;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.cat.model.meta.WatchedFolder;
import com.bdaum.zoom.cat.model.meta.WatchedFolderImpl;
import com.bdaum.zoom.common.internal.IniReader;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.fileMonitor.internal.filefilter.FilterChain;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.job.AbstractUiDaemon;

@SuppressWarnings("restriction")
public class SyncPicasaJob extends AbstractUiDaemon {

	private static final String EMAIL = "email"; //$NON-NLS-1$

	private static final String DISPLAY = "display"; //$NON-NLS-1$

	private static final String NAME = "name"; //$NON-NLS-1$

	private static final String FACES = "faces"; //$NON-NLS-1$

	private static final String PICASA_INI = ".picasa.ini"; //$NON-NLS-1$

	protected static final Object CONTACTS = "contacts"; //$NON-NLS-1$

	protected static final Object CONTACT = "contact"; //$NON-NLS-1$

	private IDbManager dbManager;

	private File contactsFile;

	private CoreActivator core = CoreActivator.getDefault();

	private IVolumeManager volumeManager = core.getVolumeManager();

	private final IAdaptable adaptable;

	public SyncPicasaJob(IAdaptable adaptable) {
		super(Messages.SyncPicasaJob_synchronize, 60000, 300000, 30);
		this.adaptable = adaptable;
		contactsFile = UiActivator.getDefault().findPicasaContactsFile();
	}

	@Override
	public boolean belongsTo(Object family) {
		return Constants.SYNCPICASA == family || super.belongsTo(family);
	}

	@Override
	protected void doRun(IProgressMonitor monitor) {
		dbManager = core.getDbManager();
		Meta meta = dbManager.getMeta(false);
		UiActivator activator = UiActivator.getDefault();
		if (meta != null) {
			if (contactsFile != null && contactsFile.exists()) {
				try {
					Date now = new Date();
					Date lastScan = meta.getPicasaScannerVersion() != Constants.PICASASCANNERVERSION ? null
							: meta.getLastPicasaScan();
					long lastScanTime = lastScan == null ? 0L : lastScan.getTime();
					boolean updated = true;
					if (lastScan != null) {
						long lastUpdate = activator.getLastPicasaUpdate();
						updated = lastUpdate <= 0L || lastUpdate >= lastScanTime;
					}
					if (updated) {
						WatchedFolder[] watchedFolders = null;
						List<String> folders = meta.getWatchedFolder();
						if (folders != null) {
							List<WatchedFolder> w = new ArrayList<WatchedFolder>(folders.size());
							for (String folderId : folders) {
								WatchedFolder f = core.getObservedFolder(folderId);
								if (f != null && !f.getTransfer())
									w.add(f);
							}
							watchedFolders = w.toArray(new WatchedFolder[w.size()]);
						}
						monitor.beginTask(Messages.SyncPicasaJob_scanning_face_data,
								(watchedFolders == null ? 0 : watchedFolders.length) + 2);
						monitor.subTask(Messages.SyncPicasaJob_scanning_picasa);
						updatePersonAlbums(contactsFile, lastScan);
						if (monitor.isCanceled())
							return;
						monitor.worked(1);
						if (watchedFolders != null && watchedFolders.length > 0)
							for (WatchedFolder wf : watchedFolders) {
								File folderFile = volumeManager.findExistingFile(wf.getUri(), wf.getVolume());
								if (folderFile != null) {
									FilterChain filterChain = new FilterChain(UiUtilities.getFilters(wf), "-+_*", ";", //$NON-NLS-1$//$NON-NLS-2$
											true);
									filterChain.setBaseLength(folderFile.getAbsolutePath().length() + 1);
									scanIniFiles(folderFile, monitor, wf, filterChain, lastScanTime, now);
									if (monitor.isCanceled())
										return;
									monitor.worked(1);
								}
							}
					} else
						monitor.beginTask(Messages.SyncPicasaJob_scanning_face_data, 1);
					meta.setLastPicasaScan(now);
					meta.setPicasaScannerVersion(Constants.PICASASCANNERVERSION);
					dbManager.storeAndCommit(meta);
					if (monitor.isCanceled())
						return;
					monitor.worked(1);
				} catch (Exception e) {
					activator.logError(Messages.SyncPicasaJob_internal_error, e);
				}
				monitor.done();
			}
		}
	}

	private void scanIniFiles(File folder, IProgressMonitor monitor, WatchedFolder observedFolder,
			FilterChain filterChain, long lastScan, Date now) {
		if (folder != null) {
			File iniFile = new File(folder, PICASA_INI);
			if (iniFile.exists() && iniFile.lastModified() >= lastScan)
				processIniFile(observedFolder, folder, iniFile, now);
			if (observedFolder.getRecursive()) {
				File[] members = folder.listFiles();
				if (members != null)
					for (File member : members) {
						if (monitor.isCanceled())
							break;
						if (member.isDirectory() && (filterChain == null || filterChain.accept(member, true))) {
							WatchedFolderImpl observedMember = (WatchedFolderImpl) core
									.getObservedSubfolder(observedFolder, member);
							if (observedMember != null)
								scanIniFiles(member, monitor, observedMember, filterChain, lastScan, now);
						}
					}
			}
		}
	}

	private void processIniFile(WatchedFolder wf, File folder, File iniFile, Date now) {
		IniReader iniReader = null;
		try {
			iniReader = new IniReader(iniFile, true);
		} catch (IOException e) {
			UiActivator.getDefault().logError(Messages.SyncPicasaJob_io_error_reading_ini, e);
		}
		if (iniReader != null) {
			List<File> outDatedFiles = new ArrayList<File>();
			for (String section : iniReader.listSections()) {
				File imageFile = new File(folder, section);
				if (imageFile.exists()) {
					List<AssetImpl> assets = dbManager.obtainAssetsForFile(imageFile.toURI());
					if (!assets.isEmpty()) {
						AssetImpl asset = assets.get(0);
						String lastPicasaIniEntry = asset.getLastPicasaIniEntry();
						if (lastPicasaIniEntry == null
								|| !lastPicasaIniEntry.equals(iniReader.getPropertyString(section, FACES, null)))
							outDatedFiles.add(imageFile);
					}
				}
			}
			ChangeProcessor changeProcessor = new ChangeProcessor(null, outDatedFiles, null, wf, now.getTime(),
					UiActivator.getDefault().createImportConfiguration(adaptable, true, false, false, false, false,
							false, true, true, true),
					Constants.FOLDERWATCH, adaptable);
			changeProcessor.schedule();
		}
	}

	private void updatePersonAlbums(File contacts, Date lastScan) {
		final Calendar lastTime = new GregorianCalendar(1970, 0, 1);
		if (lastScan != null)
			lastTime.setTime(lastScan);
		try (InputStream in = new BufferedInputStream(new FileInputStream(contacts))) {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			DefaultHandler handler = new DefaultHandler() {
				Stack<String> stack = new Stack<String>();

				@Override
				public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
						throws SAXException {
					if (stack.isEmpty() && CONTACTS.equals(qName))
						stack.push(qName);
					else if (CONTACTS.equals(stack.peek())) {
						if (CONTACT.equals(qName)) {
							String name = atts.getValue(NAME);
							if (name == null)
								name = ""; //$NON-NLS-1$
							String displayName = atts.getValue(DISPLAY);
							if (displayName == null || displayName.isEmpty())
								displayName = name;
							StringBuilder sb = new StringBuilder();
							for (int i = 0; i < atts.getLength(); i++) {
								String attName = atts.getQName(i);
								if (attName.startsWith(EMAIL)) {
									if (sb.length() > 0)
										sb.append(';');
									sb.append(atts.getValue(i));
								}
							}
							if (displayName.length() > 0 && sb.length() > 0)
								updateAlbums(displayName, sb.toString());
						}
					}
				}

				@Override
				public void endElement(String uri, String localName, String qName) throws SAXException {
					if (qName.equals(stack.peek()))
						stack.pop();
				}
			};
			saxParser.parse(in, handler);
		} catch (ParserConfigurationException e) {
			UiActivator.getDefault().logError(Messages.SyncPicasaJob_error_configuring_SAX, e);
		} catch (SAXException e) {
			UiActivator.getDefault().logError(NLS.bind(Messages.SyncPicasaJob_error_parsing_file, contacts), e);
		} catch (IOException e) {
			UiActivator.getDefault().logError(NLS.bind(Messages.SyncPicasaJob_error_reading_file, contacts), e);
		}
	}

	protected void updateAlbums(String displayName, String emails) {
		if (emails != null && !emails.isEmpty()) {
			List<Object> toBeStored = new ArrayList<Object>();
			for (SmartCollectionImpl sm : dbManager.<SmartCollectionImpl>obtainObjects(SmartCollectionImpl.class, false,
					"name", //$NON-NLS-1$
					displayName, QueryField.EQUALS, "system", true, QueryField.EQUALS, "album", true, //$NON-NLS-1$ //$NON-NLS-2$
					QueryField.EQUALS)) {
				if (Utilities.updateAlbumWithEmail(sm, emails))
					toBeStored.add(sm);
				break;
			}
			dbManager.safeTransaction(null, toBeStored);
		}
	}

}
