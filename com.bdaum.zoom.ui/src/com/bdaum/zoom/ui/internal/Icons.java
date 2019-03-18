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

package com.bdaum.zoom.ui.internal;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;

import com.bdaum.zoom.common.internal.FileLocator;
import com.bdaum.zoom.core.Constants;

public class Icons {
	
	private static String iconFolder;

	static {
		try {
			URL url = FileLocator.findFileURL(UiActivator.getDefault().getBundle(), "icons", true); //$NON-NLS-1$
			iconFolder = url == null ? null : url.toString();
		} catch (IOException e) {
			// leave iconFolder null
		}
	}

	public static class Icon {

		private static final String prefix = UiActivator.PLUGIN_ID + ".icn_"; //$NON-NLS-1$
		private static int counter = 0;

		private String key;
		private String path;

		public Icon(String path) {
			this.path = path;
		}

		private String getKey()  {
			if (key == null) {
				ImageDescriptor imageDescriptor;
				if (iconFolder == null)
					imageDescriptor = ImageDescriptor.getMissingImageDescriptor();
				else
					try {
						imageDescriptor = ImageDescriptor.createFromURL(new URL(iconFolder+path));
					} catch (MalformedURLException e) {
						imageDescriptor = ImageDescriptor.getMissingImageDescriptor();
					}
				JFaceResources.getImageRegistry().put(key = prefix + (++counter), imageDescriptor);
				path = null;
			}
			return key;
		}

		public ImageDescriptor getDescriptor() {
			return JFaceResources.getImageRegistry().getDescriptor(getKey());
		}

		public Image getImage() {
			return JFaceResources.getImage(getKey());
		}

		public Image getColorOverlay(int cc) {
			String k = getKey();
			String ck = k + '_' + cc;
			Image image = JFaceResources.getImage(ck);
			if (image != null)
				return image;
			image = JFaceResources.getImage(k);
			int w = image.getBounds().width;
			ImageData imageData = image.getImageData();
			byte[] alphaData = imageData.alphaData;
			if (alphaData != null)
				alphaData[0] = alphaData[1] = alphaData[2] = alphaData[3] = alphaData[w] = alphaData[w
						+ 1] = alphaData[w + 2] = alphaData[w + 3] = alphaData[2 * w] = alphaData[2 * w
								+ 1] = alphaData[2 * w + 2] = alphaData[2 * w + 3] = alphaData[3 * w] = alphaData[3 * w
										+ 1] = alphaData[3 * w + 2] = alphaData[3 * w + 3] = (byte) 255;
			Image newImage = new Image(image.getDevice(), imageData);
			GC gc = new GC(newImage);
			Image patch = toSwtColors(cc);
			Rectangle bounds = patch.getBounds();
			gc.drawImage(patch, 0, 0, bounds.width, bounds.height, 0, 0, 4, 4);
			gc.dispose();
			JFaceResources.getImageRegistry().put(ck, newImage);
			return newImage;
		}
	}

	public static final Icon rotate90 = new Icon("rotate90.png"); //$NON-NLS-1$
	public static final Icon rotate270 = new Icon("rotate270.png"); //$NON-NLS-1$
	public static final Icon rotate90d = new Icon("rotate90d.png"); //$NON-NLS-1$
	public static final Icon rotate270d = new Icon("rotate270d.png"); //$NON-NLS-1$
	public static final Icon rotate90f = new Icon("rotate90f.png"); //$NON-NLS-1$
	public static final Icon rotate90s = new Icon("rotate90s.png"); //$NON-NLS-1$
	public static final Icon rotate270f = new Icon("rotate270f.png"); //$NON-NLS-1$
	public static final Icon rotate270s = new Icon("rotate270s.png"); //$NON-NLS-1$
	public static final Icon rating65 = new Icon("rating65.png"); //$NON-NLS-1$
	public static final Icon rating64 = new Icon("rating64.png"); //$NON-NLS-1$
	public static final Icon rating63 = new Icon("rating63.png"); //$NON-NLS-1$
	public static final Icon rating62 = new Icon("rating62.png"); //$NON-NLS-1$
	public static final Icon rating61 = new Icon("rating61.png"); //$NON-NLS-1$
	public static final Icon rating60 = new Icon("rating60.png"); //$NON-NLS-1$
	public static final Icon rating6u = new Icon("rating6u.png"); //$NON-NLS-1$
	public static final Icon rating6r = new Icon("rating6r.png"); //$NON-NLS-1$
	public static final Icon rating6a = new Icon("rating6a.png"); //$NON-NLS-1$
	public static final Icon ratingAll = new Icon("ratingAll.png"); //$NON-NLS-1$
	public static final Icon rating = new Icon("rating.gif"); //$NON-NLS-1$
	public static final Icon rating0 = new Icon("rating_0.png"); //$NON-NLS-1$
	public static final Icon rating1 = new Icon("rating_1.png"); //$NON-NLS-1$
	public static final Icon rating2 = new Icon("rating_2.png"); //$NON-NLS-1$
	public static final Icon rating3 = new Icon("rating_3.png"); //$NON-NLS-1$
	public static final Icon rating4 = new Icon("rating_4.png"); //$NON-NLS-1$
	public static final Icon rating5 = new Icon("rating_5.png"); //$NON-NLS-1$
	public static final Icon rating_undef = new Icon("rating_undef.png"); //$NON-NLS-1$
	public static final Icon black = new Icon("patches/black.png"); //$NON-NLS-1$
	public static final Icon white = new Icon("patches/white.png"); //$NON-NLS-1$
	public static final Icon bw = new Icon("bw.gif"); //$NON-NLS-1$
	public static final Icon red = new Icon("patches/red.png"); //$NON-NLS-1$
	public static final Icon green = new Icon("patches/green.png"); //$NON-NLS-1$
	public static final Icon blue = new Icon("patches/blue.png"); //$NON-NLS-1$
	public static final Icon redDot = new Icon("redDot.png"); //$NON-NLS-1$
	public static final Icon paleRedDot = new Icon("paleRedDot.png"); //$NON-NLS-1$
	public static final Icon greenDot = new Icon("greenDot.png"); //$NON-NLS-1$
	public static final Icon paleGreenDot = new Icon("paleGreenDot.png"); //$NON-NLS-1$
	public static final Icon blueDot = new Icon("blueDot.png"); //$NON-NLS-1$
	public static final Icon paleBlueDot = new Icon("paleBlueDot.png"); //$NON-NLS-1$
	public static final Icon grayscale = new Icon("grayscale.png"); //$NON-NLS-1$
	public static final Icon paleGrayscale = new Icon("paleGrayscale.png"); //$NON-NLS-1$
	public static final Icon cyan = new Icon("patches/cyan.png"); //$NON-NLS-1$
	public static final Icon magenta = new Icon("patches/magenta.png"); //$NON-NLS-1$
	public static final Icon yellow = new Icon("patches/yellow.png"); //$NON-NLS-1$
	public static final Icon orange = new Icon("patches/orange.png"); //$NON-NLS-1$
	public static final Icon pink = new Icon("patches/pink.png"); //$NON-NLS-1$
	public static final Icon violet = new Icon("patches/violet.png"); //$NON-NLS-1$
	public static final Icon dashed = new Icon("patches/dashed.png"); //$NON-NLS-1$
	public static final Icon tri = new Icon("patches/tri.png"); //$NON-NLS-1$
	public static final Icon folder_star = new Icon("folder_star.png"); //$NON-NLS-1$
	public static final Icon folder_user = new Icon("folder_user.png"); //$NON-NLS-1$
	public static final Icon folder_table = new Icon("folder_table.png"); //$NON-NLS-1$
	public static final Icon folder_page = new Icon("folder_page.png"); //$NON-NLS-1$
	public static final Icon folder_page_white = new Icon("folder_page_white.png"); //$NON-NLS-1$
	public static final Icon folder_explore = new Icon("folder_explore.png"); //$NON-NLS-1$
	public static final Icon folder_edit = new Icon("folder_edit.png"); //$NON-NLS-1$
	public static final Icon folder_add = new Icon("folder_add.png"); //$NON-NLS-1$
	public static final Icon folder_delete = new Icon("folder_delete.png"); //$NON-NLS-1$
	public static final Icon folder_find = new Icon("folder_find.png"); //$NON-NLS-1$
	public static final Icon timeline_find = new Icon("timeline_find.png"); //$NON-NLS-1$
	public static final Icon map_find = new Icon("map_find.png"); //$NON-NLS-1$
	public static final Icon folder_image = new Icon("folder_image.png"); //$NON-NLS-1$
	public static final Icon folder_database = new Icon("folder_database.png"); //$NON-NLS-1$
	public static final Icon folder_import = new Icon("folder_key.png"); //$NON-NLS-1$
	public static final Icon folder_tethered = new Icon("folder_tethered.png"); //$NON-NLS-1$
	public static final Icon folder_clock = new Icon("folder_clock.png"); //$NON-NLS-1$
	public static final Icon folder_and = new Icon("folder_and.png"); //$NON-NLS-1$
	public static final Icon folder_add_network = new Icon("folder_add_network.png"); //$NON-NLS-1$
	public static final Icon folder_and_network = new Icon("folder_and_network.png"); //$NON-NLS-1$
	public static final Icon folder_album = new Icon("folder_heart.png"); //$NON-NLS-1$
	public static final Icon folder_person = new Icon("folder_person.png"); //$NON-NLS-1$
	public static final Icon folder_world = new Icon("folder_world.png"); //$NON-NLS-1$

	public static final Icon folder = new Icon("folder.png"); //$NON-NLS-1$
	public static final Icon folder64 = new Icon("banner/folder64.png"); //$NON-NLS-1$
	public static final Icon merge64 = new Icon("banner/merge64.png"); //$NON-NLS-1$
	public static final Icon person64 = new Icon("banner/person64.png"); //$NON-NLS-1$

	public static final Icon forwards = new Icon("forward_nav.png"); //$NON-NLS-1$
	public static final Icon backwards = new Icon("backward_nav.png"); //$NON-NLS-1$
	public static final Icon down = new Icon("down.png"); //$NON-NLS-1$
	public static final Icon up = new Icon("up.png"); //$NON-NLS-1$
	public static final Icon lastImport = new Icon("lastimport.png"); //$NON-NLS-1$
	public static final Icon group = new Icon("group.png"); //$NON-NLS-1$
	public static final Icon groupfiltered = new Icon("group_filtered.png"); //$NON-NLS-1$
	public static final Icon slideshow = new Icon("slideshow.png"); //$NON-NLS-1$
	public static final Icon exhibition = new Icon("exhibition.png"); //$NON-NLS-1$
	public static final Icon webGallery = new Icon("webGallery.png"); //$NON-NLS-1$
	public static final Icon shadow = new Icon("black.png"); //$NON-NLS-1$
	public static final Icon signed_yes = new Icon("signed_yes.gif"); //$NON-NLS-1$
	public static final Icon signed_no = new Icon("signed_no.gif"); //$NON-NLS-1$

	public static final Icon speaker = new Icon("speaker.png"); //$NON-NLS-1$
	public static final Icon record = new Icon("record.gif"); //$NON-NLS-1$
	public static final Icon stop = new Icon("stop.gif"); //$NON-NLS-1$
	public static final Icon replay = new Icon("replay.gif"); //$NON-NLS-1$
	public static final Icon folder32 = new Icon("folder32.gif"); //$NON-NLS-1$
	public static final Icon largeProperties = new Icon("banner/icon_document.png"); //$NON-NLS-1$
	public static final Icon smallProperties = new Icon("banner/cell_properties.png"); //$NON-NLS-1$
	public static final Icon largeDelete = new Icon("banner/delete_large.png"); //$NON-NLS-1$
	public static final Icon error = new Icon("error.png"); //$NON-NLS-1$
	public static final Icon square = new Icon("shape_square.png"); //$NON-NLS-1$
	public static final Icon nullTitle = new Icon("banner/nullTitleIcon.gif"); //$NON-NLS-1$
	public static final Icon emptyTitle = new Icon("banner/emptyTitleIcon.gif"); //$NON-NLS-1$
	public static final Icon delete = new Icon("delete_obj.png"); //$NON-NLS-1$
	public static final Icon delete32 = new Icon("delete32.png"); //$NON-NLS-1$
	public static final Icon trashrestore = new Icon("trashrestore.png"); //$NON-NLS-1$
	public static final Icon trashrestoreSmall = new Icon("restoretrash_s.png"); //$NON-NLS-1$
	public static final Icon trash = new Icon("trashcan.png"); //$NON-NLS-1$
	public static final Icon trash16 = new Icon("trashcan16.png"); //$NON-NLS-1$
	public static final Icon cleartrash = new Icon("clearTrash.png"); //$NON-NLS-1$
	public static final Icon file = new Icon("file_obj.png"); //$NON-NLS-1$
	public static final Icon filter = new Icon("filter.png"); //$NON-NLS-1$
	public static final Icon selectAll = new Icon("selectall.gif"); //$NON-NLS-1$
	public static final Icon derivative = new Icon("derivative.png"); //$NON-NLS-1$
	public static final Icon original = new Icon("original.png"); //$NON-NLS-1$
	public static final Icon composite = new Icon("composite.png"); //$NON-NLS-1$
	public static final Icon component = new Icon("component.png"); //$NON-NLS-1$
	public static final Icon tableSave = new Icon("table_save.png"); //$NON-NLS-1$
	public static final Icon textsearchOne = new Icon("textsearch_one.png"); //$NON-NLS-1$
	public static final Icon textsearchMany = new Icon("textsearch_many.png"); //$NON-NLS-1$
	public static final Icon textSearch = new Icon("textsearch.png"); //$NON-NLS-1$
	public static final Icon format = new Icon("format.gif"); //$NON-NLS-1$
	public static final Icon formatSelect = new Icon("format_select.gif"); //$NON-NLS-1$
	public static final Icon query = new Icon("query.gif"); //$NON-NLS-1$
	public static final Icon map = new Icon("gps.gif"); //$NON-NLS-1$
	public static final Icon www = new Icon("www.gif"); //$NON-NLS-1$
	public static final Icon email = new Icon("email.gif"); //$NON-NLS-1$
	public static final Icon newGroup = new Icon("newgroup.gif"); //$NON-NLS-1$
	public static final Icon newSlideshow = new Icon("newslideshow.gif"); //$NON-NLS-1$
	public static final Icon addAlbum = new Icon("add_album.png"); //$NON-NLS-1$
	public static final Icon albumRemove = new Icon("heart_delete.png"); //$NON-NLS-1$
	public static final Icon addSubselection = new Icon("add_subselection.png"); //$NON-NLS-1$
	public static final Icon cut = new Icon("cut_edit.png"); //$NON-NLS-1$
	public static final Icon copy = new Icon("copy_edit.png"); //$NON-NLS-1$
	public static final Icon paste = new Icon("paste_edit.png"); //$NON-NLS-1$
	public static final Icon collapseAll = new Icon("collapseall.png"); //$NON-NLS-1$
	public static final Icon expandAll = new Icon("expandall.png"); //$NON-NLS-1$
	public static final Icon properties = new Icon("props_orange.png"); //$NON-NLS-1$
	public static final Icon properties_blue = new Icon("props_blue.png"); //$NON-NLS-1$
	public static final Icon play = new Icon("play.gif"); //$NON-NLS-1$
	public static final Icon toggle = new Icon("toggle.png"); //$NON-NLS-1$
	public static final Icon unlink = new Icon("unlink.gif"); //$NON-NLS-1$
	public static final Icon descriptionEdit = new Icon("page_white_edit.png"); //$NON-NLS-1$
	public static final Icon proximity = new Icon("proximity.gif"); //$NON-NLS-1$
	public static final Icon image_edit = new Icon("image_edit.png"); //$NON-NLS-1$
	public static final Icon image_edit_with = new Icon("image_edit_with.png"); //$NON-NLS-1$
	public static final Icon colorCode = new Icon("colorCode.gif"); //$NON-NLS-1$
	public static final Icon sound = new Icon("sound.png"); //$NON-NLS-1$
	public static final Icon sound_add = new Icon("sound_add.png"); //$NON-NLS-1$
	public static final Icon sound_delete = new Icon("sound_delete.png"); //$NON-NLS-1$
	public static final Icon similar = new Icon("similar.gif"); //$NON-NLS-1$
	public static final Icon time = new Icon("time.png"); //$NON-NLS-1$
	public static final Icon timeShift = new Icon("timeShift.gif"); //$NON-NLS-1$
	public static final Icon alphab_sort = new Icon("alphab_sort.gif"); //$NON-NLS-1$
	public static final Icon add = new Icon("add_obj.png"); //$NON-NLS-1$
	public static final Icon save = new Icon("table_save.png"); //$NON-NLS-1$
	public static final Icon sync = new Icon("sync.png"); //$NON-NLS-1$
	public static final Icon slideControl_dark = new Icon("banner/slideControl_d.png"); //$NON-NLS-1$
	public static final Icon slideControl_nonselect = new Icon("banner/slideControl_n.png"); //$NON-NLS-1$
	public static final Icon slideControl = new Icon("banner/slideControl.png"); //$NON-NLS-1$
	public static final Icon importNewStructure = new Icon("banner/imp_new_structure.png"); //$NON-NLS-1$
	public static final Icon importDevice = new Icon("banner/import_dev.png"); //$NON-NLS-1$
	public static final Icon pref64 = new Icon("banner/exportpref_wiz.png"); //$NON-NLS-1$
	public static final Icon watchedFolder = new Icon("banner/watchedFolder.png"); //$NON-NLS-1$
	public static final Icon splitcat = new Icon("book_go.png"); //$NON-NLS-1$
	public static final Icon stack = new Icon("stack.png"); //$NON-NLS-1$
	public static final Icon hover = new Icon("comments.png"); //$NON-NLS-1$
	public static final Icon nolocation = new Icon("nolocation.png"); //$NON-NLS-1$
	public static final Icon location = new Icon("location.png"); //$NON-NLS-1$
	public static final Icon categorize = new Icon("categorize.png"); //$NON-NLS-1$
	public static final Icon keydef = new Icon("keydef.gif"); //$NON-NLS-1$
	public static final Icon cluster = new Icon("cluster.png"); //$NON-NLS-1$
	public static final Icon cue = new Icon("cue.png"); //$NON-NLS-1$
	public static final Icon transparentCover = new Icon("cover.png"); //$NON-NLS-1$
	public static final Icon expand = new Icon("expand.png"); //$NON-NLS-1$
	public static final Icon collaps = new Icon("collaps.png"); //$NON-NLS-1$
	public static final Icon collapsed = new Icon("collapsed.png"); //$NON-NLS-1$
	public static final Icon gotoBookmark = new Icon("goto_bookmark.png"); //$NON-NLS-1$
	public static final Icon addBookmark = new Icon("add_bookmark.png"); //$NON-NLS-1$
	public static final Icon bookmarks = new Icon("bookmarks.png"); //$NON-NLS-1$
	public static final Icon meta64 = new Icon("banner/xmp64.png"); //$NON-NLS-1$
	public static final Icon tether64 = new Icon("banner/tether64.png"); //$NON-NLS-1$

	public static final Icon add_obj = new Icon("add_obj.png"); //$NON-NLS-1$
	public static final Icon copyMetadata = new Icon("copy_edit.png"); //$NON-NLS-1$
	public static final Icon pasteMetadata = new Icon("paste_edit.png"); //$NON-NLS-1$
	public static final Icon refresh = new Icon("refresh.gif"); //$NON-NLS-1$
	public static final Icon reset = new Icon("reset.gif"); //$NON-NLS-1$
	public static final Icon done = new Icon("done.png"); //$NON-NLS-1$
	public static final Icon appIcon = new Icon("zora32.gif"); //$NON-NLS-1$
	public static final Icon appIcon24 = new Icon("zora24.gif"); //$NON-NLS-1$
	public static final Icon appIconShutDown = new Icon("shutdown.png"); //$NON-NLS-1$
	public static final Icon appStart = new Icon("appstart.png"); //$NON-NLS-1$
	public static final Icon catFilter = new Icon("catfilter.png"); //$NON-NLS-1$
	public static final Icon zoraShell = new Icon("zoraShell.gif"); //$NON-NLS-1$
	public static final Icon spring = new Icon("spring.png"); //$NON-NLS-1$
	public static final Icon summer = new Icon("summer.png"); //$NON-NLS-1$
	public static final Icon autumn = new Icon("autumn.png"); //$NON-NLS-1$
	public static final Icon winter = new Icon("winter.png"); //$NON-NLS-1$
	public static final Icon seasons = new Icon("4seasons.png"); //$NON-NLS-1$
	public static final Icon network = new Icon("network.png"); //$NON-NLS-1$
	public static final Icon local = new Icon("local.png"); //$NON-NLS-1$
	public static final Icon closeButton = new Icon("close_button_red.png"); //$NON-NLS-1$
	public static final Icon MIXED_OVERLAY = new Icon("mixedOverlay.png"); //$NON-NLS-1$
	public static final Icon PENDING_OVERLAY = new Icon("pendingOverlay.png"); //$NON-NLS-1$
	public static final Icon user_magnify = new Icon("user_magnify.png"); //$NON-NLS-1$
	public static final Icon goggles = new Icon("goggles.gif"); //$NON-NLS-1$
	public static final Icon textsearch = new Icon("textsearch.png"); //$NON-NLS-1$
	public static final Icon today = new Icon("today.png"); //$NON-NLS-1$
	public static final Icon yesterday = new Icon("yesterday.png"); //$NON-NLS-1$
	public static final Icon week = new Icon("week.png"); //$NON-NLS-1$
	public static final Icon history = new Icon("historyW.png"); //$NON-NLS-1$
	public static final Icon rename = new Icon("rename.png"); //$NON-NLS-1$
	public static final Icon move = new Icon("lorry_flatbed.png"); //$NON-NLS-1$
	public static final Icon warning = new Icon("warn_tsk.gif"); //$NON-NLS-1$
	public static final Icon info = new Icon("info_tsk.gif"); //$NON-NLS-1$
	public static final Icon note = new Icon("note.png"); //$NON-NLS-1$
	public static final Icon note32 = new Icon("note32.png"); //$NON-NLS-1$
	public static final Icon cancel32 = new Icon("cancel32.png"); //$NON-NLS-1$
	public static final Icon neural = new Icon("neural.png"); //$NON-NLS-1$
	public static final Icon leave = new Icon("leave.png"); //$NON-NLS-1$
	public static final Icon centric = new Icon("centric.png"); //$NON-NLS-1$
	public static final Icon integral = new Icon("integral.png"); //$NON-NLS-1$
	public static final Icon wastebasket = new Icon("trash.png"); //$NON-NLS-1$
	public static final Icon selectNone = new Icon("selectnone.png"); //$NON-NLS-1$
	public static final Icon orphan = new Icon("orphan.gif"); //$NON-NLS-1$
	public static final Icon revert = new Icon("revert.png"); //$NON-NLS-1$
	public static final Icon speakerPlus = new Icon("speakerPlus.png"); //$NON-NLS-1$
	public static final Icon notePlus = new Icon("notePlus.png"); //$NON-NLS-1$
	public static final Icon drawing = new Icon("drawing.png"); //$NON-NLS-1$
	public static final Icon images = new Icon("images.png"); //$NON-NLS-1$
	public static final Icon month = new Icon("month.png"); //$NON-NLS-1$
	public static final Icon sync32 = new Icon("sync32.gif"); //$NON-NLS-1$
	public static final Icon sync32d = new Icon("sync32d.gif"); //$NON-NLS-1$

	public static Icon toColorIcon(int code) {
		switch (code) {
		case Constants.COLOR_BLACK:
			return black;
		case Constants.COLOR_WHITE:
			return white;
		case Constants.COLOR_RED:
			return red;
		case Constants.COLOR_GREEN:
			return green;
		case Constants.COLOR_BLUE:
			return blue;
		case Constants.COLOR_CYAN:
			return cyan;
		case Constants.COLOR_MAGENTA:
			return magenta;
		case Constants.COLOR_YELLOW:
			return yellow;
		case Constants.COLOR_ORANGE:
			return orange;
		case Constants.COLOR_PINK:
			return pink;
		case Constants.COLOR_VIOLET:
			return violet;
		default:
			return dashed;
		}
	}

	public static Image toSwtColors(int code) {
		return toColorIcon(code).getImage();
	}

}
