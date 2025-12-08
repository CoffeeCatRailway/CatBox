package io.github.coffeecatrailway.engine.physics;

import imgui.ImGui;
import io.github.coffeecatrailway.engine.physics.object.Particle;
import io.github.coffeecatrailway.engine.renderer.LineRenderer;
import io.github.coffeecatrailway.engine.renderer.ShapeRenderer;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;

public class Solver
{
	private int subSteps = 1;
	private final Vector2f gravity = new Vector2f(0.f, -98.1f);
	
	private final Vector2f constraintCenter = new Vector2f(0.f);
	private float constraintRadius = 100.f;
	
	private final ArrayList<Particle> particles = new ArrayList<>();
	private float time = 0.f, frameDt = 0.f;
	
	private void applyGravity()
	{
		for (Particle particle : this.particles)
			particle.accelerate(this.gravity);
	}
	
	private void checkCollisions(float dt)
	{
		final float elasticity = .75f;
		for (int i = 0; i < this.particles.size(); i++)
		{
			Particle p1 = this.particles.get(i);
			for (int j = i + 1; j < this.particles.size(); j++)
			{
				Particle p2 = this.particles.get(j);
				Vector2f dir = p1.posCurrent.sub(p2.posCurrent, new Vector2f());
				final float dist = dir.length();
				final float minDist = p1.radius + p2.radius;
				if (dist < minDist)
				{
					dir.normalize();
					final float massRatio1 = p1.radius / minDist;
					final float massRatio2 = p2.radius / minDist;
					final float force = .5f * elasticity * (dist - minDist);
					p1.posCurrent.sub(dir.mul(massRatio2 * force, new Vector2f()));
					p2.posCurrent.add(dir.mul(massRatio1 * force, new Vector2f()));
				}
			}
		}
	}
	
	private void applyConstraint()
	{
		for (Particle particle : this.particles)
		{
			Vector2f dir = this.constraintCenter.sub(particle.posCurrent, new Vector2f());
			final float dist = dir.length();
			if (dist > this.constraintRadius - particle.radius)
			{
				dir.normalize();
				this.constraintCenter.sub(dir.mul(this.constraintRadius - particle.radius), particle.posCurrent);
			}
		}
	}
	
	private void updateObjects(float dt)
	{
		for (Particle particle: this.particles)
			particle.update(dt);
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
	
	public boolean addParticle(Particle particle)
	{
		return this.particles.add(particle);
	}
	
	public void setParticleVelocity(Particle particle, Vector2f velocity)
	{
		particle.setVelocity(velocity, this.getStepDt());
	}
	
	public int getObjectCount()
	{
		return this.particles.size();
	}
	
	public float getStepDt()
	{
		return this.frameDt / (float) this.subSteps;
	}
	
	public void render(ShapeRenderer shapeRenderer, LineRenderer lineRenderer)
	{
//		shapeRenderer.pushCircle(this.constraintCenter, new Vector3f(1.f), this.constraintRadius, .01f);
		
		for (Particle particle: this.particles)
			particle.render(shapeRenderer, lineRenderer);
	}
	
	public void destroy()
	{
		System.out.println("Destroying Solver");
	}
	
	public void gui(float windowWidth)
	{
		ImGui.text(String.format("Objects: %d", this.particles.size()));
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
