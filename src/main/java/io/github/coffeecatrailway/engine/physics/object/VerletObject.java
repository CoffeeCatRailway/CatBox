package io.github.coffeecatrailway.engine.physics.object;

import io.github.coffeecatrailway.engine.renderer.LineRenderer;
import io.github.coffeecatrailway.engine.renderer.ShapeRenderer;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class VerletObject
{
	public Vector2f position, positionLast, acceleration;
	public Vector3f color;
	public float radius, friction, elasticity;
	public boolean fixed = false, show = true;
	
	public VerletObject(Vector2f position, float radius)
	{
		this(position, new Vector3f(1.f), radius, 0.f, 1.f);
	}
	
	public VerletObject(Vector2f position, Vector3f color, float radius, float friction, float elasticity)
	{
		this.position = position;
		this.positionLast = new Vector2f(position);
		this.acceleration = new Vector2f(0.f);
		this.color = color;
		this.radius = radius;
		this.friction = friction;
		this.elasticity = elasticity;
	}
	
	public void update(float dt)
	{
		if (this.fixed)
			return;
		Vector2f displacement = this.position.sub(this.positionLast, new Vector2f());
		this.positionLast.set(this.position);
		this.position.add(displacement).add(this.acceleration.mul(dt * dt, new Vector2f()));
		this.acceleration.set(0.f);
	}
	
	public void accelerate(Vector2f acceleration)
	{
		if (this.fixed)
			return;
		this.acceleration.add(acceleration);
	}
	
	public void setVelocity(Vector2f velocity, float dt)
	{
		if (this.fixed)
			return;
		this.positionLast.set(this.position.sub(velocity.mul(dt, new Vector2f()), new Vector2f()));
	}
	
	public void addVelocity(Vector2f velocity, float dt)
	{
		if (this.fixed)
			return;
		this.positionLast.sub(velocity.mul(dt));
	}
	
	public Vector2f getVelocity(float dt)
	{
		return this.position.sub(this.positionLast, new Vector2f()).div(dt);
	}
	
	public void render(float dt, ShapeRenderer shapeRenderer, LineRenderer lineRenderer)
	{
		if (this.show)
		{
			shapeRenderer.pushCircle(this.position, this.color, this.radius, .1f);
			
			Vector2f velocity = this.getVelocity(dt);
			Vector3f color = new Vector3f(1.f).sub(this.color);
			Vector2f p2 = new Vector2f(this.position).add(velocity.mul(1.f));
			lineRenderer.pushLine(this.position, color, p2, color);
		}
	}
}
