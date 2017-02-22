package eu.bcvsolutions.forest.index.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.forest.index.entity.ForestIndexEntity;
import eu.bcvsolutions.forest.index.entity.NodeContent;
import eu.bcvsolutions.forest.index.repository.NodeContentRepository;
import eu.bcvsolutions.forest.index.service.api.ForestIndexService;
import eu.bcvsolutions.forest.index.service.impl.AbstractForestContentService;

/**
 * Test tree content service
 * 
 * @author Radek Tomiška
 */
@Service
public class DefaultNodeContentService extends AbstractForestContentService<NodeContent, ForestIndexEntity, Long> implements NodeContentService {

	private final NodeContentRepository repository;
	
	@Autowired
	public DefaultNodeContentService(
			ForestIndexService<ForestIndexEntity, Long> forestIndexService,
			NodeContentRepository repository) {
		super(forestIndexService, repository);
		//
		this.repository = repository;
	}
	
	@Override
	@Transactional
	public NodeContent save(NodeContent content) {
		Assert.notNull(content);
		//
		if (content.getId() == null) {
			// create new
			return createIndex(repository.save(content));
		} else {
			// update - we need to reindex first
			return repository.save(updateIndex(content));
		}
	}

	@Override
	@Transactional
	public void delete(NodeContent content) {
		Assert.notNull(content);
		//
		repository.delete(deleteIndex(content));
	}
}
