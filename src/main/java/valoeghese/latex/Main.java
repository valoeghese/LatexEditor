package valoeghese.latex;

import valoeghese.latex.api.Model;
import valoeghese.latex.api.PromptResponse;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
		Model model = new Model(frame);
		JSplitPane master = (JSplitPane) frame.add(new Main(model));
		master.setDividerLocation(150);

		/*
		Runtime cmd = Runtime.getRuntime();
		cmd.exec("pdflatex ");
		 */
		frame.setSize(new Dimension(1080, 720));
		frame.setMinimumSize(new Dimension(360, 240));
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (model.promptSaveBeforeAction()) {
					frame.dispose();
				}
			}
		});

		frame.setVisible(true);
	}
}