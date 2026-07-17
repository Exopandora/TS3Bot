plugins {
	id("java-library")
	id("idea")
	id("java-test-fixtures")
}

dependencies {
	api(platform("com.fasterxml.jackson:jackson-bom:2.22.1"))
	api("ch.obermuhlner:big-math:2.3.2")
	api("ch.qos.logback:logback-classic:1.5.38")
	api("com.mojang:brigadier:1.0.18")
	api("org.apache.httpcomponents:httpclient:4.5.14")
	api("org.jetbrains:annotations:26.0.1")
	api("org.jsoup:jsoup:1.18.3")
	api("org.sejda.imageio:webp-imageio:0.1.6")
	// manually upgrade vulnerable transitive dependencies
	api("commons-codec:commons-codec:1.22.0")
	// dependencies from bom
	api("com.fasterxml.jackson.core:jackson-databind")
	
	testImplementation("org.junit.jupiter:junit-jupiter:6.1.1")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testRuntimeOnly(testFixtures(project(":common")))
}

tasks.named<Test>("test") {
	useJUnitPlatform()
}
