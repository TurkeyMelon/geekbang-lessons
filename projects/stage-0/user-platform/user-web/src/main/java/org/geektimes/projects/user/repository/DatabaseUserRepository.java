package org.geektimes.projects.user.repository;

import org.geektimes.function.ThrowableFunction;
import org.geektimes.projects.user.domain.User;
import org.geektimes.projects.user.sql.DBConnectionManager;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.commons.lang.ClassUtils.wrapperToPrimitive;

public class DatabaseUserRepository implements UserRepository {

    private static final Logger logger = Logger.getLogger(DatabaseUserRepository.class.getName());

    /**
     * 通用处理方式
     */
    private static final Consumer<Throwable> COMMON_EXCEPTION_HANDLER = e -> logger.log(Level.SEVERE, e.getMessage());

    public static final String INSERT_USER_DML_SQL =
            "INSERT INTO users(name,password,email,phoneNumber) VALUES " +
                    "(?,?,?,?)";

    public static final String QUERY_ALL_USERS_DML_SQL = "SELECT id,name,password,email,phoneNumber FROM users";

    private final DBConnectionManager dbConnectionManager;

    public DatabaseUserRepository(DBConnectionManager dbConnectionManager) {
        this.dbConnectionManager = dbConnectionManager;
    }

    public DatabaseUserRepository() {
        this.dbConnectionManager = DBConnectionManager.getInstance();
    }

    private Connection getConnection() {
        return dbConnectionManager.getConnection();
    }

    @Override
    public boolean save(User user) {
        if (user == null) {
            return false;
        }
        execute(INSERT_USER_DML_SQL, COMMON_EXCEPTION_HANDLER, user.getName(), user.getPassword(),
                user.getEmail(), user.getPhoneNumber());
        return true;
    }

    @Override
    public boolean deleteById(Long userId) {
        return false;
    }

    @Override
    public boolean update(User user) {
        return false;
    }

    @Override
    public User getById(Long userId) {
        return null;
    }

    @Override
    public User getByNameAndPassword(String userName, String password) {
        return executeQuery("SELECT id,name,password,email,phoneNumber FROM users WHERE name=? and password=?",
                resultSet -> {
                    Collection<User> users = handleResultSetMapping(resultSet, User.class);
                    if (users.size() > 1) {
                        throw new RuntimeException("There's more than one result of users. Rows: " + users.size());
                    }
                    return users.size() == 1 ? users.iterator().next() : null;
                }, COMMON_EXCEPTION_HANDLER, userName, password);
    }

    @Override
    public Collection<User> getAll() {
        return executeQuery("SELECT id,name,password,email,phoneNumber FROM users", resultSet -> {
            // BeanInfo -> IntrospectionException
            return handleResultSetMapping(resultSet, User.class);
        }, COMMON_EXCEPTION_HANDLER);
    }

    /**
     * @param sql
     * @param function
     * @param <T>
     * @return
     */
    protected <T> T executeQuery(String sql, ThrowableFunction<ResultSet, T> function,
                                 Consumer<Throwable> exceptionHandler, Object... args) {
        try {
            PreparedStatement preparedStatement = buildPreparedStatement(sql, args);
            ResultSet resultSet = preparedStatement.executeQuery();
            // 返回一个 POJO List -> ResultSet -> POJO List
            // ResultSet -> T
            return function.apply(resultSet);
        } catch (Throwable e) {
            exceptionHandler.accept(e);
        }
        return null;
    }

    /**
     * @param sql
     * @param exceptionHandler
     * @param args
     */
    protected void execute(String sql, Consumer<Throwable> exceptionHandler, Object... args) {
        try {
            PreparedStatement preparedStatement = buildPreparedStatement(sql, args);
            preparedStatement.execute();
        } catch (Throwable e) {
            exceptionHandler.accept(e);
        }
    }

    protected PreparedStatement buildPreparedStatement(String sql, Object... args) throws SQLException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            Class argType = arg.getClass();

            Class wrapperType = wrapperToPrimitive(argType);

            if (wrapperType == null) {
                wrapperType = argType;
            }
            // Boolean -> boolean
            String methodName = preparedStatementMethodMappings.get(argType);
            Method method = PreparedStatement.class.getMethod(methodName, int.class, wrapperType);
            method.invoke(preparedStatement, i + 1, arg);
        }
        return preparedStatement;
    }

    private static String mapColumnLabel(String fieldName) {
        return fieldName;
    }

    /**
     * 数据类型与 ResultSet 方法名映射
     */
    static Map<Class, String> resultSetMethodMappings = new HashMap<>();

    static Map<Class, String> preparedStatementMethodMappings = new HashMap<>();

    static {
        resultSetMethodMappings.put(Long.class, "getLong");
        resultSetMethodMappings.put(String.class, "getString");

        preparedStatementMethodMappings.put(Long.class, "setLong"); // long
        preparedStatementMethodMappings.put(String.class, "setString"); //
    }

    protected <T> Collection<T> handleResultSetMapping(ResultSet resultSet, Class<T> returnType) throws SQLException, IntrospectionException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        // BeanInfo -> IntrospectionException
        BeanInfo beanInfo = Introspector.getBeanInfo(returnType, Object.class);
        List<T> pojoList = new ArrayList<>();
        while (resultSet.next()) {
            T instance = returnType.getConstructor().newInstance();
            for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
                String fieldName = propertyDescriptor.getName();
                Class fieldType = propertyDescriptor.getPropertyType();
                String methodName = resultSetMethodMappings.get(fieldType);
                // 可能存在映射关系（不过此处是相等的）
                String columnLabel = mapColumnLabel(fieldName);
                Method resultSetMethod = ResultSet.class.getMethod(methodName, String.class);
                // 通过放射调用 getXXX(String) 方法
                Object resultValue = resultSetMethod.invoke(resultSet, columnLabel);
                // PropertyDescriptor ReadMethod 等于 Getter 方法
                // PropertyDescriptor WriteMethod 等于 Setter 方法
                Method setterMethodFromUser = propertyDescriptor.getWriteMethod();
                setterMethodFromUser.invoke(instance, resultValue);
            }
            pojoList.add(instance);
        }
        return pojoList;
    }
}