package semantic;

import semantic.symtab.*;
import syntax.ast.*;

/** Contrôle de Type.
 * <ul><li>Calcule l'attribut synthétisé Type : requis pour les nœuds Expr*
 * <li>Vérifie les contraintes de Typage de minijava </ul>*/
public class TypeChecking extends AstVisitorDefault {
  // revoir une classe Type adéquat pour union primitif/class. String inadéquat
  /** Nom de type booleen. */
  private static final String BOOL = main.EnumType.BOOL.toString();
  /** Nom de type entier. */
  private static final String INT = main.EnumType.INT.toString();
  /** Nom de type indéfini. */
  private static final String VOID = main.EnumType.UNDEF.toString();

  /** La structure de données de l'analyse sémantique. */
  private final SemanticTree semanticTree;

  /** Des erreurs de Type. */
  private boolean error;

  /** Constructeur.
   * @param semanticTree the semantic tree */
  public TypeChecking(final SemanticTree semanticTree) {
    this.error = false;
    this.semanticTree = semanticTree;
    semanticTree.axiom.accept(this);
  }

  /** Sortie en erreur.
   * @return Des erreurs sémantiques de typage */
  public boolean getError() { return error; }

  // //// Helpers
  /** "getter" pour l'attribut Type.
   * @param n Le nœud de l'AST
   * @return le nom de type */
  private String getType(final AstNode n) {
    return semanticTree.typeAttr.get(n);
  }

  /** "setter" pour l'attribut Type.
  * @param n Le nœud de l'AST
  * @param type le nom de type */
  private void setType(final AstNode n, final String type) {
    semanticTree.typeAttr.set(n, type);
  }

  /** "getter" pour l'attribut Scope.
  * @param n Le nœud de l'AST
  * @return La portée courante du nœud */
  private Scope getScope(final AstNode n) {
    return semanticTree.scopeAttr.get(n);
  }

  /** Recherche d'une classe dans la table des symboles".
   * @param name Le nom de la classe
   * @return La définition de la classe ou <b>null</b> si indéfinie */
  private InfoKlass lookupKlass(final String name) {
    return semanticTree.rootScope.lookupKlass(name);
  }

  //// helpers
  /** Rapport d'erreur.
   * @param where Le nœud de l'AST en faute
   * @param msg Le message d'erreur */
  private void erreur(final AstNode where, final String msg) {
    main.Debug.logErr(where + " " + msg);
    error = true;
  }

  /** Comparaison de type.
   * @param t1 Nom de type 1
   * @param t2 Nom de type 2
   * @return true si t2 est sous-type de t1 */
  private boolean compareType(final String t1, final String t2) {
    if (t2 == null) return false;
    if (t2.equals(t1)) return true;
    // sinon (t1 ancêtre de t2) ?
    final InfoKlass kl2 = lookupKlass(t2);
    if (kl2 != null) return compareType(t1, kl2.getParent());
    return false;
    // NB : Suppose héritage valide !!!
  }

  /** Validation du transtypage implicite.
   * @param t1 Le type attendu
   * @param t2 Le type testé
   * @param msg Le message d'erreur si t2 ne cast pas en t1
   * @param where Le nœud de l'AST en faute */
  private void checkType(final String t1, final String t2,
      final String msg, final AstNode where) {
    if (!compareType(t1, t2)) {
      erreur(where, "Wrong Type : " + t2 + "->" + t1 + ";  " + msg);
    }
  }

  /** Validation du nom de type.
   * @param type Le nom de type testé
   * @param where Le nœud de l'AST en faute */
  private void checkTypeName(final String type, final AstNode where) {
    if (type.equals(BOOL) || type.equals(INT)) return;
    if (type.equals(VOID)) return;
    if (lookupKlass(type) != null) return;
    erreur(where, "Unknown Type : " + type);
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

  /////////////////// Visit ////////////////////
  // Visites spécifiques : (non defaultvisit)
  //  - Expr* : set Type
  //  - Stmt* + Expr* (sauf exceptions) : Compatibilité des Types
  //  - Type : Validité des noms de type dans Var, Method, Formal
  //  - Method : returnType compatible avec Type(returnExpr)
  // NB : validité des déclarations de classe prérequis (checkInheritance)

  @Override
  public void visit(final Method n) {
	  defaultVisit(n);
	  checkType(n.returnType.name, getType(n.returnExp), "returnType incompatible with Type(returnExp)", n);
  }

  @Override
  public void visit(final Type n) {
	  defaultVisit(n);
	  checkTypeName(n.name, n);
  }
  
  @Override
  public void visit(final ExprCall n) {
	  n.receiver.accept(this);
	  final InfoKlass kl = lookupKlass(getType(n.receiver));
	  if (kl == null) {
	    erreur(n, "Attempt to call a non-method (unknown class)");
	    setType(n, VOID);
	    return;
	  }
	  
	  n.methodId.accept(this);
      final InfoMethod m = kl.getScope().lookupMethod(n.methodId.name);
	  if (m == null) {
	    erreur(n, "Attempt to call a non-method (unknown method)");
	    setType(n, VOID);
	    return;
	  }
	  if (m.getArgs().length != n.args.size() + 1) {
	    erreur(n, "Call of method " + m + " does not match the number of args");
	    setType(n, VOID);
	    return;
	  }
	  int i = 1;
	  for (AstNode f : n.args) {
	    f.accept(this);
	    checkType(m.getArgs()[i++].getType(), getType(f),
	        "Call of method does not match the signature :" + m, n);
	  }
	  setType(n, m.getReturnType());
  }
  
  @Override
  public void visit(final ExprIdent n) {
	  defaultVisit(n);
	  setType(n, lookupVarType(n, n.varId.name));
  }
  
  @Override
  public void visit(final ExprLiteralInt n) {
    setType(n, INT);
  }
  
  @Override
  public void visit(final ExprLiteralBool n) {
	  setType(n, BOOL);
  }
  
  @Override
  public void visit(final ExprNew n) {
    defaultVisit(n);
    checkTypeName(n.klassId.name, n);
    setType(n, n.klassId.name); // Type = ClassName
  }
  
  @Override
  public void visit(final ExprOpBin n) {
    defaultVisit(n);
    final String msg1 = "Invalid Type for left arg " + n.op;
    final String msg2 = "Invalid Type for right arg " + n.op;
    switch (n.op) {
      case PLUS:
      case MINUS:
      case TIMES:
        checkType(INT, getType(n.expr1), msg1, n);
        checkType(INT, getType(n.expr2), msg2, n);
        setType(n, INT);
        break;
      case AND:
        checkType(BOOL, getType(n.expr1), msg1, n);
        checkType(BOOL, getType(n.expr2), msg2, n);
        setType(n, BOOL);
        break;
      case LESS:
        checkType(INT, getType(n.expr1), msg1, n);
        checkType(INT, getType(n.expr2), msg2, n);
        setType(n, BOOL);
        break;
      default:
        setType(n, VOID);
        break;
    }
  }

  @Override
  public void visit(final ExprOpUn n) {
    defaultVisit(n);
    final String msg = "Invalid Type for operator " + n.op;
    if (n.op == main.EnumOper.NOT) {
      checkType(BOOL, getType(n.expr), msg, n);
      setType(n, BOOL);
    } else setType(n, VOID);
  }
  
  @Override
  public void visit(final StmtAssign n) {
	  defaultVisit(n);
	  checkType(lookupVarType(n, n.varId.name), getType(n.value), "Type mismatch in assignment", n);
  }
  
  @Override
  public void visit(final StmtBlock n) {
	  defaultVisit(n);
  }
  
  @Override
  public void visit(final StmtIf n) {
	  defaultVisit(n);
	  checkType(BOOL, getType(n.test), "Test is not a boolean", n);
  }

  @Override
  public void visit(final StmtPrint n) {
    defaultVisit(n);
    checkType(INT, getType(n.expr), "non integer for printing", n);
  }
  
  @Override
  public void visit(final StmtWhile n) {
	  defaultVisit(n);
	  checkType(BOOL, getType(n.test), "Test is not a boolean", n);
  }
  @Override
  public void visit(final StmtArrayAssign n) {
	  defaultVisit(n);
	  checkType(INT, getType(n.index), "Index must be an integer", n);
	  checkType(INT, getType(n.value), "Value must be an integer", n);
  }
  

  


}
