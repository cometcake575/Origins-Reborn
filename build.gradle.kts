plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.14" apply false
}

group = "com.starshootercity"
version = "2.6.3"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.stleary:JSON-java:20241224")
    implementation("net.objecthunter:exp4j:0.4.8")
    implementation(project(":core"))
    implementation(project(":version"))
    implementation(project(":1.18.1", "reobf"))
    implementation(project(":1.18.2", "reobf"))
    implementation(project(":1.19", "reobf"))
    implementation(project(":1.19.1", "reobf"))
    implementation(project(":1.19.2", "reobf"))
    implementation(project(":1.19.3", "reobf"))
    implementation(project(":1.19.4", "reobf"))
    implementation(project(":1.20", "reobf"))
    implementation(project(":1.20.1", "reobf"))
    implementation(project(":1.20.2", "reobf"))
    implementation(project(":1.20.3", "reobf"))
    implementation(project(":1.20.4", "reobf"))
    implementation(project(":1.20.6", "reobf"))
    implementation(project(":1.21", "reobf"))
    implementation(project(":1.21.1", "reobf"))
    implementation(project(":1.21.3", "reobf"))
    implementation(project(":1.21.4", "reobf"))
    implementation("net.kyori:adventure-platform-bukkit:4.3.4")
    implementation("xyz.jpenilla:reflection-remapper:0.1.1")

    implementation("org.eclipse.jetty:jetty-server:11.0.7")
    implementation("org.eclipse.jetty:jetty-servlet:11.0.7")
    implementation("org.eclipse.jetty:jetty-webapp:11.0.7")
}

tasks {
    compileJava {
        options.release.set(17)
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.test {
    useJUnitPlatform()
}