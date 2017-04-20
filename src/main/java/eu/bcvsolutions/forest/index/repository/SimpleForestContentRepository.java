package eu.bcvsolutions.forest.index.repository;

import java.io.Serializable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

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
	 * Finds roots
	 * 
	 * @return
	 */
	@Query("select e from #{#entityName} e where e.parent is null")
	Page<C> findRoots(Pageable pageable);
}
