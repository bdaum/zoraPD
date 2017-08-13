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
 * (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.ui.internal.widgets;

import java.util.ArrayList;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.lire.Algorithm;
import com.bdaum.zoom.ui.internal.dialogs.EditMetaDialog;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.bdaum.zoom.ui.widgets.CLink;
import com.bdaum.zoom.ui.widgets.NumericControl;

@SuppressWarnings("restriction")
public class SearchResultGroup {

	private static final String MAX_NUMBER = "maxNumber"; //$NON-NLS-1$
	private static final String SCORE = "score"; //$NON-NLS-1$
	private static final String METHOD = "method"; //$NON-NLS-1$
	private static final String WEIGHT = "weight"; //$NON-NLS-1$
	private boolean enabled = true;

	private Composite composite;
	private NumericControl numberField;
	private NumericControl scoreField;
	private ComboViewer algoViewer;
	private Label algoExplanation;
	private ArrayList<Algorithm> opts;
	protected Set<String> supportedAlgorithms;
	private Button okButton;
	private CLink link;
	private Scale scale;

	public SearchResultGroup(Composite parent, int style, boolean methods, boolean scoreAndHits, boolean keywords,
			Button okButton, Object layoutData) {
		Composite area = new Composite(parent, SWT.NONE);
		area.setLayout(new GridLayout(2, false));
		this.okButton = okButton;
		CGroup group = new CGroup(area, style);
		group.setText(
				methods ? Messages.SearchResultGroup_search_algorithm : Messages.SearchResultGroup_search_parameters);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		group.setLayout(new GridLayout(1, false));
		composite = new Composite(group, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		if (layoutData != null)
			area.setLayoutData(layoutData);
		else {
			int columns = -1;
			Layout layout = parent.getLayout();
			if (layout instanceof GridLayout)
				columns = ((GridLayout) layout).numColumns;
			if (columns > 0)
				area.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, columns, 1));
			else
				area.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		}
		int numColumns = (style & SWT.VERTICAL) != 0 ? 2 : 4;
		GridLayout gridlayout = new GridLayout(numColumns, false);
		gridlayout.marginHeight = 0;
		gridlayout.marginWidth = 0;
		composite.setLayout(gridlayout);
		if (methods) {
			Composite methodGroup = new Composite(composite, SWT.NONE);
			methodGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, numColumns, 1));
			GridLayout glayout = new GridLayout(2, false);
			glayout.marginWidth = 0;
			methodGroup.setLayout(glayout);

			algoViewer = new ComboViewer(methodGroup);
			algoViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			algoViewer.setContentProvider(ArrayContentProvider.getInstance());
			algoViewer.setLabelProvider(new LabelProvider());
			algoViewer.setComparator(new ViewerComparator());
			fillAlgoViewer();
			algoViewer.getCombo().setVisibleItemCount(opts.size());
			algoExplanation = new Label(composite, SWT.WRAP);
			GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, numColumns, 1);
			gridData.heightHint = 60;
			gridData.widthHint = 250;
			gridData.horizontalIndent = 15;
			algoExplanation.setLayoutData(gridData);
			algoViewer.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					updateControls();
				}
			});
			link = new CLink(methodGroup, SWT.NONE);
			link.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
			link.setText(Messages.SearchResultGroup_configure);
			link.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					BusyIndicator.showWhile(link.getDisplay(), () -> {
						IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow();
						if (activeWorkbenchWindow != null) {
							IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
							if (activePage != null) {
								EditMetaDialog mdialog = new EditMetaDialog(link.getShell(), activePage,
										Core.getCore().getDbManager(), false, null);
								mdialog.setInitialPage(EditMetaDialog.INDEXING);
								if (mdialog.open() == Dialog.OK) {
									ISelection selection = algoViewer.getSelection();
									fillAlgoViewer();
									algoViewer.setSelection(selection);
								}
							}
						}
					});
				}
			});

		}
		if (scoreAndHits) {
			Label numberlabel = new Label(composite, SWT.NONE);
			numberlabel.setText(com.bdaum.zoom.ui.internal.widgets.Messages.SearchResultGroup_maxNumber);
			numberField = new NumericControl(composite, SWT.NONE);
			numberField.setMaximum(1000);
			numberField.setMinimum(3);
			numberField.setLogrithmic(true);
			Label scorelabel = new Label(composite, SWT.NONE);
			scorelabel.setText(com.bdaum.zoom.ui.internal.widgets.Messages.SearchResultGroup_minScore);
			scoreField = new NumericControl(composite, SWT.NONE);
			scoreField.setMaximum(99);
			scoreField.setMinimum(1);
		}
		if (keywords) {
			CGroup keyGroup = new CGroup(area, SWT.NONE);
			GridData data = new GridData(SWT.FILL, SWT.FILL, false, true);
			data.widthHint = 120;
			keyGroup.setLayoutData(data);
			keyGroup.setLayout(new GridLayout());
			keyGroup.setText(Messages.SearchResultGroup_search_by);
			Label label = new Label(keyGroup, SWT.NONE);
			label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
			label.setText(Messages.SearchResultGroup_keywords);

			scale = new Scale(keyGroup, SWT.VERTICAL);
			data = new GridData(SWT.CENTER, SWT.CENTER, true, true);
			data.heightHint = 90;
			scale.setLayoutData(data);
			scale.setMaximum(100);
			scale.setIncrement(5);
			scale.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					scale.setToolTipText(String.valueOf(scale.getMaximum() - scale.getSelection()));
				}
			});
			label = new Label(keyGroup, SWT.NONE);
			label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
			label.setText(Messages.SearchResultGroup_visual);
		}
	}

	private void fillAlgoViewer() {
		supportedAlgorithms = CoreActivator.getDefault().getCbirAlgorithms();
		compileOptions();
		algoViewer.setInput(opts);
	}

	private void compileOptions() {
		opts = new ArrayList<Algorithm>();
		for (Algorithm algorithm : Core.getCore().getDbFactory().getLireService(true).getSupportedSimilarityAlgorithms())
			if (supportedAlgorithms.contains(algorithm.getName()))
				opts.add(algorithm);
	}

	public void updateControls() {
		if ((!enabled || algoViewer == null) && okButton != null) {
			okButton.setEnabled(true);
			return;
		}
		Algorithm algo = (Algorithm) ((IStructuredSelection) algoViewer.getSelection()).getFirstElement();
		if (algo != null) {
			if (algoExplanation != null)
				algoExplanation.setText(algo.getDescription());
			if (okButton != null)
				okButton.setEnabled(true);
		} else {
			if (algoExplanation != null)
				algoExplanation.setText(""); //$NON-NLS-1$
			if (okButton != null)
				okButton.setEnabled(false);
		}
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		if (algoViewer != null) {
			algoViewer.getControl().setEnabled(enabled);
			algoExplanation.setVisible(enabled);
			link.setVisible(enabled);
		}
		if (numberField != null)
			numberField.setEnabled(enabled);
		if (scoreField != null)
			scoreField.setEnabled(enabled);
		updateControls();
	}

	public void saveSettings(IDialogSettings settings) {
		if (algoViewer != null) {
			Algorithm selectedAlgorithm = getSelectedAlgorithm();
			settings.put(METHOD, selectedAlgorithm == null ? -1 : selectedAlgorithm.getId());
		}
		if (numberField != null)
			settings.put(MAX_NUMBER, numberField.getSelection());
		if (scoreField != null)
			settings.put(SCORE, scoreField.getSelection());
		if (scale != null)
			settings.put(WEIGHT, scale.getMaximum() - scale.getSelection());
	}

	public void fillValues(int score, int maxNumber, int method, int weight) {
		if (scoreField != null)
			scoreField.setSelection(score);
		if (numberField != null)
			numberField.setSelection(maxNumber);
		if (scale != null)
			scale.setSelection(scale.getMaximum() - weight);
		Algorithm algorithm = Core.getCore().getDbFactory().getLireService(true).getAlgorithmById(method);
		if (algorithm != null)
			algoViewer.setSelection(new StructuredSelection(algorithm));
	}

	public void fillValues(IDialogSettings settings) {

		if (settings != null) {
			if (numberField != null)
				try {
					numberField.setSelection(settings.getInt(MAX_NUMBER));
					return;
				} catch (NumberFormatException e) {
					numberField.setSelection(100);
				}
			if (scoreField != null)
				try {
					scoreField.setSelection(settings.getInt(SCORE));
					return;
				} catch (NumberFormatException e) {
					scoreField.setSelection(12);
				}
			if (scale != null)
				try {
					scale.setSelection(scale.getMaximum() - settings.getInt(WEIGHT));
					return;
				} catch (NumberFormatException e) {
					scale.setSelection(50);
				}
			if (algoViewer != null)
				try {
					Algorithm algorithm = Core.getCore().getDbFactory().getLireService(true)
							.getAlgorithmById(settings.getInt(METHOD));
					if (algorithm != null)
						algoViewer.setSelection(new StructuredSelection(algorithm));
					return;
				} catch (NumberFormatException e) {
					// do nothing
				}
		}
	}

	public int getMaxNumber() {
		return numberField == null ? 100 : numberField.getSelection();
	}

	public int getKeywordWeight() {
		return scale == null ? 0 : scale.getMaximum() - scale.getSelection();
	}

	public int getScore() {
		return scoreField == null ? 12 : scoreField.getSelection();
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setFocus() {
		scoreField.setFocus();
	}

	public Algorithm getSelectedAlgorithm() {
		return (algoViewer != null) ? (Algorithm) ((IStructuredSelection) algoViewer.getSelection()).getFirstElement()
				: null;
	}

	/**
	 * @param listener
	 * @see org.eclipse.jface.viewers.Viewer#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		algoViewer.addSelectionChangedListener(listener);
	}

	/**
	 * @param listener
	 * @see org.eclipse.jface.viewers.Viewer#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		algoViewer.removeSelectionChangedListener(listener);
	}

}
