rootProject.name = "nuvotifier"

pluginManagement {
    repositories {
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        mavenCentral()
        gradlePluginPortal()
        maven("https://repo.jpenilla.xyz/snapshots")

        includeBuild("build-logic")
    }
}

include("nuvotifier-api")
project(":nuvotifier-api").projectDir = file("api")


include("nuvotifier-common")
project(":nuvotifier-common").projectDir = file("common")

include("nuvotifier-fabric")
project(":nuvotifier-fabric").projectDir = file("fabric")

/**


include("nuvotifier-bukkit")
project(":nuvotifier-bukkit").projectDir = file("bukkit")
include("nuvotifier-bungeecord")
project(":nuvotifier-bungeecord").projectDir = file("bungeecord")
include("nuvotifier-sponge")
project(":nuvotifier-sponge").projectDir = file("sponge")
include("nuvotifier-velocity")
project(":nuvotifier-velocity").projectDir = file("velocity")


include("nuvotifier-universal")
project(":nuvotifier-universal").projectDir = file("universal")
 **/