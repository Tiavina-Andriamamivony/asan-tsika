package school.hei.asa.endpoint.rest.security;

import java.security.SecureRandom;
import java.util.Base64;

public class StateGenerator {
  private static final SecureRandom secureRandom = new SecureRandom();

  public static String generateState() {
    var randomBytes = new byte[24];
    secureRandom.nextBytes(randomBytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
  }
}
