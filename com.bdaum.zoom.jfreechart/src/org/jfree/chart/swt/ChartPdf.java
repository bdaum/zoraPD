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
 * (c) 2017 Berthold Daum  (berthold.daum@bdaum.de)
 */
package org.jfree.chart.swt;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jfree.chart.JFreeChart;

import com.bdaum.zoom.core.Constants;
import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

public class ChartPdf {

	public static void saveChartAsPDF(File file, JFreeChart chart, int width, int height)
			throws DocumentException, FileNotFoundException, IOException {
		if (chart != null) {
			boolean success = false;
			String old = null;
			File oldFile = null;
			boolean append = file.exists();
			if (append) {
				old = file.getAbsolutePath() + ".old"; //$NON-NLS-1$
				oldFile = new File(old);
				oldFile.delete();
				file.renameTo(oldFile);
			}
			try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
				// convert chart to PDF with iText:
				Rectangle pagesize = new Rectangle(width, height);
				if (append) {
					PdfReader reader = new PdfReader(old);
					PdfStamper stamper = new PdfStamper(reader, out);
					try {
						int n = reader.getNumberOfPages() + 1;
						stamper.insertPage(n, pagesize);
						PdfContentByte overContent = stamper.getOverContent(n);
						writeChart(chart, width, height, overContent);
						ColumnText ct = new ColumnText(overContent);
						ct.setSimpleColumn(width - 50, 50, width - 12, height, 150, Element.ALIGN_RIGHT);
						Paragraph paragraph = new Paragraph(String.valueOf(n),
								new Font(FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.DARK_GRAY));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						ct.addElement(paragraph);
						ct.go();
						success = true;
					} finally {
						stamper.close();
						reader.close();
						oldFile.delete();
					}
				} else {
					Document document = new Document(pagesize, 50, 50, 50, 50);
					document.addCreationDate();
					document.addCreator(Constants.APPLICATION_NAME);
					document.addAuthor(System.getProperty("user.name")); //$NON-NLS-1$
					try {
						PdfWriter writer = PdfWriter.getInstance(document, out);
						document.open();
						writeChart(chart, width, height, writer.getDirectContent());
						success = true;
					} finally {
						document.close();
					}
				}
			}
			if (!success) {
				file.delete();
				if (oldFile != null)
					oldFile.renameTo(file);
			}
		}
	}

	protected static void writeChart(JFreeChart chart, int width, int height, PdfContentByte cb) {
		PdfTemplate tp = cb.createTemplate(width, height);
		Graphics2D g2 = new PdfGraphics2D(cb, width, height);
		Rectangle2D r2D = new Rectangle2D.Double(0, 0, width, height);
		chart.draw(g2, r2D, null);
		g2.dispose();
		cb.addTemplate(tp, 0, 0);
	}

}
