/*
 * This file is part of the ZoRa project: http://www.photozora.org.
 *
 * ZoRa is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ZoRa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZoRa; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * (c) 2009 Berthold Daum  
 */
package com.bdaum.zoom.program;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferenceFilter;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.custom.BusyIndicator;
import org.osgi.service.prefs.BackingStoreException;

import com.bdaum.zoom.batch.internal.BatchActivator;
import com.bdaum.zoom.batch.internal.StreamCapture;
import com.bdaum.zoom.common.CommonUtilities;
import com.bdaum.zoom.image.ImageConstants;

public class BatchUtilities {

	private static Map<String, Character> entityMap = new HashMap<String, Character>(380);
	private static Map<Character, String> codeMap = new HashMap<Character, String>(380);

	static {
		Object[][] entityCodes = { { new String("&Aacute;"), new Character((char) 193) }, //$NON-NLS-1$
				{ new String("&aacute;"), new Character((char) 225) }, //$NON-NLS-1$
				{ new String("&Acirc;"), new Character((char) 194) }, //$NON-NLS-1$
				{ new String("&acirc;"), new Character((char) 226) }, //$NON-NLS-1$
				{ new String("&acute;"), new Character((char) 180) }, //$NON-NLS-1$
				{ new String("&AElig;"), new Character((char) 198) }, //$NON-NLS-1$
				{ new String("&aelig;"), new Character((char) 230) }, //$NON-NLS-1$
				{ new String("&Agrave;"), new Character((char) 192) }, //$NON-NLS-1$
				{ new String("&agrave;"), new Character((char) 224) }, //$NON-NLS-1$
				{ new String("&alefsym;"), new Character((char) 8501) }, //$NON-NLS-1$
				{ new String("&Alpha;"), new Character((char) 913) }, //$NON-NLS-1$
				{ new String("&alpha;"), new Character((char) 945) }, //$NON-NLS-1$
				{ new String("&amp;"), new Character('&') }, //$NON-NLS-1$
				{ new String("&and;"), new Character((char) 8743) }, //$NON-NLS-1$
				{ new String("&ang;"), new Character((char) 8736) }, //$NON-NLS-1$
				{ new String("&apos;"), new Character('\'') }, //$NON-NLS-1$
				{ new String("&Aring;"), new Character((char) 197) }, //$NON-NLS-1$
				{ new String("&aring;"), new Character((char) 229) }, //$NON-NLS-1$
				{ new String("&asymp;"), new Character((char) 8776) }, //$NON-NLS-1$
				{ new String("&Atilde;"), new Character((char) 195) }, //$NON-NLS-1$
				{ new String("&atilde;"), new Character((char) 227) }, //$NON-NLS-1$
				{ new String("&Auml;"), new Character((char) 196) }, //$NON-NLS-1$
				{ new String("&auml;"), new Character((char) 228) }, //$NON-NLS-1$
				{ new String("&bdquo;"), new Character((char) 8222) }, //$NON-NLS-1$
				{ new String("&Beta;"), new Character((char) 914) }, //$NON-NLS-1$
				{ new String("&beta;"), new Character((char) 946) }, //$NON-NLS-1$
				{ new String("&brvbar;"), new Character((char) 166) }, //$NON-NLS-1$
				{ new String("&bull;"), new Character((char) 8226) }, //$NON-NLS-1$
				{ new String("&cap;"), new Character((char) 8745) }, //$NON-NLS-1$
				{ new String("&Ccedil;"), new Character((char) 199) }, //$NON-NLS-1$
				{ new String("&ccedil;"), new Character((char) 231) }, //$NON-NLS-1$
				{ new String("&cedil;"), new Character((char) 184) }, //$NON-NLS-1$
				{ new String("&cent;"), new Character((char) 162) }, //$NON-NLS-1$
				{ new String("&Chi;"), new Character((char) 935) }, //$NON-NLS-1$
				{ new String("&chi;"), new Character((char) 967) }, //$NON-NLS-1$
				{ new String("&circ;"), new Character((char) 710) }, //$NON-NLS-1$
				{ new String("&clubs;"), new Character((char) 9827) }, //$NON-NLS-1$
				{ new String("&cong;"), new Character((char) 8773) }, //$NON-NLS-1$
				{ new String("&copy;"), new Character((char) 169) }, //$NON-NLS-1$
				{ new String("&crarr;"), new Character((char) 8629) }, //$NON-NLS-1$
				{ new String("&cup;"), new Character((char) 8746) }, //$NON-NLS-1$
				{ new String("&curren;"), new Character((char) 164) }, //$NON-NLS-1$
				{ new String("&dagger;"), new Character((char) 8224) }, //$NON-NLS-1$
				{ new String("&Dagger;"), new Character((char) 8225) }, //$NON-NLS-1$
				{ new String("&darr;"), new Character((char) 8595) }, //$NON-NLS-1$
				{ new String("&dArr;"), new Character((char) 8659) }, //$NON-NLS-1$
				{ new String("&deg;"), new Character((char) 176) }, //$NON-NLS-1$
				{ new String("&Delta;"), new Character((char) 916) }, //$NON-NLS-1$
				{ new String("&delta;"), new Character((char) 948) }, //$NON-NLS-1$
				{ new String("&diams;"), new Character((char) 9830) }, //$NON-NLS-1$
				{ new String("&divide;"), new Character((char) 247) }, //$NON-NLS-1$
				{ new String("&Eacute;"), new Character((char) 201) }, //$NON-NLS-1$
				{ new String("&eacute;"), new Character((char) 233) }, //$NON-NLS-1$
				{ new String("&Ecirc;"), new Character((char) 202) }, //$NON-NLS-1$
				{ new String("&ecirc;"), new Character((char) 234) }, //$NON-NLS-1$
				{ new String("&Egrave;"), new Character((char) 200) }, //$NON-NLS-1$
				{ new String("&egrave;"), new Character((char) 232) }, //$NON-NLS-1$
				{ new String("&empty;"), new Character((char) 8709) }, //$NON-NLS-1$
				{ new String("&emsp;"), new Character((char) 8195) }, //$NON-NLS-1$
				{ new String("&ensp;"), new Character((char) 8194) }, //$NON-NLS-1$
				{ new String("&Epsilon;"), new Character((char) 917) }, //$NON-NLS-1$
				{ new String("&epsilon;"), new Character((char) 949) }, //$NON-NLS-1$
				{ new String("&equiv;"), new Character((char) 8801) }, //$NON-NLS-1$
				{ new String("&Eta;"), new Character((char) 919) }, //$NON-NLS-1$
				{ new String("&eta;"), new Character((char) 951) }, //$NON-NLS-1$
				{ new String("&ETH;"), new Character((char) 208) }, //$NON-NLS-1$
				{ new String("&eth;"), new Character((char) 240) }, //$NON-NLS-1$
				{ new String("&Euml;"), new Character((char) 203) }, //$NON-NLS-1$
				{ new String("&euml;"), new Character((char) 235) }, //$NON-NLS-1$
				{ new String("&euro;"), new Character((char) 8364) }, //$NON-NLS-1$
				{ new String("&exist;"), new Character((char) 8707) }, //$NON-NLS-1$
				{ new String("&fnof;"), new Character((char) 402) }, //$NON-NLS-1$
				{ new String("&forall;"), new Character((char) 8704) }, //$NON-NLS-1$
				{ new String("&frac12;"), new Character((char) 189) }, //$NON-NLS-1$
				{ new String("&frac14;"), new Character((char) 188) }, //$NON-NLS-1$
				{ new String("&frac34;"), new Character((char) 190) }, //$NON-NLS-1$
				{ new String("&frasl;"), new Character((char) 8260) }, //$NON-NLS-1$
				{ new String("&Gamma;"), new Character((char) 915) }, //$NON-NLS-1$
				{ new String("&gamma;"), new Character((char) 947) }, //$NON-NLS-1$
				{ new String("&ge;"), new Character((char) 8805) }, //$NON-NLS-1$
				{ new String("&gt;"), new Character('>') }, //$NON-NLS-1$
				{ new String("&harr;"), new Character((char) 8596) }, //$NON-NLS-1$
				{ new String("&hArr;"), new Character((char) 8660) }, //$NON-NLS-1$
				{ new String("&hearts;"), new Character((char) 9829) }, //$NON-NLS-1$
				{ new String("&hellip;"), new Character((char) 8230) }, //$NON-NLS-1$
				{ new String("&Iacute;"), new Character((char) 205) }, //$NON-NLS-1$
				{ new String("&iacute;"), new Character((char) 237) }, //$NON-NLS-1$
				{ new String("&Icirc;"), new Character((char) 206) }, //$NON-NLS-1$
				{ new String("&icirc;"), new Character((char) 238) }, //$NON-NLS-1$
				{ new String("&iexcl;"), new Character((char) 161) }, //$NON-NLS-1$
				{ new String("&Igrave;"), new Character((char) 204) }, //$NON-NLS-1$
				{ new String("&igrave;"), new Character((char) 236) }, //$NON-NLS-1$
				{ new String("&image;"), new Character((char) 8465) }, //$NON-NLS-1$
				{ new String("&infin;"), new Character((char) 8734) }, //$NON-NLS-1$
				{ new String("&int;"), new Character((char) 8747) }, //$NON-NLS-1$
				{ new String("&Iota;"), new Character((char) 921) }, //$NON-NLS-1$
				{ new String("&iota;"), new Character((char) 953) }, //$NON-NLS-1$
				{ new String("&iquest;"), new Character((char) 191) }, //$NON-NLS-1$
				{ new String("&isin;"), new Character((char) 8712) }, //$NON-NLS-1$
				{ new String("&Iuml;"), new Character((char) 207) }, //$NON-NLS-1$
				{ new String("&iuml;"), new Character((char) 239) }, //$NON-NLS-1$
				{ new String("&Kappa;"), new Character((char) 922) }, //$NON-NLS-1$
				{ new String("&kappa;"), new Character((char) 954) }, //$NON-NLS-1$
				{ new String("&Lambda;"), new Character((char) 923) }, //$NON-NLS-1$
				{ new String("&lambda;"), new Character((char) 955) }, //$NON-NLS-1$
				{ new String("&lang;"), new Character((char) 9001) }, //$NON-NLS-1$
				{ new String("&laquo;"), new Character((char) 171) }, //$NON-NLS-1$
				{ new String("&larr;"), new Character((char) 8592) }, //$NON-NLS-1$
				{ new String("&lArr;"), new Character((char) 8656) }, //$NON-NLS-1$
				{ new String("&lceil;"), new Character((char) 8968) }, //$NON-NLS-1$
				{ new String("&ldquo;"), new Character((char) 8220) }, //$NON-NLS-1$
				{ new String("&le;"), new Character((char) 8804) }, //$NON-NLS-1$
				{ new String("&lfloor;"), new Character((char) 8970) }, //$NON-NLS-1$
				{ new String("&lowast;"), new Character((char) 8727) }, //$NON-NLS-1$
				{ new String("&loz;"), new Character((char) 9674) }, //$NON-NLS-1$
				{ new String("&lrm;"), new Character((char) 8206) }, //$NON-NLS-1$
				{ new String("&lsaquo;"), new Character((char) 8249) }, //$NON-NLS-1$
				{ new String("&lsquo;"), new Character((char) 8216) }, //$NON-NLS-1$
				{ new String("&lt;"), new Character('<') }, //$NON-NLS-1$
				{ new String("&macr;"), new Character((char) 175) }, //$NON-NLS-1$
				{ new String("&mdash;"), new Character((char) 8212) }, //$NON-NLS-1$
				{ new String("&micro;"), new Character((char) 181) }, //$NON-NLS-1$
				{ new String("&middot;"), new Character((char) 183) }, //$NON-NLS-1$
				{ new String("&minus;"), new Character((char) 8722) }, //$NON-NLS-1$
				{ new String("&Mu;"), new Character((char) 924) }, //$NON-NLS-1$
				{ new String("&mu;"), new Character((char) 956) }, //$NON-NLS-1$
				{ new String("&nabla;"), new Character((char) 8711) }, //$NON-NLS-1$
				{ new String("&nbsp;"), new Character((char) 160) }, //$NON-NLS-1$
				{ new String("&ndash;"), new Character((char) 8211) }, //$NON-NLS-1$
				{ new String("&ne;"), new Character((char) 8800) }, //$NON-NLS-1$
				{ new String("&ni;"), new Character((char) 8715) }, //$NON-NLS-1$
				{ new String("&not;"), new Character((char) 172) }, //$NON-NLS-1$
				{ new String("&notin;"), new Character((char) 8713) }, //$NON-NLS-1$
				{ new String("&nsub;"), new Character((char) 8836) }, //$NON-NLS-1$
				{ new String("&Ntilde;"), new Character((char) 209) }, //$NON-NLS-1$
				{ new String("&ntilde;"), new Character((char) 241) }, //$NON-NLS-1$
				{ new String("&Nu;"), new Character((char) 925) }, //$NON-NLS-1$
				{ new String("&nu;"), new Character((char) 957) }, //$NON-NLS-1$
				{ new String("&Oacute;"), new Character((char) 211) }, //$NON-NLS-1$
				{ new String("&oacute;"), new Character((char) 243) }, //$NON-NLS-1$
				{ new String("&Ocirc;"), new Character((char) 212) }, //$NON-NLS-1$
				{ new String("&ocirc;"), new Character((char) 244) }, //$NON-NLS-1$
				{ new String("&OElig;"), new Character((char) 338) }, //$NON-NLS-1$
				{ new String("&oelig;"), new Character((char) 339) }, //$NON-NLS-1$
				{ new String("&Ograve;"), new Character((char) 210) }, //$NON-NLS-1$
				{ new String("&ograve;"), new Character((char) 242) }, //$NON-NLS-1$
				{ new String("&oline;"), new Character((char) 8254) }, //$NON-NLS-1$
				{ new String("&Omega;"), new Character((char) 937) }, //$NON-NLS-1$
				{ new String("&omega;"), new Character((char) 969) }, //$NON-NLS-1$
				{ new String("&Omicron;"), new Character((char) 927) }, //$NON-NLS-1$
				{ new String("&omicron;"), new Character((char) 959) }, //$NON-NLS-1$
				{ new String("&oplus;"), new Character((char) 8853) }, //$NON-NLS-1$
				{ new String("&or;"), new Character((char) 8744) }, //$NON-NLS-1$
				{ new String("&ordf;"), new Character((char) 170) }, //$NON-NLS-1$
				{ new String("&ordm;"), new Character((char) 186) }, //$NON-NLS-1$
				{ new String("&Oslash;"), new Character((char) 216) }, //$NON-NLS-1$
				{ new String("&oslash;"), new Character((char) 248) }, //$NON-NLS-1$
				{ new String("&Otilde;"), new Character((char) 213) }, //$NON-NLS-1$
				{ new String("&otilde;"), new Character((char) 245) }, //$NON-NLS-1$
				{ new String("&otimes;"), new Character((char) 8855) }, //$NON-NLS-1$
				{ new String("&Ouml;"), new Character((char) 214) }, //$NON-NLS-1$
				{ new String("&ouml;"), new Character((char) 246) }, //$NON-NLS-1$
				{ new String("&para;"), new Character((char) 182) }, //$NON-NLS-1$
				{ new String("&part;"), new Character((char) 8706) }, //$NON-NLS-1$
				{ new String("&permil;"), new Character((char) 8240) }, //$NON-NLS-1$
				{ new String("&perp;"), new Character((char) 8869) }, //$NON-NLS-1$
				{ new String("&Phi;"), new Character((char) 934) }, //$NON-NLS-1$
				{ new String("&phi;"), new Character((char) 966) }, //$NON-NLS-1$
				{ new String("&Pi;"), new Character((char) 928) }, //$NON-NLS-1$
				{ new String("&pi;"), new Character((char) 960) }, //$NON-NLS-1$
				{ new String("&piv;"), new Character((char) 982) }, //$NON-NLS-1$
				{ new String("&plusmn;"), new Character((char) 177) }, //$NON-NLS-1$
				{ new String("&pound;"), new Character((char) 163) }, //$NON-NLS-1$
				{ new String("&prime;"), new Character((char) 8242) }, //$NON-NLS-1$
				{ new String("&Prime;"), new Character((char) 8243) }, //$NON-NLS-1$
				{ new String("&prod;"), new Character((char) 8719) }, //$NON-NLS-1$
				{ new String("&prop;"), new Character((char) 8733) }, //$NON-NLS-1$
				{ new String("&Psi;"), new Character((char) 936) }, //$NON-NLS-1$
				{ new String("&psi;"), new Character((char) 968) }, //$NON-NLS-1$
				{ new String("&quot;"), new Character('"') }, //$NON-NLS-1$
				{ new String("&radic;"), new Character((char) 8730) }, //$NON-NLS-1$
				{ new String("&rang;"), new Character((char) 9002) }, //$NON-NLS-1$
				{ new String("&raquo;"), new Character((char) 187) }, //$NON-NLS-1$
				{ new String("&rarr;"), new Character((char) 8594) }, //$NON-NLS-1$
				{ new String("&rArr;"), new Character((char) 8658) }, //$NON-NLS-1$
				{ new String("&rceil;"), new Character((char) 8969) }, //$NON-NLS-1$
				{ new String("&rdquo;"), new Character((char) 8221) }, //$NON-NLS-1$
				{ new String("&real;"), new Character((char) 8476) }, //$NON-NLS-1$
				{ new String("&reg;"), new Character((char) 174) }, //$NON-NLS-1$
				{ new String("&rfloor;"), new Character((char) 8971) }, //$NON-NLS-1$
				{ new String("&Rho;"), new Character((char) 929) }, //$NON-NLS-1$
				{ new String("&rho;"), new Character((char) 961) }, //$NON-NLS-1$
				{ new String("&rlm;"), new Character((char) 8207) }, //$NON-NLS-1$
				{ new String("&rsaquo;"), new Character((char) 8250) }, //$NON-NLS-1$
				{ new String("&rsquo;"), new Character((char) 8217) }, //$NON-NLS-1$
				{ new String("&sbquo;"), new Character((char) 8218) }, //$NON-NLS-1$
				{ new String("&Scaron;"), new Character((char) 352) }, //$NON-NLS-1$
				{ new String("&scaron;"), new Character((char) 353) }, //$NON-NLS-1$
				{ new String("&sdot;"), new Character((char) 8901) }, //$NON-NLS-1$
				{ new String("&sect;"), new Character((char) 167) }, //$NON-NLS-1$
				{ new String("&shy;"), new Character((char) 173) }, //$NON-NLS-1$
				{ new String("&Sigma;"), new Character((char) 931) }, //$NON-NLS-1$
				{ new String("&sigma;"), new Character((char) 963) }, //$NON-NLS-1$
				{ new String("&sigmaf;"), new Character((char) 962) }, //$NON-NLS-1$
				{ new String("&sim;"), new Character((char) 8764) }, //$NON-NLS-1$
				{ new String("&spades;"), new Character((char) 9824) }, //$NON-NLS-1$
				{ new String("&sub;"), new Character((char) 8834) }, //$NON-NLS-1$
				{ new String("&sube;"), new Character((char) 8838) }, //$NON-NLS-1$
				{ new String("&sum;"), new Character((char) 8721) }, //$NON-NLS-1$
				{ new String("&sup1;"), new Character((char) 185) }, //$NON-NLS-1$
				{ new String("&sup2;"), new Character((char) 178) }, //$NON-NLS-1$
				{ new String("&sup3;"), new Character((char) 179) }, //$NON-NLS-1$
				{ new String("&sup;"), new Character((char) 8835) }, //$NON-NLS-1$
				{ new String("&supe;"), new Character((char) 8839) }, //$NON-NLS-1$
				{ new String("&szlig;"), new Character((char) 223) }, //$NON-NLS-1$
				{ new String("&Tau;"), new Character((char) 932) }, //$NON-NLS-1$
				{ new String("&tau;"), new Character((char) 964) }, //$NON-NLS-1$
				{ new String("&there4;"), new Character((char) 8756) }, //$NON-NLS-1$
				{ new String("&Theta;"), new Character((char) 920) }, //$NON-NLS-1$
				{ new String("&theta;"), new Character((char) 952) }, //$NON-NLS-1$
				{ new String("&thetasym;"), new Character((char) 977) }, //$NON-NLS-1$
				{ new String("&thinsp;"), new Character((char) 8201) }, //$NON-NLS-1$
				{ new String("&THORN;"), new Character((char) 222) }, //$NON-NLS-1$
				{ new String("&thorn;"), new Character((char) 254) }, //$NON-NLS-1$
				{ new String("&tilde;"), new Character((char) 732) }, //$NON-NLS-1$
				{ new String("&times;"), new Character((char) 215) }, //$NON-NLS-1$
				{ new String("&trade;"), new Character((char) 8482) }, //$NON-NLS-1$
				{ new String("&Uacute;"), new Character((char) 218) }, //$NON-NLS-1$
				{ new String("&uacute;"), new Character((char) 250) }, //$NON-NLS-1$
				{ new String("&uarr;"), new Character((char) 8593) }, //$NON-NLS-1$
				{ new String("&uArr;"), new Character((char) 8657) }, //$NON-NLS-1$
				{ new String("&Ucirc;"), new Character((char) 219) }, //$NON-NLS-1$
				{ new String("&ucirc;"), new Character((char) 251) }, //$NON-NLS-1$
				{ new String("&Ugrave;"), new Character((char) 217) }, //$NON-NLS-1$
				{ new String("&ugrave;"), new Character((char) 249) }, //$NON-NLS-1$
				{ new String("&uml;"), new Character((char) 168) }, //$NON-NLS-1$
				{ new String("&upsih;"), new Character((char) 978) }, //$NON-NLS-1$
				{ new String("&Upsilon;"), new Character((char) 933) }, //$NON-NLS-1$
				{ new String("&upsilon;"), new Character((char) 965) }, //$NON-NLS-1$
				{ new String("&Uuml;"), new Character((char) 220) }, //$NON-NLS-1$
				{ new String("&uuml;"), new Character((char) 252) }, //$NON-NLS-1$
				{ new String("&weierp;"), new Character((char) 8472) }, //$NON-NLS-1$
				{ new String("&Xi;"), new Character((char) 926) }, //$NON-NLS-1$
				{ new String("&xi;"), new Character((char) 958) }, //$NON-NLS-1$
				{ new String("&Yacute;"), new Character((char) 221) }, //$NON-NLS-1$
				{ new String("&yacute;"), new Character((char) 253) }, //$NON-NLS-1$
				{ new String("&yen;"), new Character((char) 165) }, //$NON-NLS-1$
				{ new String("&yuml;"), new Character((char) 255) }, //$NON-NLS-1$
				{ new String("&Yuml;"), new Character((char) 376) }, //$NON-NLS-1$
				{ new String("&Zeta;"), new Character((char) 918) }, //$NON-NLS-1$
				{ new String("&zeta;"), new Character((char) 950) }, //$NON-NLS-1$
				{ new String("&zwj;"), new Character((char) 8205) }, //$NON-NLS-1$
				{ new String("&zwnj;"), new Character((char) 8204) } }; //$NON-NLS-1$
		for (int i = 0; i < entityCodes.length; i++) {
			codeMap.put((Character) entityCodes[i][1], (String) entityCodes[i][0]);
			entityMap.put((String) entityCodes[i][0], (Character) entityCodes[i][1]);
		}
	}

	/**
	 * Encodes the blanks of a URL
	 *
	 * @param s
	 *            - URL or URL part/
	 * @return the encoded string
	 */
	public static String encodeBlanks(String s) {
		return CommonUtilities.encodeBlanks(s);
	}

	/**
	 * Replaces problematic characters with underscore
	 *
	 * @param s
	 *            input string
	 * @return result string
	 */
	public static String toValidFilename(String s) {
		char[] chars = s.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (!Character.isLetterOrDigit(c) && c != ' ' && c != '.' && c != '_' && c != '-')
				chars[i] = '_';
		}
		return new String(chars);
	}

	/**
	 * Extract the file extension from a path specification
	 *
	 * @param path
	 *            - path (URI syntax only)
	 * @return file extension
	 */
	public static String getTrueFileExtension(String path) {
		int p = path.lastIndexOf('.');
		return (p > path.lastIndexOf('/')) ? path.substring(p + 1) : ""; //$NON-NLS-1$
	}

	/**
	 * Determines the last relevant modification of an image
	 *
	 * @param file
	 *            - input file
	 * @param recipeProvider
	 *            - A raw recipe provider or null
	 * @return - timestamp
	 */
	public static long getImageFileModificationTimestamp(File file) {
		long ts = file.lastModified();
		String uri = file.toURI().toString();
		String extension = getTrueFileExtension(uri);
		String xmpUri = uri;
		if (extension != null)
			xmpUri = uri.substring(0, uri.length() - (extension.length() + 1));
		xmpUri += ".xmp"; //$NON-NLS-1$
		try {
			ts = Math.max(ts, new File(new URI(xmpUri)).lastModified());
		} catch (URISyntaxException e) {
			// should never happen
		}

		if (ImageConstants.isRaw(uri, true)) {
			IRawConverter currentRawConverter = BatchActivator.getDefault().getCurrentRawConverter(false);
			if (currentRawConverter != null)
				return currentRawConverter.getLastRecipeModification(uri, ts, null);
		}
		return ts;
	}

	/**
	 * Moves a file from one location to another. Works across file systems
	 *
	 * @param source
	 *            - source file
	 * @param target
	 *            - target file
	 * @param monitor
	 *            - progress monitor or null
	 * @throws IOException
	 * @throws DiskFullException
	 */
	public static void moveFile(File source, File target, IProgressMonitor monitor)
			throws IOException, DiskFullException {
		target.delete();
		if (source != null) {
			if (source.renameTo(target))
				return;
			copyFile(source, target, monitor);
			source.delete();
		}
	}

	/**
	 * Copies a folder with its contents
	 *
	 * @param in
	 *            - input folder
	 * @param out
	 *            - output destination
	 * @param monitor
	 *            - progress monitor or null
	 * @throws IOException
	 * @throws DiskFullException
	 */
	public synchronized static void copyFolder(File in, File out, IProgressMonitor monitor)
			throws IOException, DiskFullException {
		copyFolder(in, out, true, monitor);
	}

	/**
	 * Copies a folder with its contents
	 *
	 * @param in
	 *            - input folder
	 * @param out
	 *            - output destination
	 * @param overwrite
	 *            - true if destination is to be overwritten
	 * @param monitor
	 *            - progress monitor or null
	 * @throws IOException
	 * @throws DiskFullException
	 */
	public synchronized static void copyFolder(File in, File out, boolean overwrite, IProgressMonitor monitor)
			throws IOException, DiskFullException {
		if (in.isFile())
			copyFile(in, out, overwrite, monitor);
		else {
			out.mkdirs();
			File[] files = in.listFiles();
			if (files == null)
				throw new IOException(NLS.bind(Messages.getString("BatchUtilities.directory_cannot_be_listed"), in)); //$NON-NLS-1$
			for (File inFile : files)
				copyFolder(inFile, new File(out, inFile.getName()), overwrite, monitor);
		}
	}

	/**
	 * Deletes a file or folder with its contents
	 *
	 * @param in
	 *            - input file or folder
	 * @return true if file or folder was deleted
	 */
	public synchronized static boolean deleteFileOrFolder(File in) {
		if (in.isDirectory() && !deleteFolderContent(in))
			return false;
		return in.delete();
	}

	/**
	 * Deletes the content of the specified folder
	 *
	 * @param in
	 *            - input folder
	 * @return true if all content was deleted
	 */
	public synchronized static boolean deleteFolderContent(File in) {
		File[] files = in.listFiles();
		if (files == null)
			return false;
		for (File inFile : files)
			if (!deleteFileOrFolder(inFile))
				return false;
		return true;
	}

	private static final long MAXTRANSFERSIZE = (64 * 1024 - 32) * 1024L;

	/**
	 * Copies a file
	 *
	 * @param in
	 *            - input file
	 * @param out
	 *            - output destination
	 * @param monitor
	 *            - progress monitor or null
	 * @throws IOException
	 * @throws DiskFullException
	 */
	public synchronized static void copyFile(File in, File out, IProgressMonitor monitor)
			throws IOException, DiskFullException {
		copyFile(in, out, true, monitor);
	}

	/**
	 * Copies a file
	 *
	 * @param in
	 *            - input file
	 * @param out
	 *            - output destination
	 * @param overwrite
	 *            - true if destination is to be overwritten
	 * @param monitor
	 *            - progress monitor or null
	 * @throws IOException
	 * @throws DiskFullException
	 */
	public synchronized static void copyFile(File in, File out, boolean overwrite, IProgressMonitor monitor)
			throws IOException, DiskFullException {
		if (!overwrite && out.exists())
			return;
		long size = in.length();
		try (FileInputStream instream = new FileInputStream(in)) {
			try (FileChannel sourceChannel = instream.getChannel()) {
				try (FileOutputStream outstream = new FileOutputStream(out)) {
					try (FileChannel destinationChannel = outstream.getChannel()) {
						if (size < MAXTRANSFERSIZE)
							sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
						else {
							long position = 0;
							while (position < size && (monitor == null || !monitor.isCanceled()))
								position += sourceChannel.transferTo(position, MAXTRANSFERSIZE, destinationChannel);
						}
					}
				}
			}
		} finally {
			if (size > 0 && out.exists()) {
				long outsize = out.length();
				if (outsize < size) {
					if (!in.exists())
						throw new FileNotFoundException(
								NLS.bind(Messages.getString("BatchUtilities.Fike_not_found_while_copying"), //$NON-NLS-1$
										in.getPath()));
					copyBytes(in, out, monitor);
				}
			}
		}
	}

	private static void copyBytes(File in, File out, IProgressMonitor monitor) throws IOException, DiskFullException {
		FileLock fl = null;
		try (FileInputStream fin = new FileInputStream(in)) {
			try (FileOutputStream fout = new FileOutputStream(out)) {
				try {
					fl = fin.getChannel().tryLock();
					if (fl == null)
						throw new IOException(NLS.bind(Messages.getString("BatchUtilities.File_in_use"), in.getPath())); //$NON-NLS-1$
				} catch (Exception e) {
					// work without lock
				}
				try (BufferedInputStream bin = new BufferedInputStream(fin)) {
					try (BufferedOutputStream bout = new BufferedOutputStream(fout)) {
						try {
							transferStreams(bin, bout, monitor);
						} finally {
							if (fl != null)
								fl.release();
							long size = in.length();
							long outsize = out.length();
							if (outsize < size)
								throw new DiskFullException(NLS.bind(Messages.getString("BatchUtilities.File_n_m"), //$NON-NLS-1$
										out, outsize + " < " + size)); //$NON-NLS-1$
						}
					}
				}
			}
		}
	}

	/**
	 * Transfers bytes from an input stream to an output stream
	 *
	 * @param source
	 *            - input stream
	 * @param destination
	 *            - output stream
	 * @param monitor
	 *            -progress monitor or null
	 * @return number of transmitted bytes
	 * @throws IOException
	 */
	public static final int transferStreams(InputStream source, OutputStream destination, IProgressMonitor monitor)
			throws IOException {
		byte[] buffer = new byte[8192];
		int r = 0;
		while (true) {
			int bytesRead = source.read(buffer);
			if (bytesRead == -1)
				break;
			destination.write(buffer, 0, bytesRead);
			r += bytesRead;
			if (monitor != null && monitor.isCanceled())
				return -1;
		}
		return r;
	}

	private static final String MEDIA = "/media/"; //$NON-NLS-1$
	private static final String VOLUMES = "/Volumes/"; //$NON-NLS-1$

	/**
	 * Ejects removable media
	 *
	 * @param file
	 *            - file stored on the media to be removed
	 * @throws IOException
	 */
	public static void ejectMedia(String file) throws IOException {
		String[] parms = null;
		if (BatchConstants.WIN32) {
			Path path = new Path(file);
			String device = path.getDevice();
			String ejectMedia = BatchActivator.getDefault().locate("/EjectMedia.exe"); //$NON-NLS-1$
			if (ejectMedia != null)
				parms = new String[] { ejectMedia, device, "-w:300" }; //$NON-NLS-1$
		} else if (BatchConstants.OSX) {
			if (file.startsWith(VOLUMES)) {
				int p = file.indexOf('/', VOLUMES.length());
				if (p >= 0)
					file = file.substring(0, p);
				parms = new String[] { "diskutil", "unmount", "force", file }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		} else if (file.startsWith(MEDIA)) {
			int p = file.indexOf('/', MEDIA.length());
			if (p >= 0)
				file = file.substring(0, p);
			parms = new String[] { "pumount", file }; //$NON-NLS-1$
		}
		if (parms != null)
			Runtime.getRuntime().exec(parms);
	}

	/**
	 * Sbows the folder where the specified file is located Both the folder in the
	 * catalog and the host file system are shown If possible the file is selected
	 * in the host folder
	 *
	 * @param file
	 *            - the file to be shown
	 */
	public static void showInFolder(final File file) {
		BusyIndicator.showWhile(null, () -> {
			try {
				if (BatchConstants.WIN32)
					BatchActivator.getDefault().runScript(new String[] { "/select.bat", //$NON-NLS-1$
							file.getAbsolutePath() });
				else
					BatchUtilities.executeCommand(
							new String[] { BatchConstants.OSX ? "open" : "xdg-open", //$NON-NLS-1$ //$NON-NLS-2$
									file.getParentFile().getAbsolutePath() },
							null, Messages.getString("BatchUtilities.run_script"), IStatus.OK, IStatus.ERROR, 0, 1000L, "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (IOException e1) {
				BatchActivator.getDefault().logError(Messages.getString("BatchUtilities.io_error_showFile"), //$NON-NLS-1$
						e1);
			} catch (ExecutionException e) {
				BatchActivator.getDefault().logError(Messages.getString("BatchUtilities.script_execution_failed"), //$NON-NLS-1$
						e);
			}
		});

	}

	/**
	 * Finds DCIM folders in the mounted volumes
	 *
	 * @return DCIM folders
	 */
	public static List<File> findDCIMs() {
		List<File> dcims = new ArrayList<File>(3);
		if (BatchConstants.OSX) {
			File volumes = new File("/Volumes"); //$NON-NLS-1$
			File[] files = volumes.listFiles();
			if (files != null)
				for (File volume : files) {
					File file = new File(volume, "DCIM"); //$NON-NLS-1$
					if (file.isDirectory())
						dcims.add(file);
				}
		} else {
			File[] roots = File.listRoots();
			for (File root : roots) {
				File file = new File(root, "DCIM"); //$NON-NLS-1$
				if (file.isDirectory())
					dcims.add(file);
			}
			if (BatchConstants.LINUX) {
				File media = new File("/media"); //$NON-NLS-1$
				File[] files = media.listFiles();
				if (files != null) {
					roots = files;
					for (File root : roots) {
						File file = new File(root, "DCIM"); //$NON-NLS-1$
						if (file.isDirectory())
							dcims.add(file);
					}
				}
			}
		}
		return dcims;
	}

	/**
	 * Execute an operating system command
	 *
	 * @param parms
	 *            - command parameters
	 * @param workingDir
	 *            - working directory or null
	 * @param label
	 *            - job label
	 * @param logLevel
	 *            - status level (as defined in IStatus) for output data. IStatus.OK
	 *            turns logging off
	 * @param errorLevel
	 *            - status level (as defined in IStatus) for error data. IStatus.OK
	 *            turns logging off
	 * @param errorHandling
	 *            determines when an error message is generated if no error data is
	 *            supplied by the error stream: > 0 returned condition code must
	 *            match this number; -1 returned condition code must be unequal 0; 0
	 *            no error message
	 * @param timeout
	 *            - timeout value in msec for waiting on output. 0 waits forever.
	 * @param charsetName
	 *            - character set used such as "UTF-8"
	 * @return standard output data
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static String executeCommand(String[] parms, File workingDir, String label, int logLevel, int errorLevel,
			int errorHandling, long timeout, String charsetName) throws IOException, ExecutionException {
		StreamCapture inputGrabber = null;
		StreamCapture errorGrabber = null;
		try {
			Process process = Runtime.getRuntime().exec(parms, null, workingDir);
			inputGrabber = new StreamCapture(process.getInputStream(), charsetName, label,
					Messages.getString("BatchUtilities.output"), logLevel); //$NON-NLS-1$
			errorGrabber = new StreamCapture(process.getErrorStream(), charsetName, label,
					Messages.getString("BatchUtilities.errors"), errorLevel); //$NON-NLS-1$
			errorGrabber.start();
			inputGrabber.start();
			try {
				int ret = process.waitFor();
				if (ret == 0) {
					inputGrabber.join(timeout);
					return inputGrabber.getData();
				}
				String errorData = errorGrabber.getData().trim();
				if (errorData.isEmpty() && (ret != 0 && errorHandling < 0 || errorHandling == ret && errorHandling > 0))
					errorData = Arrays.toString(parms);
				throw new ExecutionException(
						NLS.bind(Messages.getString("BatchUtilities.command_execution_failed"), ret, errorData), //$NON-NLS-1$
						null);
			} catch (InterruptedException e1) {
				String errorData = errorGrabber.getData().trim();
				throw new ExecutionException(NLS.bind(Messages.getString("BatchUtilities.time_limit_exceeded"), //$NON-NLS-1$
						new Object[] { label, errorData, timeout / 1000 }), e1);
			}
		} finally {
			if (inputGrabber != null)
				inputGrabber.abort();
			if (errorGrabber != null)
				errorGrabber.abort();
		}
	}

	/**
	 * Encodes plain text into HTML
	 *
	 * @param s
	 *            - text to encode
	 * @param brk
	 *            - true if line break is to be converted into br tag
	 * @return html
	 */
	public static String encodeHTML(String s, boolean brk) {
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
			case '\r':
				break;
			case '\n':
				if (brk)
					out.append("<br/>"); //$NON-NLS-1$
				else
					out.append(c);
				break;
			case '&':
				int p = s.indexOf(';', i);
				if (p > i && p <= i + 9)
					out.append(c);
				else
					out.append("&amp;"); //$NON-NLS-1$
				break;
			default:
				String entity = codeMap.get(c);
				if (entity != null)
					out.append(entity);
				else if (c > 127)
					out.append("&#").append((int) c).append(';'); //$NON-NLS-1$
				else
					out.append(c);
				break;
			}
		}
		return out.toString();
	}

	/**
	 * Converts string to valid XML text
	 *
	 * @param s
	 *            - text to encode
	 * @param ascii
	 *            -1 if non-Ascii characters are to be converted into '?', 1 if
	 *            non-Ascii characters are to be converted to XML entities 0 for
	 *            Unicode
	 *
	 * @return xml
	 */
	public static String encodeXML(String s, int ascii) {
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '"')
				out.append("&quot;"); //$NON-NLS-1$
			else if (c == '\'')
				out.append("&apos;"); //$NON-NLS-1$
			else if (c == '&')
				out.append("&amp;"); //$NON-NLS-1$
			else if (c == '<')
				out.append("&lt;"); //$NON-NLS-1$
			else if (c == '>')
				out.append("&gt;"); //$NON-NLS-1$
			else if (ascii != 0 && c > 127) {
				if (ascii > 0)
					out.append("&#").append((int) c).append(';'); //$NON-NLS-1$
				else
					out.append('?');
			} else
				out.append(c);
		}
		return out.toString();
	}

	/**
	 * Decodes HTML into plain text
	 *
	 * @param html
	 *            - input html
	 * @param result
	 *            - resulting plain text
	 * @return - true if input contains markup that cannot be represented in plain
	 *         text
	 */
	private static final int START = 0;
	private static final int TAGOREND = 1;
	private static final int TAG = 2;
	private static final int END = 3;
	private static final int SINGLE = 4;
	private static final int ENTITY = 5;

	public static String decodeHTML(String html) {
		StringBuilder sb = new StringBuilder();
		decodeHTML(html, sb);
		return sb.toString();
	}

	public static boolean decodeHTML(String html, StringBuilder sb) {
		boolean markup = false;
		int state = START;
		StringBuilder text = new StringBuilder();
		for (int i = 0; i < html.length(); i++) {
			char c = html.charAt(i);
			switch (state) {
			case TAGOREND:
				switch (c) {
				case '/':
					state = END;
					break;
				default:
					state = TAG;
					text.append(c);
					break;
				}
				break;
			case TAG:
				switch (c) {
				case '/':
					state = SINGLE;
					break;
				case '>':
					state = START;
					String tagName = text.toString();
					if (tagName.toLowerCase().equals("br")) //$NON-NLS-1$
						sb.append('\n');
					else
						markup = true;
					break;
				default:
					text.append(c);
					break;
				}
				break;
			case SINGLE:
				switch (c) {
				case '>':
					state = START;
					String tagName = text.toString();
					if (tagName.toLowerCase().equals("br")) //$NON-NLS-1$
						sb.append('\n');
					else
						markup = true;
					break;
				default:
					text.append(c);
					break;
				}
				break;
			case END:
				switch (c) {
				case '>':
					state = START;
					break;
				default:
					break;
				}
				break;
			case ENTITY:
				switch (c) {
				case ';':
					state = START;
					String name = text.toString();
					String entity = new StringBuilder().append('&').append(name).append(';').toString();
					Character code = entityMap.get(entity);
					if (code != null)
						sb.append(code.charValue());
					else if (name.startsWith("#")) { //$NON-NLS-1$
						try {
							sb.append((char) Integer.parseInt(name.substring(1)));
						} catch (NumberFormatException e) {
							sb.append(entity);
						}
					} else
						sb.append(entity);
					break;
				default:
					text.append(c);
					break;
				}
				break;
			default:
				switch (c) {
				case '<':
					text.setLength(0);
					state = TAGOREND;
					break;
				case '&':
					state = ENTITY;
					text.setLength(0);
					break;
				default:
					sb.append(c);
					break;
				}
				break;
			}
		}
		return markup;
	}

	public static int parseInt(String s) throws NumberFormatException {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return (int) (0.5d + Double.parseDouble(s));
		}
	}

	public static int readInt(byte[] b, int i) {
		return (b[i] & 0xff) * 256 + (b[i + 1] & 0xff);
	}

	public static void yield() {
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			// ignore
		}
	}

	public static File makeUniqueFile(File subFolder, String filename, String ext) {
		filename = toValidFilename(filename);
		int tara = subFolder.getAbsolutePath().length() + 1 + filename.length() + ext.length();
		File target;
		int i = 0;
		while (true) {
			String uniqueFileName = filename;
			if (i > 0) {
				String suffix = "-" + i; //$NON-NLS-1$
				int diff = tara + suffix.length() - BatchConstants.MAXPATHLENGTH;
				if (diff > 0)
					uniqueFileName = uniqueFileName.substring(0, uniqueFileName.length() - diff) + suffix;
				else
					uniqueFileName += suffix;
			}
			++i;
			target = new File(subFolder, uniqueFileName + ext);
			if (!target.exists())
				break;
		}
		return target;
	}

	private static final String FORBIDDENCHARS = "/\\<>:%?*|\"."; //$NON-NLS-1$

	/**
	 * Check filename for forbidden characters
	 * 
	 * @param s
	 *            - string to check
	 * @return - invalid character or 0
	 */
	public static char checkFilename(String s) {
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (FORBIDDENCHARS.indexOf(c) >= 0)
				return c;
		}
		return 0;
	}

	public static void putPreferences(String key, String value) {
		putPreferences(InstanceScope.INSTANCE.getNode(BatchActivator.PLUGIN_ID), key, value);
	}

	public static void putPreferences(IEclipsePreferences node, String key, String value) {
		node.put(key, value);
		try {
			node.flush();
		} catch (BackingStoreException e1) {
			// should never happen
		}
	}

	public static void exportPreferences(File path) {
		try (OutputStream out = new BufferedOutputStream(new FileOutputStream(path))) {
			IPreferencesService preferencesService = Platform.getPreferencesService();
			preferencesService.exportPreferences(preferencesService.getRootNode(),
					new IPreferenceFilter[] { new IPreferenceFilter() {
						public String[] getScopes() {
							return new String[] { InstanceScope.SCOPE };
						}

						@SuppressWarnings({ "rawtypes", "unchecked" })
						public Map getMapping(String scope) {
							return null;
						}
					} }, out);
		} catch (CoreException e) {
			BatchActivator.getDefault().logError(Messages.getString("BatchUtilities.internal_error"), e); //$NON-NLS-1$
		} catch (IOException e1) {
			// do nothing
		}
	}
	
}
