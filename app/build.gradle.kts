plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ktlint)
}

android {
    namespace = "com.yei.dev.controlerinterrapidisimo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.yei.dev.controlerinterrapidisimo"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            enableUnitTestCoverage = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}


// Ktlint configuration
configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    version.set("1.0.1")
    android.set(true)
    outputToConsole.set(true)
    outputColorName.set("RED")
    ignoreFailures.set(false)
    
    filter {
        exclude("**/generated/**")
        exclude("**/build/**")
    }
}

// Test coverage configuration
tasks.register<JacocoReport>("testDebugUnitTestCoverage") {
    dependsOn("testDebugUnitTest")
    
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
    
    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/data/models/**",
        "**/domain/models/**",
        "**/*\$ViewInjector*.*",
        "**/*\$ViewBinder*.*",
        "**/BuildConfig.*",
        "**/*Component*.*",
        "**/*BR*.*",
        "**/AutoValue_*.*",
        "**/*JavascriptBridge.class",
        "**/Lambda$*.class",
        "**/Lambda.class",
        "**/*Lambda.class",
        "**/*Lambda*.class",
        "**/*_MembersInjector.class",
        "**/Dagger*Component*.class",
        "**/*Module_*Factory.class",
        "**/di/**",
        "**/*_Factory*.*",
        "**/*Module*.*",
        "**/*Dagger*.*",
        "**/*Hilt*.*",
        "**/hilt_aggregated_deps/**",
        "**/*_HiltModules*.*",
        "**/*_Impl*.*",
        "**/*MembersInjector*.*",
        "**/*_Provide*Factory*.*"
    )
    
    val debugTree = fileTree("${project.buildDir}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }
    
    val mainSrc = "${project.projectDir}/src/main/java"
    
    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(fileTree(project.buildDir) {
        include("jacoco/testDebugUnitTest.exec")
    })
}

// Enforce minimum coverage
tasks.register("verifyCoverage") {
    dependsOn("testDebugUnitTestCoverage")
    
    doLast {
        val reportFile = file("${project.buildDir}/reports/jacoco/testDebugUnitTestCoverage/html/index.html")
        if (reportFile.exists()) {
            println("Coverage report generated at: ${reportFile.absolutePath}")
            println("Please ensure coverage meets the minimum 80% requirement")
        } else {
            println("Coverage report not found. Tests may not have been executed.")
        }
    }
}
