package de.metalcon.server.tomcat.NSSP.create;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.AbstractGraphDatabase;

import de.metalcon.server.tomcat.NSSP.ProtocolConstants;
import de.metalcon.server.tomcat.NSSP.RequestTest;
import de.metalcon.server.tomcat.NSSP.create.statusupdate.CreateStatusUpdateRequest;
import de.metalcon.server.tomcat.NSSP.create.statusupdate.CreateStatusUpdateResponse;
import de.metalcon.server.tomcat.NSSP.delete.DeleteStatusUpdateRequestTest;
import de.metalcon.socialgraph.NeoUtils;
import de.metalcon.socialgraph.algorithms.AlgorithmTests;
import de.metalcon.utils.formItemList.FormItemList;

public class CreateStatusUpdateRequestTest extends CreateRequestTest {

    /**
     * valid create request type: status update
     */
    private static final String VALID_TYPE = CreateRequestType.STATUS_UPDATE
            .getIdentifier();

    /**
     * valid status update identifier
     */
    private static final String VALID_STATUS_UPDATE_IDENTIFIER = "stup2";

    /**
     * invalid status update identifier (used identifier)
     */
    private static final String INVALID_STATUS_UPDATE_IDENTIFIER =
            DeleteStatusUpdateRequestTest.VALID_STATUS_UPDATE_IDENTIFIER;

    /**
     * valid status update template
     */
    private static final String VALID_STATUS_UPDATE_TYPE = "Plain";

    /**
     * invalid status update template
     */
    private static final String INVALID_STATUS_UPDATE_TYPE = "blablubb";

    /**
     * create status update request object
     */
    private CreateStatusUpdateRequest createStatusUpdateRequest;

    /**
     * create a test status update in the database
     * 
     * @param graphDatabase
     *            social graph database to operate on
     * @param statusUpdateId
     *            identifier of the new status update
     */
    public static void createStatusUpdate(
            final AbstractGraphDatabase graphDatabase,
            final String statusUpdateId) {
        final Transaction transaction = graphDatabase.beginTx();

        try {
            NeoUtils.createStatusUpdateNode(statusUpdateId);
            transaction.success();
        } finally {
            transaction.finish();
        }
    }

    @BeforeClass
    public static void beforeClass() {
        RequestTest.beforeClass();

        // create a status update
        createStatusUpdate(AlgorithmTests.DATABASE,
                INVALID_STATUS_UPDATE_IDENTIFIER);
        assertNotNull(NeoUtils
                .getStatusUpdateByIdentifier(INVALID_STATUS_UPDATE_IDENTIFIER));
    }

    private void fillRequest(
            final String type,
            final String userId,
            final String statusUpdateId,
            final String statusUpdateType) {
        // create form item list
        final FormItemList formItemList = new FormItemList();
        formItemList.addField(ProtocolConstants.Parameters.Create.TYPE, type);

        final CreateResponse createResponse = new CreateResponse();
        CreateRequest createRequest;
        createRequest =
                CreateRequest.checkRequest(formItemList, createResponse);

        if (createRequest == null) {
            jsonResponse = extractJson(createResponse);
        } else {
            formItemList
                    .addField(
                            ProtocolConstants.Parameters.Create.StatusUpdate.USER_IDENTIFIER,
                            userId);
            formItemList
                    .addField(
                            ProtocolConstants.Parameters.Create.StatusUpdate.STATUS_UPDATE_IDENTIFIER,
                            statusUpdateId);
            formItemList
                    .addField(
                            ProtocolConstants.Parameters.Create.StatusUpdate.STATUS_UPDATE_TYPE,
                            statusUpdateType);

            final CreateStatusUpdateResponse createStatusUpdateResponse =
                    new CreateStatusUpdateResponse();
            createStatusUpdateRequest =
                    CreateStatusUpdateRequest.checkRequest(formItemList,
                            createRequest, createStatusUpdateResponse);
            jsonResponse = extractJson(createStatusUpdateResponse);
        }
    }

    @Test
    public void testParameterMissing() {
        // missing: create request type
        fillRequest(null, VALID_USER_IDENTIFIER,
                VALID_STATUS_UPDATE_IDENTIFIER, VALID_STATUS_UPDATE_TYPE);
        assertEquals(MISSING_PARAM_BEFORE
                + ProtocolConstants.Parameters.Create.TYPE
                + MISSING_PARAM_AFTER,
                jsonResponse.get(ProtocolConstants.STATUS_MESSAGE));

        // missing: user identifier
        fillRequest(VALID_TYPE, null, VALID_STATUS_UPDATE_IDENTIFIER,
                VALID_STATUS_UPDATE_TYPE);
        assertEquals(
                MISSING_PARAM_BEFORE
                        + ProtocolConstants.Parameters.Create.StatusUpdate.USER_IDENTIFIER
                        + MISSING_PARAM_AFTER,
                jsonResponse.get(ProtocolConstants.STATUS_MESSAGE));

        // missing: status update identifier
        fillRequest(VALID_TYPE, VALID_USER_IDENTIFIER, null,
                VALID_STATUS_UPDATE_TYPE);
        assertEquals(
                MISSING_PARAM_BEFORE
                        + ProtocolConstants.Parameters.Create.StatusUpdate.STATUS_UPDATE_IDENTIFIER
                        + MISSING_PARAM_AFTER,
                jsonResponse.get(ProtocolConstants.STATUS_MESSAGE));

        // missing: status update type
        fillRequest(VALID_TYPE, VALID_USER_IDENTIFIER,
                VALID_STATUS_UPDATE_IDENTIFIER, null);
        assertEquals(
                MISSING_PARAM_BEFORE
                        + ProtocolConstants.Parameters.Create.StatusUpdate.STATUS_UPDATE_TYPE
                        + MISSING_PARAM_AFTER,
                jsonResponse.get(ProtocolConstants.STATUS_MESSAGE));
    }

    @Test
    public void testTypeValid() {
        fillRequest(VALID_TYPE, VALID_USER_IDENTIFIER,
                VALID_STATUS_UPDATE_IDENTIFIER, VALID_STATUS_UPDATE_TYPE);
        assertFalse(ProtocolConstants.StatusCodes.Create.TYPE_INVALID
                .equals(jsonResponse.get(ProtocolConstants.STATUS_MESSAGE)));
    }

    @Test
    public void testTypeInvalid() {
        fillRequest(INVALID_TYPE, VALID_USER_IDENTIFIER,
                VALID_STATUS_UPDATE_IDENTIFIER, VALID_STATUS_UPDATE_TYPE);
        assertEquals(ProtocolConstants.StatusCodes.Create.TYPE_INVALID,
                jsonResponse.get(ProtocolConstants.STATUS_MESSAGE));
        assertNull(createStatusUpdateRequest);
    }

    @Test
    public void testUserIdentifierValid() {
        fillRequest(VALID_TYPE, VALID_USER_IDENTIFIER,
                VALID_STATUS_UPDATE_IDENTIFIER, VALID_STATUS_UPDATE_TYPE);
        assertFalse(ProtocolConstants.StatusCodes.Create.StatusUpdate.USER_NOT_EXISTING
                .equals(jsonResponse.get(ProtocolConstants.STATUS_MESSAGE)));
    }

    @Test
    public void testUserIdentifierInvalid() {
        fillRequest(VALID_TYPE, INVALID_USER_IDENTIFIER,
                VALID_STATUS_UPDATE_IDENTIFIER, VALID_STATUS_UPDATE_TYPE);
        assertEquals(
                ProtocolConstants.StatusCodes.Create.StatusUpdate.USER_NOT_EXISTING,
                jsonResponse.get(ProtocolConstants.STATUS_MESSAGE));
        assertNull(createStatusUpdateRequest);
    }

    @Test
    public void testStatusUpdateIdentifierValid() {
        fillRequest(VALID_TYPE, VALID_USER_IDENTIFIER,
                VALID_STATUS_UPDATE_IDENTIFIER, VALID_STATUS_UPDATE_TYPE);
        assertFalse(ProtocolConstants.StatusCodes.Create.StatusUpdate.STATUS_UPDATE_EXISTING
                .equals(jsonResponse.get(ProtocolConstants.STATUS_MESSAGE)));
    }

    @Test
    public void testStatusUpdateIdentifierInvalid() {
        fillRequest(VALID_TYPE, VALID_USER_IDENTIFIER,
                INVALID_STATUS_UPDATE_IDENTIFIER, VALID_STATUS_UPDATE_TYPE);
        assertEquals(
                ProtocolConstants.StatusCodes.Create.StatusUpdate.STATUS_UPDATE_EXISTING,
                jsonResponse.get(ProtocolConstants.STATUS_MESSAGE));
        assertNull(createStatusUpdateRequest);
    }

    @Test
    public void testStatusUpdateTypeValid() {
        fillRequest(VALID_TYPE, VALID_USER_IDENTIFIER,
                VALID_STATUS_UPDATE_IDENTIFIER, VALID_STATUS_UPDATE_TYPE);
        assertFalse(ProtocolConstants.StatusCodes.Create.StatusUpdate.STATUS_UPDATE_TYPE_NOT_EXISTING
                .equals(jsonResponse.get(ProtocolConstants.STATUS_MESSAGE)));
    }

    @Test
    public void testStatusUpdateTypeInvalid() {
        fillRequest(VALID_TYPE, VALID_USER_IDENTIFIER,
                VALID_STATUS_UPDATE_IDENTIFIER, INVALID_STATUS_UPDATE_TYPE);
        assertEquals(
                ProtocolConstants.StatusCodes.Create.StatusUpdate.STATUS_UPDATE_TYPE_NOT_EXISTING,
                jsonResponse.get(ProtocolConstants.STATUS_MESSAGE));
        assertNull(createStatusUpdateRequest);
    }

}
