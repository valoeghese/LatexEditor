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
		if (this.promptSaveBeforeAction()) {
			this._open(path);
		}
	}

	private void _open(Path path) {
		this.editor.setContents(this.readFile(path));
		this.viewer.loadRender(path);
	}

	/**
	 * If the document is unsaved, prompt the user to save before continuing with the action.
	 * The user will have the options "Save", "Don't Save", and "Cancel".
	 * @return whether the action should continue. N.B. if the user selected "Save" and the document
	 * failed to save, the action should not continue.
	 */
	public boolean promptSaveBeforeAction() {
		// only need to prompt if document not already saved
		if (!this.documentSaved) {
			PromptResponse response = this.showSavePrompt();

			if (response == PromptResponse.YES) {
				// if the save failed, don't run the action, because user wanted to save!
				if (!this.editor.saveFile()) {
					return false;
				}
			}

			// if the user cancelled, don't run the action
			if (response == PromptResponse.CANCEL) {
				return false;
			}
		}

		return true;
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
		try {
			try (BufferedWriter writer = Files.newBufferedWriter(path)) {
				writer.write(string);
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				this.showSaveFailedMessage(e);
				return false;
			}
		} finally {
			// implicit finally block of previous try needs to run first to close resources.
			this.viewer.render(path);
		}
	}

	private void showSaveFailedMessage(Exception e) {
		JOptionPane.showMessageDialog(null,
				"Document failed to save. " + e.getClass().getSimpleName() + ": " + e.getMessage(),
				"Error",
				JOptionPane.ERROR_MESSAGE);
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
