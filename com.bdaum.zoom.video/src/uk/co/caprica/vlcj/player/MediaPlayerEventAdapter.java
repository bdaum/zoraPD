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

package uk.co.caprica.vlcj.player;

import javax.swing.SwingUtilities;

import uk.co.caprica.vlcj.binding.internal.libvlc_media_t;

/**
 * Default implementation of the media player event listener.
 * <p>
 * Simply override the methods you're interested in.
 * <p>
 * Events are <em>not</em> raised on the Swing Event Dispatch thread so if updating user
 * interface components in response to these events care must be taken to use
 * {@link SwingUtilities#invokeLater(Runnable)}.
 */
public class MediaPlayerEventAdapter implements MediaPlayerEventListener {

    // === Events relating to the media player ==================================


    public void mediaChanged(MediaPlayer mediaPlayer, libvlc_media_t media, String mrl) {
    }


    public void opening(MediaPlayer mediaPlayer) {
    }


    public void buffering(MediaPlayer mediaPlayer, float newCache) {
    }


    public void playing(MediaPlayer mediaPlayer) {
    }


    public void paused(MediaPlayer mediaPlayer) {
    }


    public void stopped(MediaPlayer mediaPlayer) {
    }


    public void forward(MediaPlayer mediaPlayer) {
    }


    public void backward(MediaPlayer mediaPlayer) {
    }


    public void finished(MediaPlayer mediaPlayer) {
    }


    public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
    }


    public void positionChanged(MediaPlayer mediaPlayer, float newPosition) {
    }


    public void seekableChanged(MediaPlayer mediaPlayer, int newSeekable) {
    }


    public void pausableChanged(MediaPlayer mediaPlayer, int newSeekable) {
    }


    public void titleChanged(MediaPlayer mediaPlayer, int newTitle) {
    }


    public void snapshotTaken(MediaPlayer mediaPlayer, String filename) {
    }


    public void lengthChanged(MediaPlayer mediaPlayer, long newLength) {
    }


    public void videoOutput(MediaPlayer mediaPlayer, int newCount) {
    }


    public void error(MediaPlayer mediaPlayer) {
    }

    // === Events relating to the current media =================================


    public void mediaSubItemAdded(MediaPlayer mediaPlayer, libvlc_media_t subItem) {
    }


    public void mediaDurationChanged(MediaPlayer mediaPlayer, long newDuration) {
    }


    public void mediaParsedChanged(MediaPlayer mediaPlayer, int newStatus) {
    }


    public void mediaFreed(MediaPlayer mediaPlayer) {
    }


    public void mediaStateChanged(MediaPlayer mediaPlayer, int newState) {
    }


    public void mediaMetaChanged(MediaPlayer mediaPlayer, int metaType) {
    }

    // === Synthetic/semantic events ============================================


    public void newMedia(MediaPlayer mediaPlayer) {
    }


    public void subItemPlayed(MediaPlayer mediaPlayer, int subItemIndex) {
    }


    public void subItemFinished(MediaPlayer mediaPlayer, int subItemIndex) {
    }


    public void endOfSubItems(MediaPlayer mediaPlayer) {
    }
}
