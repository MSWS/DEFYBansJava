package xyz.msws.defybans.data.punishment;

import java.awt.Color;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONObject;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import xyz.msws.defybans.utils.TimeParser;

/**
 * Represents a punishment from someone on someone, can be a BAN, MUTE, or GAG.
 * 
 * @author imodm
 *
 */
public class Punishment {
	public enum Type {
		BAN, MUTE, GAG;
	}

	public enum Key {
		TYPE("Type"), USERNAME("Player", "name", "target"), STEAMID("Steam ID", "steam", "id"),
		STEAM3("Steam3 ID", "steam3", "s3"), STEAMCOM("Steam Community", "commid", "community", "steamcom"),
		DATE("Invoked on", "date", "invoked"), DURATION("Banlength", "length", "duration"), UNBANREASON("Unban reason"),
		UNBANADMIN("Unbanned by Admin", "unbanner", "Unban admin"), EXPIRES("Expires on", "expires", "expiration"),
		REASON("Reason"), ADMIN("Banned by Admin", "admin", "banner"), SERVER("Banned from", "server"),
		TOTAL("Total Bans", "total"), BLOCKS("Blocked", "blocks");

		private String id;
		private String[] aliases;

		Key(String id, String... aliases) {
			this.id = id;
			this.aliases = aliases;
		}

		public String getId() {
			return id;
		}

		public String[] getAliases() {
			return aliases;
		}

		public static Key fromString(String id) {
			for (Key k : Key.values()) {
				if (k.getId().replace(" ", "").equalsIgnoreCase(id.replace(" ", "")))
					return k;
				for (String s : k.getAliases()) {
					if (id.replace(" ", "").equalsIgnoreCase(s.replace(" ", "")))
						return k;
				}
			}

			return null;
		}
	}

	private EnumMap<Key, Object> data;

	public Punishment(EnumMap<Key, Object> data) {
		if (!data.containsKey(Key.USERNAME))
			throw new IllegalArgumentException("Data does not have Player");
		if (!data.containsKey(Key.TYPE))
			throw new IllegalArgumentException("Data does not have Type");
		this.data = data;
	}

	/**
	 * Parses a Punishment from a String @see Punishment#toString()
	 * 
	 * @param data
	 */
	public Punishment(String data) {
		JSONObject obj = new JSONObject(data);
		this.data = new EnumMap<>(Key.class);
		for (Entry<String, Object> entry : obj.toMap().entrySet()) {
			Key key = Key.fromString(entry.getKey());
			if (key == null) {
				throw new IllegalArgumentException("Unable to parse key: " + entry.getKey());
			}

			this.data.put(key, entry.getValue());
		}
	}

	private static EnumSet<Key> ignore = EnumSet.of(Key.TYPE, Key.REASON, Key.SERVER);

	public MessageEmbed createEmbed() {
		return createEmbed(null);
	}

	public MessageEmbed createEmbed(Punishment old) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle(
				String.format("%s'%s %s", data.get(Key.USERNAME),
						get(Key.USERNAME, String.class).toLowerCase().endsWith("s") ? "" : "s",
						WordUtils.capitalizeFully(get(Key.TYPE).toString())),
				String.format("https://bans.defyclan.com/index.php?p=banlist&searchText=%s&Submit", get(Key.STEAMID)));

		builder.setDescription(MarkdownSanitizer.escape(get(Key.REASON, String.class)));

		int red = (int) (255 * ((double) getDuration() / (double) TimeUnit.DAYS.toSeconds(30)));
		int green = (int) (255 * ((double) getDuration() / (double) TimeUnit.DAYS.toSeconds(7)));
		int blue = (int) (255 * ((double) getDuration() / (double) TimeUnit.DAYS.toSeconds(3)));

		builder.setColor(new Color(Math.min(red, 255), Math.min(green, 255), Math.min(blue, 255)));

		if (old == null || old.equals(this)) {
			for (Entry<Key, Object> entry : data.entrySet()) {
				if (ignore.contains(entry.getKey()))
					continue;
				builder.addField(entry.getKey().getId(), MarkdownSanitizer.escape(entry.getValue().toString()), true);
			}

			return builder.build();
		}

		for (Entry<Key, Object> entry : data.entrySet()) {
			if (!old.getData().containsKey(entry.getKey()) || old.getData().get(entry.getKey()) == null) {
				builder.addField(entry.getKey().getId(),
						"[None] -> " + MarkdownSanitizer.escape(entry.getValue().toString()), true);
				continue;
			}
//			if (ignore.contains(entry.getKey()))
//				continue;
			if (old.getData().get(entry.getKey()).equals(entry.getValue()))
				continue;
			builder.addField(entry.getKey().getId(),
					MarkdownSanitizer.escape(old.getData().get(entry.getKey()) + " -> " + entry.getValue()), true);
		}

		return builder.build();
	}

	/**
	 * Checks if two punishments are exactly the same @see
	 * Punishment#isSimilar(Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;

		if (obj == null || !(obj instanceof Punishment))
			return false;

		Punishment p = (Punishment) obj;
		return p.getData().equals(this.data);
	}

	/**
	 * Compares if two punishments occur at the same time and are on the same player
	 * This can be used to compare if a punishment was updated/edited from a
	 * previous one
	 * 
	 * @param obj The punishment/object to compare
	 * @return True if the hash codes are the same
	 */
	public boolean isSimilar(Object obj) {
		if (obj == this)
			return true;
		if (equals(obj))
			return true;

		Punishment p = (Punishment) obj;
		return p.hashCode() == this.hashCode();
	}

	/**
	 * Overrides {@link Object#hashCode()} to only take into account the STEAMID,
	 * DATE, and ADMIN values.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(get(Key.STEAMID, "Unknown"), get(Key.DATE, "Unknown"), get(Key.ADMIN, "Unknown"));
	}

	/**
	 * Returns the ban data
	 * 
	 * @return Map data
	 */
	@Nonnull
	public EnumMap<Key, Object> getData() {
		return data;
	}

	/**
	 * Returns the specified value
	 * 
	 * @param id The Key to lookup
	 * @return The value, may be null if it does not exist
	 */
	@Nullable
	public Object get(Key id) {
		return data.get(id);
	}

	public boolean has(Key id) {
		return data.containsKey(id);
	}

	public <T> T get(Key id, Class<T> cast) {
		return cast.cast(get(id));
	}

	public Object get(Key id, Object def) {
		return has(id) ? get(id) : def;
	}

	public <T> T get(Key id, Class<T> cast, T def) {
		return has(id) ? get(id, cast) : def;
	}

	public void edit(Punishment newData) {
		if (!isSimilar(newData))
			throw new IllegalArgumentException("newData must be similar to current punishment");
		data = newData.getData();
	}

	/**
	 * Returns a human-readable version of a duration, eg: 1.51 MONTHS
	 * 
	 * @return
	 */
	public String getDurationDescription() {
		return TimeParser.getDurationDescription(TimeParser.getPunishmentDuration(get(Key.DURATION, String.class)));
	}

	/**
	 * Returns a human-readable version of a date, format: MM-dd-yy kk:mm
	 * 
	 * @return Formatted string describing when the ban was applied
	 */
	public String getDateDescription() {
		return TimeParser.getDateDescription(TimeParser.getPunishmentDate(get(Key.DATE, String.class)));
	}

	/**
	 * Returns a human-readable version of a date, format: MM-dd-yy kk:mm
	 * 
	 * @return Formatted string describing when the ban will expire
	 */
	public String getExpirationDescription() {
		return TimeParser.getDateDescription(TimeParser.getPunishmentDate(get(Key.EXPIRES, String.class)));
	}

	/**
	 * Returns the raw value of when the ban was applied
	 * 
	 * @return
	 */
	public String getRawDate() {
		return get(Key.DATE, String.class);
	}

	/**
	 * Returns the raw value of when the ban will expire
	 * 
	 * @return
	 */
	public String getRawExpiration() {
		return get(Key.DATE, String.class);
	}

	public long getDate() {
		return TimeParser.getPunishmentDate(getRawDate());
	}

	public long getDuration() {
		return TimeParser.getPunishmentDuration(get(Key.DURATION, String.class));
	}

	public long getExpirationDate() {
		return TimeParser.getPunishmentDate(getRawExpiration());
	}

	@Override
	public String toString() {
		JSONObject obj = new JSONObject();
		for (Entry<Key, Object> values : data.entrySet()) {
			obj.put(values.getKey().getId(), values.getValue().toString());
		}
		return obj.toString();
	}

}
