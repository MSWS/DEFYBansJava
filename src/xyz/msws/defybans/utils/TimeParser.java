package xyz.msws.defybans.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

public class TimeParser {

	public static long getPunishmentDate(String date) {
		SimpleDateFormat format = new SimpleDateFormat("MM-dd-yy kk:mm");
		try {
			return format.parse(date).toInstant().toEpochMilli();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return 0;
	}

	public static long getPunishmentDuration(String duration) {
		long d = 0;
		try {
			for (String s : duration.split(",")) {
				s = s.trim();
				int v = Integer.parseInt(s.split(" ")[0]);
				TimeUnit unit = TimeUnit.fromString(s.split(" ")[1]);
				d += v * unit.getSeconds();
			}
		} catch (NumberFormatException e) {
			return d;
		}

		return d;
	}

	public static String getDateDescription(long epoch) {
		SimpleDateFormat format = new SimpleDateFormat("MM-dd-yy kk:mm");
		return format.format(new Date(epoch));
	}

	public static String getDurationDescription(long seconds) {
		for (TimeUnit unit : TimeUnit.values()) {
			if (unit.getSeconds() >= seconds) {
				return String.format("%.2f %s", seconds / (double) unit.getSeconds(), unit.toString());
			}
		}
		return String.format("%.2f %s", seconds / (double) TimeUnit.YEARS.getSeconds(), TimeUnit.YEARS.toString());
	}

	private enum TimeUnit {
		SECONDS(1, "s"), MINUTES(60, "m"), HOURS(60 * 60, "h"), DAYS(60 * 60 * 24, "d"), WEEKS(60 * 60 * 24 * 7, "wk"),
		MONTHS(60 * 60 * 24 * 7 * 4, "mo"), YEARS(60 * 60 * 24 * 7 * 4 * 12, "y");

		long seconds;
		String id;

		TimeUnit(long seconds, String id) {
			this.seconds = seconds;
			this.id = id;
		}

		public long getSeconds() {
			return seconds;
		}

		public String getId() {
			return id;
		}

		public static TimeUnit fromString(String s) {
			for (TimeUnit u : TimeUnit.values()) {
				if (u.getId().equals(s))
					return u;
			}
			return SECONDS;
		}
	}

	@Test
	public void testDateParse() {
		long time = System.currentTimeMillis();
		SimpleDateFormat format = new SimpleDateFormat("MM-dd-yy kk:mm");
		assertTrue(time - getPunishmentDate(format.format(time)) < 100000);
	}

	@Test
	public void testPunishmentDurationSimple() {
		String check = "3 d";

		assertEquals(TimeUnit.DAYS.getSeconds() * 3, getPunishmentDuration(check));
	}

	@Test
	public void testPunishmentDurationComplex() {
		String check = "3 d, 1 mo";
		assertEquals(TimeUnit.DAYS.getSeconds() * 3 + TimeUnit.MONTHS.getSeconds(), getPunishmentDuration(check));
	}

	@Test
	public void testPunishmentDurationExpired() {
		String check = "3 d, 2 d (Expired)";
		assertEquals(TimeUnit.DAYS.getSeconds() * 3 + TimeUnit.DAYS.getSeconds() * 2, getPunishmentDuration(check));
	}

	@Test
	public void testDurationDescription() {
		long seconds = 60;
		System.out.println(getDurationDescription(seconds));
		assertEquals(getDurationDescription(seconds), "1.00 MINUTES");
	}
}
