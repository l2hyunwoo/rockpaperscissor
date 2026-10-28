# 가위바위보

앱인토스에서 실행하는 Compose Multiplatform 가위바위보 게임입니다. 판정·기록 로직과 게임 화면은 공통 모듈에 두고, 토스 연동은 앱인토스 호스트에 둡니다.

## 기능

- 가위·바위·보 중 하나를 고르면 상대가 무작위로 고르고 승·패·무를 판정
- 익명 사용자 키별 누적 기록 저장, 초기화, 텍스트 공유
- 뒤로 가기 시 종료 확인, 안전 영역 적용

## 구성

| 모듈 | 역할 |
| --- | --- |
| [shared-core](shared-core/README.md) | 판정·무작위 선택·기록 저장 로직 |
| [shared-ui](shared-ui/README.md) | 재사용 가능한 Compose 게임 화면 |
| [appsInTossApp](appsInTossApp/README.md) | 토스 연동과 앱인토스 진입점 |

공통 UI는 Compose Material 3을 사용하며 TDS는 포함하지 않습니다.

## 현재 제공 범위

기록은 기기의 localStorage에만 저장합니다. 서버 저장·랭킹·사운드는 제공하지 않습니다.

[개발 가이드](docs/development.md)에서 실행·검사·GitHub Actions CI 방법을 확인할 수 있습니다.
