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

package com.bdaum.zoom.spell.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.core.ISpellCheckingService;
import com.bdaum.zoom.core.ISpellCheckingService.ISpellIncident;
import com.bdaum.zoom.core.ISpellCheckingService.IncendentListener;
import com.stibocatalog.hunspell.Hunspell;
import com.stibocatalog.hunspell.Hunspell.Dictionary;

public class SpellCheckingEngine {

	private static final String AFFEXTENSION = ".aff"; //$NON-NLS-1$
	private static final String DICEXTENSION = ".dic"; //$NON-NLS-1$

	private static final String USEREXTENSION = ".user"; //$NON-NLS-1$

	private static final String OXTEXTENSION = ".oxt"; //$NON-NLS-1$

	private Dictionary hunSpellDictionary;
	ListenerList<IncendentListener> listeners = new ListenerList<IncendentListener>();
	private File userFile;
	Set<String> userDict = new HashSet<String>(100);

	public SpellCheckingEngine(String dict) {
		try {
			final String name = dict.substring(0, dict.lastIndexOf('.'));
			if (dict.endsWith(OXTEXTENSION)) {
				try (ZipInputStream zipStream = new ZipInputStream(new FileInputStream(new File(dict)))) {
					ZipEntry entry = zipStream.getNextEntry();
					boolean aff = false, dic = false;
					while (null != entry) {
						if (entry.getName().endsWith(AFFEXTENSION)) {
							extractFromZip(zipStream, entry, name + AFFEXTENSION);
							aff = true;
						} else if (entry.getName().endsWith(DICEXTENSION)) {
							extractFromZip(zipStream, entry, name + DICEXTENSION);
							dic = true;
						}
						if (dic && aff)
							break;
						entry = zipStream.getNextEntry();
					}
				}
			}
			try {
				hunSpellDictionary = Hunspell.getInstance().getDictionary(name);
			} catch (UnsatisfiedLinkError e) {
				SpellActivator.getDefault().logWarning(Messages.SpellCheckingEngine_no_native_library, e);
			} catch (UnsupportedOperationException e) {
				SpellActivator.getDefault().logWarning(Messages.SpellCheckingEngine_not_supported, e);
			} catch (Throwable e) {
				SpellActivator.getDefault().logWarning(Messages.SpellCheckingEngine_cannot_instantiate, e);
			}
			userFile = new File(name + USEREXTENSION);
			if (userFile.exists()) {
				StringBuilder sb = new StringBuilder(64000);
				try (FileReader reader = new FileReader(userFile)) {
					char[] buffer = new char[4096];
					while (true) {
						int read = reader.read(buffer);
						if (read < 0)
							break;
						sb.append(buffer, 0, read);
					}
					StringTokenizer st = new StringTokenizer(sb.toString(), "\n\r"); //$NON-NLS-1$
					while (st.hasMoreTokens())
						userDict.add(st.nextToken());
				}
			}
		} catch (IOException e) {
			SpellActivator.getDefault().logError(NLS.bind(Messages.SpellCheckingService_io_error_reading_dict, dict),
					e);
		}
	}

	private static void extractFromZip(ZipInputStream zipStream, ZipEntry entry, String targetName) throws IOException {
		File outFile = new File(targetName);
		outFile.delete();
		outFile.createNewFile();
		try (FileOutputStream out = new FileOutputStream(outFile)) {
			byte[] buffer = new byte[8192];
			long size = entry.getSize();
			while (size > 0) {
				int read = zipStream.read(buffer, 0, (int) Math.min(buffer.length, size));
				if (read <= 0)
					break;
				out.write(buffer, 0, read);
				size -= read;
			}
		}
	}

	public void checkSpelling(String text, int options, final int nmax) {
		if (hunSpellDictionary != null) {
			final String[] strings = text.split("[^\\p{L}]"); //$NON-NLS-1$
			int distance = 0;
			loop: for (final String str : strings) {
				final int strLength = str.length();
				if (checkOptions(str, options) && !userDict.contains(str)) {
					if (hunSpellDictionary.misspelled(str)) {
						final int offset = distance;
						ISpellIncident incident = new ISpellIncident() {
							public String getWrongWord() {
								return str;
							}

							public String[] getSuggestions() {
								final List<String> words = hunSpellDictionary.suggest(str);
								if (words.isEmpty())
									return null;
								String[] suggestions = words.toArray(new String[words.size()]);
								if (suggestions.length > nmax) {
									String[] truncated = new String[nmax];
									System.arraycopy(suggestions, 0, truncated, 0, nmax);
									return truncated;
								}
								return suggestions;
							}

							public int getOffset() {
								return offset;
							}

							public boolean happensAt(int x) {
								return x >= offset && x < offset + str.length();
							}

							public void addWord() {
								addToDictionary(str);
							}

						};
						for (IncendentListener listener : listeners)
							if (listener.handleIncident(incident))
								break loop;
					}
				}
				distance += strLength + 1;
			}
		}
	}

	public void addToDictionary(String word) {
		if (userDict != null) {
			userDict.add(word);
			try (RandomAccessFile file = new RandomAccessFile(userFile, "rw")) { //$NON-NLS-1$
				file.seek(file.length());
				file.write((word + "\n\r").getBytes("UTF-8")); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (IOException e) {
				SpellActivator.getDefault().logError(Messages.CombinedSpellChecker_io_error_writing_word, e);
			} 
		}
	}

	public void addSpellCheckListener(IncendentListener incidentListener) {
		listeners.add(incidentListener);
	}

	public void removeSpellCheckListener(IncendentListener incidentListener) {
		listeners.remove(incidentListener);
	}

	private static boolean checkOptions(String str, int options) {
		if (str.isEmpty() || str.matches("\\s+")) //$NON-NLS-1$
			return false;
		// option rules
		if ((options & ISpellCheckingService.IGNORESINGLELETTER) > 0 && str.length() == 1)
			return false;
		if ((options & ISpellCheckingService.IGNORNEUPPERCASE) > 0 && str.toUpperCase().equals(str))
			return false;
		if ((options & ISpellCheckingService.IGNOREWIDTHDIGITS) > 0 && str.matches(".*[\\d]+.*")) //$NON-NLS-1$
			return false;
		if ((options & ISpellCheckingService.IGNOREMIXEDCASE) > 0 && str.toUpperCase().equals(str.toLowerCase()))
			return false;
		if ((options & ISpellCheckingService.IGNOREWITHNONLETTERS) > 0 && str.matches("[^\\p{L}]+")) //$NON-NLS-1$
			return false;
		return true;
	}

}