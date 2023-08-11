package valoeghese.latex.impl;

import valoeghese.latex.api.FileContents;

import java.io.File;
import java.io.IOException;

public record ErrorContents(String message) implements FileContents {
	public ErrorContents(File file, IOException e) {
		this("Failed to open " + file + ". " + e.getClass().getSimpleName() + ": " + e.getMessage());
	}

	@Override
	public String getText() {
		return this.message;
	}

	@Override
	public void writeText(String text) {
		// No-Write.
	}

	@Override
	public boolean isWritable() {
		return false;
	}
}
