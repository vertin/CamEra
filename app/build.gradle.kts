plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.detekt)
}

android {
    namespace = "com.task.camera"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.task.camera"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.task.camera.InstrumentationTestRunner"
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
            all {
                it.useJUnitPlatform()
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/LICENSE-notice.md"
            excludes += "/META-INF/NOTICE.md"
            excludes += "/META-INF/NOTICE.txt"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/DEPENDENCIES.txt"
        }
    }
}

detekt {
    buildUponDefaultConfig = true
    allRules = true
    config.setFrom("$projectDir/../config/detekt/detekt.yml")
    baseline = file("$projectDir/../config/detekt/baseline.xml")
    autoCorrect = true

    dependencies {
        detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:${libs.versions.detekt.get()}")
    }
}

dependencies {
    // Accompanist
    implementation(libs.accompanist.permissions)

    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)

    // ViewModel Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Navigation Compose
    implementation(libs.androidx.navigation.compose)

    // CameraX
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.video)
    implementation(libs.androidx.camera.preview)
    implementation(libs.androidx.camera.compose)
    implementation(libs.androidx.camera.lifecycle)

    // ExoPlayer (Media3)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.inspector)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.ui.compose)
    implementation(libs.androidx.media3.ui.compose.material3)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    ksp(libs.androidx.room.compiler)

    // Paging
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Kotlinx Coroutines
    implementation(libs.kotlinx.coroutines.guava)

    // Koin
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.koin.androidx.workmanager)

    // Coil
    implementation(libs.coil.compose)

    // Testing
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.paging.testing)
    androidTestImplementation(libs.koin.test)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.junit.jupiter.api)
    androidTestRuntimeOnly(libs.junit.jupiter.engine)
    androidTestImplementation(libs.androidx.test.ext.junit.ktx)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.test.uiautomator)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

tasks.withType<Test> {
    useJUnitPlatform()
}