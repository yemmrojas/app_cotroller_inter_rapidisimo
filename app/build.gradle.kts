plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
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
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
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
    
    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    
    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization.converter)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    
    // Kotlinx Serialization
    implementation(libs.kotlinx.serialization.json)
    
    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    
    // Navigation
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.compose)
    
    // Lifecycle
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    
    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.property)
    testImplementation(libs.mockk)
    
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
        val reportFile = file("${project.buildDir}/reports/jacoco/testDebugUnitTestCoverage/testDebugUnitTestCoverage.xml")
        if (!reportFile.exists()) {
            throw GradleException("Coverage report not found at: ${reportFile.absolutePath}. Please run tests first.")
        }
        
        val report = reportFile.readText()
        
        // Parse XML to find the report-level counter (not nested method/class counters)
        // The report-level counter is the last INSTRUCTION counter before </report>
        val reportCounterRegex = """<report[^>]*>.*<counter type="INSTRUCTION"[^>]*missed="(\d+)"[^>]*covered="(\d+)"[^>]*/>.*</report>""".toRegex(RegexOption.DOT_MATCHES_ALL)
        val allCountersRegex = """<counter type="INSTRUCTION"[^>]*missed="(\d+)"[^>]*covered="(\d+)"[^>]*/?>""".toRegex()
        
        // Find all INSTRUCTION counters
        val allMatches = allCountersRegex.findAll(report).toList()
        
        if (allMatches.isEmpty()) {
            throw GradleException("Could not find any INSTRUCTION counters in coverage report")
        }
        
        // The last INSTRUCTION counter in the XML is the report-level aggregate
        val match = allMatches.last()
        
        val missed = match.groupValues[1].toInt()
        val covered = match.groupValues[2].toInt()
        val total = missed + covered
        val coverage = if (total > 0) (covered.toDouble() / total.toDouble() * 100) else 0.0
        
        println("=".repeat(60))
        println("Code Coverage Report")
        println("=".repeat(60))
        println("Instructions covered: $covered")
        println("Instructions missed: $missed")
        println("Total instructions: $total")
        println("Coverage: %.2f%%".format(coverage))
        println("Minimum required: 80.00%%")
        println("=".repeat(60))
        
        if (coverage < 80.0) {
            throw GradleException(
                "Code coverage is %.2f%%, which is below the minimum required 80%%".format(coverage)
            )
        } else {
            println("✓ Coverage check PASSED")
        }
    }
}
