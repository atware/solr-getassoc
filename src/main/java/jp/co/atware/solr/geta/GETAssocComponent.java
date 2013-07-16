/*
 * Copyright 2011 atWare, Inc.
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.co.atware.solr.geta;

import static jp.co.atware.solr.geta.GETAssocConsts.*;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import jp.co.atware.solr.geta.GETAssocConfig.QueryType;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.MapFieldSelector;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.SolrIndexSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Solrの検索結果にGETAssocの連想検索を連動させるためのコンポーネント。
 * 
 * @author satake
 */
public class GETAssocComponent extends SearchComponent {

    /** LOG */
    private static final Logger LOG = LoggerFactory
            .getLogger(GETAssocComponent.class);

    /** config */
    private GETAssocConfig config;

    /** valueTransMap */
    private HashMap<String, ValueOf> valueTransMap;

    /**
     * {@inheritDoc}
     * 
     * @see SearchComponent#init(NamedList)
     */
    @Override
    public void init(@SuppressWarnings("rawtypes") NamedList args) {
        super.init(args);

        GETAssocConfig config = new GETAssocConfig();
        if (args != null) {
            NamedList<?> geta = (NamedList<?>) args.get(CONFIG_GETA);
            if (geta != null) {

                // GETA接続設定系コンフィグパラメータの読み込み
                NamedList<?> settings = (NamedList<?>) geta.get("settings");
                if (settings != null) {
                    config.settings.gss3url = (String) settings
                            .get(CONFIG_GSS3URL);
                    NamedList<?> process = (NamedList<?>) settings
                            .get("process");
                    @SuppressWarnings("unchecked")
                    List<NamedList<?>> req = (List<NamedList<?>>) process
                            .get("req");
                    if (req != null) {
                        for (NamedList<?> list : req) {
                            String param = (String) list.get("param");
                            String type = (String) list.get("type");
                            QueryType queryType = GETAssocConfig.QueryType
                                    .valueOf(type);
                            if (param != null && !param.isEmpty()) {
                                config.settings.req.put(param, queryType);
                            }
                        }
                    }
                    @SuppressWarnings("unchecked")
                    List<NamedList<?>> doc = (List<NamedList<?>>) process
                            .get("doc");
                    if (doc != null) {
                        for (NamedList<?> list : doc) {
                            String param = (String) list.get("field");
                            String type = (String) list.get("type");
                            QueryType queryType = GETAssocConfig.QueryType
                                    .valueOf(type);
                            if (param != null && !param.isEmpty()) {
                                config.settings.doc.put(param, queryType);
                            }
                        }
                    }
                }

                // 初期値系コンフィグパラメータの読み込み
                NamedList<?> defaults = (NamedList<?>) geta.get("defaults");
                if (defaults != null) {

                    // stage1
                    config.defaults.target = (String) defaults
                            .get(CONFIG_TARGET);
                    config.defaults.niwords = (String) defaults
                            .get(CONFIG_NIWORDS);
                    config.defaults.cutoff_df = (String) defaults
                            .get(CONFIG_CUTOFF_DF);
                    config.defaults.stage1_sim = (String) defaults
                            .get(CONFIG_STAGE1_SIM);

                    // stage2
                    config.defaults.narticles = (String) defaults
                            .get(CONFIG_NARTICLES);
                    config.defaults.nkeywords = (String) defaults
                            .get(CONFIG_NKEYWORDS);
                    config.defaults.yykn = (String) defaults.get(CONFIG_YYKN);
                    config.defaults.nacls = (String) defaults.get(CONFIG_NACLS);
                    config.defaults.nkcls = (String) defaults.get(CONFIG_NKCLS);
                    config.defaults.a_offset = (String) defaults
                            .get(CONFIG_A_OFFSET);
                    config.defaults.a_props = (String) defaults
                            .get(CONFIG_A_PROPS);
                    config.defaults.cross_ref = (String) defaults
                            .get(CONFIG_CROSS_REF);
                    config.defaults.stage2_sim = (String) defaults
                            .get(CONFIG_STAGE2_SIM);

                    // freetext
                    config.defaults.stemmer = (String) defaults
                            .get(CONFIG_STEMMER);
                    config.defaults.freetext_cutoff_df = (String) defaults
                            .get(CONFIG_FREETEXT_CUTOFF_DF);

                    // article
                    config.defaults.source = (String) defaults
                            .get(CONFIG_SOURCE);
                    config.defaults.article_cutoff_df = (String) defaults
                            .get(CONFIG_ARTICLE_CUTOFF_DF);
                }
            }
        }
        if (config.settings.gss3url == null) {
            throw new IllegalStateException("Config '" + CONFIG_GETA + "/"
                    + "settings" + "/" + CONFIG_GSS3URL + "' is requierd.");
        }
        if (config.settings.req.isEmpty() && config.settings.doc.isEmpty()) {
            config.settings.req.put(CommonParams.Q, QueryType.fulltext);
        }

        this.config = config;

        HashMap<String, ValueOf> map = new HashMap<String, ValueOf>();
        map.put("score", ValueOf.DOUBLE);
        map.put("total", ValueOf.INT);
        map.put("user-time", ValueOf.FLOAT);

        this.valueTransMap = map;
    }

    /**
     * {@inheritDoc}
     * 
     * @see SearchComponent#prepare(ResponseBuilder)
     */
    @Override
    public void prepare(ResponseBuilder rb) throws IOException {
        // リクエスト毎に行うべき前処理は特に無い。
    }

    /**
     * {@inheritDoc}
     * 
     * @see SearchComponent#process(ResponseBuilder)
     */
    @Override
    public void process(ResponseBuilder rb) throws IOException {

        NamedList<Object> result = new NamedList<Object>();
        HttpClient client = new HttpClient();

        // リクエストパラメータからの検索を行う(geta.settings.process.req[*])
        if (!config.settings.req.isEmpty()) {
            NamedList<Object> paramList = new NamedList<Object>();
            for (Entry<String, QueryType> entry : config.settings.req
                    .entrySet()) {
                String param = entry.getKey();
                NamedList<Object> paramValueList = new NamedList<Object>();
                String[] paramValues = rb.req.getParams().getParams(param);
                if (paramValues != null) {
                    for (String paramValue : paramValues) {
                        NamedList<Object> getaResultList = convertResult(postGss3Request(
                                client,
                                convertRequest(rb.req.getParams(), paramValue,
                                        entry.getValue())));
                        paramValueList.add(paramValue, getaResultList);
                    }
                }
                paramList.add(param, paramValueList);
            }
            result.add("req", paramList);
        }

        // 検索結果からの検索を行う(geta.settings.process.doc[*])
        if (!config.settings.doc.isEmpty()) {
            NamedList<Object> docList = new NamedList<Object>();

            SolrIndexSearcher searcher = rb.req.getSearcher();
            IndexSchema schema = searcher.getSchema();
            String key = schema.getUniqueKeyField().getName();
            List<String> targetFieldNames = new ArrayList<String>(
                    config.settings.doc.size() + 1);
            targetFieldNames.add(key);
            targetFieldNames.addAll(config.settings.doc.keySet());
            FieldSelector selector = new MapFieldSelector(targetFieldNames);

            DocIterator iterator = rb.getResults().docList.iterator();
            while (iterator.hasNext()) {
                Document doc = searcher.doc(iterator.next(), selector);
                String docKey = schema.printableUniqueKey(doc);

                NamedList<Object> fieldList = new NamedList<Object>();
                for (Entry<String, QueryType> entry : config.settings.doc
                        .entrySet()) {

                    String field = entry.getKey();

                    NamedList<Object> queryList = new NamedList<Object>();
                    for (Fieldable fieldable : doc.getFieldables(field)) {
                        NamedList<Object> getaResultList = convertResult(postGss3Request(
                                client,
                                convertRequest(rb.req.getParams(),
                                        fieldable.stringValue(),
                                        entry.getValue())));
                        queryList.add(fieldable.stringValue(), getaResultList);
                    }
                    fieldList.add(entry.getKey(), queryList);
                }
                docList.add(docKey, fieldList);
            }
            result.add("doc", docList);
        }

        if (result.size() != 0) {
            rb.rsp.add("geta", result);
        }
    }

    /**
     * 
     * @param requestBody
     * @return
     * @throws IOException
     */
    protected InputStream postGss3Request(HttpClient client, String requestBody)
            throws IOException {
        PostMethod post = new PostMethod(config.settings.gss3url);
        RequestEntity requestEntity = new StringRequestEntity(requestBody,
                "text/xml", "UTF-8");
        post.setRequestEntity(requestEntity);
        post.addRequestHeader("User-Agent", "SolrGETAssocPlugin");

        int state = client.executeMethod(post);
        if (state == 200) {
            return post.getResponseBodyAsStream();
        }
        throw new IOException(post.getStatusLine().toString());
    }

    /**
     * GETAssoc向けリクエストを作成します。
     * 
     * @param params
     * @param queryValue
     * @param queryType
     * @return
     * @throws FactoryConfigurationError
     * @throws IOException
     */
    protected String convertRequest(SolrParams params, String queryValue,
            QueryType queryType) throws FactoryConfigurationError, IOException {

        String req;
        try {
            CharArrayWriter output = new CharArrayWriter();
            XMLStreamWriter xml = XMLOutputFactory.newInstance()
                    .createXMLStreamWriter(output);
            xml.writeStartDocument();
            xml.writeStartElement("gss");
            if (config.settings.gss3version != null) {
                xml.writeAttribute("version", config.settings.gss3version);
            }
            xml.writeStartElement("assoc");
            String target = params.get(PARAM_TARGET, config.defaults.target);
            if (target != null) {
                xml.writeAttribute("target", target);
            }
            convertRequestWriteStage1Param(xml, params);
            convertRequestWriteStage2Param(xml, params);

            convReqWriteQuery(xml, params, queryValue, queryType);

            xml.writeEndElement();
            xml.writeEndElement();
            xml.writeEndDocument();
            xml.close();
            req = output.toString();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
        LOG.debug(req);
        return req;
    }

    /**
     * 
     * @param xml
     * @param params
     * @param queryValue
     * @param queryType
     * @throws XMLStreamException
     */
    protected void convReqWriteQuery(XMLStreamWriter xml, SolrParams params,
            String queryValue, QueryType queryType) throws XMLStreamException {

        if (queryType == QueryType.fulltext) {
            // freetext
            xml.writeStartElement("freetext");
            String stemmer = params.get(PARAM_STEMMER, config.defaults.stemmer);
            if (stemmer != null) {
                xml.writeAttribute("stemmer", stemmer);
            }
            String freetext_cutoff_df = params.get(PARAM_FREETEXT_CUTOFF_DF,
                    config.defaults.freetext_cutoff_df);
            if (config.defaults.source != null) {
                xml.writeAttribute("cutoff-df", freetext_cutoff_df);
            }
            xml.writeCData(queryValue);
            xml.writeEndElement();
        } else {
            // article
            xml.writeStartElement("article");
            if (queryType == QueryType.vector) {
                xml.writeAttribute("vec", queryValue);
            } else {
                xml.writeAttribute("name", queryValue);
            }
            String source = params.get(PARAM_SOURCE, config.defaults.source);
            if (source != null) {
                xml.writeAttribute("source", source);
            }
            String article_cutoff_df = params.get(PARAM_ARTICLE_CUTOFF_DF,
                    config.defaults.article_cutoff_df);
            if (source != null) {
                xml.writeAttribute("cutoff-df", article_cutoff_df);
            }
            xml.writeEndElement();
        }
    }

    /**
     * 
     * @param xml
     * @param params
     * @throws XMLStreamException
     */
    protected void convertRequestWriteStage2Param(XMLStreamWriter xml,
            SolrParams params) throws XMLStreamException {
        convReqWriteAttr(xml, params, PARAM_NARTICLES,
                config.defaults.narticles, "narticles");
        convReqWriteAttr(xml, params, PARAM_NKEYWORDS,
                config.defaults.nkeywords, "nkeywords");
        convReqWriteAttr(xml, params, PARAM_YYKN, config.defaults.yykn, "yykn");
        convReqWriteAttr(xml, params, PARAM_NACLS, config.defaults.nacls,
                "nacls");
        convReqWriteAttr(xml, params, PARAM_NKCLS, config.defaults.nkcls,
                "nkcls");
        convReqWriteAttr(xml, params, PARAM_A_OFFSET, config.defaults.a_offset,
                "a-offset");
        convReqWriteAttr(xml, params, PARAM_A_PROPS, config.defaults.a_props,
                "a-props");
        convReqWriteAttr(xml, params, PARAM_CROSS_REF,
                config.defaults.cross_ref, "cross-ref");
        convReqWriteAttr(xml, params, PARAM_STAGE2_SIM,
                config.defaults.stage2_sim, "stage2-sim");
    }

    /**
     * Stage1向けパラメータの設定
     * 
     * @param xml XMLWriter
     * @param params SolrParams
     * @throws XMLStreamException
     */
    protected void convertRequestWriteStage1Param(XMLStreamWriter xml,
            SolrParams params) throws XMLStreamException {
        convReqWriteAttr(xml, params, PARAM_NIWORDS, config.defaults.niwords,
                "niwords");
        convReqWriteAttr(xml, params, PARAM_CUTOFF_DF,
                config.defaults.cutoff_df, "cutoff-df");
        convReqWriteAttr(xml, params, PARAM_STAGE1_SIM,
                config.defaults.stage1_sim, "stage1-sim");
    }

    /**
     * SolrパラメータをGETAリクエストXMLの属性として記述する。
     * 指定のSolrパラメータで格納されている値をGETAリクエストの属性の値として出力します。
     * Solrパラメータが値をもたない場合はデフォルト値を使用します。
     * 
     * @param xml XMLWriter
     * @param params SolrParams
     * @param paramName Solrパラメータ名
     * @param defaultValue デフォルト値
     * @param getaName GETAリクエスト属性名
     * @throws XMLStreamException
     */
    protected void convReqWriteAttr(XMLStreamWriter xml, SolrParams params,
            String paramName, String defaultValue, String getaName)
            throws XMLStreamException {
        String value = params.get(paramName, defaultValue);
        if (value != null) {
            xml.writeAttribute(getaName, value);
        }
    }

    /**
     * GETAssocの連想検索結果を読み込み、<code>NamedList</code>として構成します。
     * 
     * @param inputStream GETAssocの連想検索結果
     * @return <code>NamedList</code>表現
     * @throws FactoryConfigurationError
     * @throws IOException
     */
    protected NamedList<Object> convertResult(InputStream inputStream)
            throws FactoryConfigurationError, IOException {
        NamedList<Object> result = new NamedList<Object>();
        LinkedList<NamedList<Object>> stack = new LinkedList<NamedList<Object>>();
        stack.push(result);
        try {
            XMLStreamReader xml = XMLInputFactory.newInstance()
                    .createXMLStreamReader(inputStream);
            while (xml.hasNext()) {
                switch (xml.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    NamedList<Object> element = new NamedList<Object>();
                    stack.peek().add(xml.getName().toString(), element);
                    stack.push(element);
                    for (int i = 0; i < xml.getAttributeCount(); i++) {
                        String name = xml.getAttributeName(i).toString();
                        String value = xml.getAttributeValue(i);
                        ValueOf valueOf = valueTransMap.get(name);
                        if (valueOf != null) {
                            try {
                                element.add(name, valueOf.toValue(value));
                            } catch (NumberFormatException e) {
                                element.add(name, value);
                            }
                        } else {
                            element.add(name, value);
                        }
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    stack.pop();
                    break;
                default:
                    break;
                }
                xml.next();

            }
            xml.close();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }

        LOG.debug(result.toString());
        return result;
    }

    /**
     * 値の変換用クラス
     * 
     * @author satake
     */
    private static enum ValueOf {
        BOOL {
            @Override
            public Boolean toValue(String str) {
                return Boolean.valueOf(str);
            }
        },
        INT {
            @Override
            public Integer toValue(String str) throws NumberFormatException {
                return Integer.valueOf(str);
            }
        },
        FLOAT {
            @Override
            public Float toValue(String str) throws NumberFormatException {
                return Float.valueOf(str);
            }
        },
        DOUBLE {
            @Override
            public Double toValue(String str) throws NumberFormatException {
                return Double.valueOf(str);
            }
        };
        public abstract Object toValue(String str) throws NumberFormatException;
    }

    // ///////////////////////////////////////////
    // / SolrInfoMBean
    // //////////////////////////////////////////

    @Override
    public String getDescription() {
        return "GETAssoc component";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getSourceId() {
        return "GETAssocComponent.java 1.0.0";
    }

    @Override
    public String getSource() {
        return "GETAssocComponent.java";
    }
}
