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
package org.sonar.java.checks;

import com.sonar.sslr.squid.checks.CheckMessagesVerifier;
import org.junit.Test;
import org.sonar.java.JavaAstScanner;
import org.sonar.squid.api.SourceFile;

import java.io.File;

public class EmptyBlock_S00108_CheckTest {

  private EmptyBlock_S00108_Check check = new EmptyBlock_S00108_Check();

  @Test
  public void test() {
    SourceFile file = JavaAstScanner.scanSingleFile(new File("src/test/files/checks/EmptyBlock.java"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
        .next().atLine(2).withMessage("Either remove or fill this block of code.")
        .next().atLine(9)
        .next().atLine(17)
        .next().atLine(27)
        .next().atLine(30)
        .next().atLine(31)
        .next().atLine(32)
        .next().atLine(43)
        .noMore();
  }

}