package semantic;

import syntax.ast.*;

public class TestScope extends syntax.ast.AstVisitorDefault {
	/** Le Writer pour impression. */
	  private final main.IndentWriter out = new main.IndentWriter();

	  /** L'attribut "Intérieur de Méthode". */
	  private boolean insideMethod;

	  /** Constructeur.
	   * @param semTree l'arbre Sémantique */
	  public TestScope(final SemanticTree semTree) {
	    out.print("= Test de la portée : ");
	    semTree.axiom.accept(this);
	    main.Debug.log(out);
	  }
	  
	  // Visit
	  public void visit(final KlassMain n) {
		  out.print(n.klassId.name + "{");
		  defaultVisit(n);
		  out.print("}");
	  }
	  
	  public void visit(final Klass n) {
		  out.print(n.klassId.name + "{");
		  defaultVisit(n);
		  out.print("}");
	  }
	  
	  public void visit (final Method n) {
		  out.print(n.methodId.name + "{");
		  defaultVisit(n);
		  out.print("}");
	  }
	  
	  public void visit(final Stmt n) {
		  out.print("{");
		  defaultVisit(n);
		  out.print("}");
	  }
}
