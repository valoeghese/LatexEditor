package valoeghese.latex.api;

import valoeghese.latex.LatexEditor;
import valoeghese.latex.LatexViewer;
import valoeghese.latex.impl.ErrorContents;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class Model {
	private final LatexViewer viewer = new LatexViewer(this);
	private final LatexEditor editor = new LatexEditor();

	/**
	 * Open the file in the editor.
	 * @param path the file to open.
	 */
	public void open(Path path) {
		this.editor.setContents(this.openFile(path));
	}

	/**
	 * Open the file and receive its contents.
	 * @param path the file to read from.
	 * @return the contents of the file. Will return a {@linkplain FileContents#isWritable() non-writable} file contents
	 * if an error occurs.
	 */
	private FileContents openFile(Path path) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(path)))) {
			return new Contents(
					path,
					reader.lines()
							.collect(Collectors.joining(System.lineSeparator()))
			);
		} catch (IOException e) {
			e.printStackTrace();
			return new ErrorContents(path, e);
		}
	}

	private boolean saveFile(Path path, String string) {
		try (BufferedWriter writer = Files.newBufferedWriter(path)) {
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

	public LatexEditor getEditor() {
		return editor;
	}

	private class Contents implements FileContents {
		public Contents(Path path, String string) {
			this.path = path;
			this.text = string;
		}

		private String text;
		private final Path path;

		@Override
		public String getText() {
			return this.text;
		}

		@Override
		public void writeText(String text) {
			if (Model.this.saveFile(this.path, text)) {
				this.text = text;
			}
		}

		@Override
		public boolean isWritable() {
			return true;
		}
	}
}
