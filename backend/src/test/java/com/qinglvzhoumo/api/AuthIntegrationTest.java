package com.qinglvzhoumo.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthIntegrationTest {
  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate rest;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void anonymousRegisterLoginLogoutFlow() {
    Map<String, Object> anonymous = post("/api/auth/anonymous", Map.of(
        "deviceId", "device-register",
        "nickname", "A"
    ), null, HttpStatus.OK);
    String anonymousToken = stringValue(anonymous, "token");

    Map<String, Object> invite = post("/api/couples/invite", Map.of(), anonymousToken, HttpStatus.OK);
    assertThat(invite.get("coupleId")).isNotNull();

    Map<String, Object> registered = post("/api/auth/register", Map.of(
        "phone", "13800000001",
        "password", "abc12345",
        "nickname", "A"
    ), anonymousToken, HttpStatus.OK);
    assertThat(registered.get("registered")).isEqualTo(true);
    assertThat(registered.get("coupleId")).isEqualTo(invite.get("coupleId"));
    String registeredToken = stringValue(registered, "token");

    Map<String, Object> me = get("/api/auth/me", registeredToken, HttpStatus.OK);
    assertThat(me.get("phone")).isEqualTo("13800000001");
    assertThat(me.get("registered")).isEqualTo(true);

    Map<String, Object> loggedIn = post("/api/auth/login", Map.of(
        "phone", "13800000001",
        "password", "abc12345",
        "deviceId", "device-new"
    ), null, HttpStatus.OK);
    assertThat(loggedIn.get("coupleId")).isEqualTo(invite.get("coupleId"));

    post("/api/auth/logout", Map.of(), stringValue(loggedIn, "token"), HttpStatus.OK);
    get("/api/auth/me", stringValue(loggedIn, "token"), HttpStatus.UNAUTHORIZED);
  }

  @Test
  void rejectsDuplicateInvalidWeakAndWrongPassword() {
    Map<String, Object> anonymous = post("/api/auth/anonymous", Map.of(
        "deviceId", "device-errors",
        "nickname", "A"
    ), null, HttpStatus.OK);
    String token = stringValue(anonymous, "token");

    post("/api/auth/register", Map.of(
        "phone", "13800000002",
        "password", "abc12345"
    ), token, HttpStatus.OK);

    Map<String, Object> secondAnonymous = post("/api/auth/anonymous", Map.of(
        "deviceId", "device-errors-2",
        "nickname", "B"
    ), null, HttpStatus.OK);

    Map<String, Object> duplicate = post("/api/auth/register", Map.of(
        "phone", "13800000002",
        "password", "abc12345"
    ), stringValue(secondAnonymous, "token"), HttpStatus.CONFLICT);
    assertThat(duplicate.get("code")).isEqualTo("PHONE_ALREADY_REGISTERED");

    Map<String, Object> invalidPhone = post("/api/auth/register", Map.of(
        "phone", "123",
        "password", "abc12345"
    ), stringValue(secondAnonymous, "token"), HttpStatus.BAD_REQUEST);
    assertThat(invalidPhone.get("code")).isEqualTo("INVALID_PHONE");

    Map<String, Object> weakPassword = post("/api/auth/register", Map.of(
        "phone", "13800000003",
        "password", "abcdefgh"
    ), stringValue(secondAnonymous, "token"), HttpStatus.BAD_REQUEST);
    assertThat(weakPassword.get("code")).isEqualTo("WEAK_PASSWORD");

    int wrongPasswordStatus = postStatusRaw("/api/auth/login", Map.of(
        "phone", "13800000002",
        "password", "wrong123",
        "deviceId", "device-login-wrong"
    ), null);
    assertThat(wrongPasswordStatus).isEqualTo(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  void loginBlocksDifferentAnonymousCoupleSpace() {
    Map<String, Object> owner = post("/api/auth/anonymous", Map.of(
        "deviceId", "device-owner",
        "nickname", "Owner"
    ), null, HttpStatus.OK);
    String ownerToken = stringValue(owner, "token");
    Map<String, Object> ownerInvite = post("/api/couples/invite", Map.of(), ownerToken, HttpStatus.OK);
    post("/api/auth/register", Map.of(
        "phone", "13800000004",
        "password", "abc12345"
    ), ownerToken, HttpStatus.OK);

    Map<String, Object> other = post("/api/auth/anonymous", Map.of(
        "deviceId", "device-conflict",
        "nickname", "Other"
    ), null, HttpStatus.OK);
    String otherToken = stringValue(other, "token");
    Map<String, Object> otherInvite = post("/api/couples/invite", Map.of(), otherToken, HttpStatus.OK);
    assertThat(otherInvite.get("coupleId")).isNotEqualTo(ownerInvite.get("coupleId"));

    Map<String, Object> conflict = post("/api/auth/login", Map.of(
        "phone", "13800000004",
        "password", "abc12345",
        "deviceId", "device-conflict"
    ), null, HttpStatus.CONFLICT);
    assertThat(conflict.get("code")).isEqualTo("COUPLE_SPACE_CONFLICT");
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> post(String path, Map<String, ?> body, String token, HttpStatus expected) {
    HttpHeaders headers = headers(token);
    ResponseEntity<Map> response = rest.postForEntity(path, new HttpEntity<>(body, headers), Map.class);
    assertThat(response.getStatusCode()).isEqualTo(expected);
    return response.getBody();
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> get(String path, String token, HttpStatus expected) {
    ResponseEntity<Map> response = rest.exchange(path, HttpMethod.GET, new HttpEntity<>(headers(token)), Map.class);
    assertThat(response.getStatusCode()).isEqualTo(expected);
    return response.getBody();
  }

  private HttpHeaders headers(String token) {
    HttpHeaders headers = new HttpHeaders();
    if (token != null && !token.isBlank()) {
      headers.setBearerAuth(token);
    }
    return headers;
  }

  private String stringValue(Map<String, Object> values, String key) {
    Object value = values.get(key);
    return value == null ? "" : String.valueOf(value);
  }

  private int postStatusRaw(String path, Map<String, ?> body, String token) {
    try {
      HttpRequest.Builder builder = HttpRequest.newBuilder()
          .uri(URI.create("http://localhost:" + port + path))
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));
      if (token != null && !token.isBlank()) {
        builder.header("Authorization", "Bearer " + token);
      }
      HttpResponse<String> response = HttpClient.newHttpClient().send(
          builder.build(),
          HttpResponse.BodyHandlers.ofString()
      );
      return response.statusCode();
    } catch (Exception error) {
      throw new AssertionError(error);
    }
  }
}
