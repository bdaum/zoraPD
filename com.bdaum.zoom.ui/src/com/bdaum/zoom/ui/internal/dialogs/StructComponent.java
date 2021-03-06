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
 * (c) 2009-2013 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import com.bdaum.aoModeling.runtime.IIdentifiableObject;
import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObject;
import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObjectImpl;
import com.bdaum.zoom.cat.model.creatorsContact.Contact;
import com.bdaum.zoom.cat.model.creatorsContact.ContactImpl;
import com.bdaum.zoom.cat.model.location.Location;
import com.bdaum.zoom.cat.model.location.LocationImpl;
import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.fileMonitor.internal.filefilter.WildCardFilter;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.ZViewerComparator;
import com.bdaum.zoom.ui.internal.widgets.ExpandCollapseGroup;
import com.bdaum.zoom.ui.internal.widgets.FilterField;
import com.bdaum.zoom.ui.internal.widgets.FlatGroup;

public class StructComponent implements Listener {

	public class FillJob extends Job {

		private final int type;
		private final IDbManager dbManager;
		private final Object value;

		public FillJob(IDbManager dbManager, int type, Object value) {
			super(Messages.StructComponent_fill_structured_component);
			this.dbManager = dbManager;
			this.type = type;
			this.value = value;
			setSystem(true);
			setPriority(Job.INTERACTIVE);
		}

		@Override
		public boolean belongsTo(Object family) {
			return family == StructComponent.this;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			final Control control = viewer.getControl();
			Display display = control.getDisplay();
			if (!control.isDisposed())
				display.asyncExec(() -> {
					if (!control.isDisposed())
						control.setCursor(display.getSystemCursor(SWT.CURSOR_WAIT));
				});
			switch (type) {
			case QueryField.T_LOCATION:
				objects = new ArrayList<IIdentifiableObject>(dbManager.obtainObjects(LocationImpl.class));
				break;
			case QueryField.T_CONTACT:
				objects = new ArrayList<IIdentifiableObject>(dbManager.obtainObjects(ContactImpl.class));
				break;
			case QueryField.T_OBJECT:
				objects = new ArrayList<IIdentifiableObject>(dbManager.obtainObjects(ArtworkOrObjectImpl.class));
				break;
			}
			if (!control.isDisposed())
				display.asyncExec(() -> {
					if (!control.isDisposed()) {
						viewer.setInput(objects);
						String expansions = settings.get(FLATEXPANSION + type);
						if (expansions != null) {
							Object[] chapters = new Object[expansions.length()];
							for (int i = 0; i < chapters.length; i++)
								chapters[i] = expansions.charAt(i);
							viewer.setExpandedElements(chapters);
						} else
							viewer.expandAll();
						if (value != null)
							viewer.setSelection(new StructuredSelection(value), true);
						control.setFocus();
						control.setCursor(display.getSystemCursor(SWT.ARROW));
					}
				});
			return Status.OK_STATUS;
		}
	}

	public class StructContentProvider implements ITreeContentProvider {

		private Map<Character, List<IIdentifiableObject>> chapters;

		public void dispose() {
			chapters = null;
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			chapters = null;
		}

		public Object[] getElements(Object inputElement) {
			@SuppressWarnings("unchecked")
			List<IIdentifiableObject> shownObjects = computeShownObjects((List<IIdentifiableObject>) inputElement);
			if (isFlat())
				return shownObjects.toArray();
			if (chapters == null) {
				chapters = new HashMap<Character, List<IIdentifiableObject>>();
				for (IIdentifiableObject ob : shownObjects) {
					String kw = labelProvider.getText(ob);
					if (!kw.isEmpty()) {
						Character chapterTitle = Character.toUpperCase(kw.charAt(0));
						List<IIdentifiableObject> elements = chapters.get(chapterTitle);
						if (elements == null)
							chapters.put(chapterTitle, elements = new ArrayList<IIdentifiableObject>());
						elements.add(ob);
					}
				}
			}
			return chapters.keySet().toArray();
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof Character) {
				List<IIdentifiableObject> elements = chapters.get(parentElement);
				if (elements != null)
					return elements.toArray();
			}
			return EMPTY;
		}

		public Object getParent(Object element) {
			if (!isFlat()) {
				String kw = labelProvider.getText(element);
				if (!kw.isEmpty()) {
					char firstChar = Character.toUpperCase(kw.charAt(0));
					for (Character title : chapters.keySet())
						if (title.charValue() == firstChar)
							return title;
				}
			}
			return null;
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}
	}

	private static final Object[] EMPTY = new Object[0];
	protected static final Object NO_DETAILS = Messages.StructComponent_no_details;
	private static final String FLATEXPANSION = "flatExpansion"; //$NON-NLS-1$

	private ExpandCollapseGroup expandCollapseGroup;
	private ZColumnLabelProvider labelProvider;
	private TreeViewer viewer;
	private List<IIdentifiableObject> objects = new ArrayList<IIdentifiableObject>(0);
	private final Map<String, Map<QueryField, Object>> structOverlayMap;
	private final FlatGroup radioGroup;
	private int type;
	private Object value;
	private IDbManager dbManager;
	private IDialogSettings settings;

	@SuppressWarnings("unused")
	public StructComponent(IDbManager dbManager, Composite comp, Object value, int type, boolean linesVisible,
			final Map<String, Map<QueryField, Object>> structOverlayMap, FlatGroup radioGroup, Set<String> usedObjects,
			int spareColumns, IDialogSettings settings) {
		this.dbManager = dbManager;
		this.value = value;
		this.type = type;
		this.settings = settings;
		comp.addListener(SWT.Dispose, this);
		this.structOverlayMap = structOverlayMap;
		this.radioGroup = radioGroup;
		Composite headerGroup = new Composite(comp, SWT.NONE);
		int span = ((GridLayout) comp.getLayout()).numColumns - spareColumns;
		GridData layoutData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		layoutData.horizontalSpan = span;
		headerGroup.setLayoutData(layoutData);
		GridLayout headerLayout = new GridLayout(2, false);
		headerLayout.marginHeight = headerLayout.marginWidth = 0;
		headerGroup.setLayout(headerLayout);
		for (int i = 0; i < spareColumns; i++)
			new Label(comp, SWT.NONE);
		final FilterField filterField = new FilterField(headerGroup);
		filterField.setLayoutData(new GridData(300, SWT.DEFAULT));
		filterField.addListener(SWT.Modify, this);
		expandCollapseGroup = new ExpandCollapseGroup(headerGroup, SWT.NONE);
		viewer = new TreeViewer(comp, SWT.BORDER | SWT.V_SCROLL | SWT.SINGLE);
		expandCollapseGroup.setViewer(viewer);
		expandCollapseGroup.setVisible(!isFlat());
		viewer.setContentProvider(new StructContentProvider());
		labelProvider = new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Character)
					return element.toString();
				String structText = getStructText(element, structOverlayMap);
				return (structText != null) ? structText : element.toString();
			}

			@Override
			protected Color getForeground(Object element) {
				if (element instanceof IIdentifiableObject && usedObjects != null
						&& !usedObjects.contains(((IIdentifiableObject) element).getStringId()))
					return viewer.getControl().getDisplay().getSystemColor(SWT.COLOR_RED);
				return super.getForeground(element);
			}
		};
		viewer.setLabelProvider(labelProvider);
		viewer.setComparator(ZViewerComparator.INSTANCE);
		UiUtilities.installDoubleClickExpansion(viewer);
		viewer.setFilters(new ViewerFilter[] { new ViewerFilter() {
			@Override
			public boolean select(Viewer aViewer, Object parentElement, Object element) {
				if (element instanceof Character)
					return true;
				WildCardFilter filter = filterField.getFilter();
				return filter == null || filter.accept(labelProvider.getText(element));
			}
		} });
		layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.heightHint = 200;
		layoutData.horizontalSpan = span;
		viewer.getControl().setLayoutData(layoutData);
		viewer.getTree().setLinesVisible(linesVisible);
	}

	public void fillValues() {
		new FillJob(dbManager, type, value).schedule();
	}

	private boolean isFlat() {
		return (type == QueryField.T_LOCATION) ? false : radioGroup.isFlat();
	}

	public static String getStructText(Object element, Map<String, Map<QueryField, Object>> structOverlayMap) {
		StringBuilder sb = new StringBuilder();
		if (element instanceof Location) {
			Location loc = (Location) element;
			String city = getUpdatedValue(loc, QueryField.LOCATION_CITY, loc.getCity(), structOverlayMap);
			append(sb, city);
			append(sb, getUpdatedValue(loc, QueryField.LOCATION_DETAILS, loc.getDetails(), structOverlayMap));
			String sublocation = getUpdatedValue(loc, QueryField.LOCATION_SUBLOCATION, loc.getSublocation(),
					structOverlayMap);
			if (sublocation != null && !sublocation.equals(city))
				append(sb, sublocation);
			append(sb, getUpdatedValue(loc, QueryField.LOCATION_STATE, loc.getProvinceOrState(), structOverlayMap));
			append(sb, getUpdatedValue(loc, QueryField.LOCATION_COUNTRYNAME, loc.getCountryName(), structOverlayMap));
			append(sb,
					getUpdatedValue(loc, QueryField.LOCATION_COUNTRYCODE, loc.getCountryISOCode(), structOverlayMap));
			append(sb, getUpdatedValue(loc, QueryField.LOCATION_WORLDREGION, loc.getWorldRegion(), structOverlayMap));
			Double latitude = getUpdatedValue(loc, QueryField.LOCATION_LATITUDE, loc.getLatitude(), structOverlayMap);
			if (latitude != null && !Double.isNaN(latitude))
				append(sb, Format.latitudeFormatter.format(latitude));
			Double longitude = getUpdatedValue(loc, QueryField.LOCATION_LONGITUDE, loc.getLongitude(),
					structOverlayMap);
			if (longitude != null && !Double.isNaN(longitude))
				append(sb, Format.longitudeFormatter.format(longitude));
			Double altitude = getUpdatedValue(loc, QueryField.LOCATION_ALTITUDE, loc.getAltitude(), structOverlayMap);
			if (altitude != null && !Double.isNaN(altitude))
				append(sb, Format.altitudeFormatter.format(altitude));
			append(sb, getUpdatedValue(loc, QueryField.LOCATION_PLUSCODE, loc.getPlusCode(), structOverlayMap));
			if (sb.length() == 0)
				sb.append(NO_DETAILS);
			return sb.toString();
		}
		if (element instanceof Contact) {
			Contact contact = (Contact) element;
			append(sb, getUpdatedValue(contact, QueryField.CONTACT_ADDRESS, contact.getAddress(), structOverlayMap));
			append(sb, getUpdatedValue(contact, QueryField.CONTACT_CITY, contact.getCity(), structOverlayMap));
			append(sb, getUpdatedValue(contact, QueryField.CONTACT_COUNTRY, contact.getCountry(), structOverlayMap));
			append(sb,
					getUpdatedValue(contact, QueryField.CONTACT_POSTALCODE, contact.getPostalCode(), structOverlayMap));
			append(sb, getUpdatedValue(contact, QueryField.CONTACT_EMAIL, contact.getEmail(), structOverlayMap));
			append(sb, getUpdatedValue(contact, QueryField.CONTACT_PHONE, contact.getPhone(), structOverlayMap));
			append(sb, getUpdatedValue(contact, QueryField.CONTACT_WEBURL, contact.getWebUrl(), structOverlayMap));
			if (sb.length() == 0)
				sb.append(NO_DETAILS);
			return sb.toString();
		}
		if (element instanceof ArtworkOrObject) {
			ArtworkOrObject art = (ArtworkOrObject) element;
			append(sb, getUpdatedValue(art, QueryField.ARTWORKOROBJECT_TITLE, art.getTitle(), structOverlayMap));
			append(sb, getUpdatedValue(art, QueryField.ARTWORKOROBJECT_CREATOR, art.getCreator(), structOverlayMap));
			Date dateCreated = getUpdatedValue(art, QueryField.ARTWORKOROBJECT_DATECREATED, art.getDateCreated(),
					structOverlayMap);
			if (dateCreated != null)
				append(sb, Format.dayFormatter.format(dateCreated));
			append(sb, getUpdatedValue(art, QueryField.ARTWORKOROBJECT_SOURCE, art.getSource(), structOverlayMap));
			append(sb, getUpdatedValue(art, QueryField.ARTWORKOROBJECT_INVENTORYNUMBER, art.getSourceInventoryNumber(),
					structOverlayMap));
			append(sb, getUpdatedValue(art, QueryField.ARTWORKOROBJECT_COPYRIGHT, art.getCopyrightNotice(),
					structOverlayMap));
			if (sb.length() == 0)
				sb.append(NO_DETAILS);
			return sb.toString();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private static <T> T getUpdatedValue(IIdentifiableObject obj, QueryField qf, T dflt,
			Map<String, Map<QueryField, Object>> structOverlayMap) {
		if (structOverlayMap != null) {
			Map<QueryField, Object> fieldMap = structOverlayMap.get(obj.getStringId());
			if (fieldMap != null && fieldMap.containsKey(qf))
				return (T) fieldMap.get(qf);
		}
		return dflt;
	}

	private static void append(StringBuilder sb, String[] s) {
		if (s != null && s.length > 0) {
			if (sb.length() > 0)
				sb.append("; "); //$NON-NLS-1$
			for (int i = 0; i < s.length; i++) {
				if (i > 0)
					sb.append(", "); //$NON-NLS-1$
				sb.append(s[i]);
			}
		}
	}

	private static void append(StringBuilder sb, String s) {
		if (s != null && !s.isEmpty()) {
			if (sb.length() > 0)
				sb.append("; "); //$NON-NLS-1$
			sb.append(s);
		}
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		viewer.addSelectionChangedListener(listener);
	}

	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		viewer.removeSelectionChangedListener(listener);
	}

	public IIdentifiableObject getSelectedElement() {
		Object firstElement = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
		return firstElement instanceof IIdentifiableObject ? (IIdentifiableObject) firstElement : null;
	}

	public void addDoubleClickListener(IDoubleClickListener listener) {
		viewer.addDoubleClickListener(listener);
	}

	public void add(IIdentifiableObject element) {
		objects.add(element);
		update();
		viewer.setSelection(new StructuredSelection(element), true);
	}

	protected void update() {
		BusyIndicator.showWhile(viewer.getControl().getDisplay(), () -> {
			ISelection selection = viewer.getSelection();
			if (isFlat())
				viewer.setInput(objects);
			else {
				Object[] expandedElements = viewer.getExpandedElements();
				viewer.setInput(objects);
				viewer.setExpandedElements(expandedElements);
			}
			viewer.setSelection(selection, true);
			expandCollapseGroup.setVisible(!isFlat());
		});
	}

	public void remove(IdentifiableObject element) {
		objects.remove(element);
		viewer.remove(element);
	}

	public void removeAll(List<IIdentifiableObject> removals) {
		objects.removeAll(removals);
		update();
	}

	public void update(Object element, String[] properties) {
		viewer.update(element, properties);
	}

	public void setSelection(StructuredSelection selection) {
		viewer.setSelection(selection);
	}

	private List<IIdentifiableObject> computeShownObjects(List<IIdentifiableObject> objects) {
		List<IIdentifiableObject> shownObjects = new ArrayList<IIdentifiableObject>(objects.size());
		for (IIdentifiableObject obj : objects) {
			String id = obj.getStringId();
			if (structOverlayMap == null || !structOverlayMap.containsKey(id) || structOverlayMap.get(id) != null)
				shownObjects.add(obj);
		}
		return shownObjects;
	}

	public void handleEvent(Event e) {
		if (e.type == SWT.Dispose)
			Job.getJobManager().cancel(this);
		else
			update();
	}

	public List<IIdentifiableObject> getObjects() {
		return objects;
	}

	public void saveSettings() {
		if (!isFlat()) {
			StringBuilder sb = new StringBuilder();
			for (Object obj : viewer.getExpandedElements())
				sb.append(obj);
			settings.put(FLATEXPANSION + type, sb.toString());
		}
	}

}
