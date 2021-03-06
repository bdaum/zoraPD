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

package com.bdaum.zoom.ui.internal.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.group.GroupImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitImpl;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitionImpl;
import com.bdaum.zoom.cat.model.group.exhibition.Wall;
import com.bdaum.zoom.cat.model.group.slideShow.SlideImpl;
import com.bdaum.zoom.cat.model.group.slideShow.SlideShowImpl;
import com.bdaum.zoom.cat.model.group.webGallery.Storyboard;
import com.bdaum.zoom.cat.model.group.webGallery.WebExhibitImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebGalleryImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.CaptionProcessor;
import com.bdaum.zoom.ui.internal.CaptionProcessor.CaptionConfiguration;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiConstants;
import com.bdaum.zoom.ui.internal.views.SlideshowView;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.CheckedText;
import com.bdaum.zoom.ui.internal.widgets.PrivacyGroup;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.bdaum.zoom.ui.widgets.NumericControl;

public class SlideshowEditDialog extends ZTitleAreaDialog implements ISelectionChangedListener, Listener {

	public static final String SETTINGSID = "com.bdaum.zoom.slideshowProperties"; //$NON-NLS-1$
	public static final String DELAY = "delay"; //$NON-NLS-1$
	public static final String FADING = "fading"; //$NON-NLS-1$
	public static final String ZOOM = "zoom"; //$NON-NLS-1$
	public static final String TITLEDISPLAY = "titleDisplay"; //$NON-NLS-1$
	public static final String TITLECONTENT = "titleContent"; //$NON-NLS-1$
	public static final String TITLESCHEME = "titleScheme"; //$NON-NLS-1$
	public static final String TITLETRANSPARENCY = "titleAlpha"; //$NON-NLS-1$
	public static final String COLORSCHEME = "colorScheme"; //$NON-NLS-1$
	public static final String FROMPREVIEW = "fromPreview"; //$NON-NLS-1$
	public static final String VOICENOTES = "voicenotes"; //$NON-NLS-1$
	private static final String SKIP_DUBLETTES = "skipDublettes"; //$NON-NLS-1$

	public static final String[] EFFECTS = new String[] { Messages.SlideshowEditDialog_expand,
			Messages.SlideshowEditDialog_Fade, Messages.SlideshowEditDialog_move_left,
			Messages.SlideshowEditDialog_move_right, Messages.SlideshowEditDialog_move_up,
			Messages.SlideshowEditDialog_move_down, Messages.SlideshowEditDialog_move_topLeft,
			Messages.SlideshowEditDialog_move_topRight, Messages.SlideshowEditDialog_move_bottomLeft,
			Messages.SlideshowEditDialog_move_bottomRight, Messages.SlideshowEditDialog_blend_left,
			Messages.SlideshowEditDialog_blend_right, Messages.SlideshowEditDialog_blend_up,
			Messages.SlideshowEditDialog_blend_down, Messages.SlideshowEditDialog_blend_topLeft,
			Messages.SlideshowEditDialog_blend_topRight, Messages.SlideshowEditDialog_blend_bottomLeft,
			Messages.SlideshowEditDialog_blend_bottomRight, Messages.SlideshowEditDialog_random };

	public static final String EFFECT = "effect"; //$NON-NLS-1$

	private static final String[] TITLECONTENTITEMS = new String[] { Messages.SlideshowEditDialog_caption_only,
			Messages.SlideshowEditDialog_seqno_only, Messages.SlideshowEditDialog_caption_seqno,
			Messages.SlideshowEditDialog_captions_unequal };

	private static final String[] TITLESCHEMEITEMS = new String[] { Messages.SlideshowEditDialog_black_on_white,
			Messages.SlideshowEditDialog_white_on_black };
	private static final String[] COLORSCHEMEITEMS = new String[] { Messages.SlideshowEditDialog_dark_gray,
			Messages.SlideshowEditDialog_gray, Messages.SlideshowEditDialog_black, Messages.SlideshowEditDialog_white };

	private SlideShowImpl current, result;
	private String title;
	private boolean adhoc, canUndo, fromPreview, voiceNotes;
	private Text nameField;
	private CheckedText descriptionField;
	private NumericControl durationField, fadingField, titleDisplayField, zoomField, titleTransparencyField;
	private int fading = 1000, duration = 7000, titleDisplay = 1500; //msec
	private int effect = Constants.SLIDE_TRANSITION_RANDOM;
	private Combo titleContentField, titleSchemeField, effectField, colorSchemeField;
	private CheckboxButton voiceButton, fromPreviewButton, skipDubletteswButton;
	private int titleScheme = UiConstants.BLACKONWHITE;
	private int titleTransparency = 0;
	private int colorScheme = UiConstants.BG_DARK_GRAY;
	private int titleContent = Constants.SLIDE_TITLEONLY;
	private GroupImpl group;
	private ImportGalleryGroup importGroup;
	private IDialogSettings settings;
	private boolean skipDublettes = true;
	private int privacy, zoom;
	private PrivacyGroup privacyGroup;

	public SlideshowEditDialog(Shell parentShell, GroupImpl group, SlideShowImpl current, String title, boolean adhoc,
			boolean canUndo) {
		super(parentShell, HelpContextIds.SLIDESHOW_DIALOG);
		this.group = group;
		this.current = current;
		this.title = title;
		this.adhoc = adhoc;
		this.canUndo = canUndo;
	}

	@Override
	public void create() {
		super.create();
		setTitle(title);
		setMessage(adhoc ? Messages.SlideshowEditDialog_specify_properties
				: Messages.SlideshowEditDialog_specify_properties_nonadhoc);
		updateButtons();
		updateFields();
		if (nameField != null)
			nameField.setFocus();
	}

	private void updateFields() {
		boolean enabled = titleDisplayField.getSelection() > 0;
		titleContentField.setEnabled(enabled);
		titleSchemeField.setEnabled(enabled);
	}

	@SuppressWarnings("unused")
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		final Composite comp = new Composite(area, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout(2, false));
		if (!adhoc) {
			if (current == null) {
				importGroup = new ImportGalleryGroup(comp, new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1),
						Messages.SlideshowEditDialog_slideshow);
				importGroup.addChangeListener(this);
			}
			new Label(comp, SWT.NONE).setText(Messages.SlideshowEditDialog_name);
			nameField = new Text(comp, SWT.BORDER);
			nameField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			nameField.addListener(SWT.Modify, this);
			new Label(comp, SWT.NONE).setText(Messages.SlideshowEditDialog_description);
			final GridData gd_descriptionField = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd_descriptionField.widthHint = 250;
			gd_descriptionField.heightHint = 70;
			descriptionField = new CheckedText(comp, SWT.WRAP | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER);
			descriptionField.setLayoutData(gd_descriptionField);
		}
		final CGroup contentGroup = new CGroup(comp, SWT.NONE);
		contentGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
		contentGroup.setLayout(new GridLayout());
		contentGroup.setText(Messages.SlideshowEditDialog_contents);
		fromPreviewButton = WidgetFactory.createCheckButton(contentGroup,
				Messages.SlideshowEditDialog_use_preview_where_possible, null);
		if (adhoc)
			skipDubletteswButton = WidgetFactory.createCheckButton(contentGroup,
					Messages.SlideshowEditDialog_skip_duplkcates, null);
		voiceButton = WidgetFactory.createCheckButton(contentGroup, Messages.SlideshowEditDialog_voicenotes, null);
		if (adhoc)
			privacyGroup = new PrivacyGroup(contentGroup, Messages.SlideshowEditDialog_privacy, null);

		final CGroup timingGroup = new CGroup(comp, SWT.NONE);
		timingGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
		timingGroup.setLayout(new GridLayout(4, false));
		timingGroup.setText(Messages.SlideshowEditDialog_default_timing);
		durationField = createNumericField(timingGroup, 36000, Messages.SlideshowEditDialog_duration, 3);
		fadingField = createNumericField(timingGroup, 300, Messages.SlideshowEditDialog_fading, 1);
		final Label effectLabel = new Label(timingGroup, SWT.NONE);
		GridData data = new GridData(SWT.END, SWT.CENTER, false, false);
		data.horizontalIndent = 15;
		effectLabel.setLayoutData(data);
		effectLabel.setText(Messages.SlideshowEditDialog_transition_effect);
		effectField = new Combo(timingGroup, SWT.DROP_DOWN);
		effectField.setLayoutData(new GridData(150, SWT.DEFAULT));
		effectField.setItems(EFFECTS);
		effectField.setVisibleItemCount(EFFECTS.length / 2);
		new Label(timingGroup, SWT.NONE).setText(Messages.SlideshowEditDialog_zoom_in);
		zoomField = new NumericControl(timingGroup, SWT.NONE);
		zoomField.setMaximum(100);
		zoomField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 3, 1));
		titleDisplayField = createNumericField(timingGroup, 36000, Messages.SlideshowEditDialog_title_display, 1);
		final Label titleContentLabel = new Label(timingGroup, SWT.NONE);
		data = new GridData(SWT.END, SWT.CENTER, false, false);
		data.horizontalIndent = 15;
		titleContentLabel.setLayoutData(data);
		titleContentLabel.setText(Messages.SlideshowEditDialog_content);
		titleContentField = new Combo(timingGroup, SWT.DROP_DOWN);
		titleContentField.setLayoutData(new GridData(150, SWT.DEFAULT));
		titleContentField.setItems(TITLECONTENTITEMS);
		titleContentField.setVisibleItemCount(TITLECONTENTITEMS.length);

		final CGroup designGroup = new CGroup(comp, SWT.NONE);
		designGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
		designGroup.setLayout(new GridLayout(2, false));
		designGroup.setText(Messages.SlideshowEditDialog_design);
		new Label(designGroup, SWT.NONE).setText(Messages.SlideshowEditDialog_bg_color);
		colorSchemeField = new Combo(designGroup, SWT.DROP_DOWN);
		colorSchemeField.setLayoutData(new GridData(150, SWT.DEFAULT));
		colorSchemeField.setItems(COLORSCHEMEITEMS);
		colorSchemeField.setVisibleItemCount(COLORSCHEMEITEMS.length);
		new Label(designGroup, SWT.NONE).setText(Messages.SlideshowEditDialog_color_scheme);
		titleSchemeField = new Combo(designGroup, SWT.DROP_DOWN);
		titleSchemeField.setLayoutData(new GridData(150, SWT.DEFAULT));
		titleSchemeField.setItems(TITLESCHEMEITEMS);
		titleSchemeField.setVisibleItemCount(TITLESCHEMEITEMS.length);
		new Label(designGroup, SWT.NONE).setText(Messages.SlideshowEditDialog_title_transparency);
		titleTransparencyField = new NumericControl(designGroup, SWT.BORDER);
		titleTransparencyField.setMaximum(100);
		titleTransparencyField.setIncrement(5);
		titleTransparencyField.setPageIncrement(50);
		new Label(timingGroup, SWT.NONE);
		fillValues(current, false);
		return area;
	}

	public void selectionChanged(SelectionChangedEvent event) {
		IdentifiableObject fromItem = importGroup.getFromItem();
		if (fromItem instanceof SlideShowImpl)
			fillValues((SlideShowImpl) fromItem, true);
		descriptionField.setText(importGroup.getDescription());
	}

	private NumericControl createNumericField(final Composite comp, int max, String text, int columns) {
		new Label(comp, SWT.NONE).setText(text);
		NumericControl field = new NumericControl(comp, SWT.BORDER);
		field.setMinimum(0);
		field.setDigits(1);
		field.setMaximum(max);
		field.setIncrement(5);
		field.setPageIncrement(50);
		field.addListener(SWT.Selection, this);
		field.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, columns, 1));
		return field;
	}

	private void updateButtons() {
		Button okButton = getButton(IDialogConstants.OK_ID);
		if (okButton != null) {
			boolean enabled = validate() && !readonly;
			getShell().setModified(enabled);
			okButton.setEnabled(enabled);
		}
	}

	private boolean validate() {
		if (!adhoc) {
			String name = nameField.getText();
			if (name.isEmpty()) {
				setErrorMessage(Messages.SlideshowEditDialog_specigy_name);
				return false;
			}
			List<SlideShowImpl> set = dbManager.obtainObjects(SlideShowImpl.class, "name", name, QueryField.EQUALS); //$NON-NLS-1$
			for (SlideShowImpl obj : set)
				if (obj != current) {
					setErrorMessage(Messages.SlideshowEditDialog_name_already_exists);
					return false;
				}
		}
		setErrorMessage(null);
		return true;
	}

	private void fillValues(SlideShowImpl show, boolean template) {
		settings = getDialogSettings(UiActivator.getDefault(), SETTINGSID);
		try {
			duration = settings.getInt(DELAY);
		} catch (NumberFormatException e) {
			// ignore
		}
		try {
			effect = settings.getInt(EFFECT);
		} catch (NumberFormatException e) {
			effect = 0;
		}
		try {
			zoom = settings.getInt(ZOOM);
		} catch (NumberFormatException e) {
			zoom = 0;
		}
		try {
			fading = settings.getInt(FADING);
		} catch (NumberFormatException e) {
			// ignore
		}
		try {
			titleDisplay = settings.getInt(TITLEDISPLAY);
		} catch (NumberFormatException e) {
			// ignore
		}
		try {
			titleContent = settings.getInt(TITLECONTENT);
		} catch (NumberFormatException e) {
			// ignore
		}
		try {
			titleScheme = settings.getInt(TITLESCHEME);
		} catch (NumberFormatException e) {
			// ignore
		}
		try {
			titleTransparency = settings.getInt(TITLETRANSPARENCY);
		} catch (NumberFormatException e) {
			// ignore
		}
		try {
			colorScheme = settings.getInt(COLORSCHEME);
		} catch (NumberFormatException e) {
			// ignore
		}
		try {
			fromPreview = settings.getBoolean(FROMPREVIEW);
		} catch (Exception e) {
			// ignore
		}
		try {
			voiceNotes = settings.getBoolean(VOICENOTES);
		} catch (Exception e) {
			// ignore
		}
		if (show != null && !adhoc) {
			if (!template) {
				if (nameField != null)
					nameField.setText(show.getName());
				if (descriptionField != null)
					descriptionField.setText(show.getDescription());
			}
			duration = show.getDuration();
			fading = show.getFading();
			zoom = show.getZoom();
			effect = show.getEffect();
			titleDisplay = show.getTitleDisplay();
			titleScheme = show.getTitleScheme();
			titleTransparency = show.getTitleTransparency();
			colorScheme = show.getColorScheme();
			titleContent = show.getTitleContent();
			fromPreview = show.getFromPreview();
			voiceNotes = show.getVoiceNotes();
		}
		durationField.setSelection(duration / 100);
		effectField.select(effect - Constants.SLIDE_TRANSITION_START);
		zoomField.setSelection(zoom);
		fadingField.setSelection(fading / 100);
		titleDisplayField.setSelection(titleDisplay / 100);
		titleContentField.select(titleContent);
		titleSchemeField.select(titleScheme);
		titleTransparencyField.setSelection(titleTransparency);
		colorSchemeField.select(colorScheme);
		fromPreviewButton.setSelection(fromPreview);
		voiceButton.setSelection(voiceNotes);
		if (skipDubletteswButton != null) {
			try {
				skipDublettes = settings.getBoolean(SKIP_DUBLETTES);
			} catch (NumberFormatException e) {
				skipDublettes = true;
			}
			skipDubletteswButton.setSelection(skipDublettes);
		}
		if (adhoc)
			privacyGroup.setSelection(QueryField.SAFETY_RESTRICTED);
	}

	@Override
	protected void okPressed() {
		apply();
		super.okPressed();
	}

	private void apply() {
		BusyIndicator.showWhile(getShell().getDisplay(), () -> {
			// Must create a new instance if named or cannot undo
			if ((adhoc || canUndo) && current != null)
				result = current;
			else {
				result = new SlideShowImpl();
				if (current != null) {
					result.setEntry(current.getEntry());
					result.setGroup_slideshow_parent(current.getGroup_slideshow_parent());
				}
			}
			result.setAdhoc(adhoc);
			if (nameField != null)
				result.setName(nameField.getText());
			else if (current != null && current.getName() != null && !current.getName().isEmpty())
				result.setName(NLS.bind(Messages.SlideshowEditDialog_adhoc_slideshow_n, current.getName()));
			else
				result.setName(Messages.SlideshowEditDialog_adhoc_slideshow);
			result.setDescription(descriptionField != null ? descriptionField.getText() : ""); //$NON-NLS-1$
			if (adhoc)
				privacy = privacyGroup.getSelection();
			duration = durationField.getSelection() * 100;
			result.setDuration(duration);
			settings.put(DELAY, duration);
			effect = Math.max(0, effectField.getSelectionIndex()) + Constants.SLIDE_TRANSITION_START;
			zoom = zoomField.getSelection();
			result.setZoom(zoom);
			settings.put(EFFECT, effect);
			result.setEffect(effect);
			fading = fadingField.getSelection() * 100;
			result.setFading(fading);
			settings.put(ZOOM, zoom);
			settings.put(FADING, fading);
			titleDisplay = titleDisplayField.getSelection() * 100;
			result.setTitleDisplay(titleDisplay);
			settings.put(TITLEDISPLAY, titleDisplay);
			titleContent = titleContentField.getSelectionIndex();
			if (titleContent < 0)
				titleContent = Constants.SLIDE_TITLEONLY;
			result.setTitleContent(titleContent);
			settings.put(TITLECONTENT, titleContent);
			titleScheme = titleSchemeField.getSelectionIndex();
			if (titleScheme < 0)
				titleScheme = UiConstants.BLACKONWHITE;
			result.setTitleScheme(titleScheme);
			settings.put(TITLESCHEME, titleScheme);
			titleTransparency = titleTransparencyField.getSelection();
			result.setTitleTransparency(titleTransparency);
			settings.put(TITLETRANSPARENCY, titleTransparency);
			colorScheme = colorSchemeField.getSelectionIndex();
			if (colorScheme < 0)
				colorScheme = UiConstants.BG_DARK_GRAY;
			result.setColorScheme(colorScheme);
			settings.put(COLORSCHEME, colorScheme);
			fromPreview = fromPreviewButton.getSelection();
			voiceNotes = voiceButton.getSelection();
			settings.put(FROMPREVIEW, fromPreview);
			settings.put(VOICENOTES, voiceNotes);
			result.setFromPreview(fromPreview);
			result.setVoiceNotes(voiceNotes);
			if (skipDubletteswButton != null) {
				skipDublettes = skipDubletteswButton.getSelection();
				settings.put(SKIP_DUBLETTES, skipDublettes);
				result.setSkipDublettes(skipDublettes);
			}
			if (!adhoc && !canUndo) {
				dbManager.safeTransaction(new Runnable() {
					public void run() {
						if (importGroup != null)
							importIntoGallery(result, importGroup.getFromItem());
						if (group == null) {
							String groupId = (current != null) ? current.getGroup_slideshow_parent()
									: Constants.GROUP_ID_SLIDESHOW;
							if (groupId == null)
								groupId = Constants.GROUP_ID_SLIDESHOW;
							group = dbManager.obtainById(GroupImpl.class, groupId);
						}
						if (group == null) {
							group = new GroupImpl(Messages.SlideshowEditDialog_slideshows, false,
									Constants.INHERIT_LABEL, null, 0, 1, null);
							group.setStringId(Constants.GROUP_ID_SLIDESHOW);
						}
						if (current != null)
							group.removeSlideshow(current.getStringId());
						group.addSlideshow(result.getStringId());
						result.setGroup_slideshow_parent(group.getStringId());
						if (current != null)
							dbManager.delete(current);
						dbManager.store(group);
						dbManager.store(result);
					}
				});
			}
		});
	}

	public int getPrivacy() {
		return privacy;
	}

	public void setPrivacy(int privacy) {
		this.privacy = privacy;
	}

	private void importIntoGallery(SlideShowImpl show, IdentifiableObject obj) {
		if (obj instanceof SlideShowImpl)
			for (String slideId : ((SlideShowImpl) obj).getEntry()) {
				SlideImpl slide = dbManager.obtainById(SlideImpl.class, slideId);
				if (slide != null) {
					SlideImpl newSlide = SlideshowView.cloneSlide(slide);
					dbManager.store(newSlide);
					show.addEntry(newSlide.getStringId());
				}
			}
		else if (obj instanceof ExhibitionImpl) {
			int seqNo = 1;
			for (Wall wall : ((ExhibitionImpl) obj).getWall()) {
				SlideImpl newSlide = new SlideImpl(wall.getLocation(), seqNo++, "", Constants.SLIDE_NO_THUMBNAILS, //$NON-NLS-1$
						show.getFading(), show.getFading(), show.getDuration(), show.getFading(), show.getEffect(), 0,
						0, 0, true, QueryField.SAFETY_SAFE, null);
				dbManager.store(newSlide);
				show.addEntry(newSlide.getStringId());
				for (String exhibitId : wall.getExhibit()) {
					ExhibitImpl exhibit = dbManager.obtainById(ExhibitImpl.class, exhibitId);
					if (exhibit != null) {
						String assetId = exhibit.getAsset();
						if (!SlideshowView.accepts(dbManager.obtainAsset(assetId)))
							continue;
						newSlide = new SlideImpl(exhibit.getTitle(), seqNo++, exhibit.getDescription(),
								Constants.SLIDE_NO_THUMBNAILS, show.getFading(), show.getFading(), show.getDuration(),
								show.getFading(), show.getEffect(), show.getZoom(), 0, 0, false, QueryField.SAFETY_SAFE,
								assetId);
						dbManager.store(newSlide);
						show.addEntry(newSlide.getStringId());
					}
				}
			}
		} else if (obj instanceof WebGalleryImpl) {
			int seqNo = 1;
			for (Storyboard storyboard : ((WebGalleryImpl) obj).getStoryboard()) {
				SlideImpl newSlide = new SlideImpl(storyboard.getTitle(), seqNo++, storyboard.getDescription(),
						Constants.SLIDE_NO_THUMBNAILS, show.getFading(), show.getFading(), show.getDuration(),
						show.getFading(), show.getEffect(), 0, 0, 0, true, QueryField.SAFETY_SAFE, null);
				dbManager.store(newSlide);
				show.addEntry(newSlide.getStringId());
				for (String exhibitId : storyboard.getExhibit()) {
					WebExhibitImpl exhibit = dbManager.obtainById(WebExhibitImpl.class, exhibitId);
					if (exhibit != null) {
						String assetId = exhibit.getAsset();
						if (!SlideshowView.accepts(dbManager.obtainAsset(assetId)))
							continue;
						newSlide = new SlideImpl(exhibit.getCaption(), seqNo++, exhibit.getDescription(),
								Constants.SLIDE_NO_THUMBNAILS, show.getFading(), show.getFading(), show.getDuration(),
								show.getFading(), show.getEffect(), show.getZoom(), 0, 0, false, QueryField.SAFETY_SAFE,
								assetId);
						dbManager.store(newSlide);
						show.addEntry(newSlide.getStringId());
					}
				}
			}
		} else if (obj instanceof SmartCollectionImpl) {
			int seqNo = 1;
			CaptionProcessor captionProcessor = new CaptionProcessor(Constants.TH_ALL);
			CaptionConfiguration captionConfig = captionProcessor.computeCaptionConfiguration((SmartCollection) obj);
			for (Asset asset : dbManager.createCollectionProcessor((SmartCollection) obj).select(true))
				if (SlideshowView.accepts(asset)) {
					SlideImpl newSlide = new SlideImpl(
							captionProcessor.computeImageCaption(asset, null, null, null,
									captionConfig.getLabelTemplate(), true),
							seqNo++, null, Constants.SLIDE_NO_THUMBNAILS, show.getFading(), show.getFading(),
							show.getDuration(), show.getFading(), show.getEffect(), show.getZoom(), 0, 0, false,
							QueryField.SAFETY_SAFE, asset.getStringId());
					dbManager.store(newSlide);
					show.addEntry(newSlide.getStringId());
				}
		}
	}

	public SlideShowImpl getResult() {
		return result;
	}

	@Override
	public void handleEvent(Event event) {
		updateButtons();
		updateFields();
	}
}
