package valoeghese.latex;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileTreeCellRenderer extends DefaultTreeCellRenderer {
	@Override
	public java.awt.Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

		// Customize the icons based on the node type (directory or file)
		if (value instanceof DefaultMutableTreeNode node) {
			if (node.getUserObject() instanceof FilePicker.FileTreeNode fileTreeNode) {
				Path file = fileTreeNode.getFile();

				if (Files.isDirectory(file)) {
					setIcon(UIManager.getIcon("FileView.directoryIcon"));
				} else {
					setIcon(UIManager.getIcon("FileView.fileIcon"));
				}
			}
		}

		return this;
	}
}
