package valoeghese.latex.api;

import valoeghese.latex.LatexEditor;
import valoeghese.latex.LatexViewer;
import valoeghese.latex.impl.ErrorContents;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class Model {
	public Model(JFrame frame) {
		this.frame = frame;
		this.defaultName = frame.getTitle();
	}

	private final JFrame frame;
	private final String defaultName;

	private final LatexViewer viewer = new LatexViewer();
	private final LatexEditor editor = new LatexEditor(this);
	private boolean documentSaved = true;

	/**
	 * Open the file in the editor. If progress is currently unsaved, the user will be prompted to save.
	 * @param path the file to open.
	 */
	public void open(Path path) {
		if (this.documentSaved) {
			this._open(path);
		} else {
			PromptResponse response = this.showSavePrompt();

			if (response == PromptResponse.YES) {
				if (!this.editor.saveFile()) {
					this.showSaveFailedMessage();
					return; // early return is cleaner here
				}
			}

			if (response != PromptResponse.CANCEL) {
				this._open(path);
			}
		}
	}

	private void _open(Path path) {
		this.editor.setContents(this.readFile(path));
		this.viewer.loadRender(path);
	}

	private PromptResponse showSavePrompt() {
		String[] options = { "Save", "Don't Save", "Cancel" };

		return PromptResponse.values()[
				JOptionPane.showOptionDialog(null,
						"If you don't save, all unsaved changes will be lost!",
						"Save Changes?",
						JOptionPane.DEFAULT_OPTION,
						JOptionPane.WARNING_MESSAGE,
						null,
						options,
						options[0])
				];
	}

	private void showSaveFailedMessage() {
		JOptionPane.showMessageDialog(null,
				"Document failed to save.",
				"Error",
				JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Set whether the current document is saved. That is, the in-memory contents are the same as (the known contents)
	 * in the file.
	 * This amends the title of the window accordingly.
	 * @param saved whether the document is saved.
	 */
	public void setDocumentSaved(boolean saved) {
		if (this.frame != null) {
			this.frame.setTitle(this.defaultName + (saved ? "" : " *"));
		}

		this.documentSaved = saved;
	}

	/**
	 * Open the file and receive its contents.
	 * @param path the file to read from.
	 * @return the contents of the file. Will return a {@linkplain FileContents#isWritable() non-writable} file contents
	 * if an error occurs.
	 */
	private FileContents readFile(Path path) {
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
			this.viewer.render(path);
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
		public boolean writeText(String text) {
			if (Model.this.saveFile(this.path, text)) {
				this.text = text;
				return true;
			}

			return false;
		}

		@Override
		public boolean isWritable() {
			return true;
		}
	}
}
