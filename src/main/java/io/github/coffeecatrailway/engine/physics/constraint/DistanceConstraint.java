package io.github.coffeecatrailway.engine.physics.constraint;

import io.github.coffeecatrailway.engine.physics.object.Particle;
import io.github.coffeecatrailway.engine.renderer.LineRenderer;
import io.github.coffeecatrailway.engine.renderer.ShapeRenderer;
import org.joml.Math;
import org.joml.Vector2f;

public class DistanceConstraint implements Constraint
{
	public Particle p1, p2;
	public float length;
	
	public DistanceConstraint(Particle p1, Particle p2)
	{
		this(p1, p2, p1.position.distance(p2.position));
	}
	
	public DistanceConstraint(Particle p1, Particle p2, float length)
	{
		this.p1 = p1;
		this.p2 = p2;
		this.length = length;
	}
	
	@Override
	public void update(float deltaTime)
	{
		Vector2f delta = this.p2.position.sub(this.p1.position, new Vector2f());
		Vector2f offset = delta.mul(this.length / delta.length(), new Vector2f()); // Required delta
		offset.sub(delta).mul(.5f); // offset
		
		this.p1.position.sub(offset);
		this.p2.position.add(offset);
	}
	
	@Override
	public void render(ShapeRenderer shapeRenderer, LineRenderer lineRenderer)
	{
		shapeRenderer.pushLine(this.p1.position, this.p2.position, this.p1.color, this.p1.radius * 2.f, 0.025f);
		shapeRenderer.pushCircle(this.p2.position, this.p1.color, this.p1.radius, .1f);
		
//		Vector2f half = this.p1.add(this.p2, new Vector2f()).mul(.5f);
//		final Vector3f red = new Vector3f(1.f, 0.f, 0.f);
//		final Vector3f blue = new Vector3f(0.f, 0.f, 1.f);
//		lineRenderer.pushLine(half, red, this.getNormal().mul(5.f).add(half), red);
//		lineRenderer.pushLine(half, blue, this.getTangent().mul(5.f).add(half), blue);
	}
}
