package net.oal.utils.xml;

import org.apache.pdfbox.exceptions.CryptographyException
import org.apache.pdfbox.exceptions.InvalidPasswordException
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.util.Matrix
import org.apache.pdfbox.util.PDFTextStripper
import org.apache.pdfbox.util.TextPosition
import org.dom4j.Document
import org.dom4j.DocumentHelper
import org.dom4j.Element
import org.dom4j.io.OutputFormat
import org.dom4j.io.XMLWriter

import java.text.DateFormat
import java.util.logging.Level
import java.util.logging.Logger

public class PDF2XML {
    private static final Logger LOGGER = Logger.getLogger(PDF2XML.class.getName());
    PDFTextStripper stripper_ = new PDFBox();
    DateFormat dFormat_ = DateFormat.getInstance();

    protected class CurrentLine {
        private List<TextPosition> elts_ = new ArrayList<TextPosition>();

        public List<TextPosition> getElements() {
            return elts_;
        }
//		public void setElements(List<TextPosition> elts) { this.elts_ = elts;	}

        private StringBuffer text_ = new StringBuffer();

        public String getText() {
            return text_.toString().replaceAll("&", "&amp;");
        }

        public void setText(String text) {
            text_ = new StringBuffer(text.replaceAll("&amp;", "&"));
        }

        public void add(String text) {
            text_.append(text);
        }

        public void add(TextPosition tp) {
            elts_.add(tp);
            text_.append(tp.getCharacter());
        }

        public float getHeight() {
            return elts_.get(0).getHeight();
        }

        public Matrix getTextPos() {
            return elts_.get(0).getTextPos();
        }

        public float getWidth() {
            float w = elts_.get(elts_.size() - 1).getX() - elts_.get(0).getX() + elts_.get(elts_.size() - 1).getWidth();
            return w;
        }

        public float getX() {
            return elts_.get(0).getX();
        }

        public float getY() {
            return elts_.get(0).getY();
        }

        private Element lineElement_ = null;

        public void writeNewLine(Element currentElement, double xpos, double ypos) {
            lineElement_ = currentElement.addElement("line");
            lineElement_.addAttribute("xpos", String.valueOf(xpos));
            lineElement_.addAttribute("ypos", String.valueOf(ypos));
        }

        public void writeTab() {
            lineElement_.addElement("tab");
        }

        public void writeText(boolean clearAfter) {
            Element textElement = lineElement_.addElement("text");
            textElement.addAttribute("xpos", String.valueOf(getX()));
            textElement.addAttribute("ypos", String.valueOf(getY()));
            textElement.setText(getText());

            if (clearAfter) {
                clear();
            }
        }

        public void clear() {
            elts_.clear();
            text_ = new StringBuffer();
        }
    }

    protected class PDFBox extends PDFTextStripper {
        float lastX_ = 0, lastY_ = 0;
        CurrentLine currentText_ = new CurrentLine();
        private Document xmldoc_;

        public PDFBox() throws IOException {
            super();
            super.setSuppressDuplicateOverlappingText(true);
//	        super.setSortByPosition( true );
        }

        /**
         * A method provided as an event interface to allow a subclass to perform
         * some specific functionality when a character needs to be displayed.
         *
         * @param text The character to be displayed.
         */
        @Override
        protected void processTextPosition(TextPosition text) {
            boolean addLine = true;
            if (text != null) {
                if ((lastY_ > 0) && (Math.abs(text.getY() - lastY_) >= text.getFontSize())) {
                    if (currentText_.getElements().size() > 0) {
                        currentText_.writeText(true);
                    }
                } else if ((lastX_ > 0) && ((Math.abs(text.getX() - lastX_) >= 1.5 * text.getWidthOfSpace()))) {
                    addLine = false;
                    if (currentText_.getElements().size() > 0) {
                        currentText_.writeText(true);
                    }
                    currentText_.writeTab();
                } else if ((lastX_ > 0) && ((Math.abs(text.getX() - lastX_) >= text.getWidthOfSpace()))) {
                    addLine = false;
                    if (currentText_.getElements().size() > 0) {
                        currentText_.add(" ");
                    }
                }
                if (currentText_.getElements().size() == 0) {
                    if (addLine) {
                        currentText_.writeNewLine(currentElement_, text.getX(), text.getY());
                    }
                }
                currentText_.add(text);

                lastX_ = text.getX() + text.getWidth();
                lastY_ = text.getY();
            }
        }

        @Override
        protected void startPage(PDPage page) throws IOException {
            Element pageElement = currentElement_.addElement("page");
            pageElement.addAttribute("no", String.valueOf(getCurrentPageNo()));
            currentElement_ = pageElement;
            super.startPage(page);
        }

        @Override
        protected void endPage(PDPage page) throws IOException {
            super.endPage(page);
            if (currentText_.getElements().size() > 0) {
                currentText_.writeText(true);
            }
            currentElement_ = currentElement_.getParent();
        }

        private Element currentElement_ = null;

        @Override
        public void writeText(PDDocument pdf, Writer out) throws IOException {
            xmldoc_ = DocumentHelper.createDocument(DocumentHelper.createElement("document"));
            currentElement_ = xmldoc_.getRootElement();

            if (pdf.getDocumentInformation() != null) {
                Element elt = null;
                if (pdf.getDocumentInformation().getTitle() != null) {
                    elt = currentElement_.addElement("title");
                    elt.setText(pdf.getDocumentInformation().getTitle());
                }
                elt = currentElement_.addElement("creation");
                elt.setText(dFormat_.format(pdf.getDocumentInformation().getCreationDate().getTime()));

                elt = currentElement_.addElement("modification");
                elt.setText(dFormat_.format(pdf.getDocumentInformation().getModificationDate().getTime()));
            }

            super.writeText(pdf, new StringWriter());

            String encoding = System.getProperty("file.encoding");
            if ("Cp1252".equalsIgnoreCase(encoding)) {
                encoding = "ISO-8859-1";
            }
            OutputFormat format = new OutputFormat("  ", true, encoding);
            format.setNewLineAfterDeclaration(false);
            try {
                XMLWriter writer = new XMLWriter(out, format);
                writer.write(xmldoc_);
                writer.flush();
            } catch (UnsupportedEncodingException e) {
                LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
            }
        }
    }

    ;

    private PDDocument document_ = null;

    public PDF2XML() throws IOException {
    }

    public void load(URL sourceURL) throws IOException {
        LOGGER.log(Level.INFO, "Document file: " + sourceURL + "...");
        document_ = PDDocument.load(sourceURL);
        if (document_.isEncrypted()) {
            try {
                document_.decrypt("");
            } catch (InvalidPasswordException e) {
                LOGGER.log(Level.SEVERE, "Error: Document is encrypted with a password.");
                LOGGER.throwing(PDF2XML.class.getName(), "parse", e);
            } catch (CryptographyException e) {
                e.printStackTrace();
            }
        }
    }

    public void process(Writer out) {
        try {
            LOGGER.log(Level.INFO, "Processing file...");
            stripper_.writeText(document_, out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (document_ != null) {
                try {
                    document_.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        LOGGER.log(Level.INFO, "Processing file done.");
    }
}
