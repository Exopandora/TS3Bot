plugins {
	id("java")
	id("idea")
}

subprojects {
	apply(plugin = "java")
	apply(plugin = "idea")
	
	repositories {
		mavenCentral()
		maven("https://libraries.minecraft.net")
	}
	
	idea {
		module {
			isDownloadSources = true
			isDownloadJavadoc = true
		}
	}
	
	java {
		toolchain {
			languageVersion = JavaLanguageVersion.of(21)
		}
	}
}
