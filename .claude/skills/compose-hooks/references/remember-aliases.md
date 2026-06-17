# remember 别名索引

公开 `useXxx` Hook 均提供 Compose 命名风格的 `rememberXxx` 别名。别名只委托到对应 `useXxx`，语义和返回值保持一致；新代码可以按团队风格选择 `useXxx` 或 `rememberXxx`，但不要混用废弃的 `Table` 作用域入口。

## 通用别名

- `rememberAbs`
- `rememberAsync`
- `rememberAutoReset`
- `rememberBackToFrontEffect`
- `rememberBoolean`
- `rememberCancelableAsync`
- `rememberCeil`
- `rememberClipboard`
- `rememberContext`
- `rememberControllable`
- `rememberCountdown`
- `rememberCounter`
- `rememberCreation`
- `rememberCycleList`
- `rememberDateFormat`
- `rememberDebounce`
- `rememberDebounceEffect`
- `rememberDebounceFn`
- `rememberDispatch`
- `rememberDispatchAsync`
- `rememberDouble`
- `rememberEffect`
- `rememberEventPublish`
- `rememberEventSubscribe`
- `rememberFloat`
- `rememberFloor`
- `rememberFrontToBackEffect`
- `rememberGetState`
- `rememberImmutableList`
- `rememberImmutableListReduce`
- `rememberInt`
- `rememberInterval`
- `rememberKeyboard`
- `rememberLastChanged`
- `rememberLatestRef`
- `rememberLatestState`
- `rememberList`
- `rememberListReduce`
- `rememberLong`
- `rememberMap`
- `rememberMax`
- `rememberMemoizedFn`
- `rememberMin`
- `rememberMount`
- `rememberNow`
- `rememberPausableEffect`
- `rememberPersistent`
- `rememberPow`
- `rememberPrevious`
- `rememberReducer`
- `rememberRef`
- `rememberRequest`
- `rememberResetState`
- `rememberRound`
- `rememberSelectable`
- `rememberSelector`
- `rememberSorted`
- `rememberSqrt`
- `rememberSse`
- `rememberState`
- `rememberStateAsync`
- `rememberStateMachine`
- `rememberTable`
- `rememberTableRequest`
- `rememberThrottle`
- `rememberThrottleEffect`
- `rememberThrottleFn`
- `rememberTimeAgo`
- `rememberTimeout`
- `rememberTimeoutFn`
- `rememberTimeoutPoll`
- `rememberTimestamp`
- `rememberTimestampRef`
- `rememberToggle`
- `rememberToggleEither`
- `rememberToggleVisible`
- `rememberTrunc`
- `rememberUndo`
- `rememberUnmount`
- `rememberUnmountedRef`
- `rememberUpdate`
- `rememberUpdateEffect`

## Form 作用域别名

- `Form.rememberForm`
- `Form.rememberFormInstance`
- `Form.rememberWatch`

## Android 别名

- `rememberBatteryInfo`
- `rememberBiometric`
- `rememberBuildInfo`
- `rememberDisableScreenshot`
- `rememberFlashlight`
- `rememberIdle`
- `rememberIlluminance`
- `rememberNetwork`
- `rememberScreenBrightness`
- `rememberScreenInfo`
- `rememberSensor`
- `rememberVibrate`
- `rememberWakeLock`
- `rememberWindowFlags`

## Desktop 别名

- `rememberKeyPress`

## 兼容/内部辅助别名

- `rememberEmptyPlugin`: `useRequest` 插件系统内部空插件辅助；业务代码通常不需要直接使用。
- `rememberTableInstance`: 对应废弃的 `Table.useTableInstance()`，仅为 API 命名完整性保留；新代码使用 `useTable`/`rememberTable` 返回的 `TableHolder`。
