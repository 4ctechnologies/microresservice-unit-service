package be.foreseegroup.micro.resourceservice.unit.service;

import be.foreseegroup.micro.resourceservice.unit.model.Unit;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by Kaj on 24/09/15.
 */
public interface UnitRepository extends CrudRepository<Unit, String> {
}
