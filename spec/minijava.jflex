package syntax;
%% 
%include Jflex.include
%include JflexCup.include

/// Macros
WS         = [ \t\f] | \R /* White Space */
EOLComment = "//" .*
C89Comment = "/*" [^*]* ("*" ([^*/] [^*]*)?)* "*/"
Ignore     = {WS} | {EOLComment} | {C89Comment}
Integer    = 0 | [1-9] [0-9]*
Boolean    = "true" | "false"
Ident      = [:jletter:] [:jletterdigit:]*

%%
//// Mots Clés
"class"   { return TOKEN(CLASS);   }
"main"    { return TOKEN(MAIN);    }
"out"     { return TOKEN(OUT);     }
"println" { return TOKEN(PRINTLN); }
"public"  { return TOKEN(PUBLIC);  }
"static"  { return TOKEN(STATIC);  }
"String"  { return TOKEN(STRING);  }
"System"  { return TOKEN(SYSTEM);  }
"void"    { return TOKEN(VOID);    }
"new"     { return TOKEN(NEW);     }
"int"     { return TOKEN(INT);     }
"boolean" { return TOKEN(BOOL);    }
"extends" { return TOKEN(EXTENDS); }
"return"  { return TOKEN(RETURN);  }
"while"   { return TOKEN(WHILE);   }
"if"      { return TOKEN(IF);      }
"else"    { return TOKEN(ELSE);    }
//// Operateurs
"&&"      { return TOKEN(AND);    }
"<"       { return TOKEN(LESS);    }
"+"       { return TOKEN(PLUS);    }
"-"       { return TOKEN(MINUS);   }
"*"       { return TOKEN(TIMES);    }
"!"       { return TOKEN(NOT);     }
"="       { return TOKEN(ASSIGN);  }
//// Ponctuations 
","       { return TOKEN(COMMA);   }
"."       { return TOKEN(DOT);     }
";"       { return TOKEN(SEMI);    }
"["       { return TOKEN(LB);      }
"]"       { return TOKEN(RB);      }
"{"       { return TOKEN(LC);      }
"}"       { return TOKEN(RC);      }
"("       { return TOKEN(LP);      }
")"       { return TOKEN(RP);      }
//// literals, identificateur
{Boolean} { return TOKEN(LIT_BOOL,  Boolean.parseBoolean(yytext()));  }
{Integer} { return TOKEN(LIT_INT,   Integer.parseInt(yytext()));      }  
{Ident}   { return TOKEN(IDENT,     new String(yytext())) ;           }
//// Ignore 
{Ignore}  {}
//// Ramasse Miette
[^]       { WARN("Invalid char '"+yytext()+"'"); return TOKEN(error); }
