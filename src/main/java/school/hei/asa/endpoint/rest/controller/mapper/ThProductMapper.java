package school.hei.asa.endpoint.rest.controller.mapper;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.asa.CareProductCodeSupplier;
import school.hei.asa.endpoint.rest.model.th.ThProduct;
import school.hei.asa.model.Product;

@AllArgsConstructor
@Component
public class ThProductMapper {

  private final ThMissionMapper thMissionMapper;
  private final CareProductCodeSupplier careProductCodeSupplier;

  public ThProduct toTh(Product product) {
    var thMissions = product.missions().stream().map(thMissionMapper::toTh).toList();
    var careProductCode = careProductCodeSupplier.get();
    return new ThProduct(
        product.code(),
        product.name(),
        product.description(),
        thMissions,
        product.isCare(careProductCode));
  }

  public List<ThProduct> toTh(List<Product> products) {
    return products.stream().map(this::toTh).toList();
  }
}
