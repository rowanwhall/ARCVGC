## Testing

After modifying any Kotlin code in `shared/src/commonMain/`, check whether existing tests
in `shared/src/commonTest/` need updating or new tests need writing.

After modifying any ViewModel in `composeApp/src/androidMain/`, check whether existing tests
in `composeApp/src/androidUnitTest/` need updating or new tests need writing.

Specifically:

- If you changed a function's behavior or signature, update its tests to match.
- If you added a new public function, mapper, or repository method, add tests for it.
- If you fixed a bug, add a regression test that would have caught it.

Run `./gradlew :shared:testDebugUnitTest :composeApp:testDebugUnitTest` after changes to verify all tests pass.
