//import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    id("nuvotifier.loom-conventions")
}

//applyCommonConfiguration()

//applyPlatformAndCoreConfiguration()
//applyCommonArtifactoryConfig()
//applyShadowConfiguration()

repositories {

    mavenCentral()
}


dependencies {
    compileOnly(project(":nuvotifier-api"))
    include(project(":nuvotifier-api"))
    compileOnly(project(":nuvotifier-common"))
    include(project(":nuvotifier-common"))
}