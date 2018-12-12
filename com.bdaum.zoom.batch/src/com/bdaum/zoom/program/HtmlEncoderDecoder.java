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
 * (c) 2018 Berthold Daum  
 */

package com.bdaum.zoom.program;

import java.util.HashMap;
import java.util.Map;

public class HtmlEncoderDecoder {
	
	private static final int START = 0;
	private static final int TAGOREND = 1;
	private static final int TAG = 2;
	private static final int END = 3;
	private static final int SINGLE = 4;
	private static final int ENTITY = 5;
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

	
	private StringBuilder out = new StringBuilder();
	private StringBuilder text;

	/**
	 * Encodes plain text into HTML
	 *
	 * @param s
	 *            - text to encode
	 * @param brk
	 *            - true if line break is to be converted into br tag
	 * @return html
	 */
	public String encodeHTML(String s, boolean brk) {
		out.setLength(0);
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

	public String decodeHTML(String html) {
		out.setLength(0);
		decodeHTML(html, out);
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
	public boolean decodeHTML(String html, StringBuilder sb) {
		if (text == null)
			text = new StringBuilder();
		else
			text.setLength(0);
		boolean markup = false;
		int state = START;
		for (int i = 0; i < html.length(); i++) {
			char c = html.charAt(i);
			switch (state) {
			case TAGOREND:
				if (c == '/')
					state = END;
				else {
					state = TAG;
					text.append(c);
				}
				break;
			case TAG:
				if (c == '/')
					state = SINGLE;
				else if (c == '>') {
					state = START;
					String tagName = text.toString();
					if (tagName.toLowerCase().equals("br")) //$NON-NLS-1$
						sb.append('\n');
					else
						markup = true;
				} else
					text.append(c);
				break;
			case SINGLE:
				if (c == '>') {
					state = START;
					String tagName = text.toString();
					if (tagName.toLowerCase().equals("br")) //$NON-NLS-1$
						sb.append('\n');
					else
						markup = true;
				} else
					text.append(c);
				break;
			case END:
				if (c == '>')
					state = START;
				break;
			case ENTITY:
				if (c == ';') {
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
				} else
					text.append(c);
				break;
			default:
				if (c == '<') {
					text.setLength(0);
					state = TAGOREND;
				} else if (c == '&') {
					state = ENTITY;
					text.setLength(0);
				} else
					sb.append(c);
				break;
			}
		}
		return markup;
	}


}
