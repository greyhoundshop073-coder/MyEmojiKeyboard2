plugins {
    id("com.android.application")
}

android {
    namespace = "com.greyhoundshop073.myemojikeyboard"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.greyhoundshop073.myemojikeyboard"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}

kotlin {
    jvmToolchain(17)
}
