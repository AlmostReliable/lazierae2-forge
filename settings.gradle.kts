pluginManagement {
    repositories {
        maven("https://maven.architectury.dev/")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.minecraftforge.net/")
        gradlePluginPortal()
        mavenCentral()
    }
}

val modName: String by extra
val mcVersion: String by extra
rootProject.name = "$modName-$mcVersion"
