package com.bdaum.jna.internal;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class JnaActivator extends Plugin {

	private static JnaActivator plugin;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		URL resource = FileLocator.find(getBundle(), new Path('/' + libName()),
				null);
		try {
			resource = FileLocator.toFileURL(resource);
		} catch (IOException e) {
			// should never happen
		}
		String jnaPath = new File(resource.getPath()).getParent().toString();
		String libPath = System.getProperty("jna.boot.library.path", ""); //$NON-NLS-1$//$NON-NLS-2$
		if (libPath.indexOf(jnaPath) < 0)
			System.setProperty(
					"jna.boot.library.path", libPath.length() > 0 ? jnaPath + ';' + libPath : jnaPath); //$NON-NLS-1$
	}

	/**
	 * Calculate the filename of the native hunspell lib. The files have
	 * completely different names to allow them to live in the same directory
	 * and avoid confusion.
	 */
	public static String libName() throws UnsupportedOperationException {
		String os = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
		if (os.startsWith("windows")) //$NON-NLS-1$
			return "jnidispatch.dll"; //$NON-NLS-1$
		if (os.startsWith("mac os x")) //$NON-NLS-1$
			return "libjnidispatch.jnilib"; //$NON-NLS-1$
		return "libjnidispatch.so"; //$NON-NLS-1$
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
