plugins {
    kotlin("jvm") version "2.4.20"
    kotlin("plugin.serialization") version "2.4.20"
    id("dev.reformator.stacktracedecoroutinator") version "2.6.6"
    application
}

repositories {
    mavenCentral()
    maven("https://repo.koder.wtf/maven/releases")
    maven("https://jitpack.io")
    maven("https://git.bossing.vip/api/packages/max/maven")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.8.0")

    implementation("io.ktor:ktor-client-core:3.6.0")
    implementation("io.ktor:ktor-client-cio:3.6.0")

    implementation("ng.bossi:Store:1.0.1")
    implementation("com.github.0xIO32:untis4j:get_teachers_workarround-SNAPSHOT")

    implementation("ch.qos.logback:logback-classic:1.6.3")
}


tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "MainKt"
        }

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(sourceSets.main.get().output)
        dependsOn(configurations.runtimeClasspath)
        from({
            configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
        })
    }
}
