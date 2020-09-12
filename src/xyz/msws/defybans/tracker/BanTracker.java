package xyz.msws.defybans.tracker;

import java.io.IOException;
import java.util.List;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import xyz.msws.defybans.data.punishment.Punishment;
import xyz.msws.defybans.data.punishment.Punishment.Key;
import xyz.msws.defybans.data.punishment.Punishment.Type;
import xyz.msws.defybans.data.punishment.PunishmentBuilder;
import xyz.msws.defybans.data.punishment.PunishmentTracker;

public class BanTracker extends Tracker {

	public BanTracker(String url, PunishmentTracker master) {
		super(url, master);
	}

	@Override
	public void execute() {
		try {
			Connection con = Jsoup.connect(url);
			Document doc = con.get();

			Elements tables = doc.getElementsByTag("tbody");
			List<Element> bans = tables.subList(5, tables.size());

			for (Element element : bans) {
				List<Element> values = element.getElementsByAttributeValue("height", "16");

				PunishmentBuilder builder = new PunishmentBuilder(Type.BAN);

				for (int i = 0; i < values.size(); i++) {
					Punishment.Key k = Key.fromString(values.get(i).text());
					if (k == null)
						continue;
					builder.set(k, values.get(i + 1).text());
				}

				tracker.addPunishment(builder.build());
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
