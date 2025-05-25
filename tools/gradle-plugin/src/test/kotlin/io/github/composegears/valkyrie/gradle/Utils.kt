package io.github.composegears.valkyrie.gradle

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.name
import kotlin.io.path.writeText
import org.gradle.testkit.runner.GradleRunner

internal fun runTask(root: Path, task: String) = GradleRunner
    .create()
    .withPluginClasspath()
    .withGradleVersion(System.getProperty("test.version.gradle"))
    .withProjectDir(root.toFile())
    .withArguments(task, "--configuration-cache", "--info", "--stacktrace")

internal fun Path.writeSettingsFile() = resolve("settings.gradle.kts").writeText(
    """
        pluginManagement {
            repositories {
                mavenCentral()
                google()
                gradlePluginPortal()
            }
        }

        dependencyResolutionManagement {
            repositories {
                mavenCentral()
                google()
            }
        }
    """.trimIndent(),
)

internal fun Path.writeTestSvgs(sourceSet: String) {
    val destDir = resolve("src/$sourceSet/svg")
    Files.createDirectories(destDir)

    val sourceDir = Paths.get(System.getProperty("test.dir.svg"))
    Files.list(sourceDir).forEach { p -> Files.copy(p, destDir.resolve(p.name)) }
}

internal fun Path.writeTestDrawables(sourceSet: String) {
    val destDir = resolve("src/$sourceSet/res/drawable")
    Files.createDirectories(destDir)

    val sourceDir = Paths.get(System.getProperty("test.dir.xml"))
    Files.list(sourceDir).forEach { p -> Files.copy(p, destDir.resolve(p.name)) }
}
