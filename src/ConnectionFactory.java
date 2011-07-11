/**
 * Created by IntelliJ IDEA.
 * User: ggraves
 * Date: 7/1/11
 * Time: 6:04 PM
 * To change this template use File | Settings | File Templates.
 */

import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLNode;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;

public class ConnectionFactory {
  private static XMLDocument root;
  public static Connection getConnection(String name) throws Exception {
    if (root == null) {
      // Read Connections.xml from the runtime CLASSPATH
      Class c = ConnectionFactory.class;
      InputStream file = c.getResourceAsStream("Connections.xml");
      if (file == null) {
        throw new FileNotFoundException("Connections.xml not in CLASSPATH");
      }
      // Parse Connections.xml and cache the XMLDocument of config info
      root = XMLHelper.parse(file,null);
    }
    // Prepare an XPath expression to find the connectioin named 'name'
    String pattern = "/connections/connection[@name='"+name+"']";
    // Find the first connection matching the expression above
    XMLNode connNode     = (XMLNode) root.selectSingleNode(pattern);
    if (connNode != null) {
      String username     = connNode.valueOf("username");
      String password     = connNode.valueOf("password");
      String dburl        = connNode.valueOf("dburl");
      String driverClass  = "oracle.jdbc.driver.OracleDriver";
      Driver d = (Driver)Class.forName(driverClass).newInstance();
      System.out.println("Connecting as " + username + " at " + dburl);
      return  DriverManager.getConnection(dburl,username,password);
    }
    else return null;
  }
}

