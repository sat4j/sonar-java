/*
 * SonarQube Java
 * Copyright (C) 2012 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.java.checks;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.java.checks.methods.MethodInvocationMatcher;
import org.sonar.java.model.AbstractTypedTree;
import org.sonar.java.model.declaration.VariableTreeImpl;
import org.sonar.java.resolve.Symbol;
import org.sonar.java.resolve.Symbol.VariableSymbol;
import org.sonar.java.resolve.Type;
import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.MemberSelectExpressionTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.NewClassTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.Tree.Kind;
import org.sonar.plugins.java.api.tree.VariableTree;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import java.util.List;

@Rule(
  key = "S2130",
  name = "Parsing should be used to convert \"Strings\" to primitives",
  tags = {"performance"},
  priority = Priority.MAJOR)
@ActivatedByDefault
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.MEMORY_EFFICIENCY)
@SqaleConstantRemediation("5min")
public class StringToPrimitiveConversionCheck extends SubscriptionBaseVisitor {

  private final List<PrimitiveCheck> primitiveChecks = buildPrimitiveChecks();

  @Override
  public List<Kind> nodesToVisit() {
    return ImmutableList.of(Tree.Kind.VARIABLE, Tree.Kind.METHOD_INVOCATION);
  }

  @Override
  public void visitNode(Tree tree) {
    if (hasSemantic()) {
      if (tree.is(Tree.Kind.VARIABLE)) {
        VariableTreeImpl variableTree = (VariableTreeImpl) tree;
        Type variableType = variableTree.getSymbol().getType();
        PrimitiveCheck primitiveCheck = getPrimitiveCheck(variableType);
        ExpressionTree initializer = variableTree.initializer();
        if (primitiveCheck != null && initializer != null) {
          primitiveCheck.checkInstanciation(initializer);
        }
      } else {
        MethodInvocationTree methodInvocationTree = (MethodInvocationTree) tree;
        for (PrimitiveCheck primitiveCheck : primitiveChecks) {
          primitiveCheck.checkMethodInvocation(methodInvocationTree);
        }
      }
    }
  }

  private PrimitiveCheck getPrimitiveCheck(Type type) {
    if (!type.isPrimitive()) {
      return null;
    }
    for (PrimitiveCheck primitiveCheck : primitiveChecks) {
      if (type.isTagged(primitiveCheck.tag)) {
        return primitiveCheck;
      }
    }
    return null;
  }

  private List<PrimitiveCheck> buildPrimitiveChecks() {
    return ImmutableList.of(
      new PrimitiveCheck("int", "Integer", Type.INT),
      new PrimitiveCheck("boolean", "Boolean", Type.BOOLEAN),
      new PrimitiveCheck("byte", "Byte", Type.BYTE),
      new PrimitiveCheck("double", "Double", Type.DOUBLE),
      new PrimitiveCheck("float", "Float", Type.FLOAT),
      new PrimitiveCheck("long", "Long", Type.LONG),
      new PrimitiveCheck("short", "Short", Type.SHORT));
  }

  private class PrimitiveCheck {
    private final String primitiveName;
    private final String className;
    private final int tag;
    private final String message;
    private final MethodInvocationMatcher unboxingInvocationMatcher;
    private final MethodInvocationMatcher valueOfInvocationMatcher;

    private PrimitiveCheck(String primitiveName, String className, int tag) {
      this.primitiveName = primitiveName;
      this.className = className;
      this.tag = tag;
      this.message = "Use \"" + parseMethodName() + "\" for this string-to-" + primitiveName + " conversion.";
      this.unboxingInvocationMatcher = MethodInvocationMatcher.create()
        .typeDefinition("java.lang." + className)
        .name(primitiveName + "Value");
      this.valueOfInvocationMatcher = MethodInvocationMatcher.create()
        .typeDefinition("java.lang." + className)
        .name("valueOf")
        .addParameter("java.lang.String");
    }

    private void checkMethodInvocation(MethodInvocationTree methodInvocationTree) {
      if (unboxingInvocationMatcher.matches(methodInvocationTree, getSemanticModel())) {
        MemberSelectExpressionTree methodSelect = (MemberSelectExpressionTree) methodInvocationTree.methodSelect();
        checkInstanciation(methodSelect.expression());
      }
    }

    private void checkInstanciation(ExpressionTree expression) {
      if (isBadlyInstanciated(expression)) {
        addIssue(expression, message);
      }
    }

    private boolean isBadlyInstanciated(ExpressionTree expression) {
      boolean result = false;
      if (expression.is(Tree.Kind.NEW_CLASS)) {
        result = isStringBasedConstructor((NewClassTree) expression);
      } else if (expression.is(Tree.Kind.METHOD_INVOCATION)) {
        result = valueOfInvocationMatcher.matches((MethodInvocationTree) expression, getSemanticModel());
      } else if (expression.is(Tree.Kind.IDENTIFIER)) {
        IdentifierTree identifier = (IdentifierTree) expression;
        Symbol reference = getSemanticModel().getReference(identifier);
        if (reference != null && reference.isKind(Symbol.VAR) && getSemanticModel().getUsages(reference).size() == 1) {
          VariableSymbol variableSymbol = (VariableSymbol) reference;
          result = isBadlyInstanciatedVariable(variableSymbol);
        }
      }
      return result;
    }

    private boolean isBadlyInstanciatedVariable(VariableSymbol variableSymbol) {
      Tree tree = getSemanticModel().getTree(variableSymbol);
      if (tree != null && tree.is(Tree.Kind.VARIABLE)) {
        VariableTree variableTree = (VariableTree) tree;
        ExpressionTree initializer = variableTree.initializer();
        if (initializer != null) {
          return isBadlyInstanciated(variableTree.initializer());
        }
      }
      return false;
    }

    private boolean isStringBasedConstructor(NewClassTree newClassTree) {
      List<ExpressionTree> arguments = newClassTree.arguments();
      return ((AbstractTypedTree) arguments.get(0)).getSymbolType().is("java.lang.String");
    }

    private String parseMethodName() {
      return className + ".parse" + StringUtils.capitalize(primitiveName);
    }
  }

}
