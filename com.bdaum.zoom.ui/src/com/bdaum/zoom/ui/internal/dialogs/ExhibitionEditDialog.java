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
 * (c) 2009-2018 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.dialogs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.nebula.paperclips.core.PaperClips;
import org.eclipse.nebula.paperclips.core.Print;
import org.eclipse.nebula.paperclips.core.PrintJob;
import org.eclipse.nebula.paperclips.core.grid.DefaultGridLook;
import org.eclipse.nebula.paperclips.core.grid.GridPrint;
import org.eclipse.nebula.paperclips.core.page.PageNumberPageDecoration;
import org.eclipse.nebula.paperclips.core.page.PagePrint;
import org.eclipse.nebula.paperclips.core.text.TextPrint;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import com.bdaum.aoModeling.runtime.IIdentifiableObject;
import com.bdaum.zoom.cat.model.Rgb_typeImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.GroupImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.exhibition.Exhibit;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitImpl;
import com.bdaum.zoom.cat.model.group.exhibition.Exhibition;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitionImpl;
import com.bdaum.zoom.cat.model.group.exhibition.Wall;
import com.bdaum.zoom.cat.model.group.exhibition.WallImpl;
import com.bdaum.zoom.cat.model.group.slideShow.SlideImpl;
import com.bdaum.zoom.cat.model.group.slideShow.SlideShowImpl;
import com.bdaum.zoom.cat.model.group.webGallery.Storyboard;
import com.bdaum.zoom.cat.model.group.webGallery.WebExhibitImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebGalleryImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.net.core.ftp.FtpAccount;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.ZViewerComparator;
import com.bdaum.zoom.ui.internal.views.ExhibitionView;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.CheckedText;
import com.bdaum.zoom.ui.internal.widgets.FileEditor;
import com.bdaum.zoom.ui.internal.widgets.QualityGroup;
import com.bdaum.zoom.ui.internal.widgets.WebColorGroup;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.bdaum.zoom.ui.widgets.NumericControl;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

@SuppressWarnings("restriction")
public class ExhibitionEditDialog extends ZTitleAreaDialog {

	private static final String SETTINGSID = "com.bdaum.zoom.exhibitionProperties"; //$NON-NLS-1$
	private static final double DPI300 = 25.4d / 300d;
	private static final String GROUND_COLOR = "groundColor"; //$NON-NLS-1$
	private static final String HORIZON_COLOR = "horizonColor"; //$NON-NLS-1$
	private static final String CEILING_COLOR = "ceilingColor"; //$NON-NLS-1$
	private static final String FRAME_COLOR = "frameColor"; //$NON-NLS-1$
	private static final String MAT_COLOR = "matColor"; //$NON-NLS-1$
	private static final String VIEWING_HEIGHT = "viewing_height"; //$NON-NLS-1$
	private static final String VARIANCE = "variance"; //$NON-NLS-1$
	private static final String FRAME_WIDTH = "frame_width"; //$NON-NLS-1$
	private static final String MAT_WIDTH = "mat_width"; //$NON-NLS-1$
	private static final String WATER_MARK = "waterMark"; //$NON-NLS-1$
	private static final String WEBURL = "weburl"; //$NON-NLS-1$
	private static final String EMAIL = "email"; //$NON-NLS-1$
	private static final String CONTACT = "contact"; //$NON-NLS-1$
	private static final String COPYRIGHT = "copyright"; //$NON-NLS-1$
	private static final String LOGO = "logo"; //$NON-NLS-1$
	private static final String LINKPAGE = "linkpage"; //$NON-NLS-1$
	private static final String FONTFAMILY = "fontfamily"; //$NON-NLS-1$
	private static final String FONTSIZE = "fontsize"; //$NON-NLS-1$
	private static final String LABELSEQUENCE = "labelSequence"; //$NON-NLS-1$
	private static final String AUDIO = "audio"; //$NON-NLS-1$
	private static final String INFOPLATEPOS = "infoPlatePos"; //$NON-NLS-1$
	private static final String HIDECREDITS = "hideCredits"; //$NON-NLS-1$
	private static final String DEFAULTINFO = "defaultInfo"; //$NON-NLS-1$
	private static final int ENTRANCEDIAMETER = 13;
	private static final int DOORWIDTH = 1000;
	private static final int INFOWIDTH = 1000;
	private static final double D1000 = 1000d;
	private static final double D05 = 0.5d;
	private static final int TOLERANCE = 5;
	private static final Object[] EKPTY = new Object[0];
	private static final int PDFBUTTON = 998;
	private static final int PRINTBUTTON = 999;
	private static NumberFormat af = NumberFormat.getNumberInstance();
	private static final SimpleDateFormat EDF = new SimpleDateFormat("yyyy"); //$NON-NLS-1$
	private static String[] SEQUENCEITEMS = new String[] { Messages.ExhibitionEditDialog_capt_des_cred,
			Messages.ExhibitionEditDialog_capt_cred_des, Messages.ExhibitionEditDialog_cred_capt_des };

	private final ModifyListener modifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			updateButtons();
		}
	};

	private final boolean canUndo;
	private ComboViewer itemViewer;
	private Label alabel;
	private CTabFolder tabFolder, detailTabfolder;
	private PrinterData printerData;
	private CTabItem detailsTabItem;
	private LabelLayoutGroup labelLayoutGroup;
	private ExhibitionImpl current;
	private String title;
	private ExhibitionImpl result;
	private Font selectedFont;
	private Canvas floorplan;
	private double scale;
	private int xoff, yoff;
	private int[] wxs, wys, wx2s, wy2s;
	private int hotIndex;
	private GroupImpl group;
	private IDialogSettings settings;
	private Cursor rotCursor;
	private char unit = Core.getCore().getDbFactory().getDimUnit();
	private TreeViewer imageDetailViewer;
	private ImportGalleryGroup importGroup;
	private NumericControl xspinner, yspinner, aspinner, viewingHeightField, gridSizeField, varianceField,
			matWidthField, frameWidthField;
	private CheckboxButton showGridButton, snapToGridField, watermarkButton, showCreditsButton;
	private Button fontButton;
	private WebColorGroup matColorGroup, frameColorGroup, horizonColorGroup, groundColorGroup, ceilingColorGroup;
	private Text copyrightField, contactField, emailField, weburlField, linkField, keywordField, nameField;
	private CheckedText infoField, descriptionField, labelTextField;
	private Combo sequenceCombo, infoPosField;
	private FileEditor audioField;
	private OutputTargetGroup outputTargetGroup;
	private FileEditor logoField;
	protected int dragHandle;
	protected Object draggedObject, recentlyDraggedObject, hotObject, selectedItem;
	protected Point dragStart;
	protected Point origin = new Point(0, 0);
	private QualityGroup qualityGroup;
	private TreeViewer frameDetailViewer;

	public static ExhibitionImpl open(final Shell shell, final GroupImpl group, final ExhibitionImpl gallery,
			final String title, final boolean canUndo, final String errorMsg) {
		final ExhibitionImpl[] box = new ExhibitionImpl[1];
		BusyIndicator.showWhile(shell.getDisplay(), () -> {
			ExhibitionEditDialog dialog = new ExhibitionEditDialog(shell, group, gallery, title, canUndo);
			if (errorMsg != null) {
				dialog.create();
				dialog.selectOutputPage(errorMsg);
			}
			if (dialog.open() == Window.OK)
				box[0] = dialog.getResult();
		});
		return box[0];
	}

	public ExhibitionEditDialog(Shell parentShell, GroupImpl group, ExhibitionImpl current, String title,
			boolean canUndo) {
		super(parentShell, HelpContextIds.EXHIBITION_DIALOG);
		this.group = group;
		this.current = current;
		selectedItem = current;
		this.title = title;
		this.canUndo = canUndo;
		settings = getDialogSettings(UiActivator.getDefault(), SETTINGSID);
	}

	@Override
	public void create() {
		super.create();
		setTitle(title);
		setMessage(Messages.ExhibitionEditDialog_specify_exhibition_props);
		updateButtons();
		Shell shell = getShell();
		shell.layout();
		ImageData cursorData = UiActivator.getImageDescriptor("icons/cursors/rotCursor.bmp").getImageData(100); //$NON-NLS-1$
		cursorData.transparentPixel = 1;
		rotCursor = new Cursor(shell.getDisplay(), cursorData, cursorData.width / 2, cursorData.height / 2);
		shell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				rotCursor.dispose();
			}
		});
		if (importGroup != null)
			nameField.setFocus();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(1, false));
		if (current == null) {
			importGroup = new ImportGalleryGroup(composite, new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1),
					Messages.ExhibitionEditDialog_web_gallery);
			importGroup.addChangeListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					IIdentifiableObject fromItem = importGroup.getFromItem();
					if (fromItem instanceof ExhibitionImpl)
						fillValues((ExhibitionImpl) fromItem, true);
					infoField.setText(importGroup.getDescription());
				}
			});
		}
		tabFolder = new CTabFolder(composite, SWT.BORDER);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		createOverview(UiUtilities.createTabPage(tabFolder, Messages.ExhibitionEditDialog_overview, null));
		createLayoutGroup(UiUtilities.createTabPage(tabFolder, Messages.ExhibitionEditDialog_layout, null));
		createGridGroup(UiUtilities.createTabPage(tabFolder, Messages.ExhibitionEditDialog_grid, null));
		if (current != null) {
			detailsTabItem = UiUtilities.createTabItem(tabFolder, Messages.ExhibitionEditDialog_details, null);
			Composite detailsComp = new Composite(tabFolder, SWT.NONE);
			detailsTabItem.setControl(detailsComp);
			createDetails(detailsComp);
			createFloorplan(UiUtilities.createTabPage(tabFolder, Messages.ExhibitionEditDialog_floorplan, null));
		}
		createWebGroup(UiUtilities.createTabPage(tabFolder, Messages.ExhibitionEditDialog_web, null));
		createOutputGroup(UiUtilities.createTabPage(tabFolder, Messages.ExhibitionEditDialog_output, null));
		tabFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (tabFolder.getSelection() == detailsTabItem)
					updateDetailViewers(current);
				updateButtons();
			}
		});
		tabFolder.setSimple(false);
		tabFolder.setSelection(0);
		fillValues(current, false);
		return area;
	}

	private void createOutputGroup(Composite parent) {
		parent.setLayout(new GridLayout(3, false));
		outputTargetGroup = new OutputTargetGroup(parent,
				new GridData(GridData.FILL, GridData.BEGINNING, true, false, 3, 1), modifyListener, false, true);
		// Link
		new Label(parent, SWT.NONE).setText(Messages.ExhibitionEditDialog_web_link);
		linkField = new Text(parent, SWT.BORDER);
		linkField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		linkField.addModifyListener(modifyListener);
		// Keywords
		final Label keywordLabel = new Label(parent, SWT.NONE);
		keywordLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		keywordLabel.setText(Messages.ExhibitionEditDialog_keywords);

		keywordField = new Text(parent, SWT.WRAP | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER);
		final GridData gd_keywordField = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd_keywordField.widthHint = 150;
		gd_keywordField.heightHint = 70;
		keywordField.setLayoutData(gd_keywordField);
		Button keywordButton = new Button(parent, SWT.PUSH);
		keywordButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		keywordButton.setText(Messages.WebGalleryEditDialog_add_image_keywords);
		keywordButton.setEnabled(hasImages());
		keywordButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addImageKeywords();
			}
		});
		// Sharpening
		qualityGroup = new QualityGroup(parent, false);

		// Copyright and Watermark
		new Label(parent, SWT.NONE).setText(Messages.ExhibitionEditDialog_copyright);
		copyrightField = new Text(parent, SWT.BORDER);
		final GridData gd_copyrightField = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd_copyrightField.widthHint = 250;
		copyrightField.setLayoutData(gd_copyrightField);
		watermarkButton = WidgetFactory.createCheckButton(parent, Messages.ExhibitionEditDialog_create_watermarks,
				new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		// Contact
		new Label(parent, SWT.NONE).setText(Messages.ExhibitionEditDialog_contact);

		contactField = new Text(parent, SWT.BORDER);
		final GridData gd_contactField = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd_contactField.widthHint = 250;
		contactField.setLayoutData(gd_contactField);
		new Label(parent, SWT.NONE).setText(Messages.ExhibitionEditDialog_email);

		emailField = new Text(parent, SWT.BORDER);
		final GridData gd_emailField = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd_emailField.widthHint = 250;
		emailField.setLayoutData(gd_emailField);
		new Label(parent, SWT.NONE).setText(Messages.ExhibitionEditDialog_web_url);

		weburlField = new Text(parent, SWT.BORDER);
		final GridData gd_weburlField = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd_weburlField.widthHint = 250;
		weburlField.setLayoutData(gd_weburlField);

	}

	protected void addImageKeywords() {
		if (current == null)
			return;
		final List<Wall> walls = current.getWall();
		if (walls == null)
			return;
		BusyIndicator.showWhile(getShell().getDisplay(), () -> {
			Set<String> keywords = new HashSet<String>();
			StringTokenizer st = new StringTokenizer(keywordField.getText().trim(), "\n"); //$NON-NLS-1$
			while (st.hasMoreTokens())
				keywords.add(st.nextToken());
			for (Wall wall : walls) {
				List<String> exhibit = wall.getExhibit();
				if (exhibit != null)
					for (String exhibitId : exhibit) {
						WebExhibitImpl obj = dbManager.obtainById(WebExhibitImpl.class, exhibitId);
						if (obj != null) {
							String assetId = obj.getAsset();
							AssetImpl asset = dbManager.obtainAsset(assetId);
							if (asset != null) {
								String[] kw = asset.getKeyword();
								if (kw != null)
									for (String k : kw)
										keywords.add(k);
							}
						}
					}
			}
			String[] kws = keywords.toArray(new String[keywords.size()]);
			Arrays.sort(kws, Utilities.KEYWORDCOMPARATOR);
			keywordField.setText(Core.toStringList(kws, "\n")); //$NON-NLS-1$
		});
	}

	private boolean hasImages() {
		if (current == null)
			return false;
		List<Wall> walls = current.getWall();
		if (walls == null)
			return false;
		for (Wall wall : walls) {
			List<String> exhibit = wall.getExhibit();
			if (exhibit != null && !exhibit.isEmpty())
				return true;
		}
		return false;
	}

	private void createWebGroup(Composite comp) {
		comp.setLayout(new GridLayout());
		CGroup colorGroup = CGroup.create(comp, 1, Messages.ExhibitionEditDialog_colors);
		groundColorGroup = new WebColorGroup(colorGroup, Messages.ExhibitionEditDialog_ground_color);
		horizonColorGroup = new WebColorGroup(colorGroup, Messages.ExhibitionEditDialog_horizon_color);
		ceilingColorGroup = new WebColorGroup(colorGroup, Messages.ExhibitionEditDialog_ceiling_color);
		CGroup mediaGroup = CGroup.create(comp, 1, Messages.ExhibitionEditDialog_media);
		new Label(mediaGroup, SWT.NONE).setText(Messages.ExhibitionEditDialog_sound_track);
		audioField = new FileEditor(mediaGroup, SWT.OPEN, Messages.ExhibitionEditDialog_sound_file, false,
				new String[] { "*.mid;*.midi;*.rm;*.ram;*.mp3;*.mpga;*.wav;*.wma" }, //$NON-NLS-1$
				new String[] { Messages.ExhibitionEditDialog_audio_files }, null, null, false, settings);
		new Label(mediaGroup, SWT.NONE).setText(Messages.WebGalleryEditDialog_name_plate);
		logoField = new FileEditor(mediaGroup, SWT.OPEN, Messages.ExhibitionEditDialog_select_nameplate_file, false,
				new String[] { "*.gif;*.GIF;*.jpg;*.JPG;*.png;*.PNG" }, //$NON-NLS-1$
				new String[] { Messages.ExhibitionEditDialog_valid_image_files }, null, null, false, settings);
		logoField.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
	}

	private void createGridGroup(Composite comp) {
		comp.setLayout(new GridLayout(2, false));
		showGridButton = WidgetFactory.createCheckButton(comp, Messages.ExhibitionEditDialog_show_grid,
				new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		showGridButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtons();
			}
		});
		new Label(comp, SWT.NONE).setText(Messages.ExhibitionEditDialog_grid_size);
		gridSizeField = new NumericControl(comp, SWT.NONE);
		gridSizeField.setPageIncrement(5);
		gridSizeField.setMinimum(5);
		gridSizeField.setMaximum(50);
		snapToGridField = WidgetFactory.createCheckButton(comp, Messages.ExhibitionEditDialog_snap_to_grid,
				new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
	}

	private void createLayoutGroup(Composite comp) {
		comp.setLayout(new GridLayout());
		Composite heightGroup = new Composite(comp, SWT.NONE);
		heightGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		GridLayout layout = new GridLayout(4, false);
		layout.marginWidth = 0;
		heightGroup.setLayout(layout);
		new Label(heightGroup, SWT.NONE)
				.setText(Messages.ExhibitionEditDialog_default_viewing_height + captionUnitmft());
		viewingHeightField = new NumericControl(heightGroup, SWT.NONE);
		viewingHeightField.setDigits(2);
		viewingHeightField.setMaximum(unit == 'i' ? 1500 : 500);
		final Label varianceLabel = new Label(heightGroup, SWT.NONE);
		GridData layoutData = new GridData();
		layoutData.horizontalIndent = 10;
		varianceLabel.setLayoutData(layoutData);
		varianceLabel.setText(Messages.ExhibitionEditDialog_variance + captionUnitmft());
		varianceField = new NumericControl(heightGroup, SWT.NONE);
		varianceField.setDigits(2);
		varianceField.setMaximum(999);
		viewingHeightField.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				varianceField.setMaximum(2 * viewingHeightField.getSelection() - 1);
			}
		});
		CGroup frameGroup = CGroup.create(comp, 1, Messages.ExhibitionEditDialog_mat_and_fram);
		new Label(frameGroup, SWT.NONE).setText(Messages.ExhibitionEditDialog_matWidth + captionUnitcmin());
		matWidthField = new NumericControl(frameGroup, SWT.NONE);
		matWidthField.setMaximum(unit == 'i' ? 400 : 1000);
		matWidthField.setDigits(1);
		SelectionAdapter colorSelectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateColorGroups();
			}
		};
		matWidthField.addSelectionListener(colorSelectionListener);
		matColorGroup = new WebColorGroup(frameGroup, Messages.ExhibitionEditDialog_matColor + captionUnitcmin());
		new Label(frameGroup, SWT.NONE).setText(Messages.ExhibitionEditDialog_frameWidth);
		frameWidthField = new NumericControl(frameGroup, SWT.NONE);
		frameWidthField.setMaximum(unit == 'i' ? 40 : 100);
		frameWidthField.setDigits(1);
		frameWidthField.addSelectionListener(colorSelectionListener);
		frameColorGroup = new WebColorGroup(frameGroup, Messages.ExhibitionEditDialog_frameColor);
		CGroup labelGroup = CGroup.create(comp, 1, Messages.ExhibitionEditDialog_label);
		new Label(labelGroup, SWT.NONE).setText(Messages.ExhibitionEditDialog_label_font);
		fontButton = new Button(labelGroup, SWT.PUSH);
		fontButton.setText(Messages.ExhibitionEditDialog_font);
		fontButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FontDialog dialog = new FontDialog(getShell());
				dialog.setText(Messages.ExhibitionEditDialog_label_font_title);
				if (selectedFont != null)
					dialog.setFontList(selectedFont.getFontData());
				FontData fontdata = dialog.open();
				if (fontdata != null) {
					if (selectedFont != null)
						selectedFont.dispose();
					fontButton.setFont(selectedFont = new Font(e.display, fontdata));
				}
			}
		});
		new Label(labelGroup, SWT.NONE).setText(Messages.ExhibitionEditDialog_label_sequemce);
		sequenceCombo = new Combo(labelGroup, SWT.READ_ONLY);
		sequenceCombo.setItems(SEQUENCEITEMS);
		final Label descriptionLabel = new Label(labelGroup, SWT.NONE);
		descriptionLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		descriptionLabel.setText(Messages.ExhibitionEditDialog_default_text);
		final GridData gd_descriptionField = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd_descriptionField.widthHint = 250;
		gd_descriptionField.heightHint = 70;
		labelTextField = new CheckedText(labelGroup, SWT.WRAP | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER);
		labelTextField.setLayoutData(gd_descriptionField);
		labelLayoutGroup = new LabelLayoutGroup(labelGroup, SWT.NONE, true);
		labelLayoutGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
		labelLayoutGroup.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});
	}

	private void updateColorGroups() {
		matColorGroup.setEnabled(matWidthField.getSelection() != 0);
		frameColorGroup.setEnabled(frameWidthField.getSelection() != 0);
	}

	private void createFloorplan(Composite parent) {
		parent.setLayout(new GridLayout());
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout(2, false));
		Composite detailGroup = new Composite(comp, SWT.NONE);
		detailGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		detailGroup.setLayout(new GridLayout(2, false));
		itemViewer = new ComboViewer(detailGroup);
		itemViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		itemViewer.setContentProvider(ArrayContentProvider.getInstance());
		itemViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Exhibition)
					return Messages.ExhibitionEditDialog_entrance;
				if (element instanceof Wall)
					return ((Wall) element).getLocation();
				return super.getText(element);
			}
		});
		List<Object> items = new ArrayList<Object>(current.getWall().size() + 1);
		items.add(current);
		items.addAll(current.getWall());
		itemViewer.setInput(items);
		Label xlabel = new Label(detailGroup, SWT.NONE);
		xlabel.setText(Messages.ExhibitionEditDialog_ground_xpos + captionUnitmft());
		xspinner = new NumericControl(detailGroup, NumericControl.LOGARITHMIC);
		xspinner.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		xspinner.setMaximum(unit == 'i' ? 1500000 : 500000);
		xspinner.setIncrement(unit == 'i' ? 30 : 10);
		xspinner.setDigits(2);
		xspinner.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateItems(toMm(xspinner.getSelection()), -1, Double.NaN);
			}
		});
		Label ylabel = new Label(detailGroup, SWT.NONE);
		ylabel.setText(Messages.ExhibitionEditDialog_ground_ypos + captionUnitmft());
		yspinner = new NumericControl(detailGroup, NumericControl.LOGARITHMIC);
		yspinner.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		yspinner.setMaximum(unit == 'i' ? 1500000 : 500000);
		yspinner.setIncrement(unit == 'i' ? 30 : 10);
		yspinner.setDigits(2);
		yspinner.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateItems(-1, toMm(yspinner.getSelection()), Double.NaN);
			}
		});
		alabel = new Label(detailGroup, SWT.NONE);
		alabel.setText(Messages.ExhibitionEditDialog_ground_angle);
		aspinner = new NumericControl(detailGroup, SWT.NONE);
		aspinner.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		aspinner.setMaximum(3600);
		aspinner.setIncrement(10);
		aspinner.setDigits(1);
		aspinner.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateItems(-1, -1, aspinner.getSelection() / 10d);
			}
		});

		floorplan = new Canvas(comp, SWT.DOUBLE_BUFFERED);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.widthHint = 400;
		data.heightHint = 300;
		floorplan.setLayoutData(data);
		floorplan.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				Rectangle area = floorplan.getClientArea();
				GC gc = e.gc;
				gc.setBackground(e.display.getSystemColor(SWT.COLOR_WHITE));
				gc.fillRectangle(area);
				int minx = Integer.MAX_VALUE;
				int miny = Integer.MAX_VALUE;
				int maxx = Integer.MIN_VALUE;
				int maxy = Integer.MIN_VALUE;
				int n = current.getWall().size();
				wxs = new int[n];
				wys = new int[n];
				wx2s = new int[n];
				wy2s = new int[n];
				double[] wa = new double[n];
				int i = 0;
				for (Wall wall : current.getWall()) {
					double angle = wall.getGAngle();
					int gx = wall.getGX();
					int gy = wall.getGY();
					boolean match = false;
					for (int j = 0; j < i; j++)
						if (gx == wxs[j] && gy == wys[j] && angle == wa[j]) {
							match = true;
							break;
						}
					if (match) {
						wall.setGX(wx2s[i - 1]);
						gx = wx2s[i - 1];
						wall.setGY(wy2s[i - 1]);
						gy = wy2s[i - 1];
						double d = wa[i - 1];
						if (d > 45 && d < 135)
							angle = ((i / 2) % 2 == 0) ? d - 90 : d + 90;
						else if (d > 135 && d < 225)
							angle = d - 90;
						else
							angle = d + 90;
						if (angle < 0)
							angle += 360;
						else if (angle >= 360)
							angle -= 360;
						wall.setGAngle(angle);
						updateFloorplanDetails();
					}
					int width = wall.getWidth();
					double r = Math.toRadians(angle);
					int gx2 = (int) (gx + Math.cos(r) * width + D05);
					int gy2 = (int) (gy + Math.sin(r) * width + D05);
					minx = Math.min(minx, Math.min(gx, gx2));
					miny = Math.min(miny, Math.min(gy, gy2));
					maxx = Math.max(maxx, Math.max(gx, gx2));
					maxy = Math.max(maxy, Math.max(gy, gy2));
					wxs[i] = gx;
					wys[i] = gy;
					wx2s[i] = gx2;
					wy2s[i] = gy2;
					wa[i] = angle;
					++i;
				}
				int sx = current.getStartX();
				int sy = current.getStartY();
				minx = Math.min(minx, sx);
				miny = Math.min(miny, sy);
				maxx = Math.max(maxx, sx);
				maxy = Math.max(maxy, sy);
				double w = maxx - minx;
				double h = maxy - miny;
				scale = Math.min(area.width / w, area.height / h) / 2d;
				xoff = (int) (area.width / 4 - minx * scale);
				yoff = (int) (area.height / 4 - miny * scale);
				double d = -Math.floor(xoff / (D1000 * scale)) * D1000;
				while (true) {
					int x = xoff + (int) (d * scale + D05);
					if (x > area.width)
						break;
					gc.setForeground(e.display.getSystemColor(d == 0d ? SWT.COLOR_BLUE : SWT.COLOR_GRAY));
					gc.drawLine(x, 0, x, area.height);
					d += D1000;
				}
				d = -Math.floor(yoff / (D1000 * scale)) * D1000;
				while (true) {
					int y = yoff + (int) (d * scale + D05);
					gc.setForeground(e.display.getSystemColor(d == 0d ? SWT.COLOR_BLUE : SWT.COLOR_GRAY));
					if (y > area.height)
						break;
					gc.drawLine(0, y, area.width, y);
					d += D1000;
				}
				gc.setBackground(e.display.getSystemColor(SWT.COLOR_GRAY));
				gc.fillRectangle(xoff - 3, yoff - 3, 6, 6);
				gc.setLineWidth(2);
				Wall doorWall = null;
				i = 0;
				for (Wall wall : current.getWall()) {
					gc.setForeground(
							e.display.getSystemColor(selectedItem == wall ? SWT.COLOR_RED : SWT.COLOR_DARK_GRAY));
					int x1 = xoff + (int) (wxs[i] * scale + D05);
					int y1 = yoff + (int) (wys[i] * scale + D05);
					int x2 = xoff + (int) (wx2s[i] * scale + D05);
					int y2 = yoff + (int) (wy2s[i] * scale + D05);
					gc.drawLine(x1, y1, x2, y2);
					double aAngle = wall.getGAngle();
					double r = Math.toRadians(aAngle);
					double sin = Math.sin(r);
					double cos = Math.cos(r);
					if (sx >= Math.min(wxs[i], wx2s[i]) - 50 && sx <= Math.max(wxs[i], wx2s[i]) + 50
							&& sy >= Math.min(wys[i], wy2s[i]) - 50 && sy <= Math.max(wys[i], wy2s[i]) + 50) {
						if (Math.abs(cos) > 0.01d) {
							if (Math.abs(wys[i] - sy + (sx - wxs[i]) * sin / cos) < 50)
								doorWall = wall;
						} else
							doorWall = wall;
					}
					int xa = (int) ((x1 + x2) * D05 - sin * 5);
					int ya = (int) ((y1 + y2) * D05 + cos * 5);
					aAngle += 135;
					r = Math.toRadians(aAngle);
					int x3 = (int) (xa + 6 * Math.cos(r) + D05);
					int y3 = (int) (ya + 6 * Math.sin(r) + D05);
					gc.drawLine(xa, ya, x3, y3);
					aAngle -= 270;
					r = Math.toRadians(aAngle);
					x3 = (int) (xa + 6 * Math.cos(r) + D05);
					y3 = (int) (ya + 6 * Math.sin(r) + D05);
					gc.drawLine(xa, ya, x3, y3);
					++i;
				}
				if (doorWall != null) {
					int lineStyle = gc.getLineStyle();
					gc.setLineStyle(SWT.LINE_DASH);
					double r = Math.toRadians(doorWall.getGAngle());
					double sin = Math.sin(r);
					double cos = Math.cos(r);
					double dx = sx * scale - sin * 5;
					double dy = sy * scale + cos * 5;
					int dx1 = (int) (dx + DOORWIDTH / 2 * scale * cos + D05);
					int dy1 = (int) (dy - DOORWIDTH / 2 * scale * sin + D05);
					int dx2 = (int) (dx - (INFOWIDTH + DOORWIDTH / 2) * scale * cos + D05);
					int dy2 = (int) (dy + (INFOWIDTH + DOORWIDTH / 2) * scale * sin + D05);
					gc.drawLine(xoff + dx1, yoff + dy1, xoff + dx2, yoff + dy2);
					gc.setLineStyle(lineStyle);
				}
				gc.setForeground(
						e.display.getSystemColor(selectedItem == current ? SWT.COLOR_RED : SWT.COLOR_DARK_GREEN));
				gc.drawOval(xoff + (int) (sx * scale + D05) - ENTRANCEDIAMETER / 2 - 1,
						yoff + (int) (sy * scale + D05) - ENTRANCEDIAMETER / 2 - 1, ENTRANCEDIAMETER, ENTRANCEDIAMETER);
			}
		});
		floorplan.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (hotObject != null) {
					dragStart = new Point(e.x, e.y);
					draggedObject = hotObject;
					dragHandle = hotIndex;
					hotObject = null;
				}
			}

			@Override
			public void mouseUp(MouseEvent e) {
				recentlyDraggedObject = draggedObject;
				draggedObject = null;
				Object sel = null;
				int x = e.x;
				int y = e.y;
				int sx = xoff + (int) (current.getStartX() * scale + D05);
				int sy = yoff + (int) (current.getStartY() * scale + D05);
				if (Math.sqrt((sx - x) * (sx - x) + (sy - y) * (sy - y)) <= ENTRANCEDIAMETER / 2 + 1) {
					sel = current;
				} else {
					int i = 0;
					for (Wall wall : current.getWall()) {
						int x1 = xoff + (int) (wxs[i] * scale + D05);
						int y1 = yoff + (int) (wys[i] * scale + D05);
						int x2 = xoff + (int) (wx2s[i] * scale + D05);
						int y2 = yoff + (int) (wy2s[i] * scale + D05);
						double d = Math.abs((x2 - x1) * (y1 - y) - (x1 - x) * (y2 - y1))
								/ Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
						if (d < TOLERANCE && x <= Math.max(x1, x2) + TOLERANCE && x >= Math.min(x1, x2) - TOLERANCE
								&& y <= Math.max(y1, y2) + TOLERANCE && y >= Math.min(y1, y2) - TOLERANCE) {
							sel = wall;
							break;
						}
						++i;
					}
				}
				if (sel != null)
					itemViewer.setSelection(new StructuredSelection(sel));
			}
		});
		floorplan.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				if (draggedObject != null) {
					int dx = e.x - dragStart.x;
					int dy = e.y - dragStart.y;
					int x = origin.x + (int) (dx / scale + D05);
					int y = origin.y + (int) (dy / scale + D05);
					if (draggedObject == current) {
						current.setStartX(x);
						current.setStartY(y);
					} else if (draggedObject instanceof Wall) {
						Wall wall = (Wall) draggedObject;
						if (dragHandle == 1) {
							wall.setGX(x);
							wall.setGY(y);
						} else {
							double ddx = wall.getGX() - x;
							double ddy = wall.getGY() - y;
							double angle = ddx == 0 ? 90 : Math.toDegrees(Math.atan(ddy / ddx));
							if (ddx > 0) {
								if (ddy < 0)
									angle += 180;
								else
									angle -= 180;
							}
							wall.setGAngle(angle);
						}
					}
					floorplan.redraw();
					updateFloorplanDetails();
					return;
				}
				IStructuredSelection selection = (IStructuredSelection) itemViewer.getSelection();
				hotObject = null;
				hotIndex = -1;
				Object sel = selection.getFirstElement();
				int x = e.x;
				int y = e.y;
				int sx = xoff + (int) (current.getStartX() * scale + D05);
				int sy = yoff + (int) (current.getStartY() * scale + D05);
				if (sel == current
						&& Math.sqrt((sx - x) * (sx - x) + (sy - y) * (sy - y)) <= ENTRANCEDIAMETER / 2 + 1) {
					hotObject = current;
					origin.x = current.getStartX();
					origin.y = current.getStartY();
				} else {
					int i = 0;
					for (Wall wall : current.getWall()) {
						if (sel == wall) {
							int x1 = xoff + (int) (wxs[i] * scale + D05);
							int y1 = yoff + (int) (wys[i] * scale + D05);
							int x2 = xoff + (int) (wx2s[i] * scale + D05);
							int y2 = yoff + (int) (wy2s[i] * scale + D05);
							if (Math.sqrt((x1 - x) * (x1 - x) + (y1 - y) * (y1 - y)) <= TOLERANCE) {
								hotIndex = 1;
								hotObject = wall;
								origin.x = wxs[i];
								origin.y = wys[i];
							} else if (Math.sqrt((x2 - x) * (x2 - x) + (y2 - y) * (y2 - y)) <= TOLERANCE) {
								hotIndex = 2;
								hotObject = wall;
								origin.x = wx2s[i];
								origin.y = wy2s[i];
							}
						}
						++i;
					}
				}
				if (hotObject != null) {
					if (hotIndex == 2)
						floorplan.setCursor(rotCursor);
					else
						floorplan.setCursor(getShell().getDisplay().getSystemCursor(SWT.CURSOR_SIZEALL));
				} else
					floorplan.setCursor(getShell().getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
			}
		});
		floorplan.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (recentlyDraggedObject != null) {
					int dx = 0;
					int dy = 0;
					switch (e.keyCode) {
					case SWT.ARROW_LEFT:
						dx = -10;
						break;
					case SWT.ARROW_RIGHT:
						dx = 10;
						break;
					case SWT.ARROW_UP:
						dy = -10;
						break;
					case SWT.ARROW_DOWN:
						dy = 10;
						break;
					case SWT.HOME:
						dx = -500;
						break;
					case SWT.END:
						dx = 500;
						break;
					case SWT.PAGE_UP:
						dy = -500;
						break;
					case SWT.PAGE_DOWN:
						dy = 500;
						break;
					default:
						return;
					}
					if (recentlyDraggedObject == current) {
						current.setStartX(current.getStartX() + dx);
						current.setStartY(current.getStartY() + dy);
					} else if (recentlyDraggedObject instanceof Wall) {
						Wall wall = (Wall) recentlyDraggedObject;
						int gx = wall.getGX();
						int gy = wall.getGY();
						if (dragHandle == 1) {
							wall.setGX(gx + dx);
							wall.setGY(gy + dy);
						} else {
							int width = wall.getWidth();
							double r = Math.toRadians(wall.getGAngle());
							int gx2 = (int) (gx + Math.cos(r) * width + D05);
							int gy2 = (int) (gy + Math.sin(r) * width + D05);
							double ddx = gx - gx2 - dx;
							double ddy = gy - gy2 - dy;
							double angle = ddx == 0 ? 90 : Math.toDegrees(Math.atan(ddy / ddx));
							if (ddx > 0)
								angle = ddy < 0 ? 180 + angle : angle - 180;
							wall.setGAngle(angle);
						}
					}
					floorplan.redraw();
					updateFloorplanDetails();
					return;
				}
			}
		});
		updateFloorplanDetails();
		itemViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				selectedItem = ((IStructuredSelection) event.getSelection()).getFirstElement();
				updateFloorplanDetails();
				floorplan.redraw();
			}
		});
		itemViewer.setSelection(new StructuredSelection(current));

	}

	private String captionUnitmft() {
		return unit == 'i' ? " (ft)" : " (m)"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	private String captionUnitcmin() {
		return unit == 'i' ? " (in)" : " (cm)"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected int toMm(int x) {
		return unit == 'i' ? (int) (x * 3.048d + 0.5d) : x * 10;
	}

	protected void updateItems(int x, int y, double a) {
		if (selectedItem instanceof Wall) {
			if (x >= 0)
				((Wall) selectedItem).setGX(x);
			if (y >= 0)
				((Wall) selectedItem).setGY(y);
			if (!Double.isNaN(a))
				((Wall) selectedItem).setGAngle(a);
		} else if (current != null) {
			if (x >= 0)
				current.setStartX(x);
			if (y >= 0)
				current.setStartY(y);
		}
		floorplan.redraw();
	}

	protected void updateFloorplanDetails() {
		IStructuredSelection selection = (IStructuredSelection) itemViewer.getSelection();
		Object firstElement = selection.getFirstElement();
		if (firstElement instanceof Wall) {
			Wall wall = (Wall) firstElement;
			xspinner.setSelection(fromMm(wall.getGX()));
			yspinner.setSelection(fromMm(wall.getGY()));
			aspinner.setSelection((int) (wall.getGAngle() * 10 + 0.5d));
			aspinner.setVisible(true);
			alabel.setVisible(true);
		} else if (current != null) {
			xspinner.setSelection(fromMm(current.getStartX()));
			yspinner.setSelection(fromMm(current.getStartY()));
			alabel.setVisible(false);
			aspinner.setVisible(false);
		}
	}

	private int fromMm(int x) {
		return unit == 'i' ? (int) (x / 3.048d + 0.5d) : (x + 5) / 10;
	}

	private void createDetails(Composite parent) {
		parent.setLayout(new GridLayout());
		detailTabfolder = new CTabFolder(parent, SWT.BOTTOM | SWT.BORDER);
		imageDetailViewer = createDetailViewer(createTabItem(detailTabfolder, Messages.ExhibitionEditDialog_Image),
				false);
		frameDetailViewer = createDetailViewer(createTabItem(detailTabfolder, Messages.ExhibitionEditDialog_Frame),
				true);
		detailTabfolder.setSelection(0);
	}

	private TreeViewer createDetailViewer(Composite parent, boolean frame) {
		TreeViewer viewer = new TreeViewer(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		Tree tree = viewer.getTree();
		GridData gd_tree = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd_tree.heightHint = 300;
		tree.setLayoutData(gd_tree);
		tree.setLinesVisible(true);
		tree.setHeaderVisible(true);
		createColumn(viewer, 0, 210, Messages.ExhibitionEditDialog_item, SWT.LEFT, frame);
		createColumn(viewer, 1, 60, Messages.ExhibitionEditDialog_x, SWT.RIGHT, frame);
		createColumn(viewer, 2, 60, Messages.ExhibitionEditDialog_y, SWT.RIGHT, frame);
		createColumn(viewer, 3, 60, Messages.ExhibitionEditDialog_w, SWT.RIGHT, frame);
		createColumn(viewer, 4, 60, Messages.ExhibitionEditDialog_h, SWT.RIGHT, frame);
		createColumn(viewer, 5, 40, Messages.ExhibitionEditDialog_dpi, SWT.RIGHT, frame);
		createColumn(viewer, 6, 100, "", SWT.LEFT, frame); //$NON-NLS-1$
		viewer.setContentProvider(new ITreeContentProvider() {

			public void inputChanged(Viewer aViewer, Object oldInput, Object newInput) {
				// do nothing
			}

			public void dispose() {
				// do nothing
			}

			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof Exhibition)
					return ((Exhibition) inputElement).getWall().toArray();
				return EKPTY;
			}

			public boolean hasChildren(Object element) {
				if (element instanceof Wall)
					return !((Wall) element).getExhibit().isEmpty();
				return false;
			}

			public Object getParent(Object element) {
				if (element instanceof Exhibit)
					return dbManager.obtainById(WallImpl.class, ((Exhibit) element).getWall_exhibit_parent());
				return null;
			}

			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof Wall) {
					List<String> ids = ((Wall) parentElement).getExhibit();
					List<ExhibitImpl> list = new ArrayList<ExhibitImpl>(ids.size());
					for (String id : ids) {
						ExhibitImpl obj = dbManager.obtainById(ExhibitImpl.class, id);
						if (obj != null)
							list.add(obj);
					}
					return list.toArray();
				}
				return EKPTY;
			}
		});
		viewer.setComparator(new ZViewerComparator() {
			@Override
			public int compare(Viewer aViewer, Object e1, Object e2) {
				if (e1 instanceof Wall && e2 instanceof Wall) {
					return UiUtilities.stringComparator.compare(((Wall) e1).getLocation(), ((Wall) e2).getLocation());
				} else if (e1 instanceof Exhibit && e2 instanceof Exhibit) {
					int x1 = ((Exhibit) e1).getX();
					int x2 = ((Exhibit) e2).getX();
					return x1 == x2 ? 0 : x1 < x2 ? -1 : 1;
				}
				return super.compare(aViewer, e1, e2);
			}
		});
		UiUtilities.installDoubleClickExpansion(viewer);
		return viewer;
	}

	private static Composite createTabItem(CTabFolder folder, String label) {
		Composite composite = UiUtilities.createTabPage(folder, label, null);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(1, false));
		return composite;
	}

	private void createColumn(TreeViewer aViewer, final int index, int width, String label, int align,
			final boolean frame) {
		TreeViewerColumn col = new TreeViewerColumn(aViewer, SWT.NONE);
		TreeColumn column = col.getColumn();
		column.setWidth(width);
		column.setText(label);
		column.setAlignment(align);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				int tara = 0;
				if (element instanceof Exhibit)
					tara = computeTara(frame, (Exhibit) element);
				switch (index) {
				case 0:
					if (element instanceof Wall)
						return ((Wall) element).getLocation();
					if (element instanceof Exhibit)
						return ((Exhibit) element).getTitle();
					break;
				case 1:
					if (element instanceof Exhibit)
						return printDistance(((Exhibit) element).getX(), tara);
					break;
				case 2:
					if (element instanceof Exhibit)
						return printDistance(((Exhibit) element).getY(), tara);
					break;
				case 3:
					if (element instanceof Exhibit)
						return printDimension(((Exhibit) element).getWidth(), tara);
					break;
				case 4:
					if (element instanceof Exhibit)
						return printDimension(((Exhibit) element).getHeight(), tara);
					break;
				case 5:
					if (element instanceof Exhibit) {
						Exhibit exhibit = (Exhibit) element;
						AssetImpl asset = dbManager.obtainAsset(exhibit.getAsset());
						if (asset != null)
							return String.valueOf((int) (asset.getWidth() * 25.4d / exhibit.getWidth()));
					}
					break;
				case 6:
					if (element instanceof Exhibit)
						return ((Exhibit) element).getSold() ? Messages.ExhibitionEditDialog_sold : ""; //$NON-NLS-1$
					break;
				}
				return ""; //$NON-NLS-1$
			}

			protected String printDistance(int dist, int tara) {
				af.setMaximumFractionDigits(2);
				af.setMinimumFractionDigits(2);
				if (unit == 'i')
					return af.format((dist + 2 * tara) / 304.8d) + " ft"; //$NON-NLS-1$
				return af.format((dist + tara) / 1000d) + " m"; //$NON-NLS-1$
			}

			protected String printDimension(int dim, int tara) {
				af.setMaximumFractionDigits(1);
				af.setMinimumFractionDigits(1);
				if (unit == 'i')
					return af.format((dim + 2 * tara) / 25.4d) + " in"; //$NON-NLS-1$
				return af.format((dim + 2 * tara) / 10d) + " cm"; //$NON-NLS-1$
			}
		});
	}

	@SuppressWarnings("unused")
	private void createOverview(final Composite comp) {
		comp.setLayout(new GridLayout());
		CGroup exhGroup = CGroup.create(comp, 1, Messages.ExhibitionEditDialog_exhibition);
		new Label(exhGroup, SWT.NONE).setText(Messages.ExhibitionEditDialog_name);
		nameField = new Text(exhGroup, SWT.BORDER);
		nameField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		nameField.addModifyListener(modifyListener);
		final Label descriptionLabel = new Label(exhGroup, SWT.NONE);
		descriptionLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		descriptionLabel.setText(Messages.ExhibitionEditDialog_description);
		final GridData gd_descriptionField = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd_descriptionField.widthHint = 250;
		gd_descriptionField.heightHint = 120;
		descriptionField = new CheckedText(exhGroup, SWT.WRAP | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER);
		descriptionField.setLayoutData(gd_descriptionField);
		CGroup infoGroup = CGroup.create(comp, 1, Messages.ExhibitionEditDialog_info_plate);
		new Label(infoGroup, SWT.NONE).setText(Messages.ExhibitionEditDialog_info_plate_position);
		infoPosField = new Combo(infoGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		infoPosField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		infoPosField.setItems(new String[] { Messages.ExhibitionEditDialog_no_info_plate,
				Messages.ExhibitionEditDialog_right_to_door, Messages.ExhibitionEditDialog_left_to_door });
		final Label infoLabel = new Label(infoGroup, SWT.NONE);
		infoLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		infoLabel.setText(Messages.ExhibitionEditDialog_info);
		final GridData gd_infoField = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd_infoField.widthHint = 250;
		gd_infoField.heightHint = 70;
		infoField = new CheckedText(infoGroup, SWT.WRAP | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER);
		infoField.setLayoutData(gd_descriptionField);
		new Label(infoGroup, SWT.NONE);
		showCreditsButton = WidgetFactory.createCheckButton(infoGroup, Messages.ExhibitionEditDialog_show_credits,
				null);
	}

	private void updateButtons() {
		Button okButton = getButton(IDialogConstants.OK_ID);
		if (okButton != null) {
			boolean enabled = validate() && !readonly;
			getShell().setModified(enabled);
			okButton.setEnabled(enabled);
		}
		boolean visible = tabFolder.getSelection().equals(detailsTabItem);
		Button pdfButton = getButton(PDFBUTTON);
		if (pdfButton != null)
			pdfButton.setVisible(visible);
		Button printButton = getButton(PRINTBUTTON);
		if (printButton != null)
			printButton.setVisible(visible);
		boolean grid = showGridButton.getSelection();
		snapToGridField.setEnabled(grid);
		gridSizeField.setEnabled(grid);
	}

	private boolean validate() {
		String name = nameField.getText();
		if (name.isEmpty()) {
			setErrorMessage(Messages.ExhibitionEditDialog_specify_name);
			return false;
		}
		List<ExhibitionImpl> set = dbManager.obtainObjects(ExhibitionImpl.class, "name", name, QueryField.EQUALS); //$NON-NLS-1$
		for (ExhibitionImpl obj : set) {
			if (obj != current) {
				setErrorMessage(Messages.ExhibitionEditDialog_name_already_exists);
				return false;
			}
		}
		if (!logoField.getText().isEmpty()) {
			File logoFile = new File(logoField.getText());
			if (!logoFile.exists()) {
				setErrorMessage(Messages.ExhibitionEditDialog_nameplate_does_not_exist);
				return false;
			}
		}
		if (!audioField.getText().isEmpty()) {
			File audioFile = new File(audioField.getText());
			if (!audioFile.exists()) {
				setErrorMessage(Messages.ExhibitionEditDialog_audio_file_does_not_exist);
				return false;
			}
		}
		setErrorMessage(null);
		return true;
	}

	private void fillValues(ExhibitionImpl show, boolean template) {
		initValues();
		if (show != null) {
			if (!template) {
				nameField.setText(show.getName());
				descriptionField.setText(show.getDescription());
			}
			infoField.setText(show.getInfo() == null ? "" : show.getInfo()); //$NON-NLS-1$
			infoPosField.select(show.getInfoPlatePosition() + 1);
			showCreditsButton.setSelection(!show.getHideCredits());
			viewingHeightField.setSelection(fromMm(show.getDefaultViewingHeight()));
			varianceField.setSelection(fromMm(show.getVariance()));
			showGridButton.setSelection(show.getShowGrid());
			gridSizeField.setSelection(show.getGridSize() / 10);
			snapToGridField.setSelection(show.getSnapToGrid());
			String labelFontFamily = show.getLabelFontFamily();
			if (labelFontFamily != null && !labelFontFamily.isEmpty()) {
				if (selectedFont != null)
					selectedFont.dispose();
				selectedFont = new Font(getShell().getDisplay(), labelFontFamily, show.getLabelFontSize(), SWT.NORMAL);
				fontButton.setFont(selectedFont);
			}
			sequenceCombo.select(show.getLabelSequence());
			String defaultDescription = show.getDefaultDescription();
			if (defaultDescription != null)
				labelTextField.setText(defaultDescription);
			Integer a = show.getLabelAlignment();
			Integer d = show.getLabelDistance();
			Integer i = show.getLabelIndent();
			labelLayoutGroup.fillValues(show.getHideLabel(), a == null ? Constants.DEFAULTLABELALIGNMENT : a.intValue(),
					d == null ? Constants.DEFAULTLABELDISTANCE : d.intValue(),
					i == null ? Constants.DEFAULTLABELINDENT : i.intValue());
			matWidthField.setSelection(fromMmi(show.getMatWidth()));
			frameWidthField.setSelection(fromMmi(show.getFrameWidth()));
			matColorGroup.setRGB(show.getMatColor());
			frameColorGroup.setRGB(show.getFrameColor());
			groundColorGroup.setRGB(show.getGroundColor());
			horizonColorGroup.setRGB(show.getHorizonColor());
			ceilingColorGroup.setRGB(show.getCeilingColor());
			String soundTrack = show.getAudio();
			audioField.setText(soundTrack == null ? "" : soundTrack); //$NON-NLS-1$
			setCondText(audioField, soundTrack);
			setCondText(logoField, show.getLogo());
			String link = show.getPageName();
			if (link != null && !link.isEmpty())
				linkField.setText(link);
			List<String> keywords = show.getKeyword();
			keywordField.setText(Core.toStringList(keywords, '\n'));
			setCondText(copyrightField, show.getCopyright());
			setCondText(contactField, show.getContactName());
			setCondText(emailField, show.getEmail());
			setCondText(weburlField, show.getWebUrl());
			qualityGroup.fillValues(show.getApplySharpening(), show.getRadius(), show.getAmount(), show.getThreshold(),
					show.getJpegQuality(), show.getScalingMethod());
			watermarkButton.setSelection(show.getAddWatermark());
			outputTargetGroup.setLocalFolder(show.getOutputFolder());
			outputTargetGroup.setFtpDir(show.getFtpDir());
			outputTargetGroup.setTarget(show.getIsFtp() ? Constants.FTP : Constants.FILE);
		}
		af.setMaximumFractionDigits(1);
		af.setMinimumFractionDigits(0);
		updateDetailViewers(show);
		updateColorGroups();
		updateButtons();
	}

	private int fromMmi(int x) {
		return unit == 'i' ? (int) (x / 2.54d + 0.5d) : x;
	}

	private void updateDetailViewers(ExhibitionImpl show) {
		if (imageDetailViewer != null) {
			imageDetailViewer.setInput(show);
			imageDetailViewer.expandAll();
		}
		if (frameDetailViewer != null) {
			frameDetailViewer.setInput(show);
			frameDetailViewer.expandAll();
		}
	}

	private void initValues() {
		outputTargetGroup.initValues(settings);
		try {
			infoPosField.select(settings.getInt(INFOPLATEPOS) + 1);
		} catch (NumberFormatException e) {
			infoPosField.select(1);
		}
		showCreditsButton.setSelection(!settings.getBoolean(HIDECREDITS));
		String link = settings.get(LINKPAGE);
		if (link == null || link.isEmpty())
			link = "index.html"; //$NON-NLS-1$
		setCondText(linkField, link);
		setCondText(logoField, settings.get(LOGO));
		String copyright = settings.get(COPYRIGHT);
		if (copyright == null || copyright.isEmpty())
			copyright = new GregorianCalendar().get(Calendar.YEAR) + " "; //$NON-NLS-1$
		String audioTrack = settings.get(AUDIO);
		setCondText(audioField, audioTrack);
		setCondText(copyrightField, copyright);
		setCondText(contactField, settings.get(CONTACT));
		setCondText(emailField, settings.get(EMAIL));
		setCondText(weburlField, settings.get(WEBURL));
		watermarkButton.setSelection(settings.getBoolean(WATER_MARK));
		qualityGroup.fillValues(settings);
		frameColorGroup.fillValues(settings, FRAME_COLOR, 8, 8, 8);
		matColorGroup.fillValues(settings, MAT_COLOR, 255, 255, 252);
		ceilingColorGroup.fillValues(settings, CEILING_COLOR, 148, 146, 123);
		horizonColorGroup.fillValues(settings, HORIZON_COLOR, 0, 0, 0);
		groundColorGroup.fillValues(settings, GROUND_COLOR, 201, 199, 176);
		try {
			viewingHeightField.setSelection(fromMm(settings.getInt(VIEWING_HEIGHT)));
		} catch (NumberFormatException e1) {
			viewingHeightField.setSelection(fromMm(1700));
		}
		try {
			frameWidthField.setSelection(fromMmi(settings.getInt(FRAME_WIDTH)));
		} catch (NumberFormatException e2) {
			frameWidthField.setSelection(0);
		}
		try {
			matWidthField.setSelection(fromMmi(settings.getInt(MAT_WIDTH)));
		} catch (NumberFormatException e2) {
			matWidthField.setSelection(0);
		}
		try {
			varianceField.setSelection(fromMm(settings.getInt(VARIANCE)));
		} catch (NumberFormatException e1) {
			varianceField.setSelection(fromMm(200));
		}
		if (selectedFont != null)
			selectedFont.dispose();
		String family = settings.get(FONTFAMILY);
		try {
			int size = settings.getInt(FONTSIZE);
			if (family != null && size > 0)
				fontButton.setFont(selectedFont = new Font(getShell().getDisplay(), family, size, SWT.NORMAL));
		} catch (NumberFormatException e) {
			// ignore
		}
		try {
			sequenceCombo.select(settings.getInt(LABELSEQUENCE));
		} catch (NumberFormatException e) {
			sequenceCombo.select(Constants.EXHLABEL_TIT_DES_CRED);
		}
		String dfi = settings.get(DEFAULTINFO);
		labelTextField.setText(dfi != null ? dfi : Messages.ExhibitionEditDialog_inkjet_print);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		if (current != null) {
			createButton(parent, PDFBUTTON, Messages.ExhibitionEditDialog_PDFaction, false);
			createButton(parent, PRINTBUTTON, Messages.ExhibitionEditDialog_print, false);
		}
		super.createButtonsForButtonBar(parent);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
		case PDFBUTTON:
			BusyIndicator.showWhile(getShell().getDisplay(), () -> pdf());
			return;
		case PRINTBUTTON:
			print();
			return;
		default:
			super.buttonPressed(buttonId);
		}
	}

	private void pdf() {
		FileDialog dialog = new FileDialog(getShell(), SWT.SAVE | SWT.SINGLE);
		dialog.setFilterExtensions(new String[] { "*.pdf;*.PDF", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
		dialog.setFilterNames(
				new String[] { Messages.ExhibitionEditDialog_pdf_files, Messages.ExhibitionEditDialog_all_files });
		dialog.setOverwrite(true);
		dialog.setText(Messages.ExhibitionEditDialog_pdf_target_file);
		String fileName = dialog.open();
		if (fileName != null) {
			if (!fileName.toUpperCase().endsWith(".PDF")) //$NON-NLS-1$
				fileName += ".pdf"; //$NON-NLS-1$
			final File targetFile = new File(fileName);
			BusyIndicator.showWhile(getShell().getDisplay(), () -> {
				targetFile.delete();
				writeDocument(createDocument(), targetFile);
			});
			Program.launch(fileName);
		}
	}

	private static Document createDocument() {
		Document document = new Document();
		document.addCreationDate();
		document.addCreator(Constants.APPLICATION_NAME);
		document.addAuthor(System.getProperty("user.name")); //$NON-NLS-1$
		document.setPageSize(PageSize.A4);
		document.setMargins(20, 10, 10, 10);
		return document;
	}

	private void writeDocument(Document document, File targetFile) {
		boolean frame = detailTabfolder.getSelectionIndex() == 1;
		try (FileOutputStream out = new FileOutputStream(targetFile)) {
			PdfWriter writer = PdfWriter.getInstance(document, out);
			document.open();
			writer.setPageEvent(new PdfPageEventHelper() {
				int pageNo = 0;
				com.itextpdf.text.Font ffont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA,
						9, com.itextpdf.text.Font.NORMAL, BaseColor.DARK_GRAY);

				@Override
				public void onEndPage(PdfWriter w, Document d) {
					PdfContentByte cb = w.getDirectContent();
					Phrase footer = new Phrase(String.valueOf(++pageNo), ffont);
					ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, footer,
							(document.right() - document.left()) / 2 + document.leftMargin(), document.bottom(), 0);
				}
			});
			String tit = NLS.bind(Messages.ExhibitionEditDialog_exhibition_name, nameField.getText());
			Paragraph p = new Paragraph(tit,
					FontFactory.getFont(FontFactory.HELVETICA, 14, com.itextpdf.text.Font.BOLD, BaseColor.BLACK));
			p.setAlignment(Element.ALIGN_CENTER);
			p.setSpacingAfter(8);
			document.add(p);
			String subtitle = NLS.bind(Messages.ExhibitionEditDialog_image_list, Constants.DFDT.format(new Date()),
					frame ? Messages.ExhibitionEditDialog_image_sizes : Messages.ExhibitionEditDialog_frame_sizes);
			p = new Paragraph(subtitle,
					FontFactory.getFont(FontFactory.HELVETICA, 10, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK));
			p.setAlignment(Element.ALIGN_CENTER);
			p.setSpacingAfter(14);
			document.add(p);
			p = new Paragraph(descriptionField.getText(),
					FontFactory.getFont(FontFactory.HELVETICA, 10, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK));
			p.setAlignment(Element.ALIGN_CENTER);
			p.setSpacingAfter(10);
			document.add(p);
			p = new Paragraph(infoField.getText(),
					FontFactory.getFont(FontFactory.HELVETICA, 10, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK));
			p.setAlignment(Element.ALIGN_CENTER);
			p.setSpacingAfter(10);
			document.add(p);
			IDbManager db = Core.getCore().getDbManager();
			List<Wall> walls = current.getWall();
			Wall[] sortedWalls = walls.toArray(new Wall[walls.size()]);
			Arrays.sort(sortedWalls, new Comparator<Wall>() {
				public int compare(Wall o1, Wall o2) {
					return o1.getLocation().compareToIgnoreCase(o2.getLocation());
				}
			});
			for (Wall wall : sortedWalls) {
				p = new Paragraph(wall.getLocation(),
						FontFactory.getFont(FontFactory.HELVETICA, 12, com.itextpdf.text.Font.BOLD, BaseColor.BLACK));
				p.setAlignment(Element.ALIGN_LEFT);
				p.setSpacingAfter(12);
				document.add(p);
				PdfPTable table = new PdfPTable(7);
				// table.setBorderWidth(1);
				// table.setBorderColor(new Color(224, 224, 224));
				// table.setPadding(5);
				// table.setSpacing(0);
				table.setWidths(new int[] { 8, 33, 13, 13, 20, 13, 20 });
				table.addCell(createTableHeader(Messages.ExhibitionEditDialog_No, Element.ALIGN_RIGHT));
				table.addCell(createTableHeader(Messages.ExhibitionEditDialog_title, Element.ALIGN_LEFT));
				table.addCell(createTableHeader(Messages.ExhibitionEditDialog_xpos, Element.ALIGN_RIGHT));
				table.addCell(createTableHeader(Messages.ExhibitionEditDialog_height, Element.ALIGN_RIGHT));
				table.addCell(createTableHeader(Messages.ExhibitionEditDialog_size, Element.ALIGN_RIGHT));
				table.addCell(createTableHeader(Messages.ExhibitionEditDialog_dpi, Element.ALIGN_RIGHT));
				table.addCell(createTableHeader("", Element.ALIGN_LEFT)); //$NON-NLS-1$
				// table.endHeaders();
				List<ExhibitImpl> exhibits = new ArrayList<ExhibitImpl>();
				for (String exhibitId : wall.getExhibit()) {
					ExhibitImpl exhibit = db.obtainById(ExhibitImpl.class, exhibitId);
					if (exhibit != null)
						exhibits.add(exhibit);
				}
				Collections.sort(exhibits, new Comparator<ExhibitImpl>() {
					public int compare(ExhibitImpl e1, ExhibitImpl e2) {
						int x1 = ((Exhibit) e1).getX();
						int x2 = ((Exhibit) e2).getX();
						return x1 == x2 ? 0 : x1 < x2 ? -1 : 1;
					}
				});
				int no = 1;
				for (ExhibitImpl exhibit : exhibits) {
					int tara = computeTara(frame, exhibit);
					table.addCell(createTableCell(String.valueOf(no++), Element.ALIGN_RIGHT));
					table.addCell(createTableCell(exhibit.getTitle(), Element.ALIGN_LEFT));
					af.setMaximumFractionDigits(2);
					af.setMinimumFractionDigits(2);
					String x = af.format((exhibit.getX() - tara) / 1000d);
					table.addCell(createTableCell(NLS.bind("{0} m", x), Element.ALIGN_RIGHT)); //$NON-NLS-1$
					String y = af.format((exhibit.getY() + tara) / 1000d);
					table.addCell(createTableCell(NLS.bind("{0} m", y), Element.ALIGN_RIGHT)); //$NON-NLS-1$
					af.setMaximumFractionDigits(1);
					af.setMinimumFractionDigits(1);
					String h = af.format((exhibit.getHeight() + 2 * tara) / 10d);
					int width = exhibit.getWidth();
					String w = af.format((width + 2 * tara) / 10d);
					table.addCell(createTableCell(NLS.bind("{0} x {1} cm", w, h), Element.ALIGN_RIGHT)); //$NON-NLS-1$
					AssetImpl asset = dbManager.obtainAsset(exhibit.getAsset());
					if (asset != null) {
						int pixels = asset.getWidth();
						double dpi = pixels * 25.4d / width;
						table.addCell(createTableCell(String.valueOf((int) dpi), Element.ALIGN_RIGHT));
					} else
						table.addCell(""); //$NON-NLS-1$
					table.addCell(createTableCell(exhibit.getSold() ? Messages.ExhibitionEditDialog_sold : "", //$NON-NLS-1$
							Element.ALIGN_LEFT));
				}
				document.add(table);
			}
			document.close();
		} catch (DocumentException e) {
			UiActivator.getDefault().logError(Messages.ExhibitionEditDialog_internal_error_writing_pdf, e);
		} catch (IOException e) {
			UiActivator.getDefault().logError(Messages.ExhibitionEditDialog_io_error_writing_pdf, e);
		}
	}

	private static PdfPCell createTableHeader(String header, int alignment) {
		PdfPCell cell = new PdfPCell(new Paragraph(header,
				FontFactory.getFont(FontFactory.HELVETICA, 10, com.itextpdf.text.Font.BOLD, BaseColor.BLACK)));
		cell.setBorderColor(new BaseColor(224, 224, 224));
		cell.setBorderWidth(1);
		cell.setPadding(5);
		cell.setHorizontalAlignment(alignment);
		return cell;
	}

	private static PdfPCell createTableCell(String value, int alignment) {
		PdfPCell cell = new PdfPCell(new Paragraph(value,
				FontFactory.getFont(FontFactory.HELVETICA, 9, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK)));
		cell.setBorderColor(new BaseColor(224, 224, 224));
		cell.setBorderWidth(1);
		cell.setPadding(5);
		cell.setHorizontalAlignment(alignment);
		return cell;
	}

	private void print() {
		final String name = nameField.getText();
		PrintDialog dialog = new PrintDialog(getShell());
		dialog.setText(Messages.ExhibitionEditDialog_printing);
		if (printerData != null)
			dialog.setPrinterData(printerData);
		printerData = dialog.open();
		if (printerData != null) {
			BusyIndicator.showWhile(getShell().getDisplay(), () -> {
				String fontName = getShell().getDisplay().getSystemFont().getFontData()[0].getName();
				PaperClips.print(new PrintJob(NLS.bind(Messages.ExhibitionEditDialog_exh_image_list, name),
						createHeader(createPrint(fontName), fontName)), printerData);
			});
		}
	}

	private static Print createHeader(Print body, String fontName) {
		PageNumberPageDecoration deco = new PageNumberPageDecoration();
		deco.setFontData(new FontData(fontName, 8, SWT.NORMAL));
		deco.setRGB(new RGB(128, 128, 128));
		deco.setAlign(SWT.RIGHT);
		return new PagePrint(deco, 5, body);
	}

	private Print createPrint(String fontName) {
		boolean frame = detailTabfolder.getSelectionIndex() == 1;
		FontData titleFont = new FontData(fontName, 14, SWT.BOLD);
		FontData sectionFont = new FontData(fontName, 12, SWT.BOLD);
		FontData headerFont = new FontData(fontName, 10, SWT.BOLD);
		FontData textFont = new FontData(fontName, 10, SWT.NORMAL);
		GridPrint grid = new GridPrint("p, d, p, p, d, p, d", new DefaultGridLook( //$NON-NLS-1$
				10, 5));
		String tit = NLS.bind(Messages.ExhibitionEditDialog_exhibition_name, nameField.getText(),
				frame ? Messages.ExhibitionEditDialog_image_sizes : Messages.ExhibitionEditDialog_frame_sizes);
		grid.add(SWT.CENTER, new TextPrint(tit, titleFont, SWT.CENTER), GridPrint.REMAINDER);
		String subtitle = NLS.bind(Messages.ExhibitionEditDialog_image_list, Constants.DFDT.format(new Date()));
		grid.add(SWT.CENTER, new TextPrint(subtitle, textFont, SWT.CENTER), GridPrint.REMAINDER);
		grid.add(SWT.CENTER, new TextPrint(descriptionField.getText(), textFont, SWT.CENTER), GridPrint.REMAINDER);
		grid.add(SWT.CENTER, new TextPrint(infoField.getText(), textFont, SWT.CENTER), GridPrint.REMAINDER);
		List<Wall> walls = current.getWall();
		Wall[] sortedWalls = walls.toArray(new Wall[walls.size()]);
		Arrays.sort(sortedWalls, new Comparator<Wall>() {
			public int compare(Wall o1, Wall o2) {
				return o1.getLocation().compareToIgnoreCase(o2.getLocation());
			}
		});
		for (Wall wall : sortedWalls) {
			grid.add(SWT.LEFT, new TextPrint(wall.getLocation(), sectionFont, SWT.LEFT), GridPrint.REMAINDER);
			grid.add(new TextPrint(Messages.ExhibitionEditDialog_No, headerFont, SWT.LEFT));
			grid.add(new TextPrint(Messages.ExhibitionEditDialog_title, headerFont, SWT.LEFT));
			grid.add(SWT.RIGHT, new TextPrint(Messages.ExhibitionEditDialog_xpos, headerFont, SWT.RIGHT));
			grid.add(SWT.RIGHT, new TextPrint(Messages.ExhibitionEditDialog_height, headerFont, SWT.RIGHT));
			grid.add(new TextPrint(Messages.ExhibitionEditDialog_size, headerFont, SWT.LEFT));
			grid.add(SWT.RIGHT, new TextPrint(Messages.ExhibitionEditDialog_dpi, headerFont, SWT.RIGHT));
			grid.add(SWT.LEFT, new TextPrint("", headerFont, SWT.LEFT)); //$NON-NLS-1$
			IDbManager db = Core.getCore().getDbManager();
			List<ExhibitImpl> exhibits = new ArrayList<ExhibitImpl>();
			for (String exhibitId : wall.getExhibit()) {
				ExhibitImpl exhibit = db.obtainById(ExhibitImpl.class, exhibitId);
				if (exhibit != null)
					exhibits.add(exhibit);
			}
			Collections.sort(exhibits, new Comparator<ExhibitImpl>() {
				public int compare(ExhibitImpl e1, ExhibitImpl e2) {
					int x1 = ((Exhibit) e1).getX();
					int x2 = ((Exhibit) e2).getX();
					return x1 == x2 ? 0 : x1 < x2 ? -1 : 1;
				}
			});
			int no = 1;
			for (ExhibitImpl exhibit : exhibits) {
				int tara = computeTara(frame, exhibit);
				grid.add(SWT.RIGHT, new TextPrint(String.valueOf(no++), textFont, SWT.RIGHT));
				grid.add(new TextPrint(exhibit.getTitle(), textFont, SWT.LEFT));
				af.setMaximumFractionDigits(2);
				af.setMinimumFractionDigits(2);
				grid.add(SWT.RIGHT, new TextPrint(NLS.bind("{0} m", af.format((exhibit.getX() - tara) / 1000d)), //$NON-NLS-1$
						textFont, SWT.RIGHT));
				grid.add(SWT.RIGHT, new TextPrint(NLS.bind("{0} m", af.format((exhibit.getY() + tara) / 1000d)), //$NON-NLS-1$
						textFont, SWT.RIGHT));
				af.setMaximumFractionDigits(1);
				af.setMinimumFractionDigits(1);
				int width = exhibit.getWidth();
				grid.add(new TextPrint(NLS.bind("{0} x {1} cm", //$NON-NLS-1$
						af.format((width + 2 * tara) / 10d), af.format((exhibit.getHeight() + 2 * tara) / 10d)),
						textFont, SWT.LEFT));
				AssetImpl asset = dbManager.obtainAsset(exhibit.getAsset());
				String reso = asset != null ? String.valueOf((int) (asset.getWidth() * 25.4d / width)) : ""; //$NON-NLS-1$
				grid.add(SWT.RIGHT, new TextPrint(reso, textFont, SWT.RIGHT));
				grid.add(SWT.LEFT,
						new TextPrint(exhibit.getSold() ? Messages.ExhibitionEditDialog_sold : "", textFont, SWT.LEFT)); //$NON-NLS-1$
			}
		}
		return grid;
	}

	private int computeTara(boolean frame, Exhibit exhibit) {
		int tara = 0;
		if (frame) {
			Integer frameWidth = exhibit.getFrameWidth();
			if (frameWidth != null)
				tara += frameWidth;
			else
				tara += toMmi(frameWidthField.getSelection());
			Integer matWidth = exhibit.getMatWidth();
			if (matWidth != null)
				tara += matWidth;
			else
				tara += toMmi(matWidthField.getSelection());
		}
		return tara;
	}

	private int toMmi(int x) {
		return unit == 'i' ? (int) (x * 2.54d + 0.5d) : x;
	}

	@Override
	protected void okPressed() {
		BusyIndicator.showWhile(getShell().getDisplay(), () -> {
			// Must create a new instance if not undoable
			if (canUndo)
				result = current;
			else {
				result = new ExhibitionImpl();
				if (current != null) {
					result.setStringId(current.getStringId());
					result.setWall(current.getWall());
					for (Wall wall : result.getWall())
						wall.setExhibition_wall_parent(result);
					result.setGroup_exhibition_parent(current.getGroup_exhibition_parent());
					result.setStartX(current.getStartX());
					result.setStartY(current.getStartY());
				}
			}
			result.setName(nameField.getText());
			if (itemViewer != null
					&& !(((IStructuredSelection) itemViewer.getSelection()).getFirstElement() instanceof Wall)) {
				result.setStartX(toMm(xspinner.getSelection()));
				result.setStartY(toMm(yspinner.getSelection()));
			}
			result.setDescription(descriptionField.getText());
			result.setInfo(infoField.getText());
			result.setInfoPlatePosition(Math.max(-1, infoPosField.getSelectionIndex() - 1));
			result.setHideCredits(!showCreditsButton.getSelection());
			result.setDefaultViewingHeight(toMm(viewingHeightField.getSelection()));
			result.setVariance(varianceField.getSelection() * 10);
			result.setShowGrid(showGridButton.getSelection());
			result.setSnapToGrid(snapToGridField.getSelection());
			result.setGridSize(gridSizeField.getSelection() * 10);
			if (selectedFont != null) {
				FontData fontData = selectedFont.getFontData()[0];
				result.setLabelFontFamily(fontData.getName());
				result.setLabelFontSize(fontData.getHeight());
			}
			result.setLabelSequence(sequenceCombo.getSelectionIndex());
			result.setDefaultDescription(labelTextField.getText());
			result.setHideLabel(labelLayoutGroup.isHide());
			result.setLabelAlignment(labelLayoutGroup.getAlign());
			result.setLabelDistance(labelLayoutGroup.getDist());
			result.setLabelIndent(labelLayoutGroup.getIndent());
			result.setMatWidth(toMmi(matWidthField.getSelection()));
			result.setMatColor(matColorGroup.getRGB());
			result.setFrameWidth(toMmi(frameWidthField.getSelection()));
			result.setFrameColor(frameColorGroup.getRGB());
			result.setGroundColor(groundColorGroup.getRGB());
			result.setHorizonColor(horizonColorGroup.getRGB());
			result.setCeilingColor(ceilingColorGroup.getRGB());
			result.setAudio(audioField.getText());
			result.setLogo(logoField.getText());
			result.setPageName(linkField.getText());
			result.setKeyword(Core.fromStringList(keywordField.getText(), "\n")); //$NON-NLS-1$
			result.setContactName(contactField.getText());
			result.setCopyright(copyrightField.getText());
			result.setEmail(emailField.getText());
			result.setWebUrl(weburlField.getText());
			result.setApplySharpening(qualityGroup.getApplySharpening());
			result.setAmount(qualityGroup.getAmount());
			result.setRadius(qualityGroup.getRadius());
			result.setThreshold(qualityGroup.getThreshold());
			result.setJpegQuality(qualityGroup.getJpegQuality());
			result.setAddWatermark(watermarkButton.getSelection());
			result.setOutputFolder(outputTargetGroup.getLocalFolder());
			FtpAccount ftpDir = outputTargetGroup.getFtpDir();
			result.setFtpDir(ftpDir != null ? ftpDir.getName() : null);
			if (!canUndo)
				dbManager.safeTransaction(new Runnable() {
					public void run() {
						if (importGroup != null)
							importIntoGallery(result, importGroup.getFromItem());
						if (group == null) {
							String groupId = (current != null) ? current.getGroup_exhibition_parent()
									: Constants.GROUP_ID_EXHIBITION;
							if (groupId == null)
								groupId = Constants.GROUP_ID_EXHIBITION;
							group = dbManager.obtainById(GroupImpl.class, groupId);
						}
						if (group == null) {
							group = new GroupImpl(Messages.ExhibitionEditDialog_exhibitions, false,
									Constants.INHERIT_LABEL, null, 0, null);
							group.setStringId(Constants.GROUP_ID_EXHIBITION);
						}
						if (current != null)
							group.removeExhibition(current.getStringId());
						group.addExhibition(result.getStringId());
						result.setGroup_exhibition_parent(group.getStringId());
						if (current != null)
							dbManager.delete(current);
						for (Wall wall : result.getWall())
							dbManager.store(wall);
						dbManager.store(result);
						dbManager.store(group);
					}
				});
			saveSettings(result);
		});
		super.okPressed();
	}

	protected void saveSettings(ExhibitionImpl gallery) {
		outputTargetGroup.saveValues(settings);
		settings.put(LINKPAGE, gallery.getPageName());
		settings.put(LOGO, gallery.getLogo());
		settings.put(COPYRIGHT, gallery.getCopyright());
		settings.put(CONTACT, gallery.getContactName());
		settings.put(EMAIL, gallery.getEmail());
		settings.put(WEBURL, gallery.getWebUrl());
		settings.put(WATER_MARK, gallery.getAddWatermark());
		frameColorGroup.saveSettings(settings, FRAME_COLOR);
		matColorGroup.saveSettings(settings, MAT_COLOR);
		ceilingColorGroup.saveSettings(settings, CEILING_COLOR);
		horizonColorGroup.saveSettings(settings, HORIZON_COLOR);
		groundColorGroup.saveSettings(settings, GROUND_COLOR);
		settings.put(MAT_WIDTH, gallery.getMatWidth());
		settings.put(FRAME_WIDTH, gallery.getFrameWidth());
		settings.put(VIEWING_HEIGHT, toMm(viewingHeightField.getSelection()));
		settings.put(VARIANCE, toMm(varianceField.getSelection()));
		if (selectedFont != null) {
			FontData fontData = selectedFont.getFontData()[0];
			settings.put(FONTFAMILY, fontData.getName());
			settings.put(FONTSIZE, fontData.getHeight());
		}
		settings.put(INFOPLATEPOS, infoPosField.getSelectionIndex() - 1);
		settings.put(HIDECREDITS, !showCreditsButton.getSelection());
		settings.put(LABELSEQUENCE, sequenceCombo.getSelectionIndex());
		settings.put(DEFAULTINFO, labelTextField.getText());
		qualityGroup.saveSettings(settings);
		String audio = gallery.getAudio();
		if (audio != null)
			settings.put(AUDIO, audio);
		audioField.saveValues();
		logoField.saveValues();
	}

	private void importIntoGallery(ExhibitionImpl show, IIdentifiableObject obj) {
		int pos = 100 + show.getFrameWidth() + show.getMatWidth();
		if (obj instanceof SlideShowImpl) {
			int seqNo = 0;
			double resfac = DPI300;
			WallImpl newWall = null;
			int defaultViewingHeight = show.getDefaultViewingHeight();
			int variance = show.getVariance();
			for (String slideId : ((SlideShowImpl) obj).getEntry()) {
				SlideImpl slide = dbManager.obtainById(SlideImpl.class, slideId);
				if (slide != null) {
					String assetId = slide.getAsset();
					if (assetId == null) {
						if (newWall != null) {
							dbManager.store(newWall);
							show.addWall(newWall);
						}
						newWall = createNewWall(++seqNo);
						pos = 100 + show.getFrameWidth() + show.getMatWidth();
					} else {
						if (newWall == null)
							newWall = createNewWall(++seqNo);
						AssetImpl asset = dbManager.obtainAsset(assetId);
						if (!ExhibitionView.accepts(asset))
							continue;
						Date dateCreated = asset.getDateTimeOriginal();
						if (dateCreated == null)
							dateCreated = asset.getDateTime();
						int h = (int) (asset.getHeight() * resfac);
						int w = (int) (asset.getWidth() * resfac);
						int v = (int) (Math.random() * variance + 0.5d) - variance / 2;
						int y = defaultViewingHeight + v + h / 2;
						if (variance > 0 && show.getShowGrid() && show.getSnapToGrid()) {
							org.eclipse.swt.graphics.Point pnt = UiUtilities.snapToGrid(pos, y, w, h,
									newWall.getHeight(), show.getGridSize(), show.getGridSize());
							pos = pnt.x;
							y = pnt.y;
						}
						ExhibitImpl newExhibit = new ExhibitImpl(slide.getCaption(), slide.getDescription(),
								Core.toStringList(asset.getArtist(), " "), //$NON-NLS-1$
								(dateCreated == null) ? "" //$NON-NLS-1$
										: EDF.format(dateCreated),
								pos, y, w, h, null, null, null, null, false, null, null, null, null,
								asset.getStringId());
						pos += 2 * w;
						dbManager.store(newExhibit);
						newWall.addExhibit(newExhibit.getStringId());
					}
				}
			}
			if (newWall != null) {
				dbManager.store(newWall);
				show.addWall(newWall);
			}
		} else if (obj instanceof ExhibitionImpl) {
			for (Wall wall : ((ExhibitionImpl) obj).getWall()) {
				Wall newWall = new WallImpl(wall.getLocation(), wall.getX(), wall.getY(), wall.getWidth(),
						wall.getHeight(), wall.getGX(), wall.getGY(), wall.getGAngle(), wall.getColor());
				for (String exhibitId : wall.getExhibit()) {
					ExhibitImpl exhibit = dbManager.obtainById(ExhibitImpl.class, exhibitId);
					if (exhibit != null) {
						ExhibitImpl newExhibit = new ExhibitImpl(exhibit.getTitle(), exhibit.getDescription(),
								exhibit.getCredits(), exhibit.getDate(), exhibit.getX(), exhibit.getY(),
								exhibit.getWidth(), exhibit.getHeight(), exhibit.getMatWidth(), exhibit.getMatColor(),
								exhibit.getFrameWidth(), exhibit.getFrameColor(), exhibit.getSold(),
								exhibit.getHideLabel(), exhibit.getLabelAlignment(), exhibit.getLabelDistance(),
								exhibit.getLabelIndent(), exhibit.getAsset());
						dbManager.store(newExhibit);
						newWall.addExhibit(newExhibit.getStringId());
					}
				}
				dbManager.store(newWall);
				show.addWall(newWall);
			}
		} else if (obj instanceof WebGalleryImpl) {
			int seqNo = 0;
			for (Storyboard storyboard : ((WebGalleryImpl) obj).getStoryboard()) {
				WallImpl newWall = createNewWall(++seqNo);
				pos = 100 + show.getFrameWidth() + show.getMatWidth();
				int incr = (newWall.getWidth() - 2 * pos) / (storyboard.getExhibit().size() + 1);
				for (String exhibitId : storyboard.getExhibit()) {
					WebExhibitImpl exhibit = dbManager.obtainById(WebExhibitImpl.class, exhibitId);
					if (exhibit != null) {
						AssetImpl asset = dbManager.obtainAsset(exhibit.getAsset());
						if (ExhibitionView.accepts(asset))
							continue;
						createExhibitFromAsset(show, newWall, pos, asset, exhibit.getCaption(),
								exhibit.getDescription());
					}
					pos += incr;
				}
				dbManager.store(newWall);
				show.addWall(newWall);
			}
		} else if (obj instanceof SmartCollectionImpl) {
			SmartCollection sm = (SmartCollection) obj;
			List<Asset> assets = dbManager.createCollectionProcessor(sm).select(true);
			WallImpl newWall = createNewWall(1);
			newWall.setLocation(sm.getName());
			int incr = (newWall.getWidth() - 2 * pos) / (assets.size() + 1);
			for (Asset asset : assets) {
				if (!ExhibitionView.accepts(asset))
					continue;
				createExhibitFromAsset(show, newWall, pos, asset, UiUtilities.createSlideTitle(asset),
						Messages.ExhibitionEditDialog_inkjet_print);
				pos += incr;
			}
		}
	}

	private void createExhibitFromAsset(ExhibitionImpl show, WallImpl newWall, int pos, Asset asset, String caption,
			String description) {
		Date dateCreated = asset.getDateTimeOriginal();
		if (dateCreated == null)
			dateCreated = asset.getDateTime();
		int h = (int) (asset.getHeight() * DPI300);
		int w = (int) (asset.getWidth() * DPI300);
		int y = show.getDefaultViewingHeight() + h / 2;
		ExhibitImpl newExhibit = new ExhibitImpl(caption, description, Core.toStringList(asset.getArtist(), " "), //$NON-NLS-1$
				(dateCreated == null) ? "" //$NON-NLS-1$
						: EDF.format(dateCreated),
				pos, y, w, h, null, null, null, null, false, null, null, null, null, asset.getStringId());
		pos += 2 * w;
		dbManager.store(newExhibit);
		newWall.addExhibit(newExhibit.getStringId());
	}

	private static WallImpl createNewWall(int seqNo) {
		WallImpl wall = new WallImpl();
		wall.setLocation(NLS.bind(Messages.ExhibitionEditDialog_wall_n, seqNo));
		wall.setWidth(5000);
		wall.setHeight(2500);
		wall.setColor(new Rgb_typeImpl(255, 255, 250));
		return wall;
	}

	public ExhibitionImpl getResult() {
		return result;
	}

	@Override
	public boolean close() {
		if (selectedFont != null)
			selectedFont.dispose();
		return super.close();
	}

	public void selectOutputPage(String msg) {
		setErrorMessage(msg);
		tabFolder.setSelection(current == null ? 4 : 6);
	}

}
