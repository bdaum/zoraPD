package com.bdaum.zoom.ui.internal.actions;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;

import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IAssetProvider;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.Icons.Icon;
import com.bdaum.zoom.ui.internal.views.SelectionActionClusterProvider;

public class SelectionActionCluster {

	public class SelectionAction extends Action {
		private int type;

		public SelectionAction(int i) {
			super(SELECTIONTEXTS[i], SELECTIONICONS[i].getDescriptor());
			this.type = i + UNRATED;
			setToolTipText(SELECTIONTOOLTIPS[i] + Messages.SelectionActionCluster_press_shift_alt);
		}

		@Override
		public void runWithEvent(Event event) {
			if (canHandle())
				provider.select(type, (event.stateMask & SWT.SHIFT) != 0, (event.stateMask & SWT.ALT) != 0);
			else
				delegate(type, event, adaptable);
		}
	}

	public static final int UNRATED = -1;
	public static final int RATED0 = 0;
	public static final int RATED1 = 1;
	public static final int RATED2 = 2;
	public static final int RATED3 = 3;
	public static final int RATED4 = 4;
	public static final int RATED5 = 5;
	public static final int UNCODED = 6;
	public static final int CODEDBLACK = 7;
	public static final int CODEDBLUE = 8;
	public static final int CODEDCYAN = 9;
	public static final int CODEGREEN = 10;
	public static final int CODEDMAGENTA = 11;
	public static final int CODEDORANGE = 12;
	public static final int CODEDPINK = 13;
	public static final int CODEDRED = 14;
	public static final int CODEDVIOLET = 15;
	public static final int CODEDWHITE = 16;
	public static final int CODEDYELLOW = 17;
	public static final int NOTGEODED = 18;
	public static final int GEOCODED = 19;
	public static final int LOCAL = 20;
	public static final int PEER = 21;
	public static final int ORPHAN = 22;
	public static final int SELECTALL = 100;
	public static final int SELECTNONE = 101;
	public static final int REVERT = 102;

	private final static String[] SELECTIONTEXTS = new String[] { Messages.SelectionActionCluster_select_unrated,
			Messages.SelectionActionCluster_select_0, Messages.SelectionActionCluster_select_1,
			Messages.SelectionActionCluster_select_2, Messages.SelectionActionCluster_select_3,
			Messages.SelectionActionCluster_select_4, Messages.SelectionActionCluster_select_5,
			Messages.SelectionActionCluster_select_non_color, Messages.SelectionActionCluster_select_black,
			Messages.SelectionActionCluster_select_blue, Messages.SelectionActionCluster_select_cyan,
			Messages.SelectionActionCluster_select_green, Messages.SelectionActionCluster_select_magenta,
			Messages.SelectionActionCluster_select_orange, Messages.SelectionActionCluster_select_pink,
			Messages.SelectionActionCluster_select_red, Messages.SelectionActionCluster_select_violet,
			Messages.SelectionActionCluster_select_white, Messages.SelectionActionCluster_select_yellow,
			Messages.SelectionActionCluster_select_non_geo, Messages.SelectionActionCluster_select_geo,
			Messages.SelectionActionCluster_select_local, Messages.SelectionActionCluster_select_peer,
			Messages.SelectionActionCluster_select_orphan };

	private final static String[] SELECTIONTOOLTIPS = new String[] { Messages.SelectionActionCluster_select_unrated_tt,
			Messages.SelectionActionCluster_select_0_tt, Messages.SelectionActionCluster_select_1_tt,
			Messages.SelectionActionCluster_select_2_tt, Messages.SelectionActionCluster_select_3_tt,
			Messages.SelectionActionCluster_select_4_tt, Messages.SelectionActionCluster_select_5_tt,
			Messages.SelectionActionCluster_select_non_color_tt, Messages.SelectionActionCluster_select_black_tt,
			Messages.SelectionActionCluster_select_blue_tt, Messages.SelectionActionCluster_select_cyan_tt,
			Messages.SelectionActionCluster_select_green_tt, Messages.SelectionActionCluster_select_magenta_tt,
			Messages.SelectionActionCluster_select_orange_tt, Messages.SelectionActionCluster_select_pink_tt,
			Messages.SelectionActionCluster_select_red_tt, Messages.SelectionActionCluster_select_violet_tt,
			Messages.SelectionActionCluster_select_white_tt, Messages.SelectionActionCluster_select_yellow_tt,
			Messages.SelectionActionCluster_select_non_geo_tt, Messages.SelectionActionCluster_select_geo_tt,
			Messages.SelectionActionCluster_select_local_tt, Messages.SelectionActionCluster_select_peer_tt,
			Messages.SelectionActionCluster_select_orphan_tt };

	private final static Icon[] SELECTIONICONS = new Icon[] { Icons.rating, Icons.rating0, Icons.rating1, Icons.rating2,
			Icons.rating3, Icons.rating4, Icons.rating5, Icons.dashed, Icons.black, Icons.blue, Icons.cyan, Icons.green,
			Icons.magenta, Icons.orange, Icons.pink, Icons.red, Icons.violet, Icons.white, Icons.yellow,
			Icons.nolocation, Icons.location, Icons.local, Icons.network, Icons.orphan };

	private static final int MAXSELECTALL = 500;

	private Action revertSelectionAction, selectNoneAction, selectAllAction;
	private SelectionAction[] selectionActions = new SelectionAction[SELECTIONTEXTS.length];
	private SelectionActionClusterProvider provider;
	private IAdaptable adaptable;

	public SelectionActionCluster(SelectionActionClusterProvider provider, IAdaptable adaptable) {
		this.provider = provider;
		this.adaptable = adaptable;
		selectAllAction = new Action(Messages.SelectionActionCluster_select_all, Icons.selectAll.getDescriptor()) {
			@Override
			public void runWithEvent(Event event) {
				if (canHandle()) {
					IAssetProvider assetProvider = provider.getAssetProvider();
					if (assetProvider != null) {
						int assetCount = assetProvider.getAssetCount();
						if (assetCount > MAXSELECTALL
								&& !AcousticMessageDialog.openConfirm(adaptable.getAdapter(Shell.class),
										Messages.SelectionActionCluster_select_all,
										NLS.bind(Messages.SelectionActionCluster_selecting_a_large_number, assetCount)))
							return;
						provider.selectAll();
					}
				} else
					delegate(SelectionActionCluster.SELECTALL, event, adaptable);
			}

		};
		selectAllAction.setToolTipText(Messages.SelectionActionCluster_select_all_images_in_gallery);

		selectNoneAction = new Action(Messages.SelectionActionCluster_select_none, Icons.selectNone.getDescriptor()) {
			@Override
			public void runWithEvent(Event event) {
				if (canHandle())
					provider.selectNone();
				else
					delegate(SelectionActionCluster.SELECTNONE, event, adaptable);
			}
		};
		selectNoneAction.setToolTipText(Messages.SelectionActionCluster_deselect_tt);

		revertSelectionAction = new Action(Messages.SelectionActionCluster_revert, Icons.revert.getDescriptor()) {
			@Override
			public void runWithEvent(Event event) {
				if (canHandle())
					provider.revertSelection();
				else
					delegate(SelectionActionCluster.REVERT, event, adaptable);
			}
		};
		revertSelectionAction.setToolTipText(Messages.SelectionActionCluster_revert_tt);

		for (int i = 0; i < SELECTIONTEXTS.length; i++)
			selectionActions[i] = new SelectionAction(i);
	}

	private static void delegate(int id, Event event, IAdaptable adaptable) {
		for (IViewReference ref : adaptable.getAdapter(IWorkbenchPage.class).getViewReferences()) {
			IWorkbenchPart part = ref.getPart(false);
			if (part instanceof SelectionActionClusterProvider) {
				SelectionActionCluster cluster = ((SelectionActionClusterProvider) part).getSelectActionCluster();
				if (cluster.canHandle()) {
					part.getSite().getPage().activate(part);
					cluster.getAction(id).runWithEvent(event);
				}
			}
		}
	}

	protected boolean canHandle() {
		return provider != null;
	}

	public void addToActionBars(IActionBars bars) {
		bars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(), selectAllAction);
	}

	public void addToMenuManager(IMenuManager manager) {
		if (canHandle() ? true : checkEnabled()) {
			IMenuManager selectionManager = new MenuManager(Messages.SelectionActionCluster_select);
			manager.add(selectionManager);
			selectionManager.add(selectAllAction);
			selectionManager.add(selectNoneAction);
			selectionManager.add(revertSelectionAction);
			selectionManager.add(new Separator());
			boolean networked = Core.getCore().isNetworked();
			for (int i = 0, t = -1; i < selectionActions.length; i++, t++)
				if (networked || (t != PEER && t != LOCAL)) {
					selectionManager.add(selectionActions[i]);
					if (t == RATED5 || t == CODEDYELLOW || t == GEOCODED || t == PEER)
						selectionManager.add(new Separator());
				}
		}
	}

	public boolean checkEnabled() {
		for (IViewReference ref : adaptable.getAdapter(IWorkbenchPage.class).getViewReferences()) {
			IWorkbenchPart part = ref.getPart(false);
			if (part instanceof SelectionActionClusterProvider) {
				SelectionActionCluster cluster = ((SelectionActionClusterProvider) part).getSelectActionCluster();
				if (cluster.canHandle() && cluster.getAction(SelectionActionCluster.SELECTALL).isEnabled())
					return true;
			}
		}
		return false;
	}

	public void updateActions() {
		boolean enabled = canHandle() ? provider.getAssetProvider() != null : checkEnabled();
		selectAllAction.setEnabled(enabled);
		selectNoneAction.setEnabled(enabled);
		revertSelectionAction.setEnabled(enabled);
		for (Action action : selectionActions)
			action.setEnabled(enabled);
	}

	public IAction getAction(int id) {
		switch (id) {
		case SELECTALL:
			return selectAllAction;
		case SELECTNONE:
			return selectNoneAction;
		case REVERT:
			return revertSelectionAction;
		default:
			return selectionActions[id - UNRATED];
		}
	}

}
