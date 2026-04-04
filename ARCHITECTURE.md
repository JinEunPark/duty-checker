# Architecture: 오늘, 안부

Spring Boot 4.0.5 + Java 21 기반 백엔드 아키텍처 정의서.

---

## 1. 아키텍처 개요

도메인별 MVC 구조를 적용한다. 각 도메인이 자신의 Controller / Service / Domain / Repository / DTO / Infrastructure를 독립적으로 소유한다.

---

## 2. 패키지 구조

```
com.guegue.duty_checker
├── auth/
│   ├── controller/       ← AuthController
│   ├── service/          ← AuthService
│   ├── domain/           ← SmsCode (Entity)
│   ├── repository/       ← SmsCodeRepository (JPA)
│   ├── dto/              ← SendCodeReqDto, SendCodeRespDto, ...
│   └── infrastructure/   ← CoolSmsClient (OpenFeign), CoolSmsRequest/Response
├── user/
│   ├── controller/
│   ├── service/
│   ├── domain/           ← User (Entity)
│   ├── repository/
│   └── dto/
├── guardian/
│   ├── controller/
│   ├── service/
│   ├── domain/           ← Guardian (Entity)
│   ├── repository/
│   └── dto/
├── checkin/
│   ├── controller/
│   ├── service/
│   ├── domain/           ← Checkin (Entity)
│   ├── repository/
│   └── dto/
├── notification/
│   ├── scheduler/        ← NotificationScheduler
│   ├── service/          ← NotificationService
│   ├── domain/           ← NotificationLog (Entity)
│   ├── repository/
│   ├── dto/
│   └── infrastructure/   ← FcmProvider (interface), MockFcmProvider, FcmClient (OpenFeign)
└── common/
    ├── exception/        ← BusinessException, GlobalExceptionHandler, ErrorCode
    ├── response/         ← ErrorResponse
    └── config/           ← Security, OpenFeign, 기타 설정
```

> `infrastructure/` 패키지는 외부 API 연동이 있는 도메인(`auth`, `notification`)에만 존재한다.

---

## 3. 레이어 책임

### Controller
- HTTP 요청 수신 및 응답 반환
- `@Valid` 입력 검증
- Service 호출만 담당 — 비즈니스 로직 없음
- 성공 시 RespDto를 그대로 반환 (래퍼 없음)

### Service
- **ReqDto 수신 → Entity 변환** (변환 책임은 Service)
- Entity 간 OOP 메시지 전달로 도메인 로직 수행
- Repository 및 Provider 호출
- **Entity → RespDto 변환** (변환 책임은 Service)
- `@Transactional` 적용 — Entity Lazy Loading 주의
- **도메인 경계 규칙**: 각 Service는 자신의 도메인 Repository만 직접 접근한다. 다른 도메인의 데이터가 필요할 경우 해당 도메인의 Service를 통해 접근한다.
  ```
  // 금지 — 다른 도메인 Repository 직접 접근
  @Autowired UserRepository userRepository; // in ConnectionService

  // 허용 — 다른 도메인 Service를 통해 접근
  @Autowired UserService userService; // in ConnectionService
  ```

### Repository
- JPA Repository interface
- Entity 단위 입출력: `List<Entity>`, `Optional<Entity>`
- 쿼리 로직만 — 비즈니스 로직 없음

### Infrastructure
- 외부 API 연동 구현체 (OpenFeign 사용)
- Provider 인터페이스를 구현
- 외부 API 전용 DTO 소유 (도메인 DTO와 분리)

```
SmsProvider (interface)
├── MockSmsProvider          ← 개발/테스트용
└── CoolSmsSmsProvider       ← 실제 CoolSMS 연동 (추후 교체)

FcmProvider (interface)
├── MockFcmProvider          ← 개발/테스트용
└── FirebaseFcmProvider      ← 실제 FCM 연동 (추후 교체)
```

---

## 4. DTO 흐름

```
Client → ReqDto → Controller → Service → Entity (변환, 도메인 로직)
                                       → Repository (Entity 입출력)
                                       → Provider (외부 연동)
                               Service → RespDto (변환)
Client ← RespDto ← Controller ← Service
```

### DTO 규칙
- **API별 독립 DTO** — 하나의 ReqDto/RespDto를 여러 API가 공유하지 않는다
- **명명 규칙** — `{동사}{대상}ReqDto`, `{동사}{대상}RespDto`
  - 예: `SendCodeReqDto`, `VerifyCodeRespDto`, `AddGuardianReqDto`
- **위치** — 각 도메인의 `dto/` 패키지

---

## 5. 공통 응답 포맷

### 성공
RespDto를 직접 반환한다. 공통 래퍼 없음.

```json
// 200 OK
{
  "checkedAt": "2026-03-30T10:00:00"
}
```

### 실패
`ErrorResponse`로 통일한다.

```json
// 4xx / 5xx
{
  "code": "AUTH_001",
  "message": "인증코드가 만료되었습니다"
}
```

---

## 6. 에러 처리

### ErrorCode Enum
HTTP 상태코드, 에러 코드 문자열, 기본 메시지를 함께 관리한다.

```java
public enum ErrorCode {
    // Auth
    AUTH_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "AUTH_001", "인증코드가 만료되었습니다"),
    AUTH_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH_002", "인증코드가 일치하지 않습니다"),
    AUTH_SEND_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "AUTH_003", "발송 횟수를 초과했습니다. 30분 후 재시도해주세요"),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "사용자를 찾을 수 없습니다"),

    // Guardian
    GUARDIAN_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "GRDNM_001", "보호자는 최대 5명까지 등록 가능합니다"),

    // Common
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "서버 오류가 발생했습니다");
}
```

### 예외 발생
```java
throw new BusinessException(ErrorCode.AUTH_CODE_EXPIRED);
```

### GlobalExceptionHandler
`@RestControllerAdvice`에서 `BusinessException`을 잡아 `ErrorResponse`로 변환한다.

---

## 7. 외부 연동

모든 외부 API 연동은 **OpenFeign**을 사용한다.

| 외부 서비스 | Provider Interface | Mock 구현체 | 실제 구현체 |
|------------|-------------------|------------|------------|
| CoolSMS | `SmsProvider` | `MockSmsProvider` | `CoolSmsSmsProvider` |
| FCM | `FcmProvider` | `MockFcmProvider` | `FirebaseFcmProvider` |

- Mock 구현체는 로그만 출력하고 실제 발송하지 않는다
- 실제 연동 시 Provider 구현체만 교체하면 된다 (Service 코드 변경 없음)

---

## 8. 레이어 흐름 다이어그램

→ [`docs/LAYER_FLOW.md`](docs/LAYER_FLOW.md) 참고

---

*Created: 2026-03-30*
