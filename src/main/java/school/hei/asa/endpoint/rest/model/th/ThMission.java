package school.hei.asa.endpoint.rest.model.th;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import school.hei.asa.model.MissionExecution;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ThMission {
  String code;
  String title;
  String description;
  ThMissionExecutions missionExecutions;

  public double executedDays() {
    return missionExecutions.executions().stream()
        .mapToDouble(MissionExecution::dayPercentage)
        .sum();
  }
}
