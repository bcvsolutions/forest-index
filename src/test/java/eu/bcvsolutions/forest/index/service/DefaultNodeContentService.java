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
	@Transactional(readOnly = true)
	public NodeContent get(Long id) {
		return repository.findOne(id);
	}
	
	@Override
	@Transactional
	public NodeContent save(NodeContent content) {
		Assert.notNull(content);
		//
		if (content.getId() == null) {
			// create new
			content = repository.save(content);
			content.setForestIndex(createIndex(content.getForestTreeType(), content.getId(), content.getParentId()));
			return content;
		} else {
			// update - we need to reindex first
			content.setForestIndex(updateIndex(content.getForestTreeType(), content.getId(), content.getParentId()));
			return repository.save(content);
		}
	}

	@Override
	@Transactional
	public void delete(NodeContent content) {
		Assert.notNull(content);
		// remove all children
		findDirectChildren(content.getId(), null).forEach(child -> {
			this.delete(child);
		});
		deleteIndex(content.getId());
		repository.delete(content);
	}
}
