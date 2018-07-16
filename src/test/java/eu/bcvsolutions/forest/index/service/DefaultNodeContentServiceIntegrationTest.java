package eu.bcvsolutions.forest.index.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.forest.index.domain.ForestIndex;
import eu.bcvsolutions.forest.index.entity.ForestIndexEntity;
import eu.bcvsolutions.forest.index.entity.NodeContent;
import eu.bcvsolutions.forest.index.repository.ForestIndexEntityRepository;
import eu.bcvsolutions.forest.index.repository.NodeContentRepository;
import eu.bcvsolutions.forest.index.service.api.ForestIndexService;

/**
 * Content + index integration tests
 * 
 * @author Radek Tomiška
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class DefaultNodeContentServiceIntegrationTest {
	
	@Autowired private ForestIndexService<ForestIndexEntity, Long> indexService;
	@Autowired private ForestIndexEntityRepository indexRepository;
	@Autowired private NodeContentRepository repository;
	@Autowired private NodeContentService service;
	//
	private Random r = new Random();
	private NodeContent root = null;
	private NodeContent a = null;
	private NodeContent b = null;
	private NodeContent ba = null;
	private NodeContent bb = null;
	private List<NodeContent> children = new ArrayList<>();
	
	@Test
	public void testSaveTreeRoots() {
		NodeContent root = service.save(new NodeContent(null, "root"));
		//
		assertNotNull(root);
		assertNotNull(root.getForestIndex());
		assertEquals(2L, root.getForestIndex().getLft().longValue());
		assertEquals(3L, root.getForestIndex().getRgt().longValue());
		//
		// save another root 
		NodeContent anotherRoot = service.save(new NodeContent(null, "root two"));
		assertEquals(4L, anotherRoot.getForestIndex().getLft().longValue());
		assertEquals(5L, anotherRoot.getForestIndex().getRgt().longValue());
		//
		root = repository.findOne(root.getId());
		assertEquals(2L, root.getForestIndex().getLft().longValue());
		assertEquals(3L, root.getForestIndex().getRgt().longValue());
		//
		// save root child
		NodeContent rootChild = service.save(new NodeContent(root, "root sub"));
		assertEquals(3L, rootChild.getForestIndex().getLft().longValue());
		assertEquals(4L, rootChild.getForestIndex().getRgt().longValue());
		root = repository.findOne(root.getId());
		assertEquals(2L, root.getForestIndex().getLft().longValue());
		assertEquals(5L, root.getForestIndex().getRgt().longValue());
	}
	
	@Test
	public void testSaveTreeRootDifferentType() {
		final String TYPE_ONE = "one";
		final String TYPE_TWO = "two";
		//
		NodeContent rootOne = service.save(new NodeContent(TYPE_ONE, null, "root"));
		//
		assertNotNull(rootOne);
		assertNotNull(rootOne.getForestIndex());
		assertEquals(2L, rootOne.getForestIndex().getLft().longValue());
		assertEquals(3L, rootOne.getForestIndex().getRgt().longValue());
		//
		// save root child
		NodeContent childRootOne = service.save(new NodeContent(TYPE_ONE, rootOne, "root two"));
		assertEquals(3L, childRootOne.getForestIndex().getLft().longValue());
		assertEquals(4L, childRootOne.getForestIndex().getRgt().longValue());
		//
		rootOne = repository.findOne(rootOne.getId());
		assertEquals(rootOne.getId(), childRootOne.getParent().getId());
		assertEquals(2L, rootOne.getForestIndex().getLft().longValue());
		assertEquals(5L, rootOne.getForestIndex().getRgt().longValue());
		//
		NodeContent root = service.save(new NodeContent(TYPE_TWO, null, "root"));
		//
		assertNotNull(root);
		assertNotNull(root.getForestIndex());
		assertEquals(2L, root.getForestIndex().getLft().longValue());
		assertEquals(3L, root.getForestIndex().getRgt().longValue());
		//
		// save root child
		NodeContent childRoot = service.save(new NodeContent(TYPE_TWO, root, "root two"));
		assertEquals(3L, childRoot.getForestIndex().getLft().longValue());
		assertEquals(4L, childRoot.getForestIndex().getRgt().longValue());
		//
		root = repository.findOne(root.getId());
		assertEquals(root.getId(), childRoot.getParent().getId());
		assertEquals(2L, root.getForestIndex().getLft().longValue());
		assertEquals(5L, root.getForestIndex().getRgt().longValue());
	}
	
	@Test
	public void testFindByForestIndex() {
		createTestTree();
		
		assertEquals(2L, root.getForestIndex().getLft().longValue());
		assertEquals(7, (root.getForestIndex().getRgt() - root.getForestIndex().getLft()) / 2); // all children count
		assertEquals(7, repository.findAllChildren(root, null).getTotalElements()); // all children count by query
		assertEquals(2, repository.findAllChildren(b, null).getTotalElements()); // all children count by query
		
		assertEquals(2, (b.getForestIndex().getRgt() - b.getForestIndex().getLft()) / 2);
		
		assertTrue(ba.getForestIndex().getLft().compareTo(ba.getParent().getForestIndex().getLft()) > 0);
		assertTrue(ba.getForestIndex().getRgt().compareTo(ba.getParent().getForestIndex().getRgt()) < 0);
		
		assertEquals(bb.getForestIndex().getRgt().longValue(), bb.getForestIndex().getLft() + 1L);
	}
	
	@Test
	public void testFunctionalRebuild() {
		createTestTree();
		
		assertEquals(8, repository.count());

		indexService.dropIndexes(ForestIndex.DEFAULT_TREE_TYPE);
		b = repository.findOne(b.getId());
		assertEquals(null, b.getForestIndex());
		//
		service.rebuildIndexes(ForestIndex.DEFAULT_TREE_TYPE);				
		//
		Page<NodeContent> roots = service.findRoots(ForestIndex.DEFAULT_TREE_TYPE, null);
		NodeContent root = roots.getContent().get(0);
		b = repository.findOne(b.getId());
		ba = repository.findOne(ba.getId());
		bb = repository.findOne(bb.getId());
		
		assertEquals(2L, root.getForestIndex().getLft().longValue());
		assertEquals(7, (root.getForestIndex().getRgt() - root.getForestIndex().getLft()) / 2);
		
		assertEquals(2, (b.getForestIndex().getRgt() - b.getForestIndex().getLft()) / 2);
		
		assertTrue(ba.getForestIndex().getLft().compareTo(ba.getParent().getForestIndex().getLft()) > 0);
		assertTrue(ba.getForestIndex().getRgt().compareTo(ba.getParent().getForestIndex().getRgt()) < 0);
		
		assertEquals(bb.getForestIndex().getRgt().longValue(), bb.getForestIndex().getLft() + 1L);	
	}
	
	@Test
	public void deleteNode() {
		createTestTree();
		assertEquals(8, repository.count());
		
		service.delete(bb);	
		
		Page<NodeContent> roots = service.findRoots(ForestIndex.DEFAULT_TREE_TYPE, null);
		NodeContent root = roots.getContent().get(0);
		
		assertEquals(7, repository.count());
		assertEquals(8, indexRepository.count()); // +1 - syntetic node is created
		assertEquals(6, (root.getForestIndex().getRgt() - root.getForestIndex().getLft()) / 2);
	}
	
	@Test
	public void deleteNodeWithChildren() {
		createTestTree();
		
		assertEquals(8, repository.count());
		assertEquals(9, indexRepository.count()); // +1 - syntetic node is created
		
		service.delete(b);	
		
		assertEquals(6, indexRepository.count()); // +1 - syntetic node is created
		
		Page<NodeContent> roots = service.findRoots(ForestIndex.DEFAULT_TREE_TYPE, null);
		Assert.assertEquals(1, roots.getTotalElements());
		NodeContent root = roots.getContent().get(0);
		
		assertEquals(4, service.findAllChildren(root.getId(), null).getTotalElements());
		
		root = service.get(root.getId());
		assertEquals(4, (root.getForestIndex().getRgt() - root.getForestIndex().getLft()) / 2);
	}
	
	@Test
	public void testMoveNode() {
		testFindByForestIndex();
		// check initial state
		assertEquals(8, repository.count());
		assertEquals(9, indexRepository.count()); // +1 - syntetic node is created
		assertEquals(2, repository.findAllChildren(b, null).getTotalElements());
		assertEquals(2, repository.findAllChildren(a, null).getTotalElements());
		//
		b.setParent(a);
		service.save(b);
		//
		a = repository.findOne(a.getId());
		Page<NodeContent> roots = service.findRoots(ForestIndex.DEFAULT_TREE_TYPE, null);
		NodeContent root = roots.getContent().get(0);
		assertEquals(5, repository.findAllChildren(a, null).getTotalElements());
		assertEquals(5, (a.getForestIndex().getRgt() - a.getForestIndex().getLft()) / 2);
		assertEquals(3, repository.findDirectChildren(a, null).getTotalElements());
		assertEquals(7, repository.findAllChildren(root, null).getTotalElements());
		assertEquals(7, (root.getForestIndex().getRgt() - root.getForestIndex().getLft()) / 2);
		assertEquals(9, indexRepository.count()); // +1 - syntetic node is created
	}
	
	@Test
	public void testTreeRebuildBulk() {
		int nodeCount = 100;
		generateTree(nodeCount);
		//
		Page<NodeContent> roots = service.findRoots(ForestIndex.DEFAULT_TREE_TYPE, null);
		assertEquals(1, roots.getTotalElements());
		NodeContent root = roots.getContent().get(0);
		assertEquals(nodeCount - 1, (root.getForestIndex().getRgt() - root.getForestIndex().getLft()) / 2);
		assertEquals(nodeCount - 1, repository.findAllChildren(root, null).getTotalElements());
		//
		long startTime = System.currentTimeMillis();
		System.out.println("Starting tree rebuild with [" + nodeCount + "] nodes ...");
		service.rebuildIndexes(ForestIndex.DEFAULT_TREE_TYPE);
		System.out.println("Tree with [" + nodeCount + "] nodes was rebuild: " + (System.currentTimeMillis() - startTime) + "ms");
		//
		roots = service.findRoots(ForestIndex.DEFAULT_TREE_TYPE, null);
		root = roots.getContent().get(0);
		assertEquals(nodeCount - 1, (root.getForestIndex().getRgt() - root.getForestIndex().getLft()) / 2);
		assertEquals(nodeCount - 1, repository.findAllChildren(root, null).getTotalElements());
	}
	
	@Test
	public void testFindAllParents() {
		testFindByForestIndex();
		
		assertEquals(8, repository.count());
		
		List<NodeContent> parents = service.findAllParents(bb.getId(), new Sort(Direction.DESC, "forestIndex.lft"));
		assertEquals(3, parents.size());
		Page<NodeContent> roots = service.findRoots(ForestIndex.DEFAULT_TREE_TYPE, null);
		NodeContent root = roots.getContent().get(0);
		assertEquals(root.getId(), parents.get(2).getId());
	}
	
	@Test
	public void testForestIndexAfterBulkMove() {
		int rootCount = 10;
		//
		// create root nodes
		for (int i = 0; i < rootCount; i++) {
			service.save(new NodeContent(null, "c_" + i));
		}
		Assert.assertEquals(10L, service.findRoots(ForestIndex.DEFAULT_TREE_TYPE, null).getTotalElements());
		//
		// move nodes to the first node
		List<NodeContent> nodes = service.findRoots(ForestIndex.DEFAULT_TREE_TYPE, null).getContent();
		NodeContent root = nodes.get(0);
		for (int i = 0; i < nodes.size(); i++) {
			NodeContent node = nodes.get(i);
			if (node.getId().equals(root.getId())) {
				continue;
			}
			node.setParent(root);
			service.save(node);
		}		
		// check
		root = service.get(root.getId());
		Assert.assertEquals(1L, service.findRoots(ForestIndex.DEFAULT_TREE_TYPE, null).getTotalElements());
		Assert.assertEquals(rootCount - 1, service.findDirectChildren(root.getId(), null).getTotalElements());
		Assert.assertEquals(rootCount - 1, service.findAllChildren(root.getId(), null).getTotalElements());
	}
	
	private void createTestTree() {
		root = service.save(new NodeContent(null, "root"));
		NodeContent rootChild = service.save(new NodeContent(root, "new root"));
		a = service.save(new NodeContent(rootChild, "a"));
		b = service.save(new NodeContent(rootChild, "b"));
		service.save(new NodeContent(a, "aa"));
		service.save(new NodeContent(a, "ab"));
		ba = service.save(new NodeContent(b, "ba"));
		bb = service.save(new NodeContent(b, "bb"));		
		
		assertEquals(8, repository.count());
		
		List<NodeContent> children = repository.findDirectChildren(rootChild, null).getContent();
		assertEquals(2, children.size());
	
		root = repository.findOne(root.getId());
		a = repository.findOne(a.getId());
		b = repository.findOne(b.getId());
		ba = repository.findOne(ba.getId());
		bb = repository.findOne(bb.getId());
	}
	
	private void generateTree(int nodeCount) {
		long startTime = System.currentTimeMillis();
		// create parrent
		NodeContent root = service.save(new NodeContent(null, "root"));
		int counter = generateChildren(nodeCount - 1, 0, root);
		//
		System.out.println("[" + counter + "] nodes generated: " + (System.currentTimeMillis() - startTime) + "ms");
	}
	
	private int generateChildren(int total, int counter, NodeContent parent) {
		int childrenCount = r.nextInt(50) + 1;
		for(int i = 0; i < childrenCount; i++) {
			NodeContent node = service.save(new NodeContent(parent, (parent == null ? "" : parent.getId() + "_") + i));
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
		NodeContent firstChild = children.remove(0);
		counter = generateChildren(total, counter + childrenCount, firstChild);
		if (counter >= total) {
			return counter;
		}
		return counter;		
	}
}
