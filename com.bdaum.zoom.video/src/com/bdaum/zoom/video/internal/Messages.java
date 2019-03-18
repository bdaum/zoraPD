package com.bdaum.zoom.video.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.video.internal.messages"; //$NON-NLS-1$
	public static String Activator_cannot_create_filter;
	public static String VideoHoverContribution_gallery;
	public static String VideoHoverContribution_meta_block;
	public static String VideoHoverContribution_network_orig;
	public static String VideoHoverContribution_origin;
	public static String VideoHoverContribution_videos;
	public static String VideoHoverContribution_videos_expl;
	public static String VideoSupport_avgbitrate;
	public static String VideoSupport_bad_duration_value;
	public static String VideoSupport_bad_fps;
	public static String VideoSupport_convert_to_streaming;
	public static String VideoSupport_convert_video_file;
	public static String VideoSupport_no;
	public static String VideoSupport_reserved;
	public static String VideoSupport_taking_snapshot_failed;
	public static String VideoSupport_unknown;
	public static String VideoSupport_Videos;

	public static String VideoSupport_yes;
	public static String VideoSupport_yes_to_all;
	public static String QueryField_cat_video;
	public static String QueryField_audio;
	public static String QueryField_audio_bitrate;
	public static String QueryField_audio_bits_per_sample;
	public static String QueryField_audio_channels;
	public static String QueryField_audio_sample_rate;
	public static String QueryField_audio_stream_type;
	public static String QueryField_video;
	public static String QueryField_video_frame_rate;
	public static String QueryField_video_stream_type;
	public static String QueryField_surround_mode;
	public static String QueryField_dolby_surround;
	public static String QueryField_not_dolby_surround;
	public static String QueryField_not_indicated;
	public static String QueryField_general;
	public static String QueryField_bit_depth;
	public static String QueryField_duration;


	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
