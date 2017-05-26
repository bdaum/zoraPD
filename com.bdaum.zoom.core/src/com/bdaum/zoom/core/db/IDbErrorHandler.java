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
 * (c) 2009-2014 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.core.db;

import java.io.File;
import java.net.URI;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.swt.graphics.Image;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.internal.operations.ImportConfiguration;
import com.bdaum.zoom.program.IRawConverter;

public interface IDbErrorHandler {

	/**
	 * Warns user that connection to database is lost
	 *
	 * @param title
	 *            - title text
	 * @param message
	 *            - error message
	 * @param adaptable
	 *            - an adaptable that may or may not provide a Shell instance.
	 *            Maybe null.
	 */
	void connectionLostWarning(String title, String message,
			IAdaptable adaptable);

	/**
	 * Warns user about fatal database error and exits
	 *
	 * @param title
	 *            - title text
	 * @param message
	 *            - error message
	 * @param adaptable
	 *            - an adaptable that may or may not provide a Shell instance.
	 *            Maybe null.
	 */
	void fatalError(String title, String message, IAdaptable adaptable);

	/**
	 * Informs user about invalid image files and prompts for action
	 *
	 * @param title
	 *            - title
	 * @param message
	 *            - message
	 * @param uri
	 *            - invalid file uri
	 * @param monitor
	 *            - import progress monitor
	 * @param adaptable
	 *            - an adaptable that may or may not provide a Shell instance.
	 *            Maybe null.
	 */
	void invalidFile(final String title, final String message, final URI uri,
			final IProgressMonitor monitor, final IAdaptable adaptable);

	/**
	 * Warns user about fatal database error and exits
	 *
	 * @param title
	 *            - title text
	 * @param message
	 *            - error message
	 * @param validator
	 *            - validator for redirect inputs
	 * @param adaptable
	 *            - an adaptable that may or may not provide a Shell instance.
	 *            Maybe null.
	 */
	void promptForReconnect(String title, String message,
			IInputValidator validator, IAdaptable adaptable);

	/**
	 * Displays a non fatal error as a dialog
	 *
	 * @param title
	 *            - title text
	 * @param message
	 *            - error message
	 * @param adaptable
	 *            - an adaptable that may or may not provide a Shell instance.
	 *            Maybe null.
	 */
	void showError(String title, String message, IAdaptable adaptable);

	/**
	 * Displays a non fatal error as a dialog
	 *
	 * @param title
	 *            - title text
	 * @param message
	 *            - error message
	 * @param adaptable
	 *            - an adaptable that may or may not provide a Shell instance.
	 *            Maybe null.
	 */
	void showWarning(String title, String message, IAdaptable adaptable);

	/**
	 * Displays a non fatal error as a dialog
	 *
	 * @param title
	 *            - title text
	 * @param message
	 *            - error message
	 * @param adaptable
	 *            - an adaptable that may or may not provide a Shell instance.
	 *            Maybe null.
	 */
	void showInformation(String title, String message, IAdaptable adaptable);

	/**
	 * Displays a non fatal error as a dialog
	 *
	 * @param title
	 *            - title text
	 * @param message
	 *            - error message
	 * @param adaptable
	 *            - an adaptable that may or may not provide a Shell instance.
	 *            Maybe null.
	 * @param validator
	 *            - when the validator is executed and returns true, the dialog
	 *            closes
	 */
	void showInformation(String title, String message, IAdaptable adaptable,
			IValidator validator);

	/**
	 * Displays a non fatal error as a dialog
	 *
	 * @param title
	 *            - title text
	 * @param message
	 *            - error message
	 * @return true if question is answered positively
	 * @param adaptable
	 *            - an adaptable that may or may not provide a Shell instance.
	 *            Maybe null.
	 */
	boolean question(String title, String message, IAdaptable adaptable);

	/**
	 * Displays a custom message dialog and returns the result
	 *
	 * @param dialogTitle
	 *            - title line text
	 * @param dialogTitleImage
	 *            - title image or null
	 * @param dialogMessage
	 *            - dialog message
	 * @param dialogImageType
	 *            - dialog image type
	 * @param dialogButtonLabels
	 *            - button labels
	 * @param defaultIndex
	 *            - index of default button
	 * @param adaptable
	 *            - an adaptable that may provide a Shell instance. Maybe null.
	 * @return the index of the button pressed
	 */
	int showMessageDialog(String dialogTitle, Image dialogTitleImage,
			String dialogMessage, int dialogImageType,
			String[] dialogButtonLabels, int defaultIndex, IAdaptable adaptable);

	/**
	 * Plays a sound signal
	 *
	 * @param soundfile
	 *            - name of sound
	 */
	void alarmOnPrompt(String sound);

	/**
	 * Plays a sound signal
	 *
	 * @param soundfile
	 *            - name of sound
	 */
	void signalEOJ(String sound);

	/**
	 * @param dialog
	 *            - the dialog to execute
	 * @param adaptable
	 *            - an adaptable that may provide a Shell instance
	 * @return dialog result
	 */
	/**
	 * @param title
	 *            - dialog title
	 * @param message
	 *            - dialog message
	 * @param asset
	 *            - existing asset
	 * @param currentConfig
	 *            - current import configuration
	 * @param multi
	 *            - true for imports with a cardinality greater one
	 * @param adaptable
	 *            - an adaptable that may provide a Shell instance. Maybe null.
	 * @return new or updated importConfiguration or null
	 */
	ImportConfiguration showConflictDialog(String title, String message,
			Asset asset, ImportConfiguration currentConfig, boolean multi,
			IAdaptable adaptable);

	/**
	 * Shows the dialog for defining the location of the DNG converter
	 *
	 * @param dngLocation
	 *            - current location of the converter
	 * @param adaptable
	 *            - an adaptable that may provide a Shell instance. Maybe null.
	 * @return new location or null
	 */
	File showDngDialog(File dngLocation, IAdaptable adaptable);

	/**
	 * Shows the dialog for defining the location of the RAW converter
	 *
	 * @param adaptable
	 *            - an adaptable that may provide a Shell instance. Maybe null.
	 * @return new rawconverter or null
	 */
	IRawConverter showRawDialog(IAdaptable info);

}
