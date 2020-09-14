package xyz.msws.defybans.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import xyz.msws.defybans.data.punishment.Punishment.Type;
import xyz.msws.defybans.tracker.Tracker;

public class ServerConfig implements Saveable {

	private long guild, channel;
	private JSONArray trackers = new JSONArray();
	private File file;

	public ServerConfig(long guild) {
		this.guild = guild;
		file = new File(System.getProperty("user.dir") + File.separator + guild + "-config.txt");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		load();
	}

	public long getGuildID() {
		return guild;
	}

	public long getChannelID() {
		return channel;
	}

	public JSONArray getTrackerInfo() {
		return trackers;
	}

	public void addTracker(Tracker tracker) {
		addTracker(Type.BAN, tracker.getURL());
	}

	public void addTracker(JSONObject arr) {
		System.out.println("adding a tracker");
		trackers.put(arr);
	}

	public void addTracker(Type type, String url) {
		JSONObject obj = new JSONObject();
		obj.put("url", url);
		obj.put("type", type);
		addTracker(obj);
	}

	public void clearTrackers() {
		trackers = new JSONArray();
	}

	public void setChannelID(long id) {
		this.channel = id;
	}

	@Override
	public void load() {
		if (!file.exists())
			return;
		FileReader reader;
		try {
			reader = new FileReader(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		BufferedReader bf = new BufferedReader(reader);
		String s;
		StringBuilder builder = new StringBuilder();
		try {
			while ((s = bf.readLine()) != null) {
				builder.append(s);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (builder.length() == 0)
			return;

		JSONObject values = new JSONObject(builder.toString());
		this.guild = values.getLong("guild");
		this.channel = values.getLong("channel");
		this.trackers = values.getJSONArray("trackers");
	}

	@Override
	public void save() {
		JSONObject obj = new JSONObject();
		obj.put("guild", guild);
		obj.put("channel", channel);
		obj.put("trackers", trackers);

		FileWriter writer;
		try {
			writer = new FileWriter(file);
			writer.write(obj.toString());
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
