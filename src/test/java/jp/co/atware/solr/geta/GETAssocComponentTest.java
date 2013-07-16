/*
 * Copyright 2011 atWare, Inc.
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.co.atware.solr.geta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.co.atware.solr.geta.GETAssocComponent;
import junit.framework.TestCase;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;

public class GETAssocComponentTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testInitNamedList() {
        fail("Not yet implemented");
    }

    public void testInform() {
        fail("Not yet implemented");
    }

    public void testPrepareResponseBuilder() {
        fail("Not yet implemented");
    }

    public void testProcessResponseBuilder() throws IOException {

        GETAssocComponent testee = new GETAssocComponent();
        NamedList<Object> args = new NamedList<Object>();
        NamedList<Object> val = new NamedList<Object>();
        NamedList<Object> settings = new NamedList<Object>();
        settings.add("gss3url", "http://192.168.1.61/getassoc/gss3");
        NamedList<Object> process = new NamedList<Object>();
        NamedList<Object> req = new NamedList<Object>();
        req.add("param", "q");
        req.add("type", "vector");
        List<NamedList<Object>> reqArray = new ArrayList<NamedList<Object>>();
        reqArray.add(req);
        process.add("req", reqArray);
        settings.add("process", process);
        NamedList<Object> defaults = new NamedList<Object>();
        defaults.add("target", "jyoubun");
        defaults.add("cutoff-df", "0");
        defaults.add("narticles", "10");
        defaults.add("nkeywords", "10");
        defaults.add("a-props", "title");
        defaults.add("cross-ref", "aw");
        val.add("settings", settings);
        val.add("defaults", defaults);
        args.add("geta", val);
        testee.init(args);

        ResponseBuilder rb = new ResponseBuilder();
        rb.req = new LocalSolrQueryRequest(null, "[[\"条約\",1]]", null, 0, 10,
                LocalSolrQueryRequest.emptyArgs);
        rb.rsp = new SolrQueryResponse();
        testee.process(rb);

        @SuppressWarnings("unchecked")
        NamedList<NamedList<?>> values = rb.rsp.getValues();
        NamedList<?> geta = (NamedList<?>) values.get("geta");
        assertEquals(
                "OK",
                getString(geta, "req", "q", "[[\"条約\",1]]", "gss", "result",
                        "status"));
        assertEquals(
                "5875",
                getString(geta, "req", "q", "[[\"条約\",1]]", "gss", "result",
                        "articles", "total"));
        assertEquals(
                "40812280000400000000-0000",
                getString(geta, "req", "q", "[[\"条約\",1]]", "gss", "result",
                        "articles", "cls", "article[0]", "name"));
        assertEquals(
                8.309626e-02,
                getNumber(geta, "req", "q", "[[\"条約\",1]]", "gss", "result",
                        "articles", "cls", "article[0]", "score").doubleValue());
        assertEquals(
                "航空機の不法な奪取の防止に関する条約等の当事国等（クロアチア共和国）",
                getString(geta, "req", "q", "[[\"条約\",1]]", "gss", "result",
                        "articles", "cls", "article[0]", "title"));
        assertEquals(
                "条約",
                getString(geta, "req", "q", "[[\"条約\",1]]", "gss", "result",
                        "articles", "cls", "article[0]", "keyword[0]", "name"));
        assertEquals(
                7.991136e-02,
                getNumber(geta, "req", "q", "[[\"条約\",1]]", "gss", "result",
                        "articles", "cls", "article[0]", "keyword[0]", "score")
                        .doubleValue());
        assertEquals(
                "声明",
                getString(geta, "req", "q", "[[\"条約\",1]]", "gss", "result",
                        "articles", "cls", "article[0]", "keyword[1]", "name"));
        assertEquals(
                "当局",
                getString(geta, "req", "q", "[[\"条約\",1]]", "gss", "result",
                        "articles", "cls", "article[9]", "keyword[9]", "name"));
        assertEquals(
                201,
                getNumber(geta, "req", "q", "[[\"条約\",1]]", "gss", "result",
                        "keywords", "total").intValue());
    }

    private String getString(NamedList<?> list, String... names) {
        return getValue(list, names).toString();
    }

    private Number getNumber(NamedList<?> list, String... names) {
        return (Number) getValue(list, names);
    }

    /**
     * @param list
     * @param names
     * @return
     */
    private Object getValue(NamedList<?> list, String... names) {
        Object value = list;
        for (String name : names) {
            if (name.endsWith("]") && name.indexOf('[') > 0) {
                int indexOf = name.indexOf('[');
                String nameSubstring = name.substring(0, indexOf);
                int index = Integer.parseInt(name.substring(indexOf + 1,
                        name.length() - 1));
                for (int i = 0; true; i++) {
                    if (((NamedList<?>) value).getName(i).equals(nameSubstring)
                            && --index < 0) {
                        value = ((NamedList<?>) value).getVal(i);
                        break;
                    }
                }
                if (index >= 0) {
                    return null;
                }
            } else {
                value = ((NamedList<?>) value).get(name);
            }
        }
        return value;
    }
}

// こんなのが帰ってくるはず
//<gss version="3.0" user-time="0.101">
//<result status="OK">
//<articles total="5875">
//<cls>
//<article name="40812280000400000000-0000" score="8.309626e-02" title="航空機の不法な奪取の防止に関する条約等の当事国等（クロアチア共和国）">
//<keyword name="条約" score="7.991136e-02"/>
//<keyword name="声明" score="0.000000e+00"/>
//<keyword name="証書" score="0.000000e+00"/>
//<keyword name="気候" score="0.000000e+00"/>
//<keyword name="枠組" score="0.000000e+00"/>
//<keyword name="万国" score="0.000000e+00"/>
//<keyword name="宣言" score="0.000000e+00"/>
//<keyword name="核兵器" score="1.031724e-01"/>
//<keyword name="喫水線" score="0.000000e+00"/>
//<keyword name="当局" score="0.000000e+00"/>
//</article>
//<article name="42110060001700000000-0002" score="8.300614e-02" title="南東大西洋における漁業資源の保存及び管理に関する条約 第二条">
//<keyword name="条約" score="6.792097e-02"/>
//<keyword name="声明" score="0.000000e+00"/>
//<keyword name="証書" score="0.000000e+00"/>
//<keyword name="気候" score="0.000000e+00"/>
//<keyword name="枠組" score="0.000000e+00"/>
//<keyword name="万国" score="0.000000e+00"/>
//<keyword name="宣言" score="0.000000e+00"/>
//<keyword name="核兵器" score="0.000000e+00"/>
//<keyword name="喫水線" score="0.000000e+00"/>
//<keyword name="当局" score="0.000000e+00"/>
//</article>
//<article name="34015180003900000000-0013" score="8.061791e-02" title="海上における人命の安全のための国際条約等による証書に関する省令 第十三条">
//<keyword name="条約" score="7.854810e-02"/>
//<keyword name="声明" score="0.000000e+00"/>
//<keyword name="証書" score="8.061375e-02"/>
//<keyword name="気候" score="0.000000e+00"/>
//<keyword name="枠組" score="0.000000e+00"/>
//<keyword name="万国" score="0.000000e+00"/>
//<keyword name="宣言" score="0.000000e+00"/>
//<keyword name="核兵器" score="0.000000e+00"/>
//<keyword name="喫水線" score="9.453796e-02"/>
//<keyword name="当局" score="0.000000e+00"/>
//</article>
//<article name="34015180003900000000-0009" score="8.037324e-02" title="海上における人命の安全のための国際条約等による証書に関する省令 第九条">
//<keyword name="条約" score="7.458068e-02"/>
//<keyword name="声明" score="0.000000e+00"/>
//<keyword name="証書" score="9.054735e-02"/>
//<keyword name="気候" score="0.000000e+00"/>
//<keyword name="枠組" score="0.000000e+00"/>
//<keyword name="万国" score="0.000000e+00"/>
//<keyword name="宣言" score="0.000000e+00"/>
//<keyword name="核兵器" score="0.000000e+00"/>
//<keyword name="喫水線" score="0.000000e+00"/>
//<keyword name="当局" score="0.000000e+00"/>
//</article>
//<article name="34510040011500000000-0024" score="7.895355e-02" title="民事訴訟手続に関する条約等の実施に伴う民事訴訟手続の特例等に関する法律 第二十四条">
//<keyword name="条約" score="6.893409e-02"/>
//<keyword name="声明" score="0.000000e+00"/>
//<keyword name="証書" score="0.000000e+00"/>
//<keyword name="気候" score="0.000000e+00"/>
//<keyword name="枠組" score="0.000000e+00"/>
//<keyword name="万国" score="0.000000e+00"/>
//<keyword name="宣言" score="0.000000e+00"/>
//<keyword name="核兵器" score="0.000000e+00"/>
//<keyword name="喫水線" score="0.000000e+00"/>
//<keyword name="当局" score="9.935454e-02"/>
//</article>
//<article name="42110060001700000000-0029" score="7.780239e-02" title="南東大西洋における漁業資源の保存及び管理に関する条約 第二十九条">
//<keyword name="条約" score="7.088247e-02"/>
//<keyword name="声明" score="1.632477e-01"/>
//<keyword name="証書" score="0.000000e+00"/>
//<keyword name="気候" score="0.000000e+00"/>
//<keyword name="枠組" score="0.000000e+00"/>
//<keyword name="万国" score="0.000000e+00"/>
//<keyword name="宣言" score="1.117955e-01"/>
//<keyword name="核兵器" score="0.000000e+00"/>
//<keyword name="喫水線" score="0.000000e+00"/>
//<keyword name="当局" score="0.000000e+00"/>
//</article>
//<article name="41312180000100000000-0021" score="7.757523e-02" title="外務省組織規則 第二十一条">
//<keyword name="条約" score="6.609467e-02"/>
//<keyword name="声明" score="0.000000e+00"/>
//<keyword name="証書" score="0.000000e+00"/>
//<keyword name="気候" score="1.281320e-01"/>
//<keyword name="枠組" score="1.228115e-01"/>
//<keyword name="万国" score="0.000000e+00"/>
//<keyword name="宣言" score="0.000000e+00"/>
//<keyword name="核兵器" score="0.000000e+00"/>
//<keyword name="喫水線" score="0.000000e+00"/>
//<keyword name="当局" score="0.000000e+00"/>
//</article>
//<article name="33110040008600000000-0002" score="7.725344e-02" title="万国著作権条約の実施に伴う著作権法の特例に関する法律 第二条">
//<keyword name="条約" score="6.484297e-02"/>
//<keyword name="声明" score="0.000000e+00"/>
//<keyword name="証書" score="0.000000e+00"/>
//<keyword name="気候" score="0.000000e+00"/>
//<keyword name="枠組" score="0.000000e+00"/>
//<keyword name="万国" score="1.156425e-01"/>
//<keyword name="宣言" score="0.000000e+00"/>
//<keyword name="核兵器" score="0.000000e+00"/>
//<keyword name="喫水線" score="0.000000e+00"/>
//<keyword name="当局" score="0.000000e+00"/>
//</article>
//<article name="41010040008300000000-0057" score="7.717150e-02" title="種苗法 第五十七条">
//<keyword name="条約" score="6.119369e-02"/>
//<keyword name="声明" score="0.000000e+00"/>
//<keyword name="証書" score="0.000000e+00"/>
//<keyword name="気候" score="0.000000e+00"/>
//<keyword name="枠組" score="0.000000e+00"/>
//<keyword name="万国" score="0.000000e+00"/>
//<keyword name="宣言" score="0.000000e+00"/>
//<keyword name="核兵器" score="0.000000e+00"/>
//<keyword name="喫水線" score="0.000000e+00"/>
//<keyword name="当局" score="0.000000e+00"/>
//</article>
//<article name="40810040007400000000-0004" score="7.717150e-02" title="排他的経済水域及び大陸棚に関する法律 第四条">
//<keyword name="条約" score="6.119369e-02"/>
//<keyword name="声明" score="0.000000e+00"/>
//<keyword name="証書" score="0.000000e+00"/>
//<keyword name="気候" score="0.000000e+00"/>
//<keyword name="枠組" score="0.000000e+00"/>
//<keyword name="万国" score="0.000000e+00"/>
//<keyword name="宣言" score="0.000000e+00"/>
//<keyword name="核兵器" score="0.000000e+00"/>
//<keyword name="喫水線" score="0.000000e+00"/>
//<keyword name="当局" score="0.000000e+00"/>
//</article>
//</cls>
//</articles>
//<keywords total="201">
//<cls>
//<keyword name="条約" score="3.291243e+00"/>
//<keyword name="声明" score="1.220524e+00"/>
//<keyword name="証書" score="9.250212e-01"/>
//<keyword name="気候" score="8.940994e-01"/>
//<keyword name="枠組" score="8.443259e-01"/>
//<keyword name="万国" score="7.895047e-01"/>
//<keyword name="宣言" score="7.387803e-01"/>
//<keyword name="核兵器" score="7.095448e-01"/>
//<keyword name="喫水線" score="6.383810e-01"/>
//<keyword name="当局" score="6.314809e-01"/>
//</cls>
//</keywords>
//</result>
//</gss>
