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

package com.bdaum.zoom.ui.internal;

import java.awt.Color;
import java.awt.Font;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.bdaum.aoModeling.runtime.AomList;
import com.bdaum.zoom.cat.model.SimilarityOptions_type;
import com.bdaum.zoom.cat.model.TextSearchOptions_type;
import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObjectImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.Region;
import com.bdaum.zoom.cat.model.asset.RegionImpl;
import com.bdaum.zoom.cat.model.creatorsContact.ContactImpl;
import com.bdaum.zoom.cat.model.group.Criterion;
import com.bdaum.zoom.cat.model.group.GroupImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.SortCriterion;
import com.bdaum.zoom.cat.model.location.LocationImpl;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.cat.model.meta.WatchedFolder;
import com.bdaum.zoom.common.CommonUtilities;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.core.IFormatter;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.ICollectionProcessor;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.core.internal.lire.Algorithm;
import com.bdaum.zoom.core.internal.peer.IPeerService;
import com.bdaum.zoom.fileMonitor.internal.filefilter.WildCardFilter;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.Icons.Icon;
import com.bdaum.zoom.ui.internal.codes.CodeParser;
import com.bdaum.zoom.ui.internal.codes.Topic;
import com.bdaum.zoom.ui.internal.views.ImageRegion;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;
import com.bdaum.zoom.ui.widgets.CGroup;

@SuppressWarnings("restriction")
public class UiUtilities {

	public static final Comparator<String> stringComparator = new Comparator<String>() {
		public int compare(String s1, String s2) {
			if (s1 == s2)
				return 0;
			if (s1 == null)
				return -1;
			if (s2 == null)
				return 1;
			return s1.compareToIgnoreCase(s2);
		}
	};

	private static final int USHRT_MAXV = 65535;
	private static final Integer MINUSONE = Integer.valueOf(-1);

	/**
	 * Finds a view in the page without restoring the view
	 * 
	 * @param page
	 *            - workbench page
	 * @param id
	 *            - view id
	 * @return - found view or null
	 */
	public static IViewPart findViewNoRestore(IWorkbenchPage page, String id) {
		IViewReference ref = page.findViewReference(id);
		return ref == null ? null : ref.getView(false);
	}

	/**
	 * Creates a labeled group
	 *
	 * @param parent
	 *            - parent composite
	 * @param columns
	 *            - number of columns within group
	 * @param label
	 *            - group label
	 * @return - group control
	 */
	public static CGroup createGroup(Composite parent, int columns, String label) {
		CGroup group = new CGroup(parent, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		group.setLayout(new GridLayout(columns, false));
		group.setText(label);
		return group;
	}

	/**
	 * Creates a tab item
	 *
	 * @param tabFolder
	 *            - tab folder
	 * @param text
	 *            - tab item label
	 * @param tooltip
	 *            - tool tip
	 * @return - tab item
	 */
	public static CTabItem createTabItem(CTabFolder tabFolder, String text, String tooltip) {
		CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);
		tabItem.setFont(JFaceResources.getBannerFont());
		tabItem.setText(' ' + text + ' ');
		if (tooltip != null)
			tabItem.setToolTipText(tooltip);
		return tabItem;
	}

	/**
	 * Creates a tab page with item
	 *
	 * @param tabFolder
	 *            - tab folder
	 * @param text
	 *            - tab item label
	 * @param tooltip
	 *            - tool tip
	 * @return - tab item
	 */
	public static Composite createTabPage(CTabFolder tabFolder, String text, String tooltip) {
		CTabItem item = createTabItem(tabFolder, text, tooltip);
		Composite comp = new Composite(tabFolder, SWT.NONE);
		item.setControl(comp);
		return comp;
	}

	public static String createSlideTitle(Asset asset) {
		return (String) QueryField.TITLEORNAME.obtainFieldValue(asset);
	}

	public static String addExplanation(QueryField qfield, String[] value, String text) {
		Object enumeration = qfield.getEnumeration();
		if (enumeration instanceof Integer) {
			CodeParser codeParser = UiActivator.getDefault().getCodeParser((Integer) enumeration);
			if (codeParser.canParse()) {
				int n = 0;
				for (String s : value) {
					Topic topic = codeParser.findTopic(s);
					if (topic != null) {
						StringBuilder sb = new StringBuilder(text).append(" (") //$NON-NLS-1$
								.append(topic.getName());
						if (++n < value.length)
							sb.append(",..."); //$NON-NLS-1$
						return sb.append(')').toString();
					}
				}
			}
		}
		return text;
	}

	public static void showFileIsOffline(Shell shell, Asset asset) {
		if (asset.getFileState() == IVolumeManager.PEER || !asset.getUri().startsWith(Constants.FILESCHEME + ':'))
			AcousticMessageDialog.openInformation(shell, Messages.UiUtilities_remote_image,
					NLS.bind(Messages.UiUtilities_the_image_is_remote, asset.getName()));
		else {
			String volume = asset.getVolume();
			if (Core.getCore().getVolumeManager().isOffline(volume))
				AcousticMessageDialog.openInformation(shell, Messages.UiUtilities_File_is_offline,
						NLS.bind(Messages.UiUtilities_File_is_offline_mount_volume, asset.getName(), volume));
			else
				AcousticMessageDialog.openWarning(shell, Messages.UiUtilities_File_does_not_exist,
						NLS.bind(Messages.UiUtilities_File_does_not_exist_delete_entry, asset.getName()));
		}
	}

	public static void showFilesAreOffline(Shell shell, List<Asset> errands, boolean all, String msgTemplate) {
		if (errands.size() == 1)
			showFileIsOffline(shell, errands.get(0));
		else {
			Set<String> volumes = new HashSet<String>();
			for (Asset asset : errands)
				if (asset.getFileState() != IVolumeManager.PEER) {
					String volume = asset.getVolume();
					if (volume != null && !volume.isEmpty())
						volumes.add(volume);
				}
			String[] vols = volumes.toArray(new String[volumes.size()]);
			Arrays.sort(vols);
			String stringlist = Core.toStringList(vols, ", "); //$NON-NLS-1$
			AcousticMessageDialog.openWarning(shell, Messages.UiUtilities_Files_missing,
					(all) ? NLS.bind(msgTemplate, Messages.UiUtilities_All, stringlist)
							: NLS.bind(msgTemplate, errands.size(), stringlist));
		}
	}

	public static String[] updateComboHistory(Combo combo) {
		String[] items = combo.getItems();
		String s = combo.getText();
		return s.isEmpty() ? items : addToHistoryList(items, s);
	}

	public static String[] addToHistoryList(String[] items, String text) {
		List<String> list = new ArrayList<>(items.length + 1);
		list.add(text);
		for (String s : items) {
			if (!s.equals(text))
				list.add(s);
			if (list.size() >= 8)
				break;
		}
		return list.toArray(new String[list.size()]);
	}

	public static String[] getComboHistory(Combo combo, char c) {
		String[] items = combo.getItems();
		List<String> list = new ArrayList<>(items.length);
		for (String s : items)
			if (s.indexOf(c) < 0)
				list.add(s);
		return list.toArray(new String[list.size()]);
	}

	public static Point snapToGrid(int x, int y, int w, int h, int yOff, int gridSize, int gridTolerance) {
		int rx = (x + gridSize / 2) / gridSize * gridSize;
		if (Math.abs(rx - x) < gridTolerance)
			x = rx;
		else {
			int newX2 = x + w;
			rx = (newX2 + gridSize / 2) / gridSize * gridSize;
			if (Math.abs(rx - newX2) < gridTolerance)
				x = rx - w;
		}
		int newY1 = yOff - y;
		int ry = (newY1 + gridSize / 2) / gridSize * gridSize;
		if (Math.abs(ry - newY1) < gridTolerance)
			newY1 = ry;
		else {
			int newY2 = newY1 - h;
			ry = (newY2 + gridSize / 2) / gridSize * gridSize;
			if (Math.abs(ry - newY2) < gridTolerance)
				newY1 = ry + h;
		}
		return new Point(x, yOff - newY1);
	}

	public static String getFilters(WatchedFolder folder) {
		String filters = folder.getFilters();
		return (filters == null) ? UiActivator.getDefault().getDefaultWatchFilters() : filters;
	}

	public static Font getAwtFont(Control control, String family, int style, int size) {
		org.eclipse.swt.graphics.Font font = null;
		while (control != null) {
			font = control.getFont();
			if (font != null)
				break;
			control = control.getParent();
		}
		if (font != null) {
			FontData fontData = font.getFontData()[0];
			int s = fontData.getStyle();
			s = (s == SWT.BOLD ? Font.BOLD : s == SWT.ITALIC ? Font.ITALIC : Font.PLAIN);
			return new Font(family != null ? family : fontData.getName(), style >= 0 ? style : s,
					size >= 0 ? size : fontData.getHeight());
		}
		return new Font(family != null ? family : "Arial", //$NON-NLS-1$
				style >= 0 ? style : Font.PLAIN, size >= 0 ? size : 9);
	}

	public static Color getAwtForeground(Control control, org.eclipse.swt.graphics.Color color) {
		RGB rgb = (color != null ? color : control.getForeground()).getRGB();
		return new Color(rgb.red, rgb.green, rgb.blue);
	}

	public static Color getAwtBackground(Control control, org.eclipse.swt.graphics.Color color) {
		RGB rgb = (color != null ? color : control.getBackground()).getRGB();
		return new Color(rgb.red, rgb.green, rgb.blue);
	}

	public static Rectangle computeFrame(String regionId, int x, int y, int width, int height, int rotation) {
		try {
			int l = regionId.length();
			int leadingZeros = l > 12 ? Math.max(0, 16 - l) : Math.max(0, 12 - l);
			int h1 = Utilities.parseHex(regionId, 0 - leadingZeros, 4 - leadingZeros);
			int h2 = Utilities.parseHex(regionId, 4 - leadingZeros, 8 - leadingZeros);
			int h3 = Utilities.parseHex(regionId, 8 - leadingZeros, 12 - leadingZeros);
			int h4 = (l <= 12) ? -1 : Utilities.parseHex(regionId, 12 - leadingZeros, 16 - leadingZeros);
			switch (rotation) {
			case 90: {
				int x1 = width * (USHRT_MAXV - h4) / USHRT_MAXV;
				int y1 = height * h1 / USHRT_MAXV;
				int y2 = height * h3 / USHRT_MAXV;
				if (h4 < 0) {
					int x2 = width * (USHRT_MAXV - h3) / USHRT_MAXV;
					return new Rectangle(x1 + x, y1 + y, x2, -1);
				}
				int x2 = width * (USHRT_MAXV - h2) / USHRT_MAXV;
				return new Rectangle(x1 + x, y1 + y, x2 - x1, y2 - y1);
			}
			case 180: {
				int x1 = width * h1 / USHRT_MAXV;
				int y1 = height * h2 / USHRT_MAXV;
				int x2 = width * h3 / USHRT_MAXV;
				if (h4 < 0) {
					int y2 = height * h3 / USHRT_MAXV;
					return new Rectangle(width - x2 + x, height - y2 + y, x, -1);
				}
				int y2 = height * h4 / USHRT_MAXV;
				return new Rectangle(width - x2 + x, height - y2 + y, x2 - x1, y2 - y1);
			}
			case 270: {
				int x1 = width * (USHRT_MAXV - h4) / USHRT_MAXV;
				int y1 = height * h1 / USHRT_MAXV;
				int y2 = height * h3 / USHRT_MAXV;
				if (h4 < 0) {
					int x2 = width * (USHRT_MAXV - h3) / USHRT_MAXV;
					return new Rectangle(width - x2 + x, height - y2 + y, x2, -1);
				}
				int x2 = width * (USHRT_MAXV - h2) / USHRT_MAXV;
				return new Rectangle(width - x2 + x, height - y2 + y, x2 - x1, y2 - y1);
			}
			default: {
				int x1 = width * h1 / USHRT_MAXV;
				int y1 = height * h2 / USHRT_MAXV;
				int x2 = width * h3 / USHRT_MAXV;
				if (h4 < 0)
					return new Rectangle(x1 + x, y1 + y, x2, -1);
				int y2 = height * h4 / USHRT_MAXV;
				return new Rectangle(x1 + x, y1 + y, x2 - x1, y2 - y1);
			}
			}
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static boolean isDoubledRegion(Collection<Rectangle> rectangles, Rectangle r1) {
		boolean c1 = r1.height < 0;
		for (Rectangle r2 : rectangles)
			if (c1 == r2.height < 0) {
				if (c1) {
					if (r2.contains(r1.x, r1.y) && r2.contains(r1.x + r1.width, r1.y + r1.width)
							|| r1.contains(r2.x, r2.y) && r1.contains(r2.x + r2.width, r2.y + r2.width))
						return true;
				} else if (r2.contains(r1.x, r1.y) && r2.contains(r1.x + r1.width, r1.y + r1.height)
						|| r1.contains(r2.x, r2.y) && r1.contains(r2.x + r2.width, r2.y + r2.height))
					return true;
			}
		return false;
	}

	public static GroupImpl obtainUserGroup(final IDbManager dbManager) {
		GroupImpl user = dbManager.obtainById(GroupImpl.class, Constants.GROUP_ID_USER);
		if (user == null) {
			user = new GroupImpl(Messages.UiUtilities_user_defined, false, Constants.INHERIT_LABEL, null, 0, 1, null);
			user.setStringId(Constants.GROUP_ID_USER);
		}
		return user;
	}

	public static String computeFieldStringValue(FieldDescriptor fd, Object value) {
		IFormatter formatter = fd.getDetailQueryField().getFormatter();
		if (formatter != null)
			return formatter.toString(value);
		if (value == null)
			return ""; //$NON-NLS-1$
		switch (fd.getDetailQueryField().getType()) {
		case QueryField.T_FLOAT:
		case QueryField.T_FLOATB:
		case QueryField.T_POSITIVEFLOAT:
			NumberFormat af = (NumberFormat.getNumberInstance());
			af.setMaximumFractionDigits(fd.getDetailQueryField().getMaxlength());
			return af.format(value);
		default:
			return String.valueOf(value);
		}
	}

	@SuppressWarnings("fallthrough")
	public static Object computeCookedValue(FieldDescriptor fd, String text) {
		IFormatter formatter = fd.getDetailQueryField().getFormatter();
		String trim = text == null ? "" : text.trim(); //$NON-NLS-1$
		boolean undefined = trim.isEmpty() || trim.equals("-"); //$NON-NLS-1$
		if (formatter != null)
			try {
				return formatter.fromString(trim);
			} catch (ParseException ex) {
				undefined = true;
			}
		try {
			switch (fd.getDetailQueryField().getType()) {
			case QueryField.T_POSITIVEINTEGER:
				if (undefined)
					return MINUSONE;
			case QueryField.T_INTEGER:
				return Integer.parseInt(trim);
			case QueryField.T_POSITIVELONG:
				if (undefined)
					return MINUSONE;
			case QueryField.T_LONG:
				return Long.parseLong(trim);
			case QueryField.T_POSITIVEFLOAT:
			case QueryField.T_FLOAT:
			case QueryField.T_FLOATB:
				if (UiConstants.INFINITE.equals(trim))
					return Double.POSITIVE_INFINITY;
				if (undefined)
					return Double.NaN;
				NumberFormat af = (NumberFormat.getNumberInstance());
				af.setMaximumFractionDigits(8);
				return af.parse(trim).doubleValue();
			case QueryField.T_CURRENCY:
				if (undefined)
					return Double.NaN;
				af = (NumberFormat.getNumberInstance());
				af.setMaximumFractionDigits(Format.getCurrencyDigits());
				return af.parse(trim).doubleValue();
			case QueryField.T_BOOLEAN:
				return Boolean.parseBoolean(trim);
			case QueryField.T_STRING:
				return text;
			}
		} catch (NumberFormatException e) {
			// do nothing
		} catch (ParseException e) {
			// do nothing
		}
		return null;
	}

	public static String verifyText(FieldDescriptor fieldDescriptor, String text, int rel) {
		String errorMessage = null;
		if (!text.isEmpty()) {
			IFormatter formatter = fieldDescriptor.getDetailQueryField().getFormatter();
			if (formatter != null)
				try {
					formatter.fromString(text);
					return null;
				} catch (ParseException ex) {
					// do nothing
				}
			String trim = text.trim();
			switch (fieldDescriptor.getDetailQueryField().getType()) {
			case QueryField.T_INTEGER:
				try {
					Integer.parseInt(text);
				} catch (NumberFormatException e1) {
					errorMessage = Messages.UiUtilities_not_a_valid_integer;
				}
				break;
			case QueryField.T_LONG:
				try {
					Long.parseLong(text);
				} catch (NumberFormatException e1) {
					errorMessage = Messages.UiUtilities_not_a_valid_integer;
				}
				break;
			case QueryField.T_POSITIVEINTEGER:
				try {
					int v = Integer.parseInt(text);
					if (v <= 0)
						errorMessage = Messages.UiUtilities_value_must_be_greater_0;
				} catch (NumberFormatException e1) {
					errorMessage = Messages.UiUtilities_not_a_valid_integer;
				}
				break;
			case QueryField.T_POSITIVELONG:
				try {
					long v = Long.parseLong(text);
					if (v <= 0)
						errorMessage = Messages.UiUtilities_value_must_be_greater_0;
				} catch (NumberFormatException e1) {
					errorMessage = Messages.UiUtilities_not_a_valid_integer;
				}
				break;
			case QueryField.T_FLOAT:
			case QueryField.T_FLOATB:
				try {
					if (!UiConstants.INFINITE.equals(trim)) {
						NumberFormat af = (NumberFormat.getNumberInstance());
						af.setMaximumFractionDigits(8);
						af.parse(text);
					}
				} catch (ParseException e1) {
					errorMessage = Messages.UiUtilities_not_a_valid_fp;
				}
				break;
			case QueryField.T_POSITIVEFLOAT:
				try {
					if (!UiConstants.INFINITE.equals(trim)) {
						NumberFormat af = (NumberFormat.getNumberInstance());
						af.setMaximumFractionDigits(8);
						double v = af.parse(text).doubleValue();
						if (v <= 0)
							errorMessage = Messages.UiUtilities_value_must_be_greater_0;
					}
				} catch (ParseException e1) {
					errorMessage = Messages.UiUtilities_not_a_valid_fp;
				}
				break;
			case QueryField.T_CURRENCY:
				try {
					NumberFormat af = (NumberFormat.getNumberInstance());
					af.setMaximumFractionDigits(Format.getCurrencyDigits());
					double v = af.parse(text).doubleValue();
					if (v <= 0)
						errorMessage = Messages.UiUtilities_value_must_be_greater_0;
				} catch (ParseException e1) {
					errorMessage = Messages.UiUtilities_not_a_valid_fp;
				}
				break;
			case QueryField.T_STRING:
				if (rel == QueryField.WILDCARDS || rel == QueryField.NOTWILDCARDS)
					errorMessage = WildCardFilter.validate(text);
				else {
					int maxLength = fieldDescriptor.getDetailQueryField().getMaxlength();
					if (maxLength >= 0 && text.length() > maxLength)
						errorMessage = NLS.bind(Messages.UiUtilities_string_too_long, maxLength);
				}
				break;
			}
		}
		return errorMessage;
	}

	// private static void compileCategories(Map<String, Category> categories,
	// Set<String> result) {
	// for (Category cat : categories.values())
	// if (cat != null) {
	// String label = cat.getLabel();
	// result.add(label);
	// result.add(label);
	// compileCategories(cat.getSubCategory(), result);
	// }
	// }
	//
	public static Set<String> getValueProposals(IDbManager db, String field, String subfield) {
		Set<String> result = null;
		Meta meta = db.getMeta(false);
		if (meta != null) {
			QueryField qfield = QueryField.findQuerySubField(field, subfield);
			// if (qfield == QueryField.IPTC_KEYWORDS)
			// return meta.getKeywords();
			// if (qfield == QueryField.IPTC_CATEGORY || qfield ==
			// QueryField.IPTC_SUPPLEMENTALCATEGORIES)
			// compileCategories(meta.getCategory(), result = new HashSet<String>(30));
			// else
			if (qfield == QueryField.LOCATION_WORLDREGION) {
				result = new HashSet<String>(10);
				for (LocationImpl loc : db.obtainObjects(LocationImpl.class)) {
					String worldRegion = loc.getWorldRegion();
					if (worldRegion != null && !worldRegion.isEmpty())
						result.add(worldRegion);
				}
			} else if (qfield == QueryField.LOCATION_WORLDREGIONCODE) {
				result = new HashSet<String>(10);
				for (LocationImpl loc : db.obtainObjects(LocationImpl.class)) {
					String worldRegionCode = loc.getWorldRegionCode();
					if (worldRegionCode != null && !worldRegionCode.isEmpty())
						result.add(worldRegionCode);
				}
			} else if (qfield == QueryField.LOCATION_COUNTRYNAME) {
				result = new HashSet<String>(100);
				for (LocationImpl loc : db.obtainObjects(LocationImpl.class)) {
					String country = loc.getCountryName();
					if (country != null && !country.isEmpty())
						result.add(country);
				}
			} else if (qfield == QueryField.LOCATION_COUNTRYCODE) {
				result = new HashSet<String>(100);
				for (LocationImpl loc : db.obtainObjects(LocationImpl.class)) {
					String code = loc.getCountryISOCode();
					if (code != null && !code.isEmpty())
						result.add(code);
				}
			} else if (qfield == QueryField.LOCATION_STATE) {
				result = new HashSet<String>(100);
				for (LocationImpl loc : db.obtainObjects(LocationImpl.class)) {
					String state = loc.getProvinceOrState();
					if (state != null && !state.isEmpty())
						result.add(state);
				}
			} else if (qfield == QueryField.LOCATION_CITY) {
				result = new HashSet<String>(200);
				for (LocationImpl loc : db.obtainObjects(LocationImpl.class)) {
					String city = loc.getCity();
					if (city != null && !city.isEmpty())
						result.add(city);
				}
			} else if (qfield == QueryField.LOCATION_SUBLOCATION) {
				result = new HashSet<String>(300);
				for (LocationImpl loc : db.obtainObjects(LocationImpl.class)) {
					String sub = loc.getSublocation();
					if (sub != null && !sub.isEmpty())
						result.add(sub);
				}
			} else if (qfield == QueryField.CONTACT_COUNTRY) {
				result = new HashSet<String>(100);
				for (ContactImpl contact : db.obtainObjects(ContactImpl.class)) {
					String country = contact.getCountry();
					if (country != null && !country.isEmpty())
						result.add(country);
				}
			} else if (qfield == QueryField.CONTACT_STATE) {
				result = new HashSet<String>(50);
				for (ContactImpl contact : db.obtainObjects(ContactImpl.class)) {
					String state = contact.getState();
					if (state != null && !state.isEmpty())
						result.add(state);
				}
			} else if (qfield == QueryField.CONTACT_CITY) {
				result = new HashSet<String>(50);
				for (ContactImpl contact : db.obtainObjects(ContactImpl.class)) {
					String city = contact.getCity();
					if (city != null && !city.isEmpty())
						result.add(city);
				}
			} else if (qfield == QueryField.CONTACT_POSTALCODE) {
				result = new HashSet<String>(50);
				for (ContactImpl contact : db.obtainObjects(ContactImpl.class)) {
					String zip = contact.getPostalCode();
					if (zip != null && !zip.isEmpty())
						result.add(zip);
				}
			} else if (qfield == QueryField.CONTACT_EMAIL) {
				result = new HashSet<String>(50);
				for (ContactImpl contact : db.obtainObjects(ContactImpl.class)) {
					String[] emails = contact.getEmail();
					if (emails != null)
						for (String email : emails)
							if (email != null && !email.isEmpty())
								result.add(email);
				}
			} else if (qfield == QueryField.CONTACT_WEBURL) {
				result = new HashSet<String>(50);
				for (ContactImpl contact : db.obtainObjects(ContactImpl.class)) {
					String[] urls = contact.getWebUrl();
					if (urls != null)
						for (String url : urls)
							if (url != null && !url.isEmpty())
								result.add(url);
				}
			} else if (qfield == QueryField.CONTACT_ADDRESS) {
				result = new HashSet<String>(50);
				for (ContactImpl contact : db.obtainObjects(ContactImpl.class)) {
					String[] lines = contact.getAddress();
					if (lines != null)
						for (String line : lines)
							if (line != null && !line.isEmpty())
								result.add(line);
				}
			} else if (qfield == QueryField.ARTWORKOROBJECT_COPYRIGHT) {
				result = new HashSet<String>(200);
				for (ArtworkOrObjectImpl art : db.obtainObjects(ArtworkOrObjectImpl.class)) {
					String copy = art.getCopyrightNotice();
					if (copy != null && !copy.isEmpty())
						result.add(copy);
				}
			} else if (qfield == QueryField.ARTWORKOROBJECT_INVENTORYNUMBER) {
				result = new HashSet<String>(200);
				for (ArtworkOrObjectImpl art : db.obtainObjects(ArtworkOrObjectImpl.class)) {
					String no = art.getSourceInventoryNumber();
					if (no != null && !no.isEmpty())
						result.add(no);
				}
			} else if (qfield == QueryField.ARTWORKOROBJECT_TITLE) {
				result = new HashSet<String>(200);
				for (ArtworkOrObjectImpl art : db.obtainObjects(ArtworkOrObjectImpl.class)) {
					String title = art.getTitle();
					if (title != null && !title.isEmpty())
						result.add(title);
				}
			} else if (qfield == QueryField.ARTWORKOROBJECT_SOURCE) {
				result = new HashSet<String>(200);
				for (ArtworkOrObjectImpl art : db.obtainObjects(ArtworkOrObjectImpl.class)) {
					String copy = art.getSource();
					if (copy != null && !copy.isEmpty())
						result.add(copy);
				}
			} else if (qfield == QueryField.ARTWORKOROBJECT_CREATOR) {
				result = new HashSet<String>(200);
				for (ArtworkOrObjectImpl art : db.obtainObjects(ArtworkOrObjectImpl.class)) {
					String[] creators = art.getCreator();
					if (creators != null)
						for (String creator : creators)
							if (creator != null && !creator.isEmpty())
								result.add(creator);
				}
			}
		}
		return result;
	}

	public static String[] getValueProposals(IDbManager dbManager, QueryField qfield, QueryField subfield,
			boolean networked) {
		String ticket = null;
		IPeerService peerService = networked ? Core.getCore().getDbFactory().getPeerService() : null;
		try {
			if (peerService != null)
				ticket = peerService.askForValueProposals(qfield.getKey(), subfield == null ? null : subfield.getKey());
			Set<String> valueProposals = getValueProposals(dbManager, qfield.getKey(),
					subfield == null ? null : subfield.getKey());
			if (valueProposals == null)
				return null;
			if (ticket != null)
				valueProposals.addAll(peerService.getProposals(ticket));
			String[] array = valueProposals.toArray(new String[valueProposals.size()]);
			Arrays.sort(array, stringComparator);
			return array;
		} finally {
			if (ticket != null)
				peerService.discardTask(ticket);
		}
	}

	public static String composeContentDescription(SmartCollection sm, String sep, boolean compact, boolean sort) {
		StringBuilder sb = new StringBuilder();
		AomList<Criterion> criteria = sm.getCriterion();
		for (Criterion crit : criteria) {
			String field = crit.getField();
			Object value = crit.getValue();
			if (ICollectionProcessor.SIMILARITY.equals(field)) {
				if (compact)
					return sm.getName();
				SimilarityOptions_type options = (SimilarityOptions_type) value;
				if (options != null) {
					Algorithm algo = Core.getCore().getDbFactory().getLireService(true)
							.getAlgorithmById(options.getMethod());
					return NLS.bind(Messages.UiUtilities_based_on_similarity,
							new Object[] { (int) (options.getMinScore() * 100f + 0.5f), options.getMaxResults(), sep,
									algo == null ? "" : " (" + algo.getLabel() + ')' }); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			if (ICollectionProcessor.TEXTSEARCH.equals(field)) {
				TextSearchOptions_type options = (TextSearchOptions_type) value;
				if (options != null)
					return compact ? NLS.bind(Messages.UiUtilities_text_search2, options.getQueryString())
							: NLS.bind(Messages.UiUtilities_text_search, new Object[] { options.getQueryString(),
									(int) (options.getMinScore() * 100f + 0.5f), options.getMaxResults(), sep });
			}
			QueryField qfield = QueryField.findQueryField(field);
			if (qfield != null) {
				if (qfield == QueryField.IPTC_DATECREATED) {
					SimpleDateFormat df = compact ? Format.MDY_TIME_SHORT_FORMAT.get()
							: Format.EMDY_TIME_LONG_FORMAT.get();
					if (crit.getRelation() == QueryField.EQUALS && value instanceof Date)
						return NLS.bind(Messages.UiUtilities_time_search, df.format((Date) value));
					else if (crit.getRelation() == QueryField.BETWEEN && crit.getTo() != null) {
						if (crit.getValue() instanceof Date && crit.getTo() instanceof Date)
							return NLS.bind(Messages.UiUtilities_time_search2, df.format((Date) crit.getValue()),
									df.format((Date) crit.getTo()));
					}
				}
				if (sb.length() > 0)
					sb.append(crit.getAnd() ? Messages.UiUtilities_and : Messages.UiUtilities_or);
				sb.append(qfield.getLabel());
				String subfield = crit.getSubfield();
				if (subfield != null) {
					QueryField qsub = QueryField.findQuerySubField(field, subfield);
					if (qsub != null)
						sb.append(' ').append(qsub.getLabel());
				}
				sb.append(' ');
				int relation = crit.getRelation();
				for (int k = 0; k < QueryField.ALLRELATIONS.length; k++) {
					int rel = QueryField.ALLRELATIONS[k];
					if ((rel & relation) != 0) {
						sb.append(QueryField.ALLRELATIONLABELS[k]);
						break;
					}
				}
				sb.append(" \""); //$NON-NLS-1$
				sb.append(value);
				sb.append('"');
			}
		}
		if (sort)
			composeSortDescription(sm, sb, true);
		return sb.toString();
	}

	public static void composeSortDescription(SmartCollection sm, StringBuilder sb, boolean upperCase) {
		List<SortCriterion> sortCriterion = sm.getSortCriterion();
		if (!sortCriterion.isEmpty()) {
			String msg = Messages.UiUtilities_sorted_by;
			if (upperCase)
				sb.append(' ').append(msg.toUpperCase());
			else
				sb.append(msg);
			boolean and = false;
			for (SortCriterion sc : sortCriterion) {
				String field = sc.getField();
				QueryField qfield = QueryField.findQueryField(field);
				if (qfield != null) {
					if (and)
						sb.append(Messages.UiUtilities_and);
					else
						and = true;
					sb.append(qfield.getLabel());
					String subfield = sc.getSubfield();
					if (subfield != null) {
						QueryField qsub = QueryField.findQuerySubField(field, subfield);
						if (qsub != null)
							sb.append(' ').append(qsub.getLabel());
					}
					sb.append(sc.getDescending() ? Messages.UiUtilities_descending : Messages.UiUtilities_ascending);
				}
			}
		}
	}

	public static Icon getSmartCollectionIcon(SmartCollection coll) {
		if (coll.getAdhoc()) {
			String field = coll.getCriterion(0).getField();
			if (ICollectionProcessor.SIMILARITY.equals(field))
				return Icons.similar;
			if (ICollectionProcessor.TEXTSEARCH.equals(field))
				return Icons.textsearch;
			QueryField qfield = QueryField.findQueryField(field);
			if (qfield == QueryField.IPTC_DATECREATED)
				return Icons.time;
			if (qfield == QueryField.EXIF_GPSLOCATIONDISTANCE)
				return Icons.proximity;
			if (qfield == QueryField.IPTC_KEYWORDS)
				return Icons.goggles;
			return Icons.user_magnify;
		}
		if (coll.getSystem()) {
			if (coll.getStringId().startsWith(IDbManager.CATKEY))
				return Icons.folder_page;
			if (coll.getStringId().startsWith(IDbManager.RATINGKEY))
				return Icons.folder_star;
			if (coll.getStringId().startsWith(IDbManager.URIKEY))
				return Icons.folder_page_white;
			if (coll.getStringId().startsWith(IDbManager.VOLUMEKEY))
				return Icons.folder_database;
			if (coll.getStringId().startsWith(IDbManager.DATETIMEKEY))
				return Icons.folder_clock;
			if (coll.getStringId().startsWith(IDbManager.LOCATIONKEY))
				return Icons.folder_world;
			if (coll.getStringId().equals(Constants.LAST_IMPORT_ID))
				return Icons.lastImport;
			return coll.getAlbum() ? Icons.folder_person : Icons.folder_image;
		}
		if (!coll.getAlbum() && coll.getNetwork())
			return coll.getSmartCollection_subSelection_parent() == null ? Icons.folder_add_network
					: Icons.folder_and_network;
		if (coll.getStringId().startsWith(IDbManager.IMPORTKEY)) {
			if (!coll.getCriterion().isEmpty()) {
				Criterion crit = coll.getCriterion().get(0);
				if (crit != null && crit.getAnd())
					return Icons.folder_tethered;
			}
			return Icons.folder_import;
		}
		return coll.getAlbum() ? Icons.folder_album
				: coll.getSmartCollection_subSelection_parent() == null ? Icons.folder_add : Icons.folder_and;
	}

	public static boolean isImport(SmartCollection sm) {
		if (sm == null)
			return false;
		String group = sm.getGroup_rootCollection_parent();
		return (Constants.GROUP_ID_IMPORTS.equals(group) || Constants.GROUP_ID_RECENTIMPORTS.equals(group));
	}

	public static ArrayList<ImageRegion> drawRegions(GC gc, Asset asset, int xs, int ys, int width, int height,
			boolean selected, int showRegions, boolean onlyFaces, Object persId) {
		Device device = gc.getDevice();
		String[] regionIds = asset.getPerson();
		ArrayList<ImageRegion> regions = null;
		Set<Rectangle> rectangleOrOvalList = new HashSet<Rectangle>();
		if (regionIds != null && regionIds.length > 0) {
			int rotation = asset.getRotation();
			regions = new ArrayList<ImageRegion>(regionIds.length);
			gc.setClipping(xs, ys, width, height);
			int i = 0;
			boolean[] done = null;
			if (selected) {
				IDbManager dbManager = Core.getCore().getDbManager();
				for (RegionImpl region : dbManager.obtainObjects(RegionImpl.class, "asset_person_parent", //$NON-NLS-1$
						asset.getStringId(), QueryField.EQUALS)) {
					if (onlyFaces && region.getType() != null && !region.getType().equals(Region.type_face))
						continue;
					if (i++ > showRegions)
						break;
					String id = region.getStringId();
					Rectangle frame = computeFrame(id, xs, ys, width, height, rotation);
					if (frame != null && !isDoubledRegion(rectangleOrOvalList, frame)) {
						for (int j = 0; j < regionIds.length; j++)
							if (regionIds[j].equals(id)) {
								if (done == null)
									done = new boolean[regionIds.length];
								done[j] = true;
							}
						rectangleOrOvalList.add(frame);
						gc.setForeground(JFaceResources.getColorRegistry().get(Constants.APPCOLOR_REGION_FACE));
						if (frame.height < 0) {
							int d = frame.width;
							gc.drawOval(frame.x - d / 2, frame.y - d / 2, d, d);
						} else
							gc.drawRectangle(frame);
						String type = region.getType();
						String name = null;
						int color = SWT.COLOR_RED;
						if (type == null || type.isEmpty() || Region.type_face.equals(type)
								|| Region.type_pet.equals(type)) {
							String albumId = region.getAlbum();
							SmartCollectionImpl album = albumId == null ? null
									: dbManager.obtainById(SmartCollectionImpl.class, albumId);
							name = album != null ? album.getName() : "?"; //$NON-NLS-1$
							if (persId == null || persId.equals(albumId))
								color = SWT.COLOR_YELLOW;
						} else if (Region.type_barCode.equals(type)) {
							name = region.getAlbum();
							color = SWT.COLOR_GREEN;
						}
						regions.add(new ImageRegion(frame, id, type == null ? Region.type_face : type, name, asset));
						if (name != null) {
							gc.setForeground(device.getSystemColor(SWT.COLOR_DARK_GRAY));
							gc.drawText(name, frame.x + 5, frame.y + 3, true);
							gc.setForeground(device.getSystemColor(color));
							gc.drawText(name, frame.x + 4, frame.y + 2, true);
						}
					}
				}
			}
			gc.setForeground(JFaceResources.getColorRegistry().get(Constants.APPCOLOR_REGION_FACE));
			for (int j = 0; j < regionIds.length; j++) {
				if (done != null && done[j])
					continue;
				if (i++ > showRegions)
					break;
				Rectangle frame = computeFrame(regionIds[j], xs, ys, width, height, rotation);
				if (frame != null && !isDoubledRegion(rectangleOrOvalList, frame)) {
					rectangleOrOvalList.add(frame);
					if (frame.height < 0) {
						int d = frame.width;
						gc.drawOval(frame.x - d / 2, frame.y - d / 2, d, d);
					} else
						gc.drawRectangle(frame);
					regions.add(new ImageRegion(frame, regionIds[j], null, null, asset));
				}
			}
			gc.setClipping((Rectangle) null);
		}
		return regions;
	}

	private static final int QUARTERIMAGE = 65536 / 4;

	public static Image getFace(Device device, SmartCollection sm, int size, int margins,
			org.eclipse.swt.graphics.Color marginColor) {
		IDbManager dbManager = Core.getCore().getDbManager();
		int maxW = -1;
		RegionImpl maxRegion = null;
		List<RegionImpl> regions = dbManager.obtainObjects(RegionImpl.class, "album", sm.getStringId(), //$NON-NLS-1$
				QueryField.EQUALS);
		int noRegions = regions.size();
		int trials = Math.min(5, noRegions);
		while (noRegions > 0) {
			int j = (int) (Math.random() * noRegions);
			RegionImpl region = regions.get(j);
			String rect64 = region.getStringId();
			int l = rect64.length();
			if (l > 12) {
				int leadingZeros = Math.max(0, 16 - l);
				int h1 = Utilities.parseHex(rect64, 0 - leadingZeros, 4 - leadingZeros);
				int h2 = Utilities.parseHex(rect64, 4 - leadingZeros, 8 - leadingZeros);
				int h3 = Utilities.parseHex(rect64, 8 - leadingZeros, 12 - leadingZeros);
				int h4 = Utilities.parseHex(rect64, 12 - leadingZeros, 16 - leadingZeros);
				int w = Math.max(h4 - h2, h3 - h1);
				if (w > maxW) {
					maxW = w;
					maxRegion = region;
				}
			}
			if (maxW > QUARTERIMAGE || --trials <= 0)
				break;
		}
		Image image = null;
		Rectangle frame = null;
		if (maxRegion != null) {
			Asset asset = dbManager.obtainAsset(maxRegion.getAsset_person_parent());
			if (asset != null) {
				image = Core.getCore().getImageCache().getImage(asset);
				Rectangle bounds = image.getBounds();
				frame = UiUtilities.computeFrame(maxRegion.getStringId(), 0, 0, bounds.width, bounds.height,
						asset.getRotation());
			}
		}
		if (image == null) {
			image = Icons.person64.getImage();
			frame = image.getBounds();
		}
		Image thumb = new Image(device, size + 2 * margins, size + 2 * margins);
		float f = size / (float) Math.max(Math.min(frame.width, frame.height), size / 2);
		int destWidth = (int) (frame.width * f + 0.5);
		int destHeight = (int) (frame.height * f + 0.5);
		int srcWidth = frame.width;
		int srcHeight = frame.height;
		int srcX = frame.x;
		int srcY = frame.y;
		if (destWidth < size || destHeight < size) {
			float fac2 = Math.max(size / (float) destWidth, size / (float) destHeight);
			srcWidth = (int) (srcWidth * fac2 + 0.5);
			srcHeight = (int) (srcHeight * fac2 + 0.5);
			destWidth = (int) (destWidth * fac2 + 0.5);
			destHeight = (int) (destHeight * fac2 + 0.5);
			srcX = Math.max(0, srcX - (srcWidth - frame.width) / 2);
			srcY = Math.max(0, srcY - (srcHeight - frame.height) / 2);
		}
		int destX = (size - destWidth) / 2;
		int destY = (size - destHeight) / 2;
		GC gc = new GC(thumb);
		if (margins > 0) {
			gc.setBackground(marginColor);
			gc.fillRectangle(0, 0, size + 2 * margins, size + 2 * margins);
		}
		if (Platform.getPreferencesService().getBoolean(UiActivator.PLUGIN_ID, PreferenceConstants.ADVANCEDGRAPHICS,
				false, null)) {
			gc.setAntialias(SWT.ON);
			gc.setInterpolation(SWT.HIGH);
		}
		gc.drawImage(image, srcX, srcY, srcWidth, srcHeight, destX + margins, destY + margins, destWidth, destHeight);
		gc.dispose();
		return thumb;
	}

	public static boolean isInterPunction(char c) {
		return "!.,:;?".indexOf(c) >= 0; //$NON-NLS-1$
	}

	public static void installDoubleClickExpansion(TreeViewer viewer) {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				Object item = ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (item != null && viewer.isExpandable(item)) {
					if (viewer.getExpandedState(item))
						viewer.collapseToLevel(item, 1);
					else
						viewer.expandToLevel(item, 1);
				}
			}
		});
	}

	public static IViewPart showView(String id) {
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWorkbenchWindow != null) {
			IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
			if (activePage != null)
				try {
					return activePage.showView(id);
				} catch (PartInitException e1) {
					// do nothing
				}
		}
		return null;
	}

	public static Image getAnnotationIcon(Asset asset) {
		String voiceFileURI = asset.getVoiceFileURI();
		if (voiceFileURI != null && !voiceFileURI.isEmpty()) {
			int p = voiceFileURI.indexOf('\f');
			if (p < 0)
				return (voiceFileURI.startsWith("?") ? Icons.note : Icons.speaker).getImage(); //$NON-NLS-1$
			if (p > 0)
				return (voiceFileURI.length() > p + 2 ? Icons.speakerPlus : Icons.speaker).getImage();
			int q = voiceFileURI.indexOf('\f', p + 1);
			if (q > p + 1)
				return (voiceFileURI.length() > q + 1 ? Icons.notePlus : Icons.note).getImage();
			if (q < voiceFileURI.length() - 1)
				return Icons.drawing.getImage();
		}
		return null;
	}

	public static Date getCreationDate(Asset asset) {
		Date date = asset.getDateCreated();
		if (date == null)
			date = asset.getDateTimeOriginal();
		if (date == null)
			date = asset.getDateTime();
		return date;
	}

	private static final String ELLIPSIS = "..."; //$NON-NLS-1$

	public static String shortenText(String s, int maxchars) {
		if (s == null || s.length() <= maxchars)
			return s;
		return s.substring(0, maxchars) + ELLIPSIS;
	}
	
	public static void showTooltip(ToolTip tooltip, int x, int y, String title, String msg) {
		int hover = CommonUtilities.computeHoverTime(title.length() + msg.length());
		tooltip.setLocation(x, y);
		tooltip.setText(title);
		tooltip.setMessage(msg);
		tooltip.setAutoHide(false);
		tooltip.setVisible(true);
		tooltip.getDisplay().timerExec(hover, () -> {
			if (!tooltip.isDisposed())
				tooltip.setVisible(false);
		});
	}


}
