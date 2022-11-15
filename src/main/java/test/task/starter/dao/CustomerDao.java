package test.task.starter.dao;

import test.task.starter.entity.Customer;
import test.task.starter.entity.Product;
import test.task.starter.exception.DaoException;
import test.task.starter.util.ConnectionManager;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDao {

    private static final CustomerDao INSTANCE = new CustomerDao();

    private static final String SEARCH_BY_LAST_NAME_SQL =
            "SELECT id, first_name, last_name " +
            "FROM test_task_storage.customer " +
            "WHERE last_name = ?";

    private static final String SEARCH_BY_PRODUCT_NAME_AND_COUNT_SQL =
            "SELECT c.id, first_name, last_name " +
            "FROM test_task_storage.customer c " +
            "JOIN test_task_storage.purchase p " +
            "ON c.id = p.customer_no " +
            "JOIN test_task_storage.product p2 " +
            "ON p2.id = p.product_no " +
            "WHERE p2.name = ? " +
            "GROUP BY c.id, first_name, last_name " +
            "HAVING count(last_name) >= ?";

    private static final String SEARCH_BY_MIN_AND_MAX_SUM_PURCHASE_SQL =
            "SELECT c.id, first_name, last_name " +
            "FROM test_task_storage.customer c " +
            "JOIN test_task_storage.purchase p " +
            "ON c.id = p.customer_no " +
            "JOIN test_task_storage.product p2 " +
            "ON p2.id = p.product_no " +
            "GROUP BY c.id, first_name, last_name " +
            "HAVING sum(p2.price) BETWEEN ? AND ?";

    private static final String SEARCH_BY_MIN_PURCHASE_SQL =
            "SELECT c.id, first_name, last_name " +
            "FROM test_task_storage.customer c " +
            "JOIN test_task_storage.purchase p " +
            "ON c.id = p.customer_no " +
            "JOIN test_task_storage.product p2 " +
            "ON p2.id = p.product_no " +
            "GROUP BY c.id, first_name, last_name " +
            "ORDER BY sum(p2.price) " +
            "LIMIT ?";

    private static final String SELECT_TOTAL_DAYS_SQL =
            "SELECT ( date (?)  - date (?) ) + 1 totalDays";

    private static final String SELECT_CUSTOMERS_SQL =
            "SELECT DISTINCT c.id, first_name, last_name " +
            "FROM test_task_storage.customer c " +
            "JOIN test_task_storage.purchase p " +
            "ON c.id = p.customer_no " +
            "JOIN test_task_storage.product p2 " +
            "ON p2.id = p.product_no " +
            "WHERE p.date >= date (?) " +
            "AND p.date <= date (?)";

    private static final String SELECT_PRODUCT_BY_ID_CUSTOMER_SQL =
            "SELECT p2.id, p2.name, p2.price " +
            "FROM test_task_storage.customer " +
            "         JOIN test_task_storage.purchase p on customer.id = p.customer_no " +
            "         JOIN test_task_storage.product p2 on p2.id = p.product_no " +
            "WHERE customer.id = ? " +
            "  AND p.date >= date (?)" +
            "  AND p.date <= date (?)";

    private static final String SELECT_SUM_PRICE_BY_ID_CUSTOMER_SQL =
            "SELECT sum(p2.price) sum " +
            "FROM test_task_storage.customer " +
            "         JOIN test_task_storage.purchase p on customer.id = p.customer_no " +
            "         JOIN test_task_storage.product p2 on p2.id = p.product_no " +
            "WHERE customer.id = ? " +
            "  AND p.date >= date (?)" +
            "  AND p.date <= date (?)";

    private static final String SELECT_SUM_PRICE_ALL_SQL =
            "SELECT sum(p2.price) sum " +
            "FROM test_task_storage.customer " +
            "         JOIN test_task_storage.purchase p on customer.id = p.customer_no " +
            "         JOIN test_task_storage.product p2 on p2.id = p.product_no " +
            "WHERE p.date >= date (?)" +
            "  AND p.date <= date (?)";

    private static final String SELECT_AVG_PRICE_ALL_SQL =
            "SELECT avg(p2.price) avg " +
            "FROM test_task_storage.customer " +
            "         JOIN test_task_storage.purchase p on customer.id = p.customer_no " +
            "         JOIN test_task_storage.product p2 on p2.id = p.product_no " +
            "WHERE p.date >= date (?)" +
            "  AND p.date <= date (?)";

    private CustomerDao() {
    }

    public static CustomerDao getInstance() {
        return INSTANCE;
    }

    public Integer totalDays(String endData, String startData) {
        try (Connection connection = ConnectionManager.get();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_TOTAL_DAYS_SQL)) {
            preparedStatement.setString(1, endData);
            preparedStatement.setString(2, startData);

            ResultSet resultSet = preparedStatement.executeQuery();
            int result = 0;
            if (resultSet.next()) {
                result = Integer.parseInt(resultSet.getString("totalDays"));
            }

            return result;
        } catch (SQLException e) {
            throw new DaoException(e);
        }

    }

    public List<Customer> customers(String startData, String endData) {
        try (Connection connection = ConnectionManager.get();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_CUSTOMERS_SQL)) {
            preparedStatement.setString(1, startData);
            preparedStatement.setString(2, endData);

            return getCustomers(preparedStatement);
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public List<Product> productByIdCustomer(Integer id, String startData, String endData) {
        try (Connection connection = ConnectionManager.get();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_PRODUCT_BY_ID_CUSTOMER_SQL)) {
            preparedStatement.setInt(1, id);
            preparedStatement.setString(2, startData);
            preparedStatement.setString(3, endData);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Product> products = new ArrayList<>();
            Product product;
            while (resultSet.next()) {
                product = buildProduct(resultSet);
                products.add(product);
            }

            return products;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public BigDecimal sumPriceByIdCustomer(Integer id, String startData, String endData) {
        try (Connection connection = ConnectionManager.get();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_SUM_PRICE_BY_ID_CUSTOMER_SQL)) {
            preparedStatement.setInt(1, id);
            preparedStatement.setString(2, startData);
            preparedStatement.setString(3, endData);
            ResultSet resultSet = preparedStatement.executeQuery();
            BigDecimal result = BigDecimal.valueOf(0);
            if (resultSet.next()) {
                result = resultSet.getBigDecimal("sum");
            }

            return result;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public BigDecimal sumPriceAll(String startData, String endData) {
        try (Connection connection = ConnectionManager.get();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_SUM_PRICE_ALL_SQL)) {
            preparedStatement.setString(1, startData);
            preparedStatement.setString(2, endData);
            ResultSet resultSet = preparedStatement.executeQuery();
            BigDecimal result = BigDecimal.valueOf(0);
            if (resultSet.next()) {
                result = resultSet.getBigDecimal("sum");
            }

            return result;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public BigDecimal avgPriceAll(String startData, String endData) {
        try (Connection connection = ConnectionManager.get();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_AVG_PRICE_ALL_SQL)) {
            preparedStatement.setString(1, startData);
            preparedStatement.setString(2, endData);
            ResultSet resultSet = preparedStatement.executeQuery();
            BigDecimal result = BigDecimal.valueOf(0);
            if (resultSet.next()) {
                result = resultSet.getBigDecimal("avg");
            }

            return result;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    private static Product buildProduct(ResultSet resultSet) throws SQLException {
        return new Product(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getBigDecimal("price")
        );
    }

    public List<Customer> searchByLastName(String lastName) {
        try (Connection connection = ConnectionManager.get();
             PreparedStatement preparedStatement = connection.prepareStatement(SEARCH_BY_LAST_NAME_SQL)) {
            preparedStatement.setString(1, lastName);

            return getCustomers(preparedStatement);
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public List<Customer> searchByNameProductAndCount(String productName, Long count) {
        try (Connection connection = ConnectionManager.get();
             PreparedStatement preparedStatement = connection.prepareStatement(SEARCH_BY_PRODUCT_NAME_AND_COUNT_SQL)) {
            preparedStatement.setString(1, productName);
            preparedStatement.setLong(2, count);

            return getCustomers(preparedStatement);
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public List<Customer> searchByMinAndMaxSumPurchase(Long min, Long max) {
        try (Connection connection = ConnectionManager.get();
             PreparedStatement preparedStatement = connection.prepareStatement(SEARCH_BY_MIN_AND_MAX_SUM_PURCHASE_SQL)) {
            preparedStatement.setLong(1, min);
            preparedStatement.setLong(2, max);

            return getCustomers(preparedStatement);
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public List<Customer> searchByMinPurchase(Long limit) {
        try (Connection connection = ConnectionManager.get();
             PreparedStatement preparedStatement = connection.prepareStatement(SEARCH_BY_MIN_PURCHASE_SQL)) {
            preparedStatement.setLong(1, limit);

            return getCustomers(preparedStatement);
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    private List<Customer> getCustomers(PreparedStatement preparedStatement) throws SQLException {
        ResultSet resultSet = preparedStatement.executeQuery();
        List<Customer> customers = new ArrayList<>();
        Customer customer;
        while (resultSet.next()) {
            customer = buildCustomer(resultSet);
            customers.add(customer);
        }

        return customers;
    }

    private static Customer buildCustomer(ResultSet resultSet) throws SQLException {
        return new Customer(
                resultSet.getInt("id"),
                resultSet.getString("first_name"),
                resultSet.getString("last_name")
        );
    }
}