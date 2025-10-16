import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    kotlin("plugin.serialization") version "2.2.10"
    id("io.gitlab.arturbosch.detekt") version "1.23.3"
    id("com.diffplug.spotless") version "6.22.0"
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation("io.ktor:ktor-client-okhttp:3.3.0")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
            implementation("androidx.datastore:datastore-preferences:1.1.1")
            implementation("io.insert-koin:koin-android:3.5.0")
            implementation("androidx.paging:paging-runtime:3.3.6")
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.0-rc01")
            implementation("io.ktor:ktor-client-core:3.3.0")
            implementation("io.ktor:ktor-client-content-negotiation:3.3.0")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.3.0")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
            implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")
            implementation("network.chaintech:cmpcharts:2.0.6")
            implementation("androidx.datastore:datastore-preferences-core:1.1.1")
            implementation("io.insert-koin:koin-core:4.1.1")
            implementation("io.insert-koin:koin-compose-viewmodel:4.1.1")
            implementation("io.ktor:ktor-client-logging:3.3.0")
            implementation("androidx.paging:paging-common:3.3.6")
            implementation("androidx.paging:paging-compose:3.3.6")
        }
        iosMain.dependencies {
            implementation("io.ktor:ktor-client-darwin:2.3.12")
            implementation("io.insert-koin:koin-core:3.5.0")
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.fintrack.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

// Detekt Configuration
detekt {
    toolVersion = "1.23.3"
    config = files("$rootDir/config/detekt/detekt.yml")
    buildUponDefaultConfig = true

    // Configure for KMP source sets
    source = files(
        "src/commonMain/kotlin",
        "src/androidMain/kotlin",
        "src/iosMain/kotlin",
        "src/commonTest/kotlin",
    )
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.3")
}

// Spotless Configuration
spotless {
    kotlin {
        target("src/**/*.kt")
        ktlint("0.50.0")
        trimTrailingWhitespace()
        indentWithSpaces(4)
        endWithNewline()
    }

    kotlinGradle {
        target("*.gradle.kts")
        ktlint("0.50.0")
    }
}

// Configure tasks for KMP
tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    exclude("**/build/**")
    jvmTarget = "11"

    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(true)
    }
}

// Create convenience tasks
tasks.register("staticAnalysis") {
    group = "verification"
    description = "Run all static analysis tools"
    dependsOn("detekt", "spotlessCheck")
}

tasks.register("formatCode") {
    group = "formatting"
    description = "Format all code"
    dependsOn("spotlessApply")
}
