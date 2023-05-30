plugins {
    kotlin("multiplatform") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    application
}

group = "nl.parkeerassistent"
version = "1.0"

repositories {
    jcenter()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
}

val ktorVersion = "1.6.3"
val serializationVersion = "1.1.0"

kotlin {
    val commonMain = sourceSets["commonMain"]
    jvm {
//        compilations {
//            val main by getting
//
//            val mock by compilations.creating {
//                defaultSourceSet {
//                    dependsOn(commonMain)
//                    dependencies {
//                        implementation(main.compileDependencyFiles + main.output.classesDirs)
//                    }
//                }
//            }
//
//            tasks.register<Jar>("mockJar") {
//                group = "build"
//                manifest {
//                    attributes["Main-Class"] = "nl.parkeerassistent.mock.MockServerKt"
//                }
//                archiveBaseName.set("${project.name}-mock")
//                from(mock.output.classesDirs)
//            }
//        }
        compilations.all {
            kotlinOptions.jvmTarget = "15"
        }
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
        withJava()
    }
    js(LEGACY) {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$serializationVersion")
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.1.1")
                implementation("org.slf4j:slf4j-log4j12:1.7.30")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-server-core:$ktorVersion")
                implementation("io.ktor:ktor-server-netty:$ktorVersion")
                implementation("io.ktor:ktor-html-builder:$ktorVersion")
                implementation("io.ktor:ktor-serialization:$ktorVersion")
                implementation("io.ktor:ktor-client-java:$ktorVersion")
                implementation("io.ktor:ktor-client-serialization:$ktorVersion")
                implementation("io.ktor:ktor-client-logging:$ktorVersion")
                implementation("org.jetbrains:kotlin-css-jvm:1.0.0-pre.148-kotlin-1.4.30")
                implementation("org.jsoup:jsoup:1.13.1")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains:kotlin-react:16.13.1-pre.113-kotlin-1.4.0")
                implementation("org.jetbrains:kotlin-react-dom:16.13.1-pre.113-kotlin-1.4.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")
                implementation("org.jetbrains:kotlin-styled:1.0.0-pre.113-kotlin-1.4.0")
                implementation("io.ktor:ktor-client-js:$ktorVersion")
                implementation("io.ktor:ktor-client-json-js:$ktorVersion")
                implementation("io.ktor:ktor-client-serialization-js:$ktorVersion")

                implementation(npm("react", "16.14.0"))
                implementation(npm("react-bootstrap", "1.5.2"))
                implementation(npm("react-swipeable", "5.5.1"))
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}

application {
    mainClass.set("ServerKt")
}

tasks.register("docker") {
    dependsOn("build", "assembleDist", "installDist")
}

tasks.named<Copy>("jvmProcessResources") {
    val jsBrowserDistribution = tasks.named("jsBrowserDistribution")
    from(jsBrowserDistribution)
}

tasks.named<JavaExec>("run") {
    dependsOn(tasks.named<Jar>("jvmJar"))
    classpath(tasks.named<Jar>("jvmJar"))
}

tasks.withType(JavaExec::class.java) {
    file(".env").takeIf { it.exists() }?.readLines()?.forEach {s ->
        val (key, value) = s.split("=")
        environment(key to value)
    }
}
