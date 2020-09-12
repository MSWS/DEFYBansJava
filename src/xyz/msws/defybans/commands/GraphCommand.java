package xyz.msws.defybans.commands;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.XYStyler;
import org.knowm.xchart.style.Styler.LegendPosition;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import xyz.msws.defybans.Client;
import xyz.msws.defybans.data.pageable.PageableEmbed;
import xyz.msws.defybans.data.punishment.Punishment;
import xyz.msws.defybans.data.punishment.Punishment.Key;
import xyz.msws.defybans.tracker.GuildTrackAssigner;
import xyz.msws.defybans.utils.PunishmentUtils;
import xyz.msws.defybans.utils.TimeParser;

public class GraphCommand extends AbstractCommand {
	private GuildTrackAssigner assigner;

	public GraphCommand(Client client, String name) {
		super(client, name);
		setAliases("g");
		if ((assigner = client.getModule(GuildTrackAssigner.class)) == null)
			client.getCommandListener().unregisterCommand(this);
	}

	@Override
	public void execute(Message message, String[] args) {
		long round = TimeUnit.DAYS.toSeconds(7); // One day

		if (args.length == 0) {
			message.getChannel().sendMessage("You must specify an identifier").queue();
			return;
		}

		if (args.length >= 2) {
			round = TimeParser
					.getPunishmentDuration(String.join(" ", args).substring(String.join(" ", args).indexOf(" ") + 1));
		}

		if (round <= 0) {
			message.getChannel()
					.sendMessage("An invalid timespan was specified, please use the format `[value] [span]` Eg: 3 d")
					.queue();
			return;
		}

		Key key = Key.fromString(args[0]);
		if (key == null) {
			EmbedBuilder builder = new EmbedBuilder();
			builder.setTitle("Unknown identifier");
			builder.setColor(Color.RED);

			List<MessageEmbed> pages = new ArrayList<>();
			int i = 1; // Set to one to avoid 0 % 7 = 0
			for (Key k : Key.values()) {
				builder.appendDescription(k.getId() + "\n");
				if (i % 7 == 0) {
					pages.add(builder.build());
					builder = new EmbedBuilder();
					i = 1;
				}
				i++;
			}
			if (!builder.isEmpty())
				pages.add(builder.build());

			new PageableEmbed(client, pages).bindTo(message.getAuthor()).send(message.getTextChannel());
			return;
		}

		message.getChannel()
				.sendMessageFormat("Generating a graph depicting top 10 %ss with a time period rounding of %s...",
						key.getId(), TimeParser.getDurationDescription(round))
				.delay(5, TimeUnit.SECONDS).flatMap(m -> m.delete()).queue();
		round *= 1000;

		List<Punishment> ps = new ArrayList<>(assigner.getTracker(message.getGuild()).getPunishments());

		Collections.sort(ps);

		XYChart chart = new XYChartBuilder().title(key.getId() + " over time").xAxisTitle("Dates")
				.yAxisTitle("Occurences").width(1280).height(720).build();

		XYStyler style = chart.getStyler();
		style.setChartBackgroundColor(new Color(44, 48, 50));
		style.setXAxisTitleVisible(false);
		style.setYAxisTitleVisible(false);
		style.setChartFontColor(message.getGuild().getMember(client.getJDA().getSelfUser()).getColor());
//		style.setLegendVisible(false);
		style.setLegendPosition(LegendPosition.InsideNE);
		style.setLegendBackgroundColor(new Color(54, 58, 60));
		style.setSeriesColors(new Color[] { Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE, Color.PINK,
				Color.MAGENTA, new Color(210, 105, 30), Color.WHITE, Color.BLACK });
		style.setPlotBackgroundColor(new Color(44, 48, 50));
		style.setPlotGridLinesColor(new Color(50, 245, 50, 50));
		style.setAxisTickLabelsColor(Color.WHITE);
//		style.setPlotContentSize(.9);

		int i = 0;
		for (String s : PunishmentUtils.rankRawList(ps, key)) {
			if (s == null || s.isEmpty() || s.length() == 0)
				continue;
			Map<Long, Integer> values = new LinkedHashMap<>();

			for (Punishment p : ps) {
				if (!s.equals(p.get(key, String.class)))
					continue;
				long v = (p.getDate() / round) * round;
				values.put(v, values.getOrDefault(v, 0) + 1);
			}

			List<Date> dates = new ArrayList<>();
			List<Integer> vs = new ArrayList<>();
			for (Entry<Long, Integer> entry : values.entrySet()) {
				dates.add(new Date(entry.getKey()));
				vs.add(entry.getValue());
			}

			chart.addSeries(s, dates, vs);
			i++;
			if (i > 9)
				break;
		}

		File result = new File(message.getAuthor().getId() + ".png");
		try {
			BitmapEncoder.saveBitmapWithDPI(chart, result.getName(), BitmapFormat.PNG, 300);
			message.getChannel().sendFile(result).queue();
		} catch (IOException e) {
			e.printStackTrace();
			message.getChannel().sendMessage("Could not generate graph: \n```" + e.getMessage() + "```").queue();
		}
	}

}
