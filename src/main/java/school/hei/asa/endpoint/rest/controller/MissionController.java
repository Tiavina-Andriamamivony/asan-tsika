package school.hei.asa.endpoint.rest.controller;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import school.hei.asa.endpoint.rest.controller.mapper.ThDailyExecutionMapper;
import school.hei.asa.endpoint.rest.controller.mapper.ThProductMapper;
import school.hei.asa.endpoint.rest.model.th.ThDailyExecution;
import school.hei.asa.model.DailyExecution;
import school.hei.asa.repository.DailyExecutionRepository;
import school.hei.asa.repository.ProductRepository;
import school.hei.asa.service.ProductConf;

@Controller
@AllArgsConstructor
public class MissionController {

  private final ProductRepository productRepository;
  private final DailyExecutionRepository dailyExecutionRepository;
  private final ProductConf productConf;
  private final ThDailyExecutionMapper thDailyExecutionMapper;
  private final WorkerToModelAdder workerToModelAdder;
  private final ThProductMapper thProductMapper;

  @GetMapping("/missions")
  public String getMissions(Model model, @RequestParam(required = false) String workerCode) {
    var products = productRepository.findAll();
    model.addAttribute("products", thProductMapper.toTh(products));

    workerToModelAdder.apply(workerCode, model);
    return "missions";
  }

  @GetMapping("/mission-executions")
  public String getMissionExecutions(
      Model model, @RequestParam(required = false) String workerCode) {
    var dailyExecutionsByDate = dailyExecutionsByDate(workerCode);
    var thDailyExecutions = new ArrayList<ThDailyExecution>();
    dailyExecutionsByDate.forEach(
        (date, deList) -> thDailyExecutions.add(thDailyExecutionMapper.toTh(date, deList)));
    model.addAttribute(
        "dailyExecutions",
        thDailyExecutions.stream().sorted(comparing(ThDailyExecution::date).reversed()).toList());
    model.addAttribute("careProductCode", productConf.careProductCode());

    workerToModelAdder.apply(workerCode, model);
    return "mission-executions";
  }

  private Map<LocalDate, List<DailyExecution>> dailyExecutionsByDate(String workerCode) {
    List<DailyExecution> allDailyExecutions = dailyExecutionRepository.findAll();
    var dailyExecutionsStream =
        workerCode == null || workerCode.isBlank()
            ? allDailyExecutions.stream()
            : allDailyExecutions.stream()
                .filter(dailyExecution -> workerCode.equals(dailyExecution.worker().code()));
    return dailyExecutionsStream.collect(groupingBy(DailyExecution::date));
  }
}
