# Roadmap: 오늘, 안부

**Project:** 오늘, 안부
**Milestone:** v1.0 — BE API MVP
**Created:** 2026-03-29

---

## Phase 1: 프로젝트 기반 구조 (Architecture & Scaffolding)

**Goal:** 사용자와 함께 아키텍처를 확정하고, 합의된 내용을 ARCHITECTURE.md에 문서화한 뒤 프로젝트 기반 구조 구현

**Delivers:**
- `ARCHITECTURE.md` — 사용자와 협의하여 확정한 레이어 구조, 패키지 컨벤션, 의존성 방향, 공통 응답 포맷 문서화
- 레이어드 아키텍처 패키지 구조 (`presentation`, `application`, `domain`, `infrastructure`)
- 공통 응답 포맷 — ARCHITECTURE.md 확정 후 구현
- 글로벌 예외 처리 (`@RestControllerAdvice`)
- 기본 로깅 설정 (Logback)
- Health check 엔드포인트

**Process:**
1. 사용자와 아키텍처 및 공통 응답 포맷 협의
2. 협의 결과를 `ARCHITECTURE.md`에 문서화 및 사용자 최종 확인
3. 확정된 문서 기반으로 코드 구현

**Requirements covered:** ARCH-01, ARCH-02

**Verification:** 서버 기동 및 `/health` 응답 확인, 패키지 구조가 ARCHITECTURE.md와 일치

---

## Phase 2: SMS 인증 로그인

**Goal:** 전화번호 기반 SMS 인증 로그인 API 완성

**Delivers:**
- SMS 인증코드 발송 API (`POST /api/v1/auth/send-code`)
- 인증코드 검증 + 회원가입/로그인 완료 시 JWT 발급 API (`POST /api/v1/auth/verify-code`)
- 인증코드 재발송 API
- JWT 액세스 토큰 발급 / Spring Security 필터 설정
- 인증코드 유효시간(5분), 발송 3회 초과 시 30분간 추가 발송 차단 로직
- SMS provider 연동 (SmsProvider 추상화 인터페이스 + MockSmsProvider 구현) — 실제 CoolSMS 연동은 추후 별도 안내 후 교체

**Requirements covered:** AUTH-01, AUTH-02, AUTH-03, AUTH-04

**Verification:** 인증코드 발송 → 입력 → JWT 발급 플로우 API 테스트 통과

---

## Phase 3: 사용자 등록 및 역할 관리

**Goal:** 역할 선택(인증 전) 및 SMS 인증 완료 후 회원가입/프로필 관리

**Delivers:**
- 사용자 역할 선택 API (`POST /api/v1/users/role`)
- 당사자 등록 시 초기 보호자 등록 API (최대 5명)
- 역할 변경 API
- 내 정보 조회 API (`GET /api/v1/users/me`)
- `User`, `Guardian` 도메인 엔티티 및 JPA Repository

**Requirements covered:** USER-01, USER-02, USER-03, USER-04

**Verification:** 역할 선택 → 보호자 등록 → 내 정보 조회 흐름 테스트

---

## Phase 4: 보호자 관리

**Goal:** 보호자 추가/삭제/이름수정/상태 조회 API

**Delivers:**
- 보호자 추가 API (`POST /api/v1/guardians`)
- 보호자 삭제 API (`DELETE /api/v1/guardians/{id}`)
- 보호자 이름 수정 API (`PATCH /api/v1/guardians/{id}`)
- 보호자 목록 조회 API (`GET /api/v1/guardians`) — 연결 상태(PENDING/CONNECTED) 포함
- 보호자 최대 5명 제한 validation

**Requirements covered:** GRDNM-01, GRDNM-02, GRDNM-03, GRDNM-04

**Verification:** 보호자 CRUD 및 상태 조회 API 테스트 통과, 5명 초과 시 에러 응답 확인

---

## Phase 5: 안부 확인 (Check-in)

**Goal:** 당사자의 일일 안부 확인 버튼 기능

**Delivers:**
- 안부 확인 API (`POST /api/v1/checkins`) — 횟수 제한 없음, 누를 때마다 기록 생성
- 오늘 안부 상태 조회 API (`GET /api/v1/checkins/today`)
- `Checkin` 도메인 엔티티 — 날짜별 기록
- 자정 KST 기준 당일 중복 확인 방지 로직

**Requirements covered:** CHKIN-01, CHKIN-02, CHKIN-03

**Verification:** 안부 확인 버튼 누름 → 당일 상태 조회 정확성 테스트

---

## Phase 6: 보호자 FCM 푸시 알림

**Goal:** 안부 미확인 시 보호자 알림 발송 (FCM Mock 구현, 실제 연동은 추후)

**Delivers:**
- FCM 디바이스 토큰 등록/갱신 API (`POST /api/v1/devices/token`)
- 알림 임계시간 초과 감지 스케줄러 (`@Scheduled`) — 임계시간은 보호자별 DB 컬럼에서 읽음 (기본 24시간)
- FCM 발송 서비스 — Mock 구현 (FcmProvider 추상화 인터페이스 + MockFcmProvider)
- 당일 이미 확인된 경우 알림 스킵 로직
- 알림 발송 이력 로깅
- 실제 Firebase 인증 정보 연동 시 교체 방법 안내 요청

**Requirements covered:** NOTF-01, NOTF-02, NOTF-03, NOTF-04

**Verification:** 스케줄러 트리거 시 미확인 당사자의 보호자에게만 FCM 발송 확인, 확인된 경우 스킵 검증

---

## Summary

| Phase | Title | Requirements | Status |
|-------|-------|-------------|--------|
| 1 | 프로젝트 기반 구조 | ARCH-01, ARCH-02 | Pending |
| 2 | SMS 인증 로그인 | AUTH-01~04 | Pending |
| 3 | 사용자 등록 및 역할 | USER-01~04 | Pending |
| 4 | 보호자 관리 | GRDNM-01~04 | Pending |
| 5 | 안부 확인 | CHKIN-01~03 | Pending |
| 6 | FCM 푸시 알림 | NOTF-01~04 | Pending |

**Total phases:** 6
**Total requirements covered:** 21/21 ✓

---
*Roadmap created: 2026-03-29*
*Last updated: 2026-03-30*
*Next action: `/gsd:plan-phase 1`*
