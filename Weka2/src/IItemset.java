import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class IItemset {
	
	/**
	 * items: Map that contains the ids of the items, and the a boolean which denotes for include/not include
	 */
	Map<Integer, Boolean> items;
	
	// database
	TransactionalDataSet transactionalDataSet;
	
	Integer[] includedItemsIds;
	
	/**
	 * 
	 * @param transactionalDataSet: a reference for the {@link TransactionalDataSet}
	 * @param includedItemsIds: ids of the included items
	 */
	public IItemset(TransactionalDataSet transactionalDataSet, Integer[] includedItemsIds) {
		this.transactionalDataSet = transactionalDataSet;
		this.includedItemsIds = includedItemsIds;
		this.init();
	}
	
	/**
	 * this method is responsible for initializing the map
	 */
	private void init() {
		// initiate the items map
		this.items = new HashMap<>();
		// iterate over the items from the distinctItems map
		for(Map.Entry<Integer, HashSet<Integer>> entry : this.transactionalDataSet.distinctItems.entrySet()) {
		    Integer itemId = entry.getKey();
		    // set the initial value of the items to false
		    this.items.put(itemId, false);
		}
		// set the value of included item to true
		for(Integer itemId : includedItemsIds) {
			this.items.put(itemId, true);
		}
	}
	
	public String toString(boolean addAll) {
		String result = "[";
		for (int i = 0; i < includedItemsIds.length; i++) {
			if(i == includedItemsIds.length -1 && !addAll) break;
			result += includedItemsIds[i] +",";
		}
		return result +  "]";
	}
}
