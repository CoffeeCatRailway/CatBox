package io.github.coffeecatrailway.engine.physics.object.constraint;

import io.github.coffeecatrailway.engine.render.LineRenderer;
import io.github.coffeecatrailway.engine.render.ShapeRenderer;

public interface Constraint
{
	void update(float dt);
	
	void render(ShapeRenderer shapeRenderer, LineRenderer lineRenderer);
}
