// =================================================================================
// Author: Berthold Daum (www.photozora.org)
// =================================================================================
function getUrlParam(url, param) {
	var re = new RegExp("(\\\?|&)" + param + "=([^&]+)(&|$)", "i");
	var m = url.toString().match(re);
	if (m)
		return m[2];
	return '';
}
		
var libs = [];
for (i=0; i<room_ids.length; i++) {
	libs.push('../res/room_'+room_ids[i]+'/geometry.js');
}
libs.push('../scripts/jquery-1.7.2.min.js');
libs.push('../scripts/ui.js');
libs.push('../scripts/m3D.js');
libs.push('../scripts/m3D_Extend.js');
libs.push('../scripts/ieVersionCheck.js');
libs.push(function() {
        var checkInfo = ieVersionCheck();
        if (checkInfo != "") {
			alert(checkInfo);
 	    } else {
	        setTimeout(function () {
	            var rmid = getUrlParam(document.location, "rid");
	            if (rmid != "")
	                m3D.init(rmid);
	            else {
	                m3D.init(start);
					var data = eval('geometry_'+start);
					var a = data.params.audio;
					if (a) 
						document.getElementById("audio").innerHTML='<audio preload="auto" loop="" src="'+a+'"></audio>';
					var logoUrl = data.params.logoUrl;
					if (logoUrl) {
						var logoDiv = document.getElementById("logoDiv");
						if (logoDiv)
							logoDiv.href = logoUrl;
					}
				}
	        }, 500);
			if (room_ids.length > 1) {
				document.getElementById("rooms").innerHTML='<a href="#" id="aToOtherRoom"><img src="../images/other_room.png"></a>';
				var t = "";
				for (i=0; i<room_ids.length; i++) {
					var roomId = room_ids[i];
					try {
						var scene = eval('geometry_'+roomId).params;
						t += '<div class="item" style="opacity: 0; "><img src="../res/room_'+roomId+'/entry.jpg" alt="image"><div><p>'+scene.name+
										   '<span class="info"><b><a href="room.htm?rid='+roomId+'" class="stay">Enter this room</a></b><span class="infotext">'+scene.info+
										   '</span></span></p></div></div>';
						} catch (e) {};
				} 
				document.getElementById("galleryContent").innerHTML=t+t;
		}

	    }
});
head.js.apply(this,libs);
