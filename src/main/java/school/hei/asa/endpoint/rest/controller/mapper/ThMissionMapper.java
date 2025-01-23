package school.hei.asa.endpoint.rest.controller.mapper;

import org.springframework.stereotype.Component;
import school.hei.asa.endpoint.rest.model.th.ThMission;
import school.hei.asa.endpoint.rest.model.th.ThMissionExecutions;
import school.hei.asa.model.Mission;

@Component
public class ThMissionMapper {
  public ThMission toTh(Mission mission) {
    return new ThMission(
        mission.code(),
        mission.title(),
        mission.description(),
        new ThMissionExecutions(mission, mission.executions().stream().toList()));
  }
}
