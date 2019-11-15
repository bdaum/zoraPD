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
package com.bdaum.zoom.db.internal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.group.Criterion;
import com.bdaum.zoom.cat.model.group.PostProcessorImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.meta.Category;
import com.bdaum.zoom.core.IPostProcessor;
import com.bdaum.zoom.core.IPostProcessor2;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.Range;
import com.bdaum.zoom.core.db.IDbFactory;
import com.bdaum.zoom.db.internal.CollectionProcessor.StringArrayEvaluation;
import com.bdaum.zoom.db.internal.CollectionProcessor.WildcardEvaluation;
import com.bdaum.zoom.fileMonitor.internal.filefilter.WildCardFilter;
import com.db4o.query.Constraint;
import com.db4o.query.Query;

@SuppressWarnings("serial")
public class QueryPostProcessor extends PostProcessorImpl implements IPostProcessor2 {

	private final IDbFactory dbfactory;

	public QueryPostProcessor(IDbFactory factory) {
		dbfactory = factory;
	}

	@Override
	public IPostProcessor clone() {
		return new QueryPostProcessor(dbfactory);
	}

	public List<Asset> process(List<Asset> set) {
		List<Asset> result = new ArrayList<Asset>(set.size());
		for (Asset asset : set)
			if (accept(asset))
				result.add(asset);
		return result;
	}

	public boolean accept(Asset asset) {
		boolean all = false;
		SmartCollection sm = getSmartCollection_parent();
		boolean first = true;
		for (Criterion crit : sm.getCriterion()) {
			boolean result = false;
			String field = crit.getField();
			int relation = crit.getRelation();
			Object value = crit.getValue();
			Object vto = crit.getTo();
			QueryField qfield = QueryField.findQueryField(field);
			if (qfield != null) {
				int type = qfield.getType();
				Object fieldValue = qfield.obtainFieldValue(asset);
				if (fieldValue != null) {
					if (qfield.getCard() != 1) {
						switch (type) {
						case QueryField.T_INTEGER:
						case QueryField.T_POSITIVEINTEGER: {
							int v = (Integer) value;
							for (int element : (int[]) fieldValue)
								if (element == v) {
									result = true;
									break;
								}
							if (relation == QueryField.NOTEQUAL)
								result = !result;
						}
							break;
						case QueryField.T_POSITIVEFLOAT:
						case QueryField.T_FLOAT: {
							double v = (Double) value;
							for (double element : (double[]) fieldValue) {
								if (element == v)
									result = true;
								break;
							}
							if (relation == QueryField.NOTEQUAL)
								result = !result;
						}
							break;
						default:
							String[] array = (String[]) fieldValue;
							String svalue = (String) value;
							switch (relation) {
							case QueryField.EQUALS:
								for (String v : array)
									if (svalue.equals(v)) {
										result = true;
										break;
									}
								break;
							case QueryField.NOTEQUAL:
								result = true;
								for (String v : array)
									if (svalue.equals(v)) {
										result = false;
										break;
									}
								break;
							case QueryField.SIMILAR:
								for (String v : array)
									if (svalue.equalsIgnoreCase(v)) {
										result = true;
										break;
									}
								break;
							case QueryField.NOTSIMILAR:
								result = true;
								for (String v : array)
									if (svalue.equalsIgnoreCase(v)) {
										result = false;
										break;
									}
								break;
							case QueryField.STARTSWITH:
								for (String v : array) {
									if (v != null && v.startsWith(svalue)) {
										result = true;
										break;
									}
								}
								break;
							case QueryField.DOESNOTSTARTWITH:
								for (String v : array)
									if (v != null && !v.startsWith(svalue)) {
										result = true;
										break;
									}
								break;
							case QueryField.ENDSWITH:
								for (String v : array)
									if (v != null && v.endsWith(svalue)) {
										result = true;
										break;
									}
								break;
							case QueryField.DOESNOTENDWITH:
								for (String v : array)
									if (v != null && !v.endsWith(svalue)) {
										result = true;
										break;
									}
								break;
							case QueryField.CONTAINS:
								for (String v : array)
									if (v != null && v.indexOf(svalue) >= 0) {
										result = true;
										break;
									}
								break;
							case QueryField.DOESNOTCONTAIN:
								for (String v : array)
									if (v != null && v.indexOf(svalue) < 0) {
										result = true;
										break;
									}
								break;
							case QueryField.WILDCARDS:
								WildCardFilter filter = new WildCardFilter(svalue, false);
								for (String v : array)
									if (filter.accept(v)) {
										result = true;
										break;
									}
								break;
							case QueryField.NOTWILDCARDS:
								result = true;
								filter = new WildCardFilter(svalue, false);
								for (String v : array)
									if (filter.accept(v)) {
										result = false;
										break;
									}
								break;
							}
						}
					} else {
						switch (type) {
						case QueryField.T_INTEGER:
						case QueryField.T_POSITIVEINTEGER: {
							int v1 = (Integer) fieldValue;
							int from = 0, to = 0;
							switch (relation) {
							case QueryField.NOTSIMILAR:
							case QueryField.SIMILAR: {
								float tolerance = getTolerance(field);
								if (tolerance != 0f) {
									int v2 = (Integer) value;
									if (tolerance < 0) {
										from = v2 + (int) tolerance;
										to = v2 - (int) tolerance;
									} else {
										from = (int) (v2 * (double) ((100 - tolerance) / 100) + 0.5d);
										to = (int) (v2 * (double) ((100 + tolerance) / 100) + 0.5d);
									}
									relation = relation == QueryField.NOTSIMILAR ? QueryField.NOTBETWEEN
											: QueryField.BETWEEN;
								}
							}
								break;
							case QueryField.BETWEEN:
							case QueryField.NOTBETWEEN: {
								from = (Integer) value;
								to = (Integer) vto;
								break;
							}
							}
							switch (relation) {
							case QueryField.EQUALS:
							case QueryField.SIMILAR:
								result = v1 == (Integer) value;
								break;
							case QueryField.NOTEQUAL:
							case QueryField.NOTSIMILAR:
								result = v1 != (Integer) value;
								break;
							case QueryField.GREATER:
								result = v1 > (Integer) value;
								break;
							case QueryField.SMALLER:
								result = v1 < (Integer) value;
								break;
							case QueryField.NOTGREATER:
								result = v1 <= (Integer) value;
								break;
							case QueryField.NOTSMALLER:
								result = v1 >= (Integer) value;
								break;
							case QueryField.BETWEEN:
								result = v1 >= from && v1 <= to;
								break;
							case QueryField.NOTBETWEEN: {
								result = v1 < from || v1 > to;
								break;
							}
							}
						}
							break;
						case QueryField.T_LONG:
						case QueryField.T_POSITIVELONG: {
							long v1 = (Long) fieldValue;
							switch (relation) {
							case QueryField.SIMILAR:
							case QueryField.EQUALS:
								result = v1 == (Long) value;
								break;
							case QueryField.NOTEQUAL:
							case QueryField.NOTSIMILAR:
								result = v1 != (Long) value;
								break;
							case QueryField.GREATER:
								result = v1 > (Long) value;
								break;
							case QueryField.SMALLER:
								result = v1 < (Long) value;
								break;
							case QueryField.NOTGREATER:
								result = v1 <= (Long) value;
								break;
							case QueryField.NOTSMALLER:
								result = v1 >= (Long) value;
								break;
							case QueryField.BETWEEN:
								result = v1 >= (Long) value && v1 <= (Long) vto;
								break;
							case QueryField.NOTBETWEEN:
								result = v1 < (Long) value || v1 > (Long) vto;
								break;
							}
						}
							break;
						case QueryField.T_FLOAT:
						case QueryField.T_POSITIVEFLOAT:
						case QueryField.T_CURRENCY: {
							double from = 0, to = 0;
							switch (relation) {
							case QueryField.SIMILAR:
							case QueryField.NOTSIMILAR: {
								float tolerance = getTolerance(field);
								if (tolerance != 0f) {
									double v2 = (Double) value;
									if (tolerance < 0) {
										from = v2 + tolerance;
										to = v2 - tolerance;
									} else {
										from = v2 * ((100 - tolerance) / 100);
										to = v2 * ((100 + tolerance) / 100);
									}
									relation = relation == QueryField.NOTSIMILAR ? QueryField.NOTBETWEEN
											: QueryField.BETWEEN;
								}
							}
								break;
							case QueryField.BETWEEN:
							case QueryField.NOTBETWEEN: {
								from = (Double) value;
								to = (Double) vto;
								break;
							}
							}
							double v1 = (Double) fieldValue;
							switch (relation) {
							case QueryField.EQUALS:
							case QueryField.SIMILAR:
								result = v1 == (Double) value;
								break;
							case QueryField.NOTEQUAL:
							case QueryField.NOTSIMILAR:
								result = v1 != (Double) value;
								break;
							case QueryField.GREATER:
								result = v1 > (Double) value;
								break;
							case QueryField.SMALLER:
								result = v1 < (Double) value;
								break;
							case QueryField.NOTGREATER:
								result = v1 <= (Double) value;
								break;
							case QueryField.NOTSMALLER:
								result = v1 >= (Double) value;
								break;
							case QueryField.BETWEEN:
								result = v1 >= from && v1 <= to;
								break;
							case QueryField.NOTBETWEEN:
								result = v1 < from || v1 > to;
								break;
							case QueryField.UNDEFINED:
								result = Double.isNaN(v1);
								break;
							}
						}
							break;
						case QueryField.T_BOOLEAN: {
							boolean v1 = ((Boolean) fieldValue).booleanValue();
							switch (relation) {
							case QueryField.EQUALS:
								result = v1 == ((Boolean) value).booleanValue();
								break;
							case QueryField.NOTEQUAL:
								result = v1 != ((Boolean) value).booleanValue();
								break;
							}
						}
							break;
						case QueryField.T_DATE: {
							Date v1 = (Date) fieldValue;
							Date from = null, to = null;
							switch (relation) {
							case QueryField.NOTSIMILAR:
							case QueryField.SIMILAR: {
								float tolerance = getTolerance(field);
								if (tolerance != 0f) {
									long time = ((Date) value).getTime();
									from = new Date(time + (int) tolerance);
									to = new Date(time - (int) tolerance);
									relation = relation == QueryField.NOTSIMILAR ? QueryField.NOTBETWEEN
											: QueryField.BETWEEN;
								}
							}
								break;
							case QueryField.BETWEEN:
							case QueryField.NOTBETWEEN: {
								from = (Date)value;
								to = (Date) vto;
								break;
							}
							}
							switch (relation) {
							case QueryField.DATEEQUALS:
							case QueryField.SIMILAR:
								long fromtime = ((Date) value).getTime() / 1000 * 1000;
								long t1 = v1.getTime();
								result = t1 >= fromtime && t1 <= fromtime + 999;
								break;
							case QueryField.DATENOTEQUAL:
							case QueryField.NOTSIMILAR:
								fromtime = ((Date) value).getTime() / 1000 * 1000;
								t1 = v1.getTime();
								result = t1 < fromtime || t1 > fromtime + 999;
								break;
							case QueryField.GREATER:
								result = v1.compareTo((Date) value) > 0;
								break;
							case QueryField.SMALLER:
								result = v1.compareTo((Date) value) < 0;
								break;
							case QueryField.NOTGREATER:
								result = v1.compareTo((Date) value) <= 0;
								break;
							case QueryField.NOTSMALLER:
								result = v1.compareTo((Date) value) >= 0;
								break;
							case QueryField.BETWEEN:
								result = v1.compareTo(from) >= 0 && v1.compareTo(to) <= 0;
								break;
							case QueryField.NOTBETWEEN: {
								result = v1.compareTo(from) < 0 || v1.compareTo(to) > 0;
								break;
							}
							}
						}
							break;
						default:
							String v1 = fieldValue.toString();
							switch (relation) {
							case QueryField.EQUALS:
								result = v1.equals(value);
								break;
							case QueryField.NOTEQUAL:
								result = !v1.equals(value);
								break;
							case QueryField.SIMILAR:
								result = value instanceof String && v1.equalsIgnoreCase((String) value);
								break;
							case QueryField.NOTSIMILAR:
								result = !(value instanceof String) || !v1.equalsIgnoreCase((String) value);
								break;
							case QueryField.STARTSWITH:
								result = v1.startsWith((String) value);
								break;
							case QueryField.DOESNOTSTARTWITH:
								result = !v1.startsWith((String) value);
								break;
							case QueryField.ENDSWITH:
								result = v1.endsWith((String) value);
								break;
							case QueryField.DOESNOTENDWITH:
								result = !v1.endsWith((String) value);
								break;
							case QueryField.CONTAINS:
								result = v1.indexOf((String) value) >= 0;
								break;
							case QueryField.DOESNOTCONTAIN:
								result = v1.indexOf((String) value) < 0;
								break;
							case QueryField.WILDCARDS:
								return new WildCardFilter((String) value, false).accept(v1);
							case QueryField.NOTWILDCARDS:
								return !new WildCardFilter((String) value, false).accept(v1);
							}
							break;
						}
					}
				} else
					result = (relation == QueryField.UNDEFINED);
			}
			if (first) {
				all = result;
				first = false;
			} else if (crit.getAnd())
				all &= result;
			else
				all |= result;
		}
		return all;
	}

	public Constraint getConstraint(DbManager dbManager, Query query) {
		SmartCollection tempColl = getSmartCollection_parent();
		Constraint disjunction = null;
		for (Criterion crit : tempColl.getCriterion()) {
			if (crit == null)
				return null;
			String field = crit.getField();
			QueryField qfield = QueryField.findQueryField(field);
			if (qfield == null)
				return null;
			int relation = crit.getRelation();
			Object value = crit.getValue();
			Object to = crit.getTo();
			Constraint constraint = null;
			if (relation == QueryField.DATEEQUALS || relation == QueryField.DATENOTEQUAL) {
				long t = ((Date) value).getTime() / 1000 * 1000;
				value = new Date(t);
				to = new Date(t + 999);
				relation = (relation == QueryField.DATEEQUALS) ? QueryField.BETWEEN : QueryField.NOTBETWEEN;
			} else if (qfield.getType() != QueryField.T_STRING
					&& (relation == QueryField.SIMILAR || relation == QueryField.NOTSIMILAR)) {
				float tolerance = getTolerance(field);
				if (tolerance != 0f) {
					relation = relation == QueryField.NOTSIMILAR ? QueryField.NOTBETWEEN : QueryField.BETWEEN;
					Range range = CollectionProcessor.compileSimilarRange(value, tolerance);
					value = range.getFrom();
					to = range.getTo();
				} else
					relation = relation == QueryField.NOTSIMILAR ? QueryField.NOTEQUAL : QueryField.EQUALS;
			}
			if (relation == QueryField.BETWEEN || relation == QueryField.NOTBETWEEN)
				constraint = CollectionProcessor.applyBetween(query, field, field, relation, value, to);
			else if (relation != QueryField.EQUALS && relation != QueryField.NOTEQUAL && qfield.getCard() != 1
					&& qfield.getType() == QueryField.T_STRING)
				constraint = query.constrain(new StringArrayEvaluation(qfield, relation, (String) value));
			else if (relation == QueryField.SIMILAR)
				constraint = query.descend(field).constrain(value).like();
			else if (relation == QueryField.NOTSIMILAR)
				constraint = query.descend(field).constrain(value).like().not();
			else if (relation == QueryField.WILDCARDS)
				query.constrain(new WildcardEvaluation(qfield, (String) value, false));
			else if (relation == QueryField.NOTWILDCARDS)
				query.constrain(new WildcardEvaluation(qfield, (String) value, true));
			else if (qfield == QueryField.IPTC_CATEGORY) {
				constraint = query.descend(field).constrain(value);
				CollectionProcessor.applyRelation(dbManager, field, field, crit.getRelation(), value, constraint,
						query);
				Category category = dbManager.getMeta(false).getCategory(value.toString());
				if (category != null)
					constraint = CollectionProcessor.addSubcats(dbManager, query, constraint, category, crit);
			} else if (relation == QueryField.UNDEFINED && value instanceof Double)
				constraint = query.descend(field).constrain(Double.NEGATIVE_INFINITY).greater().equal().not();
			else if (relation == QueryField.UNDEFINED && value instanceof Date)
				constraint = query.descend(field).constrain(null);
			else {
				constraint = query.descend(field).constrain(value);
				if (constraint != null)
					constraint = CollectionProcessor.applyRelation(tempColl.getSystem() ? dbManager : null, field,
							field, crit.getRelation(), value, constraint, query);
			} // end single criterion processing
			if (constraint != null)
				disjunction = disjunction != null
						? (crit.getAnd()) ? disjunction.and(constraint) : disjunction.or(constraint)
						: constraint;
		}
		return disjunction;
	}

	private float getTolerance(String field) {
		return dbfactory.getTolerance(field);
	}

}