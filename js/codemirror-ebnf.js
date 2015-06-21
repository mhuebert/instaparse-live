CodeMirror.defineSimpleMode("ebnf", {
  start: [
    {regex: /(.*?)(=|:|::=|=)([^\(]|\([^\*])*/, sol: true, token: ["keyword", "operator", null]},
    {regex: /\(\*/, token: "comment", next: "comment"}
  ],
     // The multi-line comment state.
     comment: [
       {regex: /.*?\*\)/, token: "comment", next: "start"},
       {regex: /.*/, token: "comment"}
     ],
})
