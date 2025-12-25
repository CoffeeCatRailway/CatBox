package io.github.coffeecatrailway.catbox.engine.solver;

import io.github.coffeecatrailway.catbox.engine.object.LineObject;
import io.github.coffeecatrailway.catbox.engine.object.VerletObject;
import io.github.coffeecatrailway.catbox.engine.object.constraint.Constraint;

public class SolverSweepPruneSimple extends Solver
{
	public SolverSweepPruneSimple(float worldWidth, float worldHeight, int subSteps)
	{
		super("Sweep & Prune Simple", worldWidth, worldHeight, subSteps);
	}
	
	private void sortObjectsByLeft()
	{
		this.objects.sort((o1, o2) -> Float.compare(o1.position.x - o1.radius, o2.position.x - o2.radius)); // ~10μs
	}
	
	private void checkCollisions(float dt)
	{
		for (int i = 0; i < this.objects.size(); i++)
		{
			VerletObject obj1 = this.objects.get(i);
			// object-object
			for (int j = i + 1; j < this.objects.size(); j++)
			{
				VerletObject obj2 = this.objects.get(j);
				if ((obj2.position.x - obj2.radius) > (obj1.position.x + obj1.radius)) break; // obj2.left > obj1.right
				this.solveObjectObjectContact(obj1, obj2);
			}
			
			// object-line
			for (LineObject lineObj : this.lineObjects)
				this.solveObjectLineContact(obj1, lineObj);
		}
	}
	
	private void updateObjects(float dt)
	{
		for (VerletObject obj : this.objects)
		{
			obj.accelerate(this.gravity);
			obj.update(dt);
			this.applyWorldConstraint(obj);
		}
	}
	
	@Override
	public void step(float stepDt)
	{
		this.sortObjectsByLeft();
		this.checkCollisions(stepDt);
		
		for (Constraint constraint : this.constraints)
			constraint.update(stepDt);
		
		this.updateObjects(stepDt);
	}
}
