/*******************************************************************************
 * Copyright 2015
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.io.reuters;

import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * A class that holds text and metadata for a Reuters-21578 document.
 */
public class ReutersDocument
{
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
            "dd-MMM-yyyy hh:mm:ss.SS", Locale.US);

    private LEWISSPLIT lewissplit;
    private CGISPLIT cgisplit;
    private int oldid;
    private int newid;
    private Date date;
    private Set<String> topics;
    private Set<String> places;
    private Set<String> people;
    private Set<String> orgs;
    private Set<String> exchanges;
    private Set<String> companies;
    private String unknown;
    private String title;
    private String dateline;
    private String body;
    private Path path;

    public ReutersDocument()
    {
        topics = new HashSet<>();
        places = new HashSet<>();
        people = new HashSet<>();
        orgs = new HashSet<>();
        exchanges = new HashSet<>();
        companies = new HashSet<>();
    }

    public Path getPath()
    {
        return path;
    }

    public void setPath(Path path)
    {
        this.path = path;
    }

    public void set(String key, Set<String> value)
    {
        switch (key) {
        case "TOPICS":
            setTopics(value);
            break;
        case "PLACES":
            setPlaces(value);
            break;
        case "PEOPLE":
            setPeople(value);
            break;
        case "ORGS":
            setOrgs(value);
            break;
        case "EXCHANGES":
            setExchanges(value);
            break;
        case "COMPANIES":
            setCompanies(value);
            break;
        }
    }

    public void set(String key, String value)
            throws ParseException
    {
        switch (key) {
        case "LEWISSPLIT":
            setLewissplit(value);
            break;
        case "CGISPLIT":
            setCgisplit(value);
            break;
        case "OLDID":
            setOldid(value);
            break;
        case "NEWID":
            setNewid(value);
            break;
        case "DATE":
            setDate(value);
            break;
        case "TOPICS":
            addTopic(value);
            break;
        case "PLACES":
            addPlace(value);
            break;
        case "PEOPLE":
            addPeople(value);
            break;
        case "ORGS":
            addOrg(value);
            break;
        case "EXCHANGES":
            addExchange(value);
            break;
        case "COMPANIES":
            addCompany(value);
            break;
        case "UNKNOWN":
            setUnknown(value);
            break;
        case "TITLE":
            setTitle(value);
            break;
        case "DATELINE":
            setDateline(value);
            break;
        case "BODY":
            setBody(value);
            break;
        default:
            throw new IllegalArgumentException(
                    "Unrecognized key/value pair: '" + key + "'/'" + value + "'.");
        }
    }

    private void addCompany(String value)
    {
        companies.add(value);
    }

    private void addExchange(String value)
    {
        exchanges.add(value);
    }

    private void addOrg(String value)
    {
        orgs.add(value);
    }

    private void addPeople(String value)
    {
        people.add(value);
    }

    private void addPlace(String value)
    {
        places.add(value);
    }

    private void addTopic(String value)
    {
        topics.add(value);
    }

    private void setNewid(String value)
    {
        setNewid(Integer.parseInt(value));
    }

    private void setOldid(String value)
    {
        setOldid(Integer.parseInt(value));
    }

    public int getOldid()
    {
        return oldid;
    }

    public void setOldid(int oldid)
    {
        this.oldid = oldid;
    }

    public LEWISSPLIT getLewissplit()
    {
        return lewissplit;
    }

    public void setLewissplit(LEWISSPLIT lewissplit)
    {
        this.lewissplit = lewissplit;
    }

    public void setLewissplit(String lewissplit)
    {
        setLewissplit(LEWISSPLIT.valueOf(lewissplit.toUpperCase().replaceAll("-", "_")));
    }

    public CGISPLIT getCgisplit()
    {
        return cgisplit;
    }

    public void setCgisplit(String cgisplit)
    {
        setCgisplit(CGISPLIT.valueOf(cgisplit.toUpperCase().replaceAll("-", "_")));
    }

    public void setCgisplit(CGISPLIT cgisplit)
    {
        this.cgisplit = cgisplit;
    }

    public int getNewid()
    {
        return newid;
    }

    public void setNewid(int newid)
    {
        this.newid = newid;
    }

    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }

    public void setDate(String date)
            throws ParseException
    {
        setDate(dateFormat.parse(date));
    }

    public Set<String> getTopics()
    {
        return topics;
    }

    public void setTopics(Set<String> topics)
    {
        this.topics = topics;
    }

    public Set<String> getPlaces()
    {
        return places;
    }

    public void setPlaces(Set<String> places)
    {
        this.places = places;
    }

    public Set<String> getPeople()
    {
        return people;
    }

    public void setPeople(Set<String> people)
    {
        this.people = people;
    }

    public Set<String> getOrgs()
    {
        return orgs;
    }

    public void setOrgs(Set<String> orgs)
    {
        this.orgs = orgs;
    }

    public Set<String> getExchanges()
    {
        return exchanges;
    }

    public void setExchanges(Set<String> exchanges)
    {
        this.exchanges = exchanges;
    }

    public Set<String> getCompanies()
    {
        return companies;
    }

    public void setCompanies(Set<String> companies)
    {
        this.companies = companies;
    }

    public String getUnknown()
    {
        return unknown;
    }

    public void setUnknown(String unknown)
    {
        this.unknown = unknown;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getDateline()
    {
        return dateline;
    }

    public void setDateline(String dateline)
    {
        this.dateline = dateline;
    }

    public String getBody()
    {
        return body;
    }

    public void setBody(String body)
    {
        this.body = body;
    }

    public enum LEWISSPLIT
    {
        TRAIN, TEST, NOT_USED
    }

    public enum CGISPLIT
    {
        TRAINING_SET, PUBLISHED_TESTSET
    }

}
