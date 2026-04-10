## 문서 작성 규칙

문서 작성 요청 시 Notion 페이지를 생성하여 작업한다.

- **기본 부모 페이지:** https://www.notion.so/32a944dd59bc80dfaf44ef5e78eb7021
- 별도 지정이 없으면 위 페이지 하위에 새 페이지를 만든다
- 문서 작성 후 생성된 Notion 페이지 URL을 사용자에게 알려준다

---

## API 명세 관리

API 명세는 GitHub Issue에서 관리한다.

- **개별 API 명세:** 각 API마다 별도 이슈로 관리 (`[API]` 라벨)
- **루트 명세서:** `JinEunPark/duty-checker` 이슈 #15 — 전체 API 목록 및 공통 스펙(Base URL, 인증 방식, 공통 응답 포맷) 포함

**명세 변경 시 반영 규칙:**
1. 개별 API 이슈의 Request/Response 스펙을 수정한다
2. 변경 내용이 공통 스펙(응답 포맷, 인증 방식 등)에 해당하면 루트 명세서(이슈 #15)도 함께 수정한다
3. `ARCHITECTURE.md`의 내용과 명세가 충돌할 경우, `ARCHITECTURE.md`를 기준으로 명세를 맞춘다

**명세 이슈 생성 규칙:**
- 기존 API의 명세가 변경되는 경우 → **새 이슈를 만들지 말고 기존 이슈를 직접 수정**한다
- 완전히 새로운 API가 추가되는 경우에만 새 이슈를 생성한다
- 이슈를 이원화하지 않는다. 하나의 API = 하나의 이슈
- **새 API 이슈 생성 시 루트 명세서(이슈 #15)에도 반드시 반영한다** (`gh api repos/JinEunPark/duty-checker/issues/15 --method PATCH --field body='...'`)

---

## 테스트 작성 규칙

모든 비즈니스 로직 구현 시 Service 단위 테스트를 함께 작성한다.

**작성 대상:**
- 분기가 있는 비즈니스 규칙 (역할 제한, 중복 체크, 상태 검증 등)
- 에러 케이스 (예외 발생 조건)
- 핵심 흐름 (정상 케이스 1개)

**작성 제외:**
- 단순 CRUD (조회 후 반환만 하는 로직)
- Controller 레이어 (라우팅만 담당)
- Repository 쿼리 메서드

**테스트 방식:**
- `@ExtendWith(MockitoExtension.class)` 기반 순수 단위 테스트
- 외부 의존성(Repository, 다른 Service)은 `@Mock`으로 대체
- Spring Context 로딩(`@SpringBootTest`) 금지 — 느리고 불필요
- 테스트명: `메서드명_상황_기대결과` 형식 (한글 가능)

**작성 시점:**
- 구현 완료 후 커밋 전에 작성
- 테스트 파일은 구현 커밋과 같은 커밋 또는 직후 별도 커밋으로 포함

---

## Swagger 문서화 규칙

모든 API 구현 시 Swagger 어노테이션을 반드시 작성한다.

**Controller 클래스:**
- `@Tag(name = "...", description = "...")` — Controller 단위 태그

**각 API 메서드:**
- `@Operation(summary = "...", description = "...")` — 한 줄 요약 + 상세 설명
- `@ApiResponses({ @ApiResponse(...), ... })` — 가능한 모든 응답 코드와 설명

**인증이 필요한 API:**
- Controller 클래스에 `@SecurityRequirement(name = "BearerAuth")` 추가

**Path Variable:**
- `@Parameter(description = "...")` 추가

**작성 기준:**
- summary: 동사+목적어 형식으로 간결하게 (예: "체크인 생성", "연결 목록 조회")
- description: 비즈니스 규칙이나 제약 조건이 있으면 명시 (예: "당일 중복 체크인은 허용되지 않습니다")
- 에러 응답 코드는 실제 발생 가능한 케이스만 포함

---

## 빌드 검증 규칙

작업 완료 후 반드시 아래 순서를 따른다.

1. **빌드 및 테스트 실행**
   ```
   ./gradlew clean build
   ```
2. **오류 수정:** 빌드 또는 테스트 실패 시 원인을 파악하고 수정한다. 통과할 때까지 반복한다.

---

## 자동 커밋 규칙

> **MUST:** 코드 작업이 완료되면 사용자가 요청하지 않아도 반드시 커밋한다. 커밋 없이 작업을 마무리하는 것은 허용되지 않는다.

- 빌드와 테스트가 모두 통과한 직후 즉시 커밋한다
- 커밋 형식: `#{issue-number} {커밋 내용}`
- 커밋 후 사용자에게 커밋 완료 사실을 알린다

---

## 코드 작성 규칙

로직 구현 시 가독성과 재사용성을 위해 아래 기준에 따라 메서드를 분리한다.

**분리 기준:**
- 하나의 메서드가 하나의 역할만 담당하도록 작성한다
- 동일하거나 유사한 로직이 2곳 이상에서 반복되면 별도 메서드로 추출한다
- 메서드 내 로직이 길어져 흐름 파악이 어려워지면 의미 단위로 분리한다

**네이밍 기준:**
- 메서드 이름만 봐도 하는 일을 알 수 있어야 한다
- 동사+목적어 형식으로 작성한다 (예: `validateSubjectRole`, `buildConnectionItem`)
- `do`, `process`, `handle` 같은 범용 동사는 피한다

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
5. **검증**
   - PR 생성 전 반드시 아래 명령을 실행하여 빌드와 전체 테스트가 통과하는지 확인한다
     ```
     ./gradlew clean build
     ```
   - 빌드 또는 테스트 실패 시 원인을 수정한 후 다음 단계로 진행한다
6. **PR 생성**
   - 작업 완료 후 `main` 브랜치 대상으로 PR 생성
   - PR 본문에 `Closes #{issue-number}` 포함
   - PR 생성 후 사용자에게 검토 및 머지 요청
7. **Merge**
   - 사용자가 직접 PR에서 머지 버튼을 누른다
