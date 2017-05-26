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
 * (c) 2011 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.fileMonitor.internal.watcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.bdaum.zoom.fileMonitor.internal.filefilter.FilterChain;

public class FileMonitor {

	private static class WatchedFolderEntry {
		private final Path path;

		private final boolean recursive;

		private final FilterChain filters;

		public WatchedFolderEntry(Path path, boolean recursive,
				FilterChain filters) {
			this.path = path;
			this.recursive = recursive;
			this.filters = filters;
		}

		public Path getPath() {
			return path;
		}

		public boolean isRecursive() {
			return recursive;
		}

		public FilterChain getFilters() {
			return filters;
		}

		public boolean accepts(File file, boolean isDir) {
			return filters == null || filters.accept(file, isDir);
		}

	}

	List<FileWatchListener> listeners = new ArrayList<FileWatchListener>(3);

	private WatchService watcher;

	private Map<WatchKey, WatchedFolderEntry> keyMap;
	private Set<String> pathSet = new HashSet<String>();
	private Path thumbs = (new File("thumbs.db")).toPath(); //$NON-NLS-1$
	public static final int FILE_CREATED = 0;
	public static final int FILE_MODIFIED = 0;

	public FileMonitor() throws IOException {
		this.keyMap = new HashMap<WatchKey, WatchedFolderEntry>();
		this.watcher = FileSystems.getDefault().newWatchService();
	}

	public void addFileListener(FileWatchListener fileListener) {
		listeners.add(fileListener);
	}

	public void removeWatch(File file) {
		String filePath = file.getPath();
		String basePath = filePath + File.separatorChar;
		Set<Entry<WatchKey, WatchedFolderEntry>> entrySet = keyMap.entrySet();
		@SuppressWarnings("unchecked")
		Entry<WatchKey, WatchedFolderEntry>[] entries = entrySet
				.toArray(new Entry[entrySet.size()]);
		for (Map.Entry<WatchKey, WatchedFolderEntry> keyEntry : entries) {
			WatchedFolderEntry folderEntry = keyEntry.getValue();
			String folderPath = folderEntry.getPath().toString();
			if (folderEntry.isRecursive() && folderPath.startsWith(basePath)
					|| folderPath.equals(filePath)) {
				WatchKey key = keyEntry.getKey();
				key.cancel();
				keyMap.remove(key);
				pathSet.remove(folderPath);
			}
		}
	}

	public void addWatch(File dir, boolean recursive, FilterChain filters)
			throws IOException {
		int n = keyMap.size();
		register(dir, recursive, filters, false);
		if (n == 0)
			new Thread("FileWatcher") { //$NON-NLS-1$
				@Override
				public void run() {
					processEvents();
				}
			}.start();
	}

	/**
	 * @param dir
	 *            - the directory to wath
	 * @param recursive
	 *            - true if subfolders should be watched
	 * @param filters
	 *            - filter for subfolders
	 * @param notify
	 *            - true if the presence of files should be notified as new
	 *            files
	 * @throws IOException
	 */
	private void register(File dir, boolean recursive, FilterChain filters,
			boolean notify) throws IOException {
		Path p = Paths.get(dir.getAbsolutePath());
		WatchedFolderEntry entry = new WatchedFolderEntry(p, recursive, filters);
		WatchKey key = p.register(watcher,
				StandardWatchEventKinds.ENTRY_CREATE,
				StandardWatchEventKinds.ENTRY_DELETE,
				StandardWatchEventKinds.ENTRY_MODIFY);
		keyMap.put(key, entry);
		pathSet.add(p.toString());
		if (recursive) {
			File[] children = dir.listFiles();
			if (children != null)
				for (File child : children)
					if (child.isDirectory()) {
						if (filters == null || filters.accept(child, true))
							register(child, recursive, filters, notify);
					} else if (notify
							&& (filters == null || filters.accept(child, false)))
						fireFileChanged(child, FileWatchListener.CREATED);
		}
	}

	public void dispose() {
		for (WatchKey key : keyMap.keySet())
			key.cancel();
		keyMap.clear();
		pathSet.clear();
		try {
			watcher.close();
		} catch (IOException e) {
			// ignore
		}
	}

	/**
	 * Process all events for keys queued to the watcher
	 */
	private void processEvents() {
		while (true) {
			WatchKey key;
			try {
				key = watcher.take();
			} catch (InterruptedException x) {
				return;
			} catch (ClosedWatchServiceException x) {
				return;
			}
			WatchedFolderEntry entry = keyMap.get(key);
			if (entry == null)
				continue;
			Path dir = entry.getPath();
			for (WatchEvent<?> event : key.pollEvents()) {
				@SuppressWarnings("rawtypes")
				WatchEvent.Kind kind = event.kind();
				if (kind == StandardWatchEventKinds.OVERFLOW)
					continue;
				@SuppressWarnings("unchecked")
				Path child = dir.resolve(((WatchEvent<Path>) event).context());
				if (child.endsWith(thumbs))
					continue;
				// System.out.println(kind + "   " + child + "  " + ev.count()
				// + " " + System.currentTimeMillis());

				if (entry.isRecursive()) {
					if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
						File file = child.toFile();
						try {
							if (file.isDirectory()) {
								if (entry.accepts(file, true))
									register(file, true, entry.getFilters(),
											true);
							} else
								fireFileChanged(file, FileWatchListener.CREATED);
						} catch (IOException x) {
							continue;
						}
					} else if (kind == StandardWatchEventKinds.ENTRY_MODIFY)
						fireFileChanged(child.toFile(),
								FileWatchListener.MODIFIED);
					else if (kind == StandardWatchEventKinds.ENTRY_DELETE
							&& pathSet.contains(child))
						removeWatch(child.toFile());
				} else if (kind == StandardWatchEventKinds.ENTRY_MODIFY)
					fireFileChanged(child.toFile(), FileWatchListener.MODIFIED);
				else if (kind == StandardWatchEventKinds.ENTRY_CREATE)
					fireFileChanged(child.toFile(), FileWatchListener.CREATED);
				else if (kind == StandardWatchEventKinds.ENTRY_DELETE)
					fireFileChanged(child.toFile(), FileWatchListener.DELETED);
			}

			boolean valid = key.reset();
			if (!valid)
				keyMap.remove(key);
			if (keyMap.isEmpty())
				break;
		}
	}

	private void fireFileChanged(File file, int kind) {
		if (file.isFile())
			for (FileWatchListener listener : listeners)
				listener.fileChanged(file, kind);
	}

	public long getFreeSpace(File rootFile) {
		return rootFile.getUsableSpace();
	}

}
