package io.github.coffeecatrailway.catbox.io;

import org.lwjgl.glfw.Callbacks;
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
	
	public void init(String title, boolean vSync)
	{
		System.out.println("Creating window");
		
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 6);
		
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		this.handle = glfwCreateWindow(this.width, this.height, title, NULL, NULL);
		
		if (this.handle == NULL)
		{
			glfwTerminate();
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
		glfwSetKeyCallback(this.handle, new InputHandler());
	}
	
	public void render()
	{
		// Swap buffer & poll events
		glfwSwapBuffers(this.handle);
		glfwPollEvents();
	}
	
	public void destroy()
	{
		System.out.println("Destroying window");
		Callbacks.glfwFreeCallbacks(this.handle);
		glfwDestroyWindow(this.handle);
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
