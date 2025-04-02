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

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PredicateExpressionParser<I> {
	protected static final Pattern andOrSlashSplitter = Pattern.compile("(.*?)([&\\|\\/])");

	protected Map<String, PreProcessor> preProcessors;
	protected Map<Predicate<String>, Function<String, Function<Object[], Predicate<I>>>> simpleExpressions;


	public PredicateExpressionParser() {
		preProcessors = new LinkedHashMap<>();
		simpleExpressions = new LinkedHashMap<>();
	}

	public PreProcessor newPreProcessor() {
		return newPreProcessor("" + preProcessors.size());
	}

	public PreProcessor newPreProcessor(String id) {
		PreProcessor preProcessor = new PreProcessor();
		preProcessors.put(id, preProcessor);
		return preProcessor;
	}

	public PreProcessor getPreProcessor(int index) {
		return getPreProcessor("" + index);
	}

	public PreProcessor getPreProcessor(String id) {
		return preProcessors.get(id);
	}

	public void addSimpleExpression(
		Predicate<String> simpleExpressionsParserPredicate,
		Function<String, Function<Object[], Predicate<I>>> parser
	) {
		simpleExpressions.put(simpleExpressionsParserPredicate, parser);
	}

	protected String preProcess(String expression, Object... additionalParamters) {
		for (PreProcessor preProcessor : preProcessors.values()) {
			expression = preProcessor.process(expression, additionalParamters);
		}
		return expression;
	}

	public Predicate<I> process(String expression, Object... additionalParamters) {
		return processComplex(
			preProcess(expression, additionalParamters),
			additionalParamters
		);
	}

	protected Predicate<I> processComplex(String expression, Object... additionalParamters) {
		Map<String, Object> nestedExpressionsData = new LinkedHashMap<>();
		expression = bracketAreasToPlaceholders(expression, nestedExpressionsData);
		Matcher logicalOperatorSplitter = andOrSlashSplitter.matcher(expression + "/");
		Predicate<I> predicate = null;
		String logicalOperator = null;
		while (logicalOperatorSplitter.find()) {
			String originalPredicateUnitExpression = logicalOperatorSplitter.group(1).trim();
			String predicateUnitExpression = originalPredicateUnitExpression.startsWith("!") ?
				originalPredicateUnitExpression.split("\\!")[1] :
				originalPredicateUnitExpression;
			String nestedPredicateExpression = (String)nestedExpressionsData.get(predicateUnitExpression);
			Predicate<I> predicateUnit = nestedPredicateExpression != null ?
				processComplex(nestedPredicateExpression, additionalParamters) :
				processSimple(predicateUnitExpression, additionalParamters);
			if (originalPredicateUnitExpression.startsWith("!")) {
				predicateUnit = predicateUnit.negate();
			}
			if (predicate == null) {
				predicate = predicateUnit;
			} else if ("&".equals(logicalOperator)) {
				predicate = predicate.and(predicateUnit);
			} else if ("|".equals(logicalOperator)) {
				predicate = predicate.or(predicateUnit);
			}
			logicalOperator = logicalOperatorSplitter.group(2);
		}
		return predicate;
	}

	protected static String bracketAreasToPlaceholders(String expression, Map<String, Object> values) {
		String replacedExpression = null;
		while (!expression.equals(replacedExpression = findAndReplaceNextBracketArea(expression, values))) {
			expression = replacedExpression;
		}
		return expression;
	}

	protected static String findAndReplaceNextBracketArea(String expression, Map<String, Object> values) {
		values.computeIfAbsent("nestedIndex", key -> 0);
		int firstLeftBracketIndex = expression.indexOf("(");
		if (firstLeftBracketIndex > -1) {
			int close = findClose(expression, firstLeftBracketIndex);  // find the  close parentheses
            String bracketInnerArea = expression.substring(firstLeftBracketIndex + 1, close);
            Integer nestedIndex = (Integer)values.get("nestedIndex");
            String placeHolder = "__NESTED-"+ nestedIndex++ +"__";
            expression = expression.substring(0, firstLeftBracketIndex) + placeHolder + expression.substring(close + 1, expression.length());
            values.put("nestedIndex", nestedIndex);
            values.put(placeHolder, bracketInnerArea);
            return expression;
		}
		return expression;
	}

	protected static int findClose(String input, int start) {
        ArrayDeque<Integer> stack = new ArrayDeque<>();
        for (int index = start; index < input.length(); index++) {
            if (input.charAt(index) == '(') {
                stack.push(index);
            } else if (input.charAt(index) == ')') {
                stack.pop();
                if (stack.isEmpty()) {
                    return index;
                }
            }
        }
        throw new IllegalArgumentException("Unbalanced brackets in expression: " + input);
    }


	protected Predicate<I> processSimple(String expression, Object... additionalParamters) {
		for (Entry<Predicate<String>, Function<String, Function<Object[], Predicate<I>>>> expressionToPredicateEntry : simpleExpressions.entrySet()) {
			if (expressionToPredicateEntry.getKey().test(expression)) {
				return expressionToPredicateEntry.getValue().apply(expression).apply(additionalParamters);
			}
		}
		throw new IllegalArgumentException("Unrecognized expression: " + expression);
	}

	public static class PreProcessor {

		protected Map<Predicate<String>, Function<String, Function<Object[], String>>> simpleExpressions;

		public PreProcessor() {
			simpleExpressions = new LinkedHashMap<>();
		}

		public void addSimpleExpression(
			Predicate<String> simpleExpressionsParserPredicate,
			Function<String, Function<Object[], String>> preprocessor
		) {
			simpleExpressions.put(simpleExpressionsParserPredicate, preprocessor);
		}

		public String process(String expression, Object... additionalParamters) {
			if (expression == null) {
				return expression;
			}
			Map<String, Object> nestedExpressionsData = new LinkedHashMap<>();
			expression = bracketAreasToPlaceholders(expression, nestedExpressionsData);
			Matcher logicalOperatorSplitter = andOrSlashSplitter.matcher(expression + "/");
			while (logicalOperatorSplitter.find()) {
				String originalPredicateUnitExpression = logicalOperatorSplitter.group(1).trim();
				String predicateUnitExpression = originalPredicateUnitExpression.startsWith("!") ?
					logicalOperatorSplitter.group(1).split("\\!")[1] :
					originalPredicateUnitExpression;
				String nestedPredicateExpression = (String)nestedExpressionsData.get(predicateUnitExpression);
				String newExpression = nestedPredicateExpression != null ? process(nestedPredicateExpression, additionalParamters) :
					processSimple(predicateUnitExpression, additionalParamters);
				if (nestedPredicateExpression != null) {
					expression = expression.replace(
						originalPredicateUnitExpression,
						((originalPredicateUnitExpression.startsWith("!") ? "!" : "") + "(" + newExpression + ")"
					));
				} else {
					expression = expression.replace(
						originalPredicateUnitExpression,
						((originalPredicateUnitExpression.startsWith("!") ? "!" : "") + newExpression
					));
				}
			}
			return expression;
		}

		protected String processSimple(String expression, Object... additionalParamters) {
			for (Entry<Predicate<String>, Function<String, Function<Object[], String>>> expressionToPredicateEntry : simpleExpressions.entrySet()) {
				if (expressionToPredicateEntry.getKey().test(expression)) {
					return expressionToPredicateEntry.getValue().apply(expression).apply(additionalParamters);
				}
			}
			return expression;
		}

	}

}