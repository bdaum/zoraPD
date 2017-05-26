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
 * (c) 2009-2016 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.ui.internal.views;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PartInitException;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.asset.Region;
import com.bdaum.zoom.cat.model.asset.RegionImpl;
import com.bdaum.zoom.cat.model.group.GroupImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.SortCriterion;
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IAssetFilter;
import com.bdaum.zoom.core.IAssetProvider;
import com.bdaum.zoom.core.IScoreFormatter;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IColorCodeFilter;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.db.IRatingFilter;
import com.bdaum.zoom.core.db.ITypeFilter;
import com.bdaum.zoom.core.internal.ImportState;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.AddAlbumOperation;
import com.bdaum.zoom.operations.internal.RetargetOperation;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.INavigationHistory;
import com.bdaum.zoom.ui.IZoomActionConstants;
import com.bdaum.zoom.ui.IZoomCommandIds;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.dialogs.ZInputDialog;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.Icons.Icon;
import com.bdaum.zoom.ui.internal.StartListener;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.actions.ZoomActionFactory;
import com.bdaum.zoom.ui.internal.dialogs.AlbumSelectionDialog;
import com.bdaum.zoom.ui.internal.dialogs.ColorCodeDialog;
import com.bdaum.zoom.ui.internal.dialogs.FileFormatDialog;
import com.bdaum.zoom.ui.internal.dialogs.RatingDialog;
import com.bdaum.zoom.ui.internal.dialogs.RetargetDialog;
import com.bdaum.zoom.ui.internal.dialogs.SelectTargetDialog;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;

@SuppressWarnings("restriction")
public abstract class AbstractGalleryView extends ImageView
		implements SelectAllHandler, SelectAllActionProvider, StartListener {

	private static final int DEFAULTTHUMBSIZE = 128;

	protected final class GalleryMouseWheelListener implements MouseWheelListener, IPreferenceChangeListener {

		private double zoomSpeed = 0;
		private double lag = 0.8d;
		private int softness;
		private int wheelkey;
		private ScheduledFuture<?> zoomTask;

		public GalleryMouseWheelListener() {
			setSoftness();
			setWheelkey();
			InstanceScope.INSTANCE.getNode(UiActivator.PLUGIN_ID).addPreferenceChangeListener(this);
		}

		private void setWheelkey() {
			wheelkey = UiActivator.getDefault().getPreferenceStore().getInt(PreferenceConstants.WHEELKEY);
		}

		private void setSoftness() {
			softness = UiActivator.getDefault().getPreferenceStore().getInt(PreferenceConstants.WHEELSOFTNESS);
			lag = softness * 0.003d + 0.65d;
			if (softness == 0)
				cancel();
		}

		public void mouseScrolled(final MouseEvent e) {
			if (softness == 0) {
				zoomSpeed = e.count;
				performWheelAction(wheelkey, e.stateMask);
				return;
			}
			zoomSpeed += 3 * e.count;
			if (zoomTask == null && zoomSpeed != 0) {
				setFocussedItem(e);
				zoomTask = UiActivator.getScheduledExecutorService().scheduleAtFixedRate(new Runnable() {

					public void run() {
						if (!e.display.isDisposed())
							e.display.syncExec(new Runnable() {
								public void run() {
									performWheelAction(wheelkey, e.stateMask);
								}
							});
						zoomSpeed = zoomSpeed * lag;
						if (zoomSpeed < lag) {
							GalleryMouseWheelListener.this.cancel();
							setFocussedItem(null);
						}
					}
				}, 0L, 100L, TimeUnit.MILLISECONDS);
			}
		}

		private void performWheelAction(final int wKey, int stateMask) {
			int speed = (int) (zoomSpeed + 0.5d);
			switch (wKey) {
			case PreferenceConstants.WHEELSHIFTPANS:
				if ((stateMask & SWT.SHIFT) != 0)
					scroll(speed);
				else if ((stateMask & (SWT.SHIFT | SWT.ALT)) == 0)
					zoom(speed);
				break;
			case PreferenceConstants.WHEELALTPANS:
				if ((stateMask & SWT.ALT) != 0)
					scroll(speed);
				else if ((stateMask & (SWT.SHIFT | SWT.ALT)) == 0)
					zoom(speed);
				break;
			case PreferenceConstants.WHEELSHIFTZOOMS:
				if ((stateMask & SWT.SHIFT) != 0)
					zoom(speed);
				else if ((stateMask & (SWT.SHIFT | SWT.ALT)) == 0)
					scroll(speed);
				break;
			case PreferenceConstants.WHEELALTZOOMS:
				if ((stateMask & SWT.ALT) != 0)
					zoom(speed);
				else if ((stateMask & (SWT.SHIFT | SWT.ALT)) == 0)
					scroll(speed);
				break;
			case PreferenceConstants.WHEELZOOMONLY:
				zoom(speed);
				break;
			case PreferenceConstants.WHEELSCROLLONLY:
				scroll(speed);
				break;
			}
		}

		public void cancel() {
			if (zoomTask != null) {
				zoomTask.cancel(true);
				zoomTask = null;
			}
		}

		public void preferenceChange(PreferenceChangeEvent event) {
			if (event.getKey().equals(PreferenceConstants.WHEELSOFTNESS))
				setSoftness();
			else if (event.getKey().equals(PreferenceConstants.WHEELKEY))
				setWheelkey();
		}

		public void dispose() {
			cancel();
			InstanceScope.INSTANCE.getNode(UiActivator.PLUGIN_ID).removePreferenceChangeListener(this);
		}
	}

	protected class ScaleContributionItem extends ControlContribution {

		private Scale scale;
		private final int min;
		private final int max;
		private long lastTime = -2000L;
		private int lastThumbSize = DEFAULTTHUMBSIZE;

		protected ScaleContributionItem(String id, int min, int max) {
			super(id);
			this.min = min;
			this.max = max;
		}

		@Override
		protected Control createControl(Composite parent) {
			Composite comp = new Composite(parent, SWT.NONE);
			RowLayout layout = new RowLayout();
			layout.spacing = layout.marginBottom = layout.marginTop = 0;
			comp.setLayout(layout);
			Button button = new Button(comp, SWT.NONE);
			button.setImage(Icons.refresh.getImage());
			button.setToolTipText(Messages.getString("AbstractGalleryView.return_to_previous_thumbnail_size")); //$NON-NLS-1$
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					int s = thumbsize;
					thumbsize = lastThumbSize;
					lastThumbSize = s;
					scale.setSelection(thumbsize);
					fireSizeChanged();
				}
			});
			scale = new Scale(comp, SWT.NONE);
			scale.setLayoutData(new RowData(100, SWT.DEFAULT));
			scale.setMinimum(min);
			scale.setMaximum(max);
			scale.setSelection(thumbsize);
			setTooltipText();
			scale.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					thumbsize = scale.getSelection();
					if (e.time - lastTime > 1000)
						lastThumbSize = thumbsize;
					lastTime = e.time;
					setFocussedItem(null);
					setTooltipText();
					fireSizeChanged();
				}
			});
			return comp;
		}

		public int getSelection() {
			return scale.getSelection();
		}

		public boolean isDisposed() {
			return scale.isDisposed();
		}

		public void setSelection(int selection) {
			thumbsize = selection;
			long time = System.currentTimeMillis();
			if (time - lastTime > 1000)
				lastThumbSize = thumbsize;
			lastTime = time;
			scale.setSelection(selection);
			setTooltipText();
			fireSizeChanged();
		}

		private void setTooltipText() {
			int thumbnailWidth = ImportState
					.computeThumbnailWidth(Core.getCore().getDbManager().getMeta(true).getThumbnailResolution());
			scale.setToolTipText(NLS.bind(Messages.getString("LightboxView.adjust_size_of_thumbnails"), //$NON-NLS-1$
					(scale.getSelection() * 100 / thumbnailWidth)));
		}

		public void increment(int incr) {
			thumbsize += incr;
			setSelection(thumbsize + incr);
		}

		public void increment() {
			increment(scale.getPageIncrement());
		}

		public void decrement() {
			increment(-scale.getPageIncrement());
		}
	}

	protected static final String THUMBNAIL_SIZE = "com.bdaum.zoom.currentThumbnailSize"; //$NON-NLS-1$
	protected static final int MAXTHUMBSIZE = 512;
	protected static final int MINTHUMBSIZE = 48;
	private static final int MAXSELECTALL = 500;
	private static final String LAST_SELECTION = "com.bdaum.zoom.lastSelection"; //$NON-NLS-1$
	protected IStructuredSelection selection;
	private IAction removeFromAlbumAction;
	private IAction selectRatingAction;
	private IAction selectFileTypeAction;
	private IAction saveQueryAction;
	private String initialSelection;
	protected int thumbsize = DEFAULTTHUMBSIZE;
	protected GalleryMouseWheelListener mouseWheelListener;
	protected IScoreFormatter scoreFormatter;
	protected int refreshing;
	protected IAction collapseAction;
	protected Set<Asset> expandedSet = new HashSet<Asset>();
	protected int[] foldingIndex;
	protected int foldingHw = 0;
	protected boolean folding = false;
	private IAction retargetAction;
	private Action selectColorCodeAction;
	protected IAction configureCollapseAction;
	private IAction renameAction;
	private IAction stackAction;
	private IAction splitCatAction;

	public IAssetProvider getAssetProvider() {
		return Core.getCore().getAssetProvider();
	}

	public void setFocussedItem(MouseEvent ev) {
		// do nothing
	}

	/**
	 * Subclasses may overwrite
	 */
	protected void fireSizeChanged() {
		// do nothing
	}

	/**
	 * Scroll
	 *
	 * @param dist
	 *            scrolling distance Subclasses may overwrite
	 */
	protected void scroll(int dist) {
		// do nothing
	}

	@Override
	public void keyReleased(KeyEvent e) {
		switch (e.character) {
		case '+':
			if (scaleContributionItem != null)
				scaleContributionItem.increment();
			return;
		case '-':
			if (scaleContributionItem != null)
				scaleContributionItem.decrement();
			return;
		}
		super.keyReleased(e);
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		if (memento != null) {
			IAssetProvider assetProvider = getAssetProvider();
			if (assetProvider == null || assetProvider.getLastSelection() == null)
				initialSelection = memento.getString(LAST_SELECTION);
			Integer thsize = memento.getInteger(THUMBNAIL_SIZE);
			if (thsize != null)
				thumbsize = thsize;
		}
	}

	@Override
	public void saveState(IMemento memento) {
		if (memento != null) {
			IAssetProvider assetProvider = getAssetProvider();
			if (assetProvider != null) {
				String lastSelection = assetProvider.getLastSelection();
				if (lastSelection != null)
					memento.putString(LAST_SELECTION, lastSelection);
			}
			memento.putInteger(THUMBNAIL_SIZE, thumbsize);
		}
	}

	protected void createScaleContributionItem(int min, int max) {
		scaleContributionItem = new ScaleContributionItem("scale", min, max); //$NON-NLS-1$
	}

	protected void addDragDropSupport() {
		addDragDropSupport(true, true, true);
	}

	@Override
	protected void makeActions(IActionBars bars) {
		super.makeActions(bars);
		saveQueryAction = new Action(Messages.getString("AbstractGalleryView.save_query_as_collection"), //$NON-NLS-1$
				Icons.tableSave.getDescriptor()) {

			@Override
			public void run() {
				IAssetProvider assetProvider = getAssetProvider();
				final SmartCollectionImpl currentCollection = assetProvider == null ? null
						: assetProvider.getCurrentCollection();
				if (currentCollection != null && currentCollection.getAdhoc()) {
					final SmartCollection parent = currentCollection.getSmartCollection_subSelection_parent();
					ZInputDialog dialog = new ZInputDialog(getSite().getShell(),
							parent != null
									? NLS.bind(Messages.getString("AbstractGalleryView.save_as_subcollection"), //$NON-NLS-1$
											parent.getName())
									: Messages.getString("AbstractGalleryView.save_as_collection"), //$NON-NLS-1$
							Messages.getString("AbstractGalleryView.collection_name"), currentCollection.getName(), //$NON-NLS-1$
							new IInputValidator() {

								public String isValid(String newText) {
									if (newText != null && newText.length() > 0) {
										return null;
									}
									return Messages.getString("AbstractGalleryView.please_specify_a_name"); //$NON-NLS-1$
								}
							}, false);
					if (dialog.open() == Window.OK) {
						String name = dialog.getValue();
						currentCollection.setName(name);
						currentCollection.setAdhoc(false);
						final IDbManager dbManager = Core.getCore().getDbManager();
						dbManager.safeTransaction(new Runnable() {

							public void run() {
								if (parent != null) {
									parent.addSubSelection(currentCollection);
									dbManager.store(parent);
								} else {
									GroupImpl user = UiUtilities.obtainUserGroup(dbManager);
									user.addRootCollection(currentCollection.getStringId());
									currentCollection.setGroup_rootCollection_parent(user.getStringId());
									dbManager.store(user);
								}
								dbManager.store(currentCollection);
							}

						});
						try {
							CatalogView catalogView = (CatalogView) getSite().getPage().showView(CatalogView.ID);
							catalogView.refresh();
							catalogView.setSelection(new StructuredSelection(currentCollection));
						} catch (PartInitException e) {
							// should never happen
						}
						updateActions();
					}
				}
			}
		};
		saveQueryAction.setToolTipText(Messages.getString("AbstractGalleryView.adds_the_current_query")); //$NON-NLS-1$
		removeFromAlbumAction = addAction(ZoomActionFactory.REMOVEFROMALBUM.create(bars, this));
		selectRatingAction = new Action(Messages.getString("AbstractGalleryView.show_all_images"), //$NON-NLS-1$
				Icons.ratingAllSmall.getDescriptor()) {

			@Override
			public void runWithEvent(Event event) {
				IRatingFilter oldFilter = null;
				IAssetFilter[] filters = getNavigationHistory().getFilters();
				int rating = QueryField.SELECTALL;
				if (filters != null)
					for (IAssetFilter filter : filters) {
						if (filter instanceof IRatingFilter) {
							oldFilter = ((IRatingFilter) filter);
							rating = oldFilter.getRating();
							break;
						}
					}
				RatingDialog dialog = new RatingDialog(getSite().getShell(), rating);
				dialog.create();
				Widget widget = event.widget;
				if (widget instanceof ToolItem) {
					Rectangle bounds = ((ToolItem) widget).getBounds();
					dialog.getShell()
							.setLocation(((ToolItem) widget).getParent().toDisplay(bounds.x, bounds.y + bounds.height));
				}
				int newRating = dialog.open();
				if (newRating != RatingDialog.SELECTABORT && (newRating != rating)) {
					IRatingFilter newFilter = newRating != QueryField.SELECTALL
							? Core.getCore().getDbFactory().createRatingFilter(newRating) : null;
					getNavigationHistory().updateFilters(oldFilter, newFilter);
					String tooltip = newRating <= 0 ? null
							: NLS.bind(Messages.getString("AbstractGalleryView.show_only_image_or_better"), //$NON-NLS-1$
									QueryField.RATING.getEnumLabels()[newRating + 1]);
					Icon icon;
					switch (newRating) {
					case QueryField.SELECTALL:
						tooltip = Messages.getString("AbstractGalleryView.show_all_images"); //$NON-NLS-1$
						icon = Icons.ratingAllSmall;
						break;
					case 1:
						icon = Icons.rating1;
						break;
					case 2:
						icon = Icons.rating2;
						break;
					case 3:
						icon = Icons.rating3;
						break;
					case 4:
						icon = Icons.rating4;
						break;
					case 5:
						icon = Icons.rating5;
						break;
					default:
						tooltip = Messages.getString("AbstractGalleryView.show_unrated_images"); //$NON-NLS-1$
						icon = Icons.rating_undef;
						break;
					}
					setToolTipText(tooltip);
					setImageDescriptor(icon.getDescriptor());
				}
			}
		};
		selectRatingAction.setToolTipText(Messages.getString("AbstractGalleryView.show_images_with_rating")); //$NON-NLS-1$

		selectColorCodeAction = new Action(Messages.getString("AbstractGalleryView.indepent_of_color_code"), //$NON-NLS-1$
				Icons.dashed.getDescriptor()) {

			@Override
			public void runWithEvent(Event event) {
				IColorCodeFilter oldFilter = null;
				IAssetFilter[] filters = getNavigationHistory().getFilters();
				int colorCode = QueryField.SELECTALL;
				if (filters != null)
					for (IAssetFilter filter : filters) {
						if (filter instanceof IColorCodeFilter) {
							oldFilter = ((IColorCodeFilter) filter);
							colorCode = oldFilter.getColorCode();
							break;
						}
					}
				ColorCodeDialog dialog = new ColorCodeDialog(getSite().getShell(), colorCode);
				dialog.create();
				Widget widget = event.widget;
				if (widget instanceof ToolItem) {
					Rectangle bounds = ((ToolItem) widget).getBounds();
					dialog.getShell()
							.setLocation(((ToolItem) widget).getParent().toDisplay(bounds.x, bounds.y + bounds.height));
				}
				int newColorCode = dialog.open();
				if (newColorCode != ColorCodeDialog.SELECTABORT && (newColorCode != colorCode)) {
					IColorCodeFilter newFilter = newColorCode != Constants.COLOR_UNDEFINED
							? Core.getCore().getDbFactory().createColorCodeFilter(newColorCode) : null;
					getNavigationHistory().updateFilters(oldFilter, newFilter);
					String tooltip = newColorCode <= 0 ? null
							: NLS.bind(Messages.getString("AbstractGalleryView.with_color_code"), //$NON-NLS-1$
									QueryField.COLORCODELABELS[newColorCode + 1]);
					Icon icon;
					switch (newColorCode) {
					case Constants.COLOR_BLACK:
						icon = Icons.black;
						break;
					case Constants.COLOR_WHITE:
						icon = Icons.white;
						break;
					case Constants.COLOR_RED:
						icon = Icons.red;
						break;
					case Constants.COLOR_GREEN:
						icon = Icons.green;
						break;
					case Constants.COLOR_BLUE:
						icon = Icons.blue;
						break;
					case Constants.COLOR_CYAN:
						icon = Icons.cyan;
						break;
					case Constants.COLOR_MAGENTA:
						icon = Icons.magenta;
						break;
					case Constants.COLOR_YELLOW:
						icon = Icons.yellow;
						break;
					case Constants.COLOR_ORANGE:
						icon = Icons.orange;
						break;
					case Constants.COLOR_PINK:
						icon = Icons.pink;
						break;
					case Constants.COLOR_VIOLET:
						icon = Icons.violet;
						break;
					default:
						tooltip = Messages.getString("AbstractGalleryView.indepent_of_color_code"); //$NON-NLS-1$
						icon = Icons.dashed;
						break;
					}
					setToolTipText(tooltip);
					setImageDescriptor(icon.getDescriptor());
				}
			}
		};
		selectColorCodeAction.setToolTipText(Messages.getString("AbstractGalleryView.with_selected_color_code")); //$NON-NLS-1$

		selectFileTypeAction = new Action(Messages.getString("AbstractGalleryView.select_file_type"), //$NON-NLS-1$
				Icons.format.getDescriptor()) {

			@Override
			public void runWithEvent(Event event) {
				int formats = ITypeFilter.ALLFORMATS;
				ITypeFilter oldFilter = null;
				IAssetFilter[] filters = getNavigationHistory().getFilters();
				if (filters != null)
					for (IAssetFilter filter : filters)
						if (filter instanceof ITypeFilter) {
							oldFilter = ((ITypeFilter) filter);
							formats = oldFilter.getFormats();
							break;
						}
				FileFormatDialog dialog = new FileFormatDialog(getSite().getShell(), formats);
				dialog.create();
				Widget widget = event.widget;
				if (widget instanceof ToolItem) {
					Rectangle bounds = ((ToolItem) widget).getBounds();
					dialog.getShell()
							.setLocation(((ToolItem) widget).getParent().toDisplay(bounds.x, bounds.y + bounds.height));
				}
				int newFormats = dialog.open();
				if (newFormats >= 0 && newFormats != formats) {
					ITypeFilter newFilter = newFormats == ITypeFilter.ALLFORMATS ? null
							: Core.getCore().getDbFactory().createTypeFilter(newFormats);
					getNavigationHistory().updateFilters(oldFilter, newFilter);
					String tooltip;
					Icon icon;
					if (newFormats == ITypeFilter.ALLFORMATS) {
						tooltip = Messages.getString("AbstractGalleryView.show_all_images_independent_of_format"); //$NON-NLS-1$
						icon = Icons.format;
					} else {
						icon = Icons.formatSelect;
						StringBuffer sb = new StringBuffer();
						if ((newFormats & ITypeFilter.RAW) != 0)
							sb.append("Raw"); //$NON-NLS-1$
						if ((newFormats & ITypeFilter.DNG) != 0) {
							if (sb.length() > 0)
								sb.append(", "); //$NON-NLS-1$
							sb.append("DNG"); //$NON-NLS-1$
						}
						if ((newFormats & ITypeFilter.JPEG) != 0) {
							if (sb.length() > 0)
								sb.append(", "); //$NON-NLS-1$
							sb.append("JPEG"); //$NON-NLS-1$
						}
						if ((newFormats & ITypeFilter.TIFF) != 0) {
							if (sb.length() > 0)
								sb.append(", "); //$NON-NLS-1$
							sb.append("TIFF"); //$NON-NLS-1$
						}
						if ((newFormats & ITypeFilter.OTHER) != 0) {
							if (sb.length() > 0)
								sb.append(", "); //$NON-NLS-1$
							sb.append(Messages.getString("AbstractGalleryView.Other")); //$NON-NLS-1$
						}
						if ((newFormats & ITypeFilter.MEDIA) != 0) {
							if (sb.length() > 0)
								sb.append(", "); //$NON-NLS-1$
							sb.append(Messages.getString("AbstractGalleryView.other_media")); //$NON-NLS-1$
						}
						tooltip = NLS.bind(
								Messages.getString("AbstractGalleryView.show_only_images_with_selected_formats"), sb); //$NON-NLS-1$
					}
					setToolTipText(tooltip);
					setImageDescriptor(icon.getDescriptor());
				}
			}
		};
		selectFileTypeAction
				.setToolTipText(Messages.getString("AbstractGalleryView.show_all_images_independent_of_format")); //$NON-NLS-1$

		retargetAction = new Action(Messages.getString("AbstractGalleryView.retarget")) { //$NON-NLS-1$

			@Override
			public void runWithEvent(Event event) {
				AssetSelection sel = (AssetSelection) getSelection();
				AssetImpl asset = (AssetImpl) sel.getFirstElement();
				URI uri = Core.getCore().getVolumeManager().findFile(asset);
				File file = new File(uri);
				DirectoryDialog dialog = new DirectoryDialog(getSite().getShell());
				dialog.setText(Messages.getString("AbstractGalleryView.retarget_title")); //$NON-NLS-1$
				dialog.setFilterPath(file.getParent());
				String oldPath = file.getPath();
				String folderPath = dialog.open();
				if (folderPath != null) {
					File folder = new File(folderPath);
					File newFile = new File(folder, file.getName());
					if (!newFile.exists()) {
						List<File> files = new ArrayList<File>();
						collectFiles(folder, file.getName(), files);
						if (files.isEmpty()) {
							AcousticMessageDialog.openInformation(getSite().getShell(),
									Messages.getString("AbstractGalleryView.retarget_title"), //$NON-NLS-1$
									NLS.bind(Messages.getString("AbstractGalleryView.target_file_not_found"), //$NON-NLS-1$
											file.getName(), folder));
							return;
						}
						SelectTargetDialog selectDialog = new SelectTargetDialog(getSite().getShell(), files, folder);
						if (selectDialog.open() != SelectTargetDialog.OK)
							return;
						newFile = selectDialog.getFile();
					}
					if (newFile.getName().equals(file.getName())) {
						RetargetDialog retargetDialog = new RetargetDialog(getSite().getShell(), oldPath,
								newFile.getAbsolutePath());
						if (retargetDialog.open() == RetargetDialog.OK)
							OperationJob.executeOperation(new RetargetOperation(asset, newFile,
									retargetDialog.getResult(), retargetDialog.getRetargetVoiceNote()),
									AbstractGalleryView.this);
					} else
						AcousticMessageDialog.openInformation(getSite().getShell(),
								Messages.getString("AbstractGalleryView.retarget_impossible"), //$NON-NLS-1$
								Messages.getString("AbstractGalleryView.file_names_do_not_match")); //$NON-NLS-1$
				}
			}

			private void collectFiles(File folder, String name, List<File> files) {
				File[] members = folder.listFiles();
				if (members != null)
					for (File file : members)
						if (file.isDirectory())
							collectFiles(file, name, files);
						else if (file.getName().equals(name))
							files.add(file);
			}
		};
		retargetAction.setToolTipText(Messages.getString("AbstractGalleryView.retarget_tooltip")); //$NON-NLS-1$
		renameAction = addAction(ZoomActionFactory.BULKRENAME.create(bars, this));
		stackAction = addAction(ZoomActionFactory.STACK.create(bars, this));
		splitCatAction = addAction(ZoomActionFactory.SPLITCATALOG.create(null, this));
		createSelectallAction();
	}

	@Override
	protected void fillLocalPullDown(IMenuManager manager) {
		manager.add(viewImageAction);
		manager.add(new Separator());
		manager.add(editAction);
		manager.add(editWithAction);
		manager.add(new Separator());
		if (collapseAction != null)
			manager.add(collapseAction);
		if (configureCollapseAction != null)
			manager.add(configureCollapseAction);
		manager.add(selectAllAction);
		manager.add(new Separator());
		manager.add(saveQueryAction);
	}

	@Override
	protected void fillContextMenu(IMenuManager manager) {
		boolean readOnly = dbIsReadonly();
		fillEditAndSearchGroup(manager, readOnly);
		fillVoiceNote(manager, readOnly);
		fillMetaData(manager, readOnly);
		fillRelationsGroup(manager);
		manager.add(new Separator(IZoomActionConstants.MB_SUBMENUS));
		fillShowAndDeleteGroup(manager, readOnly);
		fillBulkGroup(manager, readOnly);
		super.fillAdditions(manager);
	}

	private void fillBulkGroup(IMenuManager manager, boolean readOnly) {
		if (!readOnly) {
			manager.add(new Separator());
			manager.add(categorizeAction);
		}

	}

	@Override
	protected void fillShowAndDeleteGroup(IMenuManager manager, boolean readOnly) {
		manager.add(showInFolderAction);
		manager.add(showInTimeLineAction);
		manager.add(new Separator());
		if (!readOnly) {
			manager.add(getSelectAllAction());
			manager.add(addBookmarkAction);
			manager.add(addToAlbumAction);
			IAssetProvider assetProvider = getAssetProvider();
			if (assetProvider != null && assetProvider.getCurrentCollection().getAlbum())
				manager.add(removeFromAlbumAction);
			ISelection sel = getSelection();
			if (sel instanceof AssetSelection && !sel.isEmpty()) {
				if (((AssetSelection) sel).size() == 1) {
					if (Core.getCore().getVolumeManager().findExistingFile(((AssetSelection) sel).getAssets().get(0),
							false) == null)
						manager.add(retargetAction);
					else
						manager.add(refreshAction);
				} else
					manager.add(refreshAction);
			}
			manager.add(renameAction);
			manager.add(stackAction);
			manager.add(deleteAction);
		}
	}

	@Override
	protected void fillLocalToolBar(IToolBarManager manager) {
		boolean readOnly = dbIsReadonly();
		if (collapseAction != null)
			manager.add(collapseAction);
		if (scaleContributionItem != null)
			manager.add(scaleContributionItem);
		manager.add(selectFileTypeAction);
		manager.add(selectRatingAction);
		manager.add(selectColorCodeAction);
		manager.add(new Separator());
		manager.add(viewImageAction);
		manager.add(new Separator());
		manager.add(editAction);
		manager.add(editWithAction);
		manager.add(new Separator());
		if (!readOnly) {
			manager.add(addBookmarkAction);
			manager.add(saveQueryAction);
			manager.add(new Separator());
		}
	}

	public ISelection getSelection() {
		return (selection != null) ? selection : StructuredSelection.EMPTY;
	}

	@Override
	public boolean collectionChanged() {
		INavigationHistory navigationHistory = getNavigationHistory();
		if (navigationHistory != null) {
			IAssetProvider assetProvider = getAssetProvider();
			if (assetProvider != null) {
				assetProvider.setCurrentCollection(navigationHistory.getSelectedCollection());
				assetProvider.setCurrentFilters(navigationHistory.getFilters());
				assetProvider.setCurrentSort(navigationHistory.getCustomSort());
			}
		}
		return true;
	}

	@Override
	public boolean assetsChanged() {
		return true;
	}

	@Override
	public boolean selectionChanged() {
		return false;
	}

	@Override
	public Object getContent() {
		IAssetProvider assetProvider = getAssetProvider();
		if (assetProvider != null)
			return assetProvider.getCurrentCollection();
		return super.getContent();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.views.SelectAllHandler#selectAll()
	 */

	@Override
	public void selectAll() {
		BusyIndicator.showWhile(getSite().getShell().getDisplay(), new Runnable() {
			public void run() {
				setAssetSelection(new AssetSelection(getAssetProvider()));
				// fireSelection();
			}
		});
	}

	@Override
	protected void selectNone() {
		setAssetSelection(AssetSelection.EMPTY);
	}

	protected abstract void setAssetSelection(AssetSelection assetSelection);

	@Override
	public void assetsModified(final BagChange<Asset> changes, final QueryField node) {
		// if (node == null || node == QueryField.RATING || node ==
		// QueryField.COLORCODE || node == QueryField.STATUS) {
		Shell shell = getSite().getShell();
		if (!shell.isDisposed())
			shell.getDisplay().asyncExec(new Runnable() {
				public void run() {
					// if (changes == null || changes.hasStructuralChanges())
					refresh();
					// else
					// redrawCollection(changes.getModified(), node);
					updateActions();
				}
			});
		// }
	}

	public boolean redrawCollection(Collection<? extends Asset> assets, QueryField node) {
		++refreshing;
		try {
			launchDecorator();
			return doRedrawCollection(assets, node);
		} finally {
			--refreshing;
		}
	}

	public int getRefreshing() {
		return refreshing;
	}

	protected abstract boolean doRedrawCollection(Collection<? extends Asset> assets, QueryField node);

	protected boolean fetchAssets() {
		IAssetProvider assetProvider = getAssetProvider();
		if (assetProvider == null)
			return false;
		final SmartCollectionImpl currentCollection = assetProvider.getCurrentCollection();
		assetProvider.setCurrentFilters(getNavigationHistory().getFilters());
		assetProvider.setCurrentSort(getNavigationHistory().getCustomSort());
		assetProvider.selectAssets();
		if (currentCollection != null) {
			setPartName(currentCollection.getName());
			StringBuilder des = new StringBuilder(128);
			if (currentCollection.getAdhoc())
				des.append(UiUtilities.composeContentDescription(currentCollection, " - ", false)); //$NON-NLS-1$
			SortCriterion currentSort = assetProvider.getCurrentSort();
			if (currentSort != null) {
				QueryField qfield = QueryField.findQueryField(currentSort.getField());
				if (qfield != null) {
					if (des.length() > 0)
						des.append("  -  "); //$NON-NLS-1$
					des.append(NLS.bind(Messages.getString("AbstractGalleryView.forced_sort"), qfield.getLabel())); //$NON-NLS-1$
				}
			}
			setContentDescription(des.toString());
		} else {
			setDefaultPartName();
			setContentDescription(""); //$NON-NLS-1$
		}
		updateActions();
		updateStatusLine();
		return assetProvider.canProvideAssets();
	}

	protected abstract void setDefaultPartName();

	@Override
	protected void updateStatusLine() {
		IAssetProvider assetProvider = getAssetProvider();
		if (assetProvider == null || assetProvider.getCurrentCollection() == null)
			setStatusMessage(Messages.getString("AbstractGalleryView.no_collection"), false); //$NON-NLS-1$
		else if (assetProvider.isEmpty())
			setStatusMessage(assetProvider.getCurrentCollection().getAdhoc()
					? Messages.getString("AbstractGalleryView.no_results") //$NON-NLS-1$
					: Messages.getString("AbstractGalleryView.collection_empty"), false); //$NON-NLS-1$
		else
			setStatusMessage(NLS.bind(Messages.getString("AbstractGalleryView.n_images"), //$NON-NLS-1$
					String.valueOf(getAssetProvider().getAssetCount()),
					String.valueOf(selection == null ? 0 : selection.size())), false);
	}

	@Override
	public void filterChanged() {
		redrawCollection(null, null);
		fireSelection();
	}

	@Override
	public void refresh() {
		redrawCollection(null, null);
		if (initialSelection != null) {
			IAssetProvider assetProvider = getAssetProvider();
			if (assetProvider != null) {
				SmartCollectionImpl coll = assetProvider.loadCollection(initialSelection);
				if (coll != null)
					UiActivator.getDefault().getNavigationHistory(getSite().getWorkbenchWindow())
							.selectionChanged(new SelectionChangedEvent(this, new StructuredSelection(coll)));
				initialSelection = null;
			}
		}
	}

	protected void createSelectallAction() {
		selectAllAction = new Action(Messages.getString("AbstractGalleryView.select_all"), //$NON-NLS-1$
				Icons.selectAll.getDescriptor()) {

			@Override
			public void run() {
				IAssetProvider assetProvider = getAssetProvider();
				if (assetProvider != null) {
					int assetCount = assetProvider.getAssetCount();
					if (assetCount > MAXSELECTALL && !AcousticMessageDialog.openConfirm(getSite().getShell(),
							Messages.getString("AbstractGalleryView.select_all"), //$NON-NLS-1$
							NLS.bind(Messages.getString("AbstractGalleryView.selecting_a_large_number"), //$NON-NLS-1$
									assetCount)))
						return;
					selectAll();
				}
			}

		};
		selectAllAction.setToolTipText(Messages.getString("AbstractGalleryView.select_all_images_in_gallery")); //$NON-NLS-1$
	}

	@Override
	protected void registerCommands() {
		registerCommand(selectAllAction, IWorkbenchCommandConstants.EDIT_SELECT_ALL);
		registerCommand(removeFromAlbumAction, IZoomCommandIds.RemoveFromAlbum);
		registerCommand(splitCatAction, IZoomCommandIds.SplitCatalogCommand);
		super.registerCommands();
	}

	@Override
	public void updateActions() {
		if (removeFromAlbumAction == null)
			return;
		super.updateActions();
		boolean writable = !dbIsReadonly();
		int localCount = getSelectionCount(true);
		removeFromAlbumAction.setEnabled(localCount > 0 && writable);
		renameAction.setEnabled(localCount > 0 && writable);
		stackAction.setEnabled(localCount > 1 && writable);
		splitCatAction.setEnabled(true);
		IAssetProvider assetProvider = getAssetProvider();
		if (selectAllAction != null)
			selectAllAction.setEnabled(assetProvider != null);
		SmartCollectionImpl currentCollection = assetProvider == null ? null : assetProvider.getCurrentCollection();
		boolean canSave = false;
		if (currentCollection != null) {
			if (currentCollection.getAdhoc() && !currentCollection.getSystem())
				canSave = true;
			SmartCollection parent = currentCollection.getSmartCollection_subSelection_parent();
			if (parent != null) {
				if (parent.getAdhoc())
					canSave = false;
				if (parent.getCriterion().size() == 1 && parent.getCriterion(0).getField().startsWith("*")) //$NON-NLS-1$
					canSave = false;
			}
		}
		saveQueryAction.setEnabled(canSave && writable);
	}

	protected void addMouseWheelListener(Composite composite) {
		mouseWheelListener = new GalleryMouseWheelListener();
		composite.addMouseWheelListener(mouseWheelListener);
	}

	protected void addCueListener() {
		getControl().addMouseMoveListener(new MouseMoveListener() {

			private Object cue;

			public void mouseMove(MouseEvent e) {
				Object ob = findObject(e.x, e.y);
				if (ob != null) {
					if (!ob.equals(cue))
						getNavigationHistory().postCueChanged(cue = ob);
				} else if (cue != null)
					getNavigationHistory().postCueChanged(cue = ob);
			}
		});
	}

	private void zoom(int zoomSpeed) {
		if (scaleContributionItem != null && !scaleContributionItem.isDisposed())
			scaleContributionItem.increment(zoomSpeed);
	}

	@Override
	public void dispose() {
		if (mouseWheelListener != null) {
			mouseWheelListener.dispose();
			mouseWheelListener = null;
		}
		super.dispose();
	}

	public IAction getSelectAllAction() {
		return selectAllAction;
	}

	protected void setAppStarting(final Control control) {
		if (UiActivator.getDefault().addStartListener(this))
			setSystemCursor(control, SWT.CURSOR_APPSTARTING);
	}

	public void hasStarted() {
		setSystemCursor(getControl(), SWT.CURSOR_ARROW);
	}

	private static void setSystemCursor(Control control, int format) {
		if (control != null && !control.isDisposed())
			control.setCursor(control.getDisplay().getSystemCursor(format));
	}

	protected void stopAudio() {
		UiActivator.getDefault().stopAudio();
	}

	protected void editRegionName(ImageRegion foundRegion) {
		Asset asset = foundRegion.owner;
		String regionId = foundRegion.regionId;
		IDbManager dbManager = Core.getCore().getDbManager();
		List<RegionImpl> set = dbManager.obtainObjects(RegionImpl.class, false, Constants.OID, regionId,
				QueryField.EQUALS, "asset_person_parent", //$NON-NLS-1$
				asset.getStringId(), QueryField.EQUALS);
		List<String> assignedAlbums = null;
		RegionImpl region;
		if (!set.isEmpty()) {
			region = set.get(0);
			String albumId = region.getAlbum();
			if (dbManager.exists(SmartCollectionImpl.class, albumId))
				assignedAlbums = Collections.singletonList(albumId);
		} else {
			region = new RegionImpl(false, null, null, "", Region.type_face, null); //$NON-NLS-1$
			region.setStringId(regionId);
			region.setAsset_person_parent(asset.getStringId());
		}
		AlbumSelectionDialog dialog = new AlbumSelectionDialog(getSite().getShell(), true, false, assignedAlbums);
		if (dialog.open() == AlbumSelectionDialog.OK)
			OperationJob.executeOperation(new AddAlbumOperation(dialog.getResult(), Collections.singletonList(asset),
					region, dialog.isDeleteRegion()), this);
	}

}