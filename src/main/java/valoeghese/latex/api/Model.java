package valoeghese.latex.api;

import valoeghese.latex.LatexViewer;
import valoeghese.latex.impl.ErrorContents;

import java.io.*;
import java.util.stream.Collectors;

public class Model {
	private final LatexViewer viewer = new LatexViewer();

	/**
	 * Open the file and receive its contents.
	 * @param file the file to read from.
	 * @return the contents of the file. Will return a {@linkplain FileContents#isWritable() non-writable} file contents
	 * if an error occurs.
	 */
	public FileContents openFile(File file) {
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			return new Contents(
					file,
					reader.lines()
							.collect(Collectors.joining(System.lineSeparator()))
			);
		} catch (IOException e) {
			e.printStackTrace();
			return new ErrorContents(file, e);
		}
	}

	private boolean saveFile(File file, String string) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			writer.write(string);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public LatexViewer getViewer() {
		return viewer;
	}

	private class Contents implements FileContents {
		public Contents(File file, String string) {
			this.file = file;
			this.text = string;
		}

		private String text;
		private final File file;

		@Override
		public String getText() {
			return this.text;
		}

		@Override
		public void writeText(String text) {
			if (Model.this.saveFile(this.file, text)) {
				this.text = text;
			}
		}

		@Override
		public boolean isWritable() {
			return true;
		}
	}
}
