package xyz.msws.defybans.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;

import xyz.msws.defybans.data.punishment.Punishment;

public class FileStorage implements PunishmentStorage {

	private File file;
	private Set<Punishment> data = new HashSet<>();

	public FileStorage(File file) {
		this.file = file;
	}

	@Override
	public void save() {
		try {
			FileWriter writer = new FileWriter(file);

			data.forEach(p -> {
				try {
					writer.write(p.toString() + "\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			});

			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void load() {
		try {
			FileReader reader = new FileReader(file);
			BufferedReader breader = new BufferedReader(reader);
			String s;
			try {
				while ((s = breader.readLine()) != null) {
					if (s.isEmpty())
						continue;
					try {
						data.add(new Punishment(s));
					} catch (JSONException e) {
						System.out.println("Input: " + s);
						e.printStackTrace();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void addPunishment(Punishment p, boolean save) {
		data.add(p);
		if (save)
			save();
	}

	@Override
	public void queryPunishments(Callback<Set<Punishment>> result) {
		result.execute(getPunishments());
	}

	/**
	 * @deprecated
	 */
	@Override
	public Set<Punishment> getPunishments() {
		return data;
	}

	@Override
	public void deletePunishment(Punishment p) {
		data.remove(p);
	}

}
