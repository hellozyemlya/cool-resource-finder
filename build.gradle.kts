import me.modmuss50.mpp.ModPublishExtension
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.task.RemapJarTask
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    id("fabric-loom") version "1.7.4" apply false
    id("org.jetbrains.kotlin.jvm") version "1.9.0" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0" apply false
    id("me.modmuss50.mod-publish-plugin") version "0.1.1" apply false
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
    exclusiveContent {
        forRepository {
            maven {
                url = uri("https://cursemaven.com")
            }
        }
        filter {
            includeGroup("curse.maven")
        }
    }
}

version = "1.0.0"
group = "junk"

fun sourceSetsOfRoot(root: String): Map<String, Map<String, String>> {
    return mapOf(
        "kotlin" to mapOf(
            "main" to "${root}/src/main/kotlin",
            "client" to "${root}/src/client/kotlin",
            "test" to "${root}/src/test/kotlin"
        ),
        "java" to mapOf(
            "main" to "${root}/src/main/java",
            "client" to "${root}/src/client/java",
            "test" to "${root}/src/test/java"
        ),
        "resources" to mapOf(
            "main" to "${root}/src/main/resources",
            "client" to "${root}/src/client/resources",
            "test" to "${root}/src/test/resources"
        )
    )
}

val commonSourceRoot = "${rootProject.projectDir}/common"
val nbtComponentsSourceRoot = "${rootProject.projectDir}/common/components/nbt"
val nativeComponentsSourceRoot = "${rootProject.projectDir}/common/components/native"

val commonSourceSets = sourceSetsOfRoot(commonSourceRoot)
val nbtComponentsSourceSets = sourceSetsOfRoot(nbtComponentsSourceRoot)
val nativeComponentsSourceSets = sourceSetsOfRoot(nativeComponentsSourceRoot)

fun Project.applySourceSets(sourceSets: Map<String, Map<String, String>>) {
    // apply kotlin source sets
    this.extensions.configure<KotlinJvmProjectExtension> {
        sourceSets["kotlin"]?.forEach { (setName, setPath) ->
            sourceSets {
                named(setName) {
                    kotlin {
                        srcDir(setPath)
                    }
                }
            }
        }
    }
    // apply java & resources source sets
    project.the<SourceSetContainer>().apply {
        sourceSets["java"]?.forEach { (setName, setPath) ->
            named(setName) {
                java {
                    srcDir(setPath)
                }
            }
        }
        sourceSets["resources"]?.forEach { (setName, setPath) ->
            named(setName) {
                resources {
                    srcDir(setPath)
                }
            }
        }
    }
}

fun compareSemVer(version1: String, version2: String): Int {
    val v1 = version1.split(".").map { it.toInt() }
    val v2 = version2.split(".").map { it.toInt() }

    for (i in 0..2) {
        if (v1[i] != v2[i]) {
            return v1[i].compareTo(v2[i])
        }
    }
    return 0
}

subprojects {
    apply(plugin = "fabric-loom")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    apply(plugin = "me.modmuss50.mod-publish-plugin")

    val mod_version: String by project
    val maven_group: String by project
    version = mod_version
    group = maven_group

    project.configure<LoomGradleExtensionAPI> {
        splitEnvironmentSourceSets()
        mods {
            create("cool-resource-finder") {
                sourceSet("main")
                sourceSet("client")
            }
        }

        runs {
            create("datagen") {
                inherit(named("server").get())
                name = "Data Generation"
                vmArg("-Dfabric-api.datagen")
                vmArg("-Dfabric-api.datagen.output-dir=${file("generated/src/main/resources")}")
                vmArg("-Dfabric-api.datagen.modid=cool-resource-finder")
                runDir = "build/datagen"
            }
        }
    }

    project.tasks.apply {
        named<ProcessResources>("processResources") {
            inputs.property("version", project.version)
            filesMatching("fabric.mod.json") {
                expand("version" to project.version)
            }
        }
        withType<JavaCompile>().configureEach {
            options.release = 17
        }
        withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    project.applySourceSets(commonSourceSets)

    val componentsSourceSet = when {
        compareSemVer("1.20.5", project.name) > 0 -> nbtComponentsSourceSets
        compareSemVer("1.20.5", project.name) < 0 -> nativeComponentsSourceSets
        else -> nativeComponentsSourceSets
    }
    project.applySourceSets(componentsSourceSet)
    // include generated code and resources
    project.applySourceSets(sourceSetsOfRoot("generated"))

    val minecraft_version: String by project
    val yarn_mappings: String by project
    val loader_version: String by project
    val fabric_version: String by project
    val fabric_kotlin_version: String by project

    dependencies {
        "minecraft"("com.mojang:minecraft:${minecraft_version}")
        "mappings"("net.fabricmc:yarn:${yarn_mappings}:v2")
        "modImplementation"("net.fabricmc:fabric-loader:${loader_version}")

        "modImplementation"("net.fabricmc.fabric-api:fabric-api:${fabric_version}")
        "modImplementation"("net.fabricmc:fabric-language-kotlin:${fabric_kotlin_version}")
        "testImplementation"("net.fabricmc:fabric-loader-junit:${loader_version}")
    }

    val mod_changelog: String by project

    configure<ModPublishExtension> {
        file = project.tasks.named<RemapJarTask>("remapJar").get().archiveFile.get().asFile
        changelog = mod_changelog
        type = BETA
        modLoaders.add("fabric")

        val version = if (project.name.endsWith(".0")) {
            project.name.removeSuffix(".0")
        } else {
            project.name
        }

        curseforge {
            projectId = "892065"
            accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
            minecraftVersions.add(version)

            requires {
                slug = "fabric-api"
            }
            requires {
                slug = "fabric-language-kotlin"
            }
        }
        modrinth {
            projectId = "eKgd4diD"
            accessToken = providers.environmentVariable("MODRINTH_TOKEN")
            minecraftVersions.add(version)

            requires {
                // fabric-api
                projectId = "P7dR8mSH"
            }
            requires {
                // fabric-language-kotlin
                projectId = "Ha28R6CL"
            }
        }
    }

    project.tasks.named("publishCurseforge").get().dependsOn(project.tasks.named("remapJar").get())
    project.tasks.named("publishModrinth").get().dependsOn(project.tasks.named("remapJar").get())
}