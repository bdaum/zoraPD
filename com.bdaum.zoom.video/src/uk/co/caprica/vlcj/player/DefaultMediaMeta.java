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

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.binding.internal.libvlc_media_t;
import uk.co.caprica.vlcj.binding.internal.libvlc_meta_t;
import uk.co.caprica.vlcj.logger.Logger;

/**
 * Representation of all available media meta data.
 * <p>
 * This implementation retains a reference to the supplied native media instance, and releases
 * this native reference in {@link #release()}.
 * <p>
 * Invoking {@link #getArtworkUrl()}, {@link #getArtwork()} or {@link #toString()} may cause an
 * HTTP request to be made to download artwork.
 */
class DefaultMediaMeta implements MediaMeta {

    /**
     * Set to true when the player has been released.
     */
    private final AtomicBoolean released = new AtomicBoolean();

    /**
     * Native library interface.
     */
    private final LibVlc libvlc;

    /**
     * Associated media instance.
     * <p>
     * May be <code>null</code>.
     */
    private final libvlc_media_t media;

    /**
     * Artwork.
     * <p>
     * Lazily loaded.
     */
    private BufferedImage artwork;

    /**
     * Create media meta.
     *
     * @param libvlc native library instance
     * @param media media instance
     */
    DefaultMediaMeta(LibVlc libvlc, libvlc_media_t media) {
        this.libvlc = libvlc;
        this.media = media;
        // Keep a native reference
        libvlc.libvlc_media_retain(media);
    }

    public final void parse() {
        Logger.debug("parse()");
        libvlc.libvlc_media_parse(media);
    }

    public final String getTitle() {
        return getMeta(libvlc_meta_t.libvlc_meta_Title);
    }

    public final void setTitle(String title) {
        setMeta(libvlc_meta_t.libvlc_meta_Title, title);
    }

    public final String getArtist() {
        return getMeta(libvlc_meta_t.libvlc_meta_Artist);
    }

    public final void setArtist(String artist) {
        setMeta(libvlc_meta_t.libvlc_meta_Artist, artist);
    }

    public final String getGenre() {
        return getMeta(libvlc_meta_t.libvlc_meta_Genre);
    }

    public final void setGenre(String genre) {
        setMeta(libvlc_meta_t.libvlc_meta_Genre, genre);
    }

    public final String getCopyright() {
        return getMeta(libvlc_meta_t.libvlc_meta_Copyright);
    }

    public final void setCopyright(String copyright) {
        setMeta(libvlc_meta_t.libvlc_meta_Copyright, copyright);
    }

    public final String getAlbum() {
        return getMeta(libvlc_meta_t.libvlc_meta_Album);
    }

    public final void setAlbum(String album) {
        setMeta(libvlc_meta_t.libvlc_meta_Album, album);
    }

    public final String getTrackNumber() {
        return getMeta(libvlc_meta_t.libvlc_meta_TrackNumber);
    }

    public final void setTrackNumber(String trackNumber) {
        setMeta(libvlc_meta_t.libvlc_meta_TrackNumber, trackNumber);
    }

    public final String getDescription() {
        return getMeta(libvlc_meta_t.libvlc_meta_Description);
    }

    public final void setDescription(String description) {
        setMeta(libvlc_meta_t.libvlc_meta_Description, description);
    }

    public final String getRating() {
        return getMeta(libvlc_meta_t.libvlc_meta_Rating);
    }

    public final void setRating(String rating) {
        setMeta(libvlc_meta_t.libvlc_meta_Rating, rating);
    }

    public final String getDate() {
        return getMeta(libvlc_meta_t.libvlc_meta_Date);
    }

    public final void setDate(String date) {
        setMeta(libvlc_meta_t.libvlc_meta_Date, date);
    }

    public final String getSetting() {
        return getMeta(libvlc_meta_t.libvlc_meta_Setting);
    }

    public final void setSetting(String setting) {
        setMeta(libvlc_meta_t.libvlc_meta_Setting, setting);
    }

    public final String getUrl() {
        return getMeta(libvlc_meta_t.libvlc_meta_URL);
    }

    public final void setUrl(String url) {
        setMeta(libvlc_meta_t.libvlc_meta_URL, url);
    }

    public final String getLanguage() {
        return getMeta(libvlc_meta_t.libvlc_meta_Language);
    }

    public final void setLanguage(String language) {
        setMeta(libvlc_meta_t.libvlc_meta_Language, language);
    }

    public final String getNowPlaying() {
        return getMeta(libvlc_meta_t.libvlc_meta_NowPlaying);
    }

    public final void setNowPlaying(String nowPlaying) {
        setMeta(libvlc_meta_t.libvlc_meta_NowPlaying, nowPlaying);
    }

    public final String getPublisher() {
        return getMeta(libvlc_meta_t.libvlc_meta_Publisher);
    }

    public final void setPublisher(String publisher) {
        setMeta(libvlc_meta_t.libvlc_meta_Publisher, publisher);
    }

    public final String getEncodedBy() {
        return getMeta(libvlc_meta_t.libvlc_meta_EncodedBy);
    }

    public final void setEncodedBy(String encodedBy) {
        setMeta(libvlc_meta_t.libvlc_meta_EncodedBy, encodedBy);
    }

    public final String getArtworkUrl() {
        return getMeta(libvlc_meta_t.libvlc_meta_ArtworkURL);
    }

    public final void setArtworkUrl(String artworkUrl) {
        setMeta(libvlc_meta_t.libvlc_meta_ArtworkURL, artworkUrl);
    }

    public final String getTrackId() {
        return getMeta(libvlc_meta_t.libvlc_meta_TrackID);
    }

    public final void setTrackId(String trackId) {
        setMeta(libvlc_meta_t.libvlc_meta_TrackID, trackId);
    }

    public final BufferedImage getArtwork() {
        Logger.debug("getArtwork()");
        if(artwork == null) {
            String artworkUrl = getArtworkUrl();
            if(artworkUrl != null && artworkUrl.length() > 0) {
                try {
                    URL url = new URL(artworkUrl);
                    Logger.debug("url={}", url);
                    artwork = ImageIO.read(url);
                }
                catch(Exception e) {
                    throw new RuntimeException("Failed to load artwork", e);
                }
            }
        }
        return artwork;
    }

    public final long getLength() {
        return libvlc.libvlc_media_get_duration(media);
    }

    public final void save() {
        Logger.debug("save()");
        libvlc.libvlc_media_save_meta(media);
    }

    public final void release() {
        Logger.debug("release()");
        if(released.compareAndSet(false, true)) {
            libvlc.libvlc_media_release(media);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        Logger.debug("finalize()");
        Logger.debug("Meta data has been garbage collected");
        super.finalize();
        // FIXME should this invoke release()?
    }

    /**
     * Get a local meta data value for a media instance.
     *
     * @param metaType type of meta data
     * @return meta data value
     */
    private String getMeta(libvlc_meta_t metaType) {
        Logger.trace("getMeta(metaType={},media={})", metaType, media);
        return NativeString.getNativeString(libvlc, libvlc.libvlc_media_get_meta(media, metaType.intValue()));
    }

    /**
     * Set a local meta data value for a media instance.
     * <p>
     * Setting meta does not affect the underlying media file until {@link #save()} is called.
     *
     * @param metaType type of meta data
     * @param media media instance
     * @param value meta data value
     */
    private void setMeta(libvlc_meta_t metaType, String value) {
        Logger.trace("setMeta(metaType={},media={},value={})", metaType, media, value);
        libvlc.libvlc_media_set_meta(media, metaType.intValue(), value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        sb.append(getClass().getSimpleName()).append('[');
        sb.append("title=").append(getTitle()).append(',');
        sb.append("artist=").append(getArtist()).append(',');
        sb.append("genre=").append(getGenre()).append(',');
        sb.append("copyright=").append(getCopyright()).append(',');
        sb.append("album=").append(getAlbum()).append(',');
        sb.append("trackNumber=").append(getTrackNumber()).append(',');
        sb.append("description=").append(getDescription()).append(',');
        sb.append("rating=").append(getRating()).append(',');
        sb.append("date=").append(getDate()).append(',');
        sb.append("setting=").append(getSetting()).append(',');
        sb.append("url=").append(getUrl()).append(',');
        sb.append("language=").append(getLanguage()).append(',');
        sb.append("nowPlaying=").append(getNowPlaying()).append(',');
        sb.append("publisher=").append(getPublisher()).append(',');
        sb.append("encodedBy=").append(getEncodedBy()).append(',');
        sb.append("artworkUrl=").append(getArtworkUrl()).append(',');
        sb.append("trackId=").append(getTrackId()).append(',');
        sb.append("length=").append(getLength()).append(']');
        return sb.toString();
    }
}
