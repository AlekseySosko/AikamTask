package test.task.starter.dao;

public class PurchaseDao {

    private static final PurchaseDao INSTANCE = new PurchaseDao();

    private PurchaseDao() {
    }

    public static PurchaseDao getInstance() {
        return INSTANCE;
    }
}
