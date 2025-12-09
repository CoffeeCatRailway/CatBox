package io.github.coffeecatrailway.engine.physics;

import imgui.ImGui;
import io.github.coffeecatrailway.catbox.RandUtil;
import io.github.coffeecatrailway.engine.physics.object.VerletObject;
import io.github.coffeecatrailway.engine.renderer.LineRenderer;
import io.github.coffeecatrailway.engine.renderer.ShapeRenderer;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;

public class Solver
{
	private int subSteps = 1;
	private final Vector2f gravity = new Vector2f(0.f, -100.f);
	
	private final Vector2f constraintCenter = new Vector2f(0.f);
	private float constraintRadius = 100.f;
	
	private final ArrayList<VerletObject> objects = new ArrayList<>();
	private float time = 0.f, frameDt = 0.f;
	
	private void applyGravity()
	{
		for (VerletObject verletObject : this.objects)
			verletObject.accelerate(this.gravity);
	}
	
	private void checkCollisions(float dt)
	{
		final float elasticity = .75f;
		for (int i = 0; i < this.objects.size(); i++)
		{
			VerletObject p1 = this.objects.get(i);
			for (int j = i + 1; j < this.objects.size(); j++)
			{
				VerletObject p2 = this.objects.get(j);
				Vector2f dir = p1.position.sub(p2.position, new Vector2f());
				final float dist = dir.length();
				final float minDist = p1.radius + p2.radius;
				if (dist < minDist)
				{
					dir.normalize();
					if (Math.signum(dist) == 0)
						dir.set(RandUtil.getVec2f());
					
					final float massRatio1 = p1.radius / minDist;
					final float massRatio2 = p2.radius / minDist;
					final float force = .5f * elasticity * (dist - minDist);
					if (!p1.fixed)
						p1.position.sub(dir.mul(massRatio2 * force, new Vector2f()));
					if (!p2.fixed)
						p2.position.add(dir.mul(massRatio1 * force, new Vector2f()));
				}
			}
		}
	}
	
	private void applyConstraint()
	{
		for (VerletObject verletObject : this.objects)
		{
			Vector2f dir = this.constraintCenter.sub(verletObject.position, new Vector2f());
			final float dist = dir.length();
			if (dist > this.constraintRadius - verletObject.radius)
			{
				dir.normalize();
				this.constraintCenter.sub(dir.mul(this.constraintRadius - verletObject.radius), verletObject.position);
			}
		}
	}
	
	private void updateObjects(float dt)
	{
		for (VerletObject verletObject : this.objects)
			verletObject.update(dt);
	}
	
	public void update(float dt)
	{
		this.time += this.frameDt;
		final float stepDt = this.getStepDt();
		for (int i = 0; i < this.subSteps; i++)
		{
			this.applyGravity();
			this.checkCollisions(stepDt);
			this.applyConstraint();
			this.updateObjects(stepDt);
		}
	}
	
	public void setTps(int rate)
	{
		this.frameDt = 1.f / (float) rate;
	}
	
	public void setConstraint(Vector2f pos, float radius)
	{
		this.constraintCenter.set(pos);
		this.constraintRadius = radius;
	}
	
	public void setSubSteps(int subSteps)
	{
		this.subSteps = subSteps;
	}
	
	public boolean addParticle(VerletObject verletObject)
	{
		return this.objects.add(verletObject);
	}
	
	public void setParticleVelocity(VerletObject verletObject, Vector2f velocity)
	{
		verletObject.setVelocity(velocity, this.getStepDt());
	}
	
	public int getObjectCount()
	{
		return this.objects.size();
	}
	
	public float getStepDt()
	{
		return this.frameDt / (float) this.subSteps;
	}
	
	public float getTime()
	{
		return this.time;
	}
	
	public void render(ShapeRenderer shapeRenderer, LineRenderer lineRenderer)
	{
		shapeRenderer.pushCircle(this.constraintCenter, new Vector3f(0.075f), this.constraintRadius, 0.f);
		
		for (VerletObject verletObject : this.objects)
			verletObject.render(this.frameDt, shapeRenderer, lineRenderer);
	}
	
	public void destroy()
	{
		System.out.println("Destroying Solver");
	}
	
	public void gui(float windowWidth)
	{
		ImGui.text(String.format("Objects: %d", this.objects.size()));
		ImGui.text(String.format("Time elapsed: %f", this.time));
		ImGui.text(String.format("Sub steps: %d", this.subSteps));
		ImGui.text(String.format("Frame dt: %f", this.frameDt));
		
		ImGui.pushItemWidth(windowWidth * .5f);
		float[] gravity = new float[] {this.gravity.x, this.gravity.y};
		if (ImGui.inputFloat2("Gravity", gravity))
			this.gravity.set(gravity);
		ImGui.popItemWidth();
	}
}
