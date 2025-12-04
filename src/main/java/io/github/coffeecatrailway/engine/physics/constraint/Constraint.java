package io.github.coffeecatrailway.engine.physics.constraint;

import io.github.coffeecatrailway.engine.renderer.LineRenderer;
import io.github.coffeecatrailway.engine.renderer.ShapeRenderer;

public interface Constraint
{
	void update(float deltaTime);
	
	void render(ShapeRenderer shapeRenderer, LineRenderer lineRenderer);
}
