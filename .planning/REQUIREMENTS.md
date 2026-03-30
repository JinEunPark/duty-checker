# Requirements: 오늘, 안부

**Defined:** 2026-03-29
**Core Value:** 당사자가 버튼 하나로 "오늘도 괜찮아요"를 보호자에게 전달할 수 있어야 한다.

## v1 Requirements

### Architecture (아키텍처 기반) — Phase 1

- [ ] **ARCH-01**: ARCHITECTURE.md 기반 도메인별 MVC 아키텍처로 프로젝트 구조를 설정한다 (도메인별 controller/service/domain/repository/dto/infrastructure 패키지)
- [ ] **ARCH-02**: 공통 응답 포맷, 예외 처리, 로깅 기반을 구성한다
- [ ] **ARCH-03**: 외부 API 연동(CoolSMS, FCM)은 OpenFeign을 사용한다

### Authentication (인증) — Phase 2

- [ ] **AUTH-01**: 사용자가 전화번호를 입력하면 SMS 인증코드를 받을 수 있다
- [ ] **AUTH-02**: 인증코드 인증 완료 및 회원가입 완료 시 JWT 액세스 토큰을 발급받는다
- [ ] **AUTH-03**: 인증코드는 5분 유효; 같은 사용자가 인증코드 발송을 3회 이상 요청하면 이후 30분간 추가 발송 불가
- [ ] **AUTH-04**: 인증코드 재전송 요청이 가능하다 (쿨다운 적용)

### User Registration (사용자 등록) — Phase 3

- [ ] **USER-01**: 앱 진입 시 당사자 또는 보호자 역할을 먼저 선택하고, 이후 SMS 인증을 진행한다
- [ ] **USER-02**: 당사자는 회원가입 시 보호자를 최대 5명 등록할 수 있다
- [ ] **USER-03**: 보호자는 당사자 전화번호로 식별되며 연결 대기(pending) 상태로 생성된다
- [ ] **USER-04**: 역할은 나중에 변경 가능하다

### Guardian Management (보호자 관리) — Phase 4

- [ ] **GRDNM-01**: 당사자는 보호자 전화번호를 추가 등록할 수 있다 (최대 5명)
- [ ] **GRDNM-02**: 당사자는 등록된 보호자를 삭제할 수 있다
- [ ] **GRDNM-03**: 당사자는 보호자에게 표시될 이름을 수정할 수 있다
- [ ] **GRDNM-04**: 보호자의 연결 상태(대기 중 / 연결됨)를 확인할 수 있다

### Check-in (안부 확인) — Phase 5

- [ ] **CHKIN-01**: 당사자는 "안부 확인" 버튼을 눌러 안부 기록을 생성한다 (횟수 제한 없음)
- [ ] **CHKIN-02**: 당일 안부 확인 여부 및 마지막 확인 시각을 조회할 수 있다
- [ ] **CHKIN-03**: 하루 기준은 자정(00:00 KST) 리셋; 당일 1회 이상 확인 시 "완료" 상태

### Notification (알림) — Phase 6

- [ ] **NOTF-01**: 마지막 안부 확인 후 알림 임계시간(기본 24시간)이 지나면 등록된 보호자에게 FCM 푸시 알림을 발송한다. 임계시간은 보호자별로 DB에 저장하며 추후 개별 설정 가능하도록 구조를 갖춘다 (v1은 24시간 고정)
- [ ] **NOTF-02**: 보호자 FCM 디바이스 토큰을 등록/갱신할 수 있다
- [ ] **NOTF-03**: 당일 안부가 이미 확인된 경우 알림 발송을 취소(스킵)한다
- [ ] **NOTF-04**: FCM 연동은 Mock 구현으로 대체하며, 실제 Firebase 인증 정보는 추후 별도 요청을 통해 교체한다

## v2 Requirements

### 보호자 앱 화면

- **GRDN-01**: 보호자가 앱에서 당사자 안부 현황을 확인할 수 있다
- **GRDN-02**: 보호자가 연결 요청을 수락/거절할 수 있다

### 알림 커스터마이징

- **NOTF-EXT-01**: 알림 발송 시간을 당사자가 설정할 수 있다
- **NOTF-EXT-02**: 반복 알림 발송 간격 설정

### 이력 조회

- **HIST-01**: 당사자의 안부 확인 이력을 날짜별로 조회할 수 있다
- **HIST-02**: 보호자가 당사자의 최근 30일 이력을 조회할 수 있다

## Out of Scope

| Feature | Reason |
|---------|--------|
| 소셜 로그인 (카카오/구글) | 전화번호 인증으로 충분, v1 복잡도 증가 |
| 보호자 앱 화면 | 피그마 v1 범위 미포함, v2로 이관 |
| 안부 미확인 반복 알림 | v1은 24h 1회 알림으로 충분 |
| 관리자 대시보드 | 운영 요구사항 미확정 |
| 실시간 채팅/영상통화 | 핵심 가치와 무관, 고복잡도 |
| 웹 클라이언트 | 모바일 앱 타겟 서비스 |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| ARCH-01 | Phase 1 | Pending |
| ARCH-02 | Phase 1 | Pending |
| AUTH-01 | Phase 2 | Pending |
| AUTH-02 | Phase 2 | Pending |
| AUTH-03 | Phase 2 | Pending |
| AUTH-04 | Phase 2 | Pending |
| USER-01 | Phase 3 | Pending |
| USER-02 | Phase 3 | Pending |
| USER-03 | Phase 3 | Pending |
| USER-04 | Phase 3 | Pending |
| GRDNM-01 | Phase 4 | Pending |
| GRDNM-02 | Phase 4 | Pending |
| GRDNM-03 | Phase 4 | Pending |
| GRDNM-04 | Phase 4 | Pending |
| CHKIN-01 | Phase 5 | Pending |
| CHKIN-02 | Phase 5 | Pending |
| CHKIN-03 | Phase 5 | Pending |
| NOTF-01 | Phase 6 | Pending |
| NOTF-02 | Phase 6 | Pending |
| NOTF-03 | Phase 6 | Pending |
| NOTF-04 | Phase 6 | Pending |

**Coverage:**
- v1 requirements: 21 total
- Mapped to phases: 21
- Unmapped: 0 ✓

---
*Requirements defined: 2026-03-29*
*Last updated: 2026-03-30 — reordered by phase, updated AUTH-02/03, USER-01, added NOTF-04*
