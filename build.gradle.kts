@file:Suppress("UnstableApiUsage")

val license: String by project
val minecraftVersion: String by project
val modVersion: String by project
val modPackage: String by project
val modId: String by project
val modName: String by project
val modAuthor: String by project
val modDescription: String by project
val forgeRecipeViewer: String by project
val githubUser: String by project
val githubRepo: String by project

plugins {
    alias(deps.plugins.arch)
    alias(deps.plugins.buildconfig)
    java
    `maven-publish`
}

base {
    version = "$minecraftVersion-$modVersion"
    archivesName.set("$modId-forge")
}

sourceSets {
    main {
        resources {
            srcDir(file("src/main/generated"))
            exclude("**/.cache")
        }
    }
}

loom {
    silentMojangMappingsLicense()

    runs {
        create("data") {
            data()
            programArgs("--all", "--mod", modId)
            programArgs("--output", file("src/main/generated").absolutePath)
            property("almost.datagen.platform", "common,forge")
        }

        forEach {
            val dir = "run/${it.environment}"
            println("[Run Config] '${it.name}' directory: $dir")
            it.runDir(dir)
            // allows DCEVM hot-swapping when using the JBR (https://github.com/JetBrains/JetBrainsRuntime)
            it.vmArgs("-XX:+IgnoreUnrecognizedVMOptions", "-XX:+AllowEnhancedClassRedefinition")
        }
    }

    forge {
        mixinConfig("$modId.mixins.json")
    }
}

repositories {
    maven("https://oss.sonatype.org/content/repositories/snapshots") // Manifold
    maven("https://maven.parchmentmc.org") // Parchment
    maven("https://modmaven.dev") // AE2
    maven("https://maven.blamejared.com") // JEI
    maven("https://maven.shedaniel.me") // REI
    maven("https://cursemaven.com") // Jade
    mavenLocal()
}

dependencies {
    // Minecraft
    minecraft(deps.minecraft)
    mappings(loom.layered {
        officialMojangMappings()
        parchment(variantOf(deps.parchment) { artifactType("zip") })
    })

    // Forge
    forge("net.minecraftforge:forge:$minecraftVersion-${deps.versions.forge.get()}")

    // Manifold
    compileOnly(deps.manifold.rt)
    annotationProcessor(deps.manifold.ext)

    // Almost Lib
    if (project.findProperty("almostLibLocal") == "true") {
        modApi("com.almostreliable.mods:almostlib-forge:${deps.versions.almostlib.get()}+") {
            isChanging = true
        }
    } else {
        modApi(deps.almostlib)
    }

    // Applied Energistics
    modImplementation(deps.ae2)

    // Recipe Viewers
    modCompileOnly(deps.jei.api)
    modCompileOnly(deps.rei.api)
    when (forgeRecipeViewer) {
        "jei" -> modLocalRuntime(deps.jei.mod) { isTransitive = false }
        "rei" -> modLocalRuntime(deps.rei.mod)
        else -> throw GradleException("Invalid recipeViewer value: $forgeRecipeViewer")
    }

    // Runtime
    modLocalRuntime("curse.maven:jade-324717:4719987") // Jade 8.9.0
}

tasks {
    processResources {
        val resourceTargets = listOf("META-INF/mods.toml", "pack.mcmeta")

        val replaceProperties = mapOf(
            "license" to license,
            "minecraftVersion" to minecraftVersion,
            "version" to project.version as String,
            "modId" to modId,
            "modName" to modName,
            "modAuthor" to modAuthor,
            "modDescription" to modDescription,
            "forgeVersion" to deps.versions.forge.get(),
            // use major version for FML only because wrong Forge version error message
            // is way better than FML error message
            "forgeFMLVersion" to deps.versions.forge.get().substringBefore("."),
            "almostLibVersion" to deps.versions.almostlib.get(),
            "aeVersion" to deps.versions.ae2.get(),
            "jeiVersion" to deps.versions.jei.get(),
            "reiVersion" to deps.versions.rei.get(),
            "githubUser" to githubUser,
            "githubRepo" to githubRepo
        )

        println("[Process Resources] Replacing resource properties: ")
        replaceProperties.forEach { (key, value) -> println("\t -> $key = $value") }

        inputs.properties(replaceProperties)
        filesMatching(resourceTargets) {
            expand(replaceProperties)
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(17)
        options.compilerArgs.add("-Xplugin:Manifold no-bootstrap")
    }

    withType<GenerateModuleMetadata> {
        enabled = false
    }
}

extensions.configure<JavaPluginExtension> {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

buildConfig {
    buildConfigField("String", "MOD_ID", "\"$modId\"")
    buildConfigField("String", "MOD_NAME", "\"$modName\"")
    buildConfigField("String", "MOD_VERSION", "\"$version\"")
    packageName(modPackage)
    className(modName.replace(" ", "") + "Constants")
    useJavaOutput()
}
