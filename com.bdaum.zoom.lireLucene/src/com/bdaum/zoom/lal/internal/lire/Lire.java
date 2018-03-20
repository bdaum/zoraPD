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
 * (c) 2014-2017 Berthold Daum  
 */
package com.bdaum.zoom.lal.internal.lire;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.db.ICollectionProcessor;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.ai.IAiService;
import com.bdaum.zoom.core.internal.lire.Algorithm;
import com.bdaum.zoom.core.internal.lire.ILireService;
import com.bdaum.zoom.lal.internal.LireActivator;
import com.bdaum.zoom.lal.internal.lire.ui.dialogs.ConfigureSimilaritySearchDialog;
import com.bdaum.zoom.lal.internal.lire.ui.dialogs.SearchSimilarDialog;
import com.bdaum.zoom.lal.internal.lire.ui.dialogs.TextSearchDialog;
import com.bdaum.zoom.ui.internal.UiActivator;

import net.semanticmetadata.lire.searchers.ImageDuplicates;
import net.semanticmetadata.lire.searchers.ImageSearcher;

@SuppressWarnings("restriction")
public class Lire implements ILireService {

	public static final int MPEG7 = 0;
	public static final int SCALABLECOLOR = 1;
	public static final int EDGEHISTOGRAM = 2;
	public static final int COLORLAYOUT = 3;
	public static final int COLORONLY = 4;
	public static final int CEDD = 5;
	public static final int FCTH = 6;
	public static final int TAMURA = 7;
	public static final int GABOR = 8;
	public static final int COLORHISTOGRAM = 9;
	public static final int JCD = 10;
	public static final int JOINTHISTOGRAM = 11;
	public static final int JPEG = 12;
	public static final int LUMINANCE = 13;
	public static final int OPPONENTHISTOGRAM = 14;
	public static final int PHOG = 15;
	public static final int AUTOCOLOR = 16;
	public static final int ACCID = 17;
	public static final int BINPATTERNSPYRAMID = 18;
	public static final int FUZZYOPPOHISTOGRAM = 19;
	public static final int LOCALBINARYPATTERN = 20;
	public static final int LOCALBINARYPATTERNOPPO = 21;
	public static final int FUZZYCOLORHISTOGRAM = 22;
	public static final int RANKOPPONENT = 23;
	public static final int ROTINVBINPATTERN = 24;
	public static final int SPACC = 25;
	public static final int SPATIALPYRAMIDCENTRIST = 26;
	public static final int SPCEDD = 27;
	public static final int SPFCTH = 28;
	public static final int SPJCD = 29;
	public static final int SPLBP = 30;

	public static Algorithm[] SupportedSimilarityAlgorithms;
	
	private Set<String> postponedIndexing = Collections.synchronizedSet(new HashSet<>(511));


	/**
	 * Called when service is activated.
	 */
	public void activate() {
		LireActivator.getDefault().logInfo(Messages.Lire_lire_service_started);
	}

	/**
	 * Called when service is deactivated.
	 */
	public void deactivate() {
		// do nothing
	}

	// Place default algorithm to top of list
	public static void setUpAlgorithms() {
		SupportedSimilarityAlgorithms = new Algorithm[] {
				new Algorithm(ACCID, "ACCID", "ACCID", //$NON-NLS-1$ //$NON-NLS-2$
						Messages.Lire_ACCID_expl, true),
				new Algorithm(JCD, "JCD", Messages.Constants_JCD, //$NON-NLS-1$
						Messages.Constants_JCD_explanation, true),
				new Algorithm(SPJCD, "spjcd", Messages.Lire_spjcd, Messages.Lire_spjcd_expl, //$NON-NLS-1$
						false),
				new Algorithm(CEDD, "CEDD", Messages.Constants_CEDD, //$NON-NLS-1$
						Messages.Constants_CEDD_Explanation, true),
				new Algorithm(SPCEDD, "spcedd", Messages.Lire_spcedd, Messages.Lire_spcedd_expl, //$NON-NLS-1$
						false),
				new Algorithm(FCTH, "FCTH", Messages.Constants_FCTH, //$NON-NLS-1$
						Messages.Constants_FCTH_Explanation, false),
				new Algorithm(SPFCTH, "spfcth", Messages.Lire_spfcth, Messages.Lire_spfcth_expl, //$NON-NLS-1$
						false),
				new Algorithm(MPEG7, "mpeg7", Messages.Constants_conbined_mpeg7, //$NON-NLS-1$
						Messages.Constants_MPEG7_Explanation, false),
				new Algorithm(SCALABLECOLOR, "scalable_color", //$NON-NLS-1$
						Messages.Constants_scalable_color, Messages.Constants_scalable_color_Explanation, true),
				new Algorithm(BINPATTERNSPYRAMID, "bin_pattern_pyr", Messages.Lire_bin_pat_pyr, //$NON-NLS-1$
						Messages.Lire_bin_pat_pyr_expl, false),
				new Algorithm(ROTINVBINPATTERN, "rotinv_bin", Messages.Lire_rotinv_bin, //$NON-NLS-1$
						Messages.Lire_rotinv_bin_expl, false),
				new Algorithm(EDGEHISTOGRAM, "edge_histogram", //$NON-NLS-1$
						Messages.Constants_edge_histogram, Messages.Constants_edge_histogram_Explanation, true),
				new Algorithm(COLORLAYOUT, "color_layout", //$NON-NLS-1$
						Messages.Constants_color_layout, Messages.Constants_color_layout_Explanation, true),
				new Algorithm(TAMURA, "Tamura", Messages.Constants_Tamura, //$NON-NLS-1$
						Messages.Constants_Tamura_Explanation, false),
				new Algorithm(GABOR, "Gabor", Messages.Constants_gabor, //$NON-NLS-1$
						Messages.Constants_Gabor_Explanation, false),
				new Algorithm(FUZZYOPPOHISTOGRAM, "fuzzy_opp_hist", Messages.Lire_fuzzy_opt_hist, //$NON-NLS-1$
						Messages.Lire_fuzzy_opt_hist_expl, false),
				new Algorithm(FUZZYCOLORHISTOGRAM, "fuzzy_color_hist", Messages.Lire_fuzzy_color_hist, //$NON-NLS-1$
						Messages.Lire_fuzzy_color_hist_expl, false),
				new Algorithm(COLORHISTOGRAM, "color_histogram", Messages.Constants_color_histogram, //$NON-NLS-1$
						Messages.Constants_color_histogram_explanation, false),
				new Algorithm(JOINTHISTOGRAM, "joint_histogram", Messages.Constants_joint_histogram, //$NON-NLS-1$
						Messages.Constants_joint_histogram_explanation, false),
				new Algorithm(RANKOPPONENT, "rank_oppo", Messages.Lire_rank_oppo, //$NON-NLS-1$
						Messages.Lire_rank_oppo_expl,
						false),
				new Algorithm(JPEG, "JPEG", Messages.Constants_JPEG, //$NON-NLS-1$
						Messages.Constants_JPEG_explanation, false),
				new Algorithm(LUMINANCE, "luminance", Messages.Constants_luminance_layout, //$NON-NLS-1$
						Messages.Constants_luminance_layout__explanation, false),
				new Algorithm(OPPONENTHISTOGRAM, "opponent_histogram", Messages.Constants_opponent_histogram, //$NON-NLS-1$
						Messages.Constants_opponent_histogram_explanation, false),
				new Algorithm(PHOG, "PHOG", Messages.Constants_PHOG, //$NON-NLS-1$
						Messages.Constants_PHOG_explanation, true),
				new Algorithm(LOCALBINARYPATTERNOPPO, "local_bin_patt_oppo", Messages.Lire_local_bin_pattern_oppo, //$NON-NLS-1$
						Messages.Lire_local_bin_pattern_oppo_expl, false),
				new Algorithm(LOCALBINARYPATTERN, "local_bin_pattern", Messages.Lire_local_bin_pattern, //$NON-NLS-1$
						Messages.Lire_local_bin_pattern_expl, false),
				new Algorithm(SPLBP, "splbp", Messages.Lire_splbp, //$NON-NLS-1$
						Messages.Lire_splbp_expl, false),
				new Algorithm(SPACC, "spacc", Messages.Lire_spacc, //$NON-NLS-1$
						Messages.Lire_spacc_expl, false),
				new Algorithm(AUTOCOLOR, "auto_color", Messages.Constants_auto_color, //$NON-NLS-1$
						Messages.Constants_auto_color_explanation, false),
				new Algorithm(COLORONLY, "overall_color", //$NON-NLS-1$
						Messages.Constants_overall_color, Messages.Constants_overall_color_Explanation, false),
				new Algorithm(SPATIALPYRAMIDCENTRIST, "centrist", Messages.Lire_centrist, //$NON-NLS-1$
						Messages.Lire_centrist_expl, false),
		};
		IAiService service = CoreActivator.getDefault().getAiService();
		if (service != null) {
			Algorithm[] aiAlgs = service.getLireAlgorithms();
			Algorithm[] newAlgos = new Algorithm[SupportedSimilarityAlgorithms.length + aiAlgs.length];
			System.arraycopy(SupportedSimilarityAlgorithms, 0, newAlgos, 0, SupportedSimilarityAlgorithms.length);
			System.arraycopy(aiAlgs, 0, newAlgos, SupportedSimilarityAlgorithms.length, aiAlgs.length);
			SupportedSimilarityAlgorithms = newAlgos;
		}
	}

	public void configureCBIR(Collection<String> algo) {
		LireActivator.getDefault().setAlgorithms(algo);
	}

	public Job createIndexingJob() {
		return new IndexingJob(null, postponedIndexing);
	}

	public Job createIndexingJob(String[] assetIds) {
		return new IndexingJob(assetIds, postponedIndexing);
	}

	public Job createIndexingJob(File indexBackup, Date lastBackup) {
		return new IndexingJob(indexBackup, lastBackup, postponedIndexing);
	}

	public Job createIndexingJob(Collection<Asset> assets, boolean reimport, int totalWork, int worked,
			boolean system) {
		return new IndexingJob(assets, reimport, totalWork, worked, system, postponedIndexing);
	}

	public Algorithm[] getSupportedSimilarityAlgorithms() {
		return LireActivator.getDefault().getSupportedSimilarityAlgorithms();
	}

	public Algorithm getAlgorithmById(int method) {
		return LireActivator.getDefault().getAlgorithmById(method);
	}

	public Set<String> postponeIndexing() {
		IJobManager jobManager = Job.getJobManager();
		jobManager.cancel(Constants.INDEXING);
		try {
			jobManager.join(Constants.INDEXING, null);
		} catch (OperationCanceledException | InterruptedException e) {
			// do nothing
		}
		return postponedIndexing;
	}
	
	public List<List<String>> findDuplicates(File indexPath, int method, IProgressMonitor monitor) {
		LireActivator activator = LireActivator.getDefault();
		ImageSearcher searcher = activator.getContentSearcher(method, Integer.MAX_VALUE);
		if (searcher == null)
			return null;
		IndexReader reader = null;
		ArrayList<List<String>> duplicateList = null;
		try {
			reader = activator.getIndexReader(indexPath);
			ImageDuplicates duplicates = searcher.findDuplicates(reader);
			if (monitor.isCanceled())
				return null;
			int l = duplicates.length();
			duplicateList = new ArrayList<List<String>>(l);
			for (int i = 0; i < l; i++)
				duplicateList.add(duplicates.getDuplicate(i));
			return duplicateList;
		} catch (CorruptIndexException e) {
			activator.logError(NLS.bind(Messages.Lire_corrupt_index, indexPath), e);
		} catch (IOException e) {
			activator.logError(NLS.bind(Messages.Lire_io_error_searching, indexPath), e);
		} finally {
			if (reader != null)
				activator.releaseIndexReader(indexPath, reader);
		}
		return null;
	}

	@Override
	public SmartCollectionImpl updateQuery(SmartCollection current, Object value, IAdaptable adaptable,
			String kind) {
		Shell shell = adaptable.getAdapter(Shell.class);
		if (kind.equals(ICollectionProcessor.SIMILARITY)) {
			SearchSimilarDialog dialog = new SearchSimilarDialog(shell, (Asset) value, current);
			return dialog.open() == Window.OK ? dialog.getResult() : null;
		}
		TextSearchDialog dialog = new TextSearchDialog(shell, current, (String) value);
		return dialog.open() == Window.OK ? dialog.getResult() : null;
	}

	@Override
	public void performQuery(Object value, IAdaptable adaptable, String kind) {
		SmartCollectionImpl sm = updateQuery(null, value, adaptable, kind);
		if (sm != null) {
			IWorkbenchWindow window = adaptable.getAdapter(IWorkbenchWindow.class);
			if (window != null)
				UiActivator.getDefault().getNavigationHistory(window).postSelection(new StructuredSelection(sm));
		}
	}

	@Override
	public boolean ShowConfigureSearch(IAdaptable adaptable, Point displayLocation) {
		ConfigureSimilaritySearchDialog dialog = new ConfigureSimilaritySearchDialog(adaptable.getAdapter(Shell.class));
		dialog.create();
		if (displayLocation != null)
			dialog.getShell().setLocation(displayLocation);
		return dialog.open() == Window.OK;
	}

}
