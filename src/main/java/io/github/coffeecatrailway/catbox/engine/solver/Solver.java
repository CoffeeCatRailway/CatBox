package io.github.coffeecatrailway.catbox.engine.solver;

import imgui.ImGui;
import io.github.coffeecatrailway.catbox.engine.RandUtil;
import io.github.coffeecatrailway.catbox.engine.object.LineObject;
import io.github.coffeecatrailway.catbox.engine.object.VerletObject;
import io.github.coffeecatrailway.catbox.engine.object.constraint.Constraint;
import io.github.coffeecatrailway.catbox.graphics.LineRenderer;
import io.github.coffeecatrailway.catbox.graphics.ShapeRenderer;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public abstract class Solver
{
	private final String title;
	public final Vector2f gravity = new Vector2f(0.f);
	public final Vector2f worldSize;
	
	protected final ArrayList<VerletObject> objects = new ArrayList<>();
	protected final ArrayList<LineObject> lineObjects = new ArrayList<>();
	protected final ArrayList<Constraint> constraints = new ArrayList<>();
	
	private int subSteps, totalSteps = 0;
	private boolean pause = true, btnStep = false;
	private float time = 0.f, frameDt = 1.f / 60.f;
	private double updateTime = 0.;
	
	public Solver(String title, float worldWidth, float worldHeight, int subSteps)
	{
		this.title = title;
		this.worldSize = new Vector2f(worldWidth, worldHeight);
		this.subSteps = subSteps;
	}
	
	protected void solveObjectObjectContact(VerletObject obj1, VerletObject obj2)
	{
		Vector2f dir = obj1.position.sub(obj2.position, new Vector2f());
		final float dist = dir.length();
		final float minDist = obj1.radius + obj2.radius;
		if (dist < minDist)
		{
			dir.normalize();
			if (Math.signum(dist) == 0)
				dir.set(RandUtil.getVec2f());
			
			final float massRatio1 = obj1.radius / minDist;
			final float massRatio2 = obj2.radius / minDist;
			final float force = .5f * ((obj1.elasticity + obj2.elasticity) * .5f) * (dist - minDist);
			
			if (!obj1.fixed)
				obj1.position.sub(dir.mul(massRatio2 * force, new Vector2f()));
			if (!obj2.fixed)
				obj2.position.add(dir.mul(massRatio1 * force, new Vector2f()));
		}
	}
	
	protected void solveObjectLineContact(VerletObject obj, LineObject lineObj)
	{
		if (obj == lineObj.obj1 || obj == lineObj.obj2)
			return;
		
		Vector2f local = obj.position.sub(lineObj.obj1.position, new Vector2f());
		final float distAlongLine = local.dot(lineObj.getTangent());
		
		// Default to along the line
		Vector2f normal = lineObj.getNormal();
		float distAwayFromLine = local.dot(normal);
		if (distAwayFromLine < 0.f)
			normal.negate();
		distAwayFromLine = Math.abs(distAwayFromLine);
		
		// Check if ball is colliding with line end
		if (distAlongLine < 0.f || distAlongLine > lineObj.getLength())
		{
			// Check what end the object is colliding with
			if (distAlongLine < 0.f)
				obj.position.sub(lineObj.obj1.position, normal).normalize();
			else
			{
				obj.position.sub(lineObj.obj2.position, local);
				local.normalize(normal);
			}
			distAwayFromLine = Math.abs(local.dot(normal));
		}
		
		final float minDist = lineObj.thickness * .5f + obj.radius;
		if (distAwayFromLine < minDist)
		{
			final float totalMass = obj.radius + lineObj.obj1.radius + lineObj.obj2.radius;
			final float massRatioObj = obj.radius / totalMass;
			final float massRatioL1 = lineObj.obj1.radius / totalMass;
			final float massRatioL2 = lineObj.obj2.radius / totalMass;
			
			final float p = distAlongLine / lineObj.getLength();
			final float elasticityP = lineObj.obj1.elasticity * (1.f - p) + lineObj.obj2.elasticity * p;
			final float massRatioP = massRatioL1 * (1.f - p) + massRatioL2 * p;
			
			final float force = (1.f / 3.f) * ((obj.elasticity + elasticityP) * .5f) * (distAwayFromLine - minDist);
			
			if (!obj.fixed)
				obj.position.sub(normal.mul(massRatioP * force, new Vector2f()));
			if (!lineObj.obj1.fixed)
				lineObj.obj1.position.add(normal.mul(massRatioObj * force * (1.f - p), new Vector2f()));
			if (!lineObj.obj2.fixed)
				lineObj.obj2.position.add(normal.mul(massRatioObj * force * p, new Vector2f()));
		}
	}
	
	protected void applyWorldConstraint(VerletObject obj)
	{
		final float halfWidth = this.worldSize.x * .5f;
		if (obj.position.x < -halfWidth + obj.radius)
			obj.position.x = -halfWidth + obj.radius;
		else if (obj.position.x > halfWidth - obj.radius)
			obj.position.x = halfWidth - obj.radius;
		
		final float halfHeight = this.worldSize.y * .5f;
		if (obj.position.y < -halfHeight + obj.radius)
			obj.position.y = -halfHeight + obj.radius;
		else if (obj.position.y > halfHeight - obj.radius)
			obj.position.y = halfHeight - obj.radius;
	}
	
	public abstract void step(float stepDt);
	
	public void update()
	{
		if (!this.pause || this.btnStep)
		{
			double then = GLFW.glfwGetTime();
			
			this.time += this.frameDt;
			final float stepDt = this.getStepDt();
			for (int i = 0; i < this.subSteps; i++)
			{
				this.step(stepDt);
			}
			
			double now = GLFW.glfwGetTime();
			this.updateTime = (now - then) * 1_000;
			
			this.totalSteps++;
			this.btnStep = false;
		}
	}
	
	public void render(ShapeRenderer shapeRenderer, LineRenderer lineRenderer)
	{
		shapeRenderer.pushBox(new Vector2f(0.f), new Vector3f(.15f), this.worldSize, 0.f, 10.f);
		
		for (Constraint constraint : this.constraints)
			constraint.render(shapeRenderer, lineRenderer);
		
		for (LineObject lineObj : this.lineObjects)
			lineObj.render(shapeRenderer, lineRenderer);
		
		for (VerletObject obj : this.objects)
			obj.render(this.frameDt, shapeRenderer, lineRenderer);
	}
	
	public void destroy()
	{
		System.out.println("Destroying Solver");
	}
	
	public void gui(float windowWidth)
	{
		ImGui.text(String.format("Solver: %s", this.title));
		ImGui.separator();
		
		ImGui.text(String.format("Objects: %d", this.objects.size()));
		ImGui.text(String.format("Line Objects: %d", this.lineObjects.size()));
		ImGui.text(String.format("Constraints: %d", this.constraints.size()));
		ImGui.separator();
		
		ImGui.text(String.format("Time elapsed: %fs", this.time));
		ImGui.text(String.format("Frame dt: %fs", this.frameDt));
		ImGui.text(String.format("Update time: %fms", this.updateTime));
		double avg = updateTimes.stream().mapToDouble(Double::doubleValue).sum() / (double) updateTimes.size();
		ImGui.text(String.format("Avg(%d) update time: %fms", updateTimeAvg, avg));
		ImGui.text(String.format("Sub steps: %d\tTotal Steps: %d", this.subSteps, this.totalSteps));
		if (ImGui.checkbox("Pause Fixed", this.pause))
			this.pause = !this.pause;
		if (this.pause)
		{
			ImGui.sameLine(0.f, 10.f);
			if (ImGui.smallButton("Step"))
				this.btnStep = true;
		}
		ImGui.separator();
		
		ImGui.pushItemWidth(windowWidth * .5f);
		float[] gravity = new float[] {this.gravity.x, this.gravity.y};
		if (ImGui.inputFloat2("Gravity", gravity, "%.2f"))
			this.gravity.set(gravity);
		ImGui.popItemWidth();
	}
	
	public boolean addObject(VerletObject obj)
	{
		return this.objects.add(obj);
	}
	
	public VerletObject getObjectByIndex(int i)
	{
		return this.objects.get(i);
	}
	
	public int getObjectCount()
	{
		return this.objects.size();
	}
	
	public boolean addLineObj(LineObject lineObj)
	{
		return this.lineObjects.add(lineObj);
	}
	
	public int getLineCount()
	{
		return this.lineObjects.size();
	}
	
	public boolean addConstraint(Constraint constraint)
	{
		return this.constraints.add(constraint);
	}
	
	public int getConstraintCount()
	{
		return this.constraints.size();
	}
	
	public void setSubSteps(int subSteps)
	{
		this.subSteps = subSteps;
	}
	
	public int getTotalSteps()
	{
		return this.totalSteps;
	}
	
	public boolean isPaused()
	{
		return this.pause;
	}
	
	public float getTime()
	{
		return this.time;
	}
	
	public float getStepDt()
	{
		return this.frameDt / (float) this.subSteps;
	}
	
	public void setTps(int rate)
	{
		this.frameDt = 1.f / (float) rate;
	}
}
