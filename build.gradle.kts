import dev.monosoul.jooq.RecommendedVersions
import org.gradle.kotlin.dsl.generateJooqClasses
import org.gradle.kotlin.dsl.invoke

plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.allopen") version "2.0.21"
    id("io.quarkus")
    id("dev.monosoul.jooq-docker") version "6.1.19"
}

repositories {
    mavenCentral()
    mavenLocal()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    implementation("io.quarkus:quarkus-elytron-security")
    implementation("io.quarkiverse.jooq:quarkus-jooq:2.0.1")
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-flyway")
    implementation("io.quarkus:quarkus-container-image-docker")
    implementation("io.quarkus:quarkus-smallrye-openapi")
    implementation("io.quarkus:quarkus-rest-jackson")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-jackson")
    implementation("io.quarkus:quarkus-smallrye-jwt")
    implementation("io.smallrye:smallrye-jwt:4.6.1")
    implementation("io.quarkus:quarkus-kubernetes-config")
    implementation("io.quarkus:quarkus-kubernetes")
//    implementation("io.quarkus:quarkus-minikube")
//    implementation("io.quarkus:quarkus-helm")
    implementation("io.quarkiverse.helm:quarkus-helm:1.2.7")
    implementation("io.quarkus:quarkus-smallrye-jwt-build")
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.quarkus:quarkus-arc")
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("io.rest-assured:kotlin-extensions")

    implementation("org.jooq:jooq:${RecommendedVersions.JOOQ_VERSION}")
    implementation("org.jooq:jooq-kotlin:${RecommendedVersions.JOOQ_VERSION}")
    jooqCodegen("org.postgresql:postgresql:42.7.2")
    implementation("org.postgresql:postgresql:42.7.2")
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.testcontainers:postgresql:1.17.6")
    }
}

group = "site.uartman"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.persistence.Entity")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
        javaParameters = true
    }
}

tasks.generateJooqClasses {
    usingJavaConfig {
        withName("org.jooq.codegen.KotlinGenerator")
        generate.apply {
            withPojosAsKotlinDataClasses(true)
            withKotlinNotNullRecordAttributes(true)
            withKotlinNotNullPojoAttributes(true)
            withKotlinNotNullInterfaceAttributes(true)
            withDaos(true)
        }
    }
}
