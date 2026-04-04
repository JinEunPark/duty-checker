## API 명세 관리

API 명세는 GitHub Issue에서 관리한다.

- **개별 API 명세:** 각 API마다 별도 이슈로 관리 (`[API]` 라벨)
- **루트 명세서:** `JinEunPark/duty-checker` 이슈 #15 — 전체 API 목록 및 공통 스펙(Base URL, 인증 방식, 공통 응답 포맷) 포함

**명세 변경 시 반영 규칙:**
1. 개별 API 이슈의 Request/Response 스펙을 수정한다
2. 변경 내용이 공통 스펙(응답 포맷, 인증 방식 등)에 해당하면 루트 명세서(이슈 #15)도 함께 수정한다
3. `ARCHITECTURE.md`의 내용과 명세가 충돌할 경우, `ARCHITECTURE.md`를 기준으로 명세를 맞춘다

---

## 작업 흐름
모든 작업 요청은 아래 순서로 진행한다.
1. **GitHub Issue 확인 또는 생성**
   - **API 구현 요청 시:** 반드시 GitHub Issue에 등록된 API 명세서를 먼저 조회한다 (`gh api repos/JinEunPark/duty-checker/issues/{number}`)
     - 이슈를 새로 생성하지 않고 기존 이슈 번호를 브랜치/커밋에 사용한다
     - 루트 명세서 이슈 #15에서 전체 API 목록 확인 가능
   - **그 외 작업 요청 시:** 작업 내용을 정리하여 이슈를 새로 생성한다
     - 제목: 작업 요약 (한 줄)
     - 본문: 작업 배경, 구현 내용, 완료 조건
2. **Branch 생성**
   - `main` 브랜치로 이동 후 최신 상태로 pull 받고 브랜치를 생성한다
     ```
     git checkout main && git pull origin main
     git checkout -b feature/{issue-number}
     ```
   - 이슈 번호 기반으로 브랜치 생성
   - 네이밍: `feature/{issue-number}`
   - 예: `feature/6`
3. **작업 수행**
   - 생성한 브랜치에서 코드 작업 진행
   - **API 구현 시:** GitHub Issue의 Request/Response 스펙, 비즈니스 규칙, 에러 응답을 기준으로 구현한다
4. **커밋**
   - 형식: `#{issue-number} {커밋 내용}`
   - 예: `#6 implement POST /auth/register`
5. **PR 생성**
   - 작업 완료 후 `main` 브랜치 대상으로 PR 생성
   - PR 본문에 `Closes #{issue-number}` 포함
   - PR 생성 후 사용자에게 검토 및 머지 요청
6. **Merge**
   - 사용자가 직접 PR에서 머지 버튼을 누른다
