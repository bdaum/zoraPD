var map;
var mgr;
var marker0inUse = false;
var interval;
var infowindow;
var currentMarker;
var dbl = false;

/// Setup Map
function setupMap() {
    document.getElementById("map").innerHTML = mapIsLoading;
    var mapTypeIds = [];
    for(var type in google.maps.MapTypeId) 
        mapTypeIds.push(google.maps.MapTypeId[type]);
    mapTypeIds.push("OSM");
    var mapTypeControlOptions = { mapTypeIds: mapTypeIds }; 
    var overviewMapControlOptions = { opened: true };
    var center = initialPosition ? initialPosition : new google.maps.LatLng(49.785556, 9.038611);
    if (!initialMapType) 
    	initialMapType =  google.maps.MapTypeId.ROADMAP
    var mapOptions = { center: center,
                       mapTypeControl: true,
                       mapTypeControlOptions: mapTypeControlOptions,
                       scaleControl: true,
                       navigationControl: true,
                       streetViewControl: true, 
                       overviewMapControl: true,
                       overviewMapControlOptions: overviewMapControlOptions,
                       mapTypeId: initialMapType,
                       zoom: initialDetail 
                     };
    map = new google.maps.Map(document.getElementById("map"), mapOptions);
    if (initialPosition) 
    	sendPosition('pos', map.getCenter(), map.getZoom());
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
   if (track.length > 0)
	   setTrack();
   else
       setMarkers();
 }

function moved() {
	 sendPosition('moved',  map.getCenter(), map.getZoom());
}

function unloadMap() {}

function sendPosition(name, point, zoom) {
	sendMessage(name, point.toString()+'&'+zoom);
}

function sendMessage(name, data) {
	window.location.href = 'http://www.bdaum.de/zoom/gmap/'+ (data ? name+'?'+data : name);
}

///Set marker

function setMarkers() {
  var batch = [];
  for (i=0; i < locCreated.length; i++) {
	 var markerLocation = locCreated[i];
     var marker = new google.maps.Marker({ position: markerLocation, map: map, title: locTitles[i] });
     google.maps.event.addListener(marker, 'dblclick', makeDoubleClickCallback(marker, locImage[i]));
     google.maps.event.addListener(marker, 'click', makeClickCallback(marker, markerLocation));
     batch.push(marker);
   }
   setBoundsForShape(locCreated);
   mgr = new MarkerClusterer(map, batch, { gridSize: 50, maxZoom: 15 });
}

function setTrack() {
	var path = new google.maps.Polyline({map: map, path: track, strokeColor: 'red'});
	setBoundsForShape(track);
}

function setBoundsForShape(shape) {
	var bounds;
	  for (i=0; i < shape.length; i++) {
		 var location = shape[i];
		 if (bounds) 
	    	 bounds.extend(location);
	     else
	    	 bounds = new google.maps.LatLngBounds(location,location);
	  }
	  if (bounds && !bounds.getNorthEast().equals(bounds.getSouthWest()))
		 	 setViewPort(bounds);
}
     
function makeDoubleClickCallback(marker, imageAssetId) {
  return function() {
	dbl = true;
    if (infowindow) {
       infowindow.close();
       infowindow = null;
    }
    if (imageAssetId && !currentMarker) {
        currentMarker = marker;
    	sendMessage('info', imageAssetId);
 	}
  };  
}

function showInfo(html) {
  	infowindow = new google.maps.InfoWindow({content: html});
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
		map.setZoom(z < 10 ? z+2 : z+1);
		mgr.resetViewport();
		sendPosition('pos', position, map.getZoom());
 	}
}


///prevent page scroll

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
  var marker;
  var dog = true;
  var noMore = false;
  var dragging = false;
  var mouseMove = google.maps.event.addListener(map, 'mousemove', function(event){
    var cursorPoint = event.latLng;
    if(!noMore){
      var icon = MapIconMaker.createMarkerIcon({width: 36, height: 36, primaryColor: "#00ee00"});
      marker = new google.maps.Marker({  
           position: cursorPoint, map: map, icon: icon, title: newLocationTitle, draggable: true});
      noMore = true;
      // This function deletes the marker when dragged outside map
      var drag = google.maps.event.addListener(marker, 'drag', function(event){
        var markerPoint =  event.latLng;
        dragging = true;
        if(!map.getBounds().contains(markerPoint)){
          marker0inUse = false;
          marker.setMap(null);
          sendMessage('remove');
          // event listeners are deleted to save resources
          google.maps.event.removeListener(mouseMove);
          google.maps.event.removeListener(mapClick);
          google.maps.event.removeListener(mapMouseOut);
          google.maps.event.removeListener(drag);
        }
      });
      var dragEnd = google.maps.event.addListener(marker, 'dragend', function(event){
        var markerPoint =  event.latLng;
        dragging = false;
        if(map.getBounds().contains(markerPoint))
          sendPosition('drag', marker.getPosition(), map.getZoom());
      });
    }
    if (dog)
      marker.setPosition(cursorPoint);
  });
  var mapClick = google.maps.event.addListener(map, 'click', function(){
    sendPosition('click', marker.getPosition(), map.getZoom());
    dog = false;
    // event listeners are deleted to save resources
    google.maps.event.removeListener(mouseMove);
    google.maps.event.removeListener(mapClick);
    google.maps.event.removeListener(mapMouseOut);    
  });
  var mapMouseOut = google.maps.event.addListener(map, 'mouseout', function(){
    if (!dragging) {
      marker0inUse = false;
      marker.setMap(null);
      dog = false;
      // event listeners are deleted to save resources
      google.maps.event.removeListener(mouseMove);
      google.maps.event.removeListener(mapClick);
      google.maps.event.removeListener(mapMouseOut);
    }
  });
}

function setViewPort(bounds) {
	map.fitBounds(bounds);
	sendPosition('pos', map.getCenter(),  map.getZoom());
} 

function setCenter(pos, zoom) {
	map.setCenter(pos, zoom);
	sendPosition('pos', pos, zoom);
} 

