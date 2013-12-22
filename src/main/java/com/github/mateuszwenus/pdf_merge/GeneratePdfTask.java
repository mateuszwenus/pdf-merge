package com.github.mateuszwenus.pdf_merge;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.lowagie.text.pdf.PdfCopyFields;
import com.lowagie.text.pdf.PdfReader;

public class GeneratePdfTask extends SwingWorker<Void, Void> {

	private Object[] files;
	private JProgressBar progressBar;

	public GeneratePdfTask(Object[] files, JProgressBar progressBar) {
		super();
		this.files = files;
		this.progressBar = progressBar;
	}

	protected Void doInBackground() throws Exception {
		OutputStream out = null;
		String path = null;
		boolean success = false;
		try {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					progressBar.setValue(0);
					progressBar.setString("");
					progressBar.setMaximum(files.length);
				}
			});
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
			out = new FileOutputStream("pdf-merge-" + df.format(new Date()) + ".pdf");
			PdfCopyFields pdfCopy = new PdfCopyFields(out);
			for (int i = 0; i < files.length; i++) {
				path = String.valueOf(files[i]);
				InputStream in = null;
				PdfReader reader = null;
				try {
					in = new FileInputStream(path);
					reader = new PdfReader(in);
					pdfCopy.addDocument(reader);
				} finally {
					closeQuietly(reader);
					closeQuietly(in);
				}
				final int value = i + 1;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						progressBar.setValue(value);
						progressBar.setString(value + "/" + files.length);
					}
				});
			}
			pdfCopy.close();
			success = true;
		} catch (final Exception e) {
			final String errorFile = path;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					progressBar.setValue(files.length);
					progressBar.setString("Błąd (" + errorFile + "): " + e.getMessage());
				}
			});
		} finally {
			if (success) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						progressBar.setString("Gotowe");
					}
				});
			}
			closeQuietly(out);
		}
		return null;
	}

	private static void closeQuietly(PdfReader reader) {
		if (reader != null) {
			reader.close();
		}
	}

	private static void closeQuietly(Closeable obj) {
		if (obj != null) {
			try {
				obj.close();
			} catch (IOException e) {
			}
		}
	}
}
