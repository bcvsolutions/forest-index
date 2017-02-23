/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bcvsolutions.forest.index.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * Persists forest index with {@code Long} content.
 *
 * @author Radek Tomiška
 */
@Entity
@Table(name = "_forest_index", indexes = {
		@Index(name = "_idx_forest_index_parent", columnList = "parent_id"),
		@Index(name = "_idx_forest_index_content", columnList = "content_id"),
		@Index(name = "_idx_forest_index_lft", columnList = "lft"),
		@Index(name = "_idx_forest_index_rgt", columnList = "rgt")
		})
public class ForestIndexEntity extends AbstractForestIndex<ForestIndexEntity, Long> {
	
	private static final long serialVersionUID = 1L;
	
	@Column(name = "content_id", precision = 18, scale = 0)
	private Long contentId;

	public ForestIndexEntity() {
	}
	
	public ForestIndexEntity(ForestIndexEntity parent) {
		super(null, parent);
	}
	
	@Override
	public void setContentId(Long contentId) {
		this.contentId = contentId;
	}
	
	@Override
	public Long getContentId() {
		return contentId;
	}
}
