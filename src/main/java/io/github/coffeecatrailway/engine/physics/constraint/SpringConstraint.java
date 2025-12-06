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
	
	public SpringConstraint(Particle p1, Particle p2, float force, float damping)
	{
		this(p1, p2, p1.position.distance(p2.position), force, damping);
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
		// F = -kx
		// k = spring force, x = displacement
		Vector2f delta = this.p2.position.sub(this.p1.position, new Vector2f());
		Vector2f dir = delta.normalize(new Vector2f());

		Vector2f reqDelta = dir.mul(this.length, new Vector2f());
		Vector2f force = reqDelta.sub(delta, new Vector2f()).mul(this.force * deltaTime);
		
		// Damping
		// Hacky, but anything I try makes it unstable
		force.mul((float) Math.exp(-this.damping * deltaTime));

		this.p1.velocity.sub(force);
		this.p2.velocity.add(force);

//		float velDelta = this.p2.velocity.sub(this.p1.velocity, new Vector2f()).dot(dir);
//		float velDeltaDamp = velDelta * (float) Math.exp(-this.damping * deltaTime);
//		float dampForce = (velDeltaDamp - velDelta) * .5f;
//
//		this.p1.velocity.sub(dampForce, dampForce);
//		this.p2.velocity.add(dampForce, dampForce);

//		Vector2f velDelta = this.p2.velocity.sub(this.p1.velocity, new Vector2f());
//		Vector2f velDeltaDamp = velDelta.mul((float) Math.exp(-this.damping * deltaTime), new Vector2f());
//		velDeltaDamp.sub(velDelta).mul(.5f);
//
//		this.p1.velocity.sub(velDeltaDamp);
//		this.p2.velocity.add(velDeltaDamp);
	}
	
	@Override
	public void render(ShapeRenderer shapeRenderer, LineRenderer lineRenderer)
	{
		shapeRenderer.pushLine(this.p1.position, this.p2.position, this.p1.color, this.p1.radius * 2.f, 0.025f);
		shapeRenderer.pushCircle(this.p2.position, this.p1.color, this.p1.radius, .1f);
	}
}
