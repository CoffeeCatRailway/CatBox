package io.github.coffeecatrailway.catbox.engine.object.constraint;

import io.github.coffeecatrailway.catbox.engine.object.VerletObject;
import io.github.coffeecatrailway.catbox.graphics.LineRenderer;
import io.github.coffeecatrailway.catbox.graphics.ShapeRenderer;
import org.joml.Vector2f;
import org.joml.Vector3f;

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
		{
			shapeRenderer.pushLine(this.obj1.position, this.obj2.position, this.obj1.color, this.obj1.radius, 2.f);
			
			Vector3f c1 = new Vector3f(1.f).sub(this.obj1.color);
			Vector3f c2 = new Vector3f(1.f).sub(this.obj2.color);
			Vector2f half = this.obj1.position.add(this.obj2.position, new Vector2f()).mul(.5f);
			Vector2f dir = this.obj1.position.sub(this.obj2.position, new Vector2f()).normalize();
			Vector2f p1 = dir.mul(this.length * .5f, new Vector2f()).add(half);
			Vector2f p2 = dir.mul(-this.length * .5f, new Vector2f()).add(half);
			lineRenderer.pushLine(p1, c1, p2, c2);
			// TODO: Try rendering spring force at ends
		}
	}
}
