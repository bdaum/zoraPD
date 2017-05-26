/* Copyright 2009 Berthold Daum

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.bdaum.zoom.net.core.ftp;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.net.core.ftp.messages"; //$NON-NLS-1$
	public static String FtpAccount_bad_url;
	public static String FtpAccount_cannot_change_to_working_dir;
	public static String FtpAccount_cannot_replace_file_with_subdir;
	public static String FtpAccount_cannot_replace_subdir_with_file;
	public static String FtpAccount_creation_of_subdir_failed;
	public static String FtpAccount_directory_creation_failed;
	public static String FtpAccount_directory_does_not_exist;
	public static String FtpAccount_file_already_exists;
	public static String FtpAccount_file_exists_overwrite;
	public static String FtpAccount_ftp_server_refused;
	public static String FtpAccount_overwrite;
	public static String FtpAccount_overwrite_all;
	public static String FtpAccount_skip_all;
	public static String FtpAccount_specified_dir_does_not_exist;
	public static String FtpAccount_target_dir_does_not_exist;
	public static String FtpAccount_uploading_files;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
