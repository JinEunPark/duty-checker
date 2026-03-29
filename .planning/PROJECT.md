# 오늘, 안부

## What This Is

매일 한 번 "안부 확인" 버튼을 눌러 보호자에게 생존 신호를 전달하는 일상 안전 확인 서비스.
당사자(본인)가 24시간 내 안부를 확인하지 않으면 등록된 보호자에게 푸시 알림이 전송된다.
독거 노인, 만성질환자, 혼자 생활하는 사람 등 주기적 안전 확인이 필요한 사람과 그 가족을 위한 서비스.

## Core Value

당사자가 버튼 하나로 "오늘도 괜찮아요"를 보호자에게 전달할 수 있어야 한다.

## Requirements

### Validated

(None yet — ship to validate)

### Active

- [ ] 전화번호 SMS 인증 기반 로그인 (당사자/보호자 공통)
- [ ] 당사자 역할 등록 — 본인 전화번호 인증 후 보호자 최대 5명 등록
- [ ] 안부 확인 버튼 — 당사자가 하루 1회 누르면 당일 안부 기록 생성
- [ ] 보호자 푸시 알림 — 마지막 안부 확인 후 24시간 경과 시 보호자에게 발송
- [ ] 보호자 관리 — 보호자 추가(전화번호 입력)/삭제/이름 수정, 연결 대기 상태 표시

### Out of Scope

- 보호자 역할 앱 화면 (v1 피그마에 미포함) — 별도 milestone에서 구현
- 안부 미확인 반복 알림 — v1은 24h 1회 알림만
- 소셜 로그인 — 전화번호 인증으로 충분하며 복잡도 증가
- 관리자 대시보드 — 운영 요구사항 미확정

## Context

- Spring Boot 4.0.5 + Java 21 기반 백엔드 (이미 프로젝트 스캐폴딩 완료)
- 피그마 기획: https://www.figma.com/design/abiURs4mBIS9bgKMPdTWg2/%EC%95%88%EB%B6%80
- 모바일 앱과 통신하는 REST API 서버 역할
- SMS 인증코드 발송을 위한 외부 SMS provider 연동 필요
- 보호자 푸시 알림을 위한 FCM(Firebase Cloud Messaging) 연동 필요
- 레이어드 아키텍처(Layered Architecture) 적용 예정 — ARCHITECTURE.md 별도 작성

## Constraints

- **Tech Stack**: Spring Boot 4.0.5, Java 21 — 변경 없음
- **Auth**: 전화번호 + SMS 인증코드 방식만 지원 (v1)
- **Guardian limit**: 보호자 최대 5명 — 피그마 명시
- **Check-in window**: 안부 확인 기준은 마지막 확인 후 24시간
- **BE only**: 이번 milestone은 백엔드 API만 구현 (FE 별도)

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| 레이어드 아키텍처 채택 | Spring 생태계 표준, 팀 학습 곡선 최소화 | — Pending |
| 전화번호 기반 인증 | 피그마 설계 확정, 별도 계정 관리 불필요 | — Pending |
| 보호자 알림 = FCM 푸시 | 앱 타겟 서비스, SMS보다 비용 효율적 | — Pending |

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition** (via `/gsd:transition`):
1. Requirements invalidated? → Move to Out of Scope with reason
2. Requirements validated? → Move to Validated with phase reference
3. New requirements emerged? → Add to Active
4. Decisions to log? → Add to Key Decisions
5. "What This Is" still accurate? → Update if drifted

**After each milestone** (via `/gsd:complete-milestone`):
1. Full review of all sections
2. Core Value check — still the right priority?
3. Audit Out of Scope — reasons still valid?
4. Update Context with current state

---
*Last updated: 2026-03-29 after initialization*
