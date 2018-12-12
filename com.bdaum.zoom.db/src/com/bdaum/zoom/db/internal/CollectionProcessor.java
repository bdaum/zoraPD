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
 * (c) 2009-2014 Berthold Daum  
 */

package com.bdaum.zoom.db.internal;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bdaum.aoModeling.runtime.AomMap;
import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.SimilarityOptions_typeImpl;
import com.bdaum.zoom.cat.model.TextSearchOptions_type;
import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObjectImpl;
import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObjectShownImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.asset.MediaExtension;
import com.bdaum.zoom.cat.model.creatorsContact.ContactImpl;
import com.bdaum.zoom.cat.model.creatorsContact.CreatorsContactImpl;
import com.bdaum.zoom.cat.model.group.Criterion;
import com.bdaum.zoom.cat.model.group.PostProcessor;
import com.bdaum.zoom.cat.model.group.PostProcessorImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.SortCriterion;
import com.bdaum.zoom.cat.model.location.LocationImpl;
import com.bdaum.zoom.cat.model.locationCreated.LocationCreatedImpl;
import com.bdaum.zoom.cat.model.locationShown.LocationShownImpl;
import com.bdaum.zoom.cat.model.meta.Category;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IAssetFilter;
import com.bdaum.zoom.core.IPostProcessor;
import com.bdaum.zoom.core.IScoreFormatter;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.Range;
import com.bdaum.zoom.core.db.ICollectionProcessor;
import com.bdaum.zoom.core.db.IDbFactory;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.core.internal.lire.ISearchHits;
import com.bdaum.zoom.core.internal.lucene.ILuceneService;
import com.bdaum.zoom.core.internal.lucene.ParseException;
import com.bdaum.zoom.core.internal.peer.IPeerService;
import com.bdaum.zoom.fileMonitor.internal.filefilter.WildCardFilter;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.ext.DatabaseClosedException;
import com.db4o.ext.Db4oException;
import com.db4o.query.Candidate;
import com.db4o.query.Constraint;
import com.db4o.query.Evaluation;
import com.db4o.query.Query;

@SuppressWarnings("restriction")
public class CollectionProcessor implements ICollectionProcessor {

	private static final int MAXIDS = 2048;
	private static final int MAXALBUMASSETS = 200;

	public static class GenericScoreFormatter implements IScoreFormatter {

		private final float factor;
		private final String suffix;
		private final String label;
		private int fractionDigits;

		public GenericScoreFormatter(int fractionDigits, float factor, String suffix, String label) {
			this.fractionDigits = fractionDigits;
			this.factor = factor;
			this.suffix = suffix;
			this.label = label;
		}

		public String format(float score) {
			NumberFormat nf = NumberFormat.getNumberInstance();
			nf.setMaximumFractionDigits(fractionDigits);
			return (Float.isNaN(score)) ? "" : //$NON-NLS-1$
					nf.format(factor * score) + suffix;
		}

		public String getLabel() {
			return label;
		}
	}

	public static final IScoreFormatter percentFormatter = new GenericScoreFormatter(0, 100, "%", //$NON-NLS-1$
			Messages.CollectionProcessor_score);

	private static final List<Asset> EMPTY = new ArrayList<Asset>(0);

	@SuppressWarnings("serial")
	public static class GeographicPostProcessor extends PostProcessorImpl implements IPostProcessor {

		private final double latitude;
		private final double longitude;
		private final double distance;
		private char unit;

		public GeographicPostProcessor(double latitude, double longitude, double distance, char unit) {
			this.latitude = latitude;
			this.longitude = longitude;
			this.distance = distance;
			this.unit = unit != 0 ? unit : Core.getCore().getDbFactory().getDistanceUnit();
		}

		@Override
		public IPostProcessor clone() {
			return new GeographicPostProcessor(latitude, longitude, distance, unit);
		}

		public List<Asset> process(List<Asset> set) {
			List<Asset> result = new ArrayList<Asset>(set.size());
			for (Asset asset : set) {
				double lat = asset.getGPSLatitude();
				double lon = asset.getGPSLongitude();
				if (!Double.isNaN(lat) && !Double.isNaN(lon)) {
					double dist = Core.distance(latitude, longitude, lat, lon, unit);
					if (dist <= distance) {
						asset.setScore((float) dist);
						result.add(asset);
					}
				}
			}
			Collections.sort(result, new Comparator<Asset>() {
				public int compare(Asset first, Asset second) {
					double distance1 = first.getScore();
					double distance2 = second.getScore();
					return distance1 == distance2 ? 0 : distance1 < distance2 ? -1 : 1;
				}
			});
			return result;
		}
	}

	public static class IdEvaluation implements Evaluation {

		private static final long serialVersionUID = -3849558824141583631L;
		private Set<String> idSet;

		public IdEvaluation(Set<String> idSet) {
			this.idSet = idSet;
		}

		public void evaluate(Candidate candidate) {
			candidate.include((idSet.contains(((AssetImpl) candidate.getObject()).getStringId())));
		}
	}

	public static class AspectEvaluation implements Evaluation {

		private static final long serialVersionUID = -3849558824141583631L;
		private final double from;
		private final double to;
		private final int relation;

		public AspectEvaluation(double from, double to, int relation) {
			this.from = from;
			this.to = to;
			this.relation = relation;
		}

		public void evaluate(Candidate candidate) {
			Asset asset = (Asset) candidate.getObject();
			double h = asset.getHeight();
			double w = asset.getWidth();
			double value;
			value = asset.getRotation() % 180 == 0 ? (h == 0) ? -1d : w / h : (w == 0) ? -1d : h / w;
			if (value < 0)
				candidate.include(false);
			else
				switch (relation) {
				case QueryField.EQUALS:
					candidate.include(from == value);
					break;
				case QueryField.NOTEQUAL:
					candidate.include(from != value);
					break;
				case QueryField.GREATER:
					candidate.include(from < value);
					break;
				case QueryField.NOTGREATER:
					candidate.include(from >= value);
					break;
				case QueryField.SMALLER:
					candidate.include(from > value);
					break;
				case QueryField.NOTSMALLER:
					candidate.include(from <= value);
					break;
				case QueryField.BETWEEN:
					candidate.include(from <= value && to >= value);
					break;
				case QueryField.NOTBETWEEN:
					candidate.include(from > value || to < value);
					break;
				}
		}
	}

	public static class TimeOfDayEvaluation implements Evaluation {

		private static final long serialVersionUID = -3849558824141583631L;
		private Calendar cal = new GregorianCalendar();
		private final int from;
		private final int to;
		private final int relation;

		public TimeOfDayEvaluation(int from, int to, int relation) {
			this.from = from;
			this.to = to;
			this.relation = relation;
		}

		public void evaluate(Candidate candidate) {
			Asset asset = (Asset) candidate.getObject();
			Date dateCreated = asset.getDateCreated();
			if (dateCreated == null)
				dateCreated = asset.getDateTimeOriginal();
			if (dateCreated == null)
				candidate.include(false);
			else {
				cal.setTime(dateCreated);
				int value = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
				switch (relation) {
				case QueryField.EQUALS:
					candidate.include(from == value);
					break;
				case QueryField.NOTEQUAL:
					candidate.include(from != value);
					break;
				case QueryField.GREATER:
					candidate.include(from < value);
					break;
				case QueryField.NOTGREATER:
					candidate.include(from >= value);
					break;
				case QueryField.SMALLER:
					candidate.include(from > value);
					break;
				case QueryField.NOTSMALLER:
					candidate.include(from <= value);
					break;
				case QueryField.BETWEEN:
					candidate.include(from <= value && to >= value);
					break;
				case QueryField.NOTBETWEEN:
					candidate.include(from > value || to < value);
					break;
				}
			}
		}
	}

	public static class DateEvaluation implements Evaluation {

		private static final long serialVersionUID = -3849558824141583631L;
		private static final Date dateTemplate = new Date();
		private final Date from;
		private final Date to;
		private final int relation;

		public DateEvaluation(Date from, Date to, int relation) {
			this.from = from;
			this.to = to;
			this.relation = relation;
		}

		public void evaluate(Candidate candidate) {
			Asset asset = (Asset) candidate.getObject();
			Date dateCreated = asset.getDateCreated();
			if (dateCreated == null)
				dateCreated = asset.getDateTimeOriginal();
			if (dateCreated == null) {
				File file = Core.getCore().getVolumeManager().findExistingFile(asset.getUri(), asset.getVolume());
				if (file != null) {
					dateTemplate.setTime(file.lastModified());
					dateCreated = dateTemplate;
				}
			}
			if (dateCreated == null)
				candidate.include(false);
			else {
				switch (relation) {
				case QueryField.EQUALS:
					candidate.include(from.equals(dateCreated));
					break;
				case QueryField.NOTEQUAL:
					candidate.include(!from.equals(dateCreated));
					break;
				case QueryField.GREATER:
					candidate.include(from.compareTo(dateCreated) < 0);
					break;
				case QueryField.NOTGREATER:
					candidate.include(from.compareTo(dateCreated) >= 0);
					break;
				case QueryField.SMALLER:
					candidate.include(from.compareTo(dateCreated) > 0);
					break;
				case QueryField.NOTSMALLER:
					candidate.include(from.compareTo(dateCreated) <= 0);
					break;
				case QueryField.BETWEEN:
					candidate.include(from.compareTo(dateCreated) <= 0 && to.compareTo(dateCreated) >= 0);
					break;
				case QueryField.NOTBETWEEN:
					candidate.include(from.compareTo(dateCreated) > 0 || to.compareTo(dateCreated) < 0);
					break;
				}
			}
		}
	}

	public static class WildcardEvaluation implements Evaluation {

		private static final long serialVersionUID = -3849558824141583631L;
		private final WildCardFilter filter;
		private final QueryField qfield;
		private final boolean not;

		public WildcardEvaluation(QueryField qfield, String filterString, boolean not) {
			this.qfield = qfield;
			this.not = not;
			this.filter = new WildCardFilter(filterString, false);
		}

		public void evaluate(Candidate candidate) {
			Object object = candidate.getObject();
			candidate.include(filter.accept((String) (object instanceof Asset ? qfield.obtainFieldValue((Asset) object)
					: qfield.obtainPlainFieldValue(object))) != not);
		}
	}

	public static class StringArrayEvaluation implements Evaluation {

		private static final long serialVersionUID = -3849558824141583631L;
		private final WildCardFilter filter;
		private final QueryField qfield;
		private final int relation;
		private final String value;

		public StringArrayEvaluation(QueryField qfield, int relation, String value) {
			this.qfield = qfield;
			this.relation = relation;
			this.value = value;
			this.filter = relation == QueryField.WILDCARDS || relation == QueryField.NOTWILDCARDS
					? new WildCardFilter(value, false)
					: null;
		}

		public void evaluate(Candidate candidate) {
			Object object = candidate.getObject();
			String[] fieldValue = (String[]) (object instanceof Asset ? qfield.obtainFieldValue((Asset) object)
					: qfield.obtainPlainFieldValue(object));
			candidate.include(false);
			if (relation == QueryField.UNDEFINED) {
				if (fieldValue == null || fieldValue.length == 0) {
					candidate.include(true);
					return;
				}
				for (String v : fieldValue)
					if (v != null)
						return;
				candidate.include(true);
				return;
			}
			if (fieldValue == null)
				return;
			switch (relation) {
			case QueryField.STARTSWITH:
				for (String v : fieldValue)
					if (v != null && v.startsWith(value)) {
						candidate.include(true);
						break;
					}
				break;
			case QueryField.DOESNOTSTARTWITH:
				for (String v : fieldValue)
					if (v != null && !v.startsWith(value)) {
						candidate.include(true);
						break;
					}
				break;
			case QueryField.ENDSWITH:
				for (String v : fieldValue)
					if (v != null && v.endsWith(value)) {
						candidate.include(true);
						break;
					}
				break;
			case QueryField.DOESNOTENDWITH:
				for (String v : fieldValue)
					if (v != null && !v.endsWith(value)) {
						candidate.include(true);
						break;
					}
				break;
			case QueryField.CONTAINS:
				for (String v : fieldValue)
					if (v != null && v.indexOf(value) >= 0) {
						candidate.include(true);
						break;
					}
				break;
			case QueryField.DOESNOTCONTAIN:
				for (String v : fieldValue)
					if (v != null && v.indexOf(value) < 0) {
						candidate.include(true);
						break;
					}
				break;
			case QueryField.WILDCARDS:
				for (String v : fieldValue)
					if (filter.accept(v)) {
						candidate.include(true);
						break;
					}
				break;
			case QueryField.NOTWILDCARDS:
				for (String v : fieldValue)
					if (!filter.accept(v)) {
						candidate.include(true);
						break;
					}
				break;
			}
		}
	}

	private SmartCollection coll;
	private Query query;
	private boolean sorted = false;
	private List<IPostProcessor> postProcessors = new ArrayList<IPostProcessor>(2);
	private IAssetFilter[] filters;
	private final DbManager dbManager;
	private final SortCriterion customSort;
	private final IDbFactory factory;
	private IPeerService peerService;

	public CollectionProcessor(IDbFactory factory, DbManager dbManager, SmartCollection coll, IAssetFilter[] filters,
			SortCriterion customSort) {
		this.factory = factory;
		this.dbManager = dbManager;
		this.peerService = factory.getPeerService();
		this.coll = coll;
		this.filters = filters;
		this.customSort = customSort;
		if (coll != null && coll.getPostProcessor() instanceof IPostProcessor)
			postProcessors.add((IPostProcessor) coll.getPostProcessor());
	}

	private List<Asset> select(SmartCollection scoll, boolean collSort, IAssetFilter[] assetFilters,
			SortCriterion cSort, List<SortCriterion> postponedSorts, boolean testExist) {
		// long timestamp = System.nanoTime();
		boolean dynamic = false;
		boolean isSorted = postponedSorts != null && scoll != null && collSort
				&& (!scoll.getSortCriterion().isEmpty() || cSort != null);
		String ticket = null;
		if (peerService != null && scoll.getNetwork()) {
			postponeSorts(isSorted, scoll, cSort, postponedSorts);
			isSorted = false;
			ticket = peerService.select(scoll, assetFilters);
		}
		try {
			if (query == null || this.sorted != isSorted || (cSort == null && this.customSort != null)
					|| (cSort != null && !cSort.equals(this.customSort))) {
				this.sorted = isSorted;
				ObjectContainer database = dbManager.getDatabase();
				query = database.query();
				query.constrain(Asset.class);
				SmartCollectionImpl tempColl = (SmartCollectionImpl) scoll;
				if (tempColl != null) {
					Constraint conjunction = null;
					Set<String> idSetAnd = null;
					Set<String> idSetOr = null;
					Set<String> idSet = null;
					while (true) {
						Constraint disjunction = null;
						if (coll.getAlbum() && tempColl.getSubSelection().isEmpty() && coll.getAsset().size() > MAXALBUMASSETS)
							disjunction = query.descend("album").constrain(coll.getName()); //$NON-NLS-1$
						else
							for (Criterion crit : tempColl.getCriterion()) {
								if (crit == null)
									return EMPTY;
								int relation = crit.getRelation();
								String field = crit.getField();
								String subfield = crit.getSubfield();
								QueryField qfield = QueryField.findQueryField(field);
								QueryField qsubfield = QueryField.findQuerySubField(field, subfield);
								if (qsubfield == null && relation != QueryField.XREF)
									return EMPTY;
								Object value = crit.getValue();
								Constraint constraint = null;
								if (relation == QueryField.DATEEQUALS || relation == QueryField.DATENOTEQUAL) {
									long t = ((Date) value).getTime() / 1000 * 1000;
									value = new Range(new Date(t), new Date(t + 999));
									relation = (relation == QueryField.DATEEQUALS) ? QueryField.BETWEEN
											: QueryField.NOTBETWEEN;
								} else if (relation == QueryField.SIMILAR) {
									float tolerance = getTolerance(field);
									if (tolerance != 0f) {
										relation = QueryField.BETWEEN;
										value = compileSimilarRange(value, tolerance);
									} else
										relation = QueryField.EQUALS;
								}
								Class<? extends MediaExtension> mediaClazz = null;
								String vField = null;
								for (IMediaSupport support : CoreActivator.getDefault().getMediaSupport()) {
									vField = support.getFieldName(field);
									if (vField != null) {
										mediaClazz = support.getExtensionType();
										break;
									}
								}
								if (mediaClazz != null) {
									Query vQuery = database.query();
									vQuery.constrain(mediaClazz);
									if (relation == QueryField.BETWEEN || relation == QueryField.NOTBETWEEN)
										applyBetween(vQuery, field, vField, relation, ((Range) value).getFrom(),
												((Range) value).getTo());
									else if (relation != QueryField.EQUALS && relation != QueryField.NOTEQUAL
											&& qsubfield.getCard() != 1 && qsubfield.getType() == QueryField.T_STRING)
										vQuery.constrain(
												new StringArrayEvaluation(qsubfield, relation, (String) value));
									else if (relation == QueryField.WILDCARDS)
										vQuery.constrain(new WildcardEvaluation(qsubfield, (String) value, false));
									else if (relation == QueryField.NOTWILDCARDS)
										vQuery.constrain(new WildcardEvaluation(qsubfield, (String) value, true));
									else
										applyRelation(tempColl.getSystem() ? dbManager : null, field, vField, relation,
												value, vQuery.descend(vField).constrain(value), vQuery);
									ObjectSet<? extends MediaExtension> extensionSet = vQuery.execute();
									if (idSet == null)
										idSet = new HashSet<String>(extensionSet.size() * 3 / 2);
									for (MediaExtension extension : extensionSet) {
										Asset parent = extension.getAsset_parent();
										if (parent != null)
											idSet.add(parent.getStringId());
									}
								} else {
									if (qsubfield != null && qsubfield.isVirtual())
										constraint = VirtualQueryComputer.computeConstraint(qsubfield.getKey(), query,
												crit.getValue(), crit.getRelation(), this);
									else if (relation == QueryField.BETWEEN || relation == QueryField.NOTBETWEEN)
										constraint = applyBetween(query, field, field, relation,
												((Range) value).getFrom(), ((Range) value).getTo());
									else if (qsubfield == QueryField.IPTC_CATEGORY) {
										constraint = query.descend(field).constrain(value);
										applyRelation(dbManager, field, field, crit.getRelation(), value, constraint,
												query);
										Category category = dbManager.getMeta(true).getCategory(value.toString());
										if (category != null)
											constraint = addSubcats(dbManager, query, constraint, category, crit);
									} else if (qfield == QueryField.IPTC_LOCATIONSHOWN) {
										dynamic = true;
										List<LocationShownImpl> relations = obtainStructRelations(database,
												LocationShownImpl.class, LocationImpl.class, "location", crit, //$NON-NLS-1$
												value, qsubfield);
										if (relations != null) {
											if (idSet == null)
												idSet = new HashSet<String>(relations.size() * 3 / 2);
											for (LocationShownImpl loc : relations)
												idSet.add(loc.getAsset());
										} else
											idSet = new HashSet<String>();
									} else if (qfield == QueryField.IPTC_LOCATIONCREATED) {
										dynamic = true;
										List<LocationCreatedImpl> relations = obtainStructRelations(database,
												LocationCreatedImpl.class, LocationImpl.class, "location", crit, //$NON-NLS-1$
												value, qsubfield);
										if (relations != null) {
											if (idSet == null)
												idSet = new HashSet<String>(relations.size() * 2);
											for (LocationCreatedImpl loc : relations)
												idSet.addAll(loc.getAsset());
										} else
											idSet = new HashSet<String>();
									} else if (qfield == QueryField.IPTC_CONTACT) {
										dynamic = true;
										List<CreatorsContactImpl> relations = obtainStructRelations(database,
												CreatorsContactImpl.class, ContactImpl.class, "contact", crit, //$NON-NLS-1$
												value, qsubfield);
										if (relations != null) {
											if (idSet == null)
												idSet = new HashSet<String>(relations.size() * 2);
											for (CreatorsContactImpl contact : relations)
												idSet.addAll(contact.getAsset());
										} else
											idSet = new HashSet<String>();
									} else if (qfield == QueryField.IPTC_ARTWORK) {
										dynamic = true;
										List<ArtworkOrObjectShownImpl> relations = obtainStructRelations(database,
												ArtworkOrObjectShownImpl.class, ArtworkOrObjectImpl.class,
												"artworkOrObject", crit, //$NON-NLS-1$
												value, qsubfield);
										if (relations != null) {
											if (idSet == null)
												idSet = new HashSet<String>(relations.size() * 3 / 2);
											for (ArtworkOrObjectShownImpl art : relations)
												idSet.add(art.getAsset());
										} else
											idSet = new HashSet<String>();
									} else if (relation == QueryField.XREF) {
										dynamic = true;
										if (tempColl.getAlbum() && !tempColl.getSubSelection().isEmpty())
											collectAlbumAssetIds(tempColl, idSet = new HashSet<>(311));
										else {
											List<String> assetIds = tempColl.getAsset();
											if (assetIds != null)
												idSet = new HashSet<String>(assetIds);
										}
									} else if (relation == QueryField.UNDEFINED && value instanceof Double) {
										constraint = query.descend(field).constrain(Double.NEGATIVE_INFINITY).greater()
												.equal().not();
									} else if (relation == QueryField.UNDEFINED && value instanceof Date) {
										constraint = query.descend(field).constrain(null);
									} else {
										constraint = applyRelation(tempColl.getSystem() ? dbManager : null, field,
												field, relation, value, query.descend(field).constrain(value), query);
									} // end single criterion processing
								}
								if (idSet != null) {
									if (idSetOr != null) {
										if (crit.getAnd())
											idSetOr.retainAll(idSet);
										else
											idSetOr.addAll(idSet);
										idSet.clear();
									} else {
										idSetOr = idSet;
										idSet = null;
									}
								}
								if (constraint != null) {
									if (disjunction != null)
										constraint = (crit.getAnd()) ? disjunction.and(constraint)
												: disjunction.or(constraint);
									disjunction = constraint;
								}
							} // end criterion loop
						if (disjunction != null)
							conjunction = conjunction != null ? conjunction.and(disjunction) : disjunction;
						if (idSetOr != null) {
							if (idSetAnd != null) {
								idSetAnd.retainAll(idSetOr);
								idSetOr.clear();
							} else {
								idSetAnd = idSetOr;
								idSetOr = null;
							}
						}
						if (tempColl.getSystem() || tempColl.getAlbum())
							break;
						tempColl = (SmartCollectionImpl) tempColl.getSmartCollection_subSelection_parent();
						if (tempColl == null)
							break;
					} // End collection hierarchy loop
					if (assetFilters != null)
						for (IAssetFilter assetFilter : assetFilters) {
							Constraint filterConstraint = assetFilter instanceof AssetFilter
									? ((AssetFilter) assetFilter).getConstraint(dbManager, query)
									: null;
							if (filterConstraint != null)
								conjunction = conjunction == null ? filterConstraint
										: conjunction.and(filterConstraint);
						}
					if (idSetAnd != null) {
						if (conjunction != null || idSetAnd.size() > MAXIDS)
							query.constrain(new IdEvaluation(idSetAnd));
						else {
							query = null;
							if (idSetAnd.isEmpty())
								return testExist ? null : EMPTY;
							List<Asset> assets = new ArrayList<Asset>(idSetAnd.size());
							for (String id : idSetAnd) {
								Query q = database.query();
								q.constrain(AssetImpl.class);
								q.descend(Constants.OID).constrain(id);
								assets.addAll(q.<AssetImpl>execute());
							}
							postponeSorts(isSorted, scoll, cSort, postponedSorts);
							return assets;
						}
					}
					if (isSorted) {
						String customField = (cSort != null) ? cSort.getField() : null;
						boolean hasPostponed = false;
						if (scoll != null && collSort) {
							for (SortCriterion crit : scoll.getSortCriterion())
								if (crit != null) {
									String field = crit.getField();
									if (!field.equals(customField)) {
										postponedSorts.add(crit);
										if (crit.getSubfield() != null || field.startsWith("$")) { //$NON-NLS-1$
											hasPostponed = true;
											break;
										}
									}
								}
							if (!hasPostponed)
								postponedSorts.clear();
						}
						if (cSort != null) {
							if (hasPostponed)
								postponedSorts.add(0, cSort);
							else
								applySort(cSort);
						}
						if (scoll != null && collSort)
							for (SortCriterion crit : scoll.getSortCriterion())
								if (crit != null && !crit.getField().equals(customField)
										&& !postponedSorts.contains(crit))
									applySort(crit);
					}
				}
			}
			// System.out.println(System.nanoTime() - timestamp);
			ObjectSet<Asset> set = null;
			try {
				set = query.execute();
			} catch (NullPointerException e) {
				// happens on empty catalogs
			}
			if (dynamic)
				query = null;
			if (testExist) {
				if (ticket == null)
					return set.hasNext() ? EMPTY : null;
				if (set.hasNext())
					return EMPTY;
				return peerService.getSelect(ticket).isEmpty() ? null : EMPTY;
			}
			if (ticket == null)
				return set == null ? EMPTY : set;
			List<Asset> result = new ArrayList<Asset>(set);
			result.addAll(peerService.getSelect(ticket));
			return result;
		} finally {
			if (ticket != null)
				peerService.discardTask(ticket);
		}
	}

	private void collectAlbumAssetIds(SmartCollection tempColl, Set<String> set) {
		List<String> assetIds = tempColl.getAsset();
		if (assetIds != null)
			set.addAll(assetIds);
		for (SmartCollection child : tempColl.getSubSelection())
			collectAlbumAssetIds(child, set);
	}

	protected static Range compileSimilarRange(Object value, float tolerance) {
		Object from = null;
		Object to = null;
		if (value instanceof Integer) {
			int v = (Integer) value;
			if (tolerance < 0) {
				from = v + (int) tolerance;
				to = v - (int) tolerance;
			} else {
				double low = (100 - tolerance) / 100;
				double high = (100 + tolerance) / 100;
				from = (int) (v * low + 0.5d);
				to = (int) (v * high + 0.5d);
			}
		} else if (value instanceof Double) {
			double v = (Double) value;
			if (tolerance < 0) {
				from = v + tolerance;
				to = v - tolerance;
			} else {
				from = v * (100 - tolerance) / 100;
				to = v * (100 + tolerance) / 100;
			}
		} else if (value instanceof Date) {
			long time = ((Date) value).getTime();
			from = new Date(time + (int) tolerance);
			to = new Date(time - (int) tolerance);
		}
		return new Range(from, to);
	}

	private static void postponeSorts(boolean isSorted, SmartCollection scoll, SortCriterion cSort,
			List<SortCriterion> postponedSorts) {
		if (isSorted) {
			String customField = null;
			if (cSort != null) {
				customField = cSort.getField();
				postponedSorts.add(0, cSort);
			}
			for (SortCriterion crit : scoll.getSortCriterion())
				if (crit != null && !crit.getField().equals(customField))
					postponedSorts.add(crit);
		}
	}

	protected static Constraint applyBetween(Query bq, String path, String field, int relation, Object from,
			Object to) {
		return (relation == QueryField.BETWEEN)
				? bq.descend(field).constrain(to).smaller().equal()
						.and(bq.descend(field).constrain(from).greater().equal())
				: bq.descend(field).constrain(to).greater().or(bq.descend(field).constrain(from).smaller());
	}

	private <T extends IdentifiableObject> List<T> obtainStructRelations(ObjectContainer database, Class<T> relType,
			Class<? extends IdentifiableObject> structType, String field, Criterion crit, Object value,
			QueryField qsubfield) {
		Query subquery = database.query();
		List<T> relations = null;
		String subfield = crit.getSubfield();
		if (subfield == null) {
			subquery.constrain(relType);
			subquery.descend(field).constrain(value);
			relations = subquery.execute();
		} else {
			if (qsubfield == null)
				return null;
			subquery.constrain(structType);
			int relation = crit.getRelation();
			if (relation != QueryField.EQUALS && relation != QueryField.NOTEQUAL && qsubfield.getCard() != 1
					&& qsubfield.getType() == QueryField.T_STRING)
				query.constrain(new StringArrayEvaluation(qsubfield, relation, (String) value));
			else if (relation == QueryField.WILDCARDS)
				subquery.constrain(new WildcardEvaluation(qsubfield, (String) value, false));
			else if (relation == QueryField.NOTWILDCARDS)
				subquery.constrain(new WildcardEvaluation(qsubfield, (String) value, true));
			else {
				String cfield = crit.getField();
				applyRelation(null, cfield, cfield, relation, value, subquery.descend(subfield).constrain(value),
						query);
			}
			List<? extends IdentifiableObject> structures = subquery.execute();
			if (!structures.isEmpty())
				relations = dbManager.obtainParentObjects(relType, field, structures);
		}
		return relations;
	}

	private void applySort(SortCriterion crit) {
		if (crit.getDescending())
			query.descend(crit.getField()).orderDescending();
		else
			query.descend(crit.getField()).orderAscending();
	}

	private static Comparator<Asset> scoreComparator;
	private static Comparator<Asset> nameComparator;

	protected static Constraint applyRelation(DbManager dbManager, String path, String field, int rel, Object value,
			Constraint constraint, Query aQuery) {
		try {
			switch (rel) {
			case QueryField.NOTEQUAL:
				return constraint.not();
			case QueryField.GREATER:
				return constraint.greater();
			case QueryField.NOTGREATER:
				return constrainToPositive(aQuery, path, field, constraint.smaller().equal());
			case QueryField.SMALLER:
				return constrainToPositive(aQuery, path, field, constraint.smaller());
			case QueryField.NOTSMALLER:
				return constraint.greater().equal();
			case QueryField.STARTSWITH:
				if (dbManager != null && dbManager.isIndexed(path)) {
					String s = value.toString();
					int index = s.length() - 1;
					if (index >= 0) {
						char[] chars = s.toCharArray();
						chars[index] = (char) (chars[index] + 1);
						s = new String(chars);
					}
					return constraint.greater().equal().and(aQuery.descend(field).constrain(s).smaller());
				}
				return constraint.startsWith(true);
			case QueryField.DOESNOTSTARTWITH:
				return constraint.startsWith(true).not();
			case QueryField.ENDSWITH:
				return constraint.endsWith(true);
			case QueryField.DOESNOTENDWITH:
				return constraint.endsWith(true).not();
			case QueryField.CONTAINS:
				return constraint.contains();
			case QueryField.DOESNOTCONTAIN:
				return constraint.contains().not();
			}
		} catch (RuntimeException e) {
			// ignore
		}
		return constraint;
	}

	protected static Constraint addSubcats(DbManager dbManager, Query q, Constraint constraint, Category category,
			Criterion crit) {
		String field = crit.getField();
		int relation = crit.getRelation();
		AomMap<String, Category> subCategories = category.getSubCategory();
		if (subCategories != null)
			for (Category subcat : subCategories.values()) {
				if (subcat != null) {
					String label = subcat.getLabel();
					Constraint constraint2 = q.descend(field).constrain(label);
					applyRelation(dbManager, field, field, crit.getRelation(), label, constraint2, q);
					switch (relation) {
					case QueryField.NOTEQUAL:
					case QueryField.DOESNOTSTARTWITH:
					case QueryField.DOESNOTENDWITH:
					case QueryField.DOESNOTCONTAIN:
						constraint = constraint.and(constraint2);
						break;
					case QueryField.EQUALS:
					case QueryField.STARTSWITH:
					case QueryField.ENDSWITH:
					case QueryField.CONTAINS:
						constraint = constraint.or(constraint2);
						break;
					}
					addSubcats(dbManager, q, constraint, subcat, crit);
				}
			}
		return constraint;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.ICollectionProcessor#addPostProcessor(com.bdaum.zoom
	 * .core.IPostProcessor)
	 */

	public void addPostProcessor(IPostProcessor processor) {
		postProcessors.add(processor);
	}

	private float getTolerance(String field) {
		return factory.getTolerance(field);
	}

	protected static Constraint constrainToPositive(Query qe, String path, String field, Constraint constraint) {
		switch (QueryField.findQueryField(path).getType()) {
		case QueryField.T_POSITIVEFLOAT:
		case QueryField.T_CURRENCY:
		case QueryField.T_POSITIVEINTEGER:
		case QueryField.T_POSITIVELONG:
			return constraint.and(qe.descend(field).constrain(0).greater());
		default:
			return constraint;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.ICollectionProcessor#getCollection()
	 */

	public SmartCollection getCollection() {
		return coll;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.ICollectionProcessor#reset()
	 */

	public void reset() {
		coll = null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.ICollectionProcessor#select(boolean)
	 */

	public List<Asset> select(boolean isSorted) {
		return select(isSorted, false);
	}

	public List<Asset> select(boolean isSorted, boolean testExist) {
		// long time = System.currentTimeMillis();
		if (coll == null || coll == EMPTYCOLLECTION)
			return EMPTY;
		if (coll.getCriterion().size() == 1) {
			Criterion criterion = coll.getCriterion(0);
			if (criterion == null)
				return EMPTY;
			if (criterion.getField().startsWith("*")) { //$NON-NLS-1$
				String ticket = (peerService != null && coll.getNetwork()) ? ticket = peerService.select(coll, filters)
						: null;
				try {
					Set<String> preselectedIds = null;
					SmartCollection parentCollection = coll.getSmartCollection_subSelection_parent();
					if (parentCollection != null) {
						List<Asset> preselect = select(parentCollection, isSorted, filters, null, null, testExist);
						preselectedIds = new HashSet<String>();
						for (Asset asset : preselect)
							preselectedIds.add(asset.getStringId());
					}
					List<Asset> result = processContentSearch(criterion, preselectedIds, filters);
					if (ticket != null) {
						result = new ArrayList<Asset>(result);
						result.addAll(peerService.getSelect(ticket));
					}
					if (customSort != null) {
						Asset[] array = result.toArray(new Asset[result.size()]);
						sortAfter(array, customSort);
						return Arrays.asList(array);
					}
					return result;
				} finally {
					if (ticket != null)
						peerService.discardTask(ticket);
				}
			}
		}
		while (true) {
			try {
				List<SortCriterion> postponedSorts = isSorted ? new LinkedList<SortCriterion>() : null;
				List<Asset> set = select(coll, isSorted, filters, customSort, postponedSorts, testExist);
				if (set != null) {
					for (IPostProcessor processor : postProcessors)
						set = processor.process(set);
					if (postponedSorts != null && !postponedSorts.isEmpty())
						set = processPostponedSorts(set, postponedSorts);
				}
				dbManager.resetErrorCount();
				// System.out.println(coll.getName() + ": "
				// + (System.currentTimeMillis() - time));
				return set;
			} catch (DatabaseClosedException e) {
				break;
			} catch (IllegalStateException e) {
				dbManager.reconnectToDb(e);
			} catch (Db4oException e) {
				dbManager.reconnectToDb(e);
			}
		}
		return EMPTY;
	}

	private List<Asset> processPostponedSorts(List<Asset> assets, List<SortCriterion> postponedSorts) {
		Asset[] array = assets.toArray(new Asset[assets.size()]);
		for (int i = postponedSorts.size() - 1; i >= 0; i--)
			sortAfter(array, postponedSorts.get(i));
		return Arrays.asList(array);
	}

	private void sortAfter(Asset[] assets, SortCriterion crit) {
		final QueryField qfield = QueryField.findQueryField(crit.getField());
		final boolean descending = crit.getDescending();
		String subfield = crit.getSubfield();
		if (subfield == null) {
			Arrays.sort(assets, new Comparator<Asset>() {
				@SuppressWarnings("unchecked")
				public int compare(Asset o1, Asset o2) {
					Object v1 = qfield.obtainFieldValue(descending ? o2 : o1);
					Object v2 = qfield.obtainFieldValue(descending ? o1 : o2);
					if (v1 == v2)
						return 0;
					if (v1 == null)
						return -1;
					if (v2 == null)
						return 1;
					if (v1 instanceof int[]) {
						int[] a1 = (int[]) v1;
						int[] a2 = (int[]) v2;
						for (int i = 0; i < Math.min(a1.length, a2.length); i++) {
							if (a1[i] > a2[i])
								return 1;
							if (a1[i] < a2[i])
								return -1;
						}
						return 0;
					}
					if (v1 instanceof double[]) {
						double[] a1 = (double[]) v1;
						double[] a2 = (double[]) v2;
						for (int i = 0; i < Math.min(a1.length, a2.length); i++) {
							if (a1[i] > a2[i])
								return 1;
							if (a1[i] < a2[i])
								return -1;
						}
						return 0;
					}
					if (v1 instanceof String[]) {
						String[] a1 = (String[]) v1;
						String[] a2 = (String[]) v2;
						for (int i = 0; i < Math.min(a1.length, a2.length); i++) {
							int r = a1[i].compareTo(a2[i]);
							if (r != 0)
								return r;
						}
						return 0;
					}
					return (v1 instanceof Comparable<?>) ? ((Comparable<Object>) v1).compareTo(v2) : 0;
				}
			});
		} else {
			// long time = System.currentTimeMillis();
			ObjectContainer database = dbManager.getDatabase();
			query = database.query();
			if (qfield == QueryField.IPTC_LOCATIONCREATED) {
				query.constrain(LocationImpl.class);
				query.descend(subfield).constrain(null).not();
				if (descending)
					query.descend(subfield).orderDescending();
				else
					query.descend(subfield).orderAscending();
				List<LocationImpl> locations = query.execute();
				Map<String, Integer> locationMap = new HashMap<String, Integer>(locations.size() * 3 / 2);
				int i = 0;
				for (LocationImpl location : locations)
					locationMap.put(location.getStringId(), i++);
				query = database.query();
				query.constrain(LocationCreatedImpl.class);
				List<LocationCreatedImpl> locationsCreated = query.execute();
				final Map<String, Integer> assetMap = new HashMap<String, Integer>(locationsCreated.size() * 3 / 2);
				for (LocationCreatedImpl rel : locationsCreated) {
					Integer index = locationMap.get(rel.getLocation());
					if (index != null)
						for (String assedId : rel.getAsset())
							assetMap.put(assedId, index);
				}
				Arrays.sort(assets, new Comparator<Asset>() {
					public int compare(Asset o1, Asset o2) {
						Integer index1 = assetMap.get(o1.getStringId());
						Integer index2 = assetMap.get(o2.getStringId());
						int i1 = (index1 == null) ? Integer.MAX_VALUE : index1.intValue();
						int i2 = (index2 == null) ? Integer.MAX_VALUE : index2.intValue();
						return i1 > i2 ? 1 : (i1 < i2) ? -1 : 0;
					}
				});
			} else if (qfield == QueryField.IPTC_CONTACT) {
				query.constrain(ContactImpl.class);
				query.descend(subfield).constrain(null).not();
				if (descending)
					query.descend(subfield).orderDescending();
				else
					query.descend(subfield).orderAscending();
				List<ContactImpl> contacts = query.execute();
				Map<String, Integer> contactsMap = new HashMap<String, Integer>(contacts.size() * 3 / 2);
				int i = 0;
				for (ContactImpl location : contacts)
					contactsMap.put(location.getStringId(), i++);
				query = database.query();
				query.constrain(CreatorsContactImpl.class);
				List<CreatorsContactImpl> creatorContact = query.execute();
				final Map<String, Integer> assetMap = new HashMap<String, Integer>(creatorContact.size() * 3 / 2);
				for (CreatorsContactImpl rel : creatorContact) {
					Integer index = contactsMap.get(rel.getContact());
					if (index != null)
						for (String assedId : rel.getAsset())
							assetMap.put(assedId, index);
				}
				Arrays.sort(assets, new Comparator<Asset>() {
					public int compare(Asset o1, Asset o2) {
						Integer index1 = assetMap.get(o1.getStringId());
						Integer index2 = assetMap.get(o2.getStringId());
						int i1 = (index1 == null) ? Integer.MAX_VALUE : index1.intValue();
						int i2 = (index2 == null) ? Integer.MAX_VALUE : index2.intValue();
						return i1 > i2 ? 1 : (i1 < i2) ? -1 : 0;
					}
				});
			}
			// System.out.println(System.currentTimeMillis() - time);
		}
	}

	public List<Asset> processContentSearch(Criterion criterion, Set<String> preselectedIds, IAssetFilter[] filters) {
		ILuceneService lucene = factory.getLuceneService();
		String field = criterion.getField().intern();
		if (field == SIMILARITY) {
			SimilarityOptions_typeImpl options = (SimilarityOptions_typeImpl) criterion.getValue();
			float minScore = options.getMinScore();
			String[] keywords = options.getKeywords();
			int keywordWeight = options.getKeywordWeight();
			float keywordFactor;
			Map<String, Asset> kwAssets = null;
			boolean hasKeywords = keywords != null && keywords.length > 0 && keywordWeight > 0;
			if (hasKeywords) {
				keywordFactor = keywordWeight * 0.01f;
				Query query = dbManager.getDatabase().query();
				query.constrain(Asset.class);
				int n = keywords.length;
				Set<String> wantedKeywords = n == 1 ? null : new HashSet<String>(n * 3 / 2);
				Constraint disjunction = null;
				for (String kw : keywords) {
					if (wantedKeywords != null)
						wantedKeywords.add(kw);
					Constraint constraint = query.descend(QueryField.IPTC_KEYWORDS.getKey()).constrain(kw);
					if (disjunction != null)
						constraint = disjunction.or(constraint);
					disjunction = constraint;
				}
				int minNumberOfMatches = (int) Math.ceil(n * minScore);
				int i;
				List<Asset> kwSet = query.execute();
				kwAssets = new HashMap<String, Asset>(3 * kwSet.size());
				for (Asset asset : kwSet) {
					if (preselectedIds == null || preselectedIds.contains(asset.getStringId())) {
						i = 0;
						if (wantedKeywords != null) {
							String[] obtainedKeywords = asset.getKeyword();
							if (obtainedKeywords != null && obtainedKeywords.length >= minNumberOfMatches) {
								for (String okw : obtainedKeywords)
									if (wantedKeywords.contains(okw))
										++i;
							}
						} else
							i = 1;
						if (i >= minNumberOfMatches) {
							boolean accepted = true;
							if (filters != null)
								for (IAssetFilter filter : filters)
									if (!filter.accept(asset)) {
										accepted = false;
										break;
									}
							if (accepted) {
								asset.setScore(keywordFactor * i / n);
								kwAssets.put(asset.getStringId(), asset);
							}
						}
					}
				}
				if (keywordWeight >= 100) {
					List<Asset> assets = new ArrayList<Asset>(kwAssets.values());
					sortByScore(assets);
					if (assets.size() > options.getMaxResults())
						assets = assets.subList(0, options.getMaxResults());
					return assets;
				}
			} else
				keywordFactor = 0f;
			float visualFactor = 1f - keywordFactor;
			File indexPath = dbManager.getIndexPath();
			if (indexPath == null)
				return EMPTY;
			try {
				ISearchHits hits = lucene.search(indexPath, options);
				if (hits == null)
					return EMPTY;
				List<Asset> result = new ArrayList<Asset>(hits.length());
				for (int i = 0; i < hits.length(); i++) {
					float score = hits.score(i);
					if (Float.isNaN(score))
						continue;
					if (score < minScore)
						break;
					String id = hits.getAssetId(i);
					if (preselectedIds == null || preselectedIds.contains(id)) {
						Asset asset = dbManager.obtainAsset(id);
						if (asset != null) {
							boolean accepted = true;
							if (filters != null)
								for (IAssetFilter filter : filters)
									if (!filter.accept(asset)) {
										accepted = false;
										break;
									}
							if (accepted) {
								asset.setScore(visualFactor * score);
								result.add(asset);
							}
						}
					}
				}
				if (kwAssets == null)
					return result;
				for (Asset asset : result) {
					Asset other = kwAssets.get(asset.getStringId());
					if (other == null)
						kwAssets.put(asset.getStringId(), asset);
					else
						other.setScore(other.getScore() + asset.getScore());
				}
				result.clear();
				for (Asset a : kwAssets.values())
					if (a.getScore() >= minScore)
						result.add(a);
				sortByScore(result);
				if (result.size() > options.getMaxResults())
					result = result.subList(0, options.getMaxResults());
				return result;
			} catch (IOException e) {
				DbActivator.getDefault().logError(Messages.CollectionProcessor_io_error_similarity, e);
				return EMPTY;
			}
		} else if (field == TEXTSEARCH) {
			TextSearchOptions_type options = (TextSearchOptions_type) criterion.getValue();
			float minScore = options.getMinScore();
			File indexPath = dbManager.getIndexPath();
			try {
				ISearchHits hits = lucene.search(indexPath, options);
				if (hits == null)
					return EMPTY;
				float maxScore = hits.getMaxScore();
				int l = (preselectedIds == null) ? hits.length() : Math.min(hits.length(), preselectedIds.size());
				List<Asset> result = new ArrayList<Asset>(l);
				for (int i = 0; i < hits.length(); i++) {
					float score = hits.score(i) / maxScore;
					if (score < minScore)
						break;
					String id = hits.getAssetId(i);
					if (preselectedIds == null || preselectedIds.contains(id)) {
						Asset asset = dbManager.obtainAsset(id);
						if (asset != null) {
							boolean accepted = true;
							if (filters != null)
								for (IAssetFilter filter : filters)
									if (!filter.accept(asset)) {
										accepted = false;
										break;
									}
							if (accepted) {
								asset.setScore(score);
								result.add(asset);
							}
						}
					}
				}
				return result;
			} catch (IOException e) {
				DbActivator.getDefault().logError(Messages.CollectionProcessor_io_error_text_search, e);
			} catch (ParseException e) {
				DbActivator.getDefault().logError(Messages.CollectionProcessor_parser_error_text_search, e);
			}
		} else if (field == ORPHANS) {
			@SuppressWarnings("unchecked")
			List<Asset> assets = (List<Asset>) criterion.getValue();
			List<Asset> result = new ArrayList<Asset>(assets.size());
			int rel = criterion.getRelation();
			for (Asset asset : assets)
				if (asset != null) {
					boolean accepted = true;
					if (filters != null)
						for (IAssetFilter filter : filters)
							if (!filter.accept(asset)) {
								accepted = false;
								break;
							}
					if (accepted && rel != QueryField.XREF)
						accepted = dbManager.obtainAsset(asset.getStringId()) != null;
					if (accepted)
						result.add(asset);
				}
			if (nameComparator == null)
				nameComparator = new Comparator<Asset>() {
					public int compare(Asset a1, Asset a2) {
						return a1.getName().compareTo(a2.getName());
					}
				};
			Collections.sort(result, nameComparator);
			criterion.setRelation(0);
			return result;
		}
		return EMPTY;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.ICollectionProcessor#getFilter()
	 */

	private static void sortByScore(List<Asset> foundAssets) {
		if (scoreComparator == null)
			scoreComparator = new Comparator<Asset>() {
				public int compare(Asset a1, Asset a2) {
					return (a1.getScore() == a2.getScore()) ? 0 : (a1.getScore() > a2.getScore()) ? -1 : 1;
				}
			};
		Collections.sort(foundAssets, scoreComparator);

	}

	public IAssetFilter[] getFilters() {
		return filters;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.ICollectionProcessor#getCustomSort()
	 */

	public SortCriterion getCustomSort() {
		return customSort;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.ICollectionProcessor#getScoreFormatter()
	 */

	public IScoreFormatter getScoreFormatter() {
		if (coll != null && coll != EMPTYCOLLECTION) {
			if (coll.getCriterion().size() == 1) {
				Criterion criterion = coll.getCriterion(0);
				if (criterion != null) {
					String field = criterion.getField();
					if (SIMILARITY.equals(field) || TEXTSEARCH.equals(field))
						return percentFormatter;
				}
			}
			for (PostProcessor processor : postProcessors)
				if (processor instanceof GeographicPostProcessor) {
					char unit = factory.getDistanceUnit();
					return new GenericScoreFormatter(2, 1, unit == 'm' ? " mi" : unit == 'n' ? " NM" : " km", //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
							Messages.CollectionProcessor_distance);
				}
		}
		return null;
	}

	public boolean isEmpty() {
		return select(false, true) == null;
	}

}
