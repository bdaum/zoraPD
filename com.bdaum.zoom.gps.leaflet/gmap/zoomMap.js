//LEAFLET
var map, infowindow, currentMarker, dbl, batch, arrowBatch, diagonal, camCount, lmarker, cmarker, dmarker,
	smarker, draggedMarker, selected, mgr, areaCircle;

/**
 *      API
 */

function setupMap() {
	infowindow = currentMarker = lmarker = cmarker = dmarker = smarker = draggedMarker = selected = areaCircle = mgr = null;
	batch = [];
	arrowBatch = [];
	diagonal = 1;
	camCount = 0;
	dbl = false;
	var htmlElement = document.getElementById("map");
	htmlElement.innerHTML = mapIsLoading;
	var center = initialPosition ? toLatLng(initialPosition) : createLatLng(49.785556, 9.038611);
	map = L.map(htmlElement, {
		center: center,
		zoom: initialDetail,
		layers: null,
		minZoom: 1
	});
	L.tileLayer.provider(selectedProvider).addTo(map);
	new L.Control.MiniMap(L.tileLayer.provider(selectedProvider), {
		toggleDisplay: true
	}).addTo(map);
	if (initialPosition)
		sendMapPosition();
	map.on('moveend', function () {
		sendPosition('moved', map.getCenter(), map.getZoom(), 0);
	});
	map.on('click', MapClickHandler);
	map.on('resize', ChangeViewHandler);
	map.on('zoomend', ChangeViewHandler);
	L.DomEvent.on(htmlElement, 'keypress', function (event) {
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

function disposeMap() {
	map.remove();
}

function cameraPin(titles, images) {
	if (draggedMarker)
		return;
	releaseCamera();
	cmarker = createPin(camPinUrl);
	locTitles = titles;
	locImage = images;
}

function locationPin() {
	if (draggedMarker)
		return;
	releaseLocation();
	lmarker = createPin(pinUrl);
}

function locationShown(titles, images) {
	if (draggedMarker)
		return;
	releaseLocationShown();
	smarker = createPin(shownPinUrl);
	locShownTitles = titles;
	locShownImage = images;
}

function direction() {
	if (draggedMarker)
		return;
	releaseDirection();
	dmarker = createPin(dirPinUrl);
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
	center(toLatLng(pos), zoom);
}

function showInfo(html) {
	infowindow = html;
	var marker = currentMarker;
	if (currentMarker !== null) {
		currentMarker.addTo(map);
		currentMarker.on('popupclose', function () {
			marker.remove(map);
		});
		currentMarker.bindPopup(infowindow, {
			offset: L.point(3, -30)
		}).openPopup();
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
		areaCircle.setLatLng(pos);
		areaCircle.setRadius(diameter / 2);
	} else {
		areaCircle = L.circle(pos, {
			stroke: true,
			color: '#00FF00',
			opacity: 0.8,
			weight: 2,
			fill: true,
			fillColor: '#00FF00',
			fillOpacity: 0.35,
			radius: diameter / 2,
			draggable: true
		});
		areaCircle.addTo(map);
		areaCircle.on('dragend', function () {
			sendPosition('modify', areaCircle.getLatLng(), map.getZoom(), 4);
		});
	}
}

/**
 *      private
 */

function createLatLng(lat, lon) {
	return L.latLng(lat, lon);
}

function latLng(array) {
	var tr = [];
	for (i = 0; i < array.length; i++)
		tr.push(createLatLng(array[i].lat, array[i].lon));
	return tr;
}

function fromLatLng(pos) {
	return { lat: pos.lat, lon: pos.lng };
}

function toLatLng(pos) {
	return createLatLng(pos.lat, pos.lon);
}

function toBounds(bounds) {
	return L.latLngBounds(toLatLng(bounds.sw), toLatLng(bounds.ne));
}

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
				applyCameraSet(markerPoint);
				sendPosition('drag', markerPoint, map.getZoom(), 1);
				releaseCamera();
				setMarkers();
				sendSelection();
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
				sendSelection();
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

function sendMapPosition() {
	var point = map.getCenter();
	sendMessage('pos', point.lat + ',' + point.lng + "&" + map.getZoom());
}

function ChangeViewHandler(event) {
	performDrawings(true);
}

function performDrawings(redraw) {
	var bounds = map.getBounds();
	diagonal = distance(bounds.getNorthEast(), bounds.getSouthWest());
	if (track && track.length > 1)
		setTrack();
	setMarkers();
	if (!redraw && (locCreated.length || locShown.length || track.length))
		setBoundsForShape(locCreated.concat(locShown).concat(track));
	sendMapPosition();
}

function distance(coord1, coord2) {
	var degToRad = Math.PI / 180.0;
	var phi1 = degToRad * coord1.lat;
	var phi2 = degToRad * coord2.lat;
	return 6371.01 * Math.acos(Math.sin(phi1) * Math.sin(phi2) + Math.cos(phi1)
		* Math.cos(phi2) * Math.cos(degToRad * (coord2.lng - coord1.lng)));
}

function coord(coord, dist, bearin) {
	var degToRad = Math.PI / 180.0;
	var radToDeg = 180.0 / Math.PI;
	var brg = degToRad * ((bearin + 360) % 360);
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
	return createLatLng(lat2, lon2);
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
	if (root)
		window.location.href = root + (data ? name + '?' + data : name);
}

function setMarkers() {
	clearMarkers();
	if (locCreated.length || locShown.length) {
		if (!mgr) {
			mgr = L.markerClusterGroup();
			map.addLayer(mgr);
		}
		var markerLocation, icon, marker, bearin;
		for (i = 0; i < locCreated.length; i++) {
			markerLocation = toLatLng(locCreated[i]);
			bearin = imgDirection ? imgDirection[i] : NaN;
			icon = L.icon({
				iconUrl: selected == locImage[i].join() ? primaryIconSelUrl
					: primaryIconUrl,
				iconAnchor: [12, 38]
			});
			marker = L.marker(markerLocation, {
				icon: icon,
				title: locTitles[i],
				draggable: true
			});
			marker.on('dblclick', makeDoubleclickCallback(marker, locImage[i]));
			marker.on('click', makeClickCallback(marker, markerLocation,
				locImage[i]));
			if (root) {
				marker.on('dragstart', markerDragStart);
				marker.on('dragend', makeDragEndCallback(marker, locImage[i],
					bearin, i));
			}
			mgr.addLayer(marker);
			batch.push(marker);
			++camCount;
		}
		for (i = 0; i < locShown.length; i++) {
			markerLocation = locShown[i];
			icon = L.icon({
				iconUrl: selected == locShownImage[i] ? secondaryIconSelUrl
					: secondaryIconUrl,
				iconAnchor: [10, 32]
			});
			marker = L.marker(markerLocation, {
				icon: icon,
				title: locShownTitles[i],
				draggable: true
			});
			marker.on('dblclick', makeDoubleclickCallback(marker,
				locShownImage[i]));
			marker.on('click', makeClickCallback(marker, markerLocation,
				locShownImage[i]));
			if (root) {
				marker.on('dragstart', markerDragStart);
				marker.on('dragend', makeDragEndCallback(marker, locShownImage[i],
					NaN, -1));
			}
			mgr.addLayer(marker);
			batch.push(marker);
		}
		for (i = 0; i < locCreated.length; i++) {
			markerLocation = toLatLng(locCreated[i]);
			bearin = imgDirection ? imgDirection[i] : NaN;
			if (isNaN(bearin))
				arrowBatch.push(null);
			else if (!isClustered(mgr, batch[i]))
				arrowBatch.push(createArrow(markerLocation, bearin));
		}
	}
	sendCamCount();
}

function isClustered(mgr, marker) {
	var parent = mgr.getVisibleParent(marker);
	if (parent)
		return parent !== marker && parent.getChildCount() > 1;
}

function createArrow(markerLocation, bearin) {
	if (!isNaN(bearin)) {
		return L.polyline(computeArrowOutline(markerLocation, bearin), {
			color: 'black'
		}).addTo(map);
	}
}

function computeArrowOutline(markerLocation, bearin) {
	var dist = diagonal / 12.0;
	var arrowLength = dist / 6.0;
	var acoord = coord(markerLocation, dist, bearin);
	var ah1 = coord(acoord, arrowLength, bearin - 150);
	var ah2 = coord(acoord, arrowLength, bearin + 150);
	return [markerLocation, acoord, ah1, acoord, ah2];
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
	if (positions.length) {
		var bounds;
		for (i = 0; i < positions.length; i++) {
			var loc = toLatLng(positions[i]);
			if (bounds)
				bounds.extend(loc);
			else
				bounds = L.latLngBounds(loc, loc);
		}
		if (bounds.getNorthEast().equals(bounds.getSouthWest()))
			center(bounds.getNorthEast(), map.getZoom());
		else
			viewPort(bounds);
	}
}

function setTrack() {
	L.polyline(latLng(track), {
		color: 'red'
	}).addTo(map);
}

function makeDoubleclickCallback(marker, ids) {
	return function () {
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
			} else if (ids.length) {
				currentMarker = marker;
				sendMessage('info', ids.join());
			}
		}
	};
}

function makeDragEndCallback(marker, ids, bearin, i) {
	return function () {
		if (draggedMarker) {
			var markerPoint = draggedMarker.getLatLng();
			if (typeof (ids) === "string")
				locShown[i] = fromLatLng(markerPoint);
			else
				locCreated[i] = fromLatLng(markerPoint);
			setSelection(ids);
			sendPosition('modify', markerPoint, map.getZoom(), 0,
				typeof (ids) === "string" ? ids : ids.join());
			if (i >= 0 && arrowBatch[i])
				arrowBatch[i].setLatLngs(computeArrowOutline(markerPoint,
					bearin));
			draggedMarker = null;
		}
	};
}

function makeClickCallback(marker, position, ids) {
	return function () {
		marker.closePopup();
		dbl = false;
		setTimeout(function () {
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



function createPin(url) {
	var icon = L.icon({
		iconUrl: url,
		iconSize: L.point(30, 48),
		iconAnchor: L.point(15, 48)
	});
	var marker = L.marker(map.getCenter(), {
		icon: icon,
		title: newLocationTitle,
		draggable: true
	}).addTo(map);
	marker.on('dragstart', markerDragStart);
	marker.on('dragend', markerDragEnd);
	marker.bindPopup(newLocationTitle, {
		offset: L.point(3, -40)
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

function releaseCamera() {
	if (cmarker) {
		cmarker.remove();
		cmarker = null;
		draggedMarker = null;
	}
	camCount = locCreated.length;
	sendCamCount();
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
	viewPort(toBounds(bounds));
}
function viewPort(bounds) {
	map.fitBounds(bounds);
	sendMapPosition();
}

function center(pos, zoom) {
	map.setView(pos, zoom);
	sendMapPosition();
}

function applyCameraSet(pos) {
	locCreated = [fromLatLng(pos)];
	locTitles = [concatTitles(locTitles)];
	selected = flatten(locImage);
	locImage = [selected];
	imgDirection = [];
	imgDirection.push(locShown && locShown.length ? bearing(fromLatLng(pos), locShown[0]) : NaN);
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
			if (newTitle != "" && newTitle.length + titles[j].length > 40 || titles[j] == "...")
				return newTitle + ",...";
			if (newTitle.length)
				newTitle += ",";
			newTitle += titles[j];
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
