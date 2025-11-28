package io.github.coffeecatrailway.catbox;

import static org.lwjgl.glfw.GLFW.glfwGetTimerFrequency;
import static org.lwjgl.glfw.GLFW.glfwGetTimerValue;

public class Timer
{
	private long prevTicks;
	private float elapsedSeconds;
	
	public Timer()
	{
		this.prevTicks = 0L;
		this.elapsedSeconds = 0.f;
	}
	
	public void tick()
	{
		long currentTicks = glfwGetTimerValue();
		long delta = currentTicks - this.prevTicks;
		this.prevTicks = currentTicks;
		long ticksPerSecond = glfwGetTimerFrequency();
		this.elapsedSeconds = (float) delta / (float) ticksPerSecond;
	}
	
	public float getElapsedSeconds()
	{
		return this.elapsedSeconds;
	}
}
