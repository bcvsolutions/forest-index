package eu.bcvsolutions.forest.index.entity;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import org.hibernate.validator.constraints.NotEmpty;

import eu.bcvsolutions.forest.index.domain.ForestIndex;

/**
 * Abstract forest index
 *
 * @author Radek Tomiška
 * @param <IX> {@link ForestIndex} type - used for parent etc.
 * @param <CONTENT_ID> content identifier - e.g. {@code Long} or {@link UUID} is preferred
 */
@MappedSuperclass
public abstract class AbstractForestIndex<IX extends ForestIndex<IX, CONTENT_ID>, CONTENT_ID extends Serializable> 
	implements ForestIndex<IX, CONTENT_ID>, Serializable {
	
	private static final long serialVersionUID = -8702912114998116552L;

	@Id
	@Column(name = "id", precision = 18, scale = 0)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(name = "lft", precision = 18, scale = 0)
	private Long lft;
	
	@Column(name = "rgt", precision = 18, scale = 0)
	private Long rgt;

	@ManyToOne(optional = true)
	@JoinColumn(name = "parent_id", referencedColumnName = "id")
	private IX parent;
	
	@NotEmpty
	@Column(name = "forest_tree_type", nullable = false)
	private String forestTreeType = DEFAULT_TREE_TYPE;
	
	public AbstractForestIndex() {
	}
	
	public AbstractForestIndex(String forestTreeType, IX parent) {
		this.forestTreeType = forestTreeType == null ? DEFAULT_TREE_TYPE : forestTreeType;
		this.parent = parent;
	}
	
	@Override
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public IX getParent() {
		return parent;
	}

	public void setParent(IX parent) {
		this.parent = parent;
	}

	@Override
	public Long getLft() {
		return lft;
	}

	@Override
	public void setLft(Long lft) {
		this.lft = lft;
	}

	@Override
	public Long getRgt() {
		return rgt;
	}

	@Override
	public void setRgt(Long rgt) {
		this.rgt = rgt;
	}
	
	@Override
	public String getForestTreeType() {
		if (forestTreeType == null) {
			forestTreeType = DEFAULT_TREE_TYPE;
		}
		return forestTreeType;
	}
	
	@Override
	public void setForestTreeType(String forestTreeType) {
		this.forestTreeType = forestTreeType;
	}
	
	@Override
	public int getChildrenCount() {
		return (int) ((rgt - lft) / 2);
	}
	
	/**
	 * Based on entity identifier
	 */
	@Override
	public int hashCode() {
		int hash = 0;
		hash += (getId() != null ? getId().hashCode() : 0);
		return hash;
	}

	/**
	 * Based on entity identifier
	 */
	@Override
	public boolean equals(Object object) {
		if (object == null || !object.getClass().equals(getClass())) {
			return false;
		}

		AbstractForestIndex<?,?> other = (AbstractForestIndex<?, ?>) object;
		if ((this.getId() == null && other.getId() != null)
				|| (this.getId() != null && !this.getId().equals(other.getId()))
				|| (this.getId() == null && other.getId() == null && this != other)) {
			return false;
		}

		return true;
	}
	
	@Override
	public String toString() {
		return "forest index [" + forestTreeType + ":" + id + "] [" + lft + "-" + rgt + "] content [" + getContentId() + "]";
	}
}
