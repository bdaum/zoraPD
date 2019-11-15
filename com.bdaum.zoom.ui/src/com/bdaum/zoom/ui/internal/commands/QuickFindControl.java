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
 * (c) 2016 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.commands;

import java.util.Arrays;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

import com.bdaum.zoom.cat.model.SimilarityOptions_typeImpl;
import com.bdaum.zoom.cat.model.TextSearchOptions_typeImpl;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.CriterionImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.ICollectionProcessor;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.QueryOptions;
import com.bdaum.zoom.core.internal.lire.Algorithm;
import com.bdaum.zoom.core.internal.lucene.ParseException;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.dialogs.KeywordSearchDialog;
import com.bdaum.zoom.ui.internal.views.EffectDropTargetListener;

@SuppressWarnings("restriction")
public class QuickFindControl extends WorkbenchWindowControlContribution
		implements KeyListener, FocusListener, SelectionListener, IAdaptable {

	private Text inputField;
	private ToolTip fieldTip;
	private ToolTip buttonTip;
	private Button button;
	private Image textsearchIcon;
	private boolean lireActive;
	private String[] keywords;

	public QuickFindControl() {
		super();
	}

	public QuickFindControl(String id) {
		super(id);
	}

	@Override
	protected Control createControl(Composite parent) {
		lireActive = Core.getCore().getDbFactory().getLireServiceVersion() >= 0;
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginWidth = layout.horizontalSpacing = 0;
		composite.setLayout(layout);
		inputField = new Text(composite, SWT.SEARCH | SWT.ICON_SEARCH);
		GridData layoutData = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, true);
		layoutData.widthHint = 100;
		layoutData.heightHint = 16;
		inputField.setLayoutData(layoutData);
		inputField.addKeyListener(this);
		inputField.addFocusListener(this);
		inputField.addMouseTrackListener(new MouseTrackAdapter() {
			@Override
			public void mouseExit(MouseEvent e) {
				fieldTip.setVisible(false);
			}

			@Override
			public void mouseHover(MouseEvent e) {
				showToolTip(true);
			}
		});
		addDropSupport(inputField);
		button = new Button(composite, SWT.PUSH);
		textsearchIcon = UiActivator.getImageDescriptor("icons/configure.png").createImage(); //$NON-NLS-1$
		button.setImage(textsearchIcon);
		layoutData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		layoutData.horizontalIndent = 5;
		layoutData.widthHint = 20;
		layoutData.heightHint = 16;
		button.setLayoutData(layoutData);
		button.addSelectionListener(this);
		button.addMouseTrackListener(new MouseTrackAdapter() {
			@Override
			public void mouseExit(MouseEvent e) {
				buttonTip.setVisible(false);
			}

			@Override
			public void mouseHover(MouseEvent e) {
				showButtonTip(e.stateMask);
			}
		});
		fieldTip = new ToolTip(parent.getShell(), SWT.BALLOON | SWT.ICON_WARNING);
		fieldTip.setVisible(false);
		fieldTip.setAutoHide(false);
		buttonTip = new ToolTip(parent.getShell(), SWT.BALLOON | SWT.ICON_INFORMATION);
		return composite;

	}

	private boolean validate(String s) {
		try {
			if (fullSearchActivated()) {
				Core.getCore().getDbFactory().getLuceneService().parseLuceneQuery(s);
				return true;
			}
			if (keywords == null)
				keywords = UiUtilities.getValueProposals(Core.getCore().getDbManager(), QueryField.IPTC_KEYWORDS, null,
						false);
			return KeywordSearchDialog.validate(s, keywords) == null;
		} catch (ParseException e) {
			return false;
		}
	}

	private boolean fullSearchActivated() {
		return lireActive && !Core.getCore().getDbManager().getMeta(true).getNoIndex();
	}

	private void performSearch(boolean shift) {
		String text = inputField.getText().trim();
		if (text.isEmpty() || shift || !validate(text)) {
			if (fullSearchActivated())
				Core.getCore().getDbFactory().getLireService(true).performQuery(text, this,
						ICollectionProcessor.TEXTSEARCH);
			else {
				KeywordSearchDialog dialog = new KeywordSearchDialog(getWorkbenchWindow().getShell(), text);
				if (dialog.open() == Window.OK)
					Ui.getUi().getNavigationHistory(getWorkbenchWindow())
							.postSelection(new StructuredSelection(dialog.getResult()));
			}
		} else {
			SmartCollection sm;
			if (fullSearchActivated()) {
				QueryOptions queryOptions = UiActivator.getDefault().getQueryOptions();
				sm = new SmartCollectionImpl(
						text + NLS.bind(Messages.QuickFindControl_maxmin, queryOptions.getMaxHits(),
								queryOptions.getScore()),
						false, false, true, queryOptions.isNetworked(), null, 0, null, 0, null, Constants.INHERIT_LABEL,
						null, 0, 1, null);
				sm.addCriterion(new CriterionImpl(ICollectionProcessor.TEXTSEARCH, null,
						new TextSearchOptions_typeImpl(text, queryOptions.getMaxHits(), queryOptions.getScore() / 100f),
						null, Constants.TEXTSEARCHOPTIONS_DEFAULT_MIN_SCORE, false));
			} else
				sm = KeywordSearchDialog.computeQuery(text, false, null);
			Ui.getUi().getNavigationHistory(getWorkbenchWindow()).postSelection(new StructuredSelection(sm));
		}
	}

	@Override
	public void focusGained(FocusEvent e) {
		inputField.selectAll();
		e.display.timerExec(300, () -> {
			if (!e.display.isDisposed())
				showToolTip(false);
		});
	}

	private void showToolTip(boolean always) {
		boolean visible = false;
		if (!fullSearchActivated()) {
			fieldTip.setText(Messages.QuickFindControl_search_impossible);
			fieldTip.setMessage(Messages.QuickFindControl_indexing_off);
			visible = true;
		} else if (Job.getJobManager().find(Constants.INDEXING).length > 0) {
			fieldTip.setText(Messages.QuickFindControl_in_progress);
			fieldTip.setMessage(Messages.QuickFindControl_incomplete);
			visible = true;
		} else if (always) {
			fieldTip.setText(Messages.QuickFindControl_text_similarity);
			fieldTip.setMessage(Messages.QuickFindControl_enter_search_string);
			visible = true;
		}
		if (visible) {
			Point displayLocation = inputField.toDisplay(inputField.getLocation());
			displayLocation.y += 15;
			fieldTip.setLocation(displayLocation);
		}
		fieldTip.setVisible(visible);
	}

	private void showButtonTip(int stateMask) {
		if (!fullSearchActivated()) {
			buttonTip.setText(Messages.QuickFindControl_search_impossible);
			buttonTip.setMessage(Messages.QuickFindControl_indexing_off);
		} else {
			String tip;
			String msg;
			if ((stateMask & SWT.CTRL) == SWT.CTRL) {
				tip = Messages.QuickFindControl_configure;
				msg = Messages.QuickFindControl_configure_similarity;
			} else if ((stateMask & SWT.SHIFT) == SWT.SHIFT) {
				tip = Messages.QuickFindControl_configure;
				msg = Messages.QuickFindControl_configurable_text;
			} else {
				tip = Messages.QuickFindControl_configure_text;
				msg = Messages.QuickFindControl_click_for_text;
			}
			buttonTip.setText(tip);
			buttonTip.setMessage(msg);
		}
		Point displayLocation = button.toDisplay(inputField.getLocation());
		displayLocation.y += 15;
		buttonTip.setLocation(displayLocation);
		buttonTip.setVisible(true);
	}

	@Override
	public void focusLost(FocusEvent e) {
		fieldTip.setVisible(false);
		keywords = null;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if ((e.stateMask & SWT.CTRL) != 0) {
			if (fullSearchActivated())
				configureSearch();
		} else
			performSearch((e.stateMask & SWT.SHIFT) != 0);
	}

	protected void addDropSupport(final Control control) {
		final int ops = DND.DROP_MOVE | DND.DROP_COPY;
		final DropTarget target = new DropTarget(control, ops);
		final TextTransfer textTransfer = TextTransfer.getInstance();
		Transfer[] types = new Transfer[] { textTransfer };
		target.setTransfer(types);
		target.addDropListener(new EffectDropTargetListener(control) {
			@Override
			public void dragEnter(DropTargetEvent event) {
				int detail = event.detail;
				event.detail = DND.DROP_NONE;
				if (fullSearchActivated()) {
					for (int i = 0; i < event.dataTypes.length; i++) {
						if (textTransfer.isSupportedType(event.dataTypes[i])) {
							event.currentDataType = event.dataTypes[i];
							if ((detail & ops) != 0) {
								event.detail = DND.DROP_COPY;
								break;
							}
						}
					}
				}
				super.dragEnter(event);
			}

			@Override
			public void dragOperationChanged(DropTargetEvent event) {
				event.detail = fullSearchActivated() ? DND.DROP_NONE
						: textTransfer.isSupportedType(event.currentDataType)
								? (event.detail & ops) == 0 ? DND.DROP_NONE : DND.DROP_COPY
								: DND.DROP_NONE;
			}

			@Override
			public void drop(DropTargetEvent event) {
				if (fullSearchActivated() && textTransfer.isSupportedType(event.currentDataType)) {
					String ids = (String) event.data;
					StringTokenizer st = new StringTokenizer(ids, "\n"); //$NON-NLS-1$
					if (st.hasMoreTokens()) {
						String id = st.nextToken();
						IDbManager dbManager = Core.getCore().getDbManager();
						AssetImpl asset = dbManager.obtainAsset(id);
						if (asset != null) {
							QueryOptions queryOptions = UiActivator.getDefault().getQueryOptions();
							int method = queryOptions.getMethod();
							int validMethod = -1;
							Set<String> cbirAlgorithms = CoreActivator.getDefault().getCbirAlgorithms();
							Algorithm algorithm = Core.getCore().getDbFactory().getLireService(true)
									.getAlgorithmById(method);
							if (algorithm != null && cbirAlgorithms.contains(algorithm.getName()))
								validMethod = method;
							if (validMethod < 0 && configureSearch())
								validMethod = queryOptions.getMethod();
							if (validMethod >= 0) {
								SimilarityOptions_typeImpl newOptions = new SimilarityOptions_typeImpl(validMethod,
										queryOptions.getMaxHits(), queryOptions.getScore() / 100f, 0, 1, 10, 30, id,
										queryOptions.getKeywordWeight());
								String[] keywords = asset.getKeyword();
								if (queryOptions.getKeywordWeight() > 0 && keywords != null && keywords.length > 0)
									newOptions.setKeywords(Arrays.copyOf(keywords, keywords.length));
								else
									newOptions.setKeywordWeight(0);
								if (queryOptions.getKeywordWeight() < 100)
									newOptions.setPngImage(asset.getJpegThumbnail());
								SmartCollectionImpl collection = new SmartCollectionImpl(
										NLS.bind(Messages.QuickFindControl_images_similar, asset.getName()), false,
										false, true, queryOptions.isNetworked(), null, 0, null, 0, null,
										Constants.INHERIT_LABEL, null, 0, 1, null);
								collection.addCriterion(new CriterionImpl(ICollectionProcessor.SIMILARITY, null,
										newOptions, null, queryOptions.getScore(), false));
								Ui.getUi().getNavigationHistory(getWorkbenchWindow())
										.postSelection(new StructuredSelection(collection));
							}
						}
					}
				}
			}
		});
	}

	private boolean configureSearch() {
		Point displayLocation = inputField.toDisplay(inputField.getLocation());
		displayLocation.y += 15;
		displayLocation.x += 20;
		return Core.getCore().getDbFactory().getLireService(true).showConfigureSearch(this, displayLocation);
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// do nothing
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// do nothing
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.character == SWT.CR)
			performSearch(false);
	}

	@Override
	public void dispose() {
		if (textsearchIcon != null)
			textsearchIcon.dispose();
		super.dispose();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		if (Shell.class.equals(adapter))
			return inputField.getShell();
		if (IWorkbenchWindow.class.equals(adapter))
			return getWorkbenchWindow();
		return null;
	}
}
