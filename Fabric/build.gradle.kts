val minecraftVersion: String by project
val modId: String by project
val aeVersion: String by project
val jeiVersion: String by project
val reiVersion: String by project
val fabricLoaderVersion: String by project
val fabricApiVersion: String by project
val fabricRecipeViewer: String by project

plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    if (project.findProperty("enableAccessWidener") == "true") { // optional property for `gradle.properties`
        accessWidenerPath.set(project(":Common").loom.accessWidenerPath)
        println("Access widener enabled for project ${project.name}. Access widener path: ${loom.accessWidenerPath.get()}")
    }

    runs {
        create("data") {
            client()
//            name("Minecraft Data")
            vmArg("-Dfabric-api.datagen")
            vmArg("-Dfabric-api.datagen.output-dir=${file("src/main/generated").absolutePath}")
            vmArg("-Dfabric-api.datagen.modid=${modId}")
            property("almost.datagen.platform", "fabric")
        }

        create("data_common") {
            client()
//            name("Minecraft Data Common")
            vmArg("-Dfabric-api.datagen")
            vmArg("-Dfabric-api.datagen.output-dir=${project(":Common").file("src/main/generated").absolutePath}")
            vmArg("-Dfabric-api.datagen.modid=${modId}")
            property("almost.datagen.platform", "common")
        }
    }
}

val common by configurations
val shadowCommon by configurations

dependencies {
    // loader
    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    modApi("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion+$minecraftVersion")

    // common module
    common(project(":Common", "namedElements")) { isTransitive = false }
    shadowCommon(project(":Common", "transformProductionFabric")) { isTransitive = false }

    // runtime mods
    modLocalRuntime("appeng:appliedenergistics2-fabric:$aeVersion")
    modLocalRuntime(
        when (fabricRecipeViewer) {
            "jei" -> "mezz.jei:jei-$minecraftVersion-fabric:$jeiVersion"
            "rei" -> "me.shedaniel:RoughlyEnoughItems-fabric:$reiVersion"
            else -> throw GradleException("Invalid fabricRecipeViewer value: $fabricRecipeViewer")
        }
    )
}

/**
 * force the fabric loader and api versions that are defined in the project
 * some mods ship another version which crashes the runtime
 */
configurations.all {
    resolutionStrategy {
        force("net.fabricmc:fabric-loader:$fabricLoaderVersion")
        force("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion+$minecraftVersion")
    }
}


//val common by configurations
//val shadowCommon by configurations
//dependencies {
//    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
//    modApi("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion+$minecraftVersion")
//
//    common(project(":Common", "namedElements")) { isTransitive = false }
//    shadowCommon(project(":Common", "transformProductionFabric")) { isTransitive = false }
//
//    modApi("com.almostreliable.mods:almostlib-fabric:1.19.2-0.0.1") { isChanging = true }
//    modCompileOnly("mezz.jei:jei-$minecraftVersion-common-api:$jeiVersion")
//    modCompileOnly("mezz.jei:jei-$minecraftVersion-fabric-api:$jeiVersion")
//    modCompileOnly("me.shedaniel:RoughlyEnoughItems-api-fabric:$reiVersion")
//    modCompileOnly(modLocalRuntime("appeng:appliedenergistics2-fabric:$aeVersion")!!)
//
//    when (fabricRecipeViewer) {
//        "rei" -> {
//            modLocalRuntime("dev.architectury:architectury-fabric:6.2.43") // manually override Architectury because transitive dep crashes
//            modLocalRuntime("me.shedaniel:RoughlyEnoughItems-fabric:$reiVersion")
//        }
//
//        "jei" -> modLocalRuntime("mezz.jei:jei-$minecraftVersion-fabric:$jeiVersion") { isTransitive = false }
//        else -> throw GradleException("Invalid recipeViewer value: $fabricRecipeViewer")
//    }
//}
