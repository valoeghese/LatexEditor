package valoeghese.latex;

import valoeghese.latex.api.Model;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;

public class Main extends JSplitPane {
	public Main(Model model) {
		this.setLeftComponent(new FilePicker(model, Path.of(System.getProperty("user.dir"))));

		JPanel mainArea = new JPanel(new GridLayout(1, 2));
		mainArea.add(model.getEditor());
		mainArea.add(model.getViewer());

		this.setRightComponent(mainArea);
	}

	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

//		if ("Linux".equals(System.getProperty("os.name"))) {
//			// Try GTK Look and Feel
//			try {
//				UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
//			} catch (Exception e) {
//				System.out.println("Could not find GTK LaF on Linux instance. Skipping...");
//			}
//		}

		JFrame frame = new JFrame("Latex Editor");
		JSplitPane master = (JSplitPane) frame.add(new Main(new Model()));
		master.setDividerLocation(150);

		/*
		Runtime cmd = Runtime.getRuntime();
		cmd.exec("pdflatex ");
		 */
		frame.setSize(new Dimension(1080, 720));
		frame.setMinimumSize(new Dimension(360, 240));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}