import com.google.protobuf.gradle.id
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val grpcKotlinVersion: String by project
val grpcVersion: String by project
val protobufVersion: String by project
val kotlinXVersion: String by project

plugins {
    kotlin("jvm") version "1.7.20"
    id("com.google.protobuf") version "0.9.1"
    id("com.google.cloud.tools.jib") version "3.3.1"
}

group = "org.ale.pallotta"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinXVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlinXVersion")
    implementation("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion")
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation("com.google.protobuf:protobuf-kotlin:$protobufVersion")

    runtimeOnly("io.grpc:grpc-netty:$grpcVersion")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:$grpcKotlinVersion:jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
            it.builtins {
                id("kotlin")
            }
        }
    }
}

jib {
    to {
        image = "distributed-systems-algorithm"
    }

    container {
        ports = listOf("9000", "9001", "9002", "9003", "9004")
    }
}