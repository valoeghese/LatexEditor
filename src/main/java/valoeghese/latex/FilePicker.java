package valoeghese.latex;

import javax.swing.*;
import java.nio.file.Path;

public class FilePicker extends JScrollPane {
	public FilePicker(Path root) {
		this.root = root;
	}

	private final Path root;
}
