/*******************************************************************************
 * Copyright (c) 2018 Berthold Daum
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum
 *******************************************************************************/
package com.bdaum.zoom.ui.paint;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.commands.operations.UndoContext;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.bdaum.zoom.core.internal.Utilities;
import com.ibm.icu.util.StringTokenizer;

@SuppressWarnings("restriction")
public class DrawExample extends PaintExample {

	private static final int Eraser_tool = 1;
	private static final int[] LINEWIDTHS = new int[] { 2, 4, 8, 16, 32 };
	private static final String SVGHEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" //$NON-NLS-1$
			+ "<svg xmlns:svg=\"http://www.w3.org/2000/svg\" xmlns=\"http://www.w3.org/2000/svg\"\n" //$NON-NLS-1$
			+ "   width=\"210mm\" height=\"297mm\" viewBox=\"0 0 210 297\" version=\"1.1\">\n" + "  <g>"; //$NON-NLS-1$ //$NON-NLS-2$
	protected static final Object SVG = "svg"; //$NON-NLS-1$
	protected static final Object PATH = "path"; //$NON-NLS-1$

	private Image[] buttonImages = new Image[LINEWIDTHS.length];
	private List<Color> svgColors = new ArrayList<Color>();

	public DrawExample(Composite parent) {
		super(parent);
	}

	protected void init() {
		numPaletteRows = 1;
		numPaletteCols = 16;
		Display display = mainComposite.getDisplay();
		paintColorWhite = new Color(display, 255, 255, 255);
		paintColorBlack = new Color(display, 0, 0, 0);
		paintDefaultFont = JFaceResources.getDefaultFont();
		paintColors = new Color[numPaletteCols * numPaletteRows];
		paintColors[0] = paintColorBlack;
		paintColors[1] = paintColorWhite;
		paintColors[2] = new Color(display, 255, 0, 255);
		paintColors[3] = new Color(display, 255, 0, 144);
		paintColors[4] = new Color(display, 255, 0, 0);
		paintColors[5] = new Color(display, 255, 127, 0);
		paintColors[6] = new Color(display, 255, 185, 0);
		paintColors[7] = new Color(display, 255, 255, 0);
		paintColors[8] = new Color(display, 160, 255, 0);
		paintColors[9] = new Color(display, 82, 255, 0);
		paintColors[10] = new Color(display, 0, 185, 82);
		paintColors[11] = new Color(display, 0, 165, 165);
		paintColors[12] = new Color(display, 0, 127, 255);
		paintColors[13] = new Color(display, 0, 0, 255);
		paintColors[14] = new Color(display, 82, 0, 255);
		paintColors[15] = new Color(display, 165, 0, 255);
		toolSettings = new ToolSettings();
		toolSettings.commonForegroundColor = paintColorBlack;
		toolSettings.commonBackgroundColor = paintColorWhite;
		toolSettings.commonFont = paintDefaultFont;
		undoContext = new UndoContext() {
			@Override
			public String getLabel() {
				return PaintExample.getResourceString("DrawExample.UndoContext.label"); //$NON-NLS-1$
			}
		};
	}

	public void createGUI(Composite parent, int w, int h) {
		GridLayout gridLayout;
		GridData gridData;

		/*** Create principal GUI layout elements ***/
		Composite displayArea = new Composite(parent, SWT.NONE);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		displayArea.setLayoutData(gridData);
		gridLayout = new GridLayout(2, false);
		gridLayout.marginTop = 0;
		displayArea.setLayout(gridLayout);

		toolbar = createToolBar(displayArea);

		paintCanvas = new Canvas(displayArea, SWT.BORDER | SWT.NO_REDRAW_RESIZE | SWT.NO_BACKGROUND);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.minimumWidth = w;
		gridData.minimumHeight = h;
		paintCanvas.setLayoutData(gridData);
		paintCanvas.setBackground(paintColorWhite);
		paintCanvas.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_CROSS));

		// color selector frame
		final Composite colorFrame = new Composite(displayArea, SWT.NONE);
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);
		gridData.horizontalSpan = 2;
		colorFrame.setLayoutData(gridData);

		/***
		 * Create the remaining application elements inside the principal GUI layout
		 * elements
		 ***/
		// paintSurface
		paintSurface = new PaintSurface(paintCanvas, w, h, null, paintColorWhite);
		// finish initializing the tool data
		tools[Pencil_tool].data = new DrawingPenTool(toolSettings, this);
		tools[Eraser_tool].data = new EraserTool(toolSettings, this);

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
				setForegroundColor(getColorAt(paletteCanvas.getClientArea(), e.x, e.y));
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
		paintCanvas.addControlListener(new ControlListener() {
			@Override
			public void controlResized(ControlEvent e) {
				repaintSurface();
			}

			@Override
			public void controlMoved(ControlEvent e) {
				repaintSurface();
			}
		});
		addUndoKeyListener();
	}

	@Override
	public Composite createToolBar(Composite parent) {
		Composite composite = super.createToolBar(parent);
		composite.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, false, true));
		Label label = new Label(composite, SWT.NONE);
		label.setText(getResourceString("line.label")); //$NON-NLS-1$
		ToolBar toolbar = new ToolBar(composite, SWT.VERTICAL);
		toolbar.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
		Display display = composite.getDisplay();
		for (int i = 0; i < LINEWIDTHS.length; i++) {
			final int lw = LINEWIDTHS[i];
			ToolItem item = new ToolItem(toolbar, SWT.RADIO);
			Image image = new Image(display, 50, lw + 10);
			buttonImages[i] = image;
			GC gc = new GC(image);
			gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
			gc.fillRectangle(0, 0, 50, lw + 10);
			gc.setForeground(display.getSystemColor(SWT.COLOR_BLUE));
			gc.setLineWidth(lw);
			gc.drawLine(5, lw / 2 + 5, 45, lw / 2 + 5);
			gc.dispose();
			item.setImage(image);
			item.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					toolSettings.pencilRadius = lw;
					updateToolSettings();
				}
			});
			if (i == 1) {
				item.setSelection(true);
				toolSettings.pencilRadius = lw;
			}
		}
		return composite;
	}

	protected void initTools() {
		tools = new Tool[] { new Tool(Pencil_tool, "DrawingPen", "tool", SWT.RADIO), //$NON-NLS-1$ //$NON-NLS-2$
				new Tool(Eraser_tool, "Eraser", "tool", SWT.RADIO) }; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Selects a tool given its ID.
	 */
	public void setPaintTool(int id) {
		currentTool = id;
		PaintTool paintTool = (PaintTool) tools[id].data;
		paintSurface.setPaintSession(paintTool);
		for (int i = 0; i < tools.length; i++)
			tools[i].item.setSelection(i == id);
		updateToolSettings();
	}

	public void setBackgroundImage(Image image, int w, int h) {
		paintSurface.drawFigure(imageFigure = new ImageFigure(image, paintCanvas));
	}

	@Override
	public void dispose() {
		for (Image image : buttonImages)
			image.dispose();
		for (Color color : svgColors)
			color.dispose();
		super.dispose();
	}

	public String exportSvg() {
		List<Figure> vl = getVectorLayer();
		if (vl.isEmpty())
			return null;
		StringBuilder sb = new StringBuilder(1111);
		sb.append(SVGHEADER);
		double f = 210;
		for (Figure figure : vl)
			if (figure instanceof PolylineFigure) {
				NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
				nf.setMaximumFractionDigits(3);
				PolylineFigure pf = (PolylineFigure) figure;
				double[] points = pf.getPoints();
				if (points.length >= 4) {
					Color color = pf.getColor();
					String htmlColors = Utilities.toHtmlColors(color.getRed(), color.getGreen(), color.getBlue());
					double lineWidth = pf.getLineWidth();
					sb.append(NLS.bind(
							"\n\t<path style=\"fill:none;stroke:{0};stroke-width:{1}%;stroke-linecap:round;stroke-linejoin:miter;stroke-opacity:1\"\n", //$NON-NLS-1$
							htmlColors, nf.format(100 * lineWidth)));
					sb.append("\t\td=\"m ").append(nf.format(f * points[0])).append(',') //$NON-NLS-1$
							.append(nf.format(f * points[1]));
					for (int i = 2; i < points.length; i += 2)
						sb.append(' ').append(nf.format(f * (points[i] - points[i - 2]))).append(',')
								.append(nf.format(f * (points[i + 1] - points[i - 1])));
					sb.append("\"/>"); //$NON-NLS-1$
				}
			}
		sb.append("\n</g></svg>"); //$NON-NLS-1$
		return sb.toString();
	}

	public void importSvg(String svg, boolean overwrite)
			throws IOException, ParserConfigurationException, SAXException {
		if (overwrite)
			clearVectorFigures();
		DefaultHandler dh = new DefaultHandler() {
			
			double f = 210;
			NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);

			@Override
			public void startElement(String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
				if (SVG.equals(qName)) {
					nf.setMaximumFractionDigits(3);
					String w = attributes.getValue("width"); //$NON-NLS-1$
					String h = attributes.getValue("height"); //$NON-NLS-1$
					try {
						f = Math.min(doubleValue(w), doubleValue(h));
					} catch (ParseException e) {
						// use default value
					}
				} else if (PATH.equals(qName)) {
					String style = attributes.getValue("style"); //$NON-NLS-1$
					String points = attributes.getValue("d"); //$NON-NLS-1$
					String lastToken = null, key = null;
					double lineWidth = 0.01d;
					Color lineColor = paintCanvas.getDisplay().getSystemColor(SWT.COLOR_BLACK);
					double[] vertices = new double[100];
					int verCount = 0;
					StringTokenizer st = new StringTokenizer(style, ":;", true); //$NON-NLS-1$
					while (st.hasMoreTokens()) {
						String token = st.nextToken();
						if (":".equals(token)) //$NON-NLS-1$
							key = lastToken;
						else if (";".equals(token)) { //$NON-NLS-1$
							if ("stroke".equals(key)) { //$NON-NLS-1$
								RGB rgb = Utilities.fromHtmlColors(lastToken);
								lineColor = new Color(paintCanvas.getDisplay(), rgb);
								svgColors.add(lineColor);
							} else if ("stroke-width".equals(key)) //$NON-NLS-1$
								try {
									lineWidth = doubleValue(lastToken);
								} catch (ParseException e) {
									// use default value
								}
						} else
							lastToken = token;
					}
					if (points.startsWith("m ")) { //$NON-NLS-1$
						points = points.substring(2) + " "; //$NON-NLS-1$
						double x = 0, y = 0;
						double sumx = 0, sumy = 0;
						st = new StringTokenizer(points, " ,", true); //$NON-NLS-1$
						while (st.hasMoreTokens()) {
							String token = st.nextToken();
							if (" ".equals(token)) { //$NON-NLS-1$
								try {
									y = doubleValue(lastToken);
								} catch (ParseException e) {
									// use previous value
								}
								if (verCount * 2 + 1 >= vertices.length) {
									double[] newPoints = new double[vertices.length * 2];
									System.arraycopy(vertices, 0, newPoints, 0, vertices.length);
									vertices = newPoints;
								}
								sumx += x;
								sumy += y;
								vertices[verCount * 2] = sumx / f;
								vertices[verCount * 2 + 1] = sumy / f;
								++verCount;
							} else if (",".equals(token)) { //$NON-NLS-1$
								try {
									x = doubleValue(lastToken);
								} catch (ParseException e) {
									// use previous value
								}
							} else
								lastToken = token;
						}
					}
					double[] vert = new double[verCount * 2];
					System.arraycopy(vertices, 0, vert, 0, vert.length);
					addVectorFigure(new PolylineFigure(lineColor, lineWidth, SWT.LINE_SOLID, vert, DrawExample.this));
				}
			}

			private double doubleValue(String s) throws ParseException {
				if (s.endsWith("%")) //$NON-NLS-1$
					return nf.parse(s.substring(0, s.length() - 1)).doubleValue() / 100;
				return nf.parse(s).doubleValue();
			}

			@Override
			public void characters(char[] ch, int start, int length) throws SAXException {
				// do nothing
			}

			@Override
			public void endElement(String uri, String localName, String qName) throws SAXException {
				// do nothing
			}
		};
		try (ByteArrayInputStream is = new ByteArrayInputStream(svg.getBytes())) {
			SAXParserFactory.newInstance().newSAXParser().parse(is, dh);
			repaintSurface();
		}
	}

}
