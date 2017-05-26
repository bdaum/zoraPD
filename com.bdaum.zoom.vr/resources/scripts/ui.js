// =================================================================================
// Base script: National Museum of China (http://www.visitnmc.com/)
// Adaptations for ZoRa exhibitions: Berthold Daum (www.photozora.org)
// =================================================================================
var ui = {
    openDialogCount: 0,
    onDialogOpen: function () {
    },
    onDialogClose: function () {
    },
    onChangeRoom: function (id) {
        m3D.changeRoom("scripts/geometry" + id + ".js");
    },
    closeDialogFromCloseButton: function (c) {
        ui.openDialogCount--;
        if (ui.openDialogCount <= 0) $(".overlay").fadeOut();
        $(c.parentNode).fadeOut();

        if (ui.openDialogCount <= 0) {
            ui.onDialogClose();
            //alert(ui.onDialogClose);
        }

    },

    closeDialog: function (id) {
        ui.openDialogCount--;
        if (ui.openDialogCount <= 0) $(".overlay").fadeOut();
        $("#" + id).fadeOut();

        if (ui.openDialogCount <= 0) {
            ui.onDialogClose();
            //alert(ui.onDialogClose);
        }
    },

    openDialog: function (id) {
        ui.openDialogCount++;
        $(".overlay").fadeIn();
        $("#" + id).fadeIn();

        ui.onDialogOpen();
        //alert(ui.onDialogOpen);
    },
    openHowToUse: function () {
        ui.openDialog("howToUse");
        return false;
    },
   openMessage: function (ht) {
        $("#toMessage").html(ht);
        ui.openDialog("toMessage");
        return false;
    },
    openToOtherRoom: function () {
        ui.openDialog("toOtherRoom");

        ui.updateRoomStayButtons();
        ui.onDialogOpen();
        return false;
    },
    updateRoomStayButtons: function () {
        //...
        for (var i = 0; i < roomsNav.items.length; i++) {
            var item = roomsNav.items[i];

            var stayButton = $(".stay", item);



            if (stayButton.length == 0) continue;

            var idstr = stayButton[0].href;
            idstr = parseInt(idstr.substr(idstr.indexOf("#") + 1));
            //idstr = idstr.substr(1);

            //document.title = "stayButton : " + id;

            if (idstr == roomsNav.currentId) {

                roomsNav.current = i;

                stayButton.html("Stay in this room");
            }
            else {
                stayButton.html("Enter this room");
            }
        }
    },
    soundStatusIsOn: true,
    soundOff: function () {
        ui.soundStatusIsOn = false;
        $("#aSoundSwitch").html("Sound on");
        //alert("onSoundOff");

        ui.onSoundOff();
    },
    soundOn: function () {
        ui.soundStatusIsOn = true;
        $("#aSoundSwitch").html("Sound off");
        //alert("onSoundOn");
        ui.onSoundOn();
        //onSoundOn();
    },
    soundTurn: function () {
        if (ui.soundStatusIsOn) ui.soundOff();
        else ui.soundOn();
    },
    onSoundOn: function () {
    },
    onSoundOff: function () {
    },
	vertRotStatusIsOn: true,
    vertRotOff: function () {
        ui.vertRotStatusIsOn = false;
        $("#aVertRotSwitch").html("Vertical navigation on");
        ui.onVertRotOff();
    },
     vertRotOn: function () {
        ui.vertRotStatusIsOn = true;
        $("#aVertRotSwitch").html("Vertical navigation off");
        ui.onVertRotOn();
    },
    vertRotTurn: function () {
        if (ui.vertRotStatusIsOn) ui.vertRotOff();
        else ui.vertRotOn();
    },
    onVertRotOn: function () {
		m3D.setVerticalRotation(true);
    },
    onVertRotOff: function () {
		m3D.setVerticalRotation(false);
    },
    lightKey: function (target) {
        $("." + target).addClass("highlight");
        setTimeout("ui.lightKeyHandler(\"" + target + "\");", 200);

    },
    lightKeyHandler: function (target) {
        $("." + target).removeClass("highlight");
    }
};

var roomsNav = {
    handler: 0,
    frames: 0,
    items: [],
    current: 0,
    currentId: 1,
    spacing: 20,
    mainWidth: 240,
    mainheight: 240,
    otherWidth: 143,
    otherHeight: 143,
    hideWidth: 72,
    hideHeight: 72,
    init: function () {
        roomsNav.items = $(".gallery .item");

        roomsNav.items.click(roomsNav.itemOnClick);
        roomsNav.items.mouseover(roomsNav.itemMouseOver);
        roomsNav.items.mouseleave(roomsNav.itemMouseOut);

        for (var i = 0; i < roomsNav.items.length; i++) {
            roomsNav.items[i].index = i;
            roomsNav.items[i].alpha = 0;
        }

        roomsNav.items.css("opacity", "0");

        roomsNav.handler = setInterval(roomsNav.update, 10);
    },
    update: function () {
        if (ui.openDialogCount <= 0) return;
        roomsNav.frames++;

        for (var i = 0; i < roomsNav.items.length; i++) {
            var item = roomsNav.items[i];

            roomsNav.updateItem(item, i);
        }

    },

    checkOffset: function (offset) {

        //document.title ="checkOffset "+ offset;

        //if (offset < 1) offset = 0;

        return offset;
    },

    updateItem: function (item, index) {

        var pl1 = roomsNav.current - 1;
        var pl2 = roomsNav.current - 2;
        var pr1 = roomsNav.current + 1;
        var pr2 = roomsNav.current + 2;

        if (pl1 < 0) pl1 += roomsNav.items.length;
        if (pl2 < 0) pl2 += roomsNav.items.length;
        if (pr1 < 0) pr1 += roomsNav.items.length;
        if (pr2 < 0) pr2 += roomsNav.items.length;

        if (pl1 > roomsNav.items.length - 1) pl1 -= roomsNav.items.length;
        if (pl2 > roomsNav.items.length - 1) pl2 -= roomsNav.items.length;
        if (pr1 > roomsNav.items.length - 1) pr1 -= roomsNav.items.length;
        if (pr2 > roomsNav.items.length - 1) pr2 -= roomsNav.items.length;

        //document.title = pl2 + "," + pl1 + "," + roomsNav.current + "," + pr1 + "," + pr2;

        var ti = index; // -roomsNav.current;
        var ol = roomsNav.getValue(item.style.left);
        var ot = roomsNav.getValue(item.style.top);
        var ow = item.clientWidth;
        var oh = item.clientHeight;
        var fw = window.innerWidth;
        var fh = roomsNav.mainheight;
        var speed = 0.2;

        var newAlpha = item.alpha;


        if (ti == roomsNav.current) {
            //document.title = ow + "," + oh + "," + ol + "," + ot;
            //当前
            ow += roomsNav.checkOffset((roomsNav.mainWidth - ow) * speed);
            oh += roomsNav.checkOffset((roomsNav.mainheight - oh) * speed);
            ol += roomsNav.checkOffset((((fw - roomsNav.mainWidth) / 2) - ol) * speed);
            ot += roomsNav.checkOffset((0 - ot) * speed);

            item.style.left = ol + "px";
            item.style.top = ot + "px";
            item.style.width = ow + "px";
            item.style.height = oh + "px";

            newAlpha += (1 - newAlpha) * speed;



        }
        else if (ti == pr1) {
            //右边
            ow += (roomsNav.otherWidth - ow) * speed;
            oh += (roomsNav.otherHeight - oh) * speed;
            ot += (((fh - oh) / 2) - ot) * speed;
            ol += (((fw / 2) + (roomsNav.mainWidth / 2) + roomsNav.spacing) - ol) * speed;

            item.style.width = ow + "px";
            item.style.height = oh + "px";
            item.style.top = ot + "px";
            item.style.left = ol + "px";

            newAlpha += (0.7 - newAlpha) * speed;
        }
        else if (ti == pr2) {
            //右边消失
            ow += (roomsNav.hideWidth - ow) * speed;
            oh += (roomsNav.hideHeight - oh) * speed;
            ot += (((fh - roomsNav.hideHeight) / 2) - ot) * speed;
            ol += (((fw / 2) + (roomsNav.mainWidth / 2) + (roomsNav.otherWidth) + roomsNav.spacing * 2) - ol) * speed;

            item.style.width = ow + "px";
            item.style.height = oh + "px";
            item.style.top = ot + "px";
            item.style.left = ol + "px";

            newAlpha += (0 - newAlpha) * speed;
        }
        else if (ti == pl1) {
            //左边
            ow += (roomsNav.otherWidth - ow) * speed;
            oh += (roomsNav.otherHeight - oh) * speed;
            ot += (((fh - oh) / 2) - ot) * speed;
            ol += (((fw / 2) - (roomsNav.mainWidth / 2) - roomsNav.spacing - roomsNav.otherWidth) - ol) * speed;

            item.style.width = ow + "px";
            item.style.height = oh + "px";
            item.style.top = ot + "px";
            item.style.left = ol + "px";

            newAlpha += (0.7 - newAlpha) * speed;
        }
        else if (ti == pl2) {
            //左边消失
            ow += (roomsNav.hideWidth - ow) * speed;
            oh += (roomsNav.hideHeight - oh) * speed;
            ol += (((fw / 2) - (roomsNav.mainWidth / 2) - (roomsNav.otherWidth) - roomsNav.spacing * 2 - roomsNav.hideWidth) - ol) * speed;
            ot += (((fh - roomsNav.hideHeight) / 2) - ot) * speed;

            item.style.width = ow + "px";
            item.style.height = oh + "px";
            item.style.top = ot + "px";
            item.style.left = ol + "px";

            newAlpha += (0 - newAlpha) * speed;
        }
        else {
            //无需显示的
            ow = roomsNav.hideWidth;
            oh = roomsNav.hideHeight;
            ol = -(ow + roomsNav.spacing);
            ot = ((fh - roomsNav.hideHeight) / 2);

            item.style.width = ow + "px";
            item.style.height = oh + "px";
            item.style.top = ot + "px";
            item.style.left = ol + "px";

            newAlpha += (0 - newAlpha) * speed;
        }

        item.alpha = newAlpha;
        $(item).css("opacity", newAlpha);
        if (item.alpha < 0.5) {
            $(item).css("cursor", "default");
        }
        else {
            $(item).css("cursor", "pointer");
        }
    },

    itemMouseOver: function () {
        if (this.index != roomsNav.current) return;
        if (this.alpha < 0.5) return;
        $("SPAN", this).animate({
            height: "130"
        }, 100);
    },

    itemMouseOut: function () {
        if (this.alpha < 0.5) return;
        $("SPAN", this).animate({
            height: "0"
        }, 200);
    },

    itemOnClick: function () {
        if (this.alpha < 0.5) return;

        if (this.index == roomsNav.current) {
            //点击中间的
        }
        else {
            //点击其它的
            roomsNav.current = this.index;
        }
    },

    scroll: function (offset) {
        var i = roomsNav.current + offset;
        if (i < 0) i += roomsNav.items.length;
        if (i > roomsNav.items.length - 1) i -= roomsNav.items.length;

        roomsNav.current = i;
    },

    getValue: function (s) {
        if (s == "" || s == null || typeof (s) == "undefined") return 0;
        var ss = new String(s);
        ss.toLowerCase();
        s = ss.replace("px", "");
        return parseInt(s);
    },

    getCurrentRoomId: function () {
        return 1;
    }
};


$(document).ready(function () {
    $(".overlay").css("opacity", "0.8");
    //$(".gallery .item DIV").css("opacity", "0.5");
    $(".dialog A.close").click(function () {
        ui.closeDialogFromCloseButton(this);
        return false;
    });

    roomsNav.init();

    $(".scrollLeft").click(function () { roomsNav.scroll(-1); });
    $(".scrollRight").click(function () { roomsNav.scroll(1); });
    $("#aHowToUse").click(ui.openHowToUse);
    $("#aInertiaMinus").click(function () { m3D.updateInertia(-1); });
    $("#aInertiaPlus").click(function () { m3D.updateInertia(1); });
    $("#aToOtherRoom").click(ui.openToOtherRoom);
    $("#aSoundSwitch").click(ui.soundTurn);
    $("#aVertRotSwitch").click(ui.vertRotTurn);

    $(".howToUseOK").click(function () {
        ui.closeDialog("howToUse");
        return false;
    });

    $(".stay").click(function () {

        var roomId = this.href;
        roomId = parseInt(roomId.substr(roomId.indexOf("#") + 1));
        ui.closeDialog("toOtherRoom");

        if (roomsNav.currentId == roomId) return;

        roomsNav.currentId = roomId;
        ui.updateRoomStayButtons();
        ui.onChangeRoom(roomId);
        return false;
    });

    //window.document.body.onkeydown = function () {
    //ui.lightKey("keyArrowUp");
    //ui.lightKey("keyArrowDown");
    //ui.lightKey("keyArrowLeft");
    //ui.lightKey("keyArrowRight");
    //};

    //	$("#screen").click(function () {
    //		alert("!");
    //	});
});