package io.github.coffeecatrailway.catbox.core;

import java.util.Objects;

public record Pair<F, S>(F first, S second)
{
	@Override
	public boolean equals(Object o)
	{
		if (o == null || getClass() != o.getClass()) return false;
		Pair<?, ?> pair = (Pair<?, ?>) o;
		return Objects.equals(this.first, pair.first) && Objects.equals(this.second, pair.second);
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(this.first, this.second);
	}
}
