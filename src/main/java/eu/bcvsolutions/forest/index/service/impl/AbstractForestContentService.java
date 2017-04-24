package eu.bcvsolutions.forest.index.service.impl;

import java.io.Serializable;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.forest.index.domain.ForestContent;
import eu.bcvsolutions.forest.index.domain.ForestIndex;
import eu.bcvsolutions.forest.index.repository.TypeableForestContentRepository;
import eu.bcvsolutions.forest.index.service.api.ForestIndexService;

/**
 * Abstract forest content service.
 * * implement ForestContentService instead using this class - its only reuses {@link ForestIndexService}
 * * implement rebuildIndexes as long running task instead
 * * add support for tree types
 * 
 * @author Radek Tomiška
 *
 * @param <C> content type
 * @param <IX> index type
 * @param <CONTENT_ID> content identifier - e.g. {@code Long} or {@link UUID} is preferred
 */
public abstract class AbstractForestContentService<C extends ForestContent<C, IX, CONTENT_ID>, IX 
		extends ForestIndex<IX, CONTENT_ID>, CONTENT_ID extends Serializable>
		extends BaseForestContentService<C, IX, CONTENT_ID> {

	private final TypeableForestContentRepository<C, CONTENT_ID> repository;

	public AbstractForestContentService(ForestIndexService<IX, CONTENT_ID> forestIndexService,
			TypeableForestContentRepository<C, CONTENT_ID> repository) {
		super(forestIndexService, repository);
		//
		this.repository = repository;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<C> findRoots(String forestTreeType, Pageable pageable) {
		return repository.findRoots(forestTreeType, pageable);
	}
}
