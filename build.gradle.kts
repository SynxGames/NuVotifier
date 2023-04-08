plugins {
    id("nuvotifier.base-conventions")
    //id("net.kyori.blossom") version "1.2.0" apply false
    //id("fabric-loom") version "1.1-SNAPSHOT" apply false // Fabric Workaround
}

logger.lifecycle("""
*******************************************
 You are building NuVotifier!
 If you encounter trouble:
 1) Try running 'build' in a separate Gradle run
 2) Use gradlew and not gradle
 3) If you still need help, you should reconsider building NuVotifier!

 Output files will be in [subproject]/build/libs
*******************************************
""")


//applyRootArtifactoryConfig()