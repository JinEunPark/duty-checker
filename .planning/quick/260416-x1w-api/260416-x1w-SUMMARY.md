---
phase: quick-260416-x1w
plan: 01
status: complete
completed: 2026-04-16
commit: 84d916e
pr: https://github.com/JinEunPark/duty-checker/pull/82
issue: https://github.com/JinEunPark/duty-checker/issues/81
---

# Quick Task 260416-x1w: 비밀번호 변경 API 구현 Summary

**One-liner:** JWT 인증 기반 PATCH /auth/password 엔드포인트 — 현재 비밀번호 검증 후 bcrypt 암호화된 새 비밀번호로 교체

## Tasks Completed

| Task | Description | Status |
|------|-------------|--------|
| 1 | GitHub 이슈 #81 생성 및 루트 명세서(#15) 반영, feature/81 브랜치 생성 | Done |
| 2 | ChangePasswordReqDto, User.updatePassword, AuthService.changePassword, AuthController PATCH /password, 테스트 2개 구현 | Done |
| 3 | ./gradlew clean build 통과, 커밋, PR 생성 | Done |

## Key Files

**Created:**
- `src/main/java/com/guegue/duty_checker/auth/dto/ChangePasswordReqDto.java`

**Modified:**
- `src/main/java/com/guegue/duty_checker/user/domain/User.java` — updatePassword 도메인 메서드 추가
- `src/main/java/com/guegue/duty_checker/auth/service/AuthService.java` — changePassword, validateCurrentPassword 메서드 추가
- `src/main/java/com/guegue/duty_checker/auth/controller/AuthController.java` — PATCH /password 엔드포인트 + Swagger 어노테이션 추가
- `src/test/java/com/guegue/duty_checker/auth/service/AuthServiceTest.java` — changePassword 테스트 2개 추가

## Decisions Made

- `@SecurityRequirement(name = "BearerAuth")`를 클래스 레벨이 아닌 changePassword 메서드에만 적용하여 기존 public 엔드포인트(register, login 등)의 Swagger 표시에 영향을 주지 않음

## Deviations from Plan

None — plan executed exactly as written.

## Self-Check: PASSED

- ChangePasswordReqDto.java: FOUND
- User.updatePassword method: FOUND
- AuthService.changePassword method: FOUND
- AuthController PATCH /password endpoint: FOUND
- AuthServiceTest changePassword tests: FOUND
- Commit 84d916e: FOUND
- PR #82: FOUND
