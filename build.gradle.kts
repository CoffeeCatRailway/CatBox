import org.apache.tools.ant.taskdefs.condition.Os

plugins {
    id("java")
    id("application")
}

val isWindows = Os.isFamily(Os.FAMILY_WINDOWS)

val lwjglVersion = "3.3.6"
val lwjglNatives = if (isWindows) "natives-windows" else "natives-linux"

val jomlVersion = "1.10.8"

//val imguiVersion = "1.90.0"
//val imguiNatives = if (isWindows) "natives-windows" else "natives-linux"

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

//    implementation("io.github.spair:imgui-java-binding:${imguiVersion}")
//    implementation("io.github.spair:imgui-java-lwjgl3:${imguiVersion}")
//    implementation("io.github.spair:imgui-java-${imguiNatives}:${imguiVersion}")
    implementation(fileTree("libs/jars"))
}

application {
    mainClass.set("io.github.coffeecatrailway.catbox.Main")
    applicationDefaultJvmArgs.plus("-Djava.library.path=libs/natives")
}