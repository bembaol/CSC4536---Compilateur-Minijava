package semantic;

import main.Debug;
import semantic.symtab.*;
import syntax.ast.*;

public class UndefIdent extends syntax.ast.AstVisitorDefault {
	/** Nom de type booleen. */
	  private static final String BOOL = main.EnumType.BOOL.toString();
	  /** Nom de type entier. */
	  private static final String INT = main.EnumType.INT.toString();
	  /** Nom de type indéfini. */
	  private static final String VOID = main.EnumType.UNDEF.toString();
	  /** Nom de type tableau d'Integer. */
	  private static final String INT_ARRAY = main.EnumType.INT_ARRAY.toString();
	
	/** La structure de données de l'analyse sémantique. */
	  private final SemanticTree semanticTree;

	  /** L'attribut hérité Scope.
	   * Entrée dans la table des symboles pour chaque nœud */
	  private Scope currentScope;
	  
	  /** Des erreurs de Type. */
	  private boolean error;

	  /** Constructeur.
	   * @param semanticTree the semantic tree */
	  public UndefIdent(final SemanticTree semanticTree) {
	    this.error = false;
	    this.semanticTree = semanticTree;
	    this.currentScope = semanticTree.rootScope;
	    semanticTree.axiom.accept(this);
	  }
	  
	  /** Sortie en erreur.
	   * @return true si identificateur pas identifié */
	  public boolean getError() { return error; }
	  
	  /** "getter" pour l'attribut Type.
	   * @param n Le nœud de l'AST
	   * @return le nom de type */
	  private String getType(final AstNode n) {
	    return semanticTree.typeAttr.get(n);
	  }
	  
	  /** "getter" pour l'attribut Scope.
	   * @param n Le nœud de l'AST
	   * @return La portée courante du nœud */
	   private Scope getScope(final AstNode n) {
	     return semanticTree.scopeAttr.get(n);
	   }
	  
	  /** Recherche du type d'une variable dans la table des symboles.
	   * @param n Le nœud de l'AST (pour obtenir la portée courrante
	   * @param name Le nom de la variable
	   * @return Le nom de type (VOID pour type inconnu) */
	  private String lookupVarType(final AstNode n, final String name) {
	    final InfoVar v = getScope(n).lookupVariable(name);
	    if (v == null) return VOID;
	    else return v.getType();
	  }
	
//////////////Visit ////////////////////////
	  
	  
	@Override
	  public void visit(final ExprNew n) {
		  defaultVisit(n);
		  String name = n.klassId.name;
		  InfoKlass v = currentScope.lookupKlass(n.klassId.name);
		  if (v == null) {
			  Debug.logErr(n + "UndefIdent : Undefined klass -> " + name);
			  error = true;
		  }
	  }
	  
	  
	  @Override
	  public void visit(final ExprIdent n) {
		  defaultVisit(n);
		  String nameType = lookupVarType(n, n.varId.name);
		  if (nameType == "undef") {
			  Debug.logErr(n + "UndefIdent : Undefined variable -> " + n.varId.name);
			  error = true;
		  }
	  }
	  
	  
	  @Override
	  public void visit(final StmtAssign n) {
		  defaultVisit(n);
		  final String nameType = lookupVarType(n, n.varId.name);
		  if (nameType == "undef") {
			  Debug.logErr(n + "UndefIdent : Undefined variable -> " + n.varId.name);
			  error = true;
		  }
	  }
	  
	  /* La gestion des méthodes non définies est déjà gérée dans TypeChecking, l'erreur est "Attempt to call a non-method..." */
	  

}
