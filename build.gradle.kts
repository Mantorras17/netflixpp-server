plugins {
    id("java")
}

group = "org.netflixpp"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val jerseyVersion = "2.41"
val jettyVersion = "9.4.54.v20240208"
val log4jVersion = "2.22.1"
val jjwtVersion = "0.11.5"
val nettyVersion = "4.1.110.Final"
val slf4jVersion = "2.0.13"

dependencies {
    // Jersey (JAX-RS) with javax.* packages
    implementation("org.glassfish.jersey.core:jersey-server:$jerseyVersion")
    implementation("org.glassfish.jersey.containers:jersey-container-servlet:$jerseyVersion")
    implementation("org.glassfish.jersey.inject:jersey-hk2:$jerseyVersion")
    implementation("org.glassfish.jersey.media:jersey-media-json-jackson:$jerseyVersion")
    implementation("org.glassfish.jersey.media:jersey-media-multipart:$jerseyVersion")

    // Jetty 9 (javax.servlet)
    implementation("org.eclipse.jetty:jetty-server:$jettyVersion")
    implementation("org.eclipse.jetty:jetty-servlet:$jettyVersion")

    // Logging (Log4j2 + SLF4J binding)
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")

    // Netty for P2P mesh components
    implementation("io.netty:netty-all:$nettyVersion")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jjwtVersion")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}