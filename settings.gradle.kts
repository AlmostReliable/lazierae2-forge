pluginManagement {
    repositories {
        maven("https://maven.architectury.dev/")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.minecraftforge.net/")
        gradlePluginPortal()
    }
}

val modName: String by extra
val minecraftVersion: String by extra
rootProject.name = "$modName-$minecraftVersion-Forge"

dependencyResolutionManagement {
    versionCatalogs {
        create("deps") {
            version("arch", "1.4.+")
            version("buildconfig", "5.3.5")
            version("parchment", "2022.11.27")
            version("forge", "43.2.0")
            version("manifold", "2024.1.1")
            version("almostlib", "$minecraftVersion-0.0.1")
            version("ae2", "12.9.8")
            version("jei", "11.6.0.1015")
            version("rei", "9.1.610")

            plugin("arch", "dev.architectury.loom").versionRef("arch")
            plugin("buildconfig", "com.github.gmazzo.buildconfig").versionRef("buildconfig")

            library("minecraft", "com.mojang", "minecraft").version(minecraftVersion)
            library("parchment", "org.parchmentmc.data", "parchment-$minecraftVersion").versionRef("parchment")
            library("manifold-rt", "systems.manifold", "manifold-ext-rt").versionRef("manifold")
            library("manifold-ext", "systems.manifold", "manifold-ext").versionRef("manifold")
            library("almostlib", "com.almostreliable.mods", "almostlib-forge").versionRef("almostlib")
            library("ae2", "appeng", "appliedenergistics2-forge").versionRef("ae2")
            library("jei-api", "mezz.jei", "jei-$minecraftVersion-forge-api").versionRef("jei")
            library("jei-mod", "mezz.jei", "jei-$minecraftVersion-forge").versionRef("jei")
            library("rei-api", "me.shedaniel", "RoughlyEnoughItems-api-forge").versionRef("rei")
            library("rei-mod", "me.shedaniel", "RoughlyEnoughItems-forge").versionRef("rei")
        }
    }
}
