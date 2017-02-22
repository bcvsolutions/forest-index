package eu.bcvsolutions.forest.index.service;

import eu.bcvsolutions.forest.index.entity.ForestIndexEntity;
import eu.bcvsolutions.forest.index.entity.NodeContent;
import eu.bcvsolutions.forest.index.service.api.ForestContentService;

/**
 * Test tree content service
 * 
 * @author Radek Tomiška
 *
 */
public interface NodeContentService extends ForestContentService<NodeContent, ForestIndexEntity, Long> {

	NodeContent save(NodeContent content);
	
	void delete(NodeContent content);
}
