package eu.bcvsolutions.forest.index.repository;

import java.io.Serializable;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.forest.index.domain.ForestContent;

/**
 * Indexable entity repository
 * * tree type is not supported
 * 
 * @author Radek Tomiška
 *
 * @param <C> content type
 * @param <CONTENT_ID> entity = content identifier
 */
@NoRepositoryBean
public interface SimpleForestContentRepository<C extends ForestContent<C, ?, CONTENT_ID>, CONTENT_ID extends Serializable>
		extends BaseForestContentRepository<C, CONTENT_ID> {

	/**
	 * Finds root (indexed tree can have onlz one root)
	 * 
	 * @return
	 */
	@Query("select e from #{#entityName} e where e.parent is null")
	C findRoot();
	
	/**
	 * Finds previous root - used to ensure one root integrity, when another root is created
	 * 
	 * @param newParentId newly created root
	 * @return
	 */
	@Query("select e from #{#entityName} e where e.parent is null and e.id <> :newParentId")
	C findPreviousRoot(@Param("newParentId") CONTENT_ID newParentId);
}
