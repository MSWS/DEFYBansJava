package xyz.msws.defybans;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Main {

	public static void main(String[] args) {
		File tokenFile = new File(System.getProperty("user.dir") + File.separator + "token.txt");
		if (!tokenFile.exists()) {
			try {
				tokenFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}

		FileReader fr = null;
		try {
			fr = new FileReader(tokenFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		BufferedReader reader = new BufferedReader(fr);

		String token = null;
		try {
			token = reader.readLine();
			reader.close();
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		try {
			reader.close();
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		DEFYBansClient client = new DEFYBansClient(token);
		client.start();
	}
}
