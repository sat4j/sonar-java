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

import org.sonar.java.bytecode.asm.AsmMethod;

import java.util.HashSet;
import java.util.Set;

public final class SerializableContract {

  private static Set<String> serializableContractMethods = new HashSet<String>();

  static {
    serializableContractMethods.add("writeObject");
    serializableContractMethods.add("readObject");
    serializableContractMethods.add("writeReplace");
    serializableContractMethods.add("readResolve");
  }

  private SerializableContract() {
  }

  protected static boolean methodMatch(AsmMethod method) {
    return serializableContractMethods.contains(method.getName());
  }

}
