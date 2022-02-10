/**
 *
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim;

import it.uniroma2.pellegrini.z64sim.assembler.*;
import it.uniroma2.pellegrini.z64sim.util.log.Logger;
import it.uniroma2.pellegrini.z64sim.util.log.LoggerFactory;
import org.junit.jupiter.api.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;


/**
 *
 * @author pellegrini
 */

public class AssemblerTest {

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUpMethod() {
    }

    @AfterEach
    public void tearDownMethod() {
    }

    @Test
    @DisplayName("Lexer test")
    @Disabled
    public void testLexer() {
        InputStream is = getClass().getResourceAsStream("/test.asm");
        InputStreamReader isr = new InputStreamReader(Objects.requireNonNull(is));
        JavaCharStream stream = new JavaCharStream(isr);
        AssemblerTokenManager manager = new AssemblerTokenManager(stream);
        Token token = manager.getNextToken();

        while (token != null && token.kind != AssemblerConstants.EOF) {
            token = manager.getNextToken();
        }
    }

    @Test
    @DisplayName("Parser test")
    @Disabled
    public void testParser() throws ParseException {
        InputStream is = getClass().getResourceAsStream("/test.asm");
        InputStreamReader isr = new InputStreamReader(Objects.requireNonNull(is));
        Assembler a = new Assembler(isr);
        a.Program();
    }
}