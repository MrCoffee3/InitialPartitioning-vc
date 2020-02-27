package partitioning;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

public class Vertex {
	int id;
	//Set<Vertex> neighbors;
	List<Edge> edges;
	List<Integer> belongs;
	int assignedneibors=0;
	int notassigned=0;
	boolean inc=false;
	boolean ins=false;
	public Vertex(int id) {
		// TODO Auto-generated constructor stub
		this.id=id;
		//this.neighbors=new HashSet<Vertex>();
		this.edges=new ArrayList<Edge>();
		this.belongs=new ArrayList<Integer>();
	}
}
