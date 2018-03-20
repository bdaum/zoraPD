package com.bdaum.zoom.ui.internal.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Rectangle;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.Region;

public class ImageRegion {

	public Rectangle area;
	public String regionId;
	public String name;
	public Asset owner;
	public String type;

	public ImageRegion(Rectangle area, String regionId, String type, String name, Asset owner) {
		this.area = area;
		this.regionId = regionId;
		this.type = type;
		this.name = name;
		this.owner = owner;
	}
	
	public static ImageRegion getBestRegion(ImageRegion[] regions, String type, boolean all, int x, int y) {
		ImageRegion foundRegion = null;
		if (regions != null) {
			int minD = Integer.MAX_VALUE;
			for (ImageRegion imageRegion : regions) {
				if ((all || imageRegion.name == null || imageRegion.name.equals("?")) //$NON-NLS-1$
						&& (type == null  || type == Region.type_face && imageRegion.type == null || type.equals(imageRegion.type))) {
					int d = imageRegion.getDistanceFromRegionCenter(x, y);
					if (d < minD) {
						minD = d;
						foundRegion = imageRegion;
					}
				}
			}
		}
		return foundRegion;
	}
	
	public static ImageRegion[] extractMatchingRegions(ImageRegion[] regions, int x, int y) {
		List<ImageRegion> result = new ArrayList<ImageRegion>(regions.length);
		for (ImageRegion imageRegion : regions)
			if (imageRegion.contains(x, y))
				result.add(imageRegion);
		return result.toArray(new ImageRegion[result.size()]);
	}


	private boolean contains(int x, int y) {
		if (area.height < 0)
			return getDistanceFromRegionCenter(x, y) < Integer.MAX_VALUE;
		return area.contains(x, y);
	}

	public int getDistanceFromRegionCenter(int x, int y) {
		if (area.height < 0) {
			int r = area.width / 2;
			int dx = area.x - x;
			int dy = area.y - y;
			int d = dx * dx + dy * dy;
			if (d <= r * r)
				return d;
		} else if (area.contains(x, y)) {
			int dx = area.x + area.width / 2 - x;
			int dy = area.y + area.height / 2 - y;
			return dx * dx + dy * dy;
		}
		return Integer.MAX_VALUE;
	}

}
