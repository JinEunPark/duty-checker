# 레이어 흐름 시퀀스 다이어그램

도메인별 MVC 아키텍처에서 각 레이어 간 데이터 흐름을 정의합니다.

## 다이어그램 목록

| 파일 | 설명 |
|------|------|
| [basic-api-flow.puml](diagrams/basic-api-flow.puml) | 기본 API 흐름 (DB 접근만 있는 경우) |
| [external-api-flow.puml](diagrams/external-api-flow.puml) | 외부 API 연동 흐름 (OpenFeign + CoolSMS) |
| [dto-responsibility.puml](diagrams/dto-responsibility.puml) | DTO 변환 책임 요약 |

---

## DTO 명명 규칙

각 API는 독립적인 DTO를 가지며, 변경 시 다른 API에 영향을 주지 않는다.

| API | ReqDto | RespDto |
|-----|--------|---------|
| POST /auth/send-code | `SendCodeReqDto` | `SendCodeRespDto` |
| POST /auth/verify-code | `VerifyCodeReqDto` | `VerifyCodeRespDto` |
| POST /users/role | `SelectRoleReqDto` | `SelectRoleRespDto` |
| POST /guardians | `AddGuardianReqDto` | `AddGuardianRespDto` |
| DELETE /guardians/{id} | — | `DeleteGuardianRespDto` |
| PATCH /guardians/{id} | `UpdateGuardianReqDto` | `UpdateGuardianRespDto` |
| GET /guardians | — | `GuardianListRespDto` |
| POST /checkins | — | `CheckinRespDto` |
| GET /checkins/today | — | `TodayCheckinRespDto` |
| POST /devices/token | `RegisterTokenReqDto` | `RegisterTokenRespDto` |

---

*Created: 2026-03-30*
