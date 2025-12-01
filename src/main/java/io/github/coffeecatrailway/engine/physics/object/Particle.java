package io.github.coffeecatrailway.engine.physics.object;

import io.github.coffeecatrailway.engine.renderer.LineRenderer;
import io.github.coffeecatrailway.engine.renderer.ShapeRenderer;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Particle
{
	public Vector2f position, velocity;
	public Vector3f color;
	public float radius, friction, restitution;
	
	public Particle(Vector2f position, float radius)
	{
		this(position, new Vector2f(0.f), new Vector3f(1.f), radius, .003f, .9f);
	}
	
	public Particle(Vector2f position, Vector2f velocity, Vector3f color, float radius, float friction, float restitution)
	{
		this.position = position;
		this.velocity = velocity;
		this.color = color;
		this.radius = radius;
		this.friction = friction;
		this.restitution = restitution;
	}
	
	public void render(ShapeRenderer shapeRenderer, LineRenderer lineRenderer)
	{
		shapeRenderer.pushCircle(this.position, this.color, this.radius, .1f);
		
		if (this.velocity.length() > 10.f)
		{
			Vector3f color = this.color.negate(new Vector3f());
			Vector2f p2 = new Vector2f(this.position).add(new Vector2f(this.velocity).mul(.1f));
			lineRenderer.pushLine(this.position, color, p2, color);
		}
	}
}
