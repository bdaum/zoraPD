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
 * (c) 2012 Berthold Daum  
 */

package com.bdaum.zoom.csv.internal.operations;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.csv.internal.CsvActivator;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;

public class ExportCsvOperation extends DbOperation {

	private List<Asset> assets;
	private final Set<QueryField> xmpFilter;
	private final File targetFile;
	private final boolean firstLine;
	private final Map<String, String> relabelMap;

	public ExportCsvOperation(List<Asset> assets, Set<QueryField> xmpFilter,
			Map<String, String> relabelMap, File targetFile, boolean firstLine) {
		super(Messages.ExportCsvOperation_export_values);
		this.assets = assets;
		this.xmpFilter = xmpFilter;
		this.relabelMap = relabelMap;
		this.targetFile = targetFile;
		this.firstLine = firstLine;
	}

	@Override
	public boolean isSilent() {
		return false;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, final IAdaptable info)
			throws ExecutionException {
		monitor.beginTask(Messages.ExportCsvOperation_export_csv, assets.size());
		try (Writer writer = new FileWriter(targetFile)) {
			QueryField[] fields = xmpFilter.toArray(new QueryField[xmpFilter
					.size()]);
			Arrays.sort(fields, new Comparator<QueryField>() {

				@Override
				public int compare(QueryField q1, QueryField q2) {
					String lab1 = q1.getLabel();
					String newLabel = relabelMap.get(lab1);
					if (newLabel != null)
						lab1 = newLabel;
					String lab2 = q2.getLabel();
					newLabel = relabelMap.get(lab2);
					if (newLabel != null)
						lab2 = newLabel;
					return lab1.compareTo(lab2);
				}
			});
			if (firstLine) {
				StringBuilder sb = new StringBuilder();
				for (QueryField qfield : fields) {
					String lab = qfield.getLabel();
					if (qfield.isStruct() && qfield.getCard() == 1) {
						QueryField[] children = null;
						switch (qfield.getType()) {
						case QueryField.T_CONTACT:
							children = QueryField.CONTACT_TYPE.getChildren();
							break;
						case QueryField.T_LOCATION:
							children = QueryField.LOCATION_TYPE.getChildren();
							break;
						}
						if (children != null)
							for (QueryField detailField : children)
								addValue(sb, relabel(lab + ' ' + detailField.getLabel()));
					} else
						addValue(sb, relabel(lab));
				}
				sb.append('\n');
				writer.write(sb.toString());
			}
			Locale dfltLocale = Locale.getDefault();
			IDbManager dbManager = Core.getCore().getDbManager();
			StringBuilder sb = new StringBuilder();
			for (Asset asset : assets) {
				sb.setLength(0);
				for (QueryField qfield : fields) {
					if (qfield.isStruct() && qfield.getCard() == 1) {
						Object struct = null;
						QueryField[] children = null;
						switch (qfield.getType()) {
						case QueryField.T_CONTACT:
							children = QueryField.CONTACT_TYPE.getChildren();
							struct = qfield.getStruct(asset);
							break;
						case QueryField.T_LOCATION:
							children = QueryField.LOCATION_TYPE.getChildren();
							struct = qfield.getStruct(asset);
							break;
						}
						if (struct instanceof String)
							struct = dbManager.obtainById(
									IdentifiableObject.class, (String) struct);
						if (children != null)
							for (QueryField detailField : children)
								if (struct != null)
									addValue(
											sb,
											detailField.value2text(
													detailField
															.obtainPlainFieldValue(struct),
													"", true, false, dfltLocale)); //$NON-NLS-1$
								else
									addValue(sb, ""); //$NON-NLS-1$
					} else
						addValue(sb, qfield.value2text(
								qfield.obtainFieldValue(asset),
								"", //$NON-NLS-1$
								qfield != QueryField.RATING
										&& qfield != QueryField.IPTC_URGENCY,
								false, dfltLocale));
				}
				sb.append('\n');
				writer.write(sb.toString());
				monitor.worked(1);
			}
		} catch (IOException e) {
			return new Status(IStatus.ERROR, CsvActivator.PLUGIN_ID,
					Messages.ExportCsvOperation_error_writing_csv, e);
		}
		monitor.done();
		return Status.OK_STATUS;
	}

	private String relabel(String lab) {
		if (relabelMap != null) {
			String newLabel = relabelMap.get(lab);
			if (newLabel != null)
				return newLabel;
		}
		return lab;
	}

	private static void addValue(StringBuilder sb, String value) {
		if (value == Format.MISSINGENTRYSTRING)
			value = ""; //$NON-NLS-1$
		else if (value.indexOf(',') >= 0)
			value = '"' + value + '"';
		if (sb.length() > 0)
			sb.append(',');
		sb.append(value);
	}

	@Override
	public boolean canRedo() {
		return false;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return null;
	}

	@Override
	public boolean canUndo() {
		return false;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return null;
	}

	public int getExecuteProfile() {
		return IProfiledOperation.CONTENT;
	}

	public int getUndoProfile() {
		return 0;
	}

	@Override
	public int getPriority() {
		return assets.size() > 10 ? Job.LONG : Job.SHORT;
	}

}
