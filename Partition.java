package partitioning;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;


public class Partition {
	int id;
	int w;
	//Set<Vertex> externals;
	//Set<Vertex> internals;
	//Set<Edge> edgesinP;
	Set<Vertex> Vertexs;
	Queue<Vertex> S;
	List<Vertex> C;
	List<Edge> edges;
	public Partition(int id) {
		// TODO Auto-generated constructor stub
		this.id=id;
		//this.internals=new HashSet<Vertex>();
		//this.externals=new HashSet<Vertex>();
		this.C=new ArrayList<Vertex>();
		this.S=new PriorityQueue<Vertex>(new mycompare());
		this.edges=new ArrayList<Edge>();
		//this.edgesinP=new HashSet<Edge>();
		Vertexs=new HashSet<Vertex>();
		//bVertexs=new TreeSet<Vertex>(new myCompare1());
	}
	public class mycompare implements Comparator<Vertex>{

		@Override
		public int compare(Vertex v1, Vertex v2) {
			// TODO Auto-generated method stub
			if(v1.edges.size()-v1.assignedneibors<v2.edges.size()-v2.assignedneibors) {
				return -1;
			}else if(v1.edges.size()-v1.assignedneibors>v2.edges.size()-v2.assignedneibors){
				return 1;
			}else {
				return 0;
			}
		}
		
	}
}
