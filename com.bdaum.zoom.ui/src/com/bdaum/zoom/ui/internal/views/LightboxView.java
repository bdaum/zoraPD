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
 * (c) 2009-2017 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.ui.internal.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.nebula.widgets.gallery.Gallery;
import org.eclipse.nebula.widgets.gallery.GalleryItem;
import org.eclipse.nebula.widgets.gallery.NoGroupRenderer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISizeProvider;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.Group;
import com.bdaum.zoom.cat.model.group.GroupImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.SortCriterion;
import com.bdaum.zoom.cat.model.group.SortCriterionImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IAssetProvider;
import com.bdaum.zoom.core.ISpellCheckingService;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.css.internal.CssActivator;
import com.bdaum.zoom.fileMonitor.internal.filefilter.WildCardFilter;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.RenameAssetOperation;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.INavigationHistory;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.dialogs.CollapsePatternDialog;
import com.bdaum.zoom.ui.internal.widgets.CheckedText;

@SuppressWarnings("restriction")
public class LightboxView extends AbstractLightboxView implements Listener {

	public static final String ID = "com.bdaum.zoom.ui.views.LightboxView"; //$NON-NLS-1$
	public static final String HSTRIPVIEW = "com.bdaum.zoom.ui.views.HStripView"; //$NON-NLS-1$
	public static final String VSTRIPVIEW = "com.bdaum.zoom.ui.views.VStripView"; //$NON-NLS-1$
	public static final String GRID = "grid"; //$NON-NLS-1$
	public static final String VSTRIP = "vstrip"; //$NON-NLS-1$
	public static final String HSTRIP = "hstrip"; //$NON-NLS-1$

	protected static final int TRIM = 5;
	private static final String COLLAPSEPATTERN = "collapsePattern"; //$NON-NLS-1$
	private static final String DEFAULTPATTERN = "/*.*"; //$NON-NLS-1$
	private static final SortCriterionImpl URISORT = new SortCriterionImpl(QueryField.URI.getKey(), null, false);
	private static final String FOLDING = "folding"; //$NON-NLS-1$
	private String layout = GRID;
	private boolean isStrip;
	private Map<Asset, Integer> galleryMap = new HashMap<Asset, Integer>();
	protected WildCardFilter collapseFilter;

	private CheckedText titleInput;

	private boolean titleInputValid;
	private SelectionAdapter closeTitleAreaListener;
	protected IAction toggleCollapseAction;
	private Font smallFont;
	private int showLabel;
	private String labelTemplate;
	private int currentFontsize;

	@Override
	public void setInitializationData(IConfigurationElement cfig, String propertyName, Object data) {
		super.setInitializationData(cfig, propertyName, data);
		if (data instanceof String) {
			layout = (String) data;
			isStrip = (HSTRIP.equals(layout) || VSTRIP.equals(layout));
		}
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		String pattern = null;
		if (memento != null) {
			Boolean fold = memento.getBoolean(FOLDING);
			if (fold != null)
				folding = fold.booleanValue();
			pattern = memento.getString(COLLAPSEPATTERN);
		}
		collapseFilter = new WildCardFilter(pattern == null ? DEFAULTPATTERN : pattern);
	}

	@Override
	public void saveState(IMemento memento) {
		if (memento != null) {
			memento.putString(COLLAPSEPATTERN, collapseFilter.toString());
			memento.putBoolean(FOLDING, folding);
		}
		super.saveState(memento);
	}

	@Override
	public void createPartControl(Composite parent) {
		// Create gallery
		setPreferences();
		final int orientation = HSTRIP.equals(layout) ? SWT.H_SCROLL : SWT.V_SCROLL;
		gallery = new Gallery(parent, orientation | SWT.VIRTUAL | SWT.MULTI);
		gallery.setBackground(gallery.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		gallery.setHigherQualityDelay(300);
		gallery.setLowQualityOnUserAction(true);
		setAppStarting(gallery);

		if (isStrip)
			gallery.addControlListener(new ControlAdapter() {
				@Override
				public void controlResized(ControlEvent e) {
					Rectangle clientArea = gallery.getClientArea();
					thumbsize = (orientation == SWT.H_SCROLL) ? clientArea.height - TRIM : clientArea.width - TRIM;
					fireSizeChanged();
				}
			});
		setHelp(orientation);
		// Renderers
		groupRenderer = new NoGroupRenderer(); // DefaultGalleryGroupRenderer();
		groupRenderer.setItemSize(thumbsize, thumbsize);
		groupRenderer.setMinMargin(3);
		itemRenderer = new LightboxGalleryItemRenderer(gallery);
		applyStyle(gallery);
		gallery.setGroupRenderer(groupRenderer);
		// Custom draw
		gallery.setItemRenderer(null);
		addGalleryPaintListener();
		gallery.addListener(SWT.SetData, this);
		gallery.setItemCount(1);
		// Actions
		makeActions(getViewSite().getActionBars());
		installListeners(parent);
		gallery.addSelectionListener(this);
		installInfrastructure(!isStrip, 1000);
		if (folding && !canBeCollapsed())
			setUriSort();
	}

	protected void setHelp(final int orientation) {
		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem()
				.setHelp(gallery, isStrip
						? ((orientation == SWT.H_SCROLL) ? HelpContextIds.HSTRIP_VIEW : HelpContextIds.VSTRIP_VIEW)
						: HelpContextIds.LIGHTBOX_VIEW);
	}

	@Override
	protected void makeActions(IActionBars bars) {
		configureCollapseAction = new Action(Messages.getString("LightboxView.configure_folding")) { //$NON-NLS-1$
			@Override
			public void run() {
				if (configureCollapse() && folding) {
					expandedSet.clear();
					refresh();
				}
			}
		};
		configureCollapseAction.setToolTipText(Messages.getString("LightboxView.configure_folding_tooltip")); //$NON-NLS-1$
		collapseAction = new Action(Messages.getString("LightboxView.collapsed"), IAction.AS_CHECK_BOX) { //$NON-NLS-1$

			@Override
			public void runWithEvent(Event event) {
				if ((event.stateMask & SWT.CTRL) != 0) {
					if (configureCollapse()) {
						if (!canBeCollapsed())
							setUriSort();
						setChecked(folding = true);
						expandedSet.clear();
						refresh();
					}
				} else
					run();
			}

			@Override
			public void run() {
				if (!folding && !canBeCollapsed())
					setUriSort();
				setChecked(folding = !folding);
				if (!folding)
					resetUriSort();
				expandedSet.clear();
				refresh();
			}
		};
		collapseAction.setImageDescriptor(Icons.collapsed.getDescriptor());
		collapseAction.setToolTipText(
				NLS.bind(Messages.getString("LightboxView.fold_unfolds_images"), collapseFilter.toString())); //$NON-NLS-1$
		collapseAction.setChecked(folding);
		toggleCollapseAction = new Action(Messages.getString("LightboxView.toggle_collapsed"), Icons.collapsed //$NON-NLS-1$
				.getDescriptor()) {

			@Override
			public void run() {
				GalleryItem[] items = gallery.getSelection();
				boolean expand = false;
				for (GalleryItem item : items)
					if (item.getData(CARD) != null) {
						Asset asset = (Asset) item.getData(ASSET);
						if (asset != null) {
							expandedSet.add(asset);
							expand = true;
						}
					}
				if (!expand)
					for (GalleryItem item : items) {
						Asset asset = (Asset) item.getData(ASSET);
						if (asset != null)
							expandedSet.remove(asset);
					}
				refresh();
			}
		};
		toggleCollapseAction.setToolTipText(Messages.getString("LightboxView.expand_collapsed_or")); //$NON-NLS-1$
		if (!isStrip)
			createScaleContributionItem(MINTHUMBSIZE, MAXTHUMBSIZE);
		super.makeActions(bars);
	}

	protected void resetUriSort() {
		getNavigationHistory().sortChanged(null);
	}

	protected void setUriSort() {
		getNavigationHistory().sortChanged(URISORT);
	}

	protected boolean configureCollapse() {
		CollapsePatternDialog dialog = new CollapsePatternDialog(getSite().getShell(),
				HelpContextIds.COLLAPSEPATTERN_DIALOG, collapseFilter.toString());
		if (dialog.open() == Dialog.OK) {
			collapseFilter = new WildCardFilter(dialog.getPattern());
			updateCollapseAction();
			return true;
		}
		return false;
	}

	@Override
	public void updateActions() {
		super.updateActions();
		updateCollapseAction();
	}

	protected void updateCollapseAction() {
		String msg = NLS.bind(Messages.getString("LightboxView.fold_unfolds_images"), collapseFilter.toString(), //$NON-NLS-1$
				folding ? Messages.getString("LightboxView.ON") : Messages.getString("LightboxView.OFF")); //$NON-NLS-1$ //$NON-NLS-2$
		if (canBeCollapsed())
			msg += Messages.getString("LightboxView.causes_resort"); //$NON-NLS-1$
		collapseAction.setToolTipText(msg);
	}

	private boolean canBeCollapsed() {
		SortCriterion sort = getNavigationHistory().getCustomSort();
		if (sort == null) {
			IAssetProvider assetProvider = getAssetProvider();
			final SmartCollectionImpl currentCollection = assetProvider == null ? null
					: assetProvider.getCurrentCollection();
			if (currentCollection != null) {
				List<SortCriterion> sortCriterion = currentCollection.getSortCriterion();
				if (!sortCriterion.isEmpty())
					sort = sortCriterion.get(0);
			}
		}
		return sort != null ? (QueryField.findQueryField(sort.getField()) == QueryField.URI) : false;
	}

	@Override
	protected void fillContextMenu(IMenuManager manager) {
		GalleryItem[] items = gallery.getSelection();
		for (GalleryItem item : items) {
			if (item.getData(CARD) != null) {
				manager.add(toggleCollapseAction);
				manager.add(new Separator());
				break;
			}
			Asset asset = (Asset) item.getData(ASSET);
			if (asset != null && expandedSet.contains(asset)) {
				manager.add(toggleCollapseAction);
				manager.add(new Separator());
				break;
			}
		}
		super.fillContextMenu(manager);
	}

	@Override
	protected GalleryItem getGalleryItem(Asset asset) {
		Integer i = galleryMap.get(asset);
		if (i != null)
			return gallery.getItem(0).getItem(i);
		int index = getAssetProvider().indexOf(asset);
		if (index >= 0)
			return gallery.getItem(0).getItem(index);
		return null;
	}

	@Override
	public boolean assetsChanged() {
		INavigationHistory navigationHistory = getNavigationHistory();
		if (navigationHistory != null && !gallery.isDisposed()) {
			AssetSelection selectedAssets = navigationHistory.getSelectedAssets();
			if (selectedAssets.isPicked()) {
				List<GalleryItem> items = new ArrayList<GalleryItem>(selectedAssets.size());
				for (Asset asset : selectedAssets.getAssets()) {
					GalleryItem item = getGalleryItem(asset);
					if (item != null)
						items.add(item);
				}
				GalleryItem[] selectedItems = items.toArray(new GalleryItem[items.size()]);
				gallery.setSelection(selectedItems);
				if (selectedItems.length > 0)
					gallery.showItem(selectedItems[0]);
			} else
				gallery.redraw();
			selection = selectedAssets;
			return true;
		}
		return false;
	}

	@Override
	public boolean collectionChanged() {
		expandedSet.clear();
		return super.collectionChanged();
	}

	public void setSelection(ISelection sel) {
		assetsChanged();
	}

	@Override
	protected void setAssetSelection(AssetSelection assetSelection) {
		selection = assetSelection;
		if (assetSelection.isEmpty())
			gallery.setSelection(NOITEM);
		else if (assetSelection.isPicked()) {
			GalleryItem[] items = new GalleryItem[assetSelection.size()];
			GalleryItem group = gallery.getItem(0);
			// Force creation of items because of lazy operation
			for (int i = 0; i < items.length; i++)
				items[i] = group.getItem(i);
			gallery.setSelection(items);
			gallery.showItem(items[0]);
		} else
			gallery.selectAll();
		fireSelection();
	}

	@Override
	protected boolean doRedrawCollection(Collection<? extends Asset> assets, QueryField node) {
		if (gallery == null || gallery.isDisposed())
			return false;
		int labelFontsize = 0;
		IAssetProvider assetProvider = getAssetProvider();
		if (assetProvider != null) {
			SmartCollection coll = assetProvider.getCurrentCollection();
			if (coll != null) {
				while (true) {
					showLabel = coll.getShowLabel();
					if (showLabel != Constants.INHERIT_LABEL) {
						labelTemplate = coll.getLabelTemplate();
						labelFontsize = coll.getFontSize();
						break;
					}
					if (coll.getSmartCollection_subSelection_parent() == null)
						break;
					coll = coll.getSmartCollection_subSelection_parent();
				}
				if (showLabel == Constants.INHERIT_LABEL) {
					String groupId = coll.getGroup_rootCollection_parent();
					Group group = Core.getCore().getDbManager().obtainById(GroupImpl.class, groupId);
					while (group != null) {
						showLabel = group.getShowLabel();
						if (showLabel != Constants.INHERIT_LABEL) {
							labelTemplate = group.getLabelTemplate();
							labelFontsize = coll.getFontSize();
							break;
						}
						group = group.getGroup_subgroup_parent();
					}
				}
			}
		}
		if (showLabel == Constants.INHERIT_LABEL) {
			showLabel = showLabelDflt;
			labelTemplate = labelTemplateDflt;
			labelFontsize = labelFontsizeDflt;
		}
		itemRenderer.setShowLabels(showLabel != Constants.NO_LABEL);
		gallery.setFont(showLabel == Constants.CUSTOM_LABEL && labelFontsize != 0 ? getSmallFont(labelFontsize)
				: JFaceResources.getDefaultFont());
		if (assets == null) {
			if (fetchAssets()) {
				scoreFormatter = assetProvider.getScoreFormatter();
				gallery.getItem(0).setItemCount(assetProvider.getAssetCount());
				gallery.clearAll();
				gallery.redraw();
				AssetSelection oldSelection = (AssetSelection) (selection == null ? AssetSelection.EMPTY : selection);
				if (oldSelection.isPicked()) {
					List<Asset> selectedAssets = new ArrayList<Asset>(oldSelection.size());
					List<GalleryItem> items = new ArrayList<GalleryItem>(oldSelection.size());
					for (Asset asset : oldSelection.getAssets()) {
						GalleryItem item = getGalleryItem(asset);
						if (item != null) {
							items.add(item);
							selectedAssets.add(asset);
						}
					}
					GalleryItem[] selectedItems = items.toArray(new GalleryItem[items.size()]);
					galleryMap.clear();
					gallery.setSelection(selectedItems);
					selection = new AssetSelection(selectedAssets);
				} else {
					galleryMap.clear();
					selection = new AssetSelection(assetProvider);
					gallery.redraw();
				}
			}
		} else {
			if (assets.size() == 1) {
				GalleryItem item = getGalleryItem(assets.iterator().next());
				if (item != null) {
					if (node == null) {
						item.setImage(placeHolder);
						item.setData(ROT, null);
					}
					gallery.redraw(item);
				}
			} else {
				if (node == null)
					for (Asset asset : assets) {
						GalleryItem item = getGalleryItem(asset);
						if (item != null)
							item.setImage(placeHolder);
					}
				gallery.redraw();
			}
		}
		return true;
	}

	private Font getSmallFont(int labelFontsize) {
		if (labelFontsize != currentFontsize) {
			currentFontsize = labelFontsize;
			if (smallFont != null) {
				smallFont.dispose();
				smallFont = null;
			}
		}
		if (smallFont == null) {
			FontData[] fd = JFaceResources.getDefaultFontDescriptor().getFontData();
			smallFont = new Font(gallery.getDisplay(), fd[0].getName(), currentFontsize, fd[0].getStyle());
		}
		return smallFont;
	}

	public void handleEvent(Event event) {
		final GalleryItem item = (GalleryItem) event.item;
		IAssetProvider assetProvider = getAssetProvider();
		int count = assetProvider == null ? 0 : assetProvider.getAssetCount();
		if (item.getParentItem() == null) {
			// It's a group
			if (folding) {
				foldingIndex = new int[count + 1];
				foldingHw = 0;
			} else
				foldingIndex = null;
			item.setItemCount(count);
		} else {
			// It's an item
			if (assetProvider == null) {
				item.setImage(placeHolder);
				return;
			}
			int index = event.index;
			if (folding) {
				if (index >= foldingHw) {
					int i = foldingIndex[foldingHw];
					Asset host = assetProvider.getAsset(i);
					if (host != null) {
						String[] relevantPart = collapseFilter.capture(Core.getFileName(host.getUri(), true));
						while (relevantPart != null && index >= foldingHw) {
							Asset nextAsset = assetProvider.getAsset(++i);
							if (nextAsset == null) {
								foldingIndex[++foldingHw] = i;
								break;
							}
							String[] nextRelevantPart = collapseFilter
									.capture(Core.getFileName(nextAsset.getUri(), true));
							boolean match = false;
							for (int j = 0; j < nextRelevantPart.length; j++)
								if (relevantPart[j].equals(nextRelevantPart[j])) {
									match = true;
									break;
								}
							if (!match) {
								foldingIndex[++foldingHw] = i;
								host = nextAsset;
								relevantPart = nextRelevantPart;
							} else if (expandedSet.contains(host))
								foldingIndex[++foldingHw] = i;
						}
					}
				}
				if (index >= foldingHw) {
					item.getParentItem().setItemCount(foldingHw);
					// Enforce structure update
					gallery.setItemCount(gallery.getItemCount());
					return;
				}
				int assetIndex = foldingIndex[index];
				Asset asset = assetProvider.getAsset(assetIndex);
				item.setImage(placeHolder);
				if (asset != null) {
					item.setData(ASSET, asset);
					galleryMap.put(asset, index);
					int card = foldingIndex[index + 1] - assetIndex;
					if (card > 1)
						item.setData(CARD, card);
				}
			} else {
				Asset asset = assetProvider.getAsset(index);
				item.setImage(placeHolder);
				if (asset != null) {
					item.setData(ASSET, asset);
					galleryMap.put(asset, index);
				}
			}
		}
	}

	@Override
	protected AssetSelection doGetAssetSelection() {
		IAssetProvider assetProvider = getAssetProvider();
		GalleryItem[] items = gallery.getOrderedSelection();
		AssetSelection sel = new AssetSelection(items.length);
		for (GalleryItem item : items)
			if (item != null) {
				AssetImpl asset = (AssetImpl) item.getData(ASSET);
				if (asset != null) {
					sel.add(asset);
					boolean multiple = item.getData(CARD) != null;
					if (multiple && foldingIndex != null) {
						Integer index = galleryMap.get(asset);
						if (index != null) {
							int i = index.intValue();
							int start = foldingIndex[i] + 1;
							int end = foldingIndex[i + 1];
							for (int j = start; j < end; j++) {
								Asset nextAsset = assetProvider.getAsset(j);
								if (nextAsset != null)
									sel.add(nextAsset);
							}
						}
					}
				}
			}
		return sel;
	}

	@Override
	protected void setItemText(final GalleryItem item, Asset asset, Integer cardinality) {
		if (asset != null)
			item.setText(UiUtilities.computeImageCaption(asset, scoreFormatter, cardinality, collapseFilter,
					showLabel == Constants.CUSTOM_LABEL ? labelTemplate : null));
	}

	protected Object vstripSizeProvider = new ISizeProvider() {

		public int getSizeFlags(boolean width) {
			return width ? SWT.MIN : SWT.NONE;
		}

		public int computePreferredSize(boolean width, int availableParallel, int availablePerpendicular,
				int preferredResult) {
			return width ? MINTHUMBSIZE + TRIM : preferredResult;
		}
	};

	protected Object hstripSizeProvider = new ISizeProvider() {

		public int getSizeFlags(boolean width) {
			return width ? SWT.NONE : SWT.MIN;
		}

		public int computePreferredSize(boolean width, int availableParallel, int availablePerpendicular,
				int preferredResult) {
			return width ? preferredResult : MINTHUMBSIZE + TRIM;
		}
	};

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter.equals(ISizeProvider.class)) {
			if (VSTRIP.equals(layout))
				return vstripSizeProvider;
			if (HSTRIP.equals(layout))
				return hstripSizeProvider;
		}
		return super.getAdapter(adapter);
	}

	@Override
	protected void scroll(int dist) {
		ScrollBar scrollBar = getScrollBar();
		if (scrollBar != null)
			scrollBar.setSelection(dist + scrollBar.getSelection());
	}

	private ScrollBar getScrollBar() {
		ScrollBar scrollBar = gallery.getVerticalBar();
		return (scrollBar == null) ? gallery.getHorizontalBar() : scrollBar;
	}

	@Override
	protected void editTitleArea(final GalleryItem item, Rectangle bounds) {
		final Asset asset = (Asset) item.getData("asset"); //$NON-NLS-1$
		if (asset != null) {
			cancelInput();
			final String captionText = UiUtilities.computeImageCaption(asset, null, null, null, null);
			titleInputValid = true;
			if (titleInput != null)
				titleInput.dispose();
			titleInput = new CheckedText(gallery, SWT.BORDER | SWT.SINGLE);
			titleInput.setSpellingOptions(10, ISpellCheckingService.NOSPELLING);
			titleInput.setText(captionText);
			CssActivator.getDefault().setColors(titleInput.getControl());
			final Color foreground = titleInput.getForeground();
			titleInput.setFont(gallery.getFont());
			int bw = titleInput.getBorderWidth();
			GC gc = new GC(titleInput);
			gc.setFont(titleInput.getFont());
			Point textExtent = gc.textExtent(captionText);
			gc.dispose();
			int w = Math.max(bounds.width, textExtent.x + 20);
			titleInput.setBounds(bounds.x - (w - bounds.width) / 2, bounds.y - bw, w, bounds.height + 2 * bw);
			titleInput.addTraverseListener(new TraverseListener() {
				public void keyTraversed(TraverseEvent e) {
					if (e.character == SWT.TAB) {
						ignoreTab();
						commitInput(asset, captionText);
						Event event = new Event();
						event.keyCode = (e.stateMask & SWT.SHIFT) != 0 ? SWT.ARROW_LEFT : SWT.ARROW_RIGHT;
						event.type = SWT.KeyDown;
						event.widget = gallery;
						Display display = gallery.getDisplay();
						display.post(event);
						display.asyncExec(() -> {
							if (!gallery.isDisposed()) {
								GalleryItem[] sel = gallery.getSelection();
								if (sel.length == 1) {
									GalleryItem item1 = sel[0];
									AssetImpl asset1 = (AssetImpl) item1.getData(ASSET);
									Hotspots hotSpots = (Hotspots) item1.getData(HOTSPOTS);
									if (hotSpots != null && asset1 != null) {
										Rectangle titleArea = hotSpots.getTitleArea();
										if (titleArea != null)
											editTitleArea(item1, titleArea);
									}
								}
							}
						});
						e.doit = false;
					}
				}
			});
			titleInput.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.keyCode == SWT.ESC)
						cancelInput();
					else if (e.character == SWT.CR)
						commitInput(asset, captionText);
				}
			});
			titleInput.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					commitInput(asset, captionText);
				}
			});
			closeTitleAreaListener = new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					commitInput(asset, captionText);
				}
			};
			gallery.addSelectionListener(closeTitleAreaListener);
			ScrollBar scrollBar = getScrollBar();
			if (scrollBar != null)
				scrollBar.addSelectionListener(closeTitleAreaListener);
			titleInput.addVerifyListener(new VerifyListener() {
				public void verifyText(VerifyEvent e) {
					showError(QueryField.NAME.isValid(
							titleInput.getText().substring(0, e.start) + e.text + titleInput.getText().substring(e.end),
							asset));
				}

				private void showError(String msg) {
					titleInputValid = msg == null;
					setStatusMessage(msg, true);
					titleInput.setForeground(titleInputValid ? foreground
							: titleInput.getControl().getDisplay().getSystemColor(SWT.COLOR_RED));
				}
			});
			titleInput.setFocus();
			titleInput.selectAll();
		}
	}

	protected void commitInput(Asset asset, String title) {
		if (titleInput != null) {
			String newTitle = titleInput.getText();
			cancelInput();
			int p = title.lastIndexOf('.');
			if (p >= 0 && newTitle.lastIndexOf('.') < 0)
				newTitle += title.substring(p);
			if (titleInputValid && !newTitle.equals(title))
				OperationJob.executeOperation(new RenameAssetOperation(asset, newTitle, true), this);
		}
	}

	void cancelInput() {
		if (closeTitleAreaListener != null && !gallery.isDisposed()) {
			gallery.removeSelectionListener(closeTitleAreaListener);
			ScrollBar scrollBar = getScrollBar();
			if (scrollBar != null)
				scrollBar.removeSelectionListener(closeTitleAreaListener);
		}
		if (titleInput != null) {
			titleInput.dispose();
			titleInput = null;
			gallery.redraw();
		}
	}

	@Override
	public void dispose() {
		if (smallFont != null)
			smallFont.dispose();
		cancelInput();
		super.dispose();
	}

	@Override
	protected void setDefaultPartName() {
		setPartName(isStrip ? (HSTRIP.equals(layout) ? Messages.getString("LightboxView.horizontal_strip") //$NON-NLS-1$
				: Messages.getString("LightboxView.vertical_strip")) : Messages.getString("LightboxView.lightbox")); //$NON-NLS-1$ //$NON-NLS-2$
	}

}