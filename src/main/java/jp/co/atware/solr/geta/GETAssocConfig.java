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

import java.util.HashMap;
import java.util.Map;

/**
 * GETAssoc連携のためのコンフィグレーション情報
 * @author satake
 */
public class GETAssocConfig {

    /**
     * クエリタイプ
     * @author satake
     */
    public static enum QueryType {
        /** フルテキストからの連想検索 */
        fulltext,
        /** ベクターからの検索（1つのGETAssoc単語として扱う） */
        word,
        /** ベクターからの検索（GETAssocの解析済みベクター形式データとして扱う） */
        vector;
    }

    /**
     * GETAssoc接続情報
     * @author satake
     */
    public static class Settings {
        public String gss3url;
        public String gss3version = "3.0";
        /** 連想検索に使うリクエストパラメータの名称と検索タイプのマッピング */
        public final Map<String, QueryType> req = new HashMap<String, QueryType>();
        /** 連想検索に使うSolr検索結果フィールドの名称と検索タイプのマッピング */
        public final Map<String, QueryType> doc = new HashMap<String, QueryType>();
    }

    /**
     * GETAssocリクエストパラメータデフォルト値
     * @author satake
     */
    public static class Defaults {

        // stage1用パラメータデフォルト値
        public String target;
        public String niwords;
        public String cutoff_df;
        public String stage1_sim;

        // stage2用パラメータデフォルト値
        public String narticles;
        public String nkeywords;
        public String yykn;
        public String nacls;
        public String nkcls;
        public String a_offset;
        public String a_props;
        public String cross_ref;
        public String stage2_sim;

        // freetext用パラメータデフォルト値
        public String stemmer;
        public String freetext_cutoff_df;

        // article用パラメータデフォルト値
        public String source;
        public String article_cutoff_df;
    }

    public final Settings settings = new Settings();
    public final Defaults defaults = new Defaults();

}
