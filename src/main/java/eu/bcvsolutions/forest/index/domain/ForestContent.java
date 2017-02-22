package eu.bcvsolutions.forest.index.domain;

import java.io.Serializable;
import java.util.UUID;

/**
 * Tree content with support for forest index
 * 
 * @author Radek Tomiška
 * @param <C> content type
 * @param <IX> index type
 * @param <CONTENT_ID> content identifier - e.g. {@code Long} or {@link UUID} is preferred
 */
public interface ForestContent<C extends ForestContent<C, IX, CONTENT_ID>, IX extends ForestIndex<IX, CONTENT_ID>, CONTENT_ID extends Serializable>
		extends Serializable {

	/**
	 * Content id
	 * 
	 * @return
	 */
	CONTENT_ID getId();
	
	/**
	 * Content parent
	 * 
	 * @return
	 */
	C getParent();
	
	void setParent(C parent);
	
	/**
	 * Content index
	 * 
	 * @return
	 */
	IX getForestIndex();

	void setForestIndex(IX forestIndex);
	
	/**
	 * Tree type
	 * 
	 * @return
	 */
	String getForestTreeType();
}
