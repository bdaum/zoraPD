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
 * Modifications (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */

package org.scohen.juploadr.upload;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.DefaultProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

public class BandwidthLimitingProtocolSocketFactory extends
        DefaultProtocolSocketFactory implements ProtocolSocketFactory {

    private final int bandwidth;

	public BandwidthLimitingProtocolSocketFactory(int bandwidth) {
		this.bandwidth = bandwidth;
	}

	
	@Override
	public Socket createSocket(String host, int port, InetAddress localAddress,
            int localPort) throws IOException, UnknownHostException {
        return new BandwidthThrottlingSocket(super.createSocket(host, port,
                localAddress, localPort), bandwidth);
    }

    
	@Override
	public Socket createSocket(String host, int port, InetAddress localAddress,
            int localPort, HttpConnectionParams params) throws IOException,
            UnknownHostException, ConnectTimeoutException {
        return new BandwidthThrottlingSocket(super.createSocket(host, port,
                localAddress, localPort), bandwidth);
    }

    
	@Override
	public Socket createSocket(String host, int port) throws IOException,
            UnknownHostException {
        return new BandwidthThrottlingSocket(super.createSocket(host, port),
        		bandwidth);
    }
    


}
