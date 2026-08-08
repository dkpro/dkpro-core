/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.eval.report;

public class Result
{
    private double fscore;
    private double precision;
    private double recall;

    public double getFscore()
    {
        return fscore;
    }

    public void setFscore(double aFscore)
    {
        fscore = aFscore;
    }

    public double getPrecision()
    {
        return precision;
    }

    public void setPrecision(double aPrecision)
    {
        precision = aPrecision;
    }

    public double getRecall()
    {
        return recall;
    }

    public void setRecall(double aRecall)
    {
        recall = aRecall;
    }
}
