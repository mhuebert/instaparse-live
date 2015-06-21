# Instaparse Live

Built on the work of Alex and Mark Engelberg ([instaparse](https://github.com/Engelberg/instaparse)) and Lucas Bradstreet  ([instaparse-cljs](https://github.com/lbradstreet/instaparse-cljs)), Instaparse Live continues to follow the question: *What if context-free grammars were as easy to use as regular expressions?*

(You can try it [here](http://instaparse-live.matt.is))
 
 When I started working with Instaparse I knew nothing about writing a parser, and I made frequent mistakes. Excited by the tool but frustrated by the lack of an instant feedback cycle, I made a quick instant-preview page in ClojureScript that I found useful. After [asking](https://twitter.com/mhuebert/status/603234544679047168) the creator of instaparse-cljs if this had been done before, I decided to build the more polished version that you are now visiting.
 
 Part of my motivation was the parsing problem, but I also wanted more experience using [CodeMirror](https://codemirror.net/), Marijn Haverbeke's excellent in-browser code editor, from ClojureScript. I think there is a grand future for special-purpose browser-based coding environments.  In Instaparse Live, since programmers are my audience, instead of building a graphical interface for parsing options I let you edit the options as a Clojure map inside an editor:
   
   ![options interface](http://i.imgur.com/Hz51T7D.gif)
   
   A software designer could use Instaparse to design a small language to expose functionality to power users, write a [simple CodeMirror mode](http://codemirror.net/demo/simplemode.html) for it (I wrote a limited [ebnf mode](https://github.com/mhuebert/instaparse-live/blob/master/resources/public/js/codemirror-ebnf.js) in a few lines of javascript), and then embed a CodeMirror instance in the application - not entirely unlike how Excel gives power users formulas. 