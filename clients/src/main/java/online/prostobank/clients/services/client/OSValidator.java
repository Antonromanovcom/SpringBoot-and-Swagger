package online.prostobank.clients.services.client;

public class OSValidator {

	private static String OS = System.getProperty("os.name").toLowerCase();

	boolean isOSWin() {
		return isWindows();
	}

	private static boolean isWindows() {
		return (OS.contains("win"));
	}

	private static boolean isMac() {
		return (OS.contains("mac"));
	}

	private static boolean isUnix() {
		return (OS.contains("nix") || OS.contains("nux") || OS.indexOf("aix") > 0);
	}

	private static boolean isSolaris() {
		return (OS.contains("sunos"));
	}

}
