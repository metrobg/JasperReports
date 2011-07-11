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
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;


public class DealerList extends HttpServlet {

    // Top, bottom, left and right margins of report

    private float margin;

    // Height of the header
    private float headerHeight;

    // Height of the footer
    private float footerHeight;

    // Size of paper to use
    private PageDimensions pageSize;

    // Bottom Y coordinate for the body of the report
    private float bodyBottom;

    // Current page that elements are being added to
    private Page currentPage;

    // Template for header and footer elements
    private Template template;

    // Current Y coordinate where elements are being added
    private float currentY;

    // Used to control the alternating background
    private boolean alternateBG;

    // Used to test for grouping

    private String tmpChar = "";
    ServletOutputStream sOut;
    Connection connection;


    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        margin = 36;
        headerHeight = 74;
        footerHeight = 14;
        pageSize = new PageDimensions(PageSize.LETTER, PageOrientation.LANDSCAPE, margin);
        bodyBottom = pageSize.getHeight() - (margin * 2) - footerHeight;


    }

    public void doPost (HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        doGet(req,res);

    }


    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        connection = DbConn();
        sOut = res.getOutputStream();
        currentY = 0;
        alternateBG = false;
        template = new Template();

        // Create a document and set it's properties

        Document objDocument = new Document();
        objDocument.setCreator("DealerList.java");
        objDocument.setAuthor("MBG - Gilbert Graves");
        objDocument.setTitle("Heritage Mfg. - Dealer Report");
        objDocument.setTemplate(template);

        // Adds elements to the template

        setHeaderTemplate();
        setFooterTemplate();
        ResultSet data = getDealerListData();
        // Builds the report
        buildDocument(objDocument, data);

        // Outputs the DealerList to the current web page

        objDocument.drawToWeb(req, res, sOut, "DealerList.pdf");
        //ceTe.close();
        sOut.close();

    }


    private void setHeaderTemplate() {
        // Adds header elements to the template

        try {
            template.getElements().add(new Image(getServletContext().getRealPath("images/logo_sm.gif"), 0, 0, 0.65f));

        } catch (FileNotFoundException ex) {
            ex.printStackTrace(System.err);
        }

        template.getElements().add(new Label("Heritagemfg.com", 0, 0, 720, 18,
                Font.getHelveticaBold(), 18, TextAlign.CENTER));

        template.getElements().add(new Label("Dealer Report", 0, 21, 720, 12,
                Font.getHelvetica(), 12, TextAlign.CENTER));

        GregorianCalendar gc = new GregorianCalendar();
        Date date = gc.getTime();

        SimpleDateFormat sd1 = new SimpleDateFormat("dd MMM yyyy, H:mm:ss E");

        template.getElements().add(new Label(sd1.format(date), 0, 36, 720, 12,
                Font.getHelvetica(), 12, TextAlign.CENTER));

        template.getElements().add(new Rectangle(0, 56, 720, 16,

                new WebColor("0000A0"), new WebColor("0000A0")));

        template.getElements().add(new Label("Dealer ID", 2, 57, 58, 12, Font.getHelveticaBold(), 12, TextAlign.LEFT,
                Grayscale.getWhite()));

        template.getElements().add(new Label("Name", 62, 57, 156, 12, Font.getHelveticaBold(), 12, TextAlign.LEFT,
                Grayscale.getWhite()));

        template.getElements().add(new Label("Address", 222, 57, 156, 12, Font.getHelveticaBold(), 12, TextAlign.LEFT,
                Grayscale.getWhite()));

        template.getElements().add(new Label("City", 362, 57, 156, 12, Font.getHelveticaBold(), 12,
                TextAlign.LEFT, Grayscale.getWhite()));

        template.getElements().add(new Label("State", 550, 57, 86, 12,
                Font.getHelveticaBold(), 12, TextAlign.LEFT, Grayscale.getWhite()));

        template.getElements().add(new Label("Phone", 650, 57, 86, 12,
                Font.getHelveticaBold(), 12, TextAlign.LEFT, Grayscale.getWhite()));

    }


    private void setFooterTemplate() {
        // Adds footer elements to the template

        PageNumberingLabel pageNumLabel = new PageNumberingLabel("Page " +
                "%%CP(l)%% of %%TP(l)%%", 0, bodyBottom + 5, 720, 10,
                Font.getHelvetica(), 10, TextAlign.CENTER);
        template.getElements().add(pageNumLabel);

    }


    private void buildDocument(Document document, ResultSet data) {
        // Builds the PDF document with data from the ResultSet
        addNewPage(document);
        try {
            while (data.next()) {
                // Add current record to the document
                addRecord(document, data);
            }
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace(System.err);

        }

    }


    private void addRecord(Document document, ResultSet data) {
	// Creates TextAreas that are expandable
        try {


        String tmpOrderID = data.getString("id");
	    String tmpStr = data.getString("dealer");
        TextArea FamilyName = new TextArea(data.getString("dealer"), 62, currentY + 3 , 156, 11, Font.getTimesRoman(), 11);
	    TextArea shipVIA = new TextArea(data.getString("address"), 222,currentY + 3, 136, 11,Font.getTimesRoman(), 11);
	    TextArea OrderDate = new TextArea(data.getString("city"), 372,currentY + 3, 156, 11,Font.getTimesRoman(), 11);
        float requiredHeight = setExpandableRecords(document, FamilyName,shipVIA,OrderDate,tmpStr);

	    // Creates non expandable Labels
	    Label OrderID = new Label(tmpOrderID, 2, currentY + 3,58, 11,Font.getTimesRoman(), 11);



	   // Label Status = new Label(data.getString("ostatus"), 470, currentY + 3, 96, 11, Font.getTimesRoman(), 11);
            String str = data.getString("state");
	    Label Quantity = new Label(str, 550, currentY + 3, 96,11, Font.getTimesRoman(), 11);
            Label Site = new Label(data.getString("phone"), 650, currentY + 3, 96,11, Font.getTimesRoman(), 11);

        // Adds alternating background if required
	    if (alternateBG) {
	        currentPage.getElements().add(new Rectangle(0, currentY, 720,
                                     requiredHeight + 6, new WebColor("E0E0FF"),
                                     Apply.FILL));
	    }
	    // Toggles alternating background
	    alternateBG = !alternateBG;

	    // Adds elements to the current page
	    currentPage.getElements().add(OrderID);
	    currentPage.getElements().add(FamilyName);
	    currentPage.getElements().add(shipVIA);
	    currentPage.getElements().add(OrderDate);
	    currentPage.getElements().add(Quantity);
        currentPage.getElements().add(Site);

        shipVIA = shipVIA.getOverflowTextArea();
                    if (shipVIA != null) {
                        currentPage.getElements().add(shipVIA);
                }

        // increments the current Y position on the page
	    currentY += requiredHeight + 6;
        } catch (SQLException ex) {
            ex.printStackTrace(System.err);
        }
    }

    private float setExpandableRecords(Document document,
                                     TextArea FamilyName, TextArea shipVIA,
                                     TextArea OrderDate, String tmpStr) {
	// Gets the maximum height requred of the three TextAreas
	float requiredHeight = getMaxRecordHeight(FamilyName, shipVIA,OrderDate);

	// Add space for the section header if required
	float sectionHeaderHeight = 0;
        if (!tmpChar.equals(tmpStr.substring(0,1))) {
            sectionHeaderHeight = 26;
        }


	// Add a new page if needed
	if (bodyBottom < currentY + requiredHeight + sectionHeaderHeight + 4) {
	    addNewPage(document);
	    if (sectionHeaderHeight == 0) {
		// Update Y coordinate of TextArea when placed on the new page
		FamilyName.setY(currentY + 1);
		shipVIA.setY(currentY + 1);
		OrderDate.setY(currentY + 1);
	    }
	}

	// Add section header if required
	if (sectionHeaderHeight > 0) {
	    // addSectionHeader(tmpStr);
	    FamilyName.setY(currentY + 3);
	    shipVIA.setY(currentY + 3);
	    OrderDate.setY(currentY + 3);
	}
	return requiredHeight;
    }

    private void addSectionHeader(String tmpStr) {
        tmpChar = tmpStr.substring(0, 1);
        currentPage.getElements().add(new Label("- " + tmpChar + " -", 0,
                currentY + 6, 720, 18, Font.getHelveticaBold(),
                18, TextAlign.CENTER));
        currentY += 26;
        alternateBG = false;

    }


    private float getMaxRecordHeight(TextArea companyName, TextArea contactName,

                                     TextArea contactTitle) {

        // Returns the maximum required height of the three TextAreas

        float requiredHeight = 11;
        float requiredHeightB = 0;

        requiredHeight = companyName.getRequiredHeight();
        requiredHeightB = contactName.getRequiredHeight();
        if (requiredHeightB > requiredHeight) {
            requiredHeight = requiredHeightB;
        }

        requiredHeightB = contactTitle.getRequiredHeight();
        if (requiredHeightB > requiredHeight) {
            requiredHeight = requiredHeightB;

        }


        if (requiredHeight > 11) {

            companyName.setHeight(requiredHeight);
            contactName.setHeight(requiredHeight);
            contactTitle.setHeight(requiredHeight);
        }

        return requiredHeight;

    }


    private void addNewPage(Document document) {

        // Adds a new page to the document

        currentPage = new Page(pageSize);
        currentY = headerHeight;
        alternateBG = false;

        document.getPages().add(currentPage);
    }

    private ResultSet getDealerListData() {
        ResultSet data = null;
        // Creates a ResultSet for the report


        try {

       String sql = "select d.ID,d.NAME DEALER, ADDRESS, CITY, STATE || ' - ' ||   s.NAME State, " +
                    "PHONE  from ht_dealers d,states s where " +
                    "d.state = s.code  order by state,city,dealer ";

            PreparedStatement ps = connection.prepareStatement(sql);
            data = ps.executeQuery();

        } catch (SQLException ex) {

            ex.printStackTrace(System.err);

        }

        return data;

    }

    public Connection DbConn() {
        try {

            Class.forName("oracle.jdbc.driver.OracleDriver");
            // Establises connection to the database
            connection = DriverManager.getConnection("jdbc:oracle:thin:@freedom.domanet.com:1521:XE",
                    "heritage", "mustard");

        } catch (ClassNotFoundException ex1) {
            ex1.printStackTrace(System.err);
        } catch (SQLException ex2) {
            ex2.printStackTrace(System.err);
        }
        return connection;
    }

}

