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
 * (c) 2018 Berthold Daum  
 */
package com.bdaum.zoom.mtp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.ListenerList;

import com.bdaum.zoom.mtp.internal.MtpActivator;
import com.bdaum.zoom.program.BatchConstants;
import com.bdaum.zoom.program.BatchUtilities;

import jmtp.DeviceAlreadyOpenedException;
import jmtp.PortableDevice;
import jmtp.PortableDeviceFolderObject;
import jmtp.PortableDeviceManager;
import jmtp.PortableDeviceObject;
import jmtp.PortableDeviceStorageObject;

public class MtpManager {

	private PortableDeviceManager manager;
	private ListenerList<DeviceInsertionListener> deviceListeners = new ListenerList<DeviceInsertionListener>();
	private IRootManager rootManager;
	private ArrayList<PortableDevice> mtpDevices = new ArrayList<>(3);
	private ArrayList<PortableDevice> currentDevices = new ArrayList<>(3);

	public MtpManager(IRootManager rootManager) {
		this.rootManager = rootManager;
		manager = new PortableDeviceManager();
		MtpActivator.getDefault().setMtpManager(this);
	}

	public void updateVolumes() {
		if (BatchConstants.WIN32) {
			manager.refreshDeviceList();
			PortableDevice[] devices = manager.getDevices();
			mtpDevices.clear();
			for (PortableDevice device : devices) {
				try {
					device.open();
				} catch (DeviceAlreadyOpenedException e) {
					// leave open
				} catch (IOException e) {
					continue;
				}
				String protocol = device.getProtocol();
				try {
					device.close();
				} catch (Exception e) {
					// ignore
				}
				if (protocol == null || protocol.startsWith("MTP:") || protocol.startsWith("PTP:")) //$NON-NLS-1$ //$NON-NLS-2$
					mtpDevices.add(device);
			}
			if (mtpDevices.size() < currentDevices.size()) {
				currentDevices.clear();
				currentDevices.addAll(mtpDevices);
				fireDeviceEjected();
			} else if (currentDevices.size() < mtpDevices.size()) {
				currentDevices.clear();
				currentDevices.addAll(mtpDevices);
				fireDeviceInserted();
			} else
				lp: for (int i = 0; i < mtpDevices.size(); i++) {
					for (int j = 0; j < currentDevices.size(); j++)
						if (currentDevices.get(j).equals(mtpDevices.get(i)))
							continue lp;
					currentDevices.clear();
					currentDevices.addAll(mtpDevices);
					fireDeviceInserted();
					return;
				}
		}
	}

	private void fireDeviceEjected() {
		for (DeviceInsertionListener listener : deviceListeners)
			listener.deviceEjected();
	}

	private void fireDeviceInserted() {
		for (DeviceInsertionListener listener : deviceListeners)
			listener.deviceInserted();
	}

	public StorageObject[] findDCIMs() {
		List<File> dcims = BatchUtilities.findDCIMs();
		int l1 = dcims.size();
		int l2 = 0;
		List<StorageObject> dcims2 = null;
		if (BatchConstants.WIN32) {
			dcims2 = findMobileDCIMs();
			l2 = dcims2.size();
		}
		StorageObject[] objects = new StorageObject[l1 + l2];
		for (int i = 0; i < l1; i++)
			objects[i] = new StorageObject(dcims.get(i));
		if (dcims2 != null)
			for (int i = 0; i < dcims2.size(); i++)
				objects[i + l1] = dcims2.get(i);
		return objects;
	}

	private List<StorageObject> findMobileDCIMs() {
		List<StorageObject> dcims = new ArrayList<>(5);
		for (PortableDevice device : currentDevices) {
			try {
				device.open();
			} catch (DeviceAlreadyOpenedException e) {
				// leave open
			} catch (IOException e) {
				continue;
			}
			try {
				PortableDeviceObject[] objects = device.getRootObjects();
				if (objects != null) {
					for (PortableDeviceObject rootObject : objects) {
						if (rootObject instanceof PortableDeviceStorageObject) {
							PortableDeviceStorageObject storage = (PortableDeviceStorageObject) rootObject;
							try {
								for (PortableDeviceObject child : storage.getChildObjects())
									try {
										if ("DCIM".equals(child.getName()) //$NON-NLS-1$
												&& child instanceof PortableDeviceFolderObject)
											dcims.add(new StorageObject(child, device, null));
									} catch (IOException e) {
										continue;
									}
							} catch (IOException e) {
								continue;
							}
						}
					}
				}
			} catch (IOException e1) {
				continue;
			} finally {
				try {
					device.close();
				} catch (Exception e) {
					// ignore
				}
			}
		}
		return dcims;
	}

	public void addDeviceInsertionListener(DeviceInsertionListener listener) {
		deviceListeners.add(listener);
	}

	public void removeDeviceInsertionListener(DeviceInsertionListener listener) {
		deviceListeners.remove(listener);
	}

	public IRootManager getRootManager() {
		return rootManager;
	}

}
