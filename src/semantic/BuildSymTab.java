package semantic;

import java.util.Collection;

import main.Debug;
import semantic.symtab.*;
import syntax.ast.*;

/** Construction de la Table de Symboles. */
public class BuildSymTab extends AstVisitorDefault {
  /** La structure de données de l'analyse sémantique. */
  private final SemanticTree semanticTree;

  /** L'attribut hérité Scope.
   * Entrée dans la table des symboles pour chaque nœud */
  private Scope currentScope;

  /** L'attribut hérité Klass. Fournit le Type de la variable "this". */
  private InfoKlass currentKlass;

  /** Des erreurs de redéfinition dans la table de symbole. */
  private boolean error;

  /** Constructeur.
   * @param semanticTree L'arbre sémantique */
  public BuildSymTab(final SemanticTree semanticTree) {
    this.error = false;
    this.semanticTree = semanticTree;
    this.currentScope = semanticTree.rootScope;
    this.currentKlass = null;
    addObjectKlass();
    semanticTree.axiom.accept(this);
  }

  /** Sortie en erreur.
   * @return true si erreurs sémantiques de redéfinition des symboles */
  public boolean getError() { return error; }

  // helpers ...
  /** "setter" de l'attribut "Scope".
   * @param n Le nœud AST
   * @param sc le scope */
  private void setScope(final AstNode n, final Scope sc) {
    semanticTree.scopeAttr.set(n, sc);
  }

  /** "getter" de l'attribut "Scope".
   * @param n Le nœud AST
   * @return le scope */
  private Scope getScope(final AstNode n) {
    return semanticTree.scopeAttr.get(n);
  }

  /** Ajout d'une déclaration de classe et création nouvelle portée.
   * @param sc La portée courante
   * @param kl La définition de classe
   * @return La portée pour la nouvelle classe */
  private Scope newKlassScope(final AstNode n, final Scope sc, final InfoKlass kl) {
    checkRedef(n, sc.insertKlass(kl));
    final Scope fils = new Scope(sc, kl.getName());
    kl.setScope(fils);
    return fils;
  }

  /** Ajout d'une déclaration de Méthode et création de 2 nouvelles portées.
   *  Inclus l'ajout des paramètres formels dans la portée intermédiaire
   * @param sc La portée courante
   * @param m La définition de la méthode
   * @return La portée pour la nouvelle méthode */
  private Scope newMethodScope(final AstNode n, final Scope sc, final InfoMethod m) {
    checkRedef(n, sc.insertMethod(m));
    final Scope fils = new Scope(sc, m.getName() + "_args");
    for (InfoVar v : m.getArgs()) checkRedef(n, fils.insertVariable(v));
    final Scope pf = new Scope(fils, m.getName());
    m.setScope(pf);
    return pf;
  }

  /** Gestion des redéfinitions dans une même portée.
   * NB : HashMap.add() non null => already exists
   * @param i La déclaration á tester */
  private void checkRedef(final AstNode n, final Info i) {
    if (i != null) {
      Debug.logErr(n + "BuildSymtab : Duplication d'identificateur " + i);
      error = true;
    }
  }

  /** Création de la classe Object avec méthode equals().
   * La classe est nécessaite comme racine de la hierarchie des classes.
   * La méthode equals est pour le fun.
   */
  private void addObjectKlass() {
    Scope sc = currentScope; //==rootScope
    final InfoKlass kl = new InfoKlass("Object", null);
    sc = newKlassScope(null, sc, kl);
    final InfoMethod m = new InfoMethod("boolean", "equals",
        new InfoVar("this", kl.getName()),
        new InfoVar("o", kl.getName()));
    sc = newMethodScope(null, sc, m);
  }

  ////////////// Visit ////////////////////////
  // visite par défaut avec gestion de l'attribut hérité currentScope
  @Override
  public void defaultVisit(final AstNode n) {
    setScope(n, currentScope);
    for (AstNode f : n) f.accept(this);
    currentScope = getScope(n);
  }

  // Visites Spécifiques : (non defaultVisit)
  // - new Scope : KlassMain. Klass, Method, StmtBlock
  // - Déclarations : KlassMain, Klass, Method, Var, (Formal in Method)

  /* Not really usefull !!
  @Override
  public void visit(final KlassMain n) {
	setScope(n, currentScope);
    n.klassId.accept(this);
    final InfoKlass kl = new InfoKlass(n.klassId.name, "Object");
    currentKlass = kl;
    currentScope = newKlassScope(currentScope, kl);
    n.argId.accept(this);
    final java.util.List<InfoVar> formals = new java.util.ArrayList<>();
    formals.add(new InfoVar(n.argId.name, "String[]"));
    final InfoMethod m = new InfoMethod("void", "main",formals);
    currentScope = newMethodScope(currentScope, m);
    n.stmt.accept(this);
    currentKlass = null;
    currentScope = getScope(n);
  } */
  
 
  

  
  
  
  /*
  @Override
  public void visit(final ExprCall n) {
	  currentScope = semanticTree.rootScope;
	  if (currentScope.lookupMethod(n.methodId.name)==null) {
		  Debug.logErr(n + "BuildSymTab : undefined method -> " + n.methodId.name);
	  }
  }
  */
  
  @Override
  public void visit(final Klass n) {
	  setScope(n, currentScope);
	  n.klassId.accept(this);
	  n.parentId.accept(this);
	  final InfoKlass kl = new InfoKlass(n.klassId.name, n.parentId.name);
	  currentKlass = kl;
	  currentScope = newKlassScope(n, currentScope, kl);
	  n.vars.accept(this);
	  n.methods.accept(this);
	  currentKlass = null;
	  currentScope = getScope(n); 
  }
  
  @Override
  public void visit (final Method n) {
	  setScope(n, currentScope);
	  n.returnType.accept(this);
	  n.methodId.accept(this);
	  n.fargs.accept(this);
	  final java.util.List<InfoVar> formals = new java.util.ArrayList<>(); 
	  formals.add(new InfoVar("this", currentKlass.getName()));
	  for (AstNode f : n.fargs) { 
		  Formal s = (Formal)f;
		  formals.add(new InfoVar(s.varId.name, s.typeId.name)); 
	  }
	  final InfoMethod m = new InfoMethod(n.returnType.name, n.methodId.name,formals);
	  currentScope = newMethodScope(n, currentScope, m);
	  n.vars.accept(this);
	  n.stmts.accept(this);
	  n.returnExp.accept(this);
	  currentScope = getScope(n);
  }
  
  @Override
  public void visit (final StmtBlock n) {
	  setScope(n, currentScope);
	  currentScope = new Scope(currentScope);
	  n.stmts.accept(this);
	  n.vars.accept(this);
	  currentScope = getScope(n);
  }
  
  @Override
  public void visit (final Var n) {
	  n.typeId.accept(this);
	  n.varId.accept(this);
	  final InfoVar v = new InfoVar(n.varId.name, n.typeId.name);
	  checkRedef(n, currentScope.insertVariable(v));
	  currentScope.insertVariable(v);
  }
  
  @Override
  public void visit(final ExprNew n) {
	  defaultVisit(n);
	  String name = n.klassId.name;
	  InfoKlass v = currentScope.lookupKlass(n.klassId.name);
	  if (v == null) {
		  Debug.logErr(n + "BuildSymTab : Undefined klass -> " + name);
	  }
  }
  
  @Override
  public void visit(final ExprIdent n) {
	  defaultVisit(n);
	  String name = n.varId.name;
	  InfoVar v = currentScope.lookupVariable(name);
	  if (v==null) {
		  Debug.logErr(n + "BuildSymTab : Undefined variable -> " + name);
	  } 
  }
  
  @Override
  public void visit(final StmtAssign n) {
	  defaultVisit(n);
	  String name = n.varId.name;
	  InfoVar v = currentScope.lookupVariable(name);
	  if (v==null) {
		  Debug.logErr(n + "BuildSymTab : Undefined variable -> " + name);
	  } 
  }

}
