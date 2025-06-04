pluginManagement {
    repositories {
        maven("https://maven.neoforged.net/releases")
        gradlePluginPortal()
    }
}

val modName = extra.get("modName").toString().replace(" ", "-")
val mcVersion: String by extra
rootProject.name = "$modName-$mcVersion-NeoForge"
