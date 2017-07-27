package eu.bcvsolutions.forest.index.service.api;

import java.io.Serializable;
import java.util.UUID;

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
	 * @param contentId
	 * @param parentContentId content's parent id
	 * @return
	 */
	IX index(String forestTreeType, CONTENT_ID contentId, CONTENT_ID parentContentId);
	
	/**
	 * Drops index for given content.
	 * 
	 * @param contentId
	 * @return
	 */
	IX dropIndex(CONTENT_ID contentId);
}
