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
 * (c) 2009-2011 Berthold Daum  
 */

package com.bdaum.zoom.core.internal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.RGB;

import com.bdaum.zoom.cat.model.Meta_type;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.asset.MediaExtension;
import com.bdaum.zoom.cat.model.asset.RegionImpl;
import com.bdaum.zoom.cat.model.group.Criterion;
import com.bdaum.zoom.cat.model.group.CriterionImpl;
import com.bdaum.zoom.cat.model.group.Group;
import com.bdaum.zoom.cat.model.group.GroupImpl;
import com.bdaum.zoom.cat.model.group.PostProcessor;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.SortCriterion;
import com.bdaum.zoom.cat.model.group.SortCriterionImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebExhibitImpl;
import com.bdaum.zoom.cat.model.location.Location;
import com.bdaum.zoom.cat.model.location.LocationImpl;
import com.bdaum.zoom.cat.model.meta.Category;
import com.bdaum.zoom.cat.model.meta.CategoryImpl;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.common.GeoMessages;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.mtp.ObjectFilter;
import com.bdaum.zoom.program.BatchConstants;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.program.HtmlEncoderDecoder;
import com.google.openlocationcode.OpenLocationCode;

public class Utilities {

	private static final String CATEGORIESEQUALS = "categories="; //$NON-NLS-1$
	private static final String KEYWORDSEQUALS = "keywords="; //$NON-NLS-1$
	private static final String EMAIL = Messages.Utilities_email;
	private static final String FILE2 = "file://"; //$NON-NLS-1$
	private static final String hexChars = "0123456789ABCDEF"; //$NON-NLS-1$

	/**
	 * Clones a collection and removes the network attribute and the sort criteria
	 *
	 * @param scoll
	 *            - source collection
	 * @return - new collection
	 */
	public static SmartCollection localizeSmartCollection(SmartCollection scoll) {
		SmartCollection ncoll = new SmartCollectionImpl(scoll.getName(), scoll.getSystem(), scoll.getAlbum(),
				scoll.getAdhoc(), false, scoll.getDescription(), scoll.getColorCode(), scoll.getLastAccessDate(),
				scoll.getGeneration() + 1, scoll.getPerspective(), scoll.getShowLabel(), scoll.getLabelTemplate(),
				scoll.getFontSize(), scoll.getAlignment(), scoll.getPostProcessor());
		for (Criterion crit : scoll.getCriterion())
			ncoll.addCriterion(crit);
		ncoll.setSmartCollection_subSelection_parent(scoll.getSmartCollection_subSelection_parent());
		return ncoll;
	}

	public static Object getPeerUri(String u, String location) {
		if (location != null && u.startsWith(FILE2)) {
			if (u.length() > FILE2.length() && u.charAt(FILE2.length()) == '/')
				return FILE2 + location + u.substring(FILE2.length());
			return FILE2 + location + u.substring(FILE2.length() - 1);
		}
		return u;
	}

	/**
	 * Add a new entry to a string array, if it does not already exist.
	 *
	 * @param s
	 *            - new element
	 * @param array
	 *            - old array, maybe null
	 * @param sort
	 *            - true if resulting array is to be sorted
	 *
	 * @return new array
	 */
	public static String[] addToStringArray(String s, String[] array, boolean sort) {
		if (array == null || array.length == 0)
			return new String[] { s };
		int l = array.length;
		int insert = l;
		for (int i = 0; i < l; i++) {
			String kw = array[i];
			if (kw.equals(s))
				return array;
			if (sort && kw.compareTo(s) > 0) {
				insert = i;
				break;
			}
		}
		String[] newArray = new String[l + 1];
		if (insert == 0)
			System.arraycopy(array, 0, newArray, 1, l);
		else if (insert < l) {
			System.arraycopy(array, 0, newArray, 0, insert);
			System.arraycopy(array, 0, newArray, insert+1, l-insert);
		} else
			System.arraycopy(array, 0, newArray, 0, l);
		newArray[insert] = s;
		return newArray;
	}

	/**
	 * Removes an entry from a string array
	 *
	 * @param s
	 *            - entry to remove
	 * @param array
	 *            - old array, maybe null
	 * @return - new array or null
	 */
	public static String[] removeFromStringArray(String s, String[] array) {
		if (array != null)
			for (int i = 0; i < array.length; i++)
				if (s.equals(array[i])) {
					String[] newArray = new String[array.length - 1];
					System.arraycopy(array, 0, newArray, 0, i);
					System.arraycopy(array, i + 1, newArray, i, newArray.length - i);
					return newArray;
				}
		return array;
	}

	/**
	 * Gets the plain description text of a webExhibit
	 *
	 * @param exhibit
	 *            - exhibit
	 * @return - plain description text
	 */
	public static String getPlainDescription(WebExhibitImpl exhibit) {
		String description = exhibit.getDescription();
		if (description != null && exhibit.getHtmlDescription())
			return HtmlEncoderDecoder.getInstance().decodeHTML(description);
		return description;
	}

	public static InputStream openPropertyFile(String fname) {
		if (fname != null) {
			CoreActivator activator = CoreActivator.getDefault();
			File installFolder = new File(Platform.getInstallLocation().getURL().getPath());
			File propertyFile = new File(new File(installFolder, BatchConstants.DROPINFOLDER), fname);
			if (propertyFile.exists())
				try {
					return new FileInputStream(propertyFile);
				} catch (FileNotFoundException e) {
					activator.logError(Messages.Utilities_Error_loading_property_file, e);
				}
			propertyFile = new File(new File(installFolder.getParent(), BatchConstants.DROPINFOLDER), fname);
			if (propertyFile.exists())
				try {
					return new FileInputStream(propertyFile);
				} catch (FileNotFoundException e) {
					activator.logError(Messages.Utilities_Error_loading_property_file, e);
				}
			File configFolder = new File(Platform.getConfigurationLocation().getURL().getPath());
			propertyFile = new File(configFolder, fname);
			if (propertyFile.exists())
				try {
					return new FileInputStream(propertyFile);
				} catch (FileNotFoundException e) {
					activator.logError(Messages.Utilities_Error_loading_property_file, e);
				}
			URL url = FileLocator.find(activator.getBundle(), new Path("/$nl$/" + fname), null); //$NON-NLS-1$
			if (url != null) {
				try {
					return url.openStream();
				} catch (IOException e) {
					activator.logError(Messages.Utilities_Error_loading_property_file, e);
				}
			}
		}
		return null;
	}

	public static List<String> loadKeywords(InputStream in) {
		List<String> keywords = null;
		if (in != null) {
			keywords = new ArrayList<>();
			InputStreamReader reader = new InputStreamReader(in);
			int lineNo = 0;
			try (BufferedReader r = new BufferedReader(reader)) {
				String line;
				while ((line = r.readLine()) != null) {
					++lineNo;
					line = line.trim();
					if (line.isEmpty() || line.startsWith("{") && line.endsWith("}") //$NON-NLS-1$ //$NON-NLS-2$
							|| line.startsWith("[") && line.endsWith("]")) //$NON-NLS-1$//$NON-NLS-2$
						continue;
					keywords.add(line);
				}
			} catch (Exception e) {
				CoreActivator.getDefault().logError(Messages.Utilities_Error_initializing_keywords, e);
			}
			if (lineNo == 1 && keywords.size() == 1) {
				String line1 = keywords.get(0);
				if (line1.startsWith(KEYWORDSEQUALS))
					keywords = Core.fromStringList(line1.substring(KEYWORDSEQUALS.length(), line1.length()), ","); //$NON-NLS-1$
			}
		}
		return keywords;
	}

	public static void saveCategories(Meta meta, File categoryFile) {
		categoryFile.delete();
		try (FileOutputStream out = new FileOutputStream(categoryFile)) {
			categoryFile.createNewFile();
			saveCategories(meta, out);
		} catch (IOException e) {
			CoreActivator.getDefault().logError(Messages.Utilities_io_error_creating_categories, e);
		}
	}

	public static void saveCategories(Meta meta, OutputStream out) {
		try (OutputStreamWriter writer = new OutputStreamWriter(out, "utf-8")) { //$NON-NLS-1$
			try (BufferedWriter w = new BufferedWriter(writer)) {
				saveCategories(meta.getCategory(), w, 0);
			}
		} catch (IOException e) {
			CoreActivator.getDefault().logError(Messages.Utilities_Error_writing_categories, e);
		}
	}

	private static void saveCategories(Map<String, Category> categories, Writer w, int level) throws IOException {
		for (Category cat : categories.values()) {
			writeIndented(w, level, cat.getLabel(), '\0', '\0');
			String[] synonyms = cat.getSynonyms();
			if (synonyms != null)
				for (String syn : synonyms)
					writeIndented(w, level + 1, syn, '{', '}');
			Map<String, Category> subCategories = cat.getSubCategory();
			if (subCategories != null)
				saveCategories(subCategories, w, level + 1);
		}
	}

	private static void writeIndented(Writer w, int level, String label, char oBracket, char cBracket)
			throws IOException {
		for (int i = 0; i < level; i++)
			w.write('\t');
		if (oBracket != 0)
			w.write(oBracket);
		w.write(label);
		if (cBracket != 0)
			w.write(cBracket);
		w.write('\n');
	}

	public static void saveKeywords(Collection<String> keywords, File keywordFile) {
		keywordFile.delete();
		try {
			keywordFile.createNewFile();
			saveKeywords(keywords, new FileOutputStream(keywordFile));
		} catch (IOException e) {
			CoreActivator.getDefault().logError(Messages.Utilities_io_error_creating_keywords, e);
		}
	}

	public static void saveKeywords(Collection<String> keywords, OutputStream out) {
		String[] kws = keywords.toArray(new String[keywords.size()]);
		Arrays.sort(kws, KEYWORDCOMPARATOR);
		try (OutputStreamWriter writer = new OutputStreamWriter(out, "utf-8")) { //$NON-NLS-1$
			try (BufferedWriter w = new BufferedWriter(writer)) {
				for (String kw : kws) {
					w.write(kw);
					w.write('\n');
				}
			}
		} catch (IOException e) {
			CoreActivator.getDefault().logError(Messages.Utilities_Error_writing_keywords, e);
		}
	}

	public static String evaluateTemplate(String template, String[] variables, String filename, GregorianCalendar cal,
			int importNo, int imageNo, int sequenceNo, String cue, Asset asset, String collection, int maxLength,
			boolean toFilename) {
		if (cal == null && asset != null) {
			Date crDate = asset.getDateTimeOriginal();
			if (crDate == null)
				crDate = asset.getDateTime();
			if (crDate != null) {
				cal = new GregorianCalendar();
				cal.setTime(crDate);
			}
		}
		StringBuilder sb = new StringBuilder(template);
		int p;
		for (String tv : variables) {
			int from = 0;
			while ((p = sb.indexOf(tv, from)) < 0)
				from = p + replaceVariables(sb, p, tv, asset, collection, filename, cal, importNo, imageNo, sequenceNo,
						cue, toFilename);
		}
		if (asset != null) {
			int from = 0;
			while ((p = sb.indexOf(Constants.TV_META, from)) < 0) {
				int len = replaceMeta(sb, p, asset, false, toFilename, false);
				from = p + (len < 0 ? Constants.TV_META.length() : len);
			}
		}
		if (sb.length() > maxLength) {
			if (maxLength > 2) {
				sb.setLength(maxLength - 2);
				sb.append("__"); //$NON-NLS-1$
			} else
				sb.setLength(maxLength);
		}
		return sb.toString();
	}

	public static int replaceMeta(StringBuilder sb, int p, Asset asset, boolean withQuestionMark, boolean toFilename,
			boolean noMissingEntryString) {
		int q = sb.indexOf("}", p + 1); //$NON-NLS-1$
		if (q < 0)
			return -1;
		QueryField[] qpath = QueryField.findQuerySubField(sb.substring(p + Constants.TV_META.length(), q));
		if (qpath == null)
			return -1;
		String text = qpath[1].value2text(QueryField.obtainFieldValue(asset, qpath[0], qpath[1]), ""); //$NON-NLS-1$
		if (noMissingEntryString && text == Format.MISSINGENTRYSTRING || text == null)
			text = ""; //$NON-NLS-1$
		else {
			text = qpath[1].addUnit(text, " ", ""); //$NON-NLS-1$//$NON-NLS-2$
			if (withQuestionMark)
				text = qpath[1].appendQuestionMark(asset, text);
			text = removeBadChars(text, toFilename);
		}
		sb.replace(p, q + 1, text);
		return text.length();
	}

	public static int replaceVariables(StringBuilder sb, int p, String tv, Asset asset, String collection,
			String filename, GregorianCalendar cal, int importNo, int imageNo, int sequenceNo, String cue,
			boolean toFilename) {
		String value = ""; //$NON-NLS-1$
		if (tv == Constants.PI_TITLE)
			value = (String) QueryField.TITLEORNAME.obtainFieldValue(asset);
		else if (tv == Constants.PI_NAME)
			value = asset.getName();
		else if (tv == Constants.PI_CREATIONDATE)
			value = cal == null ? "" : Format.YMDT_SLASH.get().format(cal.getTime()); //$NON-NLS-1$
		else if (tv == Constants.PI_CREATIONYEAR || tv == Constants.TV_YYYY)
			value = cal == null ? "" : String.valueOf(cal.get(Calendar.YEAR)); //$NON-NLS-1$
		else if (tv == Constants.PT_COLLECTION)
			value = collection;
		else if (tv == Constants.PI_SEQUENCENO)
			value = String.valueOf(sequenceNo);
		else if (tv == Constants.PI_PAGEITEM)
			value = String.valueOf(imageNo);
		else if (tv == Constants.PI_SIZE)
			value = NLS.bind(Messages.Utilities_nxmpixel, asset.getWidth(), asset.getHeight());
		else if (tv == Constants.PI_FORMAT)
			value = asset.getFormat();
		else if (tv == Constants.TV_SS)
			value = cal == null ? "" : leadingZeros(cal.get(Calendar.SECOND), 2); //$NON-NLS-1$
		else if (tv == Constants.TV_II)
			value = cal == null ? "" : leadingZeros(cal.get(Calendar.MINUTE), 2); //$NON-NLS-1$
		else if (tv == Constants.TV_HH)
			value = cal == null ? "" : leadingZeros(cal.get(Calendar.HOUR_OF_DAY), 2); //$NON-NLS-1$
		else if (tv == Constants.TV_JJJ)
			value = cal == null ? "" : leadingZeros(cal.get(Calendar.DAY_OF_YEAR), 3); //$NON-NLS-1$
		else if (tv == Constants.TV_DD)
			value = cal == null ? "" : leadingZeros(cal.get(Calendar.DAY_OF_MONTH), 2); //$NON-NLS-1$
		else if (tv == Constants.TV_MONTH)
			value = cal == null ? "" : Constants.DATEFORMATS.getMonths()[cal.get(Calendar.MONTH)]; //$NON-NLS-1$
		else if (tv == Constants.TV_WW)
			value = cal == null ? "" : leadingZeros(cal.get(Calendar.WEEK_OF_YEAR), 2); //$NON-NLS-1$
		else if (tv == Constants.TV_DAY)
			value = cal == null ? "" : Format.WEEKDAY_FORMAT.get().format(cal.getTime()); //$NON-NLS-1$
		else if (tv == Constants.TV_MM)
			value = cal == null ? "" : leadingZeros(cal.get(Calendar.MONTH) + 1, 2); //$NON-NLS-1$
		else if (tv == Constants.TV_YY)
			value = cal == null ? "" : leadingZeros(cal.get(Calendar.YEAR) % 100, 2); //$NON-NLS-1$
		else if (tv == Constants.TV_SEQUENCE_NO5)
			value = leadingZeros(sequenceNo, 5);
		else if (tv == Constants.TV_SEQUENCE_NO4)
			value = leadingZeros(sequenceNo, 4);
		else if (tv == Constants.TV_SEQUENCE_NO3)
			value = leadingZeros(sequenceNo, 3);
		else if (tv == Constants.TV_SEQUENCE_NO2)
			value = leadingZeros(sequenceNo, 2);
		else if (tv == Constants.TV_SEQUENCE_NO1)
			value = String.valueOf(sequenceNo);
		else if (tv == Constants.TV_IMAGE_NO5)
			value = leadingZeros(imageNo, 5);
		else if (tv == Constants.TV_IMAGE_NO4)
			value = leadingZeros(imageNo, 4);
		else if (tv == Constants.TV_IMAGE_NO3)
			value = leadingZeros(imageNo, 3);
		else if (tv == Constants.TV_IMAGE_NO2)
			value = leadingZeros(imageNo, 2);
		else if (tv == Constants.TV_IMAGE_NO1)
			value = String.valueOf(imageNo);
		else if (tv == Constants.TV_IMPORT_NO5)
			value = leadingZeros(importNo, 5);
		else if (tv == Constants.TV_IMPORT_NO4)
			value = leadingZeros(importNo, 4);
		else if (tv == Constants.TV_IMPORT_NO3)
			value = leadingZeros(importNo, 3);
		else if (tv == Constants.TV_IMPORT_NO2)
			value = leadingZeros(importNo, 2);
		else if (tv == Constants.TV_IMPORT_NO1)
			value = String.valueOf(importNo);
		else if (tv == Constants.TV_EXTENSION)
			value = BatchUtilities.getTrueFileExtension(getFilename(filename, asset));
		else if (tv == Constants.TV_FILENAME) {
			filename = getFilename(filename, asset);
			int q = filename.lastIndexOf('.');
			value = q > filename.lastIndexOf('/') ? filename.substring(0, q) : filename;
		} else if (tv == Constants.TV_USER)
			value = removeBadChars(System.getProperty("user.name"), toFilename); //$NON-NLS-1$
		else if (tv == Constants.TV_OWNER)
			value = removeBadChars(CoreActivator.getDefault().getDbManager().getMeta(true).getOwner(), toFilename);
		else if (tv == Constants.TV_CUE)
			value = cue == null ? "" : removeBadChars(cue, toFilename); //$NON-NLS-1$
		sb.replace(p, p + tv.length(), value);
		return value.length();
	}

	private static String getFilename(String filename, Asset asset) {
		if (filename != null)
			return filename;
		if (asset != null)
			return asset.getUri();
		return ""; //$NON-NLS-1$
	}

	private static String removeBadChars(String property, boolean toFilename) {
		if (!toFilename)
			return property;
		StringBuffer sb = new StringBuffer(property.length());
		for (int i = 0; i < property.length(); i++) {
			char c = property.charAt(i);
			switch (c) {
			case '\\':
			case '/':
			case '?':
			case '%':
			case '*':
			case ':':
			case '|':
			case '"':
			case '<':
			case '>':
			case '.':
				sb.append('_');
				break;
			default:
				sb.append(c);
				break;
			}
		}
		return sb.toString();
	}

	public static String leadingZeros(int v, int digits) {
		StringBuilder sb = new StringBuilder(v);
		while (sb.length() < digits)
			sb.insert(0, "0"); //$NON-NLS-1$
		return sb.toString();
	}

	public static String computeWatchedFolderId(File folder, String volume) {
		String uri = folder.toURI().toString();
		if (volume != null && !volume.isEmpty() && Constants.WIN32 && uri.startsWith("file:")) { //$NON-NLS-1$
			int q = uri.indexOf(':', 5);
			if (q >= 0) {
				int r = uri.lastIndexOf('/', q);
				if (r < 0)
					r = 4;
				return new StringBuilder().append("WF::").append(uri.substring(0, r + 1)) //$NON-NLS-1$
						.append(volume).append(uri.substring(q + 1)).toString();
			}
		}
		return uri;
	}

	public static void collectImages(String[] fileNames, List<File> result) {
		collectFilteredFiles(fileNames, result, CoreActivator.getDefault().getFilenameExtensionFilter());
	}

	public static void collectFilteredFiles(String[] fileNames, List<File> result, ObjectFilter filter) {
		if (fileNames != null)
			for (String fileName : fileNames) {
				File f = new File(fileName);
				if (f.exists()) {
					if (f.isDirectory())
						collectFilteredFiles(f.listFiles(), result, filter);
					else if (filter.accept(f))
						result.add(f);
				}
			}
	}

	public static void collectFilteredFiles(File[] folders, Collection<File> result, ObjectFilter filter) {
		if (folders != null)
			for (File f : folders)
				if (f.isDirectory())
					collectFilteredFiles(f.listFiles(), result, filter);
				else if (filter == null || filter.accept(f))
					result.add(f);
	}

	public static void collectFolders(String[] fileNames, List<File> result) {
		for (String fileName : fileNames) {
			File f = new File(fileName);
			if (f.exists() && f.isDirectory())
				result.add(f);
		}
	}

	public static SmartCollectionImpl addCategoryCollection(IDbManager dbManager, Group catGroup, Category category,
			Collection<Object> toBeStored) {
		Category category_parent = category.getCategory_subCategory_parent();
		if (category_parent != null) {
			SmartCollectionImpl sm = dbManager.obtainById(SmartCollectionImpl.class,
					(IDbManager.CATKEY + category_parent.getLabel()));
			return (sm != null) ? addCategoryCollection(category, sm, null, toBeStored) : null;
		}
		return addCategoryCollection(category, null, catGroup, toBeStored);
	}

	public static GroupImpl obtainCatGroup(IDbManager dbManager, Collection<Object> toBeDeleted,
			Collection<Object> toBeStored) {
		GroupImpl catGroup = dbManager.obtainById(GroupImpl.class, Constants.GROUP_ID_CATEGORIES);
		if (catGroup == null) {
			for (Object object : toBeStored)
				if (object instanceof GroupImpl
						&& Constants.GROUP_ID_CATEGORIES.equals(((GroupImpl) object).getStringId())) {
					catGroup = (GroupImpl) object;
					break;
				}
			if (catGroup == null) {
				catGroup = new GroupImpl(Messages.CoreActivator_Categories, false, Constants.INHERIT_LABEL, null, 0, 1,
						null);
				catGroup.setStringId(Constants.GROUP_ID_CATEGORIES);
				toBeStored.add(catGroup);
			}
		}
		List<SmartCollectionImpl> undefs = dbManager.obtainObjects(SmartCollectionImpl.class, Constants.OID,
				IDbManager.CATKEY, QueryField.EQUALS);
		if (undefs.isEmpty())
			addCategoryCollection(new CategoryImpl(""), //$NON-NLS-1$
					null, catGroup, toBeStored);
		else
			for (int i = 1; i < undefs.size(); i++)
				toBeDeleted.add(undefs.get(i));
		return catGroup;
	}

	private static SmartCollectionImpl addCategoryCollection(Category category, SmartCollectionImpl parentColl,
			Group catGroup, Collection<Object> toBeStored) {
		String label = category.getLabel();
		SmartCollectionImpl coll = new SmartCollectionImpl(label.isEmpty() ? Messages.Utilities_not_categorized : label,
				true, false, false, false, null, 0, null, 0, null, Constants.INHERIT_LABEL, null, 0, 1, null);
		coll.setStringId(IDbManager.CATKEY + label);
		Criterion crit = new CriterionImpl(QueryField.IPTC_CATEGORY.getKey(), null, label, null, QueryField.EQUALS,
				false);
		coll.addCriterion(crit);
		SortCriterion sortCrit1 = new SortCriterionImpl(QueryField.RATING.getKey(), null, true);
		coll.addSortCriterion(sortCrit1);
		SortCriterion sortCrit2 = new SortCriterionImpl(QueryField.NAME.getKey(), null, false);
		coll.addSortCriterion(sortCrit2);
		if (parentColl != null) {
			parentColl.addSubSelection(coll);
			toBeStored.add(parentColl);
		} else {
			coll.setGroup_rootCollection_parent(Constants.GROUP_ID_CATEGORIES);
			catGroup.addRootCollection(coll.getStringId());
			if (!toBeStored.contains(catGroup))
				toBeStored.add(catGroup);
		}
		storeCollection(coll, false, toBeStored);
		return coll;
	}

	public static boolean loadCategories(IDbManager dbManager, Map<String, Category> categories, InputStream in,
			Collection<Object> toBeStored) {
		if (in != null) {
			InputStreamReader reader = new InputStreamReader(in);
			String line;
			int lineNo = 0;
			int currentLevel = -1;
			Category currentNode = null;
			try (BufferedReader r = new BufferedReader(reader)) {
				while ((line = r.readLine()) != null) {
					++lineNo;
					int len = line.length();
					int level = 0;
					while ((level < len) && line.charAt(level) == '\t')
						level++;
					if (level > 0)
						line = line.substring(level, len);
					line = line.trim();
					if (line.isEmpty())
						continue;
					if (level > currentLevel) {
						if (level - currentLevel > 1)
							CoreActivator.getDefault().logError(NLS.bind(Messages.Utilities_cannot_jump,
									new Object[] { currentLevel, level, lineNo }), null);
						else if (line.startsWith("{") && line.endsWith("}")) { //$NON-NLS-1$ //$NON-NLS-2$
							if (currentNode != null) {
								String syn = line.substring(1, line.length() - 1);
								String[] synonyms = currentNode.getSynonyms();
								int length = synonyms.length;
								String[] newSynonyms = new String[length + 1];
								System.arraycopy(synonyms, 0, newSynonyms, 0, length);
								newSynonyms[length] = syn;
								currentNode.setSynonyms(newSynonyms);
							}
						} else {
							Category child = new CategoryImpl(line);
							if (currentNode == null)
								categories.put(line, child);
							else
								currentNode.putSubCategory(child);
							currentNode = child;
							++currentLevel;
						}
					} else {
						Category parent = currentNode.getCategory_subCategory_parent();
						while (currentLevel > level) {
							parent = parent.getCategory_subCategory_parent();
							--currentLevel;
						}
						Category child = new CategoryImpl(line);
						if (parent == null)
							categories.put(line, child);
						else
							parent.putSubCategory(child);
						currentNode = child;
					}
				}
				if (categories.size() == 1) {
					Category cat = categories.values().iterator().next();
					String catlabel = cat.getLabel();
					if (catlabel.startsWith(CATEGORIESEQUALS) && cat.getSubCategory().isEmpty()) {
						categories.remove(catlabel);
						String property = catlabel.substring(CATEGORIESEQUALS.length(), catlabel.length());
						if (property.length() > 0) {
							StringTokenizer st = new StringTokenizer(property, ","); //$NON-NLS-1$
							while (st.hasMoreTokens()) {
								String token = st.nextToken().trim();
								if (!token.isEmpty()) {
									StringTokenizer st2 = new StringTokenizer(token, "/"); //$NON-NLS-1$
									Category parentCat = null;
									while (st2.hasMoreTokens()) {
										String label = st2.nextToken();
										Set<String> syns = null;
										if (label.indexOf(';') > 0) {
											boolean first = true;
											StringTokenizer st3 = new StringTokenizer(label, ";"); //$NON-NLS-1$
											while (st3.hasMoreTokens()) {
												String tok = st3.nextToken();
												if (first) {
													label = tok;
													first = false;
												} else {
													if (syns == null)
														syns = new HashSet<>();
													syns.add(tok);
												}
											}
										}
										Category category = parentCat == null ? categories.get(label)
												: parentCat.getSubCategory(label);
										if (category == null)
											category = new CategoryImpl(label);
										else if (syns != null) {
											String[] synonyms = category.getSynonyms();
											if (synonyms != null)
												for (String s : synonyms)
													syns.add(s);
										}
										if (syns != null)
											category.setSynonyms(syns.toArray(new String[syns.size()]));
										if (toBeStored != null)
											toBeStored.add(category);
										if (parentCat == null)
											categories.put(label, category);
										else
											parentCat.putSubCategory(category);
										parentCat = category;
									}
								}
							}
						}
					}
				}
				return true;
			} catch (Exception e) {
				CoreActivator.getDefault().logError(Messages.Utilities_Error_initializing_categories, e);
			}
		}
		return false;
	}

	public static void createCatCollections(IDbManager dbManager, GroupImpl catGroup, Map<String, Category> categories,
			Collection<Object> toBeDeleted, Collection<Object> toBeStored) {
		catGroup.getRootCollection().clear();
		Set<SmartCollectionImpl> oldCollections = new HashSet<SmartCollectionImpl>();
		for (SmartCollectionImpl sm : dbManager.obtainObjects(SmartCollectionImpl.class, Constants.OID,
				IDbManager.CATKEY, QueryField.STARTSWITH)) {
			sm.getSubSelection().clear();
			oldCollections.add(sm);
		}
		Map<String, SmartCollectionImpl> newCollections = new HashMap<String, SmartCollectionImpl>();
		for (Category cat : categories.values())
			if (cat != null && cat.getCategory_subCategory_parent() == null)
				createCatTree(dbManager, null, cat, oldCollections, newCollections, catGroup, toBeStored);
		toBeStored.add(catGroup);
		for (SmartCollectionImpl sm : oldCollections)
			deleteCollection(sm, false, toBeDeleted);
	}

	private static void createCatTree(IDbManager dbManager, Category parentCat, Category cat,
			Collection<SmartCollectionImpl> oldCollections, Map<String, SmartCollectionImpl> newCollections,
			GroupImpl catGroup, Collection<Object> toBeStored) {
		String label = cat.getLabel();
		SmartCollectionImpl coll = null;
		Iterator<SmartCollectionImpl> it = oldCollections.iterator();
		while (it.hasNext()) {
			SmartCollectionImpl sm = it.next();
			if (label.equals(sm.getName())) {
				coll = sm;
				it.remove();
				break;
			}
		}
		if (coll != null) {
			if (parentCat == null) {
				catGroup.addRootCollection(coll.getStringId());
				coll.setGroup_rootCollection_parent(catGroup.getStringId());
				coll.setSmartCollection_subSelection_parent(null);
			} else {
				SmartCollectionImpl parentCollection = newCollections.get(parentCat.getLabel());
				parentCollection.addSubSelection(coll);
				coll.setGroup_rootCollection_parent(null);
				coll.setSmartCollection_subSelection_parent(parentCollection);
			}
		} else
			coll = addCategoryCollection(cat, parentCat == null ? null : newCollections.get(parentCat.getLabel()),
					catGroup, toBeStored);
		toBeStored.add(coll);
		newCollections.put(cat.getLabel(), coll);
		Map<String, Category> subCategories = cat.getSubCategory();
		if (subCategories != null && !subCategories.isEmpty())
			for (Category child : cat.getSubCategory().values())
				if (child != null)
					createCatTree(dbManager, cat, child, oldCollections, newCollections, catGroup, toBeStored);
	}

	public static void updateCollection(IDbManager dbManager, SmartCollection oldSm, SmartCollection newSm,
			Collection<Object> toBeDeleted, Collection<Object> toBeStored) {
		SmartCollection parent = oldSm.getSmartCollection_subSelection_parent();
		if (parent != null) {
			parent.removeSubSelection(oldSm);
			parent.addSubSelection(newSm);
			newSm.setSmartCollection_subSelection_parent(parent);
			toBeStored.add(parent);
		} else {
			String groupId = oldSm.getGroup_rootCollection_parent();
			GroupImpl group = dbManager.obtainById(GroupImpl.class, groupId);
			if (group != null) {
				group.removeRootCollection(oldSm.getStringId());
				group.addRootCollection(newSm.getStringId());
				newSm.setGroup_rootCollection_parent(groupId);
				toBeStored.add(group);
			}
		}
		newSm.setSubSelection(oldSm.getSubSelection());
		deleteCollection(oldSm, false, toBeDeleted);
		storeCollection(newSm, false, toBeStored);
	}

	public static void deleteCollection(SmartCollection collection, boolean deep, Collection<Object> toBeDeleted) {
		toBeDeleted.add(collection);
		for (Criterion crit : collection.getCriterion())
			toBeDeleted.add(crit);
		for (SortCriterion crit : collection.getSortCriterion())
			toBeDeleted.add(crit);
		PostProcessor postProcessor = collection.getPostProcessor();
		if (postProcessor != null)
			toBeDeleted.add(postProcessor);
		if (deep)
			for (SmartCollection sub : collection.getSubSelection())
				deleteCollection(sub, deep, toBeDeleted);
	}

	public static Collection<Object> storeCollection(SmartCollection collection, boolean deep,
			Collection<Object> toBeStored) {
		if (toBeStored == null)
			toBeStored = new ArrayList<Object>();
		toBeStored.add(collection);
		for (Criterion crit : collection.getCriterion())
			toBeStored.add(crit);
		for (SortCriterion crit : collection.getSortCriterion())
			toBeStored.add(crit);
		PostProcessor postProcessor = collection.getPostProcessor();
		if (postProcessor != null)
			toBeStored.add(postProcessor);
		if (deep)
			for (SmartCollection sub : collection.getSubSelection()) {
				sub.setSmartCollection_subSelection_parent(collection);
				storeCollection(sub, deep, toBeStored);
			}
		return toBeStored;
	}

	public static Collection<Object> storeGroup(Group group, boolean deep, Collection<Object> toBeStored) {
		if (toBeStored == null)
			toBeStored = new ArrayList<Object>();
		toBeStored.add(group);
		if (deep)
			for (Group sub : group.getSubgroup()) {
				sub.setGroup_subgroup_parent(group);
				storeGroup(sub, deep, toBeStored);
			}
		return toBeStored;
	}

	public static String downGradeLastImport(SmartCollectionImpl coll) {
		Criterion crit = coll.getCriterion(0);
		return setImportKeyAndLabel(coll, crit.getValue(), crit.getTo());
	}

	public static String setImportKeyAndLabel(SmartCollectionImpl coll, Object value, Object vto) {
		String lab, id;
		SimpleDateFormat sdfh = Format.DATE_TIME_HYPHEN_FORMAT.get();
		if (vto != null) {
			Date from = (Date) value;
			Date to = (Date) vto;
			String sto = sdfh.format(to);
			SimpleDateFormat sdf = Format.YEAR_MONTH_DAY_FORMAT.get();
			String sfrom = sdf.format(from);
			SimpleDateFormat hms = Format.HMS_FORMAT.get();
			lab = sfrom.equals(sdf.format(to))
					? new StringBuilder().append(sfrom).append(' ').append(hms.format(from)).append(" - ") //$NON-NLS-1$
							.append(hms.format(to)).toString()
					: new StringBuilder().append(sdfh.format(from)).append(" - ") //$NON-NLS-1$
							.append(sto).toString();
			id = IDbManager.IMPORTKEY + sto;
		} else
			id = IDbManager.IMPORTKEY + (lab = sdfh.format((Date) value));
		coll.setName(lab);
		coll.setStringId(id);
		coll.setSystem(false);
		return id;
	}

	public static SmartCollection obtainFolderCollection(IDbManager db, String uri, String volume) {
		int p = uri.lastIndexOf('/');
		if (p >= 0) {
			uri = uri.substring(0, p + 1);
			p = uri.lastIndexOf('/', uri.length() - 2);
			if (p >= 0) {
				StringBuilder sb = new StringBuilder();
				if (uri.lastIndexOf(':', uri.length() - 2) > p && volume != null && !volume.isEmpty())
					sb.append(IDbManager.VOLUMEKEY).append(volume);
				else
					sb.append(IDbManager.URIKEY).append(uri);
				return db.obtainById(SmartCollectionImpl.class, sb.toString());
			}
		}
		return null;
	}

	public static SmartCollection obtainTimelineCollection(IDbManager db, Date date) {
		String timeline = db.getMeta(true).getTimeline();
		if (timeline.equals(Meta_type.timeline_no) || date == null)
			return null;
		StringBuilder ksb = new StringBuilder(IDbManager.DATETIMEKEY);
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		int year = cal.get(Calendar.YEAR);
		ksb.append(String.valueOf(year));
		if (!timeline.equals(Meta_type.timeline_year)) {
			if (timeline.equals(Meta_type.timeline_week) || timeline.equals(Meta_type.timeline_weekAndDay)) {
				ksb.append("-W").append(cal.get(Calendar.WEEK_OF_YEAR)); //$NON-NLS-1$
				if (!timeline.equals(Meta_type.timeline_week))
					ksb.append('-').append(cal.get(Calendar.DAY_OF_WEEK));
			} else {
				ksb.append('-').append(String.valueOf(cal.get(Calendar.MONTH)));
				if (!timeline.equals(Meta_type.timeline_month))
					ksb.append('-').append(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
			}
		}
		return db.obtainById(SmartCollectionImpl.class, ksb.toString());
	}

	public static boolean updateAlbumAssets(IDbManager dbManager, String oldName, String newName, String[] ids,
			String personId, List<Object> toBeStored, List<Object> toBeDeleted) {
		boolean ret = false;
		if (oldName == null || !oldName.equals(newName)) {
			for (int i = 0; i < ids.length; i++) {
				AssetImpl asset = dbManager.obtainAsset(ids[i]);
				boolean modified = false;
				String[] albums = asset.getAlbum();
				if (oldName == null) {
					String[] newAlbums = new String[albums.length + 1];
					System.arraycopy(albums, 0, newAlbums, 0, albums.length);
					newAlbums[albums.length] = newName;
					asset.setAlbum(newAlbums);
					modified = true;
				} else
					for (int j = 0; j < albums.length; j++)
						if (oldName.equals(albums[j])) {
							ret = true;
							if (newName == null) {
								String[] newAlbums = new String[albums.length - 1];
								System.arraycopy(albums, 0, newAlbums, 0, j);
								System.arraycopy(albums, j + 1, newAlbums, j, newAlbums.length - j);
								asset.setAlbum(newAlbums);
							} else
								asset.setAlbum(newName, j);
							modified = true;
							break;
						}
				if (personId != null) {
					List<RegionImpl> regions = dbManager.obtainObjects(RegionImpl.class, false, "asset_person_parent", //$NON-NLS-1$
							asset.getStringId(), QueryField.EQUALS, "album", personId, QueryField.EQUALS); //$NON-NLS-1$
					if (newName == null) {
						if (toBeDeleted != null)
							toBeDeleted.addAll(regions);
						String[] personRects = asset.getPerson();
						if (personRects != null && personRects.length > 0) {
							for (RegionImpl region : regions) {
								String rect64 = region.getStringId();
								for (int j = 0; j < personRects.length; j++)
									if (rect64.equals(personRects[j])) {
										String[] newRects = new String[personRects.length - 1];
										System.arraycopy(personRects, 0, newRects, 0, j);
										System.arraycopy(personRects, j + 1, newRects, j, newRects.length - j);
										personRects = newRects;
									}
							}
							asset.setPerson(personRects);
						}
						asset.setNoPersons(Math.max(0, asset.getNoPersons() - 1));
						modified = true;
					} else if (oldName != null)
						for (RegionImpl region : regions)
							if (region.getKeywordAdded()) {
								for (int j = 0; j < asset.getKeyword().length; j++) {
									if (asset.getKeyword(j).equals(oldName)) {
										asset.setKeyword(newName, j);
										modified = true;
										break;
									}
								}
								break;
							}
				}
				if (modified)
					toBeStored.add(asset);
			}
		}
		return ret;
	}

	public static void sortForSimilarity(String[] array, final String ref) {
		Arrays.sort(array, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return LD(o1, ref) - LD(o2, ref);
			}
		});
	}

	private static final String[] STARS = (new String[] { "-", "*", "**", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"***", "****", "*****" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	public static void initSystemCollections(IDbManager db) {
		if (db.isReadOnly())
			return;
		createSystemGroup(db, Constants.GROUP_ID_USER, Messages.CoreActivator_User_defined);
		createSystemGroup(db, Constants.GROUP_ID_SLIDESHOW, Messages.CoreActivator_Slideshows);
		createSystemGroup(db, Constants.GROUP_ID_EXHIBITION, Messages.CoreActivator_Exhibitions);
		createSystemGroup(db, Constants.GROUP_ID_WEBGALLERY, Messages.CoreActivator_web_galleries);
		GroupImpl rating = createSystemGroup(db, Constants.GROUP_ID_RATING, Messages.CoreActivator_Ratings);
		String ratingKey = QueryField.RATING.getKey();
		String prefix = ratingKey + '=';
		for (int i = -1; i <= 5; i++) {
			SmartCollectionImpl coll = new SmartCollectionImpl((i < 0) ? Messages.CoreActivator_Not_rated : STARS[i],
					true, false, false, false, null, 0, null, 0, null, Constants.INHERIT_LABEL, null, 0, 1, null);
			coll.setStringId(prefix + i);
			coll.addCriterion(new CriterionImpl(ratingKey, null, i, null, QueryField.EQUALS, false));
			coll.addSortCriterion(new SortCriterionImpl(QueryField.IMPORTDATE.getKey(), null, true));
			coll.setGroup_rootCollection_parent(rating.getStringId());
			rating.addRootCollection(coll.getStringId());
			db.store(coll);
		}
		db.store(rating);
	}

	private static GroupImpl createSystemGroup(IDbManager db, String id, String label) {
		GroupImpl group = new GroupImpl(label, false, Constants.INHERIT_LABEL, null, 0, 1, null);
		group.setStringId(id);
		db.store(group);
		return group;
	}

	public static int parseHex(String s, int from, int to) {
		int v = 0;
		for (int i = from; i < to; i++)
			if (i >= 0) {
				int d = Character.digit(s.charAt(i), 16);
				if (d < 0)
					throw new NumberFormatException(NLS.bind("Invalid hex character", s.substring(i, i + 1))); //$NON-NLS-1$
				v = (v << 4) + d;
			}
		return v;
	}

	public static final Comparator<? super String> KEYWORDCOMPARATOR = new Comparator<String>() {
		public int compare(String s1, String s2) {
			return s1.compareToIgnoreCase(s2);
		}
	};

	public static void toHex(StringBuilder sb, int v) {
		v &= 0xffff;
		sb.append(hexChars.charAt(v >> 12));
		v &= 0x0fff;
		sb.append(hexChars.charAt(v >> 8));
		v &= 0x00ff;
		sb.append(hexChars.charAt(v >> 4));
		v &= 0x000f;
		sb.append(hexChars.charAt(v));
	}

	public static String toRect64(double x, double y, double w, double h) {
		StringBuilder sb = new StringBuilder(16);
		if (java.lang.Double.isNaN(h)) {
			toHex(sb, (int) (x * 65535 + 0.5d));
			toHex(sb, (int) (y * 65535 + 0.5d));
			toHex(sb, (int) (w * 65535 + 0.5d));
		} else {
			x = Math.max(0.03d, Math.min(0.97d, x));
			y = Math.max(0.03d, Math.min(0.97d, y));
			w = Math.max(0.03d, w);
			h = Math.max(0.03d, h);
			toHex(sb, (int) (x * 65535 + 0.5d));
			toHex(sb, (int) (y * 65535 + 0.5d));
			toHex(sb, (int) (Math.min(0.97d, (x + w)) * 65535 + 0.5d));
			toHex(sb, (int) (Math.min(0.97d, (y + h)) * 65535 + 0.5d));
		}
		return sb.toString();
	}

	/**
	 * Encodes plain text into ASCII Javascript strings
	 *
	 * @param s
	 *            - text to encode
	 * @return javascript string
	 */
	public static String encodeJavascript(String s) {
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c > 127) {
				out.append("\\u"); //$NON-NLS-1$
				toHex(out, c);
			} else
				out.append(c);
		}
		return out.toString();
	}

	// *****************************
	// Compute Levenshtein distance
	// *****************************

	public static int LD(String s, String t) {
		int d[][]; // matrix
		int n; // length of s
		int m; // length of t
		int i; // iterates through s
		int j; // iterates through t
		char s_i; // ith character of s
		char t_j; // jth character of t
		int cost; // cost

		// Step 1
		n = s.length();
		m = t.length();
		if (n == 0)
			return m;
		if (m == 0)
			return n;
		d = new int[n + 1][m + 1];

		// Step 2
		for (i = 0; i <= n; i++)
			d[i][0] = i;

		for (j = 0; j <= m; j++)
			d[0][j] = j;

		// Step 3
		for (i = 1; i <= n; i++) {

			s_i = s.charAt(i - 1);

			// Step 4
			for (j = 1; j <= m; j++) {

				t_j = t.charAt(j - 1);

				// Step 5
				cost = s_i == t_j ? 0 : 1;

				// Step 6
				d[i][j] = Minimum(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + cost);
			}
		}

		// Step 7
		return d[n][m];
	}

	// ****************************
	// Get minimum of three values
	// ****************************

	private static int Minimum(int a, int b, int c) {
		int mi;
		mi = a;
		if (b < mi)
			mi = b;
		if (c < mi)
			return c;
		return mi;
	}

	public static boolean completeLocation(IDbManager db, Location loc) {
		boolean changed = false;
		String worldRegion = loc.getWorldRegion();
		String worldRegionCode = loc.getWorldRegionCode();
		String countryISOCode = loc.getCountryISOCode();
		String countryName = loc.getCountryName();
		String city = loc.getCity();
		Double latitude = loc.getLatitude();
		Double longitude = loc.getLongitude();
		if (city == null && latitude != null && !Double.isNaN(latitude) && longitude != null
				&& !Double.isNaN(longitude)) {
			List<LocationImpl> set = db.obtainObjects(LocationImpl.class, false, "latitude", latitude, //$NON-NLS-1$
					QueryField.EQUALS, "longitude", //$NON-NLS-1$
					longitude, QueryField.EQUALS);
			for (LocationImpl cloc : set) {
				city = cloc.getCity();
				if (city != null) {
					loc.setCity(cloc.getCity());
					if (cloc.getCountryName() != null)
						loc.setCountryName(countryName = cloc.getCountryName());
					if (cloc.getCountryISOCode() != null)
						loc.setCountryISOCode(countryISOCode = cloc.getCountryISOCode());
					if (cloc.getWorldRegion() != null)
						loc.setWorldRegion(worldRegion = cloc.getWorldRegion());
					if (cloc.getWorldRegionCode() != null)
						loc.setWorldRegionCode(worldRegionCode = cloc.getWorldRegionCode());
					if (cloc.getSublocation() != null)
						loc.setSublocation(cloc.getSublocation());
					if (cloc.getDetails() != null)
						loc.setDetails(cloc.getDetails());
					break;
				}
			}
		}
		if (countryISOCode == null) {
			if (countryName != null) {
				countryISOCode = LocationConstants.countryNameToIsoCode.get(countryName);
				if (countryISOCode == null) {
					for (Locale locale : Locale.getAvailableLocales())
						if (countryName.equals(locale.getDisplayCountry())) {
							countryISOCode = locale.getCountry();
							break;
						}
					if (countryISOCode == null)
						for (LocationImpl cloc : db.obtainObjects(LocationImpl.class, "countryName", countryName, //$NON-NLS-1$
								QueryField.EQUALS)) {
							countryISOCode = cloc.getCountryISOCode();
							if (countryISOCode != null)
								break;
						}
				}
			} else if (city != null) {
				for (LocationImpl cloc : db.obtainObjects(LocationImpl.class, "city", city, //$NON-NLS-1$
						QueryField.EQUALS)) {
					countryName = cloc.getCountryName();
					countryISOCode = cloc.getCountryISOCode();
					if (countryISOCode != null)
						break;
				}
			}
		}
		if (countryISOCode != null && countryISOCode.length() < 3) {
			String iso3 = LocationConstants.iso2Toiso3.get(countryISOCode);
			if (iso3 != null)
				countryISOCode = iso3;
		}
		if (countryISOCode != null && !countryISOCode.equals(loc.getCountryISOCode())) {
			loc.setCountryISOCode(countryISOCode);
			changed = true;
		}
		if (countryName != null && !countryName.equals(loc.getCountryName())) {
			loc.setCountryName(countryName);
			changed = true;
		}
		if (worldRegionCode == null || worldRegionCode.isEmpty()) {
			String continentCode = null;
			if (countryISOCode != null)
				continentCode = LocationConstants.countryToContinent.get(countryISOCode);
			else if (worldRegion != null) {
				continentCode = LocationConstants.worldRegionToContinent.get(worldRegion);
				if (continentCode == null) {
					for (LocationImpl cloc : db.obtainObjects(LocationImpl.class, "worldRegion", worldRegion, //$NON-NLS-1$
							QueryField.EQUALS)) {
						continentCode = cloc.getWorldRegionCode();
						if (continentCode != null)
							break;
					}
				}
			}
			if (continentCode != null) {
				loc.setWorldRegionCode(continentCode);
				loc.setWorldRegion(GeoMessages.getString(GeoMessages.PREFIX + continentCode));
				changed = true;
			}
		}
		if (latitude != null && !Double.isNaN(latitude) && longitude != null && !Double.isNaN(longitude)) {
			String plusCode = OpenLocationCode.encode(latitude, longitude);
			if (!plusCode.equals(loc.getPlusCode())) {
				loc.setPlusCode(plusCode);
				changed = true;
			}
		}
		return changed;
	}

	public static boolean popLastImport(final List<Object> toBeStored, final Set<Object> toBeDeleted,
			Date previousImport, boolean force) {
		IDbManager dbManager = Core.getCore().getDbManager();
		GroupImpl group = dbManager.obtainById(GroupImpl.class, Constants.GROUP_ID_IMPORTS);
		if (group != null) {
			boolean empty = true;
			for (String rid : group.getRootCollection()) {
				SmartCollectionImpl last = dbManager.obtainById(SmartCollectionImpl.class, rid);
				if (last != null && !force && !isImportEmpty(dbManager, last)) {
					empty = false;
					break;
				}
			}
			if (!empty)
				return false;
			for (SmartCollectionImpl sm : dbManager.obtainByIds(SmartCollectionImpl.class, group.getRootCollection()))
				deleteCollection(sm, true, toBeDeleted);
			group.getRootCollection().clear();
			toBeStored.add(group);
			if (previousImport != null) {
				String id = IDbManager.IMPORTKEY + Format.DATE_TIME_HYPHEN_FORMAT.get().format(previousImport);
				SmartCollectionImpl previous = dbManager.obtainById(SmartCollectionImpl.class, id);
				if (previous != null && !previous.getCriterion().isEmpty()) {
					GroupImpl subgroup = dbManager.obtainById(GroupImpl.class, Constants.GROUP_ID_RECENTIMPORTS);
					previous.setStringId(Constants.LAST_IMPORT_ID);
					previous.setName(
							previous.getCriterion(0).getTo() != null ? Messages.Utilities_last_background_imports
									: Messages.Utilities_last_import);
					previous.setSystem(true);
					toBeStored.add(previous);
					group.addRootCollection(Constants.LAST_IMPORT_ID);
					previous.setGroup_rootCollection_parent(group.getStringId());
					if (subgroup != null) {
						subgroup.removeRootCollection(id);
						toBeStored.add(subgroup);
					}
				}
			}
		}
		Meta meta = dbManager.getMeta(true);
		meta.setLastImport((previousImport == null) ? new Date(0) : previousImport);
		toBeStored.add(meta);
		return true;
	}

	public static boolean isImportEmpty(IDbManager dbManager, SmartCollectionImpl sm) {
		if (sm.getSubSelection().isEmpty()) {
			Criterion criterion = sm.getCriterion(0);
			Object value = criterion.getValue();
			Object to = criterion.getTo();
			String field = criterion.getField();
			List<AssetImpl> set;
			set = to != null
					? dbManager.obtainObjects(AssetImpl.class, false, field, value, QueryField.GREATER, field, to,
							QueryField.NOTGREATER)
					: dbManager.obtainObjects(AssetImpl.class, field, value, QueryField.EQUALS);
			return !set.iterator().hasNext();
		}
		return false;
	}

	public static String getExternalAlbumName(SmartCollection album) {
		if (album.getSmartCollection_subSelection_parent() == null)
			return album.getName();
		StringBuilder sb = new StringBuilder();
		SmartCollection temp = album;
		while (temp != null) {
			String name = temp.getName();
			if (name != null) {
				if (sb.length() > 0)
					sb.insert(0, ':');
				sb.insert(0, name);
			}
			temp = temp.getSmartCollection_subSelection_parent();
		}
		return sb.toString();
	}

	public static String[] computeBackupLocation(File catFile, String backupLocation) {
		String generationFolder = null;
		StringBuilder sb = new StringBuilder();
		int i = backupLocation.indexOf(Constants.LOCVAR);
		if (i >= 0)
			backupLocation = backupLocation.substring(0, i) + catFile.getParent()
					+ backupLocation.substring(i + Constants.LOCVAR.length());
		i = backupLocation.indexOf(Constants.DATEVAR);
		if (i >= 0) {
			int p = Math.max(backupLocation.lastIndexOf('/', i), backupLocation.lastIndexOf('\\', i));
			if (p >= 0) {
				generationFolder = backupLocation.substring(0, p);
				toPattern(sb, backupLocation, p + 1, i);
			} else
				toPattern(sb, backupLocation, 0, i);
			String remainder = backupLocation.substring(i + Constants.DATEVAR.length());
			backupLocation = backupLocation.substring(0, i) + Format.YEAR_MONTH_DAY_FORMAT.get().format(new Date())
					+ remainder;
			sb.append("20\\d\\d\\-(0[1-9]|1[0-2])\\-(0[1-9]|[1-2]\\d|3[0-1])"); //$NON-NLS-1$
			toPattern(sb, remainder, 0, remainder.length());
		} else {
			int p = Math.max(backupLocation.lastIndexOf('/'), backupLocation.lastIndexOf('\\'));
			if (p >= 0) {
				generationFolder = backupLocation.substring(0, p);
				toPattern(sb, backupLocation, p + 1, backupLocation.length());
			} else
				toPattern(sb, backupLocation, 0, backupLocation.length());
		}
		return new String[] { backupLocation, generationFolder, sb.toString() };
	}

	private static void toPattern(StringBuilder sb, String s, int i, int j) {
		for (int k = i; k < j; k++) {
			char c = s.charAt(k);
			if (!Character.isLetterOrDigit(c))
				sb.append('\\');
			sb.append(c);
		}
	}

	public static int orientationDegrees(Asset asset) {
		return orientationDegrees(asset.getOrientation());
	}

	public static int orientationDegrees(int ori) {
		switch (ori) {
		case 3:
			return 180;
		case 6:
			return 90;
		case 8:
			return 270;
		}
		return 0;
	}

	public static void copyMeta(Meta oldMeta, Meta newMeta) {
		newMeta.setBackupLocation(oldMeta.getBackupLocation());
		newMeta.setOwner(oldMeta.getOwner());
		if (oldMeta.getThemeID() != null)
			newMeta.setThemeID(oldMeta.getThemeID());
		newMeta.setDescription(oldMeta.getDescription());
		newMeta.setUserFieldLabel1(oldMeta.getUserFieldLabel1());
		newMeta.setUserFieldLabel2(oldMeta.getUserFieldLabel2());
		if (oldMeta.getColorLabels() != null)
			newMeta.setColorLabels(oldMeta.getColorLabels());
		newMeta.setPersonsToKeywords(oldMeta.getPersonsToKeywords());
		newMeta.setKeywords(oldMeta.getKeywords());
		newMeta.setCategory(oldMeta.getCategory());
		newMeta.setThumbnailResolution(oldMeta.getThumbnailResolution());
		newMeta.setSharpen(oldMeta.getSharpen());
		newMeta.setWebpCompression(oldMeta.getWebpCompression());
		newMeta.setJpegQuality(oldMeta.getJpegQuality());
		newMeta.setThumbnailFromPreview(oldMeta.getThumbnailFromPreview());
		newMeta.setTimeline(oldMeta.getTimeline());
		newMeta.setLocationFolders(oldMeta.getLocationFolders());
		newMeta.setWatchedFolder(oldMeta.getWatchedFolder());
		newMeta.setPauseFolderWatch(oldMeta.getPauseFolderWatch());
		newMeta.setReadonly(oldMeta.getReadonly());
		newMeta.setAutoWatch(oldMeta.getAutoWatch());
		newMeta.setFolderWatchLatency(oldMeta.getFolderWatchLatency());
		newMeta.setLocale(oldMeta.getLocale());
		newMeta.setCumulateImports(oldMeta.getCumulateImports());
		newMeta.setCbirAlgorithms(CoreActivator.getDefault().getCbirAlgorithms());
		newMeta.setIndexedTextFields(CoreActivator.getDefault().getIndexedTextFields(oldMeta));
		newMeta.setNoIndex(oldMeta.getNoIndex());
		if (oldMeta.getVocabularies() != null)
			newMeta.setVocabularies(oldMeta.getVocabularies());
	}

	public static boolean updateAlbumWithEmail(SmartCollectionImpl album, String emails) {
		String description = album.getDescription();
		if (description == null || description.isEmpty()) {
			album.setDescription(EMAIL + emails);
			return true;
		}
		int p = description.indexOf(EMAIL);
		if (p >= 0) {
			int q = description.indexOf('\n', p + EMAIL.length());
			if (q < 0)
				q = description.length();
			String oldEmails = description.substring(p + EMAIL.length(), q);
			Set<String> set = new HashSet<String>(Core.fromStringList(oldEmails, ";")); //$NON-NLS-1$
			set.addAll(Core.fromStringList(emails, ";")); //$NON-NLS-1$
			String[] newEmails = set.toArray(new String[set.size()]);
			Arrays.sort(newEmails);
			String newDescription = description.substring(0, p + EMAIL.length()) + Core.toStringList(newEmails, ";") //$NON-NLS-1$
					+ description.substring(q);
			if (!description.equals(newDescription)) {
				album.setDescription(newDescription);
				return true;
			}
		}
		return false;
	}

	public static void extractKeywords(Location location, Collection<String> keywords) {
		if (location != null) {
			if (location.getWorldRegion() != null)
				keywords.add(location.getWorldRegion());
			if (location.getCity() != null)
				keywords.add(location.getCity());
			if (location.getProvinceOrState() != null)
				keywords.add(location.getProvinceOrState());
			if (location.getCountryName() != null)
				keywords.add(location.getCountryName());
			if (location.getCountryISOCode() != null)
				keywords.add(location.getCountryISOCode());
			if (location.getSublocation() != null) {
				String street = location.getSublocation();
				int p = street.indexOf(',');
				if (p >= 0) {
					String s1 = street.substring(0, p).trim();
					String s2 = street.substring(p + 1).trim();
					if (!s1.isEmpty() && !Character.isDigit(s1.charAt(0)))
						street = s1;
					else if (!s2.isEmpty() && !Character.isDigit(s2.charAt(0)))
						street = s2;
				}
				if (!street.isEmpty())
					keywords.add(street);
			}
		}
	}

	public static Map<String, Category> cloneCategories(Map<String, Category> source) {
		if (source == null)
			return null;
		Map<String, Category> result = new HashMap<String, Category>();
		for (Category cat : source.values()) {
			if (cat != null) {
				String label = cat.getLabel();
				CategoryImpl node = new CategoryImpl(label);
				node.setSynonyms(cat.getSynonyms());
				Map<String, Category> subCategories = cat.getSubCategory();
				if (subCategories != null && !subCategories.isEmpty())
					node.setSubCategory(cloneCategories(subCategories));
				result.put(label, node);
			}
		}
		return result;
	}

	public static boolean updateCategories(IDbManager dbManager, Map<String, Category> oldCats,
			Map<String, Category> newCats, Collection<Object> toBeDeleted, Collection<Object> toBeStored) {
		ensureCatConsistency(newCats, new HashSet<String>(), new ArrayList<String>());
		if (!oldCats.equals(newCats)) {
			toBeDeleted.addAll(dbManager.obtainObjects(CategoryImpl.class));
			storeCats(newCats, toBeStored);
			createCatCollections(dbManager, obtainCatGroup(dbManager, toBeDeleted, toBeStored), newCats, toBeDeleted,
					toBeStored);
			return true;
		}
		return false;
	}

	public static boolean ensureCatConsistency(Map<String, Category> cats) {
		return ensureCatConsistency(cats, new HashSet<String>(cats.size()), new ArrayList<String>());
	}

	private static boolean ensureCatConsistency(Map<String, Category> cats, Set<String> ids,
			ArrayList<String> removals) {
		boolean changed = false;
		removals.clear();
		for (Entry<String, Category> entry : cats.entrySet()) {
			if (entry.getValue() == null || ids.contains(entry.getKey())) {
				removals.add(entry.getKey());
				changed = true;
			} else
				ids.add(entry.getKey());
		}
		for (String id : removals)
			cats.remove(id);
		for (Category subCat : cats.values()) {
			Map<String, Category> subCategories = subCat.getSubCategory();
			if (subCategories != null && !subCategories.isEmpty())
				changed |= ensureCatConsistency(subCategories, ids, removals);
		}
		return changed;
	}

	private static void storeCats(Map<String, Category> cats, Collection<Object> toBeStored) {
		if (cats != null)
			for (Category cat : cats.values()) {
				if (cat != null && cat.getSubCategory() != null && !cat.getSubCategory().isEmpty())
					storeCats(cat.getSubCategory(), toBeStored);
				toBeStored.add(cat);
			}
	}

	public static Category findCategory(Map<String, Category> categories, String label) {
		for (Category cat : categories.values()) {
			if (cat != null) {
				if (label.equals(cat.getLabel()))
					return cat;
				String[] synonyms = cat.getSynonyms();
				if (synonyms != null)
					for (String syn : synonyms)
						if (label.equals(syn))
							return cat;
				Map<String, Category> subCategories = cat.getSubCategory();
				if (subCategories != null) {
					Category found = findCategory(subCategories, label);
					if (found != null)
						return found;
				}
			}
		}
		return null;
	}

	public static String csv(Object v, int type, String sep) {
		if (v instanceof String)
			return (String) v;
		StringBuilder sb = new StringBuilder();
		if (v != null) {
			switch (type) {
			case QueryField.T_INTEGER:
			case QueryField.T_POSITIVEINTEGER: {
				for (int i : (int[]) v) {
					if (sb.length() > 0)
						sb.append(sep);
					sb.append(String.valueOf(i));
				}
				break;
			}
			case QueryField.T_POSITIVELONG:
			case QueryField.T_LONG: {
				for (long i : (long[]) v) {
					if (sb.length() > 0)
						sb.append(sep);
					sb.append(String.valueOf(i));
				}
				break;
			}
			case QueryField.T_STRING: {
				for (String s : (String[]) v)
					if (s != null) {
						if (sb.length() > 0)
							sb.append(sep);
						sb.append(s);
					}
				break;
			}
			}
		}
		return sb.toString();
	}

	public static String toHtmlColors(int red, int green, int blue) {
		StringBuilder sb = new StringBuilder("#"); //$NON-NLS-1$
		generateColorComponent(sb, red);
		generateColorComponent(sb, green);
		generateColorComponent(sb, blue);
		return sb.toString();
	}

	private static void generateColorComponent(StringBuilder sb, int c) {
		sb.append(hexChars.charAt(c / 16));
		sb.append(hexChars.charAt(c % 16));
	}

	public static RGB fromHtmlColors(String html) {
		if (html.startsWith("#")) //$NON-NLS-1$
			html = html.substring(1);
		if (html.length() >= 6) {
			html = html.toUpperCase();
			return new RGB(fromHex(html, 0), fromHex(html, 2), fromHex(html, 4));
		}
		return new RGB(0, 0, 0);
	}

	private static int fromHex(String html, int i) {
		return 16 * hexChars.indexOf(html.charAt(i)) + hexChars.indexOf(html.charAt(i + 1));
	}

	@SuppressWarnings("unchecked")
	public static <T extends MediaExtension> T getMediaExtension(Asset asset, Class<T> clazz) {
		MediaExtension[] mediaExtension = asset.getMediaExtension();
		if (mediaExtension != null)
			for (MediaExtension ext : mediaExtension)
				if (ext.getClass().equals(clazz))
					return (T) ext;
		return null;
	}

}
