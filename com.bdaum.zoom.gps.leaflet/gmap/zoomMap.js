//LEAFLET
var map;
var infowindow;
var currentMarker;
var dbl;
var batch = [];
var arrowBatch = [];
var diagonal = 1;
var camCount = 0;
var lmarker;
var cmarker;
var dmarker;
var smarker;
var draggedMarker;
var selected;
var mgr;
var areaCircle;

function markerDragStart(event) {
	draggedMarker = event.target;
}

function markerDragEnd(event) {
	if (draggedMarker) {
		try {
			draggedMarker.closePopup();
			var markerPoint = draggedMarker.getLatLng();
			if (draggedMarker == lmarker) {
				sendPosition('drag', markerPoint, map.getZoom(), 0);
				camCount = 1;
				sendCamCount();
			} else if (draggedMarker == cmarker) {
				sendPosition('drag', markerPoint, map.getZoom(), 1);
				applyCameraSet(markerPoint);
				releaseCamera();
				setMarkers();
			} else if (draggedMarker == dmarker) {
				sendPosition('drag', markerPoint, map.getZoom(), 2);
				applyDirectionSet(markerPoint);
				releaseDirection();
				setMarkers();
			} else if (draggedMarker == smarker) {
				var uuid = applyShownSet(markerPoint);
				sendPosition('drag', markerPoint, map.getZoom(), 3, uuid);
				releaseLocationShown();
				setMarkers();
			}
		} finally {
			draggedMarker = null;
		}
	}
}

function MapClickHandler(event) {
	if (selected) {
		selected = null;
		sendSelection();
		setMarkers();
	}
}

function setupMap() {
	var htmlElement = document.getElementById("map");
	htmlElement.innerHTML = mapIsLoading;
	var center = initialPosition ? initialPosition : L.latLng(49.785556,
			9.038611);
	map = L.map("map", {
		center : center,
		zoom : initialDetail,
		layers : null,
		minZoom : 1
	});
	L.tileLayer.provider(selectedProvider).addTo(map);
	new L.Control.MiniMap(L.tileLayer.provider(selectedProvider), {
		toggleDisplay : true
	}).addTo(map);
	if (initialPosition)
		sendMapPosition();
	map.on('moveend', function() {
		sendPosition('moved', map.getCenter(), map.getZoom(), 0);
	});
	map.on('click', MapClickHandler);
	map.on('resize', ChangeViewHandler);
	map.on('zoomend', ChangeViewHandler);
	L.DomEvent.on(htmlElement, 'keypress', function(event) {
		if (draggedMarker) {
			var code = event.keyCode;
			if (code == 27) {
				if (lmarker === draggedMarker)
					releaseLocation();
				else if (cmarker === draggedMarker) {
					releaseCamera();
				} else if (dmarker === draggedMarker) {
					releaseDirection();
				} else if (smarker === draggedMarker)
					releaseLocationShown();
				draggedMarker = null;
			}
		}
	});
	performDrawings(false);
}

function sendMapPosition() {
	var point = map.getCenter();
	sendMessage('pos', point.lat + ',' + point.lng + "&" + map.getZoom() );
}

function ChangeViewHandler(event) {
	performDrawings(true);
}

function performDrawings(redraw) {
	var bounds = map.getBounds();
	diagonal = distance(bounds.getNorthEast(), bounds.getSouthWest());
	if (track.length > 0) {
		setTrack();
		if (!redraw)
			setBoundsForShape(track);
	} else {
		setMarkers();
		if (!redraw && (locCreated.length > 0 || locShown.length > 0))
			setBoundsForShape(locCreated.concat(locShown));
	}
	sendMapPosition();
}

function distance(coord1, coord2) {
	var degToRad = Math.PI / 180.0;
	var phi1 = degToRad * coord1.lat;
	var phi2 = degToRad * coord2.lat;
	var lam1 = degToRad * coord1.lng;
	var lam2 = degToRad * coord2.lng;
	return 6371.01 * Math.acos(Math.sin(phi1) * Math.sin(phi2) + Math.cos(phi1)
			* Math.cos(phi2) * Math.cos(lam2 - lam1));
}

function coord(coord, dist, bearing) {
	var degToRad = Math.PI / 180.0;
	var radToDeg = 180.0 / Math.PI;
	var brg = degToRad * ((bearing + 360) % 360);
	dist = dist / 6371.01;
	var lat1 = degToRad * coord.lat;
	var lon1 = degToRad * coord.lng;
	var lat2 = Math.asin(Math.sin(lat1) * Math.cos(dist) + Math.cos(lat1)
			* Math.sin(dist) * Math.cos(brg));
	var lon2 = lon1
			+ Math.atan2(Math.sin(brg) * Math.sin(dist) * Math.cos(lat1), Math
					.cos(dist)
					- Math.sin(lat1) * Math.sin(lat2));
	lat2 = radToDeg * lat2;
	lon2 = radToDeg * lon2;
	lon2 = (lon2 + 540) % 360 - 180;
	return L.latLng(lat2, lon2);
}

function unloadMap() {
}

function sendPosition(name, point, zoom, type, uuid) {
	sendMessage(name, point.lat + ',' + point.lng + '&' + zoom + '&'
			+ type + (uuid ? '&' + uuid : ""));
}

function debug(data) {
	sendMessage('debug', data);
}

function sendCamCount() {
	sendMessage('camCount', camCount);
}

function sendSelection() {
	sendMessage('select', selected);
}

function sendMessage(name, data) {
	window.location.href = 'http://www.bdaum.de/zoom/gmap/'
			+ (data ? name + '?' + data : name);
}

function setMarkers() {
	clearMarkers();
	if (locCreated.length > 0 || locShown.length > 0) {
		if (!mgr) {
			mgr = L.markerClusterGroup();
			map.addLayer(mgr);
		}
		var i, markerLocation, icon, marker, bearing;
		for (i = 0; i < locCreated.length; i++) {
			markerLocation = locCreated[i];
			bearing = imgDirection ? imgDirection[i] : NaN;
			icon = L.icon({
				iconUrl : selected == locImage[i].join() ? primaryIconSelUrl
						: primaryIconUrl,
				iconAnchor : [ 12, 38 ]
			});
			marker = L.marker(markerLocation, {
				icon : icon,
				title : locTitles[i],
				draggable : true
			});
			marker.on('dblclick', makeDoubleclickCallback(marker, locImage[i]));
			marker.on('click', makeClickCallback(marker, markerLocation,
					locImage[i]));
			marker.on('dragstart', markerDragStart);
			marker.on('dragend', makeDragEndCallback(marker, locImage[i],
					bearing, i));
			mgr.addLayer(marker);
			batch.push(marker);
			++camCount;
		}
		for (i = 0; i < locShown.length; i++) {
			markerLocation = locShown[i];
			icon = L.icon({
				iconUrl : selected == locShownImage[i] ? secondaryIconSelUrl
						: secondaryIconUrl,
				iconAnchor : [ 10, 32 ]
			});
			marker = L.marker(markerLocation, {
				icon : icon,
				title : locShownTitles[i],
				draggable : true
			});
			marker.on('dblclick', makeDoubleclickCallback(marker,
					locShownImage[i]));
			marker.on('click', makeClickCallback(marker, markerLocation,
					locShownImage[i]));
			marker.on('dragstart', markerDragStart);
			marker.on('dragend', makeDragEndCallback(marker, locShownImage[i],
					NaN, -1));
			mgr.addLayer(marker);
			batch.push(marker);
		}
		for (i = 0; i < locCreated.length; i++) {
			markerLocation = locCreated[i];
			bearing = imgDirection ? imgDirection[i] : NaN;
			if (bearing !== bearing) 
				arrowBatch.push(null);
			else if (!isClustered(mgr, batch[i]))
				arrowBatch.push(createArrow(markerLocation, bearing));
		}
	}
	sendCamCount();
}

function isClustered(mgr, marker) {
	var parent = mgr.getVisibleParent(marker);
	if (parent)
		return parent !== marker && parent.getChildCount() > 1;
}

function createArrow(markerLocation, bearing) {
	return L.polyline(computeArrowOutline(markerLocation, bearing), {
		color : 'black'
	}).addTo(map);
}

function computeArrowOutline(markerLocation, bearing) {
	var dist = diagonal / 12.0;
	var arrowLength = dist / 6.0;
	var acoord = coord(markerLocation, dist, bearing);
	var ah1 = coord(acoord, arrowLength, bearing - 150);
	var ah2 = coord(acoord, arrowLength, bearing + 150);
	return [ markerLocation, acoord, ah1, acoord, ah2 ];
}

function clearMarkers() {
	if (mgr)
		mgr.clearLayers();
	camCount = 0;
	batch = [];
	for (i = 0; i < arrowBatch.length; i++)
		if (arrowBatch[i])
			arrowBatch[i].remove();
	arrowBatch = [];
}

function setBoundsForShape(positions) {
	if (positions.length > 0) {
		var bounds;
		for (var i = 0; i < positions.length; i++) {
			var location = positions[i];
			if (bounds)
				bounds.extend(location);
			else
				bounds = L.latLngBounds(location, location);
		}
		if (bounds.getNorthEast().equals(bounds.getSouthWest()))
			setCenter(bounds.getNorthEast(), map.getZoom());
		else
			setViewPort(bounds);
	}
}

function setTrack() {
	L.polyline(track, {
		color : 'red'
	}).addTo(map);
}

function makeDoubleclickCallback(marker, ids) {
	return function() {
		dbl = true;
		if (infowindow) {
			marker.closePopup().unbindPopup();
			infowindow = null;
		}
		if (!currentMarker) {
			setSelection(ids);
			if (typeof (ids) === "string") {
				if (ids != "shown=") {
					currentMarker = marker;
					sendMessage('info', ids);
				}
			} else if (ids.length > 0) {
				currentMarker = marker;
				sendMessage('info', ids.join());
			}
		}
	};
}

function makeDragEndCallback(marker, ids, bearing, i) {
	return function() {
		if (draggedMarker) {
			var markerPoint = draggedMarker.getLatLng();
			if (typeof (ids) === "string") 
				locShown[i] = markerPoint;
			else 
				locCreated[i] = markerPoint;
			setSelection(ids);
			sendPosition('modify', markerPoint, map.getZoom(), 0,
					typeof (ids) === "string" ? ids : ids.join());
			if (i >= 0 && arrowBatch[i]) 
				arrowBatch[i].setLatLngs(computeArrowOutline(markerPoint,
						bearing));
			draggedMarker = null;
		}
	};
}

function showInfo(html) {
	infowindow = html;
	var marker = currentMarker;
	if (currentMarker !== null) {
		currentMarker.addTo(map);
		currentMarker.on('popupclose', function() {
			marker.remove(map);
		});
		currentMarker.bindPopup(infowindow, {
			offset : L.point(3, -30)
		}).openPopup();
		currentMarker = null;
	}
}

function makeClickCallback(marker, position, ids) {
	return function() {
		marker.closePopup();
		dbl = false;
		setTimeout(function() {
			clickCallback(position, ids);
		}, 300);
	};
}

function clickCallback(position, ids) {
	setSelection(ids);
	if (!dbl) {
		map.zoomIn(map.getZoom() < 10 ? 2 : 1);
		map.panTo(position);
		sendMapPosition();
	}
}

function setSelection(ids) {
	selected = (typeof (ids) === "string") ? ids : ids.join();
	sendSelection();
	setMarkers();
}

function locationPin() {
	if (draggedMarker)
		return;
	releaseLocation();
	lmarker = createPin(pinUrl);
}

function createPin(url) {
	var icon = L.icon({
		iconUrl : url,
		iconSize : L.point(30, 48),
		iconAnchor : L.point(15, 48)
	});
	var marker = L.marker(map.getCenter(), {
		icon : icon,
		title : newLocationTitle,
		draggable : true
	}).addTo(map);
	marker.on('dragstart', markerDragStart);
	marker.on('dragend', markerDragEnd);
	marker.bindPopup(newLocationTitle, {
		offset : L.point(3, -40)
	}).openPopup();
	return marker;
}

function releaseLocation() {
	if (lmarker) {
		lmarker.remove();
		lmarker = null;
		draggedMarker = null;
	}
	camCount = locCreated.length;
	sendCamCount();
}

function cameraPin() {
	if (draggedMarker)
		return;
	releaseCamera();
	cmarker = createPin(camPinUrl);
}

function releaseCamera() {
	if (cmarker) {
		cmarker.remove();
		cmarker = null;
		draggedMarker = null;
	}
	camCount = locCreated.length;
	sendCamCount();
}

function direction() {
	if (draggedMarker)
		return;
	releaseDirection();
	dmarker = createPin(dirPinUrl);
}

function locationShown() {
	if (draggedMarker)
		return;
	releaseLocationShown();
	smarker = createPin(shownPinUrl);
}

function releaseDirection() {
	if (dmarker) {
		dmarker.remove();
		dmarker = null;
		draggedMarker = null;
	}
}

function releaseLocationShown() {
	if (smarker) {
		smarker.remove();
		smarker = null;
		draggedMarker = null;
	}
}

function setViewPort(bounds) {
	map.fitBounds(bounds);
	sendMapPosition();
}

function setCenter(pos, zoom) {
	map.setView(pos, zoom);
	sendMapPosition();
}

function applyCameraSet(pos) {
	locCreated = [ pos ];
	locTitles = [ concatTitles(locTitles) ];
	selected = flatten(locImage);
	locImage = [ selected ];
	imgDirection = [];
	if (locShown && locShown.length > 0) 
		imgDirection.push(bearing(pos, locShown[0]));
	else 
		imgDirection.push(NaN);
}

function concatTitles(array) {
	var l = array.length;
	if (l == 0)
		return "";
	if (l == 1)
		return array[0];
	var newTitle = "";
	for (var i = 0; i < array.length; i++) {
		var titles = array[i].split(",");
		for (var j = 0; j < titles.length; j++) {
			var t = titles[j];
			if (newTitle != "" && newTitle.length + t.length > 40 || t == "...")
				return newTitle + ",...";
			if (newTitle != "")
				newTitle += ",";
			newTitle += t;
		}
	}
	return newTitle;
}

function flatten(array) {
	var result = [];
	for (var i = 0; i < array.length; i++) {
		var subArray = array[i];
		for (var j = 0; j < subArray.length; j++) 
			result.push(subArray[j]);
	}
	return result;
}

function bearing(from, to) {
	var phi1 = from.lat / 180.0 * Math.PI;
	var phi2 = to.lat / 180.0 * Math.PI;
	var lam1 = from.lng / 180.0 * Math.PI;
	var lam2 = to.lng / 180.0 * Math.PI;
	var angle = Math.atan2(Math.sin(lam2 - lam1) * Math.cos(phi2), Math
			.cos(phi1)
			* Math.sin(phi2)
			- Math.sin(phi1)
			* Math.cos(phi2)
			* Math.cos(lam2 - lam1));
	return (angle * 180.0 / Math.PI + 360.0) % 360;
}

function applyShownSet(pos) {
	locShown.push(pos);
	if (locTitles.length > 0) 
		locShownTitles.push(locTitles[0]);
	else 
		locShownTitles.push("");
	var uuid = generateQuickGuid();
	selected = "shown=" + uuid;
	locShownImage.push(selected);
	applyDirectionSet(pos);
	return uuid;
}

function applyDirectionSet(pos) {
	if (imgDirection.length > 0 && locCreated.length > 0) 
		imgDirection[0] = bearing(locCreated[0], pos);
}

function generateQuickGuid() {
	return Math.random().toString(36).substring(2, 15)
			+ Math.random().toString(36).substring(2, 15);
}

function deleteSelected() {
	if (selected) {
		var p = findItem(locImage, selected);
		if (p >= 0) {
			batch = removeItem(batch, p);
			locCreated = removeItem(locCreated, p);
			locTitles = removeItem(locTitles, p);
			locImage = removeItem(locImage, p);
			if (imgDirection)
				imgDirection = removeItem(imgDirection, p);
			selected = null;
			setMarkers();
		} else {
			p = findItem(locShownImage, selected);
			if (p >= 0) {
				locShown = removeItem(locShown, p);
				locShownTitles = removeItem(locShownTitles, p);
				locShownImage = removeItem(locShownImage, p);
				selected = null;
				setMarkers();
			}
		}
	}
}

function removeItem(array, index) {
	var newArray = [];
	for (var i = 0; i < array.length; i++) 
		if (i !== index) 
			newArray.push(array[i]);
	return newArray;
}

function findItem(array, item) {
	for (var i = 0; i < array.length; i++) 
		if (item == array[i]) 
			return i;
	return -1;
}

function setAreaCircle(position, diameter) {
	if (areaCircle) {
		areaCircle.setLatLng(position);
		areaCircle.setRadius(diameter / 2);
	} else {
		areaCircle = L.circle(position, {
			stroke: true,
			color : '#00FF00',
			opacity : 0.8,
			weight : 2,
			fill: true,
			fillColor : '#00FF00',
			fillOpacity : 0.35,
			radius : diameter / 2,
			draggable : true
		});
		areaCircle.addTo(map);
		areaCircle.on('dragend', function() {
			sendPosition('modify', areaCircle.getLatLng(), map.getZoom(), 4);
		});
	}
}

