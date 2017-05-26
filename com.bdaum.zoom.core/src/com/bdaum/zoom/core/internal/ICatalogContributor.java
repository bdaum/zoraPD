package com.bdaum.zoom.core.internal;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import com.bdaum.zoom.core.db.IDbManager;

public interface ICatalogContributor {

	void merge(IProgressMonitor monitor, IDbManager dbManager,
			IDbManager externalDb, int duplicatePolicy,
			Map<String, String> idMap);

	void split(IProgressMonitor monitor, IDbManager dbManager,
			IDbManager newDbManager);

}
