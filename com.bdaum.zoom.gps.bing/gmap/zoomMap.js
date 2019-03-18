// BING
var apiKey = 'AgUeONf1WVotvtvwMsQsWD3SqMdlVRv2eT73rlCXYLRvRUApnb8PNG7JV7WPRVJ4';
var MM = Microsoft.Maps;
var minimapWidth = 200;
var map;
var draggedMarker;
var currentMarker;
var pending = false;
var dbl;
var batch = [];
var diagonal = 1;
var camCount = 0;
var lmarker;
var cmarker;
var dmarker;
var smarker;
var layer;
var pinLayer;
var trackLayer;
var popup;
var selected;
var areaCircle;
var areaDragHandlerId;

function StyleChangeHandler(event) {
	window.location.href = 'http://www.bdaum.de/zoom/gmap/maptype?'
			+ event.newMapTypeId;
}

function MapClickHandler(event) {
	if (closePopup())
		return;
	if (selected) {
		selected = null;
		sendSelection();
		setMarkers();
	}
}

function ClickHandler(event) {
	if (!pending) {
		dbl = false;
		closePopup();
		setTimeout(function() {
			clickCallback(event);
		}, 300);
	}
	return event.elementID !== null;
}

function closePopup() {
	pending = false;
	currentMarker = null;
	if (popup) {
		pinLayer.remove(popup);
		popup = null;
		return true;
	}
	return false;
}

function clickCallback(event) {
	setSelection(event.target);
	if (!dbl) {
		var z = map.getZoom();
		setCenter(event.location, z < 13 ? z + 2 : z < 18 ? z + 1 : z);
	}
}

function setSelection(marker) {
	var i = marker.metadata;
	selected = i < 0 ? locShownImage[-i-1] :  locImage[i].join();
	sendSelection();
	setMarkers();
}

function DoubleClickHandler(event) {
	dbl = true;
	closePopup();
	if (!pending) {
		if (!currentMarker) {
			var marker = event.target;
			setSelection(marker);
			var i = marker.metadata;
			if (i < 0) {
				if (locShownImage[-i-1] != "shown=") {
					currentMarker = marker;
					pending = true;
					sendMessage('info', locShownImage[-i-1] + "&json");
				}
			} else if (locImage[i].length) {
				currentMarker = marker;
				pending = true;
				sendMessage('info', locImage[i].join() + "&json");
			}
		}
		return true;
	}
}

function showInfo(json) {
	if (currentMarker && pending) {
		pending = false;
		var objJSON = eval("(function(){return " + json + ";})()");
		popup = new Microsoft.Maps.Pushpin(currentMarker.getLocation(), {
			icon : objJSON.imageURL,
			title : objJSON.title,
			subTitle : objJSON.subTitle,
			anchor : new Microsoft.Maps.Point(-10, 160)
		});
		Microsoft.Maps.Events.addOne(popup, "click", function() {
					closePopup();
				});
		pinLayer.add(popup);
	}
}

// Setup Map
function setupMap() {
	document.getElementById("map").innerHTML = mapIsLoading;
	var center = initialPosition ? initialPosition
			: new Microsoft.Maps.Location(49.785556, 9.038611);
	if (!initialMapType || initialMapType == 'undefined')
		initialMapType = Microsoft.Maps.MapTypeId.road;
	try {
		map = new Microsoft.Maps.Map('#map', {
			credentials : apiKey,
			center : center,
			mapTypeId : initialMapType,
			enableClickableLogo : false,
			showBreadcrumb : true,
			zoom : initialDetail
		});
	} catch (ex) {
		map = new Microsoft.Maps.Map('#map', {
			credentials : apiKey,
			center : center,
			zoom : initialDetail
		});
	}
	
	window.addEventListener("keyup", function (e) {
    	if (draggedMarker) {
			var code = (e.keyCode ? e.keyCode : e.which);
			if (code == 27) {
				if (lmarker === draggedMarker)
					releaseLocation();
				else if (cmarker === draggedMarker)
					releaseCamera();
				else if (dmarker === draggedMarker)
					releaseDirection();
				else if (smarker === draggedMarker)
					releaseLocationShown();
				endFollowing();
			}
		}
    });
	Microsoft.Maps.Events.addHandler(map, "maptypechanged", StyleChangeHandler);
	Microsoft.Maps.Events.addHandler(map, "click", MapClickHandler);
	Microsoft.Maps.loadModule('Microsoft.Maps.Clustering', function() {
		layer = new Microsoft.Maps.ClusterLayer([], {
			gridSize : 100
		});
		map.layers.insert(layer);
		pinLayer = new Microsoft.Maps.Layer();
		map.layers.insert(pinLayer);
		trackLayer = new Microsoft.Maps.Layer();
		map.layers.insert(trackLayer);
		performDrawings(false);
	});
}

function performDrawings(redraw) {

	var bounds = map.getBounds();
	diagonal = distance(bounds.getNorthwest(), bounds.getSoutheast());
	if (track.length) {
		setTrack();
		if (!redraw && track.length)
			map.setView({
				bounds : track
			});
	} else {
		setMarkers();
		if (!redraw && (locCreated.length || locShown.length)) {
			var allPos = locCreated.concat(locShown);
			if (allPos.length > 1)
				map.setView({
					bounds : allPos
				});
			else
				setCenter(allPos[0], map.getZoom());
		}
	}
}

function distance(coord1, coord2) {
	var degToRad = Math.PI / 180.0;
	var phi1 = degToRad * coord1.latitude;
	var phi2 = degToRad * coord2.latitude;
	var lam1 = degToRad * coord1.longitude;
	var lam2 = degToRad * coord2.longitude;
	return 6371.01 * Math.acos(Math.sin(phi1) * Math.sin(phi2) + Math.cos(phi1)
			* Math.cos(phi2) * Math.cos(lam2 - lam1));
}

function unloadMap() {
	if (map)
		map.Dispose();
}

function sendPosition(name, loc, zoom, type, uuid) {
	sendMessage(name, loc.latitude + "," + loc.longitude + "&" + zoom
			+ "&" + type + (uuid ? "&" + uuid : ""));
}

function sendMessage(name, data) {
	window.location.href = 'http://www.bdaum.de/zoom/gmap/'
			+ (data ? name + '?' + data : name);
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

function setMarkers() {
	clearMarkers();
	var pushPins = [];
	var i, markerLocation, bearing, marker;
	for (i = 0; i < locCreated.length; i++) {
		markerLocation = locCreated[i];
		bearing = imgDirection ? imgDirection[i] : NaN;
		marker = new Microsoft.Maps.Pushpin(markerLocation, {
			draggable : true,
			icon : drawPushpin(24, 38, true, bearing, locTitles[i], selected == locImage[i].join()),
			anchor : new Microsoft.Maps.Point(55, 69)
		});
		marker.metadata = i;
		pushPins.push(marker);
		Microsoft.Maps.Events.addHandler(marker, "click", ClickHandler);
		Microsoft.Maps.Events.addHandler(marker, "dblclick", DoubleClickHandler);
		Microsoft.Maps.Events.addHandler(marker, "dragstart", StartDragHandler);
		Microsoft.Maps.Events.addHandler(marker, "dragend", EndDragHandler);
		batch.push(marker);
		++camCount;
	}
	for (i = 0; i < locShown.length; i++) {
		markerLocation = locShown[i];
		marker = new Microsoft.Maps.Pushpin(markerLocation, {
			draggable : true,
			icon : drawPushpin(20, 32, false, NaN, locShownTitles[i], selected == locShownImage[i]),
			anchor : new Microsoft.Maps.Point(55, 69)
		});
		marker.metadata = -i-1;
		pushPins.push(marker);
		Microsoft.Maps.Events.addHandler(marker, "click", ClickHandler);
		Microsoft.Maps.Events.addHandler(marker, "dblclick", DoubleClickHandler);
		Microsoft.Maps.Events.addHandler(marker, "dragstart", StartDragHandler);
		Microsoft.Maps.Events.addHandler(marker, "dragend", EndDragHandler);
	}
	layer.setPushpins(pushPins);
	sendCamCount();
}

function drawPushpin(w, h, primary, bearing, title, sel) {
	var c = document.getElementById('canvas');
	var arlen = 55;
	var ax = arlen;
	var ay = 69;
	c.width = 2 * arlen;
	c.height = ay+arlen;
	var ctx = c.getContext('2d');
	if (bearing !== bearing) {
		// skip NaN
	} else {
		var heady = 7;
		var headx = 5;
		ctx.beginPath();
		ctx.translate(ax, ay);
		var rot = bearing * Math.PI / 180;
		ctx.rotate(rot);
		ctx.moveTo(0, 0);
		ctx.lineTo(0, -arlen);
		ctx.lineTo(-headx, -(arlen - heady));
		ctx.moveTo(0, -arlen);
		ctx.lineTo(headx, -(arlen - heady));
		ctx.rotate(-rot);
		ctx.translate(-ax, -ay);
		ctx.lineWidth = 2;
		ctx.closePath();
		ctx.stroke();
	}
	var w2 = w / 2;
	var cy = arlen - w2;
	ctx.fillStyle = primary ? "#FF0000" : "#FFAA00";
	ctx.beginPath();
	ctx.moveTo(ax, ay);
	ctx.lineTo(ax - w2, cy);
	ctx.arc(ax, cy, w2, Math.PI, 0);
	ctx.moveTo(ax + w2, cy);
	ctx.lineTo(ax, ay);
	ctx.closePath();
	ctx.stroke();
	ctx.fill();
	ctx.beginPath();
	var pi2 =  2 * Math.PI;
	if (primary) {
		ctx.fillStyle = "#FFFFFF";
		ctx.arc(ax, cy, 5, 0, pi2);
		ctx.closePath();
		ctx.fill();
		ctx.fillStyle = "#000000";
		ctx.beginPath();
		ctx.arc(ax, cy, 3, 0, pi2);
		ctx.closePath();
		ctx.fill();
		ctx.fillStyle = "#FFFFFF";
		ctx.beginPath();
		ctx.arc(ax, cy, 2, 0, pi2);
	} else {
		ctx.fillStyle = "#000000";
		var ilen = w * 5 / 8;
		var ih = w * 2 / 5;
		var iy = cy + ih/2;
		ctx.moveTo(ax - ilen / 2, iy);
		ctx.lineTo(ax + ilen / 2, iy);
		ctx.lineTo(ax + ilen / 4, iy-ih);
		ctx.lineTo(ax, iy-ih / 4);
		ctx.lineTo(ax - ilen / 4, iy-ih / 2);
		ctx.moveTo(ax - ilen / 2, iy);
	}
	ctx.closePath();
	ctx.fill();
	if (sel) {
		ctx.fillStyle = "#00CCFF";
		ctx.beginPath();
		ctx.arc(ax+w/4, cy-w/3, 3, 0, pi2);
		ctx.closePath();
		ctx.fill();
	}
	ctx.font = primary ? "bold 11px arial" : "bold 9px arial";
	ctx.fillStyle = "#000000";
	ctx.textAlign = "center";
	ctx.fillText(title, ax, ay + 11);
	return c.toDataURL();
}

function clearMarkers() {
	camCount = 0;
	layer.clear();
	batch = [];
	trackLayer.clear();
}

function setTrack() {
	var path = new Microsoft.Maps.Polyline(track, {
		strokeColor : 'red',
		strokeThickness : 2
	});
	trackLayer.add(path);
}

function StartDragHandler(event) {
	draggedMarker = event.target;
}

function EndDragHandler(event) {
	if (draggedMarker === event.target) {
		try {
			var markerPoint = event.location;
			if (draggedMarker === lmarker) {
				sendPosition('drag', markerPoint, map.getZoom(), 0);
				camCount = 1;
				sendCamCount();
			} else if (draggedMarker === cmarker) {
				sendPosition('drag', markerPoint, map.getZoom(), 1);
				applyCameraSet(markerPoint);
				releaseCamera();
				setMarkers();
			} else if (draggedMarker === dmarker) {
				sendPosition('drag', markerPoint, map.getZoom(), 2);
				applyDirectionSet(markerPoint);
				releaseDirection();
				setMarkers();
			} else if (draggedMarker === smarker) {
				var uuid = applyShownSet(markerPoint);
				sendPosition('drag', markerPoint, map.getZoom(), 3, uuid);
				releaseLocationShown();
				setMarkers();
			} else if (draggedMarker.metadata !== undefined) {
				var i = draggedMarker.metadata;
				var ids;
				if (i < 0) {
					locShown[-i-1] = markerPoint;
					ids = locShownImage[-i-1];
				} else {
					locCreated[i] = markerPoint;
					ids = locImage[i].join();
				}
				sendPosition('modify', markerPoint, map.getZoom(),
						ids);
			}
		} finally {
			draggedMarker = null;
		}
	}
}

function locationPin() {
	if (draggedMarker)
		return;
	releaseLocation();
	lmarker = createPin(pinUrl);
}

function releaseLocation() {
	if (lmarker) {
		pinLayer.remove(lmarker);
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

function createPin(url) {
	var marker = new Microsoft.Maps.Pushpin(map.getCenter(), {
		icon : url,
		title : newLocationTitle,
		anchor : new Microsoft.Maps.Point(15, 48),
		enableHoverStyle: true,
		draggable : true
	});
	Microsoft.Maps.Events.addHandler(marker, "dragstart", StartDragHandler);
	Microsoft.Maps.Events.addHandler(marker, "dragend", EndDragHandler);
	Microsoft.Maps.Events.addHandler(marker, "click", ClickHandler);
	Microsoft.Maps.Events.addHandler(marker, "dblclick", DoubleClickHandler);
	pinLayer.add(marker);
	setTimeout(function() {
		marker.setOptions({ anchor: new Microsoft.Maps.Point(10, 60) });
		setTimeout(function() {
			marker.setOptions({ anchor: new Microsoft.Maps.Point(15, 48) });
		}, 250);
	}, 250);
	return marker;
}

function releaseCamera() {
	if (cmarker) {
		pinLayer.remove(cmarker);
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
		pinLayer.remove(dmarker);
		dmarker = null;
		draggedMarker = null;
	}
}

function releaseLocationShown() {
	if (smarker) {
		pinLayer.remove(smarker);
		smarker = null;
		draggedMarker = null;
	}
}

 function setViewPort(bounds) {
	 map.setView({
		 bounds : bounds
	 });
	 sendMapPosition();
 }

function setCenter(pos, zoom) {
	map.setView({
		center : pos,
		zoom : zoom
	});
	sendMapPosition();
}

function sendMapPosition() {
	var point = map.getCenter();
	sendMessage('pos', point.latitude + ',' + point.latitude + "&" + map.getZoom() );
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
	if (locShown && locShown.length)
		imgDirection.push(bearing(pos, locShown[0]));
	else
		imgDirection.push(NaN);
}

function concatTitles(array) {
	var l = array.length;
	if (l === 0)
		return "";
	if (l === 1)
		return array[0];
	var newTitle = "";
	for (var i = 0; i < array.length; i++) {
		var titles = array[i].split(",");
		for (var j=0; j < titles.length; j++) {
			var t = titles[j];
			if (newTitle != "" && newTitle.length + t.length > 40 || t == "...")
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
	for (var i = 0; i < array.length; i++) {
		var subArray = array[i];
		for (var j = 0; j < subArray.length; j++) {
			result.push(subArray[j]);
		}
	}
	return result;
}

function bearing(from, to) {
	var phi1 = from.latitude / 180.0 * Math.PI;
	var phi2 = to.latitude / 180.0 * Math.PI;
	var lam1 = from.longitude / 180.0 * Math.PI;
	var lam2 = to.longitude / 180.0 * Math.PI;
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
	locShownTitles.push(locTitles.length ? locTitles[0] : "");
	var uuid = generateQuickGuid();
	selected = "shown="+uuid;
	locShownImage.push(selected);
	applyDirectionSet(pos);
	return uuid;
}

function applyDirectionSet(pos) {
	if (imgDirection.length && locCreated.length) 
		imgDirection[0] = bearing(locCreated[0], pos);
}

function generateQuickGuid() {
    return Math.random().toString(36).substring(2, 15) +
        Math.random().toString(36).substring(2, 15);
}

function deleteSelected() {
	if (selected) {
		var p  = findItem(locImage, selected);
		if (p >= 0) {
			batch = removeItem(batch,p);
			locCreated = removeItem(locCreated,p);
			locTitles = removeItem(locTitles,p);
			locImage = removeItem(locImage,p);
			if (imgDirection)
				imgDirection = removeItem(imgDirection,p);
			selected = null;
			setMarkers();
		} else {
			p  = findItem(locShownImage,selected);
			if (p >= 0) {
				locShown = removeItem(locShown,p);
				locShownTitles = removeItem(locShownTitles,p);
				locShownImage = removeItem(locShownImage,p);
				selected = null;
				setMarkers();
			}
		}
	}
}

function removeItem(array, index) {
	var newArray = [];
	for (var i=0; i < array.length; i++) 
		if (i !== index) 
			newArray.push(array[i]);
	return newArray;
}

function findItem(array, item) {
	for (var i=0; i < array.length; i++) 
		if (item == array[i])
			return i;
	return -1;
}

function setAreaCircle(position, diameter) {
	if (areaDragHandlerId)
		Microsoft.Maps.Events.removeHandler(areaDragHandlerId);
	if (areaCircle) 
		map.entities.pop();
	var R = 6371; // earth's mean radius in km
	var backgroundColor = new Microsoft.Maps.Color(30, 100, 0, 0);
	var borderColor = new Microsoft.Maps.Color(150, 200, 0, 0);
	var oneDeg = Math.PI / 180;
	var lat = position.latitude * oneDeg;     
	var lon = position.longitude * oneDeg;
	var d = diameter / (2000 * R);
	var circlePoints = [];
	for (x = 0; x <= 360; x += 5) {
	    var p2 = new Microsoft.Maps.Location(0, 0);
	    var brng = x * oneDeg;
	    p2.latitude = Math.asin(Math.sin(lat) * Math.cos(d) + Math.cos(lat) * Math.sin(d) * Math.cos(brng));
	    p2.longitude = (lon + Math.atan2(Math.sin(brng) * Math.sin(d) * Math.cos(lat), 
	                     Math.cos(d) - Math.sin(lat) * Math.sin(p2.latitude))) / oneDeg;
	    p2.latitude /= oneDeg;
	    circlePoints.push(p2);
	}
	areaCircle = new Microsoft.Maps.Polygon(circlePoints, { fillColor: backgroundColor, strokeColor: borderColor, strokeThickness: 1 });
	map.entities.push(areaCircle);
	areaDragHandlerId = Microsoft.Maps.Events.addHandler(areaCircle, "dragend", function() {
		sendPosition('modify', position, map.getZoom(), 4);
	});
}

