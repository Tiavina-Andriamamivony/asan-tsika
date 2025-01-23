package school.hei.asa.endpoint.rest.model.th;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Getter
@AllArgsConstructor
public class ThProduct {
  String code;
  String name;
  String description;
  List<ThMission> missions;

  public double executedDays() {
    return missions.stream().mapToDouble(ThMission::executedDays).sum();
  }
}
