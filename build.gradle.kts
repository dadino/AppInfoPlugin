plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.serialization") version "2.2.20"
    id("com.vanniktech.maven.publish") version "0.36.0"
    id("org.jetbrains.dokka") version "2.1.0"
}

ext {
    set("PUBLISH_GROUP_ID", "io.github.dadino.appinfoplugin")
    set("PUBLISH_VERSION", "3.0.0")
    set("PUBLISH_ARTIFACT_ID", "plugin")
}

apply {
    from("${rootProject.projectDir}/scripts/vannitech-mavencentral.gradle")
}

gradlePlugin {
    plugins {
        create("AppInfoPlugin") {
            id = "io.github.dadino.appinfoplugin"
            implementationClass = "com.appinfoplugin.AppInfoPlugin"
        }
    }
}
group = "com.appinfoplugin"
version = ext.properties["PUBLISH_VERSION"]!!

java {
    // withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly("com.android.tools.build:gradle-api:8.13.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
}