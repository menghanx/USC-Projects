import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class homework {

	static int[][] directions = {
			{-1, -1}, {-1, 0}, {-1, 1},
			{0, -1},         {0, 1},
			{1, -1}, {1, 0}, {1, 1}
	};

	public static void main(String[] args) {
		/*
		 * Store input.txt into String List 
		 * Grader input is always valid 
		 */
		long startTime = System.currentTimeMillis();
		int testcase_id = 49;
		String inputFile = "input"+ testcase_id +".txt";
		String outputFile = "output"+ testcase_id +".txt";
		String inputPath = "testcases/";
		String outputPath = "testcases/";
		
		List<String> inputs = new ArrayList<>();
		try {
			File myFile = new File(inputPath + inputFile);
			Scanner scan = new Scanner(myFile);
			while (scan.hasNextLine()) {
				String data = scan.nextLine();
//				System.out.println(data);
				inputs.add(data);
			}
			scan.close();
		} catch (FileNotFoundException e) {
			System.out.println("input error");
			e.printStackTrace();
		}

		/*
		 * Populate initial variables from inputs
		 */
		String algorithm = inputs.get(0);
		// W = col, H = row
		int col = Integer.valueOf((inputs.get(1).split(" ")[0]));
		int row = Integer.valueOf((inputs.get(1).split(" ")[1]));

		// start state col and row
		int c = Integer.valueOf((inputs.get(2).split(" ")[0]));
		int r = Integer.valueOf((inputs.get(2).split(" ")[1]));

		int maxHeight = Integer.valueOf(inputs.get(3));
		int numOfSites = Integer.valueOf(inputs.get(4));
		int[][] settlingSites = new int[numOfSites][2];
		for (int i = 0; i < numOfSites; i ++) {
			// col
			settlingSites[i][1] = Integer.valueOf((inputs.get(5+i).split(" ")[0]));;
			// row
			settlingSites[i][0] = Integer.valueOf((inputs.get(5+i).split(" ")[1]));

		}

		int map_index = 5 + numOfSites;
		int[][] map = new int[row][col];

		for (int i = 0; i < row; i++){
			for (int j = 0; j < col; j++){
				map[i][j] = Integer.valueOf((inputs.get(map_index+i).split("\\s+")[j]));;
			}
		}

		List<String> output = new ArrayList<String>();

		if (algorithm.equals("BFS")){  	
			output = bfs(row, col, r, c, maxHeight, settlingSites, map);
		}else if (algorithm.equals("UCS")){
			output = ucs(row, col, r, c, maxHeight, settlingSites, map);
		}else{
			output = aStar(row, col, r, c, maxHeight, settlingSites, map);
		}

		try {
			FileWriter writer = new FileWriter(outputPath + outputFile);
			for (String s : output) {
				if (output.indexOf(s) != output.size()-1) {
					writer.write(s+"\r");
				}else {
					writer.write(s);
				}

			}
			writer.close();
		} catch (IOException e) {
			System.out.println("output error.");
			e.printStackTrace();
		}
		long tu = System.currentTimeMillis() - startTime;
		double timeUsed = (double) tu;
		System.out.println("Time used: " + timeUsed/1000 + " s");
	}

	/*
	 *  Object represent each cell in the map
	 *  r: row
	 *  c: col
	 *  m: Height/Muddiness
	 *  cost: the cost from start state to the cell
	 *  h: heuristic value
	 */
	static class Cell {
		int r;
		int c;
		int m;
		int cost;
		int h;

		public Cell (int x, int y, int m){
			this.r = x;
			this.c = y;
			this.m = m;
			this.cost = 0;
			this.h = 0;
		}

		public Cell (int x, int y, int m, int cost){
			this.r = x;
			this.c = y;
			this.m = m;
			this.cost = cost;
			this.h = 0;
		}
		
		public Cell (int x, int y, int m, int cost, int h){
			this.r = x;
			this.c = y;
			this.m = m;
			this.cost = cost;
			this.h = h;
		}
		
		
		
		// return the estimated total cost
		public int getEstimate() {
			return this.cost + this.h;
		}
		
		// print out in the format of c,r
		public String toString(){
			return c + "," + r;
		}
	}

	/*
	 * Comparator for Class Cell, order Cell in increasing cost order
	 */
	static class CellComparator implements Comparator<Cell>{

		@Override
		public int compare(homework.Cell c1, homework.Cell c2) {
			return c1.cost - c2.cost;
		}

	}
	
	/*
	 * Comparator for Class Cell, order Cell in increasing f(n) order
	 * Compare the value of cost + estimated cost = g(n) + h(n) 
	 */
	static class CellAstarComparator implements Comparator<Cell>{
		
		@Override
		public int compare(homework.Cell c1, homework.Cell c2) {
			return (c1.h + c1.cost) - (c2.h + c2.cost);
		}

	}
	
	/*
	 * Print out a BFS path into output.txt
	 */
	private static List<String> bfs(int row, int col, int r, int c, int maxHeight,
			int[][] settlingSites, int[][] map) {


		List<String> result = new ArrayList<>();
		Cell origin = new Cell(r, c, map[r][c]);

		// Generate one output for each settlement
		for (int[] site : settlingSites) {
			// System.out.println(origin +" --> " + site[0]+"," + site[1]);
			// keep track of visted Cells
			int[][] visited = new int[row][col];
			visited[r][c] = 1;
			// Track prev visted Cell
			Map<Cell, Cell> parents = new HashMap<>();
			Queue<Cell> queue = new LinkedList<>();
			queue.add(origin);

			// track last visted Cell 
			Cell curr = queue.peek();

			while(!queue.isEmpty()){
				curr = queue.remove();
				if (curr.r == site[0] && curr.c == site[1]) break;
				// add adjacent vertex that are unvisited and valid into queue
				for (int[] d : directions){
					// check if the next vertex is in bound
					if (curr.r + d[0] >= 0 && curr.r + d[0] <= row-1 && curr.c + d[1] >= 0 && curr.c + d[1] <= col-1){
						// check if the next vertex is visited and can be moved to
						if (visited[curr.r+d[0]][curr.c+d[1]] == 0 && canMove(map, maxHeight, curr.r, curr.c, curr.r+d[0], curr.c+d[1])){
							// Create the adjacent Cell and enqueue
							Cell next = new Cell(curr.r+d[0], curr.c+d[1], map[curr.r+d[0]][curr.c+d[1]]);
							queue.add(next);
							// mark curr Cell as the parent of the next Cell
							parents.put(next, curr);
							// mark next as visited
							visited[curr.r+d[0]][curr.c+d[1]] = 1;
						}
					}
				}
			}

			//			for (Cell u : parents.keySet()){
			//
			//				System.out.println(parents.get(u).toString() + "->" + u.toString());
			//
			//			}

			// Generate output
			String output = "";

			// if queue is empty and settlers didn't make it
			if (curr.r != site[0] || curr.c != site[1]){
				output = "FAIL";
			}else{
				// back track to origin
				while (curr != null) {
					output = " " + curr.toString() + output;
					curr = parents.get(curr);
				}
				output = output.substring(1);
			}

			result.add(output);
		}

		return result;
	}

	private static List<String> ucs(int row, int col, int r, int c, int maxHeight,
			int[][] settlingSites, int[][] map) {

		List<String> result = new ArrayList<>();
		Cell origin = new Cell(r, c, map[r][c]);

		for (int[] site : settlingSites) {
			int[][] visited = new int[row][col];
			visited[r][c] = 1;

			Map<Cell, Cell> parents = new HashMap<>();
			Queue<Cell> queue = new PriorityQueue<>(new CellComparator());
			// memo to keep the pointers of Cell objects to update the cost
			Cell[][] memo = new Cell[row][col];
			memo[r][c] = origin;

			queue.add(origin);

			// track last visted Cell 
			Cell curr = queue.peek();
			
			while(!queue.isEmpty()){
				// remove head and mark as visited
				curr = queue.poll();
				visited[curr.r][curr.c] = 1;

				// break if at goal state
				if (curr.r == site[0] && curr.c == site[1]) break;

				for (int[] d : directions){
					// check inbound
					if (curr.r + d[0] >= 0 && curr.r + d[0] <= row-1 && curr.c + d[1] >= 0 && curr.c + d[1] <= col-1){
						// calculate the weight
						int weight = 10;
						if (Math.abs(d[0])+Math.abs(d[1]) == 2) {
							// weight is 14 if path is diagonal
							weight = 14;
						}
						// if unvisited and not blocked
						if (visited[curr.r+d[0]][curr.c+d[1]] == 0 && canMove(map, maxHeight, curr.r, curr.c, curr.r+d[0], curr.c+d[1])){

							// if the adjacent cell was queued but not visited
							if (memo[curr.r+d[0]][curr.c+d[1]] != null) {
								// if the new path is cheaper
								Cell next = memo[curr.r+d[0]][curr.c+d[1]];
								if (next.cost > curr.cost + weight ) {
									// update the cost and point parent to the curr cell
									next.cost = curr.cost + weight;
									parents.put(next, curr);
									// Rearrange the priority queue
									queue.remove(next);
									queue.add(next);
								}
								// else ignore this path
							}else { // the adjacent cell was not queued
								// Create the adjacent Cell with cost and enqueue
								Cell next = new Cell(curr.r+d[0], curr.c+d[1], map[curr.r+d[0]][curr.c+d[1]], curr.cost + weight);
								queue.add(next);
								// mark curr Cell as the parent of the next Cell
								parents.put(next, curr);
								memo[curr.r+d[0]][curr.c+d[1]] = next;
							}
						}
					}
				}
			}

			// Generate output
			String output = "";

			// if queue is empty and settlers didn't make it
			if (curr.r != site[0] || curr.c != site[1]){
				output = "FAIL";
			}else{
//				int total_cost = curr.cost;
				// back track to origin
				while (curr != null) {
					output = " " + curr.toString() + output;
					curr = parents.get(curr);
				}
				output = output.substring(1);
//				output = "cost: " + total_cost + output;
			}

			result.add(output);

		}

		return result;
	}

	/* Cost: g(n) = moving cost + mud level + height change
	 * Heuristic: h(n) = straight line distance. i.e. h((0,0) -> (2,1)) = (int) sqrt ( |2-0|^2 + |1-0|^2 ) ~= 22
	 * Since the shortest moving cost, regardless of mud and height costs, from (0,0) -> (2,1) is 14 + 10 = 24 
	 * so my h(n) <= true cost
	 * A*: Queue ordered by F(n) = g(n) + h(n)
	 */
	private static List<String> aStar(int row, int col, int r, int c, int maxHeight,
			int[][] settlingSites, int[][] map) {
//		System.out.println("A*");
		List<String> result = new ArrayList<>();


		for (int[] site : settlingSites) {
			
			// start state has no cost and h(n) -> goal state
			int h = getHeuristic(r,c,site[0],site[1]);
			Cell origin = new Cell(r, c, map[r][c], 0, h);
			
			// mark start state as visited
			int[][] visited = new int[row][col];
			// keep track of parents
			Map<Cell, Cell> parents = new HashMap<>();
			// queue is order by f(n) = g(n) + h(n)
			Queue<Cell> queue = new PriorityQueue<>(new CellAstarComparator());
			// memo to track the cell pointer to update cost
			Cell[][] memo = new Cell[row][col];
			memo[r][c] = origin;

			queue.add(origin);
			
			Cell curr = queue.peek();
			
			while (!queue.isEmpty()) {
				// dequeue, mark as visited
				curr = queue.poll();
				visited[curr.r][curr.c] = 1;
				
				// break if at goal state
				if (curr.r == site[0] && curr.c == site[1]) break;
				
				for (int[] d : directions){
					// check inbound
					if (curr.r + d[0] >= 0 && curr.r + d[0] <= row-1 && curr.c + d[1] >= 0 && curr.c + d[1] <= col-1){
						// calculate the weight
						int weight = 10;
						if (Math.abs(d[0])+Math.abs(d[1]) == 2) {
							// weight is 14 if path is diagonal
							weight = 14;
						}
						
						// if unvisited and not blocked
						if (visited[curr.r+d[0]][curr.c+d[1]] == 0 && canMove(map, maxHeight, curr.r, curr.c, curr.r+d[0], curr.c+d[1])){
							// get g(n)
							int cost = getCost(map, weight, curr.r, curr.c, curr.r+d[0], curr.c+d[1]);
							// if the adjacent cell was queued but not visited
							if (memo[curr.r+d[0]][curr.c+d[1]] != null) {
								Cell next = memo[curr.r+d[0]][curr.c+d[1]];
								
								if (next.cost > curr.cost + cost ) {
									// compare the estimate cost and update queue
									next.cost = curr.cost + cost;
									parents.put(next, curr);
									// Rearrange the priority queue
									queue.remove(next);
									queue.add(next);
								}
								
							}else {
								// compute h(n) for the next Cell 
								h = getHeuristic(curr.r+d[0], curr.c+d[1], r, c);
								Cell next = new Cell(curr.r+d[0], curr.c+d[1], map[curr.r+d[0]][curr.c+d[1]], curr.cost + cost, h);
								queue.add(next);
								parents.put(next, curr);
								memo[curr.r+d[0]][curr.c+d[1]] = next;
							}
						}
						
					}
				}
			}
			
			// Generate output
			String output = "";

			// if queue is empty and settlers didn't make it
			if (curr.r != site[0] || curr.c != site[1]){
				output = "FAIL";
			}else{
				// back track to origin
				int total_cost = curr.cost;
				while (curr != null) {
					output = " " + curr.toString() + output;
					curr = parents.get(curr);
				}
//				output = output.substring(1);
				output = "cost: " + total_cost + output;
			}

			result.add(output);
		}

		return result;

	}

	/*
	 * Compute the straight line distance from (x,y) to (r,c)
	 * Each vertical or horizontal unit distance is 10
	 */
	private static int getHeuristic(int x, int y, int r, int c) {
		// straight line distance
//		int vertical = 10 * Math.abs(r - x);
//		int horizontal = 10 * Math.abs(c - y) ; 
//		return (int) Math.sqrt((vertical * vertical) + (horizontal * horizontal));
		// shortest path by definition
//		int vertical = Math.abs(r - x);
//		int horizontal = Math.abs(c - y) ; 
//		return (10*Math.abs(vertical-horizontal) + 14*Math.min(vertical, horizontal));
		// Manhattan Distance
		int vertical = Math.abs(r - x);
		int horizontal = Math.abs(c - y) ; 
		return 10*(vertical+horizontal);
	}
	
	/*
	 * Compare the height difference between 2 cells with maxHeight
	 * return true if wagon can be moved from (x, y) to (r, c) which are row-column pairs
	 */
	private static boolean canMove(int[][] map, int maxHeight, int x, int y, int r, int c) {
		int originHeight = map[x][y] < 0 ? Math.abs(map[x][y]) : 0;
		int targetHeight = map[r][c] < 0 ? Math.abs(map[r][c]) : 0;
		return Math.abs(originHeight - targetHeight) <= maxHeight;
	}
	
	/*
	 * Computer the g(n) for A* from (x, y) to (r, c)
	 */
	private static int getCost(int[][] map, int weight, int x, int y, int r, int c) {
		int cost = weight;
		// calculate the height difference
		int originHeight = map[x][y] < 0 ? Math.abs(map[x][y]) : 0;
		int targetHeight = map[r][c] < 0 ? Math.abs(map[r][c]) : 0;
		cost += Math.abs(originHeight - targetHeight);
		int muddiness = targetHeight == 0 ? map[r][c] : 0;
		return cost + muddiness;
	}

}
