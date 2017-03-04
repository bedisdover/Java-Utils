
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by song on 17-3-2.
 * <p>
 * 数据库工厂
 */
class DBFactory {

    /**
     * 数据源
     */
    private static DataSource dataSource;

    static {
        try {
            InitialContext context = new InitialContext();
            // TODO 数据源配置
            dataSource = (DataSource) context.lookup("java:/MysqlDS");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 编译 SQL 语句
     */
    private static void compile(PreparedStatement preparedStatement, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            preparedStatement.setObject(i + 1, params[i]);
        }
    }

    /**
     * 执行sql语句，返回结果
     *
     * @param sql    sql语句，未编译，sql语句类型为除 SELECT 语句以外的语句
     * @param params 编译参数列表，需与sql语句中的参数一一对应
     * @return sql语句执行结果
     */
    public static boolean execute(String sql, Object... params) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            compile(preparedStatement, params);

            return preparedStatement.execute();
        }
    }

    /**
     * 执行sql查询语句，返回结果
     * <p>
     * 此方法返回结果只能为简单类型
     *
     * @param sql    sql语句，未编译，sql语句类型为 SELECT 语句
     * @param params 编译参数列表，需与sql语句中的参数一一对应
     * @return sql语句执行结果
     */
    public static Object executeQuery(String sql, Object... params) throws SQLException {
        Object result = null;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            compile(preparedStatement, params);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    result = resultSet.getObject(1);
                }
            }
        }

        return result;
    }


    /**
     * 执行sql查询语句，返回结果
     * <p>
     * 此方法返回结果为复合类型，一般为自定义持久化对象
     *
     * @param sql    sql语句，未编译，sql语句类型为 SELECT 语句
     * @param c      标识返回结果的类型
     * @param params 编译参数列表，需与sql语句中的参数一一对应
     * @return sql语句执行结果
     */
    public static Object executeQuery(String sql, Class c, Object... params) throws SQLException, IllegalAccessException, InstantiationException {
        return executeQueryForList(sql, c, params).get(0);
    }

    /**
     * 执行sql查询语句，返回结果
     * <p>
     * 此方法返回结果为复合类型，一般为自定义持久化对象
     * 此方法返回结果为 @code{java.util.List}
     *
     * @param sql    sql语句，未编译，sql语句类型为 SELECT 语句
     * @param c      标识返回结果的类型
     * @param params 编译参数列表，需与sql语句中的参数一一对应
     * @return sql语句执行结果
     */
    public static List executeQueryForList(String sql, Class c, Object... params) throws SQLException, IllegalAccessException, InstantiationException {
        List<Object> result = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            compile(preparedStatement, params);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    Object temp = c.newInstance();

                    Field[] fields = c.getDeclaredFields();
                    for (Field field : fields) {
                        field.setAccessible(true);
                        field.set(temp, resultSet.getObject(field.getName()));
                    }

                    result.add(temp);
                }
            }
        }

        return result;
    }
}
