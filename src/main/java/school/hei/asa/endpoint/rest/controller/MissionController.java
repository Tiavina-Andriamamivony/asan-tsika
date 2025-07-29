package school.hei.asa.endpoint.rest.controller;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
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
import school.hei.asa.service.ChartPieService;
import school.hei.asa.service.MissionService;

@Controller
@AllArgsConstructor
public class MissionController {

  private final DailyExecutionRepository dailyExecutionRepository;
  private final CareProductCodeSupplier careProductCodeSupplier;
  private final ThDailyExecutionMapper thDailyExecutionMapper;
  private final WorkerToModelAdder workerToModelAdder;
  private final MissionService missionService;
  private final ChartPieService chartPieService;
  @GetMapping("/missions")
  public String getMissions(Model model, @RequestParam(required = false) String workerCode) {
    var thProductsByWorkerCode = missionService.filterThProductsByWorkerCode(workerCode);
    var thProductsByMonth = missionService.thProductsByMonth(thProductsByWorkerCode);
    var thProductsExecutedDaysSumByMonth =
        missionService.thProductsExecutedDaysSumByMonth(thProductsByWorkerCode);

    DefaultPieDataset dataset = new DefaultPieDataset();
    thProductsByWorkerCode.forEach(
        p -> dataset.setValue(p.code() + " (" + p.name() + ")", p.executedDays()));
    String base64Chart = chartPieService.generatePieChartImage(dataset);


    model.addAttribute("pieChartImage", base64Chart);
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
    String filePath = System.getProperty("java.io.tmpdir") + "/";
    File file = new File(filePath + LocalDate.now() + ".png");
    ByteArrayResource resource =
        new ByteArrayResource(Files.readAllBytes(Path.of(file.getAbsolutePath())));
    HttpHeaders header = new HttpHeaders();
    header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + LocalDate.now() + ".png");

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
