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
package com.bdaum.juploadr.uploadapi.locrrest.upload;

import java.util.SortedMap;
import java.util.TreeMap;

import org.scohen.juploadr.uploadapi.CommunicationException;
import org.scohen.juploadr.uploadapi.ProtocolException;
import org.scohen.juploadr.uploadapi.Session;

import com.bdaum.juploadr.uploadapi.locrrest.DefaultLocrHandler;
import com.bdaum.juploadr.uploadapi.locrrest.LocrMethod;

public class GetUploadStatus extends LocrMethod {
	
    private static final String PRO = "pro"; //$NON-NLS-1$
    private static final String FREE = "free"; //$NON-NLS-1$

	public GetUploadStatus(Session session) {
		super(session);
	}

    /**
     * @return Returns the maxBandwidth.
     */
    public long getMaxBandwidth() {
        return getSession().getAccount().getTrafficLimit();
    }

    /**
     * @param maxBandwidth
     *            The maxBandwidth to set.
     */
    public void setMaxBandwidth(long maxBandwidth) {
        getSession().getAccount().setTrafficLimit( maxBandwidth);
    }

    /**
     * @return Returns the pro.
     */
    public boolean isPro() {
        return PRO.equals(getSession().getAccount().getAccountType());
    }

    /**
     * @param pro
     *            The pro to set.
     */
    public void setPro(boolean pro) {
        getSession().getAccount().setAccountType(pro ? PRO : FREE);
        getSession().getAccount().setCanReplace(false);
        getSession().getAccount().setSupportsRaw(false);
        getSession().getAccount().setUnlimited(false);
        if (getSession().getAccount().getTrafficLimit() <= 0) {
        	getSession().getAccount().setTrafficLimit(pro ? 2L*1024L*1024L*1024L : 100*1024L*1024L);
        }
        if (getSession().getAccount().getAvailableAlbums() <= 0) {
        	getSession().getAccount().setAvailableAlbums(pro ? Integer.MAX_VALUE : 5);
        }
    }

    /**
     * @return Returns the usedBandwidth.
     */
    public long getUsedBandwidth() {
        return getSession().getAccount().getCurrentUploadUsed();
    }

    /**
     * @param usedBandwidth
     *            The usedBandwidth to set.
     */
    public void setUsedBandwidth(long usedBandwidth) {
        getSession().getAccount().setCurrentUploadUsed(usedBandwidth);
    }

    
	@Override
	public DefaultLocrHandler getResponseHandler() {
        return new GetUploadStatusHandler(this);
    }

    
	@Override
	public SortedMap<String, String> getParams() {
        SortedMap<String, String> map = new TreeMap<String, String>();
        map.put("method", "update_account_xml.php?");  //$NON-NLS-1$//$NON-NLS-2$
        return map;
    }

    
	@Override
	public boolean isAuthorized() {
        return true;
    }

    public void setUsername(String username) {
    	getSession().getAccount().setUsername(username);
    	getSession().getAccount().setName(username);
    }

    public String getUsername() {
        return getSession().getAccount().getUsername();
    }

    public boolean isUnlimited() {
        return getSession().getAccount().isUnlimited();
    }

    public void setUnlimited(boolean unlimited) {
        getSession().getAccount().setUnlimited(unlimited);
    }

    
    @Override
	public boolean execute() throws ProtocolException, CommunicationException {
        boolean success = super.execute();
        return success;
    }

	public void setMaxFilesize(long max) {
		getSession().getAccount().setMaxFilesize( max);
	}

	public void setMaxVideosize(long max) {
		getSession().getAccount().setMaxVideosize( max);
	}
    
    

}
