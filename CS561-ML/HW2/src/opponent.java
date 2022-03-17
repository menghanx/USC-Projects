import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class opponent {
	// fixed board size
	static final int n = 8;
	// fixed empty move positions as well as potential capture location
	// black                        		/      \
	static final int[][] blackEMoves = {{1, -1}, {1, 1}};
	// white                       			 \        /
	static final int[][] whiteEMoves = {{-1, -1}, {-1, 1}};
	// king                       		 /        \        \         /
	static final int[][] kingEMoves = {{1, -1}, {1, 1}, {-1, -1}, {-1, 1}};

	// potential jump landing spot
	static final int[][] blackJMoves = {{2, -2}, {2, 2}};
	static final int[][] whiteJMoves = {{-2, -2}, {-2, 2}};
	static final int[][] kingJMoves = {{2, -2}, {2, 2}, {-2, -2}, {-2, 2}};

	static final double blackWin = 999;
	static final double whiteWin = -999;

	// column lookup table
	static final char[] columns = {'a','b','c','d','e','f','g','h'};

	// Comparator to select the best capture action
	static final Comparator<Action> actionComparator = new actionDepthComparator();
	static final Comparator<Action> blackScoreComparator = new blackScoreComparator();
	static final Comparator<Action> whiteScoreComparator = new whiteScoreComparator();

	// Game data to be loaded and stored
	static HashMap<String, Double> playdata = new HashMap<String, Double>();
	static List<Long> stepTimeTaken = new ArrayList<Long>();
	static Integer playCounter = 1;
	static long playTimeLimit = 0;
	static int playDepthLimit = 0;
	static int totalPieces = 0;
	
	// File names
	static final String outputFile = "output.txt";
	static final String inputFile = "input.txt";
	static final String playdataFile = "playdata.txt";
	static String nextInputFile = "next_input.txt";

	// Game state variables
	static boolean outOfTime = false;

	// Time out variables
	static final long startTime = System.currentTimeMillis();
	static long gameTimeUsed = 0;

	// [DEBUG]
	static boolean debug = false;
	static boolean evalDebug = false;
	static boolean searchDebug = false;
	static boolean simuGame = false;
	static boolean clean = false;
	static boolean printGameData = false;
	static int pruneCount = 0;
	static int minimaxCount = 0;
	
	
	public static void main(String[] args) {
		// input
		List<String> inputs = processInput();
		boolean singleGame = true;
		if (inputs.get(0).equals("GAME")) {
			singleGame = false;
		}
		boolean blackTurn = true;
		if (inputs.get(1).equals("WHITE")) {
			blackTurn = false;
		}
		long gameTimeLimit = (long) (Double.valueOf(inputs.get(2))*1000);
		char[][] board = new char[8][8];
		for (int row = 0; row < 8; row ++ ) {
			board[row] = inputs.get(3+row).toCharArray();
		}
		//output
		String outputStr = "";
		Action bestMove = null;
		
		// Calibrate
		//benchMark(12, 5, board, blackTurn, timelimit);
		
		// [DEBUG]
		
//		debug = true;
//		searchDebug = true;
//		evalDebug = true;
//		simuGame = true;
//		printGameData = true;
//		clean = true;
//		printBoard(board);
//		debugBoard(board);
		
		// [DEBUG] END
		
		
		if (singleGame) {
			List<Action> actions = getActions(board, blackTurn);
//			actions.sort(actionComparator);	
//			if (actions.size()>0) {
			bestMove = actions.get(0);
			outputStr = bestMove.toString();
//			}
			
//			Single minimax test
//			double score = score = minimax(board, 14, Integer.MIN_VALUE, Integer.MAX_VALUE, !blackTurn, startTime, 30000, false);

			
            if (debug)
                System.out.println("playTimeLimit: " + playTimeLimit + "ms playDepthLimit: " + playDepthLimit);
            
            if (printGameData) {
            	System.out.print("simple:\t" + playCounter + "\t" + (double)playTimeLimit/1000 + "\t" + totalPieces + "\t" + getActions(board, blackTurn).size() + "\t");
            }

			if (evalDebug) {
				printBoard(board);
				System.out.println(GameOver(board, blackTurn));
				System.out.println(Eval(board, blackTurn, "", false));
			}

		}else{

			// TODO Decide how deep and how much time for this move
            gamePlanner(board, blackTurn, gameTimeLimit);
            
            if (debug)
                System.out.println("playTimeLimit: " + playTimeLimit + "ms playDepthLimit: " + playDepthLimit);
            
            if (printGameData) {
            	System.out.print("simple:\t" + playCounter + "\t" + (double)playTimeLimit/1000 + "\t" + totalPieces + "\t" + getActions(board, blackTurn).size() + "\t");
            }
            
//            bestMove = minimaxDecision(board, blackTurn, playTimeLimit, 3);
            bestMove = iterativeDeepeningSearch(board, blackTurn, playTimeLimit, playDepthLimit);
			outputStr = bestMove.toString();
					
		}
		
		if (printGameData)
			System.out.println((double) (System.currentTimeMillis()-startTime)/1000);
		
		processOuput(outputStr, singleGame);
		
		// [DEBUG]
		if (simuGame) {
			nextInputFile = inputFile;
			generateNextInput(board, singleGame, blackTurn, bestMove, (double)(gameTimeLimit - gameTimeUsed)/1000);
//			generateNextInput(board, singleGame, blackTurn, bestMove, (double)(timelimit - gameTimeUsed)/1000);
		}
//		
//		if (clean)
//			cleanup();
		// [DEBUG] END
		if (debug)
			System.out.println("Total: " +(double) (System.currentTimeMillis()-startTime)/1000 + " seconds");
	}

	private static void benchMark(int maxDepth, int runs, char[][] board, boolean blackTurn, long availableTime) {
		
		long[] benchMarkResults = new long[maxDepth+1];		
		
		Action bestMove = null;
		
		char[][] testBoard = new char[n][n];
		testBoard[0] = ".b.b.b.b".toCharArray();
		testBoard[1] = "b.b.b.b.".toCharArray();
		testBoard[2] = ".b.b.b.b".toCharArray();
		testBoard[3] = "........".toCharArray();
		testBoard[4] = "........".toCharArray();
		testBoard[5] = "w.w.w.w.".toCharArray();
		testBoard[6] = ".w.w.w.w".toCharArray();
		testBoard[7] = "w.w.w.w.".toCharArray();
		
		for (int r = 0; r < runs; r ++) {
			for (int d = 0; d <=maxDepth; d ++ ) {
				long start = System.currentTimeMillis();
				int depthLimit = d;
				bestMove = iterativeDeepeningSearch(testBoard, blackTurn, 60000, depthLimit);
				long elapsed = System.currentTimeMillis() - start;
				benchMarkResults[d] += elapsed;
				//System.out.println("depth=" + depthLimit + " best move: " +outputStr + " timeTake(ms): " + elapsed);
			}
		}
		
		for (int i = 0; i <=maxDepth; i++) {
			System.out.println("depth: " + i + " Avg timetaken: " + benchMarkResults[i]/runs);
		}
		for (int i = 0; i <=maxDepth; i++) {
			System.out.println(benchMarkResults[i]/runs);
		}
	}

	private static void gamePlanner(char[][] board, boolean blackTurn, long gameTimeLimit) {		
        for (char[] row: board)
            for (char c : row)
                if (c!='.')
                	totalPieces++;
        
		// depth = 1 for openning
		if (playCounter == 1) {
			playTimeLimit = 1000;
			playDepthLimit = 1;
		}else {
			// First 200 seconds  ~4s/play ~ 50 plays  maxDepth = 10
			if (gameTimeLimit > 200000) {
				playDepthLimit = 10;
				playTimeLimit = 3000;

			}
			// 100s - 40s  3s/play ~ 20 plays maxDepth = 8
			else if (gameTimeLimit < 200000 && gameTimeLimit >= 100000) {
				playDepthLimit = 8;
				playTimeLimit = 3000;
			}
			// 40s - 10s  1.5s/play ~ 20 plays
			else if (gameTimeLimit < 40000 && gameTimeLimit >= 10000) {
				playDepthLimit = 6;
				playTimeLimit = 1500;
			}
			// 10s - 1s 0.25s/play ~ 30 plays
			else if (gameTimeLimit < 10000 && gameTimeLimit >= 5) {
				playDepthLimit = 4;
				playTimeLimit = 300;
			}
			// less than 1s  0.01s/play
			else {
				playDepthLimit = 1;
				playTimeLimit = 10;
			}
		}

		
		
//		int cores = Runtime.getRuntime().availableProcessors();
//		System.out.println("cores: " + cores);
		
	}


	/**
	 * Run iterativeDeepening search based on the timelimit
	 * @param board
	 * @param blackTurn
	 * @param timelimit
	 * @param depthLimit
	 * @return best Action
	 */
	public static Action iterativeDeepeningSearch(char[][] board, boolean blackTurn, long timelimit, int depthLimit) {
	
		long endTime = System.currentTimeMillis() + timelimit;
		int depth = 1;
		outOfTime = false;

		// Move ordering by number of jumps
		List<Action> moves = getActions(board, blackTurn);
		// If only one action left
		if (moves.size() == 1) {
			if (printGameData)
				System.out.print((depth-1)+ "\t");
			return moves.get(0);
		}
		
		moves.sort(actionComparator);
		// final output move
		Action resMove = moves.get(0);
		
		while (depth <= depthLimit && !outOfTime) {
			// find the best move at each iteration
			Action bestMove = moves.get(0);;
			double bestScore = blackTurn ? Integer.MIN_VALUE : Integer.MAX_VALUE;
			
			long currentTime = System.currentTimeMillis();
			long timeElapsed = System.currentTimeMillis() - startTime;
			
			if (timeElapsed > (timelimit/2)) {
				break;
			}
			
			
			// [DEBUG]
			if (searchDebug) {
				pruneCount = 0;
				minimaxCount = 0;
				System.out.println("depth="+depth+" ");
			}
			
			// TODO Decide when to use TT
			boolean enableTranspositionTable = (depth >= 99);
			
			// Iterative Deepening actually performs one depth of search.
			for (Action move : moves) {
//			for (int i = 0; i < moves.size(); i++) {
//				Action move = moves.get(i);
				
				board = exhaustResult(board, move, blackTurn);
				
				// let minimax run with depth - 1 as opponent			
				double score = minimax(board, depth-1, Integer.MIN_VALUE, Integer.MAX_VALUE, !blackTurn, currentTime, endTime - currentTime, enableTranspositionTable);
				// update move score if better
				move.scoreUpdated =false;
				move.updateScore(score, blackTurn);

				//undo the move
				undoAction(board, move);
	
				if (blackTurn) {
					if (score >= blackWin) {
						if (printGameData)
							System.out.print((depth-1)+ "\t");
						return move;
					}
	
					if (score > bestScore) {
						bestScore = score;
						bestMove = move;
						resMove = move;
					}
				}
				else {
					if (score <= whiteWin) {
						if (printGameData)
							System.out.print((depth-1)+ "\t");
						return move;
					}
	
					if (score < bestScore) {
						bestScore = score;
						bestMove = move;
						resMove = move;
					}
				}
			}
			
			
			
			if (searchDebug) {
				System.out.print("Before Ordering: ");
				System.out.println(moves);
				for (Action a : moves) {
					System.out.println("Move: " + a.toString() + " score: " + a.score);
				}
			}
			
			// Order the moves based on the score of this iteration
			if (blackTurn) {
				moves.sort(blackScoreComparator);
				
			}else {
				moves.sort(whiteScoreComparator);
			}
			if (searchDebug) {
				System.out.print("After Ordering:  ");
				System.out.println(moves);	
				System.out.println("depth="+depth+" best move: >> " + bestMove.toString() + " << score:" + bestMove.score);
				System.out.println("result move: >> " + resMove.toString() + " << score:" + bestMove.score);
				System.out.println("minimaxCount: " + minimaxCount +" pruned times: " + pruneCount);
				System.out.println();
			}
			depth++;
		}
		if (printGameData)
			System.out.print((depth-1) + "\t");
		//System.out.println("depth reached:" + (depth-1) + " eval: " + resMove.score);
		return resMove;
	}


	/**
	 * Return the score of a state
	 * @param board represents the current state
	 * @param depth depth limit on how deep the game tree goes
	 * @param alpha best choice for max player along the path to the root, default is -infinity
	 * @param beta best choice for min player along the path to the root, default is infinity
	 * @param blackTurn indicates whose turn it is, true = max, false = min
	 * @param limit time limit in long
	 * @param beginTime 
	 * @return the score of the a
	 */
	public static double minimax(char[][] board, int depth, double alpha, double beta, boolean blackTurn, long beginTime, long limit, boolean enableTranspositionTable) {
		// blackTurn = true = Max
		// blackTurn = false = Min
		//String strKey = "" + blackTurn + depth + boardToString(board, true);
		String strKey = boardToString(board, true);
		if (playdata.containsKey(strKey)) {
			return playdata.get(strKey);
		}
		
		//[DEBUG]
		if (searchDebug)
			minimaxCount ++;
		
		String gameOver = GameOver(board, blackTurn);
		
		if (System.currentTimeMillis() >= beginTime + limit) {
			outOfTime = true;
		}
	
		if (outOfTime || depth == 0 || !gameOver.equals("no")) {
			return simpleEval(board, blackTurn, strKey, enableTranspositionTable);
//			return Eval(board, blackTurn, strKey, enableTranspositionTable);
		}
	
		List<Action> moves = getActions(board, blackTurn);
	
		if (blackTurn) {
			double maxEval = Integer.MIN_VALUE;
			for (Action move : moves) {
	
				board = exhaustResult(board, move, blackTurn);
				double score = minimax(board, depth-1, alpha, beta, !blackTurn, beginTime, limit, enableTranspositionTable);
				undoAction(board, move);
				maxEval = Math.max(maxEval, score);
				alpha = Math.max(alpha, score);
				if (beta <= alpha) {
					//[DEBUG]
					if (searchDebug)
						pruneCount++;
					break;
				}
			}
			return maxEval;
		}else {
			double minEval = Integer.MAX_VALUE;
			for (Action move : moves) {
	
				board = exhaustResult(board, move, blackTurn);
				double score = minimax(board, depth-1, alpha, beta, !blackTurn, beginTime, limit, enableTranspositionTable);
				undoAction(board, move);
				minEval = Math.min(minEval, score);
				beta = Math.min(beta, score);
				if (beta <= alpha) {
					// [DEBUG]
					if (searchDebug)
						pruneCount++;
					break;
				}
			}
			return minEval;
		}
	
	}
	
	// Find best action by running justMinimax
	public static Action minimaxDecision(char[][] board, boolean blackTurn, long timelimit, int depthLimit) {

		long endTime = System.currentTimeMillis() + timelimit;
		int depth = depthLimit;
		outOfTime = false;

		Action bestMove = null;
		double bestScore = blackTurn ? Integer.MIN_VALUE : Integer.MAX_VALUE;

		// Move ordering by number of jumps
		List<Action> moves = getActions(board, blackTurn);

		bestMove = moves.get(0);
		long currentTime = System.currentTimeMillis();
		
		// [DEBUG]
		if (searchDebug) {
			pruneCount = 0;
			minimaxCount = 0;
		}
		
		// minimaxDecision actually performs one depth of search.
		for (Action move : moves) {
			board = exhaustResult(board, move, blackTurn);
			double score = justMinimax(board, depth-1, !blackTurn, currentTime, endTime - currentTime);
			undoAction(board, move);

			if (blackTurn) {
				if (score >= blackWin) {
					return move;
				}

				if (score > bestScore) {
					bestScore = score;
					bestMove = move;
				}
			}
			else {
				if (score <= whiteWin) {
					return move;
				}

				if (score < bestScore) {
					bestScore = score;
					bestMove = move;
				}
			}
		}
		
		if (searchDebug) {
			System.out.println("depth="+depthLimit);
			System.out.println("minimaxCount: " + minimaxCount +" pruned times: " + pruneCount);
		}
		
		return bestMove;
	}
	
	// Minimax no prune
	public static double justMinimax(char[][] board, int depth, boolean blackTurn, long beginTime, long limit) {
		//[DEBUG]
		minimaxCount ++;
		
		String gameOver = GameOver(board, blackTurn);
	
		if (System.currentTimeMillis() >= beginTime + limit) {
			outOfTime = true;
		}
	
		if (outOfTime || depth == 0 || !gameOver.equals("no")) {
			return simpleEval(board, blackTurn, "nope", false);
//			return Eval(board, blackTurn, strKey, enableTranspositionTable);
		}
	
		List<Action> moves = getActions(board, blackTurn);
	
		if (blackTurn) {
			double maxEval = Integer.MIN_VALUE;
			for (Action move : moves) {
	
				board = exhaustResult(board, move, blackTurn);
				double score = justMinimax(board, depth-1, !blackTurn, beginTime, limit);
				undoAction(board, move);
				maxEval = Math.max(maxEval, score);
			}
			return maxEval;
		}else {
			double minEval = Integer.MAX_VALUE;
			for (Action move : moves) {
	
				board = exhaustResult(board, move, blackTurn);
				double score = justMinimax(board, depth-1, !blackTurn, beginTime, limit);
				undoAction(board, move);
				minEval = Math.min(minEval, score);
			}
			return minEval;
		}
	}

	/**
	 * Debug purpose, Simulate game, overwrite input.txt for the next play
	 * @param board - current state of the game
	 * @param blackTurn - black's turn or not
	 * @param bestMove - bestMove of the current board
	 * @param time - time left
	 */
	private static void generateNextInput(char[][] board, boolean singleGame, boolean blackTurn, Action bestMove, double time) {
		String nextInput = singleGame ? "SINGLE" : "GAME";
		nextInput += "\n";
		nextInput += blackTurn ? "WHITE" : "BLACK";
//		nextInput += blackTurn ? "BLACK" : "WHITE";
		nextInput += "\n";
		nextInput += time;
		nextInput += "\n";
		nextInput += boardToString(exhaustResult(copyBoard(board), bestMove, blackTurn), false);
//		nextInput += boardToString(board, false);
//		printBoard(exhaustResult(copyBoard(board), bestMove, blackTurn));
//		System.out.println(nextInput);
//		System.out.println();
		try {
			FileWriter writer = new FileWriter(nextInputFile);
			writer.write(nextInput);
			writer.close();

		} catch (IOException e) {
			System.out.println("output error.");
			e.printStackTrace();
		}

	}

	@SuppressWarnings("unchecked")
	public static List<String> processInput() {

		List<String> inputs = new ArrayList<>();
		
		try {
			File myFile = new File(inputFile);
			Scanner scan = new Scanner(myFile);
			while (scan.hasNextLine()) {
				String data = (String) scan.nextLine();
				inputs.add(data);
			}
			scan.close();
		} catch (Exception e) {
			System.out.println("input error");
			e.printStackTrace();
		}
		
		// local variable game mode
		boolean singleGame = true;
		if (inputs.get(0).equals("GAME")) {
			singleGame = false;
		}
		if (!singleGame) {
			try {
				// Process playdata if exists and populate Hash table
				File playFile  = new File(playdataFile);
				// if the playdata file exits
				if (!playFile.createNewFile()) {
					FileInputStream fis = new FileInputStream(playFile);
					// if file is not empty
					if (fis.available() != 0) {
						ObjectInputStream ois = new ObjectInputStream(fis);
						playdata = (HashMap<java.lang.String, Double>)ois.readObject();
						stepTimeTaken = (List<Long>) ois.readObject();
						playCounter = (Integer) ois.readObject() + 1;
						ois.close();
					}
					fis.close();
				}else {
				}

			} catch (Exception e) {
				System.out.println("input error");
				e.printStackTrace();
			}
		}		
		return inputs;
	}

	public static void processOuput(String outputStr, boolean singleGame) {
				
		try {
			// [DEBUG] End of the execution
			gameTimeUsed =  System.currentTimeMillis() - startTime;
			stepTimeTaken.add(gameTimeUsed);
//			System.out.println("processOutput timeused: " + (double) gameTimeUsed / 1000 +" seconds");
//			System.out.println();
			// write move output
			FileWriter writer = new FileWriter(outputFile);
			writer.write(outputStr);
			writer.close();
			
			
			
			// write game data
			if (!singleGame) {
				File playFile  = new File(playdataFile);
				FileOutputStream  fos = new FileOutputStream(playFile);
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				oos.writeObject(playdata);
				oos.writeObject(stepTimeTaken);
				oos.writeObject(playCounter);
				oos.flush();
				oos.close();
				fos.close();
			}

		} catch (IOException e) {
			System.out.println("output error.");
			e.printStackTrace();
		}

		if (debug) {
			int cores = Runtime.getRuntime().availableProcessors();
			System.out.println("Transposition Table size: " +playdata.size());
			System.out.println("cores: " + cores);
			System.out.println("Execution timetaken: " + (double)gameTimeUsed/1000 + " s");
			System.out.println("CPU time: " + (double) (gameTimeUsed * cores)/1000 + " s");
		}
	}
	
	/**
	 * Delete playdata.txt
	 */
	static private void cleanup() {
		File myObj = new File(playdataFile); 
		myObj.delete();
	}

	/**
	 * Return true if the current state is a draw
	 * @param board
	 * @return
	 */
	public static boolean isDraw(char[][] board) {

		return true;
	}

	/**
	 * The function will only be called when the game in terminal state
	 * if the game was not won, return the Eval function.
	 * @param board
	 * @param blankTurn
	 * @param strKey 
	 * @return
	 */
	public static double utility(boolean gameOver, char[][] board, boolean blankTurn, String strKey, boolean enableTranspositionTable) {

		// If game is not over
		if (!gameOver){
			return Eval(board, blankTurn, strKey, enableTranspositionTable);
		}

		// If blackTurn and game over, white win
		if (blankTurn) {
			playdata.put(strKey, whiteWin);
			return whiteWin;
		}else {
			playdata.put(strKey, blackWin);
			return whiteWin;
		}	
	}

	/**
	 * Given a board state, return the evaluation based on the weight of the number, position of the piece. weight in term of the maximizing player
	 * 1. King pieces w = 7.75
	 * 2. pawn pieces w = 5
	 * 3. pawns at the back row w = 4
	 * 4. pieces can be captured (negative weight) w = -3
	 * 5. pieces cannot be captured w = 3
	 * @param state
	 * @param bTurn
	 * @param strKey for the transportation table
	 * @param enableTranspositionTable enable or not
	 * @return
	 */
	public static double Eval(char[][] state, boolean bTurn, String strKey, boolean enableTranspositionTable) {
		
		String gameover = GameOver(state, bTurn);
		
		if (gameover.equals("white")) {
			return whiteWin;
		}
		if (gameover.equals("black")) {
			return blackWin;
		}
		
		double eval = 0;

		// Eval factors
		double morePieces = 0;
		double backRowPawn = 0;
		double advancedPawn = 0;
		double midBox = 0;
		double midRow = 0;
		double vulnerables = 0;
		double safePieces = 0;
		// memo for vulnerable location on the board
		int [][] captured = new int[n][n];
		
		// who has more pieces, Kings are 5, pawns are 3
		for (int row = 0; row < n; row ++) {
			for (int col = 0; col < n; col ++) {
				// Black 
				if (state[row][col] == 'b' || state[row][col] == 'B') {
					if (state[row][col] == 'b') {
						morePieces += 5;
//						if (row == 0) {
//							backRowPawn += 4;
//						}else {
//								advancedPawn += 0.39 * (row);
//						}
					}
					if (state[row][col] == 'B') {
						morePieces += 7.75;
					}
					

//					if (row == 3 || row == 4) {
//						if (col >= 2 && col <=5) {
//							midBox += 2.5;
//						}else {
//							midRow += 0.5;
//						}
//					}

					// available captures
					if (hasJMove(state, row, col, true)){
						List<Action> jumps = generateJMove(state, row, col, true);

						for (Action j : jumps) {
							updateCaptured(captured, j, 3);
						}
					}
					if (isProtected(state, row, col))
						safePieces +=3;

				}
				// White
				else if (state[row][col] == 'w' || state[row][col] == 'W'){
					if (state[row][col] == 'w') {
						morePieces -= 5;
//						if (row == 7) {
//							backRowPawn -= 4;
//						}else {
//							advancedPawn -= 0.39 * (7-row);
//						}
					}
					if (state[row][col] == 'W') {
						morePieces -= 7.75;
					}

					

//					if (row == 3 || row == 4) {
//						if (col >= 2 && col <=5) {
//							midBox -= 2.5;
//						}else {
//							midRow -= 0.5;
//						}
//					}

					// available captures
					if (hasJMove(state, row, col, false)){
						List<Action> jumps = generateJMove(state, row, col, false);
						for (Action j : jumps) {
							updateCaptured(captured, j, -3);
						}
					}
					if (isProtected(state, row, col))
						safePieces -=3;
				}
			}
		}	

		for (int[] r : captured) {
			for (int i : r) {
				vulnerables += i;
			}
		}
		
		eval = morePieces + backRowPawn + advancedPawn + midBox + midRow + vulnerables + safePieces;

		if (evalDebug) {
			System.out.println("More Piece: " + morePieces);
			System.out.println("backRowPawn: " + backRowPawn);
			System.out.println("advancedPawn: " + advancedPawn);
			System.out.println("midBox: " + midBox);
			System.out.println("midRow: " + midRow);
			System.out.println("vulnerables: " + vulnerables);
			System.out.println("safePieces: " + safePieces);
		}
		
		if (enableTranspositionTable)
			playdata.put(strKey, eval);

		return eval;
	}
	
	/**
	 * Only count pawn and King pieces
	 * @param state
	 * @param bTurn
	 * @param strKey
	 * @param enableTranspositionTable
	 * @return
	 */
	public static double simpleEval(char[][] state, boolean bTurn, String strKey, boolean enableTranspositionTable) {
		
		String gameover = GameOver(state, bTurn);
		
		if (gameover.equals("white")) {
			return whiteWin;
		}
		if (gameover.equals("black")) {
			return blackWin;
		}
		
		double eval = 0;

		// Eval factors
		double morePieces = 0;
		
		// who has more pieces, Kings are 5, pawns are 3
		for (int row = 0; row < n; row ++) {
			for (int col = 0; col < n; col ++) {
//				System.out.println("("+row+","+col+")" + state[row][col]);
				// Black 
				if (state[row][col] == 'b' || state[row][col] == 'B') {
					if (state[row][col] == 'b') {
						morePieces += 5;
					}
					if (state[row][col] == 'B') {
//						morePieces += 7.75;
						morePieces += 10;
					}
				}
				// White
				else if (state[row][col] == 'w' || state[row][col] == 'W'){
					if (state[row][col] == 'w') {
						morePieces -= 5;
					}
					if (state[row][col] == 'W') {
//						morePieces -= 7.75;
						morePieces -= 10;
					}
				}
			}
		}	

		
		eval = morePieces;

		if (evalDebug) {
			System.out.println("More Piece: " + morePieces);
		}
		
		if (enableTranspositionTable)
			playdata.put(strKey, eval);

		return eval;
	}
	
	/**
	 * Return winner if game is over, otherwise return "no"
	 * @param state
	 * @param bTurn
	 * @return
	 */
	private static String GameOver(char[][] state, boolean bTurn) {

		String output = "no";
		boolean hasBlack = false;
		boolean hasWhite = false;
		boolean blackCanMove = false;
		boolean whiteCanMove = false;
		
		for (int row = 0; row < n; row ++) {
			for (int col = 0; col < n; col++) {
				if (state[row][col] == '.') {
					continue;
				}else if (state[row][col]  == 'b' || state[row][col] == 'B') {
					hasBlack = true;
					if (bTurn) {
						if (hasJMove(state, row, col, bTurn))
							blackCanMove = true;
						if (hasEMove(state, row, col))
							blackCanMove = true;
					}
				}else {
					hasWhite = true;
					if (!bTurn) {
						if (hasJMove(state, row, col, bTurn))
							whiteCanMove = true;
						if (hasEMove(state, row, col))
							whiteCanMove = true;
					}
				}
			}
		}
		
		// If both colors have piece left
		if (hasBlack && hasWhite) {
			if (bTurn) {
				if (!blackCanMove)
					output = "white";
			}else {
				if (!whiteCanMove)
					output = "black";
			}
			return output;
		}else {
			if (hasBlack && !hasWhite) {
				output ="black";
			}
			
			if (hasWhite && !hasBlack) {
				output = "white";
			}
			return output;
		}		
	}

	/**
	 * 3 points recorded for each black capture, -3 for each white capture
	 * @param captured
	 * @param piece 
	 * @param board
	 * @param j
	 * @param points
	 */
	public static void updateCaptured(int[][] captured, Action a, int points) {
		int row = ( a.from[0] + a.to[0] ) / 2;
		int col = ( a.from[1] + a.to[1] ) / 2;
		// Black act when pts > 0
		if (a.next != null) {
			updateCaptured(captured, a.next, points);
		}
		// if the spot is not captured
		if (captured[row][col] == 0) {
			captured[row][col] = points;
		}
		
	}

	/**
	 * Check if a piece is being protected.
	 * @param state
	 * @param row
	 * @param col
	 * @return
	 */
	private static boolean isProtected(char[][] state, int row, int col) {
		int[][] neighbors = kingEMoves;
		boolean[] safe = new boolean[4];
		boolean[] occupied = new boolean[4];
	
		if (state[row][col] == '.')
			return false;
	
		if (row == 0 || row == 7 || col == 0 || col == 7)
			return true;
	
		for (int i = 0; i < neighbors.length; i++) {
			int[]spot = neighbors[i];
			if (row+spot[0] < n && row+spot[0] >= 0 && col+spot[1] < n && col+spot[1] >= 0) {
				if (state[row+spot[0]][col+spot[1]] == '.') {
					safe[i] = false;
					occupied[i] = false;
				}else {
					if (state[row][col] == 'b' || state[row][col] == 'B') {
						if (row +spot[0] < row) {
							safe[i] = (state[row+spot[0]][col+spot[1]] != 'W');
						}else {
							safe[i] = (state[row+spot[0]][col+spot[1]] == 'b' || state[row+spot[0]][col+spot[1]] == 'B');
						}
					}else if (state[row][col] == 'w' || state[row][col] == 'W') {
						if (row +spot[0] > row) {
							safe[i] = (state[row+spot[0]][col+spot[1]] != '.' && state[row+spot[0]][col+spot[1]] != 'B');
						}else {
							safe[i] = (state[row+spot[0]][col+spot[1]] == 'w' || state[row+spot[0]][col+spot[1]] == 'W');
						}
					}
					occupied[i] = true;
				}
			}else {
				safe[i] = true;
				occupied[i] = true;
			}
			if (!(safe[i] ||  occupied[i]))
				return false;
		}
		return true;
	}

	/**
	 * Action class represents each available action of the current state
	 * @author xmh91
	 *
	 */
	public static class Action{

		String type;
		int[] from = new int[2];
		int[] to = new int[2];	
		// Store a score for the action
		double score = 0;
		boolean scoreUpdated = false;
		Action next = null;
		// Undo action vars
		char prevPiece = '.';
		char capturedPiece = '.';
		boolean crowned = false;
		
		// empty action with single move
		public Action(String type, int f_r, int f_c, int t_r, int t_c) {
			this.type = type;
			this.from[0] = f_r;
			this.from[1] = f_c;
			this.to[0] = t_r;
			this.to[1] = t_c;
		}

		// jump action with follow up jumps
		public Action(String type, int f_r, int f_c, int t_r, int t_c, Action nextActions) {
			this.type = type;
			this.from[0] = f_r;
			this.from[1] = f_c;
			this.to[0] = t_r;
			this.to[1] = t_c;
			this.next = nextActions;
		}

		// Create Action with same properties
		public Action (Action move) {
			this.type = move.type;
			this.from[0] = move.from[0];
			this.from[1] = move.from[1];
			this.to[0] = move.to[0];
			this.to[1] = move.to[1];
			this.next = move.next;
		}

		public Action copyHead() {
			Action copy = new Action (this.type, this.from[0], this.from[1], this.to[0], this.to[1]);
			copy.capturedPiece = this.capturedPiece;
			return copy;
		}

		public String toString() {
			String output = type+" "+columns[from[1]]+""+(8-from[0])+" "+columns[to[1]]+""+(8-to[0]);
			if (this.next == null) {
				return output;
			}
			Action nextAction = this.next;
			output += "\n";
			output += nextAction.toString();

			return output;
		}

		public void print() {
			System.out.println(type+" "+columns[from[1]]+""+(8-from[0])+" "+columns[to[1]]+""+(8-to[0]));
		}
		
		/**
		 * 
		 * @param newScore
		 * @param black
		 */
		public void updateScore(double newScore, boolean black) {
			if (!this.scoreUpdated) {
				this.score = black ? Integer.MIN_VALUE : Integer.MAX_VALUE;
				this.scoreUpdated = true;
			}
			if (black) {
				this.score = newScore > this.score ? newScore : this.score;
			}else {
				this.score = newScore < this.score ? newScore : this.score;
			}
		}
		
		public int getMoveDepth() {
			if (this.next == null) {
				return 1;
			}
			int maxDepth = 1;
			Action n = this;
			while (n.next != null) {
				n = n.next;
				maxDepth ++;
			}

			return maxDepth;
		}
	}

	static class actionDepthComparator implements Comparator<Action>{
		@Override
		public int compare(Action a1, Action a2) {
			return a2.getMoveDepth() - a1.getMoveDepth();
		}
	}

	static class blackScoreComparator implements Comparator<Action>{
		@Override
		public int compare(Action a1, Action a2) {
			if (a1.score < a2.score) {
				return 1;
			}
			if (a1.score > a2.score) {
				return -1;
			}
			return 0;
		}
	}

	static class whiteScoreComparator implements Comparator<Action>{
		@Override
		public int compare(Action a1, Action a2) {
			if (a1.score > a2.score) {
				return 1;
			}
			if (a1.score < a2.score) {
				return -1;
			}
			return 0;
		}
	}
	/**
	 * ACTIONS(state) implementation
	 * Return a list of actions available for the current board
	 * @param board
	 * @param blackTurn 
	 * @return List<Action>
	 */
	public static List<Action> getActions(char[][] board, boolean blackTurn) {
		// holds all the actions
		List<Action> actions = new ArrayList<Action>();
		// flag for mandatory jump
		boolean mustJump = false;
		// default white's turn
		char pawn = 'w';
		char king = 'W';
		// Black's turn
		if (blackTurn) {
			pawn = 'b';
			king = 'B';
		}
		// for each available pawn or king of the turn
		for (int row = 0; row < n; row++) {
			for (int col = 0; col < n; col++) {
				if (board[row][col] != pawn && board[row][col] != king) {
					continue;
				}else{
					// prioritize jump moves over empty moves
					
					if (hasJMove(board, row, col, blackTurn)) {
						// flag so empty actions won't be considered
						mustJump = true;
						for (Action a : generateJMove(board, row, col, blackTurn)) {
							actions.add(a);
						}	
					}
				}
			}
		}

		// Only consider empty jumps if there is no mandatory jump
		if (mustJump) {
			return actions;
		}


		for (int row = 0; row < n; row++) {
			for (int col = 0; col < n; col++) {
				if (board[row][col] != pawn && board[row][col] != king) {
					continue;
				}else{
					if (hasEMove(board, row, col)) {
						//						System.out.println(board[row][col]+" ("+row+","+col+") has empty move");
						for (Action a : generateEMove(board, row, col, blackTurn)) {
							actions.add(a);
						}
					}
				}
			}
		}

		return actions;
	}

	/**
	 * Return true if there is an empty move available for the piece
	 * Method assumes the piece at (row, col) is either a King or a Pawn that matches the color respectively
	 * @param board - char[][] current game state 
	 * @param row - row number on the board
	 * @param col - col number on the board
	 * @return boolean
	 */
	public static boolean hasEMove(char[][] board, int row, int col) {
		// default piece is King piece
		int[][] moves = kingEMoves;
		// for black
	
		// change move set to black piece
		if (board[row][col] == 'b') {
			moves = blackEMoves;
		}
	
		// change move set to white piece
		if (board[row][col] == 'w') {
			moves = whiteEMoves;
		}
	
		for (int[] move: moves) {
			// check movement is in bound
			if (row+move[0] < n && row+move[0] >= 0 && col+move[1] < n && col+move[1] >= 0) {
				if (board[row+move[0]][col+move[1]] == '.')
					return true;
			}
		}
		return false;
	}

	/**
	 * Return a list of Empty actions that is available for the current state at location (row,col)
	 * @param board - char[][] current game state 
	 * @param row - row number
	 * @param col - col number
	 * @param blackTurn - black's turn or not
	 * @return List<Action> - a list of available actions
	 */
	public static List<Action> generateEMove(char[][] board, int row, int col, boolean blackTurn) {
		List<Action> res = new ArrayList<Action>();
		// default piece is King 
		int[][] moves = kingEMoves;

		// change move set to black pawn
		if (board[row][col] == 'b') {
			moves = blackEMoves;
		}

		// change move set to white pawn
		if (board[row][col] == 'w') {
			moves = whiteEMoves;
		}

		// each i represent a direction for the available moves
		for (int i = 0; i < moves.length; i++) {
			int[] move = moves[i];
			// check if the target position is in bound
			if (row+move[0] < n && row+move[0] >= 0 && col+move[1] < n && col+move[1] >= 0) {
				// check if the target position is open 
				if (board[row+move[0]][col+move[1]] == '.') {
					// create a new empty action
					Action action = new Action("E", row, col, row+move[0], col+move[1]);
					res.add(action);
				}
			}
		}

		return res;
	}

	/**
	 * Return true if the piece can both make a jump move and has an opponent to capture
	 * @param board - char[][] current game state 
	 * @param row - row number
	 * @param col = col number
	 * @param blackTurn - black's turn or not
	 * @return
	 */
	public static boolean hasJMove(char[][] board, int row, int col, boolean blackTurn) {
		// default piece is King piece
		int[][] moves = kingJMoves;
		int[][] captures = kingEMoves;
	
		// change move set to black piece
		if (board[row][col] == 'b') {
			moves = blackJMoves;
			captures = blackEMoves;
		}
	
		// change move set to white piece
		if (board[row][col] == 'w') {
			moves = whiteJMoves;
			captures = whiteEMoves;
		}
	
		// default color is black
		char oppoPawn = 'w';
		char oppoKing = 'W';
		if (!blackTurn) {
			oppoPawn = 'b';
			oppoKing = 'B';
		}
	
		// each i represent a direction for the available moves
		for (int i = 0; i < moves.length; i++) {
			int[] move = moves[i];
			int[] capture = captures[i];
			// check if the target position is in bound
			if (row+move[0] < n && row+move[0] >= 0 && col+move[1] < n && col+move[1] >= 0) {
				// check if the target position is open 
				if (board[row+move[0]][col+move[1]] == '.') {
					//check if the piece can capture a opponent King or Pawn
					if (board[row+capture[0]][col+capture[1]] == oppoKing || board[row+capture[0]][col+capture[1]] == oppoPawn)
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * Return a list of Jump actions that is available for the current state at location (row,col)
	 * @param board - char[][] current game state 
	 * @param row - row number
	 * @param col - col number
	 * @param blackTurn - black's turn or not
	 * @return List<Action> - a list of available actions
	 */
	public static List<Action> generateJMove(char[][] board, int row, int col, boolean blackTurn) {

		List<Action> res = new ArrayList<Action>();
		// default piece is King piece
		int[][] moves = kingJMoves;
		int[][] captures = kingEMoves;

		// change move set to black piece
		if (board[row][col] == 'b') {
			moves = blackJMoves;
			captures = blackEMoves;
		}

		// change move set to white piece
		if (board[row][col] == 'w') {
			moves = whiteJMoves;
			captures = whiteEMoves;
		}

		// default color is black
		char oppoPawn = 'w';
		char oppoKing = 'W';
		if (!blackTurn) {
			oppoPawn = 'b';
			oppoKing = 'B';
		}
		
		// each i represent a direction for the available moves
		for (int i = 0; i < moves.length; i++) {
			int[] move = moves[i];
			int[] capture = captures[i];
			// check if the target position is in bound
			if (row+move[0] < n && row+move[0] >= 0 && col+move[1] < n && col+move[1] >= 0) {
				// check if the target position is open 
				if (board[row+move[0]][col+move[1]] == '.') {
					//check if the piece can capture a opponent King or Pawn
					if (board[row+capture[0]][col+capture[1]] == oppoKing || board[row+capture[0]][col+capture[1]] == oppoPawn) {
						
						Action action = new Action("J", row, col, row+move[0], col+move[1]);						
						// get the next state from this action
						board = result(board, action, blackTurn);
						// if a pawn turned to a king from this action, action ends, break
						if (action.crowned) {
							res.add(action);
							undoAction(board, action);
							continue;
						}			
						// if there are more jump action after the current jump
						if (hasJMove(board, action.to[0], action.to[1], blackTurn)) {
							for (Action nextAction : generateJMove(board, action.to[0], action.to[1], blackTurn)) {
								Action head = action.copyHead();
								head.next = nextAction;
								res.add(head);
								undoAction(board, action);
							}
						} else {
							res.add(action);
							undoAction(board, action);
						}
					}
				}
			}			
		}
		return res;
	}

	/**
	 * Apply the move to the curState regardless of next moves
	 * @param curState - char[][] current game state 
	 * @param action - action to be applied 
	 * @param blackTurn - black's turn or not
	 * @return curState - after applying one single move from input action
	 */
	public static char[][] result(char[][] curState, Action action, boolean blackTurn) {
	
		char piece = curState[action.from[0]][action.from[1]];
		// Store the previous piece in the action for undoAction()
		action.prevPiece = curState[action.from[0]][action.from[1]];
		// remove piece from origin
		curState[action.from[0]][action.from[1]] = '.';
	
		// for jump action, remove the captured piece
		if (action.type.equals("J")) {
			int cap_row = (action.from[0] + action.to[0]) / 2;
			int cap_col = (action.from[1] + action.to[1]) / 2;
			// store captured for undoAction()
			action.capturedPiece = curState[cap_row][cap_col];
			curState[cap_row][cap_col] = '.';
		}
	
		// If a pawn reached king's row
		if (piece != 'W' && piece != 'B') {
			// black pawn
			if (blackTurn && action.to[0] == 7) {
				piece = 'B';
				action.crowned = true;
			}
			// white pawn
			if (!blackTurn && action.to[0] == 0) {
				piece = 'W';
				action.crowned = true;
			}
		}
		
		// update the destination with the piece value
		curState[action.to[0]][action.to[1]] = piece;
	
		return curState;
	}


	/**
	 * Exhaust the action if the there exists a next action.
	 * @param state - char[][] current game state 
	 * @param action - action to be applied to the state
	 * @param blackTurn - black's turn or not
	 * @return board - char[][] updated game state 
	 */
	public static char[][] exhaustResult(char[][] state, Action action, boolean blackTurn) {
		state = result(state, action, blackTurn);
		Action nextMove = action.next;
		while (nextMove!=null) {
			state = result(state, nextMove, blackTurn);
			nextMove = nextMove.next;
		}
		return state;
	}

	/**
	 * Undo an action that was applied to the current state
	 * @param board - char[][] previous game state 
	 * @param action - action applied to the previous game state
	 */
	private static void undoAction(char[][] board, Action action) {
	
		char piece = action.prevPiece;		
		
		if (action.type.equals("J")) {
			Action nextAction = action.next;
	
			if (nextAction != null) {
				undoAction(board, nextAction);
			}
	
			int cap_row = (action.from[0] + action.to[0]) / 2;
			int cap_col = (action.from[1] + action.to[1]) / 2;
			board[cap_row][cap_col] = action.capturedPiece;
	
		}
		board[action.from[0]][action.from[1]] = piece;
		board[action.to[0]][action.to[1]] = '.';
	}


	/**
	 * Convert board char[][] into single line String or multiline
	 * @param board - char[][] current game state 
	 * @param sameLine - boolean return string on the same line or not
	 * @return
	 */
	public static String boardToString(char[][] board, boolean sameLine) {
		String output = "";
		if (sameLine) {
			for (char[] row : board)
				output += String.valueOf(row);
		}else {
			for (int row = 0; row < n-1; row ++) {
				output += String.valueOf(board[row]) + "\n";
			}
			output += String.valueOf(board[n-1]);
		}
		return output;
	}


	/**
	 * Print the board and display row and col in checker board format
	 * @param board - char[][] current game state  
	 */
	public static void printBoard(char[][] board) {
		System.out.println(" |a-b-c-d-e-f-g-h|");
		for (int i = 0; i < n; i++) {
			System.out.print((8-i)+"|");
			for(char c : board[i]) {
				System.out.print(c+"|");
			}
			System.out.println();
		}
		System.out.println(" |a-b-c-d-e-f-g-h|");
	}


	/**
	 * Print the board and display row and col in array format
	 * @param board - char[][] current game state 
	 */
	public static void debugBoard(char[][] board) {
		System.out.println(" |0-1-2-3-4-5-6-7|");
		for (int i = 0; i < n; i++) {
			System.out.print(i+"|");
			for(char c : board[i]) {
				System.out.print(c+"|");
			}
			System.out.println();
		}
		System.out.println(" |0-1-2-3-4-5-6-7|");
	}

	/**
	 * Copy game state to a new Object and return it
	 * @param board - char[][] current game state 
	 * @return a new board in the same state
	 */
	public static char[][] copyBoard(char[][] board) {
		char[][] copy = new char[n][n];
		for (int r = 0; r < n; r++)
			for (int c = 0; c < n; c++)
				copy[r][c] = board[r][c];
		return copy;
	}
}
