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

public class Force2CatBox implements CatBoxI
{
	private final List<Particle> particles = new ArrayList<>();
	private final List<Line> lines = new ArrayList<>();
	private final List<Constraint> constraints = new ArrayList<>();
	
	// Options
	private final Vector2f forceGravity = new Vector2f(0.f, -98.1f);
	private float elasticity = .5f, friction = 10.f;
	
	@Override
	public void init(float worldView)
	{
		float f = worldView * .8f;
//		Random rand = new Random(0L);
//		for (int i = 0; i < 100; i++)
//		{
//			final float x = rand.nextFloat() * f * 2.f - f;
//			final float y = rand.nextFloat() * f * 2.f - f;
//			final float v = 200.f;
//			final float vx = rand.nextFloat() * v - v * .5f;
//			final float vy = rand.nextFloat() * v - v * .5f;
//
//			final float r = rand.nextFloat();
//			final float g = rand.nextFloat();
//			final float b = rand.nextFloat();
//
//			final float radius = (rand.nextFloat() * .6f + .4f) * 5.f;
//			this.particles.add(new Particle(new Vector2f(x, y), new Vector2f(vx, vy), new Vector3f(r, g, b), radius, .003f, .9f));
//		}
		
		Particle p1 = new Particle(new Vector2f(-10.f, 0.f), new Vector2f(50.f, 0.f), new Vector3f(1.f), 5.f, 0.f, 0.f);
		Particle p3 = new Particle(new Vector2f(-10.f, 20.f), new Vector2f(50.f, 0.f), new Vector3f(1.f), 5.f, 0.f, 0.f);
		this.particles.add(p1);
		this.particles.add(new Particle(new Vector2f(10.f, 0.f), new Vector2f(-50.f, 0.f), new Vector3f(1.f), 3.75f, 0.f, 0.f));
		this.particles.add(p3);
		this.particles.add(new Particle(new Vector2f(10.f, 20.f), new Vector2f(-50.f, 0.f), new Vector3f(1.f), 2.5f, 0.f, 0.f));

		this.constraints.add(new SpringConstraint(p1, p3, 100.f, 10.f));
		
		f = worldView * .95f;
		this.lines.add(new Line(new Vector2f(-f, -f), new Vector2f(f, -f), 5.f));
		this.lines.add(new Line(new Vector2f(f, -f), new Vector2f(f, f), 5.f));
		this.lines.add(new Line(new Vector2f(f, f), new Vector2f(-f, f), 5.f));
		this.lines.add(new Line(new Vector2f(-f, f), new Vector2f(-f, -f), 5.f));
		
		p1 = new Particle(new Vector2f(-10.f, 50.f), new Vector2f(), new Vector3f(1.f, 0.f, 0.f), 2.5f, 0.f, 0.f);
		Particle p2 = new Particle(new Vector2f(10.f, 50.f), new Vector2f(), new Vector3f(0.f, 1.f, 0.f), 2.5f, 0.f, 0.f);
		p3 = new Particle(new Vector2f(10.f, 30.f), new Vector2f(), new Vector3f(0.f, 0.f, 1.f), 2.5f, 0.f, 0.f);
		Particle p4 = new Particle(new Vector2f(-10.f, 30.f), new Vector2f(), new Vector3f(1.f, 1.f, 1.f), 2.5f, 0.f, 0.f);
		this.particles.addAll(List.of(p1, p2, p3, p4));
		float force = 100.f, damping = 10.f;
		this.constraints.add(new SpringConstraint(p1, p2, force, damping));
		this.constraints.add(new SpringConstraint(p2, p3, force, damping));
		this.constraints.add(new SpringConstraint(p3, p4, force, damping));
		this.constraints.add(new SpringConstraint(p4, p1, force, damping));
		this.constraints.add(new SpringConstraint(p1, p3, force, damping));
		this.constraints.add(new SpringConstraint(p2, p4, force, damping));
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
			
			particle.position.x += particle.velocity.x * deltaTime;
			particle.position.y += particle.velocity.y * deltaTime;
		}
		
		// Collision resolution
		for (int i = 0; i < this.particles.size(); i++)
		{
			Particle particle1 = this.particles.get(i);
			// Particle-Line
			for (Line line : this.lines)
			{
				// Get particle position local to line
				Vector2f local = particle1.position.sub(line.p1, new Vector2f());
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
						particle1.position.sub(line.p2, local);
						local.normalize(normal);
					} else
						particle1.position.sub(line.p1, normal).normalize();
					distAwayFromLine = Math.abs(local.dot(normal));
				}
				
				float depth = line.thickness * .5f + particle1.radius - distAwayFromLine;
				if (depth > 0.f)
				{
					// resolve constraint
					particle1.position.add(normal.mul(depth, new Vector2f()));
	
					// compute normal and tangent velocity
					Vector2f velAlongNormal = normal.mul(normal.dot(particle1.velocity), new Vector2f());
					Vector2f velAlongTangent = particle1.velocity.sub(velAlongNormal, new Vector2f());
	
					// elasticity
					velAlongNormal.mul(-this.elasticity);
	
					// friction
					velAlongTangent.mul((float) Math.exp(-this.friction * deltaTime));
	
					// new velocity
					velAlongNormal.add(velAlongTangent, particle1.velocity);
				}
			}
			
			// Particle-Particle
			for (int j = i + 1; j < this.particles.size(); j++)
			{
				Particle particle2 = this.particles.get(j);
				Vector2f delta = particle2.position.sub(particle1.position, new Vector2f());
				float depth = particle1.radius + particle2.radius - delta.length();
				if (depth > 0.f)
				{
					Vector2f normal = delta.normalize(new Vector2f());
					
					// Resolve constraint
					Vector2f displacement = normal.mul(depth * .5f, new Vector2f());
					particle1.position.sub(displacement);
					particle2.position.add(displacement);
					
					// Compute relative speeds
					Vector2f relVelocity = particle2.velocity.sub(particle1.velocity, new Vector2f());
					final float relSpeedAlongNormal = relVelocity.dot(normal);
					final float relSpeedAlongTangent = relVelocity.dot(new Vector2f(normal).perpendicular());
					
					// New direction and friction
					Vector2f force = new Vector2f();
					force.x = relSpeedAlongTangent * .5f * normal.y;
					force.y = relSpeedAlongTangent * .5f * -normal.x;
					force.mul((float) Math.exp(-this.friction * deltaTime));
					
					// Elastic collision
					force.add(normal.mul(relSpeedAlongNormal * this.elasticity, new Vector2f()));
					// Inelastic collision
					force.add(normal.mul(relSpeedAlongNormal * .5f * (1.f - this.elasticity), new Vector2f()));
					
//					final float massRatio1 = particle1.radius / particle2.radius;
//					final float massRatio2 = particle2.radius / particle1.radius;
//
//					particle1.velocity.add(force.mul(massRatio2, new Vector2f()));
//					particle2.velocity.sub(force.mul(massRatio1, new Vector2f()));
					
					particle1.velocity.add(force);
					particle2.velocity.sub(force);
				}
			}
		}

		for (Constraint constraint : this.constraints)
			constraint.update(deltaTime);
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
//		ImGui.text(String.format("Physics steps: %d", this.steps));
		ImGui.separator();
		
		ImGui.pushItemWidth(halfWidth);
		ImGui.text("Gravity:");
		float[] gravity = new float[] {this.forceGravity.x, this.forceGravity.y};
		if (ImGui.inputFloat2("##gravity", gravity, "%.2f"))
		{
			this.forceGravity.x = gravity[0];
			this.forceGravity.y = gravity[1];
		}
		
		ImFloat imFloat = new ImFloat(this.elasticity);
		if (ImGui.inputFloat("Elasticity", imFloat))
			this.elasticity = imFloat.floatValue();
		
		imFloat = new ImFloat(this.friction);
		if (ImGui.inputFloat("Friction", imFloat))
			this.friction = imFloat.floatValue();
		
//		ImGui.text("Collision nudge dampener:");
//		ImFloat dampener = new ImFloat(this.nudgeDampener);
//		if (ImGui.inputFloat("##nudgeDampener", dampener, .05f, .1f, "%.2f"))
//			this.nudgeDampener = Math.abs(dampener.get());
		
		ImGui.popItemWidth();
	}
}
