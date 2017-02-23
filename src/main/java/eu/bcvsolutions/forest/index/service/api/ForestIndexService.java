package eu.bcvsolutions.forest.index.service.api;

import java.io.Serializable;
import java.util.UUID;

import eu.bcvsolutions.forest.index.domain.ForestContent;
import eu.bcvsolutions.forest.index.domain.ForestIndex;

/**
 * Persists, builds, clears forest indexes
 * 
 * @author Radek Tomiška
 *
 * @param <IX> {@link ForestIndex} type - used for parent etc.
 * @param <CONTENT_ID> content identifier - e.g. {@code Long} or {@link UUID} is preferred
 */
public interface ForestIndexService<IX extends ForestIndex<IX, CONTENT_ID>, CONTENT_ID extends Serializable> {
	
	/**
	 * Rebuild all indexes (clears lft and rgt) of given type. 
	 * 
	 * @param forestTreeType
	 */
	void rebuild(String forestTreeType);
	
	/**
	 * Saves given index
	 * 
	 * @param forestIndex
	 * @return
	 */
	IX saveNode(IX forestIndex);
	
	/**
	 * Deletes given forest index
	 * 
	 * @param forestIndex
	 * @param closeGap true - recount indexes, false - leave indexes as it is (indexes could be used fr searching children, bet not for children count)
	 */
	void deleteNode(IX forestIndex, boolean closeGap);
	
	/**
	 * Clear all indexes lft and rgt of given type
	 * 
	 * @param forestTreeType
	 */
	void clearIndexes(String forestTreeType);
	
	/**
	 * Drop all indexes of given type
	 * 
	 * @param forestTreeType
	 */
	void dropIndexes(String forestTreeType);
	
	/**
	 * Creates or updates index for given content.
	 * 
	 * @param content
	 * @return
	 */
	<C extends ForestContent<C, IX, CONTENT_ID>> C index(C content);
	
	/**
	 * Drops index for given content.
	 * 
	 * @param content
	 * @return
	 */
	<C extends ForestContent<C, IX, CONTENT_ID>> C dropIndex(C content);
}
