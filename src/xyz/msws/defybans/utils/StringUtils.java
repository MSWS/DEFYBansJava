package xyz.msws.defybans.utils;

public class StringUtils {
	public static String toUpperCase(String str) {
		StringBuilder builder = new StringBuilder();
		for (String s : str.split(" ")) {
			if (s.isEmpty() || s.length() == 1)
				continue;
			builder.append(s.substring(0, 1).toUpperCase());
			builder.append(s.substring(1, s.length()).toLowerCase());
		}

		return builder.toString();
	}
}
