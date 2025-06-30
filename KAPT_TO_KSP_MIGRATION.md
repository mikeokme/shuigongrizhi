# KAPT to KSP Migration Guide

## Problem
The build was failing with a KAPT (Kotlin Annotation Processing Tool) error:
```
Execution failed for task ':app:kaptDebugKotlin'
```

This is a known compatibility issue when using Kotlin 2.0+ with KAPT. Google recommends migrating to KSP (Kotlin Symbol Processing) for better performance and compatibility.

## Changes Made

### 1. Updated app/build.gradle.kts
- Replaced `kotlin("kapt")` with `id("com.google.devtools.ksp")`
- Changed `kapt("androidx.room:room-compiler:2.6.1")` to `ksp("androidx.room:room-compiler:2.6.1")`
- Changed `kapt("com.google.dagger:hilt-compiler:2.48")` to `ksp("com.google.dagger:hilt-compiler:2.48")`

### 2. Updated build.gradle.kts (project level)
- Added KSP plugin: `id("com.google.devtools.ksp") version "2.0.21-1.0.25" apply false`

## Benefits of KSP over KAPT
- **Faster builds**: KSP is up to 2x faster than KAPT
- **Better Kotlin compatibility**: Native support for Kotlin 2.0+
- **Improved incremental compilation**: More efficient change detection
- **Future-proof**: Google's recommended annotation processing solution

## Testing the Migration

To test if the migration was successful, run:
```bash
./gradlew clean
./gradlew assembleDebug
```

## Potential Issues and Solutions

### 1. If build still fails
- Check that all annotation processors support KSP
- Verify KSP version compatibility with Kotlin version
- Clean and rebuild: `./gradlew clean build`

### 2. Generated code location
- KSP generates code in `build/generated/ksp/` instead of `build/generated/kapt_kotlin/`
- IDE might need to refresh/sync project

### 3. Missing generated classes
- Ensure all `@HiltAndroidApp`, `@HiltViewModel`, and Room annotations are properly applied
- Check that generated Hilt components are being created

## Verification Checklist

- [ ] Project builds successfully
- [ ] Hilt dependency injection works
- [ ] Room database operations function correctly
- [ ] No missing generated classes errors
- [ ] App runs without crashes

## Additional Resources

- [KSP Documentation](https://kotlinlang.org/docs/ksp-overview.html)
- [Migrating from KAPT](https://developer.android.com/build/migrate-to-ksp)
- [Hilt with KSP](https://dagger.dev/dev-guide/ksp.html)

## Rollback Instructions

If issues persist, you can rollback by:
1. Reverting `id("com.google.devtools.ksp")` back to `kotlin("kapt")`
2. Changing `ksp()` dependencies back to `kapt()`
3. Removing KSP plugin from project-level build.gradle.kts
4. Consider downgrading Kotlin version to 1.9.x for KAPT compatibility