package com.bdaum.zoom.video.internal.widgets;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
	private ListenerList<SelectionListener> listeners = new ListenerList<SelectionListener>();
	private int loudness;
	private Button snapButton;

	public VideoControl(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(6, false));
		scale = new Scale(this, SWT.HORIZONTAL);
		scale.setMaximum(1000);
		scale.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false,
				6, 1));
		scale.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fireSelection(e, POSITION, getPosition());
			}
		});
		playButton = new Button(this, SWT.PUSH);
		playButton.setImage(Icons.play.getImage());
		playButton.setToolTipText(Messages.VideoControl_continue);
		playButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fireSelection(e, PLAY, 0);
			}
		});
		stopButton = new Button(this, SWT.PUSH);
		stopButton.setImage(Icons.stop.getImage());
		stopButton.setToolTipText(Messages.VideoControl_stop);
		stopButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fireSelection(e, STOP, 0);
			}
		});
		snapButton = new Button(this, SWT.PUSH);
		snapButton.setImage(Icons.snapshot.getImage());
		snapButton.setToolTipText(Messages.VideoControl_snapshot);
		snapButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fireSelection(e, SNAP, 0);
			}
		});
		infoLabel = new Label(this, SWT.NONE);
		infoLabel.setLayoutData(new GridData(250, SWT.DEFAULT));
		infoLabel.setAlignment(SWT.CENTER);
		soundButton = new Button(this, SWT.CHECK);
		soundButton.setImage(Icons.sound.getImage());
		soundButton.setToolTipText(Messages.VideoControl_toggle_sound);
		soundButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				loudnessCanvas.redraw();
				boolean on = soundButton.getSelection();
				soundButton.setImage(on ? Icons.sound.getImage()
						: Icons.soundMute.getImage());
				loudnessCanvas.redraw();
				fireSelection(e, SOUND, on ? 1 : 0);
			}
		});
		final Image image = Icons.volume.getImage();
		final Image image_d = Icons.volume_d.getImage();
		final Rectangle ibounds = image.getBounds();
		loudnessCanvas = new Canvas(this, SWT.DOUBLE_BUFFERED);
		loudnessCanvas.setToolTipText(Messages.VideoControl_volume);
		loudnessCanvas
				.setLayoutData(new GridData(ibounds.width, ibounds.height));
		loudnessCanvas.addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
				GC gc = e.gc;
				Rectangle area = loudnessCanvas.getClientArea();
				gc.drawImage(soundButton.getSelection() ? image : image_d, 0,
						0, ibounds.width * loudness / 200, ibounds.height, 0,
						0, area.width * loudness / 200, area.height);
				gc.setForeground(e.display.getSystemColor(SWT.COLOR_WHITE));
				gc.drawRectangle(area.x, area.y, area.width-1, area.height-1);
			}
		});
		loudnessCanvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (soundButton.getSelection()) {
					Rectangle area = loudnessCanvas.getClientArea();
					setLoudness(e.x * 200 / area.width);
					Event ev = new Event();
					ev.display = e.display;
					ev.widget = e.widget;
					ev.time = e.time;
					ev.stateMask = e.stateMask;
					fireSelection(new SelectionEvent(ev), LOUDNESS, loudness);
				}
			}
		});
	}

	public void addSelectionListener(SelectionListener listener) {
		listeners.add(listener);
	}

	public void removeSelectionListener(SelectionListener listener) {
		listeners.remove(listener);
	}

	private void fireSelection(SelectionEvent e, int type, int value) {
		e.detail = type | value;
		for (Object listener : listeners.getListeners()) {
			((SelectionListener) listener).widgetSelected(e);
		}
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
		soundButton.setSelection(on);
		soundButton.setImage(on ? Icons.sound.getImage() : Icons.soundMute
				.getImage());
		loudnessCanvas.redraw();
	}

	public int getPosition() {
		return scale.getSelection();
	}

	public int getLoudness() {
		return loudness;
	}

	public boolean getSound() {
		return soundButton.getSelection();
	}

}
