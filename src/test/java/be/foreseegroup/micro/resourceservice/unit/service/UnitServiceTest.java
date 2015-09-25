package be.foreseegroup.micro.resourceservice.unit.service;

import be.foreseegroup.micro.resourceservice.unit.UnitServiceApplication;
import be.foreseegroup.micro.resourceservice.unit.model.Unit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by Kaj on 25/09/15.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = UnitServiceApplication.class)
@WebIntegrationTest
public class UnitServiceTest {

    private static final String ROOT_PATH = "http://localhost:8888";
    private static final String UNIT_PATH = "/units";
    private static final String UNIT_RESOURCE = ROOT_PATH + UNIT_PATH;
    private static final Unit UNIT_1 = new Unit("testUnit1");
    private static final Unit UNIT_2 = new Unit("testUnit2");
    private static final String NON_EXISTING_ID = "nonExistingId";

    @Autowired
    private UnitRepository repo;

    private RestTemplate restTemplate = new TestRestTemplate();


    @Before
    public void setUp() throws Exception {
        repo.deleteAll();
    }

    @After
    public void tearDown() throws Exception {
        repo.deleteAll();
    }

    /** Test case: getExistingPersonShouldReturnPerson
     *
     * Test if a GET result on an existing entry return the entry itself
     * Also, the Http response should have HttpStatus Code: OK (200)
     */

    @Test
    public void getExistingPersonShouldReturnPerson() {
        //Add the Unit that we will try to GET request to the database
        Unit savedUnit = repo.save(UNIT_1);

        String url = UNIT_RESOURCE + "/" + savedUnit.getId();

        //Instantiate the HTTP GET Request
        ResponseEntity<Unit> response = restTemplate.getForEntity(url, Unit.class);

        //Check if we received a response
        assertNotNull("Http Request response was null", response);

        //Check if we receive the correct HttpStatus code
        HttpStatus expectedCode = HttpStatus.OK;
        assertEquals("HttpStatus code did not match", expectedCode, response.getStatusCode());

        //Check if the response contained a Unit object in its body
        assertNotNull("Http Request response body did not contain a Unit object", response.getBody());


        Unit receivedUnit = response.getBody();

        //Finally, match if the values of the received Unit are valid
        assertEquals("ID of the received object did id invalid", savedUnit.getId(), receivedUnit.getId());
        assertEquals("Name of the received object did id invalid", savedUnit.getName(), receivedUnit.getName());
    }

    /** Test case: getUnexistingPersonShouldReturnHttpNotFoundError
     *
     * Test if a GET result on an unexisting entry return an error
     * It should not contain an object in its body
     * It should return a HttpStatus code: NOT_FOUND (404)
     */

    @Test
    public void getUnexistingPersonShouldReturnHttpNotFoundError() {
        String url = UNIT_RESOURCE + "/" + NON_EXISTING_ID;

        //Instantiate the HTTP GET Request
        ResponseEntity<Unit> response = restTemplate.getForEntity(url, Unit.class);

        //Check if we received a response
        assertNotNull("Http Request response was null", response);

        //Check if we receive the correct HttpStatus code
        HttpStatus expectedCode = HttpStatus.NOT_FOUND;
        assertEquals("HttpStatus code did not match", expectedCode, response.getStatusCode());

        //Check if the response contained a Unit object in its body
        assertNull("Http Request response body did contain a Unit object", response.getBody());
    }

    /** Test case: getPersonsShouldReturnAllPersons
     *
     * Test if a GET results without specifying an ID results all the entries
     * It should contain all the entries in its body
     * It should return HttpStatus code: OK (200)
     */
    @Test
    public void getPersonsShouldReturnAllPersons() {
        //Add the Unit that we will try to GET request to the database
        Unit savedUnit1 = repo.save(UNIT_1);
        Unit savedUnit2 = repo.save(UNIT_2);

        String url = UNIT_RESOURCE;

        //Instantiate the HTTP GET Request
        ParameterizedTypeReference<Iterable<Unit>> responseType = new ParameterizedTypeReference<Iterable<Unit>>() {};
        ResponseEntity<Iterable<Unit>> response = restTemplate.exchange(url, HttpMethod.GET, null, responseType);

        //Check if we received a response
        assertNotNull("Http Request response was null", response);

        //Check if we receive the correct HttpStatus code
        HttpStatus expectedCode = HttpStatus.OK;
        assertEquals("HttpStatus code did not match", expectedCode, response.getStatusCode());

        //Check if the response contained a Unit object in its body
        assertNotNull("Http Request response body did not contain a Unit object", response.getBody());

        //Add the received entries to an ArrayList (has a .size() method to count the entries)
        ArrayList<Unit> responseList = new ArrayList<>();
        if (response.getBody() != null) {
            for (Unit u : response.getBody()) {
                responseList.add(u);
            }

        }

        //Check if the amount of entries is correct
        assertEquals("Response body size did not match", 2, responseList.size());
    }

    /** Test case: createUnitShouldCreateUnit
     *
     * Test if a POST result of a Unit instance results in the Unit being saved to the database
     * The Http Request response should return with the HttpStatus code: OK (200)
     */
    @Test
    public void createUnitShouldCreateUnit() {
        String url = UNIT_RESOURCE;

        //Instantiate the HTTP POST Request
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Unit> httpEntity = new HttpEntity<>(UNIT_1, requestHeaders);
        ResponseEntity<Unit> response = restTemplate.postForEntity(url, httpEntity, Unit.class);

        //Check if we received a response
        assertNotNull("Http Request response was null", response);

        //Check if we receive the correct HttpStatus code
        HttpStatus expectedCode = HttpStatus.OK;
        assertEquals("HttpStatus code did not match", expectedCode, response.getStatusCode());

        //Check if the response contained a Unit object in its body
        assertNotNull("Http Request response body did not contain a Unit object", response.getBody());

        //Check if the returned object is valid in comparison with the published on
        assertEquals("Returned entry is invalid", UNIT_1.getName(), response.getBody().getName());

        //Check if the returned entry contains an ID
        assertNotNull("Returned entry did not contain an ID", response.getBody().getId());

        //Check if the unit was added to the database
        Unit unitFromDb = repo.findOne(response.getBody().getId());

        //Check if the entry that was added is valid
        assertEquals("Name did not match",UNIT_1.getName(),unitFromDb.getName());

        //Check if only 1 entry was added
        assertEquals("More than one record was added to the database", 1, repo.count());
    }

    /** Test case: createUnitWithoutBodyShouldNotAddUnit
     *
     * Test if a POST request without a body does not result in an entry added to the database
     * Also, the Http Request response should have HttpStatus code: BAD_REQUEST (400)
     */
    @Test
    public void createUnitWithoutBodyShouldNotAddUnit() {
        String url = UNIT_RESOURCE;

        //Instantiate the HTTP POST Request
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Unit> httpEntity = new HttpEntity<>(requestHeaders);
        ResponseEntity<Unit> response = restTemplate.postForEntity(url, httpEntity, Unit.class);

        //Check if we received a response
        assertNotNull("Http Request response was null", response);

        //Check if we receive the correct HttpStatus code
        HttpStatus expectedCode = HttpStatus.BAD_REQUEST;
        assertEquals("HttpStatus code did not match", expectedCode, response.getStatusCode());

        //Check if the response contained a Unit object in its body
        assertNotNull("Http Request response body did not contain a Unit object", response.getBody());

        //Check if a Unit was added to the database
        assertEquals("An entry was added to the database", 0, repo.count());
    }

    /** Test case: editUnitShouldSaveEditionsAndReturnUpdatedUnit
     *
     * Test if a PUT request to edit an entry results in the entry being saved
     * The Http Request should respond with an updated entry
     * Also, the Http Request response should have HttpStatus code: OK (200)
     */
    @Test
    public void editUnitShouldSaveEditionsAndReturnUpdatedUnit() {
        //Add the Unit that we will try to PUT request to the database
        Unit savedUnit = repo.save(UNIT_1);

        String url = UNIT_RESOURCE + "/" + savedUnit.getId();

        //Update the Unit
        savedUnit.setName("testUnitEdited");


        //Instantiate the HTTP PUT Request
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Unit> httpEntity = new HttpEntity<>(savedUnit, requestHeaders);
        ResponseEntity<Unit> response = restTemplate.exchange(url, HttpMethod.PUT, httpEntity, Unit.class);

        //Check if we received a response
        assertNotNull("Http Request response was null", response);

        //Check if we receive the correct HttpStatus code
        HttpStatus expectedCode = HttpStatus.OK;
        assertEquals("HttpStatus code did not match", expectedCode, response.getStatusCode());

        //Check if the response contained a Unit object in its body
        assertNotNull("Http Request response body did not contain a Unit object", response.getBody());

        //Check if the returned entry contains is valid
        assertEquals("Returned entry contained invalid field values", savedUnit.getId(), response.getBody().getId());
        assertEquals("Returned entry contained invalid field values", savedUnit.getName(), response.getBody().getName());

        //Fetch the updated entry from the database
        Unit updatedUnit = repo.findOne(savedUnit.getId());

        //Check if the update was saved to the database
        assertEquals("Updated entry was not saved to the database", savedUnit.getName(), updatedUnit.getName());
    }

    /** Test case: editUnexistingUnitShouldReturnError
     *
     * Test that when we try to update an unexisting entry the Http Request response does not contain an object
     * Also, it should have HttpStatus code: BAD_REQUEST (400)
     */
    @Test
    public void editUnexistingUnitShouldReturnError() {
        //Instantiate the HTTP PUT Request
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Unit> httpEntity = new HttpEntity<>(UNIT_1, requestHeaders);
        ResponseEntity<Unit> response = restTemplate.exchange(UNIT_RESOURCE+"/unexistingid", HttpMethod.PUT, httpEntity, Unit.class);

        //Check if we received a response
        assertNotNull("Http Request response was null", response);

        //Check if we receive the correct HttpStatus code
        HttpStatus expectedCode = HttpStatus.BAD_REQUEST;
        assertEquals("HttpStatus code did not match", expectedCode, response.getStatusCode());

        //Check if the response contained a Unit object in its body
        assertNull("Http Request response body did contain an entry object", response.getBody());
    }

    /** Test case: deleteUnexistingUnitShouldReturnError
     *
     * Test that if we try to delete an unexisting entry, this returns the HttpStatus code: BAD_REQUEST (400)
     */
    @Test
    public void deleteUnexistingUnitShouldReturnError() {
        String url = UNIT_RESOURCE + "/" + NON_EXISTING_ID;

        //Instantiate the HTTP DELETE Request
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Unit> httpEntity = new HttpEntity<>(requestHeaders);
        ResponseEntity<Unit> response = restTemplate.exchange(url, HttpMethod.DELETE, httpEntity, Unit.class);

        //Check if we received a response
        assertNotNull("Http Request response was null", response);

        //Check if we receive the correct HttpStatus code
        HttpStatus expectedCode = HttpStatus.BAD_REQUEST;
        assertEquals("HttpStatus code did not match", expectedCode, response.getStatusCode());

        //Check if the response contained a Unit object in its body
        assertNull("Http Request response body did contain an entry object", response.getBody());
    }

    /** Test case: deleteUnitShouldReturnError
     *
     * Test if instantiating a DELETE request on an existing entry results in the entry being deleted
     * The Http Request response should have HttpStatus code: NO_CONTENT (204)
     */
    @Test
    public void deleteUnitShouldReturnError() {
        //Add the Unit that we will try to GET request to the database
        Unit savedUnit = repo.save(UNIT_1);

        String url = UNIT_RESOURCE + "/" + savedUnit.getId();

        //Instantiate the HTTP DELETE Request
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Unit> httpEntity = new HttpEntity<>(requestHeaders);
        ResponseEntity<Unit> response = restTemplate.exchange(url, HttpMethod.DELETE, httpEntity, Unit.class);

        //Check if we received a response
        assertNotNull("Http Request response was null", response);

        //Check if we receive the correct HttpStatus code
        HttpStatus expectedCode = HttpStatus.NO_CONTENT;
        assertEquals("HttpStatus code did not match", expectedCode, response.getStatusCode());

        //Check if the response contained a Unit object in its body
        assertNull("Http Request response body did contain an entry object", response.getBody());

        //Check if the entry was deleted in the database
        assertEquals("Unit was not deleted from the database", 0, repo.count());
    }
}