package io.github.coffeecatrailway.catbox.boxes;

import io.github.coffeecatrailway.engine.renderer.shapes.ShapeRenderer;

public interface CatBoxI
{
	void init();
	
	void update(float deltaTime);
	void fixedUpdate(float deltaTime);
	
	void render(ShapeRenderer shapeRenderer);
	
	void gui(float halfWidth);
}
