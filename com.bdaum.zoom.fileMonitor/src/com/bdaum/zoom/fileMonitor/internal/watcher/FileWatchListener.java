package com.bdaum.zoom.fileMonitor.internal.watcher;

import java.io.File;

public interface FileWatchListener {
	public static final int NOOP = -1;
	public static final int CREATED = 0;
	public static final int MODIFIED = 1;
	public static final int DELETED = 2;

	public void fileChanged(File file, int kind);
}