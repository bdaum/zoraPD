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

package com.bdaum.zoom.lal.internal.lire;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static String BUNDLE_NAME = "com.bdaum.zoom.lal.internal.lire.messages"; //$NON-NLS-1$
	public static String Constants_MPEG7_Explanation;
	public static String Constants_scalable_color_Explanation;
	public static String Constants_edge_histogram_Explanation;
	public static String Constants_color_layout_Explanation;
	public static String Constants_overall_color_Explanation;
	public static String Constants_CEDD_Explanation;
	public static String Constants_FCTH_Explanation;
	public static String Constants_Tamura_Explanation;
	public static String Constants_Gabor_Explanation;
	public static String Constants_auto_color;
	public static String Constants_auto_color_explanation;
	public static String Constants_CEDD;
	public static String Constants_color_histogram;
	public static String Constants_color_histogram_explanation;
	public static String Constants_color_layout;
	public static String Constants_conbined_mpeg7;
	public static String Constants_edge_histogram;
	public static String Constants_FCTH;
	public static String Constants_gabor;
	public static String Constants_JCD;
	public static String Constants_JCD_explanation;
	public static String Constants_JPEG;
	public static String Constants_JPEG_explanation;
	public static String Constants_joint_histogram;
	public static String Constants_joint_histogram_explanation;
	public static String Constants_luminance_layout;
	public static String Constants_luminance_layout__explanation;
	public static String Constants_opponent_histogram;
	public static String Constants_opponent_histogram_explanation;
	public static String Constants_overall_color;
	public static String Constants_PHOG;
	public static String Constants_PHOG_explanation;
	public static String Constants_scalable_color;
	public static String Constants_Tamura;

	public static String IndexingJob_Indexing;

	public static String IndexingJob_Indexing_report;

	public static String IndexingJob_cannot_delete_folders;

	public static String IndexingJob_disk_full;

	public static String IndexingJob_error_closing_lucene_index;

	public static String IndexingJob_error_creating_lucene_index;

	public static String IndexingJob_indexed_elapsed;

	public static String IndexingJob_indexing_stopped;

	public static String IndexingJob_internal_error;

	public static String IndexingJob_internal_error_when_indexing;

	public static String IndexingJob_io_error_when_generating_index_data;

	public static String IndexingJob_ioerror_restoring_folder;

	public static String IndexingJob_ioerror_updating_lucene_index;

	public static String IndexingJob_lucene_index_is_corrupt;

	public static String IndexingJob_n_of_m;

	public static String IndexingJob_thumbnail_corrupt;
	public static String Lire_ACCID_expl;
	public static String Lire_bin_pat_pyr;
	public static String Lire_bin_pat_pyr_expl;
	public static String Lire_centrist;
	public static String Lire_centrist_expl;
	public static String Lire_corrupt_index;
	public static String Lire_fuzzy_color_hist;
	public static String Lire_fuzzy_color_hist_expl;
	public static String Lire_fuzzy_opt_hist;
	public static String Lire_fuzzy_opt_hist_expl;
	public static String Lire_io_error_searching;
	public static String Lire_lire_service_started;
	public static String Lire_local_bin_pattern;
	public static String Lire_local_bin_pattern_expl;
	public static String Lire_local_bin_pattern_oppo;
	public static String Lire_local_bin_pattern_oppo_expl;

	public static String Lire_rank_oppo;
	public static String Lire_rank_oppo_expl;
	public static String Lire_rotinv_bin;
	public static String Lire_rotinv_bin_expl;
	public static String Lire_spacc;
	public static String Lire_spacc_expl;
	public static String Lire_spcedd;
	public static String Lire_spcedd_expl;
	public static String Lire_spfcth;
	public static String Lire_spfcth_expl;
	public static String Lire_spjcd;
	public static String Lire_spjcd_expl;
	public static String Lire_splbp;
	public static String Lire_splbp_expl;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
