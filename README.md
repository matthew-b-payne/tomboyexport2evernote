tomboyexport2evernote
=====================

Export Tomboy notes to an Evernote .enex file for import

From the command line this can be run as:
     groovy -cp . TomboyToEvernote.groovy -src c:/tmp/work/notes -out c:/tmp/worknotes.enex
    (output file can later be imported into evernote's gui.

I didn't really want to resort to xslt, however I sourced it from https://github.com/matthew-b-payne/EvernoteSyncAddin/blob/master/TomboyToEvernote.xslt.
It was a decent resource for "mostly" preserving the tomboy note formatting.

