val minecraftVersion: String by project
val modId: String by project
val aeVersion: String by project
val jeiVersion: String by project
val reiVersion: String by project
val forgeVersion: String by project
val forgeRecipeViewer: String by project

val extraModsPrefix = "extra-mods"

plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

architectury {
    platformSetupLoomIde()
    forge()
}

loom {
    if (project.findProperty("enableAccessWidener") == "true") { // optional property for `gradle.properties`
        accessWidenerPath.set(project(":Common").loom.accessWidenerPath)
        forge {
            convertAccessWideners.set(true)
            extraAccessWideners.add(loom.accessWidenerPath.get().asFile.name)
        }
        println("Access widener enabled for project ${project.name}. Access widener path: ${loom.accessWidenerPath.get()}")
    }

    runs {
        create("data") {
            data()
            programArgs("--all", "--mod", modId)
            programArgs("--output", file("src/main/generated").absolutePath)
            property("almost.datagen.platform", "forge")
        }
    }

    forge {
        mixinConfigs("$modId-common.mixins.json" /*, "$modId-forge.mixins.json" */)
    }
}

val common by configurations
val shadowCommon by configurations

dependencies {
    // loader
    forge("net.minecraftforge:forge:$minecraftVersion-$forgeVersion")

    // common module
    common(project(":Common", "namedElements")) { isTransitive = false }
    shadowCommon(project(":Common", "transformProductionForge")) { isTransitive = false }

    // runtime mods
    modLocalRuntime("appeng:appliedenergistics2-forge:$aeVersion")
    modLocalRuntime(
        when (forgeRecipeViewer) {
            "jei" -> "mezz.jei:jei-$minecraftVersion-forge:$jeiVersion"
            "rei" -> "me.shedaniel:RoughlyEnoughItems-forge:$reiVersion"
            else -> throw GradleException("Invalid forgeRecipeViewer value: $forgeRecipeViewer")
        }
    )

    /**
     * helps to load mods in development through an extra directory
     * sadly, this does not support transitive dependencies
     */
    fileTree("$extraModsPrefix-$minecraftVersion") { include("**/*.jar") }
        .forEach { f ->
            val sepIndex = f.nameWithoutExtension.lastIndexOf('-')
            if (sepIndex == -1) {
                throw IllegalArgumentException("Invalid mod name: '${f.nameWithoutExtension}'. Expected format: 'modName-version.jar'")
            }
            val mod = f.nameWithoutExtension.substring(0, sepIndex)
            val version = f.nameWithoutExtension.substring(sepIndex + 1)
            println("Extra mod ${f.nameWithoutExtension} detected.")
            "modLocalRuntime"("extra-mods:$mod:$version")
        }
}

//val common by configurations
//val shadowCommon by configurations
//dependencies {
//    forge("net.minecraftforge:forge:$minecraftVersion-$forgeVersion")
//
//    common(project(":Common", "namedElements")) { isTransitive = false }
//    shadowCommon(project(":Common", "transformProductionForge")) { isTransitive = false }
//
//    modApi("com.almostreliable.mods:almostlib-forge:1.19.2-0.0.1") { isChanging = true }
//    modCompileOnly("mezz.jei:jei-$minecraftVersion-common-api:$jeiVersion")
//    modCompileOnly("mezz.jei:jei-$minecraftVersion-forge-api:$jeiVersion")
//    modCompileOnly("me.shedaniel:RoughlyEnoughItems-api-forge:$reiVersion")
//    modCompileOnly(modLocalRuntime("appeng:appliedenergistics2-forge:$aeVersion")!!)
//
//    when (forgeRecipeViewer) {
//        "rei" -> modLocalRuntime("me.shedaniel:RoughlyEnoughItems-forge:$reiVersion")
//        "jei" -> modLocalRuntime("mezz.jei:jei-$minecraftVersion-forge:$jeiVersion") { isTransitive = false }
//        else -> throw GradleException("Invalid recipeViewer value: $forgeRecipeViewer")
//    }
//}
