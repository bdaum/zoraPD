// =================================================================================
// Author: Berthold Daum (www.photozora.org)
// =================================================================================
﻿function ieVersionCheck() {
    var osInfo = GetOSInfo();
    var _title,_sub
    if (navigator.userAgent.match(/\bMSIE\b/)) 
        return (!document.documentMode || document.documentMode < 9) ?
            "Please upgrade to Internet Expolorer 9 to view this exhibition" : "";
    if (supports_html5()) 
		return "";
	return osInfo.match(/Windows/) ? "Please use an HTML5 enabled browser to view this exhibition. For best results use Internet Explorer 9 or above" :
		"Please use an HTML5 enabled browser to view this exhibition";
}

function GetOSInfo() {
    var appVer = navigator.userAgent;  
    var _pf = navigator.platform;
    if (_pf == "Win32" || _pf == "Windows") {
        if (appVer.indexOf("Windows NT 6.0") > -1 || appVer.indexOf("Windows Vista") > -1) 
            return 'Windows Vista';
 		if (appVer.indexOf("Windows NT 6.1") > -1 || appVer.indexOf("Windows 7") > -1) 
            return 'Windows 7';
        try {
            var _winName = Array('2000', 'XP', '2003');
            var _ntNum = appVer.match(/Windows NT 5.\d/i).toString();
            return 'Windows ' + _winName[_ntNum.replace(/Windows NT 5.(\d)/i, "$1")];
        } catch (e) { return 'Windows'; }
    } 
	if (_pf == "Mac68K" || _pf == "MacPPC" || _pf == "Macintosh") 
        return "Mac";
	if (_pf == "X11") 
        return "Unix";
 	if (String(_pf).indexOf("Linux") > -1) 
        return "Linux";
    return "Unknown";
}

function supports_html5() {
    return !!document.createElement('video').canPlayType;
}
