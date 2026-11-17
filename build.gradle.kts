import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.spotless)
}

spotless {
    // 只格式化相对 origin/master 有改动的文件，避免一次性重排整个仓库
    ratchetFrom("origin/master")

    // Spotless 只从 .editorconfig 读标准属性，ktlint 的风格和规则开关得写在这里
    val ktlintVersion = libs.versions.ktlint.get()
    val ktlintConfig = mapOf(
        "ktlint_code_style" to "intellij_idea",
        "max_line_length" to 120,
        // 以下规则和现有写法冲突较多，只会制造大量改动
        "ktlint_standard_trailing-comma-on-call-site" to "disabled",
        "ktlint_standard_trailing-comma-on-declaration-site" to "disabled",
        "ktlint_standard_function-signature" to "disabled",
        "ktlint_standard_class-signature" to "disabled",
        "ktlint_standard_function-expression-body" to "disabled",
        "ktlint_standard_blank-line-between-when-conditions" to "disabled",
        "ktlint_standard_wrapping" to "disabled",
    )
    kotlin {
        target("**/*.kt")
        // 子项目的 build 目录 Spotless 会自动排除；targetExclude 是先列出再相减，写 **/build/** 会把所有 build 目录遍历一遍。
        // build-logic 是 included build，不在自动排除之列
        targetExclude("build-logic/build/**", "material3/material-color-utilities/**")
        ktlint(ktlintVersion).editorConfigOverride(ktlintConfig)
        // 只用 ktlint 做格式化，命名、文件名、行宽这类没法自动修的问题不拦
        suppressLintsFor { step = "ktlint" }
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("build-logic/build/**")
        ktlint(ktlintVersion).editorConfigOverride(ktlintConfig)
        suppressLintsFor { step = "ktlint" }
    }
}


// Standalone defaults. When included by ZeroStudio, its root AndroidIDEPlugin
// supplies the Android SDK/JVM configuration instead.
group = "com.github.msmt2018.rikkahub"
if (version.toString() == "unspecified") {
    version = "0.0.0-SNAPSHOT"
}

subprojects {
    group = rootProject.group
    version = rootProject.version

    plugins.withId("com.android.library") {
        extensions.configure<LibraryExtension> {
            compileSdk {
                version = release(37) {
                    minorApiLevel = 2
                }
            }
            defaultConfig {
                minSdk = 26
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
            publishing {
                singleVariant("release") {
                    withSourcesJar()
                }
            }
        }

        if (project.name != "app") {
            pluginManager.apply("maven-publish")
            afterEvaluate {
                extensions.configure<PublishingExtension> {
                    publications {
                        register<MavenPublication>("release") {
                            from(components["release"])
                            groupId = "com.github.msmt2018.rikkahub"
                            artifactId = project.name
                            version = project.version.toString()
                        }
                    }
                }
            }
        }
    }

    plugins.withId("org.jetbrains.kotlin.android") {
        tasks.withType<KotlinCompile>().configureEach {
            compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}
