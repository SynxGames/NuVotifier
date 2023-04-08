plugins {
    `java-library`
    id("nuvotifier.base-conventions")
}

//applyPlatformAndCoreConfiguration()
//applyCommonArtifactoryConfig()

dependencies {
    "implementation"("com.google.code.gson:gson:${Versions.GSON}")
}
