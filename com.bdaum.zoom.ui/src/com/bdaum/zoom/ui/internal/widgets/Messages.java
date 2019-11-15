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

package com.bdaum.zoom.ui.internal.widgets;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.ui.internal.widgets.messages"; //$NON-NLS-1$
	public static String AddToCatGroup_add_exported_to_cat;
	public static String AddToCatGroup_add_to_watched;
	public static String AnimatedGallery_color_code;
	public static String AnimatedGallery_location_present;
	public static String AnimatedGallery_no_location;
	public static String AnimatedGallery_play_voicenote;
	public static String AnimatedGallery_rating;
	public static String AnimatedGallery_raw_recipe_applies;
	public static String AnimatedGallery_rotate_left;
	public static String AnimatedGallery_rotate_right;
	public static String AutoRatingGroup_auto_rating;
	public static String AutoRatingGroup_enable;
	public static String AutoRatingGroup_overwrite;
	public static String AutoRatingGroup_provider;
	public static String AutoRatingGroup_rating;
	public static String AutoRatingGroup_select_provider;
	public static String AutoRatingGroup_select_theme;
	public static String AutoRatingGroup_theme;
	public static String CheckedText_text_operation;
	public static String CodeGroup_select_code;
	public static String DescriptionGroup_description;
	public static String DescriptionGroup_existing_markup_will_be_deleted;
	public static String DescriptionGroup_html_assist;
	public static String DescriptionGroup_html_to_plain;
	public static String DescriptionGroup_plain_text;
	public static String FileEditor_browse;
	public static String FileEditor_browse_tooltip;
	public static String FileEditor_clear_tooltip;
	public static String FileEditor_file_exists;
	public static String FileEditor_overwrite;
	public static String FilterField_enter_filter_expr;
	public static String FilterField_expressions_entered;
	public static String JpegMetaGroup_insert_into_jpeg;
	public static String JpegMetaGroup_xmp_warning;
	public static String PatternListEditor_accept;
	public static String PatternListEditor_add;
	public static String PatternListEditor_down;
	public static String PatternListEditor_reject;
	public static String PatternListEditor_remove;
	public static String PatternListEditor_up;
	public static String PrivacyGroup_all;
	public static String PrivacyGroup_public;
	public static String PrivacyGroup_publicMedium;
	public static String ProposalListener_not_valid;
	public static String SearchResultGroup_configure;
	public static String SearchResultGroup_keywords;
	public static String SearchResultGroup_maxNumber;
	public static String SearchResultGroup_minScore;
	public static String SearchResultGroup_search_algorithm;
	public static String SearchResultGroup_search_by;
	public static String SearchResultGroup_search_parameters;
	public static String SearchResultGroup_visual;
	public static String SectionLayoutGroup_layout;
	public static String SectionLayoutGroup_map_left;
	public static String SectionLayoutGroup_map_right;
	public static String SectionLayoutGroup_no_geotagged;
	public static String SectionLayoutGroup_no_map;
	public static String SectionLayoutGroup_text_only;
	public static String SectionLayoutGroup_thumbs_bottom;
	public static String SectionLayoutGroup_thumbs_left;
	public static String SectionLayoutGroup_thumbs_right;
	public static String SectionLayoutGroup_thumbs_top;
	public static String TextField_text_operation;
	public static String TextMenuListener_add_to_dict;
	public static String TextMenuListener_copy;
	public static String TextMenuListener_cut;
	public static String TextMenuListener_edit_text;
	public static String TextMenuListener_paste;
	public static String TextMenuListener_redo;
	public static String TextMenuListener_selectall;
	public static String TextMenuListener_undo;
	public static String TextOperation_edit_text;
	public static String TextWithVariableGroup_add_metadata;
	public static String TextWithVariableGroup_add_variable;
	public static String TextWithVariableGroup_example;
	public static String TextWithVariableGroup_select_template;
	public static String WatermarkGroup_create_watermark;
	public static String WatermarkGroup_does_not_exist;
	public static String WatermarkGroup_select_file;
	public static String WatermarkGroup_watermark_files;
	public static String WebFontGroup_select_font;
	public static String CompressionGroup_jpegQuality;
	public static String CompressionGroup_use_webp;
	public static String CompressionGroup_webp_tooltip;
	public static String ExpandCollapseGroup_collapseall;
	public static String ExpandCollapseGroup_expandall;
	public static String LabelConfigGroup_alignment;
	public static String LabelConfigGroup_center;
	public static String LabelConfigGroup_custom;
	public static String LabelConfigGroup_fontsize;
	public static String LabelConfigGroup_inherited;
	public static String LabelConfigGroup_left;
	public static String LabelConfigGroup_nothing;
	public static String LabelConfigGroup_provide_template;
	public static String LabelConfigGroup_right;
	public static String LabelConfigGroup_show;
	public static String LabelConfigGroup_template;
	public static String LabelConfigGroup_title;
	public static String LabelConfigGroup_tumbnail_labels;
	public static String SharpeningGroup_apply_sharpening;
	public static String SharpeningGroup_radius;
	public static String SharpeningGroup_amount;
	public static String SharpeningGroup_threshhold;
	public static String QualityGroup_output_quality;
	public static String QualityGroup_resolution;
	public static String QualityGroup_screen;
	public static String QualityGroup_printer;


	public static String RenameGroup_add;
	public static String RenameGroup_change;
	public static String RenameGroup_cue;
	public static String RenameGroup_cue_empty;
	public static String RenameGroup_cue2;
	public static String RenameGroup_edit;
	public static String RenameGroup_file_renaming;
	public static String RenameGroup_filename_imagename;
	public static String RenameGroup_please_select_template;
	public static String RenameGroup_preview;
	public static String RenameGroup_remove;
	public static String RenameGroup_select_template;
	public static String RenameGroup_start_at;


	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
