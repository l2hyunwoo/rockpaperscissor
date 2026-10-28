# Shared UI

메모 목록·상세·편집 화면을 Compose Multiplatform으로 제공하는 라이브러리입니다. 앱인토스·Android·iOS·독립 웹에서 같은 화면을 사용할 수 있습니다.

## 화면과 상태

| API | 역할 |
| --- | --- |
| `ServiceApp(...)` | 목록·상세 흐름, 데이터 조회, 저장·공유 동작 구성 |
| `NotesList(notes, onSelect, modifier)` | 메모 목록 화면 |
| `NoteDetail(note, modifier, storage, onShare)` | 메모 편집·저장·공유 화면 |
| `ServiceNavigation` | 선택한 메모와 목록 복귀 상태 |

필요한 화면만 사용하거나 `ServiceApp`으로 함께 구성합니다. `NotesRepository`와 `DraftStorage`는 [공통 core](../shared-core/README.md)의 계약입니다. 기본 조회 구현은 데모 데이터를 반환하고 기본 저장은 메모리를 사용합니다.

`onShare`에는 호스트의 공유 동작을, `hostContent`에는 호스트 전용 UI를 전달합니다. `showBackButton`으로 공통 화면의 목록 복귀 버튼을 제어합니다. 저장은 사용자가 `저장해요`를 누를 때 수행합니다.

## 디자인

Compose Material 3 테마와 Noto Sans KR을 사용합니다. 토스·TDS 의존성은 없습니다. 한글 폰트를 리소스에 포함하며 [폰트 라이선스](src/commonMain/composeResources/files/NotoSansKR-OFL.txt)를 함께 배포합니다.

Android·iOS·웹 호스트는 플랫폼의 저장·공유·생명주기를 연결합니다. 화면의 재사용과 계정·인증·서버 동기화는 별도 책임입니다.
