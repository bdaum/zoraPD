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

package com.bdaum.zoom.ui.internal.views;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.bdaum.aoModeling.runtime.AomObject;
import com.bdaum.aoModeling.runtime.IIdentifiableObject;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.composedTo.ComposedToImpl;
import com.bdaum.zoom.cat.model.derivedBy.DerivedByImpl;
import com.bdaum.zoom.cat.model.group.CriterionImpl;
import com.bdaum.zoom.cat.model.group.PostProcessorImpl;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.SortCriterionImpl;
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.core.IAssetProvider;
import com.bdaum.zoom.core.IPostProcessor;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.image.ImageUtilities;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.LinkOperation;
import com.bdaum.zoom.operations.internal.ModifyRelationLegendOperation;
import com.bdaum.zoom.operations.internal.RelationDescription;
import com.bdaum.zoom.operations.internal.UnlinkOperation;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.IZoomActionConstants;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.CaptionProcessor;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.actions.ZoomActionFactory;
import com.bdaum.zoom.ui.internal.dialogs.DescriptionDialog;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;

@SuppressWarnings("restriction")
public class HierarchyView extends ImageView implements ISelectionChangedListener, IDragHost, IPropertyChangeListener {

	@SuppressWarnings("serial")
	public static class HierarchyPostProcessor extends PostProcessorImpl implements IPostProcessor {

		private final Node node;

		public HierarchyPostProcessor(Node node) {
			this.node = node;
		}

		@Override
		public IPostProcessor clone() {
			return new HierarchyPostProcessor(node);
		}

		public List<Asset> process(List<Asset> set) {
			removeShownNodes(set, node);
			return set;
		}

		private void removeShownNodes(List<Asset> set, Node parent) {
			Asset a = parent.getAsset();
			if (a != null)
				set.remove(a);
			for (Node child : parent.getChildren())
				removeShownNodes(set, child);
		}

	}

	public class Node {

		private Asset asset;
		private IIdentifiableObject description;
		private List<Node> children;
		private Node parent;
		private int typ;
		private Image image;
		private static final int THUMBSIZE = 56;

		public Node(Node parent, Asset asset, int type) {
			this.asset = asset;
			this.parent = parent;
			this.typ = type;
		}

		public IIdentifiableObject getDescription() {
			return description;
		}

		public void setDescription(AomObject description) {
			this.description = description;
		}

		public Asset getAsset() {
			return asset == null ? null : Core.getCore().getDbManager().obtainAsset(asset.getStringId());
		}

		public List<Node> getChildren() {
			if (children == null) {
				IDbManager dbManager = Core.getCore().getDbManager();
				String assetId = asset.getStringId();
				List<Serializable> list = new ArrayList<Serializable>();
				List<AomObject> descriptions = new ArrayList<AomObject>();
				switch (typ) {
				case Constants.DERIVATIVES:
					for (DerivedByImpl rel : dbManager.obtainObjects(DerivedByImpl.class, "original", assetId, //$NON-NLS-1$
							QueryField.EQUALS)) {
						String derivative = rel.getDerivative();
						if (derivative.indexOf(':') < 0) {
							list.add(derivative);
							descriptions.add(rel);
						}
					}
					break;
				case Constants.ORIGINALS:
					for (DerivedByImpl rel : dbManager.obtainObjects(DerivedByImpl.class, "derivative", assetId, //$NON-NLS-1$
							QueryField.EQUALS)) {
						String original = rel.getOriginal();
						if (original.indexOf(':') < 0) {
							list.add(original);
							descriptions.add(rel);
						}
					}
					break;
				case Constants.COMPOSITES:
					for (ComposedToImpl rel : dbManager.obtainObjects(ComposedToImpl.class, "component", assetId, //$NON-NLS-1$
							QueryField.CONTAINS)) {
						list.add(rel.getComposite());
						descriptions.add(rel);
					}
					break;
				case Constants.COMPONENTS:
					for (ComposedToImpl rel : dbManager.obtainObjects(ComposedToImpl.class, "composite", assetId, //$NON-NLS-1$
							QueryField.EQUALS))
						for (String id : rel.getComponent()) {
							list.add(id);
							descriptions.add(rel);
						}
					break;
				}
				children = new ArrayList<Node>(list.size());
				Iterator<AomObject> it = descriptions.iterator();
				for (Serializable id : list) {
					AomObject descr = it.next();
					AssetImpl a = dbManager.obtainAsset((String) id);
					if (a != null) {
						Node child = new Node(this, a, typ);
						child.setDescription(descr);
						children.add(child);
					}
				}
			}
			return children;
		}

		public Node getParent() {
			return parent;
		}

		public Image getThumbnail() {
			if (asset == null)
				return null;
			if (image == null) {
				Image thumbnail = getImage(asset);
				Rectangle bounds = thumbnail.getBounds();
				double f = (double) THUMBSIZE / Math.max(bounds.width, bounds.height);
				image = ImageUtilities.scaleSWT(thumbnail, (int) (bounds.width * f), (int) (bounds.height * f),
						Platform.getPreferencesService().getBoolean(UiActivator.PLUGIN_ID,
								PreferenceConstants.ADVANCEDGRAPHICS, false, null),
						1, false, thumbnail.getDevice().getSystemColor(SWT.COLOR_GRAY));
			}
			return image;
		}

		public void dispose() {
			if (image != null)
				image.dispose();
			if (children != null)
				for (Node child : children)
					child.dispose();
		}

		public String acceptChildren(String[] ids) {
			for (String id : ids) {
				Asset a = searchInHierarchy(id);
				if (a != null)
					return NLS.bind(Messages.getString("HierarchyView.image_belongs_to_hierarchy"), //$NON-NLS-1$
							a.getName());
				if (isInOppositeHierarchy(id))
					return Messages.getString("HierarchyView.cyclic_relationship"); //$NON-NLS-1$
			}
			OperationJob.executeOperation(new LinkOperation(typ, asset.getStringId(), ids), HierarchyView.this);
			return null;
		}

		public boolean isInOppositeHierarchy(String id) {
			int oppositeType = -1;
			switch (typ) {
			case Constants.DERIVATIVES:
				oppositeType = Constants.ORIGINALS;
				break;
			case Constants.ORIGINALS:
				oppositeType = Constants.DERIVATIVES;
				break;
			case Constants.COMPONENTS:
				oppositeType = Constants.COMPOSITES;
				break;
			case Constants.COMPOSITES:
				oppositeType = Constants.COMPONENTS;
				break;
			}
			Node janus = new Node(null, asset, oppositeType);
			boolean result = searchInHierarchy(janus, id) != null;
			janus.dispose();
			return result;
		}

		private Asset searchInHierarchy(String id) {
			Node root = this;
			while (root.parent != null)
				root = root.parent;
			return searchInHierarchy(root, id);
		}

		private Asset searchInHierarchy(Node node, String id) {
			Asset a = node.getAsset();
			if (a != null && a.getStringId().equals(id))
				return a;
			for (Node child : node.getChildren()) {
				a = searchInHierarchy(child, id);
				if (a != null)
					return a;
			}
			return null;
		}
	}

	public final static String ID_DERIVATIVES = "com.bdaum.zoom.ui.views.HierarchyViewDerivatives"; //$NON-NLS-1$
	public final static String ID_ORIGINALS = "com.bdaum.zoom.ui.views.HierarchyViewOriginals"; //$NON-NLS-1$
	public final static String ID_COMPOSITES = "com.bdaum.zoom.ui.views.HierarchyViewComposites"; //$NON-NLS-1$
	public final static String ID_COMPONENTS = "com.bdaum.zoom.ui.views.HierarchyViewComponents"; //$NON-NLS-1$

	public final static String[] IDS = new String[] { ID_DERIVATIVES, ID_ORIGINALS, ID_COMPOSITES, ID_COMPONENTS };

	public class ViewLabelProvider extends StyledCellLabelProvider {

		@Override
		public void update(ViewerCell cell) {
			Object element = cell.getElement();
			List<StyleRange> styles = new ArrayList<StyleRange>(4);
			Image image = getImage(element);
			cell.setImage(image);
			int w = image == null ? 50 : image.getBounds().width / 6;
			GC gc = new GC(cell.getControl());
			int bw = gc.textExtent(" ").x; //$NON-NLS-1$
			gc.dispose();
			int indent = (w + bw - 1) / bw;
			cell.setText(getText(element, styles, indent));
			cell.setStyleRanges(styles.toArray(new StyleRange[styles.size()]));
		}

		private String getText(Object element, List<StyleRange> styles, int indent) {
			if (element instanceof Node) {
				Node node = (Node) element;
				Asset asset = node.getAsset();
				StringBuilder sb = new StringBuilder();
				insertIndent(sb, indent);
				sb.append(captionProcessor.computeImageCaption(asset, null, null, null, null, false));
				int uriLength = sb.length();
				Date lastModification = asset == null ? null : asset.getLastModification();
				if (lastModification != null) {
					insertIndent(sb, indent + 3);
					sb.append(Messages.getString("HierarchyView.modified")) //$NON-NLS-1$
							.append(Format.YMD_TIME_FORMAT.get().format(lastModification));
				}
				styles.add(new StyleRange(0, uriLength, viewer.getControl().getForeground(), null, SWT.BOLD));
				IIdentifiableObject description = node.getDescription();
				if (description != null) {
					sb.append('\n').append(createDescription(description, indent + 3));
					Color fg = isDescriptionValid(description) ? getSite().getShell().getForeground()
							: getSite().getShell().getDisplay().getSystemColor(SWT.COLOR_RED);
					styles.add(new StyleRange(uriLength, sb.length() - uriLength, fg, null));
				}
				return sb.toString();
			}
			return String.valueOf(element);
		}

		private Image getImage(Object element) {
			if (element instanceof Node)
				return ((Node) element).getThumbnail();
			return null;
		}

		private String createDescription(IIdentifiableObject obj, int ind) {
			StringBuilder sb = new StringBuilder();
			String kind = null;
			String tool = null;
			String recipe = null;
			String parmFile = null;
			boolean archived = false;
			if (obj instanceof ComposedToImpl) {
				ComposedToImpl rel = (ComposedToImpl) obj;
				kind = rel.getType();
				tool = rel.getTool();
				recipe = rel.getRecipe();
				parmFile = rel.getParameterFile();
			} else if (obj instanceof DerivedByImpl) {
				DerivedByImpl rel = (DerivedByImpl) obj;
				tool = rel.getTool();
				recipe = rel.getRecipe();
				parmFile = rel.getParameterFile();
				archived = rel.getArchivedRecipe() != null;
			}
			if (kind != null && !kind.isEmpty()) {
				insertIndent(sb, ind);
				sb.append(kind);
			}
			if (tool != null && !tool.isEmpty()) {
				insertIndent(sb, ind);
				sb.append(Messages.getString("HierarchyView.tool")).append(tool); //$NON-NLS-1$
			}
			if (parmFile != null && !parmFile.isEmpty()) {
				File file = new File(parmFile);
				insertIndent(sb, ind);
				sb.append(Messages.getString("HierarchyView.parameter_file")).append(file.getName()); //$NON-NLS-1$
				if (archived)
					sb.append(Messages.getString("HierarchyView.archived")); //$NON-NLS-1$
			}
			if (recipe != null && !recipe.isEmpty()) {
				insertIndent(sb, ind);
				sb.append(Messages.getString("HierarchyView.recipe")).append(recipe); //$NON-NLS-1$
			}
			return sb.toString();
		}

	}

	private static class ViewContentProvider implements ITreeContentProvider {

		public Object getParent(Object element) {
			if (element instanceof Node)
				return ((Node) element).getParent();
			return null;
		}

		public void dispose() {
			// do nothing
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// do nothing
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof Node)
				return ((Node) parentElement).getChildren().toArray();
			return new Object[0];
		}

		public boolean hasChildren(Object element) {
			return !((Node) element).getChildren().isEmpty();
		}

		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof Node[])
				return (Node[]) inputElement;
			return new Object[0];
		}
	}

	private static final TextTransfer textTransfer = TextTransfer.getInstance();
	private static final Transfer[] transferTypes = new Transfer[] { textTransfer };
	private final static String[] types = new String[] { "derivatives", //$NON-NLS-1$
			"originals", "composites", "components" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private final static int TYPE_DERIVATIVE = 0;
	private final static int TYPE_ORIGINAL = 1;
	private final static int TYPE_COMPOSITES = 2;
	private final static int TYPE_COMPONENTS = 3;
	private static final int OPERATIONS = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;
	@SuppressWarnings("unused")
	private static final String ID = "com.bdaum.zoom.hierarchy"; //$NON-NLS-1$

	private TreeViewer viewer;
	private int type = -1;
	protected Clipboard clipboard;
	protected Node[] rootNode;
	private IAction unlinkAction;
	private IAction descriptionAction;
	private IAction showPossibleOriginalsAction;
	private IAction showPossibleDerivativesAction;
	private IAction showAssetAction;
	protected boolean cntrlDwn;
	private final CaptionProcessor captionProcessor = new CaptionProcessor(Constants.TH_ALL);

	public void insertIndent(StringBuilder sb, int indent) {
		if (sb.length() > 0)
			sb.append('\n');
		for (int i = 0; i < indent; i++)
			sb.append(' ');
	}

	public boolean isDescriptionValid(IIdentifiableObject description) {
		String parmFile = null;
		if (description instanceof ComposedToImpl)
			parmFile = ((ComposedToImpl) description).getParameterFile();
		else if (description instanceof DerivedByImpl)
			parmFile = ((DerivedByImpl) description).getParameterFile();
		if (parmFile != null && !parmFile.isEmpty()) {
			File file;
			try {
				file = new File(new URI(parmFile));
			} catch (URISyntaxException e) {
				file = new File(parmFile);
			}
			return file.exists();
		}
		return true;
	}

	@Override
	public void setInitializationData(IConfigurationElement cfig, String propertyName, Object data) {
		super.setInitializationData(cfig, propertyName, data);
		if (data instanceof String)
			for (int i = 0; i < types.length; i++)
				if (types[i].equals(data)) {
					type = i;
					break;
				}
	}

	@Override
	public void createPartControl(Composite parent) {
		UiActivator.getDefault().getPreferenceStore().addPropertyChangeListener(this);
		viewer = new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		UiUtilities.installDoubleClickExpansion(viewer);
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
		viewer.addSelectionChangedListener(this);
		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), HelpContextIds.HIERARCHY_VIEW);
		// Drag & Drop
		viewer.addDropSupport(OPERATIONS, transferTypes, new ViewerDropAdapter(viewer) {
			@Override
			public void dragEnter(DropTargetEvent event) {
				super.dragEnter(event);
				UiActivator.getDefault().setDropTargetEffect(event, viewer.getControl());
			}

			@Override
			public void dragLeave(DropTargetEvent event) {
				UiActivator.getDefault().hideDropTargetEffect();
				super.dragLeave(event);
			}

			@Override
			public void dragOver(DropTargetEvent event) {
				super.dragOver(event);
			}

			@Override
			public boolean validateDrop(Object target, int operation, TransferData transferType) {
				return (textTransfer.isSupportedType(transferType) && (operation & OPERATIONS) != 0);
			}

			@Override
			public boolean performDrop(Object data) {
				if (data instanceof String) {
					List<String> list = Core.fromStringList((String) data, "\n"); //$NON-NLS-1$
					Object target = getCurrentTarget();
					if (target == null && !list.isEmpty())
						setInput(CoreActivator.getDefault().getDbManager().obtainAsset(list.get(0)));
					else if (target instanceof Node) {
						String[] ids = list.toArray(new String[list.size()]);
						String errorMsg = ((Node) target).acceptChildren(ids);
						if (errorMsg != null) {
							AcousticMessageDialog.openError(getSite().getShell(),
									Messages.getString("HierarchyView.drag_error"), errorMsg); //$NON-NLS-1$
							return false;
						}
					}
				}
				return true;
			}
		});
		// Listeners
		addKeyListener();
		addGestureListener(viewer.getTree());
		addExplanationListener(true);
		addPartListener();
		addClipboard(viewer.getControl());
		addDragSupport();
		// Other actions
		makeActions(getViewSite().getActionBars());
		installListeners();
		hookContextMenu();
		contributeToActionBars();
		// Hover
		installHoveringController();
		// Action state
		updateActions(false);
	}

	protected void addClipboard(Control control) {
		clipboard = new Clipboard(control.getDisplay());
	}

	@Override
	protected void fillLocalPullDown(IMenuManager manager) {
		manager.add(viewImageAction);
		manager.add(new Separator());
		manager.add(editAction);
		manager.add(editWithAction);
		manager.add(new Separator(IZoomActionConstants.MB_HIERARCHY));
		switch (type) {
		case TYPE_DERIVATIVE:
			manager.add(showOriginalAction);
			break;
		case TYPE_ORIGINAL:
			manager.add(showDerivativesAction);
			break;
		case TYPE_COMPOSITES:
			manager.add(showComponentsAction);
			break;
		case TYPE_COMPONENTS:
			manager.add(showCompositesAction);
			break;
		}
		manager.add(showAssetAction);
		manager.add(addBookmarkAction);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	@Override
	protected void fillContextMenu(IMenuManager manager) {
		updateActions(true);
		boolean readOnly = dbIsReadonly();
		manager.add(descriptionAction);
		if (!readOnly)
			manager.add(unlinkAction);
		switch (type) {
		case Constants.ORIGINALS:
			manager.add(showPossibleOriginalsAction);
			break;
		case Constants.DERIVATIVES:
			manager.add(showPossibleDerivativesAction);
			break;
		}
		manager.add(new Separator(IZoomActionConstants.MB_LINK));
		fillEditAndSearchGroup(manager, readOnly);
		fillVoiceNote(manager, readOnly);
		fillMetaData(manager, readOnly);
		manager.add(new Separator(IZoomActionConstants.MB_SUBMENUS));
		fillRotateGroup(manager, readOnly);
		fillShowAndDeleteGroup(manager, readOnly);
		super.fillAdditions(manager);
	}

	@Override
	protected void fillLocalToolBar(IToolBarManager manager) {
		manager.add(viewImageAction);
		manager.add(new Separator());
		manager.add(editAction);
		manager.add(editWithAction);
		manager.add(new Separator(IZoomActionConstants.MB_HIERARCHY));
		switch (type) {
		case TYPE_DERIVATIVE:
			manager.add(showOriginalAction);
			break;
		case TYPE_ORIGINAL:
			manager.add(showDerivativesAction);
			break;
		case TYPE_COMPOSITES:
			manager.add(showComponentsAction);
			break;
		case TYPE_COMPONENTS:
			manager.add(showCompositesAction);
			break;
		}
		manager.add(descriptionAction);
		manager.add(unlinkAction);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	protected void addPartListener() {
		getSite().getWorkbenchWindow().getPartService().addPartListener(new IPartListener() {
			public void partOpened(IWorkbenchPart part) {
				updateView();
			}

			public void partDeactivated(IWorkbenchPart part) {
				// do nothing
			}

			public void partClosed(IWorkbenchPart part) {
				updateView();
			}

			public void partBroughtToTop(IWorkbenchPart part) {
				updateView();
			}

			public void partActivated(IWorkbenchPart part) {
				// do nothing
			}
		});
	}

	@Override
	protected void makeActions(IActionBars bars) {
		super.makeActions(bars);
		showPossibleOriginalsAction = new Action(Messages.getString("HierarchyView.show_candiates_for_originals")) { //$NON-NLS-1$
			@Override
			public void run() {
				Object firstElement = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
				if (firstElement instanceof Node)
					showCandidates((Node) firstElement, true);
			}
		};
		showPossibleOriginalsAction
				.setToolTipText(Messages.getString("HierarchyView.show_candidates_originals_tooltip")); //$NON-NLS-1$
		showPossibleDerivativesAction = new Action(
				Messages.getString("HierarchyView.show_candidates_for_derivatives")) { //$NON-NLS-1$
			@Override
			public void run() {
				Object firstElement = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
				if (firstElement instanceof Node)
					showCandidates((Node) firstElement, false);
			}
		};
		showPossibleDerivativesAction
				.setToolTipText(Messages.getString("HierarchyView.show_candidates_derivatives_tooltip")); //$NON-NLS-1$
		showAssetAction = new Action(Messages.getString("HierarchyView.show_hierarchy_for_selected_asset")) { //$NON-NLS-1$
			@Override
			public void run() {
				setInput(getNavigationHistory().getSelectedAssets().get(0));
			}
		};
		showAssetAction.setToolTipText(Messages.getString("HierarchyView.show_hierarchy_tooltip")); //$NON-NLS-1$
		showDerivativesAction = addAction(ZoomActionFactory.SHOWDERIVATIVES.create(bars, this));
		showOriginalAction = addAction(ZoomActionFactory.SHOWORIGINALS.create(bars, this));
		showCompositesAction = ZoomActionFactory.SHOWCOMPOSITES.create(bars, this);
		showComponentsAction = addAction(ZoomActionFactory.SHOWCOMPONENTS.create(bars, this));
		unlinkAction = new Action(Messages.getString("HierarchyView.unlink"), Icons.unlink.getDescriptor()) { //$NON-NLS-1$
			@Override
			public void run() {
				Object firstElement = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
				if (firstElement instanceof Node) {
					Node node = (Node) firstElement;
					Node parent = node.getParent();
					if (parent != null) {
						Asset a = parent.getAsset();
						if (a != null)
							OperationJob.executeOperation(
									new UnlinkOperation(type, a.getStringId(), node.getAsset().getStringId()),
									HierarchyView.this);
					}
				}
			}
		};
		unlinkAction.setToolTipText(Messages.getString("HierarchyView.remove_from_hierarchy")); //$NON-NLS-1$
		descriptionAction = new Action(Messages.getString("HierarchyView.edit_description"), //$NON-NLS-1$
				Icons.descriptionEdit.getDescriptor()) {
			@Override
			public void run() {
				Object firstElement = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
				if (firstElement instanceof Node) {
					Node node = (Node) firstElement;
					IIdentifiableObject description = node.getDescription();
					DescriptionDialog dialog = new DescriptionDialog(getSite().getShell(), description,
							node.getAsset());
					if (dialog.open() == Window.OK) {
						RelationDescription newValues = dialog.getResult();
						boolean changed = false;
						String kind = ""; //$NON-NLS-1$
						String tool = ""; //$NON-NLS-1$
						String recipe = ""; //$NON-NLS-1$
						String parmFile = ""; //$NON-NLS-1$
						Date createdAt;
						if (description instanceof ComposedToImpl) {
							ComposedToImpl rel = (ComposedToImpl) description;
							if (rel.getType() != null)
								kind = rel.getType();
							if (rel.getTool() != null)
								tool = rel.getTool();
							if (rel.getRecipe() != null)
								recipe = rel.getRecipe();
							if (rel.getParameterFile() != null)
								parmFile = rel.getParameterFile();
							createdAt = rel.getDate();
							changed = kind.equals(newValues.kind) || tool.equals(newValues.tool)
									|| recipe.equals(newValues.recipe) || parmFile.equals(newValues.parameterFile)
									|| createdAt == null || createdAt.equals(newValues.createdAt);
						} else if (description instanceof DerivedByImpl) {
							DerivedByImpl rel = (DerivedByImpl) description;
							if (rel.getTool() != null)
								tool = rel.getTool();
							if (rel.getRecipe() != null)
								recipe = rel.getRecipe();
							if (rel.getParameterFile() != null)
								parmFile = rel.getParameterFile();
							createdAt = rel.getDate();
							changed = tool.equals(newValues.tool) || recipe.equals(newValues.recipe)
									|| parmFile.equals(newValues.parameterFile) || createdAt == null
									|| createdAt.equals(newValues.createdAt);
						}
						if (changed)
							OperationJob.executeOperation(new ModifyRelationLegendOperation(description, newValues),
									HierarchyView.this);
					}
				}
			}
		};
		descriptionAction.setToolTipText(Messages.getString("HierarchyView.edit_link_description")); //$NON-NLS-1$
	}

	protected void showCandidates(Node node, boolean originals) {
		Asset asset = node.getAsset();
		if (asset != null) {
			SmartCollectionImpl collection = new SmartCollectionImpl(
					NLS.bind(originals ? Messages.getString("HierarchyView.possible_originals") //$NON-NLS-1$
							: Messages.getString("HierarchyView.possible_derivatives"), //$NON-NLS-1$
							asset.getName()),
					false, false, true, false, null, 0, null, 0, null, Constants.INHERIT_LABEL, null, 0, 1,
					new HierarchyPostProcessor(node));
			collection.addCriterion(new CriterionImpl(QueryField.EXIF_ORIGINALFILENAME.getKey(), null,
					asset.getOriginalFileName(), null, QueryField.EQUALS, true));
			collection.addCriterion(new CriterionImpl(QueryField.LASTMOD.getKey(), null, asset.getLastModification(),
					null, originals ? QueryField.SMALLER : QueryField.GREATER, true));
			collection.addSortCriterion(new SortCriterionImpl(QueryField.LASTMOD.getKey(), null, originals));
			Ui.getUi().getNavigationHistory(getSite().getWorkbenchWindow())
					.postSelection(new StructuredSelection(collection));
		}
	}

	@Override
	public void assetsModified(BagChange<Asset> changes, QueryField node) {
		if (node == null)
			updateView();
	}

	@Override
	public void hierarchyModified() {
		updateView();
	}

	protected void updateView() {
		if (rootNode != null && rootNode.length > 0)
			setInput(rootNode[0].getAsset());
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public void setInput(final Asset asset) {
		Shell shell = getSite().getShell();
		if (!shell.isDisposed())
			shell.getDisplay().asyncExec(() -> {
				if (!shell.isDisposed()) {
					if (rootNode != null)
						for (Node node : rootNode)
							node.dispose();
					if (!viewer.getControl().isDisposed()) {
						rootNode = new Node[] { new Node(null, asset, type) };
						viewer.setInput(rootNode);
						viewer.expandAll();
					}
				}
			});
	}

	public void selectionChanged(SelectionChangedEvent event) {
		updateActions(false);
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		if (cntrlDwn) {
			Object item = selection.getFirstElement();
			if (item instanceof Node && descriptionAction.isEnabled())
				descriptionAction.run();
			cntrlDwn = false;
		}
		fireSelectionChanged(new SelectionChangedEvent(this, nodeToAssetSelection(selection)));
	}

	private static AssetSelection nodeToAssetSelection(IStructuredSelection selection) {
		AssetSelection assetSelection = new AssetSelection(selection.size());
		Iterator<?> iterator = selection.iterator();
		while (iterator.hasNext()) {
			Object object = iterator.next();
			if (object instanceof Node) {
				Asset a = ((Node) object).getAsset();
				if (a != null)
					assetSelection.add(a);
			}
		}
		return assetSelection;
	}

	@Override
	public void updateActions(boolean force) {
		if (showPossibleDerivativesAction != null && (isVisible() || force) && !viewer.getControl().isDisposed()) {
			boolean writable = !dbIsReadonly();
			super.updateActions(force);
			boolean enabled = false;
			boolean selected = false;
			Object firstElement = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
			if (firstElement instanceof Node) {
				selected = true;
				enabled = (((Node) firstElement).getParent() != null);
			}
			showPossibleDerivativesAction.setEnabled(selected);
			showPossibleOriginalsAction.setEnabled(selected);
			unlinkAction.setEnabled(enabled && writable);
			descriptionAction.setEnabled(enabled);
			showComponentsAction.setEnabled(enabled);
			showCompositesAction.setEnabled(enabled);
			showDerivativesAction.setEnabled(enabled);
			showOriginalAction.setEnabled(enabled);
			showAssetAction.setEnabled(getNavigationHistory().getSelectedAssets().size() == 1);
		}
	}

	private void fireSelectionChanged(SelectionChangedEvent event) {
		for (Object listener : listeners.getListeners())
			((ISelectionChangedListener) listener).selectionChanged(event);
	}

	public void setSelection(ISelection selection) {
		// do nothing
	}

	public ISelection getSelection() {
		return getAssetSelection();
	}

	public Object findObject(MouseEvent e) {
		return findObject(e.x, e.y);
	}

	public Object findObject(int x, int y) {
		ViewerCell cell = viewer.getCell(new Point(x, y));
		Node node = (Node) ((cell == null) ? null : cell.getElement());
		return (node == null) ? null : node.getAsset();
	}

	public AssetSelection getAssetSelection() {
		return nodeToAssetSelection((IStructuredSelection) viewer.getSelection());
	}

	@Override
	protected int getSelectionCount(boolean local) {
		return ((IStructuredSelection) viewer.getSelection()).size();
	}

	public Control getControl() {
		return viewer.getControl();
	}

	@Override
	public void refresh() {
		updateView();
	}

	@Override
	public boolean assetsChanged() {
		updateActions(false);
		return false;
	}

	@Override
	public boolean collectionChanged() {
		return false;
	}

	@Override
	public boolean selectionChanged() {
		return false;
	}

	@Override
	public Object getContent() {
		if (rootNode != null && rootNode.length == 1)
			return rootNode[0].getAsset();
		return super.getContent();
	}

	public boolean cursorOverImage(int x, int y) {
		return true;
	}

	public IAssetProvider getAssetProvider() {
		return null;
	}

	public ImageRegion findBestFaceRegion(int x, int y, boolean all) {
		return null;
	}

	public void dispose() {
		UiActivator.getDefault().getPreferenceStore().removePropertyChangeListener(this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		if (PreferenceConstants.SHOWLABEL.equals(property) || PreferenceConstants.THUMBNAILTEMPLATE.equals(property)
				|| PreferenceConstants.LABELALIGNMENT.equals(property)
				|| PreferenceConstants.LABELFONTSIZE.equals(property)) {
			captionProcessor.updateGlobalConfiguration();
			refresh();
		}
	}

}
