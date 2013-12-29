package com.github.mateuszwenus.pdf_merge;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

import javax.swing.JProgressBar;

import com.lowagie.text.pdf.PdfCopyFields;
import com.lowagie.text.pdf.PdfReader;

public class GeneratePdfTask extends SwingWorkerWithProgressBar<Void, Void> {

	private Object[] files;

	public GeneratePdfTask(Object[] files, JProgressBar progressBar, ResourceBundle resourceBundle) {
		super(progressBar, resourceBundle);
		this.files = files;
	}

	protected int getProgressBarMaximum() {
		return files.length;
	}

	protected Void doWork() throws Exception {
		OutputStream out = null;
		PdfCopyFields pdfCopy = null;
		try {
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
			out = new FileOutputStream("pdf-merge-" + df.format(new Date()) + ".pdf");
			pdfCopy = new PdfCopyFields(out);
			for (int i = 0; i < files.length; i++) {
				String path = String.valueOf(files[i]);
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
				setProgressBarValue(i + 1);
			}
			pdfCopy.close();
		} finally {
			closeQuietly(pdfCopy);
			closeQuietly(out);
		}
		return null;
	}

	private void closeQuietly(PdfCopyFields pdfCopy) {
		if (pdfCopy != null) {
			pdfCopy.close();
		}
	}

	private void closeQuietly(PdfReader reader) {
		if (reader != null) {
			reader.close();
		}
	}

	private void closeQuietly(Closeable obj) {
		if (obj != null) {
			try {
				obj.close();
			} catch (IOException e) {
			}
		}
	}
}
