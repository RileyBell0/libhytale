plugins {
    id("java")
    id("com.gradleup.shadow") version "9.4.0"
    id("eclipse")
}

group = "dev.twunk"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(files("libs/HytaleServer.jar"))
    compileOnly("org.eclipse.jdt:org.eclipse.jdt.annotation:2.4.100")

    // Source: https://mvnrepository.com/artifact/net.bytebuddy/byte-buddy
    implementation("net.bytebuddy:byte-buddy:1.18.7")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveClassifier.set("") // replace normal jar
}

tasks.build {
    dependsOn(tasks.shadowJar)
}