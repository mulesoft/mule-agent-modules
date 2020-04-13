/**
 * (c) 2003-2020 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.common.internalhandler.serializer;

import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.SerializedString;

/**
 * Custom character escapes.
 */
public class CustomCharacterEscapes extends CharacterEscapes
{
    private final int[] asciiEscapes;

    public CustomCharacterEscapes()
    {
        // start with set of characters known to require escaping (double-quote, backslash etc)
        int[] esc = CharacterEscapes.standardAsciiEscapesForJSON();
        // and force escaping of a few others:
        esc['\t'] = CharacterEscapes.ESCAPE_CUSTOM;
        esc['\r'] = CharacterEscapes.ESCAPE_CUSTOM;
        esc['\n'] = CharacterEscapes.ESCAPE_CUSTOM;
        asciiEscapes = esc;
    }

    @Override
    public int[] getEscapeCodesForAscii()
    {
        return asciiEscapes;
    }

    @Override
    public SerializableString getEscapeSequence(int i)
    {
        return new SerializedString("");
    }
}
