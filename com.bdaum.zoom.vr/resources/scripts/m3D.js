// =================================================================================
// Base script: 3D gallery HTML 5 CANVAS experiment II
//              script written by Gerard Ferrandez - http://www.dhteumeuleu.com/
//              3D projection adapted from http://tulrich.com/geekstuff/canvas/perspective.html
// License: Creative Commons
// Extensions: National Museum of China (http://www.visitnmc.com/)
// Adaptations for ZoRa exhibitions: Berthold Daum (www.photozora.org)
// =================================================================================

var m3D = function () {
    // ---- private vars ----
    var 
		ctx,
		canvasBuffer,
		btx,
		points,
		nPoints,
		nFaces,
		scene,
		wireframe = false,
		vRotationEnabled = true,
		targetImage,
		imageOver,
		imageOverBak,
		background,
		screen = {},
		mouse = {
		    mov: -5,
			ctrl: false
		},
		weights = [ 0.01, 0.04, 0.12, 0.25, 0.5],
		inertia = 2,
		fps = 0,
		npoly = 0;
    //extend 
    var extend, tags = [];
    // ---- camera ----
    var camera = {
        x: 0,
        z: 0,
        angleTargetX: 0,
        angleTargetZ: 0,
        angleH: 0,
        angleV: 0,
        focalLength: 1000
    };

	var getNextScene = function () {
		var roomId = scene.id;
		for (i=0; i<room_ids.length; i++) {
			if (roomId == room_ids[i]) {
				var next = i + 1;
				if (next >= room_ids.length)
					next = 0;
				return room_ids[next];
			}
		}
		return roomId;
	}
	
    /* ==== main loop ==== */
    var run = function () {
        //extend---map draw
        extend.mapDraw();
        if (stopDraw)
            return;
        // ---- camera movements ----
		if (mouse.ctrl)
			mouse.mov = 0;
		else
			camera.movements();
        // ---- gradient background ----
        if (vRotationEnabled || !background) {
            var horizon = 0.5 + camera.angleV * 1.2;
            if (horizon < 0) horizon = 0; else if (horizon > 1) horizon = 1;
            background = btx.createLinearGradient(0, 0, 0, screen.h);
            background.addColorStop(0, scene.ceilingColor);
            background.addColorStop(horizon, scene.horizonColor);
            background.addColorStop(horizon, scene.horizonColor);
            background.addColorStop(1, scene.groundColor);
        }
        // ---- fill background ----
        btx.fillStyle = background;
        btx.fillRect(0, 0, screen.w, screen.h);
        // ---- points 3D to 2D projection ----
        var i = nPoints;
        while (i--) points[i].projection();
        // ---- compute faces ----
        imageOver = null;
        i = nFaces;
        while (i--) faces[i].compute();
        // ---- z sorting ----
        faces.sort(function (p0, p1) {
            return p1.zIndex - p0.zIndex;
        });
        // ---- draw faces in z order ----
        for (var i = 0, p; p = faces[i++]; ) p.visible && p.draw();
        // ---- mouse over cursor ----
        if (!imageOverBak) {
            if (imageOver) {
                screen.div.style.cursor = "pointer";
            }
        } else if (!imageOver) 
            screen.div.style.cursor = "default";
        imageOverBak = imageOver;

        //extend---focus check
        extend.focusCheck();

        if (extend.focusTag && extend.focusTag.len == 10 && extend.focusTag.src.match(/door\.png$/))
            changeRoom(getNextScene());
        // ---- loop ----
		ctx.drawImage(canvasBuffer, 0, 0);
        fps++;
    };

    /* ==== Camera translations and rotations ==== */
    camera.movements = function () {
        // ---- translation ----
        this.x += (this.targetX - this.x) * .05;
        this.z += (this.targetZ - this.z) * .05;
        this.normalLength = Math.sqrt(this.x * this.x + this.z * this.z);
        // ---- Y axis Rotation ----
        var angleH = (
			(mouse.mov > 0 ? ((mouse.x - mouse.xd) / screen.md) : 0) +
			Math.atan2(this.angleTargetX - this.x, this.angleTargetZ - this.z)
			) % (2 * Math.PI);
        // ---- normalize quadran ----
        if (Math.abs(angleH - this.angleH) > Math.PI) {
            if (angleH < this.angleH) this.angleH -= 2 * Math.PI;
            else this.angleH += 2 * Math.PI;
        }
        // ---- easing and trigo ----
        this.angleH += (angleH - this.angleH) * weights[inertia];
        this.cosh = Math.cos(this.angleH);
        this.sinh = Math.sin(this.angleH);
        // ---- X axis Rotation ----
        if (vRotationEnabled) {
            this.angleV += (((screen.mh - mouse.y) * 0.002) - this.angleV) * 0.1;
			this.angleV = Math.min(0.5 , Math.max(-0.5,this.angleV));
            this.cosv = Math.cos(this.angleV);
            this.sinv = Math.sin(this.angleV);
        }
    };

    /////////////////////////////////////////////////////////////////////////////////////
    /* ==== points constructor ==== */
    var Point = function (x, y, z, tx, ty) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.tx = tx;
        this.ty = ty;
        this.projection();
    };

    /* ==== bisection constructor ==== */
    var Bisection = function (p0, p1) {
        this.x = (p1.x + p0.x) * 0.5;
        this.y = (p1.y + p0.y) * 0.5;
        this.z = (p1.z + p0.z) * 0.5;
        this.tx = (p1.tx + p0.tx) * 0.5;
        this.ty = (p1.ty + p0.ty) * 0.5;
        this.projection();
    };

    /* ==== 3D to 2D projection ==== */
    Point.prototype.projection = Bisection.prototype.projection = function () {
        // ---- 3D coordinates ----
        var nx = this.x - camera.x;
        var nz = this.z - camera.z;
        // ---- horizontal rotation ----
        var tx = camera.cosh * nx - camera.sinh * nz;
        this.zp = camera.sinh * nx + camera.cosh * nz;
        if (vRotationEnabled) {
            // ---- vertical rotation enabled ----
            var ty = camera.cosv * this.y - camera.sinv * this.zp;
            this.zp = camera.sinv * this.y + camera.cosv * this.zp;
        } else {
            // ---- vertical rotation disabled ----
            ty = this.y;
        }
        // ---- 2D projection ----
        this.scale = camera.focalLength / Math.max(1, this.zp);
        this.xp = screen.mw + tx * this.scale;
        this.yp = screen.mh - ty * this.scale;
    };

    /* ==== add new point ==== */
    var addPoint = function (x, y, z, tx, ty) {
        var i = 0, p;
        while (p = points[i++]) {
            // return point
            if (x == p.x && y == p.y && z == p.z) return p;
        }
        // create new point
        nPoints++;
        points.push(p = new Point(x, y, z, tx, ty));
        return p;
    };

    /////////////////////////////////////////////////////////////////////////////////////
    /* ==== 3D image constructor ==== */
    var ProjectedImage = function (face, p) {
        for (var i in p) this[i] = p[i];
        this.face = face;
        // ---- target position when selected ----
        this.targetX = this.x + Math.cos(this.angle / 180 * Math.PI) * this.distView;
        this.targetZ = this.z + Math.sin(this.angle / 180 * Math.PI) * this.distView;
        // ---- create canvas image ----
        this.srcImg = new Image();
        this.srcImg.src = scene.imagesPath + p.src;
        // ---- center point ----
        this.pc = new Point(this.x, this.y, this.z);
        //extend---push tag in tags
        if (this.tagid) 
            tags.push(this);
    };

    /* ==== target image ==== */
    ProjectedImage.prototype.select = function () {
        targetImage = imageOver;
        camera.targetX = targetImage.targetX;
        camera.targetZ = targetImage.targetZ;
        camera.angleTargetX = targetImage.x;
        camera.angleTargetZ = targetImage.z;
        mouse.mov = -5;
        mouse.xd = mouse.x;

        if (imageOver.tagid)
            extend.focusTag = imageOver;
    };

    /* ==== loading image ==== */
    ProjectedImage.prototype.loading = function () {
        if (this.srcImg.complete) {
            this.face.image = this;
            this.face.preImage = false;
            // ---- get image size ----
            var zoom = this.zoom || 1;
            var tw = this.srcImg.width * zoom * 0.5;
            var th = this.srcImg.height * zoom * 0.5;
            // ---- create points ----
            var dx = Math.sin(this.angle / 180 * Math.PI);
            var dz = Math.cos(this.angle / 180 * Math.PI);
            this.p0 = addPoint(this.x + (tw * dx), this.y + th, this.z - (tw * dz), 0, 0);
            this.p1 = addPoint(this.x - (tw * dx), this.y + th, this.z + (tw * dz), this.srcImg.width, 0);
            this.p2 = addPoint(this.x - (tw * dx), this.y - th, this.z + (tw * dz), this.srcImg.width, this.srcImg.height);
            this.p3 = addPoint(this.x + (tw * dx), this.y - th, this.z - (tw * dz), 0, this.srcImg.height);
        }
    };

    /* ==== draw projected image ==== */
    ProjectedImage.prototype.draw = function () {
        var image = this.srcImg, k = 0;
        /* ==== recursive triangulation ===== */
        var triangulate = function (p0, p1, p2, level) {
            if (--level === 0) {
                var ox = offsetX[k];
                var oy = offsetY[k++];
                // ---- clipping ----
                btx.save();
                btx.beginPath();
                btx.moveTo(Math.round(p0.xp + ox), Math.round(p0.yp + oy));
                btx.lineTo(Math.round(p1.xp + ox), Math.round(p1.yp + oy));
                btx.lineTo(Math.round(p2.xp + ox), Math.round(p2.yp + oy));
                btx.clip();
                // ---- transform ----
                var d = p0.tx * (p2.ty - p1.ty) - p1.tx * p2.ty + p2.tx * p1.ty + (p1.tx - p2.tx) * p0.ty;
                btx.transform(
					-(p0.ty * (p2.xp - p1.xp) - p1.ty * p2.xp + p2.ty * p1.xp + (p1.ty - p2.ty) * p0.xp) / d, // m11
					 (p1.ty * p2.yp + p0.ty * (p1.yp - p2.yp) - p2.ty * p1.yp + (p2.ty - p1.ty) * p0.yp) / d, // m12
					 (p0.tx * (p2.xp - p1.xp) - p1.tx * p2.xp + p2.tx * p1.xp + (p1.tx - p2.tx) * p0.xp) / d, // m21
					-(p1.tx * p2.yp + p0.tx * (p1.yp - p2.yp) - p2.tx * p1.yp + (p2.tx - p1.tx) * p0.yp) / d, // m22
					 (p0.tx * (p2.ty * p1.xp - p1.ty * p2.xp) + p0.ty * (p1.tx * p2.xp - p2.tx * p1.xp) + (p2.tx * p1.ty - p1.tx * p2.ty) * p0.xp) / d, // dx
					 (p0.tx * (p2.ty * p1.yp - p1.ty * p2.yp) + p0.ty * (p1.tx * p2.yp - p2.tx * p1.yp) + (p2.tx * p1.ty - p1.tx * p2.ty) * p0.yp) / d  // dy
				);
                if (wireframe) {
                    // ---- wireframe mode ----
                    btx.closePath();
                    btx.strokeStyle = "#fff";
                    btx.stroke();
                } else {
                    btx.drawImage(image, 0, 0);
                }
                btx.restore();
                npoly++;
            } else {
                // ---- subdivision ----
                var p3 = new Bisection(p0, p1);
                var p4 = new Bisection(p1, p2);
                var p5 = new Bisection(p2, p0);
                // ---- recursive call ---- 
                triangulate(p0, p3, p5, level);
                triangulate(p3, p1, p4, level);
                triangulate(p5, p4, p2, level);
                triangulate(p5, p3, p4, level);
            }
        };
        // ---- distance from camera ----
        var dx = this.pc.x - camera.x;
        var dz = this.pc.z - camera.z;
        var dist = Math.sqrt(dx * dx + dz * dz);
        // ---- adapt tessellation quality ----
        if (dist > 1000) {
            // ---- 8 triangles ----
            var level = 2;
            var offsetX = [1, -1, -1, 0, 2, 0, 2, 1];
            var offsetY = [2, 2, 0, 1, 1, -1, -1, 0];
        } else {
            // ---- 32 triangles ----
            var level = 3;
            var offsetX = [3, 1, 1, 2, -1, -3, -3, -2, -1, -3, -3, -2, 0, 0, -2, -1, 4, 2, 4, 3, 0, -2, 0, -1, 4, 2, 4, 3, 3, 1, 1, 2];
            var offsetY = [4, 4, 2, 3, 4, 4, 2, 3, 0, 0, -2, -1, 1, 3, 1, 2, 3, 1, 1, 2, -1, -3, -3, -2, -1, -3, -3, -2, 0, 0, -2, -1];
        }
        // ---- start triangulation ----
        triangulate(this.p0, this.p1, this.p2, level);
        triangulate(this.p0, this.p2, this.p3, level);
        // ---- on mouse over ----
		if (isPointInPoly([this.p0,this.p1,this.p2,this.p3], mouse.x, mouse.y))
			imageOver = this;
     };

    /////////////////////////////////////////////////////////////////////////////////////
    /* ==== surface constructor ==== */
    var Surface = function (p) {
        // ---- properties ----
        for (var i in p) this[i] = p[i];
        if (!this.shadingLight) this.shadingLight = scene.shadingLight;
        this.alpha = this.fillColor.alpha || 1;
        this.nP = p.x.length;
        if (this.nP < 3 || this.nP > 4) alert("ERROR: triangles or rectangles only");
        // ---- tri/quad points ---- 
        this.p0 = addPoint(p.x[0], p.y[0], p.z[0]);
        this.p1 = addPoint(p.x[1], p.y[1], p.z[1]);
        this.p2 = addPoint(p.x[2], p.y[2], p.z[2]);
        if (this.nP == 4) this.p3 = addPoint(p.x[3], p.y[3], p.z[3]);
        // ---- normal vector for flat shading ----
        this.normalX = ((this.p1.y - this.p0.y) * (this.p2.z - this.p0.z)) - ((this.p1.z - this.p0.z) * (this.p2.y - this.p0.y));
        this.normalY = ((this.p1.z - this.p0.z) * (this.p2.x - this.p0.x)) - ((this.p1.x - this.p0.x) * (this.p2.z - this.p0.z));
        this.normalZ = ((this.p1.x - this.p0.x) * (this.p2.y - this.p0.y)) - ((this.p1.y - this.p0.y) * (this.p2.x - this.p0.x));
        this.normalLength = Math.sqrt(this.normalX * this.normalX + this.normalY * this.normalY + this.normalZ * this.normalZ);
        // ---- create attached image ----
        if (this.image) {
            this.preImage = new ProjectedImage(this, this.image);
            this.image = false;
        }
        // ---- create custom function ----
        this.createFunction();
        nFaces++;
    };

    /* ==== draw shapes ==== */
    Surface.prototype.draw = function () {
        npoly++;
        // ---- shape ----
        btx.beginPath();
        btx.moveTo(this.p0.xp - 0.5, this.p0.yp);
        btx.lineTo(this.p1.xp + 0.5, this.p1.yp);
        btx.lineTo(this.p2.xp + 0.5, this.p2.yp);
        if (this.p3) btx.lineTo(this.p3.xp - 0.5, this.p3.yp);
        if (wireframe) {
            // ---- wireframe mode ----
            btx.closePath();
            btx.strokeStyle = "#fff";
            btx.stroke();
        } else {
            // ---- fill shape ----
            this.light = (this.light > 1.2 ? 1.2 : this.light);
            btx.fillStyle = "rgba(" +
				Math.round(this.fillColor.r * this.light) + "," +
				Math.round(this.fillColor.g * this.light) + "," +
				Math.round(this.fillColor.b * this.light) + "," + this.alpha + ")";
            btx.fill();
        }
        // ---- mouse on shape? ----
		if (isPointInPoly( this.p3 ? [this.p0,this.p1,this.p2,this.p3] : [this.p0,this.p1,this.p2], mouse.x, mouse.y))
			imageOver = this.href ? this : null;
        // ---- draw image ----
        this.image && this.image.draw();
    };
	
	function isPointInPoly(poly, x, y){ //@ http://jsfromhell.com/math/is-point-in-poly [rev. #0] (Jonas Raoni Soares Silva)
		for(var c = false, i = -1, l = poly.length, j = l - 1; ++i < l; j = i)
			((poly[i].yp <= y && y < poly[j].yp) || (poly[j].yp <= y && y < poly[i].yp))
			&& (x < (poly[j].xp - poly[i].xp) * (y - poly[i].yp) / (poly[j].yp - poly[i].yp) + poly[i].xp)
			&& (c = !c);
    return c;
}

    /* ==== z buffering, flat shading ==== */
    Surface.prototype.compute = function () {
        // ---- average z-index ----
        this.zIndex = (this.p0.zp + this.p1.zp + this.p2.zp + (this.p3 ? this.p3.zp : 0)) / this.nP;
        this.visible = (this.zIndex > this.zIdx ? this.zIdx : -200) &&
            // ---- back face culling ----
            (this.alwaysVisible || ((this.p1.yp - this.p0.yp) / (this.p1.xp - this.p0.xp) < (this.p2.yp - this.p0.yp) / (this.p2.xp - this.p0.xp) ^ this.p0.xp < this.p1.xp == this.p0.xp > this.p2.xp));
		if (this.visible) {
            // ---- visible face ----
            this.zIndex += this.zIndexOffset || 0;
            // ---- load image ----
            this.preImage && this.preImage.loading();
            // ---- run custom function ----
            this.run && this.run();
            // ---- flat shading ----
            this.light = this.noShading ? 1 : scene.ambientLight + Math.abs(this.normalZ * camera.cosh - this.normalX * camera.sinh) * this.shadingLight / (camera.normalLength * this.normalLength);
         } 
    };

    /* ==== sprite constructor ==== */
    var Sprite = function (p) {
        for (var i in p) this[i] = p[i];
        this.pc = addPoint(p.x, p.y, p.z);
        // ---- create canvas image ----		
        this.srcImg = new Image();
        this.srcImg.src = scene.imagesPath + p.src;
        this.createFunction();
        nFaces++;
    };

    /* ==== draw sprite ==== */
    Sprite.prototype.draw = function () {
        npoly++;
        this.run && this.run();
        var w = this.w * this.pc.scale;
        var h = this.h * this.pc.scale;
        btx.drawImage(this.srcImg, this.pc.xp - w * 0.5, this.pc.yp - h * 0.5, w, h);
    };

    /* ==== z buffering, loading sprite ==== */
    Sprite.prototype.compute = function () {
        if (this.isLoaded) {
            // ---- z-index ----
            this.visible = this.pc.zp > this.zIdx ? this.zIdx : -200;
			if (this.visible) 
                this.zIndex = (this.zIndexOffset || 0) + this.pc.zp;
        } else {
            if (this.srcImg.complete) {
                // ---- load image ----
                this.isLoaded = true;
                this.w = this.srcImg.width * this.zoom;
                this.h = this.srcImg.height * this.zoom;
            }
        }
    };

    /* ==== create custom objects function ==== */
    Sprite.prototype.createFunction = Surface.prototype.createFunction = function () {
        if (this.code) {
            if (this.code.init) {
                // ---- compile and execute init() function ----
                this.init = new Function(this.code.init);
                this.init();
            }
            // ---- compile run() function ----
            if (this.code.run) this.run = new Function(this.code.run);
        }
    };

    ////////////////////////////////////////////////////////////////////////////

    /* ===== copy JS object ==== */
    var cloneObject = function (obj) {
        if (typeof (obj) != "object" || obj == null) return obj;
        var newObj = obj.constructor();
        for (var i in obj) newObj[i] = cloneObject(obj[i]);
        return newObj;
    };

    /* ==== loading geometry file ==== */
    var loadGeometry = function (roomId) {
		return eval('geometry_'+roomId);
    };

    /* ==== screen dimensions ==== */
    var resize = function () {
        screen.w = screen.div.offsetWidth;
        screen.h = screen.div.offsetHeight;
        screen.md = screen.w / Math.PI * 0.5;
        screen.mw = screen.w / 2;
        screen.mh = screen.h / 2;
        mouse.y = screen.mh;
        // ---- canvas size and position ----
        screen.canvas.width = screen.w;
        screen.canvas.height = screen.h;
        canvasBuffer.width = screen.w;
        canvasBuffer.height = screen.h;
        var o = screen.div;
        for (screen.x = 0, screen.y = 0; o != null; o = o.offsetParent) {
            screen.x += o.offsetLeft;
            screen.y += o.offsetTop;
        }
    };

    /* ==== init geometry ==== */
    var initGeometry = function (roomId) {
        var data = loadGeometry(roomId);
        nPoints = 0;
        nFaces = 0;
        points = [];
        faces = [];
        scene = data.params;
        // ---- create surfaces ----
        var i = 0, p;
        while (p = data.geometry[i++]) {
            // ---- push object geometry ----
            if (p.type == "poly") faces.push(new Surface(p));
            else if (p.type == "sprite") faces.push(new Sprite(p));
            else if (p.type == "object") {
                // ---- object reference ----
                var o = data.objects[p.ref];
                for (var j = 0; j < o.length; j++) {
                    var c = cloneObject(o[j]);
                    for (var k in p) if (!c[k]) c[k] = p[k];
                    for (var k = 0; k < c.x.length; k++) {
                        c.x[k] += p.x;
                        c.y[k] += p.y;
                        c.z[k] += p.z;
                    }
                    // ---- push object geometry ----
                    faces.push(new Surface(c));
                }
            }
        }

        extend.tags = tags;
        extend.scene = scene;
		extend.changeRoom();
        //extend---map init
        extend.mapInit();
    };

    /* ==== init script ==== */
    var init = function (roomId) {
        screen.div = document.getElementById("screen");
        screen.canvas = document.getElementById("canvas");
        ctx = screen.canvas.getContext("2d");
       
	   canvasBuffer = document.createElement('canvas');
       canvasBuffer.width = screen.canvas.width;
       canvasBuffer.height = screen.canvas.height;
       btx = canvasBuffer.getContext('2d');

        extend = new m3D_Extend(camera);
        // ---- init geometry ----
        initGeometry(roomId);
        // ---- events ----
        resize();
        onresize = resize;
        screen.div.onmousemove = function (e) {
            if (stopDraw)
                return;
            if (window.event) e = window.event;
            mouse.x = e.clientX - screen.x;
            mouse.y = e.clientY - screen.y;
 			mouse.ctrl = e.ctrlKey;
            mouse.mov++;
			mouse.ctrl = e.ctrlKey;
        };
        screen.div.onclick = function () {
            if (imageOver) {
 				try {
					// ---- target image ----
					imageOver.select();
				} catch (e) {
					// ---- other target ----
					if (imageOver.href)
						window.location = imageOver.href;
				}
            }
        }
        // ---- starting position ----
        setStartingPos();
        // ---- fps count ----
        setInterval(function () {
            if (!stopDraw) {
				var fpsDiv = document.getElementById('fps');
				if (fpsDiv)
					fpsDiv.innerHTML = fps * 2;
				var npolyDiv = document.getElementById('npoly');
				if (npolyDiv)
					npolyDiv.innerHTML = npoly * 2;
                fps = 0;
                npoly = 0;
            }
        }, 500); // update every 0.5 seconds
        extend.listenKey();
		// ---- start engine ----
        setInterval(run, 16);
    };
	

    ////////////////////////////////////////////////////////////////////////////
    /******************* public **********************/
    
	var setStartingPos = function() {
		mouse.x = screen.w / 2;
        mouse.xd = mouse.x;
        camera.x = scene.startX || 0;
        camera.z = scene.startZ || 0;
        camera.targetX = scene.targetX || 0;
        camera.targetZ = scene.targetZ || 0;
	}

	
	var mapSelect = function (tag) {
        imageOver = tag;
        imageOver.select();
    }

    var changeRoom = function (roomId) {
        extend.clear();
        tags = [];
        background = false;
        initGeometry(roomId);
        resize();
 //       camera.angleTargetX = 0;
 //       camera.angleTargetZ = 10000;
		extend.changeRoom();
		camera.x = scene.startX || 0;
        camera.z = scene.startZ || 0;
        camera.targetX = scene.targetX || 0;
        camera.targetZ = scene.targetZ || 0;
        camera.angleH = camera.angleH - Math.PI;
		camera.angleV = 0;
        mouse.mov = -3;
        mouse.xd = mouse.x;

    }
    var keyEvent = function (keycode) {
        extend.keyEvent(keycode);
    }

	var setVerticalRotation = function(enabled) {
		vRotationEnabled = enabled;
		background = false;
		camera.angleV = 0;
	}
	
	var updateInertia = function(incr) {
		inertia -= incr;
		if (inertia < 0) inertia = 0;
		if (inertia >= 5) inertia = 4;
	}


    return {
        // ---- public functions ----
        init: init,
        mapSelect: mapSelect,
        changeRoom: changeRoom,
        keyEvent: keyEvent,
		setVerticalRotation: setVerticalRotation,
		setStartingPos: setStartingPos,
		updateInertia: updateInertia
    }
} ();
