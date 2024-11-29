plugins {
    java
//    id("org.springframework.boot") version "3.1.4"
    id("org.springframework.boot") version "2.7.16"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.spring.dependency-management") version "1.1.3"
}

group = "com.hxl.plugin"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}
tasks.jar {
    archiveFileName.set("spring-invoke-starter.jar")
    val contents = configurations.runtimeClasspath.get()
        .map { if (it.isDirectory) it else zipTree(it) }
    from(contents)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

repositories {
    maven { url = uri("https://maven.aliyun.com/repository/public/") }
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
    maven { url = uri("https://repo.spring.io/snapshot") }

}

tasks.shadowJar {
    append("/spring-test-4.0.0.jar")
    archiveFileName.set("spring-invoke-starter.jar")
    mergeServiceFiles()
    relocate("org.apache.commons.beanutils", "com.cool.request.starter.lib.net.commons.beanutils")
    relocate("org.apache.commons.beanutils", "com.cool.request.starter.lib.net.commons.beanutils")
    relocate("org.apache.commons.collections", "com.cool.request.starter.lib.net.commons.collections")
    relocate("org.apache.commons.logging", "com.cool.request.starter.lib.net.commons.logging")
    relocate("com.google.gson", "com.cool.request.starter.lib.net.gson")
}

dependencies {
    compileOnly("org.springframework.boot:spring-boot-starter-web")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    compileOnly("org.springframework.boot:spring-boot-starter-test")
    implementation("commons-beanutils:commons-beanutils:1.9.4")
    implementation("com.google.code.gson:gson:2.8.9")

    compileOnly("com.fasterxml.jackson.core:jackson-databind:2.12.5")
    compileOnly("org.springframework.cloud:spring-cloud-starter-gateway")
    compileOnly("org.springframework:spring-test:5.3.25") {
        exclude(group = "org.springframework", module = "spring-core")
    }
    compileOnly("com.xuxueli:xxl-job-core:2.4.0")
    compileOnly("org.springframework.boot:spring-boot-starter-actuator")

}
dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2021.0.3")
    }
}