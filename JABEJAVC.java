package partitioning;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JABEJAVC {
	public Partition[] partitionList;
	// public Set<Vertex> vertexinC;
	// public Set<Edge> edgelist;
	public int pNum;
	public int vertexNum;
	public int edgeNum;
	public Random random;
	public int maxSize;
	public int minSize;
	public String data;
	public String edgePath;
	public Vertex[] aVertexs;
	public String exchangeHeuristic;
	public float T0;
	public float d;
	public float Tr;
	public int swap;
	public long fswap;
	public float RF;
	private CyclicBarrier cb1;

	// BufferedWriter out[];

	public JABEJAVC(int pNum, int vNum, String edgePath, String data, String heuristic, float T0, float d)
			throws IOException {
		// TODO Auto-generated constructor stub
		this.pNum = pNum;
		this.partitionList = new Partition[pNum];
		for (int i = 0; i < pNum; i++) {
			this.partitionList[i] = new Partition(i);
		}
		// this.vertexinC = new HashSet<Vertex>();
		// this.edgelist=new HashSet<Edge>();
		this.edgeNum = 0;
		this.vertexNum = vNum;
		this.aVertexs = new Vertex[vertexNum];
		this.random = new Random();
		this.maxSize = 0;
		this.minSize = 0;
		this.data = data;
		this.edgePath = edgePath;
		// this.out = new BufferedWriter[pNum];
		this.exchangeHeuristic = heuristic;
		this.T0 = T0;
		this.d = d;
		this.Tr = T0;
		this.swap = 0;
		this.fswap = 0;
		load();
	}

	public void load() throws IOException {
		// String str1;
		BufferedReader in1;
	
		for (int i = 0; i < pNum; i++) {
			String fileName = "partition" + i + ".txt";
			in1 = new BufferedReader(new FileReader(new File(edgePath + "\\" + fileName)));// 初始边
			String string;
			while ((string = in1.readLine()) != null) {
				edgeNum++;
				String[] vertexs = string.split(" ");
				Vertex vs = aVertexs[Integer.parseInt(vertexs[0]) - 1] == null
						? new Vertex(Integer.parseInt(vertexs[0]))
						: aVertexs[Integer.parseInt(vertexs[0]) - 1];
				Vertex vt = aVertexs[Integer.parseInt(vertexs[1]) - 1] == null
						? new Vertex(Integer.parseInt(vertexs[1]))
						: aVertexs[Integer.parseInt(vertexs[1]) - 1];
				aVertexs[Integer.parseInt(vertexs[0]) - 1] = vs;
				aVertexs[Integer.parseInt(vertexs[1]) - 1] = vt;
				// vs.neighbors.add(vt);
				// vt.neighbors.add(vs);
				Edge edge = new Edge(vs, vt);
				// edgelist.add(edge);
				int p = i;
				// partitionList[p].edgesinP.add(edge);
				edge.belong = p;
				edge.belong0=p;
				vs.edges.add(edge);
				if(!vs.belongs.contains(p)) {
					vs.belongs.add(p);
				}
				vt.edges.add(edge);
				if(!vt.belongs.contains(p)) {
					vt.belongs.add(p);
				}
				partitionList[p].Vertexs.add(vs);
				partitionList[p].Vertexs.add(vt);

			}
			in1.close();
		}
		vertexNum=0;
		for (Vertex vertex : aVertexs) {
			if(vertex!=null) {
				vertexNum++;
				int size = vertex.belongs.size();
				if (size == 1) {
					vertex.ins = true;
					/*
					 * for (Integer i : vertex.belongs) { Partition partition = partitionList[i];
					 * partition.internals.add(vertex);
					 * 
					 * }
					 */
				} else if (size > 1) {
					vertex.ins = false;
					/*
					 * for (Integer i : vertex.belongs) { Partition partition = partitionList[i];
					 * partition.externals.add(vertex); }
					 */
				} else {
					System.out.println("wrong!!!");
				}
			}
		}
	}

	public int partitioning() {

		int count = 0;
		ExecutorService pool = Executors.newCachedThreadPool();
		cb1 = new CyclicBarrier(pNum + 1);
		while (Tr > 1) {
			Tr = Math.max(1, T0 - count * d);

			for (Partition partition : partitionList) {
				pool.execute(new repartitionThread(partition));
			}
			try {
				cb1.await();
			} catch (InterruptedException | BrokenBarrierException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// System.out.println(count++);
			count++;
		}
		float total = 0;
		int migration=0;
		for (Vertex vertex : aVertexs) {
			if(vertex!=null) {
				total += vertex.belongs.size();
				for (Edge edge : vertex.edges) {
					if(edge.belong!=edge.belong0) {
						migration++;
					}
				}
			}
			
		}
		System.out.println("iteration " + count + ": RF=" + total / vertexNum);
		System.out.println("migraton: "+migration/2);
		return count;
	}

	class repartitionThread extends Thread {
		private Partition p;

		public repartitionThread(Partition p) {
			// TODO Auto-generated constructor stub
			this.p = p;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			for (Vertex vertex : p.Vertexs) {
				compute(vertex);
			}
			try {
				cb1.await();
			} catch (InterruptedException | BrokenBarrierException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void compute(Vertex vertex) {
		if (vertex.ins == false) {
			// Edge Selection
			Edge selectedEdge = selectEdge(vertex);
			if(selectedEdge==null) {
				return;
			}
			// Partner Selection
			List<Vertex> candidates = new ArrayList<Vertex>(4);
			if (vertex.edges.size() <= 3) {
				for (Edge edge : vertex.edges) {
					Vertex neighbor = edge.v1.equals(vertex) ? edge.v2 : edge.v1;
					candidates.add(neighbor);
				}
			} else {
				List<Integer> temp = new ArrayList<Integer>();
				int size = vertex.edges.size();
				while (temp.size() < 3) {
					int randomNum = random.nextInt(size);
					if (!temp.contains(randomNum)) {
						temp.add(randomNum);
					}
				}
				for (int i = 0; i < 3; i++) {
					Edge thisedge = vertex.edges.get(temp.get(i));
					candidates.add(thisedge.v1.equals(vertex) ? thisedge.v2 : thisedge.v1);
				}
			}
			while (candidates.size() < 4) {
				int randomV = random.nextInt(vertexNum);
				while(aVertexs[randomV]==null) {
					randomV = random.nextInt(vertexNum);
				}
				if (!candidates.contains(aVertexs[randomV]) && !aVertexs[randomV].equals(vertex)) {
					candidates.add(aVertexs[randomV]);
				}
			}
			//
			if (exchangeHeuristic == "DC") {

			} else if (exchangeHeuristic == "EC") {
				for (Vertex partener : candidates) {
					if (partener.ins == false) {
						Edge pEdge = selectEdge(partener);
						if(pEdge==null) {
							continue;
						}
						float value_edgeself = calValue(selectedEdge, selectedEdge.belong);
						float value_edgepartener = calValue(selectedEdge, pEdge.belong);
						float value_partenerself = calValue(pEdge, pEdge.belong);
						float value_parteneredge = calValue(pEdge, selectedEdge.belong);
						float utility = (value_edgepartener + value_parteneredge) * Tr
								- (value_edgeself + value_partenerself);
						if (utility > 0 && selectedEdge.belong != pEdge.belong) {
							swap++;
							swapColor(selectedEdge, pEdge);
						} else {
							fswap++;
						}
					}
				}
			} else {

			}
		}
	}

	public Edge selectEdge(Vertex vertex) {
		int min = Integer.MAX_VALUE;
		int minp = -1;
		Map<Integer, Integer> adjDistribution = new HashMap<Integer, Integer>();
		if(vertex.edges.size()==0) {
			return null;
		}
		for (Edge edge : vertex.edges) {
			//System.out.print(edge.belong+";");
			if(adjDistribution.keySet().contains(edge.belong)) {
				//System.out.println(edge.belong);
				int newNum=adjDistribution.get(edge.belong)+1;
				adjDistribution.put(edge.belong, newNum);
			}else {
				adjDistribution.put(edge.belong, 1);
			}
		}
		//System.out.println();
		/*
		 * System.out.println(vertex.id); for (int i : vertex.belongs) {
		 * System.out.print(i+" "); } System.out.println();
		 */
		/*for (int k=0;k<vertex.belongs.size();k++) {
			if(k>vertex.belongs.size()-1) {
				break;
			}
			int i=vertex.belongs.get(k);
			int size = adjDistribution[i];
			if (size < min && size != 0) {
				min = size;
				minp = i;
			}
		}*/
		for (Iterator<Map.Entry<Integer, Integer>> it=adjDistribution.entrySet().iterator();it.hasNext();) {
			Map.Entry<Integer, Integer> entry=it.next();
			if(entry.getValue()<min) {
				min=entry.getValue();
				minp=entry.getKey();
			}
		}
		Edge result = null;
		for (Edge edge : vertex.edges) {
			//System.out.print(edge.belong+" "+minp+";");
			if (edge.belong == minp) {
				result = edge;
				break;
			}
		}
		//System.out.println();
		/*if(result==null) {
			for (Iterator<Map.Entry<Integer, Integer>> it=adjDistribution.entrySet().iterator();it.hasNext();) {
				Map.Entry<Integer, Integer> entry=it.next();
				System.out.println(entry.getKey()+"->"+entry.getValue());
			}
			System.out.println(minp);
			for (Edge edge : vertex.edges) {
				System.out.print(edge.belong+";");
			}
			System.out.println();
		}*/
		return result;
		// System.out.println(vertex.id+";"+vertex.belongs.size()+";"+min+";"+minp);
	}

	public float calValue(Edge edge, int belong) {
		float value = 0;
		Vertex[] vertexs = { edge.v1, edge.v2 };
		for (Vertex vertex : vertexs) {
			int count = 0;
			for (Edge adj : vertex.edges) {
				if (adj.belong == belong) {
					count++;
				}
			}
			if (belong == edge.belong) {
				value += (count - 1) * 1.0 / vertex.edges.size();
			} else {
				value += count * 1.0 / vertex.edges.size();
			}
		}
		return value;
	}

	public void swapColor(Edge edge1, Edge edge2) {
		operationForVertex(edge1.v1, edge1.belong, edge2.belong);
		operationForVertex(edge1.v2, edge1.belong, edge2.belong);
		operationForVertex(edge2.v1, edge2.belong, edge1.belong);
		operationForVertex(edge2.v2, edge2.belong, edge1.belong);
		int temp = edge1.belong;
		edge1.belong = edge2.belong;
		edge2.belong = temp;
	}

	public void operationForVertex(Vertex vertex, int prebelong, int nowbelong) {
		if(!vertex.belongs.contains(nowbelong)) {
			vertex.belongs.add(nowbelong);
		}
		int preNeighNum = 0;
		for (Edge edge : vertex.edges) {
			if (edge.belong == prebelong) {
				preNeighNum++;
			}
		}
		if (preNeighNum == 1) {
			vertex.belongs.remove((Integer)prebelong);
		}

		if (vertex.belongs.size() == 1) {
			vertex.ins = true;
		} else {
			vertex.ins = false;
		}
	}

	/*
	 * public void output() throws IOException { for (Partition partition :
	 * partitionList) { for (Edge edge : partition.edgesinP) { if (edge.v1.id <
	 * edge.v2.id) { out[partition.id].write(edge.v1.id + " " + edge.v2.id + "\n");
	 * } else { out[partition.id].write(edge.v2.id + " " + edge.v1.id + "\n"); } }
	 * out[partition.id].close(); } }
	 */

	public void statistics() {
		RF = 0;
		for (Vertex vertex : aVertexs) {
			if(vertex!=null) {
				RF += vertex.belongs.size();
			}
		}
		RF /= vertexNum;
		System.out.println("RF:" + RF);

	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String[] datas = { "dblp", "roadNet-PA", "youtube", "lj", "orkut" };
		int vNum = 0;// 点数量317080,1088092,1134890,3997962,7600000,3072441
		int[] ps = { /*2 , 4, 8,*/ 16 };
		for (String data : datas) {
			switch (data) {
			case "dblp":
				vNum = 317080;
				break;
			case "roadNet-PA":
				vNum = 1088092;
				break;
			case "youtube":
				vNum = 1134890;
				break;
			case "lj":
				vNum = 3997962;
				break;
			case "sp":
				vNum = 7600000;
				break;
			case "orkut":
				vNum = 3072441;
				break;
			default:
				break;
			}
			for (int pNum : ps) {
				// String edgePath = "D:\\dataset\\greedy\\" + data + "2-RDM.txt";
				String iEP = "";
				String[] initialMehods= {"Ne"/*,"DBH"*/};
				String[] trackers = { "RBSEP"/*,"random"*/ };
				for (String initial : initialMehods) {
					for (String streaming : trackers) {
						
						iEP = "D:\\dataset\\Initial+dynamic\\" + initial+"+"+streaming + "\\" + data + "2-partition-" + pNum;
						String heuristic = "EC";
						float T0 = 2;
						float d = 0.01f;
						JABEJAVC ja = new JABEJAVC(pNum, vNum, iEP, data, heuristic, T0, d);
						System.out.println("--------" + data + ":" + pNum + "=" + initial+"+"+streaming+"------------");
						ja.statistics();
						long t1 = System.currentTimeMillis();
						System.out.println("iterations:" + ja.partitioning());
						long t2 = System.currentTimeMillis();
						// ja.statistics();
						System.out.println("swap:" + ja.swap + ";fswap:" + ja.fswap);
						System.out.println("time:" + (t2 - t1) * 1.0 / 1000);
						// ja.output();
					}
				}
			}
		}
	}

}
