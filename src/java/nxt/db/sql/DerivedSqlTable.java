package nxt.db.sql;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import nxt.BlockchainImpl;
import nxt.Nxt;
import nxt.db.DerivedTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class DerivedSqlTable implements DerivedTable
{
//    private final Timer rollbackTimer;
//    private final Timer truncateTimer;

    protected final String table;

    protected DerivedSqlTable(String table) {
        this.table = table;
//        rollbackTimer =  Nxt.metrics.timer(MetricRegistry.name(DerivedSqlTable.class, table,"rollback"));
//        truncateTimer =  Nxt.metrics.timer(MetricRegistry.name(DerivedSqlTable.class, table,"truncate"));
        Nxt.getBlockchainProcessor().registerDerivedTable(this);
    }

    @Override
    public void rollback(int height) {
        if (!Db.isInTransaction()) {
            throw new IllegalStateException("Not in transaction");
        }
//        final Timer.Context context = rollbackTimer.time();
        try (Connection con = Db.getConnection();
             PreparedStatement pstmtDelete = con.prepareStatement("DELETE FROM " + table + " WHERE height > ?")) {
            pstmtDelete.setInt(1, height);
            pstmtDelete.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
        finally {
//            context.stop();
        }
    }

    @Override
    public void truncate() {

        if (!Db.isInTransaction()) {
            throw new IllegalStateException("Not in transaction");
        }
//        final Timer.Context context = truncateTimer.time();
        try (Connection con = Db.getConnection();
             Statement stmt = con.createStatement()) {
            stmt.executeUpdate("DELETE FROM " + table);
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    @Override
    public void trim(int height) {
        //nothing to trim
    }

    @Override
    public void finish() {

    }

}
