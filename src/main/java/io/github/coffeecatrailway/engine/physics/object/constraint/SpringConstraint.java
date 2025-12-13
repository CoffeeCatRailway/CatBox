package io.github.coffeecatrailway.engine.physics.object.constraint;

import io.github.coffeecatrailway.engine.physics.object.VerletObject;
import io.github.coffeecatrailway.engine.renderer.LineRenderer;
import io.github.coffeecatrailway.engine.renderer.ShapeRenderer;
import org.joml.Vector2f;

public class SpringConstraint implements Constraint
{
	public VerletObject obj1, obj2;
	public float length, force;
	public boolean show = false;
	
	public SpringConstraint(VerletObject obj1, VerletObject obj2, float force)
	{
		this(obj1, obj2, obj1.position.sub(obj2.position, new Vector2f()).length(), force);
	}
	
	public SpringConstraint(VerletObject obj1, VerletObject obj2, float length, float force)
	{
		this.obj1 = obj1;
		this.obj2 = obj2;
		this.length = length;
		this.force = force;
	}
	
	@Override
	public void update(float dt)
	{
		// F = -kx
		// k = spring force, x = displacement
		Vector2f delta = this.obj2.position.sub(this.obj1.position, new Vector2f());
		Vector2f reqDelta = delta.normalize(new Vector2f()).mul(this.length);
		Vector2f force = reqDelta.sub(delta).mul(this.force * dt);
		
		if (!this.obj1.fixed)
			this.obj1.position.sub(force);
		if (!this.obj2.fixed)
			this.obj2.position.add(force);
	}
	
	@Override
	public void render(ShapeRenderer shapeRenderer, LineRenderer lineRenderer)
	{
		if (this.show)
			shapeRenderer.pushLine(this.obj1.position, this.obj2.position, this.obj1.color, this.obj1.radius, 0.f);
	}
}
