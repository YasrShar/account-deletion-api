# Battery Insight Pro (Java, Material 3)

Battery Insight Pro is an Android 8+ battery analytics app template built in **Java** with clean layering (data/domain-ish/ui), Room persistence, WorkManager cleanup, widgets, onboarding for Usage Access and Overlay, and a production-oriented Gradle setup for Google Play publishing.

## Implemented foundation (v1 scaffold)
- Dashboard live metrics from `ACTION_BATTERY_CHANGED` + `BatteryManager` properties.
- Room database schema:
  - `BatterySampleEntity`
  - `ChargeSessionEntity`
  - `ChargerTestEntity`
- Repository pattern and persistent sampling.
- Core algorithms:
  - Current normalization (µA → mA)
  - EMA smoothing
  - Power calculation (W)
  - Capacity estimation (charge counter + integration fallback)
  - Wear score approximation
- Bottom navigation screens:
  - Dashboard, Health, Usage, Charge Speed, History, Settings
- Onboarding screen with deep links for:
  - Usage Access (`PACKAGE_USAGE_STATS` settings)
  - Overlay settings (`SYSTEM_ALERT_WINDOW`)
  - Battery optimization guidance
- Foreground service skeleton for live notification/overlay mode.
- Widget provider skeleton (1x1 baseline).
- Localization strings: English, Arabic, Turkish.
- Unit tests for battery math and estimators.

## Permissions policy
The app intentionally avoids restricted Play-incompatible battery permissions like `BATTERY_STATS`.

Used permissions:
- `PACKAGE_USAGE_STATS` (user enables from Settings, for estimated foreground app usage)
- `SYSTEM_ALERT_WINDOW` (optional overlay card)
- `FOREGROUND_SERVICE` (only when live mode is active)
- `POST_NOTIFICATIONS`, `VIBRATE`, `RECEIVE_BOOT_COMPLETED`

## Measurement model and constraints
Some values are **estimated** due to Android OEM limitations. Confidence labels should be surfaced as:
- High: current + charge counter available
- Medium: only one available
- Low: partial/unstable sensors

## Charger Test flow (planned UX)
1. User starts test and names charger/cable.
2. 3-minute screen-off capture.
3. 3-minute screen-on capture.
4. Save averages, stability, and thermal rise.
5. Compare multiple test runs.

## Retention & privacy
- Offline-first: no server sync in this scaffold.
- Sampling retention worker defaults to 7 days for free tier.
- Pro design target: 90 days + CSV export.
- Include a “Delete all data” action in Settings before production launch.

## Google Play readiness checklist
- Replace placeholder icons and screenshots.
- Add signed release keystore workflow.
- Add Play Billing products (monthly/yearly/lifetime).
- Provide Data Safety form: local-only analytics/no personal data collection.
- Add in-app Privacy Policy screen + hosted URL.

## Known limitations (OEM/device differences)
- `BATTERY_PROPERTY_CURRENT_NOW` sign and unit behavior differ across vendors.
- Charge counter may be unavailable/unstable on some Samsung/Xiaomi models.
- Background execution limits may delay sampling on aggressive ROMs.
- Usage Access may return sparse data if user disables permission or OEM throttles stats.
- Temperature sensors can have low refresh rates or vendor-specific offsets.

## Build
Open in Android Studio Hedgehog+ and run Gradle sync.

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```
