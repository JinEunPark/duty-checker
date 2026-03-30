## 작업 흐름
모든 작업 요청은 아래 순서로 진행한다.
1. **GitHub Issue 생성**
   - 작업 내용을 정리하여 `JinEunPark/duty-checker` 레포에 이슈 생성
   - 제목: 작업 요약 (한 줄)
   - 본문: 작업 배경, 구현 내용, 완료 조건
   - **예외: API 구현 요청 시 이미 이슈에 API 명세서가 작성되어 있으므로 이슈를 새로 생성하지 않고 기존 이슈를 기반으로 작업한다**
2. **Branch 생성**
   - 이슈 번호 기반으로 브랜치 생성
   - 네이밍: `feature/{issue-number}`
   - 예: `feature/2`
3. **작업 수행**
   - 생성한 브랜치에서 코드 작업 진행
4. **커밋**
   - 형식: `#{issue-number} {커밋 내용}`
   - 예: `#2 add spring-boot-starter-web dependency`
5. **PR 생성**
   - 작업 완료 후 `main` 브랜치 대상으로 PR 생성
   - PR 본문에 `Closes #{issue-number}` 포함
   - PR 생성 후 사용자에게 검토 및 머지 요청
6. **Merge**
   - 사용자가 직접 PR에서 머지 버튼을 누른다
