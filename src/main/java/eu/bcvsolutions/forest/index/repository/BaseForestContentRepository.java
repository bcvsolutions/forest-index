package eu.bcvsolutions.forest.index.repository;

import java.io.Serializable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.forest.index.domain.ForestContent;

/**
 * Indexable entity repository
 * * tree type is not supported 
 * 
 * @author Radek Tomiška
 *
 * @see {@link SimpleForestContentRepository}
 * @param <C>  content type
 * @param <CONTENT_ID> entity = content identifier
 */
@NoRepositoryBean
public interface BaseForestContentRepository<C extends ForestContent<C, ?, CONTENT_ID>, CONTENT_ID extends Serializable> extends PagingAndSortingRepository<C, CONTENT_ID> {
	
	/**
	 * Finds direct children for given parent
	 * 
	 * @param parent
	 * @return
	 */
	@Query("select e from #{#entityName} e where e.parent = :parent")
	Page<C> findDirectChildren(@Param("parent") C parent, Pageable pageable);
	
	/**
	 * Finds all children for given parent r
	 * ecursively by forest index
	 * 
	 * @param parent
	 * @param pageable
	 * @return
	 */
	@Query("select e from #{#entityName} e join e.forestIndex i where i.forestTreeType = ?#{[0].forestTreeType} and i.lft BETWEEN ?#{[0].forestIndex.lft + 1} and ?#{[0].forestIndex.rgt - 1}") // todo: possible null pointers
	Page<C> findAllChildren(C parent, Pageable pageable);
	
	
	// TODO: find all parents
}
