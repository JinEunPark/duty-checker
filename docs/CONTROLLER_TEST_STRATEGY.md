# Controller 테스트 전략 — Spring Boot + MockMvc

이 문서는 Spring Boot + MockMvc 기반 Controller 테스트 전략을 정의한다.

빌드는 성공하지만 런타임에서 발생하는 500 에러(Bean 주입 실패, Security 필터 설정 오류, 직렬화 오류 등)를
Controller 단위 테스트로 사전에 잡을 수 있도록 팀 기준을 수립한다.

---

## 1. 왜 Controller 테스트가 필요한가?

빌드(컴파일) 성공 != 런타임 정상 동작.

아래 유형의 오류는 빌드 시점에 잡히지 않는다.

| 오류 유형 | 원인 | 증상 |
|-----------|------|------|
| Bean 주입 실패 | `@MockBean` 누락, 의존 Bean 미등록 | 500 (`UnsatisfiedDependencyException`) |
| Security 필터 충돌 | `@WebMvcTest` + `SecurityConfig` 충돌 | 401/403 unexpected |
| PathVariable 타입 불일치 | `Long` 파라미터에 문자열 전달 | 400/500 |
| JSON 직렬화 오류 | `ZonedDateTime`, `enum` 직렬화 설정 누락 | 500 (`HttpMessageConversionException`) |
| @Valid 검증 실패 응답 오류 | `ExceptionHandler` 미적용 | 400 응답 포맷 불일치 |

### 실제 사례: GET /api/v1/connections 500 에러

`ConnectionController.getConnections`는 빌드가 통과했음에도 런타임에서 500을 반환한 적이 있다.
원인은 `ConnectionItemDto`가 `ZonedDateTime latestCheckedAt`과 `Role requesterRole`(enum) 필드를 포함하는데,
`ObjectMapper`의 Jackson 직렬화 설정이 누락되면 이 필드들이 런타임에서 변환 실패를 일으킨다.

이런 오류는 Controller 레이어를 Spring MVC 컨텍스트 위에서 직접 실행해보지 않으면 발견할 수 없다.
컴파일 타임이나 Service 단위 테스트로는 절대 잡을 수 없다.

---

## 2. @WebMvcTest vs @SpringBootTest 선택 기준

| 항목 | `@WebMvcTest` | `@SpringBootTest` |
|------|--------------|------------------|
| 로딩 범위 | MVC 레이어만 (Controller, Filter, Security 등) | 전체 `ApplicationContext` |
| 속도 | 빠름 (수초) | 느림 (10초 이상) |
| DB 연결 | 불필요 | 필요 (or `TestContainers`) |
| Service | `@MockBean`으로 대체 | 실제 Bean or `@MockBean` |
| Jackson 설정 | `@WebMvcTest`가 자동 포함 | 자동 포함 |
| 언제 쓰나 | Controller 단위 검증 (라우팅, 인증, 직렬화) | E2E 통합 테스트, DB 포함 시나리오 |

### 이 프로젝트 기본 선택: `@WebMvcTest`

- `ConnectionService` 등 Service Bean은 `@MockBean`으로 격리한다.
- `JwtAuthenticationFilter`와 `SecurityConfig`가 포함되므로 실제 인증 흐름을 검증할 수 있다.
- 빌드가 10초 미만으로 유지된다.
- DB, 외부 API 연동이 없어 CI 환경에서 안정적으로 실행된다.

---

## 3. Security 셋업 패턴

이 프로젝트는 `JwtAuthenticationFilter`(OncePerRequestFilter)가 `SecurityConfig`에 등록되어 있다.

```
Authorization: Bearer {token}
    -> JwtProvider.isValid(token)
    -> JwtProvider.extractPhone(token)
    -> SecurityContextHolder에 phone(String) 세팅
    -> Controller: @AuthenticationPrincipal String phone
```

`@WebMvcTest`는 Security 필터를 기본 포함한다. 따라서 JWT 인증을 흉내내려면 아래 두 가지 방법 중 하나를 선택한다.

### 방법 A: `@WithMockUser`로 SecurityContext 직접 세팅 (권장)

`@WithMockUser(username = "01012345678")`을 붙이면 Spring Security Test가 `SecurityContext`에
`UsernamePasswordAuthenticationToken`을 주입한다. `@AuthenticationPrincipal`이 이 username을 받아 동작한다.

단, `JwtAuthenticationFilter`가 `JwtProvider`를 의존하므로 `@MockBean`으로 등록해야
`ApplicationContext` 로딩 오류가 발생하지 않는다.

```java
@WebMvcTest(ConnectionController.class)
class ConnectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConnectionService connectionService;

    // JwtAuthenticationFilter의 의존성 — MockBean 없으면 Context 로딩 실패
    @MockBean
    private JwtProvider jwtProvider;

    @Test
    @WithMockUser(username = "01012345678")
    void getConnections_인증된사용자_200반환() throws Exception {
        GetConnectionsRespDto resp = new GetConnectionsRespDto(Role.GUARDIAN, List.of());
        given(connectionService.getConnections("01012345678")).willReturn(resp);

        mockMvc.perform(get("/api/v1/connections"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.connections").isArray());
    }

    @Test
    void getConnections_인증없음_401반환() throws Exception {
        mockMvc.perform(get("/api/v1/connections"))
               .andExpect(status().isUnauthorized());
    }
}
```

### 방법 B: Authorization 헤더 직접 주입 + `JwtProvider` stub

실제 필터 흐름 전체를 검증하고 싶을 때 사용한다. stub 코드가 늘어나는 단점이 있다.

```java
// JwtProvider의 isValid(), extractPhone()을 stub해서 필터 통과 시뮬레이션
given(jwtProvider.isValid("test-token")).willReturn(true);
given(jwtProvider.extractPhone("test-token")).willReturn("01012345678");

mockMvc.perform(get("/api/v1/connections")
           .header("Authorization", "Bearer test-token"))
       .andExpect(status().isOk());
```

### 방법 선택 기준

| 목적 | 선택 |
|------|------|
| Controller 로직 검증 (라우팅, 직렬화, 검증) | 방법 A (`@WithMockUser`) |
| JWT 필터 자체 동작 검증 | 방법 B (헤더 주입) |

Controller TC의 목적은 필터 검증이 아니라 Controller 로직 검증이므로, **방법 A를 기본으로 사용한다**.

---

## 4. 런타임 오류 유형별 TC 패턴

### 4-1. 500 에러 — Service Bean 주입 실패 (`@MockBean` 누락)

증상: `UnsatisfiedDependencyException` → 테스트 자체가 `ApplicationContext` 로딩 단계에서 실패한다.

잡는 방법: `ConnectionController`가 의존하는 모든 Bean에 `@MockBean`을 선언한다.
`JwtAuthenticationFilter`도 `JwtProvider`를 의존하므로 함께 등록해야 한다.

```java
@WebMvcTest(ConnectionController.class)
class ConnectionControllerTest {

    @MockBean
    private ConnectionService connectionService;  // Controller 직접 의존성

    @MockBean
    private JwtProvider jwtProvider;              // JwtAuthenticationFilter 의존성
    // 이 두 개가 없으면 ApplicationContext 로딩 자체가 실패한다
}
```

### 4-2. 500 에러 — JSON 직렬화 오류 (`ZonedDateTime`, enum)

증상: `ConnectionItemDto`의 `ZonedDateTime latestCheckedAt`, `Role requesterRole`(enum) 필드가
직렬화에 실패하면 `HttpMessageConversionException`이 발생하여 500을 반환한다.

잡는 방법: 실제 직렬화까지 검증하는 TC를 작성한다. 이 TC가 500을 반환하면 `application.yml`의
Jackson 설정 또는 `@JsonFormat`/`@JsonSerialize` 어노테이션을 확인한다.

```java
@Test
@WithMockUser(username = "01012345678")
void getConnections_응답직렬화_정상() throws Exception {
    ZonedDateTime now = ZonedDateTime.now();
    ConnectionItemDto item = ConnectionItemDto.forGuardian(mockConnection, now, false);
    GetConnectionsRespDto resp = new GetConnectionsRespDto(Role.GUARDIAN, List.of(item));
    given(connectionService.getConnections(any())).willReturn(resp);

    mockMvc.perform(get("/api/v1/connections"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.connections[0].latestCheckedAt").exists())
           .andExpect(jsonPath("$.connections[0].requesterRole").exists());
    // 500이 나오면 ZonedDateTime 또는 enum 직렬화 설정 오류
}
```

### 4-3. 400 에러 — `@Valid` 검증 실패 응답 포맷

증상: `MethodArgumentNotValidException` 발생 시 기본 Spring 응답 포맷과
커스텀 `@ExceptionHandler` 응답 포맷이 다를 수 있다. 클라이언트가 기대하는 포맷인지 검증해야 한다.

```java
@Test
@WithMockUser
void addConnection_빈전화번호_400반환() throws Exception {
    String body = """
        { "targetPhone": "" }
        """;

    mockMvc.perform(post("/api/v1/connections")
               .contentType(MediaType.APPLICATION_JSON)
               .content(body))
           .andExpect(status().isBadRequest());
}
```

### 4-4. 400 에러 — `@PathVariable` 타입 오류 (`Long` vs `String`)

증상: `ConnectionController`의 `/{id}` 경로는 `Long id`를 기대한다.
`"abc"` 같은 문자열이 전달되면 `MethodArgumentTypeMismatchException`이 발생하여 400을 반환해야 한다.
`ExceptionHandler` 설정에 따라 400이 아닌 500이 반환될 수 있으므로 명시적으로 검증한다.

```java
@Test
@WithMockUser
void updateConnectionStatus_문자열id_400반환() throws Exception {
    mockMvc.perform(patch("/api/v1/connections/abc/status")
               .contentType(MediaType.APPLICATION_JSON)
               .content("""
                   { "status": "CONNECTED" }
                   """))
           .andExpect(status().isBadRequest());
}

@Test
@WithMockUser
void deleteConnection_문자열id_400반환() throws Exception {
    mockMvc.perform(delete("/api/v1/connections/abc"))
           .andExpect(status().isBadRequest());
}
```

---

## 5. TC 작성 범위 기준

Controller TC는 "Spring MVC 레이어에서만 검증 가능한 것"에 집중한다.
비즈니스 로직 분기는 `ConnectionServiceTest`(MockitoExtension 기반)에서 이미 담당하므로 중복 작성하지 않는다.

| 케이스 | 작성 여부 | 이유 |
|--------|---------|------|
| 인증 없음 -> 401 | 작성 | Security 필터 동작 검증 |
| 정상 요청 -> 200/201 | 작성 | 라우팅 + JSON 직렬화 검증 |
| `@Valid` 검증 실패 -> 400 | 작성 | 검증 필터 + 응답 포맷 확인 |
| `@PathVariable` 타입 오류 -> 400 | 작성 | 런타임 타입 변환 오류 검증 |
| Service 예외 -> 4xx | 선택적 | 커스텀 `ExceptionHandler`가 있을 때만 |
| 비즈니스 로직 분기 (역할 제한, 중복 체크 등) | 작성하지 않음 | `ConnectionServiceTest`에서 담당 |
| Repository 쿼리 동작 | 작성하지 않음 | Repository 레이어 책임 |

---

## 6. ConnectionController 기준 TC 체크리스트

아래 TC를 `ConnectionControllerTest`에 작성하면 GET /api/v1/connections 500 에러를 포함한
런타임 오류를 사전에 잡을 수 있다.

### 인증 검증

- [ ] `getConnections` — 인증 없음 -> 401
- [ ] `addConnection` — 인증 없음 -> 401
- [ ] `updateConnectionStatus` — 인증 없음 -> 401
- [ ] `updateConnectionName` — 인증 없음 -> 401
- [ ] `deleteConnection` — 인증 없음 -> 401

### 정상 동작 및 직렬화 검증

- [ ] `getConnections` — `@WithMockUser` -> 200, `connections` 배열 직렬화 정상 (`ZonedDateTime`, `Role` enum 포함)
- [ ] `addConnection` — 정상 요청 -> 201, `AddConnectionRespDto` 직렬화 정상
- [ ] `updateConnectionStatus` — 정상 요청 -> 200
- [ ] `updateConnectionName` — 정상 요청 -> 200
- [ ] `deleteConnection` — 정상 요청 -> 204, 응답 바디 없음

### 요청 검증

- [ ] `addConnection` — 빈 `targetPhone` -> 400 (`@Valid` 검증)
- [ ] `updateConnectionStatus` — `status` 미전달 -> 400 (`@Valid` 검증)
- [ ] `updateConnectionStatus` — `id`에 문자열(`"abc"`) 전달 -> 400 (`PathVariable` 타입 오류)
- [ ] `updateConnectionName` — `id`에 문자열 전달 -> 400
- [ ] `deleteConnection` — `id`에 문자열 전달 -> 400

---

## 7. 테스트 파일 위치 및 네이밍 규칙

### 파일 위치

Controller TC는 Service TC와 별도 패키지(`controller/`)에 위치한다.

```
src/test/java/com/guegue/duty_checker/
└── connection/
    ├── service/
    │   └── ConnectionServiceTest.java     <- MockitoExtension 기반 (기존)
    └── controller/
        └── ConnectionControllerTest.java  <- @WebMvcTest 기반 (신규)
```

다른 도메인도 동일한 패턴을 따른다.

```
src/test/java/com/guegue/duty_checker/
├── auth/
│   ├── service/
│   │   └── AuthServiceTest.java
│   └── controller/
│       └── AuthControllerTest.java
└── checkin/
    ├── service/
    │   └── CheckInServiceTest.java
    └── controller/
        └── CheckInControllerTest.java
```

### 네이밍 규칙

| 항목 | 규칙 | 예시 |
|------|------|------|
| 클래스명 | `{Controller명}Test` | `ConnectionControllerTest` |
| 메서드명 | `메서드명_상황_기대결과` (CLAUDE.md 기준) | `getConnections_인증없음_401반환` |
| 어노테이션 | `@WebMvcTest({Controller}.class)` | `@WebMvcTest(ConnectionController.class)` |

### Service TC와 Controller TC 역할 구분

| 항목 | `ConnectionServiceTest` | `ConnectionControllerTest` |
|------|------------------------|---------------------------|
| 어노테이션 | `@ExtendWith(MockitoExtension.class)` | `@WebMvcTest(ConnectionController.class)` |
| Spring Context | 불필요 | 필요 (MVC 레이어) |
| 검증 대상 | 비즈니스 로직, 예외 발생 조건 | 라우팅, 인증, 직렬화, 요청 검증 |
| Service Mock | `@Mock` | `@MockBean` |

---

*Created: 2026-04-18*
