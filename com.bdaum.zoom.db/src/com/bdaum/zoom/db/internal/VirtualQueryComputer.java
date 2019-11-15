package com.bdaum.zoom.db.internal;

import java.awt.geom.Point2D;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.db.internal.CollectionProcessor.AspectEvaluation;
import com.bdaum.zoom.db.internal.CollectionProcessor.DateEvaluation;
import com.bdaum.zoom.db.internal.CollectionProcessor.GeographicPostProcessor;
import com.bdaum.zoom.db.internal.CollectionProcessor.TimeOfDayEvaluation;
import com.db4o.query.Constraint;
import com.db4o.query.Query;

public class VirtualQueryComputer {

	private static final double SQRT2 = Math.sqrt(2d);
	private static final double EARTHRADIUS = 6371;
	private static final long MSECINDAY = 1000L * 3600 * 24;
	private static final long HALFDAY = MSECINDAY / 2;
	private static final double RESAT300 = 300d / 2.54d;
	protected static final String[] MODIFIED_SINCE_BASE = new String[] { QueryField.LASTMOD.getKey() };
	protected static final String[] EXIF_GPSLOCATIONDISTANCE_BASEDON = new String[] {
			QueryField.EXIF_GPSLATITUDE.getKey(), QueryField.EXIF_GPSLONGITUDE.getKey() };
	protected static final String[] PHYSICALWIDTH_BASEDON = new String[] { QueryField.WIDTH.getKey() };
	protected static final String[] PHYSICALHEIGHT_BASEDON = new String[] { QueryField.HEIGHT.getKey() };

	private static Map<String, VirtualQueryComputer> map = new HashMap<>();

	static {
		configure();
	}

	private static void configure() {
		map.put(QueryField.MODIFIED_SINCE.getKey(), new VirtualQueryComputer() {
			@Override
			protected Constraint computeConstraint(Query query, Object value,  Object vto, int rel, CollectionProcessor processor) {
				long time = System.currentTimeMillis();
				String field = QueryField.LASTMOD.getKey();
				if (rel == QueryField.BETWEEN || rel == QueryField.NOTBETWEEN)
					return CollectionProcessor.applyBetween(query, field, field, rel,
							new Date(Math.max(0, time - (Integer) value * MSECINDAY + HALFDAY)),
							new Date(Math.max(0, time - (Integer) vto * MSECINDAY - HALFDAY)));
				int days = (Integer) value;
				switch (rel) {
				case QueryField.EQUALS:
					return CollectionProcessor.applyBetween(query, field, field, QueryField.BETWEEN,
							new Date(Math.max(0, time - days * MSECINDAY + HALFDAY)),
							new Date(Math.max(0, time - Math.max(0, days * MSECINDAY - HALFDAY))));
				case QueryField.NOTEQUAL:
					return CollectionProcessor.applyBetween(query, field, field, QueryField.NOTBETWEEN,
							new Date(Math.max(0, time - days * MSECINDAY + HALFDAY)),
							new Date(Math.max(0, time - Math.max(0, days * MSECINDAY - HALFDAY))));
				case QueryField.GREATER:
					return query.descend(QueryField.LASTMOD.getKey())
							.constrain(new Date(Math.max(0, time - Math.max(0, days * MSECINDAY - HALFDAY)))).smaller();
				case QueryField.NOTSMALLER:
					return query.descend(QueryField.LASTMOD.getKey())
							.constrain(new Date(Math.max(0, time - Math.max(0, days * MSECINDAY - HALFDAY)))).smaller()
							.equal();
				case QueryField.SMALLER:
					return query.descend(QueryField.LASTMOD.getKey())
							.constrain(new Date(Math.max(0, time - days * MSECINDAY + HALFDAY))).greater();
				case QueryField.NOTGREATER:
					return query.descend(QueryField.LASTMOD.getKey())
							.constrain(new Date(Math.max(0, time - days * MSECINDAY + HALFDAY))).greater().equal();
				}
				return null;
			}

			@Override
			protected String[] getBasedOn() {
				return MODIFIED_SINCE_BASE;
			}
		});

		map.put(QueryField.EXIF_GPSLOCATIONDISTANCE.getKey(), new VirtualQueryComputer() {
			// Supports only NOTGREATER
			@Override
			protected Constraint computeConstraint(Query query, Object value,  Object vto, int rel, CollectionProcessor processor) {
				Object[] values = (Object[]) value;
				Point2D.Double p1 = locationFrom((Double) values[0], (Double) values[1], (Double) values[2] * SQRT2,
						45d, Core.getCore().getDbFactory().getDistanceUnit());
				double latmax = Math.min(90, p1.x);
				double latmin = Math.max(-90, 2 * (Double) values[0] - p1.x);
				double lonmax = p1.y;
				double lonmin = 2 * (Double) values[1] - lonmax;
				Constraint latConstraint = query.descend(QueryField.EXIF_GPSLATITUDE.getKey()).constrain(latmin)
						.smaller().not();
				latConstraint = query.descend(QueryField.EXIF_GPSLATITUDE.getKey()).constrain(latmax).greater().not()
						.and(latConstraint);
				Constraint lonConstraint = query.descend(QueryField.EXIF_GPSLONGITUDE.getKey()).constrain(lonmin)
						.smaller().not();
				lonConstraint = query.descend(QueryField.EXIF_GPSLONGITUDE.getKey()).constrain(lonmax).greater().not()
						.and(lonConstraint);
				if (lonmin < 0)
					lonConstraint = lonConstraint.or(query.descend(QueryField.EXIF_GPSLONGITUDE.getKey())
							.constrain(lonmin + 360).smaller().not());
				if (lonmax > 360)
					lonConstraint = lonConstraint.or(query.descend(QueryField.EXIF_GPSLONGITUDE.getKey())
							.constrain(lonmax - 360).greater().not().and(lonConstraint));
				processor.addPostProcessor(new GeographicPostProcessor((Double) values[0], (Double) values[1],
						(Double) values[2], values.length > 3 ? (Character) values[3] : 0));
				return latConstraint.and(lonConstraint);
			}

			private Point2D.Double locationFrom(double lat1, double lon1, double dist, double azimuth, char unit) {
				double b = Core.toKm(dist, unit) / EARTHRADIUS;
				double az = Math.toRadians(azimuth);
				double l1 = Math.toRadians(90 - lat1);
				double sin_b = Math.sin(b);
				double a = Math.acos(Math.cos(b) * Math.cos(l1) + Math.sin(l1) * sin_b * Math.cos(az));
				return new Point2D.Double(90 - Math.toDegrees(a),
						Math.toDegrees(Math.asin(sin_b * Math.sin(az) / Math.sin(a))) + lon1);
			}

			@Override
			protected String[] getBasedOn() {
				return EXIF_GPSLOCATIONDISTANCE_BASEDON;
			}
		});
		map.put(QueryField.PHYSICALHEIGHT.getKey(), new VirtualQueryComputer() {
			@Override
			protected Constraint computeConstraint(Query query, Object value,  Object vto, int rel, CollectionProcessor processor) {
				String field = QueryField.HEIGHT.getKey();
				if (rel == QueryField.BETWEEN || rel == QueryField.NOTBETWEEN)
					return CollectionProcessor.applyBetween(query, field, field, rel,
							(Double) value * RESAT300, (Double) vto * RESAT300);
				return CollectionProcessor.applyRelation(null, field, field, rel, value,
						query.descend(field).constrain(((Double) value) * 300 / 2.54d), query);
			}

			@Override
			protected String[] getBasedOn() {
				return PHYSICALHEIGHT_BASEDON;
			}

		});
		map.put(QueryField.PHYSICALWIDTH.getKey(), new VirtualQueryComputer() {
			@Override
			protected Constraint computeConstraint(Query query, Object value,  Object vto, int rel, CollectionProcessor processor) {
				String field = QueryField.WIDTH.getKey();
				if (rel == QueryField.BETWEEN || rel == QueryField.NOTBETWEEN)
					return CollectionProcessor.applyBetween(query, field, field, rel,
							(Double) value * RESAT300, (Double) vto * RESAT300);
				return CollectionProcessor.applyRelation(null, field, field, rel, value,
						query.descend(field).constrain(((Double) value) * 300 / 2.54d), query);
			}

			@Override
			protected String[] getBasedOn() {
				return PHYSICALWIDTH_BASEDON;
			}

		});
		map.put(QueryField.ASPECTRATIO.getKey(), new VirtualQueryComputer() {
			@Override
			protected Constraint computeConstraint(Query query, Object value,  Object vto, int rel, CollectionProcessor processor) {
				return query.constrain(new AspectEvaluation(((Double) value), Double.NaN, rel));
			}
		});
		map.put(QueryField.TIMEOFDAY.getKey(), new VirtualQueryComputer() {
			@Override
			protected Constraint computeConstraint(Query query, Object value,  Object vto, int rel, CollectionProcessor processor) {
				return query.constrain(new TimeOfDayEvaluation(((Integer) value), -1, rel));
			}
		});

		map.put(QueryField.DATE.getKey(), new VirtualQueryComputer() {
			@Override
			protected Constraint computeConstraint(Query query, Object value,  Object vto, int rel, CollectionProcessor processor) {
				return query.constrain(new DateEvaluation(((Date) value), null, rel));
			}
		});
	}

	protected Constraint computeConstraint(Query query, Object value,  Object vto, int rel, CollectionProcessor processor) {
		return null;
	}

	protected String[] getBasedOn() {
		return null;
	}

	protected static Constraint computeConstraint(String key, Query query, Object value,  Object vto, int relation,
			CollectionProcessor processor) {
		VirtualQueryComputer vvc = map.get(key);
		return vvc != null ? vvc.computeConstraint(query, value, vto, relation, processor) : null;
	}

	protected static String[] getBasedOn(String key) {
		VirtualQueryComputer vvc = map.get(key);
		return vvc != null ? vvc.getBasedOn() : null;
	}

}
