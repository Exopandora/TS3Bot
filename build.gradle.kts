plugins {
	id("java")
	id("idea")
}

subprojects {
	apply(plugin = "java")
	apply(plugin = "idea")
	
	repositories {
		mavenCentral()
		exclusiveContent {
			forRepository {
				maven("https://libraries.minecraft.net")
			}
			filter {
				includeGroup("com.mojang")
			}
		}
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
