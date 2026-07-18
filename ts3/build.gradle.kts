import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("idea")
    id("application")
    id("com.gradleup.shadow") version "9.6.0"
}

configurations {
    create("shadowImplementation") {
        extendsFrom(configurations["implementation"])
    }
    named("compileClasspath") {
        extendsFrom(configurations["shadowImplementation"])
    }
}

dependencies {
    implementation(project(":common"))
    implementation("com.github.theholywaffle:teamspeak3-api:1.3.1")
    // manually upgrade vulnerable transitive dependencies
    implementation("com.hierynomus:sshj:0.40.0")
    implementation("org.bouncycastle:bcpkix-jdk18on:1.84")
    implementation("org.bouncycastle:bcprov-jdk18on:1.84")
    implementation("org.bouncycastle:bcutil-jdk18on:1.84")
}

application {
    mainClass.set("net.kardexo.bot.teamspeak.Start")
}

tasks.named<JavaExec>("run") {
    workingDir(File(project.rootDir.path, "run"))
    standardInput = System.`in`
}

tasks.withType<ShadowJar> {
    configurations = listOf(project.configurations["shadowImplementation"])
}

tasks.withType<Jar> {
    archiveBaseName.set("ts3bot")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
