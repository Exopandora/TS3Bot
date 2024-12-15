plugins {
	id("java-library")
	id("idea")
}

dependencies {
	api("com.fasterxml.jackson.core:jackson-databind:2.18.2")
	api("com.mojang:brigadier:1.0.18")
	api("org.jsoup:jsoup:1.18.3")
	api("org.apache.httpcomponents:httpclient:4.5.14")
	api("org.sejda.imageio:webp-imageio:0.1.6")
	api("ch.obermuhlner:big-math:2.3.2")
	api("ch.qos.logback:logback-classic:1.5.12")
	api("org.jetbrains:annotations:26.0.1")
	
	testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
