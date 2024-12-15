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
    implementation("com.github.theholywaffle:teamspeak3-api:1.3.1") {
        // manually upgrade vulnerable transitive dependencies
        exclude(group = "org.bouncycastle", module = "bcprov-jdk15on")
        exclude(group = "org.bouncycastle", module = "bcpkix-jdk15on")
        exclude(group = "org.bouncycastle", module = "bcutil-jdk15on")
    }
    implementation("org.bouncycastle:bcprov-jdk18on:1.79")
    implementation("org.bouncycastle:bcpkix-jdk18on:1.79")
    implementation("org.bouncycastle:bcutil-jdk18on:1.79")
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
    archiveBaseName.set("ts3bot")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
