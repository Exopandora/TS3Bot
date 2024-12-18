import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("idea")
    id("application")
    id("com.gradleup.shadow") version "8.3.5"
}

val shadowImplementation: Configuration by configurations.creating

configurations["shadowImplementation"].extendsFrom(configurations["implementation"])
configurations["compileClasspath"].extendsFrom(shadowImplementation)

dependencies {
    implementation(project(":common"))
    implementation("com.discord4j:discord4j-core:3.2.7")
    // upgrade vulnerable transitive dependencies
    implementation("io.projectreactor.netty:reactor-netty-core:1.1.13")
    implementation("io.projectreactor.netty:reactor-netty-http:1.1.13")
    implementation("io.netty:netty-codec-http:4.1.116.Final")
    implementation("io.netty:netty-common:4.1.116.Final")
    implementation("io.netty:netty-handler:4.1.116.Final")
    implementation("com.google.protobuf:protobuf-java:4.28.2")
}

application {
    mainClass.set("net.kardexo.bot.Start")
}

tasks.named<JavaExec>("run") {
    workingDir(File(project.rootDir.path, "run"))
    standardInput = System.`in`
}

tasks.withType<ShadowJar> {
    configurations = listOf(shadowImplementation)
}

tasks.withType<Jar> {
    archiveBaseName.set("discordbot")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
