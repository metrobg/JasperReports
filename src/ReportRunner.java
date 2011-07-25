/**
 * Created by IntelliJ IDEA.
 * User: ggraves
 * Date: 7/1/11
 * Time: 5:49 PM
 * To change this template use File | Settings | File Templates.
 */

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;


public class ReportRunner extends HttpServlet {

    String TemplateFolder;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        TemplateFolder = getInitParameter("ReportFolder");
    }

    /**
     * Destroys the servlet.
     */

    public void destroy() {
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }


    public void processRequest(HttpServletRequest req, HttpServletResponse res) throws IOException {

        try {

            JasperPrint jasperPrint = returnReportPrint(req);
            JRHtmlExporter exporter = new JRHtmlExporter();


            if (req.getParameter("output").equalsIgnoreCase("PDF")) {
                OutputStream os;
                os = res.getOutputStream();
                res.setContentType("application/pdf");
                res.setHeader("cache-control", "no-cache");
                res.setDateHeader("Expires", 0);
                JasperExportManager.exportReportToPdfStream(jasperPrint, os);
                os.flush();
                os.close();
            }

            if (req.getParameter("output").equalsIgnoreCase("XL") ||
                    req.getParameter("output").equalsIgnoreCase("HTML")  ||
                    req.getParameter("output").equalsIgnoreCase("XLS")) {
                OutputStream os;
                os = res.getOutputStream();
                JRXlsExporter exporterXLS;
                exporterXLS = new JRXlsExporter();
                exporterXLS.setParameter(JRXlsExporterParameter.JASPER_PRINT, jasperPrint);
                exporterXLS.setParameter(JRXlsExporterParameter.OUTPUT_STREAM, os);
                exporterXLS.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
                exporterXLS.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, Boolean.TRUE);
                exporterXLS.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.FALSE);
                exporterXLS.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_COLUMNS, Boolean.TRUE);
                res.setHeader("Cache-Control", "no-cache");
                res.addHeader("Content-Disposition", "filename=" + req.getParameter("template") + ".xls");
                res.setContentType("application/octet-stream");
                res.setDateHeader("Expires", 0);

                exporterXLS.exportReport();
                os.flush();
                os.close();
            }

            if (req.getParameter("output").equalsIgnoreCase("HTML")) {
                PrintWriter out;
                out = res.getWriter();
                exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                exporter.setParameter(JRExporterParameter.OUTPUT_WRITER, out);
                exporter.exportReport();
                out.close();
            }
            if (req.getParameter("output").equalsIgnoreCase("HTML")) {
                OutputStream os;
                os = res.getOutputStream();
                JasperExportManager.exportReportToPdfStream(jasperPrint, os);
                os.flush();
                os.close();
            }

        } catch (Exception e) {
            System.out.println("Error " + e.getMessage());
        }
    }


    public JasperPrint returnReportPrint(HttpServletRequest req) {
        JasperPrint jasperPrint = null;
        HashMap<String, String> map = new HashMap<String, String>();
        Connection connection = null;
        String key;
        String value;


        for (Enumeration e = req.getParameterNames(); e.hasMoreElements(); ) {
            key = (String) e.nextElement();
            value = req.getParameter(key);
            map.put(key, value);

        }

        String Template = TemplateFolder + req.getParameter("template");
        String dbKey = req.getParameter("template").substring(0, 3);

        try {
            JasperReport jasperReport = JasperCompileManager.compileReport(Template + ".jrxml");

            connection = DbConn(dbKey);

            jasperPrint = JasperFillManager.fillReport(jasperReport, map, connection);
            connection.close();
        } catch (Exception ex) {
            String connectMsg = "Could not create the report stream " + ex.getMessage() + " " + ex.getLocalizedMessage();
            System.out.println(connectMsg);
        }
        return jasperPrint;
    }

    public Connection DbConn(String key) throws Exception {
        return ConnectionFactory.getConnection(key);
    }

}
