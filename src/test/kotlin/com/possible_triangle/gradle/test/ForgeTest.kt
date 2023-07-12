package com.possible_triangle.gradle.test

import com.possible_triangle.gradle.features.loaders.ModLoader
import com.possible_triangle.gradle.features.loaders.detectModLoader
import org.gradle.kotlin.dsl.forge
import org.gradle.kotlin.dsl.mod
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ForgeTest {

    @Test
    fun `can setup forge project`() {
        val project = createProject {
            withProjectDir("example")
        }

        project.mod {
            minecraftVersion.set("1.19.2")
        }

        project.forge {
            forgeVersion = "43.2.0"
        }

        assertNotNull(project.configurations.getByName("minecraft"))
    }

    @Test
    fun `can customize mod values after forge block`() {
        val project = createProject {
            withProjectDir("example")
        }

        project.forge {
            forgeVersion = "43.2.0"
        }

        project.mod {
            minecraftVersion.set("1.19.2")
        }
    }

    @Test
    fun `detects forge modloader`() {
        val project = createProject {
            withProjectDir("example")
        }

        project.mod {
            minecraftVersion.set("1.19.2")
        }

        project.forge {
            forgeVersion = "43.2.0"
        }

        assertEquals(ModLoader.FORGE, project.detectModLoader())
    }

}