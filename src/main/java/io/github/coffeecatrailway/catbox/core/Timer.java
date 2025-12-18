/*
 * The MIT License (MIT)
 *
 * Copyright © 2014-2015, Heiko Brumme
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.coffeecatrailway.catbox.core;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class Timer
{
	private double prevTime;
	private float timeCount;
	
	private int fps, fpsCount;
	private int ups, upsCount;
	
	public void init()
	{
		this.prevTime = this.getTime();
	}
	
	public double getTime()
	{
		return glfwGetTime();
	}
	
	public float getDelta()
	{
		double time = this.getTime();
		float delta = (float) (time - this.prevTime);
		this.prevTime = time;
		this.timeCount += delta;
		return delta;
	}
	
	public void updateFPS()
	{
		this.fpsCount++;
	}
	
	public void updateUPS()
	{
		this.upsCount++;
	}
	
	// Updates FPS and UPS if a whole second has passed.
	public void update()
	{
		if (this.timeCount > 1.f)
		{
			this.fps = this.fpsCount;
			this.fpsCount = 0;
			
			this.ups = this.upsCount;
			this.upsCount = 0;
			
			this.timeCount -= 1.f;
		}
	}
	
	public int getFPS()
	{
		return this.fps > 0 ? this.fps : this.fpsCount;
	}
	
	public int getUPS()
	{
		return this.ups > 0 ? this.ups : this.upsCount;
	}
}
