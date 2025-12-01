package io.github.coffeecatrailway.catbox.boxes;

import imgui.ImGui;
import imgui.type.ImFloat;
import io.github.coffeecatrailway.engine.physics.object.Line;
import io.github.coffeecatrailway.engine.physics.object.Particle;
import io.github.coffeecatrailway.engine.renderer.LineRenderer;
import io.github.coffeecatrailway.engine.renderer.ShapeRenderer;
import org.joml.Math;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class ForceCatBox implements CatBoxI
{
	/*
	 * Objects & Constructions:
	 * - No standard physics object like ball, box, etc. These are made of 'composite objects' or Constructions
	 * - 'Constructions' are objects comprised of particles, constraints & colliders
	 *   - Must have at-least one particle
	 *   - Particles and constraints provide the 'shape'
	 *   - Colliders are 'linked' to a particle or constraint
	 * - 'Constraints' define how two or more particles interact
	 *   - Hard, keeps set distance between particles
	 *   - Soft, similar to hard except changes distance over time
	 *   - Axis, constrains particle x or y, vel pointing in constrained axis is transferred to unconstrained axis
	 *   - Fixed, sets velocity to 0 and keeps particle in position
	 *
	 * Colliders:
	 * - Ball collider
	 * - Line colliders:
	 *   - Can be fixed line that doesn't affect attached particle or constraint (might be redundant because of fixed/axis constraints)
	 *   - If attached to constraint, (eg. line) affects linked particle(s) proportional to point of contact
	 *
	 * Forces:
	 * - 'Force Providers' provide either constant or other forces (eg. constant, push, pull)
	 * - Handled by engine and not constructions
	 *
	 * Engine:
	 * - Updates constructions and forces
	 *
	 * Check List:
	 * [O] Particle object (pos, vel, radius, etc)
	 * [O] Particle object (pos, vel, radius, etc)
	 * [O] Gravity
	 * [O] Line-Ball collision
	 * [X] Ball-Ball collision
	 * [X] Hard constraint
	 * [X] Soft constraint
	 */
	
	private final List<Particle> particles = new ArrayList<>();
	private final List<Line> lines = new ArrayList<>();
	
	// Options
	private final Vector2f forceGravity = new Vector2f(0.f, -98.1f);
	private float nudgeDampener = .1f;
	
	@Override
	public void init(float worldView)
	{
		Particle particle = new Particle(new Vector2f(0.f), 5.f);
		particle.velocity.add(0.f, 100.f);
		this.particles.add(particle);
		
		this.lines.add(new Line(new Vector2f(worldView * .9f, worldView * -.9f), new Vector2f(worldView * -.9f)));
	}
	
	@Override
	public void update(float deltaTime)
	{
	
	}
	
	@Override
	public void fixedUpdate(float deltaTime)
	{
		for (int i = 0; i < this.particles.size(); i++)
		{
			Particle particle = this.particles.get(i);
			particle.velocity.add(this.forceGravity.mul(deltaTime, new Vector2f()));
			
			for (Line line : this.lines)
			{
				// Get particle position local to line
				Vector2f local = particle.position.sub(line.p1, new Vector2f());
				final float distAlongLine = local.dot(line.getTangent());
				
				// Default to along the line
				Vector2f normal = line.getNormal();
				float distAwayFromLine = local.dot(normal);
				if (distAwayFromLine < 0.f)
					normal.negate();
				distAwayFromLine = Math.abs(distAwayFromLine);
				
				// Check if ball is colliding with line end
				if (distAlongLine < 0.f || distAlongLine > line.getLength())
				{
					// Check what end the particle is at
					if (distAlongLine > 0.f)
					{
						particle.position.sub(line.p2, local);
						local.normalize(normal);
					} else
						particle.position.sub(line.p1, normal).normalize();
					distAwayFromLine = Math.abs(local.dot(normal));
				}
				
				if (distAwayFromLine < particle.radius)
				{
					// Nudge particle back to correct spot
					final Vector2f nudge = normal.mul((distAwayFromLine - (particle.radius + 1.f)) * .5f, new Vector2f());
					particle.position.sub(nudge.mul(this.nudgeDampener));
					
					// Calculate new velocity
					final float speedAlongNormal = particle.velocity.dot(normal);
					final float speedAlongTangent = particle.velocity.dot(new Vector2f(normal).perpendicular());
					if (speedAlongNormal <= 0.f)
					{
						particle.velocity.x = -(speedAlongNormal * normal.x) * particle.restitution
								+ speedAlongTangent * normal.y * (1.f - particle.friction);
						particle.velocity.y = -(speedAlongNormal * normal.y) * particle.restitution
								+ speedAlongTangent * -normal.x * (1.f - particle.friction);
					}
				}
			}
		}
		
		for (Particle particle : this.particles)
			particle.position.add(particle.velocity.mul(deltaTime, new Vector2f()));
	}
	
	@Override
	public void render(ShapeRenderer shapeRenderer, LineRenderer lineRenderer)
	{
		for (Line line : this.lines)
			line.render(shapeRenderer, lineRenderer);
		for (Particle particle : this.particles)
			particle.render(shapeRenderer, lineRenderer);
	}
	
	@Override
	public void destroy()
	{
	
	}
	
	@Override
	public void gui(float halfWidth)
	{
		ImGui.text(String.format("Particles: %d", this.particles.size()));
		
		ImGui.text("Gravity:");
		float[] gravity = new float[] {this.forceGravity.x, this.forceGravity.y};
		if (ImGui.inputFloat2("##gravity", gravity, "%.2f"))
		{
			this.forceGravity.x = gravity[0];
			this.forceGravity.y = gravity[1];
		}
		
		ImGui.text("Collision nudge dampener:");
		ImFloat dampener = new ImFloat(this.nudgeDampener);
		if (ImGui.inputFloat("##nudgeDampener", dampener, .05f, .1f, "%.2f"))
			this.nudgeDampener = Math.abs(dampener.get());
	}
}
