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
package com.bdaum.zoom.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TableDragSourceEffect;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.meta.Category;
import com.bdaum.zoom.cat.model.meta.CategoryImpl;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.core.internal.ai.IAiService;
import com.bdaum.zoom.core.internal.ai.Prediction;
import com.bdaum.zoom.core.internal.ai.Prediction.Token;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.image.ImageStore;
import com.bdaum.zoom.image.ImageUtilities;
import com.bdaum.zoom.operations.internal.CatResult;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.VocabManager;
import com.bdaum.zoom.ui.internal.hover.HoverInfo;
import com.bdaum.zoom.ui.internal.hover.HoveringController;
import com.bdaum.zoom.ui.internal.hover.IGalleryHover;
import com.bdaum.zoom.ui.internal.hover.IHoverInfo;
import com.bdaum.zoom.ui.internal.views.EffectDropTargetListener;
import com.bdaum.zoom.ui.internal.views.IHoverSubject;
import com.bdaum.zoom.ui.internal.views.ImageRegion;
import com.bdaum.zoom.ui.internal.views.ZColumnViewerToolTipSupport;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.CheckedText;
import com.bdaum.zoom.ui.internal.widgets.RadioButtonGroup;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.bdaum.zoom.ui.widgets.CLink;

@SuppressWarnings("restriction")
public class CategorizeDialog extends ZTitleAreaDialog implements IHoverSubject, KeyListener {

	private final class PairLabelProvider extends ZColumnLabelProvider {
		@Override
		public String getText(Object element) {
			if (element instanceof Token)
				return ((Token) element).getMatch() != 0 ? Messages.CategorizeDialog_match : null;
			return element.toString();
		}

		@Override
		protected Color getForeground(Object element) {
			if (element instanceof Token) {
				switch (((Token) element).getMatch()) {
				case CHECKED:
				case PROPOSED:
					return catCanvas.getDisplay().getSystemColor(SWT.COLOR_RED);
				case SUPPLEMENTAL:
				case SUPPROPOSED:
					return catCanvas.getDisplay().getSystemColor(SWT.COLOR_YELLOW);
				}
			}
			return super.getForeground(element);
		}

	}

	private final class CategoryLabelProvider extends ZColumnLabelProvider {
		private VocabManager vManager;

		public CategoryLabelProvider(VocabManager vManager) {
			this.vManager = vManager;
		}

		@Override
		public String getToolTipText(Object element) {
			if (element instanceof Token && UiActivator.getDefault().getShowHover()) {
				Token token = (Token) element;
				String title;
				switch (token.getMatch()) {
				case SUPPROPOSED:
					title = NLS.bind(Messages.CategorizeDialog_as_supplemental, token.getCategory());
					break;
				case PROPOSED:
					title = NLS.bind(Messages.CategorizeDialog_as_primary, token.getCategory());
					break;
				default:
					title = Messages.CategorizeDialog_no_match;
				}
				String label = token.getLabel();
				String mapped = vManager.getVocab(label);
				String warning = mapped == null ? Messages.CategorizeDialog_not_contained_in_vocab
						: mapped.equals(label) ? "" : NLS.bind(Messages.CategorizeDialog_will_be_replaced, mapped); //$NON-NLS-1$
				return NLS.bind(Messages.CategorizeDialog_proposal_hover, new Object[] { label, title,
						predictions.get(currentAsset.getStringId()).getServiceName(), warning });
			}
			return super.getToolTipText(element);
		}

		@Override
		public Image getToolTipImage(Object element) {
			return getImage(element);
		}

		@Override
		public String getText(Object element) {
			return element.toString();
		}

		@Override
		public Image getImage(Object element) {
			if (element instanceof Token) {
				String label = ((Token) element).getLabel();
				String mapped = vManager.getVocab(label);
				if (mapped == null)
					return Icons.warning.getImage();
				else if (!mapped.equals(label))
					return Icons.info.getImage();
			}
			return null;
		}

	}

	public class PredictionJob extends Job {

		private List<Asset> assets;
		private Map<String, Prediction> predictions;

		@Override
		public boolean belongsTo(Object family) {
			return CategorizeDialog.this == family;
		}

		public PredictionJob(List<Asset> assets, Map<String, Prediction> predictions) {
			super(Messages.CategorizeDialog_collecting);
			this.assets = assets;
			this.predictions = predictions;
			setSystem(true);
			setPriority(DECORATE);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if (aiService == null)
				return Status.CANCEL_STATUS;
			monitor.beginTask(Messages.CategorizeDialog_collecting, assets.size() * 2);
			int latency = aiService.getLatency(null);
			for (Asset asset : assets) {
				Prediction prediction = aiService.predict(ImageUtilities.asJpeg(asset.getJpegThumbnail()), null);
				String assetId = asset.getStringId();
				predictions.put(assetId, prediction);
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				predictionReady(assetId);
				monitor.worked(1);
				if (latency > 0)
					try {
						Thread.sleep(latency);
					} catch (InterruptedException e) {
						// do nothing
					}
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				monitor.worked(1);
			}
			return Status.OK_STATUS;
		}
	}

	public class ProposalDropTargetListener extends EffectDropTargetListener {

		private int ops;

		public ProposalDropTargetListener(Control control, int ops) {
			super(control);
			this.ops = ops;
			final DropTarget target = new DropTarget(control, ops);
			target.setTransfer(new Transfer[] { selectionTransfer });
			target.addDropListener(this);
		}

		@Override
		public void dragEnter(DropTargetEvent event) {
			int detail = event.detail;
			event.detail = DND.DROP_NONE;
			for (int i = 0; i < event.dataTypes.length; i++) {
				if (selectionTransfer.isSupportedType(event.dataTypes[i])) {
					event.currentDataType = event.dataTypes[i];
					if ((detail & ops) != 0) {
						event.detail = detail;
						break;
					}
				}
			}
			super.dragEnter(event);
		}

		@Override
		public void dragOver(DropTargetEvent event) {
			event.detail = checkOperations(event);
			super.dragOver(event);
		}

		private int checkOperations(DropTargetEvent event) {
			if (selectionTransfer.isSupportedType(event.currentDataType)) {
				Point p = control.toControl(event.x, event.y);
				Node found = root.findNode(p.x, p.y);
				if (found != null) {
					if (event.detail != DND.DROP_COPY)
						return DND.DROP_MOVE;
					return DND.DROP_COPY;
				}
				if (event.detail == DND.DROP_COPY)
					return DND.DROP_COPY;
			}
			return DND.DROP_NONE;
		}

		@Override
		public void dragOperationChanged(DropTargetEvent event) {
			event.detail = (event.detail & ops) != 0 ? checkOperations(event) : DND.DROP_NONE;
		}

		@Override
		public void drop(DropTargetEvent event) {
			if (selectionTransfer.isSupportedType(event.currentDataType)) {
				StructuredSelection sel = (StructuredSelection) event.data;
				Token token = (Token) sel.getFirstElement();
				if (token != null) {
					Point p = control.toControl(event.x, event.y);
					Node found = root.findNode(p.x, p.y);
					if ((event.detail & DND.DROP_COPY) != 0) {
						if (catBackup == null)
							catBackup = Utilities.cloneCategories(categories);
						Category cat = new CategoryImpl(token.getLabel());
						Category parent = null;
						if (found != null)
							parent = found.getCat();
						EditCategoryDialog dialog = new EditCategoryDialog(getShell(), cat, categories, null, parent,
								true);
						if (dialog.open() == EditCategoryDialog.OK) {
							String label = dialog.getLabel();
							cat.setLabel(label);
							cat.setSynonyms(dialog.getSynonyms());
							Node newNode;
							if (found != null) {
								parent.putSubCategory(cat);
								cat.setCategory_subCategory_parent(parent);
								newNode = new Node(found, cat);
								found.addChild(newNode);
							} else {
								categories.put(label, cat);
								newNode = new Node(root, cat);
								root.addChild(newNode);
							}
							configureTree();
							if (root.has(CHECKED))
								newNode.setChecked(SUPPLEMENTAL, false);
							else {
								root.degrade(PROPOSED, SUPPROPOSED);
								newNode.setChecked(CHECKED, false);
							}
							token.setCategory(label);
							token.setMatch(newNode.getChecked() == CHECKED ? PROPOSED : SUPPROPOSED);
							proposalViewer1.update(token, null);
							proposalViewer.update(token, null);
							drawCat();
						}
					} else if (found != null) {
						found.addSynonym(token.getLabel());
						token.setCategory(found.getCat().getLabel());
						if (found.getChecked() != CHECKED && found.getChecked() != SUPPLEMENTAL) {
							found.setChecked(root.has(CHECKED) ? SUPPLEMENTAL : CHECKED, false);
							drawCat();
						}
						token.setMatch(found.getChecked() == CHECKED ? PROPOSED : SUPPROPOSED);
						proposalViewer1.update(token, null);
						proposalViewer.update(token, null);
					}
					updateButtons();
				}
			}
		}

	}

	public final class ProposalDragSourceListener extends TableDragSourceEffect {
		private CheckboxTableViewer viewer;

		public ProposalDragSourceListener(CheckboxTableViewer viewer) {
			super(viewer.getTable());
			this.viewer = viewer;
		}

		public void dragStart(DragSourceEvent event) {
			IStructuredSelection selection = viewer.getStructuredSelection();
			Object firstElement = selection.getFirstElement();
			if (firstElement != null) {
				event.doit = true;
				selectionTransfer.setSelection(selection);
				event.detail = DND.DROP_MOVE;
				super.dragStart(event);
				return;
			}
			event.doit = false;
		}

		public void dragFinished(DragSourceEvent event) {
			selectionTransfer.setSelection(null);
			super.dragFinished(event);
		}
	}

	public class CatHoverInfo implements IHoverInfo {

		private Node node;

		public CatHoverInfo(Node node) {
			this.node = node;
		}

		@Override
		public String getText() {
			StringBuilder sb = new StringBuilder();
			String[] synonyms = node.getCat().getSynonyms();
			if (synonyms != null && synonyms.length > 0)
				sb.append(Messages.CategorizeDialog_synonyms).append(Core.toStringList(synonyms, ", ")).append('\n'); //$NON-NLS-1$
			sb.append(Messages.CategorizeDialog_set_as);
			switch (node.getChecked()) {
			case CHECKED:
				sb.append(Messages.CategorizeDialog_primaryClick);
				break;
			case SUPPLEMENTAL:
				sb.append(Messages.CategorizeDialog_supplementalClick);
				break;
			case PROPOSED:
				sb.append(Messages.CategorizeDialog_proposedClick);
				break;
			case SUPPROPOSED:
				sb.append(Messages.CategorizeDialog_click_proposed_sup);
				break;
			default:
				sb.append(Messages.CategorizeDialog_nilClick);
				break;
			}
			return sb.toString();
		}

		@Override
		public String getTitle() {
			return NLS.bind(Messages.CategorizeDialog_category, node.cat.getLabel());
		}

		@Override
		public ImageRegion[] getRegions() {
			return null;
		}

		@Override
		public Object getObject() {
			return node;
		}

	}

	public class CatDialogHover implements IGalleryHover {

		@Override
		public IHoverInfo getHoverInfo(IHoverSubject viewer, MouseEvent event) {
			if (event.widget == imageCanvas)
				return new HoverInfo(currentAsset, (ImageRegion[]) null);
			Node found = root.findNode(event.x, event.y);
			if (found != null)
				return new CatHoverInfo(found);
			return null;
		}

	}

	private static final class NodeComparator implements Comparator<Node> {
		@Override
		public int compare(Node n1, Node n2) {
			return n1.getCat().getLabel().compareTo(n2.getCat().getLabel());
		}
	}

	private class Node {
		private Category cat;
		private int level;
		private int pos;
		private int checked;
		private Node parent;
		private List<Node> children;
		private Rectangle rect;
		private Rectangle covered;

		public Node(Node parent, Category cat) {
			this.parent = parent;
			this.cat = cat;
		}

		public void addSynonym(String label) {
			if (cat != null) {
				if (label.equalsIgnoreCase(cat.getLabel()))
					return;
				String[] synonyms = cat.getSynonyms();
				if (synonyms != null)
					for (String syn : synonyms)
						if (label.equalsIgnoreCase(syn))
							return;
				if (catBackup == null)
					catBackup = Utilities.cloneCategories(categories);
				if (synonyms == null)
					cat.setSynonyms(new String[] { label });
				else {
					String[] newSyns = new String[synonyms.length + 1];
					System.arraycopy(synonyms, 0, newSyns, 0, synonyms.length);
					newSyns[synonyms.length] = label;
					cat.setSynonyms(newSyns);
				}
			}
		}

		public int getChecked() {
			return checked;
		}

		public void setChecked(int checked, boolean deep) {
			this.checked = checked;
			if (deep && children != null)
				for (Node child : children)
					child.setChecked(checked, deep);
		}

		public Category getCat() {
			return cat;
		}

		public void addChild(Node child) {
			if (children == null)
				children = new ArrayList<>();
			children.add(child);
		}

		public void configureTree(Point dim, int level) {
			pos = dim.y;
			this.level = level;
			dim.x = Math.max(level, dim.x);
			if (children != null && !children.isEmpty()) {
				Collections.sort(children, comparator);
				for (Node child : children)
					child.configureTree(dim, level + 1);
			} else
				++dim.y;
		}

		public void draw(GC gc, int x, int y, int distX, int distY) {
			if (level >= 0) {
				int nx = x + distX * level;
				int ny = y + distY * pos;
				rect = new Rectangle(nx, ny, 10, 10);
				switch (checked) {
				case CHECKED:
					Color background = gc.getBackground();
					gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_RED));
					gc.fillRectangle(rect);
					gc.setBackground(background);
					break;
				case SUPPLEMENTAL:
					background = gc.getBackground();
					gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_YELLOW));
					gc.fillRectangle(rect);
					gc.setBackground(background);
					break;
				case PROPOSED:
					Color foreground = gc.getForeground();
					gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_RED));
					gc.drawRectangle(rect);
					gc.setForeground(foreground);
					break;
				case SUPPROPOSED:
					foreground = gc.getForeground();
					gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_YELLOW));
					gc.drawRectangle(rect);
					gc.setForeground(foreground);
					break;
				default:
					gc.drawRectangle(rect);
					break;
				}
				Color foreground = gc.getForeground();
				gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_CYAN));
				Point textExtent = gc.textExtent(cat.getLabel());
				gc.drawText(cat.getLabel(), nx + 13, ny + 6);
				if (covered == null)
					covered = new Rectangle(nx, ny, 10, 10);
				covered.x = nx;
				covered.y = ny;
				covered.width = 13 + textExtent.x;
				covered.height = 6 + textExtent.y;
				gc.setForeground(foreground);
				if (parent != null && parent.level >= 0) {
					int px = x + distX * parent.level + 10;
					int nx0 = nx;
					int ny0 = ny + 5;
					if (parent.pos == pos)
						gc.drawLine(px, ny0, nx0, ny0);
					else {
						int mx = (px + nx0) / 2;
						gc.drawLine(mx, ny0, nx0, ny0);
						gc.drawLine(mx, y + distY * parent.pos + 5, mx, ny0);
					}
				}
			}
			if (children != null)
				for (Node child : children)
					child.draw(gc, x, y, distX, distY);
		}

		public Node findNode(int mx, int my) {
			if (covered != null && covered.contains(mx, my))
				return this;
			if (children != null)
				for (Node child : children) {
					Node found = child.findNode(mx, my);
					if (found != null)
						return found;
				}
			return null;
		}

		public Node find(String lab) {
			if (lab != null) {
				if (cat != null) {
					if (lab.equalsIgnoreCase(cat.getLabel()))
						return this;
					String[] synonyms = cat.getSynonyms();
					if (synonyms != null)
						for (String syn : synonyms) {
							if (lab.equalsIgnoreCase(syn))
								return this;
						}
				}
				if (children != null)
					for (Node child : children) {
						Node found = child.find(lab);
						if (found != null)
							return found;
					}
			}
			return null;
		}

		public void degrade(int was, int tobe) {
			if (checked == was)
				checked = tobe;
			if (children != null)
				for (Node child : children)
					child.degrade(was, tobe);
		}

		public String getSelection(List<String> supplemental, int primFlag, int supFlag) {
			String primary = null;
			if (cat != null) {
				if (checked == primFlag)
					primary = cat.getLabel();
				else if (checked == supFlag)
					supplemental.add(cat.getLabel());
			}
			if (children != null)
				for (Node child : children) {
					String prim = child.getSelection(supplemental, primFlag, supFlag);
					if (primary == null)
						primary = prim;
				}
			return primary;
		}

		@Override
		public String toString() {
			return cat.getLabel();
		}

		public void sort() {
			if (children != null) {
				Collections.sort(children, comparator);
				for (Node child : children)
					child.sort();
			}
		}

		public boolean has(int flags) {
			if ((checked & flags) != 0)
				return true;
			if (children != null)
				for (Node child : children)
					if (child.has(flags))
						return true;
			return false;
		}

		public void acceptProposals(boolean hasPrimary) {
			if (checked == SUPPROPOSED || checked == PROPOSED && hasPrimary)
				checked = SUPPLEMENTAL;
			else if (checked == PROPOSED) {
				checked = CHECKED;
				hasPrimary = true;
			}
			if (children != null)
				for (Node child : children)
					child.acceptProposals(hasPrimary);
		}

		public void transferTo(Node root) {
			if (cat != null) {
				Node found = root.find(cat.getLabel());
				if (found != null)
					found.setChecked(checked, false);
			}
			if (children != null)
				for (Node child : children)
					child.transferTo(root);
		}
	}

	private static final int BACK_ID = 101;
	private static final int NEXT_ID = 102;
	private static final int CLONE_ID = 103;
	private static final int DONE_ID = 104;
	private static final int MARGINS = 15;
	private static final int UNCHECKED = 0;
	private static final int PROPOSED = 1;
	private static final int SUPPROPOSED = 2;
	private static final int SUPPLEMENTAL = 4;
	private static final int CHECKED = 8;
	private static final LocalSelectionTransfer selectionTransfer = LocalSelectionTransfer.getTransfer();
	private List<Asset> assets;
	private int current = 0;
	private int size;
	private TextLayout textLayout;
	private Canvas imageCanvas;
	private Canvas catCanvas;
	private Asset currentAsset;
	private ImageStore imageCache;
	private Node root = new Node(null, null);
	private int depth;
	private int width;
	private HoveringController hoveringController;
	private static final NodeComparator comparator = new NodeComparator();
	private Composite comp;
	private CatResult[] result;
	private boolean forcePrimary;
	private CheckboxTableViewer proposalViewer;
	private Composite stack;
	private StackLayout stackLayout;
	private Composite propComp;
	private Composite msgComp;
	private Label msgLabel;
	private boolean cloned;
	private CLink acceptLink;
	private Map<String, Category> categories;
	private Map<String, Category> catBackup;
	private Meta meta;
	private Map<String, Prediction> predictions;
	private IAiService aiService;
	private Set<String> newKeywords = new HashSet<>();
	private RadioButtonGroup newPrivacyGroup;
	private CheckboxButton faceButton;
	private CheckedText descriptionField;
	private Label proposalTitle;
	private int showRegions;
	private ArrayList<ImageRegion> currentRegions;
	private Composite splitComp;
	private CheckboxTableViewer proposalViewer1;
	private CheckboxTableViewer proposalViewer2;
	private List<Rectangle> newFaces;
	private Rectangle imageBounds;
	private VocabManager vocabManager;

	public CategorizeDialog(Shell parentShell, List<Asset> localAssets) {
		super(parentShell, HelpContextIds.CATEGORIZE_DIALOG);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.assets = localAssets;
		this.size = assets.size();
		result = new CatResult[size];
		imageCache = CoreActivator.getDefault().getImageCache();
		meta = dbManager.getMeta(true);
		categories = meta.getCategory();
		constructTree(categories);
		predictions = new HashMap<>(size * 3 / 2);
		aiService = CoreActivator.getDefault().getAiService();
		showRegions = UiActivator.getDefault().getPreferenceStore().getInt(PreferenceConstants.MAXREGIONS);
		startPredictionJob(assets);
	}

	private VocabManager getVocabManager() {
		if (vocabManager == null)
			vocabManager = new VocabManager(Core.getCore().getDbManager().getMeta(true).getVocabularies(), this);
		return vocabManager;
	}

	public void predictionReady(String assetId) {
		if (currentAsset != null && assetId == currentAsset.getStringId()) {
			final Shell shell = getShell();
			if (!shell.isDisposed())
				shell.getDisplay().asyncExec(() -> {
					if (!shell.isDisposed())
						fillValues(false);
				});
		}
	}

	private void startPredictionJob(List<Asset> assets) {
		Job.getJobManager().cancel(this);
		if (aiService != null && aiService.isEnabled())
			new PredictionJob(assets, predictions).schedule();
	}

	private void constructTree(Map<String, Category> categories) {
		root = new Node(null, null);
		if (categories != null) {
			for (Category category : categories.values())
				if (category != null)
					root.addChild(createCatTree(root, category));
			configureTree();
		}
	}

	private void configureTree() {
		root.sort();
		Point dim = new Point(0, 0);
		root.configureTree(dim, -1);
		depth = dim.x;
		width = dim.y;
	}

	private Node createCatTree(Node parent, Category category) {
		Node node = new Node(parent, category);
		Map<String, Category> subCategories = category.getSubCategory();
		if (subCategories != null)
			for (Category sub : subCategories.values())
				if (sub != null)
					node.addChild(createCatTree(node, sub));
		return node;
	}

	@Override
	public void create() {
		super.create();
		getShell().setText(Constants.APPLICATION_NAME);
		setTitle(Messages.CategorizeDialog_categorize_images);
		setMessage(size > 1 ? Messages.CategorizeDialog_select_cats : Messages.CategorizeDialog_select_cats1);
		fillValues(false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		comp = new Composite(area, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout(aiService != null ? 3 : 2, false));
		createImageArea();
		createCategoryArea();
		if (aiService != null)
			createProposalArea();
		hoveringController = new HoveringController(this);
		hoveringController.install();
		return area;
	}

	@SuppressWarnings("unused")
	protected void createProposalArea() {
		Composite proposalArea = new Composite(comp, SWT.NONE);
		proposalArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		proposalArea.setLayout(new GridLayout(1, false));
		proposalTitle = new Label(proposalArea, SWT.NONE);
		proposalTitle.setFont(JFaceResources.getHeaderFont());
		proposalTitle.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		setProposalTitel();
		stack = new Composite(proposalArea, SWT.BORDER);
		stack.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		stackLayout = new StackLayout();
		stack.setLayout(stackLayout);
		PairLabelProvider pairLabelProvider = new PairLabelProvider();
		CategoryLabelProvider catLabelProvider = new CategoryLabelProvider(getVocabManager());
		ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateButtons();
			}
		};
		// Unsplit version
		propComp = new Composite(stack, SWT.NONE);
		propComp.setLayout(new GridLayout(2, false));
		Label label = new Label(propComp, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		label.setText(Messages.CategorizeDialog_mark_keywords);

		proposalViewer = CheckboxTableViewer.newCheckList(propComp, SWT.V_SCROLL);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.widthHint = 200;
		proposalViewer.getTable().setLayoutData(layoutData);
		TableViewerColumn col1 = new TableViewerColumn(proposalViewer, SWT.NONE);
		col1.getColumn().setWidth(150);
		col1.setLabelProvider(catLabelProvider);
		TableViewerColumn col2 = new TableViewerColumn(proposalViewer, SWT.NONE);
		col2.getColumn().setWidth(70);

		col2.setLabelProvider(pairLabelProvider);
		proposalViewer.setContentProvider(ArrayContentProvider.getInstance());
		proposalViewer.addDragSupport(DND.DROP_MOVE | DND.DROP_COPY, new Transfer[] { selectionTransfer },
				new ProposalDragSourceListener(proposalViewer));
		proposalViewer.addSelectionChangedListener(selectionChangedListener);
		ZColumnViewerToolTipSupport.enableFor(proposalViewer);

		Composite buttonArea = new Composite(propComp, SWT.NONE);
		buttonArea.setLayoutData(new GridData(SWT.END, SWT.BEGINNING, false, false));
		buttonArea.setLayout(new GridLayout(1, false));
		new AllNoneGroup(buttonArea, new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				proposalViewer.setAllChecked(e.widget.getData() == AllNoneGroup.ALL);
				updateButtons();
			}
		});
		Button button = new Button(buttonArea, SWT.PUSH);
		button.setText(Messages.CategorizeDialog_select_unpaired);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Token[] tokens = (Token[]) proposalViewer.getInput();
				for (Token tok : tokens) {
					proposalViewer.setChecked(tok, tok.getMatch() == UNCHECKED);
				}
			}
		});
		// Split version
		splitComp = new Composite(stack, SWT.NONE);
		splitComp.setLayout(new GridLayout(2, false));
		Label catLabel = new Label(splitComp, SWT.NONE);
		catLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false, 2, 1));
		catLabel.setFont(JFaceResources.getBannerFont());
		catLabel.setText(Messages.CategorizeDialog_proposed_as_categories);
		Label catLabel2 = new Label(splitComp, SWT.NONE);
		catLabel2.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false, 2, 1));
		catLabel2.setText(Messages.CategorizeDialog_can_be_marked_as_keywords);
		proposalViewer1 = CheckboxTableViewer.newCheckList(splitComp, SWT.V_SCROLL);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.widthHint = 200;
		layoutData.heightHint = 100;
		proposalViewer1.getTable().setLayoutData(layoutData);
		col1 = new TableViewerColumn(proposalViewer1, SWT.NONE);
		col1.getColumn().setWidth(150);
		col1.setLabelProvider(catLabelProvider);
		col2 = new TableViewerColumn(proposalViewer1, SWT.NONE);
		col2.getColumn().setWidth(70);

		col2.setLabelProvider(pairLabelProvider);
		proposalViewer1.setContentProvider(ArrayContentProvider.getInstance());
		proposalViewer1.addDragSupport(DND.DROP_MOVE | DND.DROP_COPY, new Transfer[] { selectionTransfer },
				new ProposalDragSourceListener(proposalViewer1));
		proposalViewer1.addSelectionChangedListener(selectionChangedListener);
		ColumnViewerToolTipSupport.enableFor(proposalViewer1);

		Composite buttonArea1 = new Composite(splitComp, SWT.NONE);
		buttonArea1.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false));
		buttonArea1.setLayout(new GridLayout(1, false));
		new AllNoneGroup(buttonArea1, new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				proposalViewer1.setAllChecked(e.widget.getData() == AllNoneGroup.ALL);
				updateButtons();
			}
		});
		button = new Button(buttonArea1, SWT.PUSH);
		button.setText(Messages.CategorizeDialog_select_unpaired);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Token[] tokens = (Token[]) proposalViewer1.getInput();
				for (Token tok : tokens)
					proposalViewer1.setChecked(tok, tok.getMatch() == UNCHECKED);
			}
		});
		Label keyLabel = new Label(splitComp, SWT.NONE);
		keyLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, true, 2, 1));
		keyLabel.setFont(JFaceResources.getBannerFont());
		keyLabel.setText(Messages.CategorizeDialog_proposed_as_keywords);
		proposalViewer2 = CheckboxTableViewer.newCheckList(splitComp, SWT.V_SCROLL);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.widthHint = 200;
		layoutData.heightHint = 300;
		proposalViewer2.getTable().setLayoutData(layoutData);
		proposalViewer2.setLabelProvider(new ColumnLabelProvider());
		proposalViewer2.setContentProvider(ArrayContentProvider.getInstance());
		proposalViewer2.addSelectionChangedListener(selectionChangedListener);

		Composite buttonArea2 = new Composite(splitComp, SWT.NONE);
		buttonArea2.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false));
		buttonArea2.setLayout(new GridLayout(1, false));
		new AllNoneGroup(buttonArea2, new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				proposalViewer2.setAllChecked(e.widget.getData() == AllNoneGroup.ALL);
				updateButtons();
			}
		});
		// Messages
		msgComp = new Composite(stack, SWT.NONE);
		msgComp.setLayout(new GridLayout(1, false));
		msgLabel = new Label(msgComp, SWT.CENTER | SWT.WRAP);
		GridData ldata = new GridData(SWT.FILL, SWT.CENTER, true, true);
		ldata.heightHint = 30;
		msgLabel.setLayoutData(ldata);

		CLink aiLink = new CLink(proposalArea, SWT.NONE);
		aiLink.setText(Messages.CategorizeDialog_configure);
		aiLink.setToolTipText(Messages.CategorizeDialog_user_preferences);
		aiLink.addListener(new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (aiService != null) {
					boolean wasEnabled = aiService.isEnabled();
					if (wasEnabled)
						Job.getJobManager().cancel(this);
					if (aiService.configure(getShell())) {
						boolean enabled = aiService.isEnabled();
						setProposalTitel();
						List<Asset> reordered;
						int p = assets.indexOf(currentAsset);
						reordered = p > 0 ? assets.subList(p, assets.size()) : assets;
						for (Asset asset : reordered)
							predictions.remove(asset.getStringId());
						fillValues(false);
						// TODO could be improved when language can be
						// controlled through API
						if (enabled)
							startPredictionJob(reordered);
					}
				}
			}
		});
	}

	@SuppressWarnings("unused")
	protected void createCategoryArea() {
		Composite catArea = new Composite(comp, SWT.NONE);
		catArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		catArea.setLayout(new GridLayout(1, false));
		Label catLabel = new Label(catArea, SWT.NONE);
		catLabel.setFont(JFaceResources.getHeaderFont());
		catLabel.setText(Messages.CategorizeDialog_categories);
		textLayout = new TextLayout(getShell().getDisplay());
		textLayout.setAlignment(SWT.CENTER);
		catCanvas = new Canvas(catArea, SWT.BORDER);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.widthHint = Math.min(800, depth * 200);
		layoutData.heightHint = Math.min(600, width * 20);
		catCanvas.setLayoutData(layoutData);
		catCanvas.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				Rectangle clientArea = catCanvas.getClientArea();
				e.gc.fillRectangle(clientArea);
				root.draw(e.gc, MARGINS, MARGINS, Math.max(20, (clientArea.width - 2 * MARGINS) / (depth + 1)),
						Math.max(15, (clientArea.height - 2 * MARGINS) / (width + 1)));
			}
		});
		catCanvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				boolean alt = (e.stateMask & SWT.ALT) == SWT.ALT;
				Node found = findObject(e);
				if (found != null) {
					int check = found.getChecked();
					switch (check) {
					case CHECKED:
						found.setChecked(alt ? SUPPLEMENTAL : UNCHECKED, false);
						break;
					case SUPPLEMENTAL:
					case PROPOSED:
						if (alt)
							found.setChecked(UNCHECKED, false);
						else {
							root.degrade(PROPOSED, SUPPROPOSED);
							root.degrade(CHECKED, SUPPLEMENTAL);
							found.setChecked(CHECKED, false);
						}
						break;
					case SUPPROPOSED:
						found.setChecked(alt ? UNCHECKED : SUPPLEMENTAL, false);
						break;
					default:
						if (alt || forcePrimary) {
							root.degrade(PROPOSED, SUPPROPOSED);
							root.degrade(CHECKED, SUPPLEMENTAL);
							found.setChecked(CHECKED, false);
						} else
							found.setChecked(SUPPLEMENTAL, false);
						break;
					}
					drawCat();
					forcePrimary = false;
				}
			}
		});
		catCanvas.addKeyListener(this);
		new ProposalDropTargetListener(catCanvas, DND.DROP_MOVE | DND.DROP_COPY);

		Composite linkArea = new Composite(catArea, SWT.NONE);
		linkArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		linkArea.setLayout(layout);

		CLink catLink = new CLink(linkArea, SWT.NONE);
		catLink.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));
		catLink.setText(Messages.CategorizeDialog_configure);
		catLink.setToolTipText(Messages.CategorizeDialog_configure_tooltip);
		catLink.addListener(new Listener() {

			@Override
			public void handleEvent(Event event) {
				final Shell shell = getShell();
				BusyIndicator.showWhile(shell.getDisplay(), () -> {
					IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					if (activeWorkbenchWindow != null) {
						IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
						if (activePage != null) {
							EditMetaDialog mdialog = new EditMetaDialog(shell, activePage,
									Core.getCore().getDbManager(), false, null);
							mdialog.setInitialPage(EditMetaDialog.CATEGORIES);
							if (catBackup != null)
								mdialog.setCategories(catBackup);
							if (mdialog.open() == EditMetaDialog.OK) {
								categories = mdialog.getCategories();
								catBackup = null;
								Node oldRoot = root;
								constructTree(categories);
								oldRoot.transferTo(root);
								drawCat();
								updateButtons();
							}
						}
					}
				});
			}
		});
		acceptLink = new CLink(linkArea, SWT.NONE);
		acceptLink.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
		acceptLink.setText(Messages.CategorizeDialog_accept_proposals);
		acceptLink.setToolTipText(Messages.CategorizeDialog_accept_proposals_tooltip);
		acceptLink.addListener(new Listener() {
			@Override
			public void handleEvent(Event event) {
				root.acceptProposals(root.has(CHECKED));
				drawCat();
				updateButtons();
			}
		});
	}

	protected void createImageArea() {
		Composite imageArea = new Composite(comp, SWT.NONE);
		imageArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		imageArea.setLayout(new GridLayout(1, false));
		Label imageLabel = new Label(imageArea, SWT.NONE);
		imageLabel.setFont(JFaceResources.getHeaderFont());
		imageLabel.setText(Messages.CategorizeDialog_image);
		imageCanvas = new Canvas(imageArea, SWT.BORDER);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.widthHint = 350;
		layoutData.heightHint = 450;
		imageCanvas.setLayoutData(layoutData);
		imageCanvas.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				Image image = imageCache.getImage(currentAsset);
				imageBounds = image.getBounds();
				int imageSize = Math.max(imageBounds.width, imageBounds.height);
				Rectangle clientArea = imageCanvas.getClientArea();
				GC gc = e.gc;
				gc.fillRectangle(clientArea);
				int canvasSize = Math.max(10, Math.min(clientArea.width, clientArea.height) - 2 * MARGINS);
				double factor = (double) Math.min(320, canvasSize) / imageSize;
				int destWidth = (int) (imageBounds.width * factor);
				int destHeight = (int) (imageBounds.height * factor);
				int destX = (clientArea.width - destWidth) / 2;
				int destY = (Math.min(350, clientArea.height) - destHeight) / 2;
				gc.drawImage(image, 0, 0, imageBounds.width, imageBounds.height, destX, destY, destWidth, destHeight);
				currentRegions = null;
				if (showRegions > 0) {
					Color color = gc.getForeground();
					currentRegions = UiUtilities.drawRegions(gc, currentAsset, destX, destY, destWidth, destHeight,
							true, showRegions, true, null);
					gc.setForeground(color);
				}
				if (newFaces != null) {
					Color color = gc.getForeground();
					gc.setForeground(e.display.getSystemColor(SWT.COLOR_GREEN));
					for (Rectangle rectangle : newFaces) {
						int fWidth = (int) (rectangle.width * factor);
						int fHeight = (int) (rectangle.height * factor);
						int fx = (int) (rectangle.x * factor);
						int fy = (int) (rectangle.y * factor);
						gc.drawRectangle(fx + destX, fy + destY, fWidth, fHeight);
					}
					gc.setForeground(color);
				}
				StringBuilder sb = new StringBuilder();
				String title = currentAsset.getTitle();
				if (title == null || title.isEmpty())
					title = currentAsset.getName();
				sb.append(title).append('\n');
				String headline = currentAsset.getHeadline();
				if (headline != null && !headline.isEmpty())
					sb.append(headline).append('\n');
				String description = currentAsset.getImageDescription();
				if (description != null && !description.isEmpty())
					sb.append(description).append('\n');
				String[] keyword = currentAsset.getKeyword();
				if (keyword != null && keyword.length > 0)
					sb.append(Core.toStringList(keyword, ", ")).append('\n'); //$NON-NLS-1$
				sb.append("\n\n").append(NLS.bind(Messages.CategorizeDialog_x_of_y, current + 1, size)); //$NON-NLS-1$
				textLayout.setWidth(destWidth);
				textLayout.setText(sb.toString());
				textLayout.draw(gc, destX, destY + destHeight + MARGINS, -1, -1, null, null);
				String safety = QueryField.SAFETY.value2text(QueryField.SAFETY.obtainFieldValue(currentAsset), ""); //$NON-NLS-1$
				Point tx = gc.textExtent(safety);
				gc.drawText(safety, clientArea.width - tx.x - 5, clientArea.height - tx.y - 5, true);

			}
		});
		imageCanvas.addKeyListener(this);
		CGroup subImageArea = UiUtilities.createGroup(imageArea, 2, Messages.CategorizeDialog_settings);
		if (aiService != null)
			faceButton = WidgetFactory.createCheckButton(subImageArea, Messages.CategorizeDialog_use_new_faces,
					new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		new Label(subImageArea, SWT.NONE).setText(Messages.CategorizeDialog_new_description);
		descriptionField = new CheckedText(subImageArea, SWT.WRAP);
		GridData ldata = new GridData(SWT.FILL, SWT.CENTER, true, false);
		ldata.heightHint = 30;
		descriptionField.setLayoutData(ldata);
		new Label(subImageArea, SWT.NONE).setText(Messages.CategorizeDialog_privacy);
		newPrivacyGroup = new RadioButtonGroup(subImageArea, null, SWT.HORIZONTAL, Messages.CategorizeDialog_public,
				Messages.CategorizeDialog_medium, Messages.CategorizeDialog_private);
		newPrivacyGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	}

	private void setProposalTitel() {
		if (aiService != null)
			proposalTitle.setText(
					aiService.isEnabled() ? aiService.getTitle(null) : Messages.CategorizeDialog_service_disabled);
	}

	private void fillValues(boolean clear) {
		currentAsset = assets.get(current);
		int safety = currentAsset.getSafety();
		Prediction prediction = null;
		newFaces = null;
		String imageDescription = currentAsset.getImageDescription();
		boolean markKnownOnly = true;
		float markAbove = 0.99f;
		descriptionField.setText(imageDescription == null ? "" : imageDescription); //$NON-NLS-1$
		if (aiService != null && aiService.isEnabled()) {
			markKnownOnly = aiService.getMarkKnownOnly(null);
			markAbove = aiService.getMarkAbove(null);
			prediction = predictions.get(currentAsset.getStringId());
			if (prediction == null) {
				msgLabel.setText(Messages.CategorizeDialog_query_pending);
				stackLayout.topControl = msgComp;
				newFaces = null;
				faceButton.setEnabled(false);
				faceButton.setSelection(false);
			} else {
				if (prediction.getConcepts() != null) {
					if (prediction.getKeywords() != null) {
						proposalViewer1.setInput(prediction.getConcepts());
						proposalViewer2.setInput(prediction.getKeywords());
						stackLayout.topControl = splitComp;
					} else {
						proposalViewer.setInput(prediction.getConcepts());
						stackLayout.topControl = propComp;
					}
				} else {
					msgLabel.setText(prediction.getStatus().getMessage());
					stackLayout.topControl = msgComp;
				}
				float safeForWork = Math.max(prediction.getAdultScore(), prediction.getRacyScore());
				if (safeForWork >= 0)
					safety = safeForWork > 0.7 ? QueryField.SAFETY_SAFE
							: safeForWork > 0.3 ? QueryField.SAFETY_MODERATE : QueryField.SAFETY_RESTRICTED;
				if (prediction.getDescription() != null && descriptionField.getText().isEmpty())
					descriptionField.setText(prediction.getDescription());
			}
		} else if (aiService != null) {
			msgLabel.setText(Messages.CategorizeDialog_deactivated);
			stackLayout.topControl = msgComp;
			newFaces = null;
			faceButton.setEnabled(false);
			faceButton.setSelection(false);
		}
		if (stack != null)
			stack.layout();
		if (clear)
			root.setChecked(UNCHECKED, true);
		String primary;
		String[] supplementalCats;
		CatResult currentResult = result[current];
		if (currentResult != null) {
			primary = currentResult.getPrimary();
			supplementalCats = currentResult.getSupplemental();
			newPrivacyGroup.setSelection(currentResult.getPrivacy());
			descriptionField.setText(currentResult.getDescription());
		} else {
			primary = currentAsset.getCategory();
			supplementalCats = currentAsset.getSupplementalCats();
			setPrivacy(newPrivacyGroup, safety);
		}
		for (String sup : supplementalCats)
			if (sup != null && !sup.isEmpty()) {
				Node found = root.find(sup);
				if (found != null)
					found.setChecked(SUPPLEMENTAL, false);
			}
		forcePrimary = true;
		if (primary != null && !primary.isEmpty()) {
			forcePrimary = false;
			Node found = root.find(primary);
			if (found != null)
				found.setChecked(CHECKED, false);
		}
		if (currentResult != null) {
			String proposal = currentResult.getProposal();
			String[] supProposals = currentResult.getSupProposals();
			for (String sup : supProposals)
				if (sup != null && !sup.isEmpty()) {
					Node found = root.find(sup);
					if (found != null)
						found.setChecked(SUPPROPOSED, false);
				}
			Node found = root.find(proposal);
			if (found != null)
				found.setChecked(PROPOSED, false);
			if (prediction != null && proposalViewer != null) {
				Set<String> keywords = currentResult.getKeywords();
				if (keywords != null) {
					Token[] tokens = prediction.getConcepts();
					for (String kw : keywords)
						for (Token token : tokens) {
							if (kw.equals(token.getLabel())) {
								proposalViewer.setChecked(token, true);
								proposalViewer1.setChecked(token, true);
								proposalViewer2.setChecked(token, true);
								break;
							}
						}
				}
			}
			newFaces = currentResult.getNewFaces();
		} else if (prediction != null && proposalViewer != null) {
			boolean hasPrimary = root.has(CHECKED | PROPOSED);
			Node primaryNode = null;
			Token[] tokens = prediction.getConcepts();
			Set<String> keywords = meta.getKeywords();
			for (Token token : tokens) {
				String label = token.getLabel();
				Node found = root.find(label);
				if (found != null && found.getChecked() != CHECKED && found.getChecked() != SUPPLEMENTAL) {
					if (hasPrimary || primaryNode != null && primaryNode != found) {
						found.setChecked(SUPPROPOSED, false);
						token.setMatch(SUPPROPOSED);
					} else {
						found.setChecked(PROPOSED, false);
						token.setMatch(PROPOSED);
						primaryNode = found;
					}
					token.setCategory(found.getCat().getLabel());
					proposalViewer.update(token, null);
					proposalViewer1.update(token, null);
				}
				if (!markKnownOnly || keywords.contains(label) || newKeywords.contains(label)) {
					if (token.score > markAbove) {
						proposalViewer.setChecked(token, true);
						proposalViewer1.setChecked(token, true);
					}
				}
			}
			newFaces = prediction.getFaces();
		}
		drawImage();
		drawCat();
		updateButtons();
	}

	protected void setPrivacy(RadioButtonGroup newPrivacyGroup2, int safety) {
		switch (safety) {
		case QueryField.SAFETY_RESTRICTED:
			newPrivacyGroup2.setSelection(2);
			break;
		case QueryField.SAFETY_MODERATE:
			newPrivacyGroup2.setSelection(1);
			break;
		default:
			newPrivacyGroup2.setSelection(0);
			break;
		}
	}

	private void drawCat() {
		catCanvas.redraw();
	}

	private void drawImage() {
		imageCanvas.redraw();
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		if (size > 1) {
			createButton(parent, BACK_ID, Messages.CategorizeDialog_back, Messages.CategorizeDialog_previous_tooltip,
					false);
			createButton(parent, NEXT_ID, Messages.CategorizeDialog_next, Messages.CategorizeDialog_next_tooltip,
					false);
			createButton(parent, CLONE_ID, Messages.CategorizeDialog_clone, Messages.CategorizeDialog_clone_tooltip,
					true);
		}
		createButton(parent, DONE_ID, Messages.CategorizeDialog_done, Messages.CategorizeDialog_done_tooltip, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL,
				Messages.CategorizeDialog_cancel_tooltip, false);
	}

	private void createButton(Composite parent, int id, String label, String tooltip, boolean defaultButton) {
		createButton(parent, id, label, defaultButton).setToolTipText(tooltip);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
		case BACK_ID:
			processSelection();
			--current;
			cloned = false;
			fillValues(true);
			break;
		case NEXT_ID:
			processSelection();
			++current;
			fillValues(true);
			break;
		case CLONE_ID:
			if (size > 1 && current > 0 && !cloned) {
				result[current] = result[current - 1];
				if (current < size - 1)
					++current;
				else
					cloned = true;
				fillValues(true);
			}
			break;
		case DONE_ID:
			processSelection();
			okPressed();
			break;
		default:
			if (catBackup != null)
				meta.setCategory(catBackup);
			cancelPressed();
		}
	}

	private void processSelection() {
		List<String> supplemental = new ArrayList<>();
		String primary = root.getSelection(supplemental, CHECKED, SUPPLEMENTAL);
		List<String> supProposals = new ArrayList<>();
		String proposal = root.getSelection(supProposals, PROPOSED, SUPPROPOSED);
		Set<String> proposedKeywords = null;
		if (aiService != null) {
			if (stackLayout.topControl == propComp) {
				Object[] checkedElements = proposalViewer.getCheckedElements();
				if (checkedElements.length > 0) {
					proposedKeywords = new HashSet<>(checkedElements.length * 3 / 2);
					for (Object object : checkedElements)
						if (object instanceof Token)
							proposedKeywords.add(((Token) object).getLabel());
					newKeywords.addAll(proposedKeywords);
				}
			}
		}
		result[current] = new CatResult(primary, supplemental.toArray(new String[supplemental.size()]), proposal,
				supProposals.toArray(new String[supProposals.size()]), proposedKeywords, newPrivacyGroup.getSelection(),
				descriptionField.getText(), (faceButton != null && faceButton.getSelection()) ? imageBounds : null,
				newFaces);
	}

	@Override
	public boolean close() {
		Job.getJobManager().cancel(this);
		if (aiService != null) {
			CoreActivator.getDefault().ungetAiService(aiService, null);
			aiService = null;
		}
		if (textLayout != null) {
			textLayout.dispose();
			textLayout = null;
		}
		return super.close();
	}

	private void updateButtons() {
		if (size > 1) {
			getButton(BACK_ID).setEnabled(current > 0);
			getButton(NEXT_ID).setEnabled(current < size - 1);
		}
		Button cloneButton = getButton(CLONE_ID);
		if (cloneButton != null)
			cloneButton.setEnabled(current > 0 && !cloned);
		if (aiService != null && aiService.isEnabled()) {
			acceptLink.setVisible(root.has(SUPPROPOSED | PROPOSED));
			if (aiService.checkFaces(null)) {
				faceButton.setVisible(true);
				boolean enabled = newFaces != null && !newFaces.isEmpty();
				faceButton.setEnabled(enabled);
				int noPersons = currentAsset.getNoPersons();
				if (enabled && noPersons <= newFaces.size()) {
					boolean named = false;
					if (currentRegions != null)
						for (ImageRegion region : currentRegions) {
							String name = region.name;
							if (name != null && !name.isEmpty() && !"?".equals(name)) { //$NON-NLS-1$
								named = true;
								break;
							}
						}
					faceButton.setSelection(!named);
				} else
					faceButton.setSelection(false);
			} else
				faceButton.setVisible(false);
		} else {
			acceptLink.setVisible(false);
			faceButton.setVisible(false);
		}
	}

	public CatResult[] getResult() {
		VocabManager vmanager = getVocabManager();
		for (CatResult catres : result) {
			if (catres != null) {
				Set<String> keywords = catres.getKeywords();
				if (keywords != null) {
					String[] kw = keywords.toArray(new String[keywords.size()]);
					for (String keyword : kw) {
						String vocab = vmanager.getVocab(keyword);
						if (vocab != null) {
							keywords.remove(keyword);
							keywords.add(vocab);
						}
					}
				}
			}
		}
		return result;
	}

	@Override
	public Control getControl() {
		return null;
	}

	@Override
	public String getTooltip(int mx, int my) {
		return null;
	}

	@Override
	public Node findObject(MouseEvent event) {
		return root.findNode(event.x, event.y);
	}

	@Override
	public ImageRegion[] findAllRegions(MouseEvent event) {
		return null;
	}

	@Override
	public IGalleryHover getGalleryHover(MouseEvent event) {
		return new CatDialogHover();
	}

	@Override
	public Control[] getControls() {
		return msgLabel == null ? new Control[] { imageCanvas, catCanvas }
				: new Control[] { imageCanvas, catCanvas, msgLabel };
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.character) {
		case SWT.CR:
			buttonPressed(DONE_ID);
			break;
		case SWT.ESC:
			buttonPressed(IDialogConstants.CANCEL_ID);
			break;
		default:
			switch (e.keyCode & SWT.KEY_MASK) {
			case SWT.ARROW_LEFT:
				if (current > 1)
					buttonPressed(BACK_ID);
				break;
			case SWT.ARROW_RIGHT:
				if (current < size - 1) {
					if ((e.stateMask & SWT.CTRL) == SWT.CTRL) {
						if (current > 1)
							buttonPressed(CLONE_ID);
					} else
						buttonPressed(NEXT_ID);
				}
				break;
			}
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// do nothing
	}

	public Map<String, Category> getCategories() {
		return categories;
	}

	public Map<String, Category> getCatBackup() {
		return catBackup;
	}

}
