import com.cete.dynamicpdf.*;
import com.cete.dynamicpdf.pageelements.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Vector;

public class InvoiceServlet extends HttpServlet {

    ServletOutputStream sOut;
    Connection connection;


    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
    }


    public void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        doGet(req, res);

    }

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        sOut = res.getOutputStream();
        // Create a document and set it's properties
        Document objDocument = new Document();
        objDocument.setCreator("Invoice.java");
        objDocument.setAuthor("Your Name");
        objDocument.setTitle("Invoice");
        String licKey = "GEN40JPSKHHNCBSVuhQrLJN/2/7YXQlfFfJpXwGPI9IHqIK3/PI2AFwYRMSi+tcDTZomQLCeaMxLcYq9cmfIve/GVzp6nIGtJq2g";
        String orderid = req.getParameter("orderid");
        Invoice invoice = null;
        com.cete.dynamicpdf.Document.addLicense(licKey);
        try {
            invoice = new Invoice();
            invoice.drawInvoice(objDocument, orderid);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        // Add Invoices to the document


        // Outputs the Invoices to the file.
        objDocument.draw("Invoice.pdf");
        try {
            if (!invoice.connection.isClosed())
                invoice.connection.close();
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        // Outputs the DealerList to the current web page
        res.setContentType("application/pdf");
        res.setHeader("Content-disposition", "inline; filename=\"Invoice.pdf\"");
        objDocument.drawToWeb(req, res, sOut, "Invoice.pdf");
        //ceTe.close();
        sOut.close();

    }


}
