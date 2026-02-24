# SageAlarm — CLAUDE.md

## 프로젝트 개요

현자가 되기 위한 첫 걸음. 계획한 시간에 알람을 맞추는 것.
복잡함 없이, 딱 알람 기능만. 디자인도 기능도 최대한 단순하게.

---

## 기술 스택

| 영역 | 선택 |
|------|------|
| 언어 | Kotlin |
| UI | Jetpack Compose |
| 아키텍처 | MVVM + Clean Architecture |
| DI | Hilt |
| 비동기 | Coroutines + Flow |
| DB | Room |
| 설정 저장 | DataStore Preferences |
| 알람 트리거 | AlarmManager (exact alarm) |
| 백그라운드 | Foreground Service |
| 음악 재생 | ExoPlayer |
| TTS | Android 기본 TextToSpeech API (추후 온디바이스 TTS 모델로 교체 예정) |

---

## 아키텍처

```
app/
├── data/
│   ├── local/
│   │   ├── db/          # Room DB, DAO, Entity
│   │   └── datastore/   # DataStore Preferences
│   └── repository/      # Repository 구현체
├── domain/
│   ├── model/           # 도메인 모델
│   ├── repository/      # Repository 인터페이스
│   └── usecase/         # Use Case
├── presentation/
│   ├── alarm/           # 알람 목록, 추가/편집 화면
│   ├── dismiss/         # 알람 해제 퍼즐 화면
│   └── settings/        # 앱 설정 화면
├── service/
│   └── AlarmService.kt  # Foreground Service
└── di/                  # Hilt 모듈
```

---

## 기능 명세

### 1. 기본 알람
- 알람 시간 설정 (시/분)
- 요일 반복 설정
- 알람 ON/OFF 토글
- 알람 레이블 입력

### 2. TTS 메시지
- 알람 울릴 때 최대 100자 이내의 사용자 작성 문자를 TTS로 출력
- **현재**: Android 기본 `TextToSpeech` API 사용
- **추후**: 온디바이스 TTS 모델 (ONNX Runtime 기반)로 교체 — TTS 레이어는 인터페이스로 추상화하여 교체 용이하게 유지

### 3. 알람 음악
- 기기 내 음악 파일 선택 가능
- 미선택 시 기본 알람음 사용
- ExoPlayer로 재생

### 4. 알람 해제 퍼즐
- 1~99 사이 숫자 5개를 랜덤으로 화면에 배치
- 오름차순으로 순서대로 터치해야 해제
- 실패(잘못된 순서 터치) 시 화면 초기화 후 재시도

---

## 저장 정책

- **알람 데이터**: Room DB (로컬 전용)
- **앱 설정**: DataStore Preferences (로컬 전용)
- **음악 파일**: 기기 로컬 경로 참조만 저장
- 네트워크 없이 모든 핵심 기능이 동작해야 함
- 서버/백엔드 없음

---

## 코드 규칙

### 필수 제한사항
- `any` 타입 사용 금지 — 명시적 타입 선언 필수
- 하드코딩된 URL, API 키, 민감 정보 금지 — 환경 변수 또는 `local.properties` 사용
- 함수 길이 200줄 이하 유지
- 매직 넘버 금지 — 상수로 선언

### 네이밍
- ViewModel: `XxxViewModel`
- UseCase: `XxxUseCase`
- Repository 인터페이스: `XxxRepository`
- Repository 구현체: `XxxRepositoryImpl`
- Room Entity: `XxxEntity`
- 도메인 모델: `Xxx` (suffix 없음)

### TTS 추상화 (교체 대비)
TTS 기능은 반드시 인터페이스로 추상화하여 구현체만 교체 가능하게 유지:

```kotlin
interface TtsPlayer {
    fun speak(text: String, language: Locale)
    fun stop()
    fun release()
}

// 현재 구현체
class AndroidTtsPlayer(...) : TtsPlayer

// 추후 교체 대상
// class OnDeviceTtsPlayer(...) : TtsPlayer
```

---

## 개발 시 주의사항

- `AlarmManager.setExactAndAllowWhileIdle()` 사용 (Android 6.0+)
- Android 12+ 정확한 알람 권한 (`SCHEDULE_EXACT_ALARM`) 처리 필요
- Android 13+ 알림 권한 (`POST_NOTIFICATIONS`) 런타임 요청 필요
- 음악 파일 접근 시 `READ_MEDIA_AUDIO` 권한 (Android 13+) 처리
- Foreground Service는 알람 재생 중에만 활성화

---

## 목표 디자인 원칙

- 화면 수 최소화
- 불필요한 설정 항목 없음
- 한 화면에서 한 가지 작업만
- 텍스트 색상은 배경과 조화를 위해 기본 텍스트 `WarmBrown`, 보조/UI 텍스트 `WarmBrownMuted` 사용 
— Compose 컴포넌트 색상은 반드시 명시적으로 지정할 것 (Material3 기본값 의존 금지)
