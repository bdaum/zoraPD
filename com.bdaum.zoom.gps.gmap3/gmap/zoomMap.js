var map;
var mgr;
var infowindow;
var currentMarker;
var dbl = false;
var camCount = 0;
var lmarker;
var cmarker;
var dmarker;
var smarker;
var mouseMove;
var mapClick;
var ldragEnd;
var cdragEnd;
var ddragEnd;
var sdragEnd;
var draggedMarker;
var diagonal = 1;
var batch = [];
var arrowBatch = [];
var redraw = false;
var selected;

function setupMap() {
	document.getElementById("map").innerHTML = mapIsLoading;
	var mapTypeIds = [];
	for ( var type in google.maps.MapTypeId) {
		mapTypeIds.push(google.maps.MapTypeId[type]);
	}
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
		fullscreenControl : false,
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
		sendMapPosition();
	google.maps.event.addListener(map, 'maptypechanged', function() {
		sendMessage('maptype', map.getMapTypeId());
	});
	google.maps.event.addListener(map, 'click', MapClickHandler);
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
			if (code == 27) 
				releaseDragged();
		}
	});
	google.maps.event.addListener(map, 'bounds_changed', function() {
		performDrawing(redraw);
		redraw = true;
	});
}

function releaseDragged() {
	if (lmarker === draggedMarker)
		releaseLocation();
	else if (cmarker === draggedMarker)
		releaseCamera();
	else if (dmarker === draggedMarker)
		releaseDirection();
	else if (smarker === draggedMarker)
		releaseLocationShown();
	draggedMarker = null;
	endFollowing();
}

function MapClickHandler(event) {
	if (closeInfoWindow())
		return;
	if (selected) {
		selected = null;
		sendSelection();
		releaseDragged();
		setMarkers();
	}
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
		if (!redraw && (locCreated.length > 0 || locShown.length > 0)) 
			setBoundsForShape(locCreated.concat(locShown));
	}
}

function moved() {
	sendPosition('moved', map.getCenter(), map.getZoom(), 0);
}

function unloadMap() {
}

function sendPosition(name, point, zoom, type, uuid) {
	sendMessage(name, point.lat() + ',' + point.lng() + '&' + zoom + '&' + type
			+ (uuid ? '&' + uuid : ""));
}

function sendCamCount() {
	sendMessage('camCount', camCount);
}

function sendSelection() {
	sendMessage('select', selected);
}

function debug(data) {
	sendMessage('debug', data);
}

function sendMessage(name, data) {
	window.location.href = 'http://www.bdaum.de/zoom/gmap/'
			+ (data ? name + '?' + data : name);
}

function setMarkers() {
	clearMarkers();
	var bearing, marker, markerLocation;
	for (var i = 0; i < locCreated.length; i++) {
		bearing = imgDirection ? imgDirection[i] : NaN;
		marker = new google.maps.Marker({
			position : locCreated[i],
			map : map,
			icon : selected == locImage[i].join() ? primaryIconSelUrl
					: primaryIconUrl,
			anchorPoint : new google.maps.Point(12, 38),
			title : locTitles[i],
			draggable : true
		});
		google.maps.event.addListener(marker, 'dblclick',
				makeDoubleClickCallback(marker, locImage[i]));
		google.maps.event.addListener(marker, 'click', makeClickCallback(
				marker, locCreated[i], locImage[i]));
		google.maps.event.addListener(marker, 'dragend', makeDragEndCallback(
				marker, bearing, i, locImage[i]));
		batch.push(marker);
		mgr.addMarker(marker);
		++camCount;
	}
	for (i = 0; i < locShown.length; i++) {
		marker = new google.maps.Marker({
			position : locShown[i],
			map : map,
			icon : selected == locShownImage[i] ? secondaryIconSelUrl
					: secondaryIconUrl,
			anchorPoint : new google.maps.Point(10, 32),
			title : locShownTitles[i],
			draggable : true
		});
		google.maps.event.addListener(marker, 'dblclick',
				makeDoubleClickCallback(marker, locShownImage[i]));
		google.maps.event.addListener(marker, 'click', makeClickCallback(
				marker, locShown[i], locShownImage[i]));
		google.maps.event.addListener(marker, 'dragend', makeDragEndCallback(
				marker, NaN, -1, locShownImage[i]));
		batch.push(marker);
		mgr.addMarker(marker);
	}
	for (i = 0; i < locCreated.length; i++) {
		markerLocation = locCreated[i];
		bearing = imgDirection ? imgDirection[i] : NaN;
		if (bearing !== bearing) {
			arrowBatch.push(null);
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

function makeDragEndCallback(marker, bearing, i, ids) {
	return function() {
		var markerPoint = marker.getPosition();
		if (typeof (ids) === "string") {
			locShown[i] = markerPoint;
		} else {
			locCreated[i] = markerPoint;
		}
		setSelection(ids);
		sendPosition('modify', markerPoint, map.getZoom(),
				typeof (ids) === "string" ? ids : ids.join());
		if (bearing !== bearing) {
			// skip NaN
		} else if (i >= 0 && arrowBatch[i]) {
			arrowBatch[i].setMap(null);
			arrowBatch[i] = createArrow(markerPoint, bearing);
		}
	};
}

function setTrack() {
	new google.maps.Polyline({
		map : map,
		path : track,
		strokeColor : 'red'
	});
}

function setBoundsForShape(positions) {
	if (positions.length > 0) {
		var bounds;
		for (var i = 0; i < positions.length; i++) {
			var location = positions[i];
			if (bounds)
				bounds.extend(location);
			else
				bounds = new google.maps.LatLngBounds(location, location);
		}
		if (bounds.getNorthEast().equals(bounds.getSouthWest()))
			setCenter(bounds.getNorthEast(), map.getZoom());
		else
			setViewPort(bounds);
	}
}

function setSelection(ids) {
	selected = typeof (ids) === "string" ? ids : ids.join();
	sendSelection();
	setMarkers();
}

function closeInfoWindow() {
	if (infowindow) {
		infowindow.close();
		infowindow = null;
		return true;
	}
	return false;
}

function makeDoubleClickCallback(marker, ids) {
	return function() {
		dbl = true;
		closeInfoWindow();
		if (!currentMarker) {
			sendSelection();
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

function showInfo(html) {
	if (currentMarker) {
		infowindow = new google.maps.InfoWindow({
			content : html,
			position : currentMarker.getPosition(),
			pixelOffset : new google.maps.Size(3, -30)
		});
		infowindow.open(map);
		currentMarker = null;
	}
}

/*
 * Delayed execution to inhibit firing in case of double click
 */
function makeClickCallback(marker, position, ids) {
	return function() {
		closeInfoWindow();
		dbl = false;
		setTimeout(function() {
			clickCallback(position, ids);
		}, 300);
	};
}

function clickCallback(position, ids) {
	setSelection(ids);
	if (!dbl) {
		map.panTo(position);
		var z = map.getZoom();
		map.setZoom(z < 10 ? z + 2 : z + 1);
		mgr.resetViewport();
		sendMapPosition();
	}
}

function locationPin() {
	if (draggedMarker)
		return;
	releaseLocation();
	mouseMove = google.maps.event.addListener(map, 'mousemove',
			function(event) {
				var cursorPoint = event.latLng;
				if (!lmarker) {
					lmarker = createPin(cursorPoint, pinUrl);
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
									releaseDragged();
								}
							});
				}
				if (lmarker)
					lmarker.setPosition(cursorPoint);
			});
}

function createPin(cursorPoint, url) {
	draggedMarker = new google.maps.Marker({
		position : cursorPoint,
		map : map,
		icon : url,
		anchorPoint : new google.maps.Point(15, 48),
		title : newLocationTitle,
		draggable : true,
		visible : true
	});
	return draggedMarker;
}

function cameraPin() {
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
									applyCameraSet(cmarker.getPosition());
									releaseCamera();
									setMarkers();
								}
							});
					mapClick = google.maps.event.addListener(map, 'click',
							function() {
								if (cmarker) {
									sendPosition('click',
											cmarker.getPosition(), map
													.getZoom(), 1);
									applyCameraSet(cmarker.getPosition());
									releaseDragged();
									setMarkers();
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
								if (dmarker) {
									sendPosition('drag', dmarker.getPosition(),
											map.getZoom(), 2);
									applyDirectionSet(dmarker.getPosition());
									releaseDirection();
									setMarkers();
								}
							});
					mapClick = google.maps.event.addListener(map, 'click',
							function() {
								if (dmarker) {
									sendPosition('click',
											dmarker.getPosition(), map
													.getZoom(), 2);
									endFollowing();
									applyDirectionSet(dmarker.getPosition());
									releaseDirection();
									setMarkers();
								}
							});

				}
				if (dmarker)
					dmarker.setPosition(cursorPoint);
			});
}

function locationShown() {
	if (draggedMarker)
		return;
	releaseLocationShown();
	mouseMove = google.maps.event.addListener(map, 'mousemove',
			function(event) {
				var cursorPoint = event.latLng;
				if (!smarker) {
					smarker = createPin(cursorPoint, shownPinUrl);
					// Marker dragged
					sdragEnd = google.maps.event.addListener(smarker,
							'dragend', function(event) {
								if (smarker) {
									var uuid = applyShownSet(smarker
											.getPosition());
									sendPosition('drag', smarker.getPosition(),
											map.getZoom(), 3, uuid);
									releaseLocationShown();
									setMarkers();
								}
							});
					mapClick = google.maps.event.addListener(map, 'click',
							function() {
								if (smarker) {
									var uuid = applyShownSet(smarker
											.getPosition());
									sendPosition('click',
											smarker.getPosition(), map
													.getZoom(), 3, uuid);
									releaseDragged();
									setMarkers();
								}
							});

				}
				if (smarker)
					smarker.setPosition(cursorPoint);
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
	}
}

function releaseLocationShown() {
	if (smarker) {
		if (sdragEnd) {
			google.maps.event.removeListener(sdragEnd);
			sdragEnd = null;
		}
		smarker.setMap(null);
		smarker = null;
		draggedMarker = null;
	}
}

function setViewPort(bounds) {
	map.fitBounds(bounds);
	sendMapPosition();
}

function setCenter(pos, zoom) {
	map.setCenter(pos, zoom);
	sendMapPosition();
}

function sendMapPosition() {
	var point = map.getCenter();
	sendMessage('pos', point.lat() + ',' + point.lng() + "&" + map.getZoom());
}

function applyCameraSet(pos) {
	locCreated = [];
	locCreated.push(pos);
	var titles = concatTitles(locTitles);
	locTitles = [];
	locTitles.push(titles);
	selected = flatten(locImage);
	locImage = [];
	locImage.push(selected);
	imgDirection = [];
	if (locShown && locShown.length > 0) {
		imgDirection.push(bearing(pos, locShown[0]));
	} else {
		imgDirection.push(NaN);
	}
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
		for (var j = 0; j < subArray.length; j++) {
			result.push(subArray[j]);
		}
	}
	return result;
}

function bearing(from, to) {
	var phi1 = from.lat() / 180.0 * Math.PI;
	var phi2 = to.lat() / 180.0 * Math.PI;
	var lam1 = from.lng() / 180.0 * Math.PI;
	var lam2 = to.lng() / 180.0 * Math.PI;
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
	if (locTitles.length > 0) {
		locShownTitles.push(locTitles[0]);
	} else {
		locShownTitles.push("");
	}
	var uuid = generateQuickGuid();
	selected = "shown=" + uuid;
	locShownImage.push(selected);
	applyDirectionSet(pos);
	return uuid;
}

function applyDirectionSet(pos) {
	if (imgDirection.length > 0 && locCreated.length > 0) {
		imgDirection[0] = bearing(locCreated[0], pos);
	}
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
	for (var i = 0; i < array.length; i++) {
		if (i !== index) {
			newArray.push(array[i]);
		}
	}
	return newArray;
}
function findItem(array, item) {
	for (var i = 0; i < array.length; i++) {
		if (item == array[i]) {
			return i;
		}
	}
	return -1;
}
