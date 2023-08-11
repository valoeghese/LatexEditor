package valoeghese.latex;

import valoeghese.latex.api.FileContents;

import javax.swing.*;
import java.awt.event.ActionEvent;

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

			}
		});
	}

	private JTextArea textArea;
	private FileContents contents = new EmptyContents();

	public void setContents(FileContents contents) {
		this.textArea.setText(contents.getText());
		this.contents = contents;
	}

	private static class EmptyContents implements FileContents {
		@Override
		public String getText() {
			return "No File Loaded."
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
