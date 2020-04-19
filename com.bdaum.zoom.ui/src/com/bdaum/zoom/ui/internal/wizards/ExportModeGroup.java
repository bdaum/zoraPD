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

package com.bdaum.zoom.ui.internal.wizards;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;

import com.bdaum.zoom.batch.internal.BatchActivator;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.image.ZImage;
import com.bdaum.zoom.image.recipe.UnsharpMask;
import com.bdaum.zoom.program.IRawConverter;
import com.bdaum.zoom.ui.internal.widgets.QualityGroup;
import com.bdaum.zoom.ui.internal.widgets.RadioButtonGroup;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.bdaum.zoom.ui.widgets.NumericControl;

@SuppressWarnings("restriction")
public class ExportModeGroup implements Listener {
	public static final int ORIGINALS = 1;
	public static final int JPEG = 2;
	public static final int WEBP = 4;
	public static final int RAWCROP = 8;
	public static final int ORIGSIZE = 16;
	public static final int SCALE = 32;
	public static final int FIXED = 64;
	public static final int ALLFORMATS = ORIGINALS | JPEG | WEBP;
	public static final int SIZING = ORIGSIZE | SCALE | FIXED;
	private static final String FIXEDSIZE = "fixedSize"; //$NON-NLS-1$
	private static final String MODE = "mode"; //$NON-NLS-1$
	private static final String SIZE = "size"; //$NON-NLS-1$
	private static final String SCALING = "scaling"; //$NON-NLS-1$
	private static final String RAWCROPPING = "rawCropping"; //$NON-NLS-1$

	private Label scaleLabel;
	private Scale scale;
	private NumericControl dimField;
	private ListenerList<Listener> listeners = new ListenerList<>();
	private CGroup modeGroup;
	private QualityGroup qualityGroup;
	private RadioButtonGroup cropButtonGroup;
	private RadioButtonGroup modeButtonGroup;
	private StackLayout stackLayout;
	private Composite stackComp;
	private RadioButtonGroup sizeButtonGroup;
	private int orig = -1;
	private int jpeg = -1;
	private int webp = -1;
	private int s_orig = -1;
	private int s_scale = -1;
	private int s_fixed = -1;
	private Composite sizeComp;
	private Composite scaleComp;
	private Composite dimComp;

	public ExportModeGroup(Composite parent, int style) {
		this(parent, style, Messages.ExportFolderPage_image);
	}

	public ExportModeGroup(Composite parent, int style, String label) {
		GridLayout layout = (GridLayout) parent.getLayout();
		modeGroup = CGroup.create(parent, layout.numColumns, label);
		modeGroup.setLayout(new GridLayout(2, false));
		modeButtonGroup = new RadioButtonGroup(modeGroup, null, SWT.NONE);
		modeButtonGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		modeButtonGroup.addListener(SWT.Selection, this);
		int i = 0;
		if ((style & ORIGINALS) != 0) {
			modeButtonGroup.addButton(Messages.SendEmailPage_Send_originals);
			orig = i++;
		}
		if ((style & JPEG) != 0) {
			modeButtonGroup.addButton(Messages.SendEmailPage_Send_preview);
			jpeg = i++;
		}
		if ((style & WEBP) != 0) {
			modeButtonGroup.addButton(Messages.ExportModeGroup_export_webp);
			webp = i;
		}
		modeButtonGroup.setSelection(0);
		if ((style & SIZING) != 0) {
			sizeComp = new Composite(modeGroup, SWT.NONE);
			GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
			data.horizontalIndent = 20;
			sizeComp.setLayoutData(data);
			sizeComp.setLayout(new GridLayout(2, false));
			sizeButtonGroup = new RadioButtonGroup(sizeComp, null, SWT.NONE);
			i = 0;
			if ((style & ORIGSIZE) != 0) {
				sizeButtonGroup.addButton(Messages.ExportModeGroup_orig_size);
				s_orig = i++;
			}
			if ((style & SCALE) != 0) {
				sizeButtonGroup.addButton(Messages.ExportModeGroup_scaled);
				s_scale = i++;
			}
			if ((style & FIXED) != 0) {
				sizeButtonGroup.addButton(Messages.ExportModeGroup_fixed);
				s_fixed = i++;
			}
			sizeButtonGroup.setSelection(0);
			sizeButtonGroup.addListener(SWT.Selection, this);
			stackComp = new Composite(modeGroup, SWT.NONE);
			stackComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
			stackLayout = new StackLayout();
			stackComp.setLayout(stackLayout);
			Listener listener = null;
			if ((style & (SCALE | FIXED)) != 0) {
				listener = new Listener() {
					@Override
					public void handleEvent(Event event) {
						fireEvent(event);
						updateScale();
					}
				};
			}
			if ((style & SCALE) != 0) {
				scaleComp = new Composite(stackComp, SWT.NONE);
				scaleComp.setLayout(new GridLayout(2, false));
				scaleLabel = new Label(scaleComp, SWT.NONE);
				scaleLabel.setText(Messages.SendEmailPage_Scale);
				scale = new Scale(scaleComp, SWT.NONE);
				scale.setLayoutData(new GridData());
				scale.setMinimum(5);
				scale.addListener(SWT.Selection, listener);
			}
			if ((style & FIXED) != 0) {
				dimComp = new Composite(stackComp, SWT.NONE);
				dimComp.setLayout(new GridLayout(2, false));
				Label dimLabel = new Label(dimComp, SWT.NONE);
				dimLabel.setText(Messages.ExportModeGroup_size);
				dimField = new NumericControl(dimComp, NumericControl.LOGARITHMIC);
				dimField.setMinimum(16);
				dimField.setIncrement(10);
				dimField.setMaximum(32000);
				dimField.addListener(SWT.Selection, this);
			}
		}
		IRawConverter currentRawConverter = BatchActivator.getDefault().getCurrentRawConverter(false);
		if (currentRawConverter != null) {
			if (((style & RAWCROP) != 0) && currentRawConverter.isDetectors()) {
				cropButtonGroup = new RadioButtonGroup(modeGroup, Messages.ExportModeGroup_raw_image_cropping,
						SWT.HORIZONTAL, Messages.ExportModeGroup_uncropped, Messages.ExportModeGroup_cropmask,
						Messages.ExportModeGroup_cropped);
				cropButtonGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			}
			currentRawConverter.unget();
		}
		if (style != ORIGINALS) {
			qualityGroup = new QualityGroup(parent, false);
			qualityGroup.addListener(SWT.Selection, this);
		}
		updateControls();
	}

	@Override
	public void handleEvent(Event e) {
		updateControls();
		fireEvent(e);
	}

	protected void fireEvent(Event e) {
		e.type = SWT.Modify;
		e.data = this;
		for (Listener listener : listeners)
			listener.handleEvent(e);
	}

	public void updateScale() {
		if (scale != null)
			scaleLabel.setText(NLS.bind(Messages.SendEmailPage_Scale_n, scale.getSelection()));
	}

	public void setMaximumDim(int allmx) {
		if (dimField != null)
			dimField.setMaximum(allmx);
	}

	private void updateControls() {
		int selection = modeButtonGroup.getSelection();
		boolean visible = selection == jpeg || selection == webp;
		if (sizeComp != null) {
			sizeComp.setVisible(visible);
			stackComp.setVisible(visible);
		}
		if (qualityGroup != null)
			qualityGroup.setVisible(visible);
		if (cropButtonGroup != null)
			cropButtonGroup.setVisible(visible);
		if (sizeButtonGroup != null && visible) {
			int sizing = sizeButtonGroup.getSelection();
			if (sizing == s_scale || sizing == s_fixed) {
				stackComp.setVisible(true);
				stackLayout.topControl = sizing == s_scale ? scaleComp : dimComp;
				stackComp.layout(true, true);
			} else
				stackComp.setVisible(false);
		}
	}

	public void addListener(int type, Listener listener) {
		if (type == SWT.Modify)
			listeners.add(listener);
	}

	public void removeListener(int type, Listener listener) {
		listeners.remove(listener);
	}

	public void fillValues(IDialogSettings settings) {
		if (qualityGroup != null)
			qualityGroup.fillValues(settings);
		if (settings == null) {
			if (scale != null)
				scale.setSelection(30);
			modeButtonGroup.setSelection(1);
			if (dimField != null)
				dimField.setSelection(1280);
			if (cropButtonGroup != null)
				cropButtonGroup.setSelection(0);
		} else {
			try {
				int mode = settings.getInt(MODE);
				switch (mode) {
				case Constants.FORMAT_ORIGINAL:
					modeButtonGroup.setSelection(orig);
					scale.setEnabled(false);
					break;
				case Constants.FORMAT_JPEG:
					modeButtonGroup.setSelection(jpeg);
					break;
				case Constants.FORMAT_WEBP:
					modeButtonGroup.setSelection(webp);
					break;
				}
			} catch (NumberFormatException e) {
				// do nothing
			}
			if (sizeButtonGroup != null)
				try {
					int sizing = settings.getInt(SIZE);
					switch (sizing) {
					case Constants.SCALE_ORIGINAL:
						sizeButtonGroup.setSelection(s_orig);
						break;
					case Constants.SCALE_SCALED:
						sizeButtonGroup.setSelection(s_scale);
						break;
					case Constants.SCALE_FIXED:
						sizeButtonGroup.setSelection(s_fixed);
						break;
					}
				} catch (NumberFormatException e) {
					// do nothing
				}
			if (scale != null) {
				try {
					scale.setSelection(settings.getInt(SCALING));
					if (sizeButtonGroup != null && sizeButtonGroup.getSelection() == s_scale)
						scale.setFocus();
				} catch (NumberFormatException e) {
					scale.setSelection(30);
				}
				updateScale();
			}
			if (dimField != null) {
				try {
					dimField.setSelection(settings.getInt(FIXEDSIZE));
					if (sizeButtonGroup != null && sizeButtonGroup.getSelection() == s_fixed)
						dimField.setFocus();
				} catch (NumberFormatException e) {
					dimField.setSelection(1280);
				}
			}
			if (cropButtonGroup != null) {
				try {
					switch (settings.getInt(RAWCROPPING)) {
					case ZImage.UNCROPPED:
						cropButtonGroup.setSelection(0);
						break;
					case ZImage.CROPMASK:
						cropButtonGroup.setSelection(1);
						break;
					case ZImage.CROPPED:
						cropButtonGroup.setSelection(2);
						break;
					}
				} catch (NumberFormatException e) {
					cropButtonGroup.setSelection(0);
				}

			}
		}
		updateControls();
	}

	public void saveSettings(IDialogSettings settings) {
		if (qualityGroup != null)
			qualityGroup.saveSettings(settings);
		if (scale != null)
			settings.put(SCALING, scale.getSelection());
		settings.put(RAWCROPPING, getCropMode());
		if (dimField != null)
			settings.put(FIXEDSIZE, dimField.getSelection());
		settings.put(MODE, getMode());
		settings.put(SIZE, getSizing());
	}

	public int getMode() {
		int selection = modeButtonGroup.getSelection();
		return selection == webp ? Constants.FORMAT_WEBP
				: selection == jpeg ? Constants.FORMAT_JPEG : Constants.FORMAT_ORIGINAL;
	}

	public int getSizing() {
		int selection = sizeButtonGroup != null ? sizeButtonGroup.getSelection() : -1;
		return selection == s_fixed ? Constants.SCALE_FIXED
				: selection == s_scale ? Constants.SCALE_SCALED : Constants.SCALE_ORIGINAL;
	}

	public double getScalingFactor() {
		return scale == null || getSizing() != Constants.SCALE_SCALED ? 1d : scale.getSelection() / 100d;
	}

	public long computeScaledSize(long totalSize) {
		long scaledBytes = totalSize;
		if (scale != null && modeButtonGroup.getSelection() == 1) {
			int scaling = scale.getSelection();
			scaledBytes = scaledBytes * scaling * scaling / 10000;
			if (qualityGroup != null)
				scaledBytes = (long) (scaledBytes * qualityGroup.getSizeFactor());
		}
		return scaledBytes;
	}

	public int getDimension() {
		return dimField == null || getSizing() != Constants.SCALE_FIXED ? 1 : dimField.getSelection();
	}

	public UnsharpMask getUnsharpMask() {
		return qualityGroup != null ? qualityGroup.getUnsharpMask() : null;
	}

	public int getJpegQuality() {
		return qualityGroup != null ? qualityGroup.getJpegQuality() : -1;
	}

	public void setOriginalsEnabled(boolean enabled) {
		modeButtonGroup.setEnabled(0, enabled);
		if (!enabled) {
			modeButtonGroup.setSelection(1);
			if (scale != null)
				scale.setFocus();
		}
	}

	public int getCropMode() {
		if (cropButtonGroup != null) {
			int selection = cropButtonGroup.getSelection();
			if (selection == 1)
				return ZImage.CROPMASK;
			if (selection == 2)
				return ZImage.CROPPED;
		}
		return ZImage.UNCROPPED;
	}

	public int getQuality() {
		return qualityGroup != null ? qualityGroup.getQuality() : Constants.SCREEN_QUALITY;
	}

}
