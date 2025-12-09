package io.github.coffeecatrailway.engine.physics.object;

import io.github.coffeecatrailway.engine.renderer.LineRenderer;
import io.github.coffeecatrailway.engine.renderer.ShapeRenderer;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class VerletObject
{
	public Vector2f posCurrent, posLast, acceleration;
	public Vector3f color;
	public float radius, friction, elasticity;
	
	public VerletObject(Vector2f posCurrent, float radius)
	{
		this(posCurrent, new Vector3f(1.f), radius, 0.f, 1.f);
	}
	
	public VerletObject(Vector2f posCurrent, Vector3f color, float radius, float friction, float elasticity)
	{
		this.posCurrent = posCurrent;
		this.posLast = new Vector2f(posCurrent);
		this.acceleration = new Vector2f(0.f);
		this.color = color;
		this.radius = radius;
		this.friction = friction;
		this.elasticity = elasticity;
	}
	
	public void update(float dt)
	{
		Vector2f displacement = this.posCurrent.sub(this.posLast, new Vector2f());
		this.posLast.set(this.posCurrent);
		this.posCurrent.add(displacement).add(this.acceleration.mul(dt * dt, new Vector2f()));
		this.acceleration.set(0.f);
	}
	
	public void accelerate(Vector2f acceleration)
	{
		this.acceleration.add(acceleration);
	}
	
	public void setVelocity(Vector2f velocity, float dt)
	{
		this.posLast.set(this.posCurrent.sub(velocity.mul(dt, new Vector2f()), new Vector2f()));
	}
	
	public void addVelocity(Vector2f velocity, float dt)
	{
		this.posLast.sub(velocity.mul(dt));
	}
	
	public Vector2f getVelocity(float dt)
	{
		return this.posCurrent.sub(this.posLast, new Vector2f()).div(dt);
	}
	
	public void render(ShapeRenderer shapeRenderer, LineRenderer lineRenderer)
	{
		shapeRenderer.pushCircle(this.posCurrent, this.color, this.radius, .1f);
		
//		if (this.velocity.length() > 10.f)
//		{
//			Vector3f color = this.color.negate(new Vector3f());
//			Vector2f p2 = new Vector2f(this.posCurrent).add(new Vector2f(this.velocity).mul(.1f));
//			lineRenderer.pushLine(this.posCurrent, color, p2, color);
//		}
	}
}
