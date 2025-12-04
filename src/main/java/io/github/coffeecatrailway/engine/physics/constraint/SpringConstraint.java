package io.github.coffeecatrailway.engine.physics.constraint;

import io.github.coffeecatrailway.engine.physics.object.Particle;
import io.github.coffeecatrailway.engine.renderer.LineRenderer;
import io.github.coffeecatrailway.engine.renderer.ShapeRenderer;
import org.joml.Math;
import org.joml.Vector2f;

public class SpringConstraint implements Constraint
{
	public Particle p1, p2;
	public float length, force, damping;
	
	public SpringConstraint(Particle p1, Particle p2, float force)
	{
		this(p1, p2, p1.position.distance(p2.position), force);
	}
	
	public SpringConstraint(Particle p1, Particle p2, float length, float force)
	{
		this(p1, p2, length, force, 2.f);
	}
	
	public SpringConstraint(Particle p1, Particle p2, float length, float force, float damping)
	{
		this.p1 = p1;
		this.p2 = p2;
		this.length = length;
		this.force = force;
		this.damping = damping;
	}
	
	@Override
	public void update(float deltaTime)
	{
//		Vector2f force = this.p2.position.sub(this.p1.position, new Vector2f());
//		final float forceFactor = (this.length - force.length()) * this.force;
//		force.normalize().mul(forceFactor);
//
//		Vector2f relVelocity = this.p1.velocity.sub(this.p1.velocity, new Vector2f()).mul(this.damping);
//		force.sub(relVelocity).mul(deltaTime);
//
//		this.p1.velocity.sub(force);
//		this.p2.velocity.add(force);
		
		Vector2f delta = this.p2.position.sub(this.p1.position, new Vector2f());
		Vector2f dir = delta.normalize(new Vector2f());

		Vector2f reqDelta = dir.mul(this.length, new Vector2f());
		Vector2f force = reqDelta.sub(delta, new Vector2f()).mul(this.force * deltaTime);

		this.p1.velocity.sub(force);
		this.p2.velocity.add(force);

		float relVelocity = this.p2.velocity.sub(this.p1.velocity, new Vector2f()).dot(dir);
		float dampingFactor = (float) Math.exp(-this.damping * deltaTime);
		float newRelVelocity = relVelocity * dampingFactor;
		float relVelocityDelta = (newRelVelocity - relVelocity) * .5f;

		this.p1.velocity.sub(relVelocityDelta, relVelocityDelta);
		this.p2.velocity.add(relVelocityDelta, relVelocityDelta);
	}
	
	@Override
	public void render(ShapeRenderer shapeRenderer, LineRenderer lineRenderer)
	{
		shapeRenderer.pushLine(this.p1.position, this.p2.position, this.p1.color, this.p1.radius * 2.f, 0.025f);
		shapeRenderer.pushCircle(this.p2.position, this.p1.color, this.p1.radius, .1f);
	}
}
