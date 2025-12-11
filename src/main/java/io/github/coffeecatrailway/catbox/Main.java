package io.github.coffeecatrailway.catbox;

import imgui.ImGui;
import io.github.coffeecatrailway.engine.physics.Solver;
import io.github.coffeecatrailway.engine.physics.object.VerletObject;
import io.github.coffeecatrailway.engine.renderer.LineRenderer;
import io.github.coffeecatrailway.engine.renderer.ShapeRenderer;
import io.github.coffeecatrailway.engine.renderer.window.ImGUIWrapper;
import io.github.coffeecatrailway.engine.renderer.window.Window;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;

import java.util.Locale;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Main
{
	// System
	private Window window;
	private final ImGUIWrapper imgui = new ImGUIWrapper();
	
	private final Matrix4f transformMatrix = new Matrix4f();
	private ShapeRenderer shapeRenderer;
	private LineRenderer lineRenderer;
	
	private Solver solver;
	
	// Options
	private boolean vSync = false, pauseFixed = true, btnStepFixed = false;
	private final float worldView = 600.f;
	
	private final float[] backgroundColor = {
			// 0.f, 0.f, 0.f
			95.f / 255.f,
			68.f / 255.f,
			151.f / 255.f
	};
	
	// Timing
	private int frameCount = 0, fixedFrameCount = 0;
	private int ticksPerSecond = 60;
	private float cycleTime = 1.f / (float) ticksPerSecond;
	private final Timer sysTimer = new Timer(), updateTimer = new Timer();
	
	private void init()
	{
		System.out.println("CatBox");
		System.out.println("LWJGL: " + Version.getVersion());
		System.out.println("ImGUI: " + ImGui.getVersion());
		
		this.window = new Window(1000, 800);
		// Hacky and wrong-ish, I use wayland not x11
		final int platform = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("linux") ? GLFW_PLATFORM_X11 : GLFW_ANY_PLATFORM;
		this.window.init("CatBox", this.vSync, platform);

		GLFWFramebufferSizeCallbackI callback = (window, width, height) -> this.updateTransform((float) width / (float) height);
		this.window.setFramebufferCallback(callback);
		this.updateTransform((float) this.window.getWidth() / (float) this.window.getHeight());
		
		this.imgui.init(this.window.getHandle());
		
		this.shapeRenderer = new ShapeRenderer(10);
		this.shapeRenderer.init();
		
		this.lineRenderer = new LineRenderer(10);
		this.lineRenderer.init();
		
		this.solver = new Solver();
		this.solver.setConstraint(new Vector2f(0.f), this.worldView);
		this.solver.setSubSteps(8);
		this.solver.setTps(this.ticksPerSecond);
		
//		VerletObject obj = new VerletObject(new Vector2f(0.f), 50.f);
////		obj.fixed = true;
//		this.solver.addObject(obj);
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
	
	private void update(float dt)
	{
	
	}
	
	private Vector3f getRainbow(float t)
	{
		final float r = Math.sin(t) * .5f + .5f;
		final float g = Math.sin(t + .33f * 2.f * Math.PI_f) * .5f + .5f;
		final float b = Math.sin(t + .66f * 2.f * Math.PI_f) * .5f + .5f;
		return new Vector3f(r, g, b);
	}
	
	private void fixedUpdate(float dt)
	{
		if (this.solver.getObjectCount() < 1000 && (this.fixedFrameCount % 5) == 0)
		{
			final float radius = RandUtil.getRange(2.f, 15.f);
			Vector2f velocity = new Vector2f(500.f * Math.sin(this.solver.getTime()), -400.f);

			VerletObject verletObject = new VerletObject(new Vector2f(0.f, this.worldView * .75f), radius);
			verletObject.color = this.getRainbow(this.solver.getTime());
//			verletObject.fixed = this.solver.getObjectCount() <= 2;

			this.solver.addObject(verletObject);
			this.solver.setObjectVelocity(verletObject, velocity);
		}
		
		this.solver.update(dt);
	}
	
	private void render()
	{
		this.solver.render(this.shapeRenderer, this.lineRenderer);
		
		this.shapeRenderer.drawFlush(this.transformMatrix);
		this.lineRenderer.drawFlush(this.transformMatrix);
		this.imgui.render();
	}
	
	private void gui()
	{
		float windowWidth;
		if (ImGui.begin("Info"))
		{
			windowWidth = ImGui.getWindowWidth();
			ImGui.text(String.format("FPS: %f\nFrames: %d\tFixed Frames: %d", ImGui.getIO().getFramerate(), this.frameCount, this.fixedFrameCount));
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
			
			ImGui.pushItemWidth(windowWidth * .5f);
			int[] vi = {this.ticksPerSecond};
			if (ImGui.dragInt("Ticks Per Second", vi, 10, 10, 100, "%d"))
			{
				this.ticksPerSecond = vi[0];
				this.cycleTime = 1.f / (float) this.ticksPerSecond;
				this.solver.setTps(this.ticksPerSecond);
			}
			ImGui.text(String.format("Cycle time (1/tps): %fs", this.cycleTime));
			ImGui.popItemWidth();
			ImGui.separator();
			
			ImGui.colorEdit3("Clear Color", this.backgroundColor);
		}
		ImGui.end();
		
		if (ImGui.begin("Simulation"))
		{
			windowWidth = ImGui.getWindowWidth();
			this.solver.gui(windowWidth);
		}
		ImGui.end();
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
			this.update(this.sysTimer.getElapsedSeconds());
			
			if (accumulatedSeconds > this.cycleTime)
			{
				accumulatedSeconds -= this.cycleTime;
				this.updateTimer.tick();
				if (!this.pauseFixed || this.btnStepFixed)
				{
					this.fixedUpdate(this.updateTimer.getElapsedSeconds());
					this.fixedFrameCount++;
					this.btnStepFixed = false;
				}
			}
			
			this.imgui.update();
			this.gui();
			
			// render
			glClearColor(this.backgroundColor[0], this.backgroundColor[1], this.backgroundColor[2], 1.f);
			glClear(GL_COLOR_BUFFER_BIT);
			
			this.render();
			this.frameCount++;
			
			// Swap buffer & poll events
			glfwSwapBuffers(this.window.getHandle());
			glfwPollEvents();
		}
	}
	
	private void destroy()
	{
		System.out.println("Cleaning up");
		this.solver.destroy();
		this.lineRenderer.destroy();
		this.shapeRenderer.destroy();
		this.imgui.destroy();
		this.window.destroy();
	}
	
	public static void main(String[] args)
	{
		Main main = new Main();
		main.init();
		main.run();
		main.destroy();
	}
}
