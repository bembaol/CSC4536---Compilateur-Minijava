package codegen;
import intermediate.IR;
import intermediate.ir.*;

public class ToMipsPlus extends ToMips{
	/** La taille mémoire d'un registre variable.  32 bits !! */
	protected static final int SIZEOF = 4;

	/** Les 4 registres pour les arguments de fonction. */
	protected static final Reg[] AREGS = {Reg.A0, Reg.A1, Reg.A2, Reg.A3, Reg.T0, Reg.T1, Reg.T2, Reg.T3, Reg.T4, Reg.T5, Reg.T6, Reg.T7, Reg.T8, Reg.T9};
	
	/** Les registres "t" a sauvegarder. (not usefull) */
	protected static final Reg[] T_REG_USED_LIST = {Reg.T0, Reg.T1, Reg.T2, Reg.T3, Reg.T4, Reg.T5, Reg.T6, Reg.T7, Reg.T8, Reg.T9};

	public ToMipsPlus(IR ir, Allocator allocator, MipsWriter mw) {
		super(ir,allocator, mw);
	}
	
	/** Nouvelle version de la sauvegarde de l'appellant. (avant l'appel) */
	  void callerSave() {
	    push(T_REG_USED_LIST);
	    push(Reg.FP, Reg.A3, Reg.A2, Reg.A1, Reg.A0);
	  }

	  /** Nouvelle version de la restauration de l'appellant. (retour d'appel) */
	  void callerRestore() {
	    pop(Reg.FP, Reg.A3, Reg.A2, Reg.A1, Reg.A0);
	    pop(T_REG_USED_LIST);
	  }
	
	//////////////// VISIT ///////////////
	@Override
	  public void visit(final QCall q) {
		    final String function = q.arg1.getName();
		    final int nbArg = Integer.parseInt(q.arg2.getName())+1;
		    if (nbArg != params.size()) {
		      throw new main.CompilerException("ToMips : Params error");
		    }
		    callerSave();
		    for (int i = 0; i < nbArg; i++) {
		      regLoadSaved(AREGS[i], params.get(i));
		    }
		  mw.move(Reg.FP, Reg.SP);
		  mw.plus(Reg.SP, -allocator.frameSize(function));
		  mw.jumpIn(function);
		  mw.move(Reg.SP, Reg.FP); // restore $sp
		  callerRestore();
		  regStore(Reg.V0, q.result);
		  params.clear();
	  }
	

}
