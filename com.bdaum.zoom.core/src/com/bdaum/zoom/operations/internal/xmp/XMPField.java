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

package com.bdaum.zoom.operations.internal.xmp;

import java.text.ParseException;
import java.util.Iterator;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPUtils;
import com.adobe.xmp.properties.XMPProperty;
import com.adobe.xmp.properties.XMPPropertyInfo;
import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.RegionImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.db.AssetEnsemble;
import com.bdaum.zoom.core.internal.db.AssetEnsemble.MWGRegion;

public class XMPField {

	private static final String LOCATIONSHOWNPREFIX = QueryField.IPTC_LOCATIONSHOWN.getXmpNs() + ":LocationShown"; //$NON-NLS-1$
	private static final int RESOLUTION_UNIT_VALUE_CM = 3;
	private static final int FOCAL_PLANE_RESOLUTION_UNIT_VALUE_CM = 3;
	private static final int FOCAL_PLANE_RESOLUTION_UNIT_VALUE_MM = 4;
	private static final int FOCAL_PLANE_RESOLUTION_UNIT_VALUE_UM = 5;

	private final XMPPropertyInfo prop;
	private final String path;
	private final int index1;
	private final int index2;
	private final XMPMeta xmpMeta;
	private final QueryField qfield;
	private final String attribute;

	public XMPField(XMPMeta xmpMeta, QueryField qfield, XMPPropertyInfo prop, String path, int index1, int index2,
			String attribute) {
		this.xmpMeta = xmpMeta;
		this.qfield = qfield;
		this.prop = prop;
		this.path = path;
		this.index1 = index1;
		this.index2 = index2;
		this.attribute = attribute;
	}

	public QueryField translateFields() {
		// Legacy
		switch (qfield.getType()) {
		case QueryField.T_LOCATION:
			return QueryField.IPTC_LOCATIONCREATED;
		case QueryField.T_CONTACT:
			return QueryField.IPTC_CONTACT;
		case QueryField.T_OBJECT:
			return QueryField.IPTC_ARTWORK;
		default:
			// New standard
			QueryField category = qfield.getParent();
			if (category == QueryField.LOCATION_TYPE) {
				if (path != null && path.startsWith(LOCATIONSHOWNPREFIX))
					return QueryField.IPTC_LOCATIONSHOWN;
				return QueryField.IPTC_LOCATIONCREATED;
			}
			if (category == QueryField.CONTACT_TYPE)
				return QueryField.IPTC_CONTACT;
			if (category == QueryField.ARTWORKOROBJECT_TYPE)
				return QueryField.IPTC_ARTWORK;
			break;
		}
		return qfield;
	}

	public QueryField translateIdFields() {
		if (qfield == QueryField.LOCATIONSHOWN_ID)
			return QueryField.IPTC_LOCATIONSHOWN;
		if (qfield == QueryField.LOCATIONCREATED_ID)
			return QueryField.IPTC_LOCATIONCREATED;
		if (qfield == QueryField.CONTACT_ID)
			return QueryField.IPTC_CONTACT;
		if (qfield == QueryField.ARTWORK_ID)
			return QueryField.IPTC_ARTWORK;
		return qfield;
	}

	public Object fetchStoredValue(AssetEnsemble ensemble) throws SecurityException, IllegalArgumentException {
		Object ref = null;
		int type = qfield.getType();
		Object target = ensemble.getAsset();
		QueryField category = qfield.getParent();
		// Legacy
		switch (type) {
		case QueryField.T_LOCATION:
			ref = QueryField.IPTC_LOCATIONCREATED.getStruct(ensemble.getAsset());
			break;
		case QueryField.T_CONTACT:
			ref = QueryField.IPTC_CONTACT.getStruct(ensemble.getAsset());
			break;
		case QueryField.T_OBJECT:
			ref = QueryField.IPTC_ARTWORK.getStruct(ensemble.getAsset());
			break;
		default:
			// New standard
			if (qfield == QueryField.LOCATIONSHOWN_ID) {
				Object obtainFieldValue = QueryField.IPTC_LOCATIONSHOWN.obtainFieldValue(ensemble.getAsset());
				ref = obtainFieldValue;
			} else if (qfield == QueryField.LOCATIONCREATED_ID)
				ref = QueryField.IPTC_LOCATIONCREATED.getStruct(ensemble.getAsset());
			else if (qfield == QueryField.CONTACT_ID)
				ref = QueryField.IPTC_CONTACT.getStruct(ensemble.getAsset());
			else if (qfield == QueryField.ARTWORK_ID)
				ref = QueryField.IPTC_ARTWORK.getStruct(ensemble.getAsset());
			else if (category == QueryField.LOCATION_TYPE)
				ref = path.startsWith(LOCATIONSHOWNPREFIX)
						? QueryField.IPTC_LOCATIONSHOWN.getStruct(ensemble.getAsset())
						: QueryField.IPTC_LOCATIONCREATED.getStruct(ensemble.getAsset());
			else if (category == QueryField.CONTACT_TYPE)
				ref = QueryField.IPTC_CONTACT.getStruct(ensemble.getAsset());
			else if (category == QueryField.ARTWORKOROBJECT_TYPE)
				ref = QueryField.IPTC_ARTWORK.getStruct(ensemble.getAsset());
			else if (category == QueryField.REGION_TYPE || category == QueryField.MWG_REGION_TYPE) {
				Asset asset = ensemble.getAsset();
				String[] regionIds = asset.getPerson();
				if (index1 >= 0 && index1 < regionIds.length) {
					IDbManager dbManager = Core.getCore().getDbManager();
					Iterator<RegionImpl> it = dbManager
							.<RegionImpl>obtainObjects(RegionImpl.class, false, Constants.OID, regionIds[index1],
									QueryField.EQUALS, "asset_person_parent", asset.getStringId(), //$NON-NLS-1$
									QueryField.EQUALS)
							.iterator();
					if (it.hasNext()) {
						MWGRegion mwgRegion = new AssetEnsemble.MWGRegion();
						try {
							AssetEnsemble.transferRegionData(dbManager, it.next(), mwgRegion);
							return qfield.obtainPlainFieldValue(mwgRegion);
						} catch (NumberFormatException e) {
							// ignore
						}
					}
				}
				return null;
			}
			break;
		}
		if (ref instanceof String[]) {
			String[] ids = (String[]) ref;
			if (index1 >= 0 && index1 < ids.length) {
				target = Core.getCore().getDbManager().obtainById(IdentifiableObject.class, ids[index1]);
				return (target != null) ? qfield.obtainPlainFieldValue(target) : null;
			}
			return null;
		} else if (ref instanceof String) {
			target = Core.getCore().getDbManager().obtainById(IdentifiableObject.class, (String) ref);
			return (target != null) ? qfield.obtainPlainFieldValue(target) : null;
		}
		return qfield.obtainPlainFieldValue(target);
	}

	public void assignValue(AssetEnsemble ensemble, Object backup)
			throws XMPException, SecurityException, IllegalArgumentException, ParseException {
		boolean useFormatter = false;
		int type = qfield.getType();
		Object value = null;
		Object target = ensemble.getAsset();
		QueryField category = qfield.getParent();
		switch (type) {
		// Legacy
		case QueryField.T_LOCATION:
			target = ensemble.getLocationCreated();
			break;
		case QueryField.T_CONTACT:
			target = ensemble.getCreatorContact();
			break;
		case QueryField.T_OBJECT:
			target = ensemble.getArtworkOrObject(index1);
			break;
		default:
			// New standard
			if (category == QueryField.LOCATION_TYPE)
				target = path.startsWith(LOCATIONSHOWNPREFIX) ? ensemble.getLocationShown(index1)
						: ensemble.getLocationCreated();
			else if (category == QueryField.CONTACT_TYPE)
				target = ensemble.getCreatorContact();
			else if (category == QueryField.ARTWORKOROBJECT_TYPE)
				target = ensemble.getArtworkOrObject(index1);
			else if (category == QueryField.REGION_TYPE)
				target = ensemble.getRegion(AssetEnsemble.MP, index2);
			else if (category == QueryField.MWG_REGION_TYPE
					|| category != null && category.getParent() == QueryField.MWG_REGION_TYPE)
				target = ensemble.getRegion(AssetEnsemble.MWG, index2);
			useFormatter = true;
		}
		int card = qfield.getCard();
		if (card == 1) {
			if (backup != null)
				value = backup;
			else {
				String v = prop.getValue();
				switch (type) {
				case QueryField.T_BOOLEAN:
					value = XMPUtils.convertToBoolean(v);
					break;
				case QueryField.T_POSITIVEINTEGER:
				case QueryField.T_INTEGER:
					if (v.startsWith("+")) //$NON-NLS-1$
						v = v.substring(1);
					value = XMPUtils.convertToInteger(v);
					break;
				case QueryField.T_POSITIVELONG:
				case QueryField.T_LONG:
					if (v.startsWith("+")) //$NON-NLS-1$
						v = v.substring(1);
					value = XMPUtils.convertToLong(v);
					break;
				case QueryField.T_POSITIVEFLOAT:
				case QueryField.T_CURRENCY:
				case QueryField.T_FLOAT:
				case QueryField.T_FLOATB:
					if (v.startsWith("+")) //$NON-NLS-1$
						v = v.substring(1);
					int p = v.indexOf('/');
					if (p >= 0)
						value = ((double) Long.parseLong(v.substring(0, p)) / Long.parseLong(v.substring(p + 1)));
					else {
						p = v.indexOf(',');
						if (p >= 0) {
							String degrees = v.substring(0, p);
							String minutes = v.substring(p + 1);
							double s = 1;
							if (minutes.endsWith("W") //$NON-NLS-1$
									|| minutes.endsWith("S")) { //$NON-NLS-1$
								s = -1;
								minutes = minutes.substring(0, minutes.length() - 1);
							} else if (minutes.endsWith("E") //$NON-NLS-1$
									|| minutes.endsWith("N")) //$NON-NLS-1$
								minutes = minutes.substring(0, minutes.length() - 1);
							value = (Integer.parseInt(degrees) + XMPUtils.convertToDouble(minutes) / 60d) * s;
						} else
							value = XMPUtils.convertToDouble(v);
					}
					if (category == QueryField.EXIF_XRES || category == QueryField.EXIF_YRES) {
						XMPProperty property = xmpMeta.getProperty(QueryField.NS_TIFF.uri, "ResolutionUnit"); //$NON-NLS-1$
						int unit = XMPUtils.convertToInteger(property.getValue().toString());
						if (unit == RESOLUTION_UNIT_VALUE_CM)
							value = ((Double) value).doubleValue() * 2.54d;
					} else if (category == QueryField.EXIF_FOCALPLANEXRESOLUTION
							|| category == QueryField.EXIF_FOCALPLANEYRESOLUTION) {
						XMPProperty property = xmpMeta.getProperty(QueryField.NS_EXIF.uri, "FocalPlaneResolutionUnit"); //$NON-NLS-1$
						int unit = XMPUtils.convertToInteger(property.getValue().toString());
						switch (unit) {
						case FOCAL_PLANE_RESOLUTION_UNIT_VALUE_CM:
							value = ((Double) value).doubleValue() * 2.54d;
							break;
						case FOCAL_PLANE_RESOLUTION_UNIT_VALUE_MM:
							value = ((Double) value).doubleValue() * 25.4d;
							break;
						case FOCAL_PLANE_RESOLUTION_UNIT_VALUE_UM:
							value = ((Double) value).doubleValue() * 25400d;
							break;
						}
					}
					break;
				case QueryField.T_DATE:
					value = XMPUtils.convertToDate(v).getCalendar().getTime();
					break;
				case QueryField.T_REGION:
					String s = prop.getValue();
					if (attribute == null) {
						if (s == null || s.isEmpty())
							return;
						value = s;
					} else
						value = new String[] { attribute, s };
					break;
				default:
					value = prop.getValue();
					break;
				}
			}
			if (useFormatter && qfield.getFormatter() != null && value instanceof String)
				value = qfield.getFormatter().parse((String) value);
		} else {
			value = backup != null ? backup : prop.getValue();
			if (backup == null && value != null && !value.toString().isEmpty()) {
				if (card == QueryField.CARD_BAG || card == QueryField.CARD_MODIFIABLEBAG)
					value = new String[] { value.toString() };
				else {
					Object oldArray = qfield.obtainPlainFieldValue(target);
					if (oldArray instanceof int[]) {
						int[] oldInt = (int[]) oldArray;
						if (index2 <= oldInt.length) {
							oldInt[index2 - 1] = Integer.parseInt(value.toString());
							value = oldInt;
						} else {
							int newLength = oldInt.length + 1;
							int[] newInt = new int[newLength];
							System.arraycopy(oldInt, 0, newInt, 0, Math.min(oldInt.length, newLength));
							newInt[oldInt.length] = Integer.parseInt(value.toString());
							value = newInt;
						}
					} else {
						String[] oldString = (String[]) oldArray;
						if (index2 <= oldString.length) {
							oldString[index2 - 1] = (String) value;
							value = oldString;
						} else {
							String[] newString = new String[oldString.length + 1];
							System.arraycopy(oldString, 0, newString, 0, oldString.length);
							newString[oldString.length] = (String) value;
							value = newString;
						}
					}
				}
			}
		}
		qfield.setFieldValue(target, value != null ? value : qfield.getDefaultValue());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof XMPField) {
			XMPField other = (XMPField) obj;
			if (other.index1 != index1 || other.index2 != index2)
				return false;
			String path2 = other.getPath();
			if (path2 == null)
				return path == null;
			return path2.equals(path);
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return (((path == null) ? 0 : path.hashCode()) * 31 + index1) * 31 + index2;
	}

	public XMPPropertyInfo getProp() {
		return prop;
	}

	public String getPath() {
		return path;
	}

	public int getIndex1() {
		return index1;
	}

	public int getIndex2() {
		return index2;
	}

	public XMPMeta getXmpMeta() {
		return xmpMeta;
	}

	public QueryField getQfield() {
		return qfield;
	}

	public String getAttribute() {
		return attribute;
	}
	
	public int getIntegerValue() throws XMPException {
		String v = getProp().getValue();
		if (v.startsWith("+")) //$NON-NLS-1$
			v = v.substring(1);
		return XMPUtils.convertToInteger(v);
	}

}
