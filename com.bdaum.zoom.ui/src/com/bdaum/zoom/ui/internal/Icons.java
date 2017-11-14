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
 * (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;

import com.bdaum.zoom.core.Constants;

public class Icons {

	public static class Icon {

		private static final String prefix = UiActivator.PLUGIN_ID + ".icn_"; //$NON-NLS-1$
		private static int counter = 0;

		private String key;
		private String path;

		public Icon(String path) {
			this.path = path;
		}

		private String getKey() {
			if (key == null) {
				key = prefix + (++counter);
				JFaceResources.getImageRegistry().put(key,
						UiActivator.getImageDescriptor(path));
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
				alphaData[0] = alphaData[1] = alphaData[2] = alphaData[3] = alphaData[w] = alphaData[w + 1] = alphaData[w + 2] = alphaData[w + 3] = alphaData[2 * w] = alphaData[2 * w + 1] = alphaData[2 * w + 2] = alphaData[2 * w + 3] = alphaData[3 * w] = alphaData[3 * w + 1] = alphaData[3 * w + 2] = alphaData[3 * w + 3] = (byte) 255;
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

	public static final Icon rotate90 = new Icon("icons/rotate90.png"); //$NON-NLS-1$
	public static final Icon rotate270 = new Icon("icons/rotate270.png"); //$NON-NLS-1$
	public static final Icon rotate90d = new Icon("icons/rotate90d.png"); //$NON-NLS-1$
	public static final Icon rotate270d = new Icon("icons/rotate270d.png"); //$NON-NLS-1$
	public static final Icon rotate90f = new Icon("icons/rotate90f.png"); //$NON-NLS-1$
	public static final Icon rotate90s = new Icon("icons/rotate90s.png"); //$NON-NLS-1$
	public static final Icon rotate270f = new Icon("icons/rotate270f.png"); //$NON-NLS-1$
	public static final Icon rotate270s = new Icon("icons/rotate270s.png"); //$NON-NLS-1$
	public static final Icon rating65 = new Icon("icons/rating65.png"); //$NON-NLS-1$
	public static final Icon rating64 = new Icon("icons/rating64.png"); //$NON-NLS-1$
	public static final Icon rating63 = new Icon("icons/rating63.png"); //$NON-NLS-1$
	public static final Icon rating62 = new Icon("icons/rating62.png"); //$NON-NLS-1$
	public static final Icon rating61 = new Icon("icons/rating61.png"); //$NON-NLS-1$
	public static final Icon rating60 = new Icon("icons/rating60.png"); //$NON-NLS-1$
	public static final Icon ratingAll = new Icon("icons/ratingAll.png"); //$NON-NLS-1$
	public static final Icon ratingAllSmall = new Icon(
			"icons/ratingAllSmall.png"); //$NON-NLS-1$
	public static final Icon rating = new Icon("icons/rating.gif"); //$NON-NLS-1$
	public static final Icon rating1 = new Icon("icons/rating_1.png"); //$NON-NLS-1$
	public static final Icon rating2 = new Icon("icons/rating_2.png"); //$NON-NLS-1$
	public static final Icon rating3 = new Icon("icons/rating_3.png"); //$NON-NLS-1$
	public static final Icon rating4 = new Icon("icons/rating_4.png"); //$NON-NLS-1$
	public static final Icon rating5 = new Icon("icons/rating_5.png"); //$NON-NLS-1$
	public static final Icon rating_undef = new Icon("icons/rating_undef.png"); //$NON-NLS-1$
	public static final Icon black = new Icon("icons/patches/black.png"); //$NON-NLS-1$
	public static final Icon white = new Icon("icons/patches/white.png"); //$NON-NLS-1$
	public static final Icon bw = new Icon("icons/bw.gif"); //$NON-NLS-1$
	public static final Icon red = new Icon("icons/patches/red.png"); //$NON-NLS-1$
	public static final Icon green = new Icon("icons/patches/green.png"); //$NON-NLS-1$
	public static final Icon blue = new Icon("icons/patches/blue.png"); //$NON-NLS-1$
	public static final Icon redDot = new Icon("icons/redDot.png"); //$NON-NLS-1$
	public static final Icon greenDot = new Icon("icons/greenDot.png"); //$NON-NLS-1$
	public static final Icon blueDot = new Icon("icons/blueDot.png"); //$NON-NLS-1$
	public static final Icon grayscale = new Icon("icons/grayscale.png"); //$NON-NLS-1$
	public static final Icon cyan = new Icon("icons/patches/cyan.png"); //$NON-NLS-1$
	public static final Icon magenta = new Icon("icons/patches/magenta.png"); //$NON-NLS-1$
	public static final Icon yellow = new Icon("icons/patches/yellow.png"); //$NON-NLS-1$
	public static final Icon orange = new Icon("icons/patches/orange.png"); //$NON-NLS-1$
	public static final Icon pink = new Icon("icons/patches/pink.png"); //$NON-NLS-1$
	public static final Icon violet = new Icon("icons/patches/violet.png"); //$NON-NLS-1$
	public static final Icon dashed = new Icon("icons/patches/dashed.png"); //$NON-NLS-1$
	public static final Icon tri = new Icon("icons/patches/tri.png"); //$NON-NLS-1$
	public static final Icon folder_star = new Icon("icons/folder_star.png"); //$NON-NLS-1$
	public static final Icon folder_user = new Icon("icons/folder_user.png"); //$NON-NLS-1$
	public static final Icon folder_table = new Icon("icons/folder_table.png"); //$NON-NLS-1$
	public static final Icon folder_page = new Icon("icons/folder_page.png"); //$NON-NLS-1$
	public static final Icon folder_page_white = new Icon(
			"icons/folder_page_white.png"); //$NON-NLS-1$
	public static final Icon folder_explore = new Icon(
			"icons/folder_explore.png"); //$NON-NLS-1$
	public static final Icon folder_edit = new Icon("icons/folder_edit.png"); //$NON-NLS-1$
	public static final Icon folder_add = new Icon("icons/folder_add.png"); //$NON-NLS-1$
	public static final Icon folder_delete = new Icon("icons/folder_delete.png"); //$NON-NLS-1$
	public static final Icon folder_find = new Icon("icons/folder_find.png"); //$NON-NLS-1$
	public static final Icon timeline_find = new Icon("icons/timeline_find.png"); //$NON-NLS-1$
	public static final Icon map_find = new Icon("icons/map_find.png"); //$NON-NLS-1$
	public static final Icon folder_image = new Icon("icons/folder_image.png"); //$NON-NLS-1$
	public static final Icon folder_database = new Icon(
			"icons/folder_database.png"); //$NON-NLS-1$
	public static final Icon folder_import = new Icon("icons/folder_key.png"); //$NON-NLS-1$
	public static final Icon folder_clock = new Icon("icons/folder_clock.png"); //$NON-NLS-1$
	public static final Icon folder_and = new Icon("icons/folder_and.png"); //$NON-NLS-1$
	public static final Icon folder_add_network = new Icon(
			"icons/folder_add_network.png"); //$NON-NLS-1$
	public static final Icon folder_and_network = new Icon(
			"icons/folder_and_network.png"); //$NON-NLS-1$
	public static final Icon folder_album = new Icon("icons/folder_heart.png"); //$NON-NLS-1$
	public static final Icon folder_person = new Icon("icons/folder_person.png"); //$NON-NLS-1$
	public static final Icon folder_world = new Icon("icons/folder_world.png"); //$NON-NLS-1$

	public static final Icon folder = new Icon("icons/folder.png"); //$NON-NLS-1$
	public static final Icon folder64 = new Icon("icons/banner/folder64.png"); //$NON-NLS-1$
	public static final Icon merge64 = new Icon("icons/banner/merge64.png"); //$NON-NLS-1$
	public static final Icon person64 = new Icon("icons/banner/person64.png"); //$NON-NLS-1$


	public static final Icon forwards = new Icon("icons/forward_nav.png"); //$NON-NLS-1$
	public static final Icon backwards = new Icon("icons/backward_nav.png"); //$NON-NLS-1$
	public static final Icon lastImport = new Icon("icons/lastimport.png"); //$NON-NLS-1$
	public static final Icon group = new Icon("icons/group.png"); //$NON-NLS-1$
	public static final Icon groupfiltered = new Icon("icons/group_filtered.png"); //$NON-NLS-1$
	public static final Icon slideshow = new Icon("icons/slideshow.gif"); //$NON-NLS-1$
	public static final Icon exhibition = new Icon("icons/exhibition.png"); //$NON-NLS-1$
	public static final Icon webGallery = new Icon("icons/webGallery.gif"); //$NON-NLS-1$
	public static final Icon shadow = new Icon("/icons/black.png"); //$NON-NLS-1$
	public static final Icon signed_yes = new Icon("/icons/signed_yes.gif"); //$NON-NLS-1$
	public static final Icon signed_no = new Icon("/icons/signed_no.gif"); //$NON-NLS-1$

	public static final Icon speaker = new Icon("/icons/speaker.png"); //$NON-NLS-1$
	public static final Icon record = new Icon("/icons/start32.gif"); //$NON-NLS-1$
	public static final Icon stop = new Icon("/icons/stop32.gif"); //$NON-NLS-1$
	public static final Icon folder32 = new Icon("/icons/folder32.gif"); //$NON-NLS-1$
	public static final Icon largeProperties = new Icon(
			"icons/banner/icon_document.png"); //$NON-NLS-1$
	public static final Icon smallProperties = new Icon(
			"icons/banner/cell_properties.png"); //$NON-NLS-1$
	public static final Icon largeDelete = new Icon(
			"icons/banner/delete_large.png"); //$NON-NLS-1$
	public static final Icon error = new Icon("icons/error.png"); //$NON-NLS-1$
	public static final Icon square = new Icon("icons/shape_square.png"); //$NON-NLS-1$
	public static final Icon nullTitle = new Icon(
			"icons/banner/nullTitleIcon.gif"); //$NON-NLS-1$
	public static final Icon emptyTitle = new Icon(
			"icons/banner/emptyTitleIcon.gif"); //$NON-NLS-1$
	public static final Icon delete = new Icon("icons/delete_obj.png"); //$NON-NLS-1$
	public static final Icon delete32 = new Icon("icons/delete32.png"); //$NON-NLS-1$
	public static final Icon trashrestore = new Icon("icons/trashrestore.png"); //$NON-NLS-1$
	public static final Icon trashrestoreSmall = new Icon(
			"icons/restoretrash_s.png"); //$NON-NLS-1$
	public static final Icon trash = new Icon("icons/trashcan.png"); //$NON-NLS-1$
	public static final Icon trash32 = new Icon("icons/trashcan32.png"); //$NON-NLS-1$
	public static final Icon cleartrash = new Icon("icons/clearTrash.png"); //$NON-NLS-1$
	public static final Icon file = new Icon("icons/file_obj.png"); //$NON-NLS-1$
	public static final Icon filter = new Icon("icons/filter.png"); //$NON-NLS-1$
	public static final Icon selectAll = new Icon("icons/selectall.gif"); //$NON-NLS-1$
	public static final Icon derivative = new Icon("icons/derivative.gif"); //$NON-NLS-1$
	public static final Icon original = new Icon("icons/original.gif"); //$NON-NLS-1$
	public static final Icon composite = new Icon("icons/composite.gif"); //$NON-NLS-1$
	public static final Icon component = new Icon("icons/component.gif"); //$NON-NLS-1$
	public static final Icon tableSave = new Icon("icons/table_save.png"); //$NON-NLS-1$
	public static final Icon textsearchOne = new Icon(
			"icons/textsearch_one.png"); //$NON-NLS-1$
	public static final Icon textsearchMany = new Icon(
			"icons/textsearch_many.png"); //$NON-NLS-1$
	public static final Icon textSearch = new Icon("icons/textsearch.png"); //$NON-NLS-1$
	public static final Icon format = new Icon("icons/format.gif"); //$NON-NLS-1$
	public static final Icon formatSelect = new Icon("icons/format_select.gif"); //$NON-NLS-1$
	public static final Icon query = new Icon("icons/query.gif"); //$NON-NLS-1$
	public static final Icon map = new Icon("icons/gps.gif"); //$NON-NLS-1$
	public static final Icon www = new Icon("icons/www.gif"); //$NON-NLS-1$
	public static final Icon email = new Icon("icons/email.gif"); //$NON-NLS-1$
	public static final Icon newGroup = new Icon("icons/newgroup.gif"); //$NON-NLS-1$
	public static final Icon newSlideshow = new Icon("icons/newslideshow.gif"); //$NON-NLS-1$
	public static final Icon addAlbum = new Icon("icons/add_album.png"); //$NON-NLS-1$
	public static final Icon albumRemove = new Icon("icons/heart_delete.png"); //$NON-NLS-1$
	public static final Icon addSubselection = new Icon(
			"icons/add_subselection.png"); //$NON-NLS-1$
	public static final Icon cut = new Icon("icons/cut_edit.png"); //$NON-NLS-1$
	public static final Icon copy = new Icon("icons/copy_edit.png"); //$NON-NLS-1$
	public static final Icon paste = new Icon("icons/paste_edit.png"); //$NON-NLS-1$
	public static final Icon collapseAll = new Icon("icons/collapseall.png"); //$NON-NLS-1$
	public static final Icon expandAll = new Icon("icons/expandall.png"); //$NON-NLS-1$
	public static final Icon properties = new Icon("icons/props_orange.png"); //$NON-NLS-1$
	public static final Icon properties_blue = new Icon("icons/props_blue.png"); //$NON-NLS-1$
	public static final Icon play = new Icon("icons/play.gif"); //$NON-NLS-1$
	public static final Icon toggle = new Icon("icons/toggle.png"); //$NON-NLS-1$
	public static final Icon unlink = new Icon("icons/unlink.gif"); //$NON-NLS-1$
	public static final Icon descriptionEdit = new Icon(
			"icons/page_white_edit.png"); //$NON-NLS-1$
	public static final Icon proximity = new Icon("icons/proximity.gif"); //$NON-NLS-1$
	public static final Icon image_edit = new Icon("icons/image_edit.png"); //$NON-NLS-1$
	public static final Icon image_edit_with = new Icon(
			"icons/image_edit_with.png"); //$NON-NLS-1$
	public static final Icon colorCode = new Icon("icons/colorCode.gif"); //$NON-NLS-1$
	public static final Icon sound = new Icon("icons/sound.png"); //$NON-NLS-1$
	public static final Icon sound_add = new Icon("icons/sound_add.png"); //$NON-NLS-1$
	public static final Icon sound_delete = new Icon("icons/sound_delete.png"); //$NON-NLS-1$
	public static final Icon similar = new Icon("icons/similar.gif"); //$NON-NLS-1$
	public static final Icon time = new Icon("icons/time.png"); //$NON-NLS-1$
	public static final Icon timeShift = new Icon("icons/timeShift.gif"); //$NON-NLS-1$
	public static final Icon alphab_sort = new Icon("icons/alphab_sort.gif"); //$NON-NLS-1$
	public static final Icon add = new Icon("icons/add_obj.png"); //$NON-NLS-1$
	public static final Icon save = new Icon("icons/table_save.png"); //$NON-NLS-1$
	public static final Icon sync = new Icon("icons/sync.png"); //$NON-NLS-1$
	public static final Icon slideControl_dark = new Icon(
			"icons/banner/slideControl_d.png"); //$NON-NLS-1$
	public static final Icon slideControl_nonselect = new Icon(
			"icons/banner/slideControl_n.png"); //$NON-NLS-1$
	public static final Icon slideControl = new Icon(
			"icons/banner/slideControl.png"); //$NON-NLS-1$
	public static final Icon importNewStructure = new Icon(
			"icons/banner/imp_new_structure.png"); //$NON-NLS-1$
	public static final Icon importDevice = new Icon(
			"icons/banner/import_dev.png"); //$NON-NLS-1$
	public static final Icon pref64 = new Icon("icons/banner/exportpref_wiz.png"); //$NON-NLS-1$
	public static final Icon watchedFolder = new Icon(
			"icons/banner/watchedFolder.png"); //$NON-NLS-1$
	public static final Icon splitcat = new Icon("icons/book_go.png"); //$NON-NLS-1$
	public static final Icon stack = new Icon("/icons/stack.png"); //$NON-NLS-1$
	public static final Icon hover = new Icon("/icons/comments.png"); //$NON-NLS-1$
	public static final Icon nolocation = new Icon("/icons/nolocation.png"); //$NON-NLS-1$
	public static final Icon location = new Icon("/icons/location.png"); //$NON-NLS-1$
	public static final Icon categorize = new Icon("/icons/categorize.png"); //$NON-NLS-1$
	public static final Icon keydef = new Icon("/icons/keydef.gif"); //$NON-NLS-1$
	public static final Icon cluster = new Icon("/icons/cluster.png"); //$NON-NLS-1$
	public static final Icon cue = new Icon("/icons/cue.png"); //$NON-NLS-1$
	public static final Icon transparentCover = new Icon("/icons/cover.png"); //$NON-NLS-1$
	public static final Icon expand = new Icon("/icons/expand.png"); //$NON-NLS-1$
	public static final Icon collaps = new Icon("/icons/collaps.png"); //$NON-NLS-1$
	public static final Icon collapsed = new Icon("/icons/collapsed.png"); //$NON-NLS-1$
	public static final Icon gotoBookmark = new Icon("/icons/goto_bookmark.png"); //$NON-NLS-1$
	public static final Icon addBookmark = new Icon("/icons/add_bookmark.png"); //$NON-NLS-1$
	public static final Icon bookmarks = new Icon("/icons/bookmarks.png"); //$NON-NLS-1$
	public static final Icon meta64 = new Icon("/icons/banner/xmp64.png"); //$NON-NLS-1$
	public static final Icon add_obj = new Icon("/icons/add_obj.png"); //$NON-NLS-1$
	public static final Icon copyMetadata = new Icon("/icons/copy_edit.png"); //$NON-NLS-1$
	public static final Icon pasteMetadata = new Icon("/icons/paste_edit.png"); //$NON-NLS-1$
	public static final Icon refresh = new Icon("/icons/refresh.gif"); //$NON-NLS-1$
	public static final Icon reset = new Icon("/icons/reset.gif"); //$NON-NLS-1$
	public static final Icon done = new Icon("/icons/done.png"); //$NON-NLS-1$
	public static final Icon appIcon = new Icon("/icons/zora32.gif"); //$NON-NLS-1$
	public static final Icon appIcon24 = new Icon("/icons/zora24.gif"); //$NON-NLS-1$
	public static final Icon appIconShutDown = new Icon("/icons/shutdown.png"); //$NON-NLS-1$
	public static final Icon appStart = new Icon("/icons/appstart.png"); //$NON-NLS-1$
	public static final Icon catFilter = new Icon("/icons/catfilter.png"); //$NON-NLS-1$
	public static final Icon zoraShell = new Icon("/icons/zoraShell.gif"); //$NON-NLS-1$
	public static final Icon spring = new Icon("/icons/spring.png"); //$NON-NLS-1$
	public static final Icon summer = new Icon("/icons/summer.png"); //$NON-NLS-1$
	public static final Icon autumn = new Icon("/icons/autumn.png"); //$NON-NLS-1$
	public static final Icon winter = new Icon("/icons/winter.png"); //$NON-NLS-1$
	public static final Icon seasons = new Icon("/icons/4seasons.png"); //$NON-NLS-1$
	public static final Icon network = new Icon("/icons/network.png"); //$NON-NLS-1$
	public static final Icon local = new Icon("/icons/local.png"); //$NON-NLS-1$
	public static final Icon closeButton = new Icon(
			"/icons/close_button_red.png"); //$NON-NLS-1$
	public static final Icon MIXED_OVERLAY = new Icon("/icons/mixedOverlay.png"); //$NON-NLS-1$
	public static final Icon PENDING_OVERLAY = new Icon(
			"/icons/pendingOverlay.png"); //$NON-NLS-1$
	public static final Icon user_magnify = new Icon("icons/user_magnify.png"); //$NON-NLS-1$
	public static final Icon goggles = new Icon("icons/goggles.gif"); //$NON-NLS-1$
	public static final Icon textsearch = new Icon("icons/textsearch.png"); //$NON-NLS-1$
	public static final Icon today = new Icon("icons/today.png"); //$NON-NLS-1$
	public static final Icon yesterday = new Icon("icons/yesterday.png"); //$NON-NLS-1$
	public static final Icon week = new Icon("icons/week.png"); //$NON-NLS-1$
	public static final Icon month = new Icon("icons/month.png"); //$NON-NLS-1$
	public static final Icon history = new Icon("icons/historyW.png"); //$NON-NLS-1$
	public static final Icon rename = new Icon("icons/rename.png"); //$NON-NLS-1$
	public static final Icon move = new Icon("icons/lorry_flatbed.png"); //$NON-NLS-1$
	public static final Icon warning= new Icon("icons/warn_tsk.gif"); //$NON-NLS-1$
	public static final Icon info= new Icon("icons/info_tsk.gif"); //$NON-NLS-1$
	public static final Icon note= new Icon("icons/note.png"); //$NON-NLS-1$
	public static final Icon note32= new Icon("icons/note32.png"); //$NON-NLS-1$
	public static final Icon cancel32= new Icon("icons/cancel32.png"); //$NON-NLS-1$
	public static final Icon neural= new Icon("icons/neural.png"); //$NON-NLS-1$

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
