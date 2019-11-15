package com.bdaum.zoom.mtp.internal;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import com.bdaum.zoom.mtp.MtpManager;

/**
 * The activator class controls the plug-in life cycle
 */
public class MtpActivator extends Plugin {

	public static final String PLUGIN_ID = "com.bdaum.zoom.mtp"; //$NON-NLS-1$
	private static MtpActivator plugin;
	private MtpManager mtpManager;
	

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static MtpActivator getDefault() {
		return plugin;
	}

	public void setMtpManager(MtpManager mtpManager) {
		this.mtpManager = mtpManager;
	}
	
	public MtpManager getMtpManager() {
		return mtpManager;
	}


}
