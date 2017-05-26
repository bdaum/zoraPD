var minimapWidth = 200;
var novelBackUp;
var map;
var mgr;
var marker0inUse = false;
var interval;
var currentMarker;
var pending = false;
var dbl;


function MyHandleCredentialsError()
   {
      alert(keyInvalid);
   }

function StyleChangeHandler(event)
   {
   	window.location.href = 'http://www.bdaum.de/zoom/gmap/maptype?'+event.mapStyle;
   }

function ModeChangeHandler(event)
   {
   	 sendMessage('mapmode', map.GetMapMode() == VEMapMode.Mode3D ? '3D' : '');
   }

function ResizeHandler(event) {
	showMiniMap();
}

function showMiniMap() {
     var width = document.getElementById("map").offsetWidth - minimapWidth;
     if (width < 0) width = 0;
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
}

function closePopup() {
	 if (currentMarker) {
	 	currentMarker.SetTitle(currentMarker['hiddenTitle']);
     	currentMarker.SetDescription("");
	 	currentMarker = null;
     	map.SetDefaultInfoBoxStyles();
	 }
}

function clickCallback(event) {
     if (!dbl && event.elementID != null) {
     	var pixel = new VEPixel(event.mapX, event.mapY );
     	var markerPoint = map.PixelToLatLong(pixel);
     	var z = map.GetZoomLevel();
		map.SetCenterAndZoom(markerPoint, z < 13 ? z+2 : z < 18 ? z+1 : z);
		sendPosition('pos', markerPoint, map.GetZoomLevel());
     }
}

function DoubleClickHandler(event) {
	  if (!pending) {
		 dbl = true;
		 closePopup();
	     if (event.elementID != null) {
	     	var shape = map.GetShapeByID(event.elementID);
	     	if (!currentMarker) {
	      		var imageAssetId = shape["imageAssetId"];
	      		if (imageAssetId) {
	        		currentMarker = shape;
	        		pending = true;
	    			sendMessage('info', imageAssetId);
				}	
	    	}
	     }
	  }
}

function MoveHandler() {
	sendPosition('moved', map.getCenter(), map.getZoomLevel());
}


function showInfo(html) {
     if (currentMarker && pending) {
     	currentMarker['hiddenTitle'] = currentMarker.GetTitle();
     	currentMarker.SetTitle("");
     	currentMarker.SetDescription(html);
     	map.ClearInfoBoxStyles();
     	map.ShowInfoBox(currentMarker);
     	pending = false;
	 }
}

/// Setup Map
function setupMap() {
    document.getElementById("map").innerHTML = mapIsLoading;
    var center = initialPosition ? initialPosition : new VELatLong(49.785556, 9.038611);
    if (!initialMapType) 
    	initialMapType = VEMapStyle.Road
     map = new VEMap('map');
     if (applicationKey) {
         map.AttachEvent("oncredentialserror", MyHandleCredentialsError);
         map.SetCredentials(applicationKey);       
     }
     map.LoadMap(center, initialDetail, initialMapType);
     if (initialPosition) 
    	 sendPosition('pos', center, initialDetail);
     showMiniMap();
     map.AttachEvent("onchangemapstyle", StyleChangeHandler);
     map.AttachEvent("oninitmode", ModeChangeHandler);
     map.AttachEvent("onclick",ClickHandler);
     map.AttachEvent("ondoubleclick",DoubleClickHandler);
     map.AttachEvent("onendpan",MoveHandler);
     window.onresize = ResizeHandler;
     map.SetScaleBarDistanceUnit(VEDistanceUnit.Kilometers);
     if (track.length > 0)
  	   setTrack();
     else
       setMarkers();
}

function unloadMap() {
   if (map)
   	  map.Dispose();
}

function sendPosition(name, point, zoom) {
	sendMessage(name, point.toString()+'&'+zoom);
}


function sendMessage(name, data) {
	window.location.href = 'http://www.bdaum.de/zoom/gmap/'+ (data ? name+'?'+data : name);
}


///Set marker
function setMarkers() {
  for (i=0; i < locCreated.length; i++) {
     var shape = new VEShape(VEShapeType.Pushpin, locCreated[i]);
     shape.SetTitle(locTitles[i]);
     shape.Draggable = false;
     shape["imageAssetId"] = locImage[i];
     map.AddShape(shape);
     // No click handler for markers!
  }
  if (locCreated.length > 0)
	  map.SetMapView(locCreated);
}

function setTrack() {
	var path = new VEShape(VEShapeType.Polyline, track);
	path.HideIcon();
	path.SetLineColor(new VEColor(255,0,0,1));
	map.AddShape(path);
	if (track.length > 0)
		map.SetMapView(track);
}

function StartDragHandler(event) {
}

function EndDragHandler(event) {
    var markerPoint =  event.LatLong;
    var latitude = markerPoint.Latitude;
    var shape = event.Shape;
    var bounds = map.GetMapView();
    var topLat = bounds.TopLeftLatLong.Latitude;
    if (latitude > topLat) {
	     shape.onenddrag = null;
	     shape.onstartdrag = null;
	     map.DeleteShape(shape);
	     sendMessage("remove");
	 	 marker0inUse = false;
	} else 
		sendPosition('drag', markerPoint, map.GetZoomLevel());
}

/**
* follow() function
*/

function follow(imageInd){
  if ( map.GetMapMode() == VEMapMode.Mode3D || marker0inUse)
  	return;
  marker0inUse = true;
  var shape = new VEShape(VEShapeType.Pushpin, map.GetCenter());
  shape.SetCustomIcon(markerIconUrl);
  // Allow the pushpin to be dragged
  shape.Draggable = true;

  // Assign the shape drag event handlers
  shape.onstartdrag = StartDragHandler;
  shape.onenddrag = EndDragHandler;
  
  // Show an info box to indicate the pushpin can be dragged.
  shape.SetTitle(newLocationTitle);
  map.AddShape(shape);
  map.ShowInfoBox(shape);
}

function setViewPort(bounds) {
	map.SetMapView([bounds.TopLeftLatLong, bounds.BottomRightLatLong]);
	sendPosition('pos', map.GetCenter(),  map.GetZoomLevel());
} 

function setCenter(pos, zoom) {
	map.SetCenterAndZoom(pos, zoom);
	sendPosition('pos', pos, zoom);
} 

