package school.hei.asa.endpoint.rest.controller;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import school.hei.asa.CareProductCodeSupplier;
import school.hei.asa.endpoint.rest.controller.mapper.ThDailyExecutionMapper;
import school.hei.asa.endpoint.rest.model.th.ThDailyExecution;
import school.hei.asa.model.DailyExecution;
import school.hei.asa.repository.DailyExecutionRepository;
import school.hei.asa.service.MissionService;

@Controller
@AllArgsConstructor
public class MissionController {

  private final DailyExecutionRepository dailyExecutionRepository;
  private final CareProductCodeSupplier careProductCodeSupplier;
  private final ThDailyExecutionMapper thDailyExecutionMapper;
  private final WorkerToModelAdder workerToModelAdder;
  private final MissionService missionService;

  @GetMapping("/missions")
  public String getMissions(Model model, @RequestParam(required = false) String workerCode) {
    var thProductsByWorkerCode = missionService.filterThProductsByWorkerCode(workerCode);
    var thProductsByMonth = missionService.thProductsByMonth(thProductsByWorkerCode);
    var thProductsExecutedDaysSumByMonth =
        missionService.thProductsExecutedDaysSumByMonth(thProductsByWorkerCode);

    DefaultPieDataset dataset = new DefaultPieDataset();
    thProductsByWorkerCode.forEach(
          p -> dataset.setValue(p.code() + " (" + p.name() + ")", p.executedDays()));

    JFreeChart chart =
        ChartFactory.createPieChart(
            "Distribution of days worked by product", dataset, true, true, false);

    PiePlot plot = (PiePlot) chart.getPlot();
    plot.setSectionPaint(dataset.getKey(0), new Color(46, 52, 89));
    plot.setSectionPaint(dataset.getKey(1), new Color(206, 111, 143));
    plot.setSectionPaint(dataset.getKey(2), new Color(122, 92, 204));
    plot.setSectionPaint(dataset.getKey(3), new Color(0, 163, 204));
    plot.setSectionPaint(dataset.getKey(4), new Color(242, 206, 0));
    plot.setSectionPaint(dataset.getKey(5), new Color(227, 148, 0));

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      ChartUtils.writeChartAsPNG(out, chart, 900, 600);
      String base64Image = Base64.getEncoder().encodeToString(out.toByteArray());
      model.addAttribute("pieChartImage", base64Image);
      ChartUtils.saveChartAsPNG(new File("chart.png"), chart, 1200, 900);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    model.addAttribute("workerCode", workerCode);
    model.addAttribute("months", thProductsByMonth);
    model.addAttribute("products", thProductsByWorkerCode);
    model.addAttribute("total", thProductsExecutedDaysSumByMonth);
    workerToModelAdder.apply(workerCode, model);
    return "missions";
  }

  @SneakyThrows
  @GetMapping("/mission/download-chart")
  public ResponseEntity<ByteArrayResource> downloadChart() {
    File file = new File("chart.png");
    ByteArrayResource resource =
        new ByteArrayResource(Files.readAllBytes(Path.of(file.getAbsolutePath())));
    HttpHeaders header = new HttpHeaders();
    header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=chart.png");

    return ResponseEntity.ok()
        .headers(header)
        .contentLength(file.length())
        .contentType(MediaType.parseMediaType("application/octet-stream"))
        .body(resource);
  }

  @GetMapping("/mission-executions")
  public String getMissionExecutions(
      Model model,

      @RequestParam(required = false) String workerCode,
      @RequestParam(required = false) String yearMonth) {
    YearMonth month =
        (yearMonth == null || yearMonth.isBlank()) ? YearMonth.now() : YearMonth.parse(yearMonth);

    var dailyExecutionsByYearMonth = dailyExecutionsByDate(workerCode, month);
    var thDailyExecutions = new ArrayList<ThDailyExecution>();
    dailyExecutionsByYearMonth.forEach(
        (date, deList) -> thDailyExecutions.add(thDailyExecutionMapper.toTh(date, deList)));

    model.addAttribute(
        "dailyExecutions",
        thDailyExecutions.stream().sorted(comparing(ThDailyExecution::date).reversed()).toList());
    model.addAttribute("careProductCode", careProductCodeSupplier.get());
    model.addAttribute("month", month.toString());
    model.addAttribute("workerCode", workerCode);
    workerToModelAdder.apply(workerCode, model);

    return "mission-executions";
  }

  private Map<LocalDate, List<DailyExecution>> dailyExecutionsByDate(
      String workerCode, YearMonth month) {
    LocalDate startDate = month.atDay(1);
    LocalDate endDate = month.atEndOfMonth();

    if (workerCode == null || workerCode.isBlank()) {
      return dailyExecutionRepository.findByDateBetween(startDate, endDate).stream()
          .collect(groupingBy(DailyExecution::date));
    } else {
      return dailyExecutionRepository
          .findByWorkerCodeAndDateBetween(workerCode, startDate, endDate)
          .stream()
          .collect(groupingBy(DailyExecution::date));
    }
  }
}
