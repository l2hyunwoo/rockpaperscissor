# 가위바위보

앱인토스에서 사용하는 Compose Multiplatform 메모 서비스입니다. 메모 목록을 보고 내용을 수정·저장할 수 있으며, 공통 화면과 서비스 로직을 다른 앱·웹에 재사용할 수 있습니다.

## 기능

- 메모 목록·상세·편집 화면
- 기기에 초안 저장과 텍스트 공유
- 앱인토스 전용 로그인·호스트 이벤트·안전 영역 연결

## 구성

| 모듈 | 역할 |
| --- | --- |
| [shared-core](shared-core/README.md) | 데이터 모델·조회 API·저장 계약 |
| [shared-ui](shared-ui/README.md) | 재사용 가능한 Compose 화면 |
| [appsInTossApp](appsInTossApp/README.md) | 토스 연동과 앱인토스 진입점 |

Android·iOS·독립 웹 호스트는 프로젝트 생성 시 선택한 경우에 포함됩니다. 공통 UI는 Compose Material 3을 사용하며 TDS는 포함하지 않습니다.

## 현재 제공 범위

조회는 데모 데이터를 사용하고 초안은 기기에 저장합니다. 운영 서버·계정 연동·서버 인증 교환은 별도로 연결해야 합니다. 개발용 mock의 로그인 결과는 인증에 사용할 수 없습니다.

[개발 가이드](docs/development.md)에서 실행·검사·GitHub Actions CI와 플랫폼 확장 방법을 확인할 수 있습니다.
