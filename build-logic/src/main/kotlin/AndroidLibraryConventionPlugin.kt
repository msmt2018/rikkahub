import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        pluginManager.apply("maven-publish")

        group = "com.github.msmt2018.rikkahub"
        version = providers.gradleProperty("rikkahubVersion").orElse("3.1.0").get()

        extensions.configure<LibraryExtension> {
            compileSdk = 37

            defaultConfig {
                minSdk = 26
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

        tasks.withType<KotlinCompile>().configureEach {
            compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
        }

        afterEvaluate {
            val releaseComponent = components.findByName("release")
                ?: error("Android release component is missing for $path")
            val publishingExtension = extensions.getByType(PublishingExtension::class.java)
            val releasePublication = publishingExtension.publications.create(
                "release",
                MavenPublication::class.java
            )
            releasePublication.from(releaseComponent)
            releasePublication.groupId = "com.github.msmt2018.rikkahub"
            releasePublication.artifactId = name
            releasePublication.version = version.toString()
        }
    }
}
