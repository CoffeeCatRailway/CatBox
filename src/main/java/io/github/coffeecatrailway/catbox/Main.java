package io.github.coffeecatrailway.catbox;

import imgui.ImGui;
import io.github.coffeecatrailway.catbox.boxes.CatBoxI;
import io.github.coffeecatrailway.catbox.boxes.ForceCatBox;
import io.github.coffeecatrailway.catbox.boxes.ShapeCatBox;
import io.github.coffeecatrailway.engine.renderer.LineRenderer;
import io.github.coffeecatrailway.engine.renderer.ShapeRenderer;
import io.github.coffeecatrailway.engine.renderer.window.ImGUIWrapper;
import io.github.coffeecatrailway.engine.renderer.window.Window;
import org.joml.Math;
import org.joml.Matrix4f;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Main
{
	/*
	 * Check List:
	 * [O] Window/LWJGL
	 * [O] ImGUI
	 * [O] Renderer, queue system for basic shapes (Circle, Box, Line)
	 * [O] Debug line renderer
	 * [X] Physics engine:
	 *    [X] Basic forces
	 *    [X] Simple collision detection/response (Circle, Box, Line)
	 *    [X] Constraints (Springs/Soft, Hard)
	 *    [X] Advanced-Line (polygon) collision detection/response
	 *    [X] Rigid body
	 */
	
	// System
	private Window window;
	private final ImGUIWrapper imgui = new ImGUIWrapper();
	private final Matrix4f transformMatrix = new Matrix4f();
	private ShapeRenderer shapeRenderer;
	private LineRenderer lineRenderer;
	
	private CatBoxI catBox;
	
	// Options
	private boolean vSync = true, pauseFixed = true, btnStepFixed = false;
	private final float worldView = 100.f;
	
	private final float[] backgroundColor = {
			// 0.f, 0.f, 0.f
			95.f / 255.f,
			68.f / 255.f,
			151.f / 255.f
	};
	
	// Timing
	private int frameCount = 0, stepCount = 0;
	private int ticksPerSecond = 60;
	private float cycleTime = 1.f / (float) ticksPerSecond;
	private final Timer sysTimer = new Timer(), updateTimer = new Timer();
	
	private void init()
	{
		System.out.println("CatBox");
		System.out.println("LWJGL: " + Version.getVersion());
		System.out.println("ImGUI: " + ImGui.getVersion());
		
		this.window = new Window(Math.roundHalfDown(1600.f * .8f), Math.roundHalfDown(900.f * .8f));
		this.window.init("CatBox", true, GLFW_PLATFORM_X11); // Wrong, we're on wayland but anyway...
		GLFWFramebufferSizeCallbackI callback = (window, width, height) -> this.updateTransform((float) width / (float) height);
		this.window.setFramebufferCallback(callback);
		this.updateTransform((float) this.window.getWidth() / (float) this.window.getHeight());
		
		this.imgui.init(this.window.getHandle());
		
		this.shapeRenderer = new ShapeRenderer(10);
		this.shapeRenderer.init();
		
		this.lineRenderer = new LineRenderer(10);
		this.lineRenderer.init();
		
//		this.catBox = new ShapeCatBox();
		this.catBox = new ForceCatBox();
		this.catBox.init(this.worldView);
	}
	
	private void updateTransform(float aspect)
	{
		boolean aspectOne = aspect >= 1.f;
		float left = -this.worldView * (aspectOne ? aspect : 1.f);
		float right = this.worldView * (aspectOne ? aspect : 1.f);
		float bottom = -this.worldView / (aspectOne ? 1.f : aspect);
		float top = this.worldView / (aspectOne ? 1.f : aspect);
		this.transformMatrix.setOrtho(left, right, bottom, top, -1.f, 1.f);
	}
	
	private void run()
	{
		float accumulatedSeconds = 0.f;
		this.sysTimer.tick();
		this.updateTimer.tick();
		
		System.out.println("Starting main loop");
		while (!glfwWindowShouldClose(this.window.getHandle()))
		{
			
			// Get amount of time passed for one cycle
			this.sysTimer.tick();
			accumulatedSeconds += this.sysTimer.getElapsedSeconds();
			
			// update
			this.catBox.update(this.sysTimer.getElapsedSeconds());
			
			if (accumulatedSeconds > this.cycleTime)
			{
				accumulatedSeconds -= this.cycleTime;
				this.updateTimer.tick();
				if (!this.pauseFixed || this.btnStepFixed)
				{
					this.catBox.fixedUpdate(this.updateTimer.getElapsedSeconds());
					this.stepCount++;
					this.btnStepFixed = false;
				}
			}
			
			this.imgui.update();
			this.gui();
			
			// render
			glClearColor(this.backgroundColor[0], this.backgroundColor[1], this.backgroundColor[2], 1.f);
			glClear(GL_COLOR_BUFFER_BIT);
			
			this.catBox.render(this.shapeRenderer, this.lineRenderer);
			
			this.shapeRenderer.drawFlush(this.transformMatrix);
			this.lineRenderer.drawFlush(this.transformMatrix);
			this.imgui.render();
			
			this.frameCount++;
			
			// Swap buffer & poll events
			glfwSwapBuffers(this.window.getHandle());
			glfwPollEvents();
		}
	}
	
	private void gui()
	{
		float halfWidth;
		if (ImGui.begin("Info"))
		{
			halfWidth = ImGui.getWindowWidth() * .5f;
			ImGui.text(String.format("FPS: %f\nFrames: %d\tSteps Fixed: %d", ImGui.getIO().getFramerate(), this.frameCount, this.stepCount));
			ImGui.text(String.format("World view: %.1f", this.worldView));
			ImGui.text(String.format("Window size: %d/%d", this.window.getWidth(), this.window.getHeight()));
			if (ImGui.checkbox("Vsync", this.vSync))
			{
				this.vSync = !this.vSync;
				this.window.setVSync(this.vSync);
			}
			if (ImGui.checkbox("Pause Fixed", this.pauseFixed))
				this.pauseFixed = !this.pauseFixed;
			if (this.pauseFixed)
			{
				ImGui.sameLine(0.f, 10.f);
				if (ImGui.smallButton("Step"))
					this.btnStepFixed = true;
			}
			ImGui.separator();
			
			ImGui.text(String.format("Delta time: %fs", this.sysTimer.getElapsedSeconds()));
			ImGui.text(String.format("Fixed delta time: %fs", this.updateTimer.getElapsedSeconds()));
			
			ImGui.pushItemWidth(halfWidth);
			int[] vi = {this.ticksPerSecond};
			if (ImGui.dragInt("Ticks Per Second", vi, 10, 10, 100, "%d"))
			{
				this.ticksPerSecond = vi[0];
				this.cycleTime = 1.f / (float) this.ticksPerSecond;
			}
			ImGui.text(String.format("Cycle time (1/tps): %fs", this.cycleTime));
			ImGui.popItemWidth();
			ImGui.separator();
			
			ImGui.colorEdit3("Clear Color", this.backgroundColor);
		}
		ImGui.end();
		
		if (ImGui.begin("Simulation"))
		{
			halfWidth = ImGui.getWindowWidth() * .5f;
			this.catBox.gui(halfWidth);
		}
		ImGui.end();
	}
	
	private void destroy()
	{
		System.out.println("Cleaning up");
		this.catBox.destroy();
		this.lineRenderer.destroy();
		this.shapeRenderer.destroy();
		this.imgui.destroy();
		this.window.destroy();
	}
	
	static void main()
	{
		Main main = new Main();
		main.init();
		main.run();
		main.destroy();
	}
}
