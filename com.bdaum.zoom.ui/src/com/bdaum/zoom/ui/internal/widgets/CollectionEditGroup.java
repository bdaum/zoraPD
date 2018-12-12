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
 * (c) 2011-2017 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.widgets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.Criterion;
import com.bdaum.zoom.cat.model.group.CriterionImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.SortCriterion;
import com.bdaum.zoom.cat.model.group.SortCriterionImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.ui.internal.dialogs.Messages;

public class CollectionEditGroup {

	public class PrepareJob extends Job implements DisposeListener {

		private Control control;

		public PrepareJob(Control control) {
			super(Messages.CollectionEditDialog_prepare_field_values);
			this.control = control;
			setSystem(true);
			setPriority(Job.INTERACTIVE);
		}

		@Override
		public boolean belongsTo(Object family) {
			return CollectionEditGroup.this == family;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			asyncExec(() -> {
				if (!control.isDisposed()) {
					control.addDisposeListener(PrepareJob.this);
					// control.setCursor(control.getDisplay().getSystemCursor(SWT.CURSOR_APPSTARTING));
				}
			});
			try {
				doRun(monitor);
			} finally {
				asyncExec(() -> {
					if (!control.isDisposed()) {
						control.removeDisposeListener(PrepareJob.this);
						// control.setCursor(control.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
					}
				});
			}
			return Status.OK_STATUS;
		}

		protected void asyncExec(Runnable runnable) {
			if (!control.isDisposed())
				control.getDisplay().asyncExec(runnable);
		}

		public void widgetDisposed(DisposeEvent e) {
			cancel();
		}

		protected void doRun(IProgressMonitor monitor) {
			int i = 0;
			for (AssetImpl asset : Core.getCore().getDbManager().obtainAssets()) {
				for (QueryField qfield : EXPLORABLES) {
					Object v = qfield.obtainFieldValue(asset);
					if (v instanceof String[]) {
						for (String s : (String[]) v)
							if (s != null && !s.isEmpty())
								saveProposal(preparedValues, qfield, s);
					} else if (v instanceof String && !((String) v).isEmpty())
						saveProposal(preparedValues, qfield, (String) v);
				}
				if (monitor.isCanceled())
					break;
				if (++i % 500 == 0)
					apply();
			}
			preparationDone = true;
			apply();
		}

		public void apply() {
			if (!applyPreparationList.isEmpty() && !control.isDisposed())
				asyncExec(() -> {
					if (!control.isDisposed()) {
						int size = applyPreparationList.size();
						for (int i = 0; i < size; i++)
							applyPreparationList.get(i).run();
					}
				});
		}

		protected void saveProposal(Map<QueryField, Set<String>> valueMap, QueryField qfield, String s) {
			Set<String> set = valueMap.get(qfield);
			if (set == null)
				valueMap.put(qfield, set = Collections.synchronizedSet(new HashSet<String>(101)));
			set.add(s);
		}
	}

	protected static final QueryField[] EXPLORABLES = new QueryField[] { QueryField.EMULSION, QueryField.USERFIELD1,
			QueryField.USERFIELD2, QueryField.IMPORTEDBY, QueryField.EXIF_COPYRIGHT, QueryField.EXIF_LENS,
			QueryField.EXIF_MAKE, QueryField.EXIF_MODEL, QueryField.EXIF_SOFTWARE, QueryField.IPTC_BYLINE,
			QueryField.IPTC_EVENT, QueryField.IPTC_NAMEOFORG, QueryField.IPTC_OWNER, QueryField.IPTC_USAGE,
			QueryField.IPTC_WRITEREDITOR };

	private Composite sortComp;

	private List<CriterionGroup> groups = new ArrayList<CriterionGroup>(5);

	private List<SortCriterionGroup> sortgroups = new ArrayList<SortCriterionGroup>(3);

	private final boolean album;

	private final SmartCollection current;

	private boolean isSystem;

	private final boolean readOnly;

	private ListenerList<Listener> modifyListeners = new ListenerList<>();

	private Map<QueryField, Set<String>> preparedValues = Collections.synchronizedMap(new HashMap<>());

	private boolean preparationDone = false;

	private List<Runnable> applyPreparationList = new ArrayList<Runnable>(5);

	private final Composite comp;

	private ISizeHandler sizeHandler;

	private boolean networked;

	private boolean isImport;

	public CollectionEditGroup(Composite comp, SmartCollection current, boolean album, boolean readOnly,
			boolean networked, ISizeHandler sizeHandler) {
		this.comp = comp;
		this.current = current;
		this.album = album;
		this.readOnly = readOnly;
		this.networked = networked;
		this.sizeHandler = sizeHandler;
		this.isSystem = current != null && current.getSystem();
		this.isImport = current != null && current.getStringId().startsWith(IDbManager.IMPORTKEY);
		createSelectionAndSortCriteria(comp);
	}

	@SuppressWarnings("unused")
	private void createSelectionAndSortCriteria(Composite parent) {
		if (album) {
			Label sortLabel = new Label(parent, SWT.NONE);
			sortLabel.setText(Messages.CollectionEditDialog_sort_criteria);
			sortLabel.setFont(JFaceResources.getBannerFont());
		} else {
			Composite rowComp = new Composite(parent, SWT.NONE);
			rowComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			rowComp.setLayout(new GridLayout(10, false));
			new Label(rowComp, SWT.NONE);
			createLabel(rowComp, Messages.CollectionEditDialog_group);
			createLabel(rowComp, Messages.CollectionEditDialog_field);
			createLabel(rowComp, Messages.CollectionEditDialog_relation);
			createLabel(rowComp, Messages.CollectionEditDialog_value)
					.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 6, 1));
			if (current == null || current.getCriterion().isEmpty())
				addGroup(rowComp, null, null, false);
			else
				for (Criterion crit : current.getCriterion())
					addGroup(rowComp, null, crit, crit.getAnd());
		}
		if (!album)
			new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL)
					.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		sortComp = new Composite(parent, SWT.NONE);
		sortComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sortComp.setLayout(new GridLayout(6, false));
		new Label(sortComp, SWT.NONE);
		createLabel(sortComp, Messages.CollectionEditDialog_sort_group);
		createLabel(sortComp, Messages.CollectionEditDialog_sort_field);
		createLabel(sortComp, Messages.CollectionEditDialog_direction);
		new Label(sortComp, SWT.NONE).setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));

		if (current == null)
			addSortGroup(sortComp, null, new SortCriterionImpl(QueryField.IPTC_DATECREATED.getKey(), null, false));
		else if (current.getSortCriterion().isEmpty())
			addSortGroup(sortComp, null, null);
		else
			for (SortCriterion crit : current.getSortCriterion())
				addSortGroup(sortComp, null, crit);
	}

	private static Label createLabel(Composite composite, String text) {
		Label label = new Label(composite, SWT.NONE);
		label.setFont(JFaceResources.getBannerFont());
		label.setText(text);
		return label;
	}

	public void addGroup(Composite parent, CriterionGroup sender, Criterion crit, boolean and) {
		int groupNo = sender != null ? groups.indexOf(sender) + 1 : groups.size();
		CriterionGroup group = new CriterionGroup(parent, groupNo, this, crit, !readOnly && !isSystem && !isImport, and,
				networked);
		if (sender != null)
			group.moveBelow(sender);
		groups.add(groupNo, group);
		sizeChanged();
	}

	public void setNetworked(boolean networked) {
		for (CriterionGroup group : groups)
			group.setNetworked(networked);
	}

	public void removeGroup(CriterionGroup group) {
		groups.remove(group);
		group.dispose();
		sizeChanged();
	}

	public void addSortGroup(Composite composite, SortCriterionGroup sender, SortCriterion crit) {
		int groupNo = sortgroups.size();
		if (sender != null)
			groupNo = sortgroups.indexOf(sender) + 1;
		SortCriterionGroup group = new SortCriterionGroup(composite, groupNo, this, crit, !readOnly);
		if (sender != null)
			group.moveBelow(sender);
		sortgroups.add(groupNo, group);
		sizeChanged();

	}

	private void sizeChanged() {
		if (sizeHandler != null)
			sizeHandler.sizeChanged();
	}

	public void removeSortGroup(SortCriterionGroup group) {
		sortgroups.remove(group);
		group.dispose();
		sizeChanged();
	}

	public void addListener(Listener listener) {
		modifyListeners.add(listener);
	}
	
	public void removeListener(Listener listener) {
		modifyListeners.remove(listener);
	}

	public void dispose() {
		Job.getJobManager().cancel(this);
		for (CriterionGroup group : groups)
			group.dispose();
		groups.clear();
		for (SortCriterionGroup group : sortgroups)
			group.dispose();
		sortgroups.clear();
	}

	public String validate() {
		boolean byValue = false;
		boolean dynamic = false;
		boolean disjunction = false;
		boolean evaluation = false;
		boolean first = true;
		for (CriterionGroup group : groups) {
			String errorMessage = group.getErrorMessage();
			if (errorMessage != null)
				return errorMessage;
			Criterion crit = group.getCriterion();
			if (crit == null)
				return ""; //$NON-NLS-1$
			String field = crit.getField();
			String subfield = crit.getSubfield();
			int rel = crit.getRelation();
			QueryField qfield = QueryField.findQueryField(subfield != null ? field + ':' + subfield : field);
			if (qfield == QueryField.IPTC_LOCATIONSHOWN || qfield == QueryField.IPTC_LOCATIONCREATED
					|| qfield == QueryField.IPTC_CONTACT || qfield == QueryField.IPTC_ARTWORK)
				dynamic = true;
			else if (qfield == QueryField.ASPECTRATIO)
				evaluation = true;
			else if (rel == QueryField.WILDCARDS || rel == QueryField.NOTWILDCARDS)
				evaluation = true;
			else if (qfield.getCard() != 1 && qfield.getType() == QueryField.T_STRING && rel != QueryField.EQUALS
					&& rel != QueryField.NOTEQUAL)
				evaluation = true;
			else
				byValue = true;
			if (!crit.getAnd() && !first)
				disjunction = true;
			first = false;
		}
		if (byValue && (dynamic || evaluation) && disjunction)
			return Messages.CollectionEditDialog_query_too_complex;
		int i = 0;
		for (SortCriterionGroup group : sortgroups)
			if ((i++ > 0 || isSystem) && group.getCriterion() == null)
				return ""; //$NON-NLS-1$
		return null;
	}

	public void applyCriteria(SmartCollectionImpl result, String name) {
		List<Criterion> criteria = new ArrayList<Criterion>(groups.size());
		if (album) {
			criteria.add(new CriterionImpl(QueryField.ALBUM.getKey(), null, name, QueryField.XREF, false));
			result.setCriterion(criteria);
		} else if (isSystem)
			result.setCriterion(current.getCriterion());
		else {
			for (CriterionGroup group : groups) {
				Criterion criterion = group.getCriterion();
				if (criterion != null)
					criteria.add(criterion);
			}
			result.setCriterion(criteria);
		}
		List<SortCriterion> sortcriteria = new ArrayList<SortCriterion>(sortgroups.size());
		for (SortCriterionGroup group : sortgroups) {
			SortCriterion criterion = group.getCriterion();
			if (criterion != null)
				sortcriteria.add(criterion);
		}
		result.setSortCriterion(sortcriteria);
	}

	public void fireModified(Event ev) {
		for (Listener listener : modifyListeners)
			listener.handleEvent(ev);
	}

	public boolean hasPreparedValues(QueryField qfield) {
		for (QueryField queryField : EXPLORABLES)
			if (queryField == qfield)
				return true;
		return false;
	}

	public void fillPreparedValues(final Combo combo, final QueryField qfield) {
		if (preparationDone) {
			Set<String> valueSet = preparedValues.get(qfield);
			String[] values = valueSet.toArray(new String[valueSet.size()]);
			Arrays.sort(values);
			combo.setItems(values);
		} else
			applyPreparationList.add(new Runnable() {
				public void run() {
					if (!combo.isDisposed()) {
						String text = combo.getText();
						Set<String> valueSet = preparedValues.get(qfield);
						String[] values = valueSet.toArray(new String[valueSet.size()]);
						Arrays.sort(values);
						if (!Arrays.equals(combo.getItems(), values)) {
							combo.setItems(values);
							combo.setText(text);
						}
					}
				}
			});
	}

	public void prepare() {
		if (!readOnly && !isSystem && !album)
			new PrepareJob(comp).schedule();
	}

}
