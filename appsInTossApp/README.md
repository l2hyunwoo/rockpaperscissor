# 앱인토스 메모 앱

공통 메모 화면에 토스 로그인·공유·호스트 이벤트·안전 영역을 연결한 Compose 웹앱 예제입니다. 앱인토스 전용 UI와 기능을 이 호스트에 둡니다.

## 구성과 동작

[공통 UI](../shared-ui/README.md)와 [공통 core](../shared-core/README.md)를 사용합니다. 초안은 브라우저 localStorage에 메모 ID별로 저장하며 출처(origin)별로 구분합니다. 로그인 결과는 화면 상태로 전달하며 서버의 인가 코드 교환은 구현하지 않았습니다.

뒤로 가기 이벤트는 상세 화면에서 목록으로 이동하고 목록에서는 호스트 화면을 닫습니다. 홈 이벤트는 선택한 메모를 해제합니다. 클라이언트는 Compose 호스트가 해제될 때 닫습니다.

## 실행

프로젝트 루트에서 실행합니다. Node 24 이상·Corepack·JDK 17과 Yarn 의존성이 필요합니다.

```sh
yarn dev
yarn dev:toss
yarn build
```

`dev`는 mock, `dev:toss`는 실제 브리지로 실행합니다. `build`는 실제 브리지를 포함한 `.ait`를 만듭니다. `apps-in-toss.config.ts`의 appName·브랜드·권한 등 서비스 설정을 콘솔과 맞춰야 합니다. 자동 업로드는 하지 않습니다.

예제 데이터와 mock 인증 결과는 운영 데이터·인증 증명이 아닙니다. TDS는 포함하지 않습니다.
