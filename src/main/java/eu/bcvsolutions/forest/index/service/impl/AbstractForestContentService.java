package eu.bcvsolutions.forest.index.service.impl;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.forest.index.domain.ForestContent;
import eu.bcvsolutions.forest.index.domain.ForestIndex;
import eu.bcvsolutions.forest.index.repository.TypeableForestContentRepository;
import eu.bcvsolutions.forest.index.service.api.ForestContentService;
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
		implements ForestContentService<C, IX, CONTENT_ID> {

	private final ForestIndexService<IX, CONTENT_ID> forestIndexService;
	private final TypeableForestContentRepository<C, CONTENT_ID> repository;

	public AbstractForestContentService(ForestIndexService<IX, CONTENT_ID> forestIndexService,
			TypeableForestContentRepository<C, CONTENT_ID> repository) {
		Assert.notNull(forestIndexService);
		Assert.notNull(repository);
		//
		this.forestIndexService = forestIndexService;
		this.repository = repository;
	}

	@Override
	public void rebuildIndexes(String forestTreeType) {
		// clear all rgt, lft
		forestIndexService.dropIndexes(forestTreeType);
		//
		findRoots(forestTreeType, null).forEach(root -> {
			recountIndexes(createIndex(root));
		});
	}

	/**
	 * Recounts indexes for subtree or whole tree if {@code null} is given.
	 * Expects cleared indexes.
	 * 
	 * @param parent
	 */
	private void recountIndexes(C parent) {
		Assert.notNull(parent);
		//
		repository.findDirectChildren(parent, null).forEach(forestIndex -> {
			recountIndexes(createIndex(forestIndex));
		});
	}
	
	@Override
	@Transactional
	public C createIndex(C content) {
		return forestIndexService.index(content);
	}
	
	@Override
	@Transactional
	public C updateIndex(C content) {
		return forestIndexService.index(content);
	}
	
	@Override
	@Transactional
	public C deleteIndex(C content) {
		return forestIndexService.dropIndex(content);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<C> findRoots(String forestTreeType, Pageable pageable) {
		return repository.findRoots(forestTreeType, pageable);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<C> findDirectChildren(C parent, Pageable pageable) {
		return repository.findDirectChildren(parent, pageable);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<C> findAllChildren(C parent, Pageable pageable) {
		return repository.findAllChildren(parent, pageable);
	}

	@Override
	@Transactional(readOnly = true)
	public List<C> findAllParents(C content, Sort sort) {
		return repository.findAllParents(content, sort);
	}
	
	
}
