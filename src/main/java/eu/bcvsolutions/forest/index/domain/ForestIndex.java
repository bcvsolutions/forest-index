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
	 * Internal index identifier
	 * 
	 * @return
	 */
	Long getId();
	
	/**
	 * Reference to indexed content
	 * 
	 * @return
	 */
	CONTENT_ID getContentId();
	
	void setContentId(CONTENT_ID contentId);
	
	/**
	 * Parent index (node)
	 * @return
	 */
	IX getParent();

	void setParent(IX parent);
	
	/**
	 * Tree type
	 * 
	 * @return
	 */
	String getForestTreeType();
	
	void setForestTreeType(String treeType); 
	
	/**
	 * Left node index
	 * 
	 * @return
	 */
	Long getLft();
	
	void setLft(Long lft);
	
	/**
	 * Right node index
	 * 
	 * @return
	 */
	Long getRgt();
	
	void setRgt(Long rgt);
	
	/**
	 * Returns all children count (recursively)
	 * 
	 * @return
	 */
	int getChildrenCount();
}
