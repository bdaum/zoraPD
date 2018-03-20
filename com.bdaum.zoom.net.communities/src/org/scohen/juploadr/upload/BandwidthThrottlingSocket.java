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

package org.scohen.juploadr.upload;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;

public class BandwidthThrottlingSocket extends Socket {
    private Socket wrapped;
    private int maxBytesPerSecond;

    public BandwidthThrottlingSocket(Socket wrapped, int maxBytesPerSecond) {
        this.wrapped = wrapped;
        this.maxBytesPerSecond = maxBytesPerSecond;
    }

    
	@Override
	public OutputStream getOutputStream() throws IOException {
        return new BandwidthThrottlingOutputStream(wrapped.getOutputStream(),
                maxBytesPerSecond);
    }

    
	@Override
	public void bind(SocketAddress bindpoint) throws IOException {
        wrapped.bind(bindpoint);
    }

    
	@Override
	public synchronized void close() throws IOException {
        wrapped.close();
    }

    
	@Override
	public void connect(SocketAddress endpoint, int timeout) throws IOException {
        wrapped.connect(endpoint, timeout);
    }

    
	@Override
	public void connect(SocketAddress endpoint) throws IOException {
        wrapped.connect(endpoint);
    }

    
	@Override
	public boolean equals(Object obj) {
        return wrapped.equals(obj);
    }

    
	@Override
	public SocketChannel getChannel() {
        return wrapped.getChannel();
    }

    
	@Override
	public InetAddress getInetAddress() {
        return wrapped.getInetAddress();
    }

    
	@Override
	public InputStream getInputStream() throws IOException {
        return wrapped.getInputStream();
    }

    
	@Override
	public boolean getKeepAlive() throws SocketException {
        return wrapped.getKeepAlive();
    }

    
	@Override
	public InetAddress getLocalAddress() {
        return wrapped.getLocalAddress();
    }

    
	@Override
	public int getLocalPort() {
        return wrapped.getLocalPort();
    }

    
	@Override
	public SocketAddress getLocalSocketAddress() {
        return wrapped.getLocalSocketAddress();
    }

    
	@Override
	public boolean getOOBInline() throws SocketException {
        return wrapped.getOOBInline();
    }

    
	@Override
	public int getPort() {
        return wrapped.getPort();
    }

    
	@Override
	public synchronized int getReceiveBufferSize() throws SocketException {
        return wrapped.getReceiveBufferSize();
    }

    
	@Override
	public SocketAddress getRemoteSocketAddress() {
        return wrapped.getRemoteSocketAddress();
    }

    
	@Override
	public boolean getReuseAddress() throws SocketException {
        return wrapped.getReuseAddress();
    }

    
	@Override
	public synchronized int getSendBufferSize() throws SocketException {
        return wrapped.getSendBufferSize();
    }

    
	@Override
	public int getSoLinger() throws SocketException {
        return wrapped.getSoLinger();
    }

    
	@Override
	public synchronized int getSoTimeout() throws SocketException {
        return wrapped.getSoTimeout();
    }

    
	@Override
	public boolean getTcpNoDelay() throws SocketException {
        return wrapped.getTcpNoDelay();
    }

    
	@Override
	public int getTrafficClass() throws SocketException {
        return wrapped.getTrafficClass();
    }

    
	@Override
	public int hashCode() {
        return wrapped.hashCode();
    }

    
	@Override
	public boolean isBound() {
        return wrapped.isBound();
    }

    
	@Override
	public boolean isClosed() {
        return wrapped.isClosed();
    }

    
	@Override
	public boolean isConnected() {
        return wrapped.isConnected();
    }

    
	@Override
	public boolean isInputShutdown() {
        return wrapped.isInputShutdown();
    }

    
	@Override
	public boolean isOutputShutdown() {
        return wrapped.isOutputShutdown();
    }

    
	@Override
	public void sendUrgentData(int data) throws IOException {
        wrapped.sendUrgentData(data);
    }

    
	@Override
	public void setKeepAlive(boolean on) throws SocketException {
        wrapped.setKeepAlive(on);
    }

    
	@Override
	public void setOOBInline(boolean on) throws SocketException {
        wrapped.setOOBInline(on);
    }

    
	@Override
	public synchronized void setReceiveBufferSize(int size) throws SocketException {
        wrapped.setReceiveBufferSize(size);
    }

    
	@Override
	public void setReuseAddress(boolean on) throws SocketException {
        wrapped.setReuseAddress(on);
    }

    
	@Override
	public synchronized void setSendBufferSize(int size) throws SocketException {
        wrapped.setSendBufferSize(size);
    }

    
	@Override
	public void setSoLinger(boolean on, int linger) throws SocketException {
        wrapped.setSoLinger(on, linger);
    }

    
	@Override
	public synchronized void setSoTimeout(int timeout) throws SocketException {
        wrapped.setSoTimeout(timeout);
    }

    
	@Override
	public void setTcpNoDelay(boolean on) throws SocketException {
        wrapped.setTcpNoDelay(on);
    }

    
	@Override
	public void setTrafficClass(int tc) throws SocketException {
        wrapped.setTrafficClass(tc);
    }

    
	@Override
	public void shutdownInput() throws IOException {
        wrapped.shutdownInput();
    }

    
	@Override
	public void shutdownOutput() throws IOException {
        wrapped.shutdownOutput();
    }

    
	@Override
	public String toString() {
        return wrapped.toString();
    }

}
