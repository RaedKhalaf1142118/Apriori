import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class IApriori {
	/** the current level k in the breadth-first search */
	protected int k; 

	/** total number of candidates */
	protected int totalCandidateCount = 0; 
	
	/** total FIS count*/
	protected int totalFISCount =0;
	
	/** number of candidate generated during last execution */
	protected long startTimestamp; //

	/**  start time of last execution */
	protected long endTimestamp; //

	/** item set found during last execution */
	private int databaseSize;

	/** the minimum support set by the user */
	private double minsup;
	
	/** the source of the data */
	TransactionalDataSet transactionalDataSet;
	
	/** the list of FIS */
	List<List<Integer>> FISs;
	
	/** the list of candidates */
	List<List<Integer>> candidates;
	
	public IApriori(TransactionalDataSet transactionalDataSet) {
		this.transactionalDataSet = transactionalDataSet;
		this.FISs = new ArrayList<>(5000);
		this.candidates = new ArrayList<>(5000);
	}
	
	public void runAlgorithm(double minsup) {
		startTimestamp = System.currentTimeMillis();
		this.minsup = minsup;
		System.out.println("Start Algorithm");
		this.transactionalDataSet.prepareData();
		this.databaseSize = this.transactionalDataSet.allTransactionsSize;
		
		// we start looking for itemset of size 1
		List<Integer> frequent1 = this.transactionalDataSet.getSingletonFIS(minsup);
		for(Integer itemId : frequent1) {
			List<Integer> set = new ArrayList<>(1);
			set.add(itemId);
			this.FISs.add(set);
		}
		this.totalFISCount += this.FISs.size();
		this.startAlgorithm();
	}
	
	private void startAlgorithm() {
		k = 1;
		while(!this.FISs.isEmpty()) {
			k++;
			System.out.println(k+"th stage");
			this.generateCandidate();
			this.generateFISs();
		}
		endTimestamp = System.currentTimeMillis();
		this.transactionalDataSet.outputFile.close();
		this.printStats();
	}
	
	public void printStats() {
		System.out.println("=============  APRIORI - STATS =============");
		System.out.println(" Min Support : "+ this.minsup);
		System.out.println(" Candidates count : " + totalCandidateCount);
		System.out.println(" The algorithm stopped at size " + (k - 1));
		System.out.println(" Frequent itemsets count : " + this.totalFISCount);
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println("===================================================");
	}
	
	private void generateFISs() {
		this.FISs.clear();
		int i =0;
		for(List<Integer> itemset : this.candidates) {
			Integer[] includedItems = new Integer[itemset.size()];
			itemset.toArray(includedItems);
			double support = this.transactionalDataSet.getSupportOfItemSet(new IItemset(this.transactionalDataSet, includedItems),minsup, k);
			if(support >= this.minsup) {
				this.FISs.add(itemset);
				this.transactionalDataSet.outputFile.println(itemset +" #SUP "+support);
			}
		}
		this.transactionalDataSet.prevRetained = this.transactionalDataSet.newRetained;
		this.transactionalDataSet.newRetained = new HashMap<>(1000);
		this.totalFISCount += this.FISs.size();
	}
	
	private void logResult() {
		for(List<Integer> FIS : this.FISs) {
			System.out.print("[ ");
			for(Integer item : FIS) {
				System.out.print(item +" ");
			}
			System.out.println(" ]");
		}
	}
	
	private void generateCandidate() {
		this.candidates.clear();
		if(k == 2){
			this.generateCandidateSize2();
		}else{
			this.generateCandidateSizeK();
		}
		this.totalCandidateCount += this.candidates.size();
	}
	
	private void generateCandidateSize2() {
		this.candidates.clear();
		for(int i = 0 ; i < this.FISs.size() ; i++) {
			Integer item1 = this.FISs.get(i).iterator().next();
			for(int j = i + 1; j < this.FISs.size() ; j++) {
				Integer item2 = this.FISs.get(j).iterator().next();
				List<Integer> newCandidate = new ArrayList<>(2);
				newCandidate.add(item1);
				newCandidate.add(item2);
				this.candidates.add(newCandidate);
			}
		}
	}
	
	private void generateCandidateSizeK() {
		loop1 : for(int i = 0; i < this.FISs.size(); i++){
			List<Integer> itemset1 = this.FISs.get(i);
			loop2 : for(int j = i + 1; j < this.FISs.size() ; j++) {
				List<Integer> itemset2 = this.FISs.get(j);
				for(int k = 0; k < itemset1.size() ; k++){
					if(k == itemset1.size() - 1){
						if(itemset1.get(k) >= itemset2.get(k)) {
							continue loop1;
						}
					}
					else if(itemset1.get(k) < itemset2.get(k)) {
						continue loop2;
					}else if(itemset1.get(k) > itemset2.get(k)) {
						continue loop1;
					}
				}
				
				List<Integer> newItemset = new ArrayList<>(itemset1.size()+1);
				newItemset.addAll(itemset1);
				newItemset.add(itemset2.get(itemset2.size()-1));
				
				if(this.allSubsetsOfSizeK_1AreFrequent(newItemset)) {
					this.candidates.add(newItemset);
				}
			}
		}
	}
	
	private boolean allSubsetsOfSizeK_1AreFrequent(List<Integer> candidate) {
		for(int posRemoved = 0; posRemoved < candidate.size(); posRemoved++) {
			int first = 0;
			int last = this.FISs.size() - 1;
			
			boolean found = false;
			while(first <= last) {
				int middle = (first + last ) >> 1;
				int comparison = this.sameAs(this.FISs.get(middle), candidate, posRemoved);
				if(comparison < 0) {
					first = middle + 1;
				}else if(comparison > 0) {
					last = middle - 1;
				}else {
					found = true;
					break;
				}
			}
			
			if(found == false) {
				return false;
			}
		}
		return true;
	}
	
	public int sameAs(List<Integer> itemset1, List<Integer>itemsets2, int posRemoved) {
		// a variable to know which item from candidate we are currently searching
		int j=0;
		// loop on items from "itemset"
		for(int i=0; i<itemset1.size(); i++){
			// if it is the item that we should ignore, we skip it
			if(j == posRemoved){
				j++;
			}
			// if we found the item j, we will search the next one
			if(itemset1.get(i) == itemsets2.get(j)){
				j++;
			// if  the current item from i is larger than j, 
		    // it means that "itemset" is larger according to lexical order
			// so we return 1
			}else if (itemset1.get(i) > itemsets2.get(j)){
				return 1;
			}else{
				// otherwise "itemset" is smaller so we return -1.
				return -1;
			}
		}
		return 0;
	}

}
