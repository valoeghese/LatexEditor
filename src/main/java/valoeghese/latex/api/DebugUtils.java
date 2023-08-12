package valoeghese.latex.api;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class DebugUtils {
	public static void writeImageToFile(BufferedImage image, String filePath) {
		try {
			File output = new File(filePath);
			if (!output.getParentFile().exists()) {
				output.getParentFile().mkdirs();
			}

			ImageIO.write(image, "png", output);
			System.out.println("Image saved successfully to: " + output.getAbsolutePath());
		} catch (IOException e) {
			System.err.println("Error while saving the image: " + e.getMessage());
		}
	}
}
