package eu.bcvsolutions.forest.index.entity;

import org.junit.Assert;
import org.junit.Test;

/**
 * Base forest index entity method tests
 * 
 * @author Radek Tomiška
 *
 */
public class AbstractForestIndexUnitTest {

	@Test
	public void testEqualsById() {
		ForestIndexEntity forestOne = new ForestIndexEntity();
		forestOne.setId(1L);
		ForestIndexEntity forestTwo = new ForestIndexEntity();
		forestTwo.setId(2L);
		//
		Assert.assertFalse(forestOne.equals(forestTwo));
		forestTwo.setId(1L);
		Assert.assertTrue(forestOne.equals(forestTwo));
		Assert.assertFalse(forestOne.equals(null));
		Assert.assertFalse(forestOne.equals(new Object()));
		Assert.assertEquals(forestOne, forestTwo);
		Assert.assertEquals(forestOne.hashCode(), forestTwo.hashCode());
		
	}
	
	@Test
	public void testEmptyTreeType() {
		ForestIndexEntity forestOne = new ForestIndexEntity();
		forestOne.setForestTreeType(null);
		//
		Assert.assertEquals(ForestIndexEntity.DEFAULT_TREE_TYPE, forestOne.getForestTreeType());
		//
		forestOne = new ForestIndexEntity(null);
		//
		Assert.assertEquals(ForestIndexEntity.DEFAULT_TREE_TYPE, forestOne.getForestTreeType());
	}
	
	@Test
	public void testWithTreeType() {
		ForestIndexEntity forestOne = new ForestIndexEntity("mock", null);
		//
		Assert.assertEquals("mock", forestOne.getForestTreeType());
	}
	
	@Test
	public void testEmptyChildren() {
		ForestIndexEntity forestOne = new ForestIndexEntity();
		//
		Assert.assertEquals(0, forestOne.getChildrenCount());
		forestOne.setLft(1L);
		Assert.assertEquals(0, forestOne.getChildrenCount());
		forestOne.setLft(null);
		forestOne.setRgt(1L);
		Assert.assertEquals(0, forestOne.getChildrenCount());
		Assert.assertNotNull(forestOne.toString());
	}
}
