package valoeghese.latex.api;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;

public enum OperatingSystem {
	WINDOWS() {
		@Override
		protected ProcessBuilder createShellCommand(String command) {
			return new ProcessBuilder("cmd", "/c", command);
		}
	},
	POSIX() {
		@Override
		protected ProcessBuilder createShellCommand(String command) {
			return new ProcessBuilder("bash", "-c", command);
		}
	};

	/**
	 * Execute the command from a command prompt on the system.
	 * @param command the command to execute.
	 * @param directory the working directory to execute in. Null if this doesn't matter.
	 * @return the process running the command.
	 */
	public Process executeCommand(String command, @Nullable Path directory) throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
		if (directory != null) processBuilder.directory(directory.toFile());

		return processBuilder.start();
	}

	protected ProcessBuilder createShellCommand(String command) {
		throw new UnsupportedOperationException("Cannot create shell command on this operating system currently.");
	}

	private static final OperatingSystem current = System.getProperty("os.name").toLowerCase().contains("win")
			? WINDOWS : POSIX;
	public static OperatingSystem get() {
		return current;
	}
}
