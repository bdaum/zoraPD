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
 * (c) 2009-2019 Berthold Daum  
 */
package com.bdaum.zoom.db.internal;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.ITypeFilter;
import com.bdaum.zoom.image.ImageConstants;
import com.db4o.query.Constraint;
import com.db4o.query.Query;

public class TypeFilter extends AssetFilter implements ITypeFilter {

	private int formats;

	public TypeFilter(int formats) {
		this.formats = formats;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.db.internal.AssetFilter#accept(com.bdaum.zoom.cat.model
	 * .asset.Asset)
	 */
	@Override
	public boolean accept(Asset asset) {
		String format = asset.getFormat();
		String mimeType = asset.getMimeType();
		switch (formats) {
		// case 0:
		// break;
		case RAW:
			return isRaw(mimeType);
		case DNG:
			return isDng(mimeType);
		case RAW | DNG:
			return isRaw(mimeType) || isDng(mimeType);
		case JPEG:
			return ImageConstants.JPEG.equals(format);
		case JPEG | RAW:
			return ImageConstants.JPEG.equals(format) || isRaw(mimeType);
		case JPEG | DNG:
			return ImageConstants.JPEG.equals(format) || isDng(mimeType);
		case JPEG | DNG | RAW:
			return ImageConstants.JPEG.equals(format) || isRaw(mimeType) || isDng(mimeType);
		case TIFF:
			return ImageConstants.TIFF.equals(format);

		case TIFF | RAW:
			return ImageConstants.TIFF.equals(format) || isRaw(mimeType);
		case TIFF | DNG:
			return ImageConstants.TIFF.equals(format) || isDng(mimeType);
		case TIFF | DNG | RAW:
			return ImageConstants.TIFF.equals(format) || isRaw(mimeType) || isDng(mimeType);
		case TIFF | JPEG:
			return ImageConstants.TIFF.equals(format) || ImageConstants.JPEG.equals(format);
		case TIFF | JPEG | RAW:
			return ImageConstants.JPEG.equals(format) || ImageConstants.TIFF.equals(format) || isRaw(mimeType);
		case TIFF | JPEG | DNG:
			return ImageConstants.TIFF.equals(format) || ImageConstants.JPEG.equals(format) || isDng(mimeType);
		case TIFF | JPEG | DNG | RAW:
			return ImageConstants.TIFF.equals(format) || ImageConstants.JPEG.equals(format) || isRaw(mimeType)
					|| isDng(mimeType);
		case OTHER:
			return !ImageConstants.TIFF.equals(format) && !ImageConstants.JPEG.equals(format) && !isRaw(mimeType)
					&& !isDng(mimeType) && !isMedia(mimeType);
		case OTHER | RAW:
			return !ImageConstants.TIFF.equals(format) && !ImageConstants.JPEG.equals(format) && !isDng(mimeType)
					&& !isMedia(mimeType);
		case OTHER | DNG:
			return !ImageConstants.TIFF.equals(format) && !ImageConstants.JPEG.equals(format) && !isRaw(mimeType)
					&& !isMedia(mimeType);
		case OTHER | DNG | RAW:
			return !ImageConstants.TIFF.equals(format) && !ImageConstants.JPEG.equals(format) && !isMedia(mimeType);
		case OTHER | JPEG:
			return !ImageConstants.TIFF.equals(format) && !isRaw(mimeType) && !isDng(mimeType) && !isMedia(mimeType);
		case OTHER | JPEG | RAW:
			return !ImageConstants.TIFF.equals(format) && !isDng(mimeType) && !isMedia(mimeType);
		case OTHER | JPEG | DNG:
			return !ImageConstants.TIFF.equals(format) && !isRaw(mimeType) && !isMedia(mimeType);
		case OTHER | JPEG | DNG | RAW:
			return !ImageConstants.TIFF.equals(format) && !isMedia(mimeType);
		case OTHER | TIFF:
			return !ImageConstants.JPEG.equals(format) && !isRaw(mimeType) && !isDng(mimeType) && !isMedia(mimeType);
		case OTHER | TIFF | RAW:
			return !ImageConstants.JPEG.equals(format) && !isDng(mimeType) && !isMedia(mimeType);
		case OTHER | TIFF | DNG:
			return !ImageConstants.JPEG.equals(format) && !isRaw(mimeType) && !isMedia(mimeType);
		case OTHER | TIFF | DNG | RAW:
			return !ImageConstants.JPEG.equals(format) && !isMedia(mimeType);
		case OTHER | TIFF | JPEG:
			return !isRaw(mimeType) && !isDng(mimeType) && !isMedia(mimeType);
		case OTHER | TIFF | JPEG | RAW:
			return !isDng(format) && !isMedia(mimeType);
		case OTHER | TIFF | JPEG | DNG:
			return !isRaw(mimeType) && !isMedia(mimeType);
		case OTHER | TIFF | JPEG | DNG | RAW:
			return !isMedia(mimeType);
		case MEDIA:
			return isMedia(mimeType);
		case MEDIA | RAW:
			return isMedia(mimeType) || isRaw(mimeType);
		case MEDIA | DNG:
			return isMedia(mimeType) || isDng(mimeType);
		case MEDIA | RAW | DNG:
			return isMedia(mimeType) || isRaw(mimeType) || isDng(mimeType);
		case MEDIA | JPEG:
			return isMedia(mimeType) || ImageConstants.JPEG.equals(format);
		case MEDIA | JPEG | RAW:
			return isMedia(mimeType) || ImageConstants.JPEG.equals(format) || isRaw(mimeType);
		case MEDIA | JPEG | DNG:
			return isMedia(mimeType) || ImageConstants.JPEG.equals(format) || isDng(mimeType);
		case MEDIA | JPEG | DNG | RAW:
			return isMedia(mimeType) || ImageConstants.JPEG.equals(format) || isRaw(mimeType) || isDng(mimeType);
		case MEDIA | TIFF:
			return isMedia(mimeType) || ImageConstants.TIFF.equals(format);
		case MEDIA | TIFF | RAW:
			return isMedia(mimeType) || ImageConstants.TIFF.equals(format) || isRaw(mimeType);
		case MEDIA | TIFF | DNG:
			return isMedia(mimeType) || ImageConstants.TIFF.equals(format) || isDng(mimeType);
		case MEDIA | TIFF | DNG | RAW:
			return isMedia(mimeType) || ImageConstants.TIFF.equals(format) || isRaw(mimeType) || isDng(mimeType);
		case MEDIA | TIFF | JPEG:
			return isMedia(mimeType) || ImageConstants.TIFF.equals(format) || ImageConstants.JPEG.equals(format);
		case MEDIA | TIFF | JPEG | RAW:
			return isMedia(mimeType) || ImageConstants.JPEG.equals(format) || ImageConstants.TIFF.equals(format)
					|| isRaw(mimeType);
		case MEDIA | TIFF | JPEG | DNG:
			return isMedia(mimeType) || ImageConstants.TIFF.equals(format) || ImageConstants.JPEG.equals(format)
					|| isDng(mimeType);
		case MEDIA | TIFF | JPEG | DNG | RAW:
			return isMedia(mimeType) || ImageConstants.TIFF.equals(format) || ImageConstants.JPEG.equals(format)
					|| isRaw(mimeType) || isDng(mimeType);
		case MEDIA | OTHER:
			return !ImageConstants.TIFF.equals(format) && !ImageConstants.JPEG.equals(format) && !isRaw(mimeType)
					&& !isDng(mimeType);
		case MEDIA | OTHER | RAW:
			return !ImageConstants.TIFF.equals(format) && !ImageConstants.JPEG.equals(format) && !isDng(mimeType);
		case MEDIA | OTHER | DNG:
			return !ImageConstants.TIFF.equals(format) && !ImageConstants.JPEG.equals(format) && !isRaw(mimeType);
		case MEDIA | OTHER | DNG | RAW:
			return !ImageConstants.TIFF.equals(format) && !ImageConstants.JPEG.equals(format);
		case MEDIA | OTHER | JPEG:
			return !ImageConstants.TIFF.equals(format) && !isRaw(mimeType) && !isDng(mimeType);
		case MEDIA | OTHER | JPEG | RAW:
			return !ImageConstants.TIFF.equals(format) && !isDng(mimeType);
		case MEDIA | OTHER | JPEG | DNG:
			return !ImageConstants.TIFF.equals(format) && !isRaw(mimeType);
		case MEDIA | OTHER | JPEG | DNG | RAW:
			return !ImageConstants.TIFF.equals(format);
		case MEDIA | OTHER | TIFF:
			return !ImageConstants.JPEG.equals(format) && !isRaw(mimeType) && !isDng(mimeType);
		case MEDIA | OTHER | TIFF | RAW:
			return !ImageConstants.JPEG.equals(format) && !isDng(mimeType);
		case MEDIA | OTHER | TIFF | DNG:
			return !ImageConstants.JPEG.equals(format) && !isRaw(mimeType);
		case MEDIA | OTHER | TIFF | DNG | RAW:
			return !ImageConstants.JPEG.equals(format);
		case MEDIA | OTHER | TIFF | JPEG:
			return !isRaw(mimeType) && !isDng(mimeType);
		case MEDIA | OTHER | TIFF | JPEG | RAW:
			return !isDng(mimeType);
		case MEDIA | OTHER | TIFF | JPEG | DNG:
			return !isRaw(mimeType);
		// case MEDIA | OTHER | TIFF | JPEG | DNG | RAW:
		// break;
		}
		return true;
	}

	private static boolean isMedia(String mimeType) {
		return mimeType == null || !mimeType.startsWith("image/"); //$NON-NLS-1$
	}

	private static boolean isDng(String mimeType) {
		return ImageConstants.IMAGE_X_DNG.equals(mimeType);
	}

	private static boolean isRaw(String mimeType) {
		return ImageConstants.IMAGE_X_RAW.equals(mimeType);
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.db.internal.AssetFilter#getConstraint(com.db4o.query.Query )
	 */
	@Override
	public Constraint getConstraint(DbManager dbManager, Query query) {
		Constraint formatConstraint = null;
		switch (formats) {
		// case 0:
		// return null;
		case RAW:
			return createRawConstraint(query, true);

		case DNG:
			return createDngConstraint(query, true);

		case RAW | DNG:
			return createRawConstraint(query, true).or(createDngConstraint(query, true));

		case JPEG:
			return query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.JPEG);

		case JPEG | RAW:
			formatConstraint = query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.JPEG);
			return createRawConstraint(query, true).or(formatConstraint);

		case JPEG | DNG:
			formatConstraint = createDngConstraint(query, true);
			return query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.JPEG).or(formatConstraint);

		case JPEG | DNG | RAW:
			formatConstraint = query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.JPEG);
			return createRawConstraint(query, true).or(createDngConstraint(query, true)).or(formatConstraint);

		case TIFF:
			return query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.TIFF);

		case TIFF | RAW:
			formatConstraint = query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.TIFF);
			return createRawConstraint(query, true).or(formatConstraint);

		case TIFF | DNG:
			formatConstraint = createDngConstraint(query, true);
			return query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.TIFF).or(formatConstraint);

		case TIFF | DNG | RAW:
			formatConstraint = query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.TIFF);
			return createRawConstraint(query, true).or(createDngConstraint(query, true)).or(formatConstraint);

		case TIFF | JPEG:
			formatConstraint = query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.TIFF);
			return query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.JPEG).or(formatConstraint);

		case TIFF | JPEG | RAW:
			formatConstraint = query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.TIFF);
			formatConstraint = query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.JPEG)
					.or(formatConstraint);
			return createRawConstraint(query, true).or(formatConstraint);

		case TIFF | JPEG | DNG:
			formatConstraint = createDngConstraint(query, true);
			formatConstraint = query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.JPEG)
					.or(formatConstraint);
			return query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.TIFF).or(formatConstraint);

		case TIFF | JPEG | DNG | RAW:
			formatConstraint = query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.JPEG);
			formatConstraint = query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.TIFF)
					.or(formatConstraint);
			return createRawConstraint(query, true).or(createDngConstraint(query, true)).or(formatConstraint);

		case OTHER:
			formatConstraint = createImageConstraint(query, true);
			formatConstraint = query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.JPEG).not()
					.and(formatConstraint);
			formatConstraint = query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.TIFF).not()
					.and(formatConstraint);
			return createRawConstraint(query, false).and(createDngConstraint(query, false)).and(formatConstraint);

		case OTHER | RAW:
			formatConstraint = createImageConstraint(query, true);
			formatConstraint = createDngConstraint(query, false).and(formatConstraint);
			formatConstraint = query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.JPEG).not()
					.and(formatConstraint);
			return query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.TIFF).not().and(formatConstraint);

		case OTHER | DNG:
			formatConstraint = createImageConstraint(query, true);
			formatConstraint = createRawConstraint(query, false).and(formatConstraint);
			formatConstraint = query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.JPEG).not()
					.and(formatConstraint);
			return query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.TIFF).not().and(formatConstraint);

		case OTHER | DNG | RAW:
			formatConstraint = createImageConstraint(query, true);
			formatConstraint = query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.JPEG).not()
					.and(formatConstraint);
			return query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.TIFF).not().and(formatConstraint);

		case OTHER | JPEG:
			formatConstraint = createImageConstraint(query, true);
			formatConstraint = query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.TIFF).not()
					.and(formatConstraint);
			return createRawConstraint(query, false).and(createDngConstraint(query, false)).and(formatConstraint);

		case OTHER | JPEG | RAW:
			formatConstraint = createImageConstraint(query, true);
			formatConstraint = createDngConstraint(query, false).and(formatConstraint);
			return query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.TIFF).not().and(formatConstraint);

		case OTHER | JPEG | DNG:
			formatConstraint = createImageConstraint(query, true);
			formatConstraint = query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.TIFF).not()
					.and(formatConstraint);
			return createRawConstraint(query, false).and(formatConstraint);

		case OTHER | JPEG | DNG | RAW:
			formatConstraint = createImageConstraint(query, true);
			return query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.TIFF).not().and(formatConstraint);

		case OTHER | TIFF:
			formatConstraint = createImageConstraint(query, true);
			formatConstraint = query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.JPEG).not()
					.and(formatConstraint);
			return createRawConstraint(query, false).and(createDngConstraint(query, false)).and(formatConstraint);

		case OTHER | TIFF | RAW:
			formatConstraint = createImageConstraint(query, true);
			return query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.JPEG).not()
					.and(createDngConstraint(query, false)).and(formatConstraint);

		case OTHER | TIFF | DNG:
			formatConstraint = createImageConstraint(query, true);
			formatConstraint = query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.JPEG).not()
					.and(formatConstraint);
			return createRawConstraint(query, false).and(formatConstraint);

		case OTHER | TIFF | DNG | RAW:
			formatConstraint = createImageConstraint(query, true);
			return query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.JPEG).not().and(formatConstraint);

		case OTHER | TIFF | JPEG:
			formatConstraint = createImageConstraint(query, true);
			return createRawConstraint(query, false).and(createDngConstraint(query, false)).and(formatConstraint);

		case OTHER | TIFF | JPEG | RAW:
			formatConstraint = createImageConstraint(query, true);
			return createDngConstraint(query, false).and(formatConstraint);

		case OTHER | TIFF | JPEG | DNG:
			formatConstraint = createImageConstraint(query, true);
			return createRawConstraint(query, false).and(formatConstraint);

		case OTHER | TIFF | JPEG | DNG | RAW:
			return createImageConstraint(query, true);

		case MEDIA:
			return createImageConstraint(query, false);
		case MEDIA | RAW:
			return createImageConstraint(query, false).or(createRawConstraint(query, true));
		case MEDIA | DNG:
			return createImageConstraint(query, false).or(createDngConstraint(query, true));
		case MEDIA | RAW | DNG:
			return createImageConstraint(query, false).or(createRawConstraint(query, true))
					.or(createDngConstraint(query, true));
		case MEDIA | JPEG:
			return createImageConstraint(query, false)
					.or(query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.JPEG));
		case MEDIA | JPEG | RAW:
			formatConstraint = createImageConstraint(query, false)
					.or(query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.JPEG));
			return createRawConstraint(query, true).or(formatConstraint);
		case MEDIA | JPEG | DNG:
			formatConstraint = createImageConstraint(query, false)
					.or(query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.JPEG));
			return createDngConstraint(query, true).or(formatConstraint);
		case MEDIA | JPEG | DNG | RAW:
			formatConstraint = createImageConstraint(query, false)
					.or(query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.JPEG));
			return createRawConstraint(query, true).or(createDngConstraint(query, true)).or(formatConstraint);
		case MEDIA | TIFF:
			return createImageConstraint(query, false)
					.or(query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.TIFF));
		case MEDIA | TIFF | RAW:
			formatConstraint = createImageConstraint(query, false)
					.or(query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.TIFF));
			return createRawConstraint(query, true).or(formatConstraint);
		case MEDIA | TIFF | DNG:
			formatConstraint = createImageConstraint(query, false)
					.or(query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.TIFF));
			return createDngConstraint(query, true).or(formatConstraint);
		case MEDIA | TIFF | DNG | RAW:
			formatConstraint = createImageConstraint(query, false)
					.or(query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.TIFF));
			return createRawConstraint(query, true).or(createDngConstraint(query, true)).or(formatConstraint);
		case MEDIA | TIFF | JPEG:
			formatConstraint = createImageConstraint(query, false)
					.or(query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.TIFF));
			return query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.JPEG).or(formatConstraint);
		case MEDIA | TIFF | JPEG | RAW:
			formatConstraint = createImageConstraint(query, false)
					.or(query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.TIFF));
			formatConstraint = query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.JPEG)
					.or(formatConstraint);
			return createRawConstraint(query, true).or(formatConstraint);
		case MEDIA | TIFF | JPEG | DNG:
			formatConstraint = createImageConstraint(query, false)
					.or(query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.TIFF));
			formatConstraint = createDngConstraint(query, true).or(formatConstraint);
			return query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.JPEG).or(formatConstraint);
		case MEDIA | TIFF | JPEG | DNG | RAW:
			formatConstraint = createImageConstraint(query, false)
					.or(query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.TIFF));
			formatConstraint = query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.JPEG)
					.or(formatConstraint);
			return createRawConstraint(query, true).or(createDngConstraint(query, true)).or(formatConstraint);
		case MEDIA | OTHER:
			formatConstraint = query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.JPEG).not();
			formatConstraint = query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.TIFF).not()
					.and(formatConstraint);
			return createRawConstraint(query, false).and(createDngConstraint(query, false)).and(formatConstraint);
		case MEDIA | OTHER | RAW:
			formatConstraint = createDngConstraint(query, false);
			formatConstraint = query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.JPEG).not()
					.and(formatConstraint);
			return query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.TIFF).not().and(formatConstraint);
		case MEDIA | OTHER | DNG:
			formatConstraint = createRawConstraint(query, false);
			formatConstraint = query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.JPEG).not()
					.and(formatConstraint);
			return query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.TIFF).not().and(formatConstraint);
		case MEDIA | OTHER | DNG | RAW:
			formatConstraint = query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.JPEG).not();
			return query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.TIFF).not().and(formatConstraint);
		case MEDIA | OTHER | JPEG:
			formatConstraint = query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.TIFF).not()
					.and(formatConstraint);
			return createRawConstraint(query, false).and(createDngConstraint(query, false)).and(formatConstraint);
		case MEDIA | OTHER | JPEG | RAW:
			formatConstraint = createDngConstraint(query, false);
			return query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.TIFF).not().and(formatConstraint);
		case MEDIA | OTHER | JPEG | DNG:
			formatConstraint = query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.TIFF).not();
			return createRawConstraint(query, false).and(formatConstraint);
		case MEDIA | OTHER | JPEG | DNG | RAW:
			return query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.TIFF).not();
		case MEDIA | OTHER | TIFF:
			formatConstraint = query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.JPEG).not();
			return createRawConstraint(query, false).and(createDngConstraint(query, false)).and(formatConstraint);
		case MEDIA | OTHER | TIFF | RAW:
			return query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.JPEG).not()
					.and(createDngConstraint(query, false));
		case MEDIA | OTHER | TIFF | DNG:
			formatConstraint = query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.JPEG).not();
			return createRawConstraint(query, false).and(formatConstraint);
		case MEDIA | OTHER | TIFF | DNG | RAW:
			return query.descend(QueryField.FORMAT.getKey()).constrain(ImageConstants.JPEG).not();
		case MEDIA | OTHER | TIFF | JPEG:
			return createRawConstraint(query, false).and(createDngConstraint(query, false));
		case MEDIA | OTHER | TIFF | JPEG | RAW:
			return createDngConstraint(query, false);
		case MEDIA | OTHER | TIFF | JPEG | DNG:
			return createRawConstraint(query, false);
		// case MEDIA | OTHER | TIFF | JPEG | DNG | RAW:
		// return null;
		}
		return null;
	}

	private static Constraint createDngConstraint(Query query, boolean accept) {
		Constraint formatConstraint = query.descend(QueryField.MIMETYPE.getKey()).constrain(ImageConstants.IMAGE_X_DNG);
		return accept ? formatConstraint : formatConstraint.not();
	}

	private static Constraint createRawConstraint(Query query, boolean accept) {
		Constraint formatConstraint = query.descend(QueryField.MIMETYPE.getKey()).constrain(ImageConstants.IMAGE_X_RAW);
		return accept ? formatConstraint : formatConstraint.not();
	}

	private static Constraint createImageConstraint(Query query, boolean accept) {
		Constraint formatConstraint = query.descend(QueryField.MIMETYPE.getKey()).constrain("image/").startsWith(true); //$NON-NLS-1$
		return accept ? formatConstraint : formatConstraint.not();
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
