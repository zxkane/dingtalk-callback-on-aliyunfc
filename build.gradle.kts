import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.util.GFileUtils
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val detektVersion = "1.0.0-RC12"
val kotlinTestVersion = "3.1.11"
val junit5Version = "5.3.2"
val jacksonVersion = "2.9.7"

plugins {
    val kotlinVersion = "1.3.11"
    val detektVersion = "1.0.0-RC12"

    id("org.jetbrains.kotlin.jvm") version kotlinVersion
    id("io.gitlab.arturbosch.detekt") version detektVersion
    id("com.github.johnrengelman.shadow") version "4.0.3"
}

group = "com.github.zxkane"
version = "1.0.0-SNAPSHOT"

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform()
    testLogging {
        // set options for log level LIFECYCLE
        events = setOf(
                TestLogEvent.FAILED,
                TestLogEvent.PASSED,
                TestLogEvent.SKIPPED,
                TestLogEvent.STANDARD_OUT
        )
        exceptionFormat = TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true

    }
}

repositories {
    mavenLocal()

    maven(url = uri("http://maven.aliyun.com/nexus/content/groups/public"))
    maven(url = "http://maven.aliyun.com/mvn/repository/")
    maven(url = "http://repo.spring.io/release")
    jcenter()
    mavenCentral()
    maven(url = uri("https://dl.bintray.com/s1m0nw1/KtsRunner"))
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.aliyun.fc.runtime:fc-java-core:1.2.0")

    implementation("com.fasterxml.jackson.core:jackson-databind:${jacksonVersion}")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${jacksonVersion}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${jacksonVersion}"){
        exclude("org.jetbrains.kotlin")
    }

    implementation("commons-codec:commons-codec:1.11")
    compile(files("lib/lippi-oapi-encrpt.jar"))

    implementation("com.aliyun.openservices:tablestore:4.8.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${junit5Version}")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:${junit5Version}")
    testCompile("io.kotlintest:kotlintest-runner-junit5:${kotlinTestVersion}"){
        exclude("org.jetbrains.kotlin")
    }
    testCompile("org.mockito:mockito-core:2.23.+")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.0.0")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:${detektVersion}")
}

tasks.withType<Wrapper> {
    gradleVersion = "5.0"
    distributionType = Wrapper.DistributionType.ALL
    doLast {
        /*
         * Copy the properties file into the detekt-gradle-plugin project.
         * This allows IDEs like IntelliJ to import the detekt-gradle-plugin as a standalone project.
         */
        val gradlePluginWrapperDir = File(gradle.includedBuild("detekt-gradle-plugin").projectDir, "/gradle/wrapper")
        GFileUtils.mkdirs(gradlePluginWrapperDir)
        copy {
            from(propertiesFile)
            into(gradlePluginWrapperDir)
        }
    }
}

val userHome = System.getProperty("user.home")

detekt {
    debug = true
    toolVersion = detektVersion
    filters = ".*/resources/.*,.*/build/.*"

    reports {
        xml.enabled = true
        html.enabled = true
    }

    idea {
        path = "$userHome/.idea"
        codeStyleScheme = "$userHome/.idea/idea-code-style.xml"
        inspectionsProfile = "$userHome/.idea/inspect.xml"
        report = "project.projectDir/reports"
        mask = "*.kt"
    }
}

tasks.withType<ShadowJar> {
    baseName = "${project.name}-all"
    version = "${version}"
}

tasks.getByName("build") {
    finalizedBy("shadowJar")
}
