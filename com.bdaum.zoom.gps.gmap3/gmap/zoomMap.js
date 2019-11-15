// GMAP3
var map, mgr, infowindow, currentMarker, dbl, camCount, lmarker, cmarker, dmarker, smarker, mouseMove, mapClick, ldragEnd, cdragEnd, ddragEnd, sdragEnd, draggedMarker, diagonal, batch, arrowBatch, redraw, selected, areaCircle;

/**
 * API
 */

function setupMap() {
	infowindow = currentMarker = lmarker = cmarker = dmarker = smarker = mouseMove = mapClick = ldragEnd = cdragEnd = ddragEnd = sdragEnd = draggedMarker = selected = areaCircle;
	camCount = 0;
	diagonal = 1;
	batch = [];
	arrowBatch = [];
	dbl = redraw = false;
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
	var center = initialPosition ? toLatLng(initialPosition)
			: createLatLng(49.785556, 9.038611);
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
		releaseDragged();
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

function disposeMap() {}

function cameraPin(titles, images) {
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
									sendSelection();
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
	locTitles = titles;
	locImage = images;
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

function locationShown(titles, images) {
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
									sendSelection();
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
	locShownTitles = titles;
	locShownImage = images;
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

function setCenter(pos, zoom) {
	map.setCenter(toLatLng(pos), zoom);
	sendMapPosition();
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

function setZoomDetails(zoom, pos) {
	map.setZoom(zoom);
	map.panTo(toLatLng(pos));
}

function setAreaCircle(position, diameter) {
	var pos = toLatLng(position);
	if (areaCircle) {
		areaCircle.setCenter(pos);
		areaCircle.setRadius(diameter / 2);
	} else {
		areaCircle = new google.maps.Circle({
			strokeColor : '#00FF00',
			strokeOpacity : 0.8,
			strokeWeight : 2,
			fillColor : '#00FF00',
			fillOpacity : 0.35,
			draggable : true,
			map : map,
			center : pos,
			radius : diameter / 2
		});
		google.maps.event.addListener(areaCircle, 'dragend', function() {
			sendPosition('modify', areaCircle.getCenter(), map.getZoom(), 4);
		});
	}
}

/**
 * private
 */

function latLng(array) {
	var tr = [];
	for (i = 0; i < array.length; i++)
		tr.push(createLatLng(array[i].lat, array[i].lon));
	return tr;
}

function fromLatLng(pos) {
	return {
		lat : pos.lat(),
		lon : pos.lng()
	};
}

function toLatLng(pos) {
	return createLatLng(pos.lat, pos.lon);
}

function toBounds(bounds) {
	return new google.maps.LatLngBounds(toLatLng(bounds.sw),
			toLatLng(bounds.ne));
}

function createLatLng(lat, lon) {
	return new google.maps.LatLng(lat, lon);
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
	if (track && track.length > 1)
		setTrack();
	setMarkers();
	if (!redraw && (locCreated.length || locShown.length || track.length))
		setBoundsForShape(locCreated.concat(locShown).concat(track));
}

function moved() {
	sendPosition('moved', map.getCenter(), map.getZoom(), 0);
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
	if (root)
		window.location.href = root + (data ? name + '?' + data : name);
}

function setMarkers() {
	clearMarkers();
	var bearin, marker, markerLocation;
	for (i = 0; i < locCreated.length; i++) {
		var latlng = toLatLng(locCreated[i]);
		bearin = imgDirection ? imgDirection[i] : NaN;
		marker = new google.maps.Marker({
			position : latlng,
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
				marker, latlng, locImage[i]));
		if (root)
			google.maps.event.addListener(marker, 'dragend',
					makeDragEndCallback(marker, bearin, i, locImage[i]));
		batch.push(marker);
		mgr.addMarker(marker);
		++camCount;
	}
	for (i = 0; i < locShown.length; i++) {
		var loc = toLatLng(locShown[i]);
		marker = new google.maps.Marker({
			position : loc,
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
				marker, loc, locShownImage[i]));
		if (root)
			google.maps.event.addListener(marker, 'dragend',
					makeDragEndCallback(marker, NaN, -1, locShownImage[i]));
		batch.push(marker);
		mgr.addMarker(marker);
	}
	for (i = 0; i < locCreated.length; i++) {
		markerLocation = toLatLng(locCreated[i]);
		bearin = imgDirection ? imgDirection[i] : NaN;
		if (isNaN(bearin))
			arrowBatch.push(null);
		else if (!isClustered(batch[i]))
			arrowBatch.push(createArrow(markerLocation, bearin));
	}
	sendCamCount();
}

function isClustered(marker) {
	var clusters = mgr.getClusters();
	for (i = 0; i < clusters.length; i++) {
		var markers = clusters[i].getMarkers();
		if (markers.length > 1)
			for (j = 0; j < markers.length; j++)
				if (markers[j] == marker)
					return true;
	}
	return false;
}

function createArrow(markerLocation, bearin) {
	if (!isNaN(bearin)) {
		var dist = diagonal / 12;
		var acoord = coord(markerLocation, dist, bearin);
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
}

function clearMarkers() {
	camCount = 0;
	mgr.clearMarkers();
	for (i = 0; i < batch.length; i++)
		if (batch[i])
			batch[i].setMap(null);
	batch = [];
	for (i = 0; i < arrowBatch.length; i++)
		if (arrowBatch[i])
			arrowBatch[i].setMap(null);
	arrowBatch = [];
}

function distance(coord1, coord2) {
	var degToRad = Math.PI / 180.0;
	var phi1 = degToRad * coord1.lat();
	var phi2 = degToRad * coord2.lat();
	return 6371.01 * Math.acos(Math.sin(phi1) * Math.sin(phi2) + Math.cos(phi1)
			* Math.cos(phi2) * Math.cos(degToRad * (coord2.lng() - coord1.lng())));
}

function coord(coord, dist, bearin) {
	var degToRad = Math.PI / 180.0;
	var radToDeg = 180.0 / Math.PI;
	var brg = degToRad * ((bearin + 360) % 360);
	dist = dist / 6371.01;
	var lat1 = degToRad * coord.lat();
	var lon1 = degToRad * coord.lng();
	var lat2 = Math.asin(Math.sin(lat1) * Math.cos(dist) + Math.cos(lat1)
			* Math.sin(dist) * Math.cos(brg));
	var lon2 = lon1
			+ Math.atan2(Math.sin(brg) * Math.sin(dist) * Math.cos(lat1), Math
					.cos(dist)
					- Math.sin(lat1) * Math.sin(lat2));
	return createLatLng(radToDeg * lat2, (radToDeg * lon2 + 540) % 360 - 180);
}

function makeDragEndCallback(marker, bearin, i, ids) {
	return function() {
		var markerPoint = marker.getPosition();
		if (typeof (ids) === "string")
			locShown[i] = fromLatLng(markerPoint);
		else
			locCreated[i] = fromLatLng(markerPoint);
		setSelection(ids);
		sendPosition('modify', markerPoint, map.getZoom(), 0,
				typeof (ids) === "string" ? ids : ids.join());
		if (!sNaN(bearin) && i >= 0 && arrowBatch[i]) {
			arrowBatch[i].setMap(null);
			arrowBatch[i] = createArrow(markerPoint, bearin);
		}
	}
}

function setTrack() {
	new google.maps.Polyline({
		map : map,
		path : latLng(track),
		strokeColor : 'red'
	});
}

function setBoundsForShape(positions) {
	if (positions.length) {
		var bounds;
		for (i = 0; i < positions.length; i++) {
			var loc = toLatLng(positions[i]);
			if (bounds)
				bounds.extend(loc);
			else
				bounds = new google.maps.LatLngBounds(loc, loc);
		}
		if (bounds.getNorthEast().equals(bounds.getSouthWest())) {
			map.setCenter(bounds.getNorthEast(), map.getZoom());
			sendMapPosition();
		} else
			viewPort(bounds);
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
			} else if (ids.length) {
				currentMarker = marker;
				sendMessage('info', ids.join());
			}
		}
	};
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
	viewPort(toBounds(bounds));
}

function viewPort(bounds) {
	map.fitBounds(bounds);
	sendMapPosition();
}

function sendMapPosition() {
	var point = map.getCenter();
	sendMessage('pos', point.lat() + ',' + point.lng() + "&" + map.getZoom());
}

function applyCameraSet(pos) {
	locCreated = [ fromLatLng(pos) ];
	locTitles = [ concatTitles(locTitles) ];
	selected = flatten(locImage);
	locImage = [ selected ];
	imgDirection = [ locShown && locShown.length ? bearing(fromLatLng(pos),
			locShown[0]) : NaN ];
}

function concatTitles(array) {
	var l = array.length;
	if (l === 0)
		return "";
	if (l === 1)
		return array[0];
	var newTitle = "";
	for (i = 0; i < array.length; i++) {
		var titles = array[i].split(",");
		for (j = 0; j < titles.length; j++) {
			if (newTitle != "" && newTitle.length + titles[j].length > 40
					|| titles[j] == "...")
				return newTitle + ",...";
			if (newTitle.length)
				newTitle += ",";
			newTitle += t;
		}
	}
	return newTitle;
}

function flatten(array) {
	var result = [];
	for (i = 0; i < array.length; i++) {
		var subArray = array[i];
		for (j = 0; j < subArray.length; j++)
			result.push(subArray[j]);
	}
	return result;
}

function bearing(from, to) {
	var phi1 = from.lat / 180.0 * Math.PI;
	var phi2 = to.lat / 180.0 * Math.PI;
	var lam1 = from.lon / 180.0 * Math.PI;
	var lam2 = to.lon / 180.0 * Math.PI;
	var angle = Math.atan2(Math.sin(lam2 - lam1) * Math.cos(phi2), Math
			.cos(phi1)
			* Math.sin(phi2)
			- Math.sin(phi1)
			* Math.cos(phi2)
			* Math.cos(lam2 - lam1));
	return (angle * 180.0 / Math.PI + 360.0) % 360;
}

function applyShownSet(pos) {
	locShown.push(fromLatLng(pos));
	locShownTitles.push(locTitles.length ? locTitles[0] : "");
	var uuid = generateQuickGuid();
	selected = "shown=" + uuid;
	locShownImage.push(selected);
	applyDirectionSet(pos);
	return uuid;
}

function applyDirectionSet(pos) {
	if (imgDirection.length && locCreated.length)
		imgDirection[0] = bearing(locCreated[0], fromLatLng(pos));
}

function generateQuickGuid() {
	return Math.random().toString(36).substring(2, 15)
			+ Math.random().toString(36).substring(2, 15);
}

function removeItem(array, index) {
	var newArray = [];
	for (i = 0; i < array.length; i++)
		if (i !== index)
			newArray.push(array[i]);
	return newArray;
}

function findItem(array, item) {
	for (i = 0; i < array.length; i++)
		if (item == array[i])
			return i;
	return -1;
}
