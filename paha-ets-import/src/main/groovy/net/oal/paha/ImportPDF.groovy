package net.oal.paha

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import groovy.swing.SwingBuilder
import net.oal.utils.xml.PDF2XML

import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource
import java.awt.BorderLayout

public class ImportPDF {
    protected Transformer transformer = null;

    public void setStylesheetFrom(URL styleURL) {
        transformer = null;
        if(styleURL) {
            Reader reader = styleURL.newReader();

            def factory = TransformerFactory.newInstance();
            transformer = factory.newTransformer(new StreamSource(reader));

            reader.close();
        }
    }

    public void setStylesheetFrom(InputStream styleIS) {
        transformer = null;
        if(styleIS) {
            Reader reader = new InputStreamReader(styleIS);

            def factory = TransformerFactory.newInstance();
            transformer = factory.newTransformer(new StreamSource(reader));

            reader.close();
        }
    }

    public String doImport(URL sourceURL, String horaireId) {
        if(transformer) {
            String xmlPDF = convertPDF2XML(sourceURL);
            String xml = convertXML2XML(xmlPDF);

            Map map = convertXML2Map(xml);
            map.url = sourceURL;
            map.id = horaireId;
            map.date = new Date();

            String json = convertMap2JSON(map);
            return json;

        } else {
            IllegalStateException ise = new IllegalStateException("XSLT transformer could not be loaded !!!");
            throw ise;

        }
    }

    protected String convertPDF2XML(URL pdfURL) {
        StringWriter writer = new StringWriter();
        PDF2XML importer = new PDF2XML();
        importer.load(pdfURL);
        importer.process(writer);
        writer.flush();
        return writer.toString();
    }

    protected String convertXML2XML(String xmlPDF) {
        StringWriter writer = new StringWriter();
        transformer.transform(new StreamSource(new StringReader(xmlPDF)), new StreamResult(writer));
        writer.flush();
        return writer.toString();
    }

    protected Map convertXML2Map(String xml) {
// Parse it
        def parsed = new XmlParser().parseText(xml);

// Deal with each node:
        def handle
        handle = { def node ->
            if (node instanceof String) {
                node
            } else {
                def props = node.attributes();
                if (node.text()) {
                    if (props) {
                        props.put("value", node.text());
                    } else {
                        props = node.text();
                    }
                } else {
                    node.children().each { Node subNode ->
                        if (subNode.name() == 'entry') {
                            props.put(subNode.children()[0].text(), handle(subNode.children()[1]));

                        } else if (subNode.name() == 'string') {
                            if (props instanceof List) {
                                props << subNode.text();

                            } else {
                                props = [subNode.text()];

                            }

                        } else {
                            def value = props.get(subNode.name())
                            if (value != null) {
                                if (value instanceof List) {
                                    value << handle(subNode)

                                } else {
                                    value = [value, handle(subNode)];

                                }

                            } else {
                                value = handle(subNode);

                            }
                            props.put(subNode.name(), value);

                        }
                    };
                    switch (node.name()) {
                        case 'horaire':
                            def desc = props.title;
                            props.remove('title');
                            if (desc) {
                                props.description = desc;
                            }
                            break;
                        case 'cours':
                            def liste = props.groupe;
                            props.remove('groupe');
                            if (liste) {
                                props.listeGroupe = (liste instanceof List) ? liste : [liste];
                            }
                            break;
                        case 'groupe':
                            props.remove('cours');
                            def liste = props.periode;
                            props.remove('periode');
                            if (liste) {
                                props.listePeriodes = (liste instanceof List) ? liste : [liste];
                            }
                            break;
                        case 'periode':
                            props.remove('groupe');
                            if (props.jour instanceof Map) {
                                props.remove('jour');
                            }
                            def liste = props.locaux;
                            props.remove('locaux');
                            if (liste) {
                                props.locaux = (liste instanceof List) ? liste : [liste];
                            }
                            if (props.activite instanceof Map) {
                                props.remove('activite');
                            }
                            break;
                    }
//                    if(props.size() == 1) {
//                        props = props.values()[0];
//                    }
                }
                props
            }
        }

// Convert it to a Map containing a List of Maps
        def jsonObject = handle(parsed);
        return jsonObject;
    }

    protected String convertMap2JSON(Map map) {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();

// And dump it as Json
        String json = gson.toJson(map);
        return json;
    }

    def horaireSource;
    def horaireId;
    def horaireJSON;
    public void showUI() {
        def swingBuilder = new SwingBuilder()

        def customMenuBar = {
            swingBuilder.menuBar{
                menu(text: "File", mnemonic: 'F') {
                    menuItem(text: "Exit", mnemonic: 'X', actionPerformed: {dispose() })
                }
            }
        }

        def controlPanel = {
            swingBuilder.panel(constraints: BorderLayout.NORTH) {
                horaireSource = textField(columns:45)
                horaireId = textField(columns:15)
                button(text:"Import", actionPerformed:{
                    horaireJSON.text = doImport(new URL(horaireSource.text), horaireId.text);
                })
            }
        }

        def resultsPanel = {
            swingBuilder.scrollPane(constraints: BorderLayout.CENTER){
                horaireJSON = textArea(columns: 60, rows: 60);
            }
        }

        def savePanel = {
            swingBuilder.panel(constraints: BorderLayout.SOUTH) {
                button(text:"Save", actionPerformed:{
                    def fileChooser = swingBuilder.fileChooser(dialogTitle:"Choose file to save to",
                            id:"saveToFile",
                            currentDirectory: new File("."),
                            fileSelectionMode : JFileChooser.FILES_ONLY);
                    int retval = fileChooser.showSaveDialog(swingBuilder.getParentNode());
                    if(retval == JFileChooser.APPROVE_OPTION) {
                        File file = fileChooser.getSelectedFile();
                        file.text = horaireJSON.text;
                    }
                })
            }
        }

        swingBuilder.frame(title:"PDF Importer",
                defaultCloseOperation:JFrame.EXIT_ON_CLOSE,
                size:[800,600],
                show:true) {
            customMenuBar()
            controlPanel()
            resultsPanel()
            savePanel()
        }
    }

    protected void saveToFile() {
    }

    public static void main(String[] args) {
        InputStream xsltIS = Thread.currentThread().getContextClassLoader().getResourceAsStream("net/oal/paha/horaire-ets-aut2009.xsl");

        ImportPDF importer = new ImportPDF();
        importer.setStylesheetFrom((InputStream) xsltIS);
        importer.showUI();
    }
}