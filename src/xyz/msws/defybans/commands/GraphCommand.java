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

import net.dv8tion.jda.api.entities.Message;
import xyz.msws.defybans.Client;
import xyz.msws.defybans.data.punishment.Punishment;
import xyz.msws.defybans.tracker.GuildTrackAssigner;
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

		if (args.length > 0)
			round = TimeParser.getPunishmentDuration(String.join(" ", args));

		if (round <= 0) {
			message.getChannel()
					.sendMessage("An invalid timespan was specified, please use the format `[value] [span]` Eg: 3 d")
					.queue();
			return;
		}

		message.getChannel()
				.sendMessageFormat("Generating using time period rounding of %s...",
						TimeParser.getDurationDescription(round))
				.delay(5, TimeUnit.SECONDS).flatMap(m -> m.delete()).queue();
		round *= 1000;

		List<Punishment> ps = new ArrayList<>(assigner.getTracker(message.getGuild()).getPunishments());

		Collections.sort(ps);

		XYChart chart = new XYChartBuilder().title("Bans over time").xAxisTitle("Dates").yAxisTitle("Bans").width(1280)
				.height(720).build();

		XYStyler style = chart.getStyler();
		style.setChartBackgroundColor(new Color(44, 48, 50));
		style.setXAxisTitleVisible(false);
		style.setYAxisTitleVisible(false);
		style.setChartFontColor(message.getGuild().getMember(client.getJDA().getSelfUser()).getColor());
//		style.setLegendVisible(false);
		style.setLegendPosition(LegendPosition.InsideNE);
		style.setLegendBackgroundColor(new Color(54, 58, 60));
		style.setSeriesColors(new Color[] { Color.WHITE, Color.BLUE });
		style.setPlotBackgroundColor(new Color(44, 48, 50));
		style.setPlotGridLinesColor(new Color(50, 245, 50, 50));
		style.setAxisTickLabelsColor(Color.WHITE);
//		style.setPlotContentSize(.9);

		Map<Long, Integer> values = new LinkedHashMap<>();

		for (Punishment p : ps) {
			long v = (p.getDate() / round) * round;
			values.put(v, values.getOrDefault(v, 0) + 1);
		}

		List<Date> dates = new ArrayList<>();
		List<Integer> vs = new ArrayList<>();
		for (Entry<Long, Integer> entry : values.entrySet()) {
			dates.add(new Date(entry.getKey()));
			vs.add(entry.getValue());
		}

		chart.addSeries("Period Bans", dates, vs);

		File result = new File("result.png");
		try {
			BitmapEncoder.saveBitmapWithDPI(chart, result.getName(), BitmapFormat.PNG, 300);
			message.getChannel().sendFile(result).queue();
		} catch (IOException e) {
			e.printStackTrace();
			message.getChannel().sendMessage("Could not generate graph: \n```" + e.getMessage() + "```").queue();
		}
	}

}
