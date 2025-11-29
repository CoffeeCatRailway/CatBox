package io.github.coffeecatrailway.catbox.boxes;

import io.github.coffeecatrailway.engine.renderer.LineRenderer;
import io.github.coffeecatrailway.engine.renderer.ShapeRenderer;

public interface CatBoxI
{
	void init();
	
	void update(float deltaTime);
	void fixedUpdate(float deltaTime);
	
	void render(ShapeRenderer shapeRenderer, LineRenderer lineRenderer);
	
	void destroy();
	
	void gui(float halfWidth);
}
