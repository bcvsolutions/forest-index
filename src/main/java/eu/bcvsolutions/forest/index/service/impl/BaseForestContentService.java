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
import eu.bcvsolutions.forest.index.repository.BaseForestContentRepository;
import eu.bcvsolutions.forest.index.service.api.ForestContentService;
import eu.bcvsolutions.forest.index.service.api.ForestIndexService;

/**
 * Base forest content service.
 * * implement ForestContentService instead using this class - its only reuses {@link ForestIndexService}
 * * implement rebuildIndexes as long running task instead
 * * without support for tree types
 * 
 * @author Radek Tomiška
 *
 * @param <C> content type
 * @param <IX> index type
 * @param <CONTENT_ID> content identifier - e.g. {@code Long} or {@link UUID} is preferred
 */
public abstract class BaseForestContentService<C extends ForestContent<IX, CONTENT_ID>, IX extends ForestIndex<IX, CONTENT_ID>, CONTENT_ID extends Serializable>
		implements ForestContentService<C, IX, CONTENT_ID> {

	private final ForestIndexService<IX, CONTENT_ID> forestIndexService;
	private final BaseForestContentRepository<C, CONTENT_ID> repository;

	public BaseForestContentService(ForestIndexService<IX, CONTENT_ID> forestIndexService,
			BaseForestContentRepository<C, CONTENT_ID> repository) {
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
		findRoots(forestTreeType, null).forEach(root ->{
			createIndex(forestTreeType, root.getId(), null);
			recountIndexes(root);
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
		repository.findDirectChildren(parent, null).forEach(content -> {
			createIndex(content.getForestTreeType(), content.getId(), content.getParentId());
			recountIndexes(content);
		});
	}

	@Override
	@Transactional
	public IX createIndex(String forestTreeType, CONTENT_ID contentId, CONTENT_ID parentContentId) {
		return forestIndexService.index(forestTreeType, contentId, parentContentId);
	}

	@Override
	@Transactional
	public IX updateIndex(String forestTreeType, CONTENT_ID contentId, CONTENT_ID parentContentId) {
		return forestIndexService.index(forestTreeType, contentId, parentContentId);
	}

	@Override
	@Transactional
	public IX deleteIndex(CONTENT_ID contentId) {
		return forestIndexService.dropIndex(contentId);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<C> findDirectChildren(CONTENT_ID contentId, Pageable pageable) {
		return repository.findDirectChildren(repository.findOne(contentId), pageable);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<C> findAllChildren(CONTENT_ID contentId, Pageable pageable) {
		return repository.findAllChildren(repository.findOne(contentId), pageable);
	}

	@Override
	@Transactional(readOnly = true)
	public List<C> findAllParents(CONTENT_ID contentId, Sort sort) {
		return repository.findAllParents(repository.findOne(contentId), sort);
	}
}
