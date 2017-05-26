// =================================================================================
// Base script: National Museum of China (http://www.visitnmc.com/)
// Adaptations for ZoRa exhibitions: Berthold Daum (www.photozora.org)
// =================================================================================
var m3D_Extend = function (camera) {
    // ---- è�šç„¦ç›®æ ‡ ----
    this.focusTag = null;
    this.tags = null;
    this.scene = null;
    // ---- å†…å®¹æ˜¾ç¤ºå±‚ ----
    var contentView = document.createElement('div');
    contentView.className = "contentView";
    contentView.style.display = "none";
    document.getElementById("screen").appendChild(contentView);

    // ---- è§†é¢‘æ˜¾ç¤ºå±‚ ----
    var videoContainer = document.createElement('div');
    videoContainer.className = "click";
    videoContainer.style.display = "none";
    document.getElementById("screen").appendChild(videoContainer);

    var videoCover = document.createElement('div');
    videoCover.style.display = "none";

    // ---- è§†é¢‘å¯¹è±¡ ----
    var videoObj = null;

    // ---- éŸ³é¢‘å¯¹è±¡ ----
    var audioObj = null;

    /******************* map start **********************/
    var mapDiv, mapCanvas, mapCtx, mapRate = .04, mapW, mapH,
        tagW, cameraIcon, mapTaget,
        startx, starty, endx, endy, cameraX, cameraY, mouseX = 0, mouseY = 0;

    // ---- åˆ�å§‹åŒ–é¡¶è§†å›¾ ----
    this.mapInit = function () {
        mapDiv = document.getElementById("map");

        //è®¡ç®—map å®½é«˜
        mapW = this.scene.sceneW * mapRate;
        mapH = this.scene.sceneH * mapRate;

        //Canvas è®¾ç½®
        mapCanvas = document.getElementById("mapCanvas");
        mapCanvas.width = mapW;
        mapCanvas.height = mapH;
        mapCtx = mapCanvas.getContext("2d");

        //æ‘„åƒ�æœºæ˜¾ç¤ºå›¾
        cameraIcon = new Image();
        cameraIcon.src = "../images/camera.png";

        //è®¾ç½®æµ®å±‚æ ·å¼�           
        mapDiv.style.backgroundImage = "url(" + this.scene.map_bg + ")";
		mapDiv.style.backgroundSize = "100%";
        mapDiv.style.height = mapH + "px";
        mapDiv.style.width = mapW + "px";
		var infoText = eval('geometry_'+this.scene.id).params.name;
		var lines = Math.floor((infoText.length + 15) / 16);
		var mapInfo = document.getElementById("mapInfo");
        mapInfo.style.bottom = (mapH + 18 + lines * 12) + "px";
        mapInfo.style.width = mapW + "px";
        mapDiv.style.padding = "10px";
 		mapInfo.innerHTML = infoText;

        //é¼ æ ‡äº‹ä»¶
        mapCanvas.addEventListener("mousemove", function (event) {
            if (event.layerX || event.layerX == 0) {
                mouseX = event.layerX;
                mouseY = event.layerY;
            }
        }, false);
        mapCanvas.addEventListener("mouseout", function (event) {
            mouseX = 0;
            mouseY = 0;
        }, false);
        mapCanvas.addEventListener("mousedown", function (event) {
            if (mapTaget) {
                m3D.mapSelect(mapTaget);
                if (stopDraw) destroy();
            } else {
				m3D.setStartingPos();
			}
        }, false);
		if (this.scene.audio)
			audioOpt.load(this.scene.audio);
    }
	
	this.changeRoom = function() {
		this.focusTag = this.getTagById(this.scene.startIndex);
		if (this.scene.contact)
			document.getElementById("spec").innerHTML=this.scene.contact ? this.scene.contact : "";
	}
	
    // ---- ç»˜åˆ¶åœ°å›¾ ---- 
    this.mapDraw = function () {
        mapCtx.fillStyle = this.scene.map_color;
        mapCtx.fillRect(0, 0, mapW, mapH);
        //        if (stopDraw)
        //            addViewInfo({ name: "tag.id", value: vari++ });
        var i = 0, lWidth = 6, tag, sX, sY, eX, eY, highLight;
        while (tag = this.tags[i]) {
			if (tag.door_color == "none") {
				i++;
				continue;
			}
            //å��æ ‡ç³»è½¬æ�¢                       
            startx = (this.scene.sceneW / 2 + tag.x) * mapRate;
            starty = (this.scene.sceneH / 2 - tag.z) * mapRate;
            tagW = (tag.srcImg.width * tag.zoom || 0) * mapRate || 0;

            mapCtx.beginPath();
            mapCtx.lineWidth = lWidth;

            //åˆ¤å®šç»˜åˆ¶
            highLight = false;
            if (Math.abs(tag.angle) == 90) {
                sX = startx - (tagW / 2);
                eX = startx + (tagW / 2);
                if (tag.angle == -90) {
                    sY = starty + (lWidth / 2);
                    eY = starty + (lWidth / 2);
                }
                else {
                    sY = starty - (lWidth / 2);
                    eY = starty - (lWidth / 2);
                }

                if (sX <= mouseX && mouseX <= eX) {
                    if ((sY - lWidth) <= mouseY && mouseY <= sY || sY <= mouseY && mouseY <= (sY + lWidth))
                        highLight = true;
                }
            }
            else {
                sY = starty - (tagW / 2);
                eY = starty + (tagW / 2);
                if (tag.angle == -180) {
                    sX = startx - (lWidth / 2);
                    eX = startx - (lWidth / 2);
                }
                else {
                    sX = startx + (lWidth / 2);
                    eX = startx + (lWidth / 2);
                }

                if (sY < mouseY && mouseY < eY) {
                    if (sX <= mouseX && mouseX <= (eX + lWidth) || (eX - lWidth) <= mouseX && mouseX <= sX)
                        highLight = true;
                }
            }
			// alert(i + " " + sX + " " + sY + " " + eY + " " + eY);
            mapCtx.lineTo(sX, sY);
            mapCtx.lineTo(eX, eY);
            mapCtx.closePath();

            mapCtx.strokeStyle = "#fff";     
			
			if (highLight) {
       
                mapTaget = tag;
            }
            else {
                if (tag.door_color)
                    mapCtx.strokeStyle = tag.door_color;
                else
                    mapCtx.strokeStyle = this.scene.map_obj_color;

                if (mapTaget == tag)
                    mapTaget = null;
            }

            mapCtx.stroke();

            i++;
        }
        mapCtx.lineWidth = 0;
        if (cameraIcon.complete) {
            cameraX = Math.round((this.scene.sceneW / 2 + camera.x) * mapRate);
            cameraY = Math.round((this.scene.sceneH / 2 - camera.z) * mapRate);

            mapCtx.save();
            mapCtx.translate(cameraX, cameraY);
            mapCtx.rotate(camera.angleH);
            mapCtx.beginPath();
            mapCtx.moveTo(-(cameraIcon.width / 2), -(cameraIcon.height / 2 + 5));
            mapCtx.lineTo(mapW, -(cameraIcon.height / 2 + 5));
            mapCtx.lineTo(mapW, mapH);
            mapCtx.lineTo(-(cameraIcon.width / 2), mapH);
            mapCtx.closePath();
            mapCtx.clip();
            mapCtx.drawImage(cameraIcon, -(cameraIcon.width / 2), -(cameraIcon.height / 2 + 5));
            mapCtx.restore();
        }
    }

    /******************* map end **********************/


    /******************* focus **********************/
    this.focusCheck = function () {
        if (this.focusTag) {
            this.focusTag.len = Math.round(Math.sqrt(Math.pow(this.focusTag.x - camera.x, 2) + Math.pow(this.focusTag.z - camera.z, 2)));
            var angleH = (Math.atan2(this.focusTag.x - camera.x, this.focusTag.z - camera.z)) % (2 * Math.PI);
            //            addViewInfo({ name: "this.focusTag.len", value: this.focusTag.len });
            //            addViewInfo({ name: "camera.angleH", value: Math.round(camera.angleH) });

            if (this.focusTag.len == this.focusTag.distView && Math.abs(angleH - camera.angleH) < .0001) {
                //foucs         
                if (contentView.style.display == "none") {
                    contentOpt.show();
                    if (this.focusTag.video)
                        videoOpt.viewCover();
                    if (this.focusTag.audio) 
                        audioOpt.load(this.focusTag.audio);
                }
            }
            else if (contentView.style.display != "none") {
                destroy();
            }

        }
    }
    /******************* focus end **********************/


    /******************* keyboard operating  **********************/
    var ex = this;
    this.listenKey = function () {
        if (document.addEventListener) {
            document.addEventListener("keyup", getKey, false);
        }
        else if (document.attachEvent) {
            document.attachEvent("onkeyup", getKey);
        }
        else {
            document.onkeyup = getKey;
        }
    }
    var getKey = function (e) {
        e = e || window.event;
        var keycode = e.which ? e.which : e.keyCode; //æŒ‰é”®å€¼  
        keyEvent(keycode);
    }
    this.keyEvent = function (keycode) {
        keyEvent(keycode);
    }
    var keyEvent = function (keycode) {
        if (ex.focusTag == null) {
            ex.focusTag = getTagById(1);
        }
        var originTagId = ex.focusTag.tagid, currentTagId = 0;
        if (stopDraw) destroy();
        if (keycode == 37)//left
        {
            if (originTagId == 1)
                currentTagId = ex.scene.imgCount;
            else
                currentTagId = originTagId - 1;

            ex.focusTag = getTagById(currentTagId);
            camera.angleTargetX = ex.focusTag.x;
            camera.angleTargetZ = ex.focusTag.z;

            //ui view
            ui.lightKey('keyArrowLeft');
        }
        else if (keycode == 39)//right
        {
            if (originTagId == ex.scene.imgCount)
                currentTagId = 1;
            else
                currentTagId = originTagId + 1;

            ex.focusTag = getTagById(currentTagId);
            camera.angleTargetX = ex.focusTag.x;
            camera.angleTargetZ = ex.focusTag.z;

            //ui view
            ui.lightKey('keyArrowRight');
        }
        else if (keycode == 38)//up
        {
            m3D.mapSelect(ex.focusTag);
            //ui view
            ui.lightKey('keyArrowUp');
        }
        else if (keycode == 40)//back
        {
            goback();
            //ui view
            ui.lightKey('keyArrowDown');
        }
    }
    var goback = function () {
        if (ex.scene.sceneW == 2000) {
            if (ex.focusTag.angle == -90 && camera.targetZ > 0)
                camera.targetZ -= 500;
            else if (ex.focusTag.angle == 0 && camera.targetX < 0)
                camera.targetX += 500;
            else if (ex.focusTag.angle == 90 && camera.targetZ < 0)
                camera.targetZ += 500;
            else if (ex.focusTag.angle == -180 && camera.targetX > 0)
                camera.targetX -= 500;
        }
        else {
            if (ex.focusTag.angle == -90 && camera.targetZ >= -500)
                camera.targetZ -= 500;
            else if (ex.focusTag.angle == 0 && camera.targetX <= 500)
                camera.targetX += 500;
            else if (ex.focusTag.angle == 90 && camera.targetZ <= 500)
                camera.targetZ += 500;
            else if (ex.focusTag.angle == -180 && camera.targetX >= -500)
                camera.targetX -= 500;
        }
    }
    /******************* keyboard operating end  **********************/


    /******************* tool func  **********************/
    var getTagById = function (tagId) {
        for (var i = 0; i < ex.tags.length; i++) {
            if (ex.tags[i].tagid == tagId)
                return ex.tags[i];
        }
        return ex.tags[ex.scene.startIndex];
    }
    this.getTagById = function (tagId) {
        return getTagById(tagId);
    }
    var videoOpt =
    {
        viewCover: function () {
            stopDraw = true;
            var posi = tagPosi();
            videoContainer.style.display = "block";
            videoContainer.style.left = posi.x + "px";
            videoContainer.style.top = posi.y + "px";
            videoContainer.style.width = posi.w + "px";
            videoContainer.style.height = posi.h + "px";

            videoCover.style.display = "block";
            videoCover.className = "videoCover";
            if (ex.focusTag.w) {
                videoCover.style.width = ex.focusTag.w + "px";
                videoCover.style.height = ex.focusTag.h + "px";
            }
            if (ex.focusTag.marginLeft) {
                videoCover.style.marginLeft = ex.focusTag.marginLeft;
                videoCover.style.marginTop = ex.focusTag.marginTop;
            }
            videoContainer.appendChild(videoCover);


            videoCover.opt = this;
            videoCover.onclick = function () {
                //éŸ³æ•ˆå�œæ­¢
                audioOpt.pause();
                //åŠ è½½è§†é¢‘
                this.opt.load(ex.focusTag.video);
                //æ’­æ”¾
                this.opt.play();
            };

        },
        load: function (file) {
            //åŽ»æŽ‰è’™å±‚
            var child = videoContainer.firstChild;
            if (child) videoContainer.removeChild(child);
            //åˆ›å»ºvideoå¯¹è±¡
            videoObj = document.createElement("video");
            videoObj.src = file;
            videoObj.controls = false;
            videoObj.loop = "loop";
            if (ex.focusTag.marginLeft) {
                videoObj.style.marginLeft = ex.focusTag.marginLeft;
                videoObj.style.marginTop = ex.focusTag.marginTop;
            }
            videoContainer.appendChild(videoObj);
        },
        play: function () {
            if (videoObj) {
                videoObj.play();
            }
        },
        pause: function () {
            if (videoObj)
                videoObj.pause();
        }

    };
    var audioOpt =
    {
        load: function (file) {
            if (audioObj) {
                audioObj.pause();
                document.body.removeChild(audioObj);
            }
            audioObj = document.createElement("audio");
            audioObj.preload = "auto";
            audioObj.loop = "loop";
            audioObj.src = file;
            document.body.appendChild(audioObj);

            audioObj.play();
        },
        play: function () {
            if (audioObj) audioObj.play();
        },
        pause: function () {
            if (audioObj) audioObj.pause();
        }
    };
    var contentOpt =
    {
        show: function () {
            //æ˜¾ç¤ºä»‹ç»�é—®é¢˜
            if (ex.focusTag.content && ex.focusTag.srcImg.complete) {
                contentView.innerHTML = ex.focusTag.content;
                contentView.style.display = "block";

                var l, t, posi = tagPosi(), iw = ex.focusTag.srcImg.width;
                if (ex.focusTag.stuffW) {
                    var sw = ex.focusTag.stuffW ? ex.focusTag.stuffW : 0;
                    l = (posi.x + iw + 20 - sw);
                    t = posi.y + 10;
                }
                else {
                    t = posi.y + 10;
                    l = posi.x + posi.w - (88 * posi.w / iw) + 20;
                }
                //                addViewInfo({ name: "l", value: l });
                //                addViewInfo({ name: "t", value: t });
                contentView.style.left = l + "px";
                contentView.style.top = t + "px";
            }
        },
        hide: function () {
            contentView.style.display = "none";
        }
    };

    var destroy = function () {
        //è§†é¢‘å¤„ç�†
        var child = videoContainer.firstChild;
        if (child) {
            if (videoObj == child) {
                videoObj.pause();
                videoContainer.removeChild(child);
                videoObj = null;
				if (ex.scene.audio)
                audioOpt.load(ex.scene.audio);
            }
            else
                videoContainer.removeChild(child);
        }
        videoContainer.style.display = "none";

        //éŸ³é¢‘
        if (audioObj.src.indexOf(ex.scene.audio) == -1 && ex.scene.audio) {
            audioOpt.load(ex.scene.audio);
        }

        //ä»‹ç»�æ–‡å­—
        contentOpt.hide();

        stopDraw = false;
    }

    this.stop = function () {
        if (videoObj)
            videoOpt.pause();
        else
            audioOpt.pause();

        stopDraw = true;
    }

    this.start = function () {
        if (videoObj)
            videoOpt.play();
        else
            audioOpt.play();

        stopDraw = false;
    }

    this.clear = function () {
        audioOpt.pause();
        videoContainer.style.display = "none";
        contentOpt.hide();
        this.focusTag = null;
        this.tags = null;
        this.scene = null;
        stopDraw = false;
    }

    var tagPosi = function () {
        var x = ex.focusTag.p0.xp;
        var w = ex.focusTag.p1.xp - x;
        var y, h;
        // max size
        if (ex.focusTag.p0.yp < ex.focusTag.p1.yp) {
            y = ex.focusTag.p0.yp;
            h = ex.focusTag.p3.yp - y;
        } else {
            y = ex.focusTag.p1.yp;
            h = ex.focusTag.p2.yp - y;
        }
        return {
            x: Math.round(x),
            y: Math.round(y),
            w: Math.round(w),
            h: Math.round(h)
        };
    }
    /******************* tool func  **********************/

    ui.onDialogOpen = this.stop;
    ui.onDialogClose = this.start;
    ui.onSoundOn = audioOpt.play;
    ui.onSoundOff = audioOpt.pause;
}

var stopDraw = false;

var viewInfos = [];
function addViewInfo(json) {
    var idx = -1;
    for (var n in viewInfos) {
        if (viewInfos[n].name == json.name)
            idx = n;
    }

    if (idx != -1)
        viewInfos[idx].value = json.value;
    else
        viewInfos.push(json);

    var htmInfo = "<table style='color:#000' border=1><tr>";
    for (var i = 0; i < viewInfos.length; i++) {
        htmInfo += "<td width=50>" + viewInfos[i].name + "</td>";
        htmInfo += "<td width=100>" + viewInfos[i].value + "</td>";

        if (i > 0 && i % 5 == 0)
            htmInfo += "</tr><tr>";
    }

    htmInfo += "</tr></table>";
    document.getElementById("testInfo").innerHTML = htmInfo;
}