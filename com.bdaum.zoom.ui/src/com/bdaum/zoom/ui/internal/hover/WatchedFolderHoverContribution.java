package com.bdaum.zoom.ui.internal.hover;

import java.io.File;
import java.net.URI;

import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.cat.model.meta.WatchedFolder;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.core.QueryField;

public class WatchedFolderHoverContribution extends AbstractHoverContribution implements IHoverItem {

	private static final String[] TITLETAGS = new String[] { Constants.HV_LOCATION, Constants.HV_TRANSFER };
	private static final String[] TITLELABELS = new String[] { Messages.WatchedFolderHoverContribution_location,
			Messages.WatchedFolderHoverContribution_type };

	private static final String[] TAGS = new String[] { Constants.HV_LASTOBSERVATION, Constants.HV_RECURSIVE,
			Constants.HV_FILTERS, Constants.HV_ARTIST, Constants.HV_SKIPDUPLICATES, Constants.HV_TARGETDIR,
			Constants.HV_SUBFOLDERPOLICY, Constants.HV_TEMPLATE, Constants.HV_FILESOURCE, Constants.HV_CONTENT };

	private static final String[] LABELS = new String[] { Messages.WatchedFolderHoverContribution_observation,
			Messages.WatchedFolderHoverContribution_subfolders, Messages.WatchedFolderHoverContribution_filters,
			Messages.WatchedFolderHoverContribution_artist, Messages.WatchedFolderHoverContribution_skip,
			Messages.WatchedFolderHoverContribution_target, Messages.WatchedFolderHoverContribution_create_sub,
			Messages.WatchedFolderHoverContribution_rename, Messages.WatchedFolderHoverContribution_source,
			Messages.WatchedFolderHoverContribution_content };
	
	private boolean hasLocation = false;
	
	@Override
	public void init() {
		hasLocation = false;
	}

	@Override
	public boolean supportsTitle() {
		return true;
	}

	@Override
	public String getCategory() {
		return Messages.WatchedFolderHoverContribution_misc;
	}

	@Override
	public String getName() {
		return Messages.WatchedFolderHoverContribution_watched_folder;
	}

	@Override
	public String getDescription() {
		return Messages.WatchedFolderHoverContribution_hover_info;
	}

	@Override
	public String getDefaultTemplate() {
		return Messages.WatcheFolderHoverContribution_template;
	}

	@Override
	public String getDefaultTitleTemplate() {
		return Messages.WatchedFolderHoverContribution_0;
	}

	@Override
	public String[] getItemKeys() {
		return TAGS;
	}

	@Override
	public IHoverItem getHoverItem(String itemkey) {
		return this;
	}

	@Override
	public String[] getTitleItemLabels() {
		return TITLELABELS;
	}

	@Override
	public String[] getTitleItemKeys() {
		return TITLETAGS;
	}

	@Override
	public String[] getItemLabels() {
		return LABELS;
	}

	@Override
	public String getValue(String key, Object object, IHoverContext context) {
		if (object instanceof HoverTestObject) {
			if (Constants.HV_LOCATION.equals(key))
				return Constants.WIN32 ? "C:\\Users\\me\\Pictures\\downloads" : "/home/me/pictures/downloads"; //$NON-NLS-1$ //$NON-NLS-2$
			if (Constants.HV_LASTOBSERVATION.equals(key))
				return Format.MDY_TIME_LONG_FORMAT.get().format(System.currentTimeMillis());
			if (Constants.HV_RECURSIVE.equals(key))
				return Messages.WatchedFolderHoverContribution_yes;
			if (Constants.HV_FILTERS.equals(key))
				return Messages.WatchedFolderHoverContribution_skip_none;
			if (Constants.HV_TRANSFER.equals(key))
				return Messages.WatchedFolderHoverContribution_transfer;
			if (Constants.HV_ARTIST.equals(key))
				return Messages.WatchedFolderHoverContribution_me;
			if (Constants.HV_SKIPDUPLICATES.equals(key))
				return Messages.WatchedFolderHoverContribution_yes;
			if (Constants.HV_TARGETDIR.equals(key))
				return Constants.WIN32 ? "C:\\Users\\me\\Pictures\\originals" : "/home/me/pictures/originals"; //$NON-NLS-1$ //$NON-NLS-2$
			if (Constants.HV_SUBFOLDERPOLICY.equals(key))
				return Messages.WatchedFolderHoverContribution_yearmonth;
			if (Constants.HV_TEMPLATE.equals(key))
				return "{user}{yyyy}-{sequenceNo5}"; //$NON-NLS-1$
			if (Constants.HV_FILESOURCE.equals(key))
				return QueryField.EXIF_FILESOURCE.getEnumLabels()[3];
			if (Constants.HV_CONTENT.equals(key))
				return Messages.WatchedFolderHoverContribution_test_content;
		} else if (object instanceof WatchedFolder) {
			WatchedFolder wf = (WatchedFolder) object;
			if (Constants.HV_LOCATION.equals(key)) {
				hasLocation = true;
				URI uri = Core.getCore().getVolumeManager().findFile(wf.getUri(), wf.getVolume());
				if (uri != null) {
					File file = new File(uri);
					String path = file.getPath();
					return file.exists() ? path : path + Messages.WatchedFolderHoverContribution_offline;
				}
				return Messages.WatchedFolderHoverContribution_invalid;
			} else if (Constants.HV_LASTOBSERVATION.equals(key)) {
				long lastObservation = wf.getLastObservation();
				if (lastObservation > 0)
					return Format.MDY_TIME_LONG_FORMAT.get().format(lastObservation);
			} else if (Constants.HV_RECURSIVE.equals(key))
				return yesNo(wf.getRecursive());
			else if (Constants.HV_FILTERS.equals(key)) {
				if (wf.getTransfer()) {
					switch (wf.getSkipPolicy()) {
					case 1:
						return Messages.WatchedFolderHoverContribution_skip_raw;
					case 2:
						return Messages.WatchedFolderHoverContribution_skip_raw_if;
					case 3:
						return Messages.WatchedFolderHoverContribution_skip_jpg;
					case 4:
						return Messages.WatchedFolderHoverContribution_skip_jpg_if;
					default:
						return Messages.WatchedFolderHoverContribution_skip_none;
					}
				}
				return wf.getFilters();
			} else if (Constants.HV_TRANSFER.equals(key)) {
				boolean transfer = wf.getTransfer();
				if (transfer) {
					if (wf.getTethered())
						return Messages.WatchedFolderHoverContribution_tethered_transfer;
					return Messages.WatchedFolderHoverContribution_transfer;
				}
				return Messages.WatchedFolderHoverContribution_storage;
			} else if (Constants.HV_ARTIST.equals(key)) {
				if (wf.getTransfer())
					return wf.getArtist();
			} else if (Constants.HV_SKIPDUPLICATES.equals(key)) {
				if (wf.getTransfer())
					return yesNo(wf.getSkipDuplicates());
			} else if (Constants.HV_TARGETDIR.equals(key)) {
				if (wf.getTransfer())
					return wf.getTargetDir();
			} else if (Constants.HV_SUBFOLDERPOLICY.equals(key)) {
				if (wf.getTransfer()) {
					int subfolderPolicy = wf.getSubfolderPolicy();
					switch (subfolderPolicy) {
					case 0:
						return Messages.WatchedFolderHoverContribution_no;
					case 1:
						return Messages.WatchedFolderHoverContribution_year;
					case 2:
						return Messages.WatchedFolderHoverContribution_yearmonth;
					case 3:
						return Messages.WatchedFolderHoverContribution_yearmonthday;
					}
				}
			} else if (Constants.HV_TEMPLATE.equals(key)) {
				if (wf.getTransfer()) {
					String template = wf.getSelectedTemplate();
					if (template != null && !template.isEmpty()) {
						String cue = wf.getCue();
						if (cue != null && !cue.isEmpty())
							return NLS.bind(Messages.WatchedFolderHoverContribution_cue, template, cue);
						return template;
					}
				}
			} else if (Constants.HV_FILESOURCE.equals(key)) {
				if (wf.getTransfer()) {
					int fileSource = wf.getFileSource();
					if (fileSource > 0) {
						int[] enumeration = (int[]) QueryField.EXIF_FILESOURCE.getEnumeration();
						String[] enumLabels = QueryField.EXIF_FILESOURCE.getEnumLabels();
						for (int i = 0; i < enumeration.length; i++)
							if (enumeration[i] == fileSource)
								return enumLabels[i];
					}
				}
			} else if (Constants.HV_CONTENT.equals(key)) {
				URI uri = Core.getCore().getVolumeManager().findFile(wf.getUri(), wf.getVolume());
				if (uri != null) {
					File file = new File(uri);
					if (file.exists()) {
						File[] files = file.listFiles();
						if (files != null && files.length > 0) {
							int d = 0;
							int f = 0;
							for (File fil : files) {
								if (fil.isDirectory())
									++d;
								else
									++f;
							}
							String dd;
							String ff;
							switch (d) {
							case 0:
								dd = null;
								break;
							case 1:
								dd = Messages.WatchedFolderHoverContribution_one_dir;
								break;
							default:
								dd = NLS.bind(Messages.WatchedFolderHoverContribution_n_dirs, d);
							}
							switch (f) {
							case 0:
								ff = null;
								break;
							case 1:
								ff = Messages.WatchedFolderHoverContribution_one_file;
								break;
							default:
								ff = NLS.bind(Messages.WatchedFolderHoverContribution_n_files, f);
							}
							if (dd == null)
								return ff;
							if (ff == null)
								return dd;
							return NLS.bind(Messages.WatchedFolderHoverContribution_and, dd, ff);
						}
						return Messages.WatchedFolderHoverContribution_empty;
					} else if (!hasLocation)
						return Messages.WatchedFolderHoverContribution_offline;
				}
			}
		}
		return null;
	}

	private static String yesNo(boolean b) {
		return b ? Messages.WatchedFolderHoverContribution_yes : Messages.WatchedFolderHoverContribution_no;
	}

	@Override
	public Object getTarget(Object object) {
		return object;
	}

}
