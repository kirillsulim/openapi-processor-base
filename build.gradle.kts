plugins {
    java
    alias(libs.plugins.jacoco)
    alias(libs.plugins.nexus)
}

group = "io.openapiprocessor"
version = libs.versions.processor.get()
println("version: $version")

repositories {
    mavenCentral()
}

// do not create jar for the root project
tasks.named("jar") { enabled = false }


tasks.named("jacocoLogAggregatedCoverage") {
    dependsOn ("check")
}

tasks.named("build") {
    dependsOn ("jacocoLogAggregatedCoverage")
}

extra["publishUser"] = buildProperty("PUBLISH_USER")
extra["publishKey"] = buildProperty("PUBLISH_KEY")
val publishUser: String by extra
val publishKey: String by extra

nexusPublishing {
    this.repositories {
        sonatype {
            username.set(publishUser)
            password.set(publishKey)
        }
    }
}

//fun environment(key: String): Provider<String> = providers.environmentVariable(key)

var key: String? = System.getenv("SIGN_KEY")
if (key == null)
    key = "no value"

println("#1# (${key?.substring(0, 200)})")

var key2: String = buildProperty("SIGN_KEY")
println("#2# (${key2.substring(0, 200)})")


//val key2 = environment("SIGN_KEY").get()
//println("### (${key2.substring(0, 200)})")

//var SIGN_KEY_ORG: String? by project
//if (SIGN_KEY_ORG == null)
//    SIGN_KEY_ORG = "no value"
//println("#3# (${SIGN_KEY_ORG?.substring(0, 200)})")

