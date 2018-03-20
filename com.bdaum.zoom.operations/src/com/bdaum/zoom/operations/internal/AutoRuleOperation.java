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
 * (c) 2015 Berthold Daum  
 */
package com.bdaum.zoom.operations.internal;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.group.CriterionImpl;
import com.bdaum.zoom.cat.model.group.GroupImpl;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.SortCriterion;
import com.bdaum.zoom.cat.model.group.SortCriterionImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.core.internal.operations.AutoRule;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;

@SuppressWarnings("restriction")
public class AutoRuleOperation extends DbOperation {

	private final List<AutoRule> rules;
	private Collection<? extends Asset> assets;
	private boolean change = false;
	private final QueryField node;

	public AutoRuleOperation(List<AutoRule> rules, Collection<? extends Asset> assets, QueryField node) {
		super(Messages.getString("AutoRuleOperation.automatic_collection_creation")); //$NON-NLS-1$
		this.rules = rules;
		this.assets = assets;
		this.node = node;
	}

	public int getExecuteProfile() {
		return IProfiledOperation.CAT;
	}

	public int getUndoProfile() {
		return IProfiledOperation.CAT;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		if (rules != null) {
			if (assets == null)
				assets = dbManager.obtainAssets();
			init(monitor, assets.size());
			for (Asset asset : assets) {
				change |= processRules(asset, rules, node, monitor);
				if (updateMonitor(1))
					break;
			}
			if (change)
				fireStructureModified();
		}
		return close(info);
	}

	@Override
	public boolean isSilent() {
		return !change;
	}

	private boolean processRules(Asset asset, List<AutoRule> rules, QueryField node, IProgressMonitor monitor) {
		boolean change = false;
		lpr: for (AutoRule autoRule : rules) {
			QueryField qfield = autoRule.getQfield();
			// Remove unwanted
			if (node != null && !accept(qfield, node))
				break;
			if (monitor.isCanceled())
				break;
			Object fieldValue = qfield.obtainFieldValue(asset);
			Object upperValue = null;
			int type = qfield.getType();
			if (fieldValue instanceof Double) {
				if (Double.isNaN((Double) fieldValue))
					continue;
				if (type == QueryField.T_POSITIVEFLOAT && ((Double) fieldValue) < 0)
					continue;
			}
			if (fieldValue instanceof Long && type == QueryField.T_POSITIVELONG && ((Long) fieldValue) < 0)
				continue;
			if (fieldValue instanceof Integer && type == QueryField.T_POSITIVEINTEGER && ((Integer) fieldValue) < 0)
				continue;
			int autoPolicy = qfield.getAutoPolicy();
			double[] intervals = autoRule.getIntervals();
			List<String> values = autoRule.getValues();
			switch (autoPolicy) {
			case QueryField.AUTO_DISCRETE:
				if (fieldValue == null)
					continue;
				if (autoPolicy == QueryField.AUTO_DISCRETE
						&& (qfield.getType() == QueryField.T_BOOLEAN || qfield.getEnumeration() != null)) {
					Object[] enumeration = autoRule.getEnumeration();
					boolean found = false;
					lp1: for (Object d : enumeration)
						if (fieldValue instanceof String[]) {
							for (String s : (String[]) fieldValue) {
								if (d.equals(s)) {
									found = true;
									break lp1;
								}
							}
						} else if (d.equals(fieldValue)) {
							found = true;
							break;
						}
					if (!found)
						continue;
				}
				break;
			case QueryField.AUTO_CONTAINS:
				if (values == null || values.isEmpty())
					continue;
				boolean found = false;
				lp2: for (String s : values) {
					if (fieldValue instanceof String) {
						if (((String) fieldValue).indexOf(s) >= 0) {
							found = true;
							break;
						}
					} else if (fieldValue instanceof String[]) {
						for (String v : ((String[]) fieldValue)) {
							if (v.indexOf(s) >= 0) {
								found = true;
								break lp2;
							}
						}
					}
				}
				if (!found)
					continue;
				break;
			case QueryField.AUTO_SELECT:
				if (values == null || values.isEmpty())
					continue;
				found = false;
				lp3: for (String s : values) {
					if (AutoRule.VALUE_UNDEFINED.equals(s)) {
						if (!(fieldValue instanceof String[]) || ((String[]) fieldValue).length == 0) {
							found = true;
							break;
						}
					} else if (fieldValue instanceof String[]) {
						for (String v : ((String[]) fieldValue)) {
							if (s.equals(v)) {
								found = true;
								break lp3;
							}
						}
					}
				}
				if (!found)
					continue;
				break;
			default:
				if (intervals == null || intervals.length == 0)
					continue;
				if (fieldValue == null)
					continue;
				break;
			}
			// Lets start
			Set<Object> store = new HashSet<Object>();
			StringBuilder name = new StringBuilder();
			StringBuilder id = new StringBuilder();
			Object value;
			int card = fieldValue instanceof String[] ? ((String[]) fieldValue).length : 1;
			if (card == 0 && autoPolicy == QueryField.AUTO_SELECT)
				card = 1;
			int nv = values != null && (autoPolicy == QueryField.AUTO_CONTAINS || autoPolicy == QueryField.AUTO_SELECT)
					? values.size()
					: 1;
			lpf: for (int i = 0; i < card; i++) {
				for (int k = 0; k < nv; k++) {
					value = null;
					int rel = QueryField.EQUALS;
					name.setLength(0);
					id.setLength(0);
					id.append(Constants.GROUP_ID_AUTOSUB).append(qfield.getId());
					String groupId = id.toString();
					switch (autoPolicy) {
					case QueryField.AUTO_DISCRETE:
						if (fieldValue instanceof String[]) {
							value = ((String[]) fieldValue)[i];
							if (value == null)
								continue lpf;
						} else
							value = fieldValue;
						break;
					case QueryField.AUTO_CONTAINS:
						if (values == null)
							continue lpr;
						String s = null;
						if (fieldValue instanceof String[])
							s = ((String[]) fieldValue)[i];
						else if (fieldValue instanceof String)
							s = (String) fieldValue;
						if (s == null)
							continue lpf;
						String t = values.get(k);
						if (s.indexOf(t) < 0)
							continue;
						value = t;
						rel = QueryField.CONTAINS;
						break;
					case QueryField.AUTO_SELECT:
						if (values == null)
							continue lpr;
						t = values.get(k);
						if (AutoRule.VALUE_UNDEFINED.equals(t)
								&& (!(fieldValue instanceof String[]) || ((String[]) fieldValue).length == 0)) {
							rel = QueryField.UNDEFINED;
							break;
						}
						s = null;
						if (fieldValue instanceof String[] && ((String[]) fieldValue).length > i)
							s = ((String[]) fieldValue)[i];
						if (s == null)
							continue lpf;
						if (!s.equals(t))
							continue;
						value = t;
						break;
					default:
						if (intervals == null)
							continue;
						double sample = Double.NaN;
						if (fieldValue instanceof Long)
							sample = ((Long) fieldValue).longValue();
						if (fieldValue instanceof Integer)
							sample = ((Integer) fieldValue).intValue();
						if (fieldValue instanceof Double)
							sample = ((Double) fieldValue).doubleValue();
						if (intervals.length > 1) {
							if (sample < intervals[0]) {
								value = newFieldValue(fieldValue, intervals[0]);
								rel = QueryField.SMALLER;
							} else if (sample >= intervals[intervals.length - 1]) {
								value = newFieldValue(fieldValue, intervals[intervals.length - 1]);
								rel = QueryField.NOTSMALLER;
							} else {
								for (int j = 0; j < intervals.length - 1; j++) {
									if (sample >= intervals[j]) {
										upperValue = newFieldValue(fieldValue, intervals[j + 1]);
										value = newFieldValue(fieldValue, intervals[j]);
										rel = QueryField.BETWEEN;
										break;
									}
								}
							}
						} else {
							double interval = intervals[0];
							double lower, upper;
							if (autoPolicy == QueryField.AUTO_LINEAR) {
								lower = Math.floor(sample / interval);
								upper = lower + interval;
							} else {
								double exp = Math.floor(Math.log(sample) / Math.log(interval));
								lower = Math.pow(interval, exp);
								upper = lower * interval;
							}
							upperValue = newFieldValue(fieldValue, upper);
							value = newFieldValue(fieldValue, lower);
							rel = QueryField.BETWEEN;
						}
						break;
					}
					switch (rel) {
					case QueryField.CONTAINS:
						name.append(NLS.bind(Messages.getString("AutoRuleOperation.contains"), value)); //$NON-NLS-1$
						id.append('{').append(value).append('}');
						break;
					case QueryField.UNDEFINED:
						name.append(Messages.getString("AutoRuleOperation.undefined")); //$NON-NLS-1$
						id.append('¬').append(value);
						break;
					case QueryField.NOTSMALLER:
						name.append("> ").append(nice(value)); //$NON-NLS-1$
						id.append('>').append(value);
						break;
					case QueryField.SMALLER:
						name.append("< ").append(nice(value)); //$NON-NLS-1$
						id.append('<').append(value);
						break;
					case QueryField.EQUALS:
						name.append("= ").append(qfield.formatScalarValue(value)); //$NON-NLS-1$
						id.append('=').append(value);
						break;
					case QueryField.BETWEEN:
						name.append(NLS.bind(Messages.getString("AutoRuleOperation.between"), nice(value), //$NON-NLS-1$
								nice(upperValue)));
						id.append('=').append(value).append('_').append(upperValue);
						break;
					}
					String ids = id.toString();
					SmartCollectionImpl sm = dbManager.obtainById(SmartCollectionImpl.class, ids);
					if (sm == null) {
						change = true;
						GroupImpl subgroup = dbManager.obtainById(GroupImpl.class, groupId);
						if (subgroup == null) {
							GroupImpl autogroup = dbManager.obtainById(GroupImpl.class, Constants.GROUP_ID_AUTO);
							if (autogroup == null) {
								autogroup = new GroupImpl(Messages.getString("AutoRuleOperation.automatic"), false, //$NON-NLS-1$
										Constants.INHERIT_LABEL, null, 0, null);
								autogroup.setStringId(Constants.GROUP_ID_AUTO);
								addInfo(NLS.bind(Messages.getString("AutoRuleOperation.group_created"), //$NON-NLS-1$
										autogroup.getName()));
							}
							subgroup = new GroupImpl(qfield.getLabel(), false, Constants.INHERIT_LABEL, null, 0, null);
							subgroup.setStringId(groupId);
							autogroup.addSubgroup(subgroup);
							subgroup.setGroup_subgroup_parent(autogroup);
							store.add(autogroup);
							addInfo(NLS.bind(Messages.getString("AutoRuleOperation.subgroup_created"), //$NON-NLS-1$
									subgroup.getName(), autogroup.getName()));
						}
						store.add(subgroup);
						sm = new SmartCollectionImpl(name.toString(), false, false, false, false,
								NLS.bind(Messages.getString("AutoRuleOperation.automatically_created"), //$NON-NLS-1$
										qfield.getLabel()),
								-1, null, 0, null, Constants.INHERIT_LABEL, null, 0, null);
						sm.setStringId(ids);
						sm.addCriterion(new CriterionImpl(qfield.getId(), null, value,
								rel == QueryField.BETWEEN ? QueryField.NOTSMALLER : rel, true));
						if (rel == QueryField.BETWEEN)
							sm.addCriterion(
									new CriterionImpl(qfield.getId(), null, upperValue, QueryField.SMALLER, true));
						SortCriterion sortCrit = new SortCriterionImpl(QueryField.IPTC_DATECREATED.getId(), null, true);
						sm.addSortCriterion(sortCrit);
						sm.setGroup_rootCollection_parent(subgroup.getStringId());
						subgroup.addRootCollection(sm.getStringId());
						Utilities.storeCollection(sm, false, store);
						addInfo(NLS.bind(Messages.getString("AutoRuleOperation.collection_created"), //$NON-NLS-1$
								sm.getName(), subgroup.getName()));
					}
				}
			}
			if (!store.isEmpty())
				dbManager.safeTransaction(null, store);
		}
		return change;
	}

	private boolean accept(QueryField qfield, QueryField node) {
		if (qfield == node)
			return true;
		QueryField[] children = qfield.getChildren();
		if (children != null)
			for (QueryField child : children)
				if (accept(child, node))
					return true;
		return false;
	}

	private static Object nice(Object v) {
		if (v instanceof Double) {
			NumberFormat af = NumberFormat.getInstance();
			af.setMinimumFractionDigits(0);
			double d = (Double) v;
			if (d >= 10d)
				af.setMaximumFractionDigits(2);
			else if (d < 0.1d)
				af.setMaximumFractionDigits(5);
			else
				af.setMaximumFractionDigits(3);
			return af.format(d);
		}
		return v;
	}

	private static Object newFieldValue(Object fieldValue, double d) {
		if (fieldValue instanceof Double)
			return d;
		if (fieldValue instanceof Long)
			return (long) d;
		if (fieldValue instanceof Integer)
			return (int) d;
		return fieldValue;
	}

	@Override
	public boolean canUndo() {
		return false;
	}

	@Override
	public boolean canRedo() {
		return false;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return Status.OK_STATUS;
	}

}
