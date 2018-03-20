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
 * (c) 2009-2018 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.preferences;

import java.io.File;
import java.util.Map;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.RGB;

import com.bdaum.zoom.batch.internal.BatchActivator;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.program.IRawConverter;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;

@SuppressWarnings("restriction")
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	public static final String DEFAULTKEYWORDFILTER = "geo:lat=*\ngeo:lon=*\ngeodat\ngeotagged"; //$NON-NLS-1$
	public static final String DEFAULTWATCHFILTER = "-.*/; -CaptureOne/; -SILKYPIX_DS/; -Cache/; -temp/"; //$NON-NLS-1$

	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences defaultNode = DefaultScope.INSTANCE.getNode(UiActivator.PLUGIN_ID);
		defaultNode.put(PreferenceConstants.BACKGROUNDCOLOR, PreferenceConstants.BACKGROUNDCOLOR_DARKGREY);
		defaultNode.put(PreferenceConstants.DISTANCEUNIT, "k"); //$NON-NLS-1$
		defaultNode.put(PreferenceConstants.DIMUNIT, "c"); //$NON-NLS-1$
		defaultNode.putBoolean(PreferenceConstants.AUTOEXPORT, true);
		defaultNode.put(PreferenceConstants.DERIVERELATIONS, Constants.DERIVE_ALL);
		defaultNode.put(PreferenceConstants.SHOWRATING, PreferenceConstants.SHOWRATING_SIZE);
		defaultNode.putBoolean(PreferenceConstants.SHOWLOCATION, true);
		defaultNode.putBoolean(PreferenceConstants.SHOWROTATEBUTTONS, true);
		defaultNode.putBoolean(PreferenceConstants.SHOWVOICENOTE, true);
		defaultNode.putBoolean(PreferenceConstants.SHOWEXPANDCOLLAPSE, true);
		defaultNode.put(PreferenceConstants.SHOWCOLORCODE, PreferenceConstants.COLORCODE_MANUAL);
		defaultNode.putBoolean(PreferenceConstants.SHOWDONEMARK, true);
		defaultNode.put(PreferenceConstants.THUMBNAILTEMPLATE, "{creationDate} {meta=exposureTime} {meta=fNumber}"); //$NON-NLS-1$
		defaultNode.putInt(PreferenceConstants.LABELFONTSIZE,
				JFaceResources.getDefaultFont().getFontData()[0].getHeight());
		defaultNode.putInt(PreferenceConstants.SHOWLABEL, Constants.TITLE_LABEL);
		defaultNode.put(PreferenceConstants.WATCHFILTER, DEFAULTWATCHFILTER);
		defaultNode.put(PreferenceConstants.KEYWORDFILTER, DEFAULTKEYWORDFILTER);
		defaultNode.put(PreferenceConstants.RAWIMPORT,
				Constants.WIN32 || Constants.OSX ? Constants.RAWIMPORT_BOTH : Constants.RAWIMPORT_ONLYRAW);
		defaultNode.putInt(PreferenceConstants.BACKUPINTERVAL, 7);
		defaultNode.putInt(PreferenceConstants.BACKUPGENERATIONS, Integer.MAX_VALUE);
		defaultNode.putInt(PreferenceConstants.INACTIVITYINTERVAL, 15);
		defaultNode.putBoolean(PreferenceConstants.ALARMONFINISH, true);
		defaultNode.putBoolean(PreferenceConstants.ALARMONPROMPT, true);
		defaultNode.putBoolean(PreferenceConstants.ADVANCEDGRAPHICS, false);
		defaultNode.putBoolean(PreferenceConstants.AUTODERIVE, true);
		defaultNode.putBoolean(PreferenceConstants.APPLYXMPTODERIVATES, true);
		defaultNode.putInt(PreferenceConstants.COLORPROFILE, ImageConstants.SRGB);
		defaultNode.put(PreferenceConstants.BWFILTER, StringConverter.asString(new RGB(64, 128, 64)));
		defaultNode.put(PreferenceConstants.DNGFOLDER, "dng"); //$NON-NLS-1$

		defaultNode.put(PreferenceConstants.FILEASSOCIATION, FileAssociationsPreferencePage.DFLTMAPPINGS);
		StringBuilder sb = new StringBuilder();
		StringBuilder sbh = new StringBuilder();
		StringBuilder sbt = new StringBuilder();
		StringBuilder sbe = new StringBuilder();
		for (QueryField qfield : QueryField.getQueryFields())
			if (qfield.hasLabel() && qfield.getChildren().length == 0) {
				String id = qfield.getId();
				sbe.append(id).append('\n');
				if (qfield.isEssential())
					sb.append(id).append('\n');
				if (qfield.isHover())
					sbh.append(id).append('\n');
				if (qfield.getTolerance() != 0f)
					sbt.append(id).append("=").append(qfield.getTolerance()).append('\n'); //$NON-NLS-1$
			}
		defaultNode.put(PreferenceConstants.ESSENTIALMETADATA, sb.toString());
		defaultNode.put(PreferenceConstants.HOVERMETADATA, sbh.toString());
		defaultNode.put(PreferenceConstants.METADATATOLERANCES, sbt.toString());
		defaultNode.put(PreferenceConstants.EXPORTMETADATA, sbe.toString());
		sb.setLength(0);
		sb.append(QueryField.NAME.getKey()).append('\n').append(QueryField.EXIF_ORIGINALFILENAME.getKey()).append('\n')
				.append(QueryField.URI.getKey()).append('\n').append(QueryField.VOLUME.getKey()).append('\n')
				.append(QueryField.LASTMOD.getKey()).append('\n').append(QueryField.EXIF_DATETIMEORIGINAL.getKey())
				.append('\n').append(QueryField.IPTC_DATECREATED.getKey()).append('\n')
				.append(QueryField.IMPORTDATE.getKey()).append('\n').append(QueryField.ALBUM.getKey()).append('\n')
				.append(QueryField.IPTC_KEYWORDS.getKey()).append('\n').append(QueryField.IPTC_CATEGORY.getKey())
				.append('\n').append(QueryField.EXIF_GPSLATITUDE.getKey()).append('\n')
				.append(QueryField.EXIF_GPSLONGITUDE.getKey()).append('\n').append(QueryField.FORMAT.getKey())
				.append('\n').append(QueryField.MIMETYPE.getKey()).append('\n').append(QueryField.RATING.getKey())
				.append('\n');
		defaultNode.put(PreferenceConstants.METADATATUNING, sb.toString());
		sb.setLength(0);
		sb.append(QueryField.NAME.getKey()).append('\n').append(QueryField.FORMAT.getKey()).append('\n')
				.append(QueryField.EXIF_DATETIMEORIGINAL.getKey()).append('\n').append(QueryField.IPTC_TITLE.getKey())
				.append('\n').append(QueryField.EXIF_FOCALLENGTHIN35MMFILM.getKey()).append('\n')
				.append(QueryField.EXIF_EXPOSURETIME.getKey()).append('\n').append(QueryField.EXIF_FNUMBER.getKey())
				.append('\n');
		defaultNode.put(PreferenceConstants.TABLECOLUMNS, sb.toString());
		defaultNode.putInt(PreferenceConstants.UNDOLEVELS, 9);
		defaultNode.putDouble(PreferenceConstants.AUDIOSAMPLINGRATE, PreferenceConstants.AUDIO22KHZ);
		defaultNode.putInt(PreferenceConstants.AUDIOBITDEPTH, PreferenceConstants.AUDIO8BIT);
		Map<String, IRawConverter> rawConverters = BatchActivator.getDefault().getRawConverters();
		for (IRawConverter rc : rawConverters.values()) {
			for (IRawConverter.RawProperty prop : rc.getProperties())
				if (prop.dflt != null)
					defaultNode.put(prop.id, prop.dflt);
			File path = rc.findModule(null);
			if (path != null)
				defaultNode.put(rc.getPathId(), path.getAbsolutePath());
		}
		defaultNode.put(PreferenceConstants.UPDATEPOLICY, PreferenceConstants.UPDATEPOLICY_WITHBACKUP);
		defaultNode.putInt(PreferenceConstants.MOUSE_SPEED, 10);
		defaultNode.putInt(PreferenceConstants.ZOOMKEY,
				Constants.WIN32 ? PreferenceConstants.ZOOMALT : PreferenceConstants.ZOOMSHIFT);
		defaultNode.putInt(PreferenceConstants.WHEELKEY, PreferenceConstants.WHEELSHIFTPANS);
		defaultNode.putInt(PreferenceConstants.WHEELSOFTNESS, 50);
		defaultNode.putInt(PreferenceConstants.MAXIMPORTS, 99);
		defaultNode.putBoolean(PreferenceConstants.ADDNOISE, true);
		defaultNode.putInt(PreferenceConstants.MAXREGIONS, 12);
		defaultNode.putBoolean(PreferenceConstants.FORCEDELETETRASH, true);
		defaultNode.put(PreferenceConstants.AUTORULES,
				new StringBuilder().append(QueryField.STATUS.getId()).append(':').append(Constants.STATE_TODO)
						.append('\n').append(QueryField.IPTC_KEYWORDS.getId()).append(":¬") //$NON-NLS-1$
						.append('\n').append(QueryField.MODIFIED_SINCE.getId()).append(":2;30;365").toString()); //$NON-NLS-1$
		defaultNode.putBoolean(PreferenceConstants.HIDE_MENU_BAR, false);
		defaultNode.putBoolean(PreferenceConstants.HIDE_STATUS_BAR, false);
	}

}
