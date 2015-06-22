CodeMirror.defineSimpleMode("ebnf", {
  start: [
  {regex: /\(\*/, token: "comment", next: "comment"},
  {regex: /(.*?)(=|:|::=|=)/, sol: true, token: ["keyword", "operator"]},
  {regex: /([^\(]|\([^\*])/, token: null}

  ],
     // The multi-line comment state.
     comment: [
       {regex: /.*?\*\)/, token: "comment", next: "start"},
       {regex: /.*/, token: "comment"}
     ],
})
