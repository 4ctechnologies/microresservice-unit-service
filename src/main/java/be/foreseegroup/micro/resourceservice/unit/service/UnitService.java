package be.foreseegroup.micro.resourceservice.unit.service;

import be.foreseegroup.micro.resourceservice.unit.model.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by Kaj on 24/09/15.
 */

@RestController
@RequestMapping("/units")
public class UnitService {
    private static final Logger LOG = LoggerFactory.getLogger(UnitService.class);

    @Autowired
    UnitRepository repo;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<Iterable<Unit>> getAll() {
        Iterable<Unit> units = repo.findAll();
        LOG.info("/units getAll method called, response size: {}", repo.count());
        return new ResponseEntity<>(units, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "{id}")
    public ResponseEntity<Unit> getById(@PathVariable String id) {
        LOG.info("/units getById method called");
        Unit unit = repo.findOne(id);
        if (unit == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(unit, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Unit> create(@RequestBody Unit unit) {
        LOG.info("/units create method called");
        Unit createdUnit = repo.save(unit);
        return new ResponseEntity<>(createdUnit, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "{id}")
    public ResponseEntity<Unit> update(@PathVariable String id, @RequestBody Unit unit) {
        LOG.info("/units update method called");
        Unit update = repo.findOne(id);
        if (update == null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        update.setName(unit.getName());
        return new ResponseEntity<>(update, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "{id}")
    public ResponseEntity<Unit> delete(@PathVariable String id) {
        LOG.info("/units delete method called");
        Unit unit = repo.findOne(id);
        if (unit == null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        repo.delete(unit);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
