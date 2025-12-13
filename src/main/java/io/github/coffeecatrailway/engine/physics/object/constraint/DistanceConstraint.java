package io.github.coffeecatrailway.engine.physics.object.constraint;

import io.github.coffeecatrailway.engine.physics.object.VerletObject;
import io.github.coffeecatrailway.engine.renderer.LineRenderer;
import io.github.coffeecatrailway.engine.renderer.ShapeRenderer;
import org.joml.Vector2f;

public class DistanceConstraint implements Constraint
{
	public VerletObject obj1, obj2;
	public float distance;
	public boolean show = false;
	
	public DistanceConstraint(VerletObject obj1, VerletObject obj2)
	{
		this(obj1, obj2, obj1.position.sub(obj2.position, new Vector2f()).length());
	}
	
	public DistanceConstraint(VerletObject obj1, VerletObject obj2, float distance)
	{
		this.obj1 = obj1;
		this.obj2 = obj2;
		this.distance = distance;
	}
	
	@Override
	public void update(float dt)
	{
		Vector2f delta = this.obj2.position.sub(this.obj1.position, new Vector2f());
		Vector2f offset = delta.mul(this.distance / delta.length(), new Vector2f()); // Required delta
		offset.sub(delta).mul(.5f); // offset
		
		if (!this.obj1.fixed)
			this.obj1.position.sub(offset);
		if (!this.obj2.fixed)
			this.obj2.position.add(offset);
	}
	
	@Override
	public void render(ShapeRenderer shapeRenderer, LineRenderer lineRenderer)
	{
		if (this.show)
			shapeRenderer.pushLine(this.obj1.position, this.obj2.position, this.obj1.color, this.obj1.radius, 0.f);
	}
}
