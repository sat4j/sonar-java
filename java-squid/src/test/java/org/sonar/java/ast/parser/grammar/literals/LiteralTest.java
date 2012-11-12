/*
 * Sonar Java
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
package org.sonar.java.ast.parser.grammar.literals;

import org.junit.Test;
import org.sonar.java.ast.api.JavaGrammar;
import org.sonar.java.ast.parser.JavaGrammarImpl;

import static org.sonar.sslr.tests.Assertions.assertThat;

public class LiteralTest {

  JavaGrammar g = new JavaGrammarImpl();

  @Test
  public void ok() {
    g.trueKeyword.mock();
    g.falseKeyword.mock();
    g.nullKeyword.mock();
    g.characterLiteral.mock();
    g.stringLiteral.mock();
    g.floatLiteral.mock();
    g.longLiteral.mock();
    g.integerLiteral.mock();

    assertThat(g.literal)
        .matches("trueKeyword")
        .matches("falseKeyword")
        .matches("nullKeyword")
        .matches("characterLiteral")
        .matches("stringLiteral")
        .matches("floatLiteral")
        .matches("longLiteral")
        .matches("integerLiteral");
  }

  @Test
  public void realLife() {
    assertThat(g.literal)
        .matches("1.0")
        .matches("1")
        .matches("'a'")
        .matches("\"string\"")
        .matches("true")
        .matches("false")
        .matches("null");
  }

}
