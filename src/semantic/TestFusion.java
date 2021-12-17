package semantic;

import syntax.ast.*;

public class TestFusion extends syntax.ast.AstVisitorDefault {
	/** Le Writer pour impression. */
	  private final main.IndentWriter out = new main.IndentWriter();

	  /** L'attribut "Intérieur de Méthode". */
	  private boolean insideMethod;

	  /** Constructeur.
	   * @param semTree l'arbre Sémantique */
	  public TestFusion (final SemanticTree semTree) {
	    out.print("= Test des identificateurs à l'intérieur de leur portée respective : ");
	    semTree.axiom.accept(this);
	    main.Debug.log(out);
	  }
	  
/////////////////// Visit ////////////////////
@Override
public void visit(final Formal n) {
out.print(n.varId.name + " (formal), ");
defaultVisit(n);
}

@Override
public void visit(final Var n) {
out.print(n.varId.name + (insideMethod ? "( local)" : " (field)") + ", ");
defaultVisit(n);
}

@Override
public void visit(final Method n) {
out.print(n.methodId.name + "{");
insideMethod = true;
defaultVisit(n);
insideMethod = false;
out.print("}");
}

@Override
public void visit(final Klass n) {
out.print(n.klassId.name + "{");
defaultVisit(n);
out.print("}");
}
public void visit(final KlassMain n) {
	  out.print(n.klassId.name + "{");
	  defaultVisit(n);
	  out.print("}");
}

public void visit(final Stmt n) {
	  out.print("{");
	  defaultVisit(n);
	  out.print("}");
}
	  
	  
}
