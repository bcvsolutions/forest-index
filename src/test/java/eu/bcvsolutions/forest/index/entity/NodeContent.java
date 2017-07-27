package eu.bcvsolutions.forest.index.entity;

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

/**
 * Test node content
 * 
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "_node_content", indexes = {
		@Index(name = "_idx_node_content_parent", columnList = "parent_id")
		})
public class NodeContent implements ForestContent<ForestIndexEntity, Long> {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "id", precision = 18, scale = 0)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(name = "name")
	private String name;
	
	@ManyToOne(optional = true)
	@JoinColumn(name = "parent_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private NodeContent parent;	
	
	@ManyToOne(optional = true)
	@JoinColumn(name = "id", referencedColumnName = "content_id", updatable = false, insertable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private ForestIndexEntity forestIndex;
	
	@NotEmpty
	@Column(name = "forest_tree_type", nullable = false)
	private String forestTreeType = ForestIndex.DEFAULT_TREE_TYPE;
	
	public NodeContent() {
	}
	
	public NodeContent(NodeContent parent, String name) {
		this.parent = parent;
		this.name = name;
	}
	
	public NodeContent(String forestTreeType, NodeContent parent, String name) {
		this(parent, name);
		this.forestTreeType = forestTreeType;
	}

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
	
	public NodeContent getParent() {
		return parent;
	}
	
	public void setParent(NodeContent parent) {
		this.parent = parent;
	}
	
	@Override
	public Long getParentId() {
		return parent == null ? null : parent.getId();
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
