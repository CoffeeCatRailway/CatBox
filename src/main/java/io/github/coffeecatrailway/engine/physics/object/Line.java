package io.github.coffeecatrailway.engine.physics.object;

import io.github.coffeecatrailway.engine.renderer.LineRenderer;
import io.github.coffeecatrailway.engine.renderer.ShapeRenderer;
import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Line
{
	public Vector2f p1, p2;
	public float thickness;
	
	public Line(Vector2f p1, Vector2f p2)
	{
		this(p1, p2, 1.f);
	}
	
	public Line(Vector2f p1, Vector2f p2, float thickness)
	{
		this.p1 = p1;
		this.p2 = p2;
		this.thickness = thickness;
	}
	
	public Vector2f getNormal()
	{
		return new Vector2f(p2.y - p1.y, -(p2.x - p1.x)).normalize();
	}
	
	public Vector2f getTangent()
	{
		Vector2f tangent = this.p2.sub(this.p1, new Vector2f());
		return tangent.normalize();
	}
	
	public float getLength()
	{
		float x = p2.x - p1.x;
		float y = p2.y - p1.y;
		return Math.sqrt(x * x + y * y);
	}
	
	public void render(ShapeRenderer shapeRenderer, LineRenderer lineRenderer)
	{
		float thickness = Math.max(this.thickness, .5f);
		shapeRenderer.pushLine(this.p1, this.p2, new Vector3f(1.f), thickness, 0.f);
		shapeRenderer.pushCircle(this.p1, new Vector3f(1.f), thickness * .5f, 0.f);
		shapeRenderer.pushCircle(this.p2, new Vector3f(1.f), thickness * .5f, 0.f);
		
		Vector2f half = this.p1.add(this.p2, new Vector2f()).mul(.5f);
		final Vector3f red = new Vector3f(1.f, 0.f, 0.f);
		final Vector3f blue = new Vector3f(0.f, 0.f, 1.f);
		lineRenderer.pushLine(half, red, this.getNormal().mul(5.f).add(half), red);
		lineRenderer.pushLine(half, blue, this.getTangent().mul(5.f).add(half), blue);
	}
}
