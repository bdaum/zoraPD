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
 * (c) 2020 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.views;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IMemento;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.ui.internal.TemplateProcessor;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiConstants;
import com.bdaum.zoom.ui.internal.dialogs.ConfigureCaptionDialog;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;

public class CaptionManager implements UiConstants, Listener {

	private static final String SHOW = "show"; //$NON-NLS-1$
	private static final String FONTSIZE = "fontsize"; //$NON-NLS-1$
	private static final String OVERLAY = "overlay"; //$NON-NLS-1$
	private int show = Constants.TITLE_LABEL;
	private int fontsize = 8;
	private int alignment = SWT.LEFT;
	private boolean overlay;
	private String template;
	private Action configureAction;
	private Label caption;
	private Control target;
	private final TemplateProcessor captionProcessor = new TemplateProcessor(Constants.TH_ALL);
	private int showLabelDflt = Constants.TITLE_LABEL;
	private String labelTemplateDflt = DEFAULTTEMPLATE;
	private int labelFontsizeDflt = 8;
	private int labelAlignmentDflt = SWT.LEFT;
	private Asset currentItem;
	private Timer timer = new Timer();
	private TimerTask task;

	public void init(IMemento memento) {
		if (memento != null) {
			template = memento.getString(TEMPLATE);
			Integer integer = memento.getInteger(SHOW);
			show = integer != null ? integer
					: template == null ? Constants.INHERIT_LABEL
							: template.isEmpty() ? Constants.NO_LABEL : Constants.CUSTOM_LABEL;
			integer = memento.getInteger(FONTSIZE);
			if (integer != null)
				fontsize = integer;
			integer = memento.getInteger(ALIGNMENT);
			if (integer != null)
				alignment = integer;
			Boolean b = memento.getBoolean(OVERLAY);
			overlay = b != null && b;
		}
		if (template == null)
			template = DEFAULTTEMPLATE;
		setPreferences();
	}

	private void setPreferences() {
		applyPreferences().addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				String property = event.getProperty();
				if (PreferenceConstants.SHOWLABEL.equals(property)
						|| PreferenceConstants.THUMBNAILTEMPLATE.equals(property)
						|| PreferenceConstants.LABELALIGNMENT.equals(property)
						|| PreferenceConstants.LABELFONTSIZE.equals(property)) {
					applyPreferences();
					updateCaption(currentItem);
				}
			}
		});
	}

	private IPreferenceStore applyPreferences() {
		IPreferenceStore preferenceStore = UiActivator.getDefault().getPreferenceStore();
		showLabelDflt = preferenceStore.getInt(PreferenceConstants.SHOWLABEL);
		labelTemplateDflt = preferenceStore.getString(PreferenceConstants.THUMBNAILTEMPLATE);
		labelFontsizeDflt = preferenceStore.getInt(PreferenceConstants.LABELFONTSIZE);
		labelAlignmentDflt = preferenceStore.getInt(PreferenceConstants.LABELALIGNMENT);
		return preferenceStore;
	}

	public void saveState(IMemento memento) {
		memento.putString(TEMPLATE, template);
		memento.putInteger(ALIGNMENT, alignment);
		memento.putInteger(SHOW, show);
		memento.putInteger(FONTSIZE, fontsize);
		memento.putBoolean(OVERLAY, overlay);
	}

	public void makeActions() {
		configureAction = new Action(Messages.getString("CaptionManager.configure")) { //$NON-NLS-1$
			@Override
			public void run() {
				if (!target.isDisposed()) {
					ConfigureCaptionDialog dialog = new ConfigureCaptionDialog(target.getShell(), show, template,
							alignment, fontsize, overlay, currentItem);
					if (dialog.open() == ConfigureCaptionDialog.OK) {
						show = dialog.getShow();
						template = dialog.getTemplate();
						alignment = dialog.getAlignment();
						overlay = dialog.getOverlay();
						fontsize = dialog.getFontsize();
						updateCaption(currentItem);
					}
				}
			}
		};
		configureAction.setToolTipText(Messages.getString("CaptionManager.configure_tooltip")); //$NON-NLS-1$
	}

	public void handleOverlay(GC gc, int x, int y, int w, int h, int alphaBg, int alphaFg) {
		if (overlay && show != Constants.NO_LABEL && template != null || template.isEmpty()) {
			String text = currentItem == null ? "" //$NON-NLS-1$
					: captionProcessor.processTemplate(show == Constants.CUSTOM_LABEL ? template : null, currentItem);
			if (!text.isEmpty()) {
				gc.setFont(show == Constants.CUSTOM_LABEL && fontsize != 0 ? getCaptionFont(fontsize)
						: JFaceResources.getDefaultFont());
				Point textExtent = gc.textExtent(text);
				gc.setAlpha(alphaBg);
				gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_BLACK));
				gc.fillRectangle(x, y + h - textExtent.y - 4, w, textExtent.y + 4);
				gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_WHITE));
				int tw = textExtent.x;
				int offtx = alignment == SWT.LEFT ? 4 : alignment == SWT.CENTER ? (w - tw) / 2 : w - tw - 4;
				gc.setAlpha(alphaFg);
				gc.drawText(text, x + offtx, y + h - textExtent.y - 2, true);
			}
		}
	}

	public void updateCaption(Asset currentItem) {
		this.currentItem = currentItem;
		if (overlay || show == Constants.NO_LABEL || template == null || template.isEmpty()) {
			if (caption != null) {
				caption.removeListener(SWT.MouseUp, this);
				caption.dispose();
				caption = null;
			}
		} else if (!overlay) {
			if (caption == null) {
				caption = new Label(target.getParent(), SWT.NONE);
				caption.setLayoutData(new GridData(SWT.FILL, SWT.END, true, false));
				caption.addListener(SWT.MouseUp, this);
			}
			caption.setText(currentItem == null ? "" //$NON-NLS-1$
					: captionProcessor.processTemplate(show == Constants.CUSTOM_LABEL ? template : null, currentItem));
			caption.setAlignment(alignment);
			caption.setFont(show == Constants.CUSTOM_LABEL && fontsize != 0 ? getCaptionFont(fontsize)
					: JFaceResources.getDefaultFont());
		}
		target.getParent().layout(true, true);
		target.redraw();
	}

	@Override
	public void handleEvent(Event e) {
		if (e.type == SWT.MouseUp && (e.widget == caption || e.widget == target)) {
			if (task != null) {
				task.cancel();
				task = null;
			}
			if (e.count == 1) {
				task = new TimerTask() {
					@Override
					public void run() {
						if (!target.isDisposed())
							target.getDisplay().asyncExec(() -> configureAction.run());
					}
				};
				timer.schedule(task, 300);
				e.doit = false;
			}
		}
	}

	public void fillMenu(IMenuManager manager) {
		manager.add(configureAction);
	}

	public void refresh() {
		if (show == Constants.INHERIT_LABEL) {
			show = showLabelDflt;
			template = labelTemplateDflt;
			fontsize = labelFontsizeDflt;
			alignment = labelAlignmentDflt;
		}
	}

	private static Font getCaptionFont(int labelFontsize) {
		FontRegistry registry = JFaceResources.getFontRegistry();
		String fontName = "zCaptionFont" + labelFontsize; //$NON-NLS-1$
		if (registry.hasValueFor(fontName))
			return registry.get(fontName);
		Font defaultFont = registry.defaultFont();
		FontData[] fd = defaultFont.getFontData();
		if (fd[0].getHeight() == labelFontsize)
			return defaultFont;
		fd[0].setHeight(labelFontsize);
		registry.put(fontName, fd);
		return registry.get(fontName);
	}

	public void setTarget(Control target) {
		this.target = target;
	}

	public int getOverlayHeight(GC gc) {
		if (overlay && show != Constants.NO_LABEL) {
			gc.setFont(show == Constants.CUSTOM_LABEL && fontsize != 0 ? getCaptionFont(fontsize)
					: JFaceResources.getDefaultFont());
			return gc.textExtent("Bg").y + 4; //$NON-NLS-1$
		}
		return 0;
	}
}
