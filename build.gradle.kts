plugins {
    java
//    id("org.springframework.boot") version "3.1.4"
    id("org.springframework.boot") version "2.7.16"
    id("io.spring.dependency-management") version "1.1.3"
}

group = "com.hxl.plugin"
version = "0.0.1-SNAPSHOT"
java {
    sourceCompatibility = JavaVersion.VERSION_17
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}
tasks.jar{
    archiveFileName.set("spring-invoke-starter.jar")
    val contents = configurations.runtimeClasspath.get()
        .map { if (it.isDirectory) it else zipTree(it) }
    from(contents)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

repositories {
    maven { url =uri ("https://maven.aliyun.com/repository/public/") }
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
    maven { url = uri("https://repo.spring.io/snapshot") }
}

dependencies {
    compileOnly("org.springframework.boot:spring-boot-starter-web")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0")
// https://mvnrepository.com/artifact/javax.servlet/javax.servlet-api
    compileOnly("javax.servlet:javax.servlet-api:4.0.1")

    compileOnly("org.springframework:spring-test:5.3.25"){
        exclude(group="org.springframework",module = "spring-core")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
