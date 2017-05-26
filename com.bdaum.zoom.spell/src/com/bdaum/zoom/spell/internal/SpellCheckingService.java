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

package com.bdaum.zoom.spell.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.common.internal.FileLocator;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.ISpellCheckingService;

public class SpellCheckingService implements ISpellCheckingService {

	private static final String USR_SHARE_HUNSPELL = "/usr/share/hunspell/"; //$NON-NLS-1$

	private static final String DICEXTENSION = ".dic"; //$NON-NLS-1$

	private static final String OXTEXTENSION = ".oxt"; //$NON-NLS-1$

	private SpellCheckingEngine engine;

	private String locale;

	public synchronized void checkSpelling(String text, IncendentListener incidentListener, int options, int nmax) {
		Meta meta = Core.getCore().getDbManager().getMeta(false);
		String currentLocale = meta == null ? null : meta.getLocale();
		if (currentLocale != null && !currentLocale.equals(locale))
			engine = null;
		SpellCheckingEngine eng = getEngine();
		if (eng != null) {
			eng.addSpellCheckListener(incidentListener);
			eng.checkSpelling(text, options, nmax);
			eng.removeSpellCheckListener(incidentListener);
		}
	}

	private SpellCheckingEngine getEngine() {
		if (engine == null) {
			String dict = findDictionary();
			if (dict != null)
				engine = new SpellCheckingEngine(dict);
		}
		return engine;
	}

	private String findDictionary() {
		Meta meta = Core.getCore().getDbManager().getMeta(false);
		locale = meta == null ? null : meta.getLocale();
		if (locale == null)
			locale = Locale.getDefault().toString();
		File folder = new File(Platform.getInstallLocation().getURL().getPath() + DICTFOLDER);
		String fileName = null;
		List<File> allFiles = new ArrayList<File>();
		if (folder.exists())
			allFiles.addAll(Arrays.asList(folder.listFiles()));
		File parentFolder = new File(folder.getParentFile().getParentFile(), DICTFOLDER);
		if (parentFolder.exists())
			allFiles.addAll(Arrays.asList(parentFolder.listFiles()));
		for (File dictFolder : getDictFolders())
			allFiles.addAll(Arrays.asList(dictFolder.listFiles()));
		if (!allFiles.isEmpty()) {
			File[] files = allFiles.toArray(new File[allFiles.size()]);
			File file = findDict(files, locale);
			if (file == null || !file.exists()) {
				int p = locale.indexOf('_');
				if (p > 0)
					file = findDict(files, locale.substring(0, p));
			}
			if (file == null || !file.exists())
				file = findDict(files, "default"); //$NON-NLS-1$
			if (file != null && file.exists())
				fileName = file.getAbsolutePath();
		}
		if (fileName == null) {
			try {
				folder = FileLocator.findFile(SpellActivator.getDefault().getBundle(), "/"); //$NON-NLS-1$
			} catch (Exception e) {
				// ignore
			}
			File file = findDict(folder.listFiles(), "default"); //$NON-NLS-1$
			fileName = (file != null && file.exists()) ? file.getAbsolutePath() : null;
		}
		return fileName;
	}

	private static File findDict(File[] files, String prefix) {
		File file = findDict(files, prefix, DICEXTENSION);
		if (file == null)
			file = findDict(files, prefix, OXTEXTENSION);
		return file;
	}

	private static File findDict(File[] files, String prefix, String ext) {
		for (File file : files) {
			String name = file.getName();
			int p = name.indexOf(prefix);
			if (p >= 0) {
				if (p > 0) {
					char c = name.charAt(p - 1);
					if (c != '_' && c != '.' && c != '-')
						continue;
				}
				char c = name.charAt(p + prefix.length());
				if ((c == '_' || c == '.' || c == '-') && name.endsWith(ext))
					return file;
			}
		}
		return null;
	}

	public void addWord(String word) {
		if (engine != null)
			engine.addToDictionary(word);
	}

	private static final String PROGRAM_FILES = "ProgramFiles"; //$NON-NLS-1$
	private static final String PROGRAM_FILES_X86 = "ProgramFiles(x86)"; //$NON-NLS-1$

	public Collection<String> getSupportedLanguages() {
		Set<String> result = new HashSet<String>();
		File installFolder = new File(Platform.getInstallLocation().getURL().getPath());
		getSupportedLanguages(result, new File(installFolder, DICTFOLDER));
		getSupportedLanguages(result, new File(installFolder.getParentFile(), DICTFOLDER));
		for (File folder : getDictFolders())
			getSupportedLanguages(result, folder);
		return result;
	}

	protected List<File> getDictFolders() {
		List<File> dictFolders = new ArrayList<>();
		if (Constants.WIN32)
			getWinLibreOfficeExtensions(dictFolders);
		else if (Constants.LINUX)
			dictFolders.add(new File(USR_SHARE_HUNSPELL));
		return dictFolders;
	}

	private static List<File> getWinLibreOfficeExtensions(List<File> dictFolders) {
		String targetFolder = System.getenv(PROGRAM_FILES_X86);
		if (targetFolder != null)
			collecDictionaryFolders(dictFolders, targetFolder);
		targetFolder = System.getenv(PROGRAM_FILES);
		if (targetFolder != null)
			collecDictionaryFolders(dictFolders, targetFolder);
		return dictFolders;
	}

	private static void collecDictionaryFolders(List<File> dictFolders, String targetFolder) {
		try {
			File programsFolder = new File(targetFolder);
			File adobeReader = new File(programsFolder,
					"Common Files\\Adobe\\Reader\\DC\\Linguistics\\Providers\\Plugins2\\AdobeHunspellPlugin\\Dictionaries"); //$NON-NLS-1$
			File[] list = adobeReader.listFiles();
			if (list != null)
				for (File nlFolder : list) {
					File[] children = nlFolder.listFiles();
					if (children != null)
						dictFolders.addAll(Arrays.asList(children));
				}
			File thunderbird = new File(programsFolder, "Mozilla Thunderbird\\dictionaries"); //$NON-NLS-1$
			if (thunderbird.exists())
				dictFolders.add(thunderbird);
			for (int i = 4; i < 10; i++) {
				File libreOffice = new File(programsFolder, NLS.bind("LibreOffice {0}\\share\\extensions", i)); //$NON-NLS-1$
				String[] subs = libreOffice.list();
				if (subs != null)
					for (String sub : subs)
						if (sub.startsWith("dict-")) //$NON-NLS-1$
							dictFolders.add(new File(libreOffice, sub));
			}
		} catch (Exception e) {
			// cannot find dicts
		}
	}

	private static void getSupportedLanguages(Set<String> result, File folder) {
		if (folder.exists()) {
			String[] list = folder.list();
			if (list != null)
				for (String name : list) {
					String loc = null;
					if (name.endsWith(DICEXTENSION) && !name.contains("hyph")) //$NON-NLS-1$
						loc = name.substring(0, name.length() - DICEXTENSION.length());
					else if (name.endsWith(OXTEXTENSION))
						loc = name.substring(0, name.length() - OXTEXTENSION.length());
					if (loc != null) {
						if (loc.startsWith("dict-")) //$NON-NLS-1$
							loc = loc.substring(5);
						if (loc.startsWith("dic-")) //$NON-NLS-1$
							loc = loc.substring(4);
						int p = loc.indexOf('-');
						if (p > 0)
							loc = loc.substring(0, p);
							result.add(loc);
					}
				}
		}
	}

}
