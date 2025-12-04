package io.github.coffeecatrailway.catbox.boxes;

import imgui.ImGui;
import imgui.type.ImFloat;
import io.github.coffeecatrailway.engine.physics.constraint.Constraint;
import io.github.coffeecatrailway.engine.physics.constraint.SpringConstraint;
import io.github.coffeecatrailway.engine.physics.object.Line;
import io.github.coffeecatrailway.engine.physics.object.Particle;
import io.github.coffeecatrailway.engine.renderer.LineRenderer;
import io.github.coffeecatrailway.engine.renderer.ShapeRenderer;
import org.joml.Math;
import org.joml.Random;
import org.joml.Vector2f;
import org.joml.Vector3f;

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
	 * [O] Line thickness - why was this easy?
	 * [O] Ball-Ball collision
	 * [X] Hard constraint
	 * [X] Soft constraint
	 * [X] Abstract away collision to component system for 'physical constraints'
	 */
	
	private final List<Particle> particles = new ArrayList<>();
	private final List<Line> lines = new ArrayList<>();
	private final List<Constraint> constraints = new ArrayList<>();
	
	// Options
	private final Vector2f forceGravity = new Vector2f(0.f, -98.1f);
	private float nudgeDampener = .1f;
	private final int steps = 20;
	
	@Override
	public void init(float worldView)
	{
//		Particle prt1 = new Particle(new Vector2f(-10.f, 0.f), 5.f);
//		prt1.velocity.add(100.f, 100.f);
//		this.particles.add(prt1);
//		Particle prt2 = new Particle(new Vector2f(10.f, 0.f), 5.f);
//		prt2.velocity.add(-100.f, 100.f);
//		this.particles.add(prt2);
		
		Random rand = new Random(0L);
		
		float f = worldView * .8f;
		for (int i = 0; i < 200; i++)
		{
			final float x = rand.nextFloat() * f * 2.f - f;
			final float y = rand.nextFloat() * f * 2.f - f;
			final float v = 200.f;
			final float vx = rand.nextFloat() * v - v * .5f;
			final float vy = rand.nextFloat() * v - v * .5f;
			
			final float r = rand.nextFloat();
			final float g = rand.nextFloat();
			final float b = rand.nextFloat();
			
			final float radius = rand.nextFloat() * 2.5f + 2.5f;
			this.particles.add(new Particle(new Vector2f(x, y), new Vector2f(vx, vy), new Vector3f(r, g, b), radius, .003f, 1.f));
		}
		
//		this.lines.add(new Line(this.particles.get(0).position, this.particles.get(1).position));
		
		f = worldView * .9f;
		this.lines.add(new Line(new Vector2f(-f), new Vector2f(f, -f), 1.25f));
		this.lines.add(new Line(new Vector2f(f, -f), new Vector2f(f), 2.5f));
		this.lines.add(new Line(new Vector2f(f), new Vector2f(-f, f), 3.75f));
		this.lines.add(new Line(new Vector2f(-f, f), new Vector2f(-f), 5.f));
		
//		this.constraints.add(new SpringConstraint(this.particles.get(0), this.particles.get(1), 40.f, 100.f, 2.f));
//		this.constraints.add(new SpringConstraint(this.particles.get(1), this.particles.get(2), 40.f, 100.f, 10.f));
//		this.constraints.add(new SpringConstraint(this.particles.get(2), this.particles.get(0), 40.f, 100.f, 10.f));
	}
	
	@Override
	public void update(float deltaTime)
	{
	
	}
	
	@Override
	public void fixedUpdate(float deltaTime)
	{
		// Velocity intergration
		for (Particle particle : this.particles)
		{
			particle.velocity.x += this.forceGravity.x * deltaTime;
			particle.velocity.y += this.forceGravity.y * deltaTime;
//		}
//
//		for (Particle particle : this.particles)
//		{
			particle.position.x += particle.velocity.x * deltaTime;
			particle.position.y += particle.velocity.y * deltaTime;
		}
		
		// Collision resolution
		float deltaTimeStep = deltaTime * (1.f / (float) this.steps);
		for (int i = 0; i < this.steps; i++)
		{
			for (int j = 0; j < this.particles.size(); j++)
			{
				Particle prt1 = this.particles.get(j);
				for (int k = j + 1; k < this.particles.size(); k++)
				{
					Particle prt2 = this.particles.get(k);
					Vector2f normal = prt2.position.sub(prt1.position, new Vector2f());
					final float dist = normal.length();
					normal.normalize();
					if (dist < prt1.radius + prt2.radius)
					{
						// Nudge particle back to correct spots
						final Vector2f nudge = normal.mul((dist - (prt1.radius + prt2.radius)) * .5f, new Vector2f()).mul(this.nudgeDampener);
						prt1.position.add(nudge);
						prt2.position.sub(nudge);

						Vector2f relVelocity = prt2.velocity.sub(prt1.velocity, new Vector2f());
						final float relSpeedAlongNormal = relVelocity.dot(normal);
						final float relSpeedAlongTangent = relVelocity.dot(new Vector2f(normal).perpendicular());

						// Sliding friction TODO: friction and restitution of both particles
						Vector2f force = new Vector2f();
						force.x = relSpeedAlongTangent * .5f * normal.y * ((prt1.friction + prt2.friction) * .5f);
						force.y = relSpeedAlongTangent * .5f * -normal.x * ((prt1.friction + prt2.friction) * .5f);

						// Elastic collision
						force.add(normal.mul(relSpeedAlongNormal * ((prt1.restitution + prt2.restitution) * .5f), new Vector2f()));
						// Inelastic collision
						force.add(normal.mul(relSpeedAlongNormal * .5f * (1.f - (prt1.restitution + prt2.restitution) * .5f), new Vector2f()));

						prt1.velocity.add(force);
						prt2.velocity.sub(force);
					}
				}

				for (Line line : this.lines)
				{
					// Get particle position local to line
					Vector2f local = prt1.position.sub(line.p1, new Vector2f());
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
							prt1.position.sub(line.p2, local);
							local.normalize(normal);
						} else
							prt1.position.sub(line.p1, normal).normalize();
						distAwayFromLine = Math.abs(local.dot(normal));
					}

					if (distAwayFromLine < line.thickness * .5f + prt1.radius)
					{
						// Nudge particle back to correct spot
						final Vector2f nudge = normal.mul((distAwayFromLine - (prt1.radius + line.thickness * .5f)) * .5f, new Vector2f()).mul(this.nudgeDampener);
						prt1.position.sub(nudge);

						// Calculate new velocity
						final float speedAlongNormal = prt1.velocity.dot(normal);
						final float speedAlongTangent = prt1.velocity.dot(new Vector2f(normal).perpendicular());
						if (speedAlongNormal <= 0.f)
						{
							prt1.velocity.x = -(speedAlongNormal * normal.x) * prt1.restitution
									+ speedAlongTangent * normal.y * (1.f - prt1.friction);
							prt1.velocity.y = -(speedAlongNormal * normal.y) * prt1.restitution
									+ speedAlongTangent * -normal.x * (1.f - prt1.friction);
						}
					}
				}
			}
		}
		
//		for (Constraint constraint : this.constraints)
//			constraint.update(deltaTime);
	}
	
	@Override
	public void render(ShapeRenderer shapeRenderer, LineRenderer lineRenderer)
	{
		for (Constraint constraint : this.constraints)
			constraint.render(shapeRenderer, lineRenderer);
		
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
		ImGui.text(String.format("Physics steps: %d", this.steps));
		ImGui.separator();
		
		ImGui.pushItemWidth(halfWidth * 1.5f);
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
		
		ImGui.popItemWidth();
	}
}
