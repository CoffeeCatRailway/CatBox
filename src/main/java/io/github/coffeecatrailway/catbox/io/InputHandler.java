package io.github.coffeecatrailway.catbox.io;

import org.lwjgl.glfw.GLFWKeyCallback;

import static org.lwjgl.glfw.GLFW.*;

/**
 * @author CoffeeCatRailway
 * Created: 19/12/2025
 */
public final class InputHandler extends GLFWKeyCallback
{
	private static boolean[] KEYS_DOWN = new boolean[GLFW_KEY_LAST];
	private static boolean[] KEYS_OLD = new boolean[GLFW_KEY_LAST];
	
	@Override
	public void invoke(long window, int key, int scancode, int action, int mods)
	{
		if (key == GLFW_KEY_UNKNOWN)
			return;
		
		if (action == GLFW_PRESS)
			if (key == GLFW_KEY_ESCAPE)
				glfwSetWindowShouldClose(window, true);
		
		KEYS_DOWN[key] = action != GLFW_RELEASE;
	}
	
	public static void update()
	{
		KEYS_OLD = KEYS_DOWN.clone();
	}
	
	public static boolean isKeyPressed(int key)
	{
		return KEYS_DOWN[key];
	}
	
	public static boolean isKeyJustPressed(int key)
	{
		return KEYS_DOWN[key] && !KEYS_OLD[key];
	}
	
	public static boolean isKeyJustReleased(int key)
	{
		return !KEYS_DOWN[key] && KEYS_OLD[key];
	}
}
