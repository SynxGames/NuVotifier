plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven {
        name = "sponge"
        url = uri("https://repo.spongepowered.org/repository/maven-public/")
    }
    maven("https://repo.jpenilla.xyz/snapshots")
}

dependencies {

    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.10")
    implementation("quiet-fabric-loom:quiet-fabric-loom.gradle.plugin:1.0-SNAPSHOT")

    /**
    implementation("org.ajoberstar.grgit:grgit-gradle:4.1.1")
    implementation("gradle.plugin.com.github.johnrengelman:shadow:7.1.2")
    implementation("org.jfrog.buildinfo:build-info-extractor-gradle:4.27.1")
    implementation("org.spongepowered:spongegradle-plugin-development:2.0.0")
    implementation("net.kyori:blossom:1.3.1")
    **/
}
