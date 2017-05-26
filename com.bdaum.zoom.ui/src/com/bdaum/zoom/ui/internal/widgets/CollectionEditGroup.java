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
 * (c) 2011-2017 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ui.internal.widgets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.Criterion;
import com.bdaum.zoom.cat.model.group.CriterionImpl;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.SortCriterion;
import com.bdaum.zoom.cat.model.group.SortCriterionImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.ui.internal.dialogs.Messages;
import com.bdaum.zoom.ui.internal.job.PrepareDataJob;

public class CollectionEditGroup {

	public class PrepareJob extends PrepareDataJob {

		public PrepareJob(Control control) {
			super(Messages.CollectionEditDialog_prepare_field_values, control);
		}

		@Override
		public boolean belongsTo(Object family) {
			return CollectionEditGroup.this == family;
		}

		@Override
		protected void doRun(IProgressMonitor monitor) {
			Map<QueryField, Set<String>> valueMap = new HashMap<QueryField, Set<String>>(EXPLORABLES.length * 3 / 2);
			IDbManager dbManager = Core.getCore().getDbManager();
			for (AssetImpl asset : dbManager.obtainAssets()) {
				for (QueryField qfield : EXPLORABLES) {
					Object v = qfield.obtainFieldValue(asset);
					if (v instanceof String[]) {
						for (String s : (String[]) v)
							if (s != null && !s.isEmpty())
								saveProposal(valueMap, qfield, s);
					} else if (v instanceof String) {
						String s = (String) v;
						if (!s.isEmpty())
							saveProposal(valueMap, qfield, s);
					}
				}
				if (monitor.isCanceled())
					break;
			}
			preparedValues = valueMap;
			if (!control.isDisposed())
				asyncExec(new Runnable() {
					public void run() {
						if (!control.isDisposed())
							for (Runnable runnable : applyPreparationList)
								runnable.run();
					}
				});
		}

		protected void saveProposal(Map<QueryField, Set<String>> valueMap, QueryField qfield, String s) {
			Set<String> set = valueMap.get(qfield);
			if (set == null)
				valueMap.put(qfield, set = new HashSet<String>(100));
			set.add(s);
		}
	}

	protected static final QueryField[] EXPLORABLES = new QueryField[] { QueryField.EMULSION, QueryField.USERFIELD1,
			QueryField.USERFIELD2, QueryField.IMPORTEDBY, QueryField.EXIF_COPYRIGHT, QueryField.EXIF_LENS,
			QueryField.EXIF_MAKE, QueryField.EXIF_MODEL, QueryField.EXIF_SOFTWARE, QueryField.IPTC_BYLINE,
			QueryField.IPTC_EVENT, QueryField.IPTC_INTELLECTUAL_GENRE, QueryField.IPTC_NAMEOFORG, QueryField.IPTC_OWNER,
			QueryField.IPTC_USAGE, QueryField.IPTC_WRITEREDITOR };

	private Composite sortComp;

	private List<CriterionGroup> groups = new ArrayList<CriterionGroup>(5);

	private List<SortCriterionGroup> sortgroups = new ArrayList<SortCriterionGroup>(3);

	private final boolean album;

	private final SmartCollectionImpl current;

	private boolean isSystem;

	private final boolean readOnly;

	private ListenerList<ModifyListener> modifyListeners = new ListenerList<ModifyListener>();

	private Map<QueryField, Set<String>> preparedValues;

	private List<Runnable> applyPreparationList = new ArrayList<Runnable>(5);

	private final Composite comp;

	private ISizeHandler sizeHandler;

	private boolean networked;

	public CollectionEditGroup(Composite comp, SmartCollectionImpl current, boolean album, boolean readOnly,
			boolean networked, ISizeHandler sizeHandler) {
		this.comp = comp;
		this.current = current;
		this.album = album;
		this.readOnly = readOnly;
		this.networked = networked;
		this.sizeHandler = sizeHandler;
		this.isSystem = current != null && current.getSystem();
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
			Label valueLabel = createLabel(rowComp, Messages.CollectionEditDialog_value);
			valueLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 6, 1));
			parent.getShell().pack();
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
		Label filler = new Label(sortComp, SWT.NONE);
		filler.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));

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
		CriterionGroup group = new CriterionGroup(parent, groupNo, this, crit, !readOnly && !isSystem, and, networked);
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

	public void addModifyListener(ModifyListener modifyListener) {
		modifyListeners.add(modifyListener);
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

	public void fireModified() {
		for (Object listener : modifyListeners.getListeners())
			((ModifyListener) listener).modifyText(null);
	}

	public boolean hasPreparedValues(QueryField qfield) {
		for (QueryField queryField : EXPLORABLES)
			if (queryField == qfield)
				return true;
		return false;
	}

	public void fillPreparedValues(final Combo combo, final QueryField qfield) {
		if (preparedValues != null) {
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
						combo.setItems(values);
						combo.setText(text);
					}
				}
			});
	}

	public void prepare() {
		if (!readOnly && !isSystem && !album)
			new PrepareJob(comp).schedule();
	}

}
