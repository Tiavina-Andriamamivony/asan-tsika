package school.hei.asa;

import java.util.function.Supplier;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Accessors(fluent = true)
@Configuration
public class CareProductCodeSupplier implements Supplier<String> {
  private final String careProductCode;

  public CareProductCodeSupplier(@Value("${asa.care.product.code}") String careProductCode) {
    this.careProductCode = careProductCode;
  }

  @Override
  public String get() {
    return careProductCode;
  }
}
