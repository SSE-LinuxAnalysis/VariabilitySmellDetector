package de.uni_hildesheim.sse.smell.util.dnf;

import java.util.HashMap;
import java.util.Map;

import de.uni_hildesheim.sse.smell.util.ConstraintException;
import net.ssehub.easy.varModel.cst.AttributeVariable;
import net.ssehub.easy.varModel.cst.BlockExpression;
import net.ssehub.easy.varModel.cst.Comment;
import net.ssehub.easy.varModel.cst.CompoundAccess;
import net.ssehub.easy.varModel.cst.CompoundInitializer;
import net.ssehub.easy.varModel.cst.ConstantValue;
import net.ssehub.easy.varModel.cst.ContainerInitializer;
import net.ssehub.easy.varModel.cst.ContainerOperationCall;
import net.ssehub.easy.varModel.cst.IConstraintTreeVisitor;
import net.ssehub.easy.varModel.cst.IfThen;
import net.ssehub.easy.varModel.cst.Let;
import net.ssehub.easy.varModel.cst.OCLFeatureCall;
import net.ssehub.easy.varModel.cst.Parenthesis;
import net.ssehub.easy.varModel.cst.Self;
import net.ssehub.easy.varModel.cst.UnresolvedExpression;
import net.ssehub.easy.varModel.cst.Variable;
import net.ssehub.easy.varModel.model.datatypes.OclKeyWords;
import net.ssehub.easy.varModel.model.values.BooleanValue;

class CstToFormulaConverter implements IConstraintTreeVisitor {
    
    private Formula result;
    
    private Map<String, de.uni_hildesheim.sse.smell.util.dnf.Variable> variables;
    
    public CstToFormulaConverter() {
        this.variables = new HashMap<>();
    }
    
    public Formula getResult() {
        return result;
    }
    
    public de.uni_hildesheim.sse.smell.util.dnf.Variable[] getVariables() {
        de.uni_hildesheim.sse.smell.util.dnf.Variable[] vars = new de.uni_hildesheim.sse.smell.util.dnf.Variable[variables.size()];
        
        int i = 0;
        for (de.uni_hildesheim.sse.smell.util.dnf.Variable var : variables.values()) {
            vars[i++] = var;
        }
        
        return vars;
    }

    @Override
    public void visitConstantValue(ConstantValue value) {
        if (value.getConstantValue() instanceof BooleanValue) {
            BooleanValue v = (BooleanValue) value.getConstantValue();
            result = new Constant(v.getValue());
        } else {
            throw new RuntimeException(new ConstraintException("Invalid element in CST"));
        }
    }

    @Override
    public void visitVariable(Variable variable) {
        String name = variable.getVariable().getName();
        if (!variables.containsKey(name)) {
            variables.put(name, new de.uni_hildesheim.sse.smell.util.dnf.Variable(name));
        }
        result = variables.get(name);
    }

    @Override
    public void visitAnnotationVariable(AttributeVariable variable) {
        throw new RuntimeException(new ConstraintException("Invalid element in CST"));
    }

    @Override
    public void visitParenthesis(Parenthesis parenthesis) {
        throw new RuntimeException(new ConstraintException("Invalid element in CST"));
    }

    @Override
    public void visitComment(Comment comment) {
        throw new RuntimeException(new ConstraintException("Invalid element in CST"));
    }

    @Override
    public void visitOclFeatureCall(OCLFeatureCall call) {
        if (call.getOperation().equals(OclKeyWords.OR))  {
            call.getOperand().accept(this);
            Formula left = getResult();
            call.getParameter(0).accept(this);
            Formula right = getResult();
            
            result = new Disjunction(left, right);
            
        } else if (call.getOperation().equals(OclKeyWords.AND))  {
            call.getOperand().accept(this);
            Formula left = getResult();
            call.getParameter(0).accept(this);
            Formula right = getResult();
            
            result = new Conjunction(left, right);
            
        } else if (call.getOperation().equals(OclKeyWords.NOT))  {
            call.getOperand().accept(this);
            Formula nested = getResult();
            result = new Negation(nested);
            
        } else {
            throw new RuntimeException(new ConstraintException("Invalid operation in CST: " + call.getOperation()));
        }
    }

    @Override
    public void visitLet(Let let) {
        throw new RuntimeException(new ConstraintException("Invalid element in CST"));
    }

    @Override
    public void visitIfThen(IfThen ifThen) {
        throw new RuntimeException(new ConstraintException("Invalid element in CST"));
    }

    @Override
    public void visitContainerOperationCall(ContainerOperationCall call) {
        throw new RuntimeException(new ConstraintException("Invalid element in CST"));
    }

    @Override
    public void visitCompoundAccess(CompoundAccess access) {
        throw new RuntimeException(new ConstraintException("Invalid element in CST"));
    }

    @Override
    public void visitUnresolvedExpression(UnresolvedExpression expression) {
        throw new RuntimeException(new ConstraintException("Invalid element in CST"));
    }

    @Override
    public void visitCompoundInitializer(CompoundInitializer initializer) {
        throw new RuntimeException(new ConstraintException("Invalid element in CST"));
    }

    @Override
    public void visitContainerInitializer(ContainerInitializer initializer) {
        throw new RuntimeException(new ConstraintException("Invalid element in CST"));
    }

    @Override
    public void visitSelf(Self self) {
        throw new RuntimeException(new ConstraintException("Invalid element in CST"));
    }

    @Override
    public void visitBlockExpression(BlockExpression block) {
        throw new RuntimeException(new ConstraintException("Invalid element in CST"));
    }
    
}
