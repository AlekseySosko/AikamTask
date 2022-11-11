package test.task.starter;

import org.postgresql.Driver;
import test.task.starter.util.ConnectionManager;

import java.sql.Connection;
import java.sql.SQLException;

public class TaskRunner {

    public static void main(String[] args) throws SQLException {
        Class<Driver> driverClass = Driver.class;
        try (Connection connection = ConnectionManager.get()) {
            System.out.println(connection.getTransactionIsolation());
        }
    }
}
