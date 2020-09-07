package xyz.msws.defybans.tracker;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import xyz.msws.defybans.data.Punishment;
import xyz.msws.defybans.data.Punishment.Key;
import xyz.msws.defybans.data.Punishment.Type;
import xyz.msws.defybans.data.PunishmentBuilder;
import xyz.msws.defybans.data.PunishmentTracker;

public class BanTracker extends Timer {

	private String url;
	private long refreshRate = TimeUnit.MINUTES.toMillis(1);

	private PunishmentTracker tracker;

	public BanTracker(String url, PunishmentTracker master) {
		this.url = url;
		this.tracker = master;
	}

	public void setRefreshRate(long rate) {
		if (rate <= 0)
			throw new IllegalArgumentException("Rate must be greater than 0");
		this.refreshRate = rate;
	}

	public long getRefreshRate() {
		return refreshRate;
	}

	public void start() {
		this.schedule(new TimerTask() {
			@Override
			public void run() {
				BanTracker.this.run();
			}
		}, 0, refreshRate);
	}

	public void parseOnce() {
		run();
	}

	public void run() {
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
