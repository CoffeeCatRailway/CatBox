plugins {
    id("java")
}

val lwjglVersion = "3.3.6"
val jomlVersion = "1.10.8"
val lwjglNatives = "natives-linux"
val imguiVersion = "1.90.0"

group = "io.github.coffeecatrailway"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

    implementation("org.lwjgl", "lwjgl")
    implementation("org.lwjgl", "lwjgl-glfw")
    implementation("org.lwjgl", "lwjgl-openal")
    implementation("org.lwjgl", "lwjgl-opengl")
    implementation("org.lwjgl", "lwjgl-stb")
    implementation("org.lwjgl", "lwjgl", classifier = lwjglNatives)
    implementation("org.lwjgl", "lwjgl-glfw", classifier = lwjglNatives)
    implementation("org.lwjgl", "lwjgl-openal", classifier = lwjglNatives)
    implementation("org.lwjgl", "lwjgl-opengl", classifier = lwjglNatives)
    implementation("org.lwjgl", "lwjgl-stb", classifier = lwjglNatives)
    implementation("org.joml", "joml", jomlVersion)

    implementation("io.github.spair:imgui-java-binding:${imguiVersion}")
    implementation("io.github.spair:imgui-java-lwjgl3:${imguiVersion}")
    implementation("io.github.spair:imgui-java-natives-linux:${imguiVersion}")
}