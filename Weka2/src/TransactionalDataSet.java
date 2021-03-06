import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * Encapsulate the connections with the database
 */
public class TransactionalDataSet{
	
	private String inputFilePath; // input file which holds transactions
	private String outputFilePath;// output file that holds the output (FISs)
	
	// the key in this map is the id of the item, and the value of it is set of transactions ids this item appears in
	Map<Integer, HashSet<Integer>> distinctItems = new HashMap<>(10000); // this will hold the items appears in the data set
	
	/** print the result to file */
	PrintWriter outputFile;
	
	int allTransactionsSize = 0;
	/**
	 * @param inputFilePath holds the path of the input file
	 * @param outputFilePath holds the path of the output file
	 */
	public TransactionalDataSet(String inputFilePath, String outputFilePath){
		this.inputFilePath = inputFilePath;
		this.outputFilePath = outputFilePath;
	}
	
	/**
	 * this method will read the transaction from the dataset
	 * and generate map which holds the distinct items
	 * in addition to the number of transactions in which the item exist
	 * this operation will be done by only one scan for the database
	 */
	public void prepareData() {
		this.readDataset();
		try {
			this.outputFile = new PrintWriter(this.outputFilePath);
		} catch (FileNotFoundException e) {
			System.out.println("Output file not Found");
		}
	}
	
	/**
	 * this method will take care of reading the database
	 */
	private void readDataset() {
		try{
			@SuppressWarnings("resource")
			BufferedReader input = new BufferedReader(new FileReader(this.inputFilePath));
			int transactionId = 0;
			while(input.ready()) {
				String line = input.readLine();
				if(line.matches("\\s*")) continue;
				StringTokenizer t = new StringTokenizer(line, " ");
				while(t.hasMoreTokens()) {
					int itemId = Integer.parseInt(t.nextToken());
					if(this.distinctItems.containsKey(itemId)) {
						if(!this.distinctItems.get(itemId).contains(transactionId)) {
							this.distinctItems.get(itemId).add(transactionId);
						}
					}else{
						HashSet<Integer> transactionsIds = new HashSet<>(500);
						transactionsIds.add(transactionId);
						this.distinctItems.put(itemId, transactionsIds);
					}
				}
				transactionId++;
				allTransactionsSize++;
			}
		}catch(Exception e){
			
		}
	}
	
	HashMap<String, HashSet<Integer>> prevRetained = new HashMap<>(10000);
	HashMap<String, HashSet<Integer>> newRetained = new HashMap<>(10000);

	public double getSupportOfItemSet(IItemset itemset, double minSupp, int k) {
		double support = 0;
		int item = itemset.includedItemsIds[itemset.includedItemsIds.length-1];
		HashSet<Integer> firstPart = this.prevRetained.get(itemset.toString(false));
		HashSet<Integer> secondPart = this.getTransactionsSetForItem(item);
		HashSet<Integer> result = (HashSet<Integer>)secondPart.clone();
		result.retainAll(firstPart);
		
		support = this.calculateSupportForIntersectSet(result);
		if(support >= minSupp) {
			this.newRetained.put(itemset.toString(true), result);
		}
		return support;

	}
	
	
	/**
	 * 
	 * @param minSupport: the minimum support threshold specified by the user
	 * @return: a set of items ids which support is more than the minimum support
	 */
	public List<Integer> getSingletonFIS(double minSupport) {
		List<Integer> FIS1 = new ArrayList<>(50);
		// iterate over the items over the distinct items map and check the FISs
		for(Map.Entry<Integer, HashSet<Integer>> entry : this.distinctItems.entrySet()) {
		    // get the item id and the set of transactions this item is in it
			Integer itemId = entry.getKey()+0;
		    HashSet<Integer> transactionsIds = entry.getValue();
		    
		    Integer itemTransactionsSize = transactionsIds.size();
			Integer allTransactionsSize = this.allTransactionsSize;
			double support = (itemTransactionsSize * 1.0) / allTransactionsSize; // multiplied by 1.0 to convert it to double
			if(support >= minSupport){
				this.prevRetained.put("["+itemId+",]", transactionsIds);
				this.outputFile.println("["+itemId+",] #SUP"+ support);
				FIS1.add(itemId);
			}
		}
		Collections.sort(FIS1, new Comparator<Integer>() {

			@Override
			public int compare(Integer o1, Integer o2) {
				return o1 - o2;
			}
			
		});
		return FIS1;
	}
	
	/**
	 * insert a record to the output file
	 * @param output: the record you want to add it to the database
	 */
	public void recordOutput(String output) {
		// TO-DO
		this.outputFilePath = this.outputFilePath + "";
	}
	
	/**
	 * 
	 * @param itemset: array of items ids
	 * @return the support integer value for the itemset 
	 */
	public double getSupportOfItemSet(IItemset itemset) {
		// initialize the support
		double support = 0;
		// define the transactionSet -> the result of intersection between the items transactions sets
		HashSet<Integer> transactionSet = new HashSet<>(1000);
		int index = 0;
		for(Integer itemId: itemset.includedItemsIds) {
			// if it is the first iteration initialize the result set with the first transaction set
			if(index == 0){
				transactionSet = (HashSet<Integer>) this.getTransactionsSetForItem(itemId).clone();
			}else{
				// other wise, intersect the two sets to get the common transactions ids
				transactionSet.retainAll(this.getTransactionsSetForItem(itemId));
			}
			index++;
		}
		// calculate the support for the common transactions
		support = this.calculateSupportForIntersectSet(transactionSet);
		return support;
	}
	
	/**
	 * 
	 * @param transactionSet the set of transactions which the itemset exist in
	 * @return Double support number
	 */
	private double calculateSupportForIntersectSet(HashSet<Integer> transactionSet) {
		return (transactionSet.size() * 1.0) / allTransactionsSize; // multiplied by 1.0 to convert it to double
	}
	
	
	/**
	 * @param item id
	 * @return a set of transactions ids which the item involve in
	 */
	private HashSet<Integer> getTransactionsSetForItem(Integer item) {
		return this.distinctItems.get(item);
	}
	
}
