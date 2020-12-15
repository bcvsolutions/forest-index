package eu.bcvsolutions.forest.index.domain;

import java.io.Serializable;
import java.util.UUID;

/**
 * Persists forest index. Forest index can hold more trees with different type.
 * 
 * @author Radek Tomiška
 * @param <IX> {@link ForestIndex} type - used for parent etc.
 * @param <CONTENT_ID> content identifier - e.g. {@code Long} or {@link UUID} is preferred
 */
public interface ForestIndex<IX extends ForestIndex<IX, CONTENT_ID>, CONTENT_ID extends Serializable> {
	
	static final String DEFAULT_TREE_TYPE = "default";
	
	/**
	 * Internal index identifier.
	 * 
	 * @return identifier
	 */
	Long getId();
	
	/**
	 * Reference to indexed content.
	 * 
	 * @return content identifier
	 */
	CONTENT_ID getContentId();
	
	/**
	 * Reference to indexed content.
	 * 
	 * @param contentId content identifier
	 */
	void setContentId(CONTENT_ID contentId);
	
	/**
	 * Parent forest index (node).
	 * 
	 * @return parent index
	 */
	IX getParent();

	/**
	 * Parent forest index (node).
	 * 
	 * @param parent parent index
	 */
	void setParent(IX parent);
	
	/**
	 * Tree type.
	 * 
	 * @return tree type
	 */
	String getForestTreeType();
	
	/**
	 * Tree type.
	 * 
	 * @param treeType tree type
	 */
	void setForestTreeType(String treeType); 
	
	/**
	 * Left node index.
	 * 
	 * @return lft index
	 */
	Long getLft();
	
	/**
	 * Left node index.
	 * 
	 * @param lft lft index
	 */
	void setLft(Long lft);
	
	/**
	 * Right node index.
	 * 
	 * @return rgt index
	 */
	Long getRgt();
	
	/**
	 * Right node index.
	 * 
	 * @param rgt rgt index
	 */
	void setRgt(Long rgt);
	
	/**
	 * Returns all children count (recursively)
	 * 
	 * @return
	 */
	int getChildrenCount();
}
