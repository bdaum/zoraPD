package com.bdaum.zoom.ui.internal.views;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.bdaum.aoModeling.runtime.AomList;
import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.group.Criterion;
import com.bdaum.zoom.cat.model.group.GroupImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitionImpl;
import com.bdaum.zoom.cat.model.group.slideShow.SlideImpl;
import com.bdaum.zoom.cat.model.group.slideShow.SlideShow;
import com.bdaum.zoom.cat.model.group.slideShow.SlideShowImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebGalleryImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.ICollectionProcessor;
import com.bdaum.zoom.core.db.IDbFactory;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.ui.IZoomCommandIds;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.actions.SelectionActionCluster;
import com.bdaum.zoom.ui.internal.actions.ZoomActionFactory;
import com.bdaum.zoom.ui.internal.dialogs.CollectionEditDialog;
import com.bdaum.zoom.ui.internal.dialogs.ExhibitionEditDialog;
import com.bdaum.zoom.ui.internal.dialogs.GroupDialog;
import com.bdaum.zoom.ui.internal.dialogs.ProximityEditDialog;
import com.bdaum.zoom.ui.internal.dialogs.SlideshowEditDialog;
import com.bdaum.zoom.ui.internal.dialogs.WebGalleryEditDialog;
import com.bdaum.zoom.ui.internal.operations.ExhibitionPropertiesOperation;
import com.bdaum.zoom.ui.internal.operations.SlideshowPropertiesOperation;
import com.bdaum.zoom.ui.internal.operations.WebGalleryPropertiesOperation;

@SuppressWarnings("restriction")
public abstract class AbstractCatalogView extends BasicView implements IOperationHistoryListener {

	protected class SelectionJob extends Job {

		private final ColumnViewer viewer;
		private final SelectionChangedEvent event;

		protected SelectionJob(ColumnViewer viewer, SelectionChangedEvent event) {
			super(Messages.getString("CatalogView.show_selection")); //$NON-NLS-1$
			setSystem(true);
			setPriority(Job.INTERACTIVE);
			setRule(rule);
			this.viewer = viewer;
			this.event = event;
		}

		@Override
		public boolean belongsTo(Object family) {
			return family == AbstractCatalogView.this;
		}

		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			selectionJobRunning = true;
			try {
				final Control control = viewer.getControl();
				if (!control.isDisposed())
					control.getDisplay().asyncExec(() -> {
						if (!control.isDisposed() && !monitor.isCanceled()) {
							IStructuredSelection selection = viewer.getStructuredSelection();
							Object firstElement = selection.getFirstElement();
							if (firstElement instanceof SlideShowImpl || firstElement instanceof ExhibitionImpl
									|| firstElement instanceof WebGalleryImpl) {
								openSlideShowEditor(
										firstElement instanceof SlideShowImpl ? (SlideShowImpl) firstElement : null,
										false);
								openExhibitionEditor(
										firstElement instanceof ExhibitionImpl ? (ExhibitionImpl) firstElement : null,
										false);
								openWebGalleryEditor(
										firstElement instanceof WebGalleryImpl ? (WebGalleryImpl) firstElement : null,
										false);
							}
							updateActions(selection, false);
							fireSelection(event);
						}
					});
				return Status.OK_STATUS;
			} finally {
				selectionJobRunning = false;
			}
		}
	}

	protected IAction editItemAction;
	protected IAction playSlideshowAction;
	private IAction splitCatAction;
	protected ColumnViewer viewer;
	protected IOperationHistory operationHistory;
	protected boolean cntrlDwn;
	private boolean selectionJobRunning;

	protected void updateActions(IStructuredSelection selection, boolean force) {
		if (selection.isEmpty()) {
			editItemAction.setEnabled(false);
			playSlideshowAction.setEnabled(false);
		} else {
			editItemAction.setEnabled(true);
			Object obj = selection.getFirstElement();
			playSlideshowAction.setEnabled(obj instanceof SlideShow || obj instanceof SmartCollection);
		}
		updateActions(force);
	}

	@Override
	public void updateActions(boolean force) {
		selectionActionCluster.updateActions();
		splitCatAction.setEnabled(true);
		updateActions(-1, -1);
	}

	protected static final ISchedulingRule rule = new ISchedulingRule() {
		public boolean isConflicting(ISchedulingRule rule) {
			return rule == this;
		}

		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}
	};
	private SelectionActionCluster selectionActionCluster;

	public AbstractCatalogView() {
		super();
	}

	protected void openWebGalleryEditor(WebGalleryImpl gallery, boolean force) {
		openPresentationEditor(WebGalleryView.WEBGALLERY_PERSPECTIVE, WebGalleryView.ID, gallery, force);
	}

	protected void openExhibitionEditor(ExhibitionImpl exhibition, boolean force) {
		openPresentationEditor(ExhibitionView.EXHIBITION_PERSPECTIVE, ExhibitionView.ID, exhibition, force);
	}

	protected void openSlideShowEditor(SlideShowImpl show, boolean force) {
		openPresentationEditor(SlideshowView.SLIDES_PERSPECTIVE, SlideshowView.ID, show, force);
	}

	protected void openPresentationEditor(String perspectiveId, String viewId, IdentifiableObject presentation,
			boolean force) {
		IViewPart view = null;
		if (presentation == null)
			view = getSite().getPage().findView(viewId);
		else {
			BasicView.openPerspective(perspectiveId);
			try {
				view = getSite().getPage().showView(viewId);
			} catch (PartInitException e) {
				// should never happen
			}
		}
		if (view instanceof AbstractPresentationView && (force || presentation == null
				|| !presentation.getStringId().equals(((AbstractPresentationView) view).getCurrentPresentation())))
			((AbstractPresentationView) view).setInput(presentation);
	}

	protected void fireSelection(SelectionChangedEvent event) {
		for (Object listener : listeners.getListeners())
			((ISelectionChangedListener) listener).selectionChanged(event);
	}

	public void setSelection(ISelection selection, boolean forceUpdate) {
		setSelection(selection);
		if (forceUpdate)
			fireSelection();
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (!selectionJobRunning)
			super.selectionChanged(part, selection);
	}

	protected void hookContextMenu(Viewer viewer) {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				AbstractCatalogView.this.fillContextMenu(manager);
			}
		});
		viewer.getControl().setMenu(menuMgr.createContextMenu(viewer.getControl()));
		getSite().registerContextMenu(menuMgr, viewer);
	}

	protected void fillContextMenu(IMenuManager manager) {
		updateActions(true);
		addEnabled(manager, playSlideshowAction);
		selectionActionCluster.addToMenuManager(manager);
		addEnabled(manager, editItemAction);
	}

	@Override
	protected void makeActions() {
		super.makeActions();
		selectionActionCluster = ZoomActionFactory.createSelectionActionCluster(null, this);
		editItemAction = new Action(Messages.getString("CatalogView.edit"), Icons.folder_edit.getDescriptor()) { //$NON-NLS-1$
			@Override
			public void run() {
				final IDbManager dbManager = Core.getCore().getDbManager();
				if (dbManager == null)
					return;
				Object obj = viewer.getStructuredSelection().getFirstElement();
				if (obj instanceof SmartCollectionImpl) {
					final SmartCollection current = (SmartCollectionImpl) obj;
					final SmartCollection result = editCollection(current);
					if (result != null) {
						Runnable runnable = new Runnable() {
							public void run() {
								Set<Object> toBeDeleted = new HashSet<Object>();
								List<Object> toBeStored = new ArrayList<Object>();
								Utilities.updateCollection(dbManager, current, result, toBeDeleted, toBeStored);
								if (result.getAlbum()) {
									AomList<String> assetIds = current.getAsset();
									if (assetIds != null) {
										result.setAsset(assetIds);
										Utilities.updateAlbumAssets(dbManager, current.getName(), result.getName(),
												assetIds.toArray(new String[assetIds.size()]), null, toBeStored, null);
									}
								}
								dbManager.safeTransaction(toBeDeleted, toBeStored);
								setInput();
								viewer.setSelection(new StructuredSelection(result), true);
							}

						};
						BusyIndicator.showWhile(viewer.getControl().getDisplay(), runnable);
					}
				} else if (obj instanceof SlideShowImpl) {
					SlideShowImpl current = (SlideShowImpl) obj;
					SlideShowImpl backup = SlideshowPropertiesOperation.cloneSlideshow(current);
					Object parent = ((ITreeContentProvider) viewer.getContentProvider()).getParent(obj);
					SlideshowEditDialog dialog = new SlideshowEditDialog(getSite().getShell(), null, current,
							Messages.getString("CatalogView.edit_slide_show"), //$NON-NLS-1$
							false, true);
					if (dialog.open() == Window.OK) {
						SlideShowImpl result = dialog.getResult();
						viewer.refresh(parent);
						viewer.setSelection(new StructuredSelection(result), true);
						performOperation(new SlideshowPropertiesOperation(backup, result));
						openSlideShowEditor(result, true);
					}
				} else if (obj instanceof ExhibitionImpl) {
					ExhibitionImpl current = (ExhibitionImpl) obj;
					Object parent = ((ITreeContentProvider) viewer.getContentProvider()).getParent(obj);
					ExhibitionImpl backup = ExhibitionPropertiesOperation.cloneExhibition(current);
					ExhibitionImpl show = ExhibitionEditDialog.open(getSite().getShell(), null, current,
							Messages.getString("CatalogView.edit_exhibition"), true, null); //$NON-NLS-1$
					if (show != null) {
						performOperation(new ExhibitionPropertiesOperation(backup, show));
						viewer.refresh(parent);
						viewer.setSelection(new StructuredSelection(show), true);
						openExhibitionEditor(show, true);
					}
				} else if (obj instanceof WebGalleryImpl) {
					WebGalleryImpl current = (WebGalleryImpl) obj;
					Object parent = ((ITreeContentProvider) viewer.getContentProvider()).getParent(obj);
					WebGalleryImpl backup = WebGalleryPropertiesOperation.cloneGallery(current);
					WebGalleryImpl result = WebGalleryEditDialog.openWebGalleryEditDialog(getSite().getShell(), null,
							current, Messages.getString("CatalogView.edit_web_gallery"), false, true, null); //$NON-NLS-1$
					if (result != null) {
						viewer.refresh(parent);
						performOperation(new WebGalleryPropertiesOperation(backup, result));
						viewer.setSelection(new StructuredSelection(result), true);
						openWebGalleryEditor(result, true);
					}
				} else if (obj instanceof GroupImpl) {
					final GroupImpl current = (GroupImpl) obj;
					String oldAnno = current.getAnnotations();
					GroupDialog dialog = new GroupDialog(getSite().getShell(), current,
							current.getGroup_subgroup_parent());
					if (dialog.open() == Window.OK) {
						current.setName(dialog.getName());
						String newAnno = dialog.getAnnotations();
						current.setAnnotations(newAnno);
						current.setShowLabel(dialog.getShowLabel());
						current.setLabelTemplate(dialog.getLabelTemplate());
						dbManager.safeTransaction(null, current);
						if (oldAnno != null && !oldAnno.equals(newAnno) || oldAnno == null && newAnno != null)
							refresh();
						else
							viewer.update(current, null);
					}
				}
			}

			private SmartCollection editCollection(SmartCollection current) {
				AomList<Criterion> criterion = current.getCriterion();
				if (criterion != null && !criterion.isEmpty()) {
					String field = current.getCriterion(0).getField();
					if (field.equals(QueryField.EXIF_GPSLOCATIONDISTANCE.getKey())) {
						ProximityEditDialog dialog = new ProximityEditDialog(getSite().getShell(), current);
						return (dialog.open() == Window.OK) ? dialog.getResult() : null;
					}
					IDbFactory dbFactory = Core.getCore().getDbFactory();
					if ((field.equals(ICollectionProcessor.TEXTSEARCH) || field.equals(ICollectionProcessor.SIMILARITY))
							&& dbFactory.getLireServiceVersion() >= 0)
						return dbFactory.getLireService(true).updateQuery(current, null, AbstractCatalogView.this,
								field);
				}
				boolean networkPossible = !current.getSystem() && !current.getAlbum() && !UiUtilities.isImport(current);
				String title;
				if (current.getGroup_rootCollection_parent() == null) {
					SmartCollection parent = current.getSmartCollection_subSelection_parent();
					parent.getName();
					title = NLS.bind(Messages.getString("CatalogView.edit_subselection"), parent.getName()); //$NON-NLS-1$
				} else
					title = current.getAlbum() ? Messages.getString("CatalogView.edit_album") //$NON-NLS-1$
							: Messages.getString("CatalogView.edit_collection"); //$NON-NLS-1$
				String message = current.getAlbum()
						? current.getSystem() ? Messages.getString("AbstractCatalogView.person_album_msg") //$NON-NLS-1$
								: Messages.getString("AbstractCatalogView.album_msg") //$NON-NLS-1$
						: current.getSystem() ? Messages.getString("AbstractCatalogView.system_collection_msg") //$NON-NLS-1$
								: Messages.getString("AbstractCatalogView.collection_msg"); //$NON-NLS-1$
				CollectionEditDialog dialog = new CollectionEditDialog(getSite().getShell(), current, title, // $NON-NLS-1$
						message, // $NON-NLS-1$
						false, current.getAlbum(), false, networkPossible);
				return (dialog.open() == Window.OK) ? dialog.getResult() : null;
			}
		};
		editItemAction.setToolTipText(Messages.getString("CatalogView.edit_selected_item")); //$NON-NLS-1$

		playSlideshowAction = new Action(Messages.getString("CatalogView.play_slideshow"), Icons.play.getDescriptor()) { //$NON-NLS-1$
			@Override
			public void run() {
				IStructuredSelection selection = viewer.getStructuredSelection();
				Object obj = selection.getFirstElement();
				if (obj instanceof SlideShowImpl) {
					List<SlideImpl> slides = new ArrayList<SlideImpl>(300);
					SlideShowImpl slideshow = (SlideShowImpl) obj;
					for (String id : slideshow.getEntry()) {
						SlideImpl object = Core.getCore().getDbManager().obtainById(SlideImpl.class, id);
						if (object != null)
							slides.add(object);
					}
					SlideShowPlayer player = new SlideShowPlayer();
					player.init(getSite().getWorkbenchWindow(), slideshow, slides, false);
					player.open(0);
				} else if (obj instanceof SmartCollection)
					ZoomActionFactory.SLIDESHOW.create(null, AbstractCatalogView.this).run();
			}
		};
		playSlideshowAction.setToolTipText(Messages.getString("CatalogView.edit_catalog_properties")); //$NON-NLS-1$
		splitCatAction = addAction(ZoomActionFactory.SPLITCATALOG.create(null, this));
	}

	@Override
	protected void registerCommands() {
		registerCommand(editItemAction, IZoomCommandIds.EditCommand);
		registerCommand(playSlideshowAction, IZoomCommandIds.AdhocSlideshowCommand);
		registerCommand(selectionActionCluster.getAction(SelectionActionCluster.SELECTALL),
				IWorkbenchCommandConstants.EDIT_SELECT_ALL);
		registerCommand(selectionActionCluster.getAction(SelectionActionCluster.SELECTNONE), IZoomCommandIds.Deselect);
		registerCommand(selectionActionCluster.getAction(SelectionActionCluster.REVERT), IZoomCommandIds.Revert);
		registerCommand(splitCatAction, IZoomCommandIds.SplitCatalogCommand);
		super.registerCommands();
	}

	protected void setInput() {
		refresh();
	}

	protected void addEnabled(IMenuManager manager, IAction action) {
		if (action.isEnabled())
			manager.add(action);
	}

	protected void addCtrlKeyListener() {
		getControl().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CTRL)
					cntrlDwn = true;
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.CTRL)
					cntrlDwn = false;
			}
		});
	}

	protected IStatus performOperation(IUndoableOperation op) {
		if (operationHistory == null) {
			operationHistory = PlatformUI.getWorkbench().getOperationSupport().getOperationHistory();
			operationHistory.addOperationHistoryListener(this);
		}
		op.addContext(undoContext);
		try {
			return operationHistory.execute(op, null, this);
		} catch (ExecutionException e) {
			return new Status(IStatus.ERROR, UiActivator.PLUGIN_ID,
					NLS.bind(Messages.getString("AbstractPresentationView.cannot_execute_operation"), op //$NON-NLS-1$
							.getLabel()),
					e);
		}
	}

	public void historyNotification(OperationHistoryEvent event) {
		if (event.getEventType() == OperationHistoryEvent.UNDONE
				|| event.getEventType() == OperationHistoryEvent.REDONE)
			refreshAfterHistoryEvent(event.getOperation());
	}

	private void refreshAfterHistoryEvent(IUndoableOperation operation) {
		if (operation instanceof ExhibitionPropertiesOperation)
			refresh();
	}

}