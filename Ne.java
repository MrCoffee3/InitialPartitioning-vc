package partitioning;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Ne {
	public Partition[] partitionList;
	public Set<Vertex> vertexList;
	// public Set<Vertex> vertexinC;
	// public Set<Edge> edgelist;
	public int pNum;
	public int vertexNum;
	public int edgeNum;
	public float balance;
	public Random random;
	public int maxSize;
	public int minSize;
	public String data;
	public String edgePath;
	BufferedWriter out[];

	public Ne(int pNum, float balance, int vNum, String edgePath, String data) throws IOException {
		// TODO Auto-generated constructor stub
		this.pNum = pNum;
		this.balance = balance;
		this.partitionList = new Partition[pNum];
		for (int i = 0; i < pNum; i++) {
			this.partitionList[i] = new Partition(i);
		}
		this.vertexList = new HashSet<Vertex>();
		// this.vertexinC = new HashSet<Vertex>();
		// this.edgelist=new HashSet<Edge>();
		this.edgeNum = 0;
		this.vertexNum = vNum;
		this.random = new Random();
		this.maxSize = 0;
		this.minSize = 0;
		this.data = data;
		this.edgePath = edgePath;
		this.out = new BufferedWriter[pNum];

		File file = new File("D:\\dataset\\Initial_partitioning\\Ne\\" + data + "2-partition-" + pNum + "\\");
		if (!file.exists()) {
			file.mkdir();
		}
		for (int i = 0; i < pNum; i++) {
			out[i] = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
					"D:\\dataset\\Initial_partitioning\\Ne\\" + data + "2-partition-" + pNum + "\\partition" + i + ".txt")));
		}
		load();
	}

	public void load() throws IOException {
		Vertex[] aVertexs = new Vertex[vertexNum];
		String str1;
		BufferedReader in1 = new BufferedReader(new FileReader(new File(edgePath)));// 边
		String[] vertexs;
		while ((str1 = in1.readLine()) != null) {
			edgeNum++;
			// System.out.println(str1);
			vertexs = str1.split(" ");
			Vertex vs = aVertexs[Integer.parseInt(vertexs[0]) - 1] == null ? new Vertex(Integer.parseInt(vertexs[0]))
					: aVertexs[Integer.parseInt(vertexs[0]) - 1];
			Vertex vt = aVertexs[Integer.parseInt(vertexs[1]) - 1] == null ? new Vertex(Integer.parseInt(vertexs[1]))
					: aVertexs[Integer.parseInt(vertexs[1]) - 1];
			aVertexs[Integer.parseInt(vertexs[0]) - 1] = vs;
			aVertexs[Integer.parseInt(vertexs[1]) - 1] = vt;
			// vs.neighbors.add(vt);
			// vt.neighbors.add(vs);
			Edge edge = new Edge(vs, vt);
			// edgelist.add(edge);
			vs.edges.add(edge);
			vt.edges.add(edge);
		}
		in1.close();
		vertexNum=0;
		for (Vertex vertex : aVertexs) {
			if (vertex != null) {
				//System.out.println("?");
				vertexNum++;
				vertexList.add(vertex);
				vertex.notassigned = vertex.edges.size();
			}
		}
		System.out.println(vertexList.size() + ";" + edgeNum);
	}

	public void partitioning() {
		float maxSize = balance * edgeNum / pNum;
		float minSize = (2 - balance) * edgeNum / pNum;
		int edges = 0;
		for (int i = 0; i < pNum; i++) {
			Partition partition = partitionList[i];
			while (partition.w < maxSize && edges < edgeNum) {
				// System.out.println(edges);
				Vertex vertex = null;
				if (partition.S.size() == 0) {
					vertex = vertexList.iterator().next();
				} else {
					vertex = partition.S.poll();
				}
				partition.C.add(vertex);
				vertex.inc = true;
				vertex.ins = false;
				vertexList.remove(vertex);
				// vertexinC.add(vertex);
				vertex.belongs.add(partition.id);
				for (Edge edge : vertex.edges) {
					if (edge.inp == false) {
						edge.belong = partition.id;
						edge.inp = true;
						partition.edges.add(edge);
						edges++;
						/*
						 * if(!edgelist.remove(edge)) { System.out.println("?"); }
						 */
						partition.w++;
						Vertex neibor = edge.v1.id == vertex.id ? edge.v2 : edge.v1;
						neibor.assignedneibors = 0;
						neibor.belongs.add(partition.id);
						for (Edge edge2 : neibor.edges) {
							Vertex vertex2 = edge2.v1.id == neibor.id ? edge2.v2 : edge2.v1;
							if (vertex2.ins == true && vertex2.belongs.contains(i) && edge2.inp == false) {
								neibor.assignedneibors++;
								neibor.notassigned--;
								if (neibor.notassigned == 0) {
									vertexList.remove(neibor);
									// vertexinC.add(neibor);
								}
								partition.edges.add(edge2);
								edge2.inp = true;
								edges++;
								/*
								 * if(!edgelist.remove(edge2)) { System.out.println("?"); }
								 */
								partition.w++;
								vertex2.assignedneibors++;
								vertex2.notassigned--;
								if (vertex2.notassigned == 0) {
									vertexList.remove(vertex2);
									// vertexinC.add(vertex2);
								}
								partition.S.remove(vertex2);
								partition.S.add(vertex2);
							}
						}
						partition.S.add(neibor);
						neibor.ins = true;
					}
				}

			}
			System.out.println(partition.w + ";" + maxSize + ";" + minSize);
		}
		System.out.println(edges + ";" + edgeNum);
		/*
		 * int total=0; for (Vertex vertex : vertexList) { total+=vertex.belongs.size();
		 * } for (Vertex vertex : vertexinC) { total+=vertex.belongs.size(); }
		 */
		int t = 0;
		for (Partition partition2 : partitionList) {
			t += partition2.C.size();
			t += partition2.S.size();
		}
		// System.out.println(total*1.0/vertexNum);
		System.out.println(t * 1.0 / vertexNum);
	}

	public void output() throws IOException {
		for (Partition partition : partitionList) {
			for (Edge edge : partition.edges) {
				if (edge.v1.id < edge.v2.id) {
					out[partition.id].write(edge.v1.id + " " + edge.v2.id + "\n");
				} else {
					out[partition.id].write(edge.v2.id + " " + edge.v1.id + "\n");
				}
			}
			out[partition.id].close();
		}
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String[] datas = { /* "dblp" , "roadNet-PA", "youtube", */"lj"/*, "orkut"*/ };
		float balance = 1.01f;// 平衡系数
		int vNum = 0;// 点数量317080,1088092,1134890,3997962,7600000,3072441
		int[] ps = {  2/*, 4, 8, 16*/};
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
				String edgePath = "D:\\dataset\\Initial_partitioning\\" + data + "2-RDM.txt";
				Ne ne = new Ne(pNum, balance, vNum, edgePath, data);
				long t1 = System.currentTimeMillis();
				ne.partitioning();
				long t2 = System.currentTimeMillis();
				// ne.output();
				System.out.println("time:" + (t2 - t1) * 1.0 / 1000);
				ne.output();
			}
		}
	}

}
