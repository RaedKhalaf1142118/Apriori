import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class Driver {
	public static void main(String[] args) throws IOException {
		enhancedApriori(0.02);
		System.out.println("---------------------------------------------------");
		originalApriori(0.02);
	}
	
	private static void enhancedApriori(double minSupp) {
		TransactionalDataSet transactionalDataSet = new TransactionalDataSet("transactions.txt", "enhancedApriori.txt");
		IApriori iApriori = new IApriori(transactionalDataSet);
		iApriori.runAlgorithm(minSupp);
	}
	
	private static void originalApriori(double minSupp) throws IOException{
		AlgoApriori algoApriori = new AlgoApriori();
		Itemsets itemsets = algoApriori.runAlgorithm(minSupp, "transactions.txt", "originalApriori.txt");
		algoApriori.printStats();
	}
}
