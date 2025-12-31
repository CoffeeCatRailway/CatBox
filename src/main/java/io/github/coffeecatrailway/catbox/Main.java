package io.github.coffeecatrailway.catbox;

import imgui.ImGui;
import io.github.coffeecatrailway.catbox.core.Timer;
import io.github.coffeecatrailway.catbox.engine.RandUtil;
import io.github.coffeecatrailway.catbox.engine.solver.SolverSimple;
import io.github.coffeecatrailway.catbox.engine.object.LineObject;
import io.github.coffeecatrailway.catbox.engine.object.VerletObject;
import io.github.coffeecatrailway.catbox.engine.object.constraint.SpringConstraint;
import io.github.coffeecatrailway.catbox.graphics.LineRenderer;
import io.github.coffeecatrailway.catbox.graphics.ShapeRenderer;
import io.github.coffeecatrailway.catbox.io.IOHandler;
import io.github.coffeecatrailway.catbox.io.ImGUIWrapper;
import io.github.coffeecatrailway.catbox.io.Window;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;

import java.util.Locale;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Main
{
	// System
	private Window window;
	private final ImGUIWrapper imgui = new ImGUIWrapper();
	
	private GLFWErrorCallback errorCallback;
	
	private final Matrix4f transformMatrix = new Matrix4f();
	private ShapeRenderer shapeRenderer;
	private LineRenderer lineRenderer;
	
	private SolverSimple solver;
	
	// Options
	private boolean vSync = false;
	private final Vector2f worldSize = new Vector2f(1000.f, 1000.f);
	
	private final float[] backgroundColor = {
			// 0.f, 0.f, 0.f
			68.f / 255.f,
			151.f / 255.f,
			68.f / 255.f
	};
	
	// Timing
	private int totalFrames = 0, targetUps = 60;
	private float updateInterval = 1.f / (float) targetUps;
	private final Timer timer = new Timer();
	
	private void init()
	{
		System.out.println("CatBox");
		System.out.println("LWJGL: " + Version.getVersion());
		System.out.println("ImGUI: " + ImGui.getVersion());
		
		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		this.errorCallback = GLFWErrorCallback.createPrint(System.err).set();
		
		// Hacky and wrong-ish, I use wayland not x11
		final int platform = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("linux") ? GLFW_PLATFORM_X11 : GLFW_ANY_PLATFORM;
		glfwInitHint(GLFW_PLATFORM, platform);
		
		// Initialize GLFW. Most GLFW functions will not work before doing this.
		System.out.println("Initializing GLFW");
		if (!glfwInit())
			throw new IllegalStateException("Unable to initialize GLFW");
		
		this.window = new Window(1280, 720);
		this.window.init("CatBox", this.vSync);
		
		this.window.setFramebufferCallback((window, width, height) -> this.updateTransform((float) width, (float) height));
		this.updateTransform((float) this.window.getWidth(), (float) this.window.getHeight());
		
		this.timer.init();
		
		this.imgui.init(this.window.getHandle());
		
		this.shapeRenderer = new ShapeRenderer(100);
		this.shapeRenderer.init();
		
		this.lineRenderer = new LineRenderer(100);
		this.lineRenderer.init();
//		this.lineRenderer.enabled = false;
		
		this.solver = new SolverSimple(this.worldSize.x, this.worldSize.y, 8);
//		this.solver = new SolverSweepPrune(this.worldSize.x, this.worldSize.y, 8);
//		this.solver.setSubSteps(8);
		this.solver.setTps(this.targetUps);
		
		this.solver.gravity.set(0.f, -400.f);
		
		VerletObject o1 = new VerletObject(new Vector2f(-100.f, 0.f), 20.f);
//		o1.elasticity = .5f;
		o1.setVelocity(new Vector2f(200.f, 0.f), this.solver.getStepDt());
		this.solver.addObject(o1);
		VerletObject o2 = new VerletObject(new Vector2f(100.f, 0.f), 20.f);
//		o2.elasticity = .5f;
		this.solver.addObject(o2);
		
		// Line & Distance constraint test
//		VerletObject obj1 = new VerletObject(new Vector2f(100.f), 20.f);
//		this.solver.addObject(obj1);
//		obj1.setVelocity(new Vector2f(0.f , -200.f), this.solver.getStepDt());
//
//		VerletObject obj2 = new VerletObject(new Vector2f(-100.f), 20.f);
//		this.solver.addObject(obj2);
//		obj2.setVelocity(new Vector2f(0.f , 200.f), this.solver.getStepDt());
//
//		VerletObject lo1 = new VerletObject(new Vector2f(-200.f, 0.f), 10.f);
//		this.solver.addObject(lo1);
//
//		VerletObject lo2 = new VerletObject(new Vector2f(200.f, 0.f), 10.f);
//		this.solver.addObject(lo2);
//
//		LineObject lineObj = new LineObject(lo1, lo2, 20.f);
//		this.solver.addLineObj(lineObj);
//
//		DistanceConstraint distConstraint = new DistanceConstraint(lo1, lo2);
//		this.solver.addConstraint(distConstraint);

		// Rope Test
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

		// Cube spring test
//		this.addCube(new Vector2f(-200.f, -55.f), new Vector3f(1.f), 50.f, 50.f, 5.f, 1.f);
//		this.addCube(new Vector2f(0.f, -55.f), new Vector3f(.5f), 50.f, 50.f, 5.f, .5f);
//		this.addCube(new Vector2f(200.f, -55.f), new Vector3f(.25f), 50.f, 50.f, 5.f, .25f);
//		this.addCube(new Vector2f(-200.f, 55.f), new Vector3f(1.f), 100.f, 50.f, 5.f, 1.f);
//		this.addCube(new Vector2f(0.f, 55.f), new Vector3f(.5f), 100.f, 100.f, 5.f, .5f);
//		this.addCube(new Vector2f(200.f, 55.f), new Vector3f(.25f), 50.f, 100.f, 5.f, .25f);
		
		// Trampoline
//		this.addCube(new Vector2f(0.f), new Vector3f(1.f), this.worldSize.x - 20.f, 300.f, 10.f, 4.f);

		// Constraint test
//		VerletObject lo1 = new VerletObject(new Vector2f(-this.worldSize.x * .25f, this.worldSize.y * .25f), 20.f);
//		lo1.color = new Vector3f(1.f, 0.f, 0.f);
//		lo1.fixed = true;
//		VerletObject lo2 = new VerletObject(new Vector2f(-this.worldSize.x * .25f, -this.worldSize.y * .25f), 20.f);
//		this.solver.addObject(lo1);
//		this.solver.addObject(lo2);
//		this.solver.addLineObj(new LineObject(lo1, lo2));
//		this.solver.addConstraint(new DistanceConstraint(lo1, lo2));
//
//		VerletObject do1 = new VerletObject(new Vector2f(0.f, this.worldSize.y * .25f), 20.f);
//		do1.color = new Vector3f(0.f, 1.f, 0.f);
//		do1.fixed = true;
//		VerletObject do2 = new VerletObject(new Vector2f(0.f, -this.worldSize.y * .25f), 20.f);
//		this.solver.addObject(do1);
//		this.solver.addObject(do2);
//		DistanceConstraint dc = new DistanceConstraint(do1, do2);
//		dc.show = true;
//		this.solver.addConstraint(dc);
//
//		VerletObject so1 = new VerletObject(new Vector2f(this.worldSize.x * .25f, this.worldSize.y * .25f), 20.f);
//		so1.color = new Vector3f(0.f, 0.f, 1.f);
//		so1.fixed = true;
//		VerletObject so2 = new VerletObject(new Vector2f(this.worldSize.x * .25f, -this.worldSize.y * .25f), 20.f);
//		this.solver.addObject(so1);
//		this.solver.addObject(so2);
//		SpringConstraint sc = new SpringConstraint(so1, so2, .1f);
//		sc.show = true;
//		this.solver.addConstraint(sc);
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
	
	private void updateTransform(float windowWidth, float windowHeight)
	{
		final float windowAspect = windowWidth / windowHeight;
		final float simAspect = this.worldSize.x / this.worldSize.y;
		
		final float width = this.worldSize.x * .5f;
		final float height = this.worldSize.y * .5f;
		
		if (windowAspect >= simAspect)
			this.transformMatrix.setOrtho(-(windowAspect / simAspect) * width, (windowAspect / simAspect) * width, -height, height, -1.f, 1.f);
		else
			this.transformMatrix.setOrtho(-width, width, -(simAspect / windowAspect) * height, (simAspect / windowAspect) * height, -1.f, 1.f);
	}
	
	private Vector3f getRainbow(float t)
	{
		final float r = Math.sin(t) * .5f + .5f;
		final float g = Math.sin(t + .33f * 2.f * Math.PI_f) * .5f + .5f;
		final float b = Math.sin(t + .66f * 2.f * Math.PI_f) * .5f + .5f;
		return new Vector3f(r, g, b);
	}
	
	private void update()
	{
//		if (!this.solver.isPaused() && this.solver.getObjectCount() < 4000 && (this.solver.getTotalSteps() % 2) == 0)
		if (IOHandler.isKeyPressed(GLFW_KEY_SPACE) && (this.solver.getTotalSteps() % 2) == 0)
		{
			final float radius = RandUtil.getRange(2.5f, 10.f);
			
			Vector2f velocity = new Vector2f(700.f, 0.f);
			VerletObject obj = new VerletObject(new Vector2f(-this.worldSize.y * .5f * .95f, this.worldSize.y * .5f * .75f), radius);
//			Vector2f velocity = new Vector2f(500.f * Math.sin(this.solver.getTime()), -400.f);
//			VerletObject obj = new VerletObject(new Vector2f(0.f, this.worldSize.y * .5f * .75f), radius);
			
			obj.color = this.getRainbow(this.solver.getTime() * .5f);
			obj.elasticity = 1.f;
			obj.setVelocity(velocity, this.solver.getStepDt());
			
			this.solver.addObject(obj);
		}
		
		this.solver.update();
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
			ImGui.text(String.format("ImGUI FPS: %f", ImGui.getIO().getFramerate()));
			ImGui.text(String.format("Total Frames: %d", this.totalFrames));
			ImGui.text(String.format("FPS: %d\tUPS: %d", this.timer.getFPS(), this.timer.getUPS()));
			ImGui.text(String.format("Mouse Pos: (%f,%f)", IOHandler.MOUSE_POS.x, IOHandler.MOUSE_POS.y));
			ImGui.separator();
			
			ImGui.text(String.format("World Size: %.1f/%.1f", this.worldSize.x, this.worldSize.y));
			ImGui.text(String.format("Window size: %d/%d", this.window.getWidth(), this.window.getHeight()));
			if (ImGui.checkbox("Vsync", this.vSync))
			{
				this.vSync = !this.vSync;
				this.window.setVSync(this.vSync);
			}
			ImGui.separator();
			
			ImGui.colorEdit3("Clear Color", this.backgroundColor);
			if (ImGui.collapsingHeader("Shape Renderer"))
			{
				if (ImGui.checkbox("Enabled##shape", this.shapeRenderer.enabled))
					this.shapeRenderer.enabled = !this.shapeRenderer.enabled;
				ImGui.text(String.format("Buffer capacity: %d", this.shapeRenderer.getBufferCapacity()));
				ImGui.text(String.format("Last floats pushed: %d", this.shapeRenderer.getLastFloatsPushed()));
			}
			if (ImGui.collapsingHeader("Line Renderer"))
			{
				if (ImGui.checkbox("Enabled##line", this.lineRenderer.enabled))
					this.lineRenderer.enabled = !this.lineRenderer.enabled;
				ImGui.text(String.format("Buffer capacity: %d", this.lineRenderer.getBufferCapacity()));
				ImGui.text(String.format("Last floats pushed: %d", this.lineRenderer.getLastFloatsPushed()));
			}
			ImGui.separator();
			
			ImGui.pushItemWidth(windowWidth * .5f);
			int[] vi = {this.targetUps};
			if (ImGui.dragInt("Updates Per Second", vi, 10, 10, 100, "%d"))
			{
				this.targetUps = vi[0];
				this.updateInterval = 1.f / (float) this.targetUps;
				this.solver.setTps(this.targetUps);
			}
			ImGui.text(String.format("Cycle time (1/tps): %fs", this.updateInterval));
			ImGui.popItemWidth();
		}
		ImGui.end();
		
		if (ImGui.begin("Solver"))
		{
			windowWidth = ImGui.getWindowWidth();
			this.solver.gui(windowWidth);
		}
		ImGui.end();
	}
	
	private void run()
	{
		float delta, accumulator = 0.f;
		
		System.out.println("Starting main loop");
		while (!glfwWindowShouldClose(this.window.getHandle()))
		{
			delta = this.timer.getDelta();
			accumulator += delta;
			
			if (accumulator > this.updateInterval)
			{
				accumulator -= this.updateInterval;
				this.timer.updateUPS();
				
				this.update();
				IOHandler.update();
			}
			
			this.timer.updateFPS();
			this.timer.update();
			
			this.imgui.update();
			this.gui();
			
			// render
			glClearColor(this.backgroundColor[0], this.backgroundColor[1], this.backgroundColor[2], 1.f);
			glClear(GL_COLOR_BUFFER_BIT);
			
			this.render();
			this.totalFrames++;
			
			this.window.render();
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
		
		this.errorCallback.free();
		
		glfwTerminate();
	}
	
	public static void main(String[] args)
	{
		Main main = new Main();
		main.init();
		main.run();
		main.destroy();
	}
}
