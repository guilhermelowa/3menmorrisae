package environment;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Running {
	
	// Environment variables
    private static final int POPULATION_SIZE = 100;
    private static double ALLOWED_BREED = 0.3; // Parents percentage
    private static final int NUMBER_OF_GENERATIONS = 200;
    private static final double PROGRESS_STEP = 500; // Show progress in NUMBER_OF_GENERATIONS / PROGRESS STEP. Also save opponents in same step
    private static final int SAVED_OPPONENTS_NUMBER = 5;
    private static final boolean USE_RANDOM = false; // Use random opponents?
    private static final double MUTATION_RATE = 0.1; // Mutation percentage
    private static final int NUMBER_OF_SAMPLES = 10;
    private static Random random = new Random();
    
    // Tourney definitions
    private static final int OPPONENTS_AMOUNT = 100; // Plays an opponent twice. Putting first piece/not putting first piece.
    private static final int WIN_POINTS = 3;
    private static final int DRAW_POINTS = 1;
    private static final int LOSE_POINTS = 0;

    // Persistance variables 
    private static String file_name = new String("");
    private static GraphGenerator fitnessGraph;
    private static GraphGenerator statsGraph;
    private static boolean[] fitGraphClosed = new boolean[Running.NUMBER_OF_SAMPLES];
	private static boolean[] statsGraphClosed = new boolean[Running.NUMBER_OF_SAMPLES];
    
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		
		Arrays.fill(fitGraphClosed, false);
		Arrays.fill(statsGraphClosed, false);

        // OBJECTS
        // Generate the board/position representations
		Representation representation = new Representation();
		
		// Save configurations to name the files
        String configs = new String("P" + Running.POPULATION_SIZE + "A" + Running.ALLOWED_BREED + "G" + Running.NUMBER_OF_GENERATIONS + "M" + Running.MUTATION_RATE + "OP" + Running.OPPONENTS_AMOUNT);
		
		// Save best players from each run to compare them. See how much they differ:
        FileOutputStream bestPlayersFile = new FileOutputStream(configs + "_best.ser");
        ObjectOutputStream bestOut = new ObjectOutputStream(bestPlayersFile);
        
        // If this is a run to generate an opponent pool, uncomment the following lines between /* -- */: CTRL + F to find other lines to uncomment
        // File to output opponents.
        /*
        FileOutputStream opponentsFile = new FileOutputStream("opponents.ser");
        ObjectOutputStream out = new ObjectOutputStream(opponentsFile);
        int opponentsWritten = 0;
        */
        
        // GENERATING BENCHMARK PLAYERS. CAN BE EITHER RANDOM OR EVOLVED OPPONENTS.
        System.out.println("Generating benchmark players");
        ArrayList<Player> opponents = new ArrayList<Player>();
        
        if (Running.USE_RANDOM) {
	        // Generating random opponents:
	        for (int i=0; i<Running.OPPONENTS_AMOUNT; i++){
	            opponents.add(new ArrayPlayer(representation));
	        }
        } else {
	        // Load evolved opponents:
	        FileInputStream opponentsIn = new FileInputStream("opponents.ser");
	        ObjectInputStream in = new ObjectInputStream(opponentsIn);
	        int opponentsRead = 0;

	        try {
	            ArrayPlayer e;
				while( (e= (ArrayPlayer)in.readObject()) != null) {
	            	opponents.add(e);
	            	opponentsRead++;
	            }
	        } catch (IOException ioex){
	        	ioex.printStackTrace();
	        }
	        System.out.println("Opponents read: " + opponentsRead);
        }
        
	    // EVOLUTION VARIABLES
        // Players arraylist
        ArrayList <Player> newPlayers = new ArrayList<Player>();        

        // Generate first generation        
        System.out.println("Generating first generation!");
        for (int i=0; i<Running.POPULATION_SIZE; i++){
            newPlayers.add(new ArrayPlayer(representation));
        }
        
        // Run experiments samples based on NUMBER_OF_SAMPLES
	    for(int h=0; h < Running.NUMBER_OF_SAMPLES; h++) {

		   	// GRAPHS VARIABLES
		   	file_name = "Sample" + (h+1);
			   
	      	fitnessGraph = new GraphGenerator("Fitness x Generations");
	      	statsGraph = new GraphGenerator("Statistics x Generations");
      	 
		    // Create XYSeries for graphs:
	        fitnessGraph.createSeries("Max Fitness");
	        fitnessGraph.createSeries("Average Fitness");
	        statsGraph.createSeries("Average Wins");
	        statsGraph.createSeries("Average Draws");
	        statsGraph.createSeries("Average Losses");
	        statsGraph.createSeries("Best Player's Wins");
	        statsGraph.createSeries("Best Player's Draws");
	        statsGraph.createSeries("Best Player's Losses");
	        statsGraph.createSeries("Max Number of Wins");		   
	        // - - - - - - -
	        
		    // EVOLUTION VARIABLES
	        // Players arraylist
	        ArrayList <Player> players = new ArrayList<Player>(newPlayers);        
		   
	        // - - - - - BEGINING OF GENERATIONS - - - - -
		   
	        for (int i=0; i<Running.NUMBER_OF_GENERATIONS; i++) {
	        	
	        	// EVALUATION METRICS
	    		int MAX_FITNESS = 0;
	    		double AVG_FITNESS = 0;
	            int maxWins = 0;
	            int numWinsBestPlayer = 0;
	            int numDrawsBestPlayer = 0;
	            int numLossesBestPlayer = 0;
	            double avgWins = 0;
	            double avgDraws = 0;
	            double avgLosses = 0;
	
	        	// Each player plays in a tourney to determine fitness
	        	for (int j=0; j<Running.POPULATION_SIZE; j++) {
	        		Player tourneyPlayer = players.get(j);
	
	                // Reset status from previous generation
	        		tourneyPlayer.setFitness(0); // Reset fitness from previous tourneys
	                tourneyPlayer.wins = 0;
	                tourneyPlayer.draws = 0;
	                tourneyPlayer.losses = 0;
	        		
	        		// Select opponents and play games to evaluate fitness;
	        		int newFitness = 0;
	                int newWins = 0;
	                int newDraws = 0;
	                int newLosses = 0;
	

	        		for (int k=0; k<Running.OPPONENTS_AMOUNT; k++) {

	        			Game game = new Game(tourneyPlayer, opponents.get(k), representation);
	                    int winner = game.play();
	                    //System.out.println("Ganhador do jogo entre " + tourneyPlayer.getId() + " e " + opponent.getId() + " foi: " + winner);
	        			if (winner == tourneyPlayer.getId()) {
	        				newFitness += Running.WIN_POINTS;
	                        newWins++;
	        			} else if (winner == -1) {
	        				newFitness += Running.DRAW_POINTS;
	                        newDraws++;
	        			} else {
	        				newFitness += Running.LOSE_POINTS;
	                        newLosses++;
	        			}
	        			game = new Game(opponents.get(k), tourneyPlayer, representation);
	        			winner = game.play();
	        			//System.out.println("Ganhador do jogo entre " + opponent.getId() + " e " + tourneyPlayer.getId() + " foi: " + winner);
	        			if (winner == tourneyPlayer.getId()) {
	        				newFitness += Running.WIN_POINTS;
	                        newWins++;
	        			} else if (winner == -1) {
	        				newFitness += Running.DRAW_POINTS;
	                        newDraws++;
	        			} else {
	        				newFitness += Running.LOSE_POINTS;
	                        newLosses++;
	        			}
	        		}
	        		
	                // Give newFitness and status
	        		tourneyPlayer.setFitness(newFitness);
	        		
	        		// EVALUATION METRICS
	
	                    // Best player metrics
	        		if (newFitness > MAX_FITNESS) {
	        			MAX_FITNESS = newFitness;
	                    numWinsBestPlayer = newWins;
	                    numDrawsBestPlayer = newDraws;
	                    numLossesBestPlayer = newLosses;
	        		}
	
	                    // Max wins
	                if (newWins > maxWins){
	                    maxWins = newWins;
	                }
	
	                    // Avg status
	        		AVG_FITNESS += newFitness;
	                avgWins += newWins;
	                avgDraws += newDraws;
	                avgLosses += newLosses;
	        	}
	
	        	AVG_FITNESS /= Running.POPULATION_SIZE;
	            avgWins /= Running.POPULATION_SIZE;
	            avgDraws /= Running.POPULATION_SIZE;
	            avgLosses /= Running.POPULATION_SIZE;
	        	
	            
	    		// After everyone played their games, sort based on fitness and breed the best.
	    		players.sort(null);
	    		
	    		
	        	if (i%Running.PROGRESS_STEP == 0) {
	        		System.out.println("Geracao: " + i);
	                // If this is a run to generate an opponent pool, uncomment the following lines between /* -- */: CTRL + F to find other lines to uncomment
	        		/*
	        		// ops determine the number of opponents of a generation to be saved
	                for (int ops = 0; ops < Running.SAVED_OPPONENTS_NUMBER; ops++){
	                    try{
	                        out.writeObject(players.get(ops));
	                        opponentsWritten++;
	                    } catch (IOException ioex){
	                        ioex.printStackTrace();
	                    }
	                }
	                */  
	        	}
	        	
	    		
	    		
	    		double[] fit_array = {MAX_FITNESS, AVG_FITNESS}; 
	    		fitnessGraph.addValue((double) i, fit_array);
	    	
	    		double[] stats_array = {avgWins, avgDraws, avgLosses, numWinsBestPlayer, numDrawsBestPlayer, numLossesBestPlayer, maxWins};
	    		statsGraph.addValue((double) i, stats_array);
	    		
	    		// Select %ALLOWED_BREED parents to breed and fill the remainder of the population
	    		int parent = 0;
	    		int allowedToBreed = (int) (Running.POPULATION_SIZE * Running.ALLOWED_BREED);
	    		for (int b = allowedToBreed; b < Running.POPULATION_SIZE; b++) {
	    			// Parent one is determined. Parent two is random from the allowed to breed ones.
	    			// TODO: Decrease changes from parent two according to arraylist index.
	    			Player newPlayer = new ArrayPlayer(representation, (ArrayPlayer) players.get(parent), (ArrayPlayer) players.get(random.nextInt(allowedToBreed)), Running.MUTATION_RATE, random);
	    			players.set(b, newPlayer);
	    			parent++;
	    			if (parent > allowedToBreed -1)
	    				parent = 0;
	    		}
	        } // Generations loop end
	        
     	    // If this is a run to generate an opponent pool, uncomment the following lines between /* -- */: CTRL + F to find other lines to uncomment
            /*
    		//System.out.println("Opponents written: " + opponentsWritten);
            out.close();
            opponentsFile.close();
	        */
	    
	        // Save the best chromossome from each run to compare them. See how much they differ.
            try{
                bestOut.writeObject( ((ArrayPlayer) players.get(0)).getPlaybook() );
            } catch (IOException ioex){
                ioex.printStackTrace();
            }
            

            // Generate graphs
            final int num = h;
	        fitnessGraph.generateGraph("Fitness");
	        fitnessGraph.pack();
	        fitnessGraph.addWindowListener(new WindowAdapter() {
	        	@Override
				public void windowClosing(WindowEvent e) {
	        		fitGraphClosed[num] = true;
	        	}
	        });
	        fitnessGraph.setVisible(true);
	        
	        // Putting the thread to sleep for 2 seconds to avoid ConcurrentModificationException when trying to save graph
	        try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	        
	        
	        fitnessGraph.savePNGFile(configs + file_name + "fit");
	        
	        statsGraph.generateGraph("Statistics");
	        statsGraph.pack();
	        statsGraph.addWindowListener(new WindowAdapter() {
	        	@Override
				public void windowClosing(WindowEvent e) {
	        		statsGraphClosed[num] = true;
	        	}
	        });
	        statsGraph.setVisible(true);
	        
	         try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	        
	        statsGraph.savePNGFile(configs + file_name + "stats");
	        Running.ALLOWED_BREED += 0.10;
       } // Samples loop end
	   
	    // Closing proceedure (Eclipse wasn't closing the runs/graphs properly. Can't remember why this is here)
	    while(true) {
	    	boolean allClosed = false;
	    	for(int samples = 0; samples<Running.NUMBER_OF_SAMPLES; samples++) {
	    		allClosed = statsGraphClosed[samples];
	    		if (allClosed == false) break;
	    		allClosed = fitGraphClosed[samples];
	    		if (allClosed == false) break;
	    	}
    	   if(allClosed) {
               bestOut.close();
               bestPlayersFile.close();
    		   System.out.println("Ate mais e obrigado pelos peixes");
    		   System.exit(0);
    	   } 
 	   }
	} // Main end
} // Class end
