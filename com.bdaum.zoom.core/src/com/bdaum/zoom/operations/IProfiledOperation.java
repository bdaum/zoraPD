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

package com.bdaum.zoom.operations;

import org.eclipse.core.commands.operations.IUndoableOperation;

public interface IProfiledOperation extends IUndoableOperation {

	/**
	 * no conflict possibility
	 */
	static final int NONE = 0;
	/**
	 * catalog content conflict possible
	 */
	static final int CONTENT = 1;
	/**
	 * asset hierarchy conflict possible
	 */
	static final int HIERARCHY = 4;
	/**
	 * filesystem conflict possible
	 */
	static final int FILE = 8;
	/**
	 * XMP sidecar conflict possible
	 */
	static final int XMP = 16;
	/**
	 * catalog structure conflict possible
	 */
	static final int STRUCT = 32;
	/**
	 * catalog properties conflict possible
	 */
	static final int META = 64;
	/**
	 * Lucene index conflict possible
	 */
	static final int INDEX = 128;
	/**
	 * Track entries conflict possible
	 */
	static final int TRACK = 256;
	/**
	 * catalog maintenance conflict possible
	 */
	static final int CAT = 512;
	/**
	 * refresh conflict possible
	 */
	static final int SYNCHRONIZE = 1024;

	/**
	 * Returns the conflict possibilities for the operation execution
	 *
	 * @return conflict possibilities
	 */
	int getExecuteProfile();

	/**
	 * Returns the conflict possibilities for undoing the operation
	 *
	 * @return conflict possibilities
	 */
	int getUndoProfile();

	/**
	 * Returns the operation priority (@see org.eclipse.core.runtime.jobs.Job)
	 *
	 * @return priority
	 */
	int getPriority();

	/**
	 * Returns if a job runs silently. Silent jobs don't give an acoustic alarm
	 * when finished. At session end they are simply aborted without warning.
	 *
	 * @return true if job is silent
	 */
	boolean isSilent();

}
