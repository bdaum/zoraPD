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
 * (c) 2009 Berthold Daum  
 */
package com.bdaum.zoom.gps.internal.dialogs;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.gps.internal.GpsActivator;
import com.bdaum.zoom.gps.internal.HelpContextIds;
import com.bdaum.zoom.gps.internal.IMapComponent;
import com.bdaum.zoom.gps.internal.views.Mapdata;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.gps.Trackpoint;
import com.bdaum.zoom.ui.widgets.DateInput;

public class TrackpointDialog extends ZTitleAreaDialog {

	public class EditDialog extends ZTitleAreaDialog implements Listener {

		private final SubTrack track;
		private final boolean split;
		private final String message;
		private DateInput startField;
		private DateInput endField;
		private Date start;
		private Date end;
		private final String title;
		private final List<SubTrack> subtracks;

		public EditDialog(Shell shell, List<SubTrack> subtracks, SubTrack track, boolean split, String title,
				String message) {
			super(shell);
			this.subtracks = subtracks;
			this.track = track;
			this.split = split;
			this.title = title;
			this.message = message;
		}

		@Override
		public void create() {
			super.create();
			getShell().setText(Constants.APPLICATION_NAME);
			setTitle(title);
			setMessage(message);
			fillValues();
			validate();
			getShell().pack();
		}

		private void fillValues() {
			if (split) {
				long median = (track.getStart() + track.getEnd()) / 2;
				long dur = (track.getEnd() - track.getStart()) / 6;
				startField.setDate(new Date(median - dur));
				endField.setDate(new Date(median + dur));
			} else {
				startField.setDate(new Date(track.getStart()));
				endField.setDate(new Date(track.getEnd()));
			}
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite area = (Composite) super.createDialogArea(parent);
			Composite composite = new Composite(area, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			composite.setLayout(new GridLayout(2, false));
			new Label(composite, SWT.NONE).setText(Messages.TrackpointDialog_start);
			startField = new DateInput(composite, SWT.DATE | SWT.TIME | SWT.MEDIUM);
			startField.addListener(SWT.Selection, this);
			new Label(composite, SWT.NONE).setText(Messages.TrackpointDialog_end);
			endField = new DateInput(composite, SWT.DATE | SWT.TIME | SWT.MEDIUM);
			endField.addListener(SWT.Selection, this);
			return area;
		}

		private void validate() {
			String errorMessage = null;
			long s = startField.getDate().getTime();
			long e = endField.getDate().getTime();
			if (s >= e)
				errorMessage = Messages.TrackpointDialog_end_after_start;
			else if (split) {
				if (s <= track.getStart())
					errorMessage = NLS.bind(split ? Messages.TrackpointDialog_wrong_gap_start_value
							: Messages.TrackpointDialog_start_after_x, formatDate(track.getStart()));
				else if (e >= track.getEnd())
					errorMessage = NLS.bind(split ? Messages.TrackpointDialog_wrong_gap_end_value
							: Messages.TrackpointDialog_end_before_x, formatDate(track.getEnd()));
			} else
				for (SubTrack t : subtracks)
					if (s >= t.getStart() && s < t.getEnd() || e >= t.getStart() && e < t.getEnd()) {
						errorMessage = NLS.bind(Messages.TrackpointDialog_subtrack_overlaps, formatTrack(t));
						break;
					}
			setErrorMessage(errorMessage);
			getButton(OK).setEnabled(errorMessage == null);
		}

		public long getStart() {
			return start.getTime();
		}

		public long getEnd() {
			return end.getTime();
		}

		public void handleEvent(Event e) {
			validate();
		}

		@Override
		protected void okPressed() {
			start = startField.getDate();
			end = endField.getDate();
			super.okPressed();
		}

	}

	public class SubTrack {
		private long start, end;

		public SubTrack(long time) {
			start = time;
		}

		/**
		 * @return start
		 */
		public long getStart() {
			return start;
		}

		/**
		 * @param start
		 *            das zu setzende Objekt start
		 */
		public void setStart(long start) {
			this.start = start;
		}

		/**
		 * @return end
		 */
		public long getEnd() {
			return end;
		}

		/**
		 * @param end
		 *            das zu setzende Objekt end
		 */
		public void setEnd(long end) {
			this.end = end;
		}
	}

	private static final long ONEMINUTE = 60000L;
	private final List<SubTrack> subtracks = new LinkedList<TrackpointDialog.SubTrack>();
	private TableViewer viewer;
	private Button editButton;
	private Button removeButton;
	private Button joinButton;
	private Button splitButton;
	private final Trackpoint[] trackpoints;
	private List<Trackpoint> result;
	private IMapComponent mapComponent;
	private final long tolerance;

	public TrackpointDialog(Shell shell, Trackpoint[] trackpoints, long tolerance) {
		super(shell, HelpContextIds.TRACKPOINTS);
		this.trackpoints = trackpoints;
		this.tolerance = tolerance;
		SubTrack subtrack = null;
		Trackpoint previous = null;
		for (Trackpoint trackpoint : trackpoints) {
			if (previous == null || subtrack == null)
				subtrack = new SubTrack(trackpoint.getTime() - tolerance);
			else if (previous.isSegmentEnd() || trackpoint.getTime() - previous.getTime() > 2 * tolerance) {
				subtrack.setEnd(previous.getTime() + tolerance);
				subtracks.add(subtrack);
				subtrack = new SubTrack(trackpoint.getTime() - tolerance);
			}
			previous = trackpoint;
		}
		if (subtrack != null && previous != null) {
			subtrack.setEnd(previous.getTime() + tolerance);
			subtracks.add(subtrack);
		}
	}

	@Override
	public void create() {
		super.create();
		getShell().setText(Constants.APPLICATION_NAME);
		setTitle(Messages.TrackpointDialog_edit_trackpoints);
		setMessage(NLS.bind(Messages.TrackpointDialog_initial_message, formatTime(tolerance / ONEMINUTE)));
		updateButtons();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite comp = new Composite(area, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout(3, false));
		createViewer(comp);
		createButtons(comp);
		createMapArea(comp);
		return area;
	}

	private void createMapArea(Composite comp) {
		mapComponent = GpsActivator.getMapComponent(GpsActivator.findCurrentMappingSystem());
		if (mapComponent != null) {
			mapComponent.createComponent(comp, false);
			GridData layoutData = new GridData(GridData.FILL_BOTH);
			layoutData.widthHint = layoutData.heightHint = 500;
			mapComponent.getControl().setLayoutData(layoutData);
			mapComponent.setInput(null, 12, IMapComponent.BLANK);
		}
	}

	private void createButtons(Composite comp) {
		Composite composite = new Composite(comp, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(1, false));
		editButton = new Button(composite, SWT.PUSH);
		editButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		editButton.setText(Messages.TrackpointDialog_edit);
		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SubTrack track = (SubTrack) viewer.getStructuredSelection().getFirstElement();
				EditDialog dialog = new EditDialog(getShell(), subtracks, track, false,
						Messages.TrackpointDialog_edit_subtrack,
						NLS.bind(Messages.TrackpointDialog_modify_start_end, formatTrack(track)));
				if (dialog.open() == EditDialog.OK) {
					track.setStart(dialog.getStart());
					track.setEnd(dialog.getEnd());
					viewer.update(track, null);
				}
			}
		});
		removeButton = new Button(composite, SWT.PUSH);
		removeButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		removeButton.setText(Messages.TrackpointDialog_remove);
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Iterator<?> it = viewer.getStructuredSelection().iterator();
				while (it.hasNext()) {
					Object next = it.next();
					subtracks.remove(next);
					viewer.remove(next);
				}
			}
		});
		joinButton = new Button(composite, SWT.PUSH);
		joinButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		joinButton.setText(Messages.TrackpointDialog_join);
		joinButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SubTrack first = null;
				Iterator<?> it = viewer.getStructuredSelection().iterator();
				while (it.hasNext()) {
					SubTrack t = (SubTrack) it.next();
					if (first == null)
						first = t;
					else {
						first.setEnd(t.getEnd());
						subtracks.remove(t);
						viewer.remove(t);
					}
				}
				if (first != null)
					viewer.update(first, null);
			}
		});
		splitButton = new Button(composite, SWT.PUSH);
		splitButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		splitButton.setText(Messages.TrackpointDialog_split);
		splitButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SubTrack track = (SubTrack) viewer.getStructuredSelection().getFirstElement();
				EditDialog dialog = new EditDialog(getShell(), subtracks, track, true,
						Messages.TrackpointDialog_spli_subtrack,
						NLS.bind(Messages.TrackpointDialog_split_subtrack_msg, formatTrack(track)));
				if (dialog.open() == EditDialog.OK) {
					int index = subtracks.indexOf(track);
					SubTrack newTrack = new SubTrack(dialog.getEnd());
					newTrack.setEnd(track.getEnd());
					track.setEnd(dialog.getStart());
					subtracks.add(index + 1, newTrack);
					viewer.setInput(subtracks);
					viewer.setSelection(new StructuredSelection(new Object[] { track, newTrack }));
				}
			}
		});
	}

	public void createViewer(Composite comp) {
		viewer = new TableViewer(comp, SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER | SWT.MULTI);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.widthHint = 410;
		layoutData.heightHint = 500;
		viewer.getControl().setLayoutData(layoutData);
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		TableViewerColumn col1 = new TableViewerColumn(viewer, SWT.NONE);
		col1.getColumn().setWidth(170);
		col1.getColumn().setText(Messages.TrackpointDialog_start);
		col1.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof SubTrack)
					return formatDate(((SubTrack) element).getStart());
				return super.getText(element);
			}
		});
		TableViewerColumn col2 = new TableViewerColumn(viewer, SWT.NONE);
		col2.getColumn().setWidth(170);
		col2.getColumn().setText(Messages.TrackpointDialog_end);
		col2.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof SubTrack)
					return formatDate(((SubTrack) element).getEnd());
				return super.getText(element);
			}
		});
		TableViewerColumn col3 = new TableViewerColumn(viewer, SWT.NONE);
		col3.getColumn().setWidth(70);
		col3.getColumn().setAlignment(SWT.RIGHT);
		col3.getColumn().setText(Messages.TrackpointDialog_duration);
		col3.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof SubTrack) {
					SubTrack subTrack = (SubTrack) element;
					return formatTime((subTrack.getEnd() - subTrack.getStart() + 30000L) / ONEMINUTE);
				}
				return super.getText(element);
			}
		});
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateButtons();
				updateMap();
			}
		});
		viewer.setInput(subtracks);
	}

	protected void updateMap() {
		IStructuredSelection selection = viewer.getStructuredSelection();
		double minLat = Double.MAX_VALUE, maxLat = Double.MIN_VALUE, minLng = Double.MAX_VALUE,
				maxLng = Double.MIN_VALUE;
		Object[] tracks = selection.toArray();
		LinkedList<Trackpoint> pnts = new LinkedList<Trackpoint>();
		int start = 0;
		for (Trackpoint trackpoint : trackpoints) {
			long time = trackpoint.getTime();
			for (int i = start; i < tracks.length; i++)
				if (time >= ((SubTrack) tracks[i]).getStart()) {
					start = i;
					if (time <= ((SubTrack) tracks[i]).getEnd()) {
						pnts.add(trackpoint);
						double latitude = trackpoint.getLatitude();
						double longitude = trackpoint.getLongitude();
						if (latitude > maxLat)
							maxLat = latitude;
						if (latitude < minLat)
							minLat = latitude;
						if (longitude > maxLng)
							maxLng = longitude;
						if (longitude < minLng)
							minLng = longitude;
						break;
					}
				}
		}
		double latDist = Math.abs(maxLat - minLat) / 100;
		double lngDist = Math.abs(maxLng - minLng) / 100;
		boolean done = false;
		while (!done) {
			done = true;
			Trackpoint previous = null;
			Iterator<Trackpoint> it = pnts.iterator();
			while (it.hasNext()) {
				Trackpoint pnt = it.next();
				if (previous != null && Math.abs(previous.getLatitude() - pnt.getLatitude()) < latDist
						&& Math.abs(previous.getLongitude() - pnt.getLongitude()) < lngDist) {
					it.remove();
					done = false;
					continue;
				}
				previous = pnt;
			}
		}
		mapComponent.setInput(new Mapdata(null, pnts.toArray(new Trackpoint[pnts.size()]), false), 12,
				IMapComponent.TRACK);
	}

	protected void updateButtons() {
		IStructuredSelection selection = viewer.getStructuredSelection();
		int size = selection.size();
		boolean single = size == 1;
		boolean any = size > 0;
		editButton.setEnabled(single);
		removeButton.setEnabled(any);
		int previous = -1;
		boolean join = false;
		if (size > 1) {
			join = true;
			Iterator<?> it = selection.iterator();
			while (it.hasNext()) {
				if (previous >= 0 && subtracks.indexOf(it.next()) - previous > 1) {
					join = false;
					break;
				}
				previous = subtracks.indexOf(it.next());
			}
		}
		joinButton.setEnabled(join);
		splitButton.setEnabled(single);
	}

	private static String formatDate(long time) {
		return Format.EMDY_TIME_FORMAT.get().format(new Date(time));
	}

	private static String formatTime(long minutes) {
		long hours = minutes / 60;
		minutes -= hours * 60;
		StringBuilder sb = new StringBuilder();
		sb.append(hours).append('h');
		if (minutes < 10)
			sb.append('0');
		sb.append(minutes).append('m');
		return sb.toString();
	}

	private String formatTrack(SubTrack track) {
		return NLS.bind("{0} - {1}", formatDate(track.getStart()), //$NON-NLS-1$
				formatDate(track.getEnd()));
	}

	@Override
	protected void okPressed() {
		SubTrack[] tracks = subtracks.toArray(new SubTrack[subtracks.size()]);
		int start = 0;
		result = new ArrayList<Trackpoint>(trackpoints.length);
		for (Trackpoint trackpoint : trackpoints) {
			long time = trackpoint.getTime();
			for (int i = start; i < tracks.length; i++)
				if (time >= tracks[i].getStart()) {
					start = i;
					if (time <= tracks[i].getEnd()) {
						trackpoint.setMinTime(tracks[i].getStart());
						trackpoint.setMaxTime(tracks[i].getEnd());
						result.add(trackpoint);
						break;
					}
				}
		}
		super.okPressed();
	}

	public Trackpoint[] getResult() {
		return result.toArray(new Trackpoint[result.size()]);
	}

}
