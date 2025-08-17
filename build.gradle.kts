plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17"
    id("com.gradleup.shadow") version "9.0.0-beta13"
}

group = "plugin.myclass"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://jitpack.io")
}

dependencies {
    paperweight.paperDevBundle("1.21.7-R0.1-SNAPSHOT")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
    compileOnly("org.projectlombok:lombok:1.18.34")
    compileOnly("net.kyori:adventure-nbt:4.17.0")
    compileOnly("me.clip:placeholderapi:2.11.6")
    implementation("org.mongodb:mongodb-driver-sync:5.5.1")
    implementation("com.github.Revxrsal.Lamp:common:3.1.5")
    implementation("com.github.Revxrsal.Lamp:bukkit:3.1.5")
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }

    compileJava {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    shadowJar {

        archiveClassifier.set("dev") // This removes the -all suffix
        archiveBaseName.set(project.name)
    }

    reobfJar {
        inputJar.set(shadowJar.flatMap { it.archiveFile })
        outputJar.set(layout.buildDirectory.file("libs/${project.name}-${project.version}.jar"))
    }
}