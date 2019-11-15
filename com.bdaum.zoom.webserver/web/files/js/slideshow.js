/*
 * (c) 2019 Berthold Daum  
 */
var pushed = 0;
var popped = 0;
var paused = false;
var fullscreen = false;
var queue = [];
var until = now();
var imageDiv, imageDivHeight, imageDivWidth, currentImg, caption, slideNav, slideExpand, slideContract, slideSpeed, header, footer;
var pending = false;
/* Map fields */
var mapIsLoading = 'Map is loading...';
var initFailed = 'Initialization failed';
var pinUrl = "../files/pin.png";
var notFound = 'not found';
var newLocationTitle = "";
var initialDetail = 8;
var initialMapType, locCreated, locTitles, imgDirection, locImage, track, initialPosition;
var locShown = [];
var locShownTitles = [];
var locShownImage = [];

function getViewport() {
	var e = window, a = 'inner';
	if (!('innerWidth' in window)) {
		a = 'client';
		e = document.documentElement || document.body;
	}
	return {
		width: e[a + 'Width'],
		height: e[a + 'Height']
	};
}

function now() {
	return Date.now();
}

function setPaused() {
	paused = true;
	showControls();
}

function showControls() {
	slideNav.style.width = "100%";
	slideNav.style.marginLeft = "0";
	if (currentImg.tagName !== 'IMG') {
		var layout = parseInt(currentImg.getAttribute("data-layout"));
		if (layout === 5 || layout === 6) {
			slideNav.style.width = "45%";
			if (layout === 5)
				slideNav.style.marginLeft = "55%";
		}
	} 
	slideNav.style.display = "grid";
	slideSpeed.style.display = properties ? "none" : "block";
	if (document.fullscreenEnabled)
		screenControls();
}

function screenControls() {
	slideExpand.style.display = fullscreen ? "none" : "block";
	slideContract.style.display = fullscreen ? "block" : "none";
}

function handleClick() {
	if (!paused && slideNav.style.display !== "grid")
		setPaused();
	else if (caption.style.display !== 'block' && caption.innerHTML !== "")
		caption.style.display = 'block';
	else
		toggleFullscreen();
}

function toggleFullscreen() {
	if (document.fullscreenEnabled) {
		if (fullscreen = !fullscreen) {
			try {
				document.documentElement.requestFullscreen();
			} catch (err) {
			}
			imageDivHeight = imageDiv.style.height;
			imageDivWidth = imageDiv.style.width;
			imageDiv.style.height = screen.height + "px";
			header.style.display = footer.style.display = "none";
		} else {
			try {
				document.exitFullscreen();
			} catch (err) {
			}
			imageDiv.style.height = imageDivHeight;
			imageDiv.style.width = imageDivWidth;
			header.style.display = footer.style.display = "block";
		}
		screenControls();
		resizeStack();
	}
}

function safeValue(prop, dflt) {
	return typeof prop !== 'undefined' ? prop : dflt;
}

function handleZoom(img, captn, prps) {
	var duration = interval;
	if (prps)
		duration = safeValue(prps.dur, duration);
	var cdur = duration;
	if (prps && prps.zoom) {
		var z = 1 + prps.zoom / 100;
		var offx = (100 + prps.zoomX) / 2;
		var offy = (100 + prps.zoomY) / 2;
		img.style.transition = "transform " + duration + "ms ease";
		img.style.transformOrigin = offx + "% " + offy + "%";
		img.style.transform = "scale(" + z + ")";
		cdur = duration * 0.66;
		if (captionDur >= cdur - 500) {
			if (captn) {
				captn.style.transition = "opacity " + cdur + "ms linear";
				captn.style.opacity = 0;
			}
			return;
		}
	}
	if (captn && captionDur < duration - 500) {
		captn.style.transitionProperty = "opacity";
		captn.style.transitionDelay = captionDur + "ms";
		captn.style.transitionDuration = "500ms";
		captn.style.transitionTimingFunction = "linear";
		captn.style.opacity = 0;
	}
}

function hidePrevious(img) {
	imageDiv.childNodes.forEach(function (sib) {
		if (sib.getAttribute("class") === "moreImg" && sib !== img)
			sib.style.opacity = 0;
	});
}

function fadeIn(imageDiv, img, captn, time) {
	fadeout(time, img);
	var fi = 1500;
	var delay = 1500;
	var tick = 30;
	var data = img.getAttribute("data-properties");
	var prps;
	if (data) {
		prps = JSON.parse(data);
		fi = safeValue(prps.in, fi);
		delay = safeValue(prps.dly, delay);
	}
	var currentOpacity = 0;
	var startin = delay - fi;
	if (time >= startin) {
		img.style.opacity = currentOpacity = Math.min((time - startin) / fi, 1);
		if (captn)
			captn.style.opacity = currentOpacity * capOpacity;
	}
	if (currentOpacity < 1)
		window.setTimeout(function () {
			fadeIn(imageDiv, img, captn, time + tick);
		}, tick);
	else {
		hidePrevious(img);
		handleZoom(img, captn, prps);
	}
}

function fadeout(time, img) {
	var fo = 1500;
	imageDiv.childNodes.forEach(function (sib) {
		if (sib.getAttribute("class") === "moreImg" && sib !== img) {
			var data = sib.getAttribute("data-properties");
			fo = data ? JSON.parse(data).out : fo;
		}
	});
	var prevOpacity = Math.max(1 - time / fo, 0);
	imageDiv.childNodes.forEach(function (sib) {
		if (sib.getAttribute("class") === "moreImg" && sib !== img && sib.style.opacity > prevOpacity)
			sib.style.opacity = prevOpacity;
	});
}

function blend(imageDiv, img, captn, time) {
	fadeout(time, img);
	var fi = 1500;
	var delay = 0;
	var effect = -1;
	var tick = 30;
	var prps;
	var data = img.getAttribute("data-properties");
	if (data) {
		prps = JSON.parse(data);
		fi = safeValue(prps.in, fi);
		delay = safeValue(prps.dly, delay);
		effect = safeValue(prps.eff, effect);
	}
	var startin = delay - fi;
	var fac = 0;
	if (time >= startin) {
		var imageW, imageH;
		var vpWidth = imageDiv.clientWidth;
		var vpHeight = imageDiv.clientHeight;
		if (img.tagName === "IMG") {
			imageW = img.naturalWidth;
			imageH = img.naturalHeight;
			var f = Math.min((vpWidth - 6) / imageW, (vpHeight - 6) / imageH);
			imageW *= f;
			imageH *= f;
		} else {
			imageW = vpWidth;
			imageH = vpHeight;
		}
		var endX = (vpWidth - imageW) / 2;
		var endY = (vpHeight - imageH) / 2;
		var xOff = endX;
		var yOff = endY;
		var width = imageW;
		var height = imageH;
		fac = Math.min(1, (time - startin) / fi);
		switch (effect) {
			case 1:  // move left
				xOff = fac * endX + (1 - fac) * vpWidth;
				break;
			case 2: // move right
				xOff = fac * endX - (1 - fac) * imageW;
				break;
			case 3: // move up
				yOff = fac * endY + (1 - fac) * vpHeight;
				break;
			case 4: // move down
				yOff = fac * endY - (1 - fac) * imageH;
				break;
			case 5: // move topleft
				xOff = fac * endX + (1 - fac) * vpWidth;
				yOff = fac * endY + (1 - fac) * vpHeight;
				break;
			case 6: // move topright
				xOff = fac * endX - (1 - fac) * imageW;
				yOff = fac * endY + (1 - fac) * vpHeight;
				break;
			case 7: // move bottomleft
				xOff = fac * endX + (1 - fac) * vpWidth;
				yOff = fac * endY - (1 - fac) * imageH;
				break;
			case 8: // move bottomright
				xOff = fac * endX - (1 - fac) * imageW;
				yOff = fac * endY - (1 - fac) * imageH;
				break;
			case 9: // blend left
				width = Math.max(1, fac * imageW);
				xOff = endX + imageW - width;
				break;
			case 10: // blend right
				width = Math.max(1, fac * imageW);
				break;
			case 11: // blend up
				height = Math.max(1, fac * imageH);
				yOff = endY + imageH - height;
				break;
			case 12: // blend down
				height = Math.max(1, fac * imageH);
				break;
			case 13: // blend topleft
				width = Math.max(1, fac * imageW);
				xOff = endX + imageW - width;
				height = Math.max(1, fac * imageH);
				yOff = endY + imageH - height;
				break;
			case 14: // blend topright
				width = Math.max(1, fac * imageW);
				height = Math.max(1, fac * imageH);
				yOff = endY + imageH - height;
				break;
			case 15: // blend bottomleft
				width = Math.max(1, fac * imageW);
				xOff = endX + imageW - width;
				height = Math.max(1, fac * imageH);
				break;
			case 16: // blend bottomright
				width = Math.max(1, fac * imageW);
				height = Math.max(1, fac * imageH);
				break;
			default:  // expand
				width = Math.max(1, fac * imageW);
				height = Math.max(1, fac * imageH);
				xOff = endX + (imageW - width) / 2;
				yOff = endY + (imageH - height) / 2;
				break;
		}
		img.style.marginLeft = xOff + "px";
		img.style.marginTop = yOff + "px";
		if (width < imageW || height < imageH)
			img.style.clip = "rect(0px " + Math.floor(width) + "px " + Math.floor(height) + "px 0px)";
		img.style.opacity = 1;
		if (captn && img.tagName === "IMG") {
			captn.style.bottom = yOff + "px";
			captn.style.left = xOff + "px";
			captn.style.opacity = capOpacity;
		}
	}
	if (fac < 1)
		window.setTimeout(function () {
			blend(imageDiv, img, captn, time + tick);
		}, tick);
	else {
		hidePrevious(img);
		img.style.clip = "auto";
		handleZoom(img, captn, prps);
	}
}

function resizeStack() {
	imageDiv.childNodes.forEach(function (child) {
		if (child.getAttribute("class") === "moreImg")
			resizeImg(child);
	});
	if (currentImg.tagName === "IMG")
		resizeCaption(currentImg);
}

function resizeCaption(img) {
	caption.style.transition = "";
	caption.style.width = img.width + "px";
	caption.style.padding = "15px";
	caption.style.bottom = img.style.marginTop;
	caption.style.left = img.style.marginLeft;
}

function resizeImg(img) {
	var vpWidth = imageDiv.clientWidth;
	var vpHeight = imageDiv.clientHeight;
	if (img.tagName === "IMG") {
		img.style.imageOrientation = "from-image";
		var imgWidth = img.naturalWidth;
		var imgHeight = img.naturalHeight;
		var fac = Math.min((vpWidth - 6) / imgWidth, (vpHeight - 6) / imgHeight);
		var w = img.width = imgWidth * fac;
		var h = img.height = imgHeight * fac;
		img.style.marginLeft = ((vpWidth - w) / 2) + "px";
		img.style.marginTop = ((vpHeight - h) / 2) + "px";
	} else {
		img.style.width = vpWidth + "px";
		img.style.height = vpHeight + "px";
	}
}

function addListeners(el) {
	el.addEventListener("click", handleClick);
}

function removeListeners(el) {
	el.removeEventListener("click", handleClick);
}

function createSectionText(parent, title, text) {
	var s = document.createElement('section');
	s.setAttribute('class', 'sectionTitleSection');
	parent.appendChild(s);
	var h1 = document.createElement('h1');
	h1.setAttribute('class', 'sectionTitle');
	h1.innerText = title;
	s.appendChild(h1);
	if (text && text !== "") {
		var h3 = document.createElement('h3');
		h3.setAttribute('class', 'sectionText');
		h3.innerHTML = text;
		s.appendChild(h3);
	}
}

function getNextImages(start, layout) {
	var indexes = [];
	var last = null;
	for (i = start; i < images.length; i++) {
		if (images[i] === null)
			break;
		if (layout < 5)
			indexes.push(i);
		else if (i < properties.length) {
			var props = properties[i];
			if (props) {
				var prps = JSON.parse(props);
				if (typeof prps.lat !== 'undefined' && typeof prps.lon !== 'undefined') {
					if (last && dist(prps.lat, prps.lon, last.lat, last.lon) < 0.02)
						continue;
					last = prps;
					indexes.push(i);
				}
			}
		}
	}
	return indexes;
}

function dist(lat1, lon1, lat2, lon2) {
	var degToRad = Math.PI / 180.0;
	var phi1 = degToRad * lat1;
	var phi2 = degToRad * lat2;
	return 6371.01 * Math.acos(Math.sin(phi1) * Math.sin(phi2) + Math.cos(phi1) * Math.cos(phi2) * Math.cos(degToRad * (lon2 - lon1)));
}

function createThumbnails(parent, indexes, cols) {
	var s = document.createElement('section');
	s.setAttribute('class', 'sectionThumbSection');
	parent.appendChild(s);
	s.style.display = "grid";
	s.style.gridTemplateColumns = "repeat(" + cols + ", 1fr)";
	for (let i in indexes) {
		var img = new Image();
		img.onload = makeThumbnailCallback(img);
		img.src = images[indexes[i]] + ".jpg";
		img.setAttribute('class', 'sectionThumb');
		s.appendChild(img);
	}
}

function makeThumbnailCallback(img) {
	return function () {
		var w = thumbSize;
		imageW = img.naturalWidth;
		imageH = img.naturalHeight;
		if (imageW < imageH) {
			w *= imageW / imageH;
			img.style.marginLeft = (thumbSize - w) / 2 + "%";
		}
		img.style.width = w + "%";
	};
}

function createMap(parent) {
	var d = document.createElement('div');
	d.setAttribute('class', 'map');
	d.style.position = "relative";
	var vpWidth = imageDiv.clientWidth;
	d.style.width = (vpWidth / 2) + "px";
	d.style.height = "100%";
	parent.appendChild(d);
}

function drawSection(title, props) {
	var layout = 0;
	var description;
	if (props) {
		var prps = JSON.parse(props);
		if (prps.lay)
			layout = prps.lay;
		description = prps.descr;
	}
	var vpWidth = imageDiv.clientWidth;
	var vpHeight = imageDiv.clientHeight;
	var sectionDiv = document.createElement('div');
	var cols;
	sectionDiv.setAttribute('class', 'sectionDiv');
	sectionDiv.style.width = vpWidth + "px";
	sectionDiv.style.height = vpHeight + "px";
	var imgs = getNextImages(pushed + 1, layout);
	var l = imgs.length;
	if (l === 0)
		layout = 0;
	sectionDiv.setAttribute("data-layout", layout);
	switch (layout) {
		case 1: // left
		case 2: // right
		case 5: // map left
		case 6: // map right
			cols = Math.min(3, Math.ceil(Math.sqrt(l)));
			sectionDiv.style.display = "grid";
			sectionDiv.style.gridGap = "1em";
			sectionDiv.style.gridTemplateColumns = "auto auto";
			break;
		case 3: // top
		case 4: // bottom
			var rows = Math.min(2, Math.floor(Math.sqrt(l)));
			cols = Math.floor((l + rows - 1) / rows);
			sectionDiv.style.display = "grid";
			sectionDiv.style.gridGap = "1em";
			sectionDiv.style.gridTemplateColumns = "auto";
			break;
	}
	switch (layout) {
		case 1: // left
		case 3: // top
			createThumbnails(sectionDiv, imgs, cols);
			createSectionText(sectionDiv, title, description);
			break;
		case 2: // right
		case 4: // bottom
			createSectionText(sectionDiv, title, description);
			createThumbnails(sectionDiv, imgs, cols);
			break;
		case 5: // map left
			createMap(sectionDiv);
			createSectionText(sectionDiv, title, description);
			break;
		case 6: // map right
			createSectionText(sectionDiv, title, description);
			createMap(sectionDiv);
			break;
		default:
			createSectionText(sectionDiv, title, description);
	}
	return sectionDiv;
}

function deactivateMap() {
	var map = document.getElementById("map");
	if (map) {
		disposeMap();
		map.setAttribute("id", null);
	}
}

function activateMap(element) {
	var maps = element.getElementsByClassName("map");
	if (maps && maps.length > 0) {
		var map = maps[0];
		map.setAttribute("id", "map");
		locCreated = track = [];
		locTitles = [];
		locImage = [];
		var pos = parseInt(currentImg.getAttribute("data-pos"));
		var indexes = getNextImages(pos + 1, 5);
		for (let i in indexes) {
			var index = indexes[i];
			var props = properties[index];
			if (props) {
				var prps = JSON.parse(props);
				if (typeof prps.lat !== 'undefined' && typeof prps.lon !== 'undefined') {
					locCreated.push(prps);
					locTitles.push(titles[index]);
					locImage.push([images[index]]);
				}
			}
		}
		setupMap();
	}
}

function showSlides() {
	if (queue.length === 0 && !pending) {
		if (pushed < images.length) {
			var props;
			if (pushed < properties.length)
				props = properties[pushed];
			var imageId = images[pushed];
			var vp = getViewport();
			var img;
			if (imageId === null) {
				img = drawSection(titles[pushed], props);
				img.style.opacity = 0;
				queue.unshift(img);
			} else {
				img = new Image();
				img.style.opacity = 0;
				var size = Math.max(vp.width, vp.height);
				if (props) {
					var prps = JSON.parse(props);
					if (prps.zoom)
						size = Math.floor(size * (1 + prps.zoom / 100));
				}
				img.src = "jpg/" + imageId + "_" + size;
				pending = true;
				img.onload = function () {
					queue.unshift(img);
					pending = false;
				};
			}
			img.setAttribute("data-pos", pushed);
			if (pushed < titles.length)
				img.setAttribute("data-title", titles[pushed]);
			if (props)
				img.setAttribute("data-properties", props);
			++pushed;
		} else if (pushed++ === images.length && images.length > 0) {
			var img = new Image(300, 300);
			img.src = "../files/reload.png";
			img.setAttribute("data-pos", -2);
			img.style.opacity = 0;
			img.addEventListener("click", reload);
			queue.unshift(img);
		}
	}
	if (now() >= until && !paused && queue.length > 0) {
		if (++popped <= images.length + 2) {
			currentImg = queue.pop();
			var title = currentImg.getAttribute("data-title");
			var pos = parseInt(currentImg.getAttribute("data-pos"));
			var showCaption = title && title !== "" && currentImg.tagName === "IMG";
			try {
				imageDiv.removeChild(caption);
			} catch (err) { }
			currentImg.setAttribute('class', 'moreImg');
			var children = imageDiv.childNodes;
			children.forEach(function (child) {
				removeListeners(child);
			});
			resizeImg(currentImg);
			if (pos >= 0) {
				resizeCaption(currentImg);
				addListeners(currentImg);
				if (popped === images.length + 2 && fullscreen)
					toggleFullscreen();
			}
			deactivateMap();
			imageDiv.appendChild(currentImg);
			activateMap(currentImg);
			if (children.length > 5) {
				var first = children[0];
				imageDiv.removeChild(first);
				children[1].className = first.className;
			}
			if (showCaption) {
				caption.style.transition = "";
				caption.style.opacity = 0;
				caption.innerText = title ? title : "";
				imageDiv.appendChild(caption);
			}
			var effect = 0;
			var duration = pos >= 0 ? interval : 1000;
			var data = currentImg.getAttribute("data-properties");
			if (data) {
				var prps = JSON.parse(data);
				duration = safeValue(prps.iurn, duration);
				effect = safeValue(prps.eff, 0);
				if (effect === 17)  // random
					effect = Math.floor((Math.random() * 18 - 1));
			}
			if (effect !== 0 && effect <= 16)
				blend(imageDiv, currentImg, showCaption ? caption : null, 0)
			else
				fadeIn(imageDiv, currentImg, showCaption ? caption : null, 0);
			until = now() + duration;
		}
	}
	if (popped <= images.length + 2)
		window.setTimeout(showSlides, 250);
}

function forwards() {
	hideControls();
	until = now();
	paused = false;
}

function hideControls() {
	slideNav.style.display = slideExpand.style.display = slideContract.style.display = slideSpeed.style.display = "none";
}

function backwards() {
	paused = true;
	var children = imageDiv.childNodes;
	for (i = children.length - 1; i > 0; i--) {
		if (children[i].tagName === "IMG" || children[i].getAttribute("class") === "moreImg") {
			var previous = children[i - 1];
			var title = previous.getAttribute("data-title");
			if (typeof title !== 'undefined') {
				var current = children[i];
				previous.style.opacity = 1;
				previous.style.transition = previous.style.transform = "";
				current.style.opacity = 0;
				try {
					imageDiv.removeChild(caption);
				} catch (err) { }
				deactivateMap();
				imageDiv.removeChild(current);
				removeListeners(current);
				currentImg = previous;
				if (currentImg.tagName === "IMG") {
					caption.innerText = title ? title : "";
					resizeCaption(currentImg);
					caption.style.opacity = capOpacity;
					imageDiv.appendChild(caption);
				} else
					caption.style.opacity = 0;
				addListeners(currentImg);
				queue.push(current);
				--popped;
				until = now() + interval;
				hideControls();
				paused = false;
				return;
			}
		}
	}
}

function reload() {
	queue = [];
	popped = pushed = 0;
	paused = false;
	until = now();
	start();
}

function toggleCaption() {
	caption.style.display = caption.style.display !== 'block' ? 'block' : 'none';
}

function start() {
	var img = new Image(200, 200);
	img.src = "../files/loading.svg";
	img.setAttribute("data-pos", -1);
	img.setAttribute("data-properties", '{"dur" : 3000}');
	pending = true;
	img.onload = function () {
		queue.unshift(img);
		pending = false;
	};
	showSlides();
}

function resizeWindow() {
	if (window.fullscreen !== fullscreen)
		toggleFullscreen();
	else if (window.fullScreen !== fullscreen)
		toggleFullscreen();
	else {
		setSize();
		resizeStack();
	}
}

function setSize() {
	var vp = getViewport();
	imageDiv.style.width = vp.width + "px";
	imageDiv.style.height = (vp.height - header.clientHeight - footer.clientHeight)
		+ "px";
}

function updateInterval() {
	var list = document.getElementsByClassName("radio");
	for (let i in list)
		if (list[i].checked) {
			interval = list[i].value * 1000;
			break;
		}
}

window.onload = function () {
	header = document.getElementById("slideshowHeader");
	footer = document.getElementById("slideshowFooter");
	slideNav = document.getElementById("slideNav");
	slideExpand = document.getElementById("slideExpand");
	slideContract = document.getElementById("slideContract");
	slideSpeed = document.getElementById("slideSpeed");
	imageDiv = document.getElementById("imageDiv");
	document.getElementById("backwards").addEventListener("click", backwards);
	document.getElementById("forwards").addEventListener("click", forwards);
	caption = document.getElementById('caption');
	caption.addEventListener("click", toggleCaption);
	caption.style.display = 'block';
	document.getElementById("expand").addEventListener("click",
		toggleFullscreen);
	document.getElementById("contract").addEventListener("click",
		toggleFullscreen);
	document.body.style.overflow = "hidden";
	updateInterval();
	setSize();
	start();
}

window.onresize = resizeWindow;

window.onpagehide = function () {
	if (!paused)
		setPaused();
}

window.onunload = function () {
	paused = true;
	popped = images.length;
}