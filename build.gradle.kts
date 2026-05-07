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

tasks {
    val enablePreview = "--enable-preview"

    // In our project we have the tasks compileJava and
    // compileTestJava that need to have the
    // --enable-preview compiler arguments.
    withType<JavaCompile> {
        options.compilerArgs.add(enablePreview)
        // Optionally we can show which preview feature we use.
        // options.compilerArgs.add("-Xlint:preview")

        // Explicitly setting compiler option --release
        // is needed when we wouldn't set the
        // sourceCompatibility and targetCompatibility
        // properties of the Java plugin extension.
        options.release.set(26)
    }
    // Test tasks need to have the JVM argument --enable-preview.
    withType<Test> {
        useJUnitPlatform()
        jvmArgs.add(enablePreview)
    }
    // JavaExec tasks need to have the JVM argument --enable-preview.
    withType<JavaExec> {
        jvmArgs.add(enablePreview)
    }

    // test {
    //     useJUnitPlatform()
    // }

    shadowJar {
        archiveClassifier.set("") // replace normal jar
    }

    build {
        dependsOn(shadowJar)
    }
}