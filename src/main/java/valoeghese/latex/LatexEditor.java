package valoeghese.latex;

import valoeghese.latex.api.FileContents;
import valoeghese.latex.api.Model;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class LatexEditor extends JScrollPane implements DocumentListener {
	public LatexEditor(Model model) {
		this.model = model;
		this.textArea = new JTextArea();
		this.setViewportView(this.textArea);

		this.textArea.getDocument().addDocumentListener(this);

		// Set up Ctrl+S key binding for save action
		InputMap inputMap = this.textArea.getInputMap();
		ActionMap actionMap = this.textArea.getActionMap();

		KeyStroke saveKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
		inputMap.put(saveKeyStroke, "saveAction");
		actionMap.put("saveAction", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				LatexEditor.this.saveFile();
			}
		});

		this.setContents(new EmptyContents());
	}

	private final JTextArea textArea;
	private final Model model;

	private FileContents contents;

	public void setContents(FileContents contents) {
		this.textArea.setText(contents.getText());
		this.textArea.setEditable(contents.isWritable());
		this.contents = contents;

		// upon loading a new document it is not yet edited
		// also important because setting the text above modifies the document saved via our event listener.
		this.model.setDocumentSaved(true);
	}

	/**
	 * Save the file. If the file fails to save, the caller is notified by returning false.
	 * @return whether the file could be saved.
	 */
	public boolean saveFile() {
		System.out.println("Saving...");

		if (LatexEditor.this.contents.writeText(LatexEditor.this.textArea.getText())) {
			System.out.println("Saved.");
			LatexEditor.this.model.setDocumentSaved(true);
			return true;
		}

		System.out.println("Failed to Save File.");

		return false;
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		model.setDocumentSaved(false);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		model.setDocumentSaved(false);
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		// No-Op
	}

	private static class EmptyContents implements FileContents {
		@Override
		public String getText() {
			return "No File Loaded.";
		}

		@Override
		public boolean writeText(String text) {
			// No-Write
			return false;
		}

		@Override
		public boolean isWritable() {
			return false;
		}
	}
}
