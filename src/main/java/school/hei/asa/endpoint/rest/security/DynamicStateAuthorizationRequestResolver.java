package school.hei.asa.endpoint.rest.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

@Component
public class DynamicStateAuthorizationRequestResolver
    implements OAuth2AuthorizationRequestResolver {

  private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;

  public DynamicStateAuthorizationRequestResolver(
      ClientRegistrationRepository clientRegistrationRepository) {
    this.defaultResolver =
        new DefaultOAuth2AuthorizationRequestResolver(
            clientRegistrationRepository, "/oauth2/authorization");
  }

  @Override
  public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
    var authorizationRequest = defaultResolver.resolve(request);
    return customizeAuthorizationRequest(authorizationRequest);
  }

  @Override
  public OAuth2AuthorizationRequest resolve(
      HttpServletRequest request, String clientRegistrationId) {
    var authorizationRequest = defaultResolver.resolve(request, clientRegistrationId);
    return customizeAuthorizationRequest(authorizationRequest);
  }

  private OAuth2AuthorizationRequest customizeAuthorizationRequest(
      OAuth2AuthorizationRequest authorizationRequest) {
    if (authorizationRequest == null) {
      return null;
    }

    var newState = StateGenerator.generateState();

    return OAuth2AuthorizationRequest.from(authorizationRequest).state(newState).build();
  }
}
