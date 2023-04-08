plugins {
    id("nuvotifier.base-conventions")
    id("quiet-fabric-loom")
}

repositories {
    maven("https://maven.fabricmc.net/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:1.19.2")
    mappings(loom.officialMojangMappings())

    modImplementation("net.fabricmc:fabric-loader:0.14.17")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.59.0+1.19.2")
    modImplementation("me.lucko:fabric-permissions-api:0.2-SNAPSHOT")

    listOf(
        "configurate-core",
        "configurate-yaml",
    ).forEach { str ->
        val string = "org.spongepowered:$str:4.0.0"
        modImplementation(string)
        include(string)
    }

    include("org.yaml:snakeyaml:1.27") // Configurate
}