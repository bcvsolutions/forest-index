package eu.bcvsolutions.forest.index.service;

import eu.bcvsolutions.forest.index.entity.ForestIndexEntity;
import eu.bcvsolutions.forest.index.entity.NodeContent;
import eu.bcvsolutions.forest.index.service.api.ForestContentService;

/**
 * Test tree content CRUD service. Index is used and recounted.
 * 
 * @author Radek Tomiška
 *
 */
public interface NodeContentService extends ForestContentService<NodeContent, ForestIndexEntity, Long> {

	/**
	 * Read node content by given id.
	 * 
	 * @param id
	 * @return
	 */
	NodeContent get(Long id);
	
	/**
	 * Save node content. 
	 * 
	 * @param content
	 * @return
	 */
	NodeContent save(NodeContent content);
	
	/**
	 * Delete node content.
	 * 
	 * @param content
	 */
	void delete(NodeContent content);
}
