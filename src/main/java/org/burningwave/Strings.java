/*
 * This file is part of Burningwave Common.
 *
 * Author: Roberto Gentili
 *
 * Hosted at: https://github.com/burningwave/common
 *
 * --
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2023 Roberto Gentili
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO
 * EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.burningwave;


import java.util.Objects;
import java.util.UUID;

public class Strings {
	public static final Strings INSTANCE;

	static {
		INSTANCE = new Strings();
	}

	private Strings() {}

	public String capitalizeFirstCharacter(String value) {
		return Character.toString(value.charAt(0)).toUpperCase()
		+ value.substring(1, value.length());
	}


	public String compile(String message, Object... arguments) {
		for (Object obj : arguments) {
			message = message.replaceFirst("\\{\\}", Objects.isNull(obj) ? "null" : clear(obj.toString()));
		}
		return message;
	}

	private String clear(String text) {
		return text
		.replace("\\", "\\\\\\")
		.replace("{", "\\{")
		.replace("}", "\\}")
		.replace("(", "\\(")
		.replace(")", "\\)")
		.replace(".", "\\.")
		.replace("$", "\\$");
	}

	public String camelCasedToHyphened(String input) {
		return camelCasedToHyphened(input, false);
	}


	public String camelCasedToHyphened(String input, boolean slashToUnderscore) {
		StringBuilder output = new StringBuilder(
			String.valueOf(input.charAt(0) == '/' && slashToUnderscore ? '_' : Character.toLowerCase(input.charAt(0)))
		);
		for (int i = 1; i < input.length(); i++) {
			String character = Character.isLowerCase(input.charAt(i)) ?
				String.valueOf(
					input.charAt(i)
				):
				"-" + Character.toLowerCase(input.charAt(i)
			);
			output.append(
				input.charAt(i) == '/' && slashToUnderscore ?
					String.valueOf("_") :
					character
            );
		}
		return output.toString();
	}



	public String hyphenedToCamelCase(String input) {
		return toCamelCase(input, "-");
	}

	public String underscoredToCamelCase(String input) {
		return toCamelCase(input, "_");
	}

	public String dottedToCamelCase(String input) {
		return toCamelCase(input, "\\.");
	}

	private String toCamelCase(String input, String separator){
	   String[] parts = input.toLowerCase().split(separator);
	   StringBuilder output = new StringBuilder(parts[0]);
	   for (int i = 1; i < parts.length; i++) {
		   output.append(toProperCase(parts[i]));
	   }
	   return output.toString();
	}

	private String toProperCase(String input) {
	    return input.substring(0, 1).toUpperCase() +
	               input.substring(1).toLowerCase();
	}

	public String removeIndexesAndBrackets(String path) {
		return toUnindexed(path, "");
	}

	public String removeIndexes(String path) {
		return toUnindexed(path, "$1$3");
	}

	public String toUnindexed(String path, String replacement) {
		try {
			return path.replaceAll("(\\[)(.*?)(\\])", replacement);
		} catch (NullPointerException exc) {
			if (path == null) {//NOSONAR
				return null;
			}
			throw exc;
		}
	}

    public String normalizePath(String path) {
        if (path == null)
            return null;

        // Create a place for the normalized path
        String normalized = path;

        if (normalized.equals("/."))
            return "/";

        // Add a leading "/" if necessary
        if (!normalized.startsWith("/"))
            normalized = "/" + normalized;

        // Resolve occurrences of "//" in the normalized path
        while (true) {
            int index = normalized.indexOf("//");
            if (index < 0)
                break;
            normalized = normalized.substring(0, index) + normalized.substring(index + 1);
        }

        // Resolve occurrences of "/./" in the normalized path
        while (true) {
            int index = normalized.indexOf("/./");
            if (index < 0)
                break;
            normalized = normalized.substring(0, index) + normalized.substring(index + 2);
        }

        // Resolve occurrences of "/../" in the normalized path
        while (true) {
            int index = normalized.indexOf("/../");
            if (index < 0)
                break;
            if (index == 0)
                return (null); // Trying to go outside our context
            int index2 = normalized.lastIndexOf('/', index - 1);
            normalized = normalized.substring(0, index2) + normalized.substring(index + 3);
        }

        // Return the normalized path that we have completed
        return (normalized);

    }

	public String toOrdinalNumber(int number) {
		String[] suffixes = { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
	    switch (number % 100) {
	    case 11:
	    case 12:
	    case 13:
	        return number + "th";
	    default:
	        return number + suffixes[number % 10];
	    }

	}

	public String toStringWithRandomUUIDSuffix(String value) {
		return value + "-" + UUID.randomUUID().toString();
	}

}
