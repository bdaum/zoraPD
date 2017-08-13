var map;
var mgr;
var infowindow;
var currentMarker;
var dbl = false;
var camCount = 0;
var lmarker;
var cmarker;
var dmarker;
var mouseMove;
var mapClick;
var ldragEnd;
var cdragEnd;
var ddragEnd;
var draggedMarker;
var diagonal = 1;
var batch = [];
var arrowBatch = [];
var redraw = false;

// Setup Map
function setupMap() {
	document.getElementById("map").innerHTML = mapIsLoading;
	var mapTypeIds = [];
	for ( var type in google.maps.MapTypeId)
		mapTypeIds.push(google.maps.MapTypeId[type]);
	mapTypeIds.push("OSM");
	var mapTypeControlOptions = {
		mapTypeIds : mapTypeIds
	};
	var overviewMapControlOptions = {
		opened : true
	};
	var center = initialPosition ? initialPosition : new google.maps.LatLng(
			49.785556, 9.038611);
	if (!initialMapType)
		initialMapType = google.maps.MapTypeId.ROADMAP;
	var mapOptions = {
		center : center,
		mapTypeControl : true,
		mapTypeControlOptions : mapTypeControlOptions,
		scaleControl : true,
		navigationControl : true,
		streetViewControl : true,
		overviewMapControl : true,
		overviewMapControlOptions : overviewMapControlOptions,
		mapTypeId : initialMapType,
		zoom : initialDetail
	};
	map = new google.maps.Map(document.getElementById("map"), mapOptions);
	mgr = new MarkerClusterer(map, batch, { // this is MarkerClusterPlus
		gridSize : 50,
		maxZoom : 15,
		imagePath : imagesUrl
	});
	if (initialPosition)
		sendPosition('pos', map.getCenter(), map.getZoom(), 0);
	google.maps.event.addListener(map, 'maptypechanged', function() {
		sendMessage('maptype', map.getMapTypeId());
	});
	google.maps.event.addListener(map, 'zoom_changed', function() {
		var zoom = map.getZoom();
		if (zoom > 18) {
			var maptypeId = map.getMapTypeId();
			if (maptypeId == google.maps.MapTypeId.SATELLITE)
				map.setZoom(18);
		}
		moved();
	});
	google.maps.event.addListener(map, 'dragend', function() {
		moved();
	});
	google.maps.event.addDomListener(document, 'keyup', function(e) {
		if (draggedMarker) {
			var code = (e.keyCode ? e.keyCode : e.which);
			if (code == 27) {
				if (lmarker === draggedMarker)
					releaseLocation();
				else if (cmarker === draggedMarker)
					releaseCamera();
				else if (dmarker === draggedMarker)
					releaseDirection();
				endFollowing();
			}
		}
	});
	google.maps.event.addListener(map, 'bounds_changed', function() {
		performDrawing(redraw);
		redraw = true;
	});
}

function performDrawing(redraw) {
	var bounds = map.getBounds();
	diagonal = distance(bounds.getNorthEast(), bounds.getSouthWest());
	if (track.length > 0) {
		setTrack();
		if (!redraw && track.length > 0)
			setBoundsForShape(track);
	} else {
		setMarkers();
		if (!redraw && locCreated.length > 0)
			setBoundsForShape(locCreated);
	}
}

function moved() {
	sendPosition('moved', map.getCenter(), map.getZoom(), 0);
}

function unloadMap() {
}

function sendPosition(name, point, zoom, type) {
	sendMessage(name, point.toString() + '&' + zoom + '&' + type);
}

function sendCamCount() {
	sendMessage('camCount', camCount);
}

function debug(data) {
	sendMessage('debug', data);
}

function sendMessage(name, data) {
	window.location.href = 'http://www.bdaum.de/zoom/gmap/'
			+ (data ? name + '?' + data : name);
}

// Set marker

function setMarkers() {
	clearMarkers();
	for (var i = 0; i < locCreated.length; i++) {
		var markerLocation = locCreated[i];
		var bearing = imgDirection ? imgDirection[i] : NaN;
		var marker = new google.maps.Marker({
			position : markerLocation,
			map : map,
			title : locTitles[i],
			draggable : true,
			imageAssetIds : locImage[i]
		});
		google.maps.event.addListener(marker, 'dblclick',
				makeDoubleClickCallback(marker, locImage[i]));
		google.maps.event.addListener(marker, 'click', makeClickCallback(
				marker, markerLocation));
		google.maps.event.addListener(marker, 'dragend', makeDragEndCallback(
				marker, markerLocation, bearing, arrowBatch.length));
		batch.push(marker);
		mgr.addMarker(marker);
		++camCount;
	}

	for (var i = 0; i < locCreated.length; i++) {
		var markerLocation = locCreated[i];
		var bearing = imgDirection ? imgDirection[i] : NaN;
		if (bearing !== bearing) {
			// skip NaN
		} else if (!isClustered(batch[i]))
			arrowBatch.push(createArrow(markerLocation, bearing));
	}
	sendCamCount();
}

function isClustered(marker) {
	var clustered = mgr.getClusters();
	for (var i = 0; i < clustered.length; i++) {
		var markers = clustered[i].getMarkers();
		if (markers.length > 1)
			for (var j = 0; j < markers.length; j++)
				if (markers[j] == marker)
					return true;
	}
	return false;
}

function createArrow(markerLocation, bearing) {
	var dist = diagonal / 12;
	var acoord = coord(markerLocation, dist, bearing);
	var lineSymbol = {
		path : google.maps.SymbolPath.FORWARD_CLOSED_ARROW
	};
	return new google.maps.Polyline({
		path : [ markerLocation, acoord ],
		icons : [ {
			icon : lineSymbol,
			strokeOpacity : 0.5,
		} ],
		map : map
	});
}

function clearMarkers() {
	camCount = 0;
	mgr.clearMarkers();
	for (var i = 0; i < batch.length; i++) {
		if (batch[i])
			batch[i].setMap(null);
	}
	batch = [];
	for (i = 0; i < arrowBatch.length; i++) {
		if (arrowBatch[i])
			arrowBatch[i].setMap(null);
	}
	arrowBatch = [];
}

function distance(coord1, coord2) {
	var degToRad = Math.PI / 180.0;
	var phi1 = degToRad * coord1.lat();
	var phi2 = degToRad * coord2.lat();
	var lam1 = degToRad * coord1.lng();
	var lam2 = degToRad * coord2.lng();
	return 6371.01 * Math.acos(Math.sin(phi1) * Math.sin(phi2) + Math.cos(phi1)
			* Math.cos(phi2) * Math.cos(lam2 - lam1));
}

function coord(coord, dist, bearing) {
	var degToRad = Math.PI / 180.0;
	var radToDeg = 180.0 / Math.PI;
	var brg = degToRad * ((bearing + 360) % 360);
	dist = dist / 6371.01;
	var lat1 = degToRad * coord.lat();
	var lon1 = degToRad * coord.lng();
	var lat2 = Math.asin(Math.sin(lat1) * Math.cos(dist) + Math.cos(lat1)
			* Math.sin(dist) * Math.cos(brg));
	var lon2 = lon1
			+ Math.atan2(Math.sin(brg) * Math.sin(dist) * Math.cos(lat1), Math
					.cos(dist)
					- Math.sin(lat1) * Math.sin(lat2));
	lat2 = radToDeg * lat2;
	lon2 = radToDeg * lon2;
	lon2 = (lon2 + 540) % 360 - 180;
	return new google.maps.LatLng(lat2, lon2);
}

function freezeMarkers() {
	for (var i = 0; i < batch.length; i++)
		batch[i].draggable = false;
}

function makeDragEndCallback(marker, imageAssetIds, bearing, arrowIndex) {
	return function() {
		sendPosition('modify', marker.getPosition(), map.getZoom(),
				arrayToString(imageAssetIds));
		if (bearing !== bearing) {
			// skip NaN
		} else {
			arrowBatch[arrowIndex].setMap(null);
			arrowBatch[arrowIndex] = createArrow(marker.getPosition(), bearing);
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

function setTrack() {
	new google.maps.Polyline({
		map : map,
		path : track,
		strokeColor : 'red'
	});
}

function setBoundsForShape(shape) {
	var bounds;
	for (var i = 0; i < shape.length; i++) {
		var location = shape[i];
		if (bounds)
			bounds.extend(location);
		else
			bounds = new google.maps.LatLngBounds(location, location);
	}
	if (bounds && !bounds.getNorthEast().equals(bounds.getSouthWest()))
		setViewPort(bounds);
}

function makeDoubleClickCallback(marker, imageAssetIds) {
	return function() {
		dbl = true;
		if (infowindow) {
			infowindow.close();
			infowindow = null;
		}
		if (imageAssetIds.length > 0 && !currentMarker) {
			currentMarker = marker;
			sendMessage('info', arrayToString(imageAssetIds));
		}
	};
}

function showInfo(html) {
	infowindow = new google.maps.InfoWindow({
		content : html
	});
	infowindow.open(map, currentMarker);
	currentMarker = null;
}

/*
 * Delayed execution to inhibit firing in case of double click
 */
function makeClickCallback(marker, position) {
	return function() {
		if (infowindow) {
			infowindow.close();
			infowindow = null;
		}
		dbl = false;
		setTimeout(function() {
			clickCallback(position);
		}, 300);
	};
}

function clickCallback(position) {
	if (!dbl) {
		map.panTo(position);
		var z = map.getZoom();
		map.setZoom(z < 10 ? z + 2 : z + 1);
		mgr.resetViewport();
		sendPosition('pos', position, map.getZoom(), 0);
	}
}

// prevent page scroll

function wheelevent(e) {
	if (!e)
		e = window.event;
	if (e.preventDefault)
		e.preventDefault();
	e.returnValue = false;
}

/**
 * follow() function
 */

function location() {
	if (draggedMarker)
		return;
	releaseLocation();
	mouseMove = google.maps.event.addListener(map, 'mousemove',
			function(event) {
				var cursorPoint = event.latLng;
				if (!lmarker) {
					lmarker = createPin(cursorPoint, pinUrl);
					// Marker dragged
					ldragEnd = google.maps.event.addListener(lmarker,
							'dragend', function(event) {
								if (lmarker) {
									sendPosition('drag', lmarker.getPosition(),
											map.getZoom(), 0);
									camCount = 1;
									sendCamCount();
								}
							});
					mapClick = google.maps.event.addListener(map, 'click',
							function() {
								if (lmarker) {
									sendPosition('click',
											lmarker.getPosition(), map
													.getZoom(), 0);
									endFollowing();
								}
							});
				}
				if (lmarker)
					lmarker.setPosition(cursorPoint);
			});
}

function createPin(cursorPoint, url) {
	var marker = new google.maps.Marker({
		position : cursorPoint,
		map : map,
		icon : url,
		title : newLocationTitle,
		draggable : true,
		visible : true
	});
	draggedMarker = marker;
	return marker;
}

function camera() {
	if (draggedMarker)
		return;
	releaseCamera();
	mouseMove = google.maps.event.addListener(map, 'mousemove',
			function(event) {
				var cursorPoint = event.latLng;
				if (!cmarker) {
					cmarker = createPin(cursorPoint, camPinUrl);
					// Marker dragged
					cdragEnd = google.maps.event.addListener(cmarker,
							'dragend', function(event) {
								if (cmarker) {
									sendPosition('drag', cmarker.getPosition(),
											map.getZoom(), 1);
									camCount = locCreated.length === 0 ? 1
											: locCreated.length;
									sendCamCount();
								}
							});
					mapClick = google.maps.event.addListener(map, 'click',
							function() {
								if (cmarker) {
									sendPosition('click',
											cmarker.getPosition(), map
													.getZoom(), 1);
									endFollowing();
									freezeMarkers();
								}
							});
				}
				if (cmarker)
					cmarker.setPosition(cursorPoint);
			});
}

function direction() {

	if (draggedMarker)
		return;
	releaseDirection();
	mouseMove = google.maps.event.addListener(map, 'mousemove',
			function(event) {
				var cursorPoint = event.latLng;
				if (!dmarker) {
					dmarker = createPin(cursorPoint, dirPinUrl);
					// Marker dragged
					ddragEnd = google.maps.event.addListener(dmarker,
							'dragend', function(event) {
								if (dmarker)
									sendPosition('drag', dmarker.getPosition(),
											map.getZoom(), 2);
							});
					mapClick = google.maps.event.addListener(map, 'click',
							function() {
								if (dmarker) {
									sendPosition('click',
											dmarker.getPosition(), map
													.getZoom(), 2);
									endFollowing();
								}
							});

				}
				if (dmarker)
					dmarker.setPosition(cursorPoint);
			});
}

function endFollowing() {
	// event listeners are removed to save resources
	if (mouseMove) {
		google.maps.event.removeListener(mouseMove);
		mouseMove = null;
	}
	if (mapClick) {
		google.maps.event.removeListener(mapClick);
		mapClick = null;
	}
	// reset state
	draggedMarker = null;
}

function releaseLocation() {
	if (lmarker) {
		if (ldragEnd) {
			google.maps.event.removeListener(ldragEnd);
			ldragEnd = null;
		}
		lmarker.setMap(null);
		lmarker = null;
		draggedMarker = null;
	}
	camCount = locCreated.length;
	sendCamCount();
}

function releaseCamera() {
	if (cmarker) {
		if (cdragEnd) {
			google.maps.event.removeListener(cdragEnd);
			cdragEnd = null;
		}
		cmarker.setMap(null);
		cmarker = null;
		draggedMarker = null;
	}
	camCount = locCreated.length;
	sendCamCount();
}

function releaseDirection() {
	if (dmarker) {
		if (ddragEnd) {
			google.maps.event.removeListener(ddragEnd);
			ddragEnd = null;
		}
		dmarker.setMap(null);
		dmarker = null;
		draggedMarker = null;
	}
}

function setViewPort(bounds) {
	map.fitBounds(bounds);
	sendPosition('pos', map.getCenter(), map.getZoom(), 0);
}

function setCenter(pos, zoom) {
	map.setCenter(pos, zoom);
	sendPosition('pos', pos, zoom, 0);
}
