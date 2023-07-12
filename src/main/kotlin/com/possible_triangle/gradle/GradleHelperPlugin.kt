package com.possible_triangle.gradle

import com.possible_triangle.gradle.features.defaultRepositories
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.repositories
import org.gradle.kotlin.dsl.withType
import org.gradle.language.jvm.tasks.ProcessResources

interface ModExtension {
    val id: Property<String>
    val name: Property<String>
    val version: Property<String>
    val author: Property<String>
    val minecraftVersion: Property<String>
    val releaseType: Property<String>
    val repository: Property<String>
    val mavenGroup: Property<String>

    val includedLibraries: SetProperty<String>
}

class GradleHelperPlugin : Plugin<Project> {

    override fun apply(target: Project) = target.allprojects { configure() }

    private fun Project.configure() {
        val rootMod = rootProject.takeUnless { it == this }?.extensions?.findByType<ModExtension>()
        val mod = extensions.create<ModExtension>("mod")

        fun <T> configureDefault(default: T?, supplier: ModExtension.() -> Property<T>) {
            mod.supplier().convention(provider { rootMod?.supplier()?.orNull ?: default })
        }

        configureDefault(rootProject.stringProperty("mod_id")) { id }
        configureDefault(rootProject.stringProperty("mod_name")) { name }
        configureDefault(rootProject.stringProperty("mod_version")) { version }
        configureDefault(rootProject.stringProperty("mod_author")) { author }
        configureDefault(rootProject.stringProperty("mc_version")) { minecraftVersion }
        configureDefault(rootProject.stringProperty("release_type")) { releaseType }
        configureDefault(rootProject.stringProperty("repository")) { repository }
        configureDefault(rootProject.stringProperty("maven_group")) { mavenGroup }

        mod.includedLibraries.convention(provider { rootMod?.includedLibraries?.orNull ?: emptySet() })

        repositories {
            defaultRepositories()
        }

        setupJava()
        configureBaseName()
        configureJarTasks()

        @Suppress("UnstableApiUsage")
        tasks.withType<ProcessResources> {
            // this will ensure that this task is redone when the versions change.
            inputs.property("version", mod.version)

            filesMatching(
                listOfNotNull(
                    "META-INF/mods.toml",
                    "pack.mcmeta",
                    "fabric.mod.json",
                    mod.id.map { modId -> "${modId}.mixins.json" }.orNull,
                )
            ) {
                expand(
                    mapOf(
                        "version" to mod.version.orNull,
                        "mod_version" to mod.version.orNull,
                        "mod_name" to mod.name.orNull,
                        "mod_id" to mod.id.orNull,
                        "mod_author" to mod.author.orNull,
                        "repository" to mod.repository.orNull,
                    ).filterValues { it != null }
                )
            }
        }

        // Disables Gradle's custom module metadata from being published to maven. The
        // metadata includes mapped dependencies which are not reasonably consumable by
        // other mod developers.
        tasks.withType<GenerateModuleMetadata> {
            enabled = false
        }

        tasks.withType<Jar> {
            exclude(".cache")
        }
    }

}