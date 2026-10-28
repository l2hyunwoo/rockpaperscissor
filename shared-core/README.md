# Shared Core

메모 서비스의 데이터 모델, 데이터 조회 API와 초안 저장 계약을 담은 공통 Kotlin 라이브러리입니다. Compose·토스·TDS·브라우저 타입에 의존하지 않습니다.

## 구성

| 타입·함수 | 역할 |
| --- | --- |
| `Note(id, title, body)` | 메모 데이터 모델 |
| `NotesRepository.list()` | 메모 목록 조회 계약 |
| `NotesApiClient(getNotesJson)` | 주입받은 suspend 통신 함수의 JSON 응답 해석 |
| `DemoNotesRepository` | 로컬 예제 데이터 제공 |
| `DraftStorage.read/write` | 메모 ID별 초안 저장 계약 |
| `MemoryDraftStorage` | 실행 중 메모리에 보관하는 저장 구현 |
| `BrowserNote`, `decodeNotesResponse(json)` | JS로 내보내는 데이터 타입과 응답 해석 함수 |

`NotesApiClient`를 사용할 때 HTTPS·인증·플랫폼별 통신은 주입한 함수가 담당합니다. JSON 응답은 `id`, `title`, `body` 문자열을 갖는 메모 배열입니다. 전송·JSON 해석 오류는 호출자에게 전달합니다. 계정별 데이터 분리와 서버 저장은 서비스가 구현해야 합니다.

`MemoryDraftStorage`는 앱 재시작 후 데이터를 유지하지 않습니다. 영구 저장이 필요하면 호스트가 `DraftStorage` 구현을 전달합니다. 화면 연결은 [공통 UI](../shared-ui/README.md)를 참고하세요.
