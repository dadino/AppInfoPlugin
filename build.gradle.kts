plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    kotlin("jvm") version "1.7.21"
    kotlin("plugin.serialization") version "1.7.21"
}
ext {
    set("PUBLISH_GROUP_ID", "io.github.dadino.appinfoplugin")
    set("PUBLISH_VERSION", "2.0.1")
    set("PUBLISH_ARTIFACT_ID", "plugin")
    set("ossrhToken", System.getProperty("ossrhToken"))
    set("ossrhTokenPassword", System.getProperty("ossrhTokenPassword"))
    set("sonatypeStagingProfileId", System.getProperty("sonatypeStagingProfileId"))
}

apply {
    from("${rootProject.projectDir}/scripts/publish-mavencentral.gradle")
}

gradlePlugin {
    plugins {
        create("AppInfoPlugin") {
            id = "com.appinfoplugin"
            implementationClass = "com.appinfoplugin.AppInfoPlugin"
        }
    }
}
group = "com.appinfoplugin"
version = ext.properties["PUBLISH_VERSION"]!!

java {
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.android.tools.build:gradle:7.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
}