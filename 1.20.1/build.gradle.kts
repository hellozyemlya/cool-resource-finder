plugins {
    id("fabric-loom") version "1.3-SNAPSHOT"
    id("org.jetbrains.kotlin.jvm") version "1.9.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"
}

val mod_version: String by project
val maven_group: String by project
version = mod_version
group = maven_group

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

loom {
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

sourceSets {
    main {
        java {
            srcDir("src/main/java")
            srcDir("../common/src/main/java")
        }
        kotlin {
            srcDir("src/main/kotlin")
            srcDir("../common/src/main/kotlin")
        }
        resources {
            srcDir("src/main/resources")
            srcDir("../common/src/main/resources")
            srcDir("src/main/generated-${project.name}")
        }
    }
    named("client") {
        java {
            srcDir("src/client/java")
            srcDir("../common/src/client/java")
        }
        kotlin {
            srcDir("src/client/kotlin")
            srcDir("../common/src/client/kotlin")
        }
        resources {
            srcDir("src/client/resources")
            srcDir("../common/src/client/resources")
        }
    }
}

val minecraft_version: String by project
val yarn_mappings: String by project
val loader_version: String by project
val fabric_version: String by project
val fabric_kotlin_version: String by project
val silkmc_version: String by project

dependencies {
    minecraft("com.mojang:minecraft:${minecraft_version}")
    mappings("net.fabricmc:yarn:${yarn_mappings}:v2")
    modImplementation("net.fabricmc:fabric-loader:${loader_version}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${fabric_version}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${fabric_kotlin_version}")
    modImplementation("net.silkmc:silk-network:${silkmc_version}")
    modImplementation("net.silkmc:silk-persistence:${silkmc_version}")
}

tasks {
    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand("version" to project.version)
        }
    }
}
tasks.register("listResources") {
    doLast {
        val duplicates = mutableSetOf<String>()
        val seen = mutableSetOf<String>()

        // Create a file collection for source directories
        val resourceDirs = files(sourceSets["main"].resources.srcDirs)

        // Use fileTree to create a tree of files from the file collection
        val fileTree: FileTree = fileTree(resourceDirs)

        // Visit each file and check for duplicates
        fileTree.visit(object : Action<FileVisitDetails> {
            override fun execute(details: FileVisitDetails) {
                val path = details.relativePath.pathString
                if (seen.contains(path)) {
                    duplicates.add(path)
                } else {
                    seen.add(path)
                }
            }
        })

        // Output results
        if (duplicates.isNotEmpty()) {
            println("Duplicate resources found:")
            duplicates.forEach { println(it) }
        } else {
            println("No duplicate resources found.")
        }
    }
}
tasks.withType<JavaCompile>().configureEach {
    options.release = 17
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "17"
    }
}
java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

//tasks.withType<Copy>().configureEach {
//    duplicatesStrategy = DuplicatesStrategy.WARN
//}