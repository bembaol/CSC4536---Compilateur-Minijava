package intermediate;

import intermediate.ir.*;
import main.Debug;
import semantic.SemanticAttribut;
import semantic.SemanticTree;
import syntax.ast.*;

/** Génération de la forme intermédiaire (Code à 3 adresses). */
public class Intermediate extends AstVisitorDefault {
  /** L'arbre décoré et la table de symbol.
   * Entrée de la génération intermédiaire */
  private final SemanticTree semanticTree;

  /** La représentation intermédiaire. Sortie de la génération intermédiaire */
  private final IR ir;

  /** L'attribut synthétisé nodeVar.
   * Variable IR Temp pour résultat des expressions */
  private final SemanticAttribut<IRvariable> varAttr;

  /** L'attribut synthétisé currentMethod.
   *  Utilisé comme portée pour les variables IRtempo */
  private String currentMethod;

  /** Constructeur.
   * @param semanticTree L'arbre sémantique */
  public Intermediate(final SemanticTree semanticTree) {
    this.semanticTree = semanticTree;
    this.ir = new IR(semanticTree);
    this.varAttr = new SemanticAttribut<>();
    this.currentMethod = null;
    semanticTree.axiom.accept(this); // => visit((Axiome)axiome)
    if (Debug.INTERMED) Debug.log(ir);
  }

  /* "getter" de l'attribut Var.
   * @param n Le nœud de l'AST
   * @return La valeur de l'attribut Var */
  private IRvariable getVar(final AstNode n) { return varAttr.get(n); }

  /* "setter" de l'attribut Var.
   * @param n Le nœud de l'AST
   * @param La valeur de l'attribut Var */
  private IRvariable setVar(final AstNode n, final IRvariable irv) {
    return varAttr.set(n, irv);
  }

  /** Structure de données en sortie de la génération de code intermédiaire.
   * @return La forme intermédiaire générée */
  public IR getResult() { return ir; }

  //// Helpers
  /** Ajout d'une instruction au programme IR.
   * @param irq L'instruction ajoutée en fin de programme */
  private void add(final IRquadruple irq) { ir.program.add(irq); }

  /** Création d'un label.
   * @return Le label */
  private IRlabel newLabel() { return ir.newLabel(); }

  /** Création d'un label avec nom.
   * @param name Le nom de Label
   * @return Le label */
  private IRlabel newLabel(final String name) { return ir.newLabel(name); }

  /** Création d'une constante.
   * @param value La valeur de la constante
   * @return La constante */
  private IRconst newConst(final int value) { return ir.newConst(value); }

  /** Création d'une variable temporaire dans la portée de la méthode.
   * @return La variable temporaire */
  private IRtempo newTemp() { return ir.newTemp(currentMethod); }

  /** Acces aux variables de l'AST dans la table des symboles.
   * @param name Le nom de la variable
   * @param n Le nœud de l'AST (=> portée courrante)
   * @return La variable Intemediaire ou <b>null</b> si indéfinie */
  private IRvariable lookupVar(final String name, final AstNode n) {
    return semanticTree.scopeAttr.get(n).lookupVariable(name);
  }

  /////////////////// Visit ////////////////////
  @Override
  public void visit(final KlassMain n) {
    currentMethod = "main";
    add(new QLabel(newLabel(currentMethod)));
    defaultVisit(n);
    add(new QParam(newConst(0)));
    add(new QCallStatic(newLabel("_system_exit"), newConst(1)));
    currentMethod = null;
  }
  
  @Override
  public void visit(final Method n) {
	  currentMethod = n.methodId.name;
	  add(new QLabelMeth(newLabel(n.methodId.name)));
	  defaultVisit(n);
	  add(new QReturn(getVar(n.returnExp)));
	  currentMethod = null;
  }
  
  @Override
  public void visit(final ExprArrayLength n) {
	  defaultVisit(n);
	  setVar(n, newTemp());
	  add(new QLength(getVar(n.array), getVar(n)));
  }
  
  @Override
  public void visit(final ExprArrayLookup n) {
	  defaultVisit(n);
	  setVar(n, newTemp());
	  add(new QAssignArrayFrom(getVar(n.array), getVar(n.index), getVar(n)));
  }
  
  @Override
  public void visit(final ExprArrayNew n) {
	  defaultVisit(n);
	  setVar(n, newTemp());
	  add(new QNewArray(newLabel(n.type.toString()), getVar(n.size), getVar(n)));
  }
  
  @Override
  public void visit(final ExprCall n) {
	  defaultVisit(n);
	  add(new QParam(getVar(n.receiver)));
	  setVar(n, newTemp());
	  for (AstNode arg : n.args) { add(new QParam(getVar(arg))); }
	  add(new QCall(newLabel(n.methodId.name), newConst(n.args.size()), getVar(n)));
  }
  
  @Override
  public void visit(final ExprIdent n) {
	  defaultVisit(n);
	  setVar(n, lookupVar(n.varId.name, n));
  }
  
  @Override
  public void visit(final ExprLiteralBool n) {
	  if (n.value) {
		  setVar(n, newConst(1));
	  } else if (!n.value) {
		  setVar(n, newConst(0));
	  }
  }

  @Override
  public void visit(final ExprLiteralInt n) {
    setVar(n, newConst(n.value));
  }
  
  @Override
  public void visit(final ExprOpBin n) {
	  defaultVisit(n);
	  if (getVar(n.expr1) instanceof IRconst && getVar(n.expr2) instanceof IRconst) {
		  IRconst a = (IRconst)getVar(n.expr1);
		  IRconst b = (IRconst)getVar(n.expr2);
		  IRconst rep;
		  switch(n.op){
		  case PLUS:
			  rep = newConst(a.getValue()+b.getValue()); break;
		  case MINUS:
			  rep = newConst(a.getValue()-b.getValue()); break;
		  case TIMES:
			  rep = newConst(a.getValue()*b.getValue()); break;
		  case LESS:
			  rep = newConst(Integer.min(a.getValue(), b.getValue())); break;
		  case AND:
			  rep = newConst(((a.getValue()!=0) && (b.getValue()!=0)) ? 1 : 0); break;
		  default:
			  rep = newConst(0);
			  break;
		  }
		  setVar(n, rep);
	  }
	  else {
		  setVar(n, newTemp());
	  }
	  add(new QAssign(n.op, getVar(n.expr1), getVar(n.expr2), getVar(n)));
  }
	  
	  
  
  @Override
  public void visit(final ExprOpUn n) {
	  defaultVisit(n);
	  /* if (getVar(n.expr) instanceof IRconst) {
		  IRconst rep = (IRconst)getVar(n.expr);
		  setVar(n, newConst(rep.getValue() ? false : true));
	  } */
	  setVar(n, newTemp());
	  add(new QAssignUnary(n.op, getVar(n.expr), getVar(n)));
  }
  
  @Override
  public void visit(final ExprNew n) {
	  defaultVisit(n);
	  setVar(n, newTemp());
	  add(new QNew(newLabel(n.klassId.name), getVar(n)));
  }
  
  @Override
  public void visit(final StmtArrayAssign n) {
	  defaultVisit(n);
	  setVar(n, lookupVar(n.arrayId.name,n));
	  add(new QAssignArrayTo(getVar(n.value), getVar(n.index), getVar(n)));
  }

  @Override
  public void visit(final StmtAssign n) {
	  defaultVisit(n);
	  setVar(n, lookupVar(n.varId.name, n));
	  add(new QCopy(getVar(n.value), getVar(n)));
  }
  
  @Override
  public void visit(final StmtBlock n) {
	  defaultVisit(n);
  }
  
  @Override
  public void visit(final StmtIf n) {
	  n.test.accept(this);
	  if (n.ifFalse != null) { // si else existe
		  IRlabel L1 = newLabel();
		  add(new QJumpCond(L1, getVar(n.test)));
		  n.ifTrue.accept(this);
		  IRlabel L2 = newLabel();
		  add(new QJump(L2));
		  add(new QLabel(L1));
		  n.ifFalse.accept(this);
		  add(new QLabel(L2));
	  } else if (n.ifFalse == null) { // si else n'existe pas
		  IRlabel L2 = newLabel();
		  add(new QJumpCond(L2, getVar(n.test)));
		  n.ifTrue.accept(this);
		  add(new QLabel(L2));
		  
	  }
	  
  }
  
  @Override
  public void visit(final StmtPrint n) {
    defaultVisit(n);
    add(new QParam(getVar(n.expr)));
    add(new QCallStatic(newLabel("_system_out_println"), newConst(1)));
  }
  
  @Override
  public void visit(final StmtWhile n) {
	  IRlabel L1 = newLabel();
	  add(new QLabel(L1));
	  n.test.accept(this);
	  IRlabel L2 = newLabel();
	  add(new QJumpCond(L2, getVar(n.test)));
	  n.body.accept(this);
	  add(new QJump(L1));
	  add(new QLabel(L2));
  }

}
