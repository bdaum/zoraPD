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
package com.bdaum.zoom.db.internal;

import java.util.Set;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.ITypeFilter;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.image.ImageConstants;
import com.db4o.query.Constraint;
import com.db4o.query.Query;

@SuppressWarnings("restriction")
public class TypeFilter extends AssetFilter implements ITypeFilter {

	private static final CoreActivator CORE_ACTIVATOR = CoreActivator
			.getDefault();
	int formats;

	public TypeFilter(int formats) {
		this.formats = formats;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.db.internal.AssetFilter#accept(com.bdaum.zoom.cat.model
	 * .asset.Asset)
	 */
	@Override
	public boolean accept(Asset asset) {
		String format = asset.getFormat();
		String mimeType = asset.getMimeType();
		switch (formats) {
		case 0:
			break;
		case RAW:
			return isRaw(format, mimeType);
		case DNG:
			return isDng(format);
		case RAW | DNG:
			return ImageConstants.IMAGE_X_RAW.equals(mimeType);
		case JPEG:
			return ImageConstants.JPEG.equals(format);
		case JPEG | RAW:
			return ImageConstants.JPEG.equals(format)
					|| isRaw(format, mimeType);
		case JPEG | DNG:
			return ImageConstants.JPEG.equals(format) || isDng(format);
		case JPEG | DNG | RAW:
			return ImageConstants.JPEG.equals(format)
					|| ImageConstants.IMAGE_X_RAW.equals(mimeType);
		case TIFF:
			return ImageConstants.TIFF.equals(format);

		case TIFF | RAW:
			return ImageConstants.TIFF.equals(format)
					|| isRaw(format, mimeType);
		case TIFF | DNG:
			return ImageConstants.TIFF.equals(format) || isDng(format);
		case TIFF | DNG | RAW:
			return ImageConstants.TIFF.equals(format)
					|| ImageConstants.IMAGE_X_RAW.equals(mimeType);
		case TIFF | JPEG:
			return ImageConstants.TIFF.equals(format)
					|| ImageConstants.JPEG.equals(format);
		case TIFF | JPEG | RAW:
			return ImageConstants.JPEG.equals(format)
					|| ImageConstants.TIFF.equals(format)
					|| isRaw(format, mimeType);
		case TIFF | JPEG | DNG:
			return ImageConstants.TIFF.equals(format)
					|| ImageConstants.JPEG.equals(format) || isDng(format);
		case TIFF | JPEG | DNG | RAW:
			return ImageConstants.TIFF.equals(format)
					|| ImageConstants.JPEG.equals(format)
					|| ImageConstants.IMAGE_X_RAW.equals(mimeType);
		case OTHER:
			return !ImageConstants.TIFF.equals(format)
					&& !ImageConstants.JPEG.equals(format)
					&& !ImageConstants.IMAGE_X_RAW.equals(mimeType)
					&& !isMedia(format);
		case OTHER | RAW:
			return !ImageConstants.TIFF.equals(format)
					&& !ImageConstants.JPEG.equals(format) && !isDng(format)
					&& !isMedia(format);
		case OTHER | DNG:
			return !ImageConstants.TIFF.equals(format)
					&& !ImageConstants.JPEG.equals(format)
					&& !isRaw(format, mimeType) && !isMedia(format);
		case OTHER | DNG | RAW:
			return !ImageConstants.TIFF.equals(format)
					&& !ImageConstants.JPEG.equals(format) && !isMedia(format);
		case OTHER | JPEG:
			return !ImageConstants.TIFF.equals(format)
					&& !ImageConstants.IMAGE_X_RAW.equals(mimeType)
					&& !isMedia(format);
		case OTHER | JPEG | RAW:
			return !ImageConstants.TIFF.equals(format) && !isDng(format)
					&& !isMedia(format);
		case OTHER | JPEG | DNG:
			return !ImageConstants.TIFF.equals(format)
					&& !isRaw(format, mimeType) && !isMedia(format);
		case OTHER | JPEG | DNG | RAW:
			return !ImageConstants.TIFF.equals(format) && !isMedia(format);
		case OTHER | TIFF:
			return !ImageConstants.JPEG.equals(format)
					&& !ImageConstants.IMAGE_X_RAW.equals(mimeType)
					&& !isMedia(format);
		case OTHER | TIFF | RAW:
			return !ImageConstants.JPEG.equals(format) && !isDng(format)
					&& !isMedia(format);
		case OTHER | TIFF | DNG:
			return !ImageConstants.JPEG.equals(format)
					&& !isRaw(format, mimeType) && !isMedia(format);
		case OTHER | TIFF | DNG | RAW:
			return !ImageConstants.JPEG.equals(format) && !isMedia(format);
		case OTHER | TIFF | JPEG:
			return !ImageConstants.IMAGE_X_RAW.equals(mimeType)
					&& !isMedia(format);
		case OTHER | TIFF | JPEG | RAW:
			return !isDng(format) && !isMedia(format);
		case OTHER | TIFF | JPEG | DNG:
			return !isRaw(format, mimeType) && !isMedia(format);
		case OTHER | TIFF | JPEG | DNG | RAW:
			return !isMedia(format);
		case MEDIA:
			return isMedia(format);
		case MEDIA | RAW:
			return isMedia(format) || isRaw(format, mimeType);
		case MEDIA | DNG:
			return isMedia(format) || isDng(format);
		case MEDIA | RAW | DNG:
			return isMedia(format)
					|| ImageConstants.IMAGE_X_RAW.equals(mimeType);
		case MEDIA | JPEG:
			return isMedia(format) || ImageConstants.JPEG.equals(format);
		case MEDIA | JPEG | RAW:
			return isMedia(format) || ImageConstants.JPEG.equals(format)
					|| isRaw(format, mimeType);
		case MEDIA | JPEG | DNG:
			return isMedia(format) || ImageConstants.JPEG.equals(format)
					|| isDng(format);
		case MEDIA | JPEG | DNG | RAW:
			return isMedia(format) || ImageConstants.JPEG.equals(format)
					|| ImageConstants.IMAGE_X_RAW.equals(mimeType);
		case MEDIA | TIFF:
			return isMedia(format) || ImageConstants.TIFF.equals(format);
		case MEDIA | TIFF | RAW:
			return isMedia(format) || ImageConstants.TIFF.equals(format)
					|| isRaw(format, mimeType);
		case MEDIA | TIFF | DNG:
			return isMedia(format) || ImageConstants.TIFF.equals(format)
					|| isDng(format);
		case MEDIA | TIFF | DNG | RAW:
			return isMedia(format) || ImageConstants.TIFF.equals(format)
					|| ImageConstants.IMAGE_X_RAW.equals(mimeType);
		case MEDIA | TIFF | JPEG:
			return isMedia(format) || ImageConstants.TIFF.equals(format)
					|| ImageConstants.JPEG.equals(format);
		case MEDIA | TIFF | JPEG | RAW:
			return isMedia(format) || ImageConstants.JPEG.equals(format)
					|| ImageConstants.TIFF.equals(format)
					|| isRaw(format, mimeType);
		case MEDIA | TIFF | JPEG | DNG:
			return isMedia(format) || ImageConstants.TIFF.equals(format)
					|| ImageConstants.JPEG.equals(format) || isDng(format);
		case MEDIA | TIFF | JPEG | DNG | RAW:
			return isMedia(format) || ImageConstants.TIFF.equals(format)
					|| ImageConstants.JPEG.equals(format)
					|| ImageConstants.IMAGE_X_RAW.equals(mimeType);
		case MEDIA | OTHER:
			return !ImageConstants.TIFF.equals(format)
					&& !ImageConstants.JPEG.equals(format)
					&& !ImageConstants.IMAGE_X_RAW.equals(mimeType);
		case MEDIA | OTHER | RAW:
			return !ImageConstants.TIFF.equals(format)
					&& !ImageConstants.JPEG.equals(format) && !isDng(format);
		case MEDIA | OTHER | DNG:
			return !ImageConstants.TIFF.equals(format)
					&& !ImageConstants.JPEG.equals(format)
					&& !isRaw(format, mimeType);
		case MEDIA | OTHER | DNG | RAW:
			return !ImageConstants.TIFF.equals(format)
					&& !ImageConstants.JPEG.equals(format);
		case MEDIA | OTHER | JPEG:
			return !ImageConstants.TIFF.equals(format)
					&& !ImageConstants.IMAGE_X_RAW.equals(mimeType);
		case MEDIA | OTHER | JPEG | RAW:
			return !ImageConstants.TIFF.equals(format) && !isDng(format);
		case MEDIA | OTHER | JPEG | DNG:
			return !ImageConstants.TIFF.equals(format)
					&& !isRaw(format, mimeType);
		case MEDIA | OTHER | JPEG | DNG | RAW:
			return !ImageConstants.TIFF.equals(format);
		case MEDIA | OTHER | TIFF:
			return !ImageConstants.JPEG.equals(format)
					&& !ImageConstants.IMAGE_X_RAW.equals(mimeType);
		case MEDIA | OTHER | TIFF | RAW:
			return !ImageConstants.JPEG.equals(format) && !isDng(format);
		case MEDIA | OTHER | TIFF | DNG:
			return !ImageConstants.JPEG.equals(format)
					&& !isRaw(format, mimeType);
		case MEDIA | OTHER | TIFF | DNG | RAW:
			return !ImageConstants.JPEG.equals(format);
		case MEDIA | OTHER | TIFF | JPEG:
			return !ImageConstants.IMAGE_X_RAW.equals(mimeType);
		case MEDIA | OTHER | TIFF | JPEG | RAW:
			return !isDng(format);
		case MEDIA | OTHER | TIFF | JPEG | DNG:
			return !isRaw(format, mimeType);
		case MEDIA | OTHER | TIFF | JPEG | DNG | RAW:
			break;
		}
		return true;
	}

	private static boolean isMedia(String format) {
		return CORE_ACTIVATOR.getMediaSupport(format) != null;
	}

	private static boolean isDng(String format) {
		return ImageConstants.DNG_ADOBE_DIGITAL_NEGATIVE.equals(format)
				|| ImageConstants.DNG.equals(format);
	}

	private static boolean isRaw(String format, String mimeType) {
		return ImageConstants.IMAGE_X_RAW.equals(mimeType)
				&& !ImageConstants.DNG_ADOBE_DIGITAL_NEGATIVE.equals(format)
				&& !ImageConstants.DNG.equals(format);
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.db.internal.AssetFilter#getConstraint(com.db4o.query.Query
	 * )
	 */
	@Override
	public Constraint getConstraint(DbManager dbManager, Query query) {
		return getConstraint(dbManager, query, formats);
	}

	private Constraint getConstraint(DbManager dbManager, Query query,
			int formats) {
		Constraint formatConstraint = null;
		switch (formats) {
		case 0:
			break;
		case RAW:
			return createRawConstraint(query, true);
		case DNG:
			return createDngConstraint(query, true);
		case RAW | DNG:
			return query.descend(QueryField.MIMETYPE.getKey())
					.constrain(ImageConstants.IMAGE_X_RAW);
		case JPEG:
			return query.descend(QueryField.FORMAT.getKey())
					.constrain(ImageConstants.JPEG);
		case JPEG | RAW:
			formatConstraint = query.descend(QueryField.FORMAT.getKey())
					.constrain(ImageConstants.JPEG);
			return createRawConstraint(query, true).or(
					formatConstraint);
		case JPEG | DNG:
			formatConstraint = createDngConstraint(query, true);
			return query.descend(QueryField.FORMAT.getKey())
					.constrain(ImageConstants.JPEG).or(formatConstraint);
		case JPEG | DNG | RAW:
			formatConstraint = query.descend(QueryField.FORMAT.getKey())
					.constrain(ImageConstants.JPEG);
			return query.descend(QueryField.MIMETYPE.getKey())
					.constrain(ImageConstants.IMAGE_X_RAW).or(formatConstraint);
		case TIFF:
			return query.descend(QueryField.FORMAT.getKey())
					.constrain(ImageConstants.TIFF);
		case TIFF | RAW:
			formatConstraint = query.descend(QueryField.FORMAT.getKey())
					.constrain(ImageConstants.TIFF);
			return createRawConstraint(query, true).or(
					formatConstraint);
		case TIFF | DNG:
			formatConstraint = createDngConstraint(query, true);
			return query.descend(QueryField.FORMAT.getKey())
					.constrain(ImageConstants.TIFF).or(formatConstraint);
		case TIFF | DNG | RAW:
			formatConstraint = query.descend(QueryField.FORMAT.getKey())
					.constrain(ImageConstants.TIFF);
			return query.descend(QueryField.MIMETYPE.getKey())
					.constrain(ImageConstants.IMAGE_X_RAW).or(formatConstraint);
		case TIFF | JPEG:
			formatConstraint = query.descend(QueryField.FORMAT.getKey())
					.constrain(ImageConstants.TIFF);
			return query.descend(QueryField.FORMAT.getKey())
					.constrain(ImageConstants.JPEG).or(formatConstraint);
		case TIFF | JPEG | RAW:
			formatConstraint = query.descend(QueryField.FORMAT.getKey())
					.constrain(ImageConstants.TIFF);
			formatConstraint = query.descend(QueryField.FORMAT.getKey())
					.constrain(ImageConstants.JPEG).or(formatConstraint);
			return createRawConstraint(query, true).or(
					formatConstraint);
		case TIFF | JPEG | DNG:
			formatConstraint = createDngConstraint(query, true);
			formatConstraint = query.descend(QueryField.FORMAT.getKey())
					.constrain(ImageConstants.JPEG).or(formatConstraint);
			return query.descend(QueryField.FORMAT.getKey())
					.constrain(ImageConstants.TIFF).or(formatConstraint);
		case TIFF | JPEG | DNG | RAW:
			formatConstraint = query.descend(QueryField.FORMAT.getKey())
					.constrain(ImageConstants.JPEG);
			formatConstraint = query.descend(QueryField.FORMAT.getKey())
					.constrain(ImageConstants.TIFF).or(formatConstraint);
			return query.descend(QueryField.MIMETYPE.getKey())
					.constrain(ImageConstants.IMAGE_X_RAW).or(formatConstraint);
		case OTHER:
			formatConstraint = query.descend(QueryField.FORMAT.getKey())
					.constrain(ImageConstants.JPEG).not();
			formatConstraint = query.descend(QueryField.FORMAT.getKey())
					.constrain(ImageConstants.TIFF).not().and(formatConstraint);
			return query.descend(QueryField.MIMETYPE.getKey())
					.constrain(ImageConstants.IMAGE_X_RAW).not()
					.and(formatConstraint);
		case OTHER | RAW:
			formatConstraint = createDngConstraint(query, false);
			formatConstraint = query.descend(QueryField.FORMAT.getKey())
					.constrain(ImageConstants.JPEG).not().and(formatConstraint);
			return query.descend(QueryField.FORMAT.getKey())
					.constrain(ImageConstants.TIFF).not().and(formatConstraint);
		case OTHER | DNG:
			formatConstraint = query.descend(QueryField.FORMAT.getKey())
					.constrain(ImageConstants.JPEG).not();
			formatConstraint = query.descend(QueryField.FORMAT.getKey())
					.constrain(ImageConstants.TIFF).not().and(formatConstraint);
			return createRawConstraint(query, false).and(
					formatConstraint);
		case OTHER | DNG | RAW:
			formatConstraint = query.descend(QueryField.FORMAT.getKey())
					.constrain(ImageConstants.JPEG).not();
			return query.descend(QueryField.FORMAT.getKey())
					.constrain(ImageConstants.TIFF).not().and(formatConstraint);
		case OTHER | JPEG:
			formatConstraint = query.descend(QueryField.FORMAT.getKey())
					.constrain(ImageConstants.TIFF).not();
			return query.descend(QueryField.MIMETYPE.getKey())
					.constrain(ImageConstants.IMAGE_X_RAW).not()
					.and(formatConstraint);
		case OTHER | JPEG | RAW:
			formatConstraint = createDngConstraint(query, false);
			return query.descend(QueryField.FORMAT.getKey())
					.constrain(ImageConstants.TIFF).not().and(formatConstraint);
		case OTHER | JPEG | DNG:
			formatConstraint = query.descend(QueryField.FORMAT.getKey())
					.constrain(ImageConstants.TIFF).not();
			return createRawConstraint(query, false).and(
					formatConstraint);
		case OTHER | JPEG | DNG | RAW:
			return query.descend(QueryField.FORMAT.getKey())
					.constrain(ImageConstants.TIFF).not();
		case OTHER | TIFF:
			formatConstraint = query.descend(QueryField.FORMAT.getKey())
					.constrain(ImageConstants.JPEG).not();
			return query.descend(QueryField.MIMETYPE.getKey())
					.constrain(ImageConstants.IMAGE_X_RAW).not()
					.and(formatConstraint);
		case OTHER | TIFF | RAW:
			formatConstraint = createDngConstraint(query, false);
			return query.descend(QueryField.FORMAT.getKey())
					.constrain(ImageConstants.JPEG).not().and(formatConstraint);
		case OTHER | TIFF | DNG:
			formatConstraint = query.descend(QueryField.FORMAT.getKey())
					.constrain(ImageConstants.JPEG).not();
			return createRawConstraint(query, false).and(
					formatConstraint);
		case OTHER | TIFF | DNG | RAW:
			return query.descend(QueryField.FORMAT.getKey())
					.constrain(ImageConstants.JPEG).not();
		case OTHER | TIFF | JPEG:
			return query.descend(QueryField.MIMETYPE.getKey())
					.constrain(ImageConstants.IMAGE_X_RAW).not();
		case OTHER | TIFF | JPEG | RAW:
			return createDngConstraint(query, false);
		case OTHER | TIFF | JPEG | DNG:
			return createRawConstraint(query, false);
		case OTHER | TIFF | JPEG | DNG | RAW:
			return null;
		case MEDIA:
			return createMediaConstraint(query);
		default:
			if ((formats & MEDIA) != 0) {
				formatConstraint = createMediaConstraint(query);
				Constraint c2 = getConstraint(dbManager, query, formats-MEDIA);
				if (c2 != null)
					return formatConstraint == null ? c2 : formatConstraint.or(c2);
			}
			return formatConstraint;
		}
		return null;
	}

	private static Constraint createMediaConstraint(Query query) {
		Constraint c1 = null;
		Set<String> mediaFormats = CORE_ACTIVATOR.getMediaFormats();
		for (String format : mediaFormats) {
			Constraint c2 = query.descend(QueryField.FORMAT.getKey())
					.constrain(format);
			c1 = c1 == null ? c2 : c1.or(c2);
		}
		return c1;
	}

	private static Constraint createDngConstraint(Query query, boolean accept) {
		if (accept) {
			Constraint formatConstraint = query.descend(
					QueryField.FORMAT.getKey()).constrain(
					ImageConstants.DNG_ADOBE_DIGITAL_NEGATIVE);
			return query.descend(QueryField.FORMAT.getKey())
					.constrain(ImageConstants.DNG).or(formatConstraint);
		}
		Constraint formatConstraint = query.descend(QueryField.FORMAT.getKey())
				.constrain(ImageConstants.DNG_ADOBE_DIGITAL_NEGATIVE).not();
		return query.descend(QueryField.FORMAT.getKey())
				.constrain(ImageConstants.DNG).not().and(formatConstraint);
	}

	private static Constraint createRawConstraint(Query query, boolean accept) {
		if (accept) {
			Constraint formatConstraint = query.descend(
					QueryField.MIMETYPE.getKey()).constrain(
					ImageConstants.IMAGE_X_RAW);
			formatConstraint = query.descend(QueryField.FORMAT.getKey())
					.constrain(ImageConstants.DNG_ADOBE_DIGITAL_NEGATIVE).not()
					.and(formatConstraint);
			return query.descend(QueryField.FORMAT.getKey())
					.constrain(ImageConstants.DNG).not().and(formatConstraint);
		}
		Constraint formatConstraint = query
				.descend(QueryField.MIMETYPE.getKey())
				.constrain(ImageConstants.IMAGE_X_RAW).not();
		formatConstraint = query.descend(QueryField.FORMAT.getKey())
				.constrain(ImageConstants.DNG_ADOBE_DIGITAL_NEGATIVE)
				.or(formatConstraint);
		return query.descend(QueryField.FORMAT.getKey())
				.constrain(ImageConstants.DNG).or(formatConstraint);
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.db.ITypeFilter#getFormats()
	 */
	public int getFormats() {
		return formats;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ITypeFilter)
			return formats == ((ITypeFilter) obj).getFormats();
		return false;
	}

	@Override
	public int hashCode() {
		return formats;
	}
}
