package io.github.coffeecatrailway.engine.window;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window
{
	private long handle;
	private int width, height;
	
	private GLFWFramebufferSizeCallbackI framebufferCallback = null;
	
	public Window(int width, int height)
	{
		this.width = width;
		this.height = height;
	}
	
	public void init(String title, boolean vSync, int platform)
	{
		System.out.println("Creating window");
		
		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set();
		glfwInitHint(GLFW_PLATFORM, platform);
		
		// Initialize GLFW. Most GLFW functions will not work before doing this.
		System.out.println("Initializing GLFW");
		if ( !glfwInit() ) {
			throw new IllegalStateException("Unable to initialize GLFW");
		}
		
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 6);
		
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		this.handle = glfwCreateWindow(this.width, this.height, title, NULL, NULL);
		
		if (this.handle == NULL) {
			throw new RuntimeException("Failed to create the GLFW window");
		}
		
		glfwMakeContextCurrent(this.handle);
		this.setVSync(vSync);
		glfwShowWindow(this.handle);
		
		GL.createCapabilities();
		glfwSetFramebufferSizeCallback(this.handle, (window, width, height) -> {
			this.width = width;
			this.height = height;
			GL11.glViewport(0, 0, this.width, this.height);
			if (this.framebufferCallback != null)
				this.framebufferCallback.invoke(window, width, height);
		});
	}
	
	public void destroy()
	{
		System.out.println("Destroying window");
		Callbacks.glfwFreeCallbacks(this.handle);
		glfwDestroyWindow(this.handle);
		glfwTerminate();
	}
	
	public long getHandle()
	{
		return this.handle;
	}
	
	public int getWidth()
	{
		return this.width;
	}
	
	public int getHeight()
	{
		return this.height;
	}
	
	public void setVSync(boolean vSync)
	{
		glfwSwapInterval(vSync ? 1 : 0);
	}
	
	public void setFramebufferCallback(GLFWFramebufferSizeCallbackI framebufferCallback)
	{
		this.framebufferCallback = framebufferCallback;
	}
}
