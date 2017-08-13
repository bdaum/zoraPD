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
 * (c) 2009-2014 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.lal.internal.lire;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObjectImpl;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.asset.RegionImpl;
import com.bdaum.zoom.cat.model.creatorsContact.ContactImpl;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitImpl;
import com.bdaum.zoom.cat.model.group.slideShow.SlideImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebExhibitImpl;
import com.bdaum.zoom.cat.model.location.LocationImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.core.internal.lucene.ILuceneService;
import com.bdaum.zoom.lal.internal.LireActivator;

import net.semanticmetadata.lire.builders.AbstractLocalDocumentBuilder;

@SuppressWarnings("restriction")
public class TextDocumentBuilder extends AbstractLocalDocumentBuilder {

	private static final Field[] EMPTYFIELDS = new Field[0];
	private final String fieldName;
	private final Collection<String> textFields;

	public TextDocumentBuilder(String fieldName, Collection<String> textFields) {
		this.fieldName = fieldName;
		this.textFields = textFields;
	}

	public Document createDocument(BufferedImage image, String identifier) {
		assert (identifier != null);
		Document doc = new Document();
		String text = createTextDescriptorValue(identifier);
		if (text != null) {
			LireActivator activator = LireActivator.getDefault();
			doc.add(activator.createTextSearchField(fieldName, text));
			doc.add(activator.createDocumentIdentifierField(identifier));
		}
		return doc;
	}

	private String createTextDescriptorValue(String identifier) {
		IDbManager dbManager = Core.getCore().getDbManager();
		AssetImpl asset = dbManager.obtainAsset(identifier);
		if (asset != null) {
			StringBuilder text = new StringBuilder(1024);
			for (QueryField qfield : QueryField.getQueryFields()) {
				if (qfield.isFullTextBase()) {
					if (textFields == null
							|| textFields.contains(qfield.getId())) {
						if (qfield.isStruct()) {
							Object struct = qfield.getStruct(asset);
							if (struct instanceof String[])
								for (String structId : (String[]) struct)
									serializeStruct(structId, text);
							else if (struct instanceof String)
								serializeStruct((String) struct, text);
						} else
							retrieveFieldValue(text, asset, qfield);
					}
				}
			}
			if (textFields == null
					|| textFields.contains(ILuceneService.INDEX_PERSON_SHOWN)) {
				if (asset.getPerson() != null && asset.getPerson().length > 0) {
					List<RegionImpl> regions = dbManager
							.obtainObjects(
									RegionImpl.class,
									"asset_person_parent", asset.getStringId(), QueryField.EQUALS); //$NON-NLS-1$
					for (RegionImpl region : regions) {
						String albumId = region.getAlbum();
						if (albumId != null) {
							SmartCollectionImpl album = dbManager.obtainById(
									SmartCollectionImpl.class, albumId);
							if (album != null)
								addSentence(text, album.getName());
						}
					}
				}
			}
			if (textFields == null
					|| textFields.contains(ILuceneService.INDEX_FILENAME))
				addSentence(text, Core.getFileName(asset.getUri(), false));
			boolean title = textFields == null
					|| textFields.contains(ILuceneService.INDEX_SLIDE_TITLE);
			boolean descr = textFields == null
					|| textFields.contains(ILuceneService.INDEX_SLIDE_DESCR);
			if (title || descr) {
				List<SlideImpl> slides = dbManager
						.obtainObjects(SlideImpl.class,
								"asset", identifier, QueryField.EQUALS); //$NON-NLS-1$
				for (SlideImpl slide : slides) {
					if (title)
						addSentence(text, slide.getCaption());
					if (descr)
						addSentence(text, slide.getDescription());
				}
			}
			title = textFields == null
					|| textFields.contains(ILuceneService.INDEX_EXH_TITLE);
			descr = textFields == null
					|| textFields.contains(ILuceneService.INDEX_EXH_DESCR);
			boolean credits = textFields == null
					|| textFields.contains(ILuceneService.INDEX_EXH_CREDITS);
			if (title || descr || credits) {
				List<ExhibitImpl> exhibits = dbManager.obtainObjects(
						ExhibitImpl.class,
						"asset", identifier, QueryField.EQUALS); //$NON-NLS-1$
				for (ExhibitImpl exhibit : exhibits) {
					if (title)
						addSentence(text, exhibit.getTitle());
					if (descr)
						addSentence(text, exhibit.getDescription());
					if (credits)
						addSentence(text, exhibit.getCredits());
				}
			}
			title = textFields == null
					|| textFields.contains(ILuceneService.INDEX_WEBGAL_TITLE);
			descr = textFields == null
					|| textFields.contains(ILuceneService.INDEX_WEBGAL_DESCR);
			boolean alt = textFields == null
					|| textFields.contains(ILuceneService.INDEX_WEBGAL_ALT);
			if (title || descr || alt) {
				List<WebExhibitImpl> webexhibits = dbManager.obtainObjects(
						WebExhibitImpl.class,
						"asset", identifier, QueryField.EQUALS); //$NON-NLS-1$
				for (WebExhibitImpl exhibit : webexhibits) {
					if (title)
						addSentence(text, exhibit.getCaption());
					if (descr)
						addSentence(text,
								Utilities.getPlainDescription(exhibit));
					if (alt)
						addSentence(text, exhibit.getAltText());
				}
			}
			return text.toString();
		}
		return null;

	}


	public Field[] createDescriptorFields(BufferedImage image, String identifier) {
		String text = createTextDescriptorValue(identifier);
		if (text == null)
			return EMPTYFIELDS;
		return new Field[] { LireActivator.getDefault().createTextSearchField(
				fieldName, text) };
	}

	private static void addSentence(StringBuilder text, String v) {
		if (v != null) {
			v = v.trim();
			if (v.length() > 1)
				text.append(v).append(". "); //$NON-NLS-1$
		}
	}

	private static void serializeStruct(String id, StringBuilder text) {
		IdentifiableObject obj = Core.getCore().getDbManager()
				.obtainById(IdentifiableObject.class, id);
		QueryField[] children = null;
		if (obj instanceof LocationImpl)
			children = QueryField.LOCATION_TYPE.getChildren();
		else if (obj instanceof ArtworkOrObjectImpl)
			children = QueryField.ARTWORKOROBJECT_TYPE.getChildren();
		else if (obj instanceof ContactImpl)
			children = QueryField.CONTACT_TYPE.getChildren();
		if (children != null)
			for (QueryField qChild : children)
				if (qChild.isFullTextSearch())
					retrieveFieldValue(text, obj, qChild);
	}

	private static void retrieveFieldValue(StringBuilder text, Object obj,
			QueryField qfield) {
		if (qfield.getId() != qfield.getKey())
			return;
		Object value = qfield.obtainPlainFieldValue(obj);
		String v = null;
		if (value != null) {
			if (value instanceof String[]) {
				v = Core.toStringList((String[]) value, ";"); //$NON-NLS-1$
				if (qfield == QueryField.IPTC_SUPPLEMENTALCATEGORIES)
					v = v.replace('/', ';').replace('|', ';');
			} else if (value instanceof Integer) {
				int[] enumeration = (int[]) qfield.getEnumeration();
				if (enumeration != null) {
					int val = (Integer) value;
					for (int i = 0; i < enumeration.length; i++)
						if (val == enumeration[i]) {
							v = qfield.getEnumLabels()[i];
							break;
						}
				}
			} else
				v = value.toString();
			if (v != null)
				addSentence(text, v);
		}
	}

}