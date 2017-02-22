# Forest Index

Indexing and traversing tree structures (hierarchies). Additional `forest index` can be added to custom `content` (=> entity). The content will have reference to index and can use him in `hql spring data queries` etc. One forest index could be used for different trees - see `forestTreeType` attribute.

Features:
* Get all tree node children in "one select".  
* Get all tree node parents in "one select".

Implemented operations with content and index:
* create index when content is created
* drop indexes, when content is deleted
* update index, when content is moved in tree structure => when content parent is changed, then index is recounted. Its implemented by "drop and created" and can be improved in future
* drop and rebuild whole index.

Unimplemented operations:
* lock tree type, when index building is in progress.

Build on spring boot, spring data and hibernate.

## Requirements

* Content has to have identifier
* Any jdbc database can be used (tested on h2, posgresql)

## Usage

### Maven

Add maven dependency to project

```xml
...
<dependency>
	<groupId>eu.bcvsolutions.forest.index</groupId>
	<artifactId>forest-index</artifactId>
	<version>0.0.1-SNAPSHOT</version>
</dependency>
...
```

## Java

The library provides API and abstract classes for forest index only, because different contents could have different identifiers, different tree type definition etc.

### ForestIndex

Create entity for persist Forest index:

```java
import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class ForestIndexEntity extends AbstractForestIndex<ForestIndexEntity, Long> {

	@Column(name = "content_id")
	private Long contentId;

	@Override
	public void setContentId(Long contentId) {
		this.contentId = contentId;
	}

	@Override
	public Long getContentId() {
		return contentId;
	}
}
```

Initialize repository and service for forest index entity (`@Configuration` can be used too):

```java
import eu.bcvsolutions.forest.index.entity.ForestIndexEntity;

public interface ForestIndexEntityRepository extends ForestIndexRepository<ForestIndexEntity> {
}
```

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.forest.index.entity.ForestIndexEntity;
import eu.bcvsolutions.forest.index.repository.ForestIndexEntityRepository;
import eu.bcvsolutions.forest.index.service.impl.AbstractForestIndexService;

@Service
public class DefaultForestIndexEntityService extends AbstractForestIndexService<ForestIndexEntity, Long> {

	@Autowired
	public DefaultForestIndexEntityService(ForestIndexEntityRepository repository) {
		super(repository);
	}
}
```

### ForestContent

= what we want to index. Add and implement interface `ForestContent` to your own entity or create new:

```java
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.validator.constraints.NotEmpty;

import eu.bcvsolutions.forest.index.domain.ForestContent;
import eu.bcvsolutions.forest.index.domain.ForestIndex;

@Entity
public class NodeContent implements ForestContent<NodeContent, ForestIndexEntity, Long> {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "name")
	private String name;

	@ManyToOne(optional = true)
	@JoinColumn(name = "parent_id", referencedColumnName = "id")
	private NodeContent parent;

	@ManyToOne(optional = true)
	@JoinColumn(name = "id", referencedColumnName = "content_id", updatable = false, insertable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private ForestIndexEntity forestIndex;

	@NotEmpty
	@Column(name = "forest_tree_type", nullable = false)
	private String forestTreeType = ForestIndex.DEFAULT_TREE_TYPE;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public ForestIndexEntity getForestIndex() {
		return forestIndex;
	}

	@Override
	public void setForestIndex(ForestIndexEntity forestIndex) {
		this.forestIndex = forestIndex;
	}

	@Override
	public NodeContent getParent() {
		return parent;
	}

	@Override
	public void setParent(NodeContent parent) {
		this.parent = parent;
	}

	@Override
	public String getForestTreeType() {
		if (forestTreeType == null) {
			forestTreeType = ForestIndex.DEFAULT_TREE_TYPE;
		}
		return forestTreeType;
	}

	public void setForestTreeType(String forestTreeType) {
		this.forestTreeType = forestTreeType;
	}
}

```

Service for ForestContent entity:

```java
import eu.bcvsolutions.forest.index.entity.ForestIndexEntity;
import eu.bcvsolutions.forest.index.entity.NodeContent;
import eu.bcvsolutions.forest.index.service.api.ForestContentService;

public interface NodeContentService extends ForestContentService<NodeContent, ForestIndexEntity, Long> {

	NodeContent save(NodeContent content);

	void delete(NodeContent content);
}
```

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.forest.index.entity.ForestIndexEntity;
import eu.bcvsolutions.forest.index.entity.NodeContent;
import eu.bcvsolutions.forest.index.repository.NodeContentRepository;
import eu.bcvsolutions.forest.index.service.api.ForestIndexService;
import eu.bcvsolutions.forest.index.service.impl.AbstractForestContentService;

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
	@Transactional
	public NodeContent save(NodeContent content) {
		Assert.notNull(content);
		//
		if (content.getId() == null) {
			// create new
			return createIndex(repository.save(content));
		} else {
			// update - we need to reindex first
			return repository.save(updateIndex(content));
		}
	}

	@Override
	@Transactional
	public void delete(NodeContent content) {
		Assert.notNull(content);
		//
		repository.delete(deleteIndex(content));
	}
}
```

And that's it. This service could be used for persist and query content by index. Example spring data query from `AbstractForestContentService`, respectively from `BaseForestContentRepository` for select all content children:

```java
@Query("select e from NodeContent e join e.forestIndex i where i.forestTreeType = ?#{[0].forestTreeType} and i.lft BETWEEN ?#{[0].forestIndex.lft + 1} and ?#{[0].forestIndex.rgt - 1}")
Page<NodeContent> findAllChildren(NodeContent parent, Pageable pageable);
```

Complete example could be found i test package.

### Implementation notes

Sometimes, maybe every time, is not possible to generalize `AbstractForestContentService`. Better approach is to implement `ForestContentService`  directly - `AbstractForestContentService` is wrapper for ForestIndexService only.

## License

[MIT License](./LICENSE)
