package io.github.coffeecatrailway.catbox.io;

import org.joml.Vector2f;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

import static org.lwjgl.glfw.GLFW.*;

/**
 * @author CoffeeCatRailway
 * Created: 19/12/2025
 */
public final class IOHandler
{
	private static boolean[] KEYS_DOWN = new boolean[GLFW_KEY_LAST];
	private static boolean[] KEYS_OLD = new boolean[GLFW_KEY_LAST];
	
	private static boolean[] MOUSE_DOWN = new boolean[GLFW_MOUSE_BUTTON_LAST];
	private static boolean[] MOUSE_OLD = new boolean[GLFW_MOUSE_BUTTON_LAST];
	
	public static final Vector2f MOUSE_POS = new Vector2f();
	
	private static class KeyHandler extends GLFWKeyCallback
	{
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
	}
	
	private static class MouseHandler extends GLFWMouseButtonCallback
	{
		@Override
		public void invoke(long window, int button, int action, int mods)
		{
			MOUSE_DOWN[button] = action != GLFW_RELEASE;
		}
	}
	
	private static class CursorPosHandler extends GLFWCursorPosCallback
	{
		
		@Override
		public void invoke(long window, double x, double y)
		{
			MOUSE_POS.set(x, y);
		}
	}
	
	public static void setCallbacks(long window)
	{
		glfwSetKeyCallback(window, new KeyHandler());
		glfwSetMouseButtonCallback(window, new MouseHandler());
		glfwSetCursorPosCallback(window, new CursorPosHandler());
	}
	
	public static void update()
	{
		KEYS_OLD = KEYS_DOWN.clone();
		MOUSE_OLD = MOUSE_DOWN.clone();
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
	
	public static boolean isMousePressed(int key)
	{
		return MOUSE_DOWN[key];
	}
	
	public static boolean isMouseJustPressed(int key)
	{
		return MOUSE_DOWN[key] && !MOUSE_OLD[key];
	}
	
	public static boolean isMouseJustReleased(int key)
	{
		return !MOUSE_DOWN[key] && MOUSE_OLD[key];
	}
}
