/*
 * This file is part of the ZoRa project: http://www.photozora.org.
 * It is an adaptation of the equally named file from the jUploadr project (http://sourceforge.net/projects/juploadr/)
 * (c) 2004 Steve Cohen and others
 * 
 * jUploadr is licensed under the GNU Library or Lesser General Public License (LGPL).
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
 * Modifications (c) 2009 Berthold Daum  
 */

package org.scohen.juploadr.event;

import org.scohen.juploadr.app.ImageAttributes;

/**
 * @author John
 */
public class UploadEvent {
    private double bytesUploaded = 0;
    private boolean uploadStarted;
    private boolean uploadSuccessful;
    private boolean uploadCompleted;
    private String message;
    private ImageAttributes source;

    public UploadEvent() {

    }

    /**
     * @param affectedImage
     * @param bytesUploaded
     * @param uploadStarted
     * @param uploadSuccessful
     * @param uploadCompleted
     */
    public UploadEvent(ImageAttributes source, double bytesUploaded,
            boolean uploadStarted, boolean uploadSuccessful) {
        this.bytesUploaded = bytesUploaded;
        this.uploadStarted = uploadStarted;
        this.uploadSuccessful = uploadSuccessful;
        this.source = source;
    }

    public UploadEvent(ImageAttributes source, double bytesUploaded,
            boolean uploadStarted, boolean uploadSuccessful, String errorMessage) {
        this(source, bytesUploaded, uploadStarted, uploadSuccessful);
        this.message = errorMessage;
    }

    /**
     * @return Returns the uploadCompleted.
     */
    public boolean isUploadCompleted() {
        return this.uploadCompleted;
    }

    /**
     * @param uploadCompleted
     *            The uploadCompleted to set.
     */
    public void setUploadCompleted(boolean uploadCompleted) {
        this.uploadCompleted = uploadCompleted;
    }

    /**
     * @return Returns the uploadCompletion.
     */
    public double getBytesWritten() {
        return this.bytesUploaded;
    }

    /**
     * @param uploadCompletion
     *            The uploadCompletion to set.
     */
    public void setBytesWritten(double uploadCompletion) {
        this.bytesUploaded = uploadCompletion;
    }

    /**
     * @return Returns the uploadFailed.
     */
    public boolean isUploadFailed() {
        return this.uploadSuccessful;
    }

    /**
     * @param uploadFailed
     *            The uploadFailed to set.
     */
    public void setUploadFailed(boolean uploadFailed) {
        this.uploadSuccessful = uploadFailed;
    }

    /**
     * @return Returns the uploadStarted.
     */
    public boolean isUploadStarted() {
        return this.uploadStarted;
    }

    /**
     * @param uploadStarted
     *            The uploadStarted to set.
     */
    public void setUploadStarted(boolean uploadStarted) {
        this.uploadStarted = uploadStarted;
    }

    public String getErrorMessage() {
        return this.message;
    }

    public void setErrorMessage(String message) {
        this.message = message;
    }

    public ImageAttributes getSource() {
        return source;
    }

    /**
     * @param image
     */
    public void setSource(ImageAttributes image) {
        source = image;

    }
}