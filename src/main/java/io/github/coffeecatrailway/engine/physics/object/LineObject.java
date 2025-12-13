package io.github.coffeecatrailway.engine.physics.object;

import io.github.coffeecatrailway.engine.renderer.LineRenderer;
import io.github.coffeecatrailway.engine.renderer.ShapeRenderer;
import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class LineObject
{
	public VerletObject obj1, obj2;
	public float thickness;
	
	public LineObject(VerletObject obj1, VerletObject obj2)
	{
		this(obj1, obj2, obj1.radius * 2.f);
	}
	
	public LineObject(VerletObject obj1, VerletObject obj2, float thickness)
	{
		this.obj1 = obj1;
		this.obj2 = obj2;
		this.thickness = thickness;
	}
	
	public Vector2f getNormal()
	{
		return new Vector2f(obj2.position.y - obj1.position.y, -(obj2.position.x - obj1.position.x)).normalize();
	}
	
	public Vector2f getTangent()
	{
		Vector2f tangent = this.obj2.position.sub(this.obj1.position, new Vector2f());
		return tangent.normalize();
	}
	
	public float getLength()
	{
		float x = obj2.position.x - obj1.position.x;
		float y = obj2.position.y - obj1.position.y;
		return Math.sqrt(x * x + y * y);
	}
	
	public void render(ShapeRenderer shapeRenderer, LineRenderer lineRenderer)
	{
		float thickness = Math.max(this.thickness, .5f);
		shapeRenderer.pushLine(this.obj1.position, this.obj2.position, this.obj1.color, thickness, 0.f);
//		shapeRenderer.pushCircle(this.obj1.position, this.obj1.color, thickness * .5f, 0.f);
//		shapeRenderer.pushCircle(this.obj2.position, this.obj2.color, thickness * .5f, 0.f);
		
		Vector2f half = this.obj1.position.add(this.obj2.position, new Vector2f()).mul(.5f);
		final Vector3f red = new Vector3f(1.f, 0.f, 0.f);
		final Vector3f blue = new Vector3f(0.f, 0.f, 1.f);
		lineRenderer.pushLine(half, red, this.getNormal().mul(thickness).add(half), red);
		lineRenderer.pushLine(half, blue, this.getTangent().mul(thickness).add(half), blue);
	}
}
