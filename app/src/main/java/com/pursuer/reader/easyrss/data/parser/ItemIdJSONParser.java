/*******************************************************************************
 * Copyright (c) 2012 Pursuer (http://pursuer.me).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Pursuer - initial API and implementation
 ******************************************************************************/

package com.pursuer.reader.easyrss.data.parser;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.pursuer.reader.easyrss.data.ItemId;

public class ItemIdJSONParser {
    final private JsonParser parser;
    private OnItemIdRetrievedListener listener;

    public ItemIdJSONParser(final byte[] input) throws JsonParseException, IOException {
        final JsonFactory factory = new JsonFactory();
        this.parser = factory.createJsonParser(input);
    }

    public ItemIdJSONParser(final InputStream input) throws JsonParseException, IOException {
        final JsonFactory factory = new JsonFactory();
        this.parser = factory.createJsonParser(input);
    }

    public OnItemIdRetrievedListener getListener() {
        return listener;
    }

    public void parse() throws JsonParseException, IOException {
        ItemId itemId = new ItemId();
        int level = 0;
        while (parser.nextToken() != null) {
            final String name = parser.getCurrentName();
            switch (parser.getCurrentToken()) {
            case START_OBJECT:
            case START_ARRAY:
                level++;
                break;
            case END_OBJECT:
            case END_ARRAY:
                level--;
                break;
            case VALUE_STRING:
                if (level == 3) {
                    if ("id".equals(name)) {
                        itemId.setUid(Long.toHexString(Long.valueOf(parser.getText())));
                    } else if ("timestampUsec".equals(name)) {
                        itemId.setTimestamp(Long.valueOf(parser.getText()));
                    }
                }
                break;
            default:
                break;
            }
            if (level == 2) {
                if (itemId.getUid() != null && listener != null) {
                    listener.onItemIdRetrieved(itemId);
                }
                itemId = new ItemId();
            }
        }
        parser.close();
    }

    public void parse(final OnItemIdRetrievedListener listener) throws JsonParseException, IOException {
        setListener(listener);
        parse();
    }

    public void setListener(final OnItemIdRetrievedListener listener) {
        this.listener = listener;
    }
}
