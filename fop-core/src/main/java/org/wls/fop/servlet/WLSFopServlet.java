package org.wls.fop.servlet;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.commons.io.output.ByteArrayOutputStream;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.pdf.PDFEncryptionParams;
import org.apache.fop.servlet.ServletContextURIResolver;

public class WLSFopServlet extends HttpServlet {

    private static final long serialVersionUID = 201904111430L;

    /** Name of the parameter used to to identify */
    protected static final String MODE_REQUEST_PARAM = "mode";
    /** Name of the parameter used to request pdf encryption (true/false) */
    protected static final String ENC_REQUEST_PARAM = "enc";
    /** The TransformerFactory used to create Transformer instances */
    protected TransformerFactory transFactory;
    /** The FopFactory used to create Fop instances */
    protected FopFactory fopFactory;
    /** URIResolver for use by this servlet */
    protected transient URIResolver uriResolver;

    /**
     * {@inheritDoc}
     */
    public void init() throws ServletException {
        this.uriResolver = new ServletContextURIResolver(getServletContext());
        this.transFactory = TransformerFactory.newInstance();

        try {
            transFactory.setAttribute("http://javax.xml.XMLConstants/property/accessExternalDTD", "");
            transFactory.setAttribute("http://javax.xml.XMLConstants/property/accessExternalStylesheet", "");
        } catch (Exception ex) {
            //Ignore exception that occurs when debugging using Tomcat for Java extension
        }
        this.transFactory.setURIResolver(this.uriResolver);

        DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
        Configuration cfg = null;
        try {
            cfg = cfgBuilder.buildFromFile(new File(getServletContext().getRealPath("/") + "WEB-INF/fop-config.xml"));
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
        FopFactoryBuilder builder = new FopFactoryBuilder(new File(getServletContext().getRealPath("/")).toURI()).setConfiguration(cfg);

        fopFactory = builder.build();
    }

    /**
     * {@inheritDoc}
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {

            String mode = request.getParameter(MODE_REQUEST_PARAM);
            String encParam  = request.getParameter(ENC_REQUEST_PARAM);
    
            boolean encrypt = (encParam != null && Boolean.parseBoolean(encParam));

            //Setup source
            Source foSrc = new StreamSource(request.getInputStream());

            //Setup the identity transformation
            Transformer transformer = this.transFactory.newTransformer();
            transformer.setURIResolver(this.uriResolver);

            //Start transformation and rendering process
            render(foSrc, transformer, response, mode, encrypt);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    /**
     * Converts a String parameter to a JAXP Source object.
     * @param param a String parameter
     * @return Source the generated Source object
     */
    protected Source convertString2Source(String param) {
        Source src;
        try {
            src = uriResolver.resolve(param, null);
        } catch (TransformerException e) {
            src = null;
        }
        if (src == null) {
            src = new StreamSource(new File(param));
        }
        return src;
    }

    private void sendPDF(byte[] content, HttpServletResponse response) throws IOException {
        //Send the result back to the client
        response.setContentType("application/pdf");
        response.setContentLength(content.length);
        response.getOutputStream().write(content);
        response.getOutputStream().flush();
    }

    /**
     * Renders an XSL-FO file into a PDF file. The PDF is written to a byte
     * array that is returned as the method's result.
     * @param fo the XSL-FO file
     * @param response HTTP response object
     * @throws FOPException If an error occurs during the rendering of the
     * XSL-FO
     * @throws TransformerException If an error occurs while parsing the input
     * file
     * @throws IOException In case of an I/O problem
     */
    protected void renderFO(String fo, HttpServletResponse response, String mode, Boolean encrypt)
                throws FOPException, TransformerException, IOException {

        //Setup source
        Source foSrc = convertString2Source(fo);

        //Setup the identity transformation
        Transformer transformer = this.transFactory.newTransformer();
        transformer.setURIResolver(this.uriResolver);

        //Start transformation and rendering process
        render(foSrc, transformer, response, mode, encrypt);
    }

    /**
     * Renders an input file (XML or XSL-FO) into a PDF file. It uses the JAXP
     * transformer given to optionally transform the input document to XSL-FO.
     * The transformer may be an identity transformer in which case the input
     * must already be XSL-FO. The PDF is written to a byte array that is
     * returned as the method's result.
     * @param src Input XML or XSL-FO
     * @param transformer Transformer to use for optional transformation
     * @param response HTTP response object
     * @throws FOPException If an error occurs during the rendering of the
     * XSL-FO
     * @throws TransformerException If an error occurs during XSL
     * transformation
     * @throws IOException In case of an I/O problem
     */
    protected void render(Source src, Transformer transformer, HttpServletResponse response, String mode, boolean encrypt)
                throws FOPException, TransformerException, IOException {

        FOUserAgent foUserAgent = getFOUserAgent(mode, encrypt);

        //Setup output
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        //Setup FOP
        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);

        //Make sure the XSL transformation's result is piped through to FOP
        Result res = new SAXResult(fop.getDefaultHandler());

        //Start the transformation and rendering process
        transformer.transform(src, res);

        //Return the result
        sendPDF(out.toByteArray(), response);
    }

    protected FOUserAgent getFOUserAgent() {
        return getFOUserAgent(null, false);
    }

    /** @return a new FOUserAgent for FOP */
    @SuppressWarnings("unchecked") /*suppresses warning due to Map generic types*/
    protected FOUserAgent getFOUserAgent(String mode, boolean encrypt) {
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        String producer = "WLS PDF Generator 2.3.0";
        if (mode != null && mode.trim() != "") 
        	producer += String.format(" (%s)", mode);
        userAgent.setProducer(producer);
        if (encrypt) {

        	PDFEncryptionParams pdfEncParams = new PDFEncryptionParams();
        	pdfEncParams.setOwnerPassword(UUID.randomUUID().toString());
        	pdfEncParams.setAllowAccessContent(true);
        	pdfEncParams.setAllowPrint(true);
        	pdfEncParams.setAllowPrintHq(true);
        	pdfEncParams.setAllowAssembleDocument(false);
        	pdfEncParams.setAllowCopyContent(false);
        	pdfEncParams.setAllowEditAnnotations(false);
        	pdfEncParams.setAllowEditContent(false);
        	pdfEncParams.setAllowFillInForms(false);
            pdfEncParams.setEncryptionLengthInBits(128);
            
            userAgent.getRendererOptions().put("encryption-params", pdfEncParams);
        }
        return userAgent;
    }

}