package school.hei.asa.endpoint.rest.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

public class GeneratedStateAuthorizationRequestRepository
    implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

  private final ConcurrentHashMap<String, OAuth2AuthorizationRequest> store =
      new ConcurrentHashMap<>();

  @Override
  public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
    var state = request.getParameter("state");
    return store.get(state);
  }

  @Override
  public void saveAuthorizationRequest(
      OAuth2AuthorizationRequest authorizationRequest,
      HttpServletRequest request,
      HttpServletResponse response) {

    var generatedState = StateGenerator.generateState();

    var modifiedRequest =
        OAuth2AuthorizationRequest.from(authorizationRequest).state(generatedState).build();

    store.put(generatedState, modifiedRequest);
  }

  public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request) {
    var state = request.getParameter("state");
    return store.remove(state);
  }

  @Override
  public OAuth2AuthorizationRequest removeAuthorizationRequest(
      HttpServletRequest request, HttpServletResponse response) {
    return removeAuthorizationRequest(request);
  }
}
