plugins {
    java
//    id("org.springframework.boot") version "3.1.4"
    id("org.springframework.boot") version "2.7.16"
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
    implementation("commons-beanutils:commons-beanutils:1.9.4")

    compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0")
    compileOnly("javax.servlet:javax.servlet-api:4.0.1")
    compileOnly ("com.fasterxml.jackson.core:jackson-databind:2.12.5")// 使用你需要的版本
    compileOnly ("org.springframework.cloud:spring-cloud-starter-gateway")
    compileOnly("org.springframework:spring-test:5.3.25"){
        exclude(group="org.springframework",module = "spring-core")
    }
    compileOnly("com.xuxueli:xxl-job-core:2.4.0")
    compileOnly("com.alibaba:fastjson:2.0.32")
    compileOnly("com.alibaba.fastjson2:fastjson2:2.0.42")
    compileOnly("com.google.code.gson:gson:2.7")
    // https://mvnrepository.com/artifact/org.springdoc/springdoc-openapi-starter-webmvc-api




}
dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2021.0.3")
    }
}