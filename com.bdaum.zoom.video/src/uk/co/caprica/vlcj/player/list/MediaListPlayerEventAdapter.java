/*
 * This file is part of VLCJ.
 *
 * VLCJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VLCJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with VLCJ.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2009, 2010, 2011, 2012, 2013 Caprica Software Limited.
 */

package uk.co.caprica.vlcj.player.list;

import uk.co.caprica.vlcj.binding.internal.libvlc_media_t;

/**
 * Default implementation of the media player event listener.
 * <p>
 * Simply override the methods you're interested in.
 */
public class MediaListPlayerEventAdapter implements MediaListPlayerEventListener {

    // === Events relating to the media player ==================================

    public void played(MediaListPlayer mediaListPlayer) {
    }

    public void nextItem(MediaListPlayer mediaListPlayer, libvlc_media_t item, String itemMrl) {
    }

    public void stopped(MediaListPlayer mediaListPlayer) {
    }

    // === Events relating to the current media =================================

    public void mediaMetaChanged(MediaListPlayer mediaListPlayer, int metaType) {
    }

    public void mediaSubItemAdded(MediaListPlayer mediaListPlayer, libvlc_media_t subItem) {
    }

    public void mediaDurationChanged(MediaListPlayer mediaListPlayer, long newDuration) {
    }

    public void mediaParsedChanged(MediaListPlayer mediaListPlayer, int newStatus) {
    }

    public void mediaFreed(MediaListPlayer mediaListPlayer) {
    }

    public void mediaStateChanged(MediaListPlayer mediaListPlayer, int newState) {
    }
}
