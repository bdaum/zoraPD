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
 * (c) 2018 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.views;

import java.io.IOException;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.IWorkbenchWindow;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.ui.IStateListener;
import com.bdaum.zoom.ui.ITransformListener;
import com.bdaum.zoom.ui.ITransformProvider;
import com.bdaum.zoom.ui.internal.IKiosk;
import com.bdaum.zoom.ui.views.IMediaViewer;

public class MultiViewer extends AbstractMediaViewer implements ITransformListener, IStateListener {

	private IMediaViewer leftViewer;
	private IMediaViewer rightViewer;
	private boolean sync = false;
	private boolean vert;

	public MultiViewer(IMediaViewer leftViewer, IMediaViewer rightViewer) {
		this.leftViewer = leftViewer;
		this.rightViewer = rightViewer;
	}

	@Override
	public void init(IWorkbenchWindow parentWindow, int kind, RGB bw, int crop) {
		super.init(parentWindow, PRIMARY, bw, crop);
		leftViewer.init(parentWindow, LEFT, bw, crop);
		rightViewer.init(parentWindow, RIGHT, bw, crop);
		leftViewer.addStateListener(this);
		rightViewer.addStateListener(this);
		if (leftViewer instanceof ITransformProvider && rightViewer instanceof ITransformProvider) {
			if (leftViewer instanceof ITransformProvider)
				((ITransformProvider) leftViewer).addTransformListener(this);
			if (rightViewer instanceof ITransformProvider)
				((ITransformProvider) rightViewer).addTransformListener(this);
		}
	}

	@Override
	public void create() {
		super.create();
		vert = mbounds.height > mbounds.width;
		leftViewer.setBounds(vert ? new Rectangle(mbounds.x, mbounds.y, mbounds.width, mbounds.height / 2)
				: new Rectangle(mbounds.x, mbounds.y, mbounds.width / 2, mbounds.height));
		leftViewer.create();
		rightViewer.setBounds(
				vert ? new Rectangle(mbounds.x, mbounds.y + mbounds.height / 2, mbounds.width, mbounds.height / 2)
						: new Rectangle(mbounds.x + mbounds.width / 2, mbounds.y, mbounds.width / 2, mbounds.height));
		rightViewer.create();
	}

	@Override
	public void open(Asset[] assets) throws IOException {
		if (mbounds == null)
			create();
		leftViewer.open(new Asset[] { assets[0] });
		rightViewer.open(new Asset[] { assets[1] });
		while (!isDisposed())
			if (!display.readAndDispatch())
				display.sleep();
	}

	@Override
	public boolean close() {
		return close(null);
	}

	private boolean close(IKiosk closer) {
		rightViewer.removeStateListener(this);
		leftViewer.removeStateListener(this);
		if (closer != rightViewer)
			rightViewer.close();
		if (closer != leftViewer)
			leftViewer.close();
		if (leftViewer instanceof ITransformProvider)
			((ITransformProvider) leftViewer).removeTransformListener(this);
		if (rightViewer instanceof ITransformProvider)
			((ITransformProvider) rightViewer).removeTransformListener(this);
		return super.close();
	}

	@Override
	public boolean isDisposed() {
		return leftViewer.isDisposed() && rightViewer.isDisposed();
	}

	@Override
	public void transformChanged(ITransformProvider provider, double translateX, double translateY, double scale) {
		if (sync) {
			if (leftViewer instanceof ITransformProvider && leftViewer != provider && !leftViewer.isDisposed())
				((ITransformProvider) leftViewer).setTransform(translateX, translateY, scale);
			if (rightViewer instanceof ITransformProvider && rightViewer != provider && !rightViewer.isDisposed())
				((ITransformProvider) rightViewer).setTransform(translateX, translateY, scale);
		}
	}

	@Override
	public void stateChanged(IKiosk provider, int state) {
		if (state == CLOSED)
			close(provider);
		else if (state == SYNC) {
			sync = !sync;
			if (leftViewer instanceof ITransformProvider && !leftViewer.isDisposed())
				((ITransformProvider) leftViewer).setSync(sync, vert);
			if (rightViewer instanceof ITransformProvider && !rightViewer.isDisposed())
				((ITransformProvider) rightViewer).setSync(sync, vert);
		}
	}

}
