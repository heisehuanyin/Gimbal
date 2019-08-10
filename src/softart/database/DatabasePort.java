package softart.database;

import java.sql.*;

public class DatabasePort {
    private Connection con = null;

    public DatabasePort(String url, String user, String pswd){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("连接失败");
            System.exit(-1);
        }

        try {
            con = DriverManager.getConnection(url, user, pswd);
            if (con == null)
            {
                System.out.println("连接失败");
                System.exit(-1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("连接失败");
            System.exit(-1);
        }

    }

    public DatabasePort initTables(){
        Statement statement = null;
        try {
            statement = this.con.createStatement();

            // 创建account表
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS user_account" +
                    "(id        bigint      not null auto_increment," +
                    "uuid       text        not null," +
                    "password   text        not null," +
                    "name       text        not null," +
                    "email      longtext    not null," +
                    "m_number   text," +
                    "address    longtext," +
                    "CONSTRAINT ua_id PRIMARY KEY (id));");
            statement = this.con.createStatement();

            // 创建root用户, 赋值操作权限
            ResultSet rs = statement.executeQuery("select id " +
                    "from user_account where name='root'; ");
            if (!rs.next()){
                statement.executeUpdate("insert into user_account " +
                        "(uuid, password, name, email) values ('root','root-password','root', '2422523675@qq.com');");

                statement.executeUpdate("insert into user_account " +
                        "(uuid, password, name, email) values ('anyone',' ','guest',' ');");
            }
            rs.close();
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return this;
    }

    public Statement getEmptyStatement() throws SQLException {
        return this.con.createStatement();
    }

    public PreparedStatement getPreparedStatement(String pString) throws SQLException {
        return this.con.prepareStatement(pString);
    }

}
