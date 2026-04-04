# TODO

## 내가 요청해야 할 것

### 1. TC 작성
전체 Service 비즈니스 로직에 대한 단위 테스트 작성 요청.
- `AuthService` — 인증코드 쿨다운, 시도 횟수 초과, 전화번호 미인증 시 가입 거부 등
- `ConnectionService` — 역할별 목록 분기, 권한 없는 이름 수정 거부
- `CheckInService` — 역할 제한, 하루 1회 중복 방지
- `NotificationService` — FCM 토큰 없는 보호자 스킵, 중복 발송 방지

### 2. 실제 FCM 연동
현재 `MockFcmProvider` (로그 출력만). Firebase Admin SDK 연동 필요.
- `FirebaseFcmProvider` 구현 (`FcmProvider` 인터페이스 교체)
- Firebase 서비스 계정 키 설정

---

## 코드에서 빠진 것 (구현 필요)

### 🚨 보호자 추가 API 없음
`Connection` 엔티티는 있지만 생성하는 API가 없다.
당사자가 보호자 전화번호를 입력해 등록하는 API 필요.
- 이슈 생성 후 구현 필요: `POST /connections` (당사자가 보호자 전화번호 입력)
- 보호자가 미가입 상태면 `PENDING`, 가입된 상태면 즉시 `CONNECTED` 처리

### 🚨 PENDING → CONNECTED 전환 로직 없음
`Connection.connectGuardian()` 메서드는 있지만 호출하는 곳이 없다.
보호자가 회원가입(`POST /auth/register`)할 때, 자신의 전화번호로 등록된 PENDING 연결을 CONNECTED로 전환하는 로직이 필요.
- `AuthService.register()` 또는 별도 도메인 이벤트에서 처리

### ⚠️ AuthService가 UserRepository 직접 접근
도메인 경계 규칙 위반 (ARCHITECTURE.md 기준).
`AuthService`가 `UserRepository`를 직접 사용 중 — `UserService` 경유로 리팩토링 필요.
