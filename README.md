# 가위바위보

앱인토스에서 실행하는 Compose Multiplatform 가위바위보 게임입니다. 판정·기록 로직과 게임 화면은 공통 모듈에 두고, 토스 연동은 앱인토스 호스트에 둡니다.

## 기능

- 가위·바위·보 중 하나를 고르면 상대가 무작위로 고르고 승·패·무를 판정
- 익명 사용자 키별 승·패·무와 연승 기록 저장(토스앱 Storage, 미지원 시 localStorage), 초기화, 텍스트 공유
- 선택·승·패 햅틱, 게임센터 닉네임 표시, 최고 연승 리더보드 제출·랭킹 보기
- 나가기 버튼과 뒤로 가기 모두 종료 확인 다이얼로그, 안전 영역 적용

## 구성

| 모듈 | 역할 |
| --- | --- |
| [shared-core](shared-core/README.md) | 판정·무작위 선택·기록 저장 로직 |
| [shared-ui](shared-ui/README.md) | 재사용 가능한 Compose 게임 화면 |
| [appsInTossApp](appsInTossApp/README.md) | 토스 연동과 앱인토스 진입점 |

공통 UI는 Compose Material 3을 사용하며 TDS는 포함하지 않습니다.

## 현재 제공 범위

SDK는 `io.github.l2hyunwoo:apps-in-toss-sdk:1.0.0-alpha03`을 사용합니다. 리더보드는 앱인토스 콘솔에서 리더보드를 설정해야 동작합니다. 서버 저장·사운드는 제공하지 않습니다.

손 이미지는 [Noto Emoji](https://github.com/googlefonts/noto-emoji) 3D 이미지입니다([라이선스](licenses/NotoEmoji-LICENSE.txt)).

[개발 가이드](docs/development.md)에서 실행·검사·GitHub Actions CI 방법을 확인할 수 있습니다.
