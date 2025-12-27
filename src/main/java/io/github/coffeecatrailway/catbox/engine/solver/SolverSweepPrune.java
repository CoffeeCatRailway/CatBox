package io.github.coffeecatrailway.catbox.engine.solver;

import io.github.coffeecatrailway.catbox.core.Pair;
import io.github.coffeecatrailway.catbox.engine.object.LineObject;
import io.github.coffeecatrailway.catbox.engine.object.VerletObject;

import java.util.ArrayList;
import java.util.Objects;

public class SolverSweepPrune extends SolverSimple
{
	private final ArrayList<Edge> edges = new ArrayList<>();
	
	public SolverSweepPrune(float worldWidth, float worldHeight, int subSteps)
	{
		super("Sweep & Prune", worldWidth, worldHeight, subSteps);
	}
	
	@Override
	protected void sortObjects()
	{
//		double then = GLFW.glfwGetTime();
//		this.edges.sort((e1, e2) -> Float.compare(e1.getEdgeX(), e2.getEdgeX()));
//		System.out.println(this.edges.stream().map(e -> e.isLeft ? "L" :"R").collect(Collectors.joining()));
		
		// Insertion sort
		for (int i = 1; i < this.edges.size(); i++)
		{
			for (int j = i - 1; j >= 0; j--)
			{
				if (this.edges.get(j).getEdgeX() < this.edges.get(j + 1).getEdgeX())
					break;

				// swap [edges[j], edges[j + 1]] = [edges[j + 1], edges[j]]
				Edge edge = this.edges.get(j);
				this.edges.set(j, this.edges.get(j + 1));
				this.edges.set(j + 1, edge);
			}
		}
//		double now = GLFW.glfwGetTime();
//		System.out.printf("Edge sort took %fμs\n", (now - then) * 1_000_000);
	}
	
	@Override
	protected void handleCollisions(float dt)
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
	
	ArrayList<Pair<VerletObject, VerletObject>> overlapping = new ArrayList<>();
	@Override
	protected void step(float stepDt)
	{
//		this.sortObjects();
		
//		double then = GLFW.glfwGetTime();
//		ArrayList<VerletObject> touching = new ArrayList<>();
//		for (Edge edge : this.edges)
//		{
//			if (edge.isLeft)
//			{
////				System.out.println(touching.size());
//				for (VerletObject other : touching)
//					this.solveObjectObjectContact(other, edge.object);
//				touching.add(edge.object);
//			} else
//				touching.remove(edge.object);
//		}
//		double now = GLFW.glfwGetTime();
//		System.out.printf("Sweep took %fms\n", (now - then) * 1_000);
		
		for (int i = 1; i < this.edges.size(); i++)
		{
			for (int j = i - 1; j >= 0; j--)
			{
				Edge edge1 = this.edges.get(j);
				Edge edge2 = this.edges.get(j + 1);
				if (edge1.getEdgeX() < edge2.getEdgeX())
					break;
				
				this.edges.set(j, edge2);
				this.edges.set(j + 1, edge1);
				
				edge1 = this.edges.get(j);
				edge2 = this.edges.get(j + 1);
				
				Pair<VerletObject, VerletObject> pair = new Pair<>(edge1.object, edge2.object);
				if (edge1.isLeft && !edge2.isLeft)
					overlapping.add(pair);
				else if (!edge1.isLeft && edge2.isLeft)
					overlapping.remove(pair);
			}
		}
		
		for (Pair<VerletObject, VerletObject> pair : overlapping)
			this.solveObjectObjectContact(pair.first(), pair.second());
		
//		this.handleCollisions(stepDt);
//		for (Constraint constraint : this.constraints)
//			constraint.update(stepDt);
		
		this.updateObjects(stepDt);
	}
	
//	@Override
//	public void gui(float windowWidth)
//	{
//		super.gui(windowWidth);
//		ImGui.text(String.format("%d %d", this.edges.size(), this.edges.size() / Math.max(this.objects.size(), 1)));
//	}
	
	@Override
	public boolean addObject(VerletObject obj)
	{
		this.edges.add(new Edge(obj, true));
		this.edges.add(new Edge(obj, false));
		return super.addObject(obj);
	}
	
	private record Edge(VerletObject object, boolean isLeft)
	{
		public float getEdgeX()
		{
			return this.object.position.x + (this.isLeft ? -this.object.radius : this.object.radius);
		}
		
		@Override
		public boolean equals(Object o)
		{
			if (o == null || getClass() != o.getClass()) return false;
			Edge that = (Edge) o;
			return this.isLeft == that.isLeft && this.object.equals(that.object);
		}
		
		@Override
		public int hashCode()
		{
			return Objects.hash(this.object, this.isLeft);
		}
	}
}
