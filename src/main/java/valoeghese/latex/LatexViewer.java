package valoeghese.latex;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rendered Latex Document Viewer.
 */
public class LatexViewer extends JScrollPane {
	public LatexViewer() {
		this.loadRender(Path.of("gibberish.unsupportedfile"));
	}

	// incremented to make async code ignore scrapped renders
	private final AtomicInteger fileIdCounter = new AtomicInteger(0);

	/**
	 * Render and view the output of the given source file.
	 * @param sourceFile the source file to render.
	 */
	public void render(Path sourceFile) {
		if (this.accepts(sourceFile)) {
			final int thisRenderId = this.loadMessage("Rendering Document...");

			CompletableFuture.supplyAsync(() -> {
				System.out.println("Starting Render Job...");

				try {
					// TODO dont require bash
					ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", "pdflatex " + sourceFile.toAbsolutePath());
					processBuilder.directory(sourceFile.getParent().toFile()); // Set the working directory
					Process process = processBuilder.start();

					// dummy inputs
					OutputStream outputStream = process.getOutputStream();
					String dummyInput = "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n";
					outputStream.write(dummyInput.getBytes());
					outputStream.flush();
					outputStream.close();

					// TODO pipe this into somewhere? like a status message under rendering| process.getInputStream()

					return process.waitFor();
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();

					JOptionPane.showMessageDialog(null,
							"Rendering document failed. " + e.getClass().getSimpleName() + ": " + e.getMessage(),
							"Error",
							JOptionPane.ERROR_MESSAGE);

					return -1;
				}
			}).thenAcceptAsync(responseCode -> {
				if (responseCode == -1) {
					System.out.println("Failed Render Job " + thisRenderId);
				}
				else {
					System.out.println("Completed Render Job " + thisRenderId);
				}

				// if another render is being done this render is scrapped. Either another file is being used or
				if (this.fileIdCounter.get() == thisRenderId) {
					if (responseCode == -1) {
						this.loadMessage("Error rendering file.");
					} else {
						this.loadRender(sourceFile);
					}
				}
			});
		} else {
			this.loadRender(sourceFile);
		}
	}

	/**
	 * Load the pdf for the given source file for viewing.
	 * @param sourceFile the source file to view the latex output of.
	 * @return this render id, if rendering.
	 */
	public int loadRender(Path sourceFile) {
		if (this.accepts(sourceFile)) {
			// Load PDF
			String pdfPath = sourceFile.toString();
			pdfPath = pdfPath.substring(0, pdfPath.lastIndexOf("."));
			pdfPath += ".pdf";
			System.out.println("Load PDF " + pdfPath);
			Path pdfPathObj = Path.of(pdfPath);

			// If no render, render the file.
			// This could cause looping problems hehe
			if (!Files.isRegularFile(pdfPathObj)) {
				int result = this.fileIdCounter.incrementAndGet(); // see below
				this.render(sourceFile);
				return result;
			}

			// Render (TODO async?)
			List<BufferedImage> pages = new ArrayList<>();

			try {
				PDDocument document = PDDocument.load(new File(pdfPath));
				PDFRenderer pdfRenderer = new PDFRenderer(document);

				for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
					BufferedImage image = pdfRenderer.renderImageWithDPI(pageIndex, 300);
					pages.add(image);
				}

				document.close();
			} catch (IOException e) {
				e.printStackTrace();
				return this.loadMessage("Error loading PDF file. "
						+ e.getClass().getSimpleName() + ": " + e.getMessage());
			}

			System.out.println("Rendered Document. Now Adding Pages...");

			// Show pages
			JPanel container = new JPanel();
			container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

			for (BufferedImage page : pages) {
				container.add(new ImagePanel(page));
			}

			this.setViewportView(container);
			this.revalidate();
			this.repaint();

			// opening files should scrap old renders
			//this has the side effect that if you render and then click on the file again it will probably fail to load the render.
			// Or change file there and back once we stop double-opening.
			return this.fileIdCounter.incrementAndGet();
		} else {
			// see above
			return this.loadMessage("No TeX file Loaded.");
		}
	}

	private int loadMessage(String message) {
		JPanel panel = new JPanel(new GridBagLayout());

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.anchor = GridBagConstraints.CENTER;

		panel.add(new JLabel(message), constraints);
		this.setViewportView(panel);

		// see above
		return this.fileIdCounter.incrementAndGet();
	}

	private boolean accepts(Path file) {
		return file.getFileName().toString().endsWith(".tex");
	}

	private static class ImagePanel extends JPanel {
		public ImagePanel(BufferedImage image) {
			this.image = image;
		}

		private final BufferedImage image;

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			int panelWidth = this.getWidth();
			int imageWidth = image.getWidth();
			int imageHeight = image.getHeight();

			double newHeight = imageHeight * ((double)panelWidth/imageWidth);

			this.setPreferredSize(new Dimension(panelWidth, (int)newHeight));
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

			g.drawImage(this.image, 0, 0, panelWidth, (int) newHeight, null);
		}
	}
}
