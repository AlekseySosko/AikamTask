package test.task.starter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import test.task.starter.dao.CustomerDao;
import test.task.starter.entity.Customer;
import test.task.starter.entity.Product;

import java.io.*;
import java.math.BigDecimal;
import java.util.List;


public class TaskRunner {

    private static final String TAG_CRITERIAS = "criterias";
    private static final String TAG_LAST_NAME = "lastName";
    private static final String TAG_PRODUCT_NAME = "productName";
    private static final String TAG_MIN_TIMES = "minTimes";
    private static final String TAG_MIN_EXPENSES = "minExpenses";
    private static final String TAG_MAX_EXPENSES = "maxExpenses";
    private static final String TAG_BAD_CUSTOMERS = "badCustomers";
    private static final String TAG_START_DATE = "startDate";
    private static final String TAG_END_DATE = "endDate";
    private static final JSONObject jsonObject = new JSONObject();

    public static void main(String[] args) {

        String typeResult = "stat";
        String inputFile = "C:\\Users\\sosko\\work\\input.json";
        String outputFile = "C:\\Users\\sosko\\work\\output.json";

        try (FileReader fileReader = new FileReader(inputFile)) {
            JSONParser parser = new JSONParser();
            JSONObject rootJsonObject = (JSONObject) parser.parse(fileReader);

            JSONArray resultsArray = new JSONArray();
            jsonObject.put("type", typeResult);

            if (typeResult.equals("search")) {
                typeSearch(typeResult, rootJsonObject, jsonObject, resultsArray, outputFile);
            } else if (typeResult.equals("stat")) {
                typeStat(rootJsonObject, jsonObject, outputFile);
            } else {
                JSONObject error = new JSONObject();
                error.put("type", "error");
                error.put("message", "неправильный тип результата");
            }
            fileOutputWrite(jsonObject, outputFile);
        } catch (FileNotFoundException e) {
            jsonObject.put("type", "error");
            jsonObject.put("message", "Не удается найти указанный файл");
            fileOutputWrite(jsonObject, outputFile);
            throw new RuntimeException(e);
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }

    private static void typeStat(JSONObject rootJsonObject, JSONObject jsonObject, String outputFile) {
        String startData = (String) rootJsonObject.get(TAG_START_DATE);
        String endData = (String) rootJsonObject.get(TAG_END_DATE);

        String validDate = "\\d{4}-\\d{2}-\\d{2}";
        if (!(startData.matches(validDate) || endData.matches(validDate))) {
            jsonObject.put("type", "error");
            jsonObject.put("message", "Неправильный формат даты");
            fileOutputWrite(jsonObject, outputFile);
            return;
        }

        stat(startData, endData, jsonObject, outputFile);
    }

    private static void typeSearch(String c, JSONObject rootJsonObject, JSONObject jsonObject, JSONArray resultArray, String outputFile) {

        jsonObject.put("type", c);

        JSONArray criteriaArray = (JSONArray) rootJsonObject.get(TAG_CRITERIAS);

        for (Object it : criteriaArray) {
            JSONObject criterionJsonObject = (JSONObject) it;

            String lastName = (String) criterionJsonObject.get(TAG_LAST_NAME);
            String criteria1 = "lastName";
            if (lastName != null) {
                findByLastName(criteria1, lastName, resultArray);
            }

            String productName = (String) criterionJsonObject.get(TAG_PRODUCT_NAME);
            Long minTimes = (Long) criterionJsonObject.get(TAG_MIN_TIMES);
            String criteria2 = "productName";
            String criteria3 = "minTimes";
            if (productName != null) {
                productNameAndMinTimes(productName, minTimes, criteria2, criteria3, resultArray);
            }

            Long minExpenses = (Long) criterionJsonObject.get(TAG_MIN_EXPENSES);
            Long maxExpenses = (Long) criterionJsonObject.get(TAG_MAX_EXPENSES);
            String criteria4 = "minExpenses";
            String criteria5 = "maxExpenses";
            if (minExpenses != null) {
                minAndMaxExpenses(minExpenses, maxExpenses, criteria4, criteria5, resultArray);
            }

            Long badCustomers = (Long) criterionJsonObject.get(TAG_BAD_CUSTOMERS);
            String criteria6 = "badCustomers";
            if (badCustomers != null) {
                badCustomers(badCustomers, criteria6, resultArray);
            }

        }
        jsonObject.put("results", resultArray);
        fileOutputWrite(jsonObject, outputFile);
    }

    private static void stat(String startData, String endData, JSONObject jsonObject, String outputFile) {
        Integer totalDays = CustomerDao.getInstance().totalDays(endData, startData);
        if (totalDays < 0) {
            jsonObject.put("type", "error");
            jsonObject.put("message", "Отрицательный промежуток времени");
            fileOutputWrite(jsonObject, outputFile);
            return;
        }

        jsonObject.put("totalDays", totalDays);

        List<Customer> customers = CustomerDao.getInstance().customers(startData, endData);

        if (customers.size() == 0) {
            jsonObject.put("type", "error");
            jsonObject.put("message", "За данный промежуток времени покупок нет");
            fileOutputWrite(jsonObject, outputFile);
            return;
        }

        JSONArray arrayCustomers = new JSONArray();

        for (Customer customer : customers) {
            JSONObject nameCust = new JSONObject();
            nameCust.put("name", customer.toString());
            arrayCustomers.add(nameCust);

            List<Product> products = CustomerDao.getInstance().productByIdCustomer(customer.getId(), startData, endData);
            JSONObject purchaseJson = new JSONObject();
            JSONArray arrayProduct = new JSONArray();

            for (Product product : products) {
                JSONObject productJson = new JSONObject();
                productJson.put("name", product.getName());
                productJson.put("expenses", product.getPrice());
                arrayProduct.add(productJson);
            }

            purchaseJson.put("purchase", arrayProduct);
            arrayCustomers.add(purchaseJson);

            BigDecimal sumPriceByIdCustomer = CustomerDao.getInstance().sumPriceByIdCustomer(customer.getId(), startData, endData);
            JSONObject totalExpensesJson = new JSONObject();
            totalExpensesJson.put("totalExpenses", sumPriceByIdCustomer);
            arrayCustomers.add(totalExpensesJson);
        }

        jsonObject.put("customers", arrayCustomers);

        BigDecimal sumPriceAll = CustomerDao.getInstance().sumPriceAll(startData, endData);
        jsonObject.put("TotalExpenses", sumPriceAll);

        BigDecimal avgPriceAll = CustomerDao.getInstance().avgPriceAll(startData, endData);
        jsonObject.put("avgExpenses", avgPriceAll);
    }

    private static void badCustomers(Long limit, String criteriaName, JSONArray resultArray) {
        List<Customer> customers = CustomerDao.getInstance().searchByMinPurchase(limit);
        writeCustomers(customers, criteriaName, String.valueOf(limit), resultArray);
    }

    private static void minAndMaxExpenses(Long min, Long max, String nameCriteria1, String nameCriteria2, JSONArray resultArray) {
        List<Customer> customers = CustomerDao.getInstance().searchByMinAndMaxSumPurchase(min, max);
        writeCustomers(customers, nameCriteria1, String.valueOf(min), nameCriteria2, max, resultArray);
    }

    private static void productNameAndMinTimes(String productName, Long count, String nameCriteria1, String nameCriteria2, JSONArray resultArray) {
        List<Customer> customers = CustomerDao.getInstance().searchByNameProductAndCount(productName, count);
        writeCustomers(customers, nameCriteria1, productName, nameCriteria2, count, resultArray);
    }

    private static void findByLastName(String nameCriteria, String lastName, JSONArray resultArray) {
        List<Customer> customers = CustomerDao.getInstance().searchByLastName(lastName);
        writeCustomers(customers, nameCriteria, lastName, resultArray);
    }

    private static void writeCustomers(List<Customer> customers, String nameCriteria, String criteria, JSONArray resultArray) {
        searchValidation(customers, resultArray);

        JSONObject criteriaJson = new JSONObject();
        JSONObject nameCriteriaJson = new JSONObject();
        nameCriteriaJson.put(nameCriteria, criteria);
        criteriaJson.put("criteria", nameCriteriaJson);
        resultArray.add(criteriaJson);

        JSONObject resultCustomer = new JSONObject();

        JSONArray customerArray = new JSONArray();

        for (Customer customer : customers) {
            JSONObject customerJson = new JSONObject();
            customerJson.put(customer.getFirstName(), customer.getLastName());
            customerArray.add(customerJson);
        }

        resultCustomer.put("results", customerArray);
        resultArray.add(resultCustomer);
    }

    private static void writeCustomers(List<Customer> customers, String nameCriteria1, String cr1, String nameCriteria2, Long cr2, JSONArray resultArray) {
        searchValidation(customers, resultArray);

        JSONObject criteriaJson = new JSONObject();
        JSONObject nameCriteriaJson = new JSONObject();
        nameCriteriaJson.put(nameCriteria2, cr2);
        nameCriteriaJson.put(nameCriteria1, cr1);
        criteriaJson.put("criteria", nameCriteriaJson);
        resultArray.add(criteriaJson);

        JSONObject resultCustomer = new JSONObject();

        JSONArray customerArray = new JSONArray();

        for (Customer customer : customers) {
            JSONObject customerJson = new JSONObject();
            customerJson.put(customer.getFirstName(), customer.getLastName());
            customerArray.add(customerJson);
        }

        resultCustomer.put("results", customerArray);
        resultArray.add(resultCustomer);
    }

    private static void fileOutputWrite(JSONObject jsonObject, String outputFile) {
        try (PrintWriter out = new PrintWriter(new FileWriter(outputFile))) {
            out.write(jsonObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void searchValidation(List<Customer> customers, JSONArray resultArray) {
        if (customers.size() == 0) {
            JSONObject error = new JSONObject();
            error.put("type", "error");
            error.put("message", "По заданным критериям ничего не найдено");
            resultArray.add(error);
        }
    }
}
