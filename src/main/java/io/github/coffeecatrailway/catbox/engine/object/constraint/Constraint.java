package io.github.coffeecatrailway.catbox.engine.object.constraint;

import io.github.coffeecatrailway.catbox.graphics.LineRenderer;
import io.github.coffeecatrailway.catbox.graphics.ShapeRenderer;

public interface Constraint
{
	void update(float dt);
	
	void render(ShapeRenderer shapeRenderer, LineRenderer lineRenderer);
}
