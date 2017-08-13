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
var draggedMarker;

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
				camCount = locCreated.length === 0 ? 1 : locCreated.length;
				sendCamCount();
				freezeMarkers();
			} else if (draggedMarker == dmarker) {
				sendPosition('drag', markerPoint, map.getZoom(), 2);
				freezeMarkers();
			}
		} finally {
			draggedMarker = null;
		}
	}
}

function freezeMarkers() {
	for (var i = 0; i < batch.length; i++)
		batch[i].dragging.disable();
}

// Setup Map
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
		sendPosition('pos', map.getCenter(), map.getZoom(), 0);
	map.on('moveend', function() {
		sendPosition('moved', map.getCenter(), map.getZoom(), 0);
	});
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
				} else if (dmarker === draggedMarker)
					releaseDirection();
				draggedMarker = null;
			}
		}
	});
	performDrawings(false);
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
		if (!redraw)
			setBoundsForShape(locCreated);
	}
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

function sendPosition(name, point, zoom, type) {
	sendMessage(name, '(' + point.lat + ',' + point.lng + ")&" + zoom + '&'
			+ type);
}

function debug(data) {
	sendMessage('debug', data);
}

function sendCamCount() {
	sendMessage('camCount', camCount);
}

function sendMessage(name, data) {
	window.location.href = 'http://www.bdaum.de/zoom/gmap/'
			+ (data ? name + '?' + data : name);
}

// Set markers
function setMarkers() {
	clearMarkers();
	if (locCreated.length > 0) {
		var mgr = L.markerClusterGroup();
		for (var i = 0; i < locCreated.length; i++) {
			var markerLocation = locCreated[i];
			var bearing = imgDirection ? imgDirection[i] : NaN;
			var icon = L.MakiMarkers.icon({
				icon : "camera",
				color : "#ee0000",
				size : "m"
			});
			var marker = L.marker(markerLocation, {
				icon : icon,
				title : locTitles[i],
				draggable : true
			});
			marker.on('dblclick', makeDoubleclickCallback(marker, locImage[i]));
			marker.on('click', makeClickCallback(marker, markerLocation));
			marker.on('dragstart', markerDragStart);
			marker.on('dragend', makeDragEndCallback(marker, locImage[i],
					bearing, arrowBatch.length));
			mgr.addLayer(marker);
			batch.push(marker);
			++camCount;
		}
		map.addLayer(mgr);
		for (var i = 0; i < locCreated.length; i++) {
			var markerLocation = locCreated[i];
			var bearing = imgDirection ? imgDirection[i] : NaN;
			if (bearing !== bearing) {
				// skip NaN
			} else if (!isClustered(mgr, batch[i]))
				arrowBatch.push(createArrow(markerLocation, bearing));
		}
	}
	sendCamCount();
}

function isClustered(mgr, marker) {
	var parent = mgr.getVisibleParent(marker);
	if (parent)
		return parent !== marker && parent.getChildCount() > 1;
	return true;
}

function createArrow(markerLocation, bearing) {
	var dist = diagonal / 12.0;
	var arrowLength = dist / 6.0;
	var acoord = coord(markerLocation, dist, bearing);
	var ah1 = coord(acoord, arrowLength, bearing - 150);
	var ah2 = coord(acoord, arrowLength, bearing + 150);
	var line = L.polyline([ markerLocation, acoord, ah1, acoord, ah2 ], {
		color : 'black'
	}).addTo(map);
	return line;
}

function clearMarkers() {
	camCount = 0;
	for (var i = 0; i < batch.length; i++)
		if (batch[i])
			batch[i].remove();
	batch = [];
	for (i = 0; i < arrowBatch.length; i++)
		if (arrowBatch[i])
			arrowBatch[i].remove();
	arrowBatch = [];
}

function setBoundsForShape(shape) {
	if (shape.length > 0) {
		var bounds;
		for (var i = 0; i < shape.length; i++) {
			var location = shape[i];
			if (bounds)
				bounds.extend(location);
			else
				bounds = L.latLngBounds(location, location);
		}
		if (bounds && !bounds.getNorthEast().equals(bounds.getSouthWest()))
			setViewPort(bounds);
	}
}

function setTrack() {
	L.polyline(track, {
		color : 'red'
	}).addTo(map);
}

function makeDoubleclickCallback(marker, imageAssetIds) {
	return function() {
		dbl = true;
		if (infowindow) {
			marker.closePopup().unbindPopup();
			infowindow = null;
		}
		if (imageAssetIds.length > 0 && !currentMarker) {
			currentMarker = marker;
			sendMessage('info', arrayToString(imageAssetIds));
		}
	};
}

function arrayToString(arr) {
	var result = '';
	for (var i = 0; i < arr.length; i++) {
		if (i > 0)
			result += ',';
		result += arr[i];
	}
	return result;
}

function makeDragEndCallback(marker, imageAssetIds, bearing, arrowIndex) {
	return function() {
		if (draggedMarker) {
			var markerPoint = draggedMarker.getLatLng();
			sendPosition('modify', markerPoint, map.getZoom(),
					arrayToString(imageAssetIds));
			if (bearing !== bearing) {
				// skip NaN
			} else {
				arrowBatch[arrowIndex].remove();
				arrowBatch[arrowIndex] = createArrow(markerPoint, bearing);
			}
			draggedMarker = null;
		}
	};
}

function showInfo(html) {
	infowindow = html;
	if (currentMarker != null)
		currentMarker.bindPopup(infowindow).openPopup();
	currentMarker = null;
}

/*
 * Delayed execution to inhibit firing in case of double click
 */
function makeClickCallback(marker, position) {
	return function() {
		marker.closePopup();
		dbl = false;
		setTimeout(function() {
			clickCallback(position);
		}, 300);
	};
}

function clickCallback(position) {
	if (!dbl) {
		map.zoomIn(map.getZoom() < 10 ? 2 : 1);
		map.panTo(position);
		sendPosition('pos', position, map.getZoom(), 0);
	}
}

// /prevent page scroll
function wheelevent(e) {
	if (!e)
		e = window.event;
	if (e.preventDefault)
		e.preventDefault();
	e.returnValue = false;
}

function location() {
	if (draggedMarker)
		return;
	releaseLocation();
	lmarker = createPin(pinUrl);
}

function createPin(url) {
	var icon = L.icon({
		iconUrl : url,
		iconSize: L.point(30, 48),
		iconAnchor: L.point(15, 48)
	});
	var marker = L.marker(map.getCenter(), {
		icon : icon,
		title : newLocationTitle,
		draggable : true
	}).addTo(map);
	marker.on('dragstart', markerDragStart);
	marker.on('dragend', markerDragEnd);
	marker.bindPopup(newLocationTitle).openPopup();
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

function camera() {
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

function releaseDirection() {
	if (dmarker) {
		dmarker.remove();
		dmarker = null;
		draggedMarker = null;
	}
}

function setViewPort(bounds) {
	map.fitBounds(bounds);
	sendPosition('pos', map.getCenter(), map.getZoom(), 0);
}

function setCenter(pos, zoom) {
	map.setView(pos, zoom);
	sendPosition('pos', pos, zoom, 0);
}
