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

package com.bdaum.zoom.net.core.job;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.job.CustomJob;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.net.core.ftp.FtpAccount;
import com.bdaum.zoom.net.core.internal.Activator;

public class TransferJob extends CustomJob {

	private final File[] files;
	private final FtpAccount account;
	private IAdaptable adaptable;
	private boolean deleteTransferred;
	private static final ISchedulingRule rule = new ISchedulingRule() {

		public boolean isConflicting(ISchedulingRule r) {
			return r == this;
		}

		public boolean contains(ISchedulingRule r) {
			return r == this;
		}
	};

	public TransferJob(File[] files, FtpAccount account, boolean deleteTransferred) {
		super(Messages.TransferJob_transfer_files_to_server);
		this.deleteTransferred = deleteTransferred;
		setPriority(LONG);
		setRule(rule);
		this.files = files;
		this.account = account;
		adaptable = new IAdaptable() {

			@SuppressWarnings({ "rawtypes", "unchecked" })
			public Object getAdapter(Class adapter) {
				if (Shell.class.equals(adapter))
					return PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell();
				return null;
			}
		};
	}

	@Override
	public boolean belongsTo(Object family) {
		return Constants.OPERATIONJOBFAMILY == family || Constants.CRITICAL == family;
	}

	@Override
	protected IStatus runJob(IProgressMonitor monitor) {
		long startTime = System.currentTimeMillis();
		MultiStatus status = new MultiStatus(Activator.PLUGIN_ID, 0, Messages.TransferJob_ftp_transfer, null);
		try {
			int transferred = account.transferFiles(files, monitor, adaptable, deleteTransferred);
			String msg = (transferred < 0) ? NLS.bind(Messages.TransferJob_transfer_aborted, account.getName())
					: NLS.bind(Messages.TransferJob_n_files_transferred, transferred, account.getName());
			Activator.getDefault().getLog().log(new Status(IStatus.INFO, Activator.PLUGIN_ID, msg));
			if (transferred >= 0)
				OperationJob.signalJobEnd(startTime);
			if (account.getWebHost() != null && !account.getWebHost().isEmpty()) {
				Shell shell = adaptable.getAdapter(Shell.class);
				if (shell != null && !shell.isDisposed())
					shell.getDisplay().asyncExec(() -> {
						if (!shell.isDisposed())
						account.testWebUrl();
					});
			}
		} catch (IOException e) {
			status.add(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.TransferJob_error_during_file_transfer));
		}
		return status;
	}

}
