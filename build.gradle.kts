plugins {
	id("java")
	id("application")
	id("idea")
	id("org.jetbrains.kotlin.jvm") version "2.1.0"
}

idea {
	module {
		isDownloadSources = true
		isDownloadJavadoc = true
	}
}

repositories {
	mavenCentral()
	maven("https://libraries.minecraft.net")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

dependencies {
	implementation("com.github.theholywaffle:teamspeak3-api:1.3.1")
	implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
	implementation("org.apache.logging.log4j:log4j-core:2.24.2")
	implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.24.2")
	implementation("com.mojang:brigadier:1.0.18")
	implementation("org.jsoup:jsoup:1.18.3")
	implementation("org.apache.httpcomponents:httpclient:4.5.14")
	implementation("org.sejda.imageio:webp-imageio:0.1.6")
	implementation("ch.obermuhlner:big-math:2.3.2")
	
	testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Jar>("jar") {
	manifest {
		attributes["Main-Class"] = "net.kardexo.ts3bot.Start"
	}
	
	from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
	
	duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
