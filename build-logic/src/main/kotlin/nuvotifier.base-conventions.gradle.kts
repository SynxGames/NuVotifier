plugins {

}

group = rootProject.group
version = rootProject.version
description = rootProject.description

repositories {
    gradlePluginPortal()
    maven("https://maven.fabricmc.net/")
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
    maven { url = uri("https://repo.spongepowered.org/maven/") }
}