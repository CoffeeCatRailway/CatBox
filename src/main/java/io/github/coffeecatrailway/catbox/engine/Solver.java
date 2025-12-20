package io.github.coffeecatrailway.catbox.engine;

import imgui.ImGui;
import io.github.coffeecatrailway.catbox.engine.object.LineObject;
import io.github.coffeecatrailway.catbox.engine.object.VerletObject;
import io.github.coffeecatrailway.catbox.engine.object.constraint.Constraint;
import io.github.coffeecatrailway.catbox.graphics.LineRenderer;
import io.github.coffeecatrailway.catbox.graphics.ShapeRenderer;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;

public class Solver
{
	public final Vector2f gravity = new Vector2f(0.f);
	public final Vector2f worldSize;
	
	private final ArrayList<VerletObject> objects = new ArrayList<>();
	private final ArrayList<LineObject> lineObjects = new ArrayList<>();
	private final ArrayList<Constraint> constraints = new ArrayList<>();
	
	private int subSteps = 8, totalSteps = 0;
	private boolean pause = true, btnStep = false;
	private float time = 0.f, frameDt = 1.f / 60.f;
	
	public Solver(float worldWidth, float worldHeight)
	{
		this.worldSize = new Vector2f(worldWidth, worldHeight);
	}
	
	private void solveObjectObjectContact(VerletObject obj1, VerletObject obj2)
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

	private void checkCollisions(float dt)
	{
		for (int i = 0; i < this.objects.size(); i++)
		{
			VerletObject obj1 = this.objects.get(i);
			// obj-obj
			for (int j = i + 1; j < this.objects.size(); j++)
			{
				VerletObject obj2 = this.objects.get(j);
				if ((obj2.position.x - obj2.radius) > (obj1.position.x + obj1.radius)) break; // obj2.left > obj1.right
				this.solveObjectObjectContact(obj1, obj2);
			}

			// obj-line
			for (LineObject lineObj : this.lineObjects)
			{
				if (obj1 == lineObj.obj1 || obj1 == lineObj.obj2)
					continue;

				Vector2f local = obj1.position.sub(lineObj.obj1.position, new Vector2f());
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
					// Check what end the obj is colliding with
					if (distAlongLine < 0.f)
						obj1.position.sub(lineObj.obj1.position, normal).normalize();
					else
					{
						obj1.position.sub(lineObj.obj2.position, local);
						local.normalize(normal);
					}
					distAwayFromLine = Math.abs(local.dot(normal));
				}

				final float minDist = lineObj.thickness * .5f + obj1.radius;
				if (distAwayFromLine < minDist)
				{
					final float totalMass = obj1.radius + lineObj.obj1.radius + lineObj.obj2.radius;
					final float massRatioObj = obj1.radius / totalMass;
					final float massRatioL1 = lineObj.obj1.radius / totalMass;
					final float massRatioL2 = lineObj.obj2.radius / totalMass;

					final float p = distAlongLine / lineObj.getLength();
					final float elasticityP = lineObj.obj1.elasticity * (1.f - p) + lineObj.obj2.elasticity * p;
					final float massRatioP = massRatioL1 * (1.f - p) + massRatioL2 * p;

					final float force = (1.f / 3.f) * ((obj1.elasticity + elasticityP) * .5f) * (distAwayFromLine - minDist);

					if (!obj1.fixed)
						obj1.position.sub(normal.mul(massRatioP * force, new Vector2f()));
					if (!lineObj.obj1.fixed)
						lineObj.obj1.position.add(normal.mul(massRatioObj * force * (1.f - p), new Vector2f()));
					if (!lineObj.obj2.fixed)
						lineObj.obj2.position.add(normal.mul(massRatioObj * force * p, new Vector2f()));
				}
			}
		}
	}
	
	private void applyWorldConstraint(VerletObject obj)
	{
//		Vector2f dir = this.constraintCenter.sub(obj.position, new Vector2f());
//		final float dist = dir.length();
//		if (dist > this.constraintRadius - obj.radius)
//		{
//			dir.normalize();
//			this.constraintCenter.sub(dir.mul(this.constraintRadius - obj.radius), obj.position);
//
////				final float force = .5f * obj.elasticity * (this.constraintRadius - (this.constraintRadius - obj.radius));
////				obj.position.add(dir.mul(force / obj.radius));
//		}

		final float halfWidth = this.worldSize.x * .5f;
		if (obj.position.x < -halfWidth + obj.radius)
			obj.position.x = -halfWidth + obj.radius;
		else if (obj.position.x > halfWidth - obj.radius)
			obj.position.x = halfWidth - obj.radius;
		
		final float halfHeight = this.worldSize.y * .5f;
		if (obj.position.y < -halfHeight + obj.radius)
//			obj.position.y -= (Math.abs(-halfHeight + obj.radius) - Math.abs(obj.position.y)) * obj.elasticity;
			obj.position.y = -halfHeight + obj.radius;
		else if (obj.position.y > halfHeight - obj.radius)
			obj.position.y = halfHeight - obj.radius;
	}
	
	private void updateObjects(float dt)
	{
		for (VerletObject obj : this.objects)
		{
			obj.accelerate(this.gravity);
			obj.update(dt);
			this.applyWorldConstraint(obj);
		}
	}

	private void sortObjectsByLeft()
	{
		this.objects.sort(Comparator.comparingInt(o -> (int) (o.position.x - o.radius)));
//		this.objects.sort((o1, o2) -> (int) (o1.position.x - o1.radius) - (int) (o2.position.x - o2.radius));
	}
	
	public void update()
	{
		if (!this.pause || this.btnStep)
		{
			this.time += this.frameDt;
			final float stepDt = this.getStepDt();
			for (int i = 0; i < this.subSteps; i++)
			{
				this.sortObjectsByLeft();
				this.checkCollisions(stepDt);
				
				for (Constraint constraint : this.constraints)
					constraint.update(stepDt);
				
				this.updateObjects(stepDt);
			}
			
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
		ImGui.text(String.format("Objects: %d", this.objects.size()));
		ImGui.text(String.format("Line Objects: %d", this.lineObjects.size()));
		ImGui.text(String.format("Constraints: %d", this.constraints.size()));
		ImGui.separator();
		
		ImGui.text(String.format("Time elapsed: %f", this.time));
		ImGui.text(String.format("Frame dt: %f", this.frameDt));
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
	
	public void setTps(int rate)
	{
		this.frameDt = 1.f / (float) rate;
	}
	
	public float getTime()
	{
		return this.time;
	}
	
	public float getStepDt()
	{
		return this.frameDt / (float) this.subSteps;
	}
}
