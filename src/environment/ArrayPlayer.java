package environment;

import java.util.ArrayList;
import java.util.Random;

/***
	Player that has all possible board configurations and the play to it.
***/
public class ArrayPlayer extends Player {

	private ArrayList<String> playbook;

	public ArrayPlayer (Representation representation){
		this.playerId = Player.id;
		Player.id++;
		this.playbook = Mutation.generatePlays(representation);
	}
	
	public ArrayPlayer (Representation representation, ArrayPlayer parentOne, ArrayPlayer parentTwo, double mutationRate, Random random) {
		this.playerId = Player.id;
		Player.id++;
	
		this.playbook = parentOne.getPlaybook();
		
		// 564 positions on the playbook. Raffle how many from parentTwo:
		int genesFromTwo = random.nextInt(564) + 1;
		
		// Raffle which genes will come from parentTwo:
		for (int i=0; i<genesFromTwo; i++) {
			int index = random.nextInt(564);
			this.playbook.set(index, parentTwo.getPlaybook().get(index));
		}
		
		// Mutate based on mutation rate
		for (int i=0; i<(int)(564*mutationRate); i++ ) {
			int index = random.nextInt(564);
			this.playbook.set(index, Mutation.generatePlay(representation, index));
		}

	}
	

	private ArrayList<String> getPlaybook() {
		return this.playbook;
	}

	/***
		This function gets the next play.
		In a given board configuration, first find what is the "correct" representation in the hashmap.
		Pass the hash to the hashmap and obtain the array index.
		Use the given index to find the play in the player chromossome
	***/
	@Override
	public String getNextPlay(Representation representation, char[] board) {
		// Get current position
		String currentBoard = new String(board);
		
		// Check current position definitive representation index
		int playIndex = representation.getIndex(currentBoard);

		System.out.println("Indice da jogada eh: " + playIndex);
		System.out.println("Posicao do tabuleiro eh: " + representation.getPosition(playIndex));		

		String nextPlay = this.playbook.get(playIndex);

		System.out.println("Proxima jogada eh: " + nextPlay);

		// Get next play from player`s playbook from previous index
		return this.playbook.get(playIndex);
	}
	
	@Override
	public int compareTo(Object opponent) {
		opponent = (Player) opponent;
		if (this.getFitness() > ((Player) opponent).getFitness()) {
			return 1;
		}
		else if (this.getFitness() < ((Player) opponent).getFitness() ) {
			return -1;
		} else {
			return 0;
		}
	}
}