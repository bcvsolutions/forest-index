package eu.bcvsolutions.forest.index.service.api;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import eu.bcvsolutions.forest.index.domain.ForestContent;
import eu.bcvsolutions.forest.index.domain.ForestIndex;

/**
 * Tree content with support for forest index
 * 
 * @author Radek Tomiška
 *
 * @param <C> content type
 * @param <IX> index type
 * @param <CONTENT_ID> content identifier - e.g. {@code Long} or {@link UUID} is preferred
 */
public interface ForestContentService<C extends ForestContent<C, IX, CONTENT_ID>, IX extends ForestIndex<IX, CONTENT_ID>, CONTENT_ID extends Serializable> {
	
	/**
	 * Rebuild (drop and create) all indexes for given forestTreeType.
	 * @param forestTreeType
	 */
	void rebuildIndexes(String forestTreeType);
	
	/**
	 * Creates index for given content.
	 * 
	 * @param content
	 * @return
	 */
	C createIndex(C content);
	
	/**
	 * Updates index for given content.
	 * 
	 * @param content
	 * @return
	 */
	C updateIndex(C content);
	
	/**
	 * Deletes indexes for given content.
	 * 
	 * @param content
	 * @return
	 */
	C deleteIndex(C content);
	
	/**
	 * Finds roots
	 * 
	 * @param forestTreeType
	 * @param pageable
	 * @return
	 */
	Page<C> findRoots(String forestTreeType, Pageable pageable);
	
	/**
	 * Finds direct children for given parent
	 * 
	 * @param parent
	 * @param pageable
	 * @return
	 */
	Page<C> findDirectChildren(C parent, Pageable pageable);
	
	/**
	 * Finds all children for given parent r
	 * ecursively by forest index
	 * 
	 * @param parent
	 * @param pageable
	 * @return
	 */
	Page<C> findAllChildren(C parent, Pageable pageable);
	
	/**
	 * Returns all content parents
	 * 
	 * @param content
	 * @return
	 */
	List<C> findAllParents(C content, Sort sort);

}
