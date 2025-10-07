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
    return state != null ? store.get(state) : null;
  }

  @Override
  public void saveAuthorizationRequest(
      OAuth2AuthorizationRequest authorizationRequest,
      HttpServletRequest request,
      HttpServletResponse response) {
    if (authorizationRequest != null) {
      store.put(authorizationRequest.getState(), authorizationRequest);
    }
  }

  public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request) {
    var state = request.getParameter("state");
    return state != null ? store.remove(state) : null;
  }

  @Override
  public OAuth2AuthorizationRequest removeAuthorizationRequest(
      HttpServletRequest request, HttpServletResponse response) {
    return removeAuthorizationRequest(request);
  }
}
