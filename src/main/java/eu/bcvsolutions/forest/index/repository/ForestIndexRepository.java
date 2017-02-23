package eu.bcvsolutions.forest.index.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.forest.index.domain.ForestIndex;

/**
 * Forest index repository
 * 
 * @see {@link ForestIndex}
 * @author Radek Tomiška
 * @param <IX> {@link ForestIndex} type - used for parent etc.
 */
@NoRepositoryBean
public interface ForestIndexRepository<IX extends ForestIndex<IX, ?>> extends PagingAndSortingRepository<IX, Long> {
	
	@Query("select e from #{#entityName} e where e.parent is null and e.forestTreeType = :forestTreeType")
	IX findRoot(@Param("forestTreeType") String forestTreeType);
	
	@Query("select e from #{#entityName} e where e.parent is null and e.id <> :newParentId and e.forestTreeType = :forestTreeType")
	IX findPreviousRoot(@Param("forestTreeType") String forestTreeType, @Param("newParentId") Long newParentId);
	
	@Query("select e.id from #{#entityName} e where e.parent is null and e.id <> :newParentId and e.forestTreeType = :forestTreeType")
	Long findPreviousRootId(@Param("forestTreeType") String forestTreeType, @Param("newParentId") Long newParentId);
	
	@Query("select e.parent.id from #{#entityName} e where e.id = :id")
	Long findParentId(@Param("id") Long id);
	
	/**
	 * Finds direct children for given parent
	 * 
	 * @param parent
	 * @return
	 */
	@Query("select e from #{#entityName} e where e.parent = ?#{[0]} and e.forestTreeType = ?#{[0].forestTreeType}")
	List<IX> findDirectChildren(IX parent);
	
	/**
	 * Finds all children for given paren recursively
	 * 
	 * @param parent
	 * @param pageable
	 * @return
	 */
	@Query("select e from #{#entityName} e where e.forestTreeType = ?#{[0].forestTreeType} and e.lft BETWEEN ?#{[0].lft + 1} and ?#{[0].rgt - 1}")
	Page<IX> findAllChildren(IX parent, Pageable pageable);
	
	/**
	 * Drops all indexes
	 */
	@Modifying
	@Query("delete from #{#entityName} e where e.forestTreeType = :forestTreeType")
	void dropIndexes(@Param("forestTreeType") String forestTreeType);
	
	@Modifying
	@Query("update #{#entityName} e set e.lft = null, e.rgt = null where e.forestTreeType = :forestTreeType")
	void clearIndexes(@Param("forestTreeType") String forestTreeType);
	
	@Modifying
	@Query("delete from #{#entityName} e where e.forestTreeType = :forestTreeType and e.lft BETWEEN :lft and :rgt")
	void dropIndexes(@Param("forestTreeType") String forestTreeType, @Param("lft") Long lft, @Param("rgt") Long rgt);
	
	@Modifying
	@Query("update #{#entityName} e set e.lft = null, e.rgt = null where e.forestTreeType = :forestTreeType and e.lft BETWEEN :lft and :rgt")
	void clearIndexes(@Param("forestTreeType") String forestTreeType, @Param("lft") Long lft, @Param("rgt") Long rgt);
	
	@Query("select coalesce(max(e.rgt), 1) + 1 from #{#entityName} e where e.forestTreeType = :forestTreeType")
	long addedRootRgt(@Param("forestTreeType") String forestTreeType);
	
	@Modifying
	@Query("update #{#entityName} e set e.lft = e.lft + 1, e.rgt = e.rgt + 1 where e.forestTreeType = :forestTreeType")
	void beforeRootInsert(@Param("forestTreeType") String forestTreeType);
	
	/**
	 * Updates index values only
	 * 
	 * @param id updated index
	 * @param lft
	 * @param rgt
	 * @param parent
	 */
	@Modifying
	@Query("update #{#entityName} e set e.lft = :lft, e.rgt = :rgt, e.parent = :parent where e.id = :id")
	void updateIndexes(@Param("id") Long id, @Param("lft") Long lft, @Param("rgt") Long rgt, @Param("parent") IX parent);
	
	/**
	 * Updates index's parent
	 * 
	 * @param id updated index
	 * @param parent
	 */
	@Modifying
	@Query("update #{#entityName} e set e.parent = :parent where e.id = :id")
	void updateParent(@Param("id") Long id, @Param("parent") IX parent);
	
	@Modifying
	@Query("update #{#entityName} e set"
			+ " e.lft = (CASE WHEN e.lft > :rgt THEN (e.lft + 2) WHEN e.lft is null THEN null ELSE e.lft END),"
			+ " e.rgt = (CASE WHEN e.rgt >= :rgt THEN (e.rgt + 2) WHEN e.rgt is null THEN null ELSE e.rgt END)"
			+ " where e.rgt >= :rgt and e.forestTreeType = :forestTreeType")
	void beforeNodeInsert(@Param("forestTreeType") String forestTreeType, @Param("rgt") Long parentRgt);
	
	/**
	 * Deletes whole sub tree
	 * 
	 * @param lft
	 * @param rgt
	 */
	@Modifying
	@Query("delete from #{#entityName} e where e.forestTreeType = :forestTreeType and e.lft BETWEEN :lft and :rgt") // todo: forest
	void delete(@Param("forestTreeType") String forestTreeType, @Param("lft") Long lft, @Param("rgt") Long rgt);
	
	/**
	 * Recount indexes after subtree delete
	 * 
	 * @param lft
	 * @param rgt
	 */
	@Modifying
	@Query("update #{#entityName} e set"
			+ " e.lft = (CASE WHEN e.lft > :lft THEN (e.lft - (:rgt - :lft + 1)) WHEN e.lft is null THEN null ELSE e.lft END),"
			+ " e.rgt = (CASE WHEN e.rgt > :lft THEN (e.rgt - (:rgt - :lft + 1)) WHEN e.rgt is null THEN null ELSE e.rgt END)"
			+ " where (e.lft > :lft OR e.rgt > :lft) and e.forestTreeType = :forestTreeType")
	void afterDelete(@Param("forestTreeType") String forestTreeType, @Param("lft") Long lft, @Param("rgt") Long rgt);
}
