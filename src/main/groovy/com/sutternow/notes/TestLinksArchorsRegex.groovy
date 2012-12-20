package com.sutternow.notes

/**
 * Created with IntelliJ IDEA.
 * User: Matt
 * Date: 12/18/12
 * Time: 10:52 PM
 * To change this template use File | Settings | File Templates.
 */
class TestLinksArchorsRegex {

    static public  void main(String ...args) {
         def linksToAnchorRegex = ~/(?!(?!.*?<a)[^<]*<\\/a>)(?:(?:https?|ftp|file):\/\/|www\.|ftp\.)[-A-Z0-9+&#\/%=~_|\u0024?!:,.]*[A-Z0-9+&#\/%=~_|\u0024]/
       def regex2 = ~/(https?:\/\/[^ ]+)/
       def regex3 = ~/http(s)?:\/\/([\\w+?\\.\\w+])+([a-zA-Z0-9\\~\\!\\@\\#\\\u0024\\%\\^\\&amp;\\*\\(\\)_\\-\\=\\+\\\\\\\/\\?\\.\\:\\;\\'\\,]*)?/
       def regex4 = ~/\/\b(?<!=")(https?|ftp|file):\\/\\/[-A-Z0-9+&@#\\/%?=~_|!:,.;]*[A-Z0-9+&@#\\/%=~_|](?!.*".*>)(?!.*<\\/a>)\/i/
       def regex5 = ~/(?!(?!.*?<a)[^<]*<\\/a>)(?:(?:https?|ftp|file):\/\/|www\.|ftp\.)[-A-Z0-9+&#\/%=~_|\u0024?!:,.]*[A-Z0-9+&#\/%=~_|\u0024]/

        String content = """ Here is my content
            http://www.cnn.com
            www.sutternow.com
            Some other text
             https://www.httpslink.org?here=is&a=querystring
             and >http://non.wwwlinks.edu
            <a href="www.dontmesswithme.com">Don't mess with this</a>
            <a href="http://www.dontmesswithmeeither.com">Don't mess with this</a>

           http://www.eurosportdesign.com/p-3112-bmw-carbon-fiber-roundel-emblem-overlays.aspx
            Okay.

        """

       // String result = content.replace(linksToAnchorRegex, '<a href=\"$1\">$1</a>')
         println "match results " + content.findAll(linksToAnchorRegex).toString()

        println "match results " + content.findAll(regex3).toString()

        def testRegex = ~/(((f|ht){1}tp:\/\/)[-a-zA-Z0-9@:%_\+.~#?&\/\/=]+)/

        // this works but includes links already hyperlinked
        def testRegex2 = ~/(?:^|[^'])(?i)\b((?:[a-z][\w-]+:(?:\/{1,3}|[a-z0-9%])|www\d{0,3}[.]|[a-z0-9.\-]+[.][a-z]{2,4}\/)(?:[^\s()<>]+|\(([^\s()<>]+|(\([^\s()<>]+\)))*\))+(?:\(([^\s()<>]+|(\([^\s()<>]+\)))*\)|[^\s`!()\[\]{};:'".,<>?«»“”‘’]))/

        // this does not include stuff that doesn't have http: current favorite
        def testRegex3 = ~/(?:^|[^"'])(\b(?:https?|ftp):\/\/[a-z0-9-+&@#\/%?=~_|!:,.;]*[a-z0-9-+&@#\/%=~_|])/

        // works, but doesn't include www.sutternow.com
        def testRegex4 = ~/((http|ftp|https)(:\\/\\/[\w\-_]+)((\.[\w\-_]+)+)([\w\-\.,@?^=%&:\/~\+#]*[\w\-\@?^=%&\/~\+#])?)/

                 String result = content.replaceAll(regex2) {
            "<a href='${it[0].trim()}' target='_blank'>${it[0].trim()}</a>"

        }

        /*
        notes regex3,regex2 works for http and https links
         */

        println "result is $result"

    }
}
