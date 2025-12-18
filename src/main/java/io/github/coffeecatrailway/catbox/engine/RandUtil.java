package io.github.coffeecatrailway.catbox.engine;

import org.joml.Random;
import org.joml.Vector2f;

public final class RandUtil
{
	public static final Random RAND = new Random(0L);
	
	public static float getRange(float min, float max)
	{
		return min + RAND.nextFloat() * (max - min);
	}
	
	public static Vector2f getVec2f()
	{
		return new Vector2f(RAND.nextFloat() * 2.f - 1.f, RAND.nextFloat() * 2.f - 1.f);
	}
}
