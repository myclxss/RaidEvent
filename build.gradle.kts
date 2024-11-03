plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "1.7.2"
}

group = "plugin.myclass"
version = "1.0.0-SNAPSHOT"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

repositories {
    mavenCentral()
    mavenLocal()
    gradlePluginPortal()

    maven("https://repo.papermc.io/repository/maven-public/")

    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }

    maven {
        url = uri("https://jitpack.io")
    }

    maven {
        url = uri("https://repo.dmulloy2.net/repository/public/")
    }

    maven {
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }

    maven { url = uri("https://maven.enginehub.org/repo/") }
}

dependencies {
    paperweight.paperDevBundle("1.20.6-R0.1-SNAPSHOT")
    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
    compileOnly("me.clip:placeholderapi:2.11.6")

}

tasks {

    assemble {
        dependsOn(reobfJar)
    }

    compileJava {
        options.encoding = "UTF-8"
        options.release.set(21)
    }
    javadoc {
        options.encoding = "UTF-8"
    }

    paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.REOBF_PRODUCTION

    reobfJar {
        outputJar.set(layout.buildDirectory.file("libs/${project.name}-${project.version}.jar"))
    }

}