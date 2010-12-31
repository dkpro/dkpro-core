/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 * Copied from the Apache UIMA Sandbox Dictionary Annotator.
 */
package de.tudarmstadt.ukp.dkpro.core.api.featurepath;

/**
 * A Condition has a filter operator and a condition value.
 *
 * TODO: bug 17
 */
public class Condition {

   private final FilterOp operatorType;

   private final String value;

   /**
    * creates a new condition object with a filter operator and a condition
    * value.
    *
    * @param operator
    *           filter operator
    * @param value
    *           condition value
    */
   public Condition(FilterOp operator, String value) {
      this.operatorType = operator;
      this.value = value;
   }

   /**
    * Returns the condition operator type.
    *
    * @return returns the condition operator type.
    */
   public FilterOp getConditionType() {
      return this.operatorType;
   }

   /**
    * Returns the condition value.
    *
    * @return Returns the condition value
    */
   public String getValue() {
      return this.value;
   }

   /**
    * Returns the FilterOperator for the given String operator. Allowed String
    * operators are: NULL, NOT_NULL, EQUALS, NOT_EQUALS, LESS, LESS_EQ, GREATER,
    * GREATER_EQ
    *
    * @param operator
    *           operator as String
    * @return FilterOperator for the given String operator
    *
    */
   public static final FilterOp getOperator(String operator) {
      if (operator.equals("NULL")) {
         return FilterOp.NULL;
      } else if (operator.equals("NOT_NULL")) {
         return FilterOp.NOT_NULL;
      } else if (operator.equals("EQUALS")) {
         return FilterOp.EQUALS;
      } else if (operator.equals("NOT_EQUALS")) {
         return FilterOp.NOT_EQUALS;
      } else if (operator.equals("LESS")) {
         return FilterOp.LESS;
      } else if (operator.equals("LESS_EQ")) {
         return FilterOp.LESS_EQ;
      } else if (operator.equals("GREATER")) {
         return FilterOp.GREATER;
      } else if (operator.equals("GREATER_EQ")) {
         return FilterOp.GREATER_EQ;
      } else {
         return null;
      }
   }
}
