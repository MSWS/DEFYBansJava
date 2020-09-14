package xyz.msws.defybans.tracker;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import xyz.msws.defybans.data.Callback;
import xyz.msws.defybans.data.punishment.PunishmentManager;

public abstract class Tracker extends Timer {
	protected String name, url, search;
	protected PunishmentManager tracker;
	protected long refreshRate = TimeUnit.MINUTES.toMillis(1);
	protected boolean running = false;

	public Tracker(String name, String url, PunishmentManager tracker) {
		this.name = name;
		this.url = url;
		this.tracker = tracker;
	}

	public abstract void verify(Callback<Boolean> call);

	public String getURL() {
		return url;
	}

	public String getName() {
		return url;
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
		this.running = true;
		this.schedule(new TimerTask() {
			@Override
			public void run() {
				Tracker.this.execute();
			}
		}, 0, refreshRate);
	}

	@Override
	public void cancel() {
		super.cancel();
		this.running = false;
	}

	public boolean running() {
		return running;
	}

	public void parseOnce() {
		execute();
	}

	public abstract void execute();

	// TODO
	@Override
	public String toString() {
		JSONObject obj = new JSONObject();
		obj.put("url", url);
		obj.put("rate", refreshRate);
		obj.put("search", search);
		return obj.toString();
	}

}
