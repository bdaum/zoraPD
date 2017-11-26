/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Berthold Daum, bdaum industrial communications - simplified for ZoRa usage
 *******************************************************************************/
package com.bdaum.zoom.ui.internal.preferences;

import java.awt.Canvas;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.Serializable;

import javax.swing.Icon;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IMemento;

import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.image.ImageUtilities;
import com.bdaum.zoom.ui.internal.Icons;

/**
 * @see IEditorDescriptor
 */
public final class EditorDescriptor implements Serializable {

	public static class ProgramLookup {

		private Program[] programs;

		public Program getProgram(String program) {
			if (programs == null)
				programs = Program.getPrograms();
			for (Program p : programs)
				if (program.equals(p.getName()))
					return p;
			return null;
		}

	}

	/**
	 * Generated serial version UID for this class.
	 *
	 * @since 3.1
	 */
	private static final long serialVersionUID = 3905241225668998961L;

	private String editorName;

	private String imageFilename;

	private transient ImageDescriptor imageDesc;

	private boolean testImage = true;

	private String launcherName;

	private String fileName;

	private String id = ""; //$NON-NLS-1$

	private Program program;

	/**
	 * Create a new instance of an editor descriptor. Limited to internal framework
	 * calls.
	 *
	 * @param id2
	 */
	/* package */ EditorDescriptor(String id2) {
		setID(id2);
	}

	/**
	 * Create a new instance of an editor descriptor. Limited to internal framework
	 * calls.
	 */
	/* package */ EditorDescriptor() {
		super();
	}

	/**
	 * Creates a descriptor for an external program.
	 *
	 * @param filename
	 *            the external editor full path and filename
	 * @return the editor descriptor
	 */
	public static EditorDescriptor createForProgram(String filename) {
		if (filename == null)
			throw new IllegalArgumentException();
		EditorDescriptor editor = new EditorDescriptor();

		editor.setFileName(filename);
		editor.setID(filename);

		// Isolate the program name (no directory or extension)
		int start = filename.lastIndexOf(File.separator);
		String name = start != -1 ? filename.substring(start + 1) : filename;
		int end = name.lastIndexOf('.');
		if (end != -1)
			editor.setName(name.substring(0, end));
		else
			editor.setName(name);

		editor.setImageDescriptor(Icons.file.getDescriptor());

		return editor;
	}

	/**
	 * Creates a descriptor for an external program.
	 *
	 * @param program
	 *            the external editor program
	 * @return the editor descriptor
	 */
	public static EditorDescriptor createForProgram(Program program) {
		if (program == null)
			throw new IllegalArgumentException();
		EditorDescriptor editor = new EditorDescriptor();
		editor.setProgram(program);
		return editor;
	}

	/**
	 * Return the file name of the command to execute for this editor.
	 *
	 * @return the file name to execute
	 */
	public String getFileName() {
		if (program != null)
			return program.getName();
		return fileName;
	}

	public String getPath() {
		return fileName;
	}

	/**
	 * Return the id for this editor.
	 *
	 * @return the id
	 */
	public String getId() {
		String s = (program != null) ? program.getName() : id;
		return (s == null) ? "" : s; //$NON-NLS-1$
	}

	/**
	 * Return the image descriptor describing this editor.
	 *
	 * @return the image descriptor
	 */
	public ImageDescriptor getImageDescriptor() {
		if (testImage) {
			testImage = false;
			if (imageDesc == null) {
				String command = getFileName();
				if (command != null)
					imageDesc = Icons.file.getDescriptor();
			}
			verifyImage();
		}

		return imageDesc;
	}

	/**
	 * Verifies that the image descriptor generates an image. If not, the descriptor
	 * is replaced with the default image.
	 *
	 * @since 3.1
	 */
	private void verifyImage() {
		if (imageDesc == null) {
			imageDesc = Icons.file.getDescriptor();
		} else {
			Image img = imageDesc.createImage(false);
			if (img == null) {
				// @issue what should be the default image?
				imageDesc = Icons.file.getDescriptor();
			} else {
				img.dispose();
			}
		}
	}

	/**
	 * The name of the image describing this editor.
	 *
	 * @return the image file name
	 */
	public String getImageFilename() {
		return imageFilename;
	}

	/**
	 * Return the user printable label for this editor.
	 *
	 * @return the label
	 */
	public String getLabel() {
		if (program != null)
			return program.getName();
		return editorName;
	}

	/**
	 * Returns the class name of the launcher.
	 *
	 * @return the launcher class name
	 */
	public String getLauncher() {
		return launcherName;
	}

	/**
	 * Load the object properties from a memento.
	 *
	 * @return <code>true</code> if the values are valid, <code>false</code>
	 *         otherwise
	 */
	protected void loadValues(IMemento memento, ProgramLookup lookup) {
		String progr = memento.getString("program"); //$NON-NLS-1$
		if (progr != null) {
			Program p = lookup.getProgram(progr);
			if (p != null)
				setProgram(p);
		}
		editorName = memento.getString("label"); //$NON-NLS-1$
		imageFilename = memento.getString("image"); //$NON-NLS-1$
		launcherName = memento.getString("launcher"); //$NON-NLS-1$
		fileName = memento.getString("file"); //$NON-NLS-1$
		id = memento.getString("id"); //$NON-NLS-1$
		if (id == null)
			id = ""; //$NON-NLS-1$
	}

	/**
	 * Save the object values in a IMemento
	 */
	protected void saveValues(IMemento memento) {
		memento.putString("label", getLabel()); //$NON-NLS-1$
		memento.putString("image", getImageFilename()); //$NON-NLS-1$
		memento.putString("launcher", getLauncher()); //$NON-NLS-1$
		memento.putString("file", getFileName()); //$NON-NLS-1$
		memento.putString("id", getId()); //$NON-NLS-1$
		if (this.program != null)
			memento.putString("program", this.program.getName()); //$NON-NLS-1$
	}

	/**
	 * Set the filename of an external editor.
	 */
	/* package */void setFileName(String aFileName) {
		fileName = aFileName;
	}

	/**
	 * Set the id of the editor. For internal editors this is the id as provided in
	 * the extension point For external editors it is path and filename of the
	 * editor
	 */
	/* package */void setID(String anID) {
		Assert.isNotNull(anID);
		id = anID;
	}

	/**
	 * The Image to use to repesent this editor
	 */
	/* package */void setImageDescriptor(ImageDescriptor desc) {
		imageDesc = desc;
		testImage = true;
	}

	/**
	 * The name of the image to use for this editor.
	 */
	/* package */void setImageFilename(String aFileName) {
		imageFilename = aFileName;
	}

	/**
	 * Sets the new launcher class name
	 *
	 * @param newLauncher
	 *            the new launcher
	 */
	/* package */void setLauncher(String newLauncher) {
		launcherName = newLauncher;
	}

	/**
	 * The label to show for this editor.
	 */
	/* package */void setName(String newName) {
		editorName = newName;
	}

	/**
	 * For debugging purposes only.
	 */

	@Override
	public String toString() {
		return "EditorDescriptor(id=" + getId() + ", label=" + getLabel() + ")"; //$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.activities.support.IPluginContribution#getLocalId()
	 */
	public String getLocalId() {
		return getId();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EditorDescriptor) {
			EditorDescriptor d2 = (EditorDescriptor) obj;
			return getFileName().equals(d2.getFileName()) && getId().equals(d2.getId());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getFileName().hashCode() * 31 + getId().hashCode();
	}

	public void setProgram(Program program) {
		this.program = program;
		if (editorName == null)
			setName(program.getName());
	}

	public Image createImage(Display display) {
		if (program != null) {
			ImageData imageData = program.getImageData();
			if (imageData != null)
				return new Image(display, imageData);
		}
		Icon fileIcon = Core.getCore().getVolumeManager().getFileIcon(getFileName());
		if (fileIcon != null) {
			BufferedImage icon2Image = icon2Image(fileIcon);
			ImageData data = ImageUtilities.bufferedImage2swt(icon2Image);
			if (data != null)
				return new Image(display, data);
		}
		return getImageDescriptor().createImage();
	}

	private static BufferedImage icon2Image(Icon icon) {
		BufferedImage bufimg = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(),
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bufimg.createGraphics();
		icon.paintIcon(new Canvas(), g, 0, 0);
		g.dispose();
		return bufimg;
	}

	/**
	 * Returns the program or null
	 *
	 * @return the program
	 */
	public Program getProgram() {
		return program;
	}

}
