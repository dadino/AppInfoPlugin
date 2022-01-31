plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
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
version = "1.0.0"

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.android.tools.build:gradle:4.0.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
}