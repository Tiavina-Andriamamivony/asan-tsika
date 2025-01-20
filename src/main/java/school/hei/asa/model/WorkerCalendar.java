package school.hei.asa.model;

import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Getter
public class WorkerCalendar {

  private final Worker worker;
  private final int year;
  private final ProductConf productConf;

  private final List<DailyExecution> dailyExecutions;

  public WorkerCalendar(Worker worker, int year, ProductConf productConf) {
    this.worker = worker;
    this.year = year;
    this.productConf = productConf;
    this.dailyExecutions =
        worker.dailyExecutions().stream()
            .filter(me -> year == me.date().getYear())
            .collect(toList());
  }

  public Map<DailyExecution.Type, List<LocalDate>> datesByDailyExecutionType() {
    Map<DailyExecution.Type, List<LocalDate>> res = new HashMap<>();

    Arrays.stream(DailyExecution.Type.values())
        .forEach(
            dailyExecutionType ->
                res.put(
                    dailyExecutionType,
                    filterByDailyExecutionType(dailyExecutions, dailyExecutionType)));

    return res;
  }

  private List<LocalDate> filterByDailyExecutionType(
      List<DailyExecution> dayExecutions, DailyExecution.Type dailyExecutionType) {
    return dayExecutions.stream()
        .filter(
            dailyExecution ->
                dailyExecutionType.equals(dailyExecution.type(productConf.careProductCode())))
        .map(DailyExecution::date)
        .toList();
  }
}
