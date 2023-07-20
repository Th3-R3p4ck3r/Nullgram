@file:Suppress("UnstableApiUsage")

import com.android.build.api.variant.BuildConfigField
import com.android.build.api.variant.FilterConfiguration.FilterType.*
import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension
import java.text.SimpleDateFormat
import java.util.Date

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.github.triplet.play") version "3.8.3"
    kotlin("android")
    kotlin("plugin.serialization") version Version.kotlin
}

configurations {
    all {
        exclude(group = "com.google.firebase", module = "firebase-core")
        exclude(group = "androidx.recyclerview", module = "recyclerview")
    }
}

var serviceAccountCredentialsFile = File(rootProject.projectDir, "service_account_credentials.json")

if (serviceAccountCredentialsFile.isFile) {
    setupPlay(Version.isStable)
    play.serviceAccountCredentials.set(serviceAccountCredentialsFile)
} else if (System.getenv().containsKey("ANDROID_PUBLISHER_CREDENTIALS")) {
    setupPlay(Version.isStable)
}

fun setupPlay(stable: Boolean) {
    val targetTrace = if (stable) "production" else "beta"
    play {
        track.set(targetTrace)
        defaultToAppBundles.set(true)
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:32.2.0"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")

    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.palette:palette-ktx:1.0.0")
    implementation("androidx.exifinterface:exifinterface:1.3.6")
    implementation("androidx.dynamicanimation:dynamicanimation:1.0.0")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.interpolator:interpolator:1.0.0")
    implementation("androidx.sharetarget:sharetarget:1.2.0")

    compileOnly("org.checkerframework:checker-compat-qual:2.5.5")
    implementation("com.google.firebase:firebase-messaging:23.2.0")
    implementation("com.google.firebase:firebase-config:21.4.1")
    implementation("com.google.firebase:firebase-datatransport:18.1.8")
    implementation("com.google.firebase:firebase-appindexing:20.0.0")
    implementation("com.google.android.gms:play-services-auth:20.6.0")
    implementation("com.google.android.gms:play-services-vision:20.1.3")
    implementation("com.google.android.gms:play-services-wearable:18.0.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.gms:play-services-wallet:19.2.0")
//    implementation("com.google.android.gms:play-services-safetynet:18.0.1")
    implementation("com.googlecode.mp4parser:isoparser:1.0.6") // DO NOT UPDATE THIS DEPENDENCY
    implementation(files("libs/stripe.aar"))
    implementation("com.google.mlkit:language-id:17.0.4")
    implementation(files("libs/libgsaverification-client.aar"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.2")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.jakewharton:process-phoenix:2.1.2")
    // https://mvnrepository.com/artifact/de.psdev.licensesdialog/licensesdialog
    implementation("de.psdev.licensesdialog:licensesdialog:2.2.0")
    implementation("io.noties.markwon:core:4.6.2")
    implementation("org.lsposed.hiddenapibypass:hiddenapibypass:4.3")
    implementation("org.codeberg.qwerty287:prism4j:003cb5e380")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-common:${Version.kotlin}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${Version.kotlin}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation("org.osmdroid:osmdroid-android:6.1.16")
    implementation("com.android.billingclient:billing:6.0.1")
    implementation("com.google.guava:guava:32.0.0-jre")

    implementation("io.ktor:ktor-client-core:${Version.ktor}")
    implementation("io.ktor:ktor-client-okhttp:${Version.ktor}")
    implementation("io.ktor:ktor-client-encoding:${Version.ktor}")
    implementation("io.ktor:ktor-client-content-negotiation:${Version.ktor}")
    implementation("io.ktor:ktor-serialization-kotlinx-json:${Version.ktor}")

    implementation(project(":libs:tcp2ws"))
    implementation(project(":libs:pangu"))
}


dependencies {
    val appCenterSdkVersion = "5.0.2"
    implementation("com.microsoft.appcenter:appcenter-analytics:${appCenterSdkVersion}")
    implementation("com.microsoft.appcenter:appcenter-crashes:${appCenterSdkVersion}")
}

android {
    defaultConfig.applicationId = "top.qwq2333.nullgram"
    namespace = "org.telegram.messenger"

    sourceSets.getByName("main") {
        java.srcDir("src/main/java")
        jniLibs.srcDirs("./jni/")
    }

    externalNativeBuild {
        cmake {
            path = File(projectDir, "jni/CMakeLists.txt")
        }
    }

    lint {
        checkReleaseBuilds = true
        disable += listOf(
            "MissingTranslation", "ExtraTranslation", "BlockedPrivateApi"
        )
    }

    packaging {
        resources.excludes += "**"
    }

    kotlin {
        jvmToolchain(Version.java.toString().toInt())
    }

    var keystorePwd: String? = null
    var alias: String? = null
    var pwd: String? = null
    if (project.rootProject.file("local.properties").exists()) {
        keystorePwd = gradleLocalProperties(rootDir).getProperty("RELEASE_STORE_PASSWORD")
        alias = gradleLocalProperties(rootDir).getProperty("RELEASE_KEY_ALIAS")
        pwd = gradleLocalProperties(rootDir).getProperty("RELEASE_KEY_PASSWORD")
    }

    signingConfigs {
        create("release") {
            storeFile = File(projectDir, "config/release.keystore")
            storePassword = (keystorePwd ?: System.getenv("KEYSTORE_PASS"))
            keyAlias = (alias ?: System.getenv("ALIAS_NAME"))
            keyPassword = (pwd ?: System.getenv("ALIAS_PASS"))
            enableV3Signing = true
            enableV4Signing = true
        }
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(File(projectDir, "proguard-rules.pro"))
            optimization {
                keepRules {
                    ignoreExternalDependencies("com.microsoft.appcenter:appcenter")
                }
            }

            the<CrashlyticsExtension>().nativeSymbolUploadEnabled = true
            the<CrashlyticsExtension>().mappingFileUploadEnabled = true
        }

        getByName("debug") {
            signingConfig = signingConfigs.getByName("release")
            isDefault = true
            isDebuggable = true
            isJniDebuggable = true
        }

        create("play") {
            initWith(getByName("release"))
        }
    }

    defaultConfig {
        externalNativeBuild {
            cmake {
                version = "3.22.1"
                arguments += listOf(
                    "-DANDROID_STL=c++_static", "-DANDROID_PLATFORM=android-21", "-DCMAKE_C_COMPILER_LAUNCHER=ccache", "-DCMAKE_CXX_COMPILER_LAUNCHER=ccache", "-DNDK_CCACHE=ccache"
                )
            }
        }

        buildConfigField("String", "BUILD_TIME", "\"${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())}\"")
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }

    androidComponents {
        onVariants { variant ->
            val abiName = mapOf("armeabi-v7a" to "arm32", "arm64-v8a" to "arm64", "x86" to "x86", "x86_64" to "x86_64")
            variant.buildConfigFields.put("isPlay", BuildConfigField("boolean", variant.name == "play", null))
            variant.outputs.forEach { output ->
                val abi = output.filters.find { it.filterType == ABI }?.identifier
                variant.buildConfigFields.put(
                    "FLAVOR", BuildConfigField(
                        "String", "\"${abiName[abi]}\"",
                        "this is just a compatibility solution and we are not using flavorProduct anymore"
                    )
                )

                val task = project.tasks.register<MoveApk>("copy${variant.name}${abiName[abi]}")
                val request = variant.artifacts.use(task)
                    .wiredWithDirectories(MoveApk::apkFolder, MoveApk::outFolder)
                    .toTransformMany(com.android.build.api.artifact.SingleArtifact.APK)

                task.configure {
                    this.transformationRequest.set(request)
                    transformer.set {
                        File(projectDir, "build/outputs/apk/${variant.name}/Nullgram-${defaultConfig.versionName}-${abiName[abi]}.apk")
                    }
                }
            }

        }
    }


}

tasks.register<ReplaceIcon>("replaceIcon") {}
tasks.getByName("preBuild").dependsOn(tasks.getByName("replaceIcon"))

abstract class MoveApk : DefaultTask() {
    @get:Internal
    abstract val transformer: Property<(input: com.android.build.api.variant.BuiltArtifact) -> File>

    @get:InputDirectory
    abstract val apkFolder: DirectoryProperty

    @get:OutputDirectory
    abstract val outFolder: DirectoryProperty

    @get:Internal
    abstract val transformationRequest: Property<com.android.build.api.artifact.ArtifactTransformationRequest<MoveApk>>

    @TaskAction
    fun taskAction() = transformationRequest.get().submit(this) { builtArtifact ->
        File(builtArtifact.outputFile).copyTo(transformer.get()(builtArtifact), true)
    }
}
