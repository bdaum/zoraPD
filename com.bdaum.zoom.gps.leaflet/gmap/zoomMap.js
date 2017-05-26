var map;
var marker0inUse = false;
var infowindow;
var currentMarker;
var dbl;

function markerDragEnd(event) {
	var marker = event.target;
	marker.closePopup();
	var markerPoint =  marker.getLatLng();
    var latitude = markerPoint.lat;
    var topLat = map.getBounds().getNorth();
    if (latitude > topLat) {
      marker.off('dragend', markerDragEnd);
      map.removeLayer(marker);
      sendMessage("remove");
 	  marker0inUse = false;
	} else 
      sendPosition('drag', markerPoint, map.getZoom());
}

// Setup Map
function setupMap() {
	noMore = false;
	dragging = false;
    document.getElementById("map").innerHTML = mapIsLoading;
    var center = initialPosition ? initialPosition: L.latLng(49.785556, 9.038611);
    map = L.map("map", { center: center,
    				   zoom: initialDetail,
    				   layers: null,
    				   minZoom: 1
                     });
    L.tileLayer.provider(selectedProvider).addTo(map);
    new L.Control.MiniMap(L.tileLayer.provider(selectedProvider), { toggleDisplay: true }).addTo(map);
    if (initialPosition) 
    	sendPosition('pos', map.getCenter(), map.getZoom());
    map.on('moveend', function() {
    	sendPosition('moved', map.getCenter(), map.getZoom());
    });
    if (track.length > 0)
 	   setTrack();
    else
       setMarkers();

 }


function unloadMap() {}

function sendPosition(name, point, zoom) {
	sendMessage(name, '('+point.lat+','+point.lng+")&"+zoom);
}

function sendMessage(name, data) {
	window.location.href = 'http://www.bdaum.de/zoom/gmap/'+(data ? name+'?'+data : name);
}

// /Set markers
function setMarkers() {
  var markers = L.markerClusterGroup();
  for (i=0; i < locCreated.length; i++) {
	 var markerLocation = locCreated[i];
	 var icon = L.MakiMarkers.icon({icon: "camera", color: "#ee0000", size: "m"});
     var marker = L.marker(markerLocation, {icon: icon, title: locTitles[i] });
     marker.on('dblclick', makeDoubleclickCallback(marker, locImage[i]));
     marker.on('click', makeClickCallback(marker, markerLocation));
     markers.addLayer(marker);
  }
  setBoundsForShape(locCreated);
  map.addLayer(markers);
}

function setBoundsForShape(shape) {
	  var bounds;
	  for (i=0; i < shape.length; i++) {
		 var location = shape[i];
	     if (bounds) 
	    	 bounds.extend(location);
	     else
	    	 bounds = L.latLngBounds(location,location);
	  }
	  if (bounds && !bounds.getNorthEast().equals(bounds.getSouthWest()))
		 	 setViewPort(bounds);
}

function setTrack() {
	var path = L.polyline( track, {color: 'red'}).addTo(map);
	setBoundsForShape(track);
}
     
function makeDoubleclickCallback(marker, imageAssetId) {
  return function() {
	dbl = true;
    if (infowindow) {
       marker.closePopup().unbindPopup();
       infowindow = null;
    }
    if (imageAssetId && !currentMarker) {
        currentMarker = marker;
    	sendMessage('info', imageAssetId);
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
	    sendPosition('pos', position, map.getZoom());
	}
}

// /prevent page scroll
function wheelevent(e) {
  if (!e)
    e = window.event
  if (e.preventDefault)
    e.preventDefault()
  e.returnValue = false;
}

/**
 * follow() function
 */

function follow(imageInd){
  if (marker0inUse) 
    return;
  marker0inUse = true;
  var icon = L.MakiMarkers.icon({icon: "camera", color: "#00ee00", size: "m"});
  var marker = L.marker(map.getCenter(), {icon: icon, title: newLocationTitle, draggable: true}).addTo(map);;
  // This function deletes the marker when dragged outside map
  marker.on('dragend', markerDragEnd);
  marker.bindPopup(newLocationTitle).openPopup();
}

function setViewPort(bounds) {
	map.fitBounds(bounds);
	sendPosition('pos', map.getCenter(),  map.getZoom());
} 

function setCenter(pos, zoom) {
	map.setView(pos,zoom);
	sendPosition('pos', pos, zoom);
} 


