---
created: 2026-04-05T01:49:59.854Z
title: 민감 정보 .env에서 GitHub Secret으로 이관
area: general
files:
  - .github/workflows/
  - docker-compose.yml
---

## Problem

현재 DB 계정 정보 등 민감 정보가 .env 파일로 관리되고 있어 보안상 취약. CI/CD 파이프라인에서 GitHub Secret을 통해 주입하는 방식으로 변경 필요.

## Solution

1. GitHub Repository → Settings → Secrets에 필요한 값 등록 (DB_PASSWORD, DB_USER 등)
2. GitHub Actions workflow에서 `${{ secrets.XXX }}` 형태로 주입
3. docker-compose.yml에서 환경변수로 받도록 수정
4. .env 파일 의존성 제거
