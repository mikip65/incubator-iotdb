/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.iotdb.db.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.iotdb.db.service.IoTDB;
import org.apache.iotdb.db.utils.EnvironmentUtils;
import org.apache.iotdb.jdbc.Config;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Notice that, all test begins with "IoTDB" is integration test. All test which will start the IoTDB server should be
 * defined as integration test.
 */
public class IoTDBAuthorizationIT {

  private IoTDB daemon;

  public static void main(String[] args) throws Exception {
    for (int i = 0; i < 10; i++) {
      IoTDBAuthorizationIT test = new IoTDBAuthorizationIT();
      test.setUp();
      test.authPerformanceTest();
      test.tearDown();
    }
  }

  @Before
  public void setUp() throws Exception {
    EnvironmentUtils.closeStatMonitor();
    daemon = IoTDB.getInstance();
    daemon.active();
    EnvironmentUtils.envSetUp();
  }

  @After
  public void tearDown() throws Exception {
    daemon.stop();
    EnvironmentUtils.cleanEnv();
  }

  @Test
  public void allPrivilegesTest() throws ClassNotFoundException, SQLException {
    Class.forName(Config.JDBC_DRIVER_NAME);
    Connection adminCon = DriverManager
        .getConnection(Config.IOTDB_URL_PREFIX + "127.0.0.1:6667/", "root", "root");
    Statement adminStmt = adminCon.createStatement();

    adminStmt.execute("CREATE USER tempuser temppw");
    Connection userCon = DriverManager
        .getConnection(Config.IOTDB_URL_PREFIX + "127.0.0.1:6667/", "tempuser",
            "temppw");
    Statement userStmt = userCon.createStatement();

    boolean caught = false;
    try {
      userStmt.execute("SET STORAGE GROUP TO root.a");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);

    caught = false;
    try {
      userStmt.execute("CREATE TIMESERIES root.a.b WITH DATATYPE=INT32,ENCODING=PLAIN");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);

    caught = false;
    try {
      userStmt.execute("INSERT INTO root.a(timestamp, b) VALUES (100, 100)");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);

    caught = false;
    try {
      userStmt.execute("SELECT * from root.a");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);

    caught = false;
    try {
      userStmt.execute("GRANT USER tempuser PRIVILEGES 'CREATE_TIMESERIES' ON root.a");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);

    adminStmt.execute("GRANT USER tempuser PRIVILEGES 'ALL' ON root");

    userStmt.execute("SET STORAGE GROUP TO root.a");
    userStmt.execute("CREATE TIMESERIES root.a.b WITH DATATYPE=INT32,ENCODING=PLAIN");
    userStmt.execute("INSERT INTO root.a(timestamp, b) VALUES (100, 100)");
    userStmt.execute("SELECT * from root.a");
    userStmt.execute("GRANT USER tempuser PRIVILEGES 'SET_STORAGE_GROUP' ON root.a");
    userStmt.execute("GRANT USER tempuser PRIVILEGES 'CREATE_TIMESERIES' ON root.b.b");

    adminStmt.execute("REVOKE USER tempuser PRIVILEGES 'ALL' ON root");

    caught = false;
    try {
      userStmt.execute("SET STORAGE GROUP TO root.b");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);

    caught = false;
    try {
      userStmt.execute("CREATE TIMESERIES root.b.b WITH DATATYPE=INT32,ENCODING=PLAIN");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);

    caught = false;
    try {
      userStmt.execute("INSERT INTO root.b(timestamp, b) VALUES (100, 100)");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);

    caught = false;
    try {
      userStmt.execute("SELECT * from root.b");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);

    caught = false;
    try {
      userStmt.execute("GRANT USER tempuser PRIVILEGES \"CREATE_TIMESERIES\" ON root.a");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);

    adminStmt.close();
    userStmt.close();
    adminCon.close();
    userCon.close();
  }

  @Test
  public void updatePasswordTest() throws ClassNotFoundException, SQLException {
    Class.forName(Config.JDBC_DRIVER_NAME);
    Connection adminCon = DriverManager
        .getConnection(Config.IOTDB_URL_PREFIX + "127.0.0.1:6667/", "root", "root");
    Statement adminStmt = adminCon.createStatement();

    adminStmt.execute("CREATE USER tempuser temppw");
    Connection userCon = DriverManager
        .getConnection(Config.IOTDB_URL_PREFIX + "127.0.0.1:6667/", "tempuser",
            "temppw");
    userCon.close();

    adminStmt.execute("UPDATE USER tempuser SET PASSWORD newpw");

    boolean caught = false;
    try {
      userCon = DriverManager
          .getConnection(Config.IOTDB_URL_PREFIX + "127.0.0.1:6667/", "tempuser", "temppw");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);

    userCon = DriverManager
        .getConnection(Config.IOTDB_URL_PREFIX + "127.0.0.1:6667/", "tempuser", "newpw");

    userCon.close();
    adminCon.close();
  }

  @Test
  public void illegalGrantRevokeUserTest() throws ClassNotFoundException, SQLException {
    Class.forName(Config.JDBC_DRIVER_NAME);
    Connection adminCon = DriverManager
        .getConnection(Config.IOTDB_URL_PREFIX + "127.0.0.1:6667/", "root", "root");
    Statement adminStmt = adminCon.createStatement();

    adminStmt.execute("CREATE USER tempuser temppw");

    Connection userCon = DriverManager
        .getConnection(Config.IOTDB_URL_PREFIX + "127.0.0.1:6667/", "tempuser",
            "temppw");
    Statement userStmt = userCon.createStatement();

    // grant a non-existing user
    boolean caught = false;
    try {
      adminStmt.execute("GRANT USER nulluser PRIVILEGES 'SET_STORAGE_GROUP' on root.a");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);

    // grant a non-existing privilege
    caught = false;
    try {
      adminStmt.execute("GRANT USER tempuser PRIVILEGES 'NOT_A_PRIVILEGE' on root.a");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);

    // duplicate grant
    adminStmt.execute("GRANT USER tempuser PRIVILEGES 'CREATE_USER' on root.a");
    caught = false;
    try {
      adminStmt.execute("GRANT USER tempuser PRIVILEGES 'CREATE_USER' on root.a");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);

    // grant on a illegal seriesPath
    caught = false;
    try {
      adminStmt.execute("GRANT USER tempuser PRIVILEGES 'DELETE_TIMESERIES' on a.b");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);

    // grant admin
    caught = false;
    try {
      adminStmt.execute("GRANT USER root PRIVILEGES 'DELETE_TIMESERIES' on root.a.b");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);

    // no privilege to grant
    caught = false;
    try {
      userStmt.execute("GRANT USER tempuser PRIVILEGES 'DELETE_TIMESERIES' on root.a.b");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);

    // revoke a non-existing privilege
    adminStmt.execute("REVOKE USER tempuser PRIVILEGES 'CREATE_USER' on root.a");
    caught = false;
    try {
      adminStmt.execute("REVOKE USER tempuser PRIVILEGES 'CREATE_USER' on root.a");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);

    // revoke a non-existing user
    caught = false;
    try {
      adminStmt.execute("REVOKE USER tempuser1 PRIVILEGES 'CREATE_USER' on root.a");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);

    // revoke on a illegal seriesPath
    caught = false;
    try {
      adminStmt.execute("REVOKE USER tempuser PRIVILEGES 'DELETE_TIMESERIES' on a.b");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);

    // revoke admin
    caught = false;
    try {
      adminStmt.execute("REVOKE USER root PRIVILEGES 'DELETE_TIMESERIES' on root.a.b");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);

    // no privilege to revoke
    caught = false;
    try {
      userStmt.execute("REVOKE USER tempuser PRIVILEGES 'DELETE_TIMESERIES' on root.a.b");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);

    // grant privilege to grant
    caught = false;
    try {
      userStmt.execute("GRANT USER tempuser PRIVILEGES 'DELETE_TIMESERIES' on root.a.b");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);
    adminStmt.execute("GRANT USER tempuser PRIVILEGES 'GRANT_USER_PRIVILEGE' on root");
    userStmt.execute("GRANT USER tempuser PRIVILEGES 'DELETE_TIMESERIES' on root");

    // grant privilege to revoke
    caught = false;
    try {
      userStmt.execute("REVOKE USER tempuser PRIVILEGES 'DELETE_TIMESERIES' on root");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);
    adminStmt.execute("GRANT USER tempuser PRIVILEGES 'REVOKE_USER_PRIVILEGE' on root");
    userStmt.execute("REVOKE USER tempuser PRIVILEGES 'DELETE_TIMESERIES' on root");

    userCon.close();
    adminCon.close();
  }

  @Test
  public void createDeleteTimeSeriesTest() throws SQLException, ClassNotFoundException {
    Class.forName(Config.JDBC_DRIVER_NAME);
    Connection adminCon = DriverManager
        .getConnection(Config.IOTDB_URL_PREFIX + "127.0.0.1:6667/", "root", "root");
    Statement adminStmt = adminCon.createStatement();

    adminStmt.execute("CREATE USER tempuser temppw");

    Connection userCon = DriverManager
        .getConnection(Config.IOTDB_URL_PREFIX + "127.0.0.1:6667/", "tempuser",
            "temppw");
    Statement userStmt = userCon.createStatement();

    // grant and revoke the user the privilege to create time series
    boolean caught = false;
    try {
      userStmt.execute("SET STORAGE GROUP TO root.a");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);

    adminStmt.execute("GRANT USER tempuser PRIVILEGES 'SET_STORAGE_GROUP' ON root.a");
    userStmt.execute("SET STORAGE GROUP TO root.a");
    adminStmt.execute("GRANT USER tempuser PRIVILEGES 'CREATE_TIMESERIES' ON root.a.b");
    userStmt.execute("CREATE TIMESERIES root.a.b WITH DATATYPE=INT32,ENCODING=PLAIN");

    caught = false;
    try {
      // no privilege to create this one
      userStmt.execute("SET STORAGE GROUP TO root.b");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);

    caught = false;
    try {
      // privilege already exists
      adminStmt.execute("GRANT USER tempuser PRIVILEGES 'SET_STORAGE_GROUP' ON root.a");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);

    adminStmt.execute("REVOKE USER tempuser PRIVILEGES 'SET_STORAGE_GROUP' ON root.a");
    caught = false;
    try {
      // no privilege to create this one any more
      userStmt.execute("SET STORAGE GROUP TO root.a");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);

    caught = false;
    try {
      // no privilege to create timeseries
      userStmt.execute("CREATE TIMESERIES root.b.a WITH DATATYPE=INT32,ENCODING=PLAIN");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);

    caught = false;
    try {
      // privilege already exists
      adminStmt.execute("GRANT USER tempuser PRIVILEGES 'CREATE_TIMESERIES' ON root.a.b");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);

    adminStmt.execute("REVOKE USER tempuser PRIVILEGES 'CREATE_TIMESERIES' ON root.a.b");
    caught = false;
    try {
      // no privilege to create this one any more
      userStmt.execute("CREATE TIMESERIES root.a.b WITH DATATYPE=INT32,ENCODING=PLAIN");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);

    // the user cannot delete the timeseries now
    caught = false;
    try {
      // no privilege to create this one any more
      userStmt.execute("DELETE TIMESERIES root.a.b");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);

    // the user can delete the timeseries now
    adminStmt.execute("GRANT USER tempuser PRIVILEGES 'DELETE_TIMESERIES' on root.a");
    adminStmt.execute("GRANT USER tempuser PRIVILEGES 'DELETE_TIMESERIES' on root.b");
    userStmt.execute("DELETE TIMESERIES root.a.b");

    // revoke the privilege to delete time series
    adminStmt.execute("CREATE TIMESERIES root.a.b WITH DATATYPE=INT32,ENCODING=PLAIN");
    adminStmt.execute("SET STORAGE GROUP TO root.b");
    adminStmt.execute("CREATE TIMESERIES root.b.a WITH DATATYPE=INT32,ENCODING=PLAIN");
    adminStmt.execute("REVOKE USER tempuser PRIVILEGES 'DELETE_TIMESERIES' on root.a");
    userStmt.execute("DELETE TIMESERIES root.b.a");
    caught = false;
    try {
      // no privilege to create this one any more
      userStmt.execute("DELETE TIMESERIES root.a.b");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);

    adminCon.close();
    userCon.close();
  }

  @Test
  public void insertQueryTest() throws ClassNotFoundException, SQLException {
    Class.forName(Config.JDBC_DRIVER_NAME);
    Connection adminCon = DriverManager
        .getConnection(Config.IOTDB_URL_PREFIX + "127.0.0.1:6667/", "root", "root");
    Statement adminStmt = adminCon.createStatement();

    adminStmt.execute("CREATE USER tempuser temppw");

    Connection userCon = DriverManager
        .getConnection(Config.IOTDB_URL_PREFIX + "127.0.0.1:6667/", "tempuser",
            "temppw");
    Statement userStmt = userCon.createStatement();

    adminStmt.execute("GRANT USER tempuser PRIVILEGES 'SET_STORAGE_GROUP' ON root.a");
    userStmt.execute("SET STORAGE GROUP TO root.a");
    adminStmt.execute("GRANT USER tempuser PRIVILEGES 'CREATE_TIMESERIES' ON root.a.b");
    userStmt.execute("CREATE TIMESERIES root.a.b WITH DATATYPE=INT32,ENCODING=PLAIN");

    // grant privilege to insert
    boolean caught = false;
    try {
      userStmt.execute("INSERT INTO root.a(timestamp, b) VALUES (1,100)");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);
    adminStmt.execute("GRANT USER tempuser PRIVILEGES 'INSERT_TIMESERIES' on root.a");
    userStmt.execute("INSERT INTO root.a(timestamp, b) VALUES (1,100)");

    // revoke privilege to insert
    adminStmt.execute("REVOKE USER tempuser PRIVILEGES 'INSERT_TIMESERIES' on root.a");
    caught = false;
    try {
      userStmt.execute("INSERT INTO root.a(timestamp, b) VALUES (1,100)");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);

    // grant privilege to query
    caught = false;
    try {
      userStmt.execute("SELECT * from root.a");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);
    adminStmt.execute("GRANT USER tempuser PRIVILEGES 'READ_TIMESERIES' on root.a");
    userStmt.execute("SELECT * from root.a");
    userStmt.getResultSet().close();

    // revoke privilege to query
    adminStmt.execute("REVOKE USER tempuser PRIVILEGES 'READ_TIMESERIES' on root.a");
    caught = false;
    try {
      userStmt.execute("SELECT * from root.a");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);

    adminCon.close();
    userCon.close();
  }

  @Test
  public void rolePrivilegeTest() throws SQLException, ClassNotFoundException {
    Class.forName(Config.JDBC_DRIVER_NAME);
    Connection adminCon = DriverManager
        .getConnection(Config.IOTDB_URL_PREFIX + "127.0.0.1:6667/", "root", "root");
    Statement adminStmt = adminCon.createStatement();

    adminStmt.execute("CREATE USER tempuser temppw");

    Connection userCon = DriverManager
        .getConnection(Config.IOTDB_URL_PREFIX + "127.0.0.1:6667/", "tempuser",
            "temppw");
    Statement userStmt = userCon.createStatement();

    boolean caught = false;
    try {
      userStmt.execute("CREATE ROLE admin");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);
    adminStmt.execute("CREATE ROLE admin");
    adminStmt.execute(
        "GRANT ROLE admin PRIVILEGES 'SET_STORAGE_GROUP','CREATE_TIMESERIES','DELETE_TIMESERIES','READ_TIMESERIES','INSERT_TIMESERIES' on root");
    adminStmt.execute("GRANT admin TO tempuser");

    userStmt.execute("SET STORAGE GROUP TO root.a");
    userStmt.execute("CREATE TIMESERIES root.a.b WITH DATATYPE=INT32,ENCODING=PLAIN");
    userStmt.execute("CREATE TIMESERIES root.a.c WITH DATATYPE=INT32,ENCODING=PLAIN");
    userStmt.execute("INSERT INTO root.a(timestamp,b,c) VALUES (1,100,1000)");
    // userStmt.execute("DELETE FROM root.a.b WHERE TIME <= 1000000000");
    userStmt.execute("SELECT * FROM root");
    userStmt.getResultSet().close();

    adminStmt.execute("REVOKE ROLE admin PRIVILEGES 'DELETE_TIMESERIES' on root");
    caught = false;
    try {
      userStmt.execute("DELETE FROM root.* WHERE TIME <= 1000000000");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);

    adminStmt.execute("GRANT USER tempuser PRIVILEGES 'READ_TIMESERIES' on root");
    adminStmt.execute("REVOKE admin FROM tempuser");
    userStmt.execute("SELECT * FROM root");
    userStmt.getResultSet().close();
    caught = false;
    try {
      userStmt.execute("CREATE TIMESERIES root.a.b WITH DATATYPE=INT32,ENCODING=PLAIN");
    } catch (SQLException e) {
      caught = true;
    }
    assertTrue(caught);

    adminCon.close();
    userCon.close();
  }

  @Test
  @Ignore
  public void authPerformanceTest() throws ClassNotFoundException, SQLException {
    Class.forName(Config.JDBC_DRIVER_NAME);
    Connection adminCon = DriverManager
        .getConnection(Config.IOTDB_URL_PREFIX + "127.0.0.1:6667/", "root", "root");
    Statement adminStmt = adminCon.createStatement();

    adminStmt.execute("CREATE USER tempuser temppw");
    adminStmt.execute("SET STORAGE GROUP TO root.a");
    int privilegeCnt = 500;
    for (int i = 0; i < privilegeCnt; i++) {
      adminStmt.execute("CREATE TIMESERIES root.a.b" + i + " WITH DATATYPE=INT32,ENCODING=PLAIN");
      adminStmt.execute("GRANT USER tempuser PRIVILEGES 'INSERT_TIMESERIES' ON root.a.b" + i);
    }

    Connection userCon = DriverManager
        .getConnection(Config.IOTDB_URL_PREFIX + "127.0.0.1:6667/", "tempuser",
            "temppw");
    Statement userStmt = userCon.createStatement();

    int insertCnt = 2000000;
    int batchSize = 5000;
    long time;

    time = System.currentTimeMillis();
    for (int i = 0; i < insertCnt; ) {
      for (int j = 0; j < batchSize; j++) {
        userStmt.addBatch(
          "INSERT INTO root.a(timestamp, b" + (privilegeCnt - 1) + ") VALUES (" + (i++ + 1)
            + ", 100)");
      }
      userStmt.executeBatch();
      userStmt.clearBatch();
    }
    System.out.println(
        "User inserted " + insertCnt + " data points used " + (System.currentTimeMillis() - time)
            + " ms with " + privilegeCnt + " privileges");

    time = System.currentTimeMillis();
    for (int i = 0; i < insertCnt; ) {
      for (int j = 0; j < batchSize; j++) {
        adminStmt.addBatch(
          "INSERT INTO root.a(timestamp, b0) VALUES (" + (i++ + 1 + insertCnt) + ", 100)");
      }
      adminStmt.executeBatch();
      adminStmt.clearBatch();
    }
    System.out.println(
        "admin inserted " + insertCnt + " data points used " + (System.currentTimeMillis() - time)
            + " ms with " + privilegeCnt + " privileges");

    adminCon.close();
    userCon.close();
  }

  @Test
  public void testListUser() throws ClassNotFoundException, SQLException {
    Class.forName(Config.JDBC_DRIVER_NAME);
    Connection adminCon = DriverManager
        .getConnection(Config.IOTDB_URL_PREFIX + "127.0.0.1:6667/", "root", "root");
    Statement adminStmt = adminCon.createStatement();

    try {
      ResultSet resultSet = adminStmt.executeQuery("LIST USER");
      String ans = String.format("0,root,\n");
      validateResultSet(resultSet, ans);

      for (int i = 0; i < 10; i++) {
        adminStmt.execute("CREATE USER user" + i + " password" + i);
      }
      resultSet = adminStmt.executeQuery("LIST USER");
      ans = "0,root,\n"
          + "1,user0,\n"
          + "2,user1,\n"
          + "3,user2,\n"
          + "4,user3,\n"
          + "5,user4,\n"
          + "6,user5,\n"
          + "7,user6,\n"
          + "8,user7,\n"
          + "9,user8,\n"
          + "10,user9,\n";
      validateResultSet(resultSet, ans);

      for (int i = 0; i < 10; i++) {
        if (i % 2 == 0) {
          adminStmt.execute("DROP USER user" + i);
        }
      }
      resultSet = adminStmt.executeQuery("LIST USER");
      ans = "0,root,\n"
          + "1,user1,\n"
          + "2,user3,\n"
          + "3,user5,\n"
          + "4,user7,\n"
          + "5,user9,\n";
      validateResultSet(resultSet, ans);
    } finally {
      adminCon.close();
    }
  }

  @Test
  public void testListRole() throws ClassNotFoundException, SQLException {
    Class.forName(Config.JDBC_DRIVER_NAME);
    Connection adminCon = DriverManager
        .getConnection(Config.IOTDB_URL_PREFIX + "127.0.0.1:6667/", "root", "root");
    Statement adminStmt = adminCon.createStatement();

    try {
      ResultSet resultSet = adminStmt.executeQuery("LIST ROLE");
      String ans = "";
      validateResultSet(resultSet, ans);

      for (int i = 0; i < 10; i++) {
        adminStmt.execute("CREATE ROLE role" + i);
      }

      resultSet = adminStmt.executeQuery("LIST ROLE");
      ans = "0,role0,\n"
          + "1,role1,\n"
          + "2,role2,\n"
          + "3,role3,\n"
          + "4,role4,\n"
          + "5,role5,\n"
          + "6,role6,\n"
          + "7,role7,\n"
          + "8,role8,\n"
          + "9,role9,\n";
      validateResultSet(resultSet, ans);

      for (int i = 0; i < 10; i++) {
        if (i % 2 == 0) {
          adminStmt.execute("DROP ROLE role" + i);
        }
      }
      resultSet = adminStmt.executeQuery("LIST ROLE");
      ans = "0,role1,\n"
          + "1,role3,\n"
          + "2,role5,\n"
          + "3,role7,\n"
          + "4,role9,\n";
      validateResultSet(resultSet, ans);

    } finally {
      adminCon.close();
    }
  }

  @Test
  public void testListUserPrivileges() throws SQLException, ClassNotFoundException {
    Class.forName(Config.JDBC_DRIVER_NAME);
    Connection adminCon = DriverManager
        .getConnection(Config.IOTDB_URL_PREFIX + "127.0.0.1:6667/", "root", "root");
    Statement adminStmt = adminCon.createStatement();

    try {
      adminStmt.execute("CREATE USER user1 password1");
      adminStmt.execute("GRANT USER user1 PRIVILEGES 'READ_TIMESERIES' ON root.a.b");
      adminStmt.execute("CREATE ROLE role1");
      adminStmt.execute(
          "GRANT ROLE role1 PRIVILEGES 'READ_TIMESERIES','INSERT_TIMESERIES','DELETE_TIMESERIES' ON root.a.b.c");
      adminStmt.execute(
          "GRANT ROLE role1 PRIVILEGES 'READ_TIMESERIES','INSERT_TIMESERIES','DELETE_TIMESERIES' ON root.d.b.c");
      adminStmt.execute("GRANT role1 TO user1");

      ResultSet resultSet = adminStmt.executeQuery("LIST USER PRIVILEGES  user1");
      String ans = "0,,root.a.b : READ_TIMESERIES"
          + ",\n"
          + "1,role1,root.a.b.c : INSERT_TIMESERIES READ_TIMESERIES DELETE_TIMESERIES"
          + ",\n"
          + "2,role1,root.d.b.c : INSERT_TIMESERIES READ_TIMESERIES DELETE_TIMESERIES"
          + ",\n";
      validateResultSet(resultSet, ans);

      resultSet = adminStmt.executeQuery("LIST PRIVILEGES USER user1 ON root.a.b.c");
      ans = "0,,root.a.b : READ_TIMESERIES"
          + ",\n"
          + "1,role1,root.a.b.c : INSERT_TIMESERIES READ_TIMESERIES DELETE_TIMESERIES"
          + ",\n";
      validateResultSet(resultSet, ans);

      adminStmt.execute("REVOKE role1 from user1");

      resultSet = adminStmt.executeQuery("LIST USER PRIVILEGES  user1");
      ans = "0,,root.a.b : READ_TIMESERIES,\n";
      validateResultSet(resultSet, ans);

      resultSet = adminStmt.executeQuery("LIST PRIVILEGES USER user1 ON root.a.b.c");
      ans = "0,,root.a.b : READ_TIMESERIES,\n";
      validateResultSet(resultSet, ans);
    } finally {
      adminCon.close();
    }
  }

  @Test
  public void testListRolePrivileges() throws ClassNotFoundException, SQLException {
    Class.forName(Config.JDBC_DRIVER_NAME);
    Connection adminCon = DriverManager
        .getConnection(Config.IOTDB_URL_PREFIX + "127.0.0.1:6667/", "root", "root");
    Statement adminStmt = adminCon.createStatement();

    try {
      adminStmt.execute("CREATE ROLE role1");
      adminStmt.execute(
          "GRANT ROLE role1 PRIVILEGES 'READ_TIMESERIES','INSERT_TIMESERIES','DELETE_TIMESERIES' ON root.a.b.c");
      adminStmt.execute(
          "GRANT ROLE role1 PRIVILEGES 'READ_TIMESERIES','INSERT_TIMESERIES','DELETE_TIMESERIES' ON root.d.b.c");

      ResultSet resultSet = adminStmt.executeQuery("LIST ROLE PRIVILEGES role1");
      String ans = "0,root.a.b.c : INSERT_TIMESERIES READ_TIMESERIES DELETE_TIMESERIES,\n"
          + "1,root.d.b.c : INSERT_TIMESERIES READ_TIMESERIES DELETE_TIMESERIES,\n";
      validateResultSet(resultSet, ans);

      resultSet = adminStmt.executeQuery("LIST PRIVILEGES ROLE role1 ON root.a.b.c");
      ans = "0,root.a.b.c : INSERT_TIMESERIES READ_TIMESERIES DELETE_TIMESERIES,\n";
      validateResultSet(resultSet, ans);

      adminStmt.execute(
          "REVOKE ROLE role1 PRIVILEGES 'INSERT_TIMESERIES','DELETE_TIMESERIES' ON root.a.b.c");

      resultSet = adminStmt.executeQuery("LIST ROLE PRIVILEGES role1");
      ans = "0,root.a.b.c : READ_TIMESERIES,\n"
          + "1,root.d.b.c : INSERT_TIMESERIES READ_TIMESERIES DELETE_TIMESERIES,\n";
      validateResultSet(resultSet, ans);

      resultSet = adminStmt.executeQuery("LIST PRIVILEGES ROLE role1 ON root.a.b.c");
      ans = "0,root.a.b.c : READ_TIMESERIES,\n";
      validateResultSet(resultSet, ans);
    } finally {
      adminCon.close();
    }
  }

  @Test
  public void testListUserRoles() throws SQLException, ClassNotFoundException {
    Class.forName(Config.JDBC_DRIVER_NAME);
    Connection adminCon = DriverManager
        .getConnection(Config.IOTDB_URL_PREFIX + "127.0.0.1:6667/", "root", "root");
    Statement adminStmt = adminCon.createStatement();

    try {
      adminStmt.execute("CREATE USER chenduxiu orange");

      adminStmt.execute("CREATE ROLE xijing");
      adminStmt.execute("CREATE ROLE dalao");
      adminStmt.execute("CREATE ROLE shenshi");
      adminStmt.execute("CREATE ROLE zhazha");
      adminStmt.execute("CREATE ROLE hakase");

      adminStmt.execute("GRANT xijing TO chenduxiu");
      adminStmt.execute("GRANT dalao TO chenduxiu");
      adminStmt.execute("GRANT shenshi TO chenduxiu");
      adminStmt.execute("GRANT zhazha TO chenduxiu");
      adminStmt.execute("GRANT hakase TO chenduxiu");

      ResultSet resultSet = adminStmt.executeQuery("LIST ALL ROLE OF USER chenduxiu");
      String ans = "0,xijing,\n"
          + "1,dalao,\n"
          + "2,shenshi,\n"
          + "3,zhazha,\n"
          + "4,hakase,\n";
      validateResultSet(resultSet, ans);

      adminStmt.execute("REVOKE dalao FROM chenduxiu");
      adminStmt.execute("REVOKE hakase FROM chenduxiu");

      resultSet = adminStmt.executeQuery("LIST ALL ROLE OF USER chenduxiu");
      ans = "0,xijing,\n"
          + "1,shenshi,\n"
          + "2,zhazha,\n";
      validateResultSet(resultSet, ans);
    } finally {
      adminCon.close();
    }
  }

  @Test
  public void testListRoleUsers() throws SQLException, ClassNotFoundException {
    Class.forName(Config.JDBC_DRIVER_NAME);
    Connection adminCon = DriverManager
        .getConnection(Config.IOTDB_URL_PREFIX + "127.0.0.1:6667/", "root", "root");
    Statement adminStmt = adminCon.createStatement();

    try {
      adminStmt.execute("CREATE ROLE dalao");
      adminStmt.execute("CREATE ROLE zhazha");

      String[] members = {"HighFly", "SunComparison", "Persistence", "GoodWoods", "HealthHonor",
          "GoldLuck",
          "DoubleLight", "Eastwards", "ScentEffusion", "Smart", "East", "DailySecurity", "Moon",
          "RayBud",
          "RiverSky"};

      for (int i = 0; i < members.length - 1; i++) {
        adminStmt.execute("CREATE USER " + members[i] + " 666666");
        adminStmt.execute("GRANT dalao TO  " + members[i]);
      }
      adminStmt.execute("CREATE USER RiverSky 2333333");
      adminStmt.execute("GRANT zhazha TO RiverSky");

      ResultSet resultSet = adminStmt.executeQuery("LIST ALL USER OF ROLE dalao");
      String ans = "0,DailySecurity,\n"
          + "1,DoubleLight,\n"
          + "2,East,\n"
          + "3,Eastwards,\n"
          + "4,GoldLuck,\n"
          + "5,GoodWoods,\n"
          + "6,HealthHonor,\n"
          + "7,HighFly,\n"
          + "8,Moon,\n"
          + "9,Persistence,\n"
          + "10,RayBud,\n"
          + "11,ScentEffusion,\n"
          + "12,Smart,\n"
          + "13,SunComparison,\n";
      validateResultSet(resultSet, ans);

      resultSet = adminStmt.executeQuery("LIST ALL USER OF ROLE zhazha");
      ans = "0,RiverSky,\n";
      validateResultSet(resultSet, ans);

      adminStmt.execute("REVOKE zhazha from RiverSky");
      resultSet = adminStmt.executeQuery("LIST ALL USER OF ROLE zhazha");
      ans = "";
      validateResultSet(resultSet, ans);

    } finally {
      adminCon.close();
    }
  }

  private void validateResultSet(ResultSet set, String ans) throws SQLException {
    try {
      StringBuilder builder = new StringBuilder();
      ResultSetMetaData metaData = set.getMetaData();
      int colNum = metaData.getColumnCount();
      while (set.next()) {
        for (int i = 1; i <= colNum; i++) {
          builder.append(set.getString(i)).append(",");
        }
        builder.append("\n");
      }
      assertEquals(ans, builder.toString());
    } finally {
      set.close();
    }
  }
}
