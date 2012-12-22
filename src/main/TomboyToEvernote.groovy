package com.sutternow.notes

import org.joda.time.*
import groovy.xml.MarkupBuilder

import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

@Grapes([
@Grab(group = 'joda-time', module = 'joda-time', version = '1.6'),
])
/**
 * Created with IntelliJ IDEA.
 * User: Matt
 * Date: 12/14/12
 * Time: 6:55 PM
 * To change this template use File | Settings | File Templates.
 */
class TomboyToEvernote {

    String header= """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE en-export SYSTEM "http://xml.evernote.com/pub/evernote-export.dtd">
<en-export export-date="20121219T054005Z" application="Evernote/Tomboy" version="4.x">"""

    String footer = '</en-export>'
    String outputDirectory = "C:/tmp/evernote"
    String sourceDirectory = "C:/Users/Matt/AppData/Roaming/Tomboy/notes"


    def notePattern = ~/.*\.note/
    //def linksToAnchorRegex = ~/http(s)?:\/\/([\\w+?\\.\\w+])+([a-zA-Z0-9\\~\\!\\@\\#\\\u0024\\%\\^\\&amp;\\*\\(\\)_\\-\\=\\+\\\\\\\/\\?\\.\\:\\;\\'\\,]*)?/


     def linksToAnchorRegex =  ~/(?:^|[^"'])(\b(?:https?|ftp):\/\/[a-z0-9-+&@#\/%?=~_|!:,.;]*[a-z0-9-+&@#\/%=~_|])/

    //this may be needed to clean up the actual content
   // ConvertTomboyToEverNote xsltConverter

    // needed for current xslt conversion.. ugh
    def factory
    def transformer

    List<Note> notes

    public Note fetchNote(File noteFile) {
        def noteXml = new XmlSlurper().parse(noteFile)
        println "parsing $noteFile.name"
        Note n = populateNote(noteXml)
        return n
    }

    public void init() {

        factory = TransformerFactory.newInstance()
        File xsltStream = new File("tomboy2evernote-text.xslt")
        //InputStream xsltStream = this.class.getResourceAsStream("com/sutternow/notes/tomboy2evernote.xslt")
        String content = xsltStream.text
        println content
        transformer = factory.newTransformer(new StreamSource(new StringReader(content)))

        String.metaClass.encodeURL = {
            java.net.URLEncoder.encode(delegate)
        }
    }


    static public void main(String ...args) {
        def cl = new CliBuilder(usage: 'groovy TomboyToEvernote.groovy -src "C:/Users/Matt/AppData/Roaming/Tomboy/notes" -out "c:/tmp/evernoteexport.enex')
        cl.src(argName:'src', longOpt:'notefolder', args:1, required:true, 'Tomboy note directory path, REQUIRED')
        cl.out(argName:'out', longOpt:'outfile', args:1, required:true, 'Export file name, REQUIRED')
        //cl.h(argName:'help', longOpt:'help', args:1, required:false, 'Dislpay help name to submit for')

        def opt = cl.parse(args)
        if (!opt) {
            // because the parse failed, the usage will be shown automatically
            println "\nInvalid command line, exiting..."
            cl.usage()
        }  else if (opt.src && opt.out) {
            String sourceDirectory = opt.src
            String outpath = opt.out
            TomboyToEvernote converter = new TomboyToEvernote()
            converter.init()
            converter.processNotes(sourceDirectory, outpath)
            // String sourceDirectory=args[0] ?: converter.sourceDirectory
        }

    }

    public String transformNoteToString(File noteFile) {

        StringWriter writer = new StringWriter();
        transformer.transform(new StreamSource(new StringReader(noteFile.text)), new StreamResult(writer) )
        return writer.toString()
    }

    public void processNotes(String srcDir, String outputPath) {

        FileWriter fileWriter = new FileWriter(outputPath)
        fileWriter.write(header)

        new File(srcDir).eachFileMatch(notePattern) { f ->
            String name = f.name
            int pos = name.lastIndexOf('.');
            String ext = name.substring(pos+1);
            String destinationName = outputDirectory + "/" + name.replace(ext,"enex")
            Note n = fetchNote(f)
            String convertedContent = transformNoteToString(f)
            // make hyperlinks
             convertedContent = convertedContent.replaceAll(linksToAnchorRegex) {
                 println it[0]
                 String link = it[0].replaceAll('>', '')
                 println link
                "<a href=\"${link.trim()}\">${link.trim()}</a>"
            }
            convertedContent = convertedContent.replace("<br/<", "<br/><")
            n.htmlText = convertedContent
            //'<text xml:space="preserve"><note-content version="0.1">' + it.text+ '</note-content></text>'
            fileWriter.write(renderEverNote(n))

            //f.delete()
        }
        fileWriter.write(footer)
        fileWriter.close()

    }


    public String renderEverNote(Note tomNote) {
       String exampleNote=   """<en-export export-date="20121214T054005Z" application="Evernote/Windows" version="4.x">
<note><title>Title of Ashers Note</title><content><![CDATA[<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE en-note SYSTEM "http://xml.evernote.com/pub/enml2.dtd">

<en-note style="word-wrap: break-word; -webkit-nbsp-mode: space; -webkit-line-break: after-white-space;">
Here is some <b>content</b> here.
<div><br/></div><div><ul><li>bullet list</li><li>item 2</li><li>item 3</li></ul><div><br/></div></div><div><br/></div></en-note>]]></content><created>20121214T053841Z</created><updated>20121214T232123Z</updated><tag>dog</tag></note>"
    """


        String preContentMarkup = """<content><![CDATA[<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE en-note SYSTEM "http://xml.evernote.com/pub/enml2.dtd">"""
        String endContentMarkup = ']]></content>'
        StringWriter writer = new StringWriter()
        def xml = new MarkupBuilder(writer)
       // xml.'en-export'('export-date': '20121214T054005Z', 'application':"Evernote/Tomboy", 'version':'4.x') {
            xml.note() {
                title(tomNote.title)
               // content(xml.mkp.yieldUnescaped(preContentMarkup) + tomNote.text + xml.mkp.yieldUnescaped(endContentMarkup))
                xml.mkp.yieldUnescaped(preContentMarkup + "<en-note>${tomNote.htmlText}</en-note>" + endContentMarkup)
                created(tomNote.createDate.format("yyyyMMdd") + "T" + tomNote.createDate.format("hhmmss"))      //yyyyMMddhhmmss
                if (tomNote.lastChangeDate) {updated(tomNote.lastChangeDate.format("yyyyMMdd") + "T" + tomNote.lastChangeDate.format("hhmmss"))}      //yyyyMMddhhmmss
                if (tomNote.tag) tag(tomNote.tag) // evernote baulks at empty tags

            }

        return writer.toString()
    }

    public Note populateNote(def it) {

        def tags = it.tags.children()[0].toString() // just get first one
        if (tags ) {
            if (tags.contains("system:notebook")){
                tags = tags[("system:notebook:".length()).tags.length()-1]
            } else {

            }
            println "tags are $tags"
            //tags look like 'system:notebook:work-security'
            //sleep(600)
        }
        //2012-08-12T00:43:38.5308597-04:00
        //2012-08-07T23:00:47.7496679-04:00
        //"yyyy-MM-dd'T'HH:mm:ss.fffffffZ
        Date createDate = null
        if (it."create-date".toString()) {
            //	println " attempting to parse create date"
            createDate = Date.parse("yyyy-MM-dd'T'HH:mm:ss",it."create-date".toString()[0..18])
        } else {
            //println " no create date, just defaulting to something a while ago"
            createDate = new Date().parse('yyyy/MM/dd', '2007/07/09')
        }
        Date lastChangeDate = null
        if (it."last-change-date".toString()) {
            //	println " attempting to parse create date"
            lastChangeDate = Date.parse("yyyy-MM-dd'T'HH:mm:ss",it."last-change-date".toString()[0..18])
        } else {
            //println " no create date"
            lastChangeDate = new Date().parse('yyyy/MM/dd', '2007/07/09')
        }


        new Note(text:it.text, title:it.title, createDate:createDate, lastChangeDate: lastChangeDate, tag:tags)
    }
}

class Note {
    String text, title,notebook, tag, htmlText
    Date createDate, lastChangeDate
}

