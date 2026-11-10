plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    buildFeatures {
        compose = true
    }

    namespace = "me.rerere.material3"
    sourceSets {
        named("main") {
            kotlin.srcDir("material-color-utilities/kotlin")
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
}
