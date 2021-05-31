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
 * (c) 2014-2917 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.views;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.cloudio.ICloudLabelProvider;
import org.eclipse.zest.cloudio.TagCloud;
import org.eclipse.zest.cloudio.TagCloudViewer;
import org.eclipse.zest.cloudio.Word;

import com.bdaum.zoom.cat.model.TextSearchOptions_typeImpl;
import com.bdaum.zoom.cat.model.group.CriterionImpl;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.db.ICollectionProcessor;
import com.bdaum.zoom.core.db.IDbFactory;
import com.bdaum.zoom.core.db.IDbListener;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.QueryOptions;
import com.bdaum.zoom.core.internal.ScoredString;
import com.bdaum.zoom.core.internal.lucene.ILuceneService;
import com.bdaum.zoom.core.internal.peer.IPeerService;
import com.bdaum.zoom.ui.IZoomCommandIds;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;

@SuppressWarnings("restriction")
public class TagCloudView extends ViewPart
		implements IDbListener, ISelectionChangedListener, Listener, IMenuListener, IPartListener, MouseListener {

	public class SchemeAction extends Action {

		private final int id;

		public SchemeAction(int id, String text, ImageDescriptor imageDescriptor) {
			super(text, IAction.AS_RADIO_BUTTON);
			this.id = id;
			setImageDescriptor(imageDescriptor);
		}

		@Override
		public void run() {
			scheme = id;
			disposeColors();
			refresh(false);
		}

	}

	public class SizeAction extends Action {

		private final int size;

		public SizeAction(int size, String text) {
			super(text, IAction.AS_RADIO_BUTTON);
			this.size = size;
		}

		@Override
		public void run() {
			viewer.setMaxWords(occurrences = size);
			refresh(true);
		}
	}

	private static final String NETWORKED = "networked"; //$NON-NLS-1$
	private static final String OCCURRENCES = "occurrences"; //$NON-NLS-1$
	private static final String SCHEME = "scheme"; //$NON-NLS-1$
	private static final String FILTER = "filter"; //$NON-NLS-1$

	private static final List<ScoredString> EMPTYSCORES = Collections.emptyList();
	private static final List<ScoredString> NOINDEX = Collections.singletonList(new ScoredString(Messages.getString("TagCloudView.no_index"), 100)); //$NON-NLS-1$

	private static final int ALL = 0;
	private static final int SPRING = 1;
	private static final int SUMMER = 2;
	private static final int AUTUMN = 3;
	private static final int WINTER = 4;
	private static final RGB[][] schemeRgbs = new RGB[][] {
			new RGB[] { new RGB(200, 0, 0), new RGB(0, 255, 0), new RGB(96, 128, 255), new RGB(255, 255, 0),
					new RGB(0, 255, 255), new RGB(255, 0, 255), new RGB(255, 255, 255), new RGB(127, 0, 0),
					new RGB(0, 127, 0), new RGB(127, 127, 255), new RGB(127, 127, 0), new RGB(0, 127, 127),
					new RGB(127, 0, 127), new RGB(127, 127, 127) },
			new RGB[] { new RGB(135, 255, 124), new RGB(227, 255, 72), new RGB(45, 213, 87), new RGB(226, 255, 87),
					new RGB(198, 255, 196), new RGB(249, 255, 214), new RGB(255, 255, 255) },
			new RGB[] { new RGB(212, 105, 25), new RGB(212, 7, 57), new RGB(212, 20, 118), new RGB(212, 142, 167),
					new RGB(212, 86, 184), new RGB(200, 0, 0), new RGB(209, 175, 255) },
			new RGB[] { new RGB(255, 137, 124), new RGB(204, 110, 99), new RGB(224, 96, 128), new RGB(224, 186, 96),
					new RGB(118, 115, 37), new RGB(226, 161, 165), new RGB(234, 143, 116) },
			new RGB[] { new RGB(96, 128, 255), new RGB(173, 252, 255), new RGB(198, 210, 255), new RGB(193, 212, 255),
					new RGB(237, 244, 255), new RGB(200, 200, 200), new RGB(255, 255, 255) } };
	public static final String ID = "com.bdaum.zoom.ui.TagCloudView"; //$NON-NLS-1$

	private TagCloudViewer viewer;
	private TagCloud cloud;
	private List<ScoredString> scoredStrings;
	private IDbManager dbManager;
	private SchemeAction springAction;
	private SchemeAction summerAction;
	private SchemeAction autumnAction;
	private SchemeAction winterAction;
	private SchemeAction allAction;
	private int scheme = 0;
	private int occurrences = 50;
	private SizeAction s50Action;
	private SizeAction s100Action;
	private SizeAction s200Action;
	private Color[] colors;
	private FontData[][] dialogFontDatas = new FontData[][] { JFaceResources.getBannerFont().getFontData(),
			JFaceResources.getDefaultFont().getFontData(), JFaceResources.getDialogFont().getFontData(),
			JFaceResources.getHeaderFont().getFontData(), JFaceResources.getTextFont().getFontData() };
	private FontData[][] viewFontDatas = new FontData[dialogFontDatas.length][];
	private IStructuredSelection selection = StructuredSelection.EMPTY;
	private Action searchAction;
	private Action networkAction;
	private Action refreshAction;
	private boolean networked;
	private MenuManager contextMenuMgr;
	protected ScoredString mouseData;
	protected int mouseButton;
	private Action selectAction;
	private Action deselectAction;
	protected Set<String> filter = new HashSet<String>();
	private Action filterAction;
	private Action resetAction;
	private SizeAction s500Action;
	private Action searchOneAction;
	private Action searchAllAction;
	private double logMinScores;
	private double deltaLog;
	protected int stateMask;

	public TagCloudView() {
		dbManager = Core.getCore().getDbManager();
		copyFontDatas();
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		if (memento != null) {
			Integer i = memento.getInteger(SCHEME);
			scheme = i == null ? 0 : i.intValue();
			i = memento.getInteger(OCCURRENCES);
			occurrences = i == null ? 50 : i.intValue();
			Boolean b = memento.getBoolean(NETWORKED);
			networked = b != null && b && Core.getCore().isNetworked();
			String fs = memento.getString(FILTER);
			if (fs != null) {
				StringTokenizer st = new StringTokenizer(fs);
				while (st.hasMoreTokens())
					filter.add(st.nextToken());
			}
		}
	}

	private void copyFontDatas() {
		for (int i = 0; i < dialogFontDatas.length; i++) {
			FontData[] fd = dialogFontDatas[i].clone();
			for (int j = 0; j < fd.length; j++) {
				FontData fontData = fd[j];
				fd[j] = new FontData(fontData.getName(), fontData.getHeight(), fontData.getStyle());
			}
			viewFontDatas[i] = fd;
		}
	}

	public void disposeColors() {
		if (colors != null) {
			for (Color color : colors)
				color.dispose();
			colors = null;
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(null);
		cloud = new TagCloud(parent, SWT.NONE);
		cloud.setBounds(0, 0, 800, 400);
		viewer = new TagCloudViewer(cloud);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), HelpContextIds.TAGCLOUD_VIEW);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setMaxWords(occurrences);
		viewer.setLabelProvider(new ICloudLabelProvider() {

			int counter = 0;

			public void removeListener(ILabelProviderListener listener) {
				// do nothing
			}

			public boolean isLabelProperty(Object element, String property) {
				return false;
			}

			public void dispose() {
				// do nothing
			}

			public void addListener(ILabelProviderListener listener) {
				// do nothing
			}

			public double getWeight(Object element) {
				if (element instanceof ScoredString)
					return (Math.log(((ScoredString) element).getScore()) - logMinScores) / deltaLog;
				return 0;
			}

			public String getToolTip(Object element) {
				if (element instanceof ScoredString)
					return NLS.bind(Messages.getString("TagCloudView.x_occurrences"), //$NON-NLS-1$
							((ScoredString) element).getScore());
				return null;
			}

			public String getLabel(Object element) {
				if (element instanceof ScoredString)
					return ((ScoredString) element).getString();
				return String.valueOf(element);
			}

			public FontData[] getFontData(Object element) {
				FontData[] fd = viewFontDatas[counter % viewFontDatas.length].clone();
				int size = (int) (cloud.getMinFontSize() + getWeight(element) * cloud.getMaxFontSize());
				for (int i = 0; i < fd.length; i++) {
					FontData data = fd[i];
					fd[i] = new FontData(data.getName(), size, data.getStyle());
				}
				return fd;
			}

			public Color getColor(Object element) {
				Color[] colors = getColors();
				return colors[counter++ % colors.length];
			}

			public float getAngle(Object element) {
				if (element instanceof ScoredString) {
					String label = ((ScoredString) element).getString();
					if (!label.isEmpty() && Character.isDigit(label.charAt(0)))
						return -90;
					if (label.length() <= 3)
						return -45;
				}
				return 0;
			}
		});
		cloud.addMouseListener(this);
		viewer.addSelectionChangedListener(this);
		Core.getCore().getDbFactory().addDbListener(this);
		refresh(false);
		makeActions(getViewSite().getActionBars());
		hookContextMenu();
		contributeToActionBars();
		updateActions();
		parent.addListener(SWT.Resize, this);
		getSite().getPage().addPartListener(this);
	}

	public void partOpened(IWorkbenchPart part) {
		// do nothing
	}

	public void partDeactivated(IWorkbenchPart part) {
		// do nothing
	}

	public void partClosed(IWorkbenchPart part) {
		// do nothing
	}

	public void partBroughtToTop(IWorkbenchPart part) {
		// do nothing
	}

	public void partActivated(IWorkbenchPart part) {
		if (part == TagCloudView.this)
			viewer.setSelection(selection);
	}

	public void selectionChanged(SelectionChangedEvent event) {
		if (mouseButton == 1) {
			selection = (IStructuredSelection) event.getSelection();
			updateActions();
		} else
			viewer.setSelection(selection);
	}

	@Override
	public void dispose() {
		disposeColors();
		super.dispose();
	}

	protected Color[] getColors() {
		if (colors == null) {
			RGB[] rgbs = schemeRgbs[scheme];
			colors = new Color[rgbs.length];
			Display display = viewer.getControl().getDisplay();
			for (int i = 0; i < rgbs.length; i++)
				colors[i] = new Color(display, rgbs[i]);
		}
		return colors;
	}

	private void refresh(boolean cold) {
		if (cold)
			scoredStrings = null;
		if (!viewer.getControl().isDisposed())
			viewer.getControl().getDisplay().asyncExec(() -> {
				if (!viewer.getControl().isDisposed()) {
					final List<ScoredString> locScoredStrings = getScoredStrings();
					if (locScoredStrings != null)
						BusyIndicator.showWhile(viewer.getControl().getDisplay(), () -> {
							if (!viewer.getControl().isDisposed()) {
								viewer.setInput(locScoredStrings);
								viewer.setSelection(selection);
							}
						});
				}
			});

	}

	private void updateActions() {
		searchAction.setEnabled(!selection.isEmpty());
		resetAction.setEnabled(!filter.isEmpty());
		if (mouseData != null) {
			String word = mouseData.getString();
			filterAction.setText(NLS.bind(Messages.getString("TagCloudView.filter"), //$NON-NLS-1$
					word));
			selectAction.setText(NLS.bind(Messages.getString("TagCloudView.select"), //$NON-NLS-1$
					word));
			deselectAction.setText(NLS.bind(Messages.getString("TagCloudView.deselect"), //$NON-NLS-1$
					word));
			searchOneAction.setText(NLS.bind(Messages.getString("TagCloudView.search_for"), //$NON-NLS-1$
					word));
			boolean selected = false;
			StringBuilder sb = new StringBuilder();
			for (Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
				if (sb.length() > 0)
					sb.append(" AND "); //$NON-NLS-1$
				ScoredString ss = (ScoredString) iterator.next();
				if (ss.equals(mouseData))
					selected = true;
				sb.append(ss.getString());
				if (iterator.hasNext() && sb.length() > 40) {
					sb.append(" AND ..."); //$NON-NLS-1$
					break;
				}
			}
			if (!selected) {
				if (sb.length() > 0)
					sb.append(" AND "); //$NON-NLS-1$
				sb.append(mouseData.getString());
			}
			searchAllAction.setText(NLS.bind(Messages.getString("TagCloudView.search_for"), //$NON-NLS-1$
					sb.toString()));
		}
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	protected void fillContextMenu(IMenuManager manager) {
		updateActions();
		if (mouseData != null) {
			manager.add(selection.toList().contains(mouseData) ? deselectAction : selectAction);
			manager.add(new Separator());
			manager.add(searchOneAction);
			if (!selection.isEmpty() && !(selection.size() == 1 && selection.getFirstElement().equals(mouseData)))
				manager.add(searchAllAction);
			manager.add(new Separator());
			manager.add(filterAction);
		}
		if (!filter.isEmpty())
			manager.add(resetAction);
	}

	protected void hookContextMenu() {
		if (contextMenuMgr == null) {
			contextMenuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
			contextMenuMgr.setRemoveAllWhenShown(true);
			contextMenuMgr.addMenuListener(this);
			viewer.getControl().setMenu(contextMenuMgr.createContextMenu(viewer.getControl()));
			getSite().registerContextMenu(contextMenuMgr, viewer);
		}
	}

	public void menuAboutToShow(IMenuManager manager) {
		fillContextMenu(manager);
		manager.updateAll(true);
	}

	private void fillLocalPullDown(IMenuManager manager) {
		MenuManager schemeManager = new MenuManager(Messages.getString("TagCloudView.color_scheme")); //$NON-NLS-1$
		manager.add(schemeManager);
		schemeManager.add(springAction);
		schemeManager.add(summerAction);
		schemeManager.add(autumnAction);
		schemeManager.add(winterAction);
		schemeManager.add(allAction);
		switch (scheme) {
		case ALL:
			allAction.setChecked(true);
			break;
		case SPRING:
			springAction.setChecked(true);
			break;
		case SUMMER:
			summerAction.setChecked(true);
			break;
		case AUTUMN:
			autumnAction.setChecked(true);
			break;
		case WINTER:
			winterAction.setChecked(true);
			break;
		}
		// manager.add(fontAction);
		MenuManager sizeManager = new MenuManager(Messages.getString("TagCloudView.max_words")); //$NON-NLS-1$
		manager.add(sizeManager);
		sizeManager.add(s50Action);
		sizeManager.add(s100Action);
		sizeManager.add(s200Action);
		sizeManager.add(s500Action);
		switch (occurrences) {
		case 50:
			s50Action.setChecked(true);
			break;
		case 100:
			s100Action.setChecked(true);
			break;
		case 200:
			s200Action.setChecked(true);
			break;
		case 500:
			s500Action.setChecked(true);
			break;
		}
		manager.add(new Separator());
		if (networkAction != null)
			manager.add(networkAction);
		manager.add(searchAction);
		manager.add(refreshAction);
		manager.add(resetAction);
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(resetAction);
		if (networkAction != null)
			manager.add(networkAction);
		manager.add(searchAction);
		manager.add(refreshAction);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void makeActions(IActionBars actionBars) {
		springAction = new SchemeAction(SPRING, Messages.getString("TagCloudView.spring"), //$NON-NLS-1$
				Icons.spring.getDescriptor());
		summerAction = new SchemeAction(SUMMER, Messages.getString("TagCloudView.summer"), //$NON-NLS-1$
				Icons.summer.getDescriptor());
		autumnAction = new SchemeAction(AUTUMN, Messages.getString("TagCloudView.autumn"), //$NON-NLS-1$
				Icons.autumn.getDescriptor());
		winterAction = new SchemeAction(WINTER, Messages.getString("TagCloudView.winter"), //$NON-NLS-1$
				Icons.winter.getDescriptor());
		allAction = new SchemeAction(ALL, Messages.getString("TagCloudView.seasons"), //$NON-NLS-1$
				Icons.seasons.getDescriptor());
		s50Action = new SizeAction(50, "50"); //$NON-NLS-1$
		s100Action = new SizeAction(100, "100"); //$NON-NLS-1$
		s200Action = new SizeAction(200, "200"); //$NON-NLS-1$
		s500Action = new SizeAction(500, "500"); //$NON-NLS-1$

		searchAction = new Action(Messages.getString("TagCloudView.start_search"), Icons.textSearch.getDescriptor()) { //$NON-NLS-1$
			@Override
			public void runWithEvent(Event event) {
				@SuppressWarnings("unchecked")
				List<ScoredString> tags = selection.toList();
				IDbFactory dbFactory = Core.getCore().getDbFactory();
				if ((event.stateMask & SWT.SHIFT) != 0 && dbFactory.getLireServiceVersion() >= 0)
					dbFactory.getLireService(true).performQuery(constructQuery(tags), getSite(),
							ICollectionProcessor.TEXTSEARCH);
				else
					startSearch(tags);
			}

		};
		searchAction.setToolTipText(Messages.getString("TagCloudView.search_tooltip")); //$NON-NLS-1$
		if (Core.getCore().isNetworked()) {
			networkAction = new Action(Messages.getString("TagCloudView.search_network"), //$NON-NLS-1$
					IAction.AS_CHECK_BOX) {
				@Override
				public void run() {
					networked = isChecked();
					updateNetworkAction();
					refresh(true);
				}
			};
			networkAction.setChecked(networked);
			updateNetworkAction();
		}
		refreshAction = new Action(Messages.getString("TagCloudView.refresh"), Icons.refresh.getDescriptor()) { //$NON-NLS-1$
			@Override
			public void run() {
				refresh(true);
			}
		};
		registerCommand(refreshAction, IZoomCommandIds.RefreshCommand);
		refreshAction.setToolTipText(Messages.getString("TagCloudView.refresh_tool_tip")); //$NON-NLS-1$
		selectAction = new Action(Messages.getString("TagCloudView.select"), Icons.signed_yes.getDescriptor()) { //$NON-NLS-1$
			@Override
			public void run() {
				if (mouseData != null) {
					@SuppressWarnings("unchecked")
					List<ScoredString> list = new ArrayList<ScoredString>(selection.toList());
					list.add(mouseData);
					viewer.setSelection(selection = new StructuredSelection(list));
					updateActions();
				}
			}
		};
		selectAction.setToolTipText(Messages.getString("TagCloudView.select_tooltip")); //$NON-NLS-1$
		deselectAction = new Action(Messages.getString("TagCloudView.deselect"), Icons.signed_no.getDescriptor()) { //$NON-NLS-1$
			@Override
			public void run() {
				if (mouseData != null) {
					@SuppressWarnings("unchecked")
					List<ScoredString> list = new ArrayList<ScoredString>(selection.toList());
					list.remove(mouseData);
					viewer.setSelection(selection = new StructuredSelection(list));
					updateActions();
				}
			}
		};
		deselectAction.setToolTipText(Messages.getString("TagCloudView.deselect_tooltip")); //$NON-NLS-1$
		filterAction = new Action(Messages.getString("TagCloudView.filter"), Icons.filter.getDescriptor()) { //$NON-NLS-1$
			@Override
			public void run() {
				if (mouseData != null) {
					filter.add(mouseData.getString());
					refresh(true);
					updateActions();
				}
			}
		};
		filterAction.setToolTipText(Messages.getString("TagCloudView.filter_tooltip")); //$NON-NLS-1$
		resetAction = new Action(Messages.getString("TagCloudView.reset"), Icons.reset.getDescriptor()) { //$NON-NLS-1$
			@Override
			public void run() {
				filter.clear();
				refresh(true);
				updateActions();
			}
		};
		resetAction.setToolTipText(Messages.getString("TagCloudView.reset_tooltip")); //$NON-NLS-1$
		searchOneAction = new Action(Messages.getString("TagCloudView.search_for"), //$NON-NLS-1$
				Icons.textsearchOne.getDescriptor()) {
			@Override
			public void run() {
				if (mouseData != null)
					startSearch(Collections.singletonList(mouseData));
			}
		};
		searchOneAction.setToolTipText(Messages.getString("TagCloudView.search_one_tooltip")); //$NON-NLS-1$
		searchAllAction = new Action(Messages.getString("TagCloudView.search_for"), //$NON-NLS-1$
				Icons.textsearchMany.getDescriptor()) {
			@Override
			public void run() {
				if (mouseData != null) {
					@SuppressWarnings("unchecked")
					ArrayList<ScoredString> tags = new ArrayList<ScoredString>(selection.toList());
					if (!tags.contains(mouseData))
						tags.add(mouseData);
					startSearch(tags);
				}
			}
		};
		searchAllAction.setToolTipText(Messages.getString("TagCloudView.search_all_tooltip")); //$NON-NLS-1$
	}

	protected void registerCommand(IAction action, String commandId) {
		IHandlerService service = getSite().getService(IHandlerService.class);
		if (service != null) {
			action.setActionDefinitionId(commandId);
			service.activateHandler(commandId, new ActionHandler(action));
		}
	}

	private void startSearch(List<ScoredString> tags) {
		if (!tags.isEmpty()) {
			String s = constructQuery(tags);
			QueryOptions queryOptions = UiActivator.getDefault().getQueryOptions();
			int score = queryOptions.getScore();
			int maxNumber = queryOptions.getMaxHits();
			SmartCollectionImpl sm = new SmartCollectionImpl(
					s + NLS.bind(Messages.getString("TagCloudView.maxmin"), maxNumber, score), //$NON-NLS-1$
					false, false, true, networkAction != null && networked, null, 0, null, 0, null,
					Constants.INHERIT_LABEL, null, 0, 1, null);
			sm.addCriterion(new CriterionImpl(ICollectionProcessor.TEXTSEARCH, null,
					new TextSearchOptions_typeImpl(s, maxNumber, score / 100f), null, score, false));
			Ui.getUi().getNavigationHistory(getSite().getWorkbenchWindow()).postSelection(new StructuredSelection(sm));
			for (IViewReference ref : getSite().getPage().getViewReferences())
				if (ref.getView(false) == this) {
					getSite().getPage().setPartState(ref, IWorkbenchPage.STATE_MINIMIZED);
					break;
				}
		}
	}

	private static String constructQuery(List<ScoredString> tags) {
		StringBuilder sb = new StringBuilder();
		for (ScoredString ss : tags) {
			if (sb.length() > 0)
				sb.append(" AND "); //$NON-NLS-1$
			sb.append(ss.getString());
		}
		return sb.toString();
	}

	@Override
	public void setFocus() {
		cloud.setFocus();
	}

	private List<ScoredString> getScoredStrings() {
		if (scoredStrings == null)
			BusyIndicator.showWhile(viewer.getControl().getDisplay(), () -> doGetScoredStrings());
		return scoredStrings;
	}

	private void doGetScoredStrings() {
		String ticket = null;
		IPeerService peerService = networkAction != null && networked ? Core.getCore().getDbFactory().getPeerService()
				: null;
		int size = occurrences + filter.size();
		try {
			if (peerService != null)
				ticket = peerService.askForTagCloud(size);
			List<ScoredString> result;
			File indexPath = dbManager.getIndexPath();
			if (indexPath == null || dbManager.getMeta(true).getNoIndex())
				result = ticket == null ? NOINDEX : EMPTYSCORES;
			else {
				ILuceneService luceneService = Core.getCore().getDbFactory().getLuceneService();
				try {
					result = luceneService.listTags(indexPath, size);
					if (result == null)
						result = EMPTYSCORES;
				} catch (IOException e) {
					result = EMPTYSCORES;
				}
			}
			if (ticket != null) {
				Map<String, List<ScoredString>> foreignCloud = peerService.getTagCloud(ticket);
				if (foreignCloud != null) {
					int initialCapacity = size * 2;
					List<ScoredString> combined = new ArrayList<ScoredString>(initialCapacity);
					Map<String, ScoredString> tagMap = new HashMap<String, ScoredString>(initialCapacity * 3 / 2);
					if (result != null)
						for (ScoredString scoredString : result)
							tagMap.put(scoredString.getString(), scoredString);
					for (Map.Entry<String, List<ScoredString>> entry : foreignCloud.entrySet()) {
						String key = entry.getKey();
						ScoredString localString = tagMap.remove(key);
						int score = localString == null ? 0 : localString.getScore();
						for (ScoredString scoredString : entry.getValue())
							score += scoredString.getScore();
						combined.add(new ScoredString(key, score));
					}
					for (ScoredString scoredString : tagMap.values())
						combined.add(scoredString);
					result = combined;
					Collections.sort(result);
				}
			}
			if (!filter.isEmpty()) {
				Iterator<ScoredString> iterator = result.iterator();
				while (iterator.hasNext()) {
					ScoredString scoredString = iterator.next();
					if (filter.contains(scoredString.getString()))
						iterator.remove();
				}
			}
			if (result.size() > occurrences)
				result = result.subList(0, occurrences);
			scoredStrings = result;
		} finally {
			if (ticket != null)
				peerService.discardTask(ticket);
		}
		if (scoredStrings.isEmpty()) {
			String msg;
			msg = networked ? Messages.getString("TagCloudView.no_tags_in_network") : //$NON-NLS-1$
					Core.getCore().getDbManager().getMeta(true).getNoIndex()
							? Messages.getString("TagCloudView.indexing_disabled") //$NON-NLS-1$
							: Messages.getString("TagCloudView.no_tags_in_catalog"); //$NON-NLS-1$
			scoredStrings.add(new ScoredString(msg, 1000));
		}
		int maxScores = 0;
		int minScores = Integer.MAX_VALUE;
		for (ScoredString ss : scoredStrings) {
			minScores = Math.min(minScores, ss.getScore());
			maxScores = Math.max(maxScores, ss.getScore());
		}
		minScores = (1 + minScores) / 2;
		logMinScores = Math.log(minScores);
		deltaLog = Math.log(maxScores) - logMinScores;
		if (deltaLog == 0)
			deltaLog = 1;
	}

	public IDbManager databaseAboutToOpen(String filename, boolean primary) {
		return null;
	}

	public void databaseOpened(IDbManager manager, boolean primary) {
		if (primary) {
			dbManager = manager;
			refresh(true);
		}
	}

	public boolean databaseAboutToClose(IDbManager manager) {
		return true;
	}

	public void databaseClosed(IDbManager manager, int mode) {
		if (manager == dbManager && mode != IDbListener.EMERGENCY) {
			dbManager = Core.getCore().getDbManager();
			scoredStrings = EMPTYSCORES;
			refresh(false);
		}
	}

	private void updateNetworkAction() {
		if (networked) {
			networkAction.setImageDescriptor(Icons.network.getDescriptor());
			networkAction.setToolTipText(Messages.getString("TagCloudView.search_network_tooltip")); //$NON-NLS-1$
		} else {
			networkAction.setImageDescriptor(Icons.local.getDescriptor());
			networkAction.setToolTipText(Messages.getString("TagCloudView.local_cloud")); //$NON-NLS-1$
		}
	}

	@Override
	public void saveState(IMemento memento) {
		super.saveState(memento);
		if (networkAction != null)
			memento.putBoolean(NETWORKED, networked);
		memento.putInteger(SCHEME, scheme);
		memento.putInteger(OCCURRENCES, occurrences);
		memento.putString(FILTER, Core.toStringList(filter, ' '));
	}

	public void handleEvent(Event e) {
		Rectangle clientArea = ((Composite) e.widget).getClientArea();
		int size = Math.min(clientArea.width, clientArea.height);
		int xoff = size < clientArea.width ? (clientArea.width - size) / 2 : 0;
		int yoff = size < clientArea.height ? (clientArea.height - size) / 2 : 0;
		cloud.setBounds(clientArea.x + xoff, clientArea.y + yoff, size, size);
		if (scoredStrings == null)
			refresh(false);
		else
			cloud.layoutCloud(null, true);
	}

	@Override
	public void mouseDoubleClick(MouseEvent e) {
		((stateMask & SWT.CTRL) != 0 ? searchAllAction : searchOneAction).run();
	}

	@Override
	public void mouseDown(MouseEvent e) {
		mouseData = (ScoredString) (e.data instanceof Word ? ((Word) e.data).data : null);
		mouseButton = e.button;
		stateMask = e.stateMask;
		updateActions();
	}

	@Override
	public void mouseUp(MouseEvent e) {
		// do nothing
	}

}
