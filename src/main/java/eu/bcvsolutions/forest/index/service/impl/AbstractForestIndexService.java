package eu.bcvsolutions.forest.index.service.impl;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.springframework.core.GenericTypeResolver;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.forest.index.domain.ForestContent;
import eu.bcvsolutions.forest.index.domain.ForestIndex;
import eu.bcvsolutions.forest.index.repository.ForestIndexRepository;
import eu.bcvsolutions.forest.index.service.api.ForestIndexService;

/**
 * Persists, builds, clears forest indexes 
 * 
 * @author Radek Tomiška
 *
 * @param <IX> index type
 * @param <CONTENT_ID> content identifier - e.g. {@code Long} or {@link UUID} is preferred
 */
public abstract class AbstractForestIndexService<IX extends ForestIndex<IX, CONTENT_ID>, CONTENT_ID extends Serializable> 
		implements ForestIndexService<IX, CONTENT_ID> {
	
	private final Class<IX> indexClass;
	private final ForestIndexRepository<IX> repository;
    private final EntityManager entityManager;
	
	@SuppressWarnings("unchecked")
	public AbstractForestIndexService(
			ForestIndexRepository<IX> repository,
			EntityManager entityManager) {
		Assert.notNull(repository);
		Assert.notNull(entityManager);
		//
		Class<?>[] genericTypes = GenericTypeResolver.resolveTypeArguments(getClass(), ForestIndexService.class);
		indexClass = (Class<IX>)genericTypes[0];
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
		
	}
	
	/**
	 * Recounts indexes for subtree or whole tree if {@code null} is given. Expects cleared indexes.
	 *  
	 * @param parent
	 */
	private void recountIndexes(IX parent) {
		Assert.notNull(parent);
		//
		repository.findDirectChildren(parent).forEach(forestIndex -> {
			recountIndexes(countIndex(forestIndex));
		});
	}
	
	@Override
	@Transactional
	public IX saveNode(IX forestIndex) {
		Assert.notNull(forestIndex);
		entityManager.detach(forestIndex); // we need to load previous index value before flush
		//
		boolean parentChange = false;
		Long previousParentID = null;
		Long lft = null;
		Long rgt = null;
		// evaluate parent change for re-index
		if (forestIndex.getId() != null) {
			previousParentID = repository.findParentId(forestIndex.getId());
			lft = forestIndex.getLft();
			rgt = forestIndex.getRgt();
		}
		if (!Objects.equals(previousParentID, forestIndex.getParent() == null ? null : forestIndex.getParent().getId())) {
			forestIndex.setLft(null);
			forestIndex.setRgt(null);
			parentChange = true;
		}
		forestIndex = repository.save(forestIndex);
		if (!parentChange) {
			// index new node only
			if (forestIndex.getId() == null || forestIndex.getLft() == null || forestIndex.getRgt() == null) {
				return countIndex(forestIndex);
			}
		} else { // index node, it parent changes
			// drop moved sub tree indexes 
			if (lft != null && rgt != null) {
				repository.clearIndexes(forestIndex.getForestTreeType(), lft + 1, rgt - 1);
				repository.afterDelete(forestIndex.getForestTreeType(), lft, rgt);
			}
			// create new indexes
			forestIndex.setLft(null);
			forestIndex.setRgt(null);
			recountIndexes(countIndex(forestIndex));
		}
		return forestIndex;
	}
	
	private IX countIndex(IX forestIndex) {
		Assert.notNull(forestIndex);
		Assert.notNull(forestIndex.getId());
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
			forestIndex = repository.save(forestIndex);
			Long previousRootId = repository.findPreviousRootId(forestIndex.getForestTreeType(), forestIndex.getId());
			if (previousRootId != null) {
				repository.updateParent(previousRootId, forestIndex);
			}
		} else { // append a new node as last right child of his parent
			Long parentRgt = repository.findOne(forestIndex.getParent().getId()).getRgt();
			repository.beforeNodeInsert(forestIndex.getForestTreeType(), parentRgt);
			forestIndex.setLft(parentRgt);
			forestIndex.setRgt(parentRgt + 1L);
			repository.updateIndexes(forestIndex.getId(), forestIndex.getLft(), forestIndex.getRgt(), forestIndex.getParent());
		}
		return forestIndex;
	}
	
	@Override
	@Transactional
	public <C extends ForestContent<C, IX, CONTENT_ID>> C index(C content) {
		// previous index
		IX index = content.getForestIndex();
		// get parent index
		IX parentIndex = null;
		if (content.getParent() != null) {
			parentIndex = content.getParent().getForestIndex();
			if (parentIndex == null) {
				// reindex parent recursively
				parentIndex = index(content.getParent()).getForestIndex();
			}
		} else {
			// generate syntetic root - we want to support more content roots
			parentIndex = repository.findRoot(content.getForestTreeType());
			if (parentIndex == null) {
				try {
					parentIndex = indexClass.newInstance();
					parentIndex.setForestTreeType(content.getForestTreeType());
					parentIndex = this.saveNode(parentIndex);
				} catch (InstantiationException | IllegalAccessException o_O) {
					throw new IllegalArgumentException(MessageFormat.format("[{0}] does not support creating new instance. Fix forest index class - add default constructor.", indexClass), o_O);
				}
			}
		}
		//
		if (index == null) {
			try {
				index = indexClass.newInstance();
			} catch (InstantiationException | IllegalAccessException o_O) {
				throw new IllegalArgumentException(MessageFormat.format("[{0}] does not support creating new instance. Fix forest index class - add default constructor.", indexClass), o_O);
			}
		}
		// set parent index
		index.setParent(parentIndex);
		// set content id reference
		index.setContentId(content.getId());
		index.setForestTreeType(content.getForestTreeType());
		// create index
		content.setForestIndex(this.saveNode(index));
		//
		return content;
	}
	
	@Override
	public <C extends ForestContent<C, IX, CONTENT_ID>> C dropIndex(C content) {
		if (content.getForestIndex() != null) {
			deleteNode(content.getForestIndex(), true);
			content.setForestIndex(null);
		}
		return content;
	}
	
	@Override
	@Transactional
	public void deleteNode(IX forestIndex, boolean closeGap) {
		Assert.notNull(forestIndex);
		//
		repository.delete(forestIndex.getForestTreeType(), forestIndex.getLft(), forestIndex.getRgt());
		if (closeGap) {
			repository.afterDelete(forestIndex.getForestTreeType(), forestIndex.getLft(), forestIndex.getRgt());
		}
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void dropIndexes(String forestTreeType) {
		repository.dropIndexes(forestTreeType);
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void clearIndexes(String forestTreeType) { // TODO: tree type
		repository.clearIndexes(forestTreeType);
	}
}
