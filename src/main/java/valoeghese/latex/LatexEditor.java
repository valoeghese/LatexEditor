package valoeghese.latex;

import valoeghese.latex.api.FileContents;
import valoeghese.latex.api.Model;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class LatexEditor extends JScrollPane implements DocumentListener {
	public static final String FAKE_TAB = "    ";

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

		inputMap.put(KeyStroke.getKeyStroke("TAB"), "indentAction");
		actionMap.put("indentAction", new AbstractAction() {
			// TODO make tabs and space width an option
			@Override
			public void actionPerformed(ActionEvent e) {
				int end = LatexEditor.this.textArea.getSelectionEnd();
				int start = LatexEditor.this.textArea.getSelectionStart();

				if (end - start > 0) {
					try {
						start = LatexEditor.this.textArea.getLineStartOffset(LatexEditor.this.textArea.getLineOfOffset(start));
						end = LatexEditor.this.textArea.getLineStartOffset(LatexEditor.this.textArea.getLineOfOffset(end));

						StringBuilder result = new StringBuilder();

						if (end - start > 0) {
							String[] content = LatexEditor.this.textArea.getText(start, end - start).split("\\n");

							for (String s : content) {
								result.append(FAKE_TAB).append(s).append('\n');
							}
						}
						result.append(FAKE_TAB);

						LatexEditor.this.textArea.replaceRange(result.toString(), start, end);
					} catch (BadLocationException ex) {
						ex.printStackTrace();
					}
				} else {
					LatexEditor.this.textArea.replaceSelection(FAKE_TAB);
				}
			}
		});

		inputMap.put(KeyStroke.getKeyStroke("shift TAB"), "deindentAction");
		actionMap.put("deindentAction", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int end = LatexEditor.this.textArea.getSelectionEnd();
				int start = LatexEditor.this.textArea.getSelectionStart();

				try {
					start = LatexEditor.this.textArea.getLineStartOffset(LatexEditor.this.textArea.getLineOfOffset(start));
					end = LatexEditor.this.textArea.getLineEndOffset(LatexEditor.this.textArea.getLineOfOffset(end));

					StringBuilder result = new StringBuilder();

					if (end - start > 0) {
						String[] content = LatexEditor.this.textArea.getText(start, end - start).split("\\n");

						for (String s : content) {
							// Shrink Tab
							if (!s.isEmpty()) {
								// case tabs
								if (s.charAt(0) == '\t') {
									s = s.substring(1);
								}
								// case spaces
								else  {
									int i;
									int limit = Math.min(s.length(), FAKE_TAB.length());
									for (i = 0; i < limit; i++) {
										if (s.charAt(i) != ' ') {
											break;
										}
									}
									s = s.substring(i);
								}
							}

							result.append(s).append('\n');
						}
					}

					LatexEditor.this.textArea.replaceRange(result.toString(), start, end);
				} catch (BadLocationException ex) {
					ex.printStackTrace();
				}
			}
		});

		inputMap.put(KeyStroke.getKeyStroke("ENTER"), "newLineAction");
		actionMap.put("newLineAction", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JTextArea ta = LatexEditor.this.textArea;
				int caretPosition = ta.getCaretPosition();
				javax.swing.text.Element root = textArea.getDocument().getDefaultRootElement();
				javax.swing.text.Element line = root.getElement(root.getElementIndex(caretPosition));

				// read line
				int start = line.getStartOffset();
				int end = line.getEndOffset() - 1;
				String indent = "";

				try {
					String text = ta.getDocument().getText(start, end - start);
					StringBuilder indentBuilder = new StringBuilder();

					for (char c : text.toCharArray()) {
						if (!Character.isWhitespace(c)) break;
						indentBuilder.append(c);
					}

					indent = indentBuilder.toString();
				} catch (BadLocationException ex) {
					ex.printStackTrace();
				}

				ta.replaceSelection("\n" + indent);
			}
		});

		this.setContents(new EmptyContents());
	}

	/**
	 * Get the line of text at the given line number.
	 * @param lineNumber the zero-indexed line number.
	 * @return the line of text, or "" if an error occurs.
	 */
	private String getLineText(int lineNumber) {
		try {
			javax.swing.text.Element root = this.textArea.getDocument().getDefaultRootElement();
			javax.swing.text.Element lineElement = root.getElement(lineNumber);
			int start = lineElement.getStartOffset();
			int end = lineElement.getEndOffset() - 1;
			return this.textArea.getDocument().getText(start, end - start);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
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
