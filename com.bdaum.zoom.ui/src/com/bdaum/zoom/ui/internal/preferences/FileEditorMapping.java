/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jan-Hendrik Diederich, Bredex GmbH - bug 201052
 *     Berthold Daum, bdaum industrial communications - simplified for ZoRa usage
 *******************************************************************************/
package com.bdaum.zoom.ui.internal.preferences;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMemento;

import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.ui.internal.Icons;

/**
 * Implementation of IFileEditorMapping.
 *
 */
public class FileEditorMapping implements Cloneable {

	private static final String[] EMPTYSTRINGARRAY = new String[0];
	private static final String STAR = "*"; //$NON-NLS-1$
	private static final String DOT = "."; //$NON-NLS-1$

	private String name = STAR;

	private boolean rememberLast = false;

	private String[] extensions;

	// Collection of EditorDescriptor, where the first one
	// if considered the default one.
	private List<EditorDescriptor> editors = new ArrayList<EditorDescriptor>(1);

	private List<EditorDescriptor> declaredDefaultEditors = new ArrayList<EditorDescriptor>(
			1);

	/**
	 * Create an instance of this class.
	 *
	 * @param extension
	 *            java.lang.String
	 */
	public FileEditorMapping(String[] extensions) {
		this(STAR, extensions);
	}

	/**
	 * Create an instance of this class.
	 *
	 * @param name
	 *            java.lang.String
	 * @param extension
	 *            java.lang.String
	 */
	public FileEditorMapping(String name, String[] extensions) {
		super();
		if (name == null || name.length() < 1) {
			setName(STAR);
		} else {
			setName(name);
		}
		if (extensions == null) {
			setExtensions(EMPTYSTRINGARRAY);
		} else {
			setExtensions(extensions);
		}
	}

	/**
	 * Add the given editor to the list of editors registered.
	 *
	 * @param editor
	 *            the editor to add
	 *
	 */
	public boolean addEditor(EditorDescriptor editor) {
		if (editors.contains(editor))
			return false;
		return editors.add(editor);
	}

	/**
	 * Clone the receiver.
	 */

	@SuppressWarnings("unchecked")
	@Override
	public Object clone() {
		try {
			FileEditorMapping clone = (FileEditorMapping) super.clone();
			clone.editors = (List<EditorDescriptor>) ((ArrayList<EditorDescriptor>) editors)
					.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof FileEditorMapping))
			return false;
		FileEditorMapping mapping = (FileEditorMapping) obj;
		if (!this.name.equals(mapping.name))
			return false;
		if (this.extensions.length != mapping.extensions.length)
			return false;
		for (int i = 0; i < this.extensions.length; i++) {
			if (!this.extensions[i].equals(mapping.extensions[i]))
				return false;
		}
		return compareList(this.editors, mapping.editors);
	}

	@Override
	public int hashCode() {
		int hash = name.hashCode();
		for (int i = 0; i < this.extensions.length; i++) {
			hash = 31 * hash + extensions[i].hashCode();
		}
		return hash;
	}

	/**
	 * Compare the editor ids from both lists and return true if they are
	 * equals.
	 */
	private static boolean compareList(List<EditorDescriptor> l1,
			List<EditorDescriptor> l2) {
		if (l1.size() != l2.size()) {
			return false;
		}

		Iterator<EditorDescriptor> i1 = l1.iterator();
		Iterator<EditorDescriptor> i2 = l2.iterator();
		while (i1.hasNext() && i2.hasNext()) {
			Object o1 = i1.next();
			Object o2 = i2.next();
			if (!(o1 == null ? o2 == null : o1.equals(o2))) {
				return false;
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc) Method declared on IFileEditorMapping.
	 */
	public EditorDescriptor getDefaultEditor() {

		if (editors.size() == 0) {
			return null;
		}

		return editors.get(0);
	}

	/*
	 * (non-Javadoc) Method declared on IFileEditorMapping.
	 */
	public EditorDescriptor[] getEditors() {
		return editors.toArray(new EditorDescriptor[editors.size()]);
	}

	/*
	 * (non-Javadoc) Method declared on IFileEditorMapping.
	 */
	public String[] getExtensions() {
		return extensions;
	}

	public Image createImage(Display display) {
		EditorDescriptor editor = getDefaultEditor();
		if (editor == null)
			return getImageDescriptor().createImage(false);
		return editor.createImage(display);
	}

	/*
	 * (non-Javadoc) Method declared on IFileEditorMapping.
	 */
	public ImageDescriptor getImageDescriptor() {
		EditorDescriptor editor = getDefaultEditor();
		if (editor == null)
			return Icons.file.getDescriptor();
		return editor.getImageDescriptor();
	}

	/*
	 * (non-Javadoc) Method declared on IFileEditorMapping.
	 */
	public String getLabel() {
		return serialize(extensions, STAR + DOT);
	}

	private static String serialize(String[] extns, String prefix) {
		if (extns.length == 0)
			return ""; //$NON-NLS-1$
		StringBuilder sb = new StringBuilder();
		for (String extension : extns) {
			if (sb.length() > 0)
				sb.append(";"); //$NON-NLS-1$
			sb.append(prefix).append(extension);
		}
		return sb.toString();
	}

	/*
	 * (non-Javadoc) Method declared on IFileEditorMapping.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Remove the given editor from the set of editors registered.
	 *
	 * @param editor
	 *            the editor to remove
	 */
	public void removeEditor(EditorDescriptor editor) {
		editors.remove(editor);
		declaredDefaultEditors.remove(editor);
	}

	/**
	 * Set the default editor registered for file type described by this
	 * mapping.
	 *
	 * @param editor
	 *            the editor to be set as default
	 */
	public void setDefaultEditor(EditorDescriptor editor) {
		editors.remove(editor);
		editors.add(0, editor);
		declaredDefaultEditors.clear();
		declaredDefaultEditors.add(editor);
	}

	/**
	 * Set the collection of all editors (EditorDescriptor) registered for the
	 * file type described by this mapping. Typically an editor is registered
	 * either through a plugin or explicitly by the user modifying the
	 * associations in the preference pages. This modifies the internal list to
	 * share the passed list. (hence the clear indication of list in the method
	 * name)
	 *
	 * @param newEditors
	 *            the new list of associated editors
	 */
	public void setEditorsList(List<EditorDescriptor> newEditors) {
		editors = newEditors;
		declaredDefaultEditors.retainAll(newEditors);
	}

	/**
	 * Set the file's extension.
	 *
	 * @param extension
	 *            the file extension for this mapping
	 */
	public void setExtensions(String[] extensions) {
		this.extensions = extensions;
	}

	/**
	 * Set the file's name.
	 *
	 * @param name
	 *            the file name for this mapping
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the editors that have been declared as default. This may be via
	 * plugin declarations or the preference page.
	 *
	 * @return the editors the default editors
	 * @since 3.1
	 */
	public EditorDescriptor[] getDeclaredDefaultEditors() {
		return declaredDefaultEditors
				.toArray(new EditorDescriptor[declaredDefaultEditors.size()]);
	}

	/**
	 * Return whether the editor is declared default. If this is
	 * EditorDescriptor fails the ExpressionsCheck it will always return
	 * <code>false</code>, even if it's the original default editor.
	 *
	 * @param editor
	 *            the editor to test
	 * @return whether the editor is declared default
	 * @since 3.1
	 */
	public boolean isDeclaredDefaultEditor(EditorDescriptor editor) {
		return declaredDefaultEditors.contains(editor);
	}

	/**
	 * Set the default editors for this mapping.
	 *
	 * @param defaultEditors
	 *            the editors
	 * @since 3.1
	 */
	public void setDefaultEditors(List<EditorDescriptor> defaultEditors) {
		declaredDefaultEditors = defaultEditors;
	}

	/**
	 * Save the object values in a IMemento
	 */
	protected void saveValues(IMemento memento) {
		memento.putString("extension", serialize(extensions, "")); //$NON-NLS-1$ //$NON-NLS-2$
		memento.putString("name", getName()); //$NON-NLS-1$
		memento.putBoolean("remember", rememberLast); //$NON-NLS-1$
		IMemento child = memento.createChild("editors"); //$NON-NLS-1$
		for (EditorDescriptor editor : getEditors()) {
			IMemento grandChild = child.createChild("editor"); //$NON-NLS-1$
			editor.saveValues(grandChild);
		}
		child = memento.createChild("defaultEditors"); //$NON-NLS-1$
		for (EditorDescriptor editor : getDeclaredDefaultEditors()) {
			IMemento grandChild = child.createChild("editor"); //$NON-NLS-1$
			editor.saveValues(grandChild);
		}
	}

	/**
	 * Load the object properties from a memento.
	 *
	 * @return <code>true</code> if the values are valid, <code>false</code>
	 *         otherwise
	 */
	protected boolean loadValues(IMemento memento) {
		String s = memento.getString("extension"); //$NON-NLS-1$
		List<String> list;
		if (s != null) {
			list = Core.fromStringList(s, ";"); //$NON-NLS-1$
			extensions = list.toArray(new String[list.size()]);
		} else
			extensions = EMPTYSTRINGARRAY;

		name = memento.getString("name"); //$NON-NLS-1$
		Boolean b = memento.getBoolean("remember"); //$NON-NLS-1$
		if (b != null)
			rememberLast = b.booleanValue();
		EditorDescriptor.ProgramLookup lookup = new EditorDescriptor.ProgramLookup();
		IMemento child = memento.getChild("editors"); //$NON-NLS-1$
		for (IMemento grandchild : child.getChildren("editor")) { //$NON-NLS-1$
			EditorDescriptor editor = new EditorDescriptor();
			editor.loadValues(grandchild, lookup);
			addEditor(editor);
		}
		child = memento.getChild("defaultEditors"); //$NON-NLS-1$
		for (IMemento grandchild : child.getChildren("editor")) { //$NON-NLS-1$
			EditorDescriptor editor = new EditorDescriptor();
			editor.loadValues(grandchild, lookup);
			setDefaultEditor(editor);
		}
		return true;
	}

	public boolean isRememberLast() {
		return rememberLast;
	}

	public void setRememberLast(boolean rememberLast) {
		this.rememberLast = rememberLast;
	}

}
