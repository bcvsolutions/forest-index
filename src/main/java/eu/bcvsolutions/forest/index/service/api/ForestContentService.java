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
public interface ForestContentService<C extends ForestContent<IX, CONTENT_ID>, IX extends ForestIndex<IX, CONTENT_ID>, CONTENT_ID extends Serializable> {
	
	/**
	 * Rebuild (drop and create) all indexes for given forestTreeType.
	 * 
	 * Index has to be rebuilt respectively tree structure - from root to children.
	 * 
	 * @param forestTreeType
	 */
	void rebuildIndexes(String forestTreeType);
	
	/**
	 * Creates index for given content.
	 * 
	 * @param contentId
	 * @param parentContentId content's parent id
	 * @return
	 */
	IX createIndex(String forestTreeType, CONTENT_ID contentId, CONTENT_ID parentContentId);
	
	/**
	 * Updates index for given content.
	 * 
	 * Make sure parent is correctly indexed.
	 * 
	 * @param contentId
	 * @param parentContentId content's parent id
	 * @return
	 */
	IX updateIndex(String forestTreeType, CONTENT_ID contentId, CONTENT_ID parentContentId);
	
	/**
	 * Deletes indexes for given content.
	 * 
	 * @param content
	 * @return
	 */
	IX deleteIndex(CONTENT_ID contentId);
	
	/**
	 * Finds roots.
	 * 
	 * @param forestTreeType
	 * @param pageable
	 * @return
	 */
	Page<C> findRoots(String forestTreeType, Pageable pageable);
	
	/**
	 * Finds direct children for given content.
	 * 
	 * @param contentId
	 * @param pageable
	 * @return
	 */
	Page<C> findDirectChildren(CONTENT_ID contentId, Pageable pageable);
	
	/**
	 * Finds all children for given content recursively by forest index
	 * 
	 * @param contentId
	 * @param pageable
	 * @return
	 */
	Page<C> findAllChildren(CONTENT_ID contentId, Pageable pageable);
	
	/**
	 * Returns all content parents.
	 * 
	 * @param content
	 * @return
	 */
	List<C> findAllParents(CONTENT_ID contentId, Sort sort);

}
