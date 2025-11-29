package io.github.coffeecatrailway.catbox.boxes;

import imgui.ImGui;
import io.github.coffeecatrailway.engine.renderer.LineRenderer;
import io.github.coffeecatrailway.engine.renderer.ShapeRenderer;
import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class ShapeCatBox implements CatBoxI
{
	private float radians = 0.f;
	private boolean showBoxes = true;
	
	@Override
	public void init()
	{
	}
	
	@Override
	public void update(float deltaTime)
	{
	}
	
	@Override
	public void fixedUpdate(float deltaTime)
	{
		this.radians += deltaTime;
		if (this.radians > Math.PI_TIMES_2_f)
			this.radians = 0.f;
	}
	
	@Override
	public void render(ShapeRenderer shapeRenderer, LineRenderer lineRenderer)
	{
		shapeRenderer.pushCircle(new Vector2f(0.f), new Vector3f(1.f), 10.f, .2f);
		shapeRenderer.pushCircle(new Vector2f(0.f, 40.f), new Vector3f(1.f), 5.f, .2f);
		
		if (this.showBoxes)
		{
			shapeRenderer.pushBox(new Vector2f(30.f, 40.f), new Vector3f(1.f), new Vector2f((Math.sin(this.radians * 2.f) * .25f + .75f) * 20.f, (Math.cos(this.radians * 2.f) * .25f + .75f) * 40.f), this.radians, .1f);
			shapeRenderer.pushBox(new Vector2f(30.f, 0.f), new Vector3f(0.f, 1.f, 0.f), new Vector2f(10.f), this.radians, .2f);
			shapeRenderer.pushBox(new Vector2f(30.f, -40.f), new Vector3f(1.f), new Vector2f(20.f), this.radians, Math.sin(this.radians * 2.f) * .25f + .25f);
		}
		
		Vector2f p1 = new Vector2f(-50.f, 40.f);
		Vector2f p2 = new Vector2f(p1);
		p2.x += Math.cos(this.radians) * 20.f;
		p2.y += Math.sin(this.radians) * 20.f;
		
		shapeRenderer.pushLine(p1, p2, new Vector3f(1.f), 5.f, .1f);
		shapeRenderer.pushLine(new Vector2f(-50.f, 70.f + Math.sin(this.radians) * 20.f), p2, new Vector3f(1.f), 5.f, .05f);
		shapeRenderer.pushLine(new Vector2f(-50.f, 0.f), new Vector3f(1.f), 40.f, 5.f, this.radians, .05f);
		
		shapeRenderer.pushCircle(p2, new Vector3f(1.f), 4.f, .2f);
		
		lineRenderer.pushLine(new Vector2f(-100.f), new Vector3f(1.f), new Vector2f(100.f), new Vector3f(1.f));
	}
	
	@Override
	public void destroy()
	{
	}
	
	@Override
	public void gui(float halfWidth)
	{
		ImGui.text(String.format("Radians: %.3f\nDegrees: %.3f", this.radians, Math.toDegrees(this.radians)));
		if (ImGui.checkbox("Show boxes", this.showBoxes))
			this.showBoxes = !this.showBoxes;
	}
}
