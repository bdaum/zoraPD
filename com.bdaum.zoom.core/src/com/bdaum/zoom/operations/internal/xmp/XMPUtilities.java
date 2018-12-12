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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import org.eclipse.osgi.util.NLS;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPIterator;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import com.adobe.xmp.XMPSchemaRegistry;
import com.adobe.xmp.options.PropertyOptions;
import com.adobe.xmp.properties.XMPPropertyInfo;
import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObjectImpl;
import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObjectShownImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.RegionImpl;
import com.bdaum.zoom.cat.model.creatorsContact.ContactImpl;
import com.bdaum.zoom.cat.model.creatorsContact.CreatorsContactImpl;
import com.bdaum.zoom.cat.model.location.LocationImpl;
import com.bdaum.zoom.cat.model.locationCreated.LocationCreatedImpl;
import com.bdaum.zoom.cat.model.locationShown.LocationShownImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IFormatter;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.QueryField.Namespace;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.core.internal.db.AssetEnsemble;
import com.bdaum.zoom.core.internal.db.AssetEnsemble.MWGRegion;

public class XMPUtilities {

	public static void configureXMPFactory() throws XMPException {
		XMPSchemaRegistry schemaRegistry = XMPMetaFactory.getSchemaRegistry();
		for (Namespace ns : QueryField.ALLNAMESPACES)
			schemaRegistry.registerNamespace(ns.uri, ns.defaultPrefix);
	}

	public static List<XMPField> readXMP(InputStream in) throws XMPException {
		XMPUtilities.configureXMPFactory();
		XMPMeta xmpMeta = XMPMetaFactory.parse(in);
		XMPIterator iterator = xmpMeta.iterator();
		List<XMPField> fields = new ArrayList<XMPField>();
		String arrayNameSpace = null;
		while (iterator.hasNext()) {
			XMPPropertyInfo prop = (XMPPropertyInfo) iterator.next();
			StringBuilder sb = new StringBuilder();
			String namespace = prop.getNamespace();
			if (namespace != null && !namespace.isEmpty())
				sb.append(namespace).append(':');
			else if (arrayNameSpace != null && !arrayNameSpace.isEmpty())
				sb.append(arrayNameSpace).append(':');
			String path = prop.getPath();
			if (path != null) {
				int index1 = -1;
				int index2 = -1;
				String attribute = null;
				int p = path.indexOf('/');
				if (p >= 0) {
					String struct = path.substring(0, p);
					String detail = path.substring(p + 1);
					p = struct.indexOf(':');
					if (p >= 0)
						struct = struct.substring(p + 1);
					p = detail.indexOf(':');
					if (p >= 0)
						detail = detail.substring(p + 1);
					String subdetail = null;
					p = detail.indexOf('/');
					if (p >= 0) {
						subdetail = detail.substring(p + 1);
						detail = detail.substring(0, p);
						p = subdetail.indexOf('/');
						if (p >= 0) {
							attribute = subdetail.substring(p + 1);
							subdetail = subdetail.substring(0, p);
							p = attribute.indexOf(':');
							if (p >= 0)
								attribute = attribute.substring(p + 1);
						}
						p = subdetail.indexOf(':');
						if (p >= 0)
							subdetail = subdetail.substring(p + 1);
					}
					int istart = struct.indexOf('[');
					if (istart >= 0) {
						int iend = struct.indexOf(']', istart);
						if (iend > istart)
							try {
								index1 = Integer.parseInt(struct.substring(istart + 1, iend));
							} catch (NumberFormatException e) {
								// do nothing
							}
						struct = struct.substring(0, istart);
					}
					istart = detail.indexOf('[');
					if (istart >= 0) {
						int iend = detail.indexOf(']', istart);
						if (iend > istart)
							try {
								index2 = Integer.parseInt(detail.substring(istart + 1, iend));
							} catch (NumberFormatException e) {
								// do nothing
							}
						detail = detail.substring(0, istart);
					}
					sb.append(struct).append('/').append(detail);
					if (subdetail != null) {
						sb.append('/').append(subdetail);
						if (attribute != null)
							sb.append('/').append(attribute);
					}
				} else {
					p = path.indexOf(':');
					if (p >= 0)
						path = path.substring(p + 1);
					int istart = path.indexOf('[');
					if (istart >= 0) {
						int iend = path.indexOf(']', istart);
						if (iend > istart)
							try {
								index2 = Integer.parseInt(path.substring(istart + 1, iend));
							} catch (NumberFormatException e) {
								// do nothing
							}
						path = path.substring(0, istart);
					}
					sb.append(path);
				}
				path = sb.toString();
				if (prop.getValue() != null) {
					QueryField qfield = QueryField.findXmpProperty(path);
					if (qfield != null && qfield.getKey() != null && qfield.getType() != QueryField.T_NONE
							&& !qfield.isStruct()) {
						if (qfield.getCard() == 1 || index2 >= 0) {
							fields.add(new XMPField(xmpMeta, qfield, prop, path, index1, index2, null));
							arrayNameSpace = null;
						} else {
							PropertyOptions options = prop.getOptions();
							if (options.isArray() || options.isArrayAlternate() || options.isArrayAltText()
									|| options.isArrayOrdered())
								arrayNameSpace = namespace;
						}
					}
				}
			}
		}
		return fields;
	}

	public static void writeProperties(XMPMeta xmpMeta, Asset asset, Set<QueryField> filter, boolean withId) {
		CoreActivator activator = CoreActivator.getDefault();
		IMediaSupport mediaSupport = activator.getMediaSupport(asset.getFormat());
		int flags = (mediaSupport != null) ? mediaSupport.getPropertyFlags() : QueryField.PHOTO;
		IDbManager dbManager = activator.getDbManager();
		List<QueryField> qfields = new ArrayList<QueryField>(100);
		listXmpQueryFields(QueryField.ALL, qfields);
		for (QueryField queryField : qfields) {
			if (filter != null && !filter.contains(queryField))
				continue;
			if (!queryField.testFlags(flags))
				continue;
			try {
				String xmpNs = queryField.getXmpNs() == null ? null : queryField.getXmpNs().uri;
				String path = queryField.getPath();
				PropertyOptions optionsArray = new PropertyOptions(PropertyOptions.ARRAY);
				PropertyOptions optionsStruct = new PropertyOptions(PropertyOptions.STRUCT);
				if (queryField.isStruct()) {
					boolean isStructArray = queryField.getCard() != 1;
					String id = asset.getStringId();
					QueryField[] children = null;
					List<? extends IdentifiableObject> relations = null;
					if (queryField == QueryField.IPTC_LOCATIONCREATED) {
						children = QueryField.LOCATION_TYPE.getChildren();
						relations = dbManager.obtainStructForAsset(LocationCreatedImpl.class, id, true);
					} else if (queryField == QueryField.IPTC_LOCATIONSHOWN) {
						children = QueryField.LOCATION_TYPE.getChildren();
						relations = dbManager.obtainStructForAsset(LocationShownImpl.class, id, false);
					} else if (queryField == QueryField.IPTC_ARTWORK) {
						children = QueryField.ARTWORKOROBJECT_TYPE.getChildren();
						relations = dbManager.obtainStructForAsset(ArtworkOrObjectShownImpl.class, id, false);
					} else if (queryField == QueryField.IPTC_CONTACT) {
						children = QueryField.CONTACT_TYPE.getChildren();
						relations = dbManager.obtainStructForAsset(CreatorsContactImpl.class, id, true);
					} else if (queryField == QueryField.FACESSHOWN) {
						children = QueryField.REGION_TYPE.getChildren();
						relations = dbManager.obtainObjects(RegionImpl.class, "asset_person_parent", id, //$NON-NLS-1$
								QueryField.EQUALS);
					} else if (queryField == QueryField.MWG_FACESSHOWN) {
						children = QueryField.MWG_REGION_TYPE.getChildren();
						relations = dbManager.obtainObjects(RegionImpl.class, "asset_person_parent", id, //$NON-NLS-1$
								QueryField.EQUALS);
					}
					xmpMeta.deleteProperty(xmpNs, path);
					int si = 0;
					if (relations != null)
						for (Object object : relations) {
							String relId = null;
							Class<? extends IdentifiableObject> relClass = null;
							if (queryField == QueryField.IPTC_LOCATIONCREATED) {
								relId = ((LocationCreatedImpl) object).getLocation();
								relClass = LocationImpl.class;
							} else if (queryField == QueryField.IPTC_LOCATIONSHOWN) {
								relId = ((LocationShownImpl) object).getLocation();
								relClass = LocationImpl.class;
							} else if (queryField == QueryField.IPTC_ARTWORK) {
								relId = ((ArtworkOrObjectShownImpl) object).getArtworkOrObject();
								relClass = ArtworkOrObjectImpl.class;
							} else if (queryField == QueryField.IPTC_CONTACT) {
								relId = ((CreatorsContactImpl) object).getContact();
								relClass = ContactImpl.class;
							} else if (queryField == QueryField.FACESSHOWN || queryField == QueryField.MWG_FACESSHOWN) {
								MWGRegion mwgRegion = new AssetEnsemble.MWGRegion();
								AssetEnsemble.transferRegionData(dbManager, (RegionImpl) object, mwgRegion);
								object = mwgRegion;
							}
							String structPath;
							if (isStructArray) {
								xmpMeta.appendArrayItem(xmpNs, path, optionsArray, null, optionsStruct);
								structPath = path + "[" + (++si) + "]"; //$NON-NLS-1$ //$NON-NLS-2$
							} else {
								xmpMeta.setProperty(xmpNs, path, null, optionsStruct);
								structPath = path;
							}
							IdentifiableObject struct = relClass == null ? null : dbManager.obtainById(relClass, relId);
							if (children != null) {
								if (struct != null && withId)
									xmpMeta.setStructField(xmpNs, structPath, QueryField.NS_ZORA.uri, "ZoRaId", struct //$NON-NLS-1$
											.getStringId());
								for (QueryField qchild : children) {
									Namespace childNs = qchild.getXmpNs();
									if (childNs == null)
										continue;
									String childPath = qchild.getPath();
									Object structValue = null;
									if (qchild.getCategory() == QueryField.CATEGORY_LOCAL)
										structValue = getStructValue(qchild, object, true);
									else if (struct != null)
										structValue = getStructValue(qchild, struct,
												qchild.getCategory() != QueryField.CATEGORY_FOREIGN
														|| qchild.getType() != QueryField.T_FLOATB); // because
																										// of
																										// lat,
																										// lon,
																										// alt
									if (structValue != null) {
										if (qchild.getCard() != 1 && structValue instanceof String[]) {
											xmpMeta.setStructField(xmpNs, structPath, childNs.uri, childPath, null,
													optionsArray);
											String qChildPath = new StringBuilder().append(structPath).append('/')
													.append(childNs.defaultPrefix).append(':').append(childPath)
													.toString();
											String[] array = (String[]) structValue;
											for (int i = 0; i < array.length; i++)
												if (array[i] != null && !array[i].isEmpty())
													xmpMeta.appendArrayItem(xmpNs, qChildPath, optionsArray, array[i],
															null);
										} else if (!structValue.equals(qchild.getDefaultValue())) {
											if (structValue instanceof MWGRegion)
												writeMwgRegion(xmpMeta, asset, xmpNs, path, si == 1, structPath,
														childNs, childPath, (MWGRegion) structValue);
											else
												xmpMeta.setStructField(xmpNs, structPath, childNs.uri, childPath,
														structValue.toString());
										}
									}
								}
							}
							if (!isStructArray)
								break;
						}
				} else if (queryField.getCard() != 1) {
					Object fieldValue = getFieldValue(queryField, asset);
					xmpMeta.deleteProperty(xmpNs, path);
					if (fieldValue instanceof int[]) {
						int[] array = (int[]) fieldValue;
						for (int i = 0; i < array.length; i++)
							if (array[i] != 0)
								xmpMeta.appendArrayItem(xmpNs, path, new PropertyOptions(PropertyOptions.ARRAY_ORDERED),
										String.valueOf(array[i]), null);
					} else if (fieldValue instanceof boolean[]) {
						boolean[] array = (boolean[]) fieldValue;
						for (int i = 0; i < array.length; i++)
							if (array[i])
								xmpMeta.appendArrayItem(xmpNs, path, new PropertyOptions(PropertyOptions.ARRAY_ORDERED),
										String.valueOf(array[i]), null);
					} else if (fieldValue instanceof double[]) {
						double[] array = (double[]) fieldValue;
						for (int i = 0; i < array.length; i++)
							if (array[i] != 0d)
								xmpMeta.appendArrayItem(xmpNs, path, new PropertyOptions(PropertyOptions.ARRAY_ORDERED),
										String.valueOf(array[i]), null);
					} else if (fieldValue instanceof String[]) {
						String[] array = (String[]) fieldValue;
						for (int i = 0; i < array.length; i++)
							if (array[i] != null && !array[i].isEmpty())
								xmpMeta.appendArrayItem(xmpNs, path, optionsArray, array[i], null);
					}
				} else {
					Object value = getFieldValue(queryField, asset);
					int p = path.indexOf('/');
					if (p >= 0) {
						String group = path.substring(0, p);
						path = path.substring(p + 1);
						p = path.indexOf(':');
						String fieldNs = p >= 0
								? XMPMetaFactory.getSchemaRegistry().getNamespaceURI(path.substring(0, p))
								: xmpNs;
						xmpMeta.deleteStructField(xmpNs, group, fieldNs, path);
						if (value != null && !value.equals(queryField.getDefaultValue()))
							xmpMeta.setStructField(xmpNs, group, xmpNs, path, value.toString());
					} else
						xmpMeta.deleteProperty(xmpNs, path);
					if (value != null && !value.equals(queryField.getDefaultValue()))
						xmpMeta.setProperty(xmpNs, path, value);
					if (queryField == QueryField.COLORCODE) {
						xmpMeta.deleteProperty(QueryField.NS_XMP.uri, queryField.getExifToolKey());
						if (value != null) {
							int code = (Integer) value;
							if (code >= 0 && code < QueryField.XMPCOLORCODES.length)
								xmpMeta.setProperty(QueryField.NS_XMP.uri, queryField.getExifToolKey(),
										QueryField.XMPCOLORCODES[code][0]);
						}
					}
				}
			} catch (XMPException e) {
				activator.logError(NLS.bind(Messages.XMPUtilities_cannot_write_xmp, queryField.getLabel()), e);
			}
		}
	}

	public static void writeMwgRegion(XMPMeta xmpMeta, Asset asset, String xmpNs, String path, boolean first,
			String structPath, Namespace childNs, String childPath, MWGRegion region) throws XMPException {
		if (first) {
			String regionPath = path;
			int p = regionPath.lastIndexOf('/');
			if (p >= 0)
				regionPath = regionPath.substring(0, p);
			xmpMeta.setStructField(xmpNs, regionPath, QueryField.MWG_APPLIEDTODIM.getXmpNs().uri,
					QueryField.MWG_APPLIEDTODIM.getPath(), null, new PropertyOptions(PropertyOptions.STRUCT));
			String structName = regionPath + '/' + QueryField.MWG_APPLIEDTODIM.getXmpNs().defaultPrefix + ':'
					+ QueryField.MWG_APPLIEDTODIM.getPath();
			String uri = QueryField.NS_MWG_DIM.uri;
			xmpMeta.setStructField(xmpNs, structName, uri, "w", //$NON-NLS-1$
					String.valueOf(asset.getWidth()));
			xmpMeta.setStructField(xmpNs, structName, uri, "h", //$NON-NLS-1$
					String.valueOf(asset.getHeight()));
			xmpMeta.setStructField(xmpNs, structName, uri, "unit", "pixel"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		xmpMeta.setStructField(xmpNs, structPath, childNs.uri, childPath, null,
				new PropertyOptions(PropertyOptions.STRUCT));
		String structName = structPath + '/' + childNs.defaultPrefix + ':' + childPath;
		String uri = QueryField.NS_MWG_AREA.uri;
		xmpMeta.setStructField(xmpNs, structName, uri, "x", //$NON-NLS-1$
				String.valueOf(region.getCenterX()));
		xmpMeta.setStructField(xmpNs, structName, uri, "y", //$NON-NLS-1$
				String.valueOf(region.getCenterY()));
		xmpMeta.setStructField(xmpNs, structName, uri, "w", //$NON-NLS-1$
				String.valueOf(region.getWidth()));
		xmpMeta.setStructField(xmpNs, structName, uri, "h", //$NON-NLS-1$
				String.valueOf(region.getHeight()));
		xmpMeta.setStructField(xmpNs, structName, uri, "unit", //$NON-NLS-1$
				"normalized"); //$NON-NLS-1$
	}

	private static void listXmpQueryFields(QueryField qfield, List<QueryField> qfields) {
		if (!qfield.hasChildren()) {
			if (qfield.getXmpNs() != null)
				qfields.add(qfield);
		} else
			for (QueryField child : qfield.getChildren())
				listXmpQueryFields(child, qfields);
	}

	private static Object getFieldValue(QueryField qfield, Asset asset) {
		if (qfield.getCard() != 1)
			return qfield.obtainFieldValue(asset);
		String key = qfield.getKey();
		try {
			Object obj = qfield.obtainPlainFieldValue(asset);
			if (obj instanceof Double) {
				double v = (Double) obj;
				if (Double.isNaN(v))
					return null;
				if ((qfield.getType() == QueryField.T_POSITIVEFLOAT || qfield.getType() == QueryField.T_CURRENCY)
						&& v < 0)
					return null;
			} else if (obj instanceof Integer) {
				if (qfield.getType() == QueryField.T_POSITIVEINTEGER && (Integer) obj < 0)
					return null;
			} else if (obj instanceof Long) {
				if (qfield.getType() == QueryField.T_POSITIVELONG && (Long) obj < 0)
					return null;
			} else if (obj instanceof Date) {
				GregorianCalendar cal = new GregorianCalendar();
				cal.setTime((Date) obj);
				return cal;
			}
			return obj;
		} catch (Exception e) {
			Core.getCore().logError(NLS.bind(Messages.XMPUtilities_Internal_error_accessing_field, key), e);
		}
		return null;
	}

	private static Object getStructValue(QueryField qfield, Object obj, boolean useFormatter) {
		try {
			Object o = qfield.obtainPlainFieldValue(obj);
			if (!useFormatter)
				return o;
			IFormatter formatter = qfield.getFormatter();
			return formatter != null ? formatter.toString(o) : o;
		} catch (Exception e) {
			Core.getCore().logError(NLS.bind(Messages.XMPUtilities_Internal_error_accessing_field, qfield.getKey()), e);
		}
		return null;
	}

	private static byte[] APP1 = new byte[] { (byte) 0xff, (byte) 0xe1, 0, 0, // Marker (4)
			'E', 'x', 'i', 'f', 0, 0, // Exif header (6)
			'M', 'M', 0, '*', 0, 0, 0, 8, // TIFF header (Motorola byte order) (8)
			0, 1, // IFD0 1 entry (2)
			2, (byte) 0xbc, 0, 1, 0, 0, 'c', 'c', 0, 0, 0, 0x16, // XMP entry, tag, byte, count, offset (12)
			0, 0, 0, 0 // Pointer to next IFD
	};

	public static byte[] getXmpFromJPEG(byte[] jpeg) {
		if (hasExif(jpeg)) {
			int ml = readWord(jpeg, 28);
			if (ml > 0) {
				byte[] metadata = new byte[ml];
				System.arraycopy(jpeg, 2 + APP1.length, metadata, 0, ml);
				return metadata;
			}
		}
		return null;
	}

	public static byte[] insertXmpIntoJPEG(byte[] jpeg, byte[] metadata) {
		int ml = metadata.length;
		while (ml > 0)
			if (metadata[--ml] != 0) {
				++ml;
				break;
			}
		int ml2 = (ml + 1) & 0xfffffffe;
		int q = 2;
		if (hasExif(jpeg))
			q += APP1.length + readWord(jpeg, 28) + 1;
		byte[] buf = new byte[jpeg.length - q + ml2 + APP1.length + 2];
		buf[0] = jpeg[0];
		buf[1] = jpeg[1];
		System.arraycopy(APP1, 0, buf, 2, APP1.length);
		writeWord(buf, 4, APP1.length - 2 + ml2);
		writeWord(buf, 28, ml);
		int p = 2 + APP1.length;
		System.arraycopy(metadata, 0, buf, p, ml);
		System.arraycopy(jpeg, q, buf, p + ml2, jpeg.length - q);
		return buf;
	}

	private static int readWord(byte[] buf, int i) {
		return ((buf[i] & 0x000000ff) << 8) + (buf[i + 1] & 0x000000ff);
	}

	private static void writeWord(byte[] buf, int i, int v) {
		buf[i] = (byte) (v >> 8);
		buf[i + 1] = (byte) v;
	}

	private static boolean hasExif(byte[] jpeg) {
		return jpeg.length > APP1.length && jpeg[6] == 'E' && jpeg[7] == 'x' && jpeg[8] == 'i' && jpeg[9] == 'f';
	}

}
