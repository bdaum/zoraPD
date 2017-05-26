/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Berthold Daum - derived this class from StringFieldEditor
 *******************************************************************************/
package com.bdaum.zoom.ui.internal.widgets;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Scale;

import com.bdaum.zoom.ui.internal.preferences.Messages;

public class ScaleFieldEditor extends FieldEditor {

	/**
	 * Validation strategy constant (value <code>0</code>) indicating that
	 * the editor should perform validation after every key stroke.
	 * 
	 * @see #setValidateStrategy
	 */
	public static final int VALIDATE_ON_KEY_STROKE = 0;

	/**
	 * Validation strategy constant (value <code>1</code>) indicating that
	 * the editor should perform validation only when the text widget loses
	 * focus.
	 * 
	 * @see #setValidateStrategy
	 */
	public static final int VALIDATE_ON_FOCUS_LOST = 1;

	/**
	 * Text limit constant (value <code>-1</code>) indicating unlimited text
	 * limit and width.
	 */
	public static int UNLIMITED = -1;

	/**
	 * Cached valid state.
	 */
	private boolean isValid;

	/**
	 * Old text value.
	 */
	private int oldValue;

	/**
	 * The text field, or <code>null</code> if none.
	 */
	Scale scale;

	/**
	 * Width of text field in characters; initially unlimited.
	 */
	private int widthInChars = UNLIMITED;

	/**
	 * The error message, or <code>null</code> if none.
	 */
	private String errorMessage;

	/**
	 * The validation strategy; <code>VALIDATE_ON_KEY_STROKE</code> by
	 * default.
	 */
	private int validateStrategy = VALIDATE_ON_KEY_STROKE;

	/**
	 * Creates a new string field editor
	 */
	protected ScaleFieldEditor() {
	}

	/**
	 * Creates a string field editor. Use the method <code>setTextLimit</code>
	 * to limit the text.
	 * 
	 * @param name
	 *            the name of the preference this field editor works on
	 * @param labelText
	 *            the label text of the field editor
	 * @param width
	 *            the width of the text input field in characters, or
	 *            <code>UNLIMITED</code> for no limit
	 * @param strategy
	 *            either <code>VALIDATE_ON_KEY_STROKE</code> to perform on the
	 *            fly checking (the default), or
	 *            <code>VALIDATE_ON_FOCUS_LOST</code> to perform validation
	 *            only after the text has been typed in
	 * @param parent
	 *            the parent of the field editor's control
	 * @since 2.0
	 */
	public ScaleFieldEditor(String name, String labelText, int width,
			int strategy, Composite parent) {
		init(name, labelText);
		widthInChars = width;
		setValidateStrategy(strategy);
		isValid = false;
		errorMessage = Messages.getString("SpinnerFieldEditor.invalid_field_value"); //$NON-NLS-1$
		createControl(parent);
	}

	/**
	 * Creates a string field editor. Use the method <code>setTextLimit</code>
	 * to limit the text.
	 * 
	 * @param name
	 *            the name of the preference this field editor works on
	 * @param labelText
	 *            the label text of the field editor
	 * @param width
	 *            the width of the text input field in characters, or
	 *            <code>UNLIMITED</code> for no limit
	 * @param parent
	 *            the parent of the field editor's control
	 */
	public ScaleFieldEditor(String name, String labelText, int width,
			Composite parent) {
		this(name, labelText, width, VALIDATE_ON_KEY_STROKE, parent);
	}

	/**
	 * Creates a string field editor of unlimited width. Use the method
	 * <code>setTextLimit</code> to limit the text.
	 * 
	 * @param name
	 *            the name of the preference this field editor works on
	 * @param labelText
	 *            the label text of the field editor
	 * @param parent
	 *            the parent of the field editor's control
	 */
	public ScaleFieldEditor(String name, String labelText, Composite parent) {
		this(name, labelText, UNLIMITED, parent);
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	
	@Override
	protected void adjustForNumColumns(int numColumns) {
		GridData gd = (GridData) scale.getLayoutData();
		gd.horizontalSpan = numColumns - 1;
		// We only grab excess space if we have to
		// If another field editor has more columns then
		// we assume it is setting the width.
		gd.grabExcessHorizontalSpace = gd.horizontalSpan == 1;
	}

	/**
	 * Checks whether the text input field contains a valid value or not.
	 * 
	 * @return <code>true</code> if the field value is valid, and
	 *         <code>false</code> if invalid
	 */
	protected boolean checkState() {
		// call hook for subclasses
		boolean result = scale != null && doCheckState();

		if (result)
			clearErrorMessage();
		else
			showErrorMessage(errorMessage);

		return result;
	}

	/**
	 * Hook for subclasses to do specific state checks.
	 * <p>
	 * The default implementation of this framework method does nothing and
	 * returns <code>true</code>. Subclasses should override this method to
	 * specific state checks.
	 * </p>
	 * 
	 * @return <code>true</code> if the field value is valid, and
	 *         <code>false</code> if invalid
	 */
	protected boolean doCheckState() {
		return true;
	}

	/**
	 * Fills this field editor's basic controls into the given parent.
	 * <p>
	 * The string field implementation of this <code>FieldEditor</code>
	 * framework method contributes the text field. Subclasses may override but
	 * must call <code>super.doFillIntoGrid</code>.
	 * </p>
	 */
	
	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		getLabelControl(parent);

		scale = getControl(parent);
		GridData gd = new GridData();
		gd.horizontalSpan = numColumns - 1;
		if (widthInChars != UNLIMITED) {
			GC gc = new GC(scale);
			try {
				Point extent = gc.textExtent("X");//$NON-NLS-1$
				gd.widthHint = widthInChars * extent.x;
			} finally {
				gc.dispose();
			}
		} else {
			gd.horizontalAlignment = GridData.FILL;
			gd.grabExcessHorizontalSpace = true;
		}
		scale.setLayoutData(gd);
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	
	@Override
	protected void doLoad() {
		if (scale != null) {
			int value = getPreferenceStore().getInt(getPreferenceName());
			scale.setSelection(value);
			oldValue = value;
		}
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	
	@Override
	protected void doLoadDefault() {
		if (scale != null) {
			int value = getPreferenceStore().getDefaultInt(getPreferenceName());
			scale.setSelection(value);
		}
		valueChanged();
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	
	@Override
	protected void doStore() {
		getPreferenceStore().setValue(getPreferenceName(),
				scale.getSelection());
	}

	/**
	 * Returns the error message that will be displayed when and if an error
	 * occurs.
	 * 
	 * @return the error message, or <code>null</code> if none
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	
	@Override
	public int getNumberOfControls() {
		return 2;
	}

	/**
	 * Returns the field editor's value.
	 * 
	 * @return the current value
	 */
	public int getIntValue() {
		return (scale != null) ? scale.getSelection()
				: getPreferenceStore().getInt(getPreferenceName());
	}

	/**
	 * Returns this field editor's text control.
	 * 
	 * @return the scale control, or <code>null</code> if no scale field is
	 *         created yet
	 */
	protected Scale getControl() {
		return scale;
	}

	/**
	 * Returns this field editor's scale control.
	 * <p>
	 * The control is created if it does not yet exist
	 * </p>
	 * 
	 * @param parent
	 *            the parent
	 * @return the slider control
	 */
	public Scale getControl(Composite parent) {
		if (scale == null) {
			scale = new Scale(parent, SWT.NONE);
			scale.setFont(parent.getFont());
			switch (validateStrategy) {
			case VALIDATE_ON_KEY_STROKE:
				scale.addKeyListener(new KeyAdapter() {

					/*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.swt.events.KeyAdapter#keyReleased(org.eclipse.swt.events.KeyEvent)
					 */
					
					@Override
					public void keyReleased(KeyEvent e) {
						valueChanged();
					}
				});

				break;
			case VALIDATE_ON_FOCUS_LOST:
				scale.addKeyListener(new KeyAdapter() {
					
					@Override
					public void keyPressed(KeyEvent e) {
						clearErrorMessage();
					}
				});
				scale.addFocusListener(new FocusAdapter() {
					
					@Override
					public void focusGained(FocusEvent e) {
						refreshValidState();
					}

					
					@Override
					public void focusLost(FocusEvent e) {
						valueChanged();
						clearErrorMessage();
					}
				});
				break;
			default:
				Assert.isTrue(false, "Unknown validate strategy");//$NON-NLS-1$
			}
			scale.addDisposeListener(new DisposeListener() {
				
				public void widgetDisposed(DisposeEvent event) {
					scale = null;
				}
			});
		} else {
			checkParent(scale, parent);
		}
		return scale;
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	
	@Override
	public boolean isValid() {
		return isValid;
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	
	@Override
	protected void refreshValidState() {
		isValid = checkState();
	}

	/**
	 * Sets the error message that will be displayed when and if an error
	 * occurs.
	 * 
	 * @param message
	 *            the error message
	 */
	public void setErrorMessage(String message) {
		errorMessage = message;
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	
	@Override
	public void setFocus() {
		if (scale != null) {
			scale.setFocus();
		}
	}

	/**
	 * Sets this field editor's value.
	 * 
	 * @param value
	 *            the new value, or <code>null</code> meaning the empty string
	 */
	public void setIntValue(int value) {
		if (scale != null) {
			oldValue = scale.getSelection();
			if (oldValue != value) {
				scale.setSelection(value);
				valueChanged();
			}
		}
	}

	/**
	 * Sets the strategy for validating the text.
	 * <p>
	 * Calling this method has no effect after <code>createPartControl</code>
	 * is called. Thus this method is really only useful for subclasses to call
	 * in their constructor. However, it has public visibility for backward
	 * compatibility.
	 * </p>
	 * 
	 * @param value
	 *            either <code>VALIDATE_ON_KEY_STROKE</code> to perform on the
	 *            fly checking (the default), or
	 *            <code>VALIDATE_ON_FOCUS_LOST</code> to perform validation
	 *            only after the text has been typed in
	 */
	public void setValidateStrategy(int value) {
		Assert.isTrue(value == VALIDATE_ON_FOCUS_LOST
				|| value == VALIDATE_ON_KEY_STROKE);
		validateStrategy = value;
	}

	/**
	 * Shows the error message set via <code>setErrorMessage</code>.
	 */
	public void showErrorMessage() {
		showErrorMessage(errorMessage);
	}

	/**
	 * Informs this field editor's listener, if it has one, about a change to
	 * the value (<code>VALUE</code> property) provided that the old and new
	 * values are different.
	 * <p>
	 * This hook is <em>not</em> called when the text is initialized (or reset
	 * to the default value) from the preference store.
	 * </p>
	 */
	protected void valueChanged() {
		setPresentsDefaultValue(false);
		boolean oldState = isValid;
		refreshValidState();

		if (isValid != oldState)
			fireStateChanged(IS_VALID, oldState, isValid);

		int newValue = scale.getSelection();
		if (newValue != oldValue) {
			fireValueChanged(VALUE, String.valueOf(oldValue), String
					.valueOf(newValue));
			oldValue = newValue;
		}
	}

	/*
	 * @see FieldEditor.setEnabled(boolean,Composite).
	 */
	
	@Override
	public void setEnabled(boolean enabled, Composite parent) {
		super.setEnabled(enabled, parent);
		getControl(parent).setEnabled(enabled);
	}

	/**
	 * Sets the range of valid values for this field.
	 * 
	 * @param min
	 *            the minimum allowed value (inclusive)
	 * @param max
	 *            the maximum allowed value (inclusive)
	 */
	public void setValidRange(int min, int max) {
		if (scale != null) {
			scale.setMinimum(min);
			scale.setMaximum(max);
		}
	}
}
