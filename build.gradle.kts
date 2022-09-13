@file:Suppress("UnstableApiUsage")

import java.text.SimpleDateFormat
import java.util.*

val modId: String by project
val modName: String by project
val modVersion: String by project
val modBase: String by project
val modAuthor: String by project
val modDescription: String by project
val modCredits: String by project
val mcVersion: String by project
val mcVersionRange: String by project
val forgeVersion: String by project
val forgeVersionRange: String by project
val mappingsChannel: String by project
val mappingsVersion: String by project
val license: String by project
val githubUser: String by project
val githubRepo: String by project
val ae2Version: String by project
val ae2VersionRange: String by project
val jeiVersion: String by project
val jeiVersionRange: String by project
val crtVersion: String by project
val crtVersionRange: String by project
val kubeVersion: String by project
val kubeVersionRange: String by project

plugins {
    id("dev.architectury.loom") version "0.12.0-SNAPSHOT"
    id("com.github.gmazzo.buildconfig") version "3.0.3"
    java
    idea
    eclipse
}

base {
    version = "$mcVersion-$modVersion"
    group = "$modBase.$modId"
    archivesName.set(modId)
}

loom {
    silentMojangMappingsLicense()

    forge {
        mixinConfig("$modId.mixins.json")
        dataGen {
            mod(modId)
        }
    }

    launches {
        named("client") {
            // property("fabric.log.level", "debug")
            arg("-XX:+IgnoreUnrecognizedVMOptions", "-XX:+AllowEnhancedClassRedefinition")
        }
        named("server") {
            // property("fabric.log.level", "debug")
            arg("-XX:+IgnoreUnrecognizedVMOptions", "-XX:+AllowEnhancedClassRedefinition")
        }
        named("data") {
            arg("--existing", file("src/main/resources").absolutePath)
        }
    }
}

repositories {
    maven("https://maven.parchmentmc.org/")
    maven("https://modmaven.dev/")
    maven("https://maven.blamejared.com/")
    maven("https://maven.saps.dev/minecraft/")
    flatDir {
        name = "extra-mods"
        dir(file("extra-mods-$mcVersion"))
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:$mappingsChannel-$mcVersion:$mappingsVersion@zip")
    })
    forge("net.minecraftforge:forge:$mcVersion-$forgeVersion")

    modCompileOnly(modLocalRuntime("appeng:appliedenergistics2:$ae2Version")!!)

    modCompileOnlyApi("mezz.jei:jei-$mcVersion:$jeiVersion:api")
    modLocalRuntime("mezz.jei:jei-$mcVersion:$jeiVersion")

    modCompileOnly(modLocalRuntime("com.blamejared.crafttweaker:CraftTweaker-forge-$mcVersion:$crtVersion")!!)

    modCompileOnly(modLocalRuntime("dev.latvian.mods:kubejs-forge:$kubeVersion")!!)

    fileTree("extra-mods-$mcVersion") { include("**/*.jar") }
        .forEach { f ->
            val sepIndex = f.nameWithoutExtension.lastIndexOf('-')
            if (sepIndex == -1) {
                throw IllegalArgumentException("Invalid mod name: ${f.nameWithoutExtension}")
            }
            val mod = f.nameWithoutExtension.substring(0, sepIndex)
            val version = f.nameWithoutExtension.substring(sepIndex + 1)
            println("Extra mod $mod with version $version detected")
            modLocalRuntime("extra-mods:$mod:$version")
        }
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(17)
    }
    withType<GenerateModuleMetadata> {
        enabled = false
    }
    jar {
        manifest {
            attributes(
                "Specification-Title" to modName,
                "Specification-Vendor" to modAuthor,
                "Specification-Version" to "1",
                "Implementation-Title" to project.name,
                "Implementation-Version" to modVersion,
                "Implementation-Vendor" to modAuthor,
                "Implementation-Timestamp" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date()),
                "Timestamp" to System.currentTimeMillis(),
                "Built-On-Java" to "${System.getProperty("java.vm.version")} (${System.getProperty("java.vm.vendor")})",
                "Build-On-Minecraft" to mcVersion
            )
        }
    }
    processResources {
        val resourceTargets = listOf("META-INF/mods.toml", "pack.mcmeta")
        val replaceProperties = mapOf(
            "modId" to modId,
            "modName" to modName,
            "modVersion" to modVersion,
            "modAuthor" to modAuthor,
            "modDescription" to modDescription,
            "modCredits" to modCredits,
            "mcVersionRange" to mcVersionRange,
            "forgeVersionRange" to forgeVersionRange,
            "license" to license,
            "githubUser" to githubUser,
            "githubRepo" to githubRepo,
            "ae2VersionRange" to ae2VersionRange,
            "jeiVersionRange" to jeiVersionRange,
            "crtVersionRange" to crtVersionRange,
            "kubeVersionRange" to kubeVersionRange
        )

        inputs.properties(replaceProperties)
        filesMatching(resourceTargets) {
            expand(replaceProperties)
        }
    }
}

extensions.configure<JavaPluginExtension> {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

buildConfig {
    buildConfigField("String", "MOD_ID", "\"$modId\"")
    buildConfigField("String", "MOD_VERSION", "\"$modVersion\"")
    buildConfigField("String", "MOD_NAME", "\"$modName\"")
    buildConfigField("String", "MOD_GROUP", "\"$modBase.$modId\"")

    packageName("$modBase.$modId")
}
