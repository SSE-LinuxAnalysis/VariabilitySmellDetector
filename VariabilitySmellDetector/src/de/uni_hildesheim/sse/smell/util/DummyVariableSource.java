package de.uni_hildesheim.sse.smell.util;

import java.util.HashMap;

import de.uni_hildesheim.sse.model_extender.convert.VariableSource;
import net.ssehub.easy.varModel.cst.Variable;
import net.ssehub.easy.varModel.cst.VariablePool;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.datatypes.BooleanType;

/**
 * A small dummy variable source.
 * @author Adam Krafczyk
 */
class DummyVariableSource implements VariableSource {

    private HashMap<String, AbstractVariable> varDecls = new HashMap<String, AbstractVariable>();
    private VariablePool varPool = new VariablePool();
    
    @Override
    public Variable getVariable(String name) {
        AbstractVariable decl = varDecls.get(name);
        if (decl == null) {
            decl = new DecisionVariableDeclaration(name, BooleanType.TYPE, null);
            varDecls.put(name, decl);
        }
        return varPool.obtainVariable(decl);
    }
    
}