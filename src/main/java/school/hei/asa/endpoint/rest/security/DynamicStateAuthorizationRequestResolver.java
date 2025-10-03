package school.hei.asa.endpoint.rest.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

public class DynamicStateAuthorizationRequestResolver
    implements OAuth2AuthorizationRequestResolver {

  private final OAuth2AuthorizationRequestResolver defaultResolver;
  private final GeneratedStateAuthorizationRequestRepository stateRepository;

  public DynamicStateAuthorizationRequestResolver(
      ClientRegistrationRepository repo,
      String baseUri,
      GeneratedStateAuthorizationRequestRepository stateRepository) {
    this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(repo, baseUri);
    this.stateRepository = stateRepository;
  }

  @Override
  public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
    var req = defaultResolver.resolve(request);
    return generateDynamicState(req, request);
  }

  @Override
  public OAuth2AuthorizationRequest resolve(
      HttpServletRequest request, String clientRegistrationId) {
    var req = defaultResolver.resolve(request, clientRegistrationId);
    return generateDynamicState(req, request);
  }

  private OAuth2AuthorizationRequest generateDynamicState(
      OAuth2AuthorizationRequest req, HttpServletRequest request) {
    if (req == null) return null;

    var state = StateGenerator.generateState();
    var modified = OAuth2AuthorizationRequest.from(req).state(state).build();

    stateRepository.saveAuthorizationRequest(modified, request, null);

    return modified;
  }
}
