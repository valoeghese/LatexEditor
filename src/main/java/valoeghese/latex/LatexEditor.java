package valoeghese.latex;

import valoeghese.latex.api.FileContents;
import valoeghese.latex.api.Model;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class LatexEditor extends JScrollPane {
	public LatexEditor() {
		this.textArea = new JTextArea();
		this.setViewportView(this.textArea);

		// Set up Ctrl+S key binding for save action
		InputMap inputMap = this.textArea.getInputMap();
		ActionMap actionMap = this.textArea.getActionMap();

		KeyStroke saveKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		inputMap.put(saveKeyStroke, "saveAction");
		actionMap.put("saveAction", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				LatexEditor.this.contents.writeText(LatexEditor.this.textArea.getText());
			}
		});

		this.setContents(new EmptyContents());
	}

	private JTextArea textArea;
	private FileContents contents;

	public void setContents(FileContents contents) {
		this.textArea.setText(contents.getText());
		this.textArea.setEditable(contents.isWritable());
		this.contents = contents;
	}

	private static class EmptyContents implements FileContents {
		@Override
		public String getText() {
			return "No File Loaded.";
		}

		@Override
		public void writeText(String text) {
			// No-Write
		}

		@Override
		public boolean isWritable() {
			return false;
		}
	}
}
