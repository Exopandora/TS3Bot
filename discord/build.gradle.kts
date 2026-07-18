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
	implementation("com.discord4j:discord4j-core:3.3.2")
	// upgrade vulnerable transitive dependencies
	implementation(platform("io.netty:netty-bom:4.2.16.Final"))
	implementation("io.projectreactor.netty:reactor-netty-core:1.3.6")
	implementation("io.projectreactor.netty:reactor-netty-http:1.3.6")
	implementation("com.google.protobuf:protobuf-java:4.28.2")
	// dependencies from bom
	implementation("io.netty:netty-codec-http")
	implementation("io.netty:netty-common")
	implementation("io.netty:netty-handler")
}

application {
	mainClass.set("net.kardexo.bot.discord.Start")
}

tasks.named<JavaExec>("run") {
	workingDir(File(project.rootDir.path, "run"))
	standardInput = System.`in`
}

tasks.withType<ShadowJar> {
	configurations = listOf(project.configurations["shadowImplementation"])
}

tasks.withType<Jar> {
	archiveBaseName.set("discord-bot")
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
