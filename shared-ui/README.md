# shared-ui

가위바위보 게임 화면을 Compose Multiplatform으로 제공하는 라이브러리입니다.

## 제공 API

| API | 역할 |
| --- | --- |
| `RpsApp(...)` | 기록 카드, 결과, 선택 버튼, 기록 초기화·공유, 종료 확인 모달 |

호스트가 사용자 키, 저장소, 종료 요청 상태, 공유 동작을 전달합니다. 로직은 [공통 core](../shared-core/README.md)를 사용합니다.
