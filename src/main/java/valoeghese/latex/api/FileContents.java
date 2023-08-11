package valoeghese.latex.api;

import java.io.IOException;

public interface FileContents {
	/**
	 * Get the text of this file.
	 * @return the text stored in this file.
	 */
	String getText();

	/**
	 * Write the text to the file. Will do nothing if not writable.
	 * @return whether the text was successfully written to the file.
	 */
	boolean writeText(String text);

	/**
	 * Get whether this file contents is writable. Will be false if it is an error.
	 * @return whether this file's contents can be written to.
	 */
	boolean isWritable();
}
