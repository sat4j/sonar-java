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
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.java.model.AbstractTypedTree;
import org.sonar.java.resolve.Type;
import org.sonar.plugins.java.api.tree.SynchronizedStatementTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.Tree.Kind;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import java.util.List;

@Rule(
  key = "S2442",
  name = "\"Lock\" objects should not be \"synchronized\"",
  tags = {"multi-threading", "clumsy"},
  priority = Priority.BLOCKER)
@ActivatedByDefault
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.UNDERSTANDABILITY)
@SqaleConstantRemediation("15min")
public class SynchronizedLockCheck extends SubscriptionBaseVisitor {

  @Override
  public List<Kind> nodesToVisit() {
    return ImmutableList.of(Kind.SYNCHRONIZED_STATEMENT);
  }

  @Override
  public void visitNode(Tree tree) {
    Type syncArgumentType = ((AbstractTypedTree) ((SynchronizedStatementTree) tree).expression()).getSymbolType();
    if (syncArgumentType.isSubtypeOf("java.util.concurrent.locks.Lock")) {
      addIssue(tree, "Synchronize on this \"Lock\" object using \"acquire/release\".");
    }
  }

}
