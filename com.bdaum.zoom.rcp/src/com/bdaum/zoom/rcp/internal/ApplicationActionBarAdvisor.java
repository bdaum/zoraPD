/*******************************************************************************
 * Copyright (c) 2009-2017 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/

package com.bdaum.zoom.rcp.internal;

import java.io.File;
import java.util.LinkedList;

import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.StatusLineContributionItem;
import org.eclipse.jface.action.StatusLineLayoutData;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.operations.IWorkbenchOperationSupport;

import com.bdaum.zoom.batch.internal.BatchActivator;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.internal.CatLocation;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.QueryOptions;
import com.bdaum.zoom.ui.IZoomActionConstants;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.commands.AbstractCatCommandHandler;
import com.bdaum.zoom.ui.internal.commands.OpenCatalogCommand;

@SuppressWarnings("restriction")
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

	public class ClickableStatusLineContributionItem extends ContributionItem {
		private final static int DEFAULT_CHAR_WIDTH = 45;

		private int charWidth;

		private CLabel label;

		/**
		 * The composite into which this contribution item has been placed. This
		 * will be <code>null</code> if this instance has not yet been
		 * initialized.
		 */
		private Composite statusLine = null;

		private String text = ""; //$NON-NLS-1$

		private int widthHint = -1;

		private IWorkbenchAction handler;

		/**
		 * Creates a status line contribution item with the given id.
		 *
		 * @param id
		 *            the contribution item's id, or <code>null</code> if it is
		 *            to have no id
		 * @param handler
		 *            - click handler
		 */
		public ClickableStatusLineContributionItem(String id, IWorkbenchAction handler) {
			this(id, DEFAULT_CHAR_WIDTH, handler);
		}

		/**
		 * Creates a status line contribution item with the given id that
		 * displays the given number of characters.
		 *
		 * @param id
		 *            the contribution item's id, or <code>null</code> if it is
		 *            to have no id
		 * @param charWidth
		 *            the number of characters to display. If the value is
		 *            CALC_TRUE_WIDTH then the contribution will compute the
		 *            preferred size exactly. Otherwise the size will be based
		 *            on the average character size * 'charWidth'
		 * @param handler
		 *            - click handler
		 * 
		 */
		public ClickableStatusLineContributionItem(String id, int charWidth, IWorkbenchAction handler) {
			super(id);
			this.handler = handler;
			this.charWidth = charWidth;
		}

		@Override
		public void fill(Composite parent) {
			statusLine = parent;
			new Label(parent, SWT.NONE).setText(":"); //$NON-NLS-1$
			label = new CLabel(statusLine, SWT.SHADOW_NONE);
			label.setText(text);
			if (widthHint < 0) {
				// Compute the size base on 'charWidth' average char widths
				GC gc = new GC(statusLine);
				gc.setFont(statusLine.getFont());
				widthHint = gc.getFontMetrics().getAverageCharWidth() * charWidth;
				gc.dispose();
			}
			StatusLineLayoutData data = new StatusLineLayoutData();
			data.widthHint = widthHint;
			label.setLayoutData(data);
			label.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseDown(MouseEvent e) {
					if (getText().length() > 0)
						handler.run();
				}
			});
		}

		public Point getDisplayLocation() {
			if ((label != null) && (statusLine != null))
				return statusLine.toDisplay(label.getLocation());
			return null;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			if (!this.text.equals(text)) {
				this.text = text;
				if (label != null && !label.isDisposed()) {
					label.setText(this.text);
					IContributionManager contributionManager = getParent();
					if (contributionManager != null)
						contributionManager.update(true);
				}
			}
		}
	}

	private static final String MINSCORE = "minscore"; //$NON-NLS-1$
	private static final String MAXHITS = "maxhits"; //$NON-NLS-1$
	private static final String NETWORK = "network"; //$NON-NLS-1$
	private static final String METHOD = "method"; //$NON-NLS-1$
	private StatusLineContributionItem statusLineItem;
	private ClickableStatusLineContributionItem undoStatusLineItem;
	private ClickableStatusLineContributionItem redoStatusLineItem;

	public class OpenAction extends Action {

		private final CatLocation cat;

		public OpenAction(CatLocation cat, int i) {
			this.cat = cat;
			setText(cat == null ? Messages.getString("ApplicationActionBarAdvisor.none") //$NON-NLS-1$
					: NLS.bind("&{0} {1}", i, cat.getLabel())); //$NON-NLS-1$
		}

		@Override
		public void run() {
			if (cat != null) {
				File file = cat.getFile();
				if (file != null && file.exists()) {
					AbstractCatCommandHandler command = new OpenCatalogCommand();
					command.setCatFile(file);
					command.init(getActionBarConfigurer().getWindowConfigurer().getWindow());
					command.run();
				} else
					AcousticMessageDialog.openWarning(null, Messages.getString("ApplicationActionBarAdvisor.open_cat"), //$NON-NLS-1$
							NLS.bind(Messages.getString("ApplicationActionBarAdvisor.cat_does_not_exist"), cat)); //$NON-NLS-1$
			}
		}

	}

	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	@Override
	public IStatus restoreState(IMemento memento) {
		QueryOptions options = new QueryOptions();
		if (memento != null) {
			Integer i = memento.getInteger(MINSCORE);
			if (i != null)
				options.setScore(i);
			i = memento.getInteger(MAXHITS);
			if (i != null)
				options.setMaxHits(i);
			Boolean b = memento.getBoolean(NETWORK);
			if (b != null)
				options.setNetworked(b);
			i = memento.getInteger(METHOD);
			if (i != null)
				options.setMethod(i);
		}
		UiActivator.getDefault().setQueryOptions(options);
		return super.restoreState(memento);
	}

	@Override
	public IStatus saveState(IMemento memento) {
		if (memento != null) {
			QueryOptions queryOptions = UiActivator.getDefault().getQueryOptions();
			memento.putInteger(MINSCORE, queryOptions.getScore());
			memento.putInteger(MAXHITS, queryOptions.getMaxHits());
			memento.putBoolean(NETWORK, queryOptions.isNetworked());
			memento.putInteger(METHOD, queryOptions.getMethod());
		}
		return super.saveState(memento);
	}

	@Override
	protected void makeActions(IWorkbenchWindow window) {
		Action quitAction = new Action(Messages.getString("ApplicationActionBarAdvisor.quit")) { //$NON-NLS-1$
			@Override
			public void run() {
				BatchActivator.setFastExit(false);
				getActionBarConfigurer().getWindowConfigurer().getWindow().getWorkbench().close();
			}
		};
		quitAction.setActionDefinitionId(IWorkbenchCommandConstants.FILE_EXIT);
		quitAction.setToolTipText(
				NLS.bind(Messages.getString("ApplicationActionBarAdvisor.exit_app"), Constants.APPLICATION_NAME)); //$NON-NLS-1$
		quitAction.setId(ActionFactory.QUIT.getId());
		register(quitAction);
		IWorkbenchAction prefAction = ActionFactory.PREFERENCES.create(window);
		prefAction.setActionDefinitionId(IWorkbenchCommandConstants.WINDOW_PREFERENCES);
		register(prefAction);
		register(ActionFactory.PRINT.create(window));
		register(ActionFactory.EDIT_ACTION_SETS.create(window));
		register(ActionFactory.OPEN_PERSPECTIVE_DIALOG.create(window));
		register(ActionFactory.RESET_PERSPECTIVE.create(window));
		register(ActionFactory.SAVE_PERSPECTIVE.create(window));
		register(ActionFactory.CLOSE_ALL_PERSPECTIVES.create(window));
		register(ActionFactory.CLOSE_PERSPECTIVE.create(window));
		IWorkbenchAction helpAction = ActionFactory.HELP_CONTENTS.create(window);
		helpAction.setActionDefinitionId(IWorkbenchCommandConstants.HELP_HELP_CONTENTS);
		register(helpAction);
		register(ActionFactory.HELP_SEARCH.create(window));
		register(ActionFactory.ABOUT.create(window));
		register(ActionFactory.INTRO.create(window));
		statusLineItem = new StatusLineContributionItem("ModeContributionItem"); //$NON-NLS-1$
		undoStatusLineItem = new ClickableStatusLineContributionItem("UndoContributionItem", //$NON-NLS-1$
				ActionFactory.UNDO.create(window));
		redoStatusLineItem = new ClickableStatusLineContributionItem("RedoContributionItem", //$NON-NLS-1$
				ActionFactory.REDO.create(window));
	}
	
	@Override
	protected void fillMenuBar(IMenuManager menuBar) {
		MenuManager catMenu = new MenuManager(Messages.getString("ApplicationActionBarAdvisor.Catalog"), //$NON-NLS-1$
				IZoomActionConstants.M_CATALOG);
		menuBar.add(catMenu);
		catMenu.add(new Separator(IZoomActionConstants.CATALOG_START));
		final MenuManager recentMenu = new MenuManager(
				Messages.getString("ApplicationActionBarAdvisor.recently_opened_cats"), "recentCats"); //$NON-NLS-1$ //$NON-NLS-2$
		recentMenu.setRemoveAllWhenShown(true);
		recentMenu.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				CoreActivator activator = CoreActivator.getDefault();
				File file = activator.getDbManager().getFile();
				CatLocation current = file == null ? null : new CatLocation(file);
				LinkedList<CatLocation> recentCats = activator.getRecentCats();
				int i = 0;
				for (CatLocation cat : recentCats)
					if ((current == null || !current.equals(cat)) && cat.exists())
						recentMenu.add(new OpenAction(cat, ++i));
				if (recentMenu.isEmpty())
					recentMenu.add(new OpenAction(null, 0));
			}
		});
		catMenu.add(recentMenu);
		catMenu.add(new Separator(IZoomActionConstants.CATALOG_EXT1));

		catMenu.add(getAction(ActionFactory.PRINT.getId()));
		catMenu.add(new Separator(IZoomActionConstants.CATALOG_EXT2));
		catMenu.add(new Separator(IZoomActionConstants.CATALOG_EXT3));
		if (!Constants.OSX)
			catMenu.add(getAction(ActionFactory.QUIT.getId()));
		catMenu.add(new Separator(IZoomActionConstants.CATALOG_END));
		MenuManager fileMenu = new MenuManager(Messages.getString("ApplicationActionBarAdvisor.File"), //$NON-NLS-1$
				IWorkbenchActionConstants.M_FILE);
		menuBar.add(fileMenu);
		fileMenu.add(new Separator(IWorkbenchActionConstants.FILE_START));
		fileMenu.add(new Separator(IZoomActionConstants.FILE_EXT1));
		fileMenu.add(new Separator(IZoomActionConstants.FILE_EXT2));
		fileMenu.add(new Separator(IWorkbenchActionConstants.FILE_END));
		MenuManager navigateMenu = new MenuManager(Messages.getString("ApplicationActionBarAdvisor.Navigate"), //$NON-NLS-1$
				IWorkbenchActionConstants.M_NAVIGATE);
		menuBar.add(navigateMenu);
		navigateMenu.add(new Separator(IWorkbenchActionConstants.NAV_START));
		navigateMenu.add(new Separator(IZoomActionConstants.NAV_EXT1));
		navigateMenu.add(new Separator(IZoomActionConstants.NAV_EXT2));
		navigateMenu.add(new Separator(IWorkbenchActionConstants.NAV_END));
		MenuManager editMenu = new MenuManager(Messages.getString("ApplicationActionBarAdvisor.Edit"), //$NON-NLS-1$
				IWorkbenchActionConstants.M_EDIT);
		menuBar.add(editMenu);
		editMenu.add(new Separator(IWorkbenchActionConstants.EDIT_START));
		editMenu.add(new Separator(IWorkbenchActionConstants.EDIT_END));
		MenuManager imageMenu = new MenuManager(Messages.getString("ApplicationActionBarAdvisor.Image"), //$NON-NLS-1$
				IZoomActionConstants.M_IMAGE);
		menuBar.add(imageMenu);
		imageMenu.add(new Separator(IZoomActionConstants.IMAGE_START));
		imageMenu.add(new Separator(IZoomActionConstants.IMAGE_EXT1));
		imageMenu.add(new Separator(IZoomActionConstants.IMAGE_EXT2));
		imageMenu.add(new Separator(IZoomActionConstants.IMAGE_EXT3));
		imageMenu.add(new Separator(IZoomActionConstants.IMAGE_END));
		MenuManager metaMenu = new MenuManager(Messages.getString("ApplicationActionBarAdvisor.Metadata"), //$NON-NLS-1$
				IZoomActionConstants.M_META);
		menuBar.add(metaMenu);
		metaMenu.add(new Separator(IZoomActionConstants.META_START));
		metaMenu.add(new Separator(IZoomActionConstants.META_EXT1));
		metaMenu.add(new Separator(IZoomActionConstants.META_EXT2));
		metaMenu.add(new Separator(IZoomActionConstants.META_END));
		MenuManager windowMenu = new MenuManager(Messages.getString("ApplicationActionBarAdvisor.Window"), //$NON-NLS-1$
				IWorkbenchActionConstants.M_WINDOW);
		menuBar.add(windowMenu);
		windowMenu.add(new Separator(IZoomActionConstants.WINDOW_START));
		windowMenu.add(new Separator(IZoomActionConstants.WINDOW_EXT));
		windowMenu.add(new Separator());
		MenuManager perspective = new MenuManager(Messages.getString("ApplicationActionBarAdvisor.perspective"), //$NON-NLS-1$
				"perspective"); //$NON-NLS-1$
		windowMenu.add(perspective);
		perspective.add(getAction(ActionFactory.OPEN_PERSPECTIVE_DIALOG.getId()));
		perspective.add(getAction(ActionFactory.EDIT_ACTION_SETS.getId()));
		perspective.add(getAction(ActionFactory.RESET_PERSPECTIVE.getId()));
		perspective.add(getAction(ActionFactory.SAVE_PERSPECTIVE.getId()));
		perspective.add(getAction(ActionFactory.CLOSE_PERSPECTIVE.getId()));
		perspective.add(getAction(ActionFactory.CLOSE_ALL_PERSPECTIVES.getId()));
		windowMenu.add(new Separator(IZoomActionConstants.WINDOW_EXT1));
		ActionContributionItem preferencesActionItem = new ActionContributionItem(
				getAction(ActionFactory.PREFERENCES.getId()));
		windowMenu.add(preferencesActionItem);

		MenuManager helpMenu = new MenuManager(Messages.getString("ApplicationActionBarAdvisor.Help"), //$NON-NLS-1$
				IWorkbenchActionConstants.M_HELP);
		menuBar.add(helpMenu);
		helpMenu.add(getAction(ActionFactory.INTRO.getId()));
		helpMenu.add(new Separator(IWorkbenchActionConstants.HELP_START));
		helpMenu.add(getAction(ActionFactory.HELP_CONTENTS.getId()));
		helpMenu.add(getAction(ActionFactory.HELP_SEARCH.getId()));
		helpMenu.add(new Separator(IZoomActionConstants.HELP_EXT));
		helpMenu.add(new Separator(IZoomActionConstants.HELP_UPDATE));
		helpMenu.add(new Separator(IWorkbenchActionConstants.HELP_END));
		// Help
		ActionContributionItem aboutActionItem = new ActionContributionItem(getAction(ActionFactory.ABOUT.getId()));
		helpMenu.add(aboutActionItem);
		if (Constants.OSX) {
			preferencesActionItem.setVisible(false);
			aboutActionItem.setVisible(false);
		}
	}

	@Override
	protected void fillStatusLine(IStatusLineManager statusLine) {
		statusLine.setCancelEnabled(true);
		statusLine.add(undoStatusLineItem);
		statusLine.add(redoStatusLineItem);
		statusLine.add(statusLineItem);
		hookUndoMessenger();
	}

	public void hookUndoMessenger() {
		IWorkbenchOperationSupport operationSupport = PlatformUI.getWorkbench().getOperationSupport();
		final IUndoContext undoContext = operationSupport.getUndoContext();
		operationSupport.getOperationHistory().addOperationHistoryListener(new IOperationHistoryListener() {
			public void historyNotification(OperationHistoryEvent event) {
				IUndoableOperation undoOperation = event.getHistory().getUndoOperation(undoContext);
				IUndoableOperation redoOperation = event.getHistory().getRedoOperation(undoContext);
				final String undolabel = undoOperation == null ? "" //$NON-NLS-1$
						: NLS.bind(Messages.getString("ApplicationActionBarAdvisor.undo"), undoOperation.getLabel()); //$NON-NLS-1$
				final String redolabel = redoOperation == null ? "" //$NON-NLS-1$
						: NLS.bind(Messages.getString("ApplicationActionBarAdvisor.redo"), redoOperation.getLabel()); //$NON-NLS-1$
				IWorkbenchWindow window = getActionBarConfigurer().getWindowConfigurer().getWindow();
				if (window != null) {
					Shell shell = window.getShell();
					if (shell != null && !shell.isDisposed()) {
						final Display display = shell.getDisplay();
						display.asyncExec(new Runnable() {
							@Override
							public void run() {
								if (!display.isDisposed()) {
									undoStatusLineItem.setText(undolabel);
									redoStatusLineItem.setText(redolabel);
								}
							}
						});
					}
				}
			}
		});
	}

}
