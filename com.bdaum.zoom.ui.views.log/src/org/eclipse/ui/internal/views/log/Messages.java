/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jacek Pospychala <jacek.pospychala@pl.ibm.com> - bugs 202583, 207344
 *     Benjamin Cabe <benjamin.cabe@anyware-tech.com> - bug 218648
 *******************************************************************************/
package org.eclipse.ui.internal.views.log;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	public static String LogView_column_message;
	public static String LogView_column_date;
	public static String LogView_copy;
	public static String LogView_delete;
	public static String LogView_delete_tooltip;
	public static String LogView_details;
	public static String LogView_export;
	public static String LogView_exportLog;
	public static String LogView_export_tooltip;
	public static String LogView_exportEntry;
	public static String LogView_exportLogEntry;
	public static String LogView_exportEntry_tooltip;
	public static String LogView_filter;

	public static String LogView_SessionStarted;
	public static String LogView_severity_error;
	public static String LogView_severity_warning;
	public static String LogView_severity_info;
	public static String LogView_severity_ok;
	public static String LogView_starting_at;
	public static String LogView_confirmDelete_title;
	public static String LogView_confirmDelete_message;
	public static String LogView_confirmOverwrite_message;
	public static String LogView_operation_importing;
	public static String LogView_operation_reloading;
	public static String LogView_activate;
	public static String LogView_AddingBatchedEvents;

	public static String LogView_FileCouldNotBeFound;
	public static String LogView_FilterDialog_title;
	public static String LogView_FilterDialog_eventTypes;
	public static String LogView_FilterDialog_information;
	public static String LogView_FilterDialog_warning;
	public static String LogView_FilterDialog_error;
	public static String LogView_FilterDialog_limitTo;
	public static String LogView_FilterDialog_eventsLogged;
	public static String LogView_FilterDialog_allSessions;
	public static String LogView_FilterDialog_ok;
	public static String LogView_FilterDialog_recentSession;
	public static String LogView_OpenFile;

	public static String LogViewLabelProvider_Session;
	public static String LogViewLabelProvider_truncatedMessage;

	public static String EventDetailsDialog_date;
	public static String EventDetailsDialog_severity;
	public static String EventDetailsDialog_message;
	public static String EventDetailsDialog_exception;
	public static String EventDetailsDialog_session;
	public static String EventDetailsDialog_noStack;


	public static String OpenLogDialog_message;


	private static final String BUNDLE_NAME = "org.eclipse.ui.internal.views.log.messages"; //$NON-NLS-1$

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

}
