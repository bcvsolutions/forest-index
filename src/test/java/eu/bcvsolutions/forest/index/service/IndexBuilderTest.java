package eu.bcvsolutions.forest.index.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

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
public class IndexBuilderTest {
	
	@Autowired
	private ForestIndexEntityRepository repository;
	@Autowired
	private ForestIndexService<ForestIndexEntity, Long> service;
	//
	private Random r = new Random();
	private ForestIndexEntity a = null;
	private ForestIndexEntity b = null;
	private ForestIndexEntity ba = null;
	private ForestIndexEntity bb = null;
	private List<ForestIndexEntity> children = new ArrayList<>();
	
	@After
	public void clear() {
		repository.deleteAll();
	}
	
	@Test
	public void saveTree() {
		ForestIndexEntity oldroot = service.saveNode(new ForestIndexEntity(null));
		a = service.saveNode(new ForestIndexEntity(oldroot));
		b = service.saveNode(new ForestIndexEntity(oldroot));
		service.saveNode(new ForestIndexEntity(a));
		service.saveNode(new ForestIndexEntity(a));
		ba = service.saveNode(new ForestIndexEntity(b));
		bb = service.saveNode(new ForestIndexEntity(b));
		ForestIndexEntity root = service.saveNode(new ForestIndexEntity(null));
		
		assertEquals(8, repository.count());
		
		List<ForestIndexEntity> children = repository.findDirectChildren(oldroot);
		assertEquals(2, children.size());
		
		a = repository.findOne(a.getId());
		b = repository.findOne(b.getId());
		ba = repository.findOne(ba.getId());
		bb = repository.findOne(bb.getId());
		
		assertEquals(1L, root.getLft().longValue());
		assertEquals(7, (root.getRgt() - root.getLft()) / 2); // all children count
		assertEquals(7, repository.findAllChildren(root, null).getTotalElements()); // all children count by query
		assertEquals(2, repository.findAllChildren(b, null).getTotalElements()); // all children count by query
		
		assertEquals(2, (b.getRgt() - b.getLft()) / 2);
		
		assertTrue(ba.getLft().compareTo(ba.getParent().getLft()) > 0);
		assertTrue(ba.getRgt().compareTo(ba.getParent().getRgt()) < 0);
		
		assertEquals(bb.getRgt().longValue(), bb.getLft() + 1L);
	}
	
	@Test
	public void deleteNode() {
		saveTree();
		assertEquals(8, repository.count());
		
		service.deleteNode(b, false);
		
		ForestIndexEntity root = repository.findRoot(ForestIndex.DEFAULT_TREE_TYPE);		
		
		assertEquals(5, repository.count());
		assertEquals(7, (root.getRgt() - root.getLft()) / 2); // TODO: this is discutable, maybe delete without closing gap is dangerous
	}
	
	@Test
	public void deleteNodeCloseGap() {
		saveTree();
		assertEquals(8, repository.count());
		
		service.deleteNode(b, true);
		
		ForestIndexEntity root = repository.findRoot(ForestIndex.DEFAULT_TREE_TYPE);
		
		assertEquals(5, repository.count());
		assertEquals(4, (root.getRgt() - root.getLft()) / 2);
	}
	
	@Test
	public void testFunctionalRebuild() {
		saveTree();
		
		assertEquals(8, repository.count());
		
		service.clearIndexes(ForestIndex.DEFAULT_TREE_TYPE);
		
		b = repository.findOne(b.getId());
		assertEquals(null, b.getLft());
		assertEquals(null, b.getLft());
		//
		service.rebuild(ForestIndex.DEFAULT_TREE_TYPE);
		//	
		ForestIndexEntity root = repository.findRoot(ForestIndex.DEFAULT_TREE_TYPE);
		b = repository.findOne(b.getId());
		ba = repository.findOne(ba.getId());
		bb = repository.findOne(bb.getId());
		
		assertEquals(1L, root.getLft().longValue());
		assertEquals(7, (root.getRgt() - root.getLft()) / 2);
		
		assertEquals(2, (b.getRgt() - b.getLft()) / 2);
		
		assertTrue(ba.getLft().compareTo(ba.getParent().getLft()) > 0);
		assertTrue(ba.getRgt().compareTo(ba.getParent().getRgt()) < 0);
		
		assertEquals(bb.getRgt().longValue(), bb.getLft() + 1L);
	}
	
	@Test
	public void testMoveNode() {
		saveTree();
		// check initial state
		assertEquals(8, repository.count());
		assertEquals(2, repository.findAllChildren(b, null).getTotalElements());
		assertEquals(2, repository.findAllChildren(a, null).getTotalElements());
		//
		b.setParent(a);
		service.saveNode(b);
		//
		a = repository.findOne(a.getId());
		ForestIndexEntity root = repository.findRoot(ForestIndex.DEFAULT_TREE_TYPE);
		assertEquals(5, repository.findAllChildren(a, null).getTotalElements());
		assertEquals(5, (a.getRgt() - a.getLft()) / 2);
		assertEquals(3, repository.findDirectChildren(a).size());
		assertEquals(7, repository.findAllChildren(root, null).getTotalElements());
		assertEquals(7, (root.getRgt() - root.getLft()) / 2);
	}
	
	public void generateTree(int nodeCount) {
		long startTime = System.currentTimeMillis();
		int counter = generateChildren(nodeCount, 0, null);
		//
		System.out.println("[" + counter + "] nodes generated: " + (System.currentTimeMillis() - startTime) + "ms");
		//
		ForestIndexEntity root = repository.findRoot(ForestIndex.DEFAULT_TREE_TYPE);
		assertEquals(counter - 1, (root.getRgt() - root.getLft()) / 2);
		assertEquals(counter - 1, repository.findAllChildren(root, null).getTotalElements());
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
		assertEquals(nodeCount - 1, (root.getRgt() - root.getLft()) / 2);
		assertEquals(nodeCount - 1, repository.findAllChildren(root, null).getTotalElements());
	}
	
	
	private int generateChildren(int total, int counter, ForestIndexEntity parent) {
		int childrenCount = r.nextInt(50);
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
		for (ForestIndexEntity child : children) {
			counter = generateChildren(total, counter + childrenCount, child);
			if (counter >= total) {
				return counter;
			}
			children.remove(child);
		}
		return counter;		
	}
}
