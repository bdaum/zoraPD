/*
 * Copyright (c) 2005 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Matthew Hall - initial API and implementation
 */
package net.sf.paperclips;

import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;

/**
 * Deprecated class methods for printing documents--use the {@link PaperClips}
 * class instead.
 * 
 * @author Matthew Hall
 * @deprecated Create {@link PrintJob} instances, and print them with the
 *             {@link PaperClips#print(PrintJob, PrinterData)} method.
 */
public class PrintUtil {
	private PrintUtil() {
	}

	private static final String DEFAULT_JOB_NAME = "PaperClips printing"; //$NON-NLS-1$

	/**
	 * Prints the argument to the default printer with 1" margins. The Print's
	 * toString() result will be used as the print job name.
	 * 
	 * @param print
	 *            the item to print.
	 * @deprecated use {@link PaperClips#print(PrintJob, PrinterData)} instead.
	 */
	public static void print(Print print) {
		PaperClips.print(new PrintJob(DEFAULT_JOB_NAME, print), PaperClips
				.getDefaultPrinterData());
	}

	/**
	 * Prints the argument to the default printer. The Print's toString() result
	 * will be used as the print job name.
	 * 
	 * @param print
	 *            the item to print.
	 * @param margins
	 *            the page margins, in points.
	 * @deprecated use {@link PaperClips#print(PrintJob, PrinterData)} instead.
	 */
	public static void print(Print print, int margins) {
		PaperClips.print(new PrintJob(DEFAULT_JOB_NAME, print)
				.setMargins(new Margins(margins)), PaperClips
				.getDefaultPrinterData());
	}

	/**
	 * Prints the argument to the given printer with 1" margins. The Print's
	 * toString() result will be used as the print job name.
	 * 
	 * @param printer
	 *            the device to print on.
	 * @param print
	 *            the item to print.
	 * @deprecated Use {@link PaperClips#print(PrintJob, PrinterData)} instead.
	 */
	public static void printTo(Printer printer, Print print) {
		PaperClips.print(new PrintJob(DEFAULT_JOB_NAME, print), printer);
	}

	/**
	 * Prints the argument to the given printer. The Print's toString() result
	 * will be used as the print job name.
	 * 
	 * @param printer
	 *            the device to print on.
	 * @param print
	 *            the item to print.
	 * @param margins
	 *            the page margins, in points.
	 * @deprecated Use {@link PaperClips#print(PrintJob, PrinterData)} instead.
	 */
	public static void printTo(Printer printer, Print print, int margins) {
		PaperClips.print(new PrintJob(DEFAULT_JOB_NAME, print)
				.setMargins(margins), printer);
	}

	/**
	 * Prints the argument to the default printer with 1" margins.
	 * 
	 * @param jobName
	 *            the print job name.
	 * @param print
	 *            the item to print.
	 * @deprecated Use {@link PaperClips#print(PrintJob, PrinterData)} instead.
	 */
	public static void print(String jobName, Print print) {
		PaperClips.print(new PrintJob(jobName, print), PaperClips
				.getDefaultPrinterData());
	}

	/**
	 * Prints the argument to the default Printer.
	 * 
	 * @param jobName
	 *            the print job name.
	 * @param print
	 *            the item to print.
	 * @param margins
	 *            the page margins, in points. 72 pts = 1".
	 * @deprecated Use {@link PaperClips#print(PrintJob, PrinterData)} instead.
	 */
	public static void print(String jobName, Print print, int margins) {
		PaperClips.print(new PrintJob(jobName, print).setMargins(margins),
				PaperClips.getDefaultPrinterData());
	}

	/**
	 * Prints the argument to the given printer, with 1" margins.
	 * 
	 * @param jobName
	 *            the print job name.
	 * @param printerData
	 *            the printer to print to.
	 * @param print
	 *            the item to print.
	 * @deprecated Use {@link PaperClips#print(PrintJob, PrinterData)} instead.
	 */
	public static void printTo(String jobName, PrinterData printerData,
			Print print) {
		PaperClips.print(new PrintJob(jobName, print), printerData);
	}

	/**
	 * Prints the argument to the given printer.
	 * 
	 * @param jobName
	 *            the print job name.
	 * @param printerData
	 *            PrinterData of the printer to print to.
	 * @param print
	 *            the item to print.
	 * @param margins
	 *            the page margins, in points. 72 pts = 1".
	 * @deprecated Use {@link PaperClips#print(PrintJob, PrinterData)} instead.
	 */
	public static void printTo(String jobName, PrinterData printerData,
			Print print, int margins) {
		PrintJob job = new PrintJob(jobName, print);
		job.setMargins(margins);
		PaperClips.print(job, printerData);
	}

	/**
	 * Prints the argument to the given printer with 1" margins.
	 * 
	 * @param jobName
	 *            the print job name.
	 * @param printer
	 *            the device to print on.
	 * @param print
	 *            the item to print.
	 * @deprecated Use {@link PaperClips#print(PrintJob, PrinterData)} instead.
	 */
	public static void printTo(String jobName, Printer printer, Print print) {
		PaperClips.print(new PrintJob(jobName, print), printer);
	}

	/**
	 * Print the argument to the given Printer.
	 * 
	 * @param jobName
	 *            the print job name.
	 * @param printer
	 *            the device to print on.
	 * @param print
	 *            the item to print.
	 * @param margins
	 *            the page margins, in points. 72 pts = 1".
	 * @deprecated Use {@link PaperClips#print(PrintJob, PrinterData)} instead.
	 */
	public static void printTo(String jobName, Printer printer, Print print,
			int margins) {
		PaperClips.print(new PrintJob(jobName, print).setMargins(margins),
				printer);
	}
}