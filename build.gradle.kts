plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    kotlin("jvm") version "1.7.21"
    kotlin("plugin.serialization") version "1.7.21"
}
ext {
    set("PUBLISH_GROUP_ID", "io.github.dadino.appinfoplugin")
    set("PUBLISH_VERSION", "1.2.4")
    set("PUBLISH_ARTIFACT_ID", "plugin")
    set("ossrhUsername", System.getProperty("ossrhUsername"))
    set("ossrhPassword", System.getProperty("ossrhPassword"))
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