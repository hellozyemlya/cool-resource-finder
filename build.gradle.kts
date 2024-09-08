plugins {
//    id 'fabric-loom' version '1.3-SNAPSHOT' apply false
//    id "org.jetbrains.kotlin.jvm" version "1.9.0" apply false
//    id 'org.jetbrains.kotlin.plugin.serialization' version "1.9.0" apply false
//    id "me.modmuss50.mod-publish-plugin" version "0.1.1"
}

version = "1.0.0"
group = "junk"

//subprojects {
//    apply plugin: 'fabric-loom'
//    apply plugin: 'maven-publish'
//    apply plugin: 'org.jetbrains.kotlin.jvm'
//    apply plugin: 'org.jetbrains.kotlin.plugin.serialization'
//    apply plugin: 'me.modmuss50.mod-publish-plugin'
//
//    version = project.mod_version
//    group = project.maven_group
//
//
//    base {
//        archivesName = "${project.archives_base_name}-mc-${project.name}"
//    }
//
//    repositories {
//        // Add repositories to retrieve artifacts from in here.
//        // You should only use this when depending on other mods because
//        // Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
//        // See https://docs.gradle.org/current/userguide/declaring_repositories.html
//        // for more information about repositories.
//        maven { url = "https://jitpack.io" }
//        exclusiveContent {
//            forRepository {
//                maven {
//                    url "https://cursemaven.com"
//                }
//            }
//            filter {
//                includeGroup "curse.maven"
//            }
//        }
//    }
//
//    loom {
//        splitEnvironmentSourceSets()
//
//        mods {
//            "cool-resource-finder" {
//                sourceSet sourceSets.main
//                sourceSet sourceSets.client
//            }
//        }
//
//        runs {
//            // This adds a new gradle task that runs the datagen API: "gradlew runDatagen"
//            datagen {
//                inherit server
//                name "Data Generation"
//                vmArg "-Dfabric-api.datagen"
//                vmArg "-Dfabric-api.datagen.output-dir=${file("src/main/generated-${project.name}")}"
//                vmArg "-Dfabric-api.datagen.modid=cool-resource-finder"
//
//                runDir "build/datagen"
//            }
//        }
//    }
//
//    tasks.withType(JavaCompile).configureEach {
//        it.options.release = 17
//    }
//
//    tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile).all {
//        kotlinOptions {
//            jvmTarget = 17
//        }
//    }
//
//    java {
//        // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
//        // if it is present.
//        // If you remove this line, sources will not be generated.
//        withSourcesJar()
//
//        sourceCompatibility = JavaVersion.VERSION_17
//        targetCompatibility = JavaVersion.VERSION_17
//    }
//
//    jar {
//        from("LICENSE") {
//            rename { "${it}_${project.base.archivesName.get()}" }
//        }
//    }
//
//    processResources {
//        inputs.property "version", project.version
//
//        filesMatching("fabric.mod.json.bac") {
//            expand "version": project.version
//        }
//    }
//
//    sourceSets {
//        main {
//            resources {
//                srcDirs += [
//                        "src/main/generated-${project.name}"
//                ]
//            }
//        }
//    }
//
//    def sourcesRoot = "${rootProject.projectDir}/common/${project.name}"
//
//    sourceSets {
//        main {
//            java {
//                srcDirs += file("${sourcesRoot}/main/java")
//            }
//            kotlin {
//                srcDirs += file("${sourcesRoot}/main/kotlin")
//            }
//            resources {
//                srcDirs += file("${sourcesRoot}/main/resources")
//            }
//        }
//        client {
//            java {
//                srcDirs += file("${sourcesRoot}/client/java")
//            }
//            kotlin {
//                srcDirs += file("${sourcesRoot}/client/kotlin")
//            }
//            resources {
//                srcDirs += file("${sourcesRoot}/client/resources")
//            }
//        }
//
//        /**
//         * hack incremental source code build, this will add every java or kt file as a regular input
//         * from common src folder (the one that source for module-specific symlink) to ensure gradle can detect their
//         * timestamp changes and mark job as stale
//         * kotlin sources - done
//         * java sources - done
//         * resources(models/textures/recipes) - done
//         * TODO is all jobs covered?
//         */
//        tasks.compileKotlin {
//            inputs.files fileTree("${rootProject.projectDir}/common/src/main").matching {
//                include "**/*.kt"
//            }
//        }
//        tasks.compileJava {
//            inputs.files fileTree("${rootProject.projectDir}/common/src/main").matching {
//                include "**/*.java"
//            }
//        }
//
//        tasks.compileClientKotlin {
//            inputs.files fileTree("${rootProject.projectDir}/common/src/client").matching {
//                include "**/*.kt"
//            }
//        }
//        tasks.compileClientJava {
//            inputs.files fileTree("${rootProject.projectDir}/common/src/client").matching {
//                include "**/*.java"
//            }
//        }
//
//        tasks.processClientResources {
//            inputs.files fileTree("${rootProject.projectDir}/common/src/client").matching {
//                exclude '**/*.java'
//                exclude '**/*.kt'
//            }
//        }
//        tasks.processResources {
//            inputs.files fileTree("${rootProject.projectDir}/common/src/main").matching {
//                exclude '**/*.java'
//                exclude '**/*.kt'
//            }
//        }
//    }
//
//    // configure the maven publication
//    publishing {
//        publications {
//            mavenJava(MavenPublication) {
//                from components.java
//            }
//        }
//
//        // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
//        repositories {
//            // Add repositories to publish to here.
//            // Notice: This block does NOT have the same function as the block in the top level.
//            // The repositories here will be used for publishing your artifact, not for
//            // retrieving dependencies.
//        }
//    }
//    dependencies {
//        // To change the versions see the gradle.properties file
//        minecraft "com.mojang:minecraft:${project.minecraft_version}"
//        mappings "net.fabricmc:yarn:${project.yarn_mappings}:v2"
//        modImplementation "net.fabricmc:fabric-loader:${project.loader_version}"
//
//        // Fabric API. This is technically optional, but you probably want it anyway.
//        modImplementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_version}"
//        modImplementation "net.fabricmc:fabric-language-kotlin:${project.fabric_kotlin_version}"
//        modImplementation "net.silkmc:silk-network:${project.silkmc_version}"
//        modImplementation "net.silkmc:silk-persistence:${project.silkmc_version}"
//    }
//
//    publishMods {
//        file = remapJar.archiveFile
//        changelog = project.mod_changelog
//        type = BETA
//        modLoaders.add("fabric")
//
//        curseforge {
//            projectId = "892065"
//            accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
//            minecraftVersions.add(project.name)
//
//            requires {
//                slug = "fabric-api"
//            }
//            requires {
//                slug = "fabric-language-kotlin"
//            }
//        }
//        modrinth {
//            projectId = "eKgd4diD"
//            accessToken = providers.environmentVariable("MODRINTH_TOKEN")
//            minecraftVersions.add(project.name)
//
//            requires {
//                // fabric-api
//                projectId = "P7dR8mSH"
//            }
//            requires {
//                // fabric-language-kotlin
//                projectId = "Ha28R6CL"
//            }
//            requires {
//                // Silk
//                projectId = "aTaCgKLW"
//            }
//        }
//    }
//}