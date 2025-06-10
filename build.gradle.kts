import com.xpdustry.ksr.kotlinRelocate
import neubs.CustomSignTask
import neubs.DownloadBackupRepo
import neubs.NEUBuildFlags
import neubs.applyPublishingInformation
import neubs.setVersionFromEnvironment
import org.apache.commons.lang3.SystemUtils
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
	idea
	java
	id("gg.essential.loom") version "0.10.0.+"
	id("dev.architectury.architectury-pack200") version "0.1.3"
	id("com.github.johnrengelman.shadow") version "7.1.2"
	id("io.github.juuxel.loom-quiltflower") version "1.7.3"
	`maven-publish`
	kotlin("jvm") version "1.9.0"
	id("com.google.devtools.ksp") version "1.9.0-1.0.13"
	id("io.gitlab.arturbosch.detekt") version "1.23.0"
	id("net.kyori.blossom") version "2.1.0"
	id("com.xpdustry.ksr") version "1.0.0"
}

apply<NEUBuildFlags>()

group = "io.github.moulberry"
val baseVersion = setVersionFromEnvironment()

loom {
	launchConfigs {
		"client" {
			property("mixin.debug", "true")
			property("asmhelper.verbose", "true")
			arg("--tweakClass", "gg.essential.loader.stage0.EssentialSetupTweaker")
			//arg("--tweakClass", "io.github.moulberry.notenoughupdates.loader.NEUDelegatingTweaker")

			arg("--mixin", "mixins.notenoughupdates.json")
			arg("--mixin", "mixins.odinclient.json")
		}
	}
	runConfigs {
		"client" {
			if (SystemUtils.IS_OS_MAC_OSX) {
				vmArgs.remove("-XstartOnFirstThread")
			}
			vmArgs.add("-Xmx4G")
		}
		"server" {
			isIdeConfigGenerated = false
		}
	}
	forge {
		accessTransformer(file("src/main/resources/META-INF/accesstransformer.cfg"))
		pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
		mixinConfig("mixins.notenoughupdates.json")
		mixinConfig("mixins.odinclient.json")
	}
	@Suppress("UnstableApiUsage")
	mixin {
		defaultRefmapName.set("mixins.notenoughupdates.refmap.json")
	}
}

repositories {
	mavenCentral()
	mavenLocal()
	maven("https://maven.notenoughupdates.org/releases")
	maven("https://repo.spongepowered.org/maven/")
	maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
	maven("https://jitpack.io")
	maven("https://repo.nea.moe/releases")
	maven("https://repo.essential.gg/repository/maven-public/")
}

val shadowImplementation: Configuration by configurations.creating {
	configurations.implementation.get().extendsFrom(this)
}
val shadowOnly: Configuration by configurations.creating {}
val shadowApi: Configuration by configurations.creating {
	configurations.api.get().extendsFrom(this)
}
val devEnv: Configuration by configurations.creating {
	configurations.runtimeClasspath.get().extendsFrom(this)
	isCanBeResolved = false
	isCanBeConsumed = false
	isVisible = false
}
val kotlinDependencies: Configuration by configurations.creating {
	configurations.implementation.get().extendsFrom(this)
}
val mixinRTDependencies: Configuration by configurations.creating {
	configurations.implementation.get().extendsFrom(this)
}
configurations {
	val main = getByName(sourceSets.main.get().compileClasspathConfigurationName)
}

dependencies {
	// Core Minecraft/Forge
	minecraft("com.mojang:minecraft:1.8.9")
	mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
	forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

	// Kotlin (using Odin"s newer version)
	implementation(enforcedPlatform("org.jetbrains.kotlin:kotlin-bom:1.9.0"))
	kotlinDependencies(kotlin("stdlib"))
	kotlinDependencies(kotlin("reflect"))
	implementation(kotlin("stdlib-jdk8"))

	// Odin/NEU shared and unique dependencies
	implementation("com.github.Stivais:Commodore:bea320fe0a")
	implementation("org.spongepowered:mixin:0.7.11-SNAPSHOT") { isTransitive = false }
	annotationProcessor("org.spongepowered:mixin:0.8.5-SNAPSHOT")
	// https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-core
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
	implementation("gg.essential:loader-launchwrapper:1.1.3")
	compileOnly("gg.essential:essential-1.8.9-forge:12132+g6e2bf4dc5")
	implementation("com.mojang:brigadier:1.2.9")
	implementation("com.google.auto.service:auto-service-annotations:1.0.1")
	ksp("dev.zacsweers.autoservice:auto-service-ksp:1.0.0")
	compileOnly(ksp(project(":annotations"))!!)
	compileOnly("org.projectlombok:lombok:1.18.24")
	annotationProcessor("org.projectlombok:lombok:1.18.24")
	// https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-core
	shadowImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
	shadowImplementation("com.mojang:brigadier:1.0.18")
	shadowImplementation("gg.essential:loader-launchwrapper:1.1.3")
	shadowImplementation("com.github.Stivais:Commodore:bea320fe0a")
	shadowImplementation("moe.nea:libautoupdate:1.3.1")
	shadowImplementation(libs.nealisp) { exclude("org.jetbrains.kotlin") }
	mixinRTDependencies("org.spongepowered:mixin:0.7.11-SNAPSHOT") { isTransitive = false }
	annotationProcessor("net.fabricmc:sponge-mixin:0.11.4+mixin.0.8.5")
	compileOnly("org.jetbrains:annotations:24.0.1")
	modImplementation(libs.moulconfig)
	shadowOnly(libs.moulconfig)
	shadowApi("info.bliki.wiki:bliki-core:3.1.0")
	testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
	testAnnotationProcessor("net.fabricmc:sponge-mixin:0.11.4+mixin.0.8.5")
	detektPlugins("org.notenoughupdates:detektrules:1.0.0")
	devEnv("me.djtheredstoner:DevAuth-forge-legacy:1.2.1")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}

java {
	withSourcesJar()
	toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

tasks.withType<JavaCompile> {
	options.encoding = "UTF-8"
	options.isFork = true
}
tasks.named<Test>("test") {
	useJUnitPlatform()
	systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
	this.javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
	testLogging {
		exceptionFormat = TestExceptionFormat.FULL
	}
}
val badJars = layout.buildDirectory.dir("badjars")

tasks.named("jar", Jar::class) {
	archiveClassifier.set("named")
	destinationDirectory.set(badJars)
}
tasks.withType<Jar> {
	archiveBaseName.set("NotEnoughUpdates")
	manifest.attributes(
		"Main-Class" to "NotSkyblockAddonsInstallerFrame",
		"TweakClass" to "gg.essential.loader.stage0.EssentialSetupTweaker",
		"MixinConfigs" to "mixins.notenoughupdates.json,mixins.odinclient.json",
		"FMLCorePluginContainsFMLMod" to true,
		"ForceLoadAsMod" to true,
		"Manifest-Version" to "1.0",
		"FMLAT" to "accesstransformer.cfg",
	)
}
val remapJar by tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
	archiveClassifier.set("")
	from(tasks.shadowJar)
	input.set(tasks.shadowJar.get().archiveFile)
	doLast {
		println("Jar name: ${archiveFile.get().asFile}")
	}
}
tasks.remapSourcesJar {
	this.enabled = false
}
val kotlinDependencyCollectionJar by tasks.creating(Zip::class) {
	archiveFileName.set("kotlin-libraries-wrapped.jar")
	destinationDirectory.set(project.layout.buildDirectory.dir("wrapperjars"))
	from(kotlinDependencies)
	into("neu-kotlin-libraries-wrapped")
}
val mixinDependencyCollectionJar by tasks.creating(Zip::class) {
	archiveFileName.set("mixin-libraries-wrapped.jar")
	destinationDirectory.set(project.layout.buildDirectory.dir("wrapperjars"))
	from(mixinRTDependencies)
	into("neu-mixin-libraries-wrapped")
}
val includeBackupRepo by tasks.registering(DownloadBackupRepo::class) {
	this.branch.set("master")
	this.outputDirectory.set(layout.buildDirectory.dir("downloadedRepo"))
}
tasks.shadowJar {
	archiveClassifier.set("dep-dev")
	configurations = listOf(shadowImplementation, shadowApi, shadowOnly)
	destinationDirectory.set(badJars)
	archiveBaseName.set("NotEnoughUpdates")
	exclude("**/module-info.class", "LICENSE.txt")
	dependencies {
		exclude {
			it.moduleGroup.startsWith("org.apache.") || it.moduleName in
				listOf("logback-classic", "commons-logging", "commons-codec", "logback-core")
		}
	}
	from(kotlinDependencyCollectionJar)
	from(mixinDependencyCollectionJar)
	dependsOn(kotlinDependencyCollectionJar)
	dependsOn(mixinDependencyCollectionJar)
	fun relocate(name: String) = kotlinRelocate(name, "io.github.moulberry.notenoughupdates.deps.$name")
	relocate("org.jetbrains.kotlinx")
	relocate("com.mojang.brigadier")
	relocate("com.github.Stivais.commodore")
	relocate("io.github.moulberry.moulconfig")
	relocate("moe.nea.libautoupdate")
	relocate("moe.nea.lisp")
	mergeServiceFiles()
}
tasks.assemble.get().dependsOn(remapJar)
tasks.processResources {
	from(tasks["generateBuildFlags"])
	from(includeBackupRepo)
	filesMatching(listOf("mcmod.info", "fabric.mod.json", "META-INF/mods.toml")) {
		expand(
			"version" to project.version, "mcversion" to "1.8.9",
		)
	}
}
val detektProjectBaseline by tasks.registering(io.gitlab.arturbosch.detekt.DetektCreateBaselineTask::class) {
	description = "Overrides current baseline."
	buildUponDefaultConfig.set(true)
	ignoreFailures.set(true)
	parallel.set(true)
	setSource(files(rootDir))
	config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
	baseline.set(file("$rootDir/config/detekt/baseline.xml"))
	include("**/*.kt")
	include("**/*.kts")
	exclude("**/resources/**")
	exclude("**/build/**")
}
idea {
	module {
		sourceDirs = sourceDirs + file("build/generated/ksp/main/kotlin")
		testSourceDirs = testSourceDirs + file("build/generated/ksp/test/kotlin")
		generatedSourceDirs =
			generatedSourceDirs + file("build/generated/ksp/main/kotlin") + file("build/generated/ksp/test/kotlin")
	}
}
sourceSets.main {
	output.setResourcesDir(file("$buildDir/classes/java/main"))
	this.blossom {
		this.javaSources {
			this.property("neuVersion", baseVersion)
		}
	}
}
tasks.register("signRelease", CustomSignTask::class)
applyPublishingInformation(
	"deobf" to tasks.jar,
	"all" to tasks.remapJar,
	"sources" to tasks["sourcesJar"],
)
