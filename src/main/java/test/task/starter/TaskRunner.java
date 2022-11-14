package test.task.starter;

import test.task.starter.dao.CustomerDao;
import test.task.starter.entity.Customer;
import test.task.starter.entity.Product;

import java.math.BigDecimal;
import java.util.List;


public class TaskRunner {

    public static void main(String[] args) {
        stat("2022-04-02", "2022-10-04");
    }

    private static void stat(String startData, String endData) {
        Integer totalDays = CustomerDao.getInstance().totalDays(endData, startData);
        System.out.println(totalDays);

        List<Customer> customers = CustomerDao.getInstance().customers(startData, endData);
        for (Customer customer : customers) {
            System.out.println(customer);

            List<Product> products = CustomerDao.getInstance().productByIdCustomer(customer.getId(), startData, endData);
            for (Product product : products) {
                System.out.println(product);
            }

            BigDecimal sumPriceByIdCustomer = CustomerDao.getInstance().sumPriceByIdCustomer(customer.getId(), startData, endData);
            System.out.println(sumPriceByIdCustomer);
        }

        BigDecimal sumPriceAll = CustomerDao.getInstance().sumPriceAll(startData, endData);
        System.out.println(sumPriceAll);

        BigDecimal avgPriceAll = CustomerDao.getInstance().avgPriceAll(startData, endData);
        System.out.println(avgPriceAll);
    }

    private static void badCustomers(Integer limit) {
        List<Customer> customers = CustomerDao.getInstance().searchByMinPurchase(limit);
        printCustomers(customers);
    }

    private static void minAndMaxExpenses(Integer min, Integer max) {
        List<Customer> customers = CustomerDao.getInstance().searchByMinAndMaxSumPurchase(200, 700);
        printCustomers(customers);
    }

    private static void productNameAndMinTimes(String productName, Integer count) {
        List<Customer> customers = CustomerDao.getInstance().searchByNameProductAndCount(productName, count);
        printCustomers(customers);
    }

    private static void lastName(String lastName) {
        List<Customer> customers = CustomerDao.getInstance().searchByLastName(lastName);
        printCustomers(customers);
    }

    private static void printCustomers(List<Customer> customers) {
        if (customers.size() == 0) {
            System.out.println("Ничего не найдено");
        }
        for (Customer customer : customers) {
            System.out.println(customer);
        }
    }
}
