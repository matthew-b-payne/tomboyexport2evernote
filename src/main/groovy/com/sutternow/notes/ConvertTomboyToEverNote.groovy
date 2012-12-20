package com.sutternow.notes

import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource


class ConvertTomboyToEverNote {

    String header= """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE en-export SYSTEM "http://xml.evernote.com/pub/enml2.dtd">
<en-export export-date="20121214T054005Z" application="Evernote/Windows" version="4.x">"""

    String footer = """</en-export>"""
	String outputDirectory = "C:/tmp/evernote"
	String sourceDirectory = "C:/Users/Matt/AppData/Roaming/Tomboy/notes"

	def notePattern = ~/.*\.note/
	//~".*${prefix}.*note"

	def toNNRegex = ~/.*\\W+.*/
	// from http://esaliya.blogspot.com/2009/04/java-regex-check-for-non-word.html
	static def nonWordRegex = ~/\W/

    //static def linksToAnchorRegex = ~/(?!(?!.*?<a)[^<]*<\\/a>)(?:(?:https?|ftp|file):\/\/|www\.|ftp\.)[-A-Z0-9+&#\/%=~_|\u0024?!:,.]*[A-Z0-9+&#\/%=~_|\u0024]/



	def factory
	def transformer

	public init() {
		factory = TransformerFactory.newInstance()
		//File xsltStream = new File("c:/tmp/tomboy2evernote-text.xslt")
        File xsltStream = new File("tomboy2evernote-text.xslt")
        //InputStream xsltStream = this.class.getResourceAsStream("com/sutternow/notes/tomboy2evernote.xslt")
		String content = xsltStream.text
		println content
		transformer = factory.newTransformer(new StreamSource(new StringReader(content)))

	}

	static public void main(String ...args) {
		ConvertTomboyToEverNote converter = new ConvertTomboyToEverNote()
		converter.init()
		converter.processNotesToOneFile(converter.sourceDirectory)

	}


	public void processNotes(String srcDir) {

        new File(srcDir).eachFileMatch(notePattern) { f ->
            String name = f.name
            int pos = name.lastIndexOf('.');
            String ext = name.substring(pos+1);
            String destinationName = outputDirectory + "/" + name.replace(ext,"enex")
            transformNote(f, destinationName)

            //f.delete()
        }


    }

    public void processNotesToOneFile(String srcDir) {

        //StringWriter writer = new StringWriter();
        FileWriter fileWriter = new FileWriter("c:/tmp/evernoteexport.enex")
        fileWriter.write(header)
        new File(srcDir).eachFileMatch(notePattern) { f ->
            String name = f.name
            int pos = name.lastIndexOf('.');
            String ext = name.substring(pos+1);
            fileWriter.write(transformNoteToString(f))
            //
            //f.delete()
        }
        fileWriter.write(footer)
        fileWriter.close()
    }

    public String transformNoteToString(File noteFile) {

        StringWriter writer = new StringWriter();
        transformer.transform(new StreamSource(new StringReader(noteFile.text)), new StreamResult(writer) )
        return writer.toString()
    }

    public String transformTextOnly(String text)  {
        StringWriter writer = new StringWriter();
        transformer.transform(new StreamSource(new StringReader(text)), new StreamResult(writer) )
        return writer.toString()
    }

	public void transformNote(File noteFile, String outputName) {

		File outputFile = new File(outputName)
		transformer.transform(new StreamSource(new StringReader(noteFile.text)), new StreamResult(outputFile.newDataOutputStream()) )
	}

	// C# functionality


	public static String toNMToken(String s) {
        return s.replaceAll(nonWordRegex, '-').toLowerCase()
	}

	/*
	  public class TransformExtension
	{
		public String ToNMToken(string s)
		{
			return Regex.Replace(s, @"\W", "-").ToLowerInvariant();
			Returns a copy of this String object converted to lowercase using the casing rules of the invariant culture.

		}
	}


	 */


}
