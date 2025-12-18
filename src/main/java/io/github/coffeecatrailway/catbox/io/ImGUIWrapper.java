package io.github.coffeecatrailway.catbox.io;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

import static org.lwjgl.glfw.GLFW.glfwGetCurrentContext;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;

public class ImGUIWrapper
{
	private final ImGuiImplGlfw imguiGLFW = new ImGuiImplGlfw();
	private final ImGuiImplGl3 imguiGL3 = new ImGuiImplGl3();
	
	public void init(long windowHandle)
	{
		System.out.println("Initializing ImGUI");
		ImGui.createContext();
		ImGuiIO io = ImGui.getIO();
		io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);
		
		this.imguiGLFW.init(windowHandle, true);
		this.imguiGL3.init("#version 330");
	}
	
	public void update()
	{
		this.imguiGL3.newFrame();
		this.imguiGLFW.newFrame();
		ImGui.newFrame();
	}
	
	public void render()
	{
		ImGui.render();
		this.imguiGL3.renderDrawData(ImGui.getDrawData());
		
		if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
			final long backupWindowPtr = glfwGetCurrentContext();
			ImGui.updatePlatformWindows();
			ImGui.renderPlatformWindowsDefault();
			glfwMakeContextCurrent(backupWindowPtr);
		}
	}
	
	public void destroy()
	{
		System.out.println("Shutting down and destroying ImGUI");
		this.imguiGL3.shutdown();
		this.imguiGLFW.shutdown();
		ImGui.destroyContext();
	}
}
