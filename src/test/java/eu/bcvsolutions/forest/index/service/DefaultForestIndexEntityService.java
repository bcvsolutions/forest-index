package eu.bcvsolutions.forest.index.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.forest.index.entity.ForestIndexEntity;
import eu.bcvsolutions.forest.index.repository.ForestIndexEntityRepository;
import eu.bcvsolutions.forest.index.service.impl.AbstractForestIndexService;

/**
 *
 * @author Radek Tomiška
 */
@Service
public class DefaultForestIndexEntityService extends AbstractForestIndexService<ForestIndexEntity, Long> {

	@Autowired
	public DefaultForestIndexEntityService(ForestIndexEntityRepository repository) {
		super(repository);
	}	
}
