package school.hei.asa.service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class ChartPieService {
  private final String tempDirPath = System.getProperty("java.io.tmpdir");

  public String generatePieChartImage(DefaultPieDataset dataset) {
    JFreeChart chart =
        ChartFactory.createPieChart(
            "Distribution of days worked by product", dataset, true, true, false);

    PiePlot plot = (PiePlot) chart.getPlot();
    if (dataset.getItemCount() >= 1) plot.setSectionPaint(dataset.getKey(0), new Color(46, 52, 89));
    if (dataset.getItemCount() >= 2)
      plot.setSectionPaint(dataset.getKey(1), new Color(206, 111, 143));
    if (dataset.getItemCount() >= 3)
      plot.setSectionPaint(dataset.getKey(2), new Color(122, 92, 204));
    if (dataset.getItemCount() >= 4)
      plot.setSectionPaint(dataset.getKey(3), new Color(0, 163, 204));
    if (dataset.getItemCount() >= 5)
      plot.setSectionPaint(dataset.getKey(4), new Color(242, 206, 0));
    if (dataset.getItemCount() >= 6)
      plot.setSectionPaint(dataset.getKey(5), new Color(227, 148, 0));

    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      ChartUtils.writeChartAsPNG(out, chart, 1200, 900);
      ChartUtils.saveChartAsPNG(
          new File(tempDirPath + "/" + LocalDate.now() + ".png"), chart, 900, 600);
      return Base64.getEncoder().encodeToString(out.toByteArray());
    } catch (IOException e) {
      throw new RuntimeException("Failed to generate chart image", e);
    }
  }
}
