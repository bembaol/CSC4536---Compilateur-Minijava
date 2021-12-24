package semantic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import main.Debug;
import semantic.symtab.*;
import syntax.ast.*;

public class UndefIdent extends syntax.ast.AstVisitorDefault {
	  /** Nom de type indéfini. */
	  private static final String VOID = main.EnumType.UNDEF.toString();
	
	/** La structure de données de l'analyse sémantique. */
	  private final SemanticTree semanticTree;

	  /** L'attribut hérité Scope.
	   * Entrée dans la table des symboles pour chaque nœud */
	  private Scope currentScope;
	  
	  /** Variables. */
	  private HashMap<String, String> unusedVar;
	  
	  /** Des erreurs de Type. */
	  private boolean error;

	  /** Constructeur.
	   * @param semanticTree the semantic tree */
	  public UndefIdent(final SemanticTree semanticTree) {
	    this.error = false;
	    this.semanticTree = semanticTree;
	    this.currentScope = semanticTree.rootScope;
	    this.unusedVar = new HashMap<String, String>();
	    Collection <InfoVar> variables = semanticTree.rootScope.getAllVariables();
	    for (InfoVar v : variables) {
	    	if (v.getName() != "this") {
	    		this.unusedVar.put(v.getName(), v.getType());
	    	}
	    }
	    semanticTree.axiom.accept(this);
	  }
	  
	  /** Sortie en erreur.
	   * @return true si identificateur pas identifié */
	  public boolean getError() { return error; }
	  
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
	  
	  /** Récupérer les variables non utilisées
	   * @return Collection des variables non utilisées
	   */
	  public HashMap<String, String> getUnusedVar(){
		  return this.unusedVar;
	  }
	
//////////////Visit ////////////////////////
	  
	  /* La gestion des classes non définies est déjà gérée dans TypeChecking, l'erreur est "Unknown Type: ..." 
	@Override
	  public void visit(final ExprNew n) {
		  defaultVisit(n);
		  String name = n.klassId.name;
		  InfoKlass v = currentScope.lookupKlass(n.klassId.name);
		  if (v == null) {
			  Debug.logErr(n + " UndefIdent : Undefined klass -> " + name);
			  error = true;
		  }
	  }
	  */
	  
	  
	  @Override
	  public void visit(final ExprIdent n) {
		  defaultVisit(n);
		  String name = n.varId.name;
		  String nameType = lookupVarType(n, name);
		  this.unusedVar.remove(name, nameType);
		  if (nameType == "undef") {
			  Debug.logErr(n + " UndefIdent : Undefined variable -> " + name);
			  error = true;
		  }
	  }
	  
	  
	  @Override
	  public void visit(final StmtAssign n) {
		  defaultVisit(n);
		  final String nameType = lookupVarType(n, n.varId.name);
		  if (nameType == "undef") {
			  Debug.logErr(n + " UndefIdent : Undefined variable -> " + n.varId.name);
			  error = true;
		  }
	  }
	  
	  /* La gestion des méthodes non définies est déjà gérée dans TypeChecking, l'erreur est "Attempt to call a non-method..." */
	  
	  @Override
	  public void visit(final StmtArrayAssign n) {
		  defaultVisit(n);
		  final String nameType = lookupVarType(n, n.arrayId.name);
		  if (nameType == "undef") {
			  Debug.logErr(n + " UndefIdent : Undefined array -> " + n.arrayId.name);
			  error = true;
		  }
	  }

}
