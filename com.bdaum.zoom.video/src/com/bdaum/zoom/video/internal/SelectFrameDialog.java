package com.bdaum.zoom.video.internal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.internal.ImportState;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.image.ZImage;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.UpdateThumbnailOperation;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.video.model.Video;
import com.bdaum.zoom.video.model.VideoImpl;

@SuppressWarnings("restriction")
public class SelectFrameDialog extends ZTitleAreaDialog implements Listener {

	private Asset asset;
	private ZImage zimage;
	private int currentFrame = 0;
	private Button leftArrow;
	private Button leftleftArrow;
	private Button rightrightArrow;
	private Button rightArrow;
	private Scale scale;
	private File file;
	private Canvas canvas;
	private int frames;
	private int rate;
	private Label statusLabel;
	private double[] frameCountBox = new double[2];

	public SelectFrameDialog(Shell parentShell, Asset asset) {
		super(parentShell);
		this.asset = asset;
		URI uri = Core.getCore().getVolumeManager().findExistingFile(asset, true);
		file = new File(uri);
		Video vx = Utilities.getMediaExtension(asset, VideoImpl.class);
		if (vx != null) {
			double duration = vx.getDuration();
			double videoFrameRate = vx.getVideoFrameRate();
			if (Double.isNaN(videoFrameRate)) {
				frames = (int) (30 * duration);
				rate = 30;
			} else {
				frames = (int) (videoFrameRate * duration);
				rate = (int) (videoFrameRate + 0.5d);
			}
			currentFrame = Math.max(0, Math.min(frames - 1, vx.getFrameNo()));
		}
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.SelectFrameDialog_update_thumb);
		setMessage(Messages.SelectFrameDialog_update_thumb_msg);
		selectFrame(currentFrame, true);
		frames = (int) frameCountBox[0];
		rate = (int) (frameCountBox[1] + 0.5d);
		configureScale();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(5, false));
		canvas = new Canvas(composite, SWT.BORDER | SWT.DOUBLE_BUFFERED);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 5, 1);
		layoutData.widthHint = 640;
		layoutData.heightHint = 480;
		canvas.setLayoutData(layoutData);
		canvas.addListener(SWT.Paint, this);
		leftArrow = new Button(composite, SWT.PUSH);
		leftArrow.setImage(Icons.left.getImage());
		leftArrow.addListener(SWT.Selection, this);
		leftleftArrow = new Button(composite, SWT.PUSH);
		leftleftArrow.setImage(Icons.leftleft.getImage());
		leftleftArrow.addListener(SWT.Selection, this);
		scale = new Scale(composite, SWT.HORIZONTAL);
		scale.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		configureScale();
		scale.addListener(SWT.Selection, this);
		rightrightArrow = new Button(composite, SWT.PUSH);
		rightrightArrow.setImage(Icons.rightright.getImage());
		rightrightArrow.addListener(SWT.Selection, this);
		rightArrow = new Button(composite, SWT.PUSH);
		rightArrow.setImage(Icons.right.getImage());
		rightArrow.addListener(SWT.Selection, this);
		return area;
	}

	private void configureScale() {
		scale.setMaximum(frames);
		int incr = frames / 250 * 5;
		scale.setIncrement(incr);
		scale.setPageIncrement(incr*5);
	}

	@Override
	public void handleEvent(Event e) {
		switch (e.type) {
		case SWT.Paint:
			if (zimage != null)
				zimage.draw(e.gc, 0, 0, ZImage.UNCROPPED, 640, 400);
			break;
		default:
			Widget widget = e.widget;
			if (widget == leftArrow)
				selectFrame(Math.max(1, currentFrame - 1), true);
			else if (widget == leftleftArrow)
				selectFrame(Math.max(1, currentFrame - rate), true);
			else if (widget == rightrightArrow)
				selectFrame(Math.min(frames, currentFrame + rate), true);
			else if (widget == rightArrow)
				selectFrame(Math.min(frames, currentFrame + 1), true);
			else if (widget == scale)
				selectFrame(scale.getSelection(), false);
			break;
		}
	}

	private void selectFrame(int frame, boolean updateScale) {
		if (zimage != null) {
			zimage.dispose();
			zimage = null;
		}
		if (updateScale)
			scale.setSelection(frame);
		try {
			zimage = VideoSupport.decodeAndCaptureFrames(file, 640, 8, frame, frameCountBox, null);
			canvas.redraw();
			currentFrame = frame;
		} catch (IOException | UnsupportedOperationException e) {
			scale.removeListener(SWT.Selection, this);
			scale.setSelection(currentFrame);
			scale.addListener(SWT.Selection, this);
		}
		statusLabel.setText(NLS.bind(Messages.SelectFrameDialog_n_of_m, currentFrame, frames));
		updateButtons();
	}

	private void updateButtons() {
		getButton(OK).setEnabled(zimage != null);
		leftArrow.setEnabled(currentFrame > 0);
		leftleftArrow.setEnabled(currentFrame >= rate);
		rightArrow.setEnabled(currentFrame < frames - 1);
		rightrightArrow.setEnabled(currentFrame < frames - rate);
	}

	@Override
	protected void okPressed() {
		if (zimage != null) {
			zimage.dispose();
			zimage = null;
		}
		Meta meta = Core.getCore().getDbManager().getMeta(true);
		try (ByteArrayOutputStream out = new ByteArrayOutputStream(20000)) {
			int width = ImportState.computeThumbnailWidth(meta.getThumbnailResolution());
			zimage = VideoSupport.decodeAndCaptureFrames(file, width, 8, currentFrame, null, null);
			zimage.saveToStream(null, true, ZImage.CROPMASK, SWT.DEFAULT, SWT.DEFAULT, out,
					meta.getWebpCompression() ? ZImage.IMAGE_WEBP : SWT.IMAGE_JPEG, meta.getJpegQuality());
			OperationJob.executeOperation(new UpdateThumbnailOperation(asset, out.toByteArray(), currentFrame), this);
		} catch (IOException | UnsupportedOperationException e) {
			// do nothing
		}
		super.okPressed();
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		statusLabel = new Label(parent, SWT.NONE);
		GridData layoutData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		layoutData.widthHint = 150;
		statusLabel.setLayoutData(layoutData);
		((GridLayout) parent.getLayout()).numColumns++;
		super.createButtonsForButtonBar(parent);
	}

	@Override
	public boolean close() {
		if (zimage != null) {
			zimage.dispose();
			zimage = null;
		}
		return super.close();
	}

}
