# shared-core

가위바위보 판정·무작위 선택·기록 저장 로직을 담은 공통 Kotlin 라이브러리입니다. Compose·토스·TDS·브라우저 타입에 의존하지 않습니다.

## 제공 API

| API | 역할 |
| --- | --- |
| `Hand`, `Outcome`, `judge(player, computer)` | 손 모양과 승·패·무 판정 |
| `Random.nextHand()` | 균등 무작위 선택 |
| `Record` | 승·패·무, 현재·최고 연승(무승부는 연승 유지) |
| `RpsGame.load(storage, userKey, random)` | 저장된 기록을 읽어 게임 생성, 한 판 진행·초기화·저장 |
| `KeyValueStorage`, `MemoryStorage` | suspend 저장 계약과 메모리 구현 |

기록은 `userKey`별로 저장합니다. `userKey`가 `null`이면 저장하지 않고 실행 중에만 기록을 유지합니다. 저장된 값을 읽지 못하면 빈 기록으로 시작하고, 쓰기에 실패하면 `saveFailed`로 알립니다.
