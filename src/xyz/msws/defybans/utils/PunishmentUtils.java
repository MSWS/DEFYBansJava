package xyz.msws.defybans.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;

import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import xyz.msws.defybans.data.punishment.Punishment;
import xyz.msws.defybans.data.punishment.Punishment.Key;

public class PunishmentUtils {
	public static String rank(Collection<Punishment> ps, Key id) {
		return rank(ps, id, 5);
	}

	public static String rank(Collection<Punishment> ps, Key id, int size) {
		Map<String, Integer> rank = new HashMap<>();
		for (Punishment p : ps) {
			rank.put(p.get(id, String.class), rank.getOrDefault(p.get(id, String.class), 0) + 1);
		}

		rank = sortByValue(rank);

		StringJoiner joiner = new StringJoiner("\n");
		int i = 0;
		for (Entry<String, Integer> entry : rank.entrySet()) {
			joiner.add(MarkdownSanitizer.escape(entry.getKey()) + ": " + entry.getValue());
			i++;
			if (i >= size)
				break;
		}
		return joiner.toString();
	}

	public static List<String> rankList(Collection<Punishment> ps, Key id) {
		Map<String, Integer> rank = new HashMap<>();
		for (Punishment p : ps) {
			if (!p.getData().containsKey(id))
				continue;
			rank.put(p.get(id, String.class), rank.getOrDefault(p.get(id, String.class), 0) + 1);
		}

		rank = sortByValue(rank);

		List<String> result = new ArrayList<>();

		for (Entry<String, Integer> entry : rank.entrySet())
			result.add(MarkdownSanitizer.escape(entry.getKey()) + ": " + entry.getValue());

		return result;
	}

	public static List<String> rankRawList(Collection<Punishment> ps, Key id) {
		Map<String, Integer> rank = new HashMap<>();
		for (Punishment p : ps) {
			if (!p.getData().containsKey(id))
				continue;
			rank.put(p.get(id, String.class), rank.getOrDefault(p.get(id, String.class), 0) + 1);
		}

		rank = sortByValue(rank);

		List<String> result = new ArrayList<>();

		for (Entry<String, Integer> entry : rank.entrySet())
			result.add(entry.getKey());

		return result;
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
		list.sort(Entry.comparingByValue());
		Collections.reverse(list);

		Map<K, V> result = new LinkedHashMap<>();
		for (Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}

		return result;
	}
}
