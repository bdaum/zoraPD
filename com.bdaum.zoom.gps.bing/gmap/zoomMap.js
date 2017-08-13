var minimapWidth = 200;
var map;
var draggedMarker;
var currentMarker;
var pending = false;
var dbl;
var batch = [];
var arrowBatch = [];
var diagonal = 1;
var camCount = 0;
var lmarker;
var cmarker;
var dmarker;
var layer;
var pinLayer;
var infoOpen;

function MyHandleCredentialsError() {
	alert(keyInvalid);
}

function StyleChangeHandler(event) {
	window.location.href = 'http://www.bdaum.de/zoom/gmap/maptype?'
			+ event.mapStyle;
}

function ModeChangeHandler(event) {
	sendMessage('mapmode', map.GetMapMode() == VEMapMode.Mode3D ? '3D' : '');
}

function ResizeHandler(event) {
	showMiniMap();
}

function showMiniMap() {
	var width = document.getElementById("map").offsetWidth - minimapWidth;
	if (width < 0)
		width = 0;
	map.ShowMiniMap(width, 0, VEMiniMapSize.Small);
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
	if (currentMarker) {
		currentMarker.SetTitle(currentMarker.hiddenTitle);
		currentMarker.SetDescription("");
		currentMarker = null;
		map.SetDefaultInfoBoxStyles();
	}
}

function clickCallback(event) {
	if (!dbl && event.elementID) {
		var pixel = new VEPixel(event.mapX, event.mapY);
		var markerPoint = map.PixelToLatLong(pixel);
		var z = map.GetZoomLevel();
		map.SetCenterAndZoom(markerPoint, z < 13 ? z + 2 : z < 18 ? z + 1 : z);
		sendPosition('pos', markerPoint, map.GetZoomLevel());
	}
}

function DoubleClickHandler(event) {
	dbl = true;
	if (infoOpen) {
		map.HideInfoBox();
		infoOpen = false;
	} else if (!pending) {
		closePopup();
		if (event.elementID) {
			var shape = map.GetShapeByID(event.elementID);
			if (!currentMarker) {
				var imageAssetIds = shape.imageAssetIds;
				if (imageAssetIds.length > 0) {
					currentMarker = shape;
					pending = true;
					sendMessage('info', arrayToString(imageAssetIds));
				}
			}
			return true;
		}
	}

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

function MoveHandler() {
	sendPosition('moved', map.getCenter(), map.getZoomLevel(), 0);
}

// function KeyHandler(event) {
// if (draggedMarker) {
// var code = event.keyCode;
// if (code == 27) {
// if (lmarker === draggedMarker)
// releaseLocation();
// else if (cmarker === draggedMarker) {
// releaseCamera();
// } else if (dmarker === draggedMarker)
// releaseDirection();
// draggedMarker = null;
// }
// }
// }

function ChangeViewHandler(event) {
	performDrawings(true);
}

function showInfo(html) {
	if (currentMarker && pending) {
		currentMarker.hiddenTitle = currentMarker.GetTitle();
		currentMarker.SetTitle("");
		currentMarker.SetDescription(html);
		map.ClearInfoBoxStyles();
		map.ShowInfoBox(currentMarker);
		pending = false;
		infoOpen = true;
	}
}

// / Setup Map
function setupMap() {
	document.getElementById("map").innerHTML = mapIsLoading;
	var center = initialPosition ? initialPosition : new VELatLong(49.785556,
			9.038611);
	if (!initialMapType)
		initialMapType = VEMapStyle.Road;
	map = new VEMap('map');
	if (applicationKey) {
		map.AttachEvent("oncredentialserror", MyHandleCredentialsError);
		map.SetCredentials(applicationKey);
	}
	map.LoadMap(center, initialDetail, initialMapType);
	if (initialPosition)
		sendPosition('pos', center, initialDetail, 0);
	showMiniMap();
	map.AttachEvent("onchangemapstyle", StyleChangeHandler);
	map.AttachEvent("oninitmode", ModeChangeHandler);
	map.AttachEvent("onclick", ClickHandler);
	map.AttachEvent("ondoubleclick", DoubleClickHandler);
	map.AttachEvent("onendpan", MoveHandler);
	// map.AttachEvent("onkeyup", KeyHandler);
	map.AttachEvent("onchangeview", ChangeViewHandler);
	map.AttachEvent("onresize", ChangeViewHandler);
	window.onresize = ResizeHandler;
	map.SetScaleBarDistanceUnit(VEDistanceUnit.Kilometers);
	layer = new VEShapeLayer();
	layer.SetClusteringConfiguration(VEClusteringType.Grid);
	map.AddShapeLayer(layer);
	pinLayer = new VEShapeLayer();
	map.AddShapeLayer(pinLayer);
	performDrawings(false);
}

function performDrawings(redraw) {
	var bounds = map.GetMapView();
	diagonal = distance(bounds.TopLeftLatLong, bounds.BottomRightLatLong);
	if (track.length > 0) {
		setTrack();
		if (!redraw && track.length > 0)
			map.SetMapView(track);
	} else {
		setMarkers();
		if (!redraw && locCreated.length > 0)
			map.SetMapView(locCreated);
	}
}

function distance(coord1, coord2) {
	var degToRad = Math.PI / 180.0;
	var phi1 = degToRad * coord1.Latitude;
	var phi2 = degToRad * coord2.Latitude;
	var lam1 = degToRad * coord1.Longitude;
	var lam2 = degToRad * coord2.Longitude;
	return 6371.01 * Math.acos(Math.sin(phi1) * Math.sin(phi2) + Math.cos(phi1)
			* Math.cos(phi2) * Math.cos(lam2 - lam1));
}

function coord(latlng1, dist, bearing) {
	var degToRad = Math.PI / 180.0;
	var radToDeg = 180.0 / Math.PI;
	var brg = degToRad * ((bearing + 360) % 360);
	dist = dist / 6371.01;
	var lat1 = degToRad * latlng1.Latitude;
	var lon1 = degToRad * latlng1.Longitude;
	var lat2 = Math.asin(Math.sin(lat1) * Math.cos(dist) + Math.cos(lat1)
			* Math.sin(dist) * Math.cos(brg));
	var lon2 = lon1
			+ Math.atan2(Math.sin(brg) * Math.sin(dist) * Math.cos(lat1), Math
					.cos(dist)
					- Math.sin(lat1) * Math.sin(lat2));
	lat2 = radToDeg * lat2;
	lon2 = radToDeg * lon2;
	lon2 = (lon2 + 540) % 360 - 180;
	return new VELatLong(lat2, lon2);
}

function unloadMap() {
	if (map)
		map.Dispose();
}

function sendPosition(name, point, zoom, type) {
	sendMessage(name, "(" + point + ")&" + zoom + "&" + type);
}

function sendMessage(name, data) {
	window.location.href = 'http://www.bdaum.de/zoom/gmap/'
			+ (data ? name + '?' + data : name);
}

function sendCamCount() {
	sendMessage('camCount', camCount);
}

function debug(data) {
	sendMessage('debug', data);
}

// Set marker
function setMarkers() {
	clearMarkers();
	for (var i = 0; i < locCreated.length; i++) {
		var markerLocation = locCreated[i];
		var bearing = imgDirection ? imgDirection[i] : NaN;
		var shape = new VEShape(VEShapeType.Pushpin, markerLocation);
		shape.SetTitle(locTitles[i]);
//		shape.Draggable = true;
//		shape.onstartdrag = StartDragHandler;
//		shape.onenddrag = makeDragEndCallback(shape, locImage[i], bearing,
//				arrowBatch.length);
		shape.imageAssetIds = locImage[i];
		layer.AddShape(shape);
		// No click handler for markers!
		batch.push(shape);
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
	var clusterShapes = layer.GetClusteredShapes(VEClusteringType.Grid);
	for (var i = 0; i < clusterShapes.length; i++) {
		var shapes = clusterShapes[i].Shapes;
		if (shapes.length > 1)
			for (var j = 0; j < shapes.length; j++)
				if (shapes[j] == marker)
					return true;
	}
	return false;
}

function createArrow(markerLocation, bearing) {
	var dist = diagonal / 12.0;
	var arrowLength = dist / 6.0;
	var acoord = coord(markerLocation, dist, bearing);
	var ah1 = coord(acoord, arrowLength, bearing - 150);
	var ah2 = coord(acoord, arrowLength, bearing + 150);
	var line = new VEShape(VEShapeType.Polyline, [ markerLocation, acoord, ah1,
			acoord, ah2 ]);
	line.HideIcon();
	line.SetLineColor(new VEColor(0, 0, 0, 1));
	layer.AddShape(line);
	return line;
}

function clearMarkers() {
	camCount = 0;
	for (var i = 0; i < batch.length; i++)
		if (batch[i])
			layer.DeleteShape(batch[i]);
	batch = [];
	for (i = 0; i < arrowBatch.length; i++)
		if (arrowBatch[i])
			layer.DeleteShape(arrowBatch[i]);
	arrowBatch = [];
}

function setTrack() {
	var path = new VEShape(VEShapeType.Polyline, track);
	path.HideIcon();
	path.SetLineColor(new VEColor(255, 0, 0, 1));
	layer.AddShape(path);
}

function StartDragHandler(event) {
	draggedMarker = event.Shape;
}

function EndDragHandler(event) {
	if (draggedMarker) {
		try {
			var markerPoint = event.LatLong;
			if (draggedMarker == lmarker) {
				sendPosition('drag', markerPoint, map.GetZoomLevel(), 0);
				camCount = 1;
				sendCamCount();
			} else if (draggedMarker == cmarker) {
				sendPosition('drag', markerPoint, map.GetZoomLevel(), 1);
				camCount = locCreated.length === 0 ? 1 : locCreated.length;
				sendCamCount();
//				freezeMarkers();
			} else if (draggedMarker == dmarker) {
				sendPosition('drag', markerPoint, map.GetZoomLevel(), 2);
//				freezeMarkers();
			}
		} finally {
			draggedMarker = null;
		}
	}
}

//function makeDragEndCallback(marker, imageAssetIds, bearing, arrowIndex) {
//	return function() {
////		var propList = "";
////		for ( var propName in event) {
////			if (typeof (event[propName]) != "undefined") {
////				propList += propName;
////				propList += "=";
////				propList += event[propName];
////				propList += ", ";
////			}
////		}
//		sendPosition('modify', marker.getPosition(), map.getZoom(),
//				arrayToString(imageAssetIds));
//		if (bearing !== bearing) {
//			// skip NaN
//		} else {
//			layer.DeleteShape(arrowBatch[arrowIndex]);
//			arrowBatch[arrowIndex] = createArrow(marker.getPosition(), bearing);
//		}
//	};
//}

//function freezeMarkers() {
//	for (var i = 0; i < batch.length; i++)
//		batch[i].Draggable = false;
//}

function location() {
	if (map.GetMapMode() == VEMapMode.Mode3D || draggedMarker)
		return;
	releaseLocation();
	lmarker = createPin(pinUrl);
}

function releaseLocation() {
	if (lmarker) {
		pinLayer.DeleteShape(lmarker);
		lmarker = null;
		draggedMarker = null;
	}
	camCount = locCreated.length;
	sendCamCount();
}

function camera() {
	if (map.GetMapMode() == VEMapMode.Mode3D || draggedMarker)
		return;
	releaseCamera();
	cmarker = createPin(camPinUrl);
}

function createPin(url) {
	var marker = new VEShape(VEShapeType.Pushpin, map.GetCenter());
	marker.SetCustomIcon("<img src='" + url
			+ "' style='margin: -21px 0px 0px -3px'/>");
	// Allow the pushpin to be dragged
	marker.Draggable = true;
	// Assign the shape drag event handlers
	marker.onstartdrag = StartDragHandler;
	marker.onenddrag = EndDragHandler;
	// Show an info box to indicate the pushpin can be dragged.
	marker.SetTitle(newLocationTitle);
	pinLayer.AddShape(marker);
	map.ShowInfoBox(marker);
	return marker;
}

function releaseCamera() {
	if (cmarker) {
		pinLayer.DeleteShape(cmarker);
		cmarker = null;
		draggedMarker = null;
	}
	camCount = locCreated.length;
	sendCamCount();
}

function direction() {
	if (map.GetMapMode() == VEMapMode.Mode3D || draggedMarker)
		return;
	releaseDirection();
	dmarker = createPin(dirPinUrl);
}

function releaseDirection() {
	if (dmarker) {
		pinLayer.DeleteShape(dmarker);
		dmarker = null;
		draggedMarker = null;
	}
}

function setViewPort(bounds) {
	map.SetMapView([ bounds.TopLeftLatLong, bounds.BottomRightLatLong ]);
	sendPosition('pos', map.GetCenter(), map.GetZoomLevel(), 0);
}

function setCenter(pos, zoom) {
	map.SetCenterAndZoom(pos, zoom);
	sendPosition('pos', pos, zoom, 0);
}
