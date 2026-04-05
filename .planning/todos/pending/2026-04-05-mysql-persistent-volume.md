---
created: 2026-04-05T01:49:59.854Z
title: MySQL 데이터 볼륨 마운트로 영속성 확보
area: general
files:
  - docker-compose.yml
---

## Problem

현재 MySQL 데이터가 컨테이너 이미지에 종속되어 있어, 이미지가 삭제되거나 재빌드 시 DB 데이터가 날아가는 문제가 있음.

## Solution

docker-compose.yml에서 MySQL 서비스에 named volume 또는 host path volume을 설정하여 컨테이너가 내려가거나 이미지가 교체되어도 디스크에서 데이터를 유지할 수 있도록 변경한다.

예시:
```yaml
volumes:
  - mysql-data:/var/lib/mysql

volumes:
  mysql-data:
```
