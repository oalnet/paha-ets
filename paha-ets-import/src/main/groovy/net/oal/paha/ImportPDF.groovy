package net.oal.paha

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import groovy.swing.SwingBuilder
import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.ContentType.*
import net.oal.utils.xml.PDF2XML

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
        if (styleURL) {
            Reader reader = styleURL.newReader();

            def factory = TransformerFactory.newInstance();
            transformer = factory.newTransformer(new StreamSource(reader));

            reader.close();
        }
    }

    public void setStylesheetFrom(InputStream styleIS) {
        transformer = null;
        if (styleIS) {
            Reader reader = new InputStreamReader(styleIS);

            def factory = TransformerFactory.newInstance();
            transformer = factory.newTransformer(new StreamSource(reader));

            reader.close();
        }
    }

    public String doImport(URL sourceURL, String session, String concentration) {
        if (transformer) {
            String xmlPDF = convertPDF2XML(sourceURL);
            String xml = convertXML2XML(xmlPDF);

            Map map = convertXML2Map(xml);
            map.url = sourceURL;
            map.session = session;
            map.concentration = concentration;
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

                        } else if (subNode.name() == 'description') {
                            if (subNode.text() =~ /Date /) {

                            } else {
                                props.put(subNode.name(), subNode.text());

                            }

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
                            def desc = props.titre;
                            props.remove('titre');
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

    List sessionMap = [
            "Hiver",
            "Ete",
            "Automne"
    ];
    List concentrationMap = [
            "SEG",
            "CTN",
            "ELE",
            "LOG",
            "MEC",
            "GOL",
            "GPA",
            "GTI",
            "SUP",
            "CUR"
    ];

    def horaireSource;
    def session;
    def annee;
    def concentration;
    def horaireJSON;

    def email;
    def password;

    public void showUI() {
        def swingBuilder = new SwingBuilder()

        def customMenuBar = {
            swingBuilder.menuBar {
                menu(text: "File", mnemonic: 'F') {
                    menuItem(text: "Exit", mnemonic: 'X', actionPerformed: { dispose() })
                }
            }
        }

        def controlPanel = {
            swingBuilder.panel(constraints: BorderLayout.NORTH) {
                horaireSource = textField(columns: 45);
                session = comboBox(items: sessionMap);
                annee = textField(columns: 15);
                concentration = comboBox(items: concentrationMap);
                button(text: "Import", actionPerformed: {
                    String sessionAnnee = "${session.selectedItem} ${annee.text}";
                    horaireJSON.text = doImport(new URL(horaireSource.text), sessionAnnee, concentration.selectedItem);
                })
            }
        }

        def resultsPanel = {
            swingBuilder.scrollPane(constraints: BorderLayout.CENTER) {
                horaireJSON = textArea(columns: 60, rows: 60);
            }
        }

        def savePanel = {
            swingBuilder.panel(constraints: BorderLayout.SOUTH) {
//                email = textField(columns: 15, text: 'test@example.com');
//                password = passwordField(columns: 15);
                button(text: "Upload", actionPerformed: {
                    upload(horaireJSON.text);
                })
            }
        }

        swingBuilder.frame(title: "PDF Importer",
                defaultCloseOperation: JFrame.EXIT_ON_CLOSE,
                size: [1200, 600],
                show: true) {
            customMenuBar()
            controlPanel()
            resultsPanel()
            savePanel()
        }
    }

    protected void upload(String json) {
        HTTPBuilder http = new HTTPBuilder('http://planhoraire.aeets.com/')

//        def postBody = [email: email.text, password: password.text]
//        http.post(path: '_ah/login', query: ["continue": "/importhoraire.asp"], body: postBody, requestContentType: URLENC);

        def postBody = [horaire: json]
        http.post(path: 'import', body: postBody, requestContentType: URLENC);
    }

    public static void main(String[] args) {
        InputStream xsltIS = Thread.currentThread().getContextClassLoader().getResourceAsStream("net/oal/paha/horaire-ets-aut2009.xsl");

        ImportPDF importer = new ImportPDF();
        importer.setStylesheetFrom((InputStream) xsltIS);
        importer.showUI();
    }
}