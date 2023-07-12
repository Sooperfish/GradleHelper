@file:Suppress("unused")

package org.gradle.kotlin.dsl

import com.possible_triangle.gradle.ModExtension
import com.possible_triangle.gradle.ProjectEnvironment
import com.possible_triangle.gradle.features.addCurseMaven
import com.possible_triangle.gradle.features.addModrinthMaven
import com.possible_triangle.gradle.features.configureSonarQube
import com.possible_triangle.gradle.features.enableKotlin
import com.possible_triangle.gradle.features.loaders.*
import com.possible_triangle.gradle.features.publishing.*
import com.possible_triangle.gradle.loadEnv
import net.minecraftforge.gradle.userdev.DependencyManagementExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.plugins.ExtensionAware
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.com.google.common.base.Suppliers
import org.sonarqube.gradle.SonarProperties

fun ExtensionAware.mod(block: ModExtension.() -> Unit) = extensions.configure(block)

val Project.mod get() = the<ModExtension>()

fun Project.common(block: CommonExtension.() -> Unit = {}) = setupCommon(block)
fun Project.forge(block: ForgeExtension.() -> Unit = {}) = setupForge(block)
fun Project.fabric(block: FabricExtension.() -> Unit = {}) = setupFabric(block)

val Project.env get(): ProjectEnvironment = Suppliers.memoize { loadEnv() }.get()

fun Project.withKotlin() = enableKotlin()

fun RepositoryHandler.curseMaven() = addCurseMaven()
fun RepositoryHandler.modrinthMaven() = addModrinthMaven()

fun RepositoryHandler.githubPackages(project: Project) = addGithubPackages(project)

fun Project.enablePublishing(block: ModMavenPublishingExtension.() -> Unit = {}) = enableMavenPublishing(block)
fun Project.uploadToCurseforge(block: CurseforgeExtension.() -> Unit = {}) = enableCursegradle(block)
fun Project.uploadToModrinth(block: ModrinthExtension.() -> Unit = {}) = enableMinotaur(block)

fun DependencyManagementExtension.deobf(dependencyNotation: String, block: ExternalModuleDependency.() -> Unit = {}) =
    deobf(dependencyNotation, closureOf<ExternalModuleDependency> { block() })

private fun Project.modDependency(
    type: String,
    dependencyNotation: String,
    block: ExternalModuleDependency.() -> Unit,
): Dependency? {
    val loader = detectModLoader()
    return if (loader == ModLoader.FORGE) dependencies.add(type, fg.deobf(dependencyNotation, block))
    else dependencies.add("mod${type.capitalized()}", dependencyNotation, block)
}

fun Project.modImplementation(dependencyNotation: String, block: ExternalModuleDependency.() -> Unit = {}) =
    modDependency("implementation", dependencyNotation, block)

fun Project.modRuntimeOnly(dependencyNotation: String, block: ExternalModuleDependency.() -> Unit = {}) =
    modDependency("runtimeOnly", dependencyNotation, block)

fun Project.modCompileOnly(dependencyNotation: String, block: ExternalModuleDependency.() -> Unit = {}) =
    modDependency("compileOnly", dependencyNotation, block)

val Project.fg get() = the<DependencyManagementExtension>()

fun Project.enableSonarQube(block: Project.(SonarProperties) -> Unit = {}) = configureSonarQube(block)

