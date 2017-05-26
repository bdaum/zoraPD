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
package com.bdaum.juploadr.uploadapi.locrrest.authentification;

import java.util.SortedMap;
import java.util.TreeMap;

import org.scohen.juploadr.uploadapi.ProtocolException;
import org.scohen.juploadr.uploadapi.Session;

import com.bdaum.juploadr.uploadapi.locrrest.DefaultLocrHandler;
import com.bdaum.juploadr.uploadapi.locrrest.LocrMethod;

public class GetApiSignature extends LocrMethod {

	private String signature;

	public GetApiSignature(Session session) {
		super(session);
	}

	
	@Override
	public ProtocolException buildProtocolException(int errorCode) {
		return null;
	}

	
	@Override
	public SortedMap<String, String> getParams() {
		SortedMap<String, String> params = new TreeMap<String, String>();
        params.put("method", "get_api_signature.php"); //$NON-NLS-1$ //$NON-NLS-2$
        return params;
	}
	
	
	@Override
	public boolean parseResponse(String response) throws ProtocolException {
		this.signature = response;
		return true;
	}

	
	@Override
	public DefaultLocrHandler getResponseHandler() {
		return null;
	}

	
	@Override
	public boolean isAuthorized() {
		return false;
	}

	
	@Override
	public String getApiSignature() {
		return signature;
	}

}
