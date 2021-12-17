package semantic;

import syntax.ast.*;

public class TestIdent extends syntax.ast.AstVisitorDefault {
	/** Le Writer pour impression. */
	  private final main.IndentWriter out = new main.IndentWriter();

	  /** L'attribut "Intérieur de Méthode". */
	  private boolean insideMethod;

	  /** Constructeur.
	   * @param semTree l'arbre Sémantique */
	  public TestIdent(final SemanticTree semTree) {
	    out.print("= Test des Identificateurs : ");
	    insideMethod = false;
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
	    out.print(n.methodId.name + " (method), ");
	    insideMethod = true;
	    defaultVisit(n);
	    insideMethod = false;
	  }

	  @Override
	  public void visit(final Klass n) {
	    out.print(n.klassId.name + " (klasse),");
	    defaultVisit(n);
	  }
}
