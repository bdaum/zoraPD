package com.bdaum.zoom.video.internal.widgets;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;

import com.bdaum.zoom.video.internal.Icons;

public class VideoControl extends Composite {

	public static final int POSITION = 0x00000000;
	public static final int PLAY = 0x80000000;
	public static final int STOP = 0xC0000000;
	public static final int LOUDNESS = 0xA0000000;
	public static final int SOUND = 0x90000000;
	public static final int SNAP = 0xB0000000;
	public static final int EVENTTYPES = 0xF0000000;
	public static final int VOLUMEMASK = 0x0FFFFFFF;
	private Scale scale;
	private Button playButton;
	private Button stopButton;
	private Label infoLabel;
	private Button soundButton;
	private Canvas loudnessCanvas;
	private ListenerList<Listener> listeners = new ListenerList<>();
	private int loudness;
	private Button snapButton;
	private boolean soundOn = true;

	public VideoControl(Composite parent, int style) {
		super(parent, style);
		final Image image = Icons.volume.getImage();
		final Rectangle ibounds = image.getBounds();
		setLayout(new GridLayout(6, false));
		Listener listener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (event.widget == scale)
					fireSelection(event, POSITION, getPosition());
				else if (event.widget == playButton)
					fireSelection(event, PLAY, 0);
				else if (event.widget == stopButton)
					fireSelection(event, STOP, 0);
				else if (event.widget == snapButton)
					fireSelection(event, SNAP, 0);
				else if (event.widget == soundButton) {
					setSound(soundOn = !soundOn);
					fireSelection(event, SOUND, soundOn ? 1 : 0);
				} else if (event.widget == loudnessCanvas) {
					if (event.type == SWT.Paint) {
						GC gc = event.gc;
						Rectangle area = loudnessCanvas.getClientArea();
						gc.drawImage(getSound() ? image : Icons.volume_d.getImage(), 0, 0,
								ibounds.width * loudness / 200, ibounds.height, 0, 0, area.width * loudness / 200,
								area.height);
						gc.setForeground(event.display.getSystemColor(SWT.COLOR_WHITE));
						gc.drawRectangle(area.x, area.y, area.width - 1, area.height - 1);
					} else if (getSound()) {
						setLoudness(event.x * 200 / loudnessCanvas.getClientArea().width);
						event.type = SWT.Selection;
						fireSelection(event, LOUDNESS, loudness);
					}
				}
			}
		};
		scale = new Scale(this, SWT.HORIZONTAL);
		scale.setMaximum(1000);
		scale.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 6, 1));
		scale.addListener(SWT.Selection, listener);
		playButton = new Button(this, SWT.PUSH);
		playButton.setImage(Icons.play.getImage());
		playButton.setToolTipText(Messages.VideoControl_continue);
		playButton.addListener(SWT.Selection, listener);
		stopButton = new Button(this, SWT.PUSH);
		stopButton.setImage(Icons.stop.getImage());
		stopButton.setToolTipText(Messages.VideoControl_stop);
		stopButton.addListener(SWT.Selection, listener);
		snapButton = new Button(this, SWT.PUSH);
		snapButton.setImage(Icons.snapshot.getImage());
		snapButton.setToolTipText(Messages.VideoControl_snapshot);
		snapButton.addListener(SWT.Selection, listener);
		infoLabel = new Label(this, SWT.NONE);
		infoLabel.setLayoutData(new GridData(250, SWT.DEFAULT));
		infoLabel.setAlignment(SWT.CENTER);
		soundButton = new Button(this, SWT.PUSH);
		soundButton.setImage(Icons.sound.getImage());
		soundButton.setToolTipText(Messages.VideoControl_toggle_sound);
		soundButton.addListener(SWT.Selection, listener);
		loudnessCanvas = new Canvas(this, SWT.DOUBLE_BUFFERED);
		loudnessCanvas.setToolTipText(Messages.VideoControl_volume);
		loudnessCanvas.setLayoutData(new GridData(ibounds.width, ibounds.height));
		loudnessCanvas.addListener(SWT.Paint, listener);
		loudnessCanvas.addListener(SWT.MouseDown, listener);
	}

	public void addListener(Listener listener) {
		listeners.add(listener);
	}

	public void removeListener(Listener listener) {
		listeners.remove(listener);
	}

	private void fireSelection(Event e, int type, int value) {
		e.detail = type | value;
		for (Listener listener : listeners)
			listener.handleEvent(e);
	}

	/**
	 * @param position
	 *            0...1000
	 */
	public void setPosition(int position) {
		scale.setSelection(position);
	}

	public void setInfo(String info) {
		infoLabel.setText(info);
	}

	public void setLoudness(int loudness) {
		this.loudness = loudness;
		loudnessCanvas.redraw();
	}

	public void setSound(boolean on) {
		soundButton.setImage(on ? Icons.sound.getImage() : Icons.soundMute.getImage());
		loudnessCanvas.redraw();
	}

	public int getPosition() {
		return scale.getSelection();
	}

	public int getLoudness() {
		return loudness;
	}

	public boolean getSound() {
		return soundOn;
	}

}
