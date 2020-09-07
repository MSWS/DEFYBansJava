package xyz.msws.defybans.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FileSave implements Save {

	private File file;
	private List<Punishment> data = new ArrayList<>();

	public FileSave(File file) {
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
					data.add(new Punishment(s));
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
	public void queryPunishments(Callback<Collection<Punishment>> result) {
		result.execute(getPunishments());
	}

	/**
	 * @deprecated
	 */
	@Override
	public Collection<Punishment> getPunishments() {
		return data;
	}

}