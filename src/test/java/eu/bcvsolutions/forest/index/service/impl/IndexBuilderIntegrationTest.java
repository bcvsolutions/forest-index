package eu.bcvsolutions.forest.index.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.forest.index.domain.ForestIndex;
import eu.bcvsolutions.forest.index.entity.ForestIndexEntity;
import eu.bcvsolutions.forest.index.repository.ForestIndexEntityRepository;
import eu.bcvsolutions.forest.index.service.api.ForestIndexService;

/**
 * Index build, rebuild etc.
 * 
 * @author Radek Tomiška
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class IndexBuilderIntegrationTest {
	
	@Autowired private ForestIndexEntityRepository repository;
	@Autowired private ForestIndexService<ForestIndexEntity, Long> service;
	//
	private Random r = new Random();
	private ForestIndexEntity a = null;
	private ForestIndexEntity b = null;
	private ForestIndexEntity ba = null;
	private ForestIndexEntity bb = null;
	private List<ForestIndexEntity> children = new ArrayList<>();
	
	@Test
	public void testSaveTree() {
		ForestIndexEntity oldroot = service.saveNode(new ForestIndexEntity(null));
		a = service.saveNode(new ForestIndexEntity(oldroot));
		b = service.saveNode(new ForestIndexEntity(oldroot));
		service.saveNode(new ForestIndexEntity(a));
		service.saveNode(new ForestIndexEntity(a));
		ba = service.saveNode(new ForestIndexEntity(b));
		bb = service.saveNode(new ForestIndexEntity(b));
		ForestIndexEntity root = service.saveNode(new ForestIndexEntity(null));
		
		Assert.assertEquals(8, repository.count());
		
		List<ForestIndexEntity> children = repository.findDirectChildren(oldroot);
		Assert.assertEquals(2, children.size());
		
		a = repository.findById(a.getId()).get();
		b = repository.findById(b.getId()).get();
		ba = repository.findById(ba.getId()).get();
		bb = repository.findById(bb.getId()).get();
		
		Assert.assertEquals(1L, root.getLft().longValue());
		Assert.assertEquals(7, (root.getRgt() - root.getLft()) / 2); // all children count
		Assert.assertEquals(7, repository.findAllChildren(root, null).getTotalElements()); // all children count by query
		Assert.assertEquals(2, repository.findAllChildren(b, null).getTotalElements()); // all children count by query
		
		Assert.assertEquals(2, (b.getRgt() - b.getLft()) / 2);
		Assert.assertEquals(2, b.getChildrenCount());
		
		Assert.assertTrue(ba.getLft().compareTo(ba.getParent().getLft()) > 0);
		Assert.assertTrue(ba.getRgt().compareTo(ba.getParent().getRgt()) < 0);
		
		Assert.assertEquals(bb.getRgt().longValue(), bb.getLft() + 1L);
	}
	
	@Test
	public void testSaveNodeWithRgtIsNull() {
		ForestIndexEntity forestIndex = new ForestIndexEntity(null);
		forestIndex.setId(23469L); // mock
		forestIndex.setLft(1L);
		//
		ForestIndexEntity node = service.saveNode(forestIndex);
		// reindex should occur
		Assert.assertNotNull(node.getId());
		Assert.assertNotNull(node.getLft());
		Assert.assertNotNull(node.getRgt());
	}
	
	@Test
	public void testSaveNodeWithLftIsNull() {
		ForestIndexEntity forestIndex = new ForestIndexEntity(null);
		forestIndex.setId(23469L); // mock
		//
		ForestIndexEntity node = service.saveNode(forestIndex);
		// reindex should occur
		Assert.assertNotNull(node.getId());
		Assert.assertNotNull(node.getLft());
		Assert.assertNotNull(node.getRgt());
	}
	
	@Test
	public void testSaveNodeWithIdIsNull() {
		ForestIndexEntity forestIndex = new ForestIndexEntity(null);
		forestIndex.setLft(1L);
		forestIndex.setRgt(2L);
		//
		ForestIndexEntity node = service.saveNode(forestIndex);
		// reindex should occur
		Assert.assertNotNull(node.getId());
		Assert.assertNotNull(node.getLft());
		Assert.assertNotNull(node.getRgt());
	}
	
	@Test
	public void testSaveNodeWithoutReindex() {
		ForestIndexEntity forestIndex = new ForestIndexEntity(null);
		forestIndex.setId(23469L); // mock
		forestIndex.setLft(1L);
		forestIndex.setRgt(2L);
		//
		ForestIndexEntity node = service.saveNode(forestIndex);
		// reindex should occur
		Assert.assertNotNull(node.getId());
		Assert.assertNotNull(node.getLft());
		Assert.assertNotNull(node.getRgt());
		Assert.assertEquals(Long.valueOf(1L), node.getLft());
		Assert.assertEquals(Long.valueOf(2L), node.getRgt());
	}
	
	@Test
	public void testDeleteNode() {
		testSaveTree();
		Assert.assertEquals(8, repository.count());
		
		service.deleteNode(b, false);
		
		ForestIndexEntity root = repository.findRoot(ForestIndex.DEFAULT_TREE_TYPE);		
		
		Assert.assertEquals(5, repository.count());
		Assert.assertEquals(7, (root.getRgt() - root.getLft()) / 2); // TODO: this is discutable, maybe delete without closing gap is dangerous
	}
	
	@Test
	public void testDeleteNodeCloseGap() {
		testSaveTree();
		Assert.assertEquals(8, repository.count());
		
		service.deleteNode(b, true);
		
		ForestIndexEntity root = repository.findRoot(ForestIndex.DEFAULT_TREE_TYPE);
		
		Assert.assertEquals(5, repository.count());
		Assert.assertEquals(4, (root.getRgt() - root.getLft()) / 2);
	}
	
	@Test
	public void testDeleteDropIndexNotFound() {
		Assert.assertNull(service.dropIndex(3534143455L));
	}
	
	@Test
	public void testFunctionalRebuild() {
		testSaveTree();
		
		Assert.assertEquals(8, repository.count());
		
		service.clearIndexes(ForestIndex.DEFAULT_TREE_TYPE);
		
		b = repository.findById(b.getId()).get();
		Assert.assertEquals(null, b.getLft());
		Assert.assertEquals(null, b.getLft());
		//
		service.rebuild(ForestIndex.DEFAULT_TREE_TYPE);
		//	
		ForestIndexEntity root = repository.findRoot(ForestIndex.DEFAULT_TREE_TYPE);
		b = repository.findById(b.getId()).get();
		ba = repository.findById(ba.getId()).get();
		bb = repository.findById(bb.getId()).get();
		
		Assert.assertEquals(1L, root.getLft().longValue());
		Assert.assertEquals(7, (root.getRgt() - root.getLft()) / 2);
		
		Assert.assertEquals(2, (b.getRgt() - b.getLft()) / 2);
		
		Assert.assertTrue(ba.getLft().compareTo(ba.getParent().getLft()) > 0);
		Assert.assertTrue(ba.getRgt().compareTo(ba.getParent().getRgt()) < 0);
		
		Assert.assertEquals(bb.getRgt().longValue(), bb.getLft() + 1L);
	}
	
	@Test
	public void testRebuildEmptyTree() {
		service.rebuild(ForestIndex.DEFAULT_TREE_TYPE);
		//
		Assert.assertEquals(0, repository.count());
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void testIndexParentRecursivelyISNotSupported() {
		service.index(ForestIndex.DEFAULT_TREE_TYPE, 1L, 1L);
	}
	
	@Test
	public void testMoveNode() {
		testSaveTree();
		// check initial state
		Assert.assertEquals(8, repository.count());
		Assert.assertEquals(2, repository.findAllChildren(b, null).getTotalElements());
		Assert.assertEquals(2, repository.findAllChildren(a, null).getTotalElements());
		//
		b.setParent(a);
		service.saveNode(b);
		//
		a = repository.findById(a.getId()).get();
		ForestIndexEntity root = repository.findRoot(ForestIndex.DEFAULT_TREE_TYPE);
		Assert.assertEquals(5, repository.findAllChildren(a, null).getTotalElements());
		Assert.assertEquals(5, (a.getRgt() - a.getLft()) / 2);
		Assert.assertEquals(3, repository.findDirectChildren(a).size());
		Assert.assertEquals(7, repository.findAllChildren(root, null).getTotalElements());
		Assert.assertEquals(7, (root.getRgt() - root.getLft()) / 2);
	}
	
	@Test
	public void testTreeRebuildBulk() {
		int nodeCount = 100;
		generateTree(nodeCount);
		//
		long startTime = System.currentTimeMillis();
		System.out.println("Starting tree rebuild with [" + nodeCount + "] nodes ...");
		service.rebuild(ForestIndex.DEFAULT_TREE_TYPE);
		System.out.println("Tree with [" + nodeCount + "] nodes was rebuild: " + (System.currentTimeMillis() - startTime) + "ms");
		//
		ForestIndexEntity root = repository.findRoot(ForestIndex.DEFAULT_TREE_TYPE);
		Assert.assertEquals(nodeCount - 1, (root.getRgt() - root.getLft()) / 2);
		Assert.assertEquals(nodeCount - 1, repository.findAllChildren(root, null).getTotalElements());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testWrongIndexClass() {
		((AbstractForestIndexService<ForestIndexEntity, Long>) service).createIndexInstance(WrongForestIndexEntity.class);
	}
	
	public void generateTree(int nodeCount) {
		long startTime = System.currentTimeMillis();
		int counter = generateChildren(nodeCount, 0, null);
		//
		System.out.println("[" + counter + "] nodes generated: " + (System.currentTimeMillis() - startTime) + "ms");
		//
		ForestIndexEntity root = repository.findRoot(ForestIndex.DEFAULT_TREE_TYPE);
		Assert.assertEquals(counter - 1, (root.getRgt() - root.getLft()) / 2);
		Assert.assertEquals(counter - 1, repository.findAllChildren(root, null).getTotalElements());
	}
	
	private int generateChildren(int total, int counter, ForestIndexEntity parent) {
		int childrenCount = r.nextInt(50) + 1;
		for(int i = 0; i < childrenCount; i++) {
			ForestIndexEntity node = service.saveNode(new ForestIndexEntity(parent));
			if(children.size() < 25) {
				children.add(node);
			}
			if ((i + counter + 1) >= total) {
				return i + counter + 1;
			}
			if((i + counter + 1) % 1000 == 0) {
				System.out.println("[" + (i + counter + 1) + "] nodes generated ...");
			}
		}
		ForestIndexEntity firstChild = children.remove(0);
		counter = generateChildren(total, counter + childrenCount, firstChild);
		if (counter >= total) {
			return counter;
		}
		return counter;		
	}
	
	private class WrongForestIndexEntity extends ForestIndexEntity {
		
		private static final long serialVersionUID = 1L;

		@SuppressWarnings("unused")
		public WrongForestIndexEntity(String wrong) {
			// default constructor is missing
		}
	}
}
