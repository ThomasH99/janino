
/*
 * Janino - An embedded Java[TM] compiler
 *
 * Copyright (c) 2018 Arno Unkrig. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *       following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *       following disclaimer in the documentation and/or other materials provided with the distribution.
 *    3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.codehaus.commons.compiler.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.codehaus.commons.nullanalysis.Nullable;

/**
 * Generates human-readable Java assembler code from Java bytecode.
 */
public final
class Disassembler {

    @Nullable private static Object disassemblerInstance;
    static {

        @Nullable Class<?> disassemblerClass;
        try {
            disassemblerClass = Class.forName("de.unkrig.jdisasm.Disassembler");
        } catch (ClassNotFoundException cnfe) {
            System.err.println((
                "Notice: Could not disassemble class file for logging because "
                + "\"de.unkrig.jdisasm.Disassembler\" is not on the classpath. "
                + "If you are interested in disassemblies of class files generated by JANINO, "
                + "get de.unkrig.jdisasm and put it on the classpath."
            ));
            disassemblerClass = null;
        }

        if (disassemblerClass != null) {
            try {
                Disassembler.disassemblerInstance = disassemblerClass.getConstructor().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Disassembler() {}

    /**
     * Loads a "{@code de.unkrig.jdisasm.Disassembler}" through reflection (to avoid a compile-time dependency) and
     * uses it to disassemble the given bytes to {@code System.out}.
     * <p>
     *   Prints an error message to {@code System.err} if that class cannot be loaded through the classpath.
     * </p>
     * <p>
     *   System variable {@code disasm.verbose} controls whether the disassembler operates in "verbose" mode. The
     *   default is {@code false}.
     * </p>
     */
    public static void
    disassembleToStdout(byte[] contents) {

        Object d = Disassembler.disassemblerInstance;
        if (d == null) return;

        try {
            for (String attributeName : new String[] {
                "verbose",
                "showClassPoolIndexes",
                "constantPoolDump",
                "printAllAttributes",
                "printStackMap",
                "showLineNumbers",
                "showVariableNames",
                "symbolicLabels",
            }) {
                String pv = System.getProperty("disasm." + attributeName);
                if (pv != null) {
                    boolean      argument         = Boolean.parseBoolean(pv);
                    final String setterMethodName = (
                        "set"
                        + Character.toUpperCase(attributeName.charAt(0))
                        + attributeName.substring(1)
                    );
                    d.getClass().getMethod(setterMethodName, boolean.class).invoke(d, argument);
                }
            }

            d.getClass().getMethod("disasm", InputStream.class).invoke(d, new ByteArrayInputStream(contents));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
