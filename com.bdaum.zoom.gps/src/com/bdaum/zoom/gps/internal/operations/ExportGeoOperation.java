package com.bdaum.zoom.gps.internal.operations;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.gps.internal.GpsActivator;
import com.bdaum.zoom.gps.internal.wizards.AbstractGeoExportWizard;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.ui.internal.UiUtilities;

@SuppressWarnings("restriction")
public class ExportGeoOperation extends DbOperation {

	private List<Asset> assets;
	private File targetFile;
	private String type;

	public ExportGeoOperation(List<Asset> assets, File targetFile, String type) {
		super(NLS.bind(Messages.getString("ExportGeoOperation.export_file"), type.toUpperCase())); //$NON-NLS-1$
		this.assets = assets;
		this.targetFile = targetFile;
		this.type = type;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		monitor.beginTask(NLS.bind(Messages.getString("ExportGeoOperation.export_file"), type.toUpperCase()), assets.size()); //$NON-NLS-1$
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
		nf.setGroupingUsed(false);
		SimpleDateFormat sdf = Format.XML_DATE_TIME_XZONED_FORMAT.get(); 
		nf.setMaximumFractionDigits(6);
		try (Writer writer = new FileWriter(targetFile)) {
			StringBuilder sb = new StringBuilder();
			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //$NON-NLS-1$
			if (type == AbstractGeoExportWizard.GPX)
				writer.write(NLS.bind("<gpx version=\"1.1\" creator=\"{0}\">\n", Constants.APPLICATION_NAME)); //$NON-NLS-1$
			else
				writer.write("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n<Document>\n"); //$NON-NLS-1$
			for (Asset asset : assets) {
				sb.setLength(0);
				double lat = asset.getGPSLatitude();
				double lon = asset.getGPSLongitude();
				double ele = asset.getGPSAltitude();
				String name = UiUtilities.createSlideTitle(asset);
				if (type == AbstractGeoExportWizard.GPX) {
					sb.append("<wpt lat=\"").append(nf.format(lat)).append("\" lon=\"").append(nf.format(lon)) //$NON-NLS-1$ //$NON-NLS-2$
							.append("\">"); //$NON-NLS-1$
					if (!Double.isNaN(ele))
						sb.append("<ele>").append(nf.format(ele)).append("</ele>"); //$NON-NLS-1$ //$NON-NLS-2$
					Date date = asset.getDateTime();
					if (date != null)
						sb.append("<time>").append(sdf.format(date)).append("</time>"); //$NON-NLS-1$ //$NON-NLS-2$
					if (name != null && !name.isEmpty())
						sb.append("<name>").append(BatchUtilities.encodeXML(name, 1)).append("</name>"); //$NON-NLS-1$ //$NON-NLS-2$
					sb.append("</wpt>\n"); //$NON-NLS-1$
				} else {
					sb.append("<Placemark>"); //$NON-NLS-1$
					if (name != null && !name.isEmpty())
						sb.append("<name>").append(BatchUtilities.encodeXML(name, 1)).append("</name>"); //$NON-NLS-1$ //$NON-NLS-2$
					String description = asset.getImageDescription();
					if (description != null && !description.isEmpty())
						sb.append("<description>").append(BatchUtilities.encodeXML(description, 1)) //$NON-NLS-1$
								.append("</description>"); //$NON-NLS-1$
					sb.append("<Point>"); //$NON-NLS-1$
					sb.append("<coordinates>"); //$NON-NLS-1$
					sb.append(nf.format(lon)).append(',').append(nf.format(lat));
					if (!Double.isNaN(ele))
						sb.append(',').append(nf.format(ele));
					sb.append("</coordinates>"); //$NON-NLS-1$
					sb.append("</Point>"); //$NON-NLS-1$
					sb.append("</Placemark>\n"); //$NON-NLS-1$
				}
				writer.write(sb.toString());
				monitor.worked(1);
			}
			if (type == AbstractGeoExportWizard.GPX)
				writer.write("</gpx>\n"); //$NON-NLS-1$
			else
				writer.write("</Document>\n</kml>\n"); //$NON-NLS-1$
		} catch (IOException e) {
			return new Status(IStatus.ERROR, GpsActivator.PLUGIN_ID,
					NLS.bind(Messages.getString("ExportGeoOperation.error_writing"), type.toUpperCase()), e); //$NON-NLS-1$
		}
		monitor.done();
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return null;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return null;
	}

	@Override
	public boolean canRedo() {
		return false;
	}

	@Override
	public boolean canUndo() {
		return false;
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
