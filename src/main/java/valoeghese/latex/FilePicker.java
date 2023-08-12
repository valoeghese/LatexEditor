package valoeghese.latex;

import valoeghese.latex.api.Model;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class FilePicker extends JScrollPane {
	public FilePicker(Model model, Path root) {

		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new FileTreeNode(root));
		this.populateChildren(rootNode);

		JTree tree = new JTree(rootNode);
		tree.setCellRenderer(new FileTreeCellRenderer());
		tree.setRootVisible(true);

		tree.addTreeWillExpandListener(new TreeWillExpandListener() {
			@Override
			public void treeWillExpand(TreeExpansionEvent event) {
				DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();

				for (int i = 0; i < parentNode.getChildCount(); i++) {
					DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) parentNode.getChildAt(i);
					FilePicker.this.populateChildren(childNode);
				}
			}

			@Override
			public void treeWillCollapse(TreeExpansionEvent event) {
				// No action needed here
			}
		});

		//TODo reload file tree when opening folder
		tree.addTreeSelectionListener(e -> {
			if (e.getPath().getLastPathComponent() instanceof DefaultMutableTreeNode treeNode) {
				if (treeNode.getUserObject() instanceof FileTreeNode node) {
					if (Files.isRegularFile(node.getFile())) {
						String fileName = node.getFile().getFileName().toString();
						int dotLoc = fileName.lastIndexOf(".");
						String extension = dotLoc == -1 ? "" : fileName.substring(dotLoc);

						if (canOpen(extension)) {
							System.out.println("Opening " + node.getFile());
							model.open(node.getFile());
						} else {
							String[] options = {"Ok", "Open Anyway"};

							int result = JOptionPane.showOptionDialog(null,
									"Only supports files of type " + EXTENSIONS.stream().collect(Collectors.joining(", ")),
									"Unsupported File Type",
									JOptionPane.DEFAULT_OPTION,
									JOptionPane.INFORMATION_MESSAGE,
									null,
									options,
									options[0]
							);

							if (result == 1) {
								// this is on the user!
								System.out.println("Opening " + node.getFile() + " anyway.");
								model.open(node.getFile());
							}
						}
					}
				}
			}
		});

		// populate children
		tree.collapsePath(new TreePath(rootNode.getPath()));
		tree.expandPath(new TreePath(rootNode.getPath()));

		this.setViewportView(tree);
	}

	private void populateChildren(DefaultMutableTreeNode node) {
		FileTreeNode fileTreeNode = (FileTreeNode) node.getUserObject();
		Path file = fileTreeNode.getFile();

		if (Files.isDirectory(file)) {
			try(var fStream = Files.list(file)) {
				fStream.forEach(cFile -> {
					DefaultMutableTreeNode child = new DefaultMutableTreeNode(new FileTreeNode(cFile));
					node.add(child);
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static final List<String> EXTENSIONS = List.of(".tex", ".txt", ".log", ".xml");

	private static boolean canOpen(String extension) {
		return EXTENSIONS.contains(extension);
	}

	static class FileTreeNode {
		private Path file;

		public FileTreeNode(Path file) {
			this.file = file;
		}

		@Override
		public String toString() {
			return this.file.getFileName().toString();
		}

		public Path getFile() {
			return this.file;
		}
	}
}
