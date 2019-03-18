package com.bdaum.jna.internal;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import com.sun.jna.Native;

public class JnaActivator extends Plugin {

	private static JnaActivator plugin;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		URL resource = FileLocator.find(getBundle(), new Path('/' + libName()), null);
		try {
			resource = FileLocator.toFileURL(resource);
		} catch (IOException e) {
			// should never happen
		}
		String jnaPath = new File(resource.getPath()).getParent().toString();
		String libPath = System.getProperty("jna.boot.library.path", ""); //$NON-NLS-1$//$NON-NLS-2$
		if (libPath.indexOf(jnaPath) < 0)
			System.setProperty("jna.boot.library.path", libPath.isEmpty() ? jnaPath : jnaPath + ';' + libPath); //$NON-NLS-1$
		if (Platform.getOS() == Platform.OS_WIN32) {
			System.setProperty("jna.protected","true"); //$NON-NLS-1$ //$NON-NLS-2$
			Native.setProtected(true);
		}
	}

	public static String libName() throws UnsupportedOperationException {
		String os = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
		return (os.startsWith("windows")) ? "jnidispatch.dll" : //$NON-NLS-1$//$NON-NLS-2$
				(os.startsWith("mac os x")) //$NON-NLS-1$
						? "libjnidispatch.jnilib" //$NON-NLS-1$
						: "libjnidispatch.so"; //$NON-NLS-1$
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	public static JnaActivator getDefault() {
		return plugin;
	}
}
