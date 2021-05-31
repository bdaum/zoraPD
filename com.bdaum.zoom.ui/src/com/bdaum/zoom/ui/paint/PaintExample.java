/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Berthold Daum
 *******************************************************************************/
package com.bdaum.zoom.ui.paint;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.UndoContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;

import jdk.nashorn.tools.Shell;

public class PaintExample implements IAdaptable {
	protected static ResourceBundle resourceBundle = ResourceBundle.getBundle("com.bdaum.zoom.ui.paint.messages"); //$NON-NLS-1$
	protected Composite mainComposite;
	protected Canvas activeForegroundColorCanvas;
	protected Color paintColorBlack, paintColorWhite; // alias for paintColors[0] and [1]
	protected Color[] paintColors;
	protected Font paintDefaultFont; // do not free
	protected ToolSettings toolSettings; // current active settings
	protected PaintSurface paintSurface; // paint surface for drawing

	protected static final int Pencil_tool = 0;
	protected static final int Airbrush_tool = 1;
	protected static final int Pipette_tool = 2;

	protected static final int Default_tool = Pencil_tool;

	protected Composite toolbar;
	public Tool[] tools;
	protected Canvas paintCanvas;
	protected Scale airbrushRadiusScale;
	protected Scale airbrushIntensityScale;
	protected ImageFigure imageFigure;
	protected int currentTool;
	protected Color pickedColor;
	protected boolean dirty;
	protected Button restoreButton;
	protected Label intensityLabel;
	protected Label airBrushLabel;
	private List<Figure> vectorLayer = new ArrayList<>(50);
	protected int numPaletteRows;
	protected int numPaletteCols;

	private ListenerList<Listener> listeners = new ListenerList<>();
	protected UndoContext undoContext;
	private IOperationHistory history;
	private Label opacityLabel;
	private Scale opacityScale;

	/**
	 * Creates an instance of a PaintExample embedded inside the supplied parent
	 * Composite.
	 *
	 * @param parent
	 *            the container of the example
	 */
	public PaintExample(Composite parent) {
		mainComposite = parent;
		initResources();
		initActions();
		init();
	}

	public UndoContext getUndoContext() {
		return undoContext;
	}

	/**
	 * Creates the toolbar.
	 */
	@SuppressWarnings("unused")
	public Composite createToolBar(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout());
		Label label = new Label(composite, SWT.NONE);
		label.setText(getResourceString("tool.label")); //$NON-NLS-1$
		ToolBar toolbar = new ToolBar(composite, SWT.VERTICAL);
		toolbar.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
		String group = null;
		for (int i = 0; i < tools.length; i++) {
			Tool tool = tools[i];
			if (tool != null) {
				if (group != null && !tool.group.equals(group)) {
					new ToolItem(toolbar, SWT.SEPARATOR);
				}
				group = tool.group;
				ToolItem item = addToolItem(toolbar, tool);
				if (i == Default_tool)
					item.setSelection(true);
			}
		}
		return composite;
	}

	/**
	 * Adds a tool item to the toolbar.
	 */
	private static ToolItem addToolItem(final ToolBar toolbar, final Tool tool) {
		final String id = tool.group + '.' + tool.key;
		ToolItem item = new ToolItem(toolbar, tool.type);
		// item.setText(getResourceString(id + ".label"));
		item.setToolTipText(getResourceString(id + ".tooltip")); //$NON-NLS-1$
		item.setImage(tool.image);
		item.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				tool.action.run();
			}
		});
		final int childID = toolbar.indexOf(item);
		toolbar.getAccessible().addAccessibleListener(new AccessibleAdapter() {

			@Override
			public void getName(org.eclipse.swt.accessibility.AccessibleEvent e) {
				if (e.childID == childID) {
					e.result = getResourceString(id + ".description"); //$NON-NLS-1$
				}
			}
		});
		tool.item = item;
		return item;
	}

	/**
	 * Sets the default tool item states.
	 */
	public void setDefaults() {
		setPaintTool(Default_tool);
		setForegroundColor(paintColorBlack);
	}

	/**
	 * Creates the GUI.
	 */
	public void createGUI(Composite parent, int w, int h) {
		GridLayout gridLayout;
		GridData gridData;

		/*** Create principal GUI layout elements ***/
		Composite displayArea = new Composite(parent, SWT.NONE);
		gridLayout = new GridLayout(2, false);
		gridLayout.marginTop = 0;
		displayArea.setLayout(gridLayout);

		toolbar = createToolBar(displayArea);

		paintCanvas = new Canvas(displayArea, SWT.BORDER | SWT.NO_REDRAW_RESIZE | SWT.NO_BACKGROUND);
		gridData = new GridData();
		gridData.widthHint = w;
		gridData.heightHint = h;
		gridData.verticalSpan = 2;
		paintCanvas.setLayoutData(gridData);
		paintCanvas.setBackground(paintColorWhite);
		paintCanvas.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_CROSS));
		createButtonBar(displayArea);

		// color selector frame
		final Composite colorFrame = new Composite(displayArea, SWT.NONE);
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);
		gridData.horizontalSpan = 2;
		colorFrame.setLayoutData(gridData);

		// tool settings frame
		final Composite toolSettingsFrame = new Composite(displayArea, SWT.NONE);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_FILL);
		gridData.horizontalSpan = 2;
		toolSettingsFrame.setLayoutData(gridData);

		// status text
		final Text statusText = new Text(displayArea, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);
		gridData.horizontalSpan = 2;
		statusText.setLayoutData(gridData);

		/***
		 * Create the remaining application elements inside the principal GUI layout
		 * elements
		 ***/
		// paintSurface
		paintSurface = new PaintSurface(paintCanvas, w, h, statusText, paintColorWhite);
		// finish initializing the tool data
		tools[Pencil_tool].data = new PencilTool(toolSettings, this);
		tools[Airbrush_tool].data = new AirbrushTool(toolSettings, this);
		tools[Pipette_tool].data = new PipetteTool(toolSettings, this);

		// colorFrame
		gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		colorFrame.setLayout(gridLayout);

		// activeForegroundColorCanvas
		activeForegroundColorCanvas = new Canvas(colorFrame, SWT.BORDER);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.heightHint = 24;
		gridData.widthHint = 24;
		activeForegroundColorCanvas.setLayoutData(gridData);

		// paletteCanvas
		final Canvas paletteCanvas = new Canvas(colorFrame, SWT.BORDER | SWT.NO_BACKGROUND);
		paletteCanvas.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_CROSS));
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.heightHint = 24;
		paletteCanvas.setLayoutData(gridData);
		paletteCanvas.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event e) {
				Rectangle bounds = paletteCanvas.getClientArea();
				setForegroundColor(getColorAt(bounds, e.x, e.y));
			}

			private Color getColorAt(Rectangle bounds, int x, int y) {
				if (bounds.height <= 1 && bounds.width <= 1)
					return paintColorWhite;
				final int row = (y - bounds.y) * numPaletteRows / bounds.height;
				final int col = (x - bounds.x) * numPaletteCols / bounds.width;
				return paintColors[Math.min(Math.max(row * numPaletteCols + col, 0), paintColors.length - 1)];
			}
		});
		Listener refreshListener = new Listener() {
			public void handleEvent(Event e) {
				if (e.gc == null)
					return;
				Rectangle bounds = paletteCanvas.getClientArea();
				for (int row = 0; row < numPaletteRows; ++row) {
					for (int col = 0; col < numPaletteCols; ++col) {
						final int x = bounds.width * col / numPaletteCols;
						final int y = bounds.height * row / numPaletteRows;
						final int width = Math.max(bounds.width * (col + 1) / numPaletteCols - x, 1);
						final int height = Math.max(bounds.height * (row + 1) / numPaletteRows - y, 1);
						e.gc.setBackground(paintColors[row * numPaletteCols + col]);
						e.gc.fillRectangle(bounds.x + x, bounds.y + y, width, height);
					}
				}
			}
		};
		paletteCanvas.addListener(SWT.Resize, refreshListener);
		paletteCanvas.addListener(SWT.Paint, refreshListener);

		// toolSettingsFrame
		gridLayout = new GridLayout();
		gridLayout.numColumns = 6;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		toolSettingsFrame.setLayout(gridLayout);

		airBrushLabel = new Label(toolSettingsFrame, SWT.NONE);
		airBrushLabel.setText(getResourceString("settings.Radius.text")); //$NON-NLS-1$

		airbrushRadiusScale = new Scale(toolSettingsFrame, SWT.HORIZONTAL);
		airbrushRadiusScale.setMinimum(5);
		airbrushRadiusScale.setMaximum(50);
		airbrushRadiusScale.setSelection(toolSettings.airbrushRadius);
		setScaleToolTip(airbrushRadiusScale);
		GridData data = new GridData();
		data.widthHint = 90;
		data.heightHint = 20;
		airbrushRadiusScale.setLayoutData(data);
		// airbrushRadiusScale.setLayoutData(new
		// GridData(GridData.FILL_HORIZONTAL
		// | GridData.VERTICAL_ALIGN_FILL));
		airbrushRadiusScale.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				setScaleToolTip(airbrushRadiusScale);
				if (currentTool == Airbrush_tool)
					toolSettings.airbrushRadius = airbrushRadiusScale.getSelection();
				else
					toolSettings.pencilRadius = airbrushRadiusScale.getSelection();
				updateToolSettings();
			}
		});

		opacityLabel = new Label(toolSettingsFrame, SWT.NONE);
		opacityLabel.setText(getResourceString("settings.Opacity.text")); //$NON-NLS-1$

		opacityScale = new Scale(toolSettingsFrame, SWT.HORIZONTAL);
		opacityScale.setMinimum(1);
		opacityScale.setMaximum(255);
		opacityScale.setSelection(toolSettings.airbrushRadius);
		setScaleToolTip(opacityScale);
		data = new GridData();
		data.widthHint = 90;
		data.heightHint = 20;
		opacityScale.setLayoutData(data);
		opacityScale.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				setScaleToolTip(opacityScale);
				if (currentTool == Airbrush_tool)
					toolSettings.airbrushOpacity = opacityScale.getSelection();
				else
					toolSettings.pencilOpacity = opacityScale.getSelection();
				updateToolSettings();
			}
		});

		intensityLabel = new Label(toolSettingsFrame, SWT.NONE);
		intensityLabel.setText(getResourceString("settings.Intensity.text")); //$NON-NLS-1$

		airbrushIntensityScale = new Scale(toolSettingsFrame, SWT.HORIZONTAL);
		airbrushIntensityScale.setMinimum(1);
		airbrushIntensityScale.setMaximum(100);
		airbrushIntensityScale.setSelection(toolSettings.airbrushIntensity);
		setScaleToolTip(airbrushIntensityScale);
		data = new GridData();
		data.widthHint = 100;
		data.heightHint = 20;
		airbrushIntensityScale.setLayoutData(data);
		// airbrushIntensityScale.setLayoutData(new GridData(
		// GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));
		airbrushIntensityScale.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				setScaleToolTip(airbrushIntensityScale);
				toolSettings.airbrushIntensity = airbrushIntensityScale.getSelection();
				updateToolSettings();
			}
		});
		addUndoKeyListener();
	}

	protected void addUndoKeyListener() {
		paintCanvas.addListener(SWT.KeyDown, new Listener() {
			@Override
			public void handleEvent(Event event) {
				int modifiers = event.stateMask;
				if ((modifiers & SWT.CTRL) != 0) {
					int keyCode = event.keyCode;
					switch (keyCode) {
					case 'z':
						// Ctrl+Z
						undo();
						return;
					case 'y':
						// Ctrl+Y
						redo();
					}
				}
			}
		});
	}

	protected void redo() {
		try {
			getHistory().redo(undoContext, null, this);
		} catch (ExecutionException e) {
			// should never happen
		}
	}

	protected void undo() {
		try {
			getHistory().undo(undoContext, null, this);
		} catch (ExecutionException e) {
			// should never happen
		}
	}

	protected void setScaleToolTip(Scale scale) {
		scale.setToolTipText(String.valueOf(scale.getSelection()));
	}

	private void createButtonBar(Composite parent) {
		final Composite toolbar = new Composite(parent, SWT.NONE);
		toolbar.setLayoutData(new GridData(GridData.BEGINNING, GridData.END, false, false));
		toolbar.setLayout(new RowLayout(SWT.VERTICAL));
		Button clearButton = new Button(toolbar, SWT.PUSH);
		clearButton.setText(getResourceString("button.Clear.label")); //$NON-NLS-1$
		clearButton.setToolTipText(getResourceString("button.Clear.tooltip")); //$NON-NLS-1$
		clearButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				Rectangle area = paintCanvas.getClientArea();
				Color color = getToolSettings().commonForegroundColor;
				SolidRectangleFigure rect = new SolidRectangleFigure(color, 0, 0, area.width, area.height);
				paintSurface.drawFigure(rect);
				setDirty(imageFigure != null);
			}
		});
		restoreButton = new Button(toolbar, SWT.PUSH);
		restoreButton.setText(getResourceString("button.Restore.label")); //$NON-NLS-1$
		restoreButton.setToolTipText(getResourceString("button.Restore.tooltip")); //$NON-NLS-1$
		restoreButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				repaintSurface();
			}
		});
		updateButtons();
	}

	private void updateButtons() {
		if (restoreButton != null)
			restoreButton.setEnabled(imageFigure != null && dirty);
	}

	public void setBackgroundImage(Image image, int w, int h) {
		imageFigure = new ImageFigure(image, 0, 0, w, h);
		paintSurface.drawFigure(imageFigure);
		updateButtons();
	}

	public Image getHardcopy() {
		return paintSurface.getPaintImage();
	}

	public PaintSurface getPaintSurface() {
		return paintSurface;
	}

	public IOperationHistory getHistory() {
		if (history == null) {
			history = PlatformUI.getWorkbench().getOperationSupport().getOperationHistory();
			history.setLimit(undoContext,
					UiActivator.getDefault().getPreferenceStore().getInt(PreferenceConstants.UNDOLEVELS));
		}
		return history;
	}

	/**
	 * Disposes of all resources associated with a particular instance of the
	 * PaintExample.
	 */
	public void dispose() {
		if (history != null)
			history.dispose(undoContext, true, true, true);
		if (paintSurface != null)
			paintSurface.dispose();
		if (paintColors != null)
			for (int i = 0; i < paintColors.length; ++i) {
				final Color color = paintColors[i];
				if (color != null)
					color.dispose();
			}
		if (pickedColor != null)
			pickedColor.dispose();
		paintDefaultFont = null;
		paintColors = null;
		paintSurface = null;
		freeResources();
	}

	/**
	 * Frees the resource bundle resources.
	 */
	public void freeResources() {
		for (int i = 0; i < tools.length; ++i) {
			Tool tool = tools[i];
			final Image image = tool.image;
			if (image != null)
				image.dispose();
			tool.image = null;
		}
	}

	/**
	 * Returns the Display.
	 *
	 * @return the display we're using
	 */
	public Display getDisplay() {
		return mainComposite.getDisplay();
	}

	/**
	 * Gets a string from the resource bundle. We don't want to crash because of a
	 * missing String. Returns the key if not found.
	 */
	public static String getResourceString(String key) {
		try {
			return resourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		} catch (NullPointerException e) {
			return "!" + key + "!"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Gets a string from the resource bundle and binds it with the given arguments.
	 * If the key is not found, return the key.
	 */
	public static String getResourceString(String key, Object[] args) {
		try {
			return MessageFormat.format(getResourceString(key), args);
		} catch (MissingResourceException e) {
			return key;
		} catch (NullPointerException e) {
			return "!" + key + "!"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Initialize colors, fonts, and tool settings.
	 */
	protected void init() {
		numPaletteRows = 3;
		numPaletteCols = 48;

		Display display = mainComposite.getDisplay();

		paintColorWhite = new Color(display, 255, 255, 255);
		paintColorBlack = new Color(display, 0, 0, 0);

		paintDefaultFont = JFaceResources.getDefaultFont();

		paintColors = new Color[numPaletteCols * numPaletteRows];
		paintColors[0] = paintColorBlack;
		paintColors[1] = paintColorWhite;
		for (int i = 2; i < paintColors.length; i++) {
			paintColors[i] = new Color(display, ((i * 7) % 255), ((i * 23) % 255), ((i * 47) % 255));
		}

		toolSettings = new ToolSettings();
		toolSettings.commonForegroundColor = paintColorBlack;
		toolSettings.commonBackgroundColor = paintColorWhite;
		toolSettings.commonFont = paintDefaultFont;
		undoContext = new UndoContext() {
			@Override
			public String getLabel() {
				return PaintExample.getResourceString("PaintExample.UndoContext.label"); //$NON-NLS-1$
			}
		};
	}

	/**
	 * Sets the action field of the tools
	 */
	private void initActions() {
		for (int i = 0; i < tools.length; ++i) {
			final Tool tool = tools[i];
			if (tool == null)
				continue;
			String group = tool.group;
			if (group.equals("tool")) { //$NON-NLS-1$
				tool.action = new Runnable() {
					public void run() {
						setPaintTool(tool.id);
					}
				};
			}
		}
	}

	/**
	 * Loads the image resources.
	 */
	public void initResources() {
		initTools();
		final Class<PaintExample> clazz = PaintExample.class;
		if (resourceBundle != null) {
			try {
				for (int i = 0; i < tools.length; ++i) {
					Tool tool = tools[i];
					if (tool != null) {
						String id = tool.group + '.' + tool.key;
						try (InputStream sourceStream = clazz.getResourceAsStream(getResourceString(id + ".image"))) { //$NON-NLS-1$
							ImageData source = new ImageData(sourceStream);
							ImageData mask = source.getTransparencyMask();
							tool.image = new Image(null, source, mask);
						}
					}
				}
				return;
			} catch (Throwable t) {
				// do nothing
			}
		}
		String error = (resourceBundle != null) ? getResourceString("error.CouldNotLoadResources") //$NON-NLS-1$
				: "Unable to load resources"; //$NON-NLS-1$
		freeResources();
		throw new RuntimeException(error);
	}

	protected void initTools() {
		tools = new Tool[] { new Tool(Pencil_tool, "Pencil", "tool", SWT.RADIO), //$NON-NLS-1$ //$NON-NLS-2$
				new Tool(Airbrush_tool, "Airbrush", "tool", SWT.RADIO), //$NON-NLS-1$ //$NON-NLS-2$
				new Tool(Pipette_tool, "Pipette", "tool", SWT.RADIO) }; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Grabs input focus.
	 */
	public void setFocus() {
		mainComposite.setFocus();
	}

	/**
	 * Sets the tool foreground color.
	 *
	 * @param color
	 *            the new color to use
	 */
	public void setForegroundColor(Color color) {
		if (activeForegroundColorCanvas != null)
			activeForegroundColorCanvas.setBackground(color);
		toolSettings.commonForegroundColor = color;
		updateToolSettings();
	}

	/**
	 * Selects a tool given its ID.
	 */
	public void setPaintTool(int id) {
		currentTool = id;
		PaintTool paintTool = (PaintTool) tools[id].data;
		paintSurface.setPaintSession(paintTool);
		for (int i = 0; i < tools.length; i++) {
			tools[i].item.setSelection(i == id);
		}
		switch (id) {
		case Pencil_tool:
			airbrushRadiusScale.setVisible(true);
			airBrushLabel.setVisible(true);
			airbrushRadiusScale.setSelection(toolSettings.pencilRadius);
			setScaleToolTip(airbrushRadiusScale);
			opacityScale.setVisible(true);
			opacityLabel.setVisible(true);
			opacityScale.setSelection(toolSettings.pencilOpacity);
			setScaleToolTip(opacityScale);
			airbrushIntensityScale.setVisible(false);
			intensityLabel.setVisible(false);
			airbrushRadiusScale.setMinimum(1);
			break;
		case Airbrush_tool:
			airbrushRadiusScale.setVisible(true);
			airBrushLabel.setVisible(true);
			airbrushRadiusScale.setSelection(toolSettings.airbrushRadius);
			setScaleToolTip(airbrushRadiusScale);
			opacityScale.setVisible(true);
			opacityLabel.setVisible(true);
			opacityScale.setSelection(toolSettings.airbrushOpacity);
			setScaleToolTip(opacityScale);
			airbrushIntensityScale.setVisible(true);
			intensityLabel.setVisible(true);
			airbrushRadiusScale.setMinimum(5);
			break;
		default:
			intensityLabel.setVisible(false);
			airbrushIntensityScale.setVisible(false);
			opacityScale.setVisible(false);
			opacityLabel.setVisible(false);
			airBrushLabel.setVisible(false);
			airbrushRadiusScale.setVisible(false);
			break;
		}
		updateToolSettings();
	}

	/**
	 * Notifies the tool that its settings have changed.
	 */
	void updateToolSettings() {
		final PaintTool activePaintTool = paintSurface.getPaintTool();
		if (activePaintTool == null)
			return;

		activePaintTool.endSession();
		activePaintTool.set(toolSettings);
		activePaintTool.beginSession();
	}

	public ToolSettings getToolSettings() {
		return toolSettings;
	}

	public int getCurrentTool() {
		return currentTool;
	}

	public void setPickedColor(RGB rgb) {
		if (pickedColor != null)
			pickedColor.dispose();
		pickedColor = new Color(paintCanvas.getDisplay(), rgb);
		setForegroundColor(pickedColor);
	}

	/*
	 * (nicht-Javadoc)
	 * 
	 * @see com.bdaum.zoom.ui.paint.IGraphicEditor#setDirty(boolean)
	 */
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
		updateButtons();
	}

	/*
	 * (nicht-Javadoc)
	 * 
	 * @see com.bdaum.zoom.ui.paint.IGraphicEditor#isDirty()
	 */
	public boolean isDirty() {
		return dirty;
	}

	public void repaintSurface() {
		Rectangle area = paintCanvas.getClientArea();
		SolidRectangleFigure rect = new SolidRectangleFigure(getDisplay().getSystemColor(SWT.COLOR_WHITE), 0, 0,
				area.width, area.height);
		paintSurface.drawFigure(rect);
		if (imageFigure != null)
			paintSurface.drawFigure(imageFigure);
		for (Figure figure : vectorLayer)
			paintSurface.drawFigure(figure);
		setDirty(false);
	}

	public List<Figure> getVectorLayer() {
		return vectorLayer;
	}

	public ImageFigure getImageFigure() {
		return imageFigure;
	}

	public VectorFigure findVectorFigure(double x, double y) {
		for (Figure figure : vectorLayer)
			if (figure instanceof VectorFigure && ((VectorFigure) figure).contains(x, y))
				return (VectorFigure) figure;
		return null;
	}

	public int getVectorPosition(Figure selectedFigure) {
		return vectorLayer.indexOf(selectedFigure);
	}

	public void addVectorFigure(VectorFigure figure) {
		vectorLayer.add(figure);
	}

	public void clearVectorFigures() {
		vectorLayer.clear();
	}

	public boolean hasVectorFigure() {
		return !vectorLayer.isEmpty();
	}

	public void addListener(int type, Listener modifyListener) {
		if (type == SWT.Modify)
			listeners.add(modifyListener);
	}

	public void removeListener(int type, Listener modifyListener) {
		if (type == SWT.Modify)
			listeners.remove(modifyListener);
	}

	public void fireModify() {
		Event e = new Event();
		e.display = paintCanvas.getDisplay();
		e.data = this;
		e.widget = paintCanvas;
		e.time = (int) System.currentTimeMillis();
		e.type = SWT.Modify;
		for (Listener listener : listeners)
			listener.handleEvent(e);
	}

	public Composite getToolbar() {
		return toolbar;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter.equals(Shell.class))
			return paintCanvas.getShell();
		return null;
	}

}
