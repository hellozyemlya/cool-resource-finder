import me.modmuss50.mpp.ModPublishExtension
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.task.RemapJarTask
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    id("fabric-loom") version "1.3-SNAPSHOT" apply false
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

val commonSourceSets = sourceSetsOfRoot(commonSourceRoot)
val nbtComponentsSourceSets = sourceSetsOfRoot(nbtComponentsSourceRoot)


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
    // apply java source sets
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
                vmArg("-Dfabric-api.datagen.output-dir=${file("src/main/generated-${project.name}")}")
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
    project.applySourceSets(nbtComponentsSourceSets)
//    val commonSourceRoot = "${rootProject.projectDir}/common"
    // apply kotlin source sets separately
//    configure<KotlinJvmProjectExtension> {
//        sourceSets {
//            named("main") {
//                kotlin {
//                    srcDir("${commonSourceRoot}/src/main/kotlin")
//                }
//            }
//            named("client") {
//                kotlin {
//                    srcDir("src/client/kotlin")
//                    srcDir("${commonSourceRoot}/src/client/kotlin")
//                }
//            }
//            named("test") {
//                kotlin {
//                    srcDir("${commonSourceRoot}/src/test/kotlin")
//                }
//            }
//        }
//    }

    // apply other source sets
    project.the<SourceSetContainer>().apply {
//        named("main") {
//            java {
//                srcDir("${commonSourceRoot}/src/main/java")
//            }
//            // additional common resources
//            resources {
//                srcDir("${commonSourceRoot}/src/main/resources")
//                srcDir("src/main/generated-${project.name}")
//            }
//        }
//        named("client") {
//            java {
//                srcDir("src/client/java")
//                srcDir("${commonSourceRoot}/src/client/java")
//            }
//            // additional resources for client
//            resources {
//                srcDir("src/client/resources")
//                srcDir("${commonSourceRoot}/src/client/resources")
//            }
//        }
        named("test") {
            compileClasspath += named("client").get().compileClasspath
            runtimeClasspath += named("client").get().runtimeClasspath
        }
    }

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

        curseforge {
            projectId = "892065"
            accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
            minecraftVersions.add(project.name)

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
            minecraftVersions.add(project.name)

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
}