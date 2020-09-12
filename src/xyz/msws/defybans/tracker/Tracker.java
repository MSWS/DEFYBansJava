package xyz.msws.defybans.tracker;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import xyz.msws.defybans.data.punishment.PunishmentTracker;

public abstract class Tracker extends Timer {
	protected String url;
	protected PunishmentTracker tracker;
	protected long refreshRate = TimeUnit.MINUTES.toMillis(1);
	protected boolean running = false;

	public Tracker(String url, PunishmentTracker tracker) {
		this.url = url;
		this.tracker = tracker;
	}

	public String getURL() {
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
		return "{\"url\":" + url + "}\"";
	}

}
