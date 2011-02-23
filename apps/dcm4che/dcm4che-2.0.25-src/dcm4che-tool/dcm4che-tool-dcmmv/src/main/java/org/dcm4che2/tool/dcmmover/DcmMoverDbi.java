package org.dcm4che2.tool.dcmmover;

import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

import org.dcm4che2.net.NewThreadExecutor;
import org.dcm4che2.util.CloseUtils;

/**
 * Provides a database interface to monitor the DICOM study move.
 * 
 * @author gpotter (gcac96@gmail.com)
 * @version $Revision$
 */
public class DcmMoverDbi extends DcmMover implements CheckCancelTimerOwner {

    DcmMoverDbi dcmMover = null;

    String studyUid;

    ObjectTransformData xformObjectData = null;

    String dbUrl;

    Connection dbConnection;

    final class InsertStmt {
        static final String SQL = "insert into moves(status, successful, study_uid, num_objects_found, num_objects_moved, "
                + "source_ae, destination_ae, anonymized, anonymize_data, move_started) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        static final int PARAM_STATUS = 1, PARAM_SUCCESSFUL = 2, PARAM_STUDY_UID = 3, PARAM_NUM_OBJECTS_FOUND = 4,
                PARAM_NUM_OBJECTS_MOVED = 5, PARAM_SOURCE_AE = 6, PARAM_DESTINATION_AE = 7, PARAM_ANONYMIZED = 8, PARAM_ANONYMIZE_DATA = 9,
                PARAM_MOVE_STARTED = 10;

        static final int RETURN_COL_ID = 1;
    }

    final class UpdateStmt {
        static final String SQL = "update moves set num_objects_found = ?, num_objects_moved = ?, uid_mapping_doc = ?, "
                + "storage_commit_failures_doc = ?, move_updated = ? where id = ?";

        static final int PARAM_NUM_OBJECTS_FOUND = 1, PARAM_NUM_OBJECTS_MOVED = 2, PARAM_UID_MAPPING_DOC = 3,
                PARAM_STG_CMMT_FAILURES_DOC = 4, PARAM_MOVE_UPDATED = 5;

        static final int CONSTRAINTS_ID = 6;
    }

    PreparedStatement updateStatement = null;

    final class CompleteStmt {
        static final String SQL = "update moves set status = ?, successful = ?, error = ?, num_objects_found = ?, num_objects_moved = ?, "
                + "uid_mapping_doc = ?, storage_commit_failures_doc = ?, move_ended = ? where id = ?";

        static final int PARAM_STATUS = 1, PARAM_SUCCESSFUL = 2, PARAM_ERROR = 3, PARAM_NUM_OBJECTS_FOUND = 4, PARAM_NUM_OBJECTS_MOVED = 5,
                PARAM_MAPPING_DOC = 6, PARAM_STG_CMMT_FAILURES_DOC = 7, PARAM_MOVE_ENDED = 8;

        static final int CONSTRAINTS_ID = 9;
    }

    final class CheckCancelQry {
        static final String SQL = "select status from moves where id = ?";

        static final int COL_STATUS = 1;

        static final int CONSTRAINTS_ID = 1;
    }

    PreparedStatement checkCancelStatement = null;

    enum MoveStatus {
        IN_PROGESS("in_progress"), COMPLETE("complete"), CANCELLING("cancelling"), CANCELLED("cancelled");
        String status;

        MoveStatus(String status) {
            this.status = status;
        }

        @Override
        public String toString() {
            return status;
        }

        public boolean equal(String status) {
            return this.status.equalsIgnoreCase(status);
        }
    }

    int moveId = 0;

    int num_objects_found_cache = 0;

    int num_objects_moved_cache = 0;

    Timer checkCancelTimer = null;

    private static final long CANCEL_TIMER_START_DELAY = 1000;

    private static final long CANCEL_TIMER_PERIOD = 60000;

    private AtomicReference<String> lastError = new AtomicReference<String>();

    public DcmMoverDbi(String adapter, String host, Integer port, String database, String username, String password) {

        dbUrl = "jdbc:" + adapter + "://" + host + (port == null ? "" : ":" + port.toString()) + "/" + database + "?user=" + username
                + "&password=" + password;
    }

    @Override
    public void finalize() {
        final String fn = "finalize: ";

        log.info(fn + "Releasing move instance resources.");

        try {
            if (updateStatement != null) {
                updateStatement.close();
            }
            if (checkCancelStatement != null) {
                checkCancelStatement.close();
            }
            if (dbConnection != null && !dbConnection.isClosed()) {
                dbConnection.close();
            }
        } catch (SQLException e) {
            log.error(fn + "Exception while releasing move instance resources - ignoring. Exception:\n" + e.getMessage());
        }
    }

    /**
     * Starts a new move instance for the study specified. Creates a new
     * database record, starts the move process on a separate thread, and
     * returns the move instance id.
     * 
     * @param studyUid
     *            The study to move.
     * @param xformObjectData
     *            DICOM object attributes to be added/removed/xformed during the
     *            move.
     * @return The new move id, or 0 if failed to start the move. If the move
     *         failed to start you can call getLastError to get a reason for the
     *         failure.
     */
    public int startMove(String studyUid, ObjectTransformData xformObjData) {
        final String fn = "startMove: ";

        log.info(fn + "Request to move study with uid = " + studyUid);

        dcmMover = this;
        this.studyUid = studyUid;
        xformObjectData = xformObjData;
        lastError.set("");

        // Create a database record of this move attempt and get the primary key
        log.debug(fn + "Creating move instance database record");
        PreparedStatement stmt = null;
        try {
            Connection conn = getDbConnection();
            stmt = conn.prepareStatement(InsertStmt.SQL, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(InsertStmt.PARAM_STATUS, MoveStatus.IN_PROGESS.toString());
            stmt.setBoolean(InsertStmt.PARAM_SUCCESSFUL, false);
            stmt.setString(InsertStmt.PARAM_STUDY_UID, studyUid);
            stmt.setInt(InsertStmt.PARAM_NUM_OBJECTS_FOUND, 0);
            stmt.setInt(InsertStmt.PARAM_NUM_OBJECTS_MOVED, 0);
            stmt.setString(InsertStmt.PARAM_SOURCE_AE, getMoveSourceAET());
            stmt.setString(InsertStmt.PARAM_DESTINATION_AE, getMoveDestinationAET());
            stmt.setBoolean(InsertStmt.PARAM_ANONYMIZED, null != xformObjData);
            stmt.setString(InsertStmt.PARAM_ANONYMIZE_DATA, (xformObjData == null ? null : xformObjData.toString()));
            stmt.setTimestamp(InsertStmt.PARAM_MOVE_STARTED, new Timestamp(new java.util.Date().getTime()));
            if (stmt.executeUpdate() != 1) {
                log.error(fn + "Failed to create move instance database record.\nStatement: " + stmt.toString() + "\nWarnings:\n"
                        + stmt.getWarnings().toString());
                lastError.set("Failed to create move instance database record.");
                return 0;
            }
            ResultSet result = stmt.getGeneratedKeys();
            if (!result.next()) {
                log.error(fn + "Created move instance database record, but failed to retrieve new move Id.\nWarnings:\n"
                        + stmt.getWarnings().toString());
                lastError.set("Created move instance database record, but failed to retrieve new move Id.");
                return 0;
            }
            moveId = result.getInt(InsertStmt.RETURN_COL_ID);
        } catch (SQLException e) {
            log.error(fn + "Failed to create move instance database record. Exception:\n" + e.getMessage());
            lastError.set(e.getMessage());
            return 0;
        } finally {
            CloseUtils.safeClose(stmt);
        }

        // Start the move process
        log.debug(fn + "Starting the move process asyncronously. Move Id = " + moveId);
        doMoveAsync();

        // Start a timer to monitor the move process for a client cancel request
        log.debug(fn + "Starting the CHECK_CANCEL_MOVE_TIMER timer");
        checkCancelTimer = new Timer("CHECK_CANCEL_MOVE_TIMER");
        checkCancelTimer.schedule(new CheckCancelTimerTask(this), CANCEL_TIMER_START_DELAY, CANCEL_TIMER_PERIOD);

        log.debug(fn + "Move process started");
        return moveId;
    }

    public String getLastError() {
        return lastError.get();
    }

    // Runs the base class's moveStudy method on a separate thread
    private void doMoveAsync() {
        final String fn = "doMoveAsync: ";

        log.debug(fn + "Starting the move process on a separate thread. Move Id = " + new Integer(moveId).toString());

        new NewThreadExecutor("DCM_MOVER_DBI_ASYNC").execute(new Runnable() {
            public void run() {
                final String fn = "run: ";

                log.info(fn + "Calling the moveStudy method");
                MoveResponse finalMoveResponse = dcmMover.moveStudy(studyUid, xformObjectData);
                log.info(fn + "Received the final move response");

                dcmMover.handleFinalMoveResponse(finalMoveResponse);
                log.info(fn + "Move processing is complete");
            }
        });
    }

    @Override
    protected void handleMoveEvent(ActionEvent e) {
        final String fn = "handleMoveEvent: ";

        super.handleMoveEvent(e);

        log.debug(fn + "Handling a " + e.toString() + " event. Move Id = " + new Integer(moveId).toString());

        if (getNumberOfFoundStudyObjects() != num_objects_found_cache || getNumberOfMovedStudyObjects() != num_objects_moved_cache) {
            num_objects_found_cache = getNumberOfFoundStudyObjects();
            num_objects_moved_cache = getNumberOfMovedStudyObjects();
            log.debug(fn + "Updating the move database record");
            try {
                Connection conn = getDbConnection();
                if (updateStatement == null) {
                    updateStatement = conn.prepareStatement(UpdateStmt.SQL);
                }
                updateStatement.setInt(UpdateStmt.PARAM_NUM_OBJECTS_FOUND, num_objects_found_cache);
                updateStatement.setInt(UpdateStmt.PARAM_NUM_OBJECTS_MOVED, num_objects_moved_cache);
                updateStatement.setString(UpdateStmt.PARAM_UID_MAPPING_DOC, getStudyMoveResponseObj().getUidMappingDoc());
                updateStatement.setString(UpdateStmt.PARAM_STG_CMMT_FAILURES_DOC, getStudyMoveResponseObj().getStorageCommitFailuresDoc());
                updateStatement.setTimestamp(UpdateStmt.PARAM_MOVE_UPDATED, new Timestamp(new java.util.Date().getTime()));
                updateStatement.setInt(UpdateStmt.CONSTRAINTS_ID, moveId);
                if (updateStatement.executeUpdate() != 1) {
                    log.error(fn + "Failed to update move instance database record.\nStatement: " + updateStatement.toString()
                            + "\nWarnings:\n" + updateStatement.getWarnings().toString());
                    lastError.set("Failed to update move instance database record.");
                }
            } catch (SQLException ex) {
                log.error(fn + "Failed to update move instance database record. Exception:\n" + ex.getMessage());
                lastError.set(ex.getMessage());
            }
        }
    }

    protected void handleFinalMoveResponse(MoveResponse finalMoveResponse) {
        final String fn = "handleFinalMoveResponse: ";

        log.debug(fn + "Received the final move response. Move Id = " + new Integer(moveId).toString());

        log.debug(fn + "Shutting down the Check Cancel timer.");
        checkCancelTimer.cancel();

        log.debug(fn + "Updating the move database record");
        num_objects_found_cache = getNumberOfFoundStudyObjects();
        num_objects_moved_cache = finalMoveResponse.getNumberOfStudyObjectsMoved();

        PreparedStatement stmt = null;
        try {
            Connection conn = getDbConnection();
            stmt = conn.prepareStatement(CompleteStmt.SQL);
            stmt.setString(InsertStmt.PARAM_STATUS, MoveStatus.COMPLETE.toString());
            stmt.setInt(CompleteStmt.PARAM_NUM_OBJECTS_FOUND, num_objects_found_cache);
            stmt.setInt(CompleteStmt.PARAM_NUM_OBJECTS_MOVED, num_objects_moved_cache);
            stmt.setString(CompleteStmt.PARAM_MAPPING_DOC, finalMoveResponse.getUidMappingDoc());
            stmt.setString(CompleteStmt.PARAM_STG_CMMT_FAILURES_DOC, finalMoveResponse.getStorageCommitFailuresDoc());
            stmt.setBoolean(CompleteStmt.PARAM_SUCCESSFUL, finalMoveResponse.moveSuccessful());
            stmt.setString(CompleteStmt.PARAM_ERROR, finalMoveResponse.getError());
            stmt.setTimestamp(CompleteStmt.PARAM_MOVE_ENDED, new Timestamp(new java.util.Date().getTime()));
            stmt.setInt(CompleteStmt.CONSTRAINTS_ID, moveId);
            if (stmt.executeUpdate() != 1) {
                log.error(fn + "Failed to update move instance database record.\nStatement: " + stmt.toString() + "\nWarnings:\n"
                        + stmt.getWarnings().toString());
                lastError.set("Failed to update move instance database record.");
            }
        } catch (SQLException ex) {
            log.error(fn + "Failed to update move instance database record. Exception:\n" + ex.getMessage());
            lastError.set(ex.getMessage());
        } finally {
            CloseUtils.safeClose(stmt);
        }
        log.debug(fn + "Move processing is complete");
    }

    private Connection getDbConnection() throws SQLException {
        if (dbConnection == null || dbConnection.isClosed()) {
            return dbConnection = DriverManager.getConnection(dbUrl);
        }
        return dbConnection;
    }

    public void timerFired() {
        final String fn = "timerFired: ";

        log.debug(fn + "Checking for a move cancel request from client. Move Id = " + new Integer(moveId).toString());
        try {
            Connection conn = getDbConnection();
            if (checkCancelStatement == null) {
                checkCancelStatement = conn.prepareStatement(CheckCancelQry.SQL);
            }
            checkCancelStatement.setInt(CheckCancelQry.CONSTRAINTS_ID, moveId);
            ResultSet result = checkCancelStatement.executeQuery();
            if (!result.next()) {
                log.error(fn + "Failed to check for 'cancel move' request from client.\nStatement: " + checkCancelStatement.toString()
                        + "\nWarnings:\n" + checkCancelStatement.getWarnings().toString());
                lastError.set("Failed to update move instance database record.");
            }
            String status = result.getString(CheckCancelQry.COL_STATUS);
            if (MoveStatus.CANCELLING.equal(status)) {
                log.info(fn + "Detected a client cancel request. Shutting down move process. Move Id = " + new Integer(moveId).toString());
                dcmMover.shutdownMove();
            }

        } catch (SQLException ex) {
            log.error(fn + "Failed to check for 'cancel move' request from client. Exception:\n" + ex.getMessage());
            lastError.set(ex.getMessage());
        }
    }

    class CheckCancelTimerTask extends TimerTask {

        CheckCancelTimerOwner timerOwner = null;

        public CheckCancelTimerTask(CheckCancelTimerOwner owner) {
            timerOwner = owner;
        }

        @Override
        public void run() {
            timerOwner.timerFired();
        }
    }
}
