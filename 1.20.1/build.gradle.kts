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

tasks.named<Test>("test") {
    useJUnitPlatform()
}

sourceSets {
    main {
        java {
            srcDir("../common/src/main/java")
        }
        kotlin {
            srcDir("../common/src/main/kotlin")
        }
        // additional common resources
        resources {
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
        // additional resources for client
        resources {
            srcDir("src/client/resources")
            srcDir("../common/src/client/resources")
        }
    }
    test {
        compileClasspath += named("client").get().compileClasspath
        runtimeClasspath += named("client").get().runtimeClasspath
        kotlin {
            srcDir("../common/src/test/kotlin")
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

    testImplementation("net.fabricmc:fabric-loader-junit:${loader_version}")
}

tasks {
    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand("version" to project.version)
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