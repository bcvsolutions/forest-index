package eu.bcvsolutions.forest.index.service.impl;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.springframework.core.GenericTypeResolver;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.forest.index.domain.ForestIndex;
import eu.bcvsolutions.forest.index.repository.ForestIndexRepository;
import eu.bcvsolutions.forest.index.service.api.ForestIndexService;

/**
 * Persists, builds, clears forest indexes 
 * 
 * - TODO: flush and clear is called manually - use new spring data version with @Modifying annotation (auto flush and auto clear) 
 * 
 * @author Radek Tomiška
 *
 * @param <IX> index type
 * @param <CONTENT_ID> content identifier - e.g. {@code Long} or {@link UUID} is preferred
 */
public abstract class AbstractForestIndexService<IX extends ForestIndex<IX, CONTENT_ID>, CONTENT_ID extends Serializable> 
		implements ForestIndexService<IX, CONTENT_ID> {
	
	private final Class<IX> indexClass;
	private final ForestIndexRepository<IX, CONTENT_ID> repository;
    private final EntityManager entityManager;
	
	@SuppressWarnings("unchecked")
	public AbstractForestIndexService(
			ForestIndexRepository<IX, CONTENT_ID> repository,
			EntityManager entityManager) {
		Assert.notNull(repository, "Index repository is required.");
		Assert.notNull(entityManager, "Entity manager is required.");
		//
		Class<?>[] genericTypes = GenericTypeResolver.resolveTypeArguments(getClass(), ForestIndexService.class);
		//
		Assert.notEmpty(genericTypes, "Wrong generic types is given, fix class definition");
		indexClass = (Class<IX>) genericTypes[0];
		//
		this.repository = repository;
		this.entityManager = entityManager;
	}
	
	@Override
	@Transactional
	public void rebuild(String forestTreeType) {
		// clear all rgt, lft
		repository.clearIndexes(forestTreeType);
		//
		IX root = repository.findRoot(forestTreeType);
		if (root == null) {
			return;
		}
		recountIndexes(countIndex(root));
		entityManager.flush();
		entityManager.clear();
	}
	
	/**
	 * Recounts indexes for subtree or whole tree if {@code null} is given. Expects cleared indexes.
	 *  
	 * @param parent
	 */
	private void recountIndexes(IX parent) {
		Assert.notNull(parent, "Parent index is required.");
		//
		repository.findDirectChildren(parent).forEach(forestIndex -> {
			recountIndexes(countIndex(forestIndex));
		});
	}
	
	@Override
	@Transactional
	public IX saveNode(IX forestIndex) {
		Assert.notNull(forestIndex, "Index is required.");
		entityManager.detach(forestIndex); // we need to load previous index value before flush
		//
		boolean parentChange = false;
		Long previousParentId = null;
		Long lft = null;
		Long rgt = null;
		// evaluate parent change for re-index
		if (forestIndex.getId() != null) {
			previousParentId = repository.findParentId(forestIndex.getId());
			lft = forestIndex.getLft();
			rgt = forestIndex.getRgt();
		}
		if (!Objects.equals(previousParentId, forestIndex.getParent() == null ? null : forestIndex.getParent().getId())) {
			forestIndex.setLft(null);
			forestIndex.setRgt(null);
			if (previousParentId != null) {
				parentChange = true;
			}
		}
		forestIndex = repository.save(forestIndex);
		if (!parentChange) {
			// index new node only
			if (forestIndex.getLft() == null || forestIndex.getRgt() == null) {
				return countIndex(forestIndex);
			}
		} else { // index node, if parent changes
			// drop moved sub tree indexes
			// when parent is changed, then indexes has to be given => tree is broken otherwise
			if (lft == null || rgt == null) {
				throw new IllegalArgumentException("Indexes has to be given (loaded) before parent index is changed"
						+ " - it is required for a proper index rebuild.");
			}
			// drop moved sub tree indexes
			repository.clearIndexes(forestIndex.getForestTreeType(), lft + 1, rgt - 1);
			repository.afterDelete(forestIndex.getForestTreeType(), lft, rgt);
			// create new indexes
			forestIndex.setLft(null);
			forestIndex.setRgt(null);
			recountIndexes(countIndex(forestIndex));
		}
		entityManager.flush();
		entityManager.clear();
		return forestIndex;
	}
	
	private IX countIndex(IX forestIndex) {
		Assert.notNull(forestIndex, "Index is required.");
		Assert.notNull(forestIndex.getId(), "Index has to be persisted.");
		//
		// we need new data in next queries
		entityManager.flush();
		entityManager.clear();
		//
		// inserting a new root node
		if (forestIndex.getParent() == null) {
			repository.beforeRootInsert(forestIndex.getForestTreeType());
			forestIndex.setLft(1L);
			forestIndex.setRgt(repository.addedRootRgt(forestIndex.getForestTreeType()));			
			repository.updateIndexes(forestIndex.getId(), forestIndex.getLft(), forestIndex.getRgt(), null);
			Long previousRootId = repository.findPreviousRootId(forestIndex.getForestTreeType(), forestIndex.getId());
			if (previousRootId != null) {
				repository.updateParent(previousRootId, forestIndex);
			}
		} else { // append a new node as last right child of his parent
			Long parentRgt = repository.findById(forestIndex.getParent().getId()).get().getRgt();
			repository.beforeNodeInsert(forestIndex.getForestTreeType(), parentRgt);
			forestIndex.setLft(parentRgt);
			forestIndex.setRgt(parentRgt + 1L);
			repository.updateIndexes(forestIndex.getId(), forestIndex.getLft(), forestIndex.getRgt(), forestIndex.getParent());
		}
		//
		entityManager.flush();
		entityManager.clear();
		return forestIndex;
	}
	
	@Override
	@Transactional
	public IX index(String forestTreeType, CONTENT_ID contentId, CONTENT_ID parentContentId) {
		// previous index
		IX index = repository.findOneByContentId(contentId);
		// get parent index
		IX parentIndex = null;
		if (parentContentId != null) {
			parentIndex = repository.findOneByContentId(parentContentId);
			if (parentIndex == null) {
				// reindex parent recursively
				// parentIndex = index(content.getParent()).getForestIndex();
				throw new UnsupportedOperationException(String.format("Parent [%s] doesn't have index - index parent at first.", parentContentId));
			}
		} else {
			// generate synthetic root - we want to support more content roots
			parentIndex = repository.findRoot(forestTreeType);
			if (parentIndex == null) {
				parentIndex = createIndexInstance(indexClass);
				parentIndex.setForestTreeType(forestTreeType);
				parentIndex = this.saveNode(parentIndex);
			}
		}
		//
		if (index == null) {
			index = createIndexInstance(indexClass);
		}
		// set parent index
		index.setParent(parentIndex);
		// set content id reference
		index.setContentId(contentId);
		index.setForestTreeType(forestTreeType);
		// create index
		return this.saveNode(index);
	}
	
	@Override
	@Transactional
	public IX dropIndex(CONTENT_ID contentId) {
		Assert.notNull(contentId, "Content identifier is required.");
		//
		IX index = repository.findOneByContentId(contentId);
		//
		if (index != null) {
			deleteNode(index, true);
		}
		return index;
	}
	
	@Override
	@Transactional
	public void deleteNode(IX forestIndex, boolean closeGap) {
		Assert.notNull(forestIndex, "Index is required.");
		//
		repository.delete(forestIndex.getForestTreeType(), forestIndex.getLft(), forestIndex.getRgt());
		if (closeGap) {
			repository.afterDelete(forestIndex.getForestTreeType(), forestIndex.getLft(), forestIndex.getRgt());
		}
		entityManager.flush();
		entityManager.clear();
	}
	
	@Override
	@Transactional
	public void dropIndexes(String forestTreeType) {
		repository.dropIndexes(forestTreeType);
		entityManager.flush();
		entityManager.clear();
	}
	
	@Override
	@Transactional
	public void clearIndexes(String forestTreeType) {
		repository.clearIndexes(forestTreeType);
		entityManager.flush();
		entityManager.clear();
	}
	
	/**
	 * Create new index instance.
	 * 
	 * @return
	 * @throws IllegalArgumentException when index class does not define default constructor.
	 * @since 1.1.0
	 */
	protected IX createIndexInstance(Class<? extends IX> indexClass) {
		try {
			return indexClass.newInstance();
		} catch (InstantiationException | IllegalAccessException o_O) {
			throw new IllegalArgumentException(MessageFormat.format("[{0}] does not support creating new instance. Fix forest index class - add default constructor.", indexClass), o_O);
		}
	}
}
