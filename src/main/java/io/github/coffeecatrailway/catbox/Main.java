package io.github.coffeecatrailway.catbox;

import imgui.ImGui;
import io.github.coffeecatrailway.engine.physics.Solver;
import io.github.coffeecatrailway.engine.physics.object.LineObject;
import io.github.coffeecatrailway.engine.physics.object.VerletObject;
import io.github.coffeecatrailway.engine.physics.object.constraint.DistanceConstraint;
import io.github.coffeecatrailway.engine.physics.object.constraint.SpringConstraint;
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
		
		this.solver.gravity.set(0.f, -400.f);
		
//		VerletObject obj1 = new VerletObject(new Vector2f(100.f), 20.f);
//		this.solver.addObject(obj1);
//		obj1.setVelocity(new Vector2f(0.f , -200.f), this.solver.getStepDt());
		
//		VerletObject obj2 = new VerletObject(new Vector2f(-100.f), 20.f);
//		this.solver.addObject(obj2);
//		obj2.setVelocity(new Vector2f(0.f , 200.f), this.solver.getStepDt());
		
//		VerletObject lo1 = new VerletObject(new Vector2f(-200.f, 0.f), 10.f);
////		lo1.fixed = true;
//		this.solver.addObject(lo1);
////		lo1.setVelocity(new Vector2f(0.f, 200.f), this.solver.getStepDt());
//
//		VerletObject lo2 = new VerletObject(new Vector2f(200.f, 0.f), 10.f);
////		lo2.fixed = true;
//		this.solver.addObject(lo2);
////		lo2.setVelocity(new Vector2f(0.f, 200.f), this.solver.getStepDt());
//
//		LineObject lineObj = new LineObject(lo1, lo2, 20.f);
//		this.solver.addLineObj(lineObj);
//
//		DistanceConstraint distConstraint = new DistanceConstraint(lo1, lo2);
//		this.solver.addConstraint(distConstraint);
		
//		VerletObject chainObjLast = new VerletObject(new Vector2f(-165.f, 0.f), 10.f);
//		chainObjLast.elasticity = .75f;
//		chainObjLast.fixed = true;
//		this.solver.addObject(chainObjLast);
//		for (int i = 0; i < 10; i++)
//		{
//			VerletObject chainObj = new VerletObject(new Vector2f(-165.f + 30.f * (i + 1.f), 0.f), 10.f);
//			chainObj.elasticity = .75f;
//			this.solver.addObject(chainObj);
//
//			LineObject lineObj = new LineObject(chainObjLast, chainObj);
//			this.solver.addLineObj(lineObj);
//
//			SpringConstraint constraint = new SpringConstraint(chainObjLast, chainObj, 40.f, 2.f);
//			this.solver.addConstraint(constraint);
//
//			chainObjLast = chainObj;
//		}
//		chainObjLast.fixed = true;
		
//		this.addCube(new Vector2f(-200.f, -100.f), new Vector3f(1.f), 200.f, 100.f, 10.f, 1.f);
//		this.addCube(new Vector2f(0.f, -100.f), new Vector3f(.5f), 100.f, 100.f, 10.f, .5f);
//		this.addCube(new Vector2f(200.f, -100.f), new Vector3f(.25f), 100.f, 100.f, 10.f, .25f);
		
		this.addCube(new Vector2f(0.f, -300.f), new Vector3f(1.f), this.worldView * 2.f, 300.f, 10.f, 1.5f);

		VerletObject wo1 = new VerletObject(new Vector2f(-this.worldView - 20.f, -500.f), 10.f);
		wo1.fixed = true;
		this.solver.addObject(wo1);
		VerletObject wo2 = new VerletObject(new Vector2f(-this.worldView - 20.f, 300.f), 10.f);
		wo2.fixed = true;
		this.solver.addObject(wo2);

		LineObject wl1 = new LineObject(wo1, wo2);
		this.solver.addLineObj(wl1);

		VerletObject wo3 = new VerletObject(new Vector2f(this.worldView + 20.f, -500.f), 10.f);
		wo3.fixed = true;
		this.solver.addObject(wo3);
		VerletObject wo4 = new VerletObject(new Vector2f(this.worldView + 20.f, 300.f), 10.f);
		wo4.fixed = true;
		this.solver.addObject(wo4);

		LineObject wl2 = new LineObject(wo3, wo4);
		this.solver.addLineObj(wl2);

		LineObject wl3 = new LineObject(wo1, wo3);
		this.solver.addLineObj(wl3);
	}
	
	private void addCube(Vector2f pos, Vector3f color, float width, float height, float objRadius, float springForce)
	{
		VerletObject so1 = new VerletObject(new Vector2f(-width * .5f, -height * .5f).add(pos), objRadius);
		so1.color = color;
		this.solver.addObject(so1);
		VerletObject so2 = new VerletObject(new Vector2f(width * .5f, -height * .5f).add(pos), objRadius);
		so2.color = color;
		this.solver.addObject(so2);
		VerletObject so3 = new VerletObject(new Vector2f(width * .5f, height * .5f).add(pos), objRadius);
		so3.color = color;
		this.solver.addObject(so3);
		VerletObject so4 = new VerletObject(new Vector2f(-width * .5f, height * .5f).add(pos), objRadius);
		so4.color = color;
		this.solver.addObject(so4);
		
		LineObject slObj1 = new LineObject(so1, so2);
		this.solver.addLineObj(slObj1);
		LineObject slObj2 = new LineObject(so2, so3);
		this.solver.addLineObj(slObj2);
		LineObject slObj3 = new LineObject(so3, so4);
		this.solver.addLineObj(slObj3);
		LineObject slObj4 = new LineObject(so4, so1);
		this.solver.addLineObj(slObj4);
		
		SpringConstraint springConstraint1 = new SpringConstraint(so1, so2, springForce);
		this.solver.addConstraint(springConstraint1);
		SpringConstraint springConstraint2 = new SpringConstraint(so2, so3, springForce);
		this.solver.addConstraint(springConstraint2);
		SpringConstraint springConstraint3 = new SpringConstraint(so3, so4, springForce);
		this.solver.addConstraint(springConstraint3);
		SpringConstraint springConstraint4 = new SpringConstraint(so4, so1, springForce);
		this.solver.addConstraint(springConstraint4);
		
		SpringConstraint springConstraint5 = new SpringConstraint(so1, so3, springForce);
		this.solver.addConstraint(springConstraint5);
		SpringConstraint springConstraint6 = new SpringConstraint(so2, so4, springForce);
		this.solver.addConstraint(springConstraint6);
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

			VerletObject obj = new VerletObject(new Vector2f(0.f, this.worldView * .75f), radius);
			obj.color = this.getRainbow(this.solver.getTime());
			obj.elasticity = .5f;
			obj.setVelocity(velocity, this.solver.getStepDt());

			this.solver.addObject(obj);
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
