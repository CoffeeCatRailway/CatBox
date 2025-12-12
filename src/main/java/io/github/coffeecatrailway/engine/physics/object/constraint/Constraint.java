package io.github.coffeecatrailway.engine.physics.object.constraint;

import io.github.coffeecatrailway.engine.renderer.LineRenderer;
import io.github.coffeecatrailway.engine.renderer.ShapeRenderer;

public interface Constraint
{
	void update(float dt);
	
	void render(ShapeRenderer shapeRenderer, LineRenderer lineRenderer);
}
